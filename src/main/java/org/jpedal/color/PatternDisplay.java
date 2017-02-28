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
 * PatternDisplay.java
 * ---------------
 */

package org.jpedal.color;

import java.awt.image.BufferedImage;

import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;
import org.jpedal.render.T3Display;
import org.jpedal.render.T3Renderer;

public class PatternDisplay extends T3Display implements T3Renderer
{
    
    
    private BufferedImage lastImg;
    
    private int imageCount;
    
    public PatternDisplay(final int i, final boolean b, final int j, final ObjectStore localStore)
    {
        super(i,b,j,localStore);
        
    }
    
    /* save image in array to draw */
    @Override
    public int drawImage(final int pageNumber, final BufferedImage image,
            final GraphicsState currentGraphicsState,
            final boolean alreadyCached, final String name, final int previousUse) {
        
        lastImg=image;
        
        imageCount++;
        
        return super.drawImage(pageNumber, image, currentGraphicsState, alreadyCached, name, previousUse);
    }
    
    
    @Override
    public BufferedImage getSingleImagePattern(){
        if(imageCount!=1) {
            return null;
        } else {
            return lastImg;
        }
    }
    
}
