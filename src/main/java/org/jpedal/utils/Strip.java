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
 * Strip.java
 * ---------------
 */
package org.jpedal.utils;

/**
 * general functionality to manipulate and trim string values
 */
public class Strip
{
    
    private static final String strip_start_token_deliminator = "<&";
    private static final String strip_end_token_deliminator = ">;";
    
    /**
     * remove any XML tokens to leave just text - this could be optimised further
     */
    public static  StringBuilder stripXML(final String raw_data, final boolean isXMLExtraction) {
        
        if(raw_data==null) {
            return null;
        } else {
            return stripXML(new StringBuilder(raw_data),isXMLExtraction);
        }
    }
    
    
    /**
     * remove any XML tokens to leave just text - this could be optimised further
     */
    public static  StringBuilder stripXML(final StringBuilder all_tags, final boolean isXMLExtraction) {
        
        final int rawLength=all_tags.length();
        StringBuilder general_buffer = new StringBuilder(rawLength);
        StringBuilder tag=new StringBuilder(rawLength);
        
        /**safety trap*/
        if (all_tags == null) {
            return general_buffer;
        }
        
        
        if(isXMLExtraction){
            
            /**
             * general values
             */
            char next_token;
            char start = ' ';
            boolean inToken = false;
            boolean inAttributeValue = false;
            
            //read in all tokenizer chars and loop round for each combo
            
            final int length = all_tags.length();
            boolean match;
            
            for (int i = 0; i < length; i++) {
                next_token = all_tags.charAt(i);
                match=false;
                
                if(inToken && next_token=='"'){
                    inAttributeValue = !inAttributeValue;
                }
                
                //catch spurious &...< in data
                if((inToken)&&(start == '&')&&((next_token=='<')||(next_token=='&'))){
                    //inToken=false;
                    general_buffer.append(tag);
                    tag=new StringBuilder(5);
                }
                
                
                if (!inAttributeValue && (strip_start_token_deliminator.indexOf(next_token)!= -1)) {
                    inToken = true;
                    start = next_token;
                    match=true;
                }
                
                //catch spurious & in data
                if((inToken)&&(start == '&')&&(next_token==' ')){
                    inToken=false;
                    general_buffer.append('&');
                    tag=new StringBuilder(5);
                }
                
                //add if not part of tag
                if (!inToken) {
                    general_buffer.append(next_token);
                } else {
                    tag.append(next_token);
                }
                
                
                if(!match){
                    final int endPointer =strip_end_token_deliminator.indexOf(next_token);
                    if ((endPointer == 1) & (start == '&')) {
                        
                        //put back <> into text
                        if(tag.toString().equals("&lt;")) {
                            general_buffer.append('<');
                        } else if(tag.toString().equals("&gt;")) {
                            general_buffer.append('>');
                        } else if(tag.toString().equals("&amp;")) {
                            general_buffer.append('&');
                        }
                        
                        
                        inToken = false;
                        tag=new StringBuilder();
                    } else if ((endPointer == 0) & (start == '<')) {
                        inToken = false;
                        tag=new StringBuilder();
                    }else if(!inToken && next_token=='&'){
                        general_buffer.append('&');
                    }
                }
                
            }
        }else {
            general_buffer=all_tags;
        }
        
        general_buffer =Strip.trim(general_buffer);
        
        
        return general_buffer;
    }
    
