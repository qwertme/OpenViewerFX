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
 * JpegDecoder.java
 * ---------------
 */
package com.idrsolutions.image.jpeg;

import com.idrsolutions.image.jpeg2000.EnumeratedSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class reads Jpeg images as BufferedImage
 *
 * <h3>Example:</h3>
 * <pre><code>
 * JpegDecoder decoder = new JpegDecoder();
 * // Make NO assumptions about type of BufferedImage type returned (may change)
 * BufferedImage decodedImage = decoder.read(jpegByteData);
 * </code></pre>
 *
 */
public class JpegDecoder {

    private int offset;
    private byte[] data;
    private boolean cmykInverted = true;
    private final HashMap<Integer, int[]> qTables = new HashMap<Integer, int[]>();
    private Info info;

    /**
     * Decodes and returns the Jpeg image as a BufferedImage.
     * <p>
     * Make NO assumptions about type of BufferedImage type returned (may change)
     *
     * @param jpegRawData A byte[] array containing the Jpeg data
     * @return BufferedImage The decoded image
     * @throws Exception
     *
     */
    public BufferedImage read(byte[] jpegRawData) throws Exception {
        info = new Info();
        updateJpegInfo(jpegRawData);
        Object[] YCBCR = decodeSampling(info);
        return getBufferdImageFromInfo(info, YCBCR);
    }

    /**
     * Not recommended for external use.
     * <p>
     * decodes JPEG image data as non converted bytes <br/>
     * Please note: this method does not perform ycbcr/cmyk/ycck to RGB/gray
     * conversion Example: if ycbcr component image then returned byte array
     * contains y followed by cb followed by cr
     *
     * @param jpegRawData A byte[] array containing the JPEG data
     * @return BufferedImage to read image
     * @throws Exception Provides for different exceptions thrown under java
     * lang package
     */
    public byte[] readComponentsAsRawBytes(byte[] jpegRawData) throws Exception {
        info = new Info();
        updateJpegInfo(jpegRawData);
        Object[] YCBCR = decodeSampling(info);
        return getBytesArrayFromInfo(info, YCBCR);
    }

    /**
     * Not recommended for external use.
     * <p>
     * decodes JPEG image data as converted(rgb/gray) bytes <br/>
     *
     * @param jpegRawData A byte[] array containing the JPEG data
     * @return BufferedImage to read image
     * @throws Exception Provides for different exceptions thrown under java
     * lang package
     */
    public byte[] readComponentsAsConvertedBytes(byte[] jpegRawData) throws Exception {
        info = new Info();
        updateJpegInfo(jpegRawData);
        Object[] YCBCR = decodeSampling(info);
        return getConvertedBytesFromInfo(info, YCBCR);
    }

