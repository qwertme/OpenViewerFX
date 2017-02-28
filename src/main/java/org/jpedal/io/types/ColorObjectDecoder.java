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
 * ColorObjectDecoder.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.color.ColorSpaces;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.DecryptionFactory;
import org.jpedal.io.ObjectDecoder;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.ColorSpaceObject;
import org.jpedal.objects.raw.FunctionObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;

import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
/**
 * specific to Color object
 */
public class ColorObjectDecoder {

    static final boolean debugColorspace=false;//pdfObject.getObjectRefAsString().equals("194 0 R");//pdfObject.getDebugMode();// || debugFastCode;

    public static int handleColorSpaces(final PdfObject pdfObject, int i, final byte[] raw, final PdfFileReader objectReader) {

        final DecryptionFactory decryption=objectReader.getDecryptionObject();
               
           if(debugColorspace) {
               showData(pdfObject, raw, i);
           }

            final int len=raw.length;

           //ignore any spaces
           while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]=='[' || raw[i]==']'){
               if(raw[i]=='[') //flag as indirect as encryption will need to use this level for key
               {
                   pdfObject.maybeIndirect(true);
               }

               i++;
           }

           if(raw[i]=='/'){

               /** read the first value which is ID**/
               i++;

               //move cursor to start of text
               while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47) {
                   i++;
               }

               final int keyStart=i;
               int keyLength=0;

               //move cursor to end of text
               while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62 && raw[i]!='[' && raw[i]!=']'){
                   i++;
                   keyLength++;

                   if(i==len) {
                       break;
                   }
               }

               //store value
               int constant=pdfObject.setConstant(PdfDictionary.ColorSpace,keyStart,keyLength,raw);
               if(constant==PdfDictionary.I) //allow for abreviation in ID command
               {
                   constant = ColorSpaces.Indexed;
               }

               if(debugColorspace) {
                   System.out.println(padding + ColorSpaces.IDtoString(constant) + " Colorspace");
               }

               i = setColorspace(pdfObject, i, raw, constant,objectReader);

           }else if(raw[i]=='<' && raw[i+1]=='<'){

               i = readObjectValue(pdfObject, i, raw,objectReader);

           }else if(raw[i]=='%'){ //comments
               while(raw[i]!=10 && raw[i]!=13) {
                   i++;
               }

           }else if(raw[i]=='<'){ // its array of hex values (ie <FFEE>)

               i = readHexValue(pdfObject, i, raw,decryption);

           }else if(raw[i]=='('){ // its array of hex values (ie (\000\0\027)

               i = readStringValue(pdfObject, i, raw,decryption);

           }else{ //assume its an object

               i = readColorObjectValue(pdfObject, i, raw,objectReader);
           }

           //roll back if no gap
           if(i<len && (raw[i]==47 || raw[i]=='>')) {
               i--;
           }

           return i;
       }

    static int setColorspace(final PdfObject pdfObject, int i, final byte[] raw, final int constant, final PdfFileReader objectReader) {

        switch(constant){

            case ColorSpaces.CalRGB:{

                i=handleColorSpaces(pdfObject, i,  raw,objectReader);
                i++;

                break;
            }case ColorSpaces.CalGray:{

                i=handleColorSpaces(pdfObject, i,  raw,objectReader);
                i++;

                break;
            }case ColorSpaces.DeviceCMYK:{

                break;
            }case ColorSpaces.DeviceGray:{

                break;
            }case ColorSpaces.DeviceN:{

                i = readDeviceNvalue(pdfObject, i, raw,objectReader);

                break;
            }case ColorSpaces.DeviceRGB:{

                break;
            }case ColorSpaces.ICC:{

                i=Dictionary.readDictionaryFromRefOrDirect(-1, pdfObject,"", i, raw, PdfDictionary.ColorSpace,objectReader);

                break;

            }case ColorSpaces.Indexed:{

                i = readIndexedColorspace(pdfObject, i, raw,objectReader);

                break;

            }case ColorSpaces.Lab:{

                i=handleColorSpaces(pdfObject, i,  raw,objectReader);
                i++;

                break;

            }case ColorSpaces.Pattern:{

               readPatternColorspace(pdfObject, i, raw,objectReader);

                break;
            }case ColorSpaces.Separation:{

                i = readSeparationColorspace(pdfObject, i, raw,objectReader);

                break;
            }

            //
        }
        return i;
    }

    static int readDeviceNvalue(final PdfObject pdfObject, int i, final byte[] raw, final PdfFileReader objectReader) {

        int endPoint=i;
        while(endPoint<raw.length && raw[endPoint]!=']') {
            endPoint++;
        }

        //read Components
        final Array objDecoder=new Array(objectReader,i, endPoint, PdfDictionary.VALUE_IS_STRING_ARRAY);
        i=objDecoder.readArray(false, raw, pdfObject, PdfDictionary.Components);

        while(raw[i]==93 || raw[i]==32 || raw[i]==10 || raw[i]==13) {
            i++;
        }

        if(debugColorspace){
            System.out.println(padding+"i="+i+" DeviceN Reading altColorspace >>"+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]+(char)raw[i+5]+(char)raw[i+6]+(char)raw[i+7]+(char)raw[i+8]);
            System.out.println(padding+"before alt colorspace >>"+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]);
            System.out.println("i="+i+"  >>"+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]+(char)raw[i+5]+(char)raw[i+6]+(char)raw[i+7]);
        }

        //read the alt colorspace
        final PdfObject altColorSpace=new ColorSpaceObject(-1,0);
        i=handleColorSpaces(altColorSpace, i,  raw,objectReader);
        pdfObject.setDictionary(PdfDictionary.AlternateSpace,altColorSpace);

        i++;

        //read the transform
        final PdfObject tintTransform=new FunctionObject(-1,0);

        i=handleColorSpaces(tintTransform, i,  raw,objectReader);
        pdfObject.setDictionary(PdfDictionary.tintTransform,tintTransform);

        //check for attributes
        for(int ii=i;ii<raw.length;ii++){

            if(raw[ii]==']'){
                break;
            }else if(raw[ii]==32 || raw[ii]==10 || raw[ii]==13){//ignore spaces
            }else{

                i=ii;
                //read the attributes
                final PdfObject attributesObj=new ColorSpaceObject(-1,0);
                i=handleColorSpaces(attributesObj, i,  raw,objectReader);
                pdfObject.setDictionary(PdfDictionary.Process,attributesObj);
                i--;
                ii=raw.length;

            }
        }

        i++;
        return i;
    }

    static int readColorObjectValue(final PdfObject pdfObject, int i, final byte[] raw, final PdfFileReader objectReader) {

        if(debugColorspace) {
            System.out.println(padding + "(assume object) starts with  " + pdfObject + ' ' + pdfObject.getObjectRefAsString() + " data=" + new String(raw) + " i=" + i + ' ' + raw[i] + ' ' + (char) raw[i]);
        }

        //number
        int keyStart2=i;//keyLength2=0;
        while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62 && raw[i]!=93){
            i++;
        }

        final int number= NumberUtils.parseInt(keyStart2, i, raw);

        if(debugColorspace) {
            System.out.println(">>number=" + number + ' ' + new String(raw, keyStart2, i - keyStart2));
        }

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

        if(debugColorspace) {
            System.out.println(padding + ">>" + number + ' ' + generation + ' ' + pdfObject.getObjectRefAsString());
        }

        if(raw[i]!=82){ //we are expecting R to end ref
            throw new RuntimeException("3. Unexpected value in file "+(char)raw[i]+" - please send to IDRsolutions for analysis");
        }

        i++;

        if(pdfObject.getObjectRefID()==-1 || pdfObject.maybeIndirect()) {
            pdfObject.setRef(number, generation);
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
                LogWriter.writeLog("[Linearized] " + number + ' ' + generation + " R not yet available (9)");
            }

            i=raw.length;
        }else{

            //allow for direct (ie /DeviceCMYK)
            if(data[0]=='/'){
                handleColorSpaces(pdfObject,0, data,objectReader);
            }else{

                int j=0;
                if(data[0]!='[' && data[0]!='<'){
                    //lose obj at start
                    j=3;
                    while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111){
                        j++;
                    }
                }

                if(debugColorspace) {
                    System.out.println("Read obj i=" + i + " j=" + j + ' ' + (char) data[j] + (char) data[j + 1]);
                }

                handleColorSpaces(pdfObject,j,  data,objectReader);
            }
        }
        return i;
    }

    static int readIndexedColorspace(final PdfObject pdfObject, int i, final byte[] raw, final PdfFileReader objectReader) {
        //read the base value
        final PdfObject IndexedColorSpace=new ColorSpaceObject(-1,0);

        //IndexedColorSpace.setRef(pdfObject.getObjectRefAsString());
        i=handleColorSpaces(IndexedColorSpace, i,  raw,objectReader);
        pdfObject.setDictionary(PdfDictionary.Indexed,IndexedColorSpace);


        //onto hival number
        while(i<raw.length && (raw[i]==32 || raw[i]==13 || raw[i]==10 || raw[i]==']' || raw[i]=='>')) {
            i++;
        }

        if(debugColorspace) {
            System.out.println(padding + "Indexed Reading hival starting at >>" + (char) raw[i] + (char) raw[i + 1] + (char) +raw[i + 2] + "<<i=" + i);
        }

        //hival
        i = NumberValue.setNumberValue(pdfObject, i, raw, PdfDictionary.hival,objectReader);

        if(debugColorspace) {
            System.out.println("hival=" + pdfObject.getInt(PdfDictionary.hival) + " i=" + i + " raw[i]=" + (char) raw[i]);
        }

        if(raw[i]!='(') {
            i++;
        }

        if(debugColorspace) {
            System.out.println(padding + "next chars >>" + (char) raw[i] + (char) raw[i + 1] + (char) +raw[i + 2] + "<<i=" + i);
        }

        //onto lookup
        while(i<raw.length && (raw[i]==32 || raw[i]==13 || raw[i]==10)) {
            i++;
        }

        if(debugColorspace) {
            System.out.println(padding + "Indexed Reading lookup " + (char) raw[i] + (char) raw[i + 1] + (char) raw[i + 2] + (char) raw[i + 3] + (char) raw[i + 4]);
        }

        //read lookup
        //get the colorspace data (base)

        final PdfObject IndexedColorSpaceData;

        //can be embedded in this object (when we need to use ref or in separate object
        //when we need to use that ref). This code switches as needed)
        final boolean needsKey=raw[i]=='[' || raw[i]=='(' || raw[i]=='<';
        if(needsKey) {
            IndexedColorSpaceData = new ColorSpaceObject(pdfObject.getObjectRefAsString());
        } else {
            IndexedColorSpaceData = new ColorSpaceObject(-1, 0);
        }

        //IndexedColorSpace.setRef(pdfObject.getObjectRefAsString());
        pdfObject.setDictionary(PdfDictionary.Lookup,IndexedColorSpaceData);

        i=handleColorSpaces(IndexedColorSpaceData, i,  raw,objectReader);

        i++;
        return i;
    }
    
    static void readPatternColorspace(final PdfObject pdfObject, final int i, final byte[] raw, final PdfFileReader objectReader) {
   
        int endPoint = i;
        
        /**
         * special case where value is stored in separate data structure we have not read yet 
         */
        if(i==8 && raw.length==8 && raw[i-1]=='n' && raw[i-2]=='r' && raw[i-3]=='e' && raw[i-4]=='t' && raw[i-5]=='t' && raw[i-6]=='a' && raw[i-7]=='P' && raw[i-8]=='/'){
             
            pdfObject.setConstant(PdfDictionary.ColorSpace, PdfDictionary.Pattern);
             
            return;
        }

        //roll on to end or other type
        while (endPoint < raw.length) {
            if (raw[endPoint] == '/' || raw[endPoint] == ']') {
                break;
            }
            endPoint++;
        }

        //see if color set
        if (raw[endPoint] == '/') {

            endPoint++;

            //read length of next value (ie /DeviceRGB
            while (endPoint < raw.length) {
                if (raw[endPoint] == '/' || raw[endPoint] == ']' || raw[endPoint] == ' ' || raw[endPoint] == 13 || raw[endPoint] == 10) {
                    break;
                }

                endPoint++;
            }

            //read colorspace
            //read the alt colorspace
            final PdfObject altColorSpace=new ColorSpaceObject(-1,0);
            handleColorSpaces(altColorSpace, i,  raw,objectReader);
            pdfObject.setDictionary(PdfDictionary.AlternateSpace,altColorSpace);

            if (debugColorspace) {
                System.out.println(padding + "second colorspace=" + pdfObject + " constant=" + altColorSpace);
            }
        }
    }

    static int readSeparationColorspace(final PdfObject pdfObject, int i, final byte[] raw, final PdfFileReader objectReader) {
        final int keyLength;
        int endPoint=i;

        //roll on to start
        while(raw[endPoint]=='/' || raw[endPoint]==32 ||raw[endPoint]==10 ||raw[endPoint]==13) {
            endPoint++;
        }

        final int startPt=endPoint;

        //get name length
        while(endPoint<raw.length){
            if(raw[endPoint]=='/' || raw[endPoint]==' ' || raw[endPoint]==13 || raw[endPoint]==10) {
                break;
            }

            endPoint++;
        }

        //read name
        //set value
        keyLength=endPoint-startPt;
        final byte[] stringBytes=new byte[keyLength];
        System.arraycopy(raw,startPt,stringBytes,0,keyLength);

        //store value
        pdfObject.setName(PdfDictionary.Name,stringBytes);

        if(debugColorspace) {
            System.out.println(padding + "name=" + new String(stringBytes) + ' ' + pdfObject);
        }

        i=endPoint;

        if(raw[i]!=47) {
            i++;
        }

        if(debugColorspace) {
            System.out.println(padding + "Separation Reading altColorspace >>" + (char) raw[i] + (char) raw[i + 1]);
        }

        //read the alt colorspace
        final PdfObject altColorSpace=new ColorSpaceObject(-1,0);
        i=handleColorSpaces(altColorSpace, i,  raw,objectReader);
        pdfObject.setDictionary(PdfDictionary.AlternateSpace,altColorSpace);

        //allow for no gap
        if(raw[i]!='<') {
            i++;
        }

        //read the transform
        final PdfObject tintTransform=new FunctionObject(-1,0);

        if(debugColorspace) {
            System.out.println(padding + "Separation Reading tintTransform " + (char) raw[i - 1] + (char) raw[i] + (char) raw[i + 1] + " into " + tintTransform);
        }

        i=handleColorSpaces(tintTransform, i,  raw,objectReader);
        pdfObject.setDictionary(PdfDictionary.tintTransform,tintTransform);

        i++;
        return i;
    }

    static int readObjectValue(final PdfObject pdfObject, int i, final byte[] raw, final PdfFileReader objectReader) {
        if(debugColorspace) {
            System.out.println(padding + "Direct object starting " + (char) raw[i] + (char) raw[i + 1] + (char) raw[i + 2] + " ref=" + pdfObject.getObjectRefAsString());
        }

        i = DirectDictionaryToObject.convert(pdfObject, "", i, raw, -1,objectReader);

        //allow for stream
        /**
         * look for stream afterwards
         */
        if(pdfObject.hasStream()){
            final int count=raw.length;
            int ends=0;
            for(int xx=i;xx<count-5;xx++){

                //avoid reading on subobject ie <<  /DecodeParams << >> >>
                if(raw[xx]=='>' && raw[xx+1]=='>') {
                    ends++;
                }
                if(ends==2){
                    if(debugColorspace) {
                        System.out.println(padding + "Ignore Stream as not in sub-object " + pdfObject);
                    }

                    break;
                }

                if(raw[xx] == 's' && raw[xx + 1] == 't' && raw[xx + 2] == 'r' &&
                        raw[xx + 3] == 'e' && raw[xx + 4] == 'a' && raw[xx + 5] == 'm'){

                    if(debugColorspace) {
                        System.out.println(padding + "2. Stream found afterwards");
                    }

                    Stream.readStreamIntoObject(pdfObject,xx, raw,objectReader);
                    xx=count;

                }
            }
        }
        return i;
    }

    static int readHexValue(final PdfObject pdfObject, int i, final byte[] raw, final DecryptionFactory decryption) {

        i++;
        //find end
        int end=i, validCharCount=0;

        //here
        while(raw[end]!='>'){
            if(raw[end]!=32 && raw[end]!=10 && raw[end]!=13) {
                validCharCount++;
            }
            end++;
        }

        final int byteCount=validCharCount>>1;
        byte[] stream=new byte[byteCount];

        int byteReached=0,topHex,bottomHex;
        while(true){
            while(raw[i]==32 || raw[i]==10 || raw[i]==13) {
                i++;
            }

            topHex=raw[i];

            //convert to number
            if(topHex>='A' && topHex<='F'){
                topHex -= 55;
            }else if(topHex>='a' && topHex<='f'){
                topHex -= 87;
            }else if(topHex>='0' && topHex<='9'){
                topHex -= 48;
            }else {
                throw new RuntimeException("Unexpected number " + (char) raw[i]);
            }


            i++;

            while(raw[i]==32 || raw[i]==10 || raw[i]==13) {
                i++;
            }

            bottomHex=raw[i];

            if(bottomHex>='A' && bottomHex<='F'){
                bottomHex -= 55;
            }else if(bottomHex>='a' && bottomHex<='f'){
                bottomHex -= 87;
            }else if(bottomHex>='0' && bottomHex<='9'){
                bottomHex -= 48;
            }else {
                throw new RuntimeException("Unexpected number " + (char) raw[i]);
            }

            i++;


            //calc total
            final int finalValue=bottomHex+(topHex<<4);

            stream[byteReached] = (byte)finalValue;

            byteReached++;

            //System.out.println((char)topHex+""+(char)bottomHex+" "+byteReached+"/"+byteCount);
            if(byteReached==byteCount) {
                break;
            }
        }

        try {
            if(decryption!=null) {
                stream = decryption.decrypt(stream, pdfObject.getObjectRefAsString(), false, null, false, false);
            }
        } catch (final PdfSecurityException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        pdfObject.setDecodedStream(stream);

        return i;
    }

    static int readStringValue(final PdfObject pdfObject, int i, final byte[] raw, final DecryptionFactory decryption) {
        i++; //move past (

        final int start=i;

        //find end of textstream
        while(true){

            if(raw[i]==')' && (!ObjectUtils.isEscaped(raw, i)|| raw[i-1]==0)) {
                break;
            }

            i++;
        }

        byte[] nRaw = ObjectUtils.readEscapedValue(i, raw, start, false);

        try {
            if(decryption!=null) {
                nRaw = decryption.decrypt(nRaw, pdfObject.getObjectRefAsString(), false, null, false, false);
            }
        } catch (final PdfSecurityException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        pdfObject.setDecodedStream(nRaw);
        return i;
    }

    static void showData(final PdfObject pdfObject, final byte[] raw, final int i) {

        System.out.println(padding+"Reading colorspace into "+pdfObject+" ref="+pdfObject.getObjectRefAsString()+" i="+i+" chars="+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]);

        System.out.println(padding+"------------>");
        for(int ii=i;ii<raw.length;ii++){
            System.out.print((char)raw[ii]);

            if(ii>5 && raw[ii-5]=='s' && raw[ii-4]=='t' && raw[ii-3]=='r' && raw[ii-2]=='e' && raw[ii-1]=='a' &&raw[ii]=='m') {
                ii = raw.length;
            }
        }

        System.out.println("<--------");
    }


    static int processColorSpace(final PdfObject pdfObject, final String objectRef, final int i, final byte[] raw, final PdfFileReader objectReader) {
        //very specific type of object

        //read the base value (2 cases - Colorspace pairs or value in XObject
        if (!pdfObject.ignoreRecursion()) {

            if (pdfObject.getObjectType() == PdfDictionary.ColorSpace) {//pairs

                return handleColorSpaces(pdfObject, i, raw,objectReader);

            }else{ //Direct object in XObject

                final PdfObject ColorSpace;

                //can be called in 2 diff ways and this is difference
                final boolean isKey=raw[i]=='/';
                if(isKey) {
                    ColorSpace = new ColorSpaceObject(objectRef);
                } else {
                    ColorSpace = new ColorSpaceObject(-1, 0);
                }

                pdfObject.setDictionary(PdfDictionary.ColorSpace, ColorSpace);

                if(ColorSpace.isDataExternal()){
                    ColorSpace.isDataExternal(true);
                    if(!ObjectDecoder.resolveFully(ColorSpace,objectReader)) {
                        pdfObject.setFullyResolved(false);
                    }
                }

                return handleColorSpaces(ColorSpace, i, raw,objectReader);
            }
        }
        
        return i;
    }

}
