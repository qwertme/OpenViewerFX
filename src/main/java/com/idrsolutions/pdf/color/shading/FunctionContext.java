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
 * FunctionContext.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;


import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;
import org.jpedal.parser.DecoderOptions;

public class FunctionContext implements PaintContext {
	
	final GenericColorSpace shadingColorSpace;
	
	private float scaling=1f;

	private final PDFFunction[] function;
	
	//private FunctionContext(){}
	
	private final int pageHeight;

	private final boolean colorsReversed;

	private int xstart,ystart;

	//private float[] domain;
	
	FunctionContext(final int pHeight, final float scaling, final GenericColorSpace shadingColorSpace, final boolean colorsReversed, final PDFFunction[] function){
		
		this.colorsReversed=colorsReversed;
		this.pageHeight=pHeight;
		//this.domain=domain;
		
		this.shadingColorSpace=shadingColorSpace;
		this.function = function;
		this.scaling=scaling;
		
		
		
	}
	@Override
    public void dispose() {}
	
	@Override
    public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }
	
	/**
	 * setup the raster with the colors
	 * */
	@Override
    public Raster getRaster(final int xstart, final int ystart, final int w, final int h) {
		
		this.xstart=xstart;
		this.ystart=ystart;
		
		//sets up the array of pixel values
		//WritableRaster raster =getColorModel().createCompatibleWritableRaster(w, h);
		
		//create buffer to hold all this data
		final int[] data = new int[w * h * 4];
		
		//workout color range
		Color c;

		//set current calues to default
		int cr,cg,cb;
		
		//y co-ordinates
		for (int y = 0; y < h; y++) {
			
			//x co-ordinates			
			for (int x = 0; x < w; x++) {
				
				c= calculateColor(x,y);
				
				/**
				 * workout values
				 */
				if(colorsReversed){
					cr=255-c.getRed();
					cg=255-c.getGreen();
					cb=255-c.getBlue();
				}else{
					cr=c.getRed();
					cg=c.getGreen();
					cb=c.getBlue();
				}
				
				//set color for the pixel with values
				final int base = (y * w + x) * 4;
				data[base] = cr;
				data[base + 1] = cg;
				data[base + 2] = cb;
				data[base + 3] = 255;//(int)(col.getAlpha());
				
			}
		}
		
		//set values
		final WritableRaster raster = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB).getRaster();
		raster.setPixels(0, 0, w, h, data);
		
		return raster;
	}
	
	/**workout rgb color*/
	private Color calculateColor(final float x, final float y) {
		
		final float cx;
        final float cy;

        /**
		 *take x and y and pass through conversion with domain values - this gives us xx
		 */
		//hack for MAC which is f**king broken
		if(DecoderOptions.isRunningOnMac){
			cx=scaling*(x+xstart);
			cy=scaling*(y+ystart);
		}else{
			cx=scaling*(x+xstart);
			cy=scaling*(pageHeight-(y+ystart));
		}
		
		final float[] values={cx,cy};

        final float[] colValues = ShadingFactory.applyFunctions(function,values);

        /**
         * this value is converted to a color
         */
		final int count=colValues.length;
		shadingColorSpace.setColor(colValues,count);

        return (Color) shadingColorSpace.getColor();
	}
}
