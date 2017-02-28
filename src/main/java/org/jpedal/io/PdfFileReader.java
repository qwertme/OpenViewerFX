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
 * PdfFileReader.java
 * ---------------
 */
package org.jpedal.io;


import org.jpedal.constants.PDFflags;
import org.jpedal.exception.PdfException;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.types.*;
import org.jpedal.objects.raw.*;
import org.jpedal.utils.LogWriter;

import java.io.*;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * provides access to the file using Random access class to
 * read bytes and strings from a pdf file. Pdf file is a mix of
 * character and binary data streams
 */
public class PdfFileReader
{

    ObjectReader objectReader;

    private PrivateKey key;

    private Certificate certificate;


    private LinearizedHintTable linHintTable;

    /**used to cache last compressed object*/
    private byte[] lastCompressedStream;

    /**used to cache last compressed object*/
    private Map lastOffsetStart,lastOffsetEnd;

    private PdfObject compressedObj;

    /**used to cache last compressed object*/
    private int lastFirst=-1,lastCompressedID=-1;

    private RefTable refTable;


    public PdfObject getInfoObject() {
        return refTable.getInfoObject();
    }

    /**
     * set size over which objects kept on disk
     */
    public void setCacheSize(final int miniumumCacheSize) {

        objectReader.setCacheSize(miniumumCacheSize);

    }


    PdfObject encyptionObj;

    //private boolean isFDF=false;

    private DecryptionFactory decryption;

    /**encryption password*/
    private byte[] encryptionPassword;

    /**file access*/
    private RandomAccessBuffer pdf_datafile;

   // private final static byte[] endObj = { 32, 111, 98, 106 }; //pattern endobj

    /**location from the reference table of each
     * object in the file
     */
    private Offsets offset = new Offsets( 2000 );

    /**should never be final*/
    public static int alwaysCacheInMemory=16384;

    private long eof;

    /**length of each object*/
    private int[] ObjLengthTable;

    /**
     * return pdf data
     */
    public byte[] getBuffer() {
        return pdf_datafile.getPdfBuffer();
    }

    public void init(final RandomAccessBuffer pdf_datafile){

        this.pdf_datafile=pdf_datafile;

        try{
            eof=pdf_datafile.length();
        }catch(final IOException e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        objectReader=new ObjectReader(pdf_datafile,eof,this);

        refTable=new RefTable(pdf_datafile,eof,offset);

    }

    /**
     * read an object in the pdf into a Object which can be an indirect or an object
     *
     */
    public final void readObject(final PdfObject pdfObject){

        if(pdfObject.isDataExternal() && linHintTable!=null){
            readExternalObject(pdfObject);
        }else{

            final String objectRef=pdfObject.getObjectRefAsString();

            final int id=pdfObject.getObjectRefID();

            final boolean debug=false;

            if(debug) {
                System.err.println("reading objectRef=" + objectRef + "< isCompressed=" + offset.isCompressed(id));
            }

            final boolean isCompressed=offset.isCompressed(id);
            pdfObject.setCompressedStream(isCompressed);

            //any stream
            final byte[] raw ;//stream=null;

            /**read raw object data*/
            if(isCompressed){
                raw = readCompressedObject(pdfObject);
            }else{
                movePointer(offset.elementAt(id));

                if(objectRef.charAt(0)=='<'){
                    raw=objectReader.readObjectData(-1, pdfObject);
                }else{

                    if(ObjLengthTable==null || offset.isRefTableInvalid()){ //isEncryptionObject

                        //allow for bum object
                        if(getPointer()==0) {
                            raw = new byte[0];
                        } else {
                            raw = objectReader.readObjectData(-1, pdfObject);
                        }


                    }else if(id>ObjLengthTable.length || ObjLengthTable[id]==0){
                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog(objectRef + " cannot have offset 0");
                        }

                        raw=new byte[0];
                    }else {
                        raw = objectReader.readObjectData(ObjLengthTable[id], pdfObject);
                    }
                }
            }

            if(raw.length>1){
                final ObjectDecoder objDecoder=new ObjectDecoder(this);
                objDecoder.readDictionaryAsObject(pdfObject,0,raw);
            }
        }
    }

    private void readExternalObject(final PdfObject pdfObject) {

        final int ref=pdfObject.getObjectRefID();
        final int generation=pdfObject.getObjectRefGeneration();

        final byte[] pageData = readObjectAsByteArray(pdfObject, isCompressed(ref, generation), ref, generation);

        pdfObject.setStatus(PdfObject.UNDECODED_DIRECT);
        pdfObject.setUnresolvedData(pageData, PdfDictionary.Page);

        ObjectDecoder.resolveFully(pdfObject, this);
    }

