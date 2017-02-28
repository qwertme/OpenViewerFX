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
 * SwingShape.java
 * ---------------
 */
package org.jpedal.objects;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.Serializable;
import javafx.scene.shape.Path;

import org.jpedal.parser.Cmd;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Float;
import org.jpedal.utils.repositories.Vector_Int;

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
public class SwingShape implements Serializable, PdfShape
{

    /**used to stop lots of huge, complex shapes.
     * Note we DO NOT reset as we reuse this object and
     * it stores cumulative count
     */
    int complexClipCount;
    
    private static final int initSize=1000;

    /**we tell user we have not used some shapes only ONCE*/
    private final Vector_Float shape_primitive_x2 = new Vector_Float( initSize );
    private final Vector_Float shape_primitive_y = new Vector_Float( initSize );

    /**store shape currently being assembled*/
    private final Vector_Int shape_primitives = new Vector_Int( initSize );

    /**type of winding rule used to draw shape*/
    private int winding_rule = GeneralPath.WIND_NON_ZERO;
    private final Vector_Float shape_primitive_x3 = new Vector_Float( initSize );
    private final Vector_Float shape_primitive_y3 = new Vector_Float( initSize );

    /**used when trying to choose which shapes to use to test furniture*/
    private final Vector_Float shape_primitive_y2 = new Vector_Float( initSize );
    private final Vector_Float shape_primitive_x = new Vector_Float( initSize );
    private static final int H = 3;
    private static final int L = 2;
    private static final int V = 6;

    /**flags for commands used*/
    private static final int M = 1;
    private static final int Y = 4;
    private static final int C = 5;


    /**flag to show if image is for clip*/
    private boolean isClip;

    /**flag to show if S.java needs to adjust lineWidth because we have modified the shape*/
    private boolean adjustLineWidth;

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    private boolean isClosed;

