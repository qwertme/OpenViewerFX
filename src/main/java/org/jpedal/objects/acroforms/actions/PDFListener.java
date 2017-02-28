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
 * PDFListener.java
 * ---------------
 */
package org.jpedal.objects.acroforms.actions;

import org.jpedal.objects.raw.FormObject;
     
/**
 * shared non component-specific code
 */
public class PDFListener {
	
	public static final boolean debugMouseActions = false;
	
    public final FormObject formObject;
    public final ActionHandler handler;

    protected PDFListener(final FormObject form, final ActionHandler formsHandler) {
        formObject = form;
        handler = formsHandler;
    }

    public void mouseReleased(final Object e) {
    	if(debugMouseActions) {
            System.out.println("PDFListener.mouseReleased() "+formObject.getObjectRefAsString());
        }
	
	if (handler != null) {
	    handler.A(e, formObject, ActionHandler.MOUSERELEASED);
	    handler.U(e, formObject);
	}
    }

    public void mouseClicked(final Object e) {
    	if(debugMouseActions) {
            System.out.println("PDFListener.mouseClicked() "+formObject.getObjectRefAsString());
        }
	
	if (handler != null) {
	    handler.A(e, formObject, ActionHandler.MOUSECLICKED);
	}
    }

    public void mousePressed(final Object e) {
    	if(debugMouseActions) {
            System.out.println("PDFListener.mousePressed() "+formObject.getObjectRefAsString());
        }
	
	if (handler != null){
	    handler.A(e, formObject, ActionHandler.MOUSEPRESSED);
	    handler.D(e, formObject);
	}
    }

    public void keyReleased(final Object e) {
    	if(debugMouseActions) {
            System.out.println("PDFListener.keyReleased(" + e + ") "+formObject.getObjectRefAsString());
        }
	
	if (handler != null){
	    handler.K(e, formObject, ActionHandler.MOUSERELEASED);
	    handler.V(e,formObject, ActionHandler.MOUSERELEASED);
	}
    }

    public void focusLost(final Object e) {
    	if(debugMouseActions) {
            System.out.println("PDFListener.focusLost() "+formObject.getObjectRefAsString());
        }
    	
	if (handler != null){
	    handler.Bl(e, formObject);
	    handler.K(e, formObject,ActionHandler.FOCUS_EVENT);
	    handler.V(e, formObject,ActionHandler.FOCUS_EVENT);
		    //format the field value after it has been altered
	    handler.F(formObject);
	}
        
        //this is added in so that the popup forms do not flash with focus 
        //but is causes forms focus to be lost unexpectadly.
//        acrorend.getCompData().loseFocus();
    }

    public void focusGained(final Object e) {
    	if(debugMouseActions) {
            System.out.println("PDFListener.focusGained() "+formObject.getObjectRefAsString());
        }
    	
	if(handler!=null) {
        handler.Fo(e, formObject);
    }

        //this needs to only be done on certain files, that specify this, not all.
        //user can enter some values (ie 1.10.2007 as its still valid for a date which are then turned into
        //01.10.2007 when user quits field. If user re-enters form, this sets it back to 1.10.2007
        //String fieldRef = formObject.getPDFRef();
        //String fieldRef = formObject.getObjectRefAsString();
        
        //Object lastUnformattedValue=acrorend.getCompData().getLastUnformattedValue(fieldRef);
        //if(lastUnformattedValue!=null && !lastUnformattedValue.equals(formObject.getFormValue())){
	//			acrorend.getCompData().setValue(fieldRef,lastUnformattedValue);
       //}
    }
}