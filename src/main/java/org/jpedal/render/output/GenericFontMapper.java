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
 * GenericFontMapper.java
 * ---------------
 */
package org.jpedal.render.output;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jpedal.fonts.StandardFonts;


/**
 * control translation of fonts for display
 */
public class GenericFontMapper implements org.jpedal.render.output.FontMapper {

    private static final String DEFAULT_FONT = "DEFAULT_FONT";

    //weight
    String style="normal";
    String weight="normal";
    String family;

    private String fontID;

    private int fontMode=DEFAULT_ON_UNMAPPED;

    private boolean isFontEmbedded;
    private boolean isFontSubstituted;

    //list of font mappings to substitute fonts
    public static final Map<String, String> fontMappings = new HashMap<String,String>();
    public static final Map<String, Integer> fontSizeAdjustments = new HashMap<String, Integer>();
    
    //original name in PDF
    private String rawFont;

    //<link><a name="fonts" />

    //setup font substitutions once
    static{

        //Mappings reference: http://www.ampsoft.net/webdesign-l/WindowsMacFonts.html
        final String arialType = "Arial, Helvetica, sans-serif";
        final String arialBlackType = "'Arial Black', Gadget, sans-serif";
        final String comicSansType = "'Comic Sans MS', Textile, cursive";
        final String courierNewType = "'Courier New', Courier, monospace";
        final String georgiaType = "Georgia, 'Times New Roman', Times, serif";
        final String impactType = "Impact, Charcoal, sans-serif";
        final String lucidaConsoleType = "'Lucida Console', Monaco, monospace";
        final String lucidaSansType = "'Lucida Sans Unicode', 'Lucida Grande', sans-serif";
        final String palatinoType = "'Palatino Linotype', 'Book Antiqua', Palatino, serif";
        final String tahomaType = "Tahoma, Geneva, sans-serif";
        final String romanType = "'Times New Roman', Times, serif";
        final String trebuchetType = "'Trebuchet MS', Helvetica, sans-serif";
        final String verdanaType = "Verdana, Geneva, sans-serif";
        final String symbolType = "Symbol";
        final String webdingsType = "Webdings";
        final String wingdingsType = "Wingdings, 'Zapf Dingbats'";
        final String msSansSerifType = "'MS Sans Serif', Geneva, sans-serif";
        final String msSerifType = "'MS Serif', 'New York', serif";
        final String helveticaType =  "Helvetica, Arial, sans-serif";

        if(fontMappings.keySet().isEmpty()){
            //Default fonts
            fontMappings.put("Arial", arialType);
            fontMappings.put("ArialMT", arialType);
            fontMappings.put("ArialBlack", arialBlackType);
            fontMappings.put("ComicSansMS", comicSansType);
            fontMappings.put("CourierNew", courierNewType);
            fontMappings.put("Georgia", georgiaType);
            fontMappings.put("Impact", impactType);
            fontMappings.put("LucidaConsole", lucidaConsoleType);
            fontMappings.put("LucidaSansUnicode", lucidaSansType);
            fontMappings.put("PalatinoLinotype", palatinoType);
            fontMappings.put("Tahoma", tahomaType);
            fontMappings.put("TimesNewRoman", romanType);
            fontMappings.put("Times", romanType);
            fontMappings.put("Trebuchet", trebuchetType);
            fontMappings.put("Verdana", verdanaType);
            fontMappings.put("Symbol", symbolType);
            fontMappings.put("Webdings", webdingsType);
            fontMappings.put("Wingdings", wingdingsType);
            fontMappings.put("MSSansSerif", msSansSerifType);
            fontMappings.put("MSSerif", msSerifType);
            fontMappings.put("Helvetica", helveticaType);
            fontMappings.put("ZapfDingbats", wingdingsType);

            fontMappings.put(DEFAULT_FONT, romanType);
            fontSizeAdjustments.put(DEFAULT_FONT, -1); //Err on the side of caution for default fonts.
        }
    }

    public GenericFontMapper(final String rawFont)
    {
        init(rawFont);

        this.rawFont=rawFont;
    }

    public GenericFontMapper(final String rawFont, final int fontMode, final boolean isFontEmbedded, final boolean isFontSubstituted) {

        this.fontMode = fontMode;
        this.isFontEmbedded = isFontEmbedded;
        this.isFontSubstituted = isFontSubstituted;
        this.rawFont = rawFont;

        init(rawFont);

    }

    private void init(final String rawFont)
    {

        if(fontMode==EMBED_ALL || fontMode==EMBED_ALL_EXCEPT_BASE_FAMILIES){  //Arial-Bold or Arial,bold need splitting into family and weight

            fontID=rawFont;

            //limit to nonn-emebedded of Standard (ie TNR, Arial)
            if(!isFontEmbedded || StandardFonts.isStandardFont(rawFont,true)){

                int ptr=rawFont.indexOf(',');
                if(ptr==-1){
                    ptr=rawFont.indexOf('-');
                }

                if(ptr==-1) {

                    //Split at the last number
                    for(int i = (rawFont.length() - 1); i >= 0; i--) {
                        final int pt = rawFont.codePointAt(i);
                        if(pt >= 0x30 && pt <= 0x39) {
                            if(i < (rawFont.length() - 1)) {
                                ptr = i - 1;
                            }
                            break;
                        }
                    }
                }

                if(ptr>0){
                    findAttributes(rawFont);
                }
           }

            //Font exists as it is in mappings
        }else if(!mapFont(rawFont)) {
            final String fontLessAttributes = findAttributes(rawFont);

            //Does the font name minus attributes exist in mappings?
            if(!mapFont(fontLessAttributes)) {

                //If there isnt a similiar one use the default.
                if(!hasSimiliarMapping(fontLessAttributes)) {
                    switch(this.fontMode) {
                        case DEFAULT_ON_UNMAPPED:
                            fontID = DEFAULT_FONT;
                            break;

                        case FontMapper.FAIL_ON_UNMAPPED:
                            throw new RuntimeException("Font " + rawFont + " not mapped");
                    }
                }
            }
        }
    }

