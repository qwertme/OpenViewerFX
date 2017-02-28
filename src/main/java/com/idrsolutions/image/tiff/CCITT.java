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
 * CCITT.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

public class CCITT {

    private static final short[] WHITERUN = {
        6430, 6400, 6400, 6400, 3225, 3225, 3225, 3225, 944, 944, 944, 944, 976,
        976, 976, 976, 1456, 1456, 1456, 1456, 1488, 1488, 1488, 1488, 718, 718,
        718, 718, 718, 718, 718, 718, 750, 750, 750, 750, 750, 750, 750, 750, 1520,
        1520, 1520, 1520, 1552, 1552, 1552, 1552, 428, 428, 428, 428, 428, 428,
        428, 428, 428, 428, 428, 428, 428, 428, 428, 428, 654, 654, 654, 654, 654,
        654, 654, 654, 1072, 1072, 1072, 1072, 1104, 1104, 1104, 1104, 1136, 1136,
        1136, 1136, 1168, 1168, 1168, 1168, 1200, 1200, 1200, 1200, 1232, 1232,
        1232, 1232, 622, 622, 622, 622, 622, 622, 622, 622, 1008, 1008, 1008, 1008,
        1040, 1040, 1040, 1040, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
        44, 44, 44, 44, 396, 396, 396, 396, 396, 396, 396, 396, 396, 396, 396, 396,
        396, 396, 396, 396, 1712, 1712, 1712, 1712, 1744, 1744, 1744, 1744, 846,
        846, 846, 846, 846, 846, 846, 846, 1264, 1264, 1264, 1264, 1296, 1296,
        1296, 1296, 1328, 1328, 1328, 1328, 1360, 1360, 1360, 1360, 1392, 1392,
        1392, 1392, 1424, 1424, 1424, 1424, 686, 686, 686, 686, 686, 686, 686, 686,
        910, 910, 910, 910, 910, 910, 910, 910, 1968, 1968, 1968, 1968, 2000, 2000,
        2000, 2000, 2032, 2032, 2032, 2032, 16, 16, 16, 16, 10257, 10257, 10257,
        10257, 12305, 12305, 12305, 12305, 330, 330, 330, 330, 330, 330, 330, 330,
        330, 330, 330, 330, 330, 330, 330, 330, 330, 330, 330, 330, 330, 330, 330,
        330, 330, 330, 330, 330, 330, 330, 330, 330, 362, 362, 362, 362, 362, 362,
        362, 362, 362, 362, 362, 362, 362, 362, 362, 362, 362, 362, 362, 362, 362,
        362, 362, 362, 362, 362, 362, 362, 362, 362, 362, 362, 878, 878, 878, 878,
        878, 878, 878, 878, 1904, 1904, 1904, 1904, 1936, 1936, 1936, 1936, -18413,
        -18413, -16365, -16365, -14317, -14317, -10221, -10221, 590, 590, 590, 590,
        590, 590, 590, 590, 782, 782, 782, 782, 782, 782, 782, 782, 1584, 1584,
        1584, 1584, 1616, 1616, 1616, 1616, 1648, 1648, 1648, 1648, 1680, 1680,
        1680, 1680, 814, 814, 814, 814, 814, 814, 814, 814, 1776, 1776, 1776, 1776,
        1808, 1808, 1808, 1808, 1840, 1840, 1840, 1840, 1872, 1872, 1872, 1872,
        6157, 6157, 6157, 6157, 6157, 6157, 6157, 6157, 6157, 6157, 6157, 6157,
        6157, 6157, 6157, 6157, -12275, -12275, -12275, -12275, -12275, -12275,
        -12275, -12275, -12275, -12275, -12275, -12275, -12275, -12275, -12275,
        -12275, 14353, 14353, 14353, 14353, 16401, 16401, 16401, 16401, 22547,
        22547, 24595, 24595, 20497, 20497, 20497, 20497, 18449, 18449, 18449,
        18449, 26643, 26643, 28691, 28691, 30739, 30739, -32749, -32749, -30701,
        -30701, -28653, -28653, -26605, -26605, -24557, -24557, -22509, -22509,
        -20461, -20461, 8207, 8207, 8207, 8207, 8207, 8207, 8207, 8207, 72, 72,
        72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72,
        72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72,
        72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72,
        72, 72, 72, 72, 72, 72, 72, 72, 104, 104, 104, 104, 104, 104, 104, 104,
        104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104,
        104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104,
        104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104,
        104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 4107, 4107, 4107,
        4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107,
        4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107,
        4107, 4107, 4107, 4107, 4107, 266, 266, 266, 266, 266, 266, 266, 266, 266,
        266, 266, 266, 266, 266, 266, 266, 266, 266, 266, 266, 266, 266, 266, 266,
        266, 266, 266, 266, 266, 266, 266, 266, 298, 298, 298, 298, 298, 298, 298,
        298, 298, 298, 298, 298, 298, 298, 298, 298, 298, 298, 298, 298, 298, 298,
        298, 298, 298, 298, 298, 298, 298, 298, 298, 298, 524, 524, 524, 524, 524,
        524, 524, 524, 524, 524, 524, 524, 524, 524, 524, 524, 556, 556, 556, 556,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 136, 136, 136,
        136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136,
        136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136,
        136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136,
        136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136, 136,
        136, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168,
        168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168,
        168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168,
        168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168, 168,
        168, 168, 168, 168, 168, 460, 460, 460, 460, 460, 460, 460, 460, 460, 460,
        460, 460, 460, 460, 460, 460, 492, 492, 492, 492, 492, 492, 492, 492, 492,
        492, 492, 492, 492, 492, 492, 492, 2059, 2059, 2059, 2059, 2059, 2059,
        2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059,
        2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059,
        2059, 2059, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
        200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
        200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
        200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
        200, 200, 200, 200, 200, 200, 200, 232, 232, 232, 232, 232, 232, 232, 232,
        232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232,
        232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232,
        232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232,
        232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232
    };

