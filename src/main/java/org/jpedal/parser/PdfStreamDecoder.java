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
 * PdfStreamDecoder.java
 * ---------------
 */
package org.jpedal.parser;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import org.jpedal.PdfDecoderInt;
import org.jpedal.color.PdfPaint;
import org.jpedal.constants.PageInfo;
import org.jpedal.exception.PdfException;
import org.jpedal.external.*;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.JavaFXSupport;
import org.jpedal.fonts.glyph.T3Size;
import org.jpedal.images.SamplingFactory;
import org.jpedal.io.DefaultErrorTracker;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.StatusBar;
import org.jpedal.objects.*;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.*;
import org.jpedal.parser.color.* ;
import org.jpedal.parser.gs.*;
import org.jpedal.parser.image.*;
import org.jpedal.parser.shape.*;
import org.jpedal.parser.text.*;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

/**
 * Contains the code which 'parses' the commands in
 * the stream and extracts the data (images and text).
 * Users should not need to call it.
 */
public class PdfStreamDecoder extends BaseDecoder{
    
    private static boolean showFXShadingMessage;
    
    //
    
    int formLevel;
    
    private int BDCDepth=-1;
    
    PdfObjectCache cache;
    
    PdfPageData pageData;
    
    ErrorTracker errorTracker;
    
    PdfObjectReader currentPdfFile;
        
    protected GraphicsState newGS;
    
    protected byte[] pageStream;
    
    PdfLayerList layers;
    
    protected boolean getSamplingOnly;
    
    private final Map shadingColorspacesObjects=new HashMap(50);
    
    private boolean isTTHintingRequired;
    
    final Vector_Int textDirections=new Vector_Int();
    
    final Vector_Rectangle_Int textAreas = new Vector_Rectangle_Int();
    
    /**shows if t3 glyph uses internal colour or current colour*/
    public boolean ignoreColors;
    
    /**images on page*/
    int imageCount;
    
    /**BBox on Form object*/
    float[] BBox;
    
    String lastTextValue="";
    
    //trap for recursive loop of xform calling itself
    int lastDataPointer=-1;
    
    private T3Decoder t3Decoder;
    
    /**flag to show if we REMOVE shapes*/
    private boolean removeRenderImages;
    
    //last Trm incase of multple Tj commands
    private boolean multipleTJs;
    
    /**flags to show we need colour data as well*/
    private boolean textColorExtracted;
    
    /**flag to show text is being extracted*/
    private boolean textExtracted=true;
    
    /**flag to show content is being rendered*/
    private boolean renderText;
    
    private int tokenNumber;
    
    /**list of images used for display*/
    String imagesInFile;
    
    //set threshold - value indicates several possible values
    public static final float currentThreshold=0.595f;
    
    protected ImageHandler customImageHandler;
    
    private PdfFontFactory pdfFontFactory;
    
    boolean isXMLExtraction;
    
    /**
     * internal development flag which should not be used
     */
    //turn on debugging to see commands
    public static final boolean showCommands=false;
//    public static boolean showCommands = true;
    
    /**interactive display*/
    private StatusBar statusBar;
    
    /**store text data and can be passed out to other classes*/
    final PdfData pdfData = new PdfData();
    
    /**store image data extracted from pdf*/
    final PdfImageData pdfImages=new PdfImageData();
    
    /**used to debug*/
    protected static String indent="";
    
    /**show if possible error in stream data*/
    protected boolean isDataValid=true;
    
    /**used to store font information from pdf and font functionality*/
    private PdfFont currentFontData;
    
    /**flag to show we use hi-res images to draw onscreen*/
    protected boolean useHiResImageForDisplay;
    
    protected ObjectStore objectStoreStreamRef;
    
    String formName="";
    
    protected boolean isType3Font;
    
    public static boolean useTextPrintingForNonEmbeddedFonts;
    
    /**allows us to terminate file if looks like might crash JVM due to complexity*/
    private static int maxShapesAllowed=-1;
    
    // Used to get the blendmode of an object in PDFObjectToImage
    private int currentBlendMode=PdfDictionary.Normal;
    
    static{
        SamplingFactory.setDownsampleMode(null);
        
        /**
         * we have PDFs which crashes JVM so workaround to avoid this.
         */
        final String maxShapes=System.getProperty("org.jpedal.maxShapeCount");
        if(maxShapes!=null){
            try{
                maxShapesAllowed=Integer.parseInt(maxShapes);
            }catch(final Exception e){
                throw new RuntimeException("Your setting ("+maxShapes+")for org.jpedal.maxShapeCount is not a valid number "+e);
            }
        }
    }
    
    public PdfStreamDecoder(final PdfObjectReader currentPdfFile){
        
        init(currentPdfFile);
        
    }
    
    /**
     * create new StreamDecoder to create screen display with hires images
     */
    public PdfStreamDecoder(final PdfObjectReader currentPdfFile, final boolean useHiResImageForDisplay, final PdfLayerList layers) {
        
        if(layers!=null) {
            this.layers = layers;
        }
        
        this.useHiResImageForDisplay=useHiResImageForDisplay;
        
        init(currentPdfFile);
    }
    
    private void init(final PdfObjectReader currentPdfFile) {
        
        cache=new PdfObjectCache();
        gs=new GraphicsState();
        errorTracker=new DefaultErrorTracker();
        
        pageData = new PdfPageData();
        
        StandardFonts.checkLoaded(StandardFonts.STD);
        StandardFonts.checkLoaded(StandardFonts.MAC);
        
        this.currentPdfFile=currentPdfFile;
        pdfFontFactory =new PdfFontFactory(currentPdfFile);
        
    }
    
