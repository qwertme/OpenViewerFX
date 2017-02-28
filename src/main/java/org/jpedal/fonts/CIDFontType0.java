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
 * CIDFontType0.java
 * ---------------
 */
package org.jpedal.fonts;

import java.util.Map;

import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.glyph.T1Glyphs;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;


/**
 * handles truetype specifics
 *  */
public class CIDFontType0 extends Type1C {
    
    /**used to display non-embedded fonts*/
    private CIDFontType2 subFont;
    
    /**get handles onto Reader so we can access the file*/
    public CIDFontType0(final PdfObjectReader currentPdfFile, final String substituteFontFile) {
        
        glyphs=new T1Glyphs(true);
        
        isCID=true;
        
        isCIDFont=true;
        TTstreamisCID=true;
        init(currentPdfFile);
        this.currentPdfFile=currentPdfFile;
        
        this.substituteFontFile=substituteFontFile;
        
    }
    
    /**read in a font and its details from the pdf file*/
    @Override
    public void createFont(final PdfObject pdfObject, final String fontID, final boolean renderPage, final ObjectStore objectStore, final Map substitutedFonts) throws Exception{
        
        fontTypes=StandardFonts.CIDTYPE0;
        this.fontID=fontID;
        
        final PdfObject Descendent=pdfObject.getDictionary(PdfDictionary.DescendantFonts);
        final PdfObject pdfFontDescriptor=Descendent.getDictionary(PdfDictionary.FontDescriptor);
        
        createCIDFont(pdfObject,Descendent);
        
        
        if (pdfFontDescriptor!= null){
            
//            /**CIDSet*/
//            PdfObject CIDSet=pdfFontDescriptor.getDictionary(PdfDictionary.CIDSet);
//            System.out.println(pdfObject.getObjectRefAsString()+" "+Descendent.getObjectRefAsString());
//            if(CIDSet!=null && 1==2){
//
//                int[] bitCheck={128,64,32,16,8,4,2,1};
//                int cidReached=0;
//
//                int cidCount=CIDSet.stream.length;
//                int hits=0;
//
//                for(int ptr=0;ptr<cidCount;ptr++){
//
//                    byte pixelByte=CIDSet.stream[ptr];
//
//                    for(int bits=0;bits<8;bits++){
//
//                        //System.out.println("cid="+cidReached+" "+bits+" "+ptr);
//
//                        int bitUsed=ptr & 7;
//
//                        //if set we need to set in second array, set pixel in new array as y,x
//                        if((pixelByte & bitCheck[bitUsed])== bitCheck[bitUsed]){
//
//                            hits++;
//
//                            System.out.println(cidReached+" XX="+hits);
//                        }
//
//                        cidReached++;
//                    }
//                }
//
//                System.out.println("length="+(CIDSet.stream.length));
//
//            }length
            
            final float[] newFontBBox=pdfFontDescriptor.getFloatArray(PdfDictionary.FontBBox);
            if(newFontBBox!=null) {
                FontBBox=newFontBBox;
            }
            
            //set ascent and descent
//            float value=pdfFontDescriptor.getFloatNumber(PdfDictionary.Ascent);
//            if(value!=0)
//                ascent=value;
//
//            value=pdfFontDescriptor.getFloatNumber(PdfDictionary.Descent);
//            if(value!=0)
//                descent=value;
            
            readEmbeddedFont(pdfFontDescriptor);
        }
        
        if(renderPage && !isFontEmbedded && substituteFontFile!=null){
            
            isFontSubstituted=true;
            subFont=new CIDFontType2(currentPdfFile,TTstreamisCID);
            
            subFont.substituteFontUsed(substituteFontFile);
            this.isFontEmbedded=true;
            
            glyphs.setFontEmbedded(true);
        }

        //make sure a font set
        if (renderPage) {
            setFont(getBaseFontName(), 1);
        }
        
    }
    
    
    /**
     * used by  non type3 font
     */
    @Override
    public PdfJavaGlyphs getGlyphData(){
        
        if(subFont!=null) {
            return subFont.getGlyphData();
        } else {
            return glyphs;
        }
        
    }
    
}
