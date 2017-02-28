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
 * PdfImageData.java
 * ---------------
 */
package org.jpedal.objects;

import org.jpedal.utils.repositories.Vector_Float;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_String;

/**
 * holds metadata on images extracted from the PDF file. <P>
 * Images are generally stored in the temp directory and only their meta
 * data held to reduce memory needs.</P>
 */
public class PdfImageData
{
	
	/**page id for image*/
	private final Vector_Int object_page_id = new Vector_Int( 100 );
	
	/**x co-ord for image*/
	private final Vector_Float x = new Vector_Float( 100 );
	
	/**y co-ord for image*/
	private final Vector_Float y = new Vector_Float( 100 );
	
	/**width for image*/
	private final Vector_Float w = new Vector_Float( 100 );
	
	/**height */
	private final Vector_Float h = new Vector_Float( 100 );
	
	/**image name*/
	private final Vector_String object_image_name = new Vector_String( 100 );
	
	/**count on images*/
	private int current_item;
	///////////////////////////////////////////////////////////////////////////
	/**
	 * <p>add an item (used internally as PDF page decoded).
     * @param image_name the name of the image
     * @param current_page_id the id value of the page
     * @param x1 x co-ordinates of the image
     * @param y1 y co-ordinates of the image
     * @param w1 width of the image
     * @param h1 height of the image
	 */
    public final void setImageInfo( String image_name, final int current_page_id, final float x1, final float y1, final float w1, final float h1)
	{
		//Remove slashes or troublesome characters from image name.  Same rules as ObjectStore.cleanString.
		image_name = org.jpedal.io.ObjectStore.removeIllegalFileNameCharacters(image_name);
		
		object_page_id.addElement( current_page_id );
		
		//name of image		
		object_image_name.addElement( image_name );
		
		//store shape co-ords
		x.addElement( x1 );
		y.addElement( y1 );
		h.addElement( h1 );
		w.addElement( w1 );
		
		current_item++;
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * get Y co-ord for image in pixels (user coords)
     * @param i pixel value of the image
     * @return Y co-ord for image in pixels 
	 */
    public final float getImageYCoord( final int i )
	{
		return y.elementAt( i );
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * get width for image in pixels
      * @param i pixel value of the image
     * @return width co-ord for image in pixels  
	 */
    public final float getImageWidth( final int i )
	{
		return w.elementAt( i );
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * get height for image in pixels
      * @param i pixel value of the image
     * @return height co-ord for image in pixels 
	 */
    public final float getImageHeight( final int i )
	{
		return h.elementAt( i );
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get object page id (ie sequential
	 * number of page)
     * @param i value for the page id
     * @return object page id
	 */
    public final int getImagePageID( final int i )
	{
		return object_page_id.elementAt( i );
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get image name created from raw data
     * @param i value of raw data
     * @return image name
	 */
    public final String getImageName( final int i )
	{
		return object_image_name.elementAt( i );
	}
	
	/////////////////////////////////////////////////////////////////////////
	/**
	 * clear object and reset (does not flush images from disk cache held by ObjectStore)
	 */
    public final void clearImageData()
	{
		object_image_name.clear();
		
		object_page_id.clear();
		x.clear();
		y.clear();
		w.clear();
		h.clear();
		current_item = 0;
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * get X co-ord for image in pixels (user coords)
     * @param i pixel value of the image
     * @return x co-ord for image in pixels 
	 */
    public final float getImageXCoord( final int i )
	{
		return x.elementAt( i );
	}
	////////////////////////////////////////////////////////////////////////
	/**
	 * <p>return the number of images. 
	 * <p>Note image1 is item 0, image2 is item 1 forget methods
     * @return return the number of images
	 */
    public final int getImageCount()
	{
		return current_item;
	}
}
