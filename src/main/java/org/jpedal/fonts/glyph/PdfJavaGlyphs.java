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
 * PdfJavaGlyphs.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.StringUtils;

import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.tt.Table;

public class PdfJavaGlyphs implements PdfGlyphs,Serializable{

    protected boolean hasGIDtoCID;

    /**shapes we have already drawn to speed up plotting, or <code>null</code> if there are none*/
    private Area[] cachedShapes;
    private AffineTransform[] cachedAt;

    /**lookup to translate CMAP chars*/
    public int[] CMAP_Translate;

    protected int glyphCount;

    public boolean isFontInstalled;

    /**default font to use in display*/
    public String defaultFont = "Lucida Sans";

    /**lookup for font names less any + suffix*/
    public String fontName="default";

    //some fonts need to be remapped (ie Arial-BoldMT to Arial,Bold)
    public String logicalfontName="default";

    Map chars=new HashMap();
    Map displayValues=new HashMap();
    Map embeddedChars=new HashMap();

    /**flag is CID font is identity matrix*/
    private boolean isIdentity;
    private boolean isFontEmbedded;
    private boolean hasWidths=true;

    public void flush(){
        cachedShapes = null;
        cachedAt = null;
    }

    @Override
    public String getBaseFontName() {
        return baseFontName;
    }

    public void setBaseFontName(final String baseFontName) {
        this.baseFontName=baseFontName;
    }

    public String baseFontName="";

    public boolean isSubsetted;


    /**copy of Trm so we can choose if cache should be flushed*/
    public float[][] lastTrm=new float[3][3];

    /**current font to plot, or <code>null</code> if not used yet*/
    private Font unscaledFont;

    public boolean isArialInstalledLocally;
    private int maxCharCount=255;
    public boolean isCIDFont;

    public String font_family_name;

    public int style;

    int size;

    String weight,testFont;

    /**used to render page by drawing routines*/
    public static final FontRenderContext frc =new FontRenderContext(null, true, true);

    /**list of installed fonts*/
    private static String[] fontList;


    /**
     * used for standard non-substituted version
     * @param Trm
     * @param rawInt
     * @param displayValue
     * @param currentWidth
     */
    @Override
    public Area getStandardGlyph(final float[][]Trm, final int rawInt, final String displayValue, final float currentWidth, final boolean isSTD) {

        //either calculate the glyph to draw or reuse if already drawn
        Area transformedGlyph2 = getCachedShape(rawInt);

        if (transformedGlyph2 == null) {

            double dY = -1,dX=1,y=0;

            AffineTransform at;

            //allow for text running up the page
            if ((Trm[1][0] < 0 && Trm[0][1] >= 0)||(Trm[0][1] < 0 && Trm[1][0] >= 0)) {
                dX=1f;
                dY=-1f;
            }

            if (isSTD) {

                transformedGlyph2=getGlyph(rawInt, displayValue, currentWidth);

                //hack to fix problem with Java Arial font
                if(transformedGlyph2!=null && rawInt==146 && isArialInstalledLocally) {
                    y = -(transformedGlyph2.getBounds().height - transformedGlyph2.getBounds().y);
                }
            }else {

                GlyphVector gv1 =null;

                //do not show CID fonts as Lucida unless match
                if(!isCIDFont|| isFontInstalled) {
                    gv1 = getUnscaledFont().createGlyphVector(frc, displayValue);
                }

                if(gv1!=null){

                    transformedGlyph2 = new Area(gv1.getOutline());

                    //put glyph into display position
                    double glyphX=gv1.getOutline().getBounds2D().getX();

                    //ensure inside box
                    if(glyphX<0){
                        glyphX=-glyphX;
                        at =AffineTransform.getTranslateInstance(glyphX*2,0);
                        transformedGlyph2.transform(at);
                        //x=-glyphX*2;
                    }

                    final double glyphWidth=gv1.getVisualBounds().getWidth()+(glyphX*2);
                    final double scaleFactor=currentWidth/glyphWidth;
                    if(scaleFactor<1) {
                        dX *= scaleFactor;
                    }
                }
            }

            //create shape for text using transformation to make correct size
            at =new AffineTransform(dX*Trm[0][0],dX*Trm[0][1],dY*Trm[1][0],dY*Trm[1][1] ,0,y);

            if(transformedGlyph2!=null){
                transformedGlyph2.transform(at);
            }

            //save so we can reuse if it occurs again in this TJ command
            setCachedShape(rawInt, transformedGlyph2,at);
        }

        return transformedGlyph2;
    }

