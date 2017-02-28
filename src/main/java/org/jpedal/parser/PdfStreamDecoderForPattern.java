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
 * PdfStreamDecoderForPattern.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.exception.PdfException;
import org.jpedal.fonts.glyph.T3Size;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;

public class PdfStreamDecoderForPattern extends PdfStreamDecoder {

    public PdfStreamDecoderForPattern(final PdfObjectReader currentPdfFile) {
        super(currentPdfFile);

        streamType=ValueTypes.PATTERN;

    }

    /**
     *
     *  objects off the page, stitch into a stream and
     * decode and put into our data object. Could be altered
     * if you just want to read the stream
     * @param pageStream
     * @throws org.jpedal.exception.PdfException
     */
    @SuppressWarnings("UnusedReturnValue")
    public final T3Size decodePageContent(final GraphicsState newGS, final byte[] pageStream) throws PdfException {

        this.newGS=newGS;
        this.pageStream=pageStream;

        return decodePageContent(null);
    }
}
