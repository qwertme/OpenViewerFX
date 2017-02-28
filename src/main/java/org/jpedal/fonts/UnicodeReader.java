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
 * UnicodeReader.java
 * ---------------
 */
package org.jpedal.fonts;

import org.jpedal.utils.LogWriter;

class UnicodeReader {
    
    private static final int[] powers={1,16,256,256*16};
    
    int ptr;
    
    final byte[] data;
    
    boolean hasDoubleBytes;
    
    UnicodeReader(final byte[] data){
        
        this.data=data;
    }
    
    /**
     * read unicode translation table
     */
    public String[] readUnicode(){
        
        if(data==null) {
            return null;
        }
        
        int defType = 0;
        
        //initialise unicode holder
        final String[] unicodeMappings = new String[65536];
        
        final int length=data.length;
        
        boolean inDef=false;
        
        //get stream of data
        try {
            
            //read values into lookup table
            while (true) {
                
                while(ptr<length && data[ptr]==9) {
                    ptr++;
                }
                
                if (ptr>=length) {
                    break;
                } else if(ptr+4<length && data[ptr]=='e' && data[ptr+1]=='n' && data[ptr+2]=='d' && data[ptr+3]=='b' && data[ptr+4]=='f'){
                    defType = 0;
                    inDef=false;
                }else if (inDef) {
                    
                    readLineValue(unicodeMappings,defType);
                }
                
                if(ptr>=length){
                    break;
                }else if(data[ptr]=='b' && data[ptr+1]=='e' && data[ptr+2]=='g' && data[ptr+3]=='i' && data[ptr+4]=='n' &&
                        data[ptr+5]=='b' && data[ptr+6]=='f' && data[ptr+7]=='c' && data[ptr+8]=='h' && data[ptr+9]=='a' && data[ptr+10]=='r'){
                    
                    defType = 1;
                    ptr += 10;
                    
                    inDef=true;
                    
                }else if(data[ptr]=='b' && data[ptr+1]=='e' && data[ptr+2]=='g' && data[ptr+3]=='i' && data[ptr+4]=='n' &&
                        data[ptr+5]=='b' && data[ptr+6]=='f' && data[ptr+7]=='r' && data[ptr+8]=='a' && data[ptr+9]=='n' && data[ptr+10]=='g' && data[ptr+11]=='e'){
                    
                    defType = 2;
                    ptr += 11;
                    
                    inDef=true;
                }
                
                ptr++;
            }
            
        } catch (final Exception e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception setting up text object " + e);
            }
            
            //
        }
        
        return unicodeMappings;
    }
    
    private void readLineValue(final String[] unicodeMappings,int type) {
        
        int entryCount= type +1;
        
        final int dataLen=data.length;
        
        int raw;
        
        //read 2 values
        final int[] value=new int[2000];
        boolean isMultipleValues=false;
        
        for(int vals=0;vals<entryCount;vals++){
            
            if(!isMultipleValues){
                while(ptr<data.length && data[ptr]!='<'){ //read up to
                    
                    if(vals==2 && entryCount==3 && data[ptr]=='['){ //mutiple values inside []
                        
                        type =4;
                        
                        int ii=ptr;
                        while(data[ii]!=']'){
                            if(data[ii]=='<') {
                                entryCount++;
                            }
                            
                            ii++;
                        }
                        
                        //needs to be 1 less to make it work
                        entryCount--;
                        
                        //vals=entryCount;
                        //break;
                    }
                    
                    ptr++;
                }
                
                ptr++; //skip past
            }
            
            //find end
            int count=0, charsFound=0;
            
            while(ptr<dataLen && data[ptr]!='>'){
                
                if(data[ptr]!=10 && data[ptr]!=13 && data[ptr]!=32) {
                    charsFound++;
                }
                
                ptr++;
                count++;
                
                //allow for multiple values
                if(charsFound==5){
                    
                    count=4;
                    ptr--;
                    
                    entryCount++;
                    isMultipleValues=true;
                    break;
                }
            }
            
            int pos=0;
            
            for(int jj=0;jj<count;jj++){
                //convert to number
                while(true){
                    raw=data[ptr-1-jj];
                    
                    if(raw!=10 && raw!=13 && raw!=32 ) {
                        break;
                    }
                    
                    jj++;
                }
                
                if(raw>='A' && raw<='F'){
                    raw -= 55;
                }else if(raw>='a' && raw<='f'){
                    raw -= 87;
                }else if(raw>='0' && raw<='9'){
                    raw -= 48;
                }else {
                    throw new RuntimeException("Unexpected number "+(char)raw);
                }
                
                value[vals] += (raw*powers[pos]);
                
                pos++;
            }
        }
        
        //roll to end end so works
        while(ptr<dataLen && (data[ptr]==62 || data[ptr]==32 || data[ptr]==10 || data[ptr]==13 || data[ptr]==']')) {
            ptr++;
        }
        
        ptr--;
        
        //put into array
        fillValues(unicodeMappings, entryCount, value,type);
    }
    
    private void fillValues(final String[] unicodeMappings, final int entryCount, final int[] value, final int type) {
        
        int intValue;
        
        switch(type){
            
            case 1:
                
                if(entryCount==2){
                    if(value[type]>0){
                        unicodeMappings[value[0]]= String.valueOf((char) value[type]);
                        if(value[0]>255) {
                            hasDoubleBytes=true;
                        }
                    }
                    
                }else{
                    
                    final char[] str=new char[entryCount-1];
                    
                    for(int aa=0;aa<entryCount-1;aa++) {
                        str[aa]=(char)value[type +aa];
                    }
                    
                    unicodeMappings[value[0]]= new String(str);
                    if(value[0]>255) {
                        hasDoubleBytes=true;
                    }
                    
                }
                
                break;
                
            case 4:
                
                //ptr++;
                
                int j=2;
                for (int i = value[0]; i < value[1] + 1; i++){
                    
                    
                    if(entryCount>1 && value[0]==value[1]){ //allow for <02> <02> [<0066006C>]
                        
                        unicodeMappings[i]= String.valueOf((char) (value[2]));
                        if(i>255) {
                            hasDoubleBytes=true;
                        }
                        
                        for(int jj=1;jj<entryCount;jj++){
                            unicodeMappings[i] += String.valueOf((char) (value[2+jj]));
                            if(i>255) {
                                hasDoubleBytes=true;
                            }
                        }
                        // System.out.println("val="+value[0]+" "+unicodeMappings[i]+" value[0]="+value[0]+" value[1]="+value[1]+" value[2]="+value[2]+" value[3]="+value[3]);
                        
                    }else{
                        //read next value
                        
                        intValue=value[j];
                        j++;
                        if(intValue>0){ //ignore  0 to fix issue in Dalim files
                            unicodeMappings[i]= String.valueOf((char) (intValue));
                            if(i>255) {
                                hasDoubleBytes=true;
                            }
                        }
                    }
                }
                
                break;
                
            default:
                
                for (int i = value[0]; i < value[1] + 1; i++){
                    intValue=value[type] + i - value[0];
                    if(intValue>0){ //ignore  0 to fix issue in Dalim files
                        unicodeMappings[i]= String.valueOf((char) (intValue));
                        if(i>255) {
                            hasDoubleBytes=true;
                        }
                    }
                }
                
                break;
        }
    }
    
    public boolean hasDoubleByteValues(){
        return hasDoubleBytes;
    }
    
}
