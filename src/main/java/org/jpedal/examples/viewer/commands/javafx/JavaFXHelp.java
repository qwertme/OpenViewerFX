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
 * JavaFXHelp.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.utils.BrowserLauncher;

/**
 * This class will load a Pop-Up which points the user to our Support and
 * Documentation section on our website.
 */
public class JavaFXHelp {

    public static void execute(final Object[] args) {
        if (args == null) {
            getHelpBox();
        }

    }

    /**
     * Shows a popup window which displays information for support.
     *
     * @param currentGUI
     */
    private static void getHelpBox() {

        final Text info = new Text("Please click the link below for lots of tutorials and documentation");
        info.setTextAlignment(TextAlignment.CENTER);
        info.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
        final Hyperlink link = new Hyperlink("http://idrsolutions.com/java-pdf-library-support/");
        link.setStyle("-fx-text-fill: blue;");
        final Button OK = new Button("OK");
        OK.setPadding(new Insets(10, 50, 10, 50));
        //Seperates text from hyperlink button
        final Separator topSep = new Separator();
        topSep.setPrefHeight(25);
        topSep.setOrientation(Orientation.HORIZONTAL);
        topSep.setVisible(false);
        final VBox vBox = new VBox();
        
        final Separator btnSep = new Separator();
        btnSep.setPrefHeight(25);
        btnSep.setOrientation(Orientation.HORIZONTAL);
        btnSep.setVisible(false);
        
        vBox.getChildren().addAll(info, topSep, link,btnSep,OK); //add items to vBox container
        vBox.setAlignment(Pos.CENTER);

        final FXDialog newDialog = new FXDialog(null, Modality.APPLICATION_MODAL, vBox, 400, 200);
        newDialog.setTitle("JPedal Tutorials and documentation");
        //Open default-browser window to support & docs page when link is clicked.
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                try {
                    BrowserLauncher.openURL("http://idrsolutions.com/java-pdf-library-support/");
                    newDialog.close();
                } catch (final Exception ex) {
                    Logger.getLogger(JavaFXRSSyndication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
         OK.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                try {
                    newDialog.close();
                } catch (final Exception ex) {
                    Logger.getLogger(JavaFXRSSyndication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        newDialog.show();

    }

}
