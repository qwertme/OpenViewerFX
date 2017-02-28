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
 * Type1.java
 * ---------------
 */
package org.jpedal.fonts;

import java.io.BufferedReader;
import org.jpedal.utils.repositories.FastByteArrayOutputStream;

import java.io.StringReader;
import java.util.StringTokenizer;

import org.jpedal.utils.LogWriter;

/**
 * handles type1 specifics
 */
public class Type1 extends PdfFont {
    
    protected boolean isCID;
    
    /** constant used in eexec and charset decode */
    private static final int c1 = 52845;
    
    /** constant used in eexec and charset decode */
    private static final int c2 = 22719;
    
    //number of random bytes in stream to ignore
    private int skipBytes=4;
    
    //Private DICT values
    protected int[] blueValues;
    protected int[] otherBlues;
    protected int[] familyBlues;
    protected int[] familyOtherBlues;
    protected Double blueScale;
    protected Integer blueShift;
    protected Integer blueFuzz;
    protected Double stdHW;
    protected Double stdVW;
    protected int[] stemSnapH;
    protected int[] stemSnapV;
    protected Boolean forceBold;
    protected Integer languageGroup;
    
    
    /**lookup for 1 byte draw commands*/
    public static final String T1CcharCodes1Byte[]={"-Reserved-","hstem","-Reserved-","vstem",
        "vmoveto","rlineto","hlineto","vlineto",
        "rrcurveto","closePathT1","callsubr","return",
        "escape","hsbwT1","endchar","-Reserved-",
        "blend","-Reserved-","hstemhm","hintmask",
        "cntrmask","rmoveto","hmoveto","vstemhm",
        "rcurveline","rlinecurve","vvcurveto","hhcurveto",
        "intint","callgsubr","vhcurveto","hvcurveto"};
    
    /**lookup for 2 byte draw commands*/
    public static final String T1C[]={"dotSection","vstem3","hstem3","and",
        "or","not","seacT1","swbT1",
        "store","abs","add","sub",
        "div","load","neg","eq",
        "callothersubT1","pop","drop","-Reserved-",
        "put","get","ifelse","random",
        "mul","-Reserved-","sqrt","dup",
        "exch","index","roll","-Reserved-",
        "-Reserved-","setcurrentpointT1","hflex","flex",
        "hflex1","flex1"};
    
    //flag if we store reverse map for index to CMAP (use dbt OTF conversion)
    boolean trackIndices;
    
    public static final char[] DEF_CHARS = "def".toCharArray();
    