    /**
     *
     *  objects off the page, stitch into a stream and
     * decode and put into our data object. Could be altered
     * if you just want to read the stream
     * @param pdfObject
     * @throws PdfException
     */
    public T3Size decodePageContent(final PdfObject pdfObject) throws PdfException{
        
        try{
            
            //check switched off
            parserOptions.imagesProcessedFully=true;
            parserOptions.tooManyShapes=false;
            
            //reset count
            imageCount=0;
            
            parserOptions.setPdfLayerList(this.layers);
            
            //reset count
            imagesInFile=null; //also reset here as good point as syncs with font code
            
            if(!parserOptions.renderDirectly() && statusBar!=null) {
                statusBar.percentageDone = 0;
            }
            
            if(newGS!=null) {
                gs = newGS;
            } else {
                gs = new GraphicsState(0, 0);
            }
            
            //save for later
            if (parserOptions.isRenderPage()){
                
                /**
                 * check setup and throw exception if null
                 */
                if(current==null) {
                    throw new PdfException("DynamicVectorRenderer not setup PdfStreamDecoder setStore(...) should be called");
                }
                
                current.drawClip(gs,parserOptions.defaultClip, false) ;
                
                final int pageNum=parserOptions.getPageNumber();
                
                //Paint background here to ensure we all for changed background color in extraction modes
                current.paintBackground(new Rectangle(pageData.getCropBoxX(pageNum), pageData.getCropBoxY(pageNum),
                        pageData.getCropBoxWidth(pageNum), pageData.getCropBoxHeight(pageNum)));
            }
            
            //get the binary data from the file
            final byte[] b_data;
            
            byte[][] pageContents= null;
            if(pdfObject !=null){
                pageContents= pdfObject.getKeyArray(PdfDictionary.Contents);
                isDataValid= pdfObject.streamMayBeCorrupt();
            }
            
            if(pdfObject !=null && pageContents==null) {
                b_data = currentPdfFile.readStream(pdfObject, true, true, false, false, false, pdfObject.getCacheName(currentPdfFile.getObjectReader()));
            } else if(pageStream!=null) {
                b_data = pageStream;
            } else {
                b_data = currentPdfFile.getObjectReader().readPageIntoStream(pdfObject);
            }
            
            //trap for recursive loop of xform calling itself
            lastDataPointer=-1;
            
            //if page data found, turn it into a set of commands
            //and decode the stream of commands
            if (b_data!=null && b_data.length > 0) {
                decodeStreamIntoObjects(b_data, false);
            }
            
            //flush fonts
            if(!isType3Font) {
                cache.resetFonts();
            }
            
            final T3Size t3=new T3Size();
            if(t3Decoder!=null){
                t3.x = t3Decoder.T3maxWidth;
                t3.y = t3Decoder.T3maxHeight;
                ignoreColors=t3Decoder.ignoreColors;
                t3Decoder=null;
            }
            
            return t3;
            
            
        }catch(final Error err){
            
            //
            
            if (ExternalHandlers.throwMissingCIDError && err.getMessage()!=null && err.getMessage().contains("kochi")) {
                throw err;
            }
            
            errorTracker.addPageFailureMessage("Problem decoding page " + err);
            
        }
        
        return null;
    }

    public void setObjectValue(final int key, final Object  obj){
        
        switch(key){
            
            case ValueTypes.Name:
                parserOptions.setName((String) obj);
                break;
                
            case ValueTypes.PDFPageData:
                pageData=(PdfPageData)obj;
                //flag if colour info being extracted
                if(textColorExtracted) {
                    pdfData.enableTextColorDataExtraction();
                }
                
                break;
                
                /**
                 * pass in status bar object
                 *
                 */
            case ValueTypes.StatusBar:
                this.statusBar=(StatusBar)obj;
                break;
                
            case ValueTypes.PdfLayerList:
                this.layers=(PdfLayerList) obj;
                break;
                
                //
                
            case ValueTypes.ImageHandler:
                this.customImageHandler = (ImageHandler)obj;
                if(customImageHandler!=null && current!=null) {
                    current.setCustomImageHandler(this.customImageHandler);
                }
                break;
                
                /**
                 * setup stream decoder to render directly to g2
                 * (used by image extraction)
                 */
            case ValueTypes.DirectRendering:
                
                current.setIsRenderingToImage(true);
                
                parserOptions.setRenderDirectly(true);
                
                if(obj!=null){
                    final Graphics2D g2 = (Graphics2D)obj;
                    parserOptions.defaultClip=g2.getClip();
                }
                break;
                
                /** should be called after constructor or other methods may not work*/
            case ValueTypes.ObjectStore:
                objectStoreStreamRef = (ObjectStore)obj;
                
                //current=new SwingDisplay(this.pageNum,objectStoreStreamRef,false);
                if(current!=null){
                    current.setHiResImageForDisplayMode(useHiResImageForDisplay);
                    
                    if(customImageHandler!=null && current!=null) {
                        current.setCustomImageHandler(customImageHandler);
                    }
                }
                
                break;
                
            case Options.ErrorTracker:
                this.errorTracker = (ErrorTracker) obj;
                break;
                
                
            case Options.ShapeTracker:
                parserOptions.setCustomShapeTracker ((ShapeTracker) obj);
                break;
                
                
        }
    }
    
    /**
     * flag to show interrupted by user
     */
    
    boolean isPrinting;
    
    /**
     * NOT PART OF API
     * tells software to generate glyph when first rendered not when decoded.
     * Should not need to be called in general usage
     * @param key
     * @param value
     */
    public void setBooleanValue(final int key, final boolean value) {
        
      switch(key){
            
            case GenerateGlyphOnRender:
                parserOptions.setGenerateGlyphOnRender(value);
                break;
        }
    }
    