    private static Object[] decodeSampling(Info info) {
        int nComp = info.nComp;
        int maxH = info.maxH;
        int maxV = info.maxV;

        Object[] lineComps = new Object[nComp];
        int maxLineX = 0;
        int maxLineY = 0;

        for (int i = 0; i < nComp; i++) {
            Component comp = info.frame.components.get(i);
            int blocksX = (comp.blocksX + 1);
            int blocksY = (comp.blocksY + 1);
            int lineX = blocksX << 3;
            int lineY = blocksY << 3;
            maxLineX = Math.max(lineX, maxLineX);
            maxLineY = Math.max(lineY, maxLineY);
        }

        info.maxLineX = maxLineX;

        for (int i = 0; i < nComp; i++) {

            Component comp = info.frame.components.get(i);
            int vIter = maxV / comp.v;
            int hIter = maxH / comp.h;
            int blocksX = (comp.blocksX + 1);
            int blocksY = (comp.blocksY + 1);
            int lineX = blocksX << 3;
            int lineY = blocksY << 3;

            int[] compData = comp.codeBlock;

            byte[] maxData = new byte[maxLineY * maxLineX];

            int pointer, idx = 0, tPos, sPos;
            byte tVal;

            if (vIter == 1 && hIter == 1) {
                for (int r = 0; r < blocksY; r++) {
                    for (int c = 0; c < blocksX; c++) {
                        pointer = (c + r * lineX) << 3;
                        for (int a = 0; a < 8; a++) {
                            for (int b = 0; b < 8; b++) {
                                maxData[pointer + b] = (byte) compData[idx++];
                            }
                            pointer += lineX;
                        }
                    }
                }
            } else {

                byte[] tempData = new byte[lineX * lineY];
                for (int r = 0; r < blocksY; r++) {
                    for (int c = 0; c < blocksX; c++) {
                        pointer = (c + r * lineX) << 3;
                        for (int a = 0; a < 8; a++) {
                            for (int b = 0; b < 8; b++) {
                                tempData[pointer + b] = (byte) compData[idx++];
                            }
                            pointer += lineX;
                        }
                    }
                }
                idx = 0;
                sPos = 0;
                byte[] tempLine = new byte[lineX * hIter];
                for (int y = 0; y < lineY; y++) {
                    tPos = 0;
                    for (int x = 0; x < lineX; x++) {
                        tVal = tempData[idx++];
                        for (int h = 0; h < hIter; h++) {
                            tempLine[tPos++] = tVal;
                        }
                    }
                    for (int v = 0; v < vIter; v++) {
                        if (sPos + maxLineX < maxData.length) {
                            System.arraycopy(tempLine, 0, maxData, sPos, maxLineX);
                            sPos += maxLineX;
                        }
                    }
                }
            }
            lineComps[i] = maxData;
        }
        info.frame.components.clear();
        return lineComps;
    }

    private static byte[] getBytesArrayFromInfo(Info info, Object[] YCBCR) {
        byte[] comp0, comp1, comp2, comp3, output = null;
        int index, p = 0, maxLineX = info.maxLineX;
        switch (info.nComp) {
            case 1:
                comp0 = (byte[]) YCBCR[0];
                output = new byte[info.height * info.width];
                for (int i = 0; i < info.height; i++) {
                    index = i * maxLineX;
                    for (int j = 0; j < info.width; j++) {
                        output[p++] = comp0[index++];
                    }
                }
                break;
            case 2:
                comp0 = (byte[]) YCBCR[0];
                comp1 = (byte[]) YCBCR[1];
                output = new byte[info.height * info.width * 2];
                for (int i = 0; i < info.height; i++) {
                    index = i * maxLineX;
                    for (int j = 0; j < info.width; j++) {
                        output[p++] = comp0[index];
                        output[p++] = comp1[index];
                        index++;
                    }
                }
                break;
            case 3:
                comp0 = (byte[]) YCBCR[0];
                comp1 = (byte[]) YCBCR[1];
                comp2 = (byte[]) YCBCR[2];
                output = new byte[info.height * info.width * 3];
                for (int i = 0; i < info.height; i++) {
                    index = i * maxLineX;
                    for (int j = 0; j < info.width; j++) {
                        output[p++] = comp0[index];
                        output[p++] = comp1[index];
                        output[p++] = comp2[index];
                        index++;
                    }
                }
                break;
            case 4:
                comp0 = (byte[]) YCBCR[0];
                comp1 = (byte[]) YCBCR[1];
                comp2 = (byte[]) YCBCR[2];
                comp3 = (byte[]) YCBCR[3];
                output = new byte[info.height * info.width * 4];
                for (int i = 0; i < info.height; i++) {
                    index = i * maxLineX;
                    for (int j = 0; j < info.width; j++) {
                        output[p++] = comp0[index];
                        output[p++] = comp1[index];
                        output[p++] = comp2[index];
                        output[p++] = comp3[index];
                        index++;
                    }
                }
                break;
        }
        return output;
    }

