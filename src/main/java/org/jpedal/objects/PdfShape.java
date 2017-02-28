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
 * PdfShape.java
 * ---------------
 */
package org.jpedal.objects;

import java.awt.Shape;
import javafx.scene.shape.Path;

/**
 * allow us to have both Swing or javaFX implementations of Shape
 */
public interface PdfShape {

    void setEVENODDWindingRule();

    void setNONZEROWindingRule();

    void closeShape();

    Shape generateShapeFromPath(float[][] CTM, float lineWidth, int B, int type);

    void setClip(boolean b);

    void resetPath();

    boolean isClip();

    int getSegmentCount();

    int getComplexClipCount();

    void lineTo(float parseFloat, float parseFloat0);

    void moveTo(float parseFloat, float parseFloat0);

    void appendRectangle(float parseFloat, float parseFloat0, float parseFloat1, float parseFloat2);

    void addBezierCurveC(float x, float y, float x2, float y2, float x3, float y3);

    void addBezierCurveV(float parseFloat, float parseFloat0, float parseFloat1, float parseFloat2);

    void addBezierCurveY(float parseFloat, float parseFloat0, float parseFloat1, float parseFloat2);

    Path getPath();

    boolean adjustLineWidth();

    boolean isClosed();
}
