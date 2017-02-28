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
 * JpegEncoder.java
 * ---------------
 */
package com.idrsolutions.image.jpeg;

import java.awt.image.*;
import java.io.*;

/**
 * Class writes BufferedImages as Baseline JPEGs
 *
 * <h3>Example:</h3>
 * <pre><code>
 * JpegEncoder encoder = new JpegEncoder();
 * encoder.write(image, outputStream);
 * </code></pre>
 *
 */
public class JpegEncoder {

    private static final int[] sampleFrequency = {1, 1, 1};

    private static final int[] TableNumber = {0, 1, 1};

    /**
     *
     */
    public JpegEncoder() {

    }

    /**
     * Writes BufferedImage as Baseline Jpeg to OutputStream
     * <p>
     * This method does not close the provided OutputStream after the write
     * operation has completed; it is the responsibility of the caller to close
     * the stream.
     *
     * @param image BufferedImage The image to write
     * @param out The stream to write to
     * @throws IOException if the image wasn't written
     */
    public void write(BufferedImage image, OutputStream out) throws IOException {
        int imageH = image.getHeight();
        int imageW = image.getWidth();
        int nComp = 3;

        JpegHuffman huffman = new JpegHuffman();
        WriteHeaders(out, huffman, imageW, imageH, nComp);

        int[] frequencyH = new int[nComp];
        int[] frequencyW = new int[nComp];

        int maxF = 1;
        for (int i = 0; i < sampleFrequency.length; i++) {
            maxF = Math.max(maxF, sampleFrequency[i]);
        }

        for (int i = 0; i < nComp; i++) {
            double hh = Math.ceil(1.0 * imageH * sampleFrequency[i] / maxF);
            double ww = Math.ceil(1.0 * imageW * sampleFrequency[i] / maxF);
            frequencyH[i] = (int) ((hh % 8 != 0) ? (Math.floor(hh / 8.0) + 1) * 8 : hh);
            frequencyW[i] = (int) ((ww % 8 != 0) ? (Math.floor(ww / 8.0) + 1) * 8 : ww);
        }

        int cc = 0, rr, gg, bb;

        int fw = frequencyW[0];
        int fh = frequencyH[0];
        int imageDiff = frequencyW[0] - imageW;

        int freqDim = fw * fh;
        byte[] r = new byte[freqDim];
        byte[] g = new byte[freqDim];
        byte[] b = new byte[freqDim];

        switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_ARGB:
                int[] pixInts = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                int a = 0;
                for (int i = 0; i < imageH; i++) {
                    for (int j = 0; j < imageW; j++) {
                        byte[] bgr = intToBytes(pixInts[a]);
                        r[cc] = bgr[1];
                        g[cc] = bgr[2];
                        b[cc] = bgr[3];
                        a++;
                        cc++;
                    }
                    cc += imageDiff;
                }
                break;
            case BufferedImage.TYPE_INT_BGR:
                int[] pixBGR = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                int aa = 0;
                for (int i = 0; i < imageH; i++) {
                    for (int j = 0; j < imageW; j++) {
                        byte[] bgr = intToBytes(pixBGR[aa]);
                        r[cc] = bgr[3];
                        g[cc] = bgr[2];
                        b[cc] = bgr[1];
                        aa++;
                        cc++;
                    }
                    cc += imageDiff;
                }
                break;
            case BufferedImage.TYPE_3BYTE_BGR:
                byte[] pByte = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                int pb = 0;
                for (int i = 0; i < imageH; i++) {
                    for (int j = 0; j < imageW; j++) {
                        r[cc] = pByte[pb + 2];
                        g[cc] = pByte[pb + 1];
                        b[cc] = pByte[pb];
                        pb += 3;
                        cc++;
                    }
                    cc += imageDiff;
                }
//                System.out.println("cc "+cc);
                break;
            case BufferedImage.TYPE_4BYTE_ABGR:
                byte[] pBytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                int pb4 = 0;
                for (int i = 0; i < imageH; i++) {
                    for (int j = 0; j < imageW; j++) {
                        r[cc] = pBytes[pb4 + 3];
                        g[cc] = pBytes[pb4 + 2];
                        b[cc] = pBytes[pb4 + 1];
                        pb4 += 4;
                        cc++;
                    }
                    cc += imageDiff;
                }
                break;
            default:
                BufferedImage bImage = new BufferedImage(imageW, imageH, BufferedImage.TYPE_INT_RGB);
                bImage.createGraphics().drawImage(image, 0, 0, null);
                int[] pixInt = ((DataBufferInt) bImage.getRaster().getDataBuffer()).getData();
                int ii = 0;
                for (int i = 0; i < imageH; i++) {
                    for (int j = 0; j < imageW; j++) {
                        byte[] bgr = intToBytes(pixInt[ii]);
                        r[cc] = bgr[1];
                        g[cc] = bgr[2];
                        b[cc] = bgr[3];
                        ii++;
                        cc++;
                    }
                    cc += imageDiff;
                }
                break;
        }

