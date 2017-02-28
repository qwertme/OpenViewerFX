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
 * SwingFormFactory.java
 * ---------------
 */
package org.jpedal.objects.acroforms.creation;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jpedal.color.DeviceCMYKColorSpace;

import org.jpedal.objects.acroforms.actions.SwingDownIconListener;
import org.jpedal.objects.acroforms.actions.SwingFormButtonListener;
import org.jpedal.objects.acroforms.actions.SwingListener;
import org.jpedal.objects.acroforms.overridingImplementations.PdfSwingPopup;
import org.jpedal.objects.raw.*;
import org.jpedal.objects.acroforms.GUIData;
import org.jpedal.objects.acroforms.SwingData;
import org.jpedal.objects.acroforms.overridingImplementations.FixImageIcon;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Strip;

import java.util.Iterator;
import java.util.Map;


public class SwingFormFactory extends GenericFormFactory implements FormFactory{
    
    
    /**
     * allows access to renderer variables
     *
     */
    public SwingFormFactory() {}
    
    
    private JButton setupAnnotationButton(final FormObject form){
        //point where testActions breaks - ignore this halting error as it is within the testActions flag.
        final JButton but = new JButton();
        
        setupButton(but, form);
        setupUniversalFeatures(but, form);
        
        return but;
    }
    
    private JButton createAnntoationHighlight(final FormObject form) {
        JButton but = setupAnnotationButton(form);
        but.setBackground(new Color(0, 0, 0, 0));
        but.setIcon(new FixImageIcon(form, PopupFactory.getIcon(form), 0));
        return but;
    }
    
    private JButton createAnnotationFreeText(final FormObject form){
        JButton but = setupAnnotationButton(form);

        but.setText("<html>" + form.getTextStreamValue(PdfDictionary.Contents) + "</html>");

        final Font font = new Font("TimesRoman", Font.PLAIN, 12);
        form.setTextSize(12);
        but.setFont(font);

        form.setFontName("TimesRoman");
        form.setTextFont(font);
        return but;
    }
                    
    private JButton createAnnotationText(final FormObject form){
        JButton but = setupAnnotationButton(form);

        final int rot = pageData.getRotation(form.getPageNumber());

        final BufferedImage commentIcon = PopupFactory.getIcon(form);

        if (commentIcon != null) {
            //Ensure sized correctly
            final float[] rect = form.getFloatArray(PdfDictionary.Rect);
            // rect[0] = rect[0];
            rect[1] = rect[3] - commentIcon.getHeight();
            rect[2] = rect[0] + commentIcon.getWidth();
            //   rect[3] = rect[3];
            form.setFloatArray(PdfDictionary.Rect, rect);
        }
        but.setIcon(new FixImageIcon(form, commentIcon, rot));

        return but;
    }
    
    private JComponent createAnnotationPopup(final FormObject form) {
    
        JComponent comp = (JComponent) getPopupComponent(form, pageData.getCropBoxWidth(form.getPageNumber()));
        form.setGUIComponent(comp, FormFactory.SWING);
        //set visibility
        comp.setVisible(form.getBoolean(PdfDictionary.Open));

        return comp;

    }
    
    private JButton createAnnotationUnderline(final FormObject form) {
        JButton but = setupAnnotationButton(form);
        Color color = getAnnotationColor(form);
        but.setBounds(form.getBoundingRectangle());

        float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad == null) {
            quad = form.getFloatArray(PdfDictionary.Rect);
        }

