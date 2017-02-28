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
 * XFormDecoder.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.PdfDecoderInt;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.ImageCommands;
import org.jpedal.parser.image.MaskUtils;
import org.jpedal.parser.image.XForm;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

public class XFormDecoder {
    /**
     * recursive subroutine so in actual body of PdfStreamDecoder so it can recall decodeStream
     * @param pdfStreamDecoder
     * @param dataPointer
     */
    static void processXForm(final PdfStreamDecoder pdfStreamDecoder, final int dataPointer, final PdfObject XObject, final Shape defaultClip, final CommandParser parser) {

        final boolean debug=false;

        if(debug) {
            System.out.println("processImage " + dataPointer + ' ' + XObject.getObjectRefAsString() + ' ' + defaultClip);
        }

        final String oldFormName= pdfStreamDecoder.formName;

        final String name=parser.generateOpAsString(0, true);

        //Removed to fix issue with render to g2
        //name is not unique if in form so we add form level to separate out
        //if(formLevel>1)
        //    name= formName+'_'+ formLevel+'_'+name;

        //string to hold image details

        try {

            if(ImageCommands.trackImages){
                //add details to string so we can pass back
                if(pdfStreamDecoder.imagesInFile ==null) {
                    pdfStreamDecoder.imagesInFile = name + " Form";
                } else {
                    pdfStreamDecoder.imagesInFile = name + " Form\n" + pdfStreamDecoder.imagesInFile;
                }
            }

            //reset operand
            parser.reset();

            //read stream for image
            final byte[] objectData = pdfStreamDecoder.currentPdfFile.readStream(XObject, true, true, false, false, false, XObject.getCacheName(pdfStreamDecoder.currentPdfFile.getObjectReader()));
            if (objectData != null) {

                final String oldIndent= PdfStreamDecoder.indent;
                PdfStreamDecoder.indent += "   ";

                //set value and see if Transform matrix
                float[] transformMatrix=new float[6];
                float[] matrix=XObject.getFloatArray(PdfDictionary.Matrix);

                /**
                 * see if we should ignore scaling because we have already scaled in
                 * see 14jan/test_de_signature.pdf
                 */
                final float[] formBBox = XObject.getFloatArray(PdfDictionary.BBox);

                if (matrix != null && pdfStreamDecoder.BBox != null && formBBox != null) {

                    final float Bwidth;
                    final float Bheight;

                    if (pdfStreamDecoder.parserOptions.isFlattenedForm()) {
                        Bwidth = (pdfStreamDecoder.BBox[2] - pdfStreamDecoder.BBox[0]);
                        Bheight = (pdfStreamDecoder.BBox[3] - pdfStreamDecoder.BBox[1]);
                    } else {
                        Bwidth = (pdfStreamDecoder.BBox[2] - pdfStreamDecoder.BBox[0]) / matrix[0];
                        Bheight = (pdfStreamDecoder.BBox[3] - pdfStreamDecoder.BBox[1]) / matrix[3];
                    }

                    if (Bwidth == (formBBox[2] - formBBox[0]) && Bheight == (formBBox[3] - formBBox[1]) && matrix[4]==0 && matrix[5]==0) {
                        matrix = null;
                    }

                }

                final boolean isIdentity=matrix==null || XForm.isIdentity(matrix);
                if(matrix!=null) {
                    transformMatrix = matrix;
                }

                final float[][] CTM;
                final float[][] oldCTM;

                final int currentDepth= pdfStreamDecoder.graphicsStates.getDepth();

                //allow for stroke line width being altered by scaling
                float lineWidthInForm=-1; //negative values not used


                //save current
                final float[][] currentCTM=new float[3][3];
                for(int i=0;i<3;i++) {
                    System.arraycopy(pdfStreamDecoder.gs.CTM[i], 0, currentCTM[i], 0, 3);
                }

                oldCTM = currentCTM;

                CTM= pdfStreamDecoder.gs.CTM;

                float[][] scaleF= pdfStreamDecoder.gs.scaleFactor;

                if(matrix!=null && !isIdentity) {

                    final float[][] scaleFactor={{transformMatrix[0],transformMatrix[1],0},
                        {transformMatrix[2],transformMatrix[3],0},
                        {transformMatrix[4],transformMatrix[5],1}};

                    scaleF=scaleFactor;
                    pdfStreamDecoder.gs.CTM= Matrix.multiply(scaleFactor, CTM);

                    //work out line width
                    lineWidthInForm=transformMatrix[0]* pdfStreamDecoder.gs.getLineWidth();

                    if (lineWidthInForm == 0) {
                        lineWidthInForm = transformMatrix[1] * pdfStreamDecoder.gs.getLineWidth();
                    }

                    if(lineWidthInForm<0) {
                        lineWidthInForm = -lineWidthInForm;
                    }

                    if(debug) {
                        System.out.println("setMatrix " + pdfStreamDecoder.gs.CTM[0][0] + ' ' + pdfStreamDecoder.gs.CTM[0][1] + ' ' + pdfStreamDecoder.gs.CTM[1][0] + ' ' + pdfStreamDecoder.gs.CTM[1][1] + ' ' + pdfStreamDecoder.gs.CTM[2][0] + ' ' + pdfStreamDecoder.gs.CTM[2][1]);
                    }
                }

                //track depth
                pdfStreamDecoder.formLevel++;

                //track name so we can make unique key for image name
                if(pdfStreamDecoder.formLevel ==1) {
                    pdfStreamDecoder.formName = name;
                } else if(pdfStreamDecoder.formLevel <20) //stop memory issue on silly files
                {
                    pdfStreamDecoder.formName = pdfStreamDecoder.formName + '_' + name;
                }

                //preserve colorspaces
                final GenericColorSpace mainStrokeColorData=(GenericColorSpace) pdfStreamDecoder.gs.strokeColorSpace.clone();
                final GenericColorSpace mainnonStrokeColorData=(GenericColorSpace) pdfStreamDecoder.gs.nonstrokeColorSpace.clone();

                //set form line width if appropriate
                if(lineWidthInForm>0) {
                    pdfStreamDecoder.gs.setLineWidth(lineWidthInForm);
                }

                //set gs max to current so child gs values can not exceed
                final float maxStrokeValue= pdfStreamDecoder.gs.getAlphaMax(GraphicsState.STROKE);
                final float maxFillValue= pdfStreamDecoder.gs.getAlphaMax(GraphicsState.FILL);
                final float currentFillValue= pdfStreamDecoder.gs.getAlpha(GraphicsState.FILL);
                pdfStreamDecoder.gs.setMaxAlpha(GraphicsState.STROKE, pdfStreamDecoder.gs.getAlpha(GraphicsState.STROKE));

                //if(pdfStreamDecoder.formLevel ==1) {
                if(pdfStreamDecoder.formLevel<3 && currentFillValue<maxFillValue){
                    pdfStreamDecoder.gs.setMaxAlpha(GraphicsState.FILL, currentFillValue);
                }

                //make a copy s owe can restore to original state
                //we need to pass in and then undo any changes at end
                final PdfObjectCache mainCache = pdfStreamDecoder.cache.copy();   //setup cache
                pdfStreamDecoder.cache.reset(mainCache);   //copy in data

                /**read any resources*/
                final PdfObject Resources= XObject.getDictionary(PdfDictionary.Resources);
                pdfStreamDecoder.readResources(Resources, false);

                /**read any resources*/
                pdfStreamDecoder.cache.groupObj= XObject.getDictionary(PdfDictionary.Group);
                pdfStreamDecoder.currentPdfFile.checkResolved(pdfStreamDecoder.cache.groupObj);

                /**
                 * see if bounding box and set
                 */
                float[] BBox= XObject.getFloatArray(PdfDictionary.BBox);
                Area clip=null;
                boolean clipChanged=false;
                

                //this code breaks 11jun/169351.pdf so added as possible fix
                if(BBox!=null && BBox[2]>1 && BBox[3]>1 && pdfStreamDecoder.gs.getClippingShape()==null && pdfStreamDecoder.gs.CTM[0][1]==0 && pdfStreamDecoder.gs.CTM[1][0]==0 && pdfStreamDecoder.gs.CTM[2][1]!=0 && pdfStreamDecoder.gs.CTM[2][0]<0){
                    if(debug) {
                        System.out.println("setClip1 ");
                    }

                    clip = XForm.setClip(defaultClip, BBox, pdfStreamDecoder.gs, pdfStreamDecoder.current);
                    clipChanged=true;


                    //System.out.println(BBox[0]+" "+BBox[1]+" "+BBox[2]+" "+BBox[3]);
                    //Matrix.show(gs.CTM);
                }
                else if(BBox!=null && BBox[0]==0 && BBox[1]==0 && BBox[2]>1 && BBox[3]>1 && BBox[2]!=BBox[3]   && (pdfStreamDecoder.gs.CTM[0][0]>0.99 || pdfStreamDecoder.gs.CTM[2][1]<-1) && (pdfStreamDecoder.gs.CTM[2][0]<-1 || pdfStreamDecoder.gs.CTM[2][0]>1) && pdfStreamDecoder.gs.CTM[2][1]!=0 ){//)  && BBox[2]>1 && BBox[3]>1 ){//if(BBox!=null && matrix==null && BBox[0]==0 && BBox[1]==0){

                    if(debug) {
                        System.out.println("setClip2");
                    }

                    clip = XForm.setClip(defaultClip, BBox, pdfStreamDecoder.gs, pdfStreamDecoder.current);
                    clipChanged=true;
                }

                //attempt to fix odd customers3/slides1.pdf text off page issue
                //no obvious reason to ignore text on form other than negative y value
                //adjusted to fix 11jun/Request_For_Quotation.pdf
                //if(formLevel==1 && gs.CTM[0][0]!=0 && gs.CTM[0][0]!=gs.CTM[0][1] && gs.CTM[0][0]!=1 && currentTextState.Tm[0][0]==1 && gs.CTM[1][1]<1f && (gs.CTM[1][1]>0.92f || gs.CTM[0][1]!=0) && currentTextState.Tm[2][1]<0){
                //adjusted again to fix 15jan/21130.pdf
                //else if(BBox!=null && BBox[0]==0 && BBox[1]==0 && BBox[2]>1 && BBox[3]>1 && (pdfStreamDecoder.formLevel>0 || pdfStreamDecoder.gs.getClippingShape()!=null)){
                
                else if(BBox!=null && BBox[0]==0 && BBox[1]==0 && BBox[2]>1 && BBox[3]>1 && !(pdfStreamDecoder.gs.CTM[2][0]<0) && (pdfStreamDecoder.formLevel>0 || pdfStreamDecoder.gs.getClippingShape()!=null)){
                    //&& (gs.CTM[0][0]>0.99 || gs.CTM[2][1]<-1) && (gs.CTM[2][0]<-1 || gs.CTM[2][0]>1) && gs.CTM[2][1]!=0 ){

                    if(debug) {
                        System.out.println("setClip3");
                    }

                    clip = XForm.setClip(defaultClip, BBox, pdfStreamDecoder.gs, pdfStreamDecoder.current);
                    clipChanged=true;
                }else if(pdfStreamDecoder.formLevel >1 && BBox!=null && BBox[0]>50 && BBox[1]>50 && pdfStreamDecoder.gs.getClippingShape()!=null && (BBox[0]-1)> pdfStreamDecoder.gs.getClippingShape().getBounds().x &&
                        (BBox[1]-1)> pdfStreamDecoder.gs.getClippingShape().getBounds().y ){

                    // System.out.println("XX form="+formLevel);
                    // System.out.println(BBox[0]+" "+BBox[1]+" "+BBox[2]+" "+BBox[3]);
                    //  System.out.println(gs.getClippingShape().getBounds()+" "+defaultClip);
                    if(debug) {
                        System.out.println("setClip4");
                    }

                    clip = XForm.setClip(defaultClip, BBox, pdfStreamDecoder.gs, pdfStreamDecoder.current);
                    clipChanged=true;
                }else if(BBox!=null && BBox[2]>1 && BBox[3]>1 && pdfStreamDecoder.gs.getClippingShape()==null && pdfStreamDecoder.gs.CTM[0][1]>0 && pdfStreamDecoder.gs.CTM[1][0]<0 && pdfStreamDecoder.gs.CTM[0][0]==0 && pdfStreamDecoder.gs.CTM[1][1]==0){
                    if(debug) {
                        System.out.println("setClip5");
                    }

                    clip = XForm.setClip(defaultClip, BBox, pdfStreamDecoder.gs, pdfStreamDecoder.current);
                    clipChanged=true;
                    
                }else if(debug){
                    System.out.println("no Clip set");
                    
                }

                /**decode the stream*/
                if(objectData.length>0){

                    final PdfObject newSMask = XForm.getSMask(BBox, pdfStreamDecoder.gs, pdfStreamDecoder.currentPdfFile); //check for soft mask we need to apply

                    final int blendMode= pdfStreamDecoder.gs.getBMValue();

                    /**
                     * option to include Form as image if extracting images
                     */
                    if((pdfStreamDecoder.parserOptions.getExtractionMode() & PdfDecoderInt.RASTERIZE_FORMS)==PdfDecoderInt.RASTERIZE_FORMS){
                        processXFormAsImage(XObject, pdfStreamDecoder);
                        }

                    if(newSMask!=null || blendMode!=PdfDictionary.Normal){
                        processXFormWithMaskOrBlend(debug, newSMask, pdfStreamDecoder, blendMode, XObject, name);
                    }else{

                        if(debug) {
                            System.out.println("decode");
                        }

                        final int BM= pdfStreamDecoder.gs.getBMValue();

                        pdfStreamDecoder.decodeStreamIntoObjects(objectData, false);

                        pdfStreamDecoder.current.setGraphicsState(GraphicsState.STROKE, pdfStreamDecoder.gs.getAlpha(GraphicsState.STROKE), BM);
                        pdfStreamDecoder.current.setGraphicsState(GraphicsState.FILL, pdfStreamDecoder.gs.getAlpha(GraphicsState.FILL), BM);

                    }
                }

                //restore clip if changed
                if(clipChanged){
                    pdfStreamDecoder.gs.setClippingShape(clip);
                    pdfStreamDecoder.current.drawClip(pdfStreamDecoder.gs, clip, false) ;
                }

                //restore settings
                pdfStreamDecoder.formLevel--;

                //allow for stream not correctly setup
                pdfStreamDecoder.graphicsStates.correctDepth(currentDepth, pdfStreamDecoder.gs, pdfStreamDecoder.current);

                //
                //restore old matrix or set default
                //fixes 12dec/81564885_1355243032.pdf
                if(oldCTM!=null){
                    pdfStreamDecoder.gs.CTM=oldCTM;
                }else if(pdfStreamDecoder.gs.CTM[0][0]==1f && pdfStreamDecoder.gs.CTM[1][1]==1f){
                    pdfStreamDecoder.gs.CTM=new float[][]{{1,0,0},{0,1,0},{0,0,1}};
                }

                pdfStreamDecoder.gs.scaleFactor=scaleF;

                /**restore old colorspace and fonts*/
                pdfStreamDecoder.gs.strokeColorSpace=mainStrokeColorData;
                pdfStreamDecoder.gs.nonstrokeColorSpace=mainnonStrokeColorData;

                //put back original state
                pdfStreamDecoder.cache.restore(mainCache);

                //restore gs max to current so child gs values can not exceed
                pdfStreamDecoder.gs.setMaxAlpha(GraphicsState.STROKE, maxStrokeValue);
                pdfStreamDecoder.gs.setMaxAlpha(GraphicsState.FILL, maxFillValue);

                PdfStreamDecoder.indent=oldIndent;
            }

        } catch (final Error e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //

            pdfStreamDecoder.parserOptions.imagesProcessedFully=false;
            pdfStreamDecoder.errorTracker.addPageFailureMessage("Error " + e + " in DO");

            if (ExternalHandlers.throwMissingCIDError && e.getMessage()!=null && e.getMessage().contains("kochi")) {
                throw e;
            }
        } catch (final PdfException e) {

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e);
            }

