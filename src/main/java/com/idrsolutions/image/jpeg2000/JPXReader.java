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
 * JPXReader.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

import java.nio.ByteBuffer;

/**
 *
 */
public class JPXReader {

    private final ByteBuffer buffer;

    public JPXReader(byte[] data) {
        buffer = ByteBuffer.wrap(data);       
    }
    
    public int getLimit(){
        return buffer.limit();
    }

    public byte readByte() {
        return buffer.get();
    }

    public byte readByte(int index) {
        return buffer.get(index);
    }

    public int readUByte() {
        return buffer.get() & 0xff;
    }

    public byte[] readBytes(int length) {
        final byte[] dst = new byte[length];
        buffer.get(dst);
        return dst;
    }

    public byte[] readBytes(int offset, int length) {
        final byte[] dst = new byte[length];
        buffer.get(dst, offset, length);
        return dst;
    }

    public int readUnsignedByte(int index) {
        return buffer.get(index) & 0xff;
    }

    public int readUShort() {
        return buffer.getShort() & 0xFFFF;
    }

    public int readUShort(int index) {
        return buffer.getShort(index)& 0xFFFF;
    }

    public short readShort() {
        return buffer.getShort();
    }

    public short readShort(int index) {
        return buffer.getShort(index);
    }

    public int readInt() {
        return buffer.getInt();
    }

    public int readInt(int index) {
        return buffer.getInt(index);
    }

    public int getPosition() {
        return buffer.position();
    }

    public long readLong() {
        return buffer.getLong();
    }

    public long readLong(int index) {
        return buffer.getLong(index);
    }

    public void setPosition(int pos) {
        buffer.position(pos);
    }

    public int getRemaining() {
        return buffer.remaining();
    }

}
