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
 * Trailer.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.IOException;
import org.jpedal.io.RandomAccessBuffer;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class Trailer {
    
    private static final byte[] EOFpattern = { 37, 37, 69, 79, 70 }; //pattern %%EOF
    
    private static final byte[] trailerpattern = { 't','r','a','i','l','e','r' }; //pattern %%EOF
    
    public static byte[] readTrailer(int bufSize, int pointer, final int eof, final RandomAccessBuffer pdf_datafile) {
        
        int charReached = 0, charReached2 = 0, trailerCount = 0;
        final int end = 4;
        
        /**read in the bytes, using the startRef as our terminator*/
        final FastByteArrayOutputStream bis = new FastByteArrayOutputStream();
        
        while (true) {
            
            /** adjust buffer if less than 1024 bytes left in file */
            if (pointer + bufSize > eof) {
                bufSize = eof - pointer;
            }
            
            if(bufSize==0) {
                break;
            }
            
            final byte[] buffer=new byte[bufSize];
            
            try {
                pdf_datafile.seek(pointer);
                pdf_datafile.read(buffer); //get next chars
            } catch (final IOException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
            
            
            boolean endFound = false;
            
            /** write out and lookf for startref at end */
            for (int i = 0; i < bufSize; i++) {
                
                final byte currentByte = buffer[i];
                
                /** check for startref at end - reset if not */
                if (currentByte == EOFpattern[charReached]) {
                    charReached++;
                } else {
                    charReached = 0;
                }
                
                /** check for trailer at end - ie second spurious trailer obj */
                if (currentByte == trailerpattern[charReached2]) {
                    charReached2++;
                } else {
                    charReached2 = 0;
                }
                
                if (charReached2 == 7) {
                    trailerCount++;
                    charReached2 = 0;
                }
                
                if (charReached == end || trailerCount == 2) { //located %%EOF and get last few bytes
                    
                    for (int j = 0; j < i + 1; j++) {
                        bis.write(buffer[j]);
                    }
                    
                    i = bufSize;
                    endFound = true;
                    
                }
            }
            
            //write out block if whole block used
            if (!endFound) {
                bis.write(buffer);
            }
            
            //update pointer
            pointer += bufSize;
            
            if (charReached == end || trailerCount == 2) {
                break;
            }
        }
        
        return bis.toByteArray();
        
    }
}


