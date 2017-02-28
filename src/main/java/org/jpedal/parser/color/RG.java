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
 * RG.java
 * ---------------
 */
package org.jpedal.parser.color;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.DeviceRGBColorSpace;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.CommandParser;
import org.jpedal.parser.PdfObjectCache;

/**
 *
 */
public class RG {
    
    
    public static void execute(final boolean isLowerCase, final GraphicsState gs, final CommandParser parser, final PdfObjectCache cache) {

        //set flag to show which color (stroke/nonstroke)
        final boolean isStroke=!isLowerCase;

        float[] operand=parser.getValuesAsFloat();

        final int operandCount=operand.length;

        final float[] tempValues=new float[operandCount];
        for(int ii=0;ii<operandCount;ii++) {
            tempValues[operandCount - ii - 1] = operand[ii];
        }
        operand=tempValues;

        //set colour
        if(isStroke){
            if (gs.strokeColorSpace.getID() != ColorSpaces.DeviceRGB) {
                gs.strokeColorSpace = new DeviceRGBColorSpace();
            }

            gs.strokeColorSpace.setColor(operand,operandCount);

            //track colorspace use
            cache.put(PdfObjectCache.ColorspacesUsed, gs.strokeColorSpace.getID(),"x");

        }else{
            if (gs.nonstrokeColorSpace.getID() != ColorSpaces.DeviceRGB) {
                gs.nonstrokeColorSpace = new DeviceRGBColorSpace();
            }

            gs.nonstrokeColorSpace.setColor(operand,operandCount);

            //track colrspace use
            cache.put(PdfObjectCache.ColorspacesUsed, gs.nonstrokeColorSpace.getID(),"x");

        }
    }

   
}


