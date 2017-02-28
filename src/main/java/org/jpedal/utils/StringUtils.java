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
 * StringUtils.java
 * ---------------
 */
package org.jpedal.utils;

import java.io.UnsupportedEncodingException;

import org.jpedal.fonts.StandardFonts;
import org.jpedal.io.TextTokens;
import org.jpedal.parser.DecoderOptions;

public class StringUtils {
    
    private static final int ampersand = '&';
    private static final int ampersandInt = 'A'; //use captial A as not escaped char
    private static final int aInt = 97;
    private static final int zeroInt = 48;
    private static final int nineInt = 57;
    private static final int openSquareBracketInt = 91;
    private static final int closeSquareBracketInt = 93;
    private static final int openCurlyBracket = 40;
    private static final int closeCurlyBracket = 41;
    private static final int backSlashInt = 92;
    private static final int forwardSlashInt = 47;
    private static final int hashInt = 35;
    private static final int divideInt = 247;
    private static final int fullStopInt = 46;
    private static final int spaceInt = 32;
    private static final int percentInt = 37;
    private static final int minusInt = 45;
    private static final int underScoreInt = 95;
//    private final static int backSlachInt = 92;
//    private final static int nInt = 110;
//    private final static int newLineInt = 10;
private static final int plusInt = 43;
    private static final int pInt = 112;
    private static final int colonInt = 58;
    private static final int equalsInt = 61;
    private static final int cInt = 99;
    private static final int qInt = 113;
    
    private static String enc;
    
    static{
        enc=System.getProperty("file.encoding");
        
        if(enc.equals("UTF-8") || enc.equals("MacRoman") || enc.equals("Cp1252")){
            //fine carry on
        }else if(DecoderOptions.isRunningOnMac) {
            enc="MacRoman";
        } else if(DecoderOptions.isRunningOnWindows) {
            enc="Cp1252";
        } else {
            enc="UTF-8";
        }
    }

    /**
     * turn any hex values (ie #e4) into chars
     * @param value
     * @return
     */
    public static final String convertHexChars(final String value) {
        
        //avoid null
        if(value==null) {
            return value;
        }
        
        //find char
        final int escapeChar=value.indexOf(hashInt);
        
        if(escapeChar==-1) {
            return value;
        }
        
        //process
        final StringBuilder newString=new StringBuilder();
        final int length=value.length();
        //newString.setLength(length);
        
        char c;
        
        for(int ii=0;ii<length;ii++){
            c=value.charAt(ii);
            
            if(c==hashInt){
                ii++;
                int end=ii+2;
                if(end>length) {
                    end=length;
                }
                final String key=value.substring(ii,end);
                
                c=(char)Integer.parseInt(key,16);
                
                ii++;
                
                if(c!=spaceInt) {
                    newString.append(c);
                }
            }else {
                newString.append(c);
            }
            
            
        }
        
        return newString.toString();
    }
    
    /** check to see if the string contains anything other than
     * '-' '0-9' '.'
     * if so then its not a number.
     */
    public static boolean isNumber(final String textString) {
        final byte[] data=StringUtils.toBytes(textString);
        final int strLength=data.length;
        boolean isNumber=true;
        
        //assume true and disprove
        for(int j=0;j<strLength;j++){
            if((data[j]>=zeroInt && data[j] <=nineInt)|| data[j]==fullStopInt
                    || (j==0 && data[j]==minusInt)){ //assume and disprove
            }else{
                isNumber=false;
                //exit loop
                j=strLength;
            }
        }
        
        return isNumber;
    }
    