    /*
    * creates glyph from truetype font commands
    *
    //protected T1Glyph getType1Glyph(T1Glyph current_path,int rawInt, String displayValue, float currentWidth) {
    protected T1Glyph getType1Glyph(double x,double y,T1Glyph current_path,String glyph,int rawInt, String displayValue, float currentWidth,boolean isRecursive) {
    
    
    boolean test=false;
    byte[] glyphStream=(byte[]) charStrings.get(glyph);
    
    System.out.println(glyph+" "+glyphStream.length+" "+charStrings);
    
    
    for(int i=0;i<glyphStream.length;i++)
    System.out.print((char) glyphStream[i]);
    
    if(glyphStream!=null){
    boolean isFirst=true; //flag to pick up extra possible first value
    int hintCount=0;
    
    //String testChar="xx:";//"x�";
    
    //if((displayValue.equals(testChar)))
    //	test=true;
    //
    int p = 0, nextVal,key=0,lastKey,dicEnd=glyphStream.length,opCount=0,currentOp;
    double[] op = new double[dicEnd]; //current operand in dictionary as max size (just in case)
    float[] pt=new float[6];
    
    if((test)){
    System.out.println("******************"+fontID+" "+displayValue);
    
    float h=(float)(FontBBox[3]);
    current_path.moveTo((float)FontBBox[0],(float)FontBBox[1]);
    current_path.lineTo((float)FontBBox[0],h);
    current_path.lineTo((float)FontBBox[2],h);
    current_path.lineTo((float)FontBBox[2],(float)FontBBox[1]);
    current_path.lineTo((float)FontBBox[0],(float)FontBBox[1]);
    for(int j=p;j<dicEnd;j++){
    int i=glyphStream[j] & 0xff;
    if(i<charCodes1Byte.length)
    System.out.println(" "+i+" "+charCodes1Byte[i]);
    else
    System.out.println(" "+i+" "+glyphStream[j]);
    
    }
    System.out.println("===");
    }
    
    //current_path.moveTo(0,0);
    
    while (p < dicEnd) {
    
    nextVal = glyphStream[p] & 0xFF;
    
    if (nextVal >31 | (nextVal==28)) {  //number,SID or array
    //vector used so several values can be returned
    Vector v = getInt(glyphStream, p);
    
    op[opCount] = ((Double) v.elementAt(0)).doubleValue();
    p = ((Integer) v.elementAt(1)).intValue();
    opCount++;
    
    }else{  // operator
    
    lastKey=key;
    key = nextVal;
    p++;
    currentOp=0;
    if(test){
    for(int j=0;j<opCount;j++)
    System.out.print(" "+op[j]);
    System.out.print(" "+isFirst+" \n");
    }
    
    if (key ==12) { //handle escaped keys
    key= glyphStream[p] & 0xFF;
    p++;
    if(key==34){ //hflex
    
    //first curve
    x += op[0];
    pt[0]=(float) x;
    pt[1]=(float) y;
    x += op[1];
    y += op[2];
    pt[2]=(float) x;
    pt[3]=(float) y;
    x += op[3];
    pt[4]=(float) x;
    pt[5]=(float) y;
    current_path.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
    
    //second curve
    x += op[4];
    pt[0]=(float) x;
    pt[1]=(float) y;
    x += op[5];
    pt[2]=(float) x;
    pt[3]=(float) y;
    x += op[6];
    pt[4]=(float) x;
    pt[5]=(float) y;
    current_path.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
    }else if(key==35){ //flex
    
    for(int curves=0;curves<12;curves=curves+6){
    for(int points=0;points<6;points=points+2){
    x += op[curves+points];
    y += op[curves+points+1];
    pt[points]=(float) x;
    pt[points+1]=(float) y;
    }
    current_path.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
    }
    }else if(key==36){ //hflex1
    //first curve
    x+=op[0];
    y+=op[1];
    pt[0]=(float) x;
    pt[1]=(float) y;
    x+=op[2];
    y+=op[3];
    pt[2]=(float) x;
    pt[3]=(float) y;
    x+=op[4];
    pt[4]=(float) x;
    pt[5]=(float) y;
    current_path.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
    
    //second curve
    x+=op[5];
    pt[0]=(float) x;
    pt[1]=(float) y;
    x+=op[6];
    y+=op[7];
    pt[2]=(float) x;
    pt[3]=(float) y;
    x += op[8];
    pt[4]=(float) x;
    pt[5]=(float) y;
    current_path.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
    }else if(key==37){ //flex1
    double   dx = 0, dy = 0,x1=x, y1=y;
    
    //workout dx/dy/horizontal and reset flag
    for ( int count =0; count <10; count=count+2 ){
    dx += op[count];
    dy += op[count+1];
    }
    boolean isHorizontal=(Math.abs(dx)>Math.abs(dy));
    
    for(int points=0;points<6;points=points+2){//first curve
    x += op[points];
    y += op[points+1];
    pt[points]=(float) x;
    pt[points+1]=(float) y;
    }
    current_path.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
    
    for(int points=0;points<4;points=points+2){//second curve
    x += op[points+6];
    y += op[points+7];
    pt[points]=(float) x;
    pt[points+1]=(float) y;
    }
    
    // last point
    if ( isHorizontal ){
    x += op[10];
    y  = y1;
    }else{
    x  = x1;
    y += op[10];
    }
    pt[4]=(float) x;
    pt[5]=(float) y;
    current_path.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
    
    }else  if(test){
    System.out.println(p+" "+key+" "+charCodes2Byte[key]+" <2<<"+op);
    }
    
    } else {
    if(test)
    System.out.println(charCodes1Byte[key]+" "+key);
    if(key==0){ //reserved
    }else if((key==1)|(key==3)|(key==18)|(key==23)){ //hstem vstem hstemhm vstemhm
    hintCount+=opCount/2;
    }else if(key==4){ //vmoveto
    if((isFirst)&&(opCount==2))
    currentOp++;
    y=y+op[currentOp];
    current_path.moveTo((float)x,(float)y);
    }else if((key==5)){//rlineto
    int lineCount=opCount/2;
    while ( lineCount > 0 ){
    x += op[currentOp];
    y += op[currentOp+1];
    current_path.lineTo((float)x,(float)y);
    currentOp += 2;
    lineCount--;
    }
    }else if((key==6)|(key==7)){//hlineto or vlineto
    boolean isHor = ( key==6 );
    int start=0;
    while (start<opCount ){
    if ( isHor )
    x += op[start];
    else
    y += op[start];
    
    current_path.lineTo((float)x,(float)y);
    
    start++;
    isHor =!isHor;
    }
    }else if(key==8){//rrcurveto
    int  curveCount = ( opCount  ) / 6;
    while ( curveCount > 0 ){
    float[] coords=new float[6];
    x += op[currentOp];
    y += op[currentOp+1];
    coords[0]=(float) x;
    coords[1]=(float) y;
    
    x += op[currentOp+2];
    y += op[currentOp+3];
    coords[2]=(float) x;
    coords[3]=(float) y;
    
    x += op[currentOp+4];
    y += op[currentOp+5];
    coords[4]=(float) x;
    coords[5]=(float) y;
    
    current_path.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
    
    currentOp += 6;
    curveCount--;
    }
    }else if(key==10){ //callsubr
    System.out.println("callsubr");
    }else if(key==11){ //return
    }else if(key==14){ //endchar
    
    if(opCount==5){ //allow for width and 4 chars
    opCount--;
    currentOp++;
    }
    if(opCount==4){
    StandardFonts.checkLoaded(StandardFonts.STD);
    float adx=(float)(x+op[currentOp]);
    float ady=(float)(y+op[currentOp+1]);
    String bchar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)op[currentOp+2]);
    String achar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)op[currentOp+3]);
    
    current_path=getType1CGlyph(0,0,current_path,bchar,rawInt, "", 0,true);
    current_path.closePath();
    current_path.moveTo(adx,ady);
    current_path=(getType1CGlyph(adx,ady,current_path,achar,rawInt, "", 0,true));
    
    
    }else
    current_path.closePath();
    p =dicEnd;
    }else if(key==16){ //blend
    
    }else if((key==19)|(key==20)){ //hintmask //cntrmask
    
    if((lastKey==18)|(lastKey==1))
    hintCount+=opCount/2;
    
    int count=hintCount;
    while(count>0){
    p++;
    count=count-8;
    }
    }else if(key==21){//rmoveto
    if((isFirst)&&(opCount==3))
    currentOp++;
    x=x+op[currentOp];
    y=y+op[currentOp+1];
    current_path.moveTo((float)x,(float)y);
    }else if(key==22){ //hmoveto
    if((isFirst)&&(opCount==2))
    currentOp++;
    x=x+op[currentOp];
    current_path.moveTo((float)x,(float)y);
    }else if(key==24){ //rcurveline
    //curves
    int  curveCount=( opCount - 2 ) / 6;
    while ( curveCount > 0 ){
    float[] coords=new float[6];
    x += op[currentOp];
    y += op[currentOp+1];
    coords[0]=(float) x;
    coords[1]=(float) y;
    
    x += op[currentOp+2];
    y += op[currentOp+3];
    coords[2]=(float) x;
    coords[3]=(float) y;
    
    x += op[currentOp+4];
    y += op[currentOp+5];
    coords[4]=(float) x;
    coords[5]=(float) y;
    
    current_path.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
    
    currentOp += 6;
    curveCount--;
    }
    
    // line
    x += op[currentOp];
    y += op[currentOp+1];
    current_path.lineTo((float)x,(float)y);
    currentOp += 2;
    
    }else if(key==25){ //rlinecurve
    //lines
    int lineCount=( opCount - 6 ) / 2;
    while ( lineCount > 0 ){
    x += op[currentOp];
    y += op[currentOp+1];
    current_path.lineTo((float)x,(float)y);
    currentOp += 2;
    lineCount--;
    }
    //curves
    float[] coords=new float[6];
    x += op[currentOp];
    y += op[currentOp+1];
    coords[0]=(float) x;
    coords[1]=(float) y;
    
    x += op[currentOp+2];
    y += op[currentOp+3];
    coords[2]=(float) x;
    coords[3]=(float) y;
    
    x += op[currentOp+4];
    y += op[currentOp+5];
    coords[4]=(float) x;
    coords[5]=(float) y;
    
    current_path.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
    
    currentOp += 6;
    
    }else if((key==26)|(key==27)){ //vvcurve hhcurveto
    boolean isVV=(key==26);
    if ( (opCount & 1) ==1 ){
    if(isVV)
    x += op[0];
    else
    y += op[0];
    currentOp++;
    }
    
    //note odd co-ord order
    while (currentOp<opCount ){
    if(isVV)
    y += op[currentOp];
    else
    x += op[currentOp];
    pt[0]=(float) x;
    pt[1]=(float) y;
    x += op[currentOp+1];
    y += op[currentOp+2];
    pt[2]=(float) x;
    pt[3]=(float) y;
    if(isVV)
    y += op[currentOp+3];
    else
    x += op[currentOp+3];
    pt[4]=(float) x;
    pt[5]=(float) y;
    currentOp += 4;
    current_path.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
    
    }
    }else if((key==30)|(key==31)){	//vhcurveto/hvcurveto
    boolean  isHor=(key==31);
    
    while ( opCount >= 4 ){
    opCount -= 4;
    if ( isHor )
    x += op[currentOp];
    else
    y += op[currentOp];
    pt[0]=(float) x;
    pt[1]=(float) y;
    x += op[currentOp+1];
    y += op[currentOp+2];
    pt[2]=(float) x;
    pt[3]=(float) y;
    if ( isHor ){
    y += op[currentOp+3];
    if ( opCount ==1 )
    x += op[currentOp+4];
    }else{
    x += op[currentOp+3];
    if ( opCount == 1 )
    y += op[currentOp+4];
    }
    pt[4]=(float) x;
    pt[5]=(float) y;
    current_path.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
    
    currentOp  += 4;
    
    isHor = !isHor;
    }
    }else{
    System.out.println(p+">>>>>"+hintCount+">>>>>>"+key+" "+charCodes1Byte[key]+" <1<<"+op);
    for(int j=0;j<dicEnd;j++)
    System.out.println(j+" "+(glyphStream[j] & 0xff));
    }
    }
    
    opCount=0; //move to first operator
    isFirst=false;
    }
    }
    
    return current_path;
    }else
    return null;
    }*/
    
