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
 * ShearedTexturePaint.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 *
 */
public class ShearedTexturePaint extends TexturePaint implements PdfPaint{

    private final AffineTransform callerTransform;
    public ShearedTexturePaint(BufferedImage txtr, Rectangle2D anchor, AffineTransform callerTransform) {
        super(txtr, anchor);
        this.callerTransform = callerTransform;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
        AffineTransform subForm =(AffineTransform) xform.clone();
        subForm.concatenate(callerTransform);
        return super.createContext(cm, deviceBounds, userBounds, subForm, hints); 
    }
    
    @Override
    public void setScaling(final double cropX, final double cropH, final float scaling, final float textX, final float textY) {
    }

    @Override
    public boolean isPattern() {
        return false;
    }

    @SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
    public void setPattern(final int dummy) {
    }

    @Override
    public int getRGB() {
        return 0;
    }

    @Override
    public void setRenderingType(final int createHtml) {
        //added for HTML conversion
    }
    
    @Override
    public boolean isTexture() {
        return true;
    }

    
}
