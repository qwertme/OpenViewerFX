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
 * ExpressionEngine.java
 * ---------------
 */
package org.jpedal.objects.javascript;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.AcroRenderer;

/**
 * allow user to handle expressions with own implementation
 */

public interface ExpressionEngine {

    /**
     *
     * @param ref  ie 1 0 R
     * @param type - defined in ActionHandler (ie K)
     * @param js - Javascript string
     * @param eventType - type of event (Keystroke, focus)
     * @param keyPressed - if key event, key value , otherwsie space
     * @return return code (ActionHandler.STOPPROCESSING to ignore JPedal handling)
     */
	//NOTE type is used externally so needs to be kept
    int execute(FormObject ref, int type, Object js,int eventType,char keyPressed);

    /**
     * called on close to do any cleanup
     */
    void closeFile();

    /**
     * return true if JPedal should do nothing, false if JPedal should execute command as well
     * 
     * if code == ErrorCodes.JSInvalidRangeFormat
     * then args[1] is the forrmatted message to be displayed
     */
    boolean reportError(int code, Object[] args);

//	String handleAFCommands(String js, int eventType, char keyPressed, String currentValue);

    /** adding code for the javascript to  call as needed */
    @SuppressWarnings("UnusedReturnValue")
    int addCode(String value);

	void executeFunctions(String jsCode, FormObject formObject);

	void dispose();

    void setAcroRenderer(AcroRenderer acro);
}
