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
 * Highlight.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.*;

/**
 * This class allows you to Highlight a selected text/image 
 * using Rectangles by getting the text lines.
 */
public class Highlight {

    public static void execute(final Object[] args, final PdfDecoderInt decode_pdf) {
        
        decode_pdf.getTextLines().clearHighlights();

        if (args != null) {

            final int[][] highlights = (int[][]) args[0];
            final int page = (Integer) args[1];
            boolean areaSelect = true;
            if (args.length > 2) {
                areaSelect = (Boolean) args[2];
            }
            //decode_pdf.getTextLines().clearHighlights();

            //add text highlight
            decode_pdf.getTextLines().addHighlights(highlights, areaSelect, page);

            //highlights[0].x=1;
            //                decode_pdf.scrollRectToHighlight(highlights[0]);
            //
        }
    }
}
