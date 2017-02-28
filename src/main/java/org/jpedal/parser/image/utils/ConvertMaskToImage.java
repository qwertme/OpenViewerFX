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
 * ConvertMaskToImage.java
 * ---------------
 */
package org.jpedal.parser.image.utils;

import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

import java.awt.image.*;

public class ConvertMaskToImage {

    public static BufferedImage convert(PdfObject newSMask, PdfObjectReader currentPdfFile) {
        BufferedImage image;
        byte opacity, d0 = 0, d1 = (byte) 255;

        /**
         * get opacity if not default
         */
        final float[] maskDecode = newSMask.getFloatArray(PdfDictionary.Decode);
        if (maskDecode != null) {
            opacity = (byte) ((maskDecode[0]) * 255);

            //adjust range if needed (can be 0-1 or 0-255)
            if (maskDecode[0] <= 1 && maskDecode[1] <= 1) {

                if (maskDecode[0] == 1 && maskDecode[1] == 0) { //default reversed case
                    d1 = (byte) (255 * (maskDecode[0]));
                    d0 = (byte) (255 * (maskDecode[1]));
                } else if (maskDecode[0] < maskDecode[1]) { //range
                    d1 = (byte) (255 * (maskDecode[0]));
                    d0 = (byte) (255 * (1f - maskDecode[1]));
                } else { //default case
                    d0 = (byte) (255 * (maskDecode[0]));
                    d1 = (byte) (255 * (maskDecode[1]));
                }
            }
        }else{ //correct values for null MaskDecode //see 15Jan/21330.pdf
            d0=(byte)255;
            d1=0;
            opacity=(byte)0;
        }

        //use black/white colour
        final byte[] maskIndex = {d0, d0, d0, d1, d1, d1};

        //get raw 1 bit data for smask
        final byte[] data = currentPdfFile.readStream(newSMask, true, true, false, false, false, null);

        //get dimensions
        final int w = newSMask.getInt(PdfDictionary.Width);
        final int h = newSMask.getInt(PdfDictionary.Height);

        //buffer for byte data
        final int length = w * h * 4;
        final byte[] objData = new byte[length];

        //create ARGB data from 1bit data and opacity
        ColorSpaceConvertor.flatten1bpc(w, data, 3,maskIndex, true, length, opacity, objData);

        //build image
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final DataBuffer db = new DataBufferByte(objData, objData.length);
        final WritableRaster raster = Raster.createInterleavedRaster(db, w, h, w * 4, 4, new int[]{0, 1, 2, 3}, null);
        image.setData(raster);

        return image;
    }

}
