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
 * LinearParser.java
 * ---------------
 */
package org.jpedal.linear;

import org.jpedal.FileAccess;
import org.jpedal.exception.PdfException;
import org.jpedal.io.*;
import org.jpedal.objects.raw.LinearizedObject;
import org.jpedal.objects.raw.PageObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;
import org.jpedal.utils.repositories.FastByteArrayOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class LinearParser {
    
    /**flag if we have tested - reset for every file*/
    public boolean isLinearizationTested;
    
    private PageObject linObject;
    
    private final Map linObjects=new HashMap();
    
    private int linearPageCount=-1;
    
    /**present if file Linearized*/
    private PdfObject linearObj;
    
    /**
     * hold all data in Linearized Obj
     */
    private LinearizedHintTable linHintTable;
    
    private int E=-1;
    
    public org.jpedal.linear.LinearThread linearizedBackgroundReaderer;
    
    public void closePdfFile() {
        
        E=-1;
        linearObj=null;
        isLinearizationTested =false;
        linObjects.clear();
        if(linearizedBackgroundReaderer!=null && linearizedBackgroundReaderer.isAlive()){
            linearizedBackgroundReaderer.interrupt();
        }
        
        //wait to die
        while(linearizedBackgroundReaderer !=null && linearizedBackgroundReaderer.isAlive() && !linearizedBackgroundReaderer.isInterrupted()){
            try{
                Thread.sleep(500);
            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
        }
        
        linHintTable=null;
        
    }
    
    private void testForLinearlized(final byte[] buffer, final PdfObjectReader currentPdfFile) {
        int start=0,end=0;
        boolean isLinear=false;
        
        isLinearizationTested =true;
        
        //scan for Linearized in text
        final int len=buffer.length;
        for(int i=0;i<buffer.length;i++ ){
            
            if(start==0 && (i+2)<len && buffer[i]=='o' && buffer[i+1]=='b' && buffer[i+2]=='j'){
                start=i+3;
            }else if(end==0 && (i+5)<len && buffer[i]=='e' && buffer[i+1]=='n' && buffer[i+2]=='d' && buffer[i+3]=='o' && buffer[i+4]=='b' && buffer[i+5]=='j'){
                end=i+7;
            }else if(!isLinear && (i+6)<len && buffer[i]=='/' && buffer[i+1]=='L' && buffer[i+2]=='i' && buffer[i+3]=='n' && buffer[i+4]=='e' && buffer[i+5]=='a' && buffer[i+6]=='r'){
                isLinear=true;
            }
        }
        
        /**
         * read linear object
         */
        if(isLinear){
            final int dataLength=end-start;
            final byte[] data=new byte[dataLength+1];
            
            System.arraycopy(buffer,start,data,0,dataLength);
            linearObj=new LinearizedObject("1 0 R");
            linearObj.setStatus(PdfObject.UNDECODED_DIRECT);
            linearObj.setUnresolvedData(data, PdfDictionary.Linearized);
            
            currentPdfFile.checkResolved(linearObj);
        }else {
            linearObj = null;
        }
        
    }
    
    public boolean isPageAvailable(final int rawPage, final PdfObjectReader currentPdfFile) {
        
        boolean isPageAvailable=true;
        
        try{
            if(linearizedBackgroundReaderer !=null && linearizedBackgroundReaderer.isAlive() && rawPage>1 && linHintTable!=null){
                
                final Integer key= rawPage;
                
                //cached data
                if(linObjects.containsKey(key)){
                    linObject=(PageObject)linObjects.get(key);
                    
                    return true;
                }
                
                final int objID=linHintTable.getPageObjectRef(rawPage);
                
                //return if Page data not available
                final byte[] pageData=linHintTable.getObjData(objID);
                if(pageData!=null){
                    
                    /**
                     * turn page into obj
                     */
                    linObject=new PageObject(objID+" 0 R");
                    linObject.setStatus(PdfObject.UNDECODED_DIRECT);
                    linObject.setUnresolvedData(pageData, PdfDictionary.Page);
                    linObject.isDataExternal(true);
                    
                    final PdfFileReader objectReader=currentPdfFile.getObjectReader();

                    //see if object and all refs loaded otherwise exit
                    if(!ObjectDecoder.resolveFully(linObject, objectReader)) {
                        isPageAvailable = false;
                    } else{  //cache once available
                        
                        /**
                         * check content as well
                         */
                        if(linObject!=null){
                            
                            final byte[] b_data=currentPdfFile.getObjectReader().readPageIntoStream(linObject);
                            
                            if(b_data==null){
                                isPageAvailable=false;
                            }else{
                                //check Resources
                                final PdfObject Resources=linObject.getDictionary(PdfDictionary.Resources);
                                
                                if(Resources==null){
                                    linObject=null;
                                    isPageAvailable=false;
                                }else if(!ObjectDecoder.resolveFully(Resources, objectReader)){
                                    linObject=null;
                                    isPageAvailable=false;
                                }else{
                                    Resources.isDataExternal(true);
                                    new PdfStreamDecoder(currentPdfFile).readResources(Resources,true);
                                    if(!Resources.isFullyResolved()){
                                        linObject=null;
                                        isPageAvailable=false;
                                    }
                                }
                            }
                        }
                        
                        if(isPageAvailable && linObject!=null){
                            linObjects.put(key,linObject);
                        }
                    }
                }else {
                    isPageAvailable = false;
                }
            }else {
                linObject = null;
            }
            
        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
            
            isPageAvailable=false;
        }
        
        return isPageAvailable;
    }
    
    public byte[] readLinearData(final PdfObjectReader currentPdfFile, final File tempURLFile, final InputStream is, final FileAccess fileAccess) throws IOException {
        
        final FileChannel fos = new RandomAccessFile(tempURLFile,"rws").getChannel();
        fos.force(true);
        
        final FastByteArrayOutputStream bos=new FastByteArrayOutputStream(8192);
        
        // Download buffer
        final byte[] buffer = new byte[4096];
        int read,bytesRead=0;
        byte[] b;
        
        //main loop to read all the file bytes (carries on in thread if linearized)
        while ((read = is.read(buffer)) != -1) {
            
            if(read>0){
                synchronized (fos){
                    
                    b=new byte[read];
                    System.arraycopy(buffer,0,b,0,read);
                    final ByteBuffer f=ByteBuffer.wrap(b);
                    fos.write(f);
                }
            }
            
            bytesRead += read;
            
            //see if number of bytes loaded
            if(E!=-1){
                
                bos.write(buffer,0,read);
                
                //once correct number of bytes for Linearized object read, start background thread to read rest and process Linearized/page 1
                if(E<bytesRead){

                    final byte[] linearBytes=bos.toByteArray();
                    
                    //holds all data and copy of file for access
                    linHintTable=new LinearizedHintTable(fos);
                    currentPdfFile.getObjectReader().storeLinearizedTables(linHintTable);
                    
                    linearizedBackgroundReaderer = new LinearThread(is, fos, tempURLFile, linearObj,linearBytes, linHintTable,fileAccess);
                    
                    return linearBytes;
                    
                }
            }else if(!isLinearizationTested){  //test if linearized
                
                testForLinearlized(buffer, currentPdfFile);
                
                if(linearObj!=null){
                    E=linearObj.getInt(PdfDictionary.E);
                    bos.write(buffer,0,read);
                }
            }
        }
        
        // Close streams
        is.close();
        synchronized (fos){
            fos.close();
        }
        
        return null;
    }
    
    
    public PdfObject readHintTable(final PdfObjectReader currentPdfFile) throws PdfException {
        
        long Ooffset=-1;
        
        linearPageCount=-1;
        
        final int O=linearObj.getInt(PdfDictionary.O);
        
        //read in the pages from the catalog and set values
        final PdfObject pdfObject;
        if(O!=-1){
            linearObj.setIntNumber(PdfDictionary.O, -1);
            currentPdfFile.getObjectReader().readReferenceTable(linearObj);
            pdfObject =new PageObject(O,0);
            currentPdfFile.readObject(pdfObject);
            
            //get page count from linear data
            linearPageCount=linearObj.getInt(PdfDictionary.N);
            
            Ooffset = currentPdfFile.getObjectReader().getOffset(O);
            
        }else{ //use O as flag and reset
            pdfObject =currentPdfFile.getObjectReader().readReferenceTable(null);
        }
        
        /**
         * read and decode the hints table
         */
        final int[] H=linearObj.getIntArray(PdfDictionary.H);
        
        final byte[] hintStream=currentPdfFile.getObjectReader().getBytes(H[0], H[1]);
        
        //find <<
        final int length=hintStream.length;
        int startHint=0;
        int i=0;
        boolean contentIsDodgy=false;
        
        //number
        int keyStart2=i;
        while(hintStream[i]!=10 && hintStream[i]!=13 && hintStream[i]!=32 && hintStream[i]!=47 && hintStream[i]!=60 && hintStream[i]!=62){
            
            if(hintStream[i]<48 || hintStream[i]>57) //if its not a number value it looks suspicious
            {
                contentIsDodgy = true;
            }
            
            i++;
        }
        
        //trap for content not correct
        if(!contentIsDodgy){
            
            final int number= NumberUtils.parseInt(keyStart2, i, hintStream);
            
            //generation
            while(hintStream[i]==10 || hintStream[i]==13 || hintStream[i]==32 || hintStream[i]==47 || hintStream[i]==60) {
                i++;
            }
            
            keyStart2=i;
            //move cursor to end of reference
            while(i<10 && hintStream[i]!=10 && hintStream[i]!=13 && hintStream[i]!=32 && hintStream[i]!=47 && hintStream[i]!=60 && hintStream[i]!=62) {
                i++;
            }
            final int generation= NumberUtils.parseInt(keyStart2, i, hintStream);
            
            while(i<length-1){
                
                if(hintStream[i]=='<' && hintStream[i+1]=='<'){
                    startHint=i;
                    i=length;
                }
                
                i++;
            }
            
            final byte[] data=new byte[length-startHint];
            
            //convert the raw data into a PDF object
            System.arraycopy(hintStream,startHint,data,0,data.length);
            final LinearizedObject hintObj=new LinearizedObject(number,generation);
            hintObj.setStatus(PdfObject.UNDECODED_DIRECT);
            hintObj.setUnresolvedData(data, PdfDictionary.Linearized);
            currentPdfFile.checkResolved(hintObj);
            
            //get page content pointers
            linHintTable.readTable(hintObj,linearObj,O,Ooffset);
            
        }
        
        return pdfObject;
    }
    
    public int getPageCount() {
        return linearPageCount;
    }
    
    public boolean hasLinearData() {
        return linearObj!=null && E!=-1;
    }
    
    public PdfObject getLinearPageObject() {
        return linObject;
    }
    
    public PdfObject getLinearObject(final boolean isOpen, final PdfObjectReader currentPdfFile) {
        
        //lazy initialisation if not URLstream
        if(!isLinearizationTested && isOpen) {
                testForLinearlized(currentPdfFile.getObjectReader().getBytes(0, 400), currentPdfFile);
            }
        
        return linearObj;
    }
}
