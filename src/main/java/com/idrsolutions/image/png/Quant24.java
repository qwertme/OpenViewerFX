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
 * Quant24.java
 * ---------------
 */
package com.idrsolutions.image.png;

/**
 * 24 bit to 8 bit Image Quantisation implementation
 */
public class Quant24 {

    private static final int IndexBits = 7;
    private static final int IndexBitsPlus = IndexBits + 1;
    private static final int DoubleIndexBits = IndexBits * 2;
    private static final int IndexCount = (1 << IndexBits) + 1;
    private static final int TableLength = IndexCount * IndexCount * IndexCount;

    private final long[] vwt, vmr, vmg, vmb;
    private final double[] m2;
    private final byte[] tag;

    public Quant24() {
        vwt = new long[TableLength];
        vmr = new long[TableLength];
        vmg = new long[TableLength];
        vmb = new long[TableLength];
        m2 = new double[TableLength];
        tag = new byte[TableLength];
    }

    public byte[] getPalette(int[][] image) {
        int colorCount = 256;
        histogram(image);
        M3d();
        Cube[] cube = new Cube[colorCount];
        buildCube(cube, colorCount);

        byte[] palette = new byte[256 * 3];

        int z = 0;
        for (int k = 0; k < colorCount; k++) {
//            mark(cube[k], (byte) k, tag);
            double weight = volume(cube[k], vwt);
            if (weight != 0) {
                palette[z++] = (byte) (volume(cube[k], vmr) / weight);
                palette[z++] = (byte) (volume(cube[k], vmg) / weight);
                palette[z++] = (byte) (volume(cube[k], vmb) / weight);

            } else {
                z += 3;
            }
        }
        return palette;
    }

    public byte findMatch(int rgb) {
        int mm = 8 - IndexBits;
        int r = ((rgb >> 16) & 0xff) >> mm;
        int g = ((rgb >> 8) & 0xff) >> mm;
        int b = (rgb & 0xff) >> mm;

        int ind = indexify(r + 1, g + 1, b + 1);

        return tag[ind];
    }

    private static int indexify(int r, int g, int b) {
        return (r << DoubleIndexBits) + (r << IndexBitsPlus) + (g << IndexBits) + r + g + b;
    }

    private static double volume(Cube cube, long[] moment) {
        return moment[indexify(cube.R1, cube.G1, cube.B1)]
                - moment[indexify(cube.R1, cube.G1, cube.B0)]
                - moment[indexify(cube.R1, cube.G0, cube.B1)]
                + moment[indexify(cube.R1, cube.G0, cube.B0)]
                - moment[indexify(cube.R0, cube.G1, cube.B1)]
                + moment[indexify(cube.R0, cube.G1, cube.B0)]
                + moment[indexify(cube.R0, cube.G0, cube.B1)]
                - moment[indexify(cube.R0, cube.G0, cube.B0)];
    }

    private static long base(Cube cube, int direction, long[] moment) {
        switch (direction) {

            case 2:
                return -moment[indexify(cube.R0, cube.G1, cube.B1)]
                        + moment[indexify(cube.R0, cube.G1, cube.B0)]
                        + moment[indexify(cube.R0, cube.G0, cube.B1)]
                        - moment[indexify(cube.R0, cube.G0, cube.B0)];

            case 1:
                return -moment[indexify(cube.R1, cube.G0, cube.B1)]
                        + moment[indexify(cube.R1, cube.G0, cube.B0)]
                        + moment[indexify(cube.R0, cube.G0, cube.B1)]
                        - moment[indexify(cube.R0, cube.G0, cube.B0)];

            case 0:
                return -moment[indexify(cube.R1, cube.G1, cube.B0)]
                        + moment[indexify(cube.R1, cube.G0, cube.B0)]
                        + moment[indexify(cube.R0, cube.G1, cube.B0)]
                        - moment[indexify(cube.R0, cube.G0, cube.B0)];

            default:
                return 0;
        }
    }

    private static long findTop(Cube cube, int direction, int position, long[] moment) {
        switch (direction) {
            case 2:
                return moment[indexify(position, cube.G1, cube.B1)]
                        - moment[indexify(position, cube.G1, cube.B0)]
                        - moment[indexify(position, cube.G0, cube.B1)]
                        + moment[indexify(position, cube.G0, cube.B0)];

            case 1:
                return moment[indexify(cube.R1, position, cube.B1)]
                        - moment[indexify(cube.R1, position, cube.B0)]
                        - moment[indexify(cube.R0, position, cube.B1)]
                        + moment[indexify(cube.R0, position, cube.B0)];

            case 0:
                return moment[indexify(cube.R1, cube.G1, position)]
                        - moment[indexify(cube.R1, cube.G0, position)]
                        - moment[indexify(cube.R0, cube.G1, position)]
                        + moment[indexify(cube.R0, cube.G0, position)];
            default:
                return 0;
        }
    }

