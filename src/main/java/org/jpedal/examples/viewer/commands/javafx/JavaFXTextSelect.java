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
 * JavaFXTextSelect.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import java.net.URL;
import javafx.scene.Cursor;
import org.jpedal.PdfDecoderFX;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.MouseMode;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.gui.GUIFactory;

/**
 * This Class Enables TextSelectionMode when activated via the top drop-down menu, When
 * TextSelectionMode is enabled, PanMode functionality is disabled.
 */
public class JavaFXTextSelect {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final MouseMode mouseMode, final PdfDecoderInt decode_pdf) {
        if (args == null) {

            //Disable Pan Mode
            currentGUI.getMenuItems().setCheckMenuItemSelected(Commands.PANMODE, false);
            currentGUI.getMenuItems().setCheckMenuItemSelected(Commands.TEXTSELECT, true);
            if(GUI.debugFX){
                currentGUI.getButtons().getButton(Commands.SNAPSHOT).setEnabled(true);
            }
            currentGUI.setPannable(false);

            //Set mouse mode
            mouseMode.setMouseMode(MouseMode.MOUSE_MODE_TEXT_SELECT);

            //Update buttons
            final URL url = currentGUI.getGUICursor().getURLForImage("mouse_select.png");
            if (url != null) {
                currentGUI.getButtons().getButton(Commands.MOUSEMODE).setIcon(url);
            }

            ((PdfDecoderFX) decode_pdf).setDefaultCursor(Cursor.TEXT);
        } else {

        }
    }

}
