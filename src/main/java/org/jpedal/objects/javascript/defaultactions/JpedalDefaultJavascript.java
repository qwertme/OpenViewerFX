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
 * JpedalDefaultJavascript.java
 * ---------------
 */

package org.jpedal.objects.javascript.defaultactions;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.awt.Toolkit;

import org.jpedal.utils.NumberUtils;
import org.jpedal.utils.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/** JS
 * this whole class has methods for javascript actions 
 * and should not be accessed by anything else
 */

public class JpedalDefaultJavascript{
	
	/** added to make the testing be more predictable */
	public static boolean testingStopAlertBoxes;
	public static boolean testingSetStaticDate;
	
	private static final boolean DebugDefaultJavascript = false;

	private static final String format[] = {"'D':yyyyMMddHHmmssZ","'D':yyyyMMddHHmmssZ'Z","yyyy/MM/dd HH:mm:ss"};
//	private String eventValue = null; 

	/** store the scope for this javascript object so we can create our javascript strings to return */
	private final Scriptable scope;
	
	/** store the context so that we can create the javascript strings */
	private final Context context;
	
	/** added for js so that the js can figure out which type of viewer we implement*/
    public static final String viewerType = "Exchange-Pro";
	
	/** added for js so that the js can figure out which version of viewer we implement*/
    public static final int viewerVersion = 10;
	
	/*JS if 1 or more then calculations are allowed if 0 or less they are not */
	public static int calculate = 1;
	
	//If true, the background color and hover color for form fields are shown.
	public boolean runtimeHighlight;
	
	public JpedalDefaultJavascript(final Scriptable scopeItem, final Context contextItem){
		if(DebugDefaultJavascript) {
            System.out.println("JpedalDefaultJavascript constructed");
        }
		
		scope = scopeItem;
		context = contextItem;
	}

	/** sorts the date into the string format specified 
	 * <p>
	 * longMonth = "mmmm", shortMonth = "mmm", lead0NumberedMonth = "mm", 
	 * nonLead0NumberedMonth = "m", longDay = "dddd", shortDay= "ddd", lead0NumberedDay = "dd",
	 * nonLead0NumberedDay = "d", longYear = "yyyy", shortYear = "yy", lead024Hour = "HH",
	 * nonLead024Hour = "H", lead012Hour = "hh", nonLead012Hour ="h", lead0Mins = "MM", nonLead0Mins = "M",
	 * lead0Secs = "ss", nonLead0Secs = "s", amPm = "tt", singleDigitAmPm = "t", escapeChar = "\";
	 * 
	 * <br>returns a javascript string.
	 */
	public Object printd(final String format, final org.mozilla.javascript.Scriptable obj) {
		if(DebugDefaultJavascript) {
            System.out.println("JpedalDefaultJavascript.printd(String,Scriptable)");
        }
		
		// get the Date class from the Scriptable value passed in as 
		// Rhino does not allow NativeDate to be used externally, even though this is what is passed here.
		Date jsDate = null;
		if ( org.mozilla.javascript.NativeJavaObject.canConvert(obj, Date.class ) ) {
			jsDate = (java.util.Date) org.mozilla.javascript.Context.jsToJava( obj, Date.class );
		}
		if ( jsDate == null ) {
			org.mozilla.javascript.Context.throwAsScriptRuntimeEx( new RuntimeException( "Not a Date()" ));
		}
		
		Calendar date = Calendar.getInstance();
		date.setTime(jsDate);
		//jsDate = null;//delete the variable so it does not get used
		
		// <start-demo><end-demo>
		
		// replace the javascript format string with a java format string to produce the required output.
		//we need to do this as some chars represent different types of data in each
		final char[] formatVal = format.toCharArray();
		final StringBuffer retVal = new StringBuffer();
		for(int i=0; i < formatVal.length; i++){
			if(formatVal[i] == 'm'){//replace JS month with java month
				retVal.append('M');
			}else if(formatVal[i] == 't'){
				if(formatVal[i+1] == 't'){//if tt replace with char for AM/PM
					retVal.append('a');
					i++;
				}else {// single t special case, replace with either A/P
					if(date.get(Calendar.HOUR_OF_DAY)>12) {
                        retVal.append("'P'");
                    } else {
                        retVal.append("'A'");
                    }
				}
			}else if(formatVal[i] == 'M'){// replace JS min for java min
				retVal.append('m');
			}else if(formatVal[i] == 'd' && formatVal[i+1] == 'd' && formatVal[i+2] == 'd'){
				//if word for day is needed then change to java word of day
				if(formatVal[i+3] == 'd'){//long JS day replaced with long java day
					retVal.append("EEEE");
					i+=3;
				}else {// short JS day replaced with short java day
					retVal.append("EEE");
					i+=2;
				}
			}else if(formatVal[i] == '\\'){
				retVal.append('\'');
				retVal.append(formatVal[++i]);
				retVal.append('\'');
			}else { 
				retVal.append(formatVal[i]);
			}
		}
		
		// create the formatter object with the required format
		final SimpleDateFormat df = new SimpleDateFormat(new String(retVal));
		final String newDate = df.format(date.getTime());
		
		if(DebugDefaultJavascript) {
            System.out.println("returning String=" + newDate);
        }
		
		return context.newObject(scope, "String", new Object[] { newDate});
	}
	