        cc = 0;
        int y, u, v;
        int dim = fw * fh;

        for (int i = 0; i < dim; i++) {
            rr = r[cc] & 0xff;
            gg = g[cc] & 0xff;
            bb = b[cc] & 0xff;

            y = (128 + 76 * rr + 150 * gg + 29 * bb) >> 8;
            u = (128 + 127 * bb - 84 * gg - 43 * rr) >> 8;
            v = (128 + 127 * rr - 106 * gg - 21 * bb) >> 8;

            y -= 128;
            
            r[cc] = (byte) y;
            g[cc] = (byte) u;
            b[cc] = (byte) v;

            cc++;
        }

        Object[] YCBCR = new Object[3];
        YCBCR[0] = r;
        YCBCR[1] = g;
        YCBCR[2] = b;

        WriteCompressedData(out, huffman, YCBCR, fh, fw, nComp);
        WriteEOI(out);
    }

    private static void WriteCompressedData(OutputStream out, JpegHuffman huffman, Object[] YCBCR, int fh, int fw, int nComp) throws IOException {

        int[] arrayDCT = new int[64];

        int yBlocks = fh / 8;
        int xBlocks = fw / 8;

        int lastDCvalue[] = new int[nComp];
        byte[] compBytes;
        int idx, pos, a, b, x, y;

        for (y = 0; y < yBlocks; y++) {
            for (x = 0; x < xBlocks; x++) {
                pos = (x + y * fw) * 8;
                for (int comp = 0; comp < nComp; comp++) {
                    compBytes = (byte[]) YCBCR[comp];
                    idx = 0;
                    int ayfw = pos;
                    for (a = 0; a < 8; a++) {
                        for (b = 0; b < 8; b++) {
                            arrayDCT[idx++] = compBytes[ayfw + b];
                        }
                        ayfw += fw;
                    }
                    DCT.FDCTQ(arrayDCT, TableNumber[comp]);
                    huffman.encodeBlock(out, arrayDCT, lastDCvalue[comp], TableNumber[comp]);
                    lastDCvalue[comp] = arrayDCT[0];
                }
            }
        }

        huffman.end(out);

    }

    private static void WriteEOI(OutputStream out) throws IOException {
        byte[] EOI = {(byte) 0xFF, (byte) 0xD9};
        out.write(EOI);
    }

    private static void WriteHeaders(OutputStream out, JpegHuffman huffman, int imageWidth, int imageHeight, int nComp) throws IOException {
        int index, offset;

        writeMarker(out, Markers.SOI);

        byte JFIF[] = new byte[18];
        JFIF[0] = (byte) 0xff;
        JFIF[1] = (byte) 0xe0;
        JFIF[2] = (byte) 0x00;
        JFIF[3] = (byte) 0x10;
        JFIF[4] = (byte) 0x4a;
        JFIF[5] = (byte) 0x46;
        JFIF[6] = (byte) 0x49;
        JFIF[7] = (byte) 0x46;
        JFIF[8] = (byte) 0x00;
        JFIF[9] = (byte) 0x01;
        JFIF[10] = (byte) 0x00;
        JFIF[11] = (byte) 0x00;
        JFIF[12] = (byte) 0x00;
        JFIF[13] = (byte) 0x01;
        JFIF[14] = (byte) 0x00;
        JFIF[15] = (byte) 0x01;
        JFIF[16] = (byte) 0x00;
        JFIF[17] = (byte) 0x00;
        out.write(JFIF);

        byte DQT[] = new byte[134];
        DQT[0] = (byte) 0xFF;
        DQT[1] = (byte) 0xDB;
        DQT[2] = (byte) 0x00;
        DQT[3] = (byte) 0x84;
        offset = 4;
        for (int i = 0; i < 2; i++) {
            byte tempArray[];
            DQT[offset++] = (byte) (i);
            if (i == 0) {
                tempArray = JpegLUT.QL;
                for (int j = 0; j < 64; j++) {
                    DQT[offset++] = tempArray[JpegLUT.ZIGZAGORDER[j]];
                }
            } else {
                tempArray = JpegLUT.QC;
                for (int j = 0; j < 64; j++) {
                    DQT[offset++] = tempArray[JpegLUT.ZIGZAGORDER[j]];
                }
            }

        }
        out.write(DQT);

        // Start of Frame Header
        //        int sampleFactor [] = new int[]{1,2,2};
        writeMarker(out, Markers.SOF0);
        writeLength(out, 17);
        byte SOF[] = new byte[15];
        SOF[0] = (byte) 8;
        SOF[1] = (byte) ((imageHeight >> 8) & 0xFF);
        SOF[2] = (byte) ((imageHeight) & 0xFF);
        SOF[3] = (byte) ((imageWidth >> 8) & 0xFF);
        SOF[4] = (byte) ((imageWidth) & 0xFF);
        SOF[5] = (byte) nComp;
        index = 6;
        for (int i = 0; i < nComp; i++) {
            SOF[index++] = (byte) (i + 1);
            SOF[index++] = (byte) ((sampleFrequency[i] << 4) + sampleFrequency[i]);
            SOF[index++] = (byte) TableNumber[i];
        }
        out.write(SOF);

        // The DHT Header
        byte DHT1[], DHT2[], DHT3[], DHT4[];
        int bytes, temp, oldindex, intermediateindex;
        index = 4;
        oldindex = 4;
        DHT1 = new byte[17];
        DHT4 = new byte[4];
        DHT4[0] = (byte) 0xFF;
        DHT4[1] = (byte) 0xC4;
        for (int i = 0; i < 4; i++) {
            bytes = 0;
            DHT1[index++ - oldindex] = (byte) huffman.bitList.get(i)[0];
            for (int j = 1; j < 17; j++) {
                temp = huffman.bitList.get(i)[j];
                DHT1[index++ - oldindex] = (byte) temp;
                bytes += temp;
            }
            intermediateindex = index;
            DHT2 = new byte[bytes];
            for (int j = 0; j < bytes; j++) {
                DHT2[index++ - intermediateindex] = (byte) huffman.valList.get(i)[j];
            }
            DHT3 = new byte[index];
            System.arraycopy(DHT4, 0, DHT3, 0, oldindex);
            System.arraycopy(DHT1, 0, DHT3, oldindex, 17);
            System.arraycopy(DHT2, 0, DHT3, oldindex + 17, bytes);
            DHT4 = DHT3;
            oldindex = index;
        }
        DHT4[2] = (byte) (((index - 2) >> 8) & 0xFF);
        DHT4[3] = (byte) ((index - 2) & 0xFF);
        out.write(DHT4);

        // Start of Scan Header
        byte SOS[] = new byte[14];
        SOS[0] = (byte) 0xFF;
        SOS[1] = (byte) 0xDA;
        SOS[2] = (byte) 0x00;
        SOS[3] = (byte) 12;
        SOS[4] = (byte) nComp;
        index = 5;
        for (int i = 0; i < SOS[4]; i++) {
            SOS[index++] = (byte) (i + 1);
            SOS[index++] = (byte) ((TableNumber[i] << 4) + TableNumber[i]);
        }
        SOS[index++] = (byte) 0;
        SOS[index++] = (byte) 63;
        SOS[index] = (byte) 0;   //does not need increment as value would never be used
        out.write(SOS);

    }

    private static void writeMarker(OutputStream out, int marker) throws IOException {
        out.write((byte) marker >> 8);
        out.write((byte) marker);
    }

    private static void writeLength(OutputStream out, int length) throws IOException {
        out.write((byte) length >> 8);
        out.write((byte) length);
    }

    private static byte[] intToBytes(int value) {
        return new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
    }

