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
 * ExampleImageDrawOnScreenHandler.java
 * ---------------
 */
package org.jpedal.examples.handlers;

import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.io.ObjectStore;
import org.jpedal.color.ColorSpaces;

import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.*;

/**
 * example code to plugin external image handler. Code to enable commented out in Viewer
 */
public class ExampleImageDrawOnScreenHandler implements org.jpedal.external.ImageHandler {

    //tell JPedal if it ignores its own Image code or not
    @Override
    public boolean alwaysIgnoreGenericHandler() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }//pass in raw data for image handling - if valid image returned it will be used.
    //if alwaysIgnoreGenericHandler() is true JPedal code always ignored. If false, JPedal code used if null

    @Override
    public BufferedImage processImageData(final GraphicsState gs, final PdfObject XObject) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean imageHasBeenScaled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean drawImageOnscreen(BufferedImage image, final int optionsApplied, AffineTransform upside_down,
                                     final String currentImageFile, final Graphics2D g2, final boolean renderDirect,
                                     final ObjectStore objectStoreRef, final boolean isPrinting) {

        //this is the draw code from DynamicVectorRenderer as at 11th June 2007

        final double[] values=new double[6];
        upside_down.getMatrix(values);

        final boolean isSlightlyRotated=(values[0]*values[1]!=0)||(values[2]*values[3]!=0);

        //accelerate large bw images non-rotated
        //accelerate large bw images non-rotated (use for all images for moment)
				if(isSlightlyRotated || image.getWidth()<800 || renderDirect){ //image.getType()!=12 || CTM[0][0]<0 || CTM[1][1]<0 || CTM[1][0]<0 || CTM[0][1]<0)
                    g2.drawImage(image,upside_down,null);
                }else{ //speedup large straightforward images

                    double dy=0,dx=0;

                    //if already turned, tweak transform
					
                    //int count=values.length;
                    //for(int jj=0;jj<count;jj++)
                    //System.out.println(jj+"=="+values[jj]);

//					System.out.println(image.getWidth());
//					System.out.println(image.getHeight());
//					System.out.println(values[4]*image.getHeight()/image.getWidth());

                    upside_down=new AffineTransform(values);
                    
					boolean imageProcessed=true;

						try{
							final AffineTransformOp invert =new AffineTransformOp(upside_down,ColorSpaces.hints);

							image=invert.filter(image,null);
						}catch(final Exception ee){
							imageProcessed=false;
							ee.printStackTrace();
						}catch(final Error err){
							imageProcessed=false;

							//
						}
					
					if(imageProcessed){

                        Shape rawClip=null;

                        if(isPrinting && dy==0){ //adjust to fit
                            final double[] affValues=new double[6];
                            g2.getTransform().getMatrix(affValues);

                            //for(int i=0;i<6;i++)
                            //System.out.println(i+"="+affValues[i]);

                            dx=affValues[4]/affValues[0];
                            if(dx>0) {
                                dx = -dx;
                            }

                            dy=affValues[5]/affValues[3];

                            if(dy>0) {
                                dy = -dy;
                            }

                            dy=-(dy+image.getHeight());


                        }

                        //stop part pixels causing black lines
                        if(dy!=0){
                            rawClip=g2.getClip();

                            final int xDiff;
                            final double xScale=g2.getTransform().getScaleX();
                            if(xScale<1) {
                                xDiff = (int) (1 / xScale);
                            } else {
                                xDiff = (int) (xScale + 0.5d);
                            }
                            final int yDiff;
                            final double yScale=g2.getTransform().getScaleY();
                            if(yScale<1) {
                                yDiff = (int) (1 / yScale);
                            } else {
                                yDiff = (int) (yScale + 0.5d);
                            }

                            g2.clipRect((int)dx,(int)(dy+1.5),image.getWidth()-xDiff,image.getHeight()-yDiff);
                            
                        }
                        g2.drawImage(image,(int)dx, (int) dy,null);

                        //put it back
                        if(rawClip!=null) {
                            g2.setClip(rawClip);
                        }
                    }else {
                        g2.drawImage(image, upside_down, null);
                    }
				
        }

        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
