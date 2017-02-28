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
 * TextDocumentListener.java
 * ---------------
 */
package org.jpedal.objects.acroforms.creation;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.jpedal.objects.raw.FormObject;

/**
 * listener to ensure that if component directly accesed it synchronized with
 * our FormObject
 */
public class TextDocumentListener implements DocumentListener {

    final JTextComponent textcomp;
    final FormObject form;

    TextDocumentListener(final JTextComponent textcomp, final FormObject form) {
	this.textcomp = textcomp;
	this.form = form;
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
	updateFormValue();
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
	updateFormValue();
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
	updateFormValue();
    }

    /**
     * write value back to our Object from GUI widget
     */
    private void updateFormValue() {
	form.updateValue(textcomp.getText(), false, false);  //last false is critical to stop it looping
    }
}
