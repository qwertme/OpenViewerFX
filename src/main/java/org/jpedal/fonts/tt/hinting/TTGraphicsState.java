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
 * TTGraphicsState.java
 * ---------------
 */
package org.jpedal.fonts.tt.hinting;

import java.io.Serializable;

/**
 * holds the graphics state variables
 */
public class TTGraphicsState implements Cloneable, Serializable {

    /**
     * Preset vectors -
     *
     * Vectors are stored as two F2Dot14 numbers stored in an int, with the x component taking up the high 16 bits and
     * the y component taking up the bottom 16. The hypotenuse must be 1 in length. (0x4000 in F2Dot14)
     */
    public static final int x_axis=0x40000000;
	public static final int y_axis=0x00004000;

    /**
     * Preset round states -
     *
     * The spec defines a way of interpreting an item from the stack to form a round state consisting of period, phase
     * and threshold for use in the SROUND instruction. This format is used instead of storing each aspect of the round
     * state individually.
     */
    public static final int hg =0x68;             //Half Grid
    public static final int g  =0x48;             //Grid
    public static final int dg =0x08;             //Double Grid
    public static final int dtg=0x44;             //Down to Grid
    public static final int utg=0x40;             //Up to Grid
    public static final int off=-1;               //None

    //Controls whether MIRP will flip CVT entries to match the actual distance
	public boolean autoFlip=true;

    //The maximum difference between the measured distance and the cvt entry if the entry is to be used for MIRP
	public int controlValueTableCutIn =68;

    //Lowest number in relative numbering system used by DELTA instructions
	public int deltaBase=9;

    //Determines the magnitude of movements by DELTA instructions
	public int deltaShift=3;

    //Vector which defines the direction of movement (for almost all instructions)
	public int freedomVector=x_axis;

    //Vector which defines the direction of measurement (usually)
    public int projectionVector=x_axis;

    //Vector which defines the direction of measurement when dealing with points original positions in GC, MD, MDRP and MIRP
    public int dualProjectionVector=x_axis;

    //Allows you to disable instructions entirely for glyph programs.
    public int instructControl;

    //Scan conversion not implemented - using java for anti aliased rendering instead
//    public boolean scanControl=false;

    //Used by some functions to repeat actions. It's always reset to 1 after it's used.
    public int loop=1;

    //Sets the minimum distance to which a value will be rounded in MIRP and MDRP
    public int minimumDistance=1;

    //Contains the components required to round variables
    public int roundState=g;

    //Grid period for rounding - 1 usually, sqrt(2)/2 for 45 degrees
    public double gridPeriod=1.0;

    //Reference points
    public int rp0;
    public int rp1;
    public int rp2;

    //The cutIn is the difference below which a distance will be replaced with the widthValue in MDRP and MIRP
    public int singleWidthCutIn;
	public int singleWidthValue;

    //Zone pointers
	public int zp0=TTVM.GLYPH_ZONE;
	public int zp1=TTVM.GLYPH_ZONE;
	public int zp2=TTVM.GLYPH_ZONE;

    /**
     * Create a copy of this object.
     * @return Copy reference
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Rounds a double according to the round state variable.
     * @param n Number to round
     * @return Rounded number
     */
    public double round(double n) {
        if (roundState == off) {
            return n;
        }

        final boolean isPositive = n > 0;

        //Get period
        int p = (roundState >> 6) & 3;
        final double period;
        if (p==0) {
            period = gridPeriod / 2;
        } else if (p==1) {
            period = gridPeriod;
        } else {
            period = gridPeriod * 2;
        }

        //Get phase
        p = (roundState >> 4) & 3;
        final double phase;
        if (p==0) {
            phase = 0;
        } else if (p==1) {
            phase = period / 4;
        } else if (p==2) {
            phase = period / 2;
        } else {
            phase = (3 * period) / 4;
        }

        //Get threshold
        p = roundState & 15;

        //special case - use largest number smaller than period
        if (p==0) {
            double result = phase;
            while (result < n) {
                result += period;
            }
            return result;
        }

        final double threshold = ((p-4) * period) / 8;

        //Round
        n -= phase;
        double lower = 0;
        if (n > 0) {
            n += threshold;
            while (lower+period <= n) {
                lower += period;
            }
        } else {
            n -= threshold;
            while (lower-period >= n) {
                lower -= period;
            }
        }
        n = lower;
        n += phase;

        //Make sure number doesn't flip from pos to neg/neg to pos
        if (isPositive && n < 0) {
            n = phase % period;
        }

        if (!isPositive && n > 0) {
            n = (phase - (10 * period)) % period;
        }

        return n;
    }

