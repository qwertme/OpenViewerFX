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
 * Info.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 */
public class Info {
    
    public static final int CS_CMYK = 12;
    public static final int CS_LAB = 14;
    public static final int CS_SRGB = 16;
    public static final int CS_GRAYSCALE = 17;
    public static final int CS_SYCC = 18;
    public static final int CS_ESRGB = 20;
    public static final int CS_ROMMRGB = 21;
    public static final int CS_ESYCC = 24;
    
    //main info
    public int imageWidth;
    public int imageHeight;
    public int nComp;
    public int bitDepth;
    public int compressionType = 7;
    public int unknownColorSpace;
    public int ip;
    public byte[] bitDepths; 
    
    //colorspace info
    public int enumerateCS;
    
    public final List<Integer> contiguousCodeStreamBoxes = new ArrayList<Integer>();
    public final List<Integer> tileOffsets = new ArrayList<Integer>();
    
    public Tile tileProcess;
      
    //special info
    public SIZ siz;
    public COD cod;
    public QCD qcd;
    public QCD[] qcc;
    public Palette palette;
    public Cmap cmap;
    
    public final HashMap<Integer,Tile> tilesMap = new HashMap<Integer,Tile>();
    
    public final HashMap<Integer,Integer> cDef = new HashMap<Integer,Integer>();
    
}
