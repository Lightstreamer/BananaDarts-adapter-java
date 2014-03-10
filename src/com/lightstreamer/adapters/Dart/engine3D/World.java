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
package com.lightstreamer.adapters.Dart.engine3D;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import com.lightstreamer.adapters.Dart.room.User;
import com.lightstreamer.interfaces.metadata.CreditsException;

public class World extends Thread {
    
    private String id=null;
    
    private ConcurrentHashMap<String,Dart> darts = new ConcurrentHashMap<String,Dart>();
    private Object scoreHandle = null;
    

    private int frameInterval = 0;
    
    private boolean stop = false;
    
    
    World(String id, int frameInterval)  {
        this.id = id;
        
        this.frameInterval = frameInterval;
    }
    
    synchronized void setFrameInterval(int frameInterval) {
        this.frameInterval = frameInterval;
    }

    synchronized boolean isListened() {
        return this.scoreHandle != null;
    }
    
    synchronized boolean isEmpty() {
        return darts.isEmpty();
    }
    
    synchronized void setScoreHandle(Object handle) {
        this.scoreHandle = handle;
        //TODO snapshot?
    }
    
    synchronized void addUser(User user) {
        String id = user.getId();
        if (this.darts.containsKey(id)) {
            return;
        }
        Dart player = new Dart(user,id,this);
        
        this.darts.put(id,player); 
    }
    

    synchronized void removeUser(String id) {
        this.darts.remove(id);
        //this.sendPlayerStatus(id, this.id, this.handle, null, EXIT, REALTIME);
    }
    
    synchronized void armageddon() {
        this.stop = true;
    }
    
    @Override
    public void run () {
        
        while (!stop) {
                        
            Enumeration<Dart> players = this.darts.elements();
            while(players.hasMoreElements()) {
                Dart player = players.nextElement();
                  
                //TODO I do not need to continuously calculate the position: animation is performed client side,
                //adapter only need to know when the animation ends to send the score over
                player.translate();
            }
            
            try {
                Thread.sleep(this.frameInterval);
            } catch (InterruptedException ie) {
            }
        }
    }
      
    public synchronized void release(String playerId, double x, double y, double z) throws CreditsException {
        if (!this.darts.containsKey(playerId)) {
            return;
        }
        Dart player = this.darts.get(playerId);
        player.throwDart(x,y,z);
    }

    public synchronized void move(String playerId, double x, double y, double z) throws CreditsException {
        if (!this.darts.containsKey(playerId)) {
            return;
        }
        Dart player = this.darts.get(playerId);
        player.forcePosition(x, y, z);
    }
    
    public String getWorldId() {
        return id;
    }
    

}
