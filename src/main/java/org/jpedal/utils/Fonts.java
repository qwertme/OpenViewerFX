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
 * Fonts.java
 * ---------------
 */
package org.jpedal.utils;

import java.util.StringTokenizer;

/**
 * general font routines to create XML font token or extract information
 * from the token or create a font object
 */
public class Fonts {
    
    public static final String fe="</font>";
    
    public static final String fb="<font ";
    
    //////////////////////////////////////////////////////////////////////////
    /**
     * Take data which has just been joined and tidy up fonts<br> Removes
     * duplicate font commands where a font is turned off with /FONT tag and
     * then turned back on again <BR>
     * This arises because we put font tokens into the data and then may merge
     * it together.
     */
    public static final String cleanupTokens(final String input) {
        final StringBuilder output_data = new StringBuilder();
        String current_token, current_font = "";
        int pointer = 0;
        boolean next_font_is_identical = false;
        final StringTokenizer data_As_tokens = new StringTokenizer(input, "<>", true);
        String next_item = data_As_tokens.nextToken();
        
        //work through all tokens in the data
        while (data_As_tokens.hasMoreTokens()) {
            
            if (next_item.equals("<") && data_As_tokens.hasMoreTokens()) {
                
                //get token
                current_token =
                        next_item
                        + data_As_tokens.nextToken()
                        + data_As_tokens.nextToken();
                pointer += current_token.length();
                //where we are in original data
                next_item = ""; //set to no value
                
                //track font in use so we can eliminate font off/same font on in data
                if ((current_token.startsWith(fb))) {
                    current_font = current_token;
                }
                
                /**
                 * //reset font tracking on td so table fonts preserved
                 * if ((current_token.toLowerCase().startsWith("</td"))|
                 * (current_token.toLowerCase().startsWith("</right"))
                 * |(current_token.toLowerCase().startsWith("</center"))|(current_token.toLowerCase().startsWith("</p"))) {
                 * current_font = "";
                 * next_font_is_identical = false;
                 * }
                 */
                
                //ignore if next font the same - otherewise keep
                if ((current_token.equals(fe))) {
                    
                    /**don't lose if if we are about to end a token pair
                     *
                     */
                    final int nextToken=input.indexOf('<', pointer - 1);
                    final int nextEndToken=input.indexOf("</", pointer - 1);
                    
                    if(nextToken==nextEndToken){
                        output_data.append(current_token);
                    }else{
                        
                        final int next_font_pointer_s =input.indexOf(fb, pointer - 1);
                        final int next_font_pointer_e =input.indexOf('>', next_font_pointer_s);
                        next_font_is_identical = false;
                        
                        if ((next_font_pointer_s != -1)
                                && (next_font_pointer_e != -1)) {
                            final String next_font =input.substring(next_font_pointer_s,next_font_pointer_e + 1);
                            if (next_font.equals(current_font)) {
                                next_font_is_identical = true;
                            }
                        }
                        
                        //add if no matches
                        if (!next_font_is_identical) {
                            output_data.append(current_token);
                        }
                    }
                } else if ((current_token.startsWith(fb))& next_font_is_identical) {
                    next_font_is_identical = false; //ignore next font command
                } else {
                    output_data.append(current_token);
                }
            } else {
                
                //not token so put in data
                output_data.append(next_item);
                pointer += next_item.length();
                //where we are in original data
                next_item = "";
            }
            
            //read next item if not read already
            if ((data_As_tokens.hasMoreTokens())){
                next_item = data_As_tokens.nextToken();
                
                /**allow for it being the last item*/
                if (!data_As_tokens.hasMoreTokens()){
                    //not token so put in data
                    output_data.append(next_item);
                    pointer += next_item.length();
                }
                
            }
        }
        
        return output_data.toString();
    }
    
    /**
     * extract font size from font string. If a value is not found
     * in the first value, the second will be used.
     */
    public static final String getActiveFontTag(
            final String raw_string,
            final String full_value) {
        int start;
        final int end;
        String return_value = "";
        start = raw_string.lastIndexOf(fb);
        
        if (start > -1) {
            end = raw_string.indexOf("\">", start);
            if (end > 0) {
                return_value = raw_string.substring(start, end + 2);
            }
        } else {
            start = full_value.lastIndexOf(fb);
            
            if (start > -1) {
                end = full_value.indexOf("\">", start);
                if (end > 0) {
                    return_value = full_value.substring(start, end + 2);
                }
            }
        }
        
        return return_value;
    }
    
    /**
     * create XML font token for putting into stream
     */
    public static final String createFontToken(String font_name, final int font_size) {
        
        final String font_token;
        
        //set font used and include styles for truetype (ie Arial,Bold)
        final int pointer = font_name.indexOf(',');
        if (pointer != -1) {
            final String weight = font_name.substring(pointer + 1);
            font_name = font_name.substring(0, pointer);
            font_token =fb+
                    "face=\""
                    + font_name
                    + "\" style=\"font-size:"
                    + font_size
                    + "pt;font-style:"
                    + weight
                    + "\">";
        } else {
            font_token =fb+
                    "face=\""
                    + font_name
                    + "\" style=\"font-size:"
                    + font_size
                    + "pt\">";
        }
        return font_token;
    }
}