    private static final short[] BLACKRUN = {
        62, 62, 30, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225,
        3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225,
        3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225,
        588, 588, 588, 588, 588, 588, 588, 588, 1680, 1680, 20499, 22547, 24595,
        26643, 1776, 1776, 1808, 1808, -24557, -22509, -20461, -18413, 1904, 1904,
        1936, 1936, -16365, -14317, 782, 782, 782, 782, 814, 814, 814, 814, -12269,
        -10221, 10257, 10257, 12305, 12305, 14353, 14353, 16403, 18451, 1712, 1712,
        1744, 1744, 28691, 30739, -32749, -30701, -28653, -26605, 2061, 2061, 2061,
        2061, 2061, 2061, 2061, 2061, 424, 424, 424, 424, 424, 424, 424, 424, 424,
        424, 424, 424, 424, 424, 424, 424, 424, 424, 424, 424, 424, 424, 424, 424,
        424, 424, 424, 424, 424, 424, 424, 424, 750, 750, 750, 750, 1616, 1616,
        1648, 1648, 1424, 1424, 1456, 1456, 1488, 1488, 1520, 1520, 1840, 1840,
        1872, 1872, 1968, 1968, 8209, 8209, 524, 524, 524, 524, 524, 524, 524, 524,
        556, 556, 556, 556, 556, 556, 556, 556, 1552, 1552, 1584, 1584, 2000, 2000,
        2032, 2032, 976, 976, 1008, 1008, 1040, 1040, 1072, 1072, 1296, 1296, 1328,
        1328, 718, 718, 718, 718, 456, 456, 456, 456, 456, 456, 456, 456, 456, 456,
        456, 456, 456, 456, 456, 456, 456, 456, 456, 456, 456, 456, 456, 456, 456,
        456, 456, 456, 456, 456, 456, 456, 326, 326, 326, 326, 326, 326, 326, 326,
        326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326,
        326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326,
        326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326,
        326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 326, 358, 358, 358, 358,
        358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358,
        358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358,
        358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358,
        358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358,
        490, 490, 490, 490, 490, 490, 490, 490, 490, 490, 490, 490, 490, 490, 490,
        490, 4113, 4113, 6161, 6161, 848, 848, 880, 880, 912, 912, 944, 944, 622,
        622, 622, 622, 654, 654, 654, 654, 1104, 1104, 1136, 1136, 1168, 1168,
        1200, 1200, 1232, 1232, 1264, 1264, 686, 686, 686, 686, 1360, 1360, 1392,
        1392, 12, 12, 12, 12, 12, 12, 12, 12, 390, 390, 390, 390, 390, 390, 390,
        390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390,
        390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390,
        390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390,
        390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390
    };

