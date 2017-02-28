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
 * FXDisplay.java
 * ---------------
 */
package org.jpedal.render;


import java.awt.image.*;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;
//import javafx.stage.Modality;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.PatternColorSpace;
import org.jpedal.exception.PdfException;
//import org.jpedal.examples.viewer.gui.javafx.dialog.FXMessageDialog;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.glyph.*;
import org.jpedal.fonts.tt.TTGlyph;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Object;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;
import java.util.ArrayList;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;


public class FXDisplay extends GUIDisplay {
    
    final Group pdfContent = new Group();

   // private final ObservableList children=pdfContent.getChildren();

    private final java.util.List collection=new ArrayList(2000);
    
    public FXDisplay(final int pageNumber, final boolean addBackground, final int defaultSize, final ObjectStore newObjectRef) {

        this.rawPageNumber =pageNumber;
        this.objectStoreRef = newObjectRef;
        this.addBackground=addBackground;
        
        setupArrays(defaultSize);

    }
    
    @Override
    public void flushAdditionalObjOnPage(){
        throw new RuntimeException("NOt used in JavaFX implementation - please redecode the page");
    }
    

    public FXDisplay(final int pageNumber, final ObjectStore newObjectRef, final boolean isPrinting) {

        this.rawPageNumber =pageNumber;
        this.objectStoreRef = newObjectRef;
        this.isPrinting=isPrinting;
        
        setupArrays(defaultSize);

    }
    
    /* remove all page objects and flush queue */
    @Override
    public void flush() {
        
       // children.clear();
        pageObjects.clear();
        objectType.clear();
        areas.clear();
        
        currentItem = 0;
    }
    
     
    /*Method to add Shape, Text or image to main display on page over PDF - will be flushed on redraw*/
    @Override
    public void drawAdditionalObjectsOverPage(final int[] type, final java.awt.Color[] colors, final Object[] obj) throws PdfException {
        
        if(obj==null){
            return ;
        }
        
        if(Platform.isFxApplicationThread()){
            drawUserContent(type, obj, colors);
        }else{
            Platform.runLater(new Runnable(){
                @Override public void run() {
                    try {
                        drawUserContent(type, obj, colors);
                    } catch (final PdfException e) {
                        //tell user and log
                        if (LogWriter.isOutput()) {
                            LogWriter.writeLog("Exception with additional objects: " + e.getMessage());
                        }
                    }
                }
            });
        }   
    }
    
    /* save image in array to draw */
    @Override
    public int drawImage(final int pageNumber, final BufferedImage image,
    final GraphicsState currentGraphicsState,
    final boolean alreadyCached, final String name, final int previousUse) {

        this.rawPageNumber =pageNumber;
        float CTM[][]=currentGraphicsState.CTM;

        final WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
        
        final float imageW=(float) fxImage.getWidth();
        final float imageH=(float) fxImage.getHeight();
        
        final ImageView im1View = new ImageView(fxImage);
        
        // Stores the affine used on the image to use on the clip later
        float[] affine = {CTM[0][0]/imageW,CTM[0][1]/imageW,
                -CTM[1][0]/imageH,-CTM[1][1]/imageH,
                CTM[2][0]+CTM[1][0],CTM[2][1]+CTM[1][1]};
            
        im1View.getTransforms().setAll(Transform.affine(affine[0], affine[1], affine[2], affine[3], affine[4], affine[5]));
        
        setClip(currentGraphicsState, affine, im1View);
        setBlendMode(currentGraphicsState, im1View);

        addToScene(im1View);
        
        final float WidthModifier = 1;
        final float HeightModifier = 1;
        
        //ignore in this case /PDFdata/baseline_screens/customers3/1773_A2.pdf
        if(CTM[0][0]>0 && CTM[0][0]<0.05 && CTM[0][1]!=0 && CTM[1][0]!=0 && CTM[1][1]!=0){
            areas.addElement(null);
        }else{
            w=(int)(CTM[0][0]*WidthModifier);
            if(w==0) {
                w = (int) (CTM[0][1] * WidthModifier);
            }
            h=(int)(CTM[1][1]*HeightModifier);
            if(h==0) {
                h = (int) (CTM[1][0] * HeightModifier);
            }
            
            //fix for bug if sheered in low res
            if(!useHiResImageForDisplay && CTM[1][0]<0 && CTM[0][1]>0 && CTM[0][0]==0 && CTM[1][1]==0){
                final int tmp=w;
                w=-h;
                h=tmp;
            }
            
            //corrected in generation
            if(h<0 && !useHiResImageForDisplay) {
                h = -h;
            }
            
            //fix negative height on Ghostscript image in printing
            final int x1=(int)currentGraphicsState.x;
            int y1=(int)currentGraphicsState.y;
            final int w1=w;
            int h1=h;
            if(h1<0){
                y1 += h1;
                h1=-h1;
            }
            
            if(h1==0) {
                h1 = 1;
            }
            
            final int[] rectParams = {x1,y1,w1,h1};
            
            areas.addElement(rectParams);
            objectType.addElement(DynamicVectorRenderer.IMAGE);
        }
        
        if(!isRenderingToImage()){ //don ot cache image on disk if generating image
        
            final boolean cacheInMemory=(image.getWidth()<100 && image.getHeight()<100) || image.getHeight()==1;
            if(useHiResImageForDisplay && !cacheInMemory){
                pageObjects.addElement(null);
            }else {
                pageObjects.addElement(image);
            }

            if(rawKey==null){
                objectStoreRef.saveStoredImage(pageNumber+"_HIRES_"+currentItem,image,false,false,"tif");
                imageIDtoName.put(currentItem,pageNumber+"_HIRES_"+currentItem);
            }else{
                objectStoreRef.saveStoredImage(pageNumber+"_HIRES_"+currentItem+ '_' +rawKey,image,false,false,"tif");
                imageIDtoName.put(currentItem,pageNumber+"_HIRES_"+currentItem+ '_' +rawKey);
            }
        }
        
        currentItem++;
        
        return currentItem-1;
    }
    
