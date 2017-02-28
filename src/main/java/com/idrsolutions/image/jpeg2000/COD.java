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
 * COD.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 *
 * 
 */
public class COD {
    
    /**
     *
     */
    public boolean hasPrecint;

    /**
     *
     */
    public boolean hasSOP;

    /**
     *
     */
    public boolean hasEPH;

    /**
     *
     */
    public int progressionOrder;

    /**
     *
     */
    public int nLayers;

    /**
     *
     */
    public int multiCompTransform;

    /**
     *
     */
    public int nDecompLevel;

    /**
     *
     */
    public int xcb;

    /**
     *
     */
    public int ycb;

    /**
     *
     */
    public int codeBlockStyle;

    /**
     *
     */
    public int transformation;

    /**
     *
     */
    public int[] precintSizes;
    
    /**
     *
     * @return
     */
    @Override
    public String toString() {
        
     StringBuilder str = new StringBuilder( "\n\tCoding Style: hasPrecincts: " + hasPrecint+", HasSOP: "+hasSOP+", hasEPH: "+hasEPH 
                + "\n\tProgression Order: " + progressionOrder
                + "\n\tNumber of Layers: " + nLayers
                + "\n\tMultiComponentTransform: " + multiCompTransform
                + "\n\tNumberOfDecompLevel: " + nDecompLevel
                + "\n\tCodeBlockWidth: " + xcb
                + "\n\tCodeBlockHeight: " + ycb
                + "\n\tCodeBlockStyle: "+codeBlockStyle
                +"\n\tTransformation: "+transformation);
                
        if(precintSizes!=null){
            str.append("\n\tPrecintSizes: ");
            for (int i = 0; i < precintSizes.length; i++) {
            str.append(' ').append(precintSizes[i]);
            }
        }
        
        return str.toString();
    }
    
}
