/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idrsolutions.image.jpeg2000;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 * Class reads Jpeg2000 images as BufferedImage
 *
 * <h3>Example:</h3>
 * <pre><code>
 * Jpeg2000Decoder decoder = new Jpeg2000Decoder();
 * // Make NO assumptions about type of BufferedImage type returned (may change)
 * BufferedImage decodedImage = decoder.read(jpxByteData);
 * </code></pre>
 *
 */
public class Jpeg2000Decoder {

    private static final boolean debug = false;

    /**
     * Decodes and returns the Jpeg2000 image as a BufferedImage.
     * <p>
     * Make NO assumptions about type of BufferedImage type returned (may change)
     *
     * @param jpxRawData A byte[] array containing the JPEG2000 data
     * @return BufferedImage The decoded image
     * @throws Exception
     */
    public BufferedImage read(final byte[] jpxRawData) throws Exception {
        final Info info = new Info();
        final JPXReader reader = new JPXReader(jpxRawData);
        if (Markers.SOC == ((jpxRawData[0] & 0xff) << 8 | (jpxRawData[1] & 0xff))) {
            readCodeStream(info, reader, jpxRawData.length);
        } else {
            decodeMain(info, reader);
            decodeContiguousCodeStreamBoxes(info, reader);
        }
        generateTileMap(info);
        decodeTileOffsets(info, reader);
        if (info.palette != null && info.siz.nComp == 1) {
            return convertPalette(info);
        } else if (info.enumerateCS == Info.CS_CMYK) {
            return convertCMYKTileToRGB(info, true);
        } else {
            return convertTileComponentsToBuffered(info);
        }
    }

    private static void decodeMain(Info info, JPXReader reader) {

        final int signLen = reader.readInt();
        final int signType = reader.readInt();
        if (signLen != 12 && signType != Boxes.JP) {
            throw new RuntimeException("Jpeg2000 Error: Not a valid jp2 file ");
        }
        reader.readInt(); //read signature content
        long ftypLen = reader.readInt();
        final int ftypType = reader.readInt();
        if (ftypType != Boxes.FTYP) {
            throw new RuntimeException("Jpeg2000 Error: Not a valid filetype declared in file ");
        }
        boolean isJP2BRFound = false;
        long remaining;
        if (ftypLen == 1) {
            ftypLen = reader.readLong();
            remaining = ftypLen - 16;
        } else {
            remaining = ftypLen - 8;
        }

        final long size = remaining / 4;
        for (int i = 0; i < size; i++) {
            if (reader.readInt() == Boxes.JP2) {
                isJP2BRFound = true;
            }
        }
        if (!isJP2BRFound) {
            throw new RuntimeException("Jpeg2000 Error: Not a valid JP2 Branded File");
        }

        boolean hasBoxes = true;

        while (reader.getRemaining() > 0 && hasBoxes) {

            final int offset = reader.getPosition();
            long tempLen = reader.readInt();
            final int tempType = reader.readInt();
            if (tempLen == 1) {
                tempLen = reader.readLong();
            } else if (tempLen == 0) {
                hasBoxes = false;
            }

            switch (tempType) {
                case Boxes.JP2H:
                    //read image header box first
                    long tLen = reader.readInt();
                    int tType;
                    reader.readInt(); //initial tType
                    if (tLen == 1) {
                       reader.readLong();  //read next
                    }
                    info.imageHeight = reader.readInt();
                    info.imageWidth = reader.readInt();
                    info.nComp = reader.readUShort();
                    info.bitDepth = reader.readUByte();
                    info.compressionType = reader.readUByte();
                    info.unknownColorSpace = reader.readUByte();
                    info.ip = reader.readUByte();

                    final long ii = offset + tempLen;

                    while (reader.getPosition() < ii) {
                        final int start = reader.getPosition();
                        tLen = reader.readInt();
                        tType = reader.readInt();
                        if (tLen == 1) {
                            reader.readLong();//tLen = 
                        }
                        switch (tType) {
                            case Boxes.BPCC:
                                info.bitDepths = new byte[info.nComp];
                                for (int i = 0; i < info.bitDepths.length; i++) {
                                    info.bitDepths[i] = reader.readByte();
                                }
                                break;
                            case Boxes.COLR:
                                final int m = reader.readUByte();
                                reader.readByte();//int p = 
                                reader.readUByte();//int a = 
                                if (m == 1) {
                                    info.enumerateCS = reader.readInt();
                                } else if (m == 2) {
                                    reader.readInt(); //restricted ICC;
                                }
                                if (debug) {
                                    System.err.println("has ICC box");
                                }
                                reader.setPosition((int) (start + tLen));
                                break;
                            case Boxes.PCLR:
                                final Palette pal = new Palette();
                                pal.nEntries = reader.readUShort();
                                pal.nColumns = reader.readUByte();
                                pal.bitDepts = new int[pal.nColumns];
                                for (int i = 0; i < pal.nColumns; i++) {
                                    pal.bitDepts[i] = reader.readUByte();
                                }
                                pal.cValues = new int[pal.nEntries][pal.nColumns];

                                for (int i = 0; i < pal.nEntries; i++) {
                                    for (int j = 0; j < pal.nColumns; j++) {
                                        pal.cValues[i][j] = reader.readUByte();
                                    }
                                }
                                info.palette = pal;
                                if (debug) {
                                    System.err.println("has PALLETTE box");
                                }
                                reader.setPosition((int) (start + tLen));
                                break;
                            case Boxes.CMAP:
                                int mapLen = (int) (tLen - (reader.getPosition() - start));
                                mapLen /= 4;
                                final Cmap cmap = new Cmap();
                                cmap.cmp = new int[mapLen];
                                cmap.mtyp = new int[mapLen];
                                cmap.pcol = new int[mapLen];
                                for (int i = 0; i < mapLen; i++) {
                                    cmap.cmp[i] = reader.readUShort();
                                    cmap.mtyp[i] = reader.readUByte();
                                    cmap.pcol[i] = reader.readUByte();
                                }
                                info.cmap = cmap;

                                if (debug) {
                                    System.err.println("has CMAP box");
                                }
                                reader.setPosition((int) (start + tLen));
                                break;
                            case Boxes.CDEF:
                                if (debug) {
                                    System.err.println("has CDef box");
                                }
                                int nDef = reader.readShort();
                                for (int i = 0; i < nDef; i++) {
                                    int key = reader.readShort();
                                    int type = reader.readShort();
                                    int val = reader.readShort();
                                    if (type == 0) {
                                        info.cDef.put(key, val);
                                    }
                                }
                                reader.setPosition((int) (start + tLen));
                                break;
                            case Boxes.RES:
                                if (debug) {
                                    System.err.println("has resolutions box");
                                }
                                reader.setPosition((int) (start + tLen));
                                break;
                            default:
                                reader.setPosition((int) (start + tLen));

                        }
                    }
                    reader.setPosition((int) ii);
                    break;
                case Boxes.JP2C:
                    info.contiguousCodeStreamBoxes.add(offset);
                    reader.setPosition((int) (offset + tempLen));
                    break;
                case Boxes.JP2I:
                    reader.setPosition((int) (offset + tempLen));
                    break;
                case Boxes.XML:
                    reader.setPosition((int) (offset + tempLen));
                    break;
                case Boxes.UUID:
                    reader.setPosition((int) (offset + tempLen));
                    break;
                case Boxes.UINF:
                    reader.setPosition((int) (offset + tempLen));
                    break;
                default:
                    if (debug) {
                        System.out.println("undefined header found " + tempType);
                    }
                    reader.setPosition((int) (offset + tempLen));
                    break;

            }
        }
    }

