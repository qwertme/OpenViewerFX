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
 * JavaFXPreferences.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.JavaFxGUI;
import org.jpedal.examples.viewer.gui.javafx.FXViewerTransitions;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXMessageDialog;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXOptionDialog;

import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.external.Options;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.Speech;
import org.jpedal.objects.javascript.DefaultParser;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.mozilla.javascript.ScriptRuntime;
import org.w3c.dom.NodeList;

/**
 * This Class Sets Up and Displays the Preference Window Which Allows the User
 * to Customise their JavaFX PDF Viewer Experience.
 */
public class JavaFXPreferences {

    private static final Map reverseMessage = new HashMap();
    private static final String[] menuTabs = {"ShowMenubar", "ShowButtons", "ShowDisplayoptions", "ShowNavigationbar", "ShowSidetabbar"};
    private static FXDialog preferenceDialog;
    private static final int contentGap = 10; //The vertical & padding space between objects
    private static final BorderPane borderPane = new BorderPane();
    private static final Font titleFont = Font.font("SansSerif", FontWeight.BOLD, 14);
    private static final Text title = new Text(Messages.getMessage("PdfPreferences.GeneralSection"));
    private static ScrollPane contentScrollPane;
    private static PropertiesFile properties;


    private static final int GENERAL = 0;
    private static final int PAGEDISPLAY = 1;
    private static final int INTERFACE = 2;
    private static final int COLOR = 3;
    private static final int MENU = 4;
    private static final int PRINTING = 5;
    private static final int EXTENSION = 6;

    public static void execute(final Object[] args, final GUIFactory currentGUI) {
        if (args == null) {
            showPreferenceWindow(currentGUI);
        } else {

        }
    }

    /**
     * Declare Objects for General Menu.
     */
    private static TextField resolutionTF;
    private static CheckBox trueTypeCB;
    private static CheckBox autoScrollCB;
    private static CheckBox confirmCloseCB;
    private static CheckBox checkUpdatesCB;
    private static CheckBox openDocCB;
    private static Button clearRecentDocsBtn;

    /**
     * Declare Objects for Page Layout Menu.
     */
    private static CheckBox enhancedViewerCB;
    private static CheckBox showBorderCB;
    private static TextField pageInsetsTF;
    private static ComboBox displayCombo;
    private static CheckBox enablePageFlipCB;
    private static CheckBox scrollableThumbsCB;

    /**
     * Declare Objects for Interface Meu.
     */
    private static TextField winTitleTF;
    private static TextField iconLocTF;
    private static ComboBox searchStyle;
    private static TextField maxViewerTF;
    private static TextField sideTabTF;
    private static CheckBox consistentSideTabCB;
    private static CheckBox rightClickCB;
    private static CheckBox wheelZoomCB;
    private static CheckBox mouseSelectCB;
    private static ComboBox voiceSelect;
    private static ComboBox transitionSelect;
    
    /**
     * Declare Objects for Color Menu.
     */
    private static ColorPicker highlightsPicker;
    private static TextField highlightTF;
    private static CheckBox invertHighlightsCB;
    private static ColorPicker pageColorPicker;
    private static CheckBox replaceTextColorCB;
    private static ColorPicker textColorPicker;
    private static CheckBox changeLineArtColorCB;
    private static CheckBox replaceDisplayCB;
    private static ColorPicker displayBGColorPicker;
    
    /**
     * Declare Objects for Printing Menu.
     */
    private static CheckBox hiResPrintingCB;
    private static ComboBox printerCombo=new ComboBox();
    private static ComboBox paperSizesCombo=new ComboBox();
    private static TextField defaultDPITF;
    private static TextField blackListTF;
    private static TabPane tabs;
    
    //Text to Speech external handler
    private static Speech speech;
    
