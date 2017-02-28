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
 * PdfStreamDecoderForSampling.java
 * ---------------
 */

package org.jpedal.parser;

import org.jpedal.external.ExternalHandlers;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

public class PdfStreamDecoderForSampling extends PdfStreamDecoder{

    PdfStreamDecoderForSampling(final PdfObjectReader currentPdfFile){

        super(currentPdfFile);

    }

    /**
     *
     *  just scan for DO and CM to get image sizes so we can work out sampling used
     */
    public final float decodePageContentForImageSampling(final PdfObject pdfObject) {/* take out min's%%*/

        try{

            parserOptions.setRenderDirectly(true);

            //check switched off
            parserOptions.imagesProcessedFully=true;

            //reset count
            imageCount=0;


            gs = new GraphicsState(0,0);/* take out min's%%*/

            //get the binary data from the file
            final byte[] b_data;

            byte[][] pageContents= null;
            if(pdfObject !=null){
                pageContents= pdfObject.getKeyArray(PdfDictionary.Contents);
                isDataValid= pdfObject.streamMayBeCorrupt();
            }

            if(pdfObject !=null && pageContents==null) {
                b_data = currentPdfFile.readStream(pdfObject, true, true, false, false, false, pdfObject.getCacheName(currentPdfFile.getObjectReader()));
            } else if(pageStream!=null) {
                b_data = pageStream;
            } else {
                b_data = currentPdfFile.getObjectReader().readPageIntoStream(pdfObject);
            }

            //if page data found, turn it into a set of commands
            //and decode the stream of commands
            if (b_data!=null && b_data.length > 0) {
                getSamplingOnly=true;
                decodeStreamIntoObjects(b_data, false);
            }

            //flush fonts
            cache.resetFonts();

            return parserOptions.getSamplingUsed();

        }catch(final Error err){
            errorTracker.addPageFailureMessage("Problem decoding page " + err);
            if (ExternalHandlers.throwMissingCIDError && err.getMessage()!=null && err.getMessage().contains("kochi")) {
                throw err;
            }
        }

        return -1;
    }

}
