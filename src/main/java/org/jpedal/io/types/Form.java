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
 * Form.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.StringUtils;

/**
 *
 */
public class Form {

    
    public static void setFieldNames(final PdfObject pdfObject, final PdfFileReader objectReader) {
        
        String fieldName =pdfObject.getTextStreamValue(PdfDictionary.T);
        
        if(fieldName!=null ){
            
            //at this point newString is the raw byte value (99% of the time this is the
            //string but it can be encode in some other ways (like a set of hex values)
            //so we need to use PdfObjectReader.getTextString(newString, false) rather than new String(newString)
            //6 0 obj <</T <FEFF0066006F0072006D0031005B0030005D>
            //
            //Most of the time you can forget about this because getTextStream() handles it for you
            //
            //Except here where we are manipulating the bytes directly...
            String parent = pdfObject.getStringKey(PdfDictionary.Parent);
            
            // if no name, or parent has one recursively scan tree for one in Parent
            boolean isMultiple=false;
            
            while (parent != null) {
                
                final FormObject parentObj =new FormObject(parent,false);
                objectReader.readObject(parentObj);
                
                final String newName = parentObj.getTextStreamValue(PdfDictionary.T);
                if (newName != null){
                    //we pass in kids data so stop name.name
                    if(!fieldName.equals(newName) || !parent.equals(pdfObject.getObjectRefAsString())) {
                        fieldName = newName + '.' + fieldName;
                        isMultiple=true;
                    }
                }else{
                    break;
                }
                
                parent = parentObj.getParentRef();
            }
            
            //set the field name to be the Fully Qualified Name
            if(isMultiple) {
                pdfObject.setTextStreamValue(PdfDictionary.T, StringUtils.toBytes(fieldName));
            }
        }
    }
    
}


