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
 * PDFtoImageConvertor.java
 * ---------------
 */
package org.jpedal.parser;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.GUIModes;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.PdfResources;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.PageObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.swing.PDFtoImageConvertorSwing;
import org.jpedal.render.DynamicVectorRenderer;

/**
 *
 */
public abstract class PDFtoImageConvertor {
    
    //non-static version
    private final Integer instance_bestQualityMaxScaling;

    final Boolean instance_allowPagesSmallerThanPageSize;

    final DecoderOptions options;

    /**custom upscale val for JPedal settings*/
    public float multiplyer = 1;

    //force to generate images smaller than page size
    public static Boolean allowPagesSmallerThanPageSize=Boolean.FALSE;

    //stop scaling to silly figures
    public static Integer bestQualityMaxScaling;
    
    public DynamicVectorRenderer imageDisplay;
    
    public AffineTransform imageScaling;
    
    public int mediaH,w,h;
    public int rotation;
    public float crw,crh,crx,cry;
    public boolean rotated;
    
    // Temporarily lock FX out of form rendering (breaks thumbnail generation)
    protected boolean isFX;

     public PDFtoImageConvertor(final float multiplyer, final DecoderOptions options) {
        this.multiplyer=multiplyer;
        this.instance_allowPagesSmallerThanPageSize=options.getInstance_allowPagesSmallerThanPageSize();
        this.instance_bestQualityMaxScaling=options.getInstance_bestQualityMaxScaling();
        this.options = options;      
    }

    
    public BufferedImage convert(final DecoderResults resultsFromDecode, final int displayRotation, final PdfResources res,
                                 final ExternalHandlers externalHandlers, final int renderMode, final PdfPageData pageData,
                                 final AcroRenderer formRenderer, final float scaling, final PdfObjectReader currentPdfFile, final int pageIndex,
                                 final boolean imageIsTransparent,
                                 final String currentPageOffset) throws PdfException {

        final ObjectStore localStore = new ObjectStore();

        /** read page or next pages */
        final PdfObject pdfObject=new PageObject(currentPageOffset);
        currentPdfFile.readObject(pdfObject);

        currentPdfFile.checkParentForResources(pdfObject);

        //ensure set (needed for XFA)
        pdfObject.setPageNumber(pageIndex);
         
        final PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

        imageDisplay=getDisplay(pageIndex, localStore);
        
        if(!imageDisplay.isHTMLorSVG()){
        	
        	if(options.getPageColor()!=null) {
                imageDisplay.setValue(DynamicVectorRenderer.ALT_BACKGROUND_COLOR, options.getPageColor().getRGB());
            }
        	
            if(options.getTextColor()!=null){
            	imageDisplay.setValue(DynamicVectorRenderer.ALT_FOREGROUND_COLOR, options.getTextColor().getRGB());
                
                if(options.getChangeTextAndLine()) {
                	imageDisplay.setValue(DynamicVectorRenderer.FOREGROUND_INCLUDE_LINEART, 1);
                } else {
                	imageDisplay.setValue(DynamicVectorRenderer.FOREGROUND_INCLUDE_LINEART, 0);
                }
                
                imageDisplay.setValue(DynamicVectorRenderer.COLOR_REPLACEMENT_THRESHOLD, options.getReplacementColorThreshold());
            }
        }
        
        final PdfStreamDecoder currentImageDecoder = formRenderer.getStreamDecoder(currentPdfFile,true,null,true);
        
        currentImageDecoder.setParameters(true, true, renderMode, PdfDecoderInt.TEXT,false,externalHandlers.getMode().equals(GUIModes.JAVAFX));

        externalHandlers.addHandlers(currentImageDecoder);

        //currentImageDecoder.setObjectValue(ValueTypes.Name, filename);
        currentImageDecoder.setObjectValue(ValueTypes.ObjectStore,localStore);
        currentImageDecoder.setMultiplyer(multiplyer);
        currentImageDecoder.setObjectValue(ValueTypes.PDFPageData,pageData);
        currentImageDecoder.setIntValue(ValueTypes.PageNum, pageIndex);

        currentImageDecoder.setRenderer(imageDisplay);
        externalHandlers.addHandlers(currentImageDecoder);

        res.setupResources(currentImageDecoder, true, Resources,pageIndex,currentPdfFile);

        //can for max
        if(multiplyer==-2){

            multiplyer=-1;
            currentImageDecoder.setMultiplyer(multiplyer);

            final PdfStreamDecoderForSampling currentImageDecoder2 = new PdfStreamDecoderForSampling(currentPdfFile);
            currentImageDecoder2.setParameters(true, true, renderMode,0,false,externalHandlers.getMode().equals(GUIModes.JAVAFX));

            //currentImageDecoder2.setObjectValue(ValueTypes.Name, filename);
            currentImageDecoder2.setObjectValue(ValueTypes.ObjectStore,localStore);
            currentImageDecoder2.setMultiplyer(multiplyer);
            currentImageDecoder2.setObjectValue(ValueTypes.PDFPageData,pageData);
            currentImageDecoder2.setIntValue(ValueTypes.PageNum, pageIndex);
            currentImageDecoder2.setRenderer(imageDisplay);

            res.setupResources(currentImageDecoder2, true, Resources,pageIndex,currentPdfFile);

            externalHandlers.addHandlers(currentImageDecoder2);

            /** bare minimum to get value*/
            multiplyer=currentImageDecoder2.decodePageContentForImageSampling(pdfObject);

            int bestQualityMaxScalingToUse = 0;
            if(instance_bestQualityMaxScaling != null) {
                bestQualityMaxScalingToUse = instance_bestQualityMaxScaling;
            } else if (bestQualityMaxScaling != null) {
                bestQualityMaxScalingToUse = bestQualityMaxScaling;
            }

            if (bestQualityMaxScalingToUse > 0 && multiplyer > bestQualityMaxScalingToUse) {
                multiplyer = bestQualityMaxScalingToUse;
            }

            currentImageDecoder2.setMultiplyer(multiplyer);
            currentImageDecoder.setMultiplyer(multiplyer);
        }

        if(!allowPagesSmallerThanPageSize &&
                !instance_allowPagesSmallerThanPageSize &&
                multiplyer<1 && multiplyer>0) {
            multiplyer = 1;
        }

        //allow for value not set
        if(multiplyer==-1) {
            multiplyer = 1;
        }

        /**
         * setup positions,transformations and image
         */
        imageScaling = PDFtoImageConvertorSwing.setPageParametersForImage(scaling*multiplyer, pageIndex,pageData);

        setParams(scaling, pageData, pageIndex);        
        
        final BufferedImage image=pageToImage(imageIsTransparent,currentImageDecoder,scaling,pdfObject,formRenderer);

        resultsFromDecode.update(currentImageDecoder, false);
        
        /**
         * draw acroform data onto Panel
         */
        if (formRenderer != null && formRenderer.hasFormsOnPage(pageIndex) && !formRenderer.ignoreForms()) {
            
            resultsFromDecode.resetColorSpaces();
            
            if(!formRenderer.getCompData().hasformsOnPageDecoded(pageIndex)){
                formRenderer.createDisplayComponentsForPage(pageIndex,currentImageDecoder);
            }
           
            if(isFX){
                //done in fx image code
            }else if (!formRenderer.getCompData().formsRasterizedForDisplay()) {

                if (!formRenderer.useXFA()) {

                    java.util.List[] formsOrdered = formRenderer.getCompData().getFormList(true);

                    //get unsorted components and iterate over forms
                    for (Object nextVal : formsOrdered[pageIndex]) {

                        if (nextVal != null) {
                            formRenderer.getFormFlattener().drawFlattenedForm(currentImageDecoder, (org.jpedal.objects.raw.FormObject) nextVal, false, (PdfObject) formRenderer.getFormResources()[0]);
                        }
                    }
                    
                } else {
                    formRenderer.getCompData().renderFormsOntoG2(image.getGraphics(), pageIndex, 0, displayRotation, null, null, pageData.getMediaBoxHeight(pageIndex));
                }
            } else {
                
                final java.util.List[] formsOrdered=formRenderer.getCompData().getFormList(true);
                
                //get unsorted components and iterate over forms
                for (final Object nextVal : formsOrdered[pageIndex]) {
                    
                    if (nextVal !=null) {
                       
                        formRenderer.getFormFlattener().drawFlattenedForm(currentImageDecoder, (org.jpedal.objects.raw.FormObject) nextVal, false, (PdfObject) formRenderer.getFormResources()[0]);
                        
                    }
                }
            }
        }

        if(currentImageDecoder!=null){
            currentImageDecoder.dispose();
        }

        localStore.flush();
        return image;
    }

    

