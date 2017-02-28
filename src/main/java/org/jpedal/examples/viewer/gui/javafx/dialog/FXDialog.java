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
 * FXDialog.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx.dialog;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * Allows for Dialog windows to be created. e.g. properties windows, input dialogs.
 * 
 * @author Simon
 */
public class FXDialog {
    protected Stage parent;
    protected Stage dialog;
    private Parent content;
    // Empty node used to free content after the dialog is closed
    private final Group dummyPane = new Group();
    protected boolean isCancelled;
    private boolean disposeOnExit = true;
    
    /**
     * Empty constructor for subclasses
     */
    protected FXDialog(){}
    
    /**
     * Create a new Dialog, the width and height of the Dialog will be determined
     * by the content bounds.
     * 
     * @param parent The stage of Dialog
     * @param modality The mode of the Dialog
     * @param content The content the Dialog created
     */
    public FXDialog(final Stage parent, final Modality modality, final Pane content){
        init(parent, modality, content);
        
        final Scene scene = new Scene(content);
        setScene(scene);
    }
    
    /**
     * Create a new Dialog with a specified width and height. Recommended for creating
     * Dialogs which you don't want to be resizeable
     * 
     * @param parent The stage of Dialog
     * @param modality The mode of the Dialog
     * @param content The content the Dialog created
     * @param width The fixed width value of the dialog 
     * @param height The fixed height value of the dialog 
     */
    public FXDialog(final Stage parent, final Modality modality, final Parent content, final double width, final double height){
        init(parent, modality, content);
        
        dialog.setWidth(width);
        dialog.setHeight(height);
        
        final Scene scene = new Scene(content, width, height);
        setScene(scene);
        
    }
    
    protected final void init(final Stage parent, final Modality modality, final Parent content){
        this.dialog = new Stage(StageStyle.UTILITY);
        this.parent = parent;
        this.content = content;
        
        dialog.initOwner(parent);
        dialog.initModality(modality);
        
        // When the user closes the window using the X
        dialog.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override public void handle(final WindowEvent t) {
                freeContent();
                isCancelled = true;
            }
        });
        
        dialog.setOnShown(new EventHandler<WindowEvent>() {
            @Override public void handle(final WindowEvent t) {
                centerOnParent();
            }
        });
    }
    
    protected final void setScene(final Scene scene){
        dialog.setScene(scene);
        
        dialog.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent t) {
                final KeyCode key = t.getCode();
                if((key == KeyCode.F4 && t.isAltDown()) || key == KeyCode.ESCAPE){
                    cancel();
                }else if(key == KeyCode.ENTER){
                    positiveClose();
                    close();
                }
            }
        });
    }
    
    /**
     * Works the same as show but also centres the Dialog relative to the parent window
     * See http://download.java.net/jdk8/jfxdocs/javafx/stage/Stage.html#show--
     */
    public void show(){
        dialog.show();
    }
    
    public void close(){
        freeContent();
        dialog.close();
    }
    
    /**
     *
     * Works the same as showAndWait but also centres the Dialog relative to the parent window
     * See http://download.java.net/jdk8/jfxdocs/javafx/stage/Stage.html#showAndWait--
     * 
     */
    public void showAndWait(){
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    dialog.showAndWait();
                }
            });
        }else{
            dialog.showAndWait();
        }
    }
    
    private void centerOnParent(){
        // Ignore if no parent is set
        if(parent == null){ 
            dialog.centerOnScreen();
            return;
        }
        
        final double x = parent.getX();
        final double y = parent.getY();
        final double width = dialog.getWidth();
        final double height = dialog.getHeight();
        
        final double xPos = (x + (parent.getWidth()/2)) - (width/2);
        final double yPos = (y + (parent.getHeight()/2)) - (height/2);
        
        dialog.setX(xPos);
        dialog.setY(yPos);
    }
    
    protected void cancel(){
        freeContent();
        isCancelled = true;
        dialog.close();
    }
    
    /**
     * Removes the content node from the scene, allows the node to be reused
     */
    protected final void freeContent(){
        if(disposeOnExit) {
            dialog.getScene().setRoot(dummyPane);
        }
    }
    
    /**
     * Method stub for sub classes do not call from FXDialog
     * e.g. When a user press the "Ok" button
     */
    protected void positiveClose(){
        freeContent();
    }
    
    /**
     * Gets the stage of the Dialog. Use if you need to attach listeners or access
     * properties.
     * @return Stage of the Dialog
     */
    public Stage getDialog(){
        return dialog;
    }
    
    public void setWidth(final double width){
        dialog.setWidth(width);
    }
    
    public void setHeight(final double height){
        dialog.setHeight(height);
    }
    
    public double getWidth(){
        return dialog.getWidth();
    }
    
    public double getHeight(){
        return dialog.getHeight();
    }
    
    public void setResizeable(final boolean isResizeable){
        dialog.setResizable(isResizeable);
    }
    
    public boolean isResizeable(){
        return dialog.isResizable();
    }
    

    public Parent getContent() {
        return content;
    }

    public void setContent(final Pane content) {
        this.content = content;
    }
    
    public void setTitle(final String title){
        dialog.setTitle(title);
    }
    
    /**
     * Set to true if you want to reuse the same dialog.
     * @param dispose A boolean value of setDisposeOnExit
     */
    public void setDisposeOnExit(final boolean dispose){
        disposeOnExit = dispose;
    }
    
    public boolean getDisposeOnExit(){
        return disposeOnExit;
    }
    
}
