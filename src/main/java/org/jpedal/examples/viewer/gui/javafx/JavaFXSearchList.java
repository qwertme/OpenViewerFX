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
 * JavaFXSearchList.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.javafx;

import java.util.Collections;
import java.util.Map;

import javafx.collections.ObservableList;

import org.jpedal.examples.viewer.gui.generic.GUISearchList;


/**used by search function ro provide page number as tooltip*/
public class JavaFXSearchList extends javafx.scene.control.ListView implements GUISearchList {

	private final Map textPages;
	private final Map textAreas;
//	private String pageStr="Page";
	//private int Length = 0;
    
    int status = NO_RESULTS_FOUND;
    
	private String searchTerm = "";

	/**
	 * Constructor that will set up the search list and store highlight areas
	 * internally so the search highlights can be manipulated externally.
	 * @param listModel :: List of teasers
	 * @param textPages :: Map of key to page of result
	 * @param textAreas :: Map of key to highlight area
	 */
	public JavaFXSearchList(final ObservableList listModel, final Map textPages, final Map textAreas) {
		super(listModel);
		
		//Length = listModel.capacity();
		
		this.textPages=textPages;
		this.textAreas=textAreas;
//		pageStr=Messages.getMessage("PdfViewerSearch.Page")+ ' ';
	}

//	@Override
//    public String getToolTipText(MouseEvent event){
//	
//		int index=this.locationToIndex(event.getPoint());
//		
//		Object page=textPages.get(index);
//		
//		if(page!=null)
//			return pageStr+page;
//		else
//			return null;
//	}

	@Override
    public Map getTextPages() {
		return Collections.unmodifiableMap(textPages);
	}

	@Override
    public Map textAreas() {
		return Collections.unmodifiableMap(textAreas);
	}
	
	/**
	 * Find out the current amount of results found
	 * @return the amount of search results found
	 */
	@Override
    public int getResultCount(){
		return textAreas.size();
	}

	//public void setLength(int length) {
	//	Length = length;
	//}
	
	@Override
    public void setSearchTerm(final String term){
		this.searchTerm = term;
	}
	
	@Override
    public String getSearchTerm(){
		return searchTerm;
	}

	@Override
	public int getSelectedIndex() {
        return index;
	}
	
    int index;
	@Override
	public void setSelectedIndex(final int index) {
        this.index = index;
		getSelectionModel().clearAndSelect(index);
	}
	

	public int getStatus() {
		return status;
	}

	public void setStatus(final int status) {
		this.status = status;
	}

}
