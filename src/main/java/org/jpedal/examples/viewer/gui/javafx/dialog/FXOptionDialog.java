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
 * FXOptionDialog.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx.dialog;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Option dialog allows for a user to specify a choice.
 * 
 * *_OPTION allows for a predefined option set to be used. 
 * This will be overriden by passing the options array in. 
 * The options array allows you to define your own input options.
 * 
 * The return value from showOptionsDialog() will go from 0 at the left to the maximum button count -1
 * 
 * @author Simon
 */
public class FXOptionDialog extends FXMessageDialog {
    public static final int DEFAULT_OPTION = -1;
    public static final int YES_NO_OPTION = 0;
    public static final int YES_NO_CANCEL_OPTION = 1;
    public static final int OK_CANCEL_OPTION = 2;
    
    public static final int CLOSED_OPTION = -1;
    public static final int YES_OPTION = 0;
    public static final int OK_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    
    private int choice = -1;
    
    public FXOptionDialog(final Stage parent, final String message, final String title, final int optionType, final Object[] options, final Object initialValue) {
        super(parent, Modality.APPLICATION_MODAL, message);
        
        setTitle(title);
        setupButtons(optionType, options, initialValue);
        
    }
    
    public FXOptionDialog(final Stage parent, final Object message, final String title, final int optionType, final int messageType, final Object[] options, final Object initialValue) {
        super(parent, Modality.APPLICATION_MODAL, message.toString());
        
        setTitle(title);
        setupButtons(optionType, options, initialValue);
        
    }
    
    private void setupButtons(final int optionType, final Object[] options, final Object initialValue){
        final List<Button> buttons = new ArrayList<Button>();

        if(options == null){
            if(optionType < DEFAULT_OPTION || optionType > OK_CANCEL_OPTION){
                throw new RuntimeException("Option type must be one of FXOptionDialog.DEFAULT_OPTION, FXOptionDialog.YES_NO_OPTION, FXOptionDialog.YES_NO_CANCEL_OPTION or FXOptionDialog.OK_CANCEL_OPTION");
            }
            final Button yes = new Button("Yes");
            final Button ok = new Button("Ok");
            final Button no = new Button("No");
            final Button cancel = new Button("Cancel");
            
            yes.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(final ActionEvent t) {
                choice = YES_OPTION;
                positiveClose();
            }});
            ok.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(final ActionEvent t) {
                choice = OK_OPTION;
                positiveClose();
            }});
            no.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(final ActionEvent t) {
                choice = NO_OPTION;
                positiveClose();
            }});
            cancel.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(final ActionEvent t) {
                choice = CANCEL_OPTION;
                positiveClose();
            }});
            
            if(optionType == DEFAULT_OPTION){
                buttons.add(yes);
            }else if (optionType == YES_NO_OPTION){
                buttons.add(yes);
                buttons.add(no);
            }else if(optionType == YES_NO_CANCEL_OPTION){
                buttons.add(yes);
                buttons.add(no);
                buttons.add(cancel);
            }else if(optionType == OK_CANCEL_OPTION){
                buttons.add(ok);
                buttons.add(cancel);
            }
        }else{
            for(int i = 0; i < options.length; i++){
                final Button btn = new Button(options[i].toString());
                if(options[i].equals(initialValue)){
                    btn.requestFocus();
                }
                final int val = i;
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(final ActionEvent t) {
                        choice = val;
                        positiveClose();
                    }
                });
                buttons.add(btn);
            }
        }
        
        getButtonGroup().getChildren().clear();
        getButtonGroup().getChildren().addAll(buttons);
        
    }
    
    public int showOptionDialog(){
        showAndWait();
        return choice;
    }
    
    @Override
    protected void positiveClose(){
        super.positiveClose();
        if(choice == -1){
            choice = YES_OPTION;
        }
    }
}
