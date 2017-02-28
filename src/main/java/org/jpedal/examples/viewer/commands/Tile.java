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
 * Tile.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.beans.PropertyVetoException;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.LogWriter;

/**
 * Tiles all the window frames
 */
public class Tile {

    public static void execute(final Object[] args, final GUIFactory currentGUI) {
        if (args == null) {
            tile(currentGUI);
        } else {

        }

    }
    private static void tile(final GUIFactory currentGUI) {
        
        final JDesktopPane desktopPane = (JDesktopPane)currentGUI.getMultiViewerFrames();

        final JInternalFrame[] frames = desktopPane.getAllFrames();

        // count frames that aren't iconized
        int frameCount = 0;
        for (final JInternalFrame frame1 : frames) {
            if (!frame1.isIcon()) {
                frameCount++;
            }
        }

        int rows = (int) Math.sqrt(frameCount);
        final int cols = frameCount / rows;
        final int extra = frameCount % rows;
        // number of columns with an extra row

        final int width = desktopPane.getWidth() / cols;
        int height = desktopPane.getHeight() / rows;
        int r = 0;
        int c = 0;
        for (final JInternalFrame frame : frames) {
            if (!frame.isIcon()) {
               try {
                    frame.setMaximum(false);
                    frame.reshape(c * width, r * height, width, height);
                    r++;
                    if (r == rows) {
                        r = 0;
                        c++;
                        if (c == cols - extra) { // start adding an extra row
                            rows++;
                            height = desktopPane.getHeight() / rows;
                        }
                    }
                } catch (final PropertyVetoException e) {
                    //
                    
                     if(LogWriter.isOutput()) { 
                         LogWriter.writeLog("Exception attempting to set size" + e); 
                     } 
                }
            }
        }
    }
}