    private byte[] readCompressedObject(final PdfObject pdfObject) {

        byte[] raw;
        final int objectID=pdfObject.getObjectRefID();
        final int compressedID=offset.elementAt(objectID);
        String startID=null;
        int First=lastFirst;
        boolean isCached=true; //assume cached

        //see if we already have values
        byte[] compressedStream=lastCompressedStream;
        Map offsetStart=lastOffsetStart;
        Map offsetEnd=lastOffsetEnd;

        PdfObject Extends=null;

        if(lastOffsetStart!=null && compressedID==lastCompressedID) {
            startID = (String) lastOffsetStart.get(String.valueOf(objectID));
        }

        //read 1 or more streams
        while(startID==null){

            if(Extends!=null){
                compressedObj=Extends;
            }else if(compressedID!=lastCompressedID){

                isCached=false;

                movePointer(offset.elementAt(compressedID));

                raw = objectReader.readObjectData(ObjLengthTable[compressedID],null);

                compressedObj=new CompressedObject(compressedID,0);
                final ObjectDecoder objDecoder=new ObjectDecoder(this);
                objDecoder.readDictionaryAsObject(compressedObj,0,raw);

            }

            /**get offsets table see if in this stream*/
            offsetStart=new HashMap();
            offsetEnd=new HashMap();
            First=compressedObj.getInt(PdfDictionary.First);

            compressedStream=compressedObj.getDecodedStream();

            CompressedObjects.extractCompressedObjectOffset(offsetStart, offsetEnd, First, compressedStream, compressedID,offset);

            startID=(String) offsetStart.get(String.valueOf(objectID));

            Extends=compressedObj.getDictionary(PdfDictionary.Extends);
            if(Extends==null) {
                break;
            }

        }

        if(!isCached){
            lastCompressedStream=compressedStream;
            lastCompressedID=compressedID;
            lastOffsetStart=offsetStart;
            lastOffsetEnd=offsetEnd;
            lastFirst=First;
        }

        /**put bytes in stream*/
        final int start=First+Integer.parseInt(startID);
        int end=compressedStream.length;

        final String endID=(String) offsetEnd.get(String.valueOf(objectID));
        if(endID!=null) {
            end = First + Integer.parseInt(endID);
        }

        final int streamLength=end-start;
        raw = new byte[streamLength];
        System.arraycopy(compressedStream, start, raw, 0, streamLength);

        pdfObject.setInCompressedStream(true);

        return raw;
    }

    /**read a stream*/
    public final byte[] readStream(final PdfObject pdfObject, final boolean cacheValue,
                                   final boolean decompress, final boolean keepRaw, final boolean isMetaData,
                                   final boolean isCompressedStream, final String cacheName)  {

        final boolean debugStream=false;

        boolean isCachedOnDisk = pdfObject.isCached();

        byte[] data=null;

        if(!isCachedOnDisk) {
            data = pdfObject.getDecodedStream();
        }

        //BufferedOutputStream streamCache=null;
        byte[] stream;

        //decompress first time
        if(data==null){

            stream=pdfObject.stream;

            if(isCachedOnDisk){

                //decrypt the stream
                try{
                    if(decryption!=null && !isCompressedStream && (decryption.getBooleanValue(PDFflags.IS_METADATA_ENCRYPTED) || !isMetaData)){

                        decryption.decrypt(null,pdfObject.getObjectRefAsString(), false,cacheName, false,false);
                    }
                }catch(final Exception e){
                    e.printStackTrace();
                    stream=null;
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception " + e);
                    }
                }
            }

            if(stream!=null){ /**decode and save stream*/

                //decrypt the stream
                try{
                    if(decryption!=null && !isCompressedStream  && (decryption.getBooleanValue(PDFflags.IS_METADATA_ENCRYPTED) || !isMetaData)){// && pdfObject.getObjectType()!=PdfDictionary.ColorSpace){

                        // System.out.println(objectRef+">>>"+pdfObject.getObjectRefAsString());
                        if(pdfObject.getObjectType()==PdfDictionary.ColorSpace && pdfObject.getObjectRefAsString().startsWith("[")){

                        }else {
                            stream = decryption.decrypt(stream, pdfObject.getObjectRefAsString(), false, null, false, false);
                        }

                    }
                }catch(final PdfSecurityException e){

                    //
                    stream=null;

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception " + e + " with " + pdfObject.getObjectRefAsString());
                    }
                }
            }

            if(keepRaw) {
                pdfObject.stream = null;
            }

            int length=1;

