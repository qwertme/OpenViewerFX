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
 * TiffEncoder.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

import com.idrsolutions.image.jpeg.JpegDecoder;
import com.idrsolutions.image.jpeg2000.EnumeratedSpace;
import com.idrsolutions.image.jpeg2000.JPXBitReader;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Class reads Tiff images as BufferedImage
 *
 * <h3>Example 1 (single-page tiff):</h3>
 * <pre><code>
 * TiffDecoder decoder = new TiffDecoder(rawTiffData);
 * BufferedImage decodedImage = decoder.read();
 * </code></pre>
 *
 * <h3>Example 2 (multi-page tiff)</h3>
 * <pre><code>
 * TiffDecoder decoder = new TiffDecoder(rawTiffData);
 * for (int i = 0; i<= decoder.getPageCount(); i++) {
 *      BufferedImage decodedImage = decoder.read(i);
 *      // Insert BufferedImage handling code here
 * }
 * </code></pre>
 *
 * <h3>Example 3 (RandomAccessFile):</h3>
 * <strong>We recommend to use RandomAccessFile constructor for memory efficient
 * reading.</strong>
 *
 * <pre><code>
 * RandomAccessFile raf = new RandomAccessFile("yourFileLocation", "r");
 * TiffDecoder decoder = new TiffDecoder(raf);
 * for (int i = 1; i <= decoder.getPageCount(); i++) {
 *      BufferedImage decodedImage = decoder.read(i);
 *      // Insert BufferedImage handling code here
 * }
 * raf.close();
 * </code></pre>
 *
 */
public class TiffDecoder {

    private final RandomHandler reader;
    private int pageCount;
    final List<IFD> ifds = new ArrayList<IFD>();

    /**
     * Constructor generates instance from byte data <br/>
     * For memory efficient reading use other constructor with RandomAccessFile
     *
     * @throws Exception
     */
    public TiffDecoder(byte[] rawTiffData) throws Exception {
        reader = new RandomHandler(rawTiffData);

        int a = reader.getUint8();
        int b = reader.getUint8();
        if (a == 77 && b == 77) {
            reader.setByteOrder(RandomHandler.BIGENDIAN);
        } else if (a == 73 && b == 73) {
            reader.setByteOrder(RandomHandler.LITTLEENDIAN);
        }
        int magicNumber = reader.getUint16();//42 magic number;
        if (magicNumber != 42) {
            if(magicNumber == 43){
                throw new Exception("Big Tiff File Support not added");
            }else{
                throw new Exception("This is not a valid Tiff File");
            }
        }
        int ifdOffset = reader.getInt();

        while (ifdOffset != 0) {
            IFD ifd = getIFD(reader, ifdOffset);
            ifds.add(ifd);
            ifdOffset = ifd.nextIFD;
            pageCount++;
        }
    }

    /**
     * Constructor generates instance from RandomAccessFile <br/>
     * You can use this constructor for memory efficient file reading
     * <p>
     * This method does not close random access file after the read operation is
     * completed; it is the responsibility of the caller to close
     * RandomAccessFile,
     * <p>
     * @param randomAccessFile a random access file where the tiff data is
     * located
     * @throws Exception
     */
    public TiffDecoder(RandomAccessFile randomAccessFile) throws Exception {
        reader = new RandomHandler(randomAccessFile);

        int a = reader.getUint8();
        int b = reader.getUint8();
        if (a == 77 && b == 77) {
            reader.setByteOrder(RandomHandler.BIGENDIAN);
        } else if (a == 73 && b == 73) {
            reader.setByteOrder(RandomHandler.LITTLEENDIAN);
        }
        int magicNumber = reader.getUint16();//42 magic number;
        if (magicNumber != 42) {
            if(magicNumber == 43){
                throw new Exception("Big Tiff File Support not added");
            }else{
                throw new Exception("This is not a valid Tiff File");
            }
        }
        int ifdOffset = reader.getInt();

        while (ifdOffset != 0) {
            IFD ifd = getIFD(reader, ifdOffset);
            ifds.add(ifd);
            ifdOffset = ifd.nextIFD;
            pageCount++;
        }
    }

    /**
     * Decodes and returns the first Tiff image as a BufferedImage from a single or multi page tiff file.
     * <p>
     * Make NO assumptions about type of BufferedImage type returned (may change)
     *
     * @return BufferedImage The decoded image
     * @throws IOException
     */
    public BufferedImage read() throws Exception {
        return read(1);
    }

    /**
     * Decodes and returns the requested Tiff image as a BufferedImage from a multi page tiff file.
     * <p>
     * <strong>Please Note: pageNumber should start from 1</strong>
     *
     * @param pageNumber the page number to be decoded and returned (starting from 1)
     * @return BufferedImage The decoded image at given page number
     * @throws Exception
     */
    public BufferedImage read(int pageNumber) throws Exception {
        if (pageNumber == 0) {
            throw new Exception("PageNumber should start from 1");
        } else if (pageNumber > pageCount) {
            throw new Exception("PageNumber should not be greater than Total page count");
        }
        IFD ifd = ifds.get(pageNumber - 1);
        return generateImageFromIFD(reader, ifd);
    }

