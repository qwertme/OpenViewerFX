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
 * General.java
 * ---------------
 */

package org.jpedal.io.types;

import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.ObjectFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;

/**
 *
 * @author markee
 */
public class General {

    
    public static int readGeneral(final PdfObject pdfObject, int i, final byte[] raw, final int length, final int PDFkeyInt, final boolean map, final boolean ignoreRecursion, final PdfFileReader objectReader,Object PDFkey, int endPt){

        int keyStart;

        if(debugFastCode) {
            System.out.println(padding + "general case " + i);
        }

        //see if number or ref
        int jj=i;
        int j=i+1;
        byte[] data=raw;
        int typeFound=0;
        boolean isNumber=true, isRef=false, isString=false;

        String objRef=pdfObject.getObjectRefAsString();

        while(true){

            if(data[j]=='R' && !isString){

                isRef=true;
                final int end=j;
                j=i;
                i=end;

                final int ref;
                final int generation;

                //allow for [ref] at top level (may be followed by gap
                while (data[j] == 91 || data[j] == 32 || data[j] == 13 || data[j] == 10) {
                    j++;
                }

                // get object ref
                keyStart = j;
                final int refStart=j;
                //move cursor to end of reference
                while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                    j++;
                }

                ref = NumberUtils.parseInt(keyStart, j, data);

                //move cursor to start of generation or next value
                while (data[j] == 10 || data[j] == 13 || data[j] == 32)// || data[j]==47 || data[j]==60)
                {
                    j++;
                }

                /**
                 * get generation number
                 */
                keyStart = j;
                //move cursor to end of reference
                while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                    j++;
                }

                generation = NumberUtils.parseInt(keyStart, j, data);

                /**
                 * check R at end of reference and abort if wrong
                 */
                //move cursor to start of R
                while (data[j] == 10 || data[j] == 13 || data[j] == 32 || data[j] == 47 || data[j] == 60) {
                    j++;
                }

                if (data[j] != 82)  //we are expecting R to end ref
                {
                    throw new RuntimeException("ref=" + ref + " gen=" + ref + " 1. Unexpected value " + data[j] + " in file - please send to IDRsolutions for analysis char=" + (char) data[j]);
                }

                objRef =new String(data,refStart,1+j-refStart);

                //read the Dictionary data
                //boolean setting=debugFastCode;
                final byte[] newData = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);

                //find first valid char to see if String
                int firstChar=0;

                //some data actually starts << and not 10 0 obj so allow for this
                if(newData!=null && newData.length>2 && newData[0]=='<' && newData[1]=='<'){
                    //check for already at <<

                }else{
                    final int newLength=newData.length-3;
                    for(int aa=3;aa<newLength;aa++){   //skip past 13 0 obj bit at start if present
                        if(newData[aa-2]=='o' && newData[aa-1]=='b' && newData[aa]=='j'){
                            firstChar=aa+1;
                            //roll on past and spaces
                            while(firstChar<newLength && (newData[firstChar]==10 || newData[firstChar]==13 || newData[firstChar]==32 || newData[firstChar]==9)) {
                                firstChar++;
                            }

                            aa=newLength; //exit loop
                        }else if(newData[aa]>47 && newData[aa]<58){//number
                        }else if(newData[aa]=='o' || newData[aa]=='b' || newData[aa]=='j' || newData[aa]=='R' || newData[aa]==32 || newData[aa]==10 || newData[aa]==13){ //allowed char
                        }else{ //not expected so reset and quit
                            aa= newLength;
                            firstChar=0;
                        }
                    }
                }

                //stop string with R failing in loop
                isString=newData[firstChar]=='(';

