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
 * PdfPageData.java
 * ---------------
 */
package org.jpedal.objects;

import java.io.Serializable;

import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Object;

/**
 * store data relating to page sizes set in PDF (MediaBox, CropBox, rotation)
 */
public class PdfPageData implements Serializable{

	//private boolean valuesSet=false;

    private int lastPage=-1;

    private int pagesRead=-1;

    private int pageCount=1; //number of pages
    
    private int maxPageCount = -1;

//    private float[] defaultMediaBox=null;

	/** any rotation on page (defined in degress) */
	private int rotation;

	/** max media string for page */
    private final Vector_Object mediaBoxes = new Vector_Object(500);
    private final Vector_Object cropBoxes = new Vector_Object(500);

    private Vector_Int rotations;

    /** current x and y read from page info */
	private float cropBoxX = -99999, cropBoxY = -1,
	cropBoxW = -1, cropBoxH = -1;

	/** current x and y read from page info */
	private float mediaBoxX=-1, mediaBoxY, mediaBoxW, mediaBoxH;

    /** whether the document has varying page sizes and rotation */
    private boolean hasMultipleSizes, hasMultipleSizesSet;

	/** string representation of crop box */
	private float scalingValue = 1f;

    private float[] mediaBox,cropBox;

    /** string representation of media box */
	//private int defaultrotation;
	//private float defaultcropBoxX,defaultcropBoxY,defaultcropBoxW,defaultcropBoxH;
	//private float defaultmediaBoxX,defaultmediaBoxY,defaultmediaBoxW,defaultmediaBoxH;
    
	/**where co-ords start so we can code for PDF and XFA*/
	private PageOrigins pageOrigin = PageOrigins.BOTTOM_LEFT;

	public PdfPageData(){}

	/**
	 * make sure a value set for crop and media box (used internally to trap 'odd' settings and insure setup correctly)
         * @param pageNumber Page to be displayed
	 */
	 public void checkSizeSet(final int pageNumber) {

              if(pageNumber>pageCount) {
                  pageCount = pageNumber;
              }
              
        //use default
//        if(mediaBox ==null)
//            mediaBox = defaultMediaBox;
//              System.out.println("CropBox "+pageNumber+" > "+cropBox);
        //value we keep
        if(cropBox!=null &&
                (cropBox[0]!=mediaBox[0] || cropBox[1]!=mediaBox[1] || cropBox[2]!=mediaBox[2] || cropBox[3]!=mediaBox[3])){

            mediaBoxes.setElementAt(mediaBox, pageNumber);

            if(cropBox[0]>=mediaBox[0] && cropBox[1]>=mediaBox[1] && (cropBox[2]-cropBox[0])<=(mediaBox[2]-mediaBox[0])  && (cropBox[3]-cropBox[1])<=(mediaBox[3]-mediaBox[1])) {
                cropBoxes.setElementAt(cropBox, pageNumber);
            }

        }else if(mediaBox!=null)// &&
//                (defaultMediaBox[0]!=mediaBox[0] || defaultMediaBox[1]!=mediaBox[1] || defaultMediaBox[2]!=mediaBox[2] || defaultMediaBox[3]!=mediaBox[3])) //if matches default don't save
        {
            mediaBoxes.setElementAt(mediaBox, pageNumber);
        }

        //track which pages actually read
        if(pagesRead<pageNumber) {
            pagesRead = pageNumber;
        }


        lastPage=-1;
        mediaBox=null;
        cropBox=null;
    }
	 
	 
	 /**
	  * @return height of mediaBox
          * @param pageNumber Page to be displayed
	  */
     public final int getMediaBoxHeight(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return (int)mediaBoxH;
	 }

	 /**
	  * @return mediaBox y value
          * @param pageNumber Page to be displayed
	  */
     public final int getMediaBoxY(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return (int)mediaBoxY;
	 }

	 /**
	  * @return mediaBox x value
          * @param pageNumber Page to be displayed
	  */
     public final int getMediaBoxX(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return (int)mediaBoxX;
	 }

	 /**
	  * set string with raw values and assign values to crop and media size
          * @param mediaBox row values of PDF page data
	  */
	 public void setMediaBox(final float[] mediaBox) {
		 
         this.mediaBox=mediaBox;
         cropBox=null;

//         if(defaultMediaBox==null)
//            defaultMediaBox=mediaBox;

     }

