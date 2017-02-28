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
 * MaskUtils.java
 * ---------------
 */
package org.jpedal.parser.image;

import org.jpedal.exception.PdfException;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.parser.ValueTypes;
import org.jpedal.render.*;
import org.jpedal.utils.LogWriter;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MaskUtils {


    /**
     *
     * @param XObject
     * @param name
     * @param newSMask
     * @param gs
     * @param current
     * @param currentPdfFile
     * @param parserOptions
     * @param formLevel
     * @param multiplyer
     * @param useTransparancy
     * @throws PdfException
     */
    public static void createMaskForm(final PdfObject XObject, final String name, final PdfObject newSMask, final GraphicsState gs,
                                      final DynamicVectorRenderer current, final PdfObjectReader currentPdfFile,
                                      final ParserOptions parserOptions, final int formLevel, final float multiplyer, final boolean useTransparancy) {

        final float[] BBox;//size
        BBox= XObject.getFloatArray(PdfDictionary.BBox);

        /**get form as an image*/
        final int fx=(int)BBox[0];
        final int fy=(int)BBox[1];
        final int fw=(int)BBox[2];
        final int fh=(int)(BBox[3]);
//        int bmValue = PdfDictionary.Normal;
//        float transparency = 1f;

        final int iw;
        final int ih;

        //get the form
        BufferedImage image;
//        PDFObjectToImageNonStatic pdfObjectToImage = new PDFObjectToImageNonStatic();

        if(newSMask!=null){
            image = PDFObjectToImage.getImageFromPdfObject(XObject, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer,false,4f);

            // @blendissues
//            image = pdfObjectToImage.getImageFromPdfObject(XObject, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer,false,1f);
//            bmValue = pdfObjectToImage.getBlendMode();
//            transparency = pdfObjectToImage.getTransparency();
            final BufferedImage smaskImage = PDFObjectToImage.getImageFromPdfObject(newSMask, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer,false,4f);

            //apply SMask to image
            image= SMask.applySmask(image, smaskImage,true);

            if(smaskImage!=null){
                smaskImage.flush();
            }

            iw=image.getWidth()/4;
            ih=image.getHeight()/4;

        }else{

            image = PDFObjectToImage.getImageFromPdfObject(XObject, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer,useTransparancy,4f);
            //image = PDFObjectToImage.getImageFromPdfObject(XObject, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer,true,4f);

            //image = pdfObjectToImage.getImageFromPdfObject(XObject, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer,true,4f);

            //hard-coded upscale to give better image quality
            iw=image.getWidth()/4;
            ih=image.getHeight()/4;
        }

        final GraphicsState gs1; //add in gs

        boolean isChanged=false;
        if(newSMask==null && gs.getAlphaMax(GraphicsState.FILL)<1f){

            isChanged=true;

            gs1 =new GraphicsState(); //add in gs
            gs1.setMaxAlpha(GraphicsState.FILL, gs.getAlphaMax(GraphicsState.FILL));
            gs1.setMaxAlpha(GraphicsState.STROKE, gs.getAlphaMax(GraphicsState.STROKE));

            current.setGraphicsState(GraphicsState.STROKE, gs.getAlpha(GraphicsState.STROKE), PdfDictionary.Normal);
            current.setGraphicsState(GraphicsState.FILL, gs.getAlpha(GraphicsState.FILL), PdfDictionary.Normal);

        }else {
            if(formLevel==1){
                gs1 = new GraphicsState(gs); //add in gs
            }else{
                gs1 = new GraphicsState(); //add in gs
            }
        }

        gs1.CTM=new float[][]{{iw,0,1},{0,ih,1},{0,0,0}};

        //different formula needed if flattening forms
        if(parserOptions.isFlattenedForm()){
            gs1.x= parserOptions.getflattenX();
            gs1.y= parserOptions.getflattenY();
        }else{

            if(fx<fw) {
                gs1.x = fx;
            } else {
                gs1.x = fx - iw;
            }

            if(fy<fh) {
                gs1.y = fy;
            } else {
                gs1.y = fy - ih;
            }
        }

//        float tempX = gs1.x;
//        float tempY = gs1.y;

//        if(gs.CTM[2][0]<0) {
//           gs1.x = tempX*gs.CTM[0][0] + tempY*gs.CTM[1][0] + gs.CTM[2][0];
//        }

//        if(gs.CTM[2][1]<0) {
//            gs1.y = tempY*gs.CTM[1][1] + tempY*gs.CTM[0][1] + gs.CTM[2][1];
//        }

        //see case 20638 for this quick fix we can use affine transform in future
        gs1.x += gs.CTM[2][0];
        gs1.y += gs.CTM[2][1];

        //draw as image
        gs1.CTM[2][0]= gs1.x;
        gs1.CTM[2][1]= gs1.y;

//        // @blendissues
//        if(PdfDictionary.Normal != bmValue){
//            current.setGraphicsState(GraphicsState.FILL, transparency, bmValue);
//            current.setGraphicsState(GraphicsState.STROKE, transparency, bmValue);
//            System.out.println(PdfDictionary.showAsConstant(bmValue));
//        }

        gs1.CTM[1][1]=-gs1.CTM[1][1];
        gs1.CTM[2][1] -= gs1.CTM[1][1];
            
        //separate call needed to paint image on thumbnail or background image in HTML/SVG
        if(current.isHTMLorSVG()){
            
            current.drawImage(parserOptions.getPageNumber(),image,gs1,false,name, -3);

            current.drawImage(parserOptions.getPageNumber(),image, gs1,false, name, -2);

        }else{
            
            gs1.y = gs1.CTM[2][1];
            
            current.drawImage(parserOptions.getPageNumber(),image, gs1,false, name, -1);
        }

        if(isChanged){
            current.setGraphicsState(GraphicsState.STROKE, gs.getAlpha(GraphicsState.STROKE),gs.getBMValue());
            current.setGraphicsState(GraphicsState.FILL, gs.getAlpha(GraphicsState.FILL), gs.getBMValue());

        }
    }

    public static BufferedImage createTransparentForm(final PdfObject XObject, final int fx, final int fy, final int fw, final int fh,
                                                      final PdfObjectReader currentPdfFile, final ParserOptions parserOptions, final int formLevel, final float multiplyer) {

        final BufferedImage image;
        final byte[] objectData1 = currentPdfFile.readStream(XObject,true,true,false, false,false, XObject.getCacheName(currentPdfFile.getObjectReader()));

        final ObjectStore localStore = new ObjectStore();
        final DynamicVectorRenderer glyphDisplay=new SwingDisplay(0,false,20,localStore);
        glyphDisplay.setHiResImageForDisplayMode(true);

        final PdfStreamDecoder glyphDecoder=new PdfStreamDecoder(currentPdfFile, true,null); //switch to hires as well
        glyphDecoder.setParameters(true, true, 3, 65,false, parserOptions.useJavaFX());

        glyphDecoder.setObjectValue(ValueTypes.ObjectStore, localStore);
        // glyphDecoder.setObjectValue(Options.ErrorTracker, errorTracker);
        glyphDecoder.setFormLevel(formLevel);
        glyphDecoder.setMultiplyer(multiplyer);
//        glyphDecoder.setFloatValue(SamplingUsed, samplingUsed);

        glyphDecoder.setRenderer(glyphDisplay);

        /**read any resources*/
        try{

            final PdfObject SMaskResources = XObject.getDictionary(PdfDictionary.Resources);
            if (SMaskResources != null) {
                glyphDecoder.readResources(SMaskResources, false);
            }

        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        /**decode the stream*/
        if(objectData1 !=null) {
            glyphDecoder.decodeStreamIntoObjects(objectData1, false);
        }

        int hh= fh;
        //float diff=fy;
        if(fy > fh){
            hh= fy - fh;
         //   diff=fh;
        }

        //as we make it image, make it bigger to retain quality
        final int scaling=4;

        //get bit underneath and merge in
        image=new BufferedImage(scaling*fw,scaling*hh, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D formG2=image.createGraphics();

        formG2.setColor(Color.WHITE);
        formG2.fillRect(0, 0, scaling*fw, scaling*hh);
        formG2.translate(0, scaling*hh);
        formG2.scale(1, -1);
        formG2.scale(scaling, scaling);

        //current.paint(formG2,null,null,null,false,true);

        glyphDisplay.setG2(formG2);
        glyphDisplay.paint(null,null,null);

        localStore.flush();

        return image;
    }

}
