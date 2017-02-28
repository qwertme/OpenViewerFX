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
 * Vector_Int.java
 * ---------------
 */
package org.jpedal.utils.repositories;
import java.io.Serializable;

/**
 * Provides the functionality/convenience of a Vector for ints - 
 *
 * Much faster because not synchronized and no cast - 
 * Does not double in size each time
 */
public class Vector_Int implements Serializable
{

    //how much we resize each time - will be doubled up to 160
    int  increment_size = 1000;
    protected int  current_item;

    //current max size
    int  max_size = 250;

    //holds the data
    protected int[] items = new int[max_size];

    //default size
    public Vector_Int()
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
    public Vector_Int( final int number )
    {
        max_size = number;
        items = new int[max_size];
    }

    ///////////////////////////////////
    /**
     * get element at
     */
    public final synchronized int elementAt( final int id )
    {
        if( id >= max_size ) {
            return 0;
        } else {
            return items[id];
        }
    }
    ///////////////////////////////////
    /**
     * extract underlying data
     */
    public final int[] get()
    {
        return items;
    }
    ///////////////////////////////////
    /**
     * set an element
     */
    public final void setElementAt( final int new_name, final int id )
    {
        if( id >= max_size ) {
            checkSize( id );
        }

        items[id] = new_name;
    }
    ///////////////////////////////////
    /**
     * replace underlying data
     */
    public final void set( final int[] new_items )
    {
        items = new_items;
    }
    ////////////////////////////////////
    //merge together using larger as new value
    public final void keep_larger( final int master, final int child )
    {
        if( items[master] < items[child] ) {
            items[master] = items[child];
        }
    }
    ////////////////////////////////////
    //merge to keep smaller
    public final void keep_smaller( final int master, final int child )
    {
        if( items[master] > items[child] ) {
            items[master] = items[child];
        }
    }
    ///////////////////////////////////
    /**
     * clear the array
     */
    public final void clear()
    {
        checkPoint = -1;
        items = null;
        //holds the data
        items = new int[max_size];


        if(current_item>0){
            for(int i=0;i<current_item;i++) {
                items[i]=0;
            }
        }else{
            for(int i=0;i<max_size;i++) {
                items[i]=0;
            }
        }

        current_item = 0;
    }
    ///////////////////////////////////
    /**
     * return the size+1 as in last item (so an array of 0 values is 1) if added
     * If using set, use checkCapacity
     */
    public final synchronized int size()
    {
        return current_item+1;
    }

    /**
     * return the sizeof array
     */
    public final synchronized int getCapacity()
    {
        return items.length;
    }
    ///////////////////////////////////
    /**
     * remove element at
     */
    public final void removeElementAt( final int id )
    {
        if( id >= 0){
            //copy all items back one to over-write
            System.arraycopy(items, id + 1, items, id, current_item - 1 - id);

            //flush last item
            items[current_item - 1] = 0;
        }else {
            items[0] = 0;
        }

        //reduce counter
        current_item--;
    }

    /**
     * delete element at
     */
    public final synchronized void deleteElementWithValue( final int id )
    {
        final int currentSize=items.length;
        final int[] newItems=new int[currentSize-1];
        int counter=0;

        //copy all items back except item to delete
        for (final int item : items) {
            if (item != id) {
                newItems[counter] = item;
                counter++;
            }
        }

        //reassign
        items = newItems;

        //reduce counter
        current_item--;
    }

    @Override
    public String toString(){

        final StringBuilder returnString=new StringBuilder("{");

        //copy all items back except item to delete
        for (final int item : items) {
            returnString.append(' ').append(item);
        }

        returnString.append("} ").append(current_item);
        
        return returnString.toString();
    }

    ////////////////////////////////////
    /**
     * see if value present
     */
    public final boolean contains( final int value )
    {
        boolean flag = false;
        for( int i = 0;i < current_item;i++ )
        {
            if( items[i] == value )
            {
                i = current_item + 1;
                flag = true;
            }
        }
        return flag;
    }

    /////////////////////////////////////
    /**
     * pull item from top as in LIFO stack
     */
    public final int pull()
    {

        if(current_item>0) {
            current_item--;
        }

        return ( items[current_item] );
    }

    /////////////////////////////////////
    /**
     * put item at top as in LIFO stack
     */
    public final void push( final int value )
    {

        checkSize( current_item );
        items[current_item] = value;

        current_item++;
        checkSize( current_item );
    }

    ///////////////////////////////////
    /**
     * add an item
     */
    public final void addElement( final int value )
    {
        checkSize( current_item );
        items[current_item] = value;
        current_item++;
        checkSize( current_item );
    }

    ////////////////////////////////////
    //merge together using larger as new value
    public final void add_together( final int master, final int child )
    {
        items[master] += items[child];
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
            final int[] temp = items;
            items = new int[max_size];

            System.arraycopy( temp, 0, items, 0, old_size );

            increment_size=incrementSize(increment_size);

//			if( increment_size <= i ){
//			    if(increment_size<2500)
//			       increment_size=increment_size*4;
//			    else if(increment_size<10000)
//			       increment_size=increment_size*2;
//		        else
//		            increment_size=increment_size+2000;
//			//max_size = i +increment_size+ 2;
//			}
        }

    }


    /**
     * recycle the array by just resetting the pointer
     */
    public final void reuse()
    {
        current_item = 0;
    }

    public void trim(){

        final int[] newItems = new int[current_item];

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
}
