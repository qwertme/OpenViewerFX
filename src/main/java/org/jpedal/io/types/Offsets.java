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
 * Offsets.java
 * ---------------
 */
package org.jpedal.io.types;

import java.io.IOException;

import org.jpedal.io.RandomAccessBuffer;
import org.jpedal.utils.*;
import org.jpedal.utils.repositories.*;

/**
 * byte positions of objects in PDF file
 */
public class Offsets extends Vector_Int{

    private boolean refTableInvalid;
    
    /**flag to show if compressed*/
    private Vector_boolean isCompressed=new Vector_boolean(2000);

    /**generation of each object*/
    private Vector_Int generation = new Vector_Int( 2000 );

    /**location of end ref*/
    private Vector_Int xref=new Vector_Int(100);

    public Offsets(final int i) {
        super(i);
    }

    public void addXref(final int pointer) {
        xref.addElement(pointer);
    }
    
    
    /**
     * precalculate sizes for each object
     */
    public int[] calculateObjectLength(final int eof) {

        if(refTableInvalid) {
            return null;
        }
        
        //add eol to refs as catchall
        xref.addElement(eof);

        final int[] xrefs=xref.get();

        //get order list of refs
        final int xrefCount=xrefs.length;
        int[] xrefID=new int[xrefCount];
        for(int i=0;i<xrefCount;i++) {
            xrefID[i] = i;
        }
        xrefID= Sorts.quicksort(xrefs, xrefID);

        //get ordered list of objects in offset order
        final int objectCount=this.getCapacity();

        int[] id=new int[objectCount];
        final int[] offsets=new int[objectCount];

        //read from local copies and pop lookup table
        final int[] off=this.get();
        final boolean[] isComp=isCompressed.get();
        for(int i=0;i<objectCount;i++){
            if(!isComp[i]){
                offsets[i]=off[i];
                id[i]=i;
            }
        }

        id=Sorts.quicksort( offsets, id );

        int i=0;
        //ignore empty values
        while(true){

            if(offsets[id[i]]!=0) {
                break;
            }
            i++;

        }

        /**
         * loop to calc all object lengths
         * */
        int  start=offsets[id[i]],end;

        //find next xref
        int j=0;
        while(xrefs[xrefID[j]]<start+1) {
            j++;
        }

        final int[] ObjLengthTable=new int[objectCount];

        while(i<objectCount-1){

            end=offsets[id[i+1]];
            int objLength=end-start-1;

            //adjust for any xref
            if(xrefs[xrefID[j]]<end){
                objLength=xrefs[xrefID[j]]-start-1;
                while(xrefs[xrefID[j]]<end+1) {
                    j++;
                }
            }
            ObjLengthTable[id[i]]=objLength;
            //System.out.println(id[i]+" "+objLength+" "+start+" "+end);
            start=end;
            while(xrefs[xrefID[j]]<start+1) {
                j++;
            }
            i++;
        }

        //special case - last object

        ObjLengthTable[id[i]]=xrefs[xrefID[j]]-start-1;
        //System.out.println("*"+id[i]+" "+start+" "+xref+" "+eof);

        return ObjLengthTable;
    }

    public void dispose() {
        xref=null;
        
        generation=null;
        isCompressed=null;

    }
    
