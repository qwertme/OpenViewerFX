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
 * JavaFXControlListener.java
 * ---------------
 */

package org.jpedal.objects.acroforms.actions.JavaFX;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

/**
 * Unlike Swing, JavaFX doesn't allow multiple listeners to be attached to the same event.
 * 
 * As such, instead of making an awkward daisy-chain of events in JavaFX form factory,
 * this class will be used to store the events as a single point of entry.
 * 
 * @author Simon
 */
public class JavaFXControlListener {
    private static final boolean debug = false;
    private final Control comp;
    // Event Handlers - Initialised in the constructor
    private final EventHandler<MouseEvent> mouseHandler;
    private final EventHandler<TouchEvent> touchHandler;
    private final EventHandler<KeyEvent> keyHandler;
    private final ChangeListener<Boolean> focusHandler;
    // Note: All the listeners are initialised as needed.
    // Mouse listeners
    private List<EventHandler<MouseEvent>> pressed;
    private List<EventHandler<MouseEvent>> clicked;
    private List<EventHandler<MouseEvent>> released;
    private List<EventHandler<MouseEvent>> entered;
    private List<EventHandler<MouseEvent>> exited;
    // Mouse motion listners
    private List<EventHandler<MouseEvent>> dragged;
    private List<EventHandler<MouseEvent>> moved;
    // Touch listeners
    private List<EventHandler<TouchEvent>> touchPressed;
    private List<EventHandler<TouchEvent>> touchReleased;
    // Key listeners
    private List<EventHandler<KeyEvent>> keyReleased;
    private List<EventHandler<KeyEvent>> keyPressed;
    private List<ChangeListener<Boolean>> focusEvents;
    
