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
 * PdfDecoderFX.java
 * ---------------
 */

package org.jpedal;

import org.jpedal.external.FXExternalHandlers;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javax.print.attribute.SetOfIntegerSyntax;
import javax.swing.*;
import javax.swing.border.Border;
import org.jpedal.constants.JPedalSettings;
import org.jpedal.constants.PDFflags;
import org.jpedal.constants.SpecialOptions;
import org.jpedal.display.*;
import org.jpedal.display.javafx.*;

import org.jpedal.examples.viewer.commands.javafx.JavaFXPreferences;
import org.jpedal.examples.viewer.gui.*;
import org.jpedal.examples.viewer.gui.javafx.JavaFXMouseFunctionality;
import org.jpedal.examples.viewer.gui.javafx.JavaFXMouseListener;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.external.Options;
import org.jpedal.external.PluginHandler;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.gui.*;
import org.jpedal.io.*;
import org.jpedal.objects.*;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.acroforms.actions.*;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.outlines.OutlineData;
import org.jpedal.objects.raw.PdfObject;
//
import org.jpedal.parser.DecoderOptions;
import org.jpedal.parser.DecoderResults;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.FXDisplay;
import org.jpedal.text.TextLines;
import org.jpedal.utils.DPIFactory;
import org.jpedal.utils.LogWriter;
import org.w3c.dom.Document;

/**
 * Provides an object to decode pdf files and provide a rasterizer if required -
 * Normal usage is to create instance of PdfDecoder and access via public
 * methods. Examples showing usage in org.jpedal.examples
 * <br>
 *  We recommend you access JPedal using only public methods listed in API
 */
public class PdfDecoderFX extends Pane implements Printable, Pageable, PdfDecoderInt {
    
    ///
    
    private Image previewImage;

    private String previewText;
    
    private int curThumbPage = 1;
    
    /**
     * shared values
     */
    private final DecoderOptions options=new DecoderOptions();
    
    //public SwingPrinter swingPrinter = new SwingPrinter();
    
    private final ExternalHandlers externalHandlers=new FXExternalHandlers(GUIModes.JAVAFX);
    
    private final PdfResources res=new PdfResources();
    
    final FileAccess fileAccess =new FileAccess(externalHandlers,res,options);
    
    private final DecoderResults resultsFromDecode=new DecoderResults();
    
    final Parser parser=new Parser(externalHandlers,options, fileAccess,res,resultsFromDecode);
    
    private final DPIFactory scalingdpi=new DPIFactory();
    
    //Darker background, glowing pages
    public boolean useNewGraphicsMode = true;
    
    
    public Display pages;
    
    private boolean isBorderPresent = true;
    
    private final DisplayOffsets displayOffsets=new DisplayOffsets();
    
    private ActionHandler formsActionHandler,userActionHandler;
   
    /**amount we scroll screen to make visible*/
    private int scrollInterval=10;
    
    /** when true setPageParameters draws the page rotated for use with scale to window */
    private boolean isNewRotationSet;
    
    /** used by setPageParameters to draw rotated pages */
    int displayRotation;
    
    /**width of the BufferedImage in pixels*/
    int x_size = 100;
    
    /**height of the BufferedImage in pixels*/
    int y_size = 100;
    
    /**unscaled page width and height*/
    int max_x,max_y;
    
    /**any scaling factor being used to convert co-ords into correct values and to alter image size */
    float scaling=1;
    
    /**border for component*/
    protected Border myBorder;
    
    private javafx.scene.shape.Rectangle imageHighlighter;

    /**
     * interactive status Bar
     */
    private StatusBar statusBar;
    
    /**
     * see if file open - may not be open if user interrupted open or problem encountered
     */
    @Override
    public boolean isOpen() {
        return fileAccess.isOpen();
    }
    
    Canvas previewThumbnail;
    /**
     * put a little thumbnail of page on display for user in viewer as he scrolls through
     * @param g2
     * @param visibleRect
     */
    private void drawPreviewImage(final Graphics2D g2, final Rectangle visibleRect) {

        if(previewImage!=null){
            GraphicsContext context = previewThumbnail.getGraphicsContext2D();
            
            context.setFill(new javafx.scene.paint.Color(0.25, 0.25, 0.25, 1));
            context.fillRect(0, 0, previewThumbnail.getWidth(), previewThumbnail.getHeight());
            
            context.drawImage(previewImage, 10, 10);
            context.setStroke(new javafx.scene.paint.Color(1.0, 1.0, 1.0, 1.0));
            context.strokeText(previewText, 10, (previewThumbnail.getHeight())-10);
            ScrollPane pane = (ScrollPane) (getParent().getParent().getParent().getParent());
            Group group = (Group)getParent();
            double groupX = group.getBoundsInParent().getMinX();
            double viewW = pane.getViewportBounds().getWidth();
            double viewH = pane.getViewportBounds().getHeight();
            double pdfW = getBoundsInParent().getWidth();
            double thumW = previewThumbnail.getBoundsInLocal().getWidth();
            double thumH = previewThumbnail.getBoundsInLocal().getHeight();
            double x = pdfW+(viewW-groupX-pdfW-thumW-10); //use 10 for left padding fix;
            double y = (viewH-thumH)/ getPageCount()* (curThumbPage-1);
            previewThumbnail.setLayoutX(x);
            previewThumbnail.setLayoutY(y);
        }
    }
    
    //
    
    @Override
    public ExternalHandlers getExternalHandler() {
        return externalHandlers;
    }
    
    protected PdfResources getRes() {
        return res;
    }
    
    /**
     * current page rotation (in addition to rotation in file) in degrees
     * So if user turns page by 90 degrees, value will be 90
     * @return
     */
    @Override
    public int getDisplayRotation() {
        return displayRotation;
    }
    
    DecoderOptions getOptions() {
        return options;
    }
    
    /**
     * current logical page number
     * @return
     */
    @Override
    public int getPageNumber() {
        return fileAccess.getPageNumber();
    }
    
    public void setPageNumber(final int newPage) {
        fileAccess.setPageNumber(newPage);
    }
    
    @Override
    public void setDisplayRotation(final int newRotation) {
        this.displayRotation=newRotation;
    }
    
    @Override
    public Display getPages() {
        return pages;
    }
    
    /**
     * return page number for last page decoded (only use in SingleDisplay mode)
     */
    @Override
    public int getlastPageDecoded() {
        return fileAccess.getLastPageDecoded();
    }
    
    /**
     * return details on page for type (defined in org.jpedal.constants.PageInfo) or null if no values
     * Unrecognised key will throw a RunTime exception
     *
     * null returned if JPedal not clear on result
     */
    @Override
    public Iterator getPageInfo(final int type) {
        return resultsFromDecode.getPageInfo(type);
    }
    
    /**
     * provide direct access to outlineData object
     * @return  OutlineData
     */
    @Override
    public OutlineData getOutlineData() {
        return res.getOutlineData();
    }
    
    /**
     * track if file still loaded in background
     * @return
     */
    @Override
    public boolean isLoadingLinearizedPDF() {
        return (fileAccess.linearParser.linearizedBackgroundReaderer!=null && fileAccess.linearParser.linearizedBackgroundReaderer.isAlive());
    }
    
    @Override
    public boolean useNewGraphicsMode() {
        return useNewGraphicsMode;
    }
    
    @Override
    public void useNewGraphicsMode(final boolean b) {
        useNewGraphicsMode = b;
    }
    
    @Override
    public int getSpecialMode() {
        return specialMode;
    }
    
    public void scrollRectToVisible(Rectangle rectangle) {
        
        final ScrollPane customFXHandle= ((JavaFxGUI)getExternalHandler(Options.MultiPageUpdate)).getPageContainer();
        
        //final int ch = (int)(pageData.getCropBoxHeight(commonValues.getCurrentPage())*scaling);
		//	final int cw = (int)(pageData.getCropBoxWidth(commonValues.getCurrentPage())*scaling);

        double width=this.getWidth();
        double height=this.getHeight();
        
        customFXHandle.setVvalue((rectangle.y)/height);
        
        //<end-demo>
        
        // customFXHandle.setHmax(500);
        customFXHandle.setHvalue(rectangle.x/width);
        
    }
   
