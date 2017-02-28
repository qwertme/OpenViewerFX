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
 * Tip.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Container;
import org.jpedal.examples.viewer.gui.popups.TipOfTheDay;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;

/**
 * Code to handle the Tip of the day feature we display in theViewer
 */
public class Tip {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final PropertiesFile properties) {
        if(args==null){
                    final TipOfTheDay tipOfTheDay = new TipOfTheDay((Container)currentGUI.getFrame(), "/org/jpedal/examples/viewer/res/tips", properties);
                    tipOfTheDay.setVisible(true);
                }else{
                    
                }
    }
}
