package com.lightstreamer.adapters.Dart.engine3D;

public class Constants {

    //Real-world 
    //Dartboard diameter 18 inches    ---> 45.72 cm  --> 200*scale
    //Dartboard positioning 5 feet. 8 inches from floor to the center of the bull's-eye -->   152.4 + 20.32 cm --> 769*scale 
    //Distance from front of the dartboard to the throwing line: 7 feet 9 1/4  inches. ---> 213.36 + 23.49500 --> 1053*scale
    //Human arm 25 inches --> 63.5 cm --> half arm 32 cm --> 142
    //room height --> 300 cm --> 1333
    //room width --> 150 cm --> 666
    
    //Dart length (can't find info, but it seems a little longer than the triple-score circle that's 94) --> 96*scale
    
    
    public static final double SCALE = 0.20;
    
    public static final double MAX_SIZE_X = 1333/2*SCALE;
    public static final double MAX_SIZE_Y = 666/2*SCALE;
    public static final double MAX_SIZE_Z = 1053/2*SCALE;
    
    public static final double ARM_REACH = MAX_SIZE_Z - 142*SCALE;
    
}
