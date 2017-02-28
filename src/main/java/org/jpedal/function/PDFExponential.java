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
 * PDFExponential.java
 * ---------------
 */
package org.jpedal.function;

/**
 * Class to handle Type 2 shading (Exponential)
 */
public class PDFExponential extends PDFGenericFunction implements PDFFunction {

    private float[] C0 = {0.0f}, C1 = {1.0f};

    private final float N;

    final int returnValues;

    private final float[] diffs;

    public PDFExponential(final float N, final float[] C0, final float[] C1, final float[] domain, final float[] range) {

        super(domain, range);
        this.N = N;

        if (C0 != null) {
            this.C0 = C0;
        }

        if (C1 != null) {
            this.C1 = C1;
        }

        //note C0 might be null so use this.C0
        returnValues = this.C0.length;

        diffs = new float[returnValues];

        for (int i = 0; i < diffs.length; i++) {
            diffs[i] = this.C1[i] - this.C0[i];
        }        

    }

    /**
     * Compute the required values for exponential shading (Only used by
     * Stitching)
     *
     * @param subinput : input values
     * @return The shading values as float[]
     */
    @Override
    public float[] computeStitch(final float[] subinput) {
        return compute(subinput);

    }

    @Override
    public float[] compute(final float[] values) {

	final float[] output = new float[returnValues];

        float x = min(max(values[0], domain[0]), domain[1]);
        if(N!=1.0){
            x = (float) Math.pow(x, N);
        }
        
        for (int j = 0; j < C0.length; j++) {
            output[j] = C0[j] + x * diffs[j];
        }
        
        if(range!=null){
            for (int i = 0; i < C0.length; i++) {
                output[i] = (min(max(output[i], range[i * 2]), range[i * 2 + 1]));         
            }
        }

        return output;
    }
}




//		//Only first value required
//		final float x=min(max(values[0],domain[0*2]),domain[0*2+1]);
//		
//		if (N==1f){// special case
//			
//			for (int i=0; i<C0.length; i++){
//				//x^1 = x so don't bother finding the power
//				output[i] = C0[i] + x * (C1[i]-C0[i]); 
//				
//				//clip to range if present
//				if (range!=null) {
//                    output[i] = min(max(output[i], range[i * 2]), range[i * 2 + 1]); //Clip output
//                }
//				
//				result[i]=output[i];
//				
//			}
//		}else{
//			for (int i=0; i<C0.length; i++){
//				output[i] = C0[i] + (float)Math.pow(x, N) * (C1[i]-C0[i]);
//				
//				//clip to range if present.
//				if (range!=null) {
//                    output[i] = min(max(output[i], range[i * 2]), range[i * 2 + 1]); //Clip output
//                }
//				
//				result[i]=output[i];
//				
//			}
//		}
//                System.out.println(result[0]+" "+result[1]+" "+result[2]);