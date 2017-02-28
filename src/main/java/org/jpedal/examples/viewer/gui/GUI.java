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
 * GUI.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui;

import org.jpedal.display.GUIThumbnailPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import org.jpedal.*;
import org.jpedal.display.Display;
import org.jpedal.display.GUIDisplay;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.RecentDocumentsFactory;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.Viewer;
import org.jpedal.examples.viewer.commands.OpenFile;
import org.jpedal.examples.viewer.gui.generic.*;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.exception.*;
import org.jpedal.external.AnnotationHandler;
import org.jpedal.external.CustomMessageHandler;
import org.jpedal.external.Options;
import org.jpedal.gui.GUIFactory;
import org.jpedal.gui.ShowGUIMessage;
import org.jpedal.io.StatusBar;
import org.jpedal.linear.LinearThread;
import org.jpedal.objects.acroforms.ReturnValues;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecodeStatus;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.*;
import org.jpedal.utils.Messages;
import org.jpedal.utils.StringUtils;

/**any shared GUI code - generic and AWT*/
public abstract class GUI implements GUIFactory {

    protected static int expandedSize=190;
    protected static int collapsedSize=30;
    
    boolean isJavaFX;

    /**
     * Generic ENUMS for setting similar JavaFX and Swing values.
     */
    public enum ScrollPolicy {
        VERTICAL_AS_NEEDED, HORIZONTAL_AS_NEEDED, VERTICAL_NEVER, HORIZONTAL_NEVER
    }
    public enum PageCounter {
        PAGECOUNTER1, PAGECOUNTER2, PAGECOUNTER3, ALL
    }
        
    public static String windowTitle;

    protected RecentDocumentsFactory recent;

    /**listener on buttons, menus, combboxes to execute options (one instance on all objects)*/
    protected CommandListener currentCommandListener;

    protected Commands currentCommands;

    //allow user to control messages in Viewer
    protected CustomMessageHandler customMessageHandler;

    /**control if messages appear*/
    public static boolean showMessages=true;

    //setup in init so we can pass in some objects
    protected GUIMenuItems menuItems;

    //layers tab
    protected PdfLayerList layersObject;

    protected boolean finishedDecoding;

    public static final int CURSOR = 1;
    /** grabbing cursor */
    public static final int GRAB_CURSOR = 1;
    public static final int GRABBING_CURSOR = 2;
    public static final int DEFAULT_CURSOR = 3;
    public static final int PAN_CURSOR = 4;
    public static final int PAN_CURSORL = 5;
    public static final int PAN_CURSORTL = 6;
    public static final int PAN_CURSORT = 7;
    public static final int PAN_CURSORTR = 8;
    public static final int PAN_CURSORR = 9;
    public static final int PAN_CURSORBR = 10;
    public static final int PAN_CURSORB = 11;
    public static final int PAN_CURSORBL = 12;

    protected Font textFont=new Font("Serif",Font.PLAIN,12);

    protected Font headFont=new Font("SansSerif",Font.BOLD,14);

    protected boolean previewOnSingleScroll =true;

    /** Constants for glowing border */
    protected static final int glowThickness = 11;
    protected final Color glowOuterColor = new Color(0.0f, 0.0f, 0.0f ,0.0f);
    protected final Color glowInnerColor = new Color(0.8f, 0.75f, 0.45f, 0.8f);

    private boolean commandInThread; //If we are running command in thread do not mark command as executed at end of method, it is handled by thread.

    private boolean executingCommand;

    //private Color[] annotColors={Color.RED,Color.BLUE,Color.BLUE};

    protected boolean hiResPrinting;

    //@annot - table of objects we wish to track
    protected Map objs;

    //flag if generated so we setup once for each file
    protected boolean bookmarksGenerated;
    protected GUISearchWindow searchFrame;
    protected String pageTitle,bookmarksTitle, signaturesTitle,layersTitle;

    public static final boolean debugFX=false;