    private void histogram(int[][] image) {
        int mm = 8 - IndexBits;
        int h = image.length;
        int w = image[0].length;
        int[] temp;
        int r, g, b;

        for (int y = 0; y < h; y++) {
            temp = image[y];
            for (int x = 0; x < w; x++) {
                int val = temp[x];
                r = (val >> 16) & 0xff;
                g = (val >> 8) & 0xff;
                b = val & 0xff;
                int inr = r >> mm;
                int ing = g >> mm;
                int inb = b >> mm;

                int ind = indexify(inr + 1, ing + 1, inb + 1);

                vwt[ind]++;
                vmr[ind] += r;
                vmg[ind] += g;
                vmb[ind] += b;
                m2[ind] += (r * r) + (g * g) + (b * b);
            }
        }
    }

    private void M3d() {
        long[] area, areaR, areaG, areaB;
        double[] areaTemp;

        for (int r = 1; r < IndexCount; r++) {
            area = new long[IndexCount];
            areaR = new long[IndexCount];
            areaG = new long[IndexCount];
            areaB = new long[IndexCount];
            areaTemp = new double[IndexCount];

            for (int g = 1; g < IndexCount; g++) {
                long line = 0;
                long line_r = 0;
                long line_g = 0;
                long line_b = 0;
                double line2 = 0;

                for (int b = 1; b < IndexCount; b++) {
                    int ind1 = indexify(r, g, b);

                    line += vwt[ind1];
                    line_r += vmr[ind1];
                    line_g += vmg[ind1];
                    line_b += vmb[ind1];
                    line2 += m2[ind1];

                    area[b] += line;
                    areaR[b] += line_r;
                    areaG[b] += line_g;
                    areaB[b] += line_b;
                    areaTemp[b] += line2;

                    int ind2 = ind1 - indexify(1, 0, 0);

                    vwt[ind1] = vwt[ind2] + area[b];
                    vmr[ind1] = vmr[ind2] + areaR[b];
                    vmg[ind1] = vmg[ind2] + areaG[b];
                    vmb[ind1] = vmb[ind2] + areaB[b];
                    m2[ind1] = m2[ind2] + areaTemp[b];
                }
            }
        }
    }

    private double variance(Cube cube) {
        double dr = volume(cube, vmr);
        double dg = volume(cube, vmg);
        double db = volume(cube, vmb);

        double xx = m2[indexify(cube.R1, cube.G1, cube.B1)]
                - m2[indexify(cube.R1, cube.G1, cube.B0)]
                - m2[indexify(cube.R1, cube.G0, cube.B1)]
                + m2[indexify(cube.R1, cube.G0, cube.B0)]
                - m2[indexify(cube.R0, cube.G1, cube.B1)]
                + m2[indexify(cube.R0, cube.G1, cube.B0)]
                + m2[indexify(cube.R0, cube.G0, cube.B1)]
                - m2[indexify(cube.R0, cube.G0, cube.B0)];

        return xx - (((dr * dr) + (dg * dg) + (db * db)) / volume(cube, vwt));
    }

    private Object[] maximize(Cube cube, int direction, int first, int last, double whole_r, double whole_g, double whole_b, double whole_w) {
        long base_r = base(cube, direction, vmr);
        long base_g = base(cube, direction, vmg);
        long base_b = base(cube, direction, vmb);
        long base_w = base(cube, direction, vwt);

        double max = 0.0;
        int cut = -1;

        for (int i = first; i < last; i++) {
            double half_r = base_r + findTop(cube, direction, i, vmr);
            double half_g = base_g + findTop(cube, direction, i, vmg);
            double half_b = base_b + findTop(cube, direction, i, vmb);
            double half_w = base_w + findTop(cube, direction, i, vwt);

            double temp;

            if (half_w == 0) {
                continue;
            } else {
                temp = ((half_r * half_r) + (half_g * half_g) + (half_b * half_b)) / half_w;
            }

            half_r = whole_r - half_r;
            half_g = whole_g - half_g;
            half_b = whole_b - half_b;
            half_w = whole_w - half_w;

            if (half_w == 0) {
                continue;
            } else {
                temp += ((half_r * half_r) + (half_g * half_g) + (half_b * half_b)) / half_w;
            }

            if (temp > max) {
                max = temp;
                cut = i;
            }
        }

        return new Object[]{cut, max};
    }

