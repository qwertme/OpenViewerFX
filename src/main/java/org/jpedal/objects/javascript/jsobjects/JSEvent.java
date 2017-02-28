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
 * JSEvent.java
 * ---------------
 */
package org.jpedal.objects.javascript.jsobjects;

import org.jpedal.objects.raw.PdfDictionary;


public class JSEvent {

	JSEvent() {

	}

	JSEvent(final int type) {
		setNameAndType(type);
	}


	JSEvent(final int type, final Object src, final Object target) {
		setNameAndType(type);
		this.source = src;
		this.target = target;
	}

	/**
	 * Sets the name and type based on the named type from PdfDictionary
	 * @param type
	 */
	private void setNameAndType(final int type) {
		switch (type) {
			// Page Events
			case PdfDictionary.O :
				this.name = "Open";
				this.type = "Page";
				break;
			case PdfDictionary.C :
				this.name = "Close";
				this.type = "Page";
				break;
			// Field Events
			case PdfDictionary.K :
				this.name = "Keystroke";
				this.type = "Field";
				break;
			case PdfDictionary.V :
				this.name = "Validate";
				this.type = "Field";
				break;
			case PdfDictionary.C2 :
				this.name = "Calculate";
				this.type = "Field";
				break;
			case PdfDictionary.F :
				this.name = "Format";
				this.type = "Field";
				break;
//			default:
//				throw new RuntimeException("Unhandled type used for JavaScript event object: " + PdfDictionary.showAsConstant(type) + " (" + type + ")");
		}
	}

	/**
	 * A string specifying the change in value that the user has just typed.
	 * A JavaScript may replace part or all of this string with different characters.
	 * The change may take the form of an individual keystroke or a string of characters
	 * (for example, if a paste into the field is performed).
	 */
	public String change;

	/**
	 * Contains the export value of the change and is available only during a Field/Keystroke event for list boxes and combo boxes.
	 */
	public Object changeEx;

	/**
	 * Determines how a form field will lose focus. Values are:
	 0 - Value was not committed (for example, escape key was pressed).
	 1 - Value was committed because of a click outside the field using the mouse.
	 2 - Value was committed because of pressing the Enter key.
	 3 - Value was committed by tabbing to a new field.
	 */
	public int commitKey;

	/**
	 * The name of the current event as a text string. The type and name together uniquely identify the event. Valid names are:
	 Keystroke	Mouse Exit
	 Validate	WillPrint
	 Focus	DidPrint
	 Blur	WillSave
	 Format	DidSave
	 Calculate	Init
	 MouseUp	Exec
	 MouseDown	Open
	 MouseEnter	Close
	 */
	public String name;

	/**
	 * Used for validation.
	 * Indicates whether a particular event in the event chain should succeed.
	 * Set to false to prevent a change from occurring or a value from committing.
	 * The default is true.
	 */
	public boolean rc = true;

	/**
	 * Specifies the change in value that the user has just typed.
	 * The richChange property is only defined for rich text fields and mirrors the behavior of the event.change property.
	 * The value of richChange is an array of Span objects that specify both the text entered into the field and the formatting.
	 * Keystrokes are represented as single member arrays, while rich text pasted into a field is represented as an array of arbitrary length.
	 */
	public Object[] richChange;

	/**
	 * This property is only defined for rich text fields.
	 * It mirrors the behavior of the event.changeEx property for text fields.
	 * Its value is an array of Span objects that specify both the text entered into the field and the formatting.
	 * Keystrokes are represented as single member arrays, while rich text pasted into a field is represented as an array of arbitrary length.
	 */
	public Object[] richChangeEx;

	/**
	 * This property mirrors the richValue property of the Field object and the event.value property for each event.
	 */
	public Object[] richValue;

	//... see JavaScript For Acrobat API Reference for all properties

	/**
	 * The Field object that triggered the calculation event.
	 * This object is usually different from the target of the event, which is the field that is being calculated.
	 */
	public Object source;

	/**
	 * The target object that triggered the event.
	 * In all mouse, focus, blur, calculate, validate, and format events, it is the Field object that triggered the event.
	 * In other events, such as page open and close, it is the Doc or this object.
	 */
	public Object target;

	/**
	 * Tries to return the name of the JavaScript being executed.
	 * Can be used for debugging purposes to help identify the code causing exceptions to be thrown.
	 *
	 * Common values of targetName include:
	 * 	The folder-level script file name for App/Init events
	 *	The document-level script name forDoc/Open events
	 *	The PDF file name being processed for Batch/Exec events
	 *	The field name for Field events
	 *	The menu item name for Menu/Exec events
	 *	The screen annotation name for Screen events (multimedia events)
	 *When an exception is thrown, targetName is reported if there is an identifiable name.
	 */
	public String targetName;

	/**
	 * The type of the current event. The type and name together uniquely identify the event.
	 * Valid types are:
	 * Batch		External
	 * Console		Bookmark
	 * App			Link
	 * Doc			Field
	 * Page			Menu
	 */
	public String type;

	/**
	 * This property has different meanings for different field events...
	 * See page 384 of the JavaScript for Acrobat API Reference for all the details.
	 */
	public Object value;

	public boolean willCommit;
}
