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
 * PdfGlyph.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import java.awt.*;
import java.awt.geom.Area;
import javafx.scene.shape.Path;

import org.jpedal.color.PdfPaint;

/**
 * base glyph used by T1 and Truetype fonts
 */
public interface PdfGlyph {

    int getGlyphNumber();

    void setGlyphNumber(int no);

    int FontBB_X=1;
    int FontBB_Y=2;
    int FontBB_WIDTH=3;
    int FontBB_HEIGHT=4;

    /**draw the glyph*/
	void render(int text_fill_type, Graphics2D g2, float scaling, boolean isFormGlyph);

	/**
	 * return max possible glyph width in absolute units
	 */
	float getmaxWidth();

	/**
	 * used by type3 glyphs to set colour if required
	 */
	void setT3Colors(PdfPaint strokeColor, PdfPaint nonstrokeColor, boolean lockColours);

	/**
	 * see if we ignore colours for type 3 font
	 */
	boolean ignoreColors();

	Area getShape();

	void setWidth(float width);

    /**
     * retrun fontBounds paramter where type is a contant in PdfGlyh
     * @param type
     * @return
     */
    int getFontBB(int type);

    void setStrokedOnly(boolean b);

    boolean containsBrokenData();

    Path getPath();
}