        final BufferedImage icon = new BufferedImage(form.getBoundingRectangle().width, form.getBoundingRectangle().height, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics g = icon.getGraphics();
                    //        			g.setColor(Color.blue);
        //        			g.fillRect(0,0, icon.getWidth(), icon.getHeight());
        if (quad.length >= 8) {
            for (int hi = 0; hi != quad.length; hi += 8) {
                final int x = (int) quad[hi] - form.getBoundingRectangle().x;
                int y = (int) quad[hi + 5] - form.getBoundingRectangle().y;
                //Adjust y for display
                y = (form.getBoundingRectangle().height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                final int width = (int) (quad[hi + 2] - quad[hi]);
                final int height = (int) (quad[hi + 1] - quad[hi + 5]);
                final Rectangle rh = new Rectangle(x, y, width, height);

                try {
                    g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
                    g.fillRect(rh.x, rh.y, rh.width, rh.height);
                    g.setColor(color);
                    g.fillRect(rh.x, rh.y + rh.height - 1, rh.width, 1);
                    but.setBackground(new Color(0, 0, 0, 0));
                    but.setIcon(new FixImageIcon(form, icon, 0));
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
    
    private JButton createAnnotationInk(final FormObject form) {
        JButton but = setupAnnotationButton(form);

        //we need this bit
        but.setToolTipText(form.getTextStreamValue(PdfDictionary.Contents));

        final Object[] InkListArray = form.getObjectArray(PdfDictionary.InkList);

        //resize ink size if entire ink is not contained
        final float[] r = scanInkListTree(InkListArray, form, null);
        form.setFloatArray(PdfDictionary.Rect, new float[]{r[0], r[1], r[2], r[3]});

        //Create image to draw to
        final BufferedImage icon1 = new BufferedImage(form.getBoundingRectangle().width, form.getBoundingRectangle().height, BufferedImage.TYPE_4BYTE_ABGR);
        scanInkListTree(InkListArray, form, icon1.getGraphics());

        //Add image to button
        but.setBackground(new Color(0, 0, 0, 0));
        but.setIcon(new FixImageIcon(form, icon1, 0));

        return but;
    }
    
    private JButton createAnnotationStrikeOut(final FormObject form) {
        JButton but = setupAnnotationButton(form);

        Color color = getAnnotationColor(form);

        float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad == null) {
            quad = form.getFloatArray(PdfDictionary.Rect);
        }

        final BufferedImage icon = new BufferedImage(form.getBoundingRectangle().width, form.getBoundingRectangle().height, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics g = icon.getGraphics();
                    //        			g.setColor(Color.blue);
        //        			g.fillRect(0,0, icon.getWidth(), icon.getHeight());
        if (quad.length >= 8) {
            for (int hi = 0; hi != quad.length; hi += 8) {
                final int x = (int) quad[hi] - form.getBoundingRectangle().x;
                int y = (int) quad[hi + 5] - form.getBoundingRectangle().y;
                //Adjust y for display
                y = (form.getBoundingRectangle().height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                final int width = (int) (quad[hi + 2] - quad[hi]);
                final int height = (int) (quad[hi + 1] - quad[hi + 5]);
                final Rectangle rh = new Rectangle(x, y, width, height);

                try {
                    g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
                    g.fillRect(0, 0, rh.width, rh.height);
                    g.setColor(color);
                    g.fillRect(rh.x, rh.y + (rh.height / 2), rh.width, 1);
                    but.setBackground(new Color(0, 0, 0, 0));
                    but.setIcon(new FixImageIcon(form, icon, 0));
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
        Color color = new Color(0);
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
    
    private static float[] scanInkListTree(final Object[] InkListArray, final FormObject form, final Graphics g) {
        
        
        float minX = 0;
        float minY = 0;
        float maxX = 0;
        float maxY = 0;
        
        float[] vals = null;
        final Graphics2D g2 = (Graphics2D) g;
        //if specific DecodeParms for each filter, set othereise use global
        if(InkListArray !=null){
            
            final int count= InkListArray.length;
            
            float x;
            float y;
            
            boolean isFirstPoint = true;
            
            //If Graphics not set, don't draw anything.
            if(g!=null){
                final float[] underlineColor = form.getFloatArray(PdfDictionary.C);
                Color c1 = new Color(0);
                if(underlineColor!=null){
                    switch(underlineColor.length){
                        case 0:
                            //Should not happen. Do nothing. Annotation is transparent
                            break;
                        case 1:
                            //DeviceGrey colorspace
                            c1 = new Color(underlineColor[0],underlineColor[0],underlineColor[0],1.0f);
                            break;
                        case 3:
                            //DeviceRGB colorspace
                            c1 = new Color(underlineColor[0],underlineColor[1],underlineColor[2],1.0f);
                            break;
                        case 4:
                            //DeviceCMYK colorspace
                            final DeviceCMYKColorSpace cmyk = new DeviceCMYKColorSpace();
                            cmyk.setColor(underlineColor, 4);
                            c1 = new Color(cmyk.getColor().getRGB());
                            
                            break;
                        default:
                            break;
                    }
                }
                
                g2.setColor(new Color(0.0f,0.0f,0.0f,0.0f));
                g2.fillRect(0, 0, form.getBoundingRectangle().width, form.getBoundingRectangle().height);
                g2.setColor(c1);
                g2.setPaint(c1);
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
                    final float[] r = scanInkListTree((Object[]) InkListArray[i], form, g);
                    if(isFirstPoint){
                        minX = r[0];
                        maxX = r[2];
                        minY = r[1];
                        maxY = r[3];
                        isFirstPoint = false;
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
            if(vals.length<6){ //Only use lines on ink
            for(int i=0; i<vals.length; i++){
                if(i%2==0){ //X coord
                    if(vals[i]<minX) {
                        minX = vals[i];
                    }
                    
                    if(vals[i]>maxX) {
                        maxX = vals[i];
                    }
                }else{ //Y coord
                    if(vals[i]<minY) {
                        minY = vals[i];
                    }
                    
                    if(vals[i]>maxY) {
                        maxY = vals[i];
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
            
            if(g2!=null){
                
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.52f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                for(int i=0; i<vals.length; i+=4){
                    final Line2D.Float line = new Line2D.Float(vals[0], vals[1], vals[2], vals[3]);
                    g2.draw(line);
                }
            }
            }else{ //Enough armguments so curve ink
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
            
            if(g2!=null){
                
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.52f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                for(int i=0; i<values.length; i+=8){
                    
                    
                    final CubicCurve2D curve = new CubicCurve2D.Double(values[i]+xOffset, values[i+1]+yOffset, values[i+2]+xOffset, values[i+3]+yOffset
                            , values[i+4]+xOffset, values[i+5]+yOffset, values[i+6]+xOffset, values[i+7]+yOffset);
                    g2.draw(curve);
                }
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
        final String[] items = form.getItemsList();
        final JComboBox comboBox;
        if (items == null) {
            comboBox = new JComboBox();
        } else{
            
            comboBox = new JComboBox(items);
            
            /**
             * allow background colour in cells
             */
            final Color backgroundColor = FormObject.generateColor(form.getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BG));
            if(backgroundColor!=null){
                final ListCellRenderer renderer = new ComboColorRenderer(backgroundColor);
                comboBox.setRenderer(renderer);
            }
        }
        
        //get and set currently selected value
        final String textValue = form.getSelectedItem();
        if (form.getValuesMap(true) != null) {
            comboBox.setSelectedItem(form.getValuesMap(true).get(textValue));
        } else {
            comboBox.setSelectedItem(textValue);
        }
        
        //sync to FormObject
        final int selectionIndex=comboBox.getSelectedIndex();
        form.setSelection(comboBox.getSelectedObjects(), (String) comboBox.getSelectedItem(), new int[]{selectionIndex},selectionIndex);
        
        final boolean[] flags = form.getFieldFlags();
        if (flags[FormObject.EDIT_ID]) {//FormObject.EDIT_ID
            
            comboBox.setEditable(true);
            
        } else {//is not editable
            
            comboBox.setEditable(false);
        }
        
        setupUniversalFeatures(comboBox, form);
        
        if(flags[FormObject.READONLY_ID]) {
            comboBox.setEditable(false);//combo box
            comboBox.setEnabled(false);
        }
        
        // <start-demo><end-demo>
        
        //listener to keep synced
        comboBox.addItemListener(new ComboListener(comboBox,form));
        
        return comboBox;
    }
    
    /**
     * setup and return the CheckBox button specified in the FormObject
     */
    @Override
    public Object checkBoxBut(final FormObject form) {
        
        final JCheckBox checkBut = new JCheckBox();
        
        setupButton(checkBut, form);
        
        setupUniversalFeatures(checkBut, form);
        
        if(checkBut.getBorder()!=null){
            checkBut.setBorderPainted(true);
        }
        
        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            checkBut.setEnabled(false);
            checkBut.setDisabledIcon(checkBut.getIcon());
            checkBut.setDisabledSelectedIcon(checkBut.getSelectedIcon());
        }
        
        return checkBut;
    }
    
    /**
     * setup and return the List field specified in the FormObject
     */
    @Override
    public Object listField(final FormObject form) {
        
        //populate the items array with list from Opt
        final String[] items = form.getItemsList();
        
        //create list (note we catch null value)
        final JList list;
        if (items != null) {
            list = new JList(items);
        } else {
            list = new JList();
        }
        
        if (!form.getFieldFlags()[FormObject.MULTISELECT_ID])//mulitselect
        {
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        
        //if there is a top index or selected value select it
        if (form.getSelectionIndices() != null) {
            list.setSelectedIndices(form.getSelectionIndices());
        } else  if (form.getValuesMap(true) != null) {
            list.setSelectedValue(form.getValuesMap(true).get(form.getSelectedItem()), true);
        } else {
            list.setSelectedValue(form.getSelectedItem(), true);
        }
        
        
        //sync to FormObject
        form.setSelection(list.getSelectedValues(), (String) list.getSelectedValue(), list.getSelectedIndices(),list.getSelectedIndex());
        
        setupUniversalFeatures(list, form);
        
        //listener to keep synced
        list.addListSelectionListener(new ListListener(list, form));
        
        return list;
    }
    
    /**
     * setup and return the multi line password field specified in the FormObject
     */
    @Override
    public Object multiLinePassword(final FormObject form) {
        
        final JPasswordField multipass;
        final String textValue = form.getTextString();
        final int maxLength = form.getInt(PdfDictionary.MaxLen);
        
        if (maxLength != -1) {
            multipass = new JPasswordField(textValue, maxLength);
        } else {
            multipass = new JPasswordField(textValue);
        }
        
        multipass.setEchoChar('*');
        
        setupUniversalFeatures(multipass, form);
        
        setupTextFeatures(multipass, form);
        
        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            multipass.setEditable(false);
        }
        
        setToolTip(form, multipass);
        
        return multipass;
    }
    
    /**
     * setup and return the multi line text area specified in the FormObject
     */
    @Override
    public Object multiLineText(final FormObject form) {
        
        final boolean[] flags = form.getFieldFlags();
        final boolean[] characteristics = form.getCharacteristics();
        
        final JComponent comp;
        final String text = form.getTextString();
        
        if (flags != null && flags[FormObject.READONLY_ID] ||
                characteristics!=null && characteristics[9]) {
            
            final JTextPane newTextarea = new JTextPane();
            newTextarea.setText(text);
//            newTextarea.setLineWrap(true);
//            newTextarea.setWrapStyleWord(true);
            
            newTextarea.setEditable(false);
            
            if (form.getAlignment() != -1){
            	final StyledDocument doc = newTextarea.getStyledDocument();
            	final SimpleAttributeSet center = new SimpleAttributeSet();
            	
            	switch(form.getAlignment()){
            	case SwingConstants.CENTER : 
            		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            		break;
            	case SwingConstants.RIGHT : 
            		StyleConstants.setAlignment(center, StyleConstants.ALIGN_RIGHT);
            		break;
            	}
            	
            	doc.setParagraphAttributes(0, doc.getLength(), center, false);
            }
            
            /**
             * ensure we sync back to FormObject if altered
             */
            newTextarea.getDocument().addDocumentListener(new TextDocumentListener(newTextarea,form));
            
            comp = newTextarea;
            
        }else {
            
        	final JTextPane newTextarea = new JTextPane();
            newTextarea.setText(text);
//          newTextarea.setLineWrap(true);
//          newTextarea.setWrapStyleWord(true);
          
          if (form.getAlignment() != -1){
          	final StyledDocument doc = newTextarea.getStyledDocument();
          	final SimpleAttributeSet center = new SimpleAttributeSet();
          	
          	switch(form.getAlignment()){
          	case SwingConstants.CENTER : 
          		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
          		break;
          	case SwingConstants.RIGHT : 
          		StyleConstants.setAlignment(center, StyleConstants.ALIGN_RIGHT);
          		break;
          	}
          	
          	doc.setParagraphAttributes(0, doc.getLength(), center, false);
          }
            /**
             * ensure we sync back to FormObject if altered
             */
            newTextarea.getDocument().addDocumentListener(new TextDocumentListener(newTextarea,form));
            
            comp = newTextarea;
            
        }
        
        setToolTip(form, comp);
        setupUniversalFeatures(comp, form);
        
        return comp;
    }
    
    /**
     * setup and return a signature field component,
     * <b>Note:</b> SKELETON METHOD FOR FUTURE UPGRADES.
     */
    @Override
    public Object signature(final FormObject form) {
        
        final JButton sigBut = new JButton();
        
        setupButton(sigBut, form);
        
        setupUniversalFeatures(sigBut, form);
        
        final boolean[] flags = form.getFieldFlags();
        if (flags != null && flags[FormObject.READONLY_ID]) {
            sigBut.setEnabled(false);
            sigBut.setDisabledIcon(sigBut.getIcon());
            sigBut.setDisabledSelectedIcon(sigBut.getSelectedIcon());
        }
        
        //show as box similar to Acrobat if no values
        if(!form.isAppearanceUsed()){
            sigBut.setOpaque(false);
            final BufferedImage img=new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D imgG2=img.createGraphics();
            imgG2.setPaint(new Color(221,228,255,175));
            imgG2.fillRect(0,0,1,1);
            sigBut.setIcon(new FixImageIcon(form,img,0));
            
            // sigBut.setBackground(new Color(221,228,255,175)); //r,g,b,a
        }
        
        return sigBut;
    }
    
    /**
     * setup and return the Push button specified in the FormObject
     */
    @Override
    public Object pushBut(final FormObject form) {
        
        //the text value
        final JButton pushBut = new JButton();
        
        setupButton(pushBut, form);
        
        setupUniversalFeatures(pushBut, form);
        
        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            pushBut.setEnabled(false);
            pushBut.setDisabledIcon(pushBut.getIcon());
            pushBut.setDisabledSelectedIcon(pushBut.getSelectedIcon());
        }
        
        return pushBut;
    }
    
    /**
     * setup and return the Radio button specified in the FormObject
     */
    @Override
    public Object radioBut(final FormObject form) {
        
        //the text value
        final JRadioButton radioBut = new JRadioButton();
        
        setupButton(radioBut, form);
        
        setupUniversalFeatures(radioBut, form);
        
        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            radioBut.setEnabled(false);
            radioBut.setDisabledIcon(radioBut.getIcon());
            radioBut.setDisabledSelectedIcon(radioBut.getSelectedIcon());
        }
        
        return radioBut;
    }
    
    
    /**
     * setup and return the single line password field specified in the FormObject
     */
    @Override
    public Object singleLinePassword(final FormObject form) {
        
        final JPasswordField newPassword = new JPasswordField(form.getTextString());
        newPassword.setEchoChar('*');
        
        //set length
        final int maxLength = form.getInt(PdfDictionary.MaxLen);
        if (maxLength != -1) {
            newPassword.setColumns(maxLength);
        }
        
        setupUniversalFeatures(newPassword, form);
        
        setupTextFeatures(newPassword, form);
        
        final boolean[] flags = form.getFieldFlags();
        if ((flags != null) && (flags[FormObject.READONLY_ID])) {
            newPassword.setEditable(false);
        }
        
        setToolTip(form, newPassword);
        
        
        return newPassword;
    }
    
    /**
     * setup and return the single line text field specified in the FormObject
     */
    @Override
    public Object singleLineText(final FormObject form) {
        
        final boolean[] flags = form.getFieldFlags();
        final boolean[] characteristics = form.getCharacteristics();
        
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
        
        final JComponent retComp;
        if (((flags != null) && (flags[FormObject.READONLY_ID]))
                || (characteristics!=null && characteristics[9])//characteristics[9] = LockedContents
                //p609 PDF ref ver 1-7, characteristics 'locked' flag does allow contents to be edited,
                //but the 'LockedContents' flag stops content being changed.
                ){
        	
        	if(form.isXFAObject()){
        		final JTextField newTextfield = new JTextField(form.getTextString());
                
                setupTextFeatures(newTextfield, form);
                setToolTip(form, newTextfield);
                newTextfield.setEditable(false);
                retComp = newTextfield;
            }else{
            	 //set the XObject to our icon code so it can be altered later
//                ReadOnlyTextIcon textIcon = new ReadOnlyTextIcon(form,0,currentPdfFile,AcroRes);
//                textIcon.setAlignment(form.getAlignment());
                
//                if(!textIcon.decipherAppObject(form)){
                    final JTextField newTextfield = new JTextField(form.getTextString());
                    
                    setupTextFeatures(newTextfield, form);
                    setToolTip(form, newTextfield);
                    
                    newTextfield.setEditable(false);
                    
                    retComp = newTextfield;
                    
//                }else{
//                    
//                    //****Now we know we are using our ReadOnlyTextIcon set it up.***
//                    
//                    //build new buttonTextField
//                    JButton textBut = new JButton();
//                    //force NO drawing of the AP image as we are going to do that after initial setup
//                    form.setAppreancesUsed(false);
//                    setupButton(textBut, form);
//                    
//                    //do our swing stuff here
//                    form.setAppreancesUsed(true);
//                    textBut.setText(null);
//                    
//                    //store the text for use in the font size Calculation
//                    String V = form.getTextStreamValue(PdfDictionary.V);
//                    if(V!=null)
//                        textIcon.setText(V);
//                    
//                    //most important apply the icon
//                    textBut.setIcon(textIcon);
//                    
//                    retComp = textBut;
//                	 
//                	 JTextField newTextfield = new JTextField(form.getTextString());
//                     
//                     setupTextFeatures(newTextfield, form);
//                     setToolTip(form, newTextfield);
//                     
//                     newTextfield.setEditable(false);
//                     
//                     retComp = newTextfield;
//                }
            }
            
        }else {//NOT read only
            final JTextField newTextfield = new JTextField(form.getTextString());
            
            setupTextFeatures(newTextfield, form);
            setToolTip(form, newTextfield);
            
            retComp = newTextfield;
            
        }
        
        setupUniversalFeatures(retComp, form);
        
        return retComp;
    }
    
    //############ below is all text setup ################ TAG
    /**
     * sets up all the required attributes for all text fields
     */
    static void setupTextFeatures(final JTextField textcomp, final FormObject form) {
        
        //set text field alignment
        if (form.getAlignment() != -1) {
            textcomp.setHorizontalAlignment(form.getAlignment());
        }
        
        // <start-demo><end-demo>
        
        
        /**
         * ensure we sync back to FormObject if altered
         */
        textcomp.getDocument().addDocumentListener(new TextDocumentListener(textcomp,form));
        
    }
    
    //################# below is buttons setup ################## TAG
    /**
     * sets up the buttons captions, images, etc
     * for normal, rollover, down and off or on if radio or check buttons
     */
    private void setupButton(final AbstractButton comp, final FormObject form) {
        
        /**
         * put in group (just store first time as you cannot have 1 button in group)
         */
        String name=form.getTextStreamValue(PdfDictionary.T);
        if (name == null) {
            name = "";
        }
        
        ButtonGroup bg= (ButtonGroup) groups.get(name);
        if(bg==null){
            bg=new ButtonGroup();
            groups.put(name,bg);
            firstButtons.put(name,comp); //save to avoid buttonGroup if single
        }else{
            
            //add any first comp
            final AbstractButton firstButton= (AbstractButton) firstButtons.get(name);
            
            if(firstButton!=null){
                firstButtons.remove(name);
                bg.add(firstButton);
            }
            
            bg.add(comp);
        }
        
        //transparancy
        //    	((AbstractButton) comp).setContentAreaFilled(false);//false for transparency
        final String normalCaption = form.getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);
        comp.setText(normalCaption);
        
        comp.setContentAreaFilled(false);
        
        final String downCaption = form.getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.AC);
        final String rolloverCaption = form.getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.RC);
        if((downCaption != null && !downCaption.isEmpty()) || (rolloverCaption != null && !rolloverCaption.isEmpty())) {
            comp.addMouseListener(new SwingFormButtonListener(normalCaption, rolloverCaption, downCaption));
        }
        
        if (form.isAppearanceUsed()) {
            setAPImages(form, comp); // pass in true to debug images by showing
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
                    
                    comp.setIcon(null);
                    comp.setText(normalCaption); //seems to need reset
                    break;//0=caption only
                case 1:
                    comp.setText(null);
                    break;//1=icon only
                case 2:
                    comp.setVerticalTextPosition(SwingConstants.BOTTOM);
                    break;//2=caption below icon
                case 3:
                    comp.setVerticalTextPosition(SwingConstants.TOP);
                    break;//3=caption above icon
                case 4:
                    comp.setHorizontalTextPosition(SwingConstants.RIGHT);
                    break;//4=caption on right of icon
                case 5:
                    comp.setHorizontalTextPosition(SwingConstants.LEFT);
                    break;//5=caption on left of icon
                case 6:
                    comp.setText(null);
                    break;
            }
        }
        
        //TODO get margin data from formobject
        final Insets insetZero = new Insets(0, 0, 0, 0);
        comp.setMargin(insetZero);
        
        comp.addMouseListener((MouseListener)formsActionHandler.setHoverCursor());
        
        /**
         * ensure we sync back to FormObject if altered
         */
        comp.addChangeListener(new RadioListener(comp,form));
        
    }
    
    /**
     * gets each appearance image from the map <b>appearance</b> and
     * and adds it to the relevant icon for the AbstractButton <b>comp</b>
     * showImages is to display the appearance images for that FormObject
     */
    private void setAPImages(final FormObject form, final Object rawComp) {
        
        final AbstractButton comp=(AbstractButton)rawComp;
        
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
                comp.setIcon(new FixImageIcon(form,normalOffDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,0));
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
                comp.setSelectedIcon(new FixImageIcon(form,normalOnDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,0));
                
                if(comp.getIcon()==null){
                    comp.setIcon(new FixImageIcon(form,null,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R)));
                }
            }
        }
        
        if (form.hasNoDownIcon()) {
            comp.setPressedIcon(comp.getIcon());
        } else {
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
                    comp.setText(null);
                    comp.setPressedIcon(new FixImageIcon(form,downOffDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,offsetDownImage));
                } else if (downOnDic != null) {
                    comp.setText(null);
                    comp.setPressedIcon(new FixImageIcon(form,downOnDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,offsetDownImage));
                }
            } else {
                comp.setPressedIcon(new FixImageIcon(form,downOnDic,downOffDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),comp.isSelected()?1:0,currentPdfFile,subtype,offsetDownImage));
                comp.addActionListener(new SwingDownIconListener());
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
                comp.setRolloverEnabled(true);
                comp.setText(null);
                comp.setRolloverIcon(new FixImageIcon(form,rollOffDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,0));
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
                comp.setRolloverEnabled(true);
                comp.setText(null);
                comp.setRolloverSelectedIcon(new FixImageIcon(form,rollOnDic,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R),currentPdfFile,subtype,0));
                
                if(comp.getRolloverIcon()==null){
                    comp.setRolloverIcon(new FixImageIcon(form,null,form.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R)));
                }
            }
        }
        
        //moved to end as flagLastUsed can call the imageicon
        if (form.isSelected()) {
            comp.setSelected(true);
            if(comp instanceof JToggleButton){
                final Icon icn = comp.getPressedIcon();
                if(icn instanceof FixImageIcon) {
                    ((FixImageIcon) icn).swapImage(true);
                }
            }
        }
    }
    
    /**
     * sets up the features for all fields, transparancy, font, color, border, actions,
     * background color,
     */
    private void setupUniversalFeatures(final JComponent comp, final FormObject form) {
        
        comp.setOpaque(false);
        
        final Font textFont = form.getTextFont();
        if (textFont != null) {
            comp.setFont(textFont);
        }
        comp.setForeground(form.getTextColor());
        
        final Border newBorder = (Border)SwingData.generateBorderfromForm(form,1);
        
        comp.setBorder(newBorder);
        
        final Color backgroundColor = FormObject.generateColor(form.getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BG));
        if (backgroundColor != null) {
            comp.setBackground(backgroundColor);
            comp.setOpaque(true);
        }else if(DecoderOptions.isRunningOnMac && (comp instanceof JButton)){
            //hack because OS X does not f***king work properly
            ((JButton)comp).setBorderPainted(false);
            comp.setBorder(null);
            
        }
        
        setupMouseListener(comp, form);
        
        // <start-demo><end-demo>
    }
    
    /**
     * setup the events for currentComp, from the specified parameters
     *
     * @Action - mouse events added here
     */
    private void setupMouseListener(final Component currentComp, final FormObject form) {
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
            currentComp.setVisible(false);
        }
        
        final SwingListener jpedalListener = new SwingListener(form, formsActionHandler);
        //if combobox wee need to add the listener to the component at position 0 as well as the normal one, so it works properly.
        if (currentComp instanceof JComboBox) {
            ((JComboBox) currentComp).getComponent(0).addMouseListener(jpedalListener);
            ((JComboBox) currentComp).getComponent(0).addKeyListener(jpedalListener);
            ((JComboBox) currentComp).getComponent(0).addFocusListener(jpedalListener);
            ((JComboBox)currentComp).addActionListener(jpedalListener);
        }
        if(currentComp instanceof JList){
            ((JList)currentComp).addListSelectionListener(jpedalListener);
        }
        
        currentComp.addMouseListener(jpedalListener);
        currentComp.addMouseMotionListener(jpedalListener);
        currentComp.addKeyListener(jpedalListener);
        currentComp.addFocusListener(jpedalListener);
        
        final PdfObject aData=form.getDictionary(PdfDictionary.A);
        if(aData!=null && aData.getNameAsConstant(PdfDictionary.S)==PdfDictionary.URI){
            
            final String noLinkToolTips=System.getProperty("org.jpedal.noURLaccess");
            
            if(noLinkToolTips==null || !noLinkToolTips.equals("true")){
                final String text=aData.getTextStreamValue(PdfDictionary.URI); //+"ZZ"; deliberately broken first to test checking
                ((JComponent) currentComp).setToolTipText(text);
            }
        }
    }
    
    private static void setToolTip(final FormObject formObject, final JComponent retComponent) {
        //TU seems to be used as a tooltip in text fields so added
        final String userName = formObject.getTextStreamValue(PdfDictionary.TU);
        if(userName!=null) {
            retComponent.setToolTipText(userName);
        }
    }
    
    /**
     * new data object to hold all widget implementations
     */
    @Override
    public GUIData getCustomCompData() {
        return new SwingData();
    }
    
    
    @Override
    public int getType() {
        return FormFactory.SWING;
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
        return new PdfSwingPopup(form,cropBoxWith);
    }
    
}
