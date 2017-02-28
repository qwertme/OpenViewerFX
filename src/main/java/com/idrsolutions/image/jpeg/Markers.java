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
 * -----------------
 * Markers.java
 * -----------------
 */
package com.idrsolutions.image.jpeg;

/**
 * Class contains Jpeg Markers
 */
public class Markers {

    /**
     *
     */
    public static final int TEM = 0xff01;

    /**
     *
     */
    public static final int RST0 = 0xffd0;

    /**
     *
     */
    public static final int RST1 = 0xffd1;

    /**
     *
     */
    public static final int RST2 = 0xffd2;

    /**
     *
     */
    public static final int RST3 = 0xffd3;

    /**
     *
     */
    public static final int RST4 = 0xffd4;

    /**
     *
     */
    public static final int RST5 = 0xffd5;

    /**
     *
     */
    public static final int RST6 = 0xffd6;

    /**
     *
     */
    public static final int RST7 = 0xffd7;

    /**
     * Start of image
     */
    public static final int SOI = 0xffd8;

    /**
     * End of image
     */
    public static final int EOI = 0xffd9;

    /**
     * Start of frame baseline
     */
    public static final int SOF0 = 0xffc0;

    /**
     * Start of frame, extended sequential
     */
    public static final int SOF1 = 0xffc1;

    /**
     * Start of frame, progressive
     */
    public static final int SOF2 = 0xffc2;

    /**
     * Start of frame, lossless
     */
    public static final int SOF3 = 0xffc3;

    /**
     * Define Huffman table
     */
    public static final int DHT = 0xffc4;

    /**
     * Start of frame, differential sequential
     */
    public static final int SOF5 = 0xffc5;

    /**
     * Start of frame, differential progressive
     */
    public static final int SOF6 = 0xffc6;

    /**
     * Start of frame, differential lossless
     */
    public static final int SOF7 = 0xffc7;

    /**
     * reserved
     */
    public static final int JPG = 0xffc8;

    /**
     * Start of frame, extended sequential, arithmetic coding
     */
    public static final int SOF9 = 0xffc9;

    /**
     * Start of frame, progressive, arithmetic coding
     */
    public static final int SOF10 = 0xffca;

    /**
     * Start of frame, lossless, arithmetic coding
     */
    public static final int SOF11 = 0xffcb;

    /**
     * Define arithmetic coding conditions
     */
    public static final int DAC = 0xffcc;

    /**
     * Start of frame, differential sequential, arithmetic coding
     */
    public static final int SOF13 = 0xffcd;

    /**
     * Start of frame, differential progressive, arithmetic coding
     */
    public static final int SOF14 = 0xffce;

    /**
     * Start of frame, differential lossless, arithmetic coding
     */
    public static final int SOF15 = 0xffcf;

    /**
     * Start of scan
     */
    public static final int SOS = 0xffda;

    /**
     * Define quantization tables
     */
    public static final int DQT = 0xffdb;

    /**
     * Define number of lines
     */
    public static final int DNL = 0xffdc;

    /**
     * Define restart interval
     */
    public static final int DRI = 0xffdd;

    /**
     * Define hierarchical progression
     */
    public static final int DHP = 0xffde;

    /**
     * Expand reference components
     */
    public static final int EXP = 0xffdf;

    /**
     *
     */
    public static final int APP0 = 0xffe0;

    /**
     *
     */
    public static final int APP1 = 0xffe1;

    /**
     *
     */
    public static final int APP2 = 0xffe2;

    /**
     *
     */
    public static final int APP3 = 0xffe3;

    /**
     *
     */
    public static final int APP4 = 0xffe4;

    /**
     *
     */
    public static final int APP5 = 0xffe5;

    /**
     *
     */
    public static final int APP6 = 0xffe6;

    /**
     *
     */
    public static final int APP7 = 0xffe7;

    /**
     *
     */
    public static final int APP8 = 0xffe8;

    /**
     *
     */
    public static final int APP9 = 0xffe9;

    /**
     *
     */
    public static final int APP10 = 0xffea;

    /**
     *
     */
    public static final int APP11 = 0xffeb;

    /**
     *
     */
    public static final int APP12 = 0xffec;

    /**
     *
     */
    public static final int APP13 = 0xffed;

    /**
     *
     */
    public static final int APP14 = 0xffee;

    /**
     *
     */
    public static final int APP15 = 0xffef;

    /**
     *
     */
    public static final int JPG0 = 0xfff0;

    /**
     *
     */
    public static final int JPG1 = 0xfff1;

    /**
     *
     */
    public static final int JPG2 = 0xfff2;

    /**
     *
     */
    public static final int JPG3 = 0xfff3;

    /**
     *
     */
    public static final int JPG4 = 0xfff4;

    /**
     *
     */
    public static final int JPG5 = 0xfff5;

    /**
     *
     */
    public static final int JPG6 = 0xfff6;

    /**
     *
     */
    public static final int JPG7 = 0xfff7;

    /**
     *
     */
    public static final int JPG8 = 0xfff8;

    /**
     *
     */
    public static final int JPG9 = 0xfff9;

    /**
     *
     */
    public static final int JPG10 = 0xfffa;

    /**
     *
     */
    public static final int JPG11 = 0xfffb;

    /**
     *
     */
    public static final int JPG12 = 0xfffc;

    /**
     *
     */
    public static final int JPG13 = 0xfffd;

    /**
     * Comment
     */
    public static final int COM = 0xfffe;

}
