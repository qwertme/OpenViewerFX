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
 * FastMath.java
 * ---------------
 */
package org.jpedal.utils;

/**
 * This class is used to handle computationally fastest mathematics formulas and
 * does not guarantee accurate output
 *
 * @author suda
 */
public class FastMath {

    /**
     * return the square root of c, computed using Newton's method
     *
     * @param a
     * @return
     */
    public static double sqrt(double a) {
        if (a < 0) {
            return Double.NaN;
        }
        final double e = 1E-15;
        double t = a;
        while (Math.abs(t - a / t) > e * t) {
            t = (a / t + t) / 2.0;
        }
        return t;
    }

    /**
     * not 100% accurate ,dont use in negative values     *
     * @param x
     * @param n
     * @return
     */
    public static final int pow(int x, int n) {
        if(n==0){
            return 1;
        }
        int a = x;
        for (int i = 1; i < n; i++) {
            a *= x;
        }
        return a;
    }

    /**
     * not 100% accurate ,dont use in negative values     *
     * @param x
     * @param n
     * @return
     */
    public static final float pow(float x, int n) {
        if(n==0){
            return 1;
        }
        float a = x;
        for (int i = 1; i < n; i++) {
            a *= x;
        }
        return a;
    }

    /**
     * not 100% accurate ,dont use in negative values     *
     * @param x
     * @param n
     * @return
     */
    public static final double pow(double x, int n) {
        if(n==0){
            return 1;
        }
        double a = x;
        for (int i = 1; i < n; i++) {
            a *= x;
        }
        return a;
    }

}
