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
 * AcroRenderer.java
 * ---------------
 */
package org.jpedal.objects.acroforms;

import java.awt.image.BufferedImage;
import org.jpedal.exception.PdfException;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.*;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.acroforms.utils.FormUtils;

import org.jpedal.objects.raw.*;
import org.jpedal.utils.*;

import java.util.*;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.objects.acroforms.creation.SwingFormCreator;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.parser.*;


/**
 * Provides top level to forms handling, assisted by separate classes to
 * decode widgets (FormDecoder - default implements Swing set)
 * create Form widgets (implementation of FormFactory),
 * store and render widgets (GUIData),
 * handle Javascript and Actions (Javascript and ActionHandler)
 * and support for Signature object
 */
public class AcroRenderer{
    
    FormObject[] Fforms, Aforms;
    
    private PdfObject AcroRes;
    
    private float dpi=72f;
    
    private Object[] CO;
    PdfArrayIterator fieldList;
    private PdfArrayIterator[] annotList;
   
    /**
     * flag to show we ignore forms
     */
    private boolean ignoreForms;

    private boolean alwaysUseXFA;

    /**
     * creates all GUI components from raw data in PDF and stores in GUIData instance
     */
    public FormFactory formFactory;
    
    /**holder for all data (implementations to support Swing and ULC)*/
    public GUIData compData;
    
    /**holds sig object so we can easily retrieve*/
    private Set<FormObject> sigObject;
    private Map<String, String> sigKeys; //and allow us to trap multiple if in both Annot and Form
    
    /**
     * holds copy of object to access the mediaBox and cropBox values
     */
    private PdfPageData pageData;
    
    /**
     * number of entries in acroFormDataList, each entry can have a button group of more that one button
     * 'A' annot and 'F' form - A is per page, F is total hence 3 variables
     */
    private int[] AfieldCount;
    
    private int ATotalCount,FfieldCount;
    
    /**
     * number of pages in current PDF document
     */
    int pageCount;
    
    /**
     * handle on object reader for decoding objects
     */
    PdfObjectReader currentPdfFile;
    
    /**
     * parses and decodes PDF data into generic data for converting to widgets
     */
    FormStream fDecoder;
    
    /**
     * handles events like URLS, EMAILS
     */
    private ActionHandler formsActionHandler;
    
    /**
     * handles Javascript events
     */
    private Javascript javascript;
    
    /*flag to show if XFA or FDF*/
    boolean hasXFA;
    private boolean isContainXFAStream;
    
    /**
     * flag to show if we use XFA
     */
    private boolean useXFA;

    /**
     * allow us to differentiate underlying PDF form type
     */
    Enum PDFformType;
    
    private SwingFormCreator formCreator;

    /**
     * used to create version without XFA support in XFA version.
     * Should not be used otherwise.
     * @param useXFA 
     */
    public AcroRenderer(){}
    
    public void useXFAIfAvailable(final boolean useXFA) {

        this.useXFA=useXFA;
        
    }
    
    /**
     * reset handler (must be called Before page opened)
     * - null Object resets to default
     */
    public void resetHandler(final ActionHandler formsActionHandler, final float dpi, final Javascript javascript) {
        
        this.formsActionHandler=formsActionHandler;
        
        this.dpi=dpi;
        
        this.javascript=javascript;
        
        //pass values down
        if (formFactory != null){
            formFactory.reset(this.getFormResources(), formsActionHandler,pageData,currentPdfFile);
        }
    }
    
    /**
     * make all components invisible on all pages by removing from Display
     */
    public void removeDisplayComponentsFromScreen() {
        
        if(compData!=null) {
            compData.removeAllComponentsFromScreen();
        }
        
    }
    
    /**
     * initialise holders and variables, data structures and get a handle on data object
     *
     *
     */
    public int openFile(int pageCount, final int insetW, final int insetH, final PdfPageData pageData, final PdfObjectReader currentPdfFile, final PdfObject acroObj) {
        
        this.pageCount = pageCount;
        //        if(newXFACode){
        //        	pageCount = 1000;
        //        }
        this.currentPdfFile = currentPdfFile;
        this.pageData = pageData;

        compData.flushFormData();
        
        //explicitly flush
        sigObject=null;
        sigKeys=null;
        
        //track inset on page
        compData.setPageData(pageData,insetW,insetH);
        
        if (acroObj == null) {
            
            FfieldCount = 0;
            
            fieldList=null;
        } else{
            
            //handle XFA
            final PdfObject XFAasStream;
            PdfArrayIterator XFAasArray = null;
            
            XFAasStream=acroObj.getDictionary(PdfDictionary.XFA);
            if(XFAasStream==null){
                XFAasArray=acroObj.getMixedArray(PdfDictionary.XFA);
                
                //empty array
                if(XFAasArray!=null && XFAasArray.getTokenCount()==0) {
                    XFAasArray = null;
                }
            }
            
            hasXFA= XFAasStream!=null || XFAasArray!=null;
            isContainXFAStream = hasXFA;

            /**
             * now read the fields
             **/
            fieldList = acroObj.getMixedArray(PdfDictionary.Fields);
            CO = acroObj.getObjectArray(PdfDictionary.CO);
            
            if(fieldList!=null){
                FfieldCount = fieldList.getTokenCount();
                
                AcroRes=acroObj.getDictionary(PdfDictionary.DR);
                
                if(AcroRes!=null) {
                    currentPdfFile.checkResolved(AcroRes);
                }
                
            }else{
                FfieldCount=0;
                AcroRes=null;
            }
            
            /**
             * choose correct decoder for form data
             */
            if (hasXFA && useXFA){
                processXFAFields(acroObj, currentPdfFile, pageData);
            }           
            
            //we need to read list if FDF
            //or redo list if Legacy XFA
            if(!hasXFA) {
                resolveIndirectFieldList(false);
            }
           
        }
        
        resetContainers(true);
        
        return pageCount;
    }

