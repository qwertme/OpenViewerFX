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
 * StructuredContentHandler.java
 * ---------------
 */
package org.jpedal.objects.structuredtext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * structured content
 */
public class StructuredContentHandler {

    /**store entries from BMC*/
    private final Map markedContentProperties;

    /**handle nested levels of marked content*/
    int markedContentLevel;

    /**stream of marked content*/
    private StringBuffer markedContentSequence;

    private static final boolean debug=false;

    private boolean contentExtracted;

    private String currentKey;

    final Map keys, dictionaries;
    Map values;

    final boolean buildDirectly;
    
    DynamicVectorRenderer current;

    Document doc;

    Element root;

	private float x1,y1,x2,y2;
    boolean isHTML;
    
    public StructuredContentHandler(final Object markedContent) {

        //build either tree of lookuptable
        if(markedContent instanceof Map){
        	buildDirectly=false;
            values=(Map)markedContent;
        }else{
            buildDirectly=true;
            doc=(Document)markedContent;
            root = doc.createElement("TaggedPDF-doc");
		    doc.appendChild(root);
        }
        
        if(debug) {
            System.out.println("BuildDirectly=" + buildDirectly);
        }

        //this.currentPdfFile=currentPdfFile;

        markedContentProperties=new HashMap();
        markedContentLevel = 0;

        markedContentSequence = new StringBuffer();

        currentKey="";

        
        keys=new HashMap();
        
        dictionaries=new HashMap();

    }

    public void DP(final PdfObject BDCobj) {

        if(debug){
            System.out.println("DP----------------------------------------------------------"+markedContentLevel);

            System.out.println(BDCobj);

            System.out.println("BDCobj="+BDCobj);

        }


    }

    public void BDC(final PdfObject BDCobj) {

        //if start of sequence, reinitialise settings
        if (markedContentLevel == 0) {
            markedContentSequence = new StringBuffer();
        }

        markedContentLevel++;

        //only used in direct mode and breaks non-direct code so remove
        if(buildDirectly) {
            BDCobj.setIntNumber(PdfDictionary.MCID, -1);
        }
        
        final int MCID=BDCobj.getInt(PdfDictionary.MCID);
        
        //save key

        if(MCID!=-1) {
            keys.put(markedContentLevel, String.valueOf(MCID));
        }
        
        dictionaries.put(String.valueOf(markedContentLevel),BDCobj);
    	
        
        if(debug){
            System.out.println("BDC----------------------------------------------------------"+markedContentLevel+" MCID="+MCID);
            System.out.println("BDCobj="+BDCobj);
        }
        
        
    }


    public void BMC(String op) {

        op = setBMCvalues(op);

		if(buildDirectly){
			//read any dictionay work out type
			//PdfObject dict=(PdfObject) dictionaries.get(currentKey);
			//boolean isBMC=dict==null;

			//add node with name for BMC
			if(op!=null){
				//System.out.println(op+" "+root.getElementsByTagName(op));
				Element newRoot=(Element) root.getElementsByTagName(op).item(0);
				
				if(newRoot==null){
					newRoot=doc.createElement(op);
					root.appendChild(newRoot);
				}
				root=newRoot;
			}
        }
	}

    String setBMCvalues(String op) {
        
        //stip off /
        if(op.startsWith("/")) {
            op = op.substring(1);
        }
        
        //if start of sequence, reinitialise settings
        if (markedContentLevel == 0 && !isHTML) {
            markedContentSequence = new StringBuffer();
        }
        markedContentProperties.put(markedContentLevel,op);
        markedContentLevel++;
        if(debug) {
            System.out.println("BMC----------------------------------------------------------level=" + markedContentLevel + " raw op=" + op);
        }
        
        //save label and any dictionary
        keys.put(markedContentLevel,op);

        return op;
    }