    /**
     * Strip out and set font attributes returning the font name
     * @param rawFont
     * @return String contains the name of the font
     */
    private String findAttributes(final String rawFont)
    {
        String result = rawFont;

        int ptr = rawFont.indexOf(',');

        if(ptr==-1) {
            ptr = rawFont.indexOf('-');
        }

        if(ptr==-1) {
            ptr = rawFont.lastIndexOf(' ');
        }
        
        if(ptr==-1) {

            //Split at the last number
            for(int i = (rawFont.length() - 1); i >= 0; i--) {
                final int pt = rawFont.codePointAt(i);
                if(pt >= 0x30 && pt <= 0x39) {
                    if(i < (rawFont.length() - 1)) {
                        ptr = i - 1;
                    }
                    break;
                }
            }
        }

        if(ptr != -1) {
            final String fontAttributes = rawFont.substring(ptr+1, rawFont.length()).toLowerCase();
            result = rawFont.substring(0, ptr);
            family=result; //font less any -, or number
           
            boolean isFontExists=false;
            
            for(final String k : fontMappings.keySet()){
            	if(k.startsWith(family)){
            		isFontExists = true;
            	}
            }
            
            if(isFontExists || !isFontEmbedded){

            	if(fontAttributes.contains("heavy")) {
            		weight = "900";
            	}
            	else if(fontAttributes.endsWith("black")) {
            		weight = "bolder";
            	}
            	else if(fontAttributes.contains("light")) {
            		weight = "lighter";
            	}
            	else if(fontAttributes.contains("condensed")) {
            		weight = "100";
            	}
            	else if(fontAttributes.contains("bold")) {
            		weight = "bold";
            	}

            	/**
            	 * and style
            	 */
            	if(fontAttributes.equals("it") || fontAttributes.contains("italic") || fontAttributes.contains("kursiv") || fontAttributes.contains("oblique")) {
            		style = "italic";
            	}

            }

        }
        
        return result;
    }

    /**
     * See if font is in mappings and set the font ID.  Return false if its not.
     * @param s String to check
     * @return true if it maps
     */
    private boolean mapFont(final String s)
    {
        if(fontMappings.get(s) != null) {
            fontID = s;
            return true;
        }
        return false;
    }

    /**
     * Search mappings for a one that sounds close.
     * @param fontName
     * @return
     */
    private boolean hasSimiliarMapping(final String fontName)
    {
        final Set<String> keySet = fontMappings.keySet();
        final Set<String> candidates = new HashSet<String>();

        for(final String key : keySet) {
            final String lcKey = key.toLowerCase();
            final String lcFont = fontName.toLowerCase();

            if(lcKey.equals(lcFont)) {
                fontID = key;
                return true;
            }

            if(lcKey.contains(lcFont) || lcFont.contains(lcKey)) {
                candidates.add(key);
            }
        }

        if(!candidates.isEmpty()) {
            String result[] = new String[candidates.size()];
            result = candidates.toArray(result);
            fontID = result[0];

            //@TODO Just get the shortest one for the time being.
            if(candidates.size()>1) {
                for(int i = 1; i < result.length; i++) {
                    if(result[i].length() < fontID.length()) {
                        fontID = result[i];
                    }
                }
            }
            return true;
        }

        return false;
    }
    
    @Override
    public String getFont() {

        String result = fontMappings.get(fontID);

        if(result == null && family != null && !isFontEmbedded){
            result = fontMappings.get(family);
        }
        
        if (result == null && (fontMode==EMBED_ALL || fontMode==EMBED_ALL_EXCEPT_BASE_FAMILIES)){ //just pass through
            rawFont = rawFont.replaceAll("[.,@*#]", "-");
            result = rawFont;
        } else if (result != null && isFontEmbedded && fontMode == EMBED_ALL){//if embedded font called WingDings we want to pass pack font name not generic mappings
            //for general discussion should make it live for all values
          //  System.out.println(fontID+" "+result);
            result = fontID;
        }

        return (result == null) ? "" : result;
    }


    @Override
    public String getStyle() {
        if (isFontEmbedded) {
            style = "normal";
        }
        return style;
    }

    @Override
    public String getWeight() {
        if (isFontEmbedded) {
            weight = "normal";
        }
        return weight;
    }

    @Override
    public boolean isFontEmbedded() {
        return isFontEmbedded;
    }

    @Override
    public boolean isFontSubstituted() {
        return isFontSubstituted;
    }

    @Override
    public boolean equals(FontMapper fontMapper) {
        return this.getStyle().equals(fontMapper.getStyle()) &&
                this.getFont().equals(fontMapper.getFont()) &&
                this.getWeight().equals(fontMapper.getWeight()) &&
                isFontEmbedded == fontMapper.isFontEmbedded();
    }

}