                if((pdfObject.getID()== PdfDictionary.AA || pdfObject.getID()== PdfDictionary.A) && newData[0]=='<' && newData[1]=='<'){

                    //create and store stub
                    final PdfObject valueObj= ObjectFactory.createObject(PDFkeyInt, objRef, pdfObject.getObjectType(), pdfObject.getID());
                    valueObj.setID(PDFkeyInt);

                    pdfObject.setDictionary(PDFkeyInt,valueObj);
                    valueObj.setStatus(PdfObject.UNDECODED_DIRECT);
                    valueObj.setUnresolvedData(newData,PdfObject.UNDECODED_DIRECT);

                    isNumber=false;
                    typeFound=4;

                    i=j;

                    break;

                }else if((pdfObject.getID()== -1 || pdfObject.getID()!=4384) && newData[0]=='<' && newData[1]=='<'){
                    isNumber=false;
                    typeFound=0;

                    i=j;

                    break;

                    //allow for indirect on Contents
                }else if(PDFkeyInt== PdfDictionary.Contents && data[i]=='R'){

                    //get the object data and pass in
                    int jj2=0;
                    while(newData[jj2]!='['){
                        jj2++;

                        if(newData[jj2]=='<' && newData[jj2+1]!='<') {
                            break;
                        }
                    }

                    final Array objDecoder=new Array(objectReader, jj2, endPt, PdfDictionary.VALUE_IS_KEY_ARRAY);
                    objDecoder.readArray(ignoreRecursion, newData, pdfObject, PDFkeyInt);
                    i=j;
                    break;

                }else if(PDFkeyInt== PdfDictionary.OpenAction && data[i]=='R'){
                    return readOpenAction(pdfObject, PDFkeyInt, ignoreRecursion, objectReader, endPt, j, newData);
                }else{

                    data=newData;

                    //allow for data in Linear object not yet loaded
                    if(data==null){
                        pdfObject.setFullyResolved(false);

                        if(debugFastCode) {
                            System.out.println(padding + "Data not yet loaded");
                        }

                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog("[Linearized] " + objRef + " not yet available (4)");
                        }

                        i=length;
                        break;
                    }

                    jj=3;

                    if(data.length<=3){
                        jj=0;
                    }else{
                        while(true){
                            if(data[jj-2]=='o' && data[jj-1]=='b' && data[jj]=='j') {
                                break;
                            }

                            jj++;

                            if(jj==data.length){
                                jj=0;
                                break;
                            }
                        }
                    }

                    if(data[jj]!='[' && data[jj]!='(' && data[jj]!='<') //do not roll on if text string
                    {
                        jj++;
                    }

                    while (data[jj] == 10 || data[jj] == 13 || data[jj] == 32)// || data[j]==47 || data[j]==60)
                    {
                        jj++;
                    }

                    j=jj;

