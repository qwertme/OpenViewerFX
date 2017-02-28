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
 * FontFile2.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import org.jpedal.fonts.objects.FontData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author markee
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
@SuppressWarnings("UnusedDeclaration")
public class FontFile2 implements Serializable{
    
    private static final long serialVersionUID = -3097990864237320960L;
    
    public static final int HEAD=0;
    public static final int MAXP =1;
    public static final int CMAP=2;
    public static final int LOCA=3;
    public static final int GLYF=4;
    public static final int HHEA=5;
    public static final int HMTX=6;
    public static final int NAME=7;
    public static final int POST=8;
    public static final int CVT=9;
    public static final int FPGM=10;
    public static final int HDMX=11;
    public static final int KERN=12;
    public static final int OS2=13;
    public static final int PREP=14;
    public static final int DSIG=15;
    
    public static final int CFF=16;
    public static final int GSUB=17;
    public static final int BASE=18;
    public static final int EBDT=19;
    public static final int EBLC=20;
    public static final int GASP=21;
    public static final int VHEA=22;
    public static final int VMTX=23;
    public static final int GDEF=24;
    public static final int JSTF=25;
    public static final int LTSH=26;
    public static final int PCLT=27;
    public static final int VDMX=28;
    public static final int BSLN=29;
    public static final int MORT=30;
    public static final int FDSC=31;
    public static final int FFTM=32;
    public static final int GPOS=33;
    public static final int FEAT=34;
    public static final int JUST=35;
    public static final int PROP=36;
    public static final int LCCL=37;
    public static final int Zapf=38;
    
    protected static final int tableCount=39;
    
    //location of tables
    protected int checksums[][];
    protected int tables[][];
    protected int tableLength[][];
    
    /**holds embedded font*/
    private FontData fontDataAsObject;
    
    private byte[] fontDataAsArray;
    
    private boolean useArray=true;
    
    protected ArrayList tableList=new ArrayList(32);
    
    /**current location in fontDataArray*/
    private int pointer;
    
    public static final int OPENTYPE = 1;
    public static final int TRUETYPE = 2;
    public static final int TTC = 3;
    
    /**subtypes used in conversion*/
    public static final int PS=10;
    public static final int TTF=11;
    protected int subType=PS;
    
    protected int type=TRUETYPE;
    
    //if several fonts, selects which font
    public int currentFontID;
    private int fontCount=1;
    
    //defaults are for OTF write
    protected int numTables=11,searchRange=128,entrySelector=3,rangeShift=48;
    
    public FontFile2(final FontData data){
        
        useArray=false;
        
        this.fontDataAsObject=data;
        
        readHeader();
    }
    
    public FontFile2(final byte[] data){
        
        useArray=true;
        
        this.fontDataAsArray=data;
        
        readHeader();
    }
    
    public FontFile2(final byte[] data, final boolean ignoreHeaders){
        
        useArray=true;
        
        this.fontDataAsArray=data;
        
        if(!ignoreHeaders) {
            readHeader();
        }
    }
    
    public FontFile2() {
    }
    
    
    /**
     * set selected font as a number in TTC
     * ie if 4 fonts, use 0,1,2,3
     * if less than fontCount. Otherwise does
     * nothing
     */
    public void setSelectedFontIndex(final int currentFontID) {
        
        if(currentFontID<fontCount) {
            this.currentFontID = currentFontID;
        }
    }
    
