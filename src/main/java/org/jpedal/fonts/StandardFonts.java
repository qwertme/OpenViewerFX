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
 * StandardFonts.java
 * ---------------
 */
package org.jpedal.fonts;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.jpedal.utils.LogWriter;
import org.jpedal.fonts.tt.*;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.fonts.glyph.T1Glyphs;

public class StandardFonts {
    
    /**holds names of every character*/
    private static Map unicode_name_mapping_table = new HashMap();
    
    /**holds lookup to map char values for decoding char into name*/
    private static String[][] unicode_char_decoding_table = new String[7][335];
    
    public static final int PDF = 6;
    
    /**flag used to identify ZAPF encoding*/
    public static final int ZAPF = 5;
    
    /**flag used to identify Symbol encoding*/
    public static final int SYMBOL = 4;
    
    /**flag used to identify MacExpert encoding*/
    public static final int MACEXPERT = 3;
    
    /**flag used to identify WIN encoding*/
    public static final int WIN = 2;
    
    /**flag used to identify STD encoding*/
    public static final int STD = 1;
    
    /**flag used to identify mac encoding*/
    public static final int MAC = 0;
    
    /**mapped onto CID0 or CID2*/
    public static final int TYPE0=1228944676;
    
    public static final int TYPE1=1228944677;
    public static final int TRUETYPE=1217103210;
    
    public static final int TYPE3=1228944679;
    public static final int CIDTYPE0=-1684566726;
    public static final int CIDTYPE2=-1684566724;
    
    public static final int OPENTYPE=6;
    
    public static final int TRUETYPE_COLLECTION=7;
    
    public static final int FONT_UNSUPPORTED=8;
    
    /**constant value for ellipsis*/
    private static final String ellipsis= String.valueOf((char) Integer.parseInt("2026", 16));
    
    /**must use windows encoding because files were edited on Windows*/
    private static final String enc = "Cp1252";
    
    /**lookup table to workout index from glyf*/
    private static Map[] glyphToChar= new HashMap[7];
    
    /**holds lookup to map char values for decoding NAME into encoded char*/
    private static String[] MAC_char_encoding_table;
    
    /**holds lookup to map char values for decoding NAME into encoded char*/
    private static String[] WIN_char_encoding_table;
    
    /**holds lookup to map char values for decoding NAME into encoded char*/
    private static String[] STD_char_encoding_table;
    
    /**holds lookup to map char values for decoding NAME into encoded char*/
    private static String[] PDF_char_encoding_table;
    
    /**holds lookup to map char values for decoding NAME into encoded char*/
    private static String[] ZAPF_char_encoding_table;
    
    /**holds lookup to map char values for decoding NAME into encoded char*/
    private static String[] SYMBOL_char_encoding_table;
    
    /**holds lookup to map char values for decoding NAME into encoded char*/
    private static String[] MACEXPERT_char_encoding_table;
    
    /**holds lookup to map unicode values onto their glyph name*/
    private static final Map<String,String> unicodeToName = new HashMap<String, String>();
    
    
    /**loader to load data from jar*/
    private static ClassLoader loader = StandardFonts.class.getClassLoader();
    
    /**list of standard fonts*/
    private static Map standardFileList=new HashMap();
    
    /**flag if standard font loaded*/
    private static  Map standardFontLoaded=new HashMap();
    
    /**lookup for standard fonts which we read when object created
     * (deliberately switched back to hashtable - see case 13910)
     */
    private static Map widthTableStandard = new Hashtable();
    
    /**lookup table for java fonts*/
    protected static Map javaFontList=new HashMap();
    
    /**java font versions of fonts*/
    protected static String javaFonts[] =
    {"Courier",
        "Courier-Bold",
        "Courier",
        "Courier-Bold",
        "Arial",
        "Arial-Bold",
        "Arial",
        "Arial-Italic",
        "Symbol",
        "Times New Roman",
        "Times New Roman",
        "Times New Roman",
        "Times New Roman",
        "Wingdings" };
    
    /**names of 14 local fonts used in pdf*/
    protected static String files_names[] =
    {
        "Courier",
        "Courier-Bold",
        "Courier-BoldOblique",
        "Courier-Oblique",
        "Helvetica",
        "Helvetica-Bold",
        "Helvetica-BoldOblique",
        "Helvetica-Oblique",
        "Symbol",
        "Times-Bold",
        "Times-BoldItalic",
        "Times-Italic",
        "Times-Roman",
        "ZapfDingbats" };
    
