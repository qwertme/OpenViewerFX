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
 * T1GlyphFactory.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import java.awt.geom.GeneralPath;
import org.jpedal.external.ExternalHandlers;

import org.jpedal.fonts.tt.*;
import org.jpedal.fonts.tt.hinting.TTVM;
import org.jpedal.utils.repositories.Vector_Float;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Path;

/**
 * decodes the T1 glyph into a set of paths wrapped around a glyph
 */
public class T1GlyphFactory implements GlyphFactory {

    private static JavaFXSupport javaFXSupport;
    
    
    public T1GlyphFactory(final boolean useFX){
        this.useFX=useFX; 
        
        if(useFX){
            javaFXSupport=ExternalHandlers.getFXHandler();
        }
    }
    
    final boolean useFX;

    private static final float zero=0f;

    /**we tell user we have not used some shapes only ONCE*/
    private Vector_Float shape_primitive_x2 = new Vector_Float( 1000 );
    private Vector_Float shape_primitive_y = new Vector_Float( 1000 );

    /**store shape currently being assembled*/
    private Vector_Int shape_primitives = new Vector_Int( 1000 );

    private Vector_Float shape_primitive_x3 = new Vector_Float( 1000 );
    private Vector_Float shape_primitive_y3 = new Vector_Float( 1000 );

    private Vector_Float shape_primitive_y2 = new Vector_Float( 1000 );
    private Vector_Float shape_primitive_x = new Vector_Float( 1000 );

    protected static final int H = 3;
    protected static final int L = 2;

    /**flags for commands used*/
    protected static final int M = 1;
    protected static final int C = 5;

    /**vertical positioning and scaling*/
    private float ymin;
    private int leftSideBearing;


    
    /* (non-Javadoc)
     * @see org.jpedal.fonts.GlyphFactory#getGlyph()
     */
    @Override
    public PdfGlyph getGlyph() {
        if(useFX) {
            return getFXGlyph();
        } else {
            return getSwingGlyph();
        }
        
    }

    /* (non-Javadoc)
     * @see org.jpedal.fonts.GlyphFactory#getGlyph()
     */
    private PdfGlyph getSwingGlyph() {

        //initialise cache
        final Vector_Path cached_current_path=new Vector_Path(100);

        //create the shape - we have to do it this way
        //because we get the WINDING RULE last and we need it
        //to initialise the shape
        GeneralPath current_path =new GeneralPath(GeneralPath.WIND_NON_ZERO);
        current_path.moveTo(0,0);
        //init points
        final float[] x = shape_primitive_x.get();
        final float[] y = shape_primitive_y.get();
        final float[] x2 = shape_primitive_x2.get();
        final float[] y2 = shape_primitive_y2.get();
        final float[] x3 = shape_primitive_x3.get();
        final float[] y3 = shape_primitive_y3.get();
        int i = 0;
        final int end = shape_primitives.size() - 1;
        final int[] commands=shape_primitives.get();


        //loop through commands and add to glyph
        while( i < end )
        {

            //System.out.println(i+" "+x+" "+y);
            if( commands[i] == L ){
                current_path.lineTo(x[i],y[i]-ymin);
            }else if( commands[i] == H ){
                current_path.closePath();

                //save for later use
                cached_current_path.addElement(current_path);

                current_path=new GeneralPath(GeneralPath.WIND_NON_ZERO);
                current_path.moveTo(0,0);
            }else if( commands[i] == M ){
                current_path.moveTo( x[i], y[i]-ymin);

            }else if( commands[i] == C ){
                current_path.curveTo( x[i], y[i]-ymin,
                        x2[i],y2[i]-ymin,
                        x3[i], y3[i]-ymin);

            }

            i++;
        }

        //now garbage collect arrays as not needed
        shape_primitive_x2.reuse();
        shape_primitive_y.reuse();
        shape_primitives.reuse();
        shape_primitive_x3.reuse();
        shape_primitive_y3.reuse();
        shape_primitive_y2.reuse();
        shape_primitive_x.reuse();

        return new T1Glyph(cached_current_path);
    }

