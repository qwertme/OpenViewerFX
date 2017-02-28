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
 * JavaFXExtractSelectionAsImage.java
 * ---------------
 */

package org.jpedal.examples.viewer.commands.javafx;

import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.handlers.DefaultImageHelper;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.generic.GUIExtractSelectionAsImage;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.LogWriter;

/**
 * This class is a JavaFX specific class to hold the JavaFX code for
 * Extracting the drawn CursorBox as an Image.
 */
public class JavaFXExtractSelectionAsImage extends GUIExtractSelectionAsImage {
    
    protected static final int BUTTONWIDTH = 55;
    
    public static void execute(final Values commonValues, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf) {
        extractSelectedScreenAsImage(commonValues,currentGUI,decode_pdf); //Calls the generic code.

        
        final VBox pane = new VBox();
        final FXDialog dialog = new FXDialog((Stage)currentGUI.getFrame(), Modality.APPLICATION_MODAL, pane);
        dialog.setResizeable(false);
        //wrap image so we can display
        if( snapShot != null ){
            //IconiseImage icon_image = new IconiseImage( snapShot );
            final ImageView imv1 = new ImageView();
            imv1.setImage(SwingFXUtils.toFXImage(snapShot, null));
            dialog.setWidth(imv1.getImage().getWidth());
            dialog.setHeight(imv1.getImage().getHeight()+50);
            //add image to pane if there is one
            pane.getChildren().add(imv1);
        }else{
            return;
        }
        
        final HBox btnBox = new HBox();
        final Button saveBtn = new Button("Save");
        final Button cancelBtn = new Button("Cancel");
        btnBox.getChildren().addAll(saveBtn, cancelBtn);
        btnBox.setAlignment(Pos.BOTTOM_RIGHT);
        pane.getChildren().add(btnBox);
        
        saveBtn.setPrefWidth(BUTTONWIDTH);
        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent t) {

                dialog.getDialog().hide();

                final FileChooser chooser = new FileChooser();
                chooser.setTitle("Open PDF file");
                chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                final FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("TIFF (*.tif)", "*.tif", "*.tiff");
                chooser.getExtensionFilters().add(extFilter1);
                final FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("JPEG (*.jpg)", "*.jpg", "*.jpeg");
                chooser.getExtensionFilters().add(extFilter2);

                File outputFile = chooser.showSaveDialog(dialog.getDialog());
                
                    if (outputFile != null) {
                        StringBuilder outName = new StringBuilder(outputFile.getAbsolutePath());

                        FileChooser.ExtensionFilter filter = chooser.getSelectedExtensionFilter();

                        String format = "tif";
                        if (filter.getDescription().toLowerCase().contains("jp")) {
                            format = "jpg";
                        }

                        if (!outName.toString().toLowerCase().endsWith(('.' + format).toLowerCase())) {
                            outName.append('.').append(format);
                        }

                        //Do the actual save
                        if (snapShot != null) {

                            try {
                                DefaultImageHelper.write(snapShot, format, outName.toString());
                            } catch (IOException ex) {
                                if (LogWriter.isOutput()) {
                                    LogWriter.writeLog("Exception in writing image " + ex);
                                }
                            }
                        }
                    }
            }
        });
        
        cancelBtn.setPrefWidth(BUTTONWIDTH);
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent t) {
                dialog.close();
            }
        });
        
        dialog.show();
        
        if(GUI.debugFX){
            System.out.println("Save Dialog required for JavaFXExtractSelectionAsImage.java");
        }
    }
}