    private byte[] getConvertedBytesFromInfo(Info info, Object[] YCBCR) {

        byte[] pixelsByte = null;
        int r, g, b, c, m, y, k, u, v, index, p = 0, maxLineX = info.maxLineX;
        double cc, mm, yy;
        byte[] comp0, comp1, comp2, comp3;
        switch (info.nComp) {
            case 1:
                pixelsByte = new byte[info.width * info.height];
                comp0 = (byte[]) YCBCR[0];
                for (int i = 0; i < info.height; i++) {
                    index = i * maxLineX;
                    for (int j = 0; j < info.width; j++) {
                        pixelsByte[p++] = comp0[index++];
                    }
                }
                break;
            case 2:
                System.out.println("two color component jpegs not supported yet");
                break;
            case 3:
                pixelsByte = new byte[info.width * info.height * 3];
                comp0 = (byte[]) YCBCR[0];
                comp1 = (byte[]) YCBCR[1];
                comp2 = (byte[]) YCBCR[2];

                if (info.adobe != null && info.adobe.transformCode == 0) {

                    for (int i = 0; i < info.height; i++) {
                        index = i * maxLineX;
                        for (int j = 0; j < info.width; j++) {
                            r = comp0[index] & 0xff;
                            g = comp1[index] & 0xff;
                            b = comp2[index] & 0xff;

                            r = r < 0 ? 0 : r > 255 ? 255 : r;
                            g = g < 0 ? 0 : g > 255 ? 255 : g;
                            b = b < 0 ? 0 : b > 255 ? 255 : b;

                            pixelsByte[p++] = (byte) r;
                            pixelsByte[p++] = (byte) g;
                            pixelsByte[p++] = (byte) b;
                            index++;
                        }
                    }
                } else {
                    for (int i = 0; i < info.height; i++) {
                        index = i * maxLineX;
                        for (int j = 0; j < info.width; j++) {
                            y = comp0[index] & 0xff;
                            u = comp1[index] & 0xff;
                            v = comp2[index] & 0xff;

                            u -= 128;
                            v -= 128;

                            int u2 = u >> 2;
                            int v35 = (v >> 3) + (v >> 5);

                            r = y + v + (v >> 2) + v35;
                            g = y - (u2 + (u >> 4) + (u >> 5)) - ((v >> 1) + v35 + (v >> 4));
                            b = y + u + (u >> 1) + u2 + (u >> 6);

                            r = r < 0 ? 0 : r > 255 ? 255 : r;
                            g = g < 0 ? 0 : g > 255 ? 255 : g;
                            b = b < 0 ? 0 : b > 255 ? 255 : b;

                            pixelsByte[p++] = (byte) r;
                            pixelsByte[p++] = (byte) g;
                            pixelsByte[p++] = (byte) b;
                            index++;
                        }
                    }
                }

                break;
            case 4:
                pixelsByte = new byte[info.width * info.height * 3];
                comp0 = (byte[]) YCBCR[0];
                comp1 = (byte[]) YCBCR[1];
                comp2 = (byte[]) YCBCR[2];
                comp3 = (byte[]) YCBCR[3];

                EnumeratedSpace cmyk = new EnumeratedSpace();

                if (info.adobe.transformCode == 0) {

                    if (cmykInverted) {
                        for (int i = 0; i < info.height; i++) {
                            index = i * maxLineX;
                            for (int j = 0; j < info.width; j++) {
                                c = 255 - (comp0[index] & 0xff);
                                m = 255 - (comp1[index] & 0xff);
                                y = 255 - (comp2[index] & 0xff);
                                k = 255 - (comp3[index] & 0xff);

                                byte[] bb = cmyk.getRGB((byte) c, (byte) m, (byte) y, (byte) k);
                                pixelsByte[p++] = bb[0];
                                pixelsByte[p++] = bb[1];
                                pixelsByte[p++] = bb[2];
                                index++;
                            }
                        }
                    } else {
                        for (int i = 0; i < info.height; i++) {
                            index = i * maxLineX;
                            for (int j = 0; j < info.width; j++) {
                                c = (comp0[index] & 0xff);
                                m = (comp1[index] & 0xff);
                                y = (comp2[index] & 0xff);
                                k = (comp3[index] & 0xff);

                                byte[] bb = cmyk.getRGB((byte) c, (byte) m, (byte) y, (byte) k);
                                pixelsByte[p++] = bb[0];
                                pixelsByte[p++] = bb[1];
                                pixelsByte[p++] = bb[2];
                                index++;
                            }
                        }
                    }

                } else {

                    if (cmykInverted) {
                        for (int i = 0; i < info.height; i++) {
                            index = i * maxLineX;
                            for (int j = 0; j < info.width; j++) {

                                y = 255 - (comp0[index] & 0xff);
                                u = 255 - (comp1[index] & 0xff);
                                v = 255 - (comp2[index] & 0xff);
                                k = 255 - (comp3[index] & 0xff);

                                cc = (434.456 - y - 1.402 * v);
                                mm = (119.541 - y + 0.344 * u + 0.714 * v);
                                yy = (481.816 - y - 1.772 * u);

                                c = cc < 0 ? 0 : cc > 255 ? 255 : (int) cc;
                                m = mm < 0 ? 0 : mm > 255 ? 255 : (int) mm;
                                y = yy < 0 ? 0 : yy > 255 ? 255 : (int) yy;

                                byte[] bb = cmyk.getRGB((byte) c, (byte) m, (byte) y, (byte) k);

                                pixelsByte[p++] = bb[0];
                                pixelsByte[p++] = bb[1];
                                pixelsByte[p++] = bb[2];
                                index++;
                            }
                        }

                    } else {
                        for (int i = 0; i < info.height; i++) {
                            index = i * maxLineX;
                            for (int j = 0; j < info.width; j++) {
                                y = (comp0[index] & 0xff);
                                u = (comp1[index] & 0xff);
                                v = (comp2[index] & 0xff);
                                k = (comp3[index] & 0xff);

                                cc = (434.456 - y - 1.402 * v);
                                mm = (119.541 - y + 0.344 * u + 0.714 * v);
                                yy = (481.816 - y - 1.772 * u);

                                c = cc < 0 ? 0 : cc > 255 ? 255 : (int) cc;
                                m = mm < 0 ? 0 : mm > 255 ? 255 : (int) mm;
                                y = yy < 0 ? 0 : yy > 255 ? 255 : (int) yy;

                                byte[] bb = cmyk.getRGB((byte) c, (byte) m, (byte) y, (byte) k);

                                pixelsByte[p++] = bb[0];
                                pixelsByte[p++] = bb[1];
                                pixelsByte[p++] = bb[2];
                                index++;
                            }
                        }
                    }
                }
                break;
        }
        return pixelsByte;
    }

