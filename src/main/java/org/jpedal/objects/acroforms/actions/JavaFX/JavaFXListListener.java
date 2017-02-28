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
 * JavaFXListListener.java
 * ---------------
 */


package org.jpedal.objects.acroforms.actions.JavaFX;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;
import org.jpedal.objects.raw.FormObject;

public class JavaFXListListener implements ChangeListener<Number>{
    final ListView comp;
    final FormObject form;
    
    public JavaFXListListener(final ListView list, final FormObject form){
        this.comp = list;
        this.form = form;
    }

    @Override
    public void changed(final ObservableValue<? extends Number> ov, final Number t, final Number t1) {
        final int selectionIndex = comp.getSelectionModel().getSelectedIndex();
        form.setSelection(comp.getSelectionModel().getSelectedItems().toArray(),comp.getSelectionModel().getSelectedItem().toString(),new int[]{selectionIndex},selectionIndex);   
    }
    
}