    private static final short[] MOREBLACKS = {
        28679, 28679, 31752, (short) 32777, (short) 33801, (short) 34825,
        (short) 35849, (short) 36873, (short) 29703, (short) 29703, (short) 30727,
        (short) 30727, (short) 37897, (short) 38921, (short) 39945, (short) 40969
    };

    private static final short[] FIRSTBLACKS = {
        3226, 6412, 200, 168, 38, 38, 134, 134, 100, 100, 100, 100, 68, 68, 68, 68
    };

    private static final short[] BLACKS2BIT = {292, 260, 226, 226};

    private static final int[] LEFTBITS = {0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff};

    private static final int[] CURBITS = {
        0x00, 0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe, 0xff
    };

    private static final byte[] LITTLEENDIANS = {
        0, -128, 64, -64, 32, -96, 96, -32, 16, -112, 80, -48, 48, -80, 112, -16,
        8, -120, 72, -56, 40, -88, 104, -24, 24, -104, 88, -40, 56, -72, 120, -8,
        4, -124, 68, -60, 36, -92, 100, -28, 20, -108, 84, -44, 52, -76, 116, -12,
        12, -116, 76, -52, 44, -84, 108, -20, 28, -100, 92, -36, 60, -68, 124, -4,
        2, -126, 66, -62, 34, -94, 98, -30, 18, -110, 82, -46, 50, -78, 114, -14,
        10, -118, 74, -54, 42, -86, 106, -22, 26, -102, 90, -38, 58, -70, 122, -6,
        6, -122, 70, -58, 38, -90, 102, -26, 22, -106, 86, -42, 54, -74, 118, -10,
        14, -114, 78, -50, 46, -82, 110, -18, 30, -98, 94, -34, 62, -66, 126, -2,
        1, -127, 65, -63, 33, -95, 97, -31, 17, -111, 81, -47, 49, -79, 113, -15,
        9, -119, 73, -55, 41, -87, 105, -23, 25, -103, 89, -39, 57, -71, 121, -7,
        5, -123, 69, -59, 37, -91, 101, -27, 21, -107, 85, -43, 53, -75, 117, -11,
        13, -115, 77, -51, 45, -83, 109, -19, 29, -99, 93, -35, 61, -67, 125, -3,
        3, -125, 67, -61, 35, -93, 99, -29, 19, -109, 83, -45, 51, -77, 115, -13,
        11, -117, 75, -53, 43, -85, 107, -21, 27, -101, 91, -37, 59, -69, 123, -5,
        7, -121, 71, -57, 39, -89, 103, -25, 23, -105, 87, -41, 55, -73, 119, -9,
        15, -113, 79, -49, 47, -81, 111, -17, 31, -97, 95, -33, 63, -65, 127, -1
    };

    private static final byte[] Codes2D = {
        80, 88, 23, 71, 30, 30, 62, 62, 4, 4, 4, 4, 4, 4, 4, 4,
        11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
        35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35,
        51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
        41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
        41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
        41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
        41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41
    };
    
    private byte[] input, output;
    private int[] prevItems, curItems;
    private int processItem;
    private int compression = 2;
    private int changingElemSize;
    private int bitP, bp, dim1, bitsToPut;
    private final int endianOrder, w;// h;
       
    public CCITT(int fillOrder, int w, int h) {
        this.endianOrder = fillOrder;
        this.w = w;
        //this.h = h;
        this.bitP = 0;
        this.bp = 0;
        this.prevItems = new int[w + 1];
        this.curItems = new int[w + 1];
    }

    public void decompress1D(byte[] output, byte[] input, int startX, int height) {
        this.input = input;
        this.output = output;

        int lineOffset = 0;
        int scanlineStride = (w + 7) / 8;

        bitP = 0;
        bp = 0;

        for (int i = 0; i < height; i++) {
            processNextLine(lineOffset, startX);
            lineOffset += scanlineStride;
        }
    }

