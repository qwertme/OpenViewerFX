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
 * GUIFactory.java
 * ---------------
 */
package org.jpedal.gui;

import java.util.Map;

import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.*;
import org.jpedal.examples.viewer.gui.GUI.PageCounter;
import org.jpedal.examples.viewer.gui.GUI.ScrollPolicy;
import org.jpedal.examples.viewer.gui.SwingCursor;
import org.jpedal.examples.viewer.gui.generic.GUIButtons;
import org.jpedal.examples.viewer.gui.generic.GUICombo;
import org.jpedal.examples.viewer.gui.generic.GUIMenuItems;
import org.jpedal.examples.viewer.gui.generic.GUISearchList;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.examples.viewer.paper.PaperSizes;
import org.jpedal.examples.viewer.utils.PropertiesFile;

@SuppressWarnings("UnusedDeclaration")
public interface GUIFactory {

    int BUTTONBAR = 0;
    int NAVBAR = 1;
    int PAGES = 2;
    
    
    //<start-demo><end-demo>
    
    
    /**
     * flag used to show opening of multiple PDFs
     */
    Integer MULTIPAGE = 1;

    /**
     * access to command object
     */
    org.jpedal.examples.viewer.Commands getCommand();
    
    /**
     * align rotation combo box to default for page
     */
    void resetRotationBox();

    /**
     * main method to initialise Swing specific code and create GUI display
     */
    void init(Commands currentCommands, Object currentPrinter);

    /**
     * set title or over-ride with message
     */
    void setViewerTitle(String title);

    /**
     * set all 3 combo boxes to isEnabled(value)
     */
    void resetComboBoxes(boolean value);

    /**
     * zoom into page
     */
    void scaleAndRotate();

    /**
     * get current rotation
     */
    int getRotation();

    /**
     * get current scaling
     */
    float getScaling();

    /**
     * get inset between edge of JPanel and PDF page
     */
    //	public int getPDFDisplayInset();
    /**
     * read value from rotation box and apply - called by combo listener
     */
    void rotate();

    /**
     * toggle state of autoscrolling on/off
     */
    void toogleAutoScrolling();

    void setupThumbnailPanel();
    
    void setAutoScrolling(boolean autoScroll);
    /**
     * remove outlines and flag for redraw
     */
    //public void removeOutlinePanels();
    /**
     * flush list of pages decoded
     */
    void setNoPagesDecoded();

    /**
     * set text displayed in cursor co-ordinates box
     */
    void setCoordText(String string);

    /**
     * set page number at bottom of screen
     */
    void setPageNumber();

    //

    /**
     * allow access to root frame if required
     */
    Object getFrame();

    void resetNavBar();

    void showMessageDialog(String message1);
    
    int showMessageDialog(Object message1, Object[] options, int selectedChoice);

    void showMessageDialog(Object message, String title, int type);

    String showInputDialog(Object message, String title, int type);

    String showInputDialog(String message);

    void showMessageDialog(Object info);

    int showConfirmDialog(String message, String message2, int option);

    int showOverwriteDialog(String file, boolean yesToAllPresent);

    void showFirstTimePopup();

    int showConfirmDialog(Object message, String title, int optionType, int messageType);

    /**
     * show if user has set auto-scrolling on or off - if on moves at edge of
     * panel to show more
     */
    boolean allowScrolling();

    /**
     * show is user has set the option to have exit confirmed with a dialog
     */
    boolean confirmClose();

    /**
     * message to show in status object
     */
    void updateStatusMessage(String message);

    void resetStatusMessage(String message);

    /**
     * set current status value 0 -100
     */
    void setStatusProgress(int size);

    Object printDialog(String[] printersList, String defaultPrinter);

    void setQualityBoxVisible(boolean visible);

    void setPage(int newPage);

    Enum getType();

    Object getMultiViewerFrames();
        
    void setBookmarks(boolean alwaysGenerate);
    
    String getBookmark(String bookmark);

    void alterProperty(String value, boolean show);
    
    SwingCursor getGUICursor();

    void setRotationFromExternal(int rotation);
    
    void setResults(GUISearchList results);
    
