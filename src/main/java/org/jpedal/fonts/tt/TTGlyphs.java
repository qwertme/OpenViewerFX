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
 * TTGlyphs.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import java.util.Map;

import org.jpedal.fonts.glyph.GlyphFactory;
import org.jpedal.fonts.glyph.MarkerGlyph;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;

import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.PdfDecoderInt;

import org.jpedal.utils.LogWriter;

import org.jpedal.fonts.tt.hinting.TTVM;

public class TTGlyphs extends PdfJavaGlyphs {

    protected int[] CIDToGIDMap;

    float[] FontBBox= {0f,0f,1000f,1000f};

    boolean isCorrupted;

    private CMAP currentCMAP;
    private Post currentPost;
    private Glyf currentGlyf;
    private Hmtx currentHmtx;
    private Hhea currentHhea;
    private Head currentHead;

    private FontFile2 fontTable;

    //private Head currentHead;
    //private Name currentName;
    //private Maxp currentMapx;
    private Loca currentLoca;

    private TTVM vm;

    //private Hhea currentHhea;

    private CFF currentCFF;

    //int glyphCount=0;

    //assume TT and set to OTF further down
    int type= StandardFonts.TRUETYPE;

    private int unitsPerEm;

    private boolean hasCFF;

    private boolean isCID;

    /**
     * used by  non type3 font
     */
    @Override
    public PdfGlyph getEmbeddedGlyph(final GlyphFactory factory, final String glyph, final float[][]Trm, int rawInt,
                                     final String displayValue, final float currentWidth, final String key) {

        final int id=rawInt;
        if(hasGIDtoCID && isIdentity()){
            
            rawInt=CIDToGIDMap[rawInt];
           
        }
        /**flush cache if needed*/
        if(Trm!=null && (lastTrm[0][0]!=Trm[0][0])|(lastTrm[1][0]!=Trm[1][0])|
                (lastTrm[0][1]!=Trm[0][1])|(lastTrm[1][1]!=Trm[1][1])){
            lastTrm=Trm;
            flush();
        }

        //either calculate the glyph to draw or reuse if alreasy drawn
        PdfGlyph transformedGlyph2 = getEmbeddedCachedShape(id);

        if (transformedGlyph2 == null) {

            //use CMAP to get actual glyph ID
            int idx=rawInt;
            
            
            if((!isCID || !isIdentity()) && currentCMAP!=null) {
                idx = currentCMAP.convertIndexToCharacterCode(glyph, rawInt);
            }
            
            //if no value use post to lookup
            if(idx<1){
                idx = currentPost.convertGlyphToCharacterCode(glyph);
            }
            
            //shape to draw onto
            try{
                if(hasCFF){

                    transformedGlyph2=currentCFF.getCFFGlyph(factory,glyph,Trm,idx, displayValue,currentWidth,key);

                    //set raw width to use for scaling
                    if(transformedGlyph2!=null) {
                        transformedGlyph2.setWidth(getUnscaledWidth(glyph, rawInt, false));
                    }

                }else {
                    transformedGlyph2 = getTTGlyph(idx, glyph, rawInt, displayValue, factory);
                }
            }catch(final Exception e){
                //noinspection UnusedAssignment
                transformedGlyph2=null;

                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //

                //<end-demo>


            }

            //save so we can reuse if it occurs again in this TJ command
            setEmbeddedCachedShape(id, transformedGlyph2);
        }

        return transformedGlyph2;
    }

    /*
     * creates glyph from truetype font commands
     */
    private PdfGlyph getTTGlyph(int idx, final String glyph, final int rawInt, final String displayValue, final GlyphFactory factory) {

        if(isCorrupted) {
            idx = rawInt;
        }

        PdfGlyph currentGlyph=null;

        try{
            //final boolean debug=(rawInt==2465);
            BaseTTGlyph.debug=false;


            if(idx!=-1){
                //move the pointer to the commands
                final int p=currentGlyf.getCharString(idx);

                if(p!=-1){
                    
                    if(factory.useFX()){
                        currentGlyph=factory.getGlyph(currentGlyf, fontTable, currentHmtx, idx, (unitsPerEm / 1000f), vm,baseFontName);
                        
                    }else if (TTGlyph.useHinting) {
                            currentGlyph = new TTGlyph(currentGlyf, fontTable, currentHmtx, idx, (unitsPerEm / 1000f), vm);
                        } else {
                            currentGlyph = new TTGlyph(currentGlyf, fontTable, currentHmtx, idx, (unitsPerEm / 1000f), baseFontName);
                        }
                    
                    if(BaseTTGlyph.debug) {
                        System.out.println(">>" + p + ' ' + rawInt + ' ' + displayValue + ' ' + baseFontName);
                    }

                } else if (!factory.useFX() && (" ".equals(glyph) || " ".equals(displayValue))) {
                    //Add a marker glyph in to record the number of the space glyph
                    currentGlyph = new MarkerGlyph(0,0,0,0,baseFontName);
                    currentGlyph.setGlyphNumber(idx+1);
                }
            }

        }catch(final Exception ee){
            ee.printStackTrace(System.out);

            //
        }

        //if(glyph.equals("fl"))

        return currentGlyph;
    }

