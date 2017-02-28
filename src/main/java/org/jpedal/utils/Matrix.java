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
 * Matrix.java
 * ---------------
 */
package org.jpedal.utils;

/**
provide matrix functionality used in PDF to calculate co-ords
 */
public class Matrix {
    
    //////////////////////////////////////////////////////////////////////////
    /**multiply two 3 * 3 matrices together & return result*/
    public static final float[][] multiply(final float[][] matrix1, final float[][] matrix2) {
        
        //output matrix for results
        final float[][] output_matrix = new float[3][3];
        
        //multiply
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                output_matrix[row][col] = (matrix1[row][0] * matrix2[0][col]) + (matrix1[row][1] * matrix2[1][col]) + (matrix1[row][2] * matrix2[2][col]);
                //allow for rounding errors
                /**
                if((output_matrix[row][col]>0.99)&&(output_matrix[row][col]<1))
                output_matrix[row][col]=1;
                else if((output_matrix[row][col]<-0.99)&&(output_matrix[row][col]>-1))
                output_matrix[row][col]=-1;
                else if((output_matrix[row][col]>0.0)&&(output_matrix[row][col]<0.001))
                output_matrix[row][col]=0;
                else if((output_matrix[row][col]<0.0)&&(output_matrix[row][col]>-0.001))
                output_matrix[row][col]=0;
                 */
                //if(Math.abs(output_matrix[row][col])<0.01)
                //output_matrix[row][col] =0;
            }
        }
        return output_matrix;
    }
//////////////////////////////////////////////////////////////////////////
    /**Calculates the inverse of a 3 * 3 matrix  return result*/
    public static final float[][] inverse(final float[][] input_matrix) {
        
        final float d = (input_matrix[2][0] * input_matrix[0][1] * input_matrix[1][2] - input_matrix[2][0] * input_matrix[0][2] * input_matrix[1][1] - input_matrix[1][0] * input_matrix[0][1] * input_matrix[2][2] + input_matrix[1][0] * input_matrix[0][2] * input_matrix[2][1] + input_matrix[0][0] * input_matrix[1][1] * input_matrix[2][2] - input_matrix[0][0] * input_matrix[1][2] * input_matrix[2][1]);
        final float t00 = (input_matrix[1][1] * input_matrix[2][2] - input_matrix[1][2] * input_matrix[2][1]) / d;
        final float t01 = -(input_matrix[0][1] * input_matrix[2][2] - input_matrix[0][2] * input_matrix[2][1]) / d;
        final float t02 = (input_matrix[0][1] * input_matrix[1][2] - input_matrix[0][2] * input_matrix[1][1]) / d;
        final float t10 = -(-input_matrix[2][0] * input_matrix[1][2] + input_matrix[1][0] * input_matrix[2][2]) / d;
        final float t11 = (-input_matrix[2][0] * input_matrix[0][2] + input_matrix[0][0] * input_matrix[2][2]) / d;
        final float t12 = -(-input_matrix[1][0] * input_matrix[0][2] + input_matrix[0][0] * input_matrix[1][2]) / d;
        final float t20 = (-input_matrix[2][0] * input_matrix[1][1] + input_matrix[1][0] * input_matrix[2][1]) / d;
        final float t21 = -(-input_matrix[2][0] * input_matrix[0][1] + input_matrix[0][0] * input_matrix[2][1]) / d;
        final float t22 = (-input_matrix[1][0] * input_matrix[0][1] + input_matrix[0][0] * input_matrix[1][1]) / d;
        
        final float[][] output_matrix = new float[3][3];
        
        output_matrix[0][0] = t00; output_matrix[0][1] = t01; output_matrix[0][2] = t02;
        output_matrix[1][0] = t10; output_matrix[1][1] = t11; output_matrix[1][2] = t12;
        output_matrix[2][0] = t20; output_matrix[2][1] = t21; output_matrix[2][2] = t22;
        
        return output_matrix;
    }
    
    //////////////////////////////////////////////////////////////////////////
    
    /**show matrix (used to debug)*/
    public static final void show(final float[][] matrix1) {
        
        //show lines
        for (int row = 0; row < 3; row++) {
            LogWriter.writeLog(row + "((" + matrix1[row][0] + " , " + matrix1[row][1] + " , " + matrix1[row][2] + " ))");
//            System.out.println( row + "(" + matrix1[row][0] + " , " + matrix1[row][1] + " , " + matrix1[row][2] + " )" );
        }
    }
    
    
    /**show matrix (used to debug)*/
    public static final void show(final int[][] matrix1) {
        
        //show lines
        for (int row = 0; row < 3; row++) {
            LogWriter.writeLog(row + "((" + matrix1[row][0] + " , " + matrix1[row][1] + " , " + matrix1[row][2] + " ))");
            //System.out.println( row + "(" + matrix1[row][0] + " , " + matrix1[row][1] + " , " + matrix1[row][2] + " )" );
        }
    }
}
