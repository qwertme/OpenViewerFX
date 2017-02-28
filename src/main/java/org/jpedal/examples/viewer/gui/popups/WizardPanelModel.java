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
 * WizardPanelModel.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.popups;

import java.awt.event.KeyListener;
import java.util.Map;
import javax.swing.event.ChangeListener;

/**
 * In order to use the Wizard class you need to implement this interface.
 * The implemented class will contain the contents of the user defined panels
 * and add listeners to components that effect the ability to go to the
 * next panel.  See SignWizardModel.java for an example.
 */

public interface WizardPanelModel 
{
	/**
	 * @return true if the currently displayed panel requires a Finish box instead of a next box.
	 */
	boolean isFinishPanel();
	
	/**
	 * @return The unique ID String of the first JPanel to be shown
	 */
	String getStartPanelID();
	
	/**
	 * @return The ID of the JPanel that should be displayed if next is clicked.
	 */
	String next();
	
	/**
	 * @return The ID of the JPanel that should be displayed if previous is clicked.
	 */
	String previous();
	
	/**
	 * This method can contain any housekeeping you require when the Wizard
	 * is exited.
	 */
	void close();
	
	/**
	 * @return true if the current panel has a previous panel
	 */
	boolean hasPrevious();
	
	/**
	 * @return true if the current panel is able to advance.
	 */
	boolean canAdvance();
	
	/**
	 * In order to use the Card Layout in the wizard class
	 * each JPanel must have a unique String identifier.
	 * ie HashMap (String, JPanel)
	 * 
	 * @return A mapping of ID Strings to JPanels. 
	 */
	Map getJPanels();
	
	/**
	 * A component that is registered will alert the Wizard
	 * class that it should check whether the advance button should be enabled.
	 * 
	 * @param e Alerts Wizard that the registered component effects the Next button enable/disable status
	 */
	void registerNextChangeListeners(ChangeListener e);
	
	/**
	 * The same effect as registerNextChangeListeners(ChangeListener e) except
	 * applied to keys.
	 * 
	 * @param e Alerts Wizard that the registered component effects the Next button enable/disable status
	 */
	void registerNextKeyListeners(KeyListener e);
}
