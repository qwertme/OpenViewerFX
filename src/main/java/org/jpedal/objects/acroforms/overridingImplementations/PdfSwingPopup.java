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
 * PdfSwingPopup.java
 * ---------------
 */
package org.jpedal.objects.acroforms.overridingImplementations;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

import javax.swing.*;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * provide PDF poup for Annotations
 */
public class PdfSwingPopup extends JInternalFrame{
    /**
     *
     */
    private static final long serialVersionUID = 796302916236391896L;
    
    JTextArea titleBar;
	JTextArea contentArea;
	
	final FormObject formObject;
	
    @SuppressWarnings("UnusedParameters")
    public PdfSwingPopup(final FormObject popupObj, final int cropBoxWidth) {

        formObject = popupObj;
	
	/**
	 * all the popup data is in the Parent not the popup object
	 */
	final PdfObject parentObj=popupObj.getParentPdfObj();
	
	if(parentObj==null) {
        return;
    }

	//Set color from the popup object
    float[] col = popupObj.getFloatArray(PdfDictionary.C);
	
	//If no C value present, check the parent object
	if(col==null){
		col = parentObj.getFloatArray(PdfDictionary.C);
	}
	
	//If no color specified then use our default
	if(col==null){
		col = new float[]{255,255,0};
	}
	
	//read in date for title bar
	final String mStream = parentObj.getTextStreamValue(PdfDictionary.M);
	StringBuffer date = null;
	if(mStream!=null){
	    date = new StringBuffer(mStream);
	    date.delete(0, 2);//delete D:
	    date.insert(10, ':');
	    date.insert(13, ':');
	    date.insert(16, ' ');
	    
	    final String year = date.substring(0, 4);
	    final String day = date.substring(6,8);
	    date.delete(6,8);
	    date.delete(0, 4);
	    date.insert(0, day);
	    date.insert(4, year);
	    date.insert(2, '/');
	    date.insert(5, '/');
	    date.insert(10, ' ');
	    
	    //			date.delete(19, date.length());//delete the +01'00' Time zone definition
	}
	
	//setup title text for popup
	final String subject = parentObj.getTextStreamValue(PdfDictionary.Subj);
	String popupTitle = popupObj.getTextStreamValue(PdfDictionary.T);
	if(popupTitle==null) {
        popupTitle = "";
    }
	
	String title="";
	if(subject!=null) {
        title += subject + '\t';
    }
	if(date!=null) {
        title += date;
    }
	title += '\n' +popupTitle;
	
	//main body text on contents is always a text readable form of the form or the content of the popup window.
	String contentString = parentObj.getTextStreamValue(PdfDictionary.Contents);
	if(contentString==null) {
        contentString = "";
    }
	if(contentString.indexOf('\r')!=-1) {
        contentString = contentString.replaceAll("\r", "\n");
    }
	
	//setup background color
	Color bgColor = null;
	if(col!=null){
	    if(col[0]>1 || col[1]>1 || col[2]>1) {
            bgColor = new Color((int) col[0], (int) col[1], (int) col[2]);
        } else {
            bgColor = new Color(col[0], col[1], col[2]);
        }
	    
	    //and set border to that if valid
	    setBorder(BorderFactory.createLineBorder(bgColor));
	}
	
	//remove title bar from internalframe so its looks as we want
	((javax.swing.plaf.basic.BasicInternalFrameUI)
		this.getUI()).setNorthPane(null);
	
	setLayout(new BorderLayout());
	
	
	//add title bar
	titleBar = new JTextArea(title);
	titleBar.setEditable(false);
	if(bgColor!=null) {
        titleBar.setBackground(bgColor);
    }
	add(titleBar,BorderLayout.NORTH);
	
	//add content area
	contentArea = new JTextArea(contentString);
	contentArea.setWrapStyleWord(true);
	contentArea.setLineWrap(true);
	add(contentArea,BorderLayout.CENTER);
	
	//set the font sizes so that they look more like adobes popups
	final Font titFont = titleBar.getFont();
	
	//Set base font size
	final int baseFontSize = (int)formObject.getFontSize();
			
	titleBar.setFont(new Font(titFont.getName(),titFont.getStyle(),baseFontSize-1));
	final Font curFont = contentArea.getFont();
	contentArea.setFont(new Font(curFont.getName(),curFont.getStyle(),baseFontSize-2));
	
	//add our drag listener so it acts like an internal frame
	final MyMouseMotionAdapter mmma = new MyMouseMotionAdapter();
	titleBar.addMouseMotionListener(mmma);
	
	//add focus listener to bring selected popup to front
	addFocusListener(new FocusAdapter(){
	    @Override
        public void focusGained(final FocusEvent e) {
		toFront();
		super.focusGained(e);
	    }
	});
        
        // Fix for popups showing up behind other objects on page        
        addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameActivated(final InternalFrameEvent e) {
                final JInternalFrame frame = e.getInternalFrame();
                // Brings the component to the front
                frame.getParent().setComponentZOrder(frame, 0);
            }
            
            @Override
            public void internalFrameDeactivated(final InternalFrameEvent e) {
                final JInternalFrame frame = e.getInternalFrame();
                
                //Changing page will set parent to null, so if null we ignore
                if(frame.getParent()!=null){ 
                	// Sends the component back by one when focus is lost
                	frame.getParent().setComponentZOrder(frame, 1);
                }
            }
        });
    }
    
    final Point currPos = new Point(0, 0);
    private class MyMouseMotionAdapter extends MouseMotionAdapter{
	@Override
    public void mouseDragged(final MouseEvent e) {
	    //move the popup as the user drags the mouse
	    final Point pt = e.getPoint();
	    final Point curLoc = getLocation();
	    currPos.x += pt.x;
	    currPos.y += pt.y;
	    curLoc.translate(pt.x, pt.y);
	    setLocation(curLoc);
	    formObject.setUserSetOffset(currPos);
	    super.mouseDragged(e);
	}
    }
    
    @Override
    /**
     * Set the font for the popup window.
     * The font is modified in size for the title and the content.
     */
    public void setFont(final Font f){
    	super.setFont(f);

    	final int fontSize = f.getSize();

    	if(titleBar!=null) {
            titleBar.setFont(titleBar.getFont().deriveFont((float) fontSize - 1));
        }
    	
    	if(contentArea!=null && titleBar!=null) {
            contentArea.setFont(titleBar.getFont().deriveFont((float) fontSize - 2));
        }

    }
       
    /**
     * Override paint so that the popup remains in it's current z-position
     * after a repaint (e.g. resize), preventign it from going behind other elements
     */
}
