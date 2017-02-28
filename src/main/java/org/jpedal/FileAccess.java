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
 * FileAccess.java
 * ---------------
 */
package org.jpedal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.stream.ImageInputStream;
import org.jpedal.constants.PDFflags;
import org.jpedal.display.Display;
import org.jpedal.display.PageOffsets;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.io.DecryptionFactory;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfFileReader;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.linear.LinearParser;
import org.jpedal.objects.Javascript;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.PdfResources;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.PageObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.*;
import org.jpedal.utils.LogWriter;

public class FileAccess {

    /**user can open encrypted file with certificate*/
    Certificate certificate;

    private PageOffsets currentOffset;
    
    private String password;

    /**the actual display object*/
    private DynamicVectorRenderer currentDisplay; //

    //flag to track if page decoded twice
    private int lastPageDecoded = -1;

    /**used for opening encrypted file*/
    PrivateKey key;

    /** holds page information used in grouping*/
    private PdfPageData pageData = new PdfPageData();

    /** the ObjectStore for this file */
    private ObjectStore objectStoreRef = new ObjectStore();

    public boolean isOpen;

    /**
     * the size above which objects stored on disk (-1 is off)
     */
    int minimumCacheSize = -1;//20000;

    @SuppressWarnings("UnusedDeclaration")
    private static final Calendar cal=Calendar.getInstance();

    @SuppressWarnings("WeakerAccess")
    static int bb=5;

    /**
     * sed to stop close on method
     */
    private boolean closeOnExit=true;

    private String filename;
     
    /**
     * provide access to pdf file objects
     */
    private PdfObjectReader currentPdfFile;

    /** count of how many pages loaded */
    private int pageCount;

    /**current page*/
    private int pageNumber=1;

    /**flag to stop multiple attempts to decode*/
    private boolean isDecoding;

    /**handlers file reading/decoding of linear PDF data*/
    final LinearParser linearParser=new LinearParser();

    final ExternalHandlers externalHandlers;

    //

    private final PdfResources res;

    private final DecoderOptions options;

    public FileAccess(final ExternalHandlers externalHandlers, final PdfResources res, final DecoderOptions options) {
        this.externalHandlers=externalHandlers;
        this.res=res;
        this.options=options;
        
    }
    
    public boolean isFileViewable(final PdfObjectReader currentPdfFile) {
        if (currentPdfFile != null){
            final PdfFileReader objectReader=currentPdfFile.getObjectReader();

            final DecryptionFactory decryption=objectReader.getDecryptionObject();
            return decryption==null || decryption.getBooleanValue(PDFflags.IS_FILE_VIEWABLE) || certificate!=null;
        }else {
            return false;
        }
    }
    
    void openPdfArray(final byte[] data, final String password) throws PdfException {

        this.password=password;
        
        openPdfArray(data);
        
    }

    void openPdfArray(final byte[] data) throws PdfException {

        if(data==null) {
            throw new RuntimeException("Attempting to open null byte stream");
        }

        if(isOpen) {
            //throw new RuntimeException("Previous file not closed");
            closePdfFile(); //also checks decoding done
        }
        
        isOpen = false;

        res.flush();
        res.flushObjects();

        try {

            if(password!=null){
                currentPdfFile = new PdfObjectReader(password);
            }else{
                currentPdfFile = new PdfObjectReader();
            }
        
            /** get reader object to open the file */
            currentPdfFile.openPdfFile(data);

            openPdfFile();

            /** store file name for use elsewhere as part of ref key without .pdf */
            objectStoreRef.storeFileName("r" + System.currentTimeMillis());

        } catch (final PdfException e) {
            //
            throw new PdfException("[PDF] OpenPdfArray generated exception "
                    + e.getMessage());
        }
    }

    /**
     * gets DynamicVector Object
     */
    DynamicVectorRenderer getDynamicRenderer() {
        return currentDisplay;
    }
    
    public PdfResources getRes() {
        return res;
    }
    
    /**
     * gets DynamicVector Object - NOT PART OF API and subject to change (DO NOT USE)
     */
    DynamicVectorRenderer getDynamicRenderer(final boolean reset) {

        final DynamicVectorRenderer latestVersion=currentDisplay;

        //
        
        return latestVersion;
    }

