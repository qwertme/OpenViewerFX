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
 * CharReader.java
 * ---------------
 */

package org.jpedal.parser.text;

/**
 *
 * @author markee
 */
class CharReader {

    
    static int getNextValue(int i, final byte[] stream, final GlyphData glyphData, final boolean isCID) {
        
        int rawInt;
        
        //extract the next binary index value and convert to char, losing any returns
        while(true){
            
            glyphData.setLastChar();
            
            rawInt = stream[i];
            if (rawInt < 0) {
                rawInt = 256 + rawInt;
            }
            
            glyphData.setRawChar((char)rawInt);
            
            //eliminate escaped tabs and returns
            if(glyphData.getRawChar()==92 &&(stream[i+1]==13 || stream[i+1]==10)){ // '\\'=92
                i++;
                rawInt = stream[i];
                if (rawInt < 0) {
                    rawInt = 256 + rawInt;
                }
                glyphData.setRawChar((char) rawInt);
                
            }
            
            glyphData.setRawInt(rawInt);
            
            //stop any returns in data stream getting through (happens in ghostscript)
            if((glyphData.getRawChar()!=10 || (isCID && glyphData.getOpenChar()!='<')) && glyphData.getRawChar()!=13) {
                break;
            }
            
            i++;
        }
        
        /**flag if we have entered/exited text block*/
        if (glyphData.isText()) {
            //non CID deliminator (allow for escaped deliminator)
            final char testChar=glyphData.getRawChar();
            if ((testChar==40 || testChar==41) && glyphData.getLastChar() != 92) {  // '\\'=92 ')'=41
                glyphData.updatePrefixCount(testChar);
            } else if (testChar == 62 && glyphData.getOpenChar() == 60 ){  // ie <01>tj  '<'=60 '<'=62
                glyphData.setText(false); //unset text flag
            }
        }
        
        return i;
    }    

}
