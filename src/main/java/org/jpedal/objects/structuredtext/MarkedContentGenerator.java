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
 * MarkedContentGenerator.java
 * ---------------
 */
package org.jpedal.objects.structuredtext;

import java.util.Arrays;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.PdfResources;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.PageObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.MCObject;

import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.parser.ValueTypes;
import org.jpedal.utils.LogWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.Map;
import org.jpedal.PdfDecoderInt;
import org.jpedal.render.SwingDisplay;

/**
 * extract as marked content
 */
public class MarkedContentGenerator {
    
    private PdfObjectReader currentPdfFile;
    
    private DocumentBuilder db;
    
    private Document doc;
    
    private Element root;
    
    private final Map pageStreams=new HashMap();
    
    private PdfResources res;
    
    private PdfLayerList layers;
    
    private PdfPageData pdfPageData;
    
    private boolean isDecoding;
    
    static boolean debug;
    
    // Stops "No structured content in file" message being output multiple times
    static boolean displayNoStructMsg = true;
    
    //used to indent debug output
    static String indent="";
    
    final Map reverseLookup=new HashMap();
    
    boolean isHTML;
    
    /**
     * main entry paint
     */
    public Document getMarkedContentTree(final PdfResources res, final PdfPageData pdfPageData, final PdfObjectReader currentPdfFile) {
        
        PdfObject structTreeRootObj=res.getPdfObject(PdfResources.StructTreeRootObj);
        //PdfObject markInfoObj=res.getPdfObject(PdfResources.MarkInfoObj);  //not used at present
        
        this.res=res;
        this.layers=res.getPdfLayerList();
        
        this.pdfPageData=pdfPageData;
        
        this.currentPdfFile=currentPdfFile;
        
        //read values as needed
        this.currentPdfFile.checkResolved(structTreeRootObj);
        
        /**
         * create the empty XMLtree and root to add data onto
         **/
        if(!isHTML){
            setupTree();
            
            final boolean hasTree=structTreeRootObj!=null && structTreeRootObj.getDictionary(PdfDictionary.ParentTree)!=null;
            
            if(debug) {
                System.out.println("hastree=" + hasTree);
            }
            
            //choose appropriate method
            if(hasTree){
                
                /**
                 * scan PDF and add nodes to XML tree
                 */
                buildTree(structTreeRootObj);
                
                //flush all objects
                pageStreams.clear();
                
            }else{ //from the page stream
                
                try {
                    decodePageForMarkedContent(1, null, doc);
                } catch (final Exception e) {
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        }
        
        return doc;
        
    }
    
    /**
     * create a blank XML structure and a root. Add comment to say created by JPedal
     */
    private void setupTree() {
        
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        doc =  db.newDocument();
        
        /**add creator comment*/
        doc.appendChild(doc.createComment(" Created from JPedal "));
        doc.appendChild(doc.createComment(" http://www.idrsolutions.com "));
        
    }
    
    /**
     * scan down PDF struct object, creating XML tree
     */
    private void buildTree(PdfObject structTreeRootObj) {
        
        /**
         * create root and attach
         **/
        root = doc.createElement("TaggedPDF-doc");
        doc.appendChild(root);
        
        traverseContentTree(structTreeRootObj,null);
    }

    public void traverseContentTree(PdfObject structTreeRootObj, PdfStreamDecoder current) {
        /**
         * read struct K value and decide what type
         * (can be dictionary or Array so we check both options)
         */
        final PdfObject K =structTreeRootObj.getDictionary(PdfDictionary.K);
        if(K ==null){
            final byte[][] Karray=structTreeRootObj.getStringArray(PdfDictionary.K);
            
            if(debug) {
                System.out.println("Karray=");
            }
            
        
            readKarray(Karray, root,null,null,"");
            
            if(debug) {
                System.out.println("Karray read");
            }
            
        }else{
            
            if(debug) {
                System.out.println("read child=" + K.getObjectRefAsString());
            }
            
            readChildNode(K, root,null,"");
        }
    }
    
    private void readChildNode(final PdfObject K, final Element root,Map pageStream, String fullS) {
        
        if(debug){
            indent += "   ";
                System.out.println(indent+"read child node "+K.getObjectRefAsString()+ ' ' +K.getInt(PdfDictionary.K));
        }
        
        final PdfObject Pg;
        final byte[][] Karray = K.getStringArray(PdfDictionary.K);
        final int Kint = K.getInt(PdfDictionary.K);
        
        final PdfObject Kdict = K.getDictionary(PdfDictionary.K);
        
        final String lang = K.getTextStreamValue(PdfDictionary.Lang);
        final String S = K.getName(PdfDictionary.S);
        
        fullS=fullS+ '.' +S;
        
        Element child=null;
        
        if(debug){
                System.out.println(indent+"S= "+S+ ' ');
        
                if(S==null){
                    System.out.println("S is null in "+K.getObjectRefAsString());
                
                }
        }
        
        
        //add child but collapse /Span into main Tag
        if(S!=null){
            if (S.equals("Span")) {
                child = root;
            } else {
                if(doc!=null){
                    child = doc.createElement(cleanName(S));
                }
                if (lang != null) {
                    child.setAttribute("xml:lang", lang);
                }

                if(root!=null){
                    root.appendChild(child);
                }
            }
        }
        
        //get page object
        Pg=K.getDictionary(PdfDictionary.Pg);
        
        if(Pg!=null && pageStream==null && !isHTML){
            
            //if not yet decoded, get values from it see if cached and decode if not
            //pageStream=(Map)pageStreams.get(Pg);
            
           
        
                if(debug) {
                    System.out.println(indent + "decode page ");
                }
        
        
                pageStream=new HashMap();
                try {
                    decodePageForMarkedContent(-1, Pg,pageStream); //-1 deliberate bum value as should not be used

                    //20130717 - disabled by Mark for memory issues 
                    //ie Postgres_Plus_Cloud_Database_Getting_Started_Guide_20130219.pdf 
                    //  pageStreams.put(Pg,pageStream);
                    
                } catch (final Exception e) {
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        
        
        if(debug) {
            System.out.println(indent + "page decoded karray" + Arrays.toString(Karray) + " Kdict=" + Kdict + " kint=" + Kint);
        }
        
        
        if (Karray != null) {
            readKarray(Karray,child,pageStream,S,fullS);
        }else if(Kdict!=null){
            readChildNode(Kdict, child,pageStream,fullS);
        } else if (Kint != -1 && !isHTML) { // actual value
            
            //reached the bottom so allow recursion to unwind naturally
            addContentToNode(pageStream, String.valueOf(Kint), child,S,fullS);
        } else if(K.getTextStreamValue(PdfDictionary.T)!=null){
            //System.out.println("ANnot");
        } else if(debug){
            System.out.println("unimplemented "+K.getObjectRefAsString());
        }
        
        if(debug){
                System.out.println(indent+"child node read "+K.getObjectRefAsString());
                
                indent=indent.substring(0,indent.length()-3);
        }
    }
    
    private void addContentToNode(final Map pageStream, final String Kint, final Element child, String S, String fullS) {
        
        if(!isHTML){
            
            String text = (String) pageStream.get(Kint);
            
            if (text != null) {
                text = handleXMLCharacters(text);
                
                if(doc!=null){
                    final Text textNode = doc.createTextNode(text);
                    child.appendChild(textNode);
                }
            }
            
            if(debug) {
                System.out.println(indent + " added " + text);
            }
        }
    }
    
    private static String handleXMLCharacters(String text) {
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        
        return text;
    }
    
    private void readKarray(final byte[][] Karray, final Element root, final Map pageStream,final String S,String fullS) {
        
        final int count=Karray.length;
        PdfObject kidObj;
        String KValue;
        byte[] lastChar;
        
        for(int i=0;i<count;i++){
            
            KValue=new String(Karray[i]);
            
            if(debug) {
                System.out.println(indent + "aK value=" + KValue);
            }
            
            if(count-i>=3){ //it is probably a ref
                
                lastChar=Karray[i+2];
                
                if(lastChar[0]=='R'){ //it is a ref
                    
                    kidObj = new MCObject(KValue+ ' ' +new String(Karray[i+1])+" R");
                    
                    currentPdfFile.readObject(kidObj);
                    
                    readChildNode(kidObj, root, pageStream,fullS);
                    i += 2; //allow for 3 values read in loop
                    
                }else{
                    addContentToNode(pageStream, KValue, root,S,fullS);
                }
            }else{
               
                if(isHTML){
                    if(!reverseLookup.containsKey(KValue)){
                        reverseLookup.put(KValue,fullS);
                    }
                }
                
                addContentToNode(pageStream, KValue, root,S,fullS);
            }
        }
    }
    
    private static String cleanName(String s) {
        //make sure S is valid XML
        
        final StringBuilder cleanedS = new StringBuilder(10);
        
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            
            //translate any hex values ( #xx ) into chars
            if (c == '#') { //assume 2 byte
                final StringBuilder num = new StringBuilder(2);
                for (int j = 0; j < 2; j++) {//read number
                    i++;
                    num.append(s.charAt(i));
                }
                //Convert from hex value as string into ASCI char
                c = (char) Integer.parseInt(num.toString(), 16);
                
                //hard-coded based on Adobe output
                if (!Character.isLetterOrDigit(c)) {
                    c = '-';
                }
                
            }
            
            //remap spaces
            if (c == ' ') {
                cleanedS.append('-');
            } else if (c == '-') // hard-coded
            {
                cleanedS.append(c);
            } else if (c == '_') {
                cleanedS.append(c);
            } else if (Character.isLetterOrDigit(c)) //reject non-valid
            {
                cleanedS.append(c);
            }
        }
        
        s = cleanedS.toString();
        return s;
    }
    
    /**
     * extract marked content
     */
    private synchronized void decodePageForMarkedContent(int pageNumber, PdfObject pdfObject, final Object pageStream) throws Exception {
       
        if (isDecoding) {
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("[PDF]WARNING - this file is being decoded already");
            }
            
        } else {
            
            //if no tree use page
            if(pdfObject==null){
                final String currentPageOffset = currentPdfFile.getReferenceforPage(pageNumber);
                
                pdfObject=new PageObject(currentPageOffset);
                currentPdfFile.readObject(pdfObject);
                
            }else{
                pageNumber=currentPdfFile.convertObjectToPageNumber(new String(pdfObject.getUnresolvedData()));
                currentPdfFile.checkResolved(pdfObject);
            }
            
            try{
                isDecoding=true;
                    
                /** the ObjectStore for this file */
                final ObjectStore objectStoreRef = new ObjectStore();

                final PdfStreamDecoder current = new PdfStreamDecoder(currentPdfFile, false, layers);
                current.setParameters(true, false, 0,PdfDecoderInt.TEXT + PdfDecoderInt.RAWIMAGES + PdfDecoderInt.FINALIMAGES,false,false);
                current.setXMLExtraction(false);
                current.setObjectValue(ValueTypes.Name, "markedContent");
                current.setObjectValue(ValueTypes.ObjectStore,objectStoreRef);
                current.setObjectValue(ValueTypes.StatusBar, null);
                current.setObjectValue(ValueTypes.PDFPageData,pdfPageData);
                current.setIntValue(ValueTypes.PageNum, pageNumber);
                current.setRenderer(new SwingDisplay(pageNumber,objectStoreRef,false));
                
                res.setupResources(current, false, pdfObject.getDictionary(PdfDictionary.Resources), pageNumber, currentPdfFile);

                current.setObjectValue(ValueTypes.MarkedContent,pageStream);

                if(debug) {
                    System.out.println(indent + " about to decode page " + pdfObject.getObjectRefAsString());
                }

                current.decodePageContent(pdfObject);

                objectStoreRef.flush();

                //
               
            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }finally {
                isDecoding=false;
            }
        }
    }
}
