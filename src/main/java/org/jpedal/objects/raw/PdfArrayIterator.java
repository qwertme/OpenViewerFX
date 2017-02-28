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
 * PdfArrayIterator.java
 * ---------------
 */
package org.jpedal.objects.raw;

import java.util.Arrays;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.utils.NumberUtils;

/**
 * allow fast access to data from PDF object
 *
 */
public class PdfArrayIterator {

	public static final int TYPE_KEY_INTEGER = 1;
	public static final int TYPE_VALUE_INTEGER = 2;

	final byte[][] rawData;
	
	//used for Font chars
	boolean hasHexChars;
	
	
	int tokenCount,currentToken,spaceChar=-1;
	
	public PdfArrayIterator(final byte[][] rawData) {
		this.rawData=rawData;
		
		if(rawData!=null) {
            tokenCount = rawData.length;
        }
	}

	public boolean hasMoreTokens() {
		return currentToken<tokenCount;
	}

	//return type (ie PdfArrayIterator.TYPE_KEY_INTEGER)
	public int getNextValueType() {
		
		//allow for non-valid
		if(rawData==null || rawData[currentToken]==null || rawData[currentToken].length==0) {
            return PdfDictionary.Unknown;
        } else{ //look at first char
			int firstByte;

            /**
             * assume number and exit as soon as disproved
             */
            final int len=rawData[currentToken].length;
            boolean isNumber=true;
            for(int ii=0;ii<len;ii++){
                firstByte=rawData[currentToken][ii];

                if(firstByte>=47 && firstByte<58){ //is between 0 and 9
				// / or number
                }else{
                    ii=len;
                    isNumber=false;
                }
            }
			
			if(isNumber){
                if(rawData[currentToken][0]!='/') {
                    return TYPE_KEY_INTEGER;
                } else {
                    return TYPE_VALUE_INTEGER;
                }
            }else {
                return PdfDictionary.Unknown;
            }
		}
	}

	/**
	 * should only be used with Font Object
	 */
	public String getNextValueAsFontChar(final int pointer, final boolean containsHexNumbers, final boolean allNumbers) {
		
		String value;
		
        if(currentToken<tokenCount){
			
			//allow for non-valid
			if(rawData==null || rawData[currentToken]==null || rawData[currentToken].length==0) {
                throw new RuntimeException("NullValue exception with PdfArrayIterator");
            }
			
			
			//lose / at start
			final int length=rawData[currentToken].length-1;
			
			final byte[] raw=new byte[length];
			
			System.arraycopy(rawData[currentToken], 1, raw, 0, length);

            //////////////////////////////////////////////////////
			///getNextValueAsFontChar
		    //ensure its a glyph and not a number
			value=new String(raw);
		    value = StandardFonts.convertNumberToGlyph(value,containsHexNumbers,allNumbers);

			final char c=value.charAt(0);
			if (c=='B' || c=='c' || c=='C' || c=='G') {
				int i = 1;
                final int l=value.length();
                while (!hasHexChars && i < l) {
                    hasHexChars = Character.isLetter(value.charAt(i++));
                }
			}
			//////////////////////////////////////////////////////
			
			//see if space
			if(raw.length==5 && raw[0]=='s' && raw[1]=='p' && raw[2]=='a' && raw[3]=='c' && raw[4]=='e') {
                spaceChar = pointer;
            }
			

			currentToken++;
		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }
		
		return value;
	}



	
	public float getNextValueAsFloat() {
		
		if(currentToken<tokenCount){
			
			//allow for non-valid
			if(rawData==null || rawData[currentToken]==null || rawData[currentToken].length==0) {
                throw new RuntimeException("NullValue exception with PdfArrayIterator");
            }
			
			final byte[] raw=rawData[currentToken];
			
			currentToken++;
			
			//return null as 0
			if(raw[0]=='n' && raw[1]=='u' && raw[2]=='l'  && raw[3]=='l' ) {
                return 0;
            } else {
                return NumberUtils.parseFloat(0, raw.length, raw);
            }

		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }
		
	}

    public int getNextValueAsInteger() {

		return getNextValueAsInteger(true);

	}

    public int getNextValueAsInteger(final boolean rollOn) {

		if(currentToken<tokenCount){

			//allow for non-valid
			if(rawData==null || rawData[currentToken]==null || rawData[currentToken].length==0) {
                throw new RuntimeException("NullValue exception with PdfArrayIterator");
            }

			final byte[] raw=rawData[currentToken];

            if(rollOn) {
                currentToken++;
            }

            //skip / which can be on some values
            int i=0;
            if(raw[0]=='/') {
                i++;
            }
			return NumberUtils.parseInt(i, raw.length, raw);

		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }

	}

