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
 * BinaryImage.java
 * ---------------
 */

package org.jpedal.parser.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;

/**
 *
 * @author markee
 */
public class BinaryImage {
    
    public static BufferedImage make(int w, int h, byte[] data, final GenericColorSpace decodeColorData, int d) {
        
        final BufferedImage image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);
        
        /** create an image from the raw data*/
        final DataBuffer db = new DataBufferByte(data, data.length);
        //needs to be inverted in this case (ie 12dec/mariners_annual_2012.pdf)
        if(decodeColorData.getID()==ColorSpaces.Separation){
            final int count=data.length;
            for(int aa=0;aa<count;aa++) {
                data[aa] = (byte) (data[aa] ^ 255);
            }
        }
        
        final WritableRaster raster =Raster.createPackedRaster(db, w, h, d, null);
        image.setData(raster);
        
        return image;
    }
}
