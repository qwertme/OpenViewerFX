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
 * EntropyDecoder.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 *
 */
public class EntropyDecoder {

    private final byte[] stream;
    private final int maxLen;
    private final int dataEnd;
    private int bp;
    private int ch;
    private int cl;
    private int ct;
    private int a;

    public EntropyDecoder(byte[] stream, int offset, int len) {
        this.stream = stream;
        this.maxLen = stream.length;
        bp = offset;
        dataEnd = len;
        ch = stream[offset] & 0xff;
        cl = 0;

        byteIn();

        ch = ((ch << 7) & 0xFFFF) | ((cl >> 9) & 0x7F);
        cl = (cl << 7) & 0xFFFF;
        ct -= 7;
        this.a = 0x8000;
    }

    private void byteIn() {
        if (bp<maxLen && (stream[bp] & 0xff) == 0xFF) {
            final int b1 = stream[bp + 1] & 0xff;
            if (b1 > 0x8F) {
                cl += 0xFF00;
                ct = 8;
            } else {
                bp++;
                cl += ((stream[bp] & 0xff) << 9);
                ct = 7;
            }
        } else {
            bp++;
            cl += (bp < dataEnd ? ((stream[bp] & 0xff) << 8) : 0xFF00);
            ct = 8;
        }
        if (cl > 0xFFFF) {
            ch += (cl >> 16);
            cl &= 0xFFFF;
        }
    }

    public int decodeBit(byte[] contexts, int pos) {

        int cx_idx = contexts[pos] >> 1;
        int cx_mps = contexts[pos] & 1;
        
        final int qe_ = LUT.QE[cx_idx];

        int d;
        a -= qe_;
        
        if (ch < qe_) {

            if (a < qe_) {
                a = qe_;
                d = cx_mps;
                cx_idx = LUT.NMPS[cx_idx];
            } else {
                a = qe_;
                d = 1 ^ cx_mps;
                if (LUT.SWITCHML[cx_idx] == 1) {
                    cx_mps = d;
                }
                cx_idx = LUT.NLPS[cx_idx];
            }
        } else {
            ch -= qe_;
            if ((a & 0x8000) != 0) {
                return cx_mps;
            }

            if (a < qe_) {
                d = 1 ^ cx_mps;
                if (LUT.SWITCHML[cx_idx] == 1) {
                    cx_mps = d;
                }
                cx_idx = LUT.NLPS[cx_idx];
            } else {
                d = cx_mps;
                cx_idx = LUT.NMPS[cx_idx];
            }
        }
        do {
            if (ct == 0) {
                byteIn();
            }
            a <<= 1;
            ch = ((ch << 1) & 0xFFFF) | ((cl >> 15) & 1);
            cl = (cl << 1) & 0xFFFF;
            ct--;
        } while ((a & 0x8000) == 0);

        contexts[pos] = (byte) (cx_idx << 1 | cx_mps);
        return d;
    }
}
