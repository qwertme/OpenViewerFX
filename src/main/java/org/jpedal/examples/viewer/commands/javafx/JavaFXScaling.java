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
 * JavaFXScaling.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import org.jpedal.FileAccess;
import org.jpedal.PdfDecoderFX;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Values;
import org.jpedal.external.Options;
import org.jpedal.gui.GUIFactory;

/**
 * This class controls how the viewer content is scaled, It can be scaled by
 * either a set percentage or by width/height/page, The main scaling is
 * performed depending on the index variable value in the scaleAndRotate method
 * in either JavaFXGUI/SwingGUI.
 */
public class JavaFXScaling {

    public static void execute(final Object[] args, final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI) {
        
        if (args == null) {
            if (!Values.isProcessing() && commonValues.getSelectedFile() != null) {
                
                int mode=decode_pdf.getDisplayView();
                int alignment=decode_pdf.getPageAlignment();
                ((PdfDecoderFX)decode_pdf).getChildren().clear();
               // final int pageNumber=commonValues.getCurrentPage();
                currentGUI.scaleAndRotate();
                currentGUI.setDisplayView(mode,alignment);
                //if the mode is not single page then set display needs to be called twice 
                //this is a hack fix need to be fixed properly
                if(mode==Display.SINGLE_PAGE){
                    FileAccess  fa = (FileAccess)decode_pdf.getExternalHandler(Options.FileAccess);
                    fa.setLastPageDecoded(-1);
                }else{
                    currentGUI.setDisplayView(mode,alignment);
                }
//                currentGUI.scrollToPage(pageNumber);
                
                
            }
        } else {
            currentGUI.setScalingFromExternal((String) args[0]);
            currentGUI.scaleAndRotate();
            while (Values.isProcessing()) {
                // wait while we scale your document
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

}
