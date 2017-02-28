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
 * JavaFXComboListener.java
 * ---------------
 */


package org.jpedal.objects.acroforms.actions.JavaFX;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import org.jpedal.objects.raw.FormObject;


public class JavaFXComboListener implements ChangeListener<Number> {
     
    final ComboBox comp;
    final FormObject form;
    
     public JavaFXComboListener(final ComboBox comboBox, final FormObject form) {
         
	this.comp = comboBox;
	this.form = form;
    }

  
    @Override
    public void changed(final ObservableValue<? extends Number> ov, final Number t, final Number t1) {
        final int indexnew = comp.getSelectionModel().getSelectedIndex();
         form.setSelection(new Object[]{comp.getSelectionModel().getSelectedItem()},comp.getSelectionModel().getSelectedItem().toString(), new int[]{indexnew}, indexnew);
    }

}
