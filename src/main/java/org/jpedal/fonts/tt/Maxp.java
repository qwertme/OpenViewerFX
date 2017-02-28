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
 * Maxp.java
 * ---------------
 */
package org.jpedal.fonts.tt;

public class Maxp extends Table {
	
	private int numGlyphs, maxPoints, maxContours, maxTwilightPoints, maxStorage;

	public Maxp(final FontFile2 currentFontFile){
	
		//LogWriter.writeMethod("{readMapxTable}", 0);
        init(currentFontFile);

    }

    protected Maxp() {
    }

    private void init(final FontFile2 currentFontFile) {
        //move to start and check exists
        final int startPointer=currentFontFile.selectTable(FontFile2.MAXP);

        //read 'head' table
        if(startPointer!=0){

            currentFontFile.getNextUint32(); //id
            numGlyphs=currentFontFile.getNextUint16();
            maxPoints=currentFontFile.getNextUint16();
            maxContours=currentFontFile.getNextUint16();
            currentFontFile.getNextUint16(); //maxComponentPoints
            currentFontFile.getNextUint16(); //maxComponentContours
            currentFontFile.getNextUint16(); //maxZones
            maxTwilightPoints=currentFontFile.getNextUint16();
            maxStorage=currentFontFile.getNextUint16();
            currentFontFile.getNextUint16(); //maxFunctionDefs
            currentFontFile.getNextUint16(); //maxInstructionDefs
            currentFontFile.getNextUint16(); //maxStackElements
            currentFontFile.getNextUint16(); //maxSizeOfInstructions
            currentFontFile.getNextUint16(); //maxComponentElements
            currentFontFile.getNextUint16(); //maxComponentDepth
        }
    }

    public int getGlyphCount(){
		return numGlyphs;
	}

    public int getMaxPoints(){
        return maxPoints;
    }

    public int getMaxTwilightPoints(){
        return maxTwilightPoints;
    }

    public int getMaxStorage(){
        return maxStorage;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getMaxContours(){
        return maxContours;
    }
}
