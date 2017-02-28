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
 * JavaFXSearchWindow.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.GUIDisplay;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.JavaFxGUI;
import org.jpedal.examples.viewer.gui.generic.GUISearchList;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.grouping.DefaultSearchListener;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.grouping.SearchListener;
import org.jpedal.grouping.SearchType;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

/**
 * Provides interactive search Window and search capabilities.
 *
 * We extend from Stage to add search into the left side-tab or
 * to pop open a search window when either
 * style==SEARCH_EXTERNAL_WINDOW || style==SEARCH_TABBED_PANE
 * search style can be controlled from Viewer preferences.
 */
public class JavaFXSearchWindow extends Stage implements GUISearchWindow {
    
    /**Flag to show search has happened and needs reset*/
    public boolean hasSearched;
    
    /**flag to show searching taking place*/
    public boolean isSearch;
    
    int firstPageWithResults;
    
    int searchTypeParameters;
    
    private int style;
    
    Values commonValues;
    final GUIFactory currentGUI;
    PdfDecoderInt decode_pdf;
    
    /**flag to stop multiple listeners*/
    private boolean isSetup;
    
    boolean usingMenuBarSearch;
    
    private boolean endSearch;
    
    private boolean backGroundSearch;
    
    private Map searchAreas;
    
    VBox contentVB = new VBox();
    private int searchKey;
    
