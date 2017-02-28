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
 * AFTime.java
 * ---------------
 */
package org.jpedal.objects.javascript.functions;

import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.constants.ErrorCodes;
import org.jpedal.objects.raw.FormObject;

public class AFTime extends JSFunction {
    
    public AFTime(final AcroRenderer acro, final FormObject formObject) {

        super(acro,formObject);
    }

    @Override
    public int execute(final String js, final String[] args, final int type, final int event, final char keyPressed) {

        if(event== ActionHandler.FOCUS_EVENT && (type==KEYSTROKE || type==FORMAT)){
        	//format added for file baseline_screens\forms\Testdokument PDF.pdf
        	//F action found this AFTime_FormatEx("HH:MM") unknown command
//        	if(type==FORMAT){
//        		System.out.println("AFTime.execute focus format js="+js);
//	        	org.jpedal.objects.acroforms.utils.ConvertToString.printStackTrace(7);

//        	}
            final String validatedValue= validateMask(args,":",false);

            if(validatedValue==null){

                final Object[] errArgs=new Object[1];
                errArgs[0]=formObject.getObjectRefAsString();

                maskAlert(ErrorCodes.JSInvalidFormat,errArgs);//chris unformat
                execute(js, args, type, event, keyPressed);
            }else{
				//be sure to get the current value before we change it

                formObject.setLastValidValue(validatedValue);
                formObject.updateValue(validatedValue, false, true);

            }

        }else if(type==KEYSTROKE){ //just ignore and process on focus lost
            JSFunction.debug("AFTime(keystroke)="+js);
        }else if(type==FORMAT){
        	JSFunction.debug("AFTime(format)="+js);
        }else {
            JSFunction.debug("Unknown command "+js);
        }

        return 0;
    }
}
