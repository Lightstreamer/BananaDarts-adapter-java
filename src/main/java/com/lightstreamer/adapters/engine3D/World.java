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
package com.lightstreamer.adapters.Dart.engine3D;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.adapters.Dart.room.User;
import com.lightstreamer.interfaces.metadata.CreditsException;

public class World extends Thread {
    
    private Logger logger = LogManager.getLogger(com.lightstreamer.adapters.Dart.Constants.WORLD_CAT);
    
    private String id=null;
    
    private ConcurrentHashMap<String,Dart> darts = new ConcurrentHashMap<String,Dart>();

    private int frameInterval = 0;
    
    private boolean stop = false;
    
    
    World(String id, int frameInterval)  {
        this.id = id;
        
        this.frameInterval = frameInterval;
        
        logger.info("A new world is born: " + id + " ("+this.frameInterval+")");
    }
    
    synchronized void setFrameInterval(int frameInterval) {
        logger.info(this.id+"|Frame interval changed: " + frameInterval);
        this.frameInterval = frameInterval;
    }

    synchronized boolean isEmpty() {
        return darts.isEmpty();
    }
    
    synchronized void addUser(User user) {
        String id = user.getId();
        if (this.darts.containsKey(id)) {
            return;
        }
        Dart player = new Dart(user,id,this);
        
        this.darts.put(id,player); 
        logger.info(this.id+"|A new user entered" + id);
    }
    

    synchronized void removeUser(String id) {
        this.darts.remove(id);
        logger.info(this.id+"|A user left" + id);
    }
    
    synchronized void armageddon() {
        this.stop = true;
        logger.info(this.id+"|World ended");
    }
    
    @Override
    public void run () {
        
        while (!stop) {
            
            if (logger.isTraceEnabled()) {
                logger.trace(this.id+"|generating frame");
            }
                        
            Enumeration<Dart> players = this.darts.elements();
            while(players.hasMoreElements()) {
                Dart player = players.nextElement();
                  
                //TODO I do not need to continuously calculate the position: animation is performed client side,
                //adapter only need to know when the animation ends to send the score over
                player.translate();
            }
            
            if (logger.isTraceEnabled()) {
                logger.trace(this.id+"|frame generated");
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
        if (logger.isTraceEnabled()) {
            logger.trace(this.id+"|throwing " + id);
        }
        Dart player = this.darts.get(playerId);
        player.throwDart(x,y,z);
    }

    public synchronized void move(String playerId, double x, double y, double z) throws CreditsException {
        if (!this.darts.containsKey(playerId)) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace(this.id+"|moving " + id);
        }
        Dart player = this.darts.get(playerId);
        player.forcePosition(x, y, z);
    }
    
    public synchronized void resetScore(String playerId) {
        if (!this.darts.containsKey(playerId)) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace(this.id+"|resetting score " + id);
        }
        Dart player = this.darts.get(playerId);
        player.resetScore();
    }
    
    public String getWorldId() {
        return id;
    }
    

}
