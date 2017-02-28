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
 * JavaFXShape.java
 * ---------------
 */
package org.jpedal.objects;

import java.io.Serializable;
import javafx.collections.ObservableList;

import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;


/**
 * <p>
 * defines the current shape which is created by command stream
 * </p>
 * <p>
 * <b>This class is NOT part of the API </b>
 * </p>. Shapes can be drawn onto pdf or used as a clip on other
 * image/shape/text. Shape is built up by storing commands and then turning
 * these commands into a shape. Has to be done this way as Winding rule is not
 * necessarily declared at start.
 */
public class JavaFXShape implements Serializable, PdfShape
{

    /**used to stop lots of huge, complex shapes.
     * Note we DO NOT reset as we reuse this object and
     * it stores cumulative count
     */
    int complexClipCount;

    /**flag to show if image is for clip*/
    private boolean isClip;
    
    private Path path = new Path();
    
    ObservableList<PathElement> elements = path.getElements();
        
    private FillRule windingRule;
    
    // Stores the previously moved to path (Used for addBezierCurveV())
    private final float[] currentPos = new float[2];

    private boolean isClosed;

    /////////////////////////////////////////////////////////////////////////
    /**
     * end a shape, storing info for later
     */
    @Override
    public final void closeShape()
    {        
        elements.add(new ClosePath());
    }
    //////////////////////////////////////////////////////////////////////////
    /**
     * add a curve to the shape
     */
    @Override
    public final void addBezierCurveC( final float x, final float y, final float x2, final float y2, final float x3, final float y3 )
    {
        elements.add(new CubicCurveTo(x,y,x2,y2,x3,y3));
        currentPos[0] = x3;
        currentPos[1] = y3;
    }
    //////////////////////////////////////////////////////////////////////////
    /**
     * set winding rule - non zero
     */
    @Override
    public final void setNONZEROWindingRule()
    {
        setWindingRule(FillRule.NON_ZERO);
    }
    //////////////////////////////////////////////////////////////////////////
    /**
     * add a line to the shape
     */
    @Override
    public final void lineTo( final float x, final float y )
    {
        elements.add(new LineTo(x,y));
        currentPos[0] = x;
        currentPos[1] = y;
    }
    ///////////////////////////////////////////////////////////////////////////
    /**
     * add a curve to the shape
     */
    @Override
    public final void addBezierCurveV( final float x2, final float y2, final float x3, final float y3 )
    {
        elements.add(new CubicCurveTo(currentPos[0], currentPos[1],x2,y2,x3,y3));

        currentPos[0] = x3;
        currentPos[1] = y3;
    }
    //////////////////////////////////////////////////////////////////////////
    /**
     * turn shape commands into a Shape object, storing info for later. Has to
     * be done this way because we need the winding rule to initialise the shape
     * in Java, and it could be set anywhere in the command stream
     */
    @Override
    public final java.awt.Shape generateShapeFromPath( final float[][] CTM, final float thickness, final int cmd, final int type){

        //may need to code this in if we ever implement
       // isClosed=false; // set in code if H called

        // returns an empty path as null breaks stuff.
        return new java.awt.geom.GeneralPath();
        
        /**
         * Code kept in case it needs to be used in future:
         */
        
        //used to debug
//        final boolean show = false;
//        final boolean debug= false;

        //transform matrix only if needed
//        if((CTM[0][0] == (float)1.0)&&(CTM[1][0] == (float)0.0)&&
//                (CTM[2][0] == (float)0.0)&&(CTM[0][1] == (float)0.0)&&
//                (CTM[1][1] == (float)1.0)&&(CTM[2][1] == (float)0.0)&&
//                (CTM[0][2] == (float)0.0)&&(CTM[1][2] == (float)0.0)&&(CTM[2][2] == (float)1.0)){
//            //don't waste time if not needed
//        }else{
//            AffineTransform CTM_transform = new AffineTransform( CTM[0][0], CTM[0][1], CTM[1][0], CTM[1][1], CTM[2][0], CTM[2][1]);
//
//            //apply CTM alterations
//            if( current_path != null ){
//
//                //transform
//                current_path.transform( CTM_transform );
//                //if(CTM[0][0]==0 && CTM[1][1]==0 && CTM[0][1]<0 && CTM[1][0]>0){
//                //    current_path.transform(AffineTransform.getTranslateInstance(0,current_path.getBounds().height/CTM[0][1]));
//                //System.out.println("transforms "+CTM_transform+" "+current_path.getBounds());
//                //}                    
//            }else if( current_area != null )
//                current_area.transform( CTM_transform );
//        }

        /**
         * fix for single rotated lines with thickness
         */
//        if(current_path!=null && CTM[0][0]==1 && CTM[1][1]==-1 && current_path.getBounds().height==1 && thickness>10 ){
//
//            Rectangle currentBounds=current_path.getBounds();
//            current_path = new GeneralPath( winding_rule );
//            current_path.moveTo(currentBounds.x,currentBounds.y-thickness/2);
//            current_path.lineTo(currentBounds.x,currentBounds.y+thickness/2);
//
//            current_path.closePath();
//
//        }
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * add a rectangle to set of shapes
     */
    @Override
    public final void appendRectangle( final float x, final float y, final float w, final float h )
    {
        moveTo( x, y );
        lineTo( x + w, y );
        lineTo( x + w, y + h );
        lineTo( x, y + h );
        lineTo( x, y );
        closeShape();
    }
    
    //////////////////////////////////////////////////////////////////////////
    /**
     * start a shape by creating a shape object
     */
    @Override
    public final void moveTo( final float x, final float y )
    {
        elements.add(new MoveTo(x,y));
        currentPos[0] = x;
        currentPos[1] = y;
    }

    /**
     * add a curve to the shape
     */
    @Override
    public final void addBezierCurveY( final float x, final float y, final float x3, final float y3 )
    {
        elements.add(new QuadCurveTo(x,y,x3,y3));
        currentPos[0] = x3;
        currentPos[1] = y3;
    }

    /**
     * reset path to empty
     */
    @Override
    public final void resetPath()
    { 
        path = new Path();
        elements = path.getElements();
        
        windingRule = FillRule.NON_ZERO;
    }
    ///////////////////////////////////////////////////////////////////////////
    /**
     * set winding rule - even odd
     */
    @Override
    public final void setEVENODDWindingRule()
    {
        setWindingRule(FillRule.EVEN_ODD);
    }
    
    public final void setWindingRule(final FillRule rule){
        windingRule = rule;
        path.setFillRule(windingRule);
    }

    /**
     * show the shape segments for debugging
     *
     static final private void showShape( Shape current_shape )
     {
     PathIterator xx = current_shape.getPathIterator( null );
     double[] coords = new double[6];
     while( xx.isDone() == false )
     {
     int type = xx.currentSegment( coords );
     xx.next();
     switch( type )
     {
     case PathIterator.SEG_MOVETO:
     LogWriter.writeLog( "MoveTo" + coords[0] + ' ' + coords[1] );
     if( ( coords[0] == 0 ) & ( coords[1] == 0 ) )
     LogWriter.writeLog( "xxx" );
     break;

     case PathIterator.SEG_LINETO:
     LogWriter.writeLog( "LineTo" + coords[0] + ' ' + coords[1] );
     if( ( coords[0] == 0 ) & ( coords[1] == 0 ) )
     LogWriter.writeLog( "xxx" );
     break;

     case PathIterator.SEG_CLOSE:
     LogWriter.writeLog( "CLOSE" );
     break;

     default:
     LogWriter.writeLog( "Other" + coords[0] + ' ' + coords[1] );
     break;
     }
     }
     }/**/

    /**
     * number of segments in current shape (0 if no shape or none)
     */
    @Override
    public int getSegmentCount() {
        if(path == null){
            return 0;
        }else {
            return elements.size();
        }
    }

    @Override
    public void setClip(final boolean b) {
        this.isClip=b;
    }

    @Override
    public boolean isClip() {
        return isClip;  
    }
    
    @Override
    public int getComplexClipCount() {
        return complexClipCount;
    }

    @Override
    public Path getPath() {    
//        if(DEBUG)
//            showPath();
        return path;
    }

    @Override
    public boolean adjustLineWidth() {
        throw new RuntimeException("JavaFX should not call JavaFXShape.adjustLineWidth()");//Not required in JavaFXShape (SwingShape only)
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * ==== DEBUG CODE ====
     * Prints out all the draw commands in the path
     */
//    public void showPath(){
//        System.out.println("==== Printing Path ====");
//        System.out.println("Elements: " + path.getElements().size());
//        System.out.println("isClip: " + isClip);
//        for(PathElement p: path.getElements()){
//            
//                if(p.getClass().getCanonicalName().toLowerCase().equals("javafx.scene.shape.moveto")){
//                    System.out.println("MoveTo: " + Math.round(((MoveTo)p).getX()) + ", " + Math.round(((MoveTo)p).getY()));
//                }else if( p.getClass().getCanonicalName().toLowerCase().equals("javafx.scene.shape.lineto")){
//                    System.out.println("LineTo: " + Math.round(((LineTo)p).getX()) + ", " + Math.round(((LineTo)p).getX()));
//                }else if( p.getClass().getCanonicalName().toLowerCase().equals("javafx.scene.shape.cubiccurveto")){
//                    System.out.println("CubicCurveTo: " + Math.round(((CubicCurveTo)p).getX())+ ", " +Math.round(((CubicCurveTo)p).getY())+ ": x1 " + Math.round(((CubicCurveTo)p).getControlX1())+ ", y1 " 
//                            +Math.round(((CubicCurveTo)p).getControlY1())+ ", x2 " + Math.round(((CubicCurveTo)p).getControlX2())+ ", y2 " + + Math.round(((CubicCurveTo)p).getControlY2()));
//                }else if( p.getClass().getCanonicalName().toLowerCase().equals("javafx.scene.shape.closepath")){
//                    System.out.println("ClosePath");
//                }
//        }
//        System.out.println("=========");
//    }
    
}
