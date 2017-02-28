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
 * CFF.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import org.jpedal.fonts.Type1C;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.glyph.T1Glyphs;
import org.jpedal.fonts.glyph.GlyphFactory;
import org.jpedal.utils.LogWriter;

public class CFF extends Table {

    final PdfJavaGlyphs glyphs;

    boolean hasCFFdata;

    CFF(final FontFile2 currentFontFile, final boolean isCID){

        glyphs=new T1Glyphs(isCID);
        if(isCID) {
            glyphs.init(65536, true);
        }
        
        //move to start and check exists
		final int startPointer=currentFontFile.selectTable(FontFile2.CFF);

        //read 'cff' table
		if(startPointer!=0){

            try {
                final int length=currentFontFile.getTableSize(FontFile2.CFF);

                final byte[] data=currentFontFile.readBytes(startPointer, length) ;

                //initialise glyphs
                new Type1C(data,null,glyphs);

                hasCFFdata=true;
            } catch (final Exception e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
		}
    }

    public boolean hasCFFData() {
        return hasCFFdata;
    }


    public PdfGlyph getCFFGlyph(final GlyphFactory factory, final String glyph, final float[][] Trm, final int rawInt, final String displayValue, final float currentWidth, final String key) {

        return glyphs.getEmbeddedGlyph(factory, glyph, Trm, rawInt, displayValue, currentWidth, key);

    }
}
