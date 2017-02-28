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
 * Tj.java
 * ---------------
 */
package org.jpedal.parser.text;

import org.jpedal.PdfDecoderInt;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.*;
import org.jpedal.fonts.tt.TTGlyph;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfData;
import org.jpedal.objects.TextState;
import org.jpedal.parser.*;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Fonts;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;
import org.jpedal.utils.repositories.Vector_Int;

import org.jpedal.external.GlyphTracker;

import org.jpedal.objects.structuredtext.StructuredContentHandler;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import org.jpedal.external.ErrorTracker;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

/**
 * handle conversion of the text operands
 */
public class Tj extends BaseDecoder {
    
    public static boolean showInvisibleText;
    
    private PdfData pdfData;
    
    private PdfFont currentFontData;
    
    private final Vector_Rectangle_Int textAreas;
    
    private final Vector_Int textDirections;
    
    private TextState currentTextState=new TextState();

    private final GlyphData glyphData=new GlyphData();
    
    private GlyphTracker customGlyphTracker;
    private StructuredContentHandler contentHandler;
    
    /**
     * flag to show some fonts might need hinting turned on to display properly
     */
    private boolean ttHintingRequired;
    
    /** the text value we decoded from the tj command*/
    private String tjTextValue = "";
    
    
    /**start of ascii escape char*/
    static final String[] hex={"&#0;","&#1;","&#2;","&#3;",
        "&#4;","&#5;","&#6;","&#7;","&#8;","&#9;","&#10;","&#11;",
        "&#12;","&#13;","&#14;","&#15;","&#16;","&#17;","&#18;","&#19;",
        "&#20;","&#21;","&#22;","&#23;","&#24;","&#25;","&#26;",
        "&#27;","&#28;","&#29;","&#30;","&#31;"};
    
    /**gap between characters*/
    private float charSpacing;
    
    private final GlyphFactory factory;
    
    private PdfJavaGlyphs glyphs;
    
    private float[][] Trm;
    
    /**used by forms code to read text*/
    private boolean returnText;
    
    //private static final int NONE=0;
    
    //private static final int RIGHT=1;
    
    /**co-ords (x1,y1 is top left corner)*/
    private float x1;
    private float y1;
    private float x2;
    private float y2;
    
    private float lastWidth,currentWidth;
    
    //if ActualText set store value and use if preference for text extraction
    private String actualText;
    
    private final DynamicVectorRenderer current;
    
    private int streamLength;
    
    private float[][] TrmBeforeSpace = new float[3][3];
    
    private boolean isTabRemapped,isCRRemapped,isReturnRemapped;
    
    private final ErrorTracker errorTracker;

    public Tj(final ParserOptions parserOptions, final PdfData pdfData, final boolean isXMLExtraction,
              final Vector_Rectangle_Int textAreas, final Vector_Int textDirections,
              final DynamicVectorRenderer current, final ErrorTracker errorTracker) {
        
        this.parserOptions=parserOptions;
        
        this.pdfData=pdfData;
        glyphData.setXMLExtraction(isXMLExtraction);
        
        this.textAreas=textAreas;
        this.textDirections=textDirections;
        this.current=current;
        
        this.errorTracker=errorTracker;
        
        factory=new T1GlyphFactory(parserOptions.useJavaFX());
    }
    
    public Tj(final ParserOptions parserOptions, final Vector_Rectangle_Int textAreas, final Vector_Int textDirections, final DynamicVectorRenderer current, final ErrorTracker errorTracker) {
        
        this.parserOptions=parserOptions;
        factory=new T1GlyphFactory(parserOptions.useJavaFX());
        
        this.textAreas=textAreas;
        this.textDirections=textDirections;
        this.current=current;
         this.errorTracker=errorTracker;
    }
    
    
    /**
     * Calculate the x coords for text here
     * y coords are calculated in the method
     * processTextArray(final byte[] stream,int startCommand,int dataPointer)
     */
    private void calcCoordinates(final float x, final float[][] Trm, float charSpacing ) {
        
        //clone data so we can manipulate
        final float[][] trm=new float[3][3];
        for(int xx=0;xx<3;xx++){
            System.arraycopy(Trm[xx], 0, trm[xx], 0, 3);
        }
        
        x1 = x;
        x2 = trm[2][0] - (charSpacing * trm[0][0]);
        
        if (glyphData.isHorizontal()) {
            if (trm[1][0] < 0) {
                x1 = x + trm[1][0] - (charSpacing * trm[0][0]);
                x2 = trm[2][0];
            } else if (trm[1][0] > 0) {
                x1 = x;
                x2 = trm[2][0];
            }
        } else if (trm[1][0] > 0) {
            x1 = trm[2][0];
            x2 = x + trm[1][0] - (charSpacing * trm[0][0]);
        } else if (trm[1][0] < 0) {
            x2 = trm[2][0];
            x1 = x + trm[1][0] - (charSpacing * trm[0][0]);
        }
    }
    
    public String TJ(final TextState currentTextState, final PdfFont currentFontData, final byte[] characterStream, final int
            startCommand, final int dataPointer, final boolean multipleTJs) {
        
        this.currentTextState=currentTextState;
        this.currentFontData=currentFontData;
        
        this.customGlyphTracker=parserOptions.getCustomGlyphTracker();
        this.contentHandler=parserOptions.getContentHandler();
        
        isTabRemapped = currentFontData.getDiffMapping(9)!=null;
        isCRRemapped = currentFontData.getDiffMapping(10)!=null;
        isReturnRemapped = currentFontData.getDiffMapping(13)!=null;
        
        streamLength=characterStream.length;
        
        glyphs = currentFontData.getGlyphData();
        
        /**set colors*/
        if(parserOptions.isRenderText() && gs.getTextRenderType()!=GraphicsState.INVISIBLE){
            gs.setStrokeColor(gs.strokeColorSpace.getColor());
            gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());
        }
        
