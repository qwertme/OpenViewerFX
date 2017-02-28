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
 * PdfColor.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.*;

/**
 * template for all shading operations
 */
public class PdfColor extends Color implements PdfPaint,Paint{
	
	public PdfColor(final float r, final float g, final float b) {
		super(r, g, b);
	}
	
	public PdfColor(final int r, final int g, final int b) {
		super(r, g, b);
	}

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected boolean isPattern;
	
	@Override
    public void setScaling(final double cropX, final double cropH, final float scaling, final float textX, final float textY){
		//this.cropX=(int)cropX;
		//this.cropH=(int)cropH;
	}
	
	@Override
    public boolean isPattern(){
		return isPattern;
	}
	
	//constructor for pattern color
	@SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
    public void setPattern(final int dummy){
		isPattern=true;
	}

    @Override
    public void setRenderingType(final int createHtml) {
        //added for HTML conversion
    }
    
    @Override
    public boolean isTexture() {
        return false;
    }


}
