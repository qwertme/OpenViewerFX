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
 * CCITTMix.java
 * ---------------
 */
package org.jpedal.io.filter.ccitt;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**handle case with mix of CCITT1D and 2D*/
public class CCITTMix extends CCITT2D implements CCITTDecoder {

    private int fillBits;

    public CCITTMix(final byte[] rawData, final int width, final int height, final PdfObject decodeParms) {

        super(rawData, width, height, decodeParms);

        this.data=rawData;

        is2D =false;

    }

    @Override
    public byte[] decode(){

        try {

            /** Added to stop errors*/
            int[] prev = new int[width + 1];
            int[] curr = new int[width + 1];

            final int[] currentChangeElement = new int[2];

            // The data must start with an EOL code
            if( readEOL( true ) != 1 ) {
                throw new PdfException("TIFFFaxDecoder3");
            }

            //always 1D at first
            decode1DRun(curr);

            //rest of lines either 1 or 2D
            for( int lines = 1;lines < height;lines++ ){

                // Every line must begin with an EOL followed by a bit which
                // indicates whether the following scanline is 1D or 2D encoded.
                if( readEOL( false ) == 0 ){

                    //swap
                    final int[] temp = prev;
                    prev = curr;
                    curr = temp;

                    set2D(prev, curr, changingElemSize,currentChangeElement);

                    curr[currIndex++] = bitOffset;
                    changingElemSize = currIndex;
                }else{
                    decode1DRun(curr);
                }
            }
        } catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        //put it all together
        final byte[] buffer= createOutputFromBitset();

        //by default blackIs1 is false so black pixels do not need to be set
        // invert image if needed -
        // ultimately will be quicker to add into decode
        if (!BlackIs1) {
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = (byte) (255 - buffer[i]);
            }
        }

        return buffer;
    }

    void decode1DRun(final int[] curr) throws Exception
    {
        int bitOffset=0;

        int bits, code, isT;
        int current, entry, twoBits;
        boolean isWhite = true;

        // Initialize starting of the changing elements array
        changingElemSize = 0;

        while( bitOffset < columns ){
            while( isWhite ){
                // White run (lookup entry in data and use this to fetch white value from lookuptable
                current = get1DBits( 10 );

                bitReached += 10;

                entry = white[current];

                // Get the 3 fields from the entry
                isT = entry & 0x0001;
                bits = ( entry >>> 1 ) & 0x0f;
                if( bits == 12 ){ // Additional Make up code
                    // Get the next 2 bits
                    twoBits = get1DBits( 2 );

                    bitReached += 2;

                    // Consolidate the 2 new bits and last 2 bits into 4 bits
                    current = ( ( current << 2 ) & 0x000c ) | twoBits;
                    entry = additionalMakeup[current];
                    bits = ( entry >>> 1 ) & 0x07; // 3 bits 0000 0111
                    code = ( entry >>> 4 ) & 0x0fff; // 12 bits
                    bitOffset += code; // Skip white run

                    outPtr += code;

                    bitReached -= (4 - bits);
                }else if( bits == 0 || bits == 15){ // ERROR
                    throw new Exception(( "1Derror" ) );
                }else{
                    code = ( entry >>> 5 ) & 0x07ff;
                    bitOffset += code;
                    bitReached -= (10 - bits);
                    if( isT == 0 ){
                        isWhite = false;
                        curr[changingElemSize++] = bitOffset;
                    }
                    outPtr += code;
                }
            }

            if( bitOffset == columns ) {
                break;
            }

            while(!isWhite){
                // Black run
                current = get1DBits( 4 );
                entry = initBlack[current];

                bitReached += 4;

                // Get the fields from the entry
                bits = ( entry >>> 1 ) & 0x000f;
                code = ( entry >>> 5 ) & 0x07ff;
                if( code == 100 ){
                    current = get1DBits( 9 );

                    bitReached += 9;

                    entry = black[current];

                    // Get the 3 fields from the entry
                    isT = entry & 0x0001;
                    bits = ( entry >>> 1 ) & 0x000f;
                    code = ( entry >>> 5 ) & 0x07ff;

                    if( bits == 12 ){
                        // Additional makeup codes
                        bitReached -= 5;
                        current = get1DBits( 4 );

                        bitReached += 4;

                        entry = additionalMakeup[current];
                        bits = ( entry >>> 1 ) & 0x07; // 3 bits 0000 0111
                        code = ( entry >>> 4 ) & 0x0fff; // 12 bits

                        out.set(outPtr,outPtr+ code,true);
                        outPtr += code;

                        bitOffset += code;
                        bitReached -= (4 - bits);
                    }else if( bits == 15 )
                        // EOL code
                    {
                        throw new PdfException(("1D error"));
                    } else{
                        out.set(outPtr,outPtr+ code,true);
                        outPtr += code;
                        bitOffset += code;
                        bitReached -= (9 - bits);
                        if( isT == 0 ){
                            isWhite = true;
                            curr[changingElemSize++] = bitOffset;
                        }
                    }
                }else if( code == 200 ){
                    // Is a Terminating code
                    current = get1DBits( 2 );

                    bitReached += 2;

                    entry = twoBitBlack[current];
                    code = ( entry >>> 5 ) & 0x07ff;
                    bits = ( entry >>> 1 ) & 0x0f;
                    out.set(outPtr,outPtr+ code,true);
                    outPtr += code;
                    bitOffset += code;
                    bitReached -= (2 - bits);
                    isWhite = true;
                    curr[changingElemSize++] = bitOffset;
                }else{
                    // Is a Terminating code
                    out.set(outPtr,outPtr+ code,true);
                    outPtr += code;
                    bitOffset += code;
                    bitReached -= (4 - bits);
                    isWhite = true;
                    curr[changingElemSize++] = bitOffset;
                }
            }

            // Check whether this run completed one width
            if( bitOffset == columns ) {
                break;
            }

        }
        curr[changingElemSize++] = bitOffset;
    }

    private int readEOL( final boolean isFirstEOL ) throws PdfException{

        if( fillBits == 0 ){

            //Byte align if necessary
            if (isByteAligned && bitReached % 8 != 0) {
                bitReached += 8-(bitReached%8);
            }

            final int next12Bits = get1DBits( 12 );

            bitReached += 12;
            if( isFirstEOL && next12Bits == 0 ){

                final int aa=get1DBits( 4 );

                bitReached += 4;

                if( aa == 1 ){ // EOL must be padded: reset the fillBits flag.
                    fillBits = 1;
                    bitReached++;
                    return 1;
                }
            }
            if( next12Bits != 1 ) {
                throw new PdfException(("EOL error1"));
            }
        }else if( fillBits == 1 ){

            final int bitsLeft = 8 - (bitReached & 7);

            final int rr=get1DBits( bitsLeft );

            bitReached += bitsLeft;

            if( rr != 0 ) {
                throw new PdfException(("EOL error2"));
            }

            if( bitsLeft < 4 ){
                final int rr2=get1DBits( 8 );

                bitReached += 8;

                if( rr2 != 0 ) {
                    throw new PdfException("EOL error3");
                }
            }

            int n= get1DBits( 8 );
            bitReached += 8;

            while( n != 1 ){

                // If not all zeros
                if( n != 0 ) {
                    throw new PdfException("EOL error4");
                }

                n = get1DBits( 8 );
                bitReached += 8;
            }
        }

        final int r= get1DBits( 1 );

        bitReached += 1;
        return r;
    }
}

