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
 * Cmd.java
 * ---------------
 */
package org.jpedal.parser;

/**
 * holds int value for every postscript command
 */
public class Cmd {

    public static final int Tc = 21603;

    public static final int Tw = 21623;

    public static final int Tz = 21626;

    public static final int TL = 21580;

    public static final int Tf = 21606;

    public static final int Tr = 21618;

    public static final int Ts = 21619;

    public static final int Td = 21604;

    public static final int TD = 21572;

    public static final int Tm = 21613;

    public static final int Tstar = 21546;

    public static final int Tj = 21610;

    public static final int TJ = 21578;

    public static final int quote = 39;

    public static final int doubleQuote = 34;

    //////////////////////// /

    protected static final int BI = 16969;

    private static final int ID = 18756;

    protected static final int m = 109;

    protected static final int l = 108;

    protected static final int c = 99;

    protected static final int d = 100;

    protected static final int v = 118;

    protected static final int y = 121;

    protected static final int h = 104;

    protected static final int re = 29285;

    public static final int S = 83;

    protected static final int s = 115;

    protected static final int f = 102;

    public static final int F = 70;

    protected static final int fstar = 26154;

    public static final int Fstar = 17962;

    public static final int B = 66;

    protected static final int Bstar = 16938;

    protected static final int b = 98;

    protected static final int bstar = 25130;

    public static final int n = 110;

    protected static final int W = 87;

    protected static final int Wstar = 22314;

    //////////////////////// /

    public static final int BT = 16980;

    public static final int ET = 17748;

    protected static final int Do = 17519;

    protected static final int w = 119;

    protected static final int j = 106;

    protected static final int J = 74;

    protected static final int M = 77;

    private static final int ri = 29289;

    protected static final int i = 105;

    protected static final int gs = 26483;

    protected static final int q = 113;

    protected static final int Q = 81;

    protected static final int cm = 25453;

    protected static final int d0 = 25648;

    protected static final int d1 = 25649;

    protected static final int cs = 25459;

    protected static final int CS = 17235;

    protected static final int sc = 29539;

    protected static final int scn = 7562094;

    protected static final int SC = 21315;

    protected static final int SCN = 5456718;

    protected static final int g = 103;

    protected static final int G = 71;

    protected static final int rg = 29287;

    protected static final int RG = 21063;

    protected static final int k = 107;

    protected static final int K = 75;

    private static final int sh = 29544;

    public static final int BMC = 4345155;

    public static final int BDC = 4342851;

    public static final int EMC = 4541763;

    public static final int MP = 19792;

    public static final int DP = 17488;

    private static final int BX = 16984;

    private static final int EX = 17752;

    public static final int TEXT_COMMAND = 0;

    public static final int COLOR_COMMAND = 1;

    public static final int SHAPE_COMMAND = 2;

    public static final int SHADING_COMMAND = 3;

    public static final int GS_COMMAND = 4;

    public static final int IMAGE_COMMAND = 5;

    public static final int T3_COMMAND = 6;

