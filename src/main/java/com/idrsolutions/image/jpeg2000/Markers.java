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
 * Markers.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 * class contains markers for jpeg2000
 */
public class Markers {

    //delimiting
    public static final int SOC = 0xff4f; //start of code stream
    public static final int SOT = 0xff90; //start of tile
    public static final int SOD = 0xff93; //start of data
    public static final int EOC = 0xffd9; //end of codestream

    //fixed info
    public static final int SIZ = 0xff51; //image and tile size
    
    //functional 
    public static final int COD = 0xff52; //coding style default
    public static final int COC = 0xff53; //coding style component
    public static final int RGN = 0xff5e; //region of interest
    public static final int QCD = 0xff5c; //quantization default
    public static final int QCC = 0xff5d; //quantization component
    public static final int POC = 0xff5f; //progression order change
    
    //pointer
    public static final int TLM = 0xff55; //tile length main
    public static final int PLM = 0xff57; //packet length main
    public static final int PLT = 0xff58; //packet length tile 
    public static final int PPM = 0xff60; //packed packet main 
    public static final int PPT = 0xff61; //packed packet tile
    
    //bitstream
    public static final int SOP = 0xff91; //start of packet
    public static final int EPH = 0xff92; //end packet header

    //informational
    public static final int COM = 0xff64; //comment and extension
    
    //OP parameter in cod marker
    public static final int LRCP = 0;
    public static final int RLCP = 1;
    public static final int RPCL = 2;
    public static final int PCRL = 3;
    public static final int CPRL = 4;
    
    public static final int CRG = 0xff63;
    

}