    private static void generateTileMap(Info info) {
        final SIZ siz = info.siz;

        final int numXTiles = (int) Math.ceil(1.0 * (siz.Xsiz - siz.XTOsiz) / siz.XTsiz);
        final int numYTiles = (int) Math.ceil(1.0 * (siz.Ysiz - siz.YTOsiz) / siz.YTsiz);

        int index = 0;
        for (int q = 0; q < numYTiles; q++) {
            for (int p = 0; p < numXTiles; p++) {
                final Tile tile = new Tile();
                tile.tx0 = Math.max(siz.XTOsiz + p * siz.XTsiz, siz.XOsiz);
                tile.ty0 = Math.max(siz.YTOsiz + q * siz.YTsiz, siz.YOsiz);
                tile.tx1 = Math.min(siz.XTOsiz + (p + 1) * siz.XTsiz, siz.Xsiz);
                tile.ty1 = Math.min(siz.YTOsiz + (q + 1) * siz.YTsiz, siz.Ysiz);

                for (int i = 0; i < siz.nComp; i++) {
                    final int XRsiz_ = siz.precisionInfo[i][1];
                    final int YRsiz_ = siz.precisionInfo[i][2];
                    final TileComponent tileComp = new TileComponent();
                    tileComp.x0 = (int) Math.ceil(1.0 * tile.tx0 / XRsiz_);
                    tileComp.x1 = (int) Math.ceil(1.0 * tile.tx1 / XRsiz_);
                    tileComp.y0 = (int) Math.ceil(1.0 * tile.ty0 / YRsiz_);
                    tileComp.y1 = (int) Math.ceil(1.0 * tile.ty1 / YRsiz_);

                    tile.components.add(tileComp);
                }
                info.tilesMap.put(index, tile);
                index++;
            }
        }
    }

    private static void decodeContiguousCodeStreamBoxes(Info info, JPXReader reader) {

        for (final int is : info.contiguousCodeStreamBoxes) {
            reader.setPosition(is);
            long tempLen = reader.readInt();
            reader.readInt();//read it and ignore
            if (tempLen == 1) {
                tempLen = reader.readLong();
            } else if (tempLen == 0) {
                tempLen = reader.getLimit() - is;
            }
            final long maxRead = is + tempLen;
            readCodeStream(info, reader, maxRead);
        }
    }

    private static void readCodeStream(Info info, JPXReader reader, long maxRead) {
        while (reader.getPosition() < maxRead) {
            final int header = reader.readUShort();
            switch (header) {
                case Markers.SOC:
                    break;
                case Markers.SIZ:
                    reader.readUShort();//int LSIZ 
                    info.siz = readSIZ(reader);
                    if (debug) {
                        System.out.println("Width " + info.imageWidth + " Height " + info.imageHeight);
                        System.out.println("SIZ info : " + info.siz);
                    }
                    info.qcc = new QCD[info.siz.nComp];
                    break;
                case Markers.COD:
                    reader.readUShort();//int LCOD                     
                    info.cod = readCOD(reader);
                    if (debug) {
                        System.out.println("info.cod : \n" + info.cod);
                    }
                    break;
                case Markers.COC:
                    if (debug) {
                        System.err.println("contains coc parameters");
                    }
                    final int LCOC = reader.readUShort();
                    reader.setPosition(reader.getPosition() + LCOC - 2);
                    break;
                case Markers.QCD:
                    final int LQCD = reader.readUShort();
                    info.qcd = readQCD(reader, LQCD);
                    if (debug) {
                        System.out.println("info.qcd : \n" + info.qcd);
                    }
                    break;
                case Markers.QCC:
                    final int LQCC = reader.readUShort();
                    int cVal = reader.readUByte();
                    final QCD qcc = new QCD();
                    final Byte qccQS = reader.readByte();
                    JPXBitReader qccBR = new JPXBitReader(qccQS);
                    qcc.guardBits = qccBR.readBits(3);
                    qcc.quantBits = qccBR.readBits(5);
                    qcc.hasScalar = false;
                    final int qccBalance = LQCC - 4;
                    int qccNB;
                    switch (qcc.quantBits) {
                        case 0:
                            qcc.hasScalar = true;
                            qccNB = qccBalance;
                            qcc.exponentB = new int[qccNB];
                            qcc.mantissaB = new int[qccNB];
                            for (int i = 0; i < qccNB; i++) {
                                qccBR = new JPXBitReader(reader.readByte());
                                qcc.exponentB[i] = qccBR.readBits(5);
                                qcc.mantissaB[i] = 0;
                            }
                            break;
                        case 1:
                            qcc.hasScalar = false;
                            final byte[] temp = {reader.readByte(), reader.readByte()};
                            qccBR = new JPXBitReader(temp);
                            final int eB = qccBR.readBits(5);
                            final int muB = qccBR.readBits(11);
                            qcc.exponentB = new int[]{eB};
                            qcc.mantissaB = new int[]{muB};
                            break;
                        case 2:
                            qccNB = qccBalance / 2;
                            qcc.hasScalar = true;
                            qcc.exponentB = new int[qccNB];
                            qcc.mantissaB = new int[qccNB];
                            for (int i = 0; i < qccNB; i++) {
                                final byte[] tt = {reader.readByte(), reader.readByte()};
                                qccBR = new JPXBitReader(tt);
                                qcc.exponentB[i] = qccBR.readBits(5);
                                qcc.mantissaB[i] = qccBR.readBits(11);
                            }
                            break;
                    }
                    if (debug) {
                        System.out.println("Contains Info QCC " + qcc);
                    }
                    info.qcc[cVal] = qcc;
                    break;
                case Markers.RGN:
                    final int LRGN = reader.readUShort();
                    reader.setPosition(reader.getPosition() + LRGN - 2);
                    break;
                case Markers.POC:
                    final int LPOC = reader.readUShort();
                    reader.setPosition(reader.getPosition() + LPOC - 2);
                    break;
                case Markers.PPM:
                    final int LPPM = reader.readUShort();
                    reader.setPosition(reader.getPosition() + LPPM - 2);
                    break;
                case Markers.PLM:
                    final int LPLM = reader.readUShort();
                    reader.setPosition(reader.getPosition() + LPLM - 2);
                    break;
                case Markers.TLM:
                    final int LTLM = reader.readUShort();
                    reader.setPosition(reader.getPosition() + LTLM - 2);
                    break;
                case Markers.CRG:
                    final int LCRG = reader.readUShort();
                    reader.setPosition(reader.getPosition() + LCRG - 2);
                    break;
                case Markers.COM:
                    final int LCOM = reader.readUShort();
                    reader.setPosition(reader.getPosition() + LCOM - 2);
                    break;
                case Markers.SOT:
                    final int tileOffset = reader.getPosition() - 2;
                    reader.readInt();
                    final int LTP = reader.readInt();
                    reader.readShort();
                    reader.setPosition(tileOffset + LTP);
                    info.tileOffsets.add(tileOffset);
                    break;
                case Markers.EOC:
                    return;
                default:
                    System.err.println("undefined header in jpeg2000file" + header);
            }
        }
    }

    private static SIZ readSIZ(JPXReader reader) {
        final SIZ siz = new SIZ();
        siz.capabilities = reader.readUShort();
        siz.Xsiz = reader.readInt();
        siz.Ysiz = reader.readInt();
        siz.XOsiz = reader.readInt();
        siz.YOsiz = reader.readInt();
        siz.XTsiz = reader.readInt();
        siz.YTsiz = reader.readInt();
        siz.XTOsiz = reader.readInt();
        siz.YTOsiz = reader.readInt();
        siz.nComp = reader.readUShort();
        siz.precisionInfo = new int[siz.nComp][3];
        for (int i = 0; i < siz.nComp; i++) {
            siz.precisionInfo[i][0] = reader.readUByte();
            siz.precisionInfo[i][1] = reader.readUByte();
            siz.precisionInfo[i][2] = reader.readUByte();
        }
        return siz;
    }

    private static COD readCOD(JPXReader reader) {
        final COD cod = new COD();
        final boolean[] bools = toBoolean8(reader.readByte());
        cod.hasPrecint = bools[7];
        cod.hasSOP = bools[6];
        cod.hasEPH = bools[5];
        cod.progressionOrder = reader.readUByte();
        cod.nLayers = reader.readUShort();
        cod.multiCompTransform = reader.readByte();
        cod.nDecompLevel = reader.readUByte();
        cod.xcb = reader.readUByte() + 2;
        cod.ycb = reader.readUByte() + 2;
        cod.codeBlockStyle = reader.readByte();
        cod.transformation = reader.readUByte();
        if (cod.hasPrecint) {
            cod.precintSizes = new int[cod.nDecompLevel + 1];
            for (int i = 0; i < cod.precintSizes.length; i++) {
                cod.precintSizes[i] = reader.readByte();
            }
        }
        return cod;
    }
    
