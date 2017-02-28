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
 * StandardImageIO.java
 * ---------------
 */
package org.jpedal.examples.handlers;

import org.jpedal.color.GenericColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ImageHelper;
import org.jpedal.utils.LogWriter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Iterator;
 //

public class StandardImageIO implements ImageHelper {
    
    public StandardImageIO(){
        ImageIO.setUseCache(false);
    }

    /**
     *
     * @param image
     * @param type
     * @param file_name
     * @throws IOException
     */
    @Override
    public void write(BufferedImage image, final String type, final String file_name) throws IOException {
       
        //
        ImageIO.write(image,type,new File(file_name));
        /**/
        
        //
    }

    /**
     *
     * @param file_name (including path)
     * @return
     */
    @Override
    public BufferedImage read(final String file_name) {
        
        BufferedImage image=null;
        
        try {
            image = ImageIO.read(new File(file_name));
            
            //BufferedInputStream in = new BufferedInputStream(new FileInputStream(file_name));
            //image = ImageIO.read(in);
            //in.close();
        } catch (final IOException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        } catch (final Error err) {
            
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Error: " + err.getMessage());
            }
            //
            throw new RuntimeException("Error " + err + " loading "+file_name+" with ImageIO");
            
        }
        
        return image;
    }


    /**
     *
     * @param data (final byte[] data for image file - PNG, TIF, JPEG)
     * @return
     * @throws IOException
     */
    @Override
    public synchronized BufferedImage read(final byte[] data) throws IOException{
    	final ByteArrayInputStream bis=new ByteArrayInputStream(data);
        
        ImageIO.setUseCache(false);

        return ImageIO.read(bis);

    }

    /**
     *
     * @param data (binary data for image file - PNG, TIF, JPEG)
     * @return
     * @throws IOException
     */
    @Override
    public Raster readRasterFromJPeg(final byte[] data) throws IOException {

        Raster ras=null;
        ImageReader iir=null;
        ImageInputStream iin=null;

        final ByteArrayInputStream in = new ByteArrayInputStream(data);

        //suggestion from Carol
        try{
            final Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG");

            while (iterator.hasNext()){

                final Object o = iterator.next();
                iir = (ImageReader) o;
                if (iir.canReadRaster()) {
                    break;
                }
            }

            ImageIO.setUseCache(false);

            iin = ImageIO.createImageInputStream(in);
            iir.setInput(iin, true);   //new MemoryCacheImageInputStream(in));

            ras=iir.readRaster(0,null);

        }catch(final Exception e){

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Unable to find jars on classpath "+e);
            }

        }finally{
            if(in!=null) {
                in.close();
            }

            if(iin!=null){
                iin.flush();
                iin.close();
            }

            if(iir!=null) {
                iir.dispose();
            }
        }
        return ras;
    }

    /**
     *
     * @param data  (binary data for image file - PNG, TIF, JPEG)
     * @param w   image width in pixels
     * @param h   image height in pixels
     * @param decodeArray PDF object value showing min/max with 1 paired valued per channel (ie [0 1 0 1 0 1 0 1]
     *                    can be float range 0-1 or int 0 -255). if 1 0 invert the value
     * @param pX  sensible image width in pixels (ie what we should downscale to)
     * @param pY  sensible image height in pixels (ie what we should downscale to)
     * @return
     * @throws PdfException
     */
    @Override
    public BufferedImage JPEG2000ToRGBImage(final byte[] data, final int w, final int h, final float[] decodeArray, final int pX, final int pY) throws PdfException {

        return null;
    }
}
