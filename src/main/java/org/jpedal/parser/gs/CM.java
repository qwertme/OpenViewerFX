/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2015 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * CM.java
 * ---------------
 */
package org.jpedal.parser.gs;

import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.CommandParser;
import org.jpedal.utils.Matrix;

/**
 *
 */
public class CM {
   
    public static void execute(final GraphicsState gs, final CommandParser parser) {

        //create temp Trm matrix to update Tm
        final float[][] Trm = new float[3][3];

        //set Tm matrix
        Trm[0][0] = parser.parseFloat(5);
        Trm[0][1] = parser.parseFloat(4);
        Trm[0][2] = 0;
        Trm[1][0] = parser.parseFloat(3);
        Trm[1][1] = parser.parseFloat(2);
        Trm[1][2] = 0;
        Trm[2][0] = parser.parseFloat(1);
        Trm[2][1] = parser.parseFloat(0);
        Trm[2][2] = 1;


        //copy last CM
        for(int i=0;i<3;i++) {
            System.arraycopy(gs.CTM, 0, gs.lastCTM, 0, 3);
        }

        //multiply to get new CTM
        gs.CTM = Matrix.multiply(Trm, gs.CTM);

        //remove slight sheer

        //if(gs.CTM[0][0]>0 && gs.CTM[1][1]>0 && gs.CTM[1][0]>0 && ((gs.CTM[1][0]<0.01 && gs.CTM[0][1]<0) || (gs.CTM[0][0]>100 && gs.CTM[0][1]<2))){

        //added for odd case with file upsidedownlogo.pdf
        if(gs.CTM[0][0]>0 && gs.CTM[1][1]>0 && Math.abs(gs.CTM[0][1])<0.001 && gs.CTM[1][0]==0){
            gs.CTM[0][1]=0;
        }
        
        if(gs.CTM[0][0]>0 && gs.CTM[1][1]>0 && gs.CTM[1][0]>0 && ((gs.CTM[1][0]<0.01 && gs.CTM[0][1]<0) || (gs.CTM[0][0]>100 && gs.CTM[0][1]==gs.CTM[1][0] && gs.CTM[0][1]==1))){
        
            gs.CTM[0][1]=0;
            gs.CTM[1][0]=0;
        }
        
        //Ignore very minor skew
//        if(Math.abs(gs.CTM[0][1]/gs.CTM[0][0])<0.006 && Math.abs(gs.CTM[1][0]/gs.CTM[1][1])<0.006){
//            gs.CTM[0][1]=0;
//            gs.CTM[1][0]=0;
//        }
        
        //deal with very minor rotation on page in 17780
        if(gs.CTM[1][0]>100 && gs.CTM[0][1]>100 && gs.CTM[0][0]<0.001 && Math.abs(gs.CTM[1][1])<0.001){
        
            gs.CTM[0][0]=0;
            gs.CTM[1][1]=0;
        }
    }


}


