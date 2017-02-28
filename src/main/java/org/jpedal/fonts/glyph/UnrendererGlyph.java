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
 * UnrendererGlyph.java
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
public class UnrendererGlyph implements PdfGlyph, Serializable {


    public final float x;
    public final float y;
    public final int rawInt;
    public final float currentWidth;
	
	public UnrendererGlyph(final float x, final float y, final int rawInt, final float currentWidth) {

		this.x=x;
		this.y=y;
		this.rawInt=rawInt;
		this.currentWidth=currentWidth;
		
	}

    @Override
    public void setGlyphNumber(final int no) {

    }

    @Override
    public int getGlyphNumber() {
        return -1;
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