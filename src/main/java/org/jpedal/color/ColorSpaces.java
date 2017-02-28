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
 * ColorSpaces.java
 * ---------------
 */
package org.jpedal.color;

import org.jpedal.utils.LogWriter;

import java.awt.RenderingHints;
import java.lang.reflect.Field;

public class ColorSpaces {
    
    public static final int ICC = 1247168582;
    public static final int CalGray = 391471749;
    public static final int DeviceGray = 1568372915;
    public static final int DeviceN = 960981604;
    public static final int Separation = -2073385820;
    public static final int Pattern = 1146450818;
    public static final int Lab = 1847602;
    public static final int Indexed = 895578984;
    public static final int DeviceRGB = 1785221209;
    public static final int CalRGB = 1008872003;
    public static final int DeviceCMYK = 1498837125;
    
    /**hint for conversion ops*/
    public static final RenderingHints hints;
    
    
    static {
        hints =
                new RenderingHints(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(
                RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);
        hints.put(
                RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        hints.put(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
    }
    
    /**method to convert a name to an ID values*/
    @SuppressWarnings("UnusedDeclaration")
    public static final int convertNameToID(final String name){
        
        int id=-1;
        
        if ((name.contains("Indexed"))) {
            id=Indexed;
        } else if ((name.contains("Separation"))) {
            id=Separation;
        } else if (name.contains("DeviceN")) {
            id=DeviceN;
        } else if (name.contains("DeviceCMYK") || name.contains("CMYK")) {
            id=DeviceCMYK;
        } else if (name.contains("CalGray")) {
            id=CalGray;
        } else if (name.contains("CalRGB")) {
            id=CalRGB;
        } else if (name.contains("Lab")) {
            id=Lab;
        } else if (name.contains("ICCBased")) {
            id=ICC;
        } else if (name.contains("Pattern")) {
            id=Pattern;
        } else if (name.contains("DeviceRGB") || name.contains("RGB")) {
            id=DeviceRGB;
        } else if (name.contains("DeviceGray") || name.indexOf('G') != -1) {
            id=DeviceGray;
        }
        
        return id;
    }
    
    /**
     * use reflection to show actual Constant for Key or return null if no value
     * @param parameterConstant
     * @return String or null
     */
    public static String showAsConstant(final int parameterConstant) {
        
        final Field[] ts = ColorSpaces.class.getFields();
        int count=ts.length;
        String type=null;
        
        for(int ii=0;ii<count;ii++){
            try{
                //if(ts[ii] instanceof Integer){
                final int t=ts[ii].getInt(new ColorSpaces());
                
                if(t==parameterConstant){
                    type="ColorSpaces."+ts[ii].getName();
                    count=ii;
                }
                //}
            }catch(final IllegalAccessException e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: "+e.getMessage());
                }
                //
            }catch(IllegalArgumentException ee){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: "+ee.getMessage());
                }
                //
            }
        }
        
        return type;
    }
    
    /**
     * turn ID into string (without reflection as I want it FAST
     */
    public static String IDtoString(final int id) {
        
        switch (id) {
            case ICC:
                return "ICC";
                
            case CalGray:
                return "CalGray";
                
            case DeviceGray:
                return "DeviceGray";
                
            case DeviceN:
                return "DeviceN";
                
            case Separation:
                return "Separation";
                
            case Pattern:
                return "Pattern";
                
            case Lab:
                return "Lab";
                
            case Indexed:
                return "Indexed";
                
            case DeviceRGB:
                return "DeviceRGB";
                
            case CalRGB:
                return "CalRGB";
                
            case DeviceCMYK:
                return "DeviceCMYK";
        }
        return "unknown";
    }
}
