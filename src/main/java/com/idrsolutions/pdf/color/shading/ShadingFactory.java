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
 * ShadingFactory.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import org.jpedal.function.PDFFunction;


/**
 * provides factory method to decode 
 * shading into required value
 */
public class ShadingFactory {
        
    public static float[] applyFunctions(final PDFFunction[] function, final float[] values) {

        final int functionLength= function.length;
        final float[] colValues;
        if(functionLength==1){
            colValues= function[0].compute(values);
        }else{

            final Object[] multiValues=new Object[functionLength];
            int count=0;
            for(int f=0;f<functionLength;f++){
                final float[] returnVal= function[f].compute(values);
                count += returnVal.length;
                multiValues[f]=returnVal;
            }

            colValues=new float[count];
            int ptr=0;
            for(int f=0;f<functionLength;f++){
                final float[] returnVal= (float[]) multiValues[f];

                for (final float aReturnVal : returnVal) {
                    colValues[ptr] = aReturnVal;

                    ptr++;
                }
            }
        }
        return colValues;
    }
    
   
}
