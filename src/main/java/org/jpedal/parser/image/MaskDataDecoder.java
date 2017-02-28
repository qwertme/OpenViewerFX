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
* MaskDataDecoder.java
* ---------------
*/

package org.jpedal.parser.image;

import com.idrsolutions.pdf.color.shading.BitReader;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.JPEGDecoder;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.data.ImageData;

/**
 *
 * @author markee
 */
class MaskDataDecoder {
    
    /**
     * apply the SMask to image data directly as a component on argb
     *
     * @param imageData
     * @param decodeColorData
     * @param newSMask
     * @return
     */
    static byte[] applyMask(final ImageData imageData,final GenericColorSpace decodeColorData, final PdfObject newMask, final PdfObject XObject, byte[] maskDataSream) {
        
        final int[] maskArray=newMask.getIntArray(PdfDictionary.Mask);
    
        byte[] objectData=imageData.getObjectData();
        
        /*
        * Image data
        */
        int w=imageData.getWidth();
        int h=imageData.getHeight();
        int d=imageData.getDepth();
        
        
        objectData = convertData(decodeColorData, objectData, w, h, imageData, d, 1, null);
        
        XObject.setIntNumber(PdfDictionary.BitsPerComponent, 8);
       
        if(maskArray!=null){
            objectData=applyMaskArray(w, h, objectData,maskArray);
        }else{
             objectData=applyMaskStream(maskDataSream,imageData,decodeColorData, newMask, XObject);
        }
        
        //include Decode if present
        final float[] maskDecodeArray=newMask.getFloatArray(PdfDictionary.Decode);
        
        if(maskDecodeArray!=null) {
            ImageCommands.applyDecodeArray(objectData, maskDecodeArray.length / 2, maskDecodeArray, ColorSpaces.DeviceRGB);
        }
        
        return objectData;
    }
    
