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
 * PngBitReader.java
 * ---------------
 */
package com.idrsolutions.image.png;

import java.util.BitSet;

public class PngBitReader {

    private int p; //pointer
    private final int totalBitLen;
    private BitSet bitset;
    private byte[] data;
    private final boolean hasSmallBits;

    public PngBitReader(final byte[] data, final boolean hasSmallBits) {
        this.hasSmallBits = hasSmallBits;
        this.totalBitLen = data.length * 8;
        if (this.hasSmallBits) {
            this.bitset = new BitSet(totalBitLen);
            int c = 0;
            int dLen = data.length;
            for (int i = 0; i < dLen; i++) {
                byte b = data[i];
                for (int j = 7; j >= 0; j--) {
                    boolean isOn = ((b >> j) & 1) == 1;
                    bitset.set(c, isOn);
                    c++;
                }
            }
        } else {
            this.data = data;
        }
    }

    /**
     * @param lenToRead
     * @return this return value is not actual int and it is a data
     * representation in 32 bits
     */
    private int readBits(int lenToRead) {
        int retVal = 0;
        if (hasSmallBits) {
            BitSet smallSet = bitset.get(p, p + lenToRead);
            for (int i = 0; i < lenToRead; i++) {
                if (smallSet.get(i)) {
                    retVal = (retVal << 1) | 1;
                } else {
                    retVal = (retVal << 1);
                }
            }
            p += lenToRead;
        } else {
            int len = lenToRead / 8;
            for (int i = 0; i < len; i++) {
                retVal = (retVal << 8);
                retVal |= ((data[p / 8] & 0xff));
                p += 8;
            }
        }
        return retVal;
    }

    /**
     * return positive integer only
     *
     * @param bitLen
     * @return
     */
    public int getPositive(int bitLen) {
        return readBits(bitLen);
    }

    public int getPointer() {
        return p;
    }

    public int getTotalBitLen() {
        return totalBitLen;
    }
}
