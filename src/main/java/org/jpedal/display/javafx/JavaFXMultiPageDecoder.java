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
 * JavaFXMultiPageDecoder.java
 * ---------------
 */

package org.jpedal.display.javafx;

import org.jpedal.FileAccess;
import org.jpedal.PdfDecoderFX;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.MultiDisplayOptions;
import org.jpedal.display.MultiPagesDisplay;
import org.jpedal.display.MultiPageDecoder;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.FXDisplay;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
class JavaFXMultiPageDecoder extends MultiPageDecoder {
    
    JavaFXMultiPageDecoder(final GUIFactory gui,PdfDecoderInt pdf, PdfPageData pageData, MultiPagesDisplay display, MultiDisplayOptions multiDisplayOptions, DynamicVectorRenderer currentDisplay, int pageNumber, FileAccess fileAccess, PdfObjectReader io, AcroRenderer formRenderer, DecoderOptions options) {
        super(gui, pdf, pageData, display, multiDisplayOptions, currentDisplay, pageNumber, fileAccess, io, formRenderer, options);
    }
    
    @Override
    public void repaint() {
    //    System.out.println("Repaint FX page");
    }
    
    @Override
    public DynamicVectorRenderer getNewDisplay(int pageNumber) {
        
        return new FXDisplay(pageNumber,pdf.getObjectStore(), false);
    }
    
    @Override
    public void decodePage(final int page,int originalStart, int originalEnd) {
        
        try{
            pdf.decodePage(page);
        } catch (Exception ex) {
            
            //
            
            if(LogWriter.isOutput()){
                LogWriter.writeLog("Exception in JavaFX multipage view "+ex);
            }
        }      
    }  
    
    @Override
    public void decodeMorePages(int page, int originalStart, int originalEnd) {

        /**
         * Decode or get cached page data. If it is still in cache we just need a repaint
         */
        if (currentPageViews.get(page) == null) {

            decodePage(page, originalStart, originalEnd);
        }
       
        currentPageViews.put(page, "x");
        
        
        //some hacky code to make page navigation to work in continuous mode ; need to be fixed in due time   
        ((PdfDecoderFX) pdf).setPageNumber(originalStart);        
        
    }
}
