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
 * D3.java
 * ---------------
 */
package com.idrsolutions.image.png;

/**
 * Class performs dithering in 24 bit images
 */
public class D3 {
    
    public final int r;
    public final int g;
    public final int b;
    public final int rgb;
    
    public D3(int r,int g, int b){
        this.r = r;
        this.g = g;
        this.b = b;
        this.rgb = (r << 16) | (g << 8) | b;
    }

    private static int diff(int r, int g, int b, D3 pal) {        
        int Rdiff = r - pal.r;
        int Gdiff = g - pal.g;
        int Bdiff = b - pal.b;
        return Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff;
    }
    
    public static int[] findClosest(int argb, D3[] palette) {
        D3 closest = palette[0];
        D3 n;
        int r = (argb>> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        int cDiff = diff(r,g,b,closest);
        int nDiff;
        int found = 0;
        for (int i = 1; i < 256; i++) {
            n = palette[i];
            nDiff = diff(r,g,b,n);

            if (nDiff < cDiff) {
                closest = n;
                found = i;
                cDiff = nDiff;
            }
        }
        return new int[]{closest.rgb, found};
    }

    public static byte[] process(byte[] colorPalette, int[][] image, int h, int w) {

        int p = 0;
        D3[] palette = new D3[256];
        for (int i = 0; i < 256; i++) {
            int r = colorPalette[p++] & 0xff;
            int g = colorPalette[p++] & 0xff;
            int b = colorPalette[p++] & 0xff;
            palette[i] = new D3(r, g, b);
        }

        byte[] indexedPixels = new byte[h * w];
        p = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int argb = image[y][x];

//                byte bb = wu.findMatch(argb);
//                indexedPixels[p++] = bb;
//                int nextArgb = palette[bb&0xff].rgb;
                
                int[] obj = findClosest(argb, palette);
                int nextArgb = obj[0];
                indexedPixels[p++] = (byte) obj[1];

                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;

                int nr = (nextArgb >> 16) & 0xff;
                int ng = (nextArgb >> 8) & 0xff;
                int nb = nextArgb & 0xff;

                int errR = r - nr;
                int errG = g - ng;
                int errB = b - nb;

                if (x + 1 < w) {
                    int update = applyFloyd(image[y][x + 1], errR, errG, errB, 7);
                    image[y][x + 1] = update;
                    if (y + 1 < h) {
                        update = applyFloyd(image[y + 1][x + 1], errR, errG, errB, 1);
                        image[y + 1][x + 1] = update;
                    }
                }
                if (y + 1 < h) {
                    int update = applyFloyd(image[y + 1][x], errR, errG, errB, 5);
                    image[y + 1][x] = update;
                    if (x - 1 >= 0) {
                        update = applyFloyd(image[y + 1][x - 1], errR, errG, errB, 3);
                        image[y + 1][x - 1] = update;
                    }
                }
            }
        }
        return indexedPixels;
    }

    private static int applyFloyd(final int argb, final int errR, final int errG, final int errB, final int mul) {
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;

        r += errR * mul / 16;
        g += errG * mul / 16;
        b += errB * mul / 16;

        if (r < 0) {
            r = 0;
        } else if (r > 0xff) {
            r = 0xff;
        }
        if (g < 0) {
            g = 0;
        } else if (g > 0xff) {
            g = 0xff;
        }
        if (b < 0) {
            b = 0;
        } else if (b > 0xff) {
            b = 0xff;
        }

        return (r << 16) | (g << 8) | b;
    }

}
