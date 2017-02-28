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
 * PDFGenericFunction.java
 * ---------------
 */

package org.jpedal.function;

import java.io.Serializable;

/**
 * Contains code which are used by multiple Shading Classes
 */
public class PDFGenericFunction implements Serializable {

	/**
	 * values found in Function using names used in PDF reference
	 */
	protected final float[] domain;
    protected float[] encode;
    protected float[] decode;
    protected final float[] range;
	
	
	public PDFGenericFunction(final float[] domain, final float[] range) {
		
		this.range=range;
		this.domain=domain;
		
	}

	/**
	 * Preform interpolation on the given values.
	 * @param x : current X value
	 * @param xmin : lowest x value
	 * @param xmax : highest x value
	 * @param ymin : lowest y value
	 * @param ymax : highest y value
	 * @return y value for the x value
	 */
	static float interpolate(final float x, final float xmin, final float xmax, final float ymin, final float ymax){
		
		return ((x-xmin)*(ymax-ymin)/(xmax-xmin))+ymin;
		
	}

	/**
	 * Return the lowest of the input vairbles
	 * @param a : value 1 to check
	 * @param b : value 2 to check
	 * @return The lowest of the input values
	 */
	static float min(final float a, final float b) {

		if(a>b) {
            return b;
        } else {
            return a;
        }

	}
	
	/**
	 * Return the highest of the input vairbles
	 * @param a : value 1 to check
	 * @param b : value 2 to check
	 * @return The highest of the input values
	 */
	static float max(final float a, final float b) {

		if(a<b) {
            return b;
        } else {
            return a;
        }

	}
}
