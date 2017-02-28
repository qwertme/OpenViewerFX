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
 * AFSpecial.java
 * ---------------
 */
package org.jpedal.objects.javascript.functions;

import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.constants.ErrorCodes;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.sun.PrintfFormat;

public class AFSpecial extends JSFunction{

    public AFSpecial(final AcroRenderer acro, final FormObject formObject) {
        super(acro,formObject);
    }

    @Override
    public int execute(final String js, final String[] args, final int type, final int event, final char keyPressed) {

        if(args==null ){
            debug("Unknown implementation in "+js);

        }else if(args.length<1){
            debug("Values length is less than 1");
        }else{

            //settings - if no value will default to special
            int specialID=-1;
            final char c=args[1].charAt(0);

            if(args[1].length()==1 && c>='0' && c<='3' ) //ignore if special as would throw exception
            {
                specialID = Integer.parseInt(args[1]);
            }

            boolean isExecuted=true; //asume true and reset if not

            //current form value
            final String currentVal;

            currentVal=(String)formObject.getFormValue();
            
            String processedVal="";

            if(type==KEYSTROKE){

                //massage data with regexp
                switch (specialID){

                    case 0:  //zip

                        if(event== ActionHandler.FOCUS_EVENT) {
                            processedVal = applyRegexp(currentVal, new String[]{"\\d{5}"});
                        } else if(event== ActionHandler.MOUSERELEASED) {
                            processedVal = applyRegexp(currentVal, new String[]{"\\d{0,5}"});
                        }

                        break;

                    case 1:  //extended zip

                        if(event== ActionHandler.FOCUS_EVENT) {
                            processedVal = applyRegexp(currentVal, new String[]{"\\d{5}(\\.|[- ])?\\d{4}"});
                        } else if(event== ActionHandler.MOUSERELEASED) {
                            processedVal = applyRegexp(currentVal, new String[]{"\\d{0,5}(\\.|[- ])?\\d{0,4}"});
                        }


                        break;

                    case 2:  //phone

                        if(event== ActionHandler.FOCUS_EVENT) {
                            processedVal = applyRegexp(currentVal,
                                    new String[]{"\\d{3}(\\.|[- ])?\\d{4}", "\\d{3}(\\.|[- ])?\\d{3}(\\.|[- ])?\\d{4}",
                                            "\\(\\d{3}\\)(\\.|[- ])?\\d{3}(\\.|[- ])?\\d{4}", "011(\\.|[- \\d])*"});
                        } else if(event== ActionHandler.MOUSERELEASED) {
                            processedVal = applyRegexp(currentVal,
                                    new String[]{"\\d{0,3}(\\.|[- ])?\\d{0,3}(\\.|[- ])?\\d{0,4}",
                                            "\\(\\d{0,3}", "\\(\\d{0,3}\\)(\\.|[- ])?\\d{0,3}(\\.|[- ])?\\d{0,4}",
                                            "\\(\\d{0,3}(\\.|[- ])?\\d{0,3}(\\.|[- ])?\\d{0,4}",
                                            "\\d{0,3}\\)(\\.|[- ])?\\d{0,3}(\\.|[- ])?\\d{0,4}", "011(\\.|[- \\d])*"});
                        }

                        break;

                    case 3:  //SSN

                        if(event== ActionHandler.FOCUS_EVENT) {
                            processedVal = applyRegexp(currentVal,
                                    new String[]{"\\d{3}(\\.|[- ])?\\d{2}(\\.|[- ])?\\d{4}"});
                        } else if(event== ActionHandler.MOUSERELEASED) {
                            processedVal = applyRegexp(currentVal,
                                    new String[]{"\\d{0,3}(\\.|[- ])?\\d{0,2}(\\.|[- ])?\\d{0,4}"});
                        }

                        break;

                    default:  //special

                        if(event== ActionHandler.FOCUS_EVENT || event== ActionHandler.MOUSERELEASED) {
                            processedVal = applyRegexp(currentVal, new String[]{args[1]});
                        }

                        break;
                }

                //if its changed its not valid
                if(event==ActionHandler.FOCUS_EVENT){

                    if(!processedVal.equals(currentVal)){
                        maskAlert(ErrorCodes.JSInvalidSpecialFormat,args);
                        execute(js, args, type, event, keyPressed);
                    }else{

                        formObject.setLastValidValue(processedVal);
                        formObject.updateValue(processedVal,false, true); //write back
                    }

                }else if(event==ActionHandler.MOUSEPRESSED || event ==ActionHandler.MOUSERELEASED){ //we do not check on keystrokes

                }else {
                    isExecuted = false;
                }

            }else if(type==FORMAT){

                /**
                 * strip out number value or 0 for no value
                 */
                final float number=0;

                String mask="";

                if(currentVal!=null && !currentVal.isEmpty()){

                    //massage data with regexp
                    switch (specialID){

                        case 0:  //zip

                            mask="99999";

                            break;

                        case 1:  //extended zip

                            mask = "99999-9999";
                            break;

                        case 2:  //phone

                            //count digits and choose if 'local' or area
                            final int digitCount=countDigits(currentVal);

                            if (digitCount >9 ) {
                                mask = "(999) 999-9999";
                            } else {
                                mask = "999-9999";
                            }

                            break;

                        case 3:  //SSN

                            mask = "999-99-9999";

                            break;

                        default:
                            isExecuted=false;
                            break;
                    }

                    //apply mask
                    if(isExecuted) {
                        processedVal = new PrintfFormat(mask).sprintf(number);
                    }

                    formObject.setLastValidValue(processedVal);
                    formObject.updateValue(processedVal,false, true);  //write back


                }
            }else {
                isExecuted = false;
            }



            if(!isExecuted) {
                debug("Unknown setting or command " + args[0] + " in " + js);
            }
        }
        
        return 0;
    }

    //count numbers in string
    private static int countDigits(final String currentVal) {

        int count=0;
        final int len=currentVal.length();
        for(int i=0;i<len;i++){
            final char c=currentVal.charAt(i);
            if(c>='0' && c<='9') {
                count++;
            }
        }

        return count;
    }

}