    /**alternative names of 14 local fonts used in pdf*/
    protected static String files_names_bis[] =
    {
        "CourierNew",
        "CourierNew,Bold",
        "CourierNew,BoldItalic",
        "CourierNew,Italic",
        "Arial",
        "Arial,Bold",
        "Arial,BoldItalic",
        "Arial,Italic",
        "Symbol",
        "TimesNewRoman,Bold",
        "TimesNewRoman,BoldItalic",
        "TimesNewRoman,Italic",
        "TimesNewRoman",
        "ZapfDingbats" };
    
    /**holds lookup values used by truetype font mapping*/
    private static HashMap adobeMap;
    
    //hold bounds (deliberately switched back to hashtable - see case 13910)
    private static Map fontBounds=new Hashtable();
    
    /**flag if standard_encoding loaded*/
    public static boolean usesGlyphlist;
    
    public static void dispose(){
        
        unicode_name_mapping_table =null;
        
        unicode_char_decoding_table =null;
        
        glyphToChar=null;
        
        MAC_char_encoding_table=null;
        
        WIN_char_encoding_table=null;
        
        STD_char_encoding_table=null;
        
        PDF_char_encoding_table=null;
        
        ZAPF_char_encoding_table=null;
        
        SYMBOL_char_encoding_table=null;
        
        MACEXPERT_char_encoding_table=null;
        
        
        loader = null;
        
        standardFileList=null;
        
        standardFontLoaded=null;
        
        widthTableStandard = null;
        
        /**names of CID fonts supplied by Adobe*/
        //CIDFonts =null;
        
        /**lookup table for java fonts*/
        javaFontList=null;
        
        /**java font versions of fonts*/
        javaFonts=null;
        
        files_names=null;
        
        files_names_bis=null;
        
        adobeMap=null;
        
        fontBounds=null;
    }
    
    //////////////////////////////////////////////////
    /**
     * create lookup array so we can quickly test if
     * we have one of the 14 fonts so we can test quickly
     */
    static{
        // loop to read widths from default fonts
        for (int i = 0; i < files_names.length; i++) {
            standardFileList.put(files_names_bis[i], i);
            standardFileList.put(files_names[i], i);
        }
        
        loadAdobeMap();
    }
    
    /**return font type based on file ending or FONT_UNSUPPORTED
     * if not recognised
     * @param name
     * @return int - defined in StandardFonts (no value is FONT_UNSUPPORTED)
     */
    public static int getFontType(final String name) {
        
        int type= FONT_UNSUPPORTED;
        
        if (name.endsWith(".ttf")) {
            type=TRUETYPE;
        } else if(name.endsWith(".otf")) {
            type=OPENTYPE;
        } else if(name.endsWith(".ttc")) {
            type=TRUETYPE_COLLECTION;
            //else if(name.endsWith(".afm"))
            //  type=TYPE1;
        } else if(name.endsWith(".pfb")) {
            type=TYPE1;
        }
        
        return type;
    }
    
    /**
     * get FontBonds set in afm file
     */
    public static float[] getFontBounds(final String fontName){
        return (float[]) fontBounds.get(fontName);
    }
    
    
    public static String getUnicodeName(final String key){
        return (String) unicode_name_mapping_table.get(key);
    }
    
    public static String getUnicodeChar(final int i, final int key){
        
        return unicode_char_decoding_table[i][key];
    }
    
    public static Float getStandardWidth(String font, final String key){
        
        font=font.toLowerCase();
        
        Object value=widthTableStandard.get(font+key);
        if(value==null){
            String altfont=font;
            final int p=altfont.indexOf(',');
            if(p!=-1){
                altfont=altfont.substring(0,p);
                value=widthTableStandard.get(altfont+key);
            }
        }
        
        return (Float) value;
    }
    
