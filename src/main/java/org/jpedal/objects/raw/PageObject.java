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
 * PageObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class PageObject extends PdfObject {
	
    private byte[][] Annots, Contents, Kids, OpenAction;
    
    PdfObject AA, AcroForm, Group, OCProperties, O, OpenActionDict, PO, Properties, PV, Metadata, Outlines, Pages, MarkInfo, Names,StructTreeRoot;
    
    private int StructParents=-1, pageMode=-1;

    public PageObject(final String ref) {
        super(ref);
    }

    public PageObject(final int ref, final int gen) {
       super(ref,gen);
    }

    @Override
    public int getObjectType() {
		return PdfDictionary.Page;
	}
    
    @Override
    public boolean getBoolean(final int id){

        switch(id){
	
//        case PdfDictionary.EncodedByteAlign:
//        	return EncodedByteAlign; 

            default:
            	return super.getBoolean(id);
        }

    }
    
    @Override
    public void setBoolean(final int id, final boolean value){

        switch(id){
        	
//        case PdfDictionary.EncodedByteAlign:
//        	EncodedByteAlign=value;
//        	break;

            default:
                super.setBoolean(id, value);
        }
    }

    @Override
    public PdfObject getDictionary(final int id){

        switch(id){

            case PdfDictionary.AA:
                return AA;

            case PdfDictionary.AcroForm:
	        	return AcroForm;

            case PdfDictionary.Group:
	        	return Group;

            case PdfDictionary.MarkInfo:
                return MarkInfo;

            case PdfDictionary.Metadata:
	        	return Metadata;

            case PdfDictionary.O:
	        	return O;

            case PdfDictionary.OpenAction:
	        	return OpenActionDict;

            case PdfDictionary.OCProperties:
                return OCProperties;

            case PdfDictionary.Outlines:
	        	return Outlines;

            case PdfDictionary.Pages:
                return Pages;

            case PdfDictionary.PO:
	        	return PO;

            case PdfDictionary.Properties:
                return Properties;

            case PdfDictionary.PV:
	        	return PV;

            case PdfDictionary.Names:
	        	return Names;

            case PdfDictionary.StructTreeRoot:
                return StructTreeRoot;

            default:
            	return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value){

    	switch(id){

    	case PdfDictionary.StructParents:
    		StructParents=value;
    		break;
    		
    	default:
    		super.setIntNumber(id, value);
    	}
    }

    @Override
    public int getInt(final int id){

        switch(id){

        case PdfDictionary.StructParents:
        	return StructParents;

            default:
            	return super.getInt(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value){

    	value.setID(id);
    	
        switch(id){

            case PdfDictionary.AA:
                AA=value;
            break;

            case PdfDictionary.AcroForm:
	        	AcroForm=value;
	        break;

            case PdfDictionary.Group:
	        	Group=value;
	        break;

            case PdfDictionary.OCProperties:
                OCProperties=value;
            break;

            case PdfDictionary.MarkInfo:
	        	MarkInfo=value;
	        break;

            case PdfDictionary.Metadata:
	        	Metadata=value;
	        break;

            case PdfDictionary.O:
                O=value;
            break;

            case PdfDictionary.OpenAction:
                OpenActionDict=value;
            break;

            case PdfDictionary.Outlines:
	        	Outlines=value;
	        break;

            case PdfDictionary.Pages:
                Pages=value;
            break;

            case PdfDictionary.PO:
                PO=value;
            break;

            case PdfDictionary.Properties:
                Properties=value;
            break;

            case PdfDictionary.PV:
                PV=value;
            break;

            case PdfDictionary.Names:
	        	Names=value;
	        break;

            case PdfDictionary.StructTreeRoot:
	        	StructTreeRoot=value;
	        break;

            default:
            	super.setDictionary(id, value);
        }
    }


    @Override
    public int setConstant(final int pdfKeyType, final int keyStart, final int keyLength, final byte[] raw) {

        int PDFvalue =PdfDictionary.Unknown;

        int id=0,x=0,next;

        //convert token to unique key which we can lookup

        for(int i2=keyLength-1;i2>-1;i2--){

            next=raw[keyStart+i2];

            next -= 48;

            id += ((next)<<x);

            x += 8;
        }

        switch(id){

            case PdfDictionary.Page:
                return super.setConstant(pdfKeyType,PdfDictionary.Page);

            case PdfDictionary.Pages:
                return super.setConstant(pdfKeyType,PdfDictionary.Pages);

            case PdfDictionary.PageMode:
                pageMode=id;
                break;

            default:

                PDFvalue=super.setConstant(pdfKeyType,id);

                if(PDFvalue==-1 && debug){

                        final byte[] bytes=new byte[keyLength];

                        System.arraycopy(raw,keyStart,bytes,0,keyLength);
                        System.out.println("key="+new String(bytes)+ ' ' +id+" not implemented in setConstant in "+this);

                        System.out.println("final public static int "+new String(bytes)+ '=' +id+ ';');
                        
                    }
                
                break;

        }


        //System.out.println(pdfKeyType+"="+PDFvalue);
     //   switch(pdfKeyType){

//        	case PdfDictionary.BaseEncoding:
//        		BaseEncoding=PDFvalue;
//        		break;

     //   }

        return PDFvalue;
    }

    @Override
    public int getParameterConstant(final int key) {

        switch(key){

                case PdfDictionary.PageMode:
                    return pageMode;


//            case PdfDictionary.BaseEncoding:
//
//            	//special cases first
//            	if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isZapfDingbats)
//            		return StandardFonts.ZAPF;
//            	else if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isSymbol)
//            		return StandardFonts.SYMBOL;
//            	else
//            		return BaseEncoding;

            //check general values
            default:
                return super.getParameterConstant(key);
        }
    }

    @Override
    public PdfArrayIterator getMixedArray(final int id) {

    	switch(id){

            case PdfDictionary.OpenAction:
                return new PdfArrayIterator(OpenAction);

            default:
            	return super.getMixedArray(id);
        }

	}

    @Override
    public double[] getDoubleArray(final int id) {

        switch(id){
            default:
               return super.getDoubleArray(id);

        }
    }

    @Override
    public void setDoubleArray(final int id, final double[] value) {

        switch(id){

//            case PdfDictionary.FontMatrix:
//                FontMatrix=value;
//            break;

            default:
            	super.setDoubleArray(id, value);
        }

    }

    @Override
    public byte[][] getKeyArray(final int id) {

        switch(id){

            case PdfDictionary.Annots:
       		    return deepCopy(Annots);

            case PdfDictionary.Contents:
       		    return deepCopy(Contents);

            case PdfDictionary.Kids:
       		    return deepCopy(Kids);

            default:
            	return super.getKeyArray(id);
        }
    }

    @Override
    public void setKeyArray(final int id, final byte[][] value) {

        switch(id){

            case PdfDictionary.Annots:
                Annots=value;
            break;

            case PdfDictionary.Kids:
                Kids=value;
            break;

            case PdfDictionary.Contents:
                Contents=value;
            break;

            default:
            	super.setKeyArray(id, value);
        }

    }
    
    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch(id){

            case PdfDictionary.OpenAction:
            	OpenAction=value;
            break;
//            case PdfDictionary.Differences:
//                Differences=value;
//            break;

           
            default:
            	super.setMixedArray(id, value);
        }

    }

    @Override
    public float[] getFloatArray(final int id) {

        switch(id){
            default:
            	return super.getFloatArray(id);

        }
    }

    @Override
    public void setFloatArray(final int id, final float[] value) {

        switch(id){

//	        case PdfDictionary.FontBBox:
//	            FontBBox=value;
//	        break;

            default:
            	super.setFloatArray(id, value);
        }

    }

    @Override
    public void setName(final int id, final byte[] value) {

        switch(id){


//            case PdfDictionary.CMapName:
//                rawCMapName=value;
//            break;


            default:
                super.setName(id,value);

        }

    }

    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch(id){

//	        case PdfDictionary.CharSet:
//	            rawCharSet=value;
//	        break;

            default:
                super.setTextStreamValue(id,value);

        }

    }

    @Override
    public String getName(final int id) {

        switch(id){

            case PdfDictionary.BaseFont:

            //setup first time
//            if(BaseFont==null && rawBaseFont!=null)
//                BaseFont=new String(rawBaseFont);
//
//            return BaseFont;


            default:
                return super.getName(id);

        }
    }

    @Override
    public String getTextStreamValue(final int id) {

        switch(id){

//	        case PdfDictionary.CharSet:
//
//	            //setup first time
//	            if(CharSet==null && rawCharSet!=null)
//	            	CharSet=new String(rawCharSet);
//
//	            return CharSet;

            default:

                return super.getTextStreamValue(id);

        }
    }
}