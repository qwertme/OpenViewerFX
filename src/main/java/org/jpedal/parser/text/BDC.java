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
 * BDC.java
 * ---------------
 */
package org.jpedal.parser.text;

import org.jpedal.io.ObjectDecoder;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.MCObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.ParserOptions;
import org.jpedal.render.DynamicVectorRenderer;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class BDC {

    public static PdfObject execute(int startCommand, final int dataPointer, final byte[] raw, final String op,
                                final GraphicsState gs, final PdfObjectReader currentPdfFile, final DynamicVectorRenderer current, final ParserOptions parserOptions) {

        final PdfObject BDCobj=new MCObject(op);
        BDCobj.setID(PdfDictionary.BDC); //use an existing feature to add unknown tags

        final int rawStart=startCommand;

        if(startCommand<1) {
            startCommand = 1;
        }

        boolean hasDictionary=true;
        while(startCommand<raw.length && raw[startCommand]!='<' && raw[startCommand-1]!='<'){
            startCommand++;

            if(raw[startCommand]=='B' && raw[startCommand+1]=='D' && raw[startCommand+2]=='C'){
                hasDictionary=false;
                break;
            }
        }

        /**
         * read Dictionary object
         */
        if(hasDictionary){// &&(parserOptions.getPdfLayerList()!=null && parserOptions.isLayerVisible())){
            //System.out.println(new String(raw));
            final ObjectDecoder objectDecoder=new ObjectDecoder(currentPdfFile.getObjectReader());
            objectDecoder.setEndPt(dataPointer);
            objectDecoder.readDictionaryAsObject(BDCobj, startCommand + 1, raw);
        }
        
        handleCommand(BDCobj, gs, current, dataPointer, raw, hasDictionary, rawStart, parserOptions);

        return BDCobj;

    }


    private static void handleCommand(final PdfObject BDCobj, final GraphicsState gs, final DynamicVectorRenderer current, final int dataPointer, final byte[] raw, final boolean hasDictionary, final int rawStart, final ParserOptions parserOptions) {

        parserOptions.setLayerLevel(parserOptions.getLayerLevel()+1);

        //add in layer if visible
        if(parserOptions.layers!=null && parserOptions.isLayerVisible()){

            String name="";

            if(hasDictionary){
                //see if name and if shown
                name = BDCobj.getName(PdfDictionary.OC);

                //see if Layer defined and get title if no Name as alternative
                if(name==null){

                    final PdfObject layerObj=BDCobj.getDictionary(PdfDictionary.Layer);
                    if(layerObj!=null) {
                        name = layerObj.getTextStreamValue(PdfDictionary.Title);
                    }
                }

                //needed to flags its a BMC
                parserOptions.layerClips.add(parserOptions.getLayerLevel());

                //apply any clip, saving old to restore on EMC
                final float[] BBox=BDCobj.getFloatArray(PdfDictionary.BBox);
                if(BBox!=null){
                   // Area currentClip=gs.getClippingShape();

                    //store so we restore in EMC
                    //if(currentClip!=null)
                      //  parserOptions.layerClips.put(parserOptions.layerLevel,currentClip.clone());

                    final Area clip=new Area(new Rectangle2D.Float(BBox[0], BBox[1], -gs.CTM[2][0]+(BBox[2]-BBox[0]), -gs.CTM[2][1]+(BBox[3]-BBox[1])));

                    if(clip.getBounds().getWidth()>0 && clip.getBounds().getHeight()>0){
                        gs.setClippingShape(clip);

                        current.drawClip(gs,clip,true);
                        
                        BDCobj.setClip(clip);
                    }

                }
            }else{ //direct just /OC and /MCxx

                //find /OC
                name = readOPName(dataPointer, raw, rawStart, name);
            }

            if(name!=null && !name.isEmpty()) //name referring to Layer or Title
            {
                parserOptions.setIsLayerVisible(parserOptions.layers.decodeLayer(name, true));
            }

            //flag so we can next values
            if(parserOptions.isLayerVisible()) {
                parserOptions.getLayerVisibility().add(parserOptions.getLayerLevel());           
            }

        }
    }

    private static String readOPName(final int dataPointer, final byte[] raw, final int rawStart, String name) {
        for(int ii=rawStart;ii<dataPointer;ii++){
            if(raw[ii]=='/' && raw[ii+1]=='O' && raw[ii+2]=='C'){ //find oc

                ii += 2;
                //roll onto value
                while(raw[ii]!='/') {
                    ii++;
                }

                ii++; //roll pass /

                final int strStart=ii;
                int charCount=0;

                while(ii<dataPointer){
                    ii++;
                    charCount++;

                    if(raw[ii]==13 || raw[ii]==10 || raw[ii]==32 || raw[ii]=='/') {
                        break;
                    }
                }

                name=new String(raw,strStart,charCount);

            }
        }
        return name;
    }

}
