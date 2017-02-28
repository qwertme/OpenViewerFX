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
 * ExampleImageHandler.java
 * ---------------
 */
package org.jpedal.examples.handlers;

import org.jpedal.external.ImageHandler;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.io.ObjectStore;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * example of adding in custom image code , replacing code with a blank image of correct size
 */

public class ExampleImageHandler implements ImageHandler {

    //tell JPedal if it ignores its own Image code or not
    @Override
    public boolean alwaysIgnoreGenericHandler() {
        return true;  //always use this code
    }

    /**tells JPedal not to scale image*/
    @Override
    public boolean imageHasBeenScaled() {
        return true;
    }

    @Override
    public boolean drawImageOnscreen(final BufferedImage image, final int optionsApplied, final AffineTransform upside_down, final String currentImageFile, final Graphics2D g2, final boolean renderDirect, final ObjectStore objectStore, final boolean isPrinting) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    //pass in raw data for image handling - if valid image returned it will be used.
    //if alwaysIgnoreGenericHandler() is true JPedal code always ignored. If false, JPedal code used if null
    @Override
    public BufferedImage processImageData(final GraphicsState gs, final PdfObject XObject) {

        final BufferedImage img;

        //see the raw data
        //System.out.println(values);

        /**
         * example implementation creates a blank image of correct size
         */

        /**
         * workout final size from CTM  (assumes no scaling or rotation)
         */
        int finalWidth=(int) gs.CTM[0][0];
        int finalHeight=(int) gs.CTM[1][1];

        /**allow for image upside down or right to left*/
        if(finalWidth<0) {
            finalWidth = -finalWidth;
        }
        if(finalHeight<0) {
            finalHeight = -finalHeight;
        }

        img =new BufferedImage(finalWidth,finalHeight, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2=(Graphics2D)img.getGraphics();

        final AffineTransform aff=new AffineTransform();
        aff.translate(0,-finalHeight);
        aff.scale(1,-1);
        g2.setTransform(aff);

        final String message="Image removed";

        final int fontSize=finalWidth/message.length();

        final Font font=new Font("serif",Font.PLAIN,fontSize);
        final Rectangle2D messageBounds=font.getStringBounds(message,0,message.length(),g2.getFontRenderContext());

        g2.setFont(font);
        g2.drawString(message,(int) ((finalWidth-messageBounds.getWidth())/2),-finalHeight-((finalHeight)/2));


        /***NOTE - IMAGE is expected to be UPSIDE DOWN!!!!!*/
        return img;
    }
}