        final StringBuffer current_value =processTextArray(characterStream, startCommand, dataPointer, multiplyer,multipleTJs);
        

        /**get fontsize and ensure positive*/
        int fontSize= glyphData.getFontSize();
        if(fontSize==0) {
            fontSize = (int) currentTextState.getTfs();
        }
        
        if(fontSize<0) {
            fontSize = -fontSize;
        }
        
        //will be null if no content
        if (current_value != null && parserOptions.isPageContent()){
            
            String currentColor=null;
            
            //get colour if needed
            if(parserOptions.isTextColorExtracted()){
                if ((gs.getTextRenderType() & GraphicsState.FILL) == GraphicsState.FILL){
                    currentColor=gs.nonstrokeColorSpace.getXMLColorToken();
                }else{
                    currentColor=gs.strokeColorSpace.getXMLColorToken();
                }
            }

            if(contentHandler!=null) {
                contentHandler.setText(current_value, x1, y1, x2, y2);
            } else if (parserOptions.isTextExtracted()) {
                
                /**
                 * save item and add in graphical elements
                 */
                pdfData.addRawTextElement(
                        (charSpacing * Leading.THOUSAND),
                        currentTextState.writingMode,
                        Fonts.createFontToken(currentFontData.getFontName(), fontSize),
                        currentFontData.getCurrentFontSpaceWidth(),
                        fontSize,
                        x1,
                        y1,
                        x2,
                        y2,
                        current_value,
                        glyphData.getTextLength(), currentColor, glyphData.isXMLExtraction());
            }
        }
        
