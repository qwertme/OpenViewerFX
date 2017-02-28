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
 * BrokenRefTable.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.RandomAccessBuffer;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class BrokenRefTable {

     /**
     * find a valid offsetRef
     */
    public static String findOffsets(final RandomAccessBuffer pdf_datafile, final Offsets offset) throws PdfSecurityException {

    	if(LogWriter.isOutput()) {
            LogWriter.writeLog("Corrupt xref table - trying to find objects manually");
        }

        String root_id = "",line=null;
        int pointer,i=0;

        try {
            pdf_datafile.seek(0);
        } catch (final Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " reading line");
            }
        }
        
        while (true) {

            try {
                i = (int) pdf_datafile.getFilePointer();
                line = pdf_datafile.readLine();
            } catch (final Exception e) {
            	if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " reading line");
                }
            }
            if (line == null) {
                break;
            }

            if (line.contains(" obj")) {

                pointer = line.indexOf(' ');
                
                if (pointer > 0) {
                    offset.storeObjectOffset(Integer.parseInt(line.substring(0, pointer)), i, 1, false, true);
                }
                
            } else if (line.contains("/Root")) {

                final int start = line.indexOf("/Root") + 5;
                pointer = line.indexOf('R', start);
                if (pointer > -1) {
                    root_id = line.substring(start, pointer + 1).trim();
                }
            } else if (line.contains("/Encrypt")) {
                //too much risk on corrupt file
                throw new PdfSecurityException("Corrupted, encrypted file");
            }
        }

        return root_id;
    }

}


