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
 * ParserOptions.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.PdfDecoderInt;
import org.jpedal.external.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import org.jpedal.objects.layers.PdfLayerList;

import org.jpedal.objects.structuredtext.StructuredContentHandler;

public class ParserOptions {

     /**flag to show if YCCK images*/
    public boolean hasYCCKimages;

    public boolean imagesProcessedFully;

    private boolean isLayerVisible=true;

    private int layerLevel;

    private Set<Integer> layerVisibility=new HashSet(50);

    public final Set<Integer> layerClips=new HashSet<Integer>(50);

    public PdfLayerList layers;

    protected boolean isPageContent;
    protected boolean renderPage;

    private boolean isFlattenedForm;

    /**
     * if forms flattened, different calculation needed
     */
    private float flattenX, flattenY;

    private boolean isPrinting;
    
    protected float samplingUsed=-1;

    protected org.jpedal.objects.structuredtext.StructuredContentHandler contentHandler;
    
    private int textPrint;

    /**clip if we render directly*/
    public Shape defaultClip;

    //save font info and generate glyph on first render
    protected boolean generateGlyphOnRender;

    /**flag to show text is being extracted*/
    private boolean textExtracted=true;

    /**flag to show content is being rendered*/
    private boolean renderText;
    
    private boolean renderClipText;

    /**flags to show we need colour data as well*/
    private boolean textColorExtracted;

    int renderMode;

    int pageNum;

    int extractionMode;

    boolean useJavaFX;

    /**allow us to know if XFA content*/
    private boolean isXFA;

    protected GlyphTracker customGlyphTracker;
    private boolean renderDirectly;

    ShapeTracker customShapeTracker;

    String fileName="";
    
    boolean tooManyShapes;

    public void setName(final String name) {
        if(name!=null){
            this.fileName=name.toLowerCase();

            /**check no separators*/
            int sep=fileName.lastIndexOf(47); // '/'=47
            if(sep!=-1) {
                fileName = fileName.substring(sep + 1);
            }
            sep=fileName.lastIndexOf(92); // '\\'=92
            if(sep!=-1) {
                fileName = fileName.substring(sep + 1);
            }
            sep=fileName.lastIndexOf(46); // "."=46
            if(sep!=-1) {
                fileName = fileName.substring(0, sep);
            }
        }
    }

    public GlyphTracker getCustomGlyphTracker() {
        return customGlyphTracker;
    }

    public void setCustomGlyphTracker(final GlyphTracker customGlyphTracker) {
        this.customGlyphTracker = customGlyphTracker;
    }

    public void setXFA(final boolean isXFA) {
        this.isXFA=isXFA;
    }


    public boolean isPageContent() {
        return isPageContent;
    }

    public boolean isRenderPage() {
        return renderPage;
    }

    public void init(final boolean isPageContent, final boolean renderPage, final int renderMode, final int extractionMode, final boolean isPrinting, final boolean useJavaFX){

        this.isPageContent=isPageContent;
        this.renderPage=renderPage;

        this.renderMode=renderMode;

        this.extractionMode=extractionMode;

        this.isPrinting=isPrinting;

        this.useJavaFX=useJavaFX;

        textExtracted=(extractionMode & PdfDecoderInt.TEXT)==PdfDecoderInt.TEXT;

        renderText=renderPage &&(renderMode & PdfDecoderInt.RENDERTEXT) == PdfDecoderInt.RENDERTEXT;

        textColorExtracted=(extractionMode & PdfDecoderInt.TEXTCOLOR) == PdfDecoderInt.TEXTCOLOR;

    }

    public ParserOptions() {

    }

    public boolean isRenderClipText(){
        return renderClipText;
    }
    
    public boolean isRenderText(){
        return renderText;
    }

    public boolean isPrinting(){
        return isPrinting;
    }

    public boolean isTextColorExtracted(){
        return textColorExtracted;
    }

    public boolean isTextExtracted(){
        return textExtracted;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public int getExtractionMode() {
        return extractionMode;
    }

    public boolean useJavaFX() {
        return useJavaFX;
    }

    public boolean isXFA() {
        return isXFA;
    }

    public boolean generateGlyphOnRender() {
        return generateGlyphOnRender;
    }
    
    public void setGenerateGlyphOnRender(final boolean value) {
        generateGlyphOnRender=value;
    }

    public int getTextPrint() {
        return textPrint;
    }
    
    public void setTextPrint(final int value) {
        textPrint=value;
    }

    public StructuredContentHandler getContentHandler() {
        return contentHandler;
    }

    public void setContentHandler(final StructuredContentHandler contentHandler) {
        this.contentHandler=contentHandler;
    }
    
    public boolean renderDirectly() {
        return renderDirectly;
    }

    void setRenderDirectly(final boolean b) {
        renderDirectly=b;
    }

    public void setSamplingUsed(final float scaleY) {
        samplingUsed=scaleY;
    }

    float getSamplingUsed() {
        return samplingUsed;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPageNumber() {
        return pageNum;
    }

    public void setPageNumber(final int value) {

        pageNum=value;

    }

    public void setCustomShapeTracker(final ShapeTracker obj) {
        customShapeTracker=obj;
    }

    public ShapeTracker getCustomShapeTraker() {
        return customShapeTracker;
    }

    public void setFlattenedForm(final boolean b) {
        isFlattenedForm=b;
    }

    public boolean isFlattenedForm() {
        return isFlattenedForm;
    }

    public void setOffsets(final float x, final float y) {
        flattenX=x;
        flattenY=y;
    }

    public float getflattenX() {
        return flattenX;
    }

    public float getflattenY() {
        return flattenY;
    }

    public boolean isLayerVisible() {
        return isLayerVisible;
    }

    public void setPdfLayerList(final PdfLayerList layers) {
        this.layers=layers;
    }

    boolean hasContentHandler() {
        
        return contentHandler!=null;
        
    }

    /**
     * @return the layerLevel
     */
    public int getLayerLevel() {
        return layerLevel;
    }

    /**
     * @param layerLevel the layerLevel to set
     */
    public void setLayerLevel(int layerLevel) {
        this.layerLevel = layerLevel;
    }

    /**
     * @return the layerVisibility
     */
    public Set<Integer> getLayerVisibility() {
        return layerVisibility;
    }

    /**
     * @param layerVisibility the layerVisibility to set
     */
    public void setLayerVisibility(Set<Integer> layerVisibility) {
        this.layerVisibility = layerVisibility;
    }

    /**
     * @param isLayerVisible the isLayerVisible to set
     */
    public void setIsLayerVisible(boolean isLayerVisible) {
        this.isLayerVisible = isLayerVisible;
    }
}
