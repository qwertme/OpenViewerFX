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
 * JavaFXRSSyndication.java
 * ---------------
 */

package org.jpedal.examples.viewer.commands.javafx;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;


import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.utils.BrowserLauncher;

/**
 * When the RSS button on the viewer is called it will load   
 * the stage with the link for the RSS Feed to join.
 */
public class JavaFXRSSyndication {
    public static void execute(final Object[] args) {
        if (args == null) {
            getRSSBox();
        } else {

        }
    }
    
    public static void getRSSBox() {
        
        final BorderPane border = new BorderPane();
        
        final VBox top = new VBox();
        top.setPadding(new Insets(20, 0, 20, 20));
        final Label topLabel = new Label("Click the link below to load a web browser and sign up to our RSS Feed");
        final Hyperlink link = new Hyperlink("Sign Up to RSS");
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                try {
                    BrowserLauncher.openURL("http://jpedal.org/jpedal.rss");
                } catch (final Exception ex) {
                    Logger.getLogger(JavaFXRSSyndication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        VBox.setVgrow(topLabel, Priority.ALWAYS);
        link.setTextAlignment(TextAlignment.CENTER);
        top.setAlignment(Pos.BOTTOM_CENTER);
        top.getChildren().addAll(topLabel, link);
        border.setTop(top);

        final HBox middle = new HBox();
        final int depth = 50;
        final DropShadow borderGlow = new DropShadow();
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(javafx.scene.paint.Color.BLACK);
        borderGlow.setWidth(depth);
        borderGlow.setHeight(depth);
        final Image image = new Image("/org/jpedal/examples/viewer/res/rss.png");
        final ImageView mainImage = new ImageView();
        mainImage.setImage(image);
        middle.setAlignment(Pos.CENTER);
        middle.getChildren().addAll(mainImage);
        middle.setEffect(borderGlow);
        border.setCenter(middle);
        
        final HBox bottom = new HBox();
        final Button ok = new Button("OK");
        bottom.getChildren().add(ok);
        ok.setPrefWidth(60d);
        border.setBottom(bottom);
        bottom.setAlignment(Pos.BASELINE_RIGHT);
        bottom.setPadding(new Insets(20, 20, 20, 0));
        
        final FXDialog newDialog = new FXDialog(null, Modality.APPLICATION_MODAL, border, 550, 600);
        
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                newDialog.close();
            }
        });
        
        newDialog.show();
        
    }
}
