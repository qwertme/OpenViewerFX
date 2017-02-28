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
 * PdfGroupingAlgorithms.java
 * ---------------
 */
package org.jpedal.grouping;

import java.awt.Rectangle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfData;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Fonts;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Sorts;
import org.jpedal.utils.Strip;
import org.jpedal.utils.repositories.Vector_Float;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Object;
import org.jpedal.utils.repositories.Vector_Rectangle;
import org.jpedal.utils.repositories.Vector_String;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

/**
 * Applies heuristics to unstructured PDF text to create content
 */
public class PdfGroupingAlgorithms {
    
    private boolean includeHTMLtags;
    
    private static final String SystemSeparator = System.getProperty("line.separator");
    
    //public PdfGroupingAlgorithms() {}
	
	/** ==============START OF ARRAYS================ */
	/**
	 * content is stored in a set of arrays. We have tried various methods (ie
	 * create composite object, etc) and none are entirely satisfactory. The
	 * beauty of this method is speed.
	 */

	/**
	 * flag to show this item has been merged into another and should be
	 * ignored. This allows us to repeat operations on live elements without
	 * lots of deleting.
	 */
	private boolean[] isUsed;

	/** co-ords of object (x1,y1 is top left) */
	private float[] f_x1, f_x2, f_y1, f_y2;
	
	/**track if we removed space from end*/
	private boolean[] hadSpace;
	
	/**hold colour info*/
	private String[] f_colorTag;
	
	/**hold writing mode*/
	private int[] writingMode;
	
	/** font sizes in pixels */
	private int[] fontSize;

	/** amount of space a space uses in this font/size */
	private float[] spaceWidth;

	/** actual text */
	private StringBuilder[] content;

	/** raw number of text characters */
	private int[] textLength;

	/** ==============END OF ARRAYS================ */

    /**
	 * handle on page data object. We extract data from this into local arrays
	 * and return grouped content into object at end. This is done for speed.
	 */
	private final PdfData pdf_data;

	/** flag to show if output for table is CSV or XHTML */
	private boolean isXHTML = true;

	/** slot to insert next value - used when we split fragments for table code */
	private int nextSlot;

	/** vertical breaks for table calculation */
	private Vector_Int lineBreaks = new Vector_Int();

	/** holds details as we scan lines for table */
	private Vector_Object lines;

	/** lookup table used to sort into correct order for table */
	private Vector_Int lineY2;

	/**
	 * marker char used in content (we bury location for each char so we can
	 * split)
	 */
	private static final String MARKER = PdfData.marker;
	public static final char MARKER2= MARKER.charAt(0);

	/** counters for cols and rows and pointer to final object we merge into */
	private int max_rows, master;
	
	/**flag to show color info is being extracted*/
	private boolean colorExtracted;
	
	/** used to calculate correct order for table lines */
	private int[] line_order;

	/** amount we resize arrays holding content with if no space */
    private static final int increment = 100;

	public static boolean useUnrotatedCoords;

	/**flag to show if tease created on findText*/
	private boolean includeTease;

	/**teasers for findtext*/
	private String[] teasers;

	private final List multipleTermTeasers = new ArrayList();

	private boolean usingMultipleTerms;

    private boolean isXMLExtraction=true;

    /*
      * Variables to allow cross line search results
      */
	/**Value placed between result areas to show they are part of the same result*/
	private static final int linkedSearchAreas=-101;
	
	/** create a new instance, passing in raw data */
	public PdfGroupingAlgorithms(final PdfData pdf_data, final PdfPageData pageData, final boolean isXMLExtraction) {
		this.pdf_data = pdf_data;
	this.isXMLExtraction=isXMLExtraction;
		colorExtracted=pdf_data.isColorExtracted();
    }
	
	/**
	 * workout if we should use space, CR or no separator when joining lines
	 */
    private static String getLineDownSeparator(final StringBuilder rawLine1, final StringBuilder rawLine2, final boolean isXMLExtraction) {

		String returnValue = " "; //space is default

		final boolean hasUnderline = false;

		/**get 2 lines without any XML or spaces so we can look at last char*/
        final StringBuilder line1;
        final StringBuilder line2;
        if(isXMLExtraction){
			line1 = Strip.stripXML(rawLine1,isXMLExtraction);
			line2 = Strip.stripXML(rawLine2,isXMLExtraction);
		}else{
			line1 = Strip.trim(rawLine1);
			line2 = Strip.trim(rawLine2);
		}
		
		/**get lengths and if appropriate perform tests*/
		final int line1Len = line1.length();
		final int line2Len = line2.length();
		//System.out.println(line1Len+" "+line2Len);
		if((line1Len>1)&&(line2Len>1)){

			/**get chars to test*/
			final char line1Char2 = line1.charAt(line1Len - 1);
			final char line1Char1 = line1.charAt(line1Len - 2);
			final char line2Char1 = line2.charAt(0);
			final char line2Char2 = line2.charAt(1);

			//deal with hyphenation first - ignore unless :- or space-
            final String hyphen_values = "";
            if (hyphen_values.indexOf(line1Char2) != -1) {
				returnValue = ""; //default of nothing
				if (line1Char1 == ':') {
                    returnValue = "\n";
                }
				if (line1Char2 == ' ') {
                    returnValue = " ";
                }
                //paragraph breaks if full stop and next line has ascii char or Capital Letter
            } else if (
				((line1Char1 == '.') || (line1Char2 == '.'))
					&& (Character.isUpperCase(line2Char1)
						|| (line2Char1 == '&')
						|| Character.isUpperCase(line2Char2)
						|| (line2Char2 == '&'))){
				if(isXMLExtraction) {
                    returnValue = "<p></p>\n";
                } else {
                    returnValue="\n";
                }
			}

		}
		
		//add an underline if appropriate
		if (hasUnderline){
			if(isXMLExtraction) {
                returnValue += "<p></p>\n";
            } else {
                returnValue += '\n';
            }
		}
		
		return returnValue;
	}