	 /**
	  * set crop with values and align with media box
          * @param cropBox crop values of PDF page data
	  */
	 public void setCropBox(final float[] cropBox) {
		 
		 if(cropBox!=null) {
             this.cropBox = cropBox;
         } else {
             this.cropBox = mediaBox;
         }
		 
         //If mediaBox is set and crop box leaves this area
         //we should limit the cropBox by the mediaBox
         final boolean testAlteredCrop = true;
         if(testAlteredCrop && (mediaBox!=null && !(mediaBox.length<4))){
        	 if(this.cropBox[0]<mediaBox[0]) {
                 this.cropBox[0] = mediaBox[0];
             }
        	 
        	 if(this.cropBox[1]<mediaBox[1]) {
                 this.cropBox[1] = mediaBox[1];
             }
        	 
        	 if(this.cropBox[2]>mediaBox[2]) {
                 this.cropBox[2] = mediaBox[2];
             }
        	 
        	 if(this.cropBox[3]>mediaBox[3]) {
                 this.cropBox[3] = mediaBox[3];
             }
         }
     }

	 public void setPageRotation(final int value, final int pageNumber) {

         int raw_rotation = value;

		 //convert negative
		 if (raw_rotation < 0) {
             raw_rotation = 360 + raw_rotation;
         }

         //only create if we need and set value
         if(raw_rotation !=0 || rotations!=null){
             if(rotations==null){
                if(pageNumber<2000) {
                    rotations = new Vector_Int(2000);
                } else {
                    rotations = new Vector_Int(pageNumber * 2);
                }
             }

             rotations.setElementAt(raw_rotation,pageNumber);

         }
	 }

	 /**
	  * @return width of media box
          * @param pageNumber Page to be displayed 
	  */
     public final int getMediaBoxWidth(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return (int)mediaBoxW;
	 }

	 /**
	  * @return mediaBox string found in PDF file
          * @param currentPage the new current page
	  */
	 public String getMediaValue(final int currentPage) {

         final StringBuilder returnValue=new StringBuilder();

         float[] mediaBox=null;
		 
		if(mediaBoxes !=null) {
            mediaBox = (float[]) mediaBoxes.elementAt(currentPage);
        }

         if(mediaBox!=null){

            for(int j=0;j<4;j++){
                returnValue.append(mediaBox[j]);
                returnValue.append(' ');
            }
        }
		
        return returnValue.toString();
	 }

	 /**
	  * @return cropBox string found in PDF file
          * @param currentPage the new current page
	  */
	 public String getCropValue(final int currentPage) {

		float[] cropBox=null;
		 
		//use default
		if(cropBoxes!=null) {
            cropBox = (float[]) cropBoxes.elementAt(currentPage);
        }
		
		if(cropBox==null) {
            cropBox = (float[]) mediaBoxes.elementAt(currentPage);
        }
		
//		if(cropBox==null)
//        	cropBox=defaultMediaBox;
        
        final StringBuilder returnValue=new StringBuilder();

        for(int j=0;j<4;j++){
        	returnValue.append(cropBox[j]);
        	returnValue.append(' ');
        }
		
        return returnValue.toString();
	 }
	 
	 /**
	  * @return Scaled x value for cropBox
          * @param pageNumber the page to be displayed 
	  */
	 public int getScaledCropBoxX(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return roundFloat(cropBoxX*scalingValue);
	 }

	 /**
	  * @return Scaled cropBox width
          * @param pageNumber the page to be displayed 
	  */
	 public int getScaledCropBoxWidth(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return roundFloat(cropBoxW*scalingValue);
	 }

	 /**
	  * @return Scaled y value for cropox
          * @param pageNumber the page to be displayed 
	  */
	 public int getScaledCropBoxY(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return roundFloat(cropBoxY*scalingValue);
	 }

	 /**
	  * @return Scaled cropBox height
          * @param pageNumber the page to be displayed 
	  */
	 public int getScaledCropBoxHeight(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return roundFloat(cropBoxH*scalingValue);
	 }
	 
	 
	 /**
	  * @return x value for cropBox
          * @param pageNumber the page to be displayed 
	  */
	 public int getCropBoxX(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return (int)cropBoxX;
	 }
	 
	 /**
	  * @return x value for cropBox
          * @param pageNumber the page to be displayed 
	  */
	 public float getCropBoxX2D(final int pageNumber) {
		 
		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return cropBoxX;
	 }

	 /**
	  * @return cropBox width
          * @param pageNumber the page to be displayed 
	  */
	 public int getCropBoxWidth(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return (int)cropBoxW;
	 }
	 
	 /**
	  * return cropBox width
          * @param pageNumber the page to be displayed 
	  */
	 public float getCropBoxWidth2D(final int pageNumber) {
		 
		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return cropBoxW;
	 }