    private static IFD getIFD(final RandomHandler reader, int ifdOffset) throws IOException {
        reader.setPosition(ifdOffset);
        int nEntries = reader.getUint16();
        IFD ifd = new IFD();

        for (int i = 0; i < nEntries; i++) {
            int fieldName = reader.getUint16();
            int fieldType = reader.getUint16();
            int nValues = reader.getInt();
            int current;
//            System.out.println(fieldName + " " + fieldType + " " + nValues);
            switch (fieldName) {                
                case Tags.ImageWidth:
                    ifd.imageWidth = getShortOrInt(reader, fieldType);
                    break;
                case Tags.ImageHeight:
                    ifd.imageHeight = getShortOrInt(reader, fieldType);
                    break;
                case Tags.BitsPerSample:
                    ifd.bps = new int[nValues];
                    if (nValues == 1) {
                        ifd.bps[0] = reader.getUint16();
                        reader.getUint16();
                    } else if (nValues == 2) {
                        ifd.bps[0] = reader.getUint16();
                        ifd.bps[1] = reader.getUint16();
                    } else {
                        int sampleOffset = reader.getInt();
                        current = reader.getPosition();
                        ifd.bps = readBitsPerSamples(reader, sampleOffset, nValues);
                        reader.setPosition(current);
                    }
                    break;
                case Tags.Compression:
                    ifd.compressionType = reader.getUint16();
                    reader.getUint16();
                    break;
                case Tags.PhotometricInterpolation:
                    ifd.photometric = reader.getUint16();
                    reader.getUint16();
                    break;
                case Tags.RowsPerStrip:
                    ifd.rowsPerStrip = getShortOrInt(reader, fieldType);
                    break;
                case Tags.StripOffsets:
                    if (nValues == 1) {
                        ifd.stripOffsets = new int[1];
                        ifd.stripOffsets[0] = reader.getInt();
                    } else {
                        int stripOffset = reader.getInt();
                        current = reader.getPosition();
                        ifd.stripOffsets = readOffsets(reader, stripOffset, nValues, fieldType);
                        reader.setPosition(current);
                    }
                    break;
                case Tags.StripByteCounts:
                    if (nValues == 1) {
                        ifd.stripByteCounts = new int[1];
                        ifd.stripByteCounts[0] = reader.getInt();
                    } else {
                        int stripOffset = reader.getInt();
                        current = reader.getPosition();
                        ifd.stripByteCounts = readStripTileByteCounts(reader, stripOffset, nValues, fieldType);
                        reader.setPosition(current);
                    }
                    break;
                case Tags.SamplesPerPixel:
                    ifd.samplesPerPixel = reader.getUint16();
                    reader.getUint16();
                    break;
                case Tags.ColorMap:
                    int cmapOffset = reader.getInt();
                    current = reader.getPosition();
                    ifd.colorMap = readColorMap(reader, cmapOffset, nValues);
                    reader.setPosition(current);
                    break;
                case Tags.PlanarConfiguration:
                    ifd.planarConfiguration = reader.getUint16();
                    reader.getUint16();//read and ignore;
                    break;
                case Tags.TileWidth:
                    ifd.tileWidth = getShortOrInt(reader, fieldType);
                    break;
                case Tags.TileLength:
                    ifd.tileLength = getShortOrInt(reader, fieldType);
                    break;
                case Tags.TIleOffsets:
                    if (nValues == 1) {
                        ifd.tileOffsets = new int[1];
                        ifd.tileOffsets[0] = reader.getInt();
                    } else {
                        int stripOffset = reader.getInt();
                        int cur = reader.getPosition();
                        ifd.tileOffsets = readOffsets(reader, stripOffset, nValues, fieldType);
                        reader.setPosition(cur);
                    }
                    break;
                case Tags.TIleByteCounts:
                    if (nValues == 1) {
                        ifd.tileByteCounts = new int[1];
                        ifd.tileByteCounts[0] = reader.getInt();
                    } else {
                        int stripOffset = reader.getInt();
                        int cur = reader.getPosition();
                        ifd.tileByteCounts = readStripTileByteCounts(reader, stripOffset, nValues, fieldType);
                        reader.setPosition(cur);
                    }
                    break;
                case Tags.JPEGTables:
                    int tableOffset = reader.getInt();
                    int cc = reader.getPosition();
                    reader.setPosition(tableOffset);
                    byte[] tableBytes = new byte[nValues];
                    reader.get(tableBytes);
                    reader.setPosition(cc);
                    ifd.jpegTables = new byte[nValues - 2];//remove EOI
                    System.arraycopy(tableBytes, 0, ifd.jpegTables, 0, nValues - 2);
                    break;                
                case Tags.JPEGQTables:
                    if (nValues == 1) {
                        ifd.jpegQOffsets = new int[1];
                        ifd.jpegQOffsets[0] = reader.getInt();
                    } else {
                        int stripOffset = reader.getInt();
                        int cur = reader.getPosition();
                        ifd.jpegQOffsets = readOffsets(reader, stripOffset, nValues, fieldType);
                        reader.setPosition(cur);
                    }
                    break;
                case Tags.JPEGDCTables:
                    if (nValues == 1) {
                        ifd.jpegDCOffsets = new int[1];
                        ifd.jpegDCOffsets[0] = reader.getInt();
                    } else {
                        int stripOffset = reader.getInt();
                        int cur = reader.getPosition();
                        ifd.jpegDCOffsets = readOffsets(reader, stripOffset, nValues, fieldType);
                        reader.setPosition(cur);
                    }
                    break;
                case Tags.JPEGACTables:
                    if (nValues == 1) {
                        ifd.jpegACOffsets = new int[1];
                        ifd.jpegACOffsets[0] = reader.getInt();
                    } else {
                        int stripOffset = reader.getInt();
                        int cur = reader.getPosition();
                        ifd.jpegACOffsets = readOffsets(reader, stripOffset, nValues, fieldType);
                        reader.setPosition(cur);
                    }
                    break;
                case Tags.ICC:
                    int iccOffset = reader.getInt();
                    current = reader.getPosition();
                    reader.setPosition(iccOffset);
                    ifd.iccProfile = new byte[nValues];
                    reader.get(ifd.iccProfile);
                    reader.setPosition(current);
                    break;
                case Tags.FillOrder:
                    ifd.fillOrder = reader.getInt();
                    break;
                case Tags.Predictor:
                    ifd.predictor = reader.getUint16();
                    reader.getUint16();//read and ignore
                    break;
                case Tags.SampleFormat:
                    if (nValues == 1) {
                        ifd.sampleFormat = new int[1];
                        ifd.sampleFormat[0] = reader.getInt();
                    } else {
                        int sampleOffset = reader.getInt();
                        current = reader.getPosition();
                        ifd.sampleFormat = readOffsets(reader, sampleOffset, nValues, fieldType);
                        reader.setPosition(current);
                    }
                    break;
                case Tags.NewSubfileType:
                case Tags.SubfileType:
                case Tags.JPEGProc:
                case Tags.JPEGInterchangeFormat:
                case Tags.JPEGInterchangeFormatLength:
                case Tags.JPEGRestartInterval:
                case Tags.JPEGLosslessPredictors:
                case Tags.JPEGPointTransforms:
                case Tags.YCbCrCoefficients:
                case Tags.YCbCrSubSampling:
                case Tags.YCbCrPositioning:
                case Tags.Threshholding:
                case Tags.CellWidth:
                case Tags.CellLength:
                case Tags.DocumentName:
                case Tags.ImageDescription:
                case Tags.Make:
                case Tags.Model:
                case Tags.Orientation:
                case Tags.MinSampleValue:
                case Tags.MaxSampleValue:
                case Tags.Xresolution:
                case Tags.Yresolution:
                case Tags.PageName:
                case Tags.Xposition:
                case Tags.Yposition:
                case Tags.FreeOffsets:
                case Tags.FreeByteCounts:
                case Tags.GrayResponseUnit:
                case Tags.GrayResponseCurve:
                case Tags.T4Options:
                case Tags.T6Options:
                case Tags.ResolutionUnit:
                case Tags.PageNumber:
                case Tags.TransferFunction:
                case Tags.Software:
                case Tags.DateTime:
                case Tags.Artist:
                case Tags.HostComputer:
                case Tags.WhitePoint:
                case Tags.PrimaryChromaticities:
                case Tags.HalftoneHints:
                case Tags.SubIFDs:
                case Tags.InkSet:
                case Tags.InkNames:
                case Tags.NumberOfInks:
                case Tags.DotRange:
                case Tags.TargetPrinter:
                case Tags.ExtraSamples:
                case Tags.SMinSampleValue:
                case Tags.SMaxSampleValue:
                case Tags.TransferRange:
                case Tags.ClipPath:
                case Tags.XClipPathUnits:
                case Tags.YClipPathUnits:
                case Tags.Indexed:
                case Tags.ReferenceBlackWhite:
                case Tags.StripRowCounts:
                case Tags.XMP:
                case Tags.ImageID:
                case Tags.Copyright:
                case Tags.Exif_IFD:
                case Tags.ExifVersion:
                case Tags.DateTimeOriginal:
                case Tags.DateTimeDigitized:
                case Tags.ComponentConfiguration:
                case Tags.CompressedBitsPerPixel:
                case Tags.ApertureValue:
                case Tags.ImageNumber:
                case Tags.ImageHistory:
                case Tags.ColorSpace:
                case Tags.PixelXDimension:
                case Tags.PixelYDimension:
                    reader.getInt();
                    break;
                default:
                    reader.getInt();
            }
        }
        ifd.nextIFD = reader.getInt();
        //some files contains rowsperstrip as zero;
        if (ifd.rowsPerStrip == 0 || ifd.rowsPerStrip > ifd.imageHeight) {
            ifd.rowsPerStrip = ifd.imageHeight;
        }
        return ifd;
    }

