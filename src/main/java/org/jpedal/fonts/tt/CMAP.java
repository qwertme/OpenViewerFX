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
 * CMAP.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jpedal.fonts.StandardFonts;
import org.jpedal.utils.LogWriter;

public class CMAP extends Table {

    protected int[][] glyphIndexToChar;
    
    private boolean remapType4;
    
    private boolean hasFormatZero;

    private int[] glyphToIndex;

    //flag 6 and use if not able to map elsewhere
    private boolean hasSix;

    //flag 4 
    private boolean hasFormat4;
    private boolean hasFormat6;

    private int lastFormat4Found = -1;

    //used by format 6
    private int firstCode	=	-1;
    private int entryCount	=	-1;

    //used by format 4
    private int segCount;

    /**which type of mapping to use*/
    private int fontMapping;

    //used by format 4
    protected int[] endCode;
    protected int[] startCode;
    protected int[] idDelta;
    protected int[] idRangeOffset;
    protected int[] glyphIdArray;
    private int[] f6glyphIdArray;
    private int[] offset;

    //used by Format 12
    int nGroups;
    private int[] startCharCode;
    private int[] endCharCode;
    private int[] startGlyphCode;

    /**CMap format used -1 shows not set*/
    protected int[] CMAPformats,CMAPlength,CMAPlang,CMAPsegCount,CMAPsearchRange, CMAPentrySelector,CMAPrangeShift,CMAPreserved;

    /**Platform-specific ID list*/
//	private static String[] PlatformSpecificID={"Roman","Japanese","Traditional Chinese","Korean",
//			"Arabic","Hebrew","Greek","Russian",
//			"RSymbol","Devanagari","Gurmukhi","Gujarati",
//			"Oriya","Bengali","Tamil","Telugu",
//			"Kannada","Malayalam","Sinhalese","Burmese",
//			"Khmer","Thai","Laotian","Georgian",
//			"Armenian","Simplified Chinese","Tibetan","Mongolian",
//			"Geez","Slavic","Vietnamese","Sindhi","(Uninterpreted)"};
//
    /**Platform-specific ID list*/
    //private static String[] PlatformIDName={"Unicode","Macintosh","Reserved","Microsoft"};

    /**shows which encoding used*/
    protected int[] platformID;

    private static final Map exceptions;

    /**set up differences from Mac Roman*/
    static {

        exceptions=new HashMap();

        final String[] keys={"notequal","infinity","lessequal","greaterequal",
                "partialdiff","summation","product","pi",
                "integral","Omega","radical","approxequal",
                "Delta","lozenge","Euro","apple"};
         
        final int[] values={173,176,178,179,
                182,183,184,185,
                186,189,195,197,
                198,215,219,240};
        
         for(int i=0;i<values.length;i++) {
            exceptions.put(keys[i], values[i]);
        }
        StandardFonts.checkLoaded(StandardFonts.WIN);
                     
    }

    /**which CMAP to use to decode the font*/
    private int formatToUse;

    protected int id,numberSubtables;

    protected int[] CMAPsubtables,platformSpecificID;
   
    private int fontEncoding;