	/**
	 * remove shadows from text created by double printing of text and drowned
	 * items where text inside other text
	 */
	private void cleanupShadowsAndDrownedObjects(final boolean avoidSpaces) {

		//get list of items
		final int[] items = getUnusedFragments();
		final int count = items.length;
		int c, n;
		String separator;
        float diff;

        //work through objects and eliminate shadows or roll together overlaps
		for (int p = 0; p < count; p++) {

			//master item
			c = items[p];

			//ignore used items
			if (!isUsed[c]) {

				//work out mid point in text
				float midX = (f_x1[c] + f_x2[c]) / 2;
				float midY = (f_y1[c] + f_y2[c]) / 2;
				
				for (int p2 = p + 1;p2 < count;p2++) {

					//item to test against
					n = items[p2];
					if ((!isUsed[n]) && (!isUsed[c])) {

						float fontDiff=this.fontSize[n]-fontSize[c];
						if(fontDiff<0) {
                            fontDiff=-fontDiff;
                        }

                        diff = (f_x2[n] - f_x1[n]) - (f_x2[c] - f_x1[c]);
                        if(diff<0) {
                            diff=-diff;
                        }

                        /** stop spurious matches on overlapping text*/
						if (fontDiff==0 && (midX > f_x1[n])&& (midX < f_x2[n])
							&& (diff< 10)
							&& (midY < f_y1[n])&& (midY > f_y2[n])) {
							
							isUsed[n] = true;
							
							//pick up drowned text items (item inside another)			
						} else {
				
							final boolean a_in_b =
								(f_x1[n] > f_x1[c])&& (f_x2[n] < f_x2[c])
									&& (f_y1[n] < f_y1[c])&& (f_y2[n] > f_y2[c]);
							final boolean b_in_a =
								(f_x1[c] > f_x1[n])&& (f_x2[c] < f_x2[n])
									&& (f_y1[c] < f_y1[n])&& (f_y2[c] > f_y2[n]);
							
							//merge together
							if (a_in_b || b_in_a) {
								//get order right - bottom y2 underneath
								if (f_y2[c] > f_y2[n]) {
									separator =getLineDownSeparator(content[c],content[n],isXMLExtraction);
									if((!avoidSpaces)||(separator.indexOf(' ')==-1)){
										merge(c,n,separator,true);
									}
								} else {
									separator =getLineDownSeparator(content[n],content[c],isXMLExtraction);
									if(!avoidSpaces || separator.indexOf(' ')==-1){
										merge(n,c,separator,true);
									}
								}
								
								//recalculate as may have changed
								midX = (f_x1[c] + f_x2[c]) / 2;
								midY = (f_y1[c] + f_y2[c]) / 2;
								
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * general routine to see if we add a space between 2 text fragments
	 */
    private String isGapASpace(final int c, final int l, final float actualGap, final boolean addMultiplespaceXMLTag, final int writingMode) {
		String sep = "";
		float gap;

		//use smaller gap
		final float gapA = spaceWidth[c] * fontSize[c];
		final float gapB = spaceWidth[l] * fontSize[l];

		if (gapA > gapB) {
            gap = gapB;
        } else {
            gap = gapA;
        }

        gap = (actualGap / (gap / 1000));

        //Round values to closest full integer as float -> int conversion rounds down
        if(gap > 0.51f && gap<1) {
            gap = 1;
        }

        final int spaceCount = (int) gap;

		if (spaceCount > 0) {
            sep = " ";
        }

		/** add an XML tag to flag multiple spaces */
		if (spaceCount > 1 && addMultiplespaceXMLTag && writingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT) {
            sep = " <SpaceCount space=\"" + spaceCount + "\" />";
        }

		return sep;
	}

	/**
	 * merge 2 text fragments together and update co-ordinates
	 */
    private void merge(final int m, final int c, final String separator, final boolean moveFont) {

			//update co-ords
			if (f_x1[m] > f_x1[c]) {
                f_x1[m] = f_x1[c];
            }
			if (f_y1[m] < f_y1[c]) {
                f_y1[m] = f_y1[c];
            }
			if (f_x2[m] < f_x2[c]) {
                f_x2[m] = f_x2[c];
            }
			if (f_y2[m] > f_y2[c]) {
                f_y2[m] = f_y2[c];
            }

			if(isXMLExtraction){
				String test=Fonts.fe;

				//add color tag if needed and changes
				if(colorExtracted) {
                    test=Fonts.fe+GenericColorSpace.ce;
                }

				//move </Font> if needed and add separator
				if ((moveFont) && (content[m].toString().lastIndexOf(test)!=-1)) {
					final String master = content[m].toString();
					content[m] =new StringBuilder(master.substring(0, master.lastIndexOf(test)));
					content[m].append(separator);
					content[m].append(master.substring(master.lastIndexOf(test)));	
				} else{
					content[m].append(separator);	
				}

                //Only map out space if text length is longer than 1
				if(textLength[c]>1 && content[m].toString().endsWith(" ")){
					content[m].deleteCharAt(content[m].lastIndexOf(" "));
				}
				//use font size of second text (ie at end of merged text)
				fontSize[m] = fontSize[c];
				
				//Remove excess / redundent xml tags
				if((content[c].indexOf("<color")!=-1 && content[m].indexOf("<color")!=-1) && 
					(content[c].toString().startsWith(content[m].substring(content[m].lastIndexOf("<color"), content[m].indexOf(">", content[m].lastIndexOf("<color")))) &&
							content[m].lastIndexOf("</color>")+7==content[m].lastIndexOf(">"))){
						content[c].replace(content[c].indexOf("<color"), content[c].indexOf(">")+1, "");
						content[m].replace(content[m].lastIndexOf("</color>"), content[m].lastIndexOf("</color>")+8, "");
					}

				if((content[c].indexOf("<font")!=-1 && content[m].indexOf("<font")!=-1) && 
					(content[c].toString().startsWith(content[m].substring(content[m].lastIndexOf("<font"), content[m].indexOf(">",content[m].lastIndexOf("<font")))) &&
							content[m].lastIndexOf("</font>")+6==content[m].lastIndexOf(">"))){
						content[c].replace(content[c].indexOf("<font"), content[c].indexOf(">")+1, "");
						content[m].replace(content[m].lastIndexOf("</font>"), content[m].lastIndexOf("</font>")+7, "");
					}
				
				content[m] = content[m].append(content[c]);
				
				//track length of text less all tokens
				textLength[m] += textLength[c];

				//set objects to null to flush and log as used
				isUsed[c] = true;		
				content[c] = null;
			}else{

				//use font size of second text (ie at end of merged text)
				fontSize[m] = fontSize[c];

				//add together
				content[m] = content[m].append(separator).append(content[c]);

				//track length of text less all tokens
				textLength[m] += textLength[c];

				//set objects to null to flush and log as used
				isUsed[c] = true;		
				content[c] = null;
			}
	}
	
	/**
	 * remove width data we may have buried in data
	 */
    private void removeEncoding() {

		// get list of items
		final int[] items = getUnusedFragments();
		int current;

		// work through objects and eliminate shadows or roll together overlaps
        for (final int item : items) {

            // master item
            current = item;

            // ignore used items and remove widths we hid in data
            if (!isUsed[current]) {
                content[current] = removeHiddenMarkers(current);
            }
        }
	}

	/**
	 * put raw data into Arrays for quick merging breakup_fragments shows if we
	 * break on vertical lines and spaces
	 */
    private void copyToArraysPartial(final int minX, final int minY, final int maxX, final int maxY) {

		colorExtracted=pdf_data.isColorExtracted();
		
		final int count = pdf_data.getRawTextElementCount();

		//local lists for faster access
		final boolean[] isUsed = new boolean[count];
		final int[] fontSize = new int[count];
		final int[] writingMode=new int[count];
		final float[] spaceWidth = new float[count];
		final StringBuilder[] content = new StringBuilder[count];
		final int[] textLength = new int[count];

		final float[] f_x1 = new float[count];
		final String[] f_colorTag=new String[count];
		final float[] f_x2 = new float[count];
		final float[] f_y1 = new float[count];
		final float[] f_y2 = new float[count];
		
		//set defaults and calculate dynamic values
		//int text_length;
//		count = count-increment;
        float x1,x2,y1,y2;
		//float last_pt,min,max,pt,linePos,character_spacing;
		//String raw, char_width = "",currentColor;
		//StringBuilder text = new StringBuilder();

		int currentPoint = 0;
		
		//set values
		for (int i = 0; i < count; i++) {
            
			//extract values
			//character_spacing = pdf_data.f_character_spacing[i];
			//raw = pdf_data.contents[i];
			x1 = pdf_data.f_x1[i];
			//currentColor=pdf_data.colorTag[i];
			x2 = pdf_data.f_x2[i];
			y1 = pdf_data.f_y1[i];
			y2 = pdf_data.f_y2[i];
			//text_length = pdf_data.text_length[i];
			final int mode=pdf_data.f_writingMode[i];

			boolean accepted = false;
			float height;
            
            switch (mode) {
                case PdfData.HORIZONTAL_LEFT_TO_RIGHT:
                case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                    height = y1-y2;
                    if ((((minX < x1 && x1 < maxX) || (minX < x2 && x2 < maxX)) || //Area contains the x1 or x2 coords
                            ((x1 < minX && minX < x2) || (x1 < maxX && maxX < x2)) //Area is within the x1 and x2 coords
                            )
                            && (minY < y2 + (height / 4) && y2 + (height * 0.75) < maxY) //Area also contains atleast 3/4 of the text y coords
                            ) {
                        accepted = true;
                    }
                    break;
                case PdfData.VERTICAL_BOTTOM_TO_TOP:
                case PdfData.VERTICAL_TOP_TO_BOTTOM:
                    height = x2-x1;
                    if ((((minY < y1 && y1 < maxY) || (minY < y2 && y2 < maxY)) || //Area contains the x1 or x2 coords
                            ((y2 < minY && minY < y1) || (y2 < maxY && maxY < y1)) //Area is within the x1 and x2 coords
                            )
                            && (minX < x1 + (height / 4) && x1 + (height * 0.75) < maxX) //Area also contains atleast 3/4 of the text y coords
                            ) {
                        accepted = true;
                    }
                    break;
            }
			//if at least partly in the area, process
			
			
			if(accepted){
                
                content[currentPoint] = new StringBuilder(pdf_data.contents[i]);

				fontSize[currentPoint] = pdf_data.f_end_font_size[i];
				writingMode[currentPoint]=pdf_data.f_writingMode[i];
				f_x1[currentPoint] = pdf_data.f_x1[i];
				f_colorTag[currentPoint]=pdf_data.colorTag[i];
				f_x2[currentPoint] = pdf_data.f_x2[i];
				f_y1[currentPoint] = pdf_data.f_y1[i];
				f_y2[currentPoint] = pdf_data.f_y2[i];
				
				spaceWidth[currentPoint] = pdf_data.space_width[i];
				textLength[currentPoint] = pdf_data.text_length[i];
				
				StringBuilder startTags = new StringBuilder(content[currentPoint].toString().substring(0, content[currentPoint].toString().indexOf(MARKER)));
				final String contentText = content[currentPoint].toString().substring(content[currentPoint].toString().indexOf(MARKER), content[currentPoint].toString().indexOf('<', content[currentPoint].toString().lastIndexOf(MARKER)));
				String endTags = content[currentPoint].toString().substring(content[currentPoint].toString().lastIndexOf(MARKER));
				//Skips last section of text
				endTags = endTags.substring(endTags.indexOf('<'));
				
				final StringTokenizer tokenizer = new StringTokenizer(contentText, MARKER);
				boolean setX1 = true;
				float width = 0;
				
				while(tokenizer.hasMoreTokens()){
					
					String token = tokenizer.nextToken();
					final float xCoord = (Float.parseFloat(token));
					
					token = tokenizer.nextToken();
					width = Float.parseFloat(token);
					
					token = tokenizer.nextToken();
					final String character = token;
					
					if(setX1){
						if ((mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT || mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
							f_x1[currentPoint] = xCoord;
						}else{
							f_y2[currentPoint] = xCoord;
						}
						setX1 = false;
					}
					
					if ((mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT || mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
						f_x2[currentPoint] = xCoord;
					}else{
						f_y1[currentPoint] = xCoord;
					}
					
                    boolean storeValues = false;
					if ((mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT || mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
                        if(minX<xCoord && (xCoord+width)<maxX){
                            storeValues = true;
                        }
                    }else{
                        if(minY<xCoord && (xCoord+width)<maxY){
                            storeValues = true;
                        }
                    }
					if(storeValues){
						startTags.append(MARKER);
						startTags.append(xCoord); //Add X Coord
						
						startTags.append(MARKER);
						startTags.append(width); //Add Width
						
						startTags.append(MARKER);
						startTags.append(character); //Add Letter
						
						
					}
					
				}
				
				content[currentPoint] = new StringBuilder(startTags.append(endTags).toString());
              
				if ((mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT || mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
					f_x2[currentPoint] += width;
				}else{
					f_y1[currentPoint] += width;
				}
				
				currentPoint++;
			}
		}
		
		this.isUsed = new boolean[currentPoint];
		this.fontSize = new int[currentPoint];
		this.writingMode=new int[currentPoint];
		this.spaceWidth = new float[currentPoint];
		this.content = new StringBuilder[currentPoint];
		this.textLength = new int[currentPoint];

		this.f_x1 = new float[currentPoint];
		this.f_colorTag=new String[currentPoint];
		this.f_x2 = new float[currentPoint];
		this.f_y1 = new float[currentPoint];
		this.f_y2 = new float[currentPoint];
		
		for(int i=0; i!=currentPoint; i++){
			this.isUsed[i] = isUsed[i];
			this.fontSize[i] = fontSize[i];
			this.writingMode[i]=writingMode[i];
			this.spaceWidth[i] = spaceWidth[i];
			this.content[i] = content[i];
			this.textLength[i] = textLength[i];

			this.f_x1[i] = f_x1[i];
			this.f_colorTag[i]=f_colorTag[i];
			this.f_x2[i] = f_x2[i];
			this.f_y1[i] = f_y1[i];
			this.f_y2[i] = f_y2[i];
		}
	}
	
	/**
	 * put raw data into Arrays for quick merging breakup_fragments shows if we
	 * break on vertical lines and spaces
	 */
    private void copyToArrays() {

		colorExtracted=pdf_data.isColorExtracted();
		
		final int count = pdf_data.getRawTextElementCount();

		//local lists for faster access
		isUsed = new boolean[count];
		fontSize = new int[count];
		writingMode=new int[count];
		spaceWidth = new float[count];
		content = new StringBuilder[count];
		textLength = new int[count];

		f_x1 = new float[count];
		f_colorTag=new String[count];
		f_x2 = new float[count];
		f_y1 = new float[count];
		f_y2 = new float[count];
		
		//set values
		for (int i = 0; i < count; i++) {
			content[i] = new StringBuilder(pdf_data.contents[i]);

			fontSize[i] = pdf_data.f_end_font_size[i];
			writingMode[i]=pdf_data.f_writingMode[i];
			f_x1[i] = pdf_data.f_x1[i];
			f_colorTag[i]=pdf_data.colorTag[i];
			f_x2[i] = pdf_data.f_x2[i];
			f_y1[i] = pdf_data.f_y1[i];
			f_y2[i] = pdf_data.f_y2[i];
			
			spaceWidth[i] = pdf_data.space_width[i];
			textLength[i] = pdf_data.text_length[i];
		}
	}
	
	/**
	 * get list of unused fragments and put in list
	 */
	private int[] getUnusedFragments() {
		final int total_fragments = isUsed.length;

		//get unused item pointers
		int ii = 0;
		final int[] temp_index = new int[total_fragments];
		for (int i = 0; i < total_fragments; i++) {
			if (!isUsed[i]) {
				temp_index[ii] = i;
				ii++;
			}
		}
		
		//put into correctly sized array
		final int[] items = new int[ii];
        System.arraycopy(temp_index, 0, items, 0, ii);
		return items;
	}


	/**
	 * strip the hidden numbers of position we encoded into the data
	 * (could be coded to be faster by not using Tokenizer)
	 */
	private StringBuilder removeHiddenMarkers(final int c) {

		//make sure has markers and ignore if not
		if (content[c].indexOf(MARKER) == -1) {
            return content[c];
        }
		
		//strip the markers
		final StringTokenizer tokens =new StringTokenizer(content[c].toString(), MARKER, true);
		String temp;
        StringBuilder processedData = new StringBuilder();
		
		//with a token to make sure cleanup works
		while (tokens.hasMoreTokens()) {

			//strip encoding in data
			temp = tokens.nextToken(); //see if first marker
			
			if (temp.equals(MARKER)) {
				tokens.nextToken(); //point character starts
				tokens.nextToken(); //second marker
				tokens.nextToken(); //width
				tokens.nextToken(); //third marker

				//put back chars
				processedData = processedData.append(tokens.nextToken());
				
			} else {
                processedData = processedData.append(temp);
            }
		}
		
		return processedData;
	}

    /**
     * sets if we include HTML in teasers
     * (do we want this is <b>word</b> or this is word as teaser)
     * @param value
     */
    public void setIncludeHTML(final boolean value) {
        includeHTMLtags=value;
    }
	
	/**
	 * method to show data without encoding
	 */
	public static String removeHiddenMarkers(final String contents) {

		//trap null
		if(contents==null) {
            return null;
        }
		
		//run though the string extracting our markers

		//make sure has markers and ignore if not
		if (!contents.contains(MARKER)) {
            return contents;
        }

		//strip the markers
		final StringTokenizer tokens = new StringTokenizer(contents, MARKER, true);
		String temp_token;
        StringBuilder processed_data = new StringBuilder();
		
		//with a token to make sure cleanup works
		while (tokens.hasMoreTokens()) {

			//encoding in data
			temp_token = tokens.nextToken(); //see
																		 // if
																		 // first
																		 // marker
			if (temp_token.equals(MARKER)) {
				tokens.nextToken(); //point character starts
				tokens.nextToken(); //second marker
				tokens.nextToken(); //width
				tokens.nextToken(); //third marker

				//put back chars
				processed_data = processed_data.append(tokens.nextToken());
				//value
			} else {
                processed_data = processed_data.append(temp_token);
            }
		}
		return processed_data.toString();
	}

	/**
	 * Method to try and find vertical lines in close data
	 * (not as efficient as it could be)
	 * @throws PdfException
	 */
	private void findVerticalLines(final float minX, final float minY, final float maxX, final float maxY, final int currentWritingMode) throws PdfException {

		//hold counters on all x values
		final HashMap xLines = new HashMap();

		//counter on most popular item
		int most_frequent = 0;
        final int count = pdf_data.getRawTextElementCount();
        float x1, x2, y1, y2;
		String raw;

		for (int i = 0; i < count; i++) {
			float currentX = 0, lastX;
			Integer intX;

			//extract values for data
			raw = this.pdf_data.contents[i];

			/**
			 * set pointers so left to right text
			 */
			if(currentWritingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT){
				x1=this.f_x1[i];
				x2=this.f_x2[i];
				y1=this.f_y1[i];
				y2=this.f_y2[i];
			}else if(currentWritingMode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
				x2=this.f_x1[i];
				x1=this.f_x2[i];
				y1=this.f_y1[i];
				y2=this.f_y2[i];
			}else if(currentWritingMode==PdfData.VERTICAL_BOTTOM_TO_TOP){
				x1=this.f_y1[i];
				x2=this.f_y2[i];
				y1=this.f_x2[i];
				y2=this.f_x1[i];
			}else if(currentWritingMode==PdfData.VERTICAL_TOP_TO_BOTTOM){
				x1=this.f_y2[i];
				x2=this.f_y1[i];
				y2=this.f_x1[i];
				y1=this.f_x2[i];
			}else{
				throw new PdfException("Illegal value "+currentWritingMode+"for currentWritingMode");
			}
			
			//if in the area, process
			if ((x1 > minX - .5)&& (x2 < maxX + .5)&& (y2 > minY - .5)&& (y1 < maxY + .5)) {

				//run though the string extracting our markers to get x values
				final StringTokenizer tokens =new StringTokenizer(raw, MARKER, true);
				String value, lastValue = "";
				Object currentValue;

				while (tokens.hasMoreTokens()) {

					//encoding in data
					value = tokens.nextToken(); //see if first marker
					if (value.equals(MARKER)) {

						value = tokens.nextToken(); //point character starts

						if (!value.isEmpty()) {

							lastX = currentX;
							currentX = Float.parseFloat(value);
							try {

								//add x to list or increase counter at start
								// or on space
								//add points either side of space
								if (lastValue.isEmpty() || (lastValue.indexOf(' ') != -1)) {

									intX = (int) currentX;
									currentValue = xLines.get(intX);
									if (currentValue == null) {
										xLines.put(intX, 1);
									} else {
										int countReached = (Integer) currentValue;
										countReached++;

										if (countReached > most_frequent) {
                                            most_frequent = countReached;
                                        }

										xLines.put(intX, countReached);
									}

									//work out the middle
									final int middle =(int) (lastX+ ((currentX - lastX) / 2));

									if (lastX != 0) {
										intX = middle;
										currentValue = xLines.get(intX);
										if (currentValue == null) {
											xLines.put(intX, 1);
										} else {
											int count_reached = (Integer) currentValue;
											count_reached++;

											if (count_reached > most_frequent) {
                                                most_frequent = count_reached;
                                            }

											xLines.put(intX, count_reached);
										}
									}
								}

							} catch (final Exception e) {
								LogWriter.writeLog(
									"Exception " + e + " stripping x values");
							}
						}

						tokens.nextToken(); //second marker
						tokens.nextToken(); //glyph  width
						tokens.nextToken(); //third marker
						value = tokens.nextToken(); //put back chars
						lastValue = value;

					}
				}
			}
		}

		//now analyse the data
		final Iterator keys = xLines.keySet().iterator();
		final int minimum_needed =  most_frequent / 2;

		while (keys.hasNext()) {
			final Integer current_key = (Integer) keys.next();
			final int current_count = (Integer) xLines.get(current_key);

			if (current_count > minimum_needed) {
                lineBreaks.addElement(current_key);
            }

		}
	}

        private boolean isFragmentWithinArea(final Fragment fragment, final float minX, final float minY, final float maxX, final float maxY){
            
            //if at least partly in the area, process
            if ((fragment.getWritingMode() == PdfData.HORIZONTAL_LEFT_TO_RIGHT || fragment.getWritingMode() == PdfData.HORIZONTAL_RIGHT_TO_LEFT)) {

                final float textHeight = fragment.getY1() - fragment.getY2();

                if (((fragment.getY2() > minY && fragment.getY1() < maxY) || //Check is file is enclosed by highlight
                        ((fragment.getY2() > minY && maxY - fragment.getY2() > textHeight * 0.5) || (fragment.getY1() < maxY && fragment.getY1() - minY > textHeight * 0.5))) && //Check is partially enclosed by highlight
                        (minX + maxX) > 0 && //Widt of area is not 0. This case was found in torture.pdf
                        !(fragment.getX2() < minX) && !(fragment.getX1() > maxX)) { //Ensure the text is within the x axis
                    return true;
                }
            } else if (((fragment.getWritingMode() == PdfData.VERTICAL_BOTTOM_TO_TOP || fragment.getWritingMode() == PdfData.VERTICAL_TOP_TO_BOTTOM))
                    && (fragment.getX1() > minX && fragment.getX2() < maxX && fragment.getY1() > minY && fragment.getY2() < maxY)) {
                return true;
            }
            return false;
        }
    /**
     * Method splitFragments adds raw fragments to processed fragments breaking
     * up any with vertical lines through or what looks like tabbed spaces
     *
     * @throws PdfException
     */
    private void copyToArrays(
            final float minX, final float minY, final float maxX, final float maxY,
            final boolean keepFont, final boolean breakOnSpace, final boolean findLines, final String punctuation, final boolean isWordlist) throws PdfException {

        final boolean debugSplit = false;

        //initialise local arrays allow for extra space
        int count = pdf_data.getRawTextElementCount() + increment;

        initArrays(count);

        //flag to find lines based on orientation of first text item*/
        boolean linesScanned = false;

        //set defaults and calculate dynamic values
        count -= increment;
        float last_pt, min, max, pt, linePos;
        String char_width = "";
        StringBuilder text = new StringBuilder();
        Fragment fragment;
        //work through fragments
        for (int i = 0; i < count; i++) {
            
            fragment = new Fragment(pdf_data, i);
            
            if (debugSplit) {
                System.out.println("raw data=" + fragment.getRawData());
                System.out.println("text data=" + PdfGroupingAlgorithms.removeHiddenMarkers(fragment.getRawData()));
            }

            if (isFragmentWithinArea(fragment, minX, minY, maxX, maxY)) {

                /**
                 * find lines
                 */
                //look for possible vertical or horizontal lines in the data
                if (!linesScanned && findLines) {
                    findVerticalLines(minX, minY, maxX, maxY, fragment.getWritingMode());
                    linesScanned = true;
                }

                //initialise pointers and work out an 'average character space'
                if (fragment.getWritingMode() == PdfData.HORIZONTAL_LEFT_TO_RIGHT || fragment.getWritingMode() == PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
                    //space = (coords[2] - coords[0]) / text_length;
                    pt = fragment.getX1();
                    last_pt = fragment.getX1();
                    min = minX;
                    max = maxX;
                } else { //vertical text
                    //space = (coords[1] - coords[3]) / text_length;
                    pt = fragment.getY2();
                    last_pt = fragment.getY2();
                    min = minY;
                    max = maxY;
                }

                linePos = -1;

                /**
                 * work through text, using embedded markers to work out whether
                 * each letter is IN or OUT
                 */
                final char[] line = fragment.getRawData().toCharArray();

                final int end = line.length;
                int pointer = 0;

                String value, textValue = "", pt_reached;

                //allow for no tokens and return all text fragment
                if (!fragment.getRawData().contains(MARKER)) {
                    text = new StringBuilder(fragment.getRawData());
                }

                boolean isFirstValue = true, breakPointset = false;

                /**
                 * work through text, using embedded markers to work out whether
                 * each letter is IN or OUT
                 */
                while (pointer < end) {

                    //only data between min and y locations
                    while (true) {

                        /**
                         * read value
                         */
                        if (line[pointer] != MARKER2) {
                            //find second marker and get width
                            final int startPointer = pointer;
                            while ((pointer < end) && (line[pointer] != MARKER2)) {
                                pointer++;
                            }
                            value = fragment.getRawData().substring(startPointer, pointer);

                        } else {//if (value.equals(MARKER)) { // read the next token and its location and width

                            //find first marker
                            while ((pointer < end) && (line[pointer] != MARKER2)) {
                                pointer++;
                            }

                            pointer++;

                            //find second marker and get width
                            int startPointer = pointer;
                            while ((pointer < end) && (line[pointer] != MARKER2)) {
                                pointer++;
                            }
                            pt_reached = fragment.getRawData().substring(startPointer, pointer);
                            pointer++;

                            //find third marker
                            startPointer = pointer;
                            while ((pointer < end) && (line[pointer] != MARKER2)) {
                                pointer++;
                            }

                            char_width = fragment.getRawData().substring(startPointer, pointer);
                            pointer++;

                            //find next marker
                            startPointer = pointer;
                            while ((pointer < end) && (line[pointer] != MARKER2)) {
                                pointer++;
                            }

                            value = fragment.getRawData().substring(startPointer, pointer);

                            textValue = value; //keep value with no spaces

                            if (!pt_reached.isEmpty()) { //set point character starts
                                last_pt = pt;
                                pt = Float.parseFloat(pt_reached);

                                if (breakPointset) {
                                    alterCoordsBasedOnWritingMode(fragment, pt);
                                    breakPointset = false;
                                }
                            }

                            //add font start if needed
                            if (isXMLExtraction && last_pt < min && pt > min && !value.startsWith(Fonts.fb)) {
                                value = Fonts.getActiveFontTag(fragment.getRawData(), "") + value;
                            }

                        }
                        if (!char_width.isEmpty()) {
                            final float midPoint = pt + (Float.parseFloat(char_width) * 0.3f);
                            if ((midPoint > min) & (midPoint < max)) {
                                setFragmentCoord(fragment, min, max, pt);

                                break;
                            }
                        }

                        value = "";
                        textValue = "";

                        if (pointer >= end) {
                            break;
                        }
                    }

                    /**
                     * make sure font not sliced off on first value
                     */
                    if (isFirstValue) {

                        isFirstValue = false;
                        if ((isXMLExtraction) && (keepFont) && (!value.startsWith(Fonts.fb)) && (!value.startsWith(GenericColorSpace.cb))) {//&&(!text.toString().startsWith(Fonts.fb))))
                            text.append(Fonts.getActiveFontTag(text.toString(), fragment.getRawData()));
                        }
                    }

                    /**
                     * we now have a valid value inside the selected area so perform tests
                     */
                    //see if a break occurs
                    boolean is_broken = false;
                    if (findLines && fragment.getCharacterSpacing() > 0 && text.toString().endsWith(" ")) {
                        final int counts = lineBreaks.size();
                        for (int jj = 0; jj < counts; jj++) {
                            final int test_x = lineBreaks.elementAt(jj);
                            if ((last_pt < test_x) & (pt > test_x)) {
                                jj = counts;
                                is_broken = true;
                            }
                        }
                    }

                    final boolean endsWithPunctuation = checkForPunctuation(textValue, punctuation);

                    if (is_broken) { //break on double-spaces or larger

                        text = writeOutFragment(keepFont, isWordlist, debugSplit, last_pt, pt, char_width, text, fragment, i, end, value);

                    } else if (endsWithPunctuation
                            || (breakOnSpace && (textValue.indexOf(' ') != -1 || value.endsWith(" "))) || textValue.contains("   ")) {//break on double-spaces or larger
                        if (debugSplit) {
                            System.out.println("Break 2 endsWithPunctuation=" + endsWithPunctuation + " textValue=" + textValue + '<' + " value=" + value + '<' + " text=" + text + '<');
                        }

                        pt = writeOut(keepFont, isWordlist, debugSplit, pt, char_width, text, fragment, i, value, textValue, endsWithPunctuation);

                        if (!char_width.isEmpty()) { //add in space values to start of next shape
                            //count the spaces
                            int ptr = 0;

                            if (textValue.indexOf(' ') != -1) {
                                ptr = textValue.indexOf(' ');
                            }

                            if (isWordlist) {
                                final int len = textValue.length();
                                while (ptr < len && textValue.charAt(ptr) == ' ') {
                                    ptr++;
                                }
                            }

                            if (ptr > 0) {
                                pt += ptr * Float.parseFloat(char_width);
                            } else {
                                pt += Float.parseFloat(char_width);
                            }

                            breakPointset = ptr > 0;

                        }

                        //store fact it had a space in case we generate wordlist
                        if ((breakOnSpace) & (nextSlot > 0)) {
                            hadSpace[nextSlot - 1] = true;
                        }

                        text = new StringBuilder(Fonts.getActiveFontTag(text.toString(), fragment.getRawData()));
                        alterCoordsBasedOnWritingMode(fragment, pt);

                    } else if ((linePos != -1) & (pt > linePos)) {//break on a vertical line

                        text = writeOnVerticalLineBreak(keepFont, isWordlist, linePos, text, fragment, i, value);

                        linePos = -1;

                    } else { //allow for space used as tab
                        if ((isXMLExtraction) && (value.endsWith(' ' + Fonts.fe))) {
                            value = Fonts.fe;
                            textValue = "";

                            alterCoordsBasedOnWritingMode(fragment, last_pt);
                        }
                        text.append(value);
                    }

                }

                //trap scenario we found if all goes through with no break at end
                if (keepFont && isXMLExtraction && !text.toString().endsWith(Fonts.fe)
                        && !text.toString().endsWith(GenericColorSpace.ce)) {
                    text.append(Fonts.fe);
                }

                //create new line with what is left and output
                completeLine(keepFont, isWordlist, text, fragment, i);

                text = new StringBuilder();

            }
        }

        //local lists for faster access
        isUsed = new boolean[nextSlot];

    }

    private void completeLine(boolean keepFont, boolean isWordlist, StringBuilder text, Fragment fragment, int i) {
        if (fragment.getWritingMode() == PdfData.HORIZONTAL_LEFT_TO_RIGHT || fragment.getWritingMode() == PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
            if (fragment.getX1() < fragment.getX2()) {
                addFragment(i, text, fragment.getX1(), fragment.getX2(), fragment.getY1(), fragment.getY2(),  keepFont, fragment, isWordlist);
            } else {
                addFragment(i, text, fragment.getX2(), fragment.getX1(), fragment.getY1(), fragment.getY2(),  keepFont, fragment, isWordlist);
            }
        } else if ((fragment.getWritingMode() == PdfData.VERTICAL_BOTTOM_TO_TOP || fragment.getWritingMode() == PdfData.VERTICAL_TOP_TO_BOTTOM) && (fragment.getY1() > fragment.getY2())) {
                addFragment(i, text, fragment.getX1(), fragment.getX2(), fragment.getY1(), fragment.getY2(), keepFont, fragment, isWordlist);
            }
        }

    private StringBuilder writeOnVerticalLineBreak(boolean keepFont, boolean isWordlist, float linePos, StringBuilder text, Fragment fragment, int i, String value) {
        if (fragment.getWritingMode() == PdfData.HORIZONTAL_LEFT_TO_RIGHT) {
            addFragment(i, text, fragment.getX1(), linePos, fragment.getY1(), fragment.getY2(),  keepFont, fragment, isWordlist);
        } else if (fragment.getWritingMode() == PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
            addFragment(i, text, linePos, fragment.getX2(), fragment.getY1(), fragment.getY2(),  keepFont, fragment, isWordlist);
        } else if (fragment.getWritingMode() == PdfData.VERTICAL_BOTTOM_TO_TOP) {
            addFragment(i, text, fragment.getX1(), fragment.getX2(), linePos, fragment.getY2(), keepFont, fragment, isWordlist);
        } else if (fragment.getWritingMode() == PdfData.VERTICAL_TOP_TO_BOTTOM) {
            addFragment(i, text, fragment.getX1(), fragment.getX2(), fragment.getY1(), linePos, keepFont, fragment, isWordlist);
        }

        text = new StringBuilder(Fonts.getActiveFontTag(text.toString(), fragment.getRawData()));
        text.append(value);

        alterCoordsBasedOnWritingMode(fragment, linePos);
        return text;
    }

    private float writeOut(boolean keepFont, boolean isWordlist, boolean debugSplit, float pt, String char_width, StringBuilder text, Fragment fragment, int i, String value, String textValue, boolean endsWithPunctuation) {
        //Remove final bit of the below if to fix issue in case 11542
        if (textValue.length() > 1 && textValue.indexOf(' ') != -1) {// && fragment.getX1()==pt){ //add in space values to start of next shape
            //count the spaces
            final int ptr = textValue.indexOf(' ');

            if (ptr > 0) {
                pt += ptr * (Float.parseFloat(char_width) / textValue.length());
            }
        }

        if (!endsWithPunctuation) {
            text.append(value.trim());
        }

        if (fragment.getWritingMode() == PdfData.HORIZONTAL_LEFT_TO_RIGHT) {

            if (debugSplit) {
                System.out.println("Add " + fragment.getX1() + ' ' + pt + " text=" + text + " i=" + i);
            }
            addFragment(i, text, fragment.getX1(), pt, fragment.getY1(), fragment.getY2(),  keepFont, fragment, isWordlist);
        } else if (fragment.getWritingMode() == PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
            if (debugSplit) {
                System.out.println("b");
            }
            addFragment(i, text, pt, fragment.getX2(), fragment.getY1(), fragment.getY2(), keepFont, fragment, isWordlist);
        } else if (fragment.getWritingMode() == PdfData.VERTICAL_BOTTOM_TO_TOP) {
            if (debugSplit) {
                System.out.println("c");
            }
            addFragment(i, text, fragment.getX1(), fragment.getX2(), pt, fragment.getY2(), keepFont, fragment, isWordlist);
        } else if (fragment.getWritingMode() == PdfData.VERTICAL_TOP_TO_BOTTOM) {
            if (debugSplit) {
                System.out.println("d");
            }
            addFragment(i, text, fragment.getX1(), fragment.getX2(), fragment.getY1(), pt, keepFont, fragment, isWordlist);
        }
        return pt;
    }

    private StringBuilder writeOutFragment(boolean keepFont, boolean isWordlist, boolean debugSplit, float last_pt, float pt, String char_width, StringBuilder text, Fragment fragment, int i, int end, String value) {
        if (debugSplit) {
            System.out.println("Break 1 is_broken");
        }

        Fragment temp = new Fragment(pdf_data, end);
        temp.setX1(fragment.getX1());
        temp.setY1(fragment.getY1());
        temp.setX2(fragment.getX2());
        temp.setY2(fragment.getY2());
        alterCoordsBasedOnWritingMode(temp, last_pt + Float.parseFloat(char_width));

        addFragment(i, text, temp.getX1(), temp.getX2(), temp.getY1(), temp.getY2(), keepFont, fragment, isWordlist);
        text = new StringBuilder(Fonts.getActiveFontTag(text.toString(), fragment.getRawData()));
        text.append(value);

        alterCoordsBasedOnWritingMode(fragment, pt);
        return text;
    }

    private void setFragmentCoord(Fragment fragment, float min, float max, float pt) {
        if (fragment.getWritingMode() == PdfData.HORIZONTAL_LEFT_TO_RIGHT) {
            if ((fragment.getX1() < min || fragment.getX1() > max) && pt >= min) {
                fragment.setX1(pt);
            } else if (fragment.getWritingMode() == PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
                if ((fragment.getX2() > max || fragment.getX2() < min) && pt <= max) {
                    fragment.setX2(pt);
                } else if (fragment.getWritingMode() == PdfData.VERTICAL_BOTTOM_TO_TOP) {
                    if ((fragment.getY2() < min || fragment.getY2() > max) && pt >= min) {
                        fragment.setY2(pt);
                    } else if ((fragment.getWritingMode() == PdfData.VERTICAL_TOP_TO_BOTTOM)
                            && ((fragment.getY1() < min || fragment.getY1() > max) && pt <= min)) {
                        fragment.setY1(pt);
                    }
                }
            }
        }
    }
    
    static void alterCoordsBasedOnWritingMode(Fragment fragment, float value){

        if (fragment.getWritingMode() == PdfData.HORIZONTAL_LEFT_TO_RIGHT) {
            fragment.setX1(value);
        } else if (fragment.getWritingMode() == PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
            fragment.setX2(value);
        } else if (fragment.getWritingMode() == PdfData.VERTICAL_BOTTOM_TO_TOP) {
            fragment.setY2(value);
        } else if (fragment.getWritingMode() == PdfData.VERTICAL_TOP_TO_BOTTOM) {
            fragment.setY1(value);
        }
    }
	private void initArrays(int count) {
		f_x1 = new float[count];
		f_colorTag=new String[count];
		hadSpace=new boolean[count];
		f_x2 = new float[count];
		f_y1 = new float[count];
		f_y2 = new float[count];

		spaceWidth = new float[count];
		content = new StringBuilder[count];
		fontSize = new int[count];
		textLength = new int[count];
		writingMode=new int[count];
		isUsed=new boolean[count];
	}

	/**
	 * @param textValue
	 * @return
	 */
	private static boolean checkForPunctuation(final String textValue, final String punctuation) {
		
		if(punctuation==null || (punctuation!=null && punctuation.isEmpty())) {
            return false;
        }
		
		/** see if ends with punctuation */
		boolean endsWithPunctuation = false;
		final int textLength = textValue.length();
		int ii = textLength - 1;
		if (textLength > 0) { //strip any spaces and tags in test
			char testChar = textValue.charAt(ii);
			boolean inTag = (testChar == '>');
			while (((inTag) | (testChar == ' ')) & (ii > 0)) {
				
				if (testChar == '<') {
                    inTag = false;
                }
				
				ii--;
				testChar = textValue.charAt(ii);
				
				if (testChar == '>') {
                    inTag = true;
                }
			}
			
			//stop  matches on &;
			if((testChar==';')){
				//ignore if looks like &xxx;
				endsWithPunctuation = true;
				ii--;
				while(ii>-1){
					
					testChar=textValue.charAt(ii);
					if(testChar=='&' || testChar=='#'){
						endsWithPunctuation = false;
						ii=0;
					}
					
					if(ii==0 || testChar==' ' || !Character.isLetterOrDigit(testChar)) {
                        break;
                    }
					
					ii--;
				}
			}else if (punctuation.indexOf(testChar) != -1) {
                endsWithPunctuation = true;
            }
			
		}
		return endsWithPunctuation;
	}

	/**
	 * add an object to our new XML list
	 */
	private void addFragment(
			final int index,
            final StringBuilder contentss,
			final float x1,
			final float x2,
			final float y1,
			final float y2,
			final boolean keepFontTokens, final Fragment fragment, final boolean isWordlist) {

        StringBuilder current_text = contentss;
		final String str=current_text.toString();

        final int text_len=fragment.getTextLength();
        final String currentColorTag=fragment.getColorTag();

		//strip <> or ascii equivalents
		if(isWordlist){
			if(str.contains("&#")) {
                current_text=Strip.stripAmpHash(current_text);
            }
			
			if((isXMLExtraction)&&((str.contains("&lt;"))||(str.contains("&gt;")))) {
                current_text=Strip.stripXMLArrows(current_text,true);
            } else if((!isXMLExtraction)&&((str.indexOf('<')!=-1)||(str.indexOf('>')!=-1))) {
                current_text=Strip.stripArrows(current_text);
            }
		}

		//ignore blank space objects
		if(getFirstChar(current_text)!=-1){

			//strip tags or pick up missed </font> if ends with space
			if (!keepFontTokens) {

				//strip fonts if required
				current_text = Strip.stripXML(current_text,isXMLExtraction);

			} else if (isXMLExtraction){
				
				//no color tag
			    if(pdf_data.isColorExtracted()&&(!current_text.toString().endsWith(GenericColorSpace.ce))){
			    	
			    	//se
			    	//if ends </font> add </color>
			    	//otherwise add </font></color>
			    	if(!current_text.toString().endsWith(Fonts.fe)) {
                        current_text = current_text.append(Fonts.fe);
                    }
			    	current_text = current_text.append(GenericColorSpace.ce);
			    	
			    }else if((!pdf_data.isColorExtracted())&&(!current_text.toString().endsWith(Fonts.fe))) {
                    current_text = current_text.append(Fonts.fe);
                }        		    
			}
			
			/***/
			//add to vacant slot or create new slot
			int count = f_x1.length;
			
			if (nextSlot < count) {

				f_x1[nextSlot] = x1;
				f_colorTag[nextSlot]=currentColorTag;
				f_x2[nextSlot] = x2;
				f_y1[nextSlot] = y1;
				f_y2[nextSlot] = y2;
				
				fontSize[nextSlot] = pdf_data.f_end_font_size[index];
				writingMode[nextSlot]=pdf_data.f_writingMode[index];
				textLength[nextSlot] = text_len;

				spaceWidth[nextSlot] = pdf_data.space_width[index];
				content[nextSlot] = current_text;

				nextSlot++;
			} else {
				count += increment;
				final float[] t_x1 = new float[count];
				final String[] t_colorTag=new String[count];
				final float[] t_x2 = new float[count];
				final float[] t_y1 = new float[count];
				final float[] t_y2 = new float[count];
				final float[] t_spaceWidth = new float[count];

                final StringBuilder[] t_content = new StringBuilder[count];

				final int[] t_font_size = new int[count];
				final int[] t_text_len = new int[count];
				final int[] t_writingMode=new int[count];

				final boolean[] t_isUsed = new boolean[count];
				
				final boolean[]t_hadSpace=new boolean[count];
				
				//copy in existing
				for (int i = 0; i < count - increment; i++) {
					t_x1[i] = f_x1[i];
					t_colorTag[i]=f_colorTag[i];
					t_x2[i] = f_x2[i];
					t_y1[i] = f_y1[i];
					t_y2[i] = f_y2[i];
					t_hadSpace[i]=hadSpace[i];
					t_spaceWidth[i] = spaceWidth[i];
					t_content[i] = content[i];
					t_font_size[i] = fontSize[i];
					t_writingMode[i]=writingMode[i];
					t_text_len[i] = textLength[i];
					t_isUsed[i] = isUsed[i];
				}

				f_x1 = t_x1;
				f_colorTag=t_colorTag;
				hadSpace=t_hadSpace;
				f_x2 = t_x2;
				f_y1 = t_y1;
				f_y2 = t_y2;
				isUsed=t_isUsed;
				
				fontSize = t_font_size;
				writingMode=t_writingMode;
				textLength = t_text_len;

				spaceWidth = t_spaceWidth;

				content = t_content;
				
				f_x1[nextSlot] = x1;
				f_colorTag[nextSlot]=currentColorTag;
				f_x2[nextSlot] = x2;
				f_y1[nextSlot] = y1;
				f_y2[nextSlot] = y2;

				fontSize[nextSlot] = pdf_data.f_end_font_size[index];
				writingMode[nextSlot]=pdf_data.f_writingMode[index];
				t_text_len[nextSlot] = text_len;
				content[nextSlot] = current_text;

				spaceWidth[nextSlot] = pdf_data.space_width[index];
				
				nextSlot++;

			} /***/

		}
	}

	//////////////////////////////////////////////////////////////////////
	/**
	 * put rows together into one object with start and end
	 */
	private void mergeTableRows(final int border_width) {

		//merge row contents
		String separator ="</tr>\n<tr>";
		
		if (!isXHTML) {
            separator = "\n";
        }

		master = ((Vector_Int) lines.elementAt(line_order[0])).elementAt(0);

		int item;
		for (int rr = 1; rr < max_rows; rr++) {

			item =((Vector_Int) lines.elementAt(line_order[rr])).elementAt(0);
			if(content[master]==null) {
                master=item;
            } else if(content[item]!=null) {
                merge(master,item,separator,false);
            }
		}

		//add start/end marker
		if (isXHTML) {
			if (border_width == 0){
				content[master].insert(0,"<TABLE>\n<tr>");
				content[master].append("</tr>\n</TABLE>\n");
			}else{
                final StringBuilder startTag=new StringBuilder("<TABLE border='");
				startTag.append(border_width);
				startTag.append( "'>\n<tr>");
				startTag.append(content[master]);
				content[master]=startTag;
				content[master].append("</tr>\n</TABLE>\n");
			}
		}

	}

	//////////////////////////////////////////////////
	/**
	 * get list of unused fragments and put in list and sort in sorted_items
	 */
    private int[] getsortedUnusedFragments(
		final boolean sortOnX,
		final boolean use_y1) {
		final int total_fragments = isUsed.length;

		//get unused item pointers
		int ii = 0;
		final int[] sorted_temp_index = new int[total_fragments];
		for (int i = 0; i < total_fragments; i++) {
			if (!isUsed[i]) {
				sorted_temp_index[ii] = i;
				ii++;
			}
		}
		
		final int[] unsorted_items = new int[ii];
		final int[] sorted_items;
		final int[] sorted_temp_x1 = new int[ii];
		final int[] sorted_temp_y1 = new int[ii];
		final int[] sorted_temp_y2 = new int[ii];

		//put values in array and get x/y for sort
		for (int pointer = 0; pointer < ii; pointer++) {
			final int i = sorted_temp_index[pointer];
			unsorted_items[pointer] = i;
			
			sorted_temp_x1[pointer] = (int) f_x1[i];

			//negative values to get sort in 'wrong' order from top of page
			sorted_temp_y1[pointer] = (int) f_y1[i];
			sorted_temp_y2[pointer] = (int) f_y2[i];

		}

		//sort
		if (!sortOnX) {
			if (use_y1) {
                sorted_items =
                        Sorts.quicksort(
                                sorted_temp_y1,
                                sorted_temp_x1,
                                unsorted_items);
            } else {
                sorted_items =
                        Sorts.quicksort(
                                sorted_temp_y2,
                                sorted_temp_x1,
                                unsorted_items);
            }
		} else {
            sorted_items =
                    Sorts.quicksort(sorted_temp_x1, sorted_temp_y1, unsorted_items);
        }
		
		return sorted_items;
	}

	//////////////////////////////////////////////////////////////////////
	/**
	 * create rows of data from preassembled indices, adding separators. Each
	 * row is built to a temp array and then row created - we don't know how
	 * many columns until the table is built
	 * @throws PdfException
	 */
	private void createTableRows(
		final boolean keep_alignment_information,
		final boolean keep_width_information, final int currentWritingMode) throws PdfException {

		/**
		 * create local copies of arrays 
		 */
		final float[] f_x1;
        final float[] f_x2;

        /**
		 * set pointers so left to right text
		 */
		if(currentWritingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT){
			f_x1=this.f_x1;
			f_x2=this.f_x2;
			//f_y1=this.f_y1;
			//f_y2=this.f_y2;
		}else if(currentWritingMode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
			f_x2=this.f_x1;
			f_x1=this.f_x2;
			//f_y1=this.f_y1;
			//f_y2=this.f_y2;
		}else if(currentWritingMode==PdfData.VERTICAL_BOTTOM_TO_TOP){
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			//f_y1=this.f_x2;
			//f_y2=this.f_x1;
		}else if(currentWritingMode==PdfData.VERTICAL_TOP_TO_BOTTOM){
			f_x1=this.f_y1;
			f_x2=this.f_y2;
			//f_y2=this.f_x1;
			//f_y1=this.f_x2;
			
			/**
			 * fiddle x,y co-ords so it works
			 */
			
			//get max size
			int maxX=0;
            for (final float aF_x1 : f_x1) {
                if (maxX < aF_x1) {
                    maxX = (int) aF_x1;
                }
            }
			
			maxX++; //allow for fp error
			//turn around
			for(int ii=0;ii<f_x2.length;ii++){
				f_x1[ii]=maxX-f_x1[ii];
				f_x2[ii]=maxX-f_x2[ii];
			}
			
		}else{
			throw new PdfException("Illegal value "+currentWritingMode+"for currentWritingMode");
		}

		int item, i;//, current_col = -1;

		int itemsInTable = 0, items_added = 0;
		//pointer to current element on each row
		final int[] currentItem = new int[max_rows];

		final Vector_Int[] rowContents = new Vector_Int[max_rows];
		final Vector_String alignments = new Vector_String(); //text alignment
		final Vector_Float widths = new Vector_Float(); //cell widths
		final Vector_Float cell_x1 = new Vector_Float(); //cell widths
		String separator = "", empty_cell = "&nbsp;";

		if (!isXHTML) {
			separator = "\",\"";
			empty_cell = "";
		}

		/**
		 * set number of items on each line, column count and populate empty rows
		 */
		final int[] itemCount = new int[max_rows];
		for (i = 0; i < max_rows; i++) {
			itemCount[i] = ((Vector_Int) lines.elementAt(i)).size() - 1;

			//total number of items
			itemsInTable += itemCount[i];

			//reset other values
			currentItem[i] = 0;
			rowContents[i] = new Vector_Int(20);
		}

		//now work through and split any overlapping items until all done
		while (true) {

			//size of column and pointers
			float x1 = 9999,min_x2 = 9999,x2,current_x1,current_x2,c_x1,next_x1 = 9999,c_x2,items_in_column = 0;
			
			boolean all_done = true; //flag to exit at end
			float total_x1 = 0;
            float total_x2 = 0;
            float left_gap = 0;
            final float right_gap;

            String alignment = "center";

			if (items_added < itemsInTable) {

				/** 
				 * work out cell x boundaries on basis of objects 
				 */
				for (i = 0; i < max_rows; i++) { //get width for column
					if (itemCount[i] > currentItem[i]) { //item  id
						
						item = ((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i]);
						current_x1 = f_x1[item];
						current_x2 = f_x2[item];
						
						if (current_x1 < x1) { //left margin
                            x1 = current_x1;
                        }
						if (current_x2 < min_x2) { //right margin if appropriate
                            min_x2 = current_x2;
                        }
						
					}
				}
				
				cell_x1.addElement(x1); //save left margin
				x2 = min_x2; //set default right margin

				/**
				 * workout end and next column start by scanning all items
				 */
				for (i = 0;i < max_rows;i++) { //slot the next item on each row together work out item
					item = ((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i]);
					c_x1 = f_x1[item];
					c_x2 = f_x2[item];

					//max item width of this column
					if ((c_x1 >= x1) & (c_x1 < min_x2) & (c_x2 > x2)) {
                        x2 = c_x2;
                    }

					if (currentItem[i] < itemCount[i]) { //next left margin

						item =((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i] + 1);
						current_x1 = f_x1[item];
						if ((current_x1 > min_x2) & (current_x1 < next_x1)) {
                            next_x1 = current_x1;
                        }
					}
				}

                //stop infinite loop case
                if(x1==x2) {
                    break;
                }

				//allow for last column
				if (next_x1 == 9999) {
                    next_x1 = x2;
                }
			
				/**
				 * count items in table and workout raw totals for alignment.
				 * Also work out widest x2 in column
				 */
				for (i = 0;i < max_rows;i++) { //slot the next item on each row together

					//work out item
					item =((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i]);
					c_x1 = f_x1[item];
					c_x2 = f_x2[item];

					//use items in first column of single colspan
					if ((c_x1 >= x1) & (c_x1 < min_x2) & (c_x2 <= next_x1)) {

						//running totals to calculate alignment
						total_x1 += c_x1;
						total_x2 += c_x2;
						items_in_column++;

					}
				}
				
				/**
				 * work out gap and include empty space between cols and save
				 */
				if (i == 0) {
                    left_gap = x1;
                }
				if (next_x1 == -1) {
                    right_gap = 0;
                } else {
                    right_gap = (int) ((next_x1 - x2) / 2);
                }

				final int width = (int) (x2 - x1 + right_gap + left_gap);
                
				widths.addElement(width);

				/** workout the alignment */
				final float x1_diff = (total_x1 / items_in_column) - x1;
				final float x2_diff = x2 - (total_x2 / items_in_column);
				if (x1_diff < 1) {
                    alignment = "left";
                } else if (x2_diff < 1) {
                    alignment = "right";
                }
				alignments.addElement(alignment);

				for (i = 0;i < max_rows;i++) { //slot the next item on each row together
					master = ((Vector_Int) lines.elementAt(i)).elementAt(0);
					//get next item on line or -1 for no more
					if (itemCount[i] > currentItem[i]) {
						//work out item
						item =((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i]);
						c_x1 = f_x1[item];
						//c_x2 = f_x2[item];
						all_done = false;

					} else {
						item = -1;
						c_x1 = -1;
						//c_x2 = -1;
					}

					if ((item == -1) & (items_added <= itemsInTable)) {
						//all items in table so just filling in gaps
						rowContents[i].addElement(-1);
						
					} else if ((c_x1 >= x1) & (c_x1 < x2)) {
						//fits into cell so add in and roll on marker

						rowContents[i].addElement(item);
						currentItem[i]++;
						
						items_added++;
					} else if (c_x1 > x2) { //empty cell
						rowContents[i].addElement(-1);
					}
				}
			}
			if (all_done) {
                break;
            }
		}

		//===================================================================
		/**
		 * now assemble rows
		 */
		for (int row = 0; row < max_rows; row++) {
            final StringBuilder line_content = new StringBuilder(100);
			
			int count = rowContents[row].size() - 1;
			master = ((Vector_Int) lines.elementAt(row)).elementAt(0);

			for (i = 0; i < count; i++) {
				item = rowContents[row].elementAt(i);

				if (isXHTML) {

					//get width
					float current_width = widths.elementAt(i);
					final String current_alignment = alignments.elementAt(i);
					int test, colspan = 1, pointer = i + 1;

					if (item != -1) {

						//look for colspan
						while (true) {
							test = rowContents[row].elementAt(i + 1);
							if ((test != -1) | (count == i + 1)) {
                                break;
                            }

							//break if over another col - roll up single value on line
							if (itemCount[row] > 1 && (cell_x1.elementAt(i + 1) > f_x2[item])) {
                                break;
                            }

							count--;
							rowContents[row].removeElementAt(i + 1);
							colspan++;

							//update width
							current_width += widths.elementAt(pointer);
							pointer++;
						}
					}
					line_content.append("<td");

					if (keep_alignment_information) {
						line_content.append(" align='");
						line_content.append(current_alignment);
						line_content.append('\'');
						if (colspan > 1) {
                            line_content.append(" colspan='").append(colspan).append('\'');
                        }
					}

					if (keep_width_information) {
                        line_content.append(" width='").append((int) current_width).append('\'');
                    }

					line_content.append(" nowrap>");
					if (item == -1) {
                        line_content.append(empty_cell);
                    } else {
                        line_content.append(content[item]);
                    }
					line_content.append("</td>");

				} else { //csv
					if (item == -1) { //empty col
                        line_content.append("\"\",");
                    } else{ //value
						line_content.append('\"');
						line_content.append(content[item]);
						line_content.append("\",");
					}
				}

				//merge to update other values
				if ((item != -1) && (master != item)) { //merge tracks the shape
                    merge(master,item,separator,false);
                }

			}
			//substitute our 'hand coded' value
			content[master] = line_content;

		}
	}

	/**
	 * work through data and create a set of rows and return an object with
	 * refs for each line
	 * @throws PdfException
	 */
	private void createLinesInTable(final int itemCount, int[] items, final boolean addSpaceXMLTag, final int mode) throws PdfException {

        /**
		 * reverse order if text right to left
		 */
		if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
            items=reverse(items);
        }
		
		/**
		 * create and populate local copies of arrays
		 */
		final float[] f_x1;
        final float[] f_x2;
        final float[] f_y1;
        final float[] f_y2;

        // set pointers so always left to right text
        switch(mode){
            case PdfData.HORIZONTAL_LEFT_TO_RIGHT:
			f_x1=this.f_x1;
			f_x2=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
                break;

            case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
			f_x2=this.f_x1;
			f_x1=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
                break;

            case PdfData.VERTICAL_BOTTOM_TO_TOP:
			f_x1=this.f_y1;
			f_x2=this.f_y2;
			f_y1=this.f_x2;
			f_y2=this.f_x1;
                break;

            case PdfData.VERTICAL_TOP_TO_BOTTOM:
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			f_y2=this.f_x1;
			f_y1=this.f_x2;
			items = this.getsortedUnusedFragments(false, true);
			items=reverse(items);
                break;

            default:
			throw new PdfException("Illegal value "+mode+"for currentWritingMode");
		}
		
		//holds line we're working on
		Vector_Int current_line;
		
        for (int j = 0; j < itemCount; j++) { //for all items

            final int c=items[j];
            int id = -1;
            int i;
            int last = c;
            float smallest_gap = -1, gap, yMidPt;

				if(!isUsed[c] && this.writingMode[c]==mode) {

					//reset pointer and add this element
					current_line = new Vector_Int(20);
					current_line.addElement(c);
					lineY2.addElement((int) f_y2[c]);

                //look for items along same line (already sorted into order left to right)
                while (true) {   //look for a match
                    for (int ii = 0; ii < itemCount; ii++) {

							i = items[ii];

                        if (!isUsed[i] && i!=c && writingMode[c]==mode && ((f_x1[i] > f_x1[c] && mode!=PdfData.VERTICAL_TOP_TO_BOTTOM)||(f_x1[i] < f_x1[c] && mode==PdfData.VERTICAL_TOP_TO_BOTTOM))) { //see if on right

                            gap = (f_x1[i] - f_x2[c]);

								if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM) {
                                    gap=-gap;
                            }

                            //allow for fp error
                            if (gap < 0 && gap > -2) {
                                gap = 0;
                            }

								//make sure on right
								yMidPt = (f_y1[i] + f_y2[i]) / 2;

								//see if line & if only or better fit
                            if (yMidPt < f_y1[c] && yMidPt > f_y2[c] && (smallest_gap < 0 || gap < smallest_gap)) {
									smallest_gap = gap;
									id = i;
								}
							}
						}

						if (id == -1) { //exit when no more matches
                            break;
                    }

                    //merge in best match if fit found with last or if overlaps by less than half a space,otherwise join
                    float t = f_x1[id] - f_x2[last],possSpace=f_x1[id]-f_x2[c];
                    float av_char1 =(float)1.5 *((f_x2[id] - f_x1[id])/ textLength[id]);
                    float av_char2 =(float)1.5 *((f_x2[last] - f_x1[last]) / textLength[last]);

                    if((mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM)){
                        possSpace=-possSpace;
                        t=-t;
                        av_char1=-av_char1;
                        av_char2=-av_char2;
                    }

                    if (t < av_char1 && t < av_char2) {
                        merge(last,id, isGapASpace(id, last, possSpace,addSpaceXMLTag,mode),true);
                    } else {
                        current_line.addElement(id);
                        last = id;
                    }

                    //flag used and reset variables used
                    isUsed[id] = true;
                    id = -1;
                    smallest_gap = 1000000;

                }

                //add line to list
                lines.addElement(current_line);
                max_rows++;
            }
        }
	}

	/**
	 * 
	 * calls various low level merging routines on merge - 
	 * 
	 * isCSV sets if output is XHTML or CSV format -
	 * 
	 * XHTML also has options to include font tags (keepFontInfo), 
	 * preserve widths (keepWidthInfo), try to preserve alignment 
	 * (keepAlignmentInfo), and set a table border width (borderWidth) 
	 *  - AddCustomTags should always be set to false
	 * 
	 * @param x1 is the x coord of the top left corner
	 * @param y1 is the y coord of the top left corner
	 * @param x2 is the x coord of the bottom right corner
	 * @param y2 is the y coord of the bottom right corner
	 * @param pageNumber is the page you wish to extract from
	 * @param isCSV is a boolean. If false the output is xhtml if true the text is out as CSV
	 * @param keepFontInfo if true and isCSV is false keeps font information in extrated text.
	 * @param keepWidthInfo if true and isCSV is false keeps width information in extrated text.
	 * @param keepAlignmentInfo if true and isCSV is false keeps alignment information in extrated text.
	 * @param borderWidth is the width of the border for xhtml
	 * @return Map containing text found in estimated table cells
	 * @throws PdfException If the co-ordinates are not valid
	 */
	@SuppressWarnings("UnusedParameters")
    public final Map extractTextAsTable(
		int x1,
		int y1,
		int x2,
		int y2,
		final int pageNumber,
		final boolean isCSV,
		final boolean keepFontInfo,
		final boolean keepWidthInfo,
		final boolean keepAlignmentInfo,
		final int borderWidth)
		throws PdfException {

		//check in correct order and throw exception if not
		final int[] v = validateCoordinates(x1, y1, x2, y2);
		x1 = v[0];
		y1 = v[1];
		x2 = v[2];
		y2 = v[3];
		
		/** return the content as an Element */
		final Map table_content = new HashMap();

		LogWriter.writeLog("extracting Text As Table");

		//flag type of table so we can add correct separators
        isXHTML = !isCSV;

		//init table variables
		lines = new Vector_Object(20);
		lineY2 = new Vector_Int(20);
		max_rows = 0;

		//init store for data
		copyToArrays(x1, y2, x2, y1, keepFontInfo, false,true,null,false);

		//initial grouping and delete any hidden text
		removeEncoding();

		//eliminate shadows and also merge overlapping text
		cleanupShadowsAndDrownedObjects(false);

		final int[] items = this.getsortedUnusedFragments(true, false);
		final int item_count = items.length; //number of items

		if(item_count==0) {
            return table_content;
        }
		
		/**
		 * check orientation and get preferred. Items not correct will
		 * be ignored
		 */
		final int writingMode=getWritingMode(items,item_count);

		final String message ="Table Merging algorithm being applied " + (item_count) + " items";
		LogWriter.writeLog(message);
		
		/**
		 * scan all items joining best fit to right of each fragment to build
		 * lines
		 */
		if (item_count > 1) {

			//workout the raw lines
			createLinesInTable(item_count, items,isXHTML,writingMode);

			/**
			 * generate lookup with lines in correct order (minus used to get
			 * correct order down the page)
			 */
			int dx=1;
			if(writingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT || writingMode==PdfData.VERTICAL_TOP_TO_BOTTOM) {
                dx=-1;
            }
			
			line_order = new int[max_rows];
			final int[] line_y=new int[max_rows];

			for (int i = 0; i < max_rows; i++) {
				line_y[i] = dx*lineY2.elementAt(i);
				line_order[i] = i;
			}

			line_order = Sorts.quicksort(line_y, line_order);

			//assemble the rows and columns
			createTableRows(keepAlignmentInfo, keepWidthInfo,writingMode);

			//assemble the rows and columns
			mergeTableRows(borderWidth);
			
		}

		content[master]=cleanup(content[master]);
		
		String processed_value = content[master].toString();

		if(processed_value!=null){
			
//			cleanup data if needed by removing duplicate font tokens
			if (!isCSV) {
                processed_value = Fonts.cleanupTokens(processed_value);
            }

			table_content.put("content", processed_value);
			table_content.put("x1", String.valueOf(x1));
			table_content.put("x2", String.valueOf(x2));
			table_content.put("y1", String.valueOf(y1));
			table_content.put("y2", String.valueOf(y2));
		}
		
		return table_content;
	}

	/** make sure co-ords valid and throw exception if not */
	private static int[] validateCoordinates(int x1, int y1, int x2, int y2) {
		if ((x1 > x2) | (y1 < y2)) {

//			String errorMessage = "Invalid parameters for text rectangle. ";
			if (x1 > x2){
//				errorMessage =
//					errorMessage
//						+ "x1 value ("
//						+ x1
//						+ ") must be LESS than x2 ("
//						+ x2
//						+ "). ";
				final int temp = x1;
				x1 = x2;
				x2 = temp;
				LogWriter.writeLog("x1 > x2, coordinates were swapped to validate");
			}
			
			if (y1 < y2){
//				errorMessage =
//					errorMessage
//						+ "y1 value ("
//						+ y1
//						+ ") must be MORE than y2 ("
//						+ y2
//						+ "). ";
				final int temp = y1;
				y1 = y2;
				y2 = temp;
				LogWriter.writeLog("y1 < y2, coordinates were swapped to validate");
			}
//			throw new PdfException(errorMessage);
		}
		return new int[]{x1,y1,x2,y2};
	}

	/**
	 * 
	 * algorithm to place data from within coordinates to a vector of word, word coords (x1,y1,x2,y2)
	 *
	 * @param x1 is the x coord of the top left corner
	 * @param y1 is the y coord of the top left corner
	 * @param x2 is the x coord of the bottom right corner
	 * @param y2 is the y coord of the bottom right corner
	 * @param page_number is the page you wish to extract from
	 * @param breakFragments will divide up text based on white space characters
	 * @param punctuation is a string containing all values that should be used to divide up words
	 * @return Vector containing words found and words coordinates (word, x1,y1,x2,y2...)
	 * @throws PdfException If the co-ordinates are not valid
	 */
    @SuppressWarnings("UnusedParameters")
    public final List extractTextAsWordlist(
		int x1,
		int y1,
		int x2,
		int y2,
		final int page_number,
		final boolean breakFragments,
		final String punctuation)
		throws PdfException {

		/** make sure co-ords valid and throw exception if not */
		final int[] v = validateCoordinates(x1, y1, x2, y2);
		x1 = v[0];
		y1 = v[1];
		x2 = v[2];
		y2 = v[3];

		/** extract the raw fragments (Note order or parameters passed) */
		if (breakFragments) {
            copyToArrays(x1, y2, x2, y1, true, true,false,punctuation,true);
        } else {
            copyToArrays();
        }

		
		
		/** delete any hidden text */
		removeEncoding();

		//eliminate shadows and also merge overlapping text
		cleanupShadowsAndDrownedObjects(true);

		final int[] items = getsortedUnusedFragments(true, false);
		final int count = items.length;

		/**if no values return null
		 */
		if(count==0){
			LogWriter.writeLog("Less than 1 text item on page");
			
			return null;
		}
		
		/**
		 * check orientation and get preferred. Items not correct will
		 * be ignored
		 */
		final int writingMode=getWritingMode(items,count);

			/**
			 * build set of lines from text
			 */
			createLines(count, items,writingMode,true,false,false, false);

			/**
			 * alter co-ords to rotated if requested
			 */
			float[] f_x1=null,f_x2=null,f_y1=null,f_y2=null;

			if(useUnrotatedCoords || writingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT){
				f_x1=this.f_x1;
				f_x2=this.f_x2;
				f_y1=this.f_y1;
				f_y2=this.f_y2;
			}else if(writingMode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
				f_x2=this.f_x1;
				f_x1=this.f_x2;
				f_y1=this.f_y1;
				f_y2=this.f_y2;
			}else if(writingMode==PdfData.VERTICAL_BOTTOM_TO_TOP){
				f_x1=this.f_y2;
				f_x2=this.f_y1;
				f_y1=this.f_x2;
				f_y2=this.f_x1;

			}else if(writingMode==PdfData.VERTICAL_TOP_TO_BOTTOM){
				f_x1=this.f_y1;
				f_x2=this.f_y2;
				f_y2=this.f_x1;
				f_y1=this.f_x2;
			}

		/** put into a Vector */
		final List values = new ArrayList();
			
			for (int i = 0; i < content.length; i++) {
				if (content[i] != null) {

//					System.out.println(">>>>>"+content[i]);

					if((colorExtracted)&&(isXMLExtraction)){
						if(!content[i].toString().toLowerCase().startsWith(GenericColorSpace.cb)){
							content[i].insert(0,f_colorTag[master]);
						}
						if(!content[i].toString().toLowerCase().endsWith(GenericColorSpace.ce)){
							content[i].append(GenericColorSpace.ce);
						}
					}

					if(isXMLExtraction) {
                        values.add((content[i]).toString());
                    } else {
                        values.add(Strip.convertToText((content[i]).toString(), isXMLExtraction));
                    }

					if((!useUnrotatedCoords)&&(writingMode==PdfData.VERTICAL_TOP_TO_BOTTOM)){
						values.add(String.valueOf(f_x1[i]));
						values.add(String.valueOf(f_y1[i]));
						values.add(String.valueOf(f_x2[i]));
						values.add(String.valueOf(f_y2[i]));
					}else if((!useUnrotatedCoords)&&(writingMode==PdfData.VERTICAL_BOTTOM_TO_TOP)){
						values.add(String.valueOf(f_x1[i]));
						values.add(String.valueOf(f_y2[i]));
						values.add(String.valueOf(f_x2[i]));
						values.add(String.valueOf(f_y1[i]));
					}else{	
						values.add(String.valueOf(f_x1[i]));
						values.add(String.valueOf(f_y1[i]));
						values.add(String.valueOf(f_x2[i]));
						values.add(String.valueOf(f_y2[i]));
					}
				}
			}

		LogWriter.writeLog("Text extraction as wordlist completed");
		
		return values;
		
	}

    /**
     * reset global values
     */
    private void reset(){

        isXHTML = true;
        nextSlot=0;

	    lineBreaks = new Vector_Int();

        max_rows = 0;
        master = 0;

        colorExtracted=false;

    }

    /**
	 * algorithm to place data from specified coordinates on a page into a String.
	 * 
	 * @param x1 is the x coord of the top left corner
	 * @param y1 is the y coord of the top left corner
	 * @param x2 is the x coord of the bottom right corner
	 * @param y2 is the y coord of the bottom right corner
	 * @param page_number is the page you wish to extract from
	 * @param estimateParagraphs will attempt to find paragraphs and add new lines in output if true
	 * @param breakFragments will divide up text based on white space characters if true
	 * @return Vector containing words found and words coordinates (word, x1,y1,x2,y2...)
	 * @throws PdfException If the co-ordinates are not valid
	 */
    @SuppressWarnings("UnusedParameters")
    public final String extractTextInRectangle(
		int x1,
		int y1,
		int x2,
		int y2,
		final int page_number,
		final boolean estimateParagraphs,
		final boolean breakFragments)
		throws PdfException {


        reset();

        if((breakFragments)&&(!pdf_data.IsEmbedded())) {
            throw new PdfException("[PDF] Request to breakfragments and width not added. Please add call to init(true) of PdfDecoder to your code.");
        }
	
		/** make sure co-ords valid and throw exception if not */
		final int[] v = validateCoordinates(x1, y1, x2, y2);
		x1 = v[0];
		y1 = v[1];
		x2 = v[2];
		y2 = v[3];
	
		final int master;
        final int count;

        /** extract the raw fragments (Note order or parameters passed) */
		if (breakFragments) {
            copyToArrays(x1, y2, x2, y1, (isXMLExtraction), false,false,null,false);
        } else {
            copyToArrays();
        }
		
		/** 
		 * delete any hidden text 
		 */
		removeEncoding();
		
		/**
		* eliminate shadows and also merge overlapping text
		*/
		cleanupShadowsAndDrownedObjects(false);
		
		/** get the fragments as an array */
		final int[] items = getsortedUnusedFragments(true, false);
		count = items.length;

		/**if no values return null
		 */
		if(count==0){
			LogWriter.writeLog("Less than 1 text item on page");
			
			return null;
		}
		
		/**
		 * check orientation and get preferred. Items not correct will
		 * be ignored
		 */
		final int writingMode=getWritingMode(items,count);
			
			/**
			 * build set of lines from text
			 */
			createLines(count, items,writingMode,false,isXMLExtraction,false, false);

        /**
               * roll lines together
               */
			
			master = mergeLinesTogether(writingMode,estimateParagraphs,x1,x2,y1,y2);

			/**
			 * add final deliminators 
			 */
			if(isXMLExtraction){
				content[master] =new StringBuilder(Fonts.cleanupTokens(content[master].toString()));
				content[master].insert(0,"<p>");
				content[master].append("</p>");
			}
			
		LogWriter.writeLog("Text extraction completed");

		return cleanup(content[master]).toString();

	}
	
	
	private StringBuilder cleanup(StringBuilder buffer) {
		
		if(buffer==null) {
            return buffer;
        }

         /**
        if(PdfDecoder.inDemo){
            int icount=buffer.length(),count=0;
            boolean inToken=false;
            for(int i=0;i<icount;i++){
                char c=buffer.charAt(i);
                if(c=='<')
                    inToken=true;
                else if(c=='>')
                    inToken=false;
                else if((c!=' ')&&(!inToken)){
                    count++;
                    if(count>4){
                        count=0;
                        buffer.setCharAt(i,'1');
                    }
                }
            }
		}
		/**/

        //sort out & to &amp;
        if(isXMLExtraction){
            String buf=buffer.toString();

            buf=buf.replaceAll("&#","XX#");
            buf=buf.replaceAll("&lt","XXlt");
            buf=buf.replaceAll("&gt","XXgt");

            buf=buf.replaceAll("&","&amp;");

            //put back others
            buf=buf.replaceAll("XX#", "&#");
            buf=buf.replaceAll("XXlt", "&lt");
            buf=buf.replaceAll("XXgt","&gt");

            final boolean removeInvalidXMLValues = true;
            if (removeInvalidXMLValues) {
            
	            /**
				 * Restricted Char ::=
				 *	[#x1-#x8] | [#xB-#xC] | [#xE-#x1F] | [#x7F-#x84] | [#x86-#x9F]
				 *  [#x1-#x8] | [#x11-#x12] | [#x14-#x31] | [#x127-#x132] | [#x134-#x159]
				 */
			
				/** set mappings */
				final Map asciiMappings = new HashMap();
				/** [#x1-#x8] */
				for (int i = 1; i <= 8; i++) {
                    asciiMappings.put("&#" + i + ';', "");
                }
				
				/** [#x11-#x12] */
				for (int i = 11; i <= 12; i++) {
                    asciiMappings.put("&#" + i + ';', "");
                }
				
				/** [#x14-#x31] */
				for (int i = 14; i <= 31; i++) {
                    asciiMappings.put("&#" + i + ';', "");
                }
				
				/** [#x127-#x132] */
				//for (int i = 127; i <= 132; i++)
					//asciiMappings.put("&#" + i + ";", "");
				
				/** [#x134-#x159] */
				//for (int i = 134; i <= 159; i++)
					//asciiMappings.put("&#" + i + ";", "");
				
				
				/** substitute illegal XML characters for mapped values */
                for (final Object o : asciiMappings.keySet()) {
                    final String character = (String) o;
                    final String mappedCharacter = (String) asciiMappings.get(character);

                    buf = buf.replace(character, mappedCharacter);
                }
			}
			buffer=new StringBuilder(buf);
        }
        
        return buffer;
	}

	/**
	 * scan fragments and detect orientation. If multiple,
	 * prefer horizontal
	 */
    private int getWritingMode(final int[] items, final int count) {

        int[] counts = new int[4];
        for (int j = 0; j < count; j++) {
            final int c=items[j];

            if ((!isUsed[c])){
                counts[writingMode[c]]++;
            }
        }
        
        int mode = 0;
        for(int i=1; i!=counts.length; i++){
            if(counts[i]>counts[mode]){
                mode = i;
            }
        }
        
        return mode;
        
//        /**
//         * get first value
//         */
//        int orientation=writingMode[items[0]];
//
//        //exit if first is horizontal
//        if(orientation==PdfData.HORIZONTAL_LEFT_TO_RIGHT || orientation==PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
//            return orientation;
//        }
//
//        /**
//         * scan items looking at orientation - exit if we find horizontal
//         */
//        for (int j = 1; j < count; j++) {
//
//            final int c=items[j];
//
//            if ((!isUsed[c]) && (writingMode[c]==PdfData.HORIZONTAL_LEFT_TO_RIGHT || writingMode[c]==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
//                    orientation=writingMode[c];
//                    j=count;
//                    LogWriter.writeLog("Text of multiple orientations found. Only horizontal text used.");
//                }
//            }
//      
//        return orientation;
    }

	/**
	 * @param estimateParagraphs
	 * @return
	 * @throws PdfException
	 */
	private int mergeLinesTogether(final int currentWritingMode, final boolean estimateParagraphs, final int x1, final int x2, final int y1, final int y2) throws PdfException {

        StringBuilder separator;
		
		int[] indices;
		
		//used for working out alignment
		final int middlePage;
		
		/**
		 * create local copies of 
		 */
		final float[] f_x1;
        final float[] f_x2;
        final float[] f_y1;
        final float[] f_y2;

        if(currentWritingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT){
			f_x1=this.f_x1;
			f_x2=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
			indices = getsortedUnusedFragments(false, true);
			middlePage = (x1 + x2) / 2;
		}else if(currentWritingMode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
			f_x2=this.f_x1;
			f_x1=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
			indices = getsortedUnusedFragments(false, true);
			middlePage = (x1 + x2) / 2;
		}else if(currentWritingMode==PdfData.VERTICAL_BOTTOM_TO_TOP){
			f_x1=this.f_y1;
			f_x2=this.f_y2;
			f_y1=this.f_x2;
			f_y2=this.f_x1;
			indices = getsortedUnusedFragments(true, true);

			indices=reverse(indices);
			middlePage = (y1 + y2) / 2;
			
		}else if(currentWritingMode==PdfData.VERTICAL_TOP_TO_BOTTOM){
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			f_y2=this.f_x2;
			f_y1=this.f_x1;
			indices = getsortedUnusedFragments(true, true);
			middlePage = (y1 + y2) / 2;
		}else{
			throw new PdfException("Illegal value "+currentWritingMode+"for currentWritingMode");
		}
		final int quarter = middlePage / 2;
		final int count = indices.length;
		final int master = indices[count - 1];
	
		/**
		 * now loop through all lines merging
		 */
        int ClastChar,MlastChar,CFirstChar;
		final boolean debug=false;

		for (int i = count - 2; i > -1; i--) {
			
			final int child = indices[i];
			separator = new StringBuilder();
			
				/** add formatting in to retain structure */
				//text to see if lasts ends with . and next starts with capital

				//-1 if no chars
				ClastChar=getLastChar(content[child]);
				if(debug){

					CFirstChar=getFirstChar(content[child]);
					MlastChar=getLastChar(content[master]);

                    final StringBuilder child_textX = Strip.stripXML(content[child],isXMLExtraction);
					final String master_textX =Strip.stripXML(content[master],isXMLExtraction).toString();

					//
                    
				}

				if (ClastChar!=-1) {
					
					addAlignmentFormatting(estimateParagraphs, middlePage, f_x1, f_x2, quarter, child);

					//see if we insert a line break and merge
					String lineSpace = "</p>"+SystemSeparator+"<p>";
					if(isXMLExtraction) {
                        lineSpace=SystemSeparator;
                    }

					float gap = f_y2[master] - f_y1[child];
					float line_height = f_y1[child] - f_y2[child];
					
					//Added for case where line can be less than 1 in height and cause the extraction
					//to hang and excessive new lines to be added
					if(line_height<1){
						line_height = f_y1[master] - f_y2[master];
					}
					
					if(currentWritingMode==PdfData.VERTICAL_BOTTOM_TO_TOP){
						gap = -gap;
						line_height = -line_height;
					}

					if ((gap > line_height)&(line_height>0)) { //add in line gaps

						while (gap > line_height) {
							separator.append(lineSpace);
							gap -= line_height;
						}

						if(isXMLExtraction) {
                            separator.append("</p>").append(SystemSeparator).append("<p>");
                        } else {
                            separator=new StringBuilder(SystemSeparator);
                        }
                        
                    } else if (estimateParagraphs) {
                        
                        CFirstChar=getFirstChar(content[child]);
                        MlastChar=getLastChar(content[master]);
                        
                        if ((((MlastChar=='.'))|| (((MlastChar=='\"'))))&&((CFirstChar>='A')&& (CFirstChar<='Z'))){
                            if(isXMLExtraction) {
                                separator.append("</p>").append(SystemSeparator).append("<p>");
                            } else {
                                separator=new StringBuilder(SystemSeparator);
                            }
                        }else if(fontSize[child]>70 && fontSize[child]==fontSize[master] && line_height>70 && gap>5 && line_height>0) { //add in spaces
                            
                            if(isXMLExtraction){
                                content[child].insert(0, ' ');
                            }else {
                                content[master].append(' ');
                            }
                        }

					}else{
						if(isXMLExtraction){
							content[child].insert(0, "</p>"+SystemSeparator+"<p>");
						}else {
                            content[master].append(SystemSeparator);
                        }
					}

					merge(master,child,separator.toString(),false);

			}
	}
		return master;
	}

	private int getFirstChar(final StringBuilder buffer) {
		
		int i=-1;
		boolean inTag=false;
		final int count=buffer.length();
		char openChar=' ';
		int ptr=0;
		
		while(ptr<count){
			final char nextChar=buffer.charAt(ptr);
			
			if((!inTag)&&((nextChar=='<')||(isXMLExtraction && nextChar=='&'))){
				inTag=true;
				openChar=nextChar;
				
				//trap & .... &xx; or other spurious
				if((openChar=='&')){
					if((ptr+1)==count){
						i='&';
						ptr=count;
					}else{
						final char c=buffer.charAt(ptr+1);
						
						if((c!='#')&&(c!='g')&&(c!='l')){
							i='&';
							ptr=count;
						}
					}
				}
			}
			
			if((!inTag)&&(nextChar!=' ')){
				i=nextChar;
				ptr=count;
			}
			
			//allow for valid & in stream
			if((inTag)&&(openChar=='&')&&(nextChar==' ')){
				i=openChar;
				ptr=count;
			}else if((inTag)&&((nextChar=='>')||(isXMLExtraction && openChar=='&' && nextChar==';'))){
				
				//put back < or >
				if(nextChar==';' && openChar=='&' && ptr>2 && buffer.charAt(ptr-1)=='t'){
					if((buffer.charAt(ptr-2)=='l')){
						i='<';
						ptr=count;
					}else if((buffer.charAt(ptr-2)=='g')){
						i='>';
						ptr=count;
					}
				}
				
				inTag=false;
			}
			
			ptr++;
		}
		
		return i;
	}

	/**return char as int or -1 if no match*/
	private int getLastChar(final StringBuilder buffer) {
		
		int i=-1;
		boolean inTag=false;
		int count=buffer.length();
		final int size=count;
		char openChar=' ';
		count--; //knock 1 off so points to last char
		
		while(count>-1){
			final char nextChar=buffer.charAt(count);
			
			//trap &xx;;
			if(inTag && openChar==';' && nextChar==';'){
				i=';';
				count=-1;
			}
			
			if(!inTag &&(nextChar=='>'||(isXMLExtraction && nextChar==';'))){
				inTag=true;
                
                //check it is a token and not just > at end
                final int lastTokenStart=buffer.lastIndexOf("</"); //find start of this tag if exists
                if(lastTokenStart==-1){ //no tag so ignore
                    inTag=false;
                    
                }else{ //see if real token by looking for invalid chars inside and reject if found
                   char charToTest;
                   for(int ptr=lastTokenStart;ptr<count;ptr++){
                       charToTest=buffer.charAt(ptr);
                       if(charToTest==' ' || charToTest=='>'){
                           inTag=false;
                           ptr=count;
                       }
                   }
                }
                
                if(inTag) {
                    openChar=nextChar;
                } else{
                    i=nextChar;
                    count=-1;
                }
			}
			
			if(!inTag && nextChar!=32){
				i=nextChar;
				count=-1;
			}
			
			if(nextChar=='<' ||(isXMLExtraction && openChar==';' && nextChar=='&')){
				inTag=false;
				
				//put back < or >
				if(nextChar=='&' && (count+3<size) && (buffer.charAt(count+2)=='t') && (buffer.charAt(count+3)==';')){
					if((buffer.charAt(count+1)=='l')){
						i='<';
						count=-1;
					}else if((buffer.charAt(count+1)=='g')){
						i='>';
						count=-1;
					}
				}
			}
			
			if(inTag && openChar==';' && nextChar==' '){
				count=-1;
				i=';';
			}
			count--;
		}
		
		return i;
	}

	/**
	 * reverse order in matrix so back to front
	 */
	private static int[] reverse(final int[] indices) {
		final int count =indices.length;
		final int[] newIndex=new int[count];
		for(int i=0;i<count;i++){
			newIndex[i]=indices[count-i-1];
		}
		return newIndex;
	}

	/**
	 * used to add LEFT,CENTER,RIGHT tags into XML when extracting text
	 */
	private void addAlignmentFormatting(final boolean estimateParagraphs, final int middlePage, final float[] f_x1, final float[] f_x2, final int quarter, final int child) {
		//put in some alignment
		final float left_gap = middlePage - f_x1[child];
		final float right_gap = f_x2[child] - middlePage;
		if ((!estimateParagraphs)&&(isXMLExtraction)&&
				(left_gap > 0)&& (right_gap > 0)&& (f_x1[child] > quarter)&& (f_x1[child] < (middlePage + quarter))) {
			
			float ratio = left_gap / right_gap;
			if (ratio > 1) {
                ratio = 1 / ratio;
            }
			
			if (ratio > 0.95){  //add centring if seems centered around middle
				content[child] =new StringBuilder(Fonts.cleanupTokens(content[child].toString()));
				content[child].insert(0,"<center>");
				content[child].append("</center>\n");
			}else if ((right_gap < 10) & (left_gap > 30)){  //add right align
				content[child] =new StringBuilder(Fonts.cleanupTokens(content[child].toString()));
				content[child].insert(0,"<right>");
				content[child].append("</right>\n");
					
			}
		}
	}

	/**
	 * convert fragments into lines of text
	 */
	@SuppressWarnings("unused")
	private void createLinesForSearch(final int count, int[] items, final int mode, final boolean breakOnSpace, final boolean addMultiplespaceXMLTag, final boolean sameLineOnly, final boolean isSearch) throws PdfException{
		
		String separator;

		final boolean debug=false;

		/**
		 * create local copies of arrays 
		 */
		final float[] f_x1;
        final float[] f_x2;
        final float[] f_y1;
        final float[] f_y2;

        /**
		 * reverse order if text right to left
		 */
		if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM) {
            items=reverse(items);
        }

		/**
		 * set pointers so left to right text
		 */
		if(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT){
			f_x1=this.f_x1;
			f_x2=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
			f_x2=this.f_x1;
			f_x1=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP){
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			f_y1=this.f_x2;
			f_y2=this.f_x1;
		}else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM){
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			f_y2=this.f_x1;
			f_y1=this.f_x2;
		}else{
			throw new PdfException("Illegal value "+mode+"for currentWritingMode");
		}
        
		/**
		 * scan items joining best fit to right of each fragment to build
		 * lines. This is tedious and processor intensive but necessary as the
		 * order cannot be guaranteed
		 */
		for (int j = 0; j < count; j++) {
			    
			int id = -1, i;
			final int c=items[j];
			
			//float smallest_gap = -1, gap, yMidPt;
			if(!isUsed[c] && this.writingMode[c]==mode) {
				
				if(debug) {
                    System.out.println("Look for match with "+removeHiddenMarkers(content[c].toString()));
                }
                        
					for (int j2 = 0; j2 < count && id==-1; j2++) {
                        //System.out.println("j2=="+j2);
						i=items[j2];

						if(!isUsed[i] && c!=i && this.writingMode[c]==this.writingMode[i]){

							//amount of variation in bottom of text
							//int baseLineDifference = (int) (f_y2[i] - f_y2[c]);
							//if (baseLineDifference < 0) {
                                //baseLineDifference = -baseLineDifference;
                            //}
							
							//amount of variation in bottom of text
							//int topLineDifference = (int) (f_y1[i] - f_y1[c]);
							//if (topLineDifference < 0) {
                                //topLineDifference = -topLineDifference;
                            //}

							// line gap
							//int lineGap = (int) (f_x1[i] - f_x2[c]);
							
							//Check if fragments are closer from the other end
							//if(!isSearch && lineGap>(int) (f_x1[c] - f_x2[i])) {
                              //  lineGap = (int) (f_x1[c] - f_x2[i]);
                            //}
							
							//int fontSizeChange=fontSize[c]-fontSize[i];
							//if(fontSizeChange<0) {
                                //fontSizeChange=-fontSizeChange;
                            //}
                            
                            //Get central points
                            float mx = f_x1[c] + ((f_x2[c] - f_x1[c])/2);
                            float my = f_y2[c] + ((f_y1[c] - f_y2[c])/2);
                            float cx = f_x1[i] + ((f_x2[i] - f_x1[i])/2);
                            float cy = f_y2[i] + ((f_y1[i] - f_y2[i])/2);
                            
                            float smallestHeight = (f_y1[c] - f_y2[c]);
                            float fontDifference = (f_y1[i] - f_y2[i])-smallestHeight;
                            if(fontDifference<0){
                                smallestHeight = (f_y1[i] - f_y2[i]);
                            }
                            
                            //Don't merge is font of 1 is twice the size
                            if(Math.abs(fontDifference)<smallestHeight*2){
                                //Check for the same line by checking the center of 
                                //child is within master area
                                if(Math.abs(my-cy)<(smallestHeight*0.5)){
                                    if(mx<cx){//Child on right
                                        //System.out.println("distance : "+f_x1[c]+", "+f_x2[c]+" :: "+f_x1[i]+", "+f_x2[i]);
                                        float distance = f_x1[i]-f_x2[c];
                                        //System.out.println("distance : "+distance);
                                        if(distance<=smallestHeight/2){
                                            id = i;
                                        }
                                    }
//                                else{//Child on left
//                                    float distance = f_x1[c]-f_x2[i];
//                                    if(distance<=smallestHeight/4){
//                                        id = i;
//                                    }
//                                }
                                }
                            }
                            //Match has been found
                            if(id!=-1){
                                
                                float possSpace=f_x1[id]-f_x2[c];					
                                if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM) {
                                    possSpace=-possSpace;
                                }
//                                else{
//                                	if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP) {
//                                		possSpace=(f_x2[c]-f_x1[id]);
//                                	}
//                                }
                        
                                //add space if gap between this and last object
                                separator =isGapASpace(c,id,possSpace,addMultiplespaceXMLTag,mode);
					
                                /** merge if adjoin */
                                if ((breakOnSpace)&&(hadSpace!=null)&&((hadSpace[c])||(separator.startsWith(" ")))) {
                                    break;
                                }
                                //System.out.println("c=="+c);
                                //System.out.println("id=="+id);
                                if (debug) {
                                    System.out.println("Merge items "+c+" & "+id);
                                    System.out.println("c  : "+removeHiddenMarkers(content[c].toString()));
                                    System.out.println("id : "+removeHiddenMarkers(content[id].toString()));
                                    System.out.println("");
                                }
                                if ((isSearch && (i!=c && 
									((f_x1[i] > f_x1[c] && mode!=PdfData.VERTICAL_TOP_TO_BOTTOM) ||
									(f_x1[i] < f_x1[c] && mode==PdfData.VERTICAL_TOP_TO_BOTTOM) && 
									writingMode[c]==mode)))
									||
									(!isSearch && (i!=c &&((f_x1[i] > f_x1[c] && mode!=PdfData.VERTICAL_TOP_TO_BOTTOM)||
									f_x1[i] < f_x1[c] && mode==PdfData.VERTICAL_TOP_TO_BOTTOM && writingMode[c]==mode
									)))
									) { //see if on right
                                merge(c,id,separator,true);
                                }
                    
                                id = -1;
                            }
						}
					}

//					//merge on next right item or exit when no more matches
//					if (id == -1) {
//                        break;
//                    }
//
//					float possSpace=f_x1[id]-f_x2[c];					
//				    if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM) {
//                        possSpace=-possSpace;
//                    } else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP) {
//                        possSpace=(f_x2[id]-f_x1[c]);
//                    }
//                        
//					//add space if gap between this and last object
//					separator =isGapASpace(c,id,possSpace,addMultiplespaceXMLTag,mode);
//					
//					/** merge if adjoin */
//					if ((breakOnSpace)&&(hadSpace!=null)&&((hadSpace[c])||(separator.startsWith(" ")))) {
//                        break;
//                    }
//					
//					merge(c,id,separator,true);



					//id = -1; //reset
					//smallest_gap = 1000000; //and reset the gap

			}
		}
	}
    
	/**
	 * convert fragments into lines of text
	 */
	private void createLines(final int count, int[] items, final int mode, final boolean breakOnSpace, final boolean addMultiplespaceXMLTag, final boolean sameLineOnly, final boolean isSearch) throws PdfException{
		
		String separator;

		final boolean debug=false;

		/**
		 * create local copies of arrays 
		 */
		final float[] f_x1;
        final float[] f_x2;
        final float[] f_y1;
        final float[] f_y2;

        /**
		 * reverse order if text right to left
		 */
		if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM) {
            items=reverse(items);
        }

		/**
		 * set pointers so left to right text
		 */
		if(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT){
			f_x1=this.f_x1;
			f_x2=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
			f_x2=this.f_x1;
			f_x1=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP){
			f_x1=this.f_y1;
			f_x2=this.f_y2;
			f_y1=this.f_x2;
			f_y2=this.f_x1;
		}else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM){
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			f_y2=this.f_x1;
			f_y1=this.f_x2;
		}else{
			throw new PdfException("Illegal value "+mode+"for currentWritingMode");
		}

		/**
		 * scan items joining best fit to right of each fragment to build
		 * lines. This is tedious and processor intensive but necessary as the
		 * order cannot be guaranteed
		 */
		for (int j = 0; j < count; j++) {
			
			int id = -1, i;
			final int c=items[j];
			
			float smallest_gap = -1, gap, yMidPt;
			if(!isUsed[c] && this.writingMode[c]==mode) {
				
				if(debug) {
                    System.out.println("Look for match with "+removeHiddenMarkers(content[c].toString()));
                }

				while (true) {
					for (int j2 = 0; j2 < count; j2++) {
						i=items[j2];

						if(!isUsed[i]){

							//amount of variation in bottom of text
							int baseLineDifference = (int) (f_y2[i] - f_y2[c]);
							if (baseLineDifference < 0) {
                                baseLineDifference = -baseLineDifference;
                            }
							
							//amount of variation in bottom of text
							int topLineDifference = (int) (f_y1[i] - f_y1[c]);
							if (topLineDifference < 0) {
                                topLineDifference = -topLineDifference;
                            }

							// line gap
							int lineGap = (int) (f_x1[i] - f_x2[c]);
							
							//Check if fragments are closer from the other end
							if(!isSearch && lineGap>(int) (f_x1[c] - f_x2[i])) {
                                lineGap = (int) (f_x1[c] - f_x2[i]);
                            }
							
							int fontSizeChange=fontSize[c]-fontSize[i];
							if(fontSizeChange<0) {
                                fontSizeChange=-fontSizeChange;
                            }

							if(debug) {
                                System.out.println("Against "+removeHiddenMarkers(content[i].toString()));
                            }

							if(sameLineOnly && lineGap>fontSize[c] && lineGap>0){ //ignore text in wrong order allowing slight margin for error
								// allow for multicolumns with gap

								if(debug) {
                                    System.out.println("case1 lineGap="+lineGap);
//							//Case removed as it broke one file and had no effect on other files
//							}else if (sameLineOnly && (lineGap > (fontSize[c]*10)|| lineGap > (fontSize[i]*10)) ) { //JUMP IN TEXT SIZE ACROSS COL
//								//ignore
//
//								if(debug)
//									System.out.println("case2");
                                }		
                            }else if (sameLineOnly && baseLineDifference > 1 && lineGap > 2 * fontSize[c] && (fontSize[c] == fontSize[i])) { //TEXT SLIGHTLY OFFSET
								//ignore
								if(debug) {
                                    System.out.println("case3");
                                }
							}else if(sameLineOnly && baseLineDifference>3){
								//ignore
								if(debug) {
                                    System.out.println("case4");
                                }
							}else if(sameLineOnly && fontSizeChange>2){
								//ignore
								if(debug) {
                                    System.out.println("case5");
                                }
							}else if ((isSearch && (i!=c && !(lineGap > 2 *fontSize[c] || -lineGap > 2 *fontSize[c]) && 
									((f_x1[i] > f_x1[c] && mode!=PdfData.VERTICAL_TOP_TO_BOTTOM) ||
									(f_x1[i] < f_x1[c] && mode==PdfData.VERTICAL_TOP_TO_BOTTOM) && 
									writingMode[c]==mode && 
									(!(fontSizeChange>2) || (fontSizeChange>2 && topLineDifference<3)))))
									||
									(!isSearch && (i!=c &&((f_x1[i] > f_x1[c] && mode!=PdfData.VERTICAL_TOP_TO_BOTTOM)||
									f_x1[i] < f_x1[c] && mode==PdfData.VERTICAL_TOP_TO_BOTTOM && writingMode[c]==mode 
									&& (!(fontSizeChange>2) || (fontSizeChange>2 && topLineDifference<3))
									)))
									) { //see if on right

								gap = (f_x1[i] - f_x2[c]);

								if(debug) {
                                    System.out.println("case6 gap="+gap);
                                }

								if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM) {
                                    gap=-gap;
                                }

								//allow for fp error
								if ((gap < 0) && (gap > -2)) {
                                    gap = 0;
                                }

								//make sure on right
								yMidPt = (f_y1[i] + f_y2[i]) / 2;

								//see if line & if only or better fit
								if ((yMidPt < f_y1[c])&& (yMidPt > f_y2[c])&&((smallest_gap < 0)|| (gap < smallest_gap))) {
									smallest_gap = gap;
									id = i;
								}	
							}
						}
					}

					//merge on next right item or exit when no more matches
					if (id == -1) {
                        break;
                    }

					float possSpace=f_x1[id]-f_x2[c];					
				    if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM) {
                        possSpace=-possSpace;
                    } else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP) {
                        possSpace=(f_x2[id]-f_x1[c]);
                    }
                        
					//add space if gap between this and last object
					separator =isGapASpace(c,id,possSpace,addMultiplespaceXMLTag,mode);
					
					/** merge if adjoin */
					if ((breakOnSpace)&&(hadSpace!=null)&&((hadSpace[c])||(separator.startsWith(" ")))) {
                        break;
                    }
					
					merge(c,id,separator,true);



					id = -1; //reset
					smallest_gap = 1000000; //and reset the gap

				}
			}
		}
	}

    static class ResultsComparatorRectangle implements Comparator {
		private final int rotation;
		
		ResultsComparatorRectangle(final int rotation) {
			this.rotation = rotation;
		}
		
		@Override
        public int compare(final Object o1, final Object o2) {
			final Rectangle ra1;
			final Rectangle ra2;

			if(o1 instanceof Rectangle[]){
				ra1 = ((Rectangle[]) o1)[0];
			}else {
                ra1 = (Rectangle) o1;
            }

			if(o2 instanceof Rectangle[]){
				ra2 = ((Rectangle[]) o2)[0];
			}else {
                ra2 = (Rectangle) o2;
            }

            //Orginal code kept incase of mistake.
            if (rotation == 0 || rotation == 180) {
                if (ra1.y == ra2.y) { // the two words on on the same level so pick the one on the left
                    if (ra1.x > ra2.x) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (ra1.y > ra2.y) { // the first word is above the second, so pick the first
                    return -1;
                }

                return 1; // the second word is above the first, so pick the second
            } else { // rotation == 90 or 270
                if (ra1.x == ra2.x) { // the two words on on the same level so pick the one on the left
                    if (ra1.y > ra2.y) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (ra1.x > ra2.x) { // the first word is above the second, so pick the first
                    return 1;
                }
                return -1; // the second word is above the first, so pick the second
            }
		}
	}
    
	static class ResultsComparator implements Comparator {
		private final int rotation;
		
		ResultsComparator(final int rotation) {
			this.rotation = rotation;
		}
		
		@Override
        public int compare(final Object o1, final Object o2) {
			final int[][] ra1;
			final int[][] ra2;

			if(o1 instanceof int[][]){
				ra1 = (int[][]) o1;
			}else {
                ra1 = new int[][]{(int[]) o1};
            }

			if(o2 instanceof int[][]){
				ra2 = (int[][]) o2;
			}else {
                ra2 = new int[][]{(int[]) o2};
            }

			for(int i=0; i!=ra1.length; i++) {
                for (int j = 0; j != ra2.length; j++) { //do we need this loop?
                    final int[] r1 = ra1[i];
                    final int[] r2 = ra2[j];

                    switch (rotation) {
                        case 0:
                            if (r1[1] == r2[1]) { // the two words on on the same level so pick the one on the left
                                if (r1[0] > r2[0]) {
                                    return 1;
                                } else {
                                    return -1;
                                }
                            } else if (r1[1] > r2[1]) { // the first word is above the second, so pick the first
                                return -1;
                            }

                            return 1;// the second word is above the first, so pick the second

                        case 90:
                            if (r1[0] == r2[0]) { // the two words on on the same level so pick the one on the left
                                if (r1[1] > r2[1]) {
                                    return 1;
                                } else {
                                    return -1;
                                }
                            } else if (r1[0] > r2[0]) { // the first word is above the second, so pick the first
                                return 1;
                            }

                            return -1; // the second word is above the first, so pick the second

                        case 180:
                            if (r1[1] == r2[1]) { // the two words on on the same level so pick the one on the left
                                if (r1[0] > r2[0]) {
                                    return 1;
                                } else {
                                    return -1;
                                }
                            } else if (r1[1] > r2[1]) { // the first word is above the second, so pick the first
                                return -1;
                            }

                            return 1;// the second word is above the first, so pick the second

                        case 270:
                            if (r1[0] == r2[0]) { // the two words on on the same level so pick the one on the left
                                if (r1[1] > r2[1]) {
                                    return 1;
                                } else {
                                    return -1;
                                }
                            } else if (r1[0] < r2[0]) { // the first word is above the second, so pick the first
                                return 1;
                            }

                            return -1; // the second word is above the first, so pick the second
                    }


                    //Orginal code kept incase of mistake.
//					if (rotation == 0 || rotation == 180) {
//						if (r1.y == r2.y) { // the two words on on the same level so pick the one on the left
//							if (r1.x > r2.x)
//								return 1;
//							else
//								return -1;
//						} else if (r1.y > r2.y) { // the first word is above the second, so pick the first
//							return -1;
//						}
//
//						return 1; // the second word is above the first, so pick the second
//					}
//					else { // rotation == 90 or 270
//						if (r1.x == r2.x) { // the two words on on the same level so pick the one on the left
//							if (r1.y > r2.y)
//								return 1;
//							else
//								return -1;
//						} else if (r1.x > r2.x) // the first word is above the second, so pick the first
//							return 1;
//
//						return -1; // the second word is above the first, so pick the second
//					}
                }
            }
			return -1; // the second word is above the first, so pick the second
		}
	}
	
	//<link><a name="findMultipleTermsInRectangleWithMatchingTeasers" />
	/**
	 * Algorithm to find multiple text terms in x1,y1,x2,y2 rectangle on <b>page_number</b>, with matching teaser
	 * 
	 * @param x1 the left x cord
	 * @param y1 the upper y cord
	 * @param x2 the right x cord
	 * @param y2 the lower y cord
	 * @param rotation the rotation of the page to be searched
	 * @param page_number the page number to search on
	 * @param terms the terms to search for
	 * @param searchType searchType the search type made up from one or more constants obtained from the SearchType class
	 * @param listener an implementation of SearchListener is required, this is to enable searching to be cancelled
	 * @return a SortedMap containing a collection of Rectangle describing the location of found text, mapped to a String
	 * which is the matching teaser 
	 * @throws PdfException If the co-ordinates are not valid
	 */
	public SortedMap findMultipleTermsInRectangleWithMatchingTeasers(final int x1, final int y1, final int x2, final int y2, final int rotation,
			final int page_number, final String[] terms, final int searchType, final SearchListener listener) throws PdfException {
		
		usingMultipleTerms = true;
		multipleTermTeasers.clear();
		teasers = null;
		
		final boolean origIncludeTease = includeTease;
		
		includeTease = true;
		
		final List highlights = findMultipleTermsInRectangle(x1, y1, x2, y2, page_number, terms, searchType, listener);

		final SortedMap highlightsWithTeasers = new TreeMap(new ResultsComparatorRectangle(rotation));
		
		for (int i = 0; i < highlights.size(); i++) {

			/*highlights.get(i) is a rectangle or a rectangle[]*/
			highlightsWithTeasers.put(highlights.get(i),  multipleTermTeasers.get(i));
		}

		usingMultipleTerms = false;
		
		includeTease = origIncludeTease;
		
		return highlightsWithTeasers;
	}
	
    /**
	 * Method to search a specified area on a specified page for a search term.
     * The returned map contains a set of coordinate for found values and a teaser.
     * A teaser is the found search term and surrounding text.
	 * 
	 * @param x1 the left x cord
	 * @param y1 the upper y cord
	 * @param x2 the right x cord
	 * @param y2 the lower y cord
	 * @param rotation the rotation of the page to be searched
	 * @param page_number the page number to search on
	 * @param terms the terms to search for
	 * @param searchType searchType the search type made up from one or more constants obtained from the SearchType class
	 * @param listener an implementation of SearchListener is required, this is to enable searching to be cancelled
	 * @return a SortedMap containing an int[] of coordinates as the key and a String teaser as the value
	 * @throws PdfException If the co-ordinates are not valid
	 */
	public SortedMap findTextWithinInAreaWithTeasers(final int x1, final int y1, final int x2, final int y2, final int rotation,
			final int page_number, final String[] terms, final int searchType, final SearchListener listener) throws PdfException {
		
		usingMultipleTerms = true;
		multipleTermTeasers.clear();
		teasers = null;
		
		final boolean origIncludeTease = includeTease;
		
		includeTease = true;
		
		final List highlights = findTextWithinArea(x1, y1, x2, y2, terms, searchType, listener);

		final SortedMap highlightsWithTeasers = new TreeMap(new ResultsComparator(rotation));
		
		for (int i = 0; i < highlights.size(); i++) {

			/*highlights.get(i) is a rectangle or a rectangle[]*/
			highlightsWithTeasers.put(highlights.get(i),  multipleTermTeasers.get(i));
		}

		usingMultipleTerms = false;
		
		includeTease = origIncludeTease;
		
		return highlightsWithTeasers;
	}
    
	//<link><a name="findMultipleTermsInRectangle" />
	/**
	 * Algorithm to find multiple text terms in x1,y1,x2,y2 rectangle on <b>page_number</b>.
	 * 
	 * @param x1 the left x cord
	 * @param y1 the upper y cord
	 * @param x2 the right x cord
	 * @param y2 the lower y cord
	 * @param rotation the rotation of the page to be searched
	 * @param page_number the page number to search on
	 * @param terms the terms to search for
	 * @param orderResults if true the list that is returned is ordered to return the resulting rectangles in a 
	 * logical order descending down the page, if false, rectangles for multiple terms are grouped together.
	 * @param searchType searchType the search type made up from one or more constants obtained from the SearchType class
	 * @param listener an implementation of SearchListener is required, this is to enable searching to be cancelled
	 * @return a list of Rectangle describing the location of found text
	 * @throws PdfException If the co-ordinates are not valid
	 */
	public List findMultipleTermsInRectangle(final int x1, final int y1, final int x2, final int y2, final int rotation,
			final int page_number, final String[] terms, final boolean orderResults, final int searchType, final SearchListener listener) throws PdfException {
		
		usingMultipleTerms = true;
		multipleTermTeasers.clear();
		teasers = null;
		
		final List highlights = findMultipleTermsInRectangle(x1, y1, x2, y2, page_number, terms, searchType, listener);
		
		if (orderResults) {
			Collections.sort(highlights, new ResultsComparator(rotation));
		}
		
		usingMultipleTerms = false;
		
		return highlights;
	}

	private List findMultipleTermsInRectangle(final int x1, final int y1, final int x2, final int y2, final int page_number, final String[] terms, final int searchType,
			final SearchListener listener) throws PdfException {
		
        final List list = new ArrayList();

        for (final String term : terms) {
            if (listener != null && listener.isCanceled()) {
//				System.out.println("RETURNING EARLY");
                break;
            }

            final float[] co_ords;

            co_ords = findText(x1, y1, x2, y2, new String[]{term}, searchType);

            if (co_ords != null) {
                final int count = co_ords.length;
                for (int ii = 0; ii < count; ii += 5) {

                    int wx1 = (int) co_ords[ii];
                    int wy1 = (int) co_ords[ii + 1];
                    int wx2 = (int) co_ords[ii + 2];
                    int wy2 = (int) co_ords[ii + 3];

                    Rectangle rectangle = new Rectangle(wx1, wy2, wx2 - wx1, wy1 - wy2);

                    int seperator = (int) co_ords[ii + 4];

                    if (seperator == linkedSearchAreas) {
                        final Vector_Rectangle vr = new Vector_Rectangle();
                        vr.addElement(rectangle);
                        while (seperator == linkedSearchAreas) {
                            ii += 5;
                            wx1 = (int) co_ords[ii];
                            wy1 = (int) co_ords[ii + 1];
                            wx2 = (int) co_ords[ii + 2];
                            wy2 = (int) co_ords[ii + 3];
                            seperator = (int) co_ords[ii + 4];
                            rectangle = new Rectangle(wx1, wy2, wx2 - wx1, wy1 - wy2);
                            vr.addElement(rectangle);
                        }
                        vr.trim();
                        list.add(vr.get());
                    } else {
                        list.add(rectangle);
                    }
                }
            }
        }
		return list;
	}

    private List findTextWithinArea(final int x1, final int y1, final int x2, final int y2, final String[] terms, final int searchType,
									final SearchListener listener) throws PdfException {
		
        final List list = new ArrayList();

        for (final String term : terms) {
            if (listener != null && listener.isCanceled()) {
//				System.out.println("RETURNING EARLY");
                break;
            }

            final float[] co_ords;

            co_ords = findText(x1, y1, x2, y2, new String[]{term}, searchType);

            if (co_ords != null) {
                final int count = co_ords.length;
                for (int ii = 0; ii < count; ii += 5) {

                    int wx1 = (int) co_ords[ii];
                    int wy1 = (int) co_ords[ii + 1];
                    int wx2 = (int) co_ords[ii + 2];
                    int wy2 = (int) co_ords[ii + 3];

                    int[] rectangle = {wx1, wy2, wx2 - wx1, wy1 - wy2};

                    int seperator = (int) co_ords[ii + 4];

                    if (seperator == linkedSearchAreas) {
                        final Vector_Rectangle_Int vr = new Vector_Rectangle_Int();
                        vr.addElement(rectangle);
                        while (seperator == linkedSearchAreas) {
                            ii += 5;
                            wx1 = (int) co_ords[ii];
                            wy1 = (int) co_ords[ii + 1];
                            wx2 = (int) co_ords[ii + 2];
                            wy2 = (int) co_ords[ii + 3];
                            seperator = (int) co_ords[ii + 4];
                            rectangle = new int[]{wx1, wy2, wx2 - wx1, wy1 - wy2};
                            vr.addElement(rectangle);
                        }
                        vr.trim();
                        list.add(vr.get());
                    } else {
                        list.add(rectangle);
                    }
                }
            }
        }
		return list;
	}
    
	/**
	 * Search a particular area with in pdf page currently loaded
	 * @param x1 is the x coord of the top left corner
	 * @param y1 is the y coord of the top left corner
	 * @param x2 is the x coord of the bottom right corner
	 * @param y2 is the y coord of the bottom right corner
	 * @param terms : String[] of search terms, each String is treated as a single term
	 * @param searchType : int containing bit flags for the search (See class SearchType)
	 * @return the coords of the found text in a float[] where the coords are pdf page coords.
	 * The origin of the coords is the bottom left hand corner (on unrotated page) organised in the following order.<br>
	 * [0]=result x1 coord<br>
	 * [1]=result y1 coord<br>
	 * [2]=result x2 coord<br>
	 * [3]=result y2 coord<br>
	 * [4]=either -101 to show that the next text area is the remainder of this word on another line else any other value is ignored.<br>
	 * @throws PdfException
	 */
    @SuppressWarnings("UnusedParameters")
    public final float[] findText(
    		int x1,
    		int y1,
    		int x2,
    		int y2,
			final String[] terms,
			final int searchType)
	throws PdfException {

		//Failed to supply search terms to do nothing
		if (terms == null) {
            return new float[]{};
        }
		
		//Search result and teaser holders
		final Vector_Float resultCoords = new Vector_Float(0);
		final Vector_String resultTeasers = new Vector_String(0);
		
		//make sure co-ords valid and throw exception if not
		final int[] v = validateCoordinates(x1, y1, x2, y2);
		x1 = v[0];
		y1 = v[1];
		x2 = v[2];
		y2 = v[3];
		
		//Extract the text data into local arrays for searching
		copyToArraysPartial(x1, y2, x2, y1);
		
		//Remove any hidden text on page as should not be found
		cleanupShadowsAndDrownedObjects(false);

		//Get unused text objects and sort them for correct searching
		final int[] items = getsortedUnusedFragments(true, false);

		final int[] unsorted = getWritingModeCounts(items);
		final int[] writingModes = getWritingModeOrder(unsorted);

		for(int u=0; u!=writingModes.length; u++){

			final int mode = writingModes[u];

			//if not lines for writing mode, ignore
			if(unsorted[mode]!=0){
                searchWritingMode(items, mode, searchType, terms, resultCoords, resultTeasers);
            }
            
		}
		//Return coord data for search results
		return resultCoords.get();
		 
	}

	/**
	 * Deprecated on 20/06/2014, please use findText(int x1, int y1, int x2, int y2, String[] terms, int searchType).<br>
	 * Note: input variable page_number no longer functions due to refactoring. FindText functions for the currently decoded page.
	 * @deprecated
	 */
    @SuppressWarnings("UnusedParameters")
    public final float[] findText(
			final Rectangle searchArea,
			final int page_number,
			final String[] terms,
			final int searchType)
	throws PdfException {
        return findText(searchArea.x, searchArea.y, 
                searchArea.x+searchArea.width, searchArea.y+searchArea.height, 
                terms, searchType);
	}
    
    //<link><a name="findTextInRectangle" />
	/**
	 * Method to find text in the specified area allowing for the text to be split across multiple lines.<br>
	 * @param terms = the text to search for
	 * @param searchType = info on how to search the pdf
	 * @return the coords of the found text in a float[] where the coords are pdf page coords.
	 * The origin of the coords is the bottom left hand corner (on unrotated page) organised in the following order.<br>
	 * [0]=result x1 coord<br>
	 * [1]=result y1 coord<br>
	 * [2]=result x2 coord<br>
	 * [3]=result y2 coord<br>
	 * [4]=either -101 to show that the next text area is the remainder of this word on another line else any other value is ignored.<br>
	 * @throws PdfException
	 */
    public final float[] findText(
			final String[] terms,
			final int searchType)
	throws PdfException {

		//Failed to supply search terms to do nothing
		if (terms == null) {
            return new float[]{};
        }
        
		//Search result and teaser holders
		final Vector_Float resultCoords = new Vector_Float(0);
		final Vector_String resultTeasers = new Vector_String(0);

		//Extract the text data into local arrays for searching
		copyToArrays();

		//Remove any hidden text on page as should not be found
		cleanupShadowsAndDrownedObjects(false);

		//Get unused text objects and sort them for correct searching
		final int[] items = getsortedUnusedFragments(true, false);

		final int[] unsorted = getWritingModeCounts(items);
		final int[] writingModes = getWritingModeOrder(unsorted);

		for(int u=0; u!=writingModes.length; u++){

			final int mode = writingModes[u];

			if(unsorted[mode]!=0){
                searchWritingMode(items, mode, searchType, terms, resultCoords, resultTeasers);
            }
		}
		//Return coord data for search results
		return resultCoords.get();
		 
	}

	private static String removeDuplicateSpaces(String textValue) {
		
		if(textValue.contains("  ")){
			
			textValue=textValue.replace("  ", " ");
			
		}
		return textValue;
	}

	/**return text teasers from findtext if generateTeasers() called  
	 * before find
	 */
	public String[] getTeasers() {
		
		return teasers;
	}
	
	/**
	 * tell find text to generate teasers as well
	 */
	public void generateTeasers() {
		
		includeTease=true;
	}
    
    private static int loadSearcherOptions(int searchType) {
        //Bitwise flags for regular expressions engine, options always required 
        int options = 0;

        //Turn on case sensitive mode
        if ((searchType & SearchType.CASE_SENSITIVE) != SearchType.CASE_SENSITIVE) {
            options = (options | Pattern.CASE_INSENSITIVE);
        }
        
        //Allow search to find split line results
        if ((searchType & SearchType.MUTLI_LINE_RESULTS) == SearchType.MUTLI_LINE_RESULTS) {
            options = (options | Pattern.MULTILINE | Pattern.DOTALL);
        }
        
        return options;
    }
    
    private int[] getWritingModeCounts(int[] items){
        
		/**
		 * check orientation and get preferred. Items not correct will be
		 * ignored
		 */
		int l2r = 0;
		int r2l = 0;
		int t2b = 0;
		int b2t = 0;

		for(int i=0; i!=items.length; i++){
			switch(writingMode[items[i]]){
			case 0 :l2r++; break;
			case 1 :r2l++; break;
			case 2 :t2b++; break;
			case 3 :b2t++; break;			
			}
		}

		return new int[]{l2r, r2l, t2b, b2t};
    }
    
    private static int[] getWritingModeOrder(int[] unsorted){
        final int[] sorted = {unsorted[0], unsorted[1], unsorted[2], unsorted[3]};

		//Set all to -1 so we can tell if it's been set yet
		final int[] writingModes = {-1,-1,-1,-1};

		Arrays.sort(sorted);

		for(int i=0; i!= unsorted.length; i++){
			for(int j=0; j < sorted.length; j++){
				if(unsorted[i]==sorted[j]){

					int pos = j - 3;
					if(pos<0) {
                        pos=-pos;
                    }

					if(writingModes[pos]==-1){
						writingModes[pos] = i;
						j=sorted.length;
					}
				}
			}
		}
        return writingModes;
    }
    
    private static String alterStringTooDisplayOrder(String testTerm) {

        String currentBlock = "";
        String searchValue = "";
        byte lastDirection = Character.getDirectionality(testTerm.charAt(0));
        for (int i = 0; i != testTerm.length(); i++) {
            byte dir = Character.getDirectionality(testTerm.charAt(i));
            
            //Only track is changing from left to right or right to left
            switch(dir){
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT : 
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC : 
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING : 
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE : 
                    dir = Character.DIRECTIONALITY_RIGHT_TO_LEFT;
                    break;
                case Character.DIRECTIONALITY_LEFT_TO_RIGHT : 
                case Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING : 
                case Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE : 
                    dir = Character.DIRECTIONALITY_LEFT_TO_RIGHT;
                    break;
                default:
                    dir = lastDirection;
                    break;
            }
            
            
            if (dir != lastDirection) { //Save and reset block is direction changed
                searchValue += currentBlock;
                currentBlock = "";
                lastDirection = dir;
            }
            
            //Store value based on writing mode
            if (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT) {
                currentBlock = testTerm.charAt(i) + currentBlock;
            } else {
                currentBlock += testTerm.charAt(i);
            }
        }
        searchValue += currentBlock;
        
        return searchValue;
    }
    
    private void searchWritingMode(int[] items, int mode, int searchType, String[] terms, Vector_Float resultCoords, Vector_String resultTeasers) throws PdfException {

        //Flags to control the different search options
        boolean firstOccuranceOnly = false;
        boolean wholeWordsOnly = false;
        boolean foundFirst = false;
        boolean useRegEx = false;

        //Merge text fragments into lines as displayed on page
        createLinesForSearch(items.length, items, mode, true, false, true, true);
        
        //Bitwise flags for regular expressions engine, options always required 
        int options = loadSearcherOptions(searchType);

        //Only find first occurance of each search term
        if ((searchType & SearchType.FIND_FIRST_OCCURANCE_ONLY) == SearchType.FIND_FIRST_OCCURANCE_ONLY) {
            firstOccuranceOnly = true;
        }

        //Only find whole words, not partial words
        if ((searchType & SearchType.WHOLE_WORDS_ONLY) == SearchType.WHOLE_WORDS_ONLY) {
            wholeWordsOnly = true;
        }

        //Allow the use of regular expressions symbols
        if ((searchType & SearchType.USE_REGULAR_EXPRESSIONS) == SearchType.USE_REGULAR_EXPRESSIONS) {
            useRegEx = true;
        }
        
        //Check if coords need swapping
        boolean valuesSwapped = (mode == PdfData.VERTICAL_BOTTOM_TO_TOP || mode == PdfData.VERTICAL_TOP_TO_BOTTOM);

        //Portions of text to perform the search on and find teasers
        final String searchText = buildSearchText(false, mode);
        final String coordsText = buildSearchText(true, mode);
        
        //Hold starting point data at page rotation
        int[] resultStart;

        //Work through the search terms one at a time
        for (int j = 0; j != terms.length; j++) {

            String searchValue = alterStringTooDisplayOrder(terms[j]);
            
            //Set the default separator between words in a search term
            String sep = " ";

            //Multiline needs space or newline to be recognised as word separators
            if ((searchType & SearchType.MUTLI_LINE_RESULTS) == SearchType.MUTLI_LINE_RESULTS) {
                sep = "[ \\\\n]+";
            }

            //if not using reg ex add reg ex literal flags around the text and word separators
            if (!useRegEx) {
                searchValue = "\\Q" + searchValue + "\\E";
                sep = "\\\\E" + sep + "\\\\Q";
            }

            //If word seperator has changed, replace all spaces with modified seperator
            if (!sep.equals(" ")) {
                searchValue = searchValue.replaceAll(" ", sep);
            }

            //Surround search term with word boundry tags to match whole words
            if (wholeWordsOnly) {
                searchValue = "\\b" + searchValue + "\\b";
            }

            //Create pattern to match search term
            final Pattern searchTerm = Pattern.compile(searchValue, options);

            //Create pattern to match search term with two words before and after
            final Pattern teaserTerm = Pattern.compile("(?:\\S+\\s)?\\S*(?:\\S+\\s)?\\S*" + searchValue + "\\S*(?:\\s\\S+)?\\S*(?:\\s\\S+)?", options);

            //So long as text data is not null
            if (searchText != null) {

                //Create two matchers for finding search term and teaser
                final Matcher termFinder = searchTerm.matcher(searchText);
                final Matcher teaserFinder = teaserTerm.matcher(searchText);
                boolean needToFindTeaser = true;

                //Keep looping till no result is returned
                while (termFinder.find()) {
                    resultStart = null;
                    //Make note of the text found and index in the text
                    String foundTerm = termFinder.group();
                    int termStarts = termFinder.start();
                    final int termEnds = termFinder.end() - 1;

                    //If storing teasers
                    if (includeTease) {

                        if (includeHTMLtags) {
                            foundTerm = "<b>" + foundTerm + "</b>";
                        }
                        
                        if (needToFindTeaser) {
                            findTeaser(foundTerm, teaserFinder, termStarts, termEnds, resultTeasers);
                        }
                    }

                    getResultCoords(coordsText, mode, resultStart, termStarts, termEnds, valuesSwapped, resultCoords);

						//If only finding first occurance,
                    //Stop searching this text data for search term.
                    if (firstOccuranceOnly) {
                        foundFirst = true;
                        break;
                    }
                }

							//If only finding first occurance and first is found,
                //Stop searching all text data for this search term.
                if (firstOccuranceOnly && foundFirst) {
                    break;
                }
            }
        }

        //Remove any trailing empty values
        resultCoords.trim();
        
        //If including tease values
        if (includeTease) {
            storeTeasers(resultTeasers);
        }
                
    }
    
    private String buildSearchText(boolean includeCoords, int mode){
        //Portions of text to perform the search on and find teasers
        String searchText;

		//Merge all text into one with \n line separators
        //This will allow checking for multi line split results
        StringBuilder str = new StringBuilder();
        for (int i = 0; i != content.length; i++) {
            if (content[i] != null && mode == this.writingMode[i]) {
                    str.append(content[i]).append('\n');
            }
        }
        
        //Remove double spaces, replacing them with single spaces
        searchText = removeDuplicateSpaces(str.toString());

        //Strip xml and coords data from content and keep text data
        if(!includeCoords){
            searchText = removeHiddenMarkers(searchText);
        }
        searchText = Strip.stripXML(searchText, isXMLExtraction).toString();
        
        //Store text in the search and teaser arrays
        return searchText;
    }
    
    private void getResultCoords(String coordText, int mode, int[] resultStart, int termStarts, int termEnds, boolean valuesSwapped, Vector_Float resultCoords){
        
        //Get coords of found text for highlights
        float currentX;
        float width;

        //Track point in text data line (without coord data)
        int pointInLine = -1;

        //Track line on page
        int lineCounter = 0;

        //Skip null values and value not in the correct writing mode to ensure correct result coords
        while (content[lineCounter] == null || mode != this.writingMode[lineCounter]) {
            lineCounter++;
        }

        //Flags used to catch if result is split accross lines
        boolean startFound = false;
        boolean endFound = false;

						//Cycle through coord text looking for coords of this result
        //Ignore first value as it is known to be the first marker
        for (int pointer = 1; pointer < coordText.length(); pointer++) {

            // find second marker and get x coord
            int startPointer = pointer;
            while (pointer < coordText.length()) {
                if (coordText.charAt(pointer) == MARKER2) {
                    break;
                }
                pointer++;
            }

            //Convert text to float value for x coord
            currentX = Float.parseFloat(coordText.substring(startPointer, pointer));
            pointer++;

            // find third marker and get width
            startPointer = pointer;
            while (pointer < coordText.length()) {
                if (coordText.charAt(pointer) == MARKER2) {
                    break;
                }

                pointer++;
            }

            //Convert text to float value for character width
            width = Float.parseFloat(coordText.substring(startPointer, pointer));
            pointer++;

            // find fourth marker and get text (character)
            startPointer = pointer;
            while (pointer < coordText.length()) {
                if (coordText.charAt(pointer) == MARKER2) {
                    break;
                }

                pointer++;
            }

            //Store text to check for newline character later
            final String text = coordText.substring(startPointer, pointer);
            pointInLine += text.length();

									//Start of term not found yet.
            //Point in line is equal to or greater than start of the term.
            //Store coords and mark start as found.
            if (!startFound && pointInLine >= termStarts) {
                int currentY = (int) f_y1[lineCounter];
                if(valuesSwapped){
                    currentY = (int) f_x2[lineCounter];
                }
                resultStart = new int[]{(int) currentX, currentY};
                startFound = true;
            }
            						//End of term not found yet.
            //Point in line is equal to or greater than end of the term.
            //Store coords and mark end as found.
            if (!endFound && pointInLine >= termEnds) {
                int currentY = (int) f_y2[lineCounter];
                if(valuesSwapped){
                    currentY = (int) f_x1[lineCounter];
                }
                storeResultsCoords(valuesSwapped, mode, resultCoords, resultStart[0], resultStart[1], (currentX + width), currentY, 0.0f);
                
                endFound = true;
            }

							//Using multi line option.
            //Start of term found.
            //End of term not found.
            //New line character found.
            //Set up multi line result.
            if (startFound && !endFound && text.contains("\n")) {

                storeResultsCoords(valuesSwapped, mode, resultCoords, resultStart[0], resultStart[1], (currentX + width), f_y2[lineCounter], linkedSearchAreas);

                //Set start of term as not found
                startFound = false;

										//Set this point in line as start of next term
                //Guarantees next character is found as 
                //start of the next part of the search term
                termStarts = pointInLine;
            }

									//In multiline mode we progress the line number when we find a \n
            //This is to allow the correct calculation of y coords
            if (text.contains("\n")) {
                lineCounter++;

                //If current content pointed at is null or not the correct writing mode, skip value until data is found
                while (lineCounter < content.length && (content[lineCounter] == null || mode != this.writingMode[lineCounter])) {
                    lineCounter++;
                }
            }

        }
    }
    
    private void storeTeasers(Vector_String resultTeasers){
        
        //Remove any trailing empty values
        resultTeasers.trim();

        //Store teasers so they can be retrieved by different search methods
        if (usingMultipleTerms) {
						//Store all teasers for so they may be returned as a sorted map
            //Only used for one method controled by the above flag
            for (int i = 0; i != resultTeasers.size(); i++) {
                multipleTermTeasers.add(resultTeasers.elementAt(i));
            }
            //Prevent issue this not getting cleared between writing modes 
            //resulting in duplicate teasers
            resultTeasers.clear();
        } else {
            //Store all teasers to be retrieved by getTeaser() method
            teasers = resultTeasers.get();
        }
    }
    
    private static void storeResultsCoords(boolean valuesSwapped, int mode, Vector_Float resultCoords, float x1, float y1, float x2, float y2, float connected){
        //Set ends coords      
        if (valuesSwapped) {
            if (mode == PdfData.VERTICAL_BOTTOM_TO_TOP) {
                resultCoords.addElement(y2);
                resultCoords.addElement(x2);
                resultCoords.addElement(y1);
                resultCoords.addElement(x1);
                resultCoords.addElement(connected); //Mark next result as linked

            } else {
                resultCoords.addElement(y2);
                resultCoords.addElement(x1);
                resultCoords.addElement(y1);
                resultCoords.addElement(x2);
                resultCoords.addElement(connected); //Mark next result as linked

            }
        } else {
            resultCoords.addElement(x1);
            resultCoords.addElement(y1);
            resultCoords.addElement(x2);
            resultCoords.addElement(y2);
            resultCoords.addElement(connected); //Mark next result as linked
        }
    }
    
    private void findTeaser(String teaser, Matcher teaserFinder, int termStarts, int termEnds, Vector_String resultTeasers){
        
        if (teaserFinder.find()) {
            //Get a teaser if found and set the search term to bold is allowed
            if (teaserFinder.start() < termStarts && teaserFinder.end() > termEnds) {

                //replace default with found teaser
                teaser = teaserFinder.group();
                
                if (includeHTMLtags) {
                    //Calculate points to add bold tags
                    final int teaseStarts = termStarts - teaserFinder.start();
                    final int teaseEnds = (termEnds - teaserFinder.start()) + 1;

                    //Add bold tags
                    teaser = teaser.substring(0, teaseStarts) + "<b>"
                            + teaser.substring(teaseStarts, teaseEnds) + "</b>"
                            + teaser.substring(teaseEnds, teaser.length());
                }
            }
        }
        
        //Store teaser
        resultTeasers.addElement(teaser);
    }
    
    private class Fragment{
        float x1, y1, x2, y2, character_spacing;
        String raw, currentColor;
        int text_length, mode;
        
        Fragment(PdfData pdf_data, int index){
            loadData(pdf_data, index);
        }
        
        public void loadData(PdfData pdf_data, int index){
            //extract values
            character_spacing = pdf_data.f_character_spacing[index];
            x1 = pdf_data.f_x1[index];
            x2 = pdf_data.f_x2[index];
            y1 = pdf_data.f_y1[index];
            y2 = pdf_data.f_y2[index];
            currentColor = pdf_data.colorTag[index];
            text_length = pdf_data.text_length[index];
            mode = pdf_data.f_writingMode[index];
            raw = pdf_data.contents[index];
        }
        
        public float getX1(){return x1;}
        public float getY1(){return y1;}
        public float getX2(){return x2;}
        public float getY2(){return y2;}
        public float getCharacterSpacing(){return character_spacing;}
        
        public String getRawData(){return raw;}
        public String getColorTag(){return currentColor;}
        
        public int getWritingMode(){return mode;}
        public int getTextLength(){return text_length;}
        
        public void setX1(float value){x1=value;}
        public void setY1(float value){y1=value;}
        public void setX2(float value){x2=value;}
        public void setY2(float value){y2=value;}
    }
}
