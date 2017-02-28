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
 * DecodeStatus.java
 * ---------------
 */
package org.jpedal.parser;

/*
 * flags which can return values after page decode
 */
public class DecodeStatus {

    /**indicate if last decodePage() call had any issues*/
    public static final int PageDecodingSuccessful=1;

    public static final int ImagesProcessed=2;

    public static final int NonEmbeddedCIDFonts=4;

    public static final int YCCKImages=8;

    /**whether any fonts will almost certainly need hinting turned on*/
    public static final int TTHintingRequired=32;
    
    public static final int TooManyShapes=64;
}
