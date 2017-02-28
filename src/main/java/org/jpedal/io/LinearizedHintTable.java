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
 * LinearizedHintTable.java
 * ---------------
 */
package org.jpedal.io;

import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Java representation of Linear hint tabke defined in F3/F4 of PDF spec
 */
public class LinearizedHintTable {

    //private PdfObject linearObj;
    //private PdfObject hintObj;

    private final Map startRefs=new HashMap();
    private final Map endRefs=new HashMap();

    private static final int[] mask={255,127,63,31,15,7,3,1};
    private static final int[] shift={0,8,16,24};

    private int[] pageObjectCount;
    private int[] obj;
    private int[] pageLength;

    private long[] pageStart;

    private final FileChannel fos;

    private boolean finishedReading;
    
    public LinearizedHintTable(final FileChannel fos){

        this.fos=fos;

    }

    public void readTable(final PdfObject hintObj, final PdfObject linearObj, final int O, final long Ooffset) {

        //this.hintObj=hintObj;
        //this.linearObj=linearObj;

        final byte[] hintTableData=hintObj.getDecodedStream();

        //int S=hintObj.getInt(PdfDictionary.S);
        final int N=linearObj.getInt(PdfDictionary.N);
        //int L=linearObj.getInt(PdfDictionary.L);

        if(hintTableData!=null && hintTableData.length>0) {
            parseHintTable(N, O, Ooffset, hintTableData);
        }

        //parseSharedObjectHintTable(N, S, hintTableData);

    }

