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
 * PdfObjectReader.java
 * ---------------
 */
package org.jpedal.io;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.Javascript;
import org.jpedal.objects.PageLookup;
import org.jpedal.objects.raw.*;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 *  Provide access to data of PDF file on disk
 */
public class PdfObjectReader {
    
    private PdfFileReader objectReader=new PdfFileReader();
    
    /**
     * holds pdf id (ie 4 0 R) which stores each object
     */
    final Map pagesReferences = new HashMap();
    
    /**
     * page lookup table using objects as key
     */
    private PageLookup pageLookup = new PageLookup();

    private String tempFileName;
    
    /**names lookup table*/
    private NameLookup nameLookup;
    
    RandomAccessBuffer pdf_datafile;
    
    public PdfObjectReader() {}
    
    /**
     * set password as well
     * @param password
     */
    public PdfObjectReader(String password) {

        if(password==null) {
            password = "";
        }
        
        objectReader.setPassword(password);
    }
    
    public PdfObjectReader(final Certificate certificate, final PrivateKey key) {
        
        objectReader.setCertificate(certificate,key);
        
    }
    
    /**
     * reference for Page object
     * @param page
     * @return String ref (ie 1 0 R)
     * pdfObject=new PageObject(currentPageOffset);
     * currentPdfFile.readObject(pdfObject);
     */
    public String getReferenceforPage(final int page){
        return (String) pagesReferences.get(page);
    }
    