	 /**
	  * @return y value for cropox
          * @param pageNumber the page to be displayed 
	  */
	 public int getCropBoxY(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return (int)cropBoxY;
	 }
	 
	 /**
	  * @return y value for cropox
          * @param pageNumber the page to be displayed 
	  */
	 public float getCropBoxY2D(final int pageNumber) {
		 
		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return cropBoxY;
	 }

	 /**
	  * @return cropBox height
          * @param pageNumber the page to be displayed 
	  */
	 public int getCropBoxHeight(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return (int)cropBoxH;
	 }
	 
	 /**
	  * @return cropBox height
          * @param pageNumber the page to be displayed 
	  */
	 public float getCropBoxHeight2D(final int pageNumber) {
		 
		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return cropBoxH;
	 }
	 
	 /**see if current figures generated for this page and setup if not*/
	 private synchronized void setSizeForPage(final int pageNumber) throws Exception{

		 if(pageNumber==lastPage) {
             return;
         }
		 
         if(pageNumber>pageCount) {
             pageCount = pageNumber;
         }

		 /**calculate values if first call for this page*/
		 if(pageNumber>pagesRead){

			 //set values if no value
			 mediaBoxX=0;
			 mediaBoxY=0;
			 mediaBoxW = 0;
			 mediaBoxH = 0;

			 //set values if no value
			 cropBoxX=0;
			 cropBoxY=0;
			 cropBoxW = 0;
			 cropBoxH = 0;

             lastPage=pageNumber;

		 }else if(pageNumber>0 && lastPage!=pageNumber && (maxPageCount==-1 || pageNumber<=maxPageCount)){

			 lastPage=pageNumber;

//			 boolean usingDefault=false;

			 final float[] cropBox=(float[])cropBoxes.elementAt(pageNumber);
			 final float[] mediaBox=(float[])mediaBoxes.elementAt(pageNumber);
//			 if(mediaBox==null && defaultMediaBox!=null){
//				 mediaBox=defaultMediaBox;
//				 usingDefault=true;
//			 }

			 //set rotation
			 if(rotations!=null) {
                 rotation = rotations.elementAt(pageNumber);
             }
			 //else
			//	  rotation=defaultrotation;

             while(rotation>=360) {
                 rotation -= 360;
             }

//			 if(valuesSet && usingDefault){
//
//				 cropBoxX=defaultcropBoxX;
//				 mediaBoxX=defaultmediaBoxX;
//				 cropBoxY=defaultcropBoxY;
//				 mediaBoxY=defaultmediaBoxY;
//				 cropBoxW=defaultcropBoxW;
//				 mediaBoxW=defaultmediaBoxW;
//				 cropBoxH=defaultcropBoxH;
//				 mediaBoxH=defaultmediaBoxH;
//
//			 }else{

				  /**
				   * set mediaBox, cropBox and default if none
				   */
				  
				  //set values if no value
				  mediaBoxX=0;
				  mediaBoxY=0;
				  mediaBoxW = 800;
				  mediaBoxH = 800;

				  if(mediaBox!=null){
					  mediaBoxX=mediaBox[0];
					  mediaBoxY=mediaBox[1];
					  mediaBoxW=mediaBox[2]-mediaBoxX;
					  mediaBoxH=mediaBox[3]-mediaBoxY;

                      if(mediaBoxY>0 && mediaBoxH==-mediaBoxY){
                          mediaBoxH = -mediaBoxH;
                          mediaBoxY=0;
                      }
                  }
				  
				  /**
				   * set crop
				   */
				  if(cropBox!=null){
					  
					  cropBoxX=cropBox[0];
					  cropBoxY=cropBox[1];
					  cropBoxW=cropBox[2];
					  cropBoxH=cropBox[3];

					  if(cropBoxX>cropBoxW){
						  final float temp = cropBoxX;
						  cropBoxX = cropBoxW;
						  cropBoxW = temp;
					  }
					  if(cropBoxY>cropBoxH){
						  final float temp = cropBoxY;
						  cropBoxY = cropBoxH;
						  cropBoxH = temp;
					  }

					  cropBoxW -= cropBoxX;
					  cropBoxH -= cropBoxY;

                      if(cropBoxY>0 && cropBoxH==-cropBoxY){
                          cropBoxH = -cropBoxH;
                          cropBoxY=0;
                      }

                  }else{
					  cropBoxX = mediaBoxX;
					  cropBoxY = mediaBoxY;
					  cropBoxW = mediaBoxW;
					  cropBoxH = mediaBoxH;
				  }
//			 }

             //fix for odd file with negative height
             if(cropBoxH<0){
                 cropBoxY += cropBoxH;
                 cropBoxH=-cropBoxH;
             }
             if(cropBoxW<0){
                 cropBoxX += cropBoxW;
                 cropBoxW=-cropBoxW;
             }

//			if(usingDefault && !valuesSet){
//
//				 defaultrotation=rotation;
//				 defaultcropBoxX=cropBoxX;
//				 defaultmediaBoxX=mediaBoxX;
//				 defaultcropBoxY=cropBoxY;
//				 defaultmediaBoxY=mediaBoxY;
//				 defaultcropBoxW=cropBoxW;
//				 defaultmediaBoxW=mediaBoxW;
//				 defaultcropBoxH=cropBoxH;
//				 defaultmediaBoxH=mediaBoxH;
//
//				 valuesSet=true;
//			}
		 }else if(pageNumber<=0 || (maxPageCount!=-1 && pageNumber>maxPageCount)){
			throw new Exception("Attempted to find page outside of page range 1 - "+maxPageCount+"  Page number requested:"+pageNumber);
		 }
	 }

