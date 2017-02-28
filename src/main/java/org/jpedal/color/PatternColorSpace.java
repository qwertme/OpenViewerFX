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
 * PatternColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import com.idrsolutions.pdf.color.shading.ShadedPaint;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.jpedal.exception.PdfException;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.*;
import org.jpedal.parser.PdfStreamDecoderForPattern;
import org.jpedal.parser.ValueTypes;
import org.jpedal.render.*;
import org.jpedal.utils.LogWriter;


/**
 * handle Pattern ColorSpace (there is also a shading class)
 */
public class PatternColorSpace extends GenericColorSpace{
    
    boolean newFlag;
    
    //local copy so we can access File data
    private final PdfObjectReader currentPdfFile;
    
    private boolean colorsReversed;
    
    /**new pattern code image*/
    BufferedImage patternImage;
    
    PatternObject PatternObj;
    
    final GenericColorSpace patternColorSpace;
    
    float[][] matrix;
    
    PdfPaint strokCol;
    
    // Store the pattern cell for JavaFX
    private BufferedImage fullImage;
    /**
     * Just initialises variables
     * @param currentPdfFile
     */
    public PatternColorSpace(final PdfObjectReader currentPdfFile, final GenericColorSpace patternColorSpace){
        
        setType(ColorSpaces.Pattern);
        
        this.currentPdfFile = currentPdfFile;
        this.patternColorSpace=patternColorSpace;
        
        //default value for color
        currentColor = new PdfColor(1.0f,1.0f,1.0f);
    }
    
    /**
     * convert color value to pattern
     */
    @Override
    public void setColor(final String[] value_loc, final int operandCount){
        
        if(patternColorSpace!=null){
            
            final int elementCount=value_loc.length-1;
            final String[] colVals=new String[elementCount];
            for(int i=0;i<elementCount;i++) {
                colVals[i]=value_loc[elementCount-i];
            }
            
            patternColorSpace.setColor(colVals, elementCount);
            strokCol=patternColorSpace.getColor();
        }
        
        PatternObj=(PatternObject) patterns.get(value_loc[0]);
        
//        if(PatternObj.getObjectRefAsString().equals("614 0 R")){
//            currentColor=new PdfColor(0,255,0);
//            return ;
//        }
        
        /**
         * decode Pattern on first use
         */
        
        //ensure read
        currentPdfFile.checkResolved(PatternObj);
        
        //lookup table
        final byte[] streamData=currentPdfFile.readStream(PatternObj,true,true,true, false,false, PatternObj.getCacheName(currentPdfFile.getObjectReader()));
        
        //type of Pattern (shading or tiling)
        final int shadingType= PatternObj.getInt(PdfDictionary.PatternType);
        
        // get optional matrix values
        
        final float[] inputs=PatternObj.getFloatArray(PdfDictionary.Matrix);
        
        if(inputs!=null){
            
            if(shadingType==1){
                final float[][] Nmatrix={{inputs[0],inputs[1],0f},{inputs[2],inputs[3],0f},{0f,0f,1f}};
                
                if(!newFlag && inputs[5]<0){
                        inputs[4]=0;
                        inputs[5]=0;
                }
                matrix=Nmatrix;
            }else{
                final float[][] Nmatrix={{inputs[0],inputs[1],0f},{inputs[2],inputs[3],0f},{inputs[4],inputs[5],1f}};
                
                colorsReversed = Nmatrix[2][0] < 0;
                
//                matrix=Matrix.multiply(Nmatrix,CTM); //comment out in order to match with spec
                matrix = Nmatrix;
            }
        }
        
        if(!newFlag){
            /**
             * setup appropriate type
             */
            if(shadingType == 1) { //tiling
                 currentColor = setupTilingNew(PatternObj,streamData);  
            } else if(shadingType == 2) { //shading                
                currentColor = setupShading(PatternObj,matrix);
            }
        }
    }
    
    
    public BufferedImage getImageForPatternedShape(GraphicsState gs){
        
        float mm[][] = gs.CTM;
        AffineTransform gsAffine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);
        