    /* (non-Javadoc)
     * @see org.jpedal.fonts.GlyphFactory#getGlyph()
     */
    private PdfGlyph getFXGlyph() {

        //init points
        final float[] x = shape_primitive_x.get();
        final float[] y = shape_primitive_y.get();
        final float[] x2 = shape_primitive_x2.get();
        final float[] y2 = shape_primitive_y2.get();
        final float[] x3 = shape_primitive_x3.get();
        final float[] y3 = shape_primitive_y3.get();
        final int end = shape_primitives.size() - 1;
        final int[] commands=shape_primitives.get();
        
        /**reset - we need new object as it otherwise we will pass link
         to array we then flush*/       
        shape_primitive_x2 = new Vector_Float( 1000 );
        shape_primitive_y = new Vector_Float( 1000 );
        shape_primitives = new Vector_Int( 1000 );

        shape_primitive_x3 = new Vector_Float( 1000 );
        shape_primitive_y3 = new Vector_Float( 1000 );

        shape_primitive_y2 = new Vector_Float( 1000 );
        shape_primitive_x = new Vector_Float( 1000 );

        if(javaFXSupport==null){
            return null;
        }else{
            return javaFXSupport.getGlyph(x,y,x2,y2,x3,y3,ymin,end,commands);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////
    /**
     * end a shape, storing info for later
     */
    @Override
    public final void closePath()
    {
        shape_primitives.addElement( H );

        //add empty values
        shape_primitive_x.addElement( 0 );
        shape_primitive_y.addElement( 0 );
        shape_primitive_x2.addElement( 0 );
        shape_primitive_y2.addElement( 0 );
        shape_primitive_x3.addElement( 0 );
        shape_primitive_y3.addElement( 0 );
    }
    //////////////////////////////////////////////////////////////////////////
    /**
     * add a curve to the shape
     */
    @Override
    public final void curveTo( final float x, final float y, final float x2, final float y2, final float x3, final float y3 )
    {
        shape_primitives.addElement( C );
        shape_primitive_x.addElement( x );
        shape_primitive_y.addElement( y );

        //add empty values to keep in sync
        //add empty values
        shape_primitive_x2.addElement( x2 );
        shape_primitive_y2.addElement( y2 );
        shape_primitive_x3.addElement( x3 );
        shape_primitive_y3.addElement( y3 );
    }


    //////////////////////////////////////////////////////////////////////////
    /**
     * add a line to the shape
     */
    @Override
    public final void lineTo( final float x, final float y )
    {
        shape_primitives.addElement( L );
        shape_primitive_x.addElement( x );
        shape_primitive_y.addElement( y );

        //add empty values to keep in sync
        //add empty values

        shape_primitive_x2.addElement( zero );
        shape_primitive_y2.addElement( zero );
        shape_primitive_x3.addElement( zero);
        shape_primitive_y3.addElement( zero);
    }



    /**
     * start a shape by creating a shape object
     */
    @Override
    public final void moveTo( final float x, final float y )
    {
        shape_primitives.addElement( M );
        shape_primitive_x.addElement( x );
        shape_primitive_y.addElement( y );

        //add empty values
        shape_primitive_x2.addElement( 0 );
        shape_primitive_y2.addElement( 0 );
        shape_primitive_x3.addElement( 0 );
        shape_primitive_y3.addElement( 0 );
    }

    /**
     * set ymin - ie vertical kern
     */
    @Override
    public void setYMin(final float ymin) {

        this.ymin=ymin;
        //this.ymax=ymax;

    }

    @Override
    public int getLSB() {
        return leftSideBearing;
    }

    @Override
    public boolean useFX() {
        return useFX;
    }
    
    @Override
    public PdfGlyph getGlyph(final Glyf currentGlyf, final FontFile2 fontTable, final Hmtx currentHmtx, final int idx, final float unitsPerEm, final TTVM vm, final String baseFontName) {

        if (javaFXSupport!=null) {
             return javaFXSupport.getGlyph(currentGlyf, fontTable, currentHmtx, idx, unitsPerEm, vm,baseFontName);
        }else {
            return null;
        }
    }

}
