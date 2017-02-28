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
 * OpenViewerFX.java
 * ---------------
 */
package org.jpedal.examples.viewer;

import org.jpedal.PdfDecoderFX;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jpedal.examples.viewer.gui.*;
import org.jpedal.examples.viewer.gui.javafx.*;
import org.jpedal.examples.viewer.utils.*;
import org.jpedal.examples.viewer.objects.ClientExternalHandler;
import org.jpedal.external.Options;
import org.jpedal.objects.acroforms.actions.JavaFXDefaultActionHandler;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/** <h2><b>PDF Viewer</b></h2>
 *
 * <p>If you are compiling, you will need to download all the examples source files from :
 * <a href="http://www.idrsolutions.com/how-to-view-pdf-files-in-java/">How to View PDF File in Java.</a></p>
 * 
 * <p><b>Run directly from jar with java -cp jpedal.jar org.jpedal.examples.viewer.FXStartup (needs Java8)</b></p>
 *
 * <p>There are plenty of tutorials on how to configure the Viewer on our website <a href="http://www.idrsolutions.com/java-pdf-library-support/">Support Section.</a></p>
 * 
 * <p><a href="http://javadoc.idrsolutions.com/org/jpedal/constants/JPedalSettings.html">See Here for Settings to Customise the PDF Viewer</a></p>
 *
 * <p>We also have our Swing Viewer documented at :</p>
 * <a href="http://files.idrsolutions.com/samplecode/org/jpedal/examples/viewer/Viewer.java.html">Swing Viewer Documentation.</a>
 * 
 * <p>If you want to implement your own there is
 * a very simple example at : 
 * <a href="http://files.idrsolutions.com/samplecode/org/jpedal/examples/baseviewer/BaseOpenViewerFX.java.html">BaseOpenViewerFX Demo</a>
 * <p>We recommend you look at the full viewer as it is totally configurable and does everything for you.</p>
 * 
 */
public class OpenViewerFX extends Viewer implements ViewerInt{

    static {
        checkUserJavaVersion();
        isFX = true;
    }

    private Stage stage;
    private String[] args;

    /**
     * Base Constructor to use when starting Viewer as a stand alone Stage.
     * Please ensure you call setupViewer after calling this constructor.
     * 
     * @param stage Is of type Stage
     * @param args Program arguments passed into the Viewer
     */
    public OpenViewerFX(final Stage stage, final String[] args) {
        this.stage = stage;
        this.args = args;
        init();
    }
    
    /**
     * Constructor to use when embedding the JavaFX Viewer in your own
     * JavaFX application, pass in your parentPane and the Viewer will
     * become a child of the parentPane.
     * 
     * Please ensure you call setupViewer after calling this constructor.
     * 
     * To add the JavaFX Viewer to a Tab, please pass a 
     * Pane or ScrollPane and add the passed Pane to the Tab.
     * 
     * @param parentPane Is a constructor of your own JavaFX application in which JavaFx Viewer will be embedded
     * @param preferencesPath The path of the preferences file
     */
    public OpenViewerFX(final Parent parentPane, final String preferencesPath) {

        //Enable error messages which are OFF by default
        DecoderOptions.showErrorMessages = true;

        if (preferencesPath != null && !preferencesPath.isEmpty()) {
            try {
                properties.loadProperties(preferencesPath);
            } catch (final Exception e) {
                System.err.println("Specified Preferrences file not found at " + preferencesPath + ". If this file is within a jar ensure filename has jar: at the begining.\n\nLoading default properties. "+e);

                properties.loadProperties();
            }
        } else {
            properties.loadProperties();
        }
        
        init();

        setRootContainer(parentPane);
    }

