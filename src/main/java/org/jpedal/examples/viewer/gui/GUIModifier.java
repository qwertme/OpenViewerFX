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
 * GUIModifier.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui;

import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.gui.GUI.PageCounter;
import org.jpedal.examples.viewer.gui.generic.GUIButtons;
import org.jpedal.examples.viewer.gui.generic.GUIMenuItems;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.examples.viewer.utils.PropertyTags;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * A class that handles modifying the the viewers GUI based on property values
 * found in the properties files. This class has two public methods 
 * (Load and alterProperty) to allow you to do this. 
 * 
 * Load allows you to pass in a PropertiesFile object from which we take the
 * various GUI properties and set the GUI to match these options
 * 
 * AlterProperty allows you to specify a single property and the value you wish
 * to change it to.
 * 
 * Be aware that there are some properties that will not update live and must be
 * set before the GUI is created, in these cases it is recommended to set the 
 * value within the properties file instead of changing it manually.
 * 
 * Please note, this updates the GUI only, if you wish to alter the values that
 * are stored within you properties.xml file you need to use the following method.
 * 
 * PropertiesFile.setValue(final String elementName, final String newValue)
 */
public class GUIModifier {
    
    private static void setButtonEnabledAndVisible(final GUIButtons buttons, final int type, final boolean set) {
        buttons.getButton(type).setEnabled(set);
        buttons.getButton(type).setVisible(set);
    }
    
    private static void removeUnwantedTabs(final int tabCount, final GUIFactory currentGUI, final boolean set, final String title){
        for (int i = 0; i < tabCount; i++) {

            if (currentGUI.getSidebarTabTitleAt(i).equals(currentGUI.getTitles(title)) && !set) {
                currentGUI.removeSidebarTabAt(i);
                break;
            }
        }
    }
    
    private static void loadNodeList(final NodeList tags, final GUIFactory currentGUI){
        for(int i=0; i!=tags.getLength(); i++){
            Node node = tags.item(i);
            String name = tags.item(i).getNodeName();
            if (!name.startsWith("#")) { //Actual node
                if (node.hasAttributes()) { //With attributes
                    Node value = node.getAttributes().getNamedItem("value");
                    if (value != null) { //Has attribute called value
                        if (value.getNodeValue().equalsIgnoreCase("true")) {
                            alterProperty(name, true, currentGUI);
                        } else if (value.getNodeValue().equalsIgnoreCase("false")) {
                            alterProperty(name, false, currentGUI);
                        }
                    }
                }

                if (node.hasChildNodes()) {
                    loadNodeList(node.getChildNodes(), currentGUI);
                }
            }
        }
    }
    
    /**
     * This method loads all GUI properties from a PropertiesFile object.
     * 
     * @param properties The PropertiesFile to load values from.
     * @param currentGUI The GUI to have the properties loaded into.
     */
    public static void load(final PropertiesFile properties, final GUIFactory currentGUI) {

        String value = properties.getValue("sideTabBarCollapseLength");
        if (value != null && !value.isEmpty()) {
            int iValue = Integer.parseInt(value);
            currentGUI.setStartSize(iValue);
        }

        value = properties.getValue("sideTabBarExpandLength");
        if (value != null && !value.isEmpty()) {
            GUI.expandedSize = Integer.parseInt(value);
            currentGUI.reinitialiseTabs(false);
        }

        NodeList tags = properties.getChildren("*");
        loadNodeList(tags, currentGUI);
    }

    private static boolean alterSectionProperties(final String value, final boolean set, final GUIFactory currentGUI, final boolean isSingle){
        
        int propertyCode = value.hashCode();
        boolean used = true;

        //Disable entire section
        switch (propertyCode) {
            case PropertyTags.SHOWMENUBAR:
                currentGUI.getMenuItems().setMenuItem(Commands.CURRENTMENU, set, set);
                break;
            case PropertyTags.SHOWBUTTONS:
                currentGUI.getButtons().setEnabled(set);
                currentGUI.getButtons().setVisible(set);
                break;
            case PropertyTags.SHOWDISPLAYOPTIONS:
                break;
            case PropertyTags.SHOWNAVIGATIONBAR:
                currentGUI.enableNavigationBar(set, set);
                break;
            case PropertyTags.SHOWSIDETABBAR:
                if (isSingle) {
                    if (!set) {
                        currentGUI.setupSplitPaneDivider(0, set);
                    } else {
                        currentGUI.setupSplitPaneDivider(5, set);
                    }
                }
            default:
                used = false;
                break;
        }
        return used;
    }
    
