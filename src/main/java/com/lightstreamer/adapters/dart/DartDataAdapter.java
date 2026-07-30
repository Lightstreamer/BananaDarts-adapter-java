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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.adapters.dart.engine3D.Universe;
import com.lightstreamer.adapters.dart.room.ChatRoom;
import com.lightstreamer.adapters.dart.room.ChatRoomListener;
import com.lightstreamer.adapters.dart.room.User;
import com.lightstreamer.adapter.common.InitException;
import com.lightstreamer.adapter.common.Mode;
import com.lightstreamer.adapter.data.DataAdapter;
import com.lightstreamer.adapter.data.ItemListener;
import com.lightstreamer.adapter.data.ItemManager;

public class DartDataAdapter implements DataAdapter, ChatRoomListener {
   
    public Logger logger;
    
    private final Universe universe = new Universe();
    private final ChatRoom chat = new ChatRoom(this);
    
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
        
        logger.info("Adapter Logger start.");
        
        logger.info("DartDataAdapter ready");
        
    }
    
    @Override
    public CompletionStage<ItemManager> getItemManager(String itemName) {
        return CompletableFuture.completedFuture(new DartItemManager(itemName));
    }

    @Override
    public void shutdown() {
    }

    ChatRoom getChatFeed() {
        return this.chat;
    }
    
    Universe getUniverse() {
        return this.universe;
    }

    private class DartItemManager implements ItemManager {
        private final String itemName;

        DartItemManager(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public Mode getSupportedMode() {
            if (itemName.indexOf(Constants.USER_SUBSCRIPTION) == 0) {
                return Mode.DISTINCT;
            } else if (itemName.indexOf(Constants.ROOMCHATLIST_SUBSCRIPTION) == 0) {
                return Mode.COMMAND;
            } else if (itemName.indexOf(Constants.ROOMCHAT_SUBSCRIPTION) == 0) {
                return Mode.DISTINCT;
            }
            return Mode.MERGE;
        }

        @Override
        public Integer getDistinctSnapshotLength() {
            if (getSupportedMode() == Mode.DISTINCT) {
                return 30;
            }
            return null;
        }

        @Override
        public boolean isSnapshotAvailable() {
            if (itemName.indexOf(Constants.USER_SUBSCRIPTION) == 0) { 
                return false; //currently does not generate any event at all (and never will)
            } else if (itemName.indexOf(Constants.ROOMCHATLIST_SUBSCRIPTION) == 0) {
                return true;
            } else if (itemName.indexOf(Constants.ROOMCHAT_SUBSCRIPTION) == 0) {
                return false; 
            } else {
                return true;
            }
        }

        @Override
        public CompletionStage<Void> subscribe(ItemListener listener) {
            synchronized (DartDataAdapter.this) {
                String val;
                if (( val = Constants.getVal(itemName,Constants.USER_SUBSCRIPTION)) != null) {
                    //DISTINCT used only to signal presence
                    logger.debug("User subscription: " + itemName);
                    
                    //user is created on subscription and destroyed on unsubscription
                    chat.startUserMessageListen(val,listener);
                    
                } else if (( val = Constants.getVal(itemName,Constants.ROOMCHATLIST_SUBSCRIPTION)) != null) {
                    //COMMAND contains users of a certain room
                    logger.debug("Room list subscription: " + val);
                    
                    chat.startRoomListen(val,listener);// will add the room if non-existent (room may exist if a user entered it even if no one is listening to it)
                
                } else if (( val = Constants.getVal(itemName,Constants.ROOMCHAT_SUBSCRIPTION)) != null) {
                    //DISTINCT chat for the room
                    logger.debug("Room chat subscription: " + val);
                    
                    chat.startRoomChatListen(val,listener);
                    
                } else {
                    //MERGE subscription for user status and nick + their commands and positions 
                    logger.debug("User status subscription: " + itemName);
                    
                    chat.startUserStatusListen(itemName,listener);
                }
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<Void> unsubscribe() {
            synchronized (DartDataAdapter.this) {
                String val;
                if (( val = Constants.getVal(itemName,Constants.USER_SUBSCRIPTION)) != null) {
                    logger.debug("User unsubscription: " + itemName);
                    
                    chat.stopUserMessageListen(val);
                    
                } else if (( val = Constants.getVal(itemName,Constants.ROOMCHATLIST_SUBSCRIPTION)) != null) {
                    logger.debug("Room list unsubscription: " + val);
                    
                    chat.stopRoomListen(val);
                    
                } else if (( val = Constants.getVal(itemName,Constants.ROOMCHAT_SUBSCRIPTION)) != null) {
                    logger.debug("Room chat unsubscription: " + val);
                    
                    chat.stopRoomChatListen(val);
                    
                } else {
                    logger.debug("User status unsubscription: " + itemName);
                    
                    chat.stopUserStatusListen(itemName);
                }
            }
            return CompletableFuture.completedFuture(null);
        }
    }
    
    //user related events sequentiality is ensured by the chat class; are only generated if there is the associated handle  

    @Override
    public void onUserEnter(User user, String room, ItemListener roomStatusHandle, boolean realTimeEvent) {
        String id = user.getId();
        logger.debug(id + " enters " + room);
        
        HashMap<String, String> update = new HashMap<String, String>();
        update.put(ItemListener.KEY_FIELD, id);
        update.put(ItemListener.COMMAND_FIELD, ItemListener.ADD_COMMAND);
              
        if (!realTimeEvent) {
            roomStatusHandle.snapshot(update, null);
        } else {
            roomStatusHandle.update(update);
        }
        
        universe.addPlayerToWorld(user,room);
        
    }
    
    @Override
    public void onRoomListComplete(String id, ItemListener roomStatusHandle) {
        logger.debug(id + " filled");
        
        roomStatusHandle.endOfSnapshot();
    }

    @Override
    public void onUserExit(User user, String room, ItemListener roomStatusHandle) {
        String id = user.getId();
        logger.debug(id + " exits " + room);
        
        HashMap<String, String> update = new HashMap<String, String>();
        update.put(ItemListener.KEY_FIELD, id);
        update.put(ItemListener.COMMAND_FIELD, ItemListener.DELETE_COMMAND);
        
        roomStatusHandle.update(update);
        
        universe.removePlayerFromWorld(user,room);
    }

    
    @Override
    public void onUserStatusChange(User user, String nick, String statusId, String status, Map<String,String> extra, ItemListener userStatusHandle, boolean realTimeEvent) {
        String id = user.getId();
        if (logger.isTraceEnabled()) { //just use trace as it contains the movements
            logger.trace(id + " has new info");
        }
        
        if (extra == null) {
            extra =  new HashMap<String, String>();
        }
       
        if (nick != null) {
            extra.put("nick", nick);
        }
        if (status != null) {
            extra.put("status", status);
            extra.put("statusId", statusId);
        }
        
        if (!realTimeEvent) {
            userStatusHandle.snapshot(extra, null);
        } else {
            userStatusHandle.update(extra);
        }
        
    }

    @Override
    public void onUserMessage(String user, String message, String room, ItemListener roomHandle, boolean realTimeEvent) {
        logger.debug(user + " sent a message to room " + room);
        
        Map<String,String> update = new HashMap<String,String>();
        update.put("nick", user);
        update.put("message", message);
        
        
        if (!realTimeEvent) {
            roomHandle.snapshot(update, null);
        } else {
            roomHandle.update(update);
        }
    }

    @Override
    public void onPrivateMessage(String fromId, String toId, String message, ItemListener userHandle) { //always realtime (can't send messages to disconnected users)
      //not used - not implemented
        logger.debug(toId + " got a private message from " + fromId);
    }
    
    @Override
    public void onNewUser(String id) {
        //do nothing
        logger.debug(id + " is ready");
    }

    @Override
    public void onUserDeleted(String id) {
        //do nothing
        logger.debug(id + " is gone");
    }

}
