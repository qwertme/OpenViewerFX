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
 * JpegScanner.java
 * ---------------
 */
package com.idrsolutions.image.jpeg;

import java.util.List;

/**
 *
 */
public class JpegScanner {

    private static final int DCFirst = 0;
    private static final int DCSuccessive = 1;
    private static final int ACFirst = 2;
    private static final int ACSuccessive = 3;
    private static final int Baseline = 4;

    private final byte[] data;

    private int bitPos;
    private int bitBuffer;
    private int eobrun;
    private int mcusX;
    private int offset;
    private int successive, sStart, sEnd;
    private int stateAC, stateNextAC;

    public JpegScanner(byte[] data) {
        this.data = data;
    }

    public int decodeScan(int off, Frame frame, List<Component> components, int resetInterval, int sStart, int sEnd, int sPrev, int successive) {
        mcusX = frame.mcusX;
        this.offset = off;
        this.successive = successive;
        this.sStart = sStart;
        this.sEnd = sEnd;

        int componentsLength = components.size();
        Component component;
        int i, j, k, n;
        int decodeFn;
        if (frame.progressive) {
            if (sStart == 0) {
                decodeFn = sPrev == 0 ? DCFirst : DCSuccessive;
            } else {
                decodeFn = sPrev == 0 ? ACFirst : ACSuccessive;
            }
        } else {
            decodeFn = Baseline;
        }

        int mcu = 0, marker;
        int mcuExpected;
        if (componentsLength == 1) {
            mcuExpected = components.get(0).blocksX * components.get(0).blocksY;
        } else {
            mcuExpected = mcusX * frame.mcusY;
        }
        
        int nc = resetInterval==0? mcuExpected : resetInterval;

        int h, v;
        while (mcu < mcuExpected) {
            // reset interval stuff
            for (i = 0; i < componentsLength; i++) {
                components.get(i).pred = 0;
            }
            eobrun = 0;

            if (componentsLength == 1) {
                component = components.get(0);
                for (n = 0; n < nc; n++) {
                    decodeBlock(component, decodeFn, mcu);
                    mcu++;
                }
            } else {
                for (n = 0; n < nc; n++) {
                    for (i = 0; i < componentsLength; i++) {
                        component = components.get(i);
                        h = component.h;
                        v = component.v;
                        for (j = 0; j < v; j++) {
                            for (k = 0; k < h; k++) {
                                decodeMcu(component, decodeFn, mcu, j, k);
                            }
                        }
                    }
                    mcu++;
                }
            }

            bitPos = 0;
            marker = ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
            if (marker <= 0xFF00) {

            }

            if (marker >= 0xFFD0 && marker <= 0xFFD7) { // RSTx
                offset += 2;
            } else {
                break;
            }
        }

        return offset - off;

    }

    private int findHuffmanValue(Object tree) {
        Object node = tree;
        while (true) {
            node = ((Object[]) node)[readBit()];
            if (node instanceof Integer) {
                return (Integer) node;
            }
        }
    }

    private int readBit() {
        if (bitPos > 0) {
            bitPos--;
            return (bitBuffer >> bitPos) & 1;
        }
        bitBuffer = data[offset++] & 0xff;
        if (bitBuffer == 0xFF) {
            int nextByte = data[offset++] & 0xff;
            if (nextByte != 0) {
                System.err.println("invalid marker found");
            }
        }
        bitPos = 7;
        return bitBuffer >>> 7;
    }

    private int getNext(int length) {
        int n = 0;
        while (length > 0) {
            n = (n << 1) | readBit();
            length--;
        }
        return n;
    }

    private int getNextFull(int length) {
        if (length == 1) {
            return readBit() == 1 ? 1 : -1;
        }
        int n = getNext(length);
        if (n >= 1 << (length - 1)) {
            return n;
        }
        return n + (-1 << length) + 1;
    }

    private void decodeMcu(Component component, int decodeFn, int mcu, int row, int col) {
        int mcuRow = mcu / mcusX;
        int mcuCol = mcu % mcusX;
        int blockRow = mcuRow * component.v + row;
        int blockCol = mcuCol * component.h + col;
        int offsetD = getCodeBlockOffset(component, blockRow, blockCol);
        decodeOrdering(component, offsetD, decodeFn);
    }

