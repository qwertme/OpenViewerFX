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
 * XForm.java
 * ---------------
 */
package org.jpedal.parser.image;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Matrix;

/**
 *
 */
public class XForm {
    
    private static boolean hasEmptySMask(final GraphicsState gs){
        return gs.SMask!=null && gs.SMask.getGeneralType(PdfDictionary.SMask)==PdfDictionary.None;
    }
    
    public static PdfObject getSMask(final float[] BBox, final GraphicsState gs, final PdfObjectReader currentPdfFile) {
        PdfObject newSMask=null;

        //ignore if none
        if(hasEmptySMask(gs)){

            return null;
        }

        if(gs.SMask!=null && BBox!=null && BBox[0]>=0 && BBox[2]>0 ){ //see if SMask to cache to image & stop negative cases such as Milkshake StckBook Activity disX.pdf
            
            //if(gs.SMask.getParameterConstant(PdfDictionary.Type)!=PdfDictionary.Mask || gs.SMask.getFloatArray(PdfDictionary.BC)!=null){ //fix for waves file
                newSMask= gs.SMask.getDictionary(PdfDictionary.G);
                currentPdfFile.checkResolved(newSMask);
                
           // }
        }
        return newSMask;
    }
    
    private static final float[] matches={1f,0f,0f,1f,0f,0f};

    public static boolean isIdentity(final float[] matrix) {

        boolean isIdentity=true;// assume right and try to disprove

        if(matrix!=null){

            //see if it matches if not set flag and exit
            for(int ii=0;ii<6;ii++){
                if(matrix[ii]!=matches[ii]){
                    isIdentity=false;
                    break;
                }
            }
        }

        return isIdentity;
    }

    
    public static Area setClip(final Shape defaultClip, final float[] BBox, final GraphicsState gs, final DynamicVectorRenderer current) {
        final Area clip;
        float scalingW= gs.CTM[0][0];
        if(scalingW==0) {
            scalingW= gs.CTM[0][1];
        }
        	
        float scalingH=gs.CTM[1][1];
        if(scalingH==0) {
            scalingH=gs.CTM[1][0];
        }
        if(scalingH<0) {
            scalingH = -scalingH;
        }
        	
        int x,y,w,h;
       
        //case 17909 - make live to fix and see if can replace other version underneath
        if((gs.CTM[0][0]!=0 && gs.CTM[0][1]!=0 && gs.CTM[1][0]!=0 && gs.CTM[1][1]!=0) ||
                (gs.CTM[0][1]>0 && gs.CTM[1][0]<0)){
            
            //factor in scaling
            float[][] rect= {{BBox[2],0,0},{0,BBox[3],0},{BBox[0] ,BBox[1],1}};

            //Matrix.show(rect);
            //System.out.println("");

            //Matrix.show(gs.CTM);
            rect=Matrix.multiply(rect,gs.CTM);

            x=(int)(rect[2][0]);
            y=(int)(rect[2][1]);
            w=(int)(rect[0][0]+Math.abs(rect[1][0]));
            h=(int)(rect[1][1]+Math.abs(rect[0][1]));
            
            if(rect[1][0]<0) {
                x = (int) (x + rect[1][0]);
            }
            
            //System.out.println("shape="+x+" "+y+" "+w+" "+h);
            //Matrix.show(rect);
        
        }else if(gs.CTM[0][1]<0 && gs.CTM[1][0]>0){

            x=(int)(gs.CTM[2][0]+(BBox[1]*scalingW));
            y=(int)(gs.CTM[2][1]-(BBox[2]*scalingH));
            w=(int)((BBox[3]-BBox[1])*-scalingW);
            h=(int)Math.abs((BBox[2]-BBox[0])*-scalingH);

        }else{  //note we adjust size using CTM to factor in scaling
            
            if(1==1){
                int px,py,pw,ph; //proper rectangle coords;
                pw = (int) Math.abs(BBox[2]-BBox[0]);
                ph = (int) Math.abs(BBox[3]-BBox[1]);
                px = (int) Math.min(BBox[0],BBox[2]);
                py = (int) Math.min(BBox[1],BBox[3]);

                Rectangle properRect = new Rectangle(px, py, pw, ph);
                Area properArea = new Area(properRect);
                AffineTransform affine = new AffineTransform(gs.CTM[0][0], gs.CTM[0][1], gs.CTM[1][0],gs.CTM[1][1],gs.CTM[2][0],gs.CTM[2][1]);
                properArea = properArea.createTransformedArea(affine);
                properRect = properArea.getBounds();
                x = (int) properRect.getX();
                y = (int) properRect.getY();
                w = (int) properRect.getWidth();
                h = (int) properRect.getHeight();
            }else{ // old code keep temporarily
                x=(int)((gs.CTM[2][0]+(BBox[0]*scalingW)));
                y=(int)((gs.CTM[2][1]+(BBox[1]*scalingH)-1));
                w=(int)(1+(BBox[2]-BBox[0])*scalingW);
                h=(int)(2+(BBox[3]-BBox[1])*scalingH);

                if(gs.CTM[2][1]<0){
                    h= (int) (h-(gs.CTM[2][1]*scalingH));
                }
                if(gs.CTM[2][0]<0){
                    w= (int) (w-(gs.CTM[2][0]*scalingH));
                }

                //allow for inverted
                if(gs.CTM[1][1]<0){
                    y -= h;
                }
            } 
        }

        if(gs.getClippingShape()==null) {
            clip=null;
        }
        else {
            clip= (Area) gs.getClippingShape().clone();
        }
        
        final Area newClip=new Area(new Rectangle(x,y,w,h));
        
        gs.updateClip(new Area(newClip));
        current.drawClip(gs, defaultClip,false) ;

        return clip;
    }

}