	/** sorts the date into a prerequisite format based on the index.
	 * 
	 * currently does not work corretly with javascript method calls,
	 * if this method was commented in, it would be called, even for calls to printd(String,Scritable)
	 * <br>returns a javascript string.
	 */
	public Object printd(final int index, final org.mozilla.javascript.Scriptable obj) {
		if(DebugDefaultJavascript) {
            System.out.println("JpedalDefaultJavascript.printd(int,Scriptable)");
        }
		
		Calendar date = null;
		if ( org.mozilla.javascript.NativeJavaObject.canConvert(obj, Calendar.class ) ) {
			date = (java.util.Calendar) org.mozilla.javascript.Context.jsToJava( obj, Calendar.class );
		}

		if ( date == null ) {
			org.mozilla.javascript.Context.throwAsScriptRuntimeEx( new RuntimeException( "Not a Date()" ));
		}
		
		// <start-demo><end-demo>
		
		final SimpleDateFormat df = new SimpleDateFormat(format[index]);
		
		return context.newObject(scope, "String", new Object[] { df.format(date)});
	}
	
	/**
	 * sorts the arguments passed in into a string defined by the template given in cFormat, %(char) says 
	 * what type it should be in.
	 * there can be as many arguments (comma-seperated) as there are % tags in the format string.
	 * 
	 * see page 720 of 'Javascript for Acrobat API reference'
	 */
	public Object printf(final String cFormat, final String[] args){
		/*
		cFormat - The format string to use.
	 	arguments - The optional arguments(s) that contain the data to be inserted in place of the % tags 
	 		specified in the first parameter, the format string. The number of optional arguments must be 
	 		the same as the number of % tags.
		 */
		final StringBuilder buf = new StringBuilder();
		final char PERCENT = '%';
		int tokenCount = 0;
		int stringLoct = 0;
		int tokenPos = cFormat.indexOf(PERCENT);
		String obj;
		
		while(tokenPos!=-1){
			buf.append(cFormat.substring(stringLoct, tokenPos));
			
			//store the new start position in 'stringLoct'
			int end2Pos;
			final char[] endTokens = {'d','f','s','x'};
			int c=0;
			stringLoct = cFormat.indexOf(endTokens[c++],tokenPos);
			for(; c<endTokens.length;c++){
				end2Pos = cFormat.indexOf(endTokens[c],tokenPos);
				if(stringLoct==-1 || (end2Pos!=-1 && end2Pos<stringLoct)) {
                    stringLoct = end2Pos;
                }
			}
			
			stringLoct++;//to include the character we just checked for
			final String tok = cFormat.substring(tokenPos,stringLoct);//pass in the rest of the string as the token could be long
			
			//get the argument to use
			obj = args[tokenCount++];
			
			/** NOTE CHRIS this is a hack until we can store a display and a usable value for each field */
			//remove , for multipuls of 1000, ie make 1,000 into 1000
			final int comma = obj.indexOf(',');
			final int stop = obj.indexOf('.');
			if((comma!=-1 && stop!=-1) && (comma < stop)){
					//remove commas
					final StringBuilder str = new StringBuilder();
					char next;
					for (int i = 0; i < obj.length(); i++) {
						next = obj.charAt(i);
						if(next!=','){
							str.append(next);
						}
					}
					obj = str.toString();
				}
			
			final String val = convertToken(tok,obj);
			buf.append(val);
			
			//get the next token position in the string if there is one
			tokenPos = cFormat.indexOf(PERCENT, tokenPos+1);
		}
		
		//add the rest of the string
		if(stringLoct<cFormat.length()) {
            buf.append(cFormat.substring(stringLoct));
        }
		
		return context.newObject(scope, "String", new Object[] { buf.toString() });
	}
		
