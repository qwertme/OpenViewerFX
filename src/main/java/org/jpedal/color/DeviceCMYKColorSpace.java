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
 * DeviceCMYKColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jpedal.examples.handlers.DefaultImageHelper;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

import org.jpedal.exception.PdfException;

/**
 * handle DeviceCMYKColorSpace
 */
public class DeviceCMYKColorSpace extends  GenericColorSpace{

    private static final long serialVersionUID = 4054062852632000027L;

    private float lastC = -1, lastM=-1, lastY=-1, lastK=-1;

    public  static ColorSpace CMYK;

    /**
     * ensure next setColor will not match with old color as value may be out of
     * sync
     */
    @Override
    public void clearCache() {
        lastC = -1;
    }

    /**
     * initialise CMYK profile
     */
    private void initColorspace() {

        /**
         * load the cmyk profile - I am using the Adobe version from the web.
         * There are lots out there.
         */
        InputStream stream = null;

        try {
            final String profile = System.getProperty("org.jpedal.CMYKprofile");

            if (profile == null) {
                stream = this.getClass().getResourceAsStream("/org/jpedal/res/cmm/cmyk.icm");
            } else {
                try {
                    stream = new FileInputStream(profile);
                } catch (final FileNotFoundException ee) {
                    try {
                        throw new PdfException("PdfException attempting to use user profile " + profile + " Message=" + ee);
                    } catch (PdfException ex) {
                        Logger.getLogger(DeviceCMYKColorSpace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            final ICC_Profile p = ICC_Profile.getInstance(stream);
            CMYK = new ICC_ColorSpace(p);

        } catch (final IOException e) {
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e);
            }
            //

            throw new RuntimeException("Problem setting CMYK Colorspace with message " + e + " Possible cause file cmyk.icm corrupted");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (final IOException e) {
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception " + e);
                    }
                    //
                }
            }
        }
    }

    /**
     * setup colorspaces
     */
    public DeviceCMYKColorSpace() {

        componentCount = 4;

        if (CMYK == null) {
            initColorspace();
        }

        cs = CMYK;

        setType(ColorSpaces.DeviceCMYK);

    }

    /**
     * set CalRGB color (in terms of rgb)
     */
    @Override
    public final void setColor(final String[] number_values, final int items) {

        final float[] colValues = new float[items];

        for (int ii = 0; ii < items; ii++) {
            colValues[ii] = Float.parseFloat(number_values[ii]);
        }

        setColor(colValues, items);
    }

    /**
     * convert CMYK to RGB as defined by Adobe (p354 Section 6.2.4 in Adobe 1.3
     * spec 2nd edition) and set value
     */
    @Override
    public final void setColor(final float[] operand, final int length) {

        //default of black
        c = 1;
        y = 1;
        m = 1;
        k = 1;

        if (length > 3) {
            //get values
            c = operand[0];
            // the cyan
            m = operand[1];
            // the magenta
            y = operand[2];
            // the yellow
            k = operand[3];
        } else {
            //get values
            if (length > 0) {
                c = operand[0];
            }
            // the cyan
            if (length > 1) {
                m = operand[1];
            }
            // the magenta
            if (length > 2) {
                y = operand[2];
            }
            // the yellow
            if (length > 3) {
                k = operand[3];
            }

        }

        //
        //old code still used by OpenFXViewer
        if ((lastC == c) && (lastM == m) && (lastY == y) && (lastK == k)) {
            //no change
        } else {

            //store values
            rawValues=new float[4];
            rawValues[0]=c;
            rawValues[1]=m;
            rawValues[2]=y;
            rawValues[3]=k;

            if(c==0 && y==0 && m==0 && k==0) {
                this.currentColor = new PdfColor(1.0f, 1.0f, 1.0f);
            }else if(c==1 && y==1 && m==1 && k==1){
                this.currentColor=new PdfColor(0.0f,0.0f,0.0f);

            }else{
                if(c>.99) {
                    c=1.0f;
                } else if(c<0.01) {
                    c=0.0f;
        }
                if(m>.99) {
                    m=1.0f;
                } else if(m<0.01) {
                    m=0.0f;
                }
                if(y>.99) {
                    y=1.0f;
                } else if(y<0.01) {
                    y=0.0f;
                }
                if(k>.99) {
                    k=1.0f;
                } else if(k<0.01) {
                    k=0.0f;
                }

                //we store values to speedup operation
                float[] rgb=null;
                
                if(rgb==null){
                    final float[] cmykValues = {c,m,y,k};
                    rgb=CMYK.toRGB(cmykValues);
                    
                    //check rounding
                    for(int jj=0;jj<3;jj++){
                        if(rgb[jj]>.99) {
                            rgb[jj]=1.0f;
                        } else if(rgb[jj]<0.01) {
                            rgb[jj]=0.0f;
    }
                    }
                }
                currentColor=new PdfColor(rgb[0],rgb[1],rgb[2]);

            }
            lastC=c;
            lastM=m;
            lastY=y;
            lastK=k;
        }


/**/
  
 }
    
