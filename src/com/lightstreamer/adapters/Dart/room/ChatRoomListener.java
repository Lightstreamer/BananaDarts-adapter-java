package com.lightstreamer.adapters.Dart.room;

import java.util.Map;

public interface ChatRoomListener {

    public void onUserEnter(User user, String room, Object roomStatusHandle, boolean realTimeEvent);
    public void onRoomListComplete(String id, Object roomStatusHandle);
    public void onUserExit(User user, String room, Object roomStatusHandle);
    
    public void onUserStatusChange(User user, String nick, String statusId, String status, Map<String,String> extra, Object userStatusHandle, boolean realTimeEvent);
    
    public void onUserMessage(String id, String message, String room, Object roomHandle, boolean realTimeEvent); //not implemented
    public void onPrivateMessage(String fromId, String toId, String message, Object userHandle); //not implemented  
    
    public void onNewUser(String id);
    public void onUserDeleted(String id);
    
}
