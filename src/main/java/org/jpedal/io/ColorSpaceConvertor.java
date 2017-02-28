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
 * ColorSpaceConvertor.java
 * ---------------
 */
package org.jpedal.io;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import org.jpedal.color.CMYKtoRGB;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.DeviceCMYKColorSpace;
import org.jpedal.utils.LogWriter;

/**
 * set of static methods to save/load objects to convert images between 
 * different colorspaces - 
 *
 * Several methods are very similar and I should recode my code to use a common
 * method for the RGB conversion 
 *
 * LogWriter is JPedal logging class
 *
 */
public class ColorSpaceConvertor {

    /** Flag to trigger raster printing */
    public static boolean isUsingARGB;
    
    /*
     * slightly contrived but very effective way to convert to RGB
     * @param width
     * @param height
     * @param data
     * @return
     */
    public static BufferedImage convertFromICCCMYK(final int width, final int height,byte[] data) {

        if(LogWriter.isOutput()) {
                LogWriter.writeLog("Converting ICC/CMYK colorspace to sRGB ");
            }
        
        try {

            /**make sure data big enough and pad out if not*/
            final int size = width * height * 4;
            if (data.length < size) {
                final byte[] newData = new byte[size];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }
            
            return profileConvertCMYKImageToRGB(data, width, height);
            
        } catch (final Exception ee) {

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception  " + ee + " converting from ICC colorspace");
            }
            ee.printStackTrace();
        }