    public GUI(final PdfDecoderInt decode_pdf, final Values commonValues, final GUIThumbnailPanel thumbnails, final PropertiesFile properties) {
        this.decode_pdf = decode_pdf;
        this.commonValues = commonValues;
        this.thumbnails = thumbnails;
        this.properties = properties;

        //<start-demo>
        /**
         //<end-demo>

         //
         /**/
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean useHiResPrinting() {
        return hiResPrinting;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setHiResPrinting(final boolean hiResPrinting) {
        this.hiResPrinting = hiResPrinting;
    }

    /**
     * This method is now deprecated. Please use getProperties().setValue(String item, boolean value) instead.
     * @deprecated
     * @param item :: Key for the property
     * @param value :: New value for the property
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setProperties(final String item, final boolean value){
        properties.setValue(item, String.valueOf(value));
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPreferences(final int dpi, final int search, final int border, final boolean scroll, int pageMode, final boolean updateDefaultValue, final int maxNoOfMultiViewers, final boolean useHiResPrinting){

        //Set border config value and repaint
        decode_pdf.setBorderPresent(border==1);
        properties.setValue("borderType", String.valueOf(border));

        //Set autoScroll default and add to properties file
        allowScrolling = scroll;
        properties.setValue("autoScroll", String.valueOf(scroll));

        //Dpi is taken into effect when zoom is called
        decode_pdf.getDPIFactory().setDpi(dpi);
        properties.setValue("resolution", String.valueOf(dpi));

        //Ensure valid value if not recognised
        if(pageMode<Display.SINGLE_PAGE || pageMode>Display.PAGEFLOW) {
            pageMode = Display.SINGLE_PAGE;
        }

        //Default Page Layout
        decode_pdf.setPageMode(pageMode);
        properties.setValue("startView", String.valueOf(pageMode));

        //

        //Set the search window
        final String propValue = properties.getValue("searchWindowType");
        if((!propValue.isEmpty() && !propValue.equals(String.valueOf(search))) && (showMessages) ){
                ShowGUIMessage.showGUIMessage(Messages.getMessage("PageLayoutViewMenu.ResetSearch"), null);
            }
        properties.setValue("searchWindowType", String.valueOf(search));

        properties.setValue("automaticupdate", String.valueOf(updateDefaultValue));

        commonValues.setMaxMiltiViewers(maxNoOfMultiViewers);
        properties.setValue("maxmultiviewers", String.valueOf(maxNoOfMultiViewers));

        hiResPrinting = useHiResPrinting;
        properties.setValue("useHiResPrinting", String.valueOf(useHiResPrinting));

    }

    /**handle for internal use*/
    protected PdfDecoderInt decode_pdf;

    /** minimum screen width to ensure menu buttons are visible */
    protected static final int minimumScreenWidth=700;

    //<start-demo><end-demo>

    /**XML structure of bookmarks*/
    protected GUIOutline tree;

    /**stops autoscrolling at screen edge*/
    protected boolean allowScrolling=true;

    /**confirms exit when closing the window*/
    protected boolean confirmClose;

    /**scaling values as floats to save conversion*/
    protected float[] scalingFloatValues={1.0f,1.0f,1.0f,.25f,.5f,.75f,1.0f,1.25f,1.5f,2.0f,2.5f,5.0f,7.5f,10.0f};

    /**page scaling to use 1=100%*/
    protected float scaling = 1;

    /** padding so that the pdf is not right at the edge */
    protected static int inset=25;

    /**store page rotation*/
    protected int rotation;

    /**scaling factors on the page*/
    protected GUICombo rotationBox;

    /**allows user to set quality of images*/
    protected GUICombo qualityBox;

    /**scaling factors on the page*/
    protected GUICombo scalingBox;

    /**default scaling on the combobox scalingValues*/
    protected static int defaultSelection;

    protected final Values commonValues;

    protected final GUIThumbnailPanel thumbnails;

    protected final PropertiesFile properties;

    protected String propValue;

    protected String propValue2;

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#allowScrolling()
     */
    @Override
    public boolean allowScrolling() {
        return allowScrolling;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#confirmClose()
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public boolean confirmClose() {
        return confirmClose;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#setAutoScrolling(boolean allowScrolling)
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public void setAutoScrolling(final boolean allowScrolling) {
        this.allowScrolling=allowScrolling;

    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#toogleAutoScrolling()
     */
    @Override
    public void  toogleAutoScrolling(){
        allowScrolling=!allowScrolling;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#getRotation()
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public int getRotation() {
        return rotation;
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#getScaling()
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public float getScaling() {
        return scaling;
    }

    @Override
    public Values getValues(){
        return commonValues;
    }
    /* (non-Javadoc)
	 * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#addCombo(java.lang.String, java.lang.String, int)
	 */
    protected void addCombo(final String title, final String tooltip, final int ID){

        if(debugFX){
            System.out.println("addCombo");
        }

        GUICombo combo=null;
        switch (ID){

            case Commands.QUALITY:
                combo=qualityBox;
                break;

            case Commands.SCALING:
                combo=scalingBox;
                break;
            case Commands.ROTATION:
                combo=rotationBox;
                break;
        }

        combo.setID(ID);


        if(!tooltip.isEmpty()) {
            combo.setToolTipText(tooltip);
        }


        addGUIComboBoxes(combo);

        addComboListenerAndLabel(combo,title);
    }

    /**
     * get Map containing Form Objects setup for Unique Annotations
     *
     * @return Map
     */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public Map getHotspots() {

        return Collections.unmodifiableMap(objs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setScaling(final float s){
        scaling = s;
        scalingBox.setSelectedIndex((int) scaling);
    }

    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#getPDFDisplayInset()
     */
    public static int getPDFDisplayInset() {
        return inset;
    }

    //<link><a name="handleAnnotations" />
    /** example code which sets up an individual icon for each annotation to display - only use
     * if you require each annotation to have its own icon<p>
     * To use this you ideally need to parse the annotations first -there is a method allowing you to
     * extract just the annotations from the data.
     */
    public void createUniqueAnnotationIcons() {

        //and place to store so we can test later
        //flush list if needed
        if(objs==null) {
            objs = new HashMap();
        } else {
            objs.clear();
        }

        //create Annots - you can replace with your own implementation using setExternalHandler()
        ((AnnotationHandler)decode_pdf.getExternalHandler(Options.UniqueAnnotationHandler)).handleAnnotations(decode_pdf, objs, commonValues.getCurrentPage());

    }

    @SuppressWarnings("UnusedDeclaration")
    public void setDpi(final int dpi) {
        decode_pdf.getDPIFactory().setDpi(dpi);
    }



    @Override
    public void dispose(){
        tree=null;
        scalingFloatValues=null;
        rotationBox=null;
        qualityBox=null;
        scalingBox=null;
    }


    /**
     * main method to initialise Swing specific code and create GUI display
     */
    public void init(final Commands currentCommands) {

        /**
         * single listener to execute all commands
         */
        currentCommandListener = new CommandListener(currentCommands);

        //setup custom message and switch off error messages if used
        customMessageHandler = (CustomMessageHandler) (decode_pdf.getExternalHandler(Options.CustomMessageOutput));
        if (customMessageHandler != null) {
            DecoderOptions.showErrorMessages = false;
            GUI.showMessages = false;
        }

        /**
         * Set up from properties
         */
        try {
            //Set viewer page inset
            propValue = properties.getValue("pageInsets");
            if (!propValue.isEmpty()) {
                inset = Integer.parseInt(propValue);
            }

            propValue = properties.getValue("changeTextAndLineart");
            if (!propValue.isEmpty()
                    && propValue.equalsIgnoreCase("true")) {
                currentCommands.executeCommand(Commands.CHANGELINEART, new Object[]{Boolean.parseBoolean(propValue)});
            }

            propValue = properties.getValue("windowTitle");
            if (!propValue.isEmpty()) {
                windowTitle = propValue;
            } else {
                //
                windowTitle ="LGPL PDF JavaFX Viewer " + PdfDecoderInt.version;
                /**/
            }

            propValue = properties.getValue("vbgColor");
            if (!propValue.isEmpty()) {
                currentCommands.executeCommand(Commands.SETPAGECOLOR, new Object[]{Integer.parseInt(propValue)});
            }

            propValue = properties.getValue("replaceDocumentTextColors");
            if (!propValue.isEmpty()
                    && propValue.equalsIgnoreCase("true")) {

                propValue = properties.getValue("vfgColor");
                if (!propValue.isEmpty()) {
                    currentCommands.executeCommand(Commands.SETTEXTCOLOR, new Object[]{Integer.parseInt(propValue)});
                }

            }

            propValue = properties.getValue("TextColorThreshold");
            if (!propValue.isEmpty()) {
                currentCommands.executeCommand(Commands.SETREPLACEMENTCOLORTHRESHOLD, new Object[]{Integer.parseInt(propValue)});
            }

            //Set autoScroll default and add to properties file
            propValue = properties.getValue("autoScroll");
            if (!propValue.isEmpty()) {
                allowScrolling = Boolean.getBoolean(propValue);
            }

            //set confirmClose
            propValue = properties.getValue("confirmClose");
            if (!propValue.isEmpty()) {
                confirmClose = propValue.equals("true");
            }

            //Dpi is taken into effect when zoom is called
            propValue = properties.getValue("resolution");
            if (!propValue.isEmpty()) {
                decode_pdf.getDPIFactory().setDpi(Integer.parseInt(propValue));
            }

            //Ensure valid value if not recognised
            propValue = properties.getValue("startView");

            if (!propValue.isEmpty()) {
                int pageMode = Integer.parseInt(propValue);
                //  pageMode=2;
                //  System.out.println(SwingUtilities.isEventDispatchThread());
                if (pageMode < Display.SINGLE_PAGE || pageMode > Display.PAGEFLOW) {
                    pageMode = Display.SINGLE_PAGE;
                }
                //Default Page Layout
                decode_pdf.setPageMode(pageMode);
            }

            propValue = properties.getValue("maxmuliviewers");
            if (!propValue.isEmpty()) {
                commonValues.setMaxMiltiViewers(Integer.parseInt(propValue));
            }

            propValue = properties.getValue("useHiResPrinting");
            if (!propValue.isEmpty()) {
                hiResPrinting = Boolean.valueOf(propValue);
            }

            final String val = properties.getValue("highlightBoxColor"); //empty string to old users
            if (!val.isEmpty()) {
                DecoderOptions.highlightColor = new Color(Integer.parseInt(val));
            }

            propValue = properties.getValue("highlightTextColor");
            if (!propValue.isEmpty()) {
                DecoderOptions.backgroundColor = new Color(Integer.parseInt(propValue));
            }

            propValue = properties.getValue("showMouseSelectionBox");
            if (!propValue.isEmpty()) {
                DecoderOptions.showMouseBox = Boolean.valueOf(propValue);
            }

            propValue = properties.getValue("enhancedViewerMode");
            if (!propValue.isEmpty()) {
                decode_pdf.useNewGraphicsMode(Boolean.valueOf(propValue));
            }

            propValue = properties.getValue("highlightComposite");
            if (!propValue.isEmpty()) {
                float value = Float.parseFloat(propValue);
                if (value > 1) {
                    value = 1;
                }
                if (value < 0) {
                    value = 0;
                }

                DecoderOptions.highlightComposite = value;
            }

            //Set border config value and repaint
            propValue = properties.getValue("borderType");
            if (!propValue.isEmpty()) {
                decode_pdf.setBorderPresent(Integer.parseInt(propValue)==1);
            }

            //Allow cursor to change
            propValue = properties.getValue("allowCursorToChange");
            if (!propValue.isEmpty()) {
                GUIDisplay.allowChangeCursor = propValue.equalsIgnoreCase("true");
            }

            propValue = properties.getValue("invertHighlights");
            if (!propValue.isEmpty()) {
                BaseDisplay.invertHighlight = Boolean.valueOf(propValue);
            }

            propValue = properties.getValue("enhancedFacingMode");
            if (!propValue.isEmpty()) {
                GUIDisplay.default_turnoverOn = Boolean.valueOf(propValue);
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }

        this.currentCommands = currentCommands;

        setViewerTitle(windowTitle);
        setViewerIcon();

        /**
         * arrange insets
         */
        decode_pdf.setInset(inset, inset);

    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public boolean isCommandInThread(){
        return commandInThread;
    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public void setCommandInThread(final boolean b){
        commandInThread = b;
    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public boolean isExecutingCommand(){
        return executingCommand;
    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public void setExecutingCommand(final boolean b){
        executingCommand = b;
    }

    protected static void getFlattenedTreeNodes(final TreeNode theNode, final java.util.List items) {
        // add the item
        items.add(theNode);

        // recursion
        for (final Enumeration theChildren = theNode.children(); theChildren.hasMoreElements();) {
            getFlattenedTreeNodes((TreeNode) theChildren.nextElement(), items);
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    public int getGlowThickness(){
        return glowThickness;
    }

    public Color getGlowOuterColor(){
        return glowOuterColor;
    }

    public Color getGlowInnerColor(){
        return glowInnerColor;
    }

    // Adding the search frame on to the GUI
    @Override
    public void setSearchFrame(final GUISearchWindow searchFrame) {

        this.searchFrame = searchFrame;

    }

    protected void setRotation(){
        //PdfPageData currentPageData=decode_pdf.getPdfPageData();
        //rotation=currentPageData.getRotation(commonValues.getCurrentPage());

        //Broke files with when moving from rotated page to non rotated.
        //The pages help previous rotation
        //rotation = (rotation + (getSelectedComboIndex(Commands.ROTATION)*90));

        if(rotation > 360) {
            rotation -= 360;
        }

        if(getSelectedComboIndex(Commands.ROTATION)!=(rotation/90)){
            setSelectedComboIndex(Commands.ROTATION, (rotation/90));
        }else if(!Values.isProcessing() && !Viewer.isFX()){
            //
        }
    }

    /**
     * get current value for a combobox (options QUALITY,SCALING,ROTATION)
     */
    public int getSelectedComboIndex(final int ID) {

        switch (ID){

            case Commands.QUALITY:
                return qualityBox.getSelectedIndex();

            case Commands.SCALING:
                return scalingBox.getSelectedIndex();
            case Commands.ROTATION:
                return rotationBox.getSelectedIndex();
            default:
                return -1;
        }
    }

    /**
     * set current index for a combobox (options QUALITY,SCALING,ROTATION)
     */
    public void setSelectedComboIndex(final int ID, final int index) {
        switch (ID){

            case Commands.QUALITY:
                qualityBox.setSelectedIndex(index);
                break;

            case Commands.SCALING:
                scalingBox.setSelectedIndex(index);
                break;
            case Commands.ROTATION:
                rotationBox.setSelectedIndex(index);
                break;

        }

    }

    /**
     * get current Item for a combobox (options QUALITY,SCALING,ROTATION)
     */
    public Object getSelectedComboItem(final int ID) {

        switch (ID){

            case Commands.QUALITY:
                return qualityBox.getSelectedItem();

            case Commands.SCALING:
                return scalingBox.getSelectedItem();
            case Commands.ROTATION:
                return rotationBox.getSelectedItem();
            default:
                return null;

        }
    }

    /**
     * get current Item for a combobox (options QUALITY,SCALING,ROTATION)
     */
    public void setSelectedComboItem(final int ID,String index) {
        switch (ID){

            case Commands.QUALITY:
                qualityBox.setSelectedItem(index);
                break;

            case Commands.SCALING:
                //When using any of the fit scalings, adding a % will break it
                //Only add if scaling is a number
                if(StringUtils.isNumber(index)) {
                    index += '%';
                }
                scalingBox.setSelectedItem(index);
                break;
            case Commands.ROTATION:
                rotationBox.setSelectedItem(index);
                break;

        }
    }

    public void setPdfDecoder(final PdfDecoderInt decode_pdf){
        this.decode_pdf = decode_pdf;
    }

    /**
     * called by nav functions to decode next page (in GUI code as needs to
     * manipulate large part of GUI)
     */
    public void decodeGUIPage(final GUIFactory currentGUI){

        //Remove Image extraction outlines when page is changed
        decode_pdf.getPages().setHighlightedImage(null);

        currentGUI.resetRotationBox();

        /** if running terminate first */
        if(thumbnails.isShownOnscreen()) {
            thumbnails.terminateDrawing();
        }

        if(thumbnails.isShownOnscreen()){

            final LinearThread linearizedBackgroundRenderer = (LinearThread) decode_pdf.getJPedalObject(PdfDictionary.LinearizedReader);

            if(linearizedBackgroundRenderer==null ||
                    (linearizedBackgroundRenderer!=null && !linearizedBackgroundRenderer.isAlive())) {
                currentGUI.setupThumbnailPanel();
            }
        }

        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
            currentGUI.setPageCounterText(PageCounter.PAGECOUNTER2, String.valueOf(commonValues.getCurrentPage()));
            currentGUI.setPageCounterText(PageCounter.PAGECOUNTER3, Messages.getMessage("PdfViewerOfLabel.text") + ' ' + commonValues.getPageCount());
        }

        currentGUI.updateTextBoxSize();

        //allow user to now open tabs
        currentGUI.setTabsNotInitialised(false);

        /**ensure text and color extracted. If you do not need color, take out
         * line for faster decode
         */
//		decode_pdf.setExtractionMode(PdfDecoderInt.TEXT);
        decode_pdf.setExtractionMode(PdfDecoderInt.TEXT+PdfDecoderInt.TEXTCOLOR);


        //remove any search highlight
        decode_pdf.getTextLines().clearHighlights();

        //kick-off thread to create pages
        if(decode_pdf.getDisplayView() == Display.FACING){

            /**reset as rotation may change!*/
            //decode_pdf.setPageParameters(getScaling(), commonValues.getCurrentPage());

            currentGUI.scaleAndRotate();
            currentGUI.scrollToPage(commonValues.getCurrentPage());

            decode_pdf.getPages().decodeOtherPages(commonValues.getCurrentPage(),commonValues.getPageCount());

            return ;
        }else if(decode_pdf.getDisplayView() == Display.CONTINUOUS || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){

            //resize (ensure at least certain size)
            //must be here as otherwise will not redraw if new page opened
            //in multipage mode
            currentGUI.scaleAndRotate();

            currentGUI.scrollToPage(commonValues.getCurrentPage());

                if(!Viewer.isFX()){
                return ;
            }
        }else if(decode_pdf.getDisplayView() == Display.PAGEFLOW) {
            return;
        }

        //stop user changing scaling while decode in progress
        currentGUI.resetComboBoxes(false);
        currentGUI.getButtons().setPageLayoutButtonsEnabled(false);

        Values.setProcessing(true);

        //SwingWorker worker = new SwingWorker() {

        //

        //<start-demo><end-demo>

        try {
            if(!Viewer.isFX()){
                ((StatusBar)currentGUI.getStatusBar()).updateStatus("Decoding Page",0);
            }
            /**
             * make sure screen fits display nicely
             */
            //if ((resizePanel) && (thumbnails.isShownOnscreen()))
            //	zoom();

            //			if (Thread.interrupted())
            //				throw new InterruptedException();

            /**
             * decode the page
             */
            try {
                decode_pdf.decodePage(commonValues.getCurrentPage());

                //wait to ensure decoded
                decode_pdf.waitForDecodingToFinish();


                //value set from JVM flag org.jpedal.maxShapeCount=maxNumber
                if(decode_pdf.getPageDecodeStatus(DecodeStatus.TooManyShapes)){

                    final String status = "Too many shapes on page";

                    currentGUI.showMessageDialog(status);
                }


                if(!decode_pdf.getPageDecodeStatus(DecodeStatus.ImagesProcessed)){

                    final String status = (Messages.getMessage("PdfViewer.ImageDisplayError")+
                            Messages.getMessage("PdfViewer.ImageDisplayError1")+
                            Messages.getMessage("PdfViewer.ImageDisplayError2")+
                            Messages.getMessage("PdfViewer.ImageDisplayError3")+
                            Messages.getMessage("PdfViewer.ImageDisplayError4")+
                            Messages.getMessage("PdfViewer.ImageDisplayError5")+
                            Messages.getMessage("PdfViewer.ImageDisplayError6")+
                            Messages.getMessage("PdfViewer.ImageDisplayError7"));

                    currentGUI.showMessageDialog(status);
                }

                /**
                 * see if lowres poor quality image and flag up if so
                 *
                 String imageDetailStr=decode_pdf.getInfo(PdfDictionary.Image);

                 //get iterator (each image is a single line)
                 StringTokenizer allImageDetails=new StringTokenizer(imageDetailStr,"\n");


                 while(allImageDetails.hasMoreTokens()){

                 String str=allImageDetails.nextToken();
                 StringTokenizer imageDetails=new StringTokenizer(str," ()");

                 //System.out.println(imageDetails.countTokens()+" ==>"+str);
                 //if single image check further
                 if(imageDetails.countTokens()>2){ //ignore forms
                 String imageName=imageDetails.nextToken();
                 String imageType=imageDetails.nextToken();
                 String imageW=imageDetails.nextToken().substring(2);
                 String imageH=imageDetails.nextToken().substring(2);
                 String bitsPerPixel=imageDetails.nextToken();
                 String dpi=imageDetails.nextToken().substring(4);

                 //we can also look at PDF creation tool
                 String[] metaData=decode_pdf.getFileInformationData().getFieldValues();

                 //test here and take action or set flag
                 if(Integer.parseInt(dpi)<144 && metaData[5].equals("iText 2.1.7 by 1T3XT")){
                 System.out.println("Low resolution image will not print well in Java");
                 }
                 }
                 }

                 /**/

                /**
                 * Tell user if hinting is probably required
                 */
                if(decode_pdf.getPageDecodeStatus(DecodeStatus.TTHintingRequired)){

                    final String status = Messages.getMessage("PdfCustomGui.ttHintingRequired");

                    currentGUI.showMessageDialog(status);
                }

                if(decode_pdf.getPageDecodeStatus(DecodeStatus.NonEmbeddedCIDFonts)){

                    final String status = ("This page contains non-embedded CID fonts \n" +
                            decode_pdf.getPageDecodeStatusReport(DecodeStatus.NonEmbeddedCIDFonts)+
                            "\nwhich may need mapping to display correctly.\n" +
                            "See http://www.idrsolutions.com/how-do-fonts-work");

                    currentGUI.showMessageDialog(status);
                }
                //read values for page display
                //PdfPageData page_data = decode_pdf.getPdfPageData();

                //mediaW  = page_data.getMediaBoxWidth(commonValues.getCurrentPage());
                //mediaH = page_data.getMediaBoxHeight(commonValues.getCurrentPage());
//						mediaX = page_data.getMediaBoxX(commonValues.getCurrentPage());
//						mediaY = page_data.getMediaBoxY(commonValues.getCurrentPage());

//						resetRotationBox();


                //create custom annot icons
                if(decode_pdf.getExternalHandler(Options.UniqueAnnotationHandler)!=null){
                    /**
                     * ANNOTATIONS code to create unique icons
                     *
                     * this code allows you to create a unique set on icons for any type of annotations, with
                     * an icons for every annotation, not just types.
                     */
                    final FormFactory formfactory = decode_pdf.getFormRenderer().getFormFactory();

                    //swing needs it to be done with invokeLater
                    if(formfactory.getType()== FormFactory.SWING){
                        final Runnable doPaintComponent2 = new Runnable() {
                            @Override
                            public void run() {

                                createUniqueAnnotationIcons();

                                //validate();
                            }
                        };
                        SwingUtilities.invokeLater(doPaintComponent2);

                    }else{
                        createUniqueAnnotationIcons();
                    }


                }
            if(!Viewer.isFX()){
                ((StatusBar)currentGUI.getStatusBar()).updateStatus("Displaying Page",0);
            }

            } catch (final Exception e) {
                System.err.println(Messages.getMessage("PdfViewerError.Exception")+ ' ' + e +
                        ' ' +Messages.getMessage("PdfViewerError.DecodePage"));
                e.printStackTrace();
                Values.setProcessing(false);
            }


            //tell user if we had a memory error on decodePage
            if(DecoderOptions.showErrorMessages){
                String status=decode_pdf.getPageDecodeReport();
                if(status.contains("java.lang.OutOfMemoryError")){
                    status = (Messages.getMessage("PdfViewer.OutOfMemoryDisplayError")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError1")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError2")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError3")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError4")+
                            Messages.getMessage("PdfViewer.OutOfMemoryDisplayError5"));

                    currentGUI.showMessageDialog(status);

                }
            }

            Values.setProcessing(false);

            //make sure fully drawn
            //decode_pdf.repaint();

            currentGUI.setViewerTitle(null); //restore title

            OpenFile.setPageProperties(getSelectedComboItem(Commands.ROTATION), getSelectedComboItem(Commands.SCALING));

            //<end-demo>
            if (decode_pdf.getPageCount()>0 && thumbnails.isShownOnscreen() && decode_pdf.getDisplayView()==Display.SINGLE_PAGE) {
                thumbnails.generateOtherVisibleThumbnails(commonValues.getCurrentPage());
            }

        } catch (final Exception e) {
            e.printStackTrace();
            Values.setProcessing(false);//remove processing flag so that the viewer can be exited.
            currentGUI.setViewerTitle(null); //restore title
        }

        currentGUI.selectBookmark();

        //Update multibox
        if(!Viewer.isFX()){
            ((StatusBar)currentGUI.getStatusBar()).setProgress(100);
        }
//                    ActionListener listener = new ActionListener(){
//                        public void actionPerformed(ActionEvent e) {
//                            setMultibox(new int[]{});
//                        }
//                    };
//                    t = new Timer(800, listener);
//                    t.setRepeats(false);
//                    t.start();

//        try{
//            Thread.sleep(800);
//        }catch(final Exception e){
//            //
//        }
        currentGUI.setMultibox(new int[]{});

        //reanable user changing scaling
        currentGUI.resetComboBoxes(true);

        if(decode_pdf.getPageCount()>1) {
            currentGUI.getButtons().setPageLayoutButtonsEnabled(true);
        }


        /**adds listeners to GUI widgets to track changes*/

        //rest forms changed flag to show no changes
        commonValues.setFormsChanged(false);

        /**see if flag set - not default behaviour*/
        boolean showMessage=false;
        final String formsFlag=System.getProperty("org.jpedal.listenforms");
        if(formsFlag!=null) {
            showMessage = true;
        }

        //get the form renderer which also contains the processed form data.
        //if you want simple form data, also look at the ExtractFormDataAsObject.java example
        final org.jpedal.objects.acroforms.AcroRenderer formRenderer=decode_pdf.getFormRenderer();

        if(formRenderer==null) {
            return;
        }

        //get list of forms on page
        final Object[] formsOnPage = formRenderer.getFormComponents(null,ReturnValues.FORM_NAMES,commonValues.getCurrentPage());

        //allow for no forms
        if(formsOnPage==null){

            if(showMessage) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFields"));
            }

            return;
        }

        final int formCount=formsOnPage.length;

        final JPanel formPanel=new JPanel();
        /**
         * create a JPanel to list forms and tell user a box example
         **/
        if(showMessage){
            formPanel.setLayout(new BoxLayout(formPanel,BoxLayout.Y_AXIS));
            final JLabel formHeader = new JLabel("This page contains "+formCount+" form objects");
            formHeader.setFont(headFont);
            formPanel.add(formHeader);

            formPanel.add(Box.createRigidArea(new Dimension(10,10)));
            final JTextPane instructions = new JTextPane();
            instructions.setPreferredSize(new Dimension(450,180));
            instructions.setEditable(false);
            instructions.setText("This provides a simple example of Forms handling. We have"+
                    " added a listener to each form so clicking on it shows the form name.\n\n"+
                    "Code is in addExampleListeners() in org.examples.viewer.Viewer\n\n"+
                    "This could be easily be extended to interface with a database directly "+
                    "or collect results on an action and write back using itext.\n\n"+
                    "Forms have been converted into Swing components and are directly accessible"+
                    " (as is the original data).\n\n"+
                    "If you don't like the standard SwingSet you can replace with your own set.");
            instructions.setFont(textFont);
            formPanel.add(instructions);
            formPanel.add(Box.createRigidArea(new Dimension(10,10)));
        }

        /**
         * pop-up to show forms on page
         **/
        if(showMessage){
            final JDialog displayFrame =  new JDialog((JFrame)null,true);
            displayFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            if(commonValues.getModeOfOperation()!=Values.RUNNING_APPLET){
                displayFrame.setLocationRelativeTo(null);
                displayFrame.setLocation(((Container)currentGUI.getFrame()).getLocationOnScreen().x+10,((Container)currentGUI.getFrame()).getLocationOnScreen().y+10);
            }

            final JScrollPane scroll=new JScrollPane();
            scroll.getViewport().add(formPanel);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            displayFrame.setSize(500,500);
            displayFrame.setTitle("List of forms on this page");
            displayFrame.getContentPane().setLayout(new BorderLayout());
            displayFrame.getContentPane().add(scroll,BorderLayout.CENTER);

            final JPanel buttonBar=new JPanel();
            buttonBar.setLayout(new BorderLayout());
            displayFrame.getContentPane().add(buttonBar,BorderLayout.SOUTH);

            // close option just removes display
            final JButton no=new JButton(Messages.getMessage("PdfViewerButton.Close"));
            no.setFont(new Font("SansSerif", Font.PLAIN, 12));
            buttonBar.add(no,BorderLayout.EAST);
            no.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(final ActionEvent e) {
                    displayFrame.dispose();
                }});

            /**show the popup*/
            displayFrame.setVisible(true);
        }
        
        /**
         * if page has transition we will have stored values earlier and now need to use and remove
         */
        if(isJavaFX){
            FXAdditionalData additionaValuesforPage=(FXAdditionalData) decode_pdf.getExternalHandler(Options.JavaFX_ADDITIONAL_OBJECTS);
            
            if(additionaValuesforPage!=null){
                
               DynamicVectorRenderer fxDisplay= decode_pdf.getDynamicRenderer();
                
                try {
                    fxDisplay.drawAdditionalObjectsOverPage(additionaValuesforPage.getType(), null,additionaValuesforPage.getObj());
                } catch (PdfException ex) {
                    //
                    if (org.jpedal.utils.LogWriter.isOutput()) {
                        org.jpedal.utils.LogWriter.writeLog("Exception attempting to draw additional objects " + ex);
                    }
                }
                
            }
        }
        
        if(currentGUI.getFrame() != null){
            currentGUI.reinitialiseTabs(currentGUI.getDividerLocation() > currentGUI.getStartSize());
        }

        finishedDecoding=true;

        //Ensure page is at the correct scaling and rotation for display
        currentGUI.scaleAndRotate();

        //
    }

    //

    /*
     * Set title to display on top of Swing of FX viewer (include days left on trial version)
     *(non-Javadoc)
	 * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#addCombo(java.lang.String, java.lang.String, int)
	 */
    @Override
    public void setViewerTitle(String title) {

        if(title!=null){

            //<start-demo>
            /**
             //<end-demo>
             title="("+dx+" days left) "+title;
             /**/

        }else{

            //set null title value to empty string
            title=(windowTitle+' ' + commonValues.getSelectedFile());

            final PdfObject linearObj=(PdfObject)decode_pdf.getJPedalObject(PdfDictionary.Linearized);
            if(linearObj!=null){
                final LinearThread linearizedBackgroundReaderer = (LinearThread) decode_pdf.getJPedalObject(PdfDictionary.LinearizedReader);

                if(linearizedBackgroundReaderer !=null && linearizedBackgroundReaderer.isAlive()) {
                    title += " (still loading)";
                } else {
                    title += " (Linearized)";
                }
            }

            //<start-demo>
            /**
             //<end-demo>
             finalMessage="("+dx+" days left) "+title;
             /**/

            if(commonValues.isFormsChanged()) {
                title = "* " + title;
            }
        }

        setTitle(title);
    }

    /**
     * Sets the title for the Viewer.
     */
    protected void setTitle(final String title) {
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Sets the icon for the Viewer.
     */
    protected void setViewerIcon() {
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Adds listener for the ComboBoxes and Title.
     */
    protected void addComboListenerAndLabel(final GUICombo combo, final String title) {
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Adds the ComboBoxes to theViewer.
     * Resize box & rotation box.
     */
    protected void addGUIComboBoxes(final GUICombo combo){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Sets the background for the main center display
     * panel which holds the PDF content pane.
     */
    protected void setupCenterPanelBackground(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Performs initial setup of the ComboBoxes.
     * Resize box & rotation box.
     */
    protected void setupComboBoxes(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Adds key listeners for keyboard navigation of the Viewer.
     */
    protected void setupKeyboardControl(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Sets up the central display pane to display the pdf content.
     */
    protected void setupPDFDisplayPane(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Sets up the panes to the left and right of the central display.
     * Bookmarks & Thumbnails Pages etc.
     */
    protected void setupBorderPanes(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    protected void createOtherToolBars(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Sets up the position & visual style of the items on
     * the bottom toolbar (page navigation buttons etc).
     */
    protected void setupBottomToolBarItems(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Creates a glowing border around the PDFDisplayPane.
     */
    protected void setupPDFBorder(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Creates the top two menu bars, the file loading & viewer properties one
     * and the PDF toolbar, the one which controls printing, searching etc.
     */
    protected void createTopMenuBar(){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    /**
     * Creates the Main Display Window for all of the JavaFX Content.
     *
     * @param width is of type int
     * @param height is of type int
     */
    protected void createMainViewerWindow(final int width, final int height){
        throw new UnsupportedOperationException("Should be over-ridden in SwingGUI or JavaFxGUI");
    }

    protected void setupSidebarTitles(){
        pageTitle = Messages.getMessage("PdfViewerJPanel.thumbnails");
        bookmarksTitle = Messages.getMessage("PdfViewerJPanel.bookmarks");
        layersTitle = Messages.getMessage("PdfViewerJPanel.layers");
        signaturesTitle = Messages.getMessage("PdfViewerJPanel.signatures");
    }

}
