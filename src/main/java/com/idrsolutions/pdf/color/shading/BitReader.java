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
 * BitReader.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

/**
 * a java class to handle bitwise reading in shading object
 */
public class BitReader {

    private int p; //pointer
    private int pos;
    private final byte[] data;
    private final boolean hasSmallBits;
    private int bufferSize;
    private int buffer;
    private final int totalBitLen;

    public BitReader(final byte[] data, final boolean hasSmallBits) {
        this.hasSmallBits = hasSmallBits;
        this.data = data;
        this.totalBitLen = this.data.length * 8;
    }

    /**
     * @param lenToRead
     * @return this return value is not actual int and it is a data
     * representation in 32 bits
     */
    private int readBits(int lenToRead) {
        if (hasSmallBits) {
            while (bufferSize < lenToRead) {
                int b = data[pos] & 0xff;
                pos++;
                buffer = (buffer << 8) | b;
                bufferSize += 8;
            }
            bufferSize -= lenToRead;
            p += lenToRead;
            return (buffer >>> bufferSize) & ((1 << lenToRead) - 1);
        } else {
            int retVal = 0;
            int len = lenToRead / 8;
            for (int i = 0; i < len; i++) {
                retVal = (retVal << 8);
                retVal |= ((data[p / 8] & 0xff));
                p += 8;
            }
            return retVal;
        }
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

    /**
     * return floating point;
     *
     * @param bitLen
     * @return
     */
    public float getFloat(int bitLen) {
        int value = readBits(bitLen);
        byte[] temp = {(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
        float number = 0.0f;
        switch (bitLen) {
            case 1:
            case 2:
            case 4:
                number = temp[3] / 15f;
                break;
            case 8:
                number = (temp[3] & 255) / 256f;
                break;
            case 16:
                number = (temp[2] & 255) / 256f;
                number += (temp[3] & 255) / 65536f;
                break;
            case 24:
                number = (temp[1] & 255) / 256f;
                number += (temp[2] & 255) / 65536f;
                number += (temp[3] & 255) / 16777216f;
                break;
            case 32:
                number = (temp[0] & 255) / 256f;
                number += (temp[1] & 255) / 65536f;
                number += (temp[2] & 255) / 16777216f;
                number += (temp[3] & 255) / 4294967296f;
                break;
        }
        return number;
    }

    public int getPointer() {
        return p;
    }

    public int getTotalBitLen() {
        return totalBitLen;
    }

//    public static void main(String[] args) {
    //        byte[] data = new byte[]{-128, 72, 0};
    //        BitSet bitset = new BitSet(data.length * 8);
    //        int c = 0;
    //        for (int i = 0; i < data.length; i++) {
    //            byte b = data[i];
    //            System.out.println(" "+Integer.toBinaryString(b));
    //            for (int j = 7; j >= 0; j--) {
    //                boolean isOn = ((b >> j) & 1) == 1;
    //                bitset.set(c, isOn);
    //                c++;//                
    //            }
    //        }//        
    //        BitReader bit = new BitReader(data);
    //       
    //        for (int i = 0; i < 24; i++) {
    //            System.out.println(" -- "+bit.readBits(3));
    //        }
//    }
}
