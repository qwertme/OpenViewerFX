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
 * FXClip.java
 * ---------------
 */

package org.jpedal.objects;

import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;

/**
 * JavaFX implementation of the PdfClip
 */
public class FXClip implements PdfClip {

    private Shape current_clipping_shape;
    
    @Override
    public boolean updateClip(final Object path) {
        
        // Check that the passed parameter is valid.
        // If this code causes trouble in future, it may be due to
        // null instanceof Class, which evaluates to false. null might be needed in resetting the clip
        if(!(path instanceof Shape)) {
            return false;
        }
        
        final Path fxPath = (Path)path;

        // Fill the path so that everything inside the path becomes the clipping area
        if(fxPath != null) {
            fxPath.setFill(Color.WHITE);
        }
        
        if( current_clipping_shape == null || fxPath==null){
            current_clipping_shape = fxPath;
        }else{
            current_clipping_shape = Shape.intersect(current_clipping_shape, fxPath);
        }
        
        return true;
    }
    
    @Override
    public Object getClippingShape() {
        return setupClippingShape(current_clipping_shape);
    }
    
    /**
     * A clip can be used multiple times, but can only be added to a scene once.
     * This method works around it by creating a new shape from the path elements.
     * 
     * @param clipping_shape The shape to be copied
     * @return a new Shape containing the clipping Path
     */
    private static Shape setupClippingShape(final Shape clipping_shape){
        if(clipping_shape == null) {
            return null;
        }
        
        // Cast is safe as all shapes are drawn as paths
        final Path clipPath = (Path)clipping_shape;
        final Path s = new Path(clipPath.getElements());
        s.setFillRule(clipPath.getFillRule());
       
        s.setFill(Color.WHITE);
       
        return s;
    }
    
    
}