            if(stream!=null || isCachedOnDisk){

                //values for CCITTDecode
                int height=1,width=1;

                final int newH=pdfObject.getInt(PdfDictionary.Height);
                if(newH!=-1) {
                    height = newH;
                }

                final int newW=pdfObject.getInt(PdfDictionary.Width);
                if(newW!=-1) {
                    width = newW;
                }

                final int newLength=pdfObject.getInt(PdfDictionary.Length);
                if(newLength!=-1) {
                    length = newLength;
                }

                /**allow for no width or length*/
                if(height*width==1) {
                    width = length;
                }

                final PdfArrayIterator filters = pdfObject.getMixedArray(PdfDictionary.Filter);

                //check not handled elsewhere
                int firstValue=PdfDictionary.Unknown;
                if(filters!=null && filters.hasMoreTokens()) {
                    firstValue = filters.getNextValueAsConstant(false);
                }

                if(debugStream) {
                    System.out.println("First filter=" + firstValue);
                }

                if (filters != null && firstValue!=PdfDictionary.Unknown && firstValue!=PdfFilteredReader.JPXDecode &&
                        firstValue!=PdfFilteredReader.DCTDecode){

                    if(debugStream) {
                        System.out.println("Decoding stream " + Arrays.toString(stream) + ' ' + pdfObject.isCached() + ' ' + pdfObject.getObjectRefAsString());
                    }

                    try{
                        final PdfFilteredReader filter=new PdfFilteredReader();
                        stream =filter.decodeFilters(ObjectUtils.setupDecodeParms(pdfObject,this), stream, filters ,width,height, cacheName);

                        //flag if any error
                        pdfObject.setStreamMayBeCorrupt(filter.hasError());

                    }catch(final Exception e){

                        //
                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog("[PDF] Problem " + e + " decompressing stream ");
                        }

                        stream=null;
                        isCachedOnDisk=false; //make sure we return null, and not some bum values
                    }

                    //stop spurious match down below in caching code
                    length=1;
                }else if(stream!=null && length!=-1 && length<stream.length ){

                    /**make sure length correct*/
                    //if(stream.length!=length){
                    if(stream.length!=length && length>0){//<--  last item breaks jbig??
                        final byte[] newStream=new byte[length];
                        System.arraycopy(stream, 0, newStream, 0, length);

                        stream=newStream;
                    }else if(stream.length==1 && length==0) {
                        stream = new byte[0];
                    }
                }
            }


            if(stream!=null && cacheValue) {
                pdfObject.setDecodedStream(stream);
            }

            if(decompress && isCachedOnDisk){
                final int streamLength = (int) new File(cacheName).length();

                byte[] bytes = new byte[streamLength];

                try {
                    new BufferedInputStream(new FileInputStream(cacheName)).read(bytes);
                } catch (final Exception e) {
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }

                /**resize if length supplied*/
                if((length!=1)&&(length<streamLength)){

                    /**make sure length correct*/
                    final byte[] newStream=new byte[length];
                    System.arraycopy(bytes, 0, newStream, 0, length);

                    bytes=newStream;

                }

                return bytes;
            }

        }else {
            stream = data;
        }

        if(stream==null) {
            return null;
        }

        //make a a DEEP copy so we cant alter
        final int len=stream.length;
        final byte[] copy=new byte[len];
        System.arraycopy(stream, 0, copy, 0, len);

        return  copy;
    }

    /**give user access to internal flags such as user permissions*/
    @SuppressWarnings("UnusedDeclaration")
    public int getPDFflag(final Integer flag) {

        if(decryption==null) {
            return -1;
        } else {
            return decryption.getPDFflag(flag);
        }

    }

    public void spoolStreamDataToDisk(final File tmpFile,long start, final int size) throws Exception{

        movePointer(start);

        boolean hasValues=false;
        //final boolean streamFound=false;
        boolean startStreamFound=false;

        // Create output file
        final BufferedOutputStream array =new BufferedOutputStream(new FileOutputStream(tmpFile));

        int bufSize=-1;
        int startStreamCount=0;
        int realPos=0;
        //final int streamCount=0;

        final int XXX=2*1024*1024;

        final boolean debug=false;

        if(debug) {
            System.out.println("=============================");
        }

        if(bufSize<1) {
            bufSize = 128;
        }

        //array for data
        int ptr=0, maxPtr=bufSize;

        byte[] readData=new byte[maxPtr];

        byte[] buffer=null;
        //final boolean inStream=false;

        long pointer;

        /**read the object or block*/
        try {

            byte currentByte;

            int i=bufSize-1,offset=-bufSize;

            while (true) {

                i++;

                if(i==bufSize){ //read the next block

                    pointer = getPointer();

                    if(start==-1) {
                        start = pointer;
                    }

                    /**adjust buffer if less than bytes left in file*/
                    if (pointer + bufSize > eof) {
                        bufSize = (int) (eof - pointer);
                    }

                    bufSize += 6;
                    buffer = new byte[bufSize];

                    pdf_datafile.read(buffer);  //get bytes into buffer

                    offset += i;
                    i=0;

                }

                /**write out and look for endobj at end*/
                //lastByte=currentByte;
                currentByte = buffer[i];

                //look for start of stream and set inStream true
                if((startStreamFound) && (hasValues || currentByte!=13 && currentByte!=10)){ //avoid trailing CR/LF
                        array.write(currentByte);
                        hasValues=true;

                        realPos++;
                    }

                if (startStreamCount<6 && currentByte == ObjectReader.startStream[startStreamCount]){

                    startStreamCount++;

                    if(startStreamCount == 6){ //stream start found so log
                        startStreamFound=true;
                    }

                }else {
                    startStreamCount = 0;
                }

                if (realPos>=size){
                    break;
                }

                //if(!inStream){

                    readData[ptr]=currentByte;

                    ptr++;
                    if(ptr==maxPtr){
                        if(maxPtr<XXX) {
                            maxPtr *= 2;
                        } else {
                            maxPtr += 100000;
                        }

                        final byte[] tmpArray=new byte[maxPtr];
                        System.arraycopy(readData,0,tmpArray,0,readData.length);

                        readData=tmpArray;
                    }
               // }


            }

        } catch (final Exception e) {
            e.printStackTrace();

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " reading object");
            }
        }

        if(array!=null){
            array.flush();
            array.close();
        }
    }



    public void spoolStreamDataToDisk(final File tmpFile,long start) throws Exception{

        movePointer(start);

        boolean hasValues=false;

        // Create output file
        final BufferedOutputStream array =new BufferedOutputStream(new FileOutputStream(tmpFile));

        int bufSize=-1;
        //PdfObject pdfObject=null;

        int startStreamCount=0;//newCacheSize=-1,;
        boolean startStreamFound=false;

        //if(pdfObject!=null) //only use if values found
        //newCacheSize=this.newCacheSize;

        final int XXX=2*1024*1024;

        final int rawSize=bufSize;
        int realPos=0;

        final boolean debug=false;

        boolean lengthSet=false; //start false and set to true if we find /Length in metadata
       // final boolean streamFound=false;

        if(debug) {
            System.out.println("=============================");
        }

        if(bufSize<1) {
            bufSize = 128;
        }

//        if(newCacheSize!=-1 && bufSize>newCacheSize)
        //bufSize=newCacheSize;

        //array for data
        int ptr=0, maxPtr=bufSize;

        byte[] readData=new byte[maxPtr];

        int charReached = 0,charReached2, charReached3=0;

        byte[] buffer=null;
        //final boolean inStream=false;
        boolean ignoreByte;

        /**adjust buffer if less than bytes left in file*/
        long pointer ;//lastEndStream=-1,objStart=-1;

        /**read the object or block*/
        try {

            byte currentByte ;//lastByte;

            int i=bufSize-1,offset=-bufSize;
            //int blocksRead=0;//lastEnd=-1,lastComment=-1;

            while (true) {

                i++;

                if(i==bufSize){

                    //cache data and update counter
//                    if(blocksRead==1){
//                        dataRead=buffer;
//                    }else if(blocksRead>1){
//
//                        int bytesRead=dataRead.length;
//                        int newBytes=buffer.length;
//                        byte[] tmp=new byte[bytesRead+newBytes];
//
//                        //existing data into new array
//                        System.arraycopy(dataRead, 0, tmp, 0, bytesRead);
//
//                        //data from current block
//                        System.arraycopy(buffer, 0, tmp, bytesRead, newBytes);
//
//                        dataRead=tmp;
//
//                        //PUT BACK to switch on caching
//                        if(1==2 && streamFound && dataRead.length>newCacheSize) //stop if over max size
//                            break;
//                    }
//                       blocksRead++;

                    /**
                     * read the next block
                     */
                    pointer = getPointer();

                    if(start==-1) {
                        start = pointer;
                    }

                    /**adjust buffer if less than bytes left in file*/
                    if (pointer + bufSize > eof) {
                        bufSize = (int) (eof - pointer);
                    }

                    bufSize += 6;
                    buffer = new byte[bufSize];

                    /**get bytes into buffer*/
                    pdf_datafile.read(buffer);

                    offset += i;
                    i=0;

                }

                /**write out and look for endobj at end*/
                //lastByte=currentByte;
                currentByte = buffer[i];
                ignoreByte=false;

                //track comments
                //if(currentByte=='%')
                //lastComment=realPos;

                /**check for endobj at end - reset if not*/
                if (currentByte == ObjectDecoder.endPattern[charReached])// &&  !inStream)
                {
                    charReached++;
                } else {
                    charReached = 0;
                }

                //also scan for <SPACE>obj after endstream incase no endobj
                //if(streamFound &&currentByte == endObj[charReached2] &&  !inStream)
                //    charReached2++;
                //else
                    charReached2 = 0;

                //look for start of stream and set inStream true

                if((startStreamFound) && (hasValues || currentByte!=13 && currentByte!=10)){ //avoid trailing CR/LF
                        array.write(currentByte);
                        hasValues=true;
                    }

                if (startStreamCount<6 && currentByte == ObjectReader.startStream[startStreamCount]){
                    startStreamCount++;
                }else {
                    startStreamCount = 0;
                }

                if(!startStreamFound && startStreamCount == 6){ //stream start found so log
                    //startStreamCount=offsetRef+startStreamCount;
                    startStreamFound=true;
                }


                /**if length not set we go on endstream in data*/
                if(!lengthSet){

                    //also scan for /Length if it had a valid size
                    if((rawSize!=-1) && (currentByte == ObjectReader.lengthString[charReached3])){// &&  !inStream){
                            charReached3++;
                            if(charReached3==6) {
                                lengthSet = true;
                            }
                        }else {
                            charReached3 = 0;
                        }
                    }

                if (charReached == 6 || charReached2==4){

                    if(!lengthSet) {
                        break;
                    }

                    charReached=0;
                    //charReached2=0;
                    //lastEnd=realPos;

                }

                if(lengthSet && realPos>=rawSize) {
                    break;
                }

                if(!ignoreByte){//|| !inStream)

                    readData[ptr]=currentByte;

                    ptr++;
                    if(ptr==maxPtr){
                        if(maxPtr<XXX) {
                            maxPtr *= 2;
                        } else {
                            maxPtr += 100000;
                        }

                        final byte[] tmpArray=new byte[maxPtr];
                        System.arraycopy(readData,0,tmpArray,0,readData.length);

                        readData=tmpArray;
                    }
                }

                realPos++;
            }

        } catch (final Exception e) {
            e.printStackTrace();

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " reading object");
            }
        }

        if(array!=null){
            array.flush();
            array.close();
        }
    }

    void closeFile() throws IOException {

        if(pdf_datafile!=null){
            pdf_datafile.close();
            pdf_datafile=null;
        }
    }

    public long getOffset(final int currentID) {
        return offset.elementAt(currentID );
    }

    public byte[] getBytes(final long start, final int count) {
        final byte[] buffer=new byte[count];

        movePointer(start);
        try {
            pdf_datafile.read(buffer); //get next chars
        } catch (final IOException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        return buffer;
    }

    public void storeLinearizedTables(final LinearizedHintTable linHintTable) {
        this.linHintTable=linHintTable;
    }



    public void dispose(){

        if(decryption!=null){
            decryption.flush();
            decryption.dispose();
        }

        if(decryption!=null) {
            decryption.cipher = null;
        }

        decryption=null;

        this.compressedObj=null;

        //any linearized data
        if(linHintTable!=null){
            linHintTable=null;
        }


        offset=null;

        try {
            if(pdf_datafile!=null) {
                pdf_datafile.close();
            }
        } catch (final IOException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        pdf_datafile=null;

        if(offset!=null){
            offset.dispose();
        }

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * get pdf type in file (found at start of file)
     */
    public final String getType()
    {

        String pdf_type = "";
        try{
            movePointer( 0 );
            pdf_type = pdf_datafile.readLine();

            //strip off anything before
            final int pos=pdf_type.indexOf("%PDF");
            if(pos!=-1) {
                pdf_type = pdf_type.substring(pos + 5);
            }

        }catch( final Exception e ){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " in reading type");
            }
        }
        return pdf_type;
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

    //////////////////////////////////////////////////
    /**
     * gets pointer to current location in the file
     */
    private long getPointer()
    {
        long old_pointer = 0;
        try{
            old_pointer = pdf_datafile.getFilePointer();
        }catch( final Exception e ){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception " + e + " getting pointer in file");
            }
        }
        return old_pointer;
    }

    /**
     * general routine to turn reference into id with object name
     */
    @SuppressWarnings("UnusedParameters")
    public final boolean isCompressed( final int ref, final int gen )
    {

        return offset.isCompressed(ref);
    }

    public DecryptionFactory getDecryptionObject() {
        return decryption;
    }

    public void setPassword(final String password) {

        this.encryptionPassword = password.getBytes();

        //reset
        if(decryption!=null) {
            decryption.reset(encryptionPassword);
        }
    }

    /**
     * read an object in the pdf into a Object which can be an indirect or an object
     *
     */
    public byte[] readObjectData(final PdfObject pdfObject){

        final String objectRef=pdfObject.getObjectRefAsString();

        final int id=pdfObject.getObjectRefID();

        //read the Dictionary data
        if(pdfObject.isDataExternal()){
            //byte[] data=readObjectAsByteArray(pdfObject, objectRef, isCompressed(number,generation),number,generation);
            final byte[] data=readObjectAsByteArray(pdfObject, false,id,0);

            //allow for data in Linear object not yet loaded
            if(data==null){
                pdfObject.setFullyResolved(false);

                //if(debugFastCode)
                //    System.out.println(paddingString+"Data not yet loaded");

                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (15)");
                }

                return data;
            }
        }


        final boolean debug=false;

        if(debug) {
            System.err.println("reading objectRef=" + objectRef + "< isCompressed=" + offset.isCompressed(id));
        }

        final boolean isCompressed=offset.isCompressed(id);
        pdfObject.setCompressedStream(isCompressed);

        //any stream
        final byte[] raw ;

        /**read raw object data*/
        if(isCompressed){
            raw = readCompressedObjectData(pdfObject,offset);
        }else{
            movePointer(offset.elementAt(id));

            if(objectRef.charAt(0)=='<'){
                raw=objectReader.readObjectData(-1, pdfObject);
            }else{

                if(ObjLengthTable==null || offset.isRefTableInvalid()){ //isEncryptionObject

                    //allow for bum object
                    if(getPointer()==0) {
                        raw = new byte[0];
                    } else {
                        raw = objectReader.readObjectData(-1, pdfObject);
                    }


                }else if(id>ObjLengthTable.length || ObjLengthTable[id]==0){
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog(objectRef + " cannot have offset 0");
                    }

                    raw=new byte[0];
                }else {
                    raw = objectReader.readObjectData(ObjLengthTable[id], pdfObject);
                }
            }
        }

        return raw;

    }

    private byte[] readCompressedObjectData(final PdfObject pdfObject, final Offsets offset) {
        byte[] raw;
        final int objectID=pdfObject.getObjectRefID();
        final int compressedID=offset.elementAt(objectID);
        String startID=null;
        int First=lastFirst;
        boolean isCached=true; //assume cached

        //see if we already have values
        byte[] compressedStream=lastCompressedStream;
        Map offsetStart=lastOffsetStart;
        Map offsetEnd=lastOffsetEnd;

        PdfObject Extends=null;

        if(lastOffsetStart!=null) {
            startID = (String) lastOffsetStart.get(String.valueOf(objectID));
        }

        //read 1 or more streams
        while(startID==null){

            if(Extends!=null){
                compressedObj=Extends;
            }else if(compressedID!=lastCompressedID){

                isCached=false;

                movePointer(offset.elementAt(compressedID));

                raw = objectReader.readObjectData(ObjLengthTable[compressedID],null);

                compressedObj=new CompressedObject(compressedID,0);
                final ObjectDecoder objDecoder=new ObjectDecoder(this);
                objDecoder.readDictionaryAsObject(compressedObj,0,raw);

            }

            /**get offsets table see if in this stream*/
            offsetStart=new HashMap();
            offsetEnd=new HashMap();
            First=compressedObj.getInt(PdfDictionary.First);

            compressedStream=compressedObj.getDecodedStream();

            CompressedObjects.extractCompressedObjectOffset(offsetStart, offsetEnd, First, compressedStream, compressedID,offset);

            startID=(String) offsetStart.get(String.valueOf(objectID));

            Extends=compressedObj.getDictionary(PdfDictionary.Extends);
            if(Extends==null) {
                break;
            }

        }

        if(!isCached){
            lastCompressedStream=compressedStream;
            lastCompressedID=compressedID;
            lastOffsetStart=offsetStart;
            lastOffsetEnd=offsetEnd;
            lastFirst=First;
        }

        /**put bytes in stream*/
        final int start=First+Integer.parseInt(startID);
        int end=compressedStream.length;

        final String endID=(String) offsetEnd.get(String.valueOf(objectID));
        if(endID!=null) {
            end = First + Integer.parseInt(endID);
        }

        final int streamLength=end-start;
        raw = new byte[streamLength];
        System.arraycopy(compressedStream, start, raw, 0, streamLength);

        pdfObject.setInCompressedStream(true);
        return raw;
    }


    /**
     * get object as byte[]
     * @param isCompressed
     * @param objectID
     * @param gen
     * @return
     */
    public byte[] readObjectAsByteArray(final PdfObject pdfObject, final boolean isCompressed, final int objectID, final int gen) {

        byte[] raw=null;

        //data not in PDF stream
        //if(pdfObject.isDataExternal()){
        if(linHintTable!=null){
            raw=linHintTable.getObjData(objectID);
        }

        if(raw==null){

            /**read raw object data*/
            if(isCompressed){
                raw = readCompressedObjectAsByteArray(pdfObject, objectID, gen);
            }else{
                movePointer( offset.elementAt(objectID) );

                if(ObjLengthTable==null || offset.isRefTableInvalid()) {
                    raw = objectReader.readObjectData(-1, pdfObject);
                } else if(objectID>ObjLengthTable.length) {
                    return null;
                } else {
                    raw = objectReader.readObjectData(ObjLengthTable[objectID], pdfObject);
                }
            }

        }
        
        /**/
        //check first 10 bytes
        int j=0;
        if(raw.length>15){
            for(int i2=0;i2<10;i2++){

                if(raw[i2]=='o' && raw[i2+1]=='b' && raw[i2+2]=='j'){ //okay of we hit obj firat
                    break;
                }else if(raw[i2] == 'e' && raw[i2+1] == 'n' && raw[i2+2] == 'd' && raw[i2+3] == 'o' && raw[i2+4] == 'b' && raw[i2+5] == 'j'){
                    j=i2+6;
                    objectReader.fileIsBroken=true;

                    break;
                }
            }
        }

        while (raw[j] == 10 || raw[j] == 12 || raw[j] == 32) {
            j++;
        }

        if (j > 0) { //adjust to remove stuff at start
            final byte[] oldBuffer = raw;
            final int newLength = raw.length - j;
            raw = new byte[newLength];
            System.arraycopy(oldBuffer, j, raw, 0, newLength);
        }

        return raw;
    }

    private byte[] readCompressedObjectAsByteArray(final PdfObject pdfObject, final int objectID, final int gen) {
        byte[] raw;
        int compressedID=offset.elementAt(objectID);
        String startID=null,compressedRef;
        Map offsetStart=lastOffsetStart,offsetEnd=lastOffsetEnd;
        int First=lastFirst;
        byte[] compressedStream;
        boolean isCached=true; //assume cached

        PdfObject compressedObj, Extends;

        //see if we already have values
        compressedStream=lastCompressedStream;
        if(lastOffsetStart!=null) {
            startID = (String) lastOffsetStart.get(String.valueOf(objectID));
        }

        int lastCompressedID=-1;
        
        //read 1 or more streams
        while(startID==null){

            isCached=false;
            
            if(lastCompressedID==compressedID){
                throw new RuntimeException("Compressed Object stream corrupted - PDF file broken");
            }
            
            try {
                pdf_datafile.seek( offset.elementAt(compressedID) );
            } catch (final IOException e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " moving pointer in file.");
                }
            }
            lastCompressedID=compressedID;

            raw = objectReader.readObjectData(ObjLengthTable[compressedID],null);

            //may need to use compObj and not objectRef
            final String compref=compressedID+" "+gen+" R";
            compressedObj=new CompressedObject(compref);
            final ObjectDecoder objDecoder=new ObjectDecoder(this);
            objDecoder.readDictionaryAsObject(compressedObj,0,raw);

            /**get offsets table see if in this stream*/
            offsetStart=new HashMap();
            offsetEnd=new HashMap();

            First=compressedObj.getInt(PdfDictionary.First);

            //do later due to code above
            compressedStream=compressedObj.getDecodedStream();

            CompressedObjects.extractCompressedObjectOffset(offsetStart, offsetEnd, First, compressedStream, compressedID,offset);

            startID=(String) offsetStart.get(String.valueOf(objectID));

            Extends=compressedObj.getDictionary(PdfDictionary.Extends);
            if(Extends==null) {
                compressedRef = null;
            } else {
                compressedRef = Extends.getObjectRefAsString();
            }

            if(compressedRef!=null) {
                compressedID = Integer.parseInt(compressedRef.substring(0, compressedRef.indexOf(' ')));
            }

        }

        if(!isCached){
            lastCompressedStream=compressedStream;
            lastOffsetStart=offsetStart;
            lastOffsetEnd=offsetEnd;
            lastFirst=First;
        }

        /**put bytes in stream*/
        final int start=First+Integer.parseInt(startID);
        int end=compressedStream.length;
        final String endID=(String) offsetEnd.get(String.valueOf(objectID));
        if(endID!=null) {
            end = First + Integer.parseInt(endID);
        }

        final int streamLength=end-start;
        raw = new byte[streamLength];
        System.arraycopy(compressedStream, start, raw, 0, streamLength);

        pdfObject.setInCompressedStream(true);
        return raw;
    }

    ///////////////////////////////////////////////////////////////////
    /**
     * get postscript data (which may be split across several objects)
     */
    public byte[] readPageIntoStream(final PdfObject pdfObject){

        final byte[][] pageContents= pdfObject.getKeyArray(PdfDictionary.Contents);

        //reset buffer object
        byte[] binary_data = new byte[0];

        //exit on empty
        if(pageContents==null || (pageContents!=null && pageContents.length>0 && pageContents[0]==null)) {
            return binary_data;
        }

        /**read an array*/
        if(pageContents!=null){

            final int count=pageContents.length;

            byte[] decoded_stream_data ;
            PdfObject streamData;

            //read all objects for page into stream
            for(int ii=0;ii<count;ii++) {

                //if(pageContents[ii].length==0)
                //	break;

                //get the data for an object
                //currentPdfFile.resetCache();
                //decoded_stream_data =currentPdfFile.readStream(new String(pageContents[ii]),true);

                streamData=new StreamObject(new String(pageContents[ii]));
                streamData.isDataExternal(pdfObject.isDataExternal());//flag if being read from external stream
                readObject(streamData);

                decoded_stream_data=streamData.getDecodedStream();

                //System.out.println(decoded_stream_data+" "+OLDdecoded_stream_data);
                if(ii==0 && decoded_stream_data!=null) {
                    binary_data = decoded_stream_data;
                } else {
                    binary_data = appendData(binary_data, decoded_stream_data);
                }
            }
        }

        return binary_data;
    }

    /**
     * append into data_buffer by copying processed_data then
     * binary_data into temp and then temp back into binary_data
     * @param binary_data
     * @param decoded_stream_data
     */
    static byte[] appendData(byte[] binary_data, final byte[] decoded_stream_data) {

        if (decoded_stream_data != null){
            final int current_length = binary_data.length + 1;

            //find end of our data which we decompressed.
            int processed_length = decoded_stream_data.length;
            if (processed_length > 0) { //trap error
                while (decoded_stream_data[processed_length - 1] == 0) {
                    processed_length--;
                }

                //put current into temp so I can resize array
                final byte[] temp = new byte[current_length];
                System.arraycopy(binary_data,0, temp,0, current_length - 1);

                //add a space between streams
                temp[current_length - 1] =  ' ';

                //resize
                binary_data = new byte[current_length + processed_length];

                //put original data back
                System.arraycopy(temp, 0, binary_data, 0, current_length);

                //and add in new data
                System.arraycopy(decoded_stream_data,0,binary_data,current_length,processed_length);
            }
        }
        return binary_data;
    }

    public void setCertificate(final Certificate certificate, final PrivateKey key) {
        this.certificate=certificate;
        this.key=key;
    }

    /**
     * read reference table start to see if new 1.5 type or traditional xref
     * @throws PdfException
     */
    public final PdfObject readReferenceTable(final PdfObject linearObj) throws PdfException {

        final PdfObject rootObj= refTable.readReferenceTable(linearObj,this,objectReader);

        final PdfObject encryptObj=refTable.getEncryptionObject();

        if(encryptObj!=null) {
            setupDecryption(encryptObj);
        }

        //will be null if offset table invalid
        ObjLengthTable=offset.calculateObjectLength((int) eof);

        return rootObj;
    }

    public void setupDecryption(final PdfObject encryptObj) throws PdfSecurityException {

        try{
            /**
             * instance as appropriate
             */
            final byte[] ID=refTable.getID();
            if(certificate!=null) {
                decryption = new DecryptionFactory(ID, certificate, key);
            } else {
                decryption = new DecryptionFactory(ID, encryptionPassword);
            }

            //get values
            if(encyptionObj==null){
                encyptionObj=new EncryptionObject(new String(encryptObj.getUnresolvedData()));
                readObject(encyptionObj);
            }

            decryption.readEncryptionObject(encyptionObj);

        }catch(final Error err){

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("No Bouncy castle on classpath "+err);
            }
            throw new RuntimeException("This PDF file is encrypted and JPedal needs an additional library to \n" +
                    "decode on the classpath (we recommend bouncycastle library).\n" +
                    "There is additional explanation at http://www.idrsolutions.com/additional-jars"+ '\n');

        }
    }
}

