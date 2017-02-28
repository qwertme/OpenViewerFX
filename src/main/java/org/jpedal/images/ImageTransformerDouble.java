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
 * ImageTransformerDouble.java
 * ---------------
 */
package org.jpedal.images;

import org.jpedal.color.ColorSpaces;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.GraphicsState;
import org.jpedal.render.BaseDisplay;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * class to shrink and clip an extracted image
 * On reparse just calculates co-ords
 */
public class ImageTransformerDouble {

    private static final boolean debug=false;
    
    double ny,nx;

    /**the clip*/
    private Area clip;

    /**holds the actual image*/
    private BufferedImage current_image;

    /**matrices used in transformation*/
    private final float[][] CTM;
    private float[][] Trm,Trm1, Trm2;

    /**image co-ords*/
    private int i_x, i_y, i_w, i_h;

    private final boolean scaleImage;

    /**flag to show image clipped*/
    private boolean hasClip;
    
    float scaling=1;
    
    final int pageRotation;

    /**
     * pass in image information and apply transformation matrix
     * to image
     */
    public ImageTransformerDouble(final GraphicsState currentGS, final BufferedImage new_image, final boolean scaleImage, final float scaling, final int pageRotation) {

        //save global values
        this.current_image = new_image;
        this.scaleImage=scaleImage;
        this.scaling=scaling;
        this.pageRotation=pageRotation;

        CTM = currentGS.CTM; //local copy of CTM
        
        createMatrices();

        // get clipped image and co-ords
        if(currentGS.getClippingShape()!=null) {
            clip = (Area) currentGS.getClippingShape().clone();
        }
        
        calcCoordinates();

    }

    /**
     * applies the shear/rotate of a double transformation to the clipped image
     */
    public final void doubleScaleTransformShear(){

        scale(this.Trm1);

        //create a copy of clip (so we don't alter clip)
        if(clip!=null){

            final Area final_clip = (Area) clip.clone();

            final Area unscaled_clip=getUnscaledClip((Area) clip.clone());

            final int segCount=BaseDisplay.isRectangle(final_clip);

            clipImage(unscaled_clip,final_clip,segCount);

            i_x=(int)clip.getBounds2D().getMinX();
            i_y=(int) clip.getBounds2D().getMinY();
            i_w=(int)((clip.getBounds2D().getMaxX())-i_x);
            i_h=(int)((clip.getBounds2D().getMaxY())-i_y);
        }else if(current_image.getType()==10){  //do not need to be argb
        }else{
            current_image = ColorSpaceConvertor.convertToARGB(current_image);
        }
    }

    /**
     * applies the scale of a double transformation to the clipped image
     */
    public final void doubleScaleTransformScale(){

        if((CTM[0][0]!=0.0)&(CTM[1][1]!=0.0)) {
            scale(Trm2);
        }

    }

    /**complete image and workout co-ordinates*/
    public final void completeImage(){

        //Matrix.show(CTM);
		
		/*if((CTM[0][1]>0 )&(CTM[1][0]>0 )){
			//ShowGUIMessage.showGUIMessage("",current_image,"a ");
			AffineTransform image_at =new AffineTransform();
			image_at.scale(-1,-1);
			image_at.translate(-current_image.getWidth(),-current_image.getHeight());
			AffineTransformOp invert= new AffineTransformOp(image_at,  ColorSpaces.hints);
			
			current_image = invert.filter(current_image,null);
			
		}*/

        //ShowGUIMessage.showGUIMessage("",current_image,"a ");
		
		/**/
        if(hasClip){
            i_x=(int)clip.getBounds2D().getMinX();
            i_y=(int) clip.getBounds2D().getMinY();
            i_w=(current_image.getWidth());
            i_h=(current_image.getHeight());

            //System.out.println(current_image.getWidth()+" "+current_image.getHeight());
            //System.out.println(i_x+" "+i_y+" "+i_w+" "+i_h+" "+clip.getBounds2D());

        }/***/

    }

