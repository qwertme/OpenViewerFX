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
 * PreviousResult.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.*;
import static org.jpedal.examples.viewer.Commands.FIRST_DOCUMENT_SEARCH_RESULT_NOW_SHOWN;
import static org.jpedal.examples.viewer.Commands.SEARCH_NOT_FOUND;
import static org.jpedal.examples.viewer.Commands.SEARCH_RETURNED_TO_START;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.generic.GUISearchList;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

/**
 *
 */
public class PreviousResult {

    public static Object execute(final Object[] args,  final Values commonValues, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final GUISearchWindow searchFrame) {
        
        Object status = null;
        
        GUISearchList results = null;
        
        if (args == null) {
            
            results = searchFrame.getResults(commonValues.getCurrentPage());
            
            int index = results.getSelectedIndex();

            if (index < 0) {
                index = 0;
                results.setSelectedIndex(index);
            }

            //				Object currPage = results.getTextPages().get(new Integer(index));
            final Object currPage = commonValues.getCurrentPage();

            if (index == 0
                    || results.getResultCount() == 0) {
                int currentPage = commonValues.getCurrentPage() - 1;
                if (currentPage < 1) {
                    currentPage = commonValues.getPageCount();
                }
                results = searchFrame.getResults(currentPage);
                while (results.getResultCount() < 1 && currentPage > 0
                        && searchFrame.getViewStyle() == GUISearchWindow.SEARCH_MENU_BAR) {
                    results = searchFrame.getResults(currentPage);
                    currentPage--;
                }

                if (results.getResultCount() < 1
                        && currentPage == 0) {
                    currentPage = commonValues.getPageCount();
                    results = searchFrame.getResults(currentPage);
                    status = SEARCH_RETURNED_TO_START;

                    while (results.getResultCount() < 1 && currentPage >= commonValues.getCurrentPage()
                            && searchFrame.getViewStyle() == GUISearchWindow.SEARCH_MENU_BAR) {
                        results = searchFrame.getResults(currentPage);
                        currentPage--;
                    }
                }
                index = results.getResultCount() - 1;
                if (results.getResultCount() < 1) {
                    status = SEARCH_NOT_FOUND;
                }
            } else {
                index--;
            }

            currentGUI.setResults(results);
            results.setSelectedIndex(index);

            if (!Values.isProcessing()) {//{if (!event.getValueIsAdjusting()) {

                final float scaling = currentGUI.getScaling();
                //int inset=currentGUI.getPDFDisplayInset();

                final int id = results.getSelectedIndex();

                if (!commonValues.getAllHighlightsShown()) {
                    decode_pdf.getTextLines().clearHighlights();
                }

                if (id != -1) {

                    final Integer key = id;
                    final Object newPage = results.getTextPages().get(key);

                    if (newPage != null) {
                        final int nextPage = (Integer) newPage;

                        //move to new page
                        if (commonValues.getCurrentPage() != nextPage) {
                            commonValues.setCurrentPage(nextPage);

                            currentGUI.resetStatusMessage(Messages.getMessage("PdfViewer.LoadingPage") + ' ' + commonValues.getCurrentPage());

                            /**
                             * reset as rotation may change!
                             */
                            decode_pdf.setPageParameters(scaling, commonValues.getCurrentPage());

                            //decode the page
                            currentGUI.decodePage();

                        }

                        while (Values.isProcessing()) {
                            //Ensure page has been processed else highlight may be incorrect
                            try {
                                Thread.sleep(5000);
                            } catch (Exception e) {
                                if (LogWriter.isOutput()) {
                                    LogWriter.writeLog("Attempting to set propeties values " + e);
                                }
                            }
                        }

                        if (((Integer) currPage != nextPage) && (commonValues.getAllHighlightsShown())){
                                final Vector_Rectangle_Int storageVector = new Vector_Rectangle_Int();

                                Integer kInteger;

                                //Integer allKeys = new Integer(id);
                                for (int k = 0; k != results.getResultCount(); k++) {

                                    kInteger = k;
                                    //int currentPage = ((Integer)newPage).intValue();
                                    if ((Integer) results.getTextPages().get(kInteger) == nextPage) {

                                        final Object h = searchFrame.getTextRectangles().get(kInteger);

                                        if (h instanceof int[]) {
                                            storageVector.addElement((int[]) h);
                                        }
                                        if (h instanceof int[][]) {
                                            final int[][] areas = (int[][]) h;
                                            for (int i = 0; i != areas.length; i++) {
                                                storageVector.addElement(areas[i]);
                                            }
                                        }
                                    }
                                }

                                storageVector.trim();
                                final int[][] finalHighlight;
                                finalHighlight = storageVector.get();
                                decode_pdf.getTextLines().addHighlights(finalHighlight, true, nextPage);
                        }
                        
                        if (!commonValues.getAllHighlightsShown()) {
                            final Object highlight = results.textAreas().get(key);
                            if (highlight instanceof int[]) {
                                decode_pdf.getTextLines().addHighlights(new int[][]{(int[]) highlight}, true, nextPage);
                            } else {
                                decode_pdf.getTextLines().addHighlights((int[][]) highlight, true, nextPage);
                            }

                        }

                        decode_pdf.getPages().refreshDisplay();

                        decode_pdf.repaintPane(commonValues.getCurrentPage());

                    }
                }
            }
            currentGUI.getButtons().hideRedundentNavButtons(currentGUI);
        }

        if (commonValues.getCurrentPage() == searchFrame.getFirstPageWithResults()
                && results.getSelectedIndex() == 0) {
            status = FIRST_DOCUMENT_SEARCH_RESULT_NOW_SHOWN;
        }
        
        return status;
        
    }
}