    public float[] getNextValueAsFloatArray() {

		if(currentToken<tokenCount){

			//allow for non-valid
			if(rawData==null || rawData[currentToken]==null || rawData[currentToken].length==0) {
                throw new RuntimeException("NullValue exception with PdfArrayIterator");
            }

			final byte[] raw=rawData[currentToken];

			currentToken++;

			//return null as 0
			if(raw[0]=='n' && raw[1]=='u' && raw[2]=='l'  && raw[3]=='l' ) {
                return new float[1];
            } else{

                final int length=raw.length;
                int elementCount=1, elementReached=0;
                 /**
                 * first work out number of elements by counting spaces to end
                 */
                for(int ii=1;ii<length;ii++){
                    if((raw[ii]==' ' || raw[ii]==10 || raw[ii]==13)&&(raw[ii-1]!=' ' && raw[ii-1]==10 && raw[ii-1]==13)) {
                        elementCount++;
                    }
                }

                /**
                 * now create and populate
                 */
                final float[] values=new float[elementCount];
                int start;
                for(int ii=0;ii<length;ii++){
                    while(ii<length && (raw[ii]==' ' || raw[ii]==10 || raw[ii]==13)){
                        ii++;
                    }
                    start=ii;
                    while(ii<length && (raw[ii]!=' ' && raw[ii]!=10 && raw[ii]!=13)){
                        ii++;
                    }
                    values[elementReached]= NumberUtils.parseFloat(start, ii, raw);
                    elementReached++;

                }

				return values;
            }

		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }

	}
	
	public int getNextValueAsConstant(final boolean moveToNextAfter) {
		
		if(currentToken<tokenCount){
			
			//allow for non-valid
			if(rawData==null || rawData[currentToken]==null || rawData[currentToken].length==0) {
                throw new RuntimeException("NullValue exception with PdfArrayIterator");
            }
			
			final byte[] raw=rawData[currentToken];

            if(moveToNextAfter) {
                currentToken++;
            }
			
			return PdfDictionary.getIntKey(1,raw.length-1, raw);

		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }
	}

	public int getSpaceChar() {
		return spaceChar;
	}

	public boolean hasHexChars() {
		return hasHexChars;
	}

	public int getTokenCount() {
		return this.tokenCount;
	}

	/** returns the next value as a string, and if <b>rollon</b> is true moves the count onto the next token*/
	public String getNextValueAsString(final boolean rollon) {
		
		String value="";
		
		if(currentToken<tokenCount){
			
			//allow for non-valid
			if(rawData==null) {
                throw new RuntimeException("Null Value exception with PdfArrayIterator rawData=" + Arrays.toString(rawData));
            }
			//else if(rawData[currentToken]==null)
			//	throw new RuntimeException("Null Value exception with PdfArrayIterator rawData="+rawData);
			
			final byte[] raw=rawData[currentToken];
			if(raw!=null) {
                value = new String(raw);
            }

			if(rollon) {
                currentToken++;
            }
		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }
		
		return value;
	}

    /** returns the next value as a byte[], and if <b>rollon</b> is true moves the count onto the next token*/
	public byte[] getNextValueAsByte(final boolean rollon) {

		byte[] value=null;

		if(currentToken<tokenCount){

			//allow for non-valid
			if(rawData==null) {
                throw new RuntimeException("Null Value exception with PdfArrayIterator rawData=" + Arrays.toString(rawData));
            }
			//else if(rawData[currentToken]==null)
			//	throw new RuntimeException("Null Value exception with PdfArrayIterator rawData="+rawData);

			final byte[] raw=rawData[currentToken];
			if(raw!=null){
                final int length=raw.length;
                value=new byte[length];
                System.arraycopy(raw, 0, value, 0, length);
            }

			if(rollon) {
                currentToken++;
            }
		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }

		return value;
	}

    @SuppressWarnings("UnusedDeclaration")
    public int getNextValueAsKey() {

		if(currentToken<tokenCount){

			//allow for non-valid
			if(rawData==null || rawData[currentToken]==null || rawData[currentToken].length==0) {
                throw new RuntimeException("NullValue exception with PdfArrayIterator");
            }

			final byte[] raw=rawData[currentToken];
            currentToken++;
            
            //System.out.println("String="+new String(raw));

            return PdfDictionary.getIntKey(0,raw.length,raw);

		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }

	}
    
    public boolean isNextKeyNull() {

		if(currentToken<tokenCount){

			//allow for non-valid
			return (rawData!=null && rawData[currentToken]==null);

		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }

	}


	public boolean isNextValueRef() {
		
		final boolean isRef;
		
		if(currentToken<tokenCount){
			
			//allow for non-valid
			if(rawData==null || rawData[currentToken]==null || rawData[currentToken].length==0) {
                return false;
            }
			
			final byte[] raw=rawData[currentToken];
			isRef=raw[raw.length-1]=='R';

		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }
		
		return isRef;
	}

	public boolean isNextValueNull() {

		if(currentToken<tokenCount){

			//allow for non-valid
            return rawData == null || rawData[currentToken] == null || rawData[currentToken].length == 0;

		}else {
            throw new RuntimeException("Out of range exception with PdfArrayIterator");
        }

	}
	
    public void resetToStart() {

        if(rawData!=null) {
            tokenCount = rawData.length;
        }

        currentToken=0;
    }
}
