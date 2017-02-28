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
 * Vector_Shape.java
 * ---------------
 */
package org.jpedal.utils.repositories;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jpedal.io.PathSerializer;

/**
 * Provides the functionality/convenience of a Vector for ints
 *
 * Much faster because not synchronized and no cast
 * Does not double in size each time
 */
public class Vector_Shape implements Serializable
{

    //how much we resize each time - will be doubled up to 160
    int  increment_size = 1000;
    protected int  current_item;

    //current max size
    int  max_size = 250;


    //holds the data
	private Area[] items = new Area[max_size];
	
	
	////////////////////////////////////
	

	//default size
	public Vector_Shape() 
	{
		
	}

    private int checkPoint=-1;

    /**
     * used to store end of PDF components
     */
    public void resetToCheckpoint() {

        if(checkPoint!=-1) {
            current_item=checkPoint;
        }

        checkPoint=-1;
    }

    /**
     * used to rollback array to point
     */
    public void setCheckpoint() {
        if(checkPoint==-1) {
            checkPoint=current_item;
        }
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
	public Vector_Shape( final int number )
	{
		max_size = number;
		items = new Area[max_size];
	}
	
	/**
	 * extract underlying data
	 */
    public final Area[] get()
	{
		return items;
	}


	///////////////////////////////////
	/**
	 * clear the array
	 */
    public final void clear()
	{
		checkPoint = -1;
		//items = null;
		//holds the data
		//items = new Area[max_size];
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

	/**
	 * add an item
	 */
    public final void addElement( final Area value )
	{
		checkSize( current_item );
		items[current_item] = value;
		current_item++;
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
			final Area[] temp = items;
			items = new Area[max_size];
			System.arraycopy( temp, 0, items, 0, old_size );
			
			//increase size increase for next time
			increment_size=incrementSize(increment_size);
		}
	}

	/**
	 * writes out the shapes in this collection to the ByteArrayOutputStream
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param bos - the ByteArrayOutputStream to write out to
	 * @throws IOException
	 */
	public void writeToStream(final ByteArrayOutputStream bos) throws IOException {
		
		final ObjectOutput os=new ObjectOutputStream(bos);
		
		/** size of array as first item */
		os.writeObject(max_size);
		
		/** iterate through the array, and write out each Area individualy */
		for (int i = 0; i < max_size; i++) {
			final Area nextObj = items[i];
			
			if(nextObj == null) {
                            os.writeObject(null);
                        } else{
				final PathIterator pathIterator = nextObj.getPathIterator(new AffineTransform());
				PathSerializer.serializePath(os, pathIterator);
			}
		}
	}

	/**
	 * restore the shapes from the input stream into this collections
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param bis - ByteArrayInputStream to read from
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void restoreFromStream(final ByteArrayInputStream bis) throws IOException, ClassNotFoundException {
		final ObjectInput os=new ObjectInputStream(bis);
		
		/** the number of elements in this collection */
		final int size= (Integer) os.readObject();
				
		max_size = size;
		
		items=new Area[size];
		
		/** 
		 * iterate through each item in the stream and store each object in
		 * the collection
		 */
		for (int i = 0; i < size; i++) {
			final GeneralPath path = PathSerializer.deserializePath(os);
			
			if(path == null) {
                            items[i] = null;
                        } else {
                            items[i] = new Area(path);
                        }
		}
	}
	
	public void trim(){
		
		final Area[] newItems = new Area[current_item];
		
		System.arraycopy(items,0,newItems,0,current_item);
		
		items=newItems;
		max_size=current_item;
	}
}
