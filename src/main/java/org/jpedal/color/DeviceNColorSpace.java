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
 * DeviceNColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import java.awt.image.DataBufferByte;
import java.awt.image.DataBuffer;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfObject;

/**
 * handle Device ColorSpace
 */
public class DeviceNColorSpace extends SeparationColorSpace {
    
    private static final long serialVersionUID = -1372268945371555187L;
    
    private final Map cache=new HashMap();
    
    public DeviceNColorSpace(final PdfObjectReader currentPdfFile, final PdfObject colorSpace) {
        
        setType(ColorSpaces.DeviceN);
        
        processColorToken(currentPdfFile, colorSpace);
        
    }
    
    /** set color (translate and set in alt colorspace) */
    @Override
    public void setColor(final String[] operand, final int opCount) {
        
        final float[] values = new float[opCount];
        for(int j=0;j<opCount;j++) {
            values[j] = Float.parseFloat(operand[j]);
        }
        
        setColor(values,opCount);
    }
    
    
    /** set color (translate and set in alt colorspace */
    @Override
    public void setColor(final float[] raw, final int opCount) {
        
        final int[] lookup=new int[3];
        
        int opNumbers=raw.length;
        if(opNumbers>3) {
            opNumbers=3;
        }
        
        for(int i=0;i<opNumbers;i++){
            lookup[i]=(int)(raw[i]*255);
        }
        
        boolean isCached=false;
        
        if(this.cmykMapping==Black && opCount==1){ //special case coded in
            
            final float[] newOp={0f,0f,0f, raw[0]};
            altCS.setColor(newOp,newOp.length);
            
        }else if(opCount<4 && cache.get((lookup[0] << 16) + (lookup[1] << 8) + lookup[2])!=null){
            
            isCached=true;
            
            final Object val=cache.get((lookup[0] << 16) + (lookup[1] << 8) + lookup[2]);
            final int rawValue = (Integer) val;
            final int r = ((rawValue >> 16) & 255);
            final int g = ((rawValue >> 8) & 255);
            final int b = ((rawValue) & 255);
            
            altCS.currentColor=new PdfColor(r,g,b);
            
        }else if(this.cmykMapping==CMYB && opCount==4){ //special case coded in
            
            final float[] newOp={raw[0],raw[1],raw[2],raw[3]};
            altCS.setColor(newOp,newOp.length);
        }else if(this.cmykMapping==MYK && opCount==3){ //special case coded in
            
            final float[] newOp={0.0f,raw[0],raw[1],raw[2]};
            altCS.setColor(newOp,newOp.length);
            
        }else if(this.cmykMapping==CMY && opCount==3){ //special case coded in
            
            final float[] newOp={raw[0],raw[1],raw[2],0.0f};
            altCS.setColor(newOp,newOp.length);
            
        }else if(this.cmykMapping==CMK && opCount==3){ //special case coded in
            
            final float[] newOp={raw[0],raw[1],0f, raw[2]};
            altCS.setColor(newOp,newOp.length);
            
        }else if(this.cmykMapping==CY && opCount==2){ //special case coded in
            
            final float[] newOp={raw[0],0,raw[1], 0};
            altCS.setColor(newOp,newOp.length);
            
        }else if(this.cmykMapping==CM && opCount==2){ //special case coded in
            
            final float[] newOp={raw[0],raw[1],0, 0};
            altCS.setColor(newOp,newOp.length);
        }else if(this.cmykMapping==MY && opCount==2){ //special case coded in
            
            final float[] newOp={0,raw[0],raw[1], 0};
            altCS.setColor(newOp,newOp.length);
            
        }else{
            
            final float[] operand =colorMapper.getOperandFloat(raw);
            altCS.setColor(operand,operand.length);
            
        }
        
        if(!isCached){ //not used except as flag
            
            altCS.getColor().getRGB();
            final int rawValue = altCS.getColor().getRGB();
            
            //store values in cache
            cache.put((lookup[0] << 16) + (lookup[1] << 8) + lookup[2], rawValue);
            
        }
    }
    
    /**
     * convert separation stream to RGB and return as an image
     */
    @Override
    public BufferedImage  dataToRGB(final byte[] data, final int w, final int h) {
        
        BufferedImage image;
        
      //  try {
            
            //convert data
            image=createImage(w, h, data,false);
            
//        } catch (final  ee) {
//            image = null;
//            
//            if(LogWriter.isOutput()) {
//                LogWriter.writeLog("Couldn't convert DeviceN colorspace data: " + ee);
//            }
//        }
        
        return image;
        
    }
    
    /**
     * convert data stream to srgb image
     */
    @Override
    public BufferedImage JPEGToRGBImage(final byte[] data, final int ww, final int hh, final float[] decodeArray, final int pX, final int pY, final boolean arrayInverted, final PdfObject XObject) {
        
        BufferedImage image=null;
        
    //    try{
            
            Raster ras= JPEGDecoder.getRasterFromJPEG(data, "JPEG");
            
            if(ras!=null){
                ras=cleanupRaster(ras,pX,pY, componentCount);
                final int w=ras.getWidth();
                final int h=ras.getHeight();
                
                final DataBufferByte rgb = (DataBufferByte) ras.getDataBuffer();
                
                //convert the image
                image=createImage(w, h, rgb.getData(),arrayInverted);
            }
//        } catch (final Exception ee) {
//            
//            if(LogWriter.isOutput()) {
//                LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
//            }
//            
//            ee.printStackTrace();
//        }
        
        return image;
    }
    
    /**
     * turn raw data into an image
     */
    @Override
    BufferedImage createImage(final int w, final int h, final byte[] rawData, final boolean arrayInverted) {
        
        final BufferedImage image;
        
        final byte[] rgb=new byte[w*h*3];
        
        final int bytesCount=rawData.length;
        
        //convert data to RGB format
        final int byteCount= rawData.length/componentCount;
        
        final float[] values=new float[componentCount];
        
        int j=0,j2=0;
        
        for(int i=0;i<byteCount;i++){
            
            if(j>=bytesCount) {
                break;
            }
            
            for(int comp=0;comp<componentCount;comp++){
                values[comp]=((rawData[j] & 255)/255f);
                j++;
            }
            
            setColor(values,componentCount);
            
            //set values
            final int foreground =altCS.currentColor.getRGB();
            
            rgb[j2]=(byte) ((foreground>>16) & 0xFF);
            rgb[j2+1]=(byte) ((foreground>>8) & 0xFF);
            rgb[j2+2]=(byte) ((foreground) & 0xFF);
            
            j2 += 3;
            
        }
        
        //create the RGB image
        final int[] bands = {0,1,2};
        image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        final DataBuffer dataBuf=new DataBufferByte(rgb, rgb.length);
        final Raster raster =Raster.createInterleavedRaster(dataBuf,w,h,w*3,3,bands,null);
        image.setData(raster);
        
        return image;
    }
}