    private boolean cut(Cube set1, Cube set2) {
        double whole_r = volume(set1, vmr);
        double whole_g = volume(set1, vmg);
        double whole_b = volume(set1, vmb);
        double whole_w = volume(set1, vwt);

        Object[] temp;

        temp = maximize(set1, 2, set1.R0 + 1, set1.R1, whole_r, whole_g, whole_b, whole_w);
        int cutr = (Integer) temp[0];
        double maxr = (Double) temp[1];

        temp = maximize(set1, 1, set1.G0 + 1, set1.G1, whole_r, whole_g, whole_b, whole_w);
        int cutg = (Integer) temp[0];
        double maxg = (Double) temp[1];

        temp = maximize(set1, 0, set1.B0 + 1, set1.B1, whole_r, whole_g, whole_b, whole_w);
        int cutb = (Integer) temp[0];
        double maxb = (Double) temp[1];

        int dir;

        if ((maxr >= maxg) && (maxr >= maxb)) {
            dir = 2;

            if (cutr < 0) {
                return false;
            }
        } else if ((maxg >= maxr) && (maxg >= maxb)) {
            dir = 1;
        } else {
            dir = 0;
        }

        set2.R1 = set1.R1;
        set2.G1 = set1.G1;
        set2.B1 = set1.B1;

        switch (dir) {
            case 2:
                set2.R0 = set1.R1 = cutr;
                set2.G0 = set1.G0;
                set2.B0 = set1.B0;
                break;

            case 1:
                set2.G0 = set1.G1 = cutg;
                set2.R0 = set1.R0;
                set2.B0 = set1.B0;
                break;

            case 0:
                set2.B0 = set1.B1 = cutb;
                set2.R0 = set1.R0;
                set2.G0 = set1.G0;
                break;
        }

        set1.Volume = (set1.R1 - set1.R0) * (set1.G1 - set1.G0) * (set1.B1 - set1.B0);
        set2.Volume = (set2.R1 - set2.R0) * (set2.G1 - set2.G0) * (set2.B1 - set2.B0);

        return true;
    }

//    private void mark(Cube cube, byte label, byte[] tag) {
//        for (int r = cube.R0 + 1; r <= cube.R1; r++) {
//            for (int g = cube.G0 + 1; g <= cube.G1; g++) {
//                for (int b = cube.B0 + 1; b <= cube.B1; b++) {
//                    tag[indexify(r, g, b)] = label;
//                }
//            }
//        }
//    }

    private void buildCube(Cube[] cube, int colorCount) {

        double[] vv = new double[colorCount];

        for (int i = 0; i < colorCount; i++) {
            cube[i] = new Cube();
        }

        cube[0].R0 = cube[0].G0 = cube[0].B0 = 0;
        cube[0].R1 = cube[0].G1 = cube[0].B1 = IndexCount - 1;

        int next = 0;

        for (int i = 1; i < colorCount; i++) {
            if (cut(cube[next], cube[i])) {
                vv[next] = cube[next].Volume > 1 ? variance(cube[next]) : 0.0;
                vv[i] = cube[i].Volume > 1 ? variance(cube[i]) : 0.0;
            } else {
                vv[next] = 0.0;
                i--;
            }

            next = 0;

            double temp = vv[0];
            for (int k = 1; k <= i; k++) {
                if (vv[k] > temp) {
                    temp = vv[k];
                    next = k;
                }
            }

            if (temp <= 0.0) {
                break;
            }
        }
    }

//    public Object[] quantize(int[][] image) {
//        int colorCount = 256;
//        histogram(image);
//        M3d();
//        Cube[] cube = new Cube[colorCount];
//        buildCube(cube, colorCount);
//        return generateResult(image, colorCount, cube);
//    }
//    
//    private Object[] generateResult(int[][] image, int colorCount, Cube[] cube) {
//        byte[] palette = new byte[256 * 3];
//
//        int z = 0;
//        for (int k = 0; k < colorCount; k++) {
//            mark(cube[k], (byte) k, tag);
//            double weight = volume(cube[k], vwt);
//            if (weight != 0) {
//                palette[z++] = (byte) (volume(cube[k], vmr) / weight);
//                palette[z++] = (byte) (volume(cube[k], vmg) / weight);
//                palette[z++] = (byte) (volume(cube[k], vmb) / weight);
//
//            } else {
//                palette[z++] = 0;
//                palette[z++] = 0;
//                palette[z++] = 0;
//            }
//        }
//
//        byte[] indexedBytes = new byte[image.length / 3];
//        z = 0;
//        int mm = 8 - IndexBits;
//        int ii = image.length / 3;
//        for (int i = 0; i < ii; i++) {
//            int b = (image[z++] & 0xff) >> mm;
//            int g = (image[z++] & 0xff) >> mm;
//            int r = (image[z++] & 0xff) >> mm;
//
//            int ind = indexify(r + 1, g + 1, b + 1);
//
//            indexedBytes[i] = tag[ind];
//        }
//
//        return new Object[]{palette, indexedBytes};
//
//    }
    private class Cube {

        int R0, R1, G0, G1, B0, B1, Volume;
    }

}
