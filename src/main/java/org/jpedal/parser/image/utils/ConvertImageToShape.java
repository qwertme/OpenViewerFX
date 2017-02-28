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
 * ConvertImageToShape.java
 * ---------------
 */
package org.jpedal.parser.image.utils;

import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.Cmd;
import org.jpedal.parser.ParserOptions;
import org.jpedal.render.DynamicVectorRenderer;

import java.awt.geom.GeneralPath;

public class ConvertImageToShape {


    public static void convert(final byte[] data, int h, final GraphicsState gs, final DynamicVectorRenderer current, final ParserOptions parserOptions) {


    /* Takes ac count of ef1603e.pdf.  A thin horizontal dotted line is not scaled properly, therefore its converted to a shape.
     * Condition is only executed if line is uniform along vertical axis
    **/

        final float ix = gs.CTM[2][0];
        final float iy = gs.CTM[2][1];
        float ih = gs.CTM[1][1];
        float iw = gs.CTM[0][0];

        //factor in GS rotation and swap w and h
        if (gs.CTM[0][0] == 0 && gs.CTM[0][1] > 0 && gs.CTM[1][0] != 0 && gs.CTM[1][1] == 0) {
            final float tmp = ih;
            ih = iw;
            iw = tmp;
        }

        final double byteWidth = iw / (data.length / h);
        final double bitWidth = byteWidth / 8;

        GeneralPath currentShape;

        for (int col = 0; col < data.length; col++) {
            int currentByte = (int) data[col] & 0xff;
            currentByte = ~currentByte & 0xff;

            int bitCount = 8;
            double endX = 0, startX;
            boolean draw = false;

            while (currentByte != 0 || draw) {
                bitCount--;

                if ((currentByte & 0x1) == 1) {
                    if (!draw) {
                        endX = ((bitCount + 0.5) * bitWidth) + (col * byteWidth);
                        draw = true;
                    }
                } else if (draw) {
                    draw = false;
                    startX = ((bitCount + 0.5) * bitWidth) + (col * byteWidth);

                    currentShape = new GeneralPath(GeneralPath.WIND_NON_ZERO);

                    currentShape.moveTo((float) (ix + startX), iy);
                    currentShape.lineTo((float) (ix + startX), iy + ih);
                    currentShape.lineTo((float) (ix + endX), iy + ih);
                    currentShape.lineTo((float) (ix + endX), iy);
                    currentShape.closePath();

                    //save for later
                    if (parserOptions.isRenderPage() && currentShape != null) {
                        gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());
                        gs.setFillType(GraphicsState.FILL);

                        current.drawShape(currentShape, gs, Cmd.F);

                    }
                }
                currentByte >>>= 1;
            }
        }
    }

}