    /*save shape in array to draw*/
    @Override
    public void drawShape(final Object rawShape, final GraphicsState currentGraphicsState, final int cmd) {

        final Shape currentShape=(javafx.scene.shape.Shape)rawShape;

        final float[] affine = {currentGraphicsState.CTM[0][0], currentGraphicsState.CTM[0][1], currentGraphicsState.CTM[1][0], currentGraphicsState.CTM[1][1], currentGraphicsState.CTM[2][0], currentGraphicsState.CTM[2][1]};
        // Fixes Pages from FDB-B737-FRM_nowatermark.pdf
//        if((affine[3] < 1 && affine[3] > 0)){
//           affine[3] = 1; //
//        }
        currentShape.getTransforms().add(javafx.scene.transform.Transform.affine(affine[0], affine[1], affine[2], affine[3], affine[4], affine[5]));
        
        //if Pattern, convert to Image with Pattern on instead
        if(currentGraphicsState.nonstrokeColorSpace.getID()==ColorSpaces.Pattern){
            drawPatternedShape(currentGraphicsState, (Path) currentShape);
        }else{
            setFXParams(currentShape,currentGraphicsState.getFillType(),currentGraphicsState, changeLineArtAndText);

            setClip(currentGraphicsState, affine, currentShape);
            setBlendMode(currentGraphicsState, currentShape);

            addToScene(currentShape);
        }
        final int[] shapeBounds = {(int)currentShape.getBoundsInLocal().getMinX(), (int)currentShape.getBoundsInLocal().getMinY()
                , (int)currentShape.getBoundsInLocal().getWidth(), (int)currentShape.getBoundsInLocal().getHeight()};
        
        pageObjects.addElement(currentShape);
        objectType.addElement(DynamicVectorRenderer.SHAPE);
        areas.addElement(shapeBounds);
        currentItem++;

    }
    
    private void drawPatternedShape(final GraphicsState currentGraphicsState, final Path currentShape){
//        if(true) return;
        final PatternColorSpace fillCS=(PatternColorSpace)currentGraphicsState.nonstrokeColorSpace;
        //get Image as BufferedImage and convert to javafx WritableImage
        final BufferedImage imageForPattern = fillCS.getImageForPatternedShape(currentGraphicsState);
        if(imageForPattern == null) {
            return;
        }
        Image fxImage = SwingFXUtils.toFXImage(imageForPattern, null);        
        double iw = fxImage.getWidth();
        double ih = fxImage.getHeight();        
//        final double xPos=currentShape.getBoundsInParent().getMinX();
//        final double yPos=currentShape.getBoundsInParent().getMinY();
//        double pw = currentShape.getBoundsInLocal().getWidth();
//        double ph = currentShape.getBoundsInLocal().getHeight();  
        ImagePattern pattern = new ImagePattern(fxImage, 0, 0, iw, ih,false);
        currentShape.setStroke(new Color(0, 0, 0, 0));
        currentShape.setFill(pattern);
        addToScene(currentShape);
        
//        if(true) return;
//        final PatternColorSpace fillCS=(PatternColorSpace)currentGraphicsState.nonstrokeColorSpace;
//        //get Image as BufferedImage and convert to javafx WritableImage
//        final BufferedImage imageForPattern = fillCS.getImageForPatternedShape(currentShape);
//        if(imageForPattern == null) {
//            return;
//        }
//
//        final WritableImage pattern = SwingFXUtils.toFXImage(imageForPattern, null);
//        final double xPos=currentShape.getBoundsInParent().getMinX();
//        final double yPos=currentShape.getBoundsInParent().getMinY();
//
//        final ImageView patternView  = new ImageView(pattern);
//        patternView.setX(xPos+1);
//        patternView.setY(yPos+1);        
        //addToScene(patternView, currentShape);
    }
    