    /**
     * empty implementation in non-XFA AcroRenderer
     * 
     * @param acroObj1
     * @param currentPdfFile1
     * @param pageData1 
     */
    void processXFAFields(final PdfObject acroObj1, final PdfObjectReader currentPdfFile1, final PdfPageData pageData1) {
        throw new RuntimeException("This code (processXFAFields) should never be called");
    }
        
    void resolveIndirectFieldList(final boolean resolveParents){

        //allow for indirect
        while(FfieldCount==1){

            //may have been read before so reset
            fieldList.resetToStart();
            
            final String key=fieldList.getNextValueAsString(false);

            final FormObject kidObject = new FormObject(key);
            currentPdfFile.readObject(kidObject);

            final byte[][] childList =getKid(kidObject, resolveParents);

            if(childList==null) {
                break;
            }

            fieldList=new PdfArrayIterator(childList);
            FfieldCount = fieldList.getTokenCount();
        }
    }
    
    /**
     * initialise holders and variables and get a handle on data object
     * <br>
     * Complicated as Annotations stored on a PAGE basis whereas FORMS stored on
     * a file basis
     */
    public void resetAnnotData(final int insetW, final int insetH, final PdfPageData pageData, final int page,
            final PdfObjectReader currentPdfFile, final byte[][] currentAnnotList) {
        
        this.currentPdfFile = currentPdfFile;
        this.pageData = pageData;
        
        boolean resetToEmpty = true;
        addedMissingPopup = false;
        
        //track inset on page
        compData.setPageData(pageData,insetW,insetH);
        
        if (currentAnnotList==null) {
            
            AfieldCount = null;
            ATotalCount=0;
            if(annotList!=null) {
                annotList[page] = null;
            }
            
            annotList=null;
            
        }else{
            
            int pageCount=pageData.getPageCount()+1;
            if(pageCount<=page) {
                pageCount = page + 1;
            }
            if(annotList==null){
                annotList=new PdfArrayIterator[pageCount];
                AfieldCount=new int[pageCount];
            }else if(page>=annotList.length){
                
                final PdfArrayIterator[] tempList=annotList;
                final int[] tempCount=AfieldCount;
                AfieldCount=new int[pageCount];
                annotList=new PdfArrayIterator[pageCount];
                
                for(int ii=0;ii<tempList.length;ii++){
                    AfieldCount[ii]=tempCount[ii];
                    annotList[ii]=tempList[ii];
                }
            }else if(AfieldCount==null) {
                AfieldCount = new int[pageCount];
            }
            
            annotList[page]=new PdfArrayIterator(currentAnnotList);
            
            final int size =annotList[page].getTokenCount();
            
            AfieldCount[page] = size;
            ATotalCount += size;
            resetToEmpty = false;
            
            /**
             * choose correct decoder for form data
             */
            if(fDecoder==null){

                PDFformType=FormTypes.NON_XFA;

                fDecoder = new FormStream();
                
            }
        }
        resetContainers(resetToEmpty);
        
    }
    
    /**
     * flush or resize data containers
     */
    protected void resetContainers(final boolean resetToEmpty) {
        
        
        /**form or reset Annots*/
        if (resetToEmpty) {
            
            compData.resetComponents(ATotalCount+FfieldCount, pageCount, false);
        }else{
            compData.resetComponents(ATotalCount+FfieldCount,pageCount,true);
        }
        
        if (formFactory == null) {
            formFactory=formCreator.createFormFactory();
            formFactory.reset(this.getFormResources(), formsActionHandler, pageData,  currentPdfFile);
        } else {
            //to keep customers formfactory usable
            formFactory.reset(this.getFormResources(), formsActionHandler, pageData,  currentPdfFile);
            //formFactory.setDataObjects(compData);
        }
    }

