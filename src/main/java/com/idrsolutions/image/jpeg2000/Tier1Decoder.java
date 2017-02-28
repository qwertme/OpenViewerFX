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
 * Tier1Decoder.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 *
 */
public class Tier1Decoder {

    public static final byte BYTEMB = 0;
    public static final byte SHORTMB = 1;
    public static final byte INTMB = 2;
    
    private static final byte UNIFORM = 17;
    private static final byte RUNLENGTH = 18;

    private final int width;
    private final int height;
    private EntropyDecoder decoder;
    private final byte[] contextLableTable;

    public byte[] neighborSigns;
    public byte[] coefficientsSign;
    public Object magnitude;
    public byte[] currentFlag;
    public byte[] bitsDecoded;
    public byte[] cx;

    public final int mbType;


    public Tier1Decoder(int width, int height, TileBand subband, int zeroBitPlanes, int mb) {
        this.width = width;
        this.height = height;
        this.contextLableTable = (subband.type == TileBand.HH ? LUT.ContextHH
                : (subband.type == TileBand.HL ? LUT.ContextHL : LUT.ContextLL));
        int cc = width * height;
        neighborSigns = new byte[cc];
        coefficientsSign = new byte[cc];

        mbType = mb > 14 ? INTMB : (mb > 6 ? SHORTMB : BYTEMB);
        magnitude = mb > 14 ? new int[cc] : (mb > 6 ? new short[cc] : new byte[cc]);

        currentFlag = new byte[cc];
        bitsDecoded = new byte[cc];
        if (zeroBitPlanes != 0) {
            for (int i = 0; i < cc; i++) {
                bitsDecoded[i] = (byte) zeroBitPlanes;
            }
        }

        cx = new byte[19];
        cx[0] = 4 << 1;
        cx[UNIFORM] = 46 << 1;
        cx[RUNLENGTH] = 3 << 1;
    }

    public void setDecoder(EntropyDecoder decoder) {
        this.decoder = decoder;
    }

    public void setNeighborSigns(int y, int x, int index) {
        boolean left = x > 0;
        boolean right = (x + 1) < width;
        if (y > 0) {
            int i = index - width;
            if (left) {
                neighborSigns[i - 1] = (byte) ((neighborSigns[i - 1] & 0xff) + 0x10);
            }
            if (right) {
                neighborSigns[i + 1] = (byte) ((neighborSigns[i + 1] & 0xff) + 0x10);
            }
            neighborSigns[i] = (byte) ((neighborSigns[i] & 0xff) + 0x04);
        }
        if ((y + 1) < height) {
            int i = index + width;
            if (left) {
                neighborSigns[i - 1] = (byte) ((neighborSigns[i - 1] & 0xff) + 0x10);
            }
            if (right) {
                neighborSigns[i + 1] = (byte) ((neighborSigns[i + 1] & 0xff) + 0x10);
            }
            neighborSigns[i] = (byte) ((neighborSigns[i] & 0xff) + 0x04);
        }

        if (left) {
            neighborSigns[index - 1] = (byte) ((neighborSigns[index - 1] & 0xff) + 0x01);
        }
        if (right) {
            neighborSigns[index + 1] = (byte) ((neighborSigns[index + 1] & 0xff) + 0x01);
        }
        neighborSigns[index] = (byte) ((neighborSigns[index] & 0xff) | 0x80);
    }

    public void runSPP() {

        int processedInverseMask = ~1;
        int processedMask = 1;
        int firstMagnitudeBitMask = 2;

        for (int i0 = 0; i0 < height; i0 += 4) {
            for (int j = 0; j < width; j++) {
                int index = i0 * width + j;
                for (int i1 = 0; i1 < 4; i1++, index += width) {
                    int i = i0 + i1;
                    if (i >= height) {
                        break;
                    }
                    
                    currentFlag[index] = (byte) ((currentFlag[index] & 0xff) & processedInverseMask);

                    switch (mbType) {
                        case BYTEMB:
                            if (((byte[]) magnitude)[index] != 0 || neighborSigns[index] == 0) {
                                continue;
                            }

                            break;
                        case SHORTMB:
                            if (((short[]) magnitude)[index] != 0 || neighborSigns[index] == 0) {
                                continue;
                            }

                            break;
                        case INTMB:
                            if (((int[]) magnitude)[index] != 0 || neighborSigns[index] == 0) {
                                continue;
                            }
                            break;
                    }

                    int contextLabel = contextLableTable[(neighborSigns[index] & 0xff)];

                    int decision = decoder.decodeBit(cx, contextLabel);
                    if (decision != 0) {
                        int sign = decodeSignBit(i, j, index);
                        coefficientsSign[index] = (byte) sign;
                        switch (mbType) {
                            case BYTEMB:
                                ((byte[]) magnitude)[index] = 1;
                                break;
                            case SHORTMB:
                                ((short[]) magnitude)[index] = 1;
                                break;
                            case INTMB:
                                ((int[]) magnitude)[index] = 1;
                                break;
                        }
                        setNeighborSigns(i, j, index);
                        currentFlag[index] = (byte) ((currentFlag[index] & 0xff) | firstMagnitudeBitMask);
                    }
                    bitsDecoded[index]++;
                    currentFlag[index] = (byte) ((currentFlag[index] & 0xff) | processedMask);
                }
            }
        }
    }
    
