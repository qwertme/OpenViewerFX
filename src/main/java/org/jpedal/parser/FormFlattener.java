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
 * FormFlattener.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.TextState;
import org.jpedal.objects.acroforms.creation.PopupFactory;
import org.jpedal.objects.acroforms.overridingImplementations.ReadOnlyTextIcon;
import org.jpedal.objects.raw.*;
import org.jpedal.parser.image.MaskUtils;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.jpedal.render.BaseDisplay;
import org.jpedal.render.DynamicVectorRenderer;

public class FormFlattener {
    /**
     * routine to decode an XForm stream
     * @param pdfStreamDecoder
     * @param form
     * @param isHTML
     * @param AcroRes
     * @throws org.jpedal.exception.PdfException
     */
    public void drawFlattenedForm(final PdfStreamDecoder pdfStreamDecoder, final PdfObject form, final boolean isHTML, final PdfObject AcroRes) throws PdfException {

        //Check org.jpedal.removeForms and stop rendering if form should be removed
        if(exclusionOption!=FormExclusion.ExcludeNone && !showForm(form)){
            return;
        }
        
        /**
         * ignore if not going to be drawn
         */
        //int type=form.getParameterConstant(PdfDictionary.Subtype);
        //if(type==PdfDictionary.Link)
        //    return;
        final int type = form.getParameterConstant(PdfDictionary.Subtype);
        if (type == PdfDictionary.Highlight) {
            PopupFactory.renderFlattenedAnnotation(form, pdfStreamDecoder.current, pdfStreamDecoder.parserOptions.getPageNumber(), pdfStreamDecoder.pageData.getRotation(pdfStreamDecoder.parserOptions.getPageNumber()));
            return;
        }

        //save and use new version so we can ignore changes
        final GraphicsState oldGS = pdfStreamDecoder.gs;
        pdfStreamDecoder.gs =new GraphicsState();

        pdfStreamDecoder.parserOptions.setFlattenedForm(true);

        //check if this form should be displayed
        final boolean[] characteristic = ((FormObject)form).getCharacteristics();
        if (characteristic[0] || characteristic[1] || characteristic[5] ||
                (!form.getBoolean(PdfDictionary.Open) &&
                form.getParameterConstant(PdfDictionary.Subtype)==PdfDictionary.Popup)){
            //this form should be hidden
            return;
        }

        PdfObject imgObj = null;

        final PdfObject APobjN = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.N);
        Map otherValues=new HashMap();
        if(APobjN!=null){
            otherValues=APobjN.getOtherDictionaries();
        }

        final String defaultState = form.getName(PdfDictionary.AS);
        if (defaultState != null && defaultState.equals(((FormObject)form).getNormalOnState())) {
            //use the selected appearance stream
            if(APobjN.getDictionary(PdfDictionary.On) !=null){
                imgObj = APobjN.getDictionary(PdfDictionary.On);
            }else if(APobjN.getDictionary(PdfDictionary.Off) !=null && defaultState != null && defaultState.equals("Off")){
                imgObj = APobjN.getDictionary(PdfDictionary.Off);
            }else if(otherValues!=null && defaultState != null){

                imgObj=(PdfObject)otherValues.get(defaultState);
            }else {
                if(otherValues!=null && !otherValues.isEmpty()){
                    /**final Iterator keys=otherValues.keySet().iterator();
                    final PdfObject val;
                    final String key;
                    //while(keys.hasNext()){
                    key=(String)keys.next();
                    val=(PdfObject)otherValues.get(key);
                    //System.out.println("key="+key+" "+val.getName(PdfDictionary.AS));
                    imgObj = val;
                    /**/
                    imgObj=(PdfObject) otherValues.entrySet().iterator().next();
                    //}
                }
            }
        }else {
            //use the normal appearance Stream
            if(APobjN!=null || form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I) !=null){

                //if we have a root stream then it is the off value
                //check in order of N Off, MK I, then N
                //as N Off overrides others and MK I is in preference to N
                if(APobjN!=null && APobjN.getDictionary(PdfDictionary.Off) !=null){
                    imgObj = APobjN.getDictionary(PdfDictionary.Off);

                }else if(form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I) !=null
                        && form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.IF)==null){
                    //look here for MK IF
                    //if we have an IF inside the MK then use the MK I as some files shown that this value is there
                    //only when the MK I value is not as important as the AP N.
                    imgObj = form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I);

                }else if(APobjN!=null && APobjN.getDecodedStream()!=null){
                    imgObj = APobjN;
                }
            }
        }

        /**
         * we have some examples where no text inside AP datastream so we ignore in this case
         * and use the text
         */
        if(imgObj!=null){

            final byte[] formData=imgObj.getDecodedStream(); //get from the AP

            if(formData!=null){
                final String str=new String(formData);

                if(str.contains("BMC")&& !str.contains("BT")) {
                    imgObj = null;
                }
            }
        }

        /**
         * alternative to draw image for icon
         */
        final byte[] DA=form.getTextStreamValueAsByte(PdfDictionary.DA);

        /**
         * if no object present try to create a fake one using Swing code
         * for readonly text icons
         */
        if(imgObj==null){

            String V = form.getTextStreamValue(PdfDictionary.V);

            //if(V==null)
            //    V=form.getTextStreamValue(PdfDictionary.T);

            if (DA != null || (V != null && !V.isEmpty())) {

                final ReadOnlyTextIcon textIcon = new ReadOnlyTextIcon(form,0, pdfStreamDecoder.currentPdfFile, AcroRes);
                textIcon.decipherAppObject((FormObject) form);
                if (V != null) {
                    textIcon.setText(V);

                    imgObj = textIcon.getFakeObject();
                } else if (DA != null) {

                    imgObj = textIcon.getFakeObject();
                    imgObj.setDecodedStream(DA);
                }
            }

            if(imgObj==null && DA==null){

                //if(form.getParameterConstant(PdfDictionary.Subtype)!=PdfDictionary.Link)
                //    System.out.println("missing image "+PdfDictionary.showAsConstant(form.getParameterConstant(PdfDictionary.Subtype))+" "+form.getObjectRefAsString());

                //add in Popup Icon for text Annotation
//                int type = form.getParameterConstant(PdfDictionary.Subtype);
                if (type == PdfDictionary.Text) {
                    PopupFactory.renderFlattenedAnnotation(form, pdfStreamDecoder.current, pdfStreamDecoder.parserOptions.getPageNumber(), pdfStreamDecoder.pageData.getRotation(pdfStreamDecoder.parserOptions.getPageNumber()));
                }

                return;
            }
        }

        if(imgObj!=null) {
            pdfStreamDecoder.currentPdfFile.checkResolved(imgObj);
        }

        byte[] formData=null; //get from the Fake obj
        if(imgObj!=null) {
            formData = imgObj.getDecodedStream(); //get from the DA
        }

        //debug code for mark, for the flattern case 10295
