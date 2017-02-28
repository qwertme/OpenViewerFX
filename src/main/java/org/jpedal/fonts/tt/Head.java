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
 * Head.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import org.jpedal.utils.LogWriter;


public class Head extends Table {
	
	/**format used*/
    protected int glyphDataFormat; //default for otf

    protected int indexToLocFormat;
	
	/**bounds on font*/
    protected float[] FontBBox =new float[4];

	protected int flags; //default for otf

	protected int unitsPerEm=1024;  //default for otf

	public Head(final FontFile2 currentFontFile){
	
		//move to start and check exists
		final int startPointer=currentFontFile.selectTable(FontFile2.HEAD);
		
		//read 'head' table
		if(startPointer==0){
			if(LogWriter.isOutput()) {
                LogWriter.writeLog("No head table found");
            }
		}else{
			
			currentFontFile.getNextUint32(); //id
			
			//ignore values
			for(int i=0;i<3;i++) {
                currentFontFile.getNextUint32();
            }
			
			flags=currentFontFile.getNextUint16();
			unitsPerEm=currentFontFile.getNextUint16();
			
			//ignore dates
			for(int i=0;i<2;i++) {
                currentFontFile.getNextUint64();
            }
			
			//bounds
			for(int i=0;i<4;i++) {
                FontBBox[i] = currentFontFile.getNextSignedInt16();
            }

			//ignore more flags
			for(int i=0;i<3;i++) {
                currentFontFile.getNextUint16();
            }

            indexToLocFormat=currentFontFile.getNextUint16();

			glyphDataFormat =currentFontFile.getNextUint16();
			
		}
	}

    public Head() {
    }


    public int getIndexToLocFormat() {
        return indexToLocFormat;
    }

    public float[] getFontBBox(){
		return this.FontBBox;
	}

    /**
	 *  Returns the unitsPerEm.
	 */
	public int getUnitsPerEm() {
		return unitsPerEm;
	}

}
