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
 * OneBitImage.java
 * ---------------
 */

package org.jpedal.parser.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
class OneBitImage {
    
    
    static  BufferedImage make(int d, int w, int h, byte[] data) throws RuntimeException {
        
        if(LogWriter.isOutput()) {
            LogWriter.writeLog("comp=1 and d= " + d);
        }
        
        BufferedImage image;
        
        data = ColorSpaceConvertor.normaliseTo8Bit(d, w, h, data);
        
        /** create an image from the raw data*/
        final DataBuffer db = new DataBufferByte(data, data.length);
        final int[] bands ={0};
        image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
        final Raster raster =Raster.createInterleavedRaster(db,w,h,w,1,bands,null);
        image.setData(raster);
        
        return image;
    }

}