    //////////////////////////////////////////////////////////////////////////
    /**
     * create mapping tables for pdf values
     * for Zapf and Symbol (not fully implmented yet)
     */
    private static void readStandardMappingTable(
            final int key,
            final String file_name){
        String char_value, NAME, VAL, line,hexVal;
        int value;
        BufferedReader input_stream = null;
        
        glyphToChar[key]=new HashMap();
        
        
        try {
            
            
            input_stream =
                    (file_name.equals("symbol.cfg"))
                    ? new BufferedReader(
                            new InputStreamReader(
                                    loader.getResourceAsStream(
                                            "org/jpedal/res/pdf/" + file_name),
                                    enc))
                    : new BufferedReader(
                            new InputStreamReader(
                                    loader.getResourceAsStream(
                                            "org/jpedal/res/pdf/" + file_name),
                                    "UTF-16"));
            
            // trap problems
            if (input_stream == null && LogWriter.isOutput()) {
                LogWriter.writeLog("Unable to open "+ file_name+ " to read standard encoding");
            }
            
            
            //read in lines and place in map tables for fast lookup
            while (true) {
                line = input_stream.readLine();
                if (line == null) {
                    break;
                }
                
                //write values to table, converting from Octal
                final StringTokenizer values = new StringTokenizer(line);
                
                //trap for space and lines which cause problems in Zapf
                if ((!line.contains("space")) && (values.countTokens() > 1)) {
                    
                    //ignore first as token but read as char
                    if (values.countTokens() == 3) {
                        char_value = values.nextToken();
                        NAME = values.nextToken();
                        VAL = values.nextToken();
                        
                    }else if (values.countTokens() == 4) {
                        hexVal=values.nextToken();
                        
                        values.nextToken(); //ignore in this case
                        NAME = values.nextToken();
                        VAL = values.nextToken();
                        
                        char_value=Character.toString((char)Integer.parseInt(hexVal,16));
                        
                    } else { //zapf values
                        if(values.countTokens()==2){
                            char_value = " ";
                            NAME = values.nextToken();
                            VAL = values.nextToken();
                        }else{
                            char_value = values.nextToken();
                            NAME = values.nextToken();
                            VAL = values.nextToken();
                        }
                    }
                    
                    unicode_name_mapping_table.put(key + NAME, char_value);
                    
                    glyphToChar[key].put(NAME, Integer.parseInt(VAL));
                    
                    //20021104 added to make sure names in list as well
                    //if (file_name.equals("zapf.cfg"))
                    unicode_name_mapping_table.put(NAME, char_value);
                    
                    //convert if there is a value
                    if (Character.isDigit(VAL.charAt(0))) {
                        
                        value = Integer.parseInt(VAL, 8);
                        
                        if(key==ZAPF) {
                            ZAPF_char_encoding_table[value] = char_value;
                        } else if(key==SYMBOL) {
                            SYMBOL_char_encoding_table[value] = char_value;
                        } else if(key==MACEXPERT) {
                            MACEXPERT_char_encoding_table[value] = char_value;
                        }
                        
                        unicode_char_decoding_table[key][value]=NAME;
                        
                    }
                }
            }
        } catch (final Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " reading lookup table for pdf");
            }
        }
        
        if(input_stream!=null){
            try{
                input_stream.close();
            }catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " reading lookup table for pdf  for abobe map");
                }
            }
        }
    }
    //////////////////////////////////////////////
    /**
     * create mapping tables for 'standard' pdf values
     * by reading in data from files (which are tables
     * taken from Adobe's standard documentation
     */
    private static void readStandardMappingTable(final int idx) {
        
        String char_value, NAME, STD_value, MAC_value, WIN_value,PDF_value,raw;
        int mac_value, win_value, std_value;
        String line;
        BufferedReader input_stream = null;
        
        //needed for comparison table
        if(idx==MAC) {
            checkLoaded(WIN);
        }
        
        
        try {
            
            //initialise inverse lookup
            glyphToChar[idx]=new HashMap();
            
            input_stream =
                    new BufferedReader(
                            new InputStreamReader(
                                    loader.getResourceAsStream(
                                            "org/jpedal/res/pdf/standard_encoding.cfg"),enc));
            
            usesGlyphlist=true;
            
            // trap problems
            if (input_stream == null && LogWriter.isOutput()) {
                LogWriter.writeLog("Unable to open standard_encoding.cfg from jar");
            }
            
            //read in lines and place in map tables for fast lookup
            while (true) {
                line = input_stream.readLine();
                if (line == null) {
                    break;
                }
                
                //write values to table, converting from Octal
                final StringTokenizer values = new StringTokenizer(line);
                final int count=values.countTokens();
                
                //format is NAME, STD,MAC,WIN,PDF, unicode value (as hex) char from PDF reference ignored) or
                //NAME, STD,MAC,WIN,PDF,  char (used for fi,fl and other double values)
                //ignore first as token but read as char
                
                NAME = values.nextToken();
                STD_value = values.nextToken();
                MAC_value = values.nextToken();
                WIN_value = values.nextToken();
                PDF_value =values.nextToken();
                raw=values.nextToken();
                
                if(count==7) {
                    char_value=Character.toString((char)Integer.parseInt(raw,16));
                } else {
                    char_value=raw;
                }
                
                unicodeToName.put(char_value, NAME);
                
                
                //convert if possible
                if((idx==MAC) &&(Character.isDigit(MAC_value.charAt(0)))) {
                    mac_value = Integer.parseInt(MAC_value, 8);
                    
                    //substitute ellipsis
                    if(mac_value==201) {
                        char_value=ellipsis;
                    }
                    
                    MAC_char_encoding_table[mac_value] =char_value;
                    unicode_char_decoding_table[MAC][mac_value]=NAME;
                    
                    glyphToChar[MAC].put(NAME, mac_value);
                    
                    //build a comparison table to test encoding
                    //if (Character.isDigit(WIN_value.charAt(0)))
                    //	win_value = Integer.parseInt(WIN_value, 8);
                    
                }else if ((idx==STD)&&(Character.isDigit(STD_value.charAt(0)))) {
                    std_value = Integer.parseInt(STD_value, 8);
                    
                    //substitute ellipsis
                    if(std_value==188) {
                        char_value=ellipsis;
                    }
                    
                    STD_char_encoding_table[std_value] =char_value;
                    unicode_char_decoding_table[STD][std_value]=NAME;
                    
                    glyphToChar[STD].put(NAME, std_value);
                    
                }else if ((idx==PDF)&&(Character.isDigit(PDF_value.charAt(0)))) {
                    std_value = Integer.parseInt(PDF_value, 8);
                    
                    //substitute ellipsis
                    if(std_value==131) {
                        char_value=ellipsis;
                    }
                    
                    PDF_char_encoding_table[std_value] =char_value;
                    unicode_char_decoding_table[PDF][std_value]=NAME;
                    
                }else if (idx== WIN && Character.isDigit(WIN_value.charAt(0))) {
                    win_value = Integer.parseInt(WIN_value, 8);
                    
                    //substitute ellipsis
                    if(win_value==133) {
                        char_value=ellipsis;
                    }
                    
                    WIN_char_encoding_table[win_value] =char_value;
                    unicode_char_decoding_table[WIN][win_value]=NAME;
                    
                    glyphToChar[WIN].put(NAME, win_value);
                    
                    
                }
                
                //save details for later
                unicode_name_mapping_table.put(NAME, char_value);
                
                
            }
            
            //add in alternative MAC space   312 octal == space
            if(idx==MAC) {
                MAC_char_encoding_table[202]=" ";
            }
            
            //add in alternative WIN values
            if(idx== WIN){
                WIN_char_encoding_table[160]=" ";
                WIN_char_encoding_table[255]="-";
                
                unicode_char_decoding_table[WIN][160]="space";
                
            }
            
        } catch (final Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " reading lookup table for pdf  for "+idx);
            }
        }
        
        if(input_stream!=null){
            try{
                input_stream.close();
            }catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " reading lookup table for pdf  for abobe map");
                }
            }
        }
    }
    
    /**used internally when we needed to convert bytes to MacROman to build new tables*/
    /**private static String byteToEncodedString(String value,String enc) throws Exception{
    
     String s=null;

     //Create the encoder and decoder for ISO-8859-1
     Charset charset = Charset.forName(enc);
     CharsetDecoder decoder = charset.newDecoder();
     CharsetEncoder encoder = charset.newEncoder();

     // Convert a string to ISO-LATIN-1 bytes in a ByteBuffer
     // The new ByteBuffer is ready to be read.
     java.nio.ByteBuffer bbuf = encoder.encode(CharBuffer.wrap("a"));
     bbuf.clear();
     //java.nio.ByteBuffer bbuf=new ByteBuffer();
     bbuf.put(0,(byte) Integer.parseInt(value,16));
     // Convert ISO-LATIN-1 bytes in a ByteBuffer to a character ByteBuffer and then to a string.
     // The new ByteBuffer is ready to be read.
     CharBuffer cbuf = decoder.decode(bbuf);
     s = cbuf.toString();

     System.out.println(Integer.toHexString((int)s.charAt(0)));

     return s;
     }*/

    /**
     * Returns the appropriate glyph name for a supplied unicode value.
     * @param unicode The character in unicode
     * @return The glyph name
     */
    public static String getNameFromUnicode(final String unicode) {
        return unicodeToName.get(unicode);
    }
    
    public static String  getEncodedChar(final int font_encoding, final int char_int){
        
        String return_character=null;
        
        if(font_encoding== WIN) {
            return_character=WIN_char_encoding_table[char_int];
        } else if(font_encoding==STD) {
            return_character=STD_char_encoding_table[char_int];
        } else if(font_encoding==MAC) {
            return_character=MAC_char_encoding_table[char_int];
        } else if(font_encoding==PDF) {
            return_character=PDF_char_encoding_table[char_int];
        } else if(font_encoding==ZAPF) {
            return_character=ZAPF_char_encoding_table[char_int];
        } else if(font_encoding==SYMBOL) {
            return_character=SYMBOL_char_encoding_table[char_int];
        } else if(font_encoding==MACEXPERT) {
            return_character=MACEXPERT_char_encoding_table[char_int];
        }
        
        if (return_character== null) {
            return_character = "&#" + char_int + ';';
        }
        
        return return_character;
    }
    
    /**load required mappings*/
    public  static void checkLoaded( final int enc) {
        
        
        /**load mapping if we need it and initialise storage*/
        if((enc==MAC)&&(MAC_char_encoding_table==null)){
            
            MAC_char_encoding_table = new String[335];
            readStandardMappingTable(enc);
            
        }else if((enc== WIN)&&(WIN_char_encoding_table==null)){
            
            WIN_char_encoding_table = new String[335];
            readStandardMappingTable(enc);
            
        }else if((enc==STD)&&(STD_char_encoding_table==null)){
            
            STD_char_encoding_table = new String[335];
            readStandardMappingTable(enc);
            
        }else if((enc==PDF)&&(PDF_char_encoding_table==null)){
            
            PDF_char_encoding_table = new String[335];
            readStandardMappingTable(enc);
            
        }else if((enc==SYMBOL)&&(SYMBOL_char_encoding_table==null)){
            
            SYMBOL_char_encoding_table = new String[335];
            readStandardMappingTable(SYMBOL, "symbol.cfg");
            
        }else if((enc==ZAPF)&&(ZAPF_char_encoding_table==null)){
            
            ZAPF_char_encoding_table = new String[335];
            readStandardMappingTable(ZAPF, "zapf.cfg");
            
        }else if((enc==MACEXPERT)&&(MACEXPERT_char_encoding_table==null)){
            
            MACEXPERT_char_encoding_table = new String[335];
            readStandardMappingTable(MACEXPERT, "mac_expert.cfg");
            
        }
    }
    
    /////////////////////////////////////////////////////////////////////////
    /**
     * read default widths for 14 standard fonts supplied
     * by Adobe
     */
    static final synchronized void loadStandardFont(final int i) throws IOException{
        String line, next_command, char_name = "";
        final BufferedReader input_stream;
        float width = 200;
        //int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        //int b_x1 = 0, b_y1 = 0, b_x2 = 0, b_y2 = 0;
        
        //allow for 2 calls on thread with second call stalling this so
        //we should exit
        if(standardFontLoaded.get(i)!=null) {
            return;
        }
        
        // loop to read widths from default fonts
        {
        //open the file
        input_stream =
                new BufferedReader(
                        new InputStreamReader(
                                loader.getResourceAsStream(
                                        "org/jpedal/res/pdf/defaults/"
                                                + files_names[i]
                                                + ".afm"),
                                enc));
        
        boolean char_mapping_table = false;
        //flag if in correct part of file
        while (true) { //read the lines and extract width info
            line = input_stream.readLine();
            
            if (line == null) {
                break;
            }
            if (line.startsWith("EndCharMetrics")) {
                char_mapping_table = false;
            }
            
            //extract bounding box
            if (line.startsWith("FontBBox")) {
                
                final float[] fontBBox=new float[4];
                final StringTokenizer values = new StringTokenizer(line);
                //drop FontBBox
                values.nextToken();
                
                for(int a=0;a<4;a++) {
                    fontBBox[a] = Integer.parseInt(values.nextToken());
                }
                
                fontBounds.put(files_names[i],fontBBox);
                
                
            }
            
            if (char_mapping_table) { //extract info from the line
                final StringTokenizer values = new StringTokenizer(line, " ;");
                
                //extract values
                while (values.hasMoreTokens()) {
                    next_command = values.nextToken();
                    /**
                         if (next_command.equals("C"))
                         char_number = values.nextToken();*/
                    if (next_command.equals("WX")) {
                        width = Float.parseFloat(values.nextToken()) / 1000;
                    } else if (next_command.equals("N")) {
                        char_name = values.nextToken();
                        /**
                         if (next_command.equals("B")) {
                         x1 = Integer.parseInt(values.nextToken());
                         y1 = Integer.parseInt(values.nextToken());
                         x2 = Integer.parseInt(values.nextToken());
                         y2 = Integer.parseInt(values.nextToken());
                         }*/
                    }
                }
                
                //store width
                widthTableStandard.put(files_names_bis[i].toLowerCase() + char_name, width);
                widthTableStandard.put(files_names[i].toLowerCase()  + char_name, width);
                
            }
            if (line.startsWith("StartCharMetrics")) {
                char_mapping_table = true;
            }
        }
    }
        
        if(input_stream!=null){
            try{
                input_stream.close();
            }catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " reading lookup table for pdf  for abobe map");
                }
            }
        }
        
        //flag value is set
        standardFontLoaded.put(i,"x");
    }
    
    /**
     * check if one of 14 standard fonts and if so load widths
     */
    protected static void loadStandardFontWidth(final String fontName){
        
        //get name of font if standard
        final Integer fileNumber=(Integer) standardFileList.get(fontName);
        
        
        if( fileNumber!=null && standardFontLoaded.get(fileNumber)==null){
            
            try{
                loadStandardFont(fileNumber);
                
            } catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("[PDF] " + e + " problem reading lookup table for pdf font "+fontName+ ' ' +fontName);
                }
            }
        }
    }
    
    /**
     * converts glyph into character index
     */
    public static int lookupCharacterIndex(final String glyph, final int idx) {
        
        final Object value=glyphToChar[idx].get(glyph);
        if(value==null) {
            return 0;
        } else {
            return (Integer) value;
        }
    }
    
    /**
     * load the adobe unicode mapping table for truetype fonts
     */
    private static void loadAdobeMap() {
        
        BufferedReader input_stream =null;
        
        /**load if not already loaded*/
        if(adobeMap==null){
            try {
                //initialise
                adobeMap=new HashMap();
                
                input_stream =new BufferedReader(
                        new InputStreamReader(
                                loader.getResourceAsStream(
                                        "org/jpedal/res/pdf/glyphlist.cfg"),
                                enc));
                
                // trap problems
                if (input_stream == null && LogWriter.isOutput()) {
                    LogWriter.writeLog("Unable to open glyphlist.cfg from jar");
                }
                
                //read in lines and place in map tables for fast lookup
                while (true) {
                    final String line = input_stream.readLine();
                    if (line == null) {
                        break;
                    }
                    
                    if((!line.startsWith("#"))&&(line.indexOf(';')!=-1)){
                        
                        final StringTokenizer vals=new StringTokenizer(line,";");
                        final String key=vals.nextToken();
                        String operand=vals.nextToken();
                        final int space=operand.indexOf(' ');
                        if(space!=-1) {
                            operand=operand.substring(0,space);
                        }
                        final int opVal=Integer.parseInt(operand,16);
                        adobeMap.put(key, opVal);
                        
                        unicode_name_mapping_table.put(key,Character.toString((char)opVal));
                        
                    }
                }
            } catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " reading lookup table for pdf  for abobe map");
                }
                e.printStackTrace(System.out);
            }
        }
        
        if(input_stream!=null){
            try{
                input_stream.close();
            }catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " reading lookup table for pdf  for abobe map");
                }
            }
        }
        
    }
    /**
     * @return Returns the adobe mapping for truetype case 3,1
     */
    public static int getAdobeMap(final String key){
        
        final Object value=adobeMap.get(key);
        if(value==null) {
            return -1;
        } else {
            return (Integer) value;
        }
        
    }
    
    /**
     * @return Returns a boolean if in Adobe map
     */
    public static boolean isValidGlyphName(final String key){
        
        if(key==null) {
            return false;
        } else {
            return(adobeMap.get(key)!=null);
        }
        
    }
    
    
    /**
     * see if a standard font (ie Arial, Helvetica)
     */
    public static boolean isStandardFont(final String fontName, final boolean includeWeights) {
        
        boolean isStandard=(standardFileList.get(fontName)!=null);
        
        if(!isStandard && includeWeights){
            
            final int ptr=fontName.indexOf('-');
            if(ptr!=-1){
                final String rawName=fontName.substring(0,ptr);
                //System.out.println(ptr+"<>"+fontName+"<>"+rawName+"<>"+standardFileList.get(fontName)!=null);
                isStandard=(standardFileList.get(rawName)!=null);
            }
            
        }
        return isStandard;
    }
    
    /**
     * open font , read postscript, and return Map with font details
     */
    public static Map getFontDetails(final int type, final String subFont) {
        
        final Map fontDetails=new HashMap();
        
        /**read in font data*/
        if(type==TRUETYPE || type==TRUETYPE_COLLECTION){
            
            //FontData closed in routine
            TTGlyphs.addStringValues(new FontData(subFont), fontDetails);
            
        }
        
        return fontDetails;
    }
    
    /**
     * open font , read postscript, family or full names and return array
     */
    public static String[] readNamesFromFont(final int type, final String subFont, final int mode) throws Exception {
        
        String[] fontNames=new String[1];
        fontNames[0]="";
        
        
        /**read in font data*/
        if(type==TRUETYPE || type==TRUETYPE_COLLECTION){
            
            //FontData closed in routine
            fontNames= TTGlyphs.readFontNames(new FontData(subFont), mode);
            
        }else if(type==TYPE1){
            
            //FontData closed in routine
            fontNames= T1Glyphs.readFontNames(new FontData(subFont));
            
        }
        
        return fontNames;
    }
    
    //allow for number value as well as glyph name (ie 68 rather than D)
    public static String convertNumberToGlyph(String mappedChar, final boolean containsHexNumbers, final boolean allowSingleValue) {
        
        int charCount=mappedChar.length();
        
        boolean isNumber=true; //assume true and disprove
        
        //System.out.println(mappedChar+" "+containsHexNumbers);
        
        /**/
        boolean hasHex=false;
        if(charCount==2 || charCount==3 || (allowSingleValue && charCount==1)){
            for(int ii=0;ii<charCount;ii++){ //test all values to see if number
                final char c=mappedChar.charAt(ii);
                if(c>='0' && c<='9'){
                }else if(containsHexNumbers && c>='A' && c<='F'){
                    hasHex=true;
                }else{  //fail on first and exit loop
                    isNumber=false;
                    ii=charCount;
                }
            }
            
            if(isNumber){
                
                if(charCount==3 || !containsHexNumbers){
                    if(!hasHex) { //stop spurious match (ie DC1)
                        mappedChar= String.valueOf((char) Integer.parseInt(mappedChar));
                    }
                }else{
                    mappedChar= String.valueOf((char) Integer.parseInt(mappedChar,16));
                }
            }
        }else{ //check any hash values to fix odd bug in file where #2323 truncates to #23
            boolean hasHash=false,hasText=false;
            
            for(int ii=0;ii<charCount;ii++){ //test all values to see if number
                final char c=mappedChar.charAt(ii);
                if(c=='#'){
                    hasHash=true;
                    //ii=charCount;
                }
                if(c>='A' && c<='Z') {
                    hasText=true;
                }
            }
            
            if(hasHash){
                
                final StringBuilder buf=new StringBuilder(mappedChar);
                
                //if text as well replicate what seems to happen in Acrobat and remove number value
                if(hasText){
                    
                    try{
                        //System.out.println("before="+buf.toString());
                        
                        for(int ii=0;ii<charCount;ii++){ //delete any excess values
                            final char c=buf.charAt(ii);
                            if((c=='#') && (ii<charCount)){
                                    for(int aa=0;aa<2;aa++){
                                        buf.deleteCharAt(ii+1);
                                        //System.out.println("del"+buf.);
                                        charCount--;
                                    }
                                    
                                    
                                    //if(ii>=charCount)
                                    //     break;
                                }
                            }
 
                    }catch(final Exception e){
                        //tell user and log
                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog("Exception: "+e.getMessage());
                        }
                        //
                    }
                }else{
                    for(int ii=0;ii<charCount;ii++){ //delete any excess values
                        final char c=buf.charAt(ii);
                        if(c=='#'){
                            ii += 3;
                            if(ii<charCount){
                                char c2=buf.charAt(ii);
                                while(c2>='0' && c2<='9'){
                                    buf.deleteCharAt(ii);
                                    charCount--;
                                    
                                    if(ii>=charCount) {
                                        break;
                                    }
                                    
                                    c2=buf.charAt(ii);
                                }
                            }
                            ii--;
                        }
                    }
                }
                
                mappedChar=buf.toString();
                
            }
        }
        
        return mappedChar;
    }
    
    /**
     * turn hashed key value into String
     */
    public static String getFontypeAsString(final int fontType) {
        switch(fontType){
            
            case TRUETYPE:
                return "TrueType";
            case TYPE1:
                return "Type1";
            case TYPE3:
                return "Type3";
                
            case CIDTYPE0:
                return "CIDFontType0";
            case CIDTYPE2:
                return "CIDFontType2";
                
            default:
                return "Unknown";
        }
    }
    
    
    private static final HashMap<String,HashMap<Integer,Integer>> mappedCharacters = new HashMap<String, HashMap<Integer,Integer>>();
    private static final HashMap<String,ArrayList<Integer>> takenChars = new HashMap<String, ArrayList<Integer>>();
    private static final int MAX_CHAR_CODE = 0xD800;
    
    private static void blockForbiddenRanges(final ArrayList<Integer> ids) {
        int i;
        
        for (i=0x0; i<=0x1f; i++) {
            ids.add(i);
        }
        
        for (i=0x7f; i<=0xa0; i++) {
            ids.add(i);
        }
        
        for (i=0x200c; i<=0x200f; i++) {
            ids.add(i);
        }
        
    }
    /**
     * Find an appropriate value to use for identity CID fonts as unicode values are unrelated to actual values and many
     * potential values are blocked by browsers
     * @param fontName Name of the font to use
     * @param cid The cid value to map from
     * @return The value mapped onto
     */
    public static int mapCIDToValidUnicode(final String fontName, final int cid) {
        
        HashMap<Integer,Integer> cidMap = mappedCharacters.get(fontName);
        ArrayList<Integer> taken = takenChars.get(fontName);
        
        //Create structures for this font if they don't exist yet
        if (cidMap == null) {
            cidMap = new HashMap<Integer, Integer>();
            mappedCharacters.put(fontName, cidMap);
            
            taken = new ArrayList<Integer>();
            takenChars.put(fontName, taken);
            
            blockForbiddenRanges(taken);
        }
        
        //Fetch mapping and return if present
        final Integer result = cidMap.get(cid);
        if (result != null) {
            return result;
        }
        
        //First try existing CID
        int newCid = cid;
        
        //If under 0x20 shift by 0x20 to try to get reasonable extraction
        if (cid < 0x20) {
            newCid += 0x20;
        }
        
        //Otherwise use first available CID
        while (taken.contains(newCid) && taken.size() < MAX_CHAR_CODE) {
            newCid = (newCid+1)%MAX_CHAR_CODE;
        }
        
        //Store for later lookup
        cidMap.put(cid,newCid);
        taken.add(newCid);
        
        return newCid;
    }
    
    
    public static int getIDForGlyphName(final String fontName, String glyphName) {
        boolean base10=false;
        final int uc = StandardFonts.getAdobeMap(glyphName);
        if (uc >= 0) {
            return uc;
        } else if (glyphName.startsWith("uni")){
            glyphName = glyphName.substring(3);
        } else if (glyphName.charAt(0) == 'u' || glyphName.charAt(0) == 'G'){
            glyphName = glyphName.substring(1);
        } else {
            base10 = true;
        }
        
        try {
            int num;
            if (base10) {
                num = Integer.parseInt(glyphName, 10);
                num = mapCIDToValidUnicode(fontName, num);
            } else {
                num = Integer.parseInt(glyphName, 16);
            }
            return num;
        } catch (final NumberFormatException e) {

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in handling cid id "+e);
            }
            return -1;
        }
    }
    
    /**
     * convert common abbreviations of standard fonts names
     */
    public static String expandName(String name) {
        
        final String testName=name.toLowerCase();
        
        if(testName.equals("cour")) {
            name="Courier";
        } else if(testName.equals("helv")) {
            name="Helvetica";
        } else if(testName.equals("hebo")) {
            name="Helvetica-BOLD";
        } else if(testName.equals("zadb")) {
            name="ZapfDingbats";
        } else if(testName.equals("tiro")){
            name="Times";
        }
        
        return name;
    }
    
    public static String[] CMAP;
    
    public static void readCMAP() {
        
        //initialise first time and use as flag to show read
        CMAP=new String[65536];
        
        String line;
        int rawVal,unicodeVal;
        final BufferedReader input_stream;
        
        try {
            
            input_stream =new BufferedReader(
                    new InputStreamReader(loader.getResourceAsStream("org/jpedal/res/pdf/jis.cfg")));
            
            
            //read in lines and place in map tables for fast lookup
            while (true) {
                line = input_stream.readLine();
                if (line == null) {
                    break;
                }
                
                if(line.startsWith("0") && line.contains("#")){
                    
                    
                    //write values to table, converting from Octal
                    final StringTokenizer values = new StringTokenizer(line);
                    //System.out.println(line);
                    final String xx=values.nextToken().substring(2);
                    
                    rawVal=Integer.parseInt(xx,16);//raw hex
                    //values.nextToken();//ignore
                    unicodeVal=Integer.parseInt(values.nextToken().substring(2),16);//unicode
                    CMAP[rawVal]=String.valueOf((char)unicodeVal);
                }
            }
            
            input_stream.close();
            
        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: "+e.getMessage());
            }
            //
        }
    }
    
}
