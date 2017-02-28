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
 * BaseTTGlyph.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.util.HashSet;
import javafx.scene.shape.Path;
import org.jpedal.color.PdfPaint;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.tt.hinting.TTVM;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.*;


public abstract class BaseTTGlyph {

    /**paths for the letter, marked as transient so it wont be serialized */
    transient Vector_Path paths=new Vector_Path(10);

    protected boolean ttHintingRequired;

    public static boolean useHinting = true;

    protected boolean containsBrokenGlyfData;

    protected short compMinX, compMinY, compMaxX, compMaxY;

    protected short minX,minY,maxX,maxY;

    protected int[] scaledX, scaledY;

    protected int BPoint1, BPoint2;

    protected final Vector_Int xtranslateValues=new Vector_Int(5);

    protected final Vector_Int ytranslateValues=new Vector_Int(5);

    protected final short leftSideBearing;

    protected final Vector_Double xscaleValues=new Vector_Double(5);

    protected final Vector_Double yscaleValues=new Vector_Double(5);

    protected final Vector_Double scale01Values=new Vector_Double(5);

    protected final Vector_Double scale10Values=new Vector_Double(5);

    protected double xscale=1,yscale=1,scale01,scale10;

    protected int[] instructions;

    protected int xtranslate,ytranslate;

    protected int currentInstructionDepth=Integer.MAX_VALUE;

    protected final Vector_Object glyfX=new Vector_Object(5);

    protected final Vector_Object glyfY=new Vector_Object(5);

    protected final Vector_Object curves=new Vector_Object(5);

    protected final Vector_Object contours=new Vector_Object(5);

    protected final Vector_Int endPtIndices=new Vector_Int(5);

    protected int contourCount;

    protected float unitsPerEm=64;

    public static boolean debug;

    protected int glyphNumber = -1;

    //used to track which glyf for complex glyph
    protected int compCount=1;

    protected boolean isComposite;

    protected double pixelSize;

    /**Variables used by the constructor**/
    private static final HashSet<String> testedFonts = new HashSet<String>();

    int BP1x=-1,BP2x=-1,BP1y=-1,BP2y=-1;

    //Track translations for recursion
    int existingXTranslate;
    int existingYTranslate;
    int depth;

    static {
        final String value = System.getProperty("org.jpedal.useTTFontHinting");
        if (value != null) {
            useHinting = value.equalsIgnoreCase("true");
        }
    }

    public static boolean redecodePage;

    boolean isHinted;

    private TTVM vm;

    private String baseFontName;

    /**
     * Hinted constructor
     */
    public BaseTTGlyph(final Glyf currentGlyf, final FontFile2 glyfTable, final Hmtx currentHmtx, final int idx, final float unitsPerEm, final TTVM vm){

        glyphNumber = idx+1;

        isHinted=true;

        //this.glyfName=glyfName;
        //this.idx=idx;
        this.leftSideBearing =currentHmtx.getLeftSideBearing(idx);
        //this.advanceWidth = currentHmtx.getAdvanceWidth(idx);
        this.unitsPerEm=unitsPerEm;

        final int p=currentGlyf.getCharString(idx);

        
        glyfTable.setPointer(p);

        if(glyfTable.getBytesLeft()>4){
            readGlyph(currentGlyf,glyfTable);

            this.vm=vm;

            //if(!useFX)
            createGlyph(isHinted);
        }
      
    }


    /**
     * Unhinted constructor
     */
    public BaseTTGlyph(final Glyf currentGlyf, final FontFile2 glyfTable, final Hmtx currentHmtx, final int idx, final float unitsPerEm, final String baseFontName){

        //debug=idx==2246;

        glyphNumber = idx+1;

       // this.glyfName=glyfName;
        //this.idx=idx;
        this.leftSideBearing =currentHmtx.getLeftSideBearing(idx);
        //this.advanceWidth = currentHmtx.getAdvanceWidth(idx);
        this.unitsPerEm=unitsPerEm;

        this.baseFontName=baseFontName;

        final int p=currentGlyf.getCharString(idx);
        
        glyfTable.setPointer(p);

        if(glyfTable.getBytesLeft()>4){
        
            readGlyph(currentGlyf,glyfTable);

            //if(!useFX)
            createGlyph(false);
        }
       
    }


