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
 * F.java
 * ---------------
 */
package org.jpedal.parser.shape;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javafx.scene.shape.Path;
import org.jpedal.color.ColorSpaces;
import org.jpedal.external.ShapeTracker;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfShape;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.*;
import org.jpedal.parser.image.PDFObjectToImage;
import org.jpedal.render.DynamicVectorRenderer;

public class F {
    
    
    public static void execute(final int tokenNumber, final boolean isStar, final int formLevel, final PdfShape currentDrawShape, final GraphicsState gs,
            final PdfObjectCache cache, final PdfObjectReader currentPdfFile, final DynamicVectorRenderer current, final ParserOptions parserOptions, final float multiplyer) {
        
        //ignore transparent white if group set
        if((formLevel>0 && cache.groupObj!=null && !cache.groupObj.getBoolean(PdfDictionary.K) && gs.getAlphaMax(GraphicsState.FILL)>0.84f && (gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceCMYK))
            
            && (gs.nonstrokeColorSpace.getColor().getRGB()==-1)) {
                return;
            }
        
        
        /**
         * if SMask with this color, we need to ignore
         *  (only case of white with BC of 1,1,1 at present for 11jun/12.pdf)
         */
        if(gs.SMask!=null && gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceCMYK){
            
            final float[] BC=gs.SMask.getFloatArray(PdfDictionary.BC);
            if(gs.nonstrokeColorSpace.getColor().getRGB()==-16777216 && BC!=null && BC[0]==1.0f) {
                return;
            }
        }
        
        /**
         * if SMask with this color, we need to ignore
         *  (only case of white with BC of 1,1,1 at present for 11jun/4.pdf)
         */
        if(gs.SMask!=null && gs.nonstrokeColorSpace.getID() == ColorSpaces.ICC){
            
            final float[] BC=gs.SMask.getFloatArray(PdfDictionary.BC);
            if(gs.nonstrokeColorSpace.getColor().getRGB()==-16777216 && BC!=null && BC[0]==0.0f) {
                return;
            }
        }
        
        //replace F with image if soft mask set (see randomHouse/9781609050917_DistX.pdf)
        if(gs.SMask!=null && gs.SMask.getDictionary(PdfDictionary.G)!=null && 
                (gs.nonstrokeColorSpace.getID()==ColorSpaces.DeviceRGB || gs.nonstrokeColorSpace.getID()==ColorSpaces.DeviceCMYK)){
            
            if(gs.nonstrokeColorSpace.getColor().getRGB()==-1 && gs.getOPM()==1.0f) {
                return;
            }
            
            final float[] BC=gs.SMask.getFloatArray(PdfDictionary.BC);
            
            if(gs.nonstrokeColorSpace.getColor().getRGB()==-16777216 && BC!=null && BC[0]==1.0f && BC[1]==1.0f && BC[2]==1.0f) {
                return;
            }
            
            if(gs.nonstrokeColorSpace.getColor().getRGB()==-16777216 && BC!=null && BC[0]==0.0f && BC[1]==0.0f && BC[2]==0.0f && gs.getOPM()==0.0f) {
                return;
            }
            
            createSMaskFill(gs,currentPdfFile, current, parserOptions,formLevel, multiplyer);

            return;
        }
        
        // (see randomHouse/9781609050917_DistX.pdf)
//if(gs.SMask!=null && (gs.SMask.getGeneralType(PdfDictionary.SMask)==PdfDictionary.None || gs.SMask.getGeneralType(PdfDictionary.SMask)==PdfDictionary.Multiply) && gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceRGB && gs.getOPM()==1.0f && gs.nonstrokeColorSpace.getColor().getRGB()==-16777216){
        
        if(gs.SMask!=null && gs.SMask.getGeneralType(PdfDictionary.SMask)!=PdfDictionary.None && gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceRGB && gs.getOPM()==1.0f && gs.nonstrokeColorSpace.getColor().getRGB()==-16777216){
            return;
        }
        
        if(parserOptions.isLayerVisible()){
            
            //set Winding rule
            if (isStar){
                currentDrawShape.setEVENODDWindingRule();
            }else {
                currentDrawShape.setNONZEROWindingRule();
            }
            
            currentDrawShape.closeShape();
            
            Shape currentShape=null;
            Path fxPath=null;
            
            /**
             * fx alternative
             */
            if(parserOptions.useJavaFX()){
                fxPath=currentDrawShape.getPath();
            }else{
                //generate swing shape and stroke and status. Type required to check if EvenOdd rule emulation required.
                currentShape =currentDrawShape.generateShapeFromPath(gs.CTM,gs.getLineWidth(), Cmd.F,current.getType());
            }
            
            boolean hasShape=currentShape!=null || fxPath!=null;
            
            //track for user if required
            final ShapeTracker customShapeTracker=parserOptions.getCustomShapeTraker();
            if(customShapeTracker!=null){
                
                if(isStar) {
                    customShapeTracker.addShape(tokenNumber, Cmd.Fstar, currentShape, gs.nonstrokeColorSpace.getColor(), gs.strokeColorSpace.getColor());
                } else {
                    customShapeTracker.addShape(tokenNumber, Cmd.F, currentShape, gs.nonstrokeColorSpace.getColor(), gs.strokeColorSpace.getColor());
                }
                
            }
            
            //do not paint white CMYK in overpaint mode
            if(hasShape && gs.getAlpha(GraphicsState.FILL)<1 &&
                    gs.nonstrokeColorSpace.getID()==ColorSpaces.DeviceN && gs.getOPM()==1.0f &&
                    gs.nonstrokeColorSpace.getColor().getRGB()==-16777216 ){
                
                //System.out.println(gs.getNonStrokeAlpha());
                //System.out.println(nonstrokeColorSpace.getAlternateColorSpace()+" "+nonstrokeColorSpace.getColorComponentCount()+" "+nonstrokeColorSpace.pantoneName);
                boolean ignoreTransparent =true; //assume true and disprove
                final float[] raw=gs.nonstrokeColorSpace.getRawValues();
                
                if(raw!=   null){
                    final int count=raw.length;
                    for(int ii=0;ii<count;ii++){
                        
                        //System.out.println(ii+"="+raw[ii]+" "+count);
                        
                        if(raw[ii]>0){
                            ignoreTransparent =false;
                            ii=count;
                        }
                    }
                }
                
                if(ignoreTransparent){
                    hasShape=false;
                }
            }
            
            //save for later
            if (hasShape && parserOptions.isRenderPage()){
                gs.setStrokeColor(gs.strokeColorSpace.getColor());
                gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());
                gs.setFillType(GraphicsState.FILL);
                
                if(parserOptions.useJavaFX()){
                    current.drawShape(fxPath,gs, Cmd.F);
                }else{
                    current.drawShape(currentShape,gs, Cmd.F);

                    if (current.isHTMLorSVG() && cache.groupObj==null) {
                        current.eliminateHiddenText(currentShape, gs, currentDrawShape.getSegmentCount(),false);
                    }
                }
            }
        }
        //always reset flag
        currentDrawShape.setClip(false);
        currentDrawShape.resetPath(); // flush all path ops stored
        
    }
    
    
    /**
     * make image from SMask and colour in with fill colour to simulate effect
     */
    private static void createSMaskFill(final GraphicsState gs, final PdfObjectReader currentPdfFile,
            final DynamicVectorRenderer current, final ParserOptions parserOptions, final int formLevel, final float multiplyer) {
        
        final PdfObject maskObj=gs.SMask.getDictionary(PdfDictionary.G);
        currentPdfFile.checkResolved(maskObj);
        final float[] BBox;//size
        BBox= maskObj.getFloatArray(PdfDictionary.BBox);
        
        /**get dimensions as an image*/
        int fx=(int)BBox[0];
        final int fy=(int)BBox[1];
        final int fw=(int)BBox[2];
        final int fh=(int)(BBox[3]);
        
        //check x,y offsets and factor in
        if(fx<0) {
            fx = 0;
        }
        
        /**
         * get the SMAsk
         */
        final BufferedImage smaskImage = PDFObjectToImage.getImageFromPdfObject(maskObj, fx, fw, fy, fh, currentPdfFile, parserOptions,formLevel, multiplyer,false,1f);
        
        final WritableRaster ras=smaskImage.getRaster();
        
        final int w=ras.getWidth();
        final int h=ras.getHeight();
        
        /**
         * and colour in
         */
        boolean transparent;
        final int[] values=new int[4];
        
        //get fill colour
        final int fillColor=gs.nonstrokeColorSpace.getColor().getRGB();
        values[0]=(byte) ((fillColor>>16) & 0xFF);
        values[1]=(byte) ((fillColor>>8) & 0xFF);
        values[2]=(byte) ((fillColor) & 0xFF);
        
        final int[] transparentPixel={0,0,0,0};
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                
                //get raw color data
                ras.getPixels(x,y,1,1,values);
                
                //see if transparent
               // transparent=(values[0]==0 && values[1]==0 && values[2]==0 && values[3]==255);
                transparent=(values[3]==255);
               
                //if it matched replace and move on
                if(transparent) {
                    ras.setPixels(x, y, 1, 1, transparentPixel);
                }else{
                    
                    final int[] newPixel=new int[4];
                    
                    newPixel[3]= (int) (255*0.75f);
                    newPixel[0]=values[0];
                    newPixel[1]=values[1];
                    newPixel[2]=values[2];
                    
                    ras.setPixels(x,y,1,1,newPixel);
                }
            }
        }
        
        /**
         * draw the shape as image
         */
        final GraphicsState gs1 =new GraphicsState();
        gs1.CTM=new float[][]{{smaskImage.getWidth(),0,1},{0,-smaskImage.getHeight(),1},{0,0,0}};
        
        gs1.x=fx;
        gs1.y=fy;
        
        //add as image
        gs1.CTM[2][0]= gs1.x;
        gs1.CTM[2][1]= gs1.y;
        current.drawImage(parserOptions.getPageNumber(),smaskImage, gs1,false, "F", -1);
        
        smaskImage.flush();
        
    }
    
}
