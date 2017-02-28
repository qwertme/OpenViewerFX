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
 * Leading.java
 * ---------------
 */

package org.jpedal.parser.text;

import java.util.StringTokenizer;
import static org.jpedal.parser.text.TD.getString;
import org.jpedal.utils.NumberUtils;

/**
 *
 * @author markee
 */
class Leading {
    
    /**thousand as a value*/
    static final float THOUSAND=1000;

    static float getLeading(final byte[] stream, final int ptr, final int leadingStart, final boolean isMultipleValues) {

        //get string
        int strt=leadingStart;
        while(stream[strt]==10 || stream[strt]==9 || stream[strt]==32 || stream[strt]==13) {
            strt++;
        }

        //more than one value separated by space
        float value;
        if(isMultipleValues){

            //read values
            final StringTokenizer values=new StringTokenizer(getString(strt,strt+ptr-1,stream));
            value=0;
            while(values.hasMoreTokens()) {
                value += Float.parseFloat(values.nextToken());
            }

            value =-value/ THOUSAND;
        }else{
            value=-NumberUtils.parseFloat(strt, strt + ptr, stream)/THOUSAND;
        }

        return value;
    }
    
     
     static int readLeading(int i, final byte[] stream, GlyphData glyphData) {
        
// ')'=41 '>'=62 '<'=60
        //handle leading between text ie -100 in  (The)-100(text)
        float value = 0;
        i++;
        //allow for spaces
        while(stream[i]==32 || stream[i]==13 || stream[i]==10) //' '=32
        {
            i++;
        }
        char nc = (char) stream[i], rc=' ';
        
        //allow for )( or >< (ie no value)
        if(nc==40 || nc==60){ //'('=40 '<'=60
            i--;
        }else if (nc != 39 && nc != 34 && nc != 40 &&  nc != 93 && nc != 60) { //leading so roll on char
            //'\''=39 '\"'=34 '('=40  //']'=93 '<'=60
            int ptr=0;
            
            final int leadingStart=i; //allow for failure
            boolean failed=false;
            boolean isMultipleValues=false, isLastValue=false;
            while (!failed) {
                rc = nc;
                if(rc!=10 && rc !=13){
                    ptr++;
                }
                
                nc = (char) stream[i + 1];
                
                if(nc==32) {
                    isMultipleValues = true;
                }
                
                if(nc==']') {
                    isLastValue = true;
                }
                
                if (nc == 40 || nc == 60 || nc==']' || nc==10){ // '('=40 '<'=60
                    break;
                }
                
                if(nc==45 || nc==46 || nc==32 || (nc>='0' && nc<='9')){
                    //'-'=45 '.'=46 ' '=32
                }else {
                    failed = true;
                }
                
                i++;
            }
            
            if(failed) {
                i = leadingStart;
            } else if(isMultipleValues || ptr>0){
                value = getLeading(stream, ptr, leadingStart, isMultipleValues);
            }
            
            //is someone adds on leading at end ignore as it breaks extraction width calculation
            if(isLastValue && value==-glyphData.getWidth()){
                //width=width-value;
                glyphData.subtractLeading(value);
               
            }
        }
        
        glyphData.updateGlyphSettings(value,rc);
        
        
        return i;
    }
   

}
