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
 * BaseViewerFX.java
 * ---------------
 */

package org.jpedal.examples.baseviewer;


import java.io.File;
import java.util.List;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jpedal.PdfDecoderFX;
import org.jpedal.examples.viewer.gui.javafx.FXViewerTransitions;
import org.jpedal.examples.viewer.gui.javafx.FXViewerTransitions.TransitionDirection;
import org.jpedal.examples.viewer.gui.javafx.FXViewerTransitions.TransitionType;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXInputDialog;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;
import org.jpedal.parser.DecoderOptions;

/** <h2><b>BaseViewerFX</b></h2>
 * 
 * <p>If you are compiling, you will need to download all the examples source files from :
 * <a href="http://www.idrsolutions.com/how-to-view-pdf-files-in-java/">How to View PDF File in Java.</a></p>
 *
 * <p><b>Run directly from jar with java -cp jpedal.jar org/jpedal/examples/baseviewer/BaseViewerFX</b></p>
 *
 * <p>There are plenty of tutorials on how to configure the Viewer on our website <a href="http://www.idrsolutions.com/java-pdf-library-support/">Support Section.</a></p>
 * 
 * <p>This class provides example code to create a bare-bones PDF Viewer in JavaFX.</p>
 * 
 * <p><a href="http://files.idrsolutions.com/samplecode/org/jpedal/examples/baseviewer/BaseViewer.java.html">Click here for a bare-bones PDF Viewer in Java Swing.</a></p>
 * 
 * <p><a href="http://www.idrsolutions.com/java-pdf-library-support/http://www.idrsolutions.com/java-pdf-library-support/">For more help and tutorials, visit our websites Support Section.</a></p>
 * 
 */
public class BaseViewerFX extends Application {
    
    private final org.jpedal.PdfDecoderFX pdf = new org.jpedal.PdfDecoderFX();

    /**
     * Enum to control how we fit the content to the page.
     * 
     * AUTO will automatically fit the content to the stage depending on its orientation
     * WIDTH will fit the content to the stage width depending on its orientation
     * HEIGHT will fit the content to the stage height depending on its orientation
     */
    public enum FitToPage{
        AUTO, WIDTH, HEIGHT, NONE
    }
    
    String PDFfile;
    
    //Variable to hold the current file/directory
    File file;
    
    //These two variables are todo with PDF encryption & passwords
    private String password; //Holds the password from the JVM or from User input
    private boolean closePasswordPrompt; //boolean controls whether or not we should close the prompt box
    
    
    // Layout panes
    private VBox top;
    private HBox bottom;
    private ScrollPane center;
    //Group is a container which holds the decoded PDF content
    private Group group;
    
    // for the location of the pdf file
    private Text fileLoc;
    
    private float scale = 1.0f;
    
    private final float[] scalings = {0.01f, 0.1f, 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 4.0f, 7.5f, 10.0f};
    
    private int currentScaling=5;
    
    private static final float insetX = 25;
    
    private static final float insetY = 25;
    
    private int currentPage = 1;
    
    Stage stage;
    
    Scene scene;
    
    //Controls size of the stage, in theory setting this to a higher value will
    //increase image quality as there's more pixels due to higher image resolutions
    static final int FXscaling=1;
    
    FitToPage zoomMode = FitToPage.AUTO;
    
    private TransitionType transitionType = TransitionType.None;
    
    public static void main(final String[] args){
         DecoderOptions.javaVersion = Float.parseFloat(System.getProperty("java.specification.version"));
         System.out.println("JAVA VERSION : "+ DecoderOptions.javaVersion);
            if (DecoderOptions.javaVersion < 1.8f) {
                throw new RuntimeException("You need to Run Java 1.8+");
            }
        launch(args);
    }
    
    /**
     * launches BaseViewerFX viewer using supplied stage for displaying PDF files
     * 
     * @param stage is of type final Stage
     */
    @Override
    public void start(final Stage stage){
        stage.setTitle("Base Viewer FX - " + PdfDecoderFX.version);
        
        // Set transition if set in properites:
        String trans = System.getProperty("org.jpedal.fxtransition");
        if(trans != null){
            // Ensure correct lettering 
            trans = trans.substring(0, 1).toUpperCase() + trans.substring(1).toLowerCase();
          
                transitionType = TransitionType.valueOf(trans);
         
        }
        
        // Set page if set in JVM flag
        final String pageNum = System.getProperty("org.jpedal.page");
        if(pageNum != null){

                currentPage = Integer.parseInt(pageNum);
      
        }
        
        this.stage=stage;
        scene=setupViewer(800,600);
        
        // Get command line arguments
        final List<String> args = this.getParameters().getUnnamed();

        /**
         * setup initial display
         * Setting this before loadPDF() gives access to the toolbar buttons 
         * when called in loadPDF() via id.
         */
        stage.setScene(scene);
        stage.show();
        
        // this.getParameters().getUnamed() will never be null according to FX Javadoc, so no null check needed
        // http://docs.oracle.com/javafx/2/api/javafx/application/Application.Parameters.html#getUnnamed%28%29
        if(args.size() == 1){
            final String input = args.get(0);
            file = new File(input);
            Platform.runLater(new Runnable() {
            @Override
            public void run() {
                loadPDF(file);
                // Unset the page
                if(System.getProperty("org.jpedal.page") != null) {
                    System.setProperty("org.jpedal.page", "");
                }
            }
        });
        
        }
        
        addListeners();
    }
    