    /**read the table offsets*/
    private void readHeader(){
        
        /**code to read the data at start of file*/
        //scalertype
        final int scalerType=getNextUint32();
        
        if(scalerType==1330926671)//starts OTTF
        {
            type = OPENTYPE;
        } else if(scalerType==1953784678)//ttc
        {
            type = TTC;
        }
        
        if(type==TTC){
            
            getNextUint32(); //version
            fontCount=getNextUint32();
            
            //location of tables
            checksums=new int[tableCount][fontCount];
            tables=new int[tableCount][fontCount];
            tableLength =new int[tableCount][fontCount];
            
            final int[] fontOffsets=new int[fontCount];
            
            for(int currentFont=0;currentFont<fontCount;currentFont++){
                
                currentFontID=currentFont;
                
                final int fontStart=getNextUint32();
                fontOffsets[currentFont]=fontStart;
            }
            
            for(int currentFont=0;currentFont<fontCount;currentFont++){
                
                currentFontID=currentFont; //choose this font
                
                this.pointer = fontOffsets[currentFont];
                
                getNextUint32(); //scalerType
                
                readTablesForFont();
            }
            
            //back to default
            currentFontID=0;
            
            
        }else{  //otf or ttf
            
            //location of tables
            checksums=new int[tableCount][1];
            tables=new int[tableCount][1];
            tableLength =new int[tableCount][1];
            
            readTablesForFont();
        }
    }
    
    private void readTablesForFont() {
        
        numTables=getNextUint16();   //tables in the file
        searchRange=getNextUint16(); //searchRange
        entrySelector=getNextUint16(); //entrySelector
        rangeShift=getNextUint16(); //rangeShift
        
        String tag;
        int checksum,offset,length,id;
        
        for(int l=0;l<numTables;l++){
            //read table
            tag=getNextUint32AsTag();
            checksum=getNextUint32(); //checksum
            offset=getNextUint32();
            length=getNextUint32();
            
            tableList.add(tag);
            
            id = getTableID(tag);
            
            if(id!=-1 && length != 0){
                checksums[id][currentFontID]=checksum;
                tables[id][currentFontID]=offset;
                tableLength[id][currentFontID]=length;
            }
        }
    }
    
    protected static int getTableID(final String tag) {
        
        int id=-1;
        
        if(tag.equals("maxp")) {
            id = MAXP;
        } else if(tag.equals("head")) {
            id = HEAD;
        } else if(tag.equals("cmap")) {
            id = CMAP;
        } else if(tag.equals("loca")){
            id=LOCA;
        }else if(tag.equals("glyf")){
            id=GLYF;
        }else if(tag.equals("hhea")){
            id=HHEA;
        }else if(tag.equals("hmtx")){
            id=HMTX;
        }else if(tag.equals("name")){
            id=NAME;
        }else if(tag.equals("post")){
            id=POST;
        }else if(tag.equals("cvt ")){
            id=CVT;
        }else if(tag.equals("fpgm")){
            id=FPGM;
        }else if(tag.equals("hdmx")){
            id=HDMX;
        }else if(tag.equals("kern")){
            id=KERN;
        }else if(tag.equals("OS/2")){
            id=OS2;
        }else if(tag.equals("prep")){
            id=PREP;
        }else if(tag.equals("DSIG")){
            id=DSIG;
        }else if(tag.equals("BASE")){
            id=BASE;
        }else if(tag.equals("CFF ")){
            id=CFF;
        }else if(tag.equals("GSUB")){
            id=GSUB;
        }else if(tag.equals("EBDT")){
            id=EBDT;
        }else if(tag.equals("EBLC")){
            id=EBLC;
        }else if(tag.equals("gasp")){
            id=GASP;
        }else if(tag.equals("vhea")){
            id=VHEA;
        }else if(tag.equals("vmtx")){
            id=VMTX;
        }else if(tag.equals("GDEF")){
            id=GDEF;
        }else if(tag.equals("JSTF")){
            id=JSTF;
        }else if(tag.equals("LTSH")){
            id=LTSH;
        }else if(tag.equals("PCLT")){
            id=PCLT;
        }else if(tag.equals("VDMX")){
            id=VDMX;
        }else if(tag.equals("mort")){
            id=MORT;
        }else if(tag.equals("bsln")){
            id=BSLN;
        }else if(tag.equals("fdsc")){
            id=FDSC;
        }else if(tag.equals("FFTM")){
            id=FFTM;
        }else if(tag.equals("GPOS")){
            id=GPOS;
        }else if(tag.equals("feat")){
            id=FEAT;
        }else if(tag.equals("just")){
            id=JUST;
        }else if(tag.equals("prop")){
            id=PROP;
        }else if(tag.equals("LCCL")){
            id=LCCL;
        }else if(tag.equals("Zapf")){
            id=Zapf;
        }else{
            //System.out.println("No tag for "+tag);
        }
        return id;
    }
    
