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
 * EightBitDownSampler.java
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
class EightBitDownSampler {
    
    static GenericColorSpace downSample(ImageData imageData, GenericColorSpace decodeColorData, int sampling, byte[] index) {
        
        final boolean hasIndex=decodeColorData.getIndexedMap()!=null &&
                (decodeColorData.getID()==ColorSpaces.DeviceRGB || decodeColorData.getID()==ColorSpaces.CalRGB ||
                decodeColorData.getID()==ColorSpaces.DeviceCMYK || decodeColorData.getID()==ColorSpaces.ICC || decodeColorData.getID()==ColorSpaces.DeviceN);
        
        int comp;
        
        int indexCount=1;
        
        if(hasIndex){ //convert to sRGB
            comp=1;
            
            imageData.setCompCount(3);
            indexCount=3;
            index=decodeColorData.convertIndexToRGB(index);
            
            //actually sRGB now so reset colorspace
            decodeColorData=new DeviceRGBColorSpace();
            
        }else{
            comp=decodeColorData.getColorComponentCount();
        }
        
        
        int newW=imageData.getWidth()/sampling;
        int newH=imageData.getHeight()/sampling;
        byte[] data=imageData.getObjectData();
        
        final int oldSize=data.length;
        
        int x,y,xx,yy,jj;
        
        int origLineLength;
        //black and white
        if(imageData.getWidth()*imageData.getHeight()==oldSize || decodeColorData.getID()==ColorSpaces.DeviceGray) {
            comp = 1;
        }
        
        final byte[] newData;
        
        if(hasIndex){ //hard-coded to 3 values
            newData=new byte[newW*newH*indexCount];
            origLineLength= imageData.getWidth();
        }else{
            newData=new byte[newW*newH*comp];
            origLineLength= imageData.getWidth()*comp;
        }
        //scan all pixels and down-sample
        for(y=0;y<newH;y++){
            for(x=0;x<newW;x++){
                
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
                
                int[] indexAv;
                for(jj=0;jj<comp;jj++){
                    int byteTotal=0;
                    int count=0;
                    int ptr;
                    final int newPtr;
                    //noinspection ObjectAllocationInLoop
                    indexAv=new int[indexCount];
                    //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                    for(yy=0;yy<hCount;yy++){
                        for(xx=0;xx<wCount;xx++){
                            
                            ptr=((yy+(y*sampling))*origLineLength)+(((x*sampling*comp)+(xx*comp)+jj));
                            if(ptr<oldSize){
                                if(!hasIndex){
                                    byteTotal += (data[ptr] & 255);
                                }else{
                                    for(int aa=0;aa<indexCount;aa++) {
                                        indexAv[aa] += (index[(((data[ptr] & 255) * indexCount) + aa)] & 255);
                                    }
                                    
                                }
                                
                                count++;
                            }
                        }
                    }
                    
                    //set value as white or average of pixels
                    if(hasIndex){
                        newPtr=jj+(x*indexCount)+(newW*y*indexCount);
                        for(int aa=0;aa<indexCount;aa++) {
                            newData[newPtr + aa] = (byte) ((indexAv[aa]) / count);
                        }
                    }else if(count>0){
                        newPtr=jj+(x*comp)+(newW*y*comp);
                        newData[newPtr]=(byte)((byteTotal)/count);
                    }
                }
            }
        }
        
        imageData.setObjectData(newData);
        imageData.setWidth(newW);
        imageData.setHeight(newH);
        
        return decodeColorData;
    }
}
