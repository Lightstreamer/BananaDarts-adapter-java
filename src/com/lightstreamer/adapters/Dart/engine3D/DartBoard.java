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

import java.awt.geom.Arc2D;

import com.croftsoft.core.math.geom.Circle;

public class DartBoard {

    //radius 100.12497369069196 -->
    //diameters:
    // 200
    //  150
    //  140
    //  94
    //  86
    //  14
    //   6
    // these diameters were measured directly on unit (no translation from cm)
    // *scale = 0.20
    
   
    
    
    private static final Circle mainCircle = new Circle(0,Constants.CENTER_Y,200/2);
    
    private static Circle[] circles = {
      new Circle(0,Constants.CENTER_Y,6/2),
      new Circle(0,Constants.CENTER_Y,14/2),
      new Circle(0,Constants.CENTER_Y,86/2),
      new Circle(0,Constants.CENTER_Y,94/2),
      new Circle(0,Constants.CENTER_Y,140/2),
      new Circle(0,Constants.CENTER_Y,150/2)
    };
    
    private static int circleValues[] = {
            50,
            25,
            1,
            3,
            1,
            2
    };
    
    
    private static Arc2D slices[] = new Arc2D[20];
    
    static {
        int inc = 360/20;
        int shift = inc/2;
        for (int i=0; i<20; i++) {
            slices[i] = new Arc2D.Double(-75,-Constants.CENTER_Y-75,150,150,inc*i-shift,18,Arc2D.PIE);
        }
    }
    
    private static int sliceValues[] = {
            6,
            13,
            4,
            18,
            1,
            20,
            5,
            12,
            9,
            14,
            11,
            8,
            16,
            7,
            19,
            3,
            17,
            2,
            15,
            10
    };
    
    
    public static int getScore(double x, double y) {
        
        for (int c=0; c<circles.length; c++) {
            if (circles[c].contains(x, y)) {
                if (circleValues[c]>3) { 
                    //not a multiplier
                    return circleValues[c];
                } else {
                    for (int a = 0; a<slices.length; a++) {
                        if (slices[a].contains(x, -y, 0.00001, 0.00001)) {
                            return sliceValues[a]*circleValues[c];
                        }
                    }
                }
            }
        }
        
        return 0;
    }


    public static boolean isInBoard(double x, double y) {
        return mainCircle.contains(x,y);
    }
    
    
}
