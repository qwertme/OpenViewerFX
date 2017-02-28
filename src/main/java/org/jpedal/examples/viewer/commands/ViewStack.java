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
 * ViewStack.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * the view stack type object that allows us to store views as they change and
 * then go back through them as needed.
 *
 * @author Chris Wade
 */
public class ViewStack {

    private final ArrayList ourStack = new ArrayList();
    private int index = -1;
    private int length;

    public Viewable back() {

        if (index - 1 > -1 && index - 1 < length) {
            index--;
            return (Viewable) ourStack.get(index);
        } else {
            return null;
        }
    }

    public Viewable forward() {

        if (index + 1 > -1 && index + 1 < length) {
            index++;
            return (Viewable) ourStack.get(index);
        } else {
            return null;
        }
    }

    public synchronized void add(final int page, final Rectangle location, final Integer scalingType) {
        //check capacity will take the new object and location +1 and +1 for the length.
        ourStack.ensureCapacity(index + 2);
        ourStack.add(index + 1, new Viewable(page, location, scalingType));

        //set the index and length after to ensure correct runing if an exception
        index++;
        length = index + 1;
    }
}
