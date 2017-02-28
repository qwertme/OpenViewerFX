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
 * JavaFXScrollListener.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx;

import java.awt.image.BufferedImage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author Simon
 */
public class JavaFXScrollListener implements ChangeListener<Number>, EventHandler<MouseEvent> {
    private final Timeline trapMultipleMovements;
    private boolean requestMade;
    // Avoid StackOverflowExceptions
    private boolean setValueLocally;
    private final GUI gui;
    private int nextPage=-1, lastPageSent=-1;
    private boolean decodeLock;
    private final ScrollBar scroll;
    public BufferedImage lastImage;
    
    public JavaFXScrollListener(final GUI gui, final ScrollBar callback) {
        requestMade = false;
        setValueLocally = false;
        this.gui = gui;
        scroll = callback;
        this.trapMultipleMovements = new Timeline(new KeyFrame(Duration.millis(250), new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent event) {
                if(!requestMade){
                    requestMade = true;
                    if(nextPage > 0){
                        decodeLock = true;
//                        gotoPage(nextPage);
//                        gui.getPdfDecoder().waitForDecodingToFinish();
                        setThumbnail();
                        decodeLock = false;
                        
                    }
                    requestMade = false;
                }
            }
        }));
        
        scroll.setOnMouseReleased(this);
    }
    
    private void gotoPage(final int page){
        gui.getCommand().executeCommand(Commands.GOTO, new Object[]{Integer.toString(page)});
    }
    
    @Override
    public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
        if(setValueLocally || newValue == null){
            setValueLocally = false;
            return;
        }
        
        final int newPage = newValue.intValue()+1;
        
        if(decodeLock){
            setValueLocally = true;
            scroll.setValue(oldValue.intValue());
            return;
        }
        
        if(newValue.intValue() >= 0 && nextPage != newPage){
            nextPage = newPage <= gui.getPdfDecoder().getPageCount() ? newPage : gui.getPdfDecoder().getPageCount();
            
            if(trapMultipleMovements.getStatus() == Animation.Status.RUNNING) {
                trapMultipleMovements.stop();
            }
            
            //Only start timer to display preview if mouse has been used
            if(scroll.isPressed()){
                trapMultipleMovements.setCycleCount(1);
                trapMultipleMovements.playFromStart();
            }
            
        }
    }
    
    public synchronized void setThumbnail() {

        if (lastPageSent != nextPage) {

            lastPageSent = nextPage;

            try {

                final BufferedImage image = ((GUIThumbnailPanel) gui.getThumbnailPanel()).getImage(nextPage);

                //Store and turn off using stored image
                lastImage = image;
                gui.getPdfDecoder().setPreviewThumbnail(image, "Page " + nextPage + " of " + gui.getPdfDecoder().getPageCount());

                gui.getPdfDecoder().repaintPane(nextPage);

            } catch (final Exception ee) {
                //
                if (LogWriter.isOutput()) {
                    LogWriter.writeLog("Caught an Exception " + ee);
                }
            }
        }
    }

    @Override
    public void handle(MouseEvent event) {
        if (!requestMade) {
            requestMade = true;
            if (nextPage > 0) {
                decodeLock = true;
                
                //Prevents preview creation is no preview shown but page change
                lastPageSent=nextPage;
                
                gotoPage(nextPage);
                gui.setPageNumber();
                gui.getPdfDecoder().waitForDecodingToFinish();
                decodeLock = false;

            }
            requestMade = false;
        }
    }
}