    protected static void setFXParams(final Shape currentShape, final int fillType, final GraphicsState currentGraphicsState, boolean allowColorChange){

        // Removes the default black stroke on shapes
        currentShape.setStroke(null);
        
        if (fillType == GraphicsState.FILL || fillType == GraphicsState.FILLSTROKE) {
            
            //get fill colour
            int fillCol=currentGraphicsState.nonstrokeColorSpace.getColor().getRGB();
            
            if (allowColorChange) {
                //If we have an alt text color, its within threshold and not an additional item, use alt color
                if (textColor != null && (itemToRender == -1 || (endItem == -1 || itemToRender <= endItem)) && checkColorThreshold(fillCol)) {
                    fillCol = textColor.getRGB();
                }
            }
            //get value as rgb and set current colour used in fill
            final int r = ((fillCol >> 16) & 255);    //red
            final int g = ((fillCol >> 8) & 255);     //green
            final int b = ((fillCol) & 255);          //blue
            final double a=currentGraphicsState.getAlpha(GraphicsState.FILL);     //alpha
        
            currentShape.setFill(javafx.scene.paint.Color.rgb(r,g,b,a));
        }
        
        if (fillType == GraphicsState.STROKE) {

            //get fill colour
            int strokeCol=currentGraphicsState.strokeColorSpace.getColor().getRGB();
            
            if (allowColorChange) {
                //If we have an alt text color, its within threshold and not an additional item, use alt color
                if (textColor != null && (itemToRender == -1 || (endItem == -1 || itemToRender <= endItem)) && checkColorThreshold(strokeCol)) {
                    strokeCol = textColor.getRGB();
                }
            }
            
            //get value as rgb and set current colour used in fill
            final int r = ((strokeCol >> 16) & 255);    //red
            final int g = ((strokeCol >> 8) & 255);     //green
            final int b = ((strokeCol) & 255);          //blue
            final double a=currentGraphicsState.getAlpha(GraphicsState.STROKE);     //alpha
        
            currentShape.setStroke(javafx.scene.paint.Color.rgb(r,g,b,a));
            currentGraphicsState.applyFXStroke(currentShape);
        }
    }

     @Override
    public void drawCustom(final Object value) {
	 
            addToScene((Shape) value);
    }
    
