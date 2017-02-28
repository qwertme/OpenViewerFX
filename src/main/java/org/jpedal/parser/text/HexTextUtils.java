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
 * HexTextUtils.java
 * ---------------
 */

package org.jpedal.parser.text;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.T1GlyphFactory;
import org.jpedal.parser.ParserOptions;

/**
 *
 * @author markee
 */
class HexTextUtils {
   
    static int getHexValue(final byte[] stream, int i, GlyphData glyphData, PdfFont currentFontData, ParserOptions parserOptions ) {
        //'<'=60
        
        int chars=0,nextInt;
        
        int charSize=glyphData.getCharSize();
        //get number of chars
        for (int i2 = 1; i2 < charSize; i2++) {
            nextInt = stream[i + i2];
            
            if(nextInt==62){ //allow for less than 4 chars at end of stream (ie 6c>)
                i2=4;
                charSize=2;
                glyphData.setCharSize(2);
            }else if(nextInt==10 || nextInt==13){ //avoid any returns
                i++;
                i2--;
            }else{
                chars++;
            }
        }
        
        return setValue(glyphData, getValue(chars, stream, i), i, charSize, currentFontData, parserOptions);
    }

    static int getHexCIDValue(final byte[] stream, int i, GlyphData glyphData, PdfFont currentFontData, ParserOptions parserOptions ) {
        
        //'<'=60
        
        int charSize=2;
        
        //single value
        int val = getValue(1, stream, i);
        
        //System.out.println("getHexCIDValue val="+val);
        setValue(glyphData, val, i, charSize, currentFontData, parserOptions);
         
         
      //  int firstVal=val;
        
        //lazy init if needed
        if(StandardFonts.CMAP==null){
            StandardFonts.readCMAP();
        }
        
       // String firstValue = StandardFonts.CMAP[val];
       
        /**
         * read second byte if needed (we always read first time to see if double byte or single)
         */
       // final boolean isEmbedded =currentFontData.isFontEmbedded;
        
        //also check if mapped in Charstring
        final boolean hasCharString=glyphData.getRawInt()>0 && currentFontData.CMapName!=null && currentFontData.getFontType()==StandardFonts.CIDTYPE0 && currentFontData.getGlyphData().getCharStrings().containsKey(String.valueOf(glyphData.getRawInt()));
        
        final boolean debug=false;

        boolean isMultiByte=false;
        //ignore these cases
        if(currentFontData.CMapName!=null && currentFontData.CMapName.equals("OneByteIdentityH") || stream[i]=='>'){
            
            if(debug) {
                System.out.println("ignore currentFontData.CMapName=" + currentFontData.CMapName + " stream[i+2]=" + (char)stream[i]+ ' ' + (char)stream[i+1]+ ' ' + (char)stream[i+2]);
            }
            
        }else if(!hasCharString){//not sure if really needed
            
            final char combinedVal=(char)getValue(3, stream, i);

            final int isDouble=currentFontData.isDoubleBytes(val, combinedVal & 255,false);
            
            //if the combined value has a glyph, assume a 4 byte CID value
            if(isDouble==1 || currentFontData.glyphs.getEmbeddedGlyph( new T1GlyphFactory(false),null , null, combinedVal, "", -1, null)!=null){
                isMultiByte=true;
                val=combinedVal;
                charSize=4;
                
                if(debug) {
                    System.out.println("use 2 values=" + Integer.toHexString(combinedVal));
                }
            }
        }
        
        if(isMultiByte){
            return setValue(glyphData, val, i, charSize, currentFontData, parserOptions);
        }else{
            return i+1;
        }
    }
    
    private static int setValue(GlyphData glyphData, int val, int i, int charSize, PdfFont currentFontData, ParserOptions parserOptions) {
        
        //System.out.println("setValue="+val+" "+i+" "+charSize);
        
        glyphData.setRawInt(val);
        i = i + charSize-1; //move offset
        glyphData.setRawChar((char) val);
        glyphData.setDisplayValue(currentFontData.getGlyphValue(val));
        if(currentFontData.isCIDFont() && currentFontData.getCMAP()!=null && currentFontData.getUnicodeMapping(val)==null){
            glyphData.setRawChar(glyphData.getDisplayValue().charAt(0));
            glyphData.setRawInt(glyphData.getRawChar());
        }
        if(parserOptions.isTextExtracted()) {
            glyphData.setUnicodeValue(currentFontData.getUnicodeValue(glyphData.getDisplayValue(), glyphData.getRawInt()));
        }
        
        return i;
    }

    private static int getValue(int chars, final byte[] stream, int i) {
        
        int topHex, ptr=0,val=0;
        
        for(int aa=0;aa<chars+1;aa++){
            
            topHex=stream[i+chars-aa];
            
            //convert to number
            if(topHex>='A' && topHex<='F'){
                topHex -= 55;
            }else if(topHex>='a' && topHex<='f'){
                topHex -= 87;
            }else if(topHex>='0' && topHex<='9'){
                topHex -= 48;
            }else{    //ignore 'bum' values
                continue;
            }
            val += (topHex << TD.multiply16[ptr]);
            ptr++;
        }
        return val;
    }
}
