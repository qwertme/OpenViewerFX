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
 * PageFlowFX.java
 * ---------------
 */

package org.jpedal.display;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.jpedal.PdfDecoderInt;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * Main JavaFX Code for Page Flow Display Mode used
 * by both SwingViewer and JavaFXViewer.
 */
public class PageFlowFX extends AnchorPane{
    
    private final boolean isFX;
    
    public PageFlowFX(final PdfDecoderInt pdfDecoder,final boolean isFX) {
        
        // JavaFX cannot be restarted after exiting, so turn off auto exit.
        Platform.setImplicitExit(false);

        // Setup PDF info
        pdf = pdfDecoder;
       
        this.isFX =isFX;
        //pageData = pdf.getPdfPageData();
        pageCount = pdf.getPageCount();
        setPageNumber(pdfDecoder.getPageNumber());
        pageFocus = pageNumber;

        // Setup for memory management
        pagesInMemory = 0;
        runtime = Runtime.getRuntime();
        final long maxMem = runtime.maxMemory();
        if (maxMem * 0.25f < 36000000) {
            memoryLimit = maxMem - 36000000;
        } else {
            memoryLimit = (long) (maxMem * 0.75f);
        }

        //Disable forms, but store whether they were on so we can restore on dispose()
        final AcroRenderer formRenderer = pdf.getFormRenderer();
        if (formRenderer != null) {
            formsIgnoredStore = formRenderer.ignoreForms();
            formRenderer.setIgnoreForms(true);
        }

        pages = new Page[pageCount];

        createScene();
        
        pageLimit = 50;
    }
    
    protected final PdfDecoderInt pdf;
    private boolean stopAddingPages;
    private final int pageCount;
    private int displayRotation;
    protected int pageNumber;
    
    private int pagesToGenerate = 21;

    private static final int textureSize = 256;
    private Page[] pages;
    private double totalPageWidth;

    private Rectangle backgroundTop;
    private Rectangle backgroundBottom;
    private NavBar navBar;
    private ZoomBar zoomBar;
    private CheckBox perspectiveCheckBox;
    private CheckBox reflectionCheckBox;

    //private EventHandler pageListener, messageListener;
    private javafx.scene.Cursor defaultCursor, grabbingCursor, grabCursor;

    private double sceneXOffset, sceneYOffset;

    private boolean currentlyAddingPages;
    private boolean memoryWarningShown;
    private boolean pageFlowEnding;

    private double scaling = 1.5;
    private double pageFocus = 1;

    private int currentZPosition = -1;

    private boolean formsIgnoredStore;
    private int pagesInMemory;
    private final long memoryLimit;
    private final Runtime runtime;
    private final int pageLimit;
    private boolean pageClickEvent;
    private boolean enableReflection = true, enablePerspectiveTransform = true;