        return null;

    }

    /**
     * convert any BufferedImage to RGB colourspace.
     *
     * @param image is of type BufferedImage
     * @return is of type BufferedImage
     */
    public static BufferedImage convertToRGB(BufferedImage image) {

        //don't bother if already rgb or ICC
        if ((image.getType() != BufferedImage.TYPE_INT_RGB)) {

            try{
                /**/
                final BufferedImage raw_image = image;
                image =
                        new BufferedImage(
                                image.getWidth(),
                                image.getHeight(),
                                BufferedImage.TYPE_INT_RGB);
                //ColorConvertOp xformOp = new ColorConvertOp(ColorSpaces.hints);/**/

                //THIS VERSION IS AT LEAST 5 TIMES SLOWER!!!
                //ColorConvertOp colOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), ColorSpaces.hints);
                //image=colOp.filter(image,null);

                //xformOp.filter(raw_image, image);
                new ColorConvertOp(ColorSpaces.hints).filter(raw_image, image);
                //image = raw_image;
            } catch (final Exception e) {

                e.printStackTrace();

                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " converting to RGB");
                }
            } catch (final Error ee) {

                ee.printStackTrace();
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Error " + ee + " converting to RGB");
                }
                //

                image=null;
            }
        }

        return image;
    }

    /**
     * convert a BufferedImage to RGB colourspace (used when I clip the image).
     *
     * @param image is of type BufferedImage
     * @return is type BufferedImage
     */
    public static BufferedImage convertToARGB(BufferedImage image) {

        //don't bother if already rgb
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            try {
                final BufferedImage raw_image = image;
                image =
                        new BufferedImage(
                                raw_image.getWidth(),
                                raw_image.getHeight(),
                                BufferedImage.TYPE_INT_ARGB);
                final ColorConvertOp xformOp = new ColorConvertOp(null);
                xformOp.filter(raw_image, image);
            } catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " creating argb image");
                }
            }
        }

        isUsingARGB = true;

        return image;
    }


    /**
     * save raw CMYK data by converting to RGB using algorithm method -
     * pdfsages supplied the C source and I have converted -
     * This works very well on most colours but not dark shades which are
     * all rolled into black
     *
     * This is what xpdf seems to use -
     * <b>Note</b> we store the output data in our input queue to reduce memory
     * usage - we have seen raw 2000 * 2000 images and having input and output
     * buffers is a LOT of memory -
     * I have kept the doubles in as I just rewrote Leonard's code -
     * I haven't really looked at optimisation beyond memory issues
     * 
     * @param buffer is of type byte[]
     * @param w is of type final int
     * @param h is of type final int
     * @return type BufferedImage
     */
    public static BufferedImage algorithmicConvertCMYKImageToRGB(final byte[] buffer, final int w, final int h) {

        BufferedImage image = null;
        final byte[] new_data = new byte[w * h * 3];

        final int pixelCount = w * h*4;

        double lastC=-1,lastM=-1.12,lastY=-1.12,lastK=-1.21;
        final double x=255;


        double c, m, y, aw, ac, am, ay, ar, ag, ab;
        double outRed=0, outGreen=0, outBlue=0;

        int pixelReached = 0;
        for (int i = 0; i < pixelCount; i += 4) {

            final double inCyan = (buffer[i]&0xff)/x ;
            final double inMagenta = (buffer[i + 1]&0xff) / x;
            final double inYellow = (buffer[i + 2]&0xff) / x;
            final double inBlack = (buffer[i + 3]&0xff) / x;

            if((lastC==inCyan)&&(lastM==inMagenta)&&
                    (lastY==inYellow)&&(lastK==inBlack)){
                //use existing values
            }else{//work out new
                final double k = 1;
                c = clip01(inCyan + inBlack);
                m = clip01(inMagenta + inBlack);
                y = clip01(inYellow + inBlack);
                aw = (k - c) * (k - m) * (k - y);
                ac = c * (k - m) * (k - y);
                am = (k - c) * m * (k - y);
                ay = (k - c) * (k - m) * y;
                ar = (k - c) * m * y;
                ag = c * (k - m) * y;
                ab = c * m * (k - y);
                outRed = x*clip01(aw + 0.9137 * am + 0.9961 * ay + 0.9882 * ar);
                outGreen = x*clip01(aw + 0.6196 * ac + ay + 0.5176 * ag);
                outBlue =
                        x*clip01(
                                aw
                                        + 0.7804 * ac
                                        + 0.5412 * am
                                        + 0.0667 * ar
                                        + 0.2118 * ag
                                        + 0.4863 * ab);

                lastC=inCyan;
                lastM=inMagenta;
                lastY=inYellow;
                lastK=inBlack;
            }

            new_data[pixelReached++] =(byte)(outRed);
            new_data[pixelReached++] = (byte) (outGreen);
            new_data[pixelReached++] = (byte) (outBlue);

        }

        try {
            /***/
            image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

            final Raster raster = createInterleavedRaster(new_data, w, h);
            image.setData(raster);

        } catch (final Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " with 24 bit RGB image");
            }
        }

        return image;
    }
    
    public static BufferedImage profileConvertCMYKImageToRGB(final byte[] buffer, final int w, final int h) {

    	final ColorSpace CMYK=DeviceCMYKColorSpace.getColorSpaceInstance();
    	
        BufferedImage image = null;
        final byte[] new_data = new byte[w * h * 3];

        final int pixelCount = w * h*4;

        float lastC=-1,lastM=-1,lastY=-1,lastK=-1;
        float C, M, Y, K;
        
        float[] rgb=new float[3];
        
        /**
         * loop through each pixel changing CMYK values to RGB
         */
        int pixelReached = 0;
        for (int i = 0; i < pixelCount; i += 4) {

            C = (buffer[i]&0xff)/255f;
            M = (buffer[i + 1]&0xff)/255f;
            Y = (buffer[i + 2]&0xff)/255f;
            K = (buffer[i + 3]&0xff)/255f;

            if(lastC==C && lastM==M && lastY==Y && lastK==K){
                //use existing values if not changed
            }else{//work out new
                
            	rgb=CMYK.toRGB(new float[]{C,M,Y,K});

                lastC=C;
                lastM=M;
                lastY=Y;
                lastK=K;
            }
            
            
            new_data[pixelReached++] =(byte)(rgb[0]*255);
            new_data[pixelReached++] = (byte) (rgb[1]*255);
            new_data[pixelReached++] = (byte) (rgb[2]*255);

        }

        /**
         * turn data into RGB image
         */
        try {
           
            image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
            final Raster raster = createInterleavedRaster(new_data, w, h);
            image.setData(raster);

        } catch (final Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " with 24 bit RGB image");
            }
        }

        return image;
    }

    /**
     * Convert YCbCr to RGB using formula.
     * @param buffer is of type byte[]
     * @param w is of type final int
     * @param h is of type final int
     * @return BufferedImage
     */
    public static BufferedImage algorithmicConvertYCbCrToRGB(final byte[] buffer, final int w, final int h) {


        BufferedImage image = null;
        final byte[] new_data = new byte[w * h * 3];

        int pixelCount = w * h*3;

        if(pixelCount>buffer.length) {
            pixelCount = buffer.length;
        }

        //boolean isAllBlack=true;

        int r=0,g=0,b=0;
        int lastY =-1, lastCb =-1, lastCr =-1;
        int pixelReached = 0;
        float val1;

        for (int i = 0; i < pixelCount; i += 3) {

            final int Y = ((buffer[i] & 255));
            final int Cb = ((buffer[1+i] & 255));
            final int Cr = ((buffer[2+i] & 255));

            if((lastY ==Y)&&(lastCb ==Cb)&&(lastCr ==Cr)){
                //use existing values
            }else{//work out new

                //System.out.println(Y + " " + Cb + ' ' + Cr);

                val1=298.082f*Y;

                r = (int)(((val1+(408.583f*Cr))/256f)-222.921);
                if(r<0) {
                    r = 0;
                }
                if(r>255) {
                    r = 255;
                }

                g = (int)(((val1-(100.291f*Cb)-(208.120f*Cr))/256f)+135.576f);
                if(g<0) {
                    g = 0;
                }
                if(g>255) {
                    g = 255;
                }

                b = (int)(((val1+(516.412f*Cb))/256f)-276.836f);
                if(b<0) {
                    b = 0;
                }
                if(b>255) {
                    b = 255;
                }

                //track blanks
//                if(Y==255 && Cr==0 && Cb==0) {
//
//                }else
//                    isAllBlack=false;

                //if (Y == 255 && Cr == Cb && (Cr!=0)) {

                // System.out.println(Y + " " + Cb + " " + Cr + " " + CENTER);

                //r = 255;
                //g = 255;
                //b = 255;

                //}

                //System.out.println(r+" "+g+ ' ' +b);

                lastY =Y;
                lastCb =Cb;
                lastCr =Cr;

            }

            new_data[pixelReached++] =(byte) (r);
            new_data[pixelReached++] = (byte) (g);
            new_data[pixelReached++] = (byte) (b);

        }

//            if(!nonTransparent || isAllBlack){
//
//               wasRemoved=true;
//               return null;
//            }

        try {
            /***/
            image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

            final Raster raster = createInterleavedRaster(new_data, w, h);
            image.setData(raster);

        } catch (final Exception e) {
            e.printStackTrace();

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " with 24 bit RGB image");
            }
        }

        return image;
    }


    public static BufferedImage convertIndexedToFlat(final int d, final int w, final int h, final byte[] data, final byte[] index, final boolean isARGB, final boolean isDownsampled) {

        BufferedImage image;
        final DataBuffer db;
        
        //assume true in case of 8 bit and disprove
        //not currently used
        final boolean isGrayscale=false;//d==8; //@change

        final int[] bandsRGB = {0,1,2};
        final int[] bandsARGB = {0,1,2,3};
        int[] bands;
        int components=3;


        if(isARGB){
            bands=bandsARGB;
            components=4;
        }else {
            bands = bandsRGB;
        }

        byte[] newData=convertIndexToRGBByte(index, w, h, components, d, data, isDownsampled, isARGB);
        
        /**create the image*/
        if(isARGB) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        } else if(isGrayscale) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        } else {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }

        if(isGrayscale){

            final byte[] grayData=new byte[w*h];
            int j=0;
            for(int i=0;i<newData.length;i += 3){
                grayData[j]=newData[i];
                j++;
            }

            bands = new int[]{0};

            final Raster raster =Raster.createInterleavedRaster(new DataBufferByte(grayData, grayData.length),w,h,w,1,bands,null);

            image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
            image.setData(raster);

        }else{
            db = new DataBufferByte(newData, newData.length);
            final WritableRaster raster =Raster.createInterleavedRaster(db,w,h,w * components,components,bands,null);
            image.setData(raster);
        }

        return image;
    }
    
    
    public static byte[] normaliseTo8Bit(int d, int w, int h, byte[] data) throws RuntimeException {
        
        if(d!=8){
            
            final int newSize=w*h;
            
            final byte[] newData=new byte[newSize];
            
            //Java needs 8 bit so expand out
            switch(d){
                
                case 1:
                    ColorSpaceConvertor.flatten1bpc(w, data, 0,null, false, newSize, 0,newData);
                    
                    break;
                    
                case 2:
                    ColorSpaceConvertor.flatten2bpc(w, data, null, false, newSize, newData);
                    
                    break;
                    
                case 4:
                    ColorSpaceConvertor.flatten4bpc(w, data,newSize, newData);
                    break;
                    
                case 16:
                    
                    for(int ptr=0;ptr<newSize;ptr++){
                        newData[ptr]=data[ptr*2];
                    }

                    break;
                    
                default:
                    //
                    
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("unknown comp= " + d);
                    }
            }
            
            data=newData;
            
        }
        return data;
    }
    
    public static byte[] convertIndexToRGBByte(final byte[] index, final int w, final int h, int components, final int d, final byte[] data, final boolean isDownsampled, final boolean isARGB) {
        
        int indexLength=0;
        if(index!=null) {
            indexLength = index.length;
        }
        final int length=(w * h * components);
        final byte[] newData =new byte[length];
        int id=0;
        float ratio=0f;
        switch(d){
            case 8:
                flatten8bpc(data, isDownsampled, ratio, id, length, newData, index, indexLength, isARGB);
                break;
                
            case 4:
                flatten4bpc(w, data, index, isARGB, length, newData);
                break;
                
            case 2:
                flatten2bpc(w, data, index, isARGB, length, newData);
                break;
                
            case 1:
                flatten1bpc(w, data, components,index, isARGB, length, 255,newData);
                break;
                
        }
        return newData;
    }

    public static void flatten8bpc(final byte[] data, final boolean isDownsampled, float ratio, int id, final int length, final byte[] newData, final byte[] index, int indexLength, final boolean isARGB) {
        int pt=0;
        
        for(int ii=0;ii< data.length-1;ii++){
            
            if(isDownsampled) {
                ratio = (data[ii] & 0xff) / 255f;
            } else {
                id = (data[ii] & 0xff) * 3;
            }
            
            if(pt>=length) {
                break;
            }
            
            //see - if really  grayscale all components the same
//                if(index==null)
//                    isGrayscale=isGrayscale && newData[pt]== newData[pt+1] && newData[pt]== newData[pt+2];
//                else
//                    isGrayscale=isGrayscale && index[id]== index[id+1] && index[id]== index[id+2];
            
            if(isDownsampled){
                if(ratio>0){
                    newData[pt++]= (byte) ((255- index[0])*ratio);
                    newData[pt++]= (byte) ((255- index[1])*ratio);
                    newData[pt++]= (byte) ((255- index[2])*ratio);
                }else {
                    pt += 3;
                }
            }else{
                if(id< indexLength){
                    newData[pt++]= index[id];
                    newData[pt++]= index[id+1];
                    newData[pt++]= index[id+2];
                }
            }
            
            if(isARGB){
                if(id==0 && ratio==0) {
                    newData[pt++] = (byte) 255;
                } else {
                    newData[pt++] = 0;
                }
            }
        }
    }

    private static void flatten4bpc(final int w, final byte[] data, final byte[] index, final boolean isARGB, final int length, final byte[] newData) {

        int id1,pt=0;
        final int[] shift={4,0};
        int widthReached=0;

        for (final byte aData : data) {

            for (int samples = 0; samples < 2; samples++) {

                id1 = ((aData >> shift[samples]) & 15) * 3;

                if (pt >= length) {
                    break;
                }

                newData[pt++] = index[id1];
                newData[pt++] = index[id1 + 1];
                newData[pt++] = index[id1 + 2];

                if (isARGB) {
                    if (id1 == 0) {
                        newData[pt++] = (byte) 0;
                    } else {
                        newData[pt++] = 0;
                    }
                }

                //ignore filler bits
                widthReached++;
                if (widthReached == w) {
                    widthReached = 0;
                    samples = 8;
                }
            }
        }
    }

    public static void flatten1bpc(final int w, final byte[] data, final int comp, final byte[] index, final boolean isARGB, final int length, final int transparency, final byte[] newData) {

        int pt=0;
        int id;//work through the bytes
        int widthReached=0;
        for (final byte aData : data) {

            for (int bits = 0; bits < 8; bits++) {

                //int id=((data[ii] & (1<<bits)>>bits))*3;
                id = ((aData >> (7 - bits)) & 1);

                if (pt >= length) {
                    break;
                }

//					@itemtoFix
                if (isARGB) {

                    id *= 3;
                    if (id == 0) {
                        newData[pt++] = index[id];
                        newData[pt++] = index[id + 1];
                        newData[pt++] = index[id + 2];

                        newData[pt++] = (byte) transparency;

                    } else {
                        newData[pt++] = index[id];
                        newData[pt++] = index[id + 1];
                        newData[pt++] = index[id + 2];

                        newData[pt++] = 0;
                        //System.out.println(id+" "+index[id]+" "+index[id+1]+" "+index[id+2]);
                    }

                } else {
                    if(index==null){
                        if(id==1){
                            newData[pt++] = (byte)255;
                        }else{
                            newData[pt++] = (byte)0;
                        }
                        
                    }else{
                        id *= comp;
                        for(int ii=0;ii<comp;ii++){
                            newData[pt++] = index[id+ii];
                        }
                    }

                }
                //ignore filler bits
                widthReached++;
                if (widthReached == w) {
                    widthReached = 0;
                    bits = 8;
                }
            }
        }
    }

    /**
     * convert to RGB or gray. If index is null we assume single component gray
     * @param w is of type int
     * @param data is of type byte[]
     * @param index is of type byte[]
     * @param isARGB is of type boolean
     * @param length is of type int
     * @param newData is of type byte[]
     */
    public static void flatten2bpc(final int w, final byte[] data, final byte[] index, final boolean isARGB, final int length, final byte[] newData) {

        int id1,pt=0;

        final int[] shift={6,4,2,0};
        int widthReached=0;

        for (final byte aData : data) {

            for (int samples = 0; samples < 4; samples++) {

                if (pt >= length) {
                    break;
                }

                if (index == null) {
                    id1 = ((aData << shift[3 - samples]) & 192);
                    if (id1 == 192) { //top value white needs to be 255 so trap
                        id1 = 255;
                    }
                    newData[pt++] = (byte) (id1);
                } else {
                    id1 = ((aData >> shift[samples]) & 3) * 3;

                    newData[pt++] = index[id1];
                    newData[pt++] = index[id1 + 1];
                    newData[pt++] = index[id1 + 2];

                    if (isARGB) {
                        if (id1 == 0) {
                            newData[pt++] = (byte) 0;
                        } else {
                            newData[pt++] = 0;
                        }
                    }
                }

                //ignore filler bits
                widthReached++;
                if (widthReached == w) {
                    widthReached = 0;
                    samples = 8;
                }
            }
        }
    }

