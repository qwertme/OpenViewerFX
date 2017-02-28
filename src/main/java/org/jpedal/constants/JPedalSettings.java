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
 * JPedalSettings.java
 * ---------------
 */
package org.jpedal.constants;

/**
 * used by JPedal for contants which allow user to set flags<br>
 * This is achived with a PdfDecoder method modifyJPedalParameters(Map values).<br>

 * How does it work?<br>
 *
 * The example code below gives examples of how to use this function and a full list of valid settings.
 * It should be called before any files are opened.<br>

 * Map mapValues=new HashMap();<br>
 * mapValues.put(JPedalSettings.DISPLAY_INVISIBLE_TEXT,Boolean.TRUE);<br>
 * mapValues.put(JPedalSettings.TEXT_HIGHLIGHT_COLOUR, Color.GREEN); <br>
 * mapValues.put(JPedalSettings.CACHE_LARGE_FONTS,new Integer(65536));<br>
 * //exception will be thrown if invalid value passed<br>
 * PdfDecoder.modifyJPedalParameters(mapValues);  <br>
 */

public class JPedalSettings {
    
	/**allow user to set colour used to highlight text found in JPanel*/
	public static final Integer TEXT_HIGHLIGHT_COLOUR = 1;
    
	/**allow user to display invisible text in current fill colour*/
	public static final Integer DISPLAY_INVISIBLE_TEXT = 2;

    /**allow user to cache large fonts to avoid big memory hit*/
    public static final Integer CACHE_LARGE_FONTS= 3;

    /**allow user print all fonts as textprinting*/
    public static final Integer TEXT_PRINT_NON_EMBEDDED_FONTS= 4;

    /**allow user to define color for text when highlighted*/
    public static final Integer TEXT_INVERTED_COLOUR = 5;
    
    /**allow user to define custom upscaling val to improve quality of extr images*/
    //public static final Integer IMAGE_UPSCALE =new Integer(6);
    
    /**allow user to set a flag to use hi res settings*/
    public static final Integer IMAGE_HIRES = 7;
    
    /**allow user to extract best quality images at the cost of memory */
    //public static final Integer EXTRACT_AT_BEST_QUALITY = new Integer(8);

    /**allow user to limit the amount of scaling on Best quality*/
    public static final Integer EXTRACT_AT_BEST_QUALITY_MAXSCALING = 9;

    /**allow user to limit the amount of scaling on Best quality - needs PAGE_SIZE_OVERRIDES_IMAGE set to true*/
    public static final Integer EXTRACT_AT_PAGE_SIZE = 10;

    /**allow user to choose which takes priority*/
    public static final Integer PAGE_SIZE_OVERRIDES_IMAGE = 11;

    /**allow user to set highlight to invert colours on area of page*/
    public static final Integer INVERT_HIGHLIGHT = 12;
    
    /**allow user to set highlight mode*/
    //public static final Integer TEXT_HIGHLIGHT_MODE = new Integer(13);

    /**allow user to stop some forms being printed*/
    public static final Integer IGNORE_FORMS_ON_PRINT = 14;
    
    /**allow user to generate pages smaller than page size using hi res*/
    public static final Integer ALLOW_PAGES_SMALLER_THAN_PAGE_SIZE = 15;
    
    /**Integer for color to use for background*/
    public static final Integer PAGE_COLOR = 16;
    
    /**Integer for color of non rendered page background*/
    public static final Integer UNDRAWN_PAGE_COLOR = 17;

    /**Integer for color to use for text*/
    public static final Integer TEXT_COLOR = 18;

    /**Integer to flag text value should be changed*/
    public static final Integer REPLACE_TEXT_COLOR = 19;

    /**Integer to flag alt text color should include line art*/
    public static final Integer CHANGE_LINEART = 20;
    
    /**Integer for color to use for text*/
    public static final Integer DISPLAY_BACKGROUND = 21;

    /**Integer to flag text value should be changed*/
    public static final Integer REPLACE_DISPLAY_BACKGROUND = 22;

    /**Integer to flag text value should be changed*/
    public static final Integer REPLACEMENT_COLOR_THRESHOLD = 22;
    
    /**
     * Values for use with TEXT_HIGHLIGHT_MODE
     */
    
    /**Possible value for TEXT_HIGHLIGHT_MODE use legacy highlighting mode*/
    public static final Integer LEGACY_HIGHLIGHTING = 140;

    /**Possible value for TEXT_HIGHLIGHT_MODE use legacy highlighting mode*/
    public static final Integer TEXT_BASED_HIGHLIGHTING = 150;
    

}
