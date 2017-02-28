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
 * ScalingFactory.java
 * ---------------
 */
package org.jpedal.utils;

import org.jpedal.objects.PdfPageData;

import java.awt.geom.AffineTransform;
import org.jpedal.objects.PageOrigins;

/**
 * workout Transformation to use on image
 */
public class ScalingFactory {

    public  static double[] getScalingForImage(final int pageNumber, final int rotation, final float scaling, final PdfPageData pageData) {

        final double mediaX = pageData.getMediaBoxX(pageNumber)*scaling;
        final double mediaY = pageData.getMediaBoxY(pageNumber)*scaling;
        //double mediaW = pageData.getMediaBoxWidth(pageNumber)*scaling;
        final double mediaH = pageData.getMediaBoxHeight(pageNumber)*scaling;

        final double crw = pageData.getCropBoxWidth(pageNumber)*scaling;
        final double crh = pageData.getCropBoxHeight(pageNumber)*scaling;
        final double crx = pageData.getCropBoxX(pageNumber)*scaling;
        final double cry = pageData.getCropBoxY(pageNumber)*scaling;

        //create scaling factor to use
        final AffineTransform displayScaling = new AffineTransform();
        final double[] displayScalingArray = new double[6];

        //** new x_size y_size declaration *
        final int x_size=(int) (crw+(crx-mediaX));
        final int y_size=(int) (crh+(cry-mediaY));
	
	/**
	 * XFA needs to be other way up so set page as inverted so option added
	 */
        if (rotation == 270) {

            displayScaling.rotate(-Math.PI / 2.0, x_size/ 2, y_size / 2);

	    if (pageData.getOrigin() == PageOrigins.BOTTOM_LEFT) {
		final double x_change = (displayScaling.getTranslateX());
		final double y_change = (displayScaling.getTranslateY());
		displayScaling.translate((y_size - y_change), -x_change);
		displayScaling.translate(0, y_size);
		displayScaling.scale(1, -1);
		displayScaling.translate(-(crx+mediaX), -(mediaH-crh-(cry-mediaY)));
	    }
	    
        } else if (rotation == 180) {

            displayScaling.rotate(Math.PI, x_size / 2, y_size / 2);
	    
	    if (pageData.getOrigin() == PageOrigins.BOTTOM_LEFT) {
		displayScaling.translate(-(crx+mediaX),y_size+(cry+mediaY)-(mediaH-crh-(cry-mediaY)));
		displayScaling.scale(1, -1);
	    }

        } else if (rotation == 90) {

            displayScaling.rotate(Math.PI / 2.0);
	    
	    if (pageData.getOrigin() == PageOrigins.BOTTOM_LEFT) {
		displayScaling.translate(0,(cry+mediaY)-(mediaH-crh-(cry-mediaY)));
		displayScaling.scale(1, -1);
	    }

        }else{
	    
	    if (pageData.getOrigin() ==PageOrigins.BOTTOM_LEFT) {
		displayScaling.translate(0, y_size);
		displayScaling.scale(1, -1);
		displayScaling.translate(0, -(mediaH - crh - (cry - mediaY)));
	    }
            
        }

        displayScaling.scale(scaling,scaling);

        displayScaling.getMatrix(displayScalingArray);
        
        return displayScalingArray;
    }
}
