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
 * TextStream.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.DecryptionFactory;
import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;

/**
 *
 */
public class TextStream {
    
    public static int readTextStream(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final boolean ignoreRecursion,
            final PdfFileReader objectReader) {
        
        
        if(PDFkeyInt==PdfDictionary.W || PDFkeyInt==PdfDictionary.W2) {
            return readCIDWidths(pdfObject, i, raw, PDFkeyInt, ignoreRecursion, objectReader);
        }else{
            
            byte[] data;
            try{
                if(raw[i]!='<' && raw[i]!='(') {
                    i++;
                }

                i=ArrayUtils.skipSpaces(raw,i);

                //allow for no actual value but another key
                if(raw[i]==47){
                    pdfObject.setTextStreamValue(PDFkeyInt, new byte[1]);
                    i--;
                    return i;
                }

                //get next key to see if indirect
                final boolean isRef=raw[i]!='<' && raw[i]!='(';
                
                int j=i;
                data=raw;
                if(isRef){
                    
                    //number
                    int keyStart2=i;
                    while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
                        
                        i++;
                    }
                    
                    final int number= NumberUtils.parseInt(keyStart2, i, raw);
                    
                    //generation
                    while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60) {
                        i++;
                    }
                    
                    keyStart2=i;
                    //move cursor to end of reference
                    while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62) {
                        i++;
                    }
                    final int generation= NumberUtils.parseInt(keyStart2, i, raw);
                    
                    //move cursor to start of R
                    while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60) {
                        i++;
                    }
                    
                    if(raw[i]!=82) //we are expecting R to end ref
                    {
                        return raw.length;
                    }
                    //throw new RuntimeException(i+" 3. Unexpected value in file " + (char) raw[i - 2]+ (char) raw[i-1] + (char) raw[i] + (char) raw[i+1] + (char) raw[i+2]+(char)raw[i]+" - please send to IDRsolutions for analysis "+pdfObject.getObjectRefAsString()+" "+pdfObject);
                    
                    if(!ignoreRecursion){
                        
                        //read the Dictionary data
                        data=objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(number, generation), number, generation);
                        
                        //							System.out.println("data read is>>>>>>>>>>>>>>>>>>>\n");
                        //							for(int ab=0;ab<data.length;ab++)
                        //							System.out.print((char)data[ab]);
                        //							System.out.println("\n<<<<<<<<<<<<<<<<<<<\n");
                        
                        
                        //allow for data in Linear object not yet loaded
                        if(data==null){
                            pdfObject.setFullyResolved(false);
                            
                            if(debugFastCode) {
                                System.out.println(padding + "Data not yet loaded");
                            }
                            
                            if(LogWriter.isOutput()) {
                                LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (7)");
                            }
                            
                            return raw.length;
                        }
                        
                        //lose obj at start
                        if(data[0]=='('){
                            j=0;
                        }else{
                            j=3;
                            while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111) {
                                j++;
                            }