    private static int getShortOrInt(RandomHandler reader, int fieldType) throws IOException {
        int value;
        if (fieldType == 3) {
            value = reader.getUint16();
            reader.getUint16();//read and ignore;
        } else {
            value = reader.getInt();
        }
        return value;
    }
    
    private static BufferedImage getDataFromStrips(final RandomHandler reader, IFD ifd) throws Exception {

        final int iw = ifd.imageWidth;
        final int ih = ifd.imageHeight;
        final int dim = iw * ih;
        final boolean isPlanar = ifd.planarConfiguration == 2;
        final boolean hasPalette = ifd.colorMap != null;
        int rps = ifd.rowsPerStrip;
        int bps = ifd.bps[0];
        int sampleLen = 0;
        final int stripSamples = isPlanar ? 1 : ifd.bps.length;
        if (isPlanar) {
            sampleLen = ifd.bps[0];
        } else {
            for (int i = 0; i < ifd.bps.length; i++) {
                sampleLen += ifd.bps[i];
            }
        }

        List<Tile> tiles = new ArrayList<Tile>();
        int planarStrip = ifd.stripOffsets.length / ifd.samplesPerPixel;
        JpegDecoder decoder;

        for (int t = 0; t < ifd.stripOffsets.length; t++) {
            reader.setPosition(ifd.stripOffsets[t]);
            byte[] tileData = new byte[ifd.stripByteCounts[t]];
            reader.get(tileData);

            final int height;
            if (isPlanar) {
                int stripMod = t % planarStrip;
                int balance = ih - (rps * stripMod);
                height = balance < rps ? balance : rps;
            } else {
                int balance = ih - (rps * t);
                height = balance < rps ? balance : rps;
            }
            byte[] output = null;
            CCITT fax;

            int expectation = ((((iw * sampleLen + 7) / 8) * 8 * height) + 7) / 8;

            switch (ifd.compressionType) {
                case Tags.Uncompressed:
                    output = tileData;
                    break;
                case Tags.CCITT_ID:
                    output = new byte[expectation]; //assume bps = 1;
                    fax = new CCITT(ifd.fillOrder, iw, height);
                    fax.decompress1D(output, tileData, 0, height);
                    break;
                case Tags.Group_3_Fax:
                    output = new byte[expectation]; //assume bps = 1;
                    fax = new CCITT(ifd.fillOrder, iw, height);
                    fax.decompressFax3(output, tileData, height);
                    break;
                case Tags.Group_4_Fax:
                    output = new byte[expectation]; //assume bps = 1;
                    fax = new CCITT(ifd.fillOrder, iw, height);
                    fax.decompressFax4(output, tileData, height);
                    break;
                case Tags.LZW:
                    output = new byte[expectation];
                    LZW lzw = new LZW();
                    lzw.decompress(output, tileData, iw, height);
                    break;
                case Tags.JPEG:
                    throw new Exception("Old style jpeg compression is not supported");
                case Tags.JPEG_TechNote:
                    if (ifd.jpegTables != null) {
                        int ifdLen = ifd.jpegTables.length;
                        byte[] temp = new byte[ifdLen + tileData.length - 2];
                        System.arraycopy(ifd.jpegTables, 0, temp, 0, ifdLen);
                        System.arraycopy(tileData, 2, temp, ifdLen, tileData.length - 2);
                        tileData = temp;
                    }
                    decoder = new JpegDecoder();
                    output = decoder.readComponentsAsRawBytes(tileData);
                    break;
                case Tags.PackBits:
                    int exp = (iw * height * sampleLen + 7) / 8;
                    output = PackBits.decompress(tileData, exp);
                    break;
                case Tags.ADOBEDEFLATE:
                case Tags.Deflate:
                    output = Deflate.decompress(tileData);
                    break;
                default:
                    System.err.println("unrecognized compression found");
            }
            
            if (ifd.predictor == 2) {
                int count;
                for (int j = 0; j < height; j++) {
                    count = stripSamples * (j * iw + 1);
                    for (int i = stripSamples; i < iw * stripSamples; i++) {
                        output[count] += output[count - stripSamples];
                        count++;
                    }
                }
            }

            //invert the colors to make white is zero;
            int n = 0;
            if (ifd.photometric == Tags.WhiteIsZero) {
                for (int i = 0; i < output.length; i++) {
                    output[i] = (byte) ((output[n++] & 0xff) ^ 0xff);
                }
            }
            //now normalize the tiles
            Tile tile = new Tile();
            int bp = 0;
            int iw8 = (iw * bps * stripSamples) % 8;

            if (bps != 8) {
                tileData = new byte[height * iw * stripSamples];
                JPXBitReader bitReader = new JPXBitReader(output);
                if (bps == 1) {
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < (iw - 1); j++) {
                            for (int k = 0; k < stripSamples; k++) {
                                if (hasPalette) {
                                    tileData[bp++] = (byte) (bitReader.readBits(bps));
                                } else {
                                    tileData[bp++] = (byte) (bitReader.readBits(bps) * 255);
                                }
                            }
                        }
                        if (iw8 != 0) {
                            bitReader.readBits(8 - iw8);
                        }
                    }
                } else if (bps < 8) {
                    int shift = 8 - bps;
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < iw; j++) {
                            for (int k = 0; k < stripSamples; k++) {
                                if (hasPalette) {
                                    tileData[bp++] = (byte) (bitReader.readBits(bps));
                                } else {
                                    tileData[bp++] = (byte) (bitReader.readBits(bps) << shift);
                                }
                            }
                        }
                        if (iw8 != 0) {
                            bitReader.readBits(8 - iw8);
                        }
                    }
                } else {
                    int shift = bps - 8;
                    int sampleFormat = ifd.sampleFormat[0];
                    if (hasPalette) {
                        tileData = new byte[height * iw * 3];
                        for (int i = 0; i < height; i++) {
                            for (int j = 0; j < iw; j++) {
                                int v = bitReader.readBits(bps) * 3;
                                tileData[bp++] = ifd.colorMap[v++];
                                tileData[bp++] = ifd.colorMap[v++];
                                tileData[bp++] = ifd.colorMap[v];
                            }
                            if (iw8 != 0) {
                                bitReader.readBits(8 - iw8);
                            }
                        }
                    } else {
                        for (int i = 0; i < height; i++) {
                            for (int j = 0; j < iw; j++) {
                                for (int k = 0; k < stripSamples; k++) {
                                    if (sampleFormat == 3) {
                                        tileData[bp++] = (byte) (toFloat(bitReader.readBits(bps), bps) * 255);
                                    } else {
                                        tileData[bp++] = (byte) (bitReader.readBits(bps) >> shift);
                                    }
                                }
                            }
                            if (iw8 != 0) {
                                bitReader.readBits(8 - iw8);
                            }
                        }
                    }

                }
            } else {
                tileData = output;
            }
            tile.data = tileData;
            tiles.add(tile);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (isPlanar) {

            int spp = ifd.samplesPerPixel;
            byte[][] planars = new byte[spp][dim];

            int n = 0;
            int iter = tiles.size() / spp;

            for (int s = 0; s < spp; s++) {
                for (int i = 0; i < iter; i++) {
                    bos.write(tiles.get(n++).data);
                }
                planars[s] = bos.toByteArray();
                bos.reset();
            }

            for (int i = 0; i < dim; i++) {
                for (int s = 0; s < spp; s++) {
                    bos.write(planars[s][i]);
                }
            }
        } else {
            for (Tile tile : tiles) {
                bos.write(tile.data);
            }
        }

        BufferedImage image = allocateBufferedImage(ifd);
        int[] intPixels;
        byte[] bytePixels;
        int r, g, b, a, yy, cb, cr, bp = 0;
        byte c, m, y, k;
        byte[] data = bos.toByteArray();

        switch (ifd.photometric) {
            case Tags.YCbCr:
                intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                for (int i = 0; i < dim; i++) {
                    yy = data[bp++] & 0xff;
                    cb = data[bp++] & 0xff;
                    cr = data[bp++] & 0xff;

                    cb -= 128;
                    cr -= 128;

                    int u2 = cb >> 2;
                    int v35 = (cr >> 3) + (cr >> 5);

                    r = yy + cr + (cr >> 2) + v35;
                    g = yy - (u2 + (cb >> 4) + (cb >> 5)) - ((cr >> 1) + v35 + (cr >> 4));
                    b = yy + cb + (cb >> 1) + u2 + (cb >> 6);

                    r = r < 0 ? 0 : r > 255 ? 255 : r;
                    g = g < 0 ? 0 : g > 255 ? 255 : g;
                    b = b < 0 ? 0 : b > 255 ? 255 : b;
                    intPixels[i] = r << 16 | g << 8 | b;
                }
                break;
            case Tags.RGB:
                intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                if (ifd.samplesPerPixel == 3) {
                    for (int i = 0; i < dim; i++) {
                        r = data[bp++] & 0xff;
                        g = data[bp++] & 0xff;
                        b = data[bp++] & 0xff;
                        intPixels[i] = r << 16 | g << 8 | b;
                    }
                } else { //argb;
                    for (int i = 0; i < dim; i++) {
                        r = data[bp++] & 0xff;
                        g = data[bp++] & 0xff;
                        b = data[bp++] & 0xff;
                        a = data[bp++] & 0xff;
                        intPixels[i] = a << 24 | r << 16 | g << 8 | b;
                    }
                }
                break;
            case Tags.CMYK:
                intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                EnumeratedSpace cmyk = new EnumeratedSpace();
                for (int i = 0; i < dim; i++) {
                    c = data[bp++];
                    m = data[bp++];
                    y = data[bp++];
                    k = data[bp++];
                    byte[] rgb = cmyk.getRGB(c, m, y, k);
                    intPixels[i] = (rgb[0] & 0xff) << 16 | (rgb[1] & 0xff) << 8 | (rgb[2] & 0xff);
                }
                break;
            case Tags.RGB_Palette:
                if (ifd.bps[0] > 8) {
                    intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                    for (int i = 0; i < dim; i++) {
                        r = data[bp++] & 0xff;
                        g = data[bp++] & 0xff;
                        b = data[bp++] & 0xff;
                        intPixels[i] = r << 16 | g << 8 | b;
                    }
                } else {
                    bytePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                    int min = Math.min(bytePixels.length, data.length);
                    System.arraycopy(data, 0, bytePixels, 0, min);
                }
                break;
            default:
                bytePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                int min = Math.min(bytePixels.length, data.length);
                System.arraycopy(data, 0, bytePixels, 0, min);
                break;
        }

        return image;

    }

    private static BufferedImage getImageFromTiles(RandomHandler reader, IFD ifd) throws Exception {
        //some odd files may not contain tile offsets but contain tile info
        if (ifd.tileOffsets == null) {
            ifd.tileOffsets = ifd.stripOffsets;
        }
        if (ifd.tileByteCounts == null) {
            ifd.tileByteCounts = ifd.stripByteCounts;
        }

        final int iw = ifd.imageWidth;
        final int ih = ifd.imageHeight;
        final int tw = ifd.tileWidth;
        final int th = ifd.tileLength;
        final boolean isPlanar = ifd.planarConfiguration == 2;
        int tileComp = isPlanar ? 1 : ifd.samplesPerPixel;
        final boolean hasPalette = ifd.colorMap != null;

        final int xTiles = (iw + (tw - 1)) / tw;
        final int yTiles = (ih + (th - 1)) / th;
        final int totalTiles = ifd.tileOffsets.length;

        int bps = ifd.bps[0];

        int sampleLen = 0;
        if (ifd.planarConfiguration == 2) {
            sampleLen = ifd.bps[0];
        } else {
            for (int i = 0; i < ifd.bps.length; i++) {
                sampleLen += ifd.bps[i];
            }
        }

        final List<Tile> tiles = new ArrayList<Tile>();

        for (int t = 0; t < totalTiles; t++) {
            reader.setPosition(ifd.tileOffsets[t]);
            byte[] tileData = new byte[ifd.tileByteCounts[t]];
            reader.get(tileData);
            byte[] output = null;
            CCITT fax;
            JpegDecoder decoder;

            int expectation = ((((tw * sampleLen + 7) / 8) * 8 * th) + 7) / 8;

            switch (ifd.compressionType) {
                case Tags.Uncompressed:
                    output = tileData;
                    break;
                case Tags.CCITT_ID:
                    output = new byte[expectation]; //assume bps = 1;
                    fax = new CCITT(ifd.fillOrder, tw, th);
                    fax.decompress1D(output, tileData, 0, th);
                    break;
                case Tags.Group_3_Fax:
                    output = new byte[expectation]; //assume bps = 1;
                    fax = new CCITT(ifd.fillOrder, tw, th);
                    fax.decompressFax3(output, tileData, th);
                    break;
                case Tags.Group_4_Fax:
                    output = new byte[expectation]; //assume bps = 1;
                    fax = new CCITT(ifd.fillOrder, tw, th);
                    fax.decompressFax4(output, tileData, th);
                    break;
                case Tags.LZW:
                    output = new byte[expectation];
                    LZW lzw = new LZW();
                    lzw.decompress(output, tileData, tw, th);
                    break;
                case Tags.JPEG:
                    throw new Exception("Old style jpeg compression is not supported");
                case Tags.JPEG_TechNote:
                    if (ifd.jpegTables != null) {
                        int ifdLen = ifd.jpegTables.length;
                        byte[] temp = new byte[ifdLen + tileData.length - 2];
                        System.arraycopy(ifd.jpegTables, 0, temp, 0, ifdLen);
                        System.arraycopy(tileData, 2, temp, ifdLen, tileData.length - 2);
                        tileData = temp;
                    }
                    decoder = new JpegDecoder();
                    output = decoder.readComponentsAsRawBytes(tileData);
                    break;
                case Tags.PackBits:
                    int exp = (tw * th * sampleLen + 7) / 8;
                    output = PackBits.decompress(tileData, exp);
                    break;
                case Tags.ADOBEDEFLATE:
                case Tags.Deflate:
                    output = Deflate.decompress(tileData);
                    break;
                default:
                    System.err.println("unrecognized compression found");
            }
            
            if (ifd.predictor == 2) {
                int count;
                for (int j = 0; j < th; j++) {
                    count = tileComp * (j * tw + 1);
                    for (int i = tileComp; i < tw * tileComp; i++) {
                        output[count] += output[count - tileComp];
                        count++;
                    }
                }
            }

            //invert the colors to make white is zero;
            if (ifd.photometric == Tags.WhiteIsZero) {
                int n = 0;
                for (int i = 0; i < output.length; i++) {
                    output[i] = (byte) ((output[n++] & 0xff) ^ 0xff);
                }
            }

            Tile tile = new Tile();
            int iw8 = (tw * bps * tileComp) % 8;

            if (bps != 8) {
                tileData = new byte[th * tw * tileComp];
                JPXBitReader bitReader = new JPXBitReader(output);
                int bp = 0;
                if (bps == 1) {
                    for (int i = 0; i < th; i++) {
                        for (int j = 0; j < tw; j++) {
                            for (int k = 0; k < tileComp; k++) {
                                if (hasPalette) {
                                    tileData[bp++] = (byte) (bitReader.readBits(bps));
                                } else {
                                    tileData[bp++] = (byte) (bitReader.readBits(bps) * 255);
                                }
                            }
                        }
                        if (iw8 != 0) {
                            bitReader.readBits(8 - iw8);
                        }
                    }
                } else if (bps < 8) {
                    int shift = 8 - bps;
                    for (int i = 0; i < th; i++) {
                        for (int j = 0; j < tw; j++) {
                            for (int k = 0; k < tileComp; k++) {
                                if (hasPalette) {
                                    tileData[bp++] = (byte) (bitReader.readBits(bps));
                                } else {
                                    tileData[bp++] = (byte) (bitReader.readBits(bps) << shift);
                                }
                            }
                        }
                        if (iw8 != 0) {
                            bitReader.readBits(8 - iw8);
                        }
                    }
                } else {
                    int shift = bps - 8;
                    int sampleFormat = ifd.sampleFormat[0];

                    if (hasPalette) {
                        tileData = new byte[th * tw * 3];
                        tileComp = 3;
                        for (int i = 0; i < th; i++) {
                            for (int j = 0; j < tw; j++) {
                                int v = bitReader.readBits(bps) * 3;
                                tileData[bp++] = ifd.colorMap[v++];
                                tileData[bp++] = ifd.colorMap[v++];
                                tileData[bp++] = ifd.colorMap[v];
                            }
                            if (iw8 != 0) {
                                bitReader.readBits(8 - iw8);
                            }
                        }
                    } else {
                        for (int i = 0; i < th; i++) {
                            for (int j = 0; j < tw; j++) {
                                for (int k = 0; k < tileComp; k++) {
                                    if (sampleFormat == 3) {
                                        tileData[bp++] = (byte) (toFloat(bitReader.readBits(bps), bps) * 255);
                                    } else {
                                        tileData[bp++] = (byte) (bitReader.readBits(bps) >> shift);
                                    }
                                }
                            }
                            if (iw8 != 0) {
                                bitReader.readBits(8 - iw8);
                            }
                        }
                    }

                }
            } else {
                tileData = output;
            }
            tile.data = tileData;
            tiles.add(tile);
        }

        int p, q, tx, ty, bp;

        int[][] tileDim = new int[th * yTiles][tw * xTiles];

        if (isPlanar) {
            int tp = 0;
            int planarTiles = tiles.size() / ifd.samplesPerPixel;

            for (int z = 0; z < ifd.samplesPerPixel; z++) {
                for (int t = 0; t < planarTiles; t++) {
                    Tile tile = tiles.get(tp++);
                    byte[] output = tile.data;
                    p = t % xTiles;
                    q = t / xTiles;
                    tx = p * tw;
                    ty = q * th;
                    bp = 0;

                    for (int i = 0; i < th; i++) {
                        int iPos = ty + i;
                        for (int j = 0; j < tw; j++) {
                            int jPos = tx + j;
                            int value = (output[bp++] & 0xff);
                            tileDim[iPos][jPos] = (tileDim[iPos][jPos] << 8) | value;
                        }
                    }
                }
            }

        } else {
            for (int t = 0; t < tiles.size(); t++) {
                Tile tile = tiles.get(t);
                byte[] output = tile.data;
                p = t % xTiles;
                q = t / xTiles;
                tx = p * tw;
                ty = q * th;
                bp = 0;

                for (int i = 0; i < th; i++) {
                    int iPos = ty + i;
                    for (int j = 0; j < tw; j++) {
                        int jPos = tx + j;
                        int value = 0;
                        for (int k = tileComp; k > 0; k--) {
                            value |= ((output[bp++] & 0xff) << (8 * (k - 1)));
                        }
                        tileDim[iPos][jPos] = value;
                    }
                }
            }
        }

        if (ifd.samplesPerPixel == 4 && ifd.photometric == Tags.RGB) { // convert : rgba data >> argb data
            for (int i = 0; i < tileDim.length; i++) {
                for (int j = 0; j < tileDim[0].length; j++) {
                    int value = tileDim[i][j];
                    int r = value >> 24 & 0xff;
                    int g = value >> 16 & 0xff;
                    int b = value >> 8 & 0xff;
                    int a = value & 0xff;
                    tileDim[i][j] = a << 24 | r << 16 | g << 8 | b;
                }
            }
        }

        BufferedImage image = allocateBufferedImage(ifd);
        bp = 0;
        byte c, m, y, k;
        int yy, cb, cr, r, g, b;
        int[] intPixels;

        switch (ifd.photometric) {
            case Tags.YCbCr:
                intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                for (int i = 0; i < ih; i++) {
                    for (int j = 0; j < iw; j++) {
                        int val = tileDim[i][j];
                        yy = (val >> 16) & 0xff;
                        cb = (val >> 8) & 0xff;
                        cr = val & 0xff;

                        cb -= 128;
                        cr -= 128;

                        int u2 = cb >> 2;
                        int v35 = (cr >> 3) + (cr >> 5);

                        r = yy + cr + (cr >> 2) + v35;
                        g = yy - (u2 + (cb >> 4) + (cb >> 5)) - ((cr >> 1) + v35 + (cr >> 4));
                        b = yy + cb + (cb >> 1) + u2 + (cb >> 6);

                        r = r < 0 ? 0 : r > 255 ? 255 : r;
                        g = g < 0 ? 0 : g > 255 ? 255 : g;
                        b = b < 0 ? 0 : b > 255 ? 255 : b;
                        intPixels[bp++] = r << 16 | g << 8 | b;
                    }
                }
                break;
            case Tags.RGB:
                intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                for (int i = 0; i < ih; i++) {
                    for (int j = 0; j < iw; j++) {
                        intPixels[bp++] = tileDim[i][j];
                    }
                }
                break;
            case Tags.CMYK:
                intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                EnumeratedSpace cmyk = new EnumeratedSpace();
                for (int i = 0; i < ih; i++) {
                    for (int j = 0; j < iw; j++) {
                        int val = tileDim[i][j];
                        c = (byte) (val >> 24 & 0xff);
                        m = (byte) (val >> 16 & 0xff);
                        y = (byte) (val >> 8 & 0xff);
                        k = (byte) (val & 0xff);
                        byte[] rgb = cmyk.getRGB(c, m, y, k);
                        intPixels[bp++] = (rgb[0] & 0xff) << 16 | (rgb[1] & 0xff) << 8 | (rgb[2] & 0xff);
                    }
                }
                break;
            case Tags.RGB_Palette:
                if (ifd.bps[0] > 8) {
                    intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                    for (int i = 0; i < ih; i++) {
                        for (int j = 0; j < iw; j++) {
                            intPixels[bp++] = tileDim[i][j];
                        }
                    }
                } else {
                    byte[] bytePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                    for (int i = 0; i < ih; i++) {
                        for (int j = 0; j < iw; j++) {
                            bytePixels[bp++] = (byte) tileDim[i][j];
                        }
                    }
                }
                break;
            default:
                byte[] bytePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                for (int i = 0; i < ih; i++) {
                    for (int j = 0; j < iw; j++) {
                        bytePixels[bp++] = (byte) tileDim[i][j];
                    }
                }
                break;
        }
        return image;

    }

    private static BufferedImage generateImageFromIFD(final RandomHandler reader, IFD ifd) throws Exception {

        if (ifd.tileWidth != 0) {
            return getImageFromTiles(reader, ifd);
        } else {
            return getDataFromStrips(reader, ifd);
        }

    }

    private static BufferedImage allocateBufferedImage(IFD ifd) {
        int imageWidth = ifd.imageWidth;
        int imageHeight = ifd.imageHeight;
        if (ifd.photometric == Tags.RGB_Palette) {
            if (ifd.bps[0] > 8) {
                return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            } else {
                IndexColorModel indexedCM = new IndexColorModel(8, ifd.colorMap.length / 3, ifd.colorMap, 0, false);
                WritableRaster ras = indexedCM.createCompatibleWritableRaster(ifd.imageWidth, ifd.imageHeight);
                return new BufferedImage(indexedCM, ras, false, null);
            }
        }

        switch (ifd.samplesPerPixel) {
            case 0:
            case 1:
            case 2:
                return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
            case 3:
                return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            case 4:
                if (ifd.photometric == Tags.CMYK) {
                    return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
                } else {
                    return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                }
            default:
                return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        }
    }

    private static int[] readBitsPerSamples(final RandomHandler reader, int offset, int nSamples) throws IOException {
        reader.setPosition(offset);
        int temp[] = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            temp[i] = reader.getUint16();
        }
        return temp;
    }

    private static int[] readOffsets(final RandomHandler reader, int offset, int nOffsets, int fieldType) throws IOException {
        reader.setPosition(offset);
        int temp[] = new int[nOffsets];
        if (fieldType == 3) {
            for (int i = 0; i < nOffsets; i++) {
                temp[i] = reader.getUint16();
            }
        } else {
            for (int i = 0; i < nOffsets; i++) {
                temp[i] = reader.getInt();
            }
        }
        return temp;
    }

    private static int[] readStripTileByteCounts(final RandomHandler reader, int offset, int nCount, int fieldType) throws IOException {
        reader.setPosition(offset);
        int temp[] = new int[nCount];
        if (fieldType == 3) {
            for (int i = 0; i < nCount; i++) {
                temp[i] = reader.getUint16();
            }
        } else {
            for (int i = 0; i < nCount; i++) {
                temp[i] = reader.getInt();
            }
        }
        return temp;
    }

    private static byte[] readColorMap(final RandomHandler reader, int cmapOffset, int nValues) throws IOException {
        reader.setPosition(cmapOffset);
        int totalColors = nValues / 3;

        byte rr[] = new byte[totalColors];
        byte gg[] = new byte[totalColors];
        byte bb[] = new byte[totalColors];

        for (int j = 0; j < totalColors; j++) {
            int sv = reader.getUint16();
            rr[j] = (byte) (sv >> 8);
        }
        for (int j = 0; j < totalColors; j++) {
            int sv = reader.getUint16();
            gg[j] = (byte) (sv >> 8);
        }
        for (int j = 0; j < totalColors; j++) {
            int sv = reader.getUint16();
            bb[j] = (byte) (sv >> 8);
        }

        byte temp[] = new byte[nValues];
        int p = 0;
        for (int i = 0; i < totalColors; i++) {
            temp[p++] = rr[i];
            temp[p++] = gg[i];
            temp[p++] = bb[i];
        }
        return temp;
    }

    /**
     * Returns the number of pages the tiff file contains.
     * @return The number of pages the tiff file contains.
     */
    public int getPageCount() {
        return pageCount;
    }

    private static float toFloat(int hbits, int bps) {
        if (bps == 16) {
            int mant = hbits & 0x03ff;
            int exp = hbits & 0x7c00;
            if (exp == 0x7c00) {
                exp = 0x3fc00;
            } else if (exp != 0) {
                exp += 0x1c000;
                if (mant == 0 && exp > 0x1c400) {
                    return Float.intBitsToFloat((hbits & 0x8000) << 16 | exp << 13 | 0x3ff);
                }
            } else if (mant != 0) {
                exp = 0x1c400;
                do {
                    mant <<= 1;
                    exp -= 0x400;
                } while ((mant & 0x400) == 0);
                mant &= 0x3ff;
            }
            return Float.intBitsToFloat((hbits & 0x8000) << 16 | (exp | mant) << 13);
        } else {
            return Float.intBitsToFloat(hbits);
        }

    }
    
    
        
