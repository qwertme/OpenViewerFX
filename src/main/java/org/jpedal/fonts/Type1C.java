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
 * Type1C.java
 * ---------------
 */
package org.jpedal.fonts;

import java.io.*;
import java.util.Map;
import java.awt.*;

import org.jpedal.fonts.glyph.T1Glyphs;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.glyph.objects.T1GlyphNumber;
import org.jpedal.fonts.objects.FontData;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.ObjectStore;

import org.jpedal.utils.LogWriter;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.utils.repositories.FastByteArrayOutputStream;


/**
 * handlestype1 specifics
 */
public class Type1C extends Type1{
    
    static final boolean  debugFont=false;
    
    static final boolean debugDictionary=false;
    
    int ros=-1,CIDFontVersion,CIDFontRevision,CIDFontType,CIDcount,UIDBase=-1,FDArray=-1,FDSelect=-1;
    //int [] rosArr;

    /**
    static final String[] OneByteCCFDict={"version","Notice","FullName","FamilyName","Weight",
        "FontBBox","BlueValues","OtherBlues","FamilyBlues","FamilyOtherBlues",
        "StdHW","StdVW","Escape","UniqueID","XUID",
        "charset","Encoding","CharStrings","Private", "Subrs",
        "defaultWidthX","nominalWidthX","-reserved-","-reserved-","-reserved-",
        "-reserved-","-reserved-","-reserved-","shortint","longint",
        "BCD","-reserved-"};
    
    static final String[] TwoByteCCFDict={"Copyright","isFixedPitch","ItalicAngle","UnderlinePosition","UnderlineThickness",
        "PaintType","CharstringType","FontMatrix","StrokeWidth","BlueScale",
        "BlueShift","BlueFuzz","StemSnapH","StemSnapV","ForceBold",
        "-reserved-","-reserved-","LanguageGroup","ExpansionFactor","initialRandomSeed",
        "SyntheticBase","PostScript","BaseFontName","BaseFontBlend","-reserved-",
        "-reserved-","-reserved-","-reserved-","-reserved-","-reserved-",
        "ROS","CIDFontVersion","CIDFontRevision","CIDFontType","CIDCount",
        "UIDBase","FDArray","FDSelect","FontName"};

     /**/
    //current location in file
    private int top;
    
    private int charset;
    
    private int enc;
    
    private int charstrings;
    
    private int stringIdx;
    
    private int stringStart;
    
    private int stringOffSize;
    
    private Rectangle BBox;
    
    
    private boolean hasFontMatrix;// hasFontBBox=false,;
    
    private int[] privateDictOffset = {-1}, privateDictLength ={-1};
    private int currentFD=-1;
    
    private int[] defaultWidthX = {0}, nominalWidthX = {0}, fdSelect;
    
    /** decoding table for Expert */
    private static final int ExpertSubCharset[] = { // 87
        // elements
        0,
        1,
        231,
        232,
        235,
        236,
        237,
        238,
        13,
        14,
        15,
        99,
        239,
        240,
        241,
        242,
        243,
        244,
        245,
        246,
        247,
        248,
        27,
        28,
        249,
        250,
        251,
        253,
        254,
        255,
        256,
        257,
        258,
        259,
        260,
        261,
        262,
        263,
        264,
        265,
        266,
        109,
        110,
        267,
        268,
        269,
        270,
        272,
        300,
        301,
        302,
        305,
        314,
        315,
        158,
        155,
        163,
        320,
        321,
        322,
        323,
        324,
        325,
        326,
        150,
        164,
        169,
        327,
        328,
        329,
        330,
        331,
        332,
        333,
        334,
        335,
        336,
        337,
        338,
        339,
        340,
        341,
        342,
        343,
        344,
        345,
        346 };
    
    
    /** lookup table for names for type 1C glyphs */
    public static final String type1CStdStrings[] = { // 391
        // elements
        ".notdef",
        "space",
        "exclam",
        "quotedbl",
        "numbersign",
        "dollar",
        "percent",
        "ampersand",
        "quoteright",
        "parenleft",
        "parenright",
        "asterisk",
        "plus",
        "comma",
        "hyphen",
        "period",
        "slash",
        "zero",
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "colon",
        "semicolon",
        "less",
        "equal",
        "greater",
        "question",
        "at",
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z",
        "bracketleft",
        "backslash",
        "bracketright",
        "asciicircum",
        "underscore",
        "quoteleft",
        "a",
        "b",
        "c",
        "d",
        "e",
        "f",
        "g",
        "h",
        "i",
        "j",
        "k",
        "l",
        "m",
        "n",
        "o",
        "p",
        "q",
        "r",
        "s",
        "t",
        "u",
        "v",
        "w",
        "x",
        "y",
        "z",
        "braceleft",
        "bar",
        "braceright",
        "asciitilde",
        "exclamdown",
        "cent",
        "sterling",
        "fraction",
        "yen",
        "florin",
        "section",
        "currency",
        "quotesingle",
        "quotedblleft",
        "guillemotleft",
        "guilsinglleft",
        "guilsinglright",
        "fi",
        "fl",
        "endash",
        "dagger",
        "daggerdbl",
        "periodcentered",
        "paragraph",
        "bullet",
        "quotesinglbase",
        "quotedblbase",
        "quotedblright",
        "guillemotright",
        "ellipsis",
        "perthousand",
        "questiondown",
        "grave",
        "acute",
        "circumflex",
        "tilde",
        "macron",
        "breve",
        "dotaccent",
        "dieresis",
        "ring",
        "cedilla",
        "hungarumlaut",
        "ogonek",
        "caron",
        "emdash",
        "AE",
        "ordfeminine",
        "Lslash",
        "Oslash",
        "OE",
        "ordmasculine",
        "ae",
        "dotlessi",
        "lslash",
        "oslash",
        "oe",
        "germandbls",
        "onesuperior",
        "logicalnot",
        "mu",
        "trademark",
        "Eth",
        "onehalf",
        "plusminus",
        "Thorn",
        "onequarter",
        "divide",
        "brokenbar",
        "degree",
        "thorn",
        "threequarters",
        "twosuperior",
        "registered",
        "minus",
        "eth",
        "multiply",
        "threesuperior",
        "copyright",
        "Aacute",
        "Acircumflex",
        "Adieresis",
        "Agrave",
        "Aring",
        "Atilde",
        "Ccedilla",
        "Eacute",
        "Ecircumflex",
        "Edieresis",
        "Egrave",
        "Iacute",
        "Icircumflex",
        "Idieresis",
        "Igrave",
        "Ntilde",
        "Oacute",
        "Ocircumflex",
        "Odieresis",
        "Ograve",
        "Otilde",
        "Scaron",
        "Uacute",
        "Ucircumflex",
        "Udieresis",
        "Ugrave",
        "Yacute",
        "Ydieresis",
        "Zcaron",
        "aacute",
        "acircumflex",
        "adieresis",
        "agrave",
        "aring",
        "atilde",
        "ccedilla",
        "eacute",
        "ecircumflex",
        "edieresis",
        "egrave",
        "iacute",
        "icircumflex",
        "idieresis",
        "igrave",
        "ntilde",
        "oacute",
        "ocircumflex",
        "odieresis",
        "ograve",
        "otilde",
        "scaron",
        "uacute",
        "ucircumflex",
        "udieresis",
        "ugrave",
        "yacute",
        "ydieresis",
        "zcaron",
        "exclamsmall",
        "Hungarumlautsmall",
        "dollaroldstyle",
        "dollarsuperior",
        "ampersandsmall",
        "Acutesmall",
        "parenleftsuperior",
        "parenrightsuperior",
        "twodotenleader",
        "onedotenleader",
        "zerooldstyle",
        "oneoldstyle",
        "twooldstyle",
        "threeoldstyle",
        "fouroldstyle",
        "fiveoldstyle",
        "sixoldstyle",
        "sevenoldstyle",
        "eightoldstyle",
        "nineoldstyle",
        "commasuperior",
        "threequartersemdash",
        "periodsuperior",
        "questionsmall",
        "asuperior",
        "bsuperior",
        "centsuperior",
        "dsuperior",
        "esuperior",
        "isuperior",
        "lsuperior",
        "msuperior",
        "nsuperior",
        "osuperior",
        "rsuperior",
        "ssuperior",
        "tsuperior",
        "ff",
        "ffi",
        "ffl",
        "parenleftinferior",
        "parenrightinferior",
        "Circumflexsmall",
        "hyphensuperior",
        "Gravesmall",
        "Asmall",
        "Bsmall",
        "Csmall",
        "Dsmall",
        "Esmall",
        "Fsmall",
        "Gsmall",
        "Hsmall",
        "Ismall",
        "Jsmall",
        "Ksmall",
        "Lsmall",
        "Msmall",
        "Nsmall",
        "Osmall",
        "Psmall",
        "Qsmall",
        "Rsmall",
        "Ssmall",
        "Tsmall",
        "Usmall",
        "Vsmall",
        "Wsmall",
        "Xsmall",
        "Ysmall",
        "Zsmall",
        "colonmonetary",
        "onefitted",
        "rupiah",
        "Tildesmall",
        "exclamdownsmall",
        "centoldstyle",
        "Lslashsmall",
        "Scaronsmall",
        "Zcaronsmall",
        "Dieresissmall",
        "Brevesmall",
        "Caronsmall",
        "Dotaccentsmall",
        "Macronsmall",
        "figuredash",
        "hypheninferior",
        "Ogoneksmall",
        "Ringsmall",
        "Cedillasmall",
        "questiondownsmall",
        "oneeighth",
        "threeeighths",
        "fiveeighths",
        "seveneighths",
        "onethird",
        "twothirds",
        "zerosuperior",
        "foursuperior",
        "fivesuperior",
        "sixsuperior",
        "sevensuperior",
        "eightsuperior",
        "ninesuperior",
        "zeroinferior",
        "oneinferior",
        "twoinferior",
        "threeinferior",
        "fourinferior",
        "fiveinferior",
        "sixinferior",
        "seveninferior",
        "eightinferior",
        "nineinferior",
        "centinferior",
        "dollarinferior",
        "periodinferior",
        "commainferior",
        "Agravesmall",
        "Aacutesmall",
        "Acircumflexsmall",
        "Atildesmall",
        "Adieresissmall",
        "Aringsmall",
        "AEsmall",
        "Ccedillasmall",
        "Egravesmall",
        "Eacutesmall",
        "Ecircumflexsmall",
        "Edieresissmall",
        "Igravesmall",
        "Iacutesmall",
        "Icircumflexsmall",
        "Idieresissmall",
        "Ethsmall",
        "Ntildesmall",
        "Ogravesmall",
        "Oacutesmall",
        "Ocircumflexsmall",
        "Otildesmall",
        "Odieresissmall",
        "OEsmall",
        "Oslashsmall",
        "Ugravesmall",
        "Uacutesmall",
        "Ucircumflexsmall",
        "Udieresissmall",
        "Yacutesmall",
        "Thornsmall",
        "Ydieresissmall",
        "001.000",
        "001.001",
        "001.002",
        "001.003",
        "Black",
        "Bold",
        "Book",
        "Light",
        "Medium",
        "Regular",
        "Roman",
        "Semibold" };
    