    /**/
    
    /**used internally to allow for colored streams*/
    public void setDefaultColors(final PdfPaint strokeCol, final PdfPaint nonstrokeCol) {
        
        gs.strokeColorSpace.setColor(strokeCol);
        gs.nonstrokeColorSpace.setColor(nonstrokeCol);
        gs.setStrokeColor(strokeCol);
        gs.setNonstrokeColor(nonstrokeCol);
    }
    
    /**return the data*/
    public Object getObjectValue(final int key){
        
        switch(key){
            case ValueTypes.PDFData:
                if (DecoderOptions.embedWidthData) {
                    pdfData.widthIsEmbedded();
                }
                
                // store page width/height so we can translate 270
                // rotation co-ords
                //pdfData.maxX = pageData.getMediaBoxWidth(pageNum);
                //pdfData.maxY = pageData.getMediaBoxHeight(pageNum);
                
                return  pdfData;
                
            case ValueTypes.PDFImages:
                return  pdfImages;
                
            case ValueTypes.TextAreas:
                return textAreas;
                
            case ValueTypes.TextDirections:
                return textDirections;
                
            case ValueTypes.DynamicVectorRenderer:
                return current;
                
            case PdfDictionary.Font:
                return  pdfFontFactory.getFontsInFile();
                
            case PdfDictionary.Image:
                return imagesInFile;
                
            case DecodeStatus.NonEmbeddedCIDFonts:
                return pdfFontFactory.getnonEmbeddedCIDFonts();
                
            case PageInfo.COLORSPACES:
                return cache.iterator(PdfObjectCache.ColorspacesUsed);
                
            default:
                return null;
                
                
        }
    }
    
