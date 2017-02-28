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
 * JavaFXData.java
 * ---------------
 */
package org.jpedal.objects.acroforms.javafx;


import java.awt.*;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Transform;

import org.jpedal.display.Display;
import org.jpedal.objects.acroforms.*;
import org.jpedal.objects.raw.FormObject;

import org.jpedal.objects.raw.PdfDictionary;

/**
 * Swing specific implementation of Widget data
 * (all non-Swing variables defined in ComponentData)
 *
 */
public class JavaFXData extends GUIData {

    /**
     * panel components attached to
     */
    private Pane panel;

    /**
     * alter font and size to match scaling. Note we pass in compoent so we can
     * have multiple copies (needed if printing page displayed).
     */
    private void scaleComponent(final FormObject formObject,final int rotate, final Region curComp, final boolean redraw,int indent, final boolean isPrinting) {

        // Ignore scaling for now (JavaFX handles the scaling itself
        float scale = 1f;

        if (curComp == null || formObject.getPageNumber()==-1) {
            return;
        }

        final int curPage=formObject.getPageNumber();
        /**
         * work out if visible in Layer
         */
        if (layers != null) {

            final String layerName = formObject.getLayerName();

            // do not display
            if (layerName != null && layers.isLayerName(layerName)) {

                final boolean isVisible = layers.isVisible(layerName);
                curComp.setVisible(isVisible);
            }
        }

        final int[] bounds;

        if(formObject.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Popup && !isPrinting) {
            bounds = cropComponent(formObject, scale, rotate, redraw, true);
        } else {
            bounds = cropComponent(formObject, scale, rotate, redraw, false);
        }

        // factor in offset if multiple pages displayed
        if (xReached != null) {
            bounds[0] += xReached[curPage];
            bounds[1] += yReached[curPage];
        }

        final int pageWidth;
        final int pageHeight;
        if((pageData.getRotation(curPage)+rotate)%180==90){
            pageWidth = pageData.getCropBoxHeight(curPage);
            pageHeight = pageData.getCropBoxWidth(curPage);
        }else {
            pageWidth = pageData.getCropBoxWidth(curPage);
            pageHeight = pageData.getCropBoxHeight(curPage);
        }

        if(displayView==Display.CONTINUOUS){
            final double newIndent;
            if(rotate==0 || rotate==180) {
                newIndent = (widestPageNR - (pageWidth)) / 2;
            } else {
                newIndent = (widestPageR - (pageWidth)) / 2;
            }

            indent = (int)(indent + (newIndent*scale));
        }

        final int totalOffsetX = userX+indent;
        final int totalOffsetY = userY;

        curComp.setPrefWidth(bounds[2]);
        curComp.setPrefHeight(bounds[3]);
        curComp.setTranslateX(totalOffsetX + bounds[0]);
        curComp.setTranslateY((pageHeight-(totalOffsetY + bounds[1])));

        curComp.getTransforms().clear();
        curComp.getTransforms().add(Transform.affine(1, 0, 0, -1, 0, 0));

    }


    private int[] cropComponent(final FormObject formObject, final float s, int r, final boolean redraw, final boolean positionOnly){
        
        final Rectangle rect = formObject.getBoundingRectangle();
        final int curPage=formObject.getPageNumber();
        
        
        final float[] box= {rect.x,rect.y, rect.width + rect.x,rect.height + rect.y};
        
        //NOTE if needs adding in ULC check SpecialOptions.SINGLE_PAGE
        if(displayView!=Display.SINGLE_PAGE && displayView!=Display.NODISPLAY) {
            r = (r + pageData.getRotation(curPage)) % 360;
        }
        
        final int cropX = pageData.getCropBoxX(curPage);
        final int cropY = pageData.getCropBoxY(curPage);
        final int cropW = pageData.getCropBoxWidth(curPage);
        
        final int mediaW = pageData.getMediaBoxWidth(curPage);
        final int mediaH = pageData.getMediaBoxHeight(curPage);
        
        final int cropOtherX = (mediaW - cropW - cropX);
        
        float x100=0,y100=0,w100=0,h100=0;
        final int x;
        final int y;
        final int w;
        final int h;

        {
            switch(r){
                case 0:
                    
                    x100 = box[0];
                    //if we are drawing on screen take off cropX if printing or extracting we dont need to do this.
                    if (redraw) {
                        x100 -= cropX;
                    }
                    
                    y100 = mediaH - box[3]-cropOtherY[curPage];
                    w100 = (box[2] - box[0]);
                    h100 = (box[3] - box[1]);
                    
                    break;
                case 90:
                    
                    // new hopefully better routine
                    x100 = box[1]-cropY;
                    y100 = box[0]-cropX;
                    w100 = (box[3] - box[1]);
                    h100 = (box[2] - box[0]);
                    
                    break;
                case 180:
                    
                    // new hopefully better routine
                    w100 = box[2] - box[0];
                    h100 = box[3] - box[1];
                    y100 = box[1]-cropY;
                    x100 = mediaW-box[2]-cropOtherX;
                    
                    break;
                case 270:
                    
                    // new hopefully improved routine
                    w100 = (box[3] - box[1]);
                    h100 = (box[2] - box[0]);
                    x100 = mediaH -box[3]-cropOtherY[curPage];
                    y100 = mediaW-box[2]-cropOtherX;
                    
                    break;
            }/**/
        }
        
        x = (int) (x100*s);
        y = (int) (y100*s);
        if(!positionOnly){
            w = (int) (w100*s);
            h = (int) (h100*s);
        }else{
            //Don't forget to factor in the resolution of the display
            w = (int) (w100*dpi/72);
            h = (int) (h100*dpi/72);
        }
        return new int[]{x,y,w,h};
    }
    
