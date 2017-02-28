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
 * ObjectUtils.java
 * ---------------
 */
package org.jpedal.io;

import org.jpedal.io.types.ArrayUtils;
import org.jpedal.objects.raw.DecodeParmsObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 * general static methods
 */
public class ObjectUtils {

    public static byte[] checkEndObject(byte[] array) {

        long objStart=-1,lastEndStream=-1;
        int ObjStartCount = 0;

        //check if mising endobj
        for (int i = 0; i < array.length - 8; i++) {

            //track endstream and first or second obj
            if ((ObjStartCount < 2) && (array[i] == 32) && (array[i + 1] == 111) &&
                    (array[i + 2] == 98) && (array[i + 3] == 106)) {
                ObjStartCount++;
                objStart = i;
            }
            if ((ObjStartCount < 2) && (array[i] == 101) && (array[i + 1] == 110) &&
                    (array[i + 2] == 100) && (array[i + 3] == 115) &&
                    (array[i + 4] == 116) && (array[i + 5] == 114) &&
                    (array[i + 6] == 101) && (array[i + 7] == 97) && (array[i + 8] == 109)) {
                lastEndStream = i + 9;
            }
        }

        if ((lastEndStream > 0) && (objStart > lastEndStream)) {
            final byte[] newArray = new byte[(int) lastEndStream];
            System.arraycopy(array, 0, newArray, 0, (int) lastEndStream);
            array = newArray;
        }
        return array;
    }

    public static byte[] readEscapedValue(final int j, final byte[] data, final int start, final boolean keepReturns) {
        byte[] newString;
        //see if escape values
        boolean escapedValues=false;
        for(int aa=start;aa<j;aa++){

            if(data[aa]=='\\' || data[aa]==10 || data[aa]==13){ //convert escaped chars
                escapedValues=true;
                aa=j;
            }
        }

        if(!escapedValues){ //no escapes so fastest copy
            final int stringLength=j-start;

            if(stringLength<1) {
                return new byte[0];
            }

            newString=new byte[stringLength];

            System.arraycopy(data, start, newString, 0, stringLength);
        }else{ //translate escaped chars on copy

            int jj=0; //max length
            final int stringLength=j-start;
            newString=new byte[stringLength];

            for(int aa=start;aa<j;aa++){

                if(data[aa]=='\\'){ //convert escaped chars

                    aa++;

                    //allow for bum data in unicode stream
                    if(data[aa]==13  && data[aa+1]!='\\'  && newString[0]==-2 && newString[1]==-1){
                        aa++;
                    }

                    final byte nextByte=data[aa];
                    if(nextByte=='b') {
                        newString[jj] = '\b';
                    } else if(nextByte=='n') {
                        newString[jj] = '\n';
                    } else if(nextByte=='t') {
                        newString[jj] = '\t';
                    } else if(nextByte=='r') {
                        newString[jj] = '\r';
                    } else if(nextByte=='f') {
                        newString[jj] = '\f';
                    } else if(nextByte=='\\') {
                        newString[jj] = '\\';
                    } else if(nextByte>47 && nextByte<58){ //octal

                        final StringBuilder octal=new StringBuilder(3);

                        boolean notOctal=false;
                        for(int ii=0;ii<3;ii++){

                            if(data[aa]=='\\' || data[aa]==')' || data[aa]<'0' || data[aa]>'9') //allow for less than 3 values
                            {
                                ii = 3;
                            } else{
                                octal.append((char)data[aa]);

                                //catch for odd values
                                if(data[aa]>'7') {
                                    notOctal = true;
                                }

                                aa++;
                            }
                        }
                        //move back 1
                        aa--;
                        //isOctal=true;
                        if(notOctal) {
                            newString[jj] = (byte) Integer.parseInt(octal.toString());
                        } else {
                            newString[jj] = (byte) Integer.parseInt(octal.toString(), 8);
                        }

                    }else if(!keepReturns && (nextByte==13 || nextByte==10)){ //ignore bum data
                        jj--;
                    }else {
                        newString[jj] = nextByte;
                    }

                    jj++;
                }else if(!keepReturns && (data[aa]==13 || data[aa]==10)){ //convert returns to spaces
                    newString[jj]=32;
                    jj++;
                }else{
                    newString[jj]=data[aa];
                    jj++;
                }
            }

            //now resize
            final byte[] rawString=newString;
            newString=new byte[jj];

            System.arraycopy(rawString, 0, newString, 0, jj);
        }
        return newString;
    }


    /**
     * ensure all objects resolved
     * @param pdfObject
     * @param objReader
     * @return
     */
    public static PdfObject[] setupDecodeParms(final PdfObject pdfObject, final PdfFileReader objReader) {

        final PdfObject[] decodeParmsValues;
        final Object[] DecodeParmsArray = pdfObject.getObjectArray(PdfDictionary.DecodeParms);

        //if specific DecodeParms for each filter, set othereise use global
        if(DecodeParmsArray!=null){

            final int count=DecodeParmsArray.length;
            decodeParmsValues=new PdfObject[count];
            for(int i=0;i<count;i++){

                final byte[] decodeByteData= (byte[]) DecodeParmsArray[i];

                if(decodeByteData!=null){
                    final String key= new String(decodeByteData);

                    final PdfObject DecodeParms=new DecodeParmsObject(key);

                    if(decodeByteData[0]=='<') {
                        DecodeParms.setStatus(PdfObject.UNDECODED_DIRECT);
                    } else {
                        DecodeParms.setStatus(PdfObject.UNDECODED_REF);
                    }

                    //must be done AFTER setStatus()
                    DecodeParms.setUnresolvedData(decodeByteData, PdfDictionary.DecodeParms);
                    final ObjectDecoder objDecoder=new ObjectDecoder(objReader);
                    objDecoder.checkResolved(DecodeParms);

                    decodeParmsValues[i]=DecodeParms;
                }else {
                    decodeParmsValues[i] = null;
                }
            }

        }else{
            decodeParmsValues=new PdfObject[1];
            decodeParmsValues[0]= pdfObject.getDictionary(PdfDictionary.DecodeParms);
        }
        return decodeParmsValues;
    }

