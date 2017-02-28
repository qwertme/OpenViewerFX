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
 * SingleDisplayFX.java
 * ---------------
 */
package org.jpedal.display.javafx;


import org.jpedal.PdfDecoderFX;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import org.jpedal.display.Display;
import org.jpedal.display.GUIDisplay;
import org.jpedal.examples.viewer.commands.javafx.JavaFXPreferences;
import org.jpedal.examples.viewer.gui.*;
import org.jpedal.external.Options;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.FXDisplay;
import org.jpedal.text.TextLines;

/**
 *
 * JaveFX version
 */
public class SingleDisplayFX extends GUIDisplay implements Display {
    
    final PdfDecoderFX pdf;
    
    public SingleDisplayFX(int pageNumber, final DynamicVectorRenderer currentDisplay, final PdfDecoderFX pdf, final DecoderOptions options) {

        if(pageNumber<1) {
            pageNumber = 1;
        }

        this.pageNumber=pageNumber;
        this.currentDisplay=currentDisplay;
        this.pdf=pdf;
        
        this.options=options;

        displayOffsets=pdf.getDisplayOffsets();
        
        pageData=pdf.getPdfPageData();
        
    }

    public SingleDisplayFX(final PdfDecoderFX pdf, final DecoderOptions options) {
        
        this.pdf=pdf;
        
        this.options=options;
        
        displayOffsets=pdf.getDisplayOffsets();
        
        pageData=pdf.getPdfPageData();
    }
    
    @Override
    public void refreshDisplay(){
       
        if(Platform.isFxApplicationThread()){
            if(displayScalingDbl!=null){
                pdf.getTransforms().setAll(Transform.affine(displayScalingDbl[0],displayScalingDbl[1],displayScalingDbl[2],displayScalingDbl[3],displayScalingDbl[4],displayScalingDbl[5]));
            }
            
            if(currentDisplay!=null){
                
                //init(scaling, displayRotation, pageNumber, currentDisplay, true);
                paintPage(pdf.highlightsPane, pdf.getFormRenderer(), null);
                
            }
        }else{
            //Ensure dialog is handled on FX thread
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    
                    if(displayScalingDbl!=null){
                        pdf.getTransforms().setAll(Transform.affine(displayScalingDbl[0],displayScalingDbl[1],displayScalingDbl[2],displayScalingDbl[3],displayScalingDbl[4],displayScalingDbl[5]));
                    }
                    
                    if(currentDisplay!=null){
                        
                        //init(scaling, displayRotation, pageNumber, currentDisplay, true);
                        paintPage(pdf.highlightsPane, pdf.getFormRenderer(), null);
                        
                    }                    
                }
            });
        }
    }
    

    
    /**
     * initialise panel and set size to display during updates and update the AffineTransform to new values<br>
     */
    @Override
    public void setPageRotation(final int newRotation) {
            
        super.setPageRotation(newRotation);
        
        //force redraw if screen being cached
        refreshDisplay();
    }
    
