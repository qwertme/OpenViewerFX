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
 * JavaFXSupportImpl.java
 * ---------------
 */
package org.jpedal.fonts.glyph.javafx;

import org.jpedal.fonts.glyph.T1GlyphFX;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.gui.javafx.JavaFXCommandListener;
import org.jpedal.fonts.glyph.JavaFXSupport;
import org.jpedal.fonts.glyph.PdfGlyph;

import org.jpedal.fonts.tt.*;
import org.jpedal.fonts.tt.hinting.TTVM;
import org.jpedal.objects.FXClip;
import org.jpedal.objects.JavaFXShape;
import org.jpedal.objects.PdfClip;
import org.jpedal.objects.PdfShape;

/**
 *
 * @author markee
 */
public class JavaFXSupportImpl extends JavaFXSupport {

    public JavaFXSupportImpl() {
    }

    @Override
    public PdfGlyph getGlyph(final Glyf currentGlyf, final FontFile2 fontTable, final Hmtx currentHmtx, final int idx, final float unitsPerEm, final TTVM vm, final String baseFontName) {

        PdfGlyph currentGlyph;

        if (TTGlyph.useHinting) {
            currentGlyph = new TTGlyphFX(currentGlyf, fontTable, currentHmtx, idx, unitsPerEm, vm);
        } else {
            currentGlyph = new TTGlyphFX(currentGlyf, fontTable, currentHmtx, idx, unitsPerEm, baseFontName);
        }
        
        return currentGlyph;
    }
    
    @Override
    public PdfGlyph getGlyph(final float[] x, final float[] y, final float[] x2, final float[] y2, final float[] x3, final float[] y3,
                             final float ymin, final int end, final int[] commands) {
       return new T1GlyphFX(x,y,x2,y2,x3,y3,ymin,end,commands);
    }
    
    @Override
    public Object getCommandHandler(Object currentCommands) {
        return new JavaFXCommandListener((Commands)currentCommands);
    }
                
    @Override
    public PdfShape getFXShape() {
        return new JavaFXShape();
    }
    
    @Override
    public PdfClip getFXClip() {
        return new FXClip();
    }
    
}
