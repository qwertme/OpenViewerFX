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
 * NavigateDocuments.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;

/**
 * This class Opens up a previously opened document, you can open either a
 * previous document or the next document in the Viewer.
 */
public class NavigateDocuments {
    
    public static void executePrevDoc(final Object[] args, final GUIFactory currentGUI, final Values commonValues,
            final GUISearchWindow searchFrame, final PdfDecoderInt decode_pdf,
            final PropertiesFile properties, final GUIThumbnailPanel thumbnails) {
        if (args == null) {
            //
            if (Values.isProcessing()) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
            } else {
                final String fileToOpen = currentGUI.getRecentDocument().getPreviousDocument();
                currentGUI.openFile(fileToOpen);                   
            }
        }
    }

    public static void executeNextDoc(final Object[] args, final GUIFactory currentGUI, final Values commonValues,
            final GUISearchWindow searchFrame, final PdfDecoderInt decode_pdf,
            final PropertiesFile properties, final GUIThumbnailPanel thumbnails) {
        if (args == null) {

            //
            if (Values.isProcessing()) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
            } else {
                final String fileToOpen = currentGUI.getRecentDocument().getNextDocument();
                currentGUI.openFile(fileToOpen);                 
            }
        }
    }  
}
