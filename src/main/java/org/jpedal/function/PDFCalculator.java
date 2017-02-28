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
 * PDFCalculator.java
 * ---------------
 */
package org.jpedal.function;

import org.jpedal.utils.LogWriter;

/**
 * Class to handle Type 4 shading (PostScript Calculator) from a Pdf
 */
public class PDFCalculator extends PDFGenericFunction implements PDFFunction {

    private final PostscriptFactory post;

    private final int n;

    public PDFCalculator(final byte[] stream, final float[] domain, final float[] range) {

        super(domain, range);

        post = new PostscriptFactory(stream);

        n = range.length / 2;

    }

    /**
     * Calculate the output values for this point in the shading object. (Only
     * used by Stitching)
     *
     * @return returns the shading values for this point
     */
    @Override
    public float[] computeStitch(final float[] subinput) {
        return compute(subinput);

    }

    @Override
    public float[] compute(final float[] values) {

        final float[] result = new float[n];

        try {
            post.resetStacks(values);
            final double[] stack = post.executePostscript();

            for (int i = 0; i < n; i++) {
                result[i] = min(max((float) (stack[i]), range[i * 2]), range[i * 2 + 1]);
            }

        } catch (final Exception e) {
            //tell user and log
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        return result;
    }
}
