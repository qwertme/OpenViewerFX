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
 * Cascade.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.beans.PropertyVetoException;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.LogWriter;

/**
 * Cascades all the window frames
 */
public class Cascade {

    public static void execute(final Object[] args, final GUIFactory currentGUI) {
        if (args == null) {
            cascade(currentGUI);
        } else {

        }

    }
    
    private static void cascade(final GUIFactory currentGUI) {
        final JDesktopPane desktopPane = (JDesktopPane)currentGUI.getMultiViewerFrames();

        final JInternalFrame[] frames = desktopPane.getAllFrames();

        /**
         * reverse the order of these frames, so when they are cascaded they
         * will maintain the order they were to start with
         */
        for (int left = 0, right = frames.length - 1; left < right; left++, right--) {
            // exchange the first and last
            final JInternalFrame temp = frames[left];
            frames[left] = frames[right];
            frames[right] = temp;
        }

        int x = 0;
        int y = 0;
        final int width = desktopPane.getWidth() / 2;
        final int height = desktopPane.getHeight() / 2;

        for (final JInternalFrame frame : frames) {
            if (!frame.isIcon()) { // if its minimized leave it there
                try {
                    frame.setMaximum(false);
                    frame.reshape(x, y, width, height);
                    frame.setSelected(true);

                    x += 25;
                    y += 25;
                    // wrap around at the desktop edge
                    if (x + width > desktopPane.getWidth()) {
                        x = 0;
                    }
                    if (y + height > desktopPane.getHeight()) {
                        y = 0;
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
