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
 * SIZ.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 *
 */
public class SIZ {

    public int capabilities;
    public int Xsiz;
    public int Ysiz;
    public int XOsiz;
    public int YOsiz;
    public int XTsiz;
    public int YTsiz;
    public int XTOsiz;
    public int YTOsiz;
    public int nComp;
    public int[][] precisionInfo;

    @Override
    public String toString() {
        
     StringBuilder str = new StringBuilder("\n\tCapabilities : " + capabilities
                + "\n\tGrid Width: " + Xsiz
                + "\n\tGrid Height: " + Ysiz
                + "\n\tGrid X: " + XOsiz
                + "\n\tGrid Y: " + YOsiz
                + "\n\tTile Width: " + XTsiz
                + "\n\tTile Height: " + YTsiz
                + "\n\tTile X: " + XTOsiz
                + "\n\tTile Y: " + YTOsiz
                + "\n\tNcomp: " + nComp);
        if (precisionInfo != null) {
            str.append("\n\tPrecisionInfo:\n");
            for (int[] precisionInfo1 : precisionInfo) {
                for (int j = 0; j < precisionInfo[0].length; j++) {
                    str.append('\t').append(precisionInfo1[j]);
                }
                str.append('\n');
            }
        } else {
            str.append("\n\tNo Precision Info");
        }
        return str.toString();
    }
}
