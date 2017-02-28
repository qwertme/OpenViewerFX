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
 * GUIDisplay.java
 * ---------------
 */
package org.jpedal.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javafx.scene.layout.Pane;

import org.jpedal.exception.PdfException;

import org.jpedal.external.Options;
import org.jpedal.external.RenderChangeListener;

import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.text.TextLines;
import org.jpedal.utils.ScalingFactory;

/**
 * hold code generic to Swing or FX
 */
public class GUIDisplay implements Display{
    
    protected int lastFormPage=-1,lastStart=-1,lastEnd=-1;

    protected int pageUsedForTransform;

    /**tracks indent so changing to continuous does not disturb display*/
    protected int lastIndent=-1;

    protected double indent;
    
    /**Normally null - user object to listen to paint events*/
    public RenderChangeListener customRenderChangeListener;

     //rectangle onscreen
    public int rx,ry,rw,rh;

    protected DisplayOffsets displayOffsets;
    
    public DecoderOptions options;
    
    /**
     * Flag if we should allow cursor to change
     */
    public static boolean allowChangeCursor = true;
    
    public final MultiDisplayOptions multiDisplayOptions=new MultiDisplayOptions(); 

    //Animation enabled (currently just turnover in facing)
    public static boolean default_turnoverOn =  true;//can be altered by user
    
    //Display the first page separately in Facing mode
    public static boolean default_separateCover =  true;//can be altered by user
    
    public DynamicVectorRenderer currentDisplay;
    
    public boolean isInitialised;
    
    /**flag to switch back to unaccelerate screen if no enough memory for scaling*/
    public boolean overRideAcceleration;

    /**render screen using hardware acceleration*/
    public boolean useAcceleration=true;
    
    public boolean ignoreScalingForAcceleration;
    
    public boolean accelerationAlwaysRedraw;
    
    public PageOffsets currentOffset;
    
    
    /** Holds the x,y,w,h of the current highlighted image, null if none */
    private int[] highlightedImage;
    
    protected GUIThumbnailPanel thumbnails;
    
    /** Keep a record of cumulative offsets for SINGLE_PAGE mode*/
    public int[] pageOffsetH, pageOffsetW;

    public boolean[] isRotated;
    
    public int topW,topH;
    public double cropX,cropY,cropW,cropH;
    
    /**used to draw demo cross*/
    public int crx,cry,crw,crh;

    /**local copies*/
    public int displayRotation,displayView=SINGLE_PAGE;
    public int lastDisplayRotation;

    public int insetW,insetH;

    public float scaling;
    public float lastScaling;

    public int pageNumber;
    
    /**any scaling factor being used to convert co-ords into correct values
     * and to alter image size
     */
    public float oldScaling=-1,oldRotation=-1;


    public PdfPageData pageData;
    
    
    @Deprecated
    /*
    * Please use PdfDecoder.setBorderPresent(boolean) instead
    * True : Show border around page.
    * Flase : Remove border around page.
    */
    public static int CURRENT_BORDER_STYLE = 1;
    
    // Affine transformation as a double array
    protected double[] displayScalingDbl;
    
//    public static void setBorderStyle(int style){
//        CURRENT_BORDER_STYLE = style;
//    }
//
//    public static int getBorderStyle(){
//        return CURRENT_BORDER_STYLE;
//    }

    /**
     * used internally by multiple pages
     * scaling -1 to ignore, -2 to force reset
     */
    @Override
    public int getYCordForPage(final int page, final float scaling) {

        if (scaling == -2 || (scaling != -1f && scaling != oldScaling)) {
            oldScaling = scaling;
            setPageOffsets(page);
        }
        return getYCordForPage(page);
    }
    
    @Override
    public boolean getBoolean(final BoolValue option) {
        switch (option) {
            case SEPARATE_COVER:
                return multiDisplayOptions.isSeparateCover();
            case TURNOVER_ON:
                return multiDisplayOptions.isTurnoverOn();
            default:
                //
                return false;
        }
    }

