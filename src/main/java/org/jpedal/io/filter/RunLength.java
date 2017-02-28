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
 * RunLength.java
 * ---------------
 */
package org.jpedal.io.filter;

import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * RunLength
 */
public class RunLength extends BaseFilter implements PdfFilter {


    public RunLength(final PdfObject decodeParms) {
        super(decodeParms);
    }

    /**
     * Run length decode.
     */
    @Override
    public byte[] decode(final byte[] data) throws Exception {

        final FastByteArrayOutputStream bos;

        final int count;
        int len;
        int value;

        count=data.length;
        bos = new FastByteArrayOutputStream(count);

        for (int i = 0; i < count; i++) {

            len = data[i];

            if (len < 0) {
                len = 256 + len;
            }

            if (len == 128) {

                i = count;

            } else if (len > 128) {

                i++;

                len = 257 - len;

                value=data[i];


                for (int j = 0; j < len; j++){
                    bos.write(value);
                }

            } else {
                i++;
                len++;
                for (int j = 0; j < len; j++){


                    value=data[i+j];
                    bos.write(value);

                }

                i = i + len - 1;
            }
        }

        return bos.toByteArray();

    }


    /**
     * Run length decode. If both data and cached stream are present it will check
     * they are identical
     */
    @Override
    public void decode(final BufferedInputStream bis, final BufferedOutputStream streamCache, final String cacheName, final Map<String, String> cachedObjects) {

        this.bis=bis;
        this.streamCache=streamCache;
        this.cachedObjects=cachedObjects;

        try{

            final int count;
            int len;
            int nextLen;
            int value2;


            count=bis.available();

            for (int i = 0; i < count; i++) {

                nextLen=bis.read();
                if(nextLen>=128) {
                    nextLen -= 256;
                }

                len=nextLen;

                if (len < 0) {
                    len = 256 + len;
                }

                if (len == 128) {

                    i = count;

                } else if (len > 128) {

                    i++;

                    len = 257 - len;

                    value2=bis.read();
                    if(value2>=128) {
                        value2 -= 256;
                    }

                    for (int j = 0; j < len; j++){
                        streamCache.write(value2);
                    }

                } else {
                    i++;
                    len++;
                    for (int j = 0; j < len; j++){

                        value2=bis.read();
                        if(value2>=128) {
                            value2 -= 256;
                        }
                        streamCache.write(value2);
                    }

                    i = i + len - 1;
                }
            }
        } catch (final IOException e1) {

        	if(LogWriter.isOutput()) {
                LogWriter.writeLog("IO exception in RunLength "+e1);
            }
        }

    }


}
