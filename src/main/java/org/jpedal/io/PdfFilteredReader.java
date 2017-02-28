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
 * PdfFilteredReader.java
 * ---------------
 */
package org.jpedal.io;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

//
import org.jpedal.io.filter.*;
import org.jpedal.jbig2.*;

import org.jpedal.objects.raw.*;
import org.jpedal.utils.LogWriter;

/**
 * Adds the abilty to decode streams to the PdfFileReader class
 */
public class PdfFilteredReader {

	private static final int A85=1116165;
	
	private static final int AHx=1120328;
	
	private static final int ASCII85Decode=1582784916;
	
	private static final int ASCIIHexDecode=2074112677;
	
	public static final int CCITTFaxDecode=2108391315;
	
	private static final int CCF=1250070;
	
	private static final int Crypt=1112096855;

	public static final int DCTDecode=1180911742;
	
	public static final int Fl=5692;
	
	public static final int FlateDecode=2005566619;
	
	public static final int JBIG2Decode=1247500931;
	
	public static final int JPXDecode=1399277700;
	
	private static final int LZW=1845799;
	
	private static final int LZWDecode=1566984326;
	
	private static final int RL=8732;
	
	private static final int RunLengthDecode=-1815163937;

    /**list of cached objects to delete*/
    private final Map<String, String> cachedObjects=new HashMap<String, String>();

	private BufferedOutputStream streamCache;

	private BufferedInputStream bis;

    private boolean hasError;

    public byte[] decodeFilters(final PdfObject[] DecodeParmsArray, byte[] data, final PdfArrayIterator filters,
    		final int width, final int height, final String cacheName) throws Exception {

    	streamCache = null;
    	bis = null;

    	final boolean debug = false;

        //get count and set global setting
        final int parmsCount=DecodeParmsArray.length;
        PdfObject DecodeParms=DecodeParmsArray[0];
        
        byte[] globalData=null;//used by JBIG but needs to be read now so we can decode
        if(DecodeParms!=null){
            final PdfObject Globals=DecodeParms.getDictionary(PdfDictionary.JBIG2Globals);
            if(Globals!=null){
                globalData=Globals.getDecodedStream();
            }
        }
        
    	final boolean isCached = (cacheName != null);

    	int filterType;
        final int filterCount=filters.getTokenCount();

        if (debug) {
            System.out.println("=================filterCount=" + filterCount + " DecodeParms=" + DecodeParms);
        }

        int counter=0;
        boolean resetDataToNull ;
    	// allow for no filters
    	if (filterCount>0){

            //set each time for filter
            PdfFilter filter;
            //resetDataToNull=true;

    		if (debug) {
                System.out.println("---------filterCount=" + filterCount + " hasMore" + filters.hasMoreTokens() + " parmsCount=" + parmsCount);
            }

    		/**
			 * apply each filter in turn to data
			 */
    		while (filters.hasMoreTokens()) {

    			filterType = filters.getNextValueAsConstant(true);
                resetDataToNull=false;

                //pick up specific Params if set
                if(parmsCount>1){
                    DecodeParms=DecodeParmsArray[counter];
                    globalData=null;
                    if(DecodeParms!=null){
                        final PdfObject Globals=DecodeParms.getDictionary(PdfDictionary.JBIG2Globals);
                        if(Globals!=null){
                            globalData=Globals.getDecodedStream();
                        }
                    }
                }

                if (debug) {
                    System.out.println("---------filter=" + getFilterName(filterType) + " DecodeParms=" + DecodeParms + ' ' + cacheName + ' ' + isCached);
                }

                if (isCached && cacheName != null && (filterType==Crypt || filterType==DCTDecode || filterType==JPXDecode)){
                
                	counter++;
                	continue;
                }
                
                // handle cached objects
    			if (isCached && cacheName != null) {
                    setupCachedObjectForDecoding(cacheName);
                }

    			// apply decode
    			if (filterType==FlateDecode || filterType==Fl){
    				filter=new Flate(DecodeParms);
    			}else if (filterType==PdfFilteredReader.ASCII85Decode || filterType==PdfFilteredReader.A85) {
                    filter=new ASCII85(DecodeParms);
    			}else if (filterType==CCITTFaxDecode || filterType==CCF) {
                    filter=new CCITT(DecodeParms,width, height);
    			} else if(filterType==LZWDecode || filterType==LZW){
                    filter=new LZW(DecodeParms, width, height);
                    resetDataToNull=true;

    			} else if(filterType==RunLengthDecode || filterType==RL) {
                    filter=new RunLength(DecodeParms);
    			} else if(filterType==JBIG2Decode){
    			    
//    			    filter = new JBIGFilter(DecodeParms);

                    // To work we need to add data as an input to JSD to save the resulting data.
    				//We do not write back to stream.
    				
    				if(data==null){ //hack to read into byte[] and spool back for cache
    					System.err.println("should not come here");
    					data=new byte[bis.available()];
    					bis.read(data);
    					
    					int ptr=-1;
    					//resize as JBIG fussy
    					for (int ii=data.length-1;ii>9;ii--){
    						if(data[ii]=='e' && data[ii+1]=='n' && data[ii+2]=='d' && data[ii+3]=='s' &&
    								data[ii+4]=='t' && data[ii+5]=='r' && data[ii+6]=='e' && data[ii+7]=='a' && data[ii+8]=='m'){
    							ptr=ii-1;
    							ii=-1;
    						}	
    					}
    					
    					if(ptr!=-1){
    						
    						if(data[ptr]==10 && data[ptr-1]==13) {
                                ptr--;
                            }
    						
    						final byte[] tmp=data;
    						data=new byte[ptr];    						
    						System.arraycopy(tmp, 0, data, 0, ptr);
    					}
//    					//
                        data=JBIG2.JBIGDecode(data, globalData);
                        
                        /**/
    					
    					streamCache.write(data);
    					data=null;
    					
    				}else{
    					//
    			        data=JBIG2.JBIGDecode(data, globalData);
    			        
    			        /**/    					
    				}

                    filter = null;

    			} else if (filterType==ASCIIHexDecode || filterType==AHx){
    				filter=new ASCIIHex(DecodeParms);
    			}else if(filterType==Crypt){ //just pass though
                    filter = null;
    			} else {
    				filter = null; // handled elsewhere
                }

                if(filter!=null){
                    try{
                        if (data != null) {
                            data = filter.decode(data);
                        } else if (bis != null) {
                            filter.decode(bis, streamCache, cacheName, cachedObjects);
                        }

                        //flag we may have issues in stream
                        if(!hasError && filter.hasError()) {
                            hasError = true;
                        }

                    }catch(final Exception ee){

                    	if(LogWriter.isOutput()) {
                            LogWriter.writeLog("Exception " + ee + " in " + getFilterName(filterType) + " decompression");
                        }
                        
                        ee.printStackTrace();
                        
                        if(resetDataToNull) {
                            data = null;
                        }
                    }
                }

    			if (isCached) {
    				if (bis != null) {
                        bis.close();
                    }

    				if (streamCache != null) {
    					streamCache.flush();
    					streamCache.close();
    				}
    			}

                counter++;
    		}
    	}

    	return data;
    }

