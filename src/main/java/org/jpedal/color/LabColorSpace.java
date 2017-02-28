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
 * LabColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.HashMap;
import java.util.Map;
import org.jpedal.examples.handlers.DefaultImageHelper;

import org.jpedal.io.ColorSpaceConvertor;

import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**
 * handle LabColorSpace
 */
public class LabColorSpace extends GenericColorSpace {
    
    private int r, g, b;
    private float lastL = -1, lastA=65536, lastBstar;
    
    /**holds values we have already calculated to speed-up*/
    private final Map cache = new HashMap();
    
    private static final float C1 = 108f / 841f;
    
    private static final float C2 = 4f / 29f;
    
    private static final float C3 = 6f / 29f;
    
    private static final float C4 = 100f / 255f;
    
    private static final float C5 = 128f;
    
    public LabColorSpace(final float[] whitepoint, final float[] range) {
        
        setType(ColorSpaces.Lab);
        setCIEValues(whitepoint, range, null, null);
        
    }
    
    /**
     * convert Index to RGB
     */
    @Override
    public byte[] convertIndexToRGB(final byte[] index){
        
        isConverted=true;
        
        final int size = index.length;
        float cl,ca,cb;
        
        
        for (int i = 0;
                i < size;
                i += 3) { //convert all values to rgb
            
            cl = (index[i] & 255) * C4;
            
            ca = (index[i+1] & 255) -C5;
            
            cb = (index[i+2] & 255) -C5;
            
            convertToRGB(cl, ca, cb);
            
            index[i]=(byte) r;
            index[i+1]=(byte)g;
            index[i+2]=(byte) b;
            
        }
        
        return index;
    }
    
    /**
     * <p>
     * Convert DCT encoded image bytestream to sRGB
     * </p>
     * <p>
     * It uses the internal Java classes and the Adobe icm to convert CMYK and
     * YCbCr-Alpha - the data is still DCT encoded.
     * </p>
     * <p>
     * The Sun class JPEGDecodeParam.java is worth examining because it contains
     * lots of interesting comments
     * </p>
     * <p>
     * I tried just using the new IOImage.read() but on type 3 images, all my
     * clipping code stopped working so I am still using 1.3
     * </p>
     */
    @Override
    public BufferedImage JPEGToRGBImage(final byte[] data, final int w, final int h, final float[] decodeArray, final int pX, final int pY, final boolean arrayInverted, final PdfObject XObject) {
        
        BufferedImage image;
        //ByteArrayInputStream in = null;
        
        try {
            
            Raster ras=DefaultImageHelper.readRasterFromJPeg(data);
            
            ras=cleanupRaster(ras,pX,pY,3);
            
            final int width = ras.getWidth();
            final int height = ras.getHeight();
            final int imgSize = width * height;
            final byte[] iData= ((DataBufferByte)ras.getDataBuffer()).getData();
            
            
            for (int i = 0;
                    i < imgSize * 3;
                    i += 3) { //convert all values to rgb
                
                final float cl = (iData[i] & 255) * C4;
                final float ca = (iData[i + 1] & 255) - C5;
                final float cb = (iData[i + 2] & 255) - C5;
                
                convertToRGB(cl, ca, cb);
                
                iData[i]=(byte) r;
                iData[i+1]=(byte) g;
                iData[i + 2]=(byte) b;
                
            }
            
            image =
                    new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            final DataBuffer db = new DataBufferByte(iData, iData.length);
            final int[] bands = {0,1,2};
            final Raster raster = Raster.createInterleavedRaster(db,width,height,width * 3,3,bands,null);
            
            image.setData(raster);
            
        } catch (final Exception ee) {
            image = null;
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
            }
        }
        
