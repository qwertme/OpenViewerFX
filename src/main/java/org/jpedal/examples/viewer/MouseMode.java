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
 * MouseMode.java
 * ---------------
 */
package org.jpedal.examples.viewer;

/**
 * tracks option for our Viewer
 */
public class MouseMode {

    public static final int MOUSE_MODE_TEXT_SELECT = 0;

    public static final int MOUSE_MODE_PANNING = 1;

    int mouseMode;


    public int getMouseMode() {
        return mouseMode;
    }

    public void setMouseMode(final int mouseMode) {
        this.mouseMode = mouseMode;
    }


}
