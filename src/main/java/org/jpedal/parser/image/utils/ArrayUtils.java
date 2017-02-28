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
 * ArrayUtils.java
 * ---------------
 */
package org.jpedal.parser.image.utils;

/**
 *
 * @author markee
 */
public class ArrayUtils {
    
    public static boolean isArrayInverted(final float[] decodeArray) {
        
        //work out if inverted (assume true and disprove)
        boolean arrayInverted=false;
        if(decodeArray!=null){
            
            arrayInverted=true;
            final int count=decodeArray.length;
            for(int aa=0;aa<count;aa += 2){
                if(decodeArray[aa]==1f && decodeArray[aa+1]==0f){
                    //okay
                }else{
                    arrayInverted=false;
                    aa=count;
                }
            }
        }
        return arrayInverted;
    }
}