    /**choose a table and move to start.
     * Return 0 if not present*/
    public int selectTable(final int tableID){
        pointer=tables[tableID][currentFontID];
        
        return pointer;
    }
    
    /**get table size*/
    public int getTableSize(final int tableID){
        
        return tableLength[tableID][currentFontID];
    }
    
    /**get table size*/
    public int getTableStart(final int tableID){
        
        return tables[tableID][currentFontID];
    }
    
    /**return a uint32*/
    public final int getNextUint32(){
        
        int returnValue=0,nextValue;
        
        for(int i=0;i<4;i++){
            
            if(useArray){
                if(pointer<fontDataAsArray.length) {
                    nextValue = fontDataAsArray[pointer] & 255;
                } else {
                    nextValue = 0;
                }
            }else {
                nextValue = fontDataAsObject.getByte(pointer) & 255;
            }
            
            returnValue += ((nextValue<<(8*(3-i))));
            
            pointer++;
        }
        
        return returnValue;
    }
    
    /**return a uint64*/
    @SuppressWarnings({"UnusedReturnValue"})
    public final int getNextUint64(){
        
        int returnValue=0,nextValue;
        
        for(int i=0;i<8;i++){
            
            if(useArray) {
                nextValue = fontDataAsArray[pointer];
            } else {
                nextValue = fontDataAsObject.getByte(pointer);
            }
            
            if(nextValue<0) {
                nextValue = 256 + nextValue;
            }
            
            returnValue += (nextValue<<(8*(7-i)));
            
            pointer++;
        }
        
        return returnValue;
    }
    
    /**set pointer to location in font file*/
    public final void setPointer(final int p){
        pointer=p;
    }
    
    /**get length of table*/
    public final int getOffset(final int tableID){
        return tableLength[tableID][currentFontID];
    }
    
    /**get start of table*/
    public final int getTable(final int tableID){
        return tables[tableID][currentFontID];
    }
    
    /**get pointer to location in font file*/
    public final int getPointer(){
        return pointer;
    }
    
    /**return a uint32*/
    public final String getNextUint32AsTag(){
        
        final StringBuilder returnValue=new StringBuilder();
        
        char c;
        
        for(int i=0;i<4;i++){
            
            if(useArray) {
                c = (char) fontDataAsArray[pointer];
            } else {
                c = (char) fontDataAsObject.getByte(pointer);
            }
            
            returnValue.append(c);
            
            pointer++;
        }
        
        return returnValue.toString();
    }
    
    /**return a uint16*/
    public final int getNextUint16(){
        
        int returnValue=0,nextValue;
        
        for(int i=0;i<2;i++){
            
            if(useArray) {
                nextValue = fontDataAsArray[pointer] & 255;
            } else {
                nextValue = fontDataAsObject.getByte(pointer) & 255;
            }
            
            returnValue += (nextValue<<(8*(1-i)));
            
            pointer++;
        }
        
        return returnValue;
    }
    
    /**return a short*/
    public final short getShort(){
        
        int returnValue=0,nextValue;
        
        for(int i=0;i<2;i++){
            
            if(useArray) {
                nextValue = fontDataAsArray[pointer];
            } else {
                nextValue = fontDataAsObject.getByte(pointer);
            }
            
            returnValue += (nextValue<<(8*(1-i)));
            
            pointer++;
        }
        
        return (short) returnValue;
    }
    /**return a uint8*/
    public final int getNextUint8(){
        
        final int nextValue;
        
        if(useArray) {
            nextValue = fontDataAsArray[pointer] & 255;
        } else {
            nextValue = fontDataAsObject.getByte(pointer) & 255;
        }
        
        
        //if(returnValue<0)
        //	returnValue=256+returnValue;
        
        pointer++;
        
        return nextValue;
    }
    
