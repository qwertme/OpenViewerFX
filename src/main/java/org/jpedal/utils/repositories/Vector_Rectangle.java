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
 * Vector_Rectangle.java
 * ---------------
 */
package org.jpedal.utils.repositories;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.*;
import org.jpedal.io.PathSerializer;
import org.jpedal.utils.repositories.generic.GUIVector_Rectangle;

/**
 * Provides the functionality/convenience of a
 * Vector for Rectangle
 *
 * Much faster because not synchronized and no cast
 * Does not double in size each time
 */
public class Vector_Rectangle extends GUIVector_Rectangle implements Serializable
{
    
    //holds the data
    private Rectangle[] items = new Rectangle[max_size];
    
    //set size
    public Vector_Rectangle( final int number )
    {
        max_size = number;
        items = new Rectangle[max_size];
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
        
        /** iterate through the array, and write out each Rectangle individually */
        for (int i = 0; i < max_size; i++) {
            final Rectangle nextObj = items[i];
            
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
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public void restoreFromStream(final ByteArrayInputStream bis) throws IOException, ClassNotFoundException {
        final ObjectInput os=new ObjectInputStream(bis);
        
        /** the number of elements in this collection */
        final int size= (Integer) os.readObject();
        
        max_size = size;
        
        items=new Rectangle[size];
        
        /**
         * iterate through each item in the stream and store each object in
         * the collection
         */
        for (int i = 0; i < size; i++) {
            final GeneralPath path = PathSerializer.deserializePath(os);
            
            if(path == null) {
                items[i] = null;
            } else {
                items[i] = path.getBounds();
            }
        }
    }
    ////////////////////////////////////
    
    
    //default size
    public Vector_Rectangle()
    {
        
    }
    
    /**
     * add an item
     */
    public synchronized void addElement( final Rectangle value )
    {
        checkSize( current_item );
        items[current_item] = value;
        current_item++;
    }
    
    /**
     * clear the array
     */
    public final void clear()
    {
        checkPoint = -1;
        //items = null;
        //holds the data
        //items = new Rectangle[max_size];
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
     * extract underlying data
     */
    public final Rectangle[] get()
    {
        return items;
    }
    ///////////////////////////////////

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
            
            final Rectangle[] temp = items;
            items = new Rectangle[max_size];
            System.arraycopy( temp, 0, items, 0, old_size );
            
            //increase size increase for next time
            increment_size=incrementSize(increment_size);
        }
    }
    
    public void trim(){
        
        final Rectangle[] newItems = new Rectangle[current_item];
        
        System.arraycopy(items,0,newItems,0,current_item);
        
        items=newItems;
        max_size=current_item;
    }

}
