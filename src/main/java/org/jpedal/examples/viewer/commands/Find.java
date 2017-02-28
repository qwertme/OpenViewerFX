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
 * Find.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.generic.GUICopy;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.grouping.SearchType;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;

/**
 * Find designated text and highlight it in the Viewer
 */
public class Find {

    
    
    public static void execute(final Object[] args, final Values commonValues, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final GUISearchWindow searchFrame) {
    	if (args == null){
    		if (commonValues.getSelectedFile() == null){
    			currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
    		}else{
    			if(!commonValues.isPDF()){
    				currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.ImageSearch"));
    			}else{
    				if(decode_pdf.getDisplayView() != Display.SINGLE_PAGE
    						&& decode_pdf.getDisplayView() != Display.CONTINUOUS
    						&& decode_pdf.getDisplayView() != Display.CONTINUOUS_FACING){
    					currentGUI.showMessageDialog(Messages.getMessage("PageLayoutMessage.SingleContfacingFacingPageOnly"));
    				}else{
    					if((!searchFrame.isSearchVisible())){
    						searchFrame.find(decode_pdf, commonValues);
    			    		searchFrame.grabFocusInInput();
    					}
    				}
    			}
    		}

            /**
             * Take highlighted text and add to search
             */
            if (decode_pdf.getTextLines().getHighlightedAreasAs2DArray(commonValues.getCurrentPage()) != null) {
                final String searchText = GUICopy.copySelectedText(decode_pdf, currentGUI, commonValues);
                searchFrame.setSearchText(searchText);
            }
        } else {
            final boolean multiTerms;
            final boolean singlePageSearch;
            final int searchType;
            final String value = (String) args[0];
            if (args.length > 1) {
                searchType = (Integer) args[1];
                multiTerms = (Boolean) args[2];
                singlePageSearch = (Boolean) args[3];
            } else {
                searchType = SearchType.DEFAULT;
                multiTerms = false;
                singlePageSearch = false;
            }
            if((searchType & SearchType.HIGHLIGHT_ALL_RESULTS) == SearchType.HIGHLIGHT_ALL_RESULTS){
                commonValues.setAllHighlightsShown(true);
            }

            searchFrame.findWithoutWindow(decode_pdf, commonValues, searchType,
                    multiTerms, singlePageSearch, value);

            //				while(searchFrame.isSearching()){
            //					System.out.println("Currently Searching");
            //				}
            //				System.out.println("Search has stopped");
        }

    }
}