    /**scale image to size*/
    private void scale(final float[][] Trm){

        /**
         * transform the image only if needed
         */
        if (Trm[0][0] != 1.0|| Trm[1][1] != 1.0 || Trm[0][1] != 0.0 || Trm[1][0] != 0.0) {

            final int w = current_image.getWidth(); //raw width
            final int h = current_image.getHeight(); //raw height
            
            //workout transformation for the image
            AffineTransform image_at =new AffineTransform(Trm[0][0],-Trm[0][1],-Trm[1][0],Trm[1][1],0,0);
            
            //apply it to the shape first so we can align
            final Area r =new Area(new Rectangle(0,0,w,h));
            r.transform(image_at);
            
            //make sure it fits onto image (must start at 0,0)
            ny = r.getBounds2D().getY();
            nx = r.getBounds2D().getX();
            image_at =new AffineTransform(Trm[0][0],-Trm[0][1],-Trm[1][0],Trm[1][1],-nx,-ny);
               
            //Create the affine operation.
            //ColorSpaces.hints causes single lines to vanish);
            final AffineTransformOp invert;
            if((w>10)&(h>10)) {
                invert = new AffineTransformOp(image_at, ColorSpaces.hints);
            } else {
                invert = new AffineTransformOp(image_at, null);
            }

            //scale image to produce final version
            if(scaleImage){
                /**
                 * Hack for general-Sept2013/Page-1-BAW-A380-PDP.pdf buttons
                 * the filter performed on images here seems to break on images with a height of 1px
                 * which are being sheared. Resulting in a black image.
                 */ 
                if( h == 1 && Trm[0][0] == 0 && Trm[0][1] > 0 && Trm[1][0] < 0 && Trm[1][1] == 0){                  
                    final BufferedImage newImage = new BufferedImage(h,w,BufferedImage.TYPE_INT_ARGB);
                    
                    for(int i = 0; i < w; i++){
                            final int col = current_image.getRGB(i, 0);
                            newImage.setRGB(0, (w - 1) - i, col);
                    }                    
                    current_image = newImage;
                }else {
                    current_image = invert.filter(current_image, null);
                }
            }
        }

    }

    /**workout the transformation as 1 or 2 transformations*/
    private void createMatrices(){

        final int w = (int) (current_image.getWidth()/scaling); //raw width
        final int h = (int) (current_image.getHeight()/scaling); //raw height

        //build transformation matrix by hand to avoid errors in rounding
        Trm = new float[3][3];
        Trm[0][0] = (CTM[0][0] / w);
        Trm[0][1] = (CTM[0][1] / w);
        Trm[0][2] = 0;
        Trm[1][0] = (CTM[1][0] / h);
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

        /**now work out as 2 matrices*/
        Trm1=new float[3][3];
        Trm2=new float[3][3];

        //used to handle sheer
        float x1,x2,y1,y2;

        x1=CTM[0][0];
        if(x1<0) {
            x1 = -x1;
        }
        x2=CTM[0][1];
        if(x2<0) {
            x2 = -x2;
        }

        y1=CTM[1][1];
        if(y1<0) {
            y1 = -y1;
        }
        y2=CTM[1][0];
        if(y2<0) {
            y2 = -y2;
        }

        //factor out scaling to produce just the sheer/rotation
        if(CTM[0][0]==0.0 || CTM[1][1]==0.0){
            Trm1=Trm;

        }else if((CTM[0][1]==0.0)&&(CTM[1][0]==0.0)){

            Trm1[0][0] = w/(CTM[0][0]);
            Trm1[0][1] = 0;
            Trm1[0][2] = 0;

            Trm1[1][0] =0;
            Trm1[1][1] = h/(CTM[1][1]);
            Trm1[1][2] = 0;

            Trm1[2][0] = 0;
            Trm1[2][1] = 0;
            Trm1[2][2] = 1;


            Trm1=Matrix.multiply(Trm,Trm1);

            //round numbers if close to 1
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if ((Trm1[x][y] > .99) & (Trm1[x][y] < 1)) {
                        Trm1[x][y] = 1;
                    }
                }
            }

            /**
             * correct if image reversed on horizontal axis
             */
            if(Trm1[2][0]<0 && Trm1[0][0]>0 && CTM[0][0]<0){
                Trm1[2][0]=0;
                Trm1[0][0]=-1f;

            }

