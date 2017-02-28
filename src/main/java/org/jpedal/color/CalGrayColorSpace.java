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
 * CalGrayColorSpace.java
 * ---------------
 */
package org.jpedal.color;

/**
 * handle Java version of CalGrayColorSpace in PDF
 */
public class CalGrayColorSpace extends GenericColorSpace {

	private static final long serialVersionUID = -6459433440483127497L;

	public CalGrayColorSpace(final float[] whitepoint, final float[] gamma) {

		componentCount=1;

		setCIEValues(whitepoint, null,null,gamma);
		setType(ColorSpaces.CalGray);

	}

	/**
	 * set CalGray color (in terms of rgb)
	 */
    @Override
    public final void setColor(final String[] number_values, final int opCount) {

		final float[] colValues=new float[1];
		colValues[0] = Float.parseFloat(number_values[0]);

		setColor(colValues,1);
	}

	/**
	 * set CalGray color (in terms of rgb)
	 */
    @Override
    public final void setColor(final float[] number_values, final int opCount) {

		final float A = number_values[0];

		//standard calculate (see pdf spec 1.3 page 170)
		float[] values = new float[3];
		final float AG = (float) Math.pow(A, G[0]);

		values[0] = ((W[0]) * AG);
		values[1] = ((W[1]) * AG);
		values[2] = (W[2] * AG);

		//convert to rgb
		values = cs.toRGB(values);

		//set color
		this.currentColor= new PdfColor(values[0],values[1],values[2]);

	}
}
