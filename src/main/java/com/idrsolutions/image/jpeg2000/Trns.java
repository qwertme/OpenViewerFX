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
 * Trns.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

import java.util.List;

/**
 * Class performs inverse discrete wavelet transformation in JPEG2000 data
 */
public class Trns {
    
    private static final float alpha = -1.5861343421f;
    private static final float beta = -0.0529801186f;
    private static final float gamma = 0.8829110755f;
    private static final float delta = 0.4435068520f;
    private static final float zeta = 1.2301741049f;
    private static final float zeta_ = 1.0f / zeta;

    private final boolean reversible;

    public Trns(boolean reversible) {
        this.reversible = reversible;
    }

    public SubbandCoefficient getInversed(List<SubbandCoefficient> subCos, int u0, int v0) {
        SubbandCoefficient sb = subCos.get(0);
        for (int i = 1; i < subCos.size(); i++) {
            sb = getNext(sb, subCos.get(i), u0, v0);
        }
        return sb;
    }

    private static void applyFilter(float[] data, int offset, int size, boolean reversible) {
        
        int aa = offset - 1, bb = offset + 1;
        int cc = offset + size - 2, dd = offset + size;
        data[aa--] = data[bb++];
        data[dd++] = data[cc--];
        data[aa--] = data[bb++];
        data[dd++] = data[cc--];
        data[aa--] = data[bb++];
        data[dd++] = data[cc--];
        data[aa] = data[bb];
        data[dd] = data[cc];

        int len = size >> 1;

        if (reversible) {
            int n = len;
            int j = offset;
            while (n >= 0) {
                data[j] -= ((int) (data[j - 1] + data[j + 1] + 2)) >> 2;
                j += 2;
                n--;
            }
            n = len - 1;
            j = offset + 1;
            while (n >= 0) {
                data[j] += ((int) (data[j - 1] + data[j + 1])) >> 1;
                j += 2;
                n--;
            }

        } else {
            float current, next;

            int j = offset - 3;
            for (int n = len + 4; n > 0; n--, j += 2) {
                data[j] *= zeta_;
            }

            j = offset - 2;
            current = delta * data[j - 1];
            int n = len + 2;
            while (n >= 0) {
                next = delta * data[j + 1];
                data[j] = zeta * data[j] - current - next;
                n--;
                if (n >= 0) {
                    j += 2;
                    current = delta * data[j + 1];
                    data[j] = zeta * data[j] - current - next;
                } else {
                    break;
                }
                j += 2;
                n--;
            }

            j = offset - 1;
            current = gamma * data[j - 1];
            n = len + 1;
            while (n >= 0) {
                next = gamma * data[j + 1];
                data[j] -= current + next;
                n--;
                if (n >= 0) {
                    j += 2;
                    current = gamma * data[j + 1];
                    data[j] -= current + next;
                } else {
                    break;
                }
                j += 2;
                n--;
            }

            j = offset;
            current = beta * data[j - 1];
            n = len;
            while (n >= 0) {
                next = beta * data[j + 1];
                data[j] -= current + next;
                n--;
                if (n >= 0) {
                    j += 2;
                    current = beta * data[j + 1];
                    data[j] -= current + next;
                } else {
                    break;
                }
                j += 2;
                n--;
            }

            if (len != 0) {
                j = offset + 1;
                current = alpha * data[j - 1];
                n = len - 1;
                while (n >= 0) {
                    next = alpha * data[j + 1];
                    data[j] -= current + next;
                    n--;
                    if (n >= 0) {
                        j += 2;
                        current = alpha * data[j + 1];
                        data[j] -= current + next;
                    } else {
                        break;
                    }
                    j += 2;
                    n--;
                }
            }
        }
    }

    private SubbandCoefficient getNext(SubbandCoefficient ll, SubbandCoefficient other, int u0, int v0) {

        int widthLL = ll.width;
        int heightLL = ll.height;

        int width = other.width;
        int height = other.height;
//        float[] ff = other.floatItems;

        int k = 0;
        for (int i = 0; i < heightLL; i++) {
            int l = i * 2 * width;
            for (int j = 0; j < widthLL; j++) {
                other.floatItems[l] = ll.floatItems[k];
                k++;
                l += 2;
            }
        }
        
        int bufferPadding = 4;
        float[] rowBuffer = new float[width + 2 * bufferPadding];

        if (width == 1) {
            if ((u0 & 1) != 0) {
                k = 0;
                for (int v = 0; v < height; v++) {
                    other.floatItems[k] *= 0.5;
                    k += width;
                }
            }
        } else {
            k = 0;
            for (int v = 0; v < height; v++) {
                System.arraycopy(other.floatItems, k, rowBuffer, bufferPadding, width);

                applyFilter(rowBuffer, bufferPadding, width, reversible);

                System.arraycopy(rowBuffer, bufferPadding, other.floatItems, k, width);
                k += width;
            }
        }

        int numBuffers = 16;
        float[][] colBuffers = new float[numBuffers][height + 2 * bufferPadding];

        int currentBuffer = 0;
        int ss = bufferPadding + height;

        if (height == 1) {
            if ((v0 & 1) != 0) {
                for (int u = 0; u < width; u++) {
                    other.floatItems[u] *= 0.5;
                }
            }
        } else {
            for (int u = 0; u < width; u++) {
                if (currentBuffer == 0) {
                    numBuffers = Math.min(width - u, numBuffers);
                    k = u;
                    for (int l = bufferPadding; l < ss; l++) {
                        for (int b = 0; b < numBuffers; b++) {
                            colBuffers[b][l] = other.floatItems[k + b];
                        }
                        k += width;
                    }
                    currentBuffer = numBuffers;
                }

                currentBuffer--;
                float[] buffer = colBuffers[currentBuffer];
                applyFilter(buffer, bufferPadding, height, reversible);

                if (currentBuffer == 0) {
                    k = u - numBuffers + 1;
                    for (int l = bufferPadding; l < ss; k += width, l++) {
                        for (int b = 0; b < numBuffers; b++) {
                            other.floatItems[k + b] = colBuffers[b][l];
                        }
                    }
                }
            }
        }

        SubbandCoefficient sc = new SubbandCoefficient();
        sc.width = width;
        sc.height = height;
        sc.floatItems = other.floatItems;
        
        return sc;
    }

}
