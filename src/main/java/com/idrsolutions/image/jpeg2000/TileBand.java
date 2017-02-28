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
 * TileBand.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class TileBand {
    
    public static final byte LL = 0;
    public static final byte LH = 1;
    public static final byte HL = 2;
    public static final byte HH = 3;
    
    public int x0;
    public int y0;
    public int x1;
    public int y1;
    
    public final byte type;
    public CodeBlockInfo codeBlockInfo;
    
    final List<CodeBlock> codeBlocks = new ArrayList<CodeBlock>();
    final List<Precinct> precincts = new ArrayList<Precinct>();
    
    
    public TileBand(byte type){
        this.type = type;
    }
    
    public int getMultiplier(){
        switch(type){
            case LL:
                return 0;
            case LH:
                return 1;
            case HL:
                return 1;
            case HH:
                return 2;
            default:
                return 0;
        }
    }
    
}
