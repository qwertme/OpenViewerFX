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
 * Parser.java
 * ---------------
 */
package org.jpedal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jpedal.constants.SpecialOptions;
import org.jpedal.display.Display;
import org.jpedal.display.PageOffsets;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ColorHandler;
import org.jpedal.external.ErrorTracker;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.external.Options;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.tt.TTGlyph;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.io.*;
import org.jpedal.objects.*;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.PageObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
//
import org.jpedal.parser.*;

import org.jpedal.render.*;
import org.jpedal.text.TextLines;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Int;

import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

public class Parser {

    /**
     * return any messages on decoding
     */
    private String decodeStatus = "";

    StatusBar statusBar;

    /**
     * list of fonts for decoded page
     */
    private String fontsInFile = "";

    /**
     * list of images for decoded page
     */
    private String imagesInFile= "";

    /**custom upscale val for JPedal settings*/
    private float multiplyer = 1;

    /**
     * store image data extracted from pdf file
     */
    private PdfImageData pdfImages = new PdfImageData();

    /**
     * store image data extracted from pdf
     */
    private PdfImageData pdfBackgroundImages = new PdfImageData();

    /**
     * store text data and can be passed out to other classes
     */
    private PdfData pdfData;

    /**
     * store text data and can be passed out to other classes
     */
    private PdfData pdfBackgroundData;

    private int displayRotation;

    private float scaling=1.0f;

    /**
     * flag to stop multiple access to background decoding
     */
    private boolean isBackgroundDecoding;

    private final ExternalHandlers externalHandlers;

    /**
     * current extraction mode
     */
    private int extractionMode = 7;

    /**
     * current render mode
     */
    private int renderMode = 7;

    private DecoderOptions options=new DecoderOptions();

    private final FileAccess fileAcces;

    private PdfResources res=new PdfResources();

    private DecoderResults resultsFromDecode=new DecoderResults();

    /**holds lines of text we create*/
    private final TextLines textLines=new TextLines();

    private boolean generateGlyphOnRender;
    private int indent;

    private int specialMode;
    private final boolean useJavaFX;
    private boolean warnOnceOnForms;
    private PdfObject structTreeRootObj;
    //

    /**
     * return scaleup factor applied to last Hires image of page generated
     *
     * negative values mean no upscaling applied and should be ignored
     */
    float getHiResUpscaleFactor(){

        return multiplyer;
    }

    void setParms(final int displayRotation, final float scaling, final int indent, final int specialMode) {
        this.displayRotation=displayRotation;
        this.scaling=scaling;
        this.indent=indent;
        this.specialMode=specialMode;
    }

    void resetOnOpen() {
        warnOnceOnForms=false;
    }

    /**
     * used to update statusBar object if exists
     */
    class ProgressListener implements ActionListener {

        @Override
        public void actionPerformed(final ActionEvent evt) {

            statusBar.setProgress((int) (statusBar.percentageDone));
        }
    }

    /**
     *
     * access textlines object
     */
    TextLines getTextLines() {
        return textLines;
    }

    Parser(final ExternalHandlers externalHandlers, final DecoderOptions options, final FileAccess fileAcces, final PdfResources res, final DecoderResults resultsFromDecode) {

        this.externalHandlers=externalHandlers;
        this.options=options;
        this.fileAcces=fileAcces;
        this.res=res;
        this.resultsFromDecode=resultsFromDecode;

        useJavaFX=externalHandlers.isJavaFX();
        
        //setup Swing of FX display depending on mode (External Handler can be FX of Swing)
        externalHandlers.setDVR(fileAcces);
        
    }

    //

    /**
     *
     * Please do not use for general usage. Use setPageParameters(scalingValue, pageNumber) to set page scaling
     */
    void setExtractionMode(final int mode, final float newScaling) {

        final PdfPageData pageData=fileAcces.getPdfPageData();
        pageData.setScalingValue(newScaling); //ensure aligned

        extractionMode = mode;

        final PdfLayerList layers=res.getPdfLayerList();
        if(layers!=null){
            final boolean layersChanged=layers.setZoom(newScaling);

            if(layersChanged){
//                try {
                    decodePage(-1);
////                } catch (final Exception e) {
////                    //tell user and log
////                    if(LogWriter.isOutput()) {
////                        LogWriter.writeLog("Exception: " + e.getMessage());
////                    }
////                    //
////                }
            }
        }
    }