    /**
     * remove any XML tokens to leave just text - this could be optimised further
     */
    public static  StringBuilder stripXMLArrows(final StringBuilder all_tags, final boolean isXMLExtraction) {
        
        StringBuilder general_buffer = new StringBuilder();
        StringBuilder tag=new StringBuilder();
        
        /**safety trap*/
        if (all_tags == null) {
            return general_buffer;
        }
        
        if(isXMLExtraction){
            
            /**
             * general values
             */
            char next_token;
            char start = ' ';
            boolean inToken = false;
            
            //read in all tokenizer chars and loop round for each combo
            
            final int length = all_tags.length();
            for (int i = 0; i < length; i++) {
                next_token = all_tags.charAt(i);
                
                if ((strip_start_token_deliminator.indexOf(next_token)!= -1)) {
                    inToken = true;
                    start = next_token;
                }
                
                //add if not part of tag
                if (!inToken) {
                    general_buffer.append(next_token);
                } else {
                    tag.append(next_token);
                }
                
                final int endPointer =strip_end_token_deliminator.indexOf(next_token);
                if ((endPointer == 1) & (start == '&')) {
                    
                    //put back <> into text
                    if((!tag.toString().equals("&lt;"))&&(!tag.toString().equals("&gt;"))) {
                        general_buffer.append(tag);
                    }
                    
                    inToken = false;
                    tag=new StringBuilder();
                } else if ((endPointer == 0) & (start == '<')) {
                    inToken = false;
                    tag=new StringBuilder();
                }
            }
        }else {
            general_buffer=all_tags;
        }
        
        general_buffer =Strip.trim(general_buffer);
        
        return general_buffer;
    }
    
    
    /**
     * remove any leading spaces
     */
    public static final StringBuilder trim( final StringBuilder content_buffer )
    {
        char c;
        
        final int length=content_buffer.length();
        
        /**
         * StringBuffer cb=new StringBuffer();
         * cb.setLength(length);
         * 
         * for(int ii=0;ii<length;ii++){
         * //System.out.println(ii+" "+length);
         * cb.setCharAt(ii,content_buffer.charAt(ii));
         * }
         * 
         * int len2=cb.length();
         */
        
//		//remove all start spaces
//		for( int i = 0;i < length;i++ )
//		{
//			c = content_buffer.charAt( i );
//
//			if( c == ' '  ){
//				content_buffer.deleteCharAt( i );
//				length--;
//				i--;
//				if(length==0)
//					break;
//			}else
//				i=length;
//		}

        for (int i = length - 1; i > -1; i--) {

            c = content_buffer.charAt(i);
            if (c == ' ') {
                content_buffer.deleteCharAt(i);
            } else {
                i = -1;
            }
        }

        return content_buffer;
    }
    
    /////////////////////////////////
    /**
     * remove multiple spaces and returns so just single value if multiples
     * together - if it fails will just return data
     */
    public static final String removeMultipleSpacesAndReturns( final String data )
    {
        
        final StringBuilder all_data = new StringBuilder( data );
        // remove multiple spaces and carriage returns using a loop
        int i = 1;
        while( i < all_data.length() )
        {
            if( ( ( all_data.charAt( i ) == ' ' ) && ( all_data.charAt( i - 1 ) == ' ' ) ) || ( ( all_data.charAt( i ) == Character.LINE_SEPARATOR ) && ( all_data.charAt( i - 1 ) == Character.LINE_SEPARATOR ) ) ) {
                all_data.deleteCharAt( i );
            } else {
                i++;
            }
        }
        return all_data.toString();
    }
    /////////////////////////////////////////////////
    /**
     * remove leading spaces
     */
    public static final String stripSpaces( final String data )
    {
        final StringBuilder text = new StringBuilder( ( data ) );
        
        //strip leading chars
        while( text.length() > 0 )
        {
            if( text.charAt( 0 ) != ' ' ) {
                break;
            }
            text.deleteCharAt( 0 );
            
            //strip ending chars
            int pointer2 = text.length() - 1;
            while( pointer2 > 0 )
            {
                if( text.charAt( pointer2 ) != ' ' ) {
                    break;
                }
                text.deleteCharAt( pointer2 );
                pointer2--;
                if( pointer2 < 0 ) {
                    break;
                }
            }
        }
        return text.toString();
    }
    /////////////////////////////////////////////////
    /**
     * removes all the spaces
     */
    public static final String stripAllSpaces( final String data )
    {
        final StringBuilder text = new StringBuilder( ( data ) );
        
        //strip ending chars
        int pointer2 = text.length() - 1;
        while( pointer2 > 0 )
        {
            if( text.charAt( pointer2 ) == ' ' ) {
                text.deleteCharAt( pointer2 );
            }
            pointer2--;
            if( pointer2 < 0 ) {
                break;
            }
        }
        return text.toString();
    }
    
/////////////////////////////////////////////////
    /**
     * removes all the spaces
     */
    public static final StringBuilder stripArrows( final StringBuilder text )
    {
        
        //strip ending chars
        int pointer2 = text.length() - 1;
        if(pointer2 >= 0){
            while( true ){
                
                if(( text.charAt( pointer2 ) == '<' )||( text.charAt( pointer2 ) == '>' )) {
                    text.deleteCharAt( pointer2 );
                }
                
                pointer2--;
                
                if( pointer2 < 0 ) {
                    break;
                }
            }
        }
        return text;
    }
    /////////////////////////////////////////////////
    /**
     * removes all the spaces
     */
    public static final StringBuilder stripAllSpaces( final StringBuilder text )
    {
        
        //strip ending chars
        int pointer2 = text.length() - 1;
        while( pointer2 > 0 )
        {
            if( text.charAt( pointer2 ) == ' ' ) {
                text.deleteCharAt( pointer2 );
            }
            pointer2--;
            if( pointer2 < 0 ) {
                break;
            }
        }
        return text;
    }
    
