package com.lightstreamer.adapters.Dart.room;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class ChatRoom {

    private ChatRoomListener listener;
    
    private static final boolean ENTER = true;
    private static final boolean EXIT = false;
    
    static final boolean REALTIME = true;
    private static final boolean SNAPSHOT = false;
    
    
    private HashMap<String,User> users = new HashMap<String,User>();
    private HashMap<String,Room> rooms = new HashMap<String,Room>();
    
    private Executor executor =  Executors.newSingleThreadExecutor();
    
    public ChatRoom(ChatRoomListener listener) {
        this.listener = listener;
    }
    
    public User addUser(final String id) {
        synchronized(users) {
            User user = new User(this, id);
            users.put(id, user);
            
            executor.execute(new Runnable() {
                public void run() {
                    listener.onNewUser(id);
                }
            });
            
            return user;
        }
    }
    
    public void changeUserNick(String id, String nick) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                return;
            }
            User user = users.get(id);
            user.setNick(nick);
        }
    }
    
    public void changeUserStatus(String id, String status, String statusId) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                return;
            }
            User user = users.get(id);
            user.setStatus(status,statusId);
        }
    }
    
    public void enterRoom(String id, String roomId) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                return;
            }
            User user = users.get(id);
            Room room = this.getRoomForced(roomId);
            user.enterRoom(room);
            
        }
    }
    
    public void leaveRoom(String id, String roomId) {
        synchronized(users) {
            synchronized(rooms) {
                if (!users.containsKey(id) || !rooms.containsKey(roomId)) {
                    return;
                }
                User user = users.get(id);
                Room room = rooms.get(roomId);
                user.leaveRoom(room);
                
                if (room.isEmpty() && !room.isListened()) {
                    rooms.remove(roomId);
                }
            }
        }
    }
    
    private User getUserForced(String id) {
        synchronized(users) {
            User user;
            if (!users.containsKey(id)) {
                user = this.addUser(id);
            } else {
                user = users.get(id);
            }
            return user;
        }
    }
    

    public void startUserMessageListen(String id, Object handle) {
        synchronized(users) {
            User user = this.getUserForced(id);
            user.setHandle(handle);
        }
    }
    
    public void startUserStatusListen(String id, Object userStatusHandle) {
        synchronized(users) {
            User user = this.getUserForced(id);
            user.setStatusHandle(userStatusHandle);
            
            Map<String,String> extra = new HashMap<String,String>();
            extra.putAll(user.getExtraProps());
            this.sendUserStatusEvent(user, user.getNick(), user.getStatusId(), user.getStatus(), extra, userStatusHandle, SNAPSHOT);
        }
    }
    
    public Object getUserStatusHandle(String id) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                return null;
            } 
            
            User user = users.get(id);
            return user.getStatusHandle();
        }
    }

    
    public void removeUser(final String id) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                return;
            }
            User user = users.remove(id);
            
            synchronized(rooms) {
                Iterator<Room> userRooms = user.getRooms();
                while(userRooms.hasNext()) {
                    Room room = userRooms.next();
                    user.leaveRoom(room);
                }
            }

            executor.execute(new Runnable() {
                public void run() {
                    listener.onUserDeleted(id);
                }
            });
        }
    }
    
    public void stopUserMessageListen(String id) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                return;
            }
            User user = users.get(id);
            user.setHandle(null);
            
            if (!user.isListened()) {
                this.removeUser(id);
            }
        }
    }
    
    public void stopUserStatusListen(String id) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                return;
            }
            User user = users.get(id);
            user.setStatusHandle(null);
            
            if (!user.isListened()) {
                this.removeUser(id);
            }
        }
    }
        
    //synchronized(users) {
    void sendUserStatusEvent(final User user, final String nick, final String statusId, final String status, final Map<String,String> extra, final Object userStatusHandle, final boolean realTimeEvent) {
        executor.execute(new Runnable() {
            public void run() {
                listener.onUserStatusChange(user, nick, statusId, status, extra, userStatusHandle, realTimeEvent);
            }
        });
    }
    
    private Room getRoomForced(String roomId) {
        synchronized(rooms) {
            Room room = null;
            if (!rooms.containsKey(roomId)) {
                room = new Room(roomId);
                rooms.put(roomId, room);
            } else {
                room = rooms.get(roomId);
            }
            return room;
        }
    }
    
    public void startRoomListen(final String roomId, final Object roomStatusHandle) {
        
        synchronized(rooms) {
            Room room = this.getRoomForced(roomId);
            
            room.setStatusHandle(roomStatusHandle);
            
            Iterator<String> roomUsers = room.getUsers();
            while(roomUsers.hasNext()) {
                String id = roomUsers.next();
                User user = this.getUserForced(id);
                this.sendRoomStatusEvent(user,roomId,roomStatusHandle,ENTER,SNAPSHOT);
            }

            executor.execute(new Runnable() {
                public void run() {
                    listener.onRoomListComplete(roomId, roomStatusHandle);
                }
            });
        }
    }
    
    public void stopRoomListen(String roomId) {
        synchronized(rooms) {
            if (!rooms.containsKey(roomId)) {
                return;
            }
            Room room = rooms.get(roomId);
            room.setStatusHandle(null);
            if (room.isEmpty() && !room.isListened()) {
                rooms.remove(roomId);
            }
        }
    }

    //synchronized(rooms) {
    private void sendRoomStatusEvent(final User user, final String roomId, final Object roomStatusHandle, boolean entering, final boolean realTimeEvent) {
        if (entering) {
            executor.execute(new Runnable() {
                public void run() {
                    listener.onUserEnter(user, roomId, roomStatusHandle, realTimeEvent);
                }
            });
        } else {
            executor.execute(new Runnable() {
                public void run() {
                    listener.onUserExit(user, roomId, roomStatusHandle);
                }
            });
        }
    }
    
   
    
    class Room {
        
        private String roomId;
        private Set<String> users = new HashSet<String>();
        private Object statusHandle = null;
        private Object messageHandle = null;
        
        public Room(String roomId) {
            this.roomId = roomId;
        }
       
        public boolean isEmpty() {
            return users.isEmpty();
        }
        
        public boolean isListened() {
            return this.statusHandle != null || this.messageHandle != null;
        }

        public void setStatusHandle(Object roomStatusHandle) {
            this.statusHandle = roomStatusHandle;
        }
        
        public void setMessageHandle(Object messageHandle) {
            this.messageHandle = messageHandle;
        }
        
        void addUser(User user) {
            this.users.add(user.getId());
            if (this.statusHandle != null) {
                sendRoomStatusEvent(user, this.roomId, this.statusHandle, ENTER, REALTIME);
            }
        }

        void removeUser(User user) {
            if (this.users.remove(user.getId()) && this.statusHandle != null ) {
                sendRoomStatusEvent(user, this.roomId, this.statusHandle, EXIT, REALTIME);
            }
        }
        
        public Iterator<String> getUsers() {
            return this.users.iterator();
        }
        

    }

    
}





