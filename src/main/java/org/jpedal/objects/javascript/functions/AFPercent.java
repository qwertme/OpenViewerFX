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
 * AFPercent.java
 * ---------------
 */
package org.jpedal.objects.javascript.functions;

import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.sun.PrintfFormat;

public class AFPercent extends AFNumber{

    public AFPercent(final AcroRenderer acro, final FormObject formObject) {
        super(acro,formObject);
    }


    @Override
    public int execute(final String js, final String[] args, final int type, final int event, final char keyPressed) {

        int messageCode= ActionHandler.NOMESSAGE;

        if(args==null ){
            debug("Unknown implementation in "+js);

        }else if(args.length<1){
            debug("Values length is less than 1");
        }else{

            //settings
            int decCount = JSFunction.getStaticDecimalCount();
			if(decCount==-1) {
                decCount = Integer.parseInt(args[1]);
            }
			
			int gapFormat = JSFunction.getStaticGapFormat();
			if(gapFormat==-1) {
                gapFormat = Integer.parseInt(args[2]);
            }

            if(type==KEYSTROKE){

                messageCode=validateNumber( type, event, decCount, gapFormat, 0,"",true);

            }else if(type==FORMAT){

                //current form value
                String currentVal;

                currentVal=(String)formObject.getFormValue();

                /**
                 * get value, format and add %
                 */
                float number=0;
                String mask="";

                if(currentVal!=null && !currentVal.isEmpty()){
                    final StringBuffer numValu = convertStringToNumber(currentVal,gapFormat);

                    //reset if no number or validate
                    if(numValu.length()>0) {
                        number = Float.parseFloat(numValu.toString()) * 100;
                    }


                    mask = mask + '%' + gapFormat + '.' + decCount + 'f';

                    //apply mask and add % to end
                    currentVal = new PrintfFormat(mask).sprintf(number)+ '%';
                    
                }else {
                    currentVal = "";
                }

                //write back
                formObject.setLastValidValue(currentVal);
                formObject.updateValue(currentVal,false, true);


            }else {
                debug("Unknown type " + args[0] + " in " + js);
            }
        }

        return messageCode;
    }
}