    public static int getCommandType(final int commandID){

        int type=-1;

        switch(commandID){
            case Cmd.BDC :
                type=TEXT_COMMAND;
                break;
            case Cmd.BMC :
                type=TEXT_COMMAND;
                break;
            case Cmd.BT :
                type=TEXT_COMMAND;
                break;
            case Cmd.DP :
                type=TEXT_COMMAND;
                break;
            case Cmd.EMC :
                type=TEXT_COMMAND;
                break;
            case Cmd.ET :
                type=TEXT_COMMAND;
                break;
            case Cmd.MP :
                type=TEXT_COMMAND;
                break;
            case Cmd.Tc :
                type=TEXT_COMMAND;
                break;
            case Cmd.Tw :
                type=TEXT_COMMAND;
                break;
            case Cmd.Tz :
                type=TEXT_COMMAND;
                break;
            case Cmd.TL :
                type=TEXT_COMMAND;
                break;
            case Cmd.Tf :
                type=TEXT_COMMAND;
                break;
            case Cmd.Tr :
                type=TEXT_COMMAND;
                break;
            case Cmd.Ts :
                type=TEXT_COMMAND;
                break;
            case Cmd.TD :
                type=TEXT_COMMAND;
                break;
            case Cmd.Td :
                type=TEXT_COMMAND;
                break;
            case Cmd.Tm :
                type=TEXT_COMMAND;
                break;
            case Cmd.Tstar :
                type=TEXT_COMMAND;
                break;
            case Cmd.Tj :
                type=TEXT_COMMAND;
                break;
            case Cmd.TJ :
                type=TEXT_COMMAND;
                break;
            case Cmd.quote :
                type=TEXT_COMMAND;
                break;
            case Cmd.doubleQuote :
                type=TEXT_COMMAND;
                break;

            case Cmd.rg :
                type=COLOR_COMMAND;
                break;
            case Cmd.RG :
                type=COLOR_COMMAND;
                break;
            case Cmd.SCN :
                type=COLOR_COMMAND;
                break;
            case Cmd.scn :
                type=COLOR_COMMAND;
                break;
            case Cmd.SC :
                type=COLOR_COMMAND;
                break;
            case Cmd.sc :
                type=COLOR_COMMAND;
                break;
            case Cmd.cs :
                type=COLOR_COMMAND;
                break;
            case Cmd.CS :
                type=COLOR_COMMAND;
                break;
            case Cmd.g :
                type=COLOR_COMMAND;
                break;
            case Cmd.G :
                type=COLOR_COMMAND;
                break;
            case Cmd.k :
                type=COLOR_COMMAND;
                break;
            case Cmd.K :
                type=COLOR_COMMAND;
                break;

            case Cmd.sh:
                type=SHADING_COMMAND;
                break;

            /**
             * shapes
             */
            case Cmd.B :
                type=SHAPE_COMMAND;
                break;
            case Cmd.b :
                type=SHAPE_COMMAND;
                break;
            case Cmd.bstar :
                type=SHAPE_COMMAND;
                break;
            case Cmd.Bstar :
                type=SHAPE_COMMAND;
                break;

            case Cmd.c :
                type=SHAPE_COMMAND;
                break;

            case Cmd.d :
                type=SHAPE_COMMAND;
                break;

            case Cmd.F :
                type=SHAPE_COMMAND;
                break;

            case Cmd.f :
                type=SHAPE_COMMAND;
                break;

            case Cmd.Fstar :
                type=SHAPE_COMMAND;
                break;

            case Cmd.fstar :
                type=SHAPE_COMMAND;
                break;

            case Cmd.h :
                type=SHAPE_COMMAND;
                break;

            case Cmd.i:
                type=SHAPE_COMMAND;
                break;

            case Cmd.J :
                type=SHAPE_COMMAND;
                break;

            case Cmd.j :
                type=SHAPE_COMMAND;
                break;

            case Cmd.l :
                type=SHAPE_COMMAND;
                break;

            case Cmd.M:
                type=SHAPE_COMMAND;
                break;

            case Cmd.m :
                type=SHAPE_COMMAND;
                break;

            case Cmd.n :
                type=SHAPE_COMMAND;
                break;

            case Cmd.re :
                type=SHAPE_COMMAND;
                break;

            case Cmd.S :
                type=SHAPE_COMMAND;
                break;
            case Cmd.s :
                type=SHAPE_COMMAND;
                break;

            case Cmd.v :
                type=SHAPE_COMMAND;
                break;

            case Cmd.w:
                type=SHAPE_COMMAND;
                break;

            case Cmd.Wstar :
                type=SHAPE_COMMAND;
                break;

            case Cmd.W :
                type=SHAPE_COMMAND;
                break;
            case Cmd.y :
                type=SHAPE_COMMAND;
                break;


            case Cmd.cm :
                type=GS_COMMAND;
                break;

            case Cmd.Do :
                type=IMAGE_COMMAND;
                break;

            case Cmd.q :
                type=GS_COMMAND;
                break;

            case Cmd.Q :
                type=GS_COMMAND;
                break;

            case Cmd.BI:
                type=IMAGE_COMMAND;
                break;

            case Cmd.ID :
                type=IMAGE_COMMAND;
                break;

            case Cmd.gs :
                type=GS_COMMAND;
                break;

            case Cmd.d0 :
                type=T3_COMMAND;
                break;

            case Cmd.d1 :
                type=T3_COMMAND;
                break;
        }

        return type;

    }

    /**identify if command*/
    protected static int getCommandID(final int value) {
        int id = -1;

        switch (value) {

            case Tc:
                id = Tc;
                break;
            case Tw:
                id = Tw;
                break;
            case Tz:
                id = Tz;
                break;
            case TL:
                id = TL;
                break;
            case Tf:
                id = Tf;
                break;
            case Tr:
                id = Tr;
                break;
            case Ts:
                id = Ts;
                break;
            case Td:
                id = Td;
                break;
            case TD:
                id = TD;
                break;
            case Tm:
                id = Tm;
                break;
            case Tstar:
                id = Tstar;
                break;
            case Tj:
                id = Tj;
                break;
            case TJ:
                id = TJ;
                break;
            case quote:
                id = quote;
                break;
            case doubleQuote:
                id = doubleQuote;
                break;

            //////////////////////// /

            case BI:
                id = BI;
                break;
            case ID:
                id = ID;
                break;
            case m:
                id = m;
                break;
            case l:
                id = l;
                break;
            case c:
                id = c;
                break;
            case d:
                id = d;
                break;
            case v:
                id = v;
                break;
            case y:
                id = y;
                break;
            case h:
                id = h;
                break;
            case re:
                id = re;
                break;
            case S:
                id = S;
                break;
            case s:
                id = s;
                break;
            case f:
                id = f;
                break;
            case F:
                id = F;
                break;
            case fstar:
                id = fstar;
                break;
            case Fstar:
                id = Fstar;
                break;
            case B:
                id = B;
                break;
            case Bstar:
                id = Bstar;
                break;
            case b:
                id = b;
                break;
            case bstar:
                id = bstar;
                break;
            case n:
                id = n;
                break;
            case W:
                id = W;
                break;
            case Wstar:
                id = Wstar;
                break;

            //////////////////////// /

            case BT:
                id = BT;
                break;
            case ET:
                id = ET;
                break;
            case Do:
                id = Do;
                break;
            case w:
                id = w;
                break;
            case j:
                id = j;
                break;
            case J:
                id = J;
                break;
            case M:
                id = M;
                break;
            case ri:
                id = ri;
                break;
            case i:
                id = i;
                break;
            case gs:
                id = gs;
                break;
            case q:
                id = q;
                break;
            case Q:
                id = Q;
                break;
            case cm:
                id = cm;
                break;
            case d0:
                id = d0;
                break;
            case d1:
                id = d1;
                break;
            case cs:
                id = cs;
                break;
            case CS:
                id = CS;
                break;
            case sc:
                id = sc;
                break;
            case scn:
                id = scn;
                break;
            case SC:
                id = SC;
                break;
            case SCN:
                id = SCN;
                break;
            case g:
                id = g;
                break;
            case G:
                id = G;
                break;
            case rg:
                id = rg;
                break;
            case RG:
                id = RG;
                break;
            case k:
                id = k;
                break;
            case K:
                id = K;
                break;
            case sh:
                id = sh;
                break;
            case BMC:
                id = BMC;
                break;
            case BDC:
                id = BDC;
                break;
            case EMC:
                id = EMC;
                break;
            case MP:
                id = MP;
                break;
            case DP:
                id = DP;
                break;
            case BX:
                id = BX;
                break;
            case EX:
                id = EX;
                break;

            //////////////////////// /
        }

        return id;
    }