        return tjTextValue;
    }
    
    private void resetValues(GlyphData glyphData){
        
        glyphData.reset();
        
        TrmBeforeSpace = new float[3][3];
        
        lastWidth = 0;
        currentWidth = 0;
        
        /** create temp matrix for current text location and factor in scaling*/
        Trm = Matrix.multiply(currentTextState.Tm, gs.CTM);
        
    }
    
    /**
     * turn TJ into string and plot. THis routine is long but frequently called so we want all code 'inlined'
     */
    private StringBuffer processTextArray(final byte[] stream, int startCommand, final int dataPointer, final float multiplyer, final boolean multipleTJs){
        
        isHTML=current.isHTMLorSVG();
        
        /**
         * global and local values
         */
        resetValues(glyphData);
        
        final int Tmode=gs.getTextRenderType();
        boolean hasContent=false,isMultiple=false; //flag text found as opposed to just spacing
        char lastTextChar = 'x';
        float TFS = currentTextState.getTfs();
        final float rawTFS=TFS;
        
        if(TFS<0) {
            TFS = -TFS;
        }
        
        final int type=currentFontData.getFontType();
        final float spaceWidth = currentFontData.getCurrentFontSpaceWidth();
        
        StringBuffer textData = null;
        if(parserOptions.isTextExtracted()) {
            textData = new StringBuffer(50); //used to return a value
        }
        
        float currentGap;
        
        //flag to show text highlight needs to be shifted up to allow for displacement in Trm
        boolean isTextShifted=false;
        
        //roll on at start if necessary
        while(stream[startCommand]==91 || stream[startCommand]==10 || stream[startCommand]==13 || stream[startCommand]==32){
            
            if(stream[startCommand]==91) {
                isMultiple = true;
            }
            
            startCommand++;
        }
        
        /**set character size */
        glyphData.setDefaultCharSize(currentFontData);
        
        charSpacing = currentTextState.getCharacterSpacing() / TFS;
        final float wordSpacing = currentTextState.getWordSpacing() / TFS;
        
        if(multipleTJs){ //allow for consecutive TJ commands
            Trm[2][0]=currentTextState.Tm[2][0];
            Trm[2][1]=currentTextState.Tm[2][1];
        }
        
        /**define matrix used for converting to correctly scaled matrix and multiply to set Trm*/
        float[][] temp = new float[3][3];
        temp[0][0] = rawTFS * currentTextState.getHorizontalScaling();
        temp[1][1] = rawTFS;
        temp[2][1] = currentTextState.getTextRise();
        temp[2][2] =1;
        Trm = Matrix.multiply(temp, Trm);
        
        //check for leading before text and adjust position to include
        if(isMultiple && stream[startCommand]!=60 && stream[startCommand]!=40 && stream[startCommand]!=93){
            
            float offset=0;
            while(stream[startCommand]!=40 && stream[startCommand]!=60 && stream[startCommand]!=93){
                final StringBuilder kerning = new StringBuilder(10);
                while(stream[startCommand]!=60 && stream[startCommand]!=40 && stream[startCommand]!=93 && stream[startCommand]!=32){
                    kerning.append((char)stream[startCommand]);
                    startCommand++;
                }
                offset += Float.parseFloat(kerning.toString());
                
                while(stream[startCommand]==32) {
                    startCommand++;
                }
            }
            
            //new condition as we did not cover case where text rotated by matrix so
            //we were adding 0 * offset which is zero! Fixed for just the case found
            //where Trm[0][1]>0 && Trm[1][0]<0
            
            if(Trm[0][0]==0 && Trm[1][1]==0 && Trm[0][1]!=0 && Trm[1][0]!=0){
                
                offset=Trm[0][1]*offset/ Leading.THOUSAND;
                
                Trm[2][1] -= offset;
                
            }else{
                offset=Trm[0][0]*offset/ Leading.THOUSAND;
                
                Trm[2][0] -= offset;
            }
        }
        
        /**
         * workout fontScale, direction
         */
        final int fontSize=calcFontSize(glyphData,currentTextState,Trm);
        
        /**
         * text printing mode to get around problems with PCL printers
         */
        Font javaFont=null;
        
        final int textPrint=parserOptions.getTextPrint();
        
        if(textPrint== PdfDecoderInt.STANDARDTEXTSTRINGPRINT && StandardFonts.isStandardFont(currentFontData.getFontName(), true) && parserOptions.isPrinting()){
            javaFont= currentFontData.getJavaFontX(fontSize);
        }else if(currentFontData.isFontEmbedded && !currentFontData.isFontSubstituted()){
            javaFont=null;
        }else if((PdfStreamDecoder.useTextPrintingForNonEmbeddedFonts || textPrint!=PdfDecoderInt.NOTEXTPRINT)&& parserOptions.isPrinting()) {
            javaFont = currentFontData.getJavaFontX(fontSize);
        }
        
        /**extract starting x and y values (we update Trm as we work through text)*/
        final float x= Trm[2][0];
        
        //track text needs to be moved up in highlight
        if(Trm[1][0]<0 && Trm[0][1]>0 && Trm[1][1]==0 && Trm[0][0]==0) {
            isTextShifted = true;
        }
        
        /**now work through all glyphs and render/decode*/
        int i = startCommand;
        
        StringBuffer buff = null;
        if(returnText) {
            buff = new StringBuffer(streamLength);
        }
        
        boolean resetCoords = true;
        final boolean isCID=currentFontData.isCIDFont();
        
        while (i < dataPointer) {
            
            //used by Sanface file to fix spacing issue (only set in specific case)
            glyphData.setActualWidth(-1);

            //read next value ignoring spaces, tabs etc
            i=CharReader.getNextValue(i,stream,glyphData,isCID);
           
            /**either handle glyph, process leading or handle a deliminator*/
            if (glyphData.isText()) { //process if still in text
            
                lastTextChar = glyphData.getRawChar(); //remember last char so we can avoid a rollon at end if its a space
                
                //convert escape or turn index into correct glyph allow for stream
                if (glyphData.getOpenChar() == 60) {
                   
                    //check /PDFdata/test_data/baseline_screens/14jan/ASTA invoice - $275.pdf  if you alter this code
                    if (isCID && !currentFontData.isFontSubstituted()  && currentFontData.isFontEmbedded && ( stream[i]!='0')){
                        i=HexTextUtils.getHexCIDValue(stream, i,glyphData, currentFontData, parserOptions);
                    }else{
                        i = HexTextUtils.getHexValue(stream, i,glyphData, currentFontData, parserOptions);
                    }
                    
                }else if (lastTextChar == 92 && !isCID) {
                    i = EscapedTextUtils.getEscapedValue(i, stream,glyphData, currentFontData,streamLength,parserOptions, current);
                } else if (isCID){  //could be nonCID cid
                    i=CIDTextUtils.getCIDCharValues(i,stream, streamLength,glyphData,currentFontData, parserOptions);
                }else{
                    lastTextChar = getValue(lastTextChar,glyphData, currentFontData, current, parserOptions);
                }
                
                //Handle extracting CID Identity fonts
                if (isHTML && !currentFontData.hasToUnicode() &&
                        currentFontData.getFontType() == StandardFonts.CIDTYPE0 &&
                        currentFontData.getGlyphData().isIdentity()){
                    
                    //Check if proper char has been stored instead
                    int charToUse=(int)glyphData.getRawChar();
                    final int valueForHTML=glyphData.getValueForHTML();
                    if(valueForHTML!=-1){
                        charToUse=valueForHTML;
                        glyphData.setValueForHTML(-1);
                    }
                    
                    final int rawC = StandardFonts.mapCIDToValidUnicode(currentFontData.getBaseFontName(), charToUse);
                    glyphData.setUnicodeValue(String.valueOf((char) (rawC)));
                }
                
                //Itext likes to use Tabs!
                if(!isTabRemapped && glyphData.getRawInt()==9 && currentFontData.isFontSubstituted()){
                    glyphData.setRawInt(32);
                    glyphData.set(" ");
                }
                
                //MOVE pointer to next location by updating matrix
                temp[0][0] = 1;
                temp[0][1] = 0;
                temp[0][2] = 0;
                temp[1][0] = 0;
                temp[1][1] = 1;
                temp[1][2] = 0;
                
                if(currentFontData.isFontVertical()){
                    temp[2][1] = -(currentWidth + glyphData.getLeading()); //tx;
                    temp[2][0] = 0; //ty;
                }else{
                    temp[2][0] = (currentWidth +  glyphData.getLeading()); //tx;
                    temp[2][1] = 0; //ty;
                }
                temp[2][2] = 1;
                Trm = Matrix.multiply(temp, Trm); //multiply to get new Tm
                
                /**save pointer in case its just multiple spaces at end*/
                if (glyphData.getRawChar() == ' ' && glyphData.getLastChar() != ' '){
                    TrmBeforeSpace = Trm;
                }
                
                glyphData.setLeading(0); //reset leading
                
                if(currentFontData.isCIDFont() && glyphs.is1C() && !glyphs.isIdentity()){
                    
                    final int idx=glyphs.getCMAPValue(glyphData.getRawInt());
                    if(idx>0) {
                        glyphData.setRawInt(idx);
                    }
                    
                }
                int idx=glyphData.getRawInt();
                
                if(!glyphs.isCorrupted()){
                    if(currentFontData.isCIDFont() && !glyphs.isIdentity() && !glyphs.hasGIDtoCID()){ //should only be use dif no CIDtoGID used in mapping
                        final int mappedIdx=glyphs.getConvertedGlyph(idx);
                        
                        if(mappedIdx!=-1) {
                            idx = mappedIdx;
                        }
                    }else if(currentFontData.getFontType()!=StandardFonts.TYPE3){//if a numeric value we need to replace to get correct glyph
                        final int diff=currentFontData.getDiffChar(idx);
                        if(diff>0){
                            glyphData.setRawInt(diff);
                        }
                    }
                }
                
                final float actualWidth=glyphData.getActualWidth();
                if(actualWidth>0){
                    currentWidth=actualWidth;
                }else{
                    currentWidth = currentFontData.getWidth(idx);
                }
                
                /**
                 * XFA docs can contain non-embedded fonts like Callibri and Myriad Pro
                 * which have no defined width so in this case we use Arial size instead
                 */
                if(currentWidth==0 && parserOptions.isXFA()){
                    final Float value=StandardFonts.getStandardWidth("Arial" , currentFontData.getMappedChar( glyphData.getRawInt(),false));
                    currentWidth = value!=null? value : 0.0f;
                    //we need this because code above
                    //currentWidth = currentFontData.getWidth(idx); 
                    //explicitly caches lastWidth internally so we can reread in HTML code 
                    //by passing in -1.
                    currentFontData.setLastWidth(currentWidth);
                }
                
                //used by HTML
                if(isHTML &&
                    //try to fix issue with THE mapped to The Text in output by seeing if using CAPITAL values
                    (!currentFontData.isFontSubsetted()&& currentFontData.getFontEncoding(true)==StandardFonts.WIN && (glyphData.getUnicodeValue().charAt(0)-idx)==32)){
                         glyphData.setUnicodeValue(String.valueOf((char) idx));
                    
                }
                
                if(currentWidth==0 && parserOptions.isXFA()){
                    
                    final String glyfName;
                    final int rawInt=glyphData.getRawInt();
                    if(rawInt>255){
                        glyfName=String.valueOf(rawInt); //may need some debugging for non-standard chars
                    }else{
                        glyfName=StandardFonts.getUnicodeChar(StandardFonts.WIN,rawInt); //may need some debugging for non-standard chars
                    }
                    
                    currentWidth = currentFontData.getGlyphWidth(glyfName, rawInt, glyphData.getDisplayValue());
                    
                }
                
                //debug code to lock out text if not in area
                //System.out.println(currentWidth+"=========="+" glyphData.rawInt="+glyphData.getRawInt()+" idx="+idx+" d="+glyphData.getDisplayValue()+"< uni="+glyphData.getUnicodeValue()+"< "+currentFontData+" "+currentFontData.getFontName()+" "+currentFontData.getBaseFontName());
             
                /**
                 * used by form code to return a value for FormStream.decipherTextFromAP
                 * There are actually 4 ways we render the text underneath and it was in ONE. Not having the fonts
                 * map caused one of the alteratives to be used so string was never populated and rest of code broke.
                 */
                if(returnText) {
                    buff.append(glyphData.getDisplayValue());
                }
                
                /**if we have a valid character and we are rendering, draw it */
                
                currentTextState.setLastKerningAdded(glyphData.getSpacingAdded());
                glyphData.setSpacingAdded(0);
                
               
                if ((parserOptions.isRenderText()  && (Tmode!=GraphicsState.INVISIBLE || isHTML)) || 
                        (Tmode==GraphicsState.CLIPTEXT && parserOptions.isRenderClipText())){
                    
                    if(javaFont!=null && parserOptions.isPrinting() && (textPrint==PdfDecoderInt.STANDARDTEXTSTRINGPRINT ||
                            (textPrint==PdfDecoderInt.TEXTSTRINGPRINT || (PdfStreamDecoder.useTextPrintingForNonEmbeddedFonts  &&
                            (!currentFontData.isFontEmbedded || currentFontData.isFontSubstituted()))))){
                        
                        /**support for TR7*/
                        if(Tmode==GraphicsState.CLIPTEXT){

                            /**set values used if rendering as well*/
                            final boolean isSTD= DecoderOptions.isRunningOnMac ||StandardFonts.isStandardFont(currentFontData.getBaseFontName(),false);
                            final Area transformedGlyph2= glyphs.getStandardGlyph(Trm, glyphData.getRawInt(), glyphData.getDisplayValue(), currentWidth, isSTD);
                            
                            if(transformedGlyph2!=null){
                                gs.addClip(transformedGlyph2);
                                //current.drawClip(gs) ;
                            }
                            
                            current.drawClip(gs,null,true);

                        }
                        
                        if(glyphData.getDisplayValue()!=null && !glyphData.getDisplayValue().startsWith("&#")){
                            if(isHTML || current.getType()==DynamicVectorRenderer.CREATE_EPOS) {
                                current.drawEmbeddedText(Trm, fontSize, null, null, DynamicVectorRenderer.TEXT, gs, null, glyphData.getDisplayValue(), currentFontData, -100);
                            } else {
                                current.drawText(Trm, glyphData.getDisplayValue(), gs, Trm[2][0], -Trm[2][1], javaFont);
                            }
                        }
                        
                    }else if(((textPrint!=PdfDecoderInt.TEXTGLYPHPRINT)||(javaFont==null))&&(currentFontData.isFontEmbedded &&
                            currentFontData.isFontSubstituted() &&((glyphData.getRawInt()==9 && !isTabRemapped) || (glyphData.getRawInt()==10 && !isCRRemapped) || (glyphData.getRawInt()==13 && !isReturnRemapped)))){ //&&
                        //lose returns which can cause odd display
                    }else if(((textPrint!=PdfDecoderInt.TEXTGLYPHPRINT)||(javaFont==null))&&(currentFontData.isFontSubstituted() && currentWidth==0 && (int)glyphData.getDisplayValue().charAt(0)==13)){ //remove substituted  values so do not enter test below
                    }else if(((textPrint!=PdfDecoderInt.TEXTGLYPHPRINT)||(javaFont==null))&&(currentFontData.isFontEmbedded)){
                        renderText(currentWidth, type, Tmode, multiplyer, isTextShifted);
                        
                    }else if(!glyphData.getDisplayValue().isEmpty() && !glyphData.getDisplayValue().startsWith("&#")) {
                        JavaTextRenderer.renderTextWithJavaFonts(gs, current, streamType, parserOptions, currentFontData, glyphData, Tmode, currentWidth, isTextShifted,glyphs,Trm);
                    }
                }
                
                /**now we have plotted it we update pointers and extract the text*/
                currentWidth += charSpacing;
                
                if (glyphData.getRawChar() == ' '){ //add word spacing if
                    currentWidth += wordSpacing;
                }
                
                //workout gap between chars and decide if we should add a space
                currentGap = (glyphData.getWidth() + charSpacing - lastWidth);
                String spaces="";
                if (currentGap > 0 && lastWidth > 0) {
                    spaces=PdfFont.getSpaces(currentGap, spaceWidth, PdfStreamDecoder.currentThreshold);
                }
                
                glyphData.addToWidth(currentWidth); //also increases text count
                lastWidth = glyphData.getWidth(); //increase width by current char
                
                //track for user if required
                if(customGlyphTracker!=null){
                    customGlyphTracker.addGlyph(Trm, glyphData.getRawInt(), glyphData.getDisplayValue(), glyphData.getUnicodeValue());
                }
                
                //add unicode value to our text data with embedded width
                if(parserOptions.isTextExtracted()) {
                    hasContent = writeOutText(glyphData, Trm, hasContent, currentWidth, textData, spaces);
                }
                
            } else if (glyphData.getRawChar() ==40 || glyphData.getRawChar() == 60) { //start of text stream '('=40 '<'=60

                    glyphData.setText(true); //set text flag - no escape character possible
                    glyphData.setOpenChar(glyphData.getRawChar());

            } else if ((glyphData.getRawChar() == 41) || (glyphData.getRawChar() == 62 && glyphData.getOpenChar()==60)||(!glyphData.isText()&&(glyphData.getRawChar()=='-' ||(glyphData.getRawChar()>='0' && glyphData.getRawChar()<='9')))) {
                i = Leading.readLeading(i, stream,glyphData);
            }
            
            //textExtracted added by Mark
            //generate if we are in Viewer (do not bother if thumbnails)
            if(parserOptions.isTextExtracted()) {
                resetCoords = setExtractedText(currentWidth, resetCoords);
            }
            
            i++;
        }
        
        if(returnText){
            if(!tjTextValue.isEmpty()) {
                tjTextValue += ' ' + buff.toString();
            } else {
                tjTextValue = buff.toString();
            }
        }
        
        Trm=updateMatrixPosition( Trm, glyphData.getLeading(), currentWidth, currentTextState);
        
        /** now workout the rectangular shape this text occupies
         * by creating a box of the correct width/height and transforming it
         * (this routine could undoutedly be better coded but it works and I
         * don't want to break it!!)
         */
        if(parserOptions.isTextExtracted()){
            
            return setExtractedText(lastTextChar, x, textData, hasContent);
        }else {
            return null;
        }       
    }
    
    static float[][] updateMatrixPosition(float[][] Trm, float leading, float currentWidth, TextState currentTextState) {
        /**all text is now drawn (if required) and text has been decoded*/
        
        //final move to get end of shape
        float[][] temp = new float[3][3];
        temp[0][0] = 1;
        temp[0][1] = 0;
        temp[0][2] = 0;
        temp[1][0] = 0;
        temp[1][1] = 1;
        temp[1][2] = 0;
        
        //if leading moves it back into text, leave off
        if(leading<0) {
            temp[2][0] = (currentWidth);
        } else {
            temp[2][0] = (currentWidth + leading); //tx;
        }
        
        temp[2][1] = 0; //ty;
        temp[2][2] = 1;
        Trm = Matrix.multiply(temp, Trm); //multiply to get new Tm
        
        //update Tm to cursor
        currentTextState.Tm[2][0] = Trm[2][0];
        currentTextState.Tm[2][1] = Trm[2][1]-currentTextState.getTextRise();
        
        return Trm;
    }
    
    private StringBuffer setExtractedText(final char lastTextChar, final float x, StringBuffer textData, final boolean hasContent) {
        
        /**roll on if last char is not a space - otherwise restore to before spaces*/
        if (lastTextChar == ' '){
            
            Trm = TrmBeforeSpace;
        }
        
        /**calculate rectangular shape of text*/
        calcCoordinates(x, Trm, charSpacing);
        
        /**
         * if we have an /ActualText use that instead with the width data at start of original
         */
        if(textData!=null && actualText!=null){
            int startValue= textData.indexOf(PdfData.marker,2);
            if(startValue>0) {
                startValue = textData.indexOf(PdfData.marker, startValue + 1);
            }
            
            if(startValue>0){
                textData.setLength(startValue+1); //keep width data but lose text
                textData.append(actualText); //subsitute in /ActualText
            }
            
            actualText=null;
        }
        
        /**return null for no text*/
        if (textData.length() == 0 || !hasContent) //return null if no text
        {
            textData = null;
        }
        
        if (PdfStreamDecoder.showCommands){
            if(textData==null) {
                System.out.println("no data-------------");
            } else{
                System.out.println(" data="+x1+ ' ' +y1+ ',' +x2+ ' ' +y2+ ' ' + org.jpedal.grouping.PdfGroupingAlgorithms.removeHiddenMarkers(textData + "<<"));
            }
        }
        
        return textData;
    }
    
    private void renderText(final float currentWidth, final int type, final int Tmode, final float multiplyer, final boolean isTextShifted) throws RuntimeException {
        
        //get glyph if not CID
        String charGlyph="notdef";
        
        final int  rawInt=glyphData.getRawInt();
        
        try{
            
            if(!currentFontData.isCIDFont()) {
                charGlyph = currentFontData.getMappedChar(rawInt, false);
            }
            
            PdfGlyph glyph;
            
            /**
             * store info needed to create glyph on first render or create now
             */
            if(parserOptions.generateGlyphOnRender() && !parserOptions.renderDirectly()){
                if(glyphData.isfirstTime()){
                    glyph=new MarkerGlyph(Trm[0][0], Trm[0][1], Trm[1][0], Trm[1][1], currentFontData.getBaseFontName());
                    
                    current.checkFontSaved(glyph, currentFontData.getBaseFontName(),currentFontData);
                    glyphData.setFirstTime(false);
                    
                }
                
                currentFontData.setValuesForGlyph(rawInt, charGlyph, glyphData.getDisplayValue(),currentFontData.getEmbeddedChar(rawInt));
                glyph=new UnrendererGlyph(Trm[2][0], Trm[2][1],rawInt,currentWidth);
                
            }else{ //render now 
               
                glyph= glyphs.getEmbeddedGlyph( factory,charGlyph , Trm, rawInt, glyphData.getDisplayValue(), currentWidth, currentFontData.getEmbeddedChar(rawInt));
                
                if (glyph instanceof TTGlyph){
                    
                    //check for dodgy arial and try to replace is SAP created PDF file
                    if(glyph.containsBrokenData()){
                        
                        if(glyphData.getDisplayValue()!=null && !glyphData.getDisplayValue().startsWith("&#")){
                            
                            if(current.isHTMLorSVG()) {
                                current.drawEmbeddedText(Trm, glyphData.getFontSize(), null, null, DynamicVectorRenderer.TEXT, gs, null,glyphData.getDisplayValue(), currentFontData, -100);
                            } else {
                                current.drawText(Trm, glyphData.getDisplayValue(), gs, Trm[2][0], -Trm[2][1], currentFontData.getJavaFontX(glyphData.getFontSize()));
                            }
                        }
                        
                        glyph=null;
                    }else {
                        ttHintingRequired = ttHintingRequired || ((TTGlyph) glyph).isTTHintingRequired();
                    }
                }
            }
            
            //avoid null type 3 glyphs and set color if needed
            if(type==StandardFonts.TYPE3){
                
                if(glyph!=null && glyph.getmaxWidth()==0) {
                    glyph = null;
                } else if(glyph!=null && glyph.ignoreColors()){
                    
                    glyph.setT3Colors(gs.getNonstrokeColor(),gs.getNonstrokeColor(),true);
                }
            }
            
            if(glyph!=null || isHTML){
                
                //set raw width to use for scaling
                if(glyph!=null && type==StandardFonts.TYPE1) {
                    glyph.setWidth(currentWidth * 1000);
                }
                
                float[][] finalTrm={{Trm[0][0], Trm[0][1],0},
                    {Trm[1][0], Trm[1][1] ,0},
                    {Trm[2][0], Trm[2][1],1}};
                
                final float[][] finalScale={{(float) currentFontData.FontMatrix[0],(float) currentFontData.FontMatrix[1],0},
                    {(float)currentFontData.FontMatrix[2],(float)currentFontData.FontMatrix[3],0},
                    {0,0,1}};
                
                //factor in fontmatrix (which may include italic)
                finalTrm=Matrix.multiply(finalTrm, finalScale);
                
                finalTrm[2][0]= Trm[2][0];
                finalTrm[2][1]= Trm[2][1];
                
                //manipulate matrix to get right rotation
                if(finalTrm[1][0]<0 && finalTrm[0][1]<0){
                    finalTrm[1][0]=-finalTrm[1][0];
                    finalTrm[0][1]=-finalTrm[0][1];
                }
                
                //create shape for text using tranformation to make correct size
                final AffineTransform at=new AffineTransform(finalTrm[0][0],finalTrm[0][1],finalTrm[1][0],finalTrm[1][1] ,finalTrm[2][0],finalTrm[2][1]);
                
                //add to renderer
                int fontType=DynamicVectorRenderer.TYPE1C;
                if(type==StandardFonts.OPENTYPE){
                    fontType=DynamicVectorRenderer.TYPE1C;
                    
                    //and fix for scaling in OTF
                    final float z=1000f/(glyph.getmaxWidth());
                    at.scale(currentWidth*z, 1);
                    
                }else if(type==StandardFonts.TRUETYPE || type==StandardFonts.CIDTYPE2 || (currentFontData.isFontSubstituted() && type!=StandardFonts.TYPE1)){
                    fontType=DynamicVectorRenderer.TRUETYPE;
                }else if(type==StandardFonts.TYPE3){
                    fontType=DynamicVectorRenderer.TYPE3;
                }
                
                //negative as flag to show we need to decode later
                if(parserOptions.generateGlyphOnRender()) {
                    fontType = -fontType;
                }
                
                /**
                 * add glyph outline to shape in TR7 mode
                 */
                if((Tmode==GraphicsState.CLIPTEXT)){

                    //will need FX implementation
                    if(glyph!=null && !parserOptions.useJavaFX() && glyph.getShape()!=null){
                        
                        final Area glyphShape=(Area) (glyph.getShape()).clone();
                        
                        /**
                         * some truetype fonts are using the 1000x1000 image in
                         * PDF2Image (viewer works) so this code handles this.
                         *
                         * We need to set clip to actual size of glyf so need to factor out
                         * if TT font larger
                         * (see 13jun/20130031.pdf or case 14645
                         */
                        if(TTGlyph.useHinting && glyph instanceof TTGlyph){
                            
                            glyphShape.transform(AffineTransform.getScaleInstance(0.01,0.01));
                        }
                        
                        glyphShape.transform(at);
                        
                        if(glyphShape.getBounds().getWidth()>0 &&
                                glyphShape.getBounds().getHeight()>0){
                            
                            gs.addClip(glyphShape);
                           
                        }
                    }
                }else {

                    final float lw = gs.getLineWidth();
                    float lineWidth = 0;
                    if (multiplyer > 0) {
                        lineWidth = lw / multiplyer;

                    }

                    final double[] textTrans = new double[6];
                    at.getMatrix(textTrans);
                    
                    gs.setLineWidth((float)(lineWidth/textTrans[0]));
                    
                    if (isTextShifted) {
                        current.drawEmbeddedText(Trm, -glyphData.getFontSize(), glyph, null, fontType, gs, textTrans, glyphData.getUnicodeValue(), currentFontData, -100);
                    } else {
                        current.drawEmbeddedText(Trm, glyphData.getFontSize(), glyph, null, fontType, gs, textTrans, glyphData.getUnicodeValue(), currentFontData, -100);
                    }

                    gs.setLineWidth(lw);
                }
                
            }else{ //if no valid glyph data, treat as a space
                glyphData.set(" ");
            }
        } catch (final Exception e) {
            
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            
            errorTracker.addPageFailureMessage("Exception " + e + " on embedded font renderer");
            
            //
        }
    }
    
    static char getValue(char lastTextChar, GlyphData glyphData, PdfFont currentFontData, DynamicVectorRenderer current, ParserOptions parserOptions) {
        
        final String newValue=currentFontData.getGlyphValue(glyphData.getRawInt());
        glyphData.setDisplayValue(newValue);
        
        /**
         * remap chars for HTML,etc - not needed and breaks other code
         * needed to fix glyph mapping issue in odd file for PDF2HTML5
         * /sample_pdfs_html/thoughtcorp/Simple Relational Contracts.pdf
         */
        boolean alreadyRemaped=false;
        
        if((newValue.isEmpty() || (!newValue.isEmpty() && newValue.charAt(0)<32)) && current.isHTMLorSVG() && !currentFontData.isCIDFont()){
            alreadyRemaped=HTMLTextUtils.remapGlyph(currentFontData, glyphData);
        }
        
        final int rawInt=glyphData.getRawInt();
        
        //if space is actually mapped onto something else we need to reset
        //this variable which tracks space chars (as false match)
        if(rawInt==32 && !glyphData.getDisplayValue().equals(" ")){
            lastTextChar='Z';
            //rawChar='Z';
        }
        
        if(!alreadyRemaped && parserOptions.isTextExtracted()) {
           glyphData.setUnicodeValue(currentFontData.getUnicodeValue(glyphData.getDisplayValue(),rawInt));
        }
        
        //fix for character wrong in some T1 fonts
        if(currentFontData.getFontType()==StandardFonts.TYPE1 && current.isHTMLorSVG()){
            final String possAltValue = currentFontData.getMappedChar(rawInt,true);
            if(possAltValue!=null && possAltValue.length()==1 && possAltValue.equalsIgnoreCase(glyphData.getUnicodeValue().toLowerCase())){
                glyphData.set(possAltValue);
            }
        }
        return lastTextChar;
    }
    
    static int calcFontSize(GlyphData glyphData, TextState currentTextState, float[][] Trm) throws RuntimeException {
        
        int fontSize;
        
        /**workout if horizontal or vertical plot and set values*/
        if (Trm[1][1] != 0) {
            glyphData.setHorizontal(true);
            currentTextState.writingMode = PdfData.HORIZONTAL_LEFT_TO_RIGHT;
            
            if(Trm[1][1]<0) {
                fontSize = (int) (Trm[1][1] - 0.5f);
            } else {
                fontSize = (int) (Trm[1][1] + 0.5f);
            }
            
            if(fontSize==0){
                
                if(Trm[0][1]<0) {
                    fontSize = (int) (Trm[0][1] - 0.5f);
                } else {
                    fontSize = (int) (Trm[0][1] + 0.5f);
                }
            }
            
            glyphData.setFontScale(Trm[0][0]);
            
            //allow for this odd case in 20090818_Mortgage Key Issue Packag .pdf
            if(Trm[0][0]==0 && Trm[0][1]>0 && Trm[1][0]<0 && Trm[1][1]>0) {
                currentTextState.writingMode = PdfData.VERTICAL_BOTTOM_TO_TOP;
            }
            
        } else {
            
            glyphData.setHorizontal(false);
            
            if(Trm[1][0]<0) {
                fontSize = (int) (Trm[1][0] - 0.5f);
            } else {
                fontSize = (int) (Trm[1][0] + 0.5f);
            }
            
            if(fontSize==0){
                if(Trm[0][0]<0) {
                    fontSize = (int) (Trm[0][0] - 0.5f);
                } else {
                    fontSize = (int) (Trm[0][0] + 0.5f);
                }
            }
            
            if(fontSize<0){
                fontSize=-fontSize;
                currentTextState.writingMode = PdfData.VERTICAL_BOTTOM_TO_TOP;
            }else {
                currentTextState.writingMode = PdfData.VERTICAL_TOP_TO_BOTTOM;
            }
            
            glyphData.setFontScale(Trm[0][1]);
        }
        
        if(fontSize==0) {
            fontSize = 1;
        }
        
        glyphData.setFontSize(fontSize);
        
        return fontSize;
    }
    
    /**
     * add text chars to our text object for extraction
     * @param hasContent
     * @param currentWidth
     * @param textData
     * @param spaces
     * @return
     */
    static boolean writeOutText(GlyphData glyphData, float[][] Trm,boolean hasContent, final float currentWidth, final StringBuffer textData, final String spaces) {
        
        final String unicodeValue=glyphData.getUnicodeValue();
        final float fontScale=glyphData.getFontScale();
        
        if(!unicodeValue.isEmpty()) {
            
            //add character to text we have decoded with width
            //if large space separate out
            if (DecoderOptions.embedWidthData) {
                
                final float xx=Trm[2][0];
                final float yy=Trm[2][1];
                
                textData.append(spaces);
                
                //embed width information in data
                if(glyphData.isHorizontal()){
                    textData.append(PdfData.marker);
                    textData.append(xx);
                    textData.append(PdfData.marker);
                    
                }else{
                    textData.append(PdfData.marker);
                    textData.append(yy);
                    textData.append(PdfData.marker);
                }
                
                textData.append(currentWidth * fontScale);
                
                textData.append(PdfData.marker);
                
            }else {
                textData.append(spaces);
            }
            
            /**add data to output*/
            
            //turn chars less than 32 into escape
            final int length=unicodeValue.length();
            char next;
            boolean isXMLExtraction=glyphData.isXMLExtraction();
            for (int ii = 0; ii < length; ii++) {
                next = unicodeValue.charAt(ii);
                
                hasContent=true;
                
                //map tab to space
                if(next==9) {
                    next = 32;
                }
                
                if(next=='<' && isXMLExtraction) {
                    textData.append("&lt;");
                } else if(next=='>' && isXMLExtraction) {
                    textData.append("&gt;");
                } else if(next==64258) {
                    textData.append("fl");
                } else if (next > 31) {
                    textData.append(next);
                } else {
                    textData.append(hex[next]);
                }
            }
        }else {
            textData.append(spaces);
        }
        
        return hasContent;
    }
    
    public boolean setExtractedText(final float currentWidth,boolean resetCoords) {
        
        final String displayValue=glyphData.getDisplayValue();
        
        if(!displayValue.isEmpty() && !displayValue.equals(" ")){
            
            float xx=((int)Trm[2][0]);
            float yy=((int)Trm[2][1]);
            float ww=(currentWidth * glyphData.getFontScale());
            
            float hh=(Trm[1][1]);
            if(hh==0) {
                hh = (Trm[0][1]);
            }
            
            //correct silly figures used in T3 font on some scanned pages
            if(currentFontData.getFontType()==StandardFonts.TYPE3 && hh!=0 && ((int)hh)==0 && currentFontData.FontMatrix[3]==-1){
                hh *= (currentFontData.FontBBox[3] - currentFontData.FontBBox[1]);
                hh=-hh;
            }
            
            hh=(int)hh;
            
            if(ww<0){
                ww=-ww;
                xx -= ww;
            }
            if(hh<0){
                hh=-hh;
                yy -= hh;
            }
            
            final Rectangle fontbb = currentFontData.getBoundingBox();
            
            //this fixes odd font
            if(fontbb.y<0){
                fontbb.height -= fontbb.y;
                fontbb.y=0;
            }
            
            float fy = fontbb.y;
            if(fy==0) //If no y set it may be embedded so we should guess a value
            {
                fy = 100;
            }
            if(fy<0) {
                fy = -fy;
            }
            
            float h = 1000+(fy);
            //Percentage of fontspace used compared to default
            h = 1000/h;
            final float fontHeight;
            switch(currentTextState.writingMode){
                case PdfData.HORIZONTAL_LEFT_TO_RIGHT :
                    fontHeight = (hh/h);
                    yy -= (fontHeight - hh);
                    hh = fontHeight;
                    break;
                case PdfData.HORIZONTAL_RIGHT_TO_LEFT :
                    System.out.println("THIS TEXT DIRECTION HAS NOT BEEN IMPLEMENTED YET (Right to Left)");
                    
                    break;
                case PdfData.VERTICAL_TOP_TO_BOTTOM :
                    fontHeight = (ww/h);
                    xx -= (fontHeight - ww);
                    ww = fontHeight;
                    break;
                case PdfData.VERTICAL_BOTTOM_TO_TOP :
                    fontHeight = (ww/h);
                    xx -= fontHeight;
                    ww = fontHeight;
                    break;
            }
            
            //Highlight area around text so increase x coord
            xx -= 1;
            ww += 2;
            
            /**
             * Calculate the y coords for text here
             * x coords are calculated in the method
             * calcCoordinates(float x, float[][] rawTrm, boolean horizontal, float max_height, int fontSize, float y)
             */
            if(resetCoords){
                y2 = yy;
                
                y1 = yy+hh;
                resetCoords = false;
            }
            
            if(yy<y2) {
                y2 = yy;
            }
            
            if((yy+hh)>y1) {
                y1 = (yy + hh);
            }
            
            if(textAreas!=null && parserOptions.isRenderText() ){
                
                textAreas.addElement(new int[]{(int)xx ,(int)yy ,(int)ww ,(int)hh});
                textDirections.addElement(currentTextState.writingMode);
            }
        }
        return resetCoords;
    }
    
    public boolean isTTHintingRequired() {
        return ttHintingRequired;
    }
    
    public void setReturnText(final boolean returnText) {
        this.returnText=returnText;
    }
    
    public void setActualText(String actualText) {
       this.actualText=actualText;
    }
    
}