//        if (ifd.compressionType == Tags.JPEG) {
//            ifd.jpegQData = new byte[ifd.jpegQOffsets.length][64];
//            for (int i = 0; i < ifd.jpegQOffsets.length; i++) {
//                reader.setPosition(ifd.jpegQOffsets[i]);
//                byte[] temp = new byte[64];
//                reader.get(temp);
//                System.arraycopy(temp, 0, ifd.jpegQData[i], 0, 64);
//            }
//
//            //dc
//            ifd.jpegDCData = new byte[ifd.jpegDCOffsets.length][];
//            for (int i = 0; i < ifd.jpegDCOffsets.length; i++) {
//                reader.setPosition(ifd.jpegDCOffsets[i]);
//                int codeLength = 0;
//                for (int j = 0; j < 16; j++) {
//                    codeLength += reader.getUint8();
//                }
//                int tableLen = codeLength + 16;
//                ifd.jpegDCData[i] = new byte[tableLen];
//                byte temp[] = new byte[tableLen];
//                reader.setPosition(ifd.jpegDCOffsets[i]);
//                reader.get(temp);
//                System.arraycopy(temp, 0, ifd.jpegDCData[i], 0, tableLen);
//            }
//
//            //ac
//            ifd.jpegACData = new byte[ifd.jpegACOffsets.length][];
//            for (int i = 0; i < ifd.jpegACOffsets.length; i++) {
//                reader.setPosition(ifd.jpegACOffsets[i]);
//                int codeLength = 0;
//                for (int j = 0; j < 16; j++) {
//                    codeLength += reader.getUint8();
//                }
//                int tableLen = codeLength + 16;
//                ifd.jpegACData[i] = new byte[tableLen];
//                byte temp[] = new byte[tableLen];
//                reader.setPosition(ifd.jpegACOffsets[i]);
//                reader.get(temp);
//                System.arraycopy(temp, 0, ifd.jpegACData[i], 0, tableLen);
//            }
//        }
    

