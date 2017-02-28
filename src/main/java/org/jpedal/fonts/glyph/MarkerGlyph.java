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
 * MarkerGlyph.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import org.jpedal.color.PdfPaint;

import java.awt.*;
import java.awt.geom.Area;
import java.io.Serializable;
import javafx.scene.shape.Path;


/**
 * holds data so we can draw glyph on first appearance
 *
 */
public class MarkerGlyph implements PdfGlyph, Serializable {


    public final float a;
    public final float b;
    public final float c;
    public final float d;
    public final String fontName;
    private int glyphNumber=-1;
	
	public MarkerGlyph(final float a, final float b, final float c, final float d, final String fontName) {

		this.a=a;
		this.b=b;
		this.c=c;                                                  
		this.d=d;
		this.fontName=fontName;
		
	}

    @Override
    public void setGlyphNumber(final int no) {
        glyphNumber = no;
    }

    @Override
    public int getGlyphNumber() {
        return glyphNumber;
    }

    @Override
    public void render(final int text_fill_type, final Graphics2D g2, final float scaling, final boolean isFormGlyph) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getmaxWidth() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setT3Colors(final PdfPaint strokeColor, final PdfPaint nonstrokeColor, final boolean lockColours) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean ignoreColors() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Area getShape() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
    public void setWidth(final float width) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public int getFontBB(final int type) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setStrokedOnly(final boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //use by TT to handle broken TT fonts
    @Override
    public boolean containsBrokenData() {
        return false;
    }

    @Override
    public Path getPath() {
        throw new UnsupportedOperationException("getPath Not supported yet.");
    }
}
