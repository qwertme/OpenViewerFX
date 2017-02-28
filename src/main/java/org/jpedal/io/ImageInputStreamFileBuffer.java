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
 * ImageInputStreamFileBuffer.java
 * ---------------
 */

package org.jpedal.io;

import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.FastByteArrayOutputStream;

import javax.imageio.stream.ImageInputStream;
import java.io.*;

public class ImageInputStreamFileBuffer implements RandomAccessBuffer {

	private static final String fileName="";

    final ImageInputStream iis;

    @SuppressWarnings("UnusedDeclaration")
    public ImageInputStreamFileBuffer(final ImageInputStream iis) {
        this.iis=iis;
    }

    @Override
    public long getFilePointer() throws IOException {
        return iis.getStreamPosition();
    }

    @Override
    public void seek(final long pos) throws IOException {
        iis.seek(pos);
    }

    @Override
    public int read() throws IOException {
        return iis.read();
    }

    @Override
    public String readLine() throws IOException {
        return iis.readLine();
    }

    @Override
    public long length() throws IOException {
        return iis.length();
    }

    @Override
    public void close() throws IOException {
        iis.close();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return iis.read(b);
    }

    @Override
    public byte[] getPdfBuffer(){

  	byte[] pdfByteArray = null;
  	final FastByteArrayOutputStream os;
  	
  	try {
  		os = new FastByteArrayOutputStream();
  		
  		// Download buffer
  		final byte[] buffer = new byte[4096];
  		
  		// Download the PDF document
  		int read;
  		while ((read = iis.read(buffer)) != -1) {
  			os.write(buffer, 0 ,read);
  		}

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