                            j=ArrayUtils.skipSpaces(data,j);

                        }
                    }
                }
                /////////////////
                int start=0;
                if(!isRef || !ignoreRecursion){
                    //move to start
                    while(data[j]!='(' && data[j]!='<'){
                        j++;
                        
                    }
                    
                    final byte startChar=data[j];
                    
                    start=j;
                    
                    //move to end (allow for ((text in brackets))
                    int bracketCount=1;
                    while(j<data.length){
                        //System.out.println(i+"="+raw[j]+" "+(char)raw[j]);
                        j++;
                        
                        if(startChar=='(' && (data[j]==')' || data[j]=='(') && !ObjectUtils.isEscaped(data, j)){
                            //allow for non-escaped brackets
                            if(data[j]=='(') {
                                bracketCount++;
                            } else if(data[j]==')') {
                                bracketCount--;
                            }
                            
                            if(bracketCount==0) {
                                break;
                            }
                        }
                        
                        if(startChar=='<' && (data[j]=='>' || data[j]==0)) {
                            break;
                        }
                    }
                    
                }
                
                if(!ignoreRecursion){
                    
                    byte[] newString;
                    
                    if(data[start]=='<'){
                        start++;
                        
                        final int byteCount=(j-start)>>1;
                        newString=new byte[byteCount];
                        
                        int byteReached=0,topHex,bottomHex;
                        while(true){
                            
                            if(start==j) {
                                break;
                            }

                            start=ArrayUtils.skipSpaces(data,start);

                            topHex=data[start];
                            
                            //convert to number
                            if(topHex>='A' && topHex<='F'){
                                topHex -= 55;
                            }else if(topHex>='a' && topHex<='f'){
                                topHex -= 87;
                            }else if(topHex>='0' && topHex<='9'){
                                topHex -= 48;
                            }
                            
                            start++;

                            start=ArrayUtils.skipSpaces(data,start);

                            bottomHex=data[start];
                            
                            if(bottomHex>='A' && bottomHex<='F'){
                                bottomHex -= 55;
                            }else if(bottomHex>='a' && bottomHex<='f'){
                                bottomHex -= 87;
                            }else if(bottomHex>='0' && bottomHex<='9'){
                                bottomHex -= 48;
                            }else{
                                
                                if(LogWriter.isOutput()) {
                                    LogWriter.writeLog("Unexpected number " + (char) data[start]);
                                }
                                
                                return i;
                            }
                            
                            start++;
                            
                            //calc total
                            final int finalValue=bottomHex+(topHex<<4);
                            
                            newString[byteReached] = (byte)finalValue;
                            
                            byteReached++;
                            
                        }
                        
                        
                        
                    }else{
                        //roll past (
                        if(data[start]=='(') {
                            start++;
                        }
                        
                        boolean lbKeepReturns = false;
                        switch ( PDFkeyInt ) {
                            case PdfDictionary.ID:
                                lbKeepReturns = true;
                                break;
                            case PdfDictionary.O:
                            case PdfDictionary.U:
                                // O and U in Encrypt may contain line breaks as valid password chars ...
                                lbKeepReturns = pdfObject.getObjectType() == PdfDictionary.Encrypt;
                                break;
                        }
                        
                        newString = ObjectUtils.readEscapedValue(j,data, start,lbKeepReturns);
                    }
                    
                    if(pdfObject.getObjectType()!= PdfDictionary.Encrypt){// && pdfObject.getObjectType()!=PdfDictionary.Outlines){
                        
                        try {
                            if(!pdfObject.isInCompressedStream() || PDFkeyInt==PdfDictionary.Name || PDFkeyInt==PdfDictionary.Reason || PDFkeyInt==PdfDictionary.Location || PDFkeyInt==PdfDictionary.M){
                                final DecryptionFactory decryption=objectReader.getDecryptionObject();
                            
                                if(decryption!=null) {
                                    newString = decryption.decryptString(newString, pdfObject.getObjectRefAsString());
                                }
                            }
                        } catch (final PdfSecurityException e) {
                            //tell user and log
                            if(LogWriter.isOutput()) {
                                LogWriter.writeLog("Exception: " + e.getMessage());
                            }
                            //
                        }
                    }
                    
                    pdfObject.setTextStreamValue(PDFkeyInt, newString);
                    
                    if(debugFastCode) {
                        System.out.println(padding + "TextStream=" + new String(newString) + " in pdfObject=" + pdfObject);
                    }
                }
                
                if(!isRef) {
                    i = j;
                }
                
            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
        }
        return i;
    }

    private static int readCIDWidths(PdfObject pdfObject, int i, byte[] raw, int PDFkeyInt, boolean ignoreRecursion, PdfFileReader objectReader) {

        //we need to roll on as W2 is 2 chars and W is 1
        if(PDFkeyInt== PdfDictionary.W2) {
            i++;
        }

        boolean isRef=false;

        if(debugFastCode) {
            System.out.println(padding + "Reading W or W2");
        }

        //move to start
        while(raw[i]!='[' ){ //can be number as well

            //System.out.println((char) raw[i]);
            if(raw[i]=='('){ //allow for W (7)
                isRef=false;
                break;
            }

            //allow for number as in refer 9 0 R
            if(raw[i]>='0' && raw[i]<='9'){
                isRef=true;
                break;
            }

            i++;
        }

        //allow for direct or indirect
        byte[] data=raw;

        int start=i,j=i;

        int count=0;

        //read ref data and slot in
        if(isRef){
            //number
            int keyStart2=i;
            while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){

                i++;

            }
            final int number= NumberUtils.parseInt(keyStart2, i, raw);

            //generation
            while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60) {
                i++;
            }

            keyStart2=i;
            //move cursor to end of reference
            while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62) {
                i++;
            }
            final int generation= NumberUtils.parseInt(keyStart2, i, raw);

            //move cursor to start of R
            while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60) {
                i++;
            }

            if(raw[i]!=82) //we are expecting R to end ref
            {
                throw new RuntimeException("3. Unexpected value in file " + raw[i] + " - please send to IDRsolutions for analysis");
            }

            if(!ignoreRecursion){

                //read the Dictionary data
                data=objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(number, generation), number, generation);

                //allow for data in Linear object not yet loaded
                if(data==null){
                    pdfObject.setFullyResolved(false);

                    if(debugFastCode) {
                        System.out.println(padding + "Data not yet loaded");
                    }

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (6)");
                    }

                    return raw.length;
                }

                //lose obj at start
                j = 3;

                if (data.length < 3) { //allow for empty string line []
                    j = 0;
                } else {
                    while (data[j - 1] != 106 && data[j - 2] != 98 && data[j - 3] != 111) {
                        j++;

                        //catch for error
                        if (j == data.length) {
                            j = 0;
                            break;
                        }
                    }
                }


                //skip any spaces after
                while(data[j]==10 || data[j]==13 || data[j]==32)// || data[j]==47 || data[j]==60)
                {
                    j++;
                }

                //reset pointer
                start=j;

            }
        }

        //move to end
        while(j<data.length){

            if(data[j]=='[' || data[j]=='(') {
                count++;
            } else if(data[j]==']' || data[j]==')') {
                count--;
            }

            if(count==0) {
                break;
            }

            j++;
        }

        if(!ignoreRecursion){
            final int stringLength=j-start+1;
            final byte[] newString=new byte[stringLength];

            System.arraycopy(data, start, newString, 0, stringLength);

            /**
             * clean up so matches old string so old code works
             */
            if(PDFkeyInt!=PdfDictionary.JS){ //keep returns in code
                for(int aa=0;aa<stringLength;aa++){
                    if(newString[aa]==10 || newString[aa]==13) {
                        newString[aa] = 32;
                    }
                }
            }

            pdfObject.setTextStreamValue(PDFkeyInt, newString);

            if(debugFastCode){
                if(PDFkeyInt==39) {
                    System.out.println(padding + pdfObject + " W=" + new String(newString));
                } else {
                    System.out.println(padding + pdfObject + " W2=" + new String(newString));
                }
            }
        }

        //roll on
        if(!isRef) {
            i = j;
        }

        return i;
    }


    public static int setTextStreamValue(final PdfObject pdfObject, int i, final byte[] raw, final boolean ignoreRecursion, final int PDFkeyInt, final PdfFileReader objectReader) {
        
        if(raw[i+1]==40 && raw[i+2]==41){ //allow for empty stream
            i += 3;
            pdfObject.setTextStreamValue(PDFkeyInt, new byte[1]);
            
            if(raw[i]=='/') {
                i--;
            }
        }else {
            i = TextStream.readTextStream(pdfObject, i, raw, PDFkeyInt, ignoreRecursion, objectReader);
        }
        
        return i;
    }
    
    
}


