/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idrsolutions.pdf.color.blends;

import java.awt.CompositeContext;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.objects.raw.PdfDictionary;

/**
 *
 */
public class BlendContext implements CompositeContext {

//    private final float alpha;
    private final int blendMode;

    public BlendContext(int blendMode, float alpha) {
        this.blendMode = blendMode;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {

        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());

        int[] srcPixels = new int[width];
        int[] dstInPixels = new int[width];
        int[] dstOutPixels = new int[width];

        for (int y = 0; y < height; y++) {
            src.getDataElements(0, y, width, 1, srcPixels);
            dstIn.getDataElements(0, y, width, 1, dstInPixels);

            int oldS = 0;
            int oldD = 0;
            int oldR = 0;

            for (int x = 0; x < width; x++) {
                int s = srcPixels[x];
                int d = dstInPixels[x];

                if (s == oldS && d == oldD) {
                    dstOutPixels[x] = oldR;
                } else {
                    oldS = s;
                    oldD = d;
                    int[] sp = getRGBA(s);
                    int[] dp = getRGBA(d);
                    int[] result = new int[4];

//                    if (sp[3] != 255) { //transparency involved convert to rgb
////                        int r = sp[0] * sp[3] + dp[0] * (255 - sp[3]);
////                        int g = sp[1] * sp[3] + dp[1] * (255 - sp[3]);
////                        int b = sp[2] * sp[3] + dp[2] * (255 - sp[3]);
////                        sp = new int[]{r / 255, g / 255, b / 255, sp[3]};
////                        dp = new int[]{0,0,0,dp[3]};
//                        dp[3] = 255;
////                        sp[3] = 255;
////                        System.out.println(dp[0]+" "+dp[1]+" "+dp[2]+" "+dp[3]);
//                    }
                    switch (blendMode) {
                        case PdfDictionary.Normal:
                            break;
                        case PdfDictionary.Multiply:
                            result = doMultiply(sp, dp);
                            break;
                        case PdfDictionary.Screen:
                            result = doScreen(sp, dp);
                            break;
                        case PdfDictionary.Overlay:
                            result = doOverlay(sp, dp);
                            break;
                        case PdfDictionary.Darken:
                            result = doDarken(sp, dp);
                            break;
                        case PdfDictionary.Lighten:
                            result = doLighten(sp, dp);
                            break;
                        case PdfDictionary.ColorDodge:
                            result = doColorDodge(sp, dp);
                            break;
                        case PdfDictionary.ColorBurn:
                            result = doColorBurn(sp, dp);
                            break;
                        case PdfDictionary.HardLight:
                            result = doHardLight(sp, dp);
                            break;
                        case PdfDictionary.SoftLight:
                            result = doSoftLight(sp, dp);
                            break;
                        case PdfDictionary.Difference:
                            result = doDifference(sp, dp);
                            break;
                        case PdfDictionary.Exclusion:
                            result = doExclusion(sp, dp);
                            break;
                        case PdfDictionary.Hue:
                            result = doHue(sp, dp);
                            break;
                        case PdfDictionary.Saturation:
                            result = doSaturation(sp, dp);
                            break;
                        case PdfDictionary.Color:
                            result = doColor(sp, dp);
                            break;
                        case PdfDictionary.Luminosity:
                            result = doLuminosity(sp, dp);
                            break;
                        default:
                            break;
                    }

                    if (sp[3] != 255) {
                        double sr = result[0] / 255.0;
                        double sg = result[1] / 255.0;
                        double sb = result[2] / 255.0;
                        double sa = sp[3] / 255.0;

                        double dr = dp[0] / 255.0;
                        double dg = dp[1] / 255.0;
                        double db = dp[2] / 255.0;

                        sr = ((1 - sa) * dr) + (sa * sr);
                        sg = ((1 - sa) * dg) + (sa * sg);
                        sb = ((1 - sa) * db) + (sa * sb);

                        result[0] = (int) (sr * 255);
                        result[1] = (int) (sg * 255);
                        result[2] = (int) (sb * 255);

                    }

                    dstOutPixels[x] = oldR = (Math.min(255, sp[3] + dp[3]) << 24 | result[0] << 16 | result[1] << 8 | result[2]);

                }
//                dstOutPixels[x] = sp[3] << 24 | sp[0] << 16 | sp[1] << 8 | sp[2];
//                
            }
            dstOut.setDataElements(0, y, width, 1, dstOutPixels);
        }
    }

