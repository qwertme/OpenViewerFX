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
 * JavaFXThumbnailPanel.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx;

import java.awt.Font;
import java.awt.image.BufferedImage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jpedal.PdfDecoderInt;
import org.jpedal.ThumbnailDecoder;
import org.jpedal.examples.viewer.Values;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.objects.PdfPageData;

/**
 * Used in GUI example code.
 * <br>adds thumbnail capabilities to viewer,
 * <br>shows pages as thumbnails within this panel,
 * <br>So this panel can be added to the viewer
 *
 */
public class JavaFXThumbnailPanel extends Tab implements GUIThumbnailPanel {
    
    private static final boolean debugThumbnails = false;
    
    /**can switch on or off thumbnails*/
	private boolean showThumbnailsdefault=true;
    private boolean generateOtherVisibleThumbnails;
    private boolean showThumbnails=showThumbnailsdefault;
    
    private final PdfDecoderInt decode_pdf;
    private final ThumbnailDecoder thumbDecoder;
    
	/**width and height for thumbnails*/
    private static final int thumbH=100,thumbW=70;
    private Task<Void> worker;
    private boolean drawing;
    private static final boolean debug = true;
    private boolean interrupt;
    
    private Button[] pageButton;
    private final VBox content;
    private final ScrollPane scroll;
    private int lastPage;
    private boolean[] isLandscape;
    private int[] pageHeight;
    private BufferedImage[] images;
    private boolean[] buttonDrawn;
    private final ThumbPainter painter;
    private final BorderListener border;

    private final Border selectedBorder;
    private final Border hoverBorder;
    private final Border emptyBorder;

