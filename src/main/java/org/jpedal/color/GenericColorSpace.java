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
 * GenericColorSpace.java
 * ---------------
 */
package org.jpedal.color;

//standard java
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.jpedal.examples.handlers.DefaultImageHelper;
import org.jpedal.exception.PdfException;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import javax.imageio.metadata.IIOMetadataNode;
import com.idrsolutions.image.jpeg2000.Jpeg2000Decoder;

import org.w3c.dom.NodeList;

/**
 * Provides Color functionality and conversion for pdf
 * decoding
 */
public class GenericColorSpace  implements Cloneable, Serializable {
    
    boolean isConverted;

    /** actual raw value*/
    float[] rawValues;
    
    Map patterns; //holds new PdfObjects
    
    /**for Patterns*/
    float[][] CTM;
    
    /**size for indexed colorspaces*/
    private int size;
    
    /**holds cmyk values if present*/
    float c=-1;
    float y=-1;
    float m=-1;
    float k=-1;
    
    /**matrices for calculating CIE XYZ colour*/
    float[] W;
    float[] G;
    float[] Ma;
    //private float[] B;
    float[] R;
    
    /**defines rgb colorspace*/
    static ColorSpace rgbCS;
    
    public static final String cb = "<color ";
    
    public static final String ce = "</color>";
    
    //ID of colorspace (ie DeviceRGB)
    private int value = ColorSpaces.DeviceRGB;
    
    /**conversion Op for translating rasters or images*/
    static ColorConvertOp CSToRGB;
    
    ColorSpace cs;
    
    int type;
    
    PdfPaint currentColor = new PdfColor(0,0,0);
    
    /**rgb colormodel*/
    static ColorModel rgbModel;
    
    /**currently does nothing but added so we can introduce  profile matching*/
    @SuppressWarnings("CanBeFinal")
    private static ICC_Profile ICCProfileForRGB;
    
    
    /**enables optimisations for PDF output - enabled with jvm flag org.jpedal.fasterPNG*/
    public static boolean fasterPNG;
    
    //flag to show problem with colors
    boolean failed;
    
    int alternative=PdfDictionary.Unknown;
    
    private PdfObject decodeParms;
    
    private boolean hasYCCKimages;
    
    boolean isPrinting;

    private static final int[] bands4 = { 0, 1, 2, 3 };
    private int rawCSType;    

    public void setPrinting(final boolean isPrinting){
        
        //local handles
        this.isPrinting=isPrinting;
        
    }
    
    /**initialise all the colorspaces when first needed */
    protected static void initCMYKColorspace() throws PdfException {
        
        try {
            
            if(ICCProfileForRGB ==null){
                rgbModel =
                        new ComponentColorModel(
                        rgbCS,
                        new int[] { 8, 8, 8 },
                        false,
                        false,
                        ColorModel.OPAQUE,
                        DataBuffer.TYPE_BYTE);
            }else{
                final int compCount=rgbCS.getNumComponents();
                final int[] values=new int[compCount];
                for(int i=0;i<compCount;i++) {
                    values[i]=8;
                }
                
                rgbModel =
                        new ComponentColorModel( rgbCS,
                        values,
                        false,
                        false,
                        ColorModel.OPAQUE,
                        DataBuffer.TYPE_BYTE);
            }
            
            /**create CMYK colorspace using icm profile*/
            final ICC_Profile p =ICC_Profile.getInstance(GenericColorSpace.class.getResourceAsStream(
                    "/org/jpedal/res/cmm/cmyk.icm"));
            final ICC_ColorSpace cmykCS = new ICC_ColorSpace(p);
            
            /**define the conversion. PdfColor.hints can be replaced with null or some hints*/
            CSToRGB = new ColorConvertOp(cmykCS, rgbCS, ColorSpaces.hints);
        } catch (final Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e.getMessage() + " initialising color components");
            }
            