    /**returns a generic glyph using inbuilt fonts*/
    public Area getGlyph(final int rawInt, final String displayValue, final float currentWidth){

        boolean fontMatched=true;

        /**use default if cannot be displayed*/
        GlyphVector gv1=null;

        //remap font if needed
        String xx=displayValue;

        /**commented out 18/8/04 when font code updated*/
        //if cannot display return to Lucida
        if(!getUnscaledFont().canDisplay(xx.charAt(0))){
            xx=displayValue;
            fontMatched=false;
        }

        if(this.isCIDFont && isFontEmbedded && fontMatched){
            gv1=null;
        }else if(fontMatched){
            gv1 =getUnscaledFont().createGlyphVector(frc, xx);
        }else{
            Font tempFont = new Font(defaultFont, 0, 1);
            if(!tempFont.canDisplay(xx.charAt(0))) {
                tempFont = new Font("lucida", 0, 1);
            }
            if(tempFont.canDisplay(xx.charAt(0))) {
                gv1 = tempFont.createGlyphVector(frc, xx);
            }
        }

        //gv1 =getUnscaledFont().createGlyphVector(frc, xx);

        Area transformedGlyph2 = null;
        if(gv1!=null){
            transformedGlyph2=new Area(gv1.getOutline());

            //put glyph into display position
            double glyphX=gv1.getOutline().getBounds2D().getX();
            //double glyphY=gv1.getOutline().getBounds2D().getY();
            final double width=gv1.getOutline().getBounds2D().getWidth();

            AffineTransform at;

            if(!hasWidths){ //center for looks
                //try standard values which are indexed under NAME of char
                //String charName = StandardFonts.getUnicodeChar(StandardFonts.WIN, rawInt);
                final float leading=(float)(currentWidth-(width+glyphX+glyphX))/2;

                if(leading>0){
                    at =AffineTransform.getTranslateInstance(leading,0);
                    transformedGlyph2.transform(at);
                }
            }else{

                if(glyphX<0){ //ensure inside box
                    glyphX=-glyphX;
                    at =AffineTransform.getTranslateInstance(glyphX,0);
                    transformedGlyph2.transform(at);
                }

                final double scaleFactor=currentWidth/(transformedGlyph2.getBounds2D().getWidth());
                if(scaleFactor<1)	{
                    at =AffineTransform.getScaleInstance(scaleFactor,1);
                    transformedGlyph2.transform(at);
                }
            }
        }

        return transformedGlyph2;
    }

    /**
     * Caches the specified shape.
     */
    public final void setCachedShape(final int idx, final Area shape, final AffineTransform at) {
        // using local variable instead of sync'ing
        Area[] cache = cachedShapes;
        AffineTransform[] atCache=cachedAt;

        if (cache == null){
            cachedShapes = cache = new Area[maxCharCount];
            cachedAt = atCache = new AffineTransform[maxCharCount];
        }

        if(shape==null) {
            cache[idx] = null;
        } else {
            cache[idx] = shape;
        }

        if(shape!=null && at!=null) {
            atCache[idx] = at;
        }
    }

    /**
     * Returns the specified shape from the cache, or <code>null</code> if the shape
     * is not in the cache.
     */
    public final AffineTransform getCachedTransform(final int idx) {
        // using local variable instead of sync'ing
        final AffineTransform[] cache = cachedAt;

        if(cache==null) {
            return null;
        } else {
            return cache[idx];
        }

    }