        currentPdfFile.checkResolved(PatternObj);
        final byte[] streamData=currentPdfFile.readStream(PatternObj,true,true,true, false,false, PatternObj.getCacheName(currentPdfFile.getObjectReader()));
        final int patternType= PatternObj.getInt(PdfDictionary.PatternType);
        
        if(patternType != 1){ //currently support only tiling, shading pattern not supported yet
            return null;
        }
        
        AffineTransform affine = new AffineTransform();

        float[] inputs = PatternObj.getFloatArray(PdfDictionary.Matrix);
        if (inputs != null) {
            mm = new float[][]{{inputs[0], inputs[1], 0f}, {inputs[2], inputs[3], 0f}, {inputs[4], inputs[5], 1f}};
            affine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);
        }       
        
        affine.concatenate(gsAffine);
        mm = getMatrix(affine);
        
        boolean isRotated = affine.getShearX()!=0 || affine.getShearY()!=0;
        
        if(isRotated){
            affine = new AffineTransform();
            mm = new float[][]{{1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f}};
        }
        
        final float[] rawBBox = PatternObj.getFloatArray(PdfDictionary.BBox);

        final float xGap = Math.abs(rawBBox[2] - rawBBox[0]);
        final float yGap = Math.abs(rawBBox[1] - rawBBox[3]);

        GeneralPath rawPath = new GeneralPath();
        rawPath.moveTo(rawBBox[0], rawBBox[1]);
        rawPath.lineTo(rawBBox[2], rawBBox[1]);
        rawPath.lineTo(rawBBox[2], rawBBox[3]);
        rawPath.lineTo(rawBBox[0], rawBBox[3]);
        rawPath.lineTo(rawBBox[0], rawBBox[1]);
        rawPath.closePath();
        Shape rawShape = rawPath.createTransformedShape(affine);
        Rectangle2D rawRect = rawShape.getBounds2D();

        float rawXStep = PatternObj.getFloatNumber(PdfDictionary.XStep);
        rawXStep = (30000 > Short.MAX_VALUE || rawXStep < -30000) ? 0f : rawXStep;
        float rawYStep = PatternObj.getFloatNumber(PdfDictionary.YStep);
        rawYStep = (30000 > Short.MAX_VALUE || rawYStep < -30000) ? 0f : rawYStep;

        float[] bbox = new float[4];

        if (rawXStep < 0) {
            bbox[2] = xGap - rawXStep;
        } else {
            bbox[2] = rawXStep;
        }
        if (rawYStep < 0) {
            bbox[3] = yGap - rawYStep;
        } else {
            bbox[3] = rawYStep;
        }

        GeneralPath boxPath = new GeneralPath();
        boxPath.moveTo(bbox[0], bbox[1]);
        boxPath.lineTo(bbox[2], bbox[1]);
        boxPath.lineTo(bbox[2], bbox[3]);
        boxPath.lineTo(bbox[0], bbox[3]);
        boxPath.lineTo(bbox[0], bbox[1]);
        boxPath.closePath();
        Shape boxShape = boxPath.createTransformedShape(affine);
        Rectangle2D boxRect = boxShape.getBounds2D();

        double imageW = (Math.abs(boxRect.getX()) + boxRect.getWidth()) - (Math.abs(rawRect.getX()));
        double imageH = (Math.abs(boxRect.getY()) + boxRect.getHeight()) - (Math.abs(rawRect.getY()));

        imageW = rawXStep == 0 ? rawRect.getWidth() : imageW;
        imageH = rawYStep == 0 ? rawRect.getWidth() : imageH;

        imageW = imageW > 3000 ? 1500 : imageW;
        imageH = imageH > 3000 ? 1500 : imageH;
        
        int iw = (int) (imageW);
        iw = iw < 1 ? 1 : iw;
        int ih = (int) (imageH);
        ih = ih < 1 ? 1 : ih;

        if (imageH < 1 && imageW < 2.5) {
            iw = 1;
        }

        final ObjectStore localStore = new ObjectStore();
        BufferedImage image;
        final DynamicVectorRenderer glyphDisplay;
        