    /**
     * routine to open PDF file and extract key info from pdf file so we can
     * decode any pages which also sets password.
     * Does not actually decode the pages themselves. Also
     * reads the form data. You must explicitly close your stream!!
     */
    public final void openPdfFileFromStream(final Object filename, final String password) throws PdfException {

        //tell JPedal NOT to close stream!!!
        closeOnExit=false;

        if(filename instanceof ImageInputStream){

            final ImageInputStream iis=(ImageInputStream)filename;

            if(isOpen) {
                //throw new RuntimeException("Previous file not closed");
                closePdfFile(); //also checks decoding done
            }
            
            isOpen = false;

            this.filename = "ImageInputStream" + System.currentTimeMillis();


            // 

            res.flush();
            res.flushObjects();

            /** store file name for use elsewhere as part of ref key without .pdf */
            objectStoreRef.storeFileName(this.filename);

            currentPdfFile = new PdfObjectReader(password);

            /** get reader object to open the file */
            currentPdfFile.openPdfFile(iis);

            openPdfFile();

        }else{
            throw new RuntimeException(filename+" not currently an option");
        }

    }

    void openPdfFile(final String filename, final String password) throws PdfException {

        if(isOpen) {
            //throw new RuntimeException("Previous file not closed");
            closePdfFile(); //also checks decoding done
        }
        
        isOpen = false;

        // 

        this.filename = filename;
        res.flush();
        res.flushObjects();

        /** store file name for use elsewhere as part of ref key without .pdf */
        objectStoreRef.storeFileName(filename);

        currentPdfFile = new PdfObjectReader(password);

        /** get reader object to open the file */
        currentPdfFile.openPdfFile(filename);

        openPdfFile();

    }


    int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(final int newPage) {
        this.pageNumber=newPage;
    }


    /**
     * get page count of current PDF file
     */
    public int getPageCount() {
        return pageCount;
    }

    void setPageCount(final int newPageCount) {
        pageCount=newPageCount;
    }


    boolean isDecoding() {
        return isDecoding;
    }

    void setDecoding(final boolean b) {
        isDecoding=b;
    }

    public boolean isPasswordSupplied(final PdfObjectReader currentPdfFile) {
        //allow through if user has verified password or set certificate
        if (currentPdfFile != null){
            final PdfFileReader objectReader=currentPdfFile.getObjectReader();

            final DecryptionFactory decryption=objectReader.getDecryptionObject();
            return decryption!=null && (decryption.getBooleanValue(PDFflags.IS_PASSWORD_SUPPLIED) || certificate!=null);
        }else {
            return false;
        }
    }

    /**
     * return object which provides access to file images and name
     */
    public ObjectStore getObjectStore() {
        return objectStoreRef;
    }

    /**
     * return object which provides access to file images and name (use not
     * recommended)
     */
    void setObjectStore(final ObjectStore newStore) {
        objectStoreRef = newStore;
    }

    public void setUserEncryption(final Certificate certificate, final PrivateKey key) {
        
        this.certificate=certificate;
        this.key=key;
    }

