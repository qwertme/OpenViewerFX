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
 * ImageType.java
 * ---------------
 */
package org.jpedal.render.output.io;

/**
 * The ImageType specifies what the image is used for in conversion.
 * For example, is it a background image, a thumbnail image, etc.
 *
 * It is possible that behavior may need to change between image types.
 * For example background images do not require transparency and can use JPG,
 * however SVG images do require transparency and must use PNG.
 */
public enum ImageType {
    /**
     * Used in image_* and svg_*_with_ie8fallback text modes.
     * Does not require transparency.
     */
    BACKGROUND,
    /**
     * Used in the IDRViewer, or if thumbnails are enabled in the options.
     * Does not require transparency.
     */
    THUMBNAIL,
    /**
     * Used in svg_* text modes. Images are used by the SVG file produced.
     * Requires transparency.
     */
    SVG,
    /**
     * Used for rasterising PDF forms (non XFA).
     * Requires transparency.
     */
    FORM,
    /**
     * Used in svg_* text modes. Images are used by the SVG file produced.
     * Requires transparency.
     */
    SHADE,
    /**
     * Used by *_with_ie8fallback text modes. Used to display transformed text.
     * Requires transparency.
     */
    IEOVERLAY
}