    /** Lookup table to map values */
    private static final int ISOAdobeCharset[] = { // 229
        // elements
        0,
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        15,
        16,
        17,
        18,
        19,
        20,
        21,
        22,
        23,
        24,
        25,
        26,
        27,
        28,
        29,
        30,
        31,
        32,
        33,
        34,
        35,
        36,
        37,
        38,
        39,
        40,
        41,
        42,
        43,
        44,
        45,
        46,
        47,
        48,
        49,
        50,
        51,
        52,
        53,
        54,
        55,
        56,
        57,
        58,
        59,
        60,
        61,
        62,
        63,
        64,
        65,
        66,
        67,
        68,
        69,
        70,
        71,
        72,
        73,
        74,
        75,
        76,
        77,
        78,
        79,
        80,
        81,
        82,
        83,
        84,
        85,
        86,
        87,
        88,
        89,
        90,
        91,
        92,
        93,
        94,
        95,
        96,
        97,
        98,
        99,
        100,
        101,
        102,
        103,
        104,
        105,
        106,
        107,
        108,
        109,
        110,
        111,
        112,
        113,
        114,
        115,
        116,
        117,
        118,
        119,
        120,
        121,
        122,
        123,
        124,
        125,
        126,
        127,
        128,
        129,
        130,
        131,
        132,
        133,
        134,
        135,
        136,
        137,
        138,
        139,
        140,
        141,
        142,
        143,
        144,
        145,
        146,
        147,
        148,
        149,
        150,
        151,
        152,
        153,
        154,
        155,
        156,
        157,
        158,
        159,
        160,
        161,
        162,
        163,
        164,
        165,
        166,
        167,
        168,
        169,
        170,
        171,
        172,
        173,
        174,
        175,
        176,
        177,
        178,
        179,
        180,
        181,
        182,
        183,
        184,
        185,
        186,
        187,
        188,
        189,
        190,
        191,
        192,
        193,
        194,
        195,
        196,
        197,
        198,
        199,
        200,
        201,
        202,
        203,
        204,
        205,
        206,
        207,
        208,
        209,
        210,
        211,
        212,
        213,
        214,
        215,
        216,
        217,
        218,
        219,
        220,
        221,
        222,
        223,
        224,
        225,
        226,
        227,
        228 };
    /** lookup data to convert Expert values */
    private static final int ExpertCharset[] = { // 166
        // elements
        0,
        1,
        229,
        230,
        231,
        232,
        233,
        234,
        235,
        236,
        237,
        238,
        13,
        14,
        15,
        99,
        239,
        240,
        241,
        242,
        243,
        244,
        245,
        246,
        247,
        248,
        27,
        28,
        249,
        250,
        251,
        252,
        253,
        254,
        255,
        256,
        257,
        258,
        259,
        260,
        261,
        262,
        263,
        264,
        265,
        266,
        109,
        110,
        267,
        268,
        269,
        270,
        271,
        272,
        273,
        274,
        275,
        276,
        277,
        278,
        279,
        280,
        281,
        282,
        283,
        284,
        285,
        286,
        287,
        288,
        289,
        290,
        291,
        292,
        293,
        294,
        295,
        296,
        297,
        298,
        299,
        300,
        301,
        302,
        303,
        304,
        305,
        306,
        307,
        308,
        309,
        310,
        311,
        312,
        313,
        314,
        315,
        316,
        317,
        318,
        158,
        155,
        163,
        319,
        320,
        321,
        322,
        323,
        324,
        325,
        326,
        150,
        164,
        169,
        327,
        328,
        329,
        330,
        331,
        332,
        333,
        334,
        335,
        336,
        337,
        338,
        339,
        340,
        341,
        342,
        343,
        344,
        345,
        346,
        347,
        348,
        349,
        350,
        351,
        352,
        353,
        354,
        355,
        356,
        357,
        358,
        359,
        360,
        361,
        362,
        363,
        364,
        365,
        366,
        367,
        368,
        369,
        370,
        371,
        372,
        373,
        374,
        375,
        376,
        377,
        378 };
    
    
    //one byte operators
    public static final int VERSION 			= 0 ;
    public static final int NOTICE 				= 1 ;
    public static final int FULLNAME 			= 2 ;
    public static final int FAMILYNAME 			= 3 ;
    public static final int WEIGHT 				= 4 ;
    public static final int FONTBBOX 			= 5 ;
    public static final int BLUEVALUES 			= 6 ;
    public static final int OTHERBLUES 			= 7 ;
    public static final int FAMILYBLUES 		= 8 ;
    public static final int FAMILYOTHERBLUES 	= 9 ;
    public static final int STDHW 				= 10 ;
    public static final int STDVW 				= 11 ;
    public static final int ESCAPE 				= 12 ;
    public static final int UNIQUEID 			= 13 ;
    public static final int XUID 				= 14 ;
    public static final int CHARSET 			= 15 ;
    public static final int ENCODING 			= 16 ;
    public static final int CHARSTRINGS 		= 17 ;
    public static final int PRIVATE 			= 18 ;
    public static final int SUBRS 				= 19 ;
    public static final int DEFAULTWIDTHX 		= 20 ;
    public static final int NOMINALWIDTHX 		= 21 ;
    public static final int RESERVED 			= 22 ;
    public static final int SHORTINT 			= 28 ;
    public static final int LONGINT 			= 29 ;
    public static final int BCD					= 30 ;
    
