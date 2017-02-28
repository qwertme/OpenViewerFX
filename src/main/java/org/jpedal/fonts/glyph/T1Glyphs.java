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
 * T1Glyphs.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Collections;

import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.objects.T1GlyphNumber;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;

public class T1Glyphs extends PdfJavaGlyphs {

    //flag to show if actually 1c
    private boolean is1C;
    
    private String[] charForGlyphIndex;

    private DynamicVectorRenderer dynamicVectorRenderer;

    /**holds mappings for drawing the glpyhs*/
    private final Map<String,byte[]> charStrings=new HashMap<String,byte[]>();
    private final Map<String,Integer> glyphNumbers=new HashMap<String,Integer>();
    
    /**holds the numbers*/
    private static final int max=100;
    
    private double[] operandsRead = new double[max];
    
    /**pointer on stack*/
    private int operandReached;
    
    private float[] pt;
    
    //co-ords for closing glyphs
    private double xs=-1,ys=-1,x,y;
    
    /**tracks points read in t1 flex*/
    private int ptCount;
    
    /**op to be used next*/
    private int currentOp;
    
    /**used to count up hints*/
    private int hintCount;
    
    /** I byte ops in CFF DIct table *
     * private static String[] raw1ByteValues =
     * {
     * "version",
     * "Notice",
     * "FullName",
     * "FamilyName",
     * "Weight",
     * "FontBBox",
     * "BlueValues",
     * "OtherBlues",
     * "FamilyBlues",
     * "FamilyOtherBlues",
     * "StdHW",
     * "StdVW",
     * "escape",
     * "UniqueID",
     * "XUID",
     * "charset",
     * "Encoding",
     * "CharStrings",
     * "Private",
     * "Subrs",
     * "defaultWidthX",
     * "nominalWidthX",
     * "-Reserved-",
     * "-Reserved-",
     * "-Reserved-",
     * "-Reserved-",
     * "-Reserved-",
     * "-Reserved-",
     * "intint",
     * "longint",
     * "BCD",
     * "-Reserved-" };/**/
    
    /** 2 byte ops in CFF DIct table *
     * private static String[] raw2ByteValues =
     * {
     * "Copyright",
     * "isFixedPitch",
     * "ItalicAngle",
     * "UnderlinePosition",
     * "UnderlineThickness",
     * "PaintType",
     * "CharstringType",
     * "FontMatrix",
     * "StrokeWidth",
     * "BlueScale",
     * "BlueShift",
     * "BlueFuzz",
     * "StemSnapH",
     * "StemSnapV",
     * "ForceBold",
     * "-Reserved-",
     * "-Reserved-",
     * "LanguageGroup",
     * "ExpansionFactor",
     * "initialRandomSeed",
     * "SyntheticBase",
     * "PostScript",
     * "BaseFontName",
     * "BaseFontBlend",
     * "-Reserved-",
     * "-Reserved-",
     * "-Reserved-",
     * "-Reserved-",
     * "-Reserved-",
     * "-Reserved-",
     * "ROS",
     * "CIDFontVersion",
     * "CIDFontRevision",
     * "CIDFontType",
     * "CIDCount",
     * "UIDBase",
     * "FDArray",
     * "FDSelect",
     * "FontName" };/**/
    
    
    
    
    
    /**used by t1 font renderer to ensure hsbw or sbw executed first*/
    private boolean allowAll;
    
    private double h;
    private boolean isCID;
    
    //Used by font code - base width to add offset to
    private int[] nominalWidthX = {0}, defaultWidthX = {};
    private boolean defaultWidthsPassed;
    private int[] fdSelect;
    
    public T1Glyphs(final boolean isCID) {
        this.isCID=isCID;
    }
    
    /**used by PS2OTF conversion*/
    @SuppressWarnings("UnusedParameters")
    public T1Glyphs(final boolean isCID, final boolean is1C) {
        
        this.charForGlyphIndex=new String[65536];
    }
    
