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
 * GenericFormFactory.java
 * ---------------
 */

package org.jpedal.objects.acroforms.creation;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.jpedal.objects.Javascript;
import org.jpedal.objects.acroforms.actions.ActionHandler;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.PdfPageData;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;

public abstract class GenericFormFactory {
    
    
    public final Map groups=new HashMap();
    public final Map firstButtons=new HashMap();
    
    /**
     * handle on Resources if exists
     */
    public PdfObject AcroRes;
    
    public Object[] CO;
    
    public PdfPageData pageData;
    
    public PdfObjectReader currentPdfFile;
    
    /**
     * handle on AcroRenderer needed for adding mouse listener
     */
    protected ActionHandler formsActionHandler;

    @SuppressWarnings("UnusedDeclaration")
    public void setDVR(final DynamicVectorRenderer htmLoutput, final Javascript javaScript) {
        //only used in HTML
    }
    
    public void reset(final Object[] resources, final ActionHandler actionHandler, final PdfPageData pageData, final PdfObjectReader currentPdfFile) {
        
        this.AcroRes = (PdfObject) resources[0];
        this.CO = (Object[]) resources[1];
        formsActionHandler = actionHandler;
        this.pageData=pageData;
        this.currentPdfFile=currentPdfFile;
        
        groups.clear();
        firstButtons.clear();
        
    }

    protected String readAPimagesForText(final FormObject form) {
        
        
        final PdfObject xobj = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.N);
        if(xobj!=null){
            return FormStream.decipherTextFromAP(currentPdfFile,xobj);
        }
        
        return null;
    }
    
    /**
     * create a pressed look of the <b>image</b> and return it
     */
    @SuppressWarnings("UnusedDeclaration")
    protected BufferedImage createPressedLook(final Image image) {
        
        if(image==null) {
            return null;
        }
        
        final BufferedImage pressedImage = new BufferedImage(image.getWidth(null) + 2, image.getHeight(null) + 2, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = (Graphics2D) pressedImage.getGraphics();
        g.drawImage(image, 1, 1, null);
        g.dispose();
        return pressedImage;
    }
    
    /**
     * does nothing (overriden by HTML implementation)
     */
    @SuppressWarnings("UnusedDeclaration")
    public void indexAllKids(){
        
    }
    
    /**
     * public method to allow user to replace Popup with their own implementation
     * @param form
     * @param popupObj
     * @return Swing component to use as popup (see org.jpedal.objects.acroforms.overridingImplementations.PdfSwingPopup)
     */
    @SuppressWarnings({"UnusedParameters", "MethodMayBeStatic"})
    public Object getPopupComponent(final FormObject form, final PdfObject popupObj, final int cropBoxWith) {
        return null;
    }
    
    
    public void setOptions(final EnumSet formSettings) {
        throw new RuntimeException("setOptions(EnumSet formSettings) called in GenericFormFactory - not implemented in "+this);
    }
    
    public static boolean isTextForm(final int formType){
        return formType==FormFactory.SINGLELINEPASSWORD || formType==FormFactory.MULTILINEPASSWORD || formType==FormFactory.SINGLELINETEXT || formType==FormFactory.MULTILINETEXT;
    }
    
    public static boolean isButtonForm(final int formType){
        return formType==FormFactory.RADIOBUTTON || formType==FormFactory.CHECKBOXBUTTON;
    }
}
