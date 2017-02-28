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
 * PngChunk.java
 * ---------------
 */
package com.idrsolutions.image.png;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 *
 */
public class PngChunk {

    public static final byte[] SIGNATURE = {-119, 80, 78, 71, 13, 10, 26, 10};
    public static final byte[] IHDR = {73, 72, 68, 82};
    public static final byte[] PLTE = {80, 76, 84, 69};
    public static final byte[] tRNS = {116, 82, 78, 83};
    public static final byte[] IDAT = {73, 68, 65, 84};
    public static final byte[] IEND = {73, 69, 78, 68};

    private final byte[] length;
    private final byte[] name;
    private final byte[] data;

    public PngChunk(final byte[] length, final byte[] name, final byte[] data) {
        this.length = length;
        this.name = name;
        this.data = data;
    }

    /**
     * Create An Header Chunk for PNG
     *
     * @return Header Chunk
     * @param width image width
     * @param height image height
     * @param bitDepth valid values are 1,2,4,8 and 16
     * @param colorType valid values are 0,2,3,4 and 6 <br/>
     * Gray scale normal= color type 0 : allowed bit depths 1,2,4,8,16 <br/>
     * RGB Triple normal= color type 2 : allowed bit depths 8,16 <br/>
     * Palette index = color type 3 : allowed bit depths 1,2,4,8 (PLTE chunk
     * must appear)<br/>
     * Gray scale Alpha = color type 4 : allowed bit depths 8,16 followed by
     * alpha sample<br/>
     * RGB Triple Alpha = color type 6 : allowed bit depths 8,16 followed by
     * alpha sample<br/>
     * @param compression default is 0
     * @param filter default is 0
     * @param interlace 0 (no interlace) or 1 (Adam7 interlace). *
     */
    public static PngChunk createHeaderChunk(final int width, final int height, final byte bitDepth,
            final byte colorType, final byte compression, final byte filter,
            final byte interlace) {
        final ByteBuffer buff = ByteBuffer.allocate(13);
        buff.putInt(width);
        buff.putInt(height);
        buff.put(bitDepth);
        buff.put(colorType);
        buff.put(compression);
        buff.put(filter);
        buff.put(interlace);
        final byte[] data = buff.array();
        return new PngChunk(intToBytes(13), PngChunk.IHDR, data);
    }

    public static PngChunk createPaleteChunk(final byte[] palBytes) {
        return new PngChunk(intToBytes(palBytes.length), PngChunk.PLTE, palBytes);
    }
    
    public static PngChunk createTrnsChunk(final byte[] trnsBytes){
        return new PngChunk(intToBytes(trnsBytes.length), PngChunk.tRNS, trnsBytes);
    }

    public static PngChunk createDataChunk(final byte[] zLibBytes) {
        return new PngChunk(intToBytes(zLibBytes.length), PngChunk.IDAT, zLibBytes);
    }

    public static PngChunk createEndChunk() {
        return new PngChunk(intToBytes(0), PngChunk.IEND, new byte[]{});
    }

    public byte[] getCRCValue() {
        final CRC32 crc32 = new CRC32();
        crc32.update(name);
        crc32.update(data);
        final byte[] temp = longToBytes(crc32.getValue());
        return new byte[]{temp[4], temp[5], temp[6], temp[7]};
    }

    public static byte[] intToBytes(final int value) {
        return new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
    }

    private static byte[] longToBytes(final long value) {
        return new byte[]{
            (byte) (value >> 56), (byte) (value >> 48), (byte) (value >> 40), (byte) (value >> 32),
            (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
    }

    public byte[] getLength() {
        return length;
    }

    public byte[] getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }
    
    

}
