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
 * JavaFXDocInfo.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXMessageDialog;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.PdfFileInformation;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.acroforms.ReturnValues;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.utils.Messages;

/**
 * Shows a Message Dialogue displaying the Documents Information.
 */
public class JavaFXDocInfo {

    private static final Font headerFont = Font.font("SanSerif", FontWeight.BOLD, 14); //Title Font
    private static final Font textFont = Font.font("SanSerif", 12); //Text Font
    private static final String lb = "\n"; //Line Breaker
    private static final StackPane treeContainer = new StackPane();

    public static void execute(final Object[] args, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf) {
        if (args == null) {
            if (!commonValues.isPDF()) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.ImageSearch"));
            }else if(commonValues.getSelectedFile() == null){
                    currentGUI.showMessageDialog(Messages.getMessage("PdfVieweremptyFile.message"), Messages.getMessage("PdfViewerTooltip.pageSize"),FXMessageDialog.PLAIN_MESSAGE);
            }else{
                getPropertiesBox(commonValues.getSelectedFile(), commonValues.getFileSize(), commonValues.getPageCount(), commonValues.getCurrentPage(), decode_pdf);
            }
        }
    }

    private static void getPropertiesBox(final String selectedFile, final long fileSize, final int pageCount, final int currentPage, final PdfDecoderInt decode_pdf) {

        /**
         * Setup Vars to Pass.
         */
        final String user_dir = System.getProperty("user.dir");
        final PdfFileInformation currentFileInformation=decode_pdf.getFileInformationData();

        int ptr = 0;
        if (ptr == -1) {
            ptr = selectedFile.lastIndexOf('/');
        }

        final String file = selectedFile.substring(ptr + 1, selectedFile.length());

        final String path = selectedFile.substring(0, ptr + 1);


        /**
         * Build the Default Tabs.
         */
        final TabPane tabPane = new TabPane();
        tabPane.setId("docProp");
        final Tab properties = new Tab("Properties");
        final Tab fonts = new Tab("Fonts");
        final Tab available = new Tab("Available");
        final Tab aliases = new Tab("Aliases");

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); //Stops ability to close tabs

        /**
         * Populate the Properties Tab.
         */
        properties.setContent(getPropTab(currentFileInformation,file, path, user_dir, fileSize, pageCount, currentPage, decode_pdf));

        /**
         * Populate the Fonts Tab.
         */
        fonts.setContent(getFontTab(decode_pdf.getInfo(PdfDictionary.Font)));

        /**
         * Populate the Available Tab.
         */
        available.setContent(getAvailableTab());
        
        /**
         * Populate the Aliases Tab.
         */
        aliases.setContent(getAliasesTab());
        
        tabPane.getTabs().addAll(properties, fonts, available, aliases); //Add tabs to tabPane
        
        /**
         * Populate the Forms Tab.
         */
        if(getFormsTab(decode_pdf)!=null){
            final Tab forms = new Tab("Forms");
            forms.setContent(getFormsTab(decode_pdf));
            tabPane.getTabs().add(forms);
        }
        
        /**
         * Populate the Image Tab.
         */
        if (org.jpedal.parser.image.ImageCommands.trackImages) {
            final Tab image = new Tab("Image");
            image.setContent(getImageTab(decode_pdf));
            tabPane.getTabs().add(image);
        }
        
        /**
         * Populate the XML Tab.
         */
        final String xmlText=currentFileInformation.getFileXMLMetaData();
        if(!xmlText.isEmpty()){
            final Tab xml = new Tab("XML");
            xml.setContent(getXMLTab(xmlText));
            tabPane.getTabs().add(xml);
        }

        final FXDialog newDialog = new FXDialog(null, Modality.APPLICATION_MODAL, tabPane, 400, 300);
        newDialog.show();
    }

    /**
     * Grabs the Contents for the Properties Tab.
     *
     * @param file
     * @param path
     * @param user_dir
     * @param size
     * @param pageCount
     * @param currentPage
     * @param decode_pdf
     * @return ScrollPane Object
     */
    private static ScrollPane getPropTab(final PdfFileInformation currentFileInformation, final String file, final String path, final String user_dir, final long size, final int pageCount, final int currentPage, final PdfDecoderInt decode_pdf) {


        /**
         * Setup the ScrollPane.
         */
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        /**
         * get the Pdf file information object to extract info from
         */
        if (currentFileInformation != null) {

            final VBox content = new VBox();

            /**
             * Setup General Content.
             */
            final Text generalTitle = new Text(lb + Messages.getMessage("PdfViewerGeneral"));
            generalTitle.setFont(headerFont);

            StringBuffer pdfVersion = new StringBuffer("PDF"); //store PDF version
            pdfVersion.append(decode_pdf.getPDFVersion());
            //Check if PDF is Linearized
            if (decode_pdf.getJPedalObject(PdfDictionary.Linearized) != null) {
                pdfVersion.append(" (").append(Messages.getMessage("PdfViewerLinearized.text")).append(") "); //Update PDF version
            }
            //Populate General Content
            final Text generalContent = new Text(Messages.getMessage("PdfViewerFileName") + file + lb
                    + Messages.getMessage("PdfViewerFilePath") + path + lb
                    + Messages.getMessage("PdfViewerCurrentWorkingDir") + ' ' + user_dir + lb
                    + Messages.getMessage("PdfViewerFileSize") + size + " K" + lb
                    + Messages.getMessage("PdfViewerPageCount") + pageCount + lb
                    + pdfVersion + lb
            );
            generalContent.setFont(textFont);

            /**
             * Setup Properties Content.
             */
            final Text propertiesTitle = new Text(Messages.getMessage("PdfViewerProperties"));
            propertiesTitle.setFont(headerFont);

            //Populate the Properties Content
            final Text propertiesContent = new Text();
            propertiesContent.setFont(textFont);
            final String[] values = currentFileInformation.getFieldValues();
            final String[] fields = PdfFileInformation.getFieldNames();

            for (int i = 0; i < fields.length; i++) {
                if (!values[i].isEmpty()) {
                    propertiesContent.setText(propertiesContent.getText() + fields[i] + " = " + values[i] + lb);
                }
            }

            //Populate the VBox with current information
            content.getChildren().addAll(generalTitle, generalContent, propertiesTitle, propertiesContent);

            /**
             * Setup Page Coordinates Content.
             */
            final PdfPageData currentPageSize = decode_pdf.getPdfPageData();
            if (currentPageSize != null) {
                final Text pageCoordsTitle = new Text(Messages.getMessage("PdfViewerCoords.text"));
                pageCoordsTitle.setFont(headerFont);

                final Text pageCoordsContent = new Text(Messages.getMessage("PdfViewermediaBox.text") + currentPageSize.getMediaValue(currentPage) + lb
                        + Messages.getMessage("PdfViewercropBox.text") + currentPageSize.getCropValue(currentPage) + lb
                        + Messages.getMessage("PdfViewerLabel.Rotation") + currentPageSize.getRotation(currentPage) + lb
                );
                pageCoordsContent.setFont(textFont);

                //Populate the VBox with [age coordinate information
                content.getChildren().addAll(pageCoordsTitle, pageCoordsContent);
            }

            scrollPane.setContent(content);
        }

        return scrollPane;
    }

    /**
     * Grabs the Contents for the Fonts Tab.
     *
     * @param xmlTxt
     * @return ScrollPane Object
     */
    private static ScrollPane getFontTab(final String xmlTxt) {

        /**
         * Setup the ScrollPane.
         */
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        /**
         * List of Fonts.
         */
        StringBuilder xmlText = new StringBuilder( "Font Substitution Mode: ");

        switch (FontMappings.getFontSubstitutionMode()) {
            case (1):
                xmlText.append("Using File Name");
                break;
            case (2):
                xmlText.append("Using PostScript Name");
                break;
            case (3):
                xmlText.append("Using Family Name");
                break;
            case (4):
                xmlText.append("Using The Full Font Name");
                break;
            default:
                xmlText.append("Unknown Font Substitution Mode");
                break;
        }
        xmlText.append(lb);

        if (!xmlTxt.isEmpty()) {

            final VBox content = new VBox();

            final Text generalTitle = new Text(lb + "General");
            generalTitle.setFont(headerFont);
            final Text fontContent = new Text(xmlText.toString());
            fontContent.setFont(textFont);

            final Text fontListTitle = new Text("Fonts Used");
            fontListTitle.setFont(headerFont);
            final Text fontListContent = new Text(xmlTxt);
            fontListContent.setFont(textFont);

            content.getChildren().addAll(generalTitle, fontContent, fontListTitle, fontListContent);

            scrollPane.setContent(content);

        }

        return scrollPane;
    }

    /**
     * Grabs the Contents for the Image Tab.
     * 
     * @param decode_pdf
     * @return ScrollPane Object
     */
    private static ScrollPane getImageTab(final PdfDecoderInt decode_pdf) {

        /**
         * Setup the ScrollPane.
         */
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        /**
         * list of Images (not forms).
         */
        final String xmlTxt = decode_pdf.getInfo(PdfDictionary.Image);

        if (!xmlTxt.isEmpty()) {

            final VBox content = new VBox();

            final Text imageListTitle = new Text(lb + "Images Used");
            imageListTitle.setFont(headerFont);
            final Text imageListContent = new Text(xmlTxt);
            imageListContent.setFont(textFont);

            content.getChildren().addAll(imageListTitle, imageListContent);
            
            scrollPane.setContent(content);
            
        }

        return scrollPane;
    }
    
    /**
     * Grabs the Contents for the XML Tab.
     * 
     * @param xmlText
     * @return ScrollPane Object
     */
    private static ScrollPane getXMLTab(final String xmlText){

        /**
         * Setup the ScrollPane.
         */
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        /**
         * Fill the VBox with Content.
         */
        final VBox content = new VBox();

        final Text imageListTitle = new Text(lb + "XML Content");
        imageListTitle.setFont(headerFont);
        
        final Text imageListContent = new Text(xmlText);
        imageListContent.setFont(textFont);
        
        content.getChildren().addAll(imageListTitle, imageListContent);
        
        scrollPane.setContent(content);
        
        return scrollPane;
    }
    
    /**
     * Grabs the Contents for the Forms Tab.
     * 
     * @param decode_pdf
     * @return ScrollPane Object
     */
    private static ScrollPane getFormsTab(final PdfDecoderInt decode_pdf) {

        ScrollPane scrollPane = null;

        //get the form renderer
        final AcroRenderer formRenderer = decode_pdf.getFormRenderer();

        if (formRenderer != null) {

            //get list of forms on page
            final Object[] formsOnPage = formRenderer.getFormComponents(null, ReturnValues.FORM_NAMES, decode_pdf.getPageNumber());

            //allow for no forms
            if (formsOnPage != null) {

                final int formCount = formsOnPage.length;

                /**
                 * Setup the ScrollPane.
                 */
                scrollPane = new ScrollPane();
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                /**
                 * Fill the VBox with Content.
                 */
                final VBox content = new VBox();

                final Text tabTitle = new Text(lb + "Page Contains " + formCount + " Form Objects" + lb);
                tabTitle.setFont(headerFont);
                content.getChildren().addAll(tabTitle);

                /**
                 * populate our list with details
                 */
                for (final Object aFormsOnPage : formsOnPage) {

                    // get name of form
                    final String formName = (String) aFormsOnPage;

                    //swing component we map data into
                    final Object[] comp = formRenderer.getFormComponents(formName, ReturnValues.GUI_FORMS_FROM_NAME, -1);

                    if (comp != null) {

                        final Text formObject = new Text(formName);
                        formObject.setFont(headerFont);

                        //take value or first if array to check for types (will be same if children)
                        FormObject formObj = null;

                        //extract list of actual PDF references to display and get FormObject
                        StringBuilder PDFrefs = new StringBuilder("PDF ref=");

                        //actual data read from PDF
                        final Object[] rawFormData = formRenderer.getFormComponents(formName, ReturnValues.FORMOBJECTS_FROM_NAME, -1);
                        for (final Object aRawFormData : rawFormData) {
                            formObj = (FormObject) aRawFormData;
                            PDFrefs.append(' ').append(formObj.getObjectRefAsString());
                        }

                        //extract pdf type
                        final String PDFType = "Type = "
                                + PdfDictionary.showAsConstant(formObj.getParameterConstant(PdfDictionary.Type))
                                + " Subtype=" + PdfDictionary.showAsConstant(formObj.getParameterConstant(PdfDictionary.Subtype));

                        final String standardDetails = "Java Class = " + comp[0].getClass();

                        final Text formObjInfo = new Text(PDFrefs + lb + PDFType + lb + standardDetails);
                        formObjInfo.setFont(textFont);

                        content.getChildren().addAll(formObject, formObjInfo);
                    }
                }

                scrollPane.setContent(content);
            }

        }

        return scrollPane;
    }
    
    /**
     * Grabs the Contents for the Aliases Tab.
     *
     * @return ScrollPane Object
     */
    private static ScrollPane getAliasesTab() {

        /**
         * Setup the ScrollPane.
         */
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        /**
         * Add Tab Title
         */
        final Text aliasesTitle = new Text(lb + "Aliases Content");
        aliasesTitle.setFont(headerFont);

        /**
         * list of all fonts fonts
         */
        final Text aliasesContent = new Text();
        aliasesContent.setFont(textFont);
        for (final Object nextFont : FontMappings.fontSubstitutionAliasTable.keySet()) {
            aliasesContent.setText(aliasesContent.getText() + nextFont + " ==> " + FontMappings.fontSubstitutionAliasTable.get(nextFont) + lb);
        }

        if (aliasesContent.getText() != null) {
            final VBox content = new VBox();
            content.getChildren().addAll(aliasesTitle, aliasesContent);
            scrollPane.setContent(content);
        }

        return scrollPane;
    }
    
    private static VBox getAvailableTab() {

        final VBox container = new VBox();
        
        /**
         * Setup Title.
         */
        final Text titleText = new Text (lb+"Available Fonts");
        titleText.setFont(headerFont);
        
        /**
         * Setup the Filter Options.
         */
        final GridPane filterContainer = new GridPane();

        final Text filterText = new Text("Filter Font List");
        filterText.setFont(textFont);

        final TextField filterField = new TextField();
        filterField.setMaxSize(150, 0);
        filterField.setFont(textFont);

        final RadioButton sortFolder = new RadioButton("Sort By Folder ");
        sortFolder.setFont(textFont);

        final RadioButton sortName = new RadioButton("Sort By Name ");
        sortFolder.setFont(textFont);

        filterContainer.addColumn(1, sortFolder, sortName);
        filterContainer.addColumn(2, filterText, filterField);
        filterContainer.setAlignment(Pos.CENTER);
        filterContainer.setHgap(20);

        /**
         * Setup the Search Display.
         */
        //Add format buttons to a toggle group, so only one be selected.
        final ToggleGroup docInfoGroup = new ToggleGroup();
        sortFolder.setSelected(true);
        docInfoGroup.getToggles().addAll(sortFolder, sortName);
        getAvailableFonts(filterField, sortFolder.isSelected());
        final ChangeListener<Toggle> updateSelectionListener = new ChangeListener<Toggle>() {
            @Override
            public void changed(final ObservableValue<? extends Toggle> ov,
                    final Toggle old_toggle, final Toggle new_toggle) {

                getAvailableFonts(filterField, sortFolder.isSelected());

            }
        };

        docInfoGroup.selectedToggleProperty().addListener(updateSelectionListener);

        filterText.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent t) {
                getAvailableFonts(filterField,sortFolder.isSelected());
            }
        });
        
        container.getChildren().addAll(titleText,filterContainer, treeContainer);
        container.setAlignment(Pos.CENTER);

        return container;
    }
    
    /**
     * list of all fonts properties in sorted order
     */
    private static TreeItem populateAvailableFonts(final TreeItem top, final String filter, final boolean sortFontsByDir) {

        //get list
        if (FontMappings.fontSubstitutionTable != null) {
            final Set fonts = FontMappings.fontSubstitutionTable.keySet();
            final Iterator fontList = FontMappings.fontSubstitutionTable.keySet().iterator();

            final int fontCount = fonts.size();
            final ArrayList fontNames = new ArrayList(fontCount);

            while (fontList.hasNext()) {
                fontNames.add(fontList.next().toString());
            }

            //sort
            Collections.sort(fontNames);

            //Sort and Display Fonts by Directory
            if (sortFontsByDir) {

                final java.util.List location = new ArrayList();
                final java.util.List locationNode = new ArrayList();

                //build display
                for (int ii = 0; ii < fontCount; ii++) {
                    final Object nextFont = fontNames.get(ii);

                    String current = ((String) FontMappings.fontSubstitutionLocation.get(nextFont));

                    int ptr = current.lastIndexOf(System.getProperty("file.separator"));
                    if (ptr == -1 && current.indexOf('/') != -1) {
                        ptr = current.lastIndexOf('/');
                    }

                    if (ptr != -1) {
                        current = current.substring(0, ptr);
                    }

                    if (filter == null || ((String) nextFont).toLowerCase().contains(filter.toLowerCase())) {
                        if (!location.contains(current)) {
                            location.add(current);
                            final TreeItem loc = new TreeItem(new TreeItem(current));
                            top.getChildren().add(loc);
                            locationNode.add(loc);
                        }

                        final TreeItem FontTop = new TreeItem(nextFont + " = " + FontMappings.fontSubstitutionLocation.get(nextFont));
                        final int pos = location.indexOf(current);
                        ((TreeItem) locationNode.get(pos)).getChildren().add(FontTop);

                        //add details
                        final String loc = (String) FontMappings.fontPropertiesTable.get(nextFont + "_path");
                        final Integer type = (Integer) FontMappings.fontPropertiesTable.get(nextFont + "_type");

                        final Map properties = StandardFonts.getFontDetails(type, loc);
                        if (properties != null) {

                            for (final Object key : properties.keySet()) {
                                final Object value = properties.get(key);
                                final TreeItem fontDetails = new TreeItem(key + " = " + value);
                                FontTop.getChildren().add(fontDetails);

                            }
                        }
                    }
                }
            } else {//Show all fonts in one list

                //build display
                for (int ii = 0; ii < fontCount; ii++) {
                    final Object nextFont = fontNames.get(ii);

                    if (filter == null || ((String) nextFont).toLowerCase().contains(filter.toLowerCase())) {
                        final TreeItem fontTop = new TreeItem(nextFont + " = " + FontMappings.fontSubstitutionLocation.get(nextFont));
                        top.getChildren().add(fontTop);

                        //add details
                        final Map properties = (Map) FontMappings.fontPropertiesTable.get(nextFont);
                        if (properties != null) {

                            for (final Object key : properties.keySet()) {
                                final Object value = properties.get(key);
                                final TreeItem fontDetails = new TreeItem(key + " = " + value);
                                fontTop.getChildren().add(fontDetails);

                            }
                        }
                    }
                }
            }
        }
        return top;
    }

    private static void getAvailableFonts(final TextField filterText, final boolean sortFontsByDir){
        TreeItem fontlist = new TreeItem("Fonts");
        fontlist = populateAvailableFonts(fontlist, filterText.getText(),sortFontsByDir);
        treeContainer.getChildren().clear();
        treeContainer.getChildren().add(new TreeView(fontlist));
    }
    
}
