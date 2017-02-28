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
 * CommandParser.java
 * ---------------
 */
package org.jpedal.parser;

import java.util.ArrayList;
import org.jpedal.utils.NumberUtils;

public class CommandParser {

    private final byte[] characterStream;

    private int commandID=-1;

    private static final int[] prefixes={60,40}; //important that [ comes before (  '<'=60 '('=40

    private static final int[] suffixes={62,41}; //'>'=62 ')'=41

    private static final int[][] intValues={
                {0,100000,200000,300000,400000,500000,600000,700000,800000,900000},
                {0,10000,20000,30000,40000,50000,60000,70000,80000,90000},
                {0,1000,2000,3000,4000,5000,6000,7000,8000,9000},
                {0,100,200,300,400,500,600,700,800,900},
                {0,10,20,30,40,50,60,70,80,90},
                {0,1,2,3,4,5,6,7,8,9}};

    /**maximum ops*/
    private static final int MAXOPS=50;

    /**lookup table for operands on commands*/
    private int[] opStart= new int[MAXOPS];
    private int[] opEnd= new int[MAXOPS];

    private int operandCount;

    /**current op*/
    private int currentOp;

    public CommandParser(final byte[] characterStr) {
        this.characterStream=characterStr;
    }


    int getCommandValues(int dataPointer, final int streamSize, final int tokenNumber) {

        final boolean debug=false;

        final int count=prefixes.length;
        int nextChar=characterStream[dataPointer],start,end=0;

        commandID=-1;
        final int sLen=characterStream.length;

        int current=nextChar;

        if(nextChar==13 || nextChar==10 || nextChar==32 || nextChar==9 || nextChar==0){

            dataPointer++;

            while(true){ //read next valid char

                if(dataPointer==streamSize) //allow for end of stream
                {
                    break;
                }

                current =characterStream[dataPointer];

                if(current!=13 && current!=10 && current!=32 && current!=9 && current!=0) {
                    break;
                }

                dataPointer++;

            }
        }

        //lose any comments in stream which start %
        while(current==37){

            dataPointer++;
            while(true){ //read next valid char

                if(dataPointer==streamSize) //allow for end of stream
                {
                    break;
                }

                current =characterStream[dataPointer];

                if(current==13 || current==10){

                    //exit at end of comment (shown by line ending)
                    //loop need as can get double spacing (ie debug2/hpbrokenFIle)
                    while(dataPointer+1 <streamSize && characterStream[dataPointer+1]==10){
                        dataPointer++;
                        current =characterStream[dataPointer];
                    }

                    break;
                }

                dataPointer++;

            }

            dataPointer++;

            if(dataPointer>=streamSize) //allow for end of stream
            {
                break;
            }

            current =characterStream[dataPointer];
        }

        if(dataPointer>=streamSize) //allow for end of stream
        {
            return dataPointer;
        }

        /**
         * read in value (note several options)
         */
        boolean matchFound=false;
        final int type=getType(current,  dataPointer);

        if(type==3){ //option - its an aphabetical so may be command or operand values

            start=dataPointer;

            while(true){ //read next valid char

                dataPointer++;
                if((dataPointer)>=sLen) //trap for end of stream
                {
                    break;
                }

                current = characterStream[dataPointer];
                //return,space,( / or [
                if (current == 13 || current == 10 || current == 32 || current == 40 || current == 47 || current == 91 || current == 9 || current=='<') {
                    break;
                }

            }

            end=dataPointer-1;

            if(end>=sLen) {
                return end;
            }

            //move back if ends with / or [
            final int endC=characterStream[end];
            if(endC==47 || endC==91 || endC=='<' || endC=='%') {
                end--;
            }

            //see if command
            commandID=-1;
            if(end-start<3){ //no command over 3 chars long
                //@turn key into ID.
                //convert token to int
                int key=0,x=0;
                for(int i2=end;i2>start-1;i2--){
                    key += (characterStream[i2]<<x);
                    x += 8;
                }
                commandID=Cmd.getCommandID(key);
            }

            /**
             * if command execute otherwise add to stack
             */
            if (commandID==-1) {

                opStart[currentOp]=start;
                opEnd[currentOp]=end;

                if(PdfStreamDecoder.showCommands) {
                    System.out.println(PdfStreamDecoder.indent + generateOpAsString(currentOp, false) + " (value) " + tokenNumber);
                }
                
                currentOp++;
                if (currentOp == MAXOPS) {
                    currentOp = 0;
                }
                operandCount++;
            }else{


                //showCommands=(tokenNumber>6300);
                //this makes rest of page disappear
               // if(tokenNumber>70)
               //	return streamSize;


                if(PdfStreamDecoder.showCommands) {
                    System.out.println(PdfStreamDecoder.indent + Cmd.getCommandAsString(commandID) + " (Command) " + tokenNumber);
                }
                
                //reorder values so work
                if(operandCount>0){

                    final int[] orderedOpStart=new int[MAXOPS];
                    final int[] orderedOpEnd=new int[MAXOPS];
                    int opid=0;
                    for(int jj=this.currentOp-1;jj>-1;jj--){

                        orderedOpStart[opid]=opStart[jj];
                        orderedOpEnd[opid]=opEnd[jj];
                        if(opid==operandCount) {
                            jj = -1;
                        }
                        opid++;
                    }
                    if(opid==operandCount){
                        currentOp--; //decrease to make loop comparison faster
                        for(int jj= MAXOPS-1;jj>currentOp;jj--){

                            orderedOpStart[opid]=opStart[jj];
                            orderedOpEnd[opid]=opEnd[jj];
                            if(opid==operandCount) {
                                jj = currentOp;
                            }
                            opid++;
                        }
                        currentOp++;
                    }

                    opStart=orderedOpStart;
                    opEnd=orderedOpEnd;
                }

                //use negative to flag values found
                return -dataPointer;

            }
        }else if(type!=4){

            start=dataPointer;
           
            //option  << values >>
            //option  [value] and [value (may have spaces and brackets)]
            if(type==1 || type==2){

                boolean inStream=false;
                matchFound=true;

                int last=32;  // ' '=32

                while(true){ //read rest of chars

                    if(last==92 && current==92) //allow for \\  \\=92
                    {
                        last = 120;  //'x'=120
                    } else {
                        last = current;
                    }

                    dataPointer++; //roll on counter

                    if(dataPointer==sLen) //allow for end of stream
                    {
                        break;
                    }

                    //read next valid char, converting CR to space
                    current = characterStream[dataPointer];
                    if(current==13 || current==10 || current==9) {
                        current = 32;
                    }

                    //exit at end
                    boolean isBreak=false;


                    if(current==62 && last==62 &&(type==1))  //'>'=62
                    {
                        isBreak = true;
                    }

                    if(type==2){
                        //stream flags
                        if((current==40)&&(last!=92)) 	//'('=40 '\\'=92
                        {
                            inStream = true;
                        } else if((current==41)&&(last!=92)) {
                            inStream = false;
                        }

                        //exit at end
                        if (!inStream && current==93 && last != 92)	//']'=93
                        {
                            isBreak = true;
                        }
                    }

                    if(isBreak) {
                        break;
                    }
                }

                end=dataPointer;
            }

            if(!matchFound){ //option 3 other braces

                int last=32;
                for(int startChars=0;startChars<count;startChars++){

                    if(current==prefixes[startChars]){
                        matchFound=true;

                        start=dataPointer;

                        int numOfPrefixs=0;//counts the brackets when inside a text stream
                        while(true){ //read rest of chars

                            if((last==92) &&(current==92)) //allow for \\ '\\'=92
                            {
                                last = 120; //'x'=120
                            } else {
                                last = current;
                            }
                            dataPointer++; //roll on counter

                            if(dataPointer==sLen) {
                                break;
                            }
                            current =characterStream[dataPointer]; //read next valid char, converting CR to space
                            if(current==13 || current==10 || current==9) {
                                current = 32;
                            }

                            if(current ==prefixes[startChars] && last!=92) // '\\'=92
                            {
                                numOfPrefixs++;
                            }

                            if ((current == suffixes[startChars])&& (last != 92)){ //exit at end  '\\'=92
                                if(numOfPrefixs==0) {
                                    break;
                                } else{
                                    numOfPrefixs--;

                                }
                            }
                        }
                        startChars=count; //exit loop after match
                    }
                }
                end=dataPointer;
            }

            //option 2 -its a value followed by a deliminator (CR,space,/)
            if(!matchFound){

                if(debug) {
                    System.out.println("Not type 2");
                }

                start=dataPointer;
                final int firstChar=characterStream[start];

                while(true){ //read next valid char
                    dataPointer++;
                    if(dataPointer==sLen) //trap for end of stream
                    {
                        break;
                    }

                    current = characterStream[dataPointer];
                    if (current == 13 || current == 10 || current == 32 || current == 40 || current == 47 || current == 91 || current==9 || (firstChar=='/' && current=='<'))
                        //							// '('=40	'/'=47  '['=91
                    {
                        break;
                    }

                }

                end=dataPointer;

                if(debug) {
                    System.out.println("start=" + start+ ' ' +characterStream[start]);
                    System.out.println("end=" + end+ ' ' +characterStream[dataPointer]);
                }
            }

            if(debug) {
                System.out.println("stored start=" + start + " end=" + end);
            }

            if(end<characterStream.length){
                final int next=characterStream[end];
                if(next==47 || next==91) {
                    end--;
                }
            }

            opStart[currentOp]=start;
            opEnd[currentOp]=end;

            if (PdfStreamDecoder.showCommands) {
                System.out.println(PdfStreamDecoder.indent + generateOpAsString(currentOp, false) + "<<----");
            }
            
            currentOp++;
            if (currentOp == MAXOPS) {
                currentOp = 0;
            }
            operandCount++;

        }

        //increment pointer
        if(dataPointer < streamSize){

            nextChar=characterStream[dataPointer];
            if(nextChar != 47 && nextChar != 40 && nextChar!= 91  && nextChar!= '<'){
                dataPointer++;
            }
        }

        return dataPointer;
    }

