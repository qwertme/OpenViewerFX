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
 * ShowFormNames.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.PdfDecoderInt;
import org.jpedal.objects.acroforms.ReturnValues;

/**
 * Shows the names of all the forms of the current document
 */
public class ShowFormNames {

    public static void execute(final Object[] args, final PdfDecoderInt decode_pdf) {
        if (args == null) {

            final Object[] fieldNames = decode_pdf.getFormRenderer().getFormComponents(null, ReturnValues.FORM_NAMES, -1);
            final StringBuilder buf = new StringBuilder();
            buf.append("forms - \n");
            for (final Object fieldName : fieldNames) {
                buf.append(fieldName);
                buf.append('\n');
            }
            buf.append("END OF LIST");
            System.out.println(buf);

        }
    }
}
