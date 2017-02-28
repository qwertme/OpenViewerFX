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
 * GUIData.java
 * ---------------
 */
package org.jpedal.objects.acroforms;

import java.awt.*;
import java.util.*;
import java.util.List;
import org.jpedal.external.CustomFormPrint;
import org.jpedal.external.ExternalHandlers;

import org.jpedal.objects.PdfPageData;
import static org.jpedal.objects.acroforms.ReturnValues.FORMOBJECTS_FROM_NAME;
import static org.jpedal.objects.acroforms.ReturnValues.FORMOBJECTS_FROM_REF;
import static org.jpedal.objects.acroforms.ReturnValues.FORM_NAMES;
import static org.jpedal.objects.acroforms.ReturnValues.GUI_FORMS_FROM_NAME;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.acroforms.creation.FormFactory;

/**holds all data not specific to Swing/SWT/ULC*/
public class GUIData {
    
    /**
     * flag to make forms draw as images, not swing components
     */
    protected boolean rasterizeForms;
   
    /** for if we add a popup to the panel, could be used for adding other objects*/
    protected boolean forceRedraw;
    
    //allow user to move relative draw position
    protected int userX, userY,widestPageNR,widestPageR;
    
    /** the current display view, Display.SINGLE, Display.CONTINOUS etc */
    protected int displayView;
    
    public float dpi=72.0f;

    protected final Map rawFormData=new HashMap();
    
    /**allow user to set Forms to ignore*/
    protected Map componentsToIgnore=new HashMap();
    
    protected int insetW,insetH;
    
    protected PdfPageData pageData;
    
    /**
     * local copy needed in rendering
     */
    protected int indent;
    
    protected int[] cropOtherY;
    /**
     * track page scaling
     */
    protected float displayScaling;
    protected int rotation;
    
    /**
     * used to only redraw as needed
     */
    protected float lastScaling = -1, oldRotation, oldIndent;
    
    /**
     * used for page tracking
     */
    protected int startPage, currentPage, endPage;

    /**
     * used to draw pages offset if not in SINGLE_PAGE mode
     */
    protected int[] xReached, yReached;
    
    /** stores the forms in there original order, accessable by page */
    protected List<FormObject>[] formsUnordered, formsOrdered;
    
    /**
     * array to hold page for each component so we can scan quickly on page change
     */
    private int formCount;
    protected PdfLayerList layers;
    
    protected FormFactory formFactory;
    
    public void setLayerData(final PdfLayerList layers){
	this.layers=layers;
    }
    
    public void setRasterizeForms(final boolean inlineForms) {
        rasterizeForms = inlineForms;
    } 
    
    protected void setListForPage(final int page, final List<FormObject> comps, final boolean isSorted){
        if(isSorted){
	    formsOrdered[page] = comps;
	}else{
	    formsUnordered[page] = comps;
	}
    }
    
    protected Object checkGUIObjectResolved(final FormObject formObject){

        Object comp=null;
        if(formObject!=null){
            comp= formObject.getGUIComponent();
        }
        
        if(formObject!=null && comp==null){

            comp= resolveGUIComponent(formObject);

            if(comp!=null){
                setGUIComp(formObject, comp);
            }
        }

        return comp;
    }
    
    
    protected Object resolveGUIComponent(final FormObject formObject) {

        @SuppressWarnings("UnusedAssignment") Object retComponent=null;

        final int subtype=formObject.getParameterConstant(PdfDictionary.Subtype);//FT

        final int formFactoryType=formFactory.getType();
        
        final boolean[] flags = formObject.getFieldFlags();//Ff

		//ExternalHandlers.isXFAPresent() will only be true in forms version of PDF2HTML
        if(!ExternalHandlers.isXFAPresent() && (formFactory.getType()==FormFactory.HTML || formFactoryType==FormFactory.SVG)){
                
            if((formObject.getDictionary(PdfDictionary.RichMediaContent)!=null || subtype==PdfDictionary.Link)){
                retComponent = formFactory.annotationButton(formObject);
            }

        /** setup field */
        }else if (subtype == PdfDictionary.Btn) {//----------------------------------- BUTTON  ----------------------------------------

            boolean isPushButton = false, isRadio = false;// hasNoToggleToOff = false, radioinUnison = false;
            if (flags != null) {
                isPushButton = flags[FormObject.PUSHBUTTON_ID];
                isRadio = flags[FormObject.RADIO_ID];
            }

            if (isPushButton) {

                retComponent = formFactory.pushBut(formObject);

            }else if(isRadio){
                retComponent = formFactory.radioBut(formObject);
            }else {
                retComponent = formFactory.checkBoxBut(formObject);
            }

        } else {
            if (subtype ==PdfDictionary.Tx) { //-----------------------------------------------  TEXT --------------------------------------

                boolean isMultiline = false, hasPassword = false;// doNotScroll = false, richtext = false, fileSelect = false, doNotSpellCheck = false;
                if (flags != null) {
                    isMultiline = flags[FormObject.MULTILINE_ID] || (formObject.getTextString()!=null && formObject.getTextString().indexOf('\n')!=-1);
                    hasPassword = flags[FormObject.PASSWORD_ID];
                }

                if (isMultiline) {

                    if (hasPassword) {

                        retComponent = formFactory.multiLinePassword(formObject);

                    } else {

                        retComponent = formFactory.multiLineText(formObject);

                    }
                } else {//singleLine

                    if (hasPassword) {

                        retComponent = formFactory.singleLinePassword(formObject);

                    } else {

                        retComponent = formFactory.singleLineText(formObject);

                    }
                }
            }else if (subtype==PdfDictionary.Ch) {//----------------------------------------- CHOICE ----------------------------------------------

                boolean isCombo = false;// multiSelect = false, sort = false, isEditable = false, doNotSpellCheck = false, comminOnSelChange = false;
                if (flags != null) {
                    isCombo = flags[FormObject.COMBO_ID];
                }

                if (isCombo) {// || (type==XFAFORM && ((XFAFormObject)formObject).choiceShown!=XFAFormObject.CHOICE_ALWAYS)){

                    retComponent = formFactory.comboBox(formObject);

                } else {//it is a list

                    retComponent = formFactory.listField(formObject);
                }
            } else if (subtype == PdfDictionary.Sig) {

                retComponent = formFactory.signature(formObject);

            } else{	
                retComponent = formFactory.annotationButton(formObject);
            }
        }
		
        if (retComponent != null) {
            formObject.setGUIComponent(retComponent,formFactory.getType());
            setGUIComp(formObject, retComponent);
        }

        return retComponent;

    }

