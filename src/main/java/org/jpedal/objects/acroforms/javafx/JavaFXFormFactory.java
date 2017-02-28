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
 * JavaFXFormFactory.java
 * ---------------
 */
package org.jpedal.objects.acroforms.javafx;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import org.jpedal.color.DeviceCMYKColorSpace;
import org.jpedal.color.PdfColor;
import org.jpedal.color.PdfPaint;
import org.jpedal.objects.acroforms.GUIData;
import org.jpedal.objects.acroforms.actions.JavaFX.JavaFXComboListener;
import org.jpedal.objects.acroforms.actions.JavaFX.JavaFXControlListener;
import org.jpedal.objects.acroforms.actions.JavaFX.JavaFXFormButtonListener;
import org.jpedal.objects.acroforms.actions.JavaFX.JavaFXFormsListener;
import org.jpedal.objects.acroforms.actions.JavaFX.JavaFXListListener;
import org.jpedal.objects.acroforms.actions.JavaFX.JavaFXRadioListener;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.acroforms.creation.GenericFormFactory;
import org.jpedal.objects.acroforms.utils.FormsCSSHelper;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Strip;

/**
 *
 */
public class JavaFXFormFactory extends GenericFormFactory implements FormFactory{
    

    /**
     * allows access to renderer variables
     *
     */
    public JavaFXFormFactory() {
    }
    
    private Button setupAnnotationButton(final FormObject form){
        final Button but = new Button();
        final StringBuilder buttonStyle = new StringBuilder(200);
        final JavaFXControlListener controlListener = new JavaFXControlListener(but);
        
        buttonStyle.append("-fx-padding:0;-fx-border:none;-fx-background-color:transparent;-fx-border-radius:0;-fx-background-radius:0;");
        
        
        setupButton(but,form, controlListener);
        setupUniversalFeatures(but, form, buttonStyle, controlListener);
        
        but.setStyle(buttonStyle.toString());
        
        return but;
    }
    
    private Button createAnntoationHighlight(final FormObject form) {
        Button but = setupAnnotationButton(form);
        Color color = getAnnotationColor(form);

        float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad == null) {
            quad = form.getFloatArray(PdfDictionary.Rect);
        }