    /**
     * Returns the specified shape from the cache, or <code>null</code> if the shape
     * is not in the cache.
     */
    public final Area getCachedShape(final int idx) {
        // using local variable instead of sync'ing
        final Area[] cache = cachedShapes;

        if(cache==null) {
            return null;
        } else{
            final Area currentShape=cache[idx];

            if(currentShape==null) {
                return null;
            } else {
                return currentShape;
            }
        }
        //return cache == null ? null : (Area)cache[idx].clone();
        //return cache == null ? null : cache[idx];
    }

    public void init(final int maxCharCount, final boolean isCIDFont) {
        this.maxCharCount=maxCharCount;
        this.isCIDFont=isCIDFont;
    }

    /**set the font being used or try to approximate*/
    public final void setFont(String name, final int size) {

        this.size=size;

        //allow user to totally over-ride
        //passing in this allows user to reset any global variables
        //set in this method as well.
        //Helper is a static instance of the inteface JPedalHelper
        if(DecoderOptions.Helper!=null){
            final Font f=DecoderOptions.Helper.setFont(this, StringUtils.convertHexChars(name),size);
            //if you want to implement JPedalHelper but not
            //use this function, just return null
            if(f!=null) {
                this.style = f.getStyle();
                this.font_family_name = f.getFamily();
                this.unscaledFont = f;
                return;
            }

        }

        name=StandardFonts.expandName(name);

        //set defaults
        this.font_family_name=name;
        this.style =Font.PLAIN;

        String mappedName=null;

        if(font_family_name==null) {
            font_family_name = this.fontName;
        }

        testFont=font_family_name;
        if(font_family_name!=null) {
            testFont = font_family_name.toLowerCase();
        }

        //pick up any weight in type 3 font or - standard font mapped to Java
        int pointer = font_family_name.indexOf(',');
        if ((pointer == -1))//&&(StandardFonts.javaFontList.get(font_family_name)!=null))
        {
            pointer = font_family_name.indexOf('-');
        }

        if (pointer != -1) {

            //see if present with ,
            mappedName=(String) FontMappings.fontSubstitutionAliasTable.get(testFont);


            weight =testFont.substring(pointer + 1, testFont.length());

            style = getWeight(weight);

            font_family_name = font_family_name.substring(0, pointer).toLowerCase();

            testFont=font_family_name;

            if(testFont.endsWith("mt")) {
                testFont = testFont.substring(0, testFont.length() - 2);
            }

        }

        //remap if not type 3 match
        if(mappedName==null) {
            mappedName = (String) FontMappings.fontSubstitutionAliasTable.get(testFont);
        }

        if((mappedName!=null)&&(mappedName.equals("arialbd"))) {
            mappedName = "arial-bold";
        }

        if(mappedName!=null){

            font_family_name=mappedName;

            pointer = font_family_name.indexOf('-');
            if(pointer!=-1){

                font_family_name=font_family_name.toLowerCase();

                weight =font_family_name.substring(pointer + 1, font_family_name.length());

                style = getWeight(weight);

                font_family_name = font_family_name.substring(0, pointer);
            }

            testFont=font_family_name.toLowerCase();

            if(testFont.endsWith("mt")) {
                testFont = testFont.substring(0, testFont.length() - 2);
            }

        }



    }

    /**
     * work out style (ITALIC, BOLD)
     */
    private static int getWeight(String weight) {

        int style=Font.PLAIN;

        if(weight.endsWith("mt")) {
            weight = weight.substring(0, weight.length() - 2);
        }

        if (weight.contains("heavy")) {
            style = Font.BOLD;
        } else if (weight.contains("bold")) {
            style = Font.BOLD;
        } else if (weight.contains("roman")) {
            style = Font.ROMAN_BASELINE;
        }

        if (weight.contains("italic")) {
            style += Font.ITALIC;
        } else if (weight.contains("oblique")) {
            style += Font.ITALIC;
        }

        return style;
    }