    public void EMC() {

    	setEMCValues();
        
        if(buildDirectly){

			final PdfObject BDCobj=(PdfObject) dictionaries.get(currentKey);
			
			final boolean isBMC=(BDCobj==null);
			
			if(debug) {
                System.out.println(isBMC + " " + currentKey + ' ' + BDCobj + " markedContentSequence=" + markedContentSequence);
            }

            //any custom tags
            if(BDCobj!=null){
                final Map metadata=BDCobj.getOtherDictionaries();
                if(metadata!=null){
                    final Iterator customValues=metadata.keySet().iterator();
                    Object key;
                    while(customValues.hasNext()){
                        key=customValues.next();
                        root.setAttribute(key.toString(), metadata.get(key).toString());

                        //if(addCoordinates){
                            root.setAttribute("x1", String.valueOf((int) x1));
                            root.setAttribute("y1", String.valueOf((int) y1));
                            root.setAttribute("x2", String.valueOf((int) x2));
                            root.setAttribute("y2", String.valueOf((int) y2));
                        //}
                    }
                }
            }
            
			//add node with name for BMC
			if(isBMC){
				if(currentKey!=null){
					
					final Node child=doc.createTextNode(stripEscapeChars(markedContentSequence.toString()));
					
					root.appendChild(child);

					final Node oldRoot=root.getParentNode();
					if(oldRoot instanceof Element) {
                        root = (Element) oldRoot;
                    }
				}
			}else{
				//get root key on dictionary (should only be 1)
				//and create node
				//Iterator keys=dict.keySet().iterator();
				String S="p";//(String) keys.next();
				

                //System.out.println("dict="+BDCobj.getObjectRefAsString());
                
				if(S==null) {
                    S = "p";
                }

				final Element tag = doc.createElement(S);
				root.appendChild(tag);

				//add the text
				final Node child=doc.createTextNode(markedContentSequence.toString());
				tag.appendChild(child);
            }

			//reset
			markedContentSequence=new StringBuffer();


        }else{
        	
        	final String ContentSequence = markedContentSequence.toString();

        	//System.out.println(currentKey+" "+markedContentSequence);
            if(debug) {
                System.out.println("write out " + currentKey + " text=" + markedContentSequence + '<');
            }
            
            final PdfObject BDCobj=(PdfObject) (dictionaries.get(String.valueOf(markedContentLevel)));
            
           // System.out.println("BDCobj="+BDCobj+" currentKey="+currentKey);
            
           
            //reset on MCID tag
            int MCID=-1;
            if(BDCobj!=null) {
                MCID = BDCobj.getInt(PdfDictionary.MCID);
            }
            
            if(MCID!=-1){
            	values.put(String.valueOf(MCID),ContentSequence);
            	//System.out.println(MCID+" "+ContentSequence);
                markedContentSequence=new StringBuffer();
            }
            
            //remove used dictionary
            dictionaries.remove(String.valueOf(markedContentLevel));

        }

        if (markedContentLevel > 0) {
            markedContentLevel--;
        }

        if(debug) {
            System.out.println("EMC----------------------------------------------------------" + markedContentLevel);
        }

        
    }

    void setEMCValues() {
        
        //set flag to show some content
        contentExtracted=true;
        
        /**
         * add current structure to tree
         **/
        currentKey=(String)keys.get(markedContentLevel);
        
        //if no MCID use current level as key
        if(currentKey==null) {
            currentKey = String.valueOf(markedContentLevel);
        }
        
        if(debug) {
            System.out.println("currentKey=" + currentKey + ' ' + keys);
        }
    }

    /**store the actual text in the stream*/
    public void setText(final StringBuffer current_value, final float x1, final float y1, final float x2, final float y2) {

    	if(markedContentSequence.length()==0){
    		markedContentSequence=current_value;
    		
    		//lose space at start
    		if(markedContentSequence.length()>0 && markedContentSequence.charAt(0)==' ') {
                markedContentSequence.deleteCharAt(0);
            }
    		
    	}else{ //add space to tidy up
    		
    		//char c=' ',c2=' ';

    		//if(current_value.length()>0)
    		//	c=current_value.charAt(0);

    		//int len=markedContentSequence.length()-1;
    		//if(len>0)
    		//	c2=markedContentSequence.charAt(len);

    		//if(c2!='-' && c!='-' && c!='.')
    		//	markedContentSequence.append(' ');

    		//System.out.println("\nbit=>"+current_value+"<");
    		//System.out.println("whole=>"+markedContentSequence+"<");

    		markedContentSequence.append(current_value);

    	}
    	
    	this.x1=x1;
    	this.y1=y1;
    	this.x2=x2;
    	this.y2=y2;

    }


    
    
    //delete escape chars such as \( but allow for \\
	private static String stripEscapeChars(final Object dict) {
		char c,lastC=' ';
		
		final StringBuilder str=new StringBuilder((String) dict);
		int length=str.length();
		for(int ii=0;ii<length;ii++){
			c=str.charAt(ii);
			if(c=='\\' && lastC!='\\'){
				str.deleteCharAt(ii);
				length--;
			}
			lastC=c;
				
		}
		
		return str.toString();
		
	}

	public boolean hasContent() {
		return contentExtracted;
	}
}
