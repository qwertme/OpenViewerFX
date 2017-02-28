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
 * ValueTypes.java
 * ---------------
 */
package org.jpedal.parser;

/**
 * Contains static flags we use for identifying all the object types in parser package
 */
public class ValueTypes {

    public static final int UNSET = 0;
    public static final int PATTERN = 1;
    public static final int FORM = 1;

    public static final int EmbeddedFonts=-1;
    public static final int StructuredContent=-2;
    public static final int StatusBar=-3;
    public static final int PdfLayerList=-4;
    public static final int MarkedContent=-5;
    public static final int ImageHandler=-6;
    public static final int DirectRendering=-7;
    public static final int ObjectStore=-8;
    public static final int Name=-9;
    public static final int PageNum=-10;
    public static final int GenerateGlyphOnRender=-11;
    //final public static int StreamType=-12;

    public static final int TextPrint=-15;
    
    public static final int PDFPageData=-18;
    public static final int PDFData=-19;
    public static final int PDFImages=-20;
    public static final int TextAreas=-21;
    public static final int TextDirections=22;
    public static final int DynamicVectorRenderer=23;

}