    /** replaces all spaces ' ' with underscores '_' to allow the whole name to be used in HTML
     *
     */
    public static String makeHTMLNameSafe(String name) {
        
        if(name==null || name.isEmpty()) {
            return name;
        }
        
        char[] chrs = name.toCharArray();
        
        //replace any dodgy chars
        if(name.indexOf(percentInt)!=-1 || name.indexOf(spaceInt)!=-1 || name.indexOf(fullStopInt)!=-1 ||
                name.indexOf(plusInt)!=-1 || name.indexOf(colonInt)!=-1 || name.indexOf(equalsInt)!=-1 ||
                name.indexOf(forwardSlashInt)!=-1 || name.indexOf(backSlashInt)!=-1){
            //NOTE: if you add any more please check with main method above for int values and DONT use char
            //strings as they are not cross platform. search for 'UNIVERSAL equivalents' to find main method.
            for (int i = 0; i < chrs.length; i++) {
                switch(chrs[i]){
                    
                    case ampersand:
                        chrs[i] = ampersandInt;
                        break;
                        
                    case spaceInt:
                        chrs[i] = underScoreInt;
                        break;
                        
                    case fullStopInt:
                        chrs[i] = minusInt;
                        break;
                        
                        //replace & with safe char as images break if in path ?? ANY IDEA WHAT THIS LINE IS??
                    case percentInt:
                        chrs[i] = underScoreInt;
                        break;
                        
                    case plusInt:
                        chrs[i] = pInt;
                        break;
                        
                    case colonInt:
                        chrs[i] = cInt;
                        break;
                        
                    case equalsInt:
                        chrs[i] = qInt;
                        break;
                        
                    case forwardSlashInt:
                        chrs[i] = underScoreInt;
                        break;
                        
                    case backSlashInt:
                        chrs[i] = underScoreInt;
                        break;
                }
            }
        }
        
        final char[] testchrs = {openSquareBracketInt,closeSquareBracketInt,hashInt,divideInt,
            openCurlyBracket,closeCurlyBracket};
        int count = 0;
        for (final char chr1 : chrs) {
            for (final char testchr : testchrs) {
                if (chr1 == testchr) {
                    count++;
                }
            }
        }
        
        if(count>0){
            int c=0;
            final char[] tmp = new char[chrs.length-count];
            MAINLOOP:
            for (final char chr : chrs) {
                for (final char testchr : testchrs) {
                    if (chr == testchr) {
                        continue MAINLOOP;
                    }
                }
                tmp[c++] = chr;
            }
            chrs = tmp;
            
        }
        
        if(chrs[0]>=zeroInt && chrs[0]<=nineInt){
            final char[] tmp = new char[chrs.length+1];
            System.arraycopy(chrs,0,tmp,1,chrs.length);
            tmp[0] = aInt;
            chrs = tmp;
        }
        
        name = new String(chrs);
        
        return name;
    }

    /**
     * read a text String held in fieldName in string
     */
    public static String getTextString(final byte[] rawText, final boolean keepReturns) {
        
        String returnText="";
        
        //make sure encoding loaded
        StandardFonts.checkLoaded(StandardFonts.PDF);
        
        char[] chars=null;
        if(rawText!=null) {
            chars=new char[rawText.length*2];
        }
        int ii=0;
        char nextChar;
        
        final TextTokens rawChars=new TextTokens(rawText);
        
        //test to see if unicode
        if(rawChars.isUnicode()){
            //its unicode
            while(rawChars.hasMoreTokens()){
                nextChar=rawChars.nextUnicodeToken();
                
                //breask a file and does not appear used so removed 2013/5/20
                if(nextChar==9 || (!keepReturns && (nextChar==10 || nextChar==13))){
                    chars[ii]=32;
                    ii++;
                }else 
                if(nextChar>31 || (keepReturns && (nextChar==10 || nextChar==13))){
                    chars[ii]=nextChar;
                    ii++;
                }
            }
            
        }else{
            //pdfDoc encoding
            
            while(rawChars.hasMoreTokens()){
                nextChar=rawChars.nextToken();
                
                String c = null;
               if(nextChar==9 || (!keepReturns && (nextChar==10 || nextChar==13))){
                    c = " ";
                }else if (keepReturns && (nextChar==10 || nextChar==13)){
                    c = String.valueOf( nextChar );
                }else if(nextChar>31 && nextChar<253){
                    c=StandardFonts.getEncodedChar(StandardFonts.PDF,nextChar);
                }
                
                if ( c != null ){
                    final int len=c.length();
                    
                    //resize if needed
                    if(ii+len>=chars.length){
                        final char[] tmp=new char[len+ii+10];
                        System.arraycopy(chars, 0, tmp, 0, chars.length);
                        chars=tmp;
                    }
                    
                    //add values
                    for(int i=0;i<len;i++){
                        chars[ii]=c.charAt(i);
                        ii++;
                    }
                }
            }
        }
        
        if(chars!=null) {
            returnText=String.copyValueOf(chars,0,ii);
        }
        
        return returnText;
        
    }
    
    
    