    private BufferedImage getBufferdImageFromInfo(Info info, Object[] YCBCR) {
        BufferedImage image = null;
        int[] pixels;
        byte[] pixelsByte;
        int r, g, b, c, m, y, k, u, v, index, p = 0, maxLineX = info.maxLineX;
        double cc, mm, yy;
        byte[] comp0, comp1, comp2, comp3;
        switch (info.nComp) {
            case 1:
                image = new BufferedImage(info.width, info.height, BufferedImage.TYPE_BYTE_GRAY);
                pixelsByte = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                comp0 = (byte[]) YCBCR[0];
                for (int i = 0; i < info.height; i++) {
                    index = i * maxLineX;
                    for (int j = 0; j < info.width; j++) {
                        pixelsByte[p++] = comp0[index++];
                    }
                }
                break;
            case 2:
                System.out.println("two color component jpegs not supported yet");
                break;
            case 3:
                image = new BufferedImage(info.width, info.height, BufferedImage.TYPE_INT_RGB);
                pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                comp0 = (byte[]) YCBCR[0];
                comp1 = (byte[]) YCBCR[1];
                comp2 = (byte[]) YCBCR[2];

                if (info.adobe != null && info.adobe.transformCode == 0) {

                    for (int i = 0; i < info.height; i++) {
                        index = i * maxLineX;
                        for (int j = 0; j < info.width; j++) {
                            r = comp0[index] & 0xff;
                            g = comp1[index] & 0xff;
                            b = comp2[index] & 0xff;

                            r = r < 0 ? 0 : r > 255 ? 255 : r;
                            g = g < 0 ? 0 : g > 255 ? 255 : g;
                            b = b < 0 ? 0 : b > 255 ? 255 : b;

                            pixels[p++] = (r << 16) | (g << 8) | b;
                            index++;
                        }
                    }
                } else {
                    for (int i = 0; i < info.height; i++) {
                        index = i * maxLineX;
                        for (int j = 0; j < info.width; j++) {
                            y = comp0[index] & 0xff;
                            u = comp1[index] & 0xff;
                            v = comp2[index] & 0xff;

                            u -= 128;
                            v -= 128;
                            
                            y = (y<<8)+128;
                            r = (y + 359 * v) >> 8;
                            g = (y - 88 * u - 183 * v) >> 8;
                            b = (y + 454 * u) >> 8;
                            
//                            int u2 = u >> 2;
//                            int v35 = (v >> 3) + (v >> 5);
//                            
//                            r = y + v + (v >> 2) + v35;
//                            g = y - (u2 + (u >> 4) + (u >> 5)) - ((v >> 1) + v35 + (v >> 4));
//                            b = y + u + (u >> 1) + u2 + (u >> 6);

                            r = r < 0 ? 0 : r > 255 ? 255 : r;
                            g = g < 0 ? 0 : g > 255 ? 255 : g;
                            b = b < 0 ? 0 : b > 255 ? 255 : b;

                            pixels[p++] = (r << 16) | (g << 8) | b;
                            index++;
                        }
                    }
                }

                break;
            case 4:
                image = new BufferedImage(info.width, info.height, BufferedImage.TYPE_INT_RGB);
                pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                comp0 = (byte[]) YCBCR[0];
                comp1 = (byte[]) YCBCR[1];
                comp2 = (byte[]) YCBCR[2];
                comp3 = (byte[]) YCBCR[3];

                EnumeratedSpace cmyk = new EnumeratedSpace();

                if (info.adobe.transformCode == 0) {

                    if (cmykInverted) {
                        for (int i = 0; i < info.height; i++) {
                            index = i * maxLineX;
                            for (int j = 0; j < info.width; j++) {
                                c = 255 - (comp0[index] & 0xff);
                                m = 255 - (comp1[index] & 0xff);
                                y = 255 - (comp2[index] & 0xff);
                                k = 255 - (comp3[index] & 0xff);

                                byte[] bb = cmyk.getRGB((byte) c, (byte) m, (byte) y, (byte) k);
                                r = bb[0] & 0xff;
                                g = bb[1] & 0xff;
                                b = bb[2] & 0xff;
                                pixels[p++] = (r << 16) | (g << 8) | b;
                                index++;
                            }
                        }
                    } else {
                        for (int i = 0; i < info.height; i++) {
                            index = i * maxLineX;
                            for (int j = 0; j < info.width; j++) {
                                c = (comp0[index] & 0xff);
                                m = (comp1[index] & 0xff);
                                y = (comp2[index] & 0xff);
                                k = (comp3[index] & 0xff);

                                byte[] bb = cmyk.getRGB((byte) c, (byte) m, (byte) y, (byte) k);
                                r = bb[0] & 0xff;
                                g = bb[1] & 0xff;
                                b = bb[2] & 0xff;
                                pixels[p++] = (r << 16) | (g << 8) | b;
                                index++;
                            }
                        }
                    }

                } else {

                    if (cmykInverted) {
                        for (int i = 0; i < info.height; i++) {
                            index = i * maxLineX;
                            for (int j = 0; j < info.width; j++) {

                                y = 255 - (comp0[index] & 0xff);
                                u = 255 - (comp1[index] & 0xff);
                                v = 255 - (comp2[index] & 0xff);
                                k = 255 - (comp3[index] & 0xff);

                                cc = (434.456 - y - 1.402 * v);
                                mm = (119.541 - y + 0.344 * u + 0.714 * v);
                                yy = (481.816 - y - 1.772 * u);

                                c = cc < 0 ? 0 : cc > 255 ? 255 : (int) cc;
                                m = mm < 0 ? 0 : mm > 255 ? 255 : (int) mm;
                                y = yy < 0 ? 0 : yy > 255 ? 255 : (int) yy;

                                byte[] bb = cmyk.getRGB((byte) c, (byte) m, (byte) y, (byte) k);
                                r = bb[0] & 0xff;
                                g = bb[1] & 0xff;
                                b = bb[2] & 0xff;

                                pixels[p++] = (r << 16) | (g << 8) | b;
                                index++;
                            }
                        }

                    } else {
                        for (int i = 0; i < info.height; i++) {
                            index = i * maxLineX;
                            for (int j = 0; j < info.width; j++) {
                                y = (comp0[index] & 0xff);
                                u = (comp1[index] & 0xff);
                                v = (comp2[index] & 0xff);
                                k = (comp3[index] & 0xff);

                                cc = (434.456 - y - 1.402 * v);
                                mm = (119.541 - y + 0.344 * u + 0.714 * v);
                                yy = (481.816 - y - 1.772 * u);

                                c = cc < 0 ? 0 : cc > 255 ? 255 : (int) cc;
                                m = mm < 0 ? 0 : mm > 255 ? 255 : (int) mm;
                                y = yy < 0 ? 0 : yy > 255 ? 255 : (int) yy;

                                byte[] bb = cmyk.getRGB((byte) c, (byte) m, (byte) y, (byte) k);
                                r = bb[0] & 0xff;
                                g = bb[1] & 0xff;
                                b = bb[2] & 0xff;

                                pixels[p++] = (r << 16) | (g << 8) | b;
                                index++;
                            }
                        }
                    }
                }
                break;
        }
        return image;
    }

