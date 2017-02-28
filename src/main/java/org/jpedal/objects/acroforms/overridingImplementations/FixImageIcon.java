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
 * FixImageIcon.java
 * ---------------
 */
package org.jpedal.objects.acroforms.overridingImplementations;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.SwingConstants;

import org.jpedal.objects.raw.FormObject;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.raw.PdfObject;

public class FixImageIcon extends CustomImageIcon implements Icon, SwingConstants {

    private static final long serialVersionUID = 8946195842453749725L;
    
    final PdfObject formObject;
    
    /** used to tell the paint method that we need to scale up the image for printing */
    private boolean currentlyPrinting;
    private int printMultiplier = 1;

    /** -1 means only one image,<br>0 means unselected,<br>1 means selected
     * <br>if there is only one image it is stored as the selected image
     */
    private int selected = -1;
    private static final int UNSELECTEDICON = 0;
    private static final int SELECTEDICON = 1;


    /** stores the root image for selected and unselected icons */
    private BufferedImage rootImageSelected, rootImageUnselected;

    /** stores the final image after any icon rotation */
    private BufferedImage imageSelected,imageUnselected;
    
    private PdfObject selObj;
    private PdfObject unSelObj;

	private PdfObjectReader currentpdffile;
	private int subtype;

	/** if 0 no change, 1 offset image, 2 invert image */
	private int offsetImage;

	/** constructor to be used for one image */
	public FixImageIcon(final PdfObject formObject, final BufferedImage img, final int iconRot) {
		super(iconRot);
		
        this.formObject=formObject;
    	
        if(img!=null) {
            imageSelected = img;
        } else
        	//if null store opaque image
        {
            imageSelected = FormObject.getOpaqueImage();
        }
        
        selected = -1;
    }

    /** new code to store the data to create the image when needed to the size needed
     * offset = if 0 no change, 1 offset image, 2 invert image
     */
    public FixImageIcon(final PdfObject formObject, final PdfObject imgObj, final int iconRot, final PdfObjectReader pdfObjectReader, final int type, final int offset){
    	super(iconRot);
    	
        this.formObject=formObject;
    	
    	selObj = imgObj;
    	selected = -1;

        currentpdffile = pdfObjectReader;
        subtype = type;
        offsetImage = offset;
        
//        if(selObj.getObjectRefAsString().equals("128 0 R") || selObj.getObjectRefAsString().equals("130 0 R"))
//			debug = true;
    }
    
    /** new code that stores the data to create the image at the defined size needed.
     * constructor for 2 images to be used for multipul pressed images, 
	 * <br>sel should be 1 if its currently selected, 0 if unselected.
	 * <br>offset = if 0 no change, 1 offset image, 2 invert image
     */
    public FixImageIcon(final PdfObject formObject, final PdfObject selObject, final PdfObject unselObject, final int iconRot, final int sel,
    		final PdfObjectReader pdfObjectReader, final int type, final int offset) {
    	super(iconRot);
		
        this.formObject=formObject;
    	selObj = selObject;
        unSelObj = unselObject;
        selected = sel;
        
        currentpdffile = pdfObjectReader;
        subtype = type;
        offsetImage = offset;
        
//        if(selObj.getObjectRefAsString().equals("128 0 R") || selObj.getObjectRefAsString().equals("130 0 R"))
//			debug = true;
    }

    
    /** returns the currently selected Image*/
    @Override
    public Image getImage(){
		final Image image;

    	checkAndCreateimage();

		switch(selected){
		case UNSELECTEDICON: 
			image = imageUnselected; 
			break;
		default: //or SELECTEDICON
			image = imageSelected; 
			break;
		}
		
		
		
		return image;
	}
    