    /** needed so CIDFOnt0 can extend */
    public Type1() {
    }
    
    /** Handle encoding for type1 fonts */
    protected final void readType1FontFile(final byte[] content) throws Exception {
        
        if(LogWriter.isOutput()) {
            LogWriter.writeLog("Embedded Type1 font used "+getBaseFontName());
        }
        
        final BufferedReader br =new BufferedReader(new StringReader(new String(content)));
        
        String line;
        
        while (true) {
            
            line = br.readLine();
            
            if (line == null) {
                break;
            }
            
            if (line.startsWith("/Encoding 256 array")) {
                readDiffEncoding(br);
            } else if(line.startsWith("/lenIV")){
                final StringTokenizer vals=new StringTokenizer(line);
                vals.nextToken(); //drop first value
                skipBytes=Integer.parseInt(vals.nextToken());
            }else if(line.contains("/FontMatrix")){
                
                int startP;
                final int endP;
                String values="";
                
                startP=line.indexOf('[');
                if(startP!=-1){
                    endP=line.indexOf(']');
                    values=line.substring(startP+1,endP);
                }else{
                    startP=line.indexOf('{');
                    if(startP!=-1){
                        endP=line.indexOf('}');
                        values=line.substring(startP+1,endP);
                    }
                }
                final StringTokenizer matrixValues=new StringTokenizer(values);
                
                for(int i=0;i<6;i++) {
                    FontMatrix[i]=Double.parseDouble(matrixValues.nextToken());
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
        
        //read the eexec part (which can be binary or ascii
        int glyphCount=0;
        if(this.renderPage) {
            glyphCount=readEncodedContent(content);
        }
        this.glyphs.setGlyphCount(glyphCount);
        
        if(!renderPage || glyphCount>0) {
            isFontEmbedded = true;
        }
        
        glyphs.setFontEmbedded(true);
        
        
    }
    
    /**
     * read the diff table from a type 1 font
     */
    private void readDiffEncoding(final BufferedReader br) throws Exception {
        
        String line, name, rawVal,base,val;
        int code,ptr;
        
        while ((line = br.readLine()) != null) {
            
            line = line.trim();
            
            //exit at end
            if (line.startsWith("readonly")) {
                break;
            }
            
            //read each mapping
            if (line.startsWith("dup") && line.contains("/")) {
                final StringTokenizer info = new StringTokenizer(line, " /");
                if (info.countTokens() >= 3) {
                    
                    info.nextToken(); // discard dup
                    rawVal=info.nextToken();
                    
                    ptr=rawVal.indexOf('#');
                    if(ptr==-1) {
                        code = Integer.parseInt(rawVal); //code
                    } else{
                        base=rawVal.substring(0,ptr);
                        val=rawVal.substring(ptr+1,rawVal.length());
                        code =Integer.parseInt(val,Integer.parseInt(base));
                    }
                    name = info.nextToken(); //name
                    
                    putChar(code, name);
                    
                    final char c=name.charAt(0);
                    if (c=='B' || c=='C' ||  c=='c' ||  c=='G') {
                        int i = 1;
                        final int l=name.length();
                        while (!isHex && i <l) {
                            isHex = Character.isLetter(name.charAt(i++));
                        }
                    }
                }
            }
        }
    }
    
    /**store embedded differences*/
    protected final void putChar(final int charInt, final String mappedChar) {
        
        if(diffs==null) {
            diffs = new  String[maxCharCount];
        }
        
        diffs[charInt]= mappedChar;
        
        if (!hasEncoding && !isCID){
            
            if(StandardFonts.getUnicodeName(mappedChar)!=null) {
                putMappedChar(charInt, mappedChar);
            }else if(!isHex){  //<--possibly needed for glyphs of format aXX when XX is base10 glyf char  (a19, a71)
           // }else{  //needed to convert back for HTML5

                //System.out.println(mappedChar+" "+charInt+" "+Character.isAlphabetic(charInt)+" "+(char)charInt+" "+this.is1C()+" "+this.isHex+" ");
                nonStandardMappings.put(mappedChar,charInt);
            }
            
        }
    }
    /**
     * read the encoded part from a type 1 font
     */
    private int readEncodedContent(byte[] cont) throws Exception {
        
        int glyphCount=0;
        String line  ;
        final String rd="rd";
        final String nd="nd";
        final int size = cont.length;
        int charstringStart = -1;
        int end = -1;
        int i;
        int cipher;
        int plain;
        StringBuilder tmp;
        
        for (i = 4; i < size; i++) { //find the start of /CharStrings (which is exec
            if ((cont[i - 3] == 101)&& (cont[i - 2] == 120)&& (cont[i - 1] == 101)&& (cont[i] == 99)) {
                charstringStart = i + 1;
                while (cont[charstringStart] == 10 || cont[charstringStart] == 13) {
                    charstringStart++;
                }
                i = size;
            }
        }
        
        if (charstringStart != -1) { //find the end
            for (i = charstringStart; i < size - 10; i++) {
                if ((cont[i] == 99)&& (cont[i + 1] == 108)&& (cont[i + 2] == 101)&& (cont[i + 3] == 97)&& (cont[i + 4] == 114)&& (cont[i + 5] == 116)&& (cont[i + 6] == 111)&& (cont[i + 7] == 109)&& (cont[i + 8] == 97)&& (cont[i + 9] == 114)&& (cont[i + 10] == 107)) {
                    end = i - 1;
                    while ((cont[end] == 10) || (cont[end] == 13)) {
                        end--;
                    }
                    i = size;
                }
            }
        }
        
        if (end == -1) {
            end = size;
        }
        
        /** now decode the array */
        int r = 55665;
        final int n = 4;
        
        /** workout if binary or ascii - assume true and disprove */
        boolean isAscii = true;
        for (i = charstringStart; i < charstringStart + (n * 2); i++) {
            final char c = (char) (cont[i]);
            if ((c >= '0' && c <= '9')|| (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) {
                //is okay
            } else {
                isAscii = false;
                break;
            }
        }
        
        if (charstringStart != -1) {
            
            final FastByteArrayOutputStream bos = new FastByteArrayOutputStream(end-charstringStart);
            
            //allow for offset in real pfb file
            if (isFontSubstituted && !isAscii) {
                charstringStart = charstringStart + 2 + skipBytes;
            }
            
            for (i = charstringStart; i < end; i++) {
                
                if (!isAscii){
                    cipher = cont[i] & 0xff;
                }else {
                    int chars = 0;
                    tmp = new StringBuilder();
                    
                    //get 2 chars for next hex value ignoring
                    // whitespace/returns,etc
                    while (chars < 2) {
                        cipher = cont[i] & 0xff;
                        i++;
                        
                        if (cipher != 10 &&  cipher != 13 && cipher != 9 && cipher != 32) {
                            tmp.append((char)cipher);
                            chars++;
                        }
                        
                    }
                    i--;
                    
                    //convert to hex value
                    cipher = Integer.parseInt(tmp.toString(), 16);
                }
                
                plain = (cipher ^ (r >> 8));
                
                r = ((cipher + r) * c1 + c2) & 0xffff;
                
                if (i > charstringStart + n) {
                    bos.write(plain);
                }
                
            }
            
            cont = bos.toByteArray();
        }
        
        /** now charset decode and store */
        //n=4;//set default value for n
        
        
        //read values from the stream
        final BufferedReader br =new BufferedReader(new StringReader(new String(cont)));
        
        while (true) {
            
            line = br.readLine();
            
            if (line == null) {
                break;
            }
            
            //if(line.startsWith("/two"))
            //System.out.println(line);
            
            //if(!line.startsWith("/"))
            
            //get new value for n second value
            if(line.startsWith("/lenIV")){
                final StringTokenizer vals=new StringTokenizer(line);
                vals.nextToken(); //drop first value
                skipBytes=Integer.parseInt(vals.nextToken());
                //System.out.println(line);
            }
        }
        
        br.close();
        
        /**extract the contents*/
        
        //find dictionary entries
        final int l=cont.length;
        int p=0;
        charstringStart=-1;
        int subrsStart=-1;
        int blueValuesStart = -1;
        int otherBluesStart = -1;
        int familyBluesStart = -1;
        int familyOtherBluesStart = -1;
        int blueScaleStart = -1;
        int blueShiftStart = -1;
        int blueFuzzStart = -1;
        int stdHWStart = -1;
        int stdVWStart = -1;
        int stemSnapHStart = -1;
        int stemSnapVStart = -1;
        int forceBoldStart = -1;
        int languageGroupStart = -1;
        
        final char[] charstringsChars = "/CharStrings".toCharArray();
        final char[] subrsChars = "/Subrs".toCharArray();
        final char[] blueValuesChars = "/BlueValues".toCharArray();
        final char[] otherBluesChars = "/OtherBlues".toCharArray();
        final char[] familyBluesChars = "/FamilyBlues".toCharArray();
        final char[] familyOtherBluesChars = "/FamilyOtherBlues".toCharArray();
        final char[] blueScaleChars = "/BlueScale".toCharArray();
        final char[] blueShiftChars = "/BlueShift".toCharArray();
        final char[] blueFuzzChars = "/BlueFuzz".toCharArray();
        final char[] stdHWChars = "/StdHW".toCharArray();
        final char[] stdVWChars = "/StdVW".toCharArray();
        final char[] stemSnapHChars = "/StemSnapH".toCharArray();
        final char[] stemSnapVChars = "/StemSnapV".toCharArray();
        final char[] forceBoldChars = "/ForceBold".toCharArray();
        final char[] languageGroupChars = "/LanguageGroup".toCharArray();
        
        while(p<l){
            
            if(p==l) {
                break;
            }
            
            if (charstringStart == -1 && (p+11<l) && checkForString(cont, p, charstringsChars)) {
                charstringStart=p+11;
            } else if(subrsStart == -1 && (p+5<l) && checkForString(cont, p, subrsChars)) {
                subrsStart=p+6;
            } else if(blueValuesStart == -1 && (p+11<l) && checkForString(cont, p, blueValuesChars)) {
                blueValuesStart=p+11;
            } else if(otherBluesStart == -1 && (p+11<l) && checkForString(cont, p, otherBluesChars)) {
                otherBluesStart=p+11;
            } else if(familyBluesStart == -1 && (p+12<l) && checkForString(cont, p, familyBluesChars)) {
                familyBluesStart=p+12;
            } else if(familyOtherBluesStart == -1 && (p+17<l) && checkForString(cont, p, familyOtherBluesChars)) {
                familyOtherBluesStart=p+17;
            } else if(blueScaleStart == -1 && (p+10<l) && checkForString(cont, p, blueScaleChars)) {
                blueScaleStart=p+10;
            } else if(blueShiftStart == -1 && (p+10<l) && checkForString(cont, p, blueShiftChars)) {
                blueShiftStart=p+10;
            } else if(blueFuzzStart == -1 && (p+9<l) && checkForString(cont, p, blueFuzzChars)) {
                blueFuzzStart=p+9;
            } else if(stdHWStart == -1 && (p+6<l) && checkForString(cont, p, stdHWChars)) {
                stdHWStart=p+6;
            } else if(stdVWStart == -1 && (p+6<l) && checkForString(cont, p, stdVWChars)) {
                stdVWStart=p+6;
            } else if(stemSnapHStart == -1 && (p+10<l) && checkForString(cont, p, stemSnapHChars)) {
                stemSnapHStart=p+10;
            } else if(stemSnapVStart == -1 && (p+10<l) && checkForString(cont, p, stemSnapVChars)) {
                stemSnapVStart=p+10;
            } else if(forceBoldStart == -1 && (p+10<l) && checkForString(cont, p, forceBoldChars)) {
                forceBoldStart=p+10;
            } else if(languageGroupStart == -1 && (p+14<l) && checkForString(cont, p, languageGroupChars)) {
                languageGroupStart=p+14;
            }
            
            if(subrsStart>-1 && charstringStart>-1 &&
                    blueValuesStart>-1 && otherBluesStart>-1 &&
                    familyBluesStart>-1 && familyOtherBluesStart>-1 &&
                    blueScaleStart>-1 && blueShiftStart>-1 && blueFuzzStart>-1 &&
                    stdHWStart>-1 && stdVWStart>-1 &&
                    stemSnapHStart>-1 && stemSnapVStart>-1 &&
                    forceBoldStart>-1 && languageGroupStart>-1) {
                break;
            }
            
            p++;
            
            
        }
        
        /**extract charstrings*/
        if(charstringStart==-1){
            this.isFontSubstituted=false;
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("No glyph data found");
            }
        }else {
            glyphCount=extractFontData(skipBytes,cont,charstringStart,rd,l,nd);
        }
        
        /**extract subroutines*/
        if(subrsStart>-1) {
            extractSubroutineData(skipBytes,cont,subrsStart,charstringStart,rd,l,nd);
        }
        
        if(blueValuesStart>-1) {
            blueValues = readIntArray(cont, blueValuesStart);
        }
        
        if (otherBluesStart>-1) {
            otherBlues = readIntArray(cont, otherBluesStart);
        }
        
        if (familyBluesStart>-1) {
            familyBlues = readIntArray(cont, familyBluesStart);
        }
        
        if (familyOtherBluesStart>-1) {
            familyOtherBlues = readIntArray(cont, familyOtherBluesStart);
        }
        
        if (stdHWStart>-1) {
            stdHW = readReal(cont, stdHWStart);
        }
        
        if (stdVWStart>-1) {
            stdVW = readReal(cont, stdVWStart);
        }
        
        if (stemSnapHStart>-1) {
            stemSnapH = readIntArray(cont, stemSnapHStart);
        }
        
        if (stemSnapVStart>-1) {
            stemSnapV = readIntArray(cont, stemSnapVStart);
        }
        
        if (blueScaleStart>-1) {
            blueScale = readReal(cont, blueScaleStart);
        }
        
        if (blueShiftStart>-1) {
            blueShift = readInteger(cont, blueShiftStart);
        }
        
        if (blueFuzzStart>-1) {
            blueFuzz = readInteger(cont, blueFuzzStart);
        }
        
        if (forceBoldStart>-1) {
            int j = forceBoldStart;
            int forceBoldEnd = -1;
            while (j < l && forceBoldEnd == -1) {
                if (checkForString(cont, j, DEF_CHARS)) {
                    forceBoldEnd = j;
                }
                j++;
            }
            final String val = new String(cont, forceBoldStart, forceBoldEnd-forceBoldStart);
            try {
                forceBold = Boolean.parseBoolean(val);
            } catch(final NumberFormatException e) {
                //Do nothing
                if (LogWriter.isOutput()) {
                    LogWriter.writeLog("Caught an Exception " + e);
                }
            }
        }
        
        if (languageGroupStart>-1) {
            languageGroup = readInteger(cont, languageGroupStart);
        }
        
        return glyphCount;
    }
    
    /**
     * Reads an integer
     */
    static Integer readInteger(final byte[] data, final int offset) {
        final int l = data.length;
        int j = offset;
        int end = -1;
        while (j < l && end == -1) {
            if (checkForString(data, j, DEF_CHARS)) {
                end = j;
            }
            j++;
        }
        final String val = new String(data, offset, end-offset);
        try {
            return Integer.parseInt(val);
        } catch(final NumberFormatException e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in handling Integer in Type1 "+e);
            }
            return null;
        }
    }
    
    /**
     * Reads a real or array containing a single real
     */
    static Double readReal(final byte[] data, final int offset) {
        final int l = data.length;
        int j = offset;
        int end = -1;
        while (j < l && end == -1) {
            if (checkForString(data, j, DEF_CHARS) || data[j] == ']' || data[j] == '\n') {
                end = j;
            }
            j++;
        }
        String val = new String(data, offset, end-offset);
        if (val.contains("[")) {
            final String[] stringParts = val.split("\\[");
            if (stringParts.length < 2) {
                return null;
            }
            val = stringParts[1];
        }
        try {
            return Double.parseDouble(val);
        } catch(final NumberFormatException e) {

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in handling real in Type1 "+e);
            }
            return null;
        }
    }
    
    /**
     * Reads an int array in the format [1 2 3]. Returns null if it can't be read, and stores -1 in the place of any
     * values which can't be read.
     * @param data The data to read from
     * @param start The start of the array
     * @return An int[] representation of the array
     */
    private static int[] readIntArray(final byte[] data, final int start) {
        int[] result = null;
        final int l = data.length;
        int j= start;
        int end = -1;
        while (j < l && end == -1) {
            if (data[j] == ']' || data[j] == '/' || data[j] == '\n') {
                end = j;
            }
            j++;
        }
        
        if (end != -1) {
            String values = new String(data, start, end-start);
            if (values.contains("[")) {
                final String[] stringParts = values.split("\\[");
                if (stringParts.length < 2) {
                    return null;
                }
                values = stringParts[1];
            }
            values = values.trim();
            final String[] stringValues = values.split(" ");
            result = new int[stringValues.length];
            for (j=0; j< stringValues.length; j++) {
                try {
                    result[j] = Integer.parseInt(stringValues[j].split("\\.")[0]);
                } catch(final NumberFormatException e) {

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception in handling IntArray "+e);
                    }
                    result[j] = -1;
                }
            }
        }
        return result;
    }
    