    /**
     * read page header and extract page metadata
     * @throws PdfException
     */
    public final void readResources(final PdfObject Resources, final boolean resetList) throws PdfException {
        
        if(resetList) {
            pdfFontFactory.resetfontsInFile();
        }
        
        currentPdfFile.checkResolved(Resources);
        
        cache.readResources(Resources, resetList);
        
    }
    
    
    /**
     * decode the actual 'Postscript' stream into text and images by extracting
     * commands and decoding each.
     */
    public String decodeStreamIntoObjects(final byte[] stream, final boolean returnText) {
        
        if(stream.length==0) {
            return null;
        }
        
        //start of Dictionary on Inline image
        int startInlineStream=0;
        
        final CommandParser parser=new CommandParser(stream);
        
        final int streamSize=stream.length;
        int dataPointer = 0;
        int startCommand=0;
        int shapeCommandCount=0;

        //used in CS to avoid miscaching
        String csInUse="",CSInUse="";
        
        PdfShape currentDrawShape = null;
        
        if(parserOptions.useJavaFX()) {
            JavaFXSupport fxSupport = ExternalHandlers.getFXHandler();
            if(fxSupport!=null){
                currentDrawShape = fxSupport.getFXShape();
            }
            
        } else{
            currentDrawShape = new SwingShape();
        }
        
        //setup textDecoder
        final Tj textDecoder;
        if (parserOptions.hasContentHandler()) {
            textDecoder = new Tj(parserOptions,textAreas,textDirections,current,errorTracker);
        } else {
            textDecoder = new Tj(parserOptions,pdfData,isXMLExtraction,textAreas,textDirections,current,errorTracker);
            textDecoder.setReturnText(returnText);
        }
        textDecoder.setStreamType(streamType);
        
        if(statusBar!=null && !parserOptions.renderDirectly()){
            statusBar.percentageDone=0;
            statusBar.resetStatus("stream");
        }
        
        /**
         * loop to read stream and decode
         */
        while (true) {
            
            
            //allow user to request exit and fail page
            if(errorTracker.checkForExitRequest(dataPointer,streamSize)){
                break;
            }
            
            if(statusBar!=null && !parserOptions.renderDirectly()) {
                statusBar.percentageDone = (90 * dataPointer) / streamSize;
            }
            
            dataPointer=parser.getCommandValues(dataPointer,streamSize,tokenNumber);
            final int commandID=parser.getCommandID();
            
            //use negative flag to show commands found
            if(dataPointer<0){
                
                dataPointer=-dataPointer;
                try{
                    
                    /**
                     * call method to handle commands
                     */
                    final int commandType=Cmd.getCommandType(commandID);
                    
                    /**text commands first and all other
                     * commands if not found in first
                     **/
                    switch(commandType){
                        
                        case Cmd.TEXT_COMMAND:
                            
                            if((commandID ==Cmd.EMC || parserOptions.isLayerVisible()) && !getSamplingOnly &&(renderText || textExtracted)){
                                
                                dataPointer =processTextToken(textDecoder, parser, commandID, startCommand, dataPointer);
                                
                            }
                            break;
                            
                        case Cmd.SHAPE_COMMAND:
                            
                            if(!getSamplingOnly){
                                processShapeCommands(parser, currentDrawShape, commandID);
                                
                                shapeCommandCount++;
                                
                                if(maxShapesAllowed>0 && shapeCommandCount>maxShapesAllowed){
                                    
                                    final String errMessage="[PDF] Shapes on page exceed limit set by JVM flag org.jpedal.maxShapeCount - value "+maxShapesAllowed;
                                    
                                    parserOptions.tooManyShapes=true;
                                    throw new PdfException(errMessage);
                                }
                            }
                            
                            break;
                            
                        case Cmd.SHADING_COMMAND:
                            
                            //
                            
                            if(!getSamplingOnly && parserOptions.isRenderPage()){
                                
                                if(parserOptions.useJavaFX){
                                    if(!showFXShadingMessage){
                                        System.out.println("SH not implemented in JavaFX yet");
                                        
                                        showFXShadingMessage=true;
                                    }
                                }else{
                                SH.execute(parser.generateOpAsString(0, true), cache, gs,
                                        isPrinting, shadingColorspacesObjects, parserOptions.getPageNumber(), currentPdfFile,
                                        pageData, current);
                                }
                            }
                            
                            break;
                            
                        case Cmd.COLOR_COMMAND:
                            
                            if(!getSamplingOnly){
                                if(commandID!=Cmd.SCN && commandID!=Cmd.scn && commandID!=Cmd.SC && commandID!=Cmd.sc) {
                                    current.resetOnColorspaceChange();
                                }
                                
                                switch(commandID){
                                    
                                    case Cmd.cs :
                                    {
                                        final String colorspaceObject=parser.generateOpAsString(0, true);
                                        final boolean isLowerCase=true;
                                        //ensure if used for both Cs and cs simultaneously we only cache one version and do not overwrite
                                        final boolean alreadyUsed=(!isLowerCase && colorspaceObject.equals(csInUse))||(isLowerCase && colorspaceObject.equals(CSInUse));
                                        
                                        if(isLowerCase) {
                                            csInUse = colorspaceObject;
                                        } else {
                                            CSInUse = colorspaceObject;
                                        }
                                        
                                        CS.execute(isLowerCase, colorspaceObject, gs, cache, currentPdfFile, isPrinting, parserOptions.getPageNumber(), pageData, alreadyUsed);
                                        break;
                                    }
                                    case Cmd.CS :
                                        
                                        final String colorspaceObject=parser.generateOpAsString(0, true);
                                        final boolean isLowerCase=false;
                                        //ensure if used for both Cs and cs simultaneously we only cache one version and do not overwrite
                                        final boolean alreadyUsed=(!isLowerCase && colorspaceObject.equals(csInUse))||(isLowerCase && colorspaceObject.equals(CSInUse));
                                        
                                        if(isLowerCase) {
                                            csInUse = colorspaceObject;
                                        } else {
                                            CSInUse = colorspaceObject;
                                        }
                                        
                                        CS.execute(isLowerCase, colorspaceObject, gs, cache, currentPdfFile, isPrinting, parserOptions.getPageNumber(), pageData, alreadyUsed);
                                        break;
                                        
                                    case Cmd.rg :
                                        RG.execute(true, gs, parser, cache);
                                        break;
                                        
                                    case Cmd.RG :
                                        RG.execute(false, gs, parser, cache);
                                        break;
                                        
                                    case Cmd.SCN :
                                        SCN.execute(false, gs, parser, cache);
                                        break;
                                        
                                    case Cmd.scn :
                                        SCN.execute(true, gs, parser, cache);
                                        break;
                                        
                                    case Cmd.SC :
                                        SCN.execute(false, gs, parser, cache);
                                        break;
                                        
                                    case Cmd.sc :
                                        SCN.execute(true, gs, parser, cache);
                                        break;
                                        
                                    case Cmd.g :
                                        G.execute(true, gs, parser, cache);
                                        break;
                                        
                                    case Cmd.G :
                                        G.execute(false, gs, parser, cache);
                                        break;
                                        
                                    case Cmd.k :
                                        K.execute(true, gs, parser, cache);
                                        break;
                                        
                                    case Cmd.K :
                                        K.execute(false, gs, parser, cache);
                                        break;
                                        
                                }
                                
                            }
                            
                            break;
                            
                        case Cmd.GS_COMMAND:
                            
                            processGScommands(parser, commandID);
                            
                            //may have changed so read back and reset
                            if(commandID ==Cmd.cm && textDecoder!=null) {
                                multipleTJs = false;
                            }
                            
                            break;
                            
                        case Cmd.IMAGE_COMMAND:
                            
                            if(commandID==Cmd.BI){
                                startInlineStream= dataPointer;
                            }else{


                                PdfObject XObject=null;
                                int subtype=1;
                                if(commandID==Cmd.Do){
                                    
                                    final String name=parser.generateOpAsString(0, true);
                                    //byte[] rawData;
                                    
                                    XObject = cache.getXObjects(name);
                                    if (XObject != null){
                                        
                                        //rawData=XObject.getUnresolvedData();
                                        
                                        currentPdfFile.checkResolved(XObject);
                                        
                                        subtype = XObject.getParameterConstant(PdfDictionary.Subtype);
                                    }
                                    
                                    if (subtype == PdfDictionary.Form){
                                        
                                        if(formLevel>100 &&  dataPointer==lastDataPointer){
                                            //catch for odd files like 11jun/results.pdf
                                        }else{
                                            lastDataPointer=dataPointer;
                                            
                                            if(!parserOptions.isLayerVisible() || (layers!=null && !layers.isVisible(XObject)) || XObject==null){
                                                //
                                            }else{
                                                XFormDecoder.processXForm(this, dataPointer, XObject, parserOptions.defaultClip, parser);
                                            }
                                            
                                            //THIS TURNS OUT TO BE A BAD IDEA!!!!!
                                            //breaks [1719] P012-209_001 Projektplan-Projekt-Plan Nord.pdf
                                            //if lots of objects in play turn back to ref to save memory
//                                            if(1==2 && rawData!=null && cache.getXObjectCount()>30){
//                                                 String ref=XObject.getObjectRefAsString();
//
//                                                cache.resetXObject(name,ref,rawData);
//                                                XObject=null;
//
//                                            }
                                        }
                                    }
                                }
                                
                                if (subtype != PdfDictionary.Form){
                                    
                                    final ImageDecoder imageDecoder;
                                    
                                    if(commandID!=Cmd.Do){
                                        imageDecoder= new ID(imageCount,currentPdfFile,errorTracker,customImageHandler,objectStoreStreamRef,pdfImages, formLevel,pageData,imagesInFile,formName);
                                    }else{
                                        imageDecoder= new DO(imageCount,currentPdfFile,errorTracker,customImageHandler,objectStoreStreamRef,pdfImages, formLevel,pageData,imagesInFile,formName);
                                    }
                                    
                                    imageDecoder.setRes(cache);
                                    imageDecoder.setGS(gs);
                                    imageDecoder.setSamplingOnly(getSamplingOnly);
                                    imageDecoder.setStreamType(streamType);
                                    //imageDecoder.setName(fileName);
                                    imageDecoder.setMultiplyer(multiplyer);
                                    //imageDecoder.setFloatValue(SamplingUsed, samplingUsed);
                                    //imageDecoder.setFileHandler(currentPdfFile);
                                    imageDecoder.setRenderer(current);
                                    
                                    imageDecoder.setParameters(parserOptions.isRenderPage(), parserOptions.getRenderMode(), parserOptions.getExtractionMode(), isPrinting,isType3Font,useHiResImageForDisplay);
                                    imageDecoder.setParams(parserOptions);
                                    
                                    if(commandID==Cmd.Do){
                                        
                                        //size test to remove odd lines in abacus file abacus/EP_Print_Post_Suisse_ID_120824.pdf
                                        if(XObject==null || !parserOptions.isLayerVisible() || (layers!=null && !layers.isVisible(XObject)) || (gs.CTM!=null && gs.CTM[1][1]==0 && gs.CTM[1][0]!=0 && Math.abs(gs.CTM[1][0])<0.2)) {
                                            //ignore
                                        } else {
                                            dataPointer = imageDecoder.processImage(parser.generateOpAsString(0, true), dataPointer, XObject);
                                        }
                                    }else if(parserOptions.isLayerVisible()) {
                                        dataPointer = imageDecoder.processImage(dataPointer, startInlineStream, parser.getStream(), tokenNumber);
                                    }
                                    
                                    imageCount++;
                                    
                                    imagesInFile=imageDecoder.getImagesInFile();
                                    
                                }
                            }
                            break;
                            
                        case Cmd.T3_COMMAND:
                            
                            if(!getSamplingOnly &&(renderText || textExtracted)) {
                                
                                if(t3Decoder==null) {
                                    t3Decoder = new T3Decoder();
                                }
                                
                                t3Decoder.setCommands(parser);
                                t3Decoder.setCommands(parser);
                                t3Decoder.processToken(commandID);
                                
                            }
                            break;
                    }
                } catch (final Exception e) {
                    
                    //
                    
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("[PDF] " + e + " Processing token >" + Cmd.getCommandAsString(commandID) + "<>" + parserOptions.getFileName() + " <" + parserOptions.getPageNumber());
                    }
                    
                    //only exit if no issue with stream
                    if(isDataValid){
                        //
                    }else {
                        dataPointer = streamSize;
                    }
                    
                } catch (final OutOfMemoryError ee) {
                    errorTracker.addPageFailureMessage("Memory error decoding token stream "+ee);
                    
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("[MEMORY] Memory error - trying to recover");
                    }
                }
                
                //save for next command
                startCommand=dataPointer;
                
                //reset array of trailing values
                parser.reset();
                
                //increase pointer
                tokenNumber++;
            }
            
