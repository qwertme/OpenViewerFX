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
 * JavaFXInfo.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.Messages;

/**
 * This class displays a popup window which gives user information about JPedal.
 * It can be called by going to Help then About on the main menu-bar.
 */
public class JavaFXInfo {
    
    public static void execute(final Object[] args) {
        if (args == null) {
            getInfoBox();
        } else {

        }
    }
    
    private static void getInfoBox(){
        
        /**
         * Build Main Body Title.
         */
        final Text title = new Text ("JavaFX Viewer Information");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
         
        /**
         * Build Main Body Text.
         */
   
        //
        Text info = new Text("OpenViewerFX is a JavaFX PDF Viewer written in JavaFX and released without any warranty or support under an LGPL license.\n"
            + "This Application is updated regularly and a supported, enhanced version is available as part of the commercial JPedal Java PDF library.");
        info.setText(info.getText()+"\n\n\t\tVersions: " + PdfDecoderInt.version + "          " + "Java: " + System.getProperty("java.version"));
        /**/    
        //
        
        info.setWrappingWidth(350);
        info.setTextAlignment(TextAlignment.JUSTIFY);
        info.setFont(Font.font("SansSerif", FontWeight.NORMAL,12));
        
        /**
         * Build Main Body Logo.
         */
        
        //
        ImageView imageView = new ImageView(new Image("/org/jpedal/examples/viewer/res/logo2.png"));
        /**/    
        //
        
      
        /**
         * Build Main Body HyperLink.
         */
        
        //    
        Hyperlink link = new Hyperlink("Learn more about the JPedal Commercial PDF Library");
        /**/    
        // 
        link.setBorder(Border.EMPTY);
        
        /**
         * Build Main Body ok Button.
         */
        final Button okButton = new Button("OK");
        okButton.setPadding(new Insets(7,25,7,25));
        
        /**
         * Build Text Seperators.
         */
        final Separator sepBottom = new Separator();
        sepBottom.setPrefHeight(50);
        sepBottom.setVisible(false);
        sepBottom.setOrientation(Orientation.HORIZONTAL);
        final Separator sepTop = new Separator();
        sepTop.setPrefHeight(50);
        sepTop.setVisible(false);
        sepTop.setOrientation(Orientation.HORIZONTAL);
        final Separator sepAfterImg = new Separator();
        sepAfterImg.setPrefHeight(50);
        sepAfterImg.setVisible(false);
        sepAfterImg.setOrientation(Orientation.HORIZONTAL);
        final Separator sepAfterLink = new Separator();
        sepAfterLink.setPrefHeight(50);
        sepAfterLink.setVisible(false);
        sepAfterLink.setOrientation(Orientation.HORIZONTAL);

        /**
         * Add Items to a VBox Container.
         */
        final VBox vBox = new VBox();
        vBox.getChildren().addAll(title,sepTop,info,sepBottom,imageView,sepAfterImg,link,sepAfterLink,okButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10));
        
        final FXDialog newDialog = new FXDialog(null, Modality.APPLICATION_MODAL, vBox, 400,350);
        
        //   
            newDialog.setTitle("About OpenViewerFX");
        /**/    
        //
        
        newDialog.setResizeable(false);
      
        //Open default-browser window to support & docs page when link is clicked.
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                try {
                    
                    BrowserLauncher.openURL("http://www.idrsolutions.com/java-pdf-library/");
                    newDialog.close();
                } catch (final Exception ex) {
                   ex.printStackTrace();
                }
            }
        });
        
         okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                newDialog.close();
            }
        });
        
        newDialog.show();
    }
    
}
