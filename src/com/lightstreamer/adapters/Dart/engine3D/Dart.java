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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.croftsoft.core.lang.EnumUnknownException;
import com.croftsoft.core.math.axis.AxisAngle;
import com.lightstreamer.adapters.Dart.room.User;
import com.lightstreamer.interfaces.metadata.CreditsException;

public class Dart implements IBody {

    private String id;
    
    private double  x, y, z;                                // position         Vector3
    private double  vX, vY, vZ;                             // velocity         Vector3
    private double  startX, startY, startZ;
    
    private static final int READY = 1;
    private static final int FLYING = 2;
    private static final int GRABBED = 3;
    private int status = READY;
    
    private World world;
    private User owner;

    private long timestamp;

    public Dart(User owner, String id,World world) {
        this(owner,id,world,0,0,Constants.MAX_SIZE_Z);
    }
    
    public Dart(User owner, Dart orig) {
        this(owner,orig.getId(),orig.getWorld(),
                orig.getX(),orig.getY(),orig.getZ());
    }
    
    public Dart(User owner, String id, World world, double x, double y, double z) {
        this(owner,id,world,x,y,z,0,0,0);
    }
    
    public Dart(User owner, String id, World world, 
            double x, double y, double z, 
            double vX, double vY, double vZ) {
        
        this.id = id;
        this.world = world;
        this.owner = owner;
        
        this.x = x;    
        this.y = y;
        this.z = z;
        
        this.vX = vX;
        this.vY = vY;
        this.vZ = vZ;
        
        this.startX = 0;
        this.startY = 0;
        this.startZ = 0;
        
        this.timestamp = 0;
        
        this.updateOwner(true, true);
        
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
    
    @Override
    public void translate(double factor) {
        //factor not handled
        translate();
    }
    
    @Override
    public void translate() {
        if (this.status != FLYING) {
            //in hand
            return;
        }
        
        long tNow = new Date().getTime() - this.timestamp;

        double x = this.calculateAxisPos(this.startX,this.vX,tNow);
        double y = this.calculateAxisPos(this.startY,this.vY,tNow);
        double z = this.calculateAxisPos(this.startZ,this.vZ,tNow);
        
        double endXt = this.getFinalTimeIfOverflow(x, Constants.MAX_SIZE_X, this.startX, this.vX);
        double endYt = this.getFinalTimeIfOverflow(y, Constants.MAX_SIZE_Y, this.startY, this.vY);
        double endZt = this.getFinalTimeIfOverflow(z, Constants.MAX_SIZE_Z, this.startZ, this.vZ);
        
        int score = -1;
        if (endXt != -1 || endYt != -1 || endZt != -1) {
            boolean scored = false;
            double tEnd = endXt;
            tEnd = tEnd == -1 || endYt != -1 && endYt < tEnd ? endYt : tEnd;
            if (tEnd == -1 || endZt != -1 && endZt < tEnd) {
                tEnd = endZt;
                scored = true;
            }
            tEnd = tEnd == -1 || endZt != -1 && endZt < tEnd ? endZt : tEnd;
            
            x = this.calculateAxisPos(this.startX,this.vX,tEnd);
            y = this.calculateAxisPos(this.startY,this.vY,tEnd);
            z = this.calculateAxisPos(this.startZ,this.vZ,tEnd);
            
            if (scored) {
                score = DartBoard.getScore(x, y);
            } else {
                score = 0;
            }
            
            /*System.out.println("Start "+this.startX+"|"+this.startY+"|"+this.startZ);
            System.out.println("Speed "+this.vX+"|"+this.vY+"|"+this.vZ);
            System.out.println(tEnd);
            System.out.println("End "+x+"|"+y+"|"+z);
            System.out.println("Score "+ score);*/
        }
       
        this.x = x;
        this.y = y;
        this.z = z;
        
        if (score > -1) {
            this.stopDart(score);
        }
    }

    private double calculateAxisPos(double start, double speed, double time) {
        return start + speed*time; //TODO gravity
    }
    
    private double calculateZPosition(double start, double speed, double time) {
        //s = v*t + (1/2)at^2
        double units = Constants.HALF_ACCELERATION*Math.pow(time,2);
        
        return this.calculateAxisPos(this.startZ,this.vZ,time) + units;
    }

    private double getFinalTimeIfOverflow(double val, double max, double start, double speed) {
        //TODO gravity
        if (val > max ) {
            return calculateTimestamp(max,start,speed);//Math.abs((max-start)/speed);
        } else if(val < -max) {
            return calculateTimestamp(-max,start,speed); //return  Math.abs((-max-start)/speed);
        } else {
            return -1;
        }
    }
    
    private double calculateTimestamp(double value, double start, double speed) {
        return Math.abs((value-start)/speed);
    }
    
    private double calculateTimestampZ(double value) {
        double c = -(value-this.startZ);
        double a = Constants.HALF_ACCELERATION;
        double b = this.vZ;
        if (c<0) {
          return (-b + Math.sqrt(Math.pow(b,2)-4*a*c))/(2*a);
        } else if (c>0) {
          return (-b - Math.sqrt(Math.pow(b,2)-4*a*c))/(2*a);
        }
        return 0;
        
    }

    public void stopDart(int score) {
        this.vX = 0;
        this.vY = 0;
        this.vZ = 0;
        
        this.changeStatus(READY);
        
        
        this.updateOwner(true, true);

        this.world.sendPlayerScore(this.id,score);
        
        
    }
    
    @Override
    public void translate(Translation translation, double distance) {
        // TODO Auto-generated method stub

    }
    
    //user inputs -->
    
    private void setImpulse(Axis axis, double speed) {
        switch ( axis )
        {
            case X:
                this.vX = speed;
                
                break;
                
            case Y:
                this.vY = speed;
                
                break;
                
            case Z:
                this.vZ = speed;
                
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
    
    private void fillPositionMap(Map<String,String> model) {
        try {
            model.put("posX", toBase64(this.x));
            model.put("posY", toBase64(this.y));
            model.put("posZ", toBase64(this.z));
            
        } catch (IOException e) {
            //TODO ?
        }
    }
    
    private void fillImpulseMap(Map<String,String> model) {
        model.put("dVx", String.valueOf(this.vX));
        model.put("dVy", String.valueOf(this.vY));
        model.put("dVz", String.valueOf(this.vZ));
    }
    
    public void updateOwner(boolean pos, boolean speed) {
        Map<String,String> update = new HashMap<String,String>();
        if (pos) {
            this.fillPositionMap(update);
        }
        if(speed) {
            this.fillImpulseMap(update);
        }
        this.owner.setExtraProps(update);
    }
    
    // actions -->
    
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
        
        if (x == 0 && y == 0 && z == 0) {
            //fail throw
            this.changeStatus(READY);
            return;
        }
        this.changeStatus(FLYING);
        
        this.timestamp = new Date().getTime();
        
        this.setImpulse(IBody.Axis.X, x);
        this.setImpulse(IBody.Axis.Y, y);
        this.setImpulse(IBody.Axis.Z, z);
        
        this.updateOwner(true, true);
        
        this.startX = this.x;
        this.startY = this.y;
        this.startZ = this.z;
    
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
        
        this.timestamp = 0;
        
        this.updateOwner(false, true);
        
    }
    
    public void forcePosition(double x, double y, double z) throws CreditsException {
        if (this.status != GRABBED) {
            throw new CreditsException(-12, "Can't move around while not grabbed");
        }
        
        if (z < Constants.ARM_REACH) {
            //you need to throw the dart!
            throw new CreditsException(-13, "Are you trying to cheat? Throw your dart!");
        }
        
        this.x = x;
        this.y = y;
        this.z = z;
        
        this.updateOwner(true, false);
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
