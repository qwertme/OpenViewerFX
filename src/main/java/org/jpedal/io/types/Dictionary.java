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
 * Dictionary.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.io.ObjectDecoder;
import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import static org.jpedal.io.ObjectDecoder.resolveFully;
import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.ColorSpaceObject;
import org.jpedal.objects.raw.NamesObject;
import org.jpedal.objects.raw.ObjectFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;
import org.jpedal.utils.StringUtils;

/**
 *
 */
public class Dictionary {

    public static int readDictionary(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final boolean ignoreRecursion, final PdfFileReader objectReader, final boolean isInlineImage) {
        
        int keyLength;
        final int keyStart;

        final String objectRef=pdfObject.getObjectRefAsString();
        
        //roll on
        if(raw[i]!='<') {
            i++;
        }
        
        //move cursor to start of text
        while(raw[i]==10 || raw[i]==13 || raw[i]==32) {
            i++;
        }
        
        //some objects can have a common value (ie /ToUnicode /Identity-H
        if(raw[i]==47){
            
            if(debugFastCode) {
                System.out.println(padding + "Indirect");
            }
            
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
            
            if(!ignoreRecursion){
                
                final PdfObject valueObj=ObjectFactory.createObject(PDFkeyInt,objectRef, pdfObject.getObjectType(), pdfObject.getID());
                valueObj.setID(PDFkeyInt);
                
                //store value
                final int constant=valueObj.setConstant(PDFkeyInt,keyStart,keyLength,raw);
                
                if(constant==PdfDictionary.Unknown || isInlineImage){
                    
                    final byte[] newStr=new byte[keyLength];
                    System.arraycopy(raw, keyStart, newStr, 0, keyLength);
                    
                    final String s=new String(newStr);
                    valueObj.setGeneralStringValue(s);
                    
                    if(debugFastCode) {
                        System.out.println(padding + "Set Dictionary as String=" + s + "  in " + pdfObject + " to " + valueObj);
                    }
                    
                }else if(debugFastCode) {
                    System.out.println(padding + "Set Dictionary as constant=" + constant + "  in " + pdfObject + " to " + valueObj);
                }
                
                
                //store value
                pdfObject.setDictionary(PDFkeyInt,valueObj);
                
                if(pdfObject.isDataExternal()){
                    valueObj.isDataExternal(true);
                    if(!resolveFully(valueObj,objectReader)) {
                        pdfObject.setFullyResolved(false);
                    }
                }
            }
            
        }else //allow for empty object
            if(raw[i]=='e' && raw[i+1]=='n' && raw[i+2]=='d' && raw[i+3]=='o' && raw[i+4]=='b' ){
                //        return i;
                
                if(debugFastCode) {
                    System.out.println(padding + "Empty object" + new String(raw) + "<<");
                }
                
            }else if(raw[i]=='(' && PDFkeyInt== PdfDictionary.JS){ //ie <</S/JavaScript/JS( for JS
                i++;
                final int start=i;
                //find end
                while(i<raw.length){
                    i++;
                    if(raw[i]==')' && !ObjectUtils.isEscaped(raw, i)) {
                        break;
                    }
                }
                final byte[] data=ObjectUtils.readEscapedValue(i,raw,start, false);
                
                final NamesObject JS=new NamesObject(objectRef);
                JS.setDecodedStream(data);
                pdfObject.setDictionary(PdfDictionary.JS, JS);
                
            }else{ //we need to ref from ref elsewhere which may be indirect [ref], hence loop
                
                if(debugFastCode) {
                    System.out.println(padding + "1.About to read ref orDirect i=" + i + " char=" + (char) raw[i] + " ignoreRecursion=" + ignoreRecursion);
                }
                
                
                if(ignoreRecursion){
                    
                    //roll onto first valid char
                    while(raw[i]==91 || raw[i]==32 || raw[i]==13 || raw[i]==10){
                        
                        //if(raw[i]==91) //track incase /Mask [19 19]
                        //	possibleArrayStart=i;
                        
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
                            }else if(raw[i]=='>' && raw[i+1]=='>'){
                                i += 2;
                                reflevel--;
                            }else {
                                i++;
                            }
                        }
                        i--;
                        
                    }else{ //must be a ref
                        //                					while(raw[i]!='R')
                        //                						i++;
                        //                					i++;
                        //System.out.println("read ref");
                        i = Dictionary.readDictionaryFromRefOrDirect(PDFkeyInt,pdfObject,objectRef, i, raw, PDFkeyInt,objectReader);
                    }
                    
                    if(i<raw.length && raw[i]=='/') //move back so loop works
                    {
                        i--;
                    }
                    
                }else{
                    i = Dictionary.readDictionaryFromRefOrDirect(PDFkeyInt,pdfObject,objectRef, i, raw, PDFkeyInt,objectReader);
                }
            }
        return i;
    }
    
    public static int getPairedValues(final PdfObject pdfObject, final int i, final byte[] raw, final int pdfKeyType, final int length, final int keyLength, final int keyStart) {
        
        boolean isPair=false;
        
        int jj=i;
        
        while(jj<length){
            
            //ignore any spaces
            while(jj<length && (raw[jj]==32 || raw[jj]==13 || raw[jj]==10)) {
                jj++;
            }
            
            //number (possibly reference)
            if(jj<length && raw[jj]>='0' && raw[jj]<='9'){
                
                //rest of ref
                while(jj<length && raw[jj]>='0' && raw[jj]<='9') {
                    jj++;
                }
                
                //ignore any spaces
                while(jj<length && (raw[jj]==32 || raw[jj]==10 || raw[jj]==13)) {
                    jj++;
                }
                
                //generation and spaces
                while(jj<length && ((raw[jj]>='0' && raw[jj]<='9')||(raw[jj]==32 || raw[jj]==10 || raw[jj]==13))) {
                    jj++;
                }
                
                //not a ref
                if(jj>=length || raw[jj]!='R') {
                    break;
                }
                
                //roll past R
                jj++;
            }
            
            //ignore any spaces
            while(jj<length && (raw[jj]==32 || raw[jj]==13 || raw[jj]==10)) {
                jj++;
            }
            
            //must be next key or end
            if(raw[jj]=='>' && raw[jj+1]=='>'){
                isPair=true;
                break;
            }else if(raw[jj]!='/') {
                break;
            }
            
            jj++;
            
            //ignore any spaces
            while(jj<length && (raw[jj]!=32 && raw[jj]!=13 && raw[jj]!=10)) {
                jj++;
            }
            
        }
        
        if(isPair){
            pdfObject.setCurrentKey(PdfDictionary.getKey(keyStart,keyLength,raw));
            return PdfDictionary.VALUE_IS_UNREAD_DICTIONARY;
        }else {
            return pdfKeyType;
        }
    }
    
    public static boolean isStringPair(final int i, final byte[] raw, boolean stringPair) {
        
        final int len=raw.length;
        for(int aa=i;aa<len;aa++){
            if(raw[aa]=='('){
                aa=len;
                stringPair =true;
            }else if(raw[aa]=='/' || raw[aa]=='>' || raw[aa]=='<' || raw[aa]=='[' || raw[aa]=='R'){
                aa=len;
            }else if(raw[aa]=='M' && raw[aa+1]=='C' && raw[aa+2]=='I' && raw[aa+3]=='D'){
                aa=len;
            }
        }
        return stringPair;
    }
    
    public static int findDictionaryEnd(int jj, final byte[] raw, final int length) {
        
        int keyLength=0;
        while (true) { //get key up to space or [ or / or ( or < or carriage return
            
            if (raw[jj] == 32 || raw[jj] == 13 || raw[jj] == 9 || raw[jj] == 10 || raw[jj] == 91 ||
                    raw[jj]==47 || raw[jj]==40 || raw[jj]==60 || raw[jj]==62) {
                break;
            }
            
            jj++;
            keyLength++;
            
            if(jj==length) {
                break;
            }
        }
        return keyLength;
    }
    
     
    public static int setDictionaryValue(final PdfObject pdfObject, int i, final byte[] raw, final int length, final boolean ignoreRecursion, final PdfFileReader objectReader, final int PDFkeyInt) {
        
        if(debugFastCode) {
            System.out.println(padding + ">>>Reading Dictionary Pairs i=" + i + ' ' + (char) raw[i] + (char) raw[i + 1] + (char) raw[i + 2] + (char) raw[i + 3] + (char) raw[i + 4] + (char) raw[i + 5] + (char) raw[i + 6]);
        }
        
        //move cursor to start of text
        while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47) {
            i++;
        }
        
        //set data which will be switched below if ref
        byte[] data=raw;
        int j=i;
        
        //get next key to see if indirect
        final boolean isRef=data[j]!='<';
        
        if(isRef){
            
            //number
            int keyStart2=i;
            while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62) {
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
                        LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (2)");
                    }
                    
                    i=length;
                    return i;
                }
                
                if(data[0]=='<' && data[1]=='<'){
                    j=0;
                }else{
                    //lose obj at start
                    j=3;
                    
                    while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111){
                        
                        if(data[j]=='/'){  //trap for odd case
                            j=0;
                            break;
                        }
                        
                        j++;
                        
                        if(j==data.length){ //some missing obj so catch these
                            j=0;
                            break;
                        }
                    }
                    
                    //skip any spaces after
                    while(data[j]==10 || data[j]==13 || data[j]==32)// || data[j]==47 || data[j]==60)
                    {
                        j++;
                    }
                }
                
            }
        }
        
        //allow for empty object (ie /Pattern <<>> )
        int endJ=j;
        while(data[endJ]=='<' || data[endJ]==' ' || data[endJ]==13 ||  data[endJ]==10) {
            endJ++;
        }
        
        if(data[endJ]=='>'){ //empty object
            j=endJ+1;
        }else{
            
            final PdfObject valueObj= ObjectFactory.createObject(PDFkeyInt, pdfObject.getObjectRefAsString(), pdfObject.getObjectType(), pdfObject.getID());
            valueObj.setID(PDFkeyInt);
            
            /**
             * read pairs (stream in data starting at j)
             */
            if(ignoreRecursion) //just skip to end
            {
                j = readKeyPairs(PDFkeyInt, data, j, -2, null, objectReader);
            } else{
                //count values first
                final int count=readKeyPairs(PDFkeyInt,data, j,-1, null,objectReader);
                
                //now set values
                j=readKeyPairs(PDFkeyInt,data, j,count,valueObj,objectReader);
                
                
                //store value
                pdfObject.setDictionary(PDFkeyInt,valueObj);
                
                if(debugFastCode) {
                    System.out.println(padding + "Set Dictionary " + count + " pairs type in " + pdfObject + " to " + valueObj);
                }
            }
        }
        
        //update pointer if direct so at end (if ref already in right place)
        if(!isRef){
            i=j;
            
            if(debugFastCode) {
                System.out.println(i + ">>>>" + data[i - 2] + ' ' + data[i - 1] + " >" + data[i] + "< " + data[i + 1] + ' ' + data[i + 2]);
            }
        }
        return i;
    }
    
     
    /**
     * if pairs is -1 returns number of pairs
     * otherwise sets pairs and returns point reached in stream
     */
    static int readKeyPairs(final int id, final byte[] data, final int j,int pairs, final PdfObject pdfObject, final PdfFileReader objectReader) {
        
        final boolean debug=false;
        
        int start=j,level;
        
        final int numberOfPairs=pairs;
        
        //same routine used to count first and then fill with values
        boolean isCountOnly=false,skipToEnd=false;
        byte[][] keys=null,values=null;
        PdfObject[] objs=null;
        
        if(pairs==-1){
            isCountOnly=true;
        }else if(pairs==-2){
            isCountOnly=true;
            skipToEnd=true;
        }else{
            keys=new byte[numberOfPairs][];
            values=new byte[numberOfPairs][];
            objs=new PdfObject[numberOfPairs];
            
            if(debug) {
                System.out.println("Loading " + numberOfPairs + " pairs");
            }
        }
        pairs=0;
        
        while(true){
            
            //move cursor to start of text
            start = getStart(data, start);

            //allow for comment
            if(data[start]==37){
                start = ArrayUtils.skipComment(data, start);
            }
            
            //exit at end
            if(data[start]==62) {
                break;
            }
            
            //count token or tell user problem
            if(data[start]==47){
                pairs++;
                start++;
            }else {
                throw new RuntimeException("Unexpected value " + data[start] + " - not key pair");
            }
            
            //read token key and save if on second run
            final int tokenStart=start;
            while(data[start]!=32 && data[start]!=10 && data[start]!=13 && data[start]!='[' && data[start]!='<' && data[start]!='/') {
                start++;
            }
            
            final int tokenLength=start-tokenStart;
            
            final byte[] tokenKey=new byte[tokenLength];
            System.arraycopy(data, tokenStart, tokenKey, 0, tokenLength);
            
            if(!isCountOnly) //pairs already rolled on so needs to be 1 less
            {
                keys[pairs - 1] = tokenKey;
            }

            start=ArrayUtils.skipSpaces(data,start);

            final boolean isDirect=data[start]==60 || data[start]=='[' || data[start]=='/';
            
            final byte[] dictData;
            
            if(debug) {
                System.out.println("token=" + new String(tokenKey) + " isDirect " + isDirect);
            }
            
            if(isDirect){
                //get to start at <<
                while(data[start-1]!='<' && data[start]!='<' && data[start]!='[' && data[start]!='/') {
                    start++;
                }
                
                final int streamStart=start;
                
                //find end
                boolean isObject=true;
                
                if(data[start]=='<'){
                    start += 2;
                    level=1;
                    
                    while(level>0){
                        //   System.out.print((char)data[start]);
                        if(data[start]=='<' && data[start+1]=='<'){
                            start += 2;
                            level++;
                        }else if(data[start]=='>' && data[start+1]=='>'){
                            start += 2;
                            level--;
                        }else {
                            start++;
                        }
                    }
                    
                    //System.out.println("\n<---------------"+start);
                    
                    //if(data[start]=='>' && data[start+1]=='>')
                    //start=start+2;
                }else if(data[start]=='['){
                    
                    level=1;
                    start++;
                    
                    boolean inStream=false;
                    
                    while(level>0){
                        
                        //allow for streams
                        if(!inStream && data[start]=='(') {
                            inStream = true;
                        } else if(inStream && data[start]==')' && (data[start-1]!='\\' || data[start-2]=='\\' )) {
                            inStream = false;
                        }
                        
                        //System.out.println((char)data[start]);
                        
                        if(!inStream){
                            if(data[start]=='[') {
                                level++;
                            } else if(data[start]==']') {
                                level--;
                            }
                        }
                        
                        start++;
                    }
                    
                    isObject=false;
                }else if(data[start]=='/'){
                    start++;
                    while(data[start]!='/' && data[start]!=10 && data[start]!=13 && data[start]!=32){
                        start++;
                        
                        if(start<data.length-1 && data[start]=='>' && data[start+1]=='>') {
                            break;
                        }
                    }
                }
                
                if(!isCountOnly){
                    final int len=start-streamStart;
                    dictData=new byte[len];
                    System.arraycopy(data, streamStart, dictData, 0, len);
                    //pairs already rolled on so needs to be 1 less
                    values[pairs-1]=dictData;
                    
                    final String ref=pdfObject.getObjectRefAsString();
                    
                    //@speed - will probably need to change as we add more items
                    
                    if(pdfObject.getObjectType()==PdfDictionary.ColorSpace){
                        
                        //isDirect avoids storing multiple direct objects as will overwrite each other
                        if(isObject && !isDirect){
                            ColorObjectDecoder.handleColorSpaces(pdfObject, 0,  dictData,objectReader);
                            objs[pairs-1]=pdfObject;
                        }else{
                            final ColorSpaceObject colObject=new ColorSpaceObject(ref);
                            
                            if(isDirect) {
                                colObject.setRef(-1, 0);
                            }
                            
                            ColorObjectDecoder.handleColorSpaces(colObject, 0,  dictData,objectReader);
                            objs[pairs-1]=colObject;
                        }
                        
                        //handleColorSpaces(-1, valueObj,ref, 0, dictData,debug, -1,null, paddingString);
                    }else if(isObject) {

                        final PdfObject valueObj = ObjectFactory.createObject(id, ref, pdfObject.getObjectType(), pdfObject.getID());
                        valueObj.setID(id);
                        readDictionaryFromRefOrDirect(id, valueObj, ref, 0, dictData, -1, objectReader);
                        objs[pairs - 1] = valueObj;
                    }
                }
                
            }else{ //its 50 0 R
                
                final int number;
                final int generation;
                final int refStart=start;
                int keyStart2=start;

                if(data[start]=='n' && data[start+1]=='u' && data[start+2]=='l' && data[start+3]=='l'){
                    start += 4;
                }else{
                    
                    //number
                    while(data[start]!=10 && data[start]!=13 && data[start]!=32 && data[start]!=47 &&
                            data[start]!=60 && data[start]!=62){
                        start++;
                    }
                    number= NumberUtils.parseInt(keyStart2, start, data);
                    
                    //generation
                    while(data[start]==10 || data[start]==13 || data[start]==32 || data[start]==47 || data[start]==60) {
                        start++;
                    }
                    
                    keyStart2=start;
                    //move cursor to end of reference
                    while(data[start]!=10 && data[start]!=13 && data[start]!=32 &&
                            data[start]!=47 && data[start]!=60 && data[start]!=62) {
                        start++;
                    }
                    
                    generation= NumberUtils.parseInt(keyStart2, start, data);
                    
                    //move cursor to start of R
                    while(data[start]==10 || data[start]==13 || data[start]==32 || data[start]==47 || data[start]==60) {
                        start++;
                    }
               
                    if(data[start]!=82){ //we are expecting R to end ref
                        throw new RuntimeException((char)data[start-1]+" "+(char)data[start]+ ' ' +(char)data[start+1]+" 3. Unexpected value in file - please send to IDRsolutions for analysis");
                    }
                    start++; //roll past
                    
                    if(debug) {
                        System.out.println("Data in object=" + number + ' ' + generation + " R");
                    }
                    
                    //read the Dictionary data
                    if(!isCountOnly){
                        
                        if(PdfDictionary.getKeyType(id, pdfObject.getObjectType())==PdfDictionary.VALUE_IS_UNREAD_DICTIONARY){
                            
                            
                            final String ref=new String(data, refStart,start-refStart);
                            
                            final PdfObject valueObj=ObjectFactory.createObject(id, ref, pdfObject.getObjectType(), pdfObject.getID());
                            
                            valueObj.setStatus(PdfObject.UNDECODED_REF);
                            valueObj.setUnresolvedData(StringUtils.toBytes(ref),id);
                            
                            objs[pairs-1]=valueObj;
                            
                        }else{
                            
                            final byte[] rawDictData=objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(number, generation), number, generation);
                            
                            //allow for data in Linear object not yet loaded
                            if(rawDictData==null){
                                pdfObject.setFullyResolved(false);
                                
                                if(LogWriter.isOutput()) {
                                    LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (12)");
                                }
                                
                                return data.length;
                            }
                            
                            if(debug){
                                System.out.println("============================================\n");
                                for(int aa=0;aa<rawDictData.length;aa++){
                                    System.out.print((char)rawDictData[aa]);
                                    
                                    if(aa>5 && rawDictData[aa-5]=='s' && rawDictData[aa-4]=='t' && rawDictData[aa-3]=='r'&& rawDictData[aa-2]=='e' && rawDictData[aa-1]=='a' && rawDictData[aa]=='m') {
                                        aa = rawDictData.length;
                                    }
                                }
                                System.out.println("\n============================================");
                            }
                            //cleanup
                            //lose obj at start
                            int jj=0;
                            
                            while(jj<3 ||(rawDictData[jj-1]!=106 && rawDictData[jj-2]!=98 && rawDictData[jj-3]!=111)){
                                
                                if(rawDictData[jj]=='/' || rawDictData[jj]=='[' || rawDictData[jj]=='<') {
                                    break;
                                }
                                
                                jj++;
                                
                                if(jj==rawDictData.length){
                                    jj=0;
                                    break;
                                }
                            }
                            
                            //skip any spaces after
                            while(rawDictData[jj]==10 || rawDictData[jj]==13 || rawDictData[jj]==32)// || data[j]==47 || data[j]==60)
                            {
                                jj++;
                            }
                            
                            final int len=rawDictData.length-jj;
                            dictData=new byte[len];
                            System.arraycopy(rawDictData, jj, dictData, 0, len);
                            //pairs already rolled on so needs to be 1 less
                            values[pairs-1]=dictData;
                            
                            final String ref=number+" "+generation+" R";//pdfObject.getObjectRefAsString();
                            
                            if(pdfObject.getObjectType()==PdfDictionary.Font && id==PdfDictionary.Font){//last condition for CharProcs
                                objs[pairs-1]=null;
                                values[pairs-1]=StringUtils.toBytes(ref);
                            }else if(pdfObject.getObjectType()==PdfDictionary.XObject){
                                //intel Unimplemented pattern type 0 in file
                                final PdfObject valueObj=ObjectFactory.createObject(id, ref, PdfDictionary.XObject, PdfDictionary.XObject);
                                valueObj.setStatus(PdfObject.UNDECODED_REF);
                                valueObj.setUnresolvedData(StringUtils.toBytes(ref),id);
                                
                                objs[pairs-1]=valueObj;
                            }else{
                                
                                //@speed - will probably need to change as we add more items
                                final PdfObject valueObj=ObjectFactory.createObject(id, ref, pdfObject.getObjectType(), pdfObject.getID());
                                valueObj.setID(id);
                                if(debug){
                                    System.out.println(ref+" ABOUT TO READ OBJ for "+valueObj+ ' ' +pdfObject);
                                    
                                    System.out.println("-------------------\n");
                                    for(int aa=0;aa<dictData.length;aa++){
                                        System.out.print((char)dictData[aa]);
                                        
                                        if(aa>5 && dictData[aa-5]=='s' && dictData[aa-4]=='t' && dictData[aa-3]=='r'&& dictData[aa-2]=='e' && dictData[aa-1]=='a' && dictData[aa]=='m') {
                                            aa = dictData.length;
                                        }
                                    }
                                    System.out.println("\n-------------------");
                                }
                                
                                if(valueObj.getObjectType()==PdfDictionary.ColorSpace){
                                    ColorObjectDecoder.handleColorSpaces(valueObj, 0,  dictData,objectReader);
                                }else {
                                    readDictionaryFromRefOrDirect(id, valueObj, ref, 0, dictData, -1, objectReader);
                                }
                                
                                objs[pairs-1]=valueObj;
                                
                            }
                        }
                    }
                }
            }
        }
        
        
        if(!isCountOnly) {
            pdfObject.setDictionaryPairs(keys, values, objs);
        }
        
        if(debug) {
            System.out.println("done=============================================");
        }
        
        if(skipToEnd || !isCountOnly) {
            return start;
        } else {
            return pairs;
        }
        
    }

    private static int getStart(byte[] data, int start) {

        byte b=data[start];
        while(b ==9 || b ==10 || b ==13 || b ==32 || b ==60) {
            start++;
            b=data[start];
        }
        return start;
    }

    /**
     * @param id
     * @param pdfObject
     * @param objectRef
     * @param i
     * @param raw
     * @param PDFkeyInt - -1 will store in pdfObject directly, not as separate object
     * @return
     */
    public static int readDictionaryFromRefOrDirect(final int id, final PdfObject pdfObject, final String objectRef, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {
        
        readDictionaryFromRefOrDirect:
        while (true) {
            
            int keyStart;
            int possibleArrayStart = -1;
            
            //@speed - find end so we can ignore once no longer reading into map as well
            //and skip to end of object
            //allow for [ref] or [<< >>] at top level (may be followed by gap)
            //good example is /PDFdata/baseline_screens/docusign/test3 _ Residential Purchase and Sale Agreement - 6-03.pdf
            while (raw[i] == 91 || raw[i] == 32 || raw[i] == 13 || raw[i] == 10) {
                
                if (raw[i] == 91) //track incase /Mask [19 19]
                {
                    possibleArrayStart = i;
                }
                
                i++;
            }
            
            //some items like MAsk can be [19 19] or stream
            //and colorspace is law unto itself
            if (PDFkeyInt == PdfDictionary.ColorSpace || id == PdfDictionary.ColorSpace || pdfObject.getPDFkeyInt() == PdfDictionary.ColorSpace){
                return ColorObjectDecoder.processColorSpace(pdfObject, pdfObject.getObjectRefAsString(), i, raw,objectReader);
            }else if (possibleArrayStart != -1 && (PDFkeyInt == PdfDictionary.Mask || PDFkeyInt == PdfDictionary.TR || PDFkeyInt == PdfDictionary.OpenAction)) {
                return Array.processArray(pdfObject, raw, PDFkeyInt, possibleArrayStart, objectReader);
            }
            
            if (raw[i] == '%') { // if %comment roll onto next line
                while (raw[i] != 13 && raw[i] != 10) {
                    i++;
                }
                
                //and lose space after
                while (raw[i] == 91 || raw[i] == 32 || raw[i] == 13 || raw[i] == 10) {
                    i++;
                }
            }
            
            if (raw[i] == 60) { //[<<data inside brackets>>]
                
                i =  DirectDictionaryToObject.convert(pdfObject, objectRef, i, raw, PDFkeyInt,objectReader);
                
            } else if (raw[i] == 47) { //direct value such as /DeviceGray
                
                i = ObjectUtils.setDirectValue(pdfObject, i, raw, PDFkeyInt);
                
            } else { // ref or [ref]
                
                int j = i, ref, generation;
                byte[] data = raw;
                
                while (true) {
                    
                    //allow for [ref] at top level (may be followed by gap
                    while (data[j] == 91 || data[j] == 32 || data[j] == 13 || data[j] == 10) {
                        j++;
                    }
                    
                    
                    //trap empty arrays ie [ ]
                    //ie 13jun/Factuur 2106010.PDF
                    if (data[j] == ']') {
                        return j;
                    }
                    
                    // trap nulls  as well
                    boolean hasNull = false;
                    
                    while (true) {
                        
                        //trap null arrays ie [null null]
                        if (hasNull && data[j] == ']') {
                            return j;
                        }
                        
                        /**
                         * get object ref
                         */
                        keyStart = j;
                        //move cursor to end of reference
                        while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                            
                            //trap null arrays ie [null null] or [null]
                            
                            if (data[j] == 'l' && data[j - 1] == 'l' && data[j - 2] == 'u' && data[j - 3] == 'n') {
                                hasNull = true;
                            }
                            
                            if (hasNull && data[j] == ']') {
                                return j;
                            }
                            
                            j++;
                        }
                        
                        ref = NumberUtils.parseInt(keyStart, j, data);
                        
                        //move cursor to start of generation or next value
                        while (data[j] == 10 || data[j] == 13 || data[j] == 32)// || data[j]==47 || data[j]==60)
                        {
                            j++;
                        }
                        
                        //handle nulls
                        if (ref != 69560 || data[keyStart] != 'n') {
                            break; //not null
                        } else {
                            hasNull = true;
                            if (data[j] == '<') { // /DecodeParms [ null << /K -1 /Columns 1778 >>  ] ignore null and jump down to enclosed Dictionary
                                i = j;
                                continue readDictionaryFromRefOrDirect;
                                
                            }
                        }
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
                    
                    //read the Dictionary data
                    data = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);
                    
                    //allow for data in Linear object not yet loaded
                    if (data == null) {
                        pdfObject.setFullyResolved(false);
                        
                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (11)");
                        }
                        
                        return raw.length;
                    }
                    
                    //disregard corrputed data from start of file
                    if (data != null && data.length > 4 && data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F') {
                        data = null;
                    }
                    
                    if (data == null) {
                        break;
                    }
                    
                    /**
                     * get not indirect and exit if not
                     */
                    int j2 = 0;
                    
                    //allow for [91 0 r]
                    if (data[j2] != '[' && data[0] != '<' && data[1] != '<') {
                        
                        while (j2 < 3 || (j2 > 2 && data[j2 - 1] != 106 && data[j2 - 2] != 98 && data[j2 - 3] != 111)) {
                            
                            //allow for /None as value
                            if (data[j2] == '/') {
                                break;
                            }
                            j2++;
                        }
                        
                        //skip any spaces
                        while (data[j2] != 91 && (data[j2] == 10 || data[j2] == 13 || data[j2] == 32))// || data[j]==47 || data[j]==60)
                        {
                            j2++;
                        }
                    }
                    
                    //if indirect, round we go again
                    if (data[j2] != 91) {
                        j = 0;
                        break;
                    }else if(data[j2]=='[' && data[j2+1]=='<'){
                        j2++;
                        j=j2;
                        break;
                    }
                    
                    j = j2;
                }
                
                //allow for no data found (ie /PDFdata/baseline_screens/debug/hp_broken_file.pdf)
                if (data != null) {
                    
                    /**
                     * get id from stream
                     */
                    //skip any spaces
                    while (data[j] == 10 || data[j] == 13 || data[j] == 32)// || data[j]==47 || data[j]==60)
                    {
                        j++;
                    }
                    
                    boolean isMissingValue = j < raw.length && raw[j] == '<';
                    
                    if (isMissingValue) { //check not <</Last
                        //find first valid char
                        int xx = j;
                        while (xx < data.length && (raw[xx] == '<' || raw[xx] == 10 || raw[xx] == 13 || raw[xx] == 32)) {
                            xx++;
                        }
                        
                        if (raw[xx] == '/') {
                            isMissingValue = false;
                        }
                    }
                    
                    if (isMissingValue) { //missing value at start for some reason
                        
                        /**
                         * get object ref
                         */
                        keyStart = j;
                        //move cursor to end of reference
                        while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                            j++;
                        }
                        
                        ref = NumberUtils.parseInt(keyStart, j, data);
                        
                        //move cursor to start of generation
                        while (data[j] == 10 || data[j] == 13 || data[j] == 32 || data[j] == 47 || data[j] == 60) {
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
                        
                        //lose obj at start
                        while (data[j - 1] != 106 && data[j - 2] != 98 && data[j - 3] != 111) {
                            
                            if (data[j] == '<') {
                                break;
                            }
                            
                            j++;
                        }
                    }
                    
                    //skip any spaces
                    while (data[j] == 10 || data[j] == 13 || data[j] == 32 || data[j] == 9)// || data[j]==47 || data[j]==60)
                    {
                        j++;
                    }
                    
                    //move to start of Dict values
                    if (data[0] != 60) {
                        while (data[j] != 60 && data[j + 1] != 60) {

                            //allow for null object
                            if (data[j] == 'n' && data[j + 1] == 'u' && data[j + 2] == 'l' && data[j + 3] == 'l') {
                                return i;
                            }

                            //allow for Direct value ie 2 0 obj /WinAnsiEncoding
                            if (data[j] == 47) {
                                break;
                            }

                            //allow for textStream (text)
                            if (data[j] == '(') {
                                j = TextStream.readTextStream(pdfObject, j, data, PDFkeyInt, true, objectReader);
                                break;
                            }

                            j++;
                        }
                    }
                    
                    i = ObjectDecoder.handleValue(pdfObject, i, PDFkeyInt, j, ref, generation, data,objectReader);
                }
            }
            
            return i;
        }
    }
    
   
   
    
}