                    if(debugFastCode) {
                        System.out.println(j + " >>" + new String(data) + "<<next=" + (char) data[j]);
                    }
                }
            }else if(data[j]=='[' || data[j]=='('){
                //typeFound=0;
                break;
            }else if(data[j]=='<'){
                typeFound=0;
                break;

            }else if(data[j]=='>' || data[j]=='/'){
                typeFound=1;
                break;
            }else if(data[j]==32 || data[j]==10 || data[j]==13){
            }else if((data[j]>='0' && data[j] <='9')|| data[j]=='.'){ //assume and disprove
            }else{
                isNumber=false;
            }
            if(data[j]!='['){
                j++;
            }
            if(j==data.length) {
                break;
            }
        }

        //check if name by counting /
        int count=0;
        for(int aa=jj+1;aa<data.length;aa++){
            if(data[aa]=='/') {
                count++;
            }
        }

        //lose spurious spaces
        while (data[jj] == 10 || data[jj] == 13 || data[jj] == 32)// || data[j]==47 || data[j]==60)
        {
            jj++;
        }

        if(typeFound==4){//direct ref done above
        }else if(count==0 && data[jj]=='/'){

            if(debugFastCode) {
                System.out.println(padding + "NameString ");
            }

            jj = Name.setNameStringValue(pdfObject, jj, data, map, PDFkey, PDFkeyInt, objectReader);
        }else if(data[jj]=='('){

            if(debugFastCode) {
                System.out.println(padding + "Textstream ");
            }

            jj = TextStream.readTextStream(pdfObject, jj, data, PDFkeyInt, ignoreRecursion,objectReader);
        }else if(data[jj]=='['){

            if(debugFastCode) {
                System.out.println(padding + "Array ");
            }

            final Array objDecoder=new Array(objectReader, jj, endPt, PdfDictionary.VALUE_IS_STRING_ARRAY);
            jj=objDecoder.readArray(ignoreRecursion, data, pdfObject, PDFkeyInt);
                /**/
        }else if(typeFound==0){
            if(debugFastCode) {
                System.out.println("Dictionary " + (char) +data[jj] + (char) data[jj + 1]);
            }

            try{
                jj = Dictionary.readDictionaryFromRefOrDirect(-1,pdfObject, objRef,jj , data, PDFkeyInt,objectReader);

            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
            //tests after isNUmber ensure we handle /N << /1 15 0 R /Off 16 0 R>> in next test
        }else if(isNumber && (pdfObject.getID()!=4384 ||( PDFkeyInt != PdfDictionary.D && PDFkeyInt != PdfDictionary.N && PDFkeyInt != PdfDictionary.R))){

            if(debugFastCode) {
                System.out.println("Number");
            }

            jj= NumberValue.setNumberValue(pdfObject, jj, data, PDFkeyInt,objectReader);

        }else if(typeFound==1){

            //really horrible code to allow for /N <</sillyKey 15 0 R /D 15 0 R>>
            //see CIS_Post_Training_Event_Assessment_User1Response.pdf
            if (PDFkeyInt == PdfDictionary.D || PDFkeyInt == PdfDictionary.N || PDFkeyInt == PdfDictionary.R) {
                jj = handleArray(pdfObject, PDFkeyInt, objectReader, jj, data);
            }else {
                if (debugFastCode) {
                    System.out.println("Name");
                }

                jj = Name.setNameStringValue(pdfObject, jj, data, map,PDFkey, PDFkeyInt, objectReader);
            }

        }else if(debugFastCode) {
            System.out.println(padding + "Not read");
        }

        if(!isRef) {
            i = jj;
        }

        return i;
    }

    private static int handleArray(PdfObject pdfObject, int PDFkeyInt, PdfFileReader objectReader, int jj, byte[] data) {
        final PdfObject APobj= ObjectFactory.createObject(PDFkeyInt,pdfObject.getObjectRefAsString(), PdfDictionary.Form, pdfObject.getID());
        pdfObject.setDictionary(PDFkeyInt, APobj);

        int ptr = jj;
        final int dataLength = data.length;
        while (true) {

            if (ptr >= dataLength - 1) {
                break;
            }

            //got to start of command
            while (data[ptr] != '/') {
                ptr++;
            }

            ptr++;

            int start = ptr;
            final int key;
            final String value;

            //got to start of command
            while (data[ptr] != 32 && data[ptr] != 10 && data[ptr] != 9 && data[ptr] != 13) {
                ptr++;
            }

            key = PdfDictionary.getIntKey(start, ptr - start, data);
            final Object currentKey = PdfDictionary.getKey(start, ptr - start, data);

            //goto start of value
            while (data[ptr] == 32 || data[ptr] == 10 || data[ptr] == 9 && data[ptr] == 13) {
                ptr++;
            }

            //get value
            start = ptr;

            //got to start of command
            while (ptr < dataLength && data[ptr] != '/' && data[ptr] != '>') {
                ptr++;
            }

            value = (String) PdfDictionary.getKey(start, ptr - start, data);

            if (debugFastCode) {
                System.out.println("key=" + key + "<>" + value + " nextChar=" + data[ptr]);
            }

            final PdfObject obj= ObjectFactory.createObject(key,value, PdfDictionary.Form, pdfObject.getID());

            Dictionary.readDictionaryFromRefOrDirect(key, obj, (String) currentKey, 0, value.getBytes(), -1,objectReader);

            if(key!=PdfDictionary.On && key!=PdfDictionary.Off) {
                APobj.setCurrentKey(currentKey);
            }

            APobj.setDictionary(key, obj);

            while (ptr < dataLength && data[ptr] == '>') {
                ptr++;
            }
        }

        return ptr;
    }

    private static int readOpenAction(PdfObject pdfObject, int PDFkeyInt, boolean ignoreRecursion, PdfFileReader objectReader, int endPt, int j, byte[] newData) {
        int i;//get the object data and pass in
        int jj2=0;
        while(newData[jj2]!='['){
            jj2++;

            if(newData[jj2]=='<' && newData[jj2+1]!='<') {
                break;
            }
        }

        final Array objDecoder=new Array(objectReader, jj2, endPt, PdfDictionary.VALUE_IS_MIXED_ARRAY);
        objDecoder.readArray(ignoreRecursion, newData, pdfObject, PDFkeyInt);
        i=j;

        return i;
    }
}