    private static QCD readQCD(JPXReader reader, int qcdLength){
        final QCD qcd = new QCD();
        final Byte qs = reader.readByte();
        JPXBitReader br = new JPXBitReader(qs);
        qcd.guardBits = br.readBits(3);
        qcd.quantBits = br.readBits(5);
        qcd.hasScalar = false;

        final int balance = qcdLength - 3;
        int NB;
        switch (qcd.quantBits) {
            case 0:
                qcd.hasScalar = true;
                NB = balance;
                qcd.exponentB = new int[NB];
                qcd.mantissaB = new int[NB];
                for (int i = 0; i < NB; i++) {
                    br = new JPXBitReader(reader.readByte());
                    qcd.exponentB[i] = br.readBits(5);
                    qcd.mantissaB[i] = 0;
                }
                break;
            case 1:
                qcd.hasScalar = false;
                final byte[] temp = {reader.readByte(), reader.readByte()};
                br = new JPXBitReader(temp);
                final int eB = br.readBits(5);
                final int muB = br.readBits(11);
                qcd.exponentB = new int[]{eB};
                qcd.mantissaB = new int[]{muB};
                break;
            case 2:
                NB = balance / 2;
                qcd.hasScalar = true;
                qcd.exponentB = new int[NB];
                qcd.mantissaB = new int[NB];
                for (int i = 0; i < NB; i++) {
                    final byte[] tt = {reader.readByte(), reader.readByte()};
                    br = new JPXBitReader(tt);
                    qcd.exponentB[i] = br.readBits(5);
                    qcd.mantissaB[i] = br.readBits(11);
                }
                break;
        }
        return qcd;
    }
    
    private static void decodeTileOffsets(Info info, JPXReader reader) {

        for (final int offset : info.tileOffsets) {
            reader.setPosition(offset);

            reader.readUShort();//final int SOT = 
            reader.readUShort();//final int LSOT = 
            final int tileIndex = reader.readUShort(); //index of tile
            final int lengthTilePart = reader.readInt(); //length of tilestream 
            final int indexTilePart = reader.readUByte();//tile part index
            final int numberTilePart = reader.readUByte();//number of tile parts

            final int totalLen = lengthTilePart - 12;
            final int maxRead = offset + lengthTilePart;

            final Tile tile = new Tile();
            tile.cod = info.cod;
            tile.qcd = info.qcd;
            tile.qcc = info.qcc;
            tile.index = tileIndex;
            tile.partIndex = indexTilePart;
            tile.partCount = numberTilePart;

            int otherRead = 0;

            while (reader.getPosition() < maxRead) {
                final int header = reader.readUShort();
                switch (header) {
                    case Markers.COD:
                        final int LCOD = reader.readUShort();//int LCOD 
                        otherRead = otherRead + 2 + LCOD;
                        tile.cod = readCOD(reader);
                        if (debug) {
                            System.err.println("Contains Tile COD " + tile.cod);
                        }
                        break;
                    case Markers.QCD:
                        final int LQCD = reader.readUShort();//int LQCD 
                        otherRead = otherRead + 2 + LQCD;
                        tile.qcd = readQCD(reader, LQCD);
                        if (debug) {
                            System.err.println("Contains Tile QCD " + tile.qcd);
                        }
                        break;
                    case Markers.QCC:
                        final int LQCC = reader.readUShort();
                        int cVal = reader.readUByte();
                        final QCD qcc = new QCD();
                        final Byte qccQS = reader.readByte();
                        JPXBitReader qccBR = new JPXBitReader(qccQS);
                        qcc.guardBits = qccBR.readBits(3);
                        qcc.quantBits = qccBR.readBits(5);
                        qcc.hasScalar = false;
                        final int qccBalance = LQCC - 4;
                        int qccNB;
                        switch (qcc.quantBits) {
                            case 0:
                                qcc.hasScalar = true;
                                qccNB = qccBalance;
                                qcc.exponentB = new int[qccNB];
                                qcc.mantissaB = new int[qccNB];
                                for (int i = 0; i < qccNB; i++) {
                                    qccBR = new JPXBitReader(reader.readByte());
                                    qcc.exponentB[i] = qccBR.readBits(5);
                                    qcc.mantissaB[i] = 0;
                                }
                                break;
                            case 1:
                                qcc.hasScalar = false;
                                final byte[] temp = {reader.readByte(), reader.readByte()};
                                qccBR = new JPXBitReader(temp);
                                final int eB = qccBR.readBits(5);
                                final int muB = qccBR.readBits(11);
                                qcc.exponentB = new int[]{eB};
                                qcc.mantissaB = new int[]{muB};
                                break;
                            case 2:
                                qccNB = qccBalance / 2;
                                qcc.hasScalar = true;
                                qcc.exponentB = new int[qccNB];
                                qcc.mantissaB = new int[qccNB];
                                for (int i = 0; i < qccNB; i++) {
                                    final byte[] tt = {reader.readByte(), reader.readByte()};
                                    qccBR = new JPXBitReader(tt);
                                    qcc.exponentB[i] = qccBR.readBits(5);
                                    qcc.mantissaB[i] = qccBR.readBits(11);
                                }
                                break;
                        }
                        if (debug) {
                            System.out.println("Contains Tile QCC " + qcc);
                        }
                        tile.qcc[cVal] = qcc;
                        break;
                    case Markers.COC:
                    case Markers.RGN:
                    case Markers.POC:
                    case Markers.PPT:
                    case Markers.PLT:
                    case Markers.COM:
                        if (debug) {
                            System.out.println("these marker is not supported yet " + header);
                        }
                        final int temp = reader.readUShort();
                        otherRead = otherRead + 2 + temp;
                        reader.setPosition(reader.getPosition() + temp - 2);
                        break;
                    case Markers.SOD:
                        otherRead += 2;
                        if (debug) {
                            System.out.println("start reading");
                            System.out.println("Number of tile parts " + numberTilePart);
                        }

                        byte bb[] = new byte[totalLen - otherRead];
                        for (int i = 0; i < bb.length; i++) {
                            bb[i] = reader.readByte();
                        }
                        tile.data = bb;

                        if (tile.partIndex == 0) {
                            initializeDimensions(info, tile, tile.index);
                        }

                        final TileParser parser = new TileParser(tile.data, info.tilesMap.get(tile.index));
                        parser.parseTile();

                        break;
                    case Markers.EOC:
                        break;
                }
            }
        }
    }

