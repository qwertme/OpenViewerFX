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
 * AFRange.java
 * ---------------
 */
package org.jpedal.objects.javascript.functions;

import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.constants.ErrorCodes;
import org.jpedal.objects.raw.FormObject;

public class AFRange extends JSFunction{

	public AFRange(final AcroRenderer acro, final FormObject formObject) {
		super(acro,formObject);
	}

	@Override
    public int execute(final String js, final String[] args, final int type, final int event, final char keyPressed) {
		
		if(args==null ){
			debug("Unknown implementation in "+js);

		}else if(args.length<1){
			debug("Values length is less than 1");
		}else{
			
			if(event==ActionHandler.FOCUS_EVENT && type==VALIDATE){

				String currentVal;

                currentVal=(String)formObject.getFormValue();
				currentVal=currentVal.trim();

				//allow empty if not already set
				if(currentVal.isEmpty()){

                    formObject.updateValue(currentVal,false, true);
                    formObject.setLastValidValue(currentVal);

					return 0;
				}
				
				final float min= Float.parseFloat(parseJSvariables(args[2]));
				final float max= Float.parseFloat(parseJSvariables(args[4]));
				
				if(isNotNumber(currentVal) ){
					currentVal=null;
				}else{
					
					//Convert currentVal into a format parseFloat can accept
					String newValue = currentVal;
					
					if(DECIMAL_IS_COMMA){
						newValue = newValue.replaceAll("\\.", "");
						newValue = newValue.replaceAll(",", "\\.");
					}else{
						newValue = newValue.replaceAll(",", "");
					}
					
					final float numVal=Float.parseFloat(newValue);
					
					//Get Range if if range values are include
					final boolean notEquals1 = Boolean.valueOf(args[1]);
					final boolean notEquals2 = Boolean.valueOf(args[3]);
					
					//Ensure this float value in within the provided range else set currentVal to null
					if(notEquals1 && numVal<min){
						currentVal=null;
					}else if(!notEquals1 && numVal<=min) {
                        currentVal = null;
                    }
					
					if(notEquals2 && numVal>max){
						currentVal=null;
					}else if(!notEquals2 && numVal>=max) {
                        currentVal = null;
                    }
				}

				if(currentVal==null){//restore and flag error
					//store arg 1 to convert back after alert call
					final String arg1tmp = args[1];
					
					final StringBuilder message=new StringBuilder("Invalid value: must be greater than ");
					if (args[1].equals("true")) {
                        message.append("or equal to ");
                    }

					message.append(min);
					message.append("\nand less than ");

					if ((args[3]).equals("true")) {
                        message.append("or equal to ");
                    }

					message.append(max);
					message.append('.');
					
					args[1] = message.toString();
					
					maskAlert(ErrorCodes.JSInvalidRangeFormat,args);
					
					//convert args[1] back to its original value before message sent to alert
					args[1] = arg1tmp;
					execute(js, args, type, event, keyPressed);
				}else{
					//Set value as the original formated value

                    formObject.updateValue(currentVal,false, true); //write back
                    formObject.setLastValidValue(currentVal);

				}
			}else {
                debug("Unknown command " + args[0] + " in " + js);
            }
		}
		
		return 0;
	}
}