    /**
     * Checks for a sequence of chars at a given offset in a byte array.
     */
    private static boolean checkForString(final byte[] data, final int offset, final char[] chars) {
        for (int i=0; i<chars.length; i++) {
            if (data[offset+i] != chars[i]){
                return false;
            }
        }
        
        return true;
    }
    
    private void extractSubroutineData(final int skipBytes, final byte[] cont,int start, final int charStart, final String rd, final int l, final String nd) {
        
        final int count ;
        
        //move to start of first value
        while(cont[start] == 32 || cont[start]==10 || cont[start]==13) {
            start++;
        }
        
        //read the  count
        StringBuilder tmp=new StringBuilder();
        while(true){
            final char c=(char)cont[start];
            if(c==' ') {
                break;
            }
            tmp.append(c);
            start++;
        }
        
        count=Integer.parseInt(tmp.toString());
        
        for(int i=0;i<count;i++){
            
            //read the dup
            while((start<l)){
                if (((cont[start-2] == 100)&& (cont[start - 1] == 117)&& (cont[start ] == 112))|(start==charStart)) {
                    break;
                }
                start++;
            }
            
            if(start==charStart){
                i=count;
            }else{
                //move to start of first value
                while((cont[start+1] == 32)) {
                    start++;
                }
                
                //read the  count
                final StringBuilder glyph=new StringBuilder("subrs");
                while(true){
                    start++;
                    final char c=(char)cont[start];
                    if(c==' ') {
                        break;
                    }
                    
                    glyph.append(c);
                    
                }
                
                //read the byte count
                tmp=new StringBuilder();
                while(true){
                    start++;
                    final char c=(char)cont[start];
                    if(c==' ') {
                        break;
                    }
                    tmp.append(c);
                    
                }
                
                final int byteCount=Integer.parseInt(tmp.toString());
                
                //skip any more spaces
                while((cont[start] == 32)) {
                    start++;
                }
                
                //skip RD and blank space and random bytes
                start=start+rd.length()+1;
                final byte[] stream=getStream(skipBytes,start,byteCount,cont);
                
                //store table
                glyphs.setCharString(glyph.toString(),stream, i);
                
                start=start+byteCount+nd.length();
                
                //move to start of next value
                //while((start<=cont.length)&&(cont[start] != 47))
                //	start++;
            }
        }
    }
    