    public void decompressFax3(byte[] output, byte[] input, int height) {
        this.input = input;
        this.output = output;
        compression = 3;
        int tiffT4Options = 0;
        bitP = 0;
        bp = 0;

        int scanlineStride = (w + 7) / 8;

        int a0, a1, b1, b2;
        int[] b = new int[2];
        int entry, code, bits;
        boolean isWhite;
        int currIndex;
        int temp[];

        dim1 = tiffT4Options & 0x01;
        bitsToPut = (tiffT4Options & 0x04) >> 2;

        if (readEndOfLine(true) != 1) {
            throw new RuntimeException("TIFFFaxDecoder3");
        }

        int lineOffset = 0;
        int bitOffset;

        processNextLine(lineOffset, 0);
        lineOffset += scanlineStride;

        for (int lines = 1; lines < height; lines++) {

            if (readEndOfLine(false) == 0) {
                temp = prevItems;
                prevItems = curItems;
                curItems = temp;
                currIndex = 0;

                a0 = -1;
                isWhite = true;
                bitOffset = 0;

                processItem = 0;

                while (bitOffset < w) {
                    updateNextChangeItem(a0, isWhite, b);

                    b1 = b[0];
                    b2 = b[1];

                    entry = readBalanceBits(7);

                    entry = Codes2D[entry] & 0xff;

                    code = (entry & 0x78) >>> 3;
                    bits = entry & 0x07;

                    if (code == 0) {
                        if (!isWhite) {
                            invertToBlack(lineOffset, bitOffset,
                                    b2 - bitOffset);
                        }
                        bitOffset = a0 = b2;

                        movePointers(7 - bits);
                    } else if (code == 1) {
                        movePointers(7 - bits);
                        int number;
                        if (isWhite) {
                            number = decodeWhiteCodeWord();
                            bitOffset += number;
                            curItems[currIndex++] = bitOffset;

                            number = decodeBlackCodeWord();
                            invertToBlack(lineOffset, bitOffset, number);
                            bitOffset += number;
                            curItems[currIndex++] = bitOffset;
                        } else {
                            number = decodeBlackCodeWord();
                            invertToBlack(lineOffset, bitOffset, number);
                            bitOffset += number;
                            curItems[currIndex++] = bitOffset;

                            number = decodeWhiteCodeWord();
                            bitOffset += number;
                            curItems[currIndex++] = bitOffset;
                        }

                        a0 = bitOffset;
                    } else if (code <= 8) {
                        a1 = b1 + (code - 5);

                        curItems[currIndex++] = a1;

                        if (!isWhite) {
                            invertToBlack(lineOffset, bitOffset,
                                    a1 - bitOffset);
                        }
                        bitOffset = a0 = a1;
                        isWhite = !isWhite;

                        movePointers(7 - bits);
                    } else {
                        throw new RuntimeException("TIFFFaxDecoder4");
                    }
                }

                curItems[currIndex++] = bitOffset;
                changingElemSize = currIndex;
            } else {
                processNextLine(lineOffset, 0);
            }

            lineOffset += scanlineStride;
        }
    }