    /**
     * used to remove all components from display
     */
    @Override
    protected void removeAllComponentsFromScreen() {

        if (panel != null) {
            if(Platform.isFxApplicationThread()){
                panel.getChildren().clear();
            }else{
                final Runnable doPaintComponent = new Runnable() {
                    @Override public void run() {
                        panel.getChildren().clear();
                    }
                };
                Platform.runLater(doPaintComponent);
            }
        }
        
    }
    
    /**
     * pass in object components drawn onto
     * @param rootComp
     */
    @Override
    public void setRootDisplayComponent(final Object rootComp) {
        panel = (Pane)rootComp;
    }
    
    @Override
    public void setGUIComp(final FormObject formObject, final Object rawField) {

        final Region retComponent=(Region) rawField;

        // append state to name so we can retrieve later if needed
        String name2 = formObject.getTextStreamValue(PdfDictionary.T);
        if (name2 != null) {// we have some empty values as well as null
            final String stateToCheck = formObject.getNormalOnState();
            if (stateToCheck != null && !stateToCheck.isEmpty()) {
                name2 = name2 + "-(" + stateToCheck + ')';
            }
            
            retComponent.setId(name2);
        }
        
        // make visible
        scaleComponent(formObject, rotation, retComponent, true, indent, false);
        
    }
    
    /**
     * alter location and bounds so form objects show correctly scaled
     */
    @Override
    public void resetScaledLocation(final float currentScaling, final int currentRotation, final int currentIndent) {

        // we get a spurious call in linux resulting in an exception
        if (formsUnordered == null || panel==null || startPage==0) {
            return;
        }

        // needed as code called recursively otherwise
//        if (forceRedraw || currentScaling != lastScaling || currentRotation != oldRotation || currentIndent != oldIndent){// || SwingUtilities.isEventDispatchThread()) {
            
            oldRotation = currentRotation;
            lastScaling = currentScaling;
            oldIndent = currentIndent;
            forceRedraw=false;
            
            FormObject formObject;
            Region rawComp;
            int count;

            for(int currentPage=startPage;currentPage<endPage;currentPage++){
                
                if(formsOrdered[currentPage]==null) {
                    count = 0;
                } else {
                    count = formsOrdered[currentPage].size();
                }

                // reset all locations
                for (int j=0;j<count;j++) {

                    formObject = formsOrdered[currentPage].get(count-1-j);
                    formObject.setCurrentScaling(currentScaling);

                    rawComp= (Region) formObject.getGUIComponent();

                    // scaleComponent here seems to mess up the scaling so I've left it out for the time being
                    if (Platform.isFxApplicationThread()){

                        if(rawComp!=null){
                            panel.getChildren().remove(rawComp);
//                            scaleComponent(formObject,currentScaling, currentRotation, rawComp, true, indent, false);
                            panel.getChildren().add(rawComp);

                        }
                    }else {
                        final Region finalComp=rawComp;
                        //final FormObject fo=formObject;
                        final Runnable addControl = new Runnable() {
                            @Override
                            public void run() {

                                panel.getChildren().remove(finalComp);
//                                scaleComponent(fo,currentScaling, currentRotation, finalComp, true, indent, false);
                                panel.getChildren().add(finalComp);

                            }
                        };
                        Platform.runLater(addControl);
                    }
                }
            }
//        }
    }

    @Override
    protected void displayComponent(final FormObject formObject, final Object comp) {

        if (Platform.isFxApplicationThread()) {

            scaleComponent(formObject, rotation, (Region) comp, true, indent, false);

        } else {

            final Runnable doPaintComponent = new Runnable() {
                @Override
                public void run() {
                    scaleComponent(formObject, rotation, (Region) comp, true, indent, false);
                }
            };
            Platform.runLater(doPaintComponent);
        }
    }
}
