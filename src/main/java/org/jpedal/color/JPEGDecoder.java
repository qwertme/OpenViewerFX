
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
 * JPEGDecoder.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.*;
import java.util.Iterator;

import static org.jpedal.color.GenericColorSpace.cleanupRaster;
import org.jpedal.examples.handlers.DefaultImageHelper;
import org.jpedal.utils.LogWriter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.jpedal.objects.raw.*;
import org.jpedal.parser.image.utils.ArrayUtils;

/**
 *
 */
public class JPEGDecoder {
    
    static BufferedImage JPEGToRGBImageFromLUV(final byte[] data, final int pX, final int pY) {
        
        BufferedImage image;
        
        try {
            
            Raster ras=DefaultImageHelper.readRasterFromJPeg(data);
            if(ras==null) {
                return null;
            }
            
            /**access the file* 2010 (ms) replace this code with new code above as this deprecated
             * in = new ByteArrayInputStream(data);
             * com.sun.image.codec.jpeg.JPEGImageDecoder decoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGDecoder(in);
             * Raster ras = decoder.decodeAsRaster();
             * /***/
            ras=cleanupRaster(ras,pX,pY,3);
            
            final int width = ras.getWidth();
            final int height = ras.getHeight();
            final int imgSize = width * height;
            final byte[] iData= ((DataBufferByte)ras.getDataBuffer()).getData();
            
            float y,cb,cr,r=0,g=0,b=0, last_y=-1,last_cb=-1,last_cr=-1;
            
            for (int i = 0;
                    i < imgSize * 3;
                    i += 3) { //convert all values to rgb
                
                y = (iData[i] & 255);
                cb = ((iData[i + 1] & 255));
                cr = ((iData[i + 2] & 255));
                
                //only calc if needed
                if(y!=last_y || cb!=last_cb || cr!=last_cr){
                    
                    r=y+1.402f *(cr-128);
                    if(r<0) {
                        r=0;
                    } else if(r>255) {
                        r=255;
                    }
                    
                    g=y-0.344f*(cb-128)-0.714f * (cr-128);
                    if(g<0) {
                        g=0;
                    } else if(g>255) {
                        g=255;
                    }
                    
                    b=y+1.772f *(cb-128);
                    if(b<0) {
                        b=0;
                    } else if(b>255) {
                        b=255;
                    }
                    
                    last_y=y;
                    last_cb=cb;
                    last_cr=cr;
                }
                
                iData[i]=(byte) r;
                iData[i+1]=(byte) g;
                iData[i + 2]=(byte) b;
                
            }
            
            final DataBuffer db =new DataBufferByte (iData,iData.length);
            final int[] bands = { 0, 1, 2 };
            image =
                    new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            final Raster raster =
                    Raster.createInterleavedRaster(
                            db,
                            width,
                            height,
                            width * 3,
                            3,
                            bands,
                            null);
            image.setData(raster);
            
            
            
        } catch (final Exception ee) {
            image = null;
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
            }
        }
        
        return image;
        
    }
    
    public static void write(final BufferedImage image, final String type, final String des) {
        
        try {
            final BufferedOutputStream bos= new BufferedOutputStream(new FileOutputStream(new File(des)));
            ImageIO.write(image, type, bos);
            bos.flush();
            bos.close();
        } catch (final IOException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: "+e.getMessage());
            }
            //
        }
    }
    
    public static void write(final BufferedImage image, final String type, final OutputStream bos) {
        
        try {
            ImageIO.write(image, type, bos);
            bos.flush();
            bos.close();
        } catch (final IOException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: "+e.getMessage());
            }
            //
        }
    }
    
    public static Raster getRasterFromJPEG(final byte[] data, final String type) {
        
        final ByteArrayInputStream in;
        
        ImageReader iir=null;
        final ImageInputStream iin;
        
        Raster ras=null;
        
        try {
            
            //read the image data
            in = new ByteArrayInputStream(data);
            
            //suggestion from Carol
            final Iterator iterator = ImageIO.getImageReadersByFormatName(type);
            
            while (iterator.hasNext()){
                final Object o = iterator.next();
                iir = (ImageReader) o;
                if (iir.canReadRaster()) {
                    break;
                }
            }
            
            ImageIO.setUseCache(false);
            iin = ImageIO.createImageInputStream((in));
            iir.setInput(iin, true);
            ras=iir.readRaster(0, null);
            
            in.close();
            iir.dispose();
            iin.close();
            
        }catch(final Exception ee){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Problem closing  " + ee);
            }
        }
        
        return ras;
    }
    
    static BufferedImage grayJPEGToRGBImage(final byte[] data, final int pX, final int pY, final boolean arrayInverted) {
        
        BufferedImage image=null;
        
        try {
            
            Raster ras= JPEGDecoder.getRasterFromJPEG(data, "JPEG");
            
            if(ras!=null){
                ras=cleanupRaster(ras,pX,pY,1); //note uses 1 not count
                
                final int w = ras.getWidth();
                final int h = ras.getHeight();

                final DataBufferByte rgb = (DataBufferByte) ras.getDataBuffer();
                final byte[] rawData=rgb.getData();
                
                final int byteLength=rawData.length;
                final byte[] rgbData=new byte[byteLength*3];
                int ptr=0;
                for(int ii=0;ii<byteLength;ii++){
                    
                    if(arrayInverted){ //flip if needed
                        rawData[ii]=(byte) (rawData[ii]^255);
                    }
                    
                    rgbData[ptr]=rawData[ii];
                    ptr++;
                    rgbData[ptr]=rawData[ii];
                    ptr++;
                    rgbData[ptr]=rawData[ii];
                    ptr++;
                    
                }
                
                final int[] bands = {0,1,2};
                image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
                final Raster raster =Raster.createInterleavedRaster(new DataBufferByte(rgbData, rgbData.length),w,h,w*3,3,bands,null);
                
                image.setData(raster);
            }
            
        } catch (final Exception ee) {
            image = null;
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
            }
        }
        
        return image;
    }
    
    //
    public static byte[] getBytesFromJPEG(final byte[] data, GenericColorSpace decodeColorData,final PdfObject XObject) {
        
//        try{
//        FileOutputStream fos=new FileOutputStream("/Users/markee/Desktop/"+XObject.getObjectRefAsString()+".png");
//        fos.write(data);
//        fos.close();
//        }catch(Exception e){}
        
        byte[] db=null;
        Raster ras=null;
        
        try {
            BufferedImage img=decodeColorData.JPEGToRGBImage(data, XObject.getInt(PdfDictionary.Width), XObject.getInt(PdfDictionary.Height), null, -1, -1, false, XObject);
            
            //System.out.println(decodeColorData+" "+img);
            if(img.getType()==BufferedImage.TYPE_INT_RGB){ //we need byte in rgb
                img=org.jpedal.io.ColorSpaceConvertor.convertColorspace(img, BufferedImage.TYPE_3BYTE_BGR);
                
                if(img!=null){
                    ras= img.getData();
                }
                db=((DataBufferByte)ras.getDataBuffer()).getData();
                
                //switch order
                byte r,g,b;
                for(int i=0;i<db.length;i=i+3){
                    b=db[i];
                    g=db[i+1];
                    r=db[i+2];
                    
                    db[i]=r;
                    db[i+1]=g;
                    db[i+2]=b;
                }
            }else{ //simple case
                
                if(img!=null){
                    ras= img.getData();
                }
                db=((DataBufferByte)ras.getDataBuffer()).getData();
            }
            
        } catch (Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception with JPeg Image ");
            }
        }
        
        return db;
        
    }
    /**/
    
    //
    
}


