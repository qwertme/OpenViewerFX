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
 * JavaFXMouseListener.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.javafx;

import java.io.File;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import org.jpedal.PdfDecoderFX;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.MouseMode;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.MouseSelector;
import org.jpedal.examples.viewer.gui.generic.GUIMouseHandler;
import org.jpedal.exception.PdfException;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.render.DynamicVectorRenderer;

/**
 * Class to handle JavaFX Mouse Events that take place on the Pane 
 * which holds the PDF content (PdfDecoderFX).
 */
public class JavaFXMouseListener extends MouseSelector implements GUIMouseHandler {

    private final PdfDecoderFX decode_pdf;
    private final GUIFactory currentGUI;
    private final Values commonValues;
    private final Commands currentCommands;

    final JavaFXMouseSelector selectionFunctions;
    final JavaFXMousePanMode panningFunctions;
    final JavaFXMousePageTurn pageTurnFunctions;

    //Custom mouse function
    private static JavaFXMouseFunctionality customMouseFunctions;

    //private boolean scrollPageChanging;

    /**
     * current cursor position
     */
    private double cx, cy;

    /**
     * tells user if we enter a link
     */
    private static final String message = "";

    /**
     * tracks mouse operation mode currently selected
     */
    private MouseMode mouseMode = new MouseMode();

    public JavaFXMouseListener(final PdfDecoderFX decode_pdf, final GUIFactory currentGUI,
            final Values commonValues, final Commands currentCommands) {

        this.decode_pdf = decode_pdf;
        this.currentGUI = currentGUI;
        this.commonValues = commonValues;
        this.currentCommands = currentCommands;
        this.mouseMode = currentCommands.getMouseMode();

        selectionFunctions = new JavaFXMouseSelector(decode_pdf, currentGUI, commonValues, currentCommands);
        panningFunctions = new JavaFXMousePanMode(decode_pdf);
        pageTurnFunctions = new JavaFXMousePageTurn(decode_pdf, currentGUI, commonValues, currentCommands);
    }

