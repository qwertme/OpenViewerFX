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
 * RefTable.java
 * ---------------
 */
package org.jpedal.io.types;

import java.io.IOException;
import org.jpedal.exception.PdfException;
import org.jpedal.io.*;
import org.jpedal.objects.raw.*;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;

/**
 *
 */
public class RefTable {
    
    
    PdfObject encryptObj;
    
    /**holds file ID*/
    private byte[] ID;
    
    /**pattern to look for in objects*/
    static final String pattern= "obj";
    
    /**info object*/
    private PdfObject infoObject;
    
    static final int UNSET=-1;
    static final int COMPRESSED=1;
    static final int LEGACY=2;
    
    private RandomAccessBuffer pdf_datafile;
    
    static final byte[] oldPattern = {'x','r','e','f'};
    
    private final long eof;
    
    final Offsets offset;
    
    public RefTable(final RandomAccessBuffer pdf_datafile, final long eof, final Offsets offset) {
        this.pdf_datafile=pdf_datafile;
        this.eof=eof;
        this.offset=offset;
    }
    
    /**
     * read first start ref from last 1024 bytes
     */
    private int readFirstStartRef() throws PdfException {
        
        //reset flag
        offset.setRefTableInvalid(false);
        
        
        int pointer = -1;
        int i = 1019;
        final StringBuilder startRef = new StringBuilder(10);
        
        /**move to end of file and read last 1024 bytes*/
        final int block=1024;
        byte[] lastBytes = new byte[block];
        long end;
        
        /**
         * set endpoint, losing null chars and anything before EOF
         */
        final int[] EndOfFileMarker={37,37,69,79};
        int valReached=3;
        boolean EOFFound=false;
        try {
            end=eof;
            
            /**
             * lose nulls and other trash from end of file
             */
            final int bufSize=255;
            while(true){
                final byte[] buffer=getBytes(end - bufSize, bufSize);
                
                int offset=0;
                
                for(int ii=bufSize-1;ii>-1;ii--){
                    
                    //see if we can decrement EOF tracker or restart check
                    if(!EOFFound) {
                        valReached = 3;
                    }
                    
                    if(buffer[ii]==EndOfFileMarker[valReached]){
                        valReached--;
                        EOFFound=true;
                    }else {
                        EOFFound = false;
                    }
                    
                    //move to next byte
                    offset--;
                    
                    if(valReached<0) {
                        ii = -1;
                    }
                    
                }
                
                //exit if found values on loop
                if(valReached<0){
                    end -= offset;
                    break;
                }else{
                    end -= bufSize;
                }
                
                //allow for no eof
                if(end<0){
                    end=eof;
                    break;
                }
            }
            
            //end=end+bufSize;
            
            //allow for very small file
            int count=(int)(end - block);
            
            if(count<0){
                count=0;
                final int size=(int)eof;
                lastBytes=new byte[size];
                i=size+3; //force reset below
            }
            
            lastBytes=getBytes(count, lastBytes.length);
            
        } catch (final Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " reading last 1024 bytes");
            }
            
