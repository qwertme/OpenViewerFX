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
 * FormUtils.java
 * ---------------
 */
package org.jpedal.objects.acroforms.utils;

import org.jpedal.objects.raw.FormObject;
import java.awt.*;

/**
 * general purpose functions used in forms
 */
public class FormUtils {

    /**
     * sorts the integer array into the right order to read the
     * component array in size order largest first
     * @param allFields
     */
    public static FormObject[] sortGroupLargestFirst(final FormObject[] comps) {
	
	return sortCompsDesending(comps);
    }
    
    /**
     * sorts as a tree like structure in array representation,
     * the integer array in descending size order comparing the component size,
     */
    private static FormObject[] sortCompsDesending(final FormObject[] array) {
	//reference
	//Sorts.quicksort(new int[1],new int[1]);
	
	/** copy so we don't sort original */
	final int items = array.length;
	
	//pointer to left side of unsorted array
	int left = items / 2;
	//pointer to right side of unsorted array
	int right = items - 1;
	
	//sift through array into a heap
	while (left > 0) {
	    
	    left -= 1;
	    
	    //go through tree starting with leaves and going up
	    siftCompsDesending(array, left, right);
	}
	
	//rearrange heap into a sorted array
	while (right > 0) {
	    
	    //assert: largest unsorted value is at a[0]
	    //move largest item to right end
	    final FormObject tempA = array[0];
	    array[0] = array[right];
	    array[right] = tempA;
	    //assert: a[right..] is sorted
	    
	    //right is largest and sorted decrement it
	    right -= 1;
	    
	    //get largest value in the tree to the leftMost position
	    siftCompsDesending(array, left, right);
	}
	//assert: right==0, therefore a[0..] is all sorted
	
	return array;
    }
    
    /**
     * see sortCompsDesending(Component[])
     * This Is Called from That Method ONLY
     */
    private static void siftCompsDesending(final FormObject[] array, final int left, final int right) {
	int currentLeft;
	final FormObject primaryTMP;
	int childL;
	
	//assign left to local
	currentLeft = left;
	//temp store of left item
	primaryTMP = array[currentLeft];
	
	//Left child node of currentLeft
	childL = 2 * left + 1;
	
	//Find a[left]'s larger child
	if ((childL < right) && shouldSwapControlDesending(array[childL], array[childL + 1])) {
	    childL += 1;
	}
	//assert: a[childL] is larger child
	
	//sift temp to be in correct place in highest on leftMost and arranged as tree
	while ((childL <= right) && shouldSwapControlDesending(primaryTMP, array[childL])) {
	    //assign highest item to leftmost position
	    array[currentLeft] = array[childL];
	    currentLeft = childL;
	    childL = 2 * childL + 1;
	    
	    //pick highest child
	    if ((childL < right) && shouldSwapControlDesending(array[childL], array[childL + 1])) {
		childL += 1;
	    }
	}
	//put temp in the correct place in the sub-heap
	array[currentLeft] = primaryTMP;
	//assert: a[left] is the root a sub-heap.
    }
    
    /**
     * the control of the order in the sortCompsDesending(Component[]) method
     */
    private static boolean shouldSwapControlDesending(final FormObject arg1, final FormObject arg2) {
	if(arg1==null){
	    return arg2 != null;
	}else {
	    if(arg2==null) {
            return false;
        } else{
		final Rectangle first = arg1.getBoundingRectangle();
		final Rectangle second = arg2.getBoundingRectangle();
		
		/**
		 * sorts by area, same as acrobat
		 * return (first.width*first.height)>(second.width*second.height);
		 */
		return (first.width * first.height) < (second.width * second.height);
	    }
	}
    }
}