    static BufferedImage applyMaskArray(ImageData imageData, int[] maskArray){
        int bitDepth = imageData.getDepth();
//        int negate = 8-bitDepth;
        int nComp = maskArray.length/2;
        int dim = imageData.getWidth()*imageData.getHeight();
        
        BufferedImage img = new BufferedImage(imageData.getWidth(), imageData.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int output[] = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        byte [] data = imageData.getObjectData();
        int r,g,b,t,c,m,y,k;
        boolean isMask;
        
        switch(bitDepth){
            case 1:
            case 2:
            case 4:
                BitReader reader = new BitReader(data, true);
                for (int i = 0; i < dim; i++) {
                    
                    if(nComp == 1){
                       t = reader.getPositive(bitDepth);
                       isMask = t >= maskArray[0] && t<=maskArray[1];
                       if(!isMask){
                           t ^= 0xff;
                           output[i] = (255 << 24) | (t << 16) | (t << 8) | t;
                       }
                    }else{ //assume number of components is 3 at the moment we can fix in future;
                       r = reader.getPositive(bitDepth);
                       g = reader.getPositive(bitDepth);
                       b = reader.getPositive(bitDepth);
                       
                       isMask = r >= maskArray[0] && r<=maskArray[1] 
                               && g >= maskArray[2] && g<=maskArray[3]
                               && b >= maskArray[4] && b<=maskArray[5];
                       
                       if(!isMask){
                           r ^= 0xff;
                           g ^= 0xff;
                           b ^= 0xff;
                           output[i] = (255 << 24) | (r << 16) | (g << 8) | b;
                       }
                    }
                }                
                break;
            case 8:
                int p = 0;
                
                if(nComp ==1){
                    for (int i = 0; i < dim; i++) {         
                        t = data[p++]&0xff;
                        isMask = t >= maskArray[0] && t<=maskArray[1];
                        if(!isMask){
                            output[i] = (255 << 24) | (t << 16) | (t << 8) | t;
                        }
                    }
                }else if(nComp==3){
                    for (int i = 0; i < dim; i++) {
                        r = data[p++]&0xff;
                        g = data[p++]&0xff;
                        b = data[p++]&0xff;

                        isMask = r >= maskArray[0] && r<=maskArray[1] 
                                && g >= maskArray[2] && g<=maskArray[3]
                                && b >= maskArray[4] && b<=maskArray[5];

                        if(!isMask){
                            output[i] = (255 << 24) | (r << 16) | (g << 8) | b;
                        }
                    }
                }else if(nComp==4){
                    //
                }
                break;
        }
        return img;
        
    }

    
    
    static byte[] applySMask(byte[] maskData, final ImageData imageData,final GenericColorSpace decodeColorData, final PdfObject newSMask, final PdfObject XObject) {
        
        byte[] objectData=imageData.getObjectData();
        
        /*
        * Image data
        */
        int w=imageData.getWidth();
        int h=imageData.getHeight();
        int d=imageData.getDepth();
        
        /*
        * Smask data (ASSUME single component at moment)
        */
        final int maskW=newSMask.getInt(PdfDictionary.Width);
        final int maskH=newSMask.getInt(PdfDictionary.Height);
        final int maskD=newSMask.getInt(PdfDictionary.BitsPerComponent);
       
        objectData = convertData(decodeColorData, objectData, w, h, imageData, d, maskD, maskData);
        
        //needs to be 'normalised to 8  bit'
        if(maskD!=8){
            maskData=ColorSpaceConvertor.normaliseTo8Bit(maskD, maskW, maskH, maskData);
        }
        
        //add mask as a element so we now have argb
        if(w==maskW && h==maskH){
            //System.out.println("Same size");
            objectData=buildUnscaledByteArray(w, h, objectData, maskData);
        }else if(w<maskW){ //mask bigger than image
            //System.out.println("Mask bigger");
            objectData=upScaleImageToMask(w, h, maskW,maskH,objectData, maskData);
            
            XObject.setIntNumber(PdfDictionary.Width,maskW);
            XObject.setIntNumber(PdfDictionary.Height,maskH);
            
        }else{
            //System.out.println("Image bigger");
            objectData=upScaleMaskToImage(w, h, maskW,maskH,objectData, maskData);
        }
        
        XObject.setIntNumber(PdfDictionary.BitsPerComponent, 8);
       
//        BufferedImage img= ColorSpaceConvertor.createARGBImage( XObject.getInt(PdfDictionary.Width), XObject.getInt(PdfDictionary.Height), objectData);
//        
//        try{
//        ImageIO.write(img, "PNG", new java.io.File("/Users/markee/Desktop/img.png"));
//        }catch(Exception e){}
//        
        
        
        return objectData;
    }


    /**
     * apply the Mask streamto image data directly as a component on argb
     *
     * @param imageData
     * @param decodeColorData
     * @param newSMask
     * @return
     */
    static byte[] applyMaskStream(byte[] maskData, final ImageData imageData,final GenericColorSpace decodeColorData, final PdfObject newMask, final PdfObject XObject) {
        
        byte[] objectData=imageData.getObjectData();
        
        /*
        * Image data
        */
        int w=imageData.getWidth();
        int h=imageData.getHeight();

        /*
        * mask data (ASSUME single component at moment)
        */
        final int maskW=newMask.getInt(PdfDictionary.Width);
        final int maskH=newMask.getInt(PdfDictionary.Height);
        final int maskD=newMask.getInt(PdfDictionary.BitsPerComponent);
       
        //needs to be 'normalised to 8  bit'
        if(maskD!=8){
            maskData=ColorSpaceConvertor.normaliseTo8Bit(maskD, maskW, maskH, maskData);
        }
        
        //add mask as a element so we now have argb
        if(w==maskW && h==maskH){
            //System.out.println("Same size");
            objectData=buildUnscaledByteArray(w, h, objectData, maskData);
        }else if(w<maskW){ //mask bigger than image
            //System.out.println("Mask bigger");
            objectData=upScaleImageToMask(w, h, maskW,maskH,objectData, maskData);
            
            XObject.setIntNumber(PdfDictionary.Width,maskW);
            XObject.setIntNumber(PdfDictionary.Height,maskH);
            
        }else{
            //System.out.println("Image bigger");
            objectData=upScaleMaskToImage(w, h, maskW,maskH,objectData, maskData);
        }
        
        XObject.setIntNumber(PdfDictionary.BitsPerComponent, 8);
       
//        BufferedImage img= ColorSpaceConvertor.createARGBImage( XObject.getInt(PdfDictionary.Width), XObject.getInt(PdfDictionary.Height), objectData);
//        
//        try{
//        ImageIO.write(img, "PNG", new java.io.File("/Users/markee/Desktop/img.png"));
//        }catch(Exception e){}
//        
        
        
        return objectData;
    }

    static byte[] convertSmaskData(final GenericColorSpace decodeColorData, byte[] objectData, int w, int h, final ImageData imageData, int d, final int maskD, byte[] maskData, PdfObject smask) {
        
        byte[] index=decodeColorData.getIndexedMap();
       
        if(index!=null){
            index=decodeColorData.convertIndexToRGB(index);
            objectData=ColorSpaceConvertor.convertIndexToRGBByte(index, w, h, imageData.getCompCount(), imageData.getDepth(), objectData, false, false);
        }else if(decodeColorData.getID()==ColorSpaces.CalRGB){
        }else if(decodeColorData.getID()==ColorSpaces.DeviceRGB){
            
            if(d==8){ //baseline_screens/adobe/PP_download.pdf is actually 4 bit
                check4BitData(objectData);
            }
            
            float [] decodeArr = smask.getFloatArray(PdfDictionary.Decode);
        
            if(decodeArr!=null && decodeArr[0]==1 && decodeArr[1]==0){ // data inverted refer to dec2011/example.pdf
                for (int i = 0; i < maskData.length; i++) {
                    maskData[i]^= 0xff;
                }
            }
        }else if(decodeColorData.getID()==ColorSpaces.DeviceGray && imageData.isJPX()){
            
            if(maskData!=null && maskD==1){
                for(int ii=0;ii<maskData.length;ii++){
                    maskData[ii]=(byte) (maskData[ii]^255);
                }
            }
        }else if(!imageData.isDCT() && !imageData.isJPX()){
            
            //convert the data to rgb (last parameter is used in CalRGB so left in to make method same in all)
            objectData=decodeColorData.dataToRGBByteArray(objectData,w,h,false);
            
            //System.out.println(maskColorSpace.getParameterConstant(PdfDictionary.ColorSpace)+" "+newSMask.getObjectRefAsString());
            
        }
        return objectData;
    }
    
    static byte[] convertData(final GenericColorSpace decodeColorData, byte[] objectData, int w, int h, final ImageData imageData, int d, final int maskD, byte[] maskData) {
        
        byte[] index=decodeColorData.getIndexedMap();
       
        if(index!=null){
            index=decodeColorData.convertIndexToRGB(index);
            objectData=ColorSpaceConvertor.convertIndexToRGBByte(index, w, h, imageData.getCompCount(), imageData.getDepth(), objectData, false, false);
        }else if(decodeColorData.getID()==ColorSpaces.CalRGB){
        }else if(decodeColorData.getID()==ColorSpaces.DeviceRGB){
            
            if(d==8){ //baseline_screens/adobe/PP_download.pdf is actually 4 bit
                check4BitData(objectData);
            }
            
            if(maskData!=null && maskD==1){
                for(int ii=0;ii<maskData.length;ii++){
                    maskData[ii]=(byte) (maskData[ii]^255);
                }
            }
        }else if(decodeColorData.getID()==ColorSpaces.DeviceGray && imageData.isJPX()){
            
            if(maskData!=null && maskD==1){
                for(int ii=0;ii<maskData.length;ii++){
                    maskData[ii]=(byte) (maskData[ii]^255);
                }
            }
        }else if(!imageData.isDCT() && !imageData.isJPX()){
            
            //convert the data to rgb (last parameter is used in CalRGB so left in to make method same in all)
            objectData=decodeColorData.dataToRGBByteArray(objectData,w,h,false);
            
            //System.out.println(maskColorSpace.getParameterConstant(PdfDictionary.ColorSpace)+" "+newSMask.getObjectRefAsString());
            
        }
        return objectData;
    }

    static void check4BitData(final byte[] objectData) {
        final int size=objectData.length;
        
        boolean is4Bit=true;
        
        for(byte b:objectData){
            if(b<0 || b>15){
                is4Bit=false;
                break;
            }
        }
        
        if(is4Bit){
            for (int ii=0;ii<size;ii++){
                objectData[ii]=(byte) (objectData[ii]<<4);
            }
        }
    }
    
    
    private static byte[] upScaleMaskToImage(final int w, final int h, final int maskW, final int maskH, final byte[] objectData, final byte[] maskData) {
        
        int rgbPtr=0, aPtr;
        int i=0;
        float ratioW=(float)maskW/(float)w;
        float ratioH=(float)maskH/(float)h;
        byte[] combinedData=new byte[w*h*4];
        
        final int rawDataSize=objectData.length;
        
        try{
            for(int iY=0;iY<h;iY++){
                for(int iX=0;iX<w;iX++){
                    
                    //rgb
                    for(int comp=0;comp<3;comp++){
                        if(rgbPtr<rawDataSize){
                            combinedData[i+comp]=objectData[rgbPtr];
                        }
                        rgbPtr++;
                    }
                    
                    aPtr=(((int)(iX*ratioW)))+(((int)(iY*ratioH))*w);
                    
                    combinedData[i+3]=maskData[aPtr];
                    
                    i += 4;
                    
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return combinedData;
    }
    
    
    private static byte[] upScaleImageToMask(final int w, final int h, final int maskW, final int maskH, final byte[] objectData, final byte[] maskData) {
        
        int rgbPtr, aPtr=0;
        int i=0;
        float ratioW=(float)w/(float)maskW;
        float ratioH=(float)h/(float)maskH;
        byte[] combinedData=new byte[maskW*maskH*4];
        final int rawDataSize=objectData.length;
        final int maskSize=maskData.length;
        
        try{
            for(int mY=0;mY<maskH;mY++){
                for(int mX=0;mX<maskW;mX++){
                    
                    rgbPtr=(((int)(mX*ratioW))*3)+(((int)(mY*ratioH))*w*3);
                    
                    // System.err.println(mX+"/"+maskW+" "+mY+"/"+maskH+" "+ratioW+" mask="+((int)(mX*ratioW))+" "+((int)(mY*ratioH)));
                    //rgb
                    for(int comp=0;comp<3;comp++){
                        if(rgbPtr<rawDataSize){
                            combinedData[i+comp]=objectData[rgbPtr];
                        }
                        rgbPtr++;
                    }
                    
                    if(aPtr<maskSize){
                        combinedData[i+3]=maskData[aPtr];
                        aPtr++;
                    }
                    
                    i += 4;
                    
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return combinedData;
    }
    
     static byte[] getSMaskData(byte[] maskData,ImageData smaskData, PdfObject newSMask,GenericColorSpace maskColorData) {
        smaskData.getFilter(newSMask);
       
        if(smaskData.isDCT()){
            maskData=JPEGDecoder.getBytesFromJPEG(maskData,maskColorData,newSMask);
            newSMask.setMixedArray(PdfDictionary.Filter,null);
            newSMask.setDecodedStream(maskData);
        }else if(smaskData.isJPX()){
            maskData=JPeg2000ImageDecoder.getBytesFromJPEG2000(maskData,maskColorData,newSMask);
            newSMask.setMixedArray(PdfDictionary.Filter,null);
            newSMask.setDecodedStream(maskData);
        }
        return maskData;
    }
     
     
     private static byte[] applyMaskArray(final int w, final int h, final byte[] objectData, final int[] maskArray) {
         
         int pixels=w*h*4;
         int rgbPtr=0;
         byte[] combinedData=new byte[w*h*4];
         final int rawDataSize=objectData.length;
         
         float diff=0;
         
         if(maskArray!=null){
            diff=maskArray[1]-maskArray[0];
            if(diff>1f){
                diff /= 255f;
            }
         }
         
         try{
             for(int i=0;i<pixels;i += 4){
                 
                 //rgb
                 for(int comp=0;comp<3;comp++){
                     if(rgbPtr<rawDataSize){
                         if(diff>0){
                             combinedData[i+comp]=(byte) (objectData[rgbPtr]*diff);
                         }else{
                            combinedData[i+comp]=objectData[rgbPtr];
                         }
                     }
                     rgbPtr++;
                 }
                 
                 //opacity
                 combinedData[i+3]=(byte)255;
                 
             }
         }catch(Exception e){
             e.printStackTrace();
         }
        
        return combinedData;
    }

    
    private static byte[] buildUnscaledByteArray(final int w, final int h, final byte[] objectData, final byte[] maskData) {
        
        int pixels=w*h*4;
        int rgbPtr=0, aPtr=0;
        byte[] combinedData=new byte[w*h*4];
        final int rawDataSize=objectData.length;
        final int maskSize=maskData.length;
        
        try{
            for(int i=0;i<pixels;i += 4){
                
                //rgb
                for(int comp=0;comp<3;comp++){
                    if(rgbPtr<rawDataSize){
                        combinedData[i+comp]=objectData[rgbPtr];
                    }
                    rgbPtr++;
                }
                
                if(aPtr<maskSize){
                    //System.out.println(maskData[aPtr]);
                    combinedData[i+3]=maskData[aPtr];
                    aPtr++;
                }
                
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return combinedData;
    }
}