//    may be useful in future
//    private static byte[] generateOldJPEGFile(IFD ifd, byte[] data, int tw, int th, int tComp) throws IOException {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        //SOI
//        writeShort(bos, Markers.SOI);
//
//        //SOF
//        writeShort(bos, Markers.SOF0); // support only baseline now
//        int frameLen = 8 + 3 * tComp;
//        writeShort(bos, frameLen);
//        bos.write(ifd.bps[0]);
//        writeShort(bos, th);
//        writeShort(bos, tw);
//        bos.write(tComp);
//        for (int i = 0; i < tComp; i++) {
//            bos.write(i);
//            bos.write((ifd.jpegFrequency[i] << 4) + ifd.jpegFrequency[i]);
//            bos.write(i); //planar should be handled differently
//        }
//
//        //DQT        
//        for (int i = 0; i < ifd.jpegQOffsets.length; i++) {
//            writeShort(bos, Markers.DQT);
//            int dqtLen = 2 + 1 + 64;
//            writeShort(bos, dqtLen);
//            bos.write(i);
//            bos.write(ifd.jpegQData[i]);
//        }
//
//        //DHT - DC
//        for (int i = 0; i < ifd.jpegDCOffsets.length; i++) {
//            writeShort(bos, Markers.DHT);
//            int dhtLen = 3 + ifd.jpegDCData[i].length;
//            writeShort(bos, dhtLen);
//            bos.write(i);
//            bos.write(ifd.jpegDCData[i]);
//        }
//
//        //DHT - AC
//        for (int i = 0; i < ifd.jpegACOffsets.length; i++) {
//            writeShort(bos, Markers.DHT);
//            int dhtLen = 3 + ifd.jpegACData[i].length;
//            writeShort(bos, dhtLen);
//            bos.write( 16 + i);
//            bos.write(ifd.jpegACData[i]);
//        }
//        
//        //SOS
//        writeShort(bos, Markers.SOS);
//        int scanLen = 6 + 2 * tComp;
//        writeShort(bos, scanLen);
//        bos.write(tComp);
//        for (int i = 0; i < tComp; i++) {
//            bos.write(i);
//            int dcac = i == 0 ? i : (16 + i);
//            bos.write(dcac);
//        }
//        System.out.println(data.length);
//        
//        bos.write(0);
//        bos.write(63);        
//        bos.write(0);
//        //write data
//        bos.write(data);
//        //EOI
//        writeShort(bos, Markers.EOI);
//        bos.close();
//        System.out.println(bos.toByteArray().length);
//        return bos.toByteArray();
//    }
//
//    private static void writeShort(OutputStream out, int value) throws IOException {
//        out.write((byte) (value >> 8));
//        out.write((byte) value);
//    }
}