    public void dispose(){
    	//Nothing to dispose of in generic method, overrode by swing version
    }
    
    protected void displayComponent( final FormObject formObject, final Object comp) {
	
	if(1==1) {
        throw new RuntimeException("base method displayComponent( ) should not be called");
    }
	
    }
    
    /**
     * put components onto screen display
     * @param startPage
     * @param endPage
     */
    protected void displayComponents(final int startPage, final int endPage) {
        
        
        if (rasterizeForms || formsOrdered==null) {
            return;
        }
        
        this.startPage = startPage;
        this.endPage=endPage;
        
        FormObject formObject;
        Object comp;
        
        //System.out.println("displayComponents "+startPage+" "+endPage+" "+SwingUtilities.isEventDispatchThread());
        
        if(startPage>1) {
            removeHiddenForms(1, startPage);
        }
        
        /**
         * forms currently visible
         */
        for (int page = startPage; page < endPage; page++) {
            
            //get unsorted components and iterate over forms
            if(formsOrdered[page]!=null){
                
                for (final Object o : formsOrdered[page]) {
                    
                    if (o != null) {
                        
                        formObject = (FormObject) o;
                        
                        comp= checkGUIObjectResolved(formObject);
                        
                        if (comp != null) {
                            displayComponent(formObject, comp);
                        }
                    }
                }
            }
        }
        
        removeHiddenForms(endPage,pageData.getPageCount()+1);
    }
    
    void removeHiddenForms(final int startPage, final int endPage){
        //comment out as could be called by Canoo
        //throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    public boolean hasformsOnPageDecoded(final int page){
	return formsOrdered!=null && formsOrdered.length>page && formsOrdered[page]!=null;
    }
    
    /**
     * setup values needed for drawing page
     * @param pageData
     * @param page
     */
    protected void initParametersForPage(final PdfPageData pageData, final int page, final FormFactory formFactory, final float dpi){
	
	//ensure setup
	if(cropOtherY==null || cropOtherY.length<=page) {
        this.resetComponents(0, pageData.getPageCount(), false);
    }
	
	final int mediaHeight = pageData.getMediaBoxHeight(page);
	final int cropTop = (pageData.getCropBoxHeight(page) + pageData.getCropBoxY(page));
	
	//take into account crop
	if (mediaHeight != cropTop) {
        cropOtherY[page] = (mediaHeight - cropTop);
    } else {
        cropOtherY[page] = 0;
    }
	
	this.currentPage = page; //track page displayed
	
	this.formFactory = formFactory;
	
	this.dpi=dpi;
    }

