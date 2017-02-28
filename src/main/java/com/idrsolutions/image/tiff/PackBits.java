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
 * PackBits.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PackBits {

    public static byte[] decompress(byte[] input, int expected) throws IOException {
        int total = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length * 2);
        int i = 0;
        while (total < expected) {
            if (i >= input.length) {
                throw new IOException("Error in packbit decompression ");
            }
            int n = input[i++];
            if ((n >= 0) && (n <= 127)) {
                int cc = n + 1;
                total += cc;
                for (int j = 0; j < cc; j++) {
                    bos.write(input[i++]);
                }
            } else if ((n >= -127) && (n <= -1)) {
                int b = input[i++];
                int count = -n + 1;

                total += count;
                for (int j = 0; j < count; j++) {
                    bos.write(b);
                }
            } else if (n == -128) {
                throw new IOException("Error in packbit decompression ");
            }
        }
        return bos.toByteArray();
    }

    public static byte[] compress(byte[] data, int imageW, int imageH, int nComp) throws IOException {
        int rowByteCount = imageW * nComp;
        int offset = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < imageH; i++) {
            byte temp[] = new byte[rowByteCount];
            System.arraycopy(data, offset, temp, 0, rowByteCount);
            offset += rowByteCount;
            bos.write(compressRow(temp));
        }
        bos.close();
        return bos.toByteArray();
    }

    private static byte[] compressRow(byte bytes[]) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int pos = 0;
        while (pos < bytes.length) {
            int dup = getNextDup(bytes, pos);
            if (dup == pos) {
                int len = getNextRun(bytes, dup);
                int dataLen = Math.min(len, 128);
                bos.write(-(dataLen - 1));
                bos.write(bytes[pos]);
                pos += dataLen;
            } else {
                int len = dup - pos;

                if (dup > 0) {
                    int runlen = getNextRun(bytes, dup);
                    if (runlen < 3) {
                        int nextptr = pos + len + runlen;
                        int nextdup = getNextDup(bytes, nextptr);
                        if (nextdup != nextptr) {
                            dup = nextdup;
                            len = dup - pos;
                        }
                    }
                }

                if (dup < 0) {
                    len = bytes.length - pos;
                }
                int dataLen = Math.min(len, 128);
                bos.write(dataLen - 1);
                for (int i = 0; i < dataLen; i++) {
                    bos.write(bytes[pos]);
                    pos++;
                }
            }
        }
        bos.close();
        return bos.toByteArray();
    }

    private static int getNextDup(byte bytes[], int offset) {
        if (offset >= bytes.length) {
            return -1;
        }
        byte temp = bytes[offset];

        for (int i = offset + 1; i < bytes.length; i++) {
            byte b = bytes[i];

            if (b == temp) {
                return i - 1;
            }

            temp = b;
        }

        return -1;
    }

    private static int getNextRun(byte bytes[], int offset) {
        byte b = bytes[offset];
        int c = 0;
        int len = bytes.length;
        for (int i = offset + 1; (i < len) && (bytes[i] == b); i++) {
            c = i;
        }

        return c - offset;
    }

}
