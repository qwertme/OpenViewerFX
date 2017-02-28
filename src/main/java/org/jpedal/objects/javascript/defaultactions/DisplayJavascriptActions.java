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
 * DisplayJavascriptActions.java
 * ---------------
 */

package org.jpedal.objects.javascript.defaultactions;


import java.util.StringTokenizer;
/**JS
 * whole class is for javascript static options at present, may be added to
 * the FormObject accesses it to add the values changed to the form field
 */
public class DisplayJavascriptActions {

//	Field is visible on screen and in print
public static final int visible = 0;
	
//	Field is hidden on screen and in print
public static final int hidden = 1;
	
//	Field is visible on screen but does not print
public static final int noPrint = 2;
	
//	Field is hidden on screen but prints
public static final int noView = 3;
	
//	Field is shown on screen and in print
@SuppressWarnings("UnusedDeclaration")
public static final int notHidden = 4;
	
	
	//public Object transparent = "\"T\"";
	//NOTE needs proper implement for javascript and rhino
//	public final static Object transparent = new Color(0,0,0,0);//[ "T" ]
//	public final static Object black = Color.black;//[ "G", 0 ]
//	public final static Object white = Color.white;//[ "G", 1 ]
//	public final static Object red = Color.red;//[ "RGB", 1,0,0 ]
//	public final static Object green = Color.green;//[ "RGB", 0,1,0 ]
//	public final static Object blue = Color.blue;//[ "RGB", 0, 0, 1 ]
//	public final static Object cyan = Color.cyan;//[ "CMYK", 1,0,0,0 ]
//	public final static Object magenta = Color.magenta;//[ "CMYK", 0,1 0,0 ]
//	public final static Object yellow = Color.yellow;//[ "CMYK", 0,0,1,0 ]
//	public final static Object dkGray = Color.darkGray;//[ "G", 0.25 ]
//	public final static Object gray = Color.gray;//[ "G", 0.5 ]
//	public final static Object ltGray = Color.lightGray;//[ "G", 0.75 ]
	
	public static final float[] transparent = new float[0];
	public static final float[] black = {0};
	public static final float[] white = {1};
	public static final float[] red = {1,0,0};
	public static final float[] green = {0,1,0};
	public static final float[] blue = {0,0,1};
	public static final float[] cyan = {1,0,0,0};
	public static final float[] magenta = {0,1,0,0};
	public static final float[] yellow = {0,0,1,0};
	public static final float[] dkGray = {0.25f};
	public static final float[] gray = {0.5f};
	public static final float[] ltGray = {0.75f};
	
	// <start-demo><end-demo>
	
	public static float[] convertToColorFloatArray(final String newColor) {
		
		final StringTokenizer tok = new StringTokenizer(newColor,"[,]\"");
		final float[] color = new float[tok.countTokens()-1];
		for(int i=0;i<color.length;i++){
			color[i] = Float.parseFloat(tok.nextToken());
		}
		return color;
	}
}