    public static String replaceAllManual(String string, final int find, final String replace){
        int index = string.indexOf(find);
        while(index!=-1){
            string = string.substring(0,index)+
                    replace+string.substring(index+1);
            //Continue from last point as replacing & with amp
            //will cause infinite loop if we search from start each time.
            index = string.indexOf(find, index+1);
        }
        return string;
    }
    
    public static String correctSpecialChars(String string) {
    	//Do this in a separate loop and exit after first occurance
    	//else we end up in an infinite loop as we keep adding '&'
    	//for special character
        
        //can be null value
        if(string==null) {
            return null;
        }
        
    	for (int i = 0; i < string.length(); i++) {
    		if(string.charAt(i)==38){
    			string = replaceAllManual(string,38, "&amp;");
    			i=string.length();
    		}
    	}
        for (int i = 0; i < string.length(); i++) {
            switch(string.charAt(i)){
                case 225: string = replaceAllManual(string,225, "&aacute;");
                break;
                case 224: string = replaceAllManual(string,224, "&agrave;");
                break;
                case 226: string = replaceAllManual(string,226, "&acirc;");
                break;
                case 229: string = replaceAllManual(string,229, "&aring;");
                break;
                case 227: string = replaceAllManual(string,227, "&atilde;");
                break;
                case 228: string = replaceAllManual(string,228, "&auml;");
                break;
                case 230: string = replaceAllManual(string,230, "&aelig;");
                break;
                case 231: string = replaceAllManual(string,231, "&ccedil;");
                break;
                case 233: string = replaceAllManual(string,233, "&eacute;");
                break;
                case 232: string = replaceAllManual(string,232, "&egrave;");
                break;
                case 234: string = replaceAllManual(string,234, "&ecirc;");
                break;
                case 235: string = replaceAllManual(string,235, "&euml;");
                break;
                case 237: string = replaceAllManual(string,237, "&iacute;");
                break;
                case 236: string = replaceAllManual(string,236, "&igrave;");
                break;
                case 238: string = replaceAllManual(string,238, "&icirc;");
                break;
                case 239: string = replaceAllManual(string,239, "&iuml;");
                break;
                case 241: string = replaceAllManual(string,241, "&ntilde;");
                break;
                case 243: string = replaceAllManual(string,243, "&oacute;");
                break;
                case 242: string = replaceAllManual(string,242, "&ograve;");
                break;
                case 244: string = replaceAllManual(string,244, "&ocirc;");
                break;
                case 248: string = replaceAllManual(string,248, "&oslash;");
                break;
                case 245: string = replaceAllManual(string,245, "&otilde;");
                break;
                case 246: string = replaceAllManual(string,246, "&ouml;");
                break;
                case 223: string = replaceAllManual(string,223, "&szlig;");
                break;
                case 250: string = replaceAllManual(string,250, "&uacute;");
                break;
                case 249: string = replaceAllManual(string,249, "&ugrave;");
                break;
                case 251: string = replaceAllManual(string,251, "&ucirc;");
                break;
                case 252: string = replaceAllManual(string,252, "&uuml;");
                break;
                case 255: string = replaceAllManual(string,255, "&yuml;");
                break;
                case 8217: string = replaceAllManual(string,8217,"&#39;");
                break;
                    //to find other codes check out http://www.interfacebus.com/html_escape_codes.html
            }
        }
        
        return string;
    }
    
    
    public static byte[] toBytes(final String value) {
        
        byte[] data=null;
        
        try {
            data=value.getBytes(enc);
            
        } catch (final UnsupportedEncodingException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: "+e.getMessage());
            }
            //
        }
        return data;
    }
    
    /**
     * Replaces illegal characters that aren't allowed in code
     * @param S String to have characters replaced in
     * @return A safe String that can be used as a Java or Javascript variable or function
     */
    public static String makeMethodSafe(final String S) {
        String name = makeHTMLNameSafe(S);
        name = name.replace("-", "_");
        return name;
    }
    
    /**
     * Replaces all illegal characters as defined by ses the standard UNICODE
     * Consortium character repertoire. This means it strips out characters between:
     * 0 to 31 inclusive and 127 to 159 inclusive.
     * @param S
     * @return
     */
    public static String stripIllegalCharacters(final String S) {
        final StringBuilder newString = new StringBuilder();
        for(int i = 0; i < S.length(); i ++) {
            final char ch = S.charAt(i);
            if( (ch < 32 && ch >= 0) || (ch > 126 && ch < 160) ) {
                continue;
            }
            newString.append(ch);
        }
        return newString.toString();
    }
}