    /**
     * store glyph info
     * @param Trm the Trm matrix (x,y is Trm[2][0], Trm[2][1]), other values are width (usually Trm[0][0] unless
     * rotated when could be Trm[0][1]) and height (usually Trm[1][1] or sometimes Trm[1][0]) Trm is defined in PDF
     * specification
     * @param fontSize The font size of the drawn text
     * @param embeddedGlyph For displaying rendered test
     * @param javaGlyph Is of type object used to draw text
     * @param type The type of text rendering
     * @param gs The graphics state to use
     * @param textScaling An array of text scaling
     * @param glyf Is of type String used to draw text in the Viewer
     * @param currentFontData font of the current decoded page in the Viewer
     * @param glyfWidth The width of drawn text
     */
    @Override
    public void drawEmbeddedText(final float[][] Trm, final int fontSize, final PdfGlyph embeddedGlyph,
        final Object javaGlyph, final int type, final GraphicsState gs, final double[] textScaling, final String glyf, final PdfFont currentFontData, final float glyfWidth) {
          
        //lock out type3
        if(type==DynamicVectorRenderer.TYPE3) {
            return;
        }
        
        //case one - text is using Java to draw it
        if(embeddedGlyph == null && javaGlyph == null){
            final Text t = new Text(glyf);
            // Get the affine

            /**
             * Set the font for the current decoded page in the Viewer
             */
            final Font f = Font.font(currentFontData.getGlyphData().font_family_name, fontSize);
            t.setFont(f);
            
            /**
             * Set the text color (fill and stroke)
             */
            setFXParams(t, GraphicsState.FILL, gs, textColor!=null);
            
            // If the stroke is needed, fill it in
            if((gs.getTextRenderType() & GraphicsState.STROKE) == GraphicsState.STROKE ){
                setFXParams(t, GraphicsState.STROKE, gs, textColor!=null);
            }
            
            setBlendMode(gs, t);

            // Set the affines
            if(type!=DynamicVectorRenderer.TRUETYPE){
                final double r=1d / (double) fontSize;
                t.getTransforms().add(Transform.affine(textScaling[0]*r,textScaling[1]*r,textScaling[2]*r,textScaling[3]*r,Trm[2][0],Trm[2][1]));        
            }else{
                final double r=1d / (double) fontSize;
                t.getTransforms().setAll(Transform.affine(Trm[0][0]*r,Trm[0][1]*r,Trm[1][0]*r,Trm[1][1]*r,Trm[2][0],Trm[2][1]));          
            }
            
            final float[] transform= {Trm[0][0],Trm[1][0],Trm[0][1],Trm[1][1],Trm[2][0],Trm[2][1]};
            
            final Shape clip = gs.getFXClippingShape();
        
            if(clip!=null && !clip.contains(Trm[2][0],Trm[2][1])){
                 setClip(gs, transform, t);
            }
        
            pageObjects.addElement(t);
            addToScene(t);
            
        }else { //case two - text is using our font engine
            
            // System.out.println("embeddedGlyph = "+ embeddedGlyph+" "+at+" "+Trm[0][0]);

            final Path path=embeddedGlyph.getPath();

            if(path==null){
                //
                
                return;
            }
            
            path.setFillRule(FillRule.EVEN_ODD);
            
           
            if(type!=DynamicVectorRenderer.TRUETYPE){
                  path.getTransforms().setAll(Transform.affine(textScaling[0],textScaling[1],textScaling[2],textScaling[3],textScaling[4],textScaling[5]));        
            }else{
                final double r=1d/100d;
                
                if(!TTGlyph.useHinting) {
                    path.getTransforms().setAll(Transform.affine(textScaling[0], textScaling[1], textScaling[2], textScaling[3], textScaling[4], textScaling[5]));
                } else {
                    path.getTransforms().setAll(Transform.affine(textScaling[0] * r, textScaling[1] * r, textScaling[2] * r, textScaling[3] * r, textScaling[4], textScaling[5]));
                }
            }

            setFXParams(path,gs.getTextRenderType(),gs, textColor!=null);
            setBlendMode(gs, path);
            
            final float[] transform= {Trm[0][0],Trm[1][0],Trm[0][1],Trm[1][1],Trm[2][0],Trm[2][1]};
           
            final Shape clip = gs.getFXClippingShape();
        
            if(clip!=null && !clip.contains(Trm[2][0],Trm[2][1])){
                 setClip(gs, transform, path);
            }
            pageObjects.addElement(path);
            addToScene(path);     
        }
        objectType.addElement(type);
        
        if(type<0){
            areas.addElement(null);
        }else{
            if(javaGlyph!=null){
                final int[] rectParams = {(int)(Trm[2][0]),(int)Trm[2][1],fontSize,fontSize};
                areas.addElement(rectParams);
                
            }else{
                /**now text*/
                int realSize=fontSize;
                if(realSize<0) {
                    realSize = -realSize;
                }
                final int[] area= {(int)(Trm[2][0]),(int)Trm[2][1],realSize,realSize};
                
                areas.addElement(area);
            }
        }
        
        currentItem++;
    }
    