    private Thread searchThread;
    private final Runnable searchRunner = new Runnable() {
        
        @Override
        public void run() {
            // <start-demo><end-demo>
            
            resultsList.setStatus(GUISearchList.SEARCH_INCOMPLETE);
            final boolean searchingInBackground = backGroundSearch;
            
            //Now local variable is set we can turn off global variable
            backGroundSearch = false;
            
            final int currentKey = searchKey;
            try{
                // [AWI]: Create a new list model to append the search results to
                // NOTE: This was added to prevent a deadlock issue that occurred on the
                // EDT when the search resulted in a large number of hits
                final ObservableList resultListModel;
                
                if(updateListDuringSearch) {
                    resultListModel = listModel;
                } else {
                    resultListModel = FXCollections.observableArrayList();
                }
                
                int start = 1;
                int end = decode_pdf.getPageCount()+1;
                
                if(singlePageSearch){
                    start = decode_pdf.getPageNumber();
                    end = start+1;
                }
                
                //Create new value as this current page could change half way through a search
                final int currentPage = commonValues.getCurrentPage();
                int page;
                boolean continueSearch = true;
                for(; start!=end; start++){
                    if(usingMenuBarSearch){
                        //When using menu bar, break from loop if result found
                        if(resultsList.getResultCount()>=1){
                            break;
                        }
                        page = currentPage+(start-1);
                        if(page>commonValues.getPageCount()) {
                            page -= commonValues.getPageCount();
                        }
                    }else{
                        page = start;
                    }
                    
                    if(searchAreas!=null){
                        final int[][] highlights = (int[][])searchAreas.get(page);
                        if(highlights!=null){
                            for(int i = highlights.length-1; i>-1; i--){
                                final int[] a = highlights[i];
                                //[AWI]: Update the search method to take the target list model as a parameter
                                continueSearch = searchPage(page, a[0], a[1], a[0]+a[2], a[1]+a[3], currentKey, resultListModel);
                            }
                        }
                    }else{
                        continueSearch = searchPage(page, currentKey, resultListModel);
                    }
                    
                    if(!continueSearch) {
                        break;
                    }
                    
                    // new value or 16 pages elapsed
                    if (!searchingInBackground && (!resultListModel.isEmpty()) | ((page % 16) == 0)) {
                        final int pp = page;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + ' ' + itemFoundCount + ' '
                                        + Messages.getMessage("PdfViewerSearch.Scanning") + pp);
                            }
                        });
                        
                    }
                    
                }
                
                if(!searchingInBackground){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + ' ' + itemFoundCount + "  "
                                    + Messages.getMessage("PdfViewerSearch.Done"));
                        }
                    });
                }
                
                if(!usingMenuBarSearch){ //MenuBarSearch freezes if we attempt to wait
                    //Wait for EDT to catch up and prevent losing results
                    while(resultListModel.size()!=itemFoundCount){
                        Thread.sleep(200);
                    }
                }
                
                // [AWI]: Update the list model displayed in the results list
                // NOTE: This was added here to address an EDT lock-up and contention issue
                // that can occur when a large result set is returned from a search. By
                // setting the model once at the end, we only take the hit for updating the
                // JList once.
                listModel = resultListModel;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if ( resultsList != null && listModel != null ) {
                            resultsList.setItems(listModel);
                        }
                    }
                });
                
                if(continueSearch){
                    //resultsList.setLength(listModel.capacity());
                    
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            currentGUI.setResults(resultsList);
                            resultsList.scrollTo(0);
                            
                            resultsList.getSelectionModel().clearAndSelect(0);
                        }
                    });
                    
                }
                
                if(itemFoundCount>0) {
                    resultsList.setStatus(GUISearchList.SEARCH_COMPLETE_SUCCESSFULLY);
                } else {
                    resultsList.setStatus(GUISearchList.NO_RESULTS_FOUND);
                }
                
                //switch on buttons as soon as search produces valid results
                if(!searchingInBackground){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            currentGUI.getButtons().getButton(Commands.NEXTRESULT).setEnabled(true);
                            currentGUI.getButtons().getButton(Commands.PREVIOUSRESULT).setEnabled(true);
                        }
                    });
                    
                }
                
                // 
                
            }catch(final Exception e){
                e.printStackTrace();
                if(!searchingInBackground){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            currentGUI.getButtons().getButton(Commands.NEXTRESULT).setEnabled(true);
                            currentGUI.getButtons().getButton(Commands.PREVIOUSRESULT).setEnabled(true);
                        }
                    });
                }
                
                resultsList.setStatus(GUISearchList.SEARCH_PRODUCED_ERROR);
            }
        }
    };
    
    
    int lastPage=-1;
    
    String defaultMessage="Search PDF Here";
    
    public boolean requestInterupt;
    
    /**
     * deletes message when user starts typing
     */
    private boolean deleteOnClick;
    
    final ProgressBar progress = new ProgressBar(100);
    TextField searchText;
    Label searchCount;
    //DefaultListModel listModel;
    JavaFXSearchList resultsList;
    Button advOpts;
    
    private final VBox nav=new VBox();
    private VBox advancedPanel;
    private ComboBox searchType;
    private CheckBox wholeWordsOnlyBox, caseSensitiveBox, multiLineBox, highlightAll, searchAll, useRegEx, searchHighlightedOnly;
    Button searchButton;
    ObservableList listModel;
    
    /**Search this page only*/
    boolean singlePageSearch;
    
    /**Current Search value*/
    String[] searchTerms = {""};
    
    /**number fo search items*/
    private int itemFoundCount;
    
    final Map textPages=new HashMap();
    final Map textRectangles=new HashMap();
    
    public JavaFXSearchWindow(final GUIFactory currentGUI) {
        
        if(GUI.debugFX) {
            System.out.println("JavaFXSearchWindow constructor not yet implemented for JavaFX in JavaFXSearchWindow.java");
        }
        
        this.currentGUI=currentGUI;
        
        //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
    }
    
    @Override
    public void find(final PdfDecoderInt dec, final Values values) {
        
        
        //		System.out.println("clicked pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));
        
        /**
         * pop up new window to search text (initialise if required
         */
        if(!backGroundSearch){
            init(dec, values);
            if(style==SEARCH_EXTERNAL_WINDOW) {
                this.show();
            }
        }else{
            try {
                searchText();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    @Override
    public void findWithoutWindow(final PdfDecoderInt dec, final Values values, final int searchType, final boolean listOfTerms, final boolean singlePageOnly, final String searchValue) {
        if(!isSearch){
            backGroundSearch = true;
            isSearch=true;
            
            this.decode_pdf = dec;
            this.commonValues = values;
            
            progress.setProgress(0);
            progress.setVisible(true);

            if(!listOfTerms){ // find exact word or phrase
                searchTerms = new String[] {searchValue};
            } else { // match any of the words
                searchTerms = searchValue.split(" ");
                for (int i = 0; i < searchTerms.length; i++) {
                    searchTerms[i] = searchTerms[i].trim();
                }
            }
            
            searchTypeParameters = searchType;
            
            singlePageSearch = singlePageOnly;
            
            find(dec, values);
            
        }else{
            currentGUI.showMessageDialog("Please wait for search to finish before starting another.");
        }
    }
    
    @Override
    public void grabFocusInInput() {
        searchText.requestFocus();
    }
    
    @Override
    public boolean isSearchVisible() {
        return this.isShowing();
    }
    
    @Override
    public void init(final PdfDecoderInt dec, final Values values) {
        this.decode_pdf = dec;
        this.commonValues = values;
        
        if (isSetup) { //global variable so do NOT reinitialise
            searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + ' ' + itemFoundCount);
            searchText.selectAll();
            //searchText.grabFocus();
        } else {
            isSetup = true;
            
            setTitle(Messages.getMessage("PdfViewerSearchGUITitle.DefaultMessage"));
            
            defaultMessage = Messages.getMessage("PdfViewerSearchGUI.DefaultMessage");
            
            searchText = new TextField();
            //may need to apply line wrap for searchText
            
            searchText.setText(defaultMessage);
            searchText.setId("searchText");
            /*
            * [AWI] Add a focus listener to detect when keyboard input is needed in the search text field. This
            * registration was added to support systems configured with touchscreens and virtual keyboards.
            */
            //searchText.addFocusListener(new SearchTextFocusListener());
            
            searchButton = new Button(Messages.getMessage("PdfViewerSearch.Button"));
            
            advancedPanel = new VBox();
            
            final ObservableList<String> searchOptions
                    = FXCollections.observableArrayList(
                            Messages.getMessage("PdfViewerSearch.MatchWhole"),
                            Messages.getMessage("PdfViewerSearch.MatchAny")
                    );
            
            searchType = new ComboBox(searchOptions);
            searchType.setValue(searchType.getItems().get(0));
            
            wholeWordsOnlyBox = new CheckBox(Messages.getMessage("PdfViewerSearch.WholeWords"));
            wholeWordsOnlyBox.setId("wholeWords");
            
            caseSensitiveBox = new CheckBox(Messages.getMessage("PdfViewerSearch.CaseSense"));
            caseSensitiveBox.setId("caseSensitive");
            
            multiLineBox = new CheckBox(Messages.getMessage("PdfViewerSearch.MultiLine"));
            multiLineBox.setId("multiLine");
            
            highlightAll = new CheckBox(Messages.getMessage("PdfViewerSearch.HighlightsCheckBox"));
            highlightAll.setId("highlightAll");
            
            useRegEx = new CheckBox(Messages.getMessage("PdfViewerSearch.RegExCheckBox"));
            useRegEx.setId("useregex");
            
            searchHighlightedOnly = new CheckBox(Messages.getMessage("PdfViewerSearch.HighlightsOnly"));
            searchHighlightedOnly.setId("highlightsOnly");
            
            searchType.setId("combo");
            
            advancedPanel.getChildren().add(new Label(Messages.getMessage("PdfViewerSearch.ReturnResultsAs")));
            
            advancedPanel.getChildren().add(searchType);
            
            advancedPanel.getChildren().add(new Label(Messages.getMessage("PdfViewerSearch.AdditionalOptions")));
            
            advancedPanel.getChildren().add(wholeWordsOnlyBox);
            
            advancedPanel.getChildren().add(caseSensitiveBox);
            
            advancedPanel.getChildren().add(multiLineBox);
            
            advancedPanel.getChildren().add(highlightAll);
            
            advancedPanel.getChildren().add(useRegEx);
            
            advancedPanel.getChildren().add(searchHighlightedOnly);
            
            advancedPanel.setVisible(false);
            
            searchAll = new CheckBox();
            searchAll.setSelected(true);
            searchAll.setText(Messages.getMessage("PdfViewerSearch.CheckBox"));
            
            final VBox topPanel = new VBox();
            topPanel.getChildren().add(searchAll);
            
            advOpts = new Button(Messages.getMessage("PdfViewerSearch.ShowOptions"));
            advOpts.setId("advSearch");
            
            advOpts.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent me) {
                    if (GUIDisplay.allowChangeCursor) {
                        nav.setCursor(Cursor.HAND);
                    }
                }
            });
            
            advOpts.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent me) {
                    if (GUIDisplay.allowChangeCursor) {
                        nav.setCursor(Cursor.DEFAULT);
                    }
                }
            });
            
            advOpts.setOnMouseClicked(new EventHandler<MouseEvent>() {
                boolean isVisible;
                @Override
                public void handle(final MouseEvent me) {
                    if (isVisible) {
                        advOpts.setText(Messages.getMessage("PdfViewerSearch.ShowOptions"));
                        advancedPanel.setVisible(false);
                    } else {
                        advOpts.setText(Messages.getMessage("PdfViewerSearch.HideOptions"));
                        advancedPanel.setVisible(true);
                    }
                    isVisible = !isVisible;
                }
            });
            
            topPanel.getChildren().add(advOpts);
            
            nav.getChildren().add(topPanel);
            final HBox navBox = new HBox();
            navBox.getChildren().addAll(searchText, searchButton);
            nav.getChildren().add(navBox);
            itemFoundCount = 0;
            textPages.clear();
            textRectangles.clear();
            //listModel = null;
            
            searchCount = new Label(Messages.getMessage("PdfViewerSearch.ItemsFound") + ' ' + itemFoundCount);
            nav.getChildren().add(searchCount);
            
            listModel = FXCollections.observableArrayList();
            listModel = FXCollections.observableArrayList();
            resultsList = new JavaFXSearchList(listModel, textPages, textRectangles);
            resultsList.setId("results");
            
            //<link><a name="search" />
            /**
             * highlight text on item selected
             */
            resultsList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(final ObservableValue<? extends Number> ov, final Number t, final Number t1) {
                    /**
                     * Only do something on mouse button up, prevents this code
                     * being called twice on mouse click
                     */
                    
                    if (!Values.isProcessing()) {//{if (!event.getValueIsAdjusting()) {
                        
                        final float scaling = currentGUI.getScaling();
                        //int inset=currentGUI.getPDFDisplayInset();
                        
                        final int id = (Integer)t1;//resultsList.getSelectedIndex();
                        
                        decode_pdf.getTextLines().clearHighlights();
                        //System.out.println("clicked pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));
                        
                        if (id != -1) {
                            
                            final Integer key = id;
                            final Object newPage = textPages.get(key);
                            
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
                                        Thread.sleep(500);
                                    } catch (final InterruptedException ee) {
                                        ee.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }
                                }
                                
                                /**
                                 * Highlight all search results on page.
                                 */
                                if ((searchTypeParameters & SearchType.HIGHLIGHT_ALL_RESULTS) == SearchType.HIGHLIGHT_ALL_RESULTS) {
                                    
                                    //										PdfHighlights.clearAllHighlights(decode_pdf);
                                    int[][] showAllOnPage;
                                    Vector_Rectangle_Int storageVector = new Vector_Rectangle_Int();
                                    int lastPage = -1;
                                    for (int k = 0; k != resultsList.getItems().size(); k++) {
                                        final Object page = textPages.get(k);
                                        
                                        if (page != null) {
                                            
                                            final int currentPage = (Integer) page;
                                            if (currentPage != lastPage) {
                                                storageVector.trim();
                                                showAllOnPage = storageVector.get();
                                                
                                                for (int p = 0; p != showAllOnPage.length; p++) {
                                                    System.out.println(Arrays.toString(showAllOnPage[p]));
                                                }
                                                
                                                decode_pdf.getTextLines().addHighlights(showAllOnPage, true, lastPage);
                                                lastPage = currentPage;
                                                storageVector = new Vector_Rectangle_Int();
                                            }
                                            
                                            final Object highlight = textRectangles.get(k);
                                            
                                            if (highlight instanceof int[]) {
                                                storageVector.addElement((int[]) highlight);
                                            }
                                            if (highlight instanceof int[][]) {
                                                final int[][] areas = (int[][]) highlight;
                                                for (int i = 0; i != areas.length; i++) {
                                                    storageVector.addElement(areas[i]);
                                                }
                                            }
                                            //decode_pdf.addToHighlightAreas(decode_pdf, storageVector, currentPage);
                                            //												}
                                        }
                                    }
                                    storageVector.trim();
                                    showAllOnPage = storageVector.get();
                                    
                                    decode_pdf.getTextLines().addHighlights(showAllOnPage, true, lastPage);
                                } else {
                                    //										PdfHighlights.clearAllHighlights(decode_pdf);
                                    final Object page = textPages.get(key);
                                    final int currentPage = (Integer) page;
                                    
                                    final Vector_Rectangle_Int storageVector = new Vector_Rectangle_Int();
                                    // int[] scroll = null;
                                    final Object highlight = textRectangles.get(key);
                                    if (highlight instanceof int[]) {
                                        storageVector.addElement((int[]) highlight);
                                        //scroll = (int[]) highlight;
                                    }
                                    
                                    if (highlight instanceof int[][]) {
                                        final int[][] areas = (int[][]) highlight;
                                        // scroll = areas[0];
                                        for (int i = 0; i != areas.length; i++) {
                                            storageVector.addElement(areas[i]);
                                        }
                                    }
                                    
                                    //Scroll.rectToHighlight(scroll, currentPage, decode_pdf);
                                    storageVector.trim();
                                    decode_pdf.getTextLines().addHighlights(storageVector.get(), true, currentPage);
                                    //PdfHighlights.addToHighlightAreas(decode_pdf, storageVector, currentPage);
                                    
                                }
                                
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        decode_pdf.repaintPane(commonValues.getCurrentPage());
                                        currentGUI.scaleAndRotate();
                                    }
                                });
                                
                            }
                        }
                    }
                    
                    //When page changes make sure only relevant navigation buttons are displayed
                    if (commonValues.getCurrentPage() == 1) {
                        currentGUI.getButtons().setBackNavigationButtonsEnabled(false);
                    } else {
                        currentGUI.getButtons().setBackNavigationButtonsEnabled(true);
                    }
                    
                    if (commonValues.getCurrentPage() == decode_pdf.getPageCount()) {
                        currentGUI.getButtons().setForwardNavigationButtonsEnabled(false);
                    } else {
                        currentGUI.getButtons().setForwardNavigationButtonsEnabled(true);
                    }
                }
            });
            
            //setup searching
            searchButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    
                    if (!isSearch) {
                        
                        try {
                            searchTypeParameters = SearchType.DEFAULT;
                            
                            if (wholeWordsOnlyBox.isSelected()) {
                                searchTypeParameters |= SearchType.WHOLE_WORDS_ONLY;
                            }
                            
                            if (caseSensitiveBox.isSelected()) {
                                searchTypeParameters |= SearchType.CASE_SENSITIVE;
                            }
                            
                            if (multiLineBox.isSelected()) {
                                searchTypeParameters |= SearchType.MUTLI_LINE_RESULTS;
                            }
                            
                            if (highlightAll.isSelected()) {
                                searchTypeParameters |= SearchType.HIGHLIGHT_ALL_RESULTS;
                            }
                            
                            if (useRegEx.isSelected()) {
                                searchTypeParameters |= SearchType.USE_REGULAR_EXPRESSIONS;
                            }
                            
                            if (searchHighlightedOnly.isSelected()) {
                                searchTypeParameters |= SearchType.SEARCH_HIGHLIGHTS_ONLY;
                            }
                            
                            final String textToFind = searchText.getText().trim();
                            
                            if (searchType.getValue() == searchType.getItems().get(0)) { // find exact word or phrase
                                searchTerms = new String[]{textToFind};
                            } else { // match any of the words
                                searchTerms = textToFind.split(" ");
                                for (int i = 0; i < searchTerms.length; i++) {
                                    searchTerms[i] = searchTerms[i].trim();
                                }
                            }
                            
                            singlePageSearch = !searchAll.isSelected();
                            
                            searchText();
                        } catch (final Exception e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        requestInterupt = true;
                        isSearch = false;
                        searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));
                    }
                    ((JavaFxGUI)currentGUI).getPdfDecoder().requestFocus();
                    
                }
            });
            
            searchText.selectAll();
            deleteOnClick = true;
            
            searchText.setOnKeyPressed(new EventHandler<KeyEvent>() {
                
                @Override
                public void handle(final KeyEvent e) {
                    
                    currentGUI.getButtons().getButton(Commands.NEXTRESULT).setEnabled(false);
                    currentGUI.getButtons().getButton(Commands.PREVIOUSRESULT).setEnabled(false);
                    
                    if (e.getCode() == KeyCode.ENTER) {
                        if (!decode_pdf.isOpen()) {
                            currentGUI.showMessageDialog("File must be open before you can search.");
                        } else {
                            try {
                                
                                isSearch = false;
                                searchTypeParameters = SearchType.DEFAULT;
                                
                                if (wholeWordsOnlyBox.isSelected()) {
                                    searchTypeParameters |= SearchType.WHOLE_WORDS_ONLY;
                                }
                                
                                if (caseSensitiveBox.isSelected()) {
                                    searchTypeParameters |= SearchType.CASE_SENSITIVE;
                                }
                                
                                if (multiLineBox.isSelected()) {
                                    searchTypeParameters |= SearchType.MUTLI_LINE_RESULTS;
                                }
                                
                                if (highlightAll.isSelected()) {
                                    searchTypeParameters |= SearchType.HIGHLIGHT_ALL_RESULTS;
                                }
                                
                                if (useRegEx.isSelected()) {
                                    searchTypeParameters |= SearchType.USE_REGULAR_EXPRESSIONS;
                                }
                                
                                if (searchHighlightedOnly.isSelected()) {
                                    searchTypeParameters |= SearchType.SEARCH_HIGHLIGHTS_ONLY;
                                }
                                
                                final String textToFind = searchText.getText().trim();
                                if (searchType.getValue() == searchType.getItems().get(0)) { // find exact word or phrase
                                    searchTerms = new String[]{textToFind};
                                } else { // match any of the words
                                    searchTerms = textToFind.split(" ");
                                    for (int i = 0; i < searchTerms.length; i++) {
                                        searchTerms[i] = searchTerms[i].trim();
                                    }
                                }
                                
                                singlePageSearch = !searchAll.isSelected();
                                
                                searchText();
                                
                                ((JavaFxGUI)currentGUI).getPdfDecoder().requestFocus();
                            } catch (final Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            });
            
            searchText.focusedProperty().addListener(new ChangeListener<Boolean>()
            {
                @Override
                public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean oldPropertyValue, final Boolean newPropertyValue)
                {
                    if (newPropertyValue && deleteOnClick) //focus gained
                    {
                        deleteOnClick = false;
                        searchText.setText("");
                    }
                    else if(searchText.getText().isEmpty()) //focus lost
                    {
                        searchText.setText(defaultMessage);
                        deleteOnClick = true;
                    }
                }
            });
            
            if (style == SEARCH_EXTERNAL_WINDOW || style == SEARCH_TABBED_PANE) {
                if(GUI.debugFX)
                {
                    System.err.println("Implementation of tab bar not here yet for JavaFX");
                }
                
                /**
                 * Finalise the Stage, add items and scene.
                 */
                final BorderPane bp = new BorderPane();
                contentVB = new VBox();
                contentVB.getChildren().addAll(nav,resultsList, advancedPanel);
                bp.setCenter(contentVB);
                final Scene scene = new Scene(bp);
                this.setScene(scene);
                
                
//
//                if (style == SEARCH_EXTERNAL_WINDOW) {
//                  Code here which positions the External Window to the right
//                  next to the main viewer window.
//                }
//                searchAll.setFocusable(false);
//
//                searchText.grabFocus();
                
            } else {
                //Whole Panel not used, take what is needed
                currentGUI.setSearchText(searchText);
            }
        }
    }
    public VBox getContentVB(){
        return contentVB;
    }
    
    @Override
    public void removeSearchWindow(final boolean justHide) {
        
        this.close();
        
        if (isSetup && !justHide) {
            if (listModel != null) {
                listModel.clear();
            }
            itemFoundCount = 0;
            isSearch = false;
            
        }
        
        //lose any highlights and force redraw with non-existent box
        if (decode_pdf != null) {
            decode_pdf.getTextLines().clearHighlights();
            decode_pdf.repaintPane(commonValues.getCurrentPage());
        }
    }
    
    /**
     * Reset search text and menu bar buttons when opening new page
     */
    @Override
    public void resetSearchWindow(){
        if(isSetup){
            
            searchText.setText(defaultMessage);
            deleteOnClick=true;
            
            if(hasSearched){
                //    			resultsList = null;
                currentGUI.getButtons().getButton(Commands.NEXTRESULT).setEnabled(false);
                currentGUI.getButtons().getButton(Commands.PREVIOUSRESULT).setEnabled(false);
                hasSearched = false;
            }
            decode_pdf.requestFocus();
        }
    }
    
    @Override
    public GUISearchList getResults() {
        return resultsList;
    }
    
    @Override
    public GUISearchList getResults(final int page) {
        usingMenuBarSearch = style==SEARCH_MENU_BAR;
        
        if(page !=lastPage && usingMenuBarSearch){
            
            try {
                
                searchKey++;
                if(searchKey>3000) {
                    searchKey = 0;
                }
                
                clearCurrentResults();
                
                searchPage(page, searchKey, listModel);
                
            } catch (final Exception e) {
                e.printStackTrace();
            }
            
        }
        
        return resultsList;
    }
    
    @Override
    public Map getTextRectangles() {
        return Collections.unmodifiableMap(textRectangles);
    }
    
    @Override
    public int getViewStyle() {
        return style;
    }
    
    @Override
    public void setViewStyle(final int i) {
        style = i;
    }
    
    @Override
    public int getFirstPageWithResults() {
        return firstPageWithResults;
    }
    
    @Override
    public void setWholeWords(final boolean wholeWords) {
        wholeWordsOnlyBox.setSelected(wholeWords);
    }
    
    @Override
    public void setCaseSensitive(final boolean caseSensitive) {
        caseSensitiveBox.setSelected(caseSensitive);
    }
    
    @Override
    public void setMultiLine(final boolean multiLine) {
        multiLineBox.setSelected(multiLine);
    }
    
    @Override
    public void setSearchText(final String s) {
        searchText.setText(s);
    }
    
    //@Override
   // public void setSearchHighlightsOnly(boolean highlightOnly){
    //    searchHighlightedOnly.setSelected(highlightOnly);
    //}
    
    private void searchText()  throws Exception{
        //Flag is we are using menu bar search
        usingMenuBarSearch = style == SEARCH_MENU_BAR;
        
        //Alter searchKey so the update thread knows not to update
        searchKey++;
        
        //Reset last page searched flag.
        lastPage=-1;
        
        /*
        * To prevent the chance of hitting the maximum value of searchKey
        * we should reset long after a value large enough to guarantee
        * any thread using a searchKey of 0 is closed.
        */
        if(searchKey>3000) {
            searchKey = 0;
        }
        
        //Cancel a search if currently exists
        if(searchThread!=null && searchThread.isAlive()){
            
            //Call for search to finish
            endSearch = true;
            
            searchThread.interrupt();
            
            while (searchThread.isAlive()) {
                //Wait for search to end
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Attempting to set propeties values " + e);
                    }
                }
            }

            endSearch = false;
            
        }
        
        if(!usingMenuBarSearch && (searchTypeParameters & SearchType.SEARCH_HIGHLIGHTS_ONLY) == SearchType.SEARCH_HIGHLIGHTS_ONLY){
            searchAreas = decode_pdf.getTextLines().getAllHighlights();
        }else{
            searchAreas = null;
        }
        
        clearCurrentResults();
        
        searchThread = new Thread(searchRunner);
        searchThread.start();
