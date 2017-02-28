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
 * Rotation.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.examples.viewer.Values;
import org.jpedal.gui.GUIFactory;

/**
 * Rotates the current document in the Viewer This class is Generic
 * and does not need a specific Swing/JavaFX implementation.
 */
public class Rotation {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final Values commonValues) {
        if (args == null) {
            if (commonValues.getSelectedFile() != null) {
                currentGUI.rotate();
            }
        } else {
            final int rotation = Integer.parseInt((String) args[0]);
            while (Values.isProcessing()) {
                // wait while we rotate your document
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
            currentGUI.setRotationFromExternal(rotation);
            currentGUI.scaleAndRotate();
        }
    }

}
