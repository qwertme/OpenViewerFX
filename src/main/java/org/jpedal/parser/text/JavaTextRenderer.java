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
 * JavaTextRenderer.java
 * ---------------
 */

package org.jpedal.parser.text;

import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
//
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.ValueTypes;
import org.jpedal.render.DynamicVectorRenderer;

/**
 *
 * @author markee
 */
class JavaTextRenderer {

       
    static void renderTextWithJavaFonts(GraphicsState gs, DynamicVectorRenderer current, int streamType, ParserOptions parserOptions, PdfFont currentFontData, GlyphData glyphData,final int Tmode, final float currentWidth, final boolean isTextShifted,final PdfJavaGlyphs glyphs,final float[][] Trm) {
        
        final float actualWidth=glyphData.getActualWidth();
        
        /**set values used if rendering as well*/
        Object transformedGlyph2;
        AffineTransform glyphAt=null;
        
        final int rawInt=glyphData.getRawInt();
        
        //
        { //render now
            final boolean isSTD= actualWidth>0 || DecoderOptions.isRunningOnMac || streamType==ValueTypes.FORM || StandardFonts.isStandardFont(currentFontData.getBaseFontName(),false) || currentFontData.isBrokenFont();
            
            /**flush cache if needed*/
            //if(!DynamicVectorRenderer.newCode2){
            if(glyphs.lastTrm[0][0]!= Trm[0][0] || glyphs.lastTrm[1][0]!= Trm[1][0] || glyphs.lastTrm[0][1]!= Trm[0][1] || glyphs.lastTrm[1][1]!= Trm[1][1]){
                glyphs.lastTrm = Trm;
                glyphs.flush();
            }
            // }
            
            //either calculate the glyph to draw or reuse if already drawn
            Area glyph = glyphs.getCachedShape(rawInt);
            glyphAt= glyphs.getCachedTransform(rawInt);
            
            if (glyph == null) {
                
                double dY = -1,dX=1, x3=0, y3=0;
                
                //allow for text running up the page
                if ((Trm[1][0] < 0 && Trm[0][1] >= 0)||(Trm[0][1] < 0 && Trm[1][0] >= 0)) {
                    dX=1f;
                    dY=-1f;
                }
                
                if (isSTD) {
                    
                    glyph = glyphs.getGlyph(rawInt, glyphData.getDisplayValue() , currentWidth);
                    
                    //hack to fix problem with Java Arial font
                    if(glyph !=null && rawInt ==146 && glyphs.isArialInstalledLocally) {
                        y3 = -(glyph.getBounds().height - glyph.getBounds().y);
                    }
                }else {
                    
                    //remap font if needed
                    String xx=glyphData.getDisplayValue();
                    
                    GlyphVector gv1 =null;
                    
                    //do not show CID fonts as Lucida unless match
                    if(!glyphs.isCIDFont || glyphs.isCorrupted() || glyphs.isFontInstalled) {
                        gv1 = glyphs.getUnscaledFont().createGlyphVector(PdfJavaGlyphs.frc, xx);
                    }
                    
                    if(gv1!=null){
                        
                        glyph = new Area(gv1.getOutline());
                        
                        //put glyph into display position
                        double glyphX=gv1.getOutline().getBounds2D().getX();
                        
                        //ensure inside box
                        x3=0;
                        
                        if(glyphX<0){
                            glyphX=-glyphX;
                            
                            x3=glyphX*2;
                            
                            //System.out.println(x3+" "+displayTrm[0][0]+" "+displayTrm[0][0]);
                            
                            if(Trm[0][0]>Trm[0][1]) {
                                x3 *= Trm[0][0];
                            } else {
                                x3 *= Trm[0][1];
                            }
                            
                            //glyphAt =AffineTransform.getTranslateInstance(x3,0);
                            
                        }
                        
                        final double glyphWidth=gv1.getVisualBounds().getWidth()+(glyphX*2);
                        final double scaleFactor= currentWidth /glyphWidth;
                        if(scaleFactor<1) {
                            dX *= scaleFactor;
                        }
                        
                        if(x3>0){
                            x3 *= dX;
                        }
                    }
                }
                
                glyphAt =new AffineTransform(dX* Trm[0][0],dX* Trm[0][1],dY* Trm[1][0],dY* Trm[1][1] ,x3, y3);
                //create shape for text using transformation to make correct size
                // glyphAt =new AffineTransform(dX* displayTrm[0][0],dX* displayTrm[0][1],dY* displayTrm[1][0],dY* displayTrm[1][1] ,x3, y3);
                
                //save so we can reuse if it occurs again in this TJ command
                glyphs.setCachedShape(rawInt, glyph,glyphAt);
            }
            
            if(glyph!=null && Tmode==GraphicsState.CLIPTEXT && glyph.getBounds().width>0){ /**support for TR7*/
                
                
                final Area glyphShape=(Area) glyph.clone();
                
                //we need to apply to make it all work
                glyphShape.transform(glyphAt);
                
                //if its already generated we just need to move it
                if(parserOptions.renderDirectly()){
                    final AffineTransform at2 =AffineTransform.getTranslateInstance(Trm[2][0],(Trm[2][1]));
                    glyphShape.transform(at2);
                }
                
                gs.addClip(glyphShape);

                //current.drawClip(gs,null,false);
                
               // if(parserOptions.renderDirectly()) {
                    glyph = null;
                //}
                
            }
            
            transformedGlyph2=glyph;
            
        }
        
        if(transformedGlyph2!=null){
            
            final double[] textTrans = new double[6];
            glyphAt.getMatrix(textTrans);
            
            final int fontSize=glyphData.getFontSize();

            if(parserOptions.useJavaFX()){
                current.drawEmbeddedText(Trm,fontSize,null,null,DynamicVectorRenderer.TEXT, gs,textTrans, glyphData.getUnicodeValue(),currentFontData, -100);
            }else
                
                //add to renderer
                if(parserOptions.renderDirectly()){
                    current.drawEmbeddedText(Trm,fontSize,null,transformedGlyph2,DynamicVectorRenderer.TEXT, gs,textTrans, glyphData.getUnicodeValue(),currentFontData, -100);
                }else{
                    
                    if(isTextShifted) {
                        current.drawEmbeddedText(Trm, -fontSize, null, transformedGlyph2, DynamicVectorRenderer.TEXT, gs, null, glyphData.getUnicodeValue(), currentFontData, -100);
                    } else {
                        current.drawEmbeddedText(Trm, fontSize, null, transformedGlyph2, DynamicVectorRenderer.TEXT, gs, null, glyphData.getUnicodeValue(), currentFontData, -100);
                    }
                }
        }
    }
 
}