    public float getMultiplyer() {
        
        return multiplyer;
        
    }

    public DynamicVectorRenderer getDisplay(final int pageIndex, final ObjectStore localStore) {
        throw new UnsupportedOperationException(this+" Code should never be called ");
    }
    
    public BufferedImage pageToImage(final boolean imageIsTransparent, final PdfStreamDecoder currentImageDecoder, final float scaling, 
            final PdfObject pdfObject,final AcroRenderer formRenderer) throws PdfException {
        throw new UnsupportedOperationException(this+" Code should never be called ");
    }
    
    public void setParams(final float scaling, final PdfPageData pageData, final int pageIndex) {
        
        //include scaling in size
        mediaH = (int) (scaling*pageData.getMediaBoxHeight(pageIndex));
        rotation = pageData.getRotation(pageIndex);

        crw = (scaling*pageData.getCropBoxWidth2D(pageIndex));
        crh = (scaling*pageData.getCropBoxHeight2D(pageIndex));
        crx = (scaling*pageData.getCropBoxX(pageIndex));
        cry = (scaling*pageData.getCropBoxY(pageIndex));
        
        if ((rotation == 90) || (rotation == 270)) {
            h = (int) (crw*multiplyer); // * scaling);
            w = (int) (crh*multiplyer); // * scaling);
            rotated = true;
        } else {
            w = (int) (crw*multiplyer); // * scaling);
            h = (int) (crh*multiplyer); // * scaling);
        }   
    }
}


