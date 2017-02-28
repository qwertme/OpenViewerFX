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
 * JavaFXButtons.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.javafx;

import java.util.ArrayList;
import java.util.Iterator;
import org.jpedal.display.GUIDisplay;
import org.jpedal.examples.viewer.gui.generic.GUIMenuItems;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.generic.GUIButton;
import org.jpedal.examples.viewer.gui.generic.GUIButtons;
import org.jpedal.examples.viewer.gui.CommandListener;
import org.jpedal.gui.GUIFactory;
import static org.jpedal.gui.GUIFactory.BUTTONBAR;
import static org.jpedal.gui.GUIFactory.NAVBAR;
import static org.jpedal.gui.GUIFactory.PAGES;

/**
 * This class controls everything todo with GUIButtons, 
 * it holds the objects and their corresponding methods.
 * 
 * To initialise the object/class call init()
 */
public class JavaFXButtons implements GUIButtons{

    /**holds OPEN, INFO,etc*/
    private ToolBar topButtons = new ToolBar();
   
//    Group layoutGroup = new Group ();
    ArrayList<CheckMenuItem> layoutGroup = new ArrayList<CheckMenuItem>();

    private GUIButton mouseMode;

    //Optional Buttons for menu Search
    private GUIButton nextSearch, previousSearch;
    private GUIButton searchButton;

    //Buttons on the function bar
    private GUIButton openButton;
    private GUIButton printButton;

    private GUIButton docPropButton;
    private GUIButton infoButton;
    private GUIButton snapshotButton; //allows user to toggle on/off text/image snapshot

    //Buttons to control the view mode
    private GUIButton singleButton, continuousButton, continuousFacingButton, facingButton, pageFlowButton;

    //Buttons to navigate pages in the document
    private GUIButton first, fback, back, forward, fforward, last;

    //

    private boolean isSingle;

    /**
     * Initialises the buttons
     *
     * @param isSingle is of type boolean
     */
    public void init(final boolean isSingle) {

        this.isSingle = isSingle;

        previousSearch = new JavaFXButton();
        nextSearch = new JavaFXButton();
        searchButton = new JavaFXButton();

        first = new JavaFXButton();
        fback = new JavaFXButton();
        back = new JavaFXButton();
        forward = new JavaFXButton();
        fforward = new JavaFXButton();
        last = new JavaFXButton();

        snapshotButton = new JavaFXButton();

        //

        singleButton = new JavaFXButton();
        continuousButton = new JavaFXButton();
        continuousFacingButton = new JavaFXButton();
        facingButton = new JavaFXButton();

        pageFlowButton = new JavaFXButton();

        openButton = new JavaFXButton();
        printButton = new JavaFXButton();
        docPropButton = new JavaFXButton();
        infoButton = new JavaFXButton();
        mouseMode = new JavaFXButton();
        
        setupButtonStyle();

    }

    /**
     * Returns the button associated with the ID.
     *
     * @param ID is of type Int
     * @return GUIButton object
     */
    @Override
    public GUIButton getButton(final int ID) {

        switch (ID) {
            case Commands.SNAPSHOT:
                return snapshotButton;
            //
            case Commands.ABOUT:
                return infoButton;
            case Commands.DOCINFO:
                return docPropButton;
            case Commands.PRINT:
                return printButton;
            case Commands.OPENFILE:
                return openButton;
            case Commands.CONTINUOUS_FACING:
                return continuousFacingButton;
            case Commands.CONTINUOUS:
                return continuousButton;
            case Commands.PAGEFLOW:
                return pageFlowButton;
            case Commands.FACING:
                return facingButton;
            case Commands.SINGLE:
                return singleButton;
            case Commands.MOUSEMODE:
                return mouseMode;
            case Commands.BACKPAGE:
                return back;
            case Commands.FIRSTPAGE:
                return first;
            case Commands.FBACKPAGE:
                return fback;
            case Commands.FORWARDPAGE:
                return forward;
            case Commands.FFORWARDPAGE:
                return fforward;
            case Commands.LASTPAGE:
                return last;
            case Commands.FIND:
                return searchButton;
            case Commands.PREVIOUSRESULT:
                return previousSearch;
            case Commands.NEXTRESULT:
                return nextSearch;
        }
        return null;
    }

    /**
     * Prepares all objects for the trash collector
     */
    public void dispose() {
        searchButton = null;
        nextSearch = null;
        previousSearch = null;
        first = null;
        fback = null;
        back = null;
        forward = null;
        fforward = null;
        last = null;
        singleButton = null;
        continuousButton = null;
        continuousFacingButton = null;
        facingButton = null;
        pageFlowButton = null;
        snapshotButton = null;
        //

        layoutGroup = null;

        if(topButtons!=null) {
            topButtons.getItems().removeAll(topButtons.getItems());
        }
        topButtons =null;
    }

