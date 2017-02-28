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
 * ObjectDecoder.java
 * ---------------
 */
package org.jpedal.io;

import org.jpedal.io.types.*;
import org.jpedal.objects.raw.*;
import org.jpedal.color.ColorSpaces;

import org.jpedal.utils.*;

import java.io.*;

/**
 *
 */
public class ObjectDecoder implements Serializable {
    
    public PdfFileReader objectReader;
    
    public DecryptionFactory decryption;
    
    //string representation of key only used in debugging
    private Object PDFkey;
    
    static final byte[] endPattern = { 101, 110, 100, 111, 98, 106 }; //pattern endobj
    
    //
    public static final boolean debugFastCode =false; //objRef.equals("68 0 R")
    
    /**/

    private int pdfKeyType, PDFkeyInt;
    
    /**used in debugging output*/
    public static String padding="";
    
    boolean isInlineImage;
    
    private int endPt=-1;
    
    public ObjectDecoder(final PdfFileReader pdfFileReader) {
        init(pdfFileReader);
    }

    private void init(final PdfFileReader objectReader){
        this.objectReader=objectReader;
        
        this.decryption=objectReader.getDecryptionObject();
    }
    
    /**
     * read a dictionary object
     */
    public int readDictionaryAsObject(final PdfObject pdfObject, int i, final byte[] raw){
        
        if(endPt==-1) {
            endPt = raw.length;
        }
        
        //used to debug issues by printing out details for obj
        //(set to non-final above)
        //debugFastCode =pdfObject.getObjectRefAsString().equals("5 0 R");
        
        if(debugFastCode) {
            padding += "   ";
        }
        
        final int length=raw.length;
        
        //show details in debug mode
        if(debugFastCode) {
            ObjectUtils.showData(pdfObject, i, length, raw, padding);
        }
        
        /**
         * main loop for read all values from Object data and store in PDF object
         */
        i = readObjectDataValues(pdfObject, i, raw, length);
        
        /**
         * look for stream afterwards
         */
        if(!pdfObject.ignoreStream() && pdfObject.getGeneralType(-1)!=PdfDictionary.ID) {
            Stream.readStreamData(pdfObject, i, raw, length, objectReader);
        }
        
        /**
         * we need full names for Forms
         */
        if(pdfObject.getObjectType()==PdfDictionary.Form) {
            Form.setFieldNames(pdfObject, objectReader);
        }
        
        /**
         * reset indent in debugging
         */
        if(debugFastCode){
            final int len=padding.length();
            
            if(len>3) {
                padding = padding.substring(0, len - 3);
            }
        }
        
        return i;
        
    }
    
    /**
     * get the values from the data stream and store in PdfObject
     * @param pdfObject
     * @param i
     * @param raw
     * @param length
     * @return
     */
    private int readObjectDataValues(final PdfObject pdfObject, int i, final byte[] raw, final int length) {
        
        int level=0;
        //allow for no << at start
        if(isInlineImage) {
            level = 1;
        }
        
        while(true){
            
            if(i<length && raw[i]==37) //allow for comment and ignore
            {
                i = stripComment(length, i, raw);
            }
            
            /**
             * exit conditions
             */
            if ((i>=length ||
                    (endPt !=-1 && i>= endPt))||
                    (raw[i] == 101 && raw[i + 1] == 110 && raw[i + 2] == 100 && raw[i + 3] == 111)||
                    (raw[i]=='s' && raw[i+1]=='t' && raw[i+2]=='r' && raw[i+3]=='e' && raw[i+4]=='a' && raw[i+5]=='m')) {
                break;
            }
            
            /**
             * process value
             */
            if(raw[i]==60 && raw[i+1]==60){
                i++;
                level++;
            }else if(raw[i]==62 && i+1!=length && raw[i+1]==62 && raw[i-1]!=62){
                i++;
                level--;
                
                if(level==0) {
                    break;
                }
            }else if (raw[i] == 47 && (raw[i+1] == 47 || raw[i+1]==32)) { //allow for oddity of //DeviceGray  and / /DeviceGray in colorspace
                i++;
            }else  if (raw[i] == 47) { //everything from /
                
                i++; //skip /
                
                final int keyStart=i;
                final int keyLength= Dictionary.findDictionaryEnd(i, raw, length);
                i += keyLength;
                
                if(i==length) {
                    break;
                }
                
                //if BDC see if string
                boolean isStringPair=false;
                if(pdfObject.getID()== PdfDictionary.BDC) {
                    isStringPair = Dictionary.isStringPair(i, raw, isStringPair);
                }
                
                final int type=pdfObject.getObjectType();
                
                if(debugFastCode) {
                    System.out.println("type=" + type + ' ' + ' ' + pdfObject.getID() + " chars=" + (char) raw[i - 1] + (char) raw[i] + (char) raw[i + 1] + ' ' + pdfObject + " i=" + i + ' ' + isStringPair);
                }
                
                //see if map of objects
                final boolean isMap = isMapObject(pdfObject, i, raw, length, keyStart, keyLength, isStringPair, type);
                
                if(raw[i]==47 || raw[i]==40 || (raw[i] == 91 && raw[i+1]!=']')) //move back cursor
                {
                    i--;
                }
                
                //check for unknown value and ignore
                if(pdfKeyType==-1) {
                    i = ObjectUtils.handleUnknownType(i, raw, length);
                }
                
                /**
                 * now read value
                 */
                if(PDFkeyInt==-1 || pdfKeyType==-1){
                    if(debugFastCode) {
                        System.out.println(padding + pdfObject.getObjectRefAsString() + " =================Not implemented=" + PDFkey + " pdfKeyType=" + pdfKeyType);
                    }
                }else{
                    i = setValue(pdfObject, i, raw, length, isMap);
                }
                
                //special case if Dest defined as names object and indirect
            }else if(raw[i]=='[' && level==0 && pdfObject.getObjectType()==PdfDictionary.Outlines){
                
                final Array objDecoder=new Array(objectReader,i, raw.length, PdfDictionary.VALUE_IS_MIXED_ARRAY,null, PdfDictionary.Names);
                objDecoder.readArray(false, raw, pdfObject, PdfDictionary.Dest);
                
            }
            
            i++;
            
        }
        
        return i;
    }
    
