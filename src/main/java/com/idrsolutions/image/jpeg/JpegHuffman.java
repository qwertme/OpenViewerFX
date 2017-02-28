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
 * JpegHuffman.java
 * ---------------
 */
package com.idrsolutions.image.jpeg;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Class performs huffman encoding in jpeg compression
 */
public class JpegHuffman {

    private int bitsToPut, bufferToPut;
    private final int DC_matrix0[];
    private final int AC_matrix0[];
    private final int DC_matrix1[];
    private final int AC_matrix1[];

    public final ArrayList<int[]> bitList;

    public final ArrayList<int[]> valList;

    private static final int[] bitsDCchrominance = {1, 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
    private static final int[] bitsDCluminance = {0, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] bitsACchrominance = {17, 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 119};
    private static final int[] bitsACluminance = {16, 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 125};

    private static final int[] valDCchrominance = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    private static final int[] valDCluminance = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    private static final int[] valACchrominance = {
        0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21,
        0x31, 0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71,
        0x13, 0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91,
        0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52, 0xf0,
        0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34,
        0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26,
        0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38,
        0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48,
        0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58,
        0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
        0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78,
        0x79, 0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87,
        0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96,
        0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5,
        0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4,
        0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3,
        0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2,
        0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda,
        0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9,
        0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
        0xf9, 0xfa};

    private static final int[] valACluminance = {
        0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12,
        0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07,
        0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08,
        0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0,
        0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16,
        0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28,
        0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
        0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49,
        0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59,
        0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69,
        0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79,
        0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89,
        0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98,
        0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7,
        0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6,
        0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5,
        0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4,
        0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2,
        0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea,
        0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
        0xf9, 0xfa};

    private final int AC0_0;
    private final int AC0_1;
    private final int AC0_480;
    private final int AC0_481;

    private final int AC1_0;
    private final int AC1_1;
    private final int AC1_480;
    private final int AC1_481;

    /**
     *
     */
    public JpegHuffman() {
        bitList = new ArrayList<int[]>();
        bitList.add(bitsDCluminance);
        bitList.add(bitsACluminance);
        bitList.add(bitsDCchrominance);
        bitList.add(bitsACchrominance);
        valList = new ArrayList<int[]>();
        valList.add(valDCluminance);
        valList.add(valACluminance);
        valList.add(valDCchrominance);
        valList.add(valACchrominance);

        DC_matrix0 = new int[24];
        DC_matrix1 = new int[24];
        AC_matrix0 = new int[510];
        AC_matrix1 = new int[510];

        int lastp, si, code;
        int[] huffsize = new int[257];
        int[] huffcode = new int[257];

        int p = 0;
        for (int l = 1; l <= 16; l++) {
            for (int i = 1; i <= bitsDCchrominance[l]; i++) {
                huffsize[p++] = l;
            }
        }
        huffsize[p] = 0;
        lastp = p;

        code = 0;
        si = huffsize[0];
        p = 0;
        while (huffsize[p] != 0) {
            while (huffsize[p] == si) {
                huffcode[p++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        int dp = 0;
        for (p = 0; p < lastp; p++) {
            DC_matrix1[dp++] = huffcode[p];
            DC_matrix1[dp++] = huffsize[p];
        }

        p = 0;
        for (int l = 1; l <= 16; l++) {
            for (int i = 1; i <= bitsACchrominance[l]; i++) {
                huffsize[p++] = l;
            }
        }
        huffsize[p] = 0;
        lastp = p;

        code = 0;
        si = huffsize[0];
        p = 0;
        while (huffsize[p] != 0) {
            while (huffsize[p] == si) {
                huffcode[p++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        for (p = 0; p < lastp; p++) {
            dp = valACchrominance[p] << 1;
            AC_matrix1[dp] = huffcode[p];
            AC_matrix1[dp + 1] = huffsize[p];
        }

        p = 0;
        for (int l = 1; l <= 16; l++) {
            for (int i = 1; i <= bitsDCluminance[l]; i++) {
                huffsize[p++] = l;
            }
        }
        huffsize[p] = 0;
        lastp = p;

        code = 0;
        si = huffsize[0];
        p = 0;
        while (huffsize[p] != 0) {
            while (huffsize[p] == si) {
                huffcode[p++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        dp = 0;
        for (p = 0; p < lastp; p++) {
            DC_matrix0[dp++] = huffcode[p];
            DC_matrix0[dp++] = huffsize[p];
        }

        p = 0;
        for (int l = 1; l <= 16; l++) {
            for (int i = 1; i <= bitsACluminance[l]; i++) {
                huffsize[p++] = l;
            }
        }
        huffsize[p] = 0;
        lastp = p;

        code = 0;
        si = huffsize[0];
        p = 0;
        while (huffsize[p] != 0) {
            while (huffsize[p] == si) {
                huffcode[p++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }
        for (int q = 0; q < lastp; q++) {
            dp = valACluminance[q] << 1;
            AC_matrix0[dp] = huffcode[q];
            AC_matrix0[dp + 1] = huffsize[q];
        }

        AC0_0 = AC_matrix0[0];
        AC0_1 = AC_matrix0[1];
        AC0_480 = AC_matrix0[480];
        AC0_481 = AC_matrix0[481];
        
        AC1_0 = AC_matrix1[0];
        AC1_1 = AC_matrix1[1];
        AC1_480 = AC_matrix1[480];
        AC1_481 = AC_matrix1[481];

    }

    /**
     *
     * @param outStream
     * @param zigzag
     * @param previous
     * @param code
     * @throws IOException
     */
    public void encodeBlock(OutputStream outStream, int zigzag[], int previous, int code) throws IOException {
        int temp, temp2, nbits, mp;

        if (code == 0) {
            temp = temp2 = zigzag[0] - previous;
            if (temp < 0) {
                temp = -temp;
                temp2--;
            }
            nbits = 0;
            while (temp != 0) {
                nbits++;
                temp >>= 1;
            }
            mp = nbits << 1;
            accumulate(outStream, DC_matrix0[mp], DC_matrix0[mp + 1]);

            if (nbits != 0) {
                accumulate(outStream, temp2, nbits);
            }

            int r = 0;

            for (int k = 1; k < 64; k++) {
                if ((temp = zigzag[JpegLUT.ZIGZAGORDER[k]]) == 0) {
                    r++;
                } else {
                    while (r > 15) {
                        accumulate(outStream, AC0_480, AC0_481);
                        r -= 16;
                    }
                    temp2 = temp;
                    if (temp < 0) {
                        temp = -temp;
                        temp2--;
                    }
                    nbits = 1;
                    while ((temp >>= 1) != 0) {
                        nbits++;
                    }
                    mp = ((r << 4) + nbits) << 1;
                    accumulate(outStream, AC_matrix0[mp], AC_matrix0[mp + 1]);
                    accumulate(outStream, temp2, nbits);
                    r = 0;
                }
            }
            if (r > 0) {
                accumulate(outStream, AC0_0, AC0_1);
            }
        } else {
            temp = temp2 = zigzag[0] - previous;
            if (temp < 0) {
                temp = -temp;
                temp2--;
            }
            nbits = 0;
            while (temp != 0) {
                nbits++;
                temp >>= 1;
            }
            mp = nbits << 1;
            accumulate(outStream, DC_matrix1[mp], DC_matrix1[mp + 1]);

            if (nbits != 0) {
                accumulate(outStream, temp2, nbits);
            }

            int r = 0;

            for (int k = 1; k < 64; k++) {
                if ((temp = zigzag[JpegLUT.ZIGZAGORDER[k]]) == 0) {
                    r++;
                } else {
                    while (r > 15) {
                        accumulate(outStream, AC1_480, AC1_481);
                        r -= 16;
                    }
                    temp2 = temp;
                    if (temp < 0) {
                        temp = -temp;
                        temp2--;
                    }
                    nbits = 1;
                    while ((temp >>= 1) != 0) {
                        nbits++;
                    }
                    mp = ((r << 4) + nbits) << 1;
                    accumulate(outStream, AC_matrix1[mp], AC_matrix1[mp + 1]);
                    accumulate(outStream, temp2, nbits);
                    r = 0;
                }
            }
            if (r > 0) {
                accumulate(outStream, AC1_0, AC1_1);
            }
        }

    }

    private void accumulate(OutputStream outStream, int code, int size) throws IOException {
        int PutBuffer = code;
        int PutBits = bitsToPut;

        PutBuffer &= (1 << size) - 1;
        PutBits += size;
        PutBuffer <<= 24 - PutBits;
        PutBuffer |= bufferToPut;

        int c;

        while (PutBits >= 8) {
            c = ((PutBuffer >> 16) & 0xFF);
            outStream.write(c);
            if (c == 0xFF) {
                outStream.write(0);
            }
            PutBuffer <<= 8;
            PutBits -= 8;
        }
        bufferToPut = PutBuffer;
        bitsToPut = PutBits;

    }

    /**
     *
     * @param outStream
     * @throws IOException
     */
    public void end(OutputStream outStream) throws IOException {
        int PutBuffer = bufferToPut;
        int PutBits = bitsToPut;
        while (PutBits >= 8) {
            int c = ((PutBuffer >> 16) & 0xFF);
            outStream.write(c);
            if (c == 0xFF) {
                outStream.write(0);
            }
            PutBuffer <<= 8;
            PutBits -= 8;
        }
        if (PutBits > 0) {
            int c = ((PutBuffer >> 16) & 0xFF);
            outStream.write(c);
        }
    }

    public static Object[] generateHuffmanTable(int[] codeLengths, int[] symbols) {
        int k = 0, i, j, length = 16;
        Stack<IndexMap> code = new Stack<IndexMap>();

        while (length > 0 && codeLengths[length - 1] == 0) {
            length--;
        }

        IndexMap p = new IndexMap(0, new Object[2]);
        code.push(p);

        IndexMap q;
        for (i = 0; i < length; i++) {
            int cc = codeLengths[i];
            for (j = 0; j < cc; j++) {
                p = code.pop();
                p.children[p.index] = symbols[k];
                while (p.index > 0) {
                    p = code.pop();
                }
                p.index++;
                code.push(p);
                while (code.size() <= i) {
                    q = new IndexMap(0, new Object[2]);
                    code.push(q);
                    p.children[p.index] = q.children;
                    p = q;
                }
                k++;
            }
            if (i + 1 < length) {
                q = new IndexMap(0, new Object[2]);
                code.push(q);
                p.children[p.index] = q.children;
                p = q;
            }
        }
        return code.elementAt(0).children;
    }

}
