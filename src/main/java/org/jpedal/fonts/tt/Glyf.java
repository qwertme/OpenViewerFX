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
 * Glyf.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import java.util.HashMap;
import java.util.Map;


public class Glyf extends Table {

    /**holds mappings for drawing the glyphs*/
    private final int[] charStrings;

    private final int glyfCount;

    /**holds list of empty glyphs*/
    private final boolean[] emptyCharStrings;
    private byte[] glyphTable;

    Glyf(final FontFile2 currentFontFile, final int glyphCount, final int[] glyphIndexStart){

        //save so we can access
        this.glyfCount=glyphCount;
        
        charStrings=new int[glyphCount];
        emptyCharStrings=new boolean[glyphCount];

        //move to start and check exists
        final int startPointer=currentFontFile.selectTable(FontFile2.LOCA);

        //read  table
        if(startPointer!=0){

            //read each gyf
            for(int i=0;i<glyphCount;i++){

                //just store in lookup table or flag as zero length
                if((glyphIndexStart[i]==glyphIndexStart[i+1])){
                    charStrings[i]=-1;
                    emptyCharStrings[i]=true;
                }else{
                    charStrings[i]=glyphIndexStart[i];
                }
            }

            //read the actual glyph data
            glyphTable=currentFontFile.getTableBytes(FontFile2.GLYF);

        }
    }

    public int getCharString(final int glyph){

        final int value;
        
        if(glyph<0 || glyph>=glyfCount) {
            value = glyph;
        } else {
            value = charStrings[glyph];
        }
        
        return value;
    }

    public byte[] getTableData() {
        return glyphTable;
    }

    public int getGlypfCount() {
        return glyfCount;
    }

    /**assume identify and build data needed for our OTF converter*/
    public Map buildCharStringTable() {

        final Map returnStrings=new HashMap();

        for(int key=0;key<glyfCount;key++)
        {
            if (!emptyCharStrings[key]) {
                returnStrings.put(key, key);
            }
        }
        return returnStrings;
    }
}