    /////////////////////////////////////////////////////////////////////////
    /**
     * end a shape, storing info for later
     */
    @Override
    public final void closeShape()
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
    public final void addBezierCurveC( final float x, final float y, final float x2, final float y2, final float x3, final float y3 )
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
     * set winding rule - non zero
     */
    @Override
    public final void setNONZEROWindingRule()
    {
        winding_rule = GeneralPath.WIND_NON_ZERO;
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
        shape_primitive_x2.addElement( 0 );
        shape_primitive_y2.addElement( 0 );
        shape_primitive_x3.addElement( 0 );
        shape_primitive_y3.addElement( 0 );
    }
    ///////////////////////////////////////////////////////////////////////////
    /**
     * add a curve to the shape
     */
    @Override
    public final void addBezierCurveV( final float x2, final float y2, final float x3, final float y3 )
    {
        shape_primitives.addElement( V );
        shape_primitive_x.addElement( 200 );
        shape_primitive_y.addElement( 200 );

        //add empty values to keep in sync
        //add empty values
        shape_primitive_x2.addElement( x2 );
        shape_primitive_y2.addElement( y2 );
        shape_primitive_x3.addElement( x3 );
        shape_primitive_y3.addElement( y3 );
    }
    //////////////////////////////////////////////////////////////////////////
    /**
     * turn shape commands into a Shape object, storing info for later. Has to
     * be done this way because we need the winding rule to initialise the shape
     * in Java, and it could be set anywhere in the command stream
     */
    @Override
    public final Shape generateShapeFromPath( final float[][] CTM, final float thickness, final int cmd, final int type){

        isClosed=false; //will be set to true if closed

        boolean is_clip=this.isClip;
        if(cmd==Cmd.n) {
            is_clip = false;
        }
        
        //create the shape - we have to do it this way
        //because we get the WINDING RULE last and we need it
        //to initialise the shape
        GeneralPath current_path = null;
        Area current_area = null;
        final Shape current_shape;
        adjustLineWidth = false;

        //init points
        final float[] x = shape_primitive_x.get();
        final float[] y = shape_primitive_y.get();
        final float[] x2 = shape_primitive_x2.get();
        final float[] y2 = shape_primitive_y2.get();
        final float[] x3 = shape_primitive_x3.get();
        final float[] y3 = shape_primitive_y3.get();
        final int[] command=shape_primitives.get();

        //float lx=0,ly=0;
        //float xs=0,ys=0;
        final int end = shape_primitives.size() - 1;
        
        //code to fix rounding issue in clipping if rect and boundary just over 0.5
        //tweaked for abacus/Zebra_als_PDF_OK.pdf 
        if(end==6 && cmd== Cmd.B && thickness>=0.9f){
            for(int aa=0;aa<8;aa++){
                final float diff=x[aa]-(int)x[aa];
                
                if(diff>0.5f){
                    x[aa]=(int)x[aa]-1f;
                }
            }
        }
        
        //used to debug
        final boolean show = false;
        final boolean debug= false;

        //loop through commands and add to shape
        for(int i = 0; i < end; i++){
            if( current_path == null ){
                current_path = new GeneralPath( winding_rule );
                current_path.moveTo( x[i], y[i] );
                //lx=x[i];
                //ly=y[i];
                //xs=lx;
                //ys=ly;

                if( show) {
                    LogWriter.writeLog("==START=" + x[i] + ' ' + y[i]);
                }
            }

            //only used to create clips
            if(command[i]== H){

                isClosed=true;

                current_path.closePath();
                if(is_clip){

                    //current_path.lineTo(xs,ys);
                    //current_path.closePath();
                    if(show) {
                        LogWriter.writeLog("==H\n\n" + current_area + ' ' + current_path.getBounds2D() + ' ' + new Area(current_path).getBounds2D());
                    }

                    if( current_area == null ){
                        current_area = new Area( current_path );

                        //trap for apparent bug in Java where small paths create a 0 size Area
                        if((current_area.getBounds2D().getWidth()<=0.0)||
                                (current_area.getBounds2D().getHeight()<=0.0)) {
                            current_area = new Area(current_path.getBounds2D());
                        }

                    }else {
                        current_area.add(new Area(current_path));
                    }

                    current_path = null;
                }else{
                    if(show) {
                        LogWriter.writeLog("close shape " + command[i] + " i=" + i);
                    }

                }
            }

            if( command[i]== L ){

                current_path.lineTo( x[i], y[i] );

                //lx=x[i];
                //ly=y[i];

                if(show) {
                    LogWriter.writeLog("==L" + x[i] + ',' + y[i] + "  ");
                }

            }else if( command[i] == M ){
                current_path.moveTo( x[i], y[i] );
                //lx=x[i];
                //ly=y[i];
                if(show) {
                    LogWriter.writeLog("==M" + x[i] + ',' + y[i] + "  ");
                }
            }else{
                //cubic curves which use 2 control points
                if( command[i] == Y ){
                    if(show) {
                        LogWriter.writeLog("==Y " + x[i] + ' ' + y[i] + ' ' + x3[i] + ' ' + y3[i] + ' ' + x3[i] + ' ' + y3[i]);
                    }

                    current_path.curveTo( x[i], y[i], x3[i], y3[i], x3[i], y3[i] );
                    //lx=x3[i];
                    //ly=y3[i];

                }else if( command[i] == C ){
                    if(show) {
                        LogWriter.writeLog("==C " + x[i] + ' ' + y[i] + ' ' + x2[i] + ' ' + y2[i] + ' ' + x3[i] + ' ' + y3[i]);
                    }

                    current_path.curveTo( x[i], y[i], x2[i], y2[i], x3[i], y3[i] );
                    //lx=x3[i];
                    //ly=y3[i];

                }else if( command[i] == V ){
                    final float c_x = (float)current_path.getCurrentPoint().getX();
                    final float c_y = (float)current_path.getCurrentPoint().getY();
                    if(show) {
                        LogWriter.writeLog("==v " + c_x + ',' + c_y + ',' + x2[i] + ',' + y2[i] + ',' + x3[i] + ',' + y3[i]);
                    }

                    current_path.curveTo( c_x, c_y, x2[i], y2[i], x3[i], y3[i] );
                    //lx=x3[i];
                    //ly=y3[i];
                }
            }

            if(debug){
                try{
                    final java.awt.image.BufferedImage img=new java.awt.image.BufferedImage(700,700, java.awt.image.BufferedImage.TYPE_INT_ARGB);

                    final Graphics2D gg2= img.createGraphics();
                    gg2.setPaint(Color.RED);
                    gg2.translate(current_path.getBounds().width+10,current_path.getBounds().height+10);
                    gg2.draw(current_path);

                    org.jpedal.gui.ShowGUIMessage.showGUIMessage("path",img,"path "+current_path.getBounds());
                }catch(final Exception e){
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }/***/
        }

         
        //second part hack for artus file with thin line
        if((current_path!=null)&&(current_path.getBounds().getHeight()==0 || (thickness>0.8 && thickness<0.9 && current_path.getBounds2D().getHeight()<0.1f))){


            if(current_path.getBounds2D().getWidth()==0 && current_path.getBounds2D().getHeight()==0){
                //ignore this case
            }else if(thickness>1 && current_path.getBounds2D().getWidth()<=1){ //make <1 into <=1
                current_path.moveTo(0,-thickness/2);
                current_path.lineTo(0,thickness/2);
                adjustLineWidth = true;//If we are doing this, then we need to remove the large lineWidth that's set. (In S.java)
            }else {
                current_path.moveTo(0, 1);//@Mark 13908
            }
        }

        if((current_path!=null)&&(current_path.getBounds().getWidth()==0)) {
            current_path.moveTo(1, 0);//@Mark 13908
        }

        //transform matrix only if needed
        if((CTM[0][0] == (float)1.0)&&(CTM[1][0] == (float)0.0)&&
                (CTM[2][0] == (float)0.0)&&(CTM[0][1] == (float)0.0)&&
                (CTM[1][1] == (float)1.0)&&(CTM[2][1] == (float)0.0)&&
                (CTM[0][2] == (float)0.0)&&(CTM[1][2] == (float)0.0)&&(CTM[2][2] == (float)1.0)){
            //don't waste time if not needed
        }else{
            final AffineTransform CTM_transform = new AffineTransform( CTM[0][0], CTM[0][1], CTM[1][0], CTM[1][1], CTM[2][0], CTM[2][1]);

            //apply CTM alterations
            if( current_path != null ){

                //transform
                current_path.transform( CTM_transform );
                //if(CTM[0][0]==0 && CTM[1][1]==0 && CTM[0][1]<0 && CTM[1][0]>0){
                //    current_path.transform(AffineTransform.getTranslateInstance(0,current_path.getBounds().height/CTM[0][1]));
                //System.out.println("transforms "+CTM_transform+" "+current_path.getBounds());
                //}                    
            }else if( current_area != null ) {
                current_area.transform(CTM_transform);
            }
        }

        /**
         * fix for single rotated lines with thickness
         */
        if(current_path!=null && CTM[0][0]==1 && CTM[1][1]==-1 && current_path.getBounds().height==1 && thickness>10 ){

            final Rectangle currentBounds=current_path.getBounds();
            current_path = new GeneralPath( winding_rule );
            current_path.moveTo(currentBounds.x,currentBounds.y-thickness/2);
            current_path.lineTo(currentBounds.x,currentBounds.y+thickness/2);

            current_path.closePath();

        }

        //set to current or clip
        if(!is_clip){
            if( current_area == null ) {
                current_shape = current_path;
            } else {
                current_shape = current_area;
            }
        }else {
            current_shape = current_area;
        }


        //track complex clips
        if(cmd==Cmd.n && getSegmentCount()>2500){
            complexClipCount++;
        }

        return current_shape;
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

        //delete lines for grouping over boxes
    }

    /**
     * add a curve to the shape
     */
    @Override
    public final void addBezierCurveY( final float x, final float y, final float x3, final float y3 )
    {
        shape_primitives.addElement( Y );
        shape_primitive_x.addElement( x );
        shape_primitive_y.addElement( y );

        //add empty values to keep in sync
        //add empty values
        shape_primitive_x2.addElement( 0 );
        shape_primitive_y2.addElement( 0 );
        shape_primitive_x3.addElement( x3 );
        shape_primitive_y3.addElement( y3 );
    }

    /**
     * reset path to empty
     */
    @Override
    public final void resetPath()
    {
        //reset the store
        shape_primitives.clear();
        shape_primitive_x.clear();
        shape_primitive_y.clear();
        shape_primitive_x2.clear();
        shape_primitive_y2.clear();
        shape_primitive_x3.clear();
        shape_primitive_y3.clear();

        //and reset winding rule
        winding_rule = GeneralPath.WIND_NON_ZERO;
    }
    ///////////////////////////////////////////////////////////////////////////
    /**
     * set winding rule - even odd
     */
    @Override
    public final void setEVENODDWindingRule()
    {
        winding_rule = GeneralPath.WIND_EVEN_ODD;
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

        if( shape_primitives==null) {
            return 0;
        } else{
            return shape_primitives.size() - 1;
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

    /**
     * Allow S.java to detect that we have modified the shape, and therefore the lineWidth needs to be adjusted.
     * @return
     */
    @Override
    public boolean adjustLineWidth() {
        return adjustLineWidth;
    }

    @Override
    public Path getPath() {
        return null;
    }
}
