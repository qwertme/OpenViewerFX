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
 * JSFunction.java
 * ---------------
 */
package org.jpedal.objects.javascript.functions;

import org.jpedal.constants.ErrorCodes;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.acroforms.ReturnValues;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

import javax.swing.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.jpedal.parser.DecoderOptions;

/**
 * base class for functions with shared code
 */

public class JSFunction {

    final AcroRenderer acro;
    final FormObject formObject;

    public static final int AFDate = 1;
    public static final int AFNumber = 2;
    public static final int AFPercent = 3;
    public static final int AFRange = 4;
    public static final int AFSimple = 5;
    public static final int AFSpecial = 6;
    public static final int AFTime = 7;

    static final int AVG=1;
    static final int SUM=2;
    static final int PRD=3;
    static final int MIN=4;
    static final int MAX=5;

    public static final int UNKNOWN = -1;
    public static final int KEYSTROKE = 1;
    public static final int VALIDATE = 2;
    public static final int FORMAT = 3;
    public static final int CALCULATE = 4;
    
    public boolean DECIMAL_IS_COMMA;
    
    String value;
    
    private static int staticGapformat = -1;
    private static int staticDecimalcount = -1;
    
    /** sets generic values for the gap format and the decimal count<br>
     * <br>
     * int gapFormat:<br>
     * 0  Comma separated, period decimal point<br>
     * 1  No separator, period decimal point<br>
     * 2  Period separated, comma decimal point<br>
     * 3  No separator, comma decimal point <br>
     * <br>
     * int decCount - the number of decimal places after the decimal point.
     * 
     * if either is -1 it is ignored
     */
    public static void setValidDataFormat(final int gapFormat, final int decCount){
    	staticDecimalcount = decCount;
    	staticGapformat = gapFormat;
    }
    
    public static int getStaticGapFormat(){ return staticGapformat; }
    public static int getStaticDecimalCount(){ return staticDecimalcount; }

    public JSFunction(final AcroRenderer acro, final FormObject formObject) {
        
        this.acro=acro;

        this.formObject=formObject;

    }

    public static void debug(final String str){

    	//
    }

    /**apply one of more matching patterns and return where a match*/
    protected static String applyRegexp(final String text, final String[] patterns ) {
        String matchedString="";

        final int patternCount=patterns.length;

        for(int i = 0; i<patternCount; i++){

            final Pattern pa=Pattern.compile(patterns[i]);
            final Matcher m=pa.matcher(text);
            if(m.matches()){
                i=patternCount;
                final int start=m.start();
                final int end=m.end();
                matchedString=text.substring(start,end);

                //System.out.println(matchedString+" "+patterns[i]);
            }
        }

        return matchedString;
    }
    