    private boolean isMapObject(final PdfObject pdfObject, final int i, final byte[] raw, final int length, final int keyStart, final int keyLength, final boolean stringPair, final int type) {
        
        final boolean isMap;//ensure all go into 'pool'
        if(type== PdfDictionary.MCID && (pdfObject.getID()==PdfDictionary.RoleMap ||
                (pdfObject.getID()==PdfDictionary.BDC && stringPair) ||
                (pdfObject.getID()==PdfDictionary.A && raw[i-2]=='/'))){
            
            pdfKeyType=PdfDictionary.VALUE_IS_NAME;
            
            //used in debug and this case
            PDFkey=PdfDictionary.getKey(keyStart,keyLength,raw);
            PDFkeyInt=PdfDictionary.MCID;
            isMap=true;
            
        }else{
            isMap=false;
            PDFkey=null;
            getKeyType(pdfObject, i, raw, length, keyLength, keyStart, type);
        }
        return isMap;
    }
    
    private void getKeyType(final PdfObject pdfObject, final int i, final byte[] raw, final int length, final int keyLength, final int keyStart, final int type) {
        /**
         * get Dictionary key and type of value it takes
         */
        if(debugFastCode)//used in debug
        {
            PDFkey = PdfDictionary.getKey(keyStart, keyLength, raw);
        }
            
        PDFkeyInt=PdfDictionary.getIntKey(keyStart,keyLength,raw);
        
        //correct mapping
        if(PDFkeyInt==PdfDictionary.Indexed && (type==PdfDictionary.MK ||type==PdfDictionary.Form || type==PdfDictionary.Linearized || type==PdfDictionary.Group)) {
            PDFkeyInt = PdfDictionary.I;
        }
        
        if(isInlineImage) {
            PDFkeyInt = PdfObjectFactory.getInlineID(PDFkeyInt);
        }
        
        final int id=pdfObject.getID();
        
        if(type==PdfDictionary.Resources && (PDFkeyInt==PdfDictionary.ColorSpace
                || PDFkeyInt==PdfDictionary.ExtGState || PDFkeyInt==PdfDictionary.Shading
                || PDFkeyInt==PdfDictionary.XObject || PDFkeyInt==PdfDictionary.Font|| PDFkeyInt==PdfDictionary.Pattern)){
            pdfKeyType=PdfDictionary.VALUE_IS_DICTIONARY_PAIRS;
            //}else if (type==PdfDictionary.Form && id== PdfDictionary.AA && PDFkeyInt== PdfDictionary.K){
            //     pdfKeyType= PdfDictionary.VALUE_IS_UNREAD_DICTIONARY;
        }else if (type==PdfDictionary.Outlines && PDFkeyInt== PdfDictionary.D){
            PDFkeyInt= PdfDictionary.Dest;
            pdfKeyType= PdfDictionary.VALUE_IS_MIXED_ARRAY;
        }else if ((type==PdfDictionary.Form || type==PdfDictionary.MK) && PDFkeyInt== PdfDictionary.D){
            if(id==PdfDictionary.AP || id==PdfDictionary.AA){
                pdfKeyType= PdfDictionary.VALUE_IS_VARIOUS;
            }else if(id==PdfDictionary.Win){
                pdfKeyType= PdfDictionary.VALUE_IS_TEXTSTREAM;
            }else{
                PDFkeyInt= PdfDictionary.Dest;
                pdfKeyType= PdfDictionary.VALUE_IS_MIXED_ARRAY;
            }
        }else if ((type==PdfDictionary.Form || type==PdfDictionary.MK) && (id==PdfDictionary.AP || id==PdfDictionary.AA) && PDFkeyInt== PdfDictionary.A){
            pdfKeyType= PdfDictionary.VALUE_IS_VARIOUS;
        }else if (PDFkeyInt== PdfDictionary.Order && type==PdfDictionary.OCProperties){
            pdfKeyType= PdfDictionary. VALUE_IS_OBJECT_ARRAY;
        }else if (PDFkeyInt== PdfDictionary.Name && type==PdfDictionary.OCProperties){
            pdfKeyType= PdfDictionary.VALUE_IS_TEXTSTREAM;
        }else if ((type==PdfDictionary.ColorSpace || type==PdfDictionary.Function) && PDFkeyInt== PdfDictionary.N){
            pdfKeyType= PdfDictionary.VALUE_IS_FLOAT;
        }else if(PDFkeyInt==PdfDictionary.Gamma && type==PdfDictionary.ColorSpace &&
                pdfObject.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.CalGray){ //its a number not an array
            pdfKeyType= PdfDictionary.VALUE_IS_FLOAT;
        }else if(id==PdfDictionary.Win && pdfObject.getObjectType()==PdfDictionary.Form &&
                (PDFkeyInt==PdfDictionary.P || PDFkeyInt==PdfDictionary.O)){
            pdfKeyType= PdfDictionary.VALUE_IS_TEXTSTREAM;
        }else if (isInlineImage && PDFkeyInt==PdfDictionary.ColorSpace){
            pdfKeyType= PdfDictionary.VALUE_IS_DICTIONARY;
        }else {
            pdfKeyType = PdfDictionary.getKeyType(PDFkeyInt, type);
        }
        
        
        //handle array of Function in Shading by using keyArray
        if(id==PdfDictionary.Shading && PDFkeyInt==PdfDictionary.Function){
            
            //get next non number/char value
            int ptr=i;
            while((raw[ptr]>=48 && raw[ptr]<58) || raw[ptr]==32){
                ptr++;
            }
            
            if(raw[ptr]=='['){
                pdfKeyType=PdfDictionary.VALUE_IS_KEY_ARRAY;
            }
        }
        
        //allow for other values in D,N,R definitions
        if(pdfKeyType==-1 && id== PdfDictionary.ClassMap){
            pdfKeyType = Dictionary.getPairedValues(pdfObject, i, raw, pdfKeyType, length, keyLength, keyStart);
        }else
            //allow for other values in D,N,R definitions as key pairs
            if(((((id==PdfDictionary.N || id==PdfDictionary.D || id==PdfDictionary.R))) &&
                    pdfObject.getParentID()==PdfDictionary.AP &&
                    pdfObject.getObjectType()==PdfDictionary.Form
                    && raw[i]!='['  )){
                
                //get next non number/char value
                int ptr=i;
                while((raw[ptr]>=48 && raw[ptr]<58) || raw[ptr]==32){
                    ptr++;
                }
                
                //decide if pair
                if(raw[keyStart]=='L' && raw[keyStart+1]=='e' && raw[keyStart+2]=='n' && raw[keyStart+3]=='g' && raw[keyStart+4]=='t' && raw[keyStart+5]=='h'){
                }else if(raw[keyStart]=='O' && raw[keyStart+1]=='n'){
                }else if(raw[keyStart]=='O' && raw[keyStart+1]=='f' && raw[keyStart+2]=='f'){
                }else
                    if(raw[ptr]=='R'){
                        pdfKeyType = Dictionary.getPairedValues(pdfObject, i, raw, pdfKeyType, length, keyLength, keyStart);
                        
                        if(debugFastCode) {
                            System.out.println("new Returns " + pdfKeyType + " i=" + i);
                        }
                    }
            }
        
        /**/
        
        //DecodeParms can be an array as well as a dictionary so check next char and alter if so
        if(PDFkeyInt==PdfDictionary.DecodeParms) {
            pdfKeyType = setTypeForDecodeParams(i, raw, length, pdfKeyType);
        }
        
        if(debugFastCode && pdfKeyType==-1 &&  pdfObject.getObjectType()!=PdfDictionary.Page){
            System.out.println(id+" "+type);
            System.out.println(padding +PDFkey+" NO type setting for "+PdfDictionary.getKey(keyStart,keyLength,raw)+" id="+i);
        }
    }
    