    /**
     * creates all the components and adds change listeners for auto-centering 
     * for JavaFX PDF viewer
     * 
     * @param w The width to use for the viewer 
     * @param h The height to use for the viewer 
     * @return scene
     */
    public Scene setupViewer(final int w, final int h){
    
        /* 
         * Setting up layout panes and assigning them to the appropiate locations
         */
        final BorderPane root = new BorderPane();
        
        top = new VBox();
        
        root.setTop(top);
        
        top.getChildren().add(setupToolBar());
        
        bottom = new HBox();
        bottom.setPadding(new Insets(0,10,0,10));
        root.setBottom(bottom);
        
        center = new ScrollPane();
        center.setPannable(true);
        center.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        center.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        //needs to be added via group so resizes (see http://pixelduke.wordpress.com/2012/09/16/zooming-inside-a-scrollpane/)
        group=new Group();
        group.getChildren().add(pdf);
        center.setContent(group);
        root.setCenter(center);
        
        center.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(final ObservableValue<? extends Bounds> ov, final Bounds ob, final Bounds nb) {
                adjustPagePosition(nb);
            }
        });
        
        /**Sets the text to be displayed at the bottom of the FX Viewer**/
        fileLoc = new Text("No PDF Selected");
        fileLoc.setId("file_location");
        bottom.getChildren().add(fileLoc);
        
        
        scene=new Scene(root, w*FXscaling, h*FXscaling);
        
        //
        return scene;
    }
    
    public void addListeners(){
        
             
        /**
         * auto adjust so dynamically resized as viewer width alters
         */
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observableValue, final Number oldSceneWidth, final Number newSceneWidth) {

                fitToX(zoomMode);

            }
        });
        
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observableValue, final Number oldSceneHeight, final Number newSceneHeight) {

                fitToX(zoomMode);

            }
        });
        
        /**
         * Controls for dragging a PDF into the scene
         * Using the dragboard, which extends the clipboard class, 
         * detect a file being dragged onto the scene and if the user drops the file
         * we load it.
         */
        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(final DragEvent event) {
                final Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });
        
        scene.setOnDragDropped(new EventHandler<DragEvent>() {        
            @Override
            public void handle(final DragEvent event) {
                final Dragboard db = event.getDragboard();
                boolean success = false;
                if(db.hasFiles()){
                    success = true;
                    // Only get the first file from the list
                    file = db.getFiles().get(0);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadPDF(file);
                        }
                    });
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }
    
    /**
     * Sets up a MenuBar to be used at the top of the window.
     * 
     * It contains one Menu - navMenu - which allows the user to open and navigate pdf files
     * 
     * @return 
     */
    private ToolBar setupToolBar() {

        final ToolBar toolbar = new ToolBar();

        final Button open = new Button("Open");
        final Button back = new Button("Back");
        final ComboBox<String> pages = new ComboBox<String>();
        final Label pageCount = new Label();
        final Button forward = new Button("Forward");
        final Button zoomIn = new Button("Zoom in");
        final Button zoomOut = new Button("Zoom out");
        final Button fitWidth = new Button("Fit to Width");
        final Button fitHeight = new Button("Fit to Height");
        final Button fitPage = new Button("Fit to Page");
        final Button fullScreen = new Button("Full Screen");
        ComboBox<String> transitionList = new ComboBox<String>();

        open.setId("open");
        back.setId("back");
        pageCount.setId("pgCount");
        pages.setId("pages");
        forward.setId("forward");
        zoomIn.setId("zoomIn");
        zoomOut.setId("zoomOut");
        fitWidth.setId("fitWidth");
        fitHeight.setId("fitHeight");
        fitPage.setId("fitPage");
        fullScreen.setId("fullScreen");

        /**
         * Toggle Full-screen Mode
         */
        if(stage!=null){
            fullScreen.setOnAction(new EventHandler<ActionEvent>() {
                //Toggle between fullscreen and windowed
                @Override
                public void handle(final ActionEvent t) {
                    if (stage.isFullScreen()) {
                        stage.setFullScreen(false);
                    } else {
                        stage.setFullScreen(true);
                    }
                }
            });
        }

        /**
         * Open the PDF File
         */
        open.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                final FileChooser chooser = new FileChooser();
                chooser.setTitle("Open PDF file");
                
                //Open directory from existing directory
                if(file != null){
                    final File existDirectory = file.getParentFile();
                    if(existDirectory.exists()) {
                        chooser.setInitialDirectory(existDirectory);
                    }
                }
 
                //Set extension filter
                final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
                chooser.getExtensionFilters().add(extFilter);
                
                file = chooser.showOpenDialog(null);
                
                if(file != null){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadPDF(file);
                        }
                    });                  
                }              
            }
        });
        
        pages.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(final ObservableValue<? extends Number> ov, final Number oldVal, final Number newVal) {
                if(newVal.intValue() != -1 && newVal.intValue()+1 != currentPage){
                    final int newPage = newVal.intValue() + 1;
                    goToPage(newPage);
                }
            }});
        
        // Navigate backward
        back.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent t) {
                if (currentPage > 1) {
                    goToPage(currentPage - 1);
                }
                
            }
        });
        
        // Navigate forward
        forward.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                if (currentPage < pdf.getPageCount()) {
                    goToPage(currentPage + 1);
                }

            }
        });
        
        // Zoom in
        zoomIn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent t) {
                zoomMode = FitToPage.NONE;
                
                if (currentScaling < scalings.length - 1) {

                    currentScaling = findClosestIndex(scale, scalings);

                    if (scale >= scalings[findClosestIndex(scale, scalings)]) {

                        currentScaling++;

                    }

                    scale = scalings[currentScaling];

                }

                pdf.setPageParameters(scale, currentPage);
                adjustPagePosition(center.getViewportBounds());
            }
        });

        // Zoom out
        zoomOut.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent t) {
                zoomMode = FitToPage.NONE;

                if (currentScaling > 0) {

                    currentScaling = findClosestIndex(scale, scalings);

                    if (scale <= scalings[findClosestIndex(scale, scalings)]) {

                        currentScaling--;

                    }

                    scale = scalings[currentScaling];

                }

                pdf.setPageParameters(scale, currentPage);
                adjustPagePosition(center.getViewportBounds());
            }
        });

        // Fit to width
        fitWidth.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent t) {
                zoomMode = FitToPage.WIDTH;
                fitToX(FitToPage.WIDTH);    

            }
        });

        // Fit to height
        fitHeight.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent t) {
                zoomMode = FitToPage.HEIGHT;
                fitToX(FitToPage.HEIGHT);

            }
        });

        // Fit to Page
        fitPage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent t) {
                zoomMode = FitToPage.AUTO;
                fitToX(FitToPage.AUTO);


            }
        });
        
        
        final Region spacerLeft = new Region();
        final Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);
        
        

        // Set up the ComboBox for transitions
        final ObservableList<String> options = FXCollections.observableArrayList();
        
        for(final TransitionType transition : TransitionType.values()){
            options.add(transition.name());
        }
        
        if(!options.isEmpty()){
            transitionList = new ComboBox<String>(options);
            // Put before setValue so that setValue triggers the event
            transitionList.valueProperty().addListener(new ChangeListener<String>(){
                @Override public void changed(final ObservableValue<? extends String> ov, final String oldVal, final String newVal) {
                    transitionType = TransitionType.valueOf(newVal);
                }});
            
            transitionList.setValue(options.get(transitionType.ordinal()));
        }
        
        toolbar.getItems().addAll(open, spacerLeft, back, pages, pageCount, forward, zoomIn, zoomOut, spacerRight, fullScreen, transitionList);
        
        return toolbar;
    }
    
    /**
     * take a File handle to PDF file on local filesystem and displays in PDF viewer
     * 
     * @param input  The PDF file to load in the viewer 
     */
    public void loadPDF(final File input){
                        
        if(input == null) {
            return;
        }
        
        scale = 1; //reset to default for new page

        PDFfile=input.getAbsolutePath();
        fileLoc.setText(PDFfile);
        
        openFile(input,null,false);
                
    }
    
    /**
     * take a File handle to PDF file on local filesystem and displays in PDF viewer
     * 
     * @param input The PDF file to load in the viewer  
     */
    public void loadPDF(final String input){
        
        if(input == null) {
            return;
        }
        
        scale = 1; //reset to default for new page
        PDFfile=input;
        fileLoc.setText(PDFfile);
           
        if(input.startsWith("http")){
            openFile(null, input,true);
        }else{
            openFile(new File(input),null,false);
        }
        
    }

    private void openFile(final File input,String url, boolean isURL) {
        try {
            //Open the pdf file so we can check for encryption
            if(isURL){
                pdf.openPdfFileFromURL(url,false);
            }else{
                pdf.openPdfFile(input.getAbsolutePath());
            }
            
            if(System.getProperty("org.jpedal.page") != null && !System.getProperty("org.jpedal.page").isEmpty()){
                currentPage = currentPage < 1 ? 1 : currentPage;
                currentPage = currentPage > pdf.getPageCount() ? pdf.getPageCount() : currentPage;
            }else{
                currentPage = 1;
            }
            /**
             * This code block deals with user input and JVM passwords in Encrypted PDF documents.
             */
            if(pdf.isEncrypted()){
                
                int passwordCount = 0;        //Monitors how many attempts there have been to the password
                closePasswordPrompt = false;  //Do not close the prompt box
                
                //While the PDF content is not viewable, repeat until the correct password is found
                while(!pdf.isFileViewable() && !closePasswordPrompt) {
                    
                    /**
                     * See if there's a JVM flag for the password & Use it if there is
                     * Otherwise prompt the user to enter a password
                     */
                    if(System.getProperty("org.jpedal.password")!=null){
                        password = System.getProperty("org.jpedal.password");
                    }else if(!closePasswordPrompt){
                        showPasswordPrompt(passwordCount);
                    }
                    
                    //If we have a password, try and open the PdfFile again with the password
                    if (password != null) {
                        
                        if(isURL){
                            pdf.openPdfFileFromURL(url,false,password);
                        }else{
                            pdf.openPdfFile(input.getAbsolutePath());
                        }
                        //pdf.setEncryptionPassword(password);
                        
                    }
                    passwordCount += 1; //Increment he password attempt
                    
                }
                
            }

            // Set up top bar values
            ((Label)top.lookup("#pgCount")).setText("/" + pdf.getPageCount());
            final ComboBox<String> pages = ((ComboBox<String>)top.lookup("#pages"));
            pages.getItems().clear();
            for(int i = 1; i <= pdf.getPageCount(); i++){
                pages.getItems().add(String.valueOf(i));
            }
            // Goes to the first page and starts the decoding process
            goToPage(currentPage);

        } catch (final PdfException ex) {
            ex.printStackTrace();

        }

    }
 
    /**
     * This method will show a popup box and request for a password.
     * 
     * If the user does not enter the correct password it will ask them to try again.
     * If the user presses the Cross button, the password prompt will close.
     * 
     * @param passwordCount is an int which represents the current input attempt
     */
    private void showPasswordPrompt(final int passwordCount){
        
        //Setup password prompt content
        final Text titleText = new Text("Password Request");
        final TextField inputPasswordField = new TextField("Please Enter Password");
        
        //If the user has attempted to enter the password more than once, change the text
        if(passwordCount >= 1){
            titleText.setText("Incorrect Password");
            inputPasswordField.setText("Please Try Again");
        }
        
        final FXInputDialog passwordInput = new FXInputDialog(stage, titleText.getText()){
            @Override
            protected void positiveClose(){
                super.positiveClose();
                closePasswordPrompt = true;
            }
        };
        
        password = passwordInput.showInputDialog();
        
    }
    
    private void fitToX(final FitToPage fitToPage) {
        
        if(fitToPage == FitToPage.NONE) {
            return;
        }
        
        final float pageW=pdf.getPdfPageData().getCropBoxWidth2D(currentPage);
        final float pageH=pdf.getPdfPageData().getCropBoxHeight2D(currentPage);
        final int rotation = pdf.getPdfPageData().getRotation(currentPage);
        
        //Handle how we auto fit the content to the page
        if(fitToPage == FitToPage.AUTO && (pageW < pageH)){
                if(pdf.getPDFWidth()<pdf.getPDFHeight()) {
                    fitToX(FitToPage.HEIGHT);
                }
                else {
                    fitToX(FitToPage.WIDTH);
                }
        }        
            
        //Handle how we fit the content to the page width or height
        if(fitToPage == FitToPage.WIDTH){
            final float width=(float) (scene.getWidth());
            if(rotation==90 || rotation==270){
                scale = (width - insetX - insetX) / pageH;
            }else{
                scale = (width - insetX - insetX) / pageW;
            }  
        }else if(fitToPage == FitToPage.HEIGHT){
            final float height=(float) (scene.getHeight()-top.getBoundsInLocal().getHeight()-bottom.getHeight());

            if(rotation==90 || rotation==270){
                scale = (height - insetY - insetY) / pageW;
            }else{
                scale = (height - insetY - insetY) / pageH;
            }
        }
        
        pdf.setPageParameters(scale, currentPage);
    }
    
    /**
     * Locate scaling value closest to current scaling setting
     * @param scale
     * @param scalings
     * @return int
     */
    private static int findClosestIndex(final float scale, final float[] scalings) {
        float currentMinDiff = Float.MAX_VALUE;
        int closest = 0;
        
        for(int i = 0; i < scalings.length - 1; i++) {
            
            final float diff = Math.abs(scalings[i] - scale);
            
            if(diff < currentMinDiff) {
                currentMinDiff = diff;
                closest = i;
            }
            
        }
        return closest;
    }

    private void decodePage() {

        try {
            final PdfPageData pageData = pdf.getPdfPageData();
            final int rotation = pageData.getRotation(currentPage); //rotation angle of current page

            //Only call this when the page is displayed vertically, otherwise
            //it will mess up the document cropping on side-ways documents.
            if (rotation == 0 || rotation == 180) {
                pdf.setPageParameters(scale, currentPage);
            }

            pdf.decodePage(currentPage);
            //wait to ensure decoded
            pdf.waitForDecodingToFinish();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        fitToX(FitToPage.AUTO);
        updateNavButtons();
        setBorder();
        adjustPagePosition(center.getViewportBounds());
    }
    
    private void updateNavButtons(){
        if(currentPage > 1){
            top.lookup("#back").setDisable(false);
        }else{
            top.lookup("#back").setDisable(true);
        }
        
        if(currentPage < pdf.getPageCount()){
            top.lookup("#forward").setDisable(false);
        }else{
            top.lookup("#forward").setDisable(true);
        }
        
        ((ComboBox)top.lookup("#pages")).getSelectionModel().select(currentPage - 1);
    }
    
    private void goToPage(final int newPage){
        
        final TransitionDirection direction ;
        
        // For sliding Transitions
        if(transitionType != TransitionType.Fade || transitionType != TransitionType.None){
            direction = newPage > currentPage ? TransitionDirection.LEFT: TransitionDirection.RIGHT;
        }else{
            direction = TransitionDirection.NONE;
        }
        
        switch (transitionType){
            
            case Fade:
                startTransition(  newPage, direction);
                break;
                
            case Scale:
                startTransition(  newPage, direction);
                break;
            
            case Rotate:
                startTransition(  newPage, direction);
                break;
                
            case CardStack:
                startTransition(  newPage, direction);
                break;
                
            default: //no transition
                
                currentPage = newPage;
                decodePage();
                break;
        }
        
        }
        
        private void startTransition(final int newPage,final TransitionDirection direction){
            final Transition exitTransition = FXViewerTransitions.exitTransition(pdf, transitionType, direction);
            if(exitTransition != null){
                exitTransition.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override public void handle(final ActionEvent t) {
                        
                        currentPage = newPage;

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                decodePage();
                            }
                        });
                                    
                        TransitionDirection entryDirection = direction;
                        if(direction != TransitionDirection.NONE){
                            entryDirection = direction == TransitionDirection.LEFT ? TransitionDirection.RIGHT : TransitionDirection.LEFT;
                        }
                         
                        final Transition entryTransition = FXViewerTransitions.entryTransition(pdf, transitionType, entryDirection);
                        entryTransition.play();
                    }
                });
                exitTransition.play();
            }
        }
     
        /**
         * @return the case sensitive full path and name of the PDF file   
         */
    public String getPDFfilename(){
        return PDFfile;
    }
    
    private void adjustPagePosition(final Bounds nb){
        // (new scrollbar width / 2) - (page width / 2)
        double adjustment = ((nb.getWidth() / 2) - (group.getBoundsInLocal().getWidth() /2));
        // Keep the group within the viewport of the scrollpane
        if(adjustment < 0) {
            adjustment = 0;
        }
        group.setTranslateX(adjustment);
    }
    
    // Set a space between the top toolbar and the page
    private void setBorder() {
        // Why it's easier to use a dropshadow for this is beyond me, but here it is...
        final int rotation = pdf.getPdfPageData().getRotation(currentPage);
        final double x = (rotation == 90 || rotation == 270) ? 40 : 0;
        final double y = (rotation == 90 || rotation == 270) ? 0 : 40;
        final DropShadow pdfBorder = new DropShadow(0, x,y, Color.TRANSPARENT);
        pdf.setEffect(pdfBorder); 
    }
    
}