    /**convert command into string*/
    protected static String getCommandAsString(final int value) {
        String id = "";

        switch (value) {

            case Tc:
                id = "Tc";
                break;
            case Tw:
                id = "Tw";
                break;
            case Tz:
                id = "Tz";
                break;
            case TL:
                id = "TL";
                break;
            case Tf:
                id = "Tf";
                break;
            case Tr:
                id = "Tr";
                break;
            case Ts:
                id = "Ts";
                break;
            case Td:
                id = "Td";
                break;
            case TD:
                id = "TD";
                break;
            case Tm:
                id = "Tm";
                break;
            case Tstar:
                id = "Tstar";
                break;
            case Tj:
                id = "Tj";
                break;
            case TJ:
                id = "TJ";
                break;
            case quote:
                id = "'";
                break;
            case doubleQuote:
                id = "\"";
                break;

            ////////////////////////	 	 	/

            case BI:
                id = "BI";
                break;
            case ID:
                id = "ID";
                break;
            case m:
                id = "m";
                break;
            case l:
                id = "l";
                break;
            case c:
                id = "c";
                break;
            case d:
                id = "d";
                break;
            case v:
                id = "v";
                break;
            case y:
                id = "y";
                break;
            case h:
                id = "h";
                break;
            case re:
                id = "re";
                break;
            case S:
                id = "S";
                break;
            case s:
                id = "s";
                break;
            case f:
                id = "f";
                break;
            case F:
                id = "F";
                break;
            case fstar:
                id = "f*";
                break;
            case Fstar:
                id = "F*";
                break;
            case B:
                id = "B";
                break;
            case Bstar:
                id = "B*";
                break;
            case b:
                id = "b";
                break;
            case bstar:
                id = "b*";
                break;
            case n:
                id = "n";
                break;
            case W:
                id = "W";
                break;
            case Wstar:
                id = "W*";
                break;

            ////////////////////////	 	 	/

            case BT:
                id = "BT";
                break;
            case ET:
                id = "ET";
                break;
            case Do:
                id = "Do";
                break;
            case w:
                id = "w";
                break;
            case j:
                id = "j";
                break;
            case J:
                id = "J";
                break;
            case M:
                id = "M";
                break;
            case ri:
                id = "ri";
                break;
            case i:
                id = "i";
                break;
            case gs:
                id = "gs";
                break;
            case q:
                id = "q";
                break;
            case Q:
                id = "Q";
                break;
            case cm:
                id = "cm";
                break;
            case d0:
                id = "d0";
                break;
            case d1:
                id = "d1";
                break;
            case cs:
                id = "cs";
                break;
            case CS:
                id = "CS";
                break;
            case sc:
                id = "sc";
                break;
            case scn:
                id = "scn";
                break;
            case SC:
                id = "SC";
                break;
            case SCN:
                id = "SCN";
                break;
            case g:
                id = "g";
                break;
            case G:
                id = "G";
                break;
            case rg:
                id = "rg";
                break;
            case RG:
                id = "RG";
                break;
            case k:
                id = "k";
                break;
            case K:
                id = "K";
                break;
            case sh:
                id = "sh";
                break;
            case BMC:
                id = "BMC";
                break;
            case BDC:
                id = "BDC";
                break;
            case EMC:
                id = "EMC";
                break;
            case MP:
                id = "MP";
                break;
            case DP:
                id = "DP";
                break;
            case BX:
                id = "BX";
                break;
            case EX:
                id = "EX";
                break;

            ////////////////////////	 	 	/

        }

        return id;
    }

}
