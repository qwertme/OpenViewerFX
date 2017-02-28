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
 * CustomIO.java
 * ---------------
 */
package org.jpedal.render.output.io;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**abstract IO from HTML and SVG conversion*/
public interface CustomIO {

    void writeFileFromStream(InputStream is, String path);

    void writeFont(String path, byte[] rawFontData);

    void writePlainTextFile(String path, StringBuilder content);

    boolean isOutputOpen();

    void setCompressImages(boolean compressImages);

    void setupOutput(String path, boolean append, String encodingUsed) throws FileNotFoundException, UnsupportedEncodingException;
    
    void setupOutput(OutputStream stream, boolean append, String encodingUsed) throws FileNotFoundException, UnsupportedEncodingException;

    void flush();

    void writeString(String str);

    void writeImage(String rootDir, String path, BufferedImage image, ImageType imageType);

    ImageFileType getImageTypeUsed(ImageType imageType);

    void waitForImages();

}
