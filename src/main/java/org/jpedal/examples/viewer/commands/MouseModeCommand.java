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
 * MouseModeCommand.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Cursor;
import java.net.URL;
import javax.swing.JOptionPane;
import org.jpedal.*;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.MouseMode;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;

/**
 * Sets the selection mouse to MouseMode
 */
public class MouseModeCommand {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final MouseMode mouseMode, final PdfDecoderInt decode_pdf) {
        if (args == null) {
        	if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE){
        		if (mouseMode.getMouseMode() == MouseMode.MOUSE_MODE_TEXT_SELECT) {

        			//Set Mouse Mode
        			mouseMode.setMouseMode(MouseMode.MOUSE_MODE_PANNING);

        			//Update Buttons
        			final URL url = currentGUI.getGUICursor().getURLForImage("mouse_pan.png");
        			if (url != null) {
        				currentGUI.getButtons().getButton(Commands.MOUSEMODE).setIcon(url);
        			}
        			currentGUI.getButtons().getButton(Commands.SNAPSHOT).setEnabled(false);

        			//Update Cursor
                    //

        		} else if (mouseMode.getMouseMode() == MouseMode.MOUSE_MODE_PANNING) {

        			//Set Mouse Mode
        			mouseMode.setMouseMode(MouseMode.MOUSE_MODE_TEXT_SELECT);

        			//Update buttons and mouse cursor
        			//decode_pdf.setPDFCursor(Cursor.getDefaultCursor());
        			final URL url = currentGUI.getGUICursor().getURLForImage("mouse_select.png");
        			if (url != null) {
        				currentGUI.getButtons().getButton(Commands.MOUSEMODE).setIcon(url);
        			}

        			currentGUI.getButtons().getButton(Commands.SNAPSHOT).setEnabled(true);

        			//Update Cursor
                    //
        		}
        	}else{
                currentGUI.showMessageDialog(Messages.getMessage("PdfCustomGui.textSelectionUnavailible"), Messages.getMessage("PdfCustomGui.textSelectionUnavailibleTitle"), JOptionPane.INFORMATION_MESSAGE);
            }
        }else {

        }
    }
}