    /**
     * Returns the unscaled font, initializing it first if it hasn't been used before.
     */
    public final Font getUnscaledFont() {

            if (unscaledFont == null && font_family_name!=null) {
                //Recheck
                if (fontList == null) {

                    //Make sure lowercase
                    fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                    for (int i = 0; i < fontList.length; i++) {
                        fontList[i] = fontList[i].toLowerCase();
                    }
                }

                //see if installed
                if (fontList != null) {

                    //check exact first
                    boolean isFound = false;
                    int count = fontList.length;
                    for (int i = 0; i < count; i++) {
                        if ((fontList[i].equals(testFont)) || ((weight == null) && (testFont.startsWith(fontList[i])))) {
                            isFontInstalled = true;
                            font_family_name = fontList[i];
                            i = count;
                            isFound = true;
                        }
                    }

                    if (!isFound) {
                        count = fontList.length;
                        for (int i = 0; i < count; i++) {
                            if ((fontList[i].equals(testFont)) || ((weight == null) && (testFont.startsWith(fontList[i])))) {
                                isFontInstalled = true;
                                font_family_name = fontList[i];
                                i = count;
                            }
                        }
                    }

                    //hack for windows as some odd things going on
                    if (isFontInstalled && font_family_name.equals("arial")) {
                        isArialInstalledLocally = true;
                    }
                }

                /**approximate display if not installed*/
                if (!isFontInstalled) {

                    //try to approximate font
                    if (weight == null) {

                        //pick up any weight
                        final String test = font_family_name.toLowerCase();
                        style = getWeight(test);

                    }

                    font_family_name = defaultFont;
                }

                unscaledFont = new Font(font_family_name, style, size);
            }

            /**commenting out  this broke originaldoc.pdf*/
            if (unscaledFont == null) {
                unscaledFont = new Font(defaultFont, Font.PLAIN, 1);
            }

        return unscaledFont;
    }

    protected PdfGlyph[] cachedEmbeddedShapes;

    protected int localBias,globalBias;

    /**
     * Caches the specified shape.
     */
    public final void setEmbeddedCachedShape(final int idx, final PdfGlyph shape) {
        // using local variable instead of sync'ing
        PdfGlyph[] cache = cachedEmbeddedShapes;
        if (cache == null) {
            cachedEmbeddedShapes = cache = new PdfGlyph[maxCharCount];
        }

        if(idx<cache.length) {
            cache[idx] = shape;
        }
    }

    /**
     * Returns the specified shape from the cache, or <code>null</code> if the shape
     * is not in the cache.
     */
    public final PdfGlyph getEmbeddedCachedShape(final int idx) {
        // using local variable instead of sync'ing
        final PdfGlyph[] cache = cachedEmbeddedShapes;

        if(cache==null) {
            return null;
        } else if(idx<cache.length){
            final PdfGlyph currentShape=cache[idx];

            if(currentShape==null) {
                return null;
            } else {
                return currentShape;
            }
        }else {
            return null;
        }
        //return cache == null ? null : (Area)cache[idx].clone();
        //return cache == null ? null : cache[idx];
    }

    /**
     * template used by t1/t3/tt fonts
     */
    @Override
    public PdfGlyph getEmbeddedGlyph(final GlyphFactory factory, final String glyph, final float[][] trm, final int rawInt, final String displayValue, final float currentWidth, final String key) {
        return null;
    }

    public void setGIDtoCID(final int[] cidToGIDMap) {
    }

    public void setEncodingToUse(final boolean hasEncoding, final int fontEncoding, final boolean isCIDFont) {

    }


    public int readEmbeddedFont(final boolean TTstreamisCID, final byte[] fontDataAsArray, final FontData fontData) {
        //
        return 0;
    }

