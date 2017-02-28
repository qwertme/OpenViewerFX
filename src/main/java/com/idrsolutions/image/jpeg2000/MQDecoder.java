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
 * MQDecoder.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 *
 */
public final class MQDecoder {

    /**
     * The data structures containing the probabilities for the LPS
     */
    public static final int QE[] = {
        0x5601, 0x3401, 0x1801, 0x0ac1, 0x0521, 0x0221, 0x5601, 0x5401,
        0x4801, 0x3801, 0x3001, 0x2401, 0x1c01, 0x1601, 0x5601, 0x5401,
        0x5101, 0x4801, 0x3801, 0x3401, 0x3001, 0x2801, 0x2401, 0x2201,
        0x1c01, 0x1801, 0x1601, 0x1401, 0x1201, 0x1101, 0x0ac1, 0x09c1,
        0x08a1, 0x0521, 0x0441, 0x02a1, 0x0221, 0x0141, 0x0111, 0x0085,
        0x0049, 0x0025, 0x0015, 0x0009, 0x0005, 0x0001, 0x5601
    };

    /**
     * The indexes of the next MPS
     */
    public static final byte NMPS[] = {
        1, 2, 3, 4, 5, 38, 7, 8,
        9, 10, 11, 12, 13, 29, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24,
        25, 26, 27, 28, 29, 30, 31, 32,
        33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 45, 46
    };

    /**
     * The indexes of the next LPS
     */
    public static final byte NLPS[] = {
        1, 6, 9, 12, 29, 33, 6, 14,
        14, 14, 17, 18, 20, 21, 14, 14,
        15, 16, 17, 18, 19, 19, 20, 21,
        22, 23, 24, 25, 26, 27, 28, 29,
        30, 31, 32, 33, 34, 35, 36, 37,
        38, 39, 40, 41, 42, 43, 46
    };

    /**
     * Whether LPS and MPS should be switched
     */
    public static final byte[] SWITCHML = {
        1, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0
    };

    //int bpst;
    int bp;
    int c;
    int b;
    int ct;
    int a;
    int[] i;
    int[] mps;
    int d;

    final byte[] data;

    public MQDecoder(byte[] data, int start, int end) {
        this.data = data;        
        bp = start;
        b = data[bp] & 0xff;
        c = b << 16;
        byteIn();
        c <<= 7;
        ct -= 7;
        a = 0x8000;
    }   
   

    public void byteIn() {
        int b1 = data[bp + 1] & 0xff;
        if (b == 0xff) {
            if (b1 > 0x8f) {
                c += 0xff00;
                ct = 8;
            } else {
                bp ++;
                b = b1;//b = buffer.get(bp) & 0xff;
                c += (b << 9);
                ct = 7;
            }
        } else {
            bp ++;
            b = b1;//b = buffer.get(bp) & 0xff;
            c += (b << 8);
            ct = 8;
        }
    }

    public int decode(int cx) {
        int qe = QE[i[cx]];
        a -= qe;
        int chigh = (c>>>16);
        int clow = (c & 0xffff);
        if(chigh < qe){
            d = lpsExchange(cx);
            renormalize();
        }else{
            chigh -= qe;
            c = (chigh << 16) | clow; 
            
            if((a & 0x8000) == 0){
                d = mpsExchange(cx);
                renormalize();
            }else{
                d = mps[cx];
            }
        }
        return d;
    }

    public int mpsExchange(int cx) {
        if (a < QE[i[cx]]) {
            d = 1 - mps[cx];
            if (SWITCHML[i[cx]] == 1) {
                mps[cx] = 1 - mps[cx];
            }
            i[cx] = NLPS[i[cx]];
        } else {
            d = mps[cx];
            i[cx] = NMPS[i[cx]];
        }
        return d;
    }

    public int lpsExchange(int cx) {
        if (a < QE[i[cx]]) {
            a = QE[i[cx]];
            d = mps[cx];
            i[cx] = NMPS[i[cx]];
        } else {
            a = QE[i[cx]];
            d = 1 - mps[cx];
            if (SWITCHML[i[cx]] == 1) {
                mps[cx] = 1 - mps[cx];
            }
            i[cx] = NLPS[i[cx]];
        }
        return d;
    }
    
    public void renormalize(){
        do {
            if (ct == 0) {
                byteIn();
            }
            a <<= 1;
            c <<= 1;
            ct--;
        } while ((a & 0x8000)==0);
    }


}
