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
 * T3Display.java
 * ---------------
 */
package org.jpedal.render;

import org.jpedal.color.PdfColor;
import org.jpedal.color.PdfPaint;
import org.jpedal.io.ObjectStore;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;
import org.jpedal.objects.GraphicsState;

public class T3Display extends SwingDisplay implements T3Renderer{

    /**create instance and set flag to show if we draw white background*/
    public T3Display(final int pageNumber, final boolean addBackground, final int defaultSize, final ObjectStore newObjectRef) {

        this.rawPageNumber =pageNumber;
        this.objectStoreRef = newObjectRef;
        this.addBackground=addBackground;

        setupArrays(defaultSize);
    }

    public T3Display(final byte[] dvr, final Map map) {
        super(dvr,map);
    }

    /**
     * use by type3 fonts to differentiate images in local store
     */
    @Override
    public void setType3Glyph(final String pKey) {
        this.rawKey=pKey;

        isType3Font=true;

    }

        /**
     * used by type 3 glyphs to set colour
     */
    @Override
    public void lockColors(final PdfPaint strokePaint, final PdfPaint nonstrokePaint, final boolean lockColour) {

        colorsLocked=lockColour;
        Color strokeColor=Color.white,nonstrokeColor=Color.white;

        if(strokePaint!=null && !strokePaint.isPattern()) {
            strokeColor = (Color) strokePaint;
        }
        strokeCol=new PdfColor(strokeColor.getRed(),strokeColor.getGreen(),strokeColor.getBlue());

        if(!nonstrokePaint.isPattern()) {
            nonstrokeColor = (Color) nonstrokePaint;
        }
        fillCol=new PdfColor (nonstrokeColor.getRed(),nonstrokeColor.getGreen(),nonstrokeColor.getBlue());

    }
    
     @Override
     void renderImage(final AffineTransform imageAf, BufferedImage image, final float alpha,
            final GraphicsState currentGraphicsState, final float x, final float y) {

        
        /**
         * color type3 glyphs if not black
         */
        if (image !=null && fillCol != null) {

            image = T3ImageUtils.handleType3Image(image, fillCol);

        }
        
        super.renderImage(imageAf, image, alpha, currentGraphicsState,  x, y);
     }
}