    /**
     * general routine to handle array values
     */
    String processArray(final String nextValue, final int operation) {

        float result=0;
        boolean resultNotSet = true;

        boolean hasDec=false, hasData=false;

        final String[] args2=convertToArray(nextValue);
        
        //add together except first item (which is new Array)
        final float arrayCount=args2.length;
        
        for(int ii=1;ii<arrayCount;ii++){

            //nextValue=stripQuotes(args2[ii]);
            
//            String val=(String)formObject.getFormValue();
            String val = null;
			final String strippedValue = args2[ii].replaceAll("\"", "");
			final Object[] os = acro.getFormComponents(strippedValue, ReturnValues.FORMOBJECTS_FROM_NAME, -1);
//			System.out.println(os.length);
			if(os.length > 0) {
				final FormObject o = (FormObject) os[0];
				val = o.getValue();
			}
            
            //if string is empty set value to 0 in calculations
            if(val==null || val.isEmpty()){
            	val="0";
            }
        	
            hasData=true;

            final boolean isNegative=val.startsWith("-");

            final float nextVal;
            
            
            //interprete the value properly, replace commas or remove full stops.
            if(DECIMAL_IS_COMMA){
            	val = val.replaceAll("\\.", "");// "\\. as its regular expression and quotes next char ie '\.' is decimal
            	val = val.replaceAll(",", ".");
            }else{
            	//check if we only have a comma
            	if(val.indexOf(',')!=-1 && !val.contains(".")){
            		val=val.replace(',', '.');
            	}else {
            		val = val.replaceAll(",", "");
            	}
            }
            
            //flag if any values and if decimal
            if(val.indexOf('.')!=-1) {
                hasDec = true;
            }
            
            if(isNegative){
                nextVal= -Float.parseFloat(val.substring(1));
            }else {
                nextVal = Float.parseFloat(val);
            }
            
            switch(operation){
                case AVG:
                    result += nextVal;
                    break;

                case SUM:
                    result += nextVal;
                    break;

                case PRD:
                	if(resultNotSet){
                		result = 1;
                		resultNotSet = false;
                	}
                    result *= nextVal;
                    break;

                case MIN:
                    if(ii==1) {
                        result = nextVal;
                    } else if(nextVal<result) {
                        result = nextVal;
                    }
                    break;

                case MAX:
                    if(ii==1) {
                        result = nextVal;
                    } else if(nextVal>result) {
                        result = nextVal;
                    }
                    break;
                
                default:
                    debug("Unsupported op "+operation+" in processArray");
                     break;
            }
        }
        
        //post process
        if(operation==AVG) {
            result /= (arrayCount - 1);
        }

        if(hasDec) {
            return String.valueOf(result);
        } else if(!hasData) {
            return "";
        } else {
            return String.valueOf((int) result);
        }
    }

    /**
     * turn javascript string into values in an array
     */
    public static String[] convertToArray(String js) {

        final String rawCommand=js;

        final int ptr=js.indexOf('(');
        int items=0,count=0;
        final String[] values;
        String finalValue="";

        final List rawValues=new ArrayList();

        /**
         * first value is command
         */
        if(ptr!=-1){

            final String com=js.substring(0,ptr);

            rawValues.add(com);

            items++;

            //remove
            js=js.substring(ptr,js.length()).trim();

            int charsAtEnd=1;

            //lose ; as well
            if(js.endsWith(";")) {
                charsAtEnd++;
            }

            //remove main brackets and possibly ;
            if(js.startsWith("(")) //strip brackets
            {
                js = js.substring(1, js.length() - charsAtEnd);
            } else {
                debug("Unknown args in " + rawCommand);
            }
        }

        /**
         * break into values allowing for nested values
         */
        final StringTokenizer tokens=new StringTokenizer(js,"(,);",true);
        while(tokens.hasMoreTokens()){

            //get value
            StringBuilder nextValueStr=new StringBuilder(tokens.nextToken());

            //allow for comma in brackets
            while(tokens.hasMoreTokens() && nextValueStr.toString().startsWith("\"") && !nextValueStr.toString().endsWith("\"")) {
                nextValueStr.append(tokens.nextToken());
            }
            
            final String nextValue=nextValueStr.toString();

            if(count==0 && nextValue.equals(",")){
                rawValues.add(finalValue);

                finalValue="";
                items++;
            }else{
                if(nextValue.equals("(")) {
                    count++;
                } else if(nextValue.equals(")")) {
                    count--;
                }

                finalValue += nextValue;
            }
        }

        //last value
        items++;
        rawValues.add(finalValue);


        //turn into String array to avoid casting later
        //(could be rewritten later to be cleaner if time/performance issue)
        values=new String[items];
        for(int ii=0;ii<items;ii++){
            values[ii]=((String)rawValues.get(ii)).trim();
            //System.out.println(ii+" >"+values[ii]+"<");
            }

        return values;
    }

    /**
     * ensure any empty slots at start filled
     */
    private static String padString(final String rawVal, final int maxLen) {

        final int length= rawVal.length();
        
        if(maxLen ==length) {
            return rawVal;
        } else if(maxLen <length) {
            return rawVal;
        } else{
            final StringBuilder paddedString=new StringBuilder();

            final int extraChars=maxLen-length;

            for(int jj=0;jj<extraChars;jj++) {
                paddedString.append('0');
            }

            paddedString.append(rawVal);

            return paddedString.toString();
        }
    }

