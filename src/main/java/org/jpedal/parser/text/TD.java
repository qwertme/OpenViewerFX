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
 * TD.java
 * ---------------
 */
package org.jpedal.parser.text;

import org.jpedal.objects.TextState;

import org.jpedal.utils.Matrix;

public class TD {

    /**used to speed-up conversion of hex strings to numbers*/
    static final int[] multiply8={0,3,6,9,12,15};

    static final int[] multiply16={0,4,8,12,16,20,24,28,32,36,40};

    public static void execute(final boolean isLowerCase, final float x, final float y, final TextState currentTextState) {

        relativeMove(x, y,currentTextState);

        if (!isLowerCase) { //set leading as well
            final float TL = -y;
            currentTextState.setLeading(TL);
        }
    }

    /**
     * used by TD and T* to move current co-ord
     */
    public static void relativeMove(final float new_x, final float new_y, final TextState currentTextState) {

        //create matrix to update Tm
        final float[][] temp = new float[3][3];

        currentTextState.Tm = currentTextState.getTMAtLineStart();

        //set Tm matrix
        temp[0][0] = 1;
        temp[0][1] = 0;
        temp[0][2] = 0;
        temp[1][0] = 0;
        temp[1][1] = 1;
        temp[1][2] = 0;
        temp[2][0] = new_x;
        temp[2][1] = new_y;
        temp[2][2] = 1;

        //multiply to get new Tm
        currentTextState.Tm = Matrix.multiply(temp, currentTextState.Tm);

        currentTextState.setTMAtLineStart();

    }


    /**
     * get unicode/escape value and convert to value
     */
    static int readEscapeValue(final int start, final int count, final int base, final byte[] characterStream) {

        int val;

        switch(base) {
            case 8:
                val = getOctal(start, count, characterStream);
                break;

            case 16:
                val = getHex(start, count, characterStream);
                break;

            default:
                val = getGeneral(start, count, base, characterStream);
           break;
        }

        return val;
    }

    private static int getGeneral(int start, int count, int base, byte[] characterStream) {

        final StringBuilder chars = new StringBuilder(10);

        for (int pointer = 0; pointer < count; pointer++) {
            chars.append((char) characterStream[start + pointer]);
        }

        return Integer.parseInt(chars.toString(), base);

    }

    private static int getHex(int start, int count, byte[] characterStream) {

        int val=0;

        //now convert to value
        int topHex, ptr=0;
        for (int aa = 1; aa < count + 1; aa++) {

            topHex = characterStream[start + count - aa];

            //convert to number
            if (topHex >= 'A' && topHex <= 'F') {
                topHex -= 55;
            } else if (topHex >= 'a' && topHex <= 'f') {
                topHex -= 87;
            } else if (topHex >= '0' && topHex <= '9') {
                topHex -= 48;
            } else {    //ignore 'bum' values
                continue;
            }
            val += (topHex << multiply16[ptr]);
            ptr++;
        }
        return val;
    }

    private static int getOctal(int start, int count, byte[] characterStream) {

        //now convert to value
        int topHex, ptr=0, val=0;

        for (int aa = 1; aa < count + 1; aa++) {

            topHex = characterStream[start + count - aa];

            //convert to number
            if (topHex >= '0' && topHex <= '7') {
                topHex -= 48;
            } else {    //ignore 'bum' values
                continue;
            }
            val += (topHex << multiply8[ptr]);
            ptr++;
        }
        return val;
    }


    /**
     * convert to to String
     */
    static String getString(final int start,int end, final byte[] dataStream) {

        final String s;

        //lose spaces or returns at end
        while(dataStream[end]==32 || dataStream[end]==13 || dataStream[end]==10) {
            end--;
        }

        final int count=end-start+1;

        //discount duplicate spaces
        int spaces=0;
        for(int ii=0;ii<count;ii++){
            if(ii>0 &&(dataStream[start+ii]==32 || dataStream[start+ii]==13 ||dataStream[start+ii]==10)&&
                    (dataStream[start+ii-1]==32|| dataStream[start+ii-1]==13|| dataStream[start+ii-1]==10)) {
                spaces++;
            }
        }

        final char[] charString=new char[count-spaces];
        int pos=0;

        for(int ii=0;ii<count;ii++){
            if((ii>0)&&((dataStream[start+ii]==32)||(dataStream[start+ii]==13)||(dataStream[start+ii]==10))&&
                    ((dataStream[start+ii-1]==32)||(dataStream[start+ii-1]==13)||(dataStream[start+ii-1]==10)))
            {
            }else{
                if((dataStream[start+ii]==10)||(dataStream[start+ii]==13)) {
                    charString[pos] = ' ';
                } else {
                    charString[pos] = (char) dataStream[start + ii];
                }
                pos++;
            }
        }

        s=String.copyValueOf(charString);

        return s;

    }


}
