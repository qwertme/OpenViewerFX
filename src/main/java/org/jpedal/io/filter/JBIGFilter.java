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
 * JBIGFilter.java
 * ---------------
 */

package org.jpedal.io.filter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Map;

import org.jpedal.io.JBIG2;
import org.jpedal.jbig2.*;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

public class JBIGFilter extends BaseFilter implements PdfFilter {
    private byte[] globalData;

    public JBIGFilter(final PdfObject decodeParms) {
        super(decodeParms);
        if(decodeParms!=null){
            final PdfObject Globals=decodeParms.getDictionary(PdfDictionary.JBIG2Globals);
            if(Globals!=null) {
                globalData = Globals.getDecodedStream();
            }
        }
    }

    @Override
    public byte[] decode(final byte[] data) throws Exception {
      byte [] dataBytes;
      
      //
      dataBytes=JBIG2.JBIGDecode(data, globalData);      
      /**/   
      
      return dataBytes;  
    }

    @Override
    public void decode(final BufferedInputStream bis,
            final BufferedOutputStream streamCache, final String cacheName,
            final Map cachedObjects) throws Exception {
            throw new Exception("JBIG should not be decoded in this way");
    }

}