    //alert user and reset to old value
    void maskAlert(final int code, final Object[] args){

        //restore old value
		String validValue= formObject.getLastValidValue();
		if(validValue==null) {
            validValue = "";
        }


        formObject.setLastValidValue(validValue);//may not be needed as is same value as should be there
		formObject.updateValue(validValue,false, true);


        if(((String) args[0]).contains(" R")){
        	args[0] = formObject.getTextStreamValue(PdfDictionary.T);
        }
        
        reportError(code, args);

    }

    //apply formatting in mask to data
    //returns null if invalid
    String validateMask(final String[] args, final String separator, final boolean useDefaultValues) {

    	final String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
    	final int[] monthsCount={31,28,31,30,31,30,31,31,30,31,30,31};

    	int monthMod = 1;
    	int monthValue = 0;
    	int dayValue = 0;
    	
    	String validValue=null;

    	final int count=args.length;
    	if(count!=2){
    		String list="";
    		for(int i=0;i<count;i++){

    			if(i==0) {
                    list = args[i];
                } else {
                    list = list + ',' + args[i];
                }

    		}

    		JSFunction.debug("Unexpected values items="+count+ '{' +list+ '}');

    	}else{

    		boolean isValid=true; //assume okay and disprove

    		String formData;

            formData=(String)formObject.getFormValue();

    		if(formData==null || formData.isEmpty()) {
                return "";
            }

    		final String endText;

    		//some values have additions such as PM/AM
    		final int space=formData.lastIndexOf(' ');
    		if(space!=-1){
    			endText=formData.substring(space+1).toLowerCase().trim();

    			//must end am or pm  is does, strip it off
    			if((endText!=null) &&
    				(endText.equals("am") || endText.equals("pm"))) {
                        formData = formData.substring(0, space);
                    
//  				else
//  				return null;
    			}
    		}

    		String mask=stripQuotes(args[1]);

    		//Day must be "XX" not "X"
    		final int d = mask.indexOf('d');
    		if(mask.charAt(d+1)!='d'){
    			mask = mask.replaceFirst("d", "dd");
    		}
    		
    		final StringTokenizer maskValues=new StringTokenizer(mask,separator,true); //ie mm:dd:yyyy
    		final StringTokenizer formValues=new StringTokenizer(formData,separator,true); //ie 01:01:2007
    		//match each part

    		final StringBuilder finalValue=new StringBuilder();

    		String nextMask,nextVal,nextSep, paddedValue;

    		//get a time instance and defaults for all Date values here
    		final GregorianCalendar gc = new GregorianCalendar();

    		//loop through and test each value
    		while(maskValues.hasMoreTokens()){

    			paddedValue="";

    			//get next mask and any next value (allowing for multiple separators)
    			while(true){
    				nextMask=maskValues.nextToken();
    				
    				if(!separator.contains(nextMask) || !maskValues.hasMoreTokens()) {
                        break;
                    }

    				//its muliple separators so append and retry
    				finalValue.append(nextMask);

    			}
    			
    			while(true){
    				//get form if there is one
        			if(!formValues.hasMoreTokens()) {
                        nextVal = null; //run out of values
                    } else {
                        nextVal = formValues.nextToken();
                    }
        			
    				if(nextVal==null || !separator.contains(nextVal) || !formValues.hasMoreTokens()) {
                        break;
                    }
    			}

    			if(maskValues.hasMoreTokens()) {
                    nextSep = maskValues.nextToken();
                } else {
                    nextSep = null;
                }
    			
    			if(nextVal!=null) {
                    paddedValue = padString(nextVal, nextMask.length());
                }
    			
    			if(nextMask.equals("h")){ //12 hour clock

    				//allow for null value in Date
    				if(useDefaultValues && nextVal==null){
    					paddedValue= String.valueOf(gc.get(Calendar.HOUR));
    					// <start-demo><end-demo>
    				}else {
                        paddedValue = padString(nextVal, 2);
                    }
    				
					isValid = verifyNumberInRange(paddedValue,0,11);
    				

    			}else if(nextMask.equals("HH")){ //24 hours clock

    				//allow for null value in Date
    				if(useDefaultValues && nextVal==null){
    					paddedValue= String.valueOf(gc.get(Calendar.HOUR_OF_DAY));
    					// <start-demo><end-demo>
    					paddedValue=padString(paddedValue,2);
    					isValid = verifyNumberInRange(paddedValue,0,23);
    				}else{
    					isValid = verifyNumberInRange(paddedValue,0,23);
    				}

    			}else if(nextMask.equals("MM")){

    				//allow for null value in Date
    				if(useDefaultValues && nextVal==null){
    					paddedValue= String.valueOf(gc.get(Calendar.MINUTE));
    					// <start-demo><end-demo>
    					paddedValue=padString(paddedValue,2);
    					isValid = verifyNumberInRange(paddedValue,0,59);
    				}else{
    					isValid = verifyNumberInRange(paddedValue,0,59);
    				}

    			}else if(nextMask.equals("mm") || nextMask.equals("m")){

    				isValid = verifyNumberInRange(paddedValue,0,12);
    				if(isValid){
    					final int numVal = Integer.parseInt(paddedValue);
    					if((paddedValue.length()!=nextMask.length()) && 
    						(nextMask.length()==1)) {
                                paddedValue = String.valueOf(numVal);
                            
    						//2 should have been delt with on PadString()
    					}
    					
    					final int idx=numVal-1;
    					if(idx==1 && monthMod>0) {
                            monthMod -= 1;
                        }
    				}
    			}else if(nextMask.equals("tt")){
    				if(useDefaultValues && nextVal==null) {
                        paddedValue = "am";
                    }

   					isValid = (paddedValue.toLowerCase().equals("am") || paddedValue.toLowerCase().equals("pm"));
    			
    			}else if(nextMask.equals("ss")){

    				//allow for null value in Date
    				if(useDefaultValues && nextVal==null){
    					paddedValue= String.valueOf(gc.get(Calendar.SECOND));
    					// <start-demo><end-demo>
    					
    					paddedValue=padString(paddedValue,2);
    					isValid = verifyNumberInRange(paddedValue,0,59);
    				}else{
    					isValid = verifyNumberInRange(paddedValue,0,59);
    				}

    			}else if(nextMask.equals("dd") || nextMask.equals("d")){
    				isValid = verifyNumberInRange(paddedValue,0,31);
    				if(isValid) {
                        dayValue = Integer.parseInt(paddedValue);
                    }
    				
    				
    			}else if(nextMask.equals("yyyy") || nextMask.equals("yy")){

    				//get a time instance and defaults for all Date values here
    				//add this check to all values except day and month

    				//allow for null value in Date
    				if(useDefaultValues && nextVal==null){
    					nextVal= String.valueOf(gc.get(Calendar.YEAR));
    					// <start-demo><end-demo>
    					isValid = verifyNumberInRange(nextVal,0,9999);

    				}else{
    					//cannot pad year
    					if(nextMask.length()!=nextVal.length()){
    						if(nextMask.length()>nextVal.length()){
    							isValid=false;
    						}else {
    							if(nextVal.length()==4){
    								isValid = verifyNumberInRange(nextVal,0,9999);
    								nextVal = nextVal.substring(2);
    							}
    						}
    					}else{
//    						//07  becomes 2007
//    						if(nextVal.length()==2){
//    							int year=Integer.parseInt(nextVal);
//    							if(year<50)
//    								nextVal="20"+nextVal;
//    							else
//    								nextVal="19"+nextVal;
//    						}
    						//note year is not padded out
    						isValid = verifyNumberInRange(nextVal,0,9999);

    					}
    				}
    				
    				if(isValid && Integer.parseInt(nextVal)%4!=0 && monthMod>0) {
                        monthMod -= 1;
                    }
    				//stop padded value over-writing underneath
    				paddedValue=nextVal;

    			}else if(nextMask.equals("mmm") || nextMask.equals("mmmm")){

    				//this needs to handle april apr 4 and 04 -if invalid it uses default (ie May)
    				int idx = -1;
    				//if chars used instead of month number only check first 3 chars
    				if(nextVal.length()>=3) {
                        for (int i = 0; i != months.length; i++) {
                            nextVal = nextVal.toLowerCase();
                            final int length = 3;

                            nextVal = nextVal.substring(0, length).toLowerCase();
                            final String month = months[i].substring(0, length).toLowerCase();

                            if (nextVal.equals(month)) {
                                idx = i;
                            }
                        }
                    }
    				if(idx==-1){
    					try{
    						idx = Integer.parseInt(nextVal)-1;
    						if(idx<12) {
                                paddedValue = months[idx];
                            }
    					}catch(final Exception e){

                            if(LogWriter.isOutput()) {
                                LogWriter.writeLog("Exception in handling JSscript "+e);
                            }

    						paddedValue = null;
    						isValid=false;
    					}
    				}else{
    					paddedValue = months[idx];
    				}
    				if(idx!=1 && monthMod>0) {
                        monthMod -= 1;
                    }

    				//Check valid month index
    				if(idx>11) {
                        isValid = false;
                    } else {
                        monthValue = idx;
                    }
    			}else{

    				JSFunction.debug("Mask value >"+nextMask+"< not implemented");
    				isValid=false;
    			}

    			if(!isValid) {
                    break;
                }

    			//if passed, add on to result
    			finalValue.append(paddedValue);
    			if(nextSep!=null) //not on last one
                {
                    finalValue.append(nextSep);
                }


    		}
    		
    		if(monthValue<0 || monthValue>monthsCount.length || dayValue>monthsCount[monthValue]+monthMod) {
                isValid = false;
            }
    		
    		if(isValid) {
                validValue = finalValue.toString();
            }

    	}

    	return validValue;
    }