    @Override
    public void setEncodingToUse(final boolean hasEncoding, final int fontEncoding, final boolean isCIDFont) {

        if(currentCMAP!=null){
            if(isCorrupted) {
                currentCMAP.setEncodingToUse(hasEncoding, fontEncoding, isCIDFont);
            } else {
                currentCMAP.setEncodingToUse(hasEncoding, fontEncoding, isCIDFont);
            }
        }
    }

    @Override
    public int getConvertedGlyph(final int idx){

        if(currentCMAP==null) {
            return idx;
        } else {
            return currentCMAP.convertIndexToCharacterCode(null, idx);
        }

    }

    /**
     * Return charstrings and subrs - used by PS to OTF converter
     * @return
     */
    @Override
    public Map getCharStrings() {

        if(currentCMAP!=null){
            return currentCMAP.buildCharStringTable();
        }else{
            return currentGlyf.buildCharStringTable();
        }
    }

    /*
     * creates glyph from truetype font commands
     */
    @Override
    public float getTTWidth(final String glyph, final int rawInt, final String displayValue, final boolean TTstreamisCID) {

        //use CMAP if not CID
        int idx=rawInt;

        float width=0;

        try{
            if((!TTstreamisCID)) {
                idx = currentCMAP.convertIndexToCharacterCode(glyph, rawInt);
            }

            //if no value use post to lookup
            if(idx<1) {
                idx = currentPost.convertGlyphToCharacterCode(glyph);
            }

            //if(idx!=-1)
            width=currentHmtx.getWidth(idx);

        }catch(final Exception e){
            //<end-demo>
             if (LogWriter.isOutput()) {
                LogWriter.writeLog("Attempting to read width " + e);
            }
        }

        return width;
    }

    /*
     * creates glyph from truetype font commands
     */
    private float getUnscaledWidth(final String glyph, final int rawInt, final boolean TTstreamisCID) {

        //use CMAP if not CID
        int idx=rawInt;

        float width=0;

        try{
            if((!TTstreamisCID)) {
                idx = currentCMAP.convertIndexToCharacterCode(glyph, rawInt);
            }

            //if no value use post to lookup
            if(idx<1) {
                idx = currentPost.convertGlyphToCharacterCode(glyph);
            }

            //if(idx!=-1)
            width=currentHmtx.getUnscaledWidth(idx);

        }catch(final Exception e){
            //<end-demo>
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Attempting to read width " + e);
            }

        }