            //break at end
            if (streamSize <= dataPointer) {
                break;
            }
        }
        
        if(!parserOptions.renderDirectly() && statusBar!=null) {
            statusBar.percentageDone = 100;
        }
        
        //pick up TextDecoder values
        isTTHintingRequired=textDecoder.isTTHintingRequired();
        
        if (returnText){
            
            return lastTextValue;
            
        }else {
            return "";
        }
    }
    
    private void processGScommands(final CommandParser parser, final int commandID) {
        
        switch(commandID){
            
            case Cmd.cm :
                
                CM.execute(gs, parser);
                break;
                
            case Cmd.q :
                gs = Q.execute(gs,true,graphicsStates,current);
                break;
                
            case Cmd.Q :
                gs = Q.execute(gs, false,graphicsStates,current);
                break;
                
            case Cmd.gs :
                if(!getSamplingOnly){
                    final PdfObject GS=(PdfObject) cache.GraphicsStates.get(parser.generateOpAsString(0, true));
                    
                    currentPdfFile.checkResolved(GS);
                    
                    gs.setMode(GS);
                    
                    final int blendMode=gs.getBMValue();
                    
                    current.setGraphicsState(GraphicsState.FILL,gs.getAlpha(GraphicsState.FILL),blendMode);
                    current.setGraphicsState(GraphicsState.STROKE,gs.getAlpha(GraphicsState.STROKE),blendMode);
                    
                    currentBlendMode = blendMode;
                }
                
                break;
                
        }
        
    }
    
    private void processShapeCommands(final CommandParser parser, final PdfShape currentDrawShape, final int commandID) {
        
        switch(commandID){
            
            case Cmd.B :
                if(!removeRenderImages){
                    final Shape currentShape= B.execute(false, false, gs, formLevel, currentDrawShape, current,parserOptions);
                    //track for user if required
                    if(currentShape!=null){
                        final ShapeTracker customShapeTracker=parserOptions.getCustomShapeTraker();
                        if(customShapeTracker!=null) {
                            customShapeTracker.addShape(tokenNumber, Cmd.B, currentShape, gs.nonstrokeColorSpace.getColor(), gs.strokeColorSpace.getColor());
                        }
                    }
                }
                break;
                
            case Cmd.b :
                if(!removeRenderImages){
                    final Shape currentShape= B.execute(false, true, gs, formLevel, currentDrawShape, current,parserOptions);
                    //track for user if required
                    if(currentShape!=null){
                        final ShapeTracker customShapeTracker=parserOptions.getCustomShapeTraker();
                        if(customShapeTracker!=null) {
                            customShapeTracker.addShape(tokenNumber, Cmd.b, currentShape, gs.nonstrokeColorSpace.getColor(), gs.strokeColorSpace.getColor());
                        }
                    }
                }
                break;
                
            case Cmd.bstar :
                if(!removeRenderImages){
                    final Shape currentShape= B.execute(true, true, gs, formLevel, currentDrawShape, current,parserOptions);
                    //track for user if required
                    if(currentShape!=null){
                        final ShapeTracker customShapeTracker=parserOptions.getCustomShapeTraker();
                        if(customShapeTracker!=null) {
                            customShapeTracker.addShape(tokenNumber, Cmd.bstar, currentShape, gs.nonstrokeColorSpace.getColor(), gs.strokeColorSpace.getColor());
                        }
                    }
                }
                break;
                
            case Cmd.Bstar :
                if(!removeRenderImages){
                    final Shape currentShape= B.execute(true, false, gs, formLevel, currentDrawShape, current,parserOptions);
                    //track for user if required
                    if(currentShape!=null){
                        final ShapeTracker customShapeTracker=parserOptions.getCustomShapeTraker();
                        if(customShapeTracker!=null){
                            customShapeTracker.addShape(tokenNumber, Cmd.Bstar, currentShape,gs.nonstrokeColorSpace.getColor(),gs.strokeColorSpace.getColor());
                        }
                    }
                }
                break;
                
            case Cmd.c :
                final float x3 =parser.parseFloat(1);
                final float y3 = parser.parseFloat(0);
                final float x2 =parser.parseFloat(3);
                final float y2 = parser.parseFloat(2);
                final float x = parser.parseFloat(5);
                final float y = parser.parseFloat(4);
                currentDrawShape.addBezierCurveC(x, y, x2, y2, x3, y3);
                break;
                
            case Cmd.d :
                D.execute(parser, gs);
                break;
                
            case Cmd.F :
                
                if(!removeRenderImages) {
                    F.execute(tokenNumber, false, formLevel, currentDrawShape, gs, cache, currentPdfFile, current, parserOptions, multiplyer);
                }
                break;
                
            case Cmd.f :
                if(!removeRenderImages) {
                    F.execute(tokenNumber, false, formLevel, currentDrawShape, gs, cache, currentPdfFile, current, parserOptions, multiplyer);
                }
                break;
                
            case Cmd.Fstar :
                if(!removeRenderImages) {
                    F.execute(tokenNumber, true, formLevel, currentDrawShape, gs, cache, currentPdfFile, current, parserOptions, multiplyer);
                }
                break;
                
            case Cmd.fstar :
                if(!removeRenderImages) {
                    F.execute(tokenNumber, true, formLevel, currentDrawShape, gs, cache, currentPdfFile, current, parserOptions, multiplyer);
                }
                break;
                
            case Cmd.h :
                currentDrawShape.closeShape();
                break;
                
                //case Cmd.i:
                //  I();
                //break;
                
            case Cmd.J :
                J.execute(false, parser.parseInt(), gs);
                break;
                
            case Cmd.j :
                J.execute(true, parser.parseInt(), gs);
                break;
                
            case Cmd.l :
                currentDrawShape.lineTo(parser.parseFloat(1),parser.parseFloat(0));
                break;
                
            case Cmd.M:
                gs.setMitreLimit((int) (parser.parseFloat(0)));
                break;
                
            case Cmd.m :
                currentDrawShape.setClip(false);
                currentDrawShape.moveTo(parser.parseFloat(1),parser.parseFloat(0));
                break;
                
            case Cmd.n :
                N.execute(currentDrawShape, gs, formLevel, parserOptions.defaultClip,parserOptions, current, pageData);
                break;
                
            case Cmd.re :
                currentDrawShape.appendRectangle(parser.parseFloat(3),parser.parseFloat(2),parser.parseFloat(1),parser.parseFloat(0));
                break;
                
            case Cmd.S :
                if(!removeRenderImages){
                    
                    final Shape currentShape= S.execute(false, gs, currentDrawShape, current, parserOptions);
                    
                    if(currentShape!=null){
                        final ShapeTracker customShapeTracker=parserOptions.getCustomShapeTraker();
                        if(customShapeTracker!=null) {
                            customShapeTracker.addShape(tokenNumber, Cmd.S, currentShape, gs.nonstrokeColorSpace.getColor(), gs.strokeColorSpace.getColor());
                        }
                    }
                    
                }
                break;
            case Cmd.s :
                if(!removeRenderImages){
                    final Shape currentShape= S.execute(true, gs, currentDrawShape, current, parserOptions);
                    //track for user if required
                    if(currentShape!=null){
                        final ShapeTracker customShapeTracker=parserOptions.getCustomShapeTraker();
                        if(customShapeTracker!=null) {
                            customShapeTracker.addShape(tokenNumber, Cmd.s, currentShape, gs.nonstrokeColorSpace.getColor(), gs.strokeColorSpace.getColor());
                        }
                    }
                }
                break;
                
            case Cmd.v :
                currentDrawShape.addBezierCurveV(parser.parseFloat(3),parser.parseFloat(2),parser.parseFloat(1),parser.parseFloat(0));
                break;
                
            case Cmd.w:
                gs.setLineWidth(parser.parseFloat(0));
                break;
                
            case Cmd.Wstar : //set Winding rule
                currentDrawShape.setEVENODDWindingRule();
                currentDrawShape.setClip(true);
                break;
                
            case Cmd.W :
                currentDrawShape.setNONZEROWindingRule();
                currentDrawShape.setClip(true);
                break;
                
            case Cmd.y :
                currentDrawShape.addBezierCurveY(parser.parseFloat(3),parser.parseFloat(2),parser.parseFloat(1),parser.parseFloat(0));
                break;
        }
    }

    /**
     return boolean flags with appropriate ket
     */
    public boolean getBooleanValue(final int key) {
        
        switch(key){
            
            case ValueTypes.EmbeddedFonts:
                return pdfFontFactory.hasEmbeddedFonts();
                
                //
            
            case DecodeStatus.PageDecodingSuccessful:
                return errorTracker.ispageSuccessful();
                
            case DecodeStatus.NonEmbeddedCIDFonts:
                return pdfFontFactory.hasNonEmbeddedCIDFonts();
                
            case DecodeStatus.ImagesProcessed:
                return parserOptions.imagesProcessedFully;
                
            case DecodeStatus.TooManyShapes:
                return parserOptions.tooManyShapes;
                
            case DecodeStatus.YCCKImages:
                return parserOptions.hasYCCKimages;
                
            case DecodeStatus.TTHintingRequired:
                return isTTHintingRequired;
                
            default:
                throw new RuntimeException("Unknown value "+key);
        }
    }
    
    public void dispose() {
        
        if(pdfData!=null) {
            this.pdfData.dispose();
        }
        
        //this.pageLines=null;
        
    }
    
    public void setIntValue(final int key, final int value) {
        
        switch(key){
            
            /**
             * currentPage number
             */
            case ValueTypes.PageNum:
                parserOptions.setPageNumber(value);
                break;
                
                /**
                 * tells program to try and use Java's font printing if possible
                 * as work around for issue with PCL printing
                 */
            case ValueTypes.TextPrint:
                parserOptions.setTextPrint(value);
                break;
        }
    }
    
    public void setXMLExtraction( final boolean isXMLExtraction){
        this.isXMLExtraction=isXMLExtraction;
    }
    
    public void setParameters(final boolean isPageContent, final boolean renderPage, final int renderMode, final int extractionMode, final boolean isPrinting, final boolean useJavaFX) {
        
        parserOptions.init(isPageContent, renderPage, renderMode, extractionMode, isPrinting, useJavaFX);
        
        /**
         * flags
         */
        
        renderText=renderPage &&(renderMode & PdfDecoderInt.RENDERTEXT) == PdfDecoderInt.RENDERTEXT;
        
        textExtracted=(extractionMode & PdfDecoderInt.TEXT)==PdfDecoderInt.TEXT;
        
        textColorExtracted=(extractionMode & PdfDecoderInt.TEXTCOLOR) == PdfDecoderInt.TEXTCOLOR;
        
        removeRenderImages=renderPage &&(renderMode & PdfDecoderInt.REMOVE_RENDERSHAPES )== PdfDecoderInt.REMOVE_RENDERSHAPES;
        
    }

    public void setFormLevel(final int value) {
        
        formLevel=value;
    }
    
    /**
     * process each token and add to text or decode
     * if not known command, place in array (may be operand which is
     * later used by command)
     */
    private int processTextToken(final Tj textDecoder, final CommandParser parser, final int commandID, int startCommand, final int dataPointer)
    {
        
        textDecoder.setGS(gs);
        
        final TextState currentTextState=gs.getTextState();
        
        if(commandID ==Cmd.BT && parserOptions.isRenderPage()){
            //save for later and set TR
            current.drawClip(gs,parserOptions.defaultClip,true) ;
            current.drawTR(GraphicsState.FILL);
            
        }
        
        if(commandID ==Cmd.Tj || commandID ==Cmd.TJ || commandID ==Cmd.quote || commandID ==Cmd.doubleQuote){
            
            //flag which TJ command we are on
            current.flagCommand(Cmd.Tj,tokenNumber);
            
            if(currentTextState.hasFontChanged() && currentTextState.getTfs()!=0){ //avoid text which does not appear as zero size
                
                //switch to correct font
                final String fontID=currentTextState.getFontID();
                final PdfFont restoredFont = FontResolver.resolveFont(gs,this, fontID,pdfFontFactory,cache);
                if(restoredFont!=null){
                    currentFontData=restoredFont;
                    current.drawFontBounds(currentFontData.getBoundingBox());
                }
            }
            
            if(currentFontData==null){
                currentFontData=new PdfFont(currentPdfFile);
                
                //use name for poss mappings (ie Helv)
                currentFontData.getGlyphData().logicalfontName=StandardFonts.expandName(currentTextState.getFontID());
            }
            
            if(currentTextState.hasFontChanged()){
                currentTextState.setFontChanged(false);
            }
        }
        
        switch(commandID){
            
            case Cmd.BMC :
                parserOptions.setLayerLevel(parserOptions.getLayerLevel()+1);
              
                //flag so we can next values
                if(parserOptions.isLayerVisible()) {
                    parserOptions.getLayerVisibility().add(parserOptions.getLayerLevel());
                }
                
                //
                break;
                
            case Cmd.BDC :
                
                final PdfObject BDCobj=BDC.execute(startCommand, dataPointer, parser.getStream(),
                        parser.generateOpAsString(0, false), gs, currentPdfFile, current,parserOptions);
                
                //work around for unbalanced clip
                if(BDCobj.getClip()!=null) {
                    BDCDepth = graphicsStates.getDepth();
                } else {
                    BDCDepth = -1;
                }
                
                //track setting and use in preference for text extraction
                textDecoder.setActualText(BDCobj.getTextStreamValue(PdfDictionary.ActualText));
                
                //
                break;
                
            case Cmd.BT :
                currentTextState.resetTm();
                break;
                
            case Cmd.EMC :
                textDecoder.setActualText(null);
                //
                
                //balance stack inside tagged commands
                if(parserOptions.getLayerLevel()==1 && BDCDepth!=-1 && BDCDepth!=graphicsStates.getDepth()){
                    graphicsStates.correctDepth(0, gs, current);
                }
                BDCDepth=-1;
                
                EMC.execute(current, gs, parserOptions);
                break;
                
            case Cmd.ET :
                current.resetOnColorspaceChange();

                if(gs.getTextRenderType()==GraphicsState.CLIPTEXT){
                    current.drawClip(gs,null,false);
                }

                break;
                
            case Cmd.DP :
                //
                break;

            case Cmd.Tf :
                currentTextState.TF(parser.parseFloat(0),(parser.generateOpAsString(1, true)));
                break;
                
            case Cmd.Tc :
                currentTextState.setCharacterSpacing(parser.parseFloat(0));
                break;
                
            case Cmd.TD :
                TD.execute(false, parser.parseFloat(1), parser.parseFloat(0), currentTextState);
                multipleTJs=false;
                break;
                
            case Cmd.Td :
                TD.execute(true, parser.parseFloat(1), parser.parseFloat(0), currentTextState);
                multipleTJs=false;
                break;
                
            case Cmd.Tj :
                
                if(currentTextState.getTfs()!=0){ //avoid zero size text
                    lastTextValue=textDecoder.TJ(currentTextState, currentFontData, parser.getStream(), startCommand, dataPointer,multipleTJs);
                }
                multipleTJs=true; //flag will be reset by Td/Tj/T* if move takes place.
                
                break;
                
            case Cmd.TJ :
                lastTextValue=textDecoder.TJ(currentTextState,currentFontData, parser.getStream(), startCommand, dataPointer,multipleTJs);
                multipleTJs=true; //flag will be reset by Td/Tj/T* if move takes place.
                
                break;
                
            case Cmd.quote :
                TD.relativeMove(0, -currentTextState.getLeading(), currentTextState);
                multipleTJs=false;
                lastTextValue=textDecoder.TJ(currentTextState,currentFontData, parser.getStream(), startCommand, dataPointer,multipleTJs);
                multipleTJs=true; //flag will be reset by Td/Tj/T* if move takes place.
                
                break;
                
            case Cmd.doubleQuote :
                final byte[] characterStream = parser.getStream();
                
                currentTextState.setCharacterSpacing(parser.parseFloat(1));
                currentTextState.setWordSpacing(parser.parseFloat(2));
                
                TD.relativeMove(0, -currentTextState.getLeading(), currentTextState);
                
                multipleTJs=false;
                
                //we can have values which are not accounted for before stream so rollon so we ignore
                while(characterStream[startCommand]!='(' && characterStream[startCommand]!='<' && characterStream[startCommand]!='['){
                    startCommand++;
                }
                
                lastTextValue=textDecoder.TJ(currentTextState,currentFontData, characterStream, startCommand, dataPointer,multipleTJs);
                multipleTJs=true; //flag will be reset by Td/Tj/T* if move takes place.
                
                break;
                
            case Cmd.Tm :
                //set Tm matrix
                currentTextState.Tm[0][0] =parser.parseFloat(5);
                currentTextState.Tm[0][1] =parser.parseFloat(4);
                currentTextState.Tm[0][2] = 0;
                currentTextState.Tm[1][0] =parser.parseFloat(3);
                currentTextState.Tm[1][1] =parser.parseFloat(2);
                currentTextState.Tm[1][2] = 0;
                currentTextState.Tm[2][0] =parser.parseFloat(1);
                currentTextState.Tm[2][1] =parser.parseFloat(0);
                currentTextState.Tm[2][2] = 1;
                
                //keep position in case we need
                currentTextState.setTMAtLineStart();
                multipleTJs=false;
                break;
                
            case Cmd.Tstar :
                TD.relativeMove(0, -currentTextState.getLeading(), currentTextState);
                multipleTJs=false;
                break;
                
            case Cmd.Tr :
                final int value= TR.execute(parser.parseInt(), gs);
                if (parserOptions.isRenderPage() && !parserOptions.renderDirectly()) {
                    current.drawTR(value);
                }
                break;
                
            case Cmd.Ts :
                currentTextState.setTextRise(parser.parseFloat(0));
                break;
                
            case Cmd.Tw :
                currentTextState.setWordSpacing(parser.parseFloat(0));
                break;
                
            case Cmd.Tz :
                currentTextState.setHorizontalScaling(parser.parseFloat(0)/ 100);
                break;
                
            case Cmd.TL :
                currentTextState.setLeading(parser.parseFloat(0));
                break;
        }
        return dataPointer;
    }
    
    public PdfObjectCache getObjectCache(){
        return cache;
    }
    
    /**
     * pass in BBox so we can work out if we ignore scaling
     * @param BBox
     */
    public void setBBox(final float[] BBox) {
        this.BBox=BBox;
    }
    
    public int getBlendMode(){
        return currentBlendMode;
    }
}
