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
 * OCObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.LogWriter;

public class OCObject extends PdfObject {

	//unknown CMAP as String
	//String unknownValue=null;

	//private float[] Matrix;

	//boolean ImageMask=false;

    float max,min;
    
    int Event=-1;

    private byte[] rawBaseState,rawListMode,rawViewState;
    String BaseState, ListMode;

    private PdfObject D, Layer, OCGs_dictionary, Usage, View,Zoom;

    private Object[] Order;
    private byte[][] AS, Category, Locked, ON, OFF,OCGs,Configs, RBGroups;

    public OCObject(final String ref) {
        super(ref);
    }

    public OCObject(final int ref, final int gen) {
       super(ref,gen);
    }


    @Override
    public boolean getBoolean(final int id){

        switch(id){

       // case PdfDictionary.ImageMask:
       // 	return ImageMask;


            default:
            	return super.getBoolean(id);
        }

    }

    @Override
    public void setBoolean(final int id, final boolean value){

        switch(id){

//        case PdfDictionary.ImageMask:
//        	ImageMask=value;
//        	break;

            default:
                super.setBoolean(id, value);
        }
    }

    @Override
    public PdfObject getDictionary(final int id){

        switch(id){

	        case PdfDictionary.D:
	        	return D;

            case PdfDictionary.Layer:
	        	return Layer;

            case PdfDictionary.OCGs:
	        	return OCGs_dictionary;

            case PdfDictionary.Usage:
                return Usage;

            case PdfDictionary.View:
                return View;
                    
            case PdfDictionary.Zoom:
                return Zoom;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value){

        switch(id){

//	        case PdfDictionary.FormType:
//	        	FormType=value;
//	        break;
//
//	        case PdfDictionary.Height:
//	            Height=value;
//	        break;
//
//	        case PdfDictionary.Width:
//	            Width=value;
//	        break;

            default:
            	super.setIntNumber(id, value);
        }
    }

    @Override
    public void setFloatNumber(final int id, final float value){

        switch(id){

	        case PdfDictionary.max:
	    		max=value;
	    		break;

            case PdfDictionary.min:
                            min=value;
                            break;


            default:

                super.setFloatNumber(id,value);
        }
    }

    @Override
    public float getFloatNumber(final int id){

        switch(id){

            case PdfDictionary.max:
        		return max;

            case PdfDictionary.min:
        		return min;

            default:

                return super.getFloatNumber(id);
        }
    }

    @Override
    public int getInt(final int id){

        switch(id){

//        	case PdfDictionary.FormType:
//            return FormType;
//
//        	case PdfDictionary.Height:
//            return Height;
//
//	        case PdfDictionary.Width:
//	            return Width;

            default:
            	return super.getInt(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value){

    	value.setID(id);
    	
        switch(id){

	        case PdfDictionary.D:
	        	D=value;
			break;

            case PdfDictionary.Layer:
	        	Layer=value;
			break;
                
            case PdfDictionary.OCGs:
                OCGs_dictionary=value;
            break;    

            case PdfDictionary.Usage:
                Usage=value;
    		break;

                case PdfDictionary.View:
            	View=value;
    		break;
                    
            case PdfDictionary.Zoom:
            	Zoom=value;
    		break;

            default:
            	super.setDictionary(id, value);
        }
    }


    @Override
    public int setConstant(final int pdfKeyType, final int keyStart, final int keyLength, final byte[] raw) {

        int PDFvalue =PdfDictionary.Unknown;

        int id=0,x=0,next;

        try{

            //convert token to unique key which we can lookup

            for(int i2=keyLength-1;i2>-1;i2--){

            	next=raw[keyStart+i2];

            	//System.out.println((char)next);
            	next -= 48;

                id += ((next)<<x);

                x += 8;
            }

            /**
             * not standard
             */
            switch(id){

                default:

//                	if(pdfKeyType==PdfDictionary.Encoding){
//                		PDFvalue=PdfCIDEncodings.getConstant(id);
//
//                		if(PDFvalue==PdfDictionary.Unknown){
//
//                			byte[] bytes=new byte[keyLength];
//
//                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
//
//                			unknownValue=new String(bytes);
//                		}
//
//                		if(debug && PDFvalue==PdfDictionary.Unknown){
//                			System.out.println("Value not in PdfCIDEncodings");
//
//                           	 byte[] bytes=new byte[keyLength];
//
//                               System.arraycopy(raw,keyStart,bytes,0,keyLength);
//                               System.out.println("Add to CIDEncodings and as String");
//                               System.out.println("key="+new String(bytes)+" "+id+" not implemented in setConstant in PdfFont Object");
//
//                               System.out.println("final public static int CMAP_"+new String(bytes)+"="+id+";");
//                               
//                		}
//                	}else
                	PDFvalue=super.setConstant(pdfKeyType,id);

                    if(PDFvalue==-1 && debug){

                        	 final byte[] bytes=new byte[keyLength];

                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
                            System.out.println("key="+new String(bytes)+ ' ' +id+" not implemented in setConstant in "+this);

                            System.out.println("final public static int "+new String(bytes)+ '=' +id+ ';');
                            
                        }

                    break;

            }

        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        //System.out.println(pdfKeyType+"="+PDFvalue);
        switch(pdfKeyType){

            case PdfDictionary.Event:
                    Event=PDFvalue;
                break;

            default:
    			super.setConstant(pdfKeyType,id);

        }

        return PDFvalue;
    }


    @Override
    public int getParameterConstant(final int key) {

    	//System.out.println("Get constant for "+key +" "+this);
        switch(key){

            case PdfDictionary.Event:
                return Event;


//            case PdfDictionary.BaseEncoding:
//
//            	//special cases first
//            	if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isZapfDingbats)
//            		return StandardFonts.ZAPF;
//            	else if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isSymbol)
//            		return StandardFonts.SYMBOL;
//            	else
//            		return BaseEncoding;
        default:
        	return super.getParameterConstant(key);

        }
    }

//    public void setStream(){
//
//        hasStream=true;
//    }


    @Override
    public PdfArrayIterator getMixedArray(final int id) {

    	switch(id){

            //case PdfDictionary.Differences:
            //    return new PdfArrayIterator(Differences);

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
    public int[] getIntArray(final int id) {

        switch(id){

            default:
            	return super.getIntArray(id);
        }
    }

    @Override
    public void setIntArray(final int id, final int[] value) {

        switch(id){

            default:
            	super.setIntArray(id, value);
        }
    }

    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch(id){

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

//	        case PdfDictionary.Matrix:
//	            Matrix=value;
//	        break;

            default:
            	super.setFloatArray(id, value);
        }
    }

    @Override
    public void setName(final int id, final byte[] value) {

        switch(id){

            case PdfDictionary.ListMode:
                rawListMode=value;
            break;

            case PdfDictionary.ViewState:
                rawViewState=value;
            break;    
            default:
                super.setName(id,value);

        }

    }

    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch(id){

            default:
                super.setTextStreamValue(id,value);

        }

    }

    //return as constnt we can check
    @Override
    public int getNameAsConstant(final int id) {

        final byte[] raw ;

        switch(id){

            case PdfDictionary.BaseState:
            raw=rawBaseState;
            break;

            case PdfDictionary.ListMode:
            raw=rawListMode;
            break;
                
            case PdfDictionary.ViewState:
                raw=rawViewState;
            break;

            default:
                return super.getNameAsConstant(id);

        }

        if(raw==null) {
            return super.getNameAsConstant(id);
        } else {
            return PdfDictionary.generateChecksum(0, raw.length, raw);
        }

    }


    @Override
    public String getName(final int id) {

        switch(id){

            case PdfDictionary.BaseState:

            //setup first time
            if(BaseState==null && rawBaseState!=null) {
                BaseState = new String(rawBaseState);
            }

            return BaseState;

            case PdfDictionary.ListMode:

            //setup first time
            if(ListMode==null && rawListMode!=null) {
                ListMode = new String(rawListMode);
            }

            return ListMode;


            
            default:
                return super.getName(id);

        }
    }

    @Override
    public String getTextStreamValue(final int id) {

        switch(id){

	        

            default:
                return super.getTextStreamValue(id);

        }
    }

    /**
     * unless you need special fucntions,
     * use getStringValue(int id) which is faster
     */
    @Override
    public String getStringValue(final int id, final int mode) {

        final byte[] data=null;

        //get data
   //     switch(id){

//            case PdfDictionary.BaseFont:
//                data=rawBaseFont;
//                break;

     //   }

        //

        //convert
        switch(mode){
            case PdfDictionary.STANDARD:

                //setup first time
                if(data!=null) {
                    return new String(data);
                } else {
                    return null;
                }


            case PdfDictionary.LOWERCASE:

                //setup first time
                if(data!=null) {
                    return new String(data);
                } else {
                    return null;
                }

            case PdfDictionary.REMOVEPOSTSCRIPTPREFIX:

                //setup first time
                if(data!=null){
                	final int len=data.length;
                	if(len>6 && data[6]=='+'){ //lose ABCDEF+ if present
                		final int length=len-7;
                		final byte[] newData=new byte[length];
                		System.arraycopy(data, 7, newData, 0, length);
                		return new String(newData);
                	}else {
                        return new String(data);
                    }
                }else {
                    return null;
                }

            default:
                throw new RuntimeException("Value not defined in getName(int,mode) in "+this);
        }
    }

    @Override
    public byte[][] getKeyArray(final int id) {

        switch(id){

            case PdfDictionary.AS:
                return AS;

            case PdfDictionary.Category:
                return Category;

            case PdfDictionary.Configs:
                return Configs;

            case PdfDictionary.Locked:
                return Locked;

            case PdfDictionary.OCGs:
                return OCGs;

            case PdfDictionary.OFF:
                return OFF;

            case PdfDictionary.ON:
                return ON;

            case PdfDictionary.RBGroups:
                return RBGroups;
            
            default:
            	return super.getKeyArray(id);
        }
    }

    @Override
    public void setObjectArray(final int id, final Object[] objectValues) {

        switch(id){

            case PdfDictionary.Order:
                Order=objectValues;
                break;

            default:
                super.setObjectArray(id, objectValues);
                break;
        }
    }

    @Override
    public Object[] getObjectArray(final int id) {

        switch(id){

            case PdfDictionary.Order:
                return deepCopy(Order);

            default:
                return super.getObjectArray(id);
        }
    }

    protected static Object[] deepCopy(final Object[] input){

        if(input==null) {
            return null;
        }

        final int count=input.length;

        final Object[] deepCopy=new Object[count];

        for(int aa=0;aa<count;aa++){

            if(input[aa] instanceof byte[]){
                final byte[] byteVal=(byte[])input[aa];
                final int byteCount=byteVal.length;

                final byte[] newValue=new byte[byteCount];
                deepCopy[aa]=newValue;
    
                System.arraycopy(byteVal,0,newValue,0,byteCount);
            }else{
               deepCopy[aa]=deepCopy((Object[])input[aa]); 
            }
        }

        return deepCopy;
    }


    @Override
    public void setKeyArray(final int id, final byte[][] value) {

        switch(id){

            case PdfDictionary.AS:
                AS=value;
            break;

            case PdfDictionary.Category:
                Category=value;
            break;

            case PdfDictionary.Configs:
                Configs=value;
            break;

            case PdfDictionary.Locked:
                Locked=value;
            break;

            case PdfDictionary.OCGs:
                OCGs=value;
            break;

            case PdfDictionary.OFF:
                OFF=value;
            break;

            case PdfDictionary.ON:
                ON=value;
            break;

            case PdfDictionary.RBGroups:
                RBGroups=value;
            break;

            default:
            	super.setKeyArray(id, value);
        }

    }

    @Override
    public int getObjectType(){
        return PdfDictionary.OCProperties;
    }
}