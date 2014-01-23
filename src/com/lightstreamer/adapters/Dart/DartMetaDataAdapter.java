/*
Copyright 2014 Weswit s.r.l.

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
package com.lightstreamer.adapters.Dart;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.lightstreamer.adapters.Dart.engine3D.Universe;
import com.lightstreamer.adapters.Dart.room.ChatRoom;
import com.lightstreamer.adapters.metadata.LiteralBasedProvider;
import com.lightstreamer.interfaces.metadata.CreditsException;
import com.lightstreamer.interfaces.metadata.ItemsException;
import com.lightstreamer.interfaces.metadata.MetadataProviderException;
import com.lightstreamer.interfaces.metadata.Mode;
import com.lightstreamer.interfaces.metadata.NotificationException;

public class DartMetaDataAdapter extends LiteralBasedProvider {

    /**
     * Unique identification of the Adapter Set. It is used to uniquely
     * identify the related Data Adapter instance;
     * see feedMap on LeapMotionDataAdapter.
     */
    private String adapterSetId;
    /**
     * Keeps the client context informations supplied by Lightstreamer on the
     * new session notifications.
     * Session information is needed to pass the IP to logging purpose.
     */
    private ConcurrentHashMap<String,Map<String,String>> sessions = new ConcurrentHashMap<String,Map<String,String>>();
    private ConcurrentHashMap<String,String> ids = new ConcurrentHashMap<String,String>();
    
    private DartDataAdapter feed;
    
    private int nextId = 0;
    
    private static final String LOGGER_CAT = "LS_demos_Logger.LeapDemo";
    public static Logger logger;

    @Override
    public void init(Map params, File configDir) throws MetadataProviderException {
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
        
        logger = Logger.getLogger(LOGGER_CAT);
        
        // Read the Adapter Set name, which is supplied by the Server as a parameter
        this.adapterSetId = (String) params.get("adapters_conf.id");
    }
    
    @Override
    public String[] getItems(String user, String session, String group)
            throws ItemsException {
        String[] split = super.getItems(user,session,group);
        for (int i = 0; i<split.length; i++) {
            String val;
            if (( val = Constants.getVal(split[i],Constants.USER_SUBSCRIPTION)) != null) {
                synchronized(sessions) {
                    Map<String,String> sessionInfo = sessions.get(session);
                    if (sessionInfo == null) {
                        throw new ItemsException("Can't find session");
                    }
                    
                    if (sessionInfo.containsKey(Constants.USER_ID)) {
                        //currently permit only one id per session 
                        String prevVal = sessionInfo.get(Constants.USER_ID);
                        if (!val.equals(prevVal)) {
                            throw new ItemsException("Session alredy owns an ID: " + val);
                        }
                        
                    } else {
                        if (ids.containsKey(val)) {
                            throw new ItemsException("Id already taken, try again");
                        } 
                        sessionInfo.put(Constants.USER_ID, val);
                    }
                    ids.put(val, session);
                    try {
                        this.loadFeed();
                    } catch (CreditsException e) {
                        //TODO what now?
                    }
                    ChatRoom chat = this.feed.getChatFeed();
                    chat.addUser(val); 
                    split[i] = Constants.USER_SUBSCRIPTION + val;
                }
            }
        }
        
        return split;
        
    }
    
    @Override
    public boolean modeMayBeAllowed(String item, Mode mode) {
        if (item.indexOf(Constants.USER_SUBSCRIPTION) == 0 && mode == Mode.DISTINCT) { 
            return true;
            
        } else if (item.indexOf(Constants.ROOMPOSITION_SUBSCRIPTION) == 0 && mode == Mode.COMMAND) {
            return true;
            
        } else if (item.indexOf(Constants.ROOMCHATLIST_SUBSCRIPTION) == 0  && mode == Mode.COMMAND) {
            return true;
            
        }
        
        return mode == Mode.MERGE;
    }
    
    @Override
    public void notifyUserMessage(String user, String session, String message)
            throws CreditsException, NotificationException {
        
        //be sure we can communicate with the data adapter
        this.loadFeed();
        
        //get the user id of the current user
        String id = null;
        synchronized(sessions) {
            Map<String,String> sessionInfo = sessions.get(session);
            if (sessionInfo == null) {
                throw new CreditsException(-1, "Can't find user id (session missing)");
            }
            id = sessionInfo.get(Constants.USER_ID);
            if (id == null) {
                throw new CreditsException(-2, "Can't find user id (value missing)");
            }
        }
        
        //nick| <-- changing nick
        //status| <-- changing status message
        //enter| <-- enter a room
        //leave| <-- leave a room
        //grab| <-- grab the cube
        //release| <-- release the cube
        //move| <-- move the cube
        String val;
        if (( val = Constants.getVal(message,Constants.NICK_MESSAGE)) != null) {
            ChatRoom chat = this.feed.getChatFeed();
            chat.changeUserNick(id, val);
            
        } else if (( val = Constants.getVal(message,Constants.STATUS_MESSAGE)) != null) {
            ChatRoom chat = this.feed.getChatFeed();
            chat.changeUserStatus(id, val, Constants.VOID_STATUS_ID);
            
        } else if (( val = Constants.getVal(message,Constants.ENTER_ROOM)) != null) {
            ChatRoom chat = this.feed.getChatFeed();
            chat.enterRoom(id,val);
            
        } else if (( val = Constants.getVal(message,Constants.EXIT_ROOM)) != null) {
            ChatRoom chat = this.feed.getChatFeed();
            chat.leaveRoom(id,val);
            
        } else if (( val = Constants.getVal(message,Constants.GRAB_MESSAGE)) != null) {
            Universe universe = this.feed.getUniverse();
            universe.block(id,val);
            
        } else if (( val = Constants.getVal(message,Constants.RELEASE_MESSAGE)) != null) {
            Universe universe = this.feed.getUniverse();
            String[] values = val.split(Constants.SPLIT_CHAR_REG);
            double[] dobuleValues = getDoubles(values);
            universe.release(id,values[0],dobuleValues[0],dobuleValues[1],dobuleValues[2]);
            
        } else if (( val = Constants.getVal(message,Constants.MOVE_MESSAGE)) != null) {
            Universe universe = this.feed.getUniverse();
            String[] values = val.split(Constants.SPLIT_CHAR_REG);
            double[] dobuleValues = getDoubles(values);
            universe.move(id,values[0],dobuleValues[0],dobuleValues[1],dobuleValues[2]);
        }
    }
    
    private static double[] getDoubles(String[] values) {
        if (values.length != 4) {
            //TODO throw
            return null;
        }
        double[] res = new double[3];
        try {
            res[0] = Double.parseDouble(values[1]);
            res[1] = Double.parseDouble(values[2]);
            res[2] = Double.parseDouble(values[3]);
        } catch(NumberFormatException nf) {
            //TODO throw
            return null;
        }
        return res;
    }

    
    private void loadFeed() throws CreditsException {
        if (this.feed == null) {
             try {
                 // Get the LeapMotionDataAdapter instance to bind it with this
                 // Metadata Adapter and send chat messages through it
                 this.feed = DartDataAdapter.feedMap.get(this.adapterSetId);
             } catch(Throwable t) {
                 // It can happen if the Chat Data Adapter jar was not even
                 // included in the Adapter Set lib directory (the LeapMotion
                 // Data Adapter could not be included in the Adapter Set as well)
                 logger.error("LeapMotionDataAdapter class was not loaded: " + t);
                 throw new CreditsException(0, "No feed available", "No feed available");
             }

             if (this.feed == null) {
                 // The feed is not yet available on the static map, maybe the
                 // LeapMotion Data Adapter was not included in the Adapter Set
                 logger.error("LeapMotionDataAdapter not found");
                 throw new CreditsException(0, "No feed available", "No feed available");
             }
        }
    }
    
    
   
    @Override
    public void notifyNewSession(String user, String session, Map sessionInfo) throws CreditsException, NotificationException {
        // Register the session details on the sessions HashMap.
        sessions.put(session, sessionInfo);
    }
    
    @Override
    public void notifySessionClose(String session) throws NotificationException {
        synchronized(sessions) {
            //we have to remove session informations from the session HashMap
            Map<String,String> sessionInfo = sessions.remove(session);
            String id = sessionInfo.get(Constants.USER_ID);
            if (id != null) {
                try {
                    this.loadFeed();
                } catch (CreditsException e) {
                    //TODO what now?
                }
                ids.remove(id);
                this.feed.getChatFeed().removeUser(id);
            }
        }
    }

}