    private void parseHintTable(final int N, final int O, final long Ooffset, final byte[] hintTableData){

        int startByte=0;

        //arrays to populate
        //treat zero as empty and 1 is first page
        pageObjectCount=new int[N+1];
        obj=new int[N+1];
        pageStart=new long[N+1];
        pageLength=new int[N+1];

        //final int[] sharedObjects=new int[N+1];
        //int[] sharedObjectsIdentifier=new int[N+1];
        //int[] objectNumerator=new int[N+1];
        
        /**
         * read page offset hint table header (Appendix F F3 in PDF Ref)
         */

        final boolean showHeader=false;
        if(showHeader) {
            System.out.println("-----header----");
        }
        
        //item 1
        final int leastNumberOfObjects=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
        if(showHeader) {
            System.out.println("1=" + leastNumberOfObjects + " ( " + hintTableData[startByte] + ' ' + hintTableData[startByte + 1] + ' ' + hintTableData[startByte + 2] + ' ' + hintTableData[startByte + 3] + " )");
        }
        startByte += 4;

        //item 2
//        word=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
//        if(showHeader)
//            System.out.println("2="+word+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+ ' ' +hintTableData[startByte+2]+ ' ' +hintTableData[startByte+3]+" )");
        startByte += 4;

        //item 3
        final int bitsNeededforObjectCount=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
        if(showHeader) {
            System.out.println("3=" + bitsNeededforObjectCount + " ( " + hintTableData[startByte] + ' ' + hintTableData[startByte + 1] + " )");
        }
        startByte += 2;

        //item 4
        final int smallestPageSize=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
        if(showHeader) {
            System.out.println("4=" + smallestPageSize + " ( " + hintTableData[startByte] + ' ' + hintTableData[startByte + 1] + ' ' + hintTableData[startByte + 2] + ' ' + hintTableData[startByte + 3] + " )");
        }
        startByte += 4;

        //item 5
        final int bitsNeededforPageSize=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
        if(showHeader) {
            System.out.println("5=" + bitsNeededforPageSize + " ( " + hintTableData[startByte] + ' ' + hintTableData[startByte + 1] + " )");
        }
        startByte += 2;

        //item 6
//        word=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
//        if(showHeader)
//            System.out.println("6="+word+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+ ' ' +hintTableData[startByte+2]+ ' ' +hintTableData[startByte+3]+" )");
        startByte += 4;

        //item 7
//        word=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
//        if(showHeader)
//            System.out.println("7="+word+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+" )");
        startByte += 2;

        //item 8
//        word=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
//        if(showHeader)
//            System.out.println("8="+word+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+ ' ' +hintTableData[startByte+2]+ ' ' +hintTableData[startByte+3]+" )");
        startByte += 4;

        //item 9
//        word=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
//        if(showHeader)
//            System.out.println("9="+word+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+" )");
        startByte += 2;

        //item 10
        //final int bitsNeededforSharedObject=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
        //if(showHeader)
        //    System.out.println("10="+bitsNeededforSharedObject+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+" )");
        startByte += 2;

        //item 11
        //int bitsNeededforSharedObjectIdentifier=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
        //if(showHeader)
        //    System.out.println("11="+bitsNeededforSharedObjectIdentifier+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+" )");
        startByte += 2;

        //item 12
//        int bitsNeededforObjectNumerator=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
//        if(showHeader)
//            System.out.println("12="+bitsNeededforObjectNumerator+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+" )");
        startByte += 2;

        //item 13
//        int demominator=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
//        if(showHeader)
//            System.out.println("13="+demominator+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+" )");
        startByte += 2;


        /**
         * read page offset hint table (Appendix F F4 in PDF Ref)
         */

        int bitReached=startByte<<3; //start of F4 table

        if(showHeader) {
            System.out.println("Object count");
        }

        //object count
        int objectCount=0;
        for(int pageReached=0;pageReached<N;pageReached++){

            pageObjectCount[pageReached+1]=leastNumberOfObjects+getBitsFromByteStream(bitReached,bitsNeededforObjectCount,hintTableData );
            objectCount += pageObjectCount[pageReached+1];
            bitReached += bitsNeededforObjectCount;

        }

        //lookup table of object counts for Page number
        obj[1]=O;

        if(N>1) //catch for single page
        {
            obj[2] = 1;
        }

        for(int pageReached=3;pageReached<N;pageReached++){
            obj[pageReached]=obj[pageReached-1]+pageObjectCount[pageReached-1];
        }

        //align to start of byte
        bitReached=((bitReached+7)>>3)<<3;

        if(showHeader) {
            System.out.println("Object offsets");
        }

        //page offsets
        for(int pageReached=0;pageReached<N;pageReached++){
            pageLength[pageReached+1]=smallestPageSize+getBitsFromByteStream(bitReached,bitsNeededforPageSize,hintTableData );
            bitReached += bitsNeededforPageSize;


        }


        if(showHeader) {
            System.out.println("Page start");
        }

        //and cumulative lookup table
        for(int pageReached=0;pageReached<N;pageReached++){
            if(pageReached==0) {
                pageStart[pageReached + 1] = Ooffset;
            } else {
                pageStart[pageReached + 1] = pageStart[pageReached] + pageLength[pageReached];
            }
        }

        //align to start of byte
        //bitReached=((bitReached+7)>>3)<<3;

        //shared object count
//        for(int pageReached=0;pageReached<N;pageReached++){
//            sharedObjects[pageReached+1]=getBitsFromByteStream(bitReached,bitsNeededforSharedObject,hintTableData );
//            bitReached=bitReached+bitsNeededforPageSize;
//        }

        //align to start of byte
        //bitReached=((bitReached+7)>>3)<<3;

        //shared object identifier (note starts on PAGE 2)
//        for(int pageReached=1;pageReached<N;pageReached++){
//            sharedObjectsIdentifier[pageReached+1]=getBitsFromByteStream(bitReached,bitsNeededforSharedObjectIdentifier,hintTableData );
//            bitReached=bitReached+bitsNeededforPageSize;
//        }

        //align to start of byte
        //bitReached=((bitReached+7)>>3)<<3;

        //object numerators
//        for(int pageReached=0;pageReached<35;pageReached++){
//            objectNumerator[pageReached+1]=getBitsFromByteStream(bitReached,bitsNeededforObjectNumerator,hintTableData );
//            //System.out.println(objectNumerator[pageReached+1]+" / "+demominator);
//            bitReached=bitReached+bitsNeededforPageSize;
//        }


       // int obj=O;
        //show results at end

        /**
        for(int pageReached=1;pageReached<5;pageReached++){
            System.out.println("-------------------------------------------------------------------------------");

            if(pageReached==2)
                obj=1;

            System.out.println("Page "+pageReached+" pageObjectCount="+pageObjectCount[pageReached]+"page="+obj+" 0 R  pageLength="+pageLength[pageReached]+" pageStart="+pageStart[pageReached]+" sharedObjects="+sharedObjects[pageReached]+" sharedObjectsIdentifier="+sharedObjectsIdentifier[pageReached]+" objectCount="+objectCount+" start="+pageStart[pageReached]+" length="+pageLength[pageReached]);
            obj=obj+pageObjectCount[pageReached];

        }  /**/
    }

