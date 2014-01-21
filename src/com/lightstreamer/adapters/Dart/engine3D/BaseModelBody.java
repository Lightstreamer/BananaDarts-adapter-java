/*
 * 
 *  Copyright 2013 Weswit s.r.l.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * 
 */

package com.lightstreamer.adapters.Dart.engine3D;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.croftsoft.core.lang.EnumUnknownException;
import com.croftsoft.core.math.axis.AxisAngle;
import com.lightstreamer.adapters.Dart.room.User;
import com.lightstreamer.interfaces.metadata.CreditsException;

public class BaseModelBody implements IBody {

    private static final double TRANSLATE_DELTA = 0.002;
    private static final double MAX_SIZE_X = 80;
    private static final double MAX_SIZE_Y = 45;
    private static final double MAX_SIZE_Z = 80;
    
    private String id;
    
    private double  x, y, z;                                // position         Vector3
    private double  vX, vY, vZ;                             // velocity         Vector3
    
    
    private static final int READY = 1;
    private static final int FLYING = 2;
    private static final int GRABBED = 3;
    private int status = READY;
    
    private World world;
    private User owner;

    public BaseModelBody(User owner, String id,World world) {
        this(owner,id,world,0,0,59);
    }
    
    public BaseModelBody(User owner, BaseModelBody orig) {
        this(owner,orig.getId(),orig.getWorld(),
                orig.getX(),orig.getY(),orig.getZ());
    }
    
    public BaseModelBody(User owner, String id, World world, double x, double y, double z) {
        this(owner,id,world,x,y,z,0,0,0);
    }
    
    public BaseModelBody(User owner, String id, World world, 
            double x, double y, double z, 
            double vX, double vY, double vZ) {
        
        this.id = id;
        this.world = world;
        this.owner = owner;
        
        Map<String,String> update = new HashMap<String,String>();
        this.fillPositionMap(update);
        this.fillImpulseMap(update);
        this.owner.setExtraProps(update);
        
        this.x = x;    
        this.y = y;
        this.z = z;
        
        this.vX = vX;
        this.vY = vY;
        this.vZ = vZ;
     
        
    }
    
    // --> simple getters/setters
    
    public String getId() {
        return this.id;
    }
    
    private World getWorld() {
        return this.world;
    }
   
    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    public double getvX() {
        return vX;
    }

    public double getvY() {
        return vY;
    }

    public double getvZ() {
        return vZ;
    }

    // --> model transformations
   
    
    @Override
    public void translate(Axis axis, double distance) {
        switch ( axis )
        {
            case X:
                this.x += distance;
              
                break;
              
            case Y:
                this.y += distance;
                
                break;
                
            case Z:
                this.z += distance;
                
                break;
            
            default:
                // Skip.
        }
    }
    
    public static double stopAtMax(double v, double max) {
        if (v >= max) {
            return max;
        } else if (v <= -max) {
            return -max;
        }
        return v; 
    }
    
    @Override
    public void translate() {
        translate(1);
    }
    
    @Override
    public void translate(double factor) {
        if (this.status != FLYING) {
            //in hand
            return;
        }
        
        this.z += (double)(this.vZ * TRANSLATE_DELTA * factor);
        this.z = stopAtMax(this.z,MAX_SIZE_Z);
        if (this.z != -MAX_SIZE_Z) {
            
            this.y += (double)(this.vY * TRANSLATE_DELTA * factor); 
            this.x += (double)(this.vX * TRANSLATE_DELTA * factor); 
            
            this.y = stopAtMax(this.y,MAX_SIZE_Y);
            this.x = stopAtMax(this.x,MAX_SIZE_X);
        
        } else {
            int score = DartBoard.getScore(this.x, this.y);
            this.vX = 0;
            this.vY = 0;
            this.vZ = 0;
            
            this.changeStatus(READY);
            
            Map<String,String> update = new HashMap<String,String>();
            this.fillImpulseMap(update);
            this.owner.setExtraProps(update);
            
            this.world.sendPlayerScore(this.id,score);
        }
        
    }
    
    @Override
    public void translate(Translation translation, double distance) {
        // TODO Auto-generated method stub

    }
    
    //user inputs -->
    
    public void setImpulse(Axis axis, double intensity) {
        switch ( axis )
        {
            case X:
                this.vX += intensity;
                
                break;
                
            case Y:
                this.vY += intensity;
                
                break;
                
            case Z:
                this.vZ += intensity;
                
                break;
                
            default:
                throw new EnumUnknownException ( axis );
        }
    }
    
    
    // data extraction -->
    
    private static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }
    
    private static String toBase64(double value) throws IOException {
        String s = (new Base64Manager()).encodeBytes(toByteArray((float)value),true);
        return s.substring(0, s.indexOf("="));
    }
    
    public void fillPositionMap(Map<String,String> model) {
        try {
            model.put("posX", toBase64(this.x));
            model.put("posY", toBase64(this.y));
            model.put("posZ", toBase64(this.z));
            
        } catch (IOException e) {
            //TODO ?
        }
    }
    
    public void fillImpulseMap(Map<String,String> model) {
        model.put("dVx", String.valueOf(this.vX));
        model.put("dVy", String.valueOf(this.vY));
        model.put("dVz", String.valueOf(this.vZ));
        
        model.put("locked", String.valueOf(this.status == FLYING));
    }
    
    private void changeStatus(int newStatus) {
        if (this.status == newStatus) {
            return;
        }
        
        this.status = newStatus;
    }

    public void throwDart(double x, double y, double z) throws CreditsException {
        if (this.status != GRABBED) {
            throw new CreditsException(-10, "Can't throw if not grabbed");
        }
        
        if (z >= 0) {
            //fail throw
            this.changeStatus(READY);
            return;
        }
        this.changeStatus(FLYING);
     
        this.setImpulse(IBody.Axis.X, x);
        this.setImpulse(IBody.Axis.Y, y);
        this.setImpulse(IBody.Axis.Z, z);
        
        Map<String,String> update = new HashMap<String,String>();
        this.fillImpulseMap(update);
        this.owner.setExtraProps(update);
        
    }
    
    public void block() throws CreditsException {
        if (this.status != READY) {
            //can't grab while flying or already flying
            throw new CreditsException(-11, "Can't grab while flying");
        }
        this.changeStatus(GRABBED);
        
        this.vX = 0;
        this.vY = 0;
        this.vZ = 0;
        
        Map<String,String> update = new HashMap<String,String>();
        this.fillImpulseMap(update);
        this.owner.setExtraProps(update);
        
    }
    
    public void forcePosition(double x, double y, double z) throws CreditsException {
        if (this.status != GRABBED) {
            throw new CreditsException(-12, "Can't move around while not grabbed");
        }
        
        if (z < 0) {
            //you need to throw the dart!
            throw new CreditsException(-13, "Are you trying to cheat? Throw your dart!");
        }
        
        this.x = x;
        this.y = y;
        this.z = z;
        
        Map<String,String> update = new HashMap<String,String>();
        this.fillPositionMap(update);
        this.owner.setExtraProps(update);
        
    }
    
    
    
    
    //rotation is not handled -->

    @Override
    public AxisAngle getAxisAngle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAxisAngle(AxisAngle axisAngle) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rotate(AxisAngle axisAngle) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rotate(Axis axis, double degrees) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rotate(Rotation rotation, double degrees) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rotate() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rotate(double factor) {
        // TODO Auto-generated method stub
        
    }
    
  

}