            throw new PdfException( e + " reading last 1024 bytes");
        }
        
        //		for(int ii=0;ii<lastBytes.length;ii++){
        //		System.out.print((char)lastBytes[ii]);
        //		}
        //		System.out.println();
        
        //look for tref as end of startxref
        final int fileSize=lastBytes.length;
        
        if(i>fileSize) {
            i = fileSize - 5;
        }
        
        while (i >-1) {
            
            //first check is because startref works as well a startxref !!
            if (((lastBytes[i] == 116 && lastBytes[i + 1] == 120) || (lastBytes[i] == 114 && lastBytes[i + 1] == 116))
                    && (lastBytes[i + 2] == 114)
                    && (lastBytes[i + 3] == 101)
                    && (lastBytes[i + 4] == 102)) {
                break;
            }
            
            
            i--;
            
        }
        
        /**trap buggy files*/
        if(i==-1){
            try {
                closeFile();
            } catch (final IOException e1) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e1 + " closing file");
                }
            }
            throw new PdfException( "No Startxref found in last 1024 bytes ");
        }
        
        i += 5; //allow for word length
        
        //move to start of value ignoring spaces or returns
        while (i < 1024 && (lastBytes[i] == 10 || lastBytes[i] == 32 || lastBytes[i] == 13)) {
            i++;
        }
        
        //move to start of value ignoring spaces or returns
        while ((i < 1024)
                && (lastBytes[i] != 10)
                && (lastBytes[i] != 32)
                && (lastBytes[i] != 13)) {
            startRef.append((char) lastBytes[i]);
            i++;
        }
        
        /**convert xref to string to get pointer*/
        if (startRef.length() > 0) {
            pointer = Integer.parseInt(startRef.toString());
        }
        
        if (pointer == -1){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("No Startref found in last 1024 bytes ");
            }
            try {
                closeFile();
            } catch (final IOException e1) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e1 + " closing file");
                }
            }
            throw new PdfException( "No Startref found in last 1024 bytes ");
        }
        
        return pointer;
    }
    
    
    
    /**
     * read reference table start to see if new 1.5 type or traditional xref
     * @throws PdfException
     */
    public final PdfObject readReferenceTable(final PdfObject linearObj, final PdfFileReader currentPdfFile, final ObjectReader objectReader) throws PdfException {
        
        int pointer = -1;
        final int eof = (int) this.eof;

        boolean islinearizedCompressed = false;
        
        if (linearObj == null) {
            pointer = readFirstStartRef();
        } else { //find at start of Linearized
            final byte[] data = pdf_datafile.getPdfBuffer();
            
            
            final int count = data.length;
            int ptr = 5;
            for (int i = 0; i < count; i++) {
                
                //track start of this object (needed for compressed)
                if (data[i] == 'e' && data[i + 1] == 'n' && data[i + 2] == 'd' && data[i + 3] == 'o' && data[i + 4] == 'b' && data[i + 5] == 'j') {
                    ptr = i + 6;
                    
                }
                
                if (data[i] == 'x' && data[i + 1] == 'r' && data[i + 2] == 'e' && data[i + 3] == 'f') {
                    pointer = i;
                    i = count;
                }else if (data[i] == 'X' && data[i + 1] == 'R' && data[i + 2] == 'e' && data[i + 3] == 'f') {
                    
                    islinearizedCompressed = true;
                    
                    pointer = ptr;
                    while (data[pointer] == 10 || data[pointer] == 13 || data[pointer] == 32) {
                        pointer++;
                    }
                    
                    i = count;
                }
            }
        }
        
        offset.addXref(pointer);
        
        PdfObject rootObj=null;
        
        if (pointer >= eof || pointer==0) {
            
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Pointer not if file - trying to manually find startref");
            }
            
            offset.setRefTableInvalid(true);
            
            try{
                rootObj=new PageObject(BrokenRefTable.findOffsets(pdf_datafile, offset));
            }catch(Error err){
                throw new PdfException(err.getMessage()+" attempting to manually scan file for objects");
            }
            
            currentPdfFile.readObject(rootObj);
            return rootObj;
            
        } else if (islinearizedCompressed || isCompressedStream(pointer, eof)) {
            return readCompressedStream(rootObj,pointer, currentPdfFile, objectReader,linearObj);
        } else {
            return readLegacyReferenceTable(rootObj,pointer, eof,currentPdfFile);
        }
            
        
    }
    
    
    /**
     * read reference table from file so we can locate
     * objects in pdf file and read the trailers
     */
    private PdfObject readLegacyReferenceTable(PdfObject rootObj,int pointer, final int eof, final PdfFileReader currentPdfFile) throws PdfException {
        
        
        int endTable, current = 0; //current object number
        byte[] Bytes  ;
        int bufSize = 1024;
        
        /**read and decode 1 or more trailers*/
        while (true) {
            
            try {
                
                //allow for pointer outside file
                Bytes=Trailer.readTrailer(bufSize, pointer, eof,pdf_datafile);
                
            } catch (final Exception e) {
                
                try {
                    closeFile();
                } catch (final IOException e1) {
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception " + e + " closing file "+e1);
                    }
                }
                throw new PdfException("Exception " + e + " reading trailer");
            }
            
            if (Bytes == null) //safety catch
            {
                break;
            }
            
            /**get trailer*/
            int i = 0;
            
            final int maxLen=Bytes.length;
            
            //for(int a=0;a<100;a++)
            //	System.out.println((char)Bytes[i+a]);
            while (i <maxLen) {//look for trailer keyword
                if (Bytes[i] == 116 && Bytes[i + 1] == 114 && Bytes[i + 2] == 97 && Bytes[i + 3] == 105 &&
                        Bytes[i + 4] == 108 && Bytes[i + 5] == 101 && Bytes[i + 6] == 114) {
                    break;
                }
                
                i++;
            }
            
            //save endtable position for later
            endTable = i;
            
            if(i==Bytes.length) {
                break;
            }
            
            //move to beyond <<
            while (Bytes[i] != 60 && Bytes[i - 1] != 60) {
                i++;
            }
            
            i++;
            final PdfObject pdfObject=new CompressedObject("1 0 R");
            Dictionary.readDictionary(pdfObject, i, Bytes, -1, true,currentPdfFile, false);
            
            //move to beyond >>
            int level=0;
            while(true){
                
                if(Bytes[i] == 60 && Bytes[i - 1] == 60){
                    level++;
                    i++;
                }else if(Bytes[i] =='['){
                    i++;
                    while(Bytes[i]!=']'){
                        i++;
                        if(i==Bytes.length) {
                            break;
                        }
                    }
                }else if(Bytes[i] ==62 && Bytes[i - 1] ==62){
                    level--;
                    i++;
                }
                
                if(level==0) {
                    break;
                }
                
                i++;
            }
            
            //handle optional XRefStm
            final int XRefStm=pdfObject.getInt(PdfDictionary.XRefStm);
            
            if(XRefStm!=-1){
                pointer=XRefStm;
            }else{ //usual way
                
                boolean hasRef=true;
                
                /**
                 * handle spaces and comments
                 */
                while (Bytes[i] ==10 || Bytes[i] ==13) {
                    i++;
                }
                
                while (Bytes[i] =='%'){
                    while(Bytes[i]!=10){
                        
                        i++;
                    }
                    i++;
                }
                /* fix for /Users/markee/Downloads/oneiderapartnerbrochure_web_1371798737.pdf
                /**/
                
                //look for xref as end of startref
                while (Bytes[i] != 116 && Bytes[i + 1] != 120 &&
                        Bytes[i + 2] != 114 && Bytes[i + 3] != 101 && Bytes[i + 4] != 102){
                    
                    if(Bytes[i]=='o' && Bytes[i+1]=='b' && Bytes[i+2]=='j'){
                        hasRef=false;
                        break;
                    }
                    i++;
                }
                
                if(hasRef){
                    
                    i += 8;
                    //move to start of value ignoring spaces or returns
                    while ((i < maxLen)&& (Bytes[i] == 10 || Bytes[i] == 32 || Bytes[i] == 13)) {
                        i++;
                    }
                    
                    final int s=i;
                    
                    //allow for characters between xref and startref
                    while (i < maxLen && Bytes[i] != 10 && Bytes[i] != 32 && Bytes[i] != 13) {
                        i++;
                    }
                    
                    /**convert xref to string to get pointer*/
                    if (s!=i) {
                        pointer = NumberUtils.parseInt(s, i, Bytes);
                    }
                    
                }
            }
            
            i=0;
            
            //allow for bum data at start
            while(Bytes[i]==13 || Bytes[i] == 32  || Bytes[i]==10 || Bytes[i]==9) {
                i++;
            }
            
            if (pointer == -1){
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("No startRef");
                }
                
                /**now read the objects for the trailers*/
            } else if (Bytes[i] == 120 && Bytes[i+1] == 114 && Bytes[i+2] == 101 && Bytes[i+3] == 102) { //make sure starts xref
                
                i = 5;
                
                //move to start of value ignoring spaces or returns
                while (Bytes[i] == 10 ||Bytes[i] == 32 || Bytes[i] == 13) {
                    i++;
                }
                
                current = offset.readXRefs(current, Bytes, endTable, i,eof,pdf_datafile);
                
                /**now process trailer values - only first set of table values for root, encryption and info*/
                if (rootObj==null) {
                    
                    rootObj=pdfObject.getDictionary(PdfDictionary.Root);
                    
                    encryptObj=pdfObject.getDictionary(PdfDictionary.Encrypt);
                    if(encryptObj!=null){
                        
                        final byte[][] IDs=pdfObject.getStringArray(PdfDictionary.ID);
                        if(IDs!=null && this.ID==null) {
                            // only the first encountered ID should be used as a fileID for decryption
                            this.ID = IDs[0];
                        }
                    }
                    
                    infoObject=pdfObject.getDictionary(PdfDictionary.Info);
                    
                }
                
                //make sure first values used if several tables and code for prev
                pointer=pdfObject.getInt(PdfDictionary.Prev);
                
                //see if other trailers
                if (pointer!=-1 && pointer<this.eof) {
                    //reset values for loop
                    bufSize = 1024;
                    
                    //track ref table so we can work out object length
                    offset.addXref(pointer);
                    
                }else //reset if fails second test above
                {
                    pointer = -1;
                }
                
            } else{
                pointer=-1;
                
                //needs to be read to pick up potential /Pages value
                //noinspection ObjectAllocationInLoop
                rootObj=new PageObject(BrokenRefTable.findOffsets(pdf_datafile, offset));
                currentPdfFile.readObject(rootObj);
                
                offset.setRefTableInvalid(true);
                
                
            }
            if (pointer == -1) {
                break;
            }
        }
        
        if (encryptObj == null && rootObj != null) { //manual check for broken file (ignore if Encrypted)
            
            int type=-1;
            
            int status=rootObj.getStatus();
            byte[] data=rootObj.getUnresolvedData();
            
            try{
                
                final ObjectDecoder objectDecoder = new ObjectDecoder(currentPdfFile);
                objectDecoder.checkResolved(rootObj);
                
                type = rootObj.getParameterConstant(PdfDictionary.Type);
                
            }catch(Exception e){ //we need to ignore so just catch, put back as was and log
                
                rootObj.setStatus(status);
                rootObj.setUnresolvedData(data, status);
                if(LogWriter.isOutput()){
                    LogWriter.writeLog("[PDF] Exception reading type on root object "+e);
                }
            }
           
            //something gone wrong so manually index
            if (type == PdfDictionary.Font) { //see 21153 - ref table in wrong order
                rootObj=null; ///will reset in code at end
            }
        }
        
        //something gone wrong so manually index
        if (rootObj==null) { //see 21382
            
            offset.clear();
            offset.reuse();
            
            //needs to be read to pick up potential /Pages value
            //noinspection ObjectAllocationInLoop
            rootObj = new PageObject(BrokenRefTable.findOffsets(pdf_datafile, offset));
            currentPdfFile.readObject(rootObj);
            
            offset.setRefTableInvalid(true);
            
        }
        
        return rootObj;
    }
    
    
    /**
     * read 1.5 compression stream ref table
     * @throws PdfException
     */
    private PdfObject readCompressedStream(PdfObject rootObj,int pointer, final PdfFileReader currentPdfFile, final ObjectReader objectReader, final PdfObject linearObj) throws PdfException {
        
        while (pointer != -1) {
            
            /**
             * get values to read stream ref
             */
            movePointer(pointer);
            
            final byte[] raw = objectReader.readObjectData(-1, null);
            
            /**read the object name from the start*/
            final StringBuilder objectName=new StringBuilder();
            char current1,last=' ';
            int matched=0, i1 =0;
            while(i1 <raw.length){
                current1 =(char)raw[i1];
                
                //treat returns same as spaces
                if(current1 ==10 || current1 ==13) {
                    current1 = ' ';
                }
                
                if(current1 ==' ' && last==' '){//lose duplicate or spaces
                    matched=0;
                }else if(current1 ==pattern.charAt(matched)){ //looking for obj at end
                    matched++;
                }else{
                    matched=0;
                    objectName.append(current1);
                }
                if(matched==3) {
                    break;
                }
                last= current1;
                i1++;
            }
            
            //add end and put into Map
            objectName.append('R');
            
            final PdfObject pdfObject=new CompressedObject(objectName.toString());
            pdfObject.setCompressedStream(true);
            final ObjectDecoder objectDecoder=new ObjectDecoder(currentPdfFile);
            objectDecoder.readDictionaryAsObject(pdfObject, 0, raw);
            
            //read the field sizes
            final int[] fieldSizes=pdfObject.getIntArray(PdfDictionary.W);
            
            //read the xrefs stream
            byte[] xrefs=pdfObject.getDecodedStream();
            
            //if encr
            if(xrefs==null){
                xrefs= currentPdfFile.readStream(pdfObject, true, true, false, false, true, null);
            }
            
            final int[] Index=pdfObject.getIntArray(PdfDictionary.Index);
            if(Index==null){ //single set of values
                
                //System.out.println("-------------1.Offsets-------------"+current+" "+numbEntries);
                CompressedObjects.readCompressedOffsets(0, 0, pdfObject.getInt(PdfDictionary.Size), fieldSizes, xrefs,offset,pdf_datafile);
                
            }else{ //pairs of values in Index[] array
                final int count=Index.length;
                int pntr=0;

                for(int aa=0;aa<count;aa += 2){
                    
                    //System.out.println("-------------2.Offsets-------------"+Index[aa]+" "+Index[aa+1]);
                    
                    pntr=CompressedObjects.readCompressedOffsets(pntr, Index[aa], Index[aa + 1], fieldSizes, xrefs,offset,pdf_datafile);
                }
            }
            
            /**
             * now process trailer values - only first set of table values for
             * root, encryption and info
             */
            if (rootObj==null) {
                
                rootObj=pdfObject.getDictionary(PdfDictionary.Root);
                
                /**
                 * handle encryption
                 */
                encryptObj=pdfObject.getDictionary(PdfDictionary.Encrypt);
                
                if (encryptObj != null) {
                    
                    final byte[][] IDs=pdfObject.getStringArray(PdfDictionary.ID);
                    if(IDs!=null && this.ID==null) {
                        // only the first encountered ID should be used as a fileID for decryption
                        this.ID = IDs[0];
                    }
                }
                
                infoObject=pdfObject.getDictionary(PdfDictionary.Info);
                
            }
            
            //make sure first values used if several tables and code for prev so long as not linearized
            //may need adjusting as more examples turn up
            if(linearObj!=null) {
                pointer = -1;
            } else{
                pointer=pdfObject.getInt(PdfDictionary.Prev);
                
                //a non-compressed object table can follow a compressed one so we need to allow for this
                if(pointer!=-1 && !isCompressedStream(pointer, (int)eof)) {
                    return readLegacyReferenceTable(rootObj, pointer, (int) eof, currentPdfFile);
                }
            }
        }
        
        
        return rootObj;
    }
    
    byte[] getBytes(final long start, final int count) {
        final byte[] buffer=new byte[count];
        
        if(start>=0){
            try {
                pdf_datafile.seek(start);
                pdf_datafile.read(buffer); //get next chars
            } catch (final IOException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
        }
        
        return buffer;
    }
    
    void closeFile() throws IOException {
        
        if(pdf_datafile!=null){
            pdf_datafile.close();
            pdf_datafile=null;
        }
    }
    
    
    /**
     * test first bytes to see if new 1.5 style table with obj or contains ref
     * @throws PdfException
     */
    private boolean isCompressedStream(int pointer, final int eof) throws PdfException {
        
        final boolean debug=false;
        
        int bufSize = 50,charReached_legacy=0, charReached_comp1=0,charReached_comp2=0;
        
        final int[] objStm={'O','b','j','S','t','m'};
        final int[] XRef={'X','R','e','f'};
        
        int type=UNSET;
        
        //flag to show if at start of data for check
        boolean firstRead=true;
        
        while (true) {
            
            /** adjust buffer if less than 1024 bytes left in file */
            if (pointer + bufSize > eof) {
                bufSize = eof - pointer;
            }
            
            if(bufSize<0) {
                bufSize = 50;
            }

            if (pointer < 0) {
                pointer += bufSize;
                continue;
            }

            final byte[] buffer = getBytes(pointer, bufSize);
            
            //allow for fact sometimes start of data wrong
            if(firstRead && buffer[0]=='r' && buffer[1]=='e' && buffer[2]=='f') {
                charReached_legacy = 1;
            }
            
            firstRead=false; //switch off
            
            /**look for xref or obj */
            for (int i = 0; i < bufSize; i++) {
                
                final byte currentByte = buffer[i];
                
                if(debug) {
                    System.out.print((char) currentByte);
                }
                
                /** check for xref OR end - reset if not */
                if (currentByte == oldPattern[charReached_legacy] && type!=COMPRESSED){
                    charReached_legacy++;
                    type=LEGACY;
                }else if ((currentByte == objStm[charReached_comp1] )&& (charReached_comp1==0 || type==COMPRESSED)){
                    
                    charReached_comp1++;
                    type=COMPRESSED;
                }else if ((currentByte == XRef[charReached_comp2] )&& (charReached_comp2==0 || type==COMPRESSED)){
                    
                    charReached_comp2++;
                    type=COMPRESSED;
                }else{
                    
                    charReached_legacy=0;
                    charReached_comp1=0;
                    charReached_comp2=0;
                    
                    type=UNSET;
                }
                
                if (charReached_legacy==3 || charReached_comp1==4 || charReached_comp2 == 3) {
                    break;
                }
                
            }
            
            if (charReached_legacy==3 || charReached_comp1==4 || charReached_comp2 == 3) {
                break;
            }
            
            //update pointer
            pointer += bufSize;
            
        }
        
        /**
         * throw exception if no match or tell user which type
         */
        if(type==UNSET){
            try {
                closeFile();
            } catch (final IOException e1) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + 1 + " closing file "+e1);
                }
            }
            throw new PdfException("Exception unable to find ref or obj in trailer");
        }
        
        return type == COMPRESSED;
    }
    
    public PdfObject getInfoObject() {
        return infoObject;
    }
    
    public PdfObject getEncryptionObject() {
        return encryptObj;
    }
    
    public byte[] getID(){
        return ID;
    }
    
    //////////////////////////////////////////////////////////////////////////
    /**
     * returns current location pointer and sets to new value
     */
    public void movePointer(final long pointer)
    {
        try
        {
            //make sure inside file
            if( pointer > pdf_datafile.length() ){
            	
            	if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Attempting to access ref outside file");
                }
                //throw new PdfException("Exception moving file pointer - ref outside file");
            }else{
                pdf_datafile.seek( pointer );
            }
        }catch( final Exception e ){
        	if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " moving pointer to  " + pointer + " in file.");
            }
        }
    }
}


