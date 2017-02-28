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
 * RadialContext.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

public class RadialContext implements PaintContext {

    private final GenericColorSpace shadingColorSpace;
    private final float[] background;
    private final PdfObject shadingObj;
    private final PDFFunction[] function;
    private final float[] coords;
    private boolean[] extended = {false, false};
    private float t0;
    private float t1 = 1.0f;
    private final float x0, y0, r0, x1, y1, r1, deltaX, deltaY, deltaR, deltaC, powerR0;
    private final Color colorT0, colorT1;
    

    RadialContext(AffineTransform xForm, boolean isPrinting, GenericColorSpace shadingColorSpace, float[] background, PdfObject shading, float[][] matrix,PDFFunction[] function) {

        this.shadingColorSpace = shadingColorSpace;
        this.background = background;
        this.shadingObj = shading;
        this.function = function;
        float[] src = shading.getFloatArray(PdfDictionary.Coords);
        final boolean[] extension = shadingObj.getBooleanArray(PdfDictionary.Extend);
        if (extension != null) {
            extended = extension;
        }
        final float[] domain = shadingObj.getFloatArray(PdfDictionary.Domain);
        if (domain != null) {
            t0 = domain[0];
            t1 = domain[1];
        }

        coords = new float[src.length];
        System.arraycopy(src, 0, coords, 0, src.length);

        //apply matrix and file values;
        AffineTransform affine = new AffineTransform(matrix[0][0], matrix[0][1], matrix[1][0], matrix[1][1], matrix[2][0], matrix[2][1]);

        PathIterator iter;
        GeneralPath gp;//path for coords
        double[] temp = new double[6];
        Point2D pointXY0, pointXY1, temp0, temp1;

        gp = new GeneralPath();
        gp.moveTo(coords[0], coords[1]);
        gp.lineTo(coords[0] + coords[2], coords[1]);
        iter = gp.getPathIterator(affine);
        iter.currentSegment(temp);
        pointXY0 = new Point2D.Double(temp[0], temp[1]);
        iter.next();
        iter.currentSegment(temp);
        temp0 = new Point2D.Double(temp[0],temp[1]);

        gp = new GeneralPath();
        gp.moveTo(coords[3], coords[4]);
        gp.lineTo(coords[3] + coords[5], coords[4]);
        iter = gp.getPathIterator(affine);
        iter.currentSegment(temp);
        pointXY1 = new Point2D.Double(temp[0], temp[1]);
        iter.next();
        iter.currentSegment(temp);
        temp1 = new Point2D.Double(temp[0],temp[1]);
                
        Point2D[] points = new Point2D[4];
        points[0] = pointXY0;
        points[1] = pointXY1;
        points[2] = temp0;
        points[3] = temp1;
        xForm.transform(points, 0,points, 0, 4);
        
        x0 = (float) points[0].getX();
        y0 = (float) points[0].getY();
        x1 = (float) points[1].getX();
        y1 = (float) points[1].getY();
        
        r0 = (float) points[0].distance(points[2]);
        r1 = (float) points[1].distance(points[3]);
        
        colorT0 = calculateColor(t0);
        colorT1 = calculateColor(t1);

        //dont use Math.pow functions here;
        deltaX = x1 - x0;
        deltaY = y1 - y0;
        deltaR = r1 - r0;
        deltaC = deltaX * deltaX + deltaY * deltaY - deltaR * deltaR;
        powerR0 = r0 * r0;
//        System.out.println("page height "+pageHeight+" offx"+offX+" offY"+offY+" pagew"+cropX);
    }

    @Override
    public void dispose() {

    }

    @Override
    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }

    private Color calculateColor(final float val) {
        final float[] colValues = ShadingFactory.applyFunctions(function, new float[]{val});
        shadingColorSpace.setColor(colValues, colValues.length);
        return (Color) shadingColorSpace.getColor();
    }
    
    @Override
    public Raster getRaster(int startX, int startY, int w, int h) {
        
        final int[] data = new int[w * h * 4];
        if (background != null) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    shadingColorSpace.setColor(background, shadingColorSpace.getColorComponentCount());
                    final Color c = (Color) shadingColorSpace.getColor();
                    //set color for the pixel with values
                    final int base = (y * w + x) * 4;
                    data[base] = c.getRed();
                    data[base + 1] = c.getGreen();
                    data[base + 2] = c.getBlue();
                    data[base + 3] = 255;
                }
            }
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
//                float[] xy = PixelFactory.convertPhysicalToPDF(isPrinting, x, y, offX, offY, (1f / scaling), startX, startY, cropX, pageHeight);
                float[] xy = {x+startX,y+startY};//ShadingUtils.getPixelPDF(isPrinting, rotation, x, y, startX, startY, offX, offY, cropX, pageHeight, scaling);
                Color result = null;

                float[] qr = quadraticEquate(xy[0], xy[1]);

                if (qr[1] >= 0 && qr[1] <= 1) {
                    result = calculateColor(getTfromS(qr[1]));
                } else if (extended[1] && qr[1] >= 0 && r1 + qr[1] * deltaR >= 0) {
                    result = colorT1;
                } else if (qr[0] >= 0 && qr[0] <= 1) {
                    result = calculateColor(getTfromS(qr[0]));
                } else if (extended[0] && qr[1] <= 0 && r1 + qr[1] * deltaR >= 0) {
                    result = calculateColor(getTfromS(qr[1]));
                } else if (extended[0] && qr[0] <= 1 && r1 + qr[0] * deltaR >= 0) {
                    result = colorT0;
                }

                if (result != null) {
                    final int base = (y * w + x) * 4;
                    data[base] = result.getRed();
                    data[base + 1] = result.getGreen();
                    data[base + 2] = result.getBlue();
                    data[base + 3] = 255;
                }

            }
        }
        final WritableRaster raster = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB).getRaster();
        raster.setPixels(0, 0, w, h, data);
        return raster;
    }

    private float getTfromS(float s) {
        return (s * (t1 - t0)) + t0;
    }

    private float[] quadraticEquate(float x, float y) {
        float xDiff = x - x0;
        float yDiff = y - y0;
        float p = -xDiff * deltaX - yDiff * deltaY - r0 * deltaR;
        float q = xDiff * xDiff + yDiff * yDiff - powerR0; //dont use Math.pow to xdiff,ydiff; 
        float sqrt = (float) Math.sqrt(p * p - deltaC * q);
        float sA = (sqrt - p) / deltaC;
        float sB = (-p - sqrt) / deltaC;
        return ((deltaC < 0) ? new float[]{sA, sB} : new float[]{sB, sA});
    }

}
