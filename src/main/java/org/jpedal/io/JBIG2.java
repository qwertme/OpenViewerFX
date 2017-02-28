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
 * JBIG2.java
 * ---------------
 */
package org.jpedal.io;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.jpedal.jbig2.*;

public class JBIG2 {
    
    /**
     * if this values is set to any values other than -1 than JBIGDecoder
     * will write the data to temporary files which is defined in Objectstore.tempdir
     * recommended value is 1024 for better performance.
     */
    public static int MAXIMUM_FILESIZE_IN_MEMORY = -1;
    public static boolean IS_BITMAPS_ON_FILE;
    
    /**
     * JBIG decode using our own class
     *
     */
    //
    public static byte[] JBIGDecode(final byte[] data, final byte[] globalData) throws Exception { 
        byte[] returnData;
        final org.jpedal.jbig2.JBIG2Decoder decoder = new org.jpedal.jbig2.JBIG2Decoder();
        if (globalData != null && globalData.length > 0) {
            decoder.setGlobalData(globalData);
        }        
        decoder.decodeJBIG2(data);
        returnData= decoder.getPageAsJBIG2Bitmap(0).getData(true);
        return returnData;
    }    
    /**/
    
    
}
