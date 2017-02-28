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
 * JavaFXImageIcon.java
 * ---------------
 */

package org.jpedal.objects.acroforms.javafx;

import java.awt.image.BufferedImage;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Transform;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.raw.PdfObject;

/**
 *
 * @author Simon
 */
public class JavaFXImageIcon extends ImageView {
        
    PdfObject formObject;
    
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
    private Image rootImageSelected, rootImageUnselected;

    /** stores the final image after any icon rotation */
    private Image imageSelected,imageUnselected;
    
    private PdfObject selObj;
    private PdfObject unSelObj;

	private PdfObjectReader currentpdffile;
	private int subtype;

	/** if 0 no change, 1 offset image, 2 invert image */
	private int offsetImage;
	
	/** the maximum scaling factor difference between the rootImage and the current Form dimentions */
    protected static final float MAXSCALEFACTOR = 1.5f;
    
    protected static final int iconWidth = -1;
    protected static final int iconHeight = -1;
    
    private final SimpleDoubleProperty xScale;
    private final SimpleDoubleProperty yScale;
    
    
    protected int iconRotation;
    protected int iconOpp = 180;
	/** the page rotation required for this image */
    protected int pageRotate;
	//private int iconRot;
    
    /** used to tell paint method if we are displaying in single page mode, 
	 * if so we rotate here, if not rotate is handled elsewhere.
	 */
	protected boolean displaySingle;
    
    JavaFXImageIcon(final ButtonBase parentButton, final PdfObject formObject, final PdfObject imgObj, final int iconRot, final PdfObjectReader pdfObjectReader, final int type, final int offset){
    	//this.iconRot=iconRot;
        this.formObject=formObject;
    	
    	selObj = imgObj;
    	selected = -1;
        
        xScale = new SimpleDoubleProperty(1);
        yScale = new SimpleDoubleProperty(1);

        currentpdffile = pdfObjectReader;
        subtype = type;
        offsetImage = offset;

        setupButton(parentButton);
    }
    
    JavaFXImageIcon(final ButtonBase parentButton, final PdfObject formObject, final BufferedImage img, final int iconRot) {
		this(parentButton,formObject, img == null ? null : SwingFXUtils.toFXImage(img, null), iconRot);
    }
    
    JavaFXImageIcon(final ButtonBase parentButton, final PdfObject formObject, final Image img, final int iconRot) {
		iconRotation = iconRot;
        
        this.formObject=formObject;
    	
        if(img!=null) {
            imageSelected = img;
        } else{//if null store opaque image
            imageSelected = SwingFXUtils.toFXImage(FormObject.getOpaqueImage(), null);
        }
        
        xScale = new SimpleDoubleProperty(1);
        yScale = new SimpleDoubleProperty(1);
        selected = -1;
        
        setupButton(parentButton);
    }
    
    JavaFXImageIcon(final ButtonBase parentButton, final PdfObject formObject, final PdfObject selObject, final PdfObject unselObject, final int iconRot, final int sel,
    		final PdfObjectReader pdfObjectReader, final int type, final int offset) {
		//this.iconRot=iconRot;
        this.formObject=formObject;
    	selObj = selObject;
        unSelObj = unselObject;
        selected = sel;
        
        currentpdffile = pdfObjectReader;
        subtype = type;
        offsetImage = offset;

        xScale = new SimpleDoubleProperty(1);
        yScale = new SimpleDoubleProperty(1);
//        if(selObj.getObjectRefAsString().equals("128 0 R") || selObj.getObjectRefAsString().equals("130 0 R"))
//			debug = true;
        setupButton(parentButton);
    }
    
    private JavaFXImageIcon(){
        xScale = new SimpleDoubleProperty(1);
        yScale = new SimpleDoubleProperty(1);
    }
    
