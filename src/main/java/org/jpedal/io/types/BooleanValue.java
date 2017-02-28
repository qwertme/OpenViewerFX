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
 * BooleanValue.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.objects.raw.PdfObject;

/**
 *
 */
public class BooleanValue {

    
    public static int set(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt) {
        
        final int keyStart;
        
        i++;
        
        //move cursor to start of text
        while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47){
            //System.out.println("skip="+raw[i]);
            i++;
        }
        
        keyStart=i;
        
        //move cursor to end of text
        while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
            //System.out.println("key="+raw[i]+" "+(char)raw[i]);
            i++;
        }
        
        i--;// move back so loop works
        
        //store value
        if(raw[keyStart]=='t' && raw[keyStart+1]=='r' && raw[keyStart+2]=='u' && raw[keyStart+3]=='e') {
            pdfObject.setBoolean(PDFkeyInt,true);
            
        }else if(raw[keyStart]=='f' && raw[keyStart+1]=='a' && raw[keyStart+2]=='l' && raw[keyStart+3]=='s' && raw[keyStart+4]=='e'){
            pdfObject.setBoolean(PDFkeyInt,false);
            
        }else {
            throw new RuntimeException("Unexpected value for Boolean value for" + PDFkeyInt);
        }
        
        return i;
    }
    
}