    public int getCommandID() {
        return commandID;
    }


    private int getType(final int current, final int dataPointer) {

        int type=0;

        if(current==60 && characterStream[dataPointer+1]==60) //look for <<
        {
            type = 1;
        } else if(current==32) {
            type = 4;
        } else if(current==91) //[
        {
            type = 2;
        } else if(current>=97 && current<=122) //lower case alphabetical a-z
        {
            type = 3;
        } else if(current>=65 && current<=90) //upper case alphabetical A-Z
        {
            type = 3;
        } else if(current==39 || current==34) //not forgetting the non-alphabetical commands '\'-'\"'/*
        {
            type = 3;
        }

        return type;

    }

    /**
     * convert Op value to String
     * @param p is current op number
     * @param loseSlashPrefix
     * @return 
     */
public String generateOpAsString(final int p, final boolean loseSlashPrefix) {

        final byte[] dataStream=characterStream;

        final String s;

        int start=this.opStart[p];

        //remove / on keys
        if(loseSlashPrefix && dataStream[start]==47) {
            start++;
        }

        int end=this.opEnd[p];

        //lose spaces or returns at end
        while((dataStream[end]==32)||(dataStream[end]==13)||(dataStream[end]==10)) {
            end--;
        }

        final int count=end-start+1;

        //discount duplicate spaces
        int spaces=0;
        for(int ii=0;ii<count;ii++){
            if((ii>0)&&((dataStream[start+ii]==32)||(dataStream[start+ii]==13)||(dataStream[start+ii]==10))&&
                    ((dataStream[start+ii-1]==32)||(dataStream[start+ii-1]==13)||(dataStream[start+ii-1]==10))) {
                spaces++;
            }
        }

        final char[] charString=new char[count-spaces];
        int pos=0;

        for(int ii=0;ii<count;ii++){
            if((ii>0)&&((dataStream[start+ii]==32)||(dataStream[start+ii]==13)||(dataStream[start+ii]==10))&&
                    ((dataStream[start+ii-1]==32)||(dataStream[start+ii-1]==13)||(dataStream[start+ii-1]==10)))
            {
            }else{
                if((dataStream[start+ii]==10)||(dataStream[start+ii]==13)) {
                    charString[pos] = ' ';
                } else {
                    charString[pos] = (char) dataStream[start + ii];
                }
                pos++;
            }
        }

        s=String.copyValueOf(charString);

        return s;

    }