    /**
     * <p>
     * Convert DCT encoded image bytestream to sRGB
     * </p>
     * <p>
     * It uses the internal Java classes and the Adobe icm to convert CMYK and
     * YCbCr-Alpha - the data is still DCT encoded.
     * </p>
     * <p>
     * The Sun class JPEGDecodeParam.java is worth examining because it contains
     * lots of interesting comments
     * </p>
     * <p>
     * I tried just using the new IOImage.read() but on type 3 images, all my
     * clipping code stopped working so I am still using 1.3
     * </p>
     */
    @Override
    public final BufferedImage JPEGToRGBImage(
            final byte[] data, final int w, final int h, final float[] decodeArray, final int pX, final int pY, final boolean arrayInverted, final PdfObject XObject) {

        return nonRGBJPEGToRGBImage(data, w, h, decodeArray, pX, pY);

    }

    /**
     * default RGB implementation just returns data
     */
    @Override
    public byte[] dataToRGBByteArray(final byte[] data, final int w, final int h, boolean arrayInverted) {

        int pixelCount = w * h * 4;
        final int dataSize = data.length;
        if (pixelCount > dataSize) { //allow for mis-sized
            pixelCount = dataSize - 3;
        }
        
         //
        
        
        /**
         * set colorspaces and color models using profiles if set
         */
        final ColorSpace CMYK = DeviceCMYKColorSpace.getColorSpaceInstance();

        int C, M, Y, K, lastC = -1, lastM = -1, lastY = -1, lastK = -1;

        byte[] rgbData = new byte[w * h * 3];
        int j = 0;
        float[] RGB = {0f, 0f, 0f};
        //turn YCC in Buffer to CYM using profile
        for (int i = 0; i < pixelCount; i += 4) {

            C = (data[i] & 255);
            M = (data[i + 1] & 255);
            Y = (data[i + 2] & 255);
            K = (data[i + 3] & 255);

            //cache last value, black and white
            if (C == lastC && M == lastM && Y == lastY && K == lastK) {
                //no change so use last value
            } else if (C == 0 && M == 0 && Y == 0 && K == 0) {
                RGB = new float[]{1f, 1f, 1f};
            } else if (C == 255 && M == 255 && Y == 255 && K == 255) {
                RGB = new float[]{0f, 0f, 0f};
            } else { //new value

                RGB = CMYK.toRGB(new float[]{C / 255f, M / 255f, Y / 255f, K / 255f});

                //flag so we can just reuse if next value the same
                lastC = C;
                lastM = M;
                lastY = Y;
                lastK = K;
            }

            //put back as CMY
            rgbData[j] = (byte) (RGB[0] * 255f);
            rgbData[j + 1] = (byte) (RGB[1] * 255f);
            rgbData[j + 2] = (byte) (RGB[2] * 255f);

            j += 3;

        }

        return rgbData;
    }

    /**
     * convert byte[] datastream JPEG to an image in RGB
     *
     * @throws PdfException
     */
    @Override
    public BufferedImage JPEG2000ToRGBImage(final byte[] data, int w, int h, final float[] decodeArray, final int pX, final int pY, final int d) throws PdfException {

        BufferedImage image = DefaultImageHelper.JPEG2000ToRGBImage(data, w, h, decodeArray, pX, pY);

        if (image != null) {
            return image;
        } else {
            return JPEG2000ToImage(data, pX, pY, decodeArray);
        }
    }

    /**
     * convert Index to RGB
     */
    @Override
    public final byte[] convertIndexToRGB(final byte[] index) {

        isConverted = true;

        return convert4Index(index);
    }

    public static ColorSpace getColorSpaceInstance() {

        ColorSpace CMYK = new DeviceCMYKColorSpace().getColorSpace();

        //optional alternative CMYK
        final String CMYKprofile = System.getProperty("org.jpedal.CMYKprofile");

        if (CMYKprofile != null) {

            try {
                CMYK = new ICC_ColorSpace(ICC_Profile.getInstance(new FileInputStream(CMYKprofile)));
            } catch (final IOException e) {
                throw new RuntimeException("Unable to create CMYK colorspace with  " + CMYKprofile + "\nPlease check Path and file valid or use built-in " + e);
            }
        }

        return CMYK;
    }
}
