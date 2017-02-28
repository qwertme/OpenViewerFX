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
 * JavaFXMousePageTurn.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.javafx;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Light.Point;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.jpedal.PdfDecoderFX;
import org.jpedal.display.Display;
import org.jpedal.display.DisplayOffsets;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.javafx.JavaFXPageNavigator;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.JavaFxGUI;
import org.jpedal.examples.viewer.gui.MouseSelector;
import org.jpedal.external.AnnotationHandler;
import org.jpedal.external.Options;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.utils.LogWriter;

public class JavaFXMousePageTurn extends MouseSelector implements JavaFXMouseFunctionality {

    private final PdfDecoderFX decode_pdf;
    private final GUIFactory currentGUI;
    private final Values commonValues;
    private final Commands currentCommands;

    private long lastPress;

    /**
     * allow turning page to be drawn
     */
    private boolean drawingTurnover;

    /**
     * show turning page when hovering over corner
     */
    private boolean previewTurnover;

    /**
     * middle drag panning values
     */
    private double middleDragStartX, middleDragStartY, xVelocity, yVelocity;
    private Timer middleDragTimer;

    long timeOfLastPageChange;
    
    final DisplayOffsets offsets;

    public JavaFXMousePageTurn(final PdfDecoderFX decode_pdf, final GUIFactory currentGUI,
            final Values commonValues, final Commands currentCommands) {

        this.decode_pdf = decode_pdf;
        this.currentGUI = currentGUI;
        this.commonValues = commonValues;
        this.currentCommands = currentCommands;

        offsets=(DisplayOffsets) decode_pdf.getExternalHandler(Options.DisplayOffsets);
    }

    /**
     * checks the link areas on the page and allow user to save file
     *
     */
    public void checkLinks(final boolean mouseClicked, final PdfObjectReader pdfObjectReader, final double x, final double y) {

        //get 'hotspots' for the page
        final Map objs = currentGUI.getHotspots();

        //look for a match and call code
        if (objs != null) {
            ((AnnotationHandler) decode_pdf.getExternalHandler(Options.UniqueAnnotationHandler)).checkLinks(objs, mouseClicked, pdfObjectReader, (int) x, (int) y, currentGUI, commonValues);
        }
    }

