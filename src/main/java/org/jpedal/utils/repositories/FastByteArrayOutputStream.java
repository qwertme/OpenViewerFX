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
 * FastByteArrayOutputStream.java
 * ---------------
 */
package org.jpedal.utils.repositories;

/**
 * Provides a fast version to replace default Java version (which synchronizes everything!)
 */
public class FastByteArrayOutputStream
{
    
    //how much we resize each time - will be doubled up to 160
    int  increment_size = 1000;
    protected int  current_item;
    
    //current max size
    int  max_size = 250;
    
    //holds the data
    protected byte[] items;
    
    //default size
    public FastByteArrayOutputStream()
    {
        items = new byte[max_size];
    }
    
    
    protected static int incrementSize(int increment_size){
        
        if(increment_size<8000) {
            increment_size *= 4;
        } else if(increment_size<320000) {
            increment_size *= 2;
        } else {
            increment_size += 320000;
        }
        
        return increment_size;
    }
    
    public byte[] toByteArray(){
        final byte[] newbuf = new byte[current_item];
        System.arraycopy(items, 0, newbuf, 0, current_item);
        return newbuf;
    }
    
    //set size
    public FastByteArrayOutputStream(final int number)
    {
        max_size = number;
        items = new byte[max_size];
    }
    
    
    /**
     * set an element
     */
    public final void write( final byte new_name )
    {
        if( current_item+1 >= max_size ) {
            checkSize( current_item+1 );
        }
        
        items[current_item] = new_name;
        
        current_item++;
    }
    
    /**
     * set an element
     */
    public final void write( final int new_name )
    {
        if( current_item+1 >= max_size ) {
            checkSize( current_item+1 );
        }
        
        items[current_item] = (byte)(new_name & 255);
        
        current_item++;
    }
    
    /**
     * set an element
     */
    public final void write( final byte[] new_name, final int start, final int len )
    {
        final int size=len-start;
        
        if( current_item+size >= max_size ) {
            checkSize( current_item+size );
        }
        
        for(int i=0;i<size;i++) {
            items[current_item] = new_name[i+start];
            current_item++;
        }
    }
    
    /**
     * set an element
     */
    public final void write( final byte[] new_name )
    {
        final int size=new_name.length;
        
        if( current_item+size >= max_size ) {
            checkSize( current_item+size );
        }

        for (final byte aNew_name : new_name) {
            items[current_item] = aNew_name;
            current_item++;
        }
    }

    /**
     * return the size+1 as in last item (so an array of 0 values is 1) if added
     * If using set, use checkCapacity
     */
    public final int size()
    {
        return current_item;
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
            final byte[] temp = items;
            items = new byte[max_size];
            
            
            System.arraycopy( temp, 0, items, 0, old_size );
            
            increment_size=incrementSize(increment_size);
            
        }
        
    }
    
    
    
}