    public void decompressFax4(byte[] output, byte[] input, int height) {
        this.input = input;
        this.output = output;
        compression = 4;

        bitP = 0;
        bp = 0;

        int scanlineStride = (w + 7) / 8;

        int a0, a1, b1, b2;
        int entry, code, bits;
        boolean isWhite;
        int currIndex;
        int temp[];

        int[] b = new int[2];

        int[] cce = curItems;

        changingElemSize = 0;
        cce[changingElemSize++] = w;
        cce[changingElemSize++] = w;

        int lineOffset = 0;
        int bitOffset;

        for (int lines = 0; lines < height; lines++) {
            a0 = -1;
            isWhite = true;

            temp = prevItems;
            prevItems = curItems;
            cce = curItems = temp;
            currIndex = 0;

            bitOffset = 0;

            processItem = 0;

            while (bitOffset < w) {
                updateNextChangeItem(a0, isWhite, b);
                b1 = b[0];
                b2 = b[1];

                entry = readBalanceBits(7);
                entry = Codes2D[entry] & 0xff;
                code = (entry & 0x78) >>> 3;
                bits = entry & 0x07;

                if (code == 0) {
                    if (!isWhite) {
                        invertToBlack(lineOffset, bitOffset,
                                b2 - bitOffset);
                    }
                    bitOffset = a0 = b2;

                    movePointers(7 - bits);
                } else if (code == 1) {
                    movePointers(7 - bits);

                    int number;
                    if (isWhite) {
                        number = decodeWhiteCodeWord();
                        bitOffset += number;
                        cce[currIndex++] = bitOffset;

                        number = decodeBlackCodeWord();
                        invertToBlack(lineOffset, bitOffset, number);
                        bitOffset += number;
                        cce[currIndex++] = bitOffset;
                    } else {
                        number = decodeBlackCodeWord();
                        invertToBlack(lineOffset, bitOffset, number);
                        bitOffset += number;
                        cce[currIndex++] = bitOffset;

                        number = decodeWhiteCodeWord();
                        bitOffset += number;
                        cce[currIndex++] = bitOffset;
                    }

                    a0 = bitOffset;
                } else if (code <= 8) {
                    a1 = b1 + (code - 5);
                    cce[currIndex++] = a1;

                    if (!isWhite) {
                        invertToBlack(lineOffset, bitOffset,
                                a1 - bitOffset);
                    }
                    bitOffset = a0 = a1;
                    isWhite = !isWhite;

                    movePointers(7 - bits);
                } else if (code == 11) {
                    if (readBalanceBits(3) != 7) {
                        throw new RuntimeException("TIFFFaxDecoder5");
                    }

                    int zeros = 0;
                    boolean exit = false;

                    while (!exit) {
                        while (readBalanceBits(1) != 1) {
                            zeros++;
                        }

                        if (zeros > 5) {
                            zeros = zeros - 6;

                            if (!isWhite && (zeros > 0)) {
                                cce[currIndex++] = bitOffset;
                            }

                            bitOffset += zeros;
                            if (zeros > 0) {
                                isWhite = true;
                            }

                            if (readBalanceBits(1) == 0) {
                                if (!isWhite) {
                                    cce[currIndex++] = bitOffset;
                                }
                                isWhite = true;
                            } else {
                                if (isWhite) {
                                    cce[currIndex++] = bitOffset;
                                }
                                isWhite = false;
                            }

                            exit = true;
                        }

                        if (zeros == 5) {
                            if (!isWhite) {
                                cce[currIndex++] = bitOffset;
                            }
                            bitOffset += zeros;

                            isWhite = true;
                        } else {
                            bitOffset += zeros;

                            cce[currIndex++] = bitOffset;
                            invertToBlack(lineOffset, bitOffset, 1);
                            ++bitOffset;

                            isWhite = false;
                        }

                    }
                } else {
                    throw new RuntimeException("TIFFFaxDecoder5");
                }
            }

            if (currIndex <= w) {
                cce[currIndex++] = bitOffset;
            }

            changingElemSize = currIndex;
            lineOffset += scanlineStride;
        }
    }