	 /**
	  * Get the scaling value currently being used
          * @return scalingValue
	  */
	 public float getScalingValue() {
		 return scalingValue;
	 }


	 /**
	  * Scaling value to apply to all values
          * @param scalingValue The new scaling value
	  */
	 public void setScalingValue(final float scalingValue) {
		 this.scalingValue = scalingValue;
	 }
	 
	 private static int roundFloat(final float origValue){
		 int roundedValue = (int)origValue;

         final boolean useCustomRounding = true;
         if(useCustomRounding){
			 final float frac = origValue - roundedValue;
			 if(frac>0.3) {
                 roundedValue += 1;
             }
		 }
		 return roundedValue;

	 }

    /**
     * get page count
     * @return number of pages
     */
    public final int getPageCount(){
        return pageCount;
    }
	 
	 /** @return rotation value (for outside class)
         * @param pageNumber the page to be displayed
         */
     public final int getRotation(final int pageNumber) {

		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

		 return rotation;
	 }

	/**
	  * @return Scaled height of mediaBox
          * @param pageNumber the page to be displayed
	  */
    public final int getScaledMediaBoxHeight(final int pageNumber) {
	
		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return roundFloat(mediaBoxH*scalingValue);
	 }

	/**
	  * @return Scaled width of media box
          * @param pageNumber the page to be displayed
	  */
    public final int getScaledMediaBoxWidth(final int pageNumber) {
	
		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return roundFloat(mediaBoxW*scalingValue);
	 }

	/**
	  * @return Scaled mediaBox x value
          * @param pageNumber the page to be displayed
	  */
    public final int getScaledMediaBoxX(final int pageNumber) {
	
		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return roundFloat(mediaBoxX*scalingValue);
	 }

	/**
	  * @return Scaled mediaBox y value
          * @param pageNumber the page to be displayed
	  */
    public final int getScaledMediaBoxY(final int pageNumber) {
	
		 //check values correctly set
		 try{
			 setSizeForPage(pageNumber);
		 }catch(final Exception e){
			//tell user and log
             if(LogWriter.isOutput()) {
                 LogWriter.writeLog("Exception: " + e.getMessage());
             }
             //
		 }

         return roundFloat(mediaBoxY*scalingValue);
	 }

    public boolean hasMultipleSizes() {
       //return if already calculated
        if (hasMultipleSizesSet) {
            return hasMultipleSizes;
        }

        //scan all pages and if we find one different, disable page turn
        final int pageCount= this.pageCount;
        final int pageW=getCropBoxWidth(1);
        final int pageH=getCropBoxHeight(1);
        final int pageR=getRotation(1);

        if(pageCount>1){
            for(int jj=2;jj<pageCount+1;jj++){

                if(pageW!=getCropBoxWidth(jj)|| pageH!=getCropBoxHeight(jj) ||
                        pageR!=getRotation(jj)){
                    jj=pageCount;
                    hasMultipleSizes = true;
                }
            }
        }
        hasMultipleSizesSet = true;
        return hasMultipleSizes;
    }

    /**
     * set page co-ord system to different location
     * @param newPageOrigin New location of page co-ord
     */
    public void setOrigin(final PageOrigins newPageOrigin) {
	this.pageOrigin = newPageOrigin;
    }
    
    /**
     * allow page to start at different locations (bottom left is default)
     * @return Start location of the page
     */
    public PageOrigins getOrigin() {
	//marks test code
	//pageOrigin= PageOrigins.TOP_LEFT;
	
	return pageOrigin;
    }
	 
    public void setPageCount(final int count){
    	maxPageCount = count;
    }
}
