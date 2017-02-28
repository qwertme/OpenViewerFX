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
 * FXMessageDialog.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx.dialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * An extension of FXDialog which displays a message to the user.
 * You can set an image as the warning image and the message text.
 * 
 * @author Simon
 */
public class FXMessageDialog extends FXDialog {
    /**
     * Icon references, replace JOptionPane equivalents
     */
    public static final int ERROR_MESSAGE = 0; // NYI
    public static final int INFORMATION_MESSAGE = 1; //NYI
    public static final int WARNING_MESSAGE = 2; // NYI
    public static final int QUESTION_MESSAGE = 3; // NYI
    public static final int PLAIN_MESSAGE = -1; // NYI
    
    protected static final int FONTSIZE = 14;
    protected static final int BUTTONWIDTH = 55;
    
    private final BorderPane content;
    private final StringProperty message;
    private final ImageView iconView;
    private final HBox bottom;
    private final VBox center;
    
    public FXMessageDialog(final Stage parent, final Modality modality) {
        content = new BorderPane();
        iconView = new ImageView();
        message = new SimpleStringProperty();
        final Button okButton = new Button("OK");
        
        okButton.setPrefWidth(BUTTONWIDTH);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent t) {
                positiveClose();
            }
        });
        
        init(parent, Modality.APPLICATION_MODAL, content);
        
        dialog.setResizable(false);
        
        // Setup centre
        center = new VBox();
        final Label label = new Label();
        label.setWrapText(true);
        label.textProperty().bind(message);
        label.setFont(Font.font(FONTSIZE));
        center.getChildren().add(label);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20));
        
        // Setup left pane
        final VBox left = new VBox();
        left.setAlignment(Pos.CENTER);
        iconView.setPreserveRatio(true);
        iconView.setFitWidth(0);
        left.getChildren().add(iconView);
        
        // Setup bottom pane
        bottom = new HBox(5);
        bottom.setAlignment(Pos.CENTER);
        bottom.getChildren().add(okButton);
        bottom.setPadding(new Insets(5,5,10,5));
        
        content.setCenter(center);
        content.setLeft(left);
        content.setBottom(bottom);
        // Set up scene
        setScene(new Scene(content));
        dialog.sizeToScene();
    }

    public FXMessageDialog(final Stage parent, final Modality modality, final String message) {
        this(parent, modality);
        this.message.set(message);
    }
    
    public FXMessageDialog(final Stage parent, final Modality modality, final Image icon) {
        this(parent, modality);
        setImage(icon);
    }
    
    public FXMessageDialog(final Stage parent, final Modality modality, final Image icon, final String message) {
        this(parent, modality);
        setImage(icon);
        this.message.set(message);
    }
    
    /**
     * MessageDialog with custom center node
     * 
     * @param parent The stage of Dialog
     * @param modality The mode of the Dialog
     * @param group The group of the Dialog
     */
    public FXMessageDialog(final Stage parent, final Modality modality, final Parent group){
        this(parent, modality);
        content.setCenter(group);
    }
    
    public final void setImage(final Image icon){
        if(icon == null){
            iconView.setFitWidth(0);
            return;
        }
        iconView.setFitWidth(icon.getWidth());
        iconView.setImage(icon);
    }
    
    protected BorderPane getBorderPane(){
        return content;
    }
    
    public void setCenterAlignment(final Pos pos){
        ((VBox)content.getCenter()).setAlignment(pos);
    }
    
    protected HBox getButtonGroup(){
        return bottom;
    }
    
    protected VBox getCenterGroup(){
        return center;
    }
    
    protected StringProperty getMessage(){
        return message;
    }
    
    @Override
    protected void positiveClose(){
        isCancelled = false;
        dialog.close();
    }
}