     /**
     * read table of values
     */
    public int readXRefs(int current, final byte[] Bytes, final int endTable, int i, final long eof, final RandomAccessBuffer pdf_datafile){

        char flag;
        int id,tokenCount,generation,lineLen,startLine,endLine;
        boolean skipNext=false;
        boolean isFirstValue=true;

        final int[] breaks=new int[5];
        final int[] starts=new int[5];

        // loop to read all references
        while (i < endTable) { //exit end at trailer

            startLine=i;
            endLine=-1;

            /**
             * read line locations
             */
            //move to start of value ignoring spaces or returns
            while (Bytes[i] != 10 && Bytes[i] != 13) {
                //scan for %
                if((endLine==-1)&&(Bytes[i]==37)) {
                    endLine = i - 1;
                }

                i++;
            }

            //set end if no comment
            if(endLine==-1) {
                endLine = i - 1;
            }

            //strip any spaces
            while(Bytes[startLine]==32) {
                startLine++;
            }

            //strip any spaces
            while(Bytes[endLine]==32) {
                endLine--;
            }

            i++;

            /**
             * decode the line
             */
            tokenCount=0;
            lineLen=endLine-startLine+1;

            if(lineLen>0){

                //decide if line is a section header or value

                //first count tokens
                int lastChar=1,currentChar;
                for(int j=1;j<lineLen;j++){
                    currentChar=Bytes[startLine+j];

                    if((currentChar==32)&&(lastChar!=32)){
                        breaks[tokenCount]=j;
                        tokenCount++;
                    }else if((currentChar!=32)&&(lastChar==32)){
                        starts[tokenCount]=j;
                    }

                    lastChar=currentChar;
                }

                //update numbers so loops work
                breaks[tokenCount]=lineLen;
                tokenCount++;

                if(tokenCount==1){ //fix for first 2 values on separate lines

                    if(skipNext) {
                        skipNext = false;
                    } else{
                        current= NumberUtils.parseInt(startLine, startLine + breaks[0], Bytes);
                        skipNext=true;
                    }

                }else if (tokenCount == 2){
                    current= NumberUtils.parseInt(startLine, startLine + breaks[0], Bytes);
                }else {

                    id = NumberUtils.parseInt(startLine, startLine + breaks[0], Bytes);
                    generation= NumberUtils.parseInt(startLine + starts[1], startLine + breaks[1], Bytes);

                    flag =(char)Bytes[startLine+starts[2]];

                    if ((flag=='n')) { // only add objects in use

                        /**
                         * assume not valid and test to see if valid
                         */
                        boolean isValid=false;

                        //get bytes
                        int bufSize=20;

                        //adjust buffer if less than 1024 bytes left in file
                        if (id + bufSize > eof) {
                            bufSize = (int) (eof - id);
                        }

                        if(bufSize>0){

                            /** get bytes into buffer */
                            final byte[] buffer = getBytes(id, bufSize,pdf_datafile);

                            //look for space o b j
                            for(int ii=4;ii<bufSize;ii++){
                                if((buffer[ii-3]==32 || buffer[ii-3]==10)&&(buffer[ii-2]==111)&&(buffer[ii-1]==98)&&(buffer[ii]==106)){
                                    isValid=true;
                                    ii=bufSize;
                                }
                            }

                            //check number
                            if(isValid && isFirstValue){

                                isFirstValue=false;

                                if(buffer[0]==48 && buffer[1]!=48 && current==1){
                                    current=0;
                                }else if(buffer[0]==49 && buffer[1]==32){
                                    current=1;
                                }

                            }

                            if(isValid){
                                storeObjectOffset(current, id, generation, false,false);
                                xref.addElement( id);
                            }else{
                                //
                            }
                        }

                        current++; //update our pointer
                    }else if (flag=='f') {
                        current++; //update our pointer
                    }

                }
            }
        }
        return current;
    }

    static byte[] getBytes(final long start, final int count, final RandomAccessBuffer pdf_datafile) {
        
        final byte[] buffer=new byte[count];

        
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

        return buffer;
    }
    
    //////////////////////////////////////////////////////////////////////////
    /**
     * place object details in queue
     */
    void storeObjectOffset(final int current_number, final int current_offset, final int current_generation, final boolean isEntryCompressed, final boolean isBumData)
    {

        /**
         * check it does not already exist
         */
        int existing_generation = 0;
        int offsetNumber=0;

        if(current_number<generation.getCapacity()){
            existing_generation=generation.elementAt( current_number );
            offsetNumber=this.elementAt( current_number ) ;

        }

        //write out if not a newer copy (ignore items from Prev tables if newer)
        //if bum data accept if higher position a swe are trawling file manually anf higher figure probably newer
        if( existing_generation < current_generation  || offsetNumber== 0 || isBumData && (current_offset>this.elementAt(current_number)))
        {
            this.setElementAt( current_offset, current_number );
            generation.setElementAt( current_generation, current_number );
            isCompressed.setElementAt(isEntryCompressed,current_number);
        }else{
            //LogWriter.writeLog("Object "+current_number + ", generation "+
            //current_generation + " already exists as"+
            //existing_generation);
        }
    }
    
    /**
     * general routine to turn reference into id with object name
     */
    public final boolean isCompressed( final int ref)
    {

        return isCompressed.elementAt(ref);
    }
    
    public boolean isRefTableInvalid(){
        return refTableInvalid;
    }

    public void setRefTableInvalid(final boolean value) {
        refTableInvalid=value;
    }
}


