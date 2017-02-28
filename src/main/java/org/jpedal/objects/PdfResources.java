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
 * PdfResources.java
 * ---------------
 */
package org.jpedal.objects;

import org.jpedal.exception.PdfException;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.outlines.OutlineData;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.parser.ValueTypes;
import org.jpedal.utils.LogWriter;
import org.w3c.dom.Document;

public class PdfResources {
	
	public static final int AcroFormObj = 1;
	public static final int GlobalResources = 2;
	public static final int StructTreeRootObj = 3;
	public static final int MarkInfoObj = 4;

    PdfLayerList layers;

    /**objects read from root*/
    private PdfObject metadataObj, acroFormObj, globalResources,PropertiesObj, structTreeRootObj, OCProperties,markInfoObj,OutlinesObj;

    /**
     * store outline data extracted from pdf
     */
    private OutlineData outlineData;

    /**
     * initialise OC Content and other items before Page decoded but after Resources read
     * @param current
     * @param currentPdfFile
     */
    public void setupResources(final PdfStreamDecoder current, final boolean alwaysCheck, final PdfObject Resources, final int pageNumber,
                               final PdfObjectReader currentPdfFile) throws PdfException {

        if (globalResources != null){
            current.readResources(globalResources,true);

            final PdfObject propObj=globalResources.getDictionary(PdfDictionary.Properties);
            if(propObj!=null) {
                PropertiesObj = propObj;
            }
        }

        /**read the resources for the page*/
        if (Resources != null){
            current.readResources(Resources,true);

            final PdfObject propObj=Resources.getDictionary(PdfDictionary.Properties);
            if(propObj!=null) {
                PropertiesObj = propObj;
            }
        }

        /**
         * layers
         */
        if(OCProperties!=null && (layers==null || pageNumber!=layers.getOCpageNumber() || alwaysCheck)){

            currentPdfFile.checkResolved(OCProperties);

            if(layers==null) {
                layers = new PdfLayerList();
            }

            layers.init(OCProperties, PropertiesObj, currentPdfFile,pageNumber);

        }

        current.setObjectValue(ValueTypes.PdfLayerList,layers);
    }
	
	public PdfObject getPdfObject(final int key) {
		
		PdfObject obj=null;
		
		switch(key){

            case AcroFormObj:
                obj=acroFormObj;
                break;

		case GlobalResources:
			obj=globalResources;
			break;

        case MarkInfoObj:
            obj=markInfoObj;
            break;

        case StructTreeRootObj:
            obj=structTreeRootObj;
            break;
		}
		return obj;
	}
	
	public void setPdfObject(final int key, final PdfObject obj) {
		
		switch(key){
		case GlobalResources:
			globalResources=obj;
			break;
		}
		
	}

	public void flush() {
		globalResources=null;
		
	}

    public void flushObjects() {

        //flush objects held
        metadataObj=null;
        acroFormObj=null;

        markInfoObj=null;
        PropertiesObj=null;
        OCProperties=null;
        structTreeRootObj=null;

        OutlinesObj=null;

        layers=null;

    }

    /**
     * flag to show if PDF document contains an outline
     */
    public final boolean hasOutline() {
        return OutlinesObj != null;
    }

    public void setValues(final PdfObject pdfObject, final PdfObjectReader currentPdfFile) {

        currentPdfFile.checkResolved(pdfObject);

        metadataObj=pdfObject.getDictionary(PdfDictionary.Metadata);

        acroFormObj=pdfObject.getDictionary(PdfDictionary.AcroForm);
        currentPdfFile.checkResolved(acroFormObj);

        markInfoObj=pdfObject.getDictionary(PdfDictionary.MarkInfo);

        structTreeRootObj=pdfObject.getDictionary(PdfDictionary.StructTreeRoot);

        OCProperties=pdfObject.getDictionary(PdfDictionary.OCProperties);

        OutlinesObj=pdfObject.getDictionary(PdfDictionary.Outlines);

        //set up outlines
        outlineData = null;
    }

    /**
     * provide direct access to outlineData object
     * @return  OutlineData
     */
    public OutlineData getOutlineData() {
        return outlineData;
    }

    public Document getOutlineAsXML(final PdfObjectReader currentPdfFile) {
        if (outlineData == null && OutlinesObj != null) {

            try {
                currentPdfFile.checkResolved(OutlinesObj);

                outlineData = new OutlineData();
                outlineData.readOutlineFileMetadata(OutlinesObj, currentPdfFile);

            } catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " accessing outline ");
                }
                outlineData = null;

                //
            }
        }

        if (outlineData != null) {
            return outlineData.getList();
        } else {
            return null;
        }
    }
    
    public PdfFileInformation getMetaData(final PdfObjectReader currentPdfFile) {
        if (currentPdfFile != null){
            /**Information object holds information from file*/
            return new PdfFileInformation().readPdfFileMetadata(metadataObj, currentPdfFile);
        }else {
            return null;
        }
    }

    public boolean isForm() {
        return acroFormObj!=null;
    }

    public PdfLayerList getPdfLayerList() {
        return layers;
    }
}