    private static void initializeDimensions(Info info, Tile cur, int tileIndex) {

        final Tile tile = info.tilesMap.get(tileIndex);
        tile.cod = tile.cod != null ? tile.cod : cur.cod;
        tile.qcd = tile.qcd != null ? tile.qcd : cur.qcd;
        tile.qcc = tile.qcc != null ? tile.qcc : cur.qcc;

        switch (tile.cod.progressionOrder) {
            case Markers.LRCP:
                tile.progress = new LRCP(info, tileIndex);
                break;
            case Markers.RLCP:
                tile.progress = new RLCP(info, tileIndex);
                break;
            case Markers.RPCL:
                System.err.print("This progression order not supported");
                break;
            case Markers.PCRL:
                System.err.print("This progression order not supported");
                break;
            case Markers.CPRL:
                System.err.print("This progression order not supported");
                break;
            default:
                System.err.println("Unknown progression order found");
                break;
        }

        final int NL = tile.cod.nDecompLevel;
        final int xcb = tile.cod.xcb;
        final int ycb = tile.cod.xcb;

        final int ppx = 15;
        final int ppy = 15;

        for (final TileComponent tc : tile.components) {
            for (int r = 0; r <= NL; r++) {

                final int xcb_ = r == 0 ? Math.min(xcb, ppx) : Math.min(xcb, ppx - 1);
                final int ycb_ = r == 0 ? Math.min(ycb, ppy) : Math.min(ycb, ppy - 1);

                final TileResolution tr = new TileResolution();
                final int NL_R2 = 1 << (NL - r);
                tr.x0 = (int) Math.ceil(1.0 * tc.x0 / NL_R2);
                tr.x1 = (int) Math.ceil(1.0 * tc.x1 / NL_R2);
                tr.y0 = (int) Math.ceil(1.0 * tc.y0 / NL_R2);
                tr.y1 = (int) Math.ceil(1.0 * tc.y1 / NL_R2);
                updatePrecinctInfo(tr, r, ppx, ppy);

                if (r == 0) {
                    final int powN = 1 << NL;
                    final TileBand tb = new TileBand(TileBand.LL);
                    tb.x0 = (int) Math.abs(Math.ceil(1.0 * tc.x0 / powN));
                    tb.y0 = (int) Math.abs(Math.ceil(1.0 * tc.y0 / powN));
                    tb.x1 = (int) Math.abs(Math.ceil(1.0 * tc.x1 / powN));
                    tb.y1 = (int) Math.abs(Math.ceil(1.0 * tc.y1 / powN));
                    tr.tileBands.add(tb);
                    updateCodeBlocks(tr, tb, xcb_, ycb_);

                } else {
                    final int n = NL + 1 - r;
                    final int powN = 1 << n;
                    final int powM = 1 << (n - 1);

                    TileBand tb = new TileBand(TileBand.HL);
                    tb.x0 = (int) Math.abs(Math.ceil((1.0 * tc.x0 - (powM * 1)) / powN));
                    tb.y0 = (int) Math.abs(Math.ceil((1.0 * tc.y0 - (powM * 0)) / powN));
                    tb.x1 = (int) Math.abs(Math.ceil((1.0 * tc.x1 - (powM * 1)) / powN));
                    tb.y1 = (int) Math.abs(Math.ceil((1.0 * tc.y1 - (powM * 0)) / powN));
                    tr.tileBands.add(tb);
                    updateCodeBlocks(tr, tb, xcb_, ycb_);

                    tb = new TileBand(TileBand.LH);
                    tb.x0 = (int) Math.abs(Math.ceil((1.0 * tc.x0 - (powM * 0)) / powN));
                    tb.y0 = (int) Math.abs(Math.ceil((1.0 * tc.y0 - (powM * 1)) / powN));
                    tb.x1 = (int) Math.abs(Math.ceil((1.0 * tc.x1 - (powM * 0)) / powN));
                    tb.y1 = (int) Math.abs(Math.ceil((1.0 * tc.y1 - (powM * 1)) / powN));
                    tr.tileBands.add(tb);
                    updateCodeBlocks(tr, tb, xcb_, ycb_);

                    tb = new TileBand(TileBand.HH);
                    tb.x0 = (int) Math.abs(Math.ceil((1.0 * tc.x0 - (powM * 1)) / powN));
                    tb.y0 = (int) Math.abs(Math.ceil((1.0 * tc.y0 - (powM * 1)) / powN));
                    tb.x1 = (int) Math.abs(Math.ceil((1.0 * tc.x1 - (powM * 1)) / powN));
                    tb.y1 = (int) Math.abs(Math.ceil((1.0 * tc.y1 - (powM * 1)) / powN));
                    tr.tileBands.add(tb);
                    updateCodeBlocks(tr, tb, xcb_, ycb_);
                }
                tc.resolutions.add(tr);
            }
        }
    }

    private static void updatePrecinctInfo(TileResolution resolution, int r, int ppx, int ppy) {
        final PrecinctInfo pInfo = new PrecinctInfo();
        pInfo.precinctWidth = 1 << ppx;
        pInfo.precinctHeight = 1 << ppy;
        pInfo.precinctWidthInSubband = 1 << (ppx + (r == 0 ? 0 : -1));
        pInfo.precinctHeightInSubband = 1 << (ppy + (r == 0 ? 0 : -1));
        pInfo.numPrecinctsWide = (int) (resolution.x1 > resolution.x0
                ? Math.ceil(1.0 * resolution.x1 / pInfo.precinctWidth)
                - Math.floor(1.0 * resolution.x0 / pInfo.precinctWidth) : 0);
        pInfo.numPrecinctsHigh = (int) (resolution.y1 > resolution.y0
                ? Math.ceil(1.0 * resolution.y1 / pInfo.precinctHeight)
                - Math.floor(1.0 * resolution.y0 / pInfo.precinctHeight) : 0);
        pInfo.numPrecincts = pInfo.numPrecinctsWide * pInfo.numPrecinctsHigh;
        resolution.precinctInfo = pInfo;

    }

    private static void updateCodeBlocks(TileResolution tr, TileBand tb, int xcb_, int ycb_) {
        final int codeblockWidth = 1 << xcb_;
        final int codeblockHeight = 1 << ycb_;
        final int cbx0 = tb.x0 >> xcb_;
        final int cby0 = tb.y0 >> ycb_;
        final int cbx1 = (tb.x1 + codeblockWidth - 1) >> xcb_;
        final int cby1 = (tb.y1 + codeblockHeight - 1) >> ycb_;
        final PrecinctInfo precintInfo = tr.precinctInfo;
        final List<CodeBlock> codeblocks = tb.codeBlocks;
        final List<Precinct> precincts = tb.precincts;

        for (int j = cby0; j < cby1; j++) {
            for (int i = cbx0; i < cbx1; i++) {
                final CodeBlock cblk = new CodeBlock();
                cblk.x = i;
                cblk.y = j;
                cblk.tbx0 = codeblockWidth * i;
                cblk.tby0 = codeblockHeight * j;
                cblk.tbx1 = codeblockWidth * (i + 1);
                cblk.tby1 = codeblockHeight * (j + 1);

                cblk.tbx0_ = Math.max(tb.x0, cblk.tbx0);
                cblk.tby0_ = Math.max(tb.y0, cblk.tby0);
                cblk.tbx1_ = Math.min(tb.x1, cblk.tbx1);
                cblk.tby1_ = Math.min(tb.y1, cblk.tby1);

                final int pi = (int) Math.floor((cblk.tbx0_ - tb.x0) / (precintInfo.precinctWidthInSubband * 1.0));
                final int pj = (int) Math.floor((cblk.tby0_ - tb.y0) / (precintInfo.precinctHeightInSubband * 1.0));
                final int precintNumber = pi + (pj * precintInfo.numPrecinctsWide);

                cblk.precinctNumber = precintNumber;
                cblk.subbandType = tb.type;
                cblk.Lblock = 3;

                if (cblk.tbx1_ <= cblk.tbx0_ || cblk.tby1_ <= cblk.tby0_) {
                    continue;
                }
                codeblocks.add(cblk);

                Precinct precinct;
                if (precincts.size() > precintNumber) {
                    precinct = precincts.get(precintNumber);
                    if (i < precinct.cbx0) {
                        precinct.cbx0 = i;
                    } else if (i > precinct.cbx1) {
                        precinct.cbx1 = i;
                    }
                    if (j < precinct.cby0) {
                        precinct.cbx0 = j;
                    } else if (j > precinct.cby1) {
                        precinct.cby1 = j;
                    }
                } else {
                    precinct = new Precinct();
                    precinct.cby0 = j;
                    precinct.cby1 = j;
                    precinct.cbx0 = i;
                    precinct.cbx1 = i;
                    precincts.add(precinct);
                }
                cblk.precinct = precinct;
            }
        }
        tb.codeBlockInfo = new CodeBlockInfo();
        tb.codeBlockInfo.codeBlockWidth = xcb_;
        tb.codeBlockInfo.codeBlockHeight = ycb_;
        tb.codeBlockInfo.numCodeBlockWide = cbx1 - cbx0 + 1;
        tb.codeBlockInfo.numCodeBlockHigh = cby1 - cby0 + 1;
    }