	/** for the use of the printf commend */
	private static String convertToken(final String token, final String arg1) {
		
		if(!StringUtils.isNumber(arg1)){
			return "";
		}
		
		final double value = Double.parseDouble(arg1);
		
		//a seperator is used for dates, phone numbers, times, ete
		int decimalPoints = -1,minWidth=0;
		char decimal = '.'; //seperator = ',',
		boolean padd = false,floatDecimal = false;
		final StringBuilder sValue = new StringBuilder();//used to store the value in string form for each type of output
		final StringBuilder returnString = new StringBuilder();
		
		//conversion token layout: %[,nDecSep][cFlags][nWidth][.nPrecision]cConvChar
		final char[] tokArray = token.toCharArray();
		if(tokArray[0]=='%'){
			int i=1;
			final int size = tokArray.length;
			loop:
			while(i<size){
				switch(tokArray[i]){
				case ',':
//					nDecSep - A comma character (,) followed by a digit that indicates the decimal/separator format:
					switch(tokArray[++i]){
					case '0': 
//						0 � Comma separated, period decimal point
						//seperator = ',';
						decimal = '.';
						break;
					case '1':
//						1 � No separator, period decimal point
						//seperator = 0;
						decimal = '.';
						break;
					case '2':
//						2 � Period separated, comma decimal point
						//seperator = '.';
						decimal = ',';
						break;
					case '3':
//						3 � No separator, comma decimal point
						//seperator = 0;
						decimal = ',';
						break;
					}
					break;
					
//				cFlags - Only valid for numeric conversions and consists of a number of characters (in any order), 
//				which will modify the specification:
				case '+'://cFlags
//					+ � Specifies that the number will always be formatted with a sign.
					if(value>0) {
                        returnString.append('+');
                    } else {
                        returnString.append('-');
                    }
					break;
				case ' '://cFlags
//					space � If the first character is not a sign, a space will be prefixed.
					if(value>0) {
                        returnString.append(' ');
                    } else {
                        returnString.append('-');
                    }
					break;
				case '0'://cFlags
//					0 � Specifies padding to the field with leading zeros.
					padd = true;
					break;
				case '#'://cFlags
//					# � Specifies an alternate output form. For f, the output will always have a decimal point.
					floatDecimal = true;
					break;
					
				case '.':
//					nPrecision - A period character (.) followed by a number that specifies the number of digits 
//					after the decimal point for float conversions.
					decimalPoints = Integer.parseInt(String.valueOf(tokArray[++i]));
					break;
					
				case 'd'://cConvChar
	//				d � Integer (truncating if necessary)
					sValue.append((int)value);
					if(padd){
						final int stringlen = returnString.length()+sValue.length();
						if(stringlen<minWidth) {
                            for (int p = 0; p < minWidth - stringlen; p++) {
                                returnString.append('0');
                            }
                        }
					}
					returnString.append(sValue);
					break loop;
					
				case 'f'://cConvChar
	//				f � Floating-point number
					if(decimalPoints!=-1){
						if(decimalPoints==0) {
                            sValue.append((int) value);
                        } else{
							final NumberFormat nf = NumberFormat.getInstance();
							nf.setMinimumFractionDigits(decimalPoints);
							nf.setMaximumFractionDigits(decimalPoints);
							sValue.append(nf.format(value));
						}
					}else {
                        sValue.append((float) value);
                    }
					
					if(floatDecimal && sValue.indexOf(".")!=-1) {
                        sValue.append('.');
                    }
					if(padd){
						final int stringlen = returnString.length()+sValue.length();
						if(stringlen<minWidth) {
                            for (int p = 0; p < minWidth - stringlen; p++) {
                                returnString.append('0');
                            }
                        }
					}
					String ssVal = sValue.toString();
					ssVal = ssVal.replace('.', decimal);//replace the decimal point with the defined one
					returnString.append(ssVal);
					break loop;
					
				case 's'://cConvChar
	//				s � String
					sValue.append(value);//amay need arg1
					if(padd){
						final int stringlen = returnString.length()+sValue.length();
						if(stringlen<minWidth) {
                            for (int p = 0; p < minWidth - stringlen; p++) {
                                returnString.append('0');
                            }
                        }
					}
					returnString.append(sValue);
					break loop;
					
				case 'x'://cConvChar
	//				x � Integer (truncating if necessary) and formatted in unsigned hexadecimal notation
					final int valI = (int)(value);
					final String retValS = Integer.toHexString(valI);
					
					sValue.append(retValS);
					if(padd){
						final int stringlen = returnString.length()+sValue.length();
						if(stringlen<minWidth) {
                            for (int p = 0; p < minWidth - stringlen; p++) {
                                returnString.append('0');
                            }
                        }
					}
					returnString.append(sValue);
					break loop;
					
				default:
//					nWidth - A number specifying a minimum field width. The converted argument is formatted to be at 
//					least this many characters wide, including the sign and decimal point, and may be wider 
//					if necessary. If the converted argument has fewer characters than the field width, it is 
//					padded on the left to make up the field width. The padding character is normally a space, 
//					but is 0 if the zero padding flag is present (cFlags contains 0).
					minWidth = NumberUtils.parseInt(0, 1, new byte[]{(byte) tokArray[i]});
					break;
				}
				i++;
			}//end while loop
		}
		
		return returnString.toString();
	}