            throw new PdfException("[PDF] Unable to create CMYK colorspace. Check cmyk.icm in jar file");
            
        }
    }
    
    
    
    /**
     * reset any defaults if reused
     */
    public void reset(){
        
        currentColor = new PdfColor(0,0,0);
        
    }
    
    //show if problem and we should default to Alt
    public boolean isInvalid(){
        return failed;
    }
    
    //allow user to replace sRGB colorspace
    static{
        
        //enable user to disable some checks and used indexed output
        final String fasterPNG=System.getProperty("org.jpedal.fasterPNG");
        GenericColorSpace.fasterPNG=fasterPNG!=null && fasterPNG.equalsIgnoreCase("true");
        
        final String profile=System.getProperty("org.jpedal.RGBprofile");
        
        if(profile!=null){
            try{
                ICCProfileForRGB = ICC_Profile.getInstance(new FileInputStream(profile));
                
            }catch(final Exception e){
                e.printStackTrace();
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("[PDF] Problem " + e.getMessage() + " with ICC data ");
                }
                
                if(ICCProfileForRGB==null) {
                    throw new RuntimeException("Problem wth RGB profile "+profile+ ' ' +e.getMessage());
            }
        }
        }
        
        if(ICCProfileForRGB !=null){
            rgbCS=new ICC_ColorSpace(ICCProfileForRGB);
        }else {
            rgbCS=ColorSpace.getInstance(ColorSpace.CS_sRGB);
        }
        
    }
    
    /**
     * get size
     */
    public int getIndexSize(){
        return size;
    }
    
    /**
     * get color
     */
    public PdfPaint getColor()
    {
        return currentColor;
    }
    
    /**return the set Java colorspace*/
    public ColorSpace getColorSpace() {
        return cs;
    }
    
    GenericColorSpace() {
        
        cs=rgbCS;
    }
    
    protected void setAlternateColorSpace(final int alt){
        alternative = alt;
    }
    
    public int getAlternateColorSpace(){
        return alternative;
    }
    
    /**store color setting when we push to stack*/
    int r;
    int g;
    int b;
    
    
    
    public void restoreColorStatus(){
        
        currentColor=new PdfColor(r,g,b);
        
    }
    
    /**
     * clone graphicsState
     */
    @Override
    public Object clone()
    {
        //this.setColorStatus();
        
        final Object o;
        try{
            o = super.clone();
        }catch( final Exception e ){
            throw new RuntimeException("Unable to clone object "+e);
        }
        
        return o;
    }
    
    /**any indexed colormap*/
    byte[] IndexedColorMap;
    
    /**pantone name if present*/
    String pantoneName;
    
    /**number of colors*/
    int componentCount=3;
    
    /**handle to graphics state / only set and used by Pattern*/
    GraphicsState gs;
    
    int pageWidth,pageHeight;
    
    /**
     * <p>Convert DCT encoded image bytestream to sRGB</p>
     * <p>It uses the internal Java classes
     * and the Adobe icm to convert CMYK and YCbCr-Alpha - the data is still DCT encoded.</p>
     * <p>The Sun class JPEGDecodeParam.java is worth examining because it contains lots
     * of interesting comments</p>
     * <p>I tried just using the new IOImage.read() but on type 3 images, all my clipping code
     * stopped working so I am still using 1.3</p>
     */
    protected final BufferedImage nonRGBJPEGToRGBImage(
            final byte[] data, int w, int h, final float[] decodeArray, final int pX, final int pY) {
        
        boolean isProcessed=false;
        
        BufferedImage image = null;
        ByteArrayInputStream in = null;
        
        ImageReader iir=null;
        ImageInputStream iin=null;
        
        try {
            
            if(CSToRGB==null){
                initCMYKColorspace();
            }
            
            CSToRGB = new ColorConvertOp(cs, rgbCS, ColorSpaces.hints);
            
            in = new ByteArrayInputStream(data);
            
            final int cmykType=getJPEGTransform(data);
                
            //suggestion from Carol
            try{
                final Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG");
                
                while (iterator.hasNext())
                {
                    final Object o = iterator.next();
                    iir = (ImageReader) o;
                    if (iir.canReadRaster()) {
                        break;
                    }
                }
                
            }catch(final Exception e){
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Unable to find jars on classpath "+e);
                }
                
                return null;
            }
            
            //iir = (ImageReader)ImageIO.getImageReadersByFormatName("JPEG").next();
            ImageIO.setUseCache(false);
            
            iin = ImageIO.createImageInputStream((in));
            iir.setInput(iin, true);   //new MemoryCacheImageInputStream(in));
            
            Raster ras=iir.readRaster(0,null);
            
            //invert
            if(decodeArray!=null){
                
                //decodeArray=Strip.removeArrayDeleminators(decodeArray).trim();
                
                if((decodeArray.length==6 && decodeArray[0]==1f && decodeArray[1]==0f &&
                        decodeArray[2]==1f && decodeArray[3]==0f &&
                        decodeArray[4]==1f && decodeArray[5]==0f )||
                        (decodeArray.length>2 &&
                        decodeArray[0]==1f && decodeArray[1]==0)){
                    
                    final DataBuffer buf=ras.getDataBuffer();
                    
                    final int count=buf.getSize();
                    
                    for(int ii=0;ii<count;ii++) {
                        buf.setElem(ii,255-buf.getElem(ii));
                    }
                }else if(decodeArray.length==6 &&
                        decodeArray[0]==0f && decodeArray[1]==1f &&
                        decodeArray[2]==0f && decodeArray[3]==1f &&
                        decodeArray[4]==0f && decodeArray[5]==1f){
                    // }else if(decodeArray.indexOf("0 1 0 1 0 1 0 1")!=-1){//identity
                    // }else if(decodeArray.indexOf("0.0 1.0 0.0 1.0 0.0 1.0 0.0 1.0")!=-1){//identity
                }else if(decodeArray!=null && decodeArray.length>0){
                    //
                }
            }


            if(cs.getNumComponents()==1 && cmykType==0){ //it is actually gray
                image=JPEGDecoder.grayJPEGToRGBImage( data, pX, pY,  false);
                if(image!=null){
                    isProcessed=true;
                }
            }else if(cs.getNumComponents()==4){ //if 4 col CMYK of ICC translate

                isProcessed=true;

                try{
                    if(cmykType==2){

                        hasYCCKimages=true;
                        image = ColorSpaceConvertor.iccConvertCMYKImageToRGB(((DataBufferByte)ras.getDataBuffer()).getData(),w,h);

                    }else{

                        ras=cleanupRaster(ras,pX,pY,4);
                        w=ras.getWidth();
                        h=ras.getHeight();

                        image=CMYKtoRGB.convert(ras,w,h);
                 
                    }
                }catch(final Exception e){
                    e.printStackTrace();
                    
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Problem with JPEG conversion");
                    }
                    //<start-demo><end-demo>
                }
            }else if(cmykType!=0){
                
                image=iir.read(0);
                
                image=cleanupImage(image,pX,pY);
                
                isProcessed=true;
                
            }
            
            //test
            
            if(!isProcessed){
                /**1.3 version or vanilla version*/
                final WritableRaster rgbRaster;
                
                if (cmykType == 4) { //CMYK
                    
                    ras=cleanupRaster(ras,pX,pY,4);
                    
                    final int width = ras.getWidth();
                    final int height = ras.getHeight();
                    
                    rgbRaster =rgbModel.createCompatibleWritableRaster(width, height);
                    CSToRGB.filter(ras, rgbRaster);
                    image =new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
                    image.setData(rgbRaster);
                    
                } else { //type 7 - these seem to crash the new 1.4 IO routines as far as I can see
                    
                    boolean isYCC=false;
                    try{
                        final IIOMetadata metadata = iir.getImageMetadata(0);
                        final String metadataFormat = metadata.getNativeMetadataFormatName();
                        final IIOMetadataNode iioNode = (IIOMetadataNode) metadata.getAsTree(metadataFormat);
                        
                        final NodeList children = iioNode.getElementsByTagName("app14Adobe");
                        if (children.getLength() > 0) {
                            isYCC=true;
                        }
                    }catch(final Exception ee){
                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog("[PDF] Unable to read metadata on Jpeg "+ee);
                    }
                    }
                    
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("COLOR_ID_YCbCr image");
                    }
                    
                    if(isYCC){ //sample file debug2/pdf4134.pdf suggests we need this change
                        image=DefaultImageHelper.read(data);
                    }else{
                        //try with iccConvertCMYKImageToRGB(final byte[] buffer,int w,int h) and delete if works
                        image=ColorSpaceConvertor.algorithmicConvertYCbCrToRGB(((DataBufferByte)ras.getDataBuffer()).getData(),w,h);
                    }
                    
                    image=cleanupImage(image,pX,pY);
                    image = ColorSpaceConvertor.convertToRGB(image);
                }
            }
            
        } catch (final Exception ee) {
            image = null;
            ee.printStackTrace();
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
            }
        }catch(final Error err ){

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("JPeg error "+err);
            }
            if(iir!=null) {
                iir.dispose();
            }
            if(iin!=null){
                try {
                    iin.flush();
                } catch (final IOException e) {
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: "+e.getMessage());
                    }
                    //
                }
            }
        }
        
        try {
            if(in!=null){
                in.close();
            }
            if(iir!=null){
                iir.dispose();
            }
            if(iin!=null){
                iin.close();
            }
        } catch (final Exception ee) {
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Problem closing  " + ee);
            }
        }
        
        return image;
        
    }

    protected static BufferedImage cleanupImage(BufferedImage image, final int pX, final int pY){
        
        
        try{
            
            final int imageType=image.getType();
            
            if(getSampling(image.getWidth(), image.getHeight(), pX, pY)<=1 || imageType==BufferedImage.TYPE_CUSTOM){
                return image;
            }else if(imageType==BufferedImage.TYPE_3BYTE_BGR){
                return cleanupBGRImage(image, pX, pY);
            }else{
                if(imageType==5) {
                    image= ColorSpaceConvertor.convertToRGB(image);
                }
                
                final Raster ras=cleanupRaster(image.getData(),pX, pY, image.getColorModel().getNumColorComponents());
                
                
                image =new BufferedImage(ras.getWidth(),ras.getHeight(),image.getType());
                image.setData(ras);
                
                return image;
            }
            
        }catch(final Error err){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("[PDF] Error in cleanupImage "+err);
        }
        }
        
        return image;
    }
    
    private static int getSampling(final int w, final int h, final int pX, final int pY){
        
        int sampling=1; //keep as multiple of 2
        int newW=w,newH=h;
        
        if(pX>0 && pY>0){
            
            final int smallestH=pY<<2; //double so comparison works
            final int smallestW=pX<<2;
            
            //cannot be smaller than page
            while(newW>smallestW && newH>smallestH){
                sampling <<= 1;
                newW >>= 1;
                newH >>= 1;
            }
            
            int scaleX=w/pX;
            if(scaleX<1) {
                scaleX=1;
            }
            
            int scaleY=h/pY;
            if(scaleY<1) {
                scaleY=1;
            }
            
            //choose smaller value so at least size of page
            sampling=scaleX;
            if(sampling>scaleY) {
                sampling=scaleY;
        }
        }
        
        return sampling;
    }
    
    protected static Raster cleanupRaster(Raster ras, final int pX, final int pY, final int comp) {
        
        /**
         * allow user to disable this function and just return raw data
         */
        final String avoidCleanupRaster=System.getProperty("org.jpedal.avoidCleanupRaster");
        if(avoidCleanupRaster!=null && avoidCleanupRaster.toLowerCase().contains("true")){
            return ras;
        }
        
        byte[] buffer=null;
        int[] intBuffer=null;
        final int type;
        final DataBuffer data=ras.getDataBuffer();
        if(data instanceof DataBufferInt) {
            type=1;
        } else {
            type=0;
        }
        
        if(type==1) {
            intBuffer=((DataBufferInt)data).getData();
        } else{
            final int layerCount=ras.getNumBands();
            if(layerCount==comp){
                buffer=((DataBufferByte)data).getData();
            }else if(layerCount==1){
                final byte[] rawBuffer=((DataBufferByte)ras.getDataBuffer()).getData();
                final int size=rawBuffer.length;
                final int realSize=size*comp;
                int j=0,i=0;
                buffer=new byte[realSize];
                while(true){
                    for(int a=0;a<comp;a++){
                        buffer[j]=rawBuffer[i];
                        j++;
                    }
                    i++;
                    
                    if(i>=size) {
                        break;
                }
                }
            }else{
                //
            }
        }
        
        int sampling=1; //keep as multiple of 2
        
        final int w=ras.getWidth();
        final int h=ras.getHeight();
        
        int newW=w,newH=h;
        
        if(pX>0 && pY>0){
            
            final int smallestH=pY<<2; //double so comparison works
            final int smallestW=pX<<2;
            
            //cannot be smaller than page
            while(newW>smallestW && newH>smallestH){
                sampling <<= 1;
                newW >>= 1;
                newH >>= 1;
            }
            
            int scaleX=w/pX;
            if(scaleX<1) {
                scaleX=1;
            }
            
            int scaleY=h/pY;
            if(scaleY<1) {
                scaleY=1;
            }
            
            //choose smaller value so at least size of page
            sampling=scaleX;
            if(sampling>scaleY) {
                sampling=scaleY;
        }
        }
        
        //switch to 8 bit and reduce bw image size by averaging
        if(sampling>1){
            
            newW=w/sampling;
            newH=h/sampling;
            
            int x,y,xx,yy,jj,origLineLength=w;
            try{
                
                final byte[] newData=new byte[newW*newH*comp];
                
                if(type==0) {
                    origLineLength= w*comp;
                }
                
                for(y=0;y<newH;y++){
                    for(x=0;x<newW;x++){
                        
                        //allow for edges in number of pixels left
                        int wCount=sampling,hCount=sampling;
                        final int wGapLeft=w-x;
                        final int hGapLeft=h-y;
                        if(wCount>wGapLeft) {
                            wCount=wGapLeft;
                        }
                        if(hCount>hGapLeft) {
                            hCount=hGapLeft;
                        }
                        
                        for(jj=0;jj<comp;jj++){
                            int byteTotal=0,count=0;
                            //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                            for(yy=0;yy<hCount;yy++){
                                for(xx=0;xx<wCount;xx++){
                                    if(type==0) {
                                        byteTotal += (buffer[((yy+(y*sampling))*origLineLength)+(((x*sampling*comp)+(xx*comp)+jj))] & 255);
                                    } else {
                                        byteTotal += ((intBuffer[((yy+(y*sampling))*origLineLength)+(x*sampling)+xx]>>(8*(2-jj))) & 255);
                                    }
                                    
                                    count++;
                                }
                            }
                            
                            //set value as white or average of pixels
                            if(count>0) {
                                newData[jj+(x*comp)+(newW*y*comp)]=(byte)((byteTotal)/count);
                        }
                    }
                }
                }
                
                final int[] bands=new int[comp];
                for(int jj2=0;jj2<comp;jj2++) {
                    bands[jj2]=jj2;
                }
                
                
                ras=Raster.createInterleavedRaster(new DataBufferByte(newData,newData.length), newW, newH, newW*comp, comp, bands, null);
                
            }catch(final Exception e){
                
                e.printStackTrace();
                
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Problem with Image");
            }
            }
            
        }
        
        return ras;
    }
    
    private static BufferedImage cleanupBGRImage(BufferedImage img, final int pX, final int pY) {
        
        /**
         * allow user to disable this function and just return raw data
         */
        final String avoidCleanupRaster=System.getProperty("org.jpedal.avoidCleanupRaster");
        
        if(avoidCleanupRaster!=null && avoidCleanupRaster.toLowerCase().contains("true")){
            return img;
        }
        
        //hack to fix bug on Linux (also effects Windows 1.5 so disabled)
        if(System.getProperty("java.version").startsWith("1.5")){
            //if(PdfDecoder.isRunningOnLinux && System.getProperty("java.version").startsWith("1.5")){
            return img;
        }
        
        final Raster ras= img.getData();
        final int comp=img.getColorModel().getNumColorComponents();
        
        byte[] buffer=null;
        int[] intBuffer=null;
        final int type;
        final DataBuffer data=ras.getDataBuffer();
        if(data instanceof DataBufferInt) {
            type=1;
        } else {
            type=0;
        }
        
        if(type==1) {
            intBuffer=((DataBufferInt)data).getData();
        } else{
            final int layerCount=ras.getNumBands();
            if(layerCount==comp){
                buffer=((DataBufferByte)data).getData();
            }else if(layerCount==1){
                final byte[] rawBuffer=((DataBufferByte)ras.getDataBuffer()).getData();
                final int size=rawBuffer.length;
                final int realSize=size*comp;
                int j=0,i=0;
                buffer=new byte[realSize];
                while(true){
                    for(int a=0;a<comp;a++){
                        buffer[j]=rawBuffer[i];
                        j++;
                    }
                    i++;
                    
                    if(i>=size) {
                        break;
                }
                }
            }else{
                //
            }
        }
        
        int sampling=1; //keep as multiple of 2
        
        final int w=ras.getWidth();
        final int h=ras.getHeight();
        
        
        int newW=w,newH=h;
        
        if(pX>0 && pY>0){
            
            final int smallestH=pY<<2; //double so comparison works
            final int smallestW=pX<<2;
            
            //cannot be smaller than page
            while(newW>smallestW && newH>smallestH){
                sampling <<= 1;
                newW >>= 1;
                newH >>= 1;
            }
            
            int scaleX=w/pX;
            if(scaleX<1) {
                scaleX=1;
            }
            
            int scaleY=h/pY;
            if(scaleY<1) {
                scaleY=1;
            }
            
            //choose smaller value so at least size of page
            sampling=scaleX;
            if(sampling>scaleY) {
                sampling=scaleY;
        }
        }
        
        //switch to 8 bit and reduce bw image size by averaging
        if(sampling>1){
            
            final WritableRaster newRas=((WritableRaster)ras);
            
            newW=w/sampling;
            newH=h/sampling;
            
            int x,y,xx,yy,jj,origLineLength=w;
            try{
                
                
                final int[] newData=new int[comp];
                
                if(type==0) {
                    origLineLength= w*comp;
                }
                
                for(y=0;y<newH;y++){
                    for(x=0;x<newW;x++){
                        
                        //allow for edges in number of pixels left
                        int wCount=sampling,hCount=sampling;
                        final int wGapLeft=w-x;
                        final int hGapLeft=h-y;
                        if(wCount>wGapLeft) {
                            wCount=wGapLeft;
                        }
                        if(hCount>hGapLeft) {
                            hCount=hGapLeft;
                        }
                        
                        for(jj=0;jj<comp;jj++){
                            int byteTotal=0,count=0;
                            //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                            for(yy=0;yy<hCount;yy++){
                                for(xx=0;xx<wCount;xx++){
                                    if(type==0) {
                                        byteTotal += (buffer[((yy+(y*sampling))*origLineLength)+(((x*sampling*comp)+(xx*comp)+jj))] & 255);
                                    } else {
                                        byteTotal += ((intBuffer[((yy+(y*sampling))*origLineLength)+(x*sampling)+xx]>>(8*(2-jj))) & 255);
                                    }
                                    
                                    count++;
                                }
                            }
                            
                            //set value as white or average of pixels
                            if(count>0){
                                if(jj==0) {
                                    newData[2]=((byteTotal)/count);
                                } else if(jj==2) {
                                    newData[0]=((byteTotal)/count);
                                } else {
                                    newData[jj]=((byteTotal)/count);
                                }
                                
                            }
                            
                        }
                        
                        //write back into ras
                        newRas.setPixels(x, y,1,1, newData);//changed to setPixels from setPixel for JAVA ME
                        
                        //System.out.println(x+"/"+newW+" "+y+"/"+newH+" "+newData[0]+" "+newData[1]);
                    }
                }
                
                //put back data and trim
                img=new BufferedImage(newW,newH,img.getType());
                img.setData(newRas);
                //img=img.getSubimage(0,0,newW,newH); slower so replaced
                
            }catch(final Exception e){
                
                e.printStackTrace();
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Problem with Image");
            }
            }
            
        }
        
        return img;
    }
    
    /**Toms routine to read the image type - you can also use
     * int colorType = decoder.getJPEGDecodeParam().getEncodedColorID();
     */
    private static int getJPEGTransform(final byte[] data) {
        int xform = 0;
        
        final int dataLength=data.length;
        
        for (int i=0,imax=dataLength-2; i<imax; ) {
            
            final int type = data[i+1] & 0xff;	// want unsigned bytes!
            //out_.println("+"+i+": "+Integer.toHexString(type)/*+", len="+len*/);
            i += 2;	// 0xff and type
            
            if (type==0x01 || (0xd0 <= type&&type <= 0xda)) {
                
            } else if (type==0xda) {
                i = i + ((data[i]&0xff)<<8) + (data[i+1]&0xff);
                while (true) {
                    for ( ; i<imax; i++) {
                        if ((data[i]&0xff)==0xff && data[i+1]!=0) {
                            break;
                        }
                    }
                    final int rst = data[i+1]&0xff;
                    if (0xd0 <= rst&&rst <= 0xd7) {
                        i+=2;
                    } else {
                        break;
                    }
                }
                
            } else if(i+1<dataLength){
                
                /*if (0xc0 <= type&&type <= 0xcf) {	// SOF
                * Nf = data[i+7] & 0xff;	// 1, 3=YCbCr, 4=YCCK or CMYK
                * } else*/ if ((type == 0xee) &&	// Adobe
                     (data[i+2]=='A' && data[i+3]=='d' && data[i+4]=='o' && data[i+5]=='b' && data[i+6]=='e')) {
                        xform = data[i+13]&0xff;
                        break;
                    }
                i = i + ((data[i]&0xff)<<8) + (data[i+1]&0xff);
            }
        }
        
        return xform;
    }
    
    
    public void setIndex(final byte[] IndexedColorMap, final int size) {
        
        //		set the data for an object
        this.IndexedColorMap = IndexedColorMap;
        this.size=size;
        
        //	System.out.println("Set index ="+IndexedColorMap);
    }
    
    /**
     * lookup a component for index colorspace
     */
    protected int getIndexedColorComponent(final int count) {
        int value =  255;
        
        if(IndexedColorMap!=null){
            value=IndexedColorMap[count];
            
            if (value < 0) {
                value = 256 + value;
            }
            
        }
        return value;
        
    }
    
    /**return indexed COlorMap
     */
    public byte[] getIndexedMap() {
        
        //return IndexedColorMap;
        /**/
        if(IndexedColorMap==null) {
            return null;
        }
        
        final int size=IndexedColorMap.length;
        final byte[] copy=new byte[size];
        System.arraycopy(IndexedColorMap, 0, copy, 0, size);
        
        return copy;
        /**/
    }
    
    /**
     * convert color value to sRGB color
     */
    public void setColor(final String[] value, final int operandCount){
        
    }
    
    
    /**
     * convert color value to sRGB color
     */
    public void setColor(final float[] value, final int operandCount){
        
        //
        
    }
    
    /**
     * convert byte[] datastream JPEG to an image in RGB
     */
    public BufferedImage JPEGToRGBImage(final byte[] data, final int w, final int h, final float[] decodeArray, final int pX, final int pY, final boolean arrayInverted, final PdfObject XObject) {
        
        //see if LUV
        if(decodeParms!=null && decodeParms.getInt(PdfDictionary.ColorTransform)==1 && this.value!=ColorSpaces.DeviceGray) {
            return JPEGDecoder.JPEGToRGBImageFromLUV(data, pX, pY);
        }
        
        BufferedImage image;
        
        try {
            
            
            image=DefaultImageHelper.read(data);
            
            if(image!=null && !fasterPNG){
                
                if(value!=ColorSpaces.DeviceGray) { //crashes Linux
                    image=cleanupImage(image,pX,pY);
                }
                
                if(value!=ColorSpaces.DeviceGray) {
                    image=ColorSpaceConvertor.convertToRGB(image);
                }
                
            }
            
            //don ot alter to IOException as breaks file
        } catch (final Exception ee) {
            image = null;
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Problem reading JPEG: " + ee);
            }
            
            //
        }
        
        //if all else has failed try this
        if(image==null){
            image=JPEGDecoder.JPEGToRGBImageFromLUV(data, pX, pY);
        }
        
        if(arrayInverted && this.value ==ColorSpaces.DeviceGray ) {
            
            final DataBufferByte rgb = (DataBufferByte) image.getRaster().getDataBuffer();
            final byte[] rawData=rgb.getData();
            
            for(int aa=0;aa<rawData.length;aa++){  //flip the bytes
                rawData[aa]= (byte) (rawData[aa]^255);
            }
            
            image.setData(Raster.createRaster(image.getSampleModel(),new DataBufferByte(rawData,rawData.length),null));
        }
        
        return image;
    }
    
    /**
     * convert byte[] datastream JPEG to an image in RGB
     * @throws PdfException
     */
    public BufferedImage JPEG2000ToRGBImage(byte[] data, final int w, final int h, final float[] decodeArray, final int pX, final int pY, final int d) throws PdfException {

        BufferedImage image = null;

        try {
            Jpeg2000Decoder decoder = new Jpeg2000Decoder();
            image = decoder.read(data);
            IndexedColorMap = null;//make index null as we already processed
        } catch (Exception ex) {

            //
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in JPEG2000toImage  " + ex);
            }
        }
        
        if (image != null) {

            //does not work correctly if indexed so we manipulate the data
            //if you need to alter check  the images on page 42, 53, 71, 80, 89, 98, 107 and 114 are displayed in black
            //infinite/Plumbing_Fixtures_2_V1_replaced.pdf
            byte[] index = getIndexedMap();
            if (index != null) {

                if (value != ColorSpaces.DeviceRGB) {
                    index = convertIndexToRGB(index);
                    final int count = index.length;
                    for (byte i = 0; i < count; i++) {
                        index[i] = (byte) (index[i] ^ 255);
                    }
                }

                data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                image = ColorSpaceConvertor.convertIndexedToFlat(d, w, h, data, index, false, false);

            }

            image = cleanupImage(image, pX, pY);

            //ensure white background
            if (image.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
                final BufferedImage oldImage = image;
                final int newW = image.getWidth();
                final int newH = image.getHeight();
                image = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                final Graphics2D g2 = (Graphics2D) image.getGraphics();
                g2.setPaint(Color.WHITE);
                g2.fillRect(0, 0, newW, newH);
                g2.drawImage(oldImage, 0, 0, null);

            }

            ColorSpace cSpace = image.getColorModel().getColorSpace();
            int csType = cSpace.getType();
            int trnsType = image.getColorModel().getTransferType();
            if (image.getType() == BufferedImage.TYPE_CUSTOM
                    && (csType == ColorSpace.CS_sRGB || csType == ColorSpace.TYPE_RGB)
                    && trnsType == DataBuffer.TYPE_BYTE) {
                BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                int[] tempData = ((DataBufferInt) temp.getRaster().getDataBuffer()).getData();
                byte[] imgData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                int p = 0;
                for (int i = 0; i < tempData.length; i++) {
                    int rgb = ((imgData[p] & 0xFF) << 16) | ((imgData[p + 1] & 0xFF) << 8) | ((imgData[p + 2] & 0xFF));
                    tempData[i] = rgb;
                    p += 3;
                }
                image = temp;
            } else {
                image = ColorSpaceConvertor.convertToRGB(image);
            }
        }

        return image;
    }
    
    /**
     * default RGB implementation just returns data
     */
    public byte[] dataToRGBByteArray(final byte[] data, final int w, final int h, boolean arrayInverted){
        
        return data;
    }
    
    /**
     * convert color content of data to sRGB data
     */
    public BufferedImage dataToRGB(final byte[] data, final int w, final int h){
        
        final BufferedImage image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        final Raster raster = ColorSpaceConvertor.createInterleavedRaster(data,w,h);
        image.setData(raster);
        
        return image;
    }
    
    /**get colorspace ID*/
    public int getID(){
        return value;
    }
    
    /**
     * create a CIE values for conversion to RGB colorspace
     */
    public final void setCIEValues(final float[] W, final float[] R, final float[] Ma, final float[] G){
        
        /**set to CIEXYZ colorspace*/
        cs = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
        
        //set values
        this.G = G;
        this.Ma = Ma;
        this.W = W;
        //this.B = B;
        this.R = R;
        
    }
    
    /**
     * convert 4 component index to 3
     */
    byte[] convert4Index(byte[] data){
        
        final int compCount=4;
        
        if(value==ColorSpaces.DeviceCMYK){
            
            final int len=data.length;
            
            final byte[] rgb=new byte[len*3/4];
            int j2=0;
            
            for(int ii=0;ii<len;ii += 4){
                
                final float[] vals=new float[4];
                for(int j=0;j<4;j++) {
                    vals[j]=(data[ii+j] & 255)/255f;
                }
                this.setColor(vals,4);
                
                final int foreground=this.currentColor.getRGB();
                rgb[j2]=(byte) ((foreground>>16) & 0xFF);
                rgb[j2+1]=(byte) ((foreground>>8) & 0xFF);
                rgb[j2+2]=(byte) ((foreground) & 0xFF);
                
                j2 += 3;
                if(len-4-ii<4) {
                    ii=len;
                }
            }
            
            return rgb;
        }

        try {

            /**turn it into a BufferedImage so we can convert then extract the data*/
            final int width = data.length / compCount;
            final int height = 1;
            final DataBuffer db = new DataBufferByte(data, data.length);

            final WritableRaster raster = Raster.createInterleavedRaster(db, width, height, width * compCount, compCount, bands4, null);

            if (CSToRGB == null) {
                initCMYKColorspace();

                CSToRGB = new ColorConvertOp(cs, rgbCS, ColorSpaces.hints);
            }

            final WritableRaster rgbRaster =
                    rgbModel.createCompatibleWritableRaster(width, height);

            CSToRGB.filter(raster, rgbRaster);

            final DataBuffer convertedData = rgbRaster.getDataBuffer();

            /**put into byte array*/
            final int size = width * height * 3;
            data = new byte[size];


            for (int ii = 0; ii < size; ii++){
                data[ii] = (byte) convertedData.getElem(ii);
            }

        } catch (final Exception ee) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception  " + ee + " converting colorspace");
            }
        }

        return data;
    }
    
    /**
     * convert Index to RGB
     */
    public byte[] convertIndexToRGB(final byte[] index){
        
        return index;
    }
    
    /**
     * get an xml string with the color info
     */
    public String getXMLColorToken(){
        
        final String colorToken;
        
        //only cal if not set
        if(c==-1){ //approximate
            if(currentColor instanceof Color){
                final Color col=(Color)currentColor;
                final float c=(255-col.getRed())/255f;
                final float m=(255-col.getGreen())/255f;
                final float y=(255-col.getBlue())/255f;
                float k=c;
                if(k<m) {
                    k=m;
                }
                if(k<y) {
                    k=y;
                }
                
                if(pantoneName==null) {
                    colorToken=GenericColorSpace.cb+"C='"+c+"' M='"+m+"' Y='"+y+"' K='"+k+"' >";
                } else {
                    colorToken=GenericColorSpace.cb+"C='"+c+"' M='"+m+"' Y='"+y+"' K='"+k+"' pantoneName='"+pantoneName+"' >";
                }
            }else{
                colorToken=GenericColorSpace.cb+"type='shading'>";
            }
        }else{
            if(pantoneName==null) {
                colorToken=GenericColorSpace.cb+"C='"+c+"' M='"+m+"' Y='"+y+"' K='"+k+"' >";
            } else {
                colorToken=GenericColorSpace.cb+"C='"+c+"' M='"+m+"' Y='"+y+"' K='"+k+"' pantoneName='"+pantoneName+"' >";
        }
        }
        
        return colorToken;
    }
    
    /**
     * pass in list of patterns
     */
    public void setPattern(final Map patterns, final int pageWidth, final int pageHeight, final float[][] CTM) {
        
        this.patterns=patterns;
        
        this.pageWidth=pageWidth;
        this.pageHeight=pageHeight;
        this.CTM=CTM;
        //System.out.println("set pattern called");
    }
    
    /** used by generic decoder to asign color*/
    public void setColor(final PdfPaint col) {
        this.currentColor=col;
    }
    
    /**return number of values used for color (ie 3 for rgb)*/
    public int getColorComponentCount() {
        
        return componentCount;
    }
    
    /**pattern colorspace needs access to graphicsState*/
    public void setGS(final GraphicsState currentGraphicsState) {
        
        this.gs=currentGraphicsState;
        
    }

    /**return raw values - only currently works for CMYK*/
    public float[] getRawValues() {
        return rawValues;
    }
    
    /**
     * flag to show if YCCK image decoded so we can draw attention to user
     * @return
     */
    public boolean isImageYCCK() {
        return hasYCCKimages;
    }
    
    public void setDecodeParms(final PdfObject parms) {
        this.decodeParms=parms;
    }
    
    public boolean isIndexConverted() {
        return isConverted;
    }
    
    /**
     * method to flush any caching values for cases where may need resetting (ie in CMYK where restore will render
     * last values in setColor used for caching invalid
     */
    public void clearCache() {
        
    }
    
    public static ColorSpace getColorSpaceInstance() {
        
        ColorSpace rgbCS=ColorSpace.getInstance(ColorSpace.CS_sRGB);
        
        final String profile=System.getProperty("org.jpedal.RGBprofile");
        
        if(profile!=null){
            try{
                rgbCS=new ICC_ColorSpace(ICC_Profile.getInstance(new FileInputStream(profile)));
                
                System.out.println("use "+profile);
            }catch(final Exception e){
                e.printStackTrace();
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("[PDF] Problem " + e.getMessage() + " with ICC data ");
            }
        }
        }
        
        return rgbCS;
    }

    /**
     * ColorSpace.value in ICC
     * @return 
     */
    int getType() {
        return type;
    }
    
    /**
     * set PDF type (ColorSpaces variable)
     * @param rawValue 
     */
    void setType(int rawValue) {
        value=rawValue;
        rawCSType=rawValue;

    }

    void setRawColorSpace(int rawType) {
        rawCSType=rawType;
    }
    
    public int getRawColorSpacePDFType() {
        return rawCSType;
    }
    
    public BufferedImage JPEG2000ToImage(final byte[] data, final int pX, final int pY, final float[] decodeArray) throws PdfException {
           
        BufferedImage image=null;
        
        try {
            Jpeg2000Decoder decoder = new Jpeg2000Decoder();
            image = decoder.read(data);
            image = cleanupImage(image, pX, pY);
        } catch (Exception ex) {

            //
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in JPEG2000toImage  " + ex);
            }
        }
        
        return image;
        
    }
    
}