            /**
             * correct if image reversed on vertical axis
             */
            if(Trm1[2][1]<0 && Trm1[1][1]>0 && CTM[1][1]<0 && CTM[0][0]<0){
                Trm1[2][1]=0;
                Trm1[1][1]=-1f;
            }

        }else{ //its got sheer/rotation

            if(x1>x2) {
                Trm1[0][0] = w / (CTM[0][0]);
            } else {
                Trm1[0][0] = w / (CTM[0][1]);
            }
            if (Trm1[0][0]<0) {
                Trm1[0][0] = -Trm1[0][0];
            }
            Trm1[0][1] = 0;
            Trm1[0][2] = 0;

            Trm1[1][0] =0;

            if(y1>y2) {
                Trm1[1][1] = h / (CTM[1][1]);
            } else {
                Trm1[1][1] = h / (CTM[1][0]);
            }
            if (Trm1[1][1]<0) {
                Trm1[1][1] = -Trm1[1][1];
            }
            Trm1[1][2] = 0;

            Trm1[2][0] = 0;
            Trm1[2][1] = 0;
            Trm1[2][2] = 1;


            Trm1=Matrix.multiply(Trm,Trm1);

            //round numbers if close to 1
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if ((Trm1[x][y] > .99) & (Trm1[x][y] < 1)) {
                        Trm1[x][y] = 1;
                    }
                }
            }
        }
       
        //create a transformation with just the scaling
        if(x1>x2) {
            Trm2[0][0] = (CTM[0][0] / w);
        } else {
            Trm2[0][0] = (CTM[0][1] / w);
        }

        if(Trm2[0][0] <0) {
            Trm2[0][0] = -Trm2[0][0];
        }
        Trm2[0][1] = 0;
        Trm2[0][2] = 0;
        Trm2[1][0] = 0;
        if(y1>y2) {
            Trm2[1][1] = (CTM[1][1] / h);
        } else {
            Trm2[1][1] = (CTM[1][0] / h);
        }

        if(Trm2[1][1] <0) {
            Trm2[1][1] = -Trm2[1][1];
        }

        Trm2[1][2] = 0;
        Trm2[2][0] = 0;
        Trm2[2][1] = 0;
        Trm2[2][2] = 1;

        //round numbers if close to 1
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if ((Trm2[x][y] > .99) & (Trm2[x][y] < 1)) {
                    Trm2[x][y] = 1;
                }
            }
        }
    }

    /**
     * workout correct screen co-ords allow for rotation
     */
    private void calcCoordinates(){

        if ((CTM[1][0] == 0) &( (CTM[0][1] == 0))){

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

            if((CTM[1][0]>0)&(CTM[0][1]<0)){
                i_x = (int) (CTM[2][0]);
                i_y = (int) (CTM[2][1]+CTM[0][1]);
                //System.err.println("AA "+i_w+" "+i_h);

            }else if((CTM[1][0]<0)&(CTM[0][1]>0)){
                i_x = (int) (CTM[2][0]+CTM[1][0]);
                i_y = (int) (CTM[2][1]);
                //System.err.println("BB "+i_w+" "+i_h);
            }else if((CTM[1][0]>0)&(CTM[0][1]>0)){
                i_x = (int) (CTM[2][0]);
                i_y = (int) (CTM[2][1]);
                //System.err.println("CC "+i_w+" "+i_h);
            }else{
                //System.err.println("DD "+i_w+" "+i_h);
                i_x = (int) (CTM[2][0]);
                i_y =(int) (CTM[2][1]);
            }

        }

        //System.err.println(i_x+" "+i_y+" "+i_w+" "+i_h);
        //Matrix.show(CTM);
        //alter to allow for back to front or reversed
        if ( CTM[1][1]< 0) {
            i_y -= i_h;
        }
        if ( CTM[0][0]< 0) {
            i_x -= i_w;
        }

//ShowGUIMessage.showGUIMessage("",current_image,"xx="+i_x+" "+i_y+" "+i_w+" "+i_h+" h="+current_image.getHeight());


    }
    //////////////////////////////////////////////////////////////////////////
    /**
     * get y of image (x1,y1 is top left)
     */
    public final int getImageY() {
        return i_y;
    }
    //////////////////////////////////////////////////////////////////////////
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

    /**
     * clip the image
     */
    private void clipImage(final Area final_clip, final Area unscaled_clip, final int segCount) {

        if(debug) {
            System.out.println("[clip image] segCount=" + segCount);
        }
        
        final double shape_x = unscaled_clip.getBounds2D().getX();
        final double shape_y = unscaled_clip.getBounds2D().getY();

        final int image_w = current_image.getWidth();
        final int image_h = current_image.getHeight();

        //co-ords of transformed shape
        //reset sizes to remove area clipped
        int x = (int) final_clip.getBounds().getX();
        int y = (int) final_clip.getBounds().getY();
        int w = (int) final_clip.getBounds().getWidth();
        int h = (int) final_clip.getBounds().getHeight();

        if(debug) {
            System.out.println("[clip image] raw clip size==" + x + ' ' + y + ' ' + w + ' ' + h + " image size=" + image_w + ' ' + image_h);
        }
                
        //System.out.println(x+" "+y+" "+w+" "+h+" "+current_image.getWidth()+" "+current_image.getHeight());
//if(BaseDisplay.isRectangle(final_clip)<7 && Math.abs(final_clip.getBounds().getWidth()-current_image.getWidth())<=1 && Math.abs(final_clip.getBounds().getHeight()-current_image.getHeight())<=1){


        /**
         * if not rectangle create inverse of clip and paint on to add transparency
         */
        if(segCount>5){

            if(debug) {
                System.out.println("[clip image] create inverse of clip");
            }
        
            //turn image upside down
            final AffineTransform image_at =new AffineTransform();
            image_at.scale(1,-1);
            image_at.translate(0,-current_image.getHeight());
            final AffineTransformOp invert= new AffineTransformOp(image_at,  ColorSpaces.hints);

            current_image = invert.filter(current_image,null);

            final Area inverseClip =new Area(new Rectangle(0,0,image_w,image_h));
            inverseClip.exclusiveOr(final_clip);
            current_image = ColorSpaceConvertor.convertToARGB(current_image);//make sure has opacity

            final Graphics2D image_g2 = current_image.createGraphics(); //g2 of canvas
            image_g2.setComposite(AlphaComposite.Clear);
            image_g2.fill(inverseClip);

            //and invert again
            final AffineTransform image_at2 =new AffineTransform();
            image_at2.scale(1,-1);
            image_at2.translate(0,-current_image.getHeight());
            final AffineTransformOp invert3= new AffineTransformOp(image_at2,  ColorSpaces.hints);

            current_image = invert3.filter(current_image,null);

        }
        //get image (now clipped )

        //check for rounding errors
        //if (y < 0 && 1==2) { //causes issues in some HTML files so commented out to see impact (see case 17246) general-May2014/Pages from SVA-E170-SOPM.pdf
        //    h = h - y;
       //     y = 0;
       // }else
        {

        //do not do if image inverted
        if(CTM[1][1]<0 && pageRotation==0 && CTM[0][0]>0 && CTM[1][0]==0 && CTM[0][1]==0){
          //needs to be ignored 
            ///Users/markee/PDFdata/test_data/sample_pdfs_html/general-May2014/17461.pdf
        }else{
            y=image_h-h-y;
        }
        
            //allow for fp error
            if(y<0){
                y=0;
            }
        }

        if (x < 0) {
            w -= x;
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

        //extract if smaller with clip
        if(h<1 || w<1){ //ignore if not wide/high enough
        }else if(x==0 && y==0 && w==current_image.getWidth() && h==current_image.getHeight()){
            //dont bother if no change
        }else if(CTM[1][1]==0 && pageRotation==0 && CTM[0][0]==0 && CTM[1][0]<0 && CTM[0][1]>0){
          //ignore for moment
            ///Users/markee/PDFdata/test_data/sample_pdfs_html/general-May2014/17733.pdf
        }else{
            try {
                
                current_image = current_image.getSubimage(x, y, w, h);

                if(debug) {
                    System.out.println("[clip image] reduce size x,y,w,h=" + x + ", " + y + ", " + w + ", " + h);
                }
                
            } catch (final Exception e) {
                LogWriter.writeLog("Exception " + e + " extracting clipped image with values x="+x+" y="+y+" w="+w+" h="+h+" from image "+current_image);

                //<end-demo>

            }catch(final Error err){
                LogWriter.writeLog("Exception " + err + " extracting clipped image with values x=" + x + " y=" + y + " w=" + w + " h=" + h + " from image " + current_image);
                //<end-demo>
            }
        }

        //work out new co-ords from shape and current
        final double x1;
        final double y1;
        if (i_x > shape_x){
            x1 = i_x;
        }else{
            x1 = shape_x;
        }
        if (i_y > shape_y){
            y1 = i_y;
        }else{
            y1 = shape_y;
        }

        i_x = (int) x1;
        i_y = (int) y1;
        i_w = w;
        i_h =  h;
    }

    private Area getUnscaledClip(final Area final_clip) {

        double dx=-(CTM[2][0]),dy= -CTM[2][1];

        if(CTM[1][0]<0){
            dx -= CTM[1][0] ;
        }
        if((CTM[0][0]<0)&&(CTM[1][0]>=0)){
            dx -= CTM[1][0] ;
        }

        if(CTM[0][1]<0){
            dy -= CTM[0][1];
        }
        if(CTM[1][1]<0){
            if(CTM[0][1]>0){
                dy -= CTM[0][1];
            }else if(CTM[1][1]<0){
                dy -= CTM[1][1];
            }
        }

        final AffineTransform align_clip = new AffineTransform();
        align_clip.translate(dx,dy );
        final_clip.transform(align_clip);

        final AffineTransform invert2=new AffineTransform(1/Trm2[0][0],0,0,1/Trm2[1][1],0,0);

        final_clip.transform(invert2);

        //fix for 'mirror' image on Mac
        final int dxx = (int) final_clip.getBounds().getX();
        if(dxx<0){
            final_clip.transform(AffineTransform.getTranslateInstance(-dxx,0));
        }

        return final_clip;
    }
}
