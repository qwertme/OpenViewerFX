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
 * Scroll.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Rectangle;
import org.jpedal.*;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Values;
import org.jpedal.objects.PdfPageData;

/**
 * Allows the user to scroll through the PDF document at a scroll interval
 * scroll interval is default of 10 unless changed.
 */
public class Scroll {

    public static void execute(final Object[] args, final Values commonValues, final PdfDecoderInt decode_pdf) {
        if (args == null) {

        } else {
            final int[] scrollTo = (int[]) args[0];
            int page = commonValues.getCurrentPage();
            if (args.length > 1 && args[1] != null) {
                page = (Integer) args[1];
            }

            if (scrollTo != null) {
                rectToHighlight(scrollTo, page, decode_pdf);
                //
            }
        }

    }

    public static void rectToHighlight(final int[] highlight, int page, final PdfDecoderInt decode_pdf) {
        int x = 0, y = 0, w = 0, h = 0;
        final int insetW = decode_pdf.getInsetW();
        final int insetH = decode_pdf.getInsetH();
        final float scaling = decode_pdf.getScaling();
        final int scrollInterval = decode_pdf.getScrollInterval();

        final int displayView = decode_pdf.getDisplayView();
        if (page < 1 || page > decode_pdf.getPageCount() || displayView == Display.SINGLE_PAGE) {
            page = decode_pdf.getPageNumber();
        }

        final PdfPageData pageData = decode_pdf.getPdfPageData();
        final int cropW = pageData.getCropBoxWidth(page);
        final int cropH = pageData.getCropBoxHeight(page);
        final int cropX = pageData.getCropBoxX(page);
        final int cropY = pageData.getCropBoxY(page);

        switch (decode_pdf.getDisplayRotation()) {
            case 0:
                x = (int) ((highlight[0] - cropX) * scaling) + insetW;
                y = (int) ((cropH - (highlight[1] - cropY)) * scaling) + insetH;
                w = (int) (highlight[2] * scaling);
                h = (int) (highlight[3] * scaling);

                break;
            case 90:
                x = (int) ((highlight[1] - cropY) * scaling) + insetH;
                y = (int) ((highlight[0] - cropX) * scaling) + insetW;
                w = (int) (highlight[3] * scaling);
                h = (int) (highlight[2] * scaling);

                break;
            case 180:
                x = (int) ((cropW - (highlight[0] - cropX)) * scaling) + insetW;
                y = (int) ((highlight[1] - cropY) * scaling) + insetH;
                w = (int) (highlight[2] * scaling);
                h = (int) (highlight[3] * scaling);

                break;
            case 270:
                x = (int) ((cropH - (highlight[1] - cropY)) * scaling) + insetH;
                y = (int) ((cropW - (highlight[0] - cropX)) * scaling) + insetW;
                w = (int) (highlight[3] * scaling);
                h = (int) (highlight[2] * scaling);

                break;
        }

        if (displayView != Display.SINGLE_PAGE && displayView != Display.PAGEFLOW) {
            x += decode_pdf.getPages().getXCordForPage(page);
            y += decode_pdf.getPages().getYCordForPage(page);
        }

        //
    }
}
