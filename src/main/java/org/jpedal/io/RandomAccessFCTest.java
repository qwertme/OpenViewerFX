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
 * RandomAccessFCTest.java
 * ---------------
 */

package org.jpedal.io;

import org.jpedal.utils.LogWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class RandomAccessFCTest implements RandomAccessBuffer {

    //private byte[] data;
    private long pointer;

    private int length;

    private FileChannel fc;

    private MappedByteBuffer mb;

    public RandomAccessFCTest(final FileInputStream inFile)
    {

        try{

            length=inFile.available();
            fc= (inFile).getChannel();

            mb = fc.map( FileChannel.MapMode.READ_ONLY, 0L, fc.size( ) );

        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
    }

    @Override
    public long getFilePointer() throws IOException {
        return pointer;
    }

    @Override
    public void seek(final long pos) throws IOException {
        if ( checkPos(pos) ) {
            this.pointer = pos;
        } else {
            throw new IOException("Position out of bounds");
        }
    }

    @Override
    public void close() throws IOException {

        if(fc !=null){

            fc.close();
            fc =null;
        }

        if(mb !=null){

            mb =null;
        }

        this.pointer = -1;

    }

     /**/
     @Override
     protected void finalize(){

        try {
            super.finalize();
        } catch (final Throwable e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        //ensure removal actual file
        try {
            close();
        } catch (final IOException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

    } /**/

    @Override
    public long length() throws IOException {

    	if (mb !=null) {
            return length;
        } else {
            throw new IOException("Data buffer not initialized.");
        }
    }

    @Override
    public int read() throws IOException {

        if (checkPos(this.pointer)) {

            mb.position((int)pointer);

            pointer++;

            return mb.get();
        } else {
            return -1;
        }
    }

    private int peek() throws IOException {

        if (checkPos(this.pointer)) {

            mb.position((int)pointer);

            return mb.get();
        } else {
            return -1;
        }
    }

    /**
     * return next line (returns null if no line)
     */
    @Override
    public String readLine() throws IOException {

        if (this.pointer >= this.length - 1) {
            return null;
        } else {

            final StringBuilder buf = new StringBuilder();
            int c;
            while ((c = read()) >= 0) {
                if ((c == 10) || (c == 13)) {
                    if (((peek() == 10) || (peek() == 13)) && (peek() != c)) {
                        read();
                    }
                    break;
                }
                buf.append((char) c);
            }
            return buf.toString();
        }
    }

    @Override
    public int read(final byte[] b) throws IOException {

        if (mb ==null) {
            throw new IOException("Data buffer not initialized.");
        }

        if (pointer<0 || pointer>=length) {
            return -1;
        }

        int length=this.length-(int)pointer;
        if(length>b.length) {
            length = b.length;
        }

        for (int i=0; i<length; i++) {

            mb.position((int)pointer);

            pointer++;
            b[i] = mb.get();

        }

        return length;
    }

    private boolean checkPos(final long pos) throws IOException {
        return ( (pos>=0) && (pos<length()) );
    }

    /* returns the byte data*/
    @Override
    public byte[] getPdfBuffer(){

        final byte[] bytes=new byte[length];

        mb.position(0);
        mb.get(bytes);
        
        return bytes;
    }
}
