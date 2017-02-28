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
 * JavaFXSupport.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import org.jpedal.fonts.tt.FontFile2;
import org.jpedal.fonts.tt.Glyf;
import org.jpedal.fonts.tt.Hmtx;
import org.jpedal.fonts.tt.hinting.TTVM;
import org.jpedal.objects.PdfClip;
import org.jpedal.objects.PdfShape;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.acroforms.creation.GenericFormFactory;

/**
 *
 * @author markee
 */
public class JavaFXSupport {

    public PdfGlyph getGlyph(Glyf currentGlyf, FontFile2 fontTable, Hmtx currentHmtx, int idx, float f, TTVM vm, String baseFontName) {
        throw new UnsupportedOperationException("getGlyph Not supported yet."); 
    }

    public PdfGlyph getGlyph(float[] x, float[] y, float[] x2, float[] y2, float[] x3, float[] y3, float ymin, int end, int[] commands) {
       throw new UnsupportedOperationException("getGlyph Not supported yet."); 
    }

    public Object getCommandHandler(Object currentCommands) {
        throw new UnsupportedOperationException("getCommandHandler Not supported yet.");
    }

    public PdfShape getFXShape() {
        throw new UnsupportedOperationException("getFXShape Not supported yet.");
    }

    public PdfClip getFXClip() {
        throw new UnsupportedOperationException("getFXClipNot supported yet."); 
    }

    public void renderGUIComponent(final int formType, final Object value, final Object guiComp, final int guiType) {

        if (Platform.isFxApplicationThread()) {
            setGUI(formType, value, guiComp, guiType);
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setGUI(formType, value, guiComp, guiType);
                }
            });
        }
    }

    private static void setGUI(final int formType, final Object value, final Object guiComp, final int guiType) {

        if (GenericFormFactory.isTextForm(formType)) {
            ((TextInputControl) guiComp).setText((String) value);
        } else if (formType == FormFactory.checkboxbutton) {
            ((ToggleButton) guiComp).setSelected(Boolean.valueOf((String) value));
        } else if (GenericFormFactory.isButtonForm(formType)) {
            ((ToggleButton) guiComp).setText((String) value);
            ((ToggleButton) guiComp).setSelected(Boolean.valueOf((String) value));
        } else if (formType == FormFactory.annotation
                && guiComp instanceof ToggleButton) {
            ((ToggleButton) guiComp).setSelected(Boolean.valueOf((String) value));
        }
    }

    public static void setVisible(Object guiComp, boolean isVisible) {
        ((Node) guiComp).setVisible(isVisible);
    }

    public static void select(Object guiComp, String selectedItem, int formType) {
        if (formType == FormFactory.combobox) {
            ((ComboBox) guiComp).getSelectionModel().select(selectedItem);
        } else {
            ((ListView) guiComp).getSelectionModel().select(selectedItem);
        }
    }

    public static String getSelectedItem(Object guiComp, int formType) {
        if (formType == FormFactory.combobox) {
            return (String) ((ComboBox) guiComp).getSelectionModel().getSelectedItem();
        } else {
            return (String) ((ListView) guiComp).getSelectionModel().getSelectedItem();
        }
    }

}
