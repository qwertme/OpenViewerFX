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
 * Type3.java
 * ---------------
 */
package org.jpedal.fonts;

import org.jpedal.exception.PdfException;
import org.jpedal.fonts.glyph.T3Glyph;
import org.jpedal.fonts.glyph.T3Glyphs;
import org.jpedal.fonts.glyph.T3Size;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;

import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfKeyPairsIterator;

import org.jpedal.parser.T3StreamDecoder;
import org.jpedal.parser.ValueTypes;
import org.jpedal.render.*;
import org.jpedal.utils.LogWriter;
import java.util.Map;

/**
 * handles type1 specifics
 */
public class Type3 extends PdfFont {
    
    /**handle onto GS so we can read color*/
    private final GraphicsState currentGraphicsState=new GraphicsState();
    
    /**allow us to track if user is printing this font - needed for type3*/
    private final boolean isPrinting;
    
    /** get handles onto Reader so we can access the file
     * @param current_pdf_file - handle to PDF file
     **/
    public Type3(final PdfObjectReader current_pdf_file, final boolean isPrinting) {
        
        glyphs=new T3Glyphs();
        
        this.isPrinting=isPrinting;
        
        init(current_pdf_file);
        
    }
    
    
    /**read in a font and its details from the pdf file*/
    @Override
    public final void createFont(final PdfObject pdfObject, final String fontID, final boolean renderPage, final ObjectStore objectStore, final Map substitutedFonts) throws Exception{
        
        fontTypes=StandardFonts.TYPE3;
        
        //generic setup
        init(fontID, renderPage);
        
        /**
         * get FontDescriptor object - if present contains metrics on glyphs
         */
        final PdfObject pdfFontDescriptor=pdfObject.getDictionary(PdfDictionary.FontDescriptor);
        
        // get any dimensions if present (note FBoundBox if in pdfObject not Descriptor)
        setBoundsAndMatrix(pdfObject);
        
        setName(pdfObject);
        setEncoding(pdfObject, pdfFontDescriptor);
        
        readWidths(pdfObject,false);
        
        readEmbeddedFont(pdfObject,objectStore);
        
        //make sure a font set
        if (renderPage) {
            setFont(getBaseFontName(), 1);
        }
        
    }
    
    
    private void readEmbeddedFont(final PdfObject pdfObject, final ObjectStore objectStore) {
        
        final boolean hires=true;
        
        int key,otherKey;
        
        final PdfObject CharProcs=pdfObject.getDictionary(PdfDictionary.CharProcs);
        
        //handle type 3 charProcs and store for later lookup
        if(CharProcs!=null){
            
            final T3StreamDecoder glyphDecoder=new T3StreamDecoder(currentPdfFile,hires, isPrinting);
            glyphDecoder.setParameters(false,true,7,0,false,false);
            
            glyphDecoder.setObjectValue(ValueTypes.ObjectStore, objectStore);
            
            final PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);
            if(Resources!=null){
                try {
                    glyphDecoder.readResources(Resources,false);
                } catch (final PdfException e) {
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: "+e.getMessage());
                    }
                    //
                }
            }
            
            /**
             * read all the key pairs for Glyphs
             */
            final PdfKeyPairsIterator keyPairs=CharProcs.getKeyPairsIterator();
            
            String glyphKey;
            PdfObject glyphObj;
            
            while(keyPairs.hasMorePairs()){
                
                glyphKey=keyPairs.getNextKeyAsString();
                glyphObj=keyPairs.getNextValueAsDictionary();
               
                //decode and store in array
                if(glyphObj!=null && renderPage && !glyphKey.equals(".notdef") && !glyphKey.equals(".")){
                    
                    //decode and create graphic of glyph
                    final T3Renderer glyphDisplay=new T3Display(0,false,20,objectStore);
                    
                    glyphDisplay.setHiResImageForDisplayMode(hires);
                    glyphDisplay.setType3Glyph(glyphKey);
                    
                    try{
                        glyphDecoder.setRenderer(glyphDisplay);
                        glyphDecoder.setDefaultColors(currentGraphicsState.getNonstrokeColor(),currentGraphicsState.getNonstrokeColor());
                        int  renderX,renderY;
                        
                        //if size is 1 we need to scale up so we can see
                        int factor=1;
                        final double[] fontMatrix=pdfObject.getDoubleArray(PdfDictionary.FontMatrix);
                        if(fontMatrix!=null && fontMatrix[0]==1 && (fontMatrix[3]==1 || fontMatrix[3]==-1)) {
                            factor=10;
                        }
                        
                        final GraphicsState gs=new GraphicsState(0,0);
                        gs.CTM = new float[][]{{factor,0,0},
                            {0,factor,0},{0,0,1}
                        };
                        
                        final T3Size t3 = glyphDecoder.decodePageContent(glyphObj, gs);
                        
                        renderX=t3.x;
                        renderY=t3.y;
                        
                        //allow for rotated on page in case swapped
                        if(renderX==0 && renderY!=0){
                            renderX=t3.y;
                            renderY=t3.x;
                        }
                        
                        final T3Glyph glyph=new T3Glyph(glyphDisplay, renderX,renderY,glyphDecoder.ignoreColors);
                        glyph.setScaling(1f/factor);
                        
                        otherKey=-1;
                        
                       // System.out.println("str="+" "+rawDiffKeys.keySet());
                        key=((Integer)rawDiffKeys.get(keyPairs.getNextKeyAsString())).intValue();
                        
                        glyphs.setT3Glyph(key,otherKey, glyph);
                        
                    }catch(final Exception e){
                        e.printStackTrace(System.out);
                        
                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog("Exception "+e+" is Type3 font code");
                        }
                    }
                }
                
                //roll on
                keyPairs.nextPair();
            }
            isFontEmbedded = true;
        }
    }
}
