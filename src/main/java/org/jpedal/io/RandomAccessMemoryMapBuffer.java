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
 * RandomAccessMemoryMapBuffer.java
 * ---------------
 */

package org.jpedal.io;

import org.jpedal.utils.LogWriter;

import java.io.*;

public class RandomAccessMemoryMapBuffer implements RandomAccessBuffer {

    //private byte[] data;
    private long pointer;

    private int length;

    private File file;
    private RandomAccessFile buf;

    public RandomAccessMemoryMapBuffer(final InputStream in)
    {

        this.pointer = -1;

        length=0;

        FileOutputStream to=null;
        BufferedInputStream from=null;

        try {

            file=File.createTempFile("page",".bin", new File(ObjectStore.temp_dir));
            //file.deleteOnExit();

            to =new java.io.FileOutputStream(file);

            from=new BufferedInputStream(in);

			//write
			final byte[] buffer = new byte[65535];
			int bytes_read;
			while ((bytes_read = from.read(buffer)) != -1){
				to.write(buffer, 0, bytes_read);
                length += bytes_read;
            }

        } catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //

        }
		//close streams
		try {
            if(to!=null) {
                to.close();
            }
            if(from!=null) {
                from.close();
            }
		} catch (final Exception e) {
			if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " closing files");
            }
		}

        try {
            init();
        } catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }


    }

    private void init() throws Exception{

        // Create a read-only memory-mapped file
        buf=new RandomAccessFile(file, "r");

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

        if(buf!=null){

            buf.close();
            buf=null;
        }


        this.pointer = -1;

        if(file!=null && file.exists()){
           file.delete();
        }
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

        if (buf!=null) {
            return length;
        } else {
            throw new IOException("Data buffer not initialized.");
        }
    }

    @Override
    public int read() throws IOException {
        if (checkPos(this.pointer)) {
            buf.seek(pointer++);
            return b2i(buf.readByte());
        } else {
            return -1;
        }
    }

    private int peek() throws IOException {
        if (checkPos(this.pointer)) {
            buf.seek(pointer++);
            return b2i(buf.readByte());
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

        if (buf==null) {
            throw new IOException("Data buffer not initialized.");
        }

        if (pointer<0 || pointer>=length) {
            return -1;
        }

        int length=this.length-(int)pointer;
        if(length>b.length) {
            length = b.length;
        }
 
        //replaced inefficient code with an improved performance version (up to ~60 %)
        //buf.seek(pointer);
        //buf.read(b);
        //pointer += b.length;
        
        for (int i=0; i<length; i++) {
            buf.seek(pointer++);
            b[i] = buf.readByte();
        }
        return length;
    }

    private static int b2i(final byte b) {
        if (b>=0) {
            return b;
        }
        return 256+b;
    }

    private boolean checkPos(final long pos) throws IOException {
        return ( (pos>=0) && (pos<length()) );
    }

    /* returns the byte data*/
    @Override
    public byte[] getPdfBuffer(){

        final byte[] bytes=new byte[length];
        try {
            buf.seek(0);
            buf.read(bytes);
        } catch (final IOException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        return bytes;
    }
}

