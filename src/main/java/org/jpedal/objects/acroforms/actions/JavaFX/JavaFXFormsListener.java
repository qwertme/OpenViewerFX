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
 * JavaFXFormsListener.java
 * ---------------
 */

package org.jpedal.objects.acroforms.actions.JavaFX;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.actions.PDFListener;
import org.jpedal.objects.raw.FormObject;

/**
 * Listener which listens to various actions on JavaFX Form objects and 
 * executes the correct method in ActionHandler
 * @author Simon
 */
public class JavaFXFormsListener extends PDFListener{
    private boolean debug;
    
    private final EventHandler<MouseEvent> mouseHandler;
    private EventHandler<TouchEvent> touchHandler;
    private final ChangeListener<Boolean> focusHandler;
    private final EventHandler<KeyEvent> keyHandler;

    public JavaFXFormsListener(final FormObject form, final ActionHandler handler){
        super(form,handler);
        
        mouseHandler = new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent event) {
                handleMouseEvent(event);
            }
        };
        
        focusHandler = new ChangeListener<Boolean>() {
            @Override public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
                handleFocusEvent(newValue);
            }
        };
        
        keyHandler = new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent event) {
                handleKeyEvent(event);
            }
        };
    }

    public EventHandler<MouseEvent> getMouseHandler(){
        return mouseHandler;
    }
    public EventHandler<TouchEvent> getTouchHandler(){
        return touchHandler;
    }
    public ChangeListener<Boolean> getFocusHandler(){
        return focusHandler;
    }
    public EventHandler<KeyEvent> getKeyHandler(){
        return keyHandler;
    }
    
    private void handleMouseEvent(final MouseEvent event) {
        if(debug) {
            System.out.println("JavaFXFormsMouseListener event: " + event.getEventType().getName());
        }

        if(event.getEventType() == MouseEvent.MOUSE_CLICKED){
            super.mouseClicked(event);
        }else if (event.getEventType() == MouseEvent.MOUSE_PRESSED){
            super.mousePressed(event);
        }else if (event.getEventType() == MouseEvent.MOUSE_RELEASED){
            super.mouseReleased(event);
        }else if (event.getEventType() == MouseEvent.MOUSE_ENTERED){
            handler.A(event, formObject, ActionHandler.MOUSEENTERED);
            handler.E(event, formObject);

            if (formObject.getCharacteristics()[8]) {//togglenoView
                ((Node)formObject.getGUIComponent()).setVisible(true);
            }
        }else if (event.getEventType() == MouseEvent.MOUSE_EXITED){
            handler.A(event, formObject, ActionHandler.MOUSEEXITED);
            handler.X(event, formObject);

            if (formObject.getCharacteristics()[8]) {//togglenoView
                ((Node)formObject.getGUIComponent()).setVisible(false);
            }
        }
    }

    private void handleFocusEvent(final boolean gainedFocus){
        if(gainedFocus) {
            super.focusGained(null);
        } else {
            super.focusLost(null);
        }
    }
    
    private void handleKeyEvent(final KeyEvent event){
        if(event.getEventType().equals(KeyEvent.KEY_PRESSED)){
            // Ignored at present
        }else if(event.getEventType().equals(KeyEvent.KEY_RELEASED)){
            super.keyReleased(event);
        }
    }
    
}
