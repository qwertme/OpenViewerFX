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
 * DeviceGrayColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import org.jpedal.objects.raw.MaskObject;
import org.jpedal.objects.raw.PdfObject;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;


/**
 * handle GrayColorSpace
 */
public class DeviceGrayColorSpace extends GenericColorSpace {
    
    private static final long serialVersionUID = -8160089076145994695L;
    
    public DeviceGrayColorSpace(){
        setType(ColorSpaces.DeviceGray);
        cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    }
    
    /**
     * set  color (in terms of rgb)
     */
    @Override
    public final void setColor(final String[] number_values, final int opCount) {
        
        final float[] colValues=new float[1];
        colValues[0] = Float.parseFloat(number_values[0]);
        
        setColor(colValues,1);
    }
    
    /**set color from grayscale values*/
    @Override
    public final void setColor(final float[] operand, final int length) {
        
        int val;
        final float tmp = operand[0];
        
        //handle float or int
        if(tmp<=1) {
            val =(int) (255* tmp);
        } else {
            val =(int) (tmp);
        }
        
        //allow for bum values
        if(val<0) {
            val=0;
        }
        
        this.currentColor= new PdfColor(val, val, val);
        
    }
    
    /**
     * create rgb version of gray
     */
    @Override
    public byte[] dataToRGBByteArray(final byte[] data, final int w, final int h, boolean arrayInverted){
        
        final int size=data.length;
        final byte[] newData=new byte[size*3];
        int ptr=0;
        
        for(int a=0;a<size;a++){
           for(int comp=0;comp<3;comp++){
               newData[ptr]=data[a];
               ptr++;
           } 
        }
        
        return newData;
    }
    
    /**
     * convert Index to RGB
     */
    @Override
    public byte[] convertIndexToRGB(final byte[] index){
        
        isConverted=true;
        
        final int count=index.length;
        final byte[] newIndex=new byte[count*3];
        
        for(int i=0;i<count;i++){
            final byte value=index[i];
            for(int j=0;j<3;j++) {
                newIndex[(i*3)+j]=value;
            }
            
        }
        
        return newIndex;
    }
    
    /**
     * convert data stream to srgb image
     */
    @Override
    public BufferedImage JPEGToRGBImage( final byte[] data, final int w, final int h, final float[] decodeArray, final int pX, final int pY, final boolean arrayInverted, final PdfObject XObject) {
        
        //not appropriate for MaskObject case so use super version
        if(XObject instanceof MaskObject){
            return super.JPEGToRGBImage(data,w,h,decodeArray,pX,pY,arrayInverted,XObject);
        }else{
            return JPEGDecoder.grayJPEGToRGBImage( data, pX, pY,  arrayInverted);
        }
    }
}