//        Platform.runLater(searchThread);
    }
    
    private void clearCurrentResults(){
        
        listModel.clear();
        resultsList.getItems().clear();
        textPages.clear();
        textRectangles.clear();
        itemFoundCount = 0;
        decode_pdf.getTextLines().clearHighlights();
    }
    
    /**
     * Performs the currently set up search on the given page
     *
     * @param page :: Page to be searched with the currently set term and
     * settings
     * @param currentKey :: The current search key, used to control results
     * update when search ends
     * @return True if search routine should continue
     */
    private boolean searchPage(final int page, final int currentKey, final ObservableList resultListModel) throws Exception{
        final PdfPageData currentPageData = decode_pdf.getPdfPageData();
        final int x1 = currentPageData.getMediaBoxX(page);
        final int x2 = currentPageData.getMediaBoxWidth(page) + x1;
        final int y2 = currentPageData.getMediaBoxY(page);
        final int y1 = currentPageData.getMediaBoxHeight(page) + y2;
        return searchPage(page, x1, y1, x2, y2, currentKey, resultListModel);
    }
    /**
     * Performs the currently set up search on the given page
     * @param x1 the left x cord
     * @param y1 the upper y cord
     * @param x2 the right x cord
     * @param y2 the lower y cord
     * @param page :: Page to be searched with the currently set term and settings
     * @param currentKey :: The current search key, used to control results update when search ends
     * @param model :: [AWI] The list model to append the search results to
     * @return True if search routine should continue
     */
    private boolean searchPage(final int page, final int x1, final int y1, final int x2, final int y2, final int currentKey, final ObservableList resultListModel) throws Exception {
        
        final PdfGroupingAlgorithms grouping;
        
        final PdfPageData pageSize = decode_pdf.getPdfPageData();
        
        if (page == commonValues.getCurrentPage()) {
            grouping = decode_pdf.getGroupingObject();
        } else {
            decode_pdf.decodePageInBackground(page);
            grouping = decode_pdf.getBackgroundGroupingObject();
        }
        
//        // set size
//        int x1 = pageSize.getCropBoxX(page);
//        int x2 = pageSize.getCropBoxWidth(page);
//        int y1 = pageSize.getCropBoxY(page);
//        int y2 = pageSize.getCropBoxHeight(page);
        
        final SearchListener listener = new DefaultSearchListener();
        
        // tell JPedal we want teasers
        grouping.generateTeasers();
        
        //allow us to add options
        grouping.setIncludeHTML(false);
        
        //Set search term in results list
        resultsList.setSearchTerm(searchText.getText().trim());
        
        final SortedMap highlightsWithTeasers = grouping.findTextWithinInAreaWithTeasers(x1, y1, x2, y2, pageSize.getRotation(page), page, searchTerms, searchTypeParameters, listener);
        
        /**
         * update data structures with results from this page
         */
        if (!highlightsWithTeasers.isEmpty()) {
            
            itemFoundCount += highlightsWithTeasers.size();
            
            for (final Object o : highlightsWithTeasers.entrySet()) {
                final Map.Entry e = (Map.Entry) o;
                
                /*highlight is a rectangle or a rectangle[]*/
                final Object highlight = e.getKey();
                
                final String teaser = (String) e.getValue();
                
                if(currentKey == searchKey){
                    resultListModel.add(teaser);
                }
                
                final Integer key = textRectangles.size();
                textRectangles.put(key, highlight);
                textPages.put(key, page);
                
            }
        }
        
        lastPage = page;
        
        //Ending search now
        return !endSearch;
        
    }
    
    //Flag to control if we update search results during search or at the end
    boolean updateListDuringSearch = true;
    
    @Override
    public void setUpdateListDuringSearch(final boolean updateListDuringSearch) {
        this.updateListDuringSearch = updateListDuringSearch;
    }
    
}
