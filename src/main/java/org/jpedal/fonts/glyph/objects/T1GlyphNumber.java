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
 * T1GlyphNumber.java
 * ---------------
 */
package org.jpedal.fonts.glyph.objects;

import org.jpedal.fonts.objects.FontData;

public class T1GlyphNumber {

    private static final String nybChars = "0123456789.ee -";


    /**
     * Utility method used during processing of type1C files
     */
    public static final int getNumber(final byte[] fontDataAsArray, int pos, final double[] values, final int valuePointer, final boolean is1C) {

        final int b0;
        int i;
        double x = 0;

        b0 = fontDataAsArray[pos] & 0xFF;

        if (b0 < 28 || b0 == 31) { //error!
            System.err.println("!!!!Incorrect type1C operand");
        } else if (b0 == 28) { //2 byte number in range -32768
            // +32767
            x = (fontDataAsArray[pos + 1] << 8) + (fontDataAsArray[pos + 2] & 0xff);
            pos += 3;
        } else if (b0 == 255) {

            if (is1C) {
                x = ((fontDataAsArray[pos + 1] & 0xFF) << 8) + (fontDataAsArray[pos + 2] & 0xFF);
                x += (((fontDataAsArray[pos + 3] & 0xFF) << 8) + (fontDataAsArray[pos + 4] & 0xFF)) / 65536.0;
                if (fontDataAsArray[pos + 1] < 0) {
                    x -= 65536;
                }


            } else {
                //x=((content[pos + 1]& 127) << 24) + (content[pos + 2]<<16)+(content[pos + 3] << 8) + content[pos + 4];
                x =
                    ((fontDataAsArray[pos + 1] & 0xFF) << 24)
                        + ((fontDataAsArray[pos + 2] & 0xFF) << 16)
                        + ((fontDataAsArray[pos + 3] & 0xFF) << 8)
                        + (fontDataAsArray[pos + 4] & 0xFF);

            }

            pos += 5;
        } else if (b0 == 29) { //4 byte signed number
            x =
                ((fontDataAsArray[pos + 1] & 0xFF) << 24)
                    + ((fontDataAsArray[pos + 2] & 0xFF) << 16)
                    + ((fontDataAsArray[pos + 3] & 0xFF) << 8)
                    + (fontDataAsArray[pos + 4] & 0xFF);
            pos += 5;
        } else if (b0 == 30) { //BCD values

            final char[] buf = new char[65];
            pos += 1;
            i = 0;
            while (i < 64) {
                final int b = fontDataAsArray[pos++] & 0xFF;

                final int nyb0 = (b >> 4) & 0x0f;
                final int nyb1 = b & 0x0f;

                if (nyb0 == 0xf) {
                    break;
                }
                buf[i++] = nybChars.charAt(nyb0);
                if (i == 64) {
                    break;
                }
                if (nyb0 == 0xc) {
                    buf[i++] = '-';
                }
                if (i == 64) {
                    break;
                }
                if (nyb1 == 0xf) {
                    break;
                }
                buf[i++] = nybChars.charAt(nyb1);
                if (i == 64) {
                    break;
                }
                if (nyb1 == 0xc) {
                    buf[i++] = '-';
                }
            }
            x = Double.valueOf(new String(buf, 0, i));

        } else if (b0 < 247) { //-107 +107
            x = b0 - 139;
            pos++;
        } else if (b0 < 251) { //2 bytes +108 +1131
            x = ((b0 - 247) << 8) + (fontDataAsArray[pos + 1] & 0xff) + 108;
            pos += 2;
        } else { //-1131 -108
            x = -((b0 - 251) << 8) - (fontDataAsArray[pos + 1] & 0xff) - 108;
            pos += 2;
        }

        //assign number
        values[valuePointer] = x;

        return pos;
    }

    /**
     * Utility method used during processing of type1C files
     */
    public static final int getNumber(final FontData fontDataAsObject, int pos, final double[] values, final int valuePointer, final boolean is1C) {

        final int b0;
        int i;
        double x = 0;

        b0 = fontDataAsObject.getByte(pos) & 0xFF;

        if ((b0 < 28 || b0 == 31)) { //error!
            System.err.println("!!!!Incorrect type1C operand");
        } else if (b0 == 28) { //2 byte number in range -32768
            // +32767
            x = (fontDataAsObject.getByte(pos + 1) << 8) + (fontDataAsObject.getByte(pos + 2) & 0xff);
            pos += 3;
        } else if (b0 == 255) {

            if (is1C) {
                int top = ((fontDataAsObject.getByte(pos + 1) & 0xFF) << 8) + (fontDataAsObject.getByte(pos + 2) & 0xFF);
                if (top > 32768) {
                    top = 65536 - top;
                }
                final double numb = top;
                final double dec = ((fontDataAsObject.getByte(pos + 3) & 0xFF) << 8) + (fontDataAsObject.getByte(pos + 4) & 0xFF);
                x = numb + (dec / 65536);
                if (fontDataAsObject.getByte(pos + 1) < 0) {
                    x = -x;

                }
            } else {
                //x=((content[pos + 1]& 127) << 24) + (content[pos + 2]<<16)+(content[pos + 3] << 8) + content[pos + 4];
                x =
                    ((fontDataAsObject.getByte(pos + 1) & 0xFF) << 24)
                        + ((fontDataAsObject.getByte(pos + 2) & 0xFF) << 16)
                        + ((fontDataAsObject.getByte(pos + 3) & 0xFF) << 8)
                        + (fontDataAsObject.getByte(pos + 4) & 0xFF);

            }

            pos += 5;
        } else if (b0 == 29) { //4 byte signed number
            x =
                ((fontDataAsObject.getByte(pos + 1) & 0xFF) << 24)
                    + ((fontDataAsObject.getByte(pos + 2) & 0xFF) << 16)
                    + ((fontDataAsObject.getByte(pos + 3) & 0xFF) << 8)
                    + (fontDataAsObject.getByte(pos + 4) & 0xFF);
            pos += 5;
        } else if (b0 == 30) { //BCD values

            final char[] buf = new char[65];
            pos += 1;
            i = 0;
            while (i < 64) {
                final int b = fontDataAsObject.getByte(pos++) & 0xFF;

                final int nyb0 = (b >> 4) & 0x0f;
                final int nyb1 = b & 0x0f;

                if (nyb0 == 0xf) {
                    break;
                }
                buf[i++] = nybChars.charAt(nyb0);
                if (i == 64) {
                    break;
                }
                if (nyb0 == 0xc) {
                    buf[i++] = '-';
                }
                if (i == 64) {
                    break;
                }
                if (nyb1 == 0xf) {
                    break;
                }
                buf[i++] = nybChars.charAt(nyb1);
                if (i == 64) {
                    break;
                }
                if (nyb1 == 0xc) {
                    buf[i++] = '-';
                }
            }
            x = Double.valueOf(new String(buf, 0, i));

        } else if (b0 < 247) { //-107 +107
            x = b0 - 139;
            pos++;
        } else if (b0 < 251) { //2 bytes +108 +1131
            x = ((b0 - 247) << 8) + (fontDataAsObject.getByte(pos + 1) & 0xff) + 108;
            pos += 2;
        } else { //-1131 -108
            x = -((b0 - 251) << 8) - (fontDataAsObject.getByte(pos + 1) & 0xff) - 108;
            pos += 2;
        }

        //assign number
        values[valuePointer] = x;

        return pos;
    }

}
