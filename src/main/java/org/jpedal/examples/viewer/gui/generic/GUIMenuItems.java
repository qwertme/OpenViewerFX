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
 * GUIMenuItems.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.generic;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.CommandListener;
import org.jpedal.examples.viewer.utils.PropertiesFile;

/**
 * This class controls everything todo with Menu Items, it holds the objects and
 * their corresponding methods.
 *
 * To initialise the object/class call init()
 */
public abstract class GUIMenuItems {

    protected final PropertiesFile properties;
    
    protected CommandListener currentCommandListener;

    public GUIMenuItems(final PropertiesFile properties) {
        this.properties=properties;
    }
    
    public abstract void addToMenu(Object menuItem, int parentMenuID);
    
    public abstract void ensureNoSeperators();
    
    public abstract void ensureNoSeperators(int type);
    
    public abstract boolean isMenuItemExist(int ID);
    
    public abstract void setMenuItem(int ID, boolean enabled, boolean visible);
    
    public abstract void setCheckMenuItemSelected(int ID, boolean b);
    
    public abstract void dispose();
    
    public abstract void setBackNavigationItemsEnabled(boolean enabled);
    
    public abstract void setForwardNavigationItemsEnabled(boolean enabled);
    
    public abstract void setGoToNavigationItemEnabled(boolean enabled);
    
    public abstract void setMenusForDisplayMode(int commandIDForDislayMode,int mouseMode);
    
    /**
     * create items on drop down menus
     */
	public abstract void createMainMenu(boolean includeAll, CommandListener currentCommandListener,boolean isSingle, 
            Values commonValues, Commands currentCommands, GUIButtons swButtons);
}
