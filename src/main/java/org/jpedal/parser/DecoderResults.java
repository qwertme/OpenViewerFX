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
 * DecoderResults.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.constants.PageInfo;

import java.util.Iterator;

public class DecoderResults {

    private boolean imagesProcessedFully=true,hasNonEmbeddedCIDFonts,hasYCCKimages,pageSuccessful;

    private boolean ttHintingRequired;

    //values on last decodePage
    private Iterator colorSpacesUsed;

    private String nonEmbeddedCIDFonts="";

    private boolean tooManyShapes;
    /**
     * flag to show embedded fonts present
     */
    private boolean hasEmbeddedFonts;
    
    public boolean getImagesProcessedFully() {
        return imagesProcessedFully;
    }

    public void update(final PdfStreamDecoder current, final boolean includeAll) {

        colorSpacesUsed= (Iterator) current.getObjectValue(PageInfo.COLORSPACES);


        nonEmbeddedCIDFonts= (String) current.getObjectValue(DecodeStatus.NonEmbeddedCIDFonts);

        hasYCCKimages = current.getBooleanValue(DecodeStatus.YCCKImages);
        pageSuccessful = current.getBooleanValue(DecodeStatus.PageDecodingSuccessful);

        imagesProcessedFully = current.getBooleanValue(DecodeStatus.ImagesProcessed);
        tooManyShapes = current.getBooleanValue(DecodeStatus.TooManyShapes);
        
        hasNonEmbeddedCIDFonts= current.getBooleanValue(DecodeStatus.NonEmbeddedCIDFonts);

        ttHintingRequired= current.getBooleanValue(DecodeStatus.TTHintingRequired);

        if(includeAll){
            hasEmbeddedFonts = current.getBooleanValue(ValueTypes.EmbeddedFonts);
        }

    }

    public boolean getPageDecodeStatus(final int status) {

        switch(status){
            case DecodeStatus.NonEmbeddedCIDFonts:
                return hasNonEmbeddedCIDFonts;

            case DecodeStatus.ImagesProcessed:
                return imagesProcessedFully;

            case DecodeStatus.PageDecodingSuccessful:
                return pageSuccessful;

            case DecodeStatus.YCCKImages:
                return hasYCCKimages;
            
            case DecodeStatus.TooManyShapes:
                return tooManyShapes;    

            case DecodeStatus.TTHintingRequired:
                return ttHintingRequired;

            default:
                throw new RuntimeException("Unknown parameter "+status);

        }
    }

    /**
     * return details on page for type (defined in org.jpedal.constants.PageInfo) or null if no values
     * Unrecognised key will throw a RunTime exception
     *
     * null returned if JPedal not clear on result
     */
    public Iterator getPageInfo(final int type) {
        switch(type){

            case PageInfo.COLORSPACES:
                return colorSpacesUsed;

            default:
                return null;
        }
    }

    /**
     * get page statuses
     */
    public String getPageDecodeStatusReport(final int status) {

        if(status==DecodeStatus.NonEmbeddedCIDFonts){
            return nonEmbeddedCIDFonts;
        }else {
            throw new RuntimeException("Unknown parameter");
        }
    }


    /**
     * shows if embedded fonts present on page just decoded
     */
    public boolean hasEmbeddedFonts() {
        return hasEmbeddedFonts;
    }

    public void resetColorSpaces() {
        //disable color list if forms
        colorSpacesUsed=null;
    }
}