//        iw = 1000;
//        ih = 1000;

        if (affine.getScaleX() < 0 || affine.getScaleY() < 0) {
            glyphDisplay = decodePatternContent(PatternObj, mm, streamData, localStore);
            image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g2 = image.createGraphics();
            glyphDisplay.setG2(g2);
            AffineTransform moveAffine = new AffineTransform();
            moveAffine.setToTranslation(-rawRect.getX(), -rawRect.getY());
            glyphDisplay.paint(null, moveAffine, null);

        } else {
            glyphDisplay = decodePatternContent(PatternObj, null, streamData, localStore);
            double[] rd = new double[6];
            affine.getMatrix(rd);
            rd[4] -= rawRect.getX();
            rd[5] -= rawRect.getY();
            AffineTransform rdAffine = new AffineTransform(rd);
            image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g2 = image.createGraphics();
            glyphDisplay.setG2(g2);
            glyphDisplay.paint(null, rdAffine, null);

        }
        
        return image;   
//        //flip it for using in viewer
////        if(gsAffine.getScaleY()<0){
////            AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
////            tx.translate(0, -image.getHeight(null));
////            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
////            image = op.filter(image, null);
////        }
////        
//        System.out.println(gsAffine);
//        System.out.println(rawRect);
////        Rectangle2D fRect = new Rectangle2D.Double(rawRect.getX(), rawRect.getY(), imageW, imageH);
//        Rectangle2D fRect = new Rectangle2D.Double(0,0,image.getWidth(),image.getHeight());
//        TexturePaint paint;
//        if(isRotated){
//            paint = new ShearedTexturePaint(image, fRect, rotatedAffine);            
//        }else{
//            paint = new PdfTexturePaint(image, fRect);
//        }
//        
//        BufferedImage img = new BufferedImage((int)imageW,(int)imageH, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D  imgG = (Graphics2D)img.createGraphics();
//        imgG.setPaint(paint);
//        imgG.fillRect(0, 0, pw, ph);
//        try {
//            javax.imageio.ImageIO.write(image, "png", new java.io.File("C:\\Users\\suda\\Desktop\\testimages\\10-"+System.currentTimeMillis()+".png"));
//            javax.imageio.ImageIO.write(img, "png", new java.io.File("C:\\Users\\suda\\Desktop\\testimages\\11-"+System.currentTimeMillis()+".png"));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        
    }

    private static float[][] getMatrix(AffineTransform af){
        return new float[][]{{(float)af.getScaleX(), (float)af.getShearX(), 0f}, {(float)af.getShearY(), (float)af.getScaleY(), 0f}, {(float)af.getTranslateX(), (float)af.getTranslateY(), 1f}};
    }
    
    public BufferedImage getRawImage(int iw, int ih, AffineTransform callerAffine){
        byte[] streamData=currentPdfFile.readStream(PatternObj,true,true,true, false,false, PatternObj.getCacheName(currentPdfFile.getObjectReader()));
        final ObjectStore localStore = new ObjectStore();
        //float[] inputs = PatternObj.getFloatArray(PdfDictionary.Matrix);
        AffineTransform pattern = new AffineTransform();        
        pattern.concatenate(callerAffine);
        final DynamicVectorRenderer glyphDisplay = decodePatternContent(PatternObj, getMatrix(pattern), streamData, localStore);
        return glyphDisplay.getSingleImagePattern();        
    }

    private PdfPaint setupTilingNew(final PdfObject PatternObj, final byte[] streamData) {

        float[][] mm;
        AffineTransform affine = new AffineTransform();
        AffineTransform rotatedAffine = new AffineTransform();
        float[] inputs = PatternObj.getFloatArray(PdfDictionary.Matrix);
        if (inputs != null) {
            mm = new float[][]{{inputs[0], inputs[1], 0f}, {inputs[2], inputs[3], 0f}, {inputs[4], inputs[5], 1f}};
            affine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);
        } else {
            mm = new float[][]{{1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f}};
        }
    
        final ObjectStore localStore = new ObjectStore();
        BufferedImage image;
        DynamicVectorRenderer glyphDisplay;
        
        boolean isRotated = affine.getShearX()!=0 || affine.getShearY()!=0;
        
        if(isRotated){
            rotatedAffine = affine;
            affine = new AffineTransform();
            mm = new float[][]{{1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f}};
        }
        
        //System.out.println("mm="+mm[0][0]+" "+mm[0][1]+" "+mm[1][0]+" "+mm[1][1]+" "+mm[2][0]+" "+mm[2][1]+" "+isRotated);
        
        final float[] rawBBox = PatternObj.getFloatArray(PdfDictionary.BBox);

        final float xGap = Math.abs(rawBBox[2] - rawBBox[0]);
        final float yGap = Math.abs(rawBBox[1] - rawBBox[3]);

        GeneralPath rawPath = new GeneralPath();
        rawPath.moveTo(rawBBox[0], rawBBox[1]);
        rawPath.lineTo(rawBBox[2], rawBBox[1]);
        rawPath.lineTo(rawBBox[2], rawBBox[3]);
        rawPath.lineTo(rawBBox[0], rawBBox[3]);
        rawPath.lineTo(rawBBox[0], rawBBox[1]);
        rawPath.closePath();
        Shape rawShape = rawPath.createTransformedShape(affine);
        Rectangle2D rawRect = rawShape.getBounds2D();

        float rawXStep = PatternObj.getFloatNumber(PdfDictionary.XStep);
        rawXStep = (30000 > Short.MAX_VALUE || rawXStep < -30000) ? 0f : rawXStep;
        float rawYStep = PatternObj.getFloatNumber(PdfDictionary.YStep);
        rawYStep = (30000 > Short.MAX_VALUE || rawYStep < -30000) ? 0f : rawYStep;

        float[] bbox = new float[4];

        if (rawXStep < 0) {
            bbox[2] = xGap - rawXStep;
        } else {
            bbox[2] = rawXStep;
        }
        if (rawYStep < 0) {
            bbox[3] = yGap - rawYStep;
        } else {
            bbox[3] = rawYStep;
        }

        GeneralPath boxPath = new GeneralPath();
        boxPath.moveTo(bbox[0], bbox[1]);
        boxPath.lineTo(bbox[2], bbox[1]);
        boxPath.lineTo(bbox[2], bbox[3]);
        boxPath.lineTo(bbox[0], bbox[3]);
        boxPath.lineTo(bbox[0], bbox[1]);
        boxPath.closePath();
        Shape boxShape = boxPath.createTransformedShape(affine);
        Rectangle2D boxRect = boxShape.getBounds2D();

        double imageW = (Math.abs(boxRect.getX()) + boxRect.getWidth()) - (Math.abs(rawRect.getX()));
        double imageH = (Math.abs(boxRect.getY()) + boxRect.getHeight()) - (Math.abs(rawRect.getY()));

        imageW = rawXStep == 0 ? rawRect.getWidth() : imageW;
        imageH = rawYStep == 0 ? rawRect.getWidth() : imageH;

        imageW = imageW > 3000 ? 3000 : imageW;
        imageH = imageH > 3000 ? 3000 : imageH;
        
        int iw = (int) (imageW);
        iw = iw < 1 ? 1 : iw;
        int ih = (int) (imageH);
        ih = ih < 1 ? 1 : ih;

        //hack fix to odd_pattern.pdf file
        if (imageH < 1 && imageW < 2.5) {
            iw = 1;
        }
        
        Rectangle2D fRect = new Rectangle2D.Double(rawRect.getX(), rawRect.getY(), imageW, imageH);
        image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);

        if (isRotated) {
            
            glyphDisplay = decodePatternContent(PatternObj, null, streamData, localStore);
            BufferedImage sing = glyphDisplay.getSingleImagePattern();
            
            if(sing!=null){
                sing = RenderUtils.invertImage(sing);
                return new ShearedTexturePaint(sing, fRect, rotatedAffine);
            }else{
                mm[2][0] = (float) (mm[2][0] - rawRect.getX());
                mm[2][1] = (float) (mm[2][1] - rawRect.getY());
                glyphDisplay = decodePatternContent(PatternObj, mm, streamData, localStore);
                Graphics2D g2 = image.createGraphics();
                glyphDisplay.setG2(g2);
                glyphDisplay.paint(null, null, null);
                return new ShearedTexturePaint(image, fRect, rotatedAffine);
            }
            
        } else {
            
            mm[2][0] = (float) (mm[2][0] - rawRect.getX());
            mm[2][1] = (float) (mm[2][1] - rawRect.getY());
            glyphDisplay = decodePatternContent(PatternObj, mm, streamData, localStore);
            Graphics2D g2 = image.createGraphics();
            glyphDisplay.setG2(g2);
            glyphDisplay.paint(null, null, null);
            //System.out.println("texture "+fRect+" ");
            return new ShearedTexturePaint(image, fRect, rotatedAffine);
        }
                 
    }

   
    
    
    private DynamicVectorRenderer decodePatternContent(final PdfObject PatternObj, final float[][] matrix, final byte[] streamData, final ObjectStore localStore) {
        
        final PdfObject Resources=PatternObj.getDictionary(PdfDictionary.Resources);
        
        //decode and create graphic of glyph
        
        final PdfStreamDecoderForPattern glyphDecoder=new PdfStreamDecoderForPattern(currentPdfFile);
        glyphDecoder.setParameters(false,true,7,0,false,false);
        
        glyphDecoder.setObjectValue(ValueTypes.ObjectStore,localStore);
        
        //glyphDecoder.setMultiplier(multiplyer);
        
        //T3Renderer glyphDisplay=new T3Display(0,false,20,localStore);
        final T3Renderer glyphDisplay=new PatternDisplay(0,false,20,localStore);
        glyphDisplay.setHiResImageForDisplayMode(true);
        try{
            glyphDecoder.setRenderer(glyphDisplay);
            
            /**read the resources for the page*/
            if (Resources != null){
                glyphDecoder.readResources(Resources,true);
            }
            
            /**
             * setup matrix so scales correctly
             **/
            final GraphicsState currentGraphicsState=new GraphicsState(0,0);
            glyphDecoder.setGS(currentGraphicsState);
            //multiply to get new CTM
            if(matrix!=null) {
                currentGraphicsState.CTM =matrix;
            }
            
            /**
             * add in a colour (may well need further development)
             */
            if(strokCol==null){
                glyphDecoder.setDefaultColors(gs.getStrokeColor(),gs.getNonstrokeColor());
            }else{
                glyphDecoder.setDefaultColors(strokCol,new PdfColor(0,255,0));
            }
            
            glyphDecoder.decodePageContent(currentGraphicsState, streamData);
            
        } catch (final PdfException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: "+e.getMessage());
            }
            //
        }
        
        
        //flush as image now created
        return glyphDisplay;
    }
    
    /**
     */
    private PdfPaint setupShading(final PdfObject PatternObj, final float[][] matrix) {
        
        /**
         * get the shading object
         */
        
        final PdfObject Shading=PatternObj.getDictionary(PdfDictionary.Shading);
        
        /**
         * work out colorspace
         */
        final PdfObject ColorSpace=Shading.getDictionary(PdfDictionary.ColorSpace);
        
        //convert colorspace and get details
        GenericColorSpace newColorSpace=ColorspaceFactory.getColorSpaceInstance(currentPdfFile, ColorSpace);
        
        //use alternate as preference if CMYK
        if(newColorSpace.getID()==ColorSpaces.ICC && ColorSpace.getParameterConstant(PdfDictionary.Alternate)==ColorSpaces.DeviceCMYK) {
            newColorSpace=new DeviceCMYKColorSpace();
        }
        
        return new ShadedPaint(Shading, isPrinting,newColorSpace, currentPdfFile,matrix,colorsReversed, CTM, false);
        
    }
    }