    private int extractFontData(final int skipBytes, final byte[] cont,int start, final String rd, final int l, final String nd) {
        
        final int total=cont.length;
        int glyphCount=0;

        //move to start of first value
        while(start<total && cont[start] != 47) {
            start++;
        }
        
        int end=start;
        
//        System.out.println("----->\n");
//        for(int aa=start;aa<cont.length;aa++)
//        	System.out.print((char)cont[aa]);
//        System.out.println("\n<-----");
//
//        try{
        while(start<l){
            
            //no name has space of return starts with /
            if(cont[end]==47){
                
                end += 2;
                
                //move to end
                while(end<total){
                    
                    if(cont[end-1]==124 && (cont[end]==45 || cont[end]==48) && (cont[end+1]==10|| cont[end+1]==13)) {
                        break;
                    }
                    
                    if(cont[end-1]=='N' && cont[end]=='D') {
                        break;
                    }
                    
                    end++;
                }
            }
            
            //exit on end
            if (total-end<3 || (cont[end-1]!=47 && cont[end] == 101 && cont[end + 1] == 110 && cont[end + 2] == 100)) {
                break;
            }
            
            end++;
        }
        
        while(start<=end){
            
            //read the glyph name
            final StringBuilder glyph=new StringBuilder(20);
            
            while(true){
                start++;
                final char c=(char)cont[start];
                if(c==' ') {
                    break;
                }
                glyph.append(c);
                
            }
            
            if(trackIndices){
                glyphs.setIndexForCharString(glyphCount, glyph.toString());
                
            }
            
            
            start++;
            
            //read the byte count
            final StringBuilder tmp=new StringBuilder();
            while(true){
                final char c=(char)cont[start];
                if(c==' ') {
                    break;
                }
                tmp.append(c);
                start++;
            }
            
            final int byteCount=Integer.parseInt(tmp.toString());
            
            //skip any more spaces
            while((cont[start] == 32)) {
                start++;
            }
            
            //skip RD and blank space
            start=start+rd.length()+1;
            final byte[] stream=getStream(skipBytes,start,byteCount,cont);
            
            
            //store table
            glyphs.setCharString(glyph.toString(),stream, glyphCount);
            
            glyphCount++;
            
            start=start+byteCount+nd.length();
            
            //move to start of next value
            while((start<=end)&&(cont[start] != 47)) {
                start++;
            }
            
        }
        
        return glyphCount;
    }
    
    /**extract bytestream with char data*/
    private static byte[] getStream(final int skipBytes, final int start, final int byteCount, final byte[] cont) {
        
        //get the  stream
        final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
        
        int r=4330,cipher,plain;
        
        for (int i = 0; i < byteCount; i++) {
            cipher = cont[start+i] & 0xff;
            
            plain = (cipher ^ (r >> 8));
            //here�
            r = ((cipher + r) * c1 + c2) & 0xffff;
            if(i>=skipBytes) {
                bos.write(plain);
                //  System.out.println(plain);
                //else
                //    System.out.println("SKIP="+plain);
            }
        }

        return bos.toByteArray();
    }
}