    //return an arbitary bit value from the stream
    private static int getBitsFromByteStream(final int bitReached, final int bitsNeededforObjectCount, final byte[] hintTableData) {

        final boolean debug=false;//bitsNeededforObjectCount==7;

        int value=0;
        //workout the number of bytes need
        final int startByte=bitReached >>3;
        final int startOffset=bitReached & 7;
        final int bytesNeeded=((bitsNeededforObjectCount+startOffset) >>3)+1;

        int endShift=(bytesNeeded<<3)-startOffset-bitsNeededforObjectCount;

        if(startOffset==0) {
            endShift = ((bytesNeeded << 3) - (bitsNeededforObjectCount)) & 7;
        }

        if(debug) {
            System.out.println("------------------bitReached=" + bitReached + " bitsNeeded=" + (bytesNeeded * 8) + " startOffset=" + startOffset + " endShift=" + endShift);
        }

        //assume less than 32 as we will need to recode otherwise
        if(bytesNeeded>4){

            //
            return 0;
        }else{
            //System.out.println("bytesNeeded="+bytesNeeded+" startByte="+startByte+" "+" startOffset="+startOffset);
            //get the raw bytes
            for(int ii=0;ii<bytesNeeded;ii++){
                int nextValue=(hintTableData[startByte+ii] & 255);

                if(debug) {
                    System.out.println(ii + "=" + nextValue + ' ' + Integer.toBinaryString(nextValue));
                }

                //mask out any unneeded top bytes in first byte
                if(ii==0){
                    nextValue &= mask[startOffset];

                    //System.out.println(ii+" (after mask)="+nextValue+" "+Integer.toBinaryString(nextValue));
                }

                value += (nextValue<<shift[bytesNeeded-(ii+1)]);

            }

            if(debug) {
                System.out.println("total now =" + value + ' ' + Integer.toBinaryString(value));
            }


            //shift down to correct position
            value >>= endShift;

            if(debug) {
                System.out.println(">>>>returns " + value + ' ' + Integer.toBinaryString(value) + " endShift=" + endShift);
            }

            return value;
        }
    }

//    private static void parseSharedObjectHintTable(int N,int S,byte[] hintTableData){
//
//        int startByte=S;
//        long word;
//
//        //arrays to populate
//        //treat zero as empty and 1 is first page
//        int[] sharedObjectsLength=new int[N+1];
//
//        /**
//         * read page offset hint table header (Appendix F F5 in PDF Ref)
//         */
//
//        final boolean showHeader=true;
//        if(showHeader)
//            System.out.println("-----header----");
//
//        //item 1
//        int firstSharedObject=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
//        if(showHeader)
//            System.out.println("firstSharedObject="+firstSharedObject+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+ ' ' +hintTableData[startByte+2]+ ' ' +hintTableData[startByte+3]+" )");
//        startByte=startByte+4;
//
//        //item 2
//        int firstSharedObjectOffset=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
//        if(showHeader)
//            System.out.println("firstSharedObjectOffset="+firstSharedObjectOffset+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+ ' ' +hintTableData[startByte+2]+ ' ' +hintTableData[startByte+3]+" )");
//        startByte=startByte+4;
//
//        //item 3
//        int numberOfSharedObjectsOnFirstPage=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
//        if(showHeader)
//            System.out.println("numberOfSharedObjectsOnFirstPage="+numberOfSharedObjectsOnFirstPage+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+ ' ' +hintTableData[startByte+2]+ ' ' +hintTableData[startByte+3]+" )");
//        startByte=startByte+4;
//
//        //item 4
//        int numberOfSharedEntries=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
//        if(showHeader)
//            System.out.println("numberOfSharedEntries="+numberOfSharedEntries+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+ ' ' +hintTableData[startByte+2]+ ' ' +hintTableData[startByte+3]+" )");
//        startByte=startByte+4;
//
//        //item 5
//        int bitsNeededForSharedObjectGroup=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
//        if(showHeader)
//            System.out.println("bitsNeededForSharedObjectGroup="+bitsNeededForSharedObjectGroup+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+" )");
//        startByte=startByte+2;
//
//        //item 6
//        int smallestSharedGroupObjectSize=((hintTableData[startByte]& 255)<<24)+((hintTableData[startByte+1]& 255)<<16)+((hintTableData[startByte+2]& 255)<<8)+(hintTableData[startByte+3]& 255);
//        if(showHeader)
//            System.out.println("smallestSharedGroupObjectSize="+smallestSharedGroupObjectSize+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+ ' ' +hintTableData[startByte+2]+ ' ' +hintTableData[startByte+3]+" )");
//        startByte=startByte+4;
//
//        //item 7
//        int smallestSharedGroupObjectLength=(((hintTableData[startByte]& 255)<<8)+(hintTableData[startByte+1]& 255));
//        if(showHeader)
//            System.out.println("smallestSharedGroupObjectLength="+smallestSharedGroupObjectLength+" ( "+hintTableData[startByte]+ ' ' +hintTableData[startByte+1]+" )");
//        startByte=startByte+2;
//
//        /**
//         * now read table values
//         */
//
//        //align to start of byte
//        //int bitReached=startByte<<3;
//
//        //need to read page 1 then later pages
//        
//        //shared object lengths
////        for(int pageReached=0;pageReached<N;pageReached++){
////            sharedObjectsLength[pageReached+1]=getBitsFromByteStream(bitReached,smallestSharedGroupObjectLength,hintTableData );
////            bitReached=bitReached+smallestSharedGroupObjectLength;
////        }
//        
////        for(int pageReached=0;pageReached<5;pageReached++)
////            System.out.println(firstSharedObjectOffset+" sharedObjectsLength["+(pageReached+1)+"] ="+sharedObjectsLength[pageReached+1]);
//
//
//        //byte[] buffer=currentPdfFile.getBytesFromPDF((long)firstSharedObjectOffset+40, 200);
//
//        //System.out.println("firstSharedObjectOffset="+firstSharedObjectOffset+">>\n"+new String(buffer));
//
//    }