    public CMAP(final FontFile2 currentFontFile, final int startPointer){

        final boolean debug=false;

        if(debug) {
            System.out.println("CMAP " + this);
        }

        //LogWriter.writeMethod("{readCMAPTable}", 0);

        //read 'cmap' table
        if(startPointer==0){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("No CMAP table found");
            }
        }else{

            id=currentFontFile.getNextUint16();//id
            numberSubtables=currentFontFile.getNextUint16();

            //read the subtables
            CMAPsubtables=new int[numberSubtables];
            platformID=new int[numberSubtables];
            platformSpecificID=new int[numberSubtables];
            CMAPformats=new int[numberSubtables];
            CMAPsearchRange=new int[numberSubtables];
            CMAPentrySelector=new int[numberSubtables];
            CMAPrangeShift=new int[numberSubtables];
            CMAPreserved=new int[numberSubtables];
            CMAPsegCount=new int[numberSubtables];
            CMAPlength=new int[numberSubtables];
            CMAPlang=new int[numberSubtables];
            glyphIndexToChar =new int[numberSubtables][256];

            glyphToIndex=new int[256];

            for(int i=0;i<numberSubtables;i++){

                platformID[i]=currentFontFile.getNextUint16();
                platformSpecificID[i]=currentFontFile.getNextUint16();
                CMAPsubtables[i]=currentFontFile.getNextUint32();

                if(debug) {
                    System.out.println("IDs platformID=" + platformID[i] + " platformSpecificID=" + platformSpecificID[i] + " CMAPsubtables=" + CMAPsubtables[i]);
                }
                //System.out.println(PlatformID[platformID[i]]+" "+PlatformSpecificID[platformSpecificID[i]]+CMAPsubtables[i]);

            }

            //now read each subtable
            for(int j=0;j<numberSubtables;j++){
                currentFontFile.selectTable(FontFile2.CMAP);
                currentFontFile.skip(CMAPsubtables[j]);

                //assume 16 bit format to start
                CMAPformats[j]=currentFontFile.getNextUint16();
                CMAPlength[j]=currentFontFile.getNextUint16();
                CMAPlang[j]=currentFontFile.getNextUint16();//lang

                if(debug) {
                    System.out.println(j + " type=" + CMAPformats[j] + " length=" + CMAPlength[j] + " lang=" + CMAPlang[j]);
                }
                //flag if present
                if(CMAPformats[j]==6) {
                    hasSix = true;
                }

                if(CMAPformats[j]==0 && CMAPlength[j]==262){
                    readFormatZeroTable(currentFontFile, j);
                }else if(CMAPformats[j]==4){

                    readFormat4Table(j, currentFontFile);

                }else if(CMAPformats[j]==6){
                    readFormat6Table(currentFontFile);

                }else if(CMAPformats[j]==12){

                    readFormat12Table(currentFontFile); 

                }else{
                    //System.out.println("Unsupported Format "+CMAPformats[j]);
                    //reset to avoid setting
                    CMAPformats[j]=-1;

                }
            }
        }
    }
    
    private void readFormat4Table(final int j, final FontFile2 currentFontFile) {
        
        //read values
        CMAPsegCount[j] = currentFontFile.getNextUint16();
        segCount=CMAPsegCount[j]/2;
        CMAPsearchRange[j]=currentFontFile.getNextUint16(); //searchrange
        CMAPentrySelector[j]=currentFontFile.getNextUint16();//entrySelector
        CMAPrangeShift[j]=currentFontFile.getNextUint16();//rangeShift
        
        //check current format 4 is greater than previous or vice versa and act accordingly
        //because some font files have more than one format 4 subtables with different length
        if (hasFormat4) {
            if (CMAPlength[lastFormat4Found] > CMAPlength[j]) {
                CMAPlength[j] = CMAPlength[lastFormat4Found];
                CMAPsegCount[j] = CMAPsegCount[lastFormat4Found];
                CMAPsearchRange[j]=CMAPsearchRange[lastFormat4Found]; //searchrange
                CMAPentrySelector[j]=CMAPentrySelector[lastFormat4Found];//entrySelector
                CMAPrangeShift[j]=CMAPrangeShift[lastFormat4Found];//rangeShift
                return;
            } else if(CMAPlength[lastFormat4Found] < CMAPlength[j]){
                CMAPlength[lastFormat4Found] = CMAPlength[j] ;
                CMAPsegCount[lastFormat4Found] = CMAPsegCount[j] ;
                CMAPsearchRange[lastFormat4Found] = CMAPsearchRange[j]; //searchrange
                CMAPentrySelector[lastFormat4Found] = CMAPentrySelector[j];//entrySelector
                CMAPrangeShift[lastFormat4Found] = CMAPrangeShift[j];//rangeShift
            }
        }
        
        lastFormat4Found = j;
        hasFormat4= true;
        
        //read tables and initialise size of arrays
        endCode = new int[segCount];
        for (int i = 0; i < segCount; i++) {
            endCode[i] = currentFontFile.getNextUint16();
        }
        CMAPreserved[j]=currentFontFile.getNextUint16(); //reserved (should be zero)
        startCode = new int[segCount];
        for (int i = 0; i < segCount; i++) {
            startCode[i] = currentFontFile.getNextUint16();
        }
        idDelta = new int[segCount];
        for (int i = 0; i < segCount; i++) {
            idDelta[i] = currentFontFile.getNextUint16();
        }
        idRangeOffset = new int[segCount];
        for (int i = 0; i < segCount; i++) {
            idRangeOffset[i] = currentFontFile.getNextUint16();
        }
        /**create offsets*/
        offset = new int[segCount];
        int diff,cumulative=0;
        for (int i = 0; i < segCount; i++) {
      
            if(idDelta[i]==0){// && startCode[i]!=endCode[i]){
                offset[i]=cumulative;
                diff=1+endCode[i]-startCode[i];
                
                //fixes bug in mapping theSansOffice tff font
                if(startCode[i]==endCode[i] && idRangeOffset[i]==0) {
                    diff = 0;
                }
                
                cumulative += diff;
            }
        }
        
        // glyphIdArray at end
        final int count = (CMAPlength[j] -16-(segCount*8)) / 2;
        glyphIdArray = new int[count];
        for (int i = 0; i < count; i++){
            glyphIdArray[i] =currentFontFile.getNextUint16();
        }
    }

    private void readFormat6Table(final FontFile2 currentFontFile) {
        hasFormat6 = true;
        firstCode=currentFontFile.getNextUint16();
        entryCount=currentFontFile.getNextUint16();
        
        f6glyphIdArray = new int[firstCode+entryCount];
        for(int jj=0;jj<entryCount;jj++) {
            f6glyphIdArray[jj + firstCode] = currentFontFile.getNextUint16();
        }
    }

    private void readFormat12Table(final FontFile2 currentFontFile) {
        currentFontFile.getNextUint16(); //length //not what it says in spec but what I found in file
        currentFontFile.getNextUint32(); //lang
        
        nGroups=currentFontFile.getNextUint32();
        
        startCharCode=new int[nGroups];
        endCharCode=new int[nGroups];
        startGlyphCode=new int[nGroups];
        
        for(int ii=0;ii<nGroups;ii++){
            
            startCharCode[ii]=currentFontFile.getNextUint32();
            endCharCode[ii]=currentFontFile.getNextUint32();
            startGlyphCode[ii]=currentFontFile.getNextUint32();
        }
    }

    private void readFormatZeroTable(final FontFile2 currentFontFile, int j) {
        hasFormatZero=true;
        
        for(int glyphNum=0;glyphNum<256;glyphNum++){
            
            final int index=currentFontFile.getNextUint8();
            glyphIndexToChar[j][glyphNum]=index;
            glyphToIndex[index]=glyphNum;
            
        }
    }

    public CMAP() {
    }


    /**convert raw glyph number to Character code*/
    public int convertIndexToCharacterCode(final String glyph,int index){

        int index2=-1;
        final int rawIndex=index;
        int format=CMAPformats[formatToUse];

        final boolean debugMapping=false;//(index==223);

        if(debugMapping) {
            System.out.println(glyph + " fontMapping=" + fontMapping + " index=" + index+ ' ' +remapType4);
        }

        /**
         * convert index if needed
         */
        if ((fontMapping == 1 || fontMapping == 2 || fontMapping == 3 || (fontMapping == 4 && remapType4))){//) && (!"notdef".equals(glyph))) {

            if (glyph != null && !"notdef".equals(glyph)) {
                index2 = index;//StandardFonts.lookupCharacterIndex(glyph,StandardFonts.WIN);

                index = StandardFonts.getAdobeMap(glyph);

            } else if (exceptions.containsKey(glyph)) {
                index = (Integer) exceptions.get(glyph);
            }
        }

        int value = -1;

        //exception found in Itext
        if (rawIndex == 128 && endCode != null && "Euro".equals(glyph)) {
            value = getFormat4Value(8364, value);
        } else if (format == 0) { //if no cmap use identity

            //hack
            if(index>255) {
                index = 0;
            }

            value= glyphIndexToChar[formatToUse][index];
            if(value==0 && index2!=-1) {
                value = glyphIndexToChar[formatToUse][index2];
            }

            

        }else if(format==4){

            value = getFormat4Value(index, value);

            //hack for odd value in customer file
            if(value==-1){

                if(index>0xf000) {
                    value = getFormat4Value(index - 0xf000, value);
                } else {
                    value = getFormat4Value(index + 0xf000, value);
                }

            }
           
            //see 18113 fixes ligatures on page
            if(value==-1){
                value = getFormat4Value(rawIndex+ 0xf000, value);
            }
        }else if(format==12){
            value = getFormat12Value(index, debugMapping, value);
        }

        //second attempt if no value found
        if(value==-1 && hasSix){
            index=rawIndex;
            format=6;
        }

        if(format==6){

            if(fontEncoding!=1){
                index=StandardFonts.lookupCharacterIndex(glyph,StandardFonts.MAC);
            }

            if(index>=f6glyphIdArray.length) {
                value = 0;
            } else {
                value = f6glyphIdArray[index];
            }
        }

        if(debugMapping) {
            System.out.println("returns " + value + ' ' + this);
        }

        return value;
    }

    /**
     * lookup tables similar to format 4
     * see https://developer.apple.com/fonts/TTRefMan/RM06/Chap6cmap.html
     */
    private int getFormat12Value(final int index, final boolean debugMapping, int value) {

        /**
         * cycle through tables and then add offset to Glyph start
         */
        for (int i = 0; i < nGroups ; i++) {

            if(debugMapping) {
                System.out.println("table=" + i + " start=" + startCharCode[i] + ' ' + index +
                        " end=" + endCharCode[i] + " glypgStartCode[i]=" + startGlyphCode[i]);
            }

            if (endCharCode[i] >= index && startCharCode[i] <= index){

                value=startGlyphCode[i]+index-startCharCode[i];
                i=nGroups; //exit loop
            }
        }

        return value;
    }

    private int getFormat4Value(final int index,int value) {

        final boolean debugMapping=false;
        for (int i = 0; i < segCount; i++) {

            if(debugMapping) {
                System.out.println("Segtable=" + i + " start=" + startCode[i] + ' ' + index +
                        " end=" + endCode[i] + " idRangeOffset[i]=" + idRangeOffset[i] +
                        " offset[i]=" + offset[i] + " idRangeOffset[i]=" + idRangeOffset[i] + " idDelta[i]=" + idDelta[i]);
            }

            if (endCode[i] >= index && startCode[i] <= index){

                final int idx ;
                if (idRangeOffset[i] == 0) {

                    if(debugMapping) {
                        System.out.println("xxx=" + (idDelta[i] + index));
                    }

                    value= (idDelta[i] + index) % 65536;

                    i=segCount;
                }else{

                    idx= offset[i]+(index - startCode[i]);
                    if (idx < glyphIdArray.length) {
                        value=glyphIdArray[idx];
                    }
                    
                    if(debugMapping) {
                        System.out.println("value=" + value + " idx=" +
                                idx + " glyphIdArrays=" + glyphIdArray[0] + ' ' +
                                glyphIdArray[1] + ' ' + glyphIdArray[2] + " offset[i]=" + offset[i] +
                                " index=" + index + " startCode[" + i + "]=" + startCode[i] + " i=" + i);
                    }

                    i=segCount;

                }
            }
        }

        return value;
    }

    /**
     * work out correct CMAP table to use.
     */
    public void setEncodingToUse(final boolean hasEncoding, final int fontEncoding, final boolean isCID) {

        final boolean encodingDebug=false;
       
        this.fontEncoding=fontEncoding;
        
        if(encodingDebug) {
            System.out.println(this + "hasEncoding=" + hasEncoding + " fontEncoding=" + fontEncoding + " isCID=" + isCID);
        }

        formatToUse=-1;

        final int count=platformID.length;

        /**case 1 */
        for(int i=0;i<count;i++){
            
            if((platformID[i]==3)&&(CMAPformats[i]==1 || CMAPformats[i]==0)){
                formatToUse=i;
                this.fontMapping=1;
                i=count;
                
                if(encodingDebug) {
                    System.out.println("case1");
                }
            }
        }

        /**case 2*/
        boolean wasCase2=false;
        if(formatToUse==-1 && hasFormatZero && !isCID){
            
            for(int i=0;i<count;i++){
                if(platformID[i]==1 && CMAPformats[i]==0){
                    formatToUse=i;
                    
                    wasCase2=!(glyphIndexToChar[formatToUse][223]!=0 && getFormat4Value(223, 0)==0);
                    
                    if(hasEncoding) {
                        fontMapping = 2;
                        StandardFonts.checkLoaded(StandardFonts.MAC);
                    }else if(!wasCase2 && platformSpecificID[formatToUse]==0 && platformID[formatToUse]==1) {
                        fontMapping = 3;
                        StandardFonts.checkLoaded(StandardFonts.WIN);

                    } else {
                        fontMapping =-1;
                    }
                    
                    i=count;
                    
                    if(encodingDebug) {
                            System.out.println("case2 fontMapping=" + fontMapping + " formatToUse=" + formatToUse+ ' ' +platformSpecificID[formatToUse]+ ' ' + platformID[formatToUse]+ ' ' +hasEncoding+ ' ' +fontEncoding+ ' ' +wasCase2);
                    }
                }
            }          
        }
        
        /**case 4 - no simple maps or prefer to last 1*/
        /**last check uses fl glyph and sticks to case 1 if found*/
        if(formatToUse==-1 || fontMapping==3 || wasCase2){
            //if((formatToUse==-1)){
            for(int i=0;i<count;i++){
                if((CMAPformats[i]==4)){
                    formatToUse=i;
                    fontMapping=4;
                    
                    i=count;
                    
                    if(encodingDebug) {
                        System.out.println("case4 fontMapping=" + fontMapping + " formatToUse=" + formatToUse+ ' ' +platformSpecificID[formatToUse]+ ' ' + platformID[formatToUse]+ ' ' +hasEncoding+ ' ' +fontEncoding);
                    }
                    
                    if(platformSpecificID[formatToUse]==3 && platformID[formatToUse]==0 && (hasEncoding || fontEncoding==StandardFonts.STD)){
                        remapType4=true;
                      // System.out.println("a "+wasCase2);
                    }else if(platformSpecificID[formatToUse]==1 && platformID[formatToUse]==0 && hasEncoding && fontEncoding==StandardFonts.WIN){
                        remapType4=true;  
                      //  System.out.println("b");
                    }else if(platformSpecificID[formatToUse]==0 && platformID[formatToUse]==0 && hasEncoding && fontEncoding==StandardFonts.WIN){
                        remapType4=true;     
                      //  System.out.println("c");
                    }else if(platformSpecificID[formatToUse]==1 && platformID[formatToUse]==3 && hasEncoding && fontEncoding==StandardFonts.WIN && 
                            (wasCase2 || getFormat4Value(223, 0)==0)){
                        remapType4=true;     
                      // System.out.println("d "+wasCase2+ " "+isCase2+" "+(glyphIndexToChar[formatToUse][223]+" "+getFormat4Value(223, 0)));
                    }else if(platformSpecificID[formatToUse]==1 && platformID[formatToUse]==3 && hasEncoding && (fontEncoding==StandardFonts.MAC || (count==1 && fontEncoding==StandardFonts.WIN))){
                        remapType4=true;   
                      //  System.out.println("e");
                    }else if(!hasEncoding && fontEncoding!=1){
                        remapType4=true;
                      //  System.out.println("g");
                    }
                }
            }   
        }

        
        /**case 3 - no MAC cmap in other ranges and substituting font */
        if(formatToUse==-1){
            for(int i=0;i<count;i++){
                if((CMAPformats[i]==6)){
                    formatToUse=i;
                    if(!hasEncoding){
                        fontMapping=2;
                        StandardFonts.checkLoaded(StandardFonts.MAC);
                    }else {
                        fontMapping = 6;
                    }
                    
                    i=count;
                    
                    if(encodingDebug) {
                        System.out.println("case3 fontMapping=" + fontMapping + " formatToUse=" + formatToUse+ ' ' +platformID[formatToUse]);
                    }
                }
            }
        }

        
        /**case 5 - type12*/
        if(formatToUse==-1){
            for(int i=0;i<count;i++){
                if((CMAPformats[i]==12)){
                    formatToUse=i;
                    if(!hasEncoding){
                        fontMapping=2;
                        StandardFonts.checkLoaded(StandardFonts.MAC);
                    }else {
                        fontMapping = 12;
                    }
                    
                    i=count;
                    
                    if(encodingDebug) {
                        System.out.println("case5");
                    }
                }
            }      
        }

        if(fontEncoding==StandardFonts.ZAPF){
            fontMapping=2;
            
            StandardFonts.checkLoaded(StandardFonts.MAC);


            if(encodingDebug) {
                System.out.println("Zapf");
            }
        }
    }

    /**turn type 0 table into a list of glyph*/
    public Map buildCharStringTable() {

        final Map glyfValues=new HashMap();
//      for(int i : glyphToIndex){

//      if(i>0){
//          glyfValues.put(glyphToIndex[i],i);
//      //System.out.println("i=" + i + " " + StandardFonts.getUnicodeChar(encodingToUse, i));
//      }
//  }        
        if(hasFormat4){
            final ArrayList<Integer>list4 = new ArrayList<Integer>();
            for(int z=0;z<segCount;z++){
                final int total  = endCode[z] - startCode[z] +1;
                for(int q=0 ;q<total;q++){
                    list4.add(startCode[z]+q);
                }
            }
            for(final Integer i: list4){
                glyfValues.put(i,getFormat4Value(i, 0));
            }
        }
        else if(hasFormat6){
            for(int z=0;z<entryCount;z++){
                //System.out.println(firstCode+z+" ==> "+f6glyphIdArray[firstCode+z]);
                glyfValues.put(firstCode+z,f6glyphIdArray[firstCode+z]);
            }
        }
        else{
            for(int z=0;z<glyphToIndex.length;z++){
                if(glyphToIndex[z]>0){
                    glyfValues.put(glyphToIndex[z],z);
                }
            }
        }

        return glyfValues;
    }
}
