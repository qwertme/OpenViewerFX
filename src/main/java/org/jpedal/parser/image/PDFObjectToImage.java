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
 * PDFObjectToImage.java
 * ---------------
 */
package org.jpedal.parser.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.jpedal.color.ColorSpaces;
import org.jpedal.exception.PdfException;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.parser.ValueTypes;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.ImageDisplay;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class PDFObjectToImage {

    
    
    public static BufferedImage getImageFromPdfObject(final PdfObject newSMask, int fx, final int fw, int fy, final int fh,
            final PdfObjectReader currentPdfFile, final ParserOptions parserOptions, final int formLevel, final float multiplyer, final boolean useTransparency, final float scaling) {
        
        BufferedImage smaskImage;
        final Graphics2D formG2;
        final byte[] objectData =currentPdfFile.readStream(newSMask,true,true,false, false,false, newSMask.getCacheName(currentPdfFile.getObjectReader()));
        
        final ObjectStore localStore = new ObjectStore();
        
        final DynamicVectorRenderer glyphDisplay=new ImageDisplay(0,false,20,localStore);
        
        glyphDisplay.setMode(DynamicVectorRenderer.Mode.SMASK);
        
        final boolean useHiRes=true;
        
        final PdfStreamDecoder glyphDecoder=new PdfStreamDecoder(currentPdfFile,useHiRes,null); //switch to hires as well
        
        glyphDecoder.setParameters(parserOptions.isPageContent(),parserOptions.isRenderPage(), parserOptions.getRenderMode(), parserOptions.getExtractionMode(), false,false);
        glyphDecoder.setObjectValue(ValueTypes.ObjectStore, localStore);
        glyphDisplay.setHiResImageForDisplayMode(useHiRes);
        glyphDecoder.setRenderer(glyphDisplay);
        glyphDecoder.setMultiplyer(multiplyer);
        glyphDecoder.setFormLevel(formLevel);
       
        //we need to explicitly set scaling to 1
        //glyphDisplay.setScalingValues(0,0,1);
        
        /**read any resources*/
        try{
            
            final PdfObject SMaskResources =newSMask.getDictionary(PdfDictionary.Resources);
            if (SMaskResources != null) {
                glyphDecoder.readResources(SMaskResources, false);
            }
            
        }catch(final PdfException e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        int w=fw-fx;
        if(w<0){
            w=-w;
            fx=fw;         
        }
        if(w==0) {
            w = 1;
        }
        
        int h=fh-fy;
        if(h<0){
            h=-h;
            fy=fh;          
        }
        if(h==0) {
            h = 1;
        }
        try{
            
            smaskImage=new  BufferedImage((int)(w*scaling),(int)(h*scaling),BufferedImage.TYPE_INT_ARGB);
            
            formG2=smaskImage.createGraphics();
            
            formG2.scale(scaling, scaling);
            formG2.translate(-fx,-fy);
            
            glyphDisplay.setG2(formG2);
            
            if(useTransparency){ //try to mimic any group transparency
                
                final PdfObject group=newSMask.getDictionary(PdfDictionary.Group);
                if(group!=null){
                    currentPdfFile.checkResolved(group);
                    final String Tname=group.getName(PdfDictionary.S);
                    
                    PdfObject cs = group.getDictionary(PdfDictionary.ColorSpace);

                    //System.out.println(group.getDictionary(PdfDictionary.ColorSpace) +" "+group.getObjectRefAsString()+" "+group.getBoolean(PdfDictionary.I)+" "+group.getBoolean(PdfDictionary.K)+" ");
                    if(group.getBoolean(PdfDictionary.I)==false && group.getBoolean(PdfDictionary.K)==false && cs!=null && cs.getParameterConstant(PdfDictionary.ColorSpace)==ColorSpaces.DeviceCMYK){
                      //  System.out.println("Ignore");
                    }else if(Tname.equals("Transparency")){
                        
                        formG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    }
                }
            }
            
        }catch(final Error err){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + err.getMessage());
            }
            
            smaskImage=null;
        }
        
        /**decode the stream*/
        if(objectData!=null) {
            glyphDecoder.decodeStreamIntoObjects(objectData, false);
        }
        
        glyphDecoder.dispose();
        
        localStore.flush();
        
        return smaskImage;
    }
}


