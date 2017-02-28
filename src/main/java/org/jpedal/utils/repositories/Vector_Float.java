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
 * Vector_Float.java
 * ---------------
 */
package org.jpedal.utils.repositories;
import java.io.Serializable;

/**
 * Provides the functionality/convenience of a Vector for floats
 *
 * Much faster because not synchronized and no cast
 * Does not double in size each time
 */
public class Vector_Float implements Serializable 
{

    //how much we resize each time - will be doubled up to 160
    int  increment_size = 1000;
    protected int  current_item;

    //current max size
    int  max_size = 250;


    //holds the data
	private float[] items = new float[max_size];
	
	//default size
	public Vector_Float() 
	{
		
	}
	
	//set size
	public Vector_Float( final int number )
	{
		max_size = number;
		items = new float[max_size];
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

	/**
	 * extract underlying data
	 */
    public final float[] get()
	{
		return items;
	}

	/**
	 * add an item
	 */
    public final void addElement( final float value )
	{
        if(current_item>=max_size) {
            checkSize( current_item );
        }
        
		items[current_item] = value;
		current_item++;
	}

	/**
	 * remove element at
	 */
    public final float elementAt( final int id )
	{
		if( id >= max_size ) {
                    return 0f;
                } else {
                    return items[id];
                }
	}
	///////////////////////////////////
	/**
	 * clear the array
	 */
    public final void clear()
	{
		//items = null;
		//holds the data
		//items = new float[max_size];
		if(current_item>0){
			for(int i=0;i<current_item;i++) {
                            items[i]=0f;
                        }
		}else{
			for(int i=0;i<max_size;i++) {
                            items[i]=0f;
                        }
		}
		current_item = 0;
	}
	
	/**
	 * recycle the array by just resetting the pointer
	 */
    public final void reuse()
	{
		current_item = 0;
	}

	/**
	 * check the size of the array and increase if needed
	 */
    private void checkSize( final int i )
	{
		
        final int old_size = max_size;
        max_size += increment_size;

        //allow for it not creating space
        if( max_size <= i ) {
            max_size = i +increment_size+ 2;
        }
        final float[] temp = items;
        items = new float[max_size];
        System.arraycopy( temp, 0, items, 0, old_size );

        //increase size increase for next time
        increment_size=incrementSize(increment_size);
		
	}
	
	public void trim(){
		
		final float[] newItems = new float[current_item];
		
		System.arraycopy(items,0,newItems,0,current_item);
		
		items=newItems;
		max_size=current_item;
	}

    private int checkPoint=-1;

    /**
     * used to store end of PDF components
     */
    public void resetToCheckpoint() {

        if(checkPoint!=-1) {
            current_item=checkPoint;
        }
        
        //System.err.println("CheckPoint Reset to: " + current_item);
        
        checkPoint=-1;
    }

    /**
     * used to rollback array to point
     */
    public void setCheckpoint() {

        if(checkPoint==-1 && current_item>=0) {
            checkPoint=current_item;
            
            //System.out.println("CheckPoint Set : " + current_item);
        }    
    }
}
