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
    // *scale = 0.20
    
    private static final double SCALE = 0.20;
    
    private static Circle[] circles = {
      new Circle(0,0,6*SCALE/2),
      new Circle(0,0,14*SCALE/2),
      new Circle(0,0,86*SCALE/2),
      new Circle(0,0,94*SCALE/2),
      new Circle(0,0,140*SCALE/2),
      new Circle(0,0,150*SCALE/2)
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
            slices[i] = new Arc2D.Double(-75*SCALE,-75*SCALE,150*SCALE,150*SCALE,inc*i-shift,18,Arc2D.PIE);
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
    
    
}
