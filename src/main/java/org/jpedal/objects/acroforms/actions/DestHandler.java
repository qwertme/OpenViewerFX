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
 * DestHandler.java
 * ---------------
 */
package org.jpedal.objects.acroforms.actions;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.types.Array;
import org.jpedal.objects.raw.OutlineObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.StringUtils;

/**
 *
 */
public class DestHandler {

    
    public static PdfArrayIterator getDestFromObject(PdfObject aData) {
        
        //aData can either be in top level of Form (as in Annots) and in A or AA
        //or second level (as in A/ /D - this allows for both
        //which this routine handles
        PdfObject a2=aData.getDictionary(PdfDictionary.A);
        if(a2==null) {
            a2 = aData.getDictionary(PdfDictionary.AA);
        }
        if(a2!=null) {
            aData = a2;
        }
        //allow for D as indirect
        final PdfObject Dobj=aData.getDictionary(PdfDictionary.D);
        if(Dobj!=null) {
            aData = Dobj;
        }
        
        return aData.getMixedArray(PdfDictionary.Dest);
        
    }
    
    /**
     * Update corresponding Dest object with: Dest = aData.getMixedArray(PdfDictionary.Dest); 
     * @param Dest
     * @param aData
     * @return 
     */
    public static PdfObject getIndirectDest(final PdfObjectReader currentPdfFile, final PdfArrayIterator Dest, final PdfObject aData){
        final boolean debugDest = false;
        final PdfObject newObj;
        final String ref=currentPdfFile.convertNameToRef( Dest.getNextValueAsString(false));
        if(ref!=null){

            //can be indirect object stored between []
            if(ref.charAt(0)=='['){
                if(debugDest) {
                    System.out.println("data for named obj " + ref);
                }

                final byte[] raw=StringUtils.toBytes(ref);
                //replace char so subroutine works -ignored but used as flag in routine
                raw[0]= 0;

                final Array objDecoder=new Array(currentPdfFile.getObjectReader(), 0, raw.length, PdfDictionary.VALUE_IS_MIXED_ARRAY,null, PdfDictionary.Names);
                objDecoder.readArray(false, raw, aData, PdfDictionary.Dest);
                newObj = aData;
            }else{
                if(debugDest) {
                    System.out.println("convert named obj " + ref);
                }

                newObj=new OutlineObject(ref);
                currentPdfFile.readObject(newObj);
            }
        }else{
            newObj = aData;
        }
        
        return newObj;
    }
    
}


