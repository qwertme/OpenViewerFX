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
 * ShadingUtils.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Class to hold shading related utilities
 */
public class ShadingUtils {

    public static Point2D findIntersect(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
        double xD1, yD1, xD2, yD2, xD3, yD3;
        //double dot, deg, len1, len2;
        double ua, div;

        // calculate differences  
        xD1 = p2.getX() - p1.getX();
        xD2 = p4.getX() - p3.getX();
        yD1 = p2.getY() - p1.getY();
        yD2 = p4.getY() - p3.getY();
        xD3 = p1.getX() - p3.getX();
        yD3 = p1.getY() - p3.getY();

        //len1 = Math.sqrt(xD1 * xD1 + yD1 * yD1);
        //len2 = Math.sqrt(xD2 * xD2 + yD2 * yD2);
        //dot = (xD1 * xD2 + yD1 * yD2);
        //deg = dot / (len1 * len2);
        // find intersection Pt between two lines  
        //Point pt = new Point(0, 0);
        div = yD2 * xD1 - xD2 * yD1;
        ua = (xD2 * yD3 - yD2 * xD3) / div;
        //ub = (xD1 * yD3 - yD1 * xD3) / div;
        return new Point2D.Double((p1.getX() + ua * xD1), (p1.getY() + ua * yD1));
    }

    public static Color interpolate2Color(final Color first, final Color second, float fraction) {
        final float INT_TO_FLOAT = 1f / 255f;
        fraction = Math.min(fraction, 1f);
        fraction = Math.max(fraction, 0f);

        final float R1 = first.getRed() * INT_TO_FLOAT;
        final float G1 = first.getGreen() * INT_TO_FLOAT;
        final float B1 = first.getBlue() * INT_TO_FLOAT;
        final float A1 = first.getAlpha() * INT_TO_FLOAT;

        final float R2 = second.getRed() * INT_TO_FLOAT;
        final float G2 = second.getGreen() * INT_TO_FLOAT;
        final float B2 = second.getBlue() * INT_TO_FLOAT;
        final float A2 = second.getAlpha() * INT_TO_FLOAT;

        final float DR = R2 - R1;
        final float DG = G2 - G1;
        final float DB = B2 - B1;
        final float DA = A2 - A1;

        float red = R1 + (DR * fraction);
        float green = G1 + (DG * fraction);
        float blue = B1 + (DB * fraction);
        float alpha = A1 + (DA * fraction);

        red = Math.max(Math.min(red, 1f), 0f);
        green = Math.max(Math.min(green, 1f), 0f);
        blue = Math.max(Math.min(blue, 1f), 0f);
        alpha = Math.max(Math.min(alpha, 1f), 0f);

        return new Color(red, green, blue, alpha);
    }

    public static Color bilinearInterpolateColor(
            final Color upperLeft, final Color upperRight,
            final Color lowerLeft, final Color lowerRight,
            final float fractionX, final float fractionY) {

        final Color x1 = interpolate2Color(upperLeft, upperRight, fractionX);
        final Color x2 = interpolate2Color(lowerLeft, lowerRight, fractionX);
        return interpolate2Color(x1, x2, fractionY);
    }

    /**
     * method used to find any point in given distance in cubic bezier curve
     *
     * @param t please note t varies between 0 -- 1
     * @param sp starting point
     * @param c1 control point
     * @param c2 control point
     * @param ep end point
     * @return
     */
    public static Point2D findDistancedPoint(double t, Point2D sp, Point2D c1, Point2D c2, Point2D ep) {
        //dont use Math.pow;
        double d = 1 - t;
        double dCube = d * d * d;
        double dSqr = d * d;
        double tCube = t * t * t;
        double tSqr = t * t;
        double xCoord = (dCube * sp.getX())
                + (3 * t * dSqr * c1.getX())
                + (3 * tSqr * d * c2.getX())
                + (tCube * ep.getX());
        double yCoord = (dCube * sp.getY())
                + (3 * t * dSqr * c1.getY())
                + (3 * tSqr * d * c2.getY())
                + (tCube * ep.getY());
        return new Point2D.Double(xCoord, yCoord);

    }

    public static int getRotationFromAffine(AffineTransform affine) {
        return (int) Math.toDegrees(Math.atan2(affine.getShearX(), affine.getScaleX()));
    }

    public static final float[] getPixelPDF(boolean isPrinting, int rotation, 
            float x, float y, int xStart, int yStart, float offX, float offY, 
            float minX, float pageHeight, float scaling) {

        final float pdfX;
        final float pdfY;

        if (!isPrinting) {
            switch (rotation) {
                case 0:
                    pdfX = (scaling * (xStart + x + minX - offX));
                    pdfY = (scaling * (pageHeight - (yStart + y - offY)));
                    return new float[]{pdfX, pdfY};
                case 90:
                    pdfY = (scaling * ((x + xStart)+minX-offX));
                    pdfX = (scaling * ((y + yStart)-(pageHeight+offY)));
                    return new float[]{pdfX, pdfY};
                case 180:
                    pdfX = (scaling * (offX - (x + xStart + minX)));
                    pdfY = (scaling * ((y + yStart) - (pageHeight + offY)));
                    return new float[]{pdfX, pdfY};
                case -90: //270 degrees
                    pdfY = (scaling * (pageHeight-(x + xStart+2)));
                    pdfX = (scaling * ((y + yStart)));
                    return new float[]{pdfX, pdfY};
            }
        } else {
            pdfX = ((x + xStart) - offX) * scaling;
            pdfY = ((y + yStart) - offY) * scaling;
            return new float[]{pdfX, pdfY};
        }
        return new float[]{0, 0};
    }
    
    public static GeneralPath getPathFromBBox(float[] rawBBox){
        GeneralPath rawPath = new GeneralPath();
        rawPath.moveTo(rawBBox[0], rawBBox[1]);
        rawPath.lineTo(rawBBox[2], rawBBox[1]);
        rawPath.lineTo(rawBBox[2], rawBBox[3]);
        rawPath.lineTo(rawBBox[0], rawBBox[3]);
        rawPath.lineTo(rawBBox[0], rawBBox[1]);
        rawPath.closePath();
        return rawPath;
    }
    
    public static float[] getPdfCoords(AffineTransform inversedAffine, int x, int y, int xStart, int yStart) {
        float[] ff = new float[2];
        ff[0] = x + xStart;
        ff[1] = y + yStart;
        inversedAffine.transform(ff, 0, ff, 0, 1);
        return ff;
    }

}
