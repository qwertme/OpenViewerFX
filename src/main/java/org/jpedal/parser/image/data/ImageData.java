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
 * ImageData.java
 * ---------------
 */
package org.jpedal.parser.image.data;

import org.jpedal.io.PdfFilteredReader;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

public class ImageData {

    int pX,pY;
        
    int width, height, depth = 1;

    boolean imageMask;

    byte[] objectData;
    
    boolean isDCT,isJPX,isJBIG;
    private int numComponents;
    
    int mode;
    
    private boolean removed;
    
    public ImageData(PdfObject XObject,byte[] objectData) {

        this.objectData=objectData;

        width = XObject.getInt(PdfDictionary.Width);
        height = XObject.getInt(PdfDictionary.Height);

        int newDepth = XObject.getInt(PdfDictionary.BitsPerComponent);
        if (newDepth != PdfDictionary.Unknown) {
            depth = newDepth;
        }

        imageMask= XObject.getBoolean(PdfDictionary.ImageMask);
        
        

    }

    public ImageData(int mode) {
        this.mode=mode;
    }

    public int getMode() {
        return mode;
    }
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isImageMask() {
        return imageMask;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getObjectData() {
        return objectData;
    }

    public void setObjectData(byte[] objectData) {
        this.objectData = objectData;
    }

    public void setpX(int pX) {
        this.pX=pX;
    }
    
    public void setpY(int pY) {
        this.pY=pY;
    }
    
    public int getpX() {
        return pX;
    }
    
    public int getpY() {
        return pY;
    }
    
    public void swapValues() {
        int temp = pX;
        pX=pY;
        pY=temp;
    }

    public boolean isJPX() {
        return isJPX;
    }
    
    public boolean isDCT() {
        return isDCT;
    }
    
    public boolean isJBIG(){
        return isJBIG;
    }
    
    public PdfArrayIterator getFilter(PdfObject XObject) {
        
        PdfArrayIterator Filters = XObject.getMixedArray(PdfDictionary.Filter);
        
        //check not handled elsewhere
        int firstValue;
        if(Filters!=null && Filters.hasMoreTokens()){
            while(Filters.hasMoreTokens()){
                firstValue=Filters.getNextValueAsConstant(true);
                isDCT=firstValue==PdfFilteredReader.DCTDecode;
                isJPX=firstValue==PdfFilteredReader.JPXDecode;
                isJBIG=firstValue==PdfFilteredReader.JBIG2Decode;
            }
        }else {
            Filters = null;
        }
        
        return Filters;
    }

    public void setCompCount(int numComponents) {
        this.numComponents=numComponents;
    }
    
    public int getCompCount() {
        return numComponents;
    }

    /**
     * @return if image was removed
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * @param set if image removed as not visible
     */
    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
