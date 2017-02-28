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
 * Hmtx.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import org.jpedal.utils.LogWriter;


public class Hmtx extends Table {
	
	private int[] hMetrics;
	private short[] leftSideBearing;
    private float scaling=1f/1000f;

    public Hmtx(final FontFile2 currentFontFile, final int glyphCount,int metricsCount, final int maxAdvance){

        scaling=maxAdvance;

        if(metricsCount<0) {
            metricsCount = -metricsCount;
        }
		
		//move to start and check exists
		final int startPointer=currentFontFile.selectTable(FontFile2.HMTX);
		
		int lsbCount=glyphCount-metricsCount;
		
		//System.out.println("start="+Integer.toHexString(startPointer)+" lsbCount="+lsbCount+" glyphCount="+glyphCount+" metricsCount="+metricsCount);
		
		hMetrics = new int[glyphCount];
		leftSideBearing = new short[glyphCount];
		
		int currentMetric=0;
		
		//read 'head' table
		if(startPointer==0){
			if(LogWriter.isOutput()) {
                LogWriter.writeLog("No Htmx table found");
            }
		}else if(lsbCount<0){
			if(LogWriter.isOutput()) {
                LogWriter.writeLog("Invalid Htmx table found");
            }
        }else{
			int i ;
			for (i = 0; i < metricsCount; i++){
				currentMetric=currentFontFile.getNextUint16();
				hMetrics[i] =currentMetric;
				leftSideBearing[i] = currentFontFile.getNextInt16();
				//System.out.println(i+"="+hMetrics[i]+" "+leftSideBearing[i]);
			}
			
			//workout actual number of values in table
			final int tableLength=currentFontFile.getTableSize(FontFile2.HMTX);
			final int lsbBytes=tableLength-(i*4); //each entry above used 4 bytes
			lsbCount=(lsbBytes/2); //each entry contains 2 bytes

            //catch needed for 48789.pdf
            try{
			//read additional lsb entries
			for(int j=i;j<lsbCount;j++){

				hMetrics[j] =currentMetric;
				leftSideBearing[j] = currentFontFile.getFWord();
				//System.out.println((j)+" "+leftSideBearing[j]);
			}
            }catch(final Exception ee){
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception in reading Hmtx " + ee);
                }
            }
		}
		
	}

    public Hmtx() {
    }

    //used by OTF code for aligning CFF font data
    @SuppressWarnings("UnusedDeclaration")
    public short getRAWLSB(final int i){
        if(leftSideBearing==null || i>=leftSideBearing.length) {
            return 0;
        } else {
            return leftSideBearing[i];
        }
    }

    public short getLeftSideBearing(final int i) {

		final int length=hMetrics.length;

		if (i < length) {
			return (short)(hMetrics[i] & 0xffff);
		} else if(leftSideBearing==null){
			return 0;
		}else{

			final int ptr=i-length;
			if(ptr>=0 && ptr<leftSideBearing.length){
				return leftSideBearing[i - hMetrics.length];
			}else{
		        return 0;
			}
		}
	}
	
	@SuppressWarnings("UnusedDeclaration")
    public float getAdvanceWidth(final int i) {
        /**if (i < hMetrics.length) {
			return hMetrics[i] >> 16;
		} else {
			return hMetrics[hMetrics.length - 1] >> 16;
		}*/
        return ( (hMetrics[i]-getLeftSideBearing(i))/scaling);
    }

    public float getWidth(final int i) {
        /**if (i < hMetrics.length) {
			return hMetrics[i] >> 16;
		} else {
			return hMetrics[hMetrics.length - 1] >> 16;
		}*/
        final float w=hMetrics[i];

        
        return ( (w)/scaling);
    }
    
    public float getUnscaledWidth(final int i) {
        
        return hMetrics[i];
    }
}