    @Override
    public void mouseClicked(final MouseEvent e) {

        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE
                && e.getButton().equals(MouseButton.PRIMARY)
                && decode_pdf.getExternalHandler(Options.UniqueAnnotationHandler) != null) {
            final double mouseX = e.getX();
            final double mouseY = e.getY();
            final int[] pos = updateXY((int)mouseX, (int)mouseY, decode_pdf, commonValues);
            checkLinks(true, decode_pdf.getIO(), pos[0], pos[1]);
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        //Stub
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        //Stub
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        //Activate turnover if pressed while preview on
        if (previewTurnover && decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView() == Display.FACING
                && e.getButton().equals(MouseButton.PRIMARY)) {
            drawingTurnover = true;
            //set cursor
            decode_pdf.setCursor(Cursor.CLOSED_HAND);
            lastPress = System.currentTimeMillis();
        }

        //Start dragging
        if (e.getButton().equals(MouseButton.SECONDARY)) {
            middleDragStartX = e.getX() - decode_pdf.getLayoutX();
            middleDragStartY = e.getY() - decode_pdf.getLayoutY();
            decode_pdf.setCursor(Cursor.MOVE);

            //set up timer to refresh display
            if (middleDragTimer == null) {
                middleDragTimer = new Timer();
            }
            middleDragTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (xVelocity < -2) {
                                if (yVelocity < -2) {
                                    decode_pdf.setCursor(Cursor.NW_RESIZE);
                                } else if (yVelocity > 2) {
                                    decode_pdf.setCursor(Cursor.SW_RESIZE);
                                } else {
                                    decode_pdf.setCursor(Cursor.E_RESIZE);
                                }
                            } else if (xVelocity > 2) {
                                if (yVelocity < -2) {
                                    decode_pdf.setCursor(Cursor.NE_RESIZE);
                                } else if (yVelocity > 2) {
                                    decode_pdf.setCursor(Cursor.SE_RESIZE);
                                } else {
                                    decode_pdf.setCursor(Cursor.E_RESIZE);
                                }
                            } else {
                                if (yVelocity < -2) {
                                    decode_pdf.setCursor(Cursor.N_RESIZE);
                                } else if (yVelocity > 2) {
                                    decode_pdf.setCursor(Cursor.S_RESIZE);
                                } else {
                                    decode_pdf.setCursor(Cursor.MOVE);
                                }
                            }

                        }
                    });
                }
            }, 100);
            //middleDragTimer.start();
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        //Stop drawing turnover
        if (decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView() == Display.FACING) {
            drawingTurnover = false;

            final boolean dragLeft = offsets.getDragLeft();
            final boolean dragTop = offsets.getDragTop();

            if (lastPress + 200 > System.currentTimeMillis()) {
                if (dragLeft) {
                    currentCommands.executeCommand(Commands.BACKPAGE, null);
                } else {
                    currentCommands.executeCommand(Commands.FORWARDPAGE, null);
                }
                previewTurnover = false;
                decode_pdf.setCursor(Cursor.DEFAULT);
            } else {
                //Trigger fall
                final Point corner = new Point();
                corner.setY(decode_pdf.getInsetH());
                if (!dragTop) {
                    corner.setY(corner.getY() + (decode_pdf.getPdfPageData().getCropBoxHeight(1) * decode_pdf.getScaling()));
                }

                if (dragLeft) {
                    corner.setX((decode_pdf.getBoundsInLocal().getWidth() / 2) - (decode_pdf.getPdfPageData().getCropBoxWidth(1) * decode_pdf.getScaling()));
                } else {
                    corner.setX((decode_pdf.getBoundsInLocal().getWidth() / 2) + (decode_pdf.getPdfPageData().getCropBoxWidth(1) * decode_pdf.getScaling()));
                }

                final Point cursorPoint = new Point();
                cursorPoint.setX(e.getX());
                cursorPoint.setY(e.getY());
                testFall(corner, cursorPoint, dragLeft);
            }
        }

        //stop middle click panning
        if (e.getButton().equals(MouseButton.SECONDARY)) {
            xVelocity = 0;
            yVelocity = 0;
            decode_pdf.setCursor(Cursor.DEFAULT);
            middleDragTimer.cancel();
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (e.getButton().equals(MouseButton.PRIMARY)) {
            if (decode_pdf.getExternalHandler(Options.UniqueAnnotationHandler) != null) {
                final int[] pos = updateXY((int)e.getX(), (int)e.getY(), decode_pdf, commonValues);
                checkLinks(true, decode_pdf.getIO(), pos[0], pos[1]);
            }

            //update mouse coords for turnover
            if (decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && (drawingTurnover || previewTurnover) && decode_pdf.getDisplayView() == Display.FACING) {
                decode_pdf.setCursor(Cursor.CLOSED_HAND);

                //update coords
                if (offsets.getDragLeft()) {
                    if (offsets.getDragTop()) {
                        decode_pdf.setUserOffsets((int) e.getX(), (int) e.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_LEFT);
                    } else {
                        decode_pdf.setUserOffsets((int) e.getX(), (int) e.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT);
                    }
                } else {
                    if (offsets.getDragTop()) {
                        decode_pdf.setUserOffsets((int) e.getX(), (int) e.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_RIGHT);
                    } else {
                        decode_pdf.setUserOffsets((int) e.getX(), (int) e.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT);
                    }
                }
            }

        } else if (e.getButton().equals(MouseButton.MIDDLE)) {
            //middle drag - update velocity
            xVelocity = ((e.getX() - decode_pdf.getLayoutX()) - middleDragStartX) / 4;
            yVelocity = ((e.getY() - decode_pdf.getLayoutY()) - middleDragStartY) / 4;
        }
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        if (decode_pdf.getDisplayView() == Display.FACING
                && decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON)
                && ((JavaFxGUI) decode_pdf.getExternalHandler(Options.GUIContainer)).getPageTurnScalingAppropriate()
                && !decode_pdf.getPdfPageData().hasMultipleSizes()
                && !JavaFXPageNavigator.getPageTurnAnimating()) {
            //show preview turnover

            //get width and height of page
            float pageH = (decode_pdf.getPdfPageData().getCropBoxHeight(1) * decode_pdf.getScaling()) - 1;
            float pageW = (decode_pdf.getPdfPageData().getCropBoxWidth(1) * decode_pdf.getScaling()) - 1;

            if ((decode_pdf.getPdfPageData().getRotation(1) + currentGUI.getRotation()) % 180 == 90) {
                final float temp = pageH;
                pageH = pageW + 1;
                pageW = temp;
            }

            final Point corner = new Point();

            //right turnover
            if (commonValues.getCurrentPage() + 1 < commonValues.getPageCount()) {
                corner.setX((decode_pdf.getBoundsInLocal().getWidth() / 2) + pageW);
                corner.setY(decode_pdf.getInsetH() + pageH);

                final Point cursor = new Point();
                cursor.setX(e.getX());
                cursor.setY(e.getY());

                if (cursor.getX() > corner.getX() - 30 && cursor.getX() <= corner.getX()
                        && ((cursor.getY()) > corner.getY() - 30 && cursor.getY() <= corner.getY())
                        || (cursor.getY() >= corner.getY() - pageH && cursor.getY() < corner.getY() - pageH + 30)) {
                    //if close enough display preview turnover

                    //set cursor
                    decode_pdf.setCursor(Cursor.OPEN_HAND);

                    previewTurnover = true;
                    if (cursor.getY() >= corner.getY() - pageH && cursor.getY() < corner.getY() - pageH + 30) {
                        corner.setY(corner.getY() - pageH);
                        decode_pdf.setUserOffsets((int) cursor.getX(), (int) cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_RIGHT);
                    } else {
                        decode_pdf.setUserOffsets((int) cursor.getX(), (int) cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT);
                    }

                } else {
                    if (offsets.getDragTop()) {
                        corner.setY(corner.getY() - pageH);
                    }
                    testFall(corner, cursor, false);
                }
            }

            //left turnover
            if (commonValues.getCurrentPage() != 1) {
                corner.setX((decode_pdf.getBoundsInLocal().getWidth() / 2) - pageW);
                corner.setY(decode_pdf.getInsetH() + pageH);

                final Point cursor = new Point();
                cursor.setX(e.getX());
                cursor.setY(e.getY());

                if (cursor.getX() < corner.getX() + 30 && cursor.getX() >= corner.getX()
                        && ((cursor.getY() > corner.getY() - 30 && cursor.getY() <= corner.getY())
                        || (cursor.getY() >= corner.getY() - pageH && cursor.getY() < corner.getY() - pageH + 30))) {
                    //if close enough display preview turnover
                    //                    System.out.println("drawing left live "+decode_pdf.drawLeft);
                    //set cursor
                    decode_pdf.setCursor(Cursor.OPEN_HAND);

                    previewTurnover = true;
                    if (cursor.getY() >= corner.getY() - pageH && cursor.getY() < corner.getY() - pageH + 30) {
                        corner.setX(corner.getY() - pageH);
                        decode_pdf.setUserOffsets((int) cursor.getX(), (int) cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_LEFT);
                    } else {
                        decode_pdf.setUserOffsets((int) cursor.getX(), (int) cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT);
                    }

                } else {
                    if (offsets.getDragTop()) {
                        corner.setY(corner.getY() - pageH);
                    }
                    testFall(corner, cursor, true);
                }
            }

        }

        
        //Update cursor position if over page in single mode
