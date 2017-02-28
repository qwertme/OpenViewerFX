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
 * CS.java
 * ---------------
 */
package org.jpedal.parser.color;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.ColorspaceFactory;
import org.jpedal.color.DeviceRGBColorSpace;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.PatternColorSpace;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.ColorSpaceObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.PdfObjectCache;
import org.jpedal.utils.StringUtils;

/**
 *
 */
public class CS {
   
    public static void execute(final boolean isLowerCase, final String colorspaceObject, final GraphicsState gs, final PdfObjectCache cache, final PdfObjectReader currentPdfFile, final boolean isPrinting, final int pageNum, final PdfPageData pageData, final boolean alreadyUsed) {

        //set flag for stroke
        final boolean isStroke = !isLowerCase;

        /**
         * work out colorspace
         */
        PdfObject ColorSpace=(PdfObject)cache.get(PdfObjectCache.Colorspaces,colorspaceObject);

        if(ColorSpace==null) {
            ColorSpace = new ColorSpaceObject(StringUtils.toBytes(colorspaceObject));
        }

        final String ref=ColorSpace.getObjectRefAsString();
        final String ref2=ref+ '-'+isLowerCase;

        final GenericColorSpace newColorSpace;

        //(ms) 20090430 new code does not work so commented out

        //int ID=ColorSpace.getParameterConstant(PdfDictionary.ColorSpace);

        //        if(isLowerCase)
        //            System.out.println(" cs="+colorspaceObject+" "+alreadyUsed+" ref="+ref);
        //        else
        //            System.out.println(" CS="+colorspaceObject+" "+alreadyUsed+" ref="+ref);

        if(ColorSpace.getParameterConstant(PdfDictionary.ColorSpace)==PdfDictionary.Pattern && colorspaceObject.equals("Pattern")){
            
            newColorSpace= new PatternColorSpace(currentPdfFile, new DeviceRGBColorSpace());

        }else if(!alreadyUsed && cache.containsKey(PdfObjectCache.ColorspacesObjects, ref)){

            newColorSpace=(GenericColorSpace) cache.get(PdfObjectCache.ColorspacesObjects, ref);

            //reinitialise
            newColorSpace.reset();
        }else if(alreadyUsed && cache.containsKey(PdfObjectCache.ColorspacesObjects, ref2)){

            newColorSpace=(GenericColorSpace) cache.get(PdfObjectCache.ColorspacesObjects, ref2);

            //reinitialise
            newColorSpace.reset();
        }else{

            newColorSpace=ColorspaceFactory.getColorSpaceInstance(currentPdfFile, ColorSpace);

            newColorSpace.setPrinting(isPrinting);

            //use alternate as preference if CMYK
            //if(newColorSpace.getID()==ColorSpaces.ICC && ColorSpace.getParameterConstant(PdfDictionary.Alternate)==ColorSpaces.DeviceCMYK)
            //  newColorSpace=new DeviceCMYKColorSpace();

            //broken on calRGB so ignore at present
            //if(newColorSpace.getID()!=ColorSpaces.CalRGB)

            if((newColorSpace.getID()==ColorSpaces.ICC || newColorSpace.getID()==ColorSpaces.Separation)){
                //if(newColorSpace.getID()==ColorSpaces.Separation)

                if(ref.contains("-1")){ //ignore
                }else if(!alreadyUsed){
                    cache.put(PdfObjectCache.ColorspacesObjects, ref, newColorSpace);
                }else {
                    cache.put(PdfObjectCache.ColorspacesObjects, ref2, newColorSpace);                  
                }

                // System.out.println("cache "+ref +" "+isLowerCase+" "+colorspaceObject);
            }

        }

        //pass in pattern arrays containing all values
        if(newColorSpace.getID()==ColorSpaces.Pattern){

            //at this point we only know it is Pattern so need to pass in WHOLE array
            newColorSpace.setPattern(cache.patterns,pageData.getMediaBoxWidth(pageNum), pageData.getMediaBoxHeight(pageNum),gs.CTM);
            newColorSpace.setGS(gs);
        }

        //track colorspace use
        cache.put(PdfObjectCache.ColorspacesUsed, newColorSpace.getID(),"x");

        if(isStroke) {
            gs.strokeColorSpace = newColorSpace;
        } else {
            gs.nonstrokeColorSpace = newColorSpace;
        }
    }

}


