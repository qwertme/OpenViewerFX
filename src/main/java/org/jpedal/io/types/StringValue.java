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
 * StringValue.java
 * ---------------
 */
package org.jpedal.io.types;

import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 *
 */
public class StringValue {
    
    
    public static int setStringConstantValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt) {
        
        i++;
        
        //move cursor to start of text
        while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47) {
            i++;
        }
        
        final int keyStart=i;
        int keyLength=0;
        
        //move cursor to end of text
        while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
            i++;
            keyLength++;
        }
        
        i--;// move back so loop works
        
        //store value
        pdfObject.setConstant(PDFkeyInt,keyStart,keyLength,raw);
        
        if(debugFastCode) {
            System.out.println(padding + "Set constant in " + pdfObject + " to " + pdfObject.setConstant(PDFkeyInt, keyStart, keyLength, raw));
        }
        
        return i;
    }
    

    
    public static int setStringKeyValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt) {
        
        i++;
        
        //move cursor to start of text
        while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47) {
            i++;
        }
        
        final int keyStart=i;
        int keyLength=1;
        
        boolean isNull=false;
        
        //move cursor to end of text (allow for null)
        while(raw[i]!='R' && !isNull){
            
            //allow for null for Parent
            if(PDFkeyInt== PdfDictionary.Parent && raw[i]=='n' && raw[i+1]=='u' && raw[i+2]=='l' && raw[i+3]=='l') {
                isNull = true;
            }
            
            i++;
            keyLength++;
        }
        
        i--;// move back so loop works
        
        if(!isNull){
            
            //set value
            final byte[] stringBytes=new byte[keyLength];
            System.arraycopy(raw,keyStart,stringBytes,0,keyLength);
            
            //store value
            pdfObject.setStringKey(PDFkeyInt,stringBytes);
            
            
            if(debugFastCode) {
                System.out.println(padding + "Set constant in " + pdfObject + " to " + new String(stringBytes));
            }
        }
        return i;
    }
    
}