    private static BufferedImage convertPalette(Info info) {
        final SIZ siz = info.siz;
        final int componentsCount = siz.nComp;
        final List<SubbandCoefficient> resultImages = new ArrayList<SubbandCoefficient>();

        for (int i = 0; i < info.tilesMap.size(); i++) {
            final Tile tile = info.tilesMap.get(i);
            SubbandCoefficient[] transformedTiles = new SubbandCoefficient[componentsCount];
            for (int c = 0; c < componentsCount; c++) {
                transformedTiles[c] = transformTile(info, tile, c);
                tile.components.get(c).resolutions.clear();
            }
            final SubbandCoefficient inputSC = transformedTiles[0];

            final SubbandCoefficient result = new SubbandCoefficient();
            result.x = inputSC.x;
            result.y = inputSC.y;
            result.width = inputSC.width;
            result.height = inputSC.height;

            byte[] out = new byte[result.width * result.height * componentsCount];
            result.byteItems = out;

            int shift;
            float offset, min, max;
            int pos;
            for (int c = 0; c < componentsCount; c++) {
                final float[] items = transformedTiles[c].floatItems;
                shift = (info.siz.precisionInfo[c][0] + 1) - 8;

                if (shift == 0) {
                    offset = 128.5f;
                    max = 127.5f;
                    min = -max;
                    pos = c;
                    for (int j = 0; j < items.length; j++) {
                        final float val = items[j];
                        out[pos] = (byte) (val <= min ? 0 : val >= max ? 255 : (val + offset));
                        pos += componentsCount;
                    }
                } else if (shift < 0) {
                    pos = c;
                    offset = 1 << (info.siz.precisionInfo[c][0]);
                    final int minVal = 0;
                    final int maxVal = (1 << (info.siz.precisionInfo[c][0] + 1)) - 1;
                    for (int j = 0; j < items.length; j++) {
                        final float val = items[j] + offset;
                        out[pos] = (byte) Math.max(minVal, Math.min(val, maxVal));
                        pos += componentsCount;
                    }
                }
            }
            resultImages.add(result);
        }

        info.tilesMap.clear();

        byte[] mainData;

        if (resultImages.size() == 1) {
            mainData = resultImages.get(0).byteItems;
        } else {
            mainData = new byte[siz.nComp * siz.Xsiz * siz.Ysiz];
            final int c = siz.nComp;
            final int mainStripLen = siz.Xsiz * c;

            for (final SubbandCoefficient sc : resultImages) {
                final byte[] subData = sc.byteItems;
                final int x = sc.x;
                final int y = sc.y;
                final int w = sc.width;
                final int h = sc.height;
                final int subStripLen = w * c;
                final int xc = (x * c);
                for (int i = 0; i < h; i++) {
                    final int stripOffset = i * subStripLen;
                    final int mainOffset = ((i + y) * mainStripLen) + xc;
                    System.arraycopy(subData, stripOffset, mainData, mainOffset, subStripLen);
                }
                sc.byteItems = null;
            }
        }

        BufferedImage image;

        if (info.enumerateCS == Info.CS_CMYK) {
            byte[] tempData = new byte[4 * siz.Xsiz * siz.Ysiz];
            int pos = 0;
            for (int i = 0; i < mainData.length; i++) {
                final int[] cc = info.palette.cValues[mainData[i] & 0xff];
                for (int j = 0; j < cc.length; j++) {
                    tempData[pos] = (byte) cc[j];
                    pos++;
                }
            }

            image = new BufferedImage(siz.Xsiz, siz.Ysiz, BufferedImage.TYPE_INT_RGB);
            int[] imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            pos = 0;
            double r, g, b, cc = 0, mm = 0, yy = 0, kk = 0, c, m, y, k;

            for (int i = 0; i < imageData.length; i++) {
                c = (tempData[pos++] & 0xff) / 255.0;
                m = (tempData[pos++] & 0xff) / 255.0;
                y = (tempData[pos++] & 0xff) / 255.0;
                k = (tempData[pos++] & 0xff) / 255.0;

                if (c == cc && m == mm && y == yy && k == kk && i > 0) {
                    imageData[i] = imageData[i - 1];
                } else {
                    final double dif = 1 - k;
                    r = 255 * (1 - c) * dif;
                    g = 255 * (1 - m) * dif;
                    b = 255 * (1 - y) * dif;

                    cc = c;
                    mm = m;
                    yy = y;
                    kk = k;
                    imageData[i] = (((int) r) << 16) | (((int) g) << 8) | ((int) b);
                }
            }

        } else {
            image = generateBufferedImage(info.palette.nColumns, siz.Xsiz, siz.Ysiz);
            byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

            int pos = 0;
            for (int i = 0; i < mainData.length; i++) {
                final int[] cc = info.palette.cValues[mainData[i] & 0xff];
                for (int j = 0; j < cc.length; j++) {
                    imageData[pos] = (byte) cc[j];
                    pos++;
                }
            }
        }

        return image;
    }

    private static BufferedImage convertCMYKTileToRGB(Info info, boolean convertCMYK) {
        final SIZ siz = info.siz;
        final int componentsCount = siz.nComp;
        final List<SubbandCoefficient> resultImages = new ArrayList<SubbandCoefficient>();

        for (int i = 0; i < info.tilesMap.size(); i++) {
            final Tile tile = info.tilesMap.get(i);
            SubbandCoefficient[] transformedTiles = new SubbandCoefficient[componentsCount];
            for (int c = 0; c < componentsCount; c++) {
                transformedTiles[c] = transformTile(info, tile, c);
                tile.components.get(c).resolutions.clear();
            }
            final SubbandCoefficient inputSC = transformedTiles[0];

            final SubbandCoefficient result = new SubbandCoefficient();
            result.x = inputSC.x;
            result.y = inputSC.y;
            result.width = inputSC.width;
            result.height = inputSC.height;

            byte[] out = new byte[result.width * result.height * componentsCount];
            result.byteItems = out;

            int shift;
            float offset, maxK, min, max;
            int pos = 0;
            double c1, c2, c3, c4, r, g, b;

            if (tile.cod.multiCompTransform == 1) {
                final float[] comp1Floats = transformedTiles[0].floatItems;
                final float[] comp2Floats = transformedTiles[1].floatItems;
                final float[] comp3Floats = transformedTiles[2].floatItems;
                final float[] comp4Floats = transformedTiles[3].floatItems;

                shift = (info.siz.precisionInfo[0][0] + 1) - 8;
                offset = (128 << shift) + 0.5f;
                max = 255 * (1 << shift);
                maxK = max * 0.5f;
                min = -maxK;

                if (tile.cod.transformation == 0) {
                    pos = 0;
                    for (int j = 0; j < comp1Floats.length; j++) {
                        c1 = comp1Floats[j] + offset;
                        c2 = comp2Floats[j];
                        c3 = comp3Floats[j];
                        c4 = comp4Floats[j];
                        r = c1 + 1.402 * c3;
                        g = c1 - 0.34413 * c2 - 0.71414 * c3;
                        b = c1 + 1.772 * c2;
                        out[pos++] = (byte) (r <= 0 ? 0 : r >= max ? 255 : ((int) r) >> shift);
                        out[pos++] = (byte) (g <= 0 ? 0 : g >= max ? 255 : ((int) g) >> shift);
                        out[pos++] = (byte) (b <= 0 ? 0 : b >= max ? 255 : ((int) b) >> shift);
                        out[pos++] = (byte) (c4 <= min ? 0 : c4 >= maxK ? 255 : ((int) (c4 + offset)) >> shift);
                    }
                } else {
                    for (int j = 0; j < comp1Floats.length; j++) {
                        c1 = comp1Floats[j] + offset;
                        c2 = comp2Floats[j];
                        c3 = comp3Floats[j];
                        c4 = comp4Floats[j];
                        g = c1 - (((int) (c3 + c2)) >> 2);
                        r = g + c3;
                        b = g + c2;
                        out[pos++] = (byte) (r <= 0 ? 0 : r >= max ? 255 : ((int) r) >> shift);
                        out[pos++] = (byte) (g <= 0 ? 0 : g >= max ? 255 : ((int) g) >> shift);
                        out[pos++] = (byte) (b <= 0 ? 0 : b >= max ? 255 : ((int) b) >> shift);
                        out[pos++] = (byte) (c4 <= min ? 0 : c4 >= maxK ? 255 : ((int) (c4 + offset)) >> shift);
                    }
                }
            } else {
                for (int c = 0; c < componentsCount; c++) {
                    final float[] items = transformedTiles[c].floatItems;
                    shift = (info.siz.precisionInfo[c][0] + 1) - 8;
                    offset = (128 << shift) + 0.5f;
                    max = (int) (127.5f * (1 << shift));
                    min = -max;
                    pos = c;
                    for (int j = 0; j < items.length; j++) {
                        final float val = items[j];
                        out[pos] = (byte) (val <= min ? 0 : val >= max ? 255 : ((int) (val + offset)) >> shift);
                        pos += componentsCount;
                    }
                }
            }

            resultImages.add(result);
        }

        info.tilesMap.clear();

        byte[] mainData;

        if (resultImages.size() == 1) {
            mainData = resultImages.get(0).byteItems;
        } else {
            mainData = new byte[siz.nComp * siz.Xsiz * siz.Ysiz];
            final int c = siz.nComp;
            final int mainStripLen = siz.Xsiz * c;

            for (final SubbandCoefficient sc : resultImages) {
                final byte[] subData = sc.byteItems;
                final int x = sc.x;
                final int y = sc.y;
                final int w = sc.width;
                final int h = sc.height;
                final int subStripLen = w * c;
                final int xc = (x * c);
                for (int i = 0; i < h; i++) {
                    final int stripOffset = i * subStripLen;
                    final int mainOffset = ((i + y) * mainStripLen) + xc;
                    System.arraycopy(subData, stripOffset, mainData, mainOffset, subStripLen);
                }
                sc.byteItems = null;
            }
        }

        BufferedImage image;
        if (convertCMYK) {
            image = new BufferedImage(siz.Xsiz, siz.Ysiz, BufferedImage.TYPE_INT_RGB);
            int[] imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            int pos = 0;
            int r, g, b;
            byte c, m, y, k;

            final EnumeratedSpace cs = new EnumeratedSpace();

            for (int i = 0; i < imageData.length; i++) {
                c = mainData[pos++];
                m = mainData[pos++];
                y = mainData[pos++];
                k = mainData[pos++];

                final byte[] rgb = cs.getRGB(c, m, y, k);
                r = rgb[0] & 0xff;
                g = rgb[1] & 0xff;
                b = rgb[2] & 0xff;
                imageData[i] = (r << 16) | (g << 8) | b;
            }
        } else {
            image = generateBufferedImage(4, siz.Xsiz, siz.Ysiz);
            final byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(mainData, 0, imageData, 0, mainData.length);

        }

        return image;
    }

