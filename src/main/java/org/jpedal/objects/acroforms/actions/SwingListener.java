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
 * SwingListener.java
 * ---------------
 */
package org.jpedal.objects.acroforms.actions;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class SwingListener extends PDFListener implements MouseListener, KeyListener, FocusListener, MouseMotionListener, ActionListener, ListSelectionListener{
    /*
     * deciphering characteristics from formObject bit 1 is index 0 in []
     * 1 = invisible
     * 2 = hidden - dont display or print
     * 3 = print - print if set, dont if not
     * 4 = nozoom
     * 5= norotate
     * 6= noview
     * 7 = read only (ignored by wiget)
     * 8 = locked
     * 9 = togglenoview
     */
    
    public SwingListener(final FormObject form, final ActionHandler formsHandler) {
        
        super(form, formsHandler);
    }
    
    @Override
    public void mouseClicked(final MouseEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.mouseClicked() "+formObject.getObjectRefAsString());
        }
        
        super.mouseClicked(e);
        
    }
    
    @Override
    public void mousePressed(final MouseEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.mousePressed() "+formObject.getObjectRefAsString());
        }
        super.mousePressed(e);
    }
    
    @Override
    public void mouseReleased(final MouseEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.mouseReleased() "+formObject.getObjectRefAsString());
        }
        super.mouseReleased(e);
        
    }
    
    @Override
    public void mouseEntered(final MouseEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.mouseEntered() "+formObject.getObjectRefAsString());
        }
        
        handler.A(e, formObject, ActionHandler.MOUSEENTERED);
        handler.E(e, formObject);
        
        if (formObject.getCharacteristics()[8]) {//togglenoView
            ((Component)formObject.getGUIComponent()).setVisible(true);
        }
        
    }
    
    @Override
    public void mouseExited(final MouseEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.mouseExited() "+formObject.getObjectRefAsString()+ ' ' +e.getPoint()+ ' ' +e.getSource());
        }
        
        handler.A(e, formObject, ActionHandler.MOUSEEXITED);
        handler.X(e, formObject);
        
        if (formObject.getCharacteristics()[8]) {//togglenoView
            ((Component)formObject.getGUIComponent()).setVisible(false);
        }
        
    }
    
    @Override
    public void keyTyped(final KeyEvent e) { //before key added to data
        if(debugMouseActions) {
            System.out.println("SwingListener.keyTyped(" + e + ')');
        }
        
        boolean keyIgnored=false;
        
        //set length
        final int maxLength = formObject.getInt(PdfDictionary.MaxLen);
        
        if(maxLength!=-1){
            
            final char c=e.getKeyChar();
            
            if(c!=8 && c!=127){
                
                final JTextComponent comp= ((JTextComponent) e.getSource());
                
                final String text=comp.getText();
                
                final int length=text.length();
                if(length>=maxLength){
                    e.consume();
                    keyIgnored=true;
                }
                
                if(length>maxLength) {
                    comp.setText(text.substring(0, maxLength));
                }
                
            }
        }
        
        //if valid process further
        if(!keyIgnored){
            
            if(e.getKeyChar()=='\n' && !(e.getSource() instanceof JTextArea)) {
                ((JComponent) e.getSource()).transferFocus();
            }
            //acrorend.getCompData().loseFocus();
            
            final int rejectKey=handler.K(e, formObject, ActionHandler.MOUSEPRESSED);
            
            if(rejectKey==ActionHandler.REJECTKEY) {
                e.consume();
            }
            
            handler.V(e,formObject, ActionHandler.MOUSEPRESSED);
            
        }
    }
    
    @Override
    public void keyPressed(final KeyEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.keyPressed(" + e + ')');
        }
        //ignored at present
    }
    
    @Override
    public void keyReleased(final KeyEvent e) { //after key added to component value
        if(debugMouseActions) {
            System.out.println("SwingListener.keyReleased(" + e + ')');
        }
        
        super.keyReleased(e);
    }
    
    @Override
    public void focusGained(final FocusEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.focusGained()");
        }
        
        super.focusGained(e);
    }
    
    @Override
    public void focusLost(final FocusEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.focusLost() "+formObject.getObjectRefAsString());
        }
        
        super.focusLost(e);
    }
    
    @Override
    public void mouseDragged(final MouseEvent e) {
        //		if(debugMouseActions)
        //    		System.out.println("SwingListener.mouseDragged()");
        //ignored at present
    }
    
    @Override
    public void mouseMoved(final MouseEvent e) {
        //		if(debugMouseActions)
        //    		System.out.println("SwingListener.mouseMoved()");
        
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.actionPerformed() "+formObject.getObjectRefAsString());
        }
        
        //this is called by ulc instead of mouseclicked
        mouseClicked(e);
    }
    
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if(debugMouseActions) {
            System.out.println("SwingListener.valueChanged()");
        }
        
        //added so the list selection can be updated to the proxy
        mouseClicked(e);
    }
}
