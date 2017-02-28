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
 * MaskDecoder.java
 * ---------------
 */

package org.jpedal.parser.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.data.ImageData;
import org.jpedal.render.DynamicVectorRenderer;

/**
 *
 * @author markee
 */
public class MaskDecoder {
    
    /**
     * apply the Mask to image data directly as a component on argb
     *
     * @param imageData
     * @param decodeColorData
     * @param newSMask
     * @return
     */
    static byte[] applyMask(final ImageData imageData,final GenericColorSpace decodeColorData, final PdfObject newMask, final PdfObject XObject, byte[] maskDataSream) {
        
        
        int[] maskArray=newMask.getIntArray(PdfDictionary.Mask);
        if(maskArray!=null){
           maskArray=convertToRGB(maskArray,decodeColorData);
        }
       
        
        byte[] objectData=imageData.getObjectData();
        
        /*
        * Image data
        */
        int w=imageData.getWidth();
        int h=imageData.getHeight();
        int d=imageData.getDepth();
        
        objectData = MaskDataDecoder.convertData(decodeColorData, objectData, w, h, imageData, d, 1, null);
        
        XObject.setIntNumber(PdfDictionary.BitsPerComponent, 8);
        
        if(maskArray!=null){
            objectData=applyMaskArray(w, h, objectData,maskArray);
        }else{
             objectData=applyMaskStream(maskDataSream,imageData,decodeColorData, newMask, XObject);
        }
        
        
//        img= ColorSpaceConvertor.createARGBImage( XObject.getInt(PdfDictionary.Width), XObject.getInt(PdfDictionary.Height), objectData);
//        
//        try{
//        ImageIO.write(img, "PNG", new java.io.File("/Users/markee/Desktop/mixed.png"));
//        }catch(Exception e){}

        
        return objectData;
    }
    
    
     