    private void updateJpegInfo(byte[] data) throws Exception {

        this.data = data;
        int length = data.length;
        int ri = 0;
        Frame frame = new Frame();

        if (readUShort() != Markers.SOI) {
            throw new Exception("This File is not a valid JPEG");
        }
        Object[] huffmanTablesAC = new Object[10];
        Object[] huffmanTablesDC = new Object[10];

        int markerRead = readUShort();
        while (markerRead != Markers.EOI && offset < length) {
            switch (markerRead) {
                case Markers.APP0:
                case Markers.APP1:
                case Markers.APP2:
                case Markers.APP3:
                case Markers.APP4:
                case Markers.APP5:
                case Markers.APP6:
                case Markers.APP7:
                case Markers.APP8:
                case Markers.APP9:
                case Markers.APP10:
                case Markers.APP11:
                case Markers.APP12:
                case Markers.APP13:
                case Markers.APP14:
                case Markers.APP15:
                    byte[] apps = readDataArray();
                    if (markerRead == Markers.APP0 && isJFIF(apps)) {
                        JFIFHolder jfif = new JFIFHolder();
                        jfif.majorNo = apps[5] & 0xff;
                        jfif.minorNo = apps[6] & 0xff;
                        jfif.xDensity = ((apps[8] & 0xff) << 8) | (apps[9] & 0xff);
                        jfif.yDensity = ((apps[10] & 0xff) << 8) | (apps[11] & 0xff);
                        jfif.thumbnailWidth = apps[12] & 0xff;
                        jfif.thumbnailHeight = apps[13] & 0xff;

                        info.jfif = jfif;
                    } else if (markerRead == Markers.APP14 && isAdobe(apps)) {
                        AdobeHolder adobe = new AdobeHolder();
                        adobe.version = apps[6] & 0xff;
                        adobe.flag0 = (apps[7] & 0xff << 8) | (apps[8] & 0xff);
                        adobe.flag1 = (apps[9] & 0xff << 8) | (apps[10] & 0xff);
                        adobe.transformCode = apps[11] & 0xff;
                        info.adobe = adobe;
                    }
                    break;
                case Markers.SOF0:
                case Markers.SOF1:
                case Markers.SOF2:
                    offset += 2; // skip data length
                    frame.baseline = (markerRead == Markers.SOF0);
                    frame.extended = (markerRead == Markers.SOF1);
                    frame.progressive = (markerRead == Markers.SOF2);
                    frame.precision = data[offset++] & 0xff;
                    frame.scanV = readUShort();
                    frame.scanH = readUShort();
                    int componentsCount = data[offset++] & 0xff;
                    int componentId;
                    int maxH = 0,
                     maxV = 0;
                    for (int i = 0; i < componentsCount; i++) {
                        componentId = data[offset] & 0xff;
                        int vh = data[offset + 1] & 0xff;
                        int h = vh >> 4;
                        int v = vh & 15;
                        if (maxH < h) {
                            maxH = h;
                        }
                        if (maxV < v) {
                            maxV = v;
                        }
                        int qId = data[offset + 2] & 0xff;
                        Component comp = new Component();
                        comp.h = h;
                        comp.v = v;
                        comp.qTable = qTables.get(qId);
                        frame.components.add(comp);
                        frame.componentID.put(componentId, frame.components.size() - 1);
                        offset += 3;
                    }
                    frame.maxH = maxH;
                    frame.maxV = maxV;
                    initializeComponents(frame);
                    break;
                case Markers.SOF3:
                case Markers.SOF5:
                case Markers.SOF6:
                case Markers.SOF7:
                    throw new Exception("Lossless Jpeg is not supported yet");
                case Markers.SOF9:
                case Markers.SOF10:
                case Markers.SOF11:
                    throw new Exception("Arithmetic encoded Jpeg is not supported yet");
                case Markers.DQT:
                    int dqtLen = readUShort();
                    int quantizationTablesEnd = dqtLen + offset - 2;
                    int z;
                    while (offset < quantizationTablesEnd) {
                        int qs = data[offset++] & 0xff;
                        int[] tableData = new int[64];
                        if ((qs >> 4) == 0) {
                            for (int i = 0; i < 64; i++) {
                                z = JpegLUT.ZIGZAGORDER[i];
                                tableData[z] = data[offset++] & 0xff;
                            }
                        } else if ((qs >> 4) == 1) {
                            for (int i = 0; i < 64; i++) {
                                z = JpegLUT.ZIGZAGORDER[i];
                                tableData[z] = readUShort();
                            }
                        }
                        qTables.put(qs & 15, tableData);
                    }
                    break;
                case Markers.DHT:
                    int huffmanLength = readUShort();
                    for (int i = 2; i < huffmanLength;) {
                        int huffmanTableSpec = data[offset++] & 0xff;
                        int[] codeLengths = new int[16];
                        int codeLengthTotal = 0;
                        for (int j = 0; j < 16; j++) {
                            codeLengths[j] = data[offset] & 0xff;
                            codeLengthTotal += codeLengths[j];
                            offset++;
                        }
                        int[] huffmanValues = new int[codeLengthTotal];
                        for (int j = 0; j < codeLengthTotal; j++, offset++) {
                            huffmanValues[j] = data[offset] & 0xff;
                        }
                        i += 17 + codeLengthTotal;

                        if (huffmanTableSpec >> 4 == 0) {
                            huffmanTablesDC[huffmanTableSpec & 15] = JpegHuffman.generateHuffmanTable(codeLengths, huffmanValues);
                        } else {
                            huffmanTablesAC[huffmanTableSpec & 15] = JpegHuffman.generateHuffmanTable(codeLengths, huffmanValues);
                        }
                    }
                    break;
                case Markers.DRI:
                    offset += 2;
                    ri = readUShort();
                    break;
                case Markers.SOS:
                    offset += 2;
                    int sc = data[offset++] & 0xff;
                    List<Component> components = new ArrayList<Component>();
                    for (int i = 0; i < sc; i++) {
                        int componentIndex = frame.componentID.get(data[offset++] & 0xff);
                        Component component = frame.components.get(componentIndex);
                        int tableSpec = data[offset++] & 0xff;
                        component.huffmanTableDC = huffmanTablesDC[tableSpec >> 4];
                        component.huffmanTableAC = huffmanTablesAC[tableSpec & 15];
                        components.add(component);
                    }
                    int sStart = data[offset++] & 0xff;
                    int sEnd = data[offset++] & 0xff;
                    int sApprox = data[offset++] & 0xff;
                    JpegScanner scanner = new JpegScanner(data);
                    int processed = scanner.decodeScan(offset, frame, components, ri, sStart, sEnd, sApprox >> 4, sApprox & 15);
                    offset += processed;
                    break;
                case Markers.COM:
                    readDataArray();
                    break;
                default:
                    System.err.println("Invalid jpeg marker found");
                    int len = readUShort();
                    offset += (len - 2);
                    break;
            }
            markerRead = readUShort();

        }

        info.width = frame.scanH;
        info.height = frame.scanV;
        for (Component component : frame.components) {
            component.codeBlock = buildComponentData(component);
        }
        info.maxH = frame.maxH;
        info.maxV = frame.maxV;
        info.nComp = frame.components.size();
        info.frame = frame;
    }