    //must line in range min-max (inclusive so range 0-24 will pass values 0 and 24 and 1 and 23)
    private static boolean verifyNumberInRange(final String nextVal, final int min, final int max) {
    	
        boolean valid=true;
        
        if(nextVal==null || isNotNumber(nextVal)){ //too long or invalid
            valid =false;
        }else{
            final int number =Integer.parseInt(nextVal);

            if(number<min || number>max) {
                valid = false;
            }
        }
        return valid;
    }

    //remove double quotes
    protected static String stripQuotes(String arg) {

        //lose quotes
        if(arg.startsWith("\"")) {
            arg = arg.substring(1, arg.length() - 1);
        }

        //allow for \\u00xx
        if(arg.startsWith("\\u")){
            String unicodeVal=arg.substring(2);
            
            //Fix for issue where unicode value ends with a space character.
            if(unicodeVal.endsWith(" ")){
                unicodeVal = unicodeVal.substring(0, unicodeVal.length()-1);
            }
            
            arg= String.valueOf((char) Integer.parseInt(unicodeVal, 16));
        }else if(arg.startsWith("\\")){ //and octal
            final String unicodeVal=arg.substring(1);
            arg= String.valueOf((char) Integer.parseInt(unicodeVal, 8));
        }

        return arg;
    }