    /**
     * When Transform.affine() is applied to the image, it's applied to the clip as well.
     * This causes the clip to be transformed into the incorrect position.
     * 
     * This code essentially un-transforms the clip so it clips correctly again.
     */
    private static void setClip(final GraphicsState currentGraphicsState, final float[] affine, final Node baseNode){

        final Shape clip = currentGraphicsState.getFXClippingShape();
        if(clip != null){
            try {
                // Lock out specific matrices from being reversed
                if(!Arrays.equals(affine, new float[]{1,0,0,-1,0,0})){
                    
                    // Side note: initialising a straight up Affine uses the doubles in a different order
                    final Affine inverseAff = Transform.affine(affine[0], affine[1], affine[2], affine[3], affine[4], affine[5]).createInverse();
                    clip.getTransforms().add(inverseAff);
                }
                
                /**
                 * 
                 * PDFdata\test_data\Hand_Test\awjune2003.pdf and
                 * PDFdata\test_data\Hand_Test\rechnung_file.PDF
                 * PDFdata\test_data\Hand_Test\jj.PDF
                 * PDFdata\test_data\sample_pdfs\CIDs\Article7.pdf
                * */
              
                boolean applyClip = clip.getBoundsInLocal().getMinX()>baseNode.getBoundsInLocal().getMinX() && 
                        baseNode.getBoundsInLocal().getMaxY() > clip.getBoundsInLocal().getMaxY();
              
                if (applyClip) {  
                 
                    baseNode.setClip(clip);
                }
                  


            } catch (final NonInvertibleTransformException ex) {
                ex.printStackTrace();
            }
                
        }
    }

    public Group getFXPane() {
        
      //  System.out.println("getFXPane "+this);
        if(collection!=null && !collection.isEmpty()){
         //   pdfContent.getChildren().removeAll(collection);
            pdfContent.getChildren().addAll(collection);
            collection.clear();
        }
        return pdfContent;
    }
    
    protected static void setBlendMode(final GraphicsState gs, final Node n){
        
        switch(gs.getBMValue()){
            case PdfDictionary.Multiply:
                n.setBlendMode(BlendMode.MULTIPLY);
                break;
            case PdfDictionary.Screen:
                n.setBlendMode(BlendMode.SCREEN);
                break;
            case PdfDictionary.Overlay:
                n.setBlendMode(BlendMode.OVERLAY);
                break;
            case PdfDictionary.Darken:
                n.setBlendMode(BlendMode.DARKEN);
                break;
            case PdfDictionary.Lighten:
                n.setBlendMode(BlendMode.LIGHTEN);
                break;
            case PdfDictionary.ColorDodge:
                n.setBlendMode(BlendMode.COLOR_DODGE);
                break;
            case PdfDictionary.ColorBurn:
                n.setBlendMode(BlendMode.COLOR_BURN);
                break;
            case PdfDictionary.HardLight:
                n.setBlendMode(BlendMode.HARD_LIGHT);
                break;
            case PdfDictionary.SoftLight:
                n.setBlendMode(BlendMode.SOFT_LIGHT);
                break;
            case PdfDictionary.Difference:
                n.setBlendMode(BlendMode.DIFFERENCE);
                break;
            case PdfDictionary.Exclusion:
                n.setBlendMode(BlendMode.EXCLUSION);
                break;
            default:
                n.setBlendMode(null);
                break;
        }
    }
    
     /**
     * Adds items to scene, ensuring we are on the FX thread
     * @param items All the nodes that are added to the Scene
     */
    private void addToScene(final Node items){
        collection.add(items);
        
    }
    
    
    /**
     * Adds items to scene, ensuring we are on the FX thread
     * @param items All the nodes that are added to the Scene
     */
//    private void addToScenes(final Node... items){
//        if(Platform.isFxApplicationThread()){
//            children.addAll(items);
//        }else{
//            Platform.runLater(new Runnable(){
//                @Override public void run() {
//                    children.addAll(items);
//                }
//            });
//        }
//    }
    
    /**
     * 
     * @param defaultSize The size of the array
     */
    private void setupArrays(final int defaultSize){
        areas=new Vector_Rectangle_Int(defaultSize);
        objectType=new Vector_Int(defaultSize);
        pageObjects=new Vector_Object(defaultSize);
        
        currentItem = 0;
    }

    @Override
    public void paintBackground(final java.awt.Shape dirtyRegion) {
        
        if (addBackground) {
             
            Path background = new Path();
            
            background.getElements().add(new MoveTo(xx, yy));
            background.getElements().add(new LineTo(xx, yy+(int) (h * scaling)));
            background.getElements().add(new LineTo(xx+ (int) (w * scaling), yy+(int) (h * scaling)));
            background.getElements().add(new LineTo(xx+ (int) (w * scaling), yy));
            background.getElements().add(new LineTo(xx, yy));
            
            background.setFill(new Color(backgroundColor.getRed()/255.0f, backgroundColor.getGreen()/255.0f, backgroundColor.getBlue()/255.0f, 1.0f));
           addToScene(background);
        }
    }
}