    /**
     * return ref number for page of -1 if not yet set or page out of range
     * @param rawPage
     * @return
     */
    public int getPageObjectRef(final int rawPage) {

        //allow for data not read yet.
        if(obj==null || obj.length<=rawPage) {
            return -1;
        }

        return obj[rawPage];
        
    }

    /**
     * return raw data for obj or null
     * @param objID
     * @return
     */
    public synchronized byte[] getObjData(final int objID) {

        if(finishedReading) {
            return null;
        }

        final Integer key= objID;

        //allow for data not read yet.
        if(!startRefs.containsKey(key) || !endRefs.containsKey(key)) {
            return null;
        }

        final int start= (Integer) startRefs.get(key);
        final int end= (Integer) endRefs.get(key);
        final int bufSize= end-start+1;

        long size= 0;

        try {
            if(fos.isOpen()) {
                size = fos.size() - 200;
            }
        } catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //

            size=0;
        }

        if(size<end || (end-start<1)){
            return null;
        }

        byte[] data;

        try{
            synchronized (fos){

                final ByteBuffer buf= ByteBuffer.allocateDirect(bufSize);
                fos.read(buf,start);
                buf.clear();
                data = new byte[buf.capacity()];
                buf.get(data, 0, data.length);
            }
        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //

            data=null;
        }

        return data;

    }

    /**
     * store offsets to all objects cached on disk
     * @param ref
     * @param startObjPtr
     * @param endObjPtr
     */
    public void storeOffset(final int ref, final int startObjPtr, final int endObjPtr) {

        startRefs.put(ref, startObjPtr);
        endRefs.put(ref, endObjPtr);
    }

    public void setFinishedReading() {
        finishedReading=true;
    }
}
