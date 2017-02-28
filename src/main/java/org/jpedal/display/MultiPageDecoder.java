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
 * MultiPageDecoder.java
 * ---------------
 */

package org.jpedal.display;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Semaphore;
import org.jpedal.FileAccess;
import org.jpedal.PdfDecoderInt;
import org.jpedal.constants.SpecialOptions;
import static org.jpedal.display.Display.CONTINUOUS;
import static org.jpedal.display.Display.CONTINUOUS_FACING;
import static org.jpedal.display.Display.FACING;
import static org.jpedal.display.Display.SINGLE_PAGE;
import static org.jpedal.display.Display.debugLayout;
import org.jpedal.external.Options;
import org.jpedal.external.RenderChangeListener;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.PageObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.*;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.text.TextLines;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

/**
 *
 * @author markee
 */
public abstract class MultiPageDecoder {

    private final Semaphore semaphore=new Semaphore(1);

    final GUIFactory gui;
    
    //used to redraw multiple pages
    private Thread worker;
     
    public final Map currentPageViews=new HashMap();
    
    private final FileAccess fileAccess;

    private final PdfObjectReader currentPdfFile;
    
    private final AcroRenderer formRenderer;

    private int displayView;
    
    private Map cachedPageViews=new WeakHashMap();

    private final MultiPagesDisplay display;
    
    private RenderChangeListener customRenderChangeListener;
    
    //facing mode drag pages
    private final BufferedImage[] facingDragCachedImages = new BufferedImage[4];
    private BufferedImage facingDragTempLeftImg, facingDragTempRightImg;
    private int facingDragTempLeftNo, facingDragTempRightNo;

    private final MultiDisplayOptions multiDisplayOptions;
   
    public final PdfDecoderInt pdf;
    
    private final PdfPageData pageData;
    
    final DecoderOptions options;
    
    final DisplayOffsets offsets;
    
    public MultiPageDecoder(final GUIFactory gui,final PdfDecoderInt pdf,final PdfPageData pageData,final MultiPagesDisplay display, final MultiDisplayOptions multiDisplayOptions, 
            final DynamicVectorRenderer currentDisplay, final int pageNumber,final FileAccess fileAccess, 
            final PdfObjectReader io, final AcroRenderer formRenderer,final DecoderOptions options) {
       
        this.gui=gui;
        this.pdf=pdf;
        this.pageData=pageData;
       
        this.display=display;
        this.multiDisplayOptions=multiDisplayOptions;
        this.fileAccess=fileAccess;
        this.currentPdfFile=io;
        this.formRenderer=formRenderer;
        this.options=options;
        
        offsets=(DisplayOffsets) pdf.getExternalHandler(Options.DisplayOffsets);
        
        /**cache current page*/
        if(currentDisplay!=null) {
            currentPageViews.put(pageNumber, currentDisplay);
        }
    }
    
