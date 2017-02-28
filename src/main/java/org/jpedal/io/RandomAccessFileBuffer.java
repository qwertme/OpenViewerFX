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
 * RandomAccessFileBuffer.java
 * ---------------
 */

package org.jpedal.io;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;

import org.jpedal.utils.LogWriter;

public class RandomAccessFileBuffer extends RandomAccessFile implements RandomAccessBuffer {

	private String fileName="";

    public RandomAccessFileBuffer(final String file, final String mode) throws FileNotFoundException
  {
  	
    super(file, mode);
    fileName=file;
  
  }
  
  @Override
  public byte[] getPdfBuffer(){
	  
  	final URL url;
  	byte[] pdfByteArray = null;
  	final InputStream is;
  	final FastByteArrayOutputStream os;
  	
  	try {
  		url = new URL("file:///"+fileName);
  		
  		is = url.openStream();
  		os = new FastByteArrayOutputStream();
  		
  		// Download buffer
  		final byte[] buffer = new byte[4096];
  		
  		// Download the PDF document
  		int read;
  		while ((read = is.read(buffer)) != -1) {
  			os.write(buffer, 0 ,read);
  		}

  		// Close streams
  		is.close();

  		// Copy output stream to byte array
  		pdfByteArray = os.toByteArray();
  		
  	} catch (final IOException e) {
  		e.printStackTrace();
  		if(LogWriter.isOutput()) {
              LogWriter.writeLog("[PDF] Exception " + e + " getting byte[] for " + fileName);
          }
  	}
  	
  	return pdfByteArray;
  }
}
