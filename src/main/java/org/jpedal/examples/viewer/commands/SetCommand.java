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
 * SetCommand.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.geom.AffineTransform;
import java.util.StringTokenizer;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.exception.PdfException;
import org.jpedal.gui.GUIFactory;

/**
 *
 */
public class SetCommand {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final Values commonValues) {
        if (args == null) {
            final String viewportWindow = currentGUI.showInputDialog("Entry the co-ordinates of the viewport, as x y width height");

            if (viewportWindow != null) {
                final StringTokenizer tok = new StringTokenizer(viewportWindow);
                if (tok.countTokens() == 4) {

                    final int x = Integer.parseInt(tok.nextToken());
                    final int y = Integer.parseInt(tok.nextToken());
                    final int w = Integer.parseInt(tok.nextToken());
                    final int h = Integer.parseInt(tok.nextToken());

                    //set and save scale values
                    try {
                        final AffineTransform scalingUsed = decode_pdf.getPages().setViewableArea(new int[]{x, y, w, h});
                        commonValues.dx = (int) scalingUsed.getTranslateX();
                        commonValues.dy = (int) scalingUsed.getTranslateY();
                        commonValues.maxViewY = h;
                        commonValues.viewportScale = scalingUsed.getScaleX();

                    } catch (final PdfException e1) {
                        System.err.println("setViewportError = " + e1.getMessage());
                        e1.printStackTrace();
                        commonValues.maxViewY = 0; //used as flag so ensure reset
                        decode_pdf.resetViewableArea();
                    }
                }
            }
        } else {

        }
    }
}
