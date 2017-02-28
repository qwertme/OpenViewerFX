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
 * BaseT1Glyph.java
 * ---------------
 */
package org.jpedal.fonts.glyph;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.shape.Path;
import org.jpedal.color.PdfPaint;

/**
 * <p>defines the current shape which is created by command stream</p> 
 * <p><b>This class is NOT part of the API</b></p>.
 * Shapes can be drawn onto pdf or used as a clip on other image/shape/text.
 * Shape is built up by storing commands and then turning these commands into a
 * shape. Has to be done this way as Winding rule is not necessarily
 * declared at start.
  */
public abstract class BaseT1Glyph implements Serializable, PdfGlyph
{
	
    protected float  glyfwidth=1000f;

    protected boolean isStroked;

    protected final Map strokedPositions=new HashMap();

    protected int glyphNumber = -1;

    public BaseT1Glyph(){}
    
    @Override
    public void setStrokedOnly(final boolean flag) {
        isStroked=flag;

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
    
    @Override
    public void setWidth(final float width) {
		this.glyfwidth=width;
		
	}

    @Override
    public int getGlyphNumber() {
        return glyphNumber;
    }

    @Override
    public void setGlyphNumber(final int no) {
        glyphNumber = no;
    }

    /* (non-Javadoc)
     * @see org.jpedal.fonts.PdfGlyph#getmaxWidth()
     */
    @Override
    public float getmaxWidth() {
    	
        return glyfwidth;
    }

	/* (non-Javadoc)
	 * @see org.jpedal.fonts.PdfGlyph#setT3Colors(java.awt.Color, java.awt.Color)
	 */
    @Override
    public void setT3Colors(final PdfPaint strokeColor, final PdfPaint nonstrokeColor, final boolean lockColours) {

	}


	/* (non-Javadoc)
	 * @see org.jpedal.fonts.PdfGlyph#ignoreColors()
	 */
    @Override
    public boolean ignoreColors() {
		return false;
	}
}
