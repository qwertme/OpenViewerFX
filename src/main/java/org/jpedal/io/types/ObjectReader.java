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
 * ObjectReader.java
 * ---------------
 */
package org.jpedal.io.types;

import java.io.IOException;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.io.RandomAccessBuffer;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class ObjectReader {

    public boolean fileIsBroken;

    static final byte[] endPattern = { 101, 110, 100, 111, 98, 106 }; //pattern endobj

    public static final byte[] lengthString = { 47, 76, 101, 110, 103, 116, 104}; //pattern /Length

    public static final byte[] startStream = { 115, 116, 114, 101, 97, 109};

    private final RandomAccessBuffer pdf_datafile;
    private int newCacheSize=-1;

    private final long eof;
    
    final PdfFileReader currentPdfFile;

    public ObjectReader(final RandomAccessBuffer pdf_datafile, final long eof, final PdfFileReader currentPdfFile) {
        this.pdf_datafile=pdf_datafile;
        this.eof=eof;
        this.currentPdfFile=currentPdfFile;
        
    }
    
    public byte[] readObjectData(int bufSize, final PdfObject pdfObject){

        //old version
        if(bufSize<1 || newCacheSize!=-1 || fileIsBroken) {
            return readObjectDataXX(bufSize, pdfObject);
        }

        byte[] dataRead=null;
        
        //trap for odd file with no endobj
        if(bufSize>0){
            
            bufSize += 6;
            dataRead = new byte[bufSize];

            try{
                pdf_datafile.read(dataRead);
            } catch (final IOException ex) {
                LogWriter.writeLog("Unable to fill buffer "+ex);
            }
		}

        return dataRead;
    }

    private byte[] readObjectDataXX(int bufSize, final PdfObject pdfObject){

        int newCacheSize=-1,startStreamCount=0,charReached = 0,charReached3=0;
        boolean startStreamFound=false;
        boolean reachedCacheLimit=false;
        final boolean inStream=false;
        boolean inLoop=true;
        final long start=getPointer();

        if(pdfObject!=null) //only use if values found
        {
            newCacheSize = this.newCacheSize;
        }

        final int rawSize=bufSize;
        int realPos=0;
        boolean lengthSet=false; //start false and set to true if we find /Length in metadata

        if(bufSize<1) {
            bufSize = 128;
        }

        if(newCacheSize!=-1 && bufSize>newCacheSize) {
            bufSize = newCacheSize;
        }

        byte[] dataRead=null;
        byte currentByte ;
        int i=bufSize-1;

        /**read the object or block adjust buffer if less than bytes left in file*/
        while (inLoop) {

            i++;

            if(i==bufSize){

                /** read the next block and adjust buffer if less than bytes left in file*/
                final long pointer = getPointer();

                if (pointer + bufSize > eof) {
                    bufSize = (int) (eof - pointer);
                }

                //trap for odd file with no endobj
                if(bufSize==0) {
                    break;
                }

                bufSize += 6;
                byte[] buffer = new byte[bufSize];
                try {
                    pdf_datafile.read(buffer); //get data
                } catch (final IOException ex) {
                    LogWriter.writeLog("Unable to fill buffer "+ex);
                }

                /**
                 * allow for offset being wrong on first block and hitting part of endobj and cleanup so does not break later code
                 * and set DataRead to buffer
                 */
                if (dataRead==null) {
                    int j = 0;
                    
                    //check first 10 bytes
                    for(int i2=0;i2<10;i2++){
                        if(buffer[i2] == 'e' && buffer[i2+1] == 'n' && buffer[i2+2] == 'd' && buffer[i2+3] == 'o' && buffer[i2+4] == 'b' && buffer[i2+5] == 'j'){
                            j=i2;
                            break;
                        }
                    }
                    
                    while (buffer[j] == 'e' || buffer[j] == 'n' || buffer[j] == 'd' || buffer[j] == 'o' || buffer[j] == 'b' || buffer[j] == 'j') {
                        j++;
                    }
                    
                    if (j > 0) { //adjust to remove stuff at start
                        final byte[] oldBuffer = buffer;
                        final int newLength = buffer.length - j;
                        buffer = new byte[newLength];
                        System.arraycopy(oldBuffer, j, buffer, 0, newLength);
                    
                        bufSize=buffer.length;
                    }

                    dataRead=buffer;
                    
                }else{
                    dataRead = appendDataBlock(buffer.length,buffer, dataRead);
                }

                i=0;
            }

            currentByte = dataRead[realPos];

            if(!inStream){ /**check for endobj at end - reset if not*/
                if (currentByte == endPattern[charReached]) {
                    charReached++;
                } else{
                    charReached = 0;
                }
            }

            //look for start of stream and set inStream true
            if(!startStreamFound && newCacheSize!=-1 && !reachedCacheLimit){
                if (startStreamCount<6 && currentByte == startStream[startStreamCount]){
                    startStreamCount++;

                    if(startStreamCount == 6) //stream start found so log
                    {
                        startStreamFound = true;
                    }
                }else {
                    startStreamCount = 0;
                }
            }

            //switch on caching
            if(startStreamFound && dataRead!=null &&dataRead.length>newCacheSize){ //stop if over max size
                pdfObject.setCache(start,currentPdfFile);
                reachedCacheLimit=true;
            }

            //also scan for /Length if it had a valid size - if length not set we go on endstream in data
            if(!startStreamFound && !lengthSet && rawSize!=-1){
                if(currentByte == lengthString[charReached3] &&  !inStream){
                    charReached3++;
                    if(charReached3==6) {
                        lengthSet = true;
                    }
                }else {
                    charReached3 = 0;
                }
            }

            realPos++;

            if (charReached == 6){

                if(!lengthSet) {
                    inLoop = false;
                }

                charReached=0;
            }

            if(lengthSet && realPos>rawSize) {
                inLoop = false;
            }
        }

        if(!lengthSet) {
            dataRead = ObjectUtils.checkEndObject(dataRead);
        }

        return dataRead;
    }

    static byte[] appendDataBlock( final int newBytes, final byte[] buffer, final byte[] dataRead) {
        
        final int bytesRead=dataRead.length;
        
        final byte[] tmp=new byte[bytesRead+newBytes];

        //existing data into new array
        System.arraycopy(dataRead, 0, tmp, 0, bytesRead);
        System.arraycopy(buffer, 0, tmp, bytesRead, newBytes);

        return tmp;
    }
    
    //////////////////////////////////////////////////
    /**
     * gets pointer to current location in the file
     */
    private long getPointer()
    {
        long old_pointer = 0;
        try{
            old_pointer = pdf_datafile.getFilePointer();
        }catch( final Exception e ){
        	if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " getting pointer in file");
            }
        }
        return old_pointer;
    }
    
     /**
     * set size over which objects kept on disk
     */
    public void setCacheSize(final int miniumumCacheSize) {

        newCacheSize=miniumumCacheSize;

    }

}