//    private Path getBorder() {
//
//        Path border=null;
//        
//        if(pdf.isBorderPresent() && crw >0 && crh >0){
//            
//            border=getBorder(crw,crh);
//            
//           // if(!FXDisplay.useCanvas){
//          //      border.setFill(Color.WHITE);
//          //  }
//            
//            //Border myBorder=pdf.getBorder();
//            
////            if((crw >0)&&(crh >0)&&(myBorder!=null)){
////                myBorder.paintBorder(pdf,g2,crx-myBorder.getBorderInsets(pdf).left, cry-myBorder.getBorderInsets(pdf).bottom, crw+myBorder.getBorderInsets(pdf).left+myBorder.getBorderInsets(pdf).right, crh+myBorder.getBorderInsets(pdf).bottom+myBorder.getBorderInsets(pdf).top);
////            }
//        }
//        
//        return border;
//    }

    static Path getBorder(int crw, int crh) {
        
        final Path border=new Path();
        
        border.getElements().add(new MoveTo(-1,-1));
        border.getElements().add(new LineTo(crw+2,-1));
        border.getElements().add(new LineTo(crw+2,crh+2));
        border.getElements().add(new LineTo(-1,crh+2));
        border.getElements().add(new ClosePath());
        border.setStroke(Color.rgb(0,0,0));
        
        return border;
        
        //  myBorder.paintBorder(pdf,g2,crx-myBorder.getBorderInsets(pdf).left, cry-myBorder.getBorderInsets(pdf).bottom, crw+myBorder.getBorderInsets(pdf).left+myBorder.getBorderInsets(pdf).right, crh+myBorder.getBorderInsets(pdf).bottom+myBorder.getBorderInsets(pdf).top);
    }
    
    @Override
    /**
     * Resets the FXPane when we open a new PDF
     */
    public void disableScreen() {
        if (currentDisplay != null) {
            
            final Group FXpane = ((FXDisplay) currentDisplay).getFXPane();
           
            if (pdf.getChildren().contains(FXpane)) {

                final int count = pdf.getChildren().size();

                for (int i = 0; i < count; i++) {
                    pdf.getChildren().remove(0);
                }
            }
        }
    }
    
    @Override
    public void paintPage(final Pane box, final AcroRenderer formRenderer, final TextLines textLines) {
        final boolean debugPane=false;
        
        final Group fxPane=((FXDisplay)currentDisplay).getFXPane();
        String pageNumberStr = String.valueOf(pageNumber);
        
        final Rectangle clip = new Rectangle(crx,cry,crw,crh);
        clip.setFill(Color.WHITE);
        
        
// Remove box from the current node it belongs to - avoids duplication errors
        if(box != null && box.getParent() != null) {
            ((Group) box.getParent()).getChildren().remove(box);
        }
        fxPane.getChildren().addAll(box);
        pdf.setPrefSize(crw, crh);
        
        if(displayView==SINGLE_PAGE){
            pdf.getChildren().clear();
            
            if(formRenderer.isXFA()){
                //draw wihte background border on xfa contents
                final Path border = getBorder(crw, crh);
                border.setFill(Color.WHITE);
                pdf.getChildren().addAll(border);
                border.setLayoutX(crx);
                border.setLayoutY(cry);
            }
            
            if(!pdf.getChildren().contains(fxPane)){
                pdf.getChildren().addAll(fxPane);
            }
            
            fxPane.setLayoutX(-crx);
            fxPane.setLayoutY(-cry);
        }else{
                                    
            Node pagePath = null;
            
            for (Node child : pdf.getChildren()) {
                if(child.getId()!=null && child.getId().equals(pageNumberStr)){
                    if(child instanceof Path){
                        pagePath = child;
                    }
                }
            }
            
            if(pagePath!=null){
                pdf.getChildren().remove(pagePath);
            }      
            
            fxPane.setId(pageNumberStr);
            if(!pdf.getChildren().contains(fxPane)){
                pdf.getChildren().addAll(fxPane);
            }
            
            
            final int[] xReached= multiDisplayOptions.getxReached();
            final int[] yReached= multiDisplayOptions.getyReached();
            //final int[] pageW=multiDisplayOptions.getPageW();
            //final int[] pageH=multiDisplayOptions.getPageH();
            
            int cx,cy,j=pageNumber;
            
            cx=(int)(xReached[j]/scaling);
            cy=(int)(yReached[j]/scaling);
            
            //code works differently in Swing and FX so needs reversing
            if(displayView==CONTINUOUS_FACING){
                cx=currentOffset.getWidestPageR()-cx;
            }
            
            fxPane.setLayoutX(-cx);
            fxPane.setLayoutY(pdf.getHeight()-cy);
            
        }
        
        if(!debugPane){
            clip.setFill(Color.WHITE);
            fxPane.setClip(clip);
        }else{
            //Debug Different GUI Display Panes
            clip.setFill(Color.BLUE);
            clip.setOpacity(0.5);
            fxPane.getChildren().add(clip);
            
            pdf.setStyle("-fx-background-color: red;");
            pdf.getParent().setStyle("-fx-background-color: yellow;");
            fxPane.setStyle("-fx-background-color: green;");
        }
        
        addForms(formRenderer);
   
        //
        /**/
        
    }
    
    private void addForms(final AcroRenderer formRenderer) {
        int start=pageNumber,end=pageNumber;
        //control if we display forms on multiple pages
        if(displayView!=Display.SINGLE_PAGE){
            start= getStartPage();
            end= getEndPage();
            if(start==0 || end==0 || lastEnd!=end || lastStart!=start) {
                lastFormPage = -1;
            }

            lastEnd=end;
            lastStart=start;

        }
        if((lastFormPage!=pageNumber) && (formRenderer != null)){

                formRenderer.displayComponentsOnscreen(start,end);

                //switch off if forms for this page found
                if(formRenderer.getCompData().hasformsOnPageDecoded(pageNumber)) {
                    lastFormPage = pageNumber; //ensure not called too early
                }
        }
        // Add the forms to the Pane
        if(formRenderer!=null && currentOffset !=null){ //if all forms flattened, we can get a null value for currentOffset so avoid this case
            formRenderer.getCompData().setPageValues(scaling,displayRotation,(int)indent,displayOffsets.getUserOffsetX(), displayOffsets.getUserOffsetY(),displayView,currentOffset.getWidestPageNR(), currentOffset.getWidestPageR());
            formRenderer.getCompData().resetScaledLocation(scaling,displayRotation,(int)indent);//indent here does nothing.
        }
    }
    
    @Override
    public void init(final float scaling, final int displayRotation, final int pageNumber, final DynamicVectorRenderer currentDisplay, final boolean isInit){

        this.pageData=pdf.getPdfPageData();
        
        super.init(scaling,  displayRotation, pageNumber, currentDisplay, isInit);
        
        setPageSize(pageNumber, scaling);

        lastFormPage = -1;
//        if(displayView==SINGLE_PAGE){
//            
//            int[] singlePageSize=pdf.getMaximumSize();
//            pdf.setMinSize(singlePageSize[0], singlePageSize[1]);
//        
//        }
    }   
    
    /**
     * Overridden here as we don't need to scale/rotate as that is done using FX transforms
     */
    @Override
    public void setPageSize(final int pageNumber, final float scaling) {
         
        /**
         *handle clip - crop box values
         */
        pageData.setScalingValue(scaling); //ensure aligned
        topW=pageData.getCropBoxWidth(pageNumber);
        topH=pageData.getCropBoxHeight(pageNumber);
        final double mediaH=pageData.getMediaBoxHeight(pageNumber);

        cropX=pageData.getCropBoxX(pageNumber);
        cropY=pageData.getCropBoxY(pageNumber);
//        int mediaX = pageData.getMediaBoxX(pageNumber);
        //int mediaY = pageData.getMediaBoxY(pageNumber);
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
        int offsetY=(int) (mediaH-cropH);
        // Adjust the offset more in cases like costena
        if(!pageData.getMediaValue(pageNumber).isEmpty()){
            offsetY -= pageData.getMediaBoxHeight(pageNumber) - (int)(cropY + cropH) - pageData.getCropBoxY(pageNumber);
        }

        crw =(int)(cropW);
        crh =(int)(cropH);

        cry += offsetY;
    }
    
    /**rectangle drawn on screen by user*/
    private int[] cursorBoxOnScreen;
    //private Rectangle lastCursorBoxOnScreen;
        
    @Override
    public int[] getCursorBoxOnScreenAsArray(){
        return cursorBoxOnScreen;
    }
    
    
    Rectangle cursorRect;
    final Pane cursorBoxPane = new Pane();

    @Override
    public void updateCursorBoxOnScreen(final int[] newOutlineRectangle, final int outlineColor, final int pageNumber, final int x_size, final int y_size) {
                        
        if (displayView != Display.SINGLE_PAGE && getPageSize(displayView)[0] == 0 && getPageSize(displayView)[1] == 0) {
            return;
        }
        
        if (newOutlineRectangle != null) {

            int x = newOutlineRectangle[0];
            int y = newOutlineRectangle[1];
            int w = newOutlineRectangle[2];
            int h = newOutlineRectangle[3];

            final int cropX = pageData.getCropBoxX(pageNumber);
            final int cropY = pageData.getCropBoxY(pageNumber);
           // final int cropW = pageData.getCropBoxWidth(pageNumber);
            //final int cropH = pageData.getCropBoxHeight(pageNumber);

            //allow for odd crops and correct
//            if (y > 0 && y < (cropY)) {
//                y += cropY;
//            }
//
//            if (x < cropX) {
//                final int diff = cropX - x;
//                w -= diff;
//                x = cropX;
//            }
//
//            if (y < cropY) {
//                final int diff = cropY - y;
//                h -= diff;
//                y += diff;
//            }
//            if ((x + w) > cropW + cropX) {
//                w = cropX + cropW - x;
//            }
//            if ((y + h) > (cropY + cropH)) {
//                h = cropY + cropH - y;
//            }
            
            
            y -= cropY;
            x -= cropX;
            cursorBoxOnScreen = new int[]{x, y, w, h};
            
            if(DecoderOptions.showMouseBox){
                //Setup Cursor box.
                cursorRect = new Rectangle(x, y, w, h);
                cursorRect.setStroke(JavaFXPreferences.shiftColorSpaceToFX(outlineColor));
                cursorRect.setFill(Color.TRANSPARENT);

                //Draw Cursor box.
                if(pdf.getChildren().contains(cursorBoxPane)){
                    cursorBoxPane.getChildren().clear();
                    cursorBoxPane.getChildren().add(cursorRect);
                    pdf.getChildren().remove(cursorBoxPane);
                }
                pdf.getChildren().add(cursorBoxPane);
            }
        } else {
            cursorBoxOnScreen = null;
            if (pdf.getChildren().contains(cursorBoxPane)) {
                cursorBoxPane.getChildren().clear();
                pdf.getChildren().remove(cursorBoxPane);
            }
        }
    }
    
    @Override
    public java.awt.Rectangle getDisplayedRectangle() {
       
        final ScrollPane customFXHandle= ((JavaFxGUI)pdf.getExternalHandler(Options.MultiPageUpdate)).getPageContainer();
        
        if(customFXHandle==null){
            return new java.awt.Rectangle(0,0,0,0);
        }
        
        Bounds bounds = customFXHandle.getViewportBounds();
       
        return getDisplayedRectangle(true,new java.awt.Rectangle((int)bounds.getMinX(),(int)-bounds.getMinY(),(int)bounds.getWidth(),(int)bounds.getHeight()));
    }
}
