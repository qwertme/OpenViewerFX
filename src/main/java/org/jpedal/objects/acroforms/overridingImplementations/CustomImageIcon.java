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
 * CustomImageIcon.java
 * ---------------
 */

package org.jpedal.objects.acroforms.overridingImplementations;

import java.awt.Image;

import javax.swing.ImageIcon;

public class CustomImageIcon extends ImageIcon {
	private static final long serialVersionUID = 5003778613900628453L;
	
	/** the maximum scaling factor difference between the rootImage and the current Form dimentions */
    protected static float MAXSCALEFACTOR = 1.5f;
    
    protected int iconWidth = -1;
    protected int iconHeight = -1;
    
    protected final int iconRotation;
    protected int iconOpp = 180;
	/** the page rotation required for this image */
    protected int pageRotate;
	
    /** used to tell praint method if we are displaying in single page mode, 
	 * if so we rotate here, if not rotate is handled elsewhere.
	 */
	protected boolean displaySingle;
	
	/** sets the scaling factor that the image has to change by before the root images are redraw to the current sizes.
	 * i.e if scaling factor is 1.5 start with 50% image, it wont redraw the image until its abot 75% or below 33%
	 * where as scaling factor of 1, means it will always redraw the image to the size required.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public static void setMaxScaleFactor(final float scaleFactor){
		MAXSCALEFACTOR = scaleFactor;
	}
	
	public CustomImageIcon(final int iconRot){
		iconRotation = iconRot;
    	iconOpp = iconRotation - 180;
		if(iconOpp<0) {
            iconOpp += 360;
        }
	}
	
	public void setAttributes(final int newWidth, final int newHeight, final int pageRotation, final boolean displaySing){
    	//recalculate rotationVal
    	final int finalRotation = validateRotationValue(pageRotation - iconRotation);
		
		pageRotate = pageRotation;
		
		if(finalRotation==iconRotation || finalRotation==iconOpp){
			iconWidth = newWidth;
			iconHeight = newHeight;
        }else {//the final rotation is out by 90 relative to the icon rotation
        	//turn the width and height round so that the bufferedimage is the correct orientation
        	//this is relative to the final rotation
        	iconWidth = newHeight;
        	iconHeight = newWidth;
        }
		
		displaySingle = displaySing;
    }
	
	protected static int validateRotationValue(int rotation) {
    	//make sure is between 0 and 360
		rotation %= 360;
    	//if negative make positive
		if(rotation<0) {
            rotation += 360;
        }
		
		return rotation;
	}
	
	@Override
    public int getIconHeight() {
    	if(iconHeight==-1){
    		final Image image = getImage();
        	
        	if(image==null) {
                return -1;
            } else {
                return image.getHeight(null);
            }
    	}else {
            return iconHeight;
        }
    }
	
	@Override
    public int getIconWidth() {
    	if(iconWidth==-1){
    		final Image image = getImage();
        	
        	if(image==null) {
                return -1;
            } else {
                return image.getWidth(null);
            }
    	}else {
            return iconWidth;
        }
    }
}
