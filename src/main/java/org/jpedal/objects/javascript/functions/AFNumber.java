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
 * AFNumber.java
 * ---------------
 */
package org.jpedal.objects.javascript.functions;

import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.FormObject;


import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class AFNumber extends JSFunction{

	public AFNumber(final AcroRenderer acro, final FormObject formObject) {
		super(acro,formObject);
	}


	@Override
    public int  execute(final String js, final String[] args, final int type, final int event, final char keyPressed) {

		this.value=formObject.getObjectRefAsString();
		
		int messageCode=ActionHandler.NOMESSAGE;
		
		if(args==null ){
			debug("Unknown implementation in "+js);

		}else if(args.length<1){
			debug("Values length is less than 1");
		}else{
			boolean broken = false;
			for(int i=0;i<args.length;i++){
				if(args[i].length()<1){
					debug("Value["+i+"] length is less than 1");
					broken = true;
				}
			}
			
			if(!broken){
				//settings
				int decCount = JSFunction.getStaticDecimalCount();
				if(decCount==-1) {
                    decCount = Integer.parseInt(args[1]);
                }
				
				int gapFormat = JSFunction.getStaticGapFormat();
				if(gapFormat==-1) {
                    gapFormat = Integer.parseInt(args[2]);
                }
				
				final int minusFormat=Integer.parseInt(args[3]);
				//int currentStyle=Integer.parseInt(args[4]);
				final String currencyMask=stripQuotes(args[5]);
				final boolean hasCurrencySymbol = Boolean.valueOf(args[6]);
	
				if(gapFormat==2 || gapFormat==3){
					DECIMAL_IS_COMMA = true;
				}
	
				if(event==ActionHandler.MOUSEPRESSED){ //flag if key ignored
					
					final String  actualValue=(String) formObject.getFormValue();

					final boolean isValidForNumber=((keyPressed>='0' && keyPressed<='9') ||
							(keyPressed=='-' && actualValue.indexOf(keyPressed)==-1) || 
							(keyPressed=='.' && actualValue.indexOf(keyPressed)==-1)  && gapFormat!=2 || 
							(keyPressed==',' && actualValue.indexOf(keyPressed)==-1) && gapFormat==2);
					if(!isValidForNumber) {
                        messageCode = ActionHandler.REJECTKEY;
                    }
	
				}else{
					messageCode=validateNumber(type, event, decCount, gapFormat,minusFormat, currencyMask,hasCurrencySymbol);
				}
			}
		}
		
		if(messageCode==ActionHandler.REJECTKEY) {
            this.value = null;
        }

		return messageCode;
	}

	protected int validateNumber(final int type, final int event, final int decCount, final int gapFormat, final int minusFormat, final String currencyMask, final boolean hasCurrencySymbolAtFront) {

		final int messageCode=ActionHandler.NOMESSAGE;

		//System.out.println(gapFormat+" "+minusFormat+" "+currentStyle+" "+currencyMask);

		//current form value
		String currentVal=(String)formObject.getFormValue();

		String processedVal="";

		if(type==KEYSTROKE){
			currentVal = convertStringToNumber(currentVal,gapFormat).toString();
			
			//massage data with regexp
			if(gapFormat>1){
				if(event== ActionHandler.FOCUS_EVENT) {
                    processedVal = applyRegexp(currentVal,
                            new String[]{"[+-]?\\d+([.,]\\d+)?", "[+-]?[.,]\\d+", "[+-]?\\d+[.,]"});
                } else if(event== ActionHandler.MOUSERELEASED) {
                    processedVal = currentVal;//applyRegexp(currentVal,new String[]{"[+-]?\\d*,?\\d*"});
                }

			}else{
				if(event==ActionHandler.FOCUS_EVENT) {
                    processedVal = applyRegexp(currentVal,
                            new String[]{"[+-]?\\d+(\\.\\d+)?", "[+-]?\\.\\d+", "[+-]?\\d+\\."});
                } else if(event== ActionHandler.MOUSERELEASED) {
                    processedVal = currentVal;//applyRegexp(currentVal,new String[]{"[+-]?\\d*\\.?\\d*"});
                }
			}
			
			//if its changed its not valid
			if(!processedVal.equals(currentVal) && currentVal.indexOf('-')<=0){
				//write back

                //dont set the last valid value as were not sure if it is valid
                formObject.updateValue(formObject.getLastValidValue(),false, true);

				
			}else if(event==ActionHandler.FOCUS_EVENT){

                //If '-' is not at start remove it and continue
				if(currentVal.indexOf('-')>0) {
                    currentVal = currentVal.charAt(0) + currentVal.substring(1, currentVal.length()).replaceAll("-", "");
                }
				
				/**
				 * strip out number value or 0 for no value
				 */
				double number;

				if(currentVal!=null && currentVal.length()>-1){

					//reset if no number or validate
					if(currentVal.isEmpty()){
						currentVal="";
					}else{
						if(DECIMAL_IS_COMMA) {
                            number = Double.parseDouble(currentVal.replaceAll(",", "."));
                        } else {
                            number = Double.parseDouble(currentVal);
                        }

						final boolean isNegative=number <0;
						
						if(currentVal.charAt(0)=='-' && number == 0){
							//currentVal = currentVal.substring(1,currentVal.length());
							number = 0;
						}

						//System.err.println("minusFormat="+minusFormat+" currentStyle="+currentStyle+" number="+number+" currentVal="+currentVal);

//						if(minusFormat!=0 || hasCurrencySymbolAtFront){
						if(number<0) {
                            number = -number;
                        }

//						}
						
//						int gapFormat:<br>
//						* 0  Comma separated, period decimal point<br>
//						* 1  No separator, period decimal point<br>
//						* 2  Period separated, comma decimal point<br>
//						* 3  No separator, comma decimal point <br>
						
						final String sep;
                        final String decimal;
                        switch(gapFormat){
						default: //also case 0:
							sep=",";
							decimal=".";
							break;
						case 1:
							sep="";
							decimal=".";
							break;
						case 2:
							sep=".";
							decimal=",";
							break;
						case 3:
							sep="";
							decimal=",";
							break;
						}
						
						//setup mask
						//we dont use defined decimal and separator as this is for the mask only
						StringBuilder mask =new StringBuilder("###");
						//add the thousand separator only if we have a decimal separator
						if(!sep.isEmpty()) {
                            mask.append(',');
                        }
						mask.append("##");
						if(decCount!=0){
							//we know we have at least one decimal
							//make sure we have the number before the decimal
							mask.append("0.0");
							//and all numbers to the decimal count defined
							for (int i = 1; i < decCount; i++) {
								mask.append('0');
							}
						}else {
							mask.append('#');
						}
						
						//apply mask
						final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
						dfs.setDecimalSeparator(decimal.charAt(0));
						if(!sep.isEmpty()) {
                            dfs.setGroupingSeparator(sep.charAt(0));
                        }
						
						currentVal = new DecimalFormat(mask.toString(),dfs).format(number);

						final StringBuilder rawValue=new StringBuilder(currentVal);

						if(hasCurrencySymbolAtFront) {
                            rawValue.insert(0, currencyMask);
                        }

						if(isNegative && (minusFormat==2 || minusFormat==3)) {
                            rawValue.insert(0, '(');
                        }

							if(!hasCurrencySymbolAtFront) {
                                rawValue.append(currencyMask);
                            }

							if(isNegative && (minusFormat==2 || minusFormat==3)) {
                                rawValue.append(')');
                            }

						if(isNegative && minusFormat!=1 && minusFormat!=3) {
                            rawValue.insert(0, '-');
                        }


						currentVal=rawValue.toString();

						//add back sign for minus numbers
						//if(isNegative)
						//    currentVal="-"+currentVal;

						//set colour

						if(minusFormat==1 || minusFormat==3){
							final Color textColor;
							if(isNegative) {
                                textColor = Color.RED;
                            } else {
                                textColor = Color.BLACK;
                            }

                            if(formObject.getGUIComponent() !=null) {
                                ((Component) formObject.getGUIComponent()).setForeground(textColor);
                            }
						}
					}
					
					//write back
					formObject.updateValue(currentVal,false, true);

				}
			}
	/*	}else if(type==FORMAT){
            //current form value

			String val=(String)acro.getCompData().getValue(ref);

            //get value, format and add %
            float number=0;
            String mask="";

            if(val!=null && val.length()>0){
                StringBuffer numValu = convertStringToNumber(val);

                //reset if no number or validate
                if(numValu.length()>0)
                    number=Float.parseFloat(numValu.toString())*100;


                mask = mask + '%' + gapFormat + '.' + decCount + 'f';

                //apply mask and add % to end
                val = new PrintfFormat(mask).sprintf(number);
                
            }else
            	val="";

            //write back
            if(acro==null)
            	this.value=val;
            else
            	acro.getCompData().setValue(ref,val,true,true,false);

        }else {
        	debug("Unknown type "+args[0]+" in AFNumber.validateNumber type="+type);
*/
        }
		
		return messageCode;
	}

	static StringBuffer convertStringToNumber(final String currentVal, final int gapFormat) {
		final int charCount;
		if(currentVal==null || (charCount=currentVal.length())==0){
			return new StringBuffer(0);
		}
		
		final StringBuffer numValu=new StringBuffer();

//		int gapFormat:<br>
//		* 0  Comma separated, period decimal point<br>
//		* 1  No separator, period decimal point<br>
//		* 2  Period separated, comma decimal point<br>
//		* 3  No separator, comma decimal point <br>
		
		final char decVal;
		switch(gapFormat){
		default: //also case 0:
			decVal='.';
			break;
		case 1:
			decVal='.';
			break;
		case 2:
			decVal=',';
			break;
		case 3:
			decVal=',';
			break;
		}
		
		boolean hasDecPoint=false;
		for(int i=0;i<charCount;i++){
			final char c=currentVal.charAt(i);

			if((i==0 && c=='-')||(!hasDecPoint && (c=='.' || c==','))||(c>='0' && c<='9')){

				//track decimal point
				if(c=='.' || c==','){
					//if this is a separator, then ignore it, 
					//we only care about decimals at this point
					if(c==decVal){
						hasDecPoint=true;
						numValu.append('.');
					}
				}else {
                    numValu.append(c);
                }

			}
		}
		return numValu;
	}


}