    /**
     * Sets up the Viewer and Displays it.
     * 
     * Please make sure you call this method
     * after calling any constructor to initialise
     * key components.
     */
    @Override
    public void setupViewer() {
        
        super.setupViewer();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                handleArguments(args);
                if (stage != null) {
                    // We need to manually call dispose so that the AWT/Swing threads are killed off properly and the JVM can shut down cleanly
                    // see http://docs.oracle.com/javase/1.5.0/docs/api/java/awt/doc-files/AWTThreadIssues.html
                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(final WindowEvent t) {
                            currentCommands.executeCommand(Commands.EXIT, null);
                        }
                    });
                }
            }
        });
    }
    
    @Override
    void init() {
        
        //load locale file
        try {
            Messages.setBundle(ResourceBundle.getBundle("org.jpedal.international.messages"));
        } catch (final Exception e) {
            //
            LogWriter.writeLog("Exception " + e + " loading resource bundle.\n"
                    + "Also check you have a file in org.jpedal.international.messages to support Locale=" + java.util.Locale.getDefault());
        }
        
        //

        decode_pdf = new PdfDecoderFX();

        thumbnails = new JavaFXThumbnailPanel(decode_pdf);

        currentGUI = new JavaFxGUI(stage, decode_pdf, commonValues, thumbnails, properties);
        
        decode_pdf.addExternalHandler(new JavaFXDefaultActionHandler(currentGUI), Options.FormsActionHandler);
        decode_pdf.addExternalHandler(new ClientExternalHandler(), Options.AdditionalHandler);
        
        if(GUI.debugFX) {
            System.out.println("OpenViewerFX init()");
        }

        searchFrame = new JavaFXSearchWindow(currentGUI);

        currentCommands = new JavaFXCommands(commonValues, currentGUI, decode_pdf,
                thumbnails, properties, searchFrame, currentPrinter);
        
                
		//enable error messages which are OFF by default
		DecoderOptions.showErrorMessages=true;
		
		//
		
		
		final String prefFile = System.getProperty("org.jpedal.Viewer.Prefs");
		if(prefFile != null){
			properties.loadProperties(prefFile);
		}else{
			properties.loadProperties();
		}
    }
    
    /**
     * Allows the viewer to handle any JVM/Program arguments.
     *
     * @param args :: Program arguments passed into the Viewer.
     */
    @Override
    public void handleArguments(final String[] args) {

        //Ensure default open is on event thread, otherwise the display is updated as values are changing
        if (Platform.isFxApplicationThread()) {
            if (args !=null && args.length > 0) {
                openDefaultFile(args[0]);

            } else if ((properties.getValue("openLastDocument").toLowerCase().equals("true")) &&
                 (properties.getRecentDocuments() != null
                        && properties.getRecentDocuments().length > 1)) {

                    int lastPageViewed = Integer.parseInt(properties.getValue("lastDocumentPage"));

                    if (lastPageViewed < 0) {
                        lastPageViewed = 1;
                    }

                    openDefaultFileAtPage(properties.getRecentDocuments()[0], lastPageViewed);
                }
            
        } else {

            final Runnable run = new Runnable() {

                @Override
                public void run() {
                    if (args.length > 0) {
                        openDefaultFile(args[0]);

                    } else if ((properties.getValue("openLastDocument").toLowerCase().equals("true")) &&
                         (properties.getRecentDocuments() != null
                                && properties.getRecentDocuments().length > 1)){

                            int lastPageViewed = Integer.parseInt(properties.getValue("lastDocumentPage"));

                            if (lastPageViewed < 0) {
                                lastPageViewed = 1;
                            }

                            openDefaultFileAtPage(properties.getRecentDocuments()[0], lastPageViewed);
                        }
                    }
            };
            Platform.runLater(run);
        }
    }

    /**
     * Releases all resources used by the viewer - <em>Please note this calls System.exit(0);</em>
     * This method has to be called when you explicitly want 
     * to close the viewer.
     */
    public void close(){
        Viewer.closeCalled = true;
        currentCommands.executeCommand(Commands.EXIT, null);
    }
    
    /**
     * Deprecate on 15/07/2014 
     * To add Viewer to your own parent node, please use:
     * OpenViewerFX(Parent parentPane, String preferencesPath).
     * @deprecated
     * @param args Program arguments passed into the Viewer
     */
    public OpenViewerFX(final String[] args) {
        this(null,args);
    }
    
    /**
     * 
     * Not part of the API.
     * 
     * To add Viewer to your own parent node, please use:
     * OpenViewerFX(Parent parentPane, String preferencesPath).
     * 
     * @return The root of your JavaFX application
     */
    public BorderPane getRoot(){
        return ((JavaFxGUI)currentGUI).getRoot();
    }
    
    protected static void checkUserJavaVersion(){ 
        if (Float.parseFloat(System.getProperty("java.specification.version")) < 1.8f) {
            throw new RuntimeException("To run the JPedal FX Viewer, you must have Java8 or above installed");
         }
    }
}