    @Override
    public void setBoolean(final BoolValue option, final boolean value) {
        switch (option) {
            case SEPARATE_COVER:
                multiDisplayOptions.setSeparateCover(value);
                return;
            case TURNOVER_ON:
                multiDisplayOptions.setTurnoverOn(value);
                return;
            default:
                //
        }
    }
    
     /**
     * general method to pass in Objects - only takes  RenderChangeListener at present
     * @param type
     * @param newHandler
     */
    @Override
    public void setObjectValue(final int type, final Object newHandler) {

        //set value
        switch(type){
            case Options.RenderChangeListener:
                customRenderChangeListener = (RenderChangeListener) newHandler;
                break;

            default:
                throw new RuntimeException("setObjectValue does not take value "+type);
        }
    }
    
    @Override
    public void setCursorBoxOnScreen(final Rectangle cursorBoxOnScreen, final boolean isSamePage) {
        
    }


    /**
     * 
     * @param displayView is of type int
     * @return returns an int[] with 2 values ([0]width and [1]height) 
     * which we then use to create a Dimension object in current Swing code
     */
    @Override
    public int[] getPageSize(final int displayView) {

        final int[] pageSize = new int[2];    //element 0 = width, element 1 = height
        
        //height for facing pages
        int biggestFacingHeight=0;
        int facingWidth=0;

        if((displayView==FACING)&&(multiDisplayOptions.getPageW()!=null)){
            //get 2 facing page numbers
            int p1;
            final int p2;
            if (multiDisplayOptions.isSeparateCover()) {
                p1=pageNumber;
                if((p1 & 1)==1) {
                    p1--;
                }
                p2=p1+1;
            } else {
                p1=pageNumber;
                if((p1 & 1)==0) {
                    p1--;
                }
                p2=p1+1;
            }

            if (p1 == 0) {
                biggestFacingHeight = multiDisplayOptions.getPageH(p2);
                facingWidth = multiDisplayOptions.getPageW(p2)*2;
            } else {
                biggestFacingHeight=multiDisplayOptions.getPageH(p1);
                if(p2<multiDisplayOptions.getPageH().length) {
                    if (biggestFacingHeight<multiDisplayOptions.getPageH(p2)) {
                        biggestFacingHeight = multiDisplayOptions.getPageH(p2);
                    }

                    facingWidth = multiDisplayOptions.getPageW(p1)+multiDisplayOptions.getPageW(p2);
                } else {
                    facingWidth = multiDisplayOptions.getPageW(p1)*2;
                }
            }
        }

        final int gaps=currentOffset.getGaps();
        final int doubleGaps=currentOffset.getDoubleGaps();

        switch(displayView){

            case FACING:
                pageSize[0] = facingWidth+insetW+insetW;
                pageSize[1] = biggestFacingHeight+insetH+insetH;
                break;

            case CONTINUOUS:
                if((displayRotation==90)|(displayRotation==270)){
                    pageSize[0] = ((int)(currentOffset.getBiggestHeight()*scaling)+insetW+insetW);
                    pageSize[1] = ((int)(currentOffset.getTotalSingleWidth()*scaling)+gaps+insetH+insetH);
                }
                else{
                    pageSize[0] = ((int)(currentOffset.getBiggestWidth()*scaling)+insetW+insetW);
                    pageSize[1] = ((int)(currentOffset.getTotalSingleHeight()*scaling)+gaps+insetH+insetH);
                }
                break;

            case CONTINUOUS_FACING:
                final int pageCount=pageData.getPageCount();
                
                if((displayRotation==90)|(displayRotation==270)){
                    if(pageCount == 2){
                        pageSize[0] = ((int)(currentOffset.getDoublePageHeight()*scaling)+insetW+insetW);
                        pageSize[1] = ((int)(currentOffset.getBiggestWidth()*scaling)+gaps+insetH+insetH);
                    }
                    else{
                        pageSize[0] = ((int)(currentOffset.getDoublePageHeight()*scaling)+insetW+insetW);
                        pageSize[1] = ((int)(currentOffset.getTotalDoubleWidth()*scaling)+doubleGaps+insetH+insetH);
                    }
                }else{
                    if(pageCount == 2){
                        pageSize[0] = ((int)(currentOffset.getDoublePageWidth()*scaling)+insetW+insetW);
                        pageSize[1] = ((int)(currentOffset.getBiggestHeight()*scaling)+gaps+insetH+insetH);
                    }
                    else{
                        pageSize[0] = ((int)(currentOffset.getDoublePageWidth()*scaling)+insetW+insetW);
                        pageSize[1] = ((int)(currentOffset.getTotalDoubleHeight()*scaling)+doubleGaps+insetH+insetH);
                    }
                }
                break;
        }
        
        return pageSize;
    }    
    