    private static BufferedImage convertTileComponentsToBuffered(Info info) {
        final SIZ siz = info.siz;
        final int componentsCount = siz.nComp;
        final List<SubbandCoefficient> resultImages = new ArrayList<SubbandCoefficient>();

        for (int i = 0; i < info.tilesMap.size(); i++) {
            final Tile tile = info.tilesMap.get(i);
            SubbandCoefficient[] transformedTiles = new SubbandCoefficient[componentsCount];
            for (int c = 0; c < componentsCount; c++) {
                transformedTiles[c] = transformTile(info, tile, c);
                tile.components.get(c).resolutions.clear();
            }
            final SubbandCoefficient inputSC = transformedTiles[0];

            final SubbandCoefficient result = new SubbandCoefficient();
            result.x = inputSC.x;
            result.y = inputSC.y;
            result.width = inputSC.width;
            result.height = inputSC.height;

            byte[] out;
            if (info.tilesMap.size() == 1) {
                result.bufferedImage = generateBufferedImage(componentsCount, result.width, result.height);
                out = ((DataBufferByte) result.bufferedImage.getRaster().getDataBuffer()).getData();
            } else {
                out = new byte[result.width * result.height * componentsCount];
                result.byteItems = out;
            }

            int shift;
            double offset;
            int min, max;
            int pos = 0;
            double y0, y1, y2;
            double r, g, b;
            if (tile.cod.multiCompTransform != 0) {
                final float[] y0items = getMappedComponent(transformedTiles, 0, info);
                final float[] y1items = getMappedComponent(transformedTiles, 1, info);
                final float[] y2items = getMappedComponent(transformedTiles, 2, info);

                shift = (info.siz.precisionInfo[0][0] + 1) - 8;
                offset = (128 << shift) + 0.5f;
                max = 255 * (1 << shift);

                final int alpha01 = componentsCount - 3;

                if (tile.cod.transformation == 0) {
                    for (int j = 0; j < y0items.length; j++, pos += alpha01) {
                        y0 = y0items[j] + offset;
                        y1 = y1items[j];
                        y2 = y2items[j];
                        r = (y0 + 1.402 * y2);
                        g = (y0 - 0.34413 * y1 - 0.71414 * y2);
                        b = (y0 + 1.772 * y1);
                        out[pos++] = (byte) (r < 0 ? 0 : r > max ? 255 : ((int) r) >> shift);
                        out[pos++] = (byte) (g < 0 ? 0 : g > max ? 255 : ((int) g) >> shift);
                        out[pos++] = (byte) (b < 0 ? 0 : b > max ? 255 : ((int) b) >> shift);
                    }
                } else {
                    final int yLen = y0items.length;
                    for (int j = 0; j < yLen; j++, pos += alpha01) {
                        y0 = y0items[j] + offset;
                        y1 = y1items[j];
                        y2 = y2items[j];
                        g = (y0 - (((int) (y2 + y1)) >> 2));
                        r = g + y2;
                        b = g + y1;
                        out[pos++] = (byte) (r < 0 ? 0 : r > max ? 255 : ((int) r) >> shift);
                        out[pos++] = (byte) (g < 0 ? 0 : g > max ? 255 : ((int) g) >> shift);
                        out[pos++] = (byte) (b < 0 ? 0 : b > max ? 255 : ((int) b) >> shift);
                    }
                }

            } else { // no multi-component transform

                if (info.enumerateCS == Info.CS_SYCC && componentsCount == 3) {
                    final float[] y0items = getMappedComponent(transformedTiles, 0, info);
                    final float[] y1items = getMappedComponent(transformedTiles, 1, info);
                    final float[] y2items = getMappedComponent(transformedTiles, 2, info);
                    shift = (info.siz.precisionInfo[0][0] + 1) - 8;
                    offset = (128 << shift) + 0.5f;
                    max = 255 * (1 << shift);
                    final int yLen = y0items.length;
                    for (int j = 0; j < yLen; j++) {
                        y0 = y0items[j] + offset;
                        y1 = y1items[j];
                        y2 = y2items[j];
                        r = (y0 + 1.402 * y2);
                        g = (y0 - 0.34413 * y1 - 0.71414 * y2);
                        b = (y0 + 1.772 * y1);
                        out[pos++] = (byte) (r < 0 ? 0 : r > max ? 255 : ((int) r) >> shift);
                        out[pos++] = (byte) (g < 0 ? 0 : g > max ? 255 : ((int) g) >> shift);
                        out[pos++] = (byte) (b < 0 ? 0 : b > max ? 255 : ((int) b) >> shift);
                    }

                } else {
                    for (int c = 0; c < componentsCount; c++) {
                        final float[] items = transformedTiles[c].floatItems;
                        shift = (info.siz.precisionInfo[c][0] + 1) - 8;
                        offset = (128 << shift) + 0.5f;
                        max = (int) (127.5f * (1 << shift));
                        min = -max;
                        pos = c;
                        for (int j = 0; j < items.length; j++) {
                            final float val = items[j];
                            out[pos] = (byte) (val <= min ? 0 : val >= max ? 255 : ((int) (val + offset)) >> shift);
                            pos += componentsCount;
                        }
                    }
                }
            }

            inputSC.floatItems = null;
            resultImages.add(result);
        }

        info.tilesMap.clear();

        if (resultImages.size() == 1) {
            return resultImages.get(0).bufferedImage;
        } else {
            final BufferedImage image = generateBufferedImage(siz.nComp, siz.Xsiz, siz.Ysiz);
            final byte[] mainData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            final int c = siz.nComp;
            final int mainStripLen = siz.Xsiz * c;

            for (final SubbandCoefficient sc : resultImages) {
                final byte[] subData = sc.byteItems;
                final int x = sc.x;
                final int y = sc.y;
                final int w = sc.width;
                final int h = sc.height;
                final int subStripLen = w * c;
                final int xc = (x * c);
                for (int i = 0; i < h; i++) {
                    final int stripOffset = i * subStripLen;
                    final int mainOffset = ((i + y) * mainStripLen) + xc;
                    System.arraycopy(subData, stripOffset, mainData, mainOffset, subStripLen);
                }
                sc.byteItems = null;
            }
            return image;
        }
    }

    private static float[] getMappedComponent(SubbandCoefficient[] arr, int keyVal, Info info) {
        if (info.cDef.isEmpty()) {
            return arr[keyVal].floatItems;
        } else {
            if (info.cDef.containsKey(keyVal)) {
                return arr[info.cDef.get(keyVal) - 1].floatItems;
            } else {
                return arr[keyVal].floatItems;
            }
        }
    }

