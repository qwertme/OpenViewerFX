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
 * LatticeFormContext.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 *
 * 
 */
public class LatticeFormContext implements PaintContext {

    //private final GenericColorSpace shadingColorSpace;
    //private final float[] background;
    private final int bitsPerCoordinate;
    private final int bitsPerComponent;
    private final int verticesPerRow;
    private final int colCompCount;
    private final float[] decodeArr;
//    private final float[][] CTM;
    private final float[][] matrix;
    private final ArrayList<Point2D> triangles;
    private final ArrayList<Color> triColors;
    private final ArrayList<GeneralPath> shapes;
    private final int pageHeight;
    private final float scaling;
    private final int offX;
    private final int offY;
    private final BitReader reader;

    LatticeFormContext(final GenericColorSpace shadingColorSpace,
            final float[] background, PdfObject shadingObject,
            float[][] matrix, int pageHeight, float scaling, int offX, int offY) {

        //this.shadingColorSpace = shadingColorSpace;
        //this.background = background;
        bitsPerComponent = shadingObject.getInt(PdfDictionary.BitsPerComponent);
        bitsPerCoordinate = shadingObject.getInt(PdfDictionary.BitsPerCoordinate);
        verticesPerRow = shadingObject.getInt(PdfDictionary.VerticesPerRow);
        decodeArr = shadingObject.getFloatArray(PdfDictionary.Decode);
        colCompCount = shadingColorSpace.getColorComponentCount();
        boolean hasSmallBits = bitsPerComponent<8 || bitsPerCoordinate<8;
        reader = new BitReader(shadingObject.getDecodedStream(),hasSmallBits);

        this.matrix = matrix;
        this.pageHeight = pageHeight;
        this.scaling = scaling;
        this.offX = offX;
        this.offY = offY;

        triangles = new ArrayList<Point2D>();
        triColors = new ArrayList<Color>();
        shapes = new ArrayList<GeneralPath>();

        process();
        adjustPoints();
    }

    /**
     * process the datastream and update the variables
     */
    private void process() {
        ArrayList<Point2D> pList = new ArrayList<Point2D>();
        ArrayList<Color> cList = new ArrayList<Color>();
        while (reader.getPointer() < reader.getTotalBitLen()) {
            Point2D p = getPointCoords();
            pList.add(p);
            float[] cc = {1f, 1f, 1f, 1f};
            for (int z = 0; z < colCompCount; z++) {
                cc[z] =  reader.getFloat(bitsPerComponent);
            }
            Color c = new Color(cc[0], cc[1], cc[2], cc[3]);
            cList.add(c);
        }
        int totalRows = pList.size() / verticesPerRow;

        //populate triangles : please refer to the pdf spec for understanding
        for (int mm = 0; mm < (totalRows - 1); mm++) {
            int mRows = mm * totalRows;
            for (int nn = 0; nn < (verticesPerRow - 1); nn++) {
                int nm = nn + mRows;
                int[] t = new int[6];
                t[0] = nm;
                t[1] = t[3] = nm + 1;
                t[2] = t[4] = nm + verticesPerRow;
                t[5] = nm + verticesPerRow + 1;
                for (int z = 0; z < t.length; z++) {
                    triangles.add(pList.get(t[z]));
                    triColors.add(cList.get(t[z]));
                }
            }
        }
    }

    /**
     * convert small points to x,y full space points
     */
    private void adjustPoints() {

        float xMin = decodeArr[0];
        float xMax = decodeArr[1];
        float yMin = decodeArr[2];
        float yMax = decodeArr[3];

        float xw = xMax - xMin;
        float yw = yMax - yMin;

        ArrayList<Point2D> triPoints = new ArrayList<Point2D>();
        for (Point2D p : triangles) {
            double xx = p.getX();
            double yy = p.getY();
            xx = (xw * xx) + xMin;
            yy = (yw * yy) + yMin;
            triPoints.add(new Point2D.Double(xx, yy));
        }
        triangles.clear();

        //bring back to normal matrix;
        float scaleX = 1 / (matrix[0][0]);
        float scaleY = 1 / (matrix[1][1]);
        float tx = matrix[2][0] * scaleX;
        float ty = matrix[2][1] * scaleY;

        for (Point2D t : triPoints) {
            double x = t.getX();
            double y = t.getY();
            float b = 0;
            float c = 0;
            double xx = (x) + (c * y) + tx ;
            double yy = (b * x) + y + ty;
            // convert the points to integer for better performance
            xx = (int) xx;
            yy = (int) yy;
            triangles.add(new Point2D.Double(xx, yy));

        }

        for (int t = 0; t < triangles.size(); t += 3) {
            GeneralPath sh = new GeneralPath();
            sh.moveTo(triangles.get(t).getX(), triangles.get(t).getY());
            sh.lineTo(triangles.get(t + 1).getX(), triangles.get(t + 1).getY());
            sh.lineTo(triangles.get(t + 2).getX(), triangles.get(t + 2).getY());
            sh.closePath();
            shapes.add(sh);
        }
    }

