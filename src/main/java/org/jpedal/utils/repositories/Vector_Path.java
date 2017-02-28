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
 * Vector_Path.java
 * ---------------
 */
package org.jpedal.utils.repositories;
import java.awt.geom.GeneralPath;
import java.io.Serializable;

/**
 * Provides the functionality/convenience of a Vector for ints
 *
 * Much faster because not synchronized and no cast
 * Does not double in size each time
 */
public class Vector_Path implements Serializable
{

    //how much we resize each time - will be doubled up to 160
    int  increment_size = 1000;
    protected int  current_item;

    //current max size
    int  max_size = 250;

    //holds the data
	private GeneralPath[] items = new GeneralPath[max_size];
	
	
	////////////////////////////////////
	

	//default size
	public Vector_Path() 
	{
		
	}

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
	public Vector_Path( final int number )
	{
		max_size = number;
		items = new GeneralPath[max_size];
	}
	
	/**
	 * extract underlying data
	 */
    public final GeneralPath[] get()
	{
		return items;
	}
	///////////////////////////////////
    ////////////////////////////////////
	/**
	 * does nothing
	 *
	static final public boolean contains( Shape value )
	{
		return false;
	}/**/
	///////////////////////////////////
	/**
	 * clear the array
	 */
    public final void clear()
	{
		//items = null;
		//holds the data
		//items = new GeneralPath[max_size];
		if(current_item>0){
			for(int i=0;i<current_item;i++) {
                            items[i]=null;
                        }
		}else{
			for(int i=0;i<max_size;i++) {
                            items[i]=null;
                        }
		}
		current_item = 0;
	}
	///////////////////////////////////
	/**
	 * replace underlying data
	 */
    public final void set( final GeneralPath[] new_items )
	{
		items = new_items;
	}
	///////////////////////////////////
	/**
	 * remove element at
	 */
    public final GeneralPath elementAt( final int id )
	{
		if( id >= max_size ) {
                    return null;
                } else {
                    return items[id];
                }
	}
	///////////////////////////////////
	/**
	 * add an item
	 */
    public final void addElement( final GeneralPath value )
	{
		checkSize( current_item );
		items[current_item] = value;
		current_item++;
	}
	///////////////////////////////////
	/**
     * return the size+1 as in last item (so an array of 0 values is 1)
     */
    public final int size()
	{
		return current_item + 1;
	}

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
			final GeneralPath[] temp = items;
			items = new GeneralPath[max_size];
			System.arraycopy( temp, 0, items, 0, old_size );
			
			//increase size increase for next time
			increment_size=incrementSize(increment_size);
		}
	}
	
	/**
	 * sets the current item
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param current_item
	 */
	public void setCurrent_item(final int current_item) {
		this.current_item = current_item;
	}
}
