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
 * FontData.java
 * ---------------
 */
package org.jpedal.fonts.objects;

import org.jpedal.utils.LogWriter;

import java.io.*;

/**
 * provides access to font data and caches large objects
 */
public class FontData {

    private byte[] fontData;

    /**flag to show if all fontData in memory or just some*/
    private boolean isInMemory;

    /**real size of font object*/
    private int fullLength;

    /**offset to actual block loaded*/
    private int offset;

    /**bytes size of font we keep in memory*/
    public static int maxSizeAllowedInMemory=-1;

    /**max size of data kept in memory at any one point*/
    private int blockSize=8192;

    private RandomAccessFile fontFile;

    /**
     * pass in name of temp file on disk so we can just read part at a time -
     * if not part of PDF user is responsible for deleting
     * @param cachedFile
     */
    public FontData(final String cachedFile){//, byte[] stream) {

        try{
            fontFile = new RandomAccessFile( cachedFile, "r" );
            fullLength = (int) fontFile.length();

        }catch( final Exception e ){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        //if small read all
        if(fullLength<maxSizeAllowedInMemory){

            blockSize=maxSizeAllowedInMemory;

            adjustForCache(0);

            isInMemory=true;

        }
    }

    public byte getByte(int pointer){

    	if(!isInMemory) {
            pointer = adjustForCache(pointer);
        }

       // System.err.println("Now="+pointer+" "+fontData.length+" inMemory="+isInMemory);

        if(pointer>=fontData.length) {
            return 0;
        } else {
            return fontData[pointer];
        }
    }

    /**
     * check block in memory, read if not and
     * adjust pointer
     * @param pointer
     * @return
     */
    private int adjustForCache(final int pointer) {

        //see if in memory and load if not
        if(fontData==null || pointer<offset || pointer>=(offset+blockSize-1)){

            try {

                fontFile.seek(pointer);
                fontData=new byte[blockSize];

                fontFile.read(fontData);

            } catch (final IOException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //            }
            }

            offset=pointer;

        }

        //subtract offset to make it fall in loaded range
        return pointer-offset;
    }

    private int adjustForCache(final int pointer, final int blockSize) {

        //see if in memory and load if not
        //if(fontData==null || pointer<offset || pointer>=(offset+blockSize-1)){

            try {

                fontFile.seek(pointer);
                fontData=new byte[blockSize];

                fontFile.read(fontData);

            } catch (final IOException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //            }
            }
            offset=pointer;

        //}

        //subtract offset to make it fall in loaded range
        return pointer-offset;
    }

    public byte[] getBytes(int startPointer, final int length){

        if(!isInMemory) {
            startPointer = adjustForCache(startPointer, length + 1);
        }

        final byte[] block=new byte[length];
        System.arraycopy(fontData,startPointer,block,0,length);
        return block;

    }

    /**total length of FontData in bytes*/
    public int length() {

        if(isInMemory) {
            return fontData.length;
        } else {
            return fullLength;
        }
    }
    
    public void close(){
    	if(fontFile!=null) {
            try {
                fontFile.close();
            } catch (final IOException e) {
                //tell user and log
                if (LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
        }
    }
}
