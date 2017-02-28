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
 * SwingFormButtonListener.java
 * ---------------
 */
package org.jpedal.objects.acroforms.actions;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;

/**
 * class implements MouseListener to create all required actions for the associated button
 *
 * @author chris
 */
public class SwingFormButtonListener implements MouseListener {

    private static final boolean showMethods = false;

    private Map captionChanger;

    /**
     * sets up the captions to change when needed
     */
    public SwingFormButtonListener(final String normalCaption, final String rolloverCaption, final String downCaption) {
    	if(showMethods) {
            System.out.println("SwingFormButtonListener.SwingFormButtonListener(string string string)");
        }
    	
    	int captions = 0;
        //set up the captions to work for rollover and down presses of the mouse
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

    @Override
    public void mouseEntered(final MouseEvent e) {
        //noinspection PointlessBooleanExpression,PointlessBooleanExpression,PointlessBooleanExpression,PointlessBooleanExpression
        if (PDFListener.debugMouseActions || showMethods) {
            System.out.println("SwingFormButtonListener.mouseEntered()");
        }

        if ((captionChanger != null && e.getSource() instanceof AbstractButton) &&
             (captionChanger.containsKey("rollover"))) {
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("rollover"));
            }
        }
    

    @Override
    public void mouseExited(final MouseEvent e) {
        //noinspection PointlessBooleanExpression
        if (PDFListener.debugMouseActions || showMethods) {
            System.out.println("customMouseListener.mouseExited()");
        }

        if ((captionChanger != null && e.getSource() instanceof AbstractButton) &&
             (captionChanger.containsKey("normal"))) {
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("normal"));
            }
        }
    

    @Override
    public void mouseClicked(final MouseEvent e) {
        //noinspection PointlessBooleanExpression
        if(PDFListener.debugMouseActions || showMethods) {
            System.out.println("SwingFormButtonListener.mouseClicked()");
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        //noinspection PointlessBooleanExpression
        if (PDFListener.debugMouseActions || showMethods) {
            System.out.println("customMouseListener.mousePressed()");
        }

        if ((captionChanger != null && e.getSource() instanceof AbstractButton) &&
             (captionChanger.containsKey("down"))) {
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("down"));
            }
        }
    

    @Override
    public void mouseReleased(final MouseEvent e) {
        //noinspection PointlessBooleanExpression
        if (PDFListener.debugMouseActions || showMethods) {
            System.out.println("customMouseListener.mouseReleased()");
        }

        if (captionChanger != null && e.getSource() instanceof AbstractButton) {
            if (captionChanger.containsKey("rollover")) {
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("rollover"));
            } else if (captionChanger.containsKey("normal")){
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("normal"));
            }
        }
    }
}
