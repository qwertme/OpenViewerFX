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
 * HTMLStructuredContentHandler.java
 * ---------------
 */

package org.jpedal.objects.structuredtext;

import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;

/**
 *
 * @author markee
 */
public class HTMLStructuredContentHandler extends StructuredContentHandler {

   

    public HTMLStructuredContentHandler(Object markedContent, DynamicVectorRenderer current) {

            super(markedContent);
            
            this.current=current;
            
            isHTML=true;
        
    }
    
    @Override
    public void BDC(final PdfObject BDCobj) {
        
        super.BDC(BDCobj);
        	
        if(BDCobj!=null){
            setHTMLTag(BDCobj,true);
        }
    }

    private void setHTMLTag(final PdfObject BDCobj, boolean isOpenTag) {
        

        final int MCID=BDCobj.getInt(PdfDictionary.MCID);
        
        String param=(String) values.get(String.valueOf(MCID));
        
        if(isOpenTag){
            current.writeCustom(DynamicVectorRenderer.TEXT_STRUCTURE_OPEN, param);
       //     System.out.println("Open "+MCID+" "+param+" "+current);
        }else{
            current.writeCustom(DynamicVectorRenderer.TEXT_STRUCTURE_CLOSE, param);
        //     System.out.println("Close "+MCID+" "+param+" "+current);
        }
        
        /**
        //any custom tags
        if(isOpenTag && BDCobj!=null){
            final Map metadata=BDCobj.getOtherDictionaries();
            if(metadata!=null){
                final Iterator customValues=metadata.keySet().iterator();
                Object key;
                while(customValues.hasNext()){
                    key=customValues.next();
                    
                    current.writeCustom(OutputDisplay.TEXT,key.toString()+"="+metadata.get(key).toString());
                    
                    //if(addCoordinates){
                       root.setAttribute("x1", String.valueOf((int) x1));
                       root.setAttribute("y1", String.valueOf((int) y1));
                        root.setAttribute("x2", String.valueOf((int) x2));
                        root.setAttribute("y2", String.valueOf((int) y2));
                   // }
                }
            }
        }/**/
    }
    
    @Override
    public void BMC(final String op) {

        setBMCvalues(op);

        PdfObject BDCobj=(PdfObject) dictionaries.get(String.valueOf(markedContentLevel));
    	
        if(BDCobj!=null){
            setHTMLTag(BDCobj,true);
        }
	}
    
    @Override
    public void EMC() {

        setEMCValues();
        
    	PdfObject BDCobj=(PdfObject) dictionaries.get(String.valueOf(markedContentLevel));
    	
        if(BDCobj!=null){
            setHTMLTag(BDCobj,false);
        }
    }
}
