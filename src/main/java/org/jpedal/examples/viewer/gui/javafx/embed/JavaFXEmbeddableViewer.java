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
 * JavaFXEmbeddableViewer.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx.embed;

import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.jpedal.examples.viewer.OpenViewerFX;

/**
 * A wrapper class for OpenViewerFX which allows the Viewer to be 
 * recognised as an embeddable element in JavaFX Scene Builder (2.0)
 * 
 * @author Simon
 */
public class JavaFXEmbeddableViewer extends Pane {
    
    private OpenViewerFX viewer;
    
    JavaFXEmbeddableViewer(){
        viewer = null;
        
        final Pane thisPane = this;
        
        // Running later as OpenViewerFX was creating some issue (Class load related, I believe)
        // Which caused this class to not be picked up by SceneBuilder
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try{
                    viewer = new OpenViewerFX(thisPane, null);
                    viewer.setupViewer();
                    
                    final BorderPane root = viewer.getRoot();
                    
                    // Bind the viewer to match the dimensions on this class
                    root.prefWidthProperty().bind(thisPane.prefWidthProperty());
                    root.prefHeightProperty().bind(thisPane.prefHeightProperty());
                }catch (final Exception e){
                    System.err.println(e.getMessage());
                }
            }
        });
        
    }
    
    public OpenViewerFX getOpenViewerFX(){
        if(viewer==null){
            System.err.println("OpenViewerFX has not been initialised yet - Please run in Platform.runLater()");
        }
        return viewer;
    }
}