    public JavaFXThumbnailPanel(final PdfDecoderInt decode_pdf) {
        border = new BorderListener();
        painter = new ThumbPainter();
        this.decode_pdf = decode_pdf;
        thumbDecoder = new ThumbnailDecoder(decode_pdf);
        
        content = new VBox();
        scroll = new ScrollPane(content);
        
        content.setAlignment(Pos.CENTER);
        
        scroll.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override public void changed(final ObservableValue<? extends Bounds> ov, final Bounds oldb, final Bounds newb) {
                // binding the pref width doesn't seem to work in this context so using a listener
                content.setPrefWidth(newb.getWidth());
                
                if(drawing) {
                    terminateDrawing();
                }

                decode_pdf.waitForDecodingToFinish();

                if(decode_pdf.isOpen()) {
                    drawThumbnails();
                }
            }
        });
        
        selectedBorder = new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2)));
        hoverBorder = new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2)));
        emptyBorder = new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2)));
        
        this.setContent(scroll);
    }
    
    @Override
    public boolean isShownOnscreen() {
        return showThumbnails;
    }

    @Override
    public void terminateDrawing() {
        
        generateOtherVisibleThumbnails = false;
        
        if(drawing){
            interrupt = true;
            // Removed the loop that was in Swing
            // This is due to deadlocking with the FX application Thread
            // So now what happens is we call interupt and allow any thumbnails
            // being decoded to finish.
        }
        
    }

    @Override
    public void setIsDisplayedOnscreen(final boolean b) {
        showThumbnails = b;
    }

    @Override
    public Object[] getButtons() {
        return pageButton;
    }

    @Override
    public void addComponentListener() {
        scroll.vvalueProperty().addListener(painter);
    }

    @Override
    public synchronized void generateOtherVisibleThumbnails(final int currentPage) {
        
            //flag to show drawing which terminate can reset
            generateOtherVisibleThumbnails=true;

            //stop multiple calls
            if(currentPage==-1 || currentPage==lastPage || pageButton==null) {
                return;
            }

            final int count = decode_pdf.getPageCount();
            
            for(int i = 0; i < count; i++){
                if(!generateOtherVisibleThumbnails) {
                    return;
                }
                pageButton[i].setBorder(emptyBorder);
            }
            
            if((currentPage-1 < pageButton.length)&& 
                (count > 1 && currentPage > 0)){
                    pageButton[currentPage-1].setBorder(selectedBorder);
                }
            
            final double pos = ((scroll.getVmax() / count) * (currentPage-1));
            scroll.setVvalue( pos);
            
            if(!generateOtherVisibleThumbnails) {
                return;
            }

            //commonValues.setProcessing(false);

            /** draw thumbnails in background, having checked not already drawing */
            if(drawing) {
                terminateDrawing();
            }

            if(!generateOtherVisibleThumbnails) {
                return;
            }

            /** draw thumbnails in background */
            drawThumbnails();
        
    }

    @Override
    public void setupThumbnails(final int pages, final Font textFont, final String message, final PdfPageData pageData) {
        
        lastPage = -1;
        
        content.getChildren().clear();
        
        final Image blankPortrait = createBlankThumbnail(thumbW, thumbH);
        final Image blankLandscape = createBlankThumbnail(thumbH, thumbW);
        
        isLandscape = new boolean[pages];
        pageHeight = new int[pages];
        pageButton = new Button[pages];
        images = new BufferedImage[pages];
        buttonDrawn = new boolean[pages];
                
        for(int i = 0; i < pages; i++){
            
			final int page=i+1;
            
			//create blank image with correct orientation
			final int ph;//pw
			final int cropWidth=pageData.getCropBoxWidth(page);
			final int cropHeight=pageData.getCropBoxHeight(page);
			final int rotation=pageData.getRotation(page);
			//Image usedLandscape;
            final Image usedPortrait;

            if((rotation==0)|(rotation==180)){
				ph=(pageData.getMediaBoxHeight(page));
				//pw=(pageData.getMediaBoxWidth(page));//%%
				//usedLandscape = blankLandscape;
				usedPortrait = blankPortrait;
			}else{
				ph=(pageData.getMediaBoxWidth(page));
				//pw=(pageData.getMediaBoxHeight(page));//%%
				//usedLandscape = blankPortrait;
				usedPortrait = blankLandscape;
			}
            
            if(cropWidth > cropHeight){
//                VBox contents = new VBox(new ImageView(usedLandscape), new Label(message + ' ' + page));
                final Button imageButton = new Button(message + ' ' + page, new ImageView(usedPortrait));
                pageButton[i] = imageButton;
                isLandscape[i] = true;
            }else{
//                VBox contents = new VBox(new ImageView(usedPortrait), new Label());
                final Button imageButton = new Button(message + ' ' + page, new ImageView(usedPortrait));
                pageButton[i] = imageButton;
                isLandscape[i] = false;
            }
			
            pageHeight[i] = ph;
            pageButton[i].setContentDisplay(ContentDisplay.TOP);
            // Remove button styles
            pageButton[i].setStyle("-fx-padding:0;-fx-background-color:transparent;");
            
            if(i == 0 && pages > 1){
                pageButton[0].setBorder(selectedBorder);
            }else{
                pageButton[i].setBorder(emptyBorder);
            }
            
            pageButton[i].prefWidthProperty().bind(content.prefWidthProperty());
            content.getChildren().add(pageButton[i]);
            
            pageButton[i].setOnMouseEntered(border);
            pageButton[i].setOnMouseExited(border);
        }
        
    }

    @Override
    public void resetToDefault() {
        showThumbnails = showThumbnailsdefault;
    }

    @Override
    public void removeAllListeners() {
        scroll.vvalueProperty().removeListener(painter);
        for(final Button btn : pageButton){
            btn.setOnMouseExited(null);
            btn.setOnMouseEntered(null);
            btn.setOnAction(null);
        }
    }

    @Override
    public void setThumbnailsEnabled(final boolean newValue) {
		showThumbnailsdefault=newValue;
		showThumbnails=newValue;
    }

    @Override
    public void dispose() {
    }

 
    @Override
    public synchronized BufferedImage getImage(int page){

        //actually stored starting 0 not 1 so adjust
        page--;

        if(images==null || images[page]==null){
            if(page>-1){
                int h = thumbH;
                if (isLandscape[page]) {
                    h = thumbW;
                }

                final BufferedImage image = thumbDecoder.getPageAsThumbnail(page + 1, h);
                images[page]=image;

                return image;
            }else {
                return null;
            }
        }else {
            return images[page];
        }
    }

    @Override
    public void drawThumbnails() {
        if(!isSelected()) {
            return;
        }
        
        //do not generate if still loading Linearized
        if(decode_pdf.isLoadingLinearizedPDF()) {
            return;
        }
        
        // Allow for re-entry
        if(drawing) {
            this.terminateDrawing();
        }
        
        worker = new Task<Void>() {

            @Override
            protected Void call() throws Exception{
                
                if(buttonDrawn != null && pageButton != null){
                    drawing = true;

                    try{
                        final double minY = (scroll.getVvalue() * content.getHeight() - (scroll.getHeight() * scroll.getVvalue()));
                        
                        final Rectangle visible = new Rectangle(0, minY, scroll.getWidth(), scroll.getHeight());
                        final int pages = decode_pdf.getPageCount();
                        
                        for(int i = 0; i < pages; i++){
                            decode_pdf.waitForDecodingToFinish();
                            //  i > pageButton.length fixes exception thrown when opening a new PDF
                            if(interrupt || i > pageButton.length){
                                i=pages;
                            } else if (!buttonDrawn[i] && pageButton[i] != null
                                    && visible.intersects(pageButton[i].getBoundsInParent())){
                                
                                int h = thumbH;
                                if(isLandscape[i]) {
                                    h = thumbW;
                                }
                                
                                final BufferedImage page = thumbDecoder.getPageAsThumbnail(i+1, h);
                                if(!interrupt) {
                                    createThumbnail(page, i);
                                }
                                
                            }

                        }

                    }catch(final Exception e){
                        e.printStackTrace();
                    }
                }
                
				//always reset flag so we can interupt
				interrupt=false;
				
				drawing=false;
                
                return null;
            }
        };
        
        new Thread(worker, "Thumbnail Thread").start();
    }
        
    /**
	 * create a blank tile with a cross to use as a thumbnail for unloaded page
	 */
	private static Image createBlankThumbnail(final int w, final int h) {
        final Canvas canvas = new Canvas(w,h);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.setFill(Color.WHITE);
        gc.fillRect(0.2, 0.2, w-.5, h-.5);
        gc.setFill(Color.BLACK);
        gc.strokeRect(0.2, 0.2, w-.5, h-.5);
        gc.strokeLine(0, 0, w, h);
        gc.strokeLine(0, h, w, 0);
        
        return canvas.snapshot(null, null);
	}

    private void createThumbnail(final BufferedImage page, final int i){
        
        if(page!=null){
            final Image image = SwingFXUtils.toFXImage(page, null);
            if(Platform.isFxApplicationThread()){
                ((ImageView)pageButton[i].getGraphic()).setImage(image);
                buttonDrawn[i] = true;
                images[i] = page;
            }else{
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        ((ImageView)pageButton[i].getGraphic()).setImage(image);
                        buttonDrawn[i] = true;
                        images[i] = page;
                    }
                });
            }
        }
    }
    
    private class ThumbPainter implements ChangeListener<Number>{
        private boolean requestMade;
        
        private final Timeline trapMultipleMovements = new Timeline(new KeyFrame(Duration.millis(250), new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent event) {
                if(!requestMade){
                    requestMade = true;
                    
                    if(!Values.isProcessing){
                        if(drawing) {
                            terminateDrawing();
                        }
                        
                        requestMade = false;
                        drawThumbnails();
                    }
                    
                }
            }
        }));
        
        @Override 
        public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
            if(trapMultipleMovements.getStatus() == Animation.Status.RUNNING) {
                trapMultipleMovements.stop();
            }
            
            trapMultipleMovements.setCycleCount(1);
            trapMultipleMovements.playFromStart();
            
        }
        
    }
    
    private class BorderListener implements EventHandler<MouseEvent>{
        @Override
        public void handle(final MouseEvent event) {
            final Button source = (Button)event.getSource();
            final boolean isSelected = source.getBorder().equals(selectedBorder);
            if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED) && !isSelected){
                source.setBorder(hoverBorder);
            }else if(event.getEventType().equals(MouseEvent.MOUSE_EXITED) && !isSelected){
                source.setBorder(emptyBorder);
            }
        }
    }
    
}
