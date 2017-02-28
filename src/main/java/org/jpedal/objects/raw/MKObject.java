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
 * MKObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.StringUtils;

public class MKObject extends FormObject {

	//unknown CMAP as String
	//String unknownValue=null;

	private float[] BC, BG;
	
	protected String AC, CA, RC;
	
	protected byte[] rawAC, rawCA, rawRC;

	private int TP=-1;

	int R;
	
	//boolean ImageMask=false;

	//int FormType=0, Height=1, Width=1;

	private PdfObject I;

	// <start-demo><end-demo>
	
	/** creates a copy of this MKObject but in a new Object so that changes wont affect this MkObject*/
	@Override
    public PdfObject duplicate(){
		
		final MKObject copy = new MKObject();
		
		//System.out.println(source.getMKInt(PdfDictionary.TP)+" "+TP);

		final int sourceTP=this.getInt(PdfDictionary.TP);
		if(sourceTP!=-1) {
            copy.setIntNumber(PdfDictionary.TP, sourceTP);
        }

		final int sourceR=this.getInt(PdfDictionary.R);
		copy.setIntNumber(PdfDictionary.R,sourceR);

		//make sure also added to getTextStreamValueAsByte
		final int[] textStreams= {PdfDictionary.AC,PdfDictionary.CA, PdfDictionary.RC};

        for (final int textStream : textStreams) {
            final byte[] bytes = this.getTextStreamValueAsByte(textStream);
            if (bytes != null) {
                copy.setTextStreamValue(textStream, bytes);
            }
        }

		//make sure also added to getTextStreamValueAsByte
		final int[] floatStreams= {PdfDictionary.BC,PdfDictionary.BG};

        for (final int floatStream : floatStreams) {
            final float[] floats = this.getFloatArray(floatStream);
            if (floats != null) {
                copy.setFloatArray(floatStream, floats);
            }
        }
		
		if(this.I!=null) {
            copy.I = I.duplicate();
        }
		
		return copy;
    }
	
    public MKObject(final String ref) {
        super(ref);
    }

    public MKObject(final int ref, final int gen) {
       super(ref,gen);
    }


    public MKObject() {
		// TODO Auto-generated constructor stub
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

	        case PdfDictionary.I:
	        	return I;
//
//            case PdfDictionary.XObject:
//                return XObject;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value){

        switch(id){

        case PdfDictionary.R:
            R=value;
        break;
        
        case PdfDictionary.TP:
            TP=value;
        break;


            default:
            	super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id){

        switch(id){

        case PdfDictionary.R:
        	return R;
            
        case PdfDictionary.TP:
            return TP;


            default:
            	return super.getInt(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value){

    	value.setID(id);
        switch(id){

	        case PdfDictionary.I:
	        	I=value;
			break;
//
//            case PdfDictionary.XObject:
//            	XObject=value;
//    		break;

            default:
            	super.setDictionary(id, value);
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
        
        case PdfDictionary.BC:
        	return BC;
        	
        case PdfDictionary.BG:
        	return BG;
        	
        	
            default:
            	return super.getFloatArray(id);

        }
    }

    @Override
    public void setFloatArray(final int id, final float[] value) {

        switch(id){

        	case PdfDictionary.BC:
    		BC=value;
    		break;
    		
        	case PdfDictionary.BG:
        		BG=value;
        		break;
        	

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
    public byte[] getTextStreamValueAsByte(final int id) {

        switch(id){

	        case PdfDictionary.AC:
	            return rawAC;
	            
	        case PdfDictionary.CA:
	            return rawCA;
	            
	        case PdfDictionary.RC:
	            return rawRC;    

            default:
                return super.getTextStreamValueAsByte(id);

        }
    }

    @Override
    public String getName(final int id) {

        switch(id){

//            case PdfDictionary.BaseFont:
//
//            //setup first time
//            if(BaseFont==null && rawBaseFont!=null)
//                BaseFont=new String(rawBaseFont);
//
//            return BaseFont;

            default:
                return super.getName(id);

        }
    }
    
    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch(id){

	        case PdfDictionary.AC:
	            rawAC=value;
	    	break;
	    	
	        case PdfDictionary.CA:
	        	rawCA=value;
	    	break;
        
        	case PdfDictionary.RC:
	            rawRC=value;
        	break;
        
            default:
                super.setTextStreamValue(id,value);

        }

    }

    @Override
    public String getTextStreamValue(final int id) {

        switch(id){

        case PdfDictionary.AC:

            //setup first time
            if(AC==null && rawAC!=null) {
                AC = StringUtils.getTextString(rawAC, false);
            }

            return AC; 
            
        case PdfDictionary.CA:

            //setup first time
            if(CA==null && rawCA!=null) {
                CA = StringUtils.getTextString(rawCA, false);
            }
            return CA; 
               
            case PdfDictionary.RC:

	            //setup first time
	            if(RC==null && rawRC!=null) {
                    RC = StringUtils.getTextString(rawRC, false);
                }
	
	            return RC;    
	            
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
      //  switch(id){

//            case PdfDictionary.BaseFont:
//                data=rawBaseFont;
//                break;

      //  }

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
    public int getObjectType(){
        return PdfDictionary.MK;
    }
}