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
 * ConvertToFloat.java
 * ---------------
 */
package org.jpedal.io.types;

public class ConvertToFloat {


    static float convert(final char[] stream) {

        final float d;

        final float dec;
        final float num;

        final int start = 0;
        final int charCount = stream.length;


        int ptr = charCount;
        int intStart = 0;
        boolean isMinus = false;
        //hand optimised float code
        //find decimal point
        for (int j = charCount - 1; j > -1; j--) {
            if (stream[start + j] == 46) { //'.'=46
                ptr = j;
                break;
            }
        }

        int intChars = ptr;
        //allow for minus
        if (stream[start] == 43) { //'+'=43
            intChars--;
            intStart++;
        } else if (stream[start] == 45) { //'-'=45
            intStart++;
            isMinus = true;
        }

        //optimisations
        final int intNumbers = intChars - intStart;
        final int decNumbers = charCount - ptr;

        if ((intNumbers > 4)) { //non-optimised to cover others
            isMinus = false;

            d = Float.parseFloat(new String(stream));

        } else {

            float thous = 0f, units = 0f, tens = 0f, hundreds = 0f, tenths = 0f, hundredths = 0f, thousands = 0f, tenthousands = 0f, hunthousands = 0f;

            //thousands
            if (intNumbers > 3) {
                thous = getThousands(stream[start + intStart] - 48);
                intStart++;
            }

            //hundreds
            if (intNumbers > 2) {
                hundreds = getHundreds(stream[start + intStart] - 48);
                intStart++;
            }

            //tens
            if (intNumbers > 1) {
                tens = getTens(stream[start + intStart] - 48);
                intStart++;
            }

            //units
            if (intNumbers > 0) {
                units = getUnits(stream[start + intStart] - 48);
            }

            //tenths
            if (decNumbers > 1) {
                ptr++; //move beyond.
                tenths = getTenths(stream[start + ptr] - 48);
            }

            //hundredths
            if (decNumbers > 2) {
                ptr++; //move beyond.
                hundredths = getHundredths(stream[start + ptr] - 48);
            }

            //thousands
            if (decNumbers > 3) {
                ptr++; //move beyond.
                thousands = getThousandths(stream[start + ptr] - 48);
            }

            //tenthousands
            if (decNumbers > 4) {
                ptr++; //move beyond.
                tenthousands = getTenThousandths(stream[start + ptr] - 48);
            }

//			q00thousands
            if (decNumbers > 5) {
                ptr++; //move beyond.
                hunthousands = getHundredThousandths(stream[start + ptr] - 48);
            }

            dec = tenths + hundredths + thousands + tenthousands + hunthousands;
            num = thous + hundreds + tens + units;
            d = num + dec;

        }

        if (isMinus) {
            return -d;
        } else {
            return d;
        }
    }

    private static float getHundredThousandths(int c) {

        float hunthousands=0;

        switch (c) {
            case 1:
                hunthousands = 0.00001f;
                break;
            case 2:
                hunthousands = 0.00002f;
                break;
            case 3:
                hunthousands = 0.00003f;
                break;
            case 4:
                hunthousands = 0.00004f;
                break;
            case 5:
                hunthousands = 0.00005f;
                break;
            case 6:
                hunthousands = 0.00006f;
                break;
            case 7:
                hunthousands = 0.00007f;
                break;
            case 8:
                hunthousands = 0.00008f;
                break;
            case 9:
                hunthousands = 0.00009f;
                break;
        }
        return hunthousands;
    }

    private static float getTenThousandths(int c) {

        float tenthousands=0;

        switch (c) {
            case 1:
                tenthousands = 0.0001f;
                break;
            case 2:
                tenthousands = 0.0002f;
                break;
            case 3:
                tenthousands = 0.0003f;
                break;
            case 4:
                tenthousands = 0.0004f;
                break;
            case 5:
                tenthousands = 0.0005f;
                break;
            case 6:
                tenthousands = 0.0006f;
                break;
            case 7:
                tenthousands = 0.0007f;
                break;
            case 8:
                tenthousands = 0.0008f;
                break;
            case 9:
                tenthousands = 0.0009f;
                break;
        }
        return tenthousands;
    }