    public final float parseFloat(final int id){

        final byte[] stream=characterStream;

        final float f;

        final int start=opStart[id];
        final int charCount=opEnd[id]-start;

        int floatptr=charCount,intStart=0;

        boolean isMinus=false;
        //hand optimised float code
        //find decimal point
        for(int j=charCount-1;j>-1;j--){
            if(stream[start+j]==46){ //'.'=46
                floatptr=j;
                break;
            }
        }

        int intChars=floatptr;
        //allow for minus
        if(stream[start]==43){ //'+'=43
            intChars--;
            intStart++;
        }else if(stream[start]==45){ //'-'=45
            //intChars--;
            intStart++;
            isMinus=true;
        }

        //optimisations
        final int intNumbers=intChars-intStart;
        int decNumbers=charCount-floatptr;

        if(intNumbers>3 || decNumbers>11){ //non-optimised to cover others (tiny decimals on big scaling can add up to a big diff)
            isMinus=false;
            
            f=Float.parseFloat(this.generateOpAsString(id, false));
            
        }else{

            if(decNumbers>6){ //old code used this accuracy so kept to avoid lots of minor changes
                decNumbers=6;
            }

           f = NumberUtils.convertFloatFromStream(stream, start+intStart, start+floatptr, intNumbers, decNumbers);

        }

        if(isMinus) {
            return -f;
        } else {
            return f;
        }
    }

