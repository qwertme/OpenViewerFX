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
* JPeg2000ImageDecoder.java
* ---------------
*/

package org.jpedal.parser.image;

import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.data.ImageData;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
public class JPeg2000ImageDecoder {
    
    
    public static BufferedImage decode(final String name, int w, int h, GenericColorSpace decodeColorData, byte[] data, final float[] decodeArray, final ImageData imageData, int d) throws RuntimeException, PdfException {
        
        
        BufferedImage image;
        
//needs imageio library
        
        if(LogWriter.isOutput()) {
            LogWriter.writeLog("JPeg 2000 Image " + name + ' ' + w + "W * " + h + 'H');
        }
        /**
         * try {
         * java.io.FileOutputStream a =new java.io.FileOutputStream("/Users/markee/Desktop/"+ name + ".jpg");
         *
         * a.write(data);
         * a.flush();
         * a.close();
         *
         * } catch (Exception e) {
         * LogWriter.writeLog("Unable to save jpeg " + name);
         *
         * }  /**/
        
        
        
        image = decodeColorData.JPEG2000ToRGBImage(data,w,h,decodeArray,imageData.getpX(),imageData.getpY(),d);
        
        return image;
    }
    
    //
    //OS version
    public static byte[] getBytesFromJPEG2000(final byte[] data, GenericColorSpace decodeColorData,final PdfObject XObject) {
    
        Raster ras= getRasterFromJPEG2000(data);
        
        return ((DataBufferByte)ras.getDataBuffer()).getData();
    }
    /**/
    
    //
    
    static Raster getRasterFromJPEG2000(final byte[] data) {
        
        final ByteArrayInputStream in;
        
        ImageReader iir=null;
        final ImageInputStream iin;
        
        Raster ras=null;
        
        try {
            
            //read the image data
            in = new ByteArrayInputStream(data);
            
            //suggestion from Carol
            final Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG2000");
            
            while (iterator.hasNext()){
                final Object o = iterator.next();
                iir = (ImageReader) o;
                if (iir.canReadRaster()) {
                    break;
                }
            }
            
            ImageIO.setUseCache(false);
            iin = ImageIO.createImageInputStream((in));
            iir.setInput(iin, true);
            ras=iir.read(0).getRaster();
            
            in.close();
            iir.dispose();
            iin.close();
            
        }catch(final Exception ee){
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Problem closing  " + ee);
            }
        }
        
        return ras;
    }
}