    /**used to decode multiple pages on views*/
    public void decodeOtherPages(int pageNumber, final int pageCount,int displayView, PdfDecoderInt pdf) {
        
        try {
            semaphore.acquire();
        } catch (InterruptedException ex) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + ex.getMessage());
            }
            //
        }
        
        this.displayView=displayView;
        
        if(debugLayout) {
            System.out.println("start decodeOtherPages " + pageNumber + ' ' + pageCount);
        }

        //Ensure page range does not drop below one
        if(pageNumber<1) {
            pageNumber = 1;
        }

        final int oldPN = multiDisplayOptions.getPageNumber();
        multiDisplayOptions.setPageNumber(pageNumber);

        //Store the image to be used instead of filling the borders with white
        if (displayView==FACING && multiDisplayOptions.isTurnoverOn()) {
             final int lp;
            if (multiDisplayOptions.isSeparateCover()) {
                lp = (oldPN / 2) * 2;
            } else {
                lp = oldPN - (1 - (oldPN & 1));
            }
            if (offsets.getDragLeft()) {
                facingDragTempLeftImg = facingDragCachedImages[0];
                facingDragTempLeftNo = lp-2;
                facingDragTempRightImg = facingDragCachedImages[1];
                facingDragTempRightNo = lp-1;
            } else {
                facingDragTempLeftImg = facingDragCachedImages[2];
                facingDragTempLeftNo = lp+2;
                facingDragTempRightImg = facingDragCachedImages[3];
                facingDragTempRightNo = lp+3;
            }
        }

        facingDragCachedImages[0]=null;
        facingDragCachedImages[1]=null;
        facingDragCachedImages[2]=null;
        facingDragCachedImages[3]=null;

        calcDisplayedRange();

        while(multiDisplayOptions.isRunning()){
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
        }
        
        //need in JavaFX to avoid illegalThreadState
        if(worker!=null && worker.getState()==Thread.State.TERMINATED){
            worker=null;
        }
        
        // restart if not display.running - uses pages to control loop so I hope will
        // pick up change
        if(worker!=null && worker.getState()!=Thread.State.NEW){
            //still running
            //System.out.println(worker.getState()+" "+worker);
        }else if ((worker == null || !multiDisplayOptions.isRunning())) {

            multiDisplayOptions.setRunning(true);
            
            worker = new Thread() {
                @Override
                public void run(){

                    try {

                        if(debugLayout) {
                            System.out.println("START=========================Started decoding pages "
                                    + multiDisplayOptions.getStartViewPage() + ' ' + multiDisplayOptions.getEndViewPage());
                        }

                        decodeOtherPages();

                        if(debugLayout) {
                            System.out.println("END=========================Pages done");
                        }

                        multiDisplayOptions.setRunning(false);

                        //tell user object if exists that pages painted
                        if(customRenderChangeListener!=null) {
                            customRenderChangeListener.renderingWorkerFinished();
                        }

                    } catch (final Exception e) {
                        
                        multiDisplayOptions.setRunning(false);
                        //tell user and log
                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog("Exception: " + e.getMessage());
                        }
                        //
                    }catch(final Error err){
                        multiDisplayOptions.setRunning(false);
                        //tell user and log
                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog("Error: " + err.getMessage());
                        }
                        
                         //
                    }finally{
                        semaphore.release();
                    }
                }
            };
            worker.setDaemon(true);
            
            worker.start();
        }
    }

    /**
     * scans all pages in visible range and decodes
     */
    void decodeOtherPages() {

        final int pageCount=pageData.getPageCount();
        if(debugLayout) {
            System.out.println("decodeOtherPages called");
        }

        multiDisplayOptions.setIsGeneratingOtherPages(true);

        int page = multiDisplayOptions.getStartViewPage(), originalStart = multiDisplayOptions.getStartViewPage(), originalEnd = multiDisplayOptions.getEndViewPage()+1;

        //increase decoded range for facing with turnover
        int firstFacing=1, lastFacing=1;
        if (multiDisplayOptions.isTurnoverOn() && displayView == FACING) {
            firstFacing = originalStart - 2;
            lastFacing = firstFacing + 6;

            if (firstFacing < 1) {
                firstFacing = 1;
            }
            if (lastFacing > pageCount+1) {
                lastFacing = pageCount + 1;
            }
        }
        int facingCount = lastFacing-firstFacing;

        resetPageCaches(multiDisplayOptions.getStartViewPage(), multiDisplayOptions.getEndViewPage()+1);

        if(debugLayout){
            System.out.println("decoding ------START " + originalStart + " END="+ originalEnd+" display.isGeneratingOtherPages="+multiDisplayOptions.isIsGeneratingOtherPages());
            System.out.println(multiDisplayOptions.getStartViewPage() + " "+ multiDisplayOptions.getEndViewPage());
        }
        while (multiDisplayOptions.isIsGeneratingOtherPages()) {

            // detect if restarted
            if ((originalStart != multiDisplayOptions.getStartViewPage())&& (originalEnd != multiDisplayOptions.getEndViewPage())) {
                // 

                page = multiDisplayOptions.getStartViewPage();
                originalEnd = multiDisplayOptions.getEndViewPage()+1;

                // can be zero in facing mode
                if (page == 0) {
                    page++;
                }
                originalStart = page;


                if (multiDisplayOptions.isTurnoverOn() && displayView == FACING) {
                    firstFacing = originalStart - 2;
                    lastFacing = firstFacing + 6;

                    if (firstFacing < 1) {
                        firstFacing = 1;
                    }
                    if (lastFacing > pageCount+1) {
                        lastFacing = pageCount + 1;
                    }

                    facingCount = lastFacing-firstFacing;
                }

                resetPageCaches(originalStart, originalEnd);

            }

            // exit if finished
            if (multiDisplayOptions.isTurnoverOn() && displayView == FACING && facingCount==0) {
                break;
            }

            if ((!multiDisplayOptions.isTurnoverOn() || displayView != FACING) && page == originalEnd) {
                break;
            }

            //
            if (page > 0 && page < pdf.getPageCount()+1) {
                decodeMorePages(page, originalStart, originalEnd);
            }
            
            //store thumbnail for turnover if in facing mode
            if (displayView==FACING && multiDisplayOptions.isTurnoverOn()) {
                int leftPage = multiDisplayOptions.getPageNumber();
                if (multiDisplayOptions.isSeparateCover() && (leftPage & 1) == 1) {
                    leftPage--;
                }
                if (!multiDisplayOptions.isSeparateCover() && (leftPage & 1) == 0) {
                    leftPage--;
                }
                int ref = page-leftPage + 2;
                if (!(ref > 1 && ref < 4 )){

                    if (ref > 1) {
                        ref -= 2;
                    }
                    
                    int[] pageW=multiDisplayOptions.getPageW();
                    int[] pageH=multiDisplayOptions.getPageH();
//                    System.out.println("page + \" \" + ref = " + page + " " + ref);
                    if(ref < 4 && ref > -1 && facingDragCachedImages[ref]==null) {

                        final BufferedImage image = new BufferedImage(pageW[page], pageH[page], BufferedImage.TYPE_INT_ARGB);
                        final Graphics2D pg = (Graphics2D) image.getGraphics();
                        
                        final int displayRotation=display.getDisplayRotation();
                        
                        pg.rotate(displayRotation*Math.PI/180);
                        try {
                            if (displayRotation == 90) {
                                pg.translate(0,-pageW[page]);
                                pg.drawImage(pdf.getPageAsImage(page),0,0,pageH[page]+1,pageW[page]+1,null);
                            } else if (displayRotation == 180) {
                                pg.translate(-pageW[page],-pageH[page]);
                                pg.drawImage(pdf.getPageAsImage(page),0,0,pageW[page]+1, pageH[page]+1, null);
                            } else if (displayRotation == 270) {
                                pg.translate(-pageH[page], 0);
                                pg.drawImage(pdf.getPageAsImage(page),0,0,pageH[page]+1, pageW[page]+1,null);
                            } else {
                                pg.drawImage(pdf.getPageAsImage(page),0,0,pageW[page]+1, pageH[page]+1,null);
                            }
                        } catch(final Exception e) {
                            //tell user and log
                            if(LogWriter.isOutput()) {
                                LogWriter.writeLog("Exception: " + e.getMessage());
                            }
                            //
                        }

                        facingDragCachedImages[ref] = image;
                    }
                }
            }

            facingCount--;

            //change decode order if facing
            page++;
            if (multiDisplayOptions.isTurnoverOn() && displayView == FACING && page == lastFacing) {
                page = firstFacing;
            }
        }

        if(debugLayout) {
            System.out.println("decodeOtherPageinins------ENDED");
        }
    }

    public void decodeMorePages(int page, int originalStart, int originalEnd) {
        /**
         * decode or get cached page data. If it is still in cache we just need a repaint
         */    
        if(currentPageViews.get(page)==null){

            decodePage(page, originalStart, originalEnd);
        }

        repaint();
        
    }
    
    public void decodePage(int page,int originalStart, int originalEnd) {
        
        final AcroRenderer formRenderer =pdf.getFormRenderer();
        //Create form objects
        if (displayView == CONTINUOUS || displayView == CONTINUOUS_FACING){
            final PdfStreamDecoder current= (PdfStreamDecoder) currentPageViews.get(page);
            
            //if(currentOffset!=null)
            //     formRenderer.getCompData().setPageValues(pdf.getScaling(), displayRotation,(int)getIndent(),0,0,pdf.getDisplayView(),currentOffset.widestPageNR,currentOffset.widestPageR);
            
            formRenderer.createDisplayComponentsForPage(page,current);
            
            
            if(pdf.getSpecialMode()!= SpecialOptions.NONE &&
                    pdf.getSpecialMode()!= SpecialOptions.SINGLE_PAGE &&
                    page!=pdf.getPageCount()) {
              
                formRenderer.createDisplayComponentsForPage(page + 1, current);
            }
            
            
        }
        /** get pdf object id for page to decode */
        final String currentPageOffset = pdf.getIO().getReferenceforPage(page);
        if (debugLayout) {
            System.out.println("Decoding page " + page + " currentPageOffset=" + currentPageOffset + " range=" + originalStart + ' ' + originalEnd);
        }
        /**
         * decode the file if not already decoded and stored
         */
        if (currentPageOffset != null || (formRenderer.isXFA() && formRenderer.useXFA())) {
            final Integer key= page;
            final Object currentView = currentPageViews.get(key);
            if (currentView == null  && multiDisplayOptions.isIsGeneratingOtherPages()) {
                if (debugLayout) {
                    System.out.println("recreate page");
                }
                // force redraw
                display.forceRedraw();
                getPageView(currentPageOffset, page);
            }
        }
    }
    
    
    private void getPageView(final String currentPageOffset, final int pageNumber) {

        final PdfObject pdfObject=new PageObject(currentPageOffset);

        //ensure set (needed for XFA)
        pdfObject.setPageNumber(pageNumber);

        /** read page or next pages */
        currentPdfFile.readObject(pdfObject);

        final PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

        final DynamicVectorRenderer currentDisplay = getNewDisplay(pageNumber);
            
        /** set hires mode or not for display */
        currentDisplay.setHiResImageForDisplayMode(pdf.isHiResScreenDisplay());

        int val=0;
        
        if(pdf.getDisplayView()==Display.CONTINUOUS && pdf.getDisplayView()==Display.CONTINUOUS_FACING) {
            val = 1;
        }
        
        final PdfStreamDecoder current=formRenderer.getStreamDecoder(currentPdfFile, pdf.isHiResScreenDisplay(), fileAccess.getRes().getPdfLayerList(),true);

        /**
         * draw acroform data onto Panel
         */
        if (formRenderer != null && pdf.isForm()) {
            //  formRenderer.getCompData().setPageValues(scaling, displayRotation,0,0,0,pdf.getDisplayView(),currentOffset.widestPageNR,currentOffset.widestPageR);
            formRenderer.createDisplayComponentsForPage(pageNumber,current);
        }

        current.setParameters(true, true, 7, val, false,pdf.getExternalHandler().getMode().equals(GUIModes.JAVAFX));
        current.setXMLExtraction(pdf.isXMLExtraction());
        pdf.getExternalHandler().addHandlers(current);

        current.setObjectValue(ValueTypes.Name, fileAccess.getFilename());
        current.setObjectValue(ValueTypes.ObjectStore, pdf.getObjectStore());
        current.setObjectValue(ValueTypes.PDFPageData,pageData);
        current.setIntValue(ValueTypes.PageNum, pageNumber);
        current.setRenderer(currentDisplay);

        try {

            currentDisplay.init(pageData.getMediaBoxWidth(pageNumber), pageData.getMediaBoxHeight(pageNumber),options.getPageColor());

            currentDisplay.setValue(DynamicVectorRenderer.ALT_BACKGROUND_COLOR, options.getPageColor().getRGB());
            if(options.getTextColor()!=null){
                currentDisplay.setValue(DynamicVectorRenderer.ALT_FOREGROUND_COLOR, options.getTextColor().getRGB());
                if(options.getChangeTextAndLine()) {
                    currentDisplay.setValue(DynamicVectorRenderer.FOREGROUND_INCLUDE_LINEART, 1);
                } else {
                    currentDisplay.setValue(DynamicVectorRenderer.FOREGROUND_INCLUDE_LINEART, 0);
                }


            }
            fileAccess.getRes().setupResources(current, false, Resources,pageNumber,currentPdfFile);

            current.decodePageContent(pdfObject);

            final TextLines textLines = pdf.getTextLines();
            //All data loaded so now get all line areas for page
            if(textLines!=null){
                final Vector_Rectangle_Int vr = (Vector_Rectangle_Int) current.getObjectValue(ValueTypes.TextAreas);
                vr.trim();
                final int[][] pageTextAreas = vr.get();

                final Vector_Int vi =  (Vector_Int) current.getObjectValue(ValueTypes.TextDirections);
                vi.trim();
                final int[] pageTextDirections = vi.get();

                for(int k=0; k!=pageTextAreas.length; k++){
                    textLines.addToLineAreas(pageTextAreas[k], pageTextDirections[k], pageNumber);
                }
            }

            //tell viewer we have finished so it will generate highlights
            currentDisplay.flagDecodingFinished();

        } catch (final Exception ex) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + ex.getMessage());
            }
            //
        } catch (final Error err) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + err.getMessage());
            }
            //
        }

        currentPageViews.put(pageNumber,currentDisplay);
        
        display.setCurrentDisplay(currentDisplay);
        
    }

    /**
     * copy pages between WeakMap and vice-versa
     */
    void resetPageCaches(final int startPage, final int endPage) {

        // copy any pages in existence into hashMap so not garbage collected
        //synchronized (cachedPageViews) {
        for (final Object o : this.cachedPageViews.keySet()) {
            final Integer currentKey = (Integer) o;
            final int keyValue = currentKey;
            if ((keyValue >= startPage) && (keyValue <= endPage)) {
                final Object obj = cachedPageViews.get(currentKey);
                if (obj != null) {
                    this.currentPageViews.put(currentKey, obj);
                }
            }
        }
        //}

        // move any pages not visible into cache
        {
            //synchronized (currentPageViews) {
            Iterator keys = this.currentPageViews.keySet().iterator();

            final Map keysToTrash = new HashMap();

            while (keys.hasNext()) {
                final Integer currentKey = (Integer) keys.next();
                final int keyValue = currentKey;
                if ((keyValue < startPage) || (keyValue > endPage)) {
                    final Object obj = currentPageViews.get(currentKey);
                    if (obj != null) {
                        this.cachedPageViews.put(currentKey, obj);
                    }

                    // track so we can delete outside loop
                    keysToTrash.put(currentKey, "x");

                }
            }

            // now remove
            keys = keysToTrash.keySet().iterator();

            while (keys.hasNext()) {
                currentPageViews.remove(keys.next());
            }

            //}
        }

        //System.out.println("cache contains " + currentPageViews.keySet());
    }


    /**
     * workout which pages are displayed
     */
    private synchronized void calcDisplayedRange() {

        final int pageCount=pageData.getPageCount();

        if(debugLayout) {
            System.out.println("calcDisplayedRange pageNumber=" + multiDisplayOptions.getPageNumber() + " mode=" + displayView);
        }

        if(displayView==SINGLE_PAGE) {
            return;
        }

        display.getDisplayedRectangle();

        if(displayView==FACING){

          multiDisplayOptions.calcDisplayRangeForFacing();
        
        }else{


            //// START SI'S PAGE COUNTER ////////////

            final int newPage = updatePageDisplayed();
//System.out.println("newPage="+newPage);
            fileAccess.setPageNumber(newPage);

            //// END SI'S PAGE COUNTER ////////////

            //update page number
            if(newPage!=-1)// && customSwingHandle!=null)
            {
                gui.setPage(newPage);//( (org.jpedal.gui.GUIFactory) customSwingHandle).setPage(newPage);
            }
        }

        //Ensure end page is not set beyond the total count of pages
        if(multiDisplayOptions.getEndViewPage()>pageCount) {
            multiDisplayOptions.setEndViewPage(pageCount); 
        }

        if(displayView!=FACING) {
            display.refreshDisplay();      //refresh display to fix backbuffer background color issue
        }
    }

    public void flushPageCaches() {
        currentPageViews.clear();
          
        cachedPageViews.clear();
        
    }

    public DynamicVectorRenderer getCurrentPageView(int i) {
        return (DynamicVectorRenderer) currentPageViews.get(i);
    }

    public void dispose() {
        this.cachedPageViews=null;
    }

    private int updatePageDisplayed() {
        
        int newPage=-1;
        final int pageCount=pageData.getPageCount();

        final int[] yReached=multiDisplayOptions.getyReached();
        
        final int[] pageH = multiDisplayOptions.getPageH();
        final int ry=display.getRy();
        final int rh=display.getRh();
        //final int insetH=display.getInsetH();
        
        final boolean debug=false;
        int largestH = 0;
        int firstVisiblePage = 0;
        int lastVisiblePage = 0;
        for (int i = 1; i <= pageCount; i += 1) {
            
            int pageTop=yReached[i];
            int pageBottom=yReached[i]+pageH[i];
            int viewBottom=ry+rh;
            if(debug){
                System.out.println(display.getInsetH()+" "+i+ ' ' +" pageTop="+pageTop+" pageBottom="+pageBottom+" viewTop="+ ry +" viewBottom="+ viewBottom);
            }
            if( pageTop<=viewBottom && pageBottom>= ry){
                //in view
                
                if(debug){
                    System.out.println("in view ");
                }
            }else{
                
                continue;
            }
            
            //If not set yet, set to first page by default
            if (newPage == -1) {
                newPage = i;
                firstVisiblePage=i;
            }
            lastVisiblePage = i;
            
            int midPt=ry+(rh/2);
            int gap=midPt-yReached[i];
            if(debug){
                System.out.println("gap="+gap);
            }
            if (gap<0 || gap>pageH[i]){
                gap=0;
            }
            
            int gap2=yReached[i]+pageH[i]-midPt;
            if(debug){
                System.out.println("gap2="+gap2);
            }
            if (gap2<0 || gap2>pageH[i]){
                gap2=0;
            }
            
            if(gap2>gap){
                gap=gap2;
            }
            
            if (gap>0 && gap > largestH) {
                largestH =gap;
                newPage = i;
                
                if(debug){
                    System.out.println(i+" gap now="+largestH);
                }
                
                //break; //Stop if found first whole page
            }
            
            if(debug){
                System.out.println(i+" reached "+yReached[i]+ ' ' +ry);
            }
        }
        
        //If still not set set page 1 as a default
        if (newPage == -1) {
            newPage = 1;
            firstVisiblePage = 1;
            lastVisiblePage = 1;
        }
        
        multiDisplayOptions.setStartViewPage(firstVisiblePage);
        //allow for  2 page doc with both pages onscreen
        multiDisplayOptions.setEndViewPage(lastVisiblePage);
        
        if (debugLayout) {
            System.out.println("page range start=" + multiDisplayOptions.getStartViewPage() + " end=" + multiDisplayOptions.getEndViewPage());
        }
        
        //Ensure end page is not set beyond the total count of pages
        if (multiDisplayOptions.getEndViewPage() > pageCount) {
            multiDisplayOptions.setEndViewPage(pageCount);
        }
        
        return newPage;
    }
    
    public void resetCachedFacingImages() {
        for (int i=0; i<4; i++) {
            facingDragCachedImages[i] = null;
        }       
    }

    public BufferedImage[] getFacingDragImages() {
        return facingDragCachedImages;
    }

    public BufferedImage getfacingDragTempLeftImg() {
        return facingDragTempLeftImg;
    }

    public BufferedImage getfacingDragTempRightImg() {
        return facingDragTempRightImg;
    }

    public int getFacingDragTempLeftNo() {
        return facingDragTempLeftNo;
    }
    
    public int getFacingDragTempRightNo() {
        return facingDragTempRightNo;
    }

    public void setCustomRenderChangeListener(RenderChangeListener customRenderChangeListener) {
        this.customRenderChangeListener=customRenderChangeListener;
    }

    public void repaint() {
        throw new UnsupportedOperationException("repaint Not supported yet.");
    }

    public DynamicVectorRenderer getNewDisplay(int pageNumber) {
        throw new UnsupportedOperationException(this+ "Not supported yet."); 
    }
}