            //<start-demo><end-demo>
            pdfStreamDecoder.parserOptions.imagesProcessedFully=false;
            pdfStreamDecoder.errorTracker.addPageFailureMessage("Error " + e + " in DO");
        }

        pdfStreamDecoder.formName =oldFormName;

    }

    static void processXFormWithMaskOrBlend(final boolean debug, final PdfObject newSMask, final PdfStreamDecoder pdfStreamDecoder, final int blendMode, final PdfObject XObject, final String name) {
        // || gs.getAlpha(GraphicsState.FILL)<1f){

        if(debug) {
            System.out.println("createMaskForm " + newSMask);
        }

        if(newSMask==null){ //needs to be normal for actual Mask
            pdfStreamDecoder.current.setGraphicsState(GraphicsState.STROKE, pdfStreamDecoder.gs.getAlpha(GraphicsState.STROKE), PdfDictionary.Normal);
            pdfStreamDecoder.current.setGraphicsState(GraphicsState.FILL, pdfStreamDecoder.gs.getAlpha(GraphicsState.FILL), PdfDictionary.Normal);
        }

        final boolean useTransparancy=newSMask!=null || blendMode!=PdfDictionary.Normal || pdfStreamDecoder.gs.getAlpha(GraphicsState.FILL)==1f;

        MaskUtils.createMaskForm(XObject, name, newSMask, pdfStreamDecoder.gs, pdfStreamDecoder.current, pdfStreamDecoder.currentPdfFile, pdfStreamDecoder.parserOptions, pdfStreamDecoder.formLevel, pdfStreamDecoder.multiplyer, useTransparancy);

        if(newSMask==null){
            pdfStreamDecoder.current.setGraphicsState(GraphicsState.STROKE, pdfStreamDecoder.gs.getAlpha(GraphicsState.STROKE), blendMode);
            pdfStreamDecoder.current.setGraphicsState(GraphicsState.FILL, pdfStreamDecoder.gs.getAlpha(GraphicsState.FILL), blendMode);
        }
    }

    static void processXFormAsImage(final PdfObject XObject, final PdfStreamDecoder pdfStreamDecoder) {
        float[] BBox;
        BBox= XObject.getFloatArray(PdfDictionary.BBox);
        /**get form as an image*/
        int fx=(int)BBox[0];
        final int fy=(int)BBox[1];
        final int fw=(int)BBox[2];
        final int fh=(int)(BBox[3]);
        //check x,y offsets and factor in
        if(fx<0) {
            fx = 0;
        }
        //get the form as an image
        final BufferedImage currentImage= MaskUtils.createTransparentForm(XObject, fx, fy, fw, fh, pdfStreamDecoder.currentPdfFile, pdfStreamDecoder.parserOptions, pdfStreamDecoder.formLevel, pdfStreamDecoder.multiplyer);
        final String imgName='R'+ pdfStreamDecoder.formName;
        //store  final image on disk & in memory
        pdfStreamDecoder.pdfImages.setImageInfo(imgName, pdfStreamDecoder.parserOptions.getPageNumber(), pdfStreamDecoder.gs.CTM[2][0], pdfStreamDecoder.gs.CTM[2][1], fw, fh);
        //save the image (R and normal so works with existing code)
        pdfStreamDecoder.objectStoreStreamRef.saveStoredImage('R' + imgName, currentImage, false, false, "jpg");
        pdfStreamDecoder.objectStoreStreamRef.saveStoredImage(imgName, currentImage, false, false, "jpg");
    }
}