/**
 * Convert YCC to CMY via formula and the CMYK to sRGB via profiles.
 * @param buffer is of type byte[]
 * @param w is of type int
 * @param h is of type int
 * @return BufferedImage 
 */
    public static BufferedImage iccConvertCMYKImageToRGB(final byte[] buffer, final int w, final int h) {

        final int pixelCount = w * h*4;
        int Y,Cb,Cr,CENTER,lastY=-1,lastCb=-1,lastCr=-1,lastCENTER=-1;

        int outputC=0, outputM=0,outputY=0;
        double R,G,B;
        //turn YCC in Buffer to CYM using profile
        for (int i = 0; i < pixelCount; i += 4) {

            Y=(buffer[i] & 255);
            Cb = (buffer[i+1] & 255);
            Cr = (buffer[i+2] & 255);
            CENTER = (buffer[i+3] & 255);

            if(Y==lastY && Cb==lastCb && Cr==lastCr && CENTER==lastCENTER){
                //no change so use last value
            }else{ //new value


                R = Y + 1.402 * Cr - 179.456;
                if(R<0d) {
                    R = 0d;
                } else if(R>255d) {
                    R = 255d;
                }
                
                G=Y - 0.34414 * Cb - 0.71414 * Cr + 135.45984;
                if(G<0d) {
                    G = 0d;
                } else if(G>255d) {
                    G = 255d;
                }
                
                B = Y + 1.772 * Cb - 226.816;
                if(B<0d) {
                    B = 0d;
                } else if(B>255d) {
                    B = 255d;
                }

                outputC = 255 - (int)R;
                outputM = 255 - (int)G;
                outputY = 255 - (int)B;

                //flag so we can just reuse if next value the same
                lastY=Y;
                lastCb=Cb;
                lastCr=Cr;
                lastCENTER=CENTER;
            }


            //put back as CMY
            buffer[i] = (byte) (outputC );
            buffer[i + 1] = (byte) (outputM );
            buffer[i + 2] = (byte) (outputY );

        }
        
        return CMYKtoRGB.convert(buffer,w,h);
        
    }

    /**
     * Convert a BufferedImage to RGB colourspace.
     * @param image is of type BufferedImage
     * @param newType is of type int
     * @return BufferedImage
     */
    public static BufferedImage convertColorspace(BufferedImage image, final int newType) {

        try {
            final BufferedImage raw_image = image;
            image =
                    new BufferedImage(
                            raw_image.getWidth(),
                            raw_image.getHeight(),
                            newType);
            final ColorConvertOp xformOp = new ColorConvertOp(null);
            xformOp.filter(raw_image, image);
        } catch (final Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " converting image");
            }

        }
        return image;
    }

    /**convenience method used to check value within bounds*/
    static double clip01(double value) {

        if (value < 0) {
            value = 0;
        }

        if (value > 1) {
            value = 1;
        }

        return value;
    }

    public static WritableRaster createCompatibleWritableRaaster(final ColorModel colorModel, final int w, final int h) {

        return colorModel.createCompatibleWritableRaster(w, h);
    }

    public static Raster createInterleavedRaster(final byte[] data, final int w, final int h) {

        final DataBuffer db = new DataBufferByte(data, data.length);
        final int[] bands = {0,1,2};
        return Raster.createInterleavedRaster(db,w,h,w * 3,3,bands,null);
    }

    public static void drawImage(final Graphics2D g2, final BufferedImage tileImg, final AffineTransform tileAff, final ImageObserver observer) {

        g2.drawImage(tileImg,tileAff,observer);

    }


    public static void flatten4bpc(final int w, final byte[] data, final int newSize, final byte[] newData) {

        final int origSize=data.length;

        byte rawByte;
        int ptr=0,currentLine=0;
        final boolean oddValues=((w & 1)==1);
        for(int ii=0;ii<origSize;ii++){
            rawByte=data[ii];

            currentLine += 2;
            newData[ptr]=(byte) (rawByte & 240);
            if(newData[ptr]==-16)   //fix for white
            {
                newData[ptr] = (byte) 255;
            }
            ptr++;

            if(oddValues && currentLine>w){ //ignore second value if odd as just packing
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
    }
    
    public static BufferedImage createRGBImage(int width, int height, byte[] data) {
        
      //  System.out.println("createARGBImage "+width+" "+height+" "+data.length);
        
        final DataBuffer db = new DataBufferByte(data, data.length);
        
        final int[] bands = { 0, 1, 2 };
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Raster raster = Raster.createInterleavedRaster(db, width, height, width * 3,3, bands, null);
        
        image.setData(raster);
        
        return image;
    }
    
    public static BufferedImage createARGBImage(int width, int height, byte[] data) {
        
      //  System.out.println("createARGBImage "+width+" "+height+" "+data.length);
        
        final DataBuffer db = new DataBufferByte(data, data.length);
        
        final int[] bands = { 0, 1, 2,3 };
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Raster raster = Raster.createInterleavedRaster(db, width, height, width * 4, 4, bands, null);
        
        image.setData(raster);
        
        return image;
    }
}