    public JavaFXControlListener(final Control comp){
        this.comp = comp;
        mouseHandler = new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent event) {
                handleMouseEvent(event);
            }
        };
        touchHandler = new EventHandler<TouchEvent>() {
            @Override public void handle(final TouchEvent event) {
                handleTouchEvent(event);
            }
        };
        
        keyHandler = new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent event) {
                handleKeyEvent(event);
            }
        };
        
        focusHandler = new ChangeListener<Boolean>() {
            @Override public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
                handleFocusEvent(observable, oldValue, newValue);
            }
        };
    }
    
    private void handleMouseEvent(final MouseEvent event){
        if(debug) {
            System.out.println("Mouse event - " + event.getEventType().getName());
        }
        
        if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
            executeMouseEvent(clicked, event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
            executeMouseEvent(entered, event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)){
            executeMouseEvent(exited, event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)){
            executeMouseEvent(pressed, event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)){
            executeMouseEvent(released, event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)){
            executeMouseEvent(dragged, event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_MOVED)){
            executeMouseEvent(moved, event);
        }
    }
    
    private static void executeMouseEvent(final List<EventHandler<MouseEvent>> events, final MouseEvent mouseEvent){
        if(events != null){
            for(final EventHandler<MouseEvent> event : events){
                event.handle(mouseEvent);
            }
        }
    }
    
    private void handleTouchEvent(final TouchEvent event){
        if(debug) {
            System.out.println("Touch event - " + event.getEventType().getName());
        }
        
        if(event.getEventType().equals(TouchEvent.TOUCH_PRESSED)){
            executeTouchEvent(touchPressed, event);
        }else if(event.getEventType().equals(TouchEvent.TOUCH_RELEASED)){
            executeTouchEvent(touchReleased, event);
        }
    }
    
    private static void executeTouchEvent(final List<EventHandler<TouchEvent>> events, final TouchEvent touchEvent){
        if(events != null){
            for(final EventHandler<TouchEvent> event : events){
                event.handle(touchEvent);
            }
        }
    }
    
    private void handleKeyEvent(final KeyEvent event){
        if(debug) {
            System.out.println("Key Event - " + event.getEventType().getName());
        }
        
        if(event.getEventType().equals(KeyEvent.KEY_PRESSED)){
            executeKeyEvent(keyPressed, event);
        }else if(event.getEventType().equals(KeyEvent.KEY_RELEASED)){
            executeKeyEvent(keyReleased, event);
        }
    }
    
    private static void executeKeyEvent(final List<EventHandler<KeyEvent>> events, final KeyEvent keyEvent){
        if(events != null){
            for(final EventHandler<KeyEvent> event : events){
                event.handle(keyEvent);
            }
        }
    }
    
    private void handleFocusEvent(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
        if(focusEvents != null){
            for(final ChangeListener<Boolean> event : focusEvents){
                event.changed(observable, oldValue, newValue);
            }
        }
    }
    
    /**
     * Add a listener to all mouse events
     * @param handler 
     */
    public void addMouseListener(final EventHandler<MouseEvent> handler){
        addOnMouseClickedListener(handler);
        addOnMouseEnteredListener(handler);
        addOnMouseExitedListener(handler);
        addOnMousePressedListener(handler);
        addOnMouseReleasedListener(handler);
        addOnMouseDraggedListener(handler);
        addOnMouseMovedListener(handler);
    }
    
    public void addOnMouseClickedListener(final EventHandler<MouseEvent> handler){
        if(clicked == null){
            clicked = new ArrayList<EventHandler<MouseEvent>>();
            comp.setOnMouseClicked(mouseHandler);
        }
        clicked.add(handler);
    }
    
    public void addOnMousePressedListener(final EventHandler<MouseEvent> handler){
        if(pressed == null){
            pressed = new ArrayList<EventHandler<MouseEvent>>();
            comp.setOnMousePressed(mouseHandler);
        }
        pressed.add(handler);
    }
    
    public void addOnMouseReleasedListener(final EventHandler<MouseEvent> handler){
        if(released == null){
            released = new ArrayList<EventHandler<MouseEvent>>();
            comp.setOnMouseReleased(mouseHandler);
        }
        released.add(handler);
    }
    
    public void addOnMouseEnteredListener(final EventHandler<MouseEvent> handler){
        if(entered == null){
            entered = new ArrayList<EventHandler<MouseEvent>>();
            comp.setOnMouseEntered(mouseHandler);
        }
        entered.add(handler);
    }
    
    public void addOnMouseExitedListener(final EventHandler<MouseEvent> handler){
        if(exited == null){
            exited = new ArrayList<EventHandler<MouseEvent>>();
            comp.setOnMouseExited(mouseHandler);
        }
        exited.add(handler);
    }
    
    public void addOnMouseDraggedListener(final EventHandler<MouseEvent> handler){
        if(dragged == null){
            dragged = new ArrayList<EventHandler<MouseEvent>>();
            comp.setOnMouseExited(mouseHandler);
        }
        dragged.add(handler);
    }
    
    public void addOnMouseMovedListener(final EventHandler<MouseEvent> handler){
        if(moved == null){
            moved = new ArrayList<EventHandler<MouseEvent>>();
            comp.setOnMouseExited(mouseHandler);
        }
        moved.add(handler);
    }
    
    
    
    public void addOnTouchPressedListener(final EventHandler<TouchEvent> handler){
        if(touchPressed == null){
            touchPressed = new ArrayList<EventHandler<TouchEvent>>();
            comp.setOnTouchPressed(touchHandler);
        }
        touchPressed.add(handler);
    }
    
    public void addOnTouchReleasedListener(final EventHandler<TouchEvent> handler){
        if(touchReleased == null){
            touchReleased = new ArrayList<EventHandler<TouchEvent>>();
            comp.setOnTouchReleased(touchHandler);
        }
        touchReleased.add(handler);
    }
    
    
    
    public void addKeyListener(final EventHandler<KeyEvent> handler){
        addOnKeyPressedListener(handler);
        addOnKeyReleasedListener(handler);
    }
    
    public void addOnKeyReleasedListener(final EventHandler<KeyEvent> handler){
        if(keyReleased == null){
            keyReleased = new ArrayList<EventHandler<KeyEvent>>();
            comp.setOnKeyReleased(keyHandler);
        }
        keyReleased.add(handler);
    }
    
    public void addOnKeyPressedListener(final EventHandler<KeyEvent> handler){
        if(keyPressed == null){
            keyPressed = new ArrayList<EventHandler<KeyEvent>>();
            comp.setOnKeyPressed(keyHandler);
        }
        keyPressed.add(handler);
    }
    
    public void addFocusEvent(final ChangeListener<Boolean> handler){
        if(focusEvents == null){
            focusEvents = new ArrayList<ChangeListener<Boolean>>();
            comp.focusedProperty().addListener(focusHandler);
        }
        focusEvents.add(handler);
    }
    
}