        final Canvas canvas = new Canvas(form.getBoundingRectangle().width, form.getBoundingRectangle().height);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        if (quad.length >= 8) {
            for (int hi = 0; hi != quad.length; hi += 8) {
                final int x = (int) quad[hi] - form.getBoundingRectangle().x;
                int y = (int) quad[hi + 5] - form.getBoundingRectangle().y;
                //Adjust y for display
                y = (form.getBoundingRectangle().height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                final int width = (int) (quad[hi + 2] - quad[hi]);
                final int height = (int) (quad[hi + 1] - quad[hi + 5]);

                try {
                    gc.setFill(color);
                    gc.fillRect(x, y, width, height);
                    but.setGraphic(new JavaFXImageIcon(but, form, canvas.snapshot(params, null), 0));
                } catch (final Exception e) {
                    //tell user and log
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        }
        
        return but;
    }
    
    private Button createAnnotationFreeText(final FormObject form){
        Button but = setupAnnotationButton(form);
        but.setText(form.getTextStreamValue(PdfDictionary.Contents));
        return but;
    }
                    
    private Button createAnnotationText(final FormObject form){
        Button but = setupAnnotationButton(form);
        String name = form.getTextStreamValue(PdfDictionary.Name);
        BufferedImage commentIcon = getAnnotationTextIcon(form, name);
        
        //Ensure sized correctly
        final float[] rect = form.getFloatArray(PdfDictionary.Rect);
        rect[1] = rect[3] - commentIcon.getHeight();
        rect[2] = rect[0] + commentIcon.getWidth();
        form.setFloatArray(PdfDictionary.Rect, rect);

        but.setGraphic(new JavaFXImageIcon(but, form, commentIcon, 0));

        return but;
    }
    
    private Pane createAnnotationPopup(final FormObject form) {
        
        Pane comp = (Pane) getPopupComponent(form, pageData.getCropBoxWidth(form.getPageNumber()));
        form.setGUIComponent(comp, FormFactory.JAVAFX);
        comp.setVisible(form.getBoolean(PdfDictionary.Open));
        
        return comp;

    }
    
    private Button createAnnotationUnderline(final FormObject form) {
        Button but = setupAnnotationButton(form);
        Color color = getAnnotationColor(form);

        float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad == null) {
            quad = form.getFloatArray(PdfDictionary.Rect);
        }

        final Canvas canvas = new Canvas(form.getBoundingRectangle().width, form.getBoundingRectangle().height);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        // Snapshot uses a white background unless specified otherwise
        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        if (quad.length >= 8) {
            for (int hi = 0; hi != quad.length; hi += 8) {
                final int x = (int) quad[hi] - form.getBoundingRectangle().x;
                int y = (int) quad[hi + 5] - form.getBoundingRectangle().y;
                //Adjust y for display
                y = (form.getBoundingRectangle().height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                final int width = (int) (quad[hi + 2] - quad[hi]);
                final int height = (int) (quad[hi + 1] - quad[hi + 5]);

                try {
                    gc.setFill(new Color(0, 0, 0, 0));
                    gc.fillRect(x, y, width, height);
                    gc.setFill(color);
                    gc.fillRect(x, y + height - 1, width, 1);

                    but.setGraphic(new JavaFXImageIcon(but, form, canvas.snapshot(params, null), 0));
                } catch (final Exception e) {
                    //tell user and log
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        }
        return but;
    }
    
    private Button createAnnotationInk(final FormObject form) {
        Button but = setupAnnotationButton(form);

        //we need this bit
        but.setTooltip(new Tooltip(form.getTextStreamValue(PdfDictionary.Contents)));

        final Object[] InkListArray = form.getObjectArray(PdfDictionary.InkList);

        //resize ink size if entire ink is not contained
        final float[] r = scanInkListTree(InkListArray, form, null);
        form.setFloatArray(PdfDictionary.Rect, new float[]{r[0], r[1], r[2], r[3]});

        //Create canvas to draw to
        final Canvas canvas = new Canvas(form.getBoundingRectangle().width, form.getBoundingRectangle().height);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        // Snapshot uses a white background unless specified otherwise
        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        scanInkListTree(InkListArray, form, gc);

        but.setGraphic(new JavaFXImageIcon(but, form, canvas.snapshot(params, null), 0));

        return but;
    }
    
    private Button createAnnotationStrikeOut(final FormObject form) {
        Button but = setupAnnotationButton(form);
        
        Color color = getAnnotationColor(form);

        float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad == null) {
            quad = form.getFloatArray(PdfDictionary.Rect);
        }

        final Canvas canvas = new Canvas(form.getBoundingRectangle().width, form.getBoundingRectangle().height);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        // Snapshot uses a white background unless specified otherwise
        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        if (quad.length >= 8) {
            for (int hi = 0; hi != quad.length; hi += 8) {
                final int x = (int) quad[hi] - form.getBoundingRectangle().x;
                int y = (int) quad[hi + 5] - form.getBoundingRectangle().y;
                //Adjust y for display
                y = (form.getBoundingRectangle().height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                final int width = (int) (quad[hi + 2] - quad[hi]);
                final int height = (int) (quad[hi + 1] - quad[hi + 5]);

                try {
                    gc.setFill(Color.TRANSPARENT);
                    gc.fillRect(0, 0, width, height);
                    gc.setFill(color);
                    gc.fillRect(x, y + (height / 2), width, 1);
                    but.setGraphic(new JavaFXImageIcon(but, form, canvas.snapshot(params, null), 0));
                } catch (final Exception e) {
                    //tell user and log
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        }

        return but;
    }
    
    private static Color getAnnotationColor(final FormObject form){
        final float[] formColor = form.getFloatArray(PdfDictionary.C);
        Color color = Color.TRANSPARENT;
        if (formColor != null) {
            switch (formColor.length) {
                case 0:
                    //Should not happen. Do nothing. Annotation is transparent
                    break;
                case 1:
                    //DeviceGrey colorspace
                    color = new Color(formColor[0], formColor[0], formColor[0], 1.0f);
                    break;
                case 3:
                    //DeviceRGB colorspace
                    color = new Color(formColor[0], formColor[1], formColor[2], 1.0f);
                    break;
                case 4:
                    //DeviceCMYK colorspace
                    final DeviceCMYKColorSpace cmyk = new DeviceCMYKColorSpace();
                    cmyk.setColor(formColor, 4);

                    final int r;
                    final int g;
                    final int b;
                    final int rgb = cmyk.getColor().getRGB();
                    r = (rgb >> 16) & 255;
                    g = (rgb >> 8) & 255;
                    b = (rgb) & 255;

                    color = new Color(r, g, b, 1);

                    break;
                default:
                    break;
            }
        }
        return color;
    }
    
    private BufferedImage getAnnotationTextIcon(final FormObject form, String name){
        BufferedImage commentIcon = null;
        if (name == null) {
            name = "Note";
        }
        /* Name of the icon image to use for the icon of this annotation
         * - predefined icons are needed for names:-
         * Comment, Key, Note, Help, NewParagraph, Paragraph, Insert
         */
        try {
            if (name.equals("Comment")) {
                // If the button doesn't have an AP Image attached, use local graphic
                commentIcon = ImageIO.read(getClass().getResource("/org/jpedal/objects/acroforms/res/comment.png"));
            } else {
                commentIcon = ImageIO.read(getClass().getResource("/org/jpedal/objects/acroforms/res/note.png"));
            }
        } catch (final Exception e) {
            //tell user and log
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
        }

        //Set color of annotation
        final float[] col = form.getFloatArray(PdfDictionary.C);
        if (col != null) {

            final PdfPaint c = new PdfColor(col[0], col[1], col[2]);
            final int rgb = c.getRGB();

            //Replace default color with specified color
            for (int x = 0; x != commentIcon.getWidth(); x++) {
                for (int y = 0; y != commentIcon.getHeight(); y++) {

                    //Checks for yellow (R255,G255,B000) and replaces with color
                    if (commentIcon.getRGB(x, y) == -256) {
                        commentIcon.setRGB(x, y, rgb);
                    }
                }
            }
        }
        
        return commentIcon;
    }
    /**
     * setup annotations display with popups, etc
     */
    @Override
    public Object annotationButton(final FormObject form) {
        
        final int subtype=form.getParameterConstant(PdfDictionary.Subtype);
        
        //Special case that does not return a button so handle separately.
        if(subtype == PdfDictionary.Popup){
            return createAnnotationPopup(form);
        }
        /**
         * @kieran - there are several types of annotation (Underline, highlight, Ink).
         *
         * We implemented them by adding a button and putting the content on the button's image
         * We had not added others as no example. Can you use the text example to add for missing values?
         */
        // If we're using the icon from the AP Stream, this section isn't used
        if(!form.isAppearanceUsed()){
            switch(subtype){
                case PdfDictionary.Text:/* a sticky note which displays a popup when open. */
                    return createAnnotationText(form);
                case PdfDictionary.FreeText:/* we only have 11dec/itext_sample.pdf as example) */
                    return createAnnotationFreeText(form);
                case PdfDictionary.Highlight :
                    return createAnntoationHighlight(form);
                case PdfDictionary.Underline :
                    return createAnnotationUnderline(form);
                case PdfDictionary.Ink:
                    return createAnnotationInk(form);
                case PdfDictionary.StrickOut :
                    return createAnnotationStrikeOut(form);
                default:
                    //
                    break;
            }
        }
        
        //If none of the above, just setup button
        return setupAnnotationButton(form);
    }
    
    static float[] curveInk(final float[] points){
        
        double x0, y0;
        double x1, y1;
        double x2, y2;
        double x3, y3;
        
        final double smooth_value = 1.0;
        
        int currentIndex = 0;
        final float[] returnPoints = new float[((points.length-2)/2)*8];
        
        for(int i = 0; i<points.length; i+=2){
            
            if(i==0){
                
                x0 = points[i];
                y0 = points[i+1];
                x1 = points[i];
                y1 = points[i+1];
                x2 = points[i+2];
                y2 = points[i+3];
                x3 = points[i+4];
                y3 = points[i+5];
                
                final double[] cps = findControlPoint(x0, y0, x1, y1, x2, y2, x3, y3, smooth_value);
                returnPoints[currentIndex] = (float)x1; currentIndex++;
                returnPoints[currentIndex] = (float)y1; currentIndex++;
                returnPoints[currentIndex] = (float)cps[0]; currentIndex++;
                returnPoints[currentIndex] = (float)cps[1]; currentIndex++;
                returnPoints[currentIndex] = (float)cps[2]; currentIndex++;
                returnPoints[currentIndex] = (float)cps[3]; currentIndex++;
                returnPoints[currentIndex] = (float)x2; currentIndex++;
                returnPoints[currentIndex] = (float)y2; currentIndex++;
            }
            
            if(i+6>=points.length){
                
                x0 = points[i];
                y0 = points[i+1];
                x1 = points[i+2];
                y1 = points[i+3];
                x2 = points[i+4];
                y2 = points[i+5];
                x3 = points[i+4];
                y3 = points[i+5];
                
                final double[] cps = findControlPoint(x0, y0, x1, y1, x2, y2, x3, y3, smooth_value);
                returnPoints[currentIndex] = (float)x1;
                currentIndex++;
                returnPoints[currentIndex] = (float)y1;
                currentIndex++;
                returnPoints[currentIndex] = (float)cps[0];
                currentIndex++;
                returnPoints[currentIndex] = (float)cps[1];
                currentIndex++;
                returnPoints[currentIndex] = (float)cps[2];
                currentIndex++;
                returnPoints[currentIndex] = (float)cps[3];
                currentIndex++;
                returnPoints[currentIndex] = (float)x2;
                currentIndex++;
                returnPoints[currentIndex] = (float)y2;
                //currentIndex++;
                break;
            }
            
            
            x0 = points[i];
            y0 = points[i+1];
            x1 = points[i+2];
            y1 = points[i+3];
            x2 = points[i+4];
            y2 = points[i+5];
            x3 = points[i+6];
            y3 = points[i+7];
            
            final double[] cps = findControlPoint(x0, y0, x1, y1, x2, y2, x3, y3, smooth_value);
            returnPoints[currentIndex] = (float)x1; currentIndex++;
            returnPoints[currentIndex] = (float)y1; currentIndex++;
            returnPoints[currentIndex] = (float)cps[0]; currentIndex++;
            returnPoints[currentIndex] = (float)cps[1]; currentIndex++;
            returnPoints[currentIndex] = (float)cps[2]; currentIndex++;
            returnPoints[currentIndex] = (float)cps[3]; currentIndex++;
            returnPoints[currentIndex] = (float)x2; currentIndex++;
            returnPoints[currentIndex] = (float)y2; currentIndex++;
            
        }
        
        return returnPoints;
    }
    
    private static double[] findControlPoint(final double x0, final double y0,
                                             final double x1, final double y1,
                                             final double x2, final double y2,
                                             final double x3, final double y3,
                                             final double smooth_value){
        
        //Find mid points
        final double xc1 = (x0 + x1) / 2.0;
        final double yc1 = (y0 + y1) / 2.0;
        final double xc2 = (x1 + x2) / 2.0;
        final double yc2 = (y1 + y2) / 2.0;
        final double xc3 = (x2 + x3) / 2.0;
        final double yc3 = (y2 + y3) / 2.0;
        
        //Caculate lengths
        final double len1 = Math.sqrt((x1-x0) * (x1-x0) + (y1-y0) * (y1-y0));
        final double len2 = Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
        final double len3 = Math.sqrt((x3-x2) * (x3-x2) + (y3-y2) * (y3-y2));
        
        //Mid point proportion
        final double k1 = len1 / (len1 + len2);
        final double k2 = len2 / (len2 + len3);
        
        //Length of mid point line proportions
        final double xm1 = xc1 + (xc2 - xc1) * k1;
        final double ym1 = yc1 + (yc2 - yc1) * k1;
        final double xm2 = xc2 + (xc3 - xc2) * k2;
        final double ym2 = yc2 + (yc3 - yc2) * k2;
        
        //Calculate control points
        final double ctrl1_x = xm1 + (xc2 - xm1) * smooth_value + x1 - xm1;
        final double ctrl1_y = ym1 + (yc2 - ym1) * smooth_value + y1 - ym1;
        
        final double ctrl2_x = xm2 + (xc2 - xm2) * smooth_value + x2 - xm2;
        final double ctrl2_y = ym2 + (yc2 - ym2) * smooth_value + y2 - ym2;
        
        return new double[]{ctrl1_x, ctrl1_y, ctrl2_x, ctrl2_y};
    }
    
    private static float[] scanInkListTree(final Object[] InkListArray, final FormObject form, final GraphicsContext gc) {
        
        
        float minX = 0;
        float minY = 0;
        float maxX = 0;
        float maxY = 0;
        
        float[] vals = null;

        //if specific DecodeParms for each filter, set othereise use global
        if(InkListArray !=null){
            
            final int count= InkListArray.length;
            
            float x;
            float y;
            
            boolean isFirstPoint = true;
            
            //If Graphics not set, don't draw anything.
            if(gc!=null){
                final float[] underlineColor = form.getFloatArray(PdfDictionary.C);
                Color c1 = Color.TRANSPARENT;
                if(underlineColor!=null){
                    switch(underlineColor.length){
                        case 0:
                            //Should not happen. Do nothing. Annotation is transparent
                            break;
                        case 1:
                            //DeviceGrey colorspace
                            c1 = new Color(underlineColor[0],underlineColor[0],underlineColor[0],1);
                            break;
                        case 3:
                            //DeviceRGB colorspace
                            c1 = new Color(underlineColor[0],underlineColor[1],underlineColor[2],1);
                            break;
                        case 4:
                            //DeviceCMYK colorspace
                            final DeviceCMYKColorSpace cmyk = new DeviceCMYKColorSpace();
                            cmyk.setColor(underlineColor, 4);
                            final int r;
                            final int g;
                            final int b;
                            final int rgb=cmyk.getColor().getRGB();
                            r = (rgb >> 16) & 255;
                            g = (rgb >> 8) & 255;
                            b = (rgb) & 255;

                            c1 = new Color(r,g,b,1);

                            break;
                        default:
                            break;
                    }
                }
                
                gc.setFill(Color.TRANSPARENT);
                gc.fillRect(0, 0, form.getBoundingRectangle().width, form.getBoundingRectangle().height);
                gc.setStroke(c1);
            }
            
            for(int i=0;i<count;i++){
                
                if(InkListArray[i] instanceof byte[]){
                    final byte[] decodeByteData= (byte[]) InkListArray[i];
                    
                    if(vals==null){
                        vals = new float[count];
                    }
                    
                    if(decodeByteData!=null){
                        final String val= new String(decodeByteData);
                        final float v = Float.parseFloat(val);
                        
                        switch(i%2){
                            case 0 :
                                if(isFirstPoint){
                                    minX = v;
                                    maxX = v;
                                }else{
                                    if(v<minX) {
                                        minX = v;
                                    }
                                    if(v>maxX) {
                                        maxX = v;
                                    }
                                }
                                x = (v - form.getBoundingRectangle().x);
                                vals[i] = x;
                                break;
                            case 1 :
                                if(isFirstPoint){
                                    minY = v;
                                    maxY = v;
                                    isFirstPoint = false;
                                }else{
                                    if(v<minY) {
                                        minY = v;
                                    }
                                    if(v>maxY) {
                                        maxY = v;
                                    }
                                }
                                y = form.getBoundingRectangle().height - (v - form.getBoundingRectangle().y);
                                vals[i] = y;
                                
                                //x = 0;
                                //y = 0;
                                
                                break;
                        }
                        //                        System.out.println("val="+val);
                        
                    }
                }else{
                    // System.out.println(">>");
                    final float[] r = scanInkListTree((Object[]) InkListArray[i], form, gc);
                    if(isFirstPoint){
                        minX = r[0];
                        maxX = r[2];
                        minY = r[1];
                        maxY = r[3];
                    }else{
                        if(r[0]<minX) {
                            minX = r[0];
                        }
                        if(r[2]>maxX) {
                            maxX = r[2];
                        }
                        if(r[1]<minY) {
                            minY = r[1];
                        }
                        if(r[3]>maxY) {
                            maxY = r[3];
                        }
                    }
                    // System.out.println("<<");
                }
            }
        }
        
        if(vals!=null){
            final float[] values = curveInk(vals);
            for(int i=0; i<values.length; i++){
                if(i%2==0){ //X coord
                    if(values[i]<minX) {
                        minX = values[i];
                    }
                    
                    if(values[i]>maxX) {
                        maxX = values[i];
                    }
                }else{ //Y coord
                    if(values[i]<minY) {
                        minY = values[i];
                    }
                    
                    if(values[i]>maxY) {
                        maxY = values[i];
                    }
                }
            }
            
            float xOffset = 0;
            float yOffset = 0;
            
            if(minX < 0) {
                xOffset = Math.abs(minX);
            }
            if(minY < 0) {
                yOffset = Math.abs(minY);
            }
            
            minX += xOffset;
            maxX += xOffset;
            minY += yOffset;
            maxY += yOffset;
            
            if(gc!=null){

                gc.setLineWidth(1.52f);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.setLineJoin(StrokeLineJoin.ROUND);
                
                for(int i=0; i<values.length; i+=8){
                    gc.beginPath();
                    // Start x/y
                    gc.moveTo(values[i]+xOffset, values[i+1]+yOffset);
                    gc.bezierCurveTo(
                            // Control x1/y1
                            values[i+2]+xOffset, values[i+3]+yOffset
                            // Control x2/y2
                            , values[i+4]+xOffset, values[i+5]+yOffset
                            // End x/y
                            , values[i+6]+xOffset, values[i+7]+yOffset);
                    
                    
                    gc.fill();
                    gc.stroke();
                    gc.closePath();
                }
            }
        }
        
        return new float[]{minX, minY, maxX, maxY};
    }
    
    /**
     * setup and return the ComboBox field specified in the FormObject
     */
    @Override
    public Object comboBox(final FormObject form) {
        
        //populate items array with list from Opt
        final ObservableList<String> itemsNew = form.getItemsList() == null ? null : FXCollections.observableArrayList(form.getItemsList());
        final StringBuilder styleBuilder = new StringBuilder(200);
        final ComboBox combo = itemsNew == null ? new ComboBox() : new ComboBox(itemsNew);
        boolean readonly=false;
        final JavaFXControlListener controlListener = new JavaFXControlListener(combo);

        final boolean[] flags = form.getFieldFlags();
        combo.setEditable(true);
        if (!flags[FormObject.EDIT_ID]) {//is not editable
            // Hack to get rid of padding around disabled fields
            combo.getEditor().setEditable(false);
            combo.getEditor().setCursor(Cursor.DEFAULT);
            readonly=true;
        }

        if(flags[FormObject.READONLY_ID]) {
            combo.setDisable(true);
            combo.getEditor().setEditable(false);
            combo.getEditor().setCursor(Cursor.DEFAULT);
            readonly=true;
        }
        
        //get and set currently selected value
        final String textValue = form.getSelectedItem();
        if (form.getValuesMap(true) != null) {
            combo.getSelectionModel().select(textValue);
        } else {
            combo.getSelectionModel().select(textValue);
        }
        
        final int selectionIndex = combo.getSelectionModel().getSelectedIndex();
        final Object selectedItem = combo.getSelectionModel().getSelectedItem();
        form.setSelection(new Object[]{selectedItem},selectedItem!=null ? selectedItem.toString() : null, new int[]{selectionIndex}, selectionIndex);
       
        setupUniversalFeatures(combo, form,  styleBuilder, controlListener);
        setBorder(form, styleBuilder);
        addFont(form, false, false, styleBuilder);
        
        combo.getStyleClass().add("formsComboBox");
        combo.setStyle(styleBuilder.toString());
        
        final boolean isReadonly = readonly;
        // Allow the dropdown to display when clicking on the editable box
        combo.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            boolean toggle;
            @Override public void handle(final MouseEvent event) {
                if(isReadonly){
                    if(toggle){
                        combo.hide();
                    }else{
                        combo.show();
                    }
                    toggle=!toggle;
                }
            }
        });
        
        final int[] textColor = FormsCSSHelper.getTextColor(form);
        if(textColor!=null){
            final String textFill = String.format("-fx-text-fill:rgb(%d, %d, %d);", textColor[0],textColor[1],textColor[2]);
            combo.getEditor().setStyle(textFill);
            // Set style on each cell using the cell factory
            combo.setCellFactory(new Callback<ListView<String>, ListCell<String>>(){
                @Override public ListCell<String> call(final ListView<String> param) {
                    return new ListCell<String>(){
                        @Override public void updateItem(final String item, final boolean empty){
                            super.updateItem(item, empty);
                            setStyle(textFill);
                            setText(item);
                        }
                    };
                }
            });
        }
        
        //Adding the ComboListener
        combo.getSelectionModel().selectedIndexProperty().addListener(new JavaFXComboListener (combo,form));
        return combo;
   }
    /**
     * setup and return the CheckBox button specified in the FormObject
     */
    @Override
    public Object checkBoxBut(final FormObject form) {
        //setupButton Needs to be implemented
        final ToggleButton checkBut = new ToggleButton();
        final JavaFXControlListener controlListener = new JavaFXControlListener(checkBut);
        final StringBuilder styleBuilder = new StringBuilder(200);
        
        // Used to identify whether it can be deslected on click
        checkBut.setUserData(CHECKBOXBUTTON);
        
        setupUniversalFeatures(checkBut, form, styleBuilder, controlListener);
        setupButton(checkBut, form, controlListener);
        setupToggleGroup(form, checkBut);
        addFont(form, false, false, styleBuilder);
        
        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            checkBut.setDisable(true);
        }
        
        styleBuilder.append("-fx-padding:0;-fx-border:none;-fx-background-color:transparent;");
        checkBut.setStyle(styleBuilder.toString());
        
        return checkBut;
    }
    
    /**
     * setup and return the List field specified in the FormObject
     */
    @Override
    public Object listField(final FormObject form) {
        //Listener needs to be implemented
        //populate the items array with list from Opt
        final ObservableList<String> items =  FXCollections.observableArrayList(form.getItemsList());
        final ListView lists = items == null ? new ListView() : new ListView(items);
        final JavaFXControlListener controlListener = new JavaFXControlListener(lists);
        final StringBuilder styleBuilder = new StringBuilder(200);
        
        if (!form.getFieldFlags()[FormObject.MULTISELECT_ID])//mulitselect
        {
            lists.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        
        //if there is a top index or selected value select it
        if (form.getSelectionIndices() != null) {
           lists.getSelectionModel().selectFirst();
           
        } else  if (form.getValuesMap(true) != null) {
            lists.getSelectionModel().select(form.getValuesMap(true).get(form.getSelectedItem()));
            
        } else {
           lists.getSelectionModel().select(form.getSelectedItem());
        }
        //sync to FormObject
        final int selectionIndex = lists.getSelectionModel().getSelectedIndex();
        form.setSelection(new Object[]{lists.getSelectionModel().getSelectedItem()},lists.getSelectionModel().getSelectedItem().toString(), new int[]{selectionIndex}, selectionIndex);
        
        setupUniversalFeatures(lists, form,styleBuilder, controlListener);
        setBorder(form, styleBuilder);
        addFont(form, true, true, styleBuilder);
        
        lists.getStyleClass().add("formsListBox");
        
        lists.setStyle(styleBuilder.toString());
        
        //Adding the List Listener 
        lists.getSelectionModel().selectedIndexProperty().addListener(new JavaFXListListener(lists, form));
        return lists;
    }
    
    /**
     * setup and return the multi line password field specified in the FormObject
     */
    @Override
     public Object multiLinePassword(final FormObject form) {
        final PasswordField multipass = new PasswordField();
        final JavaFXControlListener controlListener = new JavaFXControlListener(multipass);
        final String textValue = form.getTextString();
        final StringBuilder styleBuilder = new StringBuilder(200);
        final int maxLength = form.getInt(PdfDictionary.MaxLen);
        
        if (maxLength != -1){
            multipass.setText(textValue);
            multipass.textProperty().addListener(new ChangeListener<String>() {
               @Override
               public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                   if(newValue.length() >= maxLength){
                       multipass.setText(oldValue);
                   }
               }
           });
        }
        
        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            multipass.setEditable(false);
        }
        setupUniversalFeatures(multipass, form, styleBuilder, controlListener);
        setBorder(form, styleBuilder);
        addFont(form, true, true, styleBuilder);
        setupTextFeatures(multipass, form);
        setToolTip(form, multipass);

        multipass.getStyleClass().add("formsMultiLine");
        multipass.setStyle(styleBuilder.toString());
        
        return multipass;
    }
    
    /**
     * setup and return the multi line text area specified in the FormObject
     */
    @Override
    public Object multiLineText(final FormObject form) {

        final boolean[] flags = form.getFieldFlags();
        final boolean[] characteristics = form.getCharacteristics();
        final TextArea comp = new TextArea(form.getTextString());
        final JavaFXControlListener controlListener = new JavaFXControlListener(comp);
        final StringBuilder styleBuilder = new StringBuilder(200);

        if (((flags != null) && (flags[FormObject.READONLY_ID]))
            || (characteristics != null && characteristics[9])//characteristics[9] = LockedContents
            //p609 PDF ref ver 1-7, characteristics 'locked' flag does allow contents to be edited,
            //but the 'LockedContents' flag stops content being changed.
            ) {

            if (form.isXFAObject()) {
                setToolTip(form, comp);
                comp.setEditable(false);
            } else {
                setToolTip(form, comp);
                comp.setEditable(false);
            }

        } else {//NOT read only
            setToolTip(form, comp);
        }
        comp.setWrapText(true);
        
        setupUniversalFeatures(comp, form, styleBuilder, controlListener);
        setBorder(form, styleBuilder);
        addFont(form, true, true, styleBuilder);
        
        // Simplifies removing the styles from inner Nodes unaccessable without being very hacky
        comp.getStyleClass().add("formsMultiLine");
        comp.setStyle(styleBuilder.toString());
        
        return comp;
    }
    
    /**
     * setup and return a signature field component,
     * <b>Note:</b> SKELETON METHOD FOR FUTURE UPGRADES.
     */
    @Override
    public Object signature(final FormObject form) {
        final StringBuilder styleBuilder = new StringBuilder(200);
        final Button sigBut = new Button();
        final JavaFXControlListener controlListener = new JavaFXControlListener(sigBut);
        
        
//        
//        boolean[] flags = form.getFieldFlags();
//        if (flags != null && flags[FormObject.READONLY_ID]) {
//            sigBut.setDisable(true);
//        }

        final String backgroundColor;
        if(!form.isAppearanceUsed()){
            //show as box similar to Acrobat if no values
            backgroundColor="rgb(221, 228, 255,0.7)";
        }else{
            backgroundColor="transparent";
        }
        
        setupUniversalFeatures(sigBut, form, styleBuilder, controlListener);
        setupButton(sigBut, form, controlListener);
        
        styleBuilder.append("-fx-padding:0;-fx-border:none;-fx-background-color:").append(backgroundColor).append(';');
        sigBut.setStyle(styleBuilder.toString());
        
        return sigBut;
    }
    
    /**
     * setup and return the Push button specified in the FormObject
     */
    @Override
    public Object pushBut(final FormObject form) {
        
        //the text value
        //setupButton Needs to be implemented
        final Button push = new Button();
        final JavaFXControlListener controlListener = new JavaFXControlListener(push);
        final StringBuilder styleBuilder = new StringBuilder(200);
        
        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            push.setDisable(true);
        }
        setupUniversalFeatures(push,form,styleBuilder, controlListener);
        setupButton(push, form, controlListener);
        addFont(form, false, false, styleBuilder);
        
        styleBuilder.append("-fx-padding:0;-fx-border:none;-fx-background-color:transparent;");
        push.setStyle(styleBuilder.toString());
        
        return push;
    }
    
    /**
     * setup and return the Radio button specified in the FormObject
     */
    @Override
    public Object radioBut(final FormObject form) {
        
        //setupButton Needs to be implemented
        //the text value
        final ToggleButton radioBut = new ToggleButton();
        final JavaFXControlListener controlListener = new JavaFXControlListener(radioBut);
        final StringBuilder styleBuilder = new StringBuilder(200);
        
        // Used to identify whether it can be deslected on click
        radioBut.setUserData(RADIOBUTTON);
        
        
        
        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            radioBut.setDisable(true);
            //Set opacity in css to make it replase the image to look the same but non clickable 
        }
        
        setupUniversalFeatures(radioBut, form, styleBuilder, controlListener);
        setupButton(radioBut, form, controlListener);
        setupToggleGroup(form, radioBut);
        addFont(form,false,false,styleBuilder);
        
        styleBuilder.append("-fx-padding:0;-fx-border:none;-fx-background-color:transparent;");
        radioBut.selectedProperty().addListener(new JavaFXRadioListener(radioBut,form));
        radioBut.setStyle(styleBuilder.toString());
        
        return radioBut;
    }

    
    /**
     * setup and return the single line password field specified in the FormObject
     */
    @Override
   public Object singleLinePassword(final FormObject form) {
        
        final PasswordField newPassword = new PasswordField();
        final JavaFXControlListener controlListener = new JavaFXControlListener(newPassword);
        final StringBuilder styleBuilder = new StringBuilder(200);
        
        //set length
        final int maxLength = form.getInt(PdfDictionary.MaxLen);
        if (maxLength != -1) {
           newPassword.getLength();  
           newPassword.textProperty().addListener(new ChangeListener<String>() {

               @Override
               public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                   if(newValue.length() >= maxLength){
                       newPassword.setText(oldValue);
                   }
               }
           });
     }

        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            newPassword.setEditable(false);
        }
        
        setupUniversalFeatures(newPassword,form,styleBuilder, controlListener);
        addFont(form, false, false, styleBuilder);
        setBorder(form, styleBuilder);
        setToolTip(form,newPassword);
        
        newPassword.getStyleClass().add("formsSingleline");
        newPassword.setStyle(styleBuilder.toString());
        
        return newPassword;
    }
    
    /**
     * setup and return the single line text field specified in the FormObject
     */
    @Override
    public Object singleLineText(final FormObject form) {
        
        final boolean[] flags = form.getFieldFlags();
        final boolean[] characteristics = form.getCharacteristics();
        final TextField newComp = new TextField();
        final JavaFXControlListener controlListener = new JavaFXControlListener(newComp);
        final StringBuilder styleBuilder = new StringBuilder(200);
        
        //fix the form3 file.
        //check if appearance stream matches text string
        String aptext = readAPimagesForText(form);
        
        //strip out any ascii escape chars (ie &#10)
        if(aptext!=null && aptext.contains("&#")) {
            aptext = Strip.stripXML(aptext, true).toString();
        }
        
        //V value can be null so allow to be set if AP exists.
        //ie  /PDFdata/baseline_screens/abacus/eva_PDF_Felddefinitionen.pdf
        if(aptext!=null && !aptext.equals(form.getTextStreamValue(PdfDictionary.V))){
            //may need to check for inheritance in future if there are issues again.
            //use appearance text if different
            form.setTextStreamValue(PdfDictionary.V, aptext);
        }
        newComp.setText(form.getTextString());
        if (((flags != null) && (flags[FormObject.READONLY_ID]))
                || (characteristics!=null && characteristics[9])//characteristics[9] = LockedContents
                //p609 PDF ref ver 1-7, characteristics 'locked' flag does allow contents to be edited,
                //but the 'LockedContents' flag stops content being changed.
                ){
            newComp.setEditable(false);
        }
        
        
        setupUniversalFeatures(newComp, form, styleBuilder, controlListener);
        setupTextFeatures(newComp, form);
        setToolTip(form, newComp);
        setBorder(form, styleBuilder);
        addFont(form, false, false, styleBuilder);
        
        newComp.getStyleClass().add("formsSingleline");
        newComp.setStyle(styleBuilder.toString());
        
        return newComp;
    }
    
    //############ below is all text setup ################ TAG
    /**
     * sets up all the required attributes for all text fields
     */
    private static void setupTextFeatures(final TextField textcomp, final FormObject form) {
        //set text field alignment
        if (form.getAlignment() != -1){
            // Translate JTextField alignment constant to FX enum
            switch (form.getAlignment()){
                case 0: // Center
                    textcomp.setAlignment(Pos.CENTER);
                    break;
                case 2: // Left
                    textcomp.setAlignment(Pos.CENTER_LEFT);
                    break;
                case 4: // Right
                    textcomp.setAlignment(Pos.CENTER_RIGHT);
                    break;
                default:
                    
            }
        }
        /**
         * ensure we sync back to FormObject if altered
         */
        textcomp.setOnAction(new JavaFXDocumentListener(textcomp,form));
                }
   
        //################# below is buttons setup ################## TAG
    /**
     * sets up the buttons captions, images, etc
     * for normal, rollover, down and off or on if radio or check buttons
     */
    private void setupButton(final ButtonBase comp, final FormObject form, final JavaFXControlListener controlListener) {

        //transparancy
        //    	((AbstractButton) comp).setContentAreaFilled(false);//false for transparency
        final String normalCaption = form.getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);
        comp.setText(normalCaption);
        
        
        final String downCaption = form.getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.AC);
        final String rolloverCaption = form.getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.RC);
        
        if(!form.isAppearanceUsed() && ((downCaption != null && !downCaption.isEmpty()) || (rolloverCaption != null && !rolloverCaption.isEmpty()))) {
            controlListener.addMouseListener(new JavaFXFormButtonListener(normalCaption, rolloverCaption, downCaption));
        }

        if (form.isAppearanceUsed()) {
            setAPImages(form, comp, controlListener);
        }
        
        final int textPosition = form.getTextPosition();
        if (textPosition != -1) {
            /*
             * if there are any appearance images, then the text is set back to null,
             * if the textPosition needs to be setup you need to either set the text back here or not
             * set it to null in appearanceImages.
             *
             * If you need to set this up check file acodabb.pdf page 4 as it has an icon with text being
             * set to overlay the icon, which doesn't work.
             */
            switch (textPosition) {
                case 0:
                    
                    comp.setGraphic(null);
                    comp.setText(normalCaption); //seems to need reset
                    break;//0=caption only
                case 1:
                    comp.setText(null);
                    break;//1=icon only
                case 2:
                    comp.setContentDisplay(ContentDisplay.TOP);
                    break;//2=caption below icon
                case 3:
                    comp.setContentDisplay(ContentDisplay.BOTTOM);
                    break;//3=caption above icon
                case 4:
                    comp.setContentDisplay(ContentDisplay.LEFT);
                    break;//4=caption on right of icon
                case 5:
                    comp.setContentDisplay(ContentDisplay.RIGHT);
                    break;//5=caption on left of icon
                case 6:
                    comp.setText(null);
                    break;
            }
        }
        
        controlListener.addMouseListener((EventHandler)formsActionHandler.setHoverCursor());
    }
    
    /**
     * Sort out the groups for toggleable components (radioBut/checkBoxBut)
     * @param form
     * @param comp 
     */
    private void setupToggleGroup(final FormObject form, final ToggleButton comp){
        /**
         * put in group (just store first time as you cannot have 1 button in group)
         */
        String name=form.getTextStreamValue(PdfDictionary.T);
        if (name == null) {
            name = "";
        }
        
        ToggleGroup tg= (ToggleGroup) groups.get(name);
        if(tg==null){
            tg=new ToggleGroup();
            groups.put(name,tg);
            firstButtons.put(name,comp); //save to avoid buttonGroup if single
            
            final ToggleGroup group = tg;
            tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(final ObservableValue<? extends Toggle> observable, final Toggle oldValue, final Toggle newValue) {
                    final int newIndex = group.getToggles().indexOf(newValue);
                    final int oldIndex = group.getToggles().indexOf(oldValue);
                    
                    // Can't deselect with radio buttons
                    if(oldIndex > -1 && newIndex == -1 && oldValue.getUserData().equals(RADIOBUTTON)){
                        oldValue.setSelected(true);
                    }
                }
            });
            
        }else{
            
            //add any first comp
            final ToggleButton firstButton= (ToggleButton) firstButtons.get(name);
            
            if(firstButton!=null){
                firstButtons.remove(name);
                tg.getToggles().add(firstButton);
            }
            tg.getToggles().add(comp);
        }
        /**
         * ensure we sync back to FormObject if altered
         */
