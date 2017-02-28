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
 * FormRenderUtilsG2.java
 * ---------------
 */
package org.jpedal.objects.acroforms;

import com.idrsolutions.pdf.color.blends.BlendMode;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.StringTokenizer;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecoderOptions;

/**
 * Swing specific implementation of Form Rendering
 *
 */
public class FormRenderUtilsG2 {
    
    private static Color getBorderColor(FormObject formObject){
        
        Color BC = new Color(0, 0, 0, 0);
        if (formObject.getDictionary(PdfDictionary.MK) != null) {
            final PdfObject MK = formObject.getDictionary(PdfDictionary.MK);
            final float[] bc = MK.getFloatArray(PdfDictionary.BC);
            
            BC = FormObject.generateColor(bc);
        }
        
        return BC;
    }
    
    private static Color getBorderBackgroundColor(FormObject formObject){
        Color BG = new Color(0, 0, 0, 0);
        if (formObject.getDictionary(PdfDictionary.MK) != null) {
            final PdfObject MK = formObject.getDictionary(PdfDictionary.MK);
            final float[] bg = MK.getFloatArray(PdfDictionary.BG);
            
            BG = FormObject.generateColor(bg);
        }
        
        return BG;
    }
    
    private static void renderBorderSolid(Graphics2D g2, FormObject formObject, int page, int borderWidth, int pageHeight) {
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawRect(formObject.getBoundingRectangle().x + borderWidth - 1,
                pageHeight - (formObject.getBoundingRectangle().y + formObject.getBoundingRectangle().height) + borderWidth - 1,
                formObject.getBoundingRectangle().width - (borderWidth * 2) + 2,
                formObject.getBoundingRectangle().height - (borderWidth * 2) + 2);
    }

