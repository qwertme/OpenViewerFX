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
 * JavaFXFormButtonListener.java
 * ---------------
 */
package org.jpedal.objects.acroforms.actions.JavaFX;

import java.util.HashMap;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseEvent;
import org.jpedal.objects.acroforms.actions.PDFListener;

public class JavaFXFormButtonListener implements EventHandler<MouseEvent>{

    private static final boolean showMethods = false;

    private Map captionChanger;
    
    public JavaFXFormButtonListener(final String normalCaption, final String rolloverCaption, final String downCaption){
       if(showMethods) {
           System.out.println("JavaFXFormButtonListener.JavaFXFormButtonListener(string string string)");
       }
       int captions = 0;
       
       captionChanger = new HashMap();
       if (rolloverCaption != null && !rolloverCaption.isEmpty()){
            captionChanger.put("rollover", rolloverCaption);
            captions++;
        }
        if (downCaption != null && !downCaption.isEmpty()){
            captionChanger.put("down", downCaption);
            captions++;
        }
        if(normalCaption!=null  && !normalCaption.isEmpty()){
        	captionChanger.put("normal", normalCaption);
        	captions++;
        }
        
        if(captions==0) {
            captionChanger = null;
        }
    }
    
    public void mouseEntered(final MouseEvent e) {
        if(PDFListener.debugMouseActions || showMethods) {
            System.out.println("JavaFXFormButtonListener.mouseEntered()");
        }
        if((captionChanger != null && e.getSource() instanceof ButtonBase) && 
            (captionChanger.containsKey(("rollover")))){
                ((ButtonBase)e.getSource()).setText((String) captionChanger.get("rollover"));
            }
        }
    

    public void mouseExited(final MouseEvent e) {
        if (PDFListener.debugMouseActions || showMethods) {
            System.out.println("customMouseListener.mouseExited()");
        }
        if ((captionChanger != null && e.getSource() instanceof ButtonBase) &&
            (captionChanger.containsKey("normal"))) {
                ((ButtonBase) e.getSource()).setText((String) captionChanger.get("normal"));
            }
        }
    

    public static void mouseClicked(final MouseEvent e) {
        if(PDFListener.debugMouseActions || showMethods) {
            System.out.println("SwingFormButtonListener.mouseClicked()");
        }
    }

    public void mousePressed(final MouseEvent e) {
        if (PDFListener.debugMouseActions || showMethods) {
            System.out.println("customMouseListener.mousePressed()");
        }
        if ((captionChanger != null && e.getSource() instanceof ButtonBase) &&
             (captionChanger.containsKey("down"))) {
               ((ButtonBase) e.getSource()).setText((String) captionChanger.get("down"));
            }
        }
    

    public void mouseReleased(final MouseEvent e) {
         if (PDFListener.debugMouseActions || showMethods) {
             System.out.println("customMouseListener.mouseReleased()");
         }
         if (captionChanger != null && e.getSource() instanceof ButtonBase){
             if (captionChanger.containsKey("rollover")) {
               ((ButtonBase) e.getSource()).setText((String) captionChanger.get("rollover")); 
             }else if(captionChanger.containsKey("normal")){
                 ((ButtonBase)e.getSource()).setText((String) captionChanger.get("rollover")); 
             }
         } 
    }

    @Override
    public void handle(final MouseEvent event) {
        if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
            mouseClicked(event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
            mouseEntered(event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)){
            mouseExited(event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)){
            mousePressed(event);
        }else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)){
            mouseReleased(event);
        }            
    }
}