    private static float getThousandths(int c) {

        float thousands=0;

        switch (c) {
            case 1:
                thousands = 0.001f;
                break;
            case 2:
                thousands = 0.002f;
                break;
            case 3:
                thousands = 0.003f;
                break;
            case 4:
                thousands = 0.004f;
                break;
            case 5:
                thousands = 0.005f;
                break;
            case 6:
                thousands = 0.006f;
                break;
            case 7:
                thousands = 0.007f;
                break;
            case 8:
                thousands = 0.008f;
                break;
            case 9:
                thousands = 0.009f;
                break;
        }
        return thousands;
    }

    private static float getHundredths(int c) {

        float hundredths=0;

        switch (c) {
            case 1:
                hundredths = 0.01f;
                break;
            case 2:
                hundredths = 0.02f;
                break;
            case 3:
                hundredths = 0.03f;
                break;
            case 4:
                hundredths = 0.04f;
                break;
            case 5:
                hundredths = 0.05f;
                break;
            case 6:
                hundredths = 0.06f;
                break;
            case 7:
                hundredths = 0.07f;
                break;
            case 8:
                hundredths = 0.08f;
                break;
            case 9:
                hundredths = 0.09f;
                break;
        }
        return hundredths;
    }

    private static float getTenths(int c) {

        float tenths=0;

        switch (c) {
            case 1:
                tenths = 0.1f;
                break;
            case 2:
                tenths = 0.2f;
                break;
            case 3:
                tenths = 0.3f;
                break;
            case 4:
                tenths = 0.4f;
                break;
            case 5:
                tenths = 0.5f;
                break;
            case 6:
                tenths = 0.6f;
                break;
            case 7:
                tenths = 0.7f;
                break;
            case 8:
                tenths = 0.8f;
                break;
            case 9:
                tenths = 0.9f;
                break;
        }
        return tenths;
    }

    private static float getUnits(int c) {

        float units=0;

        switch (c) {
            case 1:
                units = 1.0f;
                break;
            case 2:
                units = 2.0f;
                break;
            case 3:
                units = 3.0f;
                break;
            case 4:
                units = 4.0f;
                break;
            case 5:
                units = 5.0f;
                break;
            case 6:
                units = 6.0f;
                break;
            case 7:
                units = 7.0f;
                break;
            case 8:
                units = 8.0f;
                break;
            case 9:
                units = 9.0f;
                break;
        }
        return units;
    }

    private static float getTens(int c) {

        float tens=0;

        switch (c) {
            case 1:
                tens = 10.0f;
                break;
            case 2:
                tens = 20.0f;
                break;
            case 3:
                tens = 30.0f;
                break;
            case 4:
                tens = 40.0f;
                break;
            case 5:
                tens = 50.0f;
                break;
            case 6:
                tens = 60.0f;
                break;
            case 7:
                tens = 70.0f;
                break;
            case 8:
                tens = 80.0f;
                break;
            case 9:
                tens = 90.0f;
                break;
        }
        return tens;
    }

    private static float getHundreds(int c) {

        float hundreds=0;

        switch (c) {
            case 1:
                hundreds = 100.0f;
                break;
            case 2:
                hundreds = 200.0f;
                break;
            case 3:
                hundreds = 300.0f;
                break;
            case 4:
                hundreds = 400.0f;
                break;
            case 5:
                hundreds = 500.0f;
                break;
            case 6:
                hundreds = 600.0f;
                break;
            case 7:
                hundreds = 700.0f;
                break;
            case 8:
                hundreds = 800.0f;
                break;
            case 9:
                hundreds = 900.0f;
                break;
        }
        return hundreds;
    }

    private static float getThousands(int c) {

        float thous=0;

        switch (c) {
            case 1:
                thous = 1000.0f;
                break;
            case 2:
                thous = 2000.0f;
                break;
            case 3:
                thous = 3000.0f;
                break;
            case 4:
                thous = 4000.0f;
                break;
            case 5:
                thous = 5000.0f;
                break;
            case 6:
                thous = 6000.0f;
                break;
            case 7:
                thous = 7000.0f;
                break;
            case 8:
                thous = 8000.0f;
                break;
            case 9:
                thous = 9000.0f;
                break;
        }
        return thous;
    }
}
