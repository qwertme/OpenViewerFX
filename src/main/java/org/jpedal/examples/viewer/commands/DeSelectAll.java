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
 * DeSelectAll.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;

/**
 * Deselects the text/image a user has highlighted in the Viewer
 */
public class DeSelectAll {

    public static void execute(final GUIFactory currentGUI, final PdfDecoderInt decode_pdf) {
        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
        	
            /**
             * remove any outline and reset variables used to track change
             */
            decode_pdf.getTextLines().clearHighlights(); //remove highlighted text
            decode_pdf.repaintPane(0);
            decode_pdf.getPages().setHighlightedImage(null);// remove image highlight
            decode_pdf.getPages().refreshDisplay();
        } else {
            currentGUI.showMessageDialog(Messages.getMessage("PageLayoutMessage.SinglePageOnly"));
        }
    }
}