    private void createScene() {
                
        final ObservableList<Node> children = this.getChildren();

        sceneXOffset = newSceneWidth / 2;
        sceneYOffset = newSceneHeight / 2;

        // Create 2 tone background colours.
        backgroundTop = new Rectangle(0, 0, newSceneWidth, newSceneHeight / 2);
        backgroundTop.setFill(new Color(55 / 255f, 55 / 255f, 65 / 255f, 1));
        backgroundBottom = new Rectangle(0, newSceneHeight / 2, newSceneWidth, newSceneHeight / 2);
        backgroundBottom.setFill(new Color(28 / 255f, 28 / 255f, 32 / 255f, 1));

        // Create nav & zoom bars
        navBar = new NavBar();
        zoomBar = new ZoomBar();

        perspectiveCheckBox = new CheckBox("Perspectives");
        perspectiveCheckBox.setLayoutX(5);
        perspectiveCheckBox.setLayoutY(5);
        perspectiveCheckBox.setTextFill(Color.WHITE);
        perspectiveCheckBox.setSelected(true);
        perspectiveCheckBox.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(final javafx.event.ActionEvent actionEvent) {
                togglePerspectives();
            }
        });

        reflectionCheckBox = new CheckBox("Reflections");
        reflectionCheckBox.setLayoutX(5);
        reflectionCheckBox.setLayoutY(25);
        reflectionCheckBox.setTextFill(Color.WHITE);
        reflectionCheckBox.setSelected(true);
        reflectionCheckBox.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(final javafx.event.ActionEvent actionEvent) {
                toggleReflections();
            }
        });

        if (DecoderOptions.isRunningOnLinux) {
            toggleReflections();
            togglePerspectives();
        }

        children.addAll(backgroundTop, backgroundBottom, navBar, zoomBar, perspectiveCheckBox, reflectionCheckBox);

        setupMouseHandlers();
                
        setupWindowResizeListeners();
        
        addPages();
    }
    
    private double newSceneWidth;
    private double newSceneHeight;
    
    private void repositionObjects(){
        
        newSceneWidth = getWidth();
        newSceneHeight = getHeight();
        
        sceneXOffset = newSceneWidth / 2;
        totalPageWidth = pageCount * getPageWidthOrHeight();
        sceneYOffset = newSceneHeight / 2;
        navBar.update();
        zoomBar.update();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                backgroundTop.setWidth(newSceneWidth);
                backgroundBottom.setWidth(newSceneWidth);
                backgroundTop.setHeight(newSceneHeight);
                backgroundBottom.setHeight(newSceneHeight);
                backgroundBottom.setY(newSceneHeight / 2);
                
                if (pages[pageNumber - 1] != null) {
                    pages[pageNumber - 1].setMain(true);
                }
                for (final Page page : pages) {
                    if (page != null) {
                        page.update();
                    }
                }
            }
        });
        
    }
    
    // Listen out for window resizes to update the x,y,width,height of pages dynamically.
    private void setupWindowResizeListeners() {

        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observableValue, final Number oldSceneWidth, final Number newSceneWidth) {
                repositionObjects();
            }
        });
        heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observableValue, final Number oldSceneHeight, final Number newSceneHeight) {
                repositionObjects();
            }
        });
    }

    // volatile fields because they get used across threads so we don't want optimisations like caching.
    private volatile double x; //oldX is used so we know how far the mouse has been dragged since the last event.
    private volatile boolean isAnimating, stopAnimating;

    private void setupMouseHandlers() {

        /**
         * ******************************************************
         * Handle with care please, especially releasedHandler. *
    	 *******************************************************
         */
        // If moving then clicked, stop moving
        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent mouseEvent) {

                if (GUIDisplay.allowChangeCursor) {
                    setCursor(grabbingCursor);
                }

                if (navBar.isNavBarPress(mouseEvent)) {
                    // Event is handled in the event check
                } else if (zoomBar.isZoomBarPress(mouseEvent)) {
                    // Event is handled in the event check
                } else {
                    // If we are currently scrolling, tell it to stopAnimating
                    if (isAnimating) {
                        stopAnimating = true;
                    }

                    //Reset all X positions to current pageFocus.
                    x = mouseEvent.getSceneX();
                }
            }
        });

        // Move pages in the direction being dragged
        setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent mouseEvent) {

                if (navBar.isNavBarDrag(mouseEvent)) {
                    // The nav bar is handling the drag within navBar.isNavBarDrag().
                } else if (zoomBar.isZoomBarDrag(mouseEvent)) {
                    // The zoom bar is handling the drag within zoomBar.isZoomBarDrag().
                } else {
                    // Move the pages by the amount the mouse was dragged.
                    final double newPosition = pageFocus - (((mouseEvent.getSceneX() - x) / totalPageWidth) * 4 * pageCount);
                    if (newPosition > 1 && newPosition < pageCount) {
                        isAnimating = true;
                        reorderPages(pageFocus, false);
                        pageFocus = newPosition;
                        navBar.update();

                        for (final Page page : pages) {
                            if (page != null) {
                                page.update();
                            }
                        }
                        isAnimating = false;
                    }

                    final int newPageNumber = (int) (pageFocus + 0.5);
                    if (pageNumber != newPageNumber) {
                        setPageNumber(newPageNumber);
                    }

                    addPages();

                    // Set x positions for next mouse event to use.
                    x = mouseEvent.getSceneX();
                }
            }
        });

        // Move to center of nearest page
        setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent mouseEvent) {
                if (GUIDisplay.allowChangeCursor) {
                    setCursor(grabCursor);
                }
                //Reset cursor after delay
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (GUIDisplay.allowChangeCursor) {
                            setCursor(defaultCursor);
                        }
                    }
                }, 350);

                if (navBar.isNavBarRelease(mouseEvent)) {
                    // The nav bar is handling the release.
                } else if (zoomBar.isZoomBarRelease()) {
                    // The zoom bar is handling the release.
                } else {
                    if (!pageClickEvent) {
                        if (pageFocus < 1) {
                            pageFocus = 1;
                        } else if (pageFocus > pageCount) {
                            pageFocus = pageCount;
                        }
                        goTo((int) (pageFocus + 0.5));
                    } else {
                        pageClickEvent = false;
                    }
                }
            }
        });

        // Set default cursor when moving
        setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent mouseEvent) {
                if (navBar.isNavBarHover(mouseEvent) || zoomBar.isZoomBarHover(mouseEvent)) {
                    if (GUIDisplay.allowChangeCursor) {
                        setCursor(grabCursor);
                    }
                } else {
                    if (GUIDisplay.allowChangeCursor) {
                        setCursor(defaultCursor);
                    }
                }
            }
        });

        // Left/right keyboard page change listener
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent keyEvent) {
                final KeyCode key = keyEvent.getCode();

                switch (key) {
                    case RIGHT: {
                        final int dest = pageNumber + 1;
                        if (dest <= pageCount) {
                            goTo(dest);
                        }
                        break;
                    }
                    case LEFT: {
                        final int dest = pageNumber - 1;
                        if (dest > 0) {
                            goTo(dest);
                        }
                        break;
                    }
                    case R: {
                        toggleReflections();
                        break;
                    }
                    case T: {
                        togglePerspectives();
                        break;
                    }
                }
            }
        });

        // Scroll pages or scroll zoom if ctrl held.
        setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(final ScrollEvent event) {
                final double value = event.getDeltaY();

                if (event.isControlDown()) {
                    if (value < 0) {
                        if (scaling < 2) {
                            scaling += 0.1;
                            if (scaling > 2) {
                                scaling = 2;
                            }
                            zoomBar.update();
                            for (final Page page : pages) {
                                if (page != null) {
                                    page.update();
                                }
                            }
                        }
                    } else if ((value > 0) &&  (scaling > 1)) {
                            scaling -= 0.1;
                            if (scaling < 1) {
                                scaling = 1;
                            }
                            zoomBar.update();
                            for (final Page page : pages) {
                                if (page != null) {
                                    page.update();
                                }
                            }
                        }
                    
                } else {
                    if (value > 0) {
                        final int dest = pageNumber - 1;
                        if (dest > 0) {
                            goTo(dest);
                        }
                    } else {
                        final int dest = pageNumber + 1;
                        if (dest <= pageCount) {
                            goTo(dest);
                        }
                    }
                }
            }
        });

        // Double click to toggle max/min zoom
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    if (scaling != 1) {
                        scaling = 1;
                    } else {
                        scaling = 2;
                    }
                    zoomBar.update();
                    for (final Page page : pages) {
                        if (page != null) {
                            page.update();
                        }
                    }
                }
            }
        });

    }

    // Z positioning in JavaFX is based on the order nodes appear in the Node list/tree.
    // Reorder needs to be forced when pages get added.
    private void reorderPages(final double pageFocus, final boolean forceReorder) {

        final int position = (int) (pageFocus + 0.5);

        // Check if reorder is required
        if (!forceReorder && (currentZPosition == position || position < 1 || position > pageCount)) {
            return;
        }

        currentZPosition = position;
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // Add nodes behind the pages. e.g. background
                getChildren().clear();
                getChildren().add(backgroundTop);
                getChildren().add(backgroundBottom);

                // Add pages to the right of the current page, then add those to the left.
                int i = pageCount;
                while (i > position) {
                    if (pages[i - 1] != null) {
                        getChildren().add(pages[i - 1]);
                        if (enableReflection) {
                            getChildren().add(pages[i - 1].getReflection());
                        }
                    }
                    i--;
                }
                i = 1;
                while (i < position) {
                    if (pages[i - 1] != null) {
                        getChildren().add(pages[i - 1]);
                        if (enableReflection) {
                            getChildren().add(pages[i - 1].getReflection());
                        }
                    }
                    i++;
                }
                // Add main page
                if (pages[position - 1] != null) {
                    getChildren().add(pages[position - 1]);
                    if (enableReflection) {
                        getChildren().add(pages[position - 1].getReflection());
                    }
                }

                // Add nodes appearing in front of pages e.g. nav/zoom controls.
                getChildren().add(navBar);
                getChildren().add(zoomBar);
                getChildren().add(perspectiveCheckBox);
                getChildren().add(reflectionCheckBox);

            }
        });
    }

    public void setRotation(final int displayRotation) {
        //Reload pages if rotation changes
        if (this.displayRotation != displayRotation) {
            this.displayRotation = displayRotation;

            for (final Page p : pages) {
                if (p != null) {
                    p.dispose();
                }
            }

            stop();
            stopAddingPages = false;
            goTo(pageNumber);
        }
    }

    private void toggleReflections() {
        if (enableReflection) {
            enableReflection = false;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    reflectionCheckBox.setSelected(false);
                }
            });

            for (final Page page : pages) {
                if (page != null) {
                    page.disposeReflection();
                }
            }
            reorderPages(pageFocus, true);
        } else {

            if (enablePerspectiveTransform) {
                enableReflection = true;

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        reflectionCheckBox.setSelected(true);
                    }
                });

                for (final Page page : pages) {
                    if (page != null) {
                        page.setupReflection();
                        page.update();
                    }
                }
                reorderPages(pageFocus, true);
            }
        }
    }

    private void togglePerspectives() {
        if (enablePerspectiveTransform) {
            if (enableReflection) {
                toggleReflections();
            }

            enablePerspectiveTransform = false;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    perspectiveCheckBox.setSelected(false);
                    reflectionCheckBox.setDisable(true);
                }
            });

            for (final Page page : pages) {
                if (page != null) {
                    page.disposePerspectiveTransform();
                    page.update();
                }
            }
        } else {
            enablePerspectiveTransform = true;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    perspectiveCheckBox.setSelected(true);
                    reflectionCheckBox.setDisable(false);
                }
            });

            for (final Page page : pages) {
                if (page != null) {
                    page.setupPerspectiveTransform();
                    page.update();
                }
            }
        }
    }

    public void setCursors(final BufferedImage grab, final BufferedImage grabbing) {
        if (grab != null) {
            grabCursor = new ImageCursor(SwingFXUtils.toFXImage(grab, new WritableImage(grab.getWidth(), grab.getHeight())), 8, 8);
        } else {
            grabCursor = javafx.scene.Cursor.DEFAULT;
        }
        if (grabbing != null) {
            grabbingCursor = new ImageCursor(SwingFXUtils.toFXImage(grabbing, new WritableImage(grabbing.getWidth(), grabbing.getHeight())), 8, 8);
        } else {
            grabbingCursor = javafx.scene.Cursor.DEFAULT;
        }
        defaultCursor = javafx.scene.Cursor.DEFAULT;
    }

    private int newDestination;
    private double speed;

    public void goTo(final int firstDestination) {

        setPageNumber(firstDestination);

        // This is required because when we drag the pages around, the main does not get unset due to the updating of the page number as we drag.
        for (int i = 0; i < pageCount; i++) {
            if (pages[i] != null && i != pageNumber - 1) {
                pages[i].setMain(false);
            }
        }

        if (pages[pageNumber - 1] != null) {
            pages[pageNumber - 1].setMain(true);
        }

        //update pdfdecoder's pagenumber (-100 flag to prevent loop)
        pdf.setPageParameters(-100f, firstDestination);//Purpose is to maintain same page when swapping to Single Pages view-mode.

        addPages();

        //If already moving set variable to let thread know destination has changed
        if (isAnimating) {
            newDestination = firstDestination;
            return;
        }
        
        final Thread thread = new Thread("PageFlow-goTo") {
            @Override
            public void run() {
                int destination = firstDestination;

                while (!stopAnimating
                        && (pageFocus > destination || pageFocus < destination)) {

                    //Pick up if destination changed
                    if (newDestination != 0) {
                        destination = newDestination;
                        newDestination = 0;
                    }

                    //accelerate
                    if (pageFocus < destination) {
                        if (speed < 0.2f) {
                            speed = 0.2f;
                        }
                        speed *= 1.15f;
                    } else {
                        if (speed > -0.2f) {
                            speed = -0.2f;
                        }
                        speed *= 1.15f;
                    }

                    //cap speed
                    final double maxSpeed = (destination - pageFocus) / 4;
                    if (Math.abs(speed) > Math.abs(maxSpeed)) {
                        speed = maxSpeed;
                    }

                    //update page positions
                    pageFocus += speed;

                    // If close to int, round off to an int as it improves the image quality and reduces number of updates.
                    // Don't be too greedy otherwise it will cause pages to jump into final position when they near it.
                    if (pageFocus - (int) pageFocus > 0.99) {
                        pageFocus = (int) pageFocus + 1;
                    } else if (pageFocus - (int) pageFocus < 0.01) {
                        pageFocus = (int) pageFocus;
                    }

                    navBar.update();
                    reorderPages(pageFocus, false);
                    for (final Page page : pages) {
                        if (page != null) {
                            page.update();
                        }
                    }

                    try {
                        Thread.sleep(40); // 25fps
                    } catch (final Exception e) {
                        //tell user and log
                        if (LogWriter.isOutput()) {
                            LogWriter.writeLog("Exception: " + e.getMessage());
                        }
                        //
                    }

                    //Pick up if destination changed
                    if (newDestination != 0) {
                        destination = newDestination;
                        newDestination = 0;
                    }
                }
                stopAnimating = false;
                isAnimating = false;
            }
        };
        thread.setDaemon(true);
        isAnimating = true;
        thread.start();
    }

    private synchronized Image getPageImage(final int pageNumber, final int rotation, final int quality) {
        //Only generate a hi res texture for the current page
        if (pageNumber != this.pageNumber && quality > textureSize) {
            return null;
        }

        final int width = pdf.getPdfPageData().getCropBoxWidth(pageNumber);
        final int height = pdf.getPdfPageData().getCropBoxHeight(pageNumber);

        final float scale;
        if (width > height) {
            scale = (float) quality / width;
        } else {
            scale = (float) quality / height;
        }

        try {

            final float currentScaling = pdf.getScaling();
            pdf.setScaling(scale);
            final BufferedImage raw = pdf.getPageAsImage(pageNumber);
            pdf.setScaling(currentScaling);

            final BufferedImage result = new BufferedImage(quality, quality, BufferedImage.TYPE_INT_ARGB);
            
            final java.awt.Graphics2D g2 = (java.awt.Graphics2D) result.getGraphics();
            g2.rotate((rotation / 180.0) * Math.PI, quality / 2, quality / 2);
            final int x = (quality - raw.getWidth()) / 2;
            final int y = quality - raw.getHeight();
            g2.drawImage(raw, x, y, raw.getWidth(), raw.getHeight(), null);

            return SwingFXUtils.toFXImage(result, new WritableImage(result.getWidth(), result.getHeight()));

        } catch (final Exception e) {
            //tell user and log
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        return null;
    }

    // Clean up
    @SuppressWarnings("UnusedDeclaration")
    public void dispose() {
        // Dispose of pages & containers
        for (final Page page : pages) {
            if (page != null) {
                page.dispose();
            }
        }
        pages = null;

        //Restore forms setting
        final AcroRenderer formRenderer = pdf.getFormRenderer();
        if (formRenderer != null) {
            formRenderer.setIgnoreForms(formsIgnoredStore);
        }

      //  System.gc();
    }

    public void stop() {
        stopAddingPages = true;
        if(!isFX){
            while (currentlyAddingPages) {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    //tell user and log
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        }
    }

    /**
     * Add pages around the current page
     */
    private void addPages() {
        
    final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                currentlyAddingPages = true;
                int firstPage = pageNumber;
                //Add pages on either side
                for (int i = 0; i <= pagesToGenerate; i++) {

                    if (checkMemory()) {
                        return null;
                    }

                    final int spacesLeft = ((pagesToGenerate * 2) - 1) - pagesInMemory;
                    if (spacesLeft < 2) {
                        removeFurthestPages(2 - spacesLeft);
                    }

                    if (i == pagesToGenerate - 1) {
                        long used = runtime.totalMemory() - runtime.freeMemory();
                        if (used < memoryLimit && pagesToGenerate < pageCount && pagesToGenerate < pageLimit) {
                            pagesToGenerate++;
                        } else {
                            used = runtime.totalMemory() - runtime.freeMemory();
                            if (used < memoryLimit && pagesToGenerate < pageCount && pagesToGenerate < pageLimit) {
                                pagesToGenerate++;
                            }
                        }
                    }

                    if (stopAddingPages) {
                        currentlyAddingPages = false;
                        stopAddingPages = false;
                        return null;
                    }

                    int pn = firstPage + i;
                    //if 40 from center start doing only even pages
                    if (i > 40) {
                        pn += i - 40;
                        pn -= (pn & 1);

                        //fill in the odd pages
                        if (pn > pageCount) {
                            pn -= (pageCount - (firstPage + 40));
                            if ((pn & 1) == 0) {
                                pn--;
                            }
                        }
                    }
                    if (pn <= pageCount && pages != null && pages[pn - 1] == null) {
                        try {
                            final Page page = new Page(pn);
                            if (pages != null) {
                                pages[pn - 1] = page;
                                reorderPages(pageFocus, true);
                                pagesInMemory++;
                                if (pn == pageNumber) {
                                    page.setMain(true);
                                }
                            }
                        } catch (final Exception e) {

                            if (pages != null) {
                                pages[pn - 1] = null;
                            }
                            pagesInMemory--;

                            //tell user and log
                            if (LogWriter.isOutput()) {
                                LogWriter.writeLog("Exception: " + e.getMessage());
                            }
                            //
                        }
                    }

                    if (stopAddingPages) {
                        currentlyAddingPages = false;
                        stopAddingPages = false;
                        return null;
                    }

                    pn = firstPage - i;
                    //if 40 from center start doing only even pages
                    if (i > 40) {
                        pn -= i - 40;
                        pn += (pn & 1);

                        //fill in the odd pages
                        if (pn < 1) {
                            pn += (firstPage - 41);
                            if ((pn & 1) == 0) {
                                pn--;
                            }
                        }
                    }
                    if (pn > 0 && pages != null && pages[pn - 1] == null) {
                       
                        final Page page = new Page(pn);
                        if (pages != null) {
                            pages[pn - 1] = page;
                            reorderPages(pageFocus, true);
                            pagesInMemory++;
                        }
                    }

                    //update page range to generate
                    if (firstPage != pageNumber) {
                        i = -1;
                        firstPage = pageNumber;
                    }

                    //Mandatory sleep so Swing can do it's thing
                    if (i > 10) {
                        while ((speed > 0.005f || speed < -0.005f) && firstPage == pageNumber) {
                            try {
                                Thread.sleep(10);
                            } catch (final InterruptedException e) {

                                //tell user and log
                                if (LogWriter.isOutput()) {
                                    LogWriter.writeLog("Exception: " + e.getMessage());
                                }
                                //
                            }
                        }
                    }
                }
                currentlyAddingPages = false;
                return null;
            }
        };
    
        final Thread th = new Thread(task);
        if (!currentlyAddingPages) {
            currentlyAddingPages = true;
            th.setDaemon(true);
            th.start();
        }
    }

    private boolean checkMemory() {
        final int threshold = 32000000;
        final boolean debugMemory = false;

        if (runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory()) < threshold) {

            if (debugMemory) {
                System.out.println("mem less than threshold - " + pagesInMemory + " pages in memory");
            }

            if (pagesInMemory > 1) {
                if (debugMemory) {
                    System.out.println("Clearing old pages, calling GC and retesting");
                }
               // System.gc();

                boolean shrinkingSuccessful = true;
                while (runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory()) < threshold) {
                    if (pagesToGenerate > 5) {
                        pagesToGenerate--;
                        final int toRemove = pagesInMemory - ((pagesToGenerate * 2) - 1);
                        if (toRemove > 0) {
                            removeFurthestPages(toRemove);
                        }
                      //  System.gc();
                    } else {
                        shrinkingSuccessful = false;
                    }
                }

                if (shrinkingSuccessful) {
                    return false;
                }

                if (runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory()) < threshold) {
                    if (!memoryWarningShown) {
                        if (debugMemory) {
                            System.out.println("Warning about memory issues.");
                        }

                        if (!pageFlowEnding) {
                            isUpdateMemory = true;
                            memoryMessage = Messages.getMessage("PdfViewer.PageFlowLowMemory");
                        }else{
                            isUpdateMemory = false;
                        }

                        memoryWarningShown = true;
                    }
                    if (debugMemory) {
                        System.out.println("Testing finished - no more pages will be added.");
                    }
                    currentlyAddingPages = false;
                    return true;
                }
            } else {
                if (debugMemory) {
                    System.out.println("Removing and cleaning up");
                }
                stop();
                // canvas.stopRenderer(); // No FX equivalent
                if (Platform.isFxApplicationThread()) {

                  //  currentGUI.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);

                } else {
                    final Runnable doPaintComponent = new Runnable() {

                        @Override
                        public void run() {
                          //  currentGUI.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
                        }
                    };
                    Platform.runLater(doPaintComponent);
                }
               
//                pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED); // Commented in Java3D also
                if (!pageFlowEnding) {
                    pageFlowEnding = true;
                    isUpdateMemory = true;
                    memoryMessage = Messages.getMessage("PdfViewer.PageFlowNotEnoughMemory");
                }else{
                    isUpdateMemory = false;
                }
                return true;
            }
        }
        if (debugMemory) {
            System.out.println("Testing finished - adding may resume");
        }
        return false;
    }

    private void removeFurthestPages(final int pagesToRemove) {
        int pagesRemoved = 0;

        final int before = pageNumber - 1;
        final int after = pageCount - pageNumber;

        final int max = before > after ? before : after;
        int cursor = max;

        //Remove even pages further than 40 from center first
        while (pagesRemoved < pagesToRemove) {

            if (cursor < 40) {
                break;
            }

            int pre = (pageNumber - cursor);
            pre -= (1 - (pre & 1));
            if (pre > 0 && pages[pre - 1] != null) {
                pages[pre - 1].dispose();
                pagesRemoved++;
            }

            if (pagesRemoved != pagesToRemove) {
                int post = pageNumber + cursor;
                post -= (1 - (post & 1));
                if (post <= pageCount && pages[post - 1] != null) {
                    pages[post - 1].dispose();
                    pagesRemoved++;
                }

                cursor--;
            }
        }

        cursor = max;

        //remove all pages furthest from center now
        while (pagesRemoved < pagesToRemove) {
            if (cursor < 0) {
                break;
            }

            final int pre = (pageNumber - cursor);
            if (pre > 0 && pages[pre - 1] != null) {
                pages[pre - 1].dispose();
                pagesRemoved++;
            }

            if (pagesRemoved != pagesToRemove) {
                final int post = pageNumber + cursor;
                if (post <= pageCount && pages[post - 1] != null) {
                    pages[post - 1].dispose();
                    pagesRemoved++;
                }

                cursor--;
            }
        }

//        System.gc();
    }

    private double getFullPageWidthOrHeight() {
        return (newSceneHeight / (double) 13) * 12;
    }

    private double getPageWidthOrHeight() {
        return (newSceneHeight / (13 * scaling)) * 12;
    }

    private class Page extends ImageView {

        private final Image lowResImage;
        private final int page;
        private final int rotation;
        private PerspectiveTransform trans;
        private ColorAdjust colorAdjust;
        private int mainTextureSize;
        private ImageView reflection;
        private PerspectiveTransform reflectionTransform;

        private double x, y, widthHeight, altWidthHeight;

        Page(final int page) {
            this.rotation = displayRotation;
            this.page = page;
            lowResImage = getPageImage(page, rotation, textureSize);

            if (lowResImage == null) {
                dispose();
                widthHeight = 0;
                return;
            }

            setImage(lowResImage);

            setupMouseHandlers();

            if (enableReflection) {
                setupReflection();
            }

            colorAdjust = new ColorAdjust();
            if (enablePerspectiveTransform) {
                setupPerspectiveTransform();
            } else {
                //setEffect(colorAdjust); // This causes lag too.
            }

            setCache(true);
            setCacheHint(CacheHint.QUALITY); // This seems to have little effect

            update();
        }

        public void setupReflection() {
            reflection = new ImageView();
            reflectionTransform = new PerspectiveTransform();
            reflectionTransform.setInput(new ColorAdjust(0, 0, -0.75, 0));
            reflection.setEffect(reflectionTransform);
            reflection.setImage(lowResImage);
        }

        public void disposeReflection() {
            reflection = null;
            reflectionTransform = null;
        }

        public void setupPerspectiveTransform() {
            trans = new PerspectiveTransform();
            trans.setInput(colorAdjust);
            setEffect(trans);
        }

        public void disposePerspectiveTransform() {
            trans = null;
            setEffect(null);
            //setEffect(colorAdjust); // This causes lag too.
        }

        private void setupMouseHandlers() {
            /**
             * pageClickEvent is required to let the scene know that we are
             * handling the mouse event. setOnMouseClicked still runs even when
             * the click includes a drag, hence detecting manually.
             */
            setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent e) {
                    pageClickEvent = true;
                }
            });

            setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent e) {
                    pageClickEvent = false;
                }
            });

            setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent e) {
                    if (pageClickEvent) {
                        goTo(page);
                    }
                }
            });
        }

        // Update pageFocus values then redraw.
        public void update() {

            // Set width based on height.
            // Set height based on size of scene & current scaling.
            widthHeight = getPageWidthOrHeight();

            // Set y to move page up slightly.
            y = -widthHeight / 40;
            // Set x based on current pageFocus.
            // If diff > 1 then put pages closer together (divide distance by 5 essentially).
            double diff = page - pageFocus;
            if (diff > 1) {
                diff = ((diff - 1) / 5) + 1;
            } else if (diff < -1) {
                diff = ((diff + 1) / 5) - 1;
            }
            x = diff * widthHeight;

            redraw();
        }

        private void redraw() {
            // If new or old pageFocus is visible then update pageFocus & transform.
            final boolean update = !enablePerspectiveTransform || trans != null && ((getRealX(x) + widthHeight > 0 && getRealX(x) < newSceneWidth)
                    || (trans.getUrx() > 0 && trans.getUlx() < newSceneWidth));

            if (update) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        // Use distance from current pageFocus to define the transform.
                        double diff = page - pageFocus;

                        double ddiff = Math.abs(diff);
                        if (ddiff > 1) {
                            ddiff = ((ddiff - 1) / (enablePerspectiveTransform ? 16 : 8)) + 1;
                        }
                        final double distanceScale = Math.pow(1 - 0.3, ddiff);

                        if (diff > 1) {
                            diff = 1;
                        }
                        if (diff < -1) {
                            diff = -1;
                        }
                        final boolean rightSide = diff > 0;
                        diff = Math.abs(diff);

                        // If the page is in the middle, turn off the PerspectiveTransform as it will sharpen the image by a good margin!
                        if (diff == 0) {
                            setEffect(null);
                        } else {
                            if (enablePerspectiveTransform) {
                                setEffect(trans);
                            }
                        }

                        colorAdjust.setBrightness(-diff / 2);

                        final double halfWidthHeight = widthHeight / 2;
                        double halfDiff = diff / 2;
                        if (!enablePerspectiveTransform) {
                            halfDiff /= 8;
                        }
                        final double quarterDiff = diff / 4;

                        //Set X values
                        final double lx = getRealX(halfWidthHeight + x - (1 - halfDiff) * halfWidthHeight * distanceScale);
                        final double rx = getRealX(halfWidthHeight + x + (1 - halfDiff) * halfWidthHeight * distanceScale);

                        if (enablePerspectiveTransform) {
                            trans.setLlx(lx);
                            trans.setUlx(lx);
                            trans.setLrx(rx);
                            trans.setUrx(rx);

                            // Set Y values
                            if (rightSide) { // Slant to left
                                trans.setLly(getRealY(halfWidthHeight + y + (1 - quarterDiff) * halfWidthHeight * distanceScale));
                                trans.setUly(getRealY(halfWidthHeight + y - (1 - quarterDiff) * halfWidthHeight * distanceScale));
                                trans.setLry(getRealY(halfWidthHeight + y + halfWidthHeight * distanceScale));
                                trans.setUry(getRealY(halfWidthHeight + y - halfWidthHeight * distanceScale));
                            } else { // Slant to right
                                trans.setLry(getRealY(halfWidthHeight + y + (1 - quarterDiff) * halfWidthHeight * distanceScale));
                                trans.setUry(getRealY(halfWidthHeight + y - (1 - quarterDiff) * halfWidthHeight * distanceScale));
                                trans.setLly(getRealY(halfWidthHeight + y + halfWidthHeight * distanceScale));
                                trans.setUly(getRealY(halfWidthHeight + y - halfWidthHeight * distanceScale));
                            }
                        }

                        if (enableReflection) {
                            // Set reflection X values
                            reflectionTransform.setLlx(lx);
                            reflectionTransform.setUlx(lx);
                            reflectionTransform.setLrx(rx);
                            reflectionTransform.setUrx(rx);
                            // Set reflection y values
                            reflectionTransform.setLly(trans.getLly());
                            reflectionTransform.setLry(trans.getLry());
                            reflectionTransform.setUly(trans.getLly() + (trans.getLly() - trans.getUly()));
                            reflectionTransform.setUry(trans.getLry() + (trans.getLry() - trans.getUry()));
                        }

                        // Set X,Y,Width,Height.
                        // This is redundant after setting the PerspectiveTransform but still useful to set.
                        if (!enablePerspectiveTransform) {
                            altWidthHeight = (rx - lx);
                            if (diff == 0) {
                                setFitWidth((int) altWidthHeight);
                                setFitHeight((int) altWidthHeight);
                            } else {
                                setFitWidth(altWidthHeight);
                                setFitHeight(altWidthHeight);
                            }
                        } else {
                            if (diff == 0) {
                                setFitWidth((int) widthHeight);
                                setFitHeight((int) widthHeight);
                            } else {
                                setFitWidth(widthHeight);
                                setFitHeight(widthHeight);
                            }
                        }

                        if (diff == 0) {
                            setX((int) getRealX(x));
                            setY((int) getRealY(y));
                        } else {
                            setX(getRealX(x));
                            setY(getRealY(y));
                        }
                    }
                });
            }
        }

        public void setMain(final boolean isMain) {
            if (isMain) {
                final Thread t = new Thread("FX-setMain") {
                    @Override
                    public void run() {
                        if (checkMemory()) {
                            return;
                        }
//                        if (!isFX) {
                            mainTextureSize = (int) getFullPageWidthOrHeight();
                            final Image img = getPageImage(page, rotation, mainTextureSize);
                                    
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {

                                    if (img != null) {
                                        setImage(img);

                                    }
                                }
                            });
//                        }
                    }
                };
                t.setDaemon(true);
                t.start();
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        setImage(lowResImage);
                    }
                });
            }
        }

        private ImageView getReflection() {
            return reflection;
        }

        // Convert an x pageFocus into a real world coordinate.
        private double getRealX(final double x) {
            return sceneXOffset + getXOffset() + x;
        }

        // Convert a y pageFocus into a real world coordinate.
        private double getRealY(final double y) {
            return sceneYOffset + getYOffset() + y;
        }

        private double getXOffset() {
            if (enablePerspectiveTransform) {
                return -widthHeight / 2;
            } else {
                return -altWidthHeight / 2;
            }
        }

        private double getYOffset() {
            if (enablePerspectiveTransform) {
                return -widthHeight / 2;
            } else {
                return -altWidthHeight / 2;
            }
        }

        public void dispose() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setImage(null);
                }
            });
            pagesInMemory--;
            pages[page - 1] = null;
        }
    }

    private class NavBar extends Parent {

        private final Line navLine;
        private final Circle navCircle;
        private static final int distanceFromSides = 20;
        private static final int distanceFromBottom = 15;
        private boolean handlingMouse;

        NavBar() {
            navLine = new Line();
            navLine.setStrokeWidth(1.5);
            navLine.setStroke(Color.WHITE);
            navCircle = new Circle(5);
            navCircle.setStrokeWidth(2);
            navCircle.setStroke(Color.WHITE);
            navCircle.setFill(Color.GRAY);
            
            getChildren().addAll(navLine, navCircle);
            
        }

        public boolean isNavBarHover(final MouseEvent mouseEvent) {
            return mouseEvent.getY() > newSceneHeight - (distanceFromBottom * 2);
        }

        public boolean isNavBarPress(final MouseEvent mouseEvent) {
            if (mouseEvent.getY() > newSceneHeight - (distanceFromBottom * 2)) {
                handlingMouse = true;
                return true;
            } else {
                return false;
            }
        }

        public boolean isNavBarDrag(final MouseEvent mouseEvent) {
            if (handlingMouse) {
                double x = mouseEvent.getX();
                if (x < distanceFromSides) {
                    x = distanceFromSides;
                }
                if (x > newSceneWidth - distanceFromSides) {
                    x = newSceneWidth - distanceFromSides;
                }
                if (x != navCircle.getCenterX()) {
                    navCircle.setCenterX(x);
                    final double percent = (x - distanceFromSides) / (newSceneWidth - (distanceFromSides * 2));
                    pageFocus = ((pageCount - 1) * percent) + 1;
                    final int newPageNumber = (int) (pageFocus + 0.5);
                    if (pageNumber != newPageNumber) {
                        setPageNumber(newPageNumber);
                    }
                    addPages();
                    reorderPages(pageFocus, false);
                    for (final Page page : pages) {
                        if (page != null) {
                            page.update();
                        }
                    }
                }

                return true;
            } else {
                return false;
            }
        }

        public boolean isNavBarRelease(final MouseEvent mouseEvent) {
            if (handlingMouse) {
                double x = mouseEvent.getX();
                if (x < distanceFromSides) {
                    x = distanceFromSides;
                }
                if (x > newSceneWidth - distanceFromSides) {
                    x = newSceneWidth - distanceFromSides;
                }

                final double percent = (x - distanceFromSides) / (newSceneWidth - (distanceFromSides * 2));
                final int newPageNumber = (int) ((((pageCount - 1) * percent) + 1) + 0.5);
                if (pageNumber != newPageNumber) {
                    setPageNumber(newPageNumber);
                }
                goTo(pageNumber);

                handlingMouse = false;
                return true;
            } else {
                return false;
            }
        }

        public void update() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    navCircle.setCenterY(newSceneHeight - distanceFromBottom);
                    navLine.setStartX(distanceFromSides);
                    navLine.setStartY(newSceneHeight - distanceFromBottom + 0.5);// +0.5 to make pixel perfect line
                    navLine.setEndX(newSceneWidth - distanceFromSides);
                    navLine.setEndY(newSceneHeight - distanceFromBottom + 0.5);// +0.5 to make pixel perfect line
                    final double percent = (pageFocus - 1) / (pageCount - 1);
                    final double x = distanceFromSides + ((newSceneWidth - (distanceFromSides * 2)) * percent);
                    navCircle.setCenterX(x);
                }
            });
        }
    }

    private class ZoomBar extends Parent {

        private final Line zoomLine;
        private final Circle zoomCircle;
        private static final int distanceFromSide = 15;
        private boolean handlingMouse;

        ZoomBar() {
            zoomLine = new Line();
            zoomLine.setStrokeWidth(1.5);
            zoomLine.setStroke(Color.WHITESMOKE);
            zoomCircle = new Circle(5);
            zoomCircle.setStrokeWidth(2);
            zoomCircle.setStroke(Color.WHITE);
            zoomCircle.setFill(Color.GRAY);

            getChildren().addAll(zoomLine, zoomCircle);
        }

        public boolean isZoomBarHover(final MouseEvent mouseEvent) {
            return mouseEvent.getX() < distanceFromSide * 2
                    && mouseEvent.getY() > getStartY() - 5 && mouseEvent.getY() < getEndY() + 5; // Allow +/- 5
        }

        public boolean isZoomBarPress(final MouseEvent mouseEvent) {
            if (mouseEvent.getX() < distanceFromSide * 2
                    && mouseEvent.getY() > getStartY() - 5 && mouseEvent.getY() < getEndY() + 5) { // Allow +/- 5
                handlingMouse = true;
                isZoomBarDrag(mouseEvent);// Borrow what happens with the drag!
                return true;
            } else {
                return false;
            }
        }

        public boolean isZoomBarDrag(final MouseEvent mouseEvent) {
            if (handlingMouse) {
                double y = mouseEvent.getY();
                final double start = getStartY();
                final double end = getEndY();
                if (y < start) {
                    y = start;
                }
                if (y > end) {
                    y = end;
                }
                if (y != zoomCircle.getCenterY()) {
                    zoomCircle.setCenterY(y);
                    final double percent = (y - start) / (end - start);
                    scaling = 1 + percent;
                    for (final Page page : pages) {
                        if (page != null) {
                            page.update();
                        }
                    }
                }

                return true;
            } else {
                return false;
            }
        }

        public boolean isZoomBarRelease() {
            if (handlingMouse) {
                handlingMouse = false;
                return true;
            } else {
                return false;
            }
        }

        public void update() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    zoomCircle.setCenterX(distanceFromSide);
                    zoomLine.setStartX(distanceFromSide + 0.5);// +0.5 to make pixel perfect line
                    zoomLine.setStartY(newSceneHeight * 0.2);
                    zoomLine.setEndX(distanceFromSide + 0.5);// +0.5 to make pixel perfect line
                    zoomLine.setEndY(newSceneHeight * 0.4);

                    final double percent = 2 - scaling;
                    final double start = getStartY();
                    final double end = getEndY();
                    zoomCircle.setCenterY(end - ((end - start) * percent));
                }
            });
        }

        private double getStartY() {
            return newSceneHeight * 0.2;
        }

        private double getEndY() {
            return newSceneHeight * 0.4;
        }
    }

    public PdfDecoderInt getPdfDecoderInt() {
        return pdf;
    }

    final DoubleProperty pageNumberProperty = new SimpleDoubleProperty();
    public DoubleProperty getPageNumber() {
        return pageNumberProperty;
    }
    private void setPageNumber(final int pn){
        pageNumberProperty.set(pn);
        pageNumber = pn;
    }
    
    private boolean isUpdateMemory;
    public boolean isUpdateMemory(){
        return isUpdateMemory;
    }
    
    private String memoryMessage;
    public String getMemoryMessage(){
        return memoryMessage;
    }
}