    @Override
    public synchronized void paintIcon(final Component c, final Graphics g, final int x, final int y) {

    	final BufferedImage image = (BufferedImage) getImage();
		
		if (image == null) {
            return;
        }

		if (c.isEnabled()) {
			g.setColor(c.getBackground());
		} else {
			g.setColor(Color.gray);
		}

		final Graphics2D g2 = (Graphics2D) g;
		if (iconWidth > 0 && iconHeight > 0) {
			
			int drawWidth = iconWidth;
			int drawHeight = iconHeight;
			boolean rotateIcon = false;
			if((iconRotation==270 || iconRotation==90)){
				//mark to rotate the x and y positions later
				rotateIcon = true;
				
				//swap width and height so that the image is drawn in the corect orientation
				//without changing the raw width and height for the icon size
				drawWidth = iconHeight;
				drawHeight = iconWidth;
			}
			
			//only work out scaling if we have a dictionary of an image, as otherwise it could be a blank image (i.e. 1 x 1).
			if(currentpdffile!=null){

				//work out w,h which we want to draw inside our icon to maintain aspect ratio.
				float ws = (float)drawWidth / (float)image.getWidth(null);
				float hs = (float)drawHeight / (float)image.getHeight(null);
				
				//check if dimensions are correct and alter if not
				final float diff = ws-hs;
				final int diffInt = (int)diff;
				if(diffInt!=0){
					//reserve the current rotation marker as its wrong
					rotateIcon = !rotateIcon;
					
					final int tmp = drawWidth;
					drawWidth = drawHeight;
					drawHeight = tmp;
					
					ws = (float)drawWidth / (float)image.getWidth(null);
					hs = (float)drawHeight / (float)image.getHeight(null);
				}
				
				if(ws<hs){
					drawWidth = (int)(ws * image.getWidth(null));
					drawHeight = (int)(ws * image.getHeight(null));
				}else {
					drawWidth = (int)(hs * image.getWidth(null));
					drawHeight = (int)(hs * image.getHeight(null));
				}
			}

			//now work out the x,y position to keep the icon in the centre of the icon
			int posX=0,posY=0;
			if(currentpdffile!=null){
				if(rotateIcon){
					posX = (iconHeight-drawWidth)/2;
					posY = (iconWidth-drawHeight)/2;
				}else {
					posX = (iconWidth-drawWidth)/2;
					posY = (iconHeight-drawHeight)/2;
				}
			}

			final int finalRotation;
			if(displaySingle){
				finalRotation = validateRotationValue(pageRotate - iconRotation);
			}else {
				finalRotation = pageRotate;
			}
			
			/** with new decode at needed size code the resize (drawImage) may not be needed. */
			if (finalRotation ==270) {
				g2.rotate(-Math.PI / 2);
				g2.translate(-drawWidth, 0);
				g2.drawImage(image, -posX, posY, drawWidth, drawHeight, null);
			} else if (finalRotation == 90) {
				g2.rotate(Math.PI / 2);
				g2.translate(0, -drawHeight);
				g2.drawImage(image, posX, -posY, drawWidth, drawHeight, null);
			} else if (finalRotation == 180) {
				g2.rotate(Math.PI);
				g2.translate(-drawWidth, -drawHeight);
				g2.drawImage(image, -posX, -posY, drawWidth, drawHeight, null);
			}else {
				g2.drawImage(image, posX, posY, drawWidth, drawHeight, null);
			}
		} else {
            g2.drawImage(image, 0, 0, null);
        }

		g2.translate(-x, -y);
	}

    private void checkAndCreateimage() {
		//check if pdf object reader is defined, as we still use opaque images which do NOT need redecoding
		if(currentpdffile==null) {
            return;
        }
		
		/** NOTE the image code may need changing so that we store up to a certain size image
		 *  and not store large images, once the user has rescaled to a more normal size.
		 *  we could store the root width and height for the 100% size and use 200% as the 
		 *  highest image size to keep.
		 *  
		 *  if we do this the best way would be to have an object that we move the decode routine to, and
		 *  then when we read the 100% values from the image object we can store them in that size. 
		 */
		 
		//define normal sizes for normal use
		int newWidth = iconWidth,newHeight = iconHeight;
		
		//if printing define larger sizes for root image, but dont change icon height and width
		if(currentlyPrinting){
			newWidth = iconWidth * printMultiplier;
			newHeight = iconHeight * printMultiplier;
		}
		
		
		//decode images at needed size
		switch(selected){
		case UNSELECTEDICON: 
			if(rootImageUnselected==null
					|| newWidth > (rootImageUnselected.getWidth(null)) 
					|| newHeight > (rootImageUnselected.getHeight(null))
					|| newWidth < (rootImageUnselected.getWidth(null)/MAXSCALEFACTOR) 
					|| newHeight < (rootImageUnselected.getHeight(null)/MAXSCALEFACTOR)){
				rootImageUnselected = FormStream.decode(formObject,currentpdffile, unSelObj, subtype,newWidth,newHeight,offsetImage,1);

				
				imageUnselected = FormStream.rotate(rootImageUnselected,iconRotation);

			}//icon rotation is always defined in the constructor so we dont need to change it
			break;
		default: //or SELECTEDICON
			if(rootImageSelected==null 
					|| newWidth > (rootImageSelected.getWidth(null)) 
					|| newHeight > (rootImageSelected.getHeight(null))
					|| newWidth < (rootImageSelected.getWidth(null)/MAXSCALEFACTOR) 
					|| newHeight < (rootImageSelected.getHeight(null)/MAXSCALEFACTOR)){
				rootImageSelected = FormStream.decode(formObject,currentpdffile, selObj, subtype,newWidth,newHeight,offsetImage,1);

				
				imageSelected = FormStream.rotate(rootImageSelected,iconRotation);

			}//icon rotation is always defined in the constructor so we dont need to change it
			break;
		}
	}

    /** if this imageicon was constructed for use with one image this will do nothing,
     * <br>otherwise it will set the selected image to be the selected image if the flag is true
     * or the unseleced image if the flag is false.
     */
	public void swapImage(final boolean selectedImage) {
		if(selected==-1) {
            return;
        }
		
		if(selectedImage) {
            selected = SELECTEDICON;
        } else {
            selected = UNSELECTEDICON;
        }
	}
	
	/**generates higher quality images */
	public void setPrinting(final boolean print, final int multiplier){
		
		currentlyPrinting = print;
		printMultiplier = multiplier;
		
        checkAndCreateimage();      
	}

}