    private void processNextLine(int lineOffset, int bitOffset) {
        int bits, code, isT;
        int current, entry, twoBits;
        boolean isWhite = true;

        changingElemSize = 0;

        while (bitOffset < w) {
            while (isWhite) {
                current = readBits(10);
                entry = WHITERUN[current];

                isT = entry & 0x0001;
                bits = (entry >>> 1) & 0x0f;

                if (bits == 12) {
                    twoBits = readBalanceBits(2);
                    current = ((current << 2) & 0x000c) | twoBits;
                    entry = MOREBLACKS[current];
                    bits = (entry >>> 1) & 0x07;
                    code = (entry >>> 4) & 0x0fff;
                    bitOffset += code;

                    movePointers(4 - bits);
                } else if (bits == 0) {
                    throw new RuntimeException("TIFFFaxDecoder0");
                } else if (bits == 15) {
                    movePointers(12);
                    return;
                } else {
                    code = (entry >>> 5) & 0x07ff;
                    bitOffset += code;
                    movePointers(10 - bits);
                    if (isT == 0) {
                        isWhite = false;
                        curItems[changingElemSize++] = bitOffset;
                    }
                }
            }

            if (bitOffset == w) {
                if (compression == 2) {
                    if (bitP != 0) {
                        bp++;
                        bitP = 0;
                    }
                }
                break;
            }

            while (isWhite == false) {
                current = readBalanceBits(4);
                entry = FIRSTBLACKS[current];

                bits = (entry >>> 1) & 0x000f;
                code = (entry >>> 5) & 0x07ff;

                if (code == 100) {
                    current = readBits(9);
                    entry = BLACKRUN[current];

                    isT = entry & 0x0001;
                    bits = (entry >>> 1) & 0x000f;
                    code = (entry >>> 5) & 0x07ff;

                    if (bits == 12) {
                        movePointers(5);
                        current = readBalanceBits(4);
                        entry = MOREBLACKS[current];
                        bits = (entry >>> 1) & 0x07;
                        code = (entry >>> 4) & 0x0fff;

                        invertToBlack(lineOffset, bitOffset, code);
                        bitOffset += code;

                        movePointers(4 - bits);
                    } else if (bits == 15) {
                        movePointers(12);
                        return;
                    } else {
                        invertToBlack(lineOffset, bitOffset, code);
                        bitOffset += code;

                        movePointers(9 - bits);
                        if (isT == 0) {
                            isWhite = true;
                            curItems[changingElemSize++] = bitOffset;
                        }
                    }
                } else if (code == 200) {
                    current = readBalanceBits(2);
                    entry = BLACKS2BIT[current];
                    code = (entry >>> 5) & 0x07ff;
                    bits = (entry >>> 1) & 0x0f;

                    invertToBlack(lineOffset, bitOffset, code);
                    bitOffset += code;

                    movePointers(2 - bits);
                    isWhite = true;
                    curItems[changingElemSize++] = bitOffset;
                } else {
                    invertToBlack(lineOffset, bitOffset, code);
                    bitOffset += code;

                    movePointers(4 - bits);
                    isWhite = true;
                    curItems[changingElemSize++] = bitOffset;
                }
            }

            if (bitOffset == w) {
                if (compression == 2) {
                    if (bitP != 0) {
                        bp++;
                        bitP = 0;
                    }
                }
                break;
            }
        }

        curItems[changingElemSize++] = bitOffset;
    }

    private void invertToBlack(int lineOffset, int bitOffset, int numBits) {
        int bitNum = 8 * lineOffset + bitOffset;
        int lastBit = bitNum + numBits;

        int byteNum = bitNum >> 3;

        int shift = bitNum & 0x7;
        if (shift > 0) {
            int maskVal = 1 << (7 - shift);
            byte val = output[byteNum];
            while (maskVal > 0 && bitNum < lastBit) {
                val |= maskVal;
                maskVal >>= 1;
                ++bitNum;
            }
            output[byteNum] = val;
        }

        byteNum = bitNum >> 3;
        while (bitNum < lastBit - 7) {
            output[byteNum++] = (byte) 255;
            bitNum += 8;
        }

        while (bitNum < lastBit) {
            byteNum = bitNum >> 3;
            output[byteNum] |= 1 << (7 - (bitNum & 0x7));
            ++bitNum;
        }
    }

    private int decodeWhiteCodeWord() {
        int current, entry, bits, isT, twoBits, code;
        int runLength = 0;
        boolean isWhite = true;

        while (isWhite) {
            current = readBits(10);
            entry = WHITERUN[current];

            isT = entry & 0x0001;
            bits = (entry >>> 1) & 0x0f;

            if (bits == 12) {
                twoBits = readBalanceBits(2);
                current = ((current << 2) & 0x000c) | twoBits;
                entry = MOREBLACKS[current];
                bits = (entry >>> 1) & 0x07;
                code = (entry >>> 4) & 0x0fff;
                runLength += code;
                movePointers(4 - bits);
            } else if (bits == 0) {
                throw new RuntimeException("TIFFFaxDecoder0");
            } else if (bits == 15) {
                throw new RuntimeException("TIFFFaxDecoder1");
            } else {
                code = (entry >>> 5) & 0x07ff;
                runLength += code;
                movePointers(10 - bits);
                if (isT == 0) {
                    isWhite = false;
                }
            }
        }

        return runLength;
    }

