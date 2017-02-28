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
 * HTMLTextUtils.java
 * ---------------
 */
package org.jpedal.parser.text;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;

/**
 *
 * @author markee
 */
class HTMLTextUtils {

    static boolean remapGlyph(PdfFont currentFontData, GlyphData glyphData) {

        boolean alreadyRemaped = false;

        final String charGlyph = currentFontData.getMappedChar(glyphData.getRawInt(), false);

                //fix for PDFdata/sample_pdfs_html/general-July2012/klar--men-aldri-ferdig_dacecc.pdf
        //in some examples the unicode table is wrong and maps the character into this odd range, but the glyph is always correct
        //this is a sanity check to fix this mapping issue
        if (charGlyph != null && !glyphData.getDisplayValue().isEmpty()
                && glyphData.getDisplayValue().charAt(0) < 32 && currentFontData.getDiffChar(charGlyph) == -1) {

            final int altValue = StandardFonts.getAdobeMap(charGlyph);

                //this test can return -1 for invalid value as in sample_pdfs_html/general-May2014/18147.pdf
            //which breaks code further down so we reject this value
            if (altValue > -1 && currentFontData.getMappedChar(altValue, false)!=null) {
                
                glyphData.setRawInt(altValue);
                glyphData.set(String.valueOf((char) altValue));
                alreadyRemaped = true;
            }
        }

        return alreadyRemaped;
    }
}