    private static void init(final GUIFactory currentGUI) {
        speech = (Speech)currentGUI.getPdfDecoder().getExternalHandler(Options.SpeechEngine);
        
        /**
         * Initialise Objects for General Menu.
         */
        resolutionTF = new TextField();
        trueTypeCB = new CheckBox(Messages.getMessage("PdfCustomGui.useHinting"));
        autoScrollCB = new CheckBox(Messages.getMessage("PdfViewerViewMenuAutoscrollSet.text"));
        confirmCloseCB = new CheckBox(Messages.getMessage("PfdViewerViewMenuConfirmClose.text"));
        checkUpdatesCB = new CheckBox(Messages.getMessage("PdfPreferences.CheckForUpdate"));
        openDocCB = new CheckBox(Messages.getMessage("PdfViewerViewMenuOpenLastDoc.text"));
        clearRecentDocsBtn = new Button(Messages.getMessage("PageLayoutViewMenu.ClearHistory"));
        clearRecentDocsBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                currentGUI.getRecentDocument().clearRecentDocuments(currentGUI.getProperties());
            }
        });

        /**
         * Initialise Objects for Page Layout Menu.
         */
        enhancedViewerCB = new CheckBox(Messages.getMessage("PdfCustomGui.enhancedViewer"));
        showBorderCB = new CheckBox(Messages.getMessage("PageLayoutViewMenu.Borders_Show"));
        pageInsetsTF = new TextField();
        final ObservableList<String> pageOptions
                = FXCollections.observableArrayList(
                        Messages.getMessage("PageLayoutViewMenu.SinglePage"),
                        Messages.getMessage("PageLayoutViewMenu.Continuous"),
                        Messages.getMessage("PageLayoutViewMenu.Facing"),
                        Messages.getMessage("PageLayoutViewMenu.ContinousFacing"),
                        Messages.getMessage("PageLayoutViewMenu.PageFlow")
                );
        displayCombo = new ComboBox(pageOptions);
        enablePageFlipCB = new CheckBox(Messages.getMessage("PdfCustomGui.enhancedFacing"));
        scrollableThumbsCB = new CheckBox(Messages.getMessage("PdfCustomGui.thumbnailScroll"));

        /**
         * Initialise Objects for Interface Menu.
         */
        winTitleTF = new TextField();
        iconLocTF = new TextField();
        final ObservableList<String> layoutOptions;
        if (currentGUI.isSingle()) {
            layoutOptions = FXCollections.observableArrayList(
                    Messages.getMessage("PageLayoutViewMenu.WindowSearch"),
                    Messages.getMessage("PageLayoutViewMenu.TabbedSearch"),
                    Messages.getMessage("PageLayoutViewMenu.MenuSearch")
            );
        } else {
            layoutOptions = FXCollections.observableArrayList(
                    Messages.getMessage("PageLayoutViewMenu.WindowSearch"),
                    Messages.getMessage("PageLayoutViewMenu.TabbedSearch")
            );
        }
        searchStyle = new ComboBox(layoutOptions);
        maxViewerTF = new TextField();
        sideTabTF = new TextField();
        consistentSideTabCB = new CheckBox(Messages.getMessage("PdfCustomGui.consistentTabs"));
        rightClickCB = new CheckBox(Messages.getMessage("PdfCustomGui.allowRightClick"));
        wheelZoomCB = new CheckBox(Messages.getMessage("PdfCustomGui.allowScrollwheelZoom"));
        mouseSelectCB = new CheckBox("Show Mouse Selection Box");
        if (speech!=null) {
            final List<String> availableVoices = new ArrayList<String>(Arrays.asList(speech.listVoices()));
            final ObservableList<String> speechOptions = FXCollections.observableList(availableVoices);
            voiceSelect = new ComboBox(speechOptions);
        } else {
            final ObservableList<String> speechOptions = FXCollections.observableArrayList("No Voice Options Detected");
            voiceSelect = new ComboBox(speechOptions);
        }
        
        // Set up transition list
        final List<String> transitions = new ArrayList<String>();
        for(final FXViewerTransitions.TransitionType s : FXViewerTransitions.TransitionType.values()){
            transitions.add(s.name().replace("_", " "));
        }
        final ObservableList<String> transitionOptions = FXCollections.observableArrayList(transitions);
        transitionSelect = new ComboBox(transitionOptions);
        transitionSelect.getSelectionModel().select(0);
        
        /**
         * Initialise Objects for Colour Menu.
         */
        /**
         * Initialise Objects for Menu Menu.
         */
        tabs = new TabPane();
        
        /**
         * Initialise Objects for Printing Menu.
         */
        hiResPrintingCB = new CheckBox(Messages.getMessage("Printing.HiRes"));

        //

        defaultDPITF = new TextField();
        blackListTF = new TextField();

    }

    /**
     * Ensure Dialog is Setup & Display Preference Dialog.
     *
     * @param swingGUI
     */
    private static void showPreferenceWindow(final GUIFactory currentGUI) {

        properties = currentGUI.getProperties();
        init(currentGUI);
        loadSettings();
        borderPane.setLeft(setupSideNavBar(currentGUI));     //Add the Side Menu Bar.
        borderPane.setCenter(getGeneralContent()); //Set General as the Default.
        borderPane.setBottom(setupBottomBar(currentGUI));    //Add the Bottom Buttons.

        /**
         * Finalise Stage Setup.
         */
        preferenceDialog = new FXDialog(null, Modality.APPLICATION_MODAL, borderPane, 550, 450);
        preferenceDialog.setTitle(Messages.getMessage("PdfPreferences.windowTitle"));
        preferenceDialog.show();
    }

    /**
     * Sets up the Side Menu of the Preference Window.
     *
     * @return ScrollPane Object
     */
    private static ScrollPane setupSideNavBar(final GUIFactory currentGUI) {

        /**
         * Setup SideBar Buttons.
         */
        final VBox vBox = new VBox();
        final ToggleGroup buttonsGroup = new ToggleGroup();
        final ToggleButton[] buttonsArr = {
            new ToggleButton(Messages.getMessage("PdfPreferences.GeneralTitle"), new ImageView(new Image("/org/jpedal/examples/viewer/res/display.png"))),
            new ToggleButton(Messages.getMessage("PdfPreferences.PageDisplayTitle"), new ImageView(new Image("/org/jpedal/examples/viewer/res/pagedisplay.png"))),
            new ToggleButton(Messages.getMessage("PdfPreferences.InterfaceTitle"), new ImageView(new Image("/org/jpedal/examples/viewer/res/interface.png"))),
            new ToggleButton("Color", new ImageView(new Image("/org/jpedal/examples/viewer/res/color.png"))),
            new ToggleButton("Menu", new ImageView(new Image("/org/jpedal/examples/viewer/res/menu.png"))),
            new ToggleButton(Messages.getMessage("PdfPreferences.PrintingTitle"), new ImageView(new Image("/org/jpedal/examples/viewer/res/printing.png"))),
            new ToggleButton(Messages.getMessage("PdfPreferences.ExtensionsTitle"), new ImageView(new Image("/org/jpedal/examples/viewer/res/extensions.png")))
        };
        
        buttonsArr[0].setSelected(true);
        for (final ToggleButton aButtonsArr : buttonsArr) {
            aButtonsArr.setContentDisplay(ContentDisplay.TOP);
            aButtonsArr.setTextFill(Color.BLACK);
            aButtonsArr.setStyle("-fx-base: transparent;");
            aButtonsArr.setToggleGroup(buttonsGroup);
        }

        vBox.getChildren().addAll(buttonsArr);
        vBox.setAlignment(Pos.CENTER);

        /**
         * Add SideBar Button Listeners.
         */
        buttonsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(final ObservableValue<? extends Toggle> ov,
                    final Toggle toggle, final Toggle new_toggle) {

                for (int i = 0; i < buttonsArr.length; ++i) {
                    if (buttonsGroup.getSelectedToggle() == buttonsArr[i]) {
                        /**
                         * Update the Current Main Content.
                         */
                        updateDisplay(i, currentGUI);
                    }
                }

            }
        });

        /**
         * Setup SideBar.
         */
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vBox);

        return scrollPane;
    }

    /**
     * This method checks what the current menu selection is and updates the
     * main content window accordingly.
     *
     * @param menuSelection is of type int
     */
    private static void updateDisplay(final int menuSelection, final GUIFactory currentGUI) {



        switch (menuSelection) {

            case GENERAL:
                borderPane.setCenter(getGeneralContent());
                break;
            case PAGEDISPLAY:
                borderPane.setCenter(getPageDisplayContent());
                break;
            case INTERFACE:
                borderPane.setCenter(getInterfaceContent(currentGUI));
                break;
            case COLOR:
                borderPane.setCenter(getColorContent());
                break;
            case MENU:
                borderPane.setCenter(getMenuContent());
                break;
            case PRINTING:
                borderPane.setCenter(getPrintingContent());
                break;
            case EXTENSION:
                borderPane.setCenter(getExtensionContent());
                break;
            default:
                System.out.println("menu selection not available");
                break;

        }

    }

    /**
     * This method sets up the bottom of the preferences window, It adds Reset,
     * OK, Save As and Cancel Buttons.
     *
     * @return HBox Object
     */
    private static HBox setupBottomBar(final GUIFactory currentGUI) {

        /**
         * Setup Buttons.
         */
        final Button resetBtn = new Button(Messages.getMessage("PdfPreferences.ResetToDefault"));
        final Button okBtn = new Button("OK");
        final Button saveAsBtn = new Button(Messages.getMessage("PdfPreferences.SaveAs"));
        final Button cancelBtn = new Button("Cancel");

        /**
         * Setup HBox.
         */
        final HBox hBox = new HBox();
        hBox.setPadding(new Insets(8));
        hBox.setSpacing(8);
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hBox.getChildren().addAll(resetBtn, spacer, okBtn, saveAsBtn, cancelBtn);

        /**
         * Setup Button Listeners.
         */
        okBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent e) {
                updateSettings(currentGUI);
                preferenceDialog.close();
            }
        });
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent e) {
                preferenceDialog.close();
            }
        });
        saveAsBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent e) {
                //The properties file used when jpedal opened
                final String lastProperties = currentGUI.getPropertiesFileLocation();
                
                /**
                 * Setup File Chooser.
                 */
                final FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML File", "*.xml"));
                chooser.setInitialFileName("*.xml");
                
                final File xmlFile = chooser.showSaveDialog(null);
                
                /**
                 * Start Save Protocol.
                 */
                if(xmlFile != null){
                    currentGUI.setPropertiesFileLocation(xmlFile.getAbsolutePath());
                    updateSettings(currentGUI);
                    
                    try {
                		properties.writeDoc();
                	} catch (final Exception e1) {
                		//<start-demo><end-demo>
                                
                             if(LogWriter.isOutput()) { 
                                 LogWriter.writeLog("Exception attempting to Write proterties: " + e1); 
                             }    
                	}
                }
                
                /**
                 * Reset Location.
                 */
                currentGUI.setPropertiesFileLocation(lastProperties);
            }
        });
        resetBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent e) {
                final int result = new FXOptionDialog(preferenceDialog.getDialog(), Messages.getMessage("PdfPreferences.reset"), "Reset to Default", FXOptionDialog.YES_NO_OPTION, null, null).showOptionDialog();
                //The properties file used when jpedal opened
                if (result == FXOptionDialog.YES_OPTION) {
                    final String lastProperties = currentGUI.getPropertiesFileLocation();
                    
                    final File f = new File(lastProperties);
                    if (f.exists()) {
                        f.delete();
                    }
                    properties.loadProperties(lastProperties);
                    try {
                        properties.writeDoc();
                    } catch (final Exception e2) {
                        //<start-demo><end-demo>
                        
                         if(LogWriter.isOutput()) { 
                                 LogWriter.writeLog("Exception attempting to Write proterties: " + e2); 
                             } 
                    }
                    if (GUI.showMessages) {

                        new FXMessageDialog(preferenceDialog.getDialog(), Modality.APPLICATION_MODAL, Messages.getMessage("PdfPreferences.restart")).showAndWait();
                    }
                    preferenceDialog.close();
                }
            }
        });

        return hBox;
    }

    /**
     * Updates Main Content to General Content.
     *
     * @return ScrollPane Object
     */
    private static ScrollPane getGeneralContent() {

        contentScrollPane = new ScrollPane();
        final VBox contentVBox = new VBox();
        title.setText(Messages.getMessage("PdfPreferences.GeneralTitle"));
        title.setFont(titleFont); //only needs to be set once.

        /**
         * Setup General Options.
         */
        final Text generalOptions = new Text(Messages.getMessage("PdfPreferences.GeneralSection"));
        generalOptions.setFont(titleFont);

        final HBox resolutionHBox = new HBox();
        resolutionHBox.getChildren().addAll(new Label(Messages.getMessage("PdfViewerViewMenu.Resolution")), resolutionTF);

        /**
         * Setup Startup Options.
         */
        final Text startupOptions = new Text(Messages.getMessage("PdfPreferences.StartUp"));
        startupOptions.setFont(titleFont);

        contentVBox.getChildren().addAll(title, generalOptions, resolutionHBox, trueTypeCB, autoScrollCB, confirmCloseCB,
                startupOptions, checkUpdatesCB, openDocCB, clearRecentDocsBtn);

        contentVBox.setPadding(new Insets(contentGap));
        contentVBox.setSpacing(contentGap);

        /**
         * Finalise Containers.
         */
        contentScrollPane.setContent(contentVBox);
        return contentScrollPane;
    }

    /**
     * Updates Main Content to Page Display Content.
     *
     * @return ScrollPane Object
     */
    private static ScrollPane getPageDisplayContent() {
        contentScrollPane = new ScrollPane();
        final VBox contentVBox = new VBox();
        title.setText(Messages.getMessage("PdfPreferences.PageDisplayTitle"));

        /**
         * Setup General Options.
         */
        final Text generalOptions = new Text(Messages.getMessage("PdfPreferences.GeneralSection"));
        generalOptions.setFont(titleFont);

        final HBox pageInsetsHBox = new HBox();

        pageInsetsHBox.getChildren().addAll(new Label(Messages.getMessage("PdfViewerViewMenu.pageInsets")), pageInsetsTF);

        /**
         * Setup Display Modes Options.
         */
        final Text displayOptions = new Text(Messages.getMessage("PdfPreferences.DisplayModes"));
        displayOptions.setFont(titleFont);

        final HBox displayModeHBox = new HBox();
        displayModeHBox.getChildren().addAll(new Label(Messages.getMessage("PageLayoutViewMenu.PageLayout")), displayCombo);

        contentVBox.getChildren().addAll(title, generalOptions, enhancedViewerCB, showBorderCB, pageInsetsHBox, displayOptions, displayModeHBox, enablePageFlipCB, scrollableThumbsCB);
        contentVBox.setPadding(new Insets(contentGap));
        contentVBox.setSpacing(contentGap);

        /**
         * Finalise Containers.
         */
        contentScrollPane.setContent(contentVBox);
        return contentScrollPane;
    }

    /**
     * Updates Main Content to Interface Content.
     *
     * @return ScrollPane Object
     */
    private static ScrollPane getInterfaceContent(final GUIFactory currentGUI) {
        contentScrollPane = new ScrollPane();
        title.setText(Messages.getMessage("PdfPreferences.InterfaceTitle"));

        /**
         * Setup the TabPane.
         */
        final TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        /**
         * Setup the Appearance Tab.
         */
        final Tab appearance = new Tab(Messages.getMessage("PdfPreferences.AppearanceTab"));
        final VBox appearanceVBox = new VBox();

        final Text appGenOptions = new Text(Messages.getMessage("PdfPreferences.GeneralSection"));
        appGenOptions.setFont(titleFont);

        final HBox winTitleHBox = new HBox();
        winTitleHBox.getChildren().addAll(new Label(Messages.getMessage("PdfCustomGui.windowTitle")), winTitleTF);

        final HBox iconLocHBox = new HBox();
        iconLocHBox.getChildren().addAll(new Label(Messages.getMessage("PdfViewerViewMenu.iconLocation")), iconLocTF);

        final HBox searchStyleHBox = new HBox();
        searchStyleHBox.getChildren().addAll(new Label(Messages.getMessage("PageLayoutViewMenu.SearchLayout")), searchStyle);

        final HBox maxViewersHBox = new HBox();
        maxViewersHBox.getChildren().addAll(new Label(Messages.getMessage("PdfPreferences.MaxMultiViewers")), maxViewerTF);

        final Text appSideTabOptions = new Text(Messages.getMessage("PdfPreferences.SideTab"));
        appSideTabOptions.setFont(titleFont);

        final HBox sideTabHBox = new HBox();
        sideTabHBox.getChildren().addAll(new Label(Messages.getMessage("PdfCustomGui.SideTabLength")), sideTabTF);
        
        final HBox transitionHBox = new HBox();
        transitionHBox.getChildren().addAll(new Label("Transition type: "), transitionSelect);
        
        appearanceVBox.getChildren().addAll(appGenOptions, winTitleHBox, iconLocHBox, searchStyleHBox, maxViewersHBox, appSideTabOptions, sideTabHBox, consistentSideTabCB, transitionHBox);
        appearanceVBox.setPadding(new Insets(contentGap));
        appearanceVBox.setSpacing(contentGap);
        appearance.setContent(appearanceVBox);

        /**
         * Setup the Mouse Tab.
         */
        final Tab mouse = new Tab(Messages.getMessage("PdfPreferences.Mouse"));
        final VBox mouseVBox = new VBox();

        final Text mouseGenOptions = new Text(Messages.getMessage("PdfPreferences.GeneralSection"));
        mouseGenOptions.setFont(titleFont);

        mouseVBox.getChildren().addAll(mouseGenOptions, rightClickCB, wheelZoomCB, mouseSelectCB);
        mouseVBox.setPadding(new Insets(contentGap));
        mouseVBox.setSpacing(contentGap);
        mouse.setContent(mouseVBox);

        /**
         * Setup the Speech Tab.
         */
        final Tab speech = new Tab(Messages.getMessage("PdfPreferences.Voice"));
        final VBox speechVBox = new VBox();

        final Text speechGenOptions = new Text(Messages.getMessage("PdfPreferences.GeneralSection"));
        speechGenOptions.setFont(titleFont);

        speechVBox.getChildren().addAll(speechGenOptions, voiceSelect);
        speechVBox.setPadding(new Insets(contentGap));
        speechVBox.setSpacing(contentGap);
        speech.setContent(speechVBox);

        /**
         * Finalise Containers.
         */
        tabPane.getTabs().addAll(appearance, mouse, speech);
        final VBox contentVBox = new VBox();
        contentVBox.setPadding(new Insets(contentGap));
        contentVBox.setSpacing(contentGap);
        contentVBox.getChildren().addAll(title, tabPane);
        contentScrollPane.setContent(contentVBox);
        return contentScrollPane;
    }

    /**
     * Updates Main Content to Color Content.
     *
     * @return ScrollPane Object
     */
    private static ScrollPane getColorContent() {
        
        contentScrollPane = new ScrollPane();
        title.setText("Color");
        final VBox contentVBox = new VBox();
        /**
         * Setup Highlights Option.
         */
        final Text highlightsTitle = new Text("Highlights");
        highlightsTitle.setFont(titleFont);

        final HBox highlightsHBox = new HBox();
        final Label changeHighlightsLabel = new Label(Messages.getMessage("PdfPreferences.ChangeHighlightColor") + ' ');
        
        highlightsHBox.getChildren().addAll(changeHighlightsLabel, highlightsPicker);

        //Setup the highlights transparency text field
        final HBox transparencyHBox = new HBox();
        final Label highlightLabel = new Label(Messages.getMessage("PdfPreferences.ChangeHighlightTransparency") + ' ');
        transparencyHBox.getChildren().addAll(highlightLabel,highlightTF);

        //Setup the invert highlights checkbox
        final String invertHighlights = properties.getValue("invertHighlights");
        if(!invertHighlights.isEmpty() && invertHighlights.equalsIgnoreCase("true")){
            transparencyHBox.setDisable(true);
            highlightsHBox.setDisable(true);
        }else{
            transparencyHBox.setDisable(false);
            highlightsHBox.setDisable(false);
        }

        invertHighlightsCB.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent e) {
                if(invertHighlightsCB.isSelected()){
                    transparencyHBox.setDisable(true);
                    highlightsHBox.setDisable(true);
                }else{
                    transparencyHBox.setDisable(false);
                    highlightsHBox.setDisable(false);
                }
            }
        });

        /**
         * Setup Display Colors Option.
         */
        final Text displayTitle = new Text("Display Colors");
        displayTitle.setFont(titleFont);
        
        //Setup Change Page Color
        final HBox displayColorsHBox = new HBox();
        final Label pageColorLabel = new Label(Messages.getMessage("PdfPreferences.ChangeBackgroundColor") + ' ');
        
        
        displayColorsHBox.getChildren().addAll(pageColorLabel, pageColorPicker);

        //Setup Change Text Color

        final HBox textColorHB = new HBox();
        final Label textColorLabel = new Label(Messages.getMessage("PdfPreferences.ChangeForegroundColor") + ' ');
        
        
        textColorHB.getChildren().addAll(textColorLabel, textColorPicker);


        //Setup Replace Text Color CheckBox
        final String replaceTextCol = properties.getValue("replaceDocumentTextColors");
        if (!replaceTextCol.isEmpty() && replaceTextCol.equalsIgnoreCase("true")) {
            replaceTextColorCB.setSelected(true);
            textColorHB.setDisable(false);
            changeLineArtColorCB.setDisable(false);
        } else {
            replaceTextColorCB.setSelected(false);
            textColorHB.setDisable(true);
            changeLineArtColorCB.setDisable(true);
        }
        replaceTextColorCB.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent e) {
                if(replaceTextColorCB.isSelected()){
                    textColorHB.setDisable(false);
                    changeLineArtColorCB.setDisable(false);
                }else{
                    textColorHB.setDisable(true);
                    changeLineArtColorCB.setDisable(true);
                }
            }
        });


        //Setup the Display Background Color Button

        final HBox displayColorHB = new HBox();
        final Label displayColorLabel = new Label(Messages.getMessage("PdfPreferences.ChangeDisplayBackgroundColor") + ' ');

        
        
        displayColorHB.getChildren().addAll(displayColorLabel, displayBGColorPicker);


        //Setup Replace Display Background Color CheckBox
        final String replaceDisplayCol = properties.getValue("replacePdfDisplayBackground");
        if (!replaceDisplayCol.isEmpty() && replaceDisplayCol.equalsIgnoreCase("true")) {
            replaceDisplayCB.setSelected(true);
            displayColorHB.setDisable(false);

            //this code should enable or disable the changing of the center panes background colour
            //pdfDecoderBackground.setEnabled(true);
        } else {
            replaceDisplayCB.setSelected(false);
            displayColorHB.setDisable(true);

            //this code should enable or disable the changing of the center panes background colour
            //pdfDecoderBackground.setEnabled(false);
        }

        replaceDisplayCB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                if (replaceDisplayCB.isSelected()) {
                    displayColorHB.setDisable(false);

                    //this code should enable or disable the changing of the center panes background colour
                    //pdfDecoderBackground.setEnabled(true);
                } else {
                    displayColorHB.setDisable(true);

                    //this code should enable or disable the changing of the center panes background colour
                    //pdfDecoderBackground.setEnabled(false);
                }
            }
        });

        contentVBox.setPadding(new Insets(contentGap));
        contentVBox.setSpacing(contentGap);
        contentVBox.getChildren().addAll(title, highlightsTitle, highlightsHBox , transparencyHBox, invertHighlightsCB, displayTitle, displayColorsHBox, replaceTextColorCB,textColorHB,changeLineArtColorCB,replaceDisplayCB,displayColorHB);
        
        contentScrollPane.setContent(contentVBox);
        return contentScrollPane;
    }
    
    /**
     * Performs a Bitshift so we can use Swing get sRGB with JavaFX Color.
     *
     * @param raw of type int
     * @return Color
     */
    public static Color shiftColorSpaceToFX(final int raw) {
        final int r = ((raw >> 16) & 255);
        final int g = ((raw >> 8) & 255);
        final int b = ((raw) & 255);
        return Color.rgb(r, g, b);
    }

    /**
     * Performs a Conversion from 0-1 to 0-255 so we can perform a BitShift to
     * save the RGB values.
     *
     * @param newCol of type Color
     * @return int
     */
    private static int shiftColorSpaceToSwing(final Color newCol) {
        final int r = (int) (newCol.getRed() * 255);
        final int g = (int) (newCol.getGreen() * 255);
        final int b = (int) (newCol.getBlue() * 255);

        return (r << 16) + (g << 8) + b;
    }

    private static void addMenuToTree(final NodeList nodes, final CheckBoxTreeItem top, final List previous) {

        for (int i = 0; i != nodes.getLength(); i++) {

            if (i < nodes.getLength()) {
                final String name = nodes.item(i).getNodeName();

                if (removeOption(name)) {
                    //Ignore this item
                } else if (!name.startsWith("#")) {
                    //Node to add
                    final CheckBoxTreeItem<String> newLeaf = new CheckBoxTreeItem<String>(Messages.getMessage("PdfCustomGui." + name));

                    //Set to reversedMessage for saving of preferences
                    reverseMessage.put(Messages.getMessage("PdfCustomGui." + name), name);
                    final String propValue = properties.getValue(name);
                    //Set if should be selected
                    if (!propValue.isEmpty() && propValue.equals("true")) {
                        newLeaf.setSelected(true);
                    } else {
                        newLeaf.setSelected(false);
                    }

                    if (name.equals("Preferences")) {
                        newLeaf.selectedProperty().addListener(new ChangeListener() {

                            @Override
                            public void changed(final ObservableValue ov, final Object t, final Object t1) {
                                if (!newLeaf.isSelected()) {
                                    final int result = new FXOptionDialog(null, "Disabling this option will mean you can not acces this menu using this properties file. Do you want to continue?", "Preferences Access", FXOptionDialog.YES_NO_OPTION, null, null).showOptionDialog();
                                    if (result == FXOptionDialog.NO_OPTION) {
                                        newLeaf.setSelected(true);
                                    }
                                }
                            }

                        });
                    }

                    //If has child nodes
                    if (nodes.item(i).hasChildNodes()) {
                        //Store this top value
                        previous.add(top);
                        //Set this node to ned top
                        top.getChildren().add(newLeaf);
                        //Add new menu to tree
                        addMenuToTree(nodes.item(i).getChildNodes(), newLeaf, previous);
                    } else {
                        //Add to current top
                        top.getChildren().add(newLeaf);
                    }
                }
            }
        }
    }

    private static boolean removeOption(final String name) {

        //
        //Remove help button as it is not in use in gpl version
        if(name.equals("Helpbutton")){
            return true;
        }

        //Remove rss button as it is not in use in gpl version
        if(name.equals("RSSbutton")){
            return true;
        }
        /**/

        //
        //Remove help menu item as it is not in use in gpl or full version
        if(name.equals("Helpforum")){
            return true;
        }
        /**/

        return false;
    }

    /**
     * Updates Main Content to Menu Content.
     *
     * @return ScrollPane Object
     */
    private static ScrollPane getMenuContent() {
        contentScrollPane = new ScrollPane();

        final VBox contentVBox = new VBox();

        title.setText("Menu");
        tabs.getTabs().clear(); //empty the tabs from the list
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        for (int t = 0; t != menuTabs.length; t++) {
            reverseMessage.put(Messages.getMessage("PdfCustomGui." + menuTabs[t]), menuTabs[t]);
            final CheckBoxTreeItem<String> top = new CheckBoxTreeItem<String>(Messages.getMessage("PdfCustomGui." + menuTabs[t]));
            top.setSelected(true);

            final ArrayList last = new ArrayList();
            last.add(top);

            final NodeList nodes = properties.getChildren(Messages.getMessage("PdfCustomGui." + menuTabs[t]) + "Menu");
            addMenuToTree(nodes, top, last);
            top.setExpanded(true);

            final TreeView<String> tree = new TreeView<String>(top);

            tree.setCellFactory(CheckBoxTreeCell.<String>forTreeView());

            tabs.getTabs().addAll(new Tab(Messages.getMessage("PdfCustomGui." + menuTabs[t])));
            tabs.getTabs().get(t).setContent(tree);

        }

        contentVBox.setPadding(new Insets(contentGap));
        contentVBox.setSpacing(contentGap);
        contentVBox.getChildren().addAll(title, tabs);

        contentScrollPane.setContent(contentVBox);
        return contentScrollPane;
    }

    /**
     * Updates Main Content to Printing Content.
     *
     * @return ScrollPane Object
     */
    private static ScrollPane getPrintingContent() {
        contentScrollPane = new ScrollPane();
        title.setText(Messages.getMessage("PdfPreferences.PrintingTitle"));

        final VBox contentVBox = new VBox();

        /**
         * Setup General Options.
         */
        final Text generalOptions = new Text(Messages.getMessage("PdfPreferences.GeneralSection"));
        generalOptions.setFont(titleFont);

        /**
         * Setup Default Printer and Page Size Options.
         */
        final HBox printerHBox = new HBox();
        printerHBox.getChildren().addAll(new Label(Messages.getMessage("PdfViewerPrint.defaultPrinter")), printerCombo);

        final HBox pageSizeHBox = new HBox();
        pageSizeHBox.getChildren().addAll(new Label(Messages.getMessage("PdfViewerPrint.defaultPagesize")), paperSizesCombo);

        final HBox defaultDPIHBox = new HBox();
        defaultDPIHBox.getChildren().addAll(new Label(Messages.getMessage("PdfViewerPrint.defaultDPI")), defaultDPITF);

        final HBox blackListHBox = new HBox();
        blackListHBox.getChildren().addAll(new Label(Messages.getMessage("PdfViewerPrint.blacklist")), blackListTF);

        /**
         * Finalise Containers.
         */
        contentVBox.setPadding(new Insets(contentGap));
        contentVBox.setSpacing(contentGap);
        contentVBox.getChildren().addAll(title, generalOptions, hiResPrintingCB, printerHBox, pageSizeHBox, defaultDPIHBox, blackListHBox);
        contentScrollPane.setContent(contentVBox);
        return contentScrollPane;
    }

    /**
     * Updates Main Content to Extension Content.
     *
     * @return ScrollPane Object
     */
    private static ScrollPane getExtensionContent() {
        contentScrollPane = new ScrollPane();
        title.setText(Messages.getMessage("PdfPreferences.ExtensionsTitle"));

        //Setup Title & Main Container.
        final VBox contentVBox = new VBox();

        //Setup Extensions Grid.
        final GridPane contentGridPane = new GridPane();
        contentGridPane.getColumnConstraints().setAll(new ColumnConstraints(100), new ColumnConstraints(200)); //set width of grid columns
        final Text nameTitle = new Text(Messages.getMessage("PdfPreferences.ExtensionName"));
        final Text bcmailName = new Text("BCMail");
        final Text cidName = new Text("CID");
        final Text jceName = new Text("JCE");
        final Text rhinoName = new Text("Rhino");
        nameTitle.setFont(titleFont);

        final Text descriptionTitle = new Text(Messages.getMessage("PdfPreferences.ExtensionDescription"));
        final Text bcmailDescr = new Text(Messages.getMessage("PdfExtensions.BCMail.text"));
        final Text cidDescr = new Text(Messages.getMessage("PdfExtensions.CID.text"));
        final Text jceDescr = new Text(Messages.getMessage("PdfExtensions.JCE.text"));
        final Text rhinoDescr = new Text(Messages.getMessage("PdfExtensions.Rhino.text"));
        descriptionTitle.setFont(titleFont);

        final Text versionTitle = new Text(Messages.getMessage("PdfPreferences.ExtensionVersion"));
        versionTitle.setFont(titleFont);
        
        final Hyperlink bcmailVersion = new Hyperlink();
        String details = getBCMailVersion(bcmailVersion);

        final Hyperlink cidVersion = new Hyperlink();
        details += getCidVersion(cidVersion);
        
        final Hyperlink jceVersion = new Hyperlink();
        details += getJCEVersion(jceVersion);

        final Hyperlink rhinoVersion = new Hyperlink();
        details += getRhinoVersion(rhinoVersion);

        //Setup Copy Details Buttons.
        final Button copyBtn = createCopyDetailsButton(details);

        /**
         * Finalise Containers.
         */
        contentVBox.setPadding(new Insets(contentGap));
        contentVBox.setSpacing(contentGap);
        contentVBox.getChildren().addAll(title, contentGridPane, copyBtn);
        contentScrollPane.setContent(contentVBox);
        
        int y = 0;
        addLineToExtensionGrid(contentGridPane, nameTitle, descriptionTitle, versionTitle, y);
        y++;
        addLineToExtensionGrid(contentGridPane, bcmailName, bcmailDescr, bcmailVersion, y);
        y++;
        addLineToExtensionGrid(contentGridPane, cidName, cidDescr, cidVersion, y);
        y++;
        addLineToExtensionGrid(contentGridPane, jceName, jceDescr, jceVersion, y);
        y++;
        addLineToExtensionGrid(contentGridPane, rhinoName, rhinoDescr, rhinoVersion, y);
        
        return contentScrollPane;
    }
    
    private static Button createCopyDetailsButton(String details){
        //Setup Copy Details Buttons.
        final Button copyBtn = new Button(Messages.getMessage("PdfPreferences.CopyToClipboard"));

        final String finalDetails = "java: " + System.getProperty("java.vendor") + ' ' + System.getProperty("java.version") + '\n'
            + "os: " + System.getProperty("os.name") + ' ' + System.getProperty("os.version") + ' ' + System.getProperty("os.arch") + '\n'
            + "jpedal: " + PdfDecoderInt.version + '\n' + details;
        copyBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                final FXMessageDialog dialog = new FXMessageDialog(null, Modality.APPLICATION_MODAL, Messages.getMessage("PdfExtensions.clipboard"));
                dialog.show();
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString(finalDetails);
                clipboard.setContent(clipboardContent);
            }
        });
        return copyBtn;
    }
    
    private static String getRhinoVersion(final Hyperlink versionNode){
        String details = "";
        final java.io.InputStream in = DefaultParser.class.getClassLoader().getResourceAsStream("org/mozilla/javascript/Context.class");
        if (in != null) {
            String version = ScriptRuntime.getMessage0("implementation.version");
            details += "rhino: " + version + '\n';

            String release = "";
            if (!version.replaceAll("release 1", "").equals(version)) {
                release = " R1";
            }
            if (!version.replaceAll("release 2", "").equals(version)) {
                release = " R2";
            }

            version = version.substring(0, 12).replaceAll("[^0-9|.]", "");
            versionNode.setText(version + release);
        } else {
            versionNode.setText(Messages.getMessage("PdfExtensions.getText"));

            versionNode.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    try {
                        BrowserLauncher.openURL(Messages.getMessage("PdfExtensions.Rhino.link"));
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        return details;
    }
    
    private static String getJCEVersion(final Hyperlink versionNode){
        String details = "";
        String version = "Unknown Version";
        try {
            final Class jcec = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
            final String className = jcec.getName().replace('.', '/');
            final String[] paths = jcec.getResource('/' + className + ".class").getPath().split("!");
            final URL file = new URL(paths[0]);
            final JarFile jar = new JarFile(file.getFile());
            if (!jar.getManifest().getMainAttributes().getValue("Implementation-Version").isEmpty()) {
                version = jar.getManifest().getMainAttributes().getValue("Implementation-Version");
            }
            versionNode.setText(version);
            details += "jce: " + version + '\n';
        } catch (final Exception e) {
            versionNode.setText(Messages.getMessage("PdfExtensions.getText")+' '+e);

            versionNode.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    try {
                        BrowserLauncher.openURL(Messages.getMessage("PdfExtensions.JCE.link"));
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        return details;
    }
    
    private static String getCidVersion(final Hyperlink versionNode){
        String details = "";
        try {
            if (JavaFXPreferences.class.getResourceAsStream("/org/jpedal/res/cid/00_ReadMe.pdf") != null) {
                versionNode.setText("1.0");
                details += "cid: 1.0\n";
            } else {
                versionNode.setText(Messages.getMessage("PdfExtensions.getText"));
                versionNode.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        try {
                            BrowserLauncher.openURL(Messages.getMessage("PdfExtensions.CID.link"));
                        } catch (final Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        } catch (final Exception ee) {
            ee.printStackTrace();
        }
        return details;
    }

    private static String getBCMailVersion(final Hyperlink versionNode){
        String details = "";
        String version = "Unknown Version";
        try {
            final Class bcmailc = Class.forName("org.bouncycastle.jcajce.JcaJceHelper");
            final String className = bcmailc.getName().replace('.', '/');
            final String[] paths = bcmailc.getResource('/' + className + ".class").getPath().split("!");
            final URL file = new URL(paths[0]);
            final JarFile jar = new JarFile(file.getFile());
            if (!jar.getManifest().getMainAttributes().getValue("Implementation-Version").isEmpty()) {
                version = jar.getManifest().getMainAttributes().getValue("Implementation-Version");
            }
            versionNode.setText(version);
            details += "bcmail: " + version + '\n';
        } catch (final Exception e) {
            versionNode.setText(Messages.getMessage("PdfExtensions.getText")+' '+e);
            versionNode.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    try {
                        BrowserLauncher.openURL(Messages.getMessage("PdfExtensions.BCMail.link"));
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        
        return details;
    }
    
    private static void addLineToExtensionGrid(GridPane contentGridPane, Node name, Node desc, Node value, int y){
        contentGridPane.add(name, 0, y);
        contentGridPane.add(desc, 1, y);
        contentGridPane.add(value, 2, y);
    }
    
    private static void updateSettings(final GUIFactory currentGUI) {

        /**
         * Update General Settings.
         */
        properties.setValue("resolution", String.valueOf(resolutionTF.getText()));
        properties.setValue("useHinting", String.valueOf(trueTypeCB.isSelected()));
        properties.setValue("autoScroll", String.valueOf(autoScrollCB.isSelected()));
        properties.setValue("confirmClose", String.valueOf(confirmCloseCB.isSelected()));
        properties.setValue("automaticupdate", String.valueOf(checkUpdatesCB.isSelected()));
        properties.setValue("openLastDocument", String.valueOf(openDocCB.isSelected()));
        //clearRecentDocsBtn.isSelected();

        /**
         * Update Page Display Settings.
         */
        properties.setValue("enhancedViewerMode", String.valueOf(enhancedViewerCB.isSelected()));
        // properties.setValue("borderType", String.valueOf(showBorderCB.isSelected()));
        properties.setValue("pageInsets", String.valueOf(pageInsetsTF.getText()));
        // properties.setValue("startView", String.valueOf(displayCombo.getValue()));
        properties.setValue("enhancedFacingMode", String.valueOf(enablePageFlipCB.isSelected()));
        properties.setValue("previewOnSingleScroll", String.valueOf(scrollableThumbsCB.isSelected()));

        /**
         * Update Interface Settings.
         */
        properties.setValue("windowTitle", String.valueOf(winTitleTF.getText()));
        String loc = iconLocTF.getText();
        if (!loc.endsWith("/") && !loc.endsWith("\\")) {
            loc += '/';
        }
        properties.setValue("iconLocation", String.valueOf(loc));
        properties.setValue("searchWindowType", Integer.toString(searchStyle.getItems().indexOf(searchStyle.getValue())));
        properties.setValue("maxmultiviewers", String.valueOf(maxViewerTF.getText()));
        properties.setValue("sideTabBarCollapseLength", String.valueOf(sideTabTF.getText()));
        properties.setValue("consistentTabBar", String.valueOf(consistentSideTabCB.isSelected()));
        properties.setValue("allowRightClick", String.valueOf(rightClickCB.isSelected()));
        properties.setValue("allowScrollwheelZoom", String.valueOf(wheelZoomCB.isSelected()));
        properties.setValue("showMouseSelectionBox", String.valueOf(mouseSelectCB.isSelected()));
        properties.setValue("transitionType", transitionSelect.getSelectionModel().getSelectedItem().toString());
        ((JavaFxGUI)currentGUI).updateTransitionType();
        if (speech!=null) {
            properties.setValue("voice", String.valueOf(voiceSelect.getValue()));
        }

        /**
         * Update Colour Settings.
         */
        //Save Highlights Color.
        Color col = highlightsPicker.getValue();
        properties.setValue("highlightBoxColor", String.valueOf(shiftColorSpaceToSwing(col)));
        properties.setValue("highlightComposite", String.valueOf(highlightTF.getText()));
        properties.setValue("invertHighlights", String.valueOf(invertHighlightsCB.isSelected()));
        
        //Save Page Color
        col = pageColorPicker.getValue();
        properties.setValue("vbgColor", String.valueOf(shiftColorSpaceToSwing(col)));
        properties.setValue("replaceDocumentTextColors", String.valueOf(replaceTextColorCB.isSelected()));
        
        //Save Display Color
        col = displayBGColorPicker.getValue();
        properties.setValue("pdfDisplayBackground", String.valueOf(shiftColorSpaceToSwing(col)));
        properties.setValue("changeTextAndLineart", String.valueOf(changeLineArtColorCB.isSelected()));
        properties.setValue("replacePdfDisplayBackground", String.valueOf(replaceDisplayCB.isSelected()));
        
        //Save Text Color
        col = textColorPicker.getValue();
        properties.setValue("vfgColor", String.valueOf(shiftColorSpaceToSwing(col)));
                
        /**
         * Update Menu Settings.
         */
        /**
         * Update Printing Settings.
         */
        properties.setValue("useHiResPrinting", String.valueOf(hiResPrintingCB.isSelected()));

        if (((String) printerCombo.getValue()).startsWith("System Default")) {
            properties.setValue("defaultPrinter", "");
        } else {
            properties.setValue("defaultPrinter", String.valueOf(printerCombo.getValue()));
        }

        properties.setValue("defaultPagesize", String.valueOf(paperSizesCombo.getValue()));
        properties.setValue("defaultDPI", String.valueOf(defaultDPITF.getText()));
        properties.setValue("printerBlacklist", String.valueOf(blackListTF.getText()));
        
        //Save all options found in a tree
        saveGUIPreferences(currentGUI);

    }
    
    private static void saveMenuPreferencesChildren(final CheckBoxTreeItem<String> root, final GUIFactory gui){
        for(int i=0; i!=root.getChildren().size(); i++){
            final CheckBoxTreeItem<String> node = (CheckBoxTreeItem<String>)root.getChildren().get(i);
            final String value = ((String)reverseMessage.get(node.getValue()));
            if(node.isSelected()){
                properties.setValue(value, "true");
                gui.alterProperty(value, true);
            }else{
                properties.setValue(value, "false");
                gui.alterProperty(value, false);
            }
            
            if(!node.getChildren().isEmpty()){
                saveMenuPreferencesChildren(node, gui);
            }
        }
    }
    
    private static void saveGUIPreferences(final GUIFactory gui){
    
        for(int i=0; i!=tabs.getTabs().size(); i++){
            final Tab currentTab = tabs.getTabs().get(i);
            final TreeView tree = (TreeView)currentTab.getContent();
            final CheckBoxTreeItem<String> root = (CheckBoxTreeItem<String>)tree.getRoot();
            if(!root.getChildren().isEmpty()){
                saveMenuPreferencesChildren(root, gui);
            }
        }
    }

    private static void loadSettings() {

        /**
         * Load General Settings.
         */
        String propValue = properties.getValue("resolution");
        if (!propValue.isEmpty()) {
            resolutionTF.setText(propValue);
        } else {
            resolutionTF.setText("72");
        }

        propValue = properties.getValue("useHinting");
        if (!propValue.isEmpty() && propValue.equals("true")) {
            trueTypeCB.setSelected(true);
        } else {
            trueTypeCB.setSelected(false);
        }

        propValue = properties.getValue("autoScroll");
        if (propValue.equals("true")) {
            autoScrollCB.setSelected(true);
        } else {
            autoScrollCB.setSelected(false);
        }

        propValue = properties.getValue("confirmClose");
        if (propValue.equals("true")) {
            confirmCloseCB.setSelected(true);
        } else {
            confirmCloseCB.setSelected(false);
        }

        propValue = properties.getValue("automaticupdate");
        if (propValue.equals("true")) {
            checkUpdatesCB.setSelected(true);
        } else {
            checkUpdatesCB.setSelected(false);
        }

        propValue = properties.getValue("openLastDocument");
        if (propValue.equals("true")) {
            openDocCB.setSelected(true);
        } else {
            openDocCB.setSelected(false);
        }

        /**
         * Load Page Layout Settings.
         */
        propValue = properties.getValue("enhancedViewerMode");
        if (!propValue.isEmpty() && propValue.equals("true")) {
            enhancedViewerCB.setSelected(true);
        } else {
            enhancedViewerCB.setSelected(false);
        }

        propValue = properties.getValue("borderType");
        if (!propValue.isEmpty()) {
            if (Integer.parseInt(propValue) == 1) {
                showBorderCB.setSelected(true);
            } else {
                showBorderCB.setSelected(false);
            }
        }

        propValue = properties.getValue("pageInsets");
        if (propValue != null && !propValue.isEmpty()) {
            pageInsetsTF.setText(propValue);
        } else {
            pageInsetsTF.setText("25");
        }

        propValue = properties.getValue("startView");
        if (!propValue.isEmpty()) {
            int mode = Integer.parseInt(propValue);
            if (mode < Display.SINGLE_PAGE || mode > Display.PAGEFLOW) {
                mode = Display.SINGLE_PAGE;
            }

            displayCombo.setValue(displayCombo.getItems().get(mode - 1));
        }

        propValue = properties.getValue("enhancedFacingMode");
        if (!propValue.isEmpty() && propValue.equals("true")) {
            enablePageFlipCB.setSelected(true);
        } else {
            enablePageFlipCB.setSelected(false);
        }

        propValue = properties.getValue("previewOnSingleScroll");
        if (!propValue.isEmpty() && propValue.equals("true")) {
            scrollableThumbsCB.setSelected(true);
        } else {
            scrollableThumbsCB.setSelected(false);
        }

        /**
         * Load Interface Settings.
         */
        propValue = properties.getValue("windowTitle");
        if (propValue != null && !propValue.isEmpty()) {
            winTitleTF.setText(propValue);
        }

        propValue = properties.getValue("iconLocation");
        if (propValue != null && !propValue.isEmpty()) {
            iconLocTF.setText(propValue);
        } else {
            iconLocTF.setText("/org/jpedal/examples/viewer/res/");
        }

        propValue = properties.getValue("searchWindowType");
        if (!propValue.isEmpty()) {
            final int index = Integer.parseInt(propValue);
            if (index < searchStyle.getItems().size()) {
                searchStyle.setValue(searchStyle.getItems().get(Integer.parseInt(propValue)));
            } else {
                searchStyle.setValue(searchStyle.getItems().get(0));
            }
        } else {
            searchStyle.setValue(searchStyle.getItems().get(0));
        }

        propValue = properties.getValue("maxmultiviewers");
        if (!propValue.isEmpty()) {
            maxViewerTF.setText(propValue);
        } else {
            maxViewerTF.setText("20");
        }

        propValue = properties.getValue("sideTabBarCollapseLength");
        if (propValue != null && !propValue.isEmpty()) {
            sideTabTF.setText(propValue);
        } else {
            sideTabTF.setText("30");
        }

        propValue = properties.getValue("consistentTabBar");
        if (!propValue.isEmpty() && propValue.equals("true")) {
            consistentSideTabCB.setSelected(true);
        } else {
            consistentSideTabCB.setSelected(false);
        }

        propValue = properties.getValue("allowRightClick");
        if (!propValue.isEmpty() && propValue.equals("true")) {
            rightClickCB.setSelected(true);
        } else {
            rightClickCB.setSelected(false);
        }

        propValue = properties.getValue("allowScrollwheelZoom");
        if (!propValue.isEmpty() && propValue.equals("true")) {
            wheelZoomCB.setSelected(true);
        } else {
            wheelZoomCB.setSelected(false);
        }

        propValue = properties.getValue("showMouseSelectionBox");
        if (!propValue.isEmpty() && propValue.equalsIgnoreCase("true")) {
            mouseSelectCB.setSelected(true);
        } else {
            mouseSelectCB.setSelected(false);
        }

        voiceSelect.setValue(properties.getValue("voice"));
        
        propValue = properties.getValue("transitionType");
        if(!propValue.isEmpty()){
            transitionSelect.getSelectionModel().select(propValue);
        }
        
        /**
         * Load Printing Settings.
         */
        propValue = properties.getValue("useHiResPrinting");
        if (!propValue.isEmpty() && propValue.equals("true")) {
            hiResPrintingCB.setSelected(true);
        } else {
            hiResPrintingCB.setSelected(false);
        }

        propValue = properties.getValue("defaultPrinter");
        if (propValue != null && !propValue.isEmpty()) {
            printerCombo.setValue(propValue);
        } else {
            final PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultPrintService != null) {
                printerCombo.setValue("System Default (" + defaultPrintService.getName() + ')');
            } else {
                printerCombo.setValue("System Default");
            }
        }

        propValue = properties.getValue("defaultDPI");
        if (propValue != null && !propValue.isEmpty()) {
            try {
                propValue = propValue.replaceAll("[^0-9]", "");
                defaultDPITF.setText(Integer.parseInt(propValue) + "dpi");
            } catch (final Exception e) {
                //
                 if (LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception attempting Load Printing Settings: " + e);
                }
            }
        }

        propValue = properties.getValue("printerBlacklist");
        if (propValue != null && !propValue.isEmpty()) {
            blackListTF.setText(propValue);
        }
        
        /**
         * Load Color Settings.
         */
        propValue = properties.getValue("highlightBoxColor");
        int hBoxColor;
        if(!propValue.isEmpty()){
            hBoxColor = Integer.parseInt(propValue);
        }else{
            hBoxColor = DecoderOptions.highlightColor.getRGB();
        }
        highlightsPicker = new ColorPicker(shiftColorSpaceToFX(hBoxColor));
        
        highlightTF = new TextField();
        final String hComposite = properties.getValue("highlightComposite");
        if(!hComposite.isEmpty()) {
            highlightTF.setText(hComposite);
        }
        
        invertHighlightsCB = new CheckBox(Messages.getMessage("PdfPreferences.InvertHighlight"));
        final String invertHighlights = properties.getValue("invertHighlights");
        if (!invertHighlights.isEmpty() && invertHighlights.equalsIgnoreCase("true")) {
            invertHighlightsCB.setSelected(true);
        } else {
            invertHighlightsCB.setSelected(false);
        }

        propValue = properties.getValue("vbgColor");
        if(!propValue.isEmpty()){
            hBoxColor = Integer.parseInt(propValue);
        }else if(DecoderOptions.backgroundColor!=null){
            hBoxColor = DecoderOptions.backgroundColor.getRGB();
        }
        pageColorPicker = new ColorPicker(shiftColorSpaceToFX(hBoxColor));
        
        replaceTextColorCB = new CheckBox("Replace Document Text Colors");
        final String replaceTextCol = properties.getValue("replaceDocumentTextColors");
        if (!replaceTextCol.isEmpty() && replaceTextCol.equalsIgnoreCase("true")) {
            replaceTextColorCB.setSelected(true);
        }else{
            replaceTextColorCB.setSelected(false);
        }
        
        propValue = properties.getValue("vfgColor");
        if(!propValue.isEmpty()){
            hBoxColor = Integer.parseInt(propValue);
        }
        textColorPicker = new ColorPicker(shiftColorSpaceToFX(hBoxColor));
        
        changeLineArtColorCB = new CheckBox("Change Color of Text and Line Art");

        final String changeTextAndLineart = properties.getValue("changeTextAndLineart");
        if(!changeTextAndLineart.isEmpty() && changeTextAndLineart.equalsIgnoreCase("true")){
            changeLineArtColorCB.setSelected(true);
        }else{
            changeLineArtColorCB.setSelected(false);
        }
        
        replaceDisplayCB = new CheckBox("Replace Display Background Color");

        final String replaceDisplayCol = properties.getValue("replacePdfDisplayBackground");
        if (!replaceDisplayCol.isEmpty() && replaceDisplayCol.equalsIgnoreCase("true")) {
            replaceDisplayCB.setSelected(true);
        } else {
            replaceDisplayCB.setSelected(false);
        }
        
        propValue = properties.getValue("pdfDisplayBackground");
        if(!propValue.isEmpty()){
            hBoxColor = Integer.parseInt(propValue);
        }
        displayBGColorPicker = new ColorPicker(shiftColorSpaceToFX(hBoxColor));
        
        

    }
}
