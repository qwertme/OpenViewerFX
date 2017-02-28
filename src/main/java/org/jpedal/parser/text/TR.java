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
 * TR.java
 * ---------------
 */
package org.jpedal.parser.text;

import org.jpedal.objects.GraphicsState;


public class TR {



    public static int execute(final int key, final GraphicsState gs) {

        int value=key;

        //Text render mode
        switch (key) {

            case 0:
                value = GraphicsState.FILL;
                break;

            case 1:
                value = GraphicsState.STROKE;
                break;

            case 2:
                value = GraphicsState.FILLSTROKE;
                break;

            case 3:
                value = GraphicsState.INVISIBLE;

                //allow user to over-ride
                if (Tj.showInvisibleText) {
                    value = GraphicsState.FILL;
                }

                break;

            case 7:
                value = GraphicsState.CLIPTEXT;
                break;

        }
        gs.setTextRenderType(value);

        return value;
    }


}
