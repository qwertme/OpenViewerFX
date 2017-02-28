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
 * GUISearchWindow.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.generic;

import java.util.Map;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;

/**abstract level of search window*/ 
public interface GUISearchWindow {
	
    int SEARCH_EXTERNAL_WINDOW = 0;
    int SEARCH_TABBED_PANE = 1;
    int SEARCH_MENU_BAR = 2;
    
	//Varible added to allow multiple search style to be implemented
	//int style = 0;

	void find(PdfDecoderInt decode_pdf, Values values);
	
	void findWithoutWindow(PdfDecoderInt decode_pdf, Values values, int searchType, boolean listOfTerms, boolean singlePageOnly, String searchValue);

	void grabFocusInInput();

	boolean isSearchVisible();
	
	void init(PdfDecoderInt dec, Values values);

	void removeSearchWindow(boolean justHide);
	
	void resetSearchWindow();
	
	GUISearchList getResults();

	GUISearchList getResults(int page);

	Map getTextRectangles();

	int getViewStyle();

	void setViewStyle(int i);

	//boolean isSearching();
	
	int getFirstPageWithResults();
	
	void setWholeWords(boolean wholeWords);
	
	void setCaseSensitive(boolean caseSensitive);
	
	void setMultiLine(boolean multiLine);
    
    //    public void setSearchHighlightsOnly(boolean highlightOnly);
        
    void setSearchText(String s);
	
    void setUpdateListDuringSearch(boolean updateListDuringSearch);
}
