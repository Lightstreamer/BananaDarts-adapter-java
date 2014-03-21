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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lightstreamer.adapters.Dart.room.User;
import com.lightstreamer.interfaces.metadata.CreditsException;

public class Universe {
    
    private static Logger logger = Logger.getLogger(com.lightstreamer.adapters.Dart.Constants.WORLD_CAT);
    
    private Map<String,World> worlds = new HashMap<String,World>();
    
    public Universe() {
    }

    private synchronized World getWorldForced(String id) {
        if (worlds.containsKey(id)) {
            return worlds.get(id);
            
        } else {
            World world = new World(id,Constants.FRAME_INTERVAL);
            worlds.put(id, world);
            world.start();
            return world;
        }
    }

    public synchronized void removePlayerFromWorld(User user, String room) {
        if (!worlds.containsKey(room)) {
            return;
        }
        logger.info("User exiting world " + user.getId() + ": " + room);
        
        String id = user.getId();
        World world = worlds.get(room);
        world.removeUser(id); 
        
        this.verifyWorld(world.getWorldId(),world);
    }

    public synchronized void addPlayerToWorld(User user, String room) {
        logger.info("User entering world " + user.getId() + ": " + room);
        
        World world = this.getWorldForced(room);
        world.addUser(user);
    }
          
    private synchronized void verifyWorld(String id, World world) {
        if (world.isEmpty()) {
            logger.info("World is now useless: " + id);
            world.armageddon();
            worlds.remove(id);
        }
    }

    public void release(String playerId, String worldId, double x, double y, double z) throws CreditsException {
        if (!worlds.containsKey(worldId)) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Forwarding throw command for player" + playerId + " to world " + worldId);
        }
        World world = worlds.get(worldId);
        world.release(playerId,x,y,z);
    }

    public void move(String playerId, String worldId, double x, double y, double z) throws CreditsException {
        if (!worlds.containsKey(worldId)) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Forwarding move command for player" + playerId + " to world " + worldId);
        }
        World world = worlds.get(worldId);
        world.move(playerId,x,y,z);
    }
    
    public void resetScore(String playerId, String worldId) {
        if (!worlds.containsKey(worldId)) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Forwarding reset command for player" + playerId + " to world " + worldId);
        }
        World world = worlds.get(worldId);
        world.resetScore(playerId);
    }

}
