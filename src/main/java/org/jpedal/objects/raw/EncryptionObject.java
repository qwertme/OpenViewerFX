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
 * EncryptionObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.LogWriter;

public class EncryptionObject extends PdfObject {

	//unknown CMAP as String
	//String unknownValue=null;

	//private float[] Matrix;

	boolean EncryptMetadata=true;

	int V=1; //default value
	
	int R=-1,P=-1;
	
	byte[] rawPerms,rawU,rawUE,rawO, rawOE, rawCFM, rawEFF, rawStrF, rawStmF;
	String U,UE,O,OE, EFF,CFM, StrF,StmF;

	private PdfObject CF;
    private byte[][] Recipients;

    public EncryptionObject(final String ref) {
        super(ref);
    }

    public EncryptionObject(final int ref, final int gen) {
       super(ref,gen);
    }

    @Override
    public boolean getBoolean(final int id){

        switch(id){

        case PdfDictionary.EncryptMetadata:
        	return EncryptMetadata;


            default:
            	return super.getBoolean(id);
        }

    }

    @Override
    public void setBoolean(final int id, final boolean value){

        switch(id){

        case PdfDictionary.EncryptMetadata:
        	EncryptMetadata=value;
        	break;

            default:
                super.setBoolean(id, value);
        }
    }

    @Override
    public PdfObject getDictionary(final int id){

        switch(id){

	        case PdfDictionary.CF:
	        	return CF;

//            case PdfDictionary.XObject:
//                return XObject;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value){

        switch(id){

	        case PdfDictionary.P:
	        	P=value;
	        break;
	
	        
	        case PdfDictionary.R:
	        	R=value;
	        break;

	        case PdfDictionary.V:
	        	V=value;
	        break;

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
    public int getInt(final int id){

        switch(id){

	        case PdfDictionary.P:
	            return P;
	            
	        case PdfDictionary.R:
	            return R;
            
        	case PdfDictionary.V:
            return V;

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

	        case PdfDictionary.CF:
	        	CF=value;
			break;

//            case PdfDictionary.XObject:
//            	XObject=value;
//    		break;

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


    		default:
    			super.setConstant(pdfKeyType,id);

        }

        return PDFvalue;
    }

    @Override
    public int getParameterConstant(final int key) {

    	//System.out.println("Get constant for "+key +" "+this);
        switch(key){


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


            case PdfDictionary.CFM:
                rawCFM=value;
            break;

            case PdfDictionary.EFF:
                rawEFF=value;
            break;

            case PdfDictionary.StmF:
                rawStmF=value;
            break;
            
            case PdfDictionary.StrF:
                rawStrF=value;
            break;
            
            default:
                super.setName(id,value);

        }

    }

    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch(id){

	        case PdfDictionary.O:
	            rawO=value;
	        break;

            case PdfDictionary.OE:
                rawOE=value;
                break;

            case PdfDictionary.Perms:
                rawPerms=value;
                break;

	        case PdfDictionary.U:
	            rawU=value;
	        break;

            case PdfDictionary.UE:
                rawUE=value;
                break;

	        
            default:
                super.setTextStreamValue(id,value);

        }

    }

    @Override
    public String getName(final int id) {

        switch(id){

            case PdfDictionary.CFM:

            //setup first time
            if(CFM==null && rawCFM!=null) {
                CFM = new String(rawCFM);
            }

            return CFM;

            case PdfDictionary.EFF:

                //setup first time
                if(EFF==null && rawEFF!=null) {
                    EFF = new String(rawEFF);
                }

                return EFF;
                
            case PdfDictionary.StmF:

                //setup first time
                if(StmF==null && rawStmF!=null) {
                    StmF = new String(rawStmF);
                }

                return StmF;

                
            case PdfDictionary.StrF:

                //setup first time
                if(StrF==null && rawStrF!=null) {
                    StrF = new String(rawStrF);
                }

                return StrF;

            default:
                return super.getName(id);

        }
    }

    @Override
    public String getTextStreamValue(final int id) {

        switch(id){

	        case PdfDictionary.O:

	            //setup first time
	            if(O==null && rawO!=null) {
                    O = new String(rawO);
                }

	            return O;

            case PdfDictionary.OE:

                //setup first time
                if(OE==null && rawOE!=null) {
                    OE = new String(rawOE);
                }

                return OE;
	            
	            
	        case PdfDictionary.U:

	            //setup first time
	            if(U==null && rawU!=null) {
                    U = new String(rawU);
                }

	            return U;

            case PdfDictionary.UE:

                //setup first time
                if(UE==null && rawUE!=null) {
                    UE = new String(rawUE);
                }

                return UE;


            default:
                return super.getTextStreamValue(id);

        }
    }
    
    @Override
    public byte[] getTextStreamValueAsByte(final int id) {

        switch(id){

	        case PdfDictionary.O:

	            return rawO;

            case PdfDictionary.OE:

                return rawOE;

            case PdfDictionary.Perms:

                return rawPerms;

	        case PdfDictionary.U:

	            //setup first time
	            if(U==null && rawU!=null) {
                    U = new String(rawU);
                }

	            return rawU;

            case PdfDictionary.UE:

                //setup first time
                if(UE==null && rawUE!=null) {
                    UE = new String(rawUE);
                }

                return rawUE;

            default:
                return super.getTextStreamValueAsByte(id);

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
      //  switch(id){

//            case PdfDictionary.BaseFont:
//                data=rawBaseFont;
//                break;

       // }

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
                throw new RuntimeException("Value not defined in getStringValue(int,mode) in "+this);
        }
    }

    @Override
    public byte[][] getKeyArray(final int id) {

        switch(id){

            default:
            	return super.getKeyArray(id);
        }
    }

    @Override
    public void setKeyArray(final int id, final byte[][] value) {

        switch(id){

            default:
            	super.setKeyArray(id, value);
        }

    }

    @Override
    public byte[][] getStringArray(final int id) {

        switch(id){

            case PdfDictionary.Recipients:
                            return deepCopy(Recipients);

            default:
            	return super.getStringArray(id);
        }
    }

    @Override
    public void setStringArray(final int id, final byte[][] value) {

        switch(id){

            case PdfDictionary.Recipients:
                Recipients=value;
                break;

            default:
            	super.setStringArray(id, value);
        }

    }


    @Override
    public int getObjectType(){
        return PdfDictionary.Encrypt;
    }
}