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
 * Boxes.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 * JPEG2000 main box segments
 */
public class Boxes {

    /**
     *
     */
    public static final int JP = 0x6A502020;

    /**
     *
     */
    public static final int FTYP = 0x66747970;

    /**
     *
     */
    public static final int JP2H = 0x6A703268;

    /**
     *
     */
    public static final int JP2C = 0x6A703263;

    /**
     *
     */
    public static final int JP2I = 0x64703269;

    /**
     *
     */
    public static final int XML = 0x786D6C20;

    /**
     *
     */
    public static final int UUID = 0x75756964;

    /**
     *
     */
    public static final int UINF = 0x75696e66;
    
    //A BRAND FOR JP2

    /**
     *
     */
        public static final int JP2 = 0x6a703220;

    //HEADERS

    /**
     *
     */
        public static final int IHDR = 0x69686472;

    /**
     *
     */
    public static final int BPCC = 0x62706363;

    /**
     *
     */
    public static final int COLR = 0x636f6c72;

    /**
     *
     */
    public static final int PCLR = 0x70636c72;

    /**
     *
     */
    public static final int CMAP = 0x636d6170;

    /**
     *
     */
    public static final int CDEF = 0x63646566;

    /**
     *
     */
    public static final int RES = 0x72657320;

    //RESOLUTION SUPER BOX

    /**
     *
     */
        public static final int RESC = 0x72657363;

    /**
     *
     */
    public static final int RESD = 0x72657364;

    //UUID INFO

    /**
     *
     */
        public static final int ULST = 0x75637374;

    /**
     *
     */
    public static final int URL = 0x75726c20;

    /**
     *
     */
    public static final int IMB_VERS = 0x0100;

    /**
     *
     */
    public static final int IMB_C = 7;

    /**
     *
     */
    public static final int IMB_UnkC = 1;

    /**
     *
     */
    public static final int IMB_IPR = 0;
    
    
}
