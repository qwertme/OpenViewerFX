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
 * CIDTextUtils.java
 * ---------------
 */

package org.jpedal.parser.text;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.parser.ParserOptions;

/**
 *
 * @author markee
 */
public class CIDTextUtils {
    
    static int getCIDCharValues(int i, final byte[] stream, final int streamLength, final GlyphData glyphData, final PdfFont currentFontData, final ParserOptions parserOptions) {
        
        /**
         * first time we read the first 2 values and then decide if we are in single or
         * double byte mode
         * (ie is there a 0 x 0y pattern)
         * (or do the 2 values on their own form valid settings)
         */
        final boolean debug=false;
        
        float actualWidth=0;
        
        //lazy init if needed
        if(StandardFonts.CMAP==null){
            StandardFonts.readCMAP();
        }
        
        int firstVal=glyphData.getRawInt();
        final String firstValue;
        String newValue=null;

        //System.out.println(">>"+Integer.toHexString(firstVal));
        /**
         * read first value
         */
        //if escaped roll on
        if(firstVal==92){
            
            i++;
            
            firstVal= stream[i] & 255;
            
            if ((streamLength > (i + 2)) && (Character.isDigit((char) stream[i]))) {
                
                //see how long number is
                int numberCount = 1;
                if (Character.isDigit((char)  stream[i+1])) {
                    numberCount++;
                    if (Character.isDigit((char)  stream[i+2])) {
                        numberCount++;
                    }
                }
                
                // convert octal escapes
                firstVal = TD.readEscapeValue(i, numberCount, 8, stream);
                i = i + numberCount - 1;
                
                if (firstVal > 255) {
                    firstVal -= 256;
                }
                
            }else if (firstVal == 'u') { //convert unicode of format uxxxx to char value
                firstVal = TD.readEscapeValue(i + 1, 4, 16, stream);
                i += 4;
                
            } else {
                
                firstVal = convertEscapeChar(firstVal);
            }
            
            glyphData.setRaw(firstVal);
            
        }else{
            firstVal=glyphData.getRawChar();
        }
        
        //get as 1 byte value
        firstValue = StandardFonts.CMAP[glyphData.getRawChar()];
        
        if(debug) {
            System.out.println("1 byte values=" + (int) glyphData.getRawChar() + " val=" + firstValue + " isDouble=" + currentFontData.isCIDFont() + " currentFontData.hasDoubleBytes=" + currentFontData.hasDoubleBytes+ ' ' +currentFontData.isDoubleBytes());//+" "+(char)stream[i-2]+" "+(char)stream[i-1]+" "+(char)stream[i]+" "+(char)stream[i+1]+" "+(char)stream[i+2]+" "+(char)stream[i+3]);
        }
        
        /**
         * read second byte if needed (we always read first time to see if double byte or single)
         */
        final boolean isEmbedded =currentFontData.isFontEmbedded;
        
        //also check if mapped in Charstring
        //separates out
        // PDFdata/baseline_screens/customersDec2012/5771020130000784D.pdf and
        //PDFdata/sample_pdfs_html/general/JavaMagazine glassfish article.pdf
        final boolean hasCharString=glyphData.getRawInt()>0 && currentFontData.CMapName!=null && currentFontData.getFontType()==StandardFonts.CIDTYPE0 && currentFontData.getGlyphData().getCharStrings().containsKey(String.valueOf(glyphData.getRawInt()));
        
        //ignore this case
        if(currentFontData.CMapName!=null && currentFontData.CMapName.equals("OneByteIdentityH")){
            //System.out.println(currentFontData.CMapName);
            
        }else if(!hasCharString && (currentFontData.hasDoubleBytes || firstValue==null || currentFontData.isDoubleBytes()!=0 || (glyphData.getRawInt()>128 && glyphData.getRawInt()!=233))){
            
            //flag incase we are wrong and need to switch back
            final int iBefore=i;
            
            i++;
            
            int secondVal= stream[i] & 255;
            
            boolean secondByteIsEscaped=false;
            
            //if escaped roll on as workaround hack
            if(stream[i]==92){
                i++;
                
                secondByteIsEscaped=true;
                
                if(glyphData.getRawInt()==0){
                    while(stream[i]==13 || (stream[i]==92 && stream[i-1]==13)){ //allow for garbage in stream
                        i++;
                    }
                }
                secondVal=stream[i] & 255;
                
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
                    secondVal = TD.readEscapeValue(i, numberCount, 8, stream);
                    i = i + numberCount - 1;
                    
                    if (secondVal > 255) {
                        secondVal -= 256;
                    }
                    
                }else if (secondVal == 'u') { //convert unicode of format uxxxx to char value
                    secondVal = TD.readEscapeValue(i + 1, 4, 16, stream);
                    i += 4;
                    
                } else {
                    
                    secondVal = convertEscapeChar(secondVal);
                }
            }
            
            final int secondByte=secondVal;
            
            final char combinedVal=(char)((glyphData.getRawChar()<<8)+secondVal);
            
            //lookup in 2 byte version
            newValue = StandardFonts.CMAP[combinedVal];
            
            final int isDouble=currentFontData.isDoubleBytes(firstVal,secondByte,secondByteIsEscaped);
            
            if(debug) {
                System.out.println("2 byte values=" + newValue + ' ' + " isDouble=" + isDouble + ' ' + combinedVal + ' ' + firstValue);
            }
            
            //if no 2 byte value either default to 1 byte
            if(isEmbedded  && (isDouble==1 || combinedVal<256 || newValue!=null)){// || (!secondByteIsEscaped && secondByte!=')'))){
                glyphData.setRawInt(combinedVal);
                glyphData.setRawChar(combinedVal);
                
                if(debug) {
                    System.out.println("use 2 values=" + Integer.toHexString(combinedVal) + " new value=" + newValue + " isEmbedded=" + isEmbedded + ' ' + (!secondByteIsEscaped && secondByte != ')'));
                }
                
            }else if(!isEmbedded && isDouble==1 &&(newValue!=null || combinedVal<256 || (!secondByteIsEscaped && secondByte!=')'))){
                glyphData.setRawInt(combinedVal);
                glyphData.setRawChar(combinedVal);
                
                if(debug) {
                    System.out.println("use 2 values=" + combinedVal + ' ' + newValue);
                }
                
            }else if(isDouble==0 && !isEmbedded && firstVal>128 && newValue!=null && firstValue==null){
                glyphData.setRawInt(combinedVal);
                glyphData.setRawChar(combinedVal);
                
                if(debug) {
                    System.out.println("TEST2 " + newValue + ' ' + StandardFonts.CMAP[secondByte]);
                }
                
            }else if(isDouble==0 && !isEmbedded && firstVal>128 && newValue==null && firstValue!=null){
                
                i=iBefore;
                //glyphData.rawInt=combinedVal;
                //rawChar=(char)f;
                // newValue = String.valueOf(rawChar);
                newValue=firstValue;
                if(debug) {
                    System.out.println("TEST2 " + newValue + ' ' + StandardFonts.CMAP[secondByte]);
                }
                
            }else{
                i=iBefore;
                
                if(debug) {
                    System.out.println("reset " + newValue + ' ' + StandardFonts.CMAP[secondByte]);
                }
            }
            
            if(!isEmbedded){
                actualWidth = currentFontData.getDefaultWidth(glyphData.getRawInt());
                
                if(actualWidth==-1){
                    actualWidth = currentFontData.getDefaultWidth(-1);
                }
            }
            
        }else{
            
            actualWidth =-1;
            
            if((!isEmbedded) &&
                (currentFontData.getFontType()==StandardFonts.CIDTYPE0 || currentFontData.getFontType()==StandardFonts.CIDTYPE2)){
                    actualWidth = currentFontData.getDefaultWidth(glyphData.getRawInt());
                    
                    if(actualWidth==-1){
                        actualWidth = currentFontData.getDefaultWidth(-1)/2;
                    }
                }
            }
        
        
        glyphData.setActualWidth(actualWidth);
        
