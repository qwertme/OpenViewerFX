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
 * JavaFXTipOfTheDay.java
 * ---------------
 */

package org.jpedal.examples.viewer.commands.javafx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;

/**
 * Class Which Displays the Tip of the Day
 * Can be toggle to display on Viewer Startup.
 */
public class JavaFXTipOfTheDay {
    
    private static FXDialog tipOfDayPopup;
    private static final List urls = new ArrayList();
    private static final BorderPane border = new BorderPane();
    private static  WebEngine webEngine;
    private static int currentTip;
    private static final CheckBox show = new CheckBox("Show Tips On Start Up");
    
    public static void execute(final Object[] args, final GUIFactory currentGUI, final PropertiesFile properties) {
        if (args == null) {
            try {
                populateTipsList("/org/jpedal/examples/viewer/res/tips");
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
            getTip( properties);
            
        } else {

        }
    }
    
    private static void getTip (final PropertiesFile properties){
        setupStage();
        bottomButtons();
        
        /**
         * Code to Handle Auto Startup.
         */
		final String propValue = properties.getValue("displaytipsonstartup");
		if(!propValue.isEmpty()) {
            show.setSelected(propValue.equals("true"));
        }
        
		show.setOnAction(new EventHandler<ActionEvent>(){
			@Override
            public void handle(final ActionEvent e) {
				properties.setValue("displaytipsonstartup", String.valueOf(show.isSelected()));
			}
		});
        
        tipOfDayPopup.show();
    }
   
    private static void setupStage(){
        
        /**
         * Setup the Main Stage.
         */
        tipOfDayPopup = new FXDialog(null, Modality.APPLICATION_MODAL, border, 500, 400);
        tipOfDayPopup.setTitle("Tip Of The Day");
        
        /**
         * Setup Did You Know Top Element.
         */
        final HBox titleBar = new HBox();
        final Label title = new Label("Did you know...?", new ImageView(new Image("/org/jpedal/examples/viewer/res/tip.png")));
        titleBar.getChildren().addAll(title);
        titleBar.setPadding(new Insets(10,0,20,10));
        border.setTop(titleBar);
        
        /**
         * Setup the Default WebView.
         */
        final VBox middle = new VBox();
        final Random r = new Random();
		currentTip = r.nextInt(urls.size());
        final WebView centerTip = new WebView();
        webEngine = centerTip.getEngine();
        final URL tip = JavaFXTipOfTheDay.class.getResource("/org/jpedal/examples/viewer/res/tips/apps/javabean.html");
        webEngine.load(tip.toExternalForm());  
        
        middle.getChildren().addAll(centerTip);
        middle.setAlignment(Pos.CENTER);
        middle.setPadding(new Insets(0,10,20,10));
        border.setCenter(middle);
        
    }
    
    /**
     * Sets up the Bottom Buttons.
     */
    private static void bottomButtons (){
        
        /**
         * Setup the Next Button.
         */
        final HBox bottomButtons = new HBox();
        final Button nextTip = new Button ("Next Tip");
        nextTip.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(final javafx.event.ActionEvent e) {
                if(currentTip < urls.size() -1){
                    currentTip++;
                }
                else{
                    currentTip = 0;
                }
                final URL urlNext = JavaFXTipOfTheDay.class.getResource(urls.get(currentTip).toString());
                webEngine.load(urlNext.toExternalForm());
                
            }
        });
        
        /**
         * Setup the Previous Button.
         */
        final Button prevTip = new Button ("Previous Tip");
        prevTip.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(final javafx.event.ActionEvent e) {
                if(currentTip < urls.size() && currentTip > 0){
                    currentTip--;
                }
                else{
                    currentTip = urls.size() -1;
                }
                final URL urlPrev = JavaFXTipOfTheDay.class.getResource(urls.get(currentTip).toString());
                webEngine.load(urlPrev.toExternalForm());
                
            }
        });
        show.setAlignment(Pos.BOTTOM_LEFT);
        show.setPadding(new Insets(0,0,10,10));
        final Region space =  new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        bottomButtons.getChildren().addAll(show, space, prevTip, nextTip);
        bottomButtons.setAlignment(Pos.BOTTOM_RIGHT);
        bottomButtons.setPadding(new Insets(0,10,10,0));
        bottomButtons.setSpacing(10d);
        border.setBottom(bottomButtons);
    }
    
    /**
     * Creates an Array List which holds all of the Tips located in the tips directory.
     * @param tipRoot
     * @throws IOException 
     */
    private static void populateTipsList(final String tipRoot) throws IOException {
		try {
			final URL url = JavaFXTipOfTheDay.class.getResource(tipRoot); //"/org/jpedal/examples/viewer/res/tips"
			
			/**
			 * allow for it in jar
			 */
			if(url.toString().startsWith("jar")){
				final JarURLConnection conn = (JarURLConnection) url.openConnection();
				final JarFile jar = conn.getJarFile();
	
				for (final Enumeration e = jar.entries(); e.hasMoreElements();) {
					final JarEntry entry = (JarEntry) e.nextElement();
					final String name=entry.getName();
					
					if ((!entry.isDirectory()) && name.contains("/res/tips/") && name.endsWith(".html")) { // this
						urls.add('/' + name);
					}
				}
			}else{ //IDE
				final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				
				String inputLine;
				
				while ((inputLine = in.readLine()) != null) {
					if (inputLine.indexOf('.') == -1) { // this is a directory
						populateTipsList(tipRoot + '/' + inputLine);
					} else if ((inputLine.endsWith(".htm")) || inputLine.endsWith(".html")) { // this is a file
						urls.add(tipRoot + '/' + inputLine);
					}
				}
			
			
				in.close();
			}
		} catch (final IOException e) {
			//<end-demo><end-full>
			throw e;
		}
	}
}
