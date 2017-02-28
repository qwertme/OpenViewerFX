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
 * JPXBitReader.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 * JPEG bitwise reader
 */
public class JPXBitReader {

    private int p; //pointer
    private final byte[] data;
    private int bufferSize;
    private int buffer;

    public JPXBitReader(byte[] data) {
        this.data = data;
    }

    public JPXBitReader(byte data) {
        this(new byte[]{data});
    }

    public int readBits(int lenToRead) {
        
        while (bufferSize < lenToRead) {
            final int b = data[p] & 0xff;
            p++;
            buffer = (buffer << 8) | b;
            bufferSize += 8;
        }
        bufferSize -= lenToRead;
        return (buffer >>> bufferSize) & ((1 << lenToRead) - 1);
    }

    public void reset() {
        p = 0;
    }

    public static void main(String[] args) {

        final JPXBitReader r = new JPXBitReader(new byte[]{44, 19});
        System.out.println(r.readBits(5));
        System.out.println(r.readBits(11));
    }

}