    void createGlyph(final boolean isHinted){

        if(isHinted){
            createHintedGlyph();
        }else{
            createUnhintedGlyph();
        }
    }

    void createUnhintedGlyph() {

        /**create glyphs the first time*/
        for(int i=0;i<this.compCount;i++){

            final int[] pX=(int[]) glyfX.elementAt(i);
            final int[] pY=(int[]) glyfY.elementAt(i);
            final boolean[] onCurve=(boolean[]) curves.elementAt(i);
            final boolean[] endOfContour=(boolean[])contours.elementAt(i);
            final int endIndex=endPtIndices.elementAt(i);

            if(isComposite){
                xtranslate=xtranslateValues.elementAt(i);
                ytranslate=ytranslateValues.elementAt(i);
                xscale=xscaleValues.elementAt(i);
                yscale=yscaleValues.elementAt(i);
                scale01=scale01Values.elementAt(i);
                scale10=scale10Values.elementAt(i);

                //factor in BPoint where points overlap
                if(BPoint1!=-1 && BPoint2!=-1){
                    if(BP1x==-1 && BP2x==-1 && BP1y==-1 && BP2y==-1){ //first point
                        BP1x=pX[BPoint1];
                        BP1y=pY[BPoint1];

                    }else{ //second and reset

                        BP2x=pX[BPoint2];
                        BP2y=pY[BPoint2];

                        final int xx=BP1x-BP2x;
                        final int yy=BP1y-BP2y;

                        final int count=pX.length;
                        for(int ii=0;ii<count;ii++){
                            pX[ii] += xx;

                            if(debug) {
                                System.out.println(pY[ii] + " " + yy + " BP1y=" + BP1y + " BP1y=" + BP1y);
                            }
                            pY[ii] += yy;
                        }

                        //reset for next
                        BP1x=-1;
                        BP2x=-1;
                        BP1y=-1;
                        BP2y=-1;
                    }
                }
            }

            //Check if it's a font which uses instructions to move subglyphs
            if (baseFontName!=null && instructions != null && !testedFonts.contains(baseFontName)) {

                testedFonts.add(baseFontName);

                baseFontName = baseFontName.toLowerCase();

                if (baseFontName.contains("mingli") ||
                        baseFontName.contains("kai") ||
                        baseFontName.contains("huatian")) {

                    ttHintingRequired = true;

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("TrueType hinting probably required for font " + baseFontName);
                    }
                }
            }

            //		    drawGlyf(pX,pY,onCurve,endOfContour,endIndex,debug);
            createPaths(pX,pY,onCurve,endOfContour,endIndex);
        }
    }

    /**
     * returns the middle point between two values
     */
    static int midPt(final int a, final int b) {
        return a + (b - a)/2;
    }

    /* (non-Javadoc)
     * @see org.jpedal.fonts.PdfGlyph#getWidth()
     */
    public float getmaxWidth() {

        return 0;
    }

    /* (non-Javadoc)
     * @see org.jpedal.fonts.PdfGlyph#setT3Colors(java.awt.Color, java.awt.Color)
     */
    public void setT3Colors(final PdfPaint strokeColor, final PdfPaint nonstrokeColor, final boolean lockColours) {

    }

    /* (non-Javadoc)
     * @see org.jpedal.fonts.PdfGlyph#ignoreColors()
     */
    public boolean ignoreColors() {
        return false;
    }

    /**create the actual shape
     * @param pX*/
    public void scaler(final int[] pX, final int[] pY){

        scaledX = new int[pX.length];
        scaledY = new int[pY.length];

        final double scale = (pixelSize / (unitsPerEm*1000)) * 64;

        for (int i=0; i<pX.length; i++) {
            scaledX[i] = (int)((scale * pX[i])+0.5);
            scaledY[i] = (int)((scale * pY[i])+0.5);
        }

        scaledX[pX.length-2] = 0;
        scaledY[pY.length-2] = 0;
        scaledX[pX.length-1] = (int)((scale * leftSideBearing)+0.5);
        scaledY[pY.length-1] = 0;

    }


    public final void readComplexGlyph(final Glyf currentGlyf, final FontFile2 currentFontFile){

        isComposite=true;

        //Remove elements for the compound as it's only a container
        xtranslateValues.pull();
        ytranslateValues.pull();
        xscaleValues.pull();
        yscaleValues.pull();
        scale01Values.pull();
        scale10Values.pull();


        BPoint1=-1;
        BPoint2=-1;

        //LogWriter.writeMethod("{readComplexGlyph}", 0);

        boolean WE_HAVE_INSTRUCTIONS = false;

        final int count=currentGlyf.getGlypfCount();

        while(true){
            final int flag=currentFontFile.getNextUint16();
            final int glyphIndex=currentFontFile.getNextUint16();

            if(debug) {
                System.err.println("Index=" + glyphIndex + " flag=" + flag + ' ' + count);
            }

            //allow for bum data
            if(glyphIndex>=count){
                containsBrokenGlyfData=true;
                break;
            }


            //set flag options
            final boolean ARG_1AND_2_ARE_WORDS= (flag & 1)==1;
            final boolean ARGS_ARE_XY_VALUES= (flag & 2) ==2;
            final boolean WE_HAVE_A_SCALE=(flag & 8) ==8;
            final boolean WE_HAVE_AN_X_AND_Y_SCALE=(flag & 64) ==64;
            final boolean WE_HAVE_A_TWO_BY_TWO=(flag & 128) ==128;
            WE_HAVE_INSTRUCTIONS = WE_HAVE_INSTRUCTIONS || (flag & 256) == 256;

            //<start-demo><end-demo>


            if (ARG_1AND_2_ARE_WORDS && ARGS_ARE_XY_VALUES){
                //1st short contains the value of e
                //2nd short contains the value of f
                xtranslate=currentFontFile.getNextInt16();
                ytranslate=currentFontFile.getNextInt16();
            }else if (!ARG_1AND_2_ARE_WORDS && ARGS_ARE_XY_VALUES){
                //1st byte contains the value of e
                //2nd byte contains the value of f
                xtranslate=currentFontFile.getNextint8();
                ytranslate=currentFontFile.getNextint8();
            }else if (ARG_1AND_2_ARE_WORDS && !ARGS_ARE_XY_VALUES){
                //1st short contains the index of matching point in compound being constructed
                //2nd short contains index of matching point in component
                BPoint1 =currentFontFile.getNextUint16();
                BPoint2 =currentFontFile.getNextUint16();
                xtranslate=0;
                ytranslate=0;

            }else if (!ARG_1AND_2_ARE_WORDS && !ARGS_ARE_XY_VALUES){
                // 1st byte containing index of matching point in compound being constructed
                // 2nd byte containing index of matching point in component
                BPoint1 =currentFontFile.getNextUint8();
                BPoint2 =currentFontFile.getNextUint8();
                xtranslate=0;
                ytranslate=0;

            }

            //set defaults
            xscale=1; //a
            scale01=0; //b
            scale10=0; //c
            yscale=1; //d

            /**workout scaling factors*/
            if((!WE_HAVE_A_SCALE)&&(!WE_HAVE_AN_X_AND_Y_SCALE)&&(!WE_HAVE_A_TWO_BY_TWO)){
                //uses defaults already set

            }else if((WE_HAVE_A_SCALE)&&(!WE_HAVE_AN_X_AND_Y_SCALE)&&(!WE_HAVE_A_TWO_BY_TWO)){

                xscale=currentFontFile.getF2Dot14(); //a
                scale01=0; //b
                scale10=0; //c
                yscale=xscale; //d
            }else if((!WE_HAVE_A_SCALE)&&(WE_HAVE_AN_X_AND_Y_SCALE)&&(!WE_HAVE_A_TWO_BY_TWO)){

                xscale=currentFontFile.getF2Dot14(); //a
                scale01=0; //b
                scale10=0; //c
                yscale=currentFontFile.getF2Dot14(); //d

            }else if((!WE_HAVE_A_SCALE)&&(!WE_HAVE_AN_X_AND_Y_SCALE)&&(WE_HAVE_A_TWO_BY_TWO)){

                xscale=currentFontFile.getF2Dot14(); //a
                scale01=currentFontFile.getF2Dot14(); //b
                scale10=currentFontFile.getF2Dot14(); //c
                yscale=currentFontFile.getF2Dot14(); //d
            }

            //store so we can remove later
            final int localX = xtranslate;
            final int localY = ytranslate;

            //Get total translation
            xtranslate += existingXTranslate;
            ytranslate += existingYTranslate;

            //save values
            xtranslateValues.addElement(xtranslate);
            ytranslateValues.addElement(ytranslate);
            xscaleValues.addElement(xscale);
            yscaleValues.addElement(yscale);
            scale01Values.addElement(scale01);
            scale10Values.addElement(scale10);

            //save location so we can restore
            final int pointer=currentFontFile.getPointer();
            
            /**/
            //now read the simple glyphs
            int p=currentGlyf.getCharString(glyphIndex);

            if(p!=-1){
                if(p<0) {
                    p = -p;
                }
                currentFontFile.setPointer( p);
                existingXTranslate = xtranslate;
                existingYTranslate = ytranslate;
                depth++;
                readGlyph(currentGlyf,currentFontFile);
                depth--;
                existingXTranslate -= localX;
                existingYTranslate -= localY;
            }else{
                System.err.println("Wrong value in complex");
            }

            currentFontFile.setPointer(pointer);

            //break out at end
            if((flag & 32) ==0) {

                if (WE_HAVE_INSTRUCTIONS) {
                    final int instructionLength = currentFontFile.getNextUint16();
                    final int[] instructions = new int[instructionLength];
                    for (int i=0; i< instructionLength; i++) {
                        instructions[i] = currentFontFile.getNextUint8();
                    }
                    if (depth <= currentInstructionDepth) {
                        this.instructions = instructions;
                        currentInstructionDepth=depth;
                    }
                } else {
                    if (depth <= currentInstructionDepth) {
                        this.instructions = new int[]{};
                        currentInstructionDepth=depth;
                    }
                }

                break;
            }

            compCount++;
        }
    }

    public void readSimpleGlyph(final FontFile2 currentFontFile){

        //LogWriter.writeMethod("{readSimpleGlyph}", 0);

        int flagCount=1;

        short x1  ;

        final Vector_Int rawFlags=new Vector_Int(50);
        final Vector_Int endPts=new Vector_Int(50);
        final Vector_Short XX=new Vector_Short(50);
        final Vector_Short Y=new Vector_Short(50);



        //all endpoints
        if(debug){
            System.out.println("endPoints");
            System.out.println("---------");
        }

        try{

            int lastPt=0;
            for(int i=0;i<contourCount;i++){


                lastPt=currentFontFile.getNextUint16();

                if(debug) {
                    System.out.println(i + " " + lastPt);
                }

                endPts.addElement(lastPt);

            }

            //allow for corrupted value with not enough entries
            //ie customers3/ICG3Q03.pdf
            if(currentFontFile.hasValuesLeft()){


                /**Don;t comment out !!!!!!!!!
                 * needs to be read to advance pointer*/
                final int instructionLength=currentFontFile.getNextUint16();


                final int[] instructions=new int[instructionLength];
                for(int i=0;i<instructionLength;i++) {
                    instructions[i] = currentFontFile.getNextUint8();
                }

                if (depth < currentInstructionDepth) {
                    this.instructions = instructions;
                }

                if(debug){
                    System.out.println("Instructions");
                    System.out.println("------------");
                    System.out.println("count="+instructionLength);
                }

                int count = lastPt+ 1;
                int flag;

                /**we read the flags (some can repeat)*/
                for (int i = 0; i < count; i++) {
                    if(currentFontFile.getBytesLeft()<1){
                        return;
                    }
                    flag=currentFontFile.getNextUint8();
                    rawFlags.addElement(flag);
                    flagCount++;

                    if ((flag & 8) == 8) { //repeating flags        							}
                        final int repeatCount = currentFontFile.getNextUint8();
                        for (int r = 1; r <= repeatCount; r++) {
                            rawFlags.addElement(flag);
                            flagCount++;
                        }
                        i += repeatCount;
                    }
                }

                /**read the x values and set segment for complex glyph*/
                for(int i=0;i<count;i++){
                    flag=rawFlags.elementAt(i);

                    //boolean twoByteValue=((flag  & 2)==0);
                    if ((flag & 16) != 0) { //
                        if ((flag & 2) != 0) { //1 byte + value
                            x1=(short)currentFontFile.getNextUint8();
                            XX.addElement(x1);
                        }else{ //2 byte value - same as previous - ??? same X coord or value
                            XX.addElement((short)0);
                        }

                    } else {
                        if ((flag  & 2) != 0){ //1 byte - value
                            x1=(short)-currentFontFile.getNextUint8();
                            XX.addElement(x1);
                        }else{ //signed 16 bit delta vector
                            x1=currentFontFile.getNextSignedInt16();
                            XX.addElement(x1);
                        }
                    }
                }

                /**read the y values*/
                for(int i=0;i<count;i++){
                    flag=rawFlags.elementAt(i);
                    if ((flag & 32) != 0) {
                        if ((flag & 4) != 0) {
                            if(currentFontFile.getBytesLeft()<1){
                                return;
                            }
                            Y.addElement((short)currentFontFile.getNextUint8());
                        } else {
                            Y.addElement((short)0);
                        }
                    } else {
                        if ((flag & 4) != 0) {
                            Y.addElement((short)-currentFontFile.getNextUint8());
                        } else {
                            final short val=currentFontFile.getNextSignedInt16();
                            Y.addElement(val);
                        }
                    }
                }


                /**
                 * calculate the points
                 */
                int endPtIndex = 0;
                int x=0,y=0;

                final int[] flags=rawFlags.get();

                final int[] endPtsOfContours=endPts.get();
                final short[] XPoints=XX.get();
                final short[] YPoints=Y.get();
                count=XPoints.length;

                final int[] pX=new int[count+2];
                final int[] pY=new int[count+2];
                final boolean[] onCurve=new boolean[count+2];
                final boolean[] endOfContour=new boolean[count+2];

                int endIndex=0;

                if(debug){
                    System.out.println("Points");
                    System.out.println("------");
                }
                for (int i = 0; i < count; i++) {

                    final boolean endPt = endPtsOfContours[endPtIndex] == i;
                    if (endPt){
                        endPtIndex++;
                        endIndex=i+1;
                    }
                    x +=XPoints[i];
                    y +=YPoints[i];

                    pX[i]=x;
                    pY[i]=y;

                    onCurve[i] = i < flagCount && (flags[i] & 1) != 0;

                    endOfContour[i]=endPt;

                    if(debug) {
                        System.out.println(i + " " + pX[i] + ' ' + pY[i] + " on curve=" + onCurve[i] + " endOfContour[i]=" + endOfContour[i]);
                    }


                }

                for (int i=0; i<pX.length; i++) {
                    final int lX = pX[i];
                    final int lY = pY[i];

                    //Convert x
                    //pX[i] = convertX(lX,lY);
                    if(!isComposite) {
                        if (!useHinting) {
                            pX[i] = (int) (lX / unitsPerEm);
                        } else {
                            pX[i] = lX;
                        }
                    } else {
                        if (!useHinting) {
                            pX[i] = (int) ((((lX * xscale) + (lY * scale10)) + xtranslate) / unitsPerEm);
                        } else {
                            pX[i] = (int) ((((lX * xscale) + (lY * scale10)) + xtranslate));
                        }
                    }


                    //Convert Y
                    //pY[i] = convertY(lX,lY);
                    if(!isComposite) {
                        if (!useHinting) {
                            pY[i] = (int) (lY / unitsPerEm);
                        } else {
                            pY[i] = lY;
                        }
                    } else {
                        if (!useHinting) {
                            pY[i] = (int) ((((lX * scale01) + (lY * yscale)) + ytranslate) / unitsPerEm);
                        } else {
                            pY[i] = (int) ((((lX * scale01) + (lY * yscale)) + ytranslate));
                        }
                    }
                }

                //store
                glyfX.addElement(pX);
                glyfY.addElement(pY);
                this.curves.addElement(onCurve);
                this.contours.addElement(endOfContour);
                this.endPtIndices.addElement(endIndex);


            }
        }
        catch(final Exception e){
            //System.err.println("error occured while reading TTGlyph bytes");
            //there are many files in which the glyph length is not matched with specification
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Caught an Exception while reading TTGlyph bytes" + e);
            }
        }
    }


    public void readGlyph(final Glyf currentGlyf, final FontFile2 currentFontFile){

        //LogWriter.writeMethod("{readGlyph}", 0);

        contourCount=currentFontFile.getNextUint16();

        //read the max/min co-ords
        minX=(short)currentFontFile.getNextUint16();
        minY=(short)currentFontFile.getNextUint16();
        maxX=(short)currentFontFile.getNextUint16();
        maxY=(short)currentFontFile.getNextUint16();

        if(minX>maxX || minY>maxY){
            return;
        }

        if(debug){
            System.out.println("------------------------------------------------------------");
            System.out.println("min="+minX+ ' ' +minY+" max="+maxX+' '+maxY+" contourCount="+contourCount);
        }

        if(contourCount!=65535){
            if(contourCount>0){
                readSimpleGlyph(currentFontFile);
            }
        }else{

            compMinX=minX;
            compMinY=minY;
            compMaxX=maxX;
            compMaxY=maxY;

            if(debug) {
                System.out.println("XXmain=" + minX + ' ' + minY + ' ' + maxX + ' ' + maxY);
            }

            readComplexGlyph(currentGlyf,currentFontFile);
        }


    }

    /**
     * @return Whether the font requires hinting in order to correctly arrange its subglyphs
     */
    public boolean isTTHintingRequired(){
        return ttHintingRequired;
    }

    public void createPaths(final int[] pX, final int[] pY, final boolean[] onCurve, final boolean[] endOfContour, final int endIndex) {
        throw new UnsupportedOperationException("createPaths Not supported yet.");
    }

    void clearPaths() {
        throw new UnsupportedOperationException("clearPaths Not supported yet.");
    }

    public void render(final int text_fill_type, final Graphics2D g2, final float scaling, final boolean isFormGlyph) {
        throw new UnsupportedOperationException("render Not supported yet."); 
    }

    public void setWidth(final float width) {

    }

    public int getFontBB(final int type) {

        if(isComposite){
            if(type== PdfGlyph.FontBB_X) {
                return compMinX;
            } else if(type== PdfGlyph.FontBB_Y) {
                return compMinY;
            } else if(type== PdfGlyph.FontBB_WIDTH) {
                return compMaxX;
            } else if(type== PdfGlyph.FontBB_HEIGHT) {
                return compMaxY;
            } else {
                return 0;
            }
        }else{
            if(type== PdfGlyph.FontBB_X) {
                return minX;
            } else if(type== PdfGlyph.FontBB_Y) {
                return minY;
            } else if(type== PdfGlyph.FontBB_WIDTH) {
                return maxX;
            } else if(type== PdfGlyph.FontBB_HEIGHT) {
                return maxY;
            } else {
                return 0;
            }
        }
    }

    public void setStrokedOnly(final boolean b) {
        //not used here
    }

    //use by TT to handle broken TT fonts
    public boolean containsBrokenData() {
        return this.containsBrokenGlyfData;
    }

    public Path getPath() {
        throw new UnsupportedOperationException("getPath Not supported yet."); 
    }

    public int getGlyphNumber() {
        return glyphNumber;
    }

    public void setGlyphNumber(final int no) {
        glyphNumber = no;
    }

    public Area getShape() {
        throw new UnsupportedOperationException("getShape Not supported yet.");
    }

    void createHintedGlyph() {

        /**create glyphs the first time*/
        for(int i=0;i<this.compCount;i++){

            final int[] pX=(int[]) glyfX.elementAt(i);
            final int[] pY=(int[]) glyfY.elementAt(i);

            if(isComposite){
                xtranslate=xtranslateValues.elementAt(i);
                ytranslate=ytranslateValues.elementAt(i);
                xscale=xscaleValues.elementAt(i);
                yscale=yscaleValues.elementAt(i);
                scale01=scale01Values.elementAt(i);
                scale10=scale10Values.elementAt(i);

                //factor in BPoint where points overlap
                if(BPoint1!=-1 && BPoint2!=-1){
                    if(BP1x==-1 && BP2x==-1 && BP1y==-1 && BP2y==-1){ //first point
                        BP1x=pX[BPoint1];
                        BP1y=pY[BPoint1];

                    }else{ //second and reset

                        BP2x=pX[BPoint2];
                        BP2y=pY[BPoint2];

                        final int xx=BP1x-BP2x;
                        final int yy=BP1y-BP2y;

                        final int count=pX.length;
                        for(int ii=0;ii<count;ii++){
                            pX[ii] += xx;

                            if(debug) {
                                System.out.println(pY[ii] + " " + yy + " BP1y=" + BP1y + " BP1y=" + BP1y);
                            }
                            pY[ii] += yy;
                        }

                        //reset for next
                        BP1x=-1;
                        BP2x=-1;
                        BP1y=-1;
                        BP2y=-1;
                    }
                }
            }
        }


        //Use a preset size to generate at
        pixelSize = 1000 * 100 / 64d;

        int coordCount=2;
        final int[] componentLengths = new int[this.compCount];
        for (int i=0; i<this.compCount; i++) {
            coordCount += endPtIndices.elementAt(i);
            componentLengths[i] = endPtIndices.elementAt(i);
        }



        //Combine sets of glyph points to get whole glyph
        final int[] allX = new int[coordCount];
        final int[] allY = new int[coordCount];
        final int[] unscaledX = new int[coordCount];
        final int[] unscaledY = new int[coordCount];
        final boolean[] allOnCurve = new boolean[coordCount];
        final boolean[] allEndOfContour = new boolean[coordCount];
        final int[] allEndIndex = new int[this.compCount];

        int offset=0;
        for (int i=0; i<this.compCount; i++) {
            final int[] pX = (int[]) glyfX.elementAt(i);
            final int[] pY = (int[]) glyfY.elementAt(i);
            final boolean[] onCurve = (boolean[])curves.elementAt(i);
            final boolean[] endOfContour = (boolean[])contours.elementAt(i);
            final int endIndex = endPtIndices.elementAt(i);

            //This is the ideal place to specify to only print one glyph. Use the unscaled coordinates of the first
            // point of the relevant glyph, which can be easily found using FontForge.
            //                if (i==0 && !(pY[0] == 921 && pX[0] == 317))
            //                    return;

            //Scale raw coords
            if(pX!=null) {
                scaler(pX, pY);
            }

            for (int j=0; j<componentLengths[i]; j++) {
                allX[offset+j] = scaledX[j];
                allY[offset+j] = scaledY[j];
                unscaledX[offset+j] = pX[j];
                unscaledY[offset+j] = pY[j];
                allOnCurve[offset+j] = onCurve[j];
                allEndOfContour[offset+j] = endOfContour[j];
                allEndIndex[i] = endIndex;
            }
            offset+=componentLengths[i];
        }

        final double scaler = (pixelSize / (unitsPerEm*1000)) * 64;

        allX[allX.length-1] = (int)((scaler*leftSideBearing)+0.5);

        //prepare VM and process glyph
        vm.setScaleVars(scaler, pixelSize, (pixelSize*72)/96);
        vm.processGlyph(instructions, allX, allY, allOnCurve, allEndOfContour);

        //Split back into individual glyphs and create paths
        clearPaths();

        offset=0;
        for (int i=0; i<this.compCount; i++) {
            final int[] thisX = (int[])glyfX.elementAt(i);
            final int[] thisY = (int[])glyfY.elementAt(i);
            final boolean[] onCurve = (boolean[])curves.elementAt(i);
            final boolean[] endOfContour = (boolean[])contours.elementAt(i);
            final int endIndex=allEndIndex[i];
            for (int j=0; j<componentLengths[i]; j++) {
                thisX[j] = allX[offset+j];
                thisY[j] = allY[offset+j];
                onCurve[j] = allOnCurve[offset+j];
                endOfContour[j] = allEndOfContour[offset+j];
            }

            createPaths(thisX,thisY,onCurve,endOfContour,endIndex);
            offset += componentLengths[i];
        }

    }






}