    //two byte operators
    public static final int COPYRIGHT 			= 3072 ;
    public static final int ISFIXEDPITCH 		= 3073 ;
    public static final int ITALICANGLE 		= 3074 ;
    public static final int UNDERLINEPOSITION 	= 3075 ;
    public static final int UNDERLINETHICKNESS 	= 3076 ;
    public static final int PAINTTYPE 			= 3077 ;
    public static final int CHARSTRINGTYPE 		= 3078 ;
    public static final int FONTMATRIX 			= 3079 ;
    public static final int STROKEWIDTH 		= 3080 ;
    public static final int BLUESCALE 			= 3081 ;
    public static final int BLUESHIFT 			= 3082 ;
    public static final int BLUEFUZZ 			= 3083 ;
    public static final int STEMSNAPH 			= 3084 ;
    public static final int STEMSNAPV 			= 3085 ;
    public static final int FORCEBOLD 			= 3086 ;
    public static final int LANGUAGEGROUP 		= 3089 ;
    public static final int EXPANSIONFACTOR 	= 3090 ;
    public static final int INITIALRANDOMSEED 	= 3091 ;
    public static final int SYNTHETICBASE 		= 3092 ;
    public static final int POSTSCRIPT 			= 3093 ;
    public static final int BASEFONTNAME 		= 3094 ;
    public static final int BASEFONTBLEND 		= 3095 ;
    public static final int ROS 				= 3102 ;
    public static final int CIDFONTVERSION 		= 3103 ;
    public static final int CIDFONTREVISION 	= 3104 ;
    public static final int CIDFONTTYPE 		= 3105 ;
    public static final int CIDCOUNT 			= 3106 ;
    public static final int UIDBASE 			= 3107 ;
    public static final int FDARRAY 			= 3108 ;
    public static final int FDSELECT 			= 3109 ;
    public static final int FONTNAME 			= 3110 ;
    
    
    private static final int weight = 388;//make it default
    private int[] blueValues;
   // private int[] otherBlues;
    private int[] familyBlues;
    private int[] familyOtherBlues;
    private int[] subrs = {-1};
    private int stdHW = -1,  stdVW = -1;
    
    //private byte[] encodingDataBytes;
    //private String[] stringIndexData;
    //private int[] charsetGlyphCodes;
    //private int charsetGlyphFormat;
    private final int[] rosArray = new int[3];
    
    
    /** needed so CIDFOnt0 can extend */
    public Type1C() {}
    
    
    
    /** get handles onto Reader so we can access the file */
    public Type1C(final PdfObjectReader current_pdf_file, final String substituteFont) {
        
        glyphs=new T1Glyphs(false);
        
        init(current_pdf_file);
        
        this.substituteFont=substituteFont;
    }
    
    
    /** read details of any embedded fontFile */
    protected void readEmbeddedFont(final PdfObject pdfFontDescriptor) throws Exception {
        
        if(substituteFont!=null){
            
            final byte[] bytes ;
            
            //read details
            final BufferedInputStream from;
            //create streams
            final FastByteArrayOutputStream to = new FastByteArrayOutputStream();
            
            InputStream jarFile = null;
            
            try{
                if(substituteFont.startsWith("jar:")|| substituteFont.startsWith("http:")) {
                    jarFile = loader.getResourceAsStream(substituteFont);
                } else {
                    jarFile = loader.getResourceAsStream("file:///"+substituteFont);
                }
                
            }catch(final Exception e){
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("3.Unable to open "+substituteFont+' '+e);
                }
            }catch(final Error err){
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("3.Unable to open "+substituteFont+' '+err);
                }
            }
            
            if(jarFile==null){
                /**
		            	from=new BufferedInputStream(new FileInputStream(substituteFont));
                
		              //write
		                byte[] buffer = new byte[65535];
		                int bytes_read;
		                while ((bytes_read = from.read(buffer)) != -1)
		                    to.write(buffer, 0, bytes_read);

		                to.close();
		                from.close();

		                /**/

                final File file=new File(substituteFont);
                final InputStream is = new FileInputStream(file);
                final long length = file.length();
                
                if (length > Integer.MAX_VALUE) {
                    System.out.println("Sorry! Your given file is too large.");
                    return;
                }
                
                bytes = new byte[(int)length];
                int offset = 0;
                int numRead  ;
                while (offset < bytes.length && (numRead=is.read(bytes,
                        offset, bytes.length-offset)) >= 0) {
                    offset += numRead;
                }
                if (offset < bytes.length) {
                    throw new IOException("Could not completely read file "
                            + file.getName());
                }
                is.close();
                // new BufferedReader
                //              (new InputStreamReader(loader.getResourceAsStream("org/jpedal/res/cid/" + encodingName), "Cp1252"));
                /**
		                FileReader from2=null;
		                try {
		                    from2 = new FileReader(substituteFont);
		                                //new BufferedReader);
		                    //outputStream = new FileWriter("characteroutput.txt");

		                    int c;
		                    while ((c = from2.read()) != -1) {
		                        to.write(c);
		                    }
		                } finally {
		                    if (from2 != null) {
		                        from2.close();
		                    }
		                    if (to != null) {
		                        to.close();
		                    }
		                }/**/
            }else{
                from= new BufferedInputStream(jarFile);
                
                //write
                final byte[] buffer = new byte[65535];
                int bytes_read;
                while ((bytes_read = from.read(buffer)) != -1) {
                    to.write(buffer, 0, bytes_read);
                }
                
                from.close();
                
                bytes=to.toByteArray();
            }
            /**load the font*/
            try{
                isFontSubstituted=true;
                
                //if (substituteFont.indexOf(".afm") != -1)
                readType1FontFile(bytes);
                //else
                //  readType1CFontFile(to.toByteArray(),null);
                
                
                
            } catch (final Exception e) {
                
                e.printStackTrace(System.out);
                
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("[PDF]Substitute font="+substituteFont+"Type 1 exception=" + e);
                }
            }
            
        }else if(pdfFontDescriptor!=null){
            
            final PdfObject FontFile=pdfFontDescriptor.getDictionary(PdfDictionary.FontFile);
            
            /** try type 1 first then type 1c/0c */
            if (FontFile != null) {
                try {
                    final byte[] stream=currentPdfFile.readStream(FontFile,true,true,false, false,false, FontFile.getCacheName(currentPdfFile.getObjectReader()));
                    if(stream!=null) {
                        readType1FontFile(stream);
                    }
                } catch (final Exception e) {
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: "+e.getMessage());
                    }
                    //
                }
            }else{
                
                final PdfObject FontFile3=pdfFontDescriptor.getDictionary(PdfDictionary.FontFile3);
                if(FontFile3!=null){
                    final byte[] stream=currentPdfFile.readStream(FontFile3,true,true,false, false,false, FontFile3.getCacheName(currentPdfFile.getObjectReader()));
                    if(stream!=null){ //if it fails, null returned
                        //check for type1c or ottf
                        if(stream.length>3 && stream[0]==70 && stream[1]==84 && stream[2]==84 && stream[3]==79){
                            //
                        }else {
                            //assume all standard cff for moment
                            readType1CFontFile(stream,null);
                        }
                    }
                }
            }
        }
    }
    
    /** read in a font and its details from the pdf file */
    @Override
    public void createFont(
            final PdfObject pdfObject,
            final String fontID,
            final boolean renderPage,
            final ObjectStore objectStore, final Map substitutedFonts)
            throws Exception {
        
        fontTypes = StandardFonts.TYPE1;
        
        //generic setup
        init(fontID, renderPage);
        
        /**
         * get FontDescriptor object - if present contains metrics on glyphs
         */
        final PdfObject pdfFontDescriptor=pdfObject.getDictionary(PdfDictionary.FontDescriptor);
        
        //FontBBox and FontMatix
        setBoundsAndMatrix(pdfFontDescriptor);
        
        setName(pdfObject);
        setEncoding(pdfObject, pdfFontDescriptor);
        
        try{
            readEmbeddedFont(pdfFontDescriptor);
        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: "+e.getMessage());
            }
            //
        }
        
        //setWidths(pdfObject);
        readWidths(pdfObject,true);
        
