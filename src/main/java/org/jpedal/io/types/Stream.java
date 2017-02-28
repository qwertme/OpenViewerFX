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
 * Stream.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.constants.PDFflags;
import org.jpedal.io.DecryptionFactory;
import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.PdfFileReader;
import org.jpedal.io.PdfFilteredReader;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 *
 */
public class Stream {

     
    public static void readStreamData(final PdfObject pdfObject, final int i, final byte[] raw, final int length, final PdfFileReader objectReader) {
        
        for(int xx=i;xx<length-5;xx++){
        
            //avoid reading on subobject ie <<  /DecodeParams << >> >>
            if(raw[xx]=='>' && raw[xx+1]=='>') {
                break;
            }
            
            if(raw[xx] == 's' && raw[xx + 1] == 't' && raw[xx + 2] == 'r' &&
                    raw[xx + 3] == 'e' && raw[xx + 4] == 'a' &&
                    raw[xx + 5] == 'm'){
                
                if(debugFastCode) {
                    System.out.println(padding + "1. Stream found afterwards");
                }
                
                if(!pdfObject.isCached()) {
                    Stream.readStreamIntoObject(pdfObject, xx, raw, objectReader);
                }
                
                xx=length;
            }
        }
    }
    
    public static void readStreamIntoObject(final PdfObject pdfObject, final int j, final byte[] data, final PdfFileReader objectReader) {
        
        final DecryptionFactory decryption=objectReader.getDecryptionObject();
        
        final int count=data.length;
        
        if(debugFastCode) {
            System.out.println(padding + "Looking for stream");
        }
        
        byte[] stream=null;
        
        /**
         * see if JBIG encoded
         */
        final PdfArrayIterator maskFilters = pdfObject.getMixedArray(PdfDictionary.Filter);
        
        //get type as need different handling
        boolean isJBigEncoded =false;
        int firstMaskValue=PdfDictionary.Unknown;
        if(maskFilters!=null && maskFilters.hasMoreTokens()){
            
            firstMaskValue=maskFilters.getNextValueAsConstant(true);
            
            if(firstMaskValue==PdfFilteredReader.JBIG2Decode) {
                isJBigEncoded = true;
            }
            
            while(maskFilters.hasMoreTokens() && !isJBigEncoded){
                firstMaskValue=maskFilters.getNextValueAsConstant(true);
                if(firstMaskValue==PdfFilteredReader.JBIG2Decode) {
                    isJBigEncoded = true;
                }
            }
        }
        
        
        for(int a=j;a<count;a++){
            if ((data[a] == 115)&& (data[a + 1] == 116)&& (data[a + 2] == 114)&&
                    (data[a + 3] == 101)&& (data[a + 4] == 97)&& (data[a + 5] == 109)) {
                
                
                //ignore these characters and first return
                a += 6;
                
                while(data[a]==32) {
                    a++;
                }
                
                if (data[a] == 13 && data[a+1] == 10) //allow for double linefeed
                {
                    a += 2;
                }//see /PDFdata/baseline_screens/11jun/Agency discl. Wabash.pdf
                else if (data[a] == 10 && data[a+1] == 10 && data[a+2] == 10 && data[a+3] == -1 && firstMaskValue==PdfFilteredReader.DCTDecode){ //allow for double linefeed on jpeg
                    a += 3;
                }else if (data[a] == 10 && data[a+1] == 10 && data[a+2] == -1 && firstMaskValue==PdfFilteredReader.DCTDecode){ //allow for double linefeed on jpeg
                    a += 2;
                }else if(data[a]==10 || data[a]==13) {
                    a++;
                }
                
                final int start = a;
                
                
                a--; //move pointer back 1 to allow for zero length stream
                
                /**
                 * if Length set and valid use it
                 */
                int streamLength=0;
                final int setStreamLength=pdfObject.getInt(PdfDictionary.Length);
                
                if(debugFastCode) {
                    System.out.println(padding + "setStreamLength=" + setStreamLength);
                }
                
                boolean	isValid=false;
                
                if(setStreamLength!=-1){
                    
                    streamLength=setStreamLength;
                    
                    //System.out.println("1.streamLength="+streamLength);
                    
                    a=start+streamLength;
                    
                    if(a<count && data[a]==13 && (a+1<count) && data[a+1]==10) {
                        a += 2;
                    }
                    
                    //check validity
                    if (count>(a+9) && data[a] == 101 && data[a + 1] == 110 && data[a + 2] == 100 &&
                            data[a + 3] == 115 && data[a + 4] == 116
                            && data[a + 5] == 114 && data[a + 6] == 101 && data[a + 7] == 97 && data[a + 8] == 109){
                        
                    }else{
                        
                        final int current=a;
                        //check forwards
                        if(a<count){
                            while(true){
                                a++;
                                if(isValid || a==count) {
                                    break;
                                }
                                
                                if (count-a>9 && data[a] == 101 && data[a + 1] == 110 && data[a + 2] == 100 && data[a + 3] == 115 && data[a + 4] == 116
                                        && data[a + 5] == 114 && data[a + 6] == 101 && data[a + 7] == 97 && data[a + 8] == 109){
                                    
                                    streamLength=a-start;
                                    isValid=true;
                                }
                            }
                        }
                        
                        if(!isValid){
                            a=current;
                            if(a>count) {
                                a = count;
                            }
                            //check backwords
                            while(true){
                                a--;
                                if(isValid || a<0) {
                                    break;
                                }
                                
                                if (data[a] == 101 && data[a + 1] == 110 && data[a + 2] == 100 && data[a + 3] == 115 && data[a + 4] == 116
                                        && data[a + 5] == 114 && data[a + 6] == 101 && data[a + 7] == 97 && data[a + 8] == 109){
                                    streamLength=a-start;
                                    isValid=true;
                                }
                            }
                        }
                    }
                    
                    //use correct figure if encrypted
                    if(decryption!=null && decryption.getBooleanValue(PDFflags.IS_FILE_ENCRYPTED)) {
                        streamLength = setStreamLength;
                    }
                    
                }else{
                    
                    /**workout length and check if length set*/
                    final int end;
                    
                    while (true) { //find end
                        
                        a++;
                        
                        if(a==count) {
                            break;
                        }
                        if (data[a] == 101 && data[a + 1] == 110 && data[a + 2] == 100 && data[a + 3] == 115 && data[a + 4] == 116
                                && data[a + 5] == 114 && data[a + 6] == 101 && data[a + 7] == 97 && data[a + 8] == 109) {
                            break;
                        }
                        
                    }
                    
                    end=a-1;
                    
                    if((end>start)) {
                        streamLength = end - start + 1;
                    }
                }
                
                //lose trailing 10s or 13s
                if(streamLength>1 && !(decryption!=null && decryption.getBooleanValue(PDFflags.IS_FILE_ENCRYPTED))){// && !isValid){
                    final int ptr=start+streamLength-1;
                    
                    if(ptr<data.length && ptr>0 && (data[ptr]==10 || (data[ptr]==13 && ((pdfObject!=null && isJBigEncoded)||(ptr>0 && data[ptr-1]==10))))){
                        streamLength--;
                    }
                }
                
                /**
                 * read stream into object from memory
                 */
                if(start+streamLength>count) {
                    streamLength = count - start;
                }
                
                //@speed - switch off and investigate
                if(streamLength<0) {
                    return;
                }
                
                if(streamLength<0) {
                    throw new RuntimeException("Negative stream length " + streamLength + " start=" + start + " count=" + count);
                }
                stream = new byte[streamLength];
                System.arraycopy(data, start, stream, 0, streamLength);
                
                
                a=count;
            }
            
        }
        
        if(debugFastCode && stream!=null) {
            System.out.println(padding + "stream read saved into " + pdfObject);
        }
        
        if(pdfObject!=null){
            
            pdfObject.setStream(stream);
            
            //and decompress now forsome objects
            if(pdfObject.decompressStreamWhenRead()) {
                objectReader.readStream(pdfObject, true, true, false, pdfObject.getObjectType() == PdfDictionary.Metadata, pdfObject.isCompressedStream(), null);
            }
            
        }
    }
   
}