    private static int[] getRGBA(int argb) {
        return new int[]{(argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, (argb >> 24) & 0xff};
    }

    private static int[] doMultiply(int[] src, int[] dst) {
        return new int[]{(src[0] * dst[0]) >> 8, (src[1] * dst[1]) >> 8, (src[2] * dst[2]) >> 8};
    }

    private static int[] doScreen(int[] src, int[] dst) {
        
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        return new int[]{
            255 - ((255 - src[0]) * (255 - dst[0]) >> 8),
            255 - ((255 - src[1]) * (255 - dst[1]) >> 8),
            255 - ((255 - src[2]) * (255 - dst[2]) >> 8)
        };
    }

    private static int[] doOverlay(int[] src, int[] dst) {
//        return doHardLight(src, dst);
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        int[] result = new int[3];
        for (int i = 0; i < 3; i++) {
            double ss = dst[i] / 255.0;
            double dd = src[i] / 255.0;

            if (ss > 0.5) {
                ss = 2.0 * ss - 1;
                result[i] = (int) (255.0 * (ss + dd - (ss * dd)));

            } else {
                ss = 2 * ss;
                result[i] = (int) (255.0 * (ss * dd));
            }
        }
        return result;
    }

    private static int[] doHardLight(int[] src, int[] dst) {

        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }

        int[] result = new int[3];
        for (int i = 0; i < 3; i++) {
            double ss = src[i] / 255.0;
            double dd = dst[i] / 255.0;

            if (ss <= 0.5) {
                ss = 2 * ss;
                result[i] = (int) (255 * (ss * dd));

            } else {
                ss = 2 * ss - 1;
                result[i] = (int) (255 * (ss + dd - (ss * dd)));
            }
        }
        return result;
    }

    private static int[] doDarken(int[] src, int[] dst) {
        
        return new int[]{
            Math.min(src[0], dst[0]),
            Math.min(src[1], dst[1]),
            Math.min(src[2], dst[2])
        };
    }

    private static int[] doLighten(int[] src, int[] dst) {
        
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        return new int[]{
            Math.max(src[0], dst[0]),
            Math.max(src[1], dst[1]),
            Math.max(src[2], dst[2])
        };
    }

    private static int[] doColorDodge(int[] src, int[] dst) {
                
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        return new int[]{
            src[0] == 255 ? 255
            : Math.min((dst[0] << 8) / (255 - src[0]), 255),
            src[1] == 255 ? 255
            : Math.min((dst[1] << 8) / (255 - src[1]), 255),
            src[2] == 255 ? 255
            : Math.min((dst[2] << 8) / (255 - src[2]), 255)
        };
    }

    private static int[] doColorBurn(int[] src, int[] dst) {
                
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        return new int[]{
            src[0] == 0 ? 0
            : Math.max(0, 255 - (((255 - dst[0]) << 8) / src[0])),
            src[1] == 0 ? 0
            : Math.max(0, 255 - (((255 - dst[1]) << 8) / src[1])),
            src[2] == 0 ? 0
            : Math.max(0, 255 - (((255 - dst[2]) << 8) / src[2]))
        };
    }

    private static int[] doSoftLight(int[] src, int[] dst) {
                
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        int[] result = new int[3];
        for (int i = 0; i < 3; i++) {
            double ss = src[i] / 255.0;
            double dd = dst[i] / 255.0;

            if (ss <= 0.5) {
                result[i] = (int) (255 * (dd - (1 - 2 * ss) * dd * (1 - dd)));
            } else {
                if (dd > 0.25) {
                    result[i] = (int) (255 * (dd + (2 * ss - 1) * (Math.sqrt(dd) - dd)));
                } else {
                    result[i] = (int) (255 * (dd + (2 * ss - 1) * ((((16 * dd - 12) * dd + 4) * dd) - dd)));
                }
            }
        }
        return result;
    }

    private static int[] doDifference(int[] src, int[] dst) {
        
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        return new int[]{
            Math.abs(dst[0] - src[0]),
            Math.abs(dst[1] - src[1]),
            Math.abs(dst[2] - src[2])
        };
    }

    private static int[] doExclusion(int[] src, int[] dst) {
        
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        return new int[]{
            dst[0] + src[0] - (dst[0] * src[0] >> 7),
            dst[1] + src[1] - (dst[1] * src[1] >> 7),
            dst[2] + src[2] - (dst[2] * src[2] >> 7)
        };
    }

    private static int[] doColor(int[] src, int[] dst) {
        
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }

        int[] result = new int[3];
        double sr = src[0] / 255.0;
        double sg = src[1] / 255.0;
        double sb = src[2] / 255.0;

        double dr = dst[0] / 255.0;
        double dg = dst[1] / 255.0;
        double db = dst[2] / 255.0;

        double[] rgb = setLum(sr, sg, sb, lum(dr, dg, db));

        result[0] = (int) (255 * rgb[0]);
        result[1] = (int) (255 * rgb[1]);
        result[2] = (int) (255 * rgb[2]);