    @Override
    public void setup(final boolean useAcceleration, final PageOffsets currentOffset){

        this.useAcceleration=useAcceleration;
        this.currentOffset=currentOffset;
        
        overRideAcceleration=false;
    }

    @Override
    public void setAcceleration(final boolean enable) {
        useAcceleration = enable;
    }
    
    @Override
    public void setAccelerationAlwaysRedraw(final boolean enable){
    	accelerationAlwaysRedraw = enable;
    }
    
     public void setPageSize(final int pageNumber, final float scaling) {

        /**
         *handle clip - crop box values
         */
        pageData.setScalingValue(scaling); //ensure aligned
        topW=pageData.getScaledCropBoxWidth(pageNumber);
        topH=pageData.getScaledCropBoxHeight(pageNumber);
        final double mediaH=pageData.getScaledMediaBoxHeight(pageNumber);

        cropX=pageData.getScaledCropBoxX(pageNumber);
        cropY=pageData.getScaledCropBoxY(pageNumber);
        cropW=topW;
        cropH=topH;

        /**
         * actual clip values - for flipped page
         */
        if(displayView==Display.SINGLE_PAGE){
            crx =(int)(insetW+cropX);
            cry =(int)(insetH-cropY);
        }else{
            crx =insetW;
            cry =insetH;
        }

        //amount needed to move cropped page into correct position
        final int offsetY=(int) (mediaH-cropH);

        if((displayRotation==90|| displayRotation==270)){
            crw =(int)(cropH);
            crh =(int)(cropW);

            final int tmp = crx;
            crx = cry;
            cry = tmp;

            crx += offsetY;
        }else{
            crw =(int)(cropW);
            crh =(int)(cropH);

            cry += offsetY;
        }
    }

    @Override
    public void decodeOtherPages(final int pageNumber, final int pageCount) {}

    @Override
    public void disableScreen(){
        isInitialised=false;

        oldScaling = -1;
    }
    
    @Override
    public float getOldScaling() {
        return oldScaling;
    }

    @Override
    public void refreshDisplay() {
        throw new UnsupportedOperationException("refreshDisplay Not supported yet.");
    }

    @Override
    public void flushPageCaches() {}

    @Override
    public void init(final float scaling, final int displayRotation, int pageNumber, final DynamicVectorRenderer currentDisplay, final boolean isInit){

        if(pageNumber<1) {
            pageNumber = 1;
        }

        if(currentDisplay!=null) {
            this.currentDisplay = currentDisplay;
        }
        
        this.scaling=scaling;
        this.displayRotation=displayRotation;
        this.pageNumber=pageNumber;
        
        this.insetW=options.getInsetW();
        this.insetH=options.getInsetH();

        if(currentDisplay!=null) {
            currentDisplay.setInset(insetW, insetH);
        }

        //reset over-ride which may have been enabled
        pageData.setScalingValue(scaling); //ensure aligned
        
        if(isInit){
            setPageOffsets(this.pageNumber);
            isInitialised=true;
        }
        
        lastScaling=scaling;

    }
    
    @Override
    public void setThumbnailPanel(final GUIThumbnailPanel thumbnails) {
        this.thumbnails=thumbnails;

    }
    
    @Override
    public void stopGeneratingPage(){

        //request any processes die
        multiDisplayOptions.setIsGeneratingOtherPages(false);
        
        multiDisplayOptions.waitToDieThred();
    }
    
