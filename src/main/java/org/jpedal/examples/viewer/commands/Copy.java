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
 * Copy.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;


import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.generic.GUICopy;
import org.jpedal.gui.GUIFactory;

/**
 * Copies the text a user has selected/highlighted.
 */
public class Copy extends GUICopy{

    public static void execute(final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final Values commonValues) {
        final String copyText = copySelectedText(decode_pdf, currentGUI, commonValues);
        final StringSelection ss = new StringSelection(copyText);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }

}
