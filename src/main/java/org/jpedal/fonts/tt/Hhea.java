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
 * Hhea.java
 * ---------------
 */
package org.jpedal.fonts.tt;


public class Hhea extends Table {
		
	public static final int VERSION = 0;
	public static final int ASCENDER = 1;
	public static final int DESCENDER = 2;
	public static final int LINEGAP = 3;
	public static final int ADVANCEWIDTHMAX = 4;
	public static final int MINIMUMLEFTSIDEBEARING = 5;
	public static final int MINIMUMRIGHTSIDEBEARING = 6;
	public static final int XMAXEXTENT = 7;
	public static final int CARETSLOPERISING = 8;
	public static final int CARETSLOPERUN = 9;
	public static final int CARETOFFSET = 10;
	public static final int METRICDATAFORMAT = 11;
	public static final int NUMBEROFMETRICS = 12;
	
	private int version = 65536;
	private int ascender = 1;
	private int descender = -1;
	private int lineGap;
	private int advancedWidthMax;
	private int minimumLeftSideBearing;
	private int minimumRightSideBearing;
	private int xMaxExtent;
	private int caretSlopeRise;
	private int caretSlopeRun;
	private int caretOffset;
	private int metricDataFormat;
	private int numberOfHMetrics;
	
	public Hhea(final FontFile2 currentFontFile){
	
		//move to start and check exists
		final int startPointer=currentFontFile.selectTable(FontFile2.HHEA);
		
		//read 'head' table
		if(startPointer!=0){
			
            version = currentFontFile.getNextUint32(); //version 65536
            ascender = currentFontFile.getFWord();//ascender  1972
            descender = currentFontFile.getFWord();//descender -483
            lineGap = currentFontFile.getFWord();//lineGap 0
            advancedWidthMax = currentFontFile.readUFWord();//advanceWidthMax  2513
            minimumLeftSideBearing = currentFontFile.getFWord();//minLeftSideBearing  -342
            minimumRightSideBearing = currentFontFile.getFWord();//minRightSideBearing  -340
            xMaxExtent = currentFontFile.getFWord();//xMaxExtent      2454
            caretSlopeRise = currentFontFile.getNextInt16();//caretSlopeRise 1
            caretSlopeRun = currentFontFile.getNextInt16();//caretSlopeRun   0
            caretOffset = currentFontFile.getFWord();//caretOffset          0

            //reserved values
            for( int i = 0; i < 4; i++ ) {
                currentFontFile.getNextUint16(); //0
            }

            metricDataFormat = currentFontFile.getNextInt16();//metricDataFormat
            numberOfHMetrics = currentFontFile.getNextUint16(); //261

		}
	}

    public Hhea() {
    }

    public int getNumberOfHMetrics(){
		return numberOfHMetrics;
	}
    
    /**
     * returns the value of given int number of variable
     * @param key is the key value
     * @return value of the variable     
     */
    public int getIntValue(final int key){
    	switch(key){
    	case VERSION : return version;
    	case ASCENDER: return ascender;
    	case DESCENDER : return descender;
    	case LINEGAP : return lineGap;
    	case ADVANCEWIDTHMAX : return advancedWidthMax ;
    	case MINIMUMLEFTSIDEBEARING : return minimumLeftSideBearing ;
    	case MINIMUMRIGHTSIDEBEARING : return minimumRightSideBearing;
    	case XMAXEXTENT : return xMaxExtent;
    	case CARETSLOPERISING : return caretSlopeRise;
    	case CARETSLOPERUN : return caretSlopeRun;
    	case CARETOFFSET : return caretOffset;
    	case METRICDATAFORMAT : return metricDataFormat;
    	case NUMBEROFMETRICS : return numberOfHMetrics;
    	default : return 0;
    	}
    }
	
}