    private int setValue(final PdfObject pdfObject,int i, final byte[] raw, final int length, final boolean map) {
        
        //if we only need top level do not read whole tree
        final boolean ignoreRecursion=pdfObject.ignoreRecursion();
        
        if(debugFastCode) {
            System.out.println(padding + pdfObject.getObjectRefAsString() + " =================Reading value for key=" + PDFkey + " (" + PDFkeyInt + ") type=" + PdfDictionary.showAsConstant(pdfKeyType) + " ignorRecursion=" + ignoreRecursion + ' ' + pdfObject);
        }
        
        //resolve now in this case as we need to ensure all parts present
        if(pdfKeyType==PdfDictionary.VALUE_IS_UNREAD_DICTIONARY && pdfObject.isDataExternal()) {
            pdfKeyType = PdfDictionary.VALUE_IS_DICTIONARY;
        }
        
        
        switch(pdfKeyType){
            
            //read text stream (this is text) and also special case of [] in W in CID Fonts
            case PdfDictionary.VALUE_IS_TEXTSTREAM:{
                i = TextStream.setTextStreamValue(pdfObject, i, raw, ignoreRecursion,PDFkeyInt, objectReader);
                break;
                
            }case PdfDictionary.VALUE_IS_NAMETREE:{
                i = Name.setNameTreeValue(pdfObject, i, raw, length, ignoreRecursion,PDFkeyInt,objectReader);
                break;
                
                //readDictionary keys << /A 12 0 R /B 13 0 R >>
            }case PdfDictionary.VALUE_IS_DICTIONARY_PAIRS:{
                i = Dictionary.setDictionaryValue(pdfObject, i, raw, length, ignoreRecursion,objectReader,PDFkeyInt);
                break;
                
                //Strings
            }case PdfDictionary.VALUE_IS_STRING_ARRAY:{
                final Array objDecoder=new Array(objectReader,i, endPt, PdfDictionary.VALUE_IS_STRING_ARRAY);
                i=objDecoder.readArray(ignoreRecursion, raw, pdfObject, PDFkeyInt);
                break;
                
                //read Object Refs in [] (may be indirect ref)
            }case PdfDictionary.VALUE_IS_BOOLEAN_ARRAY:{
                final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_BOOLEAN_ARRAY);
                i=objDecoder.readArray(false, raw, pdfObject, PDFkeyInt);
                break;
                
                //read Object Refs in [] (may be indirect ref)
            }case PdfDictionary.VALUE_IS_KEY_ARRAY:{
                final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_KEY_ARRAY);
                i=objDecoder.readArray(ignoreRecursion, raw, pdfObject, PDFkeyInt);
                break;
                
                //read numbers in [] (may be indirect ref)
            }case PdfDictionary.VALUE_IS_MIXED_ARRAY:{
                final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_MIXED_ARRAY);
                i=objDecoder.readArray(ignoreRecursion, raw, pdfObject, PDFkeyInt);
                break;
                
