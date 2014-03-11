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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.lightstreamer.adapters.Dart.engine3D.Universe;
import com.lightstreamer.adapters.Dart.room.ChatRoom;
import com.lightstreamer.adapters.Dart.room.ChatRoomListener;
import com.lightstreamer.adapters.Dart.room.User;
import com.lightstreamer.interfaces.data.DataProviderException;
import com.lightstreamer.interfaces.data.FailureException;
import com.lightstreamer.interfaces.data.ItemEventListener;
import com.lightstreamer.interfaces.data.SmartDataProvider;
import com.lightstreamer.interfaces.data.SubscriptionException;

public class DartDataAdapter implements SmartDataProvider, ChatRoomListener {

    public static final ConcurrentHashMap<String, DartDataAdapter> feedMap =
            new ConcurrentHashMap<String, DartDataAdapter>();
   
    public Logger logger;
    //public Logger tracer;
    
    private Universe universe = new Universe();
    private ChatRoom chat = new ChatRoom(this);
    private ItemEventListener listener;
    
    @Override
    public void init(Map params, File configDir) throws DataProviderException {
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
        
        logger = Logger.getLogger(Constants.LOGGER_CAT);
        //tracer = Logger.getLogger(TRACER_CAT);
        
        logger.info("Adapter Logger start.");
        //tracer.info("Trace Logger start.");
        
        // Read the Adapter Set name, which is supplied by the Server as a parameter
        String adapterSetId = (String) params.get("adapters_conf.id");
        
        // Put a reference to this instance on a static map
        // to be read by the Metadata Adapter
        feedMap.put(adapterSetId, this);
        
        
        logger.info("LeapMotionAdapter ready");
        
    }
    
    @Override
    public void setListener(ItemEventListener listener) {
        this.listener = listener;
    }

    ChatRoom getChatFeed() {
        return this.chat;
    }
    
    Universe getUniverse() {
        return this.universe;
    }

    @Override
    public boolean isSnapshotAvailable(String item)
            throws SubscriptionException {

        if (item.indexOf(Constants.USER_SUBSCRIPTION) == 0) { 
            return false; //currently does not generate any event at all (and never will)
        } else if (item.indexOf(Constants.ROOMCHATLIST_SUBSCRIPTION) == 0) {
            return true;
        } else if (item.indexOf(Constants.ROOMCHAT_SUBSCRIPTION) == 0) {
            return false; 
        } else {
            return true;
        }
    }

    
    @Override
    public synchronized void subscribe(String item, Object handle, boolean needsIterator)
            throws SubscriptionException, FailureException {

        String val;
        if (( val = Constants.getVal(item,Constants.USER_SUBSCRIPTION)) != null) {
            //DISTINCT used only to signal presence
            logger.debug("User subscription: " + item);
            
            //user is created on subscription and destroyed on unsubscription
            chat.startUserMessageListen(val,handle);
            
        } else if (( val = Constants.getVal(item,Constants.ROOMCHATLIST_SUBSCRIPTION)) != null) {
            //COMMAND contains users of a certain room
            logger.debug("Room list subscription: " + val);
            
            chat.startRoomListen(val,handle);// will add the room if non-existent (room may exist if a user entered it even if no one is listening to it)
        
        } else if (( val = Constants.getVal(item,Constants.ROOMCHAT_SUBSCRIPTION)) != null) {
            //DISTINCT chat for the room
            logger.debug("Room chat subscription: " + val);
            
            chat.startRoomChatListen(val,handle);
            
        } else {
            //MERGE subscription for user status and nick + their commands and forced positions 
            logger.debug("User status subscription: " + item);
            chat.startUserStatusListen(item,handle);
            
            
            //universe.flushUserStatus(item);
        }
    }

    @Override
    public synchronized void unsubscribe(String item) throws SubscriptionException,
            FailureException {
        
        String val;
        if (( val = Constants.getVal(item,Constants.USER_SUBSCRIPTION)) != null) {
            logger.debug("User unsubscription: " + item);
            chat.stopUserMessageListen(val);
            
        } else if (( val = Constants.getVal(item,Constants.ROOMCHATLIST_SUBSCRIPTION)) != null) {
            logger.debug("Room list unsubscription: " + val);
            chat.stopRoomListen(val);
            
        } else if (( val = Constants.getVal(item,Constants.ROOMCHAT_SUBSCRIPTION)) != null) {
            //DISTINCT chat for the room
            logger.debug("Room chat unsubscription: " + val);
            
            chat.stopRoomChatListen(val);
            
        } else {
            logger.debug("User status unsubscription: " + item);
            chat.stopUserStatusListen(item);
        }
    }
    
    //user related events sequentiality is ensured by the chat class; are only generated if there is the associated handle  

    @Override
    public void onUserEnter(User user, String room, Object roomStatusHandle, boolean realTimeEvent) {
        String id = user.getId();
        logger.debug(id + " enters " + room);
        
        HashMap<String, String> update = new HashMap<String, String>();
        update.put(SmartDataProvider.KEY_FIELD, id);
        update.put(SmartDataProvider.COMMAND_FIELD, SmartDataProvider.ADD_COMMAND);
              
        this.listener.smartUpdate(roomStatusHandle, update, !realTimeEvent);
        
        universe.addPlayerToWorld(user,room);
        
    }
    
    @Override
    public void onRoomListComplete(String id, Object roomStatusHandle) {
        logger.debug(id + " filled");
        
        this.listener.smartEndOfSnapshot(roomStatusHandle);
    }

    @Override
    public void onUserExit(User user, String room, Object roomStatusHandle) {
        String id = user.getId();
        logger.debug(id + " exits " + room);
        
        HashMap<String, String> update = new HashMap<String, String>();
        update.put(SmartDataProvider.KEY_FIELD, id);
        update.put(SmartDataProvider.COMMAND_FIELD, SmartDataProvider.DELETE_COMMAND);
        
        this.listener.smartUpdate(roomStatusHandle, update, false);
        
        universe.removePlayerFromWorld(user,room);
    }

    
    @Override
    public void onUserStatusChange(User user, String nick, String statusId, String status, Map<String,String> extra, Object userStatusHandle, boolean realTimeEvent) {
        String id = user.getId();
        logger.debug(id + " has new status/nick");
        
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
        
        this.listener.smartUpdate(userStatusHandle, extra, !realTimeEvent);
        
    }

    @Override
    public void onUserMessage(String user, String message, String room, Object roomHandle, boolean realTimeEvent) {
        logger.debug(user + " sent a message to " + room);
        
        Map<String,String> update = new HashMap<String,String>();
        update.put("nick", user);
        update.put("message", message);
        
        
        this.listener.smartUpdate(roomHandle, update, !realTimeEvent);
    }

    @Override
    public void onPrivateMessage(String fromId, String toId, String message, Object userHandle) { //always realtime (can't send messages to disconnected users)
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
    
    @Override
    public void subscribe(String arg0, boolean arg1)
            throws SubscriptionException, FailureException {
        // unused
        logger.error("Unexpected call");
        throw new SubscriptionException("Unexpected call");
    }

}
