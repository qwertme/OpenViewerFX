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
 * OneBitDownSampler.java
 * ---------------
 */

package org.jpedal.parser.image;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.DeviceRGBColorSpace;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.parser.image.data.ImageData;

/**
 *
 * @author markee
 */
class OneBitDownSampler {
    
    static GenericColorSpace resetSeparationColorSpace(final byte[] index,final ImageData imageData,final byte[] data) {
        
        //needs to have these settings if 1 bit not indexed
        if(index==null && imageData.getDepth()==1){
            imageData.setCompCount(1);
            
            final int count=data.length;
            for(int aa=0;aa<count;aa++) {
                data[aa] = (byte) (data[aa] ^ 255);
            }
        }
        
        return new DeviceRGBColorSpace();
    }
    
    public static GenericColorSpace downSample(int sampling, ImageData imageData, boolean imageMask, boolean arrayInverted, byte[] maskCol, byte[] index, GenericColorSpace decodeColorData) {
        
        byte[] data=imageData.getObjectData();
        
        int newW=imageData.getWidth()/sampling;
        int newH=imageData.getHeight()/sampling;
        
        int size=newW*newH;
        
        if(imageMask){
            size *= 4;
            maskCol[3]=(byte)255;
        }else if(index!=null) {
            size *= 3;
        }
        
        final byte[] newData=new byte[size];
        
        final int[] flag={1,2,4,8,16,32,64,128};
        
        final int origLineLength= (imageData.getWidth()+7)>>3;
        
        
        byte currentByte;
        int bit;
        //scan all pixels and down-sample
        for(int y=0;y<newH;y++){
            for(int x=0;x<newW;x++){
                
                int bytes=0,count=0;
                
                //allow for edges in number of pixels left
                int wCount=sampling,hCount=sampling;
                final int wGapLeft=imageData.getWidth()-x;
                final int hGapLeft=imageData.getHeight()-y;
                if(wCount>wGapLeft) {
                    wCount = wGapLeft;
                }
                if(hCount>hGapLeft) {
                    hCount = hGapLeft;
                }
                
                //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                int ptr;
                for(int yy=0;yy<hCount;yy++){
                    for(int xx=0;xx<wCount;xx++){
                        
                        ptr=((yy+(y*sampling))*origLineLength)+(((x*sampling)+xx)>>3);
                        
                        if(ptr<data.length){
                            currentByte=data[ptr];
                        }else{
                            currentByte=0;
                        }
                        
                        if(imageMask && !arrayInverted) {
                            currentByte = (byte) (currentByte ^ 255);
                        }
                        
                        bit=currentByte & flag[7-(((x*sampling)+xx)& 7)];
                        
                        if(bit!=0) {
                            bytes++;
                        }
                        count++;
                    }
                }
                
                //set value as white or average of pixels
                final int offset=x+(newW*y);
                
                if(count>0){
                    if(imageMask){
                        for(int ii=0;ii<4;ii++){
                            if(arrayInverted) {
                                newData[(offset * 4) + ii] = (byte) (255 - (((maskCol[ii] & 255) * bytes) / count));
                            } else {
                                newData[(offset * 4) + ii] = (byte) ((((maskCol[ii] & 255) * bytes) / count));
                            }
                        }
                        
                    }else if(index!=null && imageData.getDepth()==1){
                        int av;
                        
                        for(int ii=0;ii<3;ii++){
                            
                            //can be in either order so look at index
                            if(index[0]==-1 && index[1]==-1 && index[2]==-1){
                                av=(index[ii] & 255) +(index[ii+3] & 255);
                                newData[(offset*3)+ii]=(byte)(255-((av *bytes)/count));
                            }else{//  if(decodeColorData.getID()==ColorSpaces.DeviceCMYK){  //avoid color 'smoothing' - see CustomersJune2011/lead base paint.pdf
                                final float ratio=bytes/count;
                                if(ratio>0.5) {
                                    newData[(offset * 3) + ii] = index[ii + 3];
                                } else {
                                    newData[(offset * 3) + ii] = index[ii];
                                }
                                
                            }
                        }
                    }else if(index!=null){
                        for(int ii=0;ii<3;ii++) {
                            newData[(offset * 3) + ii] = (byte) (((index[ii] & 255) * bytes) / count);
                        }
                    }else {
                        newData[offset] = (byte) ((255 * bytes) / count);
                    }
                }else{
                    
                    if(imageMask){
                        for(int ii=0;ii<3;ii++) {
                            newData[(offset * 4) + ii] = (byte) 0;
                        }
                        
                    }else if(index!=null){
                        for(int ii=0;ii<3;ii++) {
                            newData[((offset) * 3) + ii] = 0;
                        }
                    }else {
                        newData[offset] = (byte) 255;
                    }
                }
            }
        }
        data=newData;
        if(index!=null) {
            imageData.setCompCount(3);
        }
        imageData.setWidth(newW);
        imageData.setHeight(newH);
        decodeColorData.setIndex(null, 0);
        
        //remap Separation as already converted here
        if(decodeColorData.getID()==ColorSpaces.Separation){
            decodeColorData=new DeviceRGBColorSpace();
            
            //needs to have these settings if 1 bit not indexed
            if(index==null && imageData.getDepth()==1){
                imageData.setCompCount(1);
                
                final int count=data.length;
                for(int aa=0;aa<count;aa++) {
                    data[aa] = (byte) (data[aa] ^ 255);
                }
            }
        }
        
        imageData.setObjectData(data);
        
        imageData.setDepth(8);
        
        return decodeColorData;
    }
}