    private static boolean alterNavButtonProperties(final String value, final boolean set, final GUIFactory currentGUI){
        
        int propertyCode = value.hashCode();
        boolean used = true;
        //Disable entire section
        switch (propertyCode) {
            case PropertyTags.FIRSTBOTTOM:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.FIRSTPAGE, set);
                break;
            case PropertyTags.BACK10BOTTOM:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.FBACKPAGE, set);
                break;
            case PropertyTags.BACKBOTTOM:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.BACKPAGE, set);
                break;
            case PropertyTags.GOTOBOTTOM:
                currentGUI.enablePageCounter(PageCounter.ALL, set, set);
                break;
            case PropertyTags.FORWARDBOTTOM:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.FORWARDPAGE, set);
                break;
            case PropertyTags.FORWARD10BOTTOM:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.FFORWARDPAGE, set);
                break;
            case PropertyTags.LASTBOTTOM:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.LASTPAGE, set);
                break;
            default :
                used = false;
                break;
        }
        
        return used;
    }
    
    private static boolean alterDisplayButtonProperties(final String value, final boolean set, final GUIFactory currentGUI){
        
        int propertyCode = value.hashCode();
        boolean used = true;
        //Disable entire section
        switch (propertyCode) {
            case PropertyTags.SINGLEBOTTOM:
                currentGUI.getButtons().getButton(Commands.SINGLE).setVisible(set);
                break;
            case PropertyTags.CONTINUOUSBOTTOM:
                currentGUI.getButtons().getButton(Commands.CONTINUOUS).setVisible(set);
                break;
            case PropertyTags.CONTINUOUSFACINGBOTTOM:
                currentGUI.getButtons().getButton(Commands.CONTINUOUS_FACING).setVisible(set);
                break;
            case PropertyTags.FACINGBOTTOM:
                currentGUI.getButtons().getButton(Commands.FACING).setVisible(set);
                break;
            case PropertyTags.PAGEFLOWBOTTOM:
                currentGUI.getButtons().getButton(Commands.PAGEFLOW).setVisible(set);
                break;
            case PropertyTags.MEMORYBOTTOM:
                currentGUI.enableMemoryBar(set, set);
                break;
            default :
                used = false;
                break;
        }
        
        return used;
    }
    
    private static boolean alterOptionPaneProperties(final String value, final boolean set, final GUIFactory currentGUI) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.SCALINGDISPLAY:
                currentGUI.getCombo(Commands.SCALING).setEnabled(set);
                currentGUI.getCombo(Commands.SCALING).setVisibility(set);
                break;
            case PropertyTags.ROTATIONDISPLAY:
                currentGUI.getCombo(Commands.ROTATION).setEnabled(set);
                currentGUI.getCombo(Commands.ROTATION).setVisibility(set);
                break;
            case PropertyTags.IMAGEOPDISPLAY:
                currentGUI.getCombo(Commands.QUALITY).setEnabled(set);
                currentGUI.getCombo(Commands.QUALITY).setVisibility(set);
                break;
            case PropertyTags.PROGRESSDISPLAY:
                currentGUI.enableStatusBar(set, set);
                break;
            case PropertyTags.DOWNLOADPROGRESSDISPLAY:
                currentGUI.enableDownloadBar(set, set);
                break;
            default :
                used = false;                
                break;
        }
        return used;
    }
    
    private static boolean alterButtonBarProperties(final String value, final boolean set, final GUIFactory currentGUI) {
    
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.OPENFILEBUTTON:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.OPENFILE, set);
                break;
            case PropertyTags.PRINTBUTTON:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.PRINT, set);
                break;
            case PropertyTags.SEARCHBUTTON:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.FIND, set);
                break;
            case PropertyTags.PROPERTIESBUTTON:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.DOCINFO, set);
                break;
            case PropertyTags.ABOUTBUTTON:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.ABOUT, set);
                break;
            case PropertyTags.SNAPSHOTBUTTON:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.SNAPSHOT, set);
                break;
            //
            case PropertyTags.CURSORBUTTON:
                currentGUI.enableCursor(set, set);
                break;
            case PropertyTags.MOUSEMODEBUTTON:
                setButtonEnabledAndVisible(currentGUI.getButtons(), Commands.MOUSEMODE, set);
                break;
            default :
                used = false;
        }
        
        return used;
    }
    
    private static boolean alterSideBarProperties(final String value, final boolean set, final GUIFactory currentGUI) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        int tabCount = currentGUI.getSidebarTabCount();
        if (tabCount != 0) {
            switch (propertyCode) {
                case PropertyTags.PAGETAB:
                    removeUnwantedTabs(tabCount, currentGUI, set, Messages.getMessage("PdfViewerJPanel.thumbnails"));
                    break;
                case PropertyTags.BOOKMARKSTAB:
                    removeUnwantedTabs(tabCount, currentGUI, set, Messages.getMessage("PdfViewerJPanel.bookmarks"));
                    break;
                case PropertyTags.LAYERSTAB:
                    removeUnwantedTabs(tabCount, currentGUI, set, Messages.getMessage("PdfViewerJPanel.layers"));
                    break;
                case PropertyTags.SIGNATURESTAB:
                    removeUnwantedTabs(tabCount, currentGUI, set, Messages.getMessage("PdfViewerJPanel.signatures"));
                    break;
                default:
                    used = false;
                    break;
            }
        }
        return used;
    }
    
    private static void alterMenuBarProperties(final String value, final boolean set, final GUIFactory currentGUI, final boolean isSingle) {
        
        boolean skipOthers = alterFileMenuItemProperties(value, set, currentGUI, currentGUI.getMenuItems());
        
        if(!skipOthers){
            skipOthers = alterEditMenuItemProperties(value, set, currentGUI.getMenuItems());
        }
        if(!skipOthers){
            skipOthers = alterViewMenuItemProperties(value, set, isSingle, currentGUI.getMenuItems());
        }
        if(!skipOthers){
            skipOthers = alterWindowMenuItemProperties(value, set, currentGUI.getMenuItems());
        }
        if(!skipOthers){
            skipOthers = alterExportMenuItemProperties(value, set, currentGUI.getMenuItems());
        }
        if(!skipOthers){
            skipOthers = alterHelpMenuItemProperties(value, set, currentGUI.getMenuItems());
        }
        
        if (skipOthers) {//Menu bar altered so double check separators
            currentGUI.getButtons().checkButtonSeparators();
        }
    }
     
    private static boolean alterEditMenuItemProperties(final String value, final boolean set, final GUIMenuItems menuItems) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.EDITMENU:
                menuItems.setMenuItem(Commands.EDITMENU, set, set);
                break;
            case PropertyTags.COPY:
                menuItems.setMenuItem(Commands.COPY, set, set);
                break;
            case PropertyTags.SELECTALL:
                menuItems.setMenuItem(Commands.SELECTALL, set, set);
                break;
            case PropertyTags.DESELECTALL:
                menuItems.setMenuItem(Commands.DESELECTALL, set, set);
                break;
            case PropertyTags.PREFERENCES:
                menuItems.setMenuItem(Commands.PREFERENCES, set, set);
                break;
            default:
                used = false;
                break;
        }

        return used;
    }
     
    private static boolean alterViewMenuItemProperties(final String value, final boolean set, final boolean isSingle, final GUIMenuItems menuItems) {
        
        boolean skipOthers = alterPageNavMenuItemProperties(value, set, menuItems);
        
        if(!skipOthers){
            skipOthers = alterPageDisplayMenuItemProperties(value, set, isSingle, menuItems);
        }
        
        if(!skipOthers){
            skipOthers = alterDisplayOptionMenuItemProperties(value, set, menuItems);
        }
        
        return skipOthers;
    }
    
    private static boolean alterPageNavMenuItemProperties(final String value, final boolean set, final GUIMenuItems menuItems) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.VIEWMENU:
                menuItems.setMenuItem(Commands.VIEWMENU, set, set);
                break;
            case PropertyTags.GOTOMENU:
                menuItems.setMenuItem(Commands.GOTOMENU, set, set);
                break;
            case PropertyTags.FIRSTPAGE:
                menuItems.setMenuItem(Commands.FIRSTPAGE, set, set);
                break;
            case PropertyTags.BACKPAGE:
                menuItems.setMenuItem(Commands.BACKPAGE, set, set);
                break;
            case PropertyTags.FORWARDPAGE:
                menuItems.setMenuItem(Commands.FORWARDPAGE, set, set);
                break;
            case PropertyTags.LASTPAGE:
                menuItems.setMenuItem(Commands.LASTPAGE, set, set);
                break;
            case PropertyTags.GOTO:
                menuItems.setMenuItem(Commands.GOTO, set, set);
                break;
            case PropertyTags.PREVIOUSDOCUMENT:
                menuItems.setMenuItem(Commands.PREVIOUSDOCUMENT, set, set);
                break;
            case PropertyTags.NEXTDOCUMENT:
                menuItems.setMenuItem(Commands.NEXTDOCUMENT, set, set);
                break;
            default:
                used = false;
                break;
        }

        return used;
    }
    
    private static boolean alterPageDisplayMenuItemProperties(final String value, final boolean set, final boolean isSingle, final GUIMenuItems menuItems) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.PAGELAYOUTMENU:
                if (isSingle) {
                    menuItems.setMenuItem(Commands.PAGELAYOUTMENU, set, set);
                }
                break;
            case PropertyTags.SINGLE:
                if (isSingle) {
                    menuItems.setMenuItem(Commands.SINGLE, set, set);
                }
                break;
            case PropertyTags.CONTINUOUS:
                if (isSingle) {
                    menuItems.setMenuItem(Commands.CONTINUOUS, set, set);
                }
                break;
            case PropertyTags.FACING:
                if (isSingle) {
                    menuItems.setMenuItem(Commands.FACING, set, set);
                }
                break;
            case PropertyTags.CONTINUOUSFACING:
                if (isSingle) {
                    menuItems.setMenuItem(Commands.CONTINUOUS_FACING, set, set);
                }
                break;
            case PropertyTags.PAGEFLOW:
                if (isSingle && menuItems.isMenuItemExist(Commands.PAGEFLOW)) {
                    menuItems.setMenuItem(Commands.PAGEFLOW, set, set);
                }
                break;
            default:
                used = false;
                break;
        }

        return used;
    }
    
    private static boolean alterDisplayOptionMenuItemProperties(final String value, final boolean set, final GUIMenuItems menuItems) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.PANMODE:
                if (menuItems.isMenuItemExist(Commands.PANMODE)) {
                    menuItems.setMenuItem(Commands.PANMODE, set, set);
                }
                break;
            case PropertyTags.TEXTSELECT:
                if (menuItems.isMenuItemExist(Commands.TEXTSELECT)) {
                    menuItems.setMenuItem(Commands.TEXTSELECT, set, set);
                }
                break;
            case PropertyTags.FULLSCREEN:
                menuItems.setMenuItem(Commands.FULLSCREEN, set, set);
                break;
            case PropertyTags.SEPARATECOVER:
                if (menuItems.isMenuItemExist(Commands.SEPARATECOVER)) {
                    menuItems.setMenuItem(Commands.SEPARATECOVER, set, set);
                }
                break;
            default:
                used = false;
                break;
        }

        return used;
    }
    
    private static boolean alterWindowMenuItemProperties(final String value, final boolean set, final GUIMenuItems menuItems) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.WINDOWMENU:
                if (menuItems.isMenuItemExist(Commands.WINDOWMENU)) {
                    menuItems.setMenuItem(Commands.WINDOWMENU, set, set);
                }
                break;
            case PropertyTags.CASCADE:
                if (menuItems.isMenuItemExist(Commands.WINDOWMENU)) {
                    menuItems.setMenuItem(Commands.CASCADE, set, set);
                }
                break;
            case PropertyTags.TILE:
                if (menuItems.isMenuItemExist(Commands.WINDOWMENU)) {
                    menuItems.setMenuItem(Commands.TILE, set, set);
                }
                break;
            default:
                used = false;
                break;
        }

        return used;
    }
    
    private static boolean alterExportMenuItemProperties(final String value, final boolean set, final GUIMenuItems menuItems) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.EXPORTMENU:
                menuItems.setMenuItem(Commands.EXPORTMENU, set, set);
                break;
            case PropertyTags.PDFMENU:
                menuItems.setMenuItem(Commands.PDFMENU, set, set);
                break;
            case PropertyTags.ONEPERPAGE:
                menuItems.setMenuItem(Commands.ONEPERPAGE, set, set);
                break;
            case PropertyTags.NUP:
                menuItems.setMenuItem(Commands.NUP, set, set);
                break;
            case PropertyTags.HANDOUTS:
                menuItems.setMenuItem(Commands.HANDOUTS, set, set);
                break;
            case PropertyTags.CONTENTMENU:
                menuItems.setMenuItem(Commands.CONTENTMENU, set, set);
                break;
            case PropertyTags.IMAGES:
                menuItems.setMenuItem(Commands.IMAGES, set, set);
                break;
            case PropertyTags.TEXT:
                menuItems.setMenuItem(Commands.TEXT, set, set);
                break;
            case PropertyTags.BITMAP:
                menuItems.setMenuItem(Commands.BITMAP, set, set);
                break;
            case PropertyTags.PAGETOOLSMENU:
                menuItems.setMenuItem(Commands.PAGETOOLSMENU, set, set);
                break;
            case PropertyTags.ROTATEPAGES:
                menuItems.setMenuItem(Commands.ROTATE, set, set);
                break;
            case PropertyTags.DELETEPAGES:
                menuItems.setMenuItem(Commands.DELETE, set, set);
                break;
            case PropertyTags.ADDPAGE:
                menuItems.setMenuItem(Commands.ADD, set, set);
                break;
            case PropertyTags.ADDHEADERFOOTER:
                menuItems.setMenuItem(Commands.ADDHEADERFOOTER, set, set);
                break;
            case PropertyTags.STAMPTEXT:
                menuItems.setMenuItem(Commands.STAMPTEXT, set, set);
                break;
            case PropertyTags.STAMPIMAGE:
                menuItems.setMenuItem(Commands.STAMPIMAGE, set, set);
                break;
            case PropertyTags.CROP:
                menuItems.setMenuItem(Commands.CROP, set, set);
                break;
            default:
                used = false;
                break;
        }

        return used;
    }
    
    private static boolean alterHelpMenuItemProperties(final String value, final boolean set, final GUIMenuItems menuItems) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.HELPMENU:
                menuItems.setMenuItem(Commands.HELP, set, set);
                break;
            case PropertyTags.VISITWEBSITE:
                menuItems.setMenuItem(Commands.VISITWEBSITE, set, set);
                break;
            case PropertyTags.TIPOFTHEDAY:
                menuItems.setMenuItem(Commands.TIP, set, set);
                break;
            case PropertyTags.ABOUT:
                menuItems.setMenuItem(Commands.ABOUT, set, set);
                break;
            //
            default:
                used = false;
                break;
        }

        return used;
    }
    
    private static boolean alterFileMenuItemProperties(final String value, final boolean set, final GUIFactory currentGUI, final GUIMenuItems menuItems) {
        
        int propertyCode = value.hashCode();
        boolean used = true;
        
        switch (propertyCode) {
            case PropertyTags.FILEMENU:
                menuItems.setMenuItem(Commands.FILEMENU, set, set);
                break;
            case PropertyTags.OPENMENU:
                menuItems.setMenuItem(Commands.OPENMENU, set, set);
                break;
            case PropertyTags.OPEN:
                menuItems.setMenuItem(Commands.OPENFILE, set, set);
                break;
            case PropertyTags.OPENURL:
                menuItems.setMenuItem(Commands.OPENURL, set, set);
                break;
            case PropertyTags.SAVE:
                menuItems.setMenuItem(Commands.SAVE, set, set);
                break;
            case PropertyTags.RESAVEASFORMS:
                if (menuItems.isMenuItemExist(Commands.RESAVEASFORM)) { //will not be initialised if Itext not on path
                    menuItems.setMenuItem(Commands.RESAVEASFORM, set, set);
                }
                break;
            case PropertyTags.FIND:
                menuItems.setMenuItem(Commands.FIND, set, set);
                break;
            case PropertyTags.DOCUMENTPROPERTIES:
                menuItems.setMenuItem(Commands.DOCINFO, set, set);
                break;
            case PropertyTags.SIGNPDF:
                menuItems.setMenuItem(Commands.SIGN, set, set);
                break;
            case PropertyTags.PRINT:
                menuItems.setMenuItem(Commands.PRINT, set, set);
                break;
            case PropertyTags.RECENTDOCUMENTS:
                currentGUI.getRecentDocument().enableRecentDocuments(set);
                break;
            case PropertyTags.EXIT:
                menuItems.setMenuItem(Commands.EXIT, set, set);
                break;
            default:
                used = false;
                break;
        }
        return used;
    }
    
    /**
     * The method alters a single property value related to the GUI.
     * 
     * @param value Name of the property to be altered as a String.
     * @param set The value you wish to set for the property as a boolean.
     * @param currentGUI The GUI interface the property is applied to.
     */
    public static void alterProperty(final String value, final boolean set, final GUIFactory currentGUI) {
        
        boolean skipOthers = alterSectionProperties(value, set, currentGUI, currentGUI.isSingle());
        
        if(!skipOthers){
            skipOthers = alterNavButtonProperties(value, set, currentGUI);
        }
        
        if(!skipOthers){
            skipOthers = alterDisplayButtonProperties(value, set, currentGUI);
        }
        
        if(!skipOthers){
            skipOthers = alterOptionPaneProperties(value, set, currentGUI);
        }
        
        if(!skipOthers){
            skipOthers = alterButtonBarProperties(value, set, currentGUI);
        }
        
        if(!skipOthers){
            skipOthers = alterSideBarProperties(value, set, currentGUI);
        }
        
        if(!skipOthers){
            alterMenuBarProperties(value, set, currentGUI, currentGUI.isSingle());
        }
        
    }
}
