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
 * Overlays.java
 * ---------------
 */
package org.jpedal.display;

import org.jpedal.exception.PdfException;
import org.jpedal.render.DynamicVectorRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public class Overlays {

    private final Map overlayType = new HashMap();
    private final Map overlayColors = new HashMap();
    private final Map overlayObj = new HashMap();

    private final Map overlayTypeG = new HashMap();
    private final Map overlayColorsG = new HashMap();
    private final Map overlayObjG = new HashMap();

    public void printAdditionalObjectsOverPage(final int page, final int[] type, final Color[] colors, final Object[] obj) throws PdfException {


        final Integer key = page;

        if (obj == null) { //flush page

            overlayType.remove(key);
            overlayColors.remove(key);
            overlayObj.remove(key);

        } else { //store for printing and add if items already there



            final int[] oldType = (int[]) overlayType.get(key);
            if (oldType == null){
                overlayType.put(key, type);

            }else { //merge items

                final int oldLength = oldType.length;
                final int newLength = type.length;
                final int[] combined = new int[oldLength + newLength];

                System.arraycopy(oldType, 0, combined, 0, oldLength);

                System.arraycopy(type, 0, combined, oldLength, newLength);

                overlayType.put(key, combined);
            }


            final Color[] oldCol = (Color[]) overlayColors.get(key);
            if (oldCol == null) {
                overlayColors.put(key, colors);
            } else { //merge items

                final int oldLength = oldCol.length;
                final int newLength = colors.length;
                final Color[] combined = new Color[oldLength + newLength];

                System.arraycopy(oldCol, 0, combined, 0, oldLength);

                System.arraycopy(colors, 0, combined, oldLength, newLength);

                overlayColors.put(key, combined);
            }

            final Object[] oldObj = (Object[]) overlayObj.get(key);

            if (oldType == null) {
                overlayObj.put(key, obj);
            } else { //merge items

                final int oldLength = oldObj.length;
                final int newLength = obj.length;
                final Object[] combined = new Object[oldLength + newLength];

                System.arraycopy(oldObj, 0, combined, 0, oldLength);

                System.arraycopy(obj, 0, combined, oldLength, newLength);

                overlayObj.put(key, combined);
            }
        }
    }

    public void printAdditionalObjectsOverAllPages(final int[] type, final Color[] colors, final Object[] obj) throws PdfException {

        final Integer key = -1;

        if (obj == null) { //flush page

            overlayTypeG.remove(key);
            overlayColorsG.remove(key);
            overlayObjG.remove(key);

        } else { //store for printing and add if items already there

            final int[] oldType = (int[]) overlayTypeG.get(key);
            if (oldType == null){
                overlayTypeG.put(key, type);

            }else { //merge items

                final int oldLength = oldType.length;
                final int newLength = type.length;
                final int[] combined = new int[oldLength + newLength];

                System.arraycopy(oldType, 0, combined, 0, oldLength);

                System.arraycopy(type, 0, combined, oldLength, newLength);

                overlayTypeG.put(key, combined);
            }


            final Color[] oldCol = (Color[]) overlayColorsG.get(key);
            if (oldCol == null) {
                overlayColorsG.put(key, colors);
            } else { //merge items

                final int oldLength = oldCol.length;
                final int newLength = colors.length;
                final Color[] combined = new Color[oldLength + newLength];

                System.arraycopy(oldCol, 0, combined, 0, oldLength);

                System.arraycopy(colors, 0, combined, oldLength, newLength);

                overlayColorsG.put(key, combined);
            }

            final Object[] oldObj = (Object[]) overlayObjG.get(key);

            if (oldType == null) {
                overlayObjG.put(key, obj);
            } else { //merge items

                final int oldLength = oldObj.length;
                final int newLength = obj.length;
                final Object[] combined = new Object[oldLength + newLength];

                System.arraycopy(oldObj, 0, combined, 0, oldLength);

                System.arraycopy(obj, 0, combined, oldLength, newLength);

                overlayObjG.put(key, combined);
            }
        }
    }
    public void clear() {

        //flush arrays
        overlayType.clear();
        overlayColors.clear();
        overlayObj.clear();

        //flush arrays
        overlayTypeG.clear();
        overlayColorsG.clear();
        overlayObjG.clear();
    }

    public void printOverlays(final DynamicVectorRenderer dvr, final int page) throws PdfException {

        //store for printing (global first)
        final Integer keyG = -1;
        final int[] typeG = (int[]) overlayTypeG.get(keyG);
        final Color[] colorsG = (Color[]) overlayColorsG.get(keyG);
        final Object[] objG = (Object[]) overlayObjG.get(keyG);

        //add to screen display
        dvr.drawAdditionalObjectsOverPage(typeG, colorsG, objG);

        //store for printing
        final Integer key = page;

        final int[] type = (int[]) overlayType.get(key);
        final Color[] colors = (Color[]) overlayColors.get(key);
        final Object[] obj = (Object[]) overlayObj.get(key);

        //add to screen display
        dvr.drawAdditionalObjectsOverPage(type, colors, obj);
    }
}
