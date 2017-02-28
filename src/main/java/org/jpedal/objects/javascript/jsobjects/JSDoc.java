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
 * JSDoc.java
 * ---------------
 */
package org.jpedal.objects.javascript.jsobjects;

import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.acroforms.ReturnValues;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;

import java.util.HashMap;
import java.util.List;


public class JSDoc {
//	public boolean external = true;
	private AcroRenderer acroRenderer;
	private HashMap<String, JSField> nameTofields = new HashMap<String, JSField>();
	private HashMap<String, JSField> refTofields = new HashMap<String, JSField>();

	//public Object ADBE = null;

	public JSDoc() {}
    
	public void setAcroRenderer(final AcroRenderer acro) {
		acroRenderer = acro;
		if(acro != null) {
			loadFormObjects();
		}
	}

	// TODO: add call to loadFormObjects() to GenericParser in a way that its called after the acrorender/GUIData has loaded the FormObjects
	public void loadFormObjects() {
		if(acroRenderer == null) {
			throw new RuntimeException("No acrorender object set for Doc object.");
		}
		final List<FormObject> obs = acroRenderer.getCompData().getFormComponents(null, ReturnValues.FORMOBJECTS_FROM_REF, -1);
//		System.out.println("obs=" + obs);
//		System.out.println("size=" + obs.size());
		for(final FormObject formObject : obs) {
			final JSField field = new JSField(formObject);
			refTofields.put(formObject.getObjectRefAsString(), field);
			nameTofields.put(formObject.getTextStreamValue(PdfDictionary.T), field);
		}
	}

	public JSField getField(final String name) {
//		System.out.println("Called getField(" + name + ");");

		// check if there are field objects in the map
		if(nameTofields.size() <= 0) {
			loadFormObjects();
		}
		if(nameTofields.containsKey(name)) {
			return nameTofields.get(name);
		}
		return null;
	}

	public JSField getFieldByRef(final String ref) {
//		System.out.println("Called getFieldByRef(" + ref + ");");
		// check if there are field objects in the map
		if(refTofields.size() <= 0) {
			loadFormObjects();
		}
		if(refTofields.containsKey(ref)) {
			return refTofields.get(ref);
		}
		return null;
	}

	public void flush() {
		nameTofields = new HashMap<String, JSField>();
		refTofields = new HashMap<String, JSField>();
	}

	public FormObject[] getFormObjects() {
		final FormObject[] formObjects = new FormObject[refTofields.size()];
		int i = 0;
		for(final String S : refTofields.keySet()) {
			formObjects[i] = refTofields.get(S).target;
			i ++;
		}
		return formObjects;
	}

}