    @Override
    public int getYCordForPage(final int page){
        int[] yReached=multiDisplayOptions.getyReached();
        //int[] pageH=multiDisplayOptions.getPageH();
        
        if (yReached != null) {
        	//Prevent Continous Facing mode from shifting forward a page
            //when scaling from large to small
//            if(displayView==Display.CONTINUOUS_FACING){
//                return yReached[page] + insetH;// - pageH[page];
//            }else{
            	return yReached[page] + insetH;
           // }
        } else {
            return insetH;
        }
    }

    public int getStartPage() {
        return multiDisplayOptions.getStartViewPage();
        
    }

    public int getEndPage() {
        return multiDisplayOptions.getEndViewPage();
    }


    @Override
    public void setScaling(final float scaling) {
        this.scaling=scaling;
        if (pageData != null) {
            pageData.setScalingValue(scaling);
        }
    }
    
    @Override
    public void setHighlightedImage(final int[] highlightedImage) {
        this.highlightedImage = highlightedImage;
    }

    @Override
    public int[] getHighlightedImage(){
        return highlightedImage;
    }
    
    @Override
    public void drawBorder() {
        throw new UnsupportedOperationException("drawBorder Not supported yet."); 
    }
    
    @Override
    public int getXCordForPage(final int page){
        
        int[] xReached=multiDisplayOptions.getxReached();
        
        if (xReached != null) {
            return xReached[page] + insetW;
        } else {
            return insetW;
        }
    }
   