    /**
     * work out machine type so we can call OS X code to get around Java bugs.
     */
    static {
        
        /**
         * see if mac
         */
        
            final String name = System.getProperty("os.name");
            if (name.equals("Mac OS X")) {
                DecoderOptions.isRunningOnMac = true;
            } else if (name.startsWith("Windows")) {
                DecoderOptions.isRunningOnWindows = true;
            }else if (name.startsWith("AIX")) {
                DecoderOptions.isRunningOnAIX = true;
            } else {
                if (name.equals("Linux")) {
                    DecoderOptions.isRunningOnLinux = true;
                }
            }
//        } catch (final Exception e) {
//            //tell user and log
//            if(LogWriter.isOutput()) {
//                LogWriter.writeLog("Exception: " + e.getMessage());
//            }
//            //
//        }
        
        /**
         * get version number so we can avoid bugs in various versions
         */
        try{
            DecoderOptions.javaVersion=Float.parseFloat(System.getProperty("java.specification.version"));
        }catch(final NumberFormatException e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        // <start-demo><end-demo>
        
    }
    
    /**
     * NOT PART OF API
     * turns off the viewable area, scaling the page back to original scaling
     */
    @Override
    public void resetViewableArea() {
        
        //<start-demo><end-demo>
        
        //swingPainter.resetViewableArea(this, fileAccess.getPdfPageData());
        
    }


    /**
     * Deprecated on 07/07/2014
     * please use getPages().setViewableArea instead.
     * @deprecated
     */
    @Override
    public AffineTransform setViewableArea(final Rectangle viewport) throws PdfException {
        
        //<start-demo><end-demo>
        
        return null;
        //return swingPainter.setViewableArea(viewport, this, fileAccess.getPdfPageData());
        
    }
    
    /**
     * return type of alignment for pages if smaller than panel
     * - see options in Display class.
     */
    @Override
    public int getPageAlignment() {
        return options.getPageAlignment();
    }
    
    //
    
    protected int specialMode= SpecialOptions.NONE;
    
    /**
     * Recommend way to create a PdfDecoder for renderer only viewer (not
     * recommended for server extraction only processes)
     */
    public PdfDecoderFX(final int specialMode, final UIViewerInt viewer) {
        
        //swingPrinter.uiViewer = viewer;
        
        this.specialMode=specialMode;
        
        pages = new SingleDisplayFX(this,options);
        
        options.setRenderPage(true);
        //@swing
//        setLayout(null);
        
        //once only setup for fonts (dispose sets flag to false just incase)
        if(!FontMappings.fontsInitialised){
            FontMappings.initFonts();
            FontMappings.fontsInitialised=true;
        }
        setId("PdfDecoderFX");
    }
    
    /**
     * NOT PART OF API
     *
     * Recommend you use Viewer class if you want this functionality
     * @param pageDisplayMode
     */
    public void setPageDisplayMode(final int pageDisplayMode) {
        specialMode=pageDisplayMode;
    }
    
    @SuppressWarnings("UnusedDeclaration")
    public static UIViewerInt getUIViewer(){
//        return swingPrinter.uiViewer;
        return null;
    }
    
    /**
     * Recommend way to create a PdfDecoder for renderer only viewer (not
     * recommended for server extraction only processes)
     */
    public PdfDecoderFX() {
        
        pages = new SingleDisplayFX(this,options);
        
        options.setRenderPage(true);
        //@swing
//        setLayout(null);
        
        //once only setup for fonts (dispose sets flag to false just incase)
        if(!FontMappings.fontsInitialised){
            FontMappings.initFonts();
            FontMappings.fontsInitialised=true;
        }
        //@swing
//        setPreferredSize(new Dimension(100, 100));
        setId("PdfDecoderFX");
    }
    
    /**
     * Recommend way to create a PdfDecoder for renderer only viewer (not
     * recommended for server extraction only processes)
     */
    public PdfDecoderFX(final org.jpedal.gui.UIViewerInt uiViewer, final org.jpedal.gui.ViewerInt viewer, final int pageCount) {
        
//        swingPrinter.uiViewer = uiViewer;
//        swingPrinter.viewer = viewer;
        fileAccess.setPageCount(pageCount);
        
//        swingPrinter.isCustomPrinting = true;
        
        pages = new SingleDisplayFX(this,options);
        
        options.setRenderPage(true);
        setId("PdfDecoderFX");
        //@swing
//        setLayout(null);
    }
    
    /**
     * remove all static elements - only do once completely finished with JPedal
     * as will not be reinitialised
     */
    public static void disposeAllStatic() {
        
        StandardFonts.dispose();
        FontMappings.dispose();
        
    }
    
    /**
     * convenience method to remove all items from memory
     * If you wish to clear all static objects as well, you will also need to call
     * disposeAllStatic()
     */
    @Override
    public final void dispose(){
        if (SwingUtilities.isEventDispatchThread()){
            parser.disposeObjects();
        }else {
            final Runnable doPaintComponent = new Runnable() {
                @Override
                public void run() {
                    parser.disposeObjects();
                }
            };
            SwingUtilities.invokeLater(doPaintComponent);
        }
    }
    
    /**
     * convenience method to close the current PDF file and release all resources/delete any temporary files
     */
    @Override
    public final void closePdfFile() {
        
        if(pages!=null) {
            pages.stopGeneratingPage();
        }
        
        pages.disableScreen();
        
        fileAccess.closePdfFile();
        
    }
    
    //
    
    /**
     * Access should not generally be required to
     * this class but used in examples. Returns the PdfData object containing
     * raw content from page
     *
     * @return PdfData object containing text content from PDF
     */
    @Override
    public final PdfData getPdfData() throws PdfException {
        return parser.getPdfData();
    }
    
    
    /**
     * flag to show if PDF document contains an outline
     */
    @Override
    public final boolean hasOutline() {
        return res.hasOutline();
    }
    
    
    /**
     * return a DOM document containing the PDF Outline object as a DOM Document - may return null
     */
    @Override
    public final Document getOutlineAsXML() {
        
        return res.getOutlineAsXML(getIO());
    }
    
    /**
     * Provides method for outside class to get data
     * object containing information on the page for calculating grouping <br>
     * Please note: Structure of PdfPageData is not guaranteed to remain
     * constant. Please contact IDRsolutions for advice.
     *
     * @return PdfPageData object
     */
    @Override
    public final PdfPageData getPdfPageData() {
        return fileAccess.getPdfPageData();
    }
    
   
    /**
     * set page range (inclusive) -
     * If end is less than start it will print them
     * backwards (invalid range will throw PdfException)
     *
     * @throws PdfException
     */
    public void setPagePrintRange(final int start, final int end) throws PdfException {
        
//        swingPrinter.setPagePrintRange(start, end, fileAccess.getPageCount());
        
    }
    
    /**
     * tells program to try and use Java's font printing if possible as work
     * around for issue with PCL printing - values are PdfDecoder.TEXTGLYPHPRINT
     * (use Java to rasterize font if available) PdfDecoder.TEXTSTRINGPRINT(
     * print as text not raster - fastest option) PdfDecoder.NOTEXTPRINT
     * (default - highest quality)
     */
    public void setTextPrint(final int textPrint) {
        this.textPrint = textPrint;
    }
    
    /**
     * flag to use Java's inbuilt font renderer if possible
     */
    public int textPrint;
    
    /**
     * If you are printing PDFs using JPedal in your custom
     * code, you may find pages missing, because JPedal does
     * not know about these additional pages. This method
     * allows you to tell JPedal you have already printed pagesPrinted
     */
    public void useLogicalPrintOffset(final int pagesPrinted){
//        swingPrinter.useLogicalPrintOffset(pagesPrinted);
    }
    
    /**
     * Implements the standard Java printing functionality.
     *
     * @param graphics		the context into which the page is drawn
     * @param pageFormat	the size and orientation of the page being drawn
     * @param page	the zero based index of the page to be drawn
     * @return int	Printable.PAGE_EXISTS or Printable.NO_SUCH_PAGE
     * @throws PrinterException
     */
    @Override
    public int print(final Graphics graphics, final PageFormat pageFormat, final int page) throws PrinterException {
        
//        return swingPrinter.print(graphics,  pageFormat,  page, getIO(), this, externalHandlers,scaling,res,getPageNumber(),options);
        return -1;
    }
    //
    /**
     * generate BufferedImage of a page in current file
     *
     * Page size is defined by CropBox
     * see http://files.idrsolutions.com/samplecode/org/jpedal/examples/images/ConvertPagesToImages.java.html for full details
     */
    @Override
    public BufferedImage getPageAsImage(final int pageIndex) throws PdfException {
        
        return getPageAsImage(pageIndex, false);
        
    }
    //
    /**
     * generate BufferedImage of a page in current file
     */
    private BufferedImage getPageAsImage(final int pageIndex, final boolean imageIsTransparent) throws PdfException {
        
        parser.setParms(displayRotation, scaling, 0, specialMode);
        
        return parser.getPageAsImage(pageIndex,imageIsTransparent);
    }
    //
    
    /**
     * provide method for outside class to clear store of objects once written out to reclaim memory
     *
     * @param reinit lag to show if image data flushed as well
     */
    @Override
    public final void flushObjectValues(final boolean reinit) {
        
        parser.flushObjectValues(reinit);
        
    }
    
    /**
     * provide method for outside class to get data object containing images
     *
     * Please look at examples for usage
     *
     * @return PdfImageData containing image metadata
     */
    @Override
    public final PdfImageData getPdfImageData() {
        return parser.getPdfImageData();
    }
    
    //
    
    /**
     * set render mode to state what is displayed onscreen (ie
     * RENDERTEXT,RENDERIMAGES) - only generally required if you do not wish to
     * show all objects on screen (default is all). Add values together to
     * combine settings.
     */
    @Override
    public final void setRenderMode(final int mode) {
        
        parser.setRenderMode(mode);
        
    }
    
    /**
     * set extraction mode telling JPedal what to extract -
     * (TEXT,RAWIMAGES,FINALIMAGES - add together to combine) - See
     * org.jpedal.examples for specific extraction examples
     */
    @Override
    public final void setExtractionMode(final int mode) {
        
        parser.setExtractionMode(mode);
        
    }
    
    /**
     * allow user to alter certain values in software
     * such as Colour,
     *
     * Please note all Color and text highlighting values are static and common across the JVM
     */
    @Override
    public void modifyNonstaticJPedalParameters(final Map values) throws PdfException {
        options.set(values);
        
        //To ensure the background changes when the value is set
        //we change this Objects background if a values for it has been passed in.
        if(values.containsKey(JPedalSettings.DISPLAY_BACKGROUND)){
            //@swing
//            setBackground(options.getDisplayBackgroundColor());
        }
    }
    
    /**
     * allow user to alter certain values in software such as Colour,
     *
     * If you are using decoder.getPageAsHiRes() after passing additional parameters into JPedal using the static method
     * PdfDecoder.modifyJPedalParameters(), then getPageAsHiRes() wont necessarily be thread safe.  If you want to use
     * getPageAsHiRes() and pass in additional parameters, in a thread safe mannor, please use the method
     * getPageAsHiRes(int pageIndex, Map params) or getPageAsHiRes(int pageIndex, Map params, boolean isTransparent) and
     * pass the additional parameters in directly to the getPageAsHiRes() method without calling PdfDecoder.modifyJPedalParameters()
     * first.
     *
     * Please see http://files.idrsolutions.com/samplecode/org/jpedal/examples/images/ConvertPagesToHiResImages.java.html for example of usage
     *
     * Please note all Color and text highlighting values except page colour are static and common across the JVM
     */
    public static void modifyJPedalParameters(final Map values) throws PdfException {
        
        if(values!=null) {
            DecoderOptions.modifyJPedalParameters(values);
        }
        
    }
    
    
    /**
     * method to return null or object giving access info fields and metadata.
     */
    @Override
    public final PdfFileInformation getFileInformationData() {
        
        return res.getMetaData(getIO());
        
    }
    
    /**
     *
     * Please do not use for general usage. Use setPageParameters(scalingValue, pageNumber) to set page scaling
     */
    @Override
    public final void setExtractionMode(final int mode, final float scaling) {
        
        this.scaling = scaling;
        
        parser.setExtractionMode(mode, scaling);
        
    }
    
    /**
     * return handle on PDFFactory which adjusts display size so matches size in Acrobat
     * @return
     */
    @Override
    public DPIFactory getDPIFactory(){
        return scalingdpi;
    }
    
    /**
     * initialise panel and set size to fit PDF page<br>
     * intializes display with rotation set to the default, specified in the PDF document
     * scaling value of -1 means keep existing setting
     */
    @Override
    public void setPageParameters(float scaling, final int pageNumber) {
        
        fileAccess.setPageNumber(pageNumber);
        parser.resetMultiplyer();
        
        //pick up flag to prevent loop
        if (getDisplayView()==Display.PAGEFLOW && scaling==-100f) {
            return;
        }
        
        //ignore negative value or set
        if(scaling>0) {
            this.scaling = scaling;
        } else {
            scaling = this.scaling;
        }
 
        if(pages!=null) {
            pages.setScaling(scaling);
        }
        
        
        final PdfLayerList layers=res.getPdfLayerList();
        if(layers!=null){
            final boolean layersChanged=layers.setZoom(scalingdpi.removeScaling(scaling));
            
            if(layersChanged){
             //   try {
                    decodePage(-1);
//                } catch (final Exception e) {
//                    //tell user and log
//                    if(LogWriter.isOutput()) {
//                        LogWriter.writeLog("Exception: " + e.getMessage());
//                    }
//                    //
//                }
            }
        }
        
        final PdfPageData pageData= fileAccess.getPdfPageData();
        
        pageData.setScalingValue(scaling); //ensure aligned
        
        final int mediaW = pageData.getMediaBoxWidth(pageNumber);
        max_y = pageData.getMediaBoxHeight(pageNumber);
        max_x = pageData.getMediaBoxWidth(pageNumber);
        
        final int cropW = pageData.getCropBoxWidth(pageNumber);
        final int cropH = pageData.getCropBoxHeight(pageNumber);
        
        this.x_size =(int) ((cropW)*scaling);
        this.y_size =(int) ((cropH)*scaling);
        
        //rotation is broken in viewer without this - you can't alter it
        //can anyone remember why we added this code???
        //it breaks PDFs if the rotation changes between pages
        if(!isNewRotationSet && getDisplayView()!=Display.PAGEFLOW){
            displayRotation = pageData.getRotation(pageNumber);
        }else{
            isNewRotationSet=false;
        }
        
        final DynamicVectorRenderer currentDisplay= fileAccess.getDynamicRenderer();
        
        currentDisplay.init(mediaW,max_y,options.getPageColor());
        
        if(!currentDisplay.isHTMLorSVG()){
            currentDisplay.setValue(DynamicVectorRenderer.ALT_BACKGROUND_COLOR, options.getPageColor().getRGB());
            if(options.getTextColor()!=null){
                currentDisplay.setValue(DynamicVectorRenderer.ALT_FOREGROUND_COLOR, options.getTextColor().getRGB());
                
                if(options.getChangeTextAndLine()) {
                    currentDisplay.setValue(DynamicVectorRenderer.FOREGROUND_INCLUDE_LINEART, 1);
                } else {
                    currentDisplay.setValue(DynamicVectorRenderer.FOREGROUND_INCLUDE_LINEART, 0);
                }
            }
        }
        
        /**update the AffineTransform using the current rotation*/
        pages.setPageRotation(displayRotation);
        
        
        if(GUI.debugFX){
            System.out.println("externalHandlers.getExternalHandler(Options.SwingContainer) in PdfDecoderFX.java not implemented yet for JavaFX");
        }
//        Object swingGUI=externalHandlers.getExternalHandler(Options.SwingContainer);
//        if(swingGUI!=null){
//            int index = ((SwingGUI)swingGUI).getCombo( org.jpedal.examples.viewer.Commands.SCALING).getSelectedIndex();
//            if(index<3){
//                ((SwingGUI) swingGUI).getCommand().getViewStack().add(pageNumber, null, index - 3);
//            }
//        }
        
        
        //refresh forms in case any effected
        final AcroRenderer formRenderer=externalHandlers.getFormRenderer();
        if(formRenderer!=null) {
            formRenderer.getCompData().setForceRedraw(true);
        }
        
    }
    
    /** calls setPageParameters(scaling,pageNumber) after setting rotation to draw page */
    @Override
    public void setPageParameters(final float scaling, final int pageNumber, final int newRotation) {
        
        isNewRotationSet=true;
        displayRotation=newRotation;
        if (getDisplayView() == Display.PAGEFLOW) {
            pages.init(0, displayRotation, 0, null, false);
        } else {
            setPageParameters(scaling, pageNumber);
        }
    }
    
    /**
     * Not part of API - used internally
     *
     * set status bar to use when decoding a page - StatusBar provides a GUI
     * object to display progress and messages.
     */
    @Override
    public void setStatusBarObject(final StatusBar statusBar) {
        this.statusBar = statusBar;
    }
    
    /**
     * wait for decoding to finish
     */
    @Override
    public void waitForDecodingToFinish() {
        
        fileAccess.waitForDecodingToFinish();
        
    }
    
    /**
     * Not part of API - used internally
     *
     * gets DynamicVector Object
     */
    @Override
    public DynamicVectorRenderer getDynamicRenderer() {
        return fileAccess.getDynamicRenderer();
    }
    
    /**
     * Not part of API - used internally
     *
     * gets DynamicVector Object - NOT PART OF API and subject to change (DO NOT USE)
     */
    @Override
    public DynamicVectorRenderer getDynamicRenderer(final boolean reset) {
        
        return fileAccess.getDynamicRenderer(reset);
        
    }
    
    /**
     * Override setCursor so that we can turn on and off
     */
    public static void setPDFCursor(final Cursor c){
        //<start-demo><end-demo>
        
        if(SingleDisplayFX.allowChangeCursor){
            //@swing
//            this.setCursor(c);
        }
    }
    
    //All Pdf decoder to remember the default for current settings
    //private Cursor defaultCursor = null;
    
    /**
     * When changing the mouse mode we call this method to set the mouse mode default cursor
     * @param c :: The cursor to set as the default
     */
    public void setDefaultCursor(final Cursor c){
        if(SingleDisplayFX.allowChangeCursor){
            setCursor(c);
        }
    }
    
    //When default option is used, use the set default not the component default
    //@swing
    /**
    @Override
    public void setCursor(Cursor c){
        if(GUIDisplay.allowChangeCursor){
            if(c.getType()==Cursor.DEFAULT_CURSOR && defaultCursor!=null){
                super.setCursor(defaultCursor);
            }else{
                super.setCursor(c);
            }
        }
    }
    /**/ 
    /**
     * decode a page, - <b>page</b> must be between 1 and
     * <b>PdfDecoder.getPageCount()</b> - Will kill off if already running
     *
     * returns minus page if trying to open linearized page not yet available
     */
    @Override
    public final void decodePage(final int rawPage)  {
        
        final boolean isPageAvailable=isPageAvailable(rawPage);
        final PdfObject pdfObject= fileAccess.linearParser.getLinearPageObject();
        
        // Used to redraw forms after layers have been changed
        final boolean hasLayersChanged;
        if(res.getPdfLayerList() != null){
            hasLayersChanged=res.getPdfLayerList().getChangesMade();
        }else{
            hasLayersChanged=false;
        }
        
        /**
         * if linearized and PdfObject then setup
         */
        if(!isPageAvailable){
            return;
        }else if(isPageAvailable && pdfObject!=null){
            fileAccess.readAllPageReferences(true, pdfObject, new HashMap(1000), new HashMap(1000),rawPage, getFormRenderer(),res, options.getInsetW(), options.getInsetH());
        }
        
        parser.setStatusBar(statusBar);
        
        parser.setParms(displayRotation,scaling,(int)pages.getIndent(),specialMode);
        
        pages.setCursorBoxOnScreen(null,rawPage== fileAccess.getLastPageDecoded());
        
        final DynamicVectorRenderer currentDisplay=new FXDisplay(rawPage,getObjectStore(), false);
        
        fileAccess.setDVR(currentDisplay);
        
        parser.decodePage(rawPage);
        
        if(hasLayersChanged){
            // Form objects get drawn over so redraw them if the layers have been changed
            externalHandlers.getFormRenderer().getCompData().resetScaledLocation(pages.getOldScaling(), displayRotation, 0);
        }
        
        if(Platform.isFxApplicationThread()){
        //    try{
                
                /**
                 * Updated variables for highlighting on page decode.
                 */
                color = DecoderOptions.highlightColor.getRGB();
                opacity = DecoderOptions.highlightComposite;
                highlightsPane.getChildren().clear();
                
                pages.init(scaling, displayRotation, rawPage, currentDisplay, true);
                
                pages.refreshDisplay();
                
        }else{
            //Ensure dialog is handled on FX thread
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    
                    /**
                     * Updated variables for highlighting on page decode.
                     */
                    color = DecoderOptions.highlightColor.getRGB();
                    opacity = DecoderOptions.highlightComposite;
                    highlightsPane.getChildren().clear();
                    
                    pages.init(scaling, displayRotation, rawPage, currentDisplay, true);
                    
                    pages.refreshDisplay();
                }
            });
        }   
    }
    
    
    /**
     * see if page available if in Linearized mode or return true
     * @param rawPage
     * @return
     */
    @Override
    public synchronized boolean isPageAvailable(final int rawPage) {
        
        return parser.isPageAvailable(rawPage);
        
    }
    
    //
    /**
     * allow user to add grapical content on top of page - for display ONLY
     * Additional calls will overwrite current settings on page
     * ONLY works in SINGLE VIEW displaymode
     */
    @Override
    public void drawAdditionalObjectsOverPage(final int page, final int[] type, final Color[] colors, final Object[] obj) throws PdfException {
        
        
        if (page == getPageNumber()) {
            
            FXAdditionalData additionaValuesforPage=(FXAdditionalData) externalHandlers.getExternalHandler(Options.JavaFX_ADDITIONAL_OBJECTS);
            if(additionaValuesforPage!=null){ //transition so store data to pickup later
                additionaValuesforPage.setType(type);
                additionaValuesforPage.setObj(obj);
                
            }else{
                final DynamicVectorRenderer currentDisplay= fileAccess.getDynamicRenderer();
                
                //add to screen display
                currentDisplay.drawAdditionalObjectsOverPage(type, colors, obj);
                
                //ensure redraw
                pages.refreshDisplay();
            }
        }
    }
    
    /**
     * allow user to remove all additional graphical content from the page (only for display)
     * ONLY works in SINGLE VIEW displaymode
     */
    @Override
    public void flushAdditionalObjectsOnPage(final int page) throws PdfException {
        
        //remove content by redrawing page
        if (page == getPageNumber()){
            fileAccess.setLastPageDecoded(-page); //stop decode thinking it is duplicat request
            decodePage(page);
        }
    }
    
    /**
     * uses hires images to create a higher quality display - downside is it is
     * slower and uses more memory (default is false).- Does nothing in OS
     * version
     *
     */
    @Override
    public boolean isHiResScreenDisplay() {
        
        return options.useHiResImageForDisplay();
        
    }
    
    /**
     * uses hires images to create a higher quality display - downside is it is
     * slower and uses more memory (default is false).- Does nothing in OS
     * version
     *
     * @param value
     */
    @Override
    public void useHiResScreenDisplay(final boolean value) {
        options.useHiResImageForDisplay(value);
    }
    

    /**
     * decode a page as a background thread (use other background methods to access data)
     *
     *  we now recommend you use decodePage as this has been heavily optimised for speed
     */
    @Override
    public final synchronized void decodePageInBackground(final int i) throws Exception {
        
        parser.decodePageInBackground(i);
    }

    
    /**
     * get page count of current PDF file
     */
    @Override
    public final int getPageCount() {
        return fileAccess.getPageCount();
    }
    
    /**
     * return true if the current pdf file is encrypted <br>
     * check <b>isFileViewable()</b>,<br>
     * <br>
     * if file is encrypted and not viewable - a user specified password is
     * needed.
     */
    @Override
    public final boolean isEncrypted() {
        
        return fileAccess.isEncrypted();
    }
    
    /**
     * show if encryption password has been supplied or set a certificate
     */
    @Override
    public final boolean isPasswordSupplied() {
        
        return fileAccess.isPasswordSupplied(getIO());
        
    }
    
    /**
     * show if encrypted file can be viewed,<br>
     * if false a password needs entering
     */
    @Override
    public boolean isFileViewable() {
        
        return fileAccess.isFileViewable(getIO());
        
    }
    
    /**
     * show if content can be extracted
     */
    @Override
    public boolean isExtractionAllowed() {
        
        if (getIO() != null){
            
            final PdfFileReader objectReader=getIO().getObjectReader();
            
            final DecryptionFactory decryption=objectReader.getDecryptionObject();
            return decryption==null || decryption.getBooleanValue(PDFflags.IS_EXTRACTION_ALLOWED);
            
        }else {
            return false;
        }
    }
    
    /**
     * set a password for encryption - software will resolve if user or owner
     * password- calls verifyAccess() from 2.74 so no separate call needed
     */
    @Override
    public final void setEncryptionPassword(final String password) throws PdfException {
        
        if (getIO() == null) {
            throw new PdfException("Must open PdfDecoder file first");
        }
        
        getIO().getObjectReader().setPassword(password);
        
        if (getIO() != null) {
            try {
                preOpen();
                
                fileAccess.openPdfFile();
                
                postOpen();
                
            } catch (final PdfException e) {
                //
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " opening file");
                }
            }
        }
    }
    
    /**
     * routine to open a byte stream containing the PDF file and extract key info
     * from pdf file so we can decode any pages. Does not actually decode the
     * pages themselves - By default files over 16384 bytes are cached to disk
     * but this can be altered by setting PdfFileReader.alwaysCacheInMemory to a maximimum size or -1 (always keep in memory)
     *
     */
    @Override
    public final void openPdfArray(final byte[] data, final String password) throws PdfException {
        
        if(data==null) {
            throw new RuntimeException("Attempting to open null byte stream");
        }
        
        preOpen();
        
        if(fileAccess.isOpen)
            //throw new RuntimeException("Previous file not closed");
        {
            closePdfFile(); //also checks decoding done
        }
        
        fileAccess.openPdfArray(data,password);
        
        postOpen();
        
    }
    
    /**
     * routine to open a byte stream containing the PDF file and extract key info
     * from pdf file so we can decode any pages. Does not actually decode the
     * pages themselves - By default files over 16384 bytes are cached to disk
     * but this can be altered by setting PdfFileReader.alwaysCacheInMemory to a maximimum size or -1 (always keep in memory)
     *
     */
    @Override
    public final void openPdfArray(final byte[] data) throws PdfException {
        
        if(data==null) {
            throw new RuntimeException("Attempting to open null byte stream");
        }
        
        preOpen();
        
        if(fileAccess.isOpen)
            //throw new RuntimeException("Previous file not closed");
        {
            closePdfFile(); //also checks decoding done
        }
        
        fileAccess.openPdfArray(data);
        
        postOpen();
        
    }

    /**
     * allow user to open file using Certificate and key
     * @param filename
     * @param certificate
     * @param key
     */
    @Override
    public void openPdfFile(final String filename, final Certificate certificate, final PrivateKey key) throws PdfException{
        
        /**
         * set values and then call generic open
         */
        fileAccess.setUserEncryption(certificate, key);
        
        openPdfFile(filename);
    }

    /**
     * routine to open PDF file and extract key info from pdf file so we can
     * decode any pages which also sets password.
     * Does not actually decode the pages themselves. Also
     * reads the form data. You must explicitly close your stream!!
     */
    @Override
    public final void openPdfFileFromStream(final Object filename, final String password) throws PdfException {
        
        preOpen();
        
        fileAccess.openPdfFileFromStream(filename, password);
        
        postOpen();
    }

    /**
     * routine to open PDF file and extract key info from pdf file so we can
     * decode any pages. Does not actually decode the pages themselves. Also
     * reads the form data. You must explicitly close any open files with
     * closePdfFile() to Java will not release all the memory
     */
    @Override
    public final void openPdfFile(final String filename) throws PdfException {
        
        if(fileAccess.isOpen && fileAccess.linearParser.linearizedBackgroundReaderer==null) {
            closePdfFile(); //also checks decoding done
        }
        
        preOpen();
        
        fileAccess.openPdfFile(filename);
        
        postOpen();
    }
    
    /**
     * routine to open PDF file and extract key info from pdf file so we can
     * decode any pages which also sets password.
     * Does not actually decode the pages themselves. Also
     * reads the form data. You must explicitly close any open files with
     * closePdfFile() or Java will not release all the memory
     */
    @Override
    public final void openPdfFile(final String filename, final String password) throws PdfException {
        
        if(fileAccess.isOpen) {
            closePdfFile(); //also checks decoding done
        }
        
        preOpen();
        
        fileAccess.openPdfFile(filename, password);
        
        postOpen();
    }
    
    /**
     * routine to open PDF file via URL and extract key info from pdf file so we
     * can decode any pages - Does not actually decode the pages themselves -
     * Also reads the form data - Based on an idea by Peter Jacobsen
     * <br>
     * You must explicitly close any open files with closePdfFile() so Java will
     * release all the memory
     * <br>
     *
     * If boolean supportLinearized is true, method will return with true value once Linearized part read
     */
    @SuppressWarnings("UnusedReturnValue")
    @Override
    public final boolean openPdfFileFromURL(final String pdfUrl, final boolean supportLinearized) throws PdfException {
        
        preOpen();
        
        InputStream is=null;
        
        String rawFileName = null;
        
        try{
            final URL url;
            url = new URL(pdfUrl);
            rawFileName = url.getPath().substring(url.getPath().lastIndexOf('/')+1);
            
            is = url.openStream();
        }catch(final IOException e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        final boolean flag= fileAccess.readFile(supportLinearized, is, rawFileName, null);
        
        postOpen();
        
        return flag;
    }
    
    /**
     * routine to open PDF file via URL and extract key info from pdf file so we
     * can decode any pages - Does not actually decode the pages themselves -
     * Also reads the form data - Based on an idea by Peter Jacobsen
     * <br>
     * You must explicitly close any open files with closePdfFile() so Java will
     * release all the memory
     * <br>
     *
     * If boolean supportLinearized is true, method will return with true value once Linearized part read
     */
    @Override
    public final boolean openPdfFileFromURL(final String pdfUrl, final boolean supportLinearized, final String password) throws PdfException {
        
        InputStream is=null;
        
        String rawFileName = null;
        
        try{
            final URL url;
            url = new URL(pdfUrl);
            rawFileName = url.getPath().substring(url.getPath().lastIndexOf('/')+1);
            
            is = url.openStream();
        }catch(final IOException e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        preOpen();
        
        final boolean flag= fileAccess.readFile(supportLinearized, is, rawFileName, password);
        
        postOpen();
        
        return flag;
    }
    
    /**
     * routine to open PDF file via InputStream and extract key info from pdf file so we
     * can decode any pages - Does not actually decode the pages themselves -
     * <br>
     * You must explicitly close any open files with closePdfFile() to Java will
     * not release all the memory
     *
     * IMPORTANT NOTE: If the stream does not contain enough bytes, test for Linearization may fail
     * If boolean supportLinearized is true, method will return with true value once Linearized part read
     * (we recommend use you false unless you know exactly what you are doing)
     */
    @SuppressWarnings("UnusedReturnValue")
    @Override
    public final boolean openPdfFileFromInputStream(final InputStream is, final boolean supportLinearized) throws PdfException {
        
        final String rawFileName = "inputstream"+System.currentTimeMillis()+ '-' + fileAccess.getObjectStore().getKey()+".pdf";
        
        preOpen();
        
        final boolean flag= fileAccess.readFile(supportLinearized, is, rawFileName, null);
        
        postOpen();
        
        return flag;
    }
    
    /**
     * routine to open PDF file via InputStream and extract key info from pdf file so we
     * can decode any pages - Does not actually decode the pages themselves -
     * <br>
     * You must explicitly close any open files with closePdfFile() to Java will
     * not release all the memory
     *
     * IMPORTANT NOTE: If the stream does not contain enough bytes, test for Linearization may fail
     * If boolean supportLinearized is true, method will return with true value once Linearized part read
     * (we recommend use you false unless you know exactly what you are doing)
     */
    @Override
    public final boolean openPdfFileFromInputStream(final InputStream is, final boolean supportLinearized, final String password) throws PdfException {
        
        final String rawFileName = "inputstream"+System.currentTimeMillis()+ '-' + fileAccess.getObjectStore().getKey()+".pdf";
        
        preOpen();
        
        final boolean flag= fileAccess.readFile(supportLinearized, is, rawFileName, password);
        
        postOpen();
        
        return flag;
    }

    private void postOpen() {
        
        //force back if only 1 page
        if (fileAccess.getPageCount() < 2) {
            options.setDisplayView(Display.SINGLE_PAGE);
        } else {
            options.setDisplayView(options.getPageMode());
        }
         
        formsActionHandler.init(this, externalHandlers.getJavaScript(), this.getFormRenderer());
       
        PluginHandler customPluginHandle=(PluginHandler) externalHandlers.getExternalHandler(Options.PluginHandler);
            
        if(customPluginHandle!=null){
            customPluginHandle.setFileName(fileAccess.getFilename());
        }
    }
    
    private void preOpen() {
        
        // reset so if continuous view mode set it will be recalculated for page
        pages.disableScreen();
        pages.stopGeneratingPage();

        fileAccess.setDecoding(true);
        
        //need to make non-single so bounces back
        //force back if only 1 page
        //if (fileAccess.getPageCount() < 2)
        //options.setDisplayView(Display.CONTINUOUS);
        //else
        //  options.setDisplayView(options.getPageMode());
        
         if (options.getCurrentOffsets() == null){
            options.setCurrentOffsets(new PageOffsets(fileAccess.getPageCount(), getPdfPageData()));
         }
        
        // reset printing
//        swingPrinter.lastPrintedPage = -1;
//        swingPrinter.currentPrintDecoder = null;
        
        fileAccess.setDecoding(false);
        parser.resetOnOpen();
        
        final Object userExpressionEngine=externalHandlers.getExternalHandler(Options.ExpressionEngine);
        externalHandlers.openPdfFile(userExpressionEngine);
        
    
        final AcroRenderer formRenderer=externalHandlers.getFormRenderer();
        
        if (userActionHandler != null) {
            formsActionHandler = userActionHandler;
        } else {
            formsActionHandler = new org.jpedal.objects.acroforms.actions.JavaFXDefaultActionHandler(null);
        }
        
        //pass in user handler if set
        formRenderer.resetHandler(formsActionHandler, scalingdpi.getDpi(),externalHandlers.getJavaScript());
        
        formRenderer.getCompData().setRootDisplayComponent(this);
   
    }
    
    /**
     * Not part of API - used internally
     *
     * will return some dictionary values - if not a set value, will return null
     * @return
     */
    @Override
    public Object getJPedalObject(final int id){
        return parser.getJPedalObject(id);
    }
    
    /**
     * Not part of API - used internally
     *
     * @param mode
     */
    @Override
    public void setPageMode(final int mode){
        options.setPageMode(mode);
    }
    
    /**
     * shows if text extraction is XML or pure text
     */
    @Override
    public boolean isXMLExtraction() {
        
        return options.isXMLExtraction();
    }
    
    /**
     * XML extraction is the default - pure text extraction is much faster
     */
    @Override
    public void useTextExtraction() {
        
        options.setXMLExtraction(false);
    }
    
    /**
     * XML extraction is the default - pure text extraction is much faster
     */
    @Override
    public void useXMLExtraction() {
        
        options.setXMLExtraction(true);
    }
    
    /**
     * remove all displayed objects for JPanel display (wipes current page)
     */
    public void clearScreen() {
        final DynamicVectorRenderer currentDisplay= fileAccess.getDynamicRenderer();
        
        currentDisplay.flush();
        pages.refreshDisplay();
    }
    
    /**
     * allows user to cache large objects to disk to avoid memory issues,
     * setting minimum size in bytes (of uncompressed stream) above which object
     * will be stored on disk if possible (default is -1 bytes which is all
     * objects stored in memory) - Must be set before file opened.
     *
     */
    @Override
    public void setStreamCacheSize(final int size) {
        fileAccess.setStreamCacheSize(size);
    }
    
    /**
     * shows if embedded fonts present on page just decoded
     */
    @Override
    public boolean hasEmbeddedFonts() {
        return resultsFromDecode.hasEmbeddedFonts();
    }
   
    /**
     * given a ref, what is the page
     * @param ref - PDF object reference
     * @return - page number with  being first page
     */
    @Override
    public int getPageFromObjectRef(final String ref) {
        
        return getIO().convertObjectToPageNumber(ref);
    }
    
    /**
     * Returns list of the fonts used on the current page decoded or null
     * type can be PdfDictionary.Font or PdfDictionary.Image
     */
    @Override
    public String getInfo(final int type) {
        
        return parser.getInfo(type);
        
    }
    
    /**
     *
     * Allow user to access Forms renderer object if needed
     */
    @Override
    public AcroRenderer getFormRenderer() {
        return externalHandlers.getFormRenderer();
    }
    
    /**
     * Allow user to access javascript object if needed
     */
    @Override
    public Javascript getJavaScript() {
        return externalHandlers.getJavaScript();
    }
    
    /**
     * shows if page reported any errors while printing. Log
     * can be found with getPageFailureMessage()
     *
     * @return Returns the printingSuccessful.
     */
    public static boolean isPageSuccessful() {
//        return swingPrinter.isPageSuccessful();
        return false;
    }
    
    /**
     * return any errors or other messages while calling decodePage() - zero length is no problems
     */
    @Override
    public String getPageDecodeReport() {
        return parser.getPageDecodeReport();
    }
    
    /**
     * Return String with all error messages from last printed (useful for debugging)
     */
    public static String getPageFailureMessage() {
//        return swingPrinter.getPageFailureMessage();
        return null;
    }
    
    /**
     * If running in GUI mode, will extract a section of rendered page as
     * BufferedImage -coordinates are PDF co-ordinates. If you wish to use hires
     * image, you will need to enable hires image display with
     * decode_pdf.useHiResScreenDisplay(true);
     *
     * @param t_x1
     * @param t_y1
     * @param t_x2
     * @param t_y2
     * @param scaling
     * @return pageErrorMessages - Any printer errors
     */
    @Override
    public BufferedImage getSelectedRectangleOnscreen(float t_x1, float t_y1,
            float t_x2, float t_y2, float scaling){
        //

        return null;
        /**/

    }
    
    /**
     * return object which provides access to file images and name
     */
    @Override
    public ObjectStore getObjectStore() {
        return fileAccess.getObjectStore();
    }
    
    /**
     * return object which provides access to file images and name (use not recommended)
     */
    @Override
    public void setObjectStore(final ObjectStore newStore) {
        fileAccess.setObjectStore(newStore);
    }
    
    /**
     * Return decoder options as object for cases where value is needed externally and can't be static
     *
     * @return DecoderOptions object containing settings for this PdfDecoder object
     */
    @Override
    public DecoderOptions getDecoderOptions(){
        return options;
    }
    
    /**
     * returns object containing grouped text of last decoded page
     * - if no page decoded, a Runtime exception is thrown to warn user
     * Please see org.jpedal.examples.text for example code.
     *
     */
    @Override
    public PdfGroupingAlgorithms getGroupingObject() throws PdfException {
        
        return parser.getGroupingObject();
        
    }

    
    /**
     * returns object containing grouped text from background grouping - Please
     * see org.jpedal.examples.text for example code
     */
    @Override
    public PdfGroupingAlgorithms getBackgroundGroupingObject() {
        
        return parser.getBackgroundGroupingObject();
    }
    
    /**
     * get PDF version in file
     */
    @Override
    public final String getPDFVersion() {
        
        if(getIO()==null) {
            return "";
        } else {
            return getIO().getObjectReader().getType();
        }
    }
    
    /**
     * used for non-PDF files to reset page
     */
    @Override
    public void resetForNonPDFPage(final int pageCount) {
        
        /** set hires mode or not for display */
        final DynamicVectorRenderer currentDisplay= fileAccess.getDynamicRenderer();
        currentDisplay.setHiResImageForDisplayMode(false);
        
        parser.resetFontsInFile();
        fileAccess.setPageCount(pageCount);
        
        final AcroRenderer formRenderer=externalHandlers.getFormRenderer();
        if (formRenderer != null) {
            formRenderer.removeDisplayComponentsFromScreen();
        }
        
        // reset page data
        fileAccess.setPageData(new PdfPageData());
    }
   
    /**
     * flag to show if we suspect problem with some images
     */
    public boolean hasAllImages() {
        return resultsFromDecode.getImagesProcessedFully();
    }
    
    /**
     * returns booleans based on flags in class org.jpedal.parser.DecoderStatus
     * @param status
     * @return
     */
    @Override
    public boolean getPageDecodeStatus(final int status) {
        
        //
        
        return resultsFromDecode.getPageDecodeStatus(status);
        
    }
    
    @Override
    public DisplayOffsets getDisplayOffsets() {
        
        return displayOffsets;
    }
    
    /**
     * get page statuses (flags in class org.jpedal.parser.DecoderStatus)
     */
    @Override
    public String getPageDecodeStatusReport(final int status) {
        
        return resultsFromDecode.getPageDecodeStatusReport(status);
    }
    
    /**
     * set print mode (Matches Abodes Auto Print and rotate output
     */
    public void setPrintAutoRotateAndCenter(final boolean value) {
//        swingPrinter.setCenterOnScaling(value);
//        swingPrinter.setAutoRotate(value);
        
    }
    
    /**
     * tell printout to print only visiable area in viewer if set
     */
    public void setPrintCurrentView(final boolean value) {
//        swingPrinter.printOnlyVisible = value;
    }
    
    /**
     * not part of API used internally
     *
     * allows external helper classes to be added to JPedal to alter default functionality -
     *
     * @param newHandler
     * @param type
     */
    @Override
    public void addExternalHandler(final Object newHandler, final int type) {
        
        switch (type) {
            
            
          
            case Options.FormsActionHandler:
                
                userActionHandler= (ActionHandler) newHandler;
                
                break;
           
           
            case Options.CustomMouseHandler:
                JavaFXMouseListener.setCustomMouseFunctions((JavaFXMouseFunctionality) newHandler);
                break;
                
            case Options.ThumbnailHandler:
                pages.setThumbnailPanel((org.jpedal.display.GUIThumbnailPanel) newHandler);
                break;
            
                
            default:
                externalHandlers.addExternalHandler(newHandler,type);
                
        }
    }
    
    /**
     * not part of API used internally
     *
     * allows external helper classes to be accessed if needed - also allows user to access SwingGUI if running
     * full Viewer package - not all Options available to get - please contact IDRsolutions if you are looking to
     * use
     *
     * @param type
     */
    @Override
    public Object getExternalHandler(final int type) {
        
        switch (type) {
            
            case Options.Display:
                return pages;
                
            case Options.CurrentOffset:
                return fileAccess.getOffset();
            
            case Options.DisplayOffsets:
                return displayOffsets;
                
            case Options.FileAccess:
                return fileAccess;
                
            default:
                return externalHandlers.getExternalHandler(type);
                
        }
    }
    
    /**
     * allow access to PDF file
     * @return
     */
    @Override
    public PdfObjectReader getIO() {
        return parser.getIO();
    }
    
    //
    
    /**
     * currently open PDF file name
     * @return
     */
    @Override
    public String getFileName() {
        return fileAccess.getFilename();
    }
    
    /**
     * return true if currently open PDF file is a PDF form
     * @return
     */
    @Override
    public boolean isForm() {
        return res.isForm();
    }
    
    
    /**
     * part of pageable interface
     *
     * @see java.awt.print.Pageable#getPrintable(int)
     */
    @Override
    public Printable getPrintable(final int page) throws IndexOutOfBoundsException {
        
        return this;
    }
    
    //
    
    /**
     *
     * access textlines object
     */
    @Override
    public TextLines getTextLines() {
        return parser.getTextLines();
    }
   
    /**
     * set an inset display so that display will not touch edge of panel*/
    @Override
    public final void setInset(final int width, final int height) {
        options.setInset(width, height);
        
        //If we have a form renderer pass the new inset to it.
        //This is needed as updating the insets after a page is open does not alter the form positions.
        final AcroRenderer formRenderer = externalHandlers.getFormRenderer();
        if(formRenderer!=null){
            formRenderer.setInsets(width, height);
        }
    }
    
    /**
     * make screen scroll to ensure point is visible
     */
    public static void ensurePointIsVisible(final Point p){
        //<start-demo><end-demo>
        //@swing
//        super.scrollRectToVisible(new Rectangle(p.x,y_size-p.y,scrollInterval,scrollInterval));
    }
    
    //
    
    /**
     *
     * not part of API used internally
     *
     * allow user to 'move' display of PDF
     *
     * mode is a Constant in org.jpedal.external.OffsetOptions (ie OffsetOptions.SWING_DISPLAY,OffsetOptions.PRINTING)
     */
    @Override
    public void setUserOffsets(final int x, final int y, final int mode){
        
        displayOffsets.setUserOffsets(x, y, (int)getHeight(),mode);      
       
    
    }
    
    /**
     * not part of API used internally
     * @param mode
     * @return
     */
    public Point getUserOffsets(final int mode){
        
        return displayOffsets.getUserOffsets(mode);
        
    }
    
    /**
     * get sizes of panel <BR>
     * This is the PDF pagesize (as set in the PDF from pagesize) -
     * It now includes any scaling factor you have set (ie a PDF size 800 * 600
     * with a scaling factor of 2 will return 1600 *1200)
     */
    public final int[] getMaximumSize() {
        
        int[] pageSize=null;
        
        final int displayView=options.getDisplayView();
        if(displayView!=Display.SINGLE_PAGE) {
            pageSize = pages.getPageSize(displayView);
        }
        
        final int insetW=options.getInsetW();
        final int insetH=options.getInsetH();
        
        if(pageSize==null){
            if(displayRotation==90 || displayRotation==270) {
                pageSize = new int[]{y_size + insetW + insetW, x_size + insetH + insetH};
            } else {
                pageSize = new int[]{x_size + insetW + insetW, y_size + insetH + insetH};
            }
            
        }
        
        if(pageSize==null) {
            pageSize = getMinimumSize();
        }
        
        return pageSize;
        
    }
    /**/
    
    /* get width of panel*/
    private int[] getMinimumSize() {
        
        return new int[]{100+options.getInsetW(),100+options.getInsetH()};
    }
   
    /**
     * Deprecated on 04/07/2014, please use 
     * updateCursorBoxOnScreen(final int[] rectParams, final int outlineColor) instead
     * @deprecated
     */
    @Override
    public final void updateCursorBoxOnScreen(final Rectangle newOutlineRectangle, final Color outlineColor) {
        
        if(options.getDisplayView()!=Display.SINGLE_PAGE) {
            return;
        }
        
        //<start-demo><end-demo>
        
    }
    
    /**
     * update rectangle we draw to highlight an area -
     * See Viewer example for example code showing current usage.
     * This method takes an int array containing the x,y,w,h params of 
     * the rectangle we wish to update.
     * It also takes an int outLineColor which is the rgb value of a Color object.
     */
    @Override
    public final void updateCursorBoxOnScreen(final int[] rectParams, final int outlineColor) {
        
        if(options.getDisplayView()!=Display.SINGLE_PAGE) {
            return;
        }
        
        pages.updateCursorBoxOnScreen(rectParams, outlineColor, getPageNumber(),x_size,y_size);
        
    }
    
    
    /**
     * get sizes of panel <BR>
     * This is the PDF pagesize (as set in the PDF from pagesize) -
     * It now includes any scaling factor you have set
     */
    @Override
    public final int getPDFWidth() {
        
        final int insetW=options.getInsetW();
        
        if(displayRotation==90 || displayRotation==270) {
            return y_size + insetW + insetW;
        } else {
            return x_size + insetW + insetW;
        }
        
    }
    
    /**
     * get sizes of panel -
     * This is the PDF pagesize
     */
    @Override
    public final int getPDFHeight() {
        
        final int insetH=options.getInsetH();
        
        if((displayRotation==90 || displayRotation==270)) {
            return x_size + insetH + insetH;
        } else {
            return y_size + insetH + insetH;
        }
        
    }
    
    /**set border for screen and print which will be displayed<br>
     * Setting a new value will enable screen and border painting - disable
     * with disableBorderForPrinting() */
    @Override
    public final void setPDFBorder(final Border newBorder){
        this.myBorder=newBorder;
    }
    
    //
    
    /**return amount to scroll window by when scrolling (default is 10)*/
    @Override
    public int getScrollInterval() {
        return scrollInterval;
    }
    
    /**set amount to scroll window by when scrolling*/
    @Override
    public void setScrollInterval(final int scrollInterval) {
        this.scrollInterval = scrollInterval;
    }
    
    /**
     * returns view mode used - ie SINGLE_PAGE,CONTINUOUS,FACING,CONTINUOUS_FACING  (no effect in OS versions)
     */
    @Override
    public int getDisplayView() {
        return options.getDisplayView();
    }
   
    //
    
    /**
     * returns current scaling value used internally
     * @return
     */
    @Override
    public float getScaling() {
        return scaling;
    }
    
    @Override
    public int getInsetH() {
        return options.getInsetH();
    }
    
    @Override
    public int getInsetW() {
        return options.getInsetW();
    }
    
    /**
     * part of pageable interface - used only in printing
     * Use getPageCount() for number of pages
     *
     * @see java.awt.print.Pageable#getNumberOfPages()
     */
    @Override
    public int getNumberOfPages() {
        
//        return swingPrinter.getNumberOfPages(fileAccess.getPageCount());
        return -1;
    }
    
    /**
     * part of pageable interface
     *
     * @see java.awt.print.Pageable#getPageFormat(int)
     */
    @Override
    public PageFormat getPageFormat(final int p) throws IndexOutOfBoundsException {
        
       // PageFormat pf = null;//swingPrinter.getPageFormat(p, fileAccess.getPdfPageData(), fileAccess.getPageCount());
        
        //If we have a null value, use PageFormat default.
        //if(pf==null)
        //    pf = new PageFormat();
        
//        return pf;
        return null;
        
    }
    //
    
    @Override
    public void setScaling(final float x) {
        scaling = x;
    }

    @Override
    public int[] getMaxSizeWH() {
        return getMaximumSize();
    }
    
    @Override
    public int[] getPaneBounds(){
        final int[] boundsArr = new int[2];
        boundsArr[0] = (int)getBoundsInLocal().getWidth();
        boundsArr[1] = (int)getBoundsInLocal().getHeight();
        return boundsArr;
    }
    
    /**
     * Variables for text highlights.
     */
    public final Pane highlightsPane = new Pane();
    private static int color = DecoderOptions.highlightColor.getRGB();
    private static float opacity = DecoderOptions.highlightComposite;
    private static javafx.scene.paint.Color highlights;
    
    @Override
    public void repaintPane(final int page) {
        curThumbPage = page;
        final Map areas = parser.getTextLines().getAllHighlights();
        if (areas != null) {
            final int[][] rawRects = ((int[][]) areas.get(page));

            if (rawRects != null) {
                highlights = JavaFXPreferences.shiftColorSpaceToFX(color);
                final javafx.scene.shape.Rectangle[] rects = new javafx.scene.shape.Rectangle[rawRects.length];

                for (int i = 0; i < rects.length; i++) {
                    rects[i] = new javafx.scene.shape.Rectangle(rawRects[i][0], rawRects[i][1], rawRects[i][2], rawRects[i][3]);
                    rects[i].setFill(highlights);
                    rects[i].setOpacity(opacity);
                }

                highlightsPane.getChildren().clear();
                highlightsPane.getChildren().addAll(rects);
            }
        } else {
            highlightsPane.getChildren().clear();
        }
        
        drawHighlightsForImage();
        
        drawPreviewImage(null, new Rectangle((int)getBoundsInLocal().getMinX(), (int)getBoundsInLocal().getMinY(), (int)getBoundsInLocal().getWidth(), (int)getBoundsInLocal().getHeight()));
        
    }
    
    private void drawHighlightsForImage(){
        final int[] highlight = pages.getHighlightedImage();
        
        if(highlight == null ){
            if(imageHighlighter != null){
                highlightsPane.getChildren().remove(imageHighlighter);
                imageHighlighter = null;
            }
        }else{
            if(highlight.length > 0){
                if (highlight[0] < 0) {
                    highlight[0] = 0;
                }

                if (highlight[1] < 0) {
                    highlight[1] = 0;
                }
                if (highlight[0] + highlight[2] > max_x) {
                    highlight[2] = max_x - highlight[0];
                }
                if (highlight[1] + highlight[3] > max_y) {
                    highlight[3] = max_y - highlight[1];
                }

                imageHighlighter = new javafx.scene.shape.Rectangle(highlight[0], highlight[1], highlight[2], highlight[3]);
                imageHighlighter.setStroke(javafx.scene.paint.Color.BLACK);
                imageHighlighter.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.25));
                highlightsPane.getChildren().add(imageHighlighter);
            }
        }
    }
    
    @Override
    public void setBorderPresent(final boolean borderPresent){
        isBorderPresent = borderPresent;
    }
    
    @Override
    public boolean isBorderPresent(){
        return isBorderPresent;
    }
    
    /**
     * internal method used by Viewer to provide preview of PDF in Viewer
     */
    @Override
    public void setPreviewThumbnail(final BufferedImage previewImage, final String previewText) {
        
        if (previewThumbnail == null) {
            previewThumbnail = new Canvas(previewImage.getWidth() + 20, previewImage.getHeight() + 40);

        }

        //Prevent thumbnail scaling
//        previewThumbnail.setScaleX(1.0f / scaling);
//        previewThumbnail.setScaleY(-(1.0f / scaling));
//        previewThumbnail.setScaleZ(1.0f / scaling);
        
        Group group = (Group)getParent();

        //Make sure preview is actually displayed
        if (!group.getChildren().contains(previewThumbnail)) {
            group.getChildren().add(previewThumbnail);
        }

        this.previewImage = SwingFXUtils.toFXImage(previewImage, null);
        this.previewText = previewText;
        
    }
    
     @Override
    public int getTextPrint() {
       return textPrint;
    }

    @Override
    public Rectangle getVisibleRect() {
        return null;
    }
}