//
//            int[] flag = new int[2];
//            flag[0] = SwingGUI.CURSOR;
//            
//            if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE || (SwingMouseSelector.activateMultipageHighlight && decode_pdf.getDisplayView()==Display.CONTINUOUS && decode_pdf.getDisplayView()==Display.CONTINUOUS_FACING)) {
//                //get raw w and h
//                int rawW,rawH;
//                if (currentGUI.getRotation()%180==90) {
//                    rawW = decode_pdf.getPdfPageData().getCropBoxHeight(1);
//                    rawH = decode_pdf.getPdfPageData().getCropBoxWidth(1);
//                } else {
//                    rawW = decode_pdf.getPdfPageData().getCropBoxWidth(1);
//                    rawH = decode_pdf.getPdfPageData().getCropBoxHeight(1);
//                }
//                
//                Point p = event.getPoint();
//                int x = (int)p.getX();
//                int y = (int)p.getY();
//                
//                float scaling = decode_pdf.getScaling();
//                
//                double pageHeight = scaling*rawH;
//                double pageWidth = scaling*rawW;
//                int yStart = decode_pdf.getInsetH();
//                
//                //move so relative to center
//                double left = (decode_pdf.getWidth()/2) - (pageWidth/2);
//                double right = (decode_pdf.getWidth()/2) + (pageWidth/2);
//                
//                if(decode_pdf.getDisplayView()==Display.FACING){
//                	 left = (decode_pdf.getWidth()/2);
//                	 if(decode_pdf.getPageNumber()!=1 || decode_pdf.getPageCount()==2)
//                		 left -= (pageWidth);
//                	 
//                     right = (decode_pdf.getWidth()/2) + (pageWidth);
//                }
//                
//                if (x >= left && x <= right &&
//                        y >= yStart && y <= yStart + pageHeight)
//                    //set displayed
//                    flag[1] = 1;
//                else
//                    //set not displayed
//                    flag[1] = 0;
//                
//                
//            } else {
//                //set not displayed
//                flag[1] = 0;
//            }
//            currentGUI.setMultibox(flag);
       
        if (decode_pdf.getExternalHandler(Options.UniqueAnnotationHandler) != null) {
            final int[] pos = updateXY((int)e.getX(), (int)e.getY(), decode_pdf, commonValues);
            checkLinks(false, decode_pdf.getIO(), pos[0], pos[1]);
        }
    }
    
    public void mouseWheelMoved(final ScrollEvent e){
        if(decode_pdf.getDisplayView() == Display.PAGEFLOW) {
            return;
        }
        
        if(currentGUI.getProperties().getValue("allowScrollwheelZoom").equalsIgnoreCase("true") && e.isControlDown()){
            //zoom
            int scaling = ((GUI)currentGUI).getSelectedComboIndex(Commands.SCALING);
            if(scaling!=-1){
                scaling = (int)decode_pdf.getDPIFactory().removeScaling(decode_pdf.getScaling()*100);
            }else{
                String numberValue = ((GUI)currentGUI).getSelectedComboItem(Commands.SCALING).toString();
                try{
                    scaling= (int)Float.parseFloat(numberValue);
                }catch(final Exception ex){

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception in handling scaling "+ex);
                    }

                    scaling=-1;
                    //its got characters in it so get first valid number string
                    final int length=numberValue.length();
                    int ii=0;
                    while(ii<length){
                        final char c=numberValue.charAt(ii);
                        if(((c>='0')&&(c<='9'))|(c=='.')) {
                            ii++;
                        } else {
                            break;
                        }
                    }
                    
                    if(ii>0) {
                        numberValue = numberValue.substring(0, ii);
                    }
                    
                    //try again if we reset above
                    if(scaling==-1){
                        try{
                            scaling = (int)Float.parseFloat(numberValue);
                        }catch(final Exception e1){

                            if(LogWriter.isOutput()) {
                                LogWriter.writeLog("Exception in handling scaling "+e1);
                            }
                            scaling=-1;
                        }
                    }
                }
            }
            
            float value = e.getTouchCount();
            
            if(scaling!=1 || value<0){
                if(value<0){
                    value = 1.25f;
                }else{
                    value = 0.8f;
                }
                if(!(scaling+value<0)){
                    float currentScaling = (scaling*value);
                    
                    if(((int)currentScaling)==(scaling)) {
                        currentScaling = scaling + 1;
                    } else {
                        currentScaling = ((int) currentScaling);
                    }
                    
                    if(currentScaling<1) {
                        currentScaling = 1;
                    }
                    
                    if(currentScaling>1000) {
                        currentScaling = 1000;
                    }
                    
                    //update scaling
                    currentGUI.snapScalingToDefaults(currentScaling);

                    /**
                     * Do not think this is necersary for JavaFX Viewer as the PdfDecoder is set into a ScrollPane
                     * which handles scrollable view.
                     
                    //store mouse location
                    final Rectangle r = ((PdfDecoder)decode_pdf).getVisibleRect();
                    final double x = event.getX()/((PdfDecoder)decode_pdf).getBounds().getWidth();
                    final double y = event.getY()/((PdfDecoder)decode_pdf).getBounds().getHeight();
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                ((PdfDecoder)decode_pdf).scrollRectToVisible(new Rectangle(
                                        (int)((x*((PdfDecoderFX)decode_pdf).getWidth())-(r.getWidth()/2)),
                                        (int)((y*((PdfDecoderFX)decode_pdf).getHeight())-(r.getHeight()/2)),
                                        (int)((PdfDecoder)decode_pdf).getVisibleRect().getWidth(),
                                        (int)((PdfDecoder)decode_pdf).getVisibleRect().getHeight()));
                                ((PdfDecoder)decode_pdf).repaint();
                            } catch (Exception e) {e.printStackTrace();}
                        }
                    };
                    t.setDaemon(true);
                    t.start();
                    SwingUtilities.invokeLater(t);*/
                }
            }
        } else {
            
            final ScrollPane scroll = ((ScrollPane)decode_pdf.getParent());
            
            if ((scroll.getVvalue()==scroll.getVmax()-scroll.getHeight() || scroll.getHeight()==0) &&
                    timeOfLastPageChange+700 < System.currentTimeMillis() &&
                    currentGUI.getValues().getCurrentPage() < decode_pdf.getPageCount()) {
                
                //change page
                timeOfLastPageChange = System.currentTimeMillis();
                currentCommands.executeCommand(Commands.FORWARDPAGE, null);
                
                //update scrollbar so at top of page
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        scroll.setVvalue(scroll.getVmin());
                    }
                });
                
            } else if (scroll.getVvalue()==scroll.getVmin() &&
                    timeOfLastPageChange+700 < System.currentTimeMillis() &&
                    currentGUI.getValues().getCurrentPage() > 1) {
                
                //change page
                timeOfLastPageChange = System.currentTimeMillis();
                currentCommands.executeCommand(Commands.BACKPAGE, null);
                
                //update scrollbar so at bottom of page
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        scroll.setVvalue(scroll.getVmax());
                    }
                });
                
            } 
            /**
             * We do not think this is necersary as the PDFContent is hosted within a ScrollPane
             * so scrolling is done for us.
            else {
                //scroll

                Area rect = new Area(((PdfDecoderFX)decode_pdf).getVisibleRect());
                AffineTransform transform = new AffineTransform();
                transform.translate(0, event.getUnitsToScroll() * decode_pdf.getScrollInterval());
                rect = rect.createTransformedArea(transform);
                ((PdfDecoder)decode_pdf).scrollRectToVisible(rect.getBounds());
            }*/
        }
    }

    private void testFall(final Point corner, final Point cursor, final boolean testLeft) {
        if (!previewTurnover) {
            return;
        }

        float width = (decode_pdf.getPdfPageData().getCropBoxWidth(1) * decode_pdf.getScaling()) - 1;

        if ((decode_pdf.getPdfPageData().getRotation(1) + currentGUI.getRotation()) % 180 == 90) {
            width = decode_pdf.getPdfPageData().getCropBoxHeight(1) * decode_pdf.getScaling();
        }

        final float pageW = width;

        if (!testLeft) {
            if (!offsets.getDragLeft()) {
                //reset cursor
                decode_pdf.setCursor(Cursor.DEFAULT);

                //If previously displaying turnover, animate to corner
                final Thread animation = new Thread() {
                    @Override
                    public void run() {

                        corner.setX((decode_pdf.getBoundsInLocal().getWidth() / 2) + pageW);
                        //work out if page change needed
                        boolean fallBack = true;
                        if (cursor.getX() < corner.getX() - pageW) {
                            corner.setX((corner.getX() - (2 * pageW)));
                            fallBack = false;
                        }

                        // Fall animation
                        int velocity = 1;

                        //ensure cursor is not outside expected range
                        if (fallBack && cursor.getX() >= corner.getX()) {
                            cursor.setX(corner.getX() - 1);
                        }
                        if (!fallBack && cursor.getX() <= corner.getX()) {
                            cursor.setX(corner.getX() + 1);
                        }
                        if (!offsets.getDragTop() && cursor.getY() >= corner.getY()) {
                            cursor.setY(corner.getY() - 1);
                        }
                        if (offsets.getDragTop() && cursor.getY() <= corner.getY()) {
                            cursor.setY(corner.getY() + 1);
                        }

                        //Calculate distance required
                        final double distX = (corner.getX() - cursor.getX());
                        final double distY = (corner.getY() - cursor.getY());

                        //Loop through animation
                        while ((fallBack && cursor.getX() <= corner.getX())
                                || (!fallBack && cursor.getX() >= corner.getX())
                                || (!offsets.getDragTop() && cursor.getY() <= corner.getY())
                                || (offsets.getDragTop() && cursor.getY() >= corner.getY())) {

                            //amount to move this time
                            double xMove = velocity * distX * 0.002;
                            double yMove = velocity * distY * 0.002;

                            //make sure always moves at least 1 pixel in each direction
                            if (Math.abs(xMove) < 1) {
                                xMove /= Math.abs(xMove);
                            }
                            if (Math.abs(yMove) < 1) {
                                yMove /= Math.abs(yMove);
                            }

                            cursor.setX(cursor.getX() + xMove);
                            cursor.setY(cursor.getY() + yMove);
                            if (offsets.getDragTop()) {
                                decode_pdf.setUserOffsets((int) cursor.getX(), (int) cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_RIGHT);
                            } else {
                                decode_pdf.setUserOffsets((int) cursor.getX(), (int) cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT);
                            }

                            //Double speed til moving 32/frame
                            if (velocity < 32) {
                                velocity *= 2;
                            }

                            //sleep til next frame
                            try {
                                Thread.sleep(50);
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }

                        }

                        if (!fallBack) {
                            //calculate page to turn to
                            int forwardPage = commonValues.getCurrentPage() + 1;
                            if (decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER) && forwardPage % 2 == 1) {
                                forwardPage++;
                            } else if (!decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER) && forwardPage % 2 == 0) {
                                forwardPage++;
                            }

                            //change page
                            commonValues.setCurrentPage(forwardPage);
                            currentGUI.setPageNumber();
                            decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
                            currentGUI.decodePage();
                        }

                        //hide turnover
                        decode_pdf.setUserOffsets(0, 0, org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK);
                        JavaFXPageNavigator.setPageTurnAnimating(false, currentGUI);
                    }
                };
                animation.setDaemon(true);
                JavaFXPageNavigator.setPageTurnAnimating(true, currentGUI);
                animation.start();
                previewTurnover = false;
            }
        } else {
            if (previewTurnover && offsets.getDragLeft()) {
                //reset cursor
                decode_pdf.setCursor(Cursor.DEFAULT);

                //If previously displaying turnover, animate to corner
                final Thread animation = new Thread() {
                    @Override
                    public void run() {

                        corner.setX((decode_pdf.getBoundsInLocal().getWidth() / 2) - pageW);
                        //work out if page change needed
                        boolean fallBack = true;
                        if (cursor.getX() > corner.getX() + pageW) {
                            corner.setX((corner.getX()) + (2 * pageW));
                            fallBack = false;
                        }

                        // Fall animation
                        int velocity = 1;

                        //ensure cursor is not outside expected range
                        if (!fallBack && cursor.getX() >= corner.getX()) {
                            cursor.setX(corner.getX() - 1);
                        }
                        if (fallBack && cursor.getX() <= corner.getX()) {
                            cursor.setX(corner.getX() + 1);
                        }
                        if (!offsets.getDragTop() && cursor.getY() >= corner.getY()) {
                            cursor.setY(corner.getY() - 1);
                        }
                        if (offsets.getDragTop() && cursor.getY() <= corner.getY()) {
                            cursor.setY(corner.getY() + 1);
                        }

                        //Calculate distance required
                        final double distX = (corner.getX() - cursor.getX());
                        final double distY = (corner.getY() - cursor.getY());

                        //Loop through animation
                        while ((!fallBack && cursor.getX() <= corner.getX())
                                || (fallBack && cursor.getX() >= corner.getX())
                                || (!offsets.getDragTop() && cursor.getY() <= corner.getY())
                                || (offsets.getDragTop() && cursor.getY() >= corner.getY())) {

                            //amount to move this time
                            double xMove = velocity * distX * 0.002;
                            double yMove = velocity * distY * 0.002;

                            //make sure always moves at least 1 pixel in each direction
                            if (Math.abs(xMove) < 1) {
                                xMove /= Math.abs(xMove);
                            }
                            if (Math.abs(yMove) < 1) {
                                yMove /= Math.abs(yMove);
                            }

                            cursor.setX(cursor.getX() + xMove);
                            cursor.setY(cursor.getY() + yMove);
                            if (offsets.getDragTop()) {
                                decode_pdf.setUserOffsets((int) cursor.getX(), (int) cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_LEFT);
                            } else {
                                decode_pdf.setUserOffsets((int) cursor.getX(), (int) cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT);
                            }

                            //Double speed til moving 32/frame
                            if (velocity < 32) {
                                velocity *= 2;
                            }

                            //sleep til next frame
                            try {
                                Thread.sleep(50);
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }

                        }

                        if (!fallBack) {
                            //calculate page to turn to
                            int backPage = commonValues.getCurrentPage() - 2;
                            if (backPage == 0) {
                                backPage = 1;
                            }

                            //change page
                            commonValues.setCurrentPage(backPage);
                            currentGUI.setPageNumber();
                            decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
                            currentGUI.decodePage();
                        }

                        //hide turnover
                        decode_pdf.setUserOffsets(0, 0, org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK);
                        JavaFXPageNavigator.setPageTurnAnimating(false, currentGUI);
                    }
                };
                animation.setDaemon(true);
                JavaFXPageNavigator.setPageTurnAnimating(true, currentGUI);
                animation.start();
                previewTurnover = false;
            }
        }
    }

}