	/** Breaks a URL into its component parts.
	 * Javascript for Acrobat API Reference version 8.1 p716
	 */
	public static Map crackURL(final String cURL){
		if(!cURL.startsWith("file") && !cURL.startsWith("http") && !cURL.startsWith("https")){
			return null;
		}
		
		final Map propertiesMap = new HashMap();
		final int index1 = cURL.indexOf("://");
		propertiesMap.put("cScheme", cURL.substring(0,index1));
		
		if(cURL.contains("@")){
			//http://user:password@host/path
			final int userInd = cURL.indexOf(':',index1+3);
			final int atInd = cURL.indexOf('@');
			final int index2 = cURL.indexOf('/',atInd+1);
			
			propertiesMap.put("cHost", cURL.substring(atInd+1,index2));
			
			propertiesMap.put("cUser", cURL.substring(index1+3,userInd));
			propertiesMap.put("cPassword", cURL.substring(userInd+1,atInd));
			propertiesMap.put("cPath", cURL.substring(index2+1));
		}else {
			//http://host[:port]/directory/file
			final int splitInd = cURL.indexOf(':',index1);
			final int index2 = cURL.indexOf('/',splitInd);
			
			propertiesMap.put("cHost", cURL.substring(index1+3,splitInd));
			propertiesMap.put("nPort", cURL.substring(splitInd+1,index2));
			
			propertiesMap.put("cPath", cURL.substring(index2+1));
		}

		return propertiesMap;
	}
	
	/**
	 * formats the given value into the required format
	 */
	public static double z(final String format, final double value) {
		final double val1 = value * 100;
		final double val2 = Math.round(val1);
		return val2/100;

	}
	
	/** tells the system to beep at the user */
	public static void beep(final int type){
		Toolkit.getDefaultToolkit().beep();
	}
}
