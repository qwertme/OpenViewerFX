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
 * DCT.java
 * ---------------
 */
package com.idrsolutions.image.jpeg;

/**
 *
 */
public class DCT {

    /**
     * Method performs fastest discrete cosine transform conversion with 64
     * length array with integer multiplications
     *
     * @param inp
     * @param code
     */
    public static void FDCTQ(final int[] inp, int code) {

        int x0, x1, x2, x3, x4, x5, x6, x7, x8;

        for (int i = 0; i < 8; i++) {
            int ii = i << 3;
            x0 = inp[ii];
            x1 = inp[ii + 1];
            x2 = inp[ii + 2];
            x3 = inp[ii + 3];
            x4 = inp[ii + 4];
            x5 = inp[ii + 5];
            x6 = inp[ii + 6];
            x7 = inp[ii + 7];

            if ((x0 | x1 | x2 | x3 | x4 | x5 | x6 | x7) == 0) {
                continue;
            }

            x8 = x7 + x0;
            x0 -= x7;
            x7 = x1 + x6;
            x1 -= x6;
            x6 = x2 + x5;
            x2 -= x5;
            x5 = x3 + x4;
            x3 -= x4;

            x4 = x8 + x5;
            x8 -= x5;
            x5 = x7 + x6;
            x7 -= x6;
            x6 = 0x3ec * (x1 + x2);
            x2 = -1204 * x2 + x6;
            x1 = -804 * x1 + x6;
            x6 = 0x353 * (x0 + x3);
            x3 = -1420 * x3 + x6;
            x0 = -282 * x0 + x6;

            x6 = x4 + x5;
            x4 -= x5;
            x5 = 0x22a * (x7 + x8);
            x7 = -1891 * x7 + x5;
            x8 = 783 * x8 + x5;
            x5 = x0 + x2;
            x0 -= x2;
            x2 = x3 + x1;
            x3 -= x1;

            inp[ii] = x6;
            inp[ii + 1] = (x2 + x5) >> 10;
            inp[ii + 2] = x8 >> 10;
            inp[ii + 3] = (x3 * 0xb5) >> 17;
            inp[ii + 4] = x4;
            inp[ii + 5] = (x0 * 0xb5) >> 17;
            inp[ii + 6] = x7 >> 10;
            inp[ii + 7] = (x2 - x5) >> 10;

        }

        for (int i = 0; i < 8; i++) {
            x0 = inp[i];
            x1 = inp[8 + i];
            x2 = inp[16 + i];
            x3 = inp[24 + i];
            x4 = inp[32 + i];
            x5 = inp[40 + i];
            x6 = inp[48 + i];
            x7 = inp[56 + i];

            if ((x0 | x1 | x2 | x3 | x4 | x5 | x6 | x7) == 0) {
                continue;
            }

            x8 = x7 + x0;
            x0 -= x7;
            x7 = x1 + x6;
            x1 -= x6;
            x6 = x2 + x5;
            x2 -= x5;
            x5 = x3 + x4;
            x3 -= x4;

            x4 = x8 + x5;
            x8 -= x5;
            x5 = x7 + x6;
            x7 -= x6;
            x6 = 0x3ec * (x1 + x2);
            x2 = -1204 * x2 + x6;
            x1 = -804 * x1 + x6;
            x6 = 0x353 * (x0 + x3);
            x3 = -1420 * x3 + x6;
            x0 = -282 * x0 + x6;

            x6 = x4 + x5;
            x4 -= x5;
            x5 = 0x22a * (x7 + x8);
            x7 = -1891 * x7 + x5;
            x8 = 783 * x8 + x5;
            x5 = x0 + x2;
            x0 -= x2;
            x2 = x3 + x1;
            x3 -= x1;

            inp[i] = (x6 + 16) >> 3;
            inp[8 + i] = (x2 + x5 + 0x4000) >> 13;
            inp[16 + i] = (x8 + 0x4000) >> 13;
            inp[24 + i] = ((x3 >> 8) * 0xb5 + 0x2000) >> 12;
            inp[32 + i] = (x4 + 16) >> 3;
            inp[40 + i] = ((x0 >> 8) * 0xb5 + 0x2000) >> 12;
            inp[48 + i] = (x7 + 0x4000) >> 13;
            inp[56 + i] = (x2 - x5 + 0x4000) >> 13;
        }

        if (code == 0) {
            for (int i = 0; i < 64; i++) {
                inp[i] = ((inp[i] * JpegLUT.IDLD100[i] + 268433408) >> 12) - 65535;
            }
        } else {
            for (int i = 0; i < 64; i++) {
                inp[i] = ((inp[i] * JpegLUT.IDCD100[i] + 268433408) >> 12) - 65535;
            }
        }

//        old 100% method
//        the lookup table can be generated by dividing 1 by ql and qc table values
//        if (code == 0) {
//            for (int i = 0; i < 64; i++) {
//                inp[i] = (int) ((inp[i] * JpegLUT.DLD100[i]) + 65535.5) - 65535;
//            }
//        } else {
//            for (int i = 0; i < 64; i++) {
//                inp[i] = (int) ((inp[i] * JpegLUT.DCD100[i]) + 65535.5) - 65535;
//            }
//        }
    }

