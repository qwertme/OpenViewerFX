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
 * EMC.java
 * ---------------
 */
package org.jpedal.parser.text;

import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.ParserOptions;
import org.jpedal.render.DynamicVectorRenderer;

public class EMC {

    public static void execute(final DynamicVectorRenderer current, final GraphicsState gs, final ParserOptions parserOptions) {

        //remove any clip
        if(parserOptions.layerClips.contains(parserOptions.getLayerLevel())){
            gs.setClippingShape(null);
            current.drawClip(gs,null,true);
        }

        parserOptions.setLayerLevel(parserOptions.getLayerLevel()-1);

        //reset flag
        boolean flag=(parserOptions.layers == null || parserOptions.getLayerLevel() == 0 || parserOptions.getLayerVisibility().contains(parserOptions.getLayerLevel()));

        parserOptions.setIsLayerVisible(flag);
    }

}
