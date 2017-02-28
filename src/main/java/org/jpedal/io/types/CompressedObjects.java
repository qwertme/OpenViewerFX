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
 * CompressedObjects.java
 * ---------------
 */
package org.jpedal.io.types;

import java.util.Map;
import org.jpedal.exception.PdfException;
import org.jpedal.io.RandomAccessBuffer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;

/**
 *
 */
public class CompressedObjects {

    
        /**
     * @param First
     * @param compressedStream
     */
     public static void extractCompressedObjectOffset(final Map offsetStart, final Map offsetEnd, final int First, final byte[] compressedStream,
                                                      final int compressedID, final Offsets offset) {

        String lastKey=null,key,offsetRef ;

        int startKey,endKey,startOff,endOff,id;

        //read the offsets table
        for(int ii=0;ii<First;ii++){

            if(compressedStream.length==0) {
                continue;
            }

            //ignore any gaps between entries
            //(for loop assumes single char and not always correct)
            while(compressedStream[ii]==10 || compressedStream[ii]==13 || compressedStream[ii]==32) {
                ii++;
            }

            /**work out key size*/
            startKey=ii;
            
            if(startKey==First) {
                continue;
            }
            
            while(compressedStream[ii]!=32 && compressedStream[ii]!=13 && compressedStream[ii]!=10){
                ii++;
            }
            endKey=ii-1;

            /**extract key*/
            int length=endKey-startKey+1;
            char[] newCommand=new char[length];

            for(int i=0;i<length;i++) {
                newCommand[i] = (char) compressedStream[startKey + i];
            }

            key =new String(newCommand);

            //track as number for later
            id= NumberUtils.parseInt(startKey, startKey + length, compressedStream);
                
            /**move to offset*/
            while(compressedStream[ii]==32 || compressedStream[ii]==13 || compressedStream[ii]==10) {
                ii++;
            }

            /**get size*/
            startOff=ii;
            while((compressedStream[ii]!=32 && compressedStream[ii]!=13 && compressedStream[ii]!=10)&&(ii<First)){
                ii++;
            }
            endOff=ii-1;

            /**extract offset*/
            length=endOff-startOff+1;
            newCommand=new char[length];
            for(int i=0;i<length;i++) {
                newCommand[i] = (char) compressedStream[startOff + i];
            }

            offsetRef =new String(newCommand);

            /**
             * save values if in correct block (can list items over-written in another compressed obj)
             */
            if(compressedID==offset.elementAt(id)){
                offsetStart.put(key,offsetRef);

                //save end as well
                if(lastKey!=null) {
                    offsetEnd.put(lastKey, offsetRef);
                }

                lastKey=key;
            }
        }
    }
     
     
    public static int readCompressedOffsets(int pntr, int current, final int numbEntries, final int[] fieldSizes, final byte[] xrefs, final Offsets offset, final RandomAccessBuffer pdf_datafile) throws PdfException {

        //now parse the stream and extract values

        final boolean debug=false;

        if(debug) {
            System.out.println("===============read offsets============= current=" + current + " numbEntries=" + numbEntries);
        }

        final int[] defaultValue={1,0,0};

        boolean hasCase0=false;

        for(int i=0;i<numbEntries;i++){

            //read the next 3 values
            final int[] nextValue=new int[3];
            for(int ii=0;ii<3;ii++){
                if(fieldSizes[ii]==0){
                    nextValue[ii]=defaultValue[ii];
                }else{
                    nextValue[ii]=getWord(xrefs,pntr,fieldSizes[ii]);
                    pntr += fieldSizes[ii];
                }
            }

            //handle values appropriately
            final int id;
            int gen;
            switch(nextValue[0]){
                case 0: //linked list of free objects
                    current++;

                    hasCase0=nextValue[1]==0 && nextValue[2]==0;

                    if(debug) {
                        System.out.println("case 0 nextFree=" + nextValue[1] + " gen=" + nextValue[2]);
                    }

                    break;

                case 1: //non-compressed
                    id=nextValue[1];
                    gen=nextValue[2];

                    if(debug) {
                        System.out.println("case 1   current=" + current + " id=" + id + " byteOffset=" + nextValue[1] + " gen=" + nextValue[2]);
                    }
                     
                    //if number equals offsetRef , test if valid
                    boolean refIsvalid=true;
                    if(current==id){
                        refIsvalid=false;

                        //allow for idiot setting in some files
                        //(ie LT2 Protokoll GV Aug 2012_oD_Teil4.pdf)
                        
                        try{
                            //get the data and see if genuine match
                            final int size=20;
                            final byte[] data=new byte[size];
                            
                            pdf_datafile.seek(current);
                            
                            pdf_datafile.read(data); //get next chars
                            
                            //find space
                            int ptr=0;
                            for(int ii=0;ii<size;ii++){
                                
                                if(data[ii]==32 || data[ii]==10 || data[ii]==13){
                                    ptr=ii;
                                    ii=size;

                                }
                            }

                            if(ptr>0){
                                final int ref= NumberUtils.parseInt(0, ptr, data);
                                if(ref==current) {
                                    refIsvalid = true;
                                }
                            }
                        }catch(final Exception ee){
                            refIsvalid=false;

                            if(LogWriter.isOutput()) {
                                LogWriter.writeLog("Ref is invalid "+ee);
                            }
                        }
                    }

                    if(refIsvalid || !hasCase0) {
                        offset.storeObjectOffset(current, id, gen, false, false);
                    }

                    current++;
                    break;

                case 2: //compressed
                    id=nextValue[1];
                    //gen=nextValue[2];

                    if(debug) {
                        System.out.println("case 2  current=" + current + " object number=" + id + " index=" + gen);
                    }

                    offset.storeObjectOffset(current, id, 0, true,false);

                    current++;

                    break;

                default:
                    throw new PdfException("Exception Unsupported Compression mode with value "+nextValue[0]);
            }
        }

        return pntr;
    }
    
    /** Utility method used during processing of type1C files */
    static int getWord(final byte[] content, final int index, final int size) {
        int result = 0;
        for (int i = 0; i < size; i++) {
            result = (result << 8) + (content[index + i] & 0xff);

        }
        return result;
    }


}


