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
 * PdfDecoderInt.java
 * ---------------
 */
package org.jpedal;

import org.jpedal.display.Display;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.*;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.outlines.OutlineData;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.text.TextLines;
import org.jpedal.utils.DPIFactory;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Map;
import javax.swing.border.Border;
import org.jpedal.display.DisplayOffsets;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.io.StatusBar;
import org.jpedal.parser.DecoderOptions;

public interface PdfDecoderInt {

    /**
     * build number of this version
     */
    String version = "6.6b14";
    /**
     * flag to show extraction mode should include any text
     */
    int TEXT = 1;
    /**
     * flag to show extraction mode should includes original images
     */
    int RAWIMAGES = 2;
    /**
     * flag to show extraction mode includes final scaled/clipped
     */
    int FINALIMAGES = 4;
    /**
     * flag to show extraction mode includes final scaled/clipped
     */
    int RAWCOMMANDS = 16;
    /**
     * flag to show extraction of clipped images at highest res
     */
    int CLIPPEDIMAGES = 32;
    /**
     * flag to show extraction of clipped images at highest res
     */
    int TEXTCOLOR = 64;
    /**
     * flag to show extraction of raw cmyk images
     */
    int CMYKIMAGES = 128;
    /**
     * flag to show extraction of xforms metadata
     */
    int XFORMMETADATA = 256;
    /**
     * flag to show extraction of color required (used in Storypad grouping)
     */
    //int COLOR = 512;
    
    /**
     * flag to tell code to flatten forms
     */
    int RASTERIZE_FORMS=1024;
    /**
     * flag to show render mode includes any text
     */
    int RENDERTEXT = 1;
    /**
     * flag to show render mode includes any images
     */
    int RENDERIMAGES = 2;
    /**
     * flag to show render mode includes any images
     */
    int REMOVE_RENDERSHAPES = 16;
    /**
     * flag to stop forms on decodePage
     */
    int REMOVE_NOFORMS = 32;
    
    /**
     * flag to show text highlights need to be done last
     */
    int OCR_PDF = 32;
    /**
     * printing mode using inbuilt java fonts and getting java to rasterize
     * fonts using Java font if match found (added to get around limitations in
     * PCL printing via JPS) - this is the default off setting
     */
    int NOTEXTPRINT = 0;
    /**
     * printing mode using inbuilt java fonts and getting java to rasterize
     * fonts using Java font if match found (added to get around limitations in
     * PCL printing via JPS)
     */
    int TEXTGLYPHPRINT = 1;
    /**
     * printing mode using inbuilt java fonts and getting java to rasterize
     * fonts using Java font if match found (added to get around limitations in
     * PCL printing via JPS)
     */
    int TEXTSTRINGPRINT = 2;
    /**
     * printing mode using inbuilt java fonts and getting java to rasterize
     * fonts using Java font if match found (added to get around limitations in
     * PCL printing via JPS) - overrides embedded fonts for standard fonts (ie Arial)
     */
    int STANDARDTEXTSTRINGPRINT = 3;
    int SUBSTITUTE_FONT_USING_FILE_NAME = 1;
    int SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME = 2;
    int SUBSTITUTE_FONT_USING_FAMILY_NAME = 3;
    int SUBSTITUTE_FONT_USING_FULL_FONT_NAME = 4;
    int SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME_USE_FAMILY_NAME_IF_DUPLICATES= 5;
    
    /**
     * returns object containing grouped text of last decoded page
     * - if no page decoded, a Runtime exception is thrown to warn user
     * Please see org.jpedal.examples.text for example code.
     *
     */
    PdfGroupingAlgorithms getGroupingObject() throws PdfException ;

    /**
     * not part of API used internally
     *
     * allows external helper classes to be added to JPedal to alter default functionality -
     *
     * @param newHandler
     * @param type
     */
    void addExternalHandler(Object newHandler, int type);

    Object getExternalHandler(int type);

    /**
     * allow access to PDF file
     * @return
     */
    PdfObjectReader getIO();

    /**
     *
     * access textlines object
     */
    TextLines getTextLines();

    /**
     * returns object containing grouped text from background grouping - Please
     * see org.jpedal.examples.text for example code
     */
    PdfGroupingAlgorithms getBackgroundGroupingObject();

    //

    boolean isOpen();

    int getDisplayRotation();

    int getPageNumber();

    int getlastPageDecoded();

    Iterator getPageInfo(int type);

    OutlineData getOutlineData();
    
    boolean isLoadingLinearizedPDF();

    int getPageAlignment();

    void dispose();

    void closePdfFile();

    //PdfData getPdfBackgroundData();

    PdfData getPdfData() throws PdfException;

    boolean hasOutline();

    Document getOutlineAsXML();

    PdfPageData getPdfPageData();
    
    int getPDFWidth();
    
    int getPDFHeight();

    BufferedImage getPageAsImage(int pageIndex) throws PdfException;

    //
    
    /**
     * Return amount to inset the page rectangle height by
     * @return int
     */
    int getInsetH();
    
    /**
     * Return amount to inset the page rectangle width by
     * @return int
     */
    int getInsetW();
    
        /**
         * Return amount to scroll window by when scrolling (default is 10).
         * @return int
         */
        int getScrollInterval();