    public static void IDCTQ(Component component, int offset) {

        int[] p = new int[64];
        int[] qt = component.qTable;
        int[] inp = component.codeBlock;
        int v0, v1, v2, v3, v4, v5, v6, v7;
        int p0, p1, p2, p3, p4, p5, p6, p7;
        int t,temp;

        for (int row = 0; row < 64; row += 8) {
            
            temp = offset + row;
            
            p0 = inp[temp];
            p1 = inp[temp + 1];
            p2 = inp[temp + 2];
            p3 = inp[temp + 3];
            p4 = inp[temp + 4];
            p5 = inp[temp + 5];
            p6 = inp[temp + 6];
            p7 = inp[temp + 7];

            
            p0 *= qt[row];
            
            if ((p1 | p2 | p3 | p4 | p5 | p6 | p7) == 0) {
                t = (5793 * p0 + 512) >> 10;
                p[row] = t;
                p[row + 1] = t;
                p[row + 2] = t;
                p[row + 3] = t;
                p[row + 4] = t;
                p[row + 5] = t;
                p[row + 6] = t;
                p[row + 7] = t;
                continue;
            }
           
            p1 *= qt[row + 1];
            p2 *= qt[row + 2];
            p3 *= qt[row + 3];
            p4 *= qt[row + 4];
            p5 *= qt[row + 5];
            p6 *= qt[row + 6];
            p7 *= qt[row + 7];
            
            v0 = (5793 * p0 + 128) >> 8;
            v1 = (5793 * p4 + 128) >> 8;
            v4 = (2896 * (p1 - p7) + 128) >> 8;
            v7 = (2896 * (p1 + p7) + 128) >> 8;
            v5 = p3 << 4;
            v6 = p5 << 4;
            
            v0 = (v0 + v1 + 1) >> 1;
            v1 = v0 - v1;
            t = (p2 * 3784 + p6 * 1567 + 128) >> 8;
            v2 = (p2 * 1567 - p6 * 3784 + 128) >> 8;            
            v4 = (v4 + v6 + 1) >> 1;
            v6 = v4 - v6;
            v7 = (v7 + v5 + 1) >> 1;
            v5 = v7 - v5;
            
            v0 = (v0 + t + 1) >> 1;
            v3 = v0 - t;
            v1 = (v1 + v2 + 1) >> 1;
            v2 = v1 - v2;
            t = (v4 * 2276 + v7 * 3406 + 2048) >> 12;
            v4 = (v4 * 3406 - v7 * 2276 + 2048) >> 12;
            v7 = t;
            t = (v5 * 799 + v6 * 4017 + 2048) >> 12;
            v5 = (v5 * 4017 - v6 * 799 + 2048) >> 12;
            
            p[row] = v0 + v7;
            p[row + 7] = v0 - v7;
            p[row + 1] = v1 + t;
            p[row + 6] = v1 - t;
            p[row + 2] = v2 + v5;
            p[row + 5] = v2 - v5;
            p[row + 3] = v3 + v4;
            p[row + 4] = v3 - v4;
        }

        for (int col = 0; col < 8; ++col) {
            p0 = p[col];
            p1 = p[col + 8];
            p2 = p[col + 16];
            p3 = p[col + 24];
            p4 = p[col + 32];
            p5 = p[col + 40];
            p6 = p[col + 48];
            p7 = p[col + 56];

            if ((p1 | p2 | p3 | p4 | p5 | p6 | p7) == 0) {
                t = (5793 * p0 + 8192) >> 14;
                t = (t < -2040) ? 0 : (t >= 2024) ? 255 : (t + 2056) >> 4;
                inp[offset + col] = t;
                inp[offset + col + 8] = t;
                inp[offset + col + 16] = t;
                inp[offset + col + 24] = t;
                inp[offset + col + 32] = t;
                inp[offset + col + 40] = t;
                inp[offset + col + 48] = t;
                inp[offset + col + 56] = t;
                continue;
            }

            v0 = (5793 * p0 + 2048) >> 12;
            v1 = (5793 * p4 + 2048) >> 12;
            v4 = (2896 * (p1 - p7) + 2048) >> 12;
            v7 = (2896 * (p1 + p7) + 2048) >> 12;
           
            v0 = ((v0 + v1 + 1) >> 1) + 4112;
            v1 = v0 - v1;
            t = (p2 * 3784 + p6 * 1567 + 2048) >> 12;
            v2 = (p2 * 1567 - p6 * 3784 + 2048) >> 12;
            v3 = t;
            v4 = (v4 + p5 + 1) >> 1;
            v6 = v4 - p5;
            v7 = (v7 + p3 + 1) >> 1;
            v5 = v7 - p3;

            v0 = (v0 + v3 + 1) >> 1;
            v3 = v0 - v3;
            v1 = (v1 + v2 + 1) >> 1;
            v2 = v1 - v2;
            t = (v4 * 2276 + v7 * 3406 + 2048) >> 12;
            v4 = (v4 * 3406 - v7 * 2276 + 2048) >> 12;
            v7 = t;
            t = (v5 * 799 + v6 * 4017 + 2048) >> 12;
            v5 = (v5 * 4017 - v6 * 799 + 2048) >> 12;
            
            p0 = v0 + v7;
            p7 = v0 - v7;
            p1 = v1 + t;
            p6 = v1 - t;
            p2 = v2 + v5;
            p5 = v2 - v5;
            p3 = v3 + v4;
            p4 = v3 - v4;

            p0 = (p0 < 16) ? 0 : (p0 >= 4080) ? 255 : p0 >> 4;
            p1 = (p1 < 16) ? 0 : (p1 >= 4080) ? 255 : p1 >> 4;
            p2 = (p2 < 16) ? 0 : (p2 >= 4080) ? 255 : p2 >> 4;
            p3 = (p3 < 16) ? 0 : (p3 >= 4080) ? 255 : p3 >> 4;
            p4 = (p4 < 16) ? 0 : (p4 >= 4080) ? 255 : p4 >> 4;
            p5 = (p5 < 16) ? 0 : (p5 >= 4080) ? 255 : p5 >> 4;
            p6 = (p6 < 16) ? 0 : (p6 >= 4080) ? 255 : p6 >> 4;
            p7 = (p7 < 16) ? 0 : (p7 >= 4080) ? 255 : p7 >> 4;

            temp = offset + col;
            inp[temp] = p0;
            inp[temp + 8] = p1;
            inp[temp + 16] = p2;
            inp[temp + 24] = p3;
            inp[temp + 32] = p4;
            inp[temp + 40] = p5;
            inp[temp + 48] = p6;
            inp[temp + 56] = p7;
        }
    }
    
}
