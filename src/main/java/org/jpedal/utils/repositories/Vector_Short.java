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
 * Vector_Short.java
 * ---------------
 */
package org.jpedal.utils.repositories;

import java.io.Serializable;

/**
 * Provides the functionality/convenience of a Vector for shorts
 *
 * Much faster because not synchronized and no cast
 * Does not double in size each time
 */
public class Vector_Short implements Serializable
{

    //how much we resize each time - will be doubled up to 160
    int  increment_size = 1000;
    protected int  current_item;

    //current max size
    int  max_size = 250;

    //holds the data
	private short[] items = new short[max_size];


    protected static int incrementSize(int increment_size){

		if(increment_size<8000) {
                    increment_size *= 4;
                } else if(increment_size<16000) {
                    increment_size *= 2;
                } else {
                    increment_size += 2000;
                }
		return increment_size;
	}
    
    //set size
	public Vector_Short( final int number )
	{
		max_size = number;
		items = new short[max_size];
	}
	///////////////////////////////////
    ///////////////////////////////////
	/**
	 * extract underlying data
	 */
    public final short[] get()
	{
		return items;
	}
	///////////////////////////////////
    ///////////////////////////////////
    ///////////////////////////////////
    ///////////////////////////////////
    ///////////////////////////////////
    ////////////////////////////////////

    /////////////////////////////////////

    /////////////////////////////////////

    ///////////////////////////////////
	/**
	 * add an item
	 */
    public final void addElement( final short value )
	{
		checkSize( current_item );
		items[current_item] = value;
		current_item++;
	}

    ////////////////////////////////////
	/**
	 * check the size of the array and increase if needed
	 */
    private void checkSize( final int i )
	{
		
		if( i >= max_size )
		{
			final int old_size = max_size;
			max_size += increment_size;
			
			//allow for it not creating space
			if( max_size <= i ) {
                            max_size = i +increment_size+ 2;
                        }
			final short[] temp = items;
			items = new short[max_size];
			System.arraycopy( temp, 0, items, 0, old_size );
			
			//increase size increase for next time
			increment_size=incrementSize(increment_size);
		}
	
	}

}
