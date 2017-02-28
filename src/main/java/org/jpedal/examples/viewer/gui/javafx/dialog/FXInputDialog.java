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
 * FXInputDialog.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx.dialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog window which prompts the user for input.
 * 
 * @author Simon
 */
public class FXInputDialog extends FXMessageDialog {
    private final StringProperty input;
    
    public FXInputDialog(final Stage parent, final String message) {
        super(parent, Modality.APPLICATION_MODAL, message);
        
        input = new SimpleStringProperty();
        final TextField textField = new TextField();
        final BorderPane contentPane = getBorderPane();
        final Button cancelButton = new Button("Cancel");
        
        input.bind(textField.textProperty());
        textField.textProperty().set("");
        
        contentPane.getCenter().boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
            @Override public void changed(final ObservableValue<? extends Bounds> ov, final Bounds oldBounds, final Bounds newBounds) {
                textField.setPrefWidth(newBounds.getWidth());
            }
        });
        
        cancelButton.setPrefWidth(BUTTONWIDTH);
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent t) {
                cancel();
            }
        });
        
        getCenterGroup().getChildren().add(textField);
        getButtonGroup().getChildren().add(cancelButton);
        
    }
    
    /**
     * Use to get the value from the input box. Returns null if the user cancels the dialog.
     * @return the value from the input box
     */
    public String showInputDialog(){
        showAndWait();
        
        if(isCancelled) {
            return null;
        } else {
            return input.get();
        }
    }
    
}
