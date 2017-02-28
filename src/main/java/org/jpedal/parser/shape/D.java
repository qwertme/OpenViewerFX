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
 * D.java
 * ---------------
 */
package org.jpedal.parser.shape;

import org.jpedal.io.types.PdfArray;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.CommandParser;

public class D {

    
    public static void execute(final CommandParser parser, final GraphicsState gs) {


        String values; //used to combine values

        //and the dash array
        final int items = parser.getOperandCount();

        if(items==1) {
            values = parser.generateOpAsString(0, false);
        } else{
            //concat values
            StringBuilder list = new StringBuilder(15);
            for (int i = items - 1; i > -1; i--){
                list.append(parser.generateOpAsString(i, false));
                list.append(' ');
            }
            values=list.toString();
        }

        //allow for default
        if ((values.equals("[ ] 0 "))|| (values.equals("[]0"))|| (values.equals("[] 0 "))) {
            gs.setDashPhase(0);
            gs.setDashArray(new float[0]);
        } else {

            //get dash pattern
            final int pointer=values.indexOf(']');

            final String dash=values.substring(0,pointer);
            final int phase=(int)Float.parseFloat(values.substring(pointer+1,values.length()).trim());

            //put into dash array
            final float[] dash_array = PdfArray.convertToFloatArray(dash);

            for(int aa=0;aa<dash_array.length;aa++){
                // System.out.println(aa+" "+dash_array[aa]);

                if(dash_array[aa]<0.001) {
                    dash_array[aa] = 0;
                }
            }
            //put array into global value
            gs.setDashArray(dash_array);

            //last value is phase
            gs.setDashPhase(phase);

        }
    }

   
}
