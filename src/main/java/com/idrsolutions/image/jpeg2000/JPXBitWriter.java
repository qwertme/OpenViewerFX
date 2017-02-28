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
 * -----------------
 * JPXBitWriter.java
 * -----------------
 */
package com.idrsolutions.image.jpeg2000;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author suda
 */
public class JPXBitWriter {

    private byte buffer;
    private int bitCount;
    private final OutputStream stream;

    public JPXBitWriter(OutputStream stream) {
        this.stream = stream;
    }
    
    public void writeUShort(int value){
        final byte b[] = {(byte) (value >> 8), (byte) value};
        for (int i = 0; i < b.length; i++) {
            writeByte(b[i]);
        }
    }

    public void writeUInt(long value) {
        final byte b[] = {(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        for (int i = 0; i < b.length; i++) {
            writeByte(b[i]);
        }
    }    

    public void writeBit(int bit) {
        buffer <<= 1;         // a bit can now be added
        buffer |= (bit & 1);  // the last bit of the parameter bit is added
        bitCount++;

        if (bitCount == 8) {
            bitCount = 0;
            try {
                stream.write(buffer);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void writeByte(byte b) {
        if (bitCount == 0) {
            try {
                stream.write(b);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            for (int j = 7; j >= 0; j--) {
                writeBit((b >> j) & 1);
            }
        }
    }

    public void end() {
        if (bitCount > 0) {
            final byte b = buffer <<= (8 - bitCount);
            try {
                stream.write(b);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            bitCount = 0;
        }
    }

    /**public static void main(String[] args) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final JPXBitWriter writer = new JPXBitWriter(bos);
        writer.writeUInt(2147483647);
        writer.end();
        bos.flush();
        bos.close();
        final byte ss[] = bos.toByteArray();
        System.out.println("done");

    }  /**/

}
