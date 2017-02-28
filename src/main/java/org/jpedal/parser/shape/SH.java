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
 * SH.java
 * ---------------
 */
package org.jpedal.parser.shape;

import com.idrsolutions.pdf.color.shading.ShadedPaint;
import org.jpedal.color.ColorspaceFactory;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.PdfPaint;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;

import java.awt.*;
import java.util.Map;
import org.jpedal.parser.Cmd;
import org.jpedal.parser.PdfObjectCache;

public class SH {

    public static void execute(final String shadingObject, final PdfObjectCache cache, final GraphicsState gs,
                          final boolean isPrinting, final Map shadingColorspacesObjects, final int pageNum,
                          final PdfObjectReader currentPdfFile,
                          final PdfPageData pageData, final DynamicVectorRenderer current) {
        
        PdfObject Shading= (PdfObject) cache.get(PdfObjectCache.LocalShadings, shadingObject);
        if(Shading==null){
            Shading= (PdfObject) cache.get(PdfObjectCache.GlobalShadings, shadingObject);
        }
        
        //workout shape
        Shape shadeShape=null;
        /**if(gs.CTM!=null){
         int x=(int)gs.CTM[2][0];
         int y=(int)gs.CTM[2][1];
         int w=(int)gs.CTM[0][0];
         if(w==0){
         w=(int)gs.CTM[1][0];
         }
         if(w<0)
         w=-w;

         int h=(int)gs.CTM[1][1];
         if(h==0)
         h=(int)gs.CTM[0][1];
         if(h<0)
         h=-h;
         shadeShape=new Rectangle(x,y,w,h);
         }/**/
        if(shadeShape==null) {
            shadeShape = gs.getClippingShape();
        }

        if(shadeShape==null) {
            shadeShape = new Rectangle(pageData.getMediaBoxX(pageNum), pageData.getMediaBoxY(pageNum), pageData.getMediaBoxWidth(pageNum), pageData.getMediaBoxHeight(pageNum));
        }
        
        if (current.isHTMLorSVG() && cache.groupObj==null) {
            current.eliminateHiddenText(shadeShape, gs, 7, true);
        }

        /**
         * generate the appropriate shading and then colour in the current clip with it
         */
        try{

            /**
             * workout colorspace
             **/
            final PdfObject ColorSpace=Shading.getDictionary(PdfDictionary.ColorSpace);

            final GenericColorSpace newColorSpace= ColorspaceFactory.getColorSpaceInstance(currentPdfFile, ColorSpace, shadingColorspacesObjects);

            newColorSpace.setPrinting(isPrinting);
            
            /**setup shading object*/ 
            final PdfPaint shading=new ShadedPaint(Shading, isPrinting,newColorSpace, currentPdfFile,gs.CTM,false, gs.CTM,true);
            
            //see 18992 and /Users/markee/PDFdata/test_data/sample_pdfs_html/general/test22.pdf
            if(gs.CTM[0][0]==0 && gs.CTM[0][1]>0 && gs.CTM[1][0]>0 && gs.CTM[1][1]==0 && current.getMode().equals(DynamicVectorRenderer.Mode.SMASK)){
               shading.setRenderingType(DynamicVectorRenderer.CREATE_SMASK);
            }

            /**
             * shade the current clip
             */
            gs.setFillType(GraphicsState.FILL);
            gs.setNonstrokeColor(shading);

            //track colorspace use
            cache.put(PdfObjectCache.ColorspacesUsed, newColorSpace.getID(), "x");

            current.drawShape(shadeShape, gs, Cmd.F);

        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
    }
}
