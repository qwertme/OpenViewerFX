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
 * JSField.java
 * ---------------
 */
package org.jpedal.objects.javascript.jsobjects;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;


public class JSField {

	public FormObject target; //TODO: Sync form object with field object

	public String alignment = "left";
//	public String borderStyle = "solid";
//	public int buttonAlignX = 50;
//	public int buttonAlignY = 50;
//	public boolean buttonFitBounds = true;
//	public int buttonPosition = 0;
//	public int buttonScaleHow = 0;
//	public int buttonScaleWhen = 0;
//	public int calcOrderIndex = 0;
	public int charLimit = -1;
//	public boolean comb = false;
//	public boolean commitOnSelChange = false;
//	public int currentValueIndices; or array
//	public Object defaultStyle;
	public String defaultValue;
//	public boolean doNotScroll = false;
//	public boolean doNotSpellCheck = true;
//	public boolean delay = false;
//	public int display = 0;
	public JSDoc doc;
//	public boolean editable = false;
//	public String[] exportValues;
//	public boolean fileSelect = false;
//	public String[] fillColor;
	public boolean hidden;
//	public String highlight = "none";
//	public int lineWidth = 1;
//	public boolean multiline = false;
//	public boolean multipleSelection = false;
	public String name;
//	public int numItems = 0;
//	public int page;
//	public boolean password = false;
//	public boolean print = true;
//	public boolean radiosInUnison = true;
	public boolean readonly;
//	public float[] rect = new float[4];
	public boolean required;
//	public boolean richText = false;
//	public Object[] richValue;
//	public int rotation = 0;
//	public String[] strokeColor;
//	public String style = "circle";
//	public String submitName;
//	public String[] textColor;
//	public String textFont = "Times-Roman";
//	public float textSize;
//	public String type = "text";
//	public String userName;
	public Object value;
	public String valueAsString;

	public JSField() {

	}
	public JSField(final FormObject fObj) {
//		System.out.println("Called for: " + fObj);
		target = fObj;
		syncUp();
	}

	private void syncUp() {
		name = target.getTextStreamValue(PdfDictionary.T);
		value = target.getValue();
		valueAsString = target.getValue();
	}
	public void syncToGUI(final boolean isSelected) {
		target.updateValue(value, isSelected, true);
	}

//	public void browseForFileToSubmit() {
//	}
//
//	public String buttonGetCaption(int nFace) {
//		return "";
//	}
//
//	public String buttonGetCaption() {
//		return "";
//	}
//
//	public Object buttonGetIcon(int nFace) {
//		return null;
//	}
//	public Object buttonGetIcon() {
//		return null;
//	}
//	public int buttonImportIcon(String cPath, int nPage) {
//		return 0;
//	}
//	public int buttonImportIcon(String cPath) {
//		return 0;
//	}
//	public int buttonImportIcon(int nPage) {
//		return 0;
//	}
//	public int buttonImportIcon() {
//		return 0;
//	}
//	public void buttonSetCaption(String cCaption, int nFace) {
//	}
//	public void buttonSetCaption(String cCaption) {
//	}
//	public void buttonSetIcon(Object oIcon, int nFace) {
//	}
//	public void buttonSetIcon(Object oIcon) {
//	}
//	public void checkThisBox(int nWidget, boolean bCheckIt) {
//	}
//	public void checkThisBox(int nWidget) {
//	}
//	public void clearItems() {
//	}
//	public boolean defaultIsChecked(int nWidget, boolean bIsDefaultChecked) {
//		return false;
//	}
//	public boolean defaultIsChecked(int nWidget) {
//		return false;
//	}
//	public void deleteItemAt(int nIdx) {
//	}
//	public void deleteItemAt() {
//	}
//	public JSField[] getArray() {
//		return null;
//	}
//	public String getItemAt(int nIdx, boolean bExportValue) {
//		return null;
//	}
//	public String getItemAt(int nIdx) {
//		return null;
//	}
//	public Object getLock() {
//		return null;
//	}
//	public void insertItemAt(String cName, String cExport, int nIdx) {
//	}
//	public void insertItemAt(String cName, String cExport) {
//	}
//	public void insertItemAt(String cName) {
//	}
//	public boolean isBoxChecked(int nWidget) {
//		return false;
//	}
//	public boolean isDefaultChecked(int nWidget) {
//		return false;
//	}
//	public void setAction(String cTrigger, String cScript) {
//	}
//	public void setFocus() {
//	}
//	public void setItems(Object[] oArray) {
//	}
//	public void setLock(Object oLock) {
//	}
//	public Object signatureGetModifications() {
//	}
//	public Object signatureGetSeedValue() {
//		return null;
//	}
//	public Object signatureInfo(Object oSig) {
//		return null;
//	}
//	public Object signatureInfo() {
//		return null;
//	}
//	public void signatureSetSeedValue(Object oSigSeedValue) {
//	}
//	public void signatureSign(Object oSig, Object oInfo, String cDIPath, boolean bUi, String cLegalAttest) {
//	}
//	public void signatureSign(Object oSig, Object oInfo, String cDIPath, boolean bUi) {
//	}
//	public void signatureSign(Object oSig, Object oInfo, String cDIPath) {
//	}
//	public void signatureSign(Object oSig, Object oInfo) {
//	}
//	public void signatureSign(Object oSig) {
//	}
//	public void signatureValidate(Object oSig, boolean bUi) {
//	}
//	public void signatureValidate(Object oSig) {
//	}
//	public void signatureValidate() {
//	}



}
