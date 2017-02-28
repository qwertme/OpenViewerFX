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
 * T3ImageUtils.java
 * ---------------
 */
package org.jpedal.render;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.color.PdfPaint;

/**
 * Ant specific methods used to Draw type3 fonts
 */
class T3ImageUtils {
 
    
    public static BufferedImage handleType3Image(BufferedImage image,PdfPaint fillCol) {
        
        final int[] maskCol = new int[4];
        final int foreground = fillCol.getRGB();
        maskCol[0] = ((foreground >> 16) & 0xFF);
        maskCol[1] = ((foreground >> 8) & 0xFF);
        maskCol[2] = ((foreground) & 0xFF);
        maskCol[3] = 255;
        if (maskCol[0] == 0 && maskCol[1] == 0 && maskCol[2] == 0) {
            //System.out.println("black");
        } else {
            
            //hack for white text in printing (see Host account statement from open.pdf)
            if(image.getType()==10 && maskCol[0]>250 && maskCol[1]>250 && maskCol[2]>250){
                image=null;
            }else{
                
                final BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                
                final Raster src = image.getRaster();
                final WritableRaster dest = img.getRaster();
                final int[] values = new int[4];
                for (int yy = 0; yy < image.getHeight(); yy++) {
                    for (int xx = 0; xx < image.getWidth(); xx++) {
                        
                        //get raw color data
                        src.getPixel(xx, yy, values);
                        
                        //if not transparent, fill with color
                        if (values[3] > 2) {
                            dest.setPixel(xx, yy, maskCol);
                        }
                    }
                }
                image = img;
            }
        }
        return image;
    }

}
