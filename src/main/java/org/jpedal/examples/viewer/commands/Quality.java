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
 * Quality.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;

/**
 * Increases the quality by using Hi-Res images in the Viewer
 */
public class Quality {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf) {
        if (args == null) {
            if (!Values.isProcessing()) {
                boolean useHiresImage = true;
                if (((GUI)currentGUI).getSelectedComboIndex(Commands.QUALITY) == 0) {
                    useHiresImage = false;
                }

                if (commonValues.getSelectedFile() != null) {

                    // tell user page will be redrawn
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewerReparseWait.message"));

                    // reset flag and re-decode page
                    decode_pdf.useHiResScreenDisplay(useHiresImage);
                    commonValues.setUseHiresImage(useHiresImage);

                    try {
                        currentGUI.decodePage();
                    } catch (final Exception e1) {
                        System.err.println("Exception" + ' ' + e1 + "decoding page after image quality changes");
                        e1.printStackTrace();
                    }
                    // decode_pdf.updateUI();
                }
            }
        } else {

        }
    }
}