    /**return a uint8*/
    public final int getNextint8(){
        
        final int nextValue;
        
        if(useArray) {
            nextValue = fontDataAsArray[pointer];
        } else {
            nextValue = fontDataAsObject.getByte(pointer);
        }
        
        
        //if(returnValue<0)
        //	returnValue=256+returnValue;
        
        pointer++;
        
        return nextValue;
    }
    
    /**
     * move forward a certain amount relative
     */
    public void skip(final int i) {
        pointer += i;
        
    }
    
    /**
     * return a short
     */
    public short getFWord() {
        int returnValue=0,nextValue;
        
        for(int i=0;i<2;i++){
            
            if(useArray) {
                nextValue = fontDataAsArray[pointer] & 255;
            } else {
                nextValue = fontDataAsObject.getByte(pointer) & 255;
            }
            
            returnValue += (nextValue<<(8*(1-i)));
            
            pointer++;
        }
        
        return (short) returnValue;
    }
    
    /**
     */
    public short getNextInt16() {
        int returnValue=0,nextValue;
        
        for(int i=0;i<2;i++){
            
            if(useArray) {
                nextValue = fontDataAsArray[pointer] & 255;
            } else {
                nextValue = fontDataAsObject.getByte(pointer) & 255;
            }
            
            returnValue += (nextValue<<(8*(1-i)));
            
            pointer++;
        }
        
        return (short) returnValue;
    }
    
    
    /**
     */
    public short getNextSignedInt16() {
        int returnValue=0,nextValue;
        
        for(int i=0;i<2;i++){
            
            if(useArray) {
                nextValue = fontDataAsArray[pointer] & 255;
            } else {
                nextValue = fontDataAsObject.getByte(pointer) & 255;
            }
            
            returnValue += (nextValue<<(8*(1-i)));
            pointer++;
        }
        
        return (short) (returnValue);
    }
    
    /**
     */
    @SuppressWarnings({"UnusedReturnValue"})
    public short readUFWord() {
        int returnValue=0,nextValue;
        
        for(int i=0;i<2;i++){
            
            if(useArray) {
                nextValue = fontDataAsArray[pointer] & 255;
            } else {
                nextValue = fontDataAsObject.getByte(pointer) & 255;
            }
            
            returnValue += (nextValue<<(8*(1-i)));
            
            pointer++;
        }
        
        return (short) returnValue;
    }
    
    /**
     * get 16 bit signed fixed point
     */
    public float getFixed() {
        
        int number;
        final int dec;

        if(useArray) {
            number = ((fontDataAsArray[pointer] & 0xff) * 256) + (fontDataAsArray[pointer + 1] & 0xff);
        } else {
            number = ((fontDataAsObject.getByte(pointer) & 0xff) * 256) + (fontDataAsObject.getByte(pointer + 1) & 0xff);
        }
        
        
        if(number>32768) {
            number -= 65536;
        }
        
        pointer += 2;
        
        if(useArray) {
            dec = ((fontDataAsArray[pointer] & 0xff) * 256) + (fontDataAsArray[pointer + 1] & 0xff);
        } else {
            dec = ((fontDataAsObject.getByte(pointer) & 0xff) * 256) + (fontDataAsObject.getByte(pointer + 1) & 0xff);
        }
        
        
        pointer += 2;
        
        return (number+(dec/65536f));
    }
    