    private void decodeBlock(Component component, int decodeFn, int mcu) {
        int blockRow = mcu / component.blocksX;
        int blockCol = mcu % component.blocksX;
        int offsetD = getCodeBlockOffset(component, blockRow, blockCol);
        decodeOrdering(component, offsetD, decodeFn);
    }

    private void decodeOrdering(Component component, int offset, int decodeFn) {
        int a, b, c, d, e, f, diff, z;
        switch (decodeFn) {
            case DCFirst:
                e = findHuffmanValue(component.huffmanTableDC);
                diff = e == 0 ? 0 : (getNextFull(e) << successive);
                component.codeBlock[offset] = (component.pred += diff);
                break;
            case DCSuccessive:
                component.codeBlock[offset] |= (readBit() << successive);
                break;
            case ACFirst:
                if (eobrun > 0) {
                    eobrun--;
                    return;
                }
                a = sStart;
                b = sEnd;
                while (a <= b) {
                    f = findHuffmanValue(component.huffmanTableAC);
                    d = f & 15;
                    c = f >> 4;
                    if (d == 0) {
                        if (c < 15) {
                            eobrun = getNext(c) + (1 << c) - 1;
                            break;
                        }
                        a += 16;
                        continue;
                    }
                    a += c;
                    z = JpegLUT.ZIGZAGORDER[a];
                    component.codeBlock[offset + z]
                            = getNextFull(d) * (1 << successive);
                    a++;
                }
                break;
            case ACSuccessive:
                a = sStart;
                b = sEnd;
                c = 0;
                while (a <= b) {
                    z = JpegLUT.ZIGZAGORDER[a];
                    switch (stateAC) {
                        case 0: // initial state
                            f = findHuffmanValue(component.huffmanTableAC);
                            d = f & 15;
                            c = f >> 4;
                            if (d == 0) {
                                if (c < 15) {
                                    eobrun = getNext(c) + (1 << c);
                                    stateAC = 4;
                                } else {
                                    c = 16;
                                    stateAC = 1;
                                }
                            } else {
                                if (d != 1) {

                                }
                                stateNextAC = getNextFull(d);
                                stateAC = c != 0 ? 2 : 3;
                            }
                            continue;
                        case 1: // skipping r zero items
                        case 2:
                            if (component.codeBlock[offset + z] != 0) {
                                component.codeBlock[offset + z] += (readBit() << successive);
                            } else {
                                c--;
                                if (c == 0) {
                                    stateAC = stateAC == 2 ? 3 : 0;
                                }
                            }
                            break;
                        case 3: // set value for a zero item
                            if (component.codeBlock[offset + z] != 0) {
                                component.codeBlock[offset + z] += (readBit() << successive);
                            } else {
                                component.codeBlock[offset + z]
                                        = stateNextAC << successive;
                                stateAC = 0;
                            }
                            break;
                        case 4: // eob
                            if (component.codeBlock[offset + z] != 0) {
                                component.codeBlock[offset + z] += (readBit() << successive);
                            }
                            break;
                    }
                    a++;
                }
                if (stateAC == 4) {
                    eobrun--;
                    if (eobrun == 0) {
                        stateAC = 0;
                    }
                }
                break;
            case Baseline:
                e = findHuffmanValue(component.huffmanTableDC);
                diff = e == 0 ? 0 : getNextFull(e);
                component.codeBlock[offset] = (component.pred += diff);
                a = 1;
                while (a < 64) {
                    f = findHuffmanValue(component.huffmanTableAC);
                    d = f & 15;
                    c = f >> 4;
                    if (d == 0) {
                        if (c < 15) {
                            break;
                        }
                        a += 16;
                        continue;
                    }
                    a += c;
                    z = JpegLUT.ZIGZAGORDER[a];
                    component.codeBlock[offset + z] = getNextFull(d);
                    a++;
                }
                break;
        }
    }

    public static int getCodeBlockOffset(Component component, int row, int col) {
        return 64 * ((component.blocksX + 1) * row + col);
    }

}
