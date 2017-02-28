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
 * CalRGBColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import java.awt.image.DataBufferByte;
import java.awt.image.Raster;





/**
 * handle CalRGBColorSpace in PDF
 */
public class CalRGBColorSpace extends  GenericColorSpace{
    
    private static final long serialVersionUID = 4569336292751894930L;
    
    private int r,g,b;
    
//    private static final double[][] xyzrgb = {
//        {  3.240449, -1.537136, -0.498531 },
//        { -0.969265,  1.876011,  0.041556 },
//        {  0.055643, -0.204026,  1.057229 }};
    
    private static final double cs00 = 3.240449, cs01 = -1.537136, cs02 = -0.498531;
    private static final double cs10 = -0.969265, cs11 = 1.876011, cs12 = 0.041556;
    private static final double cs20 = 0.055643, cs21 = -0.204026, cs22 = 1.057229;
    
    /**cache for values to stop recalculation*/
    private float lastC=-255,lastI=-255,lastE=-255;
    
    public CalRGBColorSpace(final float[] whitepoint, final float[] matrix, final float[] gamma) {
        
        cs = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
        
        setCIEValues(whitepoint, null,matrix,gamma);
        setType(ColorSpaces.CalRGB);
        
    }
    
    /**
     * CalRGB to RGB
     */
    @Override
    public byte[] dataToRGBByteArray(final byte[] data, final int w, final int h, boolean arrayInverted){
        
        final int size = w * h;
        float cl,ca,cb;
        
        for (int i = 0; i < size * 3; i += 3) { //convert all values to rgb
            
            cl = data[i] & 255;
            ca = data[i + 1] & 255;
            cb = data[i+ 2] & 255;
            
            convertToRGB(cl, ca, cb);
            
            data[i]=(byte) r;
            data[i+1]=(byte) g;
            data[i+2]=(byte) b;
        }
        
        return data;
    }
    
    /**
     * convert to RGB and return as an image
     */
    @Override
    public final BufferedImage  dataToRGB(byte[] data, final int width, final int height) {
        
        BufferedImage image;
        
        data=dataToRGBByteArray(data, width, height, false);
        
        final DataBuffer db = new DataBufferByte(data, data.length);
        
        final int[] bands = { 0, 1, 2 };
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Raster raster = Raster.createInterleavedRaster(db, width, height, width * 3, 3, bands, null);
        
        image.setData(raster);
        
        return image;
        
    }
    
    /**
     * set CalRGB color (in terms of rgb)
     */
    @Override
    public final void setColor(final String[] number_values, final int items) {
        
        final float[] colValues=new float[items];
        
        for(int ii=0;ii<items;ii++) {
            colValues[ii]=Float.parseFloat(number_values[ii]);
        }
        
        setColor(colValues,items);
    }
    
    /**
     * reset any defaults if reused
     */
    @Override
    public final void reset(){
        
        lastC=-255;
        lastI=-255;
        lastE=-255;
        
        r=0;
        g=0;
        b=0;
        
        currentColor = new PdfColor(0,0,0);
    }
    
    /**
     * set CalRGB color (in terms of rgb)
     */
    @Override
    public final void setColor(final float[] number_values, final int items) {
        
        //get values (and allow for mapped from separation where only 1 value
        final float[] A = { 1.0f, 1.0f, 1.0f };
        
        //allow for use as alt colorspace which only has one value
        if (items == 3) {
            
            for (int i = 0; i < items; i++){
                A[i] =number_values[i];
                if(A[i]>1) {
                    return;
                }
            }
            
            convertToRGB(A[0],A[1],A[2]);
            
            this.currentColor= new PdfColor(r,g,b);
            
            //allow for indexed value where 1 values points to 3 in table
        }else if(items==1 && this.getIndexedMap()!=null){
            
            final int ptr=(int) (number_values[0]*3);
            final byte[] cmap=getIndexedMap();
            
            A[0]=((float)(cmap[ptr] & 255))/255f;
            A[1]=((float)(cmap[ptr+1] & 255))/255f;
            A[2]=((float)(cmap[ptr+2] & 255))/255f;
            
            convertToRGB(A[0],A[1],A[2]);
            
            this.currentColor= new PdfColor(r,g,b);
            
        }
    }
    
    private void convertToRGB(final float C, final float I, final float E){//], boolean convert) {
        
        if ((lastC == C) && (lastI == I) && (lastE == E)) {
        } else {
            
            //thanks Leonard for the formula
            final double ag = Math.pow(C, G[0]);
            final double bg = Math.pow(I, G[1]);
            final double cg = Math.pow(E, G[2]);
            
            final double X = Ma[0] * ag + Ma[3] * bg + Ma[6] * cg;
            final double Y = Ma[1] * ag + Ma[4] * bg + Ma[7] * cg;
            final double Z = Ma[2] * ag + Ma[5] * bg + Ma[8] * cg;
            
            // convert XYZ to RGB, including gamut mapping and gamma correction
            final double rawR = cs00 * X + cs01 * Y + cs02 * Z;
            final double rawG = cs10 * X + cs11 * Y + cs12 * Z;
            final double rawB = cs20 * X + cs21 * Y + cs22 * Z;
            
            // compute the white point adjustment
            final double kr = 1 / (cs00 * W[0] + cs01 * W[1] + cs02 * W[2]);
            final double kg = 1 / (cs10 * W[0] + cs11 * W[1] + cs12 * W[2]);
            final double kb = 1 / (cs20 * W[0] + cs21 * W[1] + cs22 * W[2]);
            
            // compute final values based on
            r = (int) (255 * Math.pow(clip(rawR * kr), 0.5));
            g = (int) (255 * Math.pow(clip(rawG * kg), 0.5));
            b = (int) (255 * Math.pow(clip(rawB * kb), 0.5));
            
            //calcuate using Tristimulus values
            //r = (int) (C * 255);
            //g = (int) (I * 255);
            //b = (int) (E * 255);
            
            //cache for later
            lastC = C;
            lastI = I;
            lastE = E;
        }
    }
    
    static double clip(final double v) {
        if(v>1) {
            return 1.0;
        } else {
            return v;
        }
    }
    
}