        return image;
        
    }
    
    /**
     * Lab to RGB
     */
    @Override
    public byte[] dataToRGBByteArray(final byte[] data, final int w, final int h, boolean arrayInverted){
        
        final int imgSize = w * h;
        
        for (int i = 0;
                i < imgSize * 3;
                i += 3) { //convert all values to rgb
            
            final float cl = (data[i] & 255) * C4;
            final float ca = (data[i + 1] & 255) - C5;
            final float cb = (data[i + 2] & 255) - C5;
            
            convertToRGB(cl, ca, cb);
            
            data[i]=(byte)r;
            data[i + 1]=(byte)g;
            data[i + 2]=(byte)b;
            
        }
        return data;
    }
    
    
    
    /**
     * convert LAB stream to RGB and return as an image
     */
    @Override
    public BufferedImage  dataToRGB(byte[] data, final int width, final int height) {
        
        BufferedImage image;
        
        try {
            
            data=dataToRGBByteArray(data, width, height, false);
        
            image =new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            final Raster raster = ColorSpaceConvertor.createInterleavedRaster(data, width, height);
            image.setData(raster);
            
        } catch (final Exception ee) {
            image = null;
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
            }
        }
        
        return image;
        
    }
    
    /**convert numbers to rgb values*/
    private void convertToRGB(float l, float a, float bstar) {
        
        //make sure in range
        if (l < 0) {
            l = 0;
        } else if (l > 100) {
            l = 100;
        }
        
        if (a < R[0]) {
            a = R[0];
        } else if (a > R[1]) {
            a = R[1];
        }
        
        if (bstar < R[2]) {
            bstar = R[2];
        } else if (bstar > R[3]) {
            bstar = R[3];
        }
        
        /**don't bother if already calculated*/
        if ((lastL == l) && (lastA == a) && (lastBstar == bstar)) {
        } else {
            
            //set indices
            final int indexL = (int) l;
            final int indexA = (int) (a - R[0]);
            final int indexB = (int) (bstar - R[2]);
            
            final Integer key = (indexL << 16) + (indexA << 8) + indexB;
            
            final Object value = cache.get(key);
            /**used cache value or recalulate*/
            if (value != null) {
                
                final int raw = (Integer) value;
                r = ((raw >> 16) & 255);
                g = ((raw >> 8) & 255);
                b = ((raw) & 255);
                
            } else {
                
                final double val1 = (l + 16d) / 116d;
                final double[] vals = new double[3];
                
                vals[0] = val1 + (a / 500d);
                vals[1] = val1;
                vals[2] = val1 - (bstar / 200d);
                
                float[] out = new float[3];
                for (int j = 0; j < 3; j++) {
                    if (vals[j] >= C3) {
                        out[j] = (float) (W[j] * vals[j] * vals[j] * vals[j]);
                    } else {
                        out[j] = (float) (W[j] * C1 * (vals[j] - C2));
                    }
                    
                    //avoid negative numbers
                    //seem to give very odd results
                    if(out[j]<0) {
                        out[j]=0;
                    }
                }
                
                //convert to rgb
                out = cs.toRGB(out);
                
                //put values into array
                r = (int) (out[0] * 255);
                g = (int) (out[1]* 255);
                b = (int) (out[2] * 255);
                
                //check in range
                if (r < 0) {
                    r = 0;
                }
                if (g < 0) {
                    g = 0;
                }
                if (b < 0) {
                    b = 0;
                }
                
                if (r > 255) {
                    r = 255;
                }
                if (g > 255) {
                    g = 255;
                }
                if (b > 255) {
                    b = 255;
                }
                
                final int raw = (r << 16) + (g << 8) + b;
                
                //store values in cache
                cache.put(key, raw);
                
            }
            
            lastL = l;
            lastA = a;
            lastBstar = bstar;
            
        }
    }
    
    /**
     * set color (in terms of rgb)
     */
    @Override
    public final void setColor(final String[] number_values, final int items) {
        
        final float[] colValues=new float[items];
        
        for(int ii=0;ii<items;ii++) {
            colValues[ii]=Float.parseFloat(number_values[ii]);
        }
        
        setColor(colValues,items);
    }
    
    /**set color*/
    @Override
    public final void setColor(final float[] operand, final int length) {
        
        //get raw values
        final float l = operand[0];
        final float a = operand[1];
        final float Bstar = operand[2];
        
        convertToRGB(l, a, Bstar);
        
        this.currentColor =new PdfColor(r,g, b);
        
    }
}