    private long getCoMag(int pos){
        switch (mbType) {
            case BYTEMB:
                return ((byte[]) magnitude)[pos] & 0xff;
            case SHORTMB:
                return ((short[]) magnitude)[pos] & 0xffff;
            case INTMB:
                return ((int[]) magnitude)[pos];
        }
        return 0;
    }

    public int decodeSignBit(int y, int x, int index) {

        int contribution, sign0, sign1, contextLabel, decoded;
        boolean significance1;

        significance1 = (x > 0 && getCoMag(index - 1) != 0);
        if (x + 1 < width && getCoMag(index + 1) != 0) {
            sign1 = coefficientsSign[index + 1] & 0xff;
            if (significance1) {
                sign0 = coefficientsSign[index - 1] & 0xff;
                contribution = 1 - sign1 - sign0;
            } else {
                contribution = 1 - sign1 - sign1;
            }
        } else if (significance1) {
            sign0 = coefficientsSign[index - 1] & 0xff;
            contribution = 1 - sign0 - sign0;
        } else {
            contribution = 0;
        }
        int horizontalContribution = 3 * contribution;

        significance1 = (y > 0 && getCoMag(index - width) != 0);
        if (y + 1 < height && getCoMag(index + width) != 0) {
            sign1 = coefficientsSign[index + width] & 0xff;
            if (significance1) {
                sign0 = coefficientsSign[index - width] & 0xff;
                contribution = 1 - sign1 - sign0 + horizontalContribution;
            } else {
                contribution = 1 - sign1 - sign1 + horizontalContribution;
            }
        } else if (significance1) {
            sign0 = coefficientsSign[index - width] & 0xff;
            contribution = 1 - sign0 - sign0 + horizontalContribution;
        } else {
            contribution = horizontalContribution;
        }

        if (contribution >= 0) {
            contextLabel = 9 + contribution;
            decoded = decoder.decodeBit(cx, contextLabel);
        } else {
            contextLabel = 9 - contribution;
            decoded = decoder.decodeBit(cx, contextLabel) ^ 1;
        }
        return decoded;
    }

    public void runMRP() {
        int processedMask = 1;
        int msbMask = 2;
        int dim = width * height;
        int width4 = width * 4;

        int indexNext;

        for (int idx0 = 0; idx0 < dim; idx0 = indexNext) {
            indexNext = Math.min(dim, idx0 + width4);
            for (int j = 0; j < width; j++) {
                for (int index = idx0 + j; index < indexNext; index += width) {

                    long cmIndex = 0;
                    switch (mbType) {
                        case BYTEMB:
                            cmIndex = ((byte[]) magnitude)[index] & 0xff;
                            break;
                        case SHORTMB:
                            cmIndex = ((short[]) magnitude)[index] & 0xffff;
                            break;
                        case INTMB:
                            cmIndex = ((int[]) magnitude)[index];
                            break;
                    }

                    if (cmIndex == 0 || ((currentFlag[index] & 0xff) & processedMask) != 0) {
                        continue;
                    }

                    int contextLabel = 16;
                    if (((currentFlag[index] & 0xff) & msbMask) != 0) {
                        currentFlag[index] = (byte) ((currentFlag[index] & 0xff) ^ msbMask);
                        
                        int significance = (neighborSigns[index] & 0xff) & 127;
                        contextLabel = significance == 0 ? 15 : 14;
                    }

                    int bit = decoder.decodeBit(cx, contextLabel);

                    switch (mbType) {
                        case BYTEMB:
                            ((byte[]) magnitude)[index] = (byte) ((cmIndex << 1) | bit);
                            break;
                        case SHORTMB:
                            ((short[]) magnitude)[index] = (short) ((cmIndex << 1) | bit);
                            break;
                        case INTMB:
                            ((int[]) magnitude)[index] = (int) ((cmIndex << 1) | bit);
                            break;
                    }

                    bitsDecoded[index]++;
                    currentFlag[index] = (byte) ((currentFlag[index] & 0xff) | processedMask);
                }
            }
        }
    }

