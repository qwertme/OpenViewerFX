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
 * ThumbnailDecoder.java
 * ---------------
 */
package org.jpedal;


import org.jpedal.exception.PdfException;
import org.jpedal.utils.LogWriter;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.jpedal.objects.acroforms.AcroRenderer;

/**
 * generates thumbnails of pages for display
 */
public class ThumbnailDecoder {
    
    private final PdfDecoderInt decode_pdf;
    
    public ThumbnailDecoder(final PdfDecoderInt decode_pdf) {
        
        this.decode_pdf=decode_pdf;
        
    }
    
    /**
     * get pdf as Image of any page scaling is size (100 = full size)
     */
    public final synchronized BufferedImage getPageAsThumbnail(final int pageNumber, final int height) {
        
        BufferedImage newImg =null;
        //stopDecoding=false;
        try {
            
            //this is used in Viewer so we need to over-ride implicit assumption            
            //forms rendered as components just for thumbnail
            final AcroRenderer formRenderer=decode_pdf.getFormRenderer();
            final boolean originalRasterize = formRenderer.getCompData().formsRasterizedForDisplay();
            formRenderer.getCompData().setRasterizeForms(true);
            
            //Produce image at 50% scaling to prevent massive memory issues at large scalings
            //Also improves thumbnail quality at higher resolutions
            final float scaling = decode_pdf.getScaling();
            if(scaling>0.5f) {
                decode_pdf.setScaling(0.5f);
            }
            final BufferedImage pageImage = decode_pdf.getPageAsImage(pageNumber);
            if(scaling>0.5f) {
                decode_pdf.setScaling(scaling);
            }
            
            formRenderer.getCompData().setRasterizeForms(originalRasterize);
            
            final int imgHeight = pageImage.getHeight();
            final double scale = height/(double)imgHeight;
            final int width = (int)(pageImage.getWidth()*scale);
            
            newImg = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            final Graphics g = newImg.getGraphics();
            g.drawImage(pageImage, 0, 0, width, height, null);
            g.dispose();
            
        } catch (final PdfException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        return newImg;
        
    }   
}