    /**
     * Enables all the back navigation buttons.
     *
     * @param flag is of type boolean
     */
    @Override
    public void setBackNavigationButtonsEnabled(final boolean flag) {
        back.setEnabled(flag);
        first.setEnabled(flag);
        fback.setEnabled(flag);

    }

    /**
     * Enables all the forward navigation buttons.
     *
     * @param flag is of type boolean
     */
    @Override
    public void setForwardNavigationButtonsEnabled(final boolean flag) {
        forward.setEnabled(flag);
        last.setEnabled(flag);
        fforward.setEnabled(flag);

    }

    /**
     * Enables all the page layout buttons.
     *
     * @param flag is type boolean
     */
    @Override
    public void setPageLayoutButtonsEnabled(final boolean flag) {

        if (!isSingle) {
            return;
        }

        
        continuousButton.setEnabled(flag);
        continuousFacingButton.setEnabled(flag);
        /**
         * Currently Disabled until corresponding view mode are implemented.
         */
        /////////////////
        facingButton.setEnabled(flag);
        pageFlowButton.setEnabled(flag);
        for(int i = Commands.FACING; i < Commands.FULLSCREEN; i++){
            if(i!=Commands.PAGEFLOW){
                disableUnimplementedItems(i);
            }
        }
        /////////////////
        
        final Iterator<CheckMenuItem> menuOptions = layoutGroup.iterator();

		if (menuOptions.hasNext()) {
            CheckMenuItem item = menuOptions.next();
            //first one is always ON
            item.setDisable(false);

            //set other menu items
            while (menuOptions.hasNext()) {
                item = menuOptions.next();
                item.setDisable(!flag);
            }
        }
        //ObservableList<Node> menuOptions = layoutGroup.getChildren();
        
        if(GUI.debugFX) {
            System.out.println("menuOptions variable if statement not yet implemented in JavaFX for JavaFXButtons class");
        }
//        if (menuOptions.isEmpty()) {
//
//            //first one is always ON
//            ((MenuItem) menuOptions.nextElement()).setDisable(flag);
//
//            //set other menu items
//            while (menuOptions.hasMoreElements()) {
//                ((MenuItem) menuOptions.nextElement()).setDisable(flag);
//            }
//        }

    }

    /**
     * Aligns the layout menu option to the current view mode
     * @param mode is of type int
     */
    @Override
    public void alignLayoutMenuOption(final int mode) {

        if(GUI.debugFX) {
            System.out.println("alignLayoutMenuOption not yet implemented for JavaFX in JavaFXButtons.java");
        }
        
        int i = 1;

        //cycle set correct value to true, else false
        for (CheckMenuItem item : layoutGroup) {
            item.setSelected(i == mode);
            i++;
        }
    }

    /**
     * Getter for layoutGroup.
     *
     * @return ButtonGroup object layoutGroup
     */
    public ArrayList<CheckMenuItem> getLayoutGroup() {
        return layoutGroup;
    }
    
    @Override
    public void checkButtonSeparators() {
        /**
         * Ensure the buttonBar doesn't start or end with a separator
         */
        boolean before=false, after=false;
        Separator currentSep=null;
        for(int k=0; k!=topButtons.getItems().size(); k++) {
            if (topButtons.getItems().get(k) instanceof Separator){
                if (currentSep == null) {
                    currentSep = (Separator) topButtons.getItems().get(k);
                } else {
                    if (!before || !after) {
                        currentSep.setVisible(false);
                    } else {
                        currentSep.setVisible(true);
                    }
                    before = before || after;
                    after = false;
                    currentSep = (Separator)topButtons.getItems().get(k);
                }
            } else {
                if (topButtons.getItems().get(k).isVisible()) {
                    if (currentSep == null) {
                        before = true;
                    } else {
                        after = true;
                    }
                }
            }
        }
        if (currentSep != null) {
            if (!before || !after) {
                currentSep.setVisible(false);
            } else {
                currentSep.setVisible(true);
            }
        }
    }
    
    public ToolBar getTopButtons(){
        return topButtons;
    }
    