//    public static int[] rgbToYCBCR(int r, int g, int b) {
//        int y = (int) (0.299f * r + 0.587f * g + 0.114f * b);
//        int cb = (int) (128 - 0.169f * r - 0.331f * g + 0.5f * b);
//        int cr = (int) (128 + 0.5f * r - 0.419f * g - 0.081f * b);
//        return new int[]{y, cb, cr};
//    }

//    public static void main(String[] args) throws Exception {
//
//        java.io.File f = new java.io.File("C:\\Users\\suda\\Desktop\\testimages\\map.jpg");
//        FileInputStream fis = new FileInputStream(f);
//        byte[] bb = new byte[(int) f.length()];
//        fis.read(bb);
//        BufferedImage ii = javax.imageio.ImageIO.read(f);
//
//        long start = System.currentTimeMillis();
//        javax.imageio.ImageIO.write(ii, "jpg", new java.io.File("C:\\Users\\suda\\Desktop\\testimages\\imageIOWrite" + System.currentTimeMillis() + ".jpg"));
//        System.out.println((System.currentTimeMillis() - start));
//
//        start = System.currentTimeMillis();
//        JpegEncoder enc = new JpegEncoder();
//        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File("C:\\Users\\suda\\Desktop\\testimages\\sudaWrite" + System.currentTimeMillis() + ".jpg")));
//        enc.write(ii, bos);
//        bos.close();
//
//        System.out.println((System.currentTimeMillis() - start));
//
//    }

}


//
