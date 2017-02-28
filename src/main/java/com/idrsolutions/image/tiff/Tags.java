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
 * Tags.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

public class Tags {

    public static final int NewSubfileType = 0xfe;
    public static final int SubfileType = 0xff;
    public static final int ImageWidth = 0x100;
    public static final int ImageHeight = 0x101;
    public static final int BitsPerSample = 0x102;
    public static final int Compression = 0x103;

    public static final int Uncompressed = 1;
    public static final int CCITT_ID = 2;
    public static final int Group_3_Fax = 3;
    public static final int Group_4_Fax = 4;
    public static final int LZW = 5;
    public static final int JPEG = 6;
    public static final int JPEG_TechNote = 7;
    public static final int ADOBEDEFLATE = 8;
    public static final int PackBits = 32773;
    public static final int Deflate = 0x80b2;

    public static final int PhotometricInterpolation = 0x106;

    public static final int WhiteIsZero = 0;
    public static final int BlackIsZero = 1;
    public static final int RGB = 2;
    public static final int RGB_Palette = 3;
    public static final int Transparency_Mask = 4;
    public static final int CMYK = 5;
    public static final int YCbCr = 6;
    public static final int CIELab = 8;

    public static final int Threshholding = 0x107;
    public static final int CellWidth = 0x108;
    public static final int CellLength = 0x109;
    public static final int FillOrder = 0x10a;
    public static final int DocumentName = 0x10D;
    public static final int ImageDescription = 0x10e;
    public static final int Make = 0x10f;
    public static final int Model = 0x110;
    public static final int StripOffsets = 0x111;
    public static final int Orientation = 0x112;
    public static final int SamplesPerPixel = 0x115;
    public static final int RowsPerStrip = 0x116;
    public static final int StripByteCounts = 0x117;
    public static final int MinSampleValue = 0x118;
    public static final int MaxSampleValue = 0x119;
    public static final int Xresolution = 0x11a;
    public static final int Yresolution = 0x11b;
    public static final int PlanarConfiguration = 0x11c;
    public static final int PageName = 0x11d;
    public static final int Xposition = 0x11e;
    public static final int Yposition = 0x11f;
    public static final int FreeOffsets = 0x120;
    public static final int FreeByteCounts = 0x121;
    public static final int GrayResponseUnit = 0x122;
    public static final int GrayResponseCurve = 0x123;
    public static final int T4Options = 0x124;
    public static final int T6Options = 0x125;
    public static final int ResolutionUnit = 0x128;
    public static final int PageNumber = 0x129;
    public static final int TransferFunction = 0x12D;
    public static final int Software = 0x131;
    public static final int DateTime = 0x132;
    public static final int Artist = 0x13b;
    public static final int HostComputer = 0x13c;
    public static final int Predictor = 0x13d;
    public static final int WhitePoint = 0x13e;
    public static final int PrimaryChromaticities = 0x13f;
    public static final int ColorMap = 0x140;
    public static final int HalftoneHints = 0x141;
    public static final int TileWidth = 0x142;
    public static final int TileLength = 0x143;
    public static final int TIleOffsets = 0x144;
    public static final int TIleByteCounts = 0x145;
    public static final int BadFaxLines = 0x146;
    public static final int CleanFaxData = 0x147;
    public static final int ConsecutiveBadFaxLines = 0x148;
    public static final int SubIFDs = 0x14a;
    public static final int InkSet = 0x14c;
    public static final int InkNames = 0x14d;
    public static final int NumberOfInks = 0x14e;
    public static final int DotRange = 0x150;
    public static final int TargetPrinter = 0x151;
    public static final int ExtraSamples = 0x152;
    public static final int SampleFormat = 0x153;
    public static final int SMinSampleValue = 0x154;
    public static final int SMaxSampleValue = 0x155;
    public static final int TransferRange = 0x156;
    public static final int ClipPath = 0x157;
    public static final int XClipPathUnits = 0x158;
    public static final int YClipPathUnits = 0x159;
    public static final int Indexed = 0x15a;
    public static final int JPEGTables = 0x15b;

    public static final int JPEGProc = 0x200;
    public static final int JPEGInterchangeFormat = 0x201;
    public static final int JPEGInterchangeFormatLength = 0x202;
    public static final int JPEGRestartInterval = 0x203;
    public static final int JPEGLosslessPredictors = 0x205;
    public static final int JPEGPointTransforms = 0x206;
    public static final int JPEGQTables = 0x207;
    public static final int JPEGDCTables = 0x208;
    public static final int JPEGACTables = 0x209;
    public static final int YCbCrCoefficients = 0x211;
    public static final int YCbCrSubSampling = 0x212;
    public static final int YCbCrPositioning = 0x213;
    public static final int ReferenceBlackWhite = 0x214;
    public static final int StripRowCounts = 0x22f;
    public static final int XMP = 0x2bc;

    public static final int ImageID = 0x800d;

    public static final int Copyright = 0x8298;

    public static final int ICC = 0x8773;
    public static final int Exif_IFD = 0x8769;
    public static final int ExifVersion = 0x9000;
    public static final int DateTimeOriginal = 0x9003;
    public static final int DateTimeDigitized = 0x9004;
    public static final int ComponentConfiguration = 0x9101;
    public static final int CompressedBitsPerPixel = 0x9102;
    public static final int ApertureValue = 0x9202;
    public static final int ImageNumber = 0x9211;
    public static final int ImageHistory = 0x9213;
    public static final int ColorSpace = 0xa0001;
    public static final int PixelXDimension = 0xa002;
    public static final int PixelYDimension = 0xa003;

}
