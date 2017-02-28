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
 * AFSimple.java
 * ---------------
 */
package org.jpedal.objects.javascript.functions;

import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.javascript.defaultactions.JpedalDefaultJavascript;
import org.jpedal.objects.raw.FormObject;

public class AFSimple extends JSFunction{
    

    public AFSimple(final AcroRenderer acro, final FormObject formObject) {
        super(acro,formObject);
    }

    @Override
    public int execute(final String js, final String[] args, final int type, final int eventType, final char keyPressed) {
    	
        if(args==null ){
            debug("Unknown implementation in "+js);

        }else if(args.length<1){
            debug("Values length is less than 1");
        }else{

            if(type==JSFunction.CALCULATE){
            	//check if our calculate flag is true or not
            	if(JpedalDefaultJavascript.calculate>0){
	            	
	                String result="";
	
	                int currentItem=1;
	
	                //get first value which is command
	                String nextValue=args[currentItem];
	
	                final int objType=convertToValue(nextValue);
	
	                if(objType!=-1){
	
	                    currentItem++;
	                    nextValue=args[currentItem];
	
	                    final String rest="";
	                    if(nextValue.startsWith("new Array")){
	
	                        result = processArray(nextValue,objType);
	
	                    }else {
                            debug("Unknown params " + rest + " in " + js);
                        }
	
	                }else {
                        debug("Unknown command " + nextValue + " in " + js);
                    }
	
	
	                //write back
                    formObject.updateValue(result,false, true);
                    formObject.setLastValidValue(result);

            	}
            }else {
                debug("Unknown command " + args[0] + " in " + js);
            }
        }
        
        return 0;
    }

    /**
     * get string name as int value if recognised
     * @param rawValue
     * @return
     */
    private static int convertToValue(final String rawValue) {

        int value=-1; //default no match

        if(rawValue.equals("\"SUM\"")) {
            value = SUM;
        } else if(rawValue.equals("\"AVG\"")) {
            value = AVG;
        } else if(rawValue.equals("\"PRD\"")) {
            value = PRD;
        } else if(rawValue.equals("\"MIN\"")) {
            value = MIN;
        } else if(rawValue.equals("\"MAX\"")) {
            value = MAX;
        }


        return value;
    }

}