    /**
     * used to flush/resize data structures on new document/page
     * @param formCount
     * @param pageCount
     * @param keepValues
     * return true if successful, false if formCount is less than current count.
     */
    public void resetComponents(final int formCount, final int pageCount, final boolean keepValues) {
	
	if(keepValues && this.formCount>formCount) {
        return;
    }
	
	this.formCount=formCount;
	
	if(!keepValues){
	    
            if(formsUnordered==null){
                formsUnordered = new List[pageCount+1];
                formsOrdered = new List[pageCount + 1];
            }
        
	    //reset offsets
	    cropOtherY=new int[pageCount+1];
	    
	    //reset the multi page shifting values so we dont get forms half way across the page on a single page view.
	    xReached = yReached = null;
	    
	}
    }
    
    /**
     * pass in current values used for all components
     * @param scaling
     * @param rotation
     * @param indent
     */
    public void setPageValues(final float scaling, final int rotation, final int indent, final int userX, final int userY, final int displayView, final int widestPageNR, final int widestPageR) {
	
	this.rotation=rotation;
	this.displayScaling=scaling;
	this.indent=indent;
	this.userX=userX;
	this.userY=userY;
	this.displayView = displayView;
	
	this.widestPageNR = widestPageNR;
	this.widestPageR = widestPageR;
    }
    
    /**
     * used to pass in offsets and PdfPageData object so we can access in rendering
     * @param pageData
     * @param insetW
     * @param insetH
     */
    protected void setPageData(final PdfPageData pageData, final int insetW, final int insetH) {
	
	//track inset on page
	this.insetW = insetW;
	this.insetH = insetH;
	
	this.pageData = pageData;
	
    }
    
    /**
     * offsets for forms in multi-page mode
     */
    public void setPageDisplacements(final int[] xReached, final int[] yReached) {
	
	this.xReached = xReached;
	this.yReached = yReached;
	
	//force redraw
	forceRedraw=true;
	
    }
    
    /**
     * force redraw (ie for refreshing layers)
     */
    public void setForceRedraw(final boolean forceRedraw) {
	
	this.forceRedraw=forceRedraw;
	
    }
    
    /**
     * store form data and allow lookup by PDF ref or name
     * (name may not be unique)
     * @param formObject
     */
    protected void storeRawData(final FormObject formObject) {
	
	final String ref=formObject.getObjectRefAsString();
	rawFormData.put(ref,formObject);
	
    }
    
    protected void flushFormData() {
	
	rawFormData.clear();
	
        formsOrdered=null;
        formsUnordered=null;
    
	this.oldIndent=-oldIndent;
	
    }
    
    /**
     * returns the raw formdata so that DefaultAcroRender can access
     */
    protected Map getRawFormData() {
	return Collections.unmodifiableMap(rawFormData);
    }
   
    
    protected void setGUIComp(final FormObject formObject, final Object rawField) {
	throw new RuntimeException("Should never be called");
    }

    /**
     * Allow you to get a list of values from the Forms.
     * 
     * If possible we recommend you work with the Form Objects rather than 
     * resolve GUI components unless you need the GUI values
     *
     * null key will return all values
     * 
     * if pageNumber is -1 it will process whole document, otherwise just that page
     * 
     * NO values will return empty list, not null
     */
    public List getFormComponents(final String key, final ReturnValues value, final int pageNumber) {
	
	final Iterator i=rawFormData.keySet().iterator();
	final ArrayList selectedForms=new ArrayList();
	FormObject form;
	boolean isPageSelected;
	String name;
	while(i.hasNext()){
	  
	    form= (FormObject) rawFormData.get(i.next());
	    isPageSelected=pageNumber==-1 || form.getPageNumber()==pageNumber;
	    name=form.getTextStreamValue(PdfDictionary.T);
	    
            switch(value){
		case GUI_FORMS_FROM_NAME:
		    if(isPageSelected && (key==null || (name != null && name.equals(key)) )){
			selectedForms.add(checkGUIObjectResolved(form));
		    }
		    break;
		    
		case FORMOBJECTS_FROM_NAME:
		    if (isPageSelected &&(key == null || (name != null && name.equals(key)) )) {
			selectedForms.add(form);
		    }
		    break; 
                    
               case FORMOBJECTS_FROM_REF:
		    if (isPageSelected &&(key == null || form.getObjectRefAsString().equals(key))) {
			selectedForms.add(form);
		    }
		    break;      
		    
		case FORM_NAMES:
		    if (isPageSelected && name!=null && !name.isEmpty() && !selectedForms.contains(name)) {
			selectedForms.add(name);
		    }
		    break;
		    
		default:
		    throw new RuntimeException("value "+ value+" not implemented");
	    }
	}
	
	return selectedForms;
    }

    public void resetAfterPrinting(){
	
	    forceRedraw=true;
    }