    void setMultibox(int[] flags);
    
    /**
    * This method returns the object that stores and handles the various preferences for the viewer.
    * The returned object can be used to get property values and set them.
    * @return The PropertiesFile object currently in use by the viewer.
    */
    PropertiesFile getProperties();
    
    void snapScalingToDefaults(float newScaling);
    
    Object getVerticalScrollBar();
    
    String getPropertiesFileLocation();
    
    boolean isSingle();
    
    Object getPageContainer();
    
    PaperSizes getPaperSizes();
    
    void setPropertiesFileLocation(String file);
    
    /**
    * return comboBox or nul if not (QUALITY, SCALING or ROTATION
    * @param ID
    * @return
    */
    GUICombo getCombo(int ID);
    
    /**
    * Method to enable / disable search options on the toolbar.
    */
    void enableSearchItems(boolean enabled);
    
    Object getDisplayPane();
    
    void reinitialiseTabs(boolean showVisible);
    
    void scrollToPage(int page);
    
    Object getStatusBar();
    
    void setTabsNotInitialised(boolean b);
   
    void selectBookmark();
    
    void setScalingFromExternal(String scale);
    
    boolean getPageTurnScalingAppropriate();
    
    void resetPageNav();
    
    void removeSearchWindow(boolean justHide);
    
    Map getHotspots();
     
     void setSearchText(Object searchText);
     
     void stopThumbnails();
     
     Object getThumbnailPanel();
     
     Object getOutlinePanel();
     
     void setDownloadProgress(String message, int percentage);
     
     void reinitThumbnails();
     
     Object getThumbnailScrollBar();
     
     void setThumbnailScrollBarVisibility(boolean v);
     
     void setThumbnailScrollBarValue(int pageNum);
     
     Object getSideTabBar();
     
     int getSplitDividerLocation();
     
     void dispose();
     
     void rescanPdfLayers();
     
     void setRootContainer(Object rawValue);
     
     void setSearchFrame(GUISearchWindow searchFrame);
     
     void searchInTab(GUISearchWindow searchFrame);
     
     void setDisplayMode(Integer mode);
     /**
      * @return a boolean commandInThread
      */
     boolean isCommandInThread();
     
     /**
      * @param b
      * assigns commandInThread to b
      */
     void setCommandInThread(boolean b);
     
     /**
      * @return a boolean executingCommand
      */
     boolean isExecutingCommand();
     
     /**
      * @param b
      * assigns executingCommand to b
      */
     void setExecutingCommand(boolean b);

    PdfDecoderInt getPdfDecoder();
    
    GUIButtons getButtons();
    
    Values getValues();
    
    void setScrollBarPolicy(ScrollPolicy pol);
    
    GUIMenuItems getMenuItems();
    
    void decodePage();

    RecentDocumentsFactory getRecentDocument();
    
    void setRecentDocument();

    void openFile(String fileToOpen);
    
    void open(String fileName);
    
    void enablePageCounter(PageCounter value, boolean enabled, boolean visibility);
    
    void setPageCounterText(PageCounter value, String text);
    
    Object getPageCounter(PageCounter value);
    
    void updateTextBoxSize();
    
    String getTitles(String title);
    
    void enableStatusBar(boolean enabled, boolean visible);
    
    void enableCursor(boolean enabled, boolean visible);
    
    void enableMemoryBar(boolean enabled, boolean visible);
    
    void enableNavigationBar(boolean enabled, boolean visible);
    
    void enableDownloadBar(boolean enabled, boolean visible);
    
    int getSidebarTabCount();
    
    String getSidebarTabTitleAt(int pos);
    
    void removeSidebarTabAt(int pos);

    double getDividerLocation();
    
    float scaleToVisible(float left, float right, float top, float bottom);
    
    int getDropShadowDepth();
    
    void setPannable(boolean pan);
    
    void setupSplitPaneDivider(int size, boolean visibility);
    
    double getStartSize();
    
    void setStartSize(int size);

    void setDisplayView(int SINGLE_PAGE, int DISPLAY_CENTERED);

    void removePageListener();
    
}