    private int readUShort() {
        int value = ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
        offset += 2;
        return value;
    }

    private static boolean isJFIF(byte[] db) {
        return (db[0] & 0xff) == 0x4A
                && (db[1] & 0xff) == 0x46
                && (db[2] & 0xff) == 0x49
                && (db[3] & 0xff) == 0x46
                && (db[4] & 0xff) == 0;
    }

    private static boolean isAdobe(byte[] db) {
        return (db[0] & 0xff) == 0x41
                && (db[1] & 0xff) == 0x64
                && (db[2] & 0xff) == 0x6F
                && (db[3] & 0xff) == 0x62
                && (db[4] & 0xff) == 0x65
                && (db[5] & 0xff) == 0;
    }

    private byte[] readDataArray() {
        int len = readUShort();
        byte[] bb = new byte[len - 2];
        System.arraycopy(data, offset, bb, 0, bb.length);
        offset += bb.length;
        return bb;
    }

    private static void initializeComponents(Frame frame) {
        int mcusPerLine = (int) Math.ceil(frame.scanH / 8.0 / frame.maxH);
        int mcusPerColumn = (int) Math.ceil(frame.scanV / 8.0 / frame.maxV);

        for (Component component : frame.components) {
            int blocksPerLine = (int) Math.ceil(Math.ceil(frame.scanH / 8.0) * component.h / (1.0 * frame.maxH));
            int blocksPerColumn = (int) Math.ceil(Math.ceil(frame.scanV / 8.0) * component.v / (1.0 * frame.maxV));
            int blocksPerLineForMcu = mcusPerLine * component.h;
            int blocksPerColumnForMcu = mcusPerColumn * component.v;

            int blocksBufferSize = 64 * (blocksPerColumnForMcu + 1) * (blocksPerLineForMcu + 1);
            component.codeBlock = new int[blocksBufferSize];
            component.blocksX = blocksPerLine;
            component.blocksY = blocksPerColumn;
        }
        frame.mcusX = mcusPerLine;
        frame.mcusY = mcusPerColumn;
    }

