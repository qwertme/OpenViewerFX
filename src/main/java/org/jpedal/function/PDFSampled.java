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
 * PDFSampled.java
 * ---------------
 */
package org.jpedal.function;

/**
 * Class to handle Type 0 shading (Sampled) from a Pdf
 */
public class PDFSampled extends PDFGenericFunction implements PDFFunction {

    private final int[] size;
    private final int m;
    private final int n;
    private final float outputs[];
    private final double[] sampleArray;
    private float[] prevInputs;

    public PDFSampled(final byte[] stream, final int bits, final float[] domain, final float[] range,
            final float[] encode, final float[] decode, final int[] size) {
        super(domain, range);
        this.size = size;
        this.m = domain.length / 2;
        this.n = range.length / 2;

        int sampleLen = 1;

        for (int i = 0; i < size.length; i++) {
            sampleLen *= size[i];
        }
        sampleLen *= (range.length / 2);

        sampleArray = new double[sampleLen];
        int pos = 0;
        int buffer = 0;

        double sampleMul = 1.0 / (Math.pow(2, bits) - 1);

        int index = 0;
        for (int i = 0; i < sampleLen; i++) {
            while (pos < bits) {
                buffer <<= 8;
                buffer |= (stream[index++]&0xff);
                pos += 8;
            }
            pos -= bits;
            sampleArray[i] = (buffer >> pos) * sampleMul;
            buffer &= (1L << pos) - 1;
        }

        if (encode != null) {
            this.encode = encode;
        } else {
            final int defaultSize = size.length;
            this.encode = new float[defaultSize * 2];
            for (int ii = 0; ii < defaultSize; ii++) {
                this.encode[(ii * 2) + 1] = size[ii] - 1;
            }
        }

        if (decode != null) {
            this.decode = decode;
        } else {
            final int defaultSize = range.length;
            this.decode = new float[defaultSize];
            System.arraycopy(range, 0, this.decode, 0, defaultSize);
        }

        outputs = new float[n];
        prevInputs = new float[m];
        for (int i = 0; i < m; i++) {
            prevInputs[i] = Float.MAX_VALUE;
        }
    }

    /**
     * Calculate the output values for this point in the shading object. (Only
     * used by Stitching)
     *
     * @param subinput : Shading input values
     * @return returns the shading values for this point
     */
    @Override
    public float[] computeStitch(final float[] subinput) {
        return compute(subinput);
    }
    
    private static boolean isSame(float[] arr0, float[] arr1){
        for (int i = 0; i < arr0.length; i++) {
            if(arr0[i]!=arr1[i]){
                return false;
            }
        }
        return true;
    }

    @Override
    public float[] compute(float[] input) {
        if(isSame(input,prevInputs)){
            return outputs;
        }
        
        prevInputs = input.clone();

        int cubeVertices = 1 << m;
        double[] cubeN = new double[cubeVertices];
        int[] cubeVertex = new int[cubeVertices];

        for (int i = 0; i < cubeVertices; i++) {
            cubeN[i] = 1;
        }

        int k = n, pos = 1;
        for (int i = 0; i < m; ++i) {
            int first = 2 * i;
            int next = first + 1;

            double xi = Math.min(Math.max(input[i], domain[first]), domain[next]);
            double e = interpolateDouble(xi, domain[first], domain[next], encode[first], encode[next]);

            int cur = size[i];
            e = Math.min(Math.max(e, 0), cur - 1);

            double e0 = e < cur - 1 ? ((int)e) : (e - 1.0);
            double n0 = e0 + 1 - e;
            double n1 = e - e0; 
            double offset0 = e0 * k;
            double offset1 = offset0 + k; 
            for (int j = 0; j < cubeVertices; j++) {
                if ((j & pos) != 0) {
                    cubeN[j] *= n1;
                    cubeVertex[j] += offset1;
                } else {
                    cubeN[j] *= n0;
                    cubeVertex[j] += offset0;
                }
            }           

            k *= cur;
            pos <<= 1;
        }
        
        for (int i = 0; i < n; i++) {            
            int first = 2 * i;
            int next = first + 1;            
            double pp = 0;
            for (int j = 0; j < cubeVertices; j++) {
                pp += sampleArray[cubeVertex[j] + i] * cubeN[j];
            }            
            pp = interpolateDouble(pp, 0, 1, decode[first], decode[next]);
            outputs[i] = (float) Math.min(Math.max(pp, range[first]), range[next]);
        }
        return outputs;

    }

    private static double interpolateDouble(final double x, final double xmin, final double xmax, final double ymin, final double ymax) {
        return ((x - xmin) * (ymax - ymin) / (xmax - xmin)) + ymin;
    }

}