    //replace sequence 13 10 with 32
    public static byte[] convertReturnsToSpaces(byte[] newValues) {

        if(newValues==null) {
            return null;
        }

        //see if needed
        int returnCount=0;
        final int len=newValues.length;
        for(int aa=0;aa<len;aa++){
            if(newValues[aa]==13 && newValues[aa+1]==10){
                aa++;
                returnCount++;
            }
        }

        //swap out if needed
        if(returnCount>0){

            final int newLen=len-returnCount;
            int jj=0;
            final byte[] oldValue=newValues;
            newValues=new byte[newLen];

            for(int aa=0;aa<len;aa++){

                if(oldValue[aa]==13 && aa<len-1 && oldValue[aa+1]==10){
                    newValues[jj]=32;
                    aa++;
                }else {
                    newValues[jj] = oldValue[aa];
                }

                jj++;
            }
        }

        return newValues;

    }

    static int handleUnknownType(int i, final byte[] raw, final int length) {
        final int count=length-1;

        for(int jj=i;jj<count;jj++){

            if(raw[jj]=='R' && raw[jj-2]=='0'){
                i=jj;
                jj=count;
            }else if(raw[jj]=='<' && raw[jj+1]=='<'){

                int levels=0;
                while(true){

                    if(raw[jj]=='<' && raw[jj+1]=='<') {
                        levels++;
                    } else if(raw[jj]=='>' && raw[jj+1]=='>'){
                        levels--;

                        //allow for >>>>
                        if(raw[jj+2]=='>') {
                            jj++;
                        }
                    }

                    jj++;
                    if(levels==0 || jj>=count) {
                        break;
                    }
                }

                i=jj;

                jj=count;

            }else if(raw[jj]=='/'){
                jj=count;
            }else if(raw[jj]=='>' && raw[jj+1]=='>'){
                i=jj-1;
                jj=count;
            }else if(raw[jj]=='('){

                while(jj<count && (raw[jj]!=')' || isEscaped(raw, jj))){
                    jj++;
                }

                i=jj;
                jj=count;
            }
        }
        return i;
    }

    //count backwards to get total number of escape chars
    public static boolean isEscaped(final byte[] raw, final int i) {
        int j=i-1, escapedFound=0;
        while(j>-1 && raw[j]=='\\'){
            j--;
            escapedFound++;
        }


        //System.out.println("escapedFound="+escapedFound+" "+(escapedFound & 2 ));
        return (escapedFound & 1) == 1;

    }

    public static int setDirectValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt) {

        final int keyStart;
        i++;
        keyStart = i;
        while (i < raw.length && raw[i] != 32 && raw[i] != 10 && raw[i] != 13) {
            i++;
        }

        //store value
        pdfObject.setConstant(PDFkeyInt, keyStart, i - keyStart, raw);
        return i;
    }

    /**
     * used to debug object reading code
     * @param pdfObject
     * @param i
     * @param length
     * @param raw
     * @param padding
     */
    static void showData(final PdfObject pdfObject, final int i, final int length, final byte[] raw, final String padding) {

        System.out.println("\n\n"+ padding +" ------------readDictionaryAsObject ref="+ pdfObject.getObjectRefAsString() +" into "+pdfObject+"-----------------\ni="+i+"\nData=>>>>");
        System.out.print(padding);

        for(int jj=i;jj<length;jj++){
            System.out.print((char)raw[jj]);

            //allow for comment
            if(raw[jj]==37){

                jj = ArrayUtils.skipComment(raw, jj);
            }

            if(jj>5 && raw[jj-5]=='s' && raw[jj-4]=='t' && raw[jj-3]=='r' && raw[jj-2]=='e' && raw[jj-1]=='a' &&raw[jj]=='m') {
                jj = length;
            }

            if(jj>2 && raw[jj-2]=='B' && raw[jj-1]=='D' &&raw[jj]=='C') {
                jj = length;
            }
        }
        System.out.println(padding +"\n<<<<-----------------------------------------------------\n");
    }

    public static String showMixedValuesAsString(final Object[] objectValues, String values) {

        if(objectValues==null) {
            return "null";
        }

        values += '[';
        final int count=objectValues.length;

        for(int jj=0;jj<count;jj++){

            if(objectValues[jj]==null) {
                values += "null ";
            } else if(objectValues[jj] instanceof byte[]){
                values += new String((byte[])objectValues[jj]);
                if(count-jj>1) {
                    values += " , ";
                }
            }else{
                values = showMixedValuesAsString((Object[])objectValues[jj], values)+ ']';
                if(count-jj>1) {
                    values += " ,";
                }
            }
        }
        return values;
    }
}