        return width;
    }


    @Override
    public void setGIDtoCID(final int[] cidToGIDMap) {

        hasGIDtoCID=true;
        this.CIDToGIDMap=cidToGIDMap;

    }

    /**
     * return name of font or all fonts if TTC
     * NAME will be LOWERCASE to avoid issues of capitalisation
     * when used for lookup - if no name, will default to  null
     *
     * Mode is PdfDecoder.SUBSTITUTE_* CONSTANT. RuntimeException will be thrown on invalid value
     */
    public static String[] readFontNames(final FontData fontData, final int mode) {

        /**setup read the table locations*/
        final FontFile2 currentFontFile=new FontFile2(fontData);

        //get type
        //int fontType=currentFontFile.getType();

        final int fontCount=currentFontFile.getFontCount();

        final String[] fontNames=new String[fontCount];

        /**read tables for names*/
        for(int i=0;i<fontCount;i++){

            currentFontFile.setSelectedFontIndex(i);

            final Name currentName=new Name(currentFontFile);

            final String name;

            if(mode==PdfDecoderInt.SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME){
                name=currentName.getString(Name.POSTSCRIPT_NAME);
            }else if(mode==PdfDecoderInt.SUBSTITUTE_FONT_USING_FAMILY_NAME){
                name=currentName.getString(Name.FONT_FAMILY_NAME);
            }else if(mode==PdfDecoderInt.SUBSTITUTE_FONT_USING_FULL_FONT_NAME){
                name=currentName.getString(Name.FULL_FONT_NAME);
            }else{ //tell user if invalid
                throw new RuntimeException("Unsupported mode "+mode+". Unable to resolve font names");
            }

            if(name==null){
                fontNames[i]=null;
            }else{
                fontNames[i]=name.toLowerCase();
            }
        }

        if(fontData!=null) {
            fontData.close();
        }

        return fontNames;
    }

    /**
     * Add font details to Map so we can access later
     */
    public static void addStringValues(final FontData fontData, final Map fontDetails) {

        /**setup read the table locations*/
        final FontFile2 currentFontFile=new FontFile2(fontData);

        //get type
        //int fontType=currentFontFile.getType();

        final int fontCount=currentFontFile.getFontCount();

        /**read tables for names*/
        for(int i=0;i<fontCount;i++){

            currentFontFile.setSelectedFontIndex(i);

            final Name currentName=new Name(currentFontFile);

            final Map stringValues= currentName.getStrings();


            if(stringValues!=null){
                for (final Object o : stringValues.keySet()) {
                    final Integer currentKey = (Integer) o;

                    final int keyInt = currentKey;
                    if (keyInt < Name.stringNames.length) {
                        fontDetails.put(Name.stringNames[currentKey], stringValues.get(currentKey));
                    }
                }
            }
        }

        if(fontData!=null) {
            fontData.close();
        }
    }

    @Override
    public int readEmbeddedFont(final boolean TTstreamisCID, final byte[] fontDataAsArray, final FontData fontData) {

        final FontFile2 currentFontFile;

        isCID=TTstreamisCID;

        /**setup read the table locations*/
        if(fontDataAsArray!=null) {
            currentFontFile = new FontFile2(fontDataAsArray);
        } else {
            currentFontFile = new FontFile2(fontData);
        }

        //select font if TTC
        //does nothing if TT
        if(FontMappings.fontSubstitutionFontID==null){
            currentFontFile.setPointer(0);
        }else{
            final Integer fontID= (Integer) FontMappings.fontSubstitutionFontID.get(fontName.toLowerCase());

            if(fontID!=null) {
                currentFontFile.setPointer(fontID);
            } else {
                currentFontFile.setPointer(0);
            }
        }

        /**read tables*/
        currentHead=new Head(currentFontFile);

        currentPost=new Post(currentFontFile);

        //currentName=new Name(currentFontFile);



        final Maxp currentMaxp =new Maxp(currentFontFile);
        glyphCount= currentMaxp.getGlyphCount();
        currentLoca=new Loca(currentFontFile,glyphCount,currentHead.getIndexToLocFormat());

        isCorrupted=currentLoca.isCorrupted();

        currentGlyf=new Glyf(currentFontFile,glyphCount,currentLoca.getIndices());

        currentCFF=new CFF(currentFontFile,isCID);

        hasCFF=currentCFF.hasCFFData();
        if(hasCFF) {
            type = StandardFonts.OPENTYPE;
        }

        //currentCvt=new Cvt(currentFontFile);

        if(TTGlyph.useHinting){
            //Classes in hinting package which we will delete in lgpl
            vm=new TTVM(currentFontFile, currentMaxp);
        }

        currentHhea=new Hhea(currentFontFile);

        FontBBox=currentHead.getFontBBox();

        currentHmtx=new Hmtx(currentFontFile,glyphCount,currentHhea.getNumberOfHMetrics(),(int)FontBBox[3]);

        //not all files have CMAPs
        //if(!TTstreamisCID){
        final int startPointer=currentFontFile.selectTable(FontFile2.CMAP);

        if(startPointer!=0) {
            currentCMAP = new CMAP(currentFontFile, startPointer);
        }

        //}

        unitsPerEm=currentHead.getUnitsPerEm();

        fontTable=new FontFile2(currentGlyf.getTableData(),true);

        if(fontData!=null) {
            fontData.close();
        }

        return type;
    }

    @Override
    public float[] getFontBoundingBox() {
        return FontBBox;
    }

    @Override
    public int getType() {
        return type;
    }

    //flag if Loca broken so we need to try and Substitute
    @Override
    public boolean isCorrupted() {
        return isCorrupted;
    }

    @Override
    public void setCorrupted(final boolean corrupt) {
        isCorrupted=corrupt;
    }

    @Override
    public Table getTable(final int type){

        final Table table;
        switch(type){
            case FontFile2.LOCA:
                table=currentLoca;
                break;

            case FontFile2.CMAP:
                table=currentCMAP;
                break;

            case FontFile2.HHEA:
                table=currentHhea;
                break;

            case FontFile2.HMTX:
                table=currentHmtx;
                break;

            case FontFile2.HEAD:
                table=currentHead;
                break;

            default:
                throw new RuntimeException("table not yet added to getTable)");
        }

        return table;
    }
}