	private static String getFilterName(final int filterType) {
		switch(filterType){
		
		case A85:
			return "A85";
			
		case AHx:
			return "AHx";
			
		case ASCII85Decode:
			return "ASCII85Decode";
			
		case ASCIIHexDecode:
			return "ASCIIHexDecode";
			
		case CCITTFaxDecode:
			return "CCITTFaxDecode";
			
		case CCF:
			return "CCF";
			
		case Crypt:
			return "Crypt";
			
		case DCTDecode:
			return "DCTDecode";
			
		case Fl:
			return "Fl";
			
		case FlateDecode:
			return "FlateDecode";
			
		case JBIG2Decode:
			return "JBIG2Decode";
			
		case JPXDecode:
			return "";
			
		case LZW:
			return "";
			
		case LZWDecode:
			return "";
			
		case RL:
			return "";
			
		case RunLengthDecode:
			return "";
		
			default:
				return "Unknown";
		}
	}

	private void setupCachedObjectForDecoding(final String cacheName) throws IOException {
		// rename file
		final File tempFile2 = File.createTempFile("jpedal", ".raw", new File(ObjectStore.temp_dir));
		cachedObjects.put(tempFile2.getAbsolutePath(), "x"); // store to
																// delete when
																// PDF closed
		ObjectStore.copy(cacheName, tempFile2.getAbsolutePath());
		final File rawFile = new File(cacheName);
		rawFile.delete();

		// where its going after decompression
		streamCache = new BufferedOutputStream(new FileOutputStream(cacheName));

		// where data is coming from
		bis = new BufferedInputStream(new FileInputStream(tempFile2));

	}

    public boolean hasError() {
        return hasError;
    }
}