    @Override
    public void setupMouse() {
        /**
         * track and display screen co-ordinates and support links
         */
        /**
         * Motion Listener Code.
         */
        decode_pdf.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent e) {
                mouseDragged(e);
            }
        });

        decode_pdf.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent e) {
                mouseMoved(e);
            }
        }); 

        /**
         * Mouse Listener Code.
         */
        decode_pdf.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent e) {
                mouseClicked(e);
            }
        });

        decode_pdf.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent e) {
                mouseEntered(e);
            }
        });

        decode_pdf.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent e) {
                mouseExited(e);
            }
        });

        decode_pdf.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent e) {
                mousePressed(e);
            }
        });

        decode_pdf.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent e) {
                mouseReleased(e);
            }
        });
        
        /**
         * Controls for dragging a PDF into the scene
         * Using the dragboard, which extends the clipboard class, 
         * detect a file being dragged onto the scene and if the user drops the file
         * we load it.
         */
        decode_pdf.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(final DragEvent event) {
                mouseDragOver(event);
            }
        });
        
        decode_pdf.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(final DragEvent event) {
                mouseDragDropped(event);
            }
        });
        
        
        /**
         * Mouse Wheel Listener Code.
         */
        if (GUI.debugFX) {
            System.out.println("addMouseWheelListener in JavaFXMouseListener.java not yet implemented for JavaFX");
        }
        //decode_pdf.addMouseWheelListener(this);

        //set cursor
        decode_pdf.setDefaultCursor(Cursor.DEFAULT);
    }

    private void mouseReleased(final MouseEvent e) {
        //stop middle click panning
        if (e.getButton().equals(MouseButton.MIDDLE)) {
            panningFunctions.mouseReleased(e);
        } else {
            switch (mouseMode.getMouseMode()) {

                case MouseMode.MOUSE_MODE_TEXT_SELECT:
                    selectionFunctions.mouseReleased(e);
                    break;

                case MouseMode.MOUSE_MODE_PANNING:
                    panningFunctions.mouseReleased(e);
                    break;

            }

            if (decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView() == Display.FACING) {
                pageTurnFunctions.mouseReleased(e);
            }

            if (customMouseFunctions != null) {
                customMouseFunctions.mouseReleased(e);
            }
        }
    }
    
	public static void setCustomMouseFunctions(final JavaFXMouseFunctionality cmf) {
		customMouseFunctions = cmf;
	}
    
    private void mouseDragDropped(final DragEvent e) {
        final Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            // Only get the first file from the list
            final File file = db.getFiles().get(0);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        currentCommands.handleTransferedFile(file.getAbsolutePath());
                    } catch (final PdfException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        e.setDropCompleted(success);
        e.consume();
    }

    private static void mouseDragOver(final DragEvent e) {
        final Dragboard db = e.getDragboard();
        //Only accept pdf files
        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".pdf");
        if (db.hasFiles()) {
            if (isAccepted) {
                e.acceptTransferModes(TransferMode.COPY);
            }
        } else {
            e.consume();
        }
    }
    
    private void mouseClicked(final MouseEvent e) {
        switch (mouseMode.getMouseMode()) {

            case MouseMode.MOUSE_MODE_TEXT_SELECT:
                if (decode_pdf.isOpen()){
                    selectionFunctions.mouseClicked(e);
                }
                break;

            case MouseMode.MOUSE_MODE_PANNING:
                //Does Nothing so ignore
                break;

        }

        if (decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView() == Display.FACING) {
            pageTurnFunctions.mouseClicked(e);
        }

        if (customMouseFunctions != null) {
            customMouseFunctions.mouseClicked(e);
        }
    }
    
    private void mouseEntered(final MouseEvent e) {
        
        currentGUI.enableMemoryBar(true, false);
        currentGUI.enableCursor(true, true);
        currentGUI.setMultibox(new int[]{GUI.CURSOR,1});
        
        switch (mouseMode.getMouseMode()) {

            case MouseMode.MOUSE_MODE_TEXT_SELECT:
			//Text selection does nothing here
                //selectionFunctions.mouseEntered(e);
                break;

            case MouseMode.MOUSE_MODE_PANNING:
                //Does Nothing so ignore
                break;

        }

        if (decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView() == Display.FACING) {
            pageTurnFunctions.mouseEntered(e);
        }

        if (customMouseFunctions != null) {
            customMouseFunctions.mouseEntered(e);
        }
    }
    
    private void mousePressed(final MouseEvent e) {

        //Start dragging
        if (e.getButton().equals(MouseButton.MIDDLE)) {
            panningFunctions.mousePressed(e);
        } else {
            switch (mouseMode.getMouseMode()) {

                case MouseMode.MOUSE_MODE_TEXT_SELECT:
                    selectionFunctions.mousePressed(e);
                    break;

                case MouseMode.MOUSE_MODE_PANNING:
                    panningFunctions.mousePressed(e);
                    break;

            }

            if (decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView() == Display.FACING) {
                pageTurnFunctions.mousePressed(e);
            }

            if (customMouseFunctions != null) {
                customMouseFunctions.mousePressed(e);
            }
        }
    }

    private void mouseMoved(final MouseEvent e) {
        //int page = commonValues.getCurrentPage();

        //Point p = selectionFunctions.getCoordsOnPage(e.getX(), e.getY(), page);
        final int x = (int) e.getX();
        final int y = (int) e.getY();
        updateCoords(x, y);

        /*
         * Mouse mode specific code
         */
        switch (mouseMode.getMouseMode()) {

            case MouseMode.MOUSE_MODE_TEXT_SELECT:
                //final int[] values = updateXY((int) e.getX(), (int) e.getY(), decode_pdf, commonValues);
                //x = values[0];
                //y = values[1];
               
                if (!currentCommands.extractingAsImage && decode_pdf.isOpen()) {
                    final int pagenumber = decode_pdf.getPageNumber();
                    final int crx = decode_pdf.getPdfPageData().getCropBoxX(pagenumber);
                    final int cry = decode_pdf.getPdfPageData().getCropBoxY(pagenumber);
                    getObjectUnderneath((int)cx+crx, (int)cy+cry);
                }

                selectionFunctions.mouseMoved(e);
                break;

            case MouseMode.MOUSE_MODE_PANNING:
                //Does Nothing so ignore
                break;

        }

        if (decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView() == Display.FACING) {
            pageTurnFunctions.mouseMoved(e);
        }

        if (customMouseFunctions != null) {
            customMouseFunctions.mouseMoved(e);
        }
    }

    private void mouseDragged(final MouseEvent e) {

        if (e.getButton().equals(MouseButton.MIDDLE)) {
            panningFunctions.mouseDragged(e);
        } else {
            switch (mouseMode.getMouseMode()) {

                case MouseMode.MOUSE_MODE_TEXT_SELECT:
                    selectionFunctions.mouseDragged(e);
                    break;

                case MouseMode.MOUSE_MODE_PANNING:
                    panningFunctions.mouseDragged(e);
                    break;

            }

            if (decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView() == Display.FACING) {
                pageTurnFunctions.mouseDragged(e);
            }

            if (customMouseFunctions != null) {
                customMouseFunctions.mouseDragged(e);
            }
        }
    }
    
    private void mouseExited(final MouseEvent e) {
        
        currentGUI.enableCursor(true, false);
        currentGUI.enableMemoryBar(true, true);
        currentGUI.setMultibox(new int[]{GUI.CURSOR,0});

        switch (mouseMode.getMouseMode()) {

            case MouseMode.MOUSE_MODE_TEXT_SELECT:
                selectionFunctions.mouseExited(e);
                break;

            case MouseMode.MOUSE_MODE_PANNING:
                //Does Nothing so ignore
                break;

        }

        if (decode_pdf.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView() == Display.FACING) {
            pageTurnFunctions.mouseExited(e);
        }

        if (customMouseFunctions != null) {
            customMouseFunctions.mouseExited(e);
        }
    }

    public double[] getCursorLocation() {
        return new double[]{cx, cy};
    }

    public void checkLinks(final boolean mouseClicked, final PdfObjectReader pdfObjectReader) {
        //int[] pos = updateXY(event.getX(), event.getY());
        pageTurnFunctions.checkLinks(mouseClicked, pdfObjectReader, cx, cy);
    }

    public void updateCordsFromFormComponent(final MouseEvent e) {
       // Region component = (Region) e.getSource();

        //double x = component.getBoundsInLocal().getMinX() + e.getX();
        //double y = component.getBoundsInLocal().getMinX() + e.getY();
        //Point p = selectionFunctions.getCoordsOnPage(x, y, commonValues.getCurrentPage());
        final double x = (int) e.getX();
        final double y = (int) e.getY();

        updateCoords(x, y);
    }

    /**
     * update current page co-ordinates on screen
     */
    public void updateCoords(final double x, final double y) {

        cx = x;
        cy = y;

        if (decode_pdf.getDisplayView() != Display.SINGLE_PAGE) {

            if (JavaFXMouseSelector.activateMultipageHighlight && (decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING)) {

                if ((decode_pdf.getDisplayView() == Display.FACING) &&
                     (decode_pdf.getPageNumber() < decode_pdf.getPageCount())) {
                        //Width of page on left so we can show the correct coords on right page
                        final int xOffset = decode_pdf.getPdfPageData().getCropBoxWidth(decode_pdf.getPageNumber());
                        if (cx >= xOffset) {
                            cx -= xOffset;
                        }
                    }
            } else {
                cx = 0;
                cy = 0;
            }
        }
        
        // We adjust the PDF to (0,0) of decode_pdf, so add on the cropbox x/y account for this
        final int pagenumber = decode_pdf.getPageNumber();
        final int crx = decode_pdf.getPdfPageData().getCropBoxX(pagenumber);
        final int cry = decode_pdf.getPdfPageData().getCropBoxY(pagenumber);
        final Bounds pdfBounds = decode_pdf.getBoundsInLocal();
        
        if ((Values.isProcessing()) || (commonValues.getSelectedFile() == null)) {
            currentGUI.setCoordText("  X: " + " Y: " + ' ' + ' '); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        } else if (cx >= 0 && cy >= 0 && cx <= pdfBounds.getMaxX() - currentGUI.getDropShadowDepth() && cy <= pdfBounds.getMaxY() - currentGUI.getDropShadowDepth()) {
            //Make sure we only display coords when we're inside the decode_pdf Pane display area.
            currentGUI.setCoordText("  X: " + (cx+crx) + " Y: " + (cy+cry) + ' ' + ' ' + message); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
    }

    /**
     * scroll to visible Rectangle and update Coords box on screen
     */
    protected void scrollAndUpdateCoords(final MouseEvent e) {
        //scroll if user hits side
       // int interval = decode_pdf.getScrollInterval();
       // Rectangle visible_test = new Rectangle(adjustForAlignment((int) e.getX(), decode_pdf), e.getY(), interval, interval);
        if (GUI.debugFX) {
            //System.out.println("getVisibleRect and scrollRectToVisible is not implemented yet in JavaFXMouseListener.java");
            /**
             *
             * if((currentGUI.allowScrolling())&&(!decode_pdf.getVisibleRect().contains(visible_test))){
             * ((PdfDecoder)decode_pdf).scrollRectToVisible(visible_test); }
             */
        }

        //int page = commonValues.getCurrentPage();

        //Point p = selectionFunctions.getCoordsOnPage(e.getX(), e.getY(), page);
        final int x = (int) e.getX();
        final int y = (int) e.getY();
        updateCoords(x, y);
    }

    
    /**
     * This method is not currently used as we change cursor via the CSS.
     * @param x
     * @param y 
     */
    private void getObjectUnderneath(final int x, final int y) {
        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
            final int type = decode_pdf.getDynamicRenderer().getObjectUnderneath(x, y);
            switch (type) {
                case -1:
                    decode_pdf.setCursor(Cursor.DEFAULT);
                    break;
                case DynamicVectorRenderer.TEXT:
                    decode_pdf.setCursor(Cursor.TEXT);
                    break;
                case DynamicVectorRenderer.IMAGE:
                    decode_pdf.setCursor(Cursor.CROSSHAIR);
                    break;
                case DynamicVectorRenderer.TRUETYPE:
                    decode_pdf.setCursor(Cursor.TEXT);
                    break;
                case DynamicVectorRenderer.TYPE1C:
                    decode_pdf.setCursor(Cursor.TEXT);
                    break;
                case DynamicVectorRenderer.TYPE3:
                    decode_pdf.setCursor(Cursor.TEXT);
                    break;
            }
        }
    }
}