    private int decodeBlackCodeWord() {
        int current, entry, bits, isT, code;
        int runLength = 0;
        boolean isWhite = false;

        while (!isWhite) {
            current = readBalanceBits(4);
            entry = FIRSTBLACKS[current];

            // Get the 3 fields from the entry
            bits = (entry >>> 1) & 0x000f;
            code = (entry >>> 5) & 0x07ff;

            if (code == 100) {
                current = readBits(9);
                entry = BLACKRUN[current];

                isT = entry & 0x0001;
                bits = (entry >>> 1) & 0x000f;
                code = (entry >>> 5) & 0x07ff;

                if (bits == 12) {
                    movePointers(5);
                    current = readBalanceBits(4);
                    entry = MOREBLACKS[current];
                    bits = (entry >>> 1) & 0x07;
                    code = (entry >>> 4) & 0x0fff;
                    runLength += code;

                    movePointers(4 - bits);
                } else if (bits == 15) {
                    throw new RuntimeException("TIFFFaxDecoder2");
                } else {
                    runLength += code;
                    movePointers(9 - bits);
                    if (isT == 0) {
                        isWhite = true;
                    }
                }
            } else if (code == 200) {
                current = readBalanceBits(2);
                entry = BLACKS2BIT[current];
                code = (entry >>> 5) & 0x07ff;
                runLength += code;
                bits = (entry >>> 1) & 0x0f;
                movePointers(2 - bits);
                isWhite = true;
            } else {
                runLength += code;
                movePointers(4 - bits);
                isWhite = true;
            }
        }

        return runLength;
    }

    private boolean findEndOfLine() {
        int bitIndexMax = input.length * 8 - 1;
        int bitIndex = bp * 8 + bitP;

        while (bitIndex <= bitIndexMax - 12) {
            int next12Bits = readBits(12);
            bitIndex += 12;
            while (next12Bits != 1 && bitIndex < bitIndexMax) {
                next12Bits
                        = ((next12Bits & 0x000007ff) << 1)
                        | (readBalanceBits(1) & 0x00000001);
                bitIndex++;
            }

            if (next12Bits == 1) {
                movePointers(12);
                return true;
            }
        }

        return false;
    }

    private int readEndOfLine(boolean isFirstEOL) {
        if (dim1 == 0) {
            if (!findEndOfLine()) {
                throw new RuntimeException("TIFFFaxDecoder9");
            }
        }

        if (bitsToPut == 0) {
            int next12Bits = readBits(12);
            if (isFirstEOL && next12Bits == 0) {

                if (readBits(4) == 1) {
                    bitsToPut = 1;
                    return 1;
                }
            }
            if (next12Bits != 1) {
                throw new RuntimeException("TIFFFaxDecoder6");
            }
        } else if (bitsToPut == 1) {

            int bitsLeft = 8 - bitP;

            if (readBits(bitsLeft) != 0) {
                throw new RuntimeException("TIFFFaxDecoder8");
            }

            if (bitsLeft < 4) {
                if (readBits(8) != 0) {
                    throw new RuntimeException("TIFFFaxDecoder8");
                }
            }

            //
            int next8 = readBits(8);

            if (isFirstEOL && (next8 & 0xf0) == 0x10) {

                bitsToPut = 0;
                movePointers(4);
            } else {

                while (next8 != 1) {
                    if (next8 != 0) {
                        throw new RuntimeException("TIFFFaxDecoder8");
                    }
                    next8 = readBits(8);
                }
            }
        }

        if (dim1 == 0) {
            return 1;
        } else {
            return readBalanceBits(1);
        }
    }