    /**
     * Rounds a F26Dot6 number according to the round state variable.
     * @param f26dot6 Number to round
     * @return Rounded number
     */
    public int round(final int f26dot6) {
        double d = TTVM.getDoubleFromF26Dot6(f26dot6);
        d = round(d);
        return TTVM.storeDoubleAsF26Dot6(d);
    }

    /**
     * Takes an F26Dot6 distance along the projection vector and calculates the shift along the freedom vector IN ACTUAL
     * SPACE required.
     * @param distance Move required along PV
     * @return F26Dot6 shifts needed in x and y
     */
    public int[] getFVMoveforPVDistance(final int distance) {
        if (distance == 0) {
            return new int[]{0, 0};
        }

        final int[] fv = getVectorComponents(freedomVector);
        fv[0] = TTVM.storeDoubleAsF26Dot6(TTVM.getDoubleFromF2Dot14(fv[0]));
        fv[1] = TTVM.storeDoubleAsF26Dot6(TTVM.getDoubleFromF2Dot14(fv[1]));
        final double fvWorth = TTVM.getDoubleFromF26Dot6(getCoordsOnVector(projectionVector, fv[0], fv[1]));
        if (fvWorth != 0) {
            final double mul = TTVM.getDoubleFromF26Dot6(distance);
            fv[0] = (int)((fv[0]*mul)/fvWorth);
            fv[1] = (int)((fv[1]*mul)/fvWorth);
        } else {
            fv[0] = 0;
            fv[1] = 0;
        }
        return fv;
    }

    /**
     * Get the F2Dot14 components of a vector.
     * @param vector vector to get
     * @return array of x and y components
     */
    static int[] getVectorComponents(final int vector) {
        return new int[]{(vector >> 16), ((vector << 16) >> 16)};
    }


    /**
     * Create a vector from two F2Dot14 numbers.
     * @param x x component
     * @param y y component
     * @return newly created vector
     */
    static int createVector(final int x, final int y) {
        return ((x & 0xFFFF) << 16) + (y & 0xFFFF);
    }

    /**
     * Get the coordinates of a point on a vector.
     * @param vector Vector to measure against
     * @param x F26Dot6 x coordinate
     * @param y F26Dot6 y coordinate
     * @return F26Dot6 coordinate on vector
     */
    static int getCoordsOnVector(final int vector, final int x, final int y) {
        final int[] pv = getVectorComponents(vector);
//        double xProj = TTVM.getDoubleFromF2Dot14(pv[0]) * TTVM.getDoubleFromF26Dot6(x);
//        double yProj = TTVM.getDoubleFromF2Dot14(pv[1]) * TTVM.getDoubleFromF26Dot6(y);
//        return TTVM.storeDoubleAsF26Dot6(xProj + yProj);
        final long xProj = (long)pv[0] * (long)x;
        final long yProj = (long)pv[1] * (long)y;
        long bigResult = xProj + yProj;
        final boolean roundUp = (bigResult & 0x3FFF) >= 0x7F;
        bigResult >>= 14;
        if (roundUp) {
            bigResult++;
        }
        return (int)bigResult;
    }

    /**
     * Resets various variables for each glyph.
     */
    public void resetForGlyph() {
        zp0 = TTVM.GLYPH_ZONE;
        zp1 = TTVM.GLYPH_ZONE;
        zp2 = TTVM.GLYPH_ZONE;
        projectionVector = x_axis;
        dualProjectionVector = x_axis;
        freedomVector = x_axis;
        roundState = g;
        loop = 1;
        controlValueTableCutIn=68;
    }


    //

}