    private boolean addedMissingPopup;
    /**
     * build forms display using standard swing components
     */
    public void createDisplayComponentsForPage(final int page, final PdfStreamDecoder current) {

       // System.out.println("createDisplayComponents "+page);
        
        final Map<String, String> formsCreated=new HashMap<String, String>();
        
        //check if we want to flatten forms
        final String s = System.getProperty("org.jpedal.flattenForm");
        if(s!=null && s.equalsIgnoreCase("true")){
            compData.setRasterizeForms(true);
        }
        
        /**see if already done*/
         if (!compData.hasformsOnPageDecoded(page) || (formsRasterizedForDisplay() && current!=null)) {
        
            /**ensure space for all values*/
            compData.initParametersForPage(pageData,page,formFactory,dpi);
            
            /**
             * think this needs to be revised, and different approach maybe storing, and reuse if respecified in file,
             * need to look at other files to work out solution.
             * files :-
             * lettreenvoi.pdf page 2+ no next page field
             * costena.pdf checkboxes not changable
             *
             * maybe if its just reset on multipage files
             */
            
            //list of forms done
            final Map<String, String> formsProcessed=new HashMap<String, String>();
            
            int Acount=0;
            if(AfieldCount!=null && AfieldCount.length>page) {
                Acount = AfieldCount[page];
            }
            
            Fforms = new FormObject[FfieldCount];
            FormObject[] xfaFormList = null;
            
            Aforms = new FormObject[Acount];
            FormObject formObject;
            String objRef;
            int i, count;
            
            if(hasXFA && useXFA){
                xfaFormList=createXFADisplayComponentsForPage(xfaFormList,page);
            }else{
            
                //scan list for all relevant values and add to array if valid
                //0  = forms, 1 = annots
                final int decodeToForm = 2;
                
                for(int forms=0;forms<decodeToForm;forms++){
                    
                    i=0;
                    
                    if(forms==0){
                        count=0;
                        if(fieldList!=null){
                            fieldList.resetToStart();
                            count=fieldList.getTokenCount()-1;
                        }
                        
                    }else{
                        if(annotList!=null && annotList.length>page && annotList[page]!=null && !isContainXFAStream){
                            annotList[page].resetToStart();
                            
                            //create lookup and array for values to set order correctly in HTML
                            if(formFactory.getType()==FormFactory.HTML){
                                final Map<String, String> annotOrder=new HashMap<String, String>();
                                
                                final int count2=annotList[page].getTokenCount();
                                String val;
                                for(int ii=0;ii<count2;ii++){
                                    val=annotList[page].getNextValueAsString(true);
                                    annotOrder.put(val,String.valueOf(ii+1));
                                }
                                
                                formFactory.setAnnotOrder(annotOrder);
                            }
                            
                            annotList[page].resetToStart();
                        }
                        count=Acount-1;
                    }
                    
                    for (int fieldNum =count; fieldNum >-1; fieldNum--) {
                        
                        objRef=null;
                        
                        if(forms==0){
                            if(fieldList!=null) {
                                objRef = fieldList.getNextValueAsString(true);
                            }
                        }else{
                            if(addedMissingPopup && !annotList[page].hasMoreTokens()){
                        		//Ignore this as we have added our own object that does not need reading
                        		//with the PdfArrayIterator. Code positioned like this to explain.
                                //Test File : baseline_screens\cid2\SampleError.pdf
                        	}else{
                        		if(annotList.length>page && annotList[page]!=null) {
                        			objRef = annotList[page].getNextValueAsString(true);
                        		}
                        	}
                        }
                        
                        if(objRef==null || (objRef!=null && (formsProcessed.get(objRef)!=null || objRef.isEmpty()))) {
                            continue;
                        }
                        
                        formObject=convertRefToFormObject(objRef,page);

                        /**
                         * Only allows Annotations if in Annots page stream
                         */
                        if(forms==0 && formObject!=null && formObject.getFormType()==-1){

                            //storeSignatures(formObject, formObject.getParameterConstant(PdfDictionary.Subtype));

                            continue;
                        }
                        
                        if(formObject.getParameterConstant(PdfDictionary.Subtype)==PdfDictionary.Text &&
                        	formObject.getDictionary(PdfDictionary.Popup)==null){
                        		
                        		final FormObject po = new FormObject(PdfDictionary.Annot);
                        		po.setIntNumber(PdfDictionary.F, 24); //Bit Flag for bits 4+5 (No Zoom, No Rotate)
                        		po.setBoolean(PdfDictionary.Open, formObject.getBoolean(PdfDictionary.Open));
                        		po.setConstant(PdfDictionary.Subtype, PdfDictionary.Popup);
                        		final float[] rect = formObject.getFloatArray(PdfDictionary.Rect);
                        		
                        		if(pageData.getRotation(page)%180!=0) {
                                    po.setFloatArray(PdfDictionary.Rect, new float[]{rect[2] - 160, -100, rect[2], 0});
                                } else {
                                    po.setFloatArray(PdfDictionary.Rect, new float[]{pageData.getCropBoxWidth(page), rect[3] - 100, pageData.getCropBoxWidth(page) + 160, rect[3]});
                                }
                        		
                        		po.setStringKey(PdfDictionary.Parent, formObject.getObjectRefAsString().getBytes());
                        		po.setParentPdfObj(formObject);
								
                        		po.setPageNumber(page);
                        		formObject.setDictionary(PdfDictionary.Popup, po);
                        		
                        		final FormObject[] newForms = new FormObject[Aforms.length+1];
                        		for(int ii=0; ii!=Aforms.length; ii++){
                        			newForms[ii] = Aforms[ii];
                        		}
                        		newForms[Aforms.length] = po;
                        		
                        		Aforms = newForms;
                        		
                        		AfieldCount[page]++;
                                
                                addedMissingPopup = true;
                        	}
                      
                        final byte[][] kids=formObject.getKeyArray(PdfDictionary.Kids);
                        if(kids!=null) //not 'proper' kids so process here
                        {
                            i = flattenKids(page, formsProcessed, formObject, i, forms);
                        } else {
                            i = processFormObject(page, formsProcessed, formObject, objRef, i, forms);
                        }
                    }
                }             
            }
            
            final List<FormObject> unsortedForms= new ArrayList<FormObject>();
            final List<FormObject> sortedForms = new ArrayList<FormObject>();
            compData.setListForPage(page,unsortedForms,false);
            compData.setListForPage(page, sortedForms, true);
            
            //XFA, FDF FORMS then ANNOTS
            final int readToForm = 3;
            
            for(int forms=0;forms<readToForm;forms++){
                
                count=0;
                
                if(forms==0){
                    if(xfaFormList!=null) {
                        count = xfaFormList.length;
                    }
                }else if(forms==1){
                    if(Fforms==null){
                        count=0;
                    }else{
                        //store current order of forms for printing
                        for (final FormObject Fform : Fforms) {
                            if (Fform != null) {
                                unsortedForms.add(Fform);
                            }
                        }

                        //sort forms into size order for display
                        Fforms = FormUtils.sortGroupLargestFirst(Fforms);
                        count=Fforms.length;
                    }
                }else{
                    //store current order of forms for printing
                    for (final FormObject Aform : Aforms) {
                        if (Aform != null) {
                            unsortedForms.add(Aform);
                        }
                    }
                    
                    //sort forms into size order for display
                    if(!formsRasterizedForDisplay()){
                        Aforms = FormUtils.sortGroupLargestFirst(Aforms);
                    }
                    
                    if(isContainXFAStream){
                        final HashMap<Double,ArrayList<FormObject>> tabMap = new HashMap<Double,ArrayList<FormObject>>();
                        double maxY = 0;
                        for(final FormObject obj : Aforms){
                            if(obj!=null){
                                final int x = obj.getBoundingRectangle().x;
                                final Double y = obj.getBounding2DRectangleForTabbing().getY();
                                maxY = Math.max(y,maxY);
                                
                                if(tabMap.containsKey(y)){
                                    final ArrayList<FormObject> fList = tabMap.get(y);
                                    int insertion = -1;
                                    for(int z=0;z<fList.size();z++){
                                        final int nextX = fList.get(z).getBoundingRectangle().x;
                                        if(nextX<x){
                                            insertion = z;
                                        }
                                    }
                                    if(insertion == -1){
                                        fList.add(0,obj);
                                    } else{
                                        fList.add(insertion+1, obj);
                                    }
                                } else {
                                    final ArrayList<FormObject> list = new ArrayList<FormObject>();
                                    list.add(obj);
                                    tabMap.put(y,list);
                                }
                            }
                        }
                        final FormObject [] finalList = new FormObject[Aforms.length];
                        int objCount = 0;
                        
                        final Object[] keys  = new Object[tabMap.size()];
                        int cc = 0;
                        for(final Object k : tabMap.keySet().toArray()){
                            keys[cc] = k;
                            cc++;
                        }
                        Arrays.sort(keys);
                                                
                        for(int k = keys.length;k>0;k--){
                            final ArrayList<FormObject> objList = tabMap.get(keys[k-1]);
                            if(objList != null){
                                for(final FormObject f :objList){
                                    finalList[objCount] = f;
                                    objCount++;
                                }
                            }
                        }
                        Aforms =  finalList;
                    }
                    count=Aforms.length;
                }
                
                boolean firstPopup = true;
                
                for (int k = 0; k <count; k++) {
                    
                    if(forms==0) {
                        formObject = xfaFormList[k];
                    } else if(forms==1) {
                        formObject = Fforms[k];
                    } else {
                        formObject = Aforms[k];
                    }

                    if (formObject != null && (formsCreated.get(formObject.getObjectRefAsString())==null) && page==formObject.getPageNumber()){// && !formObject.getObjectRefAsString().equals("216 0 R")){
                        //String OEPROPval=formObject.getTextStreamValue(PdfDictionary.EOPROPtype);
                        //NOTE: if this custom form needs redrawing more change ReadOnlyTextIcon.MAXSCALEFACTOR to 1;
                        if(formsRasterizedForDisplay() && current!=null){// || OEPROPval!=null){
                            
                            //rasterize any flattened PDF forms here
                            try {
                               // current.drawFlattenedForm(formObject,false);
                                final int type=formFactory.getType();
                                getFormFlattener().drawFlattenedForm(current, formObject, type == FormFactory.HTML || type == FormFactory.SVG, (PdfObject) this.getFormResources()[0]);
                            
                            }catch( final PdfException e ){
                                //tell user and log
                                if(LogWriter.isOutput()) {
                                    LogWriter.writeLog("Exception: " + e.getMessage());
                                }
                                //
                            }
                            
                        }else {
                            createField(formObject); //now we turn the data into a Swing component
                            //set the raw data here so that the field names are the fully qualified names
                            compData.storeRawData(formObject); //store data so user can access
                            
                            formsCreated.put(formObject.getObjectRefAsString(), "x");
                            
                            //original method we still use for HTML/SVG
                            if(this.formFactory.getType()==FormFactory.HTML || this.formFactory.getType()==FormFactory.SVG || this.formFactory.getType()==FormFactory.JAVAFX){
                                sortedForms.add(formObject);
                            
                                //neede in display to fix position issues
                            }else if(formObject.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Popup){
                            	/*
                            	 * To match examples the first popup is drawn over all
                            	 * then we draw all other popups in the pdf order from
                            	 * the bottom to the one before the top
                            	 */
                            	if(firstPopup){
                            		sortedForms.add(formObject);
                            		firstPopup = false;
                            	}else {
                                    sortedForms.add(sortedForms.size() - 1, formObject);
                                }
                            }else {
                                sortedForms.add(0, formObject);
                            }
                        }
                    }
                }
            }
            
            if(!formsRasterizedForDisplay()){
                
                // Go through all forms created and run through javascript for initiation
                try{
                    //for(int p=1;p<=page;p++){ //THIS DOES NOT SCALE Do not add back!!!!
                    //get current page object
                    final String ref = currentPdfFile.getReferenceforPage(page);
                    final PageObject pageObj = new PageObject(ref);
                    currentPdfFile.readObject(pageObj);
                    
                    //call Page Level open commands
                    if (javascript != null && formsActionHandler!=null) {
                        //NOTE: moved here as the Runnable can be executed after forms have been created which is too late.
                        formsActionHandler.O(pageObj,PdfDictionary.AA);
                        formsActionHandler.O(pageObj,PdfDictionary.A);//just in case
                        formsActionHandler.PO(pageObj,PdfDictionary.AA);
                        formsActionHandler.PO(pageObj,PdfDictionary.A);//just in case
                    }
                    
                    if(formFactory.getType()!=FormFactory.HTML && formFactory.getType()!=FormFactory.SVG){
                        //then initilise each field
                        initJSonFields(formsCreated);
                    }
                    
                }catch(final Exception e){
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        }
    }

    private void storeSignatures(final FormObject formObject, final int subtype) {

        //if sig object set global sig object so we can access later
        if(subtype == PdfDictionary.Sig){

            if(sigObject==null){ //ensure initialised
                sigObject = new HashSet<FormObject>();
                sigKeys=new HashMap<String, String>();
            }

            if(!sigKeys.containsKey(formObject.getObjectRefAsString())) {  //avoid duplicates
                sigObject.add(formObject);
                sigKeys.put(formObject.getObjectRefAsString(),"x");
            }

        }
    }

    private int flattenKids(final int page, final Map<String, String> formsProcessed, final FormObject formObject, int i, final int forms) {
        
        final byte[][] kidList=formObject.getKeyArray(PdfDictionary.Kids);
        final int kidCount=kidList.length;
        
        //resize to fit
        if(forms==0){
            final int oldCount=Fforms.length;
            final FormObject[] temp=Fforms;
            Fforms=new FormObject[oldCount+kidCount-1];
            System.arraycopy(temp, 0, Fforms, 0, oldCount);
        }else{
            final int oldCount=Aforms.length;
            final FormObject[] temp=Aforms;
            Aforms=new FormObject[oldCount+kidCount-1];
            System.arraycopy(temp, 0, Aforms, 0, oldCount);
        }
        
        for (final byte[] aKidList : kidList) { //iterate through all parts
            
            final String key = new String(aKidList);
            
            //now we have inherited values, read
            final FormObject childObj = new FormObject(key);
            
            //inherit values
            if (formObject != null) {
                childObj.copyInheritedValuesFromParent(formObject);
            }
            
            currentPdfFile.readObject(childObj);
            //childObj.setPDFRef(key);
            
            
            childObj.setRef(key);
            
            if (childObj.getKeyArray(PdfDictionary.Kids) == null) {
                
                if(!childObj.isAppearanceUsed()){
                    new FormStream().createAppearanceString(childObj, currentPdfFile);
                }
                
                i = processFormObject(page, formsProcessed, childObj, key, i, forms);
            } else {
                
                i = flattenKids(page, formsProcessed, childObj, i, forms);
            }
        }
        return i;
    }
    
    private int processFormObject(final int page, final Map<String, String> formsProcessed, final FormObject formObject, final String objRef, int i, final int forms) {
        boolean isOnPage=false;
        if(forms==0){ //check page
            PdfObject pageObj=formObject.getDictionary(PdfDictionary.P);
            
            byte[] pageRef=null;
            
            if(pageObj!=null) {
                pageRef = pageObj.getUnresolvedData();
            }
            
            if(pageRef==null || pageObj==null){
                
                final String parent=formObject.getStringKey(PdfDictionary.Parent);
                
                if(parent!=null){
                    final PdfObject parentObj = getParent(parent);
                    
                    pageObj=parentObj.getDictionary(PdfDictionary.P);
                    if(pageObj!=null) {
                        pageRef = pageObj.getUnresolvedData();
                    }
                }
            }
            
            if(pageRef==null){
                
                final byte[][] kidList = getKid(formObject,false);
                
                final boolean hasKids=kidList!=null && kidList.length>0;
                
                if (hasKids) {
                    
                    final int kidCount=kidList.length;
                    
                    FormObject kidObject;
                    for(int jj=0;jj<kidCount;jj++){
                        final String key=new String(kidList[jj]);
                        
                        kidObject= (FormObject) compData.getRawFormData().get(key);
                        
                        if(kidObject==null){
                            kidObject = new FormObject(key);
                            
                            currentPdfFile.readObject(kidObject);
                            
                            compData.storeRawData(kidObject);
                            
                        }
                        
                        pageObj=kidObject.getDictionary(PdfDictionary.P);
                        
                        if(pageObj!=null) {
                            pageRef = pageObj.getUnresolvedData();
                        }
                        
                        if(pageRef!=null) {
                            jj = kidCount;
                        }
                    }
                }
            }
            
            int objPage=-1;
            if(pageRef!=null) {
                objPage = currentPdfFile.convertObjectToPageNumber(new String(pageRef));
            }
            
            isOnPage=objPage==page;
            
        }
        
        if(forms==1 || isOnPage){
            
            formObject.setPageNumber(page);
            
            final String parent=formObject.getStringKey(PdfDictionary.Parent);
            
            //clone parent to allow inheritance of values or create new
            if(parent!=null){
                final FormObject parentObj = getParent(parent);
                
                //inherited values
                if(parentObj!=null){
                    //all values are copied from the parent inside this call
                    formObject.setParent(parent,parentObj,true);
                }
            }
            
            if(!formObject.isAppearanceUsed()){
                fDecoder.createAppearanceString(formObject, currentPdfFile);
            }
            
            //Check that type returns as a valid value to lock out broken objects
            //Added for case 22215
            if (formObject!= null && formObject.getParameterConstant(PdfDictionary.Subtype)!=-1){
                if(parent!=null) {
                    formObject.setParent(parent);//parent object was added earlier
                }
                
                if(forms==0) {
                    Fforms[i++] = formObject;
                } else {
                    Aforms[i++] = formObject;
                }
                
                //also flag
                if(objRef!=null) {
                    formsProcessed.put(objRef, "x");
                }
                //moved to after createField so the fully qualified names are stored.
                //keep this in case we have to return to old functioning.
                //                compData.storeRawData(formObject); //store data so user can access
            }
        }
        return i;
    }
    
    private FormObject getParent(final String parent) {
        
        FormObject parentObj=(FormObject)compData.getRawFormData().get(parent);
        
        if(parentObj==null && parent!=null){ //not yet read so read and cache
            parentObj = new FormObject(parent);
            currentPdfFile.readObject(parentObj);
            
            //remove kids in Parent
            parentObj.setKeyArray(PdfDictionary.Kids,null);
            
            compData.storeRawData(parentObj);
            
        }
        return parentObj;
    }
    
    private byte[][] getKid(final FormObject formObject, final boolean ignoreParent) {
        
        final int subtype=formObject.getParameterConstant(PdfDictionary.Subtype);
        if(subtype==PdfDictionary.Tx || subtype==PdfDictionary.Btn) {
            return null;
        }
        
        byte[][] kidList=formObject.getKeyArray(PdfDictionary.Kids);
        
        if(kidList!=null && !ignoreParent){
            final String parentRef=formObject.getStringKey(PdfDictionary.Parent);
            
            final PdfObject parentObj= this.getFormObject(parentRef);
            
            if(parentObj!=null && parentObj.getKeyArray(PdfDictionary.Kids)!=null) {
                kidList = null;
            }
        }
        
        return kidList;
    }
    
    /**
     * display widgets onscreen for range (inclusive)
     */
    public void displayComponentsOnscreen(final int startPage, int endPage) {
        
       // System.out.println("displayComponentsOnscreen "+startPage+" "+endPage);
        
        //make sure this page is inclusive in loop
        endPage++;
        
        compData.displayComponents(startPage, endPage);
        
        // <start-demo><end-demo>
    }
    
    private void initJSonFields(final Map<String, String> formsCreated) {
        
        //scan all fields for javascript actions
        //boolean formsChanged=false;
        for (final String ref : formsCreated.keySet()) {
            
            final FormObject formObject = getFormObject(ref);
            
            javascript.execute(formObject, PdfDictionary.K,ActionHandler.FOCUS_EVENT, ' ');
            
            //            if (result == ActionHandler.VALUESCHANGED) {
            //                formsChanged = true;
            //            }
        }
        
        // if we have some changed forms values then lets update them
        //if(formsChanged)
        //  updateChangedForms();
        
    }
    
    /**
     * create a widget to handle fields
     */
    private void createField(final FormObject formObject) {

        //
        
        /**/
        
        final Integer widgetType; //no value set
        
        final Object retComponent=null;
        
        final int subtype=formObject.getParameterConstant(PdfDictionary.Subtype);//FT
        
        final int formFactoryType=formFactory.getType();
        
        //if sig object set global sig object so we can access later
        storeSignatures(formObject, subtype);

        //check if a popup is associated
        if(formObject.getDictionary(PdfDictionary.Popup)!=null){
            formObject.setActionFlag(FormObject.POPUP);
        }
        
        //flags used to alter interactivity of all fields
        final boolean readOnly;
        final boolean required;
        final boolean noexport;

        final boolean[] flags = formObject.getFieldFlags();//Ff
        if (flags != null) {
            //noinspection UnusedAssignment
            readOnly = flags[FormObject.READONLY_ID];
            //noinspection UnusedAssignment
            required = flags[FormObject.REQUIRED_ID];
            //noinspection UnusedAssignment
            noexport = flags[FormObject.NOEXPORT_ID];
            
            /*
             * boolean comb=flags[FormObject.COMB_ID];
             * boolean comminOnSelChange=flags[FormObject.COMMITONSELCHANGE_ID];
             * boolean donotScrole=flags[FormObject.DONOTSCROLL_ID];
             * boolean doNotSpellCheck=flags[FormObject.DONOTSPELLCHECK_ID];
             * boolean fileSelect=flags[FormObject.FILESELECT_ID];
             * boolean isCombo=flags[FormObject.COMBO_ID];
             * boolean isEditable=flags[FormObject.EDIT_ID];
             * boolean isMultiline=flags[FormObject.MULTILINE_ID];
             * boolean isPushButton=flags[FormObject.PUSHBUTTON_ID];
             * boolean isRadio=flags[FormObject.RADIO_ID];
             * boolean hasNoToggleToOff=flags[FormObject.NOTOGGLETOOFF_ID];
             * boolean hasPassword=flags[FormObject.PASSWORD_ID];
             * boolean multiSelect=flags[FormObject.MULTISELECT_ID];
             * boolean radioinUnison=flags[FormObject.RADIOINUNISON_ID];
             * boolean richtext=flags[FormObject.RICHTEXT_ID];
             * boolean sort=flags[FormObject.SORT_ID];
             */
        }
        
        //hard-coded for HTML non-forms
        if(!ExternalHandlers.isXFAPresent() && (formFactoryType==FormFactory.HTML || formFactoryType==FormFactory.SVG)){
            widgetType=FormFactory.ANNOTATION;
           
        }else if (subtype == PdfDictionary.Btn) {//----------------------------------- BUTTON  ----------------------------------------
            
            //flags used for button types
            //20100212 (ms) Unused ones commented out
            boolean isPushButton = false, isRadio = false;// hasNoToggleToOff = false, radioinUnison = false;
            if (flags != null) {
                isPushButton = flags[FormObject.PUSHBUTTON_ID];
                isRadio = flags[FormObject.RADIO_ID];
                //hasNoToggleToOff = flags[FormObject.NOTOGGLETOOFF_ID];
                //radioinUnison = flags[FormObject.RADIOINUNISON_ID];
            }
            
            if (isPushButton) {               
                widgetType=FormFactory.PUSHBUTTON;               
            }else if(isRadio){
                widgetType=FormFactory.RADIOBUTTON;
                
            }else {
                widgetType=FormFactory.CHECKBOXBUTTON;
            }
            
        } else {
            if (subtype ==PdfDictionary.Tx) { //-----------------------------------------------  TEXT --------------------------------------
                
                //flags used for text types
                // 20100212 (ms) commented out ones not used
                boolean isMultiline = false, hasPassword = false;// doNotScroll = false, richtext = false, fileSelect = false, doNotSpellCheck = false;
                if (flags != null) {
                    isMultiline = flags[FormObject.MULTILINE_ID];
                    hasPassword = flags[FormObject.PASSWORD_ID];
                    //doNotScroll = flags[FormObject.DONOTSCROLL_ID];
                    //richtext = flags[FormObject.RICHTEXT_ID];
                    //fileSelect = flags[FormObject.FILESELECT_ID];
                    //doNotSpellCheck = flags[FormObject.DONOTSPELLCHECK_ID];
                }
                
                if (isMultiline) {
                    
                    if (hasPassword) {
                        
                        widgetType=FormFactory.MULTILINEPASSWORD;
                        
                    } else {                        
                        widgetType=FormFactory.MULTILINETEXT;
                    }
                } else {//singleLine
                    
                    if (hasPassword) {                      
                        widgetType=FormFactory.SINGLELINEPASSWORD;                       
                    } else {
                        widgetType=FormFactory.SINGLELINETEXT;                       
                    }
                }
            }else if (subtype==PdfDictionary.Ch) {//----------------------------------------- CHOICE ----------------------------------------------
                
                //flags used for choice types
                //20100212 (ms) Unused ones commented out
                boolean isCombo = false;// multiSelect = false, sort = false, isEditable = false, doNotSpellCheck = false, comminOnSelChange = false;
                if (flags != null) {
                    isCombo = flags[FormObject.COMBO_ID];
                    //multiSelect = flags[FormObject.MULTISELECT_ID];
                    //sort = flags[FormObject.SORT_ID];
                    //isEditable = flags[FormObject.EDIT_ID];
                    //doNotSpellCheck = flags[FormObject.DONOTSPELLCHECK_ID];
                    //comminOnSelChange = flags[FormObject.COMMITONSELCHANGE_ID];
                }
                
                if (isCombo) {// || (type==XFAFORM && ((XFAFormObject)formObject).choiceShown!=XFAFormObject.CHOICE_ALWAYS)){                   
                    widgetType=FormFactory.COMBOBOX;
                } else {//it is a list                   
                    widgetType=FormFactory.LIST;
                }
            } else if (subtype == PdfDictionary.Sig) {                
                widgetType=FormFactory.SIGNATURE;
            } else{//assume annotation if (formType == ANNOTATION) {
                
                widgetType=FormFactory.ANNOTATION;
                
            }
        }
        
        formObject.setFormType(widgetType);

        if(formFactory.getType()==FormFactory.HTML || formFactory.getType()==FormFactory.SVG){
            compData.checkGUIObjectResolved(formObject);
            
        }else if(retComponent!=null && formFactory.getType()!=FormFactory.SWING){
            formObject.setGUIComponent(retComponent,formFactory.getType());
            compData.setGUIComp(formObject, retComponent);
        }
    }
    
    /**
     * If possible we recommend you work with the Form Objects rather than
     * resolve GUI components
     *
     * null key will return all values
     *
     * if pageNumber is -1 it will process whole document, otherwise just that
     * page
     * 
     * For a full example of usage please see http://files.idrsolutions.com/samplecode/org/jpedal/examples/acroform/ExtractFormDataAsObject.java.html
     *
     * Object[] will vary depending on what ReturnValues enum is passed in and could contain String (Names), FormObject or Component 
     */
    public Object[] getFormComponents(final String objectName, final ReturnValues value, final int pageNumber) {
        
        //if(formFactory.getType()!=FormFactory.ULC){
            /**make sure all forms decoded*/
            if(pageNumber==-1){
                for (int p = 1; p < this.pageCount + 1; p++) //add init method and move scaling/rotation to it
                {
                    createDisplayComponentsForPage(p, null);
                }
            }else{
                createDisplayComponentsForPage(pageNumber, null);
            }
        //}
        
        return compData.getFormComponents(objectName,value,pageNumber).toArray();
        
    }
    
    /**
     * setup object which creates all GUI objects
     */
    public void setFormFactory(final FormFactory newFormFactory) {
        
        formFactory = newFormFactory;
        
        /**
         * allow user to create custom structure to hold data
         */
        compData=formFactory.getCustomCompData();
        
    }
    
    /**
     * get GUIData object with all widgets
     */
    public GUIData getCompData() {
        return compData;
    }
    
    /**return Signature as iterator with one or more objects or null*/
    public Iterator<FormObject> getSignatureObjects() {
        if(sigObject==null) {
            return null;
        } else {
            return sigObject.iterator();
        }
    }
    
    public ActionHandler getActionHandler() {
        return formsActionHandler;
    }
    
    public FormFactory getFormFactory() {
        return formFactory;
    }
    
    public void setIgnoreForms(final boolean ignoreForms) {
        this.ignoreForms=ignoreForms;
    }
    
    public boolean ignoreForms() {
        return ignoreForms;
    }
    
    public void dispose() {
        
        AfieldCount = null;
        
        fDecoder=null;
        
        formsActionHandler=null;
        
        //linkHandler=null;
        
        javascript=null;
        
        Fforms=null;
        
        Aforms=null;
        
        fieldList=null;
        annotList=null;
        
        formFactory=null;
        
        compData.dispose();
        compData=null;
        
        sigObject=null;
        sigKeys=null;
        
        pageData=null;
        
        currentPdfFile=null;
        
        fDecoder=null;
        
       
    }
    
    /**
     * get Iterator with list of all Annots on page or
     * return null if no Annots  - no longer needs
     * call to decodePage beforehand as checks itself
     *
     * @deprecated - getFormComponents(String objectName, ReturnValues value,int pageNumber) recommended
     * as much more flexible
     */
    public PdfArrayIterator getAnnotsOnPage(final int page) {
        
        //check annots decoded - will just return if done
        createDisplayComponentsForPage(page,null);
        
        if(annotList!=null && annotList.length>page && annotList[page]!=null){
            annotList[page].resetToStart();
            return annotList[page];
        }else{
            return null;
        }
    }
    
    /**
     * returns false if not XFA (or XFA in Legacy mode)
     * and true if XFA using XFA data
     * @return 
     */
    public boolean isXFA() {
        return hasXFA;
    }
    
    public boolean useXFA() {
        return useXFA;
    }

    public boolean hasFormsOnPage(final int page) {
        
        final boolean hasAnnots=(annotList!=null && annotList.length>page && annotList[page]!=null);
        final boolean hasForm=(hasXFA && useXFA && fDecoder.hasXFADataSet())||fieldList!=null;
        return hasAnnots || hasForm;
    }
    
    public Object[] getFormResources() {
        
        return new Object[]{AcroRes,CO};
    }
    
    
    public boolean formsRasterizedForDisplay() {
        return compData.formsRasterizedForDisplay();
    }
    
    /**
     * get FormObject
     * @param ref
     * @return
     * In all modes except HTML,SVG,JavaFX will decode other forms on pages if not
     * found
     */
    public FormObject getFormObject(final String ref) {
        
        FormObject obj = (FormObject) compData.getRawFormData().get(ref);
        
        //if not found now decode all page and retry
        if (obj == null && formFactory.getType()!=FormFactory.HTML && formFactory.getType()!=FormFactory.SVG) {
            for (int ii = 1; ii < this.pageCount; ii++) {
                
                createDisplayComponentsForPage(ii, null);
                obj = (FormObject) compData.getRawFormData().get(ref);
                
                if (obj != null) {
                    break;
                }
            }
        }
        
        return obj;
    }
    
    public void setInsets(final int width, final int height){
        compData.setPageData(compData.pageData, width, height);
    }

    FormObject convertRefToFormObject(final String objRef, final int page) {
        
        FormObject formObject = (FormObject) compData.getRawFormData().get(objRef);
        if (formObject == null) {

            formObject = new FormObject(objRef);
            
            if(page!=-1) {
                formObject.setPageRotation(pageData.getRotation(page));
            }
            
            //formObject.setPDFRef((String)objRef);

            if (objRef.charAt(objRef.length() - 1) == 'R') {
                currentPdfFile.readObject(formObject);
            } else {

                //changed by Mark as cover <<>> as well as 1 0 R
                formObject.setStatus(PdfObject.UNDECODED_REF);
                formObject.setUnresolvedData(StringUtils.toBytes(objRef), -1);
                currentPdfFile.checkResolved(formObject);
            }

            compData.storeRawData(formObject);
        }
        return formObject;
    }

    /**
     * Allow user to get ENUM to show type of form
     * FormTypes (XFA_LEGACY, XFA_DYNAMIC, NON_XFA)
     * @return
     */
    public Enum getPDFformType() {
        return PDFformType;
    }

    public void alwaysuseXFA(final boolean alwaysUseXFA) {
        this.alwaysUseXFA=alwaysUseXFA;
    }

    public boolean alwaysuseXFA() {
        return alwaysUseXFA;
    }

    public void init(SwingFormCreator formCreator) {

        this.formCreator=formCreator;
        
        compData=formCreator.getData();
        
    }

    public PdfStreamDecoder getStreamDecoder(PdfObjectReader currentPdfFile, boolean isHires, PdfLayerList layer,boolean isFirst) {
    
        if(isFirst){
            return new PdfStreamDecoder(currentPdfFile); 
        }else{
            return new PdfStreamDecoder(currentPdfFile, isHires,layer);
        }
    }
    
    public boolean showFormWarningMessage(int page) {
        
        boolean warnOnceOnForms=false;

        if(hasXFA){
            warnOnceOnForms=true;
            System.out.println("[WARNING] This file contains XFA forms that are not supported by this version of JPDF2HTML5. To convert into functional HTML forms and display non-legacy mode page content, JPDF2HTML5 Forms Edition must be used.");
        }else if(hasFormsOnPage(page)){
            warnOnceOnForms=true;
            System.out.println("[WARNING] This file contains form components that have been rasterized. To convert into functional HTML forms, JPDF2HTML5 Forms Edition must be used.");
        }
        
        return warnOnceOnForms;
    }

    FormObject[] createXFADisplayComponentsForPage(FormObject[] xfaFormList, int page) {
        throw new UnsupportedOperationException("createXFADisplayComponentsForPage should never be called");
    }

    public HashMap getPageMapXFA() {
        throw new UnsupportedOperationException("getPageMapXFA should never be called");
    }
    
    public byte[] getXMLContentAsBytes(int dataType) {
        return null;
    }

    public void outputJavascriptXFA(String path, String name) {
        throw new UnsupportedOperationException("outputJavascriptXFA should never be called");
    }

    public PrintStreamDecoder getStreamDecoderForPrinting(PdfObjectReader currentPdfFile, boolean isHires, PdfLayerList pdfLayerList) {
        //
         return null;
        /**/
    }

    public BufferedImage decode(PdfObject pdfObject, PdfObjectReader currentPdfFile, PdfObject XObject, int subtype, int width, int height, int offsetImage, float pageScaling) {
      return null;//
    }

    public FormFlattener getFormFlattener() {
        return new FormFlattener();
    }
}