    private int readBits(int bitsToGet) {
        byte b, next, next2next;
        int l = input.length - 1;
        int bp = this.bp;

        if (endianOrder == 1) {
            b = input[bp];

            if (bp == l) {
                next = 0x00;
                next2next = 0x00;
            } else if ((bp + 1) == l) {
                next = input[bp + 1];
                next2next = 0x00;
            } else {
                next = input[bp + 1];
                next2next = input[bp + 2];
            }
        } else if (endianOrder == 2) {
            b = LITTLEENDIANS[input[bp] & 0xff];

            if (bp == l) {
                next = 0x00;
                next2next = 0x00;
            } else if ((bp + 1) == l) {
                next = LITTLEENDIANS[input[bp + 1] & 0xff];
                next2next = 0x00;
            } else {
                next = LITTLEENDIANS[input[bp + 1] & 0xff];
                next2next = LITTLEENDIANS[input[bp + 2] & 0xff];
            }
        } else {
            throw new RuntimeException("TIFFFaxDecoder7");
        }

        int bitsLeft = 8 - bitP;
        int bitsFromNextByte = bitsToGet - bitsLeft;
        int bitsFromNext2NextByte = 0;
        if (bitsFromNextByte > 8) {
            bitsFromNext2NextByte = bitsFromNextByte - 8;
            bitsFromNextByte = 8;
        }

        this.bp++;

        int i1 = (b & LEFTBITS[bitsLeft]) << (bitsToGet - bitsLeft);
        int i2 = (next & CURBITS[bitsFromNextByte]) >>> (8 - bitsFromNextByte);

        int i3;
        if (bitsFromNext2NextByte != 0) {
            i2 <<= bitsFromNext2NextByte;
            i3 = (next2next & CURBITS[bitsFromNext2NextByte])
                    >>> (8 - bitsFromNext2NextByte);
            i2 |= i3;
            this.bp++;
            bitP = bitsFromNext2NextByte;
        } else {
            if (bitsFromNextByte == 8) {
                bitP = 0;
                this.bp++;
            } else {
                bitP = bitsFromNextByte;
            }
        }

        return i1 | i2;
    }

    private int readBalanceBits(int bitsToGet) {
        byte b, next;
        int l = input.length - 1;
        int bp = this.bp;

        if (endianOrder == 1) {
            b = input[bp];
            if (bp == l) {
                next = 0x00;
            } else {
                next = input[bp + 1];
            }
        } else if (endianOrder == 2) {
            b = LITTLEENDIANS[input[bp] & 0xff];
            if (bp == l) {
                next = 0x00;
            } else {
                next = LITTLEENDIANS[input[bp + 1] & 0xff];
            }
        } else {
            throw new RuntimeException("TIFFFaxDecoder7");
        }

        int bitsLeft = 8 - bitP;
        int bitsFromNextByte = bitsToGet - bitsLeft;

        int shift = bitsLeft - bitsToGet;
        int i1, i2;
        if (shift >= 0) {
            i1 = (b & LEFTBITS[bitsLeft]) >>> shift;
            bitP += bitsToGet;
            if (bitP == 8) {
                bitP = 0;
                this.bp++;
            }
        } else {
            i1 = (b & LEFTBITS[bitsLeft]) << (-shift);
            i2 = (next & CURBITS[bitsFromNextByte]) >>> (8 - bitsFromNextByte);

            i1 |= i2;
            this.bp++;
            bitP = bitsFromNextByte;
        }

        return i1;
    }

    private void movePointers(int reverseBits) {
        if (reverseBits > 8) {
            bp -= reverseBits / 8;
            reverseBits %= 8;
        }
        int i = bitP - reverseBits;
        if (i < 0) {
            bp--;
            bitP = 8 + i;
        } else {
            bitP = i;
        }
    }

    private void updateNextChangeItem(int a0, boolean isWhite, int[] ret) {
        int[] pce = this.prevItems;
        int ces = this.changingElemSize;

        int start = processItem > 0 ? processItem - 1 : 0;
        if (isWhite) {
            start &= ~0x1;
        } else {
            start |= 0x1;
        }

        int i = start;
        for (; i < ces; i += 2) {
            int temp = pce[i];
            if (temp > a0) {
                processItem = i;
                ret[0] = temp;
                break;
            }
        }

        if (i + 1 < ces) {
            ret[1] = pce[i + 1];
        }
    }

}
