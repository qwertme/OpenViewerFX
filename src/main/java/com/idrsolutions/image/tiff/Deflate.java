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
 * Deflate.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Deflate {

    public static byte[] decompress(byte[] compressedData) {
        Inflater decompressor = new Inflater();
        decompressor.setInput(compressedData);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
        byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
                bos.close();
            } catch (IOException ex) {
                writeLog("Exception: " + ex.getMessage());
            } catch (DataFormatException ex) {
                writeLog("Exception: " + ex.getMessage());
            }
        }
        return bos.toByteArray();
    }

    public static byte[] compress(final byte[] pixels) throws IOException {
        final Deflater deflater;
        deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(pixels);
        final int min = Math.min(pixels.length / 2, 4096);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(min);
        deflater.finish();
        final byte[] buffer = new byte[min];
        while (!deflater.finished()) {
            final int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        deflater.end();
        outputStream.close();
        return outputStream.toByteArray();
    }
    
    private static void writeLog(final String msg) {
        System.out.println(msg);
    }
}
