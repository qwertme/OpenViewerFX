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
 * ImageDisplay.java
 * ---------------
 */
package org.jpedal.render;


import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import org.jpedal.color.PdfPaint;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.Cmd;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

public class ImageDisplay extends BaseDisplay implements DynamicVectorRenderer {

    public ImageDisplay(final int pageNumber, final boolean addBackground, final int defaultSize, final ObjectStore newObjectRef) {

        type = DynamicVectorRenderer.DISPLAY_IMAGE;

        this.rawPageNumber = pageNumber;
        this.objectStoreRef = newObjectRef;
        this.addBackground = addBackground;

        //setupArrays(defaultSize);
        areas = new Vector_Rectangle_Int(defaultSize);
    }

    // save image in array to draw
    @Override
    public final int drawImage(final int pageNumber, final BufferedImage image, final GraphicsState gs, final boolean alreadyCached, final String name, final int previousUse) {

        //track objects
        int iw = (int) gs.CTM[0][0];
        if (iw < 0) {
            iw = -iw;
        } else if (iw == 0) {
            iw = (int) gs.CTM[0][1];

            if (iw < 0) {
                iw = -iw;
            }
        }

        int ih = (int) gs.CTM[1][1];
        if (ih < 0) {
            ih = -ih;
        } else if (ih == 0) {
            ih = (int) gs.CTM[1][0];

            if (ih < 0) {
                ih = -ih;
            }
        }

        final int[] rectParams = {(int) gs.CTM[2][0], (int) gs.CTM[2][1], iw, ih};
        areas.addElement(rectParams);

        blendMode=gs.getBMValue();

        renderImage(null, image, gs.getAlpha(GraphicsState.FILL), gs, gs.x, gs.y);

        return -1;

    }


    /*save clip in array to draw*/
    @Override
    public void drawClip(final GraphicsState currentGraphicsState, final Shape defaultClip, final boolean canBeCached) {

        final Area clip=currentGraphicsState.getClippingShape();

        if(canBeCached && hasClips && lastClip==null&& clip==null){

        }else if (!canBeCached || lastClip==null || clip==null || !clip.equals(lastClip)){

            RenderUtils.renderClip(currentGraphicsState.getClippingShape(), null, defaultClip, g2);

            lastClip=clip;

            hasClips=true;

        }
    }

    @Override
    public void drawEmbeddedText(final float[][] Trm, final int fontSize, final PdfGlyph embeddedGlyph,
                                 final Object javaGlyph, final int type, final GraphicsState gs, final double[] textTransform, final String glyf, final PdfFont currentFontData, final float glyfWidth) {

        blendMode=gs.getBMValue();

        AffineTransform at = null;
        if(textTransform!=null) //can actually be null at line 199 
        {
            at = new AffineTransform(textTransform);
        }

        if (type == TEXT) {

            PdfPaint currentCol = null, fillCol = null;
            final int text_fill_type = gs.getTextRenderType();

            //for a fill
            if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL) {
                fillCol = gs.getNonstrokeColor();
            }

            //and/or do a stroke
            if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE) {
                currentCol = gs.getStrokeColor();
            }

            //set the stroke to current value
            final Stroke newStroke = gs.getStroke();
            g2.setStroke(newStroke);

            final AffineTransform def = g2.getTransform();

            g2.translate(Trm[2][0], Trm[2][1]);

            if(at!=null) {
                g2.transform(at);
            }

            renderText(Trm[2][0], Trm[2][1], text_fill_type, (Area) javaGlyph, null, currentCol, fillCol, gs.getAlpha(GraphicsState.STROKE),
                    gs.getAlpha(GraphicsState.FILL));

            g2.setTransform(def);
        } else {

            PdfPaint strokeCol = null, fillCol = null;
            final int text_fill_type = gs.getTextRenderType();

            //for a fill
            if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL) {
                fillCol = gs.getNonstrokeColor();
            }

            //and/or do a stroke
            if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE) {
                strokeCol = gs.getStrokeColor();
            }

            //set the stroke to current value
            final Stroke newStroke = gs.getStroke();
            final Stroke currentStroke = g2.getStroke();

            //avoid if stroke/fill
            if (text_fill_type == GraphicsState.STROKE) {
                g2.setStroke(newStroke);
            }

            //track objects so we can work out if anything behind
            int fontSize2 = (int) gs.CTM[1][1];
            if (fontSize2 < 0) {
                fontSize2 = -fontSize2;
            }

            if (fontSize2 == 0) {
                fontSize2 = (int) gs.CTM[0][1];
            }
            if (fontSize2 < 0) {
                fontSize2 = -fontSize2;
            }

            //  System.out.println(">>"+CTM[2][0]+" "+CTM[2][1]+" "+CTM[0][0]+" "+" "+CTM[0][1]+" "+" "+CTM[1][0]+" "+" "+CTM[1][1]+" "+this);
            final int[] rectParams = {(int) gs.CTM[2][0], (int) gs.CTM[2][1], fontSize2, fontSize2};
            areas.addElement(rectParams);

            renderEmbeddedText(text_fill_type, embeddedGlyph, type, at, null, strokeCol, fillCol,
                    gs.getAlpha(GraphicsState.STROKE), gs.getAlpha(GraphicsState.FILL), (int) gs.getLineWidth());

            g2.setStroke(currentStroke);

        }
    }

    /*save shape in array to draw*/
    @Override
    public final void drawShape(final Shape currentShape, final GraphicsState gs, final int cmd) {


        if((cmd==Cmd.F || cmd==Cmd.B) && gs.CTM[0][1]==0 && gs.CTM[1][0]==0){
            final int x = currentShape.getBounds().x;
            final int y = currentShape.getBounds().y;
            final int w = currentShape.getBounds().width;
            final int h = currentShape.getBounds().height;
            final int[] rectParams = {x,y,w,h};
            areas.addElement(rectParams);
        }else{
            areas.addElement(null);
        }

        blendMode=gs.getBMValue();

        renderShape(null, gs.getFillType(), gs.getStrokeColor(), gs.getNonstrokeColor(), gs.getStroke(),
                currentShape, gs.getAlpha(GraphicsState.STROKE), gs.getAlpha(GraphicsState.FILL));
    }

    @Override
    public void drawShape(final Object currentShape, final GraphicsState currentGraphicsState, final int cmd){
        // Stub method to supress BaseDisplay message
    }

}