    private static int[] buildComponentData(Component component) {
        int blocksPerLine = component.blocksX;
        int blocksPerColumn = component.blocksY;

        for (int blockRow = 0; blockRow < blocksPerColumn; blockRow++) {
            for (int blockCol = 0; blockCol < blocksPerLine; blockCol++) {
                int offset = JpegScanner.getCodeBlockOffset(component, blockRow, blockCol);
                DCT.IDCTQ(component, offset);
            }
        }
        return component.codeBlock;
    }

    /**
     * Returns whether the data byte is inverted.
     * @return Whether the data byte is inverted.
     */
    public boolean isInverted() {
        return cmykInverted;
    }

    /**
     * Not recommended for external use.
     * @param inverted
     */
    public void setInverted(boolean inverted) {
        this.cmykInverted = inverted;
    }

    /**
     * Returns the Jpeg information object
     * @return The Jpeg information object
     */
    public Info getInfo() {
        return info;
    }

//    public static void main(String[] args) throws Exception {
//        java.io.File f = new java.io.File("C:\\Users\\suda\\Desktop\\testimages\\big1.jpg");
//        FileInputStream fis = new FileInputStream(f);
//        byte[] bb = new byte[(int) f.length()];
//        fis.read(bb);
//        
//
//        long start = System.currentTimeMillis();
//        BufferedImage ii = ImageIO.read(f);
//        System.out.println((System.currentTimeMillis() - start));
//
//        start = System.currentTimeMillis();
//        JpegDecoder decoder = new JpegDecoder();
//        ii = decoder.read(bb);
//        System.out.println((System.currentTimeMillis() - start));
//
//        ImageIO.write(ii, "jpg", new java.io.File("C:\\Users\\suda\\Desktop\\testimages\\Sudaresult" + System.currentTimeMillis() + ".jpg"));
//
//    }

}
