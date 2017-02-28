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
 * ImageTransformer.java
 * ---------------
 */
package org.jpedal.images;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import org.jpedal.color.ColorSpaces;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.GraphicsState;
import org.jpedal.utils.LogWriter;

/**
 * class to shrink and clip an extracted image
 * On reparse just calculates co-ords
 */
public class ImageTransformer {

	/**holds the actual image*/
	private BufferedImage current_image;

	/**matrices used in transformation*/
	private final float[][] Trm, CTM;

	/**image co-ords*/
	private int i_x, i_y, i_w, i_h;

	/**
	 * pass in image information and apply transformation matrix
	 * to image
	 */
	public ImageTransformer(
		final GraphicsState current_graphics_state,
		final BufferedImage new_image) {

		//save global values
		this.current_image = new_image;
		final int w;
        final int h;

        w = current_image.getWidth(); //raw width
		h = current_image.getHeight(); //raw height

		CTM = current_graphics_state.CTM; //local copy of CTM

        //build transformation matrix by hand to avoid errors in rounding
		Trm = new float[3][3];
		Trm[0][0] = (CTM[0][0] / w);
		Trm[0][1] = -(CTM[0][1] / w);
		Trm[0][2] = 0;
		Trm[1][0] = -(CTM[1][0] / h);
		Trm[1][1] = (CTM[1][1] / h);
		Trm[1][2] = 0;
		Trm[2][0] = CTM[2][0];
		Trm[2][1] = CTM[2][1];
		Trm[2][2] = 1;

		//round numbers if close to 1
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				if ((Trm[x][y] > .99) & (Trm[x][y] < 1)) {
                    Trm[x][y] = 1;
                }
			}
		}


		scale(w,h);

		completeImage();
	}

	private void scale(final int w, final int h){

        /**
         * transform the image only if needed
         */
        if (Trm[0][0] != 1.0 || Trm[1][1] != 1.0 || Trm[0][1] != 0.0 || Trm[1][0] != 0.0) {

            //workout transformation for the image
            AffineTransform image_at =new AffineTransform(Trm[0][0],Trm[0][1],Trm[1][0],Trm[1][1],0,0);

            //apply it to the shape first so we can align
            final Area r =new Area(new Rectangle(0,0,w,h));
            r.transform(image_at);
            
            //make sure it fits onto image (must start at 0,0)
            final double ny = r.getBounds2D().getY();
            final double nx = r.getBounds2D().getX();


            float a = Trm[0][0];
            float b=Trm[0][1];
            float c=Trm[1][0];
            float d=Trm[1][1];
            image_at =new AffineTransform(a,b,c,d,-nx,-ny);

            /**
             * avoid upscaling
             */
            if(a<0) {
                a = -a;
            }
            if(b<0) {
                b = -b;
            }
            if(c<0) {
                c = -c;
            }
            if(d<0) {
                d = -d;
            }

            //avoid large figures
            if(a>5 || b>5 || c>5 || d>5) {
                return;
            }

            //Create the affine operation.
            //ColorSpaces.hints causes single lines to vanish);
            AffineTransformOp invert;


            if(w>1 && h>1){

                //fix image inversion if matrix (0,x,-y,0)
                if(CTM[0][0]==0 && CTM[1][1]==0 && CTM[0][1]>0 && CTM[1][0]<0){
                    image_at.scale(-1,1);
                    image_at.translate(-current_image.getWidth(),0);
                }
                
                invert = new AffineTransformOp(image_at,  ColorSpaces.hints);

            }else{

                //allow for line with changing values
                boolean isSolid=true;

                if(h==1){
                    //test all pixels set so we can keep a solid line
                    final Raster ras=current_image.getRaster();
                    final int bands=ras.getNumBands();
                    final int width=ras.getWidth();
                    final int[] elements=new int[(width*bands)+1];

                    ras.getPixels(0,0,width,1, elements);
                    for(int j=0;j<bands;j++){
                        final int first=elements[0];
                        for(int i=1;i<width;i++){
                            if(elements[i*j]!=first){
                                isSolid=false;
                                i=width;
                                j=bands;
                            }
                        }
                    }
                }

                if(isSolid) {
                    invert = new AffineTransformOp(image_at, null);
                } else {
                    invert = new AffineTransformOp(image_at, ColorSpaces.hints);
                }
            }

            //if there is a rotation make image ARGB so we can clip
            if (CTM[1][0] != 0 || CTM[0][1] != 0) {
                current_image = ColorSpaceConvertor.convertToARGB(current_image);
            }

            //scale image to produce final version
            scaleImage(h, image_at, r, invert);

        }
    }

    private void scaleImage(int h, AffineTransform image_at, Area r, AffineTransformOp invert) {

        final BufferedImage destImage;

//                if(r.getBounds2D().getWidth() < ((double)w)-.5){
        int newW=(int)(r.getBounds2D().getWidth());
        // Rounding up height fixes some files like shading/Reporttyp_21.pdf
        int newH=(int)(r.getBounds2D().getHeight()+.7);

        if(newH < 1) {
            newH = 1;
        }
        if(newW < 1) {
            newW = 1;
        }

        destImage=new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        /**if not sheer/rotate, then bicubic*/
        if(h>1){

            boolean failed=false;
            //allow for odd behaviour on some files
            try{
                invert.filter(current_image,destImage);
                current_image=destImage;
            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //

                failed=true;
            }
            if(failed){
                try{
                    invert = new AffineTransformOp(image_at,null);
                    current_image = invert.filter(current_image,null);
                }catch(final Exception e){
                    //
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
	 * complete image
	 */
	private void completeImage(){

		/**
		 * now workout correct screen co-ords allow for rotation
		 *
		if ((CTM[1][0] == 0) &( (CTM[0][1] == 0))){
			i_w =(int) Math.sqrt((CTM[0][0] * CTM[0][0]) + (CTM[0][1] * CTM[0][1]));
			i_h =(int) Math.sqrt((CTM[1][1] * CTM[1][1]) + (CTM[1][0] * CTM[1][0]));

		}else{
			i_h =(int) Math.sqrt((CTM[0][0] * CTM[0][0]) + (CTM[0][1] * CTM[0][1]));
			i_w =(int) Math.sqrt((CTM[1][1] * CTM[1][1]) + (CTM[1][0] * CTM[1][0]));
		}

		if (CTM[1][0] < 0)
			i_x = (int) (CTM[2][0] + CTM[1][0]);
		else
			i_x = (int) CTM[2][0];

		if (CTM[0][1] < 0) {
			i_y = (int) (CTM[2][1] + CTM[0][1]);
		} else {
			i_y = (int) CTM[2][1];
		}

		//alter to allow for back to front or reversed
		if (CTM[1][1] < 0)
			i_y = i_y - i_h;

		if (CTM[0][0] < 0)
			i_x = i_x - i_w;
*/
		calcCoordinates();
	}

	/**
	 * workout correct screen co-ords allow for rotation
	 */
	private void calcCoordinates(){

        if (CTM[1][0] == 0 && CTM[0][1] == 0){

			i_x = (int) CTM[2][0];
			i_y = (int) CTM[2][1];

			i_w =(int) CTM[0][0];
			i_h =(int) CTM[1][1];
			if(i_w<0) {
                i_w = -i_w;
            }

			if(i_h<0) {
                i_h = -i_h;
            }

		}else{ //some rotation/skew
			i_w=(int) (Math.sqrt((CTM[0][0] * CTM[0][0]) + (CTM[0][1] * CTM[0][1])));
			i_h =(int) (Math.sqrt((CTM[1][1] * CTM[1][1]) + (CTM[1][0] * CTM[1][0])));

			if(CTM[1][0]>0 && CTM[0][1]<0){
				i_x = (int) (CTM[2][0]);
				i_y = (int) (CTM[2][1]+CTM[0][1]);
				//System.err.println("AA "+i_w+" "+i_h);

			}else if(CTM[1][0]<0 && CTM[0][1]>0){
				i_x = (int) (CTM[2][0]+CTM[1][0]);
				i_y = (int) (CTM[2][1]);
				//System.err.println("BB "+i_w+" "+i_h);
                
            }else if(CTM[1][0]>0 && CTM[0][1]>0){
				i_x = (int) (CTM[2][0]);
				i_y = (int) (CTM[2][1]);
				//System.err.println("CC "+i_w+" "+i_h);
			}else{
				//System.err.println("DD "+i_w+" "+i_h);
				i_x = (int) (CTM[2][0]);
				i_y =(int) (CTM[2][1]);
			}

		}

		//alter to allow for back to front or reversed
		if ( CTM[1][1]< 0) {
            i_y -= i_h;
        }
		if ( CTM[0][0]< 0) {
            i_x -= i_w;
        }

	}

	/**
	 * get y of image (x1,y1 is top left)
	 */
    public final int getImageY() {
		return i_y;
	}

	/**
	 * get image
	 */
    public final BufferedImage getImage() {
		return current_image;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get width of image
	 */
    public final int getImageW() {
		return i_w;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get height of image
	 */
    public final int getImageH() {
		return i_h;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get X of image (x,y is top left)
	 */
    public final int getImageX() {
		return i_x;
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * clip the image
	 */
    public final void clipImage(final Area current_shape) {

		//create a copy of clip (so we don't alter clip)
		final Area final_clip = (Area) current_shape.clone();

		//actual size so we can trap any rounding error
		final int image_w = current_image.getWidth();
		final int image_h = current_image.getHeight();

		//shape of final image
		final double shape_x = final_clip.getBounds2D().getX();
		final double shape_y = final_clip.getBounds2D().getY();
		final double shape_h = final_clip.getBounds2D().getHeight();
		final double d_y = (image_h - shape_h);
		final AffineTransform upside_down = new AffineTransform();
		upside_down.translate(-shape_x, -shape_y); //center
		upside_down.scale(1, -1); //reflect in x axis
		upside_down.translate(shape_x, - (shape_y + shape_h));
		final_clip.transform(upside_down);

		//line up to shape
		final AffineTransform align_clip = new AffineTransform();

		//if not working at 72 dpi, alter clip to fit
		align_clip.translate(-i_x, i_y + d_y);
		final_clip.transform(align_clip);

		//co-ords of transformed shape
		//reset sizes to remove area clipped
		double x = final_clip.getBounds2D().getX();
		double y = final_clip.getBounds2D().getY();
		double w = final_clip.getBounds2D().getWidth();
		double h = final_clip.getBounds2D().getHeight();

		//get type of image used
		int image_type = current_image.getType();

		//set type so ICC and RGB uses ARGB
		if ((image_type == 0)) {
            image_type = BufferedImage.TYPE_INT_ARGB; //
        } else if ((image_type == BufferedImage.TYPE_INT_RGB)) {
            image_type = BufferedImage.TYPE_INT_ARGB; //
        }

		//draw image onto graphic (with clip) and then re-extract
		final BufferedImage offscreen =
			new BufferedImage(image_w, image_h, image_type);
		//image of  'canvas'
		final Graphics2D image_g2 = offscreen.createGraphics(); //g2 of canvas

		//if not transparent make background white
		if (!offscreen.getColorModel().hasAlpha()) {
			image_g2.setBackground(Color.white);
			image_g2.fill(new Rectangle(0, 0, image_w, image_h));
		}

		image_g2.setClip(final_clip);

		try {
			//redraw image clipped and extract as rectangular shape
			image_g2.drawImage(current_image, 0, 0,null);
		} catch (final Exception e) {
			LogWriter.writeLog("Exception " + e + " plotting clipping image");
		}

		//get image (now clipped )

		//check for rounding errors
		if (y < 0) {
			h += y;
			y = 0;
		}
		if (x < 0) {
			w += x;
			x = 0;
		}
		if (w > image_w) {
            w = image_w;
        }
		if (h > image_h) {
            h = image_h;
        }
		if (y + h > image_h) {
            h = image_h - y;
        }
		if (x + w > image_w) {
            w = image_w - x;
        }

		try {
			current_image = offscreen.getSubimage((int)x, (int)y, (int)(w), (int)(h));
		} catch (final Exception e) {
			LogWriter.writeLog("Exception " + e + " extracting clipped image with values x="+x+" y="+y+" w="+w+" h="+h+" from image ");
		}

		//work out new co-ords from shape and current
		final double x1;
        final double y1;
        if (i_x > shape_x) {
            x1 = i_x;
        } else {
            x1 = shape_x;
        }
		if (i_y > shape_y) {
            y1 = i_y;
        } else {
            y1 = shape_y;
        }

		i_x = (int) (x1);
		i_y = (int) (y1);
		i_w = (int) w;
		i_h = (int) h;

	}
}
