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
 * PrintStreamDecoder.java
 * ---------------
 */

package org.jpedal.parser;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.print.PrinterException;
import org.jpedal.*;
import org.jpedal.exception.PdfException;
import org.jpedal.external.*;
import org.jpedal.fonts.glyph.T3Size;
import org.jpedal.objects.raw.PdfObject;

/**
 * allow printing of XFA and PDF
 */
public interface PrintStreamDecoder {

    void print(Graphics2D g2, AffineTransform scaling, int currentPrintPage,
               Rectangle userAnnot, CustomPrintHintingHandler customPrintHintingHandler, PdfDecoderInt pdf) throws PrinterException ;
    
    void setObjectValue(int key, Object obj);

    void setParameters(boolean b, boolean b0, int i, int i0, boolean isPrinting, boolean b1);

    void setIntValue(int PageNum, int page);

    boolean getBooleanValue(int PageDecodingSuccessful);

    @SuppressWarnings("UnusedReturnValue")
    T3Size decodePageContent(PdfObject pdfObject) throws PdfException;
    
    Object getObjectValue(int key);

    ErrorTracker getErrorTracker();
}
