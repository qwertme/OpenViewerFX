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
 * JavaFXCommandListener.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Viewer;
import org.jpedal.examples.viewer.gui.CommandListenerImpl;
import org.jpedal.examples.viewer.gui.generic.GUIButton;

/**
 *
 */
public class JavaFXCommandListener extends CommandListenerImpl implements EventHandler<ActionEvent> {

    
    final Commands currentCommands;
    
    public JavaFXCommandListener(final Commands currentCommands){
        this.currentCommands = currentCommands;
    }
    
    @Override
    public void handle(final ActionEvent t) {

        final Object source = t.getSource();
        final int ID;
        if (source instanceof GUIButton) {
            ID = ((GUIButton) source).getID();
        } else if (source instanceof JavaFXMenuItem) {
            ID = ((JavaFXMenuItem) source).getID();
        } else if (source instanceof JavaFXCombo) {
            ID = ((JavaFXCombo) source).getID();
        } else {
            ID = ((JavaFXID) source).getID();
        }
           if (!Viewer.closeCalled) {
            currentCommands.executeCommand(ID, null);
        } else {
            throw new RuntimeException("No resource to open document, call to close() disposes viewer resources");
        }

    }
    
}
