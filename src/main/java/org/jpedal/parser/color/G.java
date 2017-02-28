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
 * G.java
 * ---------------
 */
package org.jpedal.parser.color;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.DeviceGrayColorSpace;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.CommandParser;
import org.jpedal.parser.PdfObjectCache;

/**
 *
 */
public class G {
    
    public static void execute(final boolean isLowerCase, final GraphicsState gs, final CommandParser parser, final PdfObjectCache cache) {

        final boolean isStroke=!isLowerCase;
        final float[] operand= parser.getValuesAsFloat();
        final int operandCount=operand.length;

        //set colour and colorspace
        if(isStroke){
            if (gs.strokeColorSpace.getID() != ColorSpaces.DeviceGray) {
                gs.strokeColorSpace = new DeviceGrayColorSpace();
            }

            gs.strokeColorSpace.setColor(operand,operandCount);

            //track colrspace use
            cache.put(PdfObjectCache.ColorspacesUsed, gs.strokeColorSpace.getID(), "x");

        }else{
            if (gs.nonstrokeColorSpace.getID() != ColorSpaces.DeviceGray) {
                gs.nonstrokeColorSpace = new DeviceGrayColorSpace();
            }

            gs.nonstrokeColorSpace.setColor(operand,operandCount);

            //track colorspace use
            cache.put(PdfObjectCache.ColorspacesUsed, gs.nonstrokeColorSpace.getID(), "x");

        }
    }

}


