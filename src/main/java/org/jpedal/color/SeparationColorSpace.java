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
 * SeparationColorSpace.java
 * ---------------
 */

package org.jpedal.color;

import org.jpedal.exception.PdfException;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;


import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.util.*;
import com.idrsolutions.image.jpeg2000.Jpeg2000Decoder;

/**
 * handle Separation ColorSpace and some DeviceN functions
 */
public class SeparationColorSpace extends GenericColorSpace {
    
    protected GenericColorSpace altCS;
    
    static final int Black=1009857357;
    static final int PANTONE_BLACK=573970506;
    private static final int Cyan=323563838;
    private static final int Magenta=895186280;
    public static final int Yellow=1010591868;
    
    protected ColorMapping colorMapper;
    
    //avoid rereading colorspaces
    //protected Map cachedValues=new HashMap();
    
    private float[] domain;
    
    /*if we use CMYK*/
    protected int cmykMapping=NOCMYK;
    
    protected static final int NOCMYK=-1;
    protected static final int MYK=1;
    protected static final int CMY=2;
    protected static final int CMK=4;
    protected static final int CY=5;
    protected static final int MY=6;
    protected static final int CM=8;
    
    protected static final int CMYK=7; //use don 6 values where CMYK is first 4
    
    protected static final int CMYB=9;
    
    public SeparationColorSpace() {}
    
    public SeparationColorSpace(final PdfObjectReader currentPdfFile, final PdfObject colorSpace) {
        
        setType(ColorSpaces.Separation);
        
        processColorToken(currentPdfFile, colorSpace);
    }
    
    protected void processColorToken(final PdfObjectReader currentPdfFile, PdfObject colorSpace) {
        
        final PdfObject indexed=colorSpace.getDictionary(PdfDictionary.Indexed);
        PdfObject functionObj=colorSpace.getDictionary(PdfDictionary.tintTransform);
        
        domain = null;
        
        //if(colorSpace.getDictionary(PdfDictionary.Process)!=null)
        //  isProcess=true;
        
        //name of color if separation or Components if device and component count
        byte[] name=null;
        byte[][] components=null;
        if(getID()==ColorSpaces.Separation){
            name=colorSpace.getStringValueAsByte(PdfDictionary.Name);
            
            if(name!=null) {
                components=new byte[][]{name};
            }
            componentCount=1;
        }else{
            components=colorSpace.getStringArray(PdfDictionary.Components);
            componentCount=components.length;
        }
        
        //test values
        
        cmykMapping=NOCMYK;
        
        final int[] values=new int[componentCount];
        if(components!=null){
            for(int ii=0;ii<componentCount;ii++){
                values[ii]=PdfDictionary.generateChecksum(1, components[ii].length-1, components[ii]);
            }
        }
        
        switch(componentCount){
            
            case 1:
                if(components!=null && (values[0]==Black || values[0]==PANTONE_BLACK)){
                    cmykMapping=Black;
                }
                
                break;
                
            case 2:
                
                if(values[0]==Cyan){
                    if(values[1]==Yellow) {
                        cmykMapping=CY;
                    } else if(values[1]==Magenta) {
                        cmykMapping=CM;
                    }
                }else if(values[0]==Magenta && values[1]==Yellow) {
                    cmykMapping=MY;
                }
                
                break;
                
            case 3:
                
                if(values[0]==Magenta && values[1]==Yellow && values[2]==Black) {
                    cmykMapping=MYK;
                } else if(values[0]==Cyan && values[1]==Magenta && values[2]==Yellow) {
                    cmykMapping=CMY;
                } else if(values[0]==Cyan && values[1]==Magenta && values[2]==Black) {
                    cmykMapping=CMK;
                }
                break;
                
            case 4:
                
                if(values[0]==Cyan && values[1]==Magenta && values[2]==Yellow && values[3]==Black) {
                    cmykMapping=CMYB;
                }
                break;
                
            case 5:
                
                if(values[0]==Cyan && values[1]==Magenta && values[2]==Yellow && values[3]==Black) {
                    cmykMapping=CMYK;
                }
                break;
                
            case 6:
                
                if(values[0]==Cyan && values[1]==Magenta && values[2]==Yellow && values[3]==Black) {
                    cmykMapping=CMYK;
                }
                break;
        }
        
        //hard-code myk and cmy
        if(cmykMapping!=NOCMYK){
            
            altCS=new DeviceCMYKColorSpace();
            
        }else{
            
            /**
             * work out colorspace (can also be direct ie /Pattern)
             */
            colorSpace=colorSpace.getDictionary(PdfDictionary.AlternateSpace);
            
            //System.out.println("Set uncached AlCS "+colorSpace.getObjectRefAsString()+" "+this);
            altCS =ColorspaceFactory.getColorSpaceInstance(currentPdfFile, colorSpace);
            
            //use alternate as preference if CMYK
            if(altCS.getID()==ColorSpaces.ICC && colorSpace.getParameterConstant(PdfDictionary.Alternate)==ColorSpaces.DeviceCMYK) {
                altCS=new DeviceCMYKColorSpace();
            }
            
            
        }
        
        if(name!=null){
            final int len=name.length;
            int jj=0;
            int topHex;
            int bottomHex;
            final byte[] tempName=new byte[len];
            for(int i=0;i<len;i++){
                if(name[i]=='#'){
                    //roll on past #
                    i++;
                    
                    topHex=name[i];
                    
                    //convert to number
                    if(topHex>='A' && topHex<='F') {
                        topHex -= 55;
                    } else if(topHex>='a' && topHex<='f') {
                        topHex -= 87;
                    } else if(topHex>='0' && topHex<='9') {
                        topHex -= 48;
                    }
                    
                    i++;
                    
                    while(name[i]==32 || name[i]==10 || name[i]==13) {
                        i++;
                    }
                    
                    bottomHex=name[i];
                    
                    if(bottomHex>='A' && bottomHex<='F') {
                        bottomHex -= 55;
                    } else if(bottomHex>='a' && bottomHex<='f') {
                        bottomHex -= 87;
                    } else if(bottomHex>='0' && bottomHex<='9') {
                        bottomHex -= 48;
                    }
                    
                    tempName[jj]=(byte) (bottomHex+(topHex<<4));
                }else{
                    tempName[jj]=name[i];
                }
                
                jj++;
            }
            
            //resize
            if(jj!=len){
                name=new byte[jj];
                System.arraycopy(tempName, 0, name, 0, jj);
                
            }
            
            pantoneName=new String(name);
        }
        
        /**
         * setup transformation
         **/
        if(functionObj==null) {
            colorSpace.getDictionary(PdfDictionary.tintTransform);
        }
        
        if(functionObj==null && indexed!=null) {
            functionObj=indexed.getDictionary(PdfDictionary.tintTransform);
        }
        
        colorMapper=new ColorMapping(currentPdfFile,functionObj);
        domain=functionObj.getFloatArray(PdfDictionary.Domain);
        
    }
    
