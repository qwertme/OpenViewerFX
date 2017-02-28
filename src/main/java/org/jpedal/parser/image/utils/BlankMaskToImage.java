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
 * BlankMaskToImage.java
 * ---------------
 */
package org.jpedal.parser.image.utils;

import org.jpedal.color.GenericColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.io.PdfFilteredReader;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.data.ImageData;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class BlankMaskToImage {


    public static boolean convert(byte[] objectData, ImageData imageData, GenericColorSpace decodeColorData, PdfObject newSMask, PdfObjectReader currentPdfFile) throws PdfException {

        newSMask.setFloatArray(PdfDictionary.Decode, new float[]{1, 0});

        final PdfArrayIterator Filters = newSMask.getMixedArray(PdfDictionary.Filter);

        boolean isMaskJPX = false;
        //check not handled elsewhere
        int firstValue;
        if (Filters != null && Filters.hasMoreTokens()) {
            while (Filters.hasMoreTokens()) {
                firstValue = Filters.getNextValueAsConstant(true);
                //isDCT=firstValue==PdfFilteredReader.DCTDecode;
                isMaskJPX = firstValue == PdfFilteredReader.JPXDecode;
            }
        }

        byte[] objData = currentPdfFile.readStream(newSMask, true, true, false, false, false, null);

        imageData.setObjectData(objData);
        imageData.setDepth(1);
        imageData.setWidth(newSMask.getInt(PdfDictionary.Width));
        imageData.setHeight(newSMask.getInt(PdfDictionary.Height));
        int newDepth = newSMask.getInt(PdfDictionary.BitsPerComponent);

        //JPEG2000 causes us special difficulties as we not actually decoding objectData because
        //it is JPEG2000 and we decode as part of image handling because Java does byte[] to image directly
        //
        //This code fixes specific example by getting the actual data and ignoring empty mask but will
        //probably need developing further if we find other examples
        if (isMaskJPX) { //see case 17665 for example
            BufferedImage img = decodeColorData.JPEG2000ToRGBImage(imageData.getObjectData(), imageData.getWidth(), imageData.getHeight(), null, -1, -1, 8);
            img = ColorSpaceConvertor.convertColorspace(img, 10);

            objData = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
            imageData.setObjectData(objData);
            boolean isEmptyMask = true;
            for (final byte b : objectData) {
                if (b != 0) {
                    isEmptyMask = false;
                    break;
                }
            }
            if (isEmptyMask) {
                imageData.setObjectData(null);
            }

            //we need to unset this as e have handled the JPX filter
            newSMask.setMixedArray(PdfDictionary.Filter, null);
        }

        if (newDepth != PdfDictionary.Unknown) {
            imageData.setDepth(newDepth);
        }

        return objData==null;
    }

}
