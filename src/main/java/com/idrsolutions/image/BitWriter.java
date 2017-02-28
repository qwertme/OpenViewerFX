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
 * BitWriter.java
 * ---------------
 */
package com.idrsolutions.image;

import java.io.*;

/**
 * Bit writing capability do not forget to call end method
 */
public class BitWriter {

    private int bitCount;
    private int pointer;
    private final OutputStream stream;

    /**
     * Dont forget to call end()
     *
     * @param stream OutputStream to write to
     */
    public BitWriter(final OutputStream stream) {
        this.stream = stream;
    }

    /**
     *
     */
    public void end() {
        while (bitCount > 0) {
            pointer = pointer << 1;
            bitCount++;
            if (bitCount == 8) {
                try {
                    stream.write(pointer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pointer = 0;
                bitCount = 0;
            }
        }
    }

    /**
     *
     * @param bits
     * @param num
     */
    public void writeBits(final int bits, int num) {
        if ((num < 0) || (num > 32)) {
            throw new IllegalArgumentException("Number of bits is out of range");
        }

        while (num > 0) {
            final int cbit = Math.min(num, (8 - bitCount));
            pointer = (pointer << cbit) | ((bits >>> (num - cbit)) & ((1 << cbit) - 1));
            bitCount += cbit;
            num -= cbit;

            if (bitCount == 8) {
                try {
                    stream.write(pointer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pointer = 0;
                bitCount = 0;
            }
        }
    }

    /**
     *
     * @param nextByte
     */
    public void writeByte(final byte nextByte) {
        // fast path
        if (bitCount == 0) {
            try {
                stream.write(nextByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            writeBits(nextByte, 8);
        }
    }

    /**
     *
     * @param bytes
     */
    public void writeBytes(final byte[] bytes) {
        if (bitCount == 0) {
            try {
                stream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            for (final byte b : bytes) {
                writeByte(b);
            }
        }
    }

}
