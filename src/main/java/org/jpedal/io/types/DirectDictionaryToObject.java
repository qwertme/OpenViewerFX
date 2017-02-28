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
 * DirectDictionaryToObject.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.io.ObjectDecoder;
import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.ObjectFactory;
import org.jpedal.objects.raw.PdfObject;

/**
 *
 */
public class DirectDictionaryToObject {

    
    public static int convert(final PdfObject pdfObject, String objectRef, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {
        
        //convert data to new Dictionary object
        final PdfObject valueObj ;
        
        if(PDFkeyInt==-1){
            valueObj=pdfObject;
            
            //if only 1 item use that ref not parent and indirect (ie <</Metadata 38 0 R>>)
            int objCount=0, refStarts=-1,refEnds=-1;
            if(raw[0]=='<'){
                for(int ii=0;ii<raw.length;ii++){
                    
                    //avoid binary data
                    if(raw[ii]=='s' && raw[ii+1]=='t' && raw[ii+2]=='r' && raw[ii+3]=='e' && raw[ii+4]=='a' && raw[ii+5]=='m') {
                        break;
                    }
                    
                    //count keys
                    if(raw[ii]=='/' && raw[ii+1]!='L' && raw[ii+2]!='e' && raw[ii+3]!='n' ) {
                        objCount++;
                    }
                    //find start of ref
                    if(objCount==1){
                        if(refStarts==-1){
                            if(raw[ii]>'0' && raw[ii]<'9') {
                                refStarts = ii;
                            }
                        }else{
                            if(raw[ii]=='R') {
                                refEnds = ii + 1;
                            }
                        }
                    }
                }
                
                if(objCount==1 && refStarts!=-1 && refEnds!=-1){
                    objectRef=new String(raw,refStarts,refEnds-refStarts);
                    valueObj.setRef(objectRef);
                }
            }
            
        }else{
            valueObj= ObjectFactory.createObject(PDFkeyInt,objectRef, pdfObject.getObjectType(), pdfObject.getID());
            valueObj.setInCompressedStream(pdfObject.isInCompressedStream());
            valueObj.setID(PDFkeyInt);
            
            //if it is cached, we need to copy across data so in correct Obj
            // (we read data before we created object so in wrong obj at this point)
            if(pdfObject.isCached()) {
                valueObj.moveCacheValues(pdfObject);
            }
            
            if(debugFastCode) {
                System.out.println("valueObj=" + valueObj + " pdfObject=" + pdfObject + " PDFkeyInt=" + PDFkeyInt + ' ' + pdfObject.getID() + ' ' + pdfObject.getParentID());
            }
        }
        
        //
        
        if(debugFastCode) {
            System.out.println(padding + "Reading [<<data>>] to " + valueObj + " into " + pdfObject + " i=" + i);
        }
        
        final ObjectDecoder objDecoder=new ObjectDecoder(objectReader);
        i=objDecoder.readDictionaryAsObject( valueObj, i, raw);
        
        //needed to ensure >>>> works
        if(i<raw.length && raw[i]=='>') {
            i--;
        }
        
        if(debugFastCode){
            System.out.println(padding+"data "+valueObj+" into pdfObject="+pdfObject+" i="+i);
        }
        
        //store value (already set above for -1
        if(PDFkeyInt!=-1) {
            pdfObject.setDictionary(PDFkeyInt, valueObj);
        }
        
        //roll on to end
        final int count=raw.length;
        while( i<count-1 && raw[i]==62  && raw[i+1]==62){ //
            i++;
            if(i+1<raw.length && raw[i+1]==62) //allow for >>>>
            {
                break;
            }
        }
        return i;
    }
    
}