    private static SubbandCoefficient transformTile(Info info, Tile tile, int c) {
        final SIZ siz = info.siz;
        final TileComponent comp = tile.components.get(c);
        final QCD qcd = tile.qcc[c] != null ? tile.qcc[c] : tile.qcd;
        final COD cod = tile.cod;
        final int NL = cod.nDecompLevel;
        final int guardBits = qcd.guardBits;
        final int precision = siz.precisionInfo[c][0] + 1;
        final boolean reversible = cod.transformation == 1;

        final Trns trns = new Trns(reversible);

        List<SubbandCoefficient> subcos = new ArrayList<SubbandCoefficient>();
        int b = 0;

        for (int i = 0; i <= NL; i++) {
            TileResolution resolution = comp.resolutions.get(i);

            int width = resolution.getWidth();
            int height = resolution.getHeight();
            float[] coefficients = new float[width * height];

            for (TileBand subband1 : resolution.tileBands) {
                int mu, epsilon;
                if (!qcd.hasScalar) {
                    epsilon = qcd.exponentB[0] + (i > 0 ? 1 - i : 0);
                    mu = qcd.mantissaB[0];
                } else {
                    epsilon = qcd.exponentB[b];
                    mu = qcd.mantissaB[b];
                    b++;
                }
                int multi = subband1.getMultiplier();
                double toBePowered = precision + multi - epsilon;
                float delta = (float) (cod.transformation == 1 ? 1 : Math.pow(2, toBePowered) * (1 + mu / (2048 * 1.0f)));
                int mb = (guardBits + epsilon - 1);
                sendForPassing(coefficients, width, subband1, delta, mb, reversible);
            }
            SubbandCoefficient sc = new SubbandCoefficient();
            sc.width = width;
            sc.height = height;
            sc.floatItems = coefficients;
            subcos.add(sc);
        }

        SubbandCoefficient result = trns.getInversed(subcos, comp.x0, comp.y0);
        result.x = comp.x0;
        result.y = comp.y0;

        int sw = result.width;
        int sh = result.height;
        int dw = result.width * siz.precisionInfo[c][1];
        int dh = result.height * siz.precisionInfo[c][1];
        if (sw != dw || sh != dh) {
            result.floatItems = applyBilinearScaling(result.floatItems, sw, sh, dw, dh);
        }
        return result;
    }

    private static float[] applyBilinearScaling(float[] data, int sw, int sh, int dw, int dh) {

        if (sh == 1) {
            float[] temp = new float[2 * sw];
            System.arraycopy(data, 0, temp, 0, sw);
            System.arraycopy(data, 0, temp, sw, sw);
            sh = 2;
            data = temp;
        }

        float[] temp = new float[dw * dh];
        float A, B, C, D;
        int x, y, index;
        float xRatio = ((float) (sw - 1)) / dw;
        float yRatio = ((float) (sh - 1)) / dh;
        float xDiff, yDiff;
        int offset = 0;
        for (int i = 0; i < dh; i++) {
            for (int j = 0; j < dw; j++) {
                x = (int) (xRatio * j);
                y = (int) (yRatio * i);
                xDiff = (xRatio * j) - x;
                yDiff = (yRatio * i) - y;
                index = y * sw + x;

                A = data[index];
                B = data[index + 1];
                C = data[index + sw];
                D = data[index + sw + 1];

                temp[offset++] = (A * (1 - xDiff) * (1 - yDiff) + B * (xDiff) * (1 - yDiff)
                        + C * (yDiff) * (1 - xDiff) + D * (xDiff * yDiff));
            }
        }
        return temp;
    }

    private static void sendForPassing(float[] coefficients, int resWidth, TileBand subband, float delta, int mb, boolean reversible) {

        int width = subband.x1 - subband.x0;
        List<CodeBlock> codeBlocks = subband.codeBlocks;
        int right = 0;
        int bottom = 0;
        switch (subband.type) {
            case TileBand.HH:
                right = 1;
                bottom = resWidth;
                break;
            case TileBand.LH:
                bottom = resWidth;
                break;
            case TileBand.HL:
                right = 1;
                break;
        }

        for (CodeBlock codeBlock : codeBlocks) {
            int blockWidth = codeBlock.tbx1_ - codeBlock.tbx0_;
            int blockHeight = codeBlock.tby1_ - codeBlock.tby0_;
            if (blockWidth == 0 || blockHeight == 0 || codeBlock.dataList.isEmpty()) {
                continue;
            }

            Tier1Decoder tier1 = new Tier1Decoder(blockWidth, blockHeight, subband, codeBlock.zeroBitPlanes, mb);

            List<BlockData> datas = codeBlock.dataList;
            int totalLength = 0;
            int codingpasses = 0;

            for (BlockData dataItem : datas) {
                totalLength += (dataItem.end - dataItem.start);
                codingpasses += (dataItem.nCodingPass & 0xff);
            }

            byte[] encodedData = new byte[totalLength];
            int position = 0;
            for (BlockData dataItem : datas) {
                byte[] chunk = new byte[dataItem.end - dataItem.start];
                System.arraycopy(dataItem.data, dataItem.start, chunk, 0, chunk.length);
                System.arraycopy(chunk, 0, encodedData, position, chunk.length);
                position += chunk.length;
            }

            EntropyDecoder decoder = new EntropyDecoder(encodedData, 0, totalLength);
            tier1.setDecoder(decoder);

            int currentCodingpassType = 2;
            for (int j = 0; j < codingpasses; j++) {
                switch (currentCodingpassType) {
                    case 0:
                        tier1.runSPP();
                        break;
                    case 1:
                        tier1.runMRP();
                        break;
                    case 2:
                        tier1.runCP();
                        break;
                }
                currentCodingpassType = (currentCodingpassType + 1) % 3;
            }

            int offset = (codeBlock.tbx0_ - subband.x0) + (codeBlock.tby0_ - subband.y0) * width;
            byte[] sign = tier1.coefficientsSign;
            Object magObj = tier1.magnitude;
            byte[] bitsDecoded = tier1.bitsDecoded;
            float magnitudeCorrection = reversible ? 0.0f : 0.5f;
            position = 0;

            boolean interleave = (subband.type != TileBand.LL);

            for (int j = 0; j < blockHeight; j++) {
                int row = (offset / width);
                int levelOffset = 2 * row * (resWidth - width) + right + bottom;
                for (int k = 0; k < blockWidth; k++) {
                    float n = 0.0f;

                    switch (tier1.mbType) {
                        case Tier1Decoder.BYTEMB:
                            n = ((byte[]) magObj)[position] & 0xff;
                            break;
                        case Tier1Decoder.SHORTMB:
                            n = ((short[]) magObj)[position] & 0xffff;
                            break;
                        case Tier1Decoder.INTMB:
                            n = ((int[]) magObj)[position];
                            break;
                    }

                    if (n != 0) {
                        n = (n + magnitudeCorrection) * delta;
                        if (sign[position] != 0) {
                            n = -n;
                        }
                        int nb = bitsDecoded[position] & 0xff;
                        int pos = interleave ? (levelOffset + (offset << 1)) : offset;
                        if (reversible && (nb >= mb)) {
                            coefficients[pos] = n;
                        } else {
                            coefficients[pos] = n * (1 << (mb - nb));
                        }
                    }
                    offset++;
                    position++;
                }
                offset += width - blockWidth;
            }

            tier1.neighborSigns = null;
            tier1.coefficientsSign = null;
            tier1.magnitude = null;
            tier1.currentFlag = null;
            tier1.bitsDecoded = null;
            tier1.cx = null;
        }
    }

    private static boolean[] toBoolean8(byte b) {
        boolean[] bool = new boolean[8];
        for (int j = 7; j >= 0; j--) {
            bool[j] = ((b >> j) & 1) == 1;
        }
        return bool;
    }

