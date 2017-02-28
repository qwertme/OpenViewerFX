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
 * GUICopy.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.generic;

import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Messages;
import org.jpedal.utils.Strip;

/**
 * Class to hold generic code for JavaFXCopy.java and Copy.java.
 */
public class GUICopy {

    /**
     * routine to link GUI into text extraction functions
     * @param decode_pdf Displayed PDF file
     * @param currentGUI The Viewer
     * @param commonValues Stores data including Page Number
     * @return Extracted text
     */
    public static String copySelectedText(final PdfDecoderInt decode_pdf, final GUIFactory currentGUI, final Values commonValues) {

        if (!decode_pdf.isExtractionAllowed()) {
            currentGUI.showMessageDialog("Not allowed");
            return "";
        }

        StringBuilder returnValue = new StringBuilder("");

        final int[][] highlights = decode_pdf.getTextLines().getHighlightedAreasAs2DArray(commonValues.getCurrentPage());

        if (highlights == null) {
            return "";
        }
        final boolean multipleAreas = (highlights.length > 1);

        final PdfPageData page_data = decode_pdf.getPdfPageData();
        final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
        final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
        final int cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
        final int cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());

        if (highlights != null) {
            for (int t = 0; t != highlights.length; t++) {
                /**
                 * ensure co-ords in right order
                 */

                highlights[t] = adjustHighlightForExtraction(highlights[t]);

                int t_x1 = highlights[t][0];
                int t_x2 = highlights[t][0] + highlights[t][2];
                int t_y1 = highlights[t][1] + highlights[t][3];
                int t_y2 = highlights[t][1];

                if (t_y1 < t_y2) {
                    final int temp = t_y2;
                    t_y2 = t_y1;
                    t_y1 = temp;
                }

                if (t_x1 > t_x2) {
                    final int temp = t_x2;
                    t_x2 = t_x1;
                    t_x1 = temp;
                }

                if (t_x1 < cropX) {
                    t_x1 = cropX;
                }
                if (t_x1 > cropW + cropX) {
                    t_x1 = cropW + cropX;
                }

                if (t_x2 < cropX) {
                    t_x2 = cropX;
                }
                if (t_x2 > cropW + cropX) {
                    t_x2 = cropW + cropX;
                }

                if (t_y1 < cropY) {
                    t_y1 = cropY;
                }
                if (t_y1 > cropH + cropY) {
                    t_y1 = cropH + cropY;
                }

                if (t_y2 < cropY) {
                    t_y2 = cropY;
                }
                if (t_y2 > cropH + cropY) {
                    t_y2 = cropH + cropY;
                }

                String extractedText;

                try {
                    /**
                     * create a grouping object to apply grouping to data
                     */
                    final PdfGroupingAlgorithms currentGrouping = decode_pdf.getGroupingObject();

                    /**
                     * get the text
                     */
                    extractedText = currentGrouping.extractTextInRectangle(
                            t_x1, t_y1, t_x2, t_y2, commonValues.getCurrentPage(), false,
                            true);

                    /**
                     * find out if xml or text - as we need to turn xml off
                     * before extraction. So we assume xml and strip out. This
                     * is obviously
                     */
                    if (extractedText == null || extractedText.isEmpty()) {
                        if (!multipleAreas) {
                            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoTextFound"));
                        }
                    } else {
                        extractedText = Strip.stripXML(extractedText, decode_pdf.isXMLExtraction()).toString();
                    }

                    if (extractedText != null) //Add extractedText & newline to returnValue
                    {
                        returnValue.append(extractedText).append((char) 0x0D).append((char) 0x0A);
                    }

                } catch (final PdfException e) {
                    System.err.println("Exception " + e.getMessage()
                            + " in file " + commonValues.getSelectedFile());
                    e.printStackTrace();
                }
            }
        }
        if (returnValue.length() > 2) {
            return returnValue.substring(0, returnValue.length() - 2);
        } else {
            return "";
        }

    }

    /**
     * Increase size of highlight area to ensure it fully encompasses the text
     * to be extracted
     *
     * @param highlight : original highlight area
     * @return updated highlgiht area
     */
    public static int[] adjustHighlightForExtraction(final int[] highlight) {

        final int x = highlight[0] - 1;
        final int y = highlight[1] - 3;
        final int width = highlight[2] + 2;
        final int height = highlight[3] + 6;
        return new int[]{x, y, width, height};
    }

}