    /**
     * return name of font
     * NAME will be LOWERCASE to avoid issues of capitalisation
     * when used for lookup - if no name, will default to  null
     *
     * Mode is PdfDecoder.SUBSTITUTE_* CONSTANT. RuntimeException will be thrown on invalid value
     * @param fontData
     */
    public static String[] readFontNames(final FontData fontData) {
        
        final String[] fontNames=new String[1];
        fontNames[0]=null;
        
        final BufferedReader br =new BufferedReader(new StringReader(new String(fontData.getBytes(0,fontData.length()))));
        
        String line=null;
        
        while (true) {
            
            try {
                line = br.readLine();
            } catch (final IOException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
            
            if (line == null) {
                break;
            }
            
            if (line.startsWith("/FontName")){
                final int nameStart=line.indexOf('/',9);
                if(nameStart!=-1){
                    final int nameEnd=line.indexOf(' ', nameStart);
                    if(nameEnd!=-1){
                        final String name=line.substring(nameStart+1,nameEnd);
                        fontNames[0]=name.toLowerCase();
                        break;
                    }
                }
            }
        }
        
        if(br!=null){
            try{
                br.close();
            }catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " closing stream");
                }
            }
        }
        
        if(fontData!=null) {
            fontData.close();
        }
        
        
        return fontNames;
    }
    
    /**
     * @param factory
     * @param isFlex
     * @param routine
     */
    private boolean processFlex(final GlyphFactory factory, boolean isFlex, final int routine) {
        
        
        //if in flex feature see if we have all values - exit if not
        if((isFlex)&&(ptCount==14)&&(routine==0)){
            isFlex=false;
            for(int i=0;i<12;i += 6){
                factory.curveTo(pt[i],pt[i+1],pt[i+2],pt[i+3],pt[i+4],pt[i+5]);
            }
        }else if((!isFlex)&&(routine>=0)&&(routine<=2)){ //determine if flex feature and enable
            isFlex=true;
            ptCount=0;
            pt=new float[16];
        }
        return isFlex;
    }
    
    /**
     * @param factory
     * @param rawInt
     */
    private void endchar(final GlyphFactory factory, final int rawInt) {

        if(operandReached==5){ //allow for width and 4 chars
            operandReached--;
            currentOp++;
        }
        if(operandReached==4){
            StandardFonts.checkLoaded(StandardFonts.STD);
            final float adx=(float)(x+operandsRead[currentOp]);
            final float ady=(float)(y+operandsRead[currentOp+1]);
            final String bchar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)operandsRead[currentOp+2]);
            final String achar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)operandsRead[currentOp+3]);

            x=0;
            y=0;
            decodeGlyph(null,factory,bchar,rawInt, "");
            factory.closePath();
            factory.moveTo(adx,ady);
            x=adx;
            y=ady;
            decodeGlyph(null,factory,achar,rawInt, "");

            if(xs==-1){
                xs=x;
                ys=y;
            }
        }else {
            factory.closePath();
        }
    }
    
    /**
     * @param p
     */
    private int mask(int p) {

        hintCount+=operandReached/2;

        int count=hintCount;
        while(count>0){
            p++;
            count -= 8;
        }
        return p;
    }
    
    /**
     *
     */
    private double sbw() {
        
        final double yy;
        
        double val=operandsRead[operandReached-2];
        y=val;
        
        val=operandsRead[operandReached-1];
        x=val;
        
        xs=x;
        ys=y;
        allowAll=true;
        yy=y;
        
        h=operandsRead[operandReached-3];

        return yy;
    }
    
    /**
     * @param factory
     * @param isFirst
     */
    private void hmoveto(final GlyphFactory factory, final boolean isFirst) {
        if(isFirst && operandReached==2) {
            currentOp++;
        }
        
        final double val=operandsRead[currentOp];
        x += val;
        factory.moveTo((float)x,(float)y);

        xs=x;
        ys=y;

    }
    
    /**
     * @param factory
     * @param isFirst
     */
    private void rmoveto(final GlyphFactory factory, final boolean isFirst) {
        if((isFirst)&&(operandReached==3)) {
            currentOp++;
        }

        double val=operandsRead[currentOp+1];
        y += val;
        val=operandsRead[currentOp];
        x += val;
        
        factory.moveTo((float)x,(float)y);
        //if(xs==-1){
        xs=x;
        ys=y;

    }
    
    /**
     * @param factory
     * @param key
     *
     */
    private void vhhvcurveto(final GlyphFactory factory, final int key) {
        boolean  isHor=(key==31);
        while ( operandReached >= 4 ){
            operandReached -= 4;
            if ( isHor ) {
                x += operandsRead[currentOp];
            } else {
                y += operandsRead[currentOp];
            }
            pt[0]=(float) x;
            pt[1]=(float) y;
            x += operandsRead[currentOp+1];
            y += operandsRead[currentOp+2];
            pt[2]=(float) x;
            pt[3]=(float) y;
            if ( isHor ){
                y += operandsRead[currentOp+3];
                if ( operandReached ==1 ) {
                    x += operandsRead[currentOp + 4];
                }
            }else{
                x += operandsRead[currentOp+3];
                if ( operandReached == 1 ) {
                    y += operandsRead[currentOp + 4];
                }
            }
            pt[4]=(float) x;
            pt[5]=(float) y;
            factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

            currentOp  += 4;
            
            isHor = !isHor;
        }
    }
    
    /**
     * @param factory
     * @param key
     */
    private void vvhhcurveto(final GlyphFactory factory, final int key) {
        
        final boolean isVV=(key==26);
        if ( (operandReached & 1) ==1 ){
            if(isVV) {
                x += operandsRead[0];
            } else {
                y += operandsRead[0];
            }
            currentOp++;
        }
        
        //note odd co-ord order
        while (currentOp<operandReached ){
            if(isVV) {
                y += operandsRead[currentOp];
            } else {
                x += operandsRead[currentOp];
            }
            pt[0]=(float) x;
            pt[1]=(float) y;
            x += operandsRead[currentOp+1];
            y += operandsRead[currentOp+2];
            pt[2]=(float) x;
            pt[3]=(float) y;
            if(isVV) {
                y += operandsRead[currentOp + 3];
            } else {
                x += operandsRead[currentOp + 3];
            }
            pt[4]=(float) x;
            pt[5]=(float) y;
            currentOp += 4;
            factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

        }
    }
    
    /**
     * @param factory
     */
    private void rlinecurve(final GlyphFactory factory) {
        //lines
        int lineCount=( operandReached - 6 ) / 2;
        while ( lineCount > 0 ){
            x += operandsRead[currentOp];
            y += operandsRead[currentOp+1];
            factory.lineTo((float)x,(float)y);

            currentOp += 2;
            lineCount--;
        }
        //curves
        final float[] coords=new float[6];
        x += operandsRead[currentOp];
        y += operandsRead[currentOp+1];
        coords[0]=(float) x;
        coords[1]=(float) y;
        
        x += operandsRead[currentOp+2];
        y += operandsRead[currentOp+3];
        coords[2]=(float) x;
        coords[3]=(float) y;
        
        x += operandsRead[currentOp+4];
        y += operandsRead[currentOp+5];
        coords[4]=(float) x;
        coords[5]=(float) y;
        
        factory.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);

        currentOp += 6;
    }
    
    /**
     * @param factory
     */
    private void closepath(final GlyphFactory factory) {
        if(xs!=-1) {
            factory.lineTo((float) xs, (float) ys);
        }

        xs=-1; //flag as unset
        
    }
    
    /**
     * @param factory
     */
    private void hsbw(final GlyphFactory factory, final String glyphName) {
        x += operandsRead[0];
        factory.moveTo((float)x,0);

        if (baseFontName != null &&                             //Check right call
                dynamicVectorRenderer != null &&                    //Check right call
                dynamicVectorRenderer.isHTMLorSVG()) {     //Just to be safe

            dynamicVectorRenderer.saveAdvanceWidth(baseFontName,glyphName,(int)ys);
            
        }
        
        allowAll=true;
    }
    
    /**
     *
     */
    private void pop() {

        if(operandReached>0) {
            operandReached--;
        }
    }

    /**
     *
     */
    private void div() {

        final double value=operandsRead[operandReached-2]/operandsRead[operandReached-1];

        //operandReached--;
        if(operandReached>0) {
            operandReached--;
        }
        operandsRead[operandReached-1]=value;

    }
    
    /**
     * @param factory
     * @param isFirst
     */
    private void vmoveto(final GlyphFactory factory, final boolean isFirst) {
        if((isFirst)&&(operandReached==2)) {
            currentOp++;
        }
        y += operandsRead[currentOp];
        factory.moveTo((float)x,(float)y);
        
        //if((xs==-1)){
        xs=x;
        ys=y;

    }
    
    /**
     * @param factory
     */
    private void rlineto(final GlyphFactory factory) {
        int lineCount=operandReached/2;
        while ( lineCount > 0 ){
            x += operandsRead[currentOp];
            y += operandsRead[currentOp+1];
            factory.lineTo((float)x,(float)y);
            currentOp += 2;
            lineCount--;

        }
    }
    
    /**
     * @param factory
     * @param key
     */
    private void hvlineto(final GlyphFactory factory, final int key) {
        boolean isHor = ( key==6 );
        int start=0;
        while (start<operandReached ){
            if ( isHor ) {
                x += operandsRead[start];
            } else {
                y += operandsRead[start];
            }
            factory.lineTo((float)x,(float)y);

            start++;
            isHor =!isHor;
        }
    }
    
    /**
     * @param factory
     */
    private void rrcurveto(final GlyphFactory factory) {

        int  curveCount = ( operandReached  ) / 6;

        while ( curveCount > 0 ){
            final float[] coords=new float[6];
            x += operandsRead[currentOp];
            y += operandsRead[currentOp+1];
            coords[0]=(float) x;
            coords[1]=(float) y;
            
            x += operandsRead[currentOp+2];
            y += operandsRead[currentOp+3];
            coords[2]=(float) x;
            coords[3]=(float) y;
            
            x += operandsRead[currentOp+4];
            y += operandsRead[currentOp+5];
            coords[4]=(float) x;
            coords[5]=(float) y;
            
            factory.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);

            currentOp += 6;
            curveCount--;
        }
    }

    
    /**
     * @param factory
     */
    private void rcurveline(final GlyphFactory factory) {
        //curves
        int  curveCount=( operandReached - 2 ) / 6;
        while ( curveCount > 0 ){
            final float[] coords=new float[6];
            x += operandsRead[currentOp];
            y += operandsRead[currentOp+1];
            coords[0]=(float) x;
            coords[1]=(float) y;
            
            x += operandsRead[currentOp+2];
            y += operandsRead[currentOp+3];
            coords[2]=(float) x;
            coords[3]=(float) y;
            
            x += operandsRead[currentOp+4];
            y += operandsRead[currentOp+5];
            coords[4]=(float) x;
            coords[5]=(float) y;
            
            factory.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);

            currentOp += 6;
            curveCount--;
        }
        
        // line
        x += operandsRead[currentOp];
        y += operandsRead[currentOp+1];
        factory.lineTo((float)x,(float)y);
        currentOp += 2;

    }
    
    /**
     * @param factory
     * @param rawInt
     * @param currentOp
     */
    private void seac(final GlyphFactory factory, final int rawInt, final int currentOp) {
        
        StandardFonts.checkLoaded(StandardFonts.STD);
        final float adx=(float)(operandsRead[currentOp+1]);
        final float ady=(float)(operandsRead[currentOp+2]);
        final String bchar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)operandsRead[currentOp+3]);
        final String achar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)operandsRead[currentOp+4]);
        
        final double preX=x;
        //x=0;
        y=0;
        decodeGlyph(null,factory,bchar,rawInt, "");
        
        factory.closePath();
        factory.moveTo(0,0);
        x=adx+preX;
        y=ady;
        decodeGlyph(null,factory,achar,rawInt, "");
    }
    
    /**
     * @param factory
     */
    private void flex1(final GlyphFactory factory) {

        double   dx = 0;
        double dy = 0;
        final double x1=x;
        final double y1=y;
        
        /*workout dx/dy/horizontal and reset flag*/
        for ( int count =0; count <10; count += 2 ){
            dx += operandsRead[count];
            dy += operandsRead[count+1];
        }
        final boolean isHorizontal=(Math.abs(dx)>Math.abs(dy));
        
        for(int points=0;points<6;points += 2){//first curve
            x += operandsRead[points];
            y += operandsRead[points+1];
            pt[points]=(float) x;
            pt[points+1]=(float) y;
        }
        factory.curveTo(pt[0], pt[1], pt[2], pt[3], pt[4],pt[5]);

        for(int points=0;points<4;points += 2){//second curve
            x += operandsRead[points+6];
            y += operandsRead[points+7];
            pt[points]=(float) x;
            pt[points+1]=(float) y;
        }
        
        if ( isHorizontal ){ // last point
            x += operandsRead[10];
            y  = y1;
        }else{
            x  = x1;
            y += operandsRead[10];
        }
        pt[4]=(float) x;
        pt[5]=(float) y;
        factory.curveTo(pt[0], pt[1], pt[2], pt[3], pt[4],pt[5]);
    }
    
    /**
     * @param factory
     */
    private void flex(final GlyphFactory factory) {
        for(int curves=0;curves<12;curves += 6){
            for(int points=0;points<6;points += 2){
                x += operandsRead[curves+points];
                y += operandsRead[curves+points+1];
                pt[points]=(float) x;
                pt[points+1]=(float) y;
            }
            factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
        }
    }
    
    /**
     * @param factory
     */
    private void hflex(final GlyphFactory factory) {
        //first curve
        x += operandsRead[0];
        pt[0]=(float) x;
        pt[1]=(float) y;
        x += operandsRead[1];
        y += operandsRead[2];
        pt[2]=(float) x;
        pt[3]=(float) y;
        x += operandsRead[3];
        pt[4]=(float) x;
        pt[5]=(float) y;
        factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

        //second curve
        x += operandsRead[4];
        pt[0]=(float) x;
        pt[1]=(float) y;
        x += operandsRead[5];
        pt[2]=(float) x;
        pt[3]=(float) y;
        x += operandsRead[6];
        pt[4]=(float) x;
        pt[5]=(float) y;
        factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

    }
    
    /**
     * @param factory
     */
    private void hflex1(final GlyphFactory factory) {
        //first curve
        x+=operandsRead[0];
        y+=operandsRead[1];
        pt[0]=(float) x;
        pt[1]=(float) y;
        x+=operandsRead[2];
        y+=operandsRead[3];
        pt[2]=(float) x;
        pt[3]=(float) y;
        x+=operandsRead[4];
        pt[4]=(float) x;
        pt[5]=(float) y;
        factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

        //second curve
        x+=operandsRead[5];
        pt[0]=(float) x;
        pt[1]=(float) y;
        x+=operandsRead[6];
        y+=operandsRead[7];
        pt[2]=(float) x;
        pt[3]=(float) y;
        x += operandsRead[8];
        pt[4]=(float) x;
        pt[5]=(float) y;
        factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

    }
    
    
    /**
     * used by  non type3 font
     */
    @Override
    public PdfGlyph getEmbeddedGlyph(final GlyphFactory factory, final String glyph, final float[][] Trm, final int rawInt,
            final String displayValue, final float currentWidth, final String key) {
        
        
        /**flush cache if needed*/
        if(Trm!=null && (lastTrm[0][0]!=Trm[0][0] || lastTrm[1][0]!=Trm[1][0] || lastTrm[0][1]!=Trm[0][1] || lastTrm[1][1]!=Trm[1][1])){
            lastTrm=Trm;
            flush();
        }
        
        //either calculate the glyph to draw or reuse if alreasy drawn
        PdfGlyph transformedGlyph2 = getEmbeddedCachedShape(rawInt);
        
        if (transformedGlyph2 == null) {
            
            /**create new stack for glyph*/
            operandsRead = new double[max];
            operandReached=0;
            
            x=-factory.getLSB();
            
            y=0;
            decodeGlyph(key,factory,glyph,rawInt, displayValue);
            
            //generate Glyph
            transformedGlyph2=factory.getGlyph();
            
            //save so we can reuse if it occurs again in this TJ command
            setEmbeddedCachedShape(rawInt, transformedGlyph2);
        }
        
        /**
         * Save glyph number into object
         */
        //Try fetching number using glyph
        Object num = glyphNumbers.get(glyph);
        
        //Failing that, use rawInt
        if (num == null) {
            num = glyphNumbers.get(Integer.toString(rawInt));
        }
        
        //Failing that, use key
        if (num == null) {
            num = glyphNumbers.get(key);
        }
        
        //If the glyph number has been found, store it in the glyph
        if (num != null) {
            transformedGlyph2.setGlyphNumber((Integer) num);
        }
        
        return transformedGlyph2;
    }

    
    /*
    * creates glyph from type1c font commands
    */
    void decodeGlyph(final String embKey, final GlyphFactory factory, String glyph, final int rawInt, final String displayValue) {
        
        byte[]  glyphStream;

        //System.out.println(glyph+" "+baseFontName+" "+rawInt+" "+currentWidth);
        
        allowAll=false; //used by T1 to make sure sbw of hsbw
        
        /**
         * get the stream of commands for the glyph
         */
        if(isCID){
            glyphStream= charStrings.get(String.valueOf(rawInt));
        }else{
            if(glyph==null) {
                glyph = displayValue;//getMappedChar(rawInt,false);
            }
            
            
            if(glyph==null){
                glyph=embKey;
                
                if(glyph==null) {
                    glyph = ".notdef";
                }
            }
            
            /**
             * get the bytestream of commands and reset global values
             */
            glyphStream = charStrings.get(glyph);
            
            if(glyphStream==null){
                
                if(embKey!=null) {
                    glyphStream = charStrings.get(embKey);
                }
                if(glyphStream==null) {
                    glyphStream = charStrings.get(".notdef");
                }
            }
        }
        
        /**
         * if valid stream then decode
         */
        if(glyphStream!=null){

            decodeGlyphStream(factory, glyph, rawInt, glyphStream);
            
        }
    }

    private void decodeGlyphStream(final GlyphFactory factory, final String glyph, final int rawInt, byte[] glyphStream) {

        boolean isFirst=true; //flag to pick up extra possible first value

        ptCount=0;

        int nonSubrCommandCount=0; //command number ignoring subr commands
        int p = 0,lastNumberStart=0, nextVal,key=0,lastKey,dicEnd=glyphStream.length,lastVal=0;
        currentOp=0;
        hintCount=0;
        double ymin=999999,ymax=0,yy=1000;
        boolean isFlex=false; //flag to show its a flex command in t1
        pt=new float[6];
        int potentialWidth = 0;

        h=100000;
        /**set length for 1C*/
        if(is1C){
            operandsRead=new double[max];
            operandReached=0;
            allowAll=true;
        }

        /**
         * work through the commands decoding and extracting numbers (operands are FIRST)
         */

        while (p < dicEnd) {

            //get next byte value from stream
            nextVal = glyphStream[p] & 0xFF;

            if (nextVal >31 || nextVal==28) {  //if its a number get it and update pointer p

                //track location
                lastNumberStart=p;

                //isNumber=true;
                p= T1GlyphNumber.getNumber(glyphStream, p,operandsRead,operandReached,is1C);
                lastVal=(int) operandsRead[operandReached];//nextVal;
                operandReached++;

                //Pick up if first item is a number as this may be the width offset - currently only used for HTML
                if (lastNumberStart == 0) {
                    if (nominalWidthX.length == 1) {
                        potentialWidth = nominalWidthX[0]+lastVal;
                    } else {
                        final int glyphNo = glyphNumbers.get(String.valueOf(rawInt))-1;
                        if (glyphNo < fdSelect.length) {
                            potentialWidth = nominalWidthX[fdSelect[glyphNo]]+lastVal;
                        }
                    }
                }

            }else{  // operator

                //This tests whether the previously saved potential width is an argument of the first operator or
                //an actual width. If it's an actual width, it's saved.
                if (is1C && nonSubrCommandCount == 0 &&
                        nextVal != 10 &&    //callsubr
                        nextVal != 11 &&    //return
                        nextVal != 29) {    //callgsubr

                    boolean hasOddArgs=false;
                    if (nextVal == 22 ||        //hmoveto
                            nextVal == 4 ||     //vmoveto
                            (nextVal == 12 && (
                            glyphStream[p+1] == 9 ||    //abs
                            glyphStream[p+1] == 14 ||   //neg
                            glyphStream[p+1] == 26 ||   //sqrt
                            glyphStream[p+1] == 18 ||   //drop
                            glyphStream[p+1] == 27 ||   //dup
                            glyphStream[p+1] == 21 ||   //get
                            glyphStream[p+1] == 5))) {   //not
                        hasOddArgs = true;
                    }

                    if (((!hasOddArgs && (operandReached % 2 == 1)) || (hasOddArgs && (operandReached % 2 == 0)))) { //Make sure not an argument
                        saveWidth(glyph, rawInt, potentialWidth);
                    }
                }

                nonSubrCommandCount++;
                //isNumber=false;
                lastKey=key;
                key = nextVal;
                p++;
                currentOp=0;

                if (key ==12) { //handle escaped keys (ie 2 byte ops)
                    key= glyphStream[p] & 0xFF;
                    p++;

                    if(key==7){ //sbw
                        yy = sbw();
                        operandReached=0; //move to first operator
                    }else if((key == 16 && allowAll) || key != 16){ //other 2 byte operands
                        isFlex = handle2ByteOp(factory, rawInt, key, lastVal, isFlex);
                    }
                }else if(key==13){ //hsbw (T1 only)
                    hsbw(factory, glyph);
                    operandReached=0; //move to first operator
                } else if(allowAll){ //other one byte ops
                    if(key==0){ //reserved
                    }else if(key==1 || key==3 || key==18 || key==23){ //hstem vstem hstemhm vstemhm
                        hintCount+=operandReached/2;
                        operandReached=0; //move to first operator

                    }else if(key==4){ //vmoveto
                        if(isFlex){
                            flex();
                        }else {
                            vmoveto(factory, isFirst);
                        }
                        operandReached=0; //move to first operator
                    }else if(key==5){//rlineto
                        rlineto(factory);
                        operandReached=0; //move to first operator
                    } else if(key==6 || key==7){//hlineto or vlineto
                        hvlineto(factory, key);
                        operandReached=0; //move to first operator
                    }else if(key==8){//rrcurveto
                        rrcurveto(factory);
                        operandReached=0; //move to first operator
                    }else if(key==9){ //closepath (T1 only)
                        closepath(factory);
                        operandReached=0; //move to first operator
                    }else if(key==10 || (key==29)){ //callsubr and callgsubr

                        nonSubrCommandCount--;

                        if(!is1C && key==10 && (lastVal>=0)&&(lastVal<=2) && lastKey!=11 && operandReached>5){//last key stops spurious match in multiple sub-routines
                            isFlex = processFlex(factory, isFlex, lastVal);
                            operandReached=0; //move to first operator

                        }else{

                            //								factor in bias
                            if(key==10) {
                                lastVal += localBias;
                            } else {
                                lastVal += globalBias;
                            }

                            final byte[] newStream;
                            if(key==10){ //local subroutine

                                newStream = charStrings.get("subrs"+ (lastVal));

                            }else{ //global subroutine

                                newStream = charStrings.get("global"+ (lastVal));
                            }

                            if(newStream!=null){

                                final int newLength=newStream.length;
                                final int oldLength=glyphStream.length;
                                final int totalLength=newLength+oldLength-2;

                                dicEnd=dicEnd+newLength-2;
                                //workout length of new stream
                                final byte[] combinedStream=new byte[totalLength];

                                System.arraycopy(glyphStream, 0, combinedStream, 0, lastNumberStart);
                                System.arraycopy(newStream, 0, combinedStream, lastNumberStart, newLength);
                                System.arraycopy(glyphStream, p, combinedStream, lastNumberStart+newLength, oldLength-p);

                                glyphStream=combinedStream;

                                p=lastNumberStart;

                                if(operandReached>0) {
                                    operandReached--;
                                }

                            }
                        }
                        //operandReached=0; //move to first operator
                    }else if(key==11) { //return

                        nonSubrCommandCount--;
                        //operandReached=0; //move to first operator
                    }else if((key==14)){ //endchar
                        endchar(factory, rawInt);
                        operandReached=0; //move to first operator
                        p=dicEnd+1;
                    }else if(key==16) { //blend

                        operandReached=0; //move to first operator
                    }else if((key==19 || key==20)){ //hintmask //cntrmask
                        p = mask( p);
                        operandReached=0; //move to first operator
                    }else if(key==21){//rmoveto
                        if(isFlex){
                            moveToAsFlex();
                        }else {
                            rmoveto(factory, isFirst);
                        }
                        operandReached=0; //move to first operator
                    }else if(key==22){ //hmoveto
                        if(isFlex){
                            final double val=operandsRead[currentOp];
                            x += val;
                            pt[ptCount]=(float) x;
                            ptCount++;
                            pt[ptCount]=(float) y;
                            ptCount++;

                        }else {
                            hmoveto(factory, isFirst);
                        }
                        operandReached=0; //move to first operator
                    }else if(key==24){ //rcurveline
                        rcurveline(factory);
                        operandReached=0; //move to first operator
                    }else if(key==25){ //rlinecurve
                        rlinecurve(factory);
                        operandReached=0; //move to first operator
                    }else if(key==26 || key==27){ //vvcurve hhcurveto
                        vvhhcurveto(factory, key);
                        operandReached=0; //move to first operator
                    }else if(key==30 || key==31){	//vhcurveto/hvcurveto
                        vhhvcurveto(factory, key);
                        operandReached=0; //move to first operator
                    }
                }

                if(ymin>y) {
                    ymin = y;
                }

                if(ymax<y) {
                    ymax = y;
                }

                if(key!=19 && key!=29 && key!=10) {
                    isFirst = false;
                }
            }
        }

        if(yy>h) {
            ymin = yy - h;
        }

        if(ymax<yy){
            ymin=0;

        }else if(yy!=ymax){//added for M2003W.pdf font display

            final float dy=(float) (ymax-(yy-ymin));

            if(dy<0){

                if(yy-ymax<=dy) {
                    ymin = dy;
                } else {
                    ymin -= dy;
                }
            }else {
                ymin = 0;
            }

            if(ymin<0) {
                ymin = 0;
            }
        }

        /**set values to adjust glyph vertically*/
        factory.setYMin((float)(ymin));

    }

    private boolean handle2ByteOp(GlyphFactory factory, int rawInt, int key, int lastVal, boolean isFlex) {

        switch(key) {

            case 0: //dotsection
                operandReached = 0; //move to first operator
                break;

            case 6: //seac
                seac(factory, rawInt, currentOp);
                operandReached = 0; //move to first operator
                break;

            case 12: //div functionn
                div();
                break;

            case 16: //other subroutine
                isFlex = processFlex(factory, isFlex, lastVal);
                operandReached = 0; //move to first operator
                break;

            case 17: //POP function
                pop();
                break;

            case 33: //setcurrentpoint
                operandReached = 0; //move to first operator
                break;

            case 34: //hflex
                hflex(factory);
                operandReached = 0; //move to first operator
                break;

            case 35: //fle
                flex(factory);
                operandReached = 0; //move to first operator
                break;

            case 36: //hflex1
                hflex1(factory);
                operandReached = 0; //move to first operator
                break;

            case 37: //flex1
                flex1(factory);
                operandReached = 0; //move to first operator
                break;

            default:
                operandReached = 0; //move to first operator
                break;
        }

        return isFlex;
    }

    private void moveToAsFlex() {

        double val=operandsRead[currentOp+1];
        y += val;
        val=operandsRead[currentOp];
        x += val;
        pt[ptCount]=(float) x;
        ptCount++;

        pt[ptCount]=(float) y;
        ptCount++;

    }

    private void flex() {
        final double val=operandsRead[currentOp];
        y += val;
        pt[ptCount]=(float) x;
        ptCount++;
        pt[ptCount]=(float) y;
        ptCount++;

    }

    /**
     * Save a width value out.
     * @param glyph The name of the glyph.
     * @param rawInt The value of the glyph.
     * @param potentialWidth The width to save.
     */
    private void saveWidth(final String glyph, final int rawInt, final int potentialWidth) {
        if (baseFontName != null &&                             //Check right call
                dynamicVectorRenderer != null &&                    //Check right call
                dynamicVectorRenderer.isHTMLorSVG()) {       //Just to be safe

            if ("notdef".equals(glyph)) {
                dynamicVectorRenderer.saveAdvanceWidth(baseFontName, String.valueOf(rawInt),potentialWidth);
            } else {
                dynamicVectorRenderer.saveAdvanceWidth(baseFontName,glyph,potentialWidth);
            }
            
            //Store the default widths.
            if (!defaultWidthsPassed) {
                for (int i=0; i<defaultWidthX.length; i++) {
                    dynamicVectorRenderer.saveAdvanceWidth(baseFontName, "JPedalDefaultWidth"+i, defaultWidthX[i]);
                }
                defaultWidthsPassed = true;
            }
        }
    }
    
    /**add a charString value*/
    @Override
    public void setCharString(final String glyph, final byte[] stream, final int glyphNo){
        charStrings.put(glyph,stream);
        glyphNumbers.put(glyph, glyphNo);
    }
    
    /**index for each char*/
    @Override
    public void setIndexForCharString(final int index, final String charName){
        if (charForGlyphIndex == null) {
            charForGlyphIndex = new String[65536];
        }
        
        if (index < charForGlyphIndex.length) {
            charForGlyphIndex[index] = charName;
        }
    }
    
    /**index for each char*/
    @Override
    public String getIndexForCharString(final int index){
        return charForGlyphIndex[index];
    }
    
    @Override
    public boolean is1C() {
        return is1C;
    }
    
    /**
     * Used by HTML code
     * @param defaultWidthX
     * @param nominalWidthX
     */
    public void setWidthValues(final int[] defaultWidthX, final int[] nominalWidthX) {
        this.nominalWidthX = nominalWidthX;
        this.defaultWidthX = defaultWidthX;
    }
    
    @Override
    public void setis1C(final boolean is1C) {
        
        this.is1C=is1C;
    }
    
    @Override
    public void setRenderer(final DynamicVectorRenderer current) {
        dynamicVectorRenderer = current;
        
        //Pass default widths
        if (baseFontName != null &&                             //Check right call
                dynamicVectorRenderer != null &&                    //Check right call
                dynamicVectorRenderer.isHTMLorSVG()) {       //Just to be safe
            
            //Store the default widths.
            for (int i=0; i<defaultWidthX.length; i++) {
                dynamicVectorRenderer.saveAdvanceWidth(baseFontName, "JPedalDefaultWidth"+i, defaultWidthX[i]);
            }
        }
    }
    
    /**
     * Pass in array saying which Font DICT to use for each glyph.
     * @param fdSelect
     */
    public void setFDSelect(final int[] fdSelect) {
        this.fdSelect = fdSelect;
    }
    
    /**
     * Return charstrings and subrs - used by PS to OTF converter
     * @return
     */
    @Override
    public Map getCharStrings() {
        return Collections.unmodifiableMap(charStrings);
    }

    /**
     * Return the glyph number of the given glyph name
     * @param glyphName The name of the glyph to find
     * @return The glyph number in the original font file
     */
    public int getGlyphNumber(final String glyphName) {
        return glyphNumbers.get(glyphName);
    }
}
