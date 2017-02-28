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
 * PixelFactory.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

public class PixelFactory {

	//return both pdfX at [0] & pdfY at [1]
	public static float[] convertPhysicalToPDF(final boolean isPrinting, final float x, final float y, final float offX, final float offY, final float scaling, final int xstart, final int ystart, final int minX, final int pageHeight) {

        final float pdfX;
        final float pdfY;

        if(isPrinting){
        	
            pdfX=((x+xstart)-offX)*scaling;
            pdfY=((y+ystart)-offY)*scaling;
            
        }else{
        	pdfX=(scaling*(x+xstart+minX-offX));
            pdfY=(scaling*((pageHeight-(y+ystart-offY))));
            
        }
		return new float[] {pdfX, pdfY};
	}
}