    public float[] getValuesAsFloat() {

        if (this.characterStream[opStart[0]] == 91) { // [0.0 0.0 0.0]

            return readFloatArray();

        } else {
            final float[] op = new float[operandCount];
            for (int i = 0; i < operandCount; i++) {
                op[i] = parseFloat(i);
            }

            return op;
        }

    }

    private float[] readFloatArray() {
        final int start = opStart[0];
        final int end = this.opEnd[0];
        int count = 0;
        int startPtr, endPtr;
        ArrayList values = new ArrayList();
        for (int chars = start + 1; chars < end; chars++) {
            
            char c = (char) characterStream[chars];
            
            //gap
            while (c != '.' && c != '-' && (c < '0' || c > '9')) {
                chars++;
                c = (char) characterStream[chars];
            }
            
            startPtr = chars;
            
            //number
            while (c == '.' || c == '-' || (c >= '0' && c <= '9')) {
                chars++;
                c = (char) characterStream[chars];
            }
            
            endPtr = chars;
            
            count++;
            
            values.add(Float.valueOf(NumberUtils.parseFloat(startPtr, endPtr - startPtr, characterStream)));
            
        }
        
        final float[] op = new float[count];
        for (int i = 0; i < count; i++) {
            op[i] = ((Float) values.get(i)).floatValue();
        }
        return op;
    }

    public String[] getValuesAsString() {

        final String[] op=new String[operandCount];
        for(int i=0;i<operandCount;i++) {
            op[i] = generateOpAsString(i, true);
        }
        return op;
    }


    public final int parseInt(){

        final int start=opStart[0];
        final int end =this.opEnd[0];

        final byte[] stream=characterStream;

        int number=0;
        final int id=0;

        final int charCount= end -start;

        int intStart=0;
        boolean isMinus=false;


        int intChars=charCount;
        //allow for minus
        if(stream[start]==43){ //'+'=43
            intChars--;
            intStart++;
        }else if(stream[start]==45){ //'-'=45
            //intChars--;
            intStart++;
            isMinus=true;
        }

        //optimisations
        final int intNumbers=intChars-intStart;

        if((intNumbers>6)){ //non-optimised to cover others
            isMinus=false;
            number=Integer.parseInt(generateOpAsString(id, false));

        }else{ //optimised lookup version

            int c;

            for(int jj=5;jj>-1;jj--){
                if(intNumbers>jj){
                    c=stream[start+intStart]-48;
                    number += intValues[5-jj][c];
                    intStart++;
                }
            }
        }

        if(isMinus) {
            return -number;
        } else {
            return number;
        }
    }



    public void reset() {
        currentOp=0;
        operandCount=0;
    }

    public int getOperandCount() {
        return operandCount;
    }

    public byte[] getStream() {
        return this.characterStream;
    }

    public int getcurrentOp() {
        return currentOp;
    }
}
