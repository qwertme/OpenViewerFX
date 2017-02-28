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
 * BaseDecoder.java
 * ---------------
 */

package org.jpedal.parser;

import org.jpedal.PdfDecoderInt;
import org.jpedal.objects.GraphicsState;

import org.jpedal.parser.gs.GraphicsStates;
import org.jpedal.render.DynamicVectorRenderer;


public class BaseDecoder {

    public boolean isHTML;
    
//    public static final int TextPrint=20;
    public static final int GenerateGlyphOnRender=21;

    protected ParserOptions parserOptions=new ParserOptions();

    final GraphicsStates graphicsStates=new GraphicsStates(parserOptions);

    protected DynamicVectorRenderer current;

    protected GraphicsState gs=new GraphicsState();

    protected float multiplyer = 1;

    protected int streamType;

    public void setParams(final ParserOptions parserOptions){
        this.parserOptions=parserOptions;
    }

    /**custom up scale value for JPedal settings*/
    public void setMultiplyer(final float value) {

        multiplyer=value;

    }

    public void setStreamType(final int value) {

        streamType=value;
        
    }

    public void setGS(final GraphicsState gs) {
        this.gs=gs;
    }

    public void setRenderer(final DynamicVectorRenderer current) {
        this.current =current;
        isHTML=current.isHTMLorSVG();
            
        //flag OCR used
        final boolean isOCR=(parserOptions.getRenderMode() & PdfDecoderInt.OCR_PDF)==PdfDecoderInt.OCR_PDF;
        if(isOCR && current!=null) {
            current.setOCR(true);
        }
    }
}
