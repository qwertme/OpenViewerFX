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
 * GUIButtons.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.generic;

import org.jpedal.gui.GUIFactory;

/**
 * Abstract class so we can use getButtons and instantiate JavaFXButtons.java and
 * Buttons.java objects in either SwingGUI.java or JavaFXGUI.java which ensures
 * Swing code(Buttons.java) stays in SwingGUI.java and JavaFX code(JavaFXButtons.java)
 * stays in JavaFXGUI, keeping GUI.java generic.
 */
public interface GUIButtons {
    
    GUIButton getButton(int ID);
    
    void setBackNavigationButtonsEnabled(boolean flag);
    
    void setForwardNavigationButtonsEnabled(boolean flag);
    
    void checkButtonSeparators();
    
    void setVisible(boolean set);
    
    void setEnabled(boolean set);
    
    void hideRedundentNavButtons(GUIFactory currentGUI);
    
    void alignLayoutMenuOption(int mode);
    
    void setPageLayoutButtonsEnabled(boolean flag);
    
}
