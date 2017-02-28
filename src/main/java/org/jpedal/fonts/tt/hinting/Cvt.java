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
 * Cvt.java
 * ---------------
 */
package org.jpedal.fonts.tt.hinting;

import org.jpedal.fonts.tt.Table;
import org.jpedal.fonts.tt.FontFile2;


public class Cvt extends Table {
	
	final short[] unscaledCvt;

	final int[] cvt;
    double scale;

    /**debug message on first time we show message Cvt.get(): Key out of range. */
    private static boolean messageDisplayed;

    Cvt(final FontFile2 currentFontFile){
	
		//LogWriter.writeMethod("{readCvtTable}", 0);
		
		//move to start and check exists
		final int startPointer=currentFontFile.selectTable(FontFile2.CVT);
	
		//read table
		if(startPointer!=0){
			final int len = currentFontFile.getOffset(FontFile2.CVT)/2;
			unscaledCvt = new short[len];
			cvt = new int[len];
			for (int i = 0; i < len; i++) {
                unscaledCvt[i] = currentFontFile.getFWord();
            }
			
		} else {
            unscaledCvt = new short[]{};
            cvt = new int[]{};
        }
	}


    /**
     * Scales the CVT to match the font
     * @param scale The new scale
     */
    public void scale(final double scale) {
        this.scale = scale;
        for (int i=0; i<unscaledCvt.length; i++) {
            cvt[i] = (int)((scale*unscaledCvt[i])+0.5);
        }
    }

    /**
     * Put a new value in the table in pixels (already scaled)
     * @param key Which value to replace
     * @param value The (scaled) value
     */
    public void putInPixels(final int key, final int value) {
        if (key >= 0 && key < cvt.length) {
            cvt[key] = value;
        }else {
            //
        }

    }

    /**
     * Put a new value in the table in FUnits (needs scaling)
     * @param key Which value to replace
     * @param value The (unscaled) value
     */
    public void putInFUnits(final int key, int value) {
        value = (int)((value*scale)+0.5);
        if (key >= 0 && key < cvt.length) {
            cvt[key] = value;
        }else {
            //
        }

    }

    /**
     * Get a value
     * @param key The value to get
     * @return The (scaled) value
     */
    public int get(final int key) {
        if (key >= 0 && key < cvt.length) {
            return cvt[key];
        }else if (!messageDisplayed){ //only for first case
            //
            messageDisplayed=true;
        }

        return 0;
    }

    //<start-demo><end-demo>
}
