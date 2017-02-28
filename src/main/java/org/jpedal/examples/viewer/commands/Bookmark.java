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
 * Bookmark.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.*;
import org.jpedal.gui.GUIFactory;

/**
 * Bookmark the current page in the Viewer
 */
public class Bookmark {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf) {
        //Only works if a bookmark is specified and the currentGUI is not null
        if (args.length >= 1 && currentGUI != null) {
            final String bookmark = (String) args[0];

            currentGUI.setBookmarks(true);

            final String page = currentGUI.getBookmark(bookmark);

            if (page != null) {
                final int p = Integer.parseInt(page);

                try {
                    decode_pdf.decodePage(p);
                    //
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
