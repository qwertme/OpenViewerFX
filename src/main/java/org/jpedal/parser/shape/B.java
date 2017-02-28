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
 * B.java
 * ---------------
 */
package org.jpedal.parser.shape;

import java.awt.Shape;
import java.awt.geom.Area;
import javafx.scene.shape.Path;
import org.jpedal.color.ColorSpaces;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfShape;
import org.jpedal.parser.Cmd;
import org.jpedal.parser.ParserOptions;
import org.jpedal.render.DynamicVectorRenderer;

public class B {
    
    public static Shape execute(final boolean isStar, final boolean isLowerCase, final GraphicsState gs, final int formLevel, final PdfShape currentDrawShape, final DynamicVectorRenderer current, final ParserOptions parserOptions) {

        final boolean useJavaFX=parserOptions.useJavaFX();
        final boolean renderPage=parserOptions.isRenderPage();

        Shape currentShape=null;
        
        if(parserOptions.isLayerVisible()){
            //set Winding rule
            if (isStar) {
                currentDrawShape.setEVENODDWindingRule();
            } else {
                currentDrawShape.setNONZEROWindingRule();
            }

            //close for s command
            if (isLowerCase) {
                currentDrawShape.closeShape();
            }

            
            Path fxPath=null;
            
            /**
             * fx alternative
             */
            if(useJavaFX){
                fxPath=currentDrawShape.getPath();
            }else{
                //generate swing shape and stroke and status. Type required to check if EvenOdd rule emulation required.
                currentShape =currentDrawShape.generateShapeFromPath(gs.CTM,gs.getLineWidth(),Cmd.B,current.getType());
            
                //hack which fixes blocky text on Customers3/demo_3.pdf in Swing
                if(currentShape!=null && currentShape.getBounds2D().getWidth()<1 && currentShape.getBounds2D().getHeight()<1){
                    currentDrawShape.resetPath();
                    return null;
                }
            }

            final boolean hasShape=currentShape!=null || fxPath!=null;
            

            //only curently implemented in Swing (fixes /PDFdata/test_data/baseline_screens/debug3/535B-X-test.pdf)
            if(!useJavaFX && !isLowerCase && formLevel > 2 &&  hasShape && currentDrawShape.isClosed() && gs.getClippingShape()!=null && gs.nonstrokeColorSpace.getID()== ColorSpaces.DeviceCMYK && gs.nonstrokeColorSpace.getColor().getRGB()==-1){

                final Area a=gs.getClippingShape();
                a.subtract(new Area(currentShape));
                currentShape=a;

            }

            //save for later
            if (renderPage && hasShape){

                gs.setStrokeColor(gs.strokeColorSpace.getColor());
                gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());

                if(gs.nonstrokeColorSpace.getColor().getRGB()==-16777216 && (gs.getAlpha(GraphicsState.STROKE)==0)){
                    gs.setFillType(GraphicsState.STROKE);
                }else {
                    gs.setFillType(GraphicsState.FILLSTROKE);
                }

                if(useJavaFX){
                    current.drawShape(fxPath,gs, Cmd.B);
                }else{
                    current.drawShape(currentShape,gs, Cmd.B);
                    if (current.isHTMLorSVG()) {
                        current.eliminateHiddenText(currentShape,gs,currentDrawShape.getSegmentCount(), false);
                    }
                }
            }
        }
        //always reset flag
        currentDrawShape.setClip(false);

        currentDrawShape.resetPath(); // flush all path ops stored

        return currentShape;
    }

}