                //read numbers in [] (may be indirect ref) same as Mixed but allow for recursion and store as objects
            }case PdfDictionary.VALUE_IS_OBJECT_ARRAY:{
                final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_OBJECT_ARRAY);
                i=objDecoder.readArray(false, raw, pdfObject, PDFkeyInt);
                break;
                
                //read numbers in [] (may be indirect ref)
            }case PdfDictionary.VALUE_IS_DOUBLE_ARRAY:{
                final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_DOUBLE_ARRAY);
                i=objDecoder.readArray(false, raw, pdfObject,PDFkeyInt);
                break;
                
                //read numbers in [] (may be indirect ref)
            }case PdfDictionary.VALUE_IS_INT_ARRAY:{
                final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_INT_ARRAY);
                i=objDecoder.readArray(false, raw, pdfObject, PDFkeyInt);
                break;
                
                //read numbers in [] (may be indirect ref)
            }case PdfDictionary.VALUE_IS_FLOAT_ARRAY:{
                final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_FLOAT_ARRAY);
                i=objDecoder.readArray(false, raw, pdfObject, PDFkeyInt);
                break;
                
                //read String (may be indirect ref)
            }case PdfDictionary.VALUE_IS_NAME:{
                i = Name.setNameStringValue(pdfObject, i, raw, map,PDFkey, PDFkeyInt, objectReader);
                break;
                
                //read true or false
            }case PdfDictionary.VALUE_IS_BOOLEAN:{
                i = BooleanValue.set(pdfObject, i, raw, PDFkeyInt);
                break;
                
                //read known set of values
            }case PdfDictionary.VALUE_IS_STRING_CONSTANT:{
                i = StringValue.setStringConstantValue(pdfObject, i, raw,PDFkeyInt);
                break;
                
                //read known set of values
            }case PdfDictionary.VALUE_IS_STRING_KEY:{
                i = StringValue.setStringKeyValue(pdfObject, i, raw,PDFkeyInt);
                break;
                
                //read number (may be indirect ref)
            }case PdfDictionary.VALUE_IS_INT:{
                
                //roll on
                i++;
                
                //move cursor to start of text
                while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47) {
                    i++;
                }
                
                i = NumberValue.setNumberValue(pdfObject, i, raw, PDFkeyInt,objectReader);
                break;
                
                //read float number (may be indirect ref)
            }case PdfDictionary.VALUE_IS_FLOAT:{
                i = FloatValue.setFloatValue(pdfObject, i, raw, length,PDFkeyInt,objectReader);
                break;
                
                //read known Dictionary object which may be direct or indirect
            }case PdfDictionary.VALUE_IS_UNREAD_DICTIONARY:{
                i = setUnreadDictionaryValue(pdfObject, i, raw);
                break;
                
            }case PdfDictionary.VALUE_IS_VARIOUS:{
                if(raw.length-5>0 && raw[i+1]=='n' && raw[i+2]=='u' && raw[i+3]=='l' && raw[i+4]=='l'){ //ignore null value and skip (ie /N null)
                    i += 5;
                }else{
                    i = setVariousValue(pdfObject, i, raw, length, PDFkeyInt, map, ignoreRecursion,objectReader);
                }
                break;
                
            }case PdfDictionary.VALUE_IS_DICTIONARY:{
                i = setDictionaryValue(pdfObject, i, raw, ignoreRecursion);
                break;
            }
        }
        return i;
    }
    
    static int stripComment(final int length, int i, final byte[] raw) {
        
        while(i<length && raw[i]!=10 && raw[i]!=13) {
            i++;
        }
        
        //move cursor to start of text
        while(i<length &&(raw[i]==9 || raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==60)) {
            i++;
        }
        
        return i;
    }
    
    int setVariousValue(final PdfObject pdfObject, int i, final byte[] raw, final int length, final int PDFkeyInt, final boolean map, final boolean ignoreRecursion, final PdfFileReader objectReader) {

        if(raw[i]!='<') {
            i++;
        }
        
        if(debugFastCode) {
            System.out.println(padding + "Various value (first char=" + (char) raw[i] + (char) raw[i + 1] + " )");
        }
        
        if(raw[i]=='/'){
            i = Name.setNameStringValue(pdfObject, i, raw, map, PDFkey, PDFkeyInt, objectReader);
        }else if(raw[i]=='f' && raw[i+1]=='a' && raw[i+2]=='l' && raw[i+3]=='s' && raw[i+4]=='e'){
            pdfObject.setBoolean(PDFkeyInt,false);
            i += 4;
        }else if(raw[i]=='t' && raw[i+1]=='r' && raw[i+2]=='u' && raw[i+3]=='e') {
            pdfObject.setBoolean(PDFkeyInt,true);
            i += 3;
        }else if(raw[i]=='(' || (raw[i]=='<' && raw[i-1]!='<' && raw[i+1]!='<')){
            i = TextStream.readTextStream(pdfObject, i, raw, PDFkeyInt, ignoreRecursion,objectReader);
        }else if(raw[i]=='['){
            i = setArray(pdfObject, i, raw, PDFkeyInt, ignoreRecursion, objectReader,endPt);
        }else if((raw[i]=='<' && raw[i+1]=='<')){
            i = Dictionary.readDictionary(pdfObject, i, raw, PDFkeyInt, ignoreRecursion,objectReader, isInlineImage);
        }else{
            i=General.readGeneral(pdfObject, i, raw, length, PDFkeyInt, map, ignoreRecursion, objectReader,PDFkey, endPt);
        }

        return i;
    }

    static int setArray(PdfObject pdfObject, int i, byte[] raw, int PDFkeyInt, boolean ignoreRecursion, PdfFileReader objectReader, int endPt) {
        if(PDFkeyInt== PdfDictionary.XFA){
            final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_MIXED_ARRAY);
            i=objDecoder.readArray(ignoreRecursion, raw, pdfObject, PDFkeyInt);
        }else if(PDFkeyInt== PdfDictionary.K){
            final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_STRING_ARRAY);
            i=objDecoder.readArray(ignoreRecursion, raw, pdfObject, PDFkeyInt);
        }else if(PDFkeyInt== PdfDictionary.C){
            final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_FLOAT_ARRAY);
            i=objDecoder.readArray(ignoreRecursion, raw, pdfObject, PDFkeyInt);
        }else if(PDFkeyInt== PdfDictionary.OCGs){
            final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_KEY_ARRAY);
            i=objDecoder.readArray(ignoreRecursion, raw, pdfObject, PDFkeyInt);
        }else{
            final Array objDecoder=new Array(objectReader, i, endPt, PdfDictionary.VALUE_IS_STRING_ARRAY);
            i=objDecoder.readArray(ignoreRecursion, raw, pdfObject, PDFkeyInt);
        }
        return i;
    }

    static int setTypeForDecodeParams(final int i, final byte[] raw, final int length, int pdfKeyType) {
        int ii=i;
        
        //roll onto first valid char
        while(ii<length && (raw[ii]==32 || raw[ii]==9 || raw[ii]==13 || raw[ii]==10)) {
            ii++;
        }
        
        //see if might be object arrays
        if(raw[ii]!='<'){
            
            //roll onto first valid char
            while(ii<length && (raw[ii]==32 || raw[ii]==9 || raw[ii]==13 || raw[ii]==10 || raw[ii]==91)) {
                ii++;
            }
            
            if(raw[ii]=='<' || (raw[ii]>='0' && raw[ii]<='9')) {
                pdfKeyType = PdfDictionary.VALUE_IS_OBJECT_ARRAY;
            }
            
        }
        return pdfKeyType;
    }
    
    private int setDictionaryValue(final PdfObject pdfObject,int i, final byte[] raw, final boolean ignoreRecursion) {
        /**
         * workout actual end as not always returned right
         */
        int end=i;
        int nextC=i;
        
        //ignore any gaps
        while(raw[nextC]==10 || raw[nextC]==32 || raw[nextC]==9) {
            nextC++;
        }
        
        //allow for null object
        if(raw[nextC]=='n' && raw[nextC+1]=='u' && raw[nextC+2]=='l' && raw[nextC+3]=='l'){
            i=nextC+4;
            return i;
        }else if(raw[nextC]=='[' && raw[nextC+1]==']'){ //allow for empty object []
            i=nextC;
            return i;
        }
        
        if(raw[i]!='<' && raw[i+1]!='<') {
            end += 2;
        }
        
        boolean inDictionary=true;
        final boolean isKey=raw[end-1]=='/';
        
        while(inDictionary){
            
            if(raw[end]=='<'&& raw[end+1]=='<'){
                int level2=1;
                end++;
                while(level2>0){
                    
                    if(raw[end]=='<'&& raw[end+1]=='<'){
                        level2++;
                        end += 2;
                    }else if(raw[end-1]=='>'&& raw[end]=='>'){
                        level2--;
                        if(level2>0) {
                            end += 2;
                        }
                    }else if(raw[end]=='('){ //scan (strings) as can contain >> 
                            
                            end++;
                            while(raw[end]!=')' || ObjectUtils.isEscaped(raw, end)) {
                                end++;
                            }
                    }else {
                        end++;
                    }
                }
                
                inDictionary=false;
                
            }else if(raw[end]=='R' ){
                inDictionary=false;
            }else if(isKey && (raw[end]==' ' || raw[end]==13 || raw[end]==10 || raw[end]==9)){
                inDictionary=false;
            }else if(raw[end]=='/'){
                inDictionary=false;
                end--;
            }else if(raw[end]=='>' && raw[end+1]=='>'){
                inDictionary=false;
                end--;
            }else {
                end++;
            }
        }
        
        //boolean save=debugFastCode;
        Dictionary.readDictionary(pdfObject,i, raw, PDFkeyInt, ignoreRecursion, objectReader, isInlineImage);
        
        //use correct value
        return end;
    }
    
    private int setUnreadDictionaryValue(final PdfObject pdfObject, int i, final byte[] raw) {
        
        if(raw[i]!='<')  //roll on
        {
            i++;
        }
        
        while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==9) //move cursor to start of text
        {
            i++;
        }
        
        final int start=i;
        final int keyStart;
        int keyLength;

        //create and store stub
        final PdfObject valueObj= ObjectFactory.createObject(PDFkeyInt,pdfObject.getObjectRefAsString(), pdfObject.getObjectType(), pdfObject.getID());
        valueObj.setID(PDFkeyInt);
        
        if(raw[i]=='n' && raw[i+1]=='u' && raw[i+2]=='l' && raw[i+3]=='l'){ //allow for null
        }else {
            pdfObject.setDictionary(PDFkeyInt, valueObj);
        }
        
        int status=PdfObject.UNDECODED_DIRECT; //assume not object and reset below if wrong
        
        //some objects can have a common value (ie /ToUnicode /Identity-H
        if(raw[i]==47){ //not worth caching
            
            //move cursor to start of text
            while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60) {
                i++;
            }
            
            keyStart=i;
            keyLength=0;
            
            //move cursor to end of text
            while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
                i++;
                keyLength++;
            }
            
            i--;// move back so loop works
            
            //store value
            final int constant=valueObj.setConstant(PDFkeyInt,keyStart,keyLength,raw);
            
            if(constant== PdfDictionary.Unknown || isInlineImage){
                
                final byte[] newStr=new byte[keyLength];
                System.arraycopy(raw, keyStart, newStr, 0, keyLength);
                
                final String s=new String(newStr);
                valueObj.setGeneralStringValue(s);
                
            }
            
            status=PdfObject.DECODED;
            
        }else //allow for empty object
            if(raw[i]=='e' && raw[i+1]=='n' && raw[i+2]=='d' && raw[i+3]=='o' && raw[i+4]=='b' ){
            }else{ //we need to ref from ref elsewhere which may be indirect [ref], hence loop
                
                //roll onto first valid char
                while((raw[i]==91 && PDFkeyInt!=PdfDictionary.ColorSpace)  || raw[i]==32 || raw[i]==13 || raw[i]==10){
                    i++;
                }
                
                //roll on and ignore
                if(raw[i]=='<' && raw[i+1]=='<'){
                    
                    i += 2;
                    int reflevel=1;
                    
                    while(reflevel>0){
                        if(raw[i]=='<' && raw[i+1]=='<'){
                            i += 2;
                            reflevel++;
                        }else if(raw[i]=='(' ){ //allow for << (>>) >>
                            
                            i++;
                            while(raw[i]!=')' || ObjectUtils.isEscaped(raw, i)) {
                                i++;
                            }
                            
                        }else if(raw[i]=='>' && i+1==raw.length){
                            reflevel=0;
                        }else if(raw[i]=='>' && raw[i+1]=='>'){
                            i += 2;
                            reflevel--;
                        }else {
                            i++;
                        }
                    }
                }else if(raw[i]=='['){
                    
                    i++;
                    int reflevel=1;
                    
                    while(reflevel>0){
                        
                        if(raw[i]=='(' ){ //allow for [[ in stream ie [/Indexed /DeviceRGB 255 (abc[[z
                            
                            i++;
                            while(raw[i]!=')' || ObjectUtils.isEscaped(raw, i)) {
                                i++;
                            }
                            
                        }else if(raw[i]=='[' ){
                            reflevel++;
                        }else if(raw[i]==']'){
                            reflevel--;
                        }
                        
                        i++;
                    }
                    i--;
                }else if(raw[i]=='n' && raw[i+1]=='u' && raw[i+2]=='l' && raw[i+3]=='l'){ //allow for null
                    i += 4;
                }else{ //must be a ref
                    
                    //assume not object and reset below if wrong
                    status=PdfObject.UNDECODED_REF;
                    
                    while(raw[i]!='R' || raw[i-1]=='e') { //second condition to stop spurious match on DeviceRGB
                        i++;
                        
                        if(i==raw.length) {
                            break;
                        }
                    }
                    i++;
                    
                    if(i>=raw.length) {
                        i = raw.length - 1;
                    }
                }
            }
        
        valueObj.setStatus(status);
        if(status!=PdfObject.DECODED){
            
            final int StrLength=i-start;
            final byte[] unresolvedData=new byte[StrLength];
            System.arraycopy(raw, start, unresolvedData, 0, StrLength);
            
            //check for returns in data if ends with R and correct to space
            if(unresolvedData[StrLength-1]==82){
                
                for(int jj=0;jj<StrLength;jj++){
                    
                    if(unresolvedData[jj]==10 || unresolvedData[jj]==13) {
                        unresolvedData[jj] = 32;
                    }
                    
                }
            }
            valueObj.setUnresolvedData(unresolvedData,PDFkeyInt);
            
        }
        
        if(raw[i]=='/' || raw[i]=='>') //move back so loop works
        {
            i--;
        }
        return i;
    }
   
    public static int handleValue(final PdfObject pdfObject, int i, final int PDFkeyInt, int j, final int ref, final int generation, final byte[] data, final PdfFileReader objectReader) {
        
        final int keyStart;
        int keyLength;
        final int dataLen=data.length;
        
        if (data[j] == 47) {
            j++; //roll on past /
            
            keyStart = j;
            keyLength = 0;
            
            //move cursor to end of text
            while (j<dataLen && data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                j++;
                keyLength++;
                
            }
            
            i--;// move back so loop works
            
            if (PDFkeyInt == -1) {
                //store value directly
                pdfObject.setConstant(PDFkeyInt, keyStart, keyLength, data);
                
                if (debugFastCode) {
                    System.out.println(padding + "Set object Constant directly to " + pdfObject.setConstant(PDFkeyInt, keyStart, keyLength, data));
                }
            } else {
                //convert data to new Dictionary object
                final PdfObject valueObj = ObjectFactory.createObject(PDFkeyInt, null,pdfObject.getObjectType(), pdfObject.getID());
                valueObj.setID(PDFkeyInt);
                //store value
                valueObj.setConstant(PDFkeyInt, keyStart, keyLength, data);
                pdfObject.setDictionary(PDFkeyInt, valueObj);
                
                if(pdfObject.isDataExternal()){
                    valueObj.isDataExternal(true);
                    if(!resolveFully(valueObj,objectReader)) {
                        pdfObject.setFullyResolved(false);
                    }
                }
            }
        } else {
            
            //convert data to new Dictionary object
            final PdfObject valueObj  ;
            if (PDFkeyInt == -1) {
                valueObj = pdfObject;
            } else {
                valueObj = ObjectFactory.createObject(PDFkeyInt, ref, generation, pdfObject.getObjectType());
                valueObj.setID(PDFkeyInt);
                valueObj.setInCompressedStream(pdfObject.isInCompressedStream());
                
                if(pdfObject.isDataExternal()){
                    valueObj.isDataExternal(true);
                    
                    if(!resolveFully(valueObj,objectReader)) {
                        pdfObject.setFullyResolved(false);
                    }
                }
                
                if (PDFkeyInt != PdfDictionary.Resources) {
                    valueObj.ignoreRecursion(pdfObject.ignoreRecursion());
                }
            }
            
            final ObjectDecoder objDecoder=new ObjectDecoder(objectReader);
            objDecoder.readDictionaryAsObject(valueObj, j, data);
            
            //store value
            if (PDFkeyInt != -1) {
                pdfObject.setDictionary(PDFkeyInt, valueObj);
            }
        }
        
        return i;
    }
    
    /**
     * used by linearization to check object fully fully available and return false if not
     * @param pdfObject
     */
    public static synchronized boolean resolveFully(final PdfObject pdfObject, final PdfFileReader objectReader){
        
        boolean fullyResolved=pdfObject!=null;
        
        if(fullyResolved){
            
            final byte[] raw;
            if(pdfObject.getStatus()==PdfObject.DECODED) {
                raw = StringUtils.toBytes(pdfObject.getObjectRefAsString());
            } else {
                raw = pdfObject.getUnresolvedData();
            }
            
            //flag now done and flush raw data
            pdfObject.setStatus(PdfObject.DECODED);
            
            //allow for empty object
            if(raw[0]!='e' && raw[1]!='n' && raw[2]!='d' && raw[3]!='o' && raw[4]!='b' ){
                
                int j=0;
                
                //allow for [ref] at top level (may be followed by gap
                while (raw[j] == 91 || raw[j] == 32 || raw[j] == 13 || raw[j] == 10) {
                    j++;
                }
                
                // get object ref
                int keyStart = j;
                
                //move cursor to end of reference
                while (raw[j] != 10 && raw[j] != 13 && raw[j] != 32 && raw[j] != 47 && raw[j] != 60 && raw[j] != 62) {
                    j++;
                }
                
                final int ref = NumberUtils.parseInt(keyStart, j, raw);
                
                //move cursor to start of generation or next value
                while (raw[j] == 10 || raw[j] == 13 || raw[j] == 32)// || data[j]==47 || data[j]==60)
                {
                    j++;
                }
                
                /**
                 * get generation number
                 */
                keyStart = j;
                
                //move cursor to end of reference
                while (raw[j] != 10 && raw[j] != 13 && raw[j] != 32 && raw[j] != 47 && raw[j] != 60 && raw[j] != 62) {
                    j++;
                }
                
                final int generation = NumberUtils.parseInt(keyStart, j, raw);
                
                if(raw[raw.length-1]=='R') //recursively validate all child objects
                {
                    fullyResolved = resolveFullyChildren(pdfObject, fullyResolved, raw, ref, generation, objectReader);
                }
                
                if(fullyResolved){
                    pdfObject.ignoreRecursion(false);
                    final ObjectDecoder objDecoder=new ObjectDecoder(objectReader);
                    objDecoder.readDictionaryAsObject(pdfObject, j, raw);
                    
                    //if(!pdfObject.isFullyResolved())
                    //    fullyResolved=false;
                }
            }
        }
        
        return fullyResolved;
    }
    
    static boolean resolveFullyChildren(final PdfObject pdfObject, boolean fullyResolved, final byte[] raw, final int ref, final int generation, final PdfFileReader objectReader) {
        
        pdfObject.setRef(new String(raw));
        pdfObject.isDataExternal(true);
        
        final byte[] pageData = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);
        
        //allow for data in Linear object not yet loaded
        if(pageData==null){
            pdfObject.setFullyResolved(false);
            fullyResolved=false;
        }else{
            pdfObject.setStatus(PdfObject.UNDECODED_DIRECT);
            pdfObject.setUnresolvedData(pageData, PdfDictionary.Linearized);
            pdfObject.isDataExternal(true);
            
            if(!resolveFully(pdfObject,objectReader)) {
                pdfObject.setFullyResolved(false);
            }
        }
        
        return fullyResolved;
    }
    
    /**
     * read object setup to contain only ref to data
     * @param pdfObject
     */
    public void checkResolved(final PdfObject pdfObject){
        
        if(pdfObject!=null && pdfObject.getStatus()!=PdfObject.DECODED){
            
            final byte[] raw=pdfObject.getUnresolvedData();

            //flag now done and flush raw data
            pdfObject.setStatus(PdfObject.DECODED);
            
            //allow for empty object
            if(raw[0]=='e' && raw[1]=='n' && raw[2]=='d' && raw[3]=='o' && raw[4]=='b' ){
                //empty object
            }else if(raw[0]=='n' && raw[1]=='u' && raw[2]=='l' && raw[3]=='l'){
                //null object
            }else{ //we need to ref from ref elsewhere which may be indirect [ref], hence loop
                
                String objectRef=pdfObject.getObjectRefAsString();
                
                //allow for Color where starts [/ICCBased 2 0 R so we get the ref if present
                if(raw[0]=='['){
                    
                    //scan along to find number
                    int ptr=0;
                    final int len=raw.length;
                    for(int jj=0;jj<len;jj++){
                        
                        if(raw[jj]>='0' && raw[jj]<='9'){
                            ptr=jj;
                            jj=len;
                        }
                    }
                    
                    //check first non-number is R
                    int end=ptr;
                    while((raw[end]>='0' && raw[end]<='9') || raw[end]==' ' || raw[end]==10 || raw[end]==13 || raw[end]==9) {
                        end++;
                    }
                    //and store if it is a ref
                    if(raw[end]=='R') {
                        pdfObject.setRef(new String(raw, ptr, len - ptr));
                    }
                    
                }else if(raw[raw.length-1]=='R'){
                    objectRef=new String(raw);
                    pdfObject.setRef(objectRef);
                }
                
                Dictionary.readDictionaryFromRefOrDirect(-1,pdfObject,objectRef, 0, raw , -1,objectReader);
                
            }
        }
    }
    
    /**
     * set end if not end of data stream
     */
    public void setEndPt(final int dataPointer) {
        this.endPt=dataPointer;
    }
}