    //check it is a number
    protected static boolean isNotNumber(final String nextVal) {
    	
        //allow for empty string
        if(nextVal.isEmpty()) {
            return true;
        }

        //assume false and disprove
        boolean notNumber =false;

        final char[] chars=nextVal.toCharArray();
        final int count=chars.length;

        //exit on first char not 0-9
        for(int ii=0;ii<count;ii++){
            if(chars[ii]=='.' || chars[ii]=='-' || chars[ii]==','){

            }else if(chars[ii]<48 || chars[ii]>57){
                ii=count;
                notNumber =true;
            }
        }
        
        return notNumber;

    }

	public String getValue() {
		return value;
	}

	public int execute(final String js, final String[] args, final int type, final int eventType,
			final char keyPressed) {
		return 0;
		
	}
	
	public String parseJSvariables(String arg) {
		final String methodToFind = "this.getField(";
		
		final int start = arg.indexOf(methodToFind);
		if(start!=-1){
			final int nameSt = start + methodToFind.length();
			int finish = arg.indexOf(')', nameSt);
			String name = arg.substring(nameSt, finish);
			if(name.startsWith("\"")){
				name = name.substring(1, name.length()-1);
			}
			
			//DEFINE the strings to search for within getfield
			final String valStr = ".value";

			if(arg.indexOf(valStr,finish+1)!=-1){
                            finish  = arg.indexOf(valStr,finish+1)+valStr.length();
			
                            final FormObject field = acro.getFormObject(name);

                            arg = arg.substring(0,start) + field.getValue() +arg.substring(finish);
                
                        }
		}
		
		//check for * / + - %
		if(!StringUtils.isNumber(arg)){
			double firstNum,secondNum;
			String nextNum;
			
			endloop://TAG so that the we can exit for loop from inside switch
			for (int i = 0; i < arg.length(); i++) {
				switch(arg.charAt(i)){
				case '*':case '/':case '+':case '-':case '%':
					firstNum = Double.parseDouble(arg.substring(0,i));
					
					nextNum = getNextNum(arg,i+1);
					secondNum = Double.parseDouble(nextNum);
					
					final double newValue;
					switch(arg.charAt(i)){
					case '*':
						newValue = (firstNum * secondNum);
						break;
					case '/':
						newValue = (firstNum / secondNum);
						break;
					case '-':
						newValue = (firstNum - secondNum);
						break;
					case '%':
						newValue = (firstNum % secondNum);
						break;
					default://'+'
						newValue = (firstNum + secondNum);
						break;
					}
					arg = newValue + arg.substring(i+1+nextNum.length());
					
					if(StringUtils.isNumber(arg)){
						break endloop;
					}
					break;
				}
			}
		}
		
		return arg;
	}

