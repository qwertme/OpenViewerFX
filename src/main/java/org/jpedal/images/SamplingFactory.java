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
 * SamplingFactory.java
 * ---------------
 */
package org.jpedal.images;

public class SamplingFactory {


    /**do nothing*/
    public static int none;

    /** medium will make sure images larger than page are reduced in size (often large scanned tiffs)*/
    public static final int medium=1;

    /** (default setting) high will agressively reduce images bigger than twice their drawn size at 100% so a image
     * which appears as 100x100 on the PDF but whose raw images is 250x250 will be stored as 125x125 image
     * and not a 250x250 image
     */
    public static final int high=2;

    /** use down-sampling on printing (which is default) */
    public static final int print_enable=3;

    /** do not use down-sampling on printing*/
    public static final int print_disable=4;

    /**current setting - do not set directly*/
    public static int downsampleLevel=high;

    /**current setting - do not set directly*/
    public static boolean isPrintDownsampleEnabled=true;

    /**
     * PDFs contain images which may often be much larger than the actual space they occupy and they are reduced to fit the space<br>
     * alters image sampling to speed up program and reduce memory in with the strategies "high","medium", "none", "enable_printing", "disable_printing"
     *
     * @param newLevel A String value of image sampling strategies
     */
    public static void setDownsampleMode(String newLevel){

        if(newLevel==null) {
            newLevel = System.getProperty("org.jpedal.downsample");
        }

		if(newLevel!=null){
			if(newLevel.equals("high")) {
                downsampleLevel = high;
            } else if(newLevel.equals("medium")) {
                downsampleLevel = medium;
            } else if(newLevel.equals("none")) {
                downsampleLevel = none;
            } else if(newLevel.equals("print_disable")) {
                isPrintDownsampleEnabled = false;
            } else if(newLevel.equals("print_enable")) {
                isPrintDownsampleEnabled = true;
            }
        }

	}

    /**
     * PDFs contain images which may often be much larger than the actual space they occupy and they are reduced to fit the space<br>
     * alters image sampling to speed up program and reduce memory with the strategies high, medium, none, enable_printing, disable_printing
     *
     * @param newLevel A String value of image sampling strategies
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void setDownsampleMode(final int newLevel){

        if(newLevel==high || newLevel==medium || newLevel==none) {
            downsampleLevel = newLevel;
        } else if(newLevel==print_disable) {
            isPrintDownsampleEnabled = false;
        } else if(newLevel==print_enable) {
            isPrintDownsampleEnabled = true;
        }

    }
}
