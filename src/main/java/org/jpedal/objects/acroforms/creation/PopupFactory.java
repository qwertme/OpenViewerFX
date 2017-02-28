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
 * PopupFactory.java
 * ---------------
 */
package org.jpedal.objects.acroforms.creation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.jpedal.color.DeviceCMYKColorSpace;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class PopupFactory {

    /**
     * Determine the type of annotation from the sub type value and call appropriate method
     * @param form :: PdfObject containing the annotation
     * @return BufferedImage of the annotation or null if formobject contains errors
     */
    public static BufferedImage getIcon(final PdfObject form){
        BufferedImage commentIcon = null;
        
        switch(form.getParameterConstant(PdfDictionary.Subtype)){
            case PdfDictionary.Text :
                commentIcon = getTextIcon(form);
                break;
            case PdfDictionary.Highlight :
                commentIcon = getHightlightIcon(form);
                break;
        }
        
        return commentIcon;
    }

    private static BufferedImage getHightlightIcon(final PdfObject form){
        final float[] f = form.getFloatArray(PdfDictionary.C);
        Color c = new Color(0);
        if (f != null) {
            switch (f.length) {
                case 0:
                    //Should not happen. Do nothing. Annotation is transparent
                    break;
                case 1:
                    //DeviceGrey colorspace
                    c = new Color(f[0], f[0], f[0], 0.5f);
                    break;
                case 3:
                    //DeviceRGB colorspace
                    c = new Color(f[0], f[1], f[2], 0.5f);
                    break;
                case 4:
                    //DeviceCMYK colorspace
                    final DeviceCMYKColorSpace cmyk = new DeviceCMYKColorSpace();
                    cmyk.setColor(f, 4);
                    c = new Color(cmyk.getColor().getRGB());
                    c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.5f);

                    break;
                default:
                    break;
            }
        }
        
        float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad != null) {
            Rectangle bounds = ((FormObject)form).getBoundingRectangle();
            
            //Bounds is 0 so calculate based on quad areas
            if(bounds.getWidth()==0 && bounds.getHeight()==0){
                for(int i=0; i!=quad.length; i++){
                    if(i%2==0){
                        if(bounds.x>quad[i]){
                            bounds.x = (int)quad[i];
                        }
                        if(bounds.x+bounds.width<quad[i]){
                            bounds.width = (int)(quad[i]-bounds.x);
                        }
                    }else{
                        if(bounds.y>quad[i]){
                            bounds.y = (int)quad[i];
                        }
                        if(bounds.y+bounds.height<quad[i]){
                            bounds.height = (int)(quad[i]-bounds.y);
                        }
                    }
                    
                }
            }
            
            final BufferedImage icon = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics g = icon.getGraphics();
        
            if (quad.length >= 8) {
                for (int hi = 0; hi != quad.length; hi += 8) {
                    final int x = (int) quad[hi] - bounds.x;
                    int y = (int) quad[hi + 5] - bounds.y;
                    //Adjust y for display
                    y = (bounds.height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                    final int width = (int) (quad[hi + 2] - quad[hi]);
                    final int height = (int) (quad[hi + 1] - quad[hi + 5]);
                    final Rectangle rh = new Rectangle(x, y, width, height);
                    g.setColor(c);
                    g.fillRect(rh.x, rh.y, rh.width, rh.height);
                }
            }
            return icon;
        }
        //Return a small empty image as no highlight to make.
        return null;
    }
    
    private static BufferedImage getTextIcon(final PdfObject form){
        
        String name = form.getTextStreamValue(PdfDictionary.Name);
        final String iconFile;
        
        if(name==null) {
            name = "Note";
        }
        
        /* Name of the icon image to use for the icon of this annotation
         * - predefined icons are needed for names:-
         * Comment, Key, Note, Help, NewParagraph, Paragraph, Insert
         */
        if(name.equals("Comment")){
            iconFile = "/org/jpedal/objects/acroforms/res/comment.png";
        }else if(name.equals("Check")){
            iconFile = "/org/jpedal/objects/acroforms/res/Check.png";
        }else if(name.equals("Checkmark")){
            iconFile = "/org/jpedal/objects/acroforms/res/Checkmark.png";
        }else if(name.equals("Circle")){
            iconFile = "/org/jpedal/objects/acroforms/res/Circle.png";
        }else if(name.equals("Cross")){
            iconFile = "/org/jpedal/objects/acroforms/res/Cross.png";
        }else if(name.equals("CrossHairs")){
            iconFile = "/org/jpedal/objects/acroforms/res/CrossHairs.png";
        }else if(name.equals("Help")){
            iconFile = "/org/jpedal/objects/acroforms/res/Help.png";
        }else if(name.equals("Insert")){
            iconFile = "/org/jpedal/objects/acroforms/res/InsertText.png";
        }else if(name.equals("Key")){
            iconFile = "/org/jpedal/objects/acroforms/res/Key.png";
        }else if(name.equals("NewParagraph")){
            iconFile = "/org/jpedal/objects/acroforms/res/NewParagraph.png";
        }else if(name.equals("Paragraph")){
            iconFile = "/org/jpedal/objects/acroforms/res/Paragraph.png";
        }else if(name.equals("RightArrow")){
            iconFile = "/org/jpedal/objects/acroforms/res/RightArrow.png";
        }else if(name.equals("RightPointer")){
            iconFile = "/org/jpedal/objects/acroforms/res/RightPointer.png";
        }else if(name.equals("Star")){
            iconFile = "/org/jpedal/objects/acroforms/res/Star.png";
        }else if(name.equals("UpLeftArrow")){
            iconFile = "/org/jpedal/objects/acroforms/res/Up-LeftArrow.png";
        }else if(name.equals("UpArrow")){
            iconFile = "/org/jpedal/objects/acroforms/res/UpArrow.png";
        }else{ //Default option. Name = Note
            iconFile = "/org/jpedal/objects/acroforms/res/TextNote.png";
        }
        
        BufferedImage commentIcon = null;
        try {
            commentIcon = ImageIO.read(PopupFactory.class.getResource(iconFile));
        } catch (final IOException e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        //Set color of annotation
        float[] col = form.getFloatArray(PdfDictionary.C);
        
        if(col==null){//If not color set we should use white
            col = new float[]{1.0f,1.0f,1.0f};
        }
        
        final Color c = new Color(col[0], col[1], col[2]);
        final int rgb = c.getRGB();

        //Replace default color with specified color
        for(int x=0; x!=commentIcon.getWidth(); x++){
            for(int y=0; y!=commentIcon.getHeight(); y++){
                
               //Checks for yellow (R255,G255,B000) and replaces with color
                if(commentIcon.getRGB(x, y)==-256){
                    commentIcon.setRGB(x, y, rgb);
                }
            }
        }

        return commentIcon;
    }
    
    /**
     * Method to create an icon to represent the annotation and render it.
     * @param form :: PdfObject to hold the annotation data
     * @param current :: DynamicVectorRender to draw the annotation
     * @param pageNumber :: Int value of the page number
     * @param rotation :: Int value of the page rotation
     */
    public static void renderFlattenedAnnotation(final PdfObject form, final DynamicVectorRenderer current, final int pageNumber, final int rotation) {

        final BufferedImage image=PopupFactory.getIcon(form);

        if (image != null) {
            final GraphicsState gs = new GraphicsState();

            /**
             * now draw the finished image of the form
             */
            final int iconHeight = image.getHeight();
            final int iconWidth = image.getWidth();

            final float[] rect = form.getFloatArray(PdfDictionary.Rect);

            //Some Text annotations can have incorrect sizes so correct to icon size
            if (form.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Text) {
                rect[2] = rect[0] + iconWidth;
                rect[1] = rect[3] - iconHeight;
                form.setFloatArray(PdfDictionary.Rect, rect);
            }
        //4 needed as we upsample by a factor of 4
            //Factor out rotation as icon should not be rotated
            switch (rotation % 360) {
                case 0:
                    gs.CTM = new float[][]{{iconWidth, 0, 1}, {0, iconHeight, 1}, {0, 0, 0}};

                    gs.x = rect[0];
                    gs.y = rect[3] - iconHeight;

                    //draw onto image
                    gs.CTM[2][0] = rect[0];
                    gs.CTM[2][1] = rect[3] - iconHeight;
                    break;
                case 90:
                    gs.CTM = new float[][]{{0, iconWidth, 1}, {-iconHeight, 0, 1}, {0, 0, 0}};

                    gs.x = rect[0] + iconHeight;
                    gs.y = rect[3];

                    //draw onto image
                    gs.CTM[2][0] = rect[0] + iconHeight;
                    gs.CTM[2][1] = rect[3];
                    break;
                case 180:
                    gs.CTM = new float[][]{{-iconWidth, 0, 1}, {0, -iconHeight, 1}, {0, 0, 0}};

                    gs.x = rect[0];
                    gs.y = rect[3] + iconHeight;

                    //draw onto image
                    gs.CTM[2][0] = rect[0];
                    gs.CTM[2][1] = rect[3] + iconHeight;
                    break;
                case 270:
                    gs.CTM = new float[][]{{0, -iconWidth, 1}, {iconHeight, 0, 1}, {0, 0, 0}};

                    gs.x = rect[0] - iconHeight;
                    gs.y = rect[3];

                    //draw onto image
                    gs.CTM[2][0] = rect[0] - iconHeight;
                    gs.CTM[2][1] = rect[3];
                    break;
            }

            //Hard code blendMode for highlights to ensure correct output
            if (form.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Highlight) {
                current.setGraphicsState(GraphicsState.STROKE, gs.getAlpha(GraphicsState.STROKE), PdfDictionary.Darken);
                current.setGraphicsState(GraphicsState.FILL, gs.getAlpha(GraphicsState.FILL), PdfDictionary.Darken);
            }

            current.drawImage(pageNumber, image, gs, false, form.getObjectRefAsString(), -1);

            if (form.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Highlight) {
                current.setGraphicsState(GraphicsState.STROKE, gs.getAlpha(GraphicsState.STROKE), PdfDictionary.Normal);
                current.setGraphicsState(GraphicsState.FILL, gs.getAlpha(GraphicsState.FILL), PdfDictionary.Normal);
            }
        }
    }
}