    /**private method to do the calculation*/
    private void setColor(final float value){
           
        if(this.cmykMapping==Black){ //special case coded in
            
            final float[] newOp={0f,0f,0f, value};
            altCS.setColor(newOp,1);
            
        }else{
            //optimisation for white
            //if(altCS.getID()==ColorSpaces.DeviceCMYK && value==1.0){
            //    altCS.setColor(new float[]{0,0,0,0},4);
            //}else
            
            //adjust size if needed
            int elements=1;
            
            if(domain!=null) {
                elements=domain.length/2;
            }
            
            final float[] values = new float[elements];
            for(int j=0;j<elements;j++) {
                values[j] = value;
            }
            
            final float[] operand =colorMapper.getOperandFloat(values);
            
            altCS.setColor(operand,operand.length);
        
        }
    }
    
    /** set color (translate and set in alt colorspace */
    @Override
    public void setColor(final float[] operand, final int opCount) {
        
        setColor(operand[0]);
        
    }
    
    /** set color (translate and set in alt colorspace */
    @Override
    public void setColor(final String[] operand, final int opCount) {
        
        final float[] f=new float[1];
        f[0]=Float.parseFloat(operand[0]);
        
        setColor(f,1);
        
    }
    
    /**
     * convert data stream to srgb image
     */
    @Override
    public BufferedImage JPEGToRGBImage( final byte[] data, final int ww, final int hh, final float[] decodeArray, final int pX, final int pY, final boolean arrayInverted, final PdfObject XObject) {
        
        BufferedImage image;
        ByteArrayInputStream in = null;
        
        ImageReader iir=null;
        ImageInputStream iin=null;
        
        try {
            
            //read the image data
            in = new ByteArrayInputStream(data);
            
//iir = (ImageReader)ImageIO.getImageReadersByFormatName("JPEG").next();
            
            //suggestion from Carol
            final Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG");
            
            while (iterator.hasNext())
            {
                final Object o = iterator.next();
                iir = (ImageReader) o;
                if (iir.canReadRaster()) {
                    break;
                }
            }
            
            ImageIO.setUseCache(false);
            iin = ImageIO.createImageInputStream((in));
            iir.setInput(iin, true);
            Raster ras=iir.readRaster(0, null);
            
            ras=cleanupRaster(ras,pX,pY,1); //note uses 1 not count
            
            final int w = ras.getWidth();
            final int h = ras.getHeight();

            final DataBufferByte rgb = (DataBufferByte) ras.getDataBuffer();
            final byte[] rawData=rgb.getData();
            
            //special case
            if(this.altCS.getID()==ColorSpaces.DeviceGray){
                
                for(int aa=0;aa<rawData.length;aa++) {
                    rawData[aa]= (byte) (rawData[aa]^255);
                }
                final int[] bands = {0};
                image=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
                final Raster raster =Raster.createInterleavedRaster(new DataBufferByte(rawData, rawData.length),w,h,w,1,bands,null);
                
                image.setData(raster);
                
            }else{
                //convert the image in general case
                image=createImage(w, h, rawData, arrayInverted);
            }
        } catch (final Exception ee) {
            image = null;
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
            }
        }
        
