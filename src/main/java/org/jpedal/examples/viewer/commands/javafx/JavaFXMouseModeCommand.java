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
 * JavaFXMouseModeCommand.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import java.net.URL;
import javafx.scene.Cursor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jpedal.PdfDecoderFX;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.MouseMode;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXMessageDialog;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;

/**
 * This Class Toggles Between Text Selection and Pan Mode for the ToolBar Toggle
 * Button.
 */
public class JavaFXMouseModeCommand {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final MouseMode mouseMode, final PdfDecoderInt decode_pdf) {
        if (args == null) {
            if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
                if (mouseMode.getMouseMode() == MouseMode.MOUSE_MODE_TEXT_SELECT) {

                    currentGUI.getMenuItems().setCheckMenuItemSelected(Commands.PANMODE, true);
                    currentGUI.getMenuItems().setCheckMenuItemSelected(Commands.TEXTSELECT, false);
                    currentGUI.setPannable(true);
                    //Set Mouse Mode
                    mouseMode.setMouseMode(MouseMode.MOUSE_MODE_PANNING);

                    //Update Buttons
                    final URL url = currentGUI.getGUICursor().getURLForImage("mouse_pan.png");
                    if (url != null) {
                        currentGUI.getButtons().getButton(Commands.MOUSEMODE).setIcon(url);
                    }
                    currentGUI.getButtons().getButton(Commands.SNAPSHOT).setEnabled(false);

                    //Update Cursor
                    ((PdfDecoderFX) decode_pdf).setDefaultCursor(Cursor.MOVE);

                } else if (mouseMode.getMouseMode() == MouseMode.MOUSE_MODE_PANNING) {

                    currentGUI.getMenuItems().setCheckMenuItemSelected(Commands.TEXTSELECT, true);
                    currentGUI.getMenuItems().setCheckMenuItemSelected(Commands.PANMODE, false);
                    currentGUI.setPannable(false);
                    
                    //Set Mouse Mode
                    mouseMode.setMouseMode(MouseMode.MOUSE_MODE_TEXT_SELECT);

                    //Update buttons and mouse cursor
                    //decode_pdf.setPDFCursor(Cursor.getDefaultCursor());
                    final URL url = currentGUI.getGUICursor().getURLForImage("mouse_select.png");
                    if (url != null) {
                        currentGUI.getButtons().getButton(Commands.MOUSEMODE).setIcon(url);
                    }
                    if(GUI.debugFX){
                        currentGUI.getButtons().getButton(Commands.SNAPSHOT).setEnabled(true);
                    }
                    //Update Cursor
                    ((PdfDecoderFX) decode_pdf).setDefaultCursor(Cursor.TEXT);
                }
            } else {
                final FXMessageDialog newMessage = new FXMessageDialog((Stage)currentGUI.getFrame(), Modality.APPLICATION_MODAL, Messages.getMessage("PdfCustomGui.textSelectionUnavailible"));
                newMessage.show();
            }
        } else {

        }
    }

}
