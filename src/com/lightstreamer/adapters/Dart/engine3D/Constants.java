package com.lightstreamer.adapters.Dart.engine3D;

public class Constants {

    //Real-world 
    //Dartboard diameter 18 inches    ---> 45.72 cm  --> 200*scale --> this is the base conversion thus to convert cm to our unit do -> 200/45.72 * val -> 4.37445319335 * val
    //Dartboard positioning 5 feet. 8 inches from floor to the center of the bull's-eye -->   152.4 + 20.32 cm --> 172.2 cm
    //Distance from front of the dartboard to the throwing line: 7 feet 9 1/4  inches. ---> 213.36 + 23.49500 --> 236.855 cm
    //Human arm 25 inches --> 63.5 cm --> half arm 32 cm 
    //room height --> 300 cm 
    //room width --> 150 cm
    //speed --> 1mm/s
    
    //Dart length (can't find info, but it seems a little longer than the triple-score circle that's 94) --> 96*scale
    
    //http://piccole.rispostesenzadomanda.com/post/76949354071
    
  //why didn't we use only positive positions?
    
    private static double cmToUnit(double cm) {
        return cm*4.37445319335;
    }
    
    public static final double SCALE = 0.20;
    
    public static final double MAX_SIZE_X = cmToUnit(300/2)*SCALE; 
    public static final double MAX_SIZE_Y = cmToUnit(200/2)*SCALE;
    public static final double MAX_SIZE_Z = cmToUnit(236.855/2)*SCALE; //represents half the length of the room
    
    public static final double CENTER_Y = cmToUnit(172.2)*Constants.SCALE - MAX_SIZE_Y; //NB floor is @ MAX_SIZE_Y units from the bottom of the room
    
    public static final double ARM_REACH = MAX_SIZE_Z - cmToUnit(32)*SCALE;

    public static final int FRAME_INTERVAL = 50;
    
    private static final double  MOON_GRAVITY =  1.6249; 
    private static final double EARTH_GRAVITY = 9.82;
    
    private static final double ACCELERATION_UNIT_MS_SQUARE = cmToUnit(MOON_GRAVITY/10000)*Constants.SCALE; //(9.81/1000000)*100 cm/ms^2;
    public static final double HALF_ACCELERATION = ACCELERATION_UNIT_MS_SQUARE/2;
    
    
}
