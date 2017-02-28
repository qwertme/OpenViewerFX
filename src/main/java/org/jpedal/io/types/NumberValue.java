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
 * NumberValue.java
 * ---------------
 */
package org.jpedal.io.types;

import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;

/**
 *
 */
public class NumberValue {
    
    
    
    public static int setNumberValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {
        
        int keyStart=i;
        final int rawLength=raw.length;

        //move cursor to end of text
        while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62 && raw[i]!='(' && raw[i]!='.'){
            i++;
        }
        
        //actual value or first part of ref
        int number= NumberUtils.parseInt(keyStart, i, raw);
        
        
        //roll onto next nonspace char and see if number
        int jj=i;
        while(jj<rawLength &&(raw[jj]==32 || raw[jj]==13 || raw[jj]==10)) {
            jj++;
        }
        
        boolean  isRef=false;
        
        //check its not a ref (assumes it XX 0 R)
        if(raw[jj]>= 48 && raw[jj]<=57){ //if next char is number 0-9 it may be a ref
            
            int aa=jj;
            
            //move cursor to end of number
            while((raw[aa]!=10 && raw[aa]!=13 && raw[aa]!=32 && raw[aa]!=47 && raw[aa]!=60 && raw[aa]!=62)) {
                aa++;
            }
            
            //move cursor to start of text
            while(aa<rawLength && (raw[aa]==10 || raw[aa]==13 || raw[aa]==32 || raw[aa]==47)) {
                aa++;
            }
            
            isRef=aa<rawLength && raw[aa]=='R';
            
        }
        
        if(isRef){
            //move cursor to start of generation
            while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60) {
                i++;
            }
            
            /**
             * get generation number
             */
            keyStart=i;
            //move cursor to end of reference
            while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62) {
                i++;
            }
            
            final int generation= NumberUtils.parseInt(keyStart, i, raw);
            
            //move cursor to start of R
            while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60) {
                i++;
            }
            
            if(raw[i]!=82){ //we are expecting R to end ref
                //
                throw new RuntimeException("3. Unexpected value in file - please send to IDRsolutions for analysis");
            }
            
            //read the Dictionary data
            final byte[] data=objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(number, generation), number, generation);
            
            //allow for data in Linear object not yet loaded
            if(data==null){
                pdfObject.setFullyResolved(false);
                
                if(debugFastCode) {
                    System.out.println(padding + "Data not yet loaded");
                }
                
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (8)");
                }
                
                return rawLength;
            }
            
            //lose obj at start
            int j=0;
            final int len=data.length;

            //allow for example where start <<
            if(len>1 && data[0]=='<' && data[1]=='<'){
            }else if(len<=3){ //fix for short indirect value /K 30 0 R where 30 0R is -1 ie (11dec/Real Estate Tax Bill 2011.pdf)
            }else{
                j=3;
                if(len>3){ //allow for small values (ie Rotate object pointing to value 0)
                    while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111){
                        j++;
                        
                        if(j==len){
                            j=0;
                            break;
                        }
                    }
                }
            }
            
            //skip any spaces after
            if(len>1){//allow for small values (ie Rotate object pointing to value 0)
                while( j<data.length &&(data[j]==9 || data[j]==10 || data[j]==13 || data[j]==32))// || data[j]==47 || data[j]==60)
                {
                    j++;
                }
            }
            
            int count=j;
            
            //skip any spaces at end
            while(count<len && data[count]!=9 && data[count]!=10 && data[count]!=13 && data[count]!=32)// || data[j]==47 || data[j]==60)
            {
                count++;
            }
            
            number= NumberUtils.parseInt(j, count, data);
            
        }
        
        //store value
        pdfObject.setIntNumber(PDFkeyInt,number);
        
        if(debugFastCode) {
            System.out.println(padding + "set numberValue=" + number);//+" in "+pdfObject);
        }
        
        i--;// move back so loop works
        
        return i;
    }
    

}


