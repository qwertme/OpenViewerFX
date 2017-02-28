/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author suda
 */
public class AxialShadeContext implements PaintContext {

    private final GenericColorSpace shadingColorSpace;
    private final PDFFunction[] function;

    private final float[] background;
    private float[] domain = {0.0f, 1.0f};
    private boolean[] extension;
//    private final int pageHeight;
//    private final boolean isReversed;
final float t0;
    final float t1;
    final double x0;
    final double y0;
    final double x1;
    final double y1;
    final double deltaX;
    final double deltaY;
    //final double deltaT;
    final double multiXY;
    //final double textX;
    //final double textY;

    private final AffineTransform shadeAffine;
    private AffineTransform inversed;

    public AxialShadeContext(AffineTransform xform, GenericColorSpace shadingColorSpace,float[] background, PdfObject shadingObject, float[][] mm, PDFFunction[] function, int textX,int textY) {
        
        
        this.shadingColorSpace = shadingColorSpace;
        this.function = function;
        //this.textX = textX;
        //this.textY = textY;
//        this.pageHeight = pageHeight;

        final float[] newDomain = shadingObject.getFloatArray(PdfDictionary.Domain);
        if (newDomain != null) {
            domain = newDomain;
        }
        this.background = background;
        extension = shadingObject.getBooleanArray(PdfDictionary.Extend);
        if (extension == null) {
            extension = new boolean[]{false, false};
        }

        t0 = domain[0];
        t1 = domain[1];
        //deltaT = Math.abs(t1 - t0);

        if (mm == null) {
            shadeAffine = new AffineTransform();
        } else {
            shadeAffine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);
        }
        
        try {
            inversed = xform.createInverse();
        } catch (NoninvertibleTransformException ex) {
            
            if(LogWriter.isOutput()){
                LogWriter.writeLog("Exception "+ex+ ' ');
            }
            if(inversed==null){
                inversed = new AffineTransform();
            }
        }

        float[] coords = shadingObject.getFloatArray(PdfDictionary.Coords);
        GeneralPath coordPath = ShadingUtils.getPathFromBBox(coords);
        PathIterator iter = coordPath.getPathIterator(shadeAffine);
        double temp[] = new double[6];
        iter.currentSegment(temp);
        x0 = temp[0];
        y0 = temp[1];
        iter.next();
        iter.currentSegment(temp);
        x1 = temp[0];
        y1 = temp[1];
       
        deltaX = (x1 - x0);
        deltaY = (y1 - y0);
        multiXY = deltaX * deltaX + deltaY * deltaY;

    }

    @Override
    public void dispose() {

    }

    @Override
    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }

    @Override
    public Raster getRaster(int startX, int startY, int w, int h) {

        final int rastSize = (w * h * 4);
        final int[] data = new int[rastSize];

        if (background != null) {
            shadingColorSpace.setColor(background, 4);
            final Color c = (Color) shadingColorSpace.getColor();
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    final int base = (i * w + j) * 4;
                    data[base] = c.getRed();
                    data[base + 1] = c.getGreen();
                    data[base + 2] = c.getBlue();
                    data[base + 3] = 255;
                }
            }
        }

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {

                boolean render = true;

                double[] src = { startX + j, startY + i};
                inversed.transform(src, 0, src, 0, 1);
                
                double x = src[0];
                double y = src[1];

                float t = 0;
                
                double xp = (deltaX*(x-x0)+ deltaY*(y-y0))/multiXY;
                
                if (xp >= 0 && xp <= 1) {
                    t = (float) (t0 + (t1 - t0) * xp);
                } else if (xp < 0 && extension[0]) {
                    t = t0;
                } else if (xp > 1 && extension[1]) {
                    t = t1;
                } else {
                    render = false;
                }

                if (render) {                           
                    Color c = calculateColor(t);
                    final int base = (i * w + j) * 4;
                    data[base] = c.getRed();
                    data[base + 1] = c.getGreen();
                    data[base + 2] = c.getBlue();
                    data[base + 3] = 255;
                }
            }
        }

        final WritableRaster raster = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB).getRaster();
        raster.setPixels(0, 0, w, h, data);

        return raster;

    }

    private Color calculateColor(final float val) {
        final Color col;
        final float[] colValues = ShadingFactory.applyFunctions(function, new float[]{val});
        shadingColorSpace.setColor(colValues, colValues.length);
        col = (Color) shadingColorSpace.getColor();
        return col;
    }

    
}