    public static int calculateFontSize(final int height, final int width, final boolean area, final String text) {
	
	//our new auto size routine, better size.
	int rawSize;
	final float areaFactor = 0.8f;
	
	final double h1 = height*areaFactor;
	if(text==null || text.isEmpty()) {
        return (int) h1;//12
    }
	
	//work out number of lines and chars on longest line
	final char[] textChrs = text.toCharArray();
	int maxLen = 0,curLen = 0,lines = 1;
	char lastChar=0;
	for (final char textChr : textChrs) {
	    switch (textChr) {
		case 10:
		case 13:
		    if ((textChr == 13 && lastChar == 10) ||
			    (textChr == 10 && lastChar == 13)) {
			//ignore;
		    }
		    lines++;
		    if (maxLen < curLen) {
                maxLen = curLen;
            }
		    curLen = 0;
		    break;
		default:
		    curLen++;
		    break;
	    }
	    lastChar = textChr;
	}
	
	if(maxLen<curLen) {
        maxLen = curLen;
    }
	
	//work out font size
	final double w1 = width*areaFactor;
	final double x2 = w1/maxLen;
	final double y2 = h1/lines;
	if(y2>x2*2){
	    rawSize = (int)x2*2;
	}else{
	    if(area && y2>14){
		    double fontsize = 14;
		    while(true){
			final double approxLines = height/fontsize;
			if(approxLines<5) {
                return (int) fontsize;
            } else {
                fontsize *= 1.1;
            }
		    }
		}
	    
	    rawSize = (int)y2;
	}
	
	//make sure its not over max of 14 in a textarea
	//or if we have a silly tiny number (probably flowing text) ignore it
	if(area && (rawSize<4 ||rawSize>14)) {
        rawSize = 12;
    }
	
	return rawSize;
	
    }
    
    public boolean formsRasterizedForDisplay() {
	return this.rasterizeForms;
    }
    
    protected static int getFontSize(final FormObject formObject, final int rotate, final float scale) {
	
	int rawSize = formObject.getTextSize();
	
	if (rawSize == -1) {
        rawSize = 0;//change -1 to best fit so that text is more visible
    }
	
	if (rawSize == 0) {// best fit
	    
	    // work out best size for bounding box of object
	    final Rectangle bounds=formObject.getBoundingRectangle();
	    
	    int width=bounds.width;
	    int height=bounds.height;
	    
	    if (rotate == 90 || rotate == 270) {
		final int tmp = height;
		height = width;
		width = tmp;
	    }
	    
	    rawSize = (int)(height * 0.85);
	    
	    final String textVal = formObject.getTextString();
	    
	    final int formType=formObject.getFormType();
	    
	    // check this code tested and change
	    if (formType==FormFactory.MULTILINETEXT ||  formType==FormFactory.MULTILINEPASSWORD) {
		
		rawSize = calculateFontSize(height, width, true,textVal);
		
	    } else if (formType==FormFactory.SINGLELINETEXT ||  formType==FormFactory.SINGLELINEPASSWORD) {
		
		rawSize = calculateFontSize(height, width, false,textVal);
		
	    } else if (textVal != null) {
		rawSize = calculateFontSize(height, width, false,textVal);
	    }
	}
	
	int size = (int) (rawSize * scale);
	if (size < 1) {
	    size = 1;
	}
	
	return size;
    }

    protected void removeAllComponentsFromScreen() {
        throw new UnsupportedOperationException("removeAllComponentsFromScreen Not supported yet.");
    }

    public void setAutoFontSize(final FormObject formObj) {
        throw new UnsupportedOperationException("setAutoFontSize Not supported yet."); 
    }

     public void renderFormsOntoG2(final Object raw, final int pageIndex, final int currentIndent,
                                  final int currentRotation, final Map componentsToIgnore, final FormFactory formFactory, final int pageHeight) {
        throw new UnsupportedOperationException("renderFormsOntoG2Not supported yet."); 
    }

    public void setCustomPrintInterface(final CustomFormPrint customFormPrint) {
        throw new UnsupportedOperationException("setCustomPrintInterface Not supported yet."); 
    }

    public void resetScaledLocation(final float scaling, final int i, final int i0) {
        throw new UnsupportedOperationException("resetScaledLocation Not supported yet."); 
    }

    public void setRootDisplayComponent(final Object formsPane) {
        throw new UnsupportedOperationException("setRootDisplayComponent Not supported yet."); 
    }

    /**
     * used internally by HTML  drawing code to flatten forms
     * @param isOrdered
     * @return 
     */
    public java.util.List[] getFormList(final boolean isOrdered) {

        if(isOrdered) {
            return this.formsOrdered;
        } else {
            return this.formsUnordered;
        }
    }

    public void renderFormsOntoG2InHeadless(final Object raw, final int pageIndex, final int currentRotation, final Map componentsToIgnore, final FormFactory formFactory, final int pageHeight)  {
        throw new UnsupportedOperationException("renderFormsOntoG2InHeadless Not supported yet.");
    }
}