        try {
            in.close();
            iir.dispose();
            iin.close();
        } catch (final Exception ee) {
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Problem closing  " + ee);
            }
        }
        
        return image;
        
    }
    
    /**
     * convert data stream to srgb image
     */
    @Override
    public BufferedImage  JPEG2000ToRGBImage(final byte[] data,int w,int h, final float[] decodeArray, final int pX, final int pY, final int d) throws PdfException{
        
        
        BufferedImage image;
        
        try{
           
            Jpeg2000Decoder decoder = new Jpeg2000Decoder();
            image = decoder.read(data);
            IndexedColorMap = null;//make index null as we already processed
            
            
            if(IndexedColorMap==null){ //avoid on index colorspaces
                image=cleanupImage(image,pX,pY);
            }
            
            final int iw = image.getWidth();
            final int ih = image.getHeight();
            
            final DataBufferByte rgb = (DataBufferByte) image.getRaster().getDataBuffer();
            final byte[] rawData=rgb.getData();
            
            //convert the image
            if(getID()==ColorSpaces.DeviceN){
                image=createImageN(iw, ih, rawData);
            }else{
                image=createImage(iw, ih, rawData, false);
            }
        } catch (final Exception ee) {
            image = null;
            
            //<end-demo>
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in JPEG2000ToRGBImage: " + ee);
            }
        }
        
        return image;
        
    }
    
    private BufferedImage createImageN(final int w, final int h, final byte[] rawData) {
        
        final BufferedImage image;
        
        final byte[] rgb=new byte[w*h*3];
        
        final int bytesCount=rawData.length;
        
        //convert data to RGB format
        int byteCount;
        if(IndexedColorMap!=null){
            byteCount=rawData.length;
        }else{
            byteCount= rawData.length/componentCount;
        }
        
        final float[] values=new float[componentCount];
        
        int j=0,j2=0,index;
        
        for(int i=0;i<byteCount;i++){
            
            if(j>=bytesCount) {
                break;
            }
            
            if(IndexedColorMap!=null){
                index=(rawData[i] & 255)*componentCount;
               
                for(int comp=0;comp<componentCount;comp++){
                    values[comp]=((IndexedColorMap[index+comp] & 255)/255f);
                }
            }else{
                for(int comp=0;comp<componentCount;comp++){
                    values[comp]=((rawData[j] & 255)/255f);
                    j++;
                }
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
    
    /**
     * convert separation stream to RGB and return as an image
     */
    @Override
    public BufferedImage  dataToRGB(final byte[] data, final int w, final int h) {
        
        BufferedImage image;
        
        try {
            
            //convert data
            image=createImage(w, h, data, false);
            
        } catch (final Exception ee) {
            image = null;
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Couldn't convert Separation colorspace data: " + ee);
            }
        }
        
        return image;
        
    }
    
    /**
     * convert separation stream to RGB and return as an image
     */
    @Override
    public byte[]  dataToRGBByteArray(final byte[] rgb, final int w, final int h, final boolean arrayInverted) {
        
        final int pixelCount=3*w*h;
        final byte[] imageData=new byte[pixelCount];
        
        //convert data to RGB format
        int pixelReached=0;
        
        //cache table for speed
        final float[][] lookuptable=new float[3][256];
        for(int i=0;i<256;i++) {
            lookuptable[0][i]=-1;
        }
        
        for (final byte aRgb : rgb) {
            
            final int value = (aRgb & 255);
            
            if (lookuptable[0][value] == -1) {
                if (arrayInverted) {
                    setColor(1f - (value / 255f));
                } else {
                    setColor(value / 255f);
                }
                
                lookuptable[0][value] = ((Color) this.getColor()).getRed();
                lookuptable[1][value] = ((Color) this.getColor()).getGreen();
                lookuptable[2][value] = ((Color) this.getColor()).getBlue();
                
            }
            
            for (int comp = 0; comp < 3; comp++) {
                imageData[pixelReached] = (byte) lookuptable[comp][value];
                pixelReached++;
            }
        }
        
        return imageData;
    }
    
    /**
     * turn raw data into an image
     */
    BufferedImage createImage(final int w, final int h, final byte[] rgb, final boolean arrayInverted) {
        
        final BufferedImage image;
        
        byte[]imageData=dataToRGBByteArray(rgb,w,h,arrayInverted);
        
        //create the RGB image
        image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        final Raster raster = ColorSpaceConvertor.createInterleavedRaster(imageData, w, h);
        image.setData(raster);
        
        return image;
    }
    
    /**
     * create rgb index for color conversion
     */
    @Override
    public byte[] convertIndexToRGB(final byte[] data){
        
        isConverted=true;
        
        final byte[] newdata=new byte[3*256]; //converting to RGB so size known
        
        try {
            
            int outputReached=0;
            float[] opValues;
            Color currentCol;
            float[] operand;
            final int byteCount=data.length;
            final float[] values = new float[componentCount];
            
            //scan each byte and convert
            for(int i=0;i<byteCount;i += componentCount){
                
                //turn into rgb and store
                if(this.componentCount==1 && getID()==ColorSpaces.Separation && colorMapper==null){ //separation (fix bug with 1 component DeviceN with second check)
                    opValues=new float[1];
                    opValues[1]= (data[i] & 255);
                    setColor(opValues,1);
                    currentCol=(Color)this.getColor();
                }else{ //convert deviceN
                    
                    for(int j=0;j<componentCount;j++) {
                        values[j] = (data[i+j] & 255)/255f;
                    }
                    
                    operand = colorMapper.getOperandFloat(values);
                    
                    altCS.setColor(operand,operand.length);
                    currentCol=(Color)altCS.getColor();
                    
                }
                
                newdata[outputReached]=(byte) currentCol.getRed();
                outputReached++;
                newdata[outputReached]=(byte)currentCol.getGreen();
                outputReached++;
                newdata[outputReached]=(byte)currentCol.getBlue();
                outputReached++;
                
            }
            
        } catch (final Exception ee) {
            
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception  " + ee + " converting colorspace");
            }
            
            //
            
        }
        
        return newdata;
    }
    
    /**
     * get color
     */
    @Override
    public PdfPaint getColor() {
        
        return altCS.getColor();
        
    }
    
    /**
     * clone graphicsState
     */
    @Override
    public final Object clone()
    {
        
        this.setColorStatus();
        
        final Object o;
        try{
            o = super.clone();
        }catch( final Exception e ){

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Unable to close "+e);
            }
            throw new RuntimeException("Unable to clone object");
        }
        
        return o;
    }
    
    private void setColorStatus(){
        
        final int foreground=altCS.currentColor.getRGB();
        
        r= ((foreground>>16) & 0xFF);
        g= ((foreground>>8) & 0xFF);
        b=((foreground) & 0xFF);
        
    }
    
    @Override
    public void restoreColorStatus(){
        
        altCS.currentColor=new PdfColor(r,g,b);
        
        //values may now be wrong in cache code so force reset
        altCS.clearCache();
    }
    
}
