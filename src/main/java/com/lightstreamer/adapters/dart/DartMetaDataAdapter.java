/*
Copyright (c) Lightstreamer Srl

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.lightstreamer.adapters.dart;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.adapters.dart.engine3D.Universe;
import com.lightstreamer.adapters.dart.room.ChatRoom;
import com.lightstreamer.adapter.common.InitException;
import com.lightstreamer.adapter.metadata.CreditsException;
import com.lightstreamer.adapter.metadata.ItemsException;
import com.lightstreamer.adapter.metadata.LoginInfo;
import com.lightstreamer.adapter.metadata.SessionListener;
import com.lightstreamer.adapter.metadata.SessionRequestHandler;
import com.lightstreamer.adapter.metadata.SubscribedItemConfiguration;
import com.lightstreamer.adapter.metadata.MetadataAdapter;
import com.lightstreamer.adapter.metadata.SubscriptionHandler;
import com.lightstreamer.adapter.metadata.SubscriptionInfo;

public class DartMetaDataAdapter implements MetadataAdapter {

    private String feedAdapterName;
    private final Set<String> ids = ConcurrentHashMap.newKeySet();
    
    private DartDataAdapter feed;

    private Logger logger;

    @Override
    public void init(Map<String, String> params, File configDir) throws InitException {
        /*
        String logConfig = (String) params.get("log_config");
        if (logConfig != null) {
            File logConfigFile = new File(configDir, logConfig);
            String logRefresh = (String) params.get("log_config_refresh_seconds");
            if (logRefresh != null) {
                DOMConfigurator.configureAndWatch(logConfigFile.getAbsolutePath(), Integer.parseInt(logRefresh) * 1000);
            } else {
                DOMConfigurator.configure(logConfigFile.getAbsolutePath());
            }
        } //else the bridge to logback is expected
        */
        
        logger = LogManager.getLogger(Constants.LOGGER_CAT);
    }
    
    @Override
    public void onDataAdapterAvailable(String dataAdapterName, Object dataAdapter) {
        if (!(dataAdapter instanceof DartDataAdapter)) {
            return;
        }
        if (this.feed != null) {
            logger.warn("Unexpected second Data Adapter: " + dataAdapterName);
            return;
        }
        this.feed = (DartDataAdapter) dataAdapter;
        this.feedAdapterName = dataAdapterName;
        logger.info("Data Adapter available: " + dataAdapterName);
    }

    @Override
    public void onDataAdapterUnavailable(String dataAdapterName) {
        if (dataAdapterName.equals(this.feedAdapterName)) {
            this.feed = null;
            this.feedAdapterName = null;
            logger.info("Data Adapter unavailable: " + dataAdapterName);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public CompletionStage<SessionRequestHandler> loginAsync(LoginInfo loginInfo) {
        //////////////////////////////////////////////////////////////////////////
        // WARNING: No authentication logic is implemented; any login is accepted.
        // This is suitable for demos only and MUST NOT be used in production.
        //////////////////////////////////////////////////////////////////////////
        return CompletableFuture.completedFuture(new SessionRequestHandler() {
            private String sessionId;
            private Map<String,String> sessionInfo;

            @Override
            public Double getAllowedMaxBandwidth() {
                return null;
            }

            @Override
            public void onStart(SessionListener listener) {
                sessionId = listener.getSessionId();
                // Register the session details on the handler.
                logger.info("New session available " + sessionId);
                sessionInfo = new HashMap<>(loginInfo.getClientContext());
            }

            @Override
            public CompletionStage<SubscriptionHandler> newSubscription(SubscriptionInfo info)
                    throws ItemsException {
                List<String> itemRefs = info.getItemRefs();
                List<SubscribedItemConfiguration> result = new ArrayList<>();
                for (String item : itemRefs) {
                    String val;
                    if (( val = Constants.getVal(item,Constants.USER_SUBSCRIPTION)) != null) {
                        synchronized(ids) {
                            if (sessionInfo == null) {
                                logger.warn("Can't find session " + sessionId);
                                throw new ItemsException("Can't find session");
                            }
                            
                            if (sessionInfo.containsKey(Constants.USER_ID)) {
                                //currently permit only one id per session 
                                String prevVal = sessionInfo.get(Constants.USER_ID);
                                if (!val.equals(prevVal)) {
                                    logger.warn("Session alredy owns an ID: " + val);
                                    throw new ItemsException("Session alredy owns an ID: " + val);
                                }
                                
                            } else {
                                if (ids.contains(val)) {
                                    logger.debug("Id already taken: " + val);
                                    throw new ItemsException("Id already taken, try again");
                                } 
                                sessionInfo.put(Constants.USER_ID, val);
                            }
                            ids.add(val);
                            if (feed == null) {
                                throw new ItemsException("Feed unavailable");
                            }
                            ChatRoom chat = feed.getChatFeed();
                            chat.startUser(val);
                        }
                    }
                    result.add(new SubscribedItemConfiguration(item));
                }
                
                List<SubscribedItemConfiguration> items = result;
                return CompletableFuture.completedFuture(new SubscriptionHandler(info) {
                    @Override
                    public List<SubscribedItemConfiguration> getItems() {
                        return items;
                    }
                });
            }

            @Override
            public CompletionStage<String> userMessageAsync(String message) throws CreditsException {

                // we won't introduce blocking operations, hence we can proceed inline

                //be sure we can communicate with the data adapter
                if (feed == null) {
                    throw new CreditsException("No feed available", 0, "No feed available");
                }
                
                //get the user id of the current user
                String id = null;
                String IP = null;
                synchronized(ids) {
                    if (sessionInfo == null) {
                        logger.warn("Can't find session " + sessionId);
                        throw new CreditsException(-1, "Can't find user id (session missing)");
                    }
                    id = sessionInfo.get(Constants.USER_ID);
                    if (id == null) {
                        logger.warn("Can't find user id");
                        throw new CreditsException(-2, "Can't find user id (value missing)");
                    }
                    
                    IP =  sessionInfo.get("REMOTE_IP");
                }
                
                //nick| <-- changing nick
                //status| <-- changing status message (unused)
                //enter| <-- enter a room
                //leave| <-- leave a room
                //release| <-- throw the dart
                //move| <-- move the dart
                //chat| <-- message the room
                String val;
                if (( val = Constants.getVal(message,Constants.NICK_MESSAGE)) != null) {
                    logger.debug("new nick message from "+id+" received: " + message);
                    
                    ChatRoom chat = feed.getChatFeed();
                    chat.changeUserNick(id, val, IP);
                    
                } else if (( val = Constants.getVal(message,Constants.STATUS_MESSAGE)) != null) {
                    logger.debug("new status message from "+id+" received: " + message);
                    
                    ChatRoom chat = feed.getChatFeed();
                    chat.changeUserStatus(id, val, Constants.VOID_STATUS_ID, IP);
                    
                } else if (( val = Constants.getVal(message,Constants.ENTER_ROOM)) != null) {
                    logger.debug("new enter-room message from "+id+" received: " + message);
                    
                    ChatRoom chat = feed.getChatFeed();
                    chat.enterRoom(id,val);
                    
                } else if (( val = Constants.getVal(message,Constants.EXIT_ROOM)) != null) {
                    logger.debug("new exit-room message from "+id+" received: " + message);
                    
                    ChatRoom chat = feed.getChatFeed();
                    chat.leaveRoom(id,val);
                    
                } else if (( val = Constants.getVal(message,Constants.RELEASE_MESSAGE)) != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("new release message from "+id+" received: " + message);
                    }
                    
                    Universe universe = feed.getUniverse();
                    String[] values = val.split(Constants.SPLIT_CHAR_REG);
                    double[] dobuleValues = getDoubles(values);
                    universe.release(id,values[0],dobuleValues[0],dobuleValues[1],dobuleValues[2]);
                    
                } else if (( val = Constants.getVal(message,Constants.MOVE_MESSAGE)) != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("new move message from "+id+" received: " + message);
                    }
                    
                    Universe universe = feed.getUniverse();
                    String[] values = val.split(Constants.SPLIT_CHAR_REG);
                    double[] dobuleValues = getDoubles(values);
                    universe.move(id,values[0],dobuleValues[0],dobuleValues[1],dobuleValues[2]);
                
                } else if (( val = Constants.getVal(message,Constants.RESET_MESSAGE)) != null) {
                    logger.debug("new reset message from "+id+" received: " + message);
                    
                    Universe universe = feed.getUniverse();
                    universe.resetScore(id,val);

                } else if (( val = Constants.getVal(message,Constants.CHAT_MESSAGE)) != null) {
                    logger.debug("new chat message from "+id+" received: " + message);
                    
                    ChatRoom chat = feed.getChatFeed();
                    String[] vals = val.split(Constants.SPLIT_CHAR_REG);
                    chat.broadcastMessage(id,vals[0],vals[1], IP);
                    
                } else {
                    logger.warn("unexpected message from "+id+" received: " + message);
                    throw new CreditsException(-3, "Unexpected message");
                }

                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void onClose() {
                synchronized(ids) {
                    //we have to remove session information from the session HashMap
                    logger.info("Clearing session " + sessionId);
                    String id = sessionInfo.get(Constants.USER_ID);
                    if (id != null) {
                        ids.remove(id);
                        
                        if (feed == null) {
                            logger.error("Unexpected: feed not available");
                            return;
                        }
                        
                        ChatRoom chat = feed.getChatFeed();
                        chat.leaveAllRooms(id);
                        chat.stopUser(id);
                    }
                }
            }

            private double[] getDoubles(String[] values) throws CreditsException {
                if (values.length != 4) {
                    throw new CreditsException(-4, "wrongly formatted message");
                }
                double[] res = new double[3];
                try {
                    res[0] = Double.parseDouble(values[1]);
                    res[1] = Double.parseDouble(values[2]);
                    res[2] = Double.parseDouble(values[3]);
                } catch(NumberFormatException nf) {
                    throw new CreditsException(-5, "wrongly formatted number");
                }
                return res;
            }
        });
    }

}
