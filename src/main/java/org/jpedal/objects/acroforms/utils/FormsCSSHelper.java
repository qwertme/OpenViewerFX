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
 * FormsCSSHelper.java
 * ---------------
 */

package org.jpedal.objects.acroforms.utils;

import java.awt.Color;
import java.awt.Rectangle;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.GUIData;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.output.FontMapper;
import org.jpedal.render.output.GenericFontMapper;

/**
 * Methods to create attribute values to share between JavaFX and CSS 
 * 
 * @author Simon
 */
public class FormsCSSHelper {
    
    /**
     * Gets text color as RGB
     * @param object
     * @return int[]{R,G,B}
     */
    public static int[] getTextColor(final FormObject object){
        final int[] rgb;
        if(object != null){
            rgb = getColor(object.getTextColor());
        }else{
            rgb=null;
        }
        
        return rgb;
    }
    /**
     * Gets background color as RGB
     * @param object
     * @return int[]{R,G,B}
     */
    public static int[] getBackgroundColor(final FormObject object){
        final int[] rgb;
        if(object != null){
            rgb = getColor(FormObject.generateColor(object.getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BG)));
        }else{
            rgb=null;
        }
        return rgb;
    }
    
    /**
     * Gets border color as RGB
     * @param object
     * @return int[]{R,G,B}
     */
    public static int[] getBorderColor(final FormObject object){
        final int[] rgb;
        if(object != null){
            rgb = getColor(FormObject.generateColor(object.getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BC)));
        }else{
            rgb=null;
        }
        return rgb;
    }
    
    public static int getBorderWidth(final FormObject object){
        final PdfObject BS = object.getDictionary(PdfDictionary.BS);

        int w=-1;
        if(BS!=null) {
            w = BS.getInt(PdfDictionary.W);
        }
        if(w<0) {
            w = 1;
        }
        
        return w;
    }
    
    private static int[] getColor(final Color color){
        final int[] rgb;
        
        if(color != null){
            final int col=color.getRGB();
            final int r=(col>>16) & 255;
            final int g=(col>>8) & 255;
            final int b=col & 255;
            rgb = new int[]{r,g,b};
        }else{
            rgb=null;
        }
        
        return rgb;
    }
    
    /**
     * Sets up the font CSS value as "[Weight] [Size] [family]"
     * @param formObject
     * @param area
     * @param isMultiLine
     * @param pageData
     * @param pageNum
     * @param scaling
     * @return 
     */
    public static String addFont(final FormObject formObject, final boolean area, final boolean isMultiLine, final PdfPageData pageData, final int pageNum, final float scaling){
        final String fontString;
        final FontMapper fontMapper=new GenericFontMapper(formObject.getFontName());

        final String font=fontMapper.getFont();
        final String weight =fontMapper.getWeight();

        float size = formObject.getTextSize();
        if(size==0 || size==-1){
            final Rectangle pos = formObject.getBoundingRectangle();

            if(isMultiLine){
                size=12;
                //rows= (int) (pos.height/size);
                //  System.out.println(rows+" "+pos.height);
            }else{
                final int rotation = pageData.getRotation(pageNum);
                if(rotation == 90 || rotation==270){
                    size = GUIData.calculateFontSize(pos.width,pos.height, area, formObject.getTextStreamValue(PdfDictionary.V));
                }else{
                    size = GUIData.calculateFontSize(pos.height, pos.width,area, formObject.getTextStreamValue(PdfDictionary.V));
                }
            }
        }

        //Removed as it made text smaller than it should be
        //if issue with text to large add this back in
        //factor in any pge scaling
        //float scaling=output.getScaling();///1.33f; //we need to divide by 1.33 to offset scaling on rest

        size *= scaling;
        
        fontString = String.format("%s %dpx %s", weight, (int)size, font);
        
        return fontString;
    }
}
