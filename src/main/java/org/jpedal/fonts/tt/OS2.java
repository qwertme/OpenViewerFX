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
 * OS2.java
 * ---------------
 */
package org.jpedal.fonts.tt;


public class OS2 extends Table {

    protected int version, xAvgCharWidth, usWeightClass, usWidthClass, fsType, yStrikeoutSize, yStrikeoutPosition, sFamilyClass,
        fsSelection, usFirstCharIndex, usLastCharIndex, sTypoAscender, sTypoDescender, sTypoLineGap, usWinAscent,
        usWinDescent, sxHeight, sCapHeight, usDefaultChar, usBreakChar, usMaxContext;
    protected final int[] subscriptData = new int[4];
    protected final int[] superscriptData = new int[4];
    protected final int[] panose = new int[10];
    protected final int[] ulUnicodeRange = new int[4];
    protected final int[] achVendID = new int[4];
    protected final int[] ulCodePageRange = new int[2];


    public OS2(final FontFile2 font) {
        final int baseOffset = font.getTableStart(FontFile2.OS2);

        final int pointer = font.getPointer();
        font.setPointer(baseOffset);

        version = font.getNextUint16();

        xAvgCharWidth = font.getNextInt16();
        usWeightClass = font.getNextUint16();
        usWidthClass = font.getNextInt16();
        fsType = font.getNextInt16();
        for (int i=0; i<4; i++) {
            subscriptData[i] = font.getNextInt16();
        }
        for (int i=0; i<4; i++) {
            superscriptData[i] = font.getNextInt16();
        }
        yStrikeoutSize = font.getNextInt16();
        yStrikeoutPosition = font.getNextInt16();
        sFamilyClass = font.getNextInt16();
        for (int i=0; i<10; i++) {
            panose[i] = font.getNextint8();
        }
        for (int i=0; i<4; i++) {
            ulUnicodeRange[i] = font.getNextUint32();
        }
        for (int i=0; i<4; i++) {
            achVendID[i] = font.getNextint8();
        }
        fsSelection = font.getNextUint16();
        usFirstCharIndex = font.getNextUint16();
        usLastCharIndex = font.getNextUint16();
        sTypoAscender = font.getNextInt16();
        sTypoDescender = font.getNextInt16();
        sTypoLineGap = font.getNextInt16();
        usWinAscent = font.getNextInt16();
        usWinDescent = font.getNextInt16();

        if (version > 0) {
            for (int i=0; i<2; i++) {
                ulCodePageRange[i] = font.getNextUint32();
            }
        }

        if (version > 1) {
            sxHeight = font.getNextInt16();
            sCapHeight = font.getNextInt16();
            usDefaultChar = font.getNextUint16();
            usBreakChar = font.getNextUint16();
            usMaxContext = font.getNextUint16();
        }

        font.setPointer(pointer);
    }

    public OS2() {

    }

}