//        new JavaFXRadioListener(comp,form);
    }
    
    /**
     * gets each appearance image from the map <b>appearance</b> and
     * and adds it to the relevant icon for the AbstractButton <b>comp</b>
     * showImages is to display the appearance images for that FormObject
     */
    private void setAPImages(final FormObject form, final ButtonBase comp, final JavaFXControlListener controlListener) {
        
        JavaFXImageIcon normal = null;
        JavaFXImageIcon selected = null;
        JavaFXImageIcon rollover = null;
        JavaFXImageIcon rolloverSelected = null;
        JavaFXImageIcon down = null;
        
        final PdfObject APobjN = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.N);
        final PdfObject APobjD = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.D);
        final PdfObject APobjR = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.R);
        PdfObject normalOffDic=null,normalOnDic=null,downOffDic=null,downOnDic=null,rollOffDic=null,rollOnDic=null;
        
        //if 0 no change, 1 offset image, 2 invert image
        int offsetDownImage = 0;
        
        final int subtype = form.getParameterConstant(PdfDictionary.Subtype);
        
        if(APobjN!=null || form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I) !=null){
            
            //if we have a root stream then it is the off value
            //check in order of N Off, MK I, then N
            //as N Off overrides others and MK I is in preference to N
            if(APobjN.getDictionary(PdfDictionary.Off) !=null){
                normalOffDic = APobjN.getDictionary(PdfDictionary.Off);
            }else if(form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I) !=null
                    && form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.IF)==null){
                //look here for MK IF
                //if we have an IF inside the MK then use the MK I as some files shown that this value is there
                //only when the MK I value is not as important as the AP N.
                normalOffDic = form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I);
            }else if(APobjN.getDecodedStream()!=null){
                normalOffDic = APobjN;
            }
            
            if(normalOffDic!=null){
                comp.setText(null);
                normal = new JavaFXImageIcon(comp, form,normalOffDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,0);
            }
            
            if(APobjN.getDictionary(PdfDictionary.On) !=null){
                normalOnDic = APobjN.getDictionary(PdfDictionary.On);
                form.setNormalOnState("On");
            }else {
                final Map otherValues=APobjN.getOtherDictionaries();
                if(otherValues!=null && !otherValues.isEmpty()){
                    final Iterator keys=otherValues.keySet().iterator();
                    PdfObject val;
                    String key;
                    while(keys.hasNext()){
                        key=(String)keys.next();
                        val=(PdfObject)otherValues.get(key);
                        normalOnDic = val;
                        form.setNormalOnState(key);
                    }
                }
            }
            
            if(normalOnDic!=null){
                comp.setText(null);
                selected = new JavaFXImageIcon(comp, form,normalOnDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,0);
                if(normal == null){
                    normal = new JavaFXImageIcon(comp,form,(Image)null,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R));
                }
            }
        }
        
        if (!form.hasNoDownIcon()) {
            if(form.hasOffsetDownIcon()) {
                offsetDownImage = 1;
            } else if(form.hasInvertDownIcon()) {
                offsetDownImage = 2;
            }
            
            if(offsetDownImage!=0){
                if (normalOffDic!=null) {
                    if (normalOnDic!=null) {
                        downOffDic = normalOffDic;
                        downOnDic = normalOnDic;
                    } else {
                        downOffDic = normalOffDic;
                    }
                } else if (normalOnDic!=null) {
                    downOffDic = normalOnDic;
                }
            }
            
            
            if(APobjD!=null){
                //down off
                //if we have a root stream then it is the off value
                if(APobjD.getDecodedStream()!=null){
                    downOffDic = APobjD;
                }else if(APobjD.getDictionary(PdfDictionary.Off) !=null){
                    downOffDic = APobjD.getDictionary(PdfDictionary.Off);
                }
                
                //down on
                if(APobjD.getDictionary(PdfDictionary.On) !=null){
                    downOnDic = APobjD.getDictionary(PdfDictionary.On);
                }else {
                    final Map otherValues=APobjD.getOtherDictionaries();
                    if(otherValues!=null && !otherValues.isEmpty()){
                        final Iterator keys=otherValues.keySet().iterator();
                        PdfObject val;
                        String key;
                        while(keys.hasNext()){
                            key=(String)keys.next();
                            val=(PdfObject)otherValues.get(key);
                            downOnDic = val;
                        }
                    }
                }
            }
            
            if (downOffDic == null || downOnDic == null) {
                if (downOffDic != null) {
                    down=new JavaFXImageIcon(comp,form,downOffDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,offsetDownImage);
                } else if (downOnDic != null) {
                    down=new JavaFXImageIcon(comp,form,downOnDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,offsetDownImage);
                }
            } else {
                down = new JavaFXImageIcon(comp,form,downOnDic,downOffDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),comp.isPressed()?1:0,currentPdfFile,subtype,offsetDownImage);
            }
        }
        
        if (APobjR!=null) {
            //if we have a root stream then it is the off value
            if(APobjR.getDecodedStream()!=null){
                rollOffDic = APobjR;
            }else if(APobjR.getDictionary(PdfDictionary.Off) !=null){
                rollOffDic = APobjR.getDictionary(PdfDictionary.Off);
            }
            
            if(rollOffDic!=null){
                rollover = new JavaFXImageIcon(comp,form,rollOffDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,0);
            }
            
            //if we have a root stream then it is the off value
            if(APobjR.getDictionary(PdfDictionary.On) !=null){
                rollOnDic = APobjR.getDictionary(PdfDictionary.On);
            }else {
                final Map otherValues=APobjR.getOtherDictionaries();
                if(otherValues!=null && !otherValues.isEmpty()){
                    final Iterator keys=otherValues.keySet().iterator();
                    PdfObject val;
                    String key;
                    while(keys.hasNext()){
                        key=(String)keys.next();
                        val=(PdfObject)otherValues.get(key);
                        rollOnDic = val;
                    }
                }
            }
            
            if(rollOnDic!=null){                
                rolloverSelected = new JavaFXImageIcon(comp,form,rollOnDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,0);
                if(rollover==null){
                    rollover = new JavaFXImageIcon(comp,form,(Image)null,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R));
                }
            }
        }
        
        
        //moved to end as flagLastUsed can call the imageicon
        if (form.isSelected() && down != null && comp instanceof ToggleButton){
            ((ToggleButton)comp).setSelected(true);
            down.swapImage(true);
        }

        createListener(comp, normal, down, selected, rollover, rolloverSelected, controlListener);
    }
    
    /**
     * Create a set of listeners for form buttons 
     * @param comp
     * @param normal
     * @param down
     * @param selected
     * @param rollover
     * @param rolloverSelected 
     */
    @SuppressWarnings("null") // Warns null on comp.setGraphic... but it should never be null
    private static void createListener(final ButtonBase comp, final JavaFXImageIcon normal, final JavaFXImageIcon down, final JavaFXImageIcon selected,
                                       final JavaFXImageIcon rollover, final JavaFXImageIcon rolloverSelected, final JavaFXControlListener controlListener){
        // Convenience method
        final boolean isToggleButton = comp instanceof ToggleButton;
        
        comp.setGraphic(normal);
        
        if(down != null){
            
            controlListener.addOnMousePressedListener(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent event) {
                    comp.setText(null);
                    comp.setGraphic(down);
                }
            });
            controlListener.addOnMouseReleasedListener(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent event) {
                    resetToDefault(isToggleButton, selected, normal, comp);
                }
            });
            // Touch controls
            controlListener.addOnTouchPressedListener(new EventHandler<TouchEvent>() {
                @Override public void handle(final TouchEvent event) {
                    comp.setText(null);
                    comp.setGraphic(down);
                }
            });
            controlListener.addOnTouchReleasedListener(new EventHandler<TouchEvent>() {
                @Override public void handle(final TouchEvent event) {
                    resetToDefault(isToggleButton, selected, normal, comp);
                }
            });
            
        }

        controlListener.addOnMouseExitedListener(new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent event) {
                resetToDefault(isToggleButton, selected, normal, comp);
            }
        });
        
        if(rollover != null){
            controlListener.addOnMouseEnteredListener(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent event) {
                    comp.setText(null);
                    if(isToggleButton && ((ToggleButton)comp).isSelected() && rolloverSelected != null){
                        comp.setGraphic(rolloverSelected);
                    }else{
                        comp.setGraphic(rollover);
                    }
                }
            });
        }
        
        if(isToggleButton){
            final ToggleButton tb = (ToggleButton)comp;
            if(tb.isSelected()){
                tb.setGraphic(selected);
            }
            tb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
                    if(down != null) {
                        down.swapImage(newValue);
                    }
                    selected.swapImage(newValue);
                    resetToDefault(isToggleButton, selected, normal, comp);
                }
            });
        }
    }
    
    private static void resetToDefault(final boolean isToggleButton, final JavaFXImageIcon selected, final JavaFXImageIcon normal, final ButtonBase comp){
        comp.setText(null);
        if(isToggleButton && ((ToggleButton)comp).isSelected() && selected!=null){
            comp.setGraphic(selected);
        }else{
            comp.setGraphic(normal);
        }
        
    }
    
    private void setupUniversalFeatures(final Control comp, final FormObject form, final StringBuilder styleBuilder, final JavaFXControlListener controlListener) {
        // Stub method for now
        
        final int[] textColor = FormsCSSHelper.getTextColor(form);
        if(textColor == null){
            styleBuilder.append("-fx-text-fill:rgb(0, 0, 0);");
        }else{
            styleBuilder.append(String.format("-fx-text-fill:rgb(%d, %d, %d);", textColor[0],textColor[1],textColor[2]));
        }
        
        final int[] backgroundColor = FormsCSSHelper.getBackgroundColor(form);
        if(backgroundColor == null){
            styleBuilder.append("-fx-background-color:transparent;");
        }else{
            styleBuilder.append(String.format("-fx-background-color:rgb(%d, %d, %d);", backgroundColor[0],backgroundColor[1],backgroundColor[2]));
        }
        if(controlListener != null) {
            setupMouseListener(comp, form, controlListener);
        }
    }
    
    private void addFont(final FormObject formObject, final boolean area, final boolean isMultiLine, final StringBuilder styleBuilder) {
        final String fontData = FormsCSSHelper.addFont(formObject, area, isMultiLine, pageData, formObject.getPageNumber(), 1f);
        if(formObject.isAppearanceUsed() && formObject.getFormType() == FormFactory.RADIOBUTTON){
            // Makes text next to radiobuttons invisible if we're using the AP stream (stops ... appearing next to buttons)
            styleBuilder.append("-fx-text-fill:transparent;");
        }else{
            styleBuilder.append("-fx-font:").append(fontData).append(';');
        }
    }

    /**
     * setup the events for currentComp, from the specified parameters
     *
     * @Action - mouse events added here
     */
    private void setupMouseListener(final Control comp, final FormObject form, final JavaFXControlListener controlListener) {
        /* bit 1 is index 0 in []
         * 1 = invisible
         * 2 = hidden - dont display or print
         * 3 = print - print if set, dont if not
         * 4 = nozoom
         * 5= norotate
         * 6= noview
         * 7 = read only (ignored by wiget)
         * 8 = locked
         * 9 = togglenoview
         * 10 = LockedContent
         */
        
        final boolean[] characteristic = form.getCharacteristics();//F
        if (characteristic[0] || characteristic[1] || characteristic[5]) {
            comp.setVisible(false);
        }
        
        final JavaFXFormsListener jpedalListener = new JavaFXFormsListener(form, formsActionHandler);
        
        controlListener.addMouseListener(jpedalListener.getMouseHandler());
        controlListener.addKeyListener(jpedalListener.getKeyHandler());
        controlListener.addFocusEvent(jpedalListener.getFocusHandler());
        
        //if combobox wee need to add the listener to the component at position 0 as well as the normal one, so it works properly.
//        if (currentComp instanceof JComboBox) {
//            ((JComboBox) currentComp).getComponent(0).addMouseListener(jpedalListener);
//            ((JComboBox) currentComp).getComponent(0).addKeyListener(jpedalListener);
//            ((JComboBox) currentComp).getComponent(0).addFocusListener(jpedalListener);
//            ((JComboBox)currentComp).addActionListener(jpedalListener);
//        }
//        if(currentComp instanceof JList){
//            ((JList)currentComp).addListSelectionListener(jpedalListener);
//        }
        
        final PdfObject aData=form.getDictionary(PdfDictionary.A);
        if(aData!=null && aData.getNameAsConstant(PdfDictionary.S)==PdfDictionary.URI){
            
            final String noLinkToolTips=System.getProperty("org.jpedal.noURLaccess");
            
            if(noLinkToolTips==null || !noLinkToolTips.equals("true")){
                final String text=aData.getTextStreamValue(PdfDictionary.URI); //+"ZZ"; deliberately broken first to test checking
//                ((JComponent) currentComp).setToolTipText(text);
                comp.setTooltip(new Tooltip(text));
            }
        }
    }
    
    private static void setToolTip(final FormObject formObject, final Control retComponent) {
        //TU seems to be used as a tooltip in text fields so added
        final String userName = formObject.getTextStreamValue(PdfDictionary.TU);
        if(userName!=null && !userName.isEmpty()){
            retComponent.setTooltip(new Tooltip(userName));
        }
    }
    
    /**
     * new data object to hold all widget implementations
     */
    @Override
    public GUIData getCustomCompData() {
        return new JavaFXData();
    }
    
    
    @Override
    public int getType() {
        return FormFactory.JAVAFX;
    }
    
    /**
     * pass in Map contains annot field list in order to set tabindex
     */
    @Override
    public void setAnnotOrder(final Map<String, String> annotOrder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    
    /**
     * public method to allow user to replace Popup with their own implementation
     * @param form
     * @return Swing component to use as popup (see org.jpedal.objects.acroforms.overridingImplementations.PdfSwingPopup)
     */
    @SuppressWarnings("MethodMayBeStatic")
    public Object getPopupComponent(final FormObject form, final int cropBoxWith) {
        return new PdfJavaFXPopup(form,cropBoxWith);
    }
    
    private static void setBorder(final FormObject formObject, final StringBuilder styleBuilder){
        int[] borderColor = FormsCSSHelper.getBorderColor(formObject);
        final int[] backgroundColor = {255,255,255};
        
        final int width = FormsCSSHelper.getBorderWidth(formObject);
        
        if(borderColor == null && formObject.isXFAObject()){
            borderColor = new int[]{0,0,0};
        }
        
        if(borderColor == null || width == 0){ 
            return;
        }
        
        final String foreColor = String.format("rgb(%d,%d,%d) ", borderColor[0], borderColor[1], borderColor[2]);
        final String backColor = String.format("rgb(%d,%d,%d) ", backgroundColor[0], backgroundColor[1], backgroundColor[2]);
        String borderStyleColor = "-fx-border-color:"+foreColor+ ';';
        String borderStyle="-fx-border-style:solid;";
        String borderWidth="-fx-border-width:"+width+"px;";
        
        int style=PdfDictionary.S;
        final PdfObject BS = formObject.getDictionary(PdfDictionary.BS);
        if(BS!=null){
            style=BS.getNameAsConstant(PdfDictionary.S);
            if(style==PdfDictionary.Unknown){
                style=PdfDictionary.S;
            }
        }
        
        if(formObject.isXFAObject()){
            final int[] t = formObject.getMatteBorderDetails();
            borderWidth = String.format("-fx-border-width:%d %d %d %d;", t[0], t[1], t[2], t[3]);
        }else{
            switch(style){
                case PdfDictionary.U:
                    //single line at bottom of box
                    borderWidth = "-fx-border-width:0 0 "+width+"px 0;";
                    break;

                case PdfDictionary.I:
                    //inset(engraved appeared to be below page)
                    borderStyleColor = "-fx-border-color:"+foreColor+backColor+backColor+foreColor+ ';';
                    break;

                case PdfDictionary.B:
                    //beveled(embossed appears to above page)
                    borderStyleColor = "-fx-border-color:"+backColor+foreColor+foreColor+backColor+ ';';
                    break;

                case PdfDictionary.S:
                    // Same as defaults
                    break;

                case PdfDictionary.D:
                    borderStyle = "-fx-border-style:dashed;";
                    break;

            }
        }

        styleBuilder.append(borderStyleColor).append(borderStyle).append(borderWidth);
    }
}