    public void runCP() {
        int processedMask = 1;
        int firstMagnitudeBitMask = 2;
        int w1 = width;
        int w2 = width * 2;
        int w3 = width * 3;
        int nextBit;
        for (int i0 = 0; i0 < height; i0 = nextBit) {
            nextBit = Math.min(i0 + 4, height);
            int indexBase = i0 * width;
            boolean checkAllEmpty = (i0 + 3) < height;
            for (int j = 0; j < width; j++) {
                int index0 = indexBase + j;

                boolean allEmpty = (checkAllEmpty
                        && currentFlag[index0] == 0
                        && currentFlag[index0 + w1] == 0
                        && currentFlag[index0 + w2] == 0
                        && currentFlag[index0 + w3] == 0
                        && neighborSigns[index0] == 0
                        && neighborSigns[index0 + w1] == 0
                        && neighborSigns[index0 + w2] == 0
                        && neighborSigns[index0 + w3] == 0);
                int i1 = 0, index = index0;
                int i = i0, sign;

                if (allEmpty) {
                    int hasSignificantCoefficent = decoder.decodeBit(cx, RUNLENGTH);

                    if (hasSignificantCoefficent == 0) {
                        bitsDecoded[index0]++;
                        bitsDecoded[index0 + w1]++;
                        bitsDecoded[index0 + w2]++;
                        bitsDecoded[index0 + w3]++;
                        continue;
                    }
                    i1 = (decoder.decodeBit(cx, UNIFORM) << 1) | decoder.decodeBit(cx, UNIFORM);
                    if (i1 != 0) {
                        i = i0 + i1;
                        index += (i1 * width);
                    }

                    sign = decodeSignBit(i, j, index);

                    coefficientsSign[index] = (byte) sign;

                    switch (mbType) {
                        case BYTEMB:
                            ((byte[]) magnitude)[index] = 1;
                            break;
                        case SHORTMB:
                            ((short[]) magnitude)[index] = 1;
                            break;
                        case INTMB:
                            ((int[]) magnitude)[index] = 1;
                            break;
                    }

                    setNeighborSigns(i, j, index);
                    currentFlag[index] = (byte) ((currentFlag[index] & 0xff) | firstMagnitudeBitMask);

                    index = index0;
                    for (int i2 = i0; i2 <= i; i2++) {
                        bitsDecoded[index]++;
                        index += width;
                    }

                    i1++;
                }

                for (i = i0 + i1; i < nextBit; i++, index += width) {

                    long coefIndex = 0;

                    switch (mbType) {
                        case BYTEMB:
                            coefIndex = ((byte[]) magnitude)[index];
                            break;
                        case SHORTMB:
                            coefIndex = ((short[]) magnitude)[index];
                            break;
                        case INTMB:
                            coefIndex = ((int[]) magnitude)[index];
                            break;
                    }

                    if (coefIndex != 0 || ((currentFlag[index] & 0xff) & processedMask) != 0) {
                        continue;
                    }

                    int contextLabel = contextLableTable[(neighborSigns[index] & 0xff)] ;
                    int decision = decoder.decodeBit(cx, contextLabel);

                    if (decision == 1) {
                        sign = decodeSignBit(i, j, index);
                        coefficientsSign[index] = (byte) sign;
                        switch (mbType) {
                            case BYTEMB:
                                ((byte[]) magnitude)[index] = 1;
                                break;
                            case SHORTMB:
                                ((short[]) magnitude)[index] = 1;
                                break;
                            case INTMB:
                                ((int[]) magnitude)[index] = 1;
                                break;
                        }
                        setNeighborSigns(i, j, index);
                        currentFlag[index] = (byte) ((currentFlag[index] & 0xff) | firstMagnitudeBitMask);
                    }
                    bitsDecoded[index]++;

                }
            }
        }
    }

}
