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
 * MouseSelector.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui;

import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Values;
import org.jpedal.objects.PdfPageData;

/**
 *
 */
public class MouseSelector {

    /**
     * adjusty x co-ordinate shown in display for user to include any page
     * centering
     */
    protected static int adjustForAlignment(int cx, final PdfDecoderInt decode_pdf) {

        if (decode_pdf.getPageAlignment() == Display.DISPLAY_CENTERED) {
            final int width =  decode_pdf.getPaneBounds()[0];
            int pdfWidth = decode_pdf.getPDFWidth();

            if (decode_pdf.getDisplayView() != Display.SINGLE_PAGE) {
                pdfWidth = decode_pdf.getMaxSizeWH()[0];
            }

            if (width > pdfWidth) {
                cx -= ((width - pdfWidth) / (2));
            }
        }

        return cx;
    }

    /**
     * get raw co-ords and convert to correct scaled units
     *
     * @return int[] of size 2, [0]=new x value, [1] = new y value
     */
    protected static int[] updateXY(final int originalX, final int originalY, final PdfDecoderInt decode_pdf, final Values commonValues) {

        final PdfPageData page_data = decode_pdf.getPdfPageData();

        final int mediaW = page_data.getMediaBoxWidth(commonValues.getCurrentPage());
        final int mediaH = page_data.getMediaBoxHeight(commonValues.getCurrentPage());
        final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
        final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
        final int cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
        final int cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());
        final float scaling = page_data.getScalingValue();
        final int rotation = page_data.getRotation(commonValues.getCurrentPage());

        //float scaling=currentGUI.getScaling();
        final int inset = GUI.getPDFDisplayInset();
        //int rotation=currentGUI.getRotation();

        //get co-ordinates of top point of outine rectangle
        int x = (int) (((adjustForAlignment(originalX, decode_pdf)) - inset) / scaling);
        int y = (int) ((originalY - inset) / scaling);

        //undo any viewport scaling
        if (commonValues.maxViewY != 0) { // will not be zero if viewport in play
            x = (int) (((x - (commonValues.dx * scaling)) / commonValues.viewportScale));
            y = (int) ((mediaH - ((mediaH - (y / scaling) - commonValues.dy) / commonValues.viewportScale)) * scaling);
        }

        final int[] ret = new int[2];
        if (rotation == 90) {
            ret[1] = x + cropY;
            ret[0] = y + cropX;
        } else if ((rotation == 180)) {
            ret[0] = mediaW - (x + mediaW - cropW - cropX);
            ret[1] = y + cropY;
        } else if ((rotation == 270)) {
            ret[1] = mediaH - (x + mediaH - cropH - cropY);
            ret[0] = mediaW - (y + mediaW - cropW - cropX);
        } else {
            ret[0] = x + cropX;
            ret[1] = mediaH - (y + mediaH - cropH - cropY);
        }
        return ret;
    }
}