        //if no value ignore for moment
        if(newValue!=null){
            glyphData.setDisplayValue(newValue);
        }else{  //default if no value
            glyphData.setDisplayValue(String.valueOf(glyphData.getRawChar()));
        }
        
        if(parserOptions.isTextExtracted()){ //(not sure if this is correct - may need more samples)
            glyphData.setUnicodeValue(currentFontData.getUnicodeValue(glyphData.getDisplayValue(), glyphData.getRawChar()));
        }
        
        //fix for \\) at end of stream
        if(glyphData.getRawChar()==92) {
            glyphData.setValueForHTML(92);
            glyphData.setRawChar((char)120);
        }
        
        if(debug) {
            System.out.println("returns =" + glyphData.getDisplayValue() + ' ' + glyphData.getUnicodeValue() + " int=" + glyphData.getRawInt() + " actualWidth=" + actualWidth);
        }
        
        return i;
    }

    static int convertEscapeChar(int secondVal) {
        if(secondVal=='n'){
            secondVal='\n';
        }else if(secondVal=='b'){
            secondVal='\b';
        }else if(secondVal=='t'){
            secondVal='\t';
        }else if(secondVal=='r'){
            secondVal='\r';
        }else if(secondVal=='f'){
            secondVal='\f';
        }
        return secondVal;
    }
}