	private static String getNextNum(final String arg, final int s) {
		int f = -1;
		
		ENDLOOP://TAG to allow us to exit the for loop from inside the switch
		for (int i = s; i < arg.length(); i++) {
			switch(arg.charAt(i)){
			case '0':case '1':case '2':case '3':case '4':case '.':
			case '5':case '6':case '7':case '8':case '9':
				break;
			default:
				f = i;
				break ENDLOOP;
			}
		}
		
		if(f==-1) {
            f = arg.length();
        }
		
		return arg.substring(s,f);
	}

    /**
     */
    private static void reportError(final int code, final Object[] args) {

        final boolean errorReported=false;

        //report error
        if(!errorReported){
            if(!DecoderOptions.showErrorMessages) {
                return;
            }

            // tell user
            if (code == ErrorCodes.JSInvalidFormat) {
                JOptionPane.showMessageDialog(null, "The values entered does not match the format of the field [" + args[0] + " ]",
                        "Warning: Javascript Window", JOptionPane.INFORMATION_MESSAGE);
            } else if (code == ErrorCodes.JSInvalidDateFormat) {
                JOptionPane.showMessageDialog(null, "Invalid date/time: please ensure that the date/time exists. Field [" + args[0] + " ] should match format " + args[1],
                        "Warning: Javascript Window", JOptionPane.INFORMATION_MESSAGE);
            } else if (code == ErrorCodes.JSInvalidRangeFormat) {

                JOptionPane.showMessageDialog(null, args[1],
                        "Warning: Javascript Window",JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "The values entered does not match the format of the field",
                        "Warning: Javascript Window", JOptionPane.INFORMATION_MESSAGE);
            }

        }
    }
}
