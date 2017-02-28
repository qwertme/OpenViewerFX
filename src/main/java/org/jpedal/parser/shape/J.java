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
 * J.java
 * ---------------
 */
package org.jpedal.parser.shape;

import java.awt.BasicStroke;
import org.jpedal.objects.GraphicsState;

public class J {

    public static void execute(final boolean isLowerCase, final int value, final GraphicsState gs) {

        int style = 0;
        if (isLowerCase) {

            //map join style
            if (value == 0) {
                style = BasicStroke.JOIN_MITER;
            }
            if (value == 1) {
                style = BasicStroke.JOIN_ROUND;
            }
            if (value == 2) {
                style = BasicStroke.JOIN_BEVEL;
            }

            //set value
            gs.setJoinStyle(style);
        } else {
            //map cap style
            if (value == 0) {
                style = BasicStroke.CAP_BUTT;
            }
            if (value == 1) {
                style = BasicStroke.CAP_ROUND;
            }
            if (value == 2) {
                style = BasicStroke.CAP_SQUARE;
            }

            //set value
            gs.setCapStyle(style);
        }
    }   
}
