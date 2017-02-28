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
 * N.java
 * ---------------
 */
package org.jpedal.parser.shape;

import java.awt.Shape;
import java.awt.geom.Area;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.PdfShape;
import org.jpedal.parser.Cmd;
import org.jpedal.parser.ParserOptions;
import org.jpedal.render.DynamicVectorRenderer;

public class N {

    public static void execute(final PdfShape currentDrawShape, final GraphicsState gs, final int formLevel, final Shape defaultClip, final ParserOptions parserOptions, final DynamicVectorRenderer current, final PdfPageData pageData) {

        final boolean useJavaFX=parserOptions.useJavaFX();
        final boolean renderPage=parserOptions.isRenderPage();

        if (currentDrawShape.isClip()) {

            //create clipped shape
            currentDrawShape.closeShape();

            if(useJavaFX) {
                gs.updateClip(currentDrawShape.getPath());
            }else{
                gs.updateClip(new Area(currentDrawShape.generateShapeFromPath(gs.CTM, 0, Cmd.n, current.getType())));
            }
            
            if(formLevel==0){
                final int pageNum=parserOptions.getPageNumber();
                gs.checkWholePageClip(pageData.getMediaBoxHeight(pageNum)+pageData.getMediaBoxY(pageNum));
            }

            //always reset flag
            currentDrawShape.setClip(false);

            //save for later
            if (renderPage){
                current.drawClip(gs,defaultClip,false) ;
            }
        }

        currentDrawShape.resetPath(); // flush all path ops stored

    }   
}