     /**
     * workout offsets so we  can draw pages
     * */
    @Override
    public void setPageOffsets(final int pageNumber) {

        final int pageCount=pageData.getPageCount();

        multiDisplayOptions.resetValues(pageCount);
        
        pageOffsetW = new int[pageCount+1];
        pageOffsetH = new int[pageCount+1];

        int heightCorrection;
        int displayRotation;

        isRotated=new boolean[pageCount+1];
        int gap= PageOffsets.pageGap;//set.pageGap*scaling);

        if (multiDisplayOptions.isTurnoverOn() &&
                pageCount != 2 &&
                !pageData.hasMultipleSizes() &&
                displayView == Display.FACING) {
            gap = 0;
        }

        //Used to help allign first page is page 2 is cropped / rotated
        int LmaxWidth=0;
        int LmaxHeight=0;
        int RmaxWidth=0;
        int RmaxHeight=0;
        
        int[] pageW=multiDisplayOptions.getPageW();
        int[] pageH=multiDisplayOptions.getPageH();
        /**work out page sizes - need to do it first as we can look ahead*/
        for(int i=1;i<pageCount+1;i++){

            /**
             * get unrotated page sizes
             */
            pageW[i]=pageData.getScaledCropBoxWidth(i);
            pageH[i]=pageData.getScaledCropBoxHeight(i);

            displayRotation=pageData.getRotation(i)+this.displayRotation;
            if(displayRotation>=360) {
                displayRotation -= 360;
            }

            //swap if this page rotated and flag
            if((displayRotation==90|| displayRotation==270)){
                final int tmp=pageW[i];
                pageW[i]=pageH[i];
                pageH[i]=tmp;

                isRotated[i]=true; //flag page as rotated
            }

            if((i&1)==1){
                if(pageW[i]>RmaxWidth) {
                    RmaxWidth = pageW[i];
                }
                if(pageH[i]>RmaxHeight) {
                    RmaxHeight = pageH[i];
                }
            }else{
                if(multiDisplayOptions.getPageW(i)>LmaxWidth) {
                    LmaxWidth = multiDisplayOptions.getPageW(i);
                }
                if(pageH[i]>LmaxHeight) {
                    LmaxHeight = pageH[i];
                }
            }
        }

        int[] xReached=multiDisplayOptions.getxReached();
        int[] yReached=multiDisplayOptions.getyReached();
         
        //loop through all pages and work out positions
        for(int i=1;i<pageCount+1;i++){
            heightCorrection = 0;
            if(((pageCount==2)&&(displayView==FACING || displayView==CONTINUOUS_FACING)) || (displayView ==FACING && !multiDisplayOptions.isSeparateCover())){ //special case
                //if only 2 pages display side by side
                if((i&1)==1){
                    xReached[i]=0;
                    yReached[i]=0;
                }else{
                    xReached[i]=xReached[i-1]+pageW[i-1]+gap;
                    yReached[i]=0;
                    if(!(i==2 && pageData.getRotation(1) == 270)) {
                        pageOffsetW[2] = (pageW[2] - pageW[1]) + pageOffsetW[1];
                        pageOffsetH[2] = (pageH[2] - pageH[1]) + pageOffsetH[1];
                    }
                }

            }else if(i==1){  //first page is special case
                //First page should be on the left so indent
                if(displayView==CONTINUOUS){
                    xReached[1]=0;
                    yReached[1]=0;
                    pageOffsetW[1]=0;
                    pageOffsetH[1]=0;
                    pageOffsetW[0]=gap; //put the gap values in the empty entry in the offset array. A bit bodgy!
                    pageOffsetH[0]=gap;

                }else if(displayView==CONTINUOUS_FACING){
                    pageOffsetW[0]=gap; //put the gap values in the empty entry in the offset array.  A bit bodgy!
                    pageOffsetH[0]=gap;
                    pageOffsetW[1]=0;
                    pageOffsetH[1]=0;
                    xReached[1]=LmaxWidth+gap;
                    yReached[1]=0;
                }else if(displayView==FACING) {
                    xReached[1]=pageW[1]+gap;
                    yReached[1]=0;
                }

            }else{
                //Calculate position for all other pages / cases
                if((displayView==CONTINUOUS_FACING)){

                    if(!(i>=2 &&
                            (((pageData.getRotation(i) == 270 || pageData.getRotation(i) == 90) &&
                            (pageData.getRotation(i-1) != 270 || pageData.getRotation(i-1) != 90))
                            || ((pageData.getRotation(i-1) == 270 || pageData.getRotation(i-1) == 90) &&
                            (pageData.getRotation(i) != 270 || pageData.getRotation(i) != 90))))) {
                        pageOffsetW[i] = (pageW[i] - pageW[i-1]) + pageOffsetW[i-1];
                        pageOffsetH[i] = (pageH[i] - pageH[i-1]) + pageOffsetH[i-1];
                    }

                    //Left Pages
                    if((i & 1)==0){
                        //Last Page rotated so correct height
                        if(i<pageCount) {
                            heightCorrection = (pageH[i + 1] - pageH[i]) / 2;
                        }
                        if(heightCorrection<0) {
                            heightCorrection = 0;//-heightCorrection;
                        }
                        if(i>3){
                            final int temp = (pageH[i-2]-pageH[i-1])/2;
                            if(temp>0) {
                                heightCorrection += temp;
                            }
                        }
                        yReached[i] = (yReached[i-1]+pageH[i-1] +gap)+heightCorrection;
                    }else{ //Right Pages
                        //Last Page rotated so correct height
                        heightCorrection = (pageH[i-1]-pageH[i])/2;
                        yReached[i] = (yReached[i-1])+heightCorrection;
                    }
                    
                    if((i & 1)==0){//Indent Left pages by diff between maxWidth and pageW (will only indent unrotated)
                        xReached[i] += (LmaxWidth-pageW[i]);
                    }else{//Place Right Pages with a gap (This keeps pages centered)
                        xReached[i] = xReached[i-1]+pageW[i-1] +gap;
                    }

                }else if(displayView==CONTINUOUS){
                    //Place page below last with gap
                    yReached[i] = (yReached[i-1]+pageH[i-1]+gap);

                    if(!(i>=2 &&
                            (((pageData.getRotation(i) == 270 || pageData.getRotation(i) == 90) &&
                            (pageData.getRotation(i-1) != 270 || pageData.getRotation(i-1) != 90))
                            || ((pageData.getRotation(i-1) == 270 || pageData.getRotation(i-1) == 90) &&
                            (pageData.getRotation(i) != 270 || pageData.getRotation(i) != 90))))) {
                        pageOffsetW[i] = (pageW[i] - pageW[i-1]) + pageOffsetW[i-1];
                        pageOffsetH[i] = (pageH[i] - pageH[i-1]) + pageOffsetH[i-1];
                    }

                }else if((displayView==FACING)){
                    if((i&1)==1){ //If right page, place on right with gap
                        xReached[i] = (xReached[i-1]+pageW[i-1]+gap);
                        if(pageH[i] < pageH[i-1])//Drop page down to keep pages centred
                        {
                            yReached[i] += (((pageH[i - 1] - pageH[i]) / 2));
                        }
                    }else{ //If left page, indent by diff of max and current page
                        xReached[i] = 0;
                        if(i<pageCount && (pageH[i] < pageH[i + 1])) {
                          //Drop page down to keep pages centered
                           
                                yReached[i] += ((pageH[i + 1] - pageH[i]) / 2);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void dispose() {
        
        currentOffset=null;
        multiDisplayOptions.setPageValuesToNull();
        this.isRotated=null;

    }
    
    /**
     * Please use public int[] getCursorBoxOnScreenAsArray() instead.
     * @deprecated on 04/07/2014
     */
    @Override
    public Rectangle getCursorBoxOnScreen() {
        throw new UnsupportedOperationException("Please use public int[] getCursorBoxOnScreenAsArray() instead");
    }
    

    @Override
    public int[] getCursorBoxOnScreenAsArray() {
        throw new UnsupportedOperationException("getCursorBoxOnScreenAsArray Not supported yet.");
    }

    @Override
    public double getIndent() {
        return indent;
    }

    @Override
    public void forceRedraw() {

        lastFormPage = -1;
        lastEnd = -1;
        lastStart = -1;
    }

    /**
     * initialise panel and set size to display during updates and update the AffineTransform to new values<br>
     */
    @Override
    public void setPageRotation(int newRotation) {


        //assume unrotated for multiple views and rotate on a page basis
        if(displayView!=Display.SINGLE_PAGE) {
            newRotation = 0;
        }

        pageUsedForTransform= pageNumber;
        if(displayView!=Display.SINGLE_PAGE && displayView!=Display.FACING){
            displayScalingDbl = ScalingFactory.getScalingForImage(1, 0, scaling,pageData);//(int)(pageData.getCropBoxWidth(pageNumber)*scaling),(int)(pageData.getCropBoxHeight(pageNumber)*scaling),
        }else{
            displayScalingDbl = ScalingFactory.getScalingForImage(pageNumber,newRotation, scaling, pageData);//(int)(pageData.getCropBoxWidth(pageNumber)*scaling),(int)(pageData.getCropBoxHeight(pageNumber)*scaling),
        }
      
        final int insetW=options.getInsetW();
        final int insetH=options.getInsetH();
        
        // Affine transformations
        if(newRotation == 90){
            
            displayScalingDbl[4] += ((insetW/scaling) * displayScalingDbl[1] );
            displayScalingDbl[5] += ((insetH/scaling) * displayScalingDbl[2] );
        }else if(newRotation == 270){
            displayScalingDbl[4] += ((-insetW/scaling) * displayScalingDbl[1] );
            displayScalingDbl[5] += ((-insetH/scaling) * displayScalingDbl[2] );
        }else if(newRotation == 180){
            displayScalingDbl[4] += ((-insetW/scaling) * displayScalingDbl[0] );
            displayScalingDbl[5] += ((insetH/scaling) * displayScalingDbl[3] );
        }else{
            displayScalingDbl[4] += ((insetW/scaling) * displayScalingDbl[0] );
            displayScalingDbl[5] += ((-insetH/scaling) * displayScalingDbl[3] );
        }
        
        //force redraw if screen being cached
        refreshDisplay();
    }

    @Override
    public void resetViewableArea() {
        throw new UnsupportedOperationException("resetViewableArea Not supported yet.");
    }

    @Override
    public void paintPage(final Graphics2D g2, final AcroRenderer formRenderer, final TextLines textLines) {
        throw new UnsupportedOperationException("paintPage not supported yet.");
    }

    /**
     * Deprecated on 04/07/2014, please use 
     * updateCursorBoxOnScreen(int[] newOutlineRectangle, int outlineColor, int pageNumber,int x_size,int y_size) instead.
     * @deprecated
     */
    @Override
    public void updateCursorBoxOnScreen(final Rectangle newOutlineRectangle, final Color outlineColor, final int pageNumber, final int x_size, final int y_size) {
        throw new UnsupportedOperationException("please use updateCursorBoxOnScreen(int[] newOutlineRectangle, int outlineColor, int pageNumber,int x_size,int y_size) instead");
    }
    
    @Override
    public void updateCursorBoxOnScreen(final int[] newOutlineRectangle, final int outlineColor, final int pageNumber, final int x_size, final int y_size){
        throw new UnsupportedOperationException("updateCursorBoxOnScreen Not supported yet."); 
    }

    @Override
    public void drawCursor(final Graphics g, final float scaling) {
        throw new UnsupportedOperationException("drawCursor Not supported yet.");
    }

    /**
     * Deprecated on 07/07/2014
     * Please use setViewableArea(int[] viewport) instead.
     * @deprecated
     */
    @Override
    public AffineTransform setViewableArea(final Rectangle viewport) throws PdfException {
        throw new UnsupportedOperationException("setViewableArea Not supported yet."); 
    }
    
    /**
     * NOT PART OF API
     *
     * allows the user to create a viewport within the displayed page, the
     * aspect ratio is keep for the PDF page <br>
     * <br>
     * Passing in a null value is the same as calling resetViewableArea()
     * <br>
     * <br>
     * The viewport works from the bottom left of the PDF page <br>
     * The general formula is <br>
     * (leftMargin, <br>
     * bottomMargin, <br>
     * pdfWidth-leftMargin-rightMargin, <br>
     * pdfHeight-bottomMargin-topMargin)
     * <br>
     * The viewport will not be incorporated in printing <br>
     * <br>
     * Throws PdfException if the viewport is not totally enclosed within the
     * 100% cropped pdf
     */
    @Override
    public AffineTransform setViewableArea(final int[] viewport) throws PdfException{
        throw new UnsupportedOperationException("setViewableArea Not supported yet.");
    }
    
    @Override
    public void drawFacing(final Rectangle visibleRect) {
        throw new UnsupportedOperationException("drawFacing Not supported yet.");
    }

    @Override
    public void paintPage(final Pane box, final AcroRenderer formRenderer, final TextLines textLines) {
        throw new UnsupportedOperationException("paintPage Not supported yet.");
    }
    
    public void setCurrentDisplay(final DynamicVectorRenderer pageView) {
       this.currentDisplay=pageView;
    }
    
    public int getDisplayRotation() {
        return displayRotation;
    }
    
    public int getRx() {
       return rx;
    }
    
    public int getRy() {
       return ry;
    }
    
    public int getRw() {
       return rw;
    }
    
    public int getRh() {
       return rh;
    }
    
    
    public int getInsetW() {
        return insetW;
    }
    
    public int getInsetH() {
        return insetH;
    }
    
    
    @Override
    public Rectangle getDisplayedRectangle() {
        throw new UnsupportedOperationException("getDisplayedRectangle Not supported yet.");
    }
    
    public Rectangle getDisplayedRectangle(final boolean isShowing,final Rectangle userAnnot) {

        //get raw rectangle
        rx =userAnnot.x;
        ry =userAnnot.y;
        rw =userAnnot.width;
        rh =userAnnot.height;

        //Best way I found to catch if pdf decoder is being used but never displayed
        if(!isShowing && (rw==0 || rh==0)){
            rx = 0;
            ry = 0;
            rw = pageData.getScaledCropBoxWidth(pageNumber);
            rh = pageData.getScaledCropBoxHeight(pageNumber);

            if(pageData.getRotation(pageNumber)%180!=0){
                rh = pageData.getScaledCropBoxWidth(pageNumber);
                rw = pageData.getScaledCropBoxHeight(pageNumber);
            }
        }

        return userAnnot;
    }
    
}