//            if(embeddedFontName!=null && is1C() && PdfStreamDecoder.runningStoryPad){
//            	embeddedFontName= cleanupFontName(embeddedFontName);
//            	this.setBaseFontName(embeddedFontName);
//            	this.setFontName(embeddedFontName);
//            }
        
        //make sure a font set
        if (renderPage) {
            setFont(getBaseFontName(), 1);
        }
        
    }
    
    /** Constructor for html fonts */
    public Type1C(final byte[] fontDataAsArray, final PdfJavaGlyphs glyphs, final boolean is1C) throws Exception{
        
        this.glyphs=glyphs;
        
        //generate reverse lookup so we can encode CMAP
        this.trackIndices=true;
        
        //flags we extract all details (used originally by rendering and now by PS2OTF)
        this.renderPage=true;
        
        if(is1C) {
            readType1CFontFile(fontDataAsArray,null);
        } else {
            readType1FontFile(fontDataAsArray);
        }
    }
    
    /** Constructor for OTF fonts */
    public Type1C(final byte[] fontDataAsArray, final FontData fontData, final PdfJavaGlyphs glyphs) throws Exception{
        
        this.glyphs=glyphs;
        
        readType1CFontFile(fontDataAsArray,fontData);
        
    }
    
    
    /** Handle encoding for type1C fonts. Also used for CIDFontType0C */
    @SuppressWarnings("PointlessBooleanExpression")
    private void readType1CFontFile(final byte[] fontDataAsArray, final FontData fontDataAsObject) throws Exception{
        
        if(LogWriter.isOutput()) {
            LogWriter.writeLog("Embedded Type1C font used");
        }
        
        glyphs.setis1C(true);
        
        final boolean isByteArray=(fontDataAsArray!=null);
        
        //debugFont=getBaseFontName().indexOf("LC")!=-1;
        
        if(debugFont) {
            System.err.println(getBaseFontName());
        }
        
        int start; //pointers within table
        final int size=2;
        
        /**
         * read Header
         */
        final int major;
        final int minor;
        if(isByteArray){
            major = fontDataAsArray[0];
            minor = fontDataAsArray[1];
        }else{
            major = fontDataAsObject.getByte(0);
            minor = fontDataAsObject.getByte(1);
        }
        
        if ((major != 1 || minor != 0) && LogWriter.isOutput()) {
            LogWriter.writeLog("1C  format "+ major+ ':' + minor+ " not fully supported");
        }
        
        if(debugFont) {
            System.out.println("major="+major+" minor="+minor);
        }
        
        // read header size to workout start of names index
        if(isByteArray) {
            top = fontDataAsArray[2];
        } else {
            top = fontDataAsObject.getByte(2);
        }
        
        /**
         * read names index
         */
        // read name index for the first font
        int count,offsize;
        if(isByteArray){
            count = getWord(fontDataAsArray, top, size);
            offsize = fontDataAsArray[top + size];
        }else{
            count = getWord(fontDataAsObject, top, size);
            offsize = fontDataAsObject.getByte(top + size);
        }
        
        /**
         * get last offset and use to move to top dict index
         */
        top += (size+1);  //move pointer to start of font names
        start = top + (count + 1) * offsize - 1; //move pointer to end of offsets
        if(isByteArray) {
            top = start + getWord(fontDataAsArray, top + count * offsize, offsize);
        } else {
            top = start + getWord(fontDataAsObject, top + count * offsize, offsize);
        }
        
        
        /**
         * read the dict index
         */
        if(isByteArray){
            count = getWord(fontDataAsArray, top, size);
            offsize = fontDataAsArray[top + size];
        }else{
            count = getWord(fontDataAsObject, top, size);
            offsize = fontDataAsObject.getByte(top + size);
        }
        
        top += (size+1); //update pointer
        start = top + (count + 1) * offsize - 1;
        
        int dicStart,dicEnd;
        if(isByteArray){
            dicStart = start + getWord(fontDataAsArray, top, offsize);
            dicEnd = start + getWord(fontDataAsArray, top + offsize, offsize);
        }else{
            dicStart = start + getWord(fontDataAsObject, top, offsize);
            dicEnd = start + getWord(fontDataAsObject, top + offsize, offsize);
        }
        
        /**
         * read string index
         */
        final String[] strings=readStringIndex(fontDataAsArray, fontDataAsObject, start, offsize, count);
        
        /**
         * read global subroutines (top set by Strings code)
         */
        readGlobalSubRoutines(fontDataAsArray,fontDataAsObject);
        
        /**
         * decode the dictionary
         */
        decodeDictionary(fontDataAsArray, fontDataAsObject, dicStart, dicEnd, strings);
        
        /**
         * allow  for subdictionaries in CID  font
         */
        if(FDSelect!=-1 ){
            
            
            if(debugDictionary) {
                System.out.println("=============FDSelect===================="+getBaseFontName());
            }
            
            
            //Read FDSelect
            int nextDic = FDSelect;
            
            final int format;
            if (isByteArray) {
                format = getWord(fontDataAsArray, nextDic, 1);
            } else {
                format = getWord(fontDataAsObject, nextDic, 1);
            }
            
            final int glyphCount;
            if (isByteArray) {
                glyphCount = getWord(fontDataAsArray, charstrings, 2);
            } else {
                glyphCount = getWord(fontDataAsObject, charstrings, 2);
            }
            
            fdSelect = new int[glyphCount];
            if (format == 0) {
                //Format 0 is just an array of which to use for each glyph
                for (int i=0; i<glyphCount; i++) {
                    if (isByteArray) {
                        fdSelect[i] = getWord(fontDataAsArray, nextDic + 1 + i, 1);
                    } else {
                        fdSelect[i] = getWord(fontDataAsObject, nextDic + 1 + i, 1);
                    }
                }
            } else if (format == 3) {
                final int nRanges;
                if (isByteArray) {
                    nRanges = getWord(fontDataAsArray, nextDic+1, 2);
                } else {
                    nRanges = getWord(fontDataAsObject, nextDic+1, 2);
                }
                
                final int[] rangeStarts= new int[nRanges+1];
                final int[] fDicts = new int[nRanges];

                //Find ranges of glyphs with their DICT index
                for (int i=0; i<nRanges; i++) {
                    if (isByteArray) {
                        rangeStarts[i] = getWord(fontDataAsArray, nextDic+3+(3*i), 2);
                        fDicts[i] = getWord(fontDataAsArray, nextDic+5+(3*i), 1);
                    } else {
                        rangeStarts[i] = getWord(fontDataAsObject, nextDic+3+(3*i), 2);
                        fDicts[i] = getWord(fontDataAsObject, nextDic+5+(3*i), 1);
                    }
                }
                rangeStarts[rangeStarts.length-1] = glyphCount;
                
                //Fill fdSelect array
                for (int i=0; i<nRanges; i++) {
                    for (int j=rangeStarts[i]; j<rangeStarts[i+1]; j++) {
                        fdSelect[j] = fDicts[i];
                    }
                }
            }
            ((T1Glyphs)glyphs).setFDSelect(fdSelect);
            
            
            //Read FDArray
            nextDic=FDArray;
            
            if(isByteArray){
                count = getWord(fontDataAsArray, nextDic, size);
                offsize = fontDataAsArray[nextDic + size];
            }else{
                count = getWord(fontDataAsObject, nextDic, size);
                offsize = fontDataAsObject.getByte(nextDic + size);
            }
            
            nextDic += (size+1); //update pointer
            start = nextDic + (count + 1) * offsize - 1;
            
            privateDictOffset = new int[count];
            privateDictLength = new int[count];
            subrs = new int[count];
            defaultWidthX = new int[count];
            nominalWidthX = new int[count];
            
            for (int i=0; i<count; i++) {
                currentFD = i;
                privateDictOffset[i] = -1;
                privateDictLength[i] = -1;
                subrs[i] = -1;
                
                if(isByteArray){
                    dicStart = start+getWord(fontDataAsArray, nextDic+(i*offsize), offsize);
                    dicEnd =start+getWord(fontDataAsArray, nextDic+((i+1)*offsize), offsize);
                }else{
                    dicStart = start+getWord(fontDataAsObject, nextDic+(i*offsize), offsize);
                    dicEnd =start+getWord(fontDataAsObject, nextDic+((i+1)*offsize), offsize);
                }
                
                decodeDictionary(fontDataAsArray, fontDataAsObject, dicStart, dicEnd, strings);
                
            }
            currentFD = -1;
            if(debugDictionary) {
                System.out.println("================================="+getBaseFontName());
            }
            
        }
        
        /**
         * get number of glyphs from charstrings index
         */
        top = charstrings;
        
        final int nGlyphs;
        
        if(isByteArray) {
            nGlyphs = getWord(fontDataAsArray, top, size); //start of glyph index
        } else {
            nGlyphs = getWord(fontDataAsObject, top, size); //start of glyph index
        }
        
        glyphs.setGlyphCount(nGlyphs);
        
        if(debugFont) {
            System.out.println("nGlyphs="+nGlyphs);
        }
        
        final int[] names =readCharset(charset, nGlyphs, fontDataAsObject,fontDataAsArray);
        
        if(debugFont){
            System.out.println("=======charset===============");
            final int count2=names.length;
            for(int jj=0;jj<count2;jj++){
                System.out.println(jj+" "+names[jj]);
            }
            
            System.out.println("=======Encoding===============");
        }
        
        /**
         * set encoding if not set
         */
        setEncoding(fontDataAsArray, fontDataAsObject,nGlyphs,names);
        
        /**
         * read glyph index
         */
        top = charstrings;
        readGlyphs(fontDataAsArray, fontDataAsObject, nGlyphs, names);
        
        /**/
        for (int i=0; i< privateDictOffset.length; i++) {
            currentFD = i;
            final int dict = privateDictOffset[i];
            if(dict != -1){
                
                //Decode Private DICT
                final int dictLength = privateDictLength[i];
                decodeDictionary(fontDataAsArray, fontDataAsObject, dict, dict+dictLength, strings);
                
                //Get font length
                final int fontLength;
                if(isByteArray) {
                    fontLength=fontDataAsArray.length;
                } else {
                    fontLength=fontDataAsObject.length();
                }
                
                //Check for Subrs & decode if found
                if((subrs[currentFD] != -1) && (subrs[currentFD] < fontLength)){
                    top = subrs[currentFD];
                    
                    final int nSubrs;
                    if(isByteArray) {
                        nSubrs = getWord(fontDataAsArray, top, size);
                    } else {
                        nSubrs = getWord(fontDataAsObject, top, size);
                    }
                    
                    if(nSubrs>0) {
                        readSubrs(fontDataAsArray, fontDataAsObject, nSubrs);
                    }
                }else if(debugFont || debugDictionary){
                    System.out.println("Private subroutine out of range");
                }
                
            }
        }
        currentFD = -1;
        /**/
        /**
         * set flags to tell software to use these descritpions
         */
        isFontEmbedded = true;
        
        glyphs.setFontEmbedded(true);
        
    }
    
    /**pick up encoding from embedded font*/
    private void setEncoding(final byte[] fontDataAsArray, final FontData fontDataAsObject, final int nGlyphs, final int[] names){
        
        final boolean isByteArray=fontDataAsArray!=null;
        
        if(debugFont) {
            System.out.println("Enc="+enc);
        }
        
        // read encoding (glyph -> code mapping)
        if (enc == 0){
            embeddedEnc=StandardFonts.STD;
            if (fontEnc == -1) {
                putFontEncoding(StandardFonts.STD);
            }
            
            if(isCID){
                //store values for lookup on text
                try{
                    
                    int count=nGlyphs;
                    if(count>names.length) {
                        count=names.length;
                    }
                    
                    String name;
                    for (int i = 1; i < count; ++i) {
                        
                        if(names[i]<391){
                            if(isByteArray) {
                                name =getString(fontDataAsArray,names[i],stringIdx,stringStart,stringOffSize);
                            } else {
                                name =getString(fontDataAsObject,names[i],stringIdx,stringStart,stringOffSize);
                            }
                            
                            putMappedChar(names[i],StandardFonts.getUnicodeName(name) );
                        }
                    }
                }catch(final Exception e){
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: "+e.getMessage());
                    }
                    //
                }
                
            }
        }else if (enc == 1){
            embeddedEnc=StandardFonts.MACEXPERT;
            if (fontEnc == -1) {
                putFontEncoding(StandardFonts.MACEXPERT);
            }
        }else { //custom mapping
            
            if(debugFont) {
                System.out.println("custom mapping");
            }
            
            top = enc;
            final int encFormat;
            int c;

            if(isByteArray) {
                encFormat = (fontDataAsArray[top++] & 0xff);
            } else {
                encFormat = (fontDataAsObject.getByte(top++) & 0xff);
            }
            
            String name;
            
            if ((encFormat & 0x7f) == 0) { //format 0
                
                int nCodes;
                
                if(isByteArray) {
                    nCodes = 1 + (fontDataAsArray[top++] & 0xff);
                } else {
                    nCodes = 1 + (fontDataAsObject.getByte(top++) & 0xff);
                }
                
                if (nCodes > nGlyphs) {
                    nCodes = nGlyphs;
                }
                for (int i = 1; i < nCodes; ++i) {
                    
                    if(isByteArray){
                        c =fontDataAsArray[top++] & 0xff;
                        name =getString(fontDataAsArray,names[i],stringIdx,stringStart,stringOffSize);
                        
                    }else{
                        c =fontDataAsObject.getByte(top++) & 0xff;
                        name =getString(fontDataAsObject,names[i],stringIdx,stringStart,stringOffSize);
                    }
                    
                    putChar(c, name);
                    
                }
                
            } else if ((encFormat & 0x7f) == 1) { //format 1
                
                final int nRanges;
                if(isByteArray) {
                    nRanges = (fontDataAsArray[top++] & 0xff);
                } else {
                    nRanges = (fontDataAsObject.getByte(top++) & 0xff);
                }
                
                int nCodes = 1;
                for (int i = 0; i < nRanges; ++i) {
                    
                    final int nLeft;
                    
                    if(isByteArray){
                        c = (fontDataAsArray[top++] & 0xff);
                        nLeft = (fontDataAsArray[top++] & 0xff);
                    }else{
                        c = (fontDataAsObject.getByte(top++) & 0xff);
                        nLeft = (fontDataAsObject.getByte(top++) & 0xff);
                    }
                    
                    for (int j = 0; j <= nLeft && nCodes < nGlyphs; ++j) {
                        
                        if(isByteArray) {
                            name =getString(fontDataAsArray,names[nCodes],stringIdx,stringStart,stringOffSize);
                        } else {
                            name =getString(fontDataAsObject,names[nCodes],stringIdx,stringStart,stringOffSize);
                        }
                        
                        putChar(c, name);
                        
                        nCodes++;
                        c++;
                    }
                }
            }
            
            if ((encFormat & 0x80) != 0) { //supplimentary encodings
                
                final int nSups;
                
                if(isByteArray) {
                    nSups = (fontDataAsArray[top++] & 0xff);
                } else {
                    nSups = (fontDataAsObject.getByte(top++) & 0xff);
                }
                
                for (int i = 0; i < nSups; ++i) {
                    
                    if(isByteArray) {
                        c = (fontDataAsArray[top++] & 0xff);
                    } else {
                        c = (fontDataAsObject.getByte(top++) & 0xff);
                    }
                    
                    final int sid;
                    
                    if(isByteArray) {
                        sid = getWord(fontDataAsArray, top, 2);
                    } else {
                        sid = getWord(fontDataAsObject, top, 2);
                    }
                    
                    top += 2;
                    
                    if(isByteArray) {
                        name =getString(fontDataAsArray,sid,stringIdx,stringStart,stringOffSize);
                    } else {
                        name =getString(fontDataAsObject,sid,stringIdx,stringStart,stringOffSize);
                    }
                    
                    putChar(c, name);
                    
                }
            }
        }
    }
    
    // LILYPONDTOOL
    private void readSubrs(final byte[] fontDataAsArray, final FontData fontDataAsObject, final int nSubrs) throws Exception {
        
        final boolean isByteArray=fontDataAsArray!=null;
        
        final int subrOffSize;
        
        if(isByteArray) {
            subrOffSize = fontDataAsArray[top+2];
        } else {
            subrOffSize = fontDataAsObject.getByte(top+2);
        }
        
        top+=3;
        final int subrIdx = top;
        final int subrStart = top + (nSubrs + 1) * subrOffSize - 1;
        
        final int nextTablePtr= top+nSubrs*subrOffSize;
        
        if(isByteArray){
            if(nextTablePtr<fontDataAsArray.length) { //allow for table at end of file
                top = subrStart + getWord(fontDataAsArray,nextTablePtr, subrOffSize);
            } else {
                top=fontDataAsArray.length-1;
            }
        }else{
            if(nextTablePtr<fontDataAsArray.length) { //allow for table at end of file
                top = subrStart + getWord(fontDataAsObject, nextTablePtr, subrOffSize);
            } else {
                top=fontDataAsObject.length()-1;
            }
        }
        
        final int[] subrOffset = new int [nSubrs + 2];
        int ii = subrIdx;
        for (int jj = 0; jj<nSubrs+1; jj++) {
            
            if(isByteArray){
                if((ii+subrOffSize)<fontDataAsArray.length) {
                    subrOffset[jj] = subrStart + getWord(fontDataAsArray, ii, subrOffSize);
                }
            }else{
                if((ii+subrOffSize)<fontDataAsObject.length()) {
                    subrOffset[jj] = subrStart + getWord(fontDataAsObject, ii, subrOffSize);
                }
            }
            
            ii += subrOffSize;
        }
        subrOffset[nSubrs + 1] = top;
        
        glyphs.setLocalBias(calculateSubroutineBias(nSubrs));
        
        //read the glyphs and store
        int current = subrOffset[0];
        
        for (int jj = 1; jj < nSubrs+1; jj++) {
            
            
            //skip if out of bounds
            if(current==0 || subrOffset[jj]>fontDataAsArray.length || subrOffset[jj]<0 || subrOffset[jj]==0) {
                continue;
            }
            
            final FastByteArrayOutputStream nextSubr = new FastByteArrayOutputStream();
            
            for (int c = current; c < subrOffset[jj]; c++){
                if(!isByteArray && c<fontDataAsObject.length()) {
                    nextSubr.write(fontDataAsObject.getByte(c));
                }
                
            }
            
            if(isByteArray){
                
                final int length=subrOffset[jj]-current;
                
                if(length>0){
                    final byte[] nextSub=new byte[length];
                    
                    System.arraycopy(fontDataAsArray,current,nextSub,0,length);
                    
                    glyphs.setCharString("subrs"+(jj-1),nextSub, jj);
                }
            }else{

                glyphs.setCharString("subrs"+(jj-1),nextSubr.toByteArray(), jj);
            }
            current = subrOffset[jj];
            
        }
    }
    
    
    private void readGlyphs(final byte[] fontDataAsArray, final FontData fontDataAsObject, final int nGlyphs, final int[] names) throws Exception{
        
        final boolean isByteArray=fontDataAsArray!=null;
        
        final int glyphOffSize;
        
        if(isByteArray) {
            glyphOffSize = fontDataAsArray[top + 2];
        } else {
            glyphOffSize = fontDataAsObject.getByte(top + 2);
        }
        
        top += 3;
        final int glyphIdx = top;
        final int glyphStart = top + (nGlyphs + 1) * glyphOffSize - 1;
        
        if(isByteArray) {
            top =glyphStart+ getWord(fontDataAsArray,top + nGlyphs * glyphOffSize,glyphOffSize);
        } else {
            top =glyphStart+ getWord(fontDataAsObject,top + nGlyphs * glyphOffSize,glyphOffSize);
        }
        
        final int[] glyphoffset = new int[nGlyphs + 2];
        
        int ii = glyphIdx;
        
        //read the offsets
        for (int jj = 0; jj < nGlyphs + 1; jj++) {
            
            if(isByteArray) {
                glyphoffset[jj] = glyphStart+getWord(fontDataAsArray,ii,glyphOffSize);
            } else {
                glyphoffset[jj] = glyphStart+getWord(fontDataAsObject,ii,glyphOffSize);
            }
            
            ii += glyphOffSize;
            
        }
        
        glyphoffset[nGlyphs + 1] = top;
        
        //read the glyphs and store
        int current = glyphoffset[0];
        String glyphName;
        byte[] nextGlyph;
        for (int jj = 1; jj < nGlyphs+1; jj++) {
            
            nextGlyph=new byte[glyphoffset[jj]-current];  //read name of glyph
            
            //get data for the glyph
            for (int c = current; c < glyphoffset[jj]; c++){
                
                if(isByteArray) {
                    nextGlyph[c-current]=fontDataAsArray[c];
                } else {
                    nextGlyph[c-current]=fontDataAsObject.getByte(c);
                }
            }
            
            if(isCID){
                glyphName =String.valueOf(names[jj - 1]);
            }else{
                
                if(isByteArray) {
                    glyphName =getString(fontDataAsArray,names[jj-1],stringIdx,stringStart,stringOffSize);
                } else {
                    glyphName =getString(fontDataAsObject,names[jj-1],stringIdx,stringStart,stringOffSize);
                }
            }
            if(debugFont) {
                System.out.println("glyph= "+ glyphName +" start="+current+" length="+glyphoffset[jj]+" isCID="+isCID);
            }

            glyphs.setCharString(glyphName,nextGlyph, jj);
            
            current = glyphoffset[jj];
            
            if(trackIndices){
                glyphs.setIndexForCharString(jj, glyphName);
                
            }
        }
    }
    
    private static int calculateSubroutineBias(final int subroutineCount) {
        final int bias;
        if (subroutineCount < 1240) {
            bias = 107;
        } else if (subroutineCount < 33900) {
            bias = 1131;
        } else {
            bias = 32768;
        }
        return bias;
    }
    
    private void readGlobalSubRoutines(final byte[] fontDataAsArray, final FontData fontDataAsObject) throws Exception{
        
        final boolean isByteArray=(fontDataAsArray!=null);
        
        final int subOffSize;
        final int count;

        if(isByteArray){
            subOffSize = (fontDataAsArray[top + 2] & 0xff);
            count= getWord(fontDataAsArray, top, 2);
        }else{
            subOffSize = (fontDataAsObject.getByte(top + 2) & 0xff);
            count= getWord(fontDataAsObject, top, 2);
        }
        
        top += 3;
        if(count>0){
            
            final int idx = top;
            final int start = top + (count + 1) * subOffSize - 1;
            if(isByteArray) {
                top =start+ getWord(fontDataAsArray,top + count * subOffSize,subOffSize);
            } else {
                top =start+ getWord(fontDataAsObject,top + count * subOffSize,subOffSize);
            }
            
            final int[] offset = new int[count + 2];
            
            int ii = idx;
            
            //read the offsets
            for (int jj = 0; jj < count + 1; jj++) {
                
                if(isByteArray) {
                    offset[jj] = start + getWord(fontDataAsArray,ii,subOffSize);
                } else {
                    offset[jj] = start + getWord(fontDataAsObject,ii,subOffSize);
                }
                
                ii += subOffSize;
                
            }
            
            offset[count + 1] = top;
            
            glyphs.setGlobalBias(calculateSubroutineBias(count));
            
            //read the subroutines and store
            int current = offset[0];
            for (int jj = 1; jj < count+1; jj++) {
                
                final FastByteArrayOutputStream nextStream = new FastByteArrayOutputStream();
                for (int c = current; c < offset[jj]; c++){
                    if(isByteArray) {
                        nextStream.write(fontDataAsArray[c]);
                    } else {
                        nextStream.write(fontDataAsObject.getByte(c));
                    }
                }

                //store
                glyphs.setCharString("global"+(jj-1),nextStream.toByteArray(), jj);
                
                //setGlobalSubroutine(new Integer(jj-1+bias),nextStream.toByteArray());
                current = offset[jj];
                
            }
        }
    }
    
    private void decodeDictionary(final byte[] fontDataAsArray, final FontData fontDataAsObject, final int dicStart, final int dicEnd, final String[] strings){
        
        boolean fdReset=false;

        final boolean isByteArray=fontDataAsArray!=null;
        
        int p = dicStart, nextVal,key;
        int i=0;
        final double[] op = new double[48]; //current operand in dictionary
        
        while (p < dicEnd) {
            
            if(isByteArray) {
                nextVal = fontDataAsArray[p] & 0xFF;
            } else {
                nextVal = fontDataAsObject.getByte(p) & 0xFF;
            }
            
            if (nextVal <= 27 || nextVal == 31) { // operator
                
                key = nextVal;
                
                p++;

                if (key == 0x0c) { //handle 2 byte keys
                    
                    if(isByteArray) {
                        key = fontDataAsArray[p] & 0xFF;
                    } else {
                        key = fontDataAsObject.getByte(p) & 0xFF;
                    }

                    p++;

                    fdReset = handle2ByteOperand(strings, fdReset, key, op);
                } else {
                    handle1ByteOperand(dicStart, strings, key, op);
                }
                
                i=0;
                
            }else{
                
                if(isByteArray) {
                    p= T1GlyphNumber.getNumber(fontDataAsArray, p, op, i, is1C());
                } else {
                    p= T1GlyphNumber.getNumber(fontDataAsObject, p, op, i, is1C());
                }
                
                i++;
            }
        }

        //reset
        if(!fdReset) {
            FDSelect=-1;
        }
    }

    private void handle1ByteOperand(final int dicStart, final String[] strings, final int key, final double[] op) {

        switch(key) {
            case 2: //fullname

                int id = (int) op[0];
                if (id > 390) {
                    id -= 390;
                }
                embeddedFontName = strings[id];
                break;

            case 3: //familyname
                //embeddedFamilyName=strings[id];
                break;

            case 5: //fontBBox
                for (int ii = 0; ii < 4; ii++) {
                    this.FontBBox[ii] = (float) op[ii];
                }
                //hasFontBBox=true;
                break;

            case 0x0f: // charset
                charset = (int) op[0];
                break;

            case 0x10: // encoding
                enc = (int) op[0];
                break;

            case 0x11: // charstrings
                charstrings = (int) op[0];
                break;

            case 18:
                if(glyphs.is1C()) { // readPrivate
                    int dictNo = currentFD;
                    if (dictNo == -1) {
                        dictNo = 0;
                    }

                    privateDictOffset[dictNo] = (int) op[1];
                    privateDictLength[dictNo] = (int) op[0];
                }
                break;

            case 19:  //Subrs
            {
                int dictNo = currentFD;
                if (dictNo == -1) {
                    dictNo = 0;
                }

                subrs[dictNo] = dicStart + (int) op[0];
            }
            break;

            case 20:         //defaultWidthX
            {
                int dictNo = currentFD;
                if (dictNo == -1) {
                    dictNo = 0;
                }

                defaultWidthX[dictNo] = (int) op[0];
                if (glyphs instanceof T1Glyphs) {
                    ((T1Glyphs) glyphs).setWidthValues(defaultWidthX, nominalWidthX);
                }
            }
            break;

            case 21: //nominalWidthX
            {
                int dictNo = currentFD;
                if (dictNo == -1) {
                    dictNo = 0;
                }

                nominalWidthX[dictNo] = (int) op[0];
                if (glyphs instanceof T1Glyphs) {
                    ((T1Glyphs) glyphs).setWidthValues(defaultWidthX, nominalWidthX);
                }
            }
            break;
        }
    }

    private boolean handle2ByteOperand(final String[] strings, boolean fdReset, final int key, final double[] op) {

        if(key!=36 && key!=37 && key!=7 && FDSelect!=-1){

        }else {
            switch(key) {

                case 0: //copyright
                    int id = (int) op[0];
                    if (id > 390) {
                        id -= 390;
                    }
                    copyright = strings[id];
                    break;

                case 2: //italic
                     italicAngle = (int) op[0];
                   break;

                case  6:
                    if (op[0] > 0) {
                        blueValues = new int[6];
                        for (int z = 0; z < blueValues.length; z++) {
                            blueValues[z] = (int) op[z];
                        }
                    }
                    break;

                case 7: //fontMatrix
                    if (!hasFontMatrix) {
                        System.arraycopy(op, 0, FontMatrix, 0, 6);
                    }

                    hasFontMatrix = true;
                    break;

                case 8:
                    familyBlues = new int[6];
                    for (int z = 0; z < familyBlues.length; z++) {
                        familyBlues[z] = (int) op[z];
                    }
                    break;

                case 9:
                    if (op[0] > 0) {
                        familyOtherBlues = new int[6];
                        for (int z = 0; z < familyOtherBlues.length; z++) {
                            familyOtherBlues[z] = (int) op[z];
                        }
                    }
                    break;

                case 10:
                    stdHW = (int) op[0];
                    break;

                case 11:
                    stdVW = (int) op[0];
                    break;

                case 21: //Postscript
                    //postscriptFontName=strings[id];
                    break;

                case 22: //BaseFontname
                    //baseFontName=strings[id];
                    break;

                case 30: //ROS
                    ros = (int) op[0];
                    isCID = true;
                    break;

                case 31: //CIDFontVersion
                    CIDFontVersion = (int) op[0];
                    break;

                case 32: //CIDFontRevision
                    CIDFontRevision = (int) op[0];
                    break;

                case 33: //CIDFontType
                    CIDFontType = (int) op[0];
                    break;

                case 34: //CIDcount
                    CIDcount = (int) op[0];
                    break;

                case 35: //UIDBase
                    UIDBase = (int) op[0];
                    break;

                case 36: //FDArray
                    FDArray = (int) op[0];
                    break;

                case 37: //FDSelect
                    FDSelect = (int) op[0];

                    fdReset = true;
                    break;

                case 38: //fullname
                    //fullname=strings[id];
                    break;

            }
        }
        return fdReset;
    }

    private String[] readStringIndex(final byte[] fontDataAsArray, final FontData fontDataAsObject, final int start, final int offsize, final int count){
        
        final int nStrings;
        
        final boolean isByteArray=(fontDataAsArray!=null);
        
        if(isByteArray){
            top = start + getWord(fontDataAsArray, top + count * offsize, offsize);
            //start of string index
            nStrings = getWord(fontDataAsArray, top, 2);
            stringOffSize = fontDataAsArray[top + 2];
        }else{
            top = start + getWord(fontDataAsObject, top + count * offsize, offsize);
            //start of string index
            nStrings = getWord(fontDataAsObject, top, 2);
            stringOffSize = fontDataAsObject.getByte(top + 2);
        }
        
        top += 3;
        stringIdx = top;
        stringStart = top + (nStrings + 1) * stringOffSize - 1;
        
        if(isByteArray) {
            top =stringStart+ getWord(fontDataAsArray,top + nStrings * stringOffSize,stringOffSize);
        } else {
            top =stringStart+ getWord(fontDataAsObject,top + nStrings * stringOffSize,stringOffSize);
        }
        
        final int[] offsets = new int[nStrings + 2];
        final String[] strings = new String[nStrings + 2];
        
        int ii = stringIdx;
        //read the offsets
        for (int jj = 0; jj < nStrings + 1; jj++) {
            
            if(isByteArray) {
                offsets[jj] = getWord(fontDataAsArray,ii,stringOffSize); //content[ii] & 0xff;
            } else {
                offsets[jj] = getWord(fontDataAsObject,ii,stringOffSize); //content[ii] & 0xff;
            }
            //getWord(content,ii,stringOffSize);
            ii += stringOffSize;
            
        }
        
        offsets[nStrings + 1] = top - stringStart;
        
        //read the strings
        int current = 0;
        StringBuilder nextString;
        for (int jj = 0; jj < nStrings + 1; jj++) {
            
            nextString = new StringBuilder(offsets[jj]-current);
            for (int c = current; c < offsets[jj]; c++){
                if(isByteArray) {
                    nextString.append((char) fontDataAsArray[stringStart + c]);
                } else {
                    nextString.append((char) fontDataAsObject.getByte(stringStart + c));
                }
            }
            
            if(debugFont) {
                System.out.println("String "+jj+" ="+nextString);
            }
            
            strings[jj] = nextString.toString();
            current = offsets[jj];
            
        }
        return strings;
    }
    
    /** Utility method used during processing of type1C files */
    private static String getString(final FontData fontDataAsObject,int sid, final int idx, final int start, final int offsize) {
        
        int len;
        final String result  ;
        
        if (sid < 391) {
            result = type1CStdStrings[sid];
        } else {
            sid -= 391;
            final int idx0 =start+ getWord(fontDataAsObject, idx + sid * offsize,offsize);
            final int idxPtr1 =start+ getWord(fontDataAsObject, idx + (sid + 1) * offsize,offsize);
            //System.out.println(sid+" "+idx0+" "+idxPtr1);
            if ((len = idxPtr1 - idx0) > 255) {
                len = 255;
            }
            
            result = new String(fontDataAsObject.getBytes(idx0,len));
        }
        return result;
    }
    
    /** Utility method used during processing of type1C files */
    private static String getString(final byte[] fontDataAsArray,int sid, final int idx, final int start, final int offsize) {
        
        int len;
        final String result  ;
        
        if (sid < 391) {
            result = type1CStdStrings[sid];
        } else {
            sid -= 391;
            final int idx0 =start+ getWord(fontDataAsArray, idx + sid * offsize,offsize);
            final int idxPtr1 =start+ getWord(fontDataAsArray, idx + (sid + 1) * offsize,offsize);
            //System.out.println(sid+" "+idx0+" "+idxPtr1);
            if ((len = idxPtr1 - idx0) > 255) {
                len = 255;
            }
            
            result=new String(fontDataAsArray,idx0,len);
            
        }
        return result;
    }
    
    
    
    /** get standard charset or extract from type 1C font */
    private int[] readCharset(final int charset, final int nGlyphs, final FontData fontDataAsObject, final byte[] fontDataAsArray) {
        
        final boolean isByteArray=fontDataAsArray!=null;
        
        final int[] glyphNames  ;
        int i, j;
        
        if(debugFont) {
            System.out.println("charset="+charset);
        }
        
        /**/
		//handle CIDS first
		if(isCID && charset>8000){
			glyphNames = new int[nGlyphs];
			glyphNames[0] = 0;

			for (i = 1; i < nGlyphs; ++i) {
				glyphNames[i] = i;//getWord(fontData, top, 2);
				//top += 2;
           //     System.out.println(i+" ");
				}

		// read appropriate non-CID charset
		}else if (charset == 0) {
             glyphNames = ISOAdobeCharset;
         } else if (charset == 1) {
             glyphNames = ExpertCharset;
         } else if (charset == 2) {
             glyphNames = ExpertSubCharset;
         } else {
             glyphNames = new int[nGlyphs+1];
             glyphNames[0] = 0;
             int top = charset;
             
             final int charsetFormat;
             
             if(isByteArray) {
                 charsetFormat = fontDataAsArray[top++] & 0xff;
             } else {
                 charsetFormat = fontDataAsObject.getByte(top++) & 0xff;
             }
             
             if(debugFont) {
                 System.out.println("charsetFormat="+charsetFormat);
             }
             
             if (charsetFormat == 0) {
                 for (i = 1; i < nGlyphs; ++i) {
                     if(isByteArray) {
                         glyphNames[i] = getWord(fontDataAsArray, top, 2);
                     } else {
                         glyphNames[i] = getWord(fontDataAsObject, top, 2);
                     }
                     
                     top += 2;
                 }
                 
             } else if (charsetFormat == 1) {
                 
                 i = 1;
                 
                 int c,nLeft;
                 while (i < nGlyphs) {
                     
                     if(isByteArray) {
                         c = getWord(fontDataAsArray, top, 2);
                     } else {
                         c = getWord(fontDataAsObject, top, 2);
                     }
                     top += 2;
                     if(isByteArray) {
                         nLeft = fontDataAsArray[top++] & 0xff;
                     } else {
                         nLeft = fontDataAsObject.getByte(top++) & 0xff;
                     }
                     
                     for (j = 0; j <= nLeft; ++j) {
                         glyphNames[i++] =c++;
                     }
                     
                 }
             } else if (charsetFormat == 2) {
                 i = 1;
                 
                 int c,nLeft;
                 
                 while (i < nGlyphs) {
                     if(isByteArray) {
                         c = getWord(fontDataAsArray, top, 2);
                     } else {
                         c = getWord(fontDataAsObject, top, 2);
                     }
                     
                     top += 2;
                     
                     if(isByteArray) {
                         nLeft = getWord(fontDataAsArray, top, 2);
                     } else {
                         nLeft = getWord(fontDataAsObject, top, 2);
                     }
                     
                     top += 2;
                     for (j = 0; j <= nLeft; ++j) {
                         glyphNames[i++] =c++;
                     }
                 }
             }
         }
         
         return glyphNames;
    }
    
    /** Utility method used during processing of type1C files */
    private static int getWord(final FontData fontDataAsObject, final int index, final int size) {
        int result = 0;
        for (int i = 0; i < size; i++) {
            result = (result << 8) + (fontDataAsObject.getByte(index + i) & 0xff);
            
        }
        return result;
    }
    
    /** Utility method used during processing of type1C files */
    private static int getWord(final byte[] fontDataAsArray, final int index, final int size) {
        int result = 0;
        for (int i = 0; i < size; i++) {
            result = (result << 8) + (fontDataAsArray[index + i] & 0xff);
            
        }
        return result;
    }
    
    /**
     * get bounding box to highlight
     * @return
     */
    @Override
    public Rectangle getBoundingBox() {
        
        if(BBox==null){
            if(isFontEmbedded) {
                BBox=new Rectangle((int)FontBBox[0], (int)FontBBox[1],
                        (int)(FontBBox[2]-FontBBox[0]), (int)(FontBBox[3]-FontBBox[1]));  //To change body of created methods use File | Settings | File Templates.
            } else {
                BBox=super.getBoundingBox();
            }
        }
        
        return BBox;
    }
    
    
    /**
     * @return The Font Dictionary select array
     */
    public int[] getFDSelect() {
        return fdSelect;
    }
    
    public int[] getRosArray(){
        return rosArray;
    }
    
    public static byte[] getOperatorBytes(final int key){
        byte [] b = null;
        if(key<=30 && key>=0){
            b = new byte[]{(byte)key};
            return b;
        }
        else if(key>=3072 && key<=3110){
            b = setNextInt16(key);
            return b;
        }
        return b;
        
    }
    
    /**
     * turn int back into byte[2]
     **/
    static final byte[] setNextInt16(final int value){

        final byte[] returnValue=new byte[2];

        for(int i=0;i<2;i++){
            returnValue[i]= (byte) ((value>>(8*(1-i)))& 255);
        }
        return returnValue;
    }

    
    public Object getKeyValue(final int key){
        
        switch(key){
            case WEIGHT:			return weight;
            case ITALICANGLE:		return italicAngle;
            case FONTMATRIX:		return FontMatrix;
            case FONTBBOX:			return FontBBox;
            case ENCODING:			return enc;
                
            case DEFAULTWIDTHX:		return defaultWidthX[0];
            case NOMINALWIDTHX:		return nominalWidthX[0];
            case BLUEVALUES:		return blueValues;
            case OTHERBLUES:		return otherBlues;
            case FAMILYBLUES:		return familyBlues;
            case FAMILYOTHERBLUES:	return familyOtherBlues;
            case STDHW:				return stdHW;
            case STDVW:				return stdVW;
            case SUBRS:				return subrs;
                
            case ROS:				return ros;
            case CIDCOUNT:			return CIDcount;
            case CIDFONTREVISION:	return CIDFontRevision;
            case CIDFONTVERSION:	return CIDFontVersion;
            case CIDFONTTYPE:		return CIDFontType;
            case FDARRAY:			return FDArray;
            case FDSELECT:			return FDSelect;
                
            default:
                throw new RuntimeException("Key is unknown or value is not yet assigned "+key );
        }
    }
    
}