    /* (non-Javadoc)
     * @see org.jpedal.examples.viewer.gui.swing.GUIFactory#addButton(int, java.lang.String, java.lang.String, int)
     */
    public void addButton(final int line, final String toolTip, final String path, final int ID,
                            final GUIMenuItems fxMenuItems, final GUIFactory currentGUI,
                            final CommandListener currentCommandListener, final ToolBar pagesToolBar, final ToolBar navToolBar) {

        GUIButton newButton = new JavaFXButton();
        
        /**
         * specific buttons
         */
        switch (ID) {

            //

            //

            //

            case Commands.FIRSTPAGE:
                newButton = getButton(Commands.FIRSTPAGE);
                break;
            case Commands.FBACKPAGE:
                newButton = getButton(Commands.FBACKPAGE);
                break;
            case Commands.BACKPAGE:
                newButton = getButton(Commands.BACKPAGE);
                break;
            case Commands.FORWARDPAGE:
                newButton = getButton(Commands.FORWARDPAGE);
                break;
            case Commands.FFORWARDPAGE:
                newButton = getButton(Commands.FFORWARDPAGE);
                break;
            case Commands.LASTPAGE:
                newButton = getButton(Commands.LASTPAGE);
                break;
            case Commands.SNAPSHOT:
                newButton = getButton(Commands.SNAPSHOT);
                break;
            case Commands.SINGLE:
                newButton = getButton(Commands.SINGLE);
                ((JavaFXButton) newButton).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent t) {
                        fxMenuItems.setMenusForDisplayMode(Commands.SINGLE, -1);
                    }
                });
                break;
            case Commands.CONTINUOUS:
                newButton = getButton(Commands.CONTINUOUS);
                newButton.setEnabled(false);
                ((JavaFXButton) newButton).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent t) {
                        fxMenuItems.setMenusForDisplayMode(Commands.CONTINUOUS, -1);
                    }
                });
                break;
            case Commands.CONTINUOUS_FACING:
                newButton = getButton(Commands.CONTINUOUS_FACING);
                newButton.setEnabled(false);
                ((JavaFXButton) newButton).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent t) {
                        fxMenuItems.setMenusForDisplayMode(Commands.CONTINUOUS_FACING, -1);
                    }
                });
                break;
            case Commands.FACING:
                newButton = getButton(Commands.FACING);
                newButton.setEnabled(false);
                break;
            case Commands.PAGEFLOW:
                newButton = getButton(Commands.PAGEFLOW);
                newButton.setEnabled(false);
                break;
            case Commands.PREVIOUSRESULT:
                newButton = getButton(Commands.PREVIOUSRESULT);
                newButton.setEnabled(false);
                newButton.setName("PREVIOUSRESULT");
                break;
            case Commands.NEXTRESULT:
                newButton = getButton(Commands.NEXTRESULT);
                newButton.setEnabled(false);
                newButton.setName("NEXTRESULT");
                break;
            case Commands.OPENFILE:
                newButton = getButton(Commands.OPENFILE);
                newButton.setName("open");
                break;
            case Commands.PRINT:
                newButton = getButton(Commands.PRINT);
                newButton.setName("print");
                break;
            case Commands.FIND:
                newButton = getButton(Commands.FIND);
                newButton.setName("search");
                break;
            case Commands.DOCINFO:
                newButton = getButton(Commands.DOCINFO);
                break;
            case Commands.ABOUT:
                newButton = getButton(Commands.ABOUT);
                break;
            case Commands.MOUSEMODE:
                newButton = getButton(Commands.MOUSEMODE);
                ((JavaFXButton) newButton).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent t) {
                        if (currentGUI.getPdfDecoder().getDisplayView() == Display.SINGLE_PAGE) {
                            fxMenuItems.setMenusForDisplayMode(Commands.MOUSEMODE, currentGUI.getCommand().getMouseMode().getMouseMode());
                        }
                    }
                });
                newButton.setName("mousemode");
                break;
        }

        
        //Changes the cursor style to hand if we enter the buttons area
        ((JavaFXButton) newButton).setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent me) {
                if (GUIDisplay.allowChangeCursor) {
                    ((JavaFXButton) me.getSource()).setCursor(Cursor.HAND);
                }
            }
        });
        
        //Changes the cursor style to default if we exit the buttons area
        ((JavaFXButton) newButton).setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent me) {
                if (GUIDisplay.allowChangeCursor) {
                    ((JavaFXButton) me.getSource()).setCursor(Cursor.DEFAULT);
                }
            }
        });

        newButton.init(currentGUI.getGUICursor().getURLForImage(path), ID, toolTip);

        //add listener
        ((JavaFXButton) newButton).setOnAction((EventHandler) currentCommandListener.getCommandListener());

        final int mode = currentGUI.getValues().getModeOfOperation();

        //add to toolbar
        if (line == BUTTONBAR || mode == Values.RUNNING_PLUGIN) {
            topButtons.getItems().add((Button) newButton);
        } else if (line == NAVBAR) {
            navToolBar.getItems().add((Button) newButton);
        } else if (line == PAGES) {
            pagesToolBar.getItems().add((Button) newButton);
        }
        
        disableUnimplementedItems(ID);
    }
    
	//When page changes make sure only relevant navigation buttons are displayed
	@Override
    public void hideRedundentNavButtons(final GUIFactory currentGUI){

		int maxPages = currentGUI.getPdfDecoder().getPageCount();
		if(currentGUI.getValues().isMultiTiff()){
			maxPages = currentGUI.getValues().getPageCount();
		}

        if (((currentGUI.getPdfDecoder().getDisplayView() == Display.FACING && currentGUI.getPdfDecoder().getPages().getBoolean(Display.BoolValue.SEPARATE_COVER)) ||
                currentGUI.getPdfDecoder().getDisplayView() == Display.CONTINUOUS_FACING) 
                && (maxPages & 1)==1) {
            maxPages--;
        }
        
		if(currentGUI.getValues().getCurrentPage()==1){
			setBackNavigationButtonsEnabled(false);
			currentGUI.getMenuItems().setBackNavigationItemsEnabled(false);
		}else{
			setBackNavigationButtonsEnabled(true);
			currentGUI.getMenuItems().setBackNavigationItemsEnabled(true);
		}
		
		if(currentGUI.getValues().getCurrentPage()==maxPages){
			setForwardNavigationButtonsEnabled(false);
			currentGUI.getMenuItems().setForwardNavigationItemsEnabled(false);
		}else{
			setForwardNavigationButtonsEnabled(true);
			currentGUI.getMenuItems().setForwardNavigationItemsEnabled(true);
		}
		
		currentGUI.getMenuItems().setGoToNavigationItemEnabled(maxPages!=1);
		
        //update single mode toolbar to be visible in only SINGLE if set
        if(currentGUI.getThumbnailScrollBar()!=null){
            if(currentGUI.getPdfDecoder().getDisplayView() == Display.SINGLE_PAGE){
                
                currentGUI.setScrollBarPolicy(GUI.ScrollPolicy.VERTICAL_NEVER);
                currentGUI.setScrollBarPolicy(GUI.ScrollPolicy.HORIZONTAL_NEVER);

                currentGUI.setThumbnailScrollBarVisibility(true);
            }else if (currentGUI.getPdfDecoder().getDisplayView() == Display.PAGEFLOW){

                currentGUI.setScrollBarPolicy(GUI.ScrollPolicy.VERTICAL_NEVER);
                currentGUI.setScrollBarPolicy(GUI.ScrollPolicy.HORIZONTAL_NEVER);

                currentGUI.setThumbnailScrollBarVisibility(false);
            }else{

                currentGUI.setScrollBarPolicy(GUI.ScrollPolicy.VERTICAL_AS_NEEDED);
                currentGUI.setScrollBarPolicy(GUI.ScrollPolicy.HORIZONTAL_AS_NEEDED);

                currentGUI.setThumbnailScrollBarVisibility(false);
            }
        }
	}
    
    /**
    * This is where we setup the Button Styles
    */
    public static void setupButtonStyle(){
        if(GUI.debugFX) {
            System.out.println("setupButtonStyle not yet implemented for JavaFX in JavaFXButtons class");
        }
        //topButtons.setBorder(BorderFactory.createEmptyBorder());
        //topButtons.setLayout(new FlowLayout(FlowLayout.LEADING));
        //topButtons.setFloatable(false);
        //topButtons.setFont(new Font("SansSerif", Font.PLAIN, 8));
    }
    
    @Override
    public void setVisible(final boolean set){
        
        topButtons.setVisible(set);
        
    }
    
    @Override
    public void setEnabled(final boolean set){
        
        //we reverse the flag for FX because setDisabled is the reverse equivalent of the Swing setEnabled.
        topButtons.setDisable(!set);
        
    }
    
    /**
     * Temporary Method to Disable unimplemented Viewer Items.
     * Edit and Remove the items from this method as we implement features.
     * When all items have been implemented, do a search for and remove
     * all instances of this method + the method itself.
     * 
     */
    public void disableUnimplementedItems(final int ID){
        
        final boolean debug = GUI.debugFX;
        final int ALL = -10;
        
        if(ID!=ALL){
            switch(ID){
                case Commands.PRINT:
                    printButton.setEnabled(debug);
                    break;
                case Commands.SNAPSHOT:
                    snapshotButton.setEnabled(debug);
                    break;
                    
                    
                case Commands.CONTINUOUS_FACING:
                    continuousFacingButton.setEnabled(debug);
                    break;
                    //
                case Commands.FACING:
                    facingButton.setEnabled(debug);
                    break;
                    /**/
                    case Commands.PAGEFLOW:
                    pageFlowButton.setEnabled(debug);
                    break;
                    case Commands.CONTINUOUS:
                    continuousButton.setEnabled(debug);
                    break;
            }
        }else{
            printButton.setEnabled(debug);
            snapshotButton.setEnabled(debug);
            continuousFacingButton.setEnabled(debug);
            facingButton.setEnabled(debug);
            continuousButton.setEnabled(debug);
        }
    }
}