     private static byte[] applyMaskArray(final int w, final int h, final byte[] objectData, final int[] maskArray) {
         
         int pixels=w*h*4;
         int rgbPtr=0;
         byte[] combinedData=new byte[w*h*4];
         final int rawDataSize=objectData.length;
         
         float[] diff=new float[3];
         
         if(maskArray!=null){
             for(int a=0;a<3;a++){
                 diff[a]=maskArray[1]-maskArray[0];
                 if(diff[a]>1f){
                     diff[a] /= 255f;
                 }
             }
         }
         
         try{
             for(int i=0;i<pixels;i += 4){
                 
                 
                 //rgb
                 if(rgbPtr+3<=rawDataSize && objectData[rgbPtr]==-1 && objectData[rgbPtr+1]==-1 && objectData[rgbPtr+2]==-1){ //transparent
                     rgbPtr += 3;
                     combinedData[i]=(byte)255;
                     combinedData[i+1]=(byte)255;
                     combinedData[i+2]=(byte)255;
                     combinedData[i+3]=(byte)0;
                 }else if(rgbPtr+3<=rawDataSize && objectData[rgbPtr]==0 && objectData[rgbPtr+1]==0 && objectData[rgbPtr+2]==0){ //transparent
                     rgbPtr += 3;
                     combinedData[i]=(byte)255;
                     combinedData[i+1]=(byte)0;
                     combinedData[i+2]=(byte)0;
                     combinedData[i+3]=(byte)0;
                 }else{
                     for(int comp=0;comp<3;comp++){
                         if(rgbPtr<rawDataSize){
                             if(1==2 && diff[comp]>0){
                                 combinedData[i+comp]=(byte) (objectData[rgbPtr]*diff[comp]);
                             }else{
                                 combinedData[i+comp]=objectData[rgbPtr];
                             }
                         }
                         rgbPtr++;
                     }
                     
                     //opacity
                     combinedData[i+3]=(byte)255;                    
                 }
             }
         }catch(Exception e){
             e.printStackTrace();
         }
         
        return combinedData;
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
        //int d=imageData.getDepth();
        
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
        
        final float[] maskDecodeArray=newMask.getFloatArray(PdfDictionary.Decode);
        if(maskDecodeArray!=null){
            float diff=maskDecodeArray[1]-maskDecodeArray[0];
            if(diff==-1){
                for(int i=0;i<maskData.length;i++){
                    maskData[i]=(byte) (maskData[i]^255);
                }
            }
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

    
    static BufferedImage createMaskImage(boolean isDownsampled, boolean isPrinting, GraphicsState gs, boolean isType3Font, DynamicVectorRenderer current, byte[] data, BufferedImage image, int w, int h, final ImageData imageData, final boolean imageMask, int d, GenericColorSpace decodeColorData, final byte[] maskCol, final String name) {
        
        //see if black and back object
        if(isDownsampled){
            /** create an image from the raw data*/
            final DataBuffer db = new DataBufferByte(data, data.length);
            
            final int[] bands = {0,1,2,3};
            image =new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            final Raster raster =Raster.createInterleavedRaster(db,w,h,w * 4,4,bands,null);
            image.setData(raster);
            
        }else{
            
            //try to keep as binary if possible
            boolean hasObjectBehind=true;
            
            //added as found file with huge number of tiny tiles
            if(h>=20 || imageData.getMode()!=ImageCommands.ID){ //not worth it for inline image
                hasObjectBehind = current.hasObjectsBehind(gs.CTM);
            }
            
            //remove empty images in some files
            boolean isBlank=false,keepNonTransparent=false;
            if(imageMask && d==1 && decodeColorData.getID()==ColorSpaces.DeviceRGB && maskCol[0]==0 && maskCol[1]==0 && maskCol[2]==0){
                
                //see if blank (assume true and disprove) and remove as totally see-through
                isBlank=true;
                for(int aa=0;aa<data.length;aa++){
                    if(data[aa]!=-1){
                        isBlank=false;
                        aa=data.length;
                    }
                }
                
                if(isPrinting && (imageData.getMode()==ImageCommands.ID || isType3Font || d==1)){ //avoid transparency if possible
                    final WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(data, data.length), w, h, 1, null);
                    image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);
                    image.setData(raster);
                    keepNonTransparent=true;
                }else if(isBlank){
                    image=null;
                    imageData.setRemoved(true);
                    
                }else{
                    final byte[] newIndex={(maskCol[0]),(maskCol[1]), (maskCol[2]),(byte)255,(byte)255,(byte)255};
                    image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, newIndex,true,true);
                }
            }
            
            if(!isBlank){  //done above so ignore
                if(!isPrinting && maskCol[0]==0 && maskCol[1]==0 && maskCol[2]==0 && !hasObjectBehind && !isType3Font && decodeColorData.getID()!=ColorSpaces.DeviceRGB){
                    
                    if(d==1){
                        final WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(data, data.length), w, h, 1, null);
                        image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);
                        image.setData(raster);
                        
                    }else{ //down-sampled above //never called
                        final int[] bands = {0};
                        
                        final Raster raster =Raster.createInterleavedRaster(new DataBufferByte(data, data.length),w,h,w,1,bands,null);
                        
                        image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
                        image.setData(raster);
                        
                    }
                }else if(!keepNonTransparent){
                    
                    if(d==8 && isDownsampled){ //never called
                        
                        final byte[] newIndex={(maskCol[0]),(maskCol[1]), (maskCol[2]),(byte)255,(byte)255,(byte)255};
                        image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, newIndex, true,true);
                        
                    }else if((w<4000 && h<4000)|| hasObjectBehind){   //needed for hires
                        final byte[] newIndex={maskCol[0],maskCol[1],maskCol[2],(byte)255,(byte)255,(byte)255};
                        image = ColorSpaceConvertor.convertIndexedToFlat(1,w, h, data, newIndex, true,false);
                        
                    }else{
                        //
                    }
                }
            }
        }
        return image;
    }  
    
     
    private static byte[] buildUnscaledByteArray(final int w, final int h, final byte[] objectData, final byte[] maskData) {
        
        int pixels=w*h*4;
        int rgbPtr=0, aPtr=0;
        byte[] combinedData=new byte[w*h*4];
        final int rawDataSize=objectData.length;
        
        try{
            for(int i=0;i<pixels;i += 4){
                
                if(maskData[aPtr]==-1){
                   
                    for(int comp=0;comp<3;comp++){
                        if(rgbPtr<rawDataSize){
                            combinedData[i+comp]=(byte) 255;
                        }
                        rgbPtr++;
                    }

                    combinedData[i+3]=(byte) 0;
                }else{
                
                    
                    for(int comp=0;comp<3;comp++){
                        if(rgbPtr<rawDataSize){
                            combinedData[i+comp]=objectData[rgbPtr];
                        }
                        rgbPtr++;
                    }

                    combinedData[i+3]=(byte) 255;
               
                }
                
                aPtr++;
                
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return combinedData;
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
                    
                    aPtr=(((int)(iX*ratioW)))+(((int)(iY*ratioH))*w);
                    
                    if(maskData[aPtr]==-1){
                        
                        //rgb
                        for(int comp=0;comp<3;comp++){
                            if(rgbPtr<rawDataSize){
                                combinedData[i+comp]=(byte)255;
                            }
                            rgbPtr++;
                        }
                        
                        
                        combinedData[i+3]=(byte) 0;
                    }else{
                        
                        
                        for(int comp=0;comp<3;comp++){
                            if(rgbPtr<rawDataSize){
                                combinedData[i+comp]=objectData[rgbPtr];
                            }
                            rgbPtr++;
                        }
                        
                        combinedData[i+3]=(byte) 255;
                        
                    }
                    
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
        
        try{
            for(int mY=0;mY<maskH;mY++){
                for(int mX=0;mX<maskW;mX++){
                    
                    rgbPtr=(((int)(mX*ratioW))*3)+(((int)(mY*ratioH))*w*3);
                    
                    if(maskData[aPtr]==-1){
                        
                        for(int comp=0;comp<3;comp++){
                            if(rgbPtr<rawDataSize){
                                combinedData[i+comp]=(byte)255;
                            }
                            rgbPtr++;
                        }
                        
                        combinedData[i+3]=(byte) 0;
                    }else{
                        
                        
                        for(int comp=0;comp<3;comp++){
                            if(rgbPtr<rawDataSize){
                                combinedData[i+comp]=objectData[rgbPtr];
                            }
                            rgbPtr++;
                        }
                        
                        combinedData[i+3]=(byte) 255;
                        
                    }
                   
                    aPtr++;
                    
                    i += 4;
                    
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return combinedData;
    }

    private static int[] convertToRGB(int[] intArray, GenericColorSpace decodeColorData) {
        
        byte[] index = decodeColorData.getIndexedMap();
        if(index!=null){
            
            int ptr;
            index=decodeColorData.convertIndexToRGB(index);
            
            int[] indexedArray=intArray;
            intArray=new int[6];
            
            for(int values=0;values<2;values++){
                
                ptr=indexedArray[values];
                
                intArray[0+(3*values)]=(byte) (index[0+(3*ptr)] & 0xFF);
                intArray[1+(3*values)]=(byte) (index[1+(3*ptr)] & 0xFF);
                intArray[2+(3*values)]=(byte) (index[2+(3*ptr)] & 0xFF);               
            }           
        }
        int comps=intArray.length/2;
        
        int[] rgbArray=new int[6];
        
        float[] rawColorData=new float[comps];
        for(int values=0;values<2;values++){
            for(int a=0;a<comps;a++){
                rawColorData[a]=intArray[a*2];
               
                if(rawColorData[a]>1){
                    rawColorData[a] /= 255f;
                }
                decodeColorData.setColor(rawColorData, comps);
            }
            
            int foreground=decodeColorData.getColor().getRGB();
            
            for(int a=0;a<comps;a++){
                rgbArray[0+(3*values)]=(byte) ((foreground>>16) & 0xFF);
                rgbArray[1+(3*values)]=(byte) ((foreground>>8) & 0xFF);
                rgbArray[2+(3*values)]=(byte) ((foreground) & 0xFF);
                
            }
        }
            
        return rgbArray;
    }
 
}