    /**
     * Strip out XML tags and put in a tab
     * (do not use on Chinese text)
     */
    public static final String convertToText( final String input, final boolean isXMLExtraction) {
        final StringBuffer output_data;
        
        if(isXMLExtraction){
            /**
             * ////////////////////////
             * String current_token = "";
             * StringBuffer old=new StringBuffer();
             * StringTokenizer data_As_tokens = new StringTokenizer( input,"<>", true );
             * 
             * while( data_As_tokens.hasMoreTokens() ){
             * String next_item = data_As_tokens.nextToken();
             * if( ( next_item.equals( "<" ) ) & ( ( data_As_tokens.hasMoreTokens() ) ) ) {
             * //get token
             * current_token = next_item + data_As_tokens.nextToken() +data_As_tokens.nextToken();
             * 
             * //the tab in the data
             * if( current_token.equals( "<Space" ) )
             * old.append('\t');
             * 
             * }else
             * old.append( next_item );
             * }/**/
            ////////////////////////////
            //new code
            
            final byte[] rawData=StringUtils.toBytes(input);
            final int length=rawData.length;
            int ptr=0;
            boolean inToken=false;
            for(int i=0;i<length;i++){
                
                if(rawData[i]=='<'){
                    inToken=true;
                    
                    if(rawData[i+1]=='S' && rawData[i+2]=='p' && rawData[i+3]=='a' && rawData[i+4]=='c' && rawData[i+5]=='e'){
                        rawData[ptr]='\t';
                        ptr++;
                    }
                }else if(rawData[i]=='>'){
                    inToken=false;
                }else if(!inToken){
                    rawData[ptr]=rawData[i];
                    ptr++;
                }
            }
            
            final byte[] cleanedString=new byte[ptr];
            System.arraycopy(rawData,0,cleanedString,0,ptr);
            
            output_data=new StringBuffer(new String(cleanedString));
            
            //if(!old.toString().equals(output_data.toString()))
            //throw new RuntimeException("MIsmatch on text old="+old+" new="+output_data);
        }else {
            output_data=new StringBuffer(input);
        }
        
        return output_data.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////
    /**
     * remove any comment from a string in Storypad menu
     * comments are prefixed with a **
     */
    public static final String stripComment( String value )
    {
        
        //if there is a value, find ** and strip from there, along with excess spaces
        if( value != null )
        {
            final int pointer = value.indexOf( "**" );
            if( pointer > 0 ) {
                value = value.substring( 0, pointer - 1 ).trim();
            }
            if( pointer == 0 ) {
                value = "";
            }
        }
        return value;
    }
    
    public static StringBuilder stripAmpHash(final StringBuilder current_text) {
        final StringBuilder sb = new StringBuilder();
        boolean inEscape=false;
        char nextChar;
        for(int i=0;i<current_text.length();i++){
            
            nextChar = current_text.charAt(i);
            
            if(inEscape){
                if(nextChar == ';') {
                    inEscape=false;
                }
            }else{
                if(nextChar == '&') {
                    inEscape=true;
                } else {
                    sb.append(nextChar);
                }
            }
        }
        
        return sb;
    }
}
