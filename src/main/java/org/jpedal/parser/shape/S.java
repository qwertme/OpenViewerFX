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
 * S.java
 * ---------------
 */
package org.jpedal.parser.shape;

import java.awt.*;
import java.awt.geom.Area;
import javafx.scene.shape.Path;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfShape;
import org.jpedal.parser.Cmd;
import org.jpedal.parser.ParserOptions;
import org.jpedal.render.DynamicVectorRenderer;

public class S {
    
    
    public static Shape execute(final boolean isLowerCase, final GraphicsState gs, final PdfShape currentDrawShape, final DynamicVectorRenderer current, final ParserOptions parserOptions) {
        
        final boolean useJavaFX=parserOptions.useJavaFX();
        final boolean renderPage=parserOptions.isRenderPage();
        
        Shape currentShape=null;
        if(parserOptions.isLayerVisible()){
            
            //close for s command
            if (isLowerCase) {
                currentDrawShape.closeShape();
            }
            
            Path fxPath=null;
            
            float realLineWidth=-1;
            /**
             * fx alternative
             */
            if(useJavaFX){
                fxPath=currentDrawShape.getPath();
            }else {
                currentShape = currentDrawShape.generateShapeFromPath(gs.CTM, gs.getLineWidth(), Cmd.S, current.getType());
                
                if (currentDrawShape.adjustLineWidth()) {
                    gs.setLineWidth(0.6f);//0.6f because the scaling will multiply by 1.527 (we want the final value < 1)
                }
                
                if (currentShape != null) {
                    
                    Rectangle bounds;//=currentShape.getBounds();
                    
                    // System.out.println("S bounds="+bounds+" "+" "+gs.CTM[0][0]+" "+gs.CTM[0][1]+" "+gs.CTM[1][0]+" "+gs.CTM[1][1]);
                    
                    //allow for tiny line with huge width to draw a line (see baseline_screens/14jan/Pages from FDB-B737-FRM_nowatermark.pdf)
                    if(1==2 && gs.CTM[0][0]<=1 && gs.CTM[1][1]<=1 && gs.getLineWidth()>30 && bounds.height==1 && (bounds.width==1 || bounds.width>4)){
                        
                        realLineWidth=gs.getLineWidth();
                        
                        final float scaledThickness=realLineWidth*gs.CTM[1][1];
                        
                        final Rectangle current_path = new Rectangle(bounds.x,(int)(bounds.y-(scaledThickness/2)),bounds.width,(int)(scaledThickness));
                        
                        currentShape=new Area(current_path);
                        
                        // System.out.println("use "+current_path+" "+bounds.width+" "+realLineWidth);
                        
                        gs.setLineWidth(1f);
                        
                    }
                }
            }
            
            boolean hasShape=currentShape!=null || fxPath!=null;
            
            if(hasShape){ //allow for the odd combination of crop with zero size
                final Area crop=gs.getClippingShape();
                
                if(crop!=null && (crop.getBounds().getWidth()==0 || crop.getBounds().getHeight()==0 )){
                    currentShape=null;
                    fxPath=null;
                    hasShape=false;
                }
            }
            
            if(hasShape){ //allow for the odd combination of f then S
                
                //fix forSwing. (not required in HTML/SVG
                //Alter to only check bounds <1 instead of <=1 for fedexLabelAM.pdf
                if(currentShape!=null && currentShape.getBounds().getWidth()<1 && !current.isHTMLorSVG()) {// && currentGraphicsState.getLineWidth()<=1.0f){
                    currentShape=currentShape.getBounds2D();
                }
                
                //save for later
                if (renderPage){
                    
                    gs.setStrokeColor( gs.strokeColorSpace.getColor());
                    gs.setNonstrokeColor( gs.nonstrokeColorSpace.getColor());
                    gs.setFillType(GraphicsState.STROKE);
                    
                    if(useJavaFX){
                        current.drawShape(fxPath,gs, Cmd.S);
                    }else{
                        current.drawShape(currentShape,gs, Cmd.S);
                    }
                    
                    if(realLineWidth!=-1){
                        gs.setLineWidth(realLineWidth);
                    }
                }
            }
            
            if(currentDrawShape.isClip()){
                
                if(useJavaFX) {
                    gs.updateClip(fxPath);
                } else{
                    gs.updateClip(new Area(currentShape));
                }
            }
        }
        
        //always reset flag
        currentDrawShape.setClip(false);
        currentDrawShape.resetPath(); // flush all path ops stored
        
        return currentShape;
    }
    
}