        /**
         * Sets the ammount to scroll the window by (default is 10).
         * @param scrollInterval 
         */
        void setScrollInterval(int scrollInterval);


        /**
         * NOT PART OF API
         * turns off the viewable area, scaling the page back to original scaling
         */
        void resetViewableArea();


        /**
         * used for non-PDF files to reset page
         */
        void resetForNonPDFPage(int pageCount);
        
    void setPDFBorder(Border newBorder);

    void flushObjectValues(boolean reinit);

    PdfImageData getPdfImageData();

    //PdfImageData getPdfBackgroundImageData();

    void setRenderMode(int mode);

    void setExtractionMode(int mode);

    void modifyNonstaticJPedalParameters(Map values) throws PdfException;

    PdfFileInformation getFileInformationData();

    void setExtractionMode(int mode, float scaling);
    
    //
    
    DPIFactory getDPIFactory();

    void waitForDecodingToFinish();

    DynamicVectorRenderer getDynamicRenderer();

    DynamicVectorRenderer getDynamicRenderer(boolean reset);

    void decodePage(int rawPage) throws Exception;

    boolean isPageAvailable(int rawPage);

    boolean isHiResScreenDisplay();
    
    /**
     * Deprecated on 04/07/2014, please use 
     * updateCursorBoxOnScreen(final int[] rectParams, final int outlineColor) instead
     * @deprecated
     */
    void updateCursorBoxOnScreen(Rectangle newOutlineRectangle, Color outlineColor);
    
    void updateCursorBoxOnScreen(int[] rectParams, int outlineColor);
   
    void useHiResScreenDisplay(boolean value);

    void decodePageInBackground(int i) throws Exception;

    int getPageCount();

    boolean isEncrypted();

    boolean isPasswordSupplied();
    
    boolean isForm();

    boolean isFileViewable();

    boolean isExtractionAllowed();

    void setEncryptionPassword(String password) throws PdfException;

    void openPdfFile(String filename) throws PdfException;

    void openPdfFile(String filename, String password) throws PdfException;

    void openPdfArray(byte[] data) throws PdfException;
    
    void openPdfArray(byte[] data, String password) throws PdfException;

    void openPdfFile(String filename, Certificate certificate, PrivateKey key) throws PdfException;

    void openPdfFileFromStream(Object filename, String password) throws PdfException;

    @SuppressWarnings("UnusedReturnValue")
    boolean openPdfFileFromURL(String pdfUrl, boolean supportLinearized) throws PdfException;

    boolean openPdfFileFromURL(String pdfUrl, boolean supportLinearized, String password) throws PdfException;

    @SuppressWarnings("UnusedReturnValue")
    boolean openPdfFileFromInputStream(InputStream is, boolean supportLinearized) throws PdfException;

    boolean openPdfFileFromInputStream(InputStream is, boolean supportLinearized, String password) throws PdfException;

    Object getJPedalObject(int id);

    void setPageMode(int mode);

    boolean isXMLExtraction();

    void useTextExtraction();

    void useXMLExtraction();

    void setStreamCacheSize(int size);
    
    void setUserOffsets(int x, int y, int mode);
    
    /**
     * Deprecated on 07/07/2014
     * please use getPages().setViewableArea instead.
     * @deprecated
     */
    AffineTransform setViewableArea(Rectangle viewport) throws PdfException;
    
    boolean hasEmbeddedFonts();

    int getPageFromObjectRef(String ref);

    String getInfo(int type);

    AcroRenderer getFormRenderer();

    Javascript getJavaScript();

   // boolean isPageSuccessful();

    String getPageDecodeReport();

    //String getPageFailureMessage();

    ObjectStore getObjectStore();

    void setObjectStore(ObjectStore newStore);

    int getDisplayView();

    BufferedImage getSelectedRectangleOnscreen(float t_x1, float t_y1,
                                               float t_x2, float t_y2, float scaling);
    
    float getScaling();
    
    void setScaling(float x);
    
    //temporary below
    void setDisplayRotation(int newRotation);

    Display getPages();

    void setPageParameters(float scaleBy, int page);


    void drawAdditionalObjectsOverPage(int page, int[] type, Color[] colors, Object[] obj) throws PdfException;

    /**
     * allow user to remove all additional grapical content from the page (only for display)
     * ONLY works in SINGLE VIEW displaymode
     */
    void flushAdditionalObjectsOnPage(int page) throws PdfException;

    String getFileName();
    
    boolean getPageDecodeStatus(int status);
    
    String getPageDecodeStatusReport(int status);
    
    String getPDFVersion();
    
    ExternalHandlers getExternalHandler();
    
    int getSpecialMode();
    
    boolean useNewGraphicsMode();
    
    void useNewGraphicsMode(boolean b);
    
    void setPageParameters(float scaling, int pageNumber, int newRotation);
    
    void setStatusBarObject(StatusBar statusBar);
    
    void setInset(int width, int height);
    
    DecoderOptions getDecoderOptions();
    
    int[] getMaxSizeWH();
    
    int[] getPaneBounds();
    
    void repaintPane(int page);
    
    void requestFocus();
    
    void setBorderPresent(boolean borderPresent);
    
    boolean isBorderPresent();
    
    void setPreviewThumbnail(final BufferedImage previewImage, final String previewText);

    Rectangle getVisibleRect();

    DisplayOffsets getDisplayOffsets();

    int getTextPrint();
}
