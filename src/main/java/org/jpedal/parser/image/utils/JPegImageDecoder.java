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
 * JPegImageDecoder.java
 * ---------------
 */

package org.jpedal.parser.image.utils;

import java.awt.image.BufferedImage;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.DeviceCMYKColorSpace;
import org.jpedal.color.DeviceRGBColorSpace;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.external.ErrorTracker;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.image.data.ImageData;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
public class JPegImageDecoder {
    
    
    public static BufferedImage decode(final String name, int w, int h, boolean arrayInverted,
            GenericColorSpace decodeColorData, byte[] data, final float[] decodeArray, final ImageData imageData,
            final PdfObject XObject, ErrorTracker errorTracker, ParserOptions parserOptions) {
        
        GenericColorSpace jpegDecodeColorData=decodeColorData;
        
        BufferedImage image=null;
        
        //handle JPEGS
        
        if(LogWriter.isOutput()) {
            LogWriter.writeLog("JPeg Image " + name + ' ' + w + "W * " + h + 'H' + " arrayInverted=" + arrayInverted);
        }
        
        /**
        try {
            java.io.FileOutputStream a =new java.io.FileOutputStream("/Users/markee/Desktop/jpg.jpg");
            
            a.write(data);
            a.flush();
            a.close();
            
        } catch (Exception e) {
            LogWriter.writeLog("Unable to save jpeg " + name);
            
        }  /**/
        
        //if ICC with Alt RGB, use alternative first
        boolean decodedOnAltColorspace=false;
        if(decodeColorData.getID()==ColorSpaces.ICC){
            
            
            //try first and catch any error
            final int alt=decodeColorData.getAlternateColorSpace();
            
            GenericColorSpace altDecodeColorData = null;
            
            if(alt==ColorSpaces.DeviceRGB) {
                altDecodeColorData = new DeviceRGBColorSpace();
            } else if(alt==ColorSpaces.DeviceCMYK) {
                altDecodeColorData = new DeviceCMYKColorSpace();
            }
            
            //try if any alt found
            if(altDecodeColorData!=null){
                
                try{
                    image=altDecodeColorData.JPEGToRGBImage(data, w, h, decodeArray,imageData.getpX() , imageData.getpY() , arrayInverted, XObject);
                    
                    //if it returns image it worked flag and switch over
                    if(image!=null){
                        decodedOnAltColorspace=true;
                        jpegDecodeColorData=altDecodeColorData;
                        
                        //flag if YCCK
                        if(jpegDecodeColorData.isImageYCCK()) {
                            parserOptions.hasYCCKimages = true;
                        }
                    }
                }catch(final Exception e){
                    errorTracker.addPageFailureMessage("Unable to use alt colorspace with " + name + " to JPEG");
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                    if(image!=null){
                        image.flush();
                    }
                    image=null;
                }
            }
        }
        /**decode if not done above*/
        if(!decodedOnAltColorspace){
            //separation, renderer
            try{
                image=jpegDecodeColorData.JPEGToRGBImage(data, w, h, decodeArray,imageData.getpX(),imageData.getpY() , arrayInverted, XObject);
                
                //flag if YCCK
                if(jpegDecodeColorData.isImageYCCK()) {
                    parserOptions.hasYCCKimages = true;
                }
                
                //image=simulateOP(image);
            }catch(final Exception e){
                errorTracker.addPageFailureMessage("Problem converting " + name + " to JPEG");
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
                image.flush();
                image=null;
            }
        }
        return image;
    }
    
}
