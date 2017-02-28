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
 * ThreeComponentImage.java
 * ---------------
 */

package org.jpedal.parser.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.Arrays;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
class ThreeComponentImage {
    
    
    public static BufferedImage make(int d, byte[] data, byte[] index, int w, int h) {
        
        BufferedImage image;
        
        //some odd files with 16 bit need to be down-sampled to work with 8 bit (fogbugz 15839)
        // baseline_screens/14jan/fatt18SKYspeseOND13.pdf
        if(d==16){
            final int origSize=data.length;
            final int newSize=origSize/2;
            
            final byte[] newData=new byte[newSize];
            
            for(int ptr=0;ptr<newSize;ptr++){
                newData[ptr]=data[ptr*2];
            }
            data=newData;
            
            d=8;
            
        }
        if(LogWriter.isOutput()) {
            LogWriter.writeLog("Converting 3 comp colorspace to sRGB index=" + Arrays.toString(index));
        }
        
        //work out from size what sort of image data we have
        if (w * h == data.length) {
            image = makeIndexImage(d, index, w, h, data);
        } else{
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Converting data to sRGB " + data.length + " depth=" + d);
            }
            
            image =makeImage(d, data, w, h);
            
            
        }
        return image;
    }

    static BufferedImage makeImage(int d, byte[] data, int w, int h) {
       
        //expand out 4 bit raster as does not appear to be easy way
        if(d==4){
            data=expand4bitData(data, w, h);
        }
        
        BufferedImage image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        data=checkSize(data,w,h,3);
        final Raster raster = ColorSpaceConvertor.createInterleavedRaster(data, w, h);
        image.setData(raster);
        
        return image;
    }

    static byte[] expand4bitData(byte[] data, int w, int h) {
        
        final int origSize=data.length;
        final int newSize=w*h*3;
        final boolean isOdd=(w & 1) ==1;
        final int scanLine=((w*3)+1)>>1;
        final byte[] newData=new byte[newSize];
        byte rawByte;
        int ptr=0,currentLine=0;
        
        for(int ii=0;ii<origSize;ii++){
            rawByte=data[ii];
            
            currentLine++;
            newData[ptr]=(byte) (rawByte & 240);
            if(newData[ptr]==-16){   //fix for white
                newData[ptr] = (byte) 255;
            }
            ptr++;
            
            if((currentLine)==scanLine && isOdd){ //ignore pack bit at end of odd line
                currentLine=0;
            }else{
                newData[ptr]=(byte) ((rawByte & 15) <<4);
                if(newData[ptr]==-16)  //fix for white
                {
                    newData[ptr] = (byte) 255;
                }
                
                ptr++;
            }
            
            if(ptr==newSize) {
                ii = origSize;
            }
        }
        
        data=newData;
        
        return data;
    }

    static BufferedImage makeIndexImage(int d, byte[] index, int w, int h, byte[] data) {
        
        BufferedImage image;
        
        if (d == 8 && index!=null){
            image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, index, false,false);
        }else{
            
            /** create an image from the raw data*/
            final DataBuffer db = new DataBufferByte(data, data.length);
            
            final int[] bands = {0};
            
            image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
            final Raster raster =Raster.createInterleavedRaster(db,w,h,w,1,bands,null);
            image.setData(raster);
            
        }
        return image;
    }
    
    private static byte[] checkSize(byte[] data, final int w, final int h, final int comp) {

        final int correctSize=w*h*comp;
        if(data.length<correctSize){
            final byte[] newData=new byte[correctSize];
            System.arraycopy(data,0,newData,0,data.length);
            data=newData;
        }

        return data;
    }
    
}
