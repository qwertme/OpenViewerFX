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
 * D4.java
 * ---------------
 */
package com.idrsolutions.image.png;

/**
 * Class performs dithering in 32 bit images
 *
 */
public class D4 {
    
    public final int a;
    public final int r;
    public final int g;
    public final int b;
    public final int argb;
    
    public D4(int a, int r,int g, int b){
        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
        this.argb = (a<<24) | (r << 16) | (g << 8) | b;
    }
    
    private static int diff(int a, int r, int g, int b, D4 pal) {        
        int Adiff = a - pal.a;
        int Rdiff = r - pal.r;
        int Gdiff = g - pal.g;
        int Bdiff = b - pal.b;
        return Adiff * Adiff + Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff;
    }
    
    public static int[] findClosest(int argb, D4[] palette) {
        D4 closest = palette[0];
        D4 n;
        int a = (argb>> 24) & 0xff;
        int r = (argb>> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        int cDiff = diff(a,r,g,b,closest);
        int nDiff;
        int found = 0;
        for (int i = 1; i < 256; i++) {
            n = palette[i];
            nDiff = diff(a,r,g,b,n);

            if (nDiff < cDiff) {
                closest = n;
                found = i;
                cDiff = nDiff;
            }
        }
        return new int[]{closest.argb, found};
    }

    public static byte[] process(byte[] colorPalette, byte[] trns, int[][] image, int h, int w) {

        int p = 0;
        D4[] palette = new D4[256];
        for (int i = 0; i < 256; i++) {
            int a = trns[i] & 0xff;
            int r = colorPalette[p++] & 0xff;
            int g = colorPalette[p++] & 0xff;
            int b = colorPalette[p++] & 0xff;
            palette[i] = new D4(a, r, g, b);
        }


        byte[] indexedPixels = new byte[h * w];
        p = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int argb = image[y][x];
                int[] obj = findClosest(argb, palette);
                int nextArgb = obj[0];
                indexedPixels[p++] = (byte) obj[1];

                final int a = (argb >> 24) & 0xff;
                final int r = (argb >> 16) & 0xff;
                final int g = (argb >> 8) & 0xff;
                final int b = argb & 0xff;

                final int na = (nextArgb >> 24) & 0xff;
                final int nr = (nextArgb >> 16) & 0xff;
                final int ng = (nextArgb >> 8) & 0xff;
                final int nb = nextArgb & 0xff;

                final int errA = a - na;
                final int errR = r - nr;
                final int errG = g - ng;
                final int errB = b - nb;

                if (x + 1 < w) {
                    int update = applyFloyd(image[y][x + 1], errA, errR, errG, errB, 7);
                    image[y][x + 1] = update;
                    if (y + 1 < h) {
                        update = applyFloyd(image[y + 1][x + 1], errA, errR, errG, errB, 1);
                        image[y + 1][x + 1] = update;
                    }
                }
                if (y + 1 < h) {
                    int update = applyFloyd(image[y + 1][x], errA, errR, errG, errB, 5);
                    image[y + 1][x] = update;
                    if (x - 1 >= 0) {
                        update = applyFloyd(image[y + 1][x - 1], errA, errR, errG, errB, 3);
                        image[y + 1][x - 1] = update;
                    }

                }
            }
        }
        return indexedPixels;
    }

    private static int applyFloyd(final int argb, final int errA, final int errR, final int errG, final int errB, final int mul) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;

        a += errA * mul / 16;
        r += errR * mul / 16;
        g += errG * mul / 16;
        b += errB * mul / 16;

        if (a < 0) {
            a = 0;
        } else if (a > 0xff) {
            a = 0xff;
        }

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

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

}
