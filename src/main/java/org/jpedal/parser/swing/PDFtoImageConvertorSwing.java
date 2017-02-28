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
 * PDFtoImageConvertorSwing.java
 * ---------------
 */
package org.jpedal.parser.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.jpedal.objects.PdfPageData;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.jpedal.color.ColorSpaces;
import org.jpedal.exception.PdfException;
import org.jpedal.render.*;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.*;

public class PDFtoImageConvertorSwing extends PDFtoImageConvertor{


     public PDFtoImageConvertorSwing(final float multiplyer, final DecoderOptions options) {
        
        super(multiplyer,options);
    }

    @Override
    public DynamicVectorRenderer getDisplay(final int pageIndex, final ObjectStore localStore) {
        return imageDisplay = new ImageDisplay(pageIndex,true, 5000, localStore);
        
    }

    public static AffineTransform setPageParametersForImage(final float scaling, final int pageNumber, final PdfPageData pageData) {

        //create scaling factor to use
        final AffineTransform imageScaling = new AffineTransform();

        final int crw = pageData.getCropBoxWidth(pageNumber);
        final int crh = pageData.getCropBoxHeight(pageNumber);
        final int crx = pageData.getCropBoxX(pageNumber);
        final int cry = pageData.getCropBoxY(pageNumber);


        final int image_x_size =(int) ((crw)*scaling);
        final int image_y_size =(int) ((crh)*scaling);

        final int raw_rotation = pageData.getRotation(pageNumber);

        imageScaling.translate(-crx*scaling,+cry*scaling);

        if (raw_rotation == 270) {

            imageScaling.rotate(-Math.PI / 2.0, image_x_size/ 2, image_y_size / 2);

            final double x_change = (imageScaling.getTranslateX());
            final double y_change = (imageScaling.getTranslateY());
            imageScaling.translate((image_y_size - y_change), -x_change);

            
            if(cry<0){
                imageScaling.translate(2*cry*scaling,2*cry*scaling);
            }else{
                imageScaling.translate(2*cry*scaling,0);
            }
            imageScaling.translate(0,-scaling*(pageData.getCropBoxHeight(pageNumber)-pageData.getMediaBoxHeight(pageNumber)));
        } else if (raw_rotation == 180) {

            imageScaling.rotate(Math.PI, image_x_size / 2, image_y_size / 2);

        } else if (raw_rotation == 90) {

            imageScaling.rotate(Math.PI / 2.0,  image_x_size / 2,  image_y_size / 2);

            final double x_change =(imageScaling.getTranslateX());
            final double y_change = (imageScaling.getTranslateY());
            imageScaling.translate(-y_change, image_x_size - x_change);

        }

        imageScaling.translate(image_x_size, image_y_size);
        imageScaling.scale(1, -1);
        imageScaling.translate(-image_x_size, 0);

        imageScaling.scale(scaling,scaling);
        
        return imageScaling;
    }
    
    
    @Override
    public BufferedImage pageToImage(final boolean imageIsTransparent, final PdfStreamDecoder currentImageDecoder, final float scaling,
            final PdfObject pdfObject,final AcroRenderer formRenderer) throws PdfException {

        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        final Graphics graphics = image.getGraphics();

        final Graphics2D g2 = (Graphics2D) graphics;

        if (!imageIsTransparent) {
            g2.setColor(Color.white);
            g2.fillRect(0, 0, w, h);
        }

        /**
         * adjustment for upside down images
         */
        if(rotation==180){
            g2.translate(crx*2*multiplyer, -(cry*2*multiplyer));
        }

        /**
         * pass in values as needed for patterns
         */
        imageDisplay.setScalingValues(crx*multiplyer, (crh*multiplyer) + cry, multiplyer*scaling);

        g2.setRenderingHints(ColorSpaces.hints);
        g2.transform(imageScaling);

        if (rotated){

            if(rotation==90){//90
                
                if(multiplyer<1){
                    cry = (int)(imageScaling.getTranslateX() + cry);
                    crx = (int)(imageScaling.getTranslateY() + crx);

                }else{
                    cry = (int)((imageScaling.getTranslateX()/multiplyer) + cry);
                    crx = (int)((imageScaling.getTranslateY()/multiplyer) + crx);
                }
                
                
                crx /= scaling;
                cry /= scaling;
                
                g2.translate(-crx, -cry);

            }else{ //270
                if(cry<0) {
                    g2.translate(-(crx/scaling), (mediaH - crh + cry)/scaling);
                } else {
                    g2.translate(-(crx/scaling), (mediaH - crh - cry)/scaling);
                }
            }
        }
        
        /** decode and print in 1 go */
        currentImageDecoder.setObjectValue(ValueTypes.DirectRendering, g2);//(Graphics2D) graphics);
        imageDisplay.setG2(g2);
        currentImageDecoder.decodePageContent(pdfObject);

        g2.setClip(null);
        
        return image;
    }
}
