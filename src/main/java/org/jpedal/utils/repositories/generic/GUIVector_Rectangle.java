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
 * GUIVector_Rectangle.java
 * ---------------
 */
package org.jpedal.utils.repositories.generic;

/**
 * Class to Hold Generic code for JavaFXVector_Rectangle and Vector_Rectangle.
 */
public class GUIVector_Rectangle {

    //how much we resize each time - will be doubled up to 160
    protected int increment_size = 1000;
    protected int current_item;

    //current max size
    protected int max_size = 250;

    protected int checkPoint = -1;

    protected static int incrementSize(int increment_size) {

        if (increment_size < 8000) {
            increment_size *= 4;
        } else if (increment_size < 16000) {
            increment_size *= 2;
        } else {
            increment_size += 2000;
        }
        return increment_size;
    }

    /**
     * used to store end of PDF components
     */
    public void resetToCheckpoint() {

        if (checkPoint != -1) {
            current_item = checkPoint;
        }

        checkPoint = -1;
    }

    /**
     * used to rollback array to point
     */
    public void setCheckpoint() {
        if (checkPoint == -1) {
            checkPoint = current_item;
        }
    }

    ///////////////////////////////////
    /**
     * return the size+1 as in last item (so an array of 0 values is 1) if added
     * If using set, use checkCapacity
     */
    public final int size() {
        return current_item + 1;
    }

}