    private Point2D getPointCoords() {
        double x = 0;
        double y = 0;

        for (int z = 0; z < 2; z++) {
            switch (z) {
                case 0:
                    x = reader.getFloat(bitsPerCoordinate);
                    break;
                case 1:
                    y = reader.getFloat(bitsPerCoordinate);
                    break;
            }
        }
        return new Point2D.Double(x, y);
    }
    
//    private float getFloat(int bits) {
//        float number = 0.0f;
//        switch (bits) {
//            case 8:
//                number = (bytes[pointer] & 255) / 256f;
//                pointer++;
//                break;
//            case 16:
//                number = (bytes[pointer] & 255) / 256f;
//                pointer++;
//                number += (bytes[pointer] & 255) / 65536f;
//                pointer++;
//                break;
//            case 24:
//                number = (bytes[pointer] & 255) / 256f;
//                pointer++;
//                number += (bytes[pointer] & 255) / 65536f;
//                pointer++;
//                number += (bytes[pointer] & 255) / 16777216f;
//                pointer++;
//                break;
//            case 32:
//                number = (bytes[pointer] & 255) / 256f;
//                pointer++;
//                number += (bytes[pointer] & 255) / 65536f;
//                pointer++;
//                number += (bytes[pointer] & 255) / 16777216f;
//                pointer++;
//                number += (bytes[pointer] & 255) / 4294967296f;
//                pointer++;
//                break;
//        }
//        return number;
//    }

    @Override
    public void dispose() {
        //
    }

    @Override
    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }

    @Override
    public Raster getRaster(int xStart, int yStart, int w, int h) {

        final int rastSize = (w * h * 4);
        final int[] data = new int[rastSize];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
//                float pdfX = xStart + x;
//                float pdfY = pageHeight - (yStart + y);
                float[] xy = PixelFactory.convertPhysicalToPDF(false, x, y, offX, offY, 1/scaling, xStart, yStart, 0, pageHeight);
                float pdfX = xy[0];
                float pdfY = xy[1];
                int sc = 0; //shape counts
                for (GeneralPath sh : shapes) {
                    Point2D pdfPoint = new Point2D.Double(pdfX, pdfY);
                    if (sh.contains(pdfPoint)) {
                        Rectangle2D rect = sh.getBounds2D();
                        Point2D scanStart = new Point2D.Double(-1024, pdfY);
                        Point2D scanEnd = new Point2D.Double(rect.getX() + rect.getWidth(), pdfY);
                        Line2D scanLine = new Line2D.Double(scanStart, scanEnd);

                        Point2D a = triangles.get(sc + 0);
                        Color aCol = triColors.get(sc + 0);
                        Point2D b = triangles.get(sc + 1);
                        Color bCol = triColors.get(sc + 1);
                        Point2D c = triangles.get(sc + 2);
                        Color cCol = triColors.get(sc + 2);

                        Line2D ab = new Line2D.Double(a, b);
                        Line2D bc = new Line2D.Double(b, c);
                        Line2D ac = new Line2D.Double(a, c);

                        Color result;

                        ArrayList<Color> colPoints = new ArrayList<Color>();
                        ArrayList<Point2D> interPoints = new ArrayList<Point2D>();

                        if (ab.intersectsLine(scanLine)) {
                            Point2D inter = ShadingUtils.findIntersect(scanStart, scanEnd, a, b);
                            double ai = a.distance(inter);
                            double bi = b.distance(inter);
                            float fraction = (float) (ai / (ai + bi));
                            Color col = ShadingUtils.interpolate2Color(aCol, bCol, fraction);
                            colPoints.add(col);
                            interPoints.add(inter);
                        }
                        if (bc.intersectsLine(scanLine)) {
                            Point2D inter = ShadingUtils.findIntersect(scanStart, scanEnd, b, c);
                            double bi = b.distance(inter);
                            double ci = c.distance(inter);
                            float fraction = (float) (bi / (bi + ci));
                            Color col = ShadingUtils.interpolate2Color(bCol, cCol, fraction);
                            colPoints.add(col);
                            interPoints.add(inter);
                        }
                        if (ac.intersectsLine(scanLine)) {
                            Point2D inter = ShadingUtils.findIntersect(scanStart, scanEnd, a, c);
                            double ai = a.distance(inter);
                            double ci = c.distance(inter);
                            float fraction = (float) (ai / (ai + ci));
                            Color col = ShadingUtils.interpolate2Color(aCol, cCol, fraction);
                            colPoints.add(col);
                            interPoints.add(inter);
                        }

                        if (interPoints.size() == 2) {
                            double first = interPoints.get(0).distance(pdfPoint);
                            double second = interPoints.get(1).distance(pdfPoint);
                            Color firstColor = colPoints.get(0);
                            Color secondColor = colPoints.get(1);
                            float fraction = (float) (first / (first + second));
                            result = ShadingUtils.interpolate2Color(firstColor, secondColor, fraction);

                        } else {
                            if (a.getY() == pdfPoint.getY()) {
                                double first = interPoints.get(1).distance(pdfPoint);
                                double second = a.distance(pdfPoint);
                                Color firstColor = colPoints.get(1);
                                float fraction = (float) (first / (first + second));
                                result = ShadingUtils.interpolate2Color(firstColor, aCol, fraction);
                            } else if (b.getY() == pdfPoint.getY()) {
                                double first = interPoints.get(2).distance(pdfPoint);
                                double second = b.distance(pdfPoint);
                                Color firstColor = colPoints.get(2);
                                float fraction = (float) (first / (first + second));
                                result = ShadingUtils.interpolate2Color(firstColor, bCol, fraction);
                            } else {
                                double first = interPoints.get(0).distance(pdfPoint);
                                double second = c.distance(pdfPoint);
                                Color firstColor = colPoints.get(0);
                                float fraction = (float) (first / (first + second));
                                result = ShadingUtils.interpolate2Color(firstColor, cCol, fraction);
                            }
                        }
                        final int base = (y * w + x) * 4;
                        data[base] = result.getRed();
                        data[base + 1] = result.getGreen();
                        data[base + 2] = result.getBlue();
                        data[base + 3] = result.getAlpha();

                    }
                    sc += 3;
                }
            }
        }

        final WritableRaster raster = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB).getRaster();
        raster.setPixels(0, 0, w, h, data);
        return raster;

    }

}
