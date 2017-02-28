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
 * ActionHandler.java
 * ---------------
 */
package org.jpedal.objects.acroforms.actions;

import org.jpedal.PdfDecoderInt;

import org.jpedal.objects.Javascript;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.layers.PdfLayerList;

@SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
public interface ActionHandler {

    int MOUSEPRESSED = 1;
    int MOUSERELEASED = 2;
    int MOUSECLICKED = 3;
    int MOUSEENTERED = 4;
    int MOUSEEXITED = 5;

    int FOCUS_EVENT = 6;

    int TODO = -1;

    int NOMESSAGE = 0;
    int REJECTKEY = 1;
    int STOPPROCESSING = 2;
    int VALUESCHANGED = 3;


    /**
     * A action when pressed in active area ?some others should now be ignored?
     * @param e Object contained by the action
     * @param formObject - Actual formObject containing data
     * @param eventType - type of event (Keystroke, focus)
     */
    void A(Object e, FormObject formObject, int eventType);

    /**
     * E action when cursor enters active area
     * @param e Object contained by the action
     * @param formObject - Actual formObject containing data
     */
    void E(Object e, FormObject formObject);

    /**
     * X action when cursor exits active area
     * @param e Object contained by the action
     * @param formObject - Actual formObject containing data
     */
    void X(Object e, FormObject formObject);

    /**
     * D action when cursor button pressed inside active area
     * @param e Object contained by the action
     * @param formObj - Actual formObject containing data
     */
    void D(Object e, FormObject formObj);

    /**
     * U action when cursor button released inside active area
     * @param e Object contained by the action
     * @param formObj - Actual formObject containing data
     */
    void U(Object e, FormObject formObj);

    /**
     * Fo action on input focus
     * @param e Object contained by the action
     * @param formObj - Actual formObject containing data
     */
    void Fo(Object e, FormObject formObj);

    /**
     * Bl action when input focus lost
     * @param e Object contained by the action
     * @param formObj - Actual formObject containing data
     */
    void Bl(Object e, FormObject formObj);

    /**
     * PO action when page containing is opened,
     * action O from page dictionary, and OpenAction in document catalog should be done first
     * @param pdfObject - of the opening page
     * @param type  - the type of action
     */
    void PO(PdfObject pdfObject, int type);

    /**
     * O action when page containing is opened,
     * @param pdfObject - of the opening page
     * @param type  - the type of action
     */
    void O(PdfObject pdfObject, int type);

    /**
     * PC action when page is closed
     * @param pdfObject - of the opening page
     * @param type  - the type of action
     */
    void PC(PdfObject pdfObject, int type);

    /**
     * PV action on viewing containing page
     * @param pdfObject - of the opening page
     * @param type  - the type of action
     */
    void PV(PdfObject pdfObject, int type);

    /**
     * PI action when no longer visible in viewer
     * @param pdfObject - of the opening page
     * @param type  - the type of action
     */
    void PI(PdfObject pdfObject, int type);

    /**
     * K action on - [javascript]
     * keystroke in textfield or combobox
     * modifys the list box selection
     * (can access the keystroke for validity and reject or modify)
     * @param e - Object contained by the action
     * @param formObject  - Actual formObject containing data
     * @param actionID - integer value of the actionID
     * @return the action
     */
    int K(Object e, FormObject formObject, int actionID);

    /**
     * F the display formatting of the field (e.g 2 decimal places) [javascript]
     * @param formObject - Actual formObject containing data
     */
    void F(FormObject formObject);

    /**
     * V action when fields value is changed [javascript]
     * @param e - Object contained by the action
     * @param formObject - Actual formObject containing data
     * @param actionID - integer value of the actionID
     */
    void V(Object e, FormObject formObject, int actionID);

    /**
     * C action when another field changes (recalculate this field) [javascript]
     * should not be called other than from internal methods to action changes on other fields.
     * @param formObject - Actual formObject containing data
     */
    void C(FormObject formObject);

    PdfDecoderInt getPDFDecoder();
    
    /**
     * creates a returns an action listener that will change the down icon for each click
     * <br>
     * 2 icons that need to be changed when the button is sellected and not selected,
     * so that when the button is pressed the appropriate icon is shown correctly
     *
     * (ms) 09-10-08 Added rotation parameter
     */
    //public Object setupChangingDownIcon(Object downOff, Object downOn, int rotation);

    /**
     * setup mouse actions to allow the text of the button to change with the captions provided
     * <br>
     * should change the caption as the moouse actions occure on the field
     */
    //public Object setupChangingCaption(String normalCaption, String rolloverCaption, String downCaption);

    /**
     * setup hand cursor when hovering and reset, on exiting
     * @return hand cursor
     */
    Object setHoverCursor();

    void init(PdfDecoderInt panel, Javascript javascript, AcroRenderer defaultAcroRenderer);
    
    //void setPageAccess(int pageHeight, int insetH);

	PdfLayerList getLayerHandler();

	void changeTo(String file, int page, Object location, Integer type, boolean storeView);


    //allow Swing client to plug into Dest handling code returning page as may change
    @SuppressWarnings("UnusedReturnValue")
    int gotoDest(PdfObject aobj, int mouseclicked, int dest);
}
