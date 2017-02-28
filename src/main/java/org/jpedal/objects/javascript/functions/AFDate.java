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
 * AFDate.java
 * ---------------
 */
package org.jpedal.objects.javascript.functions;

import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.constants.ErrorCodes;
import org.jpedal.objects.raw.FormObject;

public class AFDate extends JSFunction{


    public AFDate(final AcroRenderer acro, final FormObject formObject) {
        super(acro,formObject);
    }

    @Override
    public int execute(final String js, final String[] args, final int type, final int event, final char keyPressed) {

        if(event!=ActionHandler.FOCUS_EVENT){
            JSFunction.debug("Not called on key event event="+event+" js="+js);
            return 0;
        }

        final Object[] errArgs=new Object[2];
        errArgs[0]=formObject.getObjectRefAsString();
        errArgs[1]=stripQuotes(args[1]);
        
        final String validatedValue= validateMask(args,":.,/ -",true);

        if(validatedValue!=null && !validatedValue.isEmpty()){ //with date we also test its valid date

            //of course mask is not a perfect match :-(
            //(ie MM is month, not minutes)
           // String dateMask = getJavaDateMask(args);

            //we need to bounce into date and out to string
            //and compare as Date happily excepts 32nd Feb 2006, and other invalid values
           // SimpleDateFormat testDate =new SimpleDateFormat(dateMask);

//            try {
//                Date convertedDate =testDate.parse(validatedValue);
//                System.out.println(validatedValue+" - "+testDate.format(convertedDate).toString());
//                //30th feb would become March so no match
//                if(!validatedValue.equals(testDate.format(convertedDate).toString()))
//                validatedValue=null;
//                
//            }
            
        }

        //will reset if null
        if(validatedValue==null){
            maskAlert(ErrorCodes.JSInvalidDateFormat,errArgs);//chris unformat
            execute(js, args, type, event, keyPressed);
        }else{

            formObject.setLastValidValue(validatedValue);
            formObject.updateValue(validatedValue,false, true);

        }
        
        return 0;
    }

//    private String getJavaDateMask(String[] args) {
//        String dateMask=stripQuotes(args[1]);
//        StringBuffer mappedMask=new StringBuffer();
//        StringTokenizer dateValue=new StringTokenizer(dateMask,".");
//        while(dateValue.hasMoreTokens()){
//
//            String val=dateValue.nextToken();
//
//            if(val.equals("mm"))
//            val="MM";
//
//            if(mappedMask.length()>0)
//            mappedMask.append('.');
//
//            mappedMask.append(val);
//
//        }
//
//        dateMask=mappedMask.toString();
//        return dateMask;
//    }
}
