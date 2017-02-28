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
 * EscapedTextUtils.java
 * ---------------
 */
package org.jpedal.parser.text;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.parser.ParserOptions;
import org.jpedal.render.DynamicVectorRenderer;

/**
 *
 * @author markee
 */
class EscapedTextUtils {

    static int getEscapedValue(int i, final byte[] stream,final GlyphData glyphData, final PdfFont currentFontData, 
            final int streamLength, final ParserOptions parserOptions, DynamicVectorRenderer current) {
        // any escape chars '\\'=92
        i++;
        
        glyphData.setLastChar(glyphData.getRawChar());//update last char as escape
        
        if ((streamLength > (i + 2)) && (Character.isDigit((char) stream[i]))) {
            
            //see how long number is
            int numberCount = 1;
            if (Character.isDigit((char) stream[i + 1])) {
                numberCount++;
                if (Character.isDigit((char) stream[i + 2])) {
                    numberCount++;
                }
            }
            
            // convert octal escapes
            int rawInt = TD.readEscapeValue(i, numberCount, 8, stream);
            i = i + numberCount - 1;
            
            if (rawInt > 255) {
                rawInt -= 256;
            }
            
            glyphData.setRawChar((char) rawInt); //set to dummy value as may be / value
            
            glyphData.setRawInt(rawInt);
            
           glyphData.setDisplayValue(currentFontData.getGlyphValue(rawInt));
            
            if (parserOptions.isTextExtracted()) { 
                glyphData.setUnicodeValue(currentFontData.getUnicodeValue(glyphData.getDisplayValue(), rawInt));
                
                //if ffi or similar we want to map to correct value
                if(glyphData.getUnicodeValue().length()>1){
                    final int mapped=StandardFonts.getAdobeMap(glyphData.getUnicodeValue());
                    
                    if(mapped!=-1){
                        glyphData.setUnicodeValue(String.valueOf((char)mapped));
                    }
                }
            }
            
            //allow for \134 (ie \\)
            if (glyphData.getRawChar() == 92) // '\\'=92
            {
                glyphData.setRawChar((char)120);
            }
            
        } else {
            
            int rawInt = stream[i] & 255;
            glyphData.setRawChar((char) rawInt);
            
            if (glyphData.getRawChar() == 'u') { //convert unicode of format uxxxx to char value
                rawInt = TD.readEscapeValue(i + 1, 4, 16, stream);
                i += 4;
                //rawChar = (char) glyphData.rawInt;
                glyphData.setDisplayValue(currentFontData.getGlyphValue(rawInt));
                if(parserOptions.isTextExtracted()) {
                    glyphData.setUnicodeValue(currentFontData.getUnicodeValue(glyphData.getDisplayValue(), rawInt));
                }
                
            } else {
                
                char testChar=glyphData.getRawChar();
                if(testChar=='n'){
                    rawInt='\n';
                    glyphData.setRawChar('\n');
                }else if(testChar=='b'){
                    rawInt='\b';
                    glyphData.setRawChar('\b');
                }else if(testChar=='t'){
                    rawInt='\t';
                    glyphData.setRawChar('\t');
                }else if(testChar=='r'){
                    rawInt='\r';
                    glyphData.setRawChar('\r');
                }else if(testChar=='f'){
                    rawInt='\f';
                    glyphData.setRawChar('\f');
                }
                
                glyphData.setDisplayValue(currentFontData.getGlyphValue(rawInt));
                
                if (parserOptions.isTextExtracted()) {
                    glyphData.setUnicodeValue(currentFontData.getUnicodeValue(glyphData.getDisplayValue(),rawInt));
                }
                
                if (!glyphData.getDisplayValue().isEmpty()){ //set raw char
                    glyphData.setRawChar(glyphData.getDisplayValue().charAt(0));
                }
            }
            
            glyphData.setRawInt(rawInt);
        }
        //fix for character wrong in some T1 fonts
        if(currentFontData.getFontType()==StandardFonts.TYPE1 && current.isHTMLorSVG()){
            final String possAltValue = currentFontData.getMappedChar(glyphData.getRawInt(),true);
            if(possAltValue!=null && possAltValue.length()==1 && possAltValue.equalsIgnoreCase(glyphData.getUnicodeValue().toLowerCase())){
                glyphData.set(possAltValue);
            }
        }
        return i;
    }
    
}