    public void setIsSubsetted(final boolean b) {
        isSubsetted=b;
    }

    public void setT3Glyph(final int key, final int altKey, final PdfGlyph glyph) {

    }


    public void setCharString(final String s, final byte[] bytes, final int glyphNo) {
    }

    public boolean is1C() {
        return false;
    }

    public void setis1C(final boolean b) {
    }


    public void setValuesForGlyph(final int rawInt, final String charGlyph, final String displayValue, final String embeddedChar) {
        final Integer key= rawInt;
        chars.put(key,charGlyph);
        displayValues.put(key,displayValue);
        embeddedChars.put(key,embeddedChar);
    }

    @Override
    public String getDisplayValue(final Integer key) {
        return (String) displayValues.get(key);
    }

    @Override
    public String getCharGlyph(final Integer key) {
        return (String) chars.get(key);
    }

    @Override
    public String getEmbeddedEnc(final Integer key) {

        return (String) embeddedChars.get(key);
    }

    public Map getDisplayValues() {
        return Collections.unmodifiableMap(displayValues);
    }

    public Map getCharGlyphs() {
        return Collections.unmodifiableMap(chars);
    }

    public Map getEmbeddedEncs() {

        return  Collections.unmodifiableMap(embeddedChars);
    }

    public void setDisplayValues(final Map displayValues) {
        this.displayValues=displayValues;
    }

    public void setCharGlyphs(final Map chars) {
        this.chars=chars;
    }

    public void setEmbeddedEncs(final Map embeddedChars) {

        this.embeddedChars=embeddedChars;
    }

    public void setLocalBias(final int i) {
        localBias=i;

    }

    public void setGlobalBias(final int i) {
        globalBias=i;

    }

    @SuppressWarnings("UnusedParameters")
    public float getTTWidth(final String charGlyph, final int rawInt, final String displayValue, final boolean b) {

        //<start-demo>

        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    @SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
    public static String getPostName(final int rawInt) {
        return "notdef";
    }

    /**
     * should never be called - just to allow TTGlyphs to extend
     * @param rawInt
     */
    public int getConvertedGlyph(final int rawInt) {
        return -1;
    }

    /**
     * flag for CID TT fonts
     * @param isIdentity
     */
    public void setIsIdentity(final boolean isIdentity) {
        this.isIdentity=isIdentity;
    }

    /**
     * flag to show if CID TT fonts have identity matrix
     * @return
     */
    public boolean isIdentity() {
        return isIdentity;
    }

    @SuppressWarnings("UnusedDeclaration")
    public float[] getFontBoundingBox() {
        return new float[]{0f, 0f, 1000f, 1000f};
    }

    public void setFontEmbedded(final boolean isSet) {
        isFontEmbedded =isSet;
    }

    public int getType() {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public void setHasWidths(final boolean hasWidths) {
        this.hasWidths=hasWidths;
    }

    /**
     * return value or -1
     */
    public int getCMAPValue(final int rawInt) {
        if(CMAP_Translate==null) {
            return -1;
        } else{
            //System.out.println(rawInt+" becomes "+CMAP_Translate[rawInt]);
            return (CMAP_Translate[rawInt]);
        }
    }


    public boolean isCorrupted() {
        return false;
    }

    public void setCorrupted(final boolean corrupt) {

    }

    //used by PS to OTF convertor
    public void setIndexForCharString(final int jj, final String glyphName) {
    }

    //used by PS to OTF convertor
    public String getIndexForCharString(final int jj) {
        return null;
    }

    //used by PS to OTF converter
    public Map getCharStrings() {
        return null;
    }

    public void setGlyphCount(final int nGlyphs) {
        glyphCount=nGlyphs;
    }

    public int getGlyphCount() {
        return glyphCount;
    }

    public void setRenderer(final DynamicVectorRenderer current) {
    }

    public Table getTable(final int LOCA) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public boolean hasGIDtoCID() {
        return hasGIDtoCID;
    }
}
