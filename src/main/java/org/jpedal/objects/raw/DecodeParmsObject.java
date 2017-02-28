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
 * DecodeParmsObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.LogWriter;

public class DecodeParmsObject extends PdfObject {

    boolean EncodedByteAlign,EndOfBlock=true, EndOfLine, BlackIs1,Uncompressed;

    PdfObject JBIG2Globals;

    int Blend=-1, Colors=-1, ColorTransform=1, Columns=-1,DamagedRowsBeforeError, EarlyChange=1, K, Predictor=1, QFactor=-1,Rows=-1;

    public DecodeParmsObject(final String ref) {
        super(ref);
    }

    public DecodeParmsObject(final int ref, final int gen) {
        super(ref,gen);
    }

    @Override
    public boolean getBoolean(final int id){

        switch(id){

            case PdfDictionary.BlackIs1:
                return BlackIs1;

            case PdfDictionary.EncodedByteAlign:
                return EncodedByteAlign;

            case PdfDictionary.EndOfBlock:
                return EndOfBlock;

            case PdfDictionary.EndOfLine:
                return EndOfLine;

            case PdfDictionary.Uncompressed:
                return Uncompressed;

            default:
                return super.getBoolean(id);
        }

    }

    @Override
    public void setBoolean(final int id, final boolean value){

        switch(id){

            case PdfDictionary.BlackIs1:
                BlackIs1=value;
                break;

            case PdfDictionary.EncodedByteAlign:
                EncodedByteAlign=value;
                break;

            case PdfDictionary.EndOfBlock:
                EndOfBlock=value;
                break;

            case PdfDictionary.EndOfLine:
                EndOfLine=value;
                break;

            case PdfDictionary.Uncompressed:
                Uncompressed=value;
                break;

            default:
                super.setBoolean(id, value);
        }
    }

    @Override
    public PdfObject getDictionary(final int id){

        switch(id){

            case PdfDictionary.JBIG2Globals:
                return JBIG2Globals;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value){

        switch(id){

            case PdfDictionary.Blend:
                Blend=value;
                break;

            case PdfDictionary.Colors:
                Colors=value;
                break;

            case PdfDictionary.ColorTransform:
                ColorTransform=value;
                break;

            case PdfDictionary.Columns:
                Columns=value;
                break;

            case PdfDictionary.DamagedRowsBeforeError:
                DamagedRowsBeforeError=value;
                break;

            case PdfDictionary.EarlyChange:
                EarlyChange=value;
                break;

            case PdfDictionary.K:
                K=value;
                break;

            case PdfDictionary.Predictor:
                Predictor=value;
                break;

            case PdfDictionary.QFactor:
                QFactor=value;
                break;

            case PdfDictionary.Rows:
                Rows=value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id){

        switch(id){

            case PdfDictionary.Blend:
                return Blend;

            case PdfDictionary.Colors:
                return Colors;

            case PdfDictionary.ColorTransform:
                return ColorTransform;

            case PdfDictionary.Columns:
                return Columns;

            case PdfDictionary.DamagedRowsBeforeError:
                return DamagedRowsBeforeError;

            case PdfDictionary.EarlyChange:
                return EarlyChange;

            case PdfDictionary.K:
                return K;

            case PdfDictionary.Predictor:
                return Predictor;

            case PdfDictionary.QFactor:
                return QFactor;

            case PdfDictionary.Rows:
                return Rows;

            default:
                return super.getInt(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value){

        value.setID(id);

        switch(id){

            case PdfDictionary.JBIG2Globals:
                JBIG2Globals=value;
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

                next -= 48;

                id += ((next)<<x);

                x += 8;
            }

            switch(id){

//                case StandardFonts.CIDTYPE0:
//                    PDFvalue =StandardFonts.CIDTYPE0;
//                break;

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

        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        //System.out.println(pdfKeyType+"="+PDFvalue);
    //    switch(pdfKeyType){

//        	case PdfDictionary.BaseEncoding:
//        		BaseEncoding=PDFvalue;
//        		break;

    //    }

        return PDFvalue;
    }

    @Override
    public int getParameterConstant(final int key) {

        final int def ;

  //      switch(key){


//            case PdfDictionary.BaseEncoding:
//
//            	//special cases first
//            	if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isZapfDingbats)
//            		return StandardFonts.ZAPF;
//            	else if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isSymbol)
//            		return StandardFonts.SYMBOL;
//            	else
//            		return BaseEncoding;
 //       }

        //check general values
        def=super.getParameterConstant(key);

        return def;
    }


    @Override
    public PdfArrayIterator getMixedArray(final int id) {

        switch(id){

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
    public boolean decompressStreamWhenRead() {
        return true;
    }

}