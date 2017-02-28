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
 * ImageCommands.java
 * ---------------
 */
package org.jpedal.parser.image;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.constants.PDFflags;
import org.jpedal.function.FunctionFactory;
import org.jpedal.function.PDFFunction;
import org.jpedal.io.*;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.*;
import org.jpedal.render.DynamicVectorRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class ImageCommands {
    
    public static final int ID=0;
    
    public static final int XOBJECT=2;
    
    @SuppressWarnings("CanBeFinal")
    static boolean sharpenDownsampledImages;
    
    public static boolean trackImages;
    
    public static boolean rejectSuperimposedImages=true;
    
    static{
        final String operlapValue=System.getProperty("org.jpedal.rejectsuperimposedimages");
        if(operlapValue!=null) {
            ImageCommands.rejectSuperimposedImages = (operlapValue.toLowerCase().contains("true"));
        }
        
        //hidden value to turn on function
        final String imgSetting=System.getProperty("org.jpedal.trackImages");
        if(imgSetting!=null) {
            trackImages = (imgSetting.toLowerCase().contains("true"));
        }
        
        final String nodownsamplesharpen=System.getProperty("org.jpedal.sharpendownsampledimages");
        if(nodownsamplesharpen!=null) {
            sharpenDownsampledImages = (nodownsamplesharpen.toLowerCase().contains("true"));
        }
        
    }
    
    /**
     * make transparent
     */
    static BufferedImage makeBlackandWhiteTransparent(final BufferedImage image) {
        
        final Raster ras=image.getRaster();
        
        final int w=ras.getWidth();
        final int h=ras.getHeight();
        
        //image=ColorSpaceConvertor.convertToARGB(image);
        final BufferedImage newImage=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        
        boolean validPixelsFound=false,transparent,isBlack;
        final int[] values=new int[3];
        
        final int[] transparentPixel={255,0,0,0};
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                
                //get raw color data
                ras.getPixels(x,y,1,1,values);
                
                //see if white
                transparent=(values[0]>245 && values[1]>245 && values[2]>245);
                isBlack=(values[0]<10 && values[1]<10 && values[2]<10);
                
                
                //if it matched replace and move on
                if(transparent || isBlack) {
                    newImage.getRaster().setPixels(x,y,1,1,transparentPixel);
                }else{
                    validPixelsFound=true;
                    
                    final int[] newPixel=new int[4];
                    
                    newPixel[3]=255;
                    newPixel[0]=values[0];
                    newPixel[1]=values[1];
                    newPixel[2]=values[2];
                    
                    newImage.getRaster().setPixels(x,y,1,1,newPixel);
                }
            }
        }
        
        if(validPixelsFound) {
            return newImage;
        } else {
            return null;
        }
        
    }
   
    /**
     * CMYK overprint mode
     */
    static BufferedImage simulateOP(BufferedImage image, final boolean whiteIs255) {
        
        final Raster ras=image.getRaster();
        image= ColorSpaceConvertor.convertToARGB(image);
        final int w=image.getWidth();
        final int h=image.getHeight();
        
        boolean hasNoTransparent=false;// pixelsSet=false;
        
        //reset
        //minX=w;
        //minY=h;
        //maxX=-1;
        //maxY=-1;
        
        final int[] transparentPixel={255,0,0,0};
        final int[] values=new int[4];
        
        boolean transparent;
        
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                
                //get raw color data
                ras.getPixel(x,y,values);
                
                //see if black
                if(whiteIs255){
                    transparent=values[0]>243 && values[1]>243 && values[2]>243;
                }else{
                    transparent=values[1]<3 && values[2]<3 && values[3]<3;
                }
                
                //if it matched replace and move on
                if(transparent){
                    image.getRaster().setPixel(x,y,transparentPixel);
                }else{
                    hasNoTransparent=true;
                }
            }
        }
        
        if(hasNoTransparent){
            return image;
        }else {
            return null;
        }
        
    }
    
    /**
     * @param maskCol
     */
    static void getMaskColor(final byte[] maskCol, final GraphicsState gs) {
        final int foreground =gs.nonstrokeColorSpace.getColor().getRGB();
        maskCol[0]=(byte) ((foreground>>16) & 0xFF);
        maskCol[1]=(byte) ((foreground>>8) & 0xFF);
        maskCol[2]=(byte) ((foreground) & 0xFF);
    }
    
    /**
     * Test whether the data representing a line is uniform along it height
     */
    static boolean isRepeatingLine(final byte[] lineData, final int height)
    {
        if(lineData.length % height != 0) {
            return false;
        }
        
        final int step = lineData.length / height;
        
        for(int x = 0; x < (lineData.length / height) - 1; x++) {
            int targetIndex = step;
            while(targetIndex < lineData.length - 1) {
                if(lineData[x] != lineData[targetIndex]) {
                    return false;
                }
                targetIndex += step;
            }
        }
        return true;
    }
    
    static BufferedImage simulateOverprint(final GenericColorSpace decodeColorData,
            final byte[] data, final boolean isDCT, final boolean isJPX, BufferedImage image,
            final int colorspaceID, final DynamicVectorRenderer current, final GraphicsState gs) {
        
        //simulate overPrint //currentGraphicsState.getNonStrokeOP() &&
        if((colorspaceID==ColorSpaces.DeviceCMYK || colorspaceID==ColorSpaces.ICC) && gs.getOPM()==1.0f){
            //if((colorspaceID==ColorSpaces.DeviceCMYK || colorspaceID==ColorSpaces.ICC) && gs.getOPM()==1.0f){
            
            //try to keep as binary if possible
            boolean isBlank=false;
            
            //indexed colors
            final byte[] index = decodeColorData.getIndexedMap();
            
            //see if allblack
            if(index==null && current.hasObjectsBehind(gs.CTM)){
                
                isBlank=true; //assume true and disprove
                for(int ii=0;ii<data.length;ii++){
                    //                    if(index!=null){
                    //                        int colUsed=(data[ii] &255)*3;
                    //                        if(colorspaceID+2<index.length && index[colUsed]==0 && index[colUsed+1]==0 && index[colUsed+2]==0){
                    //                            ii=data.length;
                    //                            isBlank=false;
                    //                        }
                    //                    }else
                    if(data[ii]!=0){
                        ii=data.length;
                        isBlank=false;
                    }
                }
            }
            
            //if so reject
            if(isBlank){
                image.flush();
                image=null;
            }else if(gs.getNonStrokeOP()){    
                if((isDCT || isJPX)){
                    image=ImageCommands.simulateOP(image,false);
                }else if(gs.getNonStrokeOP()){
                    if(colorspaceID==ColorSpaces.DeviceCMYK) {
                        image = ImageCommands.simulateOP(image, false);//image.getType()==1);
                    } else {
                        image = ImageCommands.simulateOP(image, image.getType() == 1);
                    }
                }
            }
        }
        
        return image;
    }
    
    
    static BufferedImage addBackgroundToMask(BufferedImage image, final boolean isMask) {
        
        if(isMask){
            
            final int cw = image.getWidth();
            final int ch = image.getHeight();
            
            final BufferedImage background=new BufferedImage(cw,ch,BufferedImage.TYPE_INT_RGB);
            final Graphics2D g2 = background.createGraphics();
            g2.setColor(Color.white);
            g2.fillRect(0, 0, cw, ch);
            g2.drawImage(image,0,0,null);
            image=background;
            
        }
        return image;
    }
    
    /**
     * apply TR
     */
    static BufferedImage applyTR(final BufferedImage image, final PdfObject TR, final PdfObjectReader currentPdfFile) {
        
        /**
         * get TR function first
         **/
        final PDFFunction[] functions =new PDFFunction[4];

        boolean hasFunction = false;

        int total=0;
        
        final byte[][] kidList = TR.getKeyArray(PdfDictionary.TR);
        
        if(kidList!=null) {
            total = kidList.length;
        }
        
        //get functions
        for(int count=0;count<total;count++){
            
            if(kidList[count]==null) {
                continue;
            }
            
            final String ref=new String(kidList[count]);
            PdfObject Function=new FunctionObject(ref);
            
            //handle /Identity as null or read
            final byte[] possIdent=kidList[count];
            if(possIdent!=null && possIdent.length>4 && possIdent[0]==47 &&  possIdent[1]==73 && possIdent[2]==100 &&  possIdent[3]==101)//(/Identity
            {
                Function = null;
            } else {
                currentPdfFile.readObject(Function);
            }
            
            /** setup the translation function */
            if(Function!=null) {
                functions[count] = FunctionFactory.getFunction(Function, currentPdfFile);
                hasFunction = true;
            }
            
        }
        
        if (!hasFunction){
          return image;
        }

        /**
         * apply colour transform
         */
        final Raster ras=image.getRaster();
        //image=ColorSpaceConvertor.convertToARGB(image);
        
        final int[] values=new int[4];
        
        for(int y=0;y<image.getHeight();y++){
            for(int x=0;x<image.getWidth();x++){
                
                
                //get raw color data
                ras.getPixels(x,y,1,1,values);
                
                for(int a=0;a<3;a++){
                    final float[] raw={values[a]/255f};
                    
                    if(functions[a]!=null){
                        final float[] processed=functions[a].compute(raw);
                        
                        values[a]= (int) (255*processed[0]);
                    }
                }
                
                image.getRaster().setPixels(x,y,1,1,values);
            }
        }
        
        return image;
        
    }
    
    
    /**
     * apply DecodeArray
     */
    static void applyDecodeArray(final byte[] data, final int d, final float[] decodeArray, final int type) {
        
        final int count = decodeArray.length;
        
        int maxValue=0;
        for (final float aDecodeArray : decodeArray) {
            if (maxValue < aDecodeArray) {
                maxValue = (int) aDecodeArray;
            }
        }
        
        /**
         * see if will not change output
         * and ignore if unnecessary
         */
        boolean isIdentify=true; //assume true and disprove
        final int compCount=decodeArray.length;
        
        for(int comp=0;comp<compCount;comp += 2){
            if((decodeArray[comp]!=0.0f)||((decodeArray[comp+1]!=1.0f)&&(decodeArray[comp+1]!=255.0f))){
                isIdentify=false;
                comp=compCount;
            }
        }
        
        if(isIdentify) {
            return;
        }
        
        if(d==1){ //bw straight switch (ignore gray)
            
            //changed for /baseline_screens/11dec/Jones contract for Dotloop.pdf
            if(decodeArray[0]>decodeArray[1]){
                
                //if(type!=ColorSpaces.DeviceGray){// || (decodeArray[0]>decodeArray[1] && XObject instanceof MaskObject)){
                final int byteCount=data.length;
                for(int ii=0;ii<byteCount;ii++){
                    data[ii]=(byte) ~data[ii];
                    
                }
            }
            
            /**
             * handle rgb
             */
        }else if((d==8 && maxValue>1)&&(type==ColorSpaces.DeviceRGB || type==ColorSpaces.CalRGB || type==ColorSpaces.DeviceCMYK)){
            
            int j=0;
            
            for(int ii=0;ii<data.length;ii++){
                int currentByte=(data[ii] & 0xff);
                if(currentByte<decodeArray[j]) {
                    currentByte = (int) decodeArray[j];
                } else if(currentByte>decodeArray[j+1]) {
                    currentByte = (int) decodeArray[j + 1];
                }
                
                j += 2;
                if(j==decodeArray.length) {
                    j = 0;
                }
                data[ii]=(byte)currentByte;
            }
        }else{
            /**
             * apply array
             *
             * Assumes black and white or gray colorspace
             * */
            maxValue = (d<< 1);
            final int divisor = maxValue - 1;
            
            for(int ii=0;ii<data.length;ii++){
                final byte currentByte=data[ii];
                
                int dd=0;
                int newByte=0;
                int min=0,max=1;
                for(int bits=7;bits>-1;bits--){
                    int current=(currentByte >> bits) & 1;
                    
                    current =(int)(decodeArray[min]+ (current* ((decodeArray[max] - decodeArray[min])/ (divisor))));
                    
                    /**check in range and set*/
                    if (current > maxValue) {
                        current = maxValue;
                    }
                    if (current < 0) {
                        current = 0;
                    }
                    
                    current=((current & 1)<<bits);
                    
                    newByte += current;
                    
                    //rotate around array
                    dd += 2;
                    
                    if(dd==count){
                        dd=0;
                        min=0;
                        max=1;
                    }else{
                        min += 2;
                        max += 2;
                    }
                }
                
                data[ii]=(byte)newByte;
                
            }
        }
    }
    
    static boolean isExtractionAllowed(final PdfObjectReader currentPdfFile) {
        
        final PdfFileReader objectReader=currentPdfFile.getObjectReader();
        
        final DecryptionFactory decryption=objectReader.getDecryptionObject();
        
        return decryption==null || decryption.getBooleanValue(PDFflags.IS_EXTRACTION_ALLOWED);
        
    }
}
