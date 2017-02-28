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
 * ObjectCloneFactory.java
 * ---------------
 */
package org.jpedal.utils;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Map;

/**
 * custom optimised cloning code for speed
 */
public class ObjectCloneFactory {
    
    public static byte[] cloneArray(final byte[] array){
        
        if(array==null) {
            return null;
        }
        
        final int count=array.length;
        
        final byte[] returnValue=new byte[count];
        
        System.arraycopy(array,0,returnValue,0,count);
        
        return returnValue;
        
    }
    
    public static byte[][] cloneDoubleArray(final byte[][] byteDArray) {
        if(byteDArray == null){
            return null;
        }else {
            final byte[][] tmp = new byte[byteDArray.length][];
            for (int b = 0; b < byteDArray.length; b++) {
                tmp[b] = byteDArray[b].clone();
            }
            return tmp;
        }
    }
    
    public static BufferedImage deepCopy(final BufferedImage bi) {
        if(bi==null) {
            return null;
        }
        
        final ColorModel cm = bi.getColorModel();
        final boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        final WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    public static Map cloneMap(final Map optValues) {
        if(optValues!=null){
            try {
                final Map tmpMap = optValues.getClass().newInstance();
                tmpMap.putAll(optValues);
                return tmpMap;
            } catch (final Exception e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: "+e.getMessage());
                }
                //
            }
        }
        
        return null;
    }
}