//        System.out.println("ref="+form.getObjectRefAsString()+" stream="+new String(formData));

        //might be needed to pick up fonts
        if(imgObj!=null){
            final PdfObject resources = imgObj.getDictionary(PdfDictionary.Resources);
            pdfStreamDecoder.readResources(resources, false);
        }

        /**
         * see if bounding box and set
         */
        float[] BBox=form.getFloatArray(PdfDictionary.Rect);

        if(imgObj!=null && imgObj.getObjectType()==PdfDictionary.XFA_APPEARANCE){
            final Rectangle rect=((FormObject)form).getBoundingRectangle();
            if(rect!=null){
                BBox=new float[]{rect.x,rect.y,rect.width,rect.height};
            }
        }

        if(BBox==null){
            BBox=new float[]{0,0,1,1};
        }

        //we need to factor in this to calculations
        final int pageRotation= pdfStreamDecoder.pageData.getRotation(pdfStreamDecoder.parserOptions.getPageNumber());
        
        if (pageRotation == 0) {
            if (BBox[1] > BBox[3]) {
                float t = BBox[1];
                BBox[1] = BBox[3];
                BBox[3] = t;
            }

            if (BBox[0] > BBox[2]) {
                float t = BBox[0];
                BBox[0] = BBox[2];
                BBox[2] = t;
            }
        }
                
        //if we flatten form objects with XForms, we need to use diff calculation
        if(pdfStreamDecoder.parserOptions.isFlattenedForm()){
            pdfStreamDecoder.parserOptions.setOffsets(BBox[0], BBox[1]);
        }

        //please dont delete through merge this fixes most of the flatten form positionsing.
        float[] matrix= {1,0,0,1,0,0};

        if(imgObj!=null) {
            matrix = imgObj.getFloatArray(PdfDictionary.Matrix);
        }
        
        float x = BBox[0],y = BBox[1];
        Area newClip=null;

        //check for null and then recalculate insets
        if (matrix != null) {

            float yScale = 1;

            if (imgObj!=null && pageRotation == 0 && matrix[4]>0 && matrix[5]>0) {

                final float[] BoundingBox = imgObj.getFloatArray(PdfDictionary.BBox);
                if (BoundingBox[1] > BoundingBox[3]) {
                    float t = BoundingBox[1];
                    BoundingBox[1] = BoundingBox[3];
                    BoundingBox[3] = t;
                }

                if (BoundingBox[0] > BoundingBox[2]) {
                    float t = BoundingBox[0];
                    BoundingBox[0] = BoundingBox[2];
                    BoundingBox[2] = t;
                }
                
                matrix[0] = (BBox[2] - BBox[0]) / (BoundingBox[2] - BoundingBox[0]);
                matrix[1] = 0;
                matrix[2] = 0;
                matrix[3] = (BBox[3] - BBox[1]) / (BoundingBox[3] - BoundingBox[1]);
                matrix[4] = (BBox[0] - BoundingBox[0]);
                matrix[5] = (BBox[1] - BoundingBox[1]);
                
                pdfStreamDecoder.gs.CTM = new float[][]{{matrix[0],matrix[1],0},{matrix[2],matrix[3],0},{matrix[4],matrix[5],1}};
                newClip=new Area(new Rectangle((int)BBox[0],(int)BBox[1],(int)((BBox[2]-BBox[0])+2),(int)((BBox[3]-BBox[1])+2)));                   
                
                //Set variables for draw form call
                x = (matrix[4]);
                y = (matrix[5]);
            } else {

                //Check for appearnce stream
                PdfObject temp = form.getDictionary(PdfDictionary.AP);
                if (temp != null) {

                    //Check for N object
                    temp = temp.getDictionary(PdfDictionary.N);
                    if (temp != null) {

                        //Check for a bounding box of this object
                        final float[] BoundingBox = temp.getFloatArray(PdfDictionary.BBox);
                        if (BoundingBox != null) {

                            //If different from BB provided and matrix is standard than add scaling
                            if (BBox[0] != BoundingBox[0] && BBox[1] != BoundingBox[1]
                                    && BBox[2] != BoundingBox[2] && BBox[3] != BoundingBox[3]) {

                                if (//Check matrix is standard
                                        matrix[0] * matrix[3] == 1.0f
                                        && matrix[1] * matrix[2] == 0.0f) {

                                    //float bbw = BBox[2]-BBox[0];
                                    final float bbh = BBox[3] - BBox[1];
                                    //float imw = BoundingBox[2]-BoundingBox[0];
                                    final float imh = BoundingBox[3] - BoundingBox[1];

                                    //Adjust scale on the y to fit form size
                                    if ((int) bbh != (int) imh) {
                                        yScale = bbh / imh;
                                    }
                                } else {
                                    //-90 rotation
                                    if (matrix[0] * matrix[3] == 0.0f
                                            && matrix[1] * matrix[2] == -1.0f) {
                                    //float bbw = BBox[2]-BBox[0];

                                        //Handle 0 rot case here
                                        float bbh = BBox[2] - BBox[0];
                                        switch (pageRotation) {

                                            case 90:
                                                bbh = BBox[2] - BBox[0];
                                                break;

                                            case 180:
                                                break;

                                            case 270:
                                                bbh = BBox[2] - BBox[0];
                                                break;
                                        }
                                        //float imw = BoundingBox[2]-BoundingBox[0];
                                        final float imh = BoundingBox[3] - BoundingBox[1];

                                        //Adjust scale on the y to fit form size
                                        if ((int) bbh != (int) imh) {
                                            yScale = bbh / imh;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                switch (pageRotation) {
                    case 90:

                        //Allow for rotated forms, form requires lowest value
                        if (BBox[0] < BBox[2]) {
                            x = BBox[0] + (matrix[4] * yScale);
                        } else {
                            x = BBox[2] + (matrix[4] * yScale);
                        }

                        //Added code to fix ny1981.pdf and 133419-without-annotations-p2.pdf
                        if (matrix[4] < 0) {
                            x = BBox[0] + (matrix[4] * yScale);
                        }
                        //newClip=new Area(new Rectangle((int)BBox[2],(int)BBox[1],(int)BBox[0],(int)BBox[3]));
                        break;
                    default:
                        x = BBox[0] + (matrix[4] * yScale);
                        newClip = new Area(new Rectangle((int) (BBox[0]-1), (int) (BBox[1]-1), (int) ((BBox[2] - BBox[0])+2), (int) ((BBox[3] - BBox[1])+2)));
                        break;
                }

                y = BBox[1] + (matrix[5] * yScale);

                //set gs.CTM to form coords (probably {1,0,0}{0,1,0}{x,y,1} at a guess
                pdfStreamDecoder.gs.CTM = new float[][]{{matrix[0] * yScale, matrix[1] * yScale, 0}, {matrix[2] * yScale, matrix[3] * yScale, 0}, {x, y, 1}};
            }
        }else{
            pdfStreamDecoder.gs.CTM = new float[][]{{1,0,0},{0,1,0},{x,y,1}};
            newClip=new Area(new Rectangle((int)BBox[0],(int)BBox[1],(int)BBox[2],(int)BBox[3]));
        }
        
        //Convert clip here
        newClip = BaseDisplay.convertPDFClipToJavaClip(newClip);
        
        drawForm(imgObj, form, pdfStreamDecoder, newClip, isHTML, BBox, x, y, formData, APobjN, oldGS);
      
    }
    
    void drawForm(PdfObject imgObj, final PdfObject form, final PdfStreamDecoder pdfStreamDecoder, Area newClip, final boolean isHTML, float[] BBox, float x, float y, byte[] formData, final PdfObject APobjN, final GraphicsState oldGS) throws PdfException {
        
        //set clip to match bounds on form
        if(newClip!=null) {
            pdfStreamDecoder.gs.updateClip(new Area(newClip));
        }
        pdfStreamDecoder.current.drawClip(pdfStreamDecoder.gs, pdfStreamDecoder.parserOptions.defaultClip, false) ;
        /**
         * avoid values in main stream
         */
        final TextState oldState= pdfStreamDecoder.gs.getTextState();
        pdfStreamDecoder.gs.setTextState(new TextState());
        /**
         * write out forms as images in HTML mode - hooks into flatten forms mode
         */
        if(isHTML){

            //create the image from the form data
            final int w=(int)(BBox[2]-BBox[0]);
            final int h=(int) (BBox[3]-BBox[1]);

            if(w>0 && h>0){
                final BufferedImage image= MaskUtils.createTransparentForm(imgObj, 0, 0, w, h, pdfStreamDecoder.currentPdfFile, pdfStreamDecoder.parserOptions, pdfStreamDecoder.formLevel, pdfStreamDecoder.multiplyer);

                //draw the image to HTML

                //4 needed as we upsample by a factor of 4
                pdfStreamDecoder.gs.CTM=new float[][]{{image.getWidth()/4,0,1},{0,image.getHeight()/4,1},{0,0,0}};

                pdfStreamDecoder.gs.x=x;
                pdfStreamDecoder.gs.y=y;

                //draw onto image
                pdfStreamDecoder.gs.CTM[2][0]= x;
                pdfStreamDecoder.gs.CTM[2][1]= y;

                //-3 tells it to render to background image and thumbnail if present
                pdfStreamDecoder.current.drawImage(pdfStreamDecoder.parserOptions.getPageNumber(), image, pdfStreamDecoder.gs, false, form.getObjectRefAsString(), -3);
            
                //add to SVG as external image if needed
                if(pdfStreamDecoder.current.getBooleanValue(DynamicVectorRenderer.IsSVGMode)){
                    pdfStreamDecoder.current.drawImage(pdfStreamDecoder.parserOptions.getPageNumber(), image, pdfStreamDecoder.gs, false, form.getObjectRefAsString(), -2);            
                }
            
            }

        }else{

            /**decode the stream*/
            if(formData!=null){
                pdfStreamDecoder.BBox=BBox;

                //we can potentially have a local Resource object we need to read for GS and font values (see 18343)
                if(APobjN!=null){
                    final PdfObject res=(PdfObject) APobjN.getOtherDictionaries().get("Resources");
                    if(res!=null){
                        pdfStreamDecoder.currentPdfFile.checkResolved(res);
                        pdfStreamDecoder.readResources(res, false);

                    }
                }

                pdfStreamDecoder.decodeStreamIntoObjects(formData, false);
                pdfStreamDecoder.BBox=null;
            }

        }
        /**
         * we need to reset clip otherwise items drawn afterwards
         * like forms data in image or print will not appear.
         */
        pdfStreamDecoder.gs.updateClip(null);
        pdfStreamDecoder.current.drawClip(pdfStreamDecoder.gs, null, true) ;
        //restore
        pdfStreamDecoder.gs =oldGS;
        pdfStreamDecoder.gs.setTextState(oldState);
        
    }
    
    private enum FormExclusion {
        ExcludeNone, ExcludeForms, ExcludeAnnotations, ExcludeFormsAndAnnotations
    }
    
    private static FormExclusion exclusionOption = FormExclusion.ExcludeNone;
    
    static {
        String value = System.getProperty("org.jpedal.removeForms");
        if (value != null && !value.isEmpty()) {
            exclusionOption = FormExclusion.valueOf(value);

        }
    }
    
    private boolean showForm(final PdfObject form){
        
        switch (exclusionOption) {
            case ExcludeFormsAndAnnotations:
                //Show no annotations or forms
                return false;
            case ExcludeAnnotations:
                //Show only forms
                if (form.getParameterConstant(PdfDictionary.Type) == PdfDictionary.Annot && form.getNameAsConstant(PdfDictionary.FT) == -1) {
                    return false;
                }
                break;
            case ExcludeForms:
                //Show only annotations
                if (form.getNameAsConstant(PdfDictionary.FT) != -1) {
                    return false;
                }
                break;
            case ExcludeNone:
                //Show both annotations and forms
                break;
        }

        return true;
    }
    
}
