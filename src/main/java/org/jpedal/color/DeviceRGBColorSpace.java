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
 * DeviceRGBColorSpace.java
 * ---------------
 */
package org.jpedal.color;

/**
 * handle RGB ColorSpace
 */
public class DeviceRGBColorSpace extends  GenericColorSpace{
    
    private static final long serialVersionUID = -7269417965203263694L;
    
    public DeviceRGBColorSpace(boolean isARGB){
        setType(ColorSpaces.DeviceRGB);
        
        if(isARGB){
            componentCount=4;
        }
    }
    
    public DeviceRGBColorSpace(){

        setType(ColorSpaces.DeviceRGB);
    }
    
    /**
     * set DeviceRGB color (in terms of rgb)
     */
    @Override
    public final void setColor(final String[] number_values, final int items) {
        
        final float[] colValues=new float[items];
        
        for(int ii=0;ii<items;ii++) {
            colValues[ii]=Float.parseFloat(number_values[ii]);
        }
        
        setColor(colValues,items);
    }
    
    /**set color*/
    @Override
    public final void setColor(final float[] operand, final int length) {
        
        int value;
        
        //note indexed also uses this
        if (length == 1) {
            
            if(IndexedColorMap==null){ //fix for odd file bert.pdf
                currentColor = new PdfColor(operand[0],operand[0],operand[0]);
            }else{
                //get indexed values
                final int[] val=new int[3];
                
                final int id =(int)(operand[0]*3);
                for(int i=0;i<3;i++){
                    value =getIndexedColorComponent(id+i);
                    val[i] = value;
                }
                
                //set color
                currentColor = new PdfColor(val[0],val[1],val[2]);
                
            }
            
        } else if (length > 2) {
            
            final float[] val=new float[3];
            
            //set colours and check in range
            for(int i=0;i<3;i++){
                
                val[i]= operand[i];
                if(val[i]<0) {
                    val[i]=0;
                }
                if(val[i]>1) {
                    val[i]=1;
                }
                
            }
            
            //set color
            currentColor = new PdfColor(val[0],val[1],val[2]);
            
        }
    }
}