    void decodePageInBackground(final int i) throws PdfException {

        if (fileAcces.isDecoding()) {
            if(LogWriter.isOutput()){
                LogWriter.writeLog("[PDF]WARNING - this file is being decoded already in foreground");
                LogWriter.writeLog("[PDF]Multiple access not recommended - use  waitForDecodingToFinish() to check");
            }

        } else if (isBackgroundDecoding) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("[PDF]WARNING - this file is being decoded already in background");
            }
        } else {

            try{
                isBackgroundDecoding = true;

                /** check in range */
                if (i > fileAcces.getPageCount()) {

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Page out of bounds");
                    }

                } else {

                    /** get pdf object id for page to decode */
                    final String currentPageOffset = getIO().getReferenceforPage(i);

                    final AcroRenderer formRenderer=externalHandlers.getFormRenderer();

                    /**
                     * decode the file if not already decoded, there is a valid
                     * object id and it is unencrypted
                     */
                    if (currentPageOffset != null || (formRenderer.isXFA() && formRenderer.useXFA())) {

                        if (getIO() == null) {
                            throw new PdfException(
                                    "File not open - did you call closePdfFile() inside a loop and not reopen");
                        }

                        /** read page or next pages */
                        final PdfObject pdfObject=new PageObject(currentPageOffset);
                        getIO().readObject(pdfObject);
                        final PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

                        //ensure set (needed for XFA)
                        pdfObject.setPageNumber(i);

                        final ObjectStore backgroundObjectStoreRef = new ObjectStore();

                        final PdfStreamDecoder backgroundDecoder=formRenderer.getStreamDecoder(getIO(), options.useHiResImageForDisplay(),res.getPdfLayerList(),false);

                        backgroundDecoder.setParameters(true, false, 0, extractionMode,false,useJavaFX);

                        backgroundDecoder.setXMLExtraction(options.isXMLExtraction());
                        externalHandlers.addHandlers(backgroundDecoder);

                        backgroundDecoder.setObjectValue(ValueTypes.Name, fileAcces.getFilename());
                        //Display object added but not rendered as renderPage is false (DO NOT REMOVE, BREAKS SEARCH)
                        backgroundDecoder.setRenderer(new ImageDisplay(fileAcces.getPageNumber(), false, 5000, new ObjectStore()));
                        backgroundDecoder.setObjectValue(ValueTypes.ObjectStore,backgroundObjectStoreRef);
                        backgroundDecoder.setObjectValue(ValueTypes.PDFPageData,fileAcces.getPdfPageData());
                        backgroundDecoder.setIntValue(ValueTypes.PageNum, i);

                        res.setupResources(backgroundDecoder, false, Resources,fileAcces.getPageNumber(),getIO());

                        backgroundDecoder.decodePageContent(pdfObject);

                        //get extracted data
                        pdfBackgroundData = (PdfData)backgroundDecoder.getObjectValue(ValueTypes.PDFData);
                        pdfBackgroundImages = (PdfImageData) backgroundDecoder.getObjectValue(ValueTypes.PDFImages);


                    }
                }

            }catch(final PdfException e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }finally {
                isBackgroundDecoding = false;
            }
        }
    }



    /**
     * generate BufferedImage of a page in current file
     */
    BufferedImage getPageAsImage(final int pageIndex, final boolean imageIsTransparent) throws PdfException {

        BufferedImage image = null;

        // make sure in range
        if (pageIndex > fileAcces.getPageCount() || pageIndex < 1) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Page " + pageIndex + " not in range");
            }
        } else {

            if (getIO() == null) {
                throw new PdfException("File not open - did you call closePdfFile() inside a loop and not reopen");
            }

            /** get pdf object id for page to decode */
            final String currentPageOffset = getIO().getReferenceforPage(pageIndex);

            final PdfPageData pageData=fileAcces.getPdfPageData();

            if (currentPageOffset != null || externalHandlers.getFormRenderer().isXFA()) {

                final PDFtoImageConvertor pdfToImageConvertor=externalHandlers.getConverter(multiplyer, options);
                
                image = pdfToImageConvertor.convert(resultsFromDecode, displayRotation, res, externalHandlers,renderMode,pageData,externalHandlers.getFormRenderer(),scaling,getIO(),pageIndex, imageIsTransparent, currentPageOffset);

                //Check for exceptions in TrueType hinting and re decode if neccessary
                if (TTGlyph.redecodePage) {
                    TTGlyph.redecodePage = false;
                    return getPageAsImage(pageIndex, imageIsTransparent);
                }

                multiplyer=pdfToImageConvertor.getMultiplyer();

            }

            //workaround for bug in AIX
            if (!DecoderOptions.isRunningOnAIX && !imageIsTransparent && image != null) {
                image = ColorSpaceConvertor.convertToRGB(image);
            }

        }

        return image;
    }

    /**
     * set render mode to state what is displayed onscreen (ie
     * RENDERTEXT,RENDERIMAGES) - only generally required if you do not wish to
     * show all objects on screen (default is all). Add values together to
     * combine settings.
     */
    void setRenderMode(final int mode) {

        renderMode = mode;

        extractionMode = mode;

    }

    /**
     * set extraction mode telling JPedal what to extract -
     * (TEXT,RAWIMAGES,FINALIMAGES - add together to combine) - See
     * org.jpedal.examples for specific extraction examples
     */
    void setExtractionMode(final int mode) {

        extractionMode = mode;

    }

    void disposeObjects() {

        FontMappings.fontsInitialised=false;

        externalHandlers.dispose();

        if(pdfData!=null) {
            pdfData.dispose();
        }
        pdfData=null;



        FontMappings.defaultFont=null;



        //        if(current!=null)
        //            current.dispose();

        fileAcces.dispose();

    }

    /**
     * will return some dictionary values - if not a set value, will return null
     * @return
     */
    Object getJPedalObject(final int id){
        switch(id){
            case PdfDictionary.Layer:
                return res.getPdfLayerList();

            case PdfDictionary.Linearized:

                return fileAcces.linearParser.getLinearObject(fileAcces.isOpen,getIO());

            case PdfDictionary.LinearizedReader:

                return fileAcces.linearParser.linearizedBackgroundReaderer;

            case PdfDictionary.FileAccess:

                return fileAcces;

            default:
                return null;
        }
    }

    /**
     * @param pageIndex number of the page we want to extract
     * @return image of the extracted page
     * @throws org.jpedal.exception.PdfException
     * Page size is defined by CropBox
     * see http://files.idrsolutions.com/samplecode/org/jpedal/examples/images/ConvertPagesToHiResImages.java.html for full details
     *
     */
    synchronized BufferedImage getPageAsHiRes(final int pageIndex, final boolean isTransparent)throws PdfException{

        multiplyer=options.getImageDimensions(pageIndex, fileAcces.getPdfPageData());

        return getPageAsImage(pageIndex, isTransparent);

    }

    /**
     * see if page available if in Linearized mode or return true
     * @param rawPage
     * @return
     */
    synchronized boolean isPageAvailable(final int rawPage) {

        return fileAcces.linearParser.isPageAvailable(rawPage, getIO());

    }

    /**
     * Access should not generally be required to
     * this class. Please look at getBackgroundGroupingObject() - provide method
     * for outside class to get data object containing text and metrics of text. -
     * Viewer can only access data for finding on page
     *
     * @return PdfData object containing text content from PDF
     */
    PdfData getPdfBackgroundData() {

        return pdfBackgroundData;
    }

    /**
     * Access should not generally be required to
     * this class. Please look at getGroupingObject() - provide method for
     * outside class to get data object containing raw text and metrics of text<br> -
     * Viewer can only access data for finding on page
     *
     * @return PdfData object containing text content from PDF
     */
    PdfData getPdfData() throws PdfException {
        if ((extractionMode & PdfDecoderInt.TEXT) == 0) {
            throw new PdfException(
                    "[PDF] Page data object requested will be empty as text extraction disabled. Enable with PdfDecoder method setExtractionMode(PdfDecoderInt.TEXT | other values");
        } else {
            return pdfData;
        }
    }

    /**
     * returns object containing grouped text of last decoded page
     * - if no page decoded, a Runtime exception is thrown to warn user
     * Please see org.jpedal.examples.text for example code.
     *
     */
    PdfGroupingAlgorithms getGroupingObject() throws PdfException {

        return options.getGroupingObject(fileAcces.getLastPageDecoded() , getPdfData(), fileAcces.getPdfPageData());

    }

    /**
     * returns object containing grouped text from background grouping - Please
     * see org.jpedal.examples.text for example code
     */
    PdfGroupingAlgorithms getBackgroundGroupingObject() {

        return options.getBackgroundGroupingObject(pdfBackgroundData, fileAcces.getPdfPageData());
    }
    
    /**
     * provide method for outside class to get data object
     * containing images
     *
     * @return PdfImageData containing image metadata
     */
    PdfImageData getPdfImageData() {
        return pdfImages;
    }

    /**
     * provide method for outside class to get data object
     * containing images.
     *
     * @return PdfImageData containing image metadata
     */
    PdfImageData getPdfBackgroundImageData() {
        return pdfBackgroundImages;
    }

    /**
     * provide method for outside class to clear store of objects once written
     * out to reclaim memory
     *
     * @param reinit lag to show if image data flushed as well
     */
    void flushObjectValues(final boolean reinit) {

        if (pdfData != null && !reinit) {
            pdfData.flushTextList();
        }

        if (pdfImages != null && reinit) {
            pdfImages.clearImageData();
        }

    }

    /**
     * return any errors or other messages while calling decodePage() - zero
     * length is no problems
     */
    String getPageDecodeReport() {
        return decodeStatus;
    }


    /**
     * Returns list of the fonts used on the current page decoded or null
     * type can be PdfDictionary.Font or PdfDictionary.Image
     */
    String getInfo(final int type) {

        final String returnValue;

        switch (type) {
            case PdfDictionary.Font:

                if (fontsInFile == null) {
                    returnValue = "No fonts defined";
                } else {
                    returnValue = fontsInFile;
                }

                break;

            case PdfDictionary.Image:

                if (imagesInFile == null) {
                    returnValue = "No images defined as XObjects";
                } else {
                    returnValue = imagesInFile;
                }

                break;

            default:
                returnValue = null;
        }

        return returnValue;

    }

    void decodePage(int rawPage) {

        //flag if decoding started
        final Object customErrorTracker=externalHandlers.getExternalHandler(Options.ErrorTracker);

        if(customErrorTracker!=null) {
            ((ErrorTracker) customErrorTracker).startedPageDecoding(rawPage);
        }

        //allow us to insert our own version (ie HTML)
        final DynamicVectorRenderer customDVR = (DynamicVectorRenderer) externalHandlers.getExternalHandler(Options.CustomOutput);

        final DynamicVectorRenderer currentDisplay;

        final PdfPageData pageData=fileAcces.getPdfPageData();

        //
        {
            currentDisplay=fileAcces.getDynamicRenderer();
        }

        //flag to allow us to not do some things when we re decode the page with layers on for example
        boolean isDuplicate = false;
        if(rawPage==-1){
            rawPage=fileAcces.getLastPageDecoded();
            isDuplicate = true;
        }

        final int page=rawPage;

        if (fileAcces.isDecoding()) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("[PDF]WARNING - this file is being decoded already - use  waitForDecodingToFinish() to check");
            }

        } else {

            PdfObject pdfObject=fileAcces.linearParser.getLinearPageObject();

            final AcroRenderer formRenderer=externalHandlers.getFormRenderer();

            fileAcces.setDecoding(true);

            try{
                fileAcces.setDecoding(true);

                final PdfLayerList layers=res.getPdfLayerList();
                if(layers!=null && layers.getChangesMade()){
                    handleJSInLayer(formRenderer, layers);
                }

                //<start-demo><end-demo>

                fileAcces.setLastPageDecoded(page);

                decodeStatus = "";

                // <start-demo><end-demo>

                /** flush renderer */
                currentDisplay.flush();

                /** check in range */
                if (page > fileAcces.getPageCount() || page < 1) {

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Page out of bounds");
                    }

                    fileAcces.setDecoding(false);

                } else{

                    /**
                     * title changes to give user something to see under timer
                     * control
                     */
                    javax.swing.Timer t = null;
                    if (statusBar != null) {
                        final ActionListener listener = new ProgressListener();
                        t = new javax.swing.Timer(150, listener);
                        t.start(); // start it
                    }

                    fileAcces.setPageNumber(page);

                    /**
                     * sanity check I/O and Pdfobject and initi PDF object
                     */
                    if (getIO() == null) {
                        throw new PdfException("File not open - did you call closePdfFile() inside a loop and not reopen");
                    }

                    pdfObject = getPdfObject(page, pdfObject);

                    final PdfStreamDecoder current = setupObjectsForDecode(currentDisplay, pageData, page, pdfObject, formRenderer);

                   // if(isHTML)
                  //      current.setObjectValue(ValueTypes.DirectRendering, null);//(Graphics2D) graphics);
        
                    try {
                        /*
                        * If highlights are required for page, reset highlights
                        */
                        if(textLines != null) {
                            textLines.setLineAreas(null);
                        }

                        //
                        current.decodePageContent(pdfObject);
                        
                    } catch (final Error err) {
                        decodeStatus = decodeStatus+ "Error in decoding page "+ err;
                    } catch (final PdfException e) {

                        //

                        //cascade up so we can show in viewer
                        if(e.getMessage()!=null && e.getMessage().contains("JPeg 2000")){
                            decodeStatus = decodeStatus+ "Error in decoding page "+ e;
                        }
                    }

                    setResultsFromDecode(page, current);


                    /** turn off status bar update */
                    if (t != null) {
                        t.stop();
                        statusBar.setProgress(100);

                    }
                    
                    /**
                     * handle acroform data to display
                     */
                    if (options.getRenderPage() && !isDuplicate && (renderMode & PdfDecoderInt.REMOVE_NOFORMS) != PdfDecoderInt.REMOVE_NOFORMS && !formRenderer.ignoreForms()) {

                        final PageOffsets currentOffset=fileAcces.getOffset();
                        if(currentOffset!=null) {
                            formRenderer.getCompData().setPageValues(scaling, displayRotation, indent, 0, 0, Display.SINGLE_PAGE, currentOffset.getWidestPageNR(), currentOffset.getWidestPageR());
                        }

                        formRenderer.createDisplayComponentsForPage(page,current);

                        formRenderer.getFormFactory().indexAllKids();
                        
                        //critical we enable this code in standard mode to render forms
                        if (!formRenderer.useXFA()) {
                        
                            if (currentDisplay.getType() == DynamicVectorRenderer.CREATE_HTML || currentDisplay.getType() == DynamicVectorRenderer.CREATE_SVG) {

                                java.util.List[] formsOrdered = formRenderer.getCompData().getFormList(true);

                                //get unsorted components and iterate over forms
                                for (Object nextVal : formsOrdered[page]) {

                                    if (nextVal != null) {

                                        formRenderer.getFormFlattener().drawFlattenedForm(current,(org.jpedal.objects.raw.FormObject) nextVal, true, (PdfObject) formRenderer.getFormResources()[0]);

                                    }
                                }
                            }
                        }
                        
                        if(specialMode!= SpecialOptions.NONE &&
                                specialMode!= SpecialOptions.SINGLE_PAGE &&
                                page!=fileAcces.getPageCount()) {
                                //
                        }
                        
                        // }
                    }
                }
            } catch (PdfException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

                //

                fileAcces.setDecoding(false);

                if(statusBar!=null) {
                    statusBar.percentageDone = 100;
                }
            }
        }

        //Check for exceptions in TrueType hinting and re decode if neccessary
        if (TTGlyph.redecodePage) {
            TTGlyph.redecodePage = false;
            decodePage(rawPage);
        }
        
        if(customErrorTracker!=null) {
            ((ErrorTracker) customErrorTracker).finishedPageDecoding(rawPage);
        }

        //tell software page all done
        fileAcces.getDynamicRenderer().flagDecodingFinished();
    }

    private PdfObject getPdfObject(final int page, PdfObject pdfObject) {
        /** get pdf object id for page to decode */
        if(pdfObject==null){
            pdfObject=new PageObject(getIO().getReferenceforPage(page));

            getIO().readObject(pdfObject);

            //allow for res in parent and add to object
            getIO().checkParentForResources(pdfObject);
        }

        if(pdfObject.getPageNumber()==-1){
            pdfObject.setPageNumber(page);
        }
        return pdfObject;
    }

    private void handleJSInLayer(final AcroRenderer formRenderer, final PdfLayerList layers) {
        /**
         * execute any JS needed (true flushes list)
         */
        final Iterator commands=layers.getJSCommands();
        final Javascript javascript=externalHandlers.getJavaScript();
        if(javascript!=null && commands!=null){
            //execute code here
            while(commands.hasNext()){
                javascript.executeAction((String) commands.next());
            }
        }

        fileAcces.setLastPageDecoded(-1);
        layers.setChangesMade(false);//set flag to say we have decoded the changes

        //refresh forms in case any effected by layer change
        formRenderer.getCompData().setForceRedraw(true);
        formRenderer.getCompData().setLayerData(layers);
        formRenderer.getCompData().resetScaledLocation(scaling,displayRotation,indent);//indent here does nothing.
    }

    private PdfStreamDecoder setupObjectsForDecode(final DynamicVectorRenderer currentDisplay, final PdfPageData pageData, final int page, final PdfObject pdfObject, final AcroRenderer formRenderer) throws PdfException {
        final PdfStreamDecoder current;//location for non-XFA res
        PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

        /** read page or next pages */
        if(formRenderer.isXFA() && formRenderer.useXFA()){
            current = formRenderer.getStreamDecoder(getIO(), options.useHiResImageForDisplay(), res.getPdfLayerList(), false);
            Resources=(PdfObject) formRenderer.getFormResources()[0];//XFA in Acroforms
        }else{
            
            //needs to be out of loop as we can get flattened forms on pages with no content
            current = formRenderer.getStreamDecoder(getIO(), options.useHiResImageForDisplay(),res.getPdfLayerList(),false);

            if(!warnOnceOnForms){
                warnOnceOnForms=true; //not used in XFA at present but set for consistency
            }
        }
        
        if(!warnOnceOnForms){
            warnOnceOnForms=formRenderer.showFormWarningMessage(page);
        }
        
        /** set hires mode or not for display */
        current.setXMLExtraction(options.isXMLExtraction());

        currentDisplay.setHiResImageForDisplayMode(options.useHiResImageForDisplay());
        currentDisplay.setPrintPage(page);
        currentDisplay.setCustomColorHandler((ColorHandler) externalHandlers.getExternalHandler(Options.ColorHandler));

        current.setParameters(true, options.getRenderPage(), renderMode, extractionMode,false,useJavaFX);

        externalHandlers.addHandlers(current);

        //

        current.setObjectValue(ValueTypes.Name, fileAcces.getFilename());
        current.setIntValue(ValueTypes.PageNum, page);
        current.setRenderer(currentDisplay);
        current.setObjectValue(ValueTypes.ObjectStore,fileAcces.getObjectStore());
        current.setObjectValue(ValueTypes.StatusBar, statusBar);
        current.setObjectValue(ValueTypes.PDFPageData,pageData);

        res.setupResources(current, false, Resources, page, getIO());

        currentDisplay.init(pageData.getMediaBoxWidth(page), pageData.getMediaBoxHeight(page), options.getPageColor());

        if((!currentDisplay.isHTMLorSVG())&& (options.getTextColor()!=null)){
                currentDisplay.setValue(DynamicVectorRenderer.ALT_FOREGROUND_COLOR, options.getTextColor().getRGB());

                if(options.getChangeTextAndLine()) {
                    currentDisplay.setValue(DynamicVectorRenderer.FOREGROUND_INCLUDE_LINEART, 1);
                } else {
                    currentDisplay.setValue(DynamicVectorRenderer.FOREGROUND_INCLUDE_LINEART, 0);
                }

                currentDisplay.setValue(DynamicVectorRenderer.COLOR_REPLACEMENT_THRESHOLD, options.getReplacementColorThreshold());
            }
        
        return current;
    }

    private void setResultsFromDecode(final int page, final PdfStreamDecoder current) {
        //All data loaded so now get all line areas for page
        if(textLines!=null && extractionMode>0){
            final Vector_Rectangle_Int vr = (Vector_Rectangle_Int) current.getObjectValue(ValueTypes.TextAreas);
            vr.trim();
            final int[][] pageTextAreas = vr.get();

            final Vector_Int vi =  (Vector_Int) current.getObjectValue(ValueTypes.TextDirections);
            vi.trim();
            final int[] pageTextDirections = vi.get();

            for(int k=0; k!=pageTextAreas.length; k++){
                textLines.addToLineAreas(pageTextAreas[k], pageTextDirections[k], page);
            }
        }

        /**
         * set flags after decode
         */
        fontsInFile = (String) current.getObjectValue(PdfDictionary.Font);
        imagesInFile = (String) current.getObjectValue(PdfDictionary.Image);

        pdfData = (PdfData) current.getObjectValue(ValueTypes.PDFData);

        pdfImages = (PdfImageData) current.getObjectValue(ValueTypes.PDFImages);

        //read flags
        resultsFromDecode.update(current,true);
    }

     protected PdfObjectReader getIO() {
         return fileAcces.getIO();
     }
     
     void resetMultiplyer() {
         multiplyer = 1; // Reset multiplier so we don't get an image scaled too far in
     }
     
     void resetFontsInFile() {
         fontsInFile="";
     }
     
     void setStatusBar(final StatusBar statusBar) {
         this.statusBar=statusBar;
     }
}
