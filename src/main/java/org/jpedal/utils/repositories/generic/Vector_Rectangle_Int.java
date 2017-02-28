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
 * Vector_Rectangle_Int.java
 * ---------------
 */

package org.jpedal.utils.repositories.generic;

/**
 * A generic class to create and manipulate the raw 
 * x,y,w,h values for Swing/JavaFX rectangles.
 */
public class Vector_Rectangle_Int extends GUIVector_Rectangle {
    //holds the data
    private int[][] items = new int[max_size][4];

    //set size
    public Vector_Rectangle_Int(final int number) {
        max_size = number;
        items = new int[max_size][4];
    }
    
    public Vector_Rectangle_Int() {
    }
    
    /**
     * check the size of the array and increase if needed
     */
    private void checkSize(final int i) {
        if (i >= max_size) {
            final int old_size = max_size;
            max_size += increment_size;

            //allow for it not creating space
            if (max_size <= i) {
                max_size = i + increment_size + 2;
            }

            final int[][] temp = items;
            items = new int[max_size][4];
            System.arraycopy(temp, 0, items, 0, old_size);

            //increase size increase for next time
            increment_size = incrementSize(increment_size);
        }
    }

    
    /**
     * add an item
     */
    public synchronized void addElement(final int[] value )
    {
        checkSize( current_item );
        items[current_item] = value;
        current_item++;
    }
    
    /**
     * clear the array
     */
    public final void clear() {
        checkPoint = -1;
        //items = null;
        //holds the data
        //items = new Rectangle[max_size];
        if (current_item > 0) {
            for (int i = 0; i < current_item; i++) {
                items[i] = null;
            }
        } else {
            for (int i = 0; i < max_size; i++) {
                items[i] = null;
            }
        }
        current_item = 0;
    }
    
    /**
     * Returns two dimensional array
     * containing raw x,y,w,h data for
     * rectangle construction.
     */
    public final int[][] get() {
        return items;
    }
    
    /**
     * Returns an array containing x,y,w,h
     * of one of items[][] elements.
     */
    public final synchronized int[] elementAt(final int id) {
        if (id >= max_size) {
            return null;
        } else {
            final int[] rectParams = new int[4];
            System.arraycopy(items[id], 0, rectParams, 0, rectParams.length);
           return rectParams;
        }
    }
    
    public void trim() {

        final int[][] newItems = new int[current_item][4];

        System.arraycopy(items, 0, newItems, 0, current_item);

        items = newItems;
        max_size = current_item;
    }
  
}
