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
 * NameLookup.java
 * ---------------
 */
package org.jpedal.io;

import org.jpedal.objects.Javascript;
import org.jpedal.objects.raw.NamesObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.StringUtils;

import java.util.HashMap;
import org.jpedal.objects.raw.XObject;

/**
 * convert names to refs
 */
public class NameLookup extends HashMap {

    private final PdfFileReader objectReader;

    /**
     *
     * @param objectReader
     */
    public NameLookup(final PdfFileReader objectReader) {

        this.objectReader=objectReader;

    }

    /**
     * read any names
     * @param nameObject
     * @param javascript
     * @param isKid
     */
    public void readNames(final PdfObject nameObject, final Javascript javascript, final boolean isKid){

        final ObjectDecoder objectDecoder=new ObjectDecoder(objectReader);
        objectDecoder.checkResolved(nameObject);

        /**
         *  loop to read required values into lookup
         */
        final int[] nameLists= {PdfDictionary.Dests, PdfDictionary.JavaScript,PdfDictionary.XFAImages};
        int count=nameLists.length;
        if(isKid) {
            count = 1;
        }

        PdfObject pdfObj;
        PdfArrayIterator namesArray;

        String name,value;

        for(int ii=0;ii<count;ii++){

            if(isKid) {
                pdfObj = nameObject;
            } else {
                pdfObj = nameObject.getDictionary(nameLists[ii]);
            }

            if(pdfObj==null) {
                continue;
            }
            
            //any kids
            final byte[][] kidList = pdfObj.getKeyArray(PdfDictionary.Kids);
            if(kidList!=null){
                final int kidCount=kidList.length;

                /** allow for empty value and put next pages in the queue */
                if (kidCount> 0) {

                    for (final byte[] aKidList : kidList) {

                        final String nextValue = new String(aKidList);

                        final PdfObject nextObject = new NamesObject(nextValue);
                        nextObject.ignoreRecursion(false);

                        objectReader.readObject(nextObject);

                        readNames(nextObject, javascript, true);
                    }
                }
            }

            //get any names object
            namesArray = pdfObj.getMixedArray(PdfDictionary.Names);

            //read all the values
            if (namesArray != null && namesArray.getTokenCount()>0) {
                while (namesArray.hasMoreTokens()) {
                    name =namesArray.getNextValueAsString(true);

                    //fix for baseline_screens/11jun/Bundy_vs_F_Kruger_Sons_Bundy_v_F_Kruger!~!2200.pdf
                    //as code assumes paired values and not in this file (List a list)
                    if(!namesArray.hasMoreTokens()) {
                        continue;
                    }

                    value =namesArray.getNextValueAsString(true);

                    switch(nameLists[ii]){
                        //if Javascript, get full value and store, otherwise just get name
                        case PdfDictionary.JavaScript:
                            setJavaScriptName(value, objectDecoder, javascript, name);
                            break;
                            
                        case PdfDictionary.XFAImages:
                            setXFAImage(value, objectDecoder, name);
                            break;
                            
                        default: //just store
                            this.put(name, value);
                    }
                }
            }
        }
    }

    private void setXFAImage(final String value, final ObjectDecoder objectDecoder, final String name) {
        
        final PdfObject XFAImagesObj=new XObject(value);
        XFAImagesObj.decompressStreamWhenRead();
        final byte[] xfaData=StringUtils.toBytes(value);
        if(xfaData[0]=='<') {
            XFAImagesObj.setStatus(PdfObject.UNDECODED_DIRECT);
        } else {
            XFAImagesObj.setStatus(PdfObject.UNDECODED_REF);
        }
        
        if(value.contains(" ") || value.contains("<")){
            
            //must be done AFTER setStatus()
            XFAImagesObj.setUnresolvedData(xfaData, PdfDictionary.XObject);
            objectDecoder.checkResolved(XFAImagesObj);

            final byte[] decodedImageData=objectReader.readStream(XFAImagesObj, true, true, false, false, false, null);

            this.put(name,decodedImageData);
        }
    }

    static void setJavaScriptName(final String value, final ObjectDecoder objectDecoder, final Javascript javascript, final String name) {
        final String JSstring;
        
        final PdfObject javascriptObj=new NamesObject(value);
        final byte[] jsData=StringUtils.toBytes(value);
        if(jsData[0]=='<') {
            javascriptObj.setStatus(PdfObject.UNDECODED_DIRECT);
        } else {
            javascriptObj.setStatus(PdfObject.UNDECODED_REF);
        }
        
        if(value.contains(" ") || value.contains("<")){
            //must be done AFTER setStatus()
            javascriptObj.setUnresolvedData(jsData, PdfDictionary.JS);
            objectDecoder.checkResolved(javascriptObj);
            
            
            final PdfObject JS=javascriptObj.getDictionary(PdfDictionary.JS);
            if(JS!=null){ //in stream
                JSstring=new String(JS.getDecodedStream());
            }else{ //can also be text
                JSstring=javascriptObj.getTextStreamValue((PdfDictionary.JS));
            }
        }else{
            JSstring=value;
        }
        
        if(JSstring!=null){
            //store
            javascript.setCode(name, JSstring);
        }
    }

}
