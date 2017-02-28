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
 * RandomHandler.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * class performs LittleEndian, BigEndian readings in tiff file
 */
public class RandomHandler {

    public static final int BIGENDIAN = 1;
    public static final int LITTLEENDIAN = 2;

    private int byteOrder = 1;
    private ByteBuffer buffer;
    private RandomAccessFile raf;
    private final boolean isBuff;

    public RandomHandler(byte[] data) {
        buffer = ByteBuffer.wrap(data);
        isBuff = true;
    }

    public RandomHandler(RandomAccessFile raf) {
        this.raf = raf;
        isBuff = false;
    }

    public int getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(int byteOrder) {
        this.byteOrder = byteOrder;
        if (isBuff) {
            if (byteOrder == LITTLEENDIAN) {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            } else {
                buffer.order(ByteOrder.BIG_ENDIAN);
            }
        }
    }

    public void get(byte[] temp) throws IOException {
        if (isBuff) {
            buffer.get(temp);
        } else {
            raf.read(temp);
        }
    }

    public int getUint8() throws IOException {
        if (isBuff) {
            return buffer.get() & 0xff;
        } else {
            return raf.read();
        }
    }

    public int getUint8(int index) throws IOException {
        if (isBuff) {
            return buffer.get(index) & 0xff;
        } else {
            raf.seek(index);
            return raf.read();
        }
    }

    public int getUint16() throws IOException {
        if (isBuff) {
            return buffer.getShort() & 0xffff;
        } else {
            if (byteOrder == BIGENDIAN) {
                return raf.readUnsignedShort();
            } else {
                int a = raf.read();
                int b = raf.read();
                return b << 8 | a;
            }
        }
    }

    public int getUint16(int index) throws IOException {
        if (isBuff) {
            return buffer.getShort(index) & 0xffff;
        } else {
            raf.seek(index);
            if (byteOrder == BIGENDIAN) {
                return raf.readUnsignedShort();
            } else {
                int a = raf.read();
                int b = raf.read();
                return b << 8 | a;
            }
        }
    }

    public int getInt() throws IOException {
        if (isBuff) {
            return buffer.getInt();
        } else {
            if (byteOrder == BIGENDIAN) {
                return raf.readInt();
            } else {
                int a = raf.read();
                int b = raf.read();
                int c = raf.read();
                int d = raf.read();
                return d << 24 | c << 16 | b << 8 | a;
            }
        }
    }

    public int getInt(int index) throws IOException {
        if (isBuff) {
            return buffer.getInt(index);
        } else {
            raf.seek(index);
            if (byteOrder == BIGENDIAN) {
                return raf.readInt();
            } else {
                int a = raf.read();
                int b = raf.read();
                int c = raf.read();
                int d = raf.read();
                return d << 24 | c << 16 | b << 8 | a;
            }
        }
    }

    public long getLong() throws IOException {
        if (isBuff) {
            return buffer.getLong();
        } else {
            if (byteOrder == BIGENDIAN) {
                return raf.readLong();
            } else {
                long a = raf.read();
                long b = raf.read();
                long c = raf.read();
                long d = raf.read();
                long e = raf.read();
                long f = raf.read();
                long g = raf.read();
                long h = raf.read();
                return h << 56 | g << 48 | f << 40 | e << 32 | d << 24 | c << 16 | b << 8 | a;
            }
        }
    }

    public int getPosition() throws IOException {
        if (isBuff) {
            return buffer.position();
        } else {
            return (int) raf.getFilePointer();
        }
    }

    public void setPosition(int position) throws IOException {
        if (isBuff) {
            buffer.position(position);
        } else {
            raf.seek(position);
        }
    }

}