    private static BufferedImage generateBufferedImage(int nComp, int width, int height) {
        ICC_ColorSpace cSpace;
        ComponentColorModel model;
        WritableRaster ras;
        switch (nComp) {
            case 1:
                cSpace = new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_GRAY));
                model = new ComponentColorModel(cSpace, false, false, 1, DataBuffer.TYPE_BYTE);
                ras = model.createCompatibleWritableRaster(width, height);
                return new BufferedImage(model, ras, false, null);
            case 2:
                cSpace = new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_GRAY));
                model = new ComponentColorModel(cSpace, true, false, 1, DataBuffer.TYPE_BYTE);
                ras = model.createCompatibleWritableRaster(width, height);
                return new BufferedImage(model, ras, false, null);
            case 3:
                cSpace = new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_sRGB));
                model = new ComponentColorModel(cSpace, false, false, 1, DataBuffer.TYPE_BYTE);
                ras = model.createCompatibleWritableRaster(width, height);
                return new BufferedImage(model, ras, false, null);
            case 4:
                cSpace = new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_sRGB));
                model = new ComponentColorModel(cSpace, true, false, 1, DataBuffer.TYPE_BYTE);
                ras = model.createCompatibleWritableRaster(width, height);
                return new BufferedImage(model, ras, false, null);
        }
        return null;
    }

    /**
     * Not recommended for external use.
     * <p>
     * decodes JPEG2000 image data as rgb/gray image bytes <br/>
     * Example: if rgb component image then returned byte array contains r
     * followed by g followed by b
     *
     * @param jpxRawData A byte[] array containing the JPEG data
     * @return BufferedImage to read image
     * @throws Exception Provides for different exceptions thrown under java
     * lang package
     */
    public byte[] readComponentsAsConvertedBytes(final byte[] jpxRawData) throws Exception {
        final Info info = new Info();
        final JPXReader reader = new JPXReader(jpxRawData);
        if (Markers.SOC == ((jpxRawData[0] & 0xff) << 8 | (jpxRawData[1] & 0xff))) {
            readCodeStream(info, reader, jpxRawData.length);
        } else {
            decodeMain(info, reader);
            decodeContiguousCodeStreamBoxes(info, reader);
        }
        generateTileMap(info);
        decodeTileOffsets(info, reader);

        final SIZ siz = info.siz;
        final int componentsCount = siz.nComp;
        final List<SubbandCoefficient> resultImages = new ArrayList<SubbandCoefficient>();

        for (int i = 0; i < info.tilesMap.size(); i++) {
            final Tile tile = info.tilesMap.get(i);
            SubbandCoefficient[] transformedTiles = new SubbandCoefficient[componentsCount];
            for (int c = 0; c < componentsCount; c++) {
                transformedTiles[c] = transformTile(info, tile, c);
                tile.components.get(c).resolutions.clear();
            }
            final SubbandCoefficient inputSC = transformedTiles[0];

            final SubbandCoefficient result = new SubbandCoefficient();
            result.x = inputSC.x;
            result.y = inputSC.y;
            result.width = inputSC.width;
            result.height = inputSC.height;

            byte[] out = new byte[result.width * result.height * componentsCount];
            result.byteItems = out;

            int shift;
            float offset, maxK, min, max;
            int pos = 0;
            double c1, c2, c3, c4, r, g, b;
            float[] comp1, comp2, comp3, comp4 = null;

            if (tile.cod.multiCompTransform == 1) {
                comp1 = transformedTiles[0].floatItems;
                comp2 = transformedTiles[1].floatItems;
                comp3 = transformedTiles[2].floatItems;
                if (tile.components.size() == 4) {
                    comp4 = transformedTiles[3].floatItems;
                }

                shift = (info.siz.precisionInfo[0][0] + 1) - 8;
                offset = (128 << shift) + 0.5f;
                max = 255 * (1 << shift);
                maxK = max * 0.5f;
                min = -maxK;

                if (tile.cod.transformation == 0) {
                    pos = 0;
                    for (int j = 0; j < comp1.length; j++) {
                        c1 = comp1[j] + offset;
                        c2 = comp2[j];
                        c3 = comp3[j];
                        r = c1 + 1.402 * c3;
                        g = c1 - 0.34413 * c2 - 0.71414 * c3;
                        b = c1 + 1.772 * c2;
                        out[pos++] = (byte) (r <= 0 ? 0 : r >= max ? 255 : ((int) r) >> shift);
                        out[pos++] = (byte) (g <= 0 ? 0 : g >= max ? 255 : ((int) g) >> shift);
                        out[pos++] = (byte) (b <= 0 ? 0 : b >= max ? 255 : ((int) b) >> shift);
                        if (comp4 != null) {
                            c4 = comp4[j];
                            out[pos++] = (byte) (c4 <= min ? 0 : c4 >= maxK ? 255 : ((int) (c4 + offset)) >> shift);
                        }
                    }
                } else {
                    for (int j = 0; j < comp1.length; j++) {
                        c1 = comp1[j] + offset;
                        c2 = comp2[j];
                        c3 = comp3[j];
                        g = c1 - (((int) (c3 + c2)) >> 2);
                        r = g + c3;
                        b = g + c2;
                        out[pos++] = (byte) (r <= 0 ? 0 : r >= max ? 255 : ((int) r) >> shift);
                        out[pos++] = (byte) (g <= 0 ? 0 : g >= max ? 255 : ((int) g) >> shift);
                        out[pos++] = (byte) (b <= 0 ? 0 : b >= max ? 255 : ((int) b) >> shift);
                        if (comp4 != null) {
                            c4 = comp4[j];
                            out[pos++] = (byte) (c4 <= min ? 0 : c4 >= maxK ? 255 : ((int) (c4 + offset)) >> shift);
                        }
                    }
                }
            } else {
                for (int c = 0; c < componentsCount; c++) {
                    final float[] items = transformedTiles[c].floatItems;
                    shift = (info.siz.precisionInfo[c][0] + 1) - 8;
                    offset = (128 << shift) + 0.5f;
                    max = (int) (127.5f * (1 << shift));
                    min = -max;
                    pos = c;
                    for (int j = 0; j < items.length; j++) {
                        final float val = items[j];
                        out[pos] = (byte) (val <= min ? 0 : val >= max ? 255 : ((int) (val + offset)) >> shift);
                        pos += componentsCount;
                    }
                }
            }

            resultImages.add(result);
        }

        info.tilesMap.clear();

        byte[] mainData;

        if (resultImages.size() == 1) {
            mainData = resultImages.get(0).byteItems;
        } else {
            mainData = new byte[siz.nComp * siz.Xsiz * siz.Ysiz];
            final int c = siz.nComp;
            final int mainStripLen = siz.Xsiz * c;

            for (final SubbandCoefficient sc : resultImages) {
                final byte[] subData = sc.byteItems;
                final int x = sc.x;
                final int y = sc.y;
                final int w = sc.width;
                final int h = sc.height;
                final int subStripLen = w * c;
                final int xc = (x * c);
                for (int i = 0; i < h; i++) {
                    final int stripOffset = i * subStripLen;
                    final int mainOffset = ((i + y) * mainStripLen) + xc;
                    System.arraycopy(subData, stripOffset, mainData, mainOffset, subStripLen);
                }
                sc.byteItems = null;
            }
        }
        boolean isPalette = info.palette != null && info.siz.nComp == 1;
        if (isPalette) {
            byte[] imageData = new byte[info.palette.nColumns * siz.Xsiz * siz.Ysiz];
            int pos = 0;
            for (int i = 0; i < mainData.length; i++) {
                final int[] cc = info.palette.cValues[mainData[i] & 0xff];
                for (int j = 0; j < cc.length; j++) {
                    imageData[pos] = (byte) cc[j];
                    pos++;
                }
            }
            mainData = imageData;
        }

        if (info.enumerateCS == Info.CS_CMYK) {
            final EnumeratedSpace cs = new EnumeratedSpace();
            byte c, m, y, k;
            int a = 0, p = 0;
            byte[] rgbData = new byte[info.imageHeight * info.imageWidth * 3];

            for (int i = 0; i < mainData.length; i += 4) {
                c = mainData[a++];
                m = mainData[a++];
                y = mainData[a++];
                k = mainData[a++];

                final byte[] rgb = cs.getRGB(c, m, y, k);
                rgbData[p++] = rgb[0];
                rgbData[p++] = rgb[1];
                rgbData[p++] = rgb[2];
            }
            mainData = rgbData;
        }

        return mainData;
    }

}