        return result;
    }

    private static int[] doLuminosity(int[] src, int[] dst) {
                
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }

        int[] result = new int[3];
        double sr = src[0] / 255.0;
        double sg = src[1] / 255.0;
        double sb = src[2] / 255.0;

        double dr = dst[0] / 255.0;
        double dg = dst[1] / 255.0;
        double db = dst[2] / 255.0;

        double[] rgb = setLum(dr, dg, db, lum(sr, sg, sb));

        result[0] = (int) (255 * rgb[0]);
        result[1] = (int) (255 * rgb[1]);
        result[2] = (int) (255 * rgb[2]);

        return result;
    }

    private static int[] doHue(int[] src, int[] dst) {
                
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        double[] srcHSL = new double[3];
        rgbToHSL(src[0], src[1], src[2], srcHSL);
        double[] dstHSL = new double[3];
        rgbToHSL(dst[0], dst[1], dst[2], dstHSL);

        int[] result = new int[4];
        hslToRGB(srcHSL[0], dstHSL[1], dstHSL[2], result);

        return result;
    }

    private static int[] doSaturation(int[] src, int[] dst) {
                
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        double[] srcHSL = new double[3];
        rgbToHSL(src[0], src[1], src[2], srcHSL);
        double[] dstHSL = new double[3];
        rgbToHSL(dst[0], dst[1], dst[2], dstHSL);

        int[] result = new int[4];
        hslToRGB(dstHSL[0], srcHSL[1], dstHSL[2], result);

        return result;
    }

    private static double lum(double r, double g, double b) {
        return 0.3 * r + 0.59 * g + 0.11 * b;
    }

    private static double[] setLum(double r, double g, double b, double l) {
        double d = l - lum(r, g, b);
        r += d;
        g += d;
        b += d;
        return clipColor(r, g, b);
    }

    private static double[] clipColor(double r, double g, double b) {
        double l = lum(r, g, b);
        double n = Math.min(Math.min(r, g), b);
        double x = Math.max(Math.max(r, g), b);
        if (n < 0.0) {
            r = l + (((r - l) * l) / (l - n));
            g = l + (((g - l) * l) / (l - n));
            b = l + (((b - l) * l) / (l - n));
        }
        if (x > 1.0) {
            r = l + (((r - l) * (1 - l)) / (x - l));
            g = l + (((g - l) * (1 - l)) / (x - l));
            b = l + (((b - l) * (1 - l)) / (x - l));
        }
        return new double[]{r, g, b};
    }

    private static void rgbToHSL(int r, int g, int b, double[] hsl) {
        double rr = (r / 255.0);
        double gg = (g / 255.0);
        double bb = (b / 255.0);

        double var_Min = Math.min(Math.min(rr, gg), bb);
        double var_Max = Math.max(Math.max(rr, gg), bb);
        double del_Max = var_Max - var_Min;

        double H, S, L;
        L = (var_Max + var_Min) / 2.0;

        if (del_Max - 0.01 <= 0.0) {
            H = 0;
            S = 0;
        } else {
            if (L < 0.5) {
                S = del_Max / (var_Max + var_Min);
            } else {
                S = del_Max / (2 - var_Max - var_Min);
            }

            double del_R = (((var_Max - rr) / 6.0) + (del_Max / 2.0)) / del_Max;
            double del_G = (((var_Max - gg) / 6.0) + (del_Max / 2.0)) / del_Max;
            double del_B = (((var_Max - bb) / 6.0) + (del_Max / 2.0)) / del_Max;

            if (rr == var_Max) {
                H = del_B - del_G;
            } else if (gg == var_Max) {
                H = (1 / 3f) + del_R - del_B;
            } else {
                H = (2 / 3f) + del_G - del_R;
            }
            if (H < 0) {
                H += 1;
            }
            if (H > 1) {
                H -= 1;
            }
        }

        hsl[0] = H;
        hsl[1] = S;
        hsl[2] = L;
    }

    private static void hslToRGB(double h, double s, double l, int[] rgb) {
        int R, G, B;

        if (s - 0.01 <= 0.0) {
            R = (int) (l * 255.0f);
            G = (int) (l * 255.0f);
            B = (int) (l * 255.0f);
        } else {
            double v1, v2;
            if (l < 0.5f) {
                v2 = l * (1 + s);
            } else {
                v2 = (l + s) - (s * l);
            }
            v1 = 2 * l - v2;

            R = (int) (255.0 * hueToRGB(v1, v2, h + (1.0 / 3.0)));
            G = (int) (255.0 * hueToRGB(v1, v2, h));
            B = (int) (255.0 * hueToRGB(v1, v2, h - (1.0 / 3.0)));
        }

        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;
    }

    private static double hueToRGB(double v1, double v2, double vH) {
        if (vH < 0.0) {
            vH += 1.0;
        }
        if (vH > 1.0) {
            vH -= 1.0;
        }
        if ((6.0 * vH) < 1.0) {
            return (v1 + (v2 - v1) * 6.0 * vH);
        }
        if ((2.0 * vH) < 1.0) {
            return v2;
        }
        if ((3.0 * vH) < 2.0) {
            return (v1 + (v2 - v1) * ((2.0 / 3.0) - vH) * 6.0);
        }
        return v1;
    }

//    private static double sat(double r, double g, double b) {
//        return (Math.max(Math.max(r, g), b) - Math.min(Math.min(r, g), b));
//    }    
//    private static double setSat(double r, double g, double b, double s) {
//
//    }
}
