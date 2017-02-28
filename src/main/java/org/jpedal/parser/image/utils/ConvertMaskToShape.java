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
 * ConvertMaskToShape.java
 * ---------------
 */
package org.jpedal.parser.image.utils;

import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.Cmd;
import org.jpedal.parser.ParserOptions;
import org.jpedal.render.DynamicVectorRenderer;

import java.awt.geom.GeneralPath;

public class ConvertMaskToShape {

    public static void convert(final GraphicsState gs, final DynamicVectorRenderer current, final ParserOptions parserOptions) {

        float ix=gs.CTM[2][0];
        float iy=gs.CTM[2][1];

        float ih=gs.CTM[1][1];
        if(ih==0) {
            ih = gs.CTM[1][0];
        }
        if(ih<0){
            iy += ih;
            ih=-ih;
        }

        float iw=gs.CTM[0][0];
        if(iw==0) {
            iw = gs.CTM[0][1];
        }
        if(iw<0){
            ix += iw;
            iw=-iw;
        }

        //factor in GS rotation and swap w and h
        if(gs.CTM[0][0]==0 && gs.CTM[0][1]>0 && gs.CTM[1][0]!=0 && gs.CTM[1][1]==0){
            final float tmp=ih;
            ih=iw;
            iw=tmp;
        }

        //allow for odd values less than 1 and ensure minimum width
        if(iw<1) {
            iw = 1;
        }
        if(ih<1) {
            ih = 1;
        }

        int lwidth=-1;

        //for thin lines, use line width to ensure appears
        if(ih<3){

            lwidth=(int)ih;
            ih=1;
        }else if(iw<3){
            lwidth=(int)iw;
            iw=1;
        }

        final GeneralPath currentShape =new GeneralPath(GeneralPath.WIND_NON_ZERO);

        currentShape.moveTo(ix,iy);
        currentShape.lineTo(ix,iy+ih);
        currentShape.lineTo(ix+iw,iy+ih);
        currentShape.lineTo(ix+iw,iy);
        currentShape.closePath();

        //save for later
        if (parserOptions.isRenderPage() && currentShape!=null){

            final float lastLineWidth=gs.getLineWidth();

            if(lwidth>0) {
                gs.setLineWidth(lwidth);
            }

            gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());
            gs.setFillType(GraphicsState.FILL);

            current.drawShape(currentShape,gs, Cmd.F) ;

            //restore after draw
            if(lwidth>0) {
                gs.setLineWidth(lastLineWidth);
            }

        }
    }


}