    /**
     * close the file
     */
    public final void closePdfFile()
    {
        try
        {
            objectReader.closeFile();
            
            if(pdf_datafile!=null) {
                pdf_datafile.close();
            }
            
            //ensure temp file deleted
            if(tempFileName!=null){
                final File fileToDelete=new File(tempFileName);
                fileToDelete.delete();
                tempFileName=null;
            }
        }catch( final Exception e ){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " closing file");
            }
        }
        
    }
    
    /**
     * allow user to access SOME PDF objects
     * currently PdfDictionary.Encryption
     */
    public PdfObject getPDFObject(final int key) {
        
        if(key==PdfDictionary.Encrypt){
            return objectReader.encyptionObj;
        }else {
            throw new RuntimeException("Access to " + key + " not supported");
        }
    }
    
    public PdfFileReader getObjectReader() {
        return objectReader;
    }
    
    /**
     * convert name into object ref
     */
    public String convertNameToRef(final String value) {
        
        //see if decoded
        if(nameLookup==null) {
            return null;
        } else {
            return (String) nameLookup.get(value);
        }
        
    }
    
    /**
     * get Names lookup table
     */
    public NameLookup getNamesLookup() {
        
        //see if decoded
        if(nameLookup==null) {
            return null;
        } else {
            return nameLookup;
        }
        
    }
    
    ///////////////////////////////////////////////////////////////////////////

    /**
     * read any names into names lookup
     */
    public void readNames(final PdfObject nameObject, final Javascript javascript, final boolean isKid){
        
        nameLookup=new NameLookup(this.objectReader);
        nameLookup.readNames(nameObject, javascript, isKid);
    }
    
    /**
     * given a ref, what is the page
     * @param ref - PDF object reference
     * @return - page number with  being first page
     */
    public int convertObjectToPageNumber(final String ref) {
        
        return pageLookup.convertObjectToPageNumber(ref);
    }
    
    public void setLookup(final String currentPageOffset, final int tempPageCount) {
        pageLookup.put(currentPageOffset, tempPageCount);
        pagesReferences.put(tempPageCount, currentPageOffset);
    }
    
    public void dispose(){
        
        //this.objData=null;
        //this.lastRef=null;
        
        nameLookup=null;
        
        //this.fields=null;
        
        if(objectReader!=null) {
            objectReader.dispose();
        }
        objectReader=null;
        
        if(pageLookup!=null) {
            pageLookup.dispose();
        }
        pageLookup=null;
        
    }
    
    /**
     * open pdf file<br> Only files allowed (not http)
     * so we can handle Random Access of pdf
     */
    public final void openPdfFile( final InputStream in) throws PdfException
    {
        
        try
        {
            
            //use byte[] directly if small otherwise use Memory Map
            pdf_datafile = new RandomAccessMemoryMapBuffer(in );
            
            objectReader.init(pdf_datafile);
            
            //this.eof = pdf_datafile.length();
            //pdf_datafile = new RandomAccessFile( filename, "r" );
            
        }catch( final Exception e ){
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " accessing file");
            }
            
            throw new PdfException( "Exception " + e + " accessing file" );
        }
        
    }
    
    /**
     * open pdf file<br> Only files allowed (not http)
     * so we can handle Random Access of pdf
     */
    public final void openPdfFile(final ImageInputStream iis ) throws PdfException
    {
        
        final RandomAccessBuffer pdf_datafile;
        
        try
        {
            
            //use byte[] directly if small otherwise use Memory Map
            pdf_datafile = new ImageInputStreamFileBuffer(iis);
            
            //pdf_datafile = new RandomAccessFileBuffer( filename, "r" );
            //pdf_datafile = new RandomAccessFCTest( new FileInputStream(filename));
            
            objectReader.init(pdf_datafile);
            
            //this.eof = pdf_datafile.length();
            
        }catch( final Exception e ){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " accessing file");
            }
            
            throw new PdfException( "Exception " + e + " accessing file" );
        }
        
    }
    
    public void checkParentForResources(final PdfObject pdfObject) {
        
        /**
         * if no resource, check parent for one
         * (in theory should recurse up whole tree)
         */
        if(pdfObject.getDictionary(PdfDictionary.Resources)==null){
            
            final String parent=pdfObject.getStringKey(PdfDictionary.Parent);
            
            if(parent!=null){
                final PdfObject parentObj=new PageObject(parent);
                readObject(parentObj);
                
                final PdfObject resObj=parentObj.getDictionary(PdfDictionary.Resources);
                
                if(resObj!=null){
                    pdfObject.setDictionary(PdfDictionary.Resources,resObj);
                }
            }
        }
    }
    
    /**
     * open pdf file<br> Only files allowed (not http)
     * so we can handle Random Access of pdf
     */
    public final void openPdfFile( final String filename ) throws PdfException
    {
        
        final RandomAccessBuffer pdf_datafile;
        
        try
        {
            
            pdf_datafile = new RandomAccessFileBuffer( filename, "r" );
            //pdf_datafile = new RandomAccessFCTest( new FileInputStream(filename));
            
            objectReader.init(pdf_datafile);
            
            //this.eof = pdf_datafile.length();
            
        }catch( final Exception e ){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " accessing file");
            }
            
            throw new PdfException( "Exception " + e + " accessing file" );
        }
        
    }
    
    /**
     * open pdf file using a byte stream - By default files under 16384 bytes are cached to disk
     * but this can be altered by setting PdfFileReader.alwaysCacheInMemory to a maximimum size or -1 (always keep in memory)
     */
    public final void openPdfFile( final byte[] data ) throws PdfException
    {
        
        final RandomAccessBuffer pdf_datafile;
        
        try
        {
            //use byte[] directly if small otherwise use Memory Map
            if(PdfFileReader.alwaysCacheInMemory ==-1 || data.length<PdfFileReader.alwaysCacheInMemory) {
                pdf_datafile = new RandomAccessDataBuffer(data);
            } else{ //cache as file and access via RandomAccess
                
                //pdf_datafile = new RandomAccessMemoryMapBuffer( data ); old version very slow
                
                try {
                    
                    final File file=File.createTempFile("page",".bin", new File(ObjectStore.temp_dir));
                    tempFileName=file.getAbsolutePath();
                    
                    //file.deleteOnExit();
                    
                    final java.io.FileOutputStream a =new java.io.FileOutputStream(file);
                    
                    a.write(data);
                    a.flush();
                    a.close();
                    
                    pdf_datafile = new RandomAccessFileBuffer( tempFileName,"r");
                } catch (final Exception e) {
                    throw new RuntimeException("Unable to create temporary file in "+ObjectStore.temp_dir+ ' ' +e);
                }
            }
            
            objectReader.init(pdf_datafile);
            
            //eof = pdf_datafile.length();
            //pdf_datafile = new RandomAccessFile( filename, "r" );
            
        }catch( final Exception e ){
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " accessing file");
            }
            
            throw new PdfException( "Exception " + e + " accessing file" );
        }
    }
    
    
    
    /**handle onto JS object*/
    private Javascript javascript;
    
    /**pass in Javascript object from JPedal*/
    public void setJavaScriptObject(final Javascript javascript) {
        this.javascript=javascript;
    }
    
    public void checkResolved(final PdfObject pdfObject) {
        final ObjectDecoder objectDecoder=new ObjectDecoder(this.objectReader);
        objectDecoder.checkResolved(pdfObject);
    }
    
    public void setJavascriptForObject(final FormObject formObject, final int parentType, final int actionType) {
        final String JSscript;
        
        final PdfObject actionObj ;
        final PdfObject JSobj;
        
        final PdfObject additionalObject=formObject.getDictionary(parentType);
        
        final ObjectDecoder objectDecoder=new ObjectDecoder(this.objectReader);
        objectDecoder.checkResolved(additionalObject);
        
        if(additionalObject==null) {
            return;
        }
        
        if(actionType==parentType){
            actionObj = additionalObject;
        }else {
            if(actionType==PdfDictionary.C2) //special case
            {
                actionObj = additionalObject.getDictionary(PdfDictionary.C);
            } else {
                actionObj = additionalObject.getDictionary(actionType);
            }
        }
        
        if(actionObj==null){
            //				throw new RuntimeException("Failed on actionType="+actionType+" "+formObject.getPDFRef()+
            //						"\nformObject="+formObject+" parentObject="+additionalObject+
            //						"\nadditionalObject="+additionalObject.getTextStreamValue(PdfDictionary.T)
            //						+"\nadditionalObject="+formObject.getTextStreamValue(PdfDictionary.T));
        }else{
            
            objectDecoder.checkResolved(actionObj);
            
            JSobj=actionObj.getDictionary(PdfDictionary.JS);
            
            if(JSobj!=null){
                
                final byte[] data=JSobj.getDecodedStream();
                JSscript= StringUtils.getTextString(data, true);
                
            }else{
                JSscript=actionObj.getTextStreamValue(PdfDictionary.JS);
            }
            
            //store
            if(JSscript!=null){
                //use name to reference Js if name is null use ref. seems to be slower, but better on abacus/L295KantoonVaadt.pdf
                String name = formObject.getTextStreamValue(PdfDictionary.T);
                if(name==null) {
                    name = formObject.getObjectRefAsString();
                }
                javascript.storeJavascript(name,JSscript,actionType);
                
                //old version
                //                javascript.storeJavascript(formObject.getObjectRefAsString(),JSscript,actionType);
            }
        }
    }
    
    public byte[] readStream(final PdfObject obj, final boolean cacheValue, final boolean decompress, final boolean keepRaw, final boolean isMetaData, final boolean isCompressedStream, final String cacheFile) {
        
        return this.objectReader.readStream(obj, cacheValue, decompress, keepRaw, isMetaData, isCompressedStream, cacheFile);
    }
    
    public void readObject(final PdfObject pdfObject) {
        objectReader.readObject(pdfObject);
    }
    
    /**
     * return type of encryption as Enum in EncryptionUsed
     */
    public EncryptionUsed getEncryptionType() {
        
        final PdfFileReader objectReader= this.objectReader;
        final DecryptionFactory decryption=objectReader.getDecryptionObject();
        
        if(decryption==null){
            return EncryptionUsed.NO_ENCRYPTION;
        }else if(decryption.hasPassword()){
            return EncryptionUsed.PASSWORD;
        }else  //cert by process of elimination
        {
            return EncryptionUsed.CERTIFICATE;
        }
        
    }
    
}