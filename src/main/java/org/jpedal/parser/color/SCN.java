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
 * SCN.java
 * ---------------
 */
package org.jpedal.parser.color;

import org.jpedal.color.ColorSpaces;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.CommandParser;
import org.jpedal.parser.PdfObjectCache;

/**
 *
 */
public class SCN {
   

    public static void execute(final boolean isLowerCase, final GraphicsState gs, final CommandParser parser, final PdfObjectCache cache) {

        float[] values;

        if(isLowerCase){

            if(gs.nonstrokeColorSpace.getID()==ColorSpaces.Pattern){
                final String[] vals=parser.getValuesAsString();
                gs.nonstrokeColorSpace.setColor(vals,vals.length);
            }else{
                values=parser.getValuesAsFloat();

                final int operandCount=values.length;
                final float[] tempValues=new float[operandCount];
                for(int ii=0;ii<operandCount;ii++) {
                    tempValues[operandCount - ii - 1] = values[ii];
                }
                values=tempValues;

                //System.out.println(nonstrokeColorSpace);
                gs.nonstrokeColorSpace.setColor(values,operandCount);
            }

            //track colrspace use
            cache.put(PdfObjectCache.ColorspacesUsed, gs.nonstrokeColorSpace.getID(),"x");

        }else{
            if(gs.strokeColorSpace.getID()==ColorSpaces.Pattern){
                final String[] vals=parser.getValuesAsString();
                gs.strokeColorSpace.setColor(vals,vals.length);
            }else{
                values=parser.getValuesAsFloat();

                final int operandCount=values.length;
                final float[] tempValues=new float[operandCount];
                for(int ii=0;ii<operandCount;ii++) {
                    tempValues[operandCount - ii - 1] = values[ii];
                }
                values=tempValues;

                gs.strokeColorSpace.setColor(values,operandCount);
            }

            //track colrspace use
            cache.put(PdfObjectCache.ColorspacesUsed, gs.strokeColorSpace.getID(),"x");

        }
    }
}