    /**
     * get a pascal string
     */
    public String getString() {
        final int length;
        
        //catch bug in odd file
        if(useArray && pointer==fontDataAsArray.length) {
            return "";
        }
        
        if(useArray) {
            length = fontDataAsArray[pointer] & 255;
        } else {
            length = fontDataAsObject.getByte(pointer) & 255;
        }
        
        final char[] chars=new char[length];
        //StringBuilder value=new StringBuilder();
        //value.setLength(length);
        
        
        pointer++;
        
        for(int i=0;i<length;i++){
            final int nextChar;
            
            if(useArray) {
                nextChar = fontDataAsArray[pointer] & 255;
            } else {
                nextChar = fontDataAsObject.getByte(pointer) & 255;
            }
            
            pointer++;
            
            //value.append((char)nextChar);
            //value.setCharAt(i,(char)nextChar);
            chars[i]=(char)nextChar;
            //allow for error
            if(useArray && pointer>=fontDataAsArray.length) {
                i = length;
            }
            
        }
        return String.copyValueOf(chars);
        //return value.toString();
    }
    
    /**
     * get a pascal string
     */
    public byte[] getStringBytes() {
        final int length;
        
        //catch bug in odd file
        if(useArray && pointer==fontDataAsArray.length) {
            return new byte[1];
        }
        
        if(useArray) {
            length = fontDataAsArray[pointer] & 255;
        } else {
            length = fontDataAsObject.getByte(pointer) & 255;
        }
        
        final byte[] value=new byte[length];
        
        pointer++;
        
        for(int i=0;i<length;i++){
            final byte nextChar;
            
            if(useArray) {
                nextChar = fontDataAsArray[pointer];
            } else {
                nextChar = fontDataAsObject.getByte(pointer);
            }
            
            pointer++;
            
            //value.append((char)nextChar);
            value[i]=nextChar;
            
            //allow for error
            if(useArray && pointer>=fontDataAsArray.length) {
                i = length;
            }
            
        }
        return value;
    }
    
    public float getF2Dot14() {
        
        final int firstValue;
        
        if(useArray) {
            firstValue = ((fontDataAsArray[pointer] & 0xff) << 8) + (fontDataAsArray[pointer + 1] & 0xff);
        } else {
            firstValue = ((fontDataAsObject.getByte(pointer) & 0xff) << 8) + (fontDataAsObject.getByte(pointer + 1) & 0xff);
        }
        
        pointer += 2;
        
        if(firstValue==49152){
            return -1.0f;
        }else if(firstValue==16384){
            return 1.0f;
        }else{
            return (firstValue - (2 * (firstValue & 32768))) / 16384f;
        }
    }
    
    public byte[] readBytes(final int startPointer, final int length) {
        
        if(useArray){
            final byte[] block=new byte[length];
            System.arraycopy(fontDataAsArray,startPointer,block,0,length);
            return block;
        }else {
            return fontDataAsObject.getBytes(startPointer, length);
        }
        
    }
    
    public byte[] getTableBytes(final int tableID){
        
        final int startPointer=tables[tableID][currentFontID];
        int length= tableLength[tableID][currentFontID];
        
        if(useArray){
            if (fontDataAsArray.length - startPointer < length) {
                length = fontDataAsArray.length - startPointer;
            }
            
            final byte[] block=new byte[length];
            System.arraycopy(fontDataAsArray,startPointer,block,0,length);
            return block;
        }else {
            return fontDataAsObject.getBytes(startPointer, length);
        }
        
    }
    
    //number of fonts - 1 for Open/True, can be more for TTC
    public int getFontCount() {
        return fontCount;
    }
    
    /**
     * used to test if table too short so need to stop reading
     * @return
     */
    public boolean hasValuesLeft() {
        
        final int size;
        if(useArray) {
            size = fontDataAsArray.length;
        } else {
            size = fontDataAsObject.length();
        }
        
        return pointer<size;
        
    }
    
    /**
     * used to see how many bytes left to avoid exception if figure wrong
     * @return
     */
    public int getBytesLeft() {
        
        final int size;
        if(useArray) {
            size = fontDataAsArray.length;
        } else {
            size = fontDataAsObject.length();
        }
        
        return size-pointer;
        
    }
    
    public void setFontDataAsArray(final byte[]data){
        this.pointer = 0;
        this.fontDataAsArray = data;
    }
}