    private static void renderBorderDashed(Graphics2D g2, FormObject formObject, PdfArrayIterator dashPattern, int page, int borderWidth, int pageHeight) {

        float[] dash = {3};
        int phase = 0;
        if (dashPattern.getTokenCount() > 0) {
            final int count = dashPattern.getTokenCount();
            if (count > 0) {
                dash = dashPattern.getNextValueAsFloatArray();
            }

            if (count > 1) {
                phase = dashPattern.getNextValueAsInteger();
            }
        }

        if (dash.length == 0) {
            g2.setStroke(new BasicStroke(borderWidth));
        } else if (dash.length > 0) {
            g2.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, dash, phase));
        }

        g2.drawRect(formObject.getBoundingRectangle().x + borderWidth - 1,
                pageHeight - (formObject.getBoundingRectangle().y + formObject.getBoundingRectangle().height) + borderWidth - 1,
                formObject.getBoundingRectangle().width - (borderWidth * 2) + 2,
                formObject.getBoundingRectangle().height - (borderWidth * 2) + 2);
    }

    private static void renderBorderBeveled(Graphics2D g2, FormObject formObject, Color BG, int page, int borderWidth, int pageHeight) {

        final Color bckUp = g2.getColor();

        int x = formObject.getBoundingRectangle().x;
        int y = pageHeight - (formObject.getBoundingRectangle().y + formObject.getBoundingRectangle().height);
        int width = formObject.getBoundingRectangle().width;
        int height = formObject.getBoundingRectangle().height;

        //Outer Line
        g2.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f));
        g2.drawRect(x,
                y,
                width,
                height);

        //Inner Line
        g2.setStroke(new BasicStroke(1));

        x += 1;
        y += 1;
        width -= 2;
        height -= 2;

        //DARKER
        g2.setColor(BG.darker());
        g2.fillPolygon(
                new int[]{x + width, x + width, x + width - borderWidth, x + width - borderWidth, x + borderWidth, x},
                new int[]{y + height, y, y + borderWidth, y + height - borderWidth, y + height - borderWidth, y + height},
                6);

        //LIGHTER
        g2.setColor(BG.brighter());
        g2.fillPolygon(
                new int[]{x, x, x + borderWidth, x + borderWidth, x + width - borderWidth, x + width},
                new int[]{y, y + height, y + height - borderWidth, y + borderWidth, y + borderWidth, y},
                6);

        g2.setColor(bckUp);
    }

    private static void renderBorderInset(Graphics2D g2, FormObject formObject, int page, int borderWidth, int pageHeight) {

        final Color bckUp = g2.getColor();

        int x = formObject.getBoundingRectangle().x;
        int y = pageHeight - (formObject.getBoundingRectangle().y + formObject.getBoundingRectangle().height);
        int width = formObject.getBoundingRectangle().width;
        int height = formObject.getBoundingRectangle().height;

        //Outer Line
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawRect(x,
                y,
                width,
                height);

        //Inner Line
        g2.setStroke(new BasicStroke(1));

        x += 1;
        y += 1;
        width -= 2;
        height -= 2;
        //LIGHTER
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillPolygon(
                new int[]{x + width, x + width, x + width - borderWidth, x + width - borderWidth, x + borderWidth, x},
                new int[]{y + height, y, y + borderWidth, y + height - borderWidth, y + height - borderWidth, y + height},
                6);

        //DARKER
        g2.setColor(Color.GRAY);
        g2.fillPolygon(
                new int[]{x, x, x + borderWidth, x + borderWidth, x + width - borderWidth, x + width},
                new int[]{y, y + height, y + height - borderWidth, y + borderWidth, y + borderWidth, y},
                6);

        g2.setColor(bckUp);
    }

    private static void renderBorderUnderline(Graphics2D g2, FormObject formObject, int page, int borderWidth, int pageHeight) {
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawLine(formObject.getBoundingRectangle().x + borderWidth - 1,
                pageHeight - (formObject.getBoundingRectangle().y) + borderWidth - 1,
                formObject.getBoundingRectangle().x + formObject.getBoundingRectangle().width - 1,
                pageHeight - (formObject.getBoundingRectangle().y) + borderWidth - 1);

    }

    public static int renderBorder(Graphics2D g2, FormObject formObject, int page, int pageHeight){
        
        //Turn off antialiasing for border and background
        final Object antiA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        //Variables needed for Border
        int borderWidth = 0;
        Color BC = getBorderColor(formObject);
        Color BG = getBorderBackgroundColor(formObject);
        
        boolean borderCreated = false;
        //Handle Border BS dictionary
        if (formObject.getDictionary(PdfDictionary.BS) != null) {

            final PdfObject BS = formObject.getDictionary(PdfDictionary.BS);
            final String s = BS.getName(PdfDictionary.S);
            borderWidth = BS.getInt(PdfDictionary.W);

            if (borderWidth > 0) {//Ignore border is width is 0 or less

                //Set to border color
                g2.setColor(BC);

                if (s == null || s.equals("S")) {
                    renderBorderSolid(g2, formObject, borderWidth, page, pageHeight);
                    borderCreated = true;
                } else if (s.equals("D")) {
                    final PdfArrayIterator dashPattern = BS.getMixedArray(PdfDictionary.D);
                    renderBorderDashed(g2, formObject, dashPattern, borderWidth, page, pageHeight);
                    borderCreated = true;
                } else if (s.equals("B")) {
                    renderBorderBeveled(g2, formObject, BG, borderWidth, page, pageHeight);
                    borderCreated = true;
                } else if (s.equals("I")) {
                    renderBorderInset(g2, formObject, borderWidth, page, pageHeight);
                    borderCreated = true;
                } else if (s.equals("U")) {
                    renderBorderUnderline(g2, formObject, borderWidth, page, pageHeight);
                    borderCreated = true;
                }

                //Reset to old color
                //g2.setColor(old);
            }
        } else if (formObject.getObjectArray(PdfDictionary.Border) != null) {
            //borderCreated = true;
            throw new RuntimeException("Border Array not implemented yet");

        }

        //No border created so use the defaults
        if (!borderCreated) {
            //Set to border color
            g2.setColor(BC);
            borderWidth = 1;
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRect(formObject.getBoundingRectangle().x + borderWidth - 1,
                    pageHeight - (formObject.getBoundingRectangle().y + formObject.getBoundingRectangle().height) + borderWidth - 1,
                    formObject.getBoundingRectangle().width - (borderWidth * 2) + 2,
                    formObject.getBoundingRectangle().height - (borderWidth * 2) + 2);
            
        }

        //Reset Antialiasing for the text
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiA);
        
        return borderWidth;
    }
    
    public static FontMetrics renderFont(Graphics2D g2, FormObject formObject, String textValue, int borderWidth) {
        
        //If font size is 0, resize to fit text within area.
        if (formObject.getTextSize() <= 0) {
            if (!formObject.getFieldFlags()[FormObject.MULTILINE_ID]) {

                //Create font for string shorter than form
                final float fh = (formObject.getBoundingRectangle().height - (borderWidth * 2)) - 2;
                g2.setFont(formObject.getTextFont().deriveFont(fh));

                //Create metrics and bounds for text using current font
                FontMetrics metrics = g2.getFontMetrics(formObject.getTextFont().deriveFont(fh));
                Rectangle2D r = metrics.getStringBounds(textValue, g2);

                //Find scaling based on string and form width
                float scale = (float) (((formObject.getBoundingRectangle().width - (borderWidth * 2)) - 4) / r.getWidth());
                final float hScale = (float) (formObject.getBoundingRectangle().height / r.getHeight());
                if (scale > 1.0f && scale > hScale) {
                    scale = hScale;
                }

                //Apply scaling to make font needed if scaling down and reset font metrics
                if (scale < 1.0f) {
                    g2.setFont(formObject.getTextFont().deriveFont(fh * scale));
                }

            } else {
                //Sets a default for mutliLine forms as using full form size is incorrect.
                g2.setFont(formObject.getTextFont().deriveFont(12.0f));
                
            }
        } else {
            g2.setFont(formObject.getTextFont());
        }

        if (formObject.getTextColor() != null) {
            g2.setColor(formObject.getTextColor());
        } else {
            g2.setColor(Color.BLACK);
        }
        
        return g2.getFontMetrics();
    }
    
    public static void renderComboForms(Graphics2D g2, FormObject formObject, FontMetrics metrics, Rectangle2D r, int page, int borderWidth, int justification, int pageHeight) {
        final String[] values = formObject.getItemsList();
        if (values != null) {
            final int[] selected = formObject.getIntArray(PdfDictionary.I);

            int startingIndex = formObject.getInt(PdfDictionary.TI);
            if (startingIndex < 0) {
                startingIndex = 0;
            }

            //Add highlighting to form if any
            if (selected != null) {
                final Color c = g2.getColor();

                final Color highlight = new Color(DecoderOptions.highlightColor.getRed() / 255, DecoderOptions.highlightColor.getGreen() / 255, DecoderOptions.highlightColor.getBlue() / 255, DecoderOptions.highlightComposite);

                g2.setColor(highlight);
                
                if (formObject.getBoundingRectangle().getHeight() < (metrics.getHeight() * values.length)) {
                    startingIndex = selected[0];
                }

                for (int i = 0; i != selected.length; i++) {
                    final int x = formObject.getBoundingRectangle().x + (borderWidth);
                    int y = (pageHeight - (formObject.getBoundingRectangle().y + (formObject.getBoundingRectangle().height))) - (borderWidth);

                						//Text is drawn a font baseline and not the font descent
                    //Add descent to the coords for the highlight to position correctly.
                    y += metrics.getDescent();
                    y += (metrics.getHeight() * (selected[i] - startingIndex));
                    g2.fillRect(x, y + borderWidth,
                            formObject.getBoundingRectangle().width - (borderWidth * 2) + 2,
                            metrics.getHeight());
                }
                g2.setColor(c);
            }
            g2.setClip(new Rectangle(formObject.getBoundingRectangle().x + borderWidth - 1,
                    pageHeight - (formObject.getBoundingRectangle().y + formObject.getBoundingRectangle().height) + borderWidth - 1,
                    formObject.getBoundingRectangle().width - (borderWidth * 2) + 2,
                    formObject.getBoundingRectangle().height - (borderWidth * 2) + 2));

            for (int i = startingIndex; i != values.length; i++) {
                final int x = formObject.getBoundingRectangle().x + (borderWidth) + 2;
                int y = ((pageHeight - (formObject.getBoundingRectangle().y + (formObject.getBoundingRectangle().height))) + (borderWidth) + (metrics.getHeight())) - borderWidth;
                y += (metrics.getHeight() * (i - startingIndex));
                renderTextString(g2, formObject, values[i], r, x, y, borderWidth, justification);
            }
        }
    }
    
    public static void renderMultilineTextField(Graphics2D g2, FormObject formObject, FontMetrics metrics, Rectangle2D r, String textValue, int page, int borderWidth, int justification, int pageHeight) {

        final int x = formObject.getBoundingRectangle().x + (borderWidth);
        int y = (pageHeight - (formObject.getBoundingRectangle().y + (formObject.getBoundingRectangle().height))) - (borderWidth) - borderWidth;
        final StringTokenizer tokenizer = new StringTokenizer(textValue, "\n");

        while (tokenizer.hasMoreTokens()) {
            y += metrics.getHeight();
            renderTextString(g2, formObject, tokenizer.nextToken(), r, x, y, borderWidth, justification);
        }
    }
    
    public static void renderSingleLineTextField(Graphics2D g2, FormObject formObject, FontMetrics metrics, Rectangle2D r, String textValue, int page, int borderWidth, int justification, int pageHeight){

        final int x = formObject.getBoundingRectangle().x + (borderWidth);
        final int y = (pageHeight - (formObject.getBoundingRectangle().y)) - (formObject.getBoundingRectangle().height - metrics.getHeight());
        
        renderTextString(g2, formObject, textValue, r, x, y, borderWidth, justification);
    }
    
    private static void renderTextString(Graphics2D g2, FormObject formObject, String textValue, Rectangle2D r, int x, int y, int borderWidth, int justification){
        switch (justification) {
            case 0: //JTextField.CENTER
                g2.drawString(textValue, (int) (x + ((formObject.getBoundingRectangle().width - (borderWidth * 2) - r.getWidth()) / 2)), y);
                break;
            case 4: //JTextField.RIGHT
                g2.drawString(textValue, (int) (x + formObject.getBoundingRectangle().width - (borderWidth * 2) - r.getWidth()) - 2, y);
                break;
            default:
                g2.drawString(textValue, x + 2, y);
                break;
        }
    }
    
    public static void renderQuadPoint(Graphics2D g2, FormObject formObject, Color bgColor, int page, int pageHeight){
        final float[] quadPoints = formObject.getFloatArray(PdfDictionary.QuadPoints);
        if (quadPoints != null) {
            final Color c = g2.getColor();
            final Composite com = g2.getComposite();
            //Loop through values as have found cases where coords are stored in incorrect order.
            for (int i = 0; i != quadPoints.length / 8; i++) {
                float minX = 0;
                float minY = 0;
                float maxX = 0;
                float maxY = 0;
                for (int j = 0; j != 8; j++) {
                    if (j < 2) {
                        if (j % 2 == 0) { //x coord
                            minX = quadPoints[(i * 8) + j];
                            maxX = quadPoints[(i * 8) + j];
                        } else { //y coord
                            minY = quadPoints[(i * 8) + j];
                            maxY = quadPoints[(i * 8) + j];
                        }
                    } else {
                        if (j % 2 == 0) { //x coord
                            if (quadPoints[(i * 8) + j] < minX) {
                                minX = quadPoints[(i * 8) + j];
                            }
                            if (quadPoints[(i * 8) + j] > maxX) {
                                maxX = quadPoints[(i * 8) + j];
                            }
                        } else { //y coord
                            if (quadPoints[(i * 8) + j] < minY) {
                                minY = quadPoints[(i * 8) + j];
                            }
                            if (quadPoints[(i * 8) + j] > maxY) {
                                maxY = quadPoints[(i * 8) + j];
                            }
                        }
                    }
                }

                if (formObject.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Highlight) {
                    g2.setColor(bgColor);
                    g2.setComposite(new BlendMode(PdfDictionary.Multiply, 0.5f));
                    g2.fillRect((int) minX, (int) (pageHeight - maxY), (int) (maxX - minX), (int) (maxY - minY));
                } else { //Draw for link

                }

                g2.setColor(c);
                g2.setComposite(com);

            }

        }
    }
    
    public static void renderPopupWindow(Graphics2D g2, FormObject formObject, Color bgColor, int page, boolean isPrinting, int pageHeight) {

        //read in date for title bar
        final String mStream = formObject.getParentPdfObj().getTextStreamValue(PdfDictionary.M);
        StringBuffer date = null;
        if (mStream != null) {
            date = new StringBuffer(mStream);
            date.delete(0, 2);//delete D:
            date.insert(10, ':');
            date.insert(13, ':');
            date.insert(16, ' ');

            final String year = date.substring(0, 4);
            final String day = date.substring(6, 8);
            date.delete(6, 8);
            date.delete(0, 4);
            date.insert(0, day);
            date.insert(4, year);
            date.insert(2, '/');
            date.insert(5, '/');
            date.insert(10, ' ');

            //date.delete(19, date.length());//delete the +01'00' Time zone definition
        }

        //setup title text for popup
        final String subject = formObject.getParentPdfObj().getTextStreamValue(PdfDictionary.Subj);
        String popupTitle = formObject.getParentPdfObj().getTextStreamValue(PdfDictionary.T);
        if (popupTitle == null) {
            popupTitle = "";
        }

        String title = "";
        if (subject != null) {
            title += subject + '\t';
        }
        if (date != null) {
            title += date;
        }
        title += '\n' + popupTitle;

        String contents = formObject.getParentPdfObj().getTextStreamValue(PdfDictionary.Contents);
        if (contents == null) {
            contents = "";
        }
        if (contents.indexOf('\r') != -1) {
            contents = contents.replaceAll("\r", "\n");
        }

        final float[] rect = formObject.getFloatArray(PdfDictionary.Rect);

        final int titleBarHeight = 24;

        if (isPrinting) {
            g2.translate(0, titleBarHeight);
        }

//      //Add Border, useful for debugging
//      g2.setColor(Color.BLACK);
//      g2.drawRect((int)(rect[0]), (int)((pageData.getCropBoxHeight(page)-(int)rect[3])), (int)((rect[2]-rect[0])), (int)((rect[3]-rect[1])));
        g2.setColor(Color.WHITE);
        g2.fillRect((int) (rect[0]), (pageHeight - (int) rect[3]), (int) ((rect[2] - rect[0])), (int) ((rect[3] - rect[1])));

        g2.setColor(bgColor);
        g2.fillRect((int) (rect[0]), (pageHeight - (int) rect[3]), (int) ((rect[2] - rect[0])), titleBarHeight + 2);

        g2.setColor(Color.BLACK);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));

        //Title Font size set
        g2.setFont(g2.getFont().deriveFont(8.0f));

        if (title.indexOf('\n') == -1) {
            g2.drawString(title, (int) (rect[0]) + 2, (int) (((pageHeight - (int) rect[3])) + g2.getFont().getSize2D()) + 2);
        } else {
            final StringTokenizer tokenizer = new StringTokenizer(title, "\n", true);
            int lineCount = 1;
            while (tokenizer.hasMoreTokens()) {

                final String t = tokenizer.nextToken();

                if (!t.equals("\n")) {
                    g2.drawString(t, (int) (rect[0]) + 2, (int) (((pageHeight - (int) rect[3])) + ((g2.getFont().getSize2D() + 2) * lineCount)));
                } else {
                    lineCount++;
                }
            }
        }

        //Content Font size set
        g2.setFont(g2.getFont().deriveFont(7.0f));

        if (contents.indexOf('\n') == -1) {
            g2.drawString(contents, (int) (rect[0]), (int) (((pageHeight - (int) rect[3])) + g2.getFont().getSize2D() + titleBarHeight));
        } else {
            final StringTokenizer tokenizer = new StringTokenizer(contents, "\n", true);
            int lineCount = 1;

            while (tokenizer.hasMoreTokens()) {

                final String t = tokenizer.nextToken();

                if (!t.equals("\n")) {
                    g2.drawString(t, (int) (rect[0]) + 2, (int) (((pageHeight - (int) rect[3])) + ((g2.getFont().getSize2D() + 3) * lineCount) + titleBarHeight));
                } else {
                    lineCount++;
                }
            }
        }
    }
}
