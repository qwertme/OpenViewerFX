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
 * QCD.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 *
 */
public class QCD {

    public int guardBits;
    /**
     * 0 - no quantization 1 - scalar quantization 2 - scalar expounded
     */
    public int quantBits;
    public boolean hasScalar;
    public int[] exponentB;
    public int[] mantissaB;

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("ExponentValues\n");
        if (exponentB != null) {
            for (int i = 0; i < exponentB.length; i++) {
                str.append('\t').append(exponentB[i]);
            }
            str.append('\n');
        }
        if (mantissaB != null) {
            str.append("MantissaValues\n");
            for (int i = 0; i < mantissaB.length; i++) {
                str.append('\t').append(mantissaB[i]);
            }
            str.append('\n');
        }
        return str.toString();
    }

}
