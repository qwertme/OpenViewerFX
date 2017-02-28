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
 * ExampleColorHandler.java
 * ---------------
 */
package org.jpedal.examples.handlers;

import org.jpedal.color.PdfPaint;
import org.jpedal.external.ColorHandler;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ExampleColorHandler implements ColorHandler {

    /**allow user to alter colour (ie to convert to bw)*/
    @Override
    public void setPaint(final Graphics2D g2, final PdfPaint col, final int pageNumber, final boolean isPrinting) {

        //if(isPrinting)
        //System.out.println("page="+pageNumber);

        //example here converts to bw for printing
        if(isPrinting){ //only on printout


            final int rgb=col.getRGB();

            //black and white conversion
            if(rgb>-16777216/2) //less than 50% is white
            {
                g2.setPaint(Color.WHITE);
            } else {
                g2.setPaint(Color.BLACK);
            }

            //grayscale conversion

            //get the value
            //float[] val=new float[3];
            //val[0]=((rgb>>16) & 255)/255f;
            //val[1]=((rgb>>8) & 255)/255f;
            //val[2]=(rgb & 255)/255f;

            //to gray
            //ColorSpace cs=ColorSpace.getInstance(ColorSpace.CS_GRAY);
            //float[] grayVal=cs.fromRGB(val);

            //Color colGray= new Color(cs,grayVal,1f);
            //g2.setPaint(colGray);



        }else {
            g2.setPaint(col);
        }
    }

    /**allow user to alter colour (ie to convert to bw)*/
    @Override
    public BufferedImage processImage(final BufferedImage image, final int pageNumber, final boolean isPrinting) {

        BufferedImage newImage=null;

        if(isPrinting){ //only on printout

            //black and white conversion
            newImage=new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            final Graphics2D newG2bw=newImage.createGraphics();
            newG2bw.setPaint(Color.WHITE);
            newG2bw.fillRect(0,0,image.getWidth(), image.getHeight());
            newG2bw.drawImage(image,0,0,null);

            //grayscale conversion
//            newImage=new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
//            Graphics2D newG2=newImage.createGraphics();
//            newG2.setPaint(Color.WHITE);
//            newG2.fillRect(0,0,image.getWidth(), image.getHeight());
//            newG2.drawImage(image,0,0,null);


        }
        return newImage;
    }
}
