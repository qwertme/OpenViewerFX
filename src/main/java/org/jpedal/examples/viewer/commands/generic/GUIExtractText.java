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
 * GUIExtractText.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.generic;

import java.util.Iterator;
import java.util.Map;
import javax.swing.JOptionPane;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Messages;
import org.jpedal.utils.Strip;

/**
 * Class to Handle the popup dialogs created when user right clicks 
 * highlighted text and chooses text extraction.
 */
public class GUIExtractText {

    protected static String extractTextList(final PdfDecoderInt decode_pdf, final Values commonValues, final GUIFactory currentGUI, final boolean isXML, final int t_x1, final int t_x2, final int t_y1, final int t_y2) throws PdfException {

        //int page = commonValues.getCurrentHighlightedPage();
        //if(page==-1)
        //	page = commonValues.getCurrentPage();
        String extractedText = "";
        //always reset to use unaltered co-ords
        PdfGroupingAlgorithms.useUnrotatedCoords = true;

        //page data so we can choose portrait or landscape
        final PdfPageData pageData = decode_pdf.getPdfPageData();
        final int rotation = pageData.getRotation(commonValues.getCurrentPage());
        if (rotation != 0) {
            final int alterCoords = currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerRotatedCoords.message"),
                    Messages.getMessage("PdfViewerOutputFormat.message"),
                    JOptionPane.YES_NO_OPTION);

            if (alterCoords == 0) {
                PdfGroupingAlgorithms.useUnrotatedCoords = false;
            }
        }

        /**
         * get the text
         */
        final java.util.List words = decode_pdf.getGroupingObject().extractTextAsWordlist(
                t_x1,
                t_y1,
                t_x2,
                t_y2,
                commonValues.getCurrentPage(),
                true, null);

        if (words == null) {
            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoTextFound") + "\nx1:" + t_x1 + " y1:" + t_y1 + " x2:" + t_x2 + " y2:" + t_y2);
        }

        if (words != null) {
            //put words into list
            final StringBuilder textOutput = new StringBuilder();
            final Iterator wordIterator = words.iterator();

            while (wordIterator.hasNext()) {

                String currentWord = (String) wordIterator.next();

                /**
                 * remove the XML formatting if present - not needed for pure text
                 */
                if (!isXML) {
                    currentWord = Strip.convertToText(currentWord, decode_pdf.isXMLExtraction());
                }

                final int wx1 = (int) Float.parseFloat((String) wordIterator.next());
                final int wy1 = (int) Float.parseFloat((String) wordIterator.next());
                final int wx2 = (int) Float.parseFloat((String) wordIterator.next());
                final int wy2 = (int) Float.parseFloat((String) wordIterator.next());

                /**
                 * this could be inserting into a database instead
                 */
                textOutput.append(currentWord).append(',').append(wx1).append(',').append(wy1).append(',').append(wx2).append(',').append(wy2).append('\n');

            }
            if (textOutput.toString() != null) {
                extractedText = textOutput.toString();
            }
        }
        return extractedText;
    }

    protected static String extractTextTable(final Values commonValues, final PdfDecoderInt decode_pdf, final boolean isCSV, final int t_x1, final int t_x2, final int t_y1, final int t_y2) throws PdfException {

        //int page = commonValues.getCurrentHighlightedPage();
        //if(page==-1)
        //	page = commonValues.getCurrentPage();
        //rest to default in case text option selected
        final Map content;

        /**
         * find out if xml or text - as we need to turn xml off before
         * extraction. So we assume xml and strip out. This is obviously
         */
        if (!isCSV) {
            content = decode_pdf.getGroupingObject().extractTextAsTable(t_x1,
                    t_y1, t_x2, t_y2, commonValues.getCurrentPage(), true, false,
                    false, false, 0);
        } else {
            content = decode_pdf.getGroupingObject().extractTextAsTable(t_x1,
                    t_y1, t_x2, t_y2, commonValues.getCurrentPage(), false, true,
                    true, false, 1);
        }

        if (content.get("content") != null) {
            return (String) content.get("content");
        } else {
            return "";
        }
    }

    protected static String extractTextRectangle(final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI, final boolean isXML, final int t_x1, final int t_x2, final int t_y1, final int t_y2) throws PdfException {

        //int page = commonValues.getCurrentHighlightedPage();
        //if(page==-1)
        //	page = commonValues.getCurrentPage();
        /**
         * get the text
         */
        String extractedText = decode_pdf.getGroupingObject().extractTextInRectangle(
                t_x1, t_y1, t_x2, t_y2, commonValues.getCurrentPage(), false,
                true);

        /**
         * find out if xml or text - as we need to turn xml off before
         * extraction. So we assume xml and strip out. This is obviously
         */
        if (extractedText == null) {
            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoTextFound") + "\nx1:" + t_x1 + " y1:" + t_y1 + " x2:" + t_x2 + " y2:" + t_y2);
            return "";
        } else if (!isXML) {
            extractedText = Strip.stripXML(extractedText, decode_pdf.isXMLExtraction()).toString();
        }

        return extractedText;
    }

}