    private void setupButton(final ButtonBase parentButton){
                
        this.fitWidthProperty().bind(parentButton.prefWidthProperty());
        this.fitHeightProperty().bind(parentButton.prefHeightProperty());
        
        // This listener detects when the parent of the button (PdfDecoderFX) has it's transformation changed
        // - e.g. on a zoom in - and calls for the image to be redrawn
        final ListChangeListener<Transform> transListener = new ListChangeListener<Transform>() {
            @Override
            public void onChanged(final ListChangeListener.Change<? extends Transform> c) {
                c.next();
                if(c.wasAdded()){
                    final Transform newt = c.getAddedSubList().get(0);
                    xScale.set(Math.abs(newt.getMxx()));
                    yScale.set(Math.abs(newt.getMyy()));
                    
                    setSelectedImage();
                }
            }
        };
        
        final ChangeListener<Parent> listener = new ChangeListener<Parent>() {
            @Override
            public void changed(final ObservableValue<? extends Parent> observable, final Parent oldValue, final Parent newValue) {
                if(oldValue != null){
                    oldValue.getTransforms().removeListener(transListener);
                }
                if(newValue != null){
                    newValue.getTransforms().addListener(transListener);
                }
            }
        };
        
        parentButton.parentProperty().addListener(listener);
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
		int newWidth = (int) (this.getFitWidth()*xScale.get()),newHeight = (int) (this.getFitHeight()*yScale.get());
		
		//if printing define larger sizes for root image, but dont change icon height and width
		if(currentlyPrinting){
			newWidth = iconWidth * printMultiplier;
			newHeight = iconHeight * printMultiplier;
		}
		
		//decode images at needed size
		switch(selected){
		case UNSELECTEDICON: 
			if(rootImageUnselected==null
					|| newWidth > (rootImageUnselected.getWidth()) 
					|| newHeight > (rootImageUnselected.getHeight())
					|| newWidth < (rootImageUnselected.getWidth()/MAXSCALEFACTOR) 
					|| newHeight < (rootImageUnselected.getHeight()/MAXSCALEFACTOR)){
                // Get the images from the stream as buffered images and then convert them to FX Images
				final BufferedImage rootImageUnselectedAWT = FormStream.decode(formObject,currentpdffile, unSelObj, subtype,newWidth,newHeight,offsetImage,1);
				final BufferedImage imageUnselectedAWT = FormStream.rotate(rootImageUnselectedAWT,iconRotation);
                
                rootImageUnselected = SwingFXUtils.toFXImage(rootImageUnselectedAWT, null);
                imageUnselected = SwingFXUtils.toFXImage(imageUnselectedAWT, null);

            }
            break;
		default: //or SELECTEDICON
			if(rootImageSelected==null 
					|| newWidth > (rootImageSelected.getWidth()) 
					|| newHeight > (rootImageSelected.getHeight())
					|| newWidth < (rootImageSelected.getWidth()/MAXSCALEFACTOR) 
					|| newHeight < (rootImageSelected.getHeight()/MAXSCALEFACTOR)){
                
                // Get the images from the stream as buffered images and then convert them to FX Images
				final BufferedImage rootImageSelectedAWT = FormStream.decode(formObject,currentpdffile, selObj, subtype,newWidth,newHeight,offsetImage,1);
				final BufferedImage imageSelectedAWT = FormStream.rotate(rootImageSelectedAWT,iconRotation);
                
                if(rootImageSelectedAWT!=null){
                    rootImageSelected = SwingFXUtils.toFXImage(rootImageSelectedAWT, null);
                }
                
                if(imageSelectedAWT!=null){
                    imageSelected = SwingFXUtils.toFXImage(imageSelectedAWT, null);
                }
			}//icon rotation is always defined in the constructor so we dont need to change it
		}
	}
    
    public void swapImage(final boolean selectedImage) {
		if(selected==-1) {
            return;
        }
		
		if(selectedImage) {
            selected = SELECTEDICON;
        } else {
            selected = UNSELECTEDICON;
        }
        
        setSelectedImage();
	}
    
    /**generates higher quality images */
	public void setPrinting(final boolean print, final int multiplier){
		
		currentlyPrinting = print;
		printMultiplier = multiplier;
		
        checkAndCreateimage();      
	}
    
    private void setSelectedImage(){
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
        
        setImage(image);
        
	}
    public Image getSelectedImage(){
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
}
