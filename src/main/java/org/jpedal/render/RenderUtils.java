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
 * RenderUtils.java
 * ---------------
 */
package org.jpedal.render;

import org.jpedal.color.ColorSpaces;
import org.jpedal.utils.LogWriter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * static helper methods for rendering
 */
public class RenderUtils {

    public static boolean isInverted(final float[][] CTM){
        
        if(CTM[0][0]<0 && CTM[1][1]<1){
            return false;
        }else if(CTM[0][0]<0 && CTM[1][1]>1){
            return false;
        }else{
            return CTM[0][0] < 0 || CTM[1][1] < 0;
        }
    }

    public static BufferedImage invertImage(BufferedImage image) {

//    	if((CTM[0][0]==0 || CTM[1][1]==0) || (isRotated(CTM) && !isInverted(CTM))) {
//    		return image;
//    	}

        //turn upside down
        final AffineTransform image_at2 =new AffineTransform();
        image_at2.scale(1,-1);
        image_at2.translate(0,-image.getHeight());
        
        AffineTransformOp invert3= new AffineTransformOp(image_at2,  ColorSpaces.hints);
        
        if(image.getType()==12){ //avoid turning into ARGB
            
            final BufferedImage source=image;
            image =new BufferedImage(source.getWidth(),source.getHeight(),source.getType());
            
            invert3.filter(source,image);
        }else{
            
            boolean failed=false;
            //allow for odd behaviour on some files
            try{
                image = invert3.filter(image,null);
            }catch(final Exception e){
                failed=true;
                
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception in handling image "+e);
                }
            }
            if(failed){
                try{
                    invert3 = new AffineTransformOp(image_at2,null);
                    image = invert3.filter(image,null);
                }catch(final Exception e){
                    //
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Caught a Exception " + e);
                    }
                }
            }
        }
        
        return image;
    }


    static BufferedImage invertImageBeforeSave(BufferedImage image, final boolean horizontal) {

        //turn upside down
        final AffineTransform image_at2 =new AffineTransform();
        if(horizontal){
            image_at2.scale(-1,1);
            image_at2.translate(-image.getWidth(),0);
        }else{
            image_at2.scale(1,-1);
            image_at2.translate(0,-image.getHeight());
        }
        
        final AffineTransformOp invert3= new AffineTransformOp(image_at2,  ColorSpaces.hints);
        
        if(image.getType()==12){ //avoid turning into ARGB
            
            final BufferedImage source=image;
            image =new BufferedImage(source.getWidth(),source.getHeight(),source.getType());
            
            invert3.filter(source,image);
        }else {
            image = invert3.filter(image, null);
        }
        
        return image;
    }

    /**resize array*/
    static float[] checkSize(float[] array, final int currentItem) {

        final int size=array.length;
        if(size<=currentItem){
            final int newSize=size*2;
            final float[] newArray=new float[newSize];
            System.arraycopy( array, 0, newArray, 0, size );

            array=newArray;
        }


        return array;

    }

    /**
     * update clip
     * @param defaultClip
     */
    public static void renderClip(final Area clip, final Rectangle dirtyRegion,  final Shape defaultClip, final Graphics2D g2) {

        if (clip != null){
            g2.setClip(clip);

            //can cause problems in Canoo so limit effect if Canoo running
            if(dirtyRegion!=null)// && (!isRunningOnRemoteClient || clip.intersects(dirtyRegion)))
            {
                g2.clip(dirtyRegion);
            }
        }else if(g2!=null) {
            g2.setClip(defaultClip);
        }
    }


    //work out size glyph occupies
    static Rectangle getAreaForGlyph(final float[][] trm){
        //workout area
        final int w=(int) Math.sqrt((trm[0][0]*trm[0][0])+(trm[1][0]*trm[1][0]));
        final int h=(int) Math.sqrt((trm[1][1]*trm[1][1])+(trm[0][1]*trm[0][1]));

        float xDiff = 0;
        float yDiff = 0;

        if(trm[0][0]<0) {
            xDiff = trm[0][0];
        } else if(trm[1][0]<0) {
            xDiff = trm[1][0];
        }

        if(trm[1][1]<0) {
            yDiff = trm[1][1];
        } else if(trm[0][1]<0) {
            yDiff = trm[0][1];
        }

        return (new Rectangle((int)(trm[2][0]+xDiff),(int)(trm[2][1]+yDiff),w,h));

    }

    /**
     * Rectangle contains code does not handle negative values
     * Use this instead.
     * @param area : Rectangle to look in
     * @param x : value on the x axis
     * @param y : value on the y axis
     * @return true is point is within area
     */
    public static boolean rectangleContains(final int[] area, final int x, final int y){

        int lowX = area[0];
        int hiX = area[0]+area[2];
        int lowY = area[1];
        int hiY = area[1]+area[3];
        boolean containsPoint = false;

        //if negative value used swap the lowest and highest point
        if(lowX>hiX){
            final int temp = lowX;
            lowX = hiX;
            hiX = temp;
        }

        if(lowY>hiY){
            final int temp = lowY;
            lowY = hiY;
            hiY = temp;
        }

        if((lowY < y && y < hiY) && (lowX < x && x < hiX)) {
            containsPoint = true;
        }

        return containsPoint;
    }

    /**
     * generic method to return a serilized object from an InputStream
     *
     * NOT PART OF API and subject to change (DO NOT USE)
     *
     * @param bis - ByteArrayInputStream containing serilized object
     * @return - deserilized object
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Object restoreFromStream(final ByteArrayInputStream bis) throws IOException, ClassNotFoundException{

        //turn back into object
        final ObjectInput os=new ObjectInputStream(bis);

        return os.readObject();
    }

    /**
     * generic method to serilized an object to an OutputStream
     *
     * NOT PART OF API and subject to change (DO NOT USE)
     *
     *
     * @param bos - ByteArrayOutputStream to serilize to
     * @param obj - object to serilize
     * @throws java.io.IOException
     */
    public static void writeToStream(final ByteArrayOutputStream bos, final Object obj) throws IOException {

        final ObjectOutput os=new ObjectOutputStream(bos);

        os.writeObject(obj);
        os.close();
    }


}