    public PdfObjectReader getNewReader() {

        final PdfObjectReader currentPdfFile;

        if(certificate!=null){
            currentPdfFile = new PdfObjectReader(certificate, key);
        }else {
            currentPdfFile = new PdfObjectReader();
        }

        return currentPdfFile;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public PdfObjectReader getIO() {
        return currentPdfFile;
    }

    public void setIO(final PdfObjectReader pdfObjectReader) {
        currentPdfFile =pdfObjectReader;
    }

    /**
     * return true if the current pdf file is encrypted <br>
     * check <b>isFileViewable()</b>,<br>
     * <br>
     * if file is encrypted and not viewable - a user specified password is
     * needed.
     */
    public final boolean isEncrypted() {

        if (currentPdfFile != null) {
            final PdfFileReader objectReader= currentPdfFile.getObjectReader();
            final DecryptionFactory decryption=objectReader.getDecryptionObject();
            return decryption!=null && decryption.getBooleanValue(PDFflags.IS_FILE_ENCRYPTED);
        }else {
            return false;
        }
    }

    public void dispose() {

        //current=null;
        if(currentPdfFile!=null) {
            currentPdfFile.dispose();
        }
        
        currentPdfFile=null;

        if(currentDisplay!=null) {
            currentDisplay.dispose();
        }
        
        currentDisplay=null;
        
        bb += 1;
    }

    public String getFilename() {
        return filename;
    }

    float[] defaultMediaSize;
    float[] defaultCropSize;
    
    /**
     * read the data from pages lists and pages so we can open each page.
     *
     * object reference to first trailer
     */
    public int readAllPageReferences(final boolean ignoreRecursion, final PdfObject pdfObject , final Map rotations, final Map parents, int tempPageCount, final AcroRenderer formRenderer, final PdfResources res, final int insetW, final int insetH) {

        final String currentPageOffset=pdfObject.getObjectRefAsString();

        final boolean debug=false;

        int rotation=0;

        int type=pdfObject.getParameterConstant(PdfDictionary.Type);

        if(debug) {
            System.out.println("currentPageOffset="+currentPageOffset+" type="+type+ ' '+PdfDictionary.showAsConstant(type));
        }

        if(type== PdfDictionary.Unknown) {
            type= PdfDictionary.Pages;
        }


        /**
         * handle common values which can occur at page level or higher
         */

        /** page rotation for this or up tree*/
        int rawRotation=pdfObject.getInt(PdfDictionary.Rotate);
        String parent=pdfObject.getStringKey(PdfDictionary.Parent);

        if(rawRotation==-1 ){

            while(parent!=null && rawRotation==-1){

                if(parent!=null){
                    final Object savedRotation=rotations.get(parent);
                    if(savedRotation!=null) {
                        rawRotation= (Integer) savedRotation;
                    }
                }

                if(rawRotation==-1) {
                    parent=(String) parents.get(parent);
                }

            }

            //save
            if(rawRotation!=-1){
                rotations.put(currentPageOffset, rawRotation);
                parents.put(currentPageOffset,parent);
            }
        }else{ //save so we can lookup
            rotations.put(currentPageOffset, rawRotation);
            parents.put(currentPageOffset,parent);
        }

        if(rawRotation!=-1) {
            rotation=rawRotation;
        }

        final PdfPageData pageData= this.pageData;

        pageData.setPageRotation(rotation, tempPageCount);

        /**
         * handle media and crop box, defaulting to higher value if needed (ie
         * Page uses Pages and setting crop box
         */
        float[] mediaBox=pdfObject.getFloatArray(PdfDictionary.MediaBox);
        float[] cropBox=pdfObject.getFloatArray(PdfDictionary.CropBox);

        if(mediaBox==null) {
            mediaBox = defaultMediaSize;
        }
        
        if(cropBox==null) {
            cropBox = defaultCropSize;
        }
        
        if (mediaBox != null) {
            pageData.setMediaBox(mediaBox);
        }

        if (cropBox != null) {
            pageData.setCropBox(cropBox);
        }

        /** process page to read next level down */
        if (type==PdfDictionary.Pages) {

            if(pdfObject.getDictionary(PdfDictionary.Resources)!=null) {
                res.setPdfObject(PdfResources.GlobalResources, pdfObject.getDictionary(PdfDictionary.Resources));
            }

            final byte[][] kidList = pdfObject.getKeyArray(PdfDictionary.Kids);

            int kidCount=0;
            if(kidList!=null) {
                kidCount=kidList.length;
            }

            if(debug) {
                System.out.println("PAGES---------------------currentPageOffset="+currentPageOffset+" kidCount="+kidCount);
            }

            /** allow for empty value and put next pages in the queue */
            if (kidCount> 0) {

                if(debug) {
                    System.out.println("KIDS---------------------currentPageOffset="+currentPageOffset);
                }

                PdfObject nextObject;
                for(int ii=0;ii<kidCount;ii++){

                    nextObject=new PageObject(new String(kidList[ii]));
                    nextObject.ignoreRecursion(ignoreRecursion);
                    nextObject.ignoreStream(true);
                    
                    final float[] lastMediaBox = defaultMediaSize;
                    defaultMediaSize = mediaBox;
                    
                    final float[] lastCropBox = defaultCropSize;
                    defaultCropSize = cropBox;
                    currentPdfFile.readObject(nextObject);
                    tempPageCount=readAllPageReferences(ignoreRecursion, nextObject, rotations, parents,tempPageCount,formRenderer,res,  insetW, insetH);
                    defaultMediaSize=lastMediaBox;
                    defaultCropSize=lastCropBox;
                }

            }

        } else if (type==PdfDictionary.Page) {

            if(debug) {
                System.out.println("PAGE---------------------currentPageOffset="+currentPageOffset);
            }

            // store ref for later
            currentPdfFile.setLookup(currentPageOffset, tempPageCount);

            pageData.checkSizeSet(tempPageCount); // make sure we have min values

            /**
             * add Annotations
             */
            if (formRenderer != null) {

                // read the annotations reference for the page we have found lots of issues with annotations so trap errors
                byte[][] annotList = pdfObject.getKeyArray(PdfDictionary.Annots);

                //allow for empty
                if(annotList!=null && annotList.length==1 && annotList[0]==null) {
                    annotList=null;
                }

                if (annotList != null) {

                    // pass handle into renderer
                    formRenderer.resetAnnotData(insetW, insetH, this.pageData, tempPageCount, currentPdfFile, annotList);
                }
            }

            tempPageCount++;
        }

        return tempPageCount;
    }
    
    

    //<start-demo>
    /**
     //<end-demo>

     //


    /**
     * Provides method for outside class to get data
     * object containing information on the page for calculating grouping <br>
     * Please note: Structure of PdfPageData is not guaranteed to remain
     * constant. Please contact IDRsolutions for advice.
     *
     * @return PdfPageData object
     */
    PdfPageData getPdfPageData() {
        return pageData;
    }

    /**
     * used by remote printing to pass in page metrics
     *
     * @param pageData
     */
    void setPageData(final PdfPageData pageData) {
        this.pageData = pageData;
    }

    /**
     * wait for decoding to finish
     */
    public void waitForDecodingToFinish() {

        //wait to die
        while (isDecoding) {
            // System.out.println("Waiting to die");
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: "+e.getMessage());
                }
                //

                //ensure will exit loop
                isDecoding = false;
            }
        }
    }


    /**
     * common code for reading URL and InputStream
     * @param supportLinearized
     * @param is
     * @param rawFileName
     * @return
     * @throws PdfException
     */
    boolean readFile(final boolean supportLinearized, final InputStream is, final String rawFileName, final String password) throws PdfException {

        //make sure it will be deleted
        objectStoreRef.setFileToDeleteOnFlush(ObjectStore.temp_dir + rawFileName);
        objectStoreRef.setFileToDeleteOnFlush(rawFileName);

        res.flush();
        res.flushObjects();

        if(password==null){
            currentPdfFile = new PdfObjectReader();
        }else{
            currentPdfFile = new PdfObjectReader(password);
        }

        if(is!=null){
            try {

                final File tempURLFile;
                if(rawFileName.startsWith("inputstream")){
                    tempURLFile = new File(ObjectStore.temp_dir+rawFileName);
                    filename = tempURLFile.getAbsolutePath();
                }else {
                    tempURLFile = ObjectStore.createTempFile(rawFileName);
                }

                /** store fi name for use elsewhere as part of ref key without .pdf */
                objectStoreRef.storeFileName(tempURLFile.getName().substring(0, tempURLFile.getName().lastIndexOf('.')));

                if(supportLinearized){

                    final byte[] linearBytes=linearParser.readLinearData(currentPdfFile,tempURLFile,is, this);

                    if(linearBytes!=null){

                        /**
                         * read partial data so we can display
                         */

                        currentPdfFile.openPdfFile(linearBytes);

                        openPdfFile();

                        //force back if only 1 page
                        if (pageCount < 2) {
                            options.setDisplayView(Display.SINGLE_PAGE);
                        } else {
                            options.setDisplayView(options.getPageMode());
                        }

                        //read rest of file and reset
                        linearParser.linearizedBackgroundReaderer.start();

                        return true;
                    }

                }else{

                    currentPdfFile.openPdfFile(is);

                    openPdfFile();

                    //force back if only 1 page
                    if (pageCount < 2) {
                        options.setDisplayView(Display.SINGLE_PAGE);
                    } else {
                        options.setDisplayView(options.getPageMode());
                    }

                }

                if(supportLinearized){
                    //else{
                    //          System.out.println("xx");
                    /** get reader object to open the file */
                    openPdfFile(tempURLFile.getAbsolutePath());

                    /** store fi name for use elsewhere as part of ref key without .pdf */
                    objectStoreRef.storeFileName(tempURLFile.getName().substring(0, tempURLFile.getName().lastIndexOf('.')));
                    // }
                }

            } catch (final IOException e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("[PDF] Exception " + e + " opening URL ");
                }

                e.printStackTrace();
            }
        }
        return false;
    }


    public void closePdfFile() {

        waitForDecodingToFinish();


        //<start-demo>
        /**
         //<end-demo>
         //
         /**/

        if (!isOpen) {
            return;
        }

        isOpen = false;

        //flush linearization objects and
        //make sure we have stopped thread doing background linear reading
        linearParser.closePdfFile();

        final Javascript javascript=externalHandlers.getJavaScript();
        if (javascript != null){
            javascript.closeFile();
        }

        // pass handle into renderer
        final AcroRenderer formRenderer=externalHandlers.getFormRenderer();
        if (formRenderer != null) {
            formRenderer.openFile(pageCount,0,0, pageData, currentPdfFile, null);

            formRenderer.removeDisplayComponentsFromScreen();
        }
        objectStoreRef.flush();

        ObjectStore.flushPages();

        pageCount = 0;

        if (currentPdfFile != null && closeOnExit){
            currentPdfFile.closePdfFile();

            currentPdfFile = null;
        }
        
        lastPageDecoded = -1;

        currentDisplay.flush();

    }

    /**
     * allows user to cache large objects to disk to avoid memory issues,
     * setting minimum size in bytes (of uncompressed stream) above which object
     * will be stored on disk if possible (default is -1 bytes which is all
     * objects stored in memory) - Must be set before file opened.
     *
     */
    public void setStreamCacheSize(final int size) {
        this.minimumCacheSize = size;
    }
    
     int getLastPageDecoded() {
        return lastPageDecoded;
    }

    public void setLastPageDecoded(final int page) {
      lastPageDecoded = page;
    }

    public void setDVR(final DynamicVectorRenderer swingDisplay) {
        currentDisplay=swingDisplay;
    }

    public PageOffsets getOffset() {
        return currentOffset;
    }

    public void setOffset(final PageOffsets newOffset) {
        currentOffset=newOffset;
    }

    public void openPdfFile() throws PdfException {

        currentPdfFile.setJavaScriptObject(externalHandlers.getJavaScript());

        //<start-demo>
        /**
         //<end-demo>
         //
         /**/

        pageNumber = 1; // reset page number for metadata

        isDecoding = true;

        final AcroRenderer formRenderer=externalHandlers.getFormRenderer();

        try {
            // set cache size to use
            currentPdfFile.getObjectReader().setCacheSize(minimumCacheSize);

            // reset page data - needed to flush crop settings
            pageData = new PdfPageData();

            final PdfPageData pageData= this.pageData;

            // read and log the version number of pdf used
            final String pdfVersion = currentPdfFile.getObjectReader().getType();

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Pdf version : " + pdfVersion);
            }

            if (pdfVersion == null) {
                currentPdfFile = null;
                isDecoding = false;

                throw new PdfException( "No version on first line ");

            }

            // read reference table so we can find all objects and say if
            // encrypted
            PdfObject pdfObject;

            int linearPageCount=-1;

            //linear page object set differently
            if(linearParser.hasLinearData()){

                /**
                 * read and decode the hints table and the ref table
                 */
                pdfObject=linearParser.readHintTable(currentPdfFile);

                linearPageCount=linearParser.getPageCount();

            }else {
                pdfObject= currentPdfFile.getObjectReader().readReferenceTable(null);
            }

            //new load code - be more judicious in how far down tree we scan
            final boolean ignoreRecursion=true;

            // open if not encrypted or has password
            if (!isEncrypted() || isPasswordSupplied(currentPdfFile)) {

                if (pdfObject != null){
                    pdfObject.ignoreRecursion(ignoreRecursion);

                    res.setValues(pdfObject, currentPdfFile);

                    //check read as may be used for Dest
                    final PdfObject nameObj=pdfObject.getDictionary(PdfDictionary.Names);
                    if (nameObj != null){
                        currentPdfFile.readNames(nameObj, externalHandlers.getJavaScript(), false);
                    }
                }

                final int type=pdfObject.getParameterConstant(PdfDictionary.Type);
                if(type!=PdfDictionary.Page){

                    final PdfObject pageObj= pdfObject.getDictionary(PdfDictionary.Pages);
                    if(pageObj!=null){ //do this way incase in separate compressed stream

                        pdfObject=new PageObject(pageObj.getObjectRefAsString());
                        currentPdfFile.readObject(pdfObject);

                        // System.out.println("page="+pageObj+" "+pageObj.getObjectRefAsString());
                        //catch for odd files
                        if(pdfObject.getParameterConstant(PdfDictionary.Type)==-1) {
                            pdfObject=pageObj;
                        }                    
                    }
                }


                if (pdfObject != null) {

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Pages being read from "+pdfObject+ ' '+pdfObject.getObjectRefAsString());
                    }

                    pageNumber = 1; // reset page number for metadata

                    //flush annots before we reread

                    if(formRenderer!=null) {
                        formRenderer.resetAnnotData(options.getInsetW(), options.getInsetW(), pageData, 1, currentPdfFile,null);
                    }

                    //recursively read all pages
                    final int tempPageCount=readAllPageReferences(ignoreRecursion, pdfObject, new HashMap(1000), new HashMap(1000),1,formRenderer,res, options.getInsetW(), options.getInsetH());

                    //set PageCount if in Linearized data
                    if(linearPageCount>0){
                        pageCount = linearPageCount;
                    }else{
                        pageCount = tempPageCount - 1; // save page count
                    }
                    
                    //pageNumber = 0; // reset page number for metadata;
                    if (pageCount == 0 && LogWriter.isOutput()) {
                        LogWriter.writeLog("No pages found");
                    }
                }

                // pass handle into renderer
                if (formRenderer != null) {
                    pageCount=formRenderer.openFile(pageCount,options.getInsetW(), options.getInsetH(), pageData, currentPdfFile, res.getPdfObject(PdfResources.AcroFormObj));

                    //

                }
                
                pageData.setPageCount(pageCount);
            }

            currentOffset = null;

            isOpen = true;
        } catch (final PdfException e) {

            //ensure all data structures/handles flushed
            isDecoding = false;
            isOpen=true; //temporarily set to true as otherwise will not work
            closePdfFile();

            isOpen=false;

            throw new PdfException(e.getMessage() + " opening file");
        }finally{
            isDecoding = false;
        }
    }

    public void openPdfFile(final String filename) throws PdfException {

        isOpen = false;

        // 

        //System.out.println(filename);

        this.filename = filename;
        res.flush();
        res.flushObjects();
        //pagesReferences.clear();

        /** store file name for use elsewhere as part of ref key without .pdf */
        objectStoreRef.storeFileName(filename);

        /**
         * create Reader, passing in certificate if set
         */
        currentPdfFile = getNewReader();

        /** get reader object to open the file */
        currentPdfFile.openPdfFile(filename);

        /**test code in case we need to test byte[] version
         //get size
         try{
         File file=new File(filename);
         int length= (int) file.length();
         byte[] fileData=new byte[length];
         FileInputStream fis=new FileInputStream(filename);
         fis.read(fileData);
         fis.close();
         currentPdfFile.openPdfFile(fileData);
         }catch(Exception e){

         }/**/


        openPdfFile();

        //force back if only 1 page
        if (pageCount < 2) {
            options.setDisplayView(Display.SINGLE_PAGE);
        } else {
            options.setDisplayView(options.getPageMode());
        }
    }
}
