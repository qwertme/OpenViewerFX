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
 * Javascript.java
 * ---------------
 */
package org.jpedal.objects;

import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.javascript.*;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.AcroRenderer;

import java.util.*;

import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_String;



/**
 * general container for javascript
 * and store text commands
 */
public class Javascript {

    /**default to handle commands*/
    private ExpressionEngine jsParser;

	private static boolean useNewJSParser;
	private static boolean disableJavascript;

    public Javascript(final ExpressionEngine userExpressionEngine, final AcroRenderer acro, final Object swingGUI) {
		if(disableJavascript) {
            return;
        }
		if(System.getProperty("org.jpedal.newJS") != null) {
			useNewJSParser = true;
		}

        if(userExpressionEngine!=null){
            jsParser=userExpressionEngine;
        }else{
            try{
                //noinspection PointlessBooleanExpression
                if(!useNewJSParser) {
                    //
                    {  //just AF commands coded in Java
                        jsParser = new DefaultParser();
                    }
				}
				else {
                    final GenericParser genericParser = new GenericParser(this);
					jsParser = genericParser;
                    // <end-demo>
                    genericParser.setupPDFObjects(this);
				}

            }catch(final Error err){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Error: " + err.getMessage());
                }
                //
            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //

            }
        }

        /**
         * set here so if user passes in we still configure
         */
        jsParser.setAcroRenderer(acro);

    }

    private final Map javascriptCommands=new HashMap();
    private final Map javascriptTypesUsed=new HashMap();
    private final Map linkedjavascriptCommands=new HashMap();
    private final Map javascriptNamesObjects = new HashMap();

    /**
     * called to execute various action commands such as page opened
     * triggered by events not easily tracked with listeners
     */
    public  void executeAction(final String jsCode){
		if(disableJavascript) {
            return;
        }
    	jsParser.executeFunctions(jsCode,null);
    }

    /**
     * we execute the command given and then execute command C on any linked fields 
     * or on itself if there are no linked fields
     */
    public int execute(final FormObject ref, final int type, final int eventType, final char keyPressed) {

        final int returnCode=executeCommand(ref, type, eventType, keyPressed);
        
        boolean executeChangedCode = false;
    	if(eventType==ActionHandler.FOCUS_EVENT &&
    			(type != PdfDictionary.C2 ||
    				(type == PdfDictionary.C2 &&
    					(returnCode == ActionHandler.NOMESSAGE ||
    					returnCode == ActionHandler.VALUESCHANGED)))) {
            executeChangedCode = true;
        }
        
        if(executeChangedCode){
        	final String refName = ref.getTextStreamValue(PdfDictionary.T);
        	
            //C action requires us to execute other objects code
        	final Vector_String linkedObj= (Vector_String) linkedjavascriptCommands.get(refName);
        	
            if(linkedObj!=null){
            	linkedObj.trim();
            	final String[] value = linkedObj.get();

                for (final String nextVal : value) {
                    //if values.nexttoken is this field and type is C2 ignore as it will start a loop
                    if (nextVal.equals(refName) && type == PdfDictionary.C2) {
                    }

//                    Object tmp = renderer.getField(nextVal);
//                    if (tmp instanceof Object[]) {
//                        Object[] forms = (Object[]) tmp;
//                        for (Object form : forms) {
//                            if (((PdfProxy) form).getFormObject()[0].getTextStreamValue(PdfDictionary.T)
//                                    .equals(refName) && type == PdfDictionary.C2)
//                                continue;
//                            returnCode = execute(((PdfProxy) form).getFormObject()[0], PdfDictionary.C2, eventType, keyPressed);
//                        }
//                    } else {
//                        if (((PdfProxy) tmp).getFormObject()[0].getTextStreamValue(PdfDictionary.T)
//                                .equals(refName) && type == PdfDictionary.C2)
//                            continue;
//                        returnCode = execute(((PdfProxy) tmp).getFormObject()[0], PdfDictionary.C2, eventType, keyPressed);
//                    }
                }
            }
        }

        return returnCode;
    }
    
    public Object getJavascriptCommand(final String ref, final int type){
    	//get javascript
    	return javascriptCommands.get(ref+ '-' +type);
        
    }

    private int executeCommand(final FormObject ref, final int type, final int eventType, final char keyPressed) {
    	
        int message=ActionHandler.NOMESSAGE;

		if(disableJavascript) {
            return message;
        }
        
        if(ref==null) {
            return message;
        }
        
        //get javascript
        //we read the ref first,
        Object js= javascriptCommands.get(ref.getObjectRefAsString()+'-'+type);
        if(js==null){
        	//if this is null then the name is read to get JS for parent objects.
        	js= javascriptCommands.get(ref.getTextStreamValue(PdfDictionary.T)+'-'+type);
        }

        if(js==null) {
            return ActionHandler.NOMESSAGE;
        }

        if(message!=ActionHandler.STOPPROCESSING) {
            message = jsParser.execute(ref, type, js, eventType, keyPressed);
        }

        return message;
    }

    /**
     * store and execute code from Names object
     */
    public void setCode(final String name, final String value) {
		if(disableJavascript) {
            return;
        }

    	javascriptNamesObjects.put(name, value);

        jsParser.addCode(value);
        
    }

    /**
     * Returns the JavaScript from a JavaScript Name object <br>
     * If key is set to null it will return the whole contents of the map
     * @param key
     * @return JavaScript as a String
     */
    public String getJavaScript(final String key) {
    	String str;
    	if(key == null) {
	        final Collection c = javascriptNamesObjects.values();
	        
	        //obtain an Iterator for Collection
	        final Iterator itr = c.iterator();
	       
	        //iterate through HashMap values iterator
            StringBuilder s=new StringBuilder();
	        while(itr.hasNext()) {
	        	s.append(itr.next());
	        }
            str=s.toString();
    	}
    	else {
    		str = (String) javascriptNamesObjects.get(key);
    	}
          return str;
    }

    public void closeFile() {

        javascriptTypesUsed.clear();
        javascriptCommands.clear();
        linkedjavascriptCommands.clear();

		if(disableJavascript) {
            return;
        }
        jsParser.closeFile();

    }

    public void storeJavascript(final String name, final String script, final int type) {

		//adobe spec says explicitly:-This method will overwrite any action already defined for the chosen trigger.
        javascriptCommands.put(name+ '-' +type,script);

        javascriptTypesUsed.put(type,"x");//track types used so we can recall

        //log all values in "" as possible fields
        //(will include commands and spurious links as well)
        if(type==PdfDictionary.C2){

            int ptr=0,start;

            while(true){

                //get start " but ignore \"
                int escapedptr=script.indexOf("\\\"",ptr);
                while(true){
                    ptr=script.indexOf('\"',ptr);
                    if(ptr==-1 || escapedptr==-1 || ptr-1>escapedptr) {
                        break;
                    }

                }

                if(ptr==-1) {
                    break;
                }

                ptr++; //roll on
                start=ptr;

                //get end " but ignore \"
                escapedptr=script.indexOf("\\\"",ptr);
                while(true){
                    ptr=script.indexOf('\"',ptr);

                    if(ptr==-1 || escapedptr==-1 || ptr-1>escapedptr) {
                        break;
                    }

                }

                if(ptr==-1) {
                    break;
                }

                final String obj=script.substring(start,ptr);

                if(obj!=null){
                	Vector_String existingList=(Vector_String) linkedjavascriptCommands.get(obj);

                    if(existingList==null){
                        existingList=new Vector_String();
                        existingList.addElement(name);
                    }else{
                    	//add if not already in
                    	if(!existingList.contains(name)) {
                            existingList.addElement(name);
                        }
                    }

                    linkedjavascriptCommands.put(obj,existingList);
                }

                ptr++; //roll on

            }
        }
    }

    public void dispose(){
		if(disableJavascript) {
            return;
        }
    	jsParser.dispose();

    }

	/**
	 * Stop JavaScript from being run.
	 * Should be run before instancing a Javascript object.
	 */
	public static void disableJavascript() {
		disableJavascript = true;
	}

}
