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
 * PageOffsets.java
 * ---------------
 */
package org.jpedal.display;

import static org.jpedal.display.Display.CONTINUOUS;
import static org.jpedal.display.Display.CONTINUOUS_FACING;
import static org.jpedal.display.Display.FACING;
import org.jpedal.objects.PdfPageData;


/**
 * holds offsets for all multiple pages
 */
public class PageOffsets {

    private int width,height;
    
    /**width of all pages*/
    private int totalSingleWidth,totalDoubleWidth,gaps,doubleGaps;

    /**height of all pages*/
    private int totalSingleHeight,totalDoubleHeight;

    protected int maxW, maxH;

    /**gap between pages*/
    public static final int pageGap=10;

    /**max widths and heights for facing and continuous pages*/
    private int doublePageWidth,doublePageHeight,biggestWidth,biggestHeight,widestPageNR,widestPageR;

    public PageOffsets(final int pageCount, final PdfPageData pageData) {


			/** calulate sizes for continuous and facing page modes */
            int pageH, pageW,rotation;
            int facingW = 0, facingH = 0;
            int greatestW = 0, greatestH = 0;
            totalSingleHeight = 0;
            totalSingleWidth = 0;

			int widestLeftPage=0,widestRightPage=0,highestLeftPage=0,highestRightPage=0;

			widestPageR=0;
			widestPageNR=0;

			totalDoubleWidth = 0;
            totalDoubleHeight = 0;
            gaps=0;
            doubleGaps=0;

            biggestWidth = 0;
            biggestHeight = 0;

			for (int i = 1; i < pageCount + 1; i++) {

				//get page sizes
                pageW = pageData.getCropBoxWidth(i);
                pageH = pageData.getCropBoxHeight(i);
				rotation = pageData.getRotation(i);

				//swap if this page rotated and flag
				if((rotation==90|| rotation==270)){
	                final int tmp=pageW;
	                pageW=pageH;
	                pageH=tmp;
				}

                if (pageW > maxW) {
                    maxW = pageW;
                }

                if (pageH > maxH) {
                    maxH = pageH;
                }

				gaps += pageGap;


				totalSingleWidth += pageW;
				totalSingleHeight += pageH;

				//track page sizes
				if(( i & 1)==1){//odd
					if(widestRightPage<pageW) {
                        widestRightPage = pageW;
                    }
					if(highestRightPage<pageH) {
                        highestRightPage = pageH;
                    }
				}else{
					if(widestLeftPage<pageW) {
                        widestLeftPage = pageW;
                    }
					if(highestLeftPage<pageH) {
                        highestLeftPage = pageH;
                    }
				}

				if(widestPageNR<pageW) {
                    widestPageNR = pageW;
                }

				if(widestPageR<pageH) {
                    widestPageR = pageH;
                }

				if (pageW > biggestWidth) {
                    biggestWidth = pageW;
                }
				if (pageH > biggestHeight) {
                    biggestHeight = pageH;
                }

				// track widest and highest combination of facing pages
                if ((i & 1) == 1) {

					if (greatestW < pageW) {
                        greatestW = pageW;
                    }
                    if (greatestH < pageH) {
                        greatestH = pageH;
                    }

					if (i == 1) {// first page special case
						totalDoubleWidth = pageW;
						totalDoubleHeight = pageH;
					} else {
                        totalDoubleWidth += greatestW;
                        totalDoubleHeight += greatestH;
					}

					doubleGaps += pageGap;

					facingW = pageW;
                    facingH = pageH;

                } else {

					facingW += pageW;
                    facingH += pageH;

					greatestW = pageW;
					greatestH = pageH;

                    if (i == pageCount) { // allow for even number of pages
                        totalDoubleWidth = totalDoubleWidth + greatestW + pageGap;
                        totalDoubleHeight = totalDoubleHeight + greatestH + pageGap;
                    }
                }

                //choose largest (to allow for rotation on specific pages)
                //int max=facingW;
                //if(max<facingH)
                //	max=facingH;

            }

			doublePageWidth=widestLeftPage+widestRightPage+pageGap;
			doublePageHeight=highestLeftPage+highestRightPage+pageGap;

            // subtract pageGap to make sum correct
            totalSingleWidth -= pageGap;
			totalSingleHeight -= pageGap;

		}

    public int getMaxH() {
        return maxH;
    }

    public int getMaxW() {
        return maxW;
    }

	public int getWidestPageR() {
		return widestPageR;
	}

	public int getWidestPageNR() {
		return widestPageNR;
	}

    /**
     * @return the totalSingleWidth
     */
    public int getTotalSingleWidth() {
        return totalSingleWidth;
    }

    /**
     * @param totalSingleWidth the totalSingleWidth to set
     */
    public void setTotalSingleWidth(int totalSingleWidth) {
        this.totalSingleWidth = totalSingleWidth;
    }

    /**
     * @return the gaps
     */
    public int getGaps() {
        return gaps;
    }

    /**
     * @param gaps the gaps to set
     */
    public void setGaps(int gaps) {
        this.gaps = gaps;
    }

    /**
     * @return the doubleGaps
     */
    public int getDoubleGaps() {
        return doubleGaps;
    }

    /**
     * @param doubleGaps the doubleGaps to set
     */
    public void setDoubleGaps(int doubleGaps) {
        this.doubleGaps = doubleGaps;
    }

    /**
     * @return the totalDoubleWidth
     */
    public int getTotalDoubleWidth() {
        return totalDoubleWidth;
    }

    /**
     * @param totalDoubleWidth the totalDoubleWidth to set
     */
    public void setTotalDoubleWidth(int totalDoubleWidth) {
        this.totalDoubleWidth = totalDoubleWidth;
    }

    /**
     * @return the totalSingleHeight
     */
    public int getTotalSingleHeight() {
        return totalSingleHeight;
    }

    /**
     * @param totalSingleHeight the totalSingleHeight to set
     */
    public void setTotalSingleHeight(int totalSingleHeight) {
        this.totalSingleHeight = totalSingleHeight;
    }

    /**
     * @return the totalDoubleHeight
     */
    public int getTotalDoubleHeight() {
        return totalDoubleHeight;
    }

    /**
     * @param totalDoubleHeight the totalDoubleHeight to set
     */
    public void setTotalDoubleHeight(int totalDoubleHeight) {
        this.totalDoubleHeight = totalDoubleHeight;
    }

    /**
     * @return the doublePageWidth
     */
    public int getDoublePageWidth() {
        return doublePageWidth;
    }

    /**
     * @param doublePageWidth the doublePageWidth to set
     */
    public void setDoublePageWidth(int doublePageWidth) {
        this.doublePageWidth = doublePageWidth;
    }

    /**
     * @return the doublePageHeight
     */
    public int getDoublePageHeight() {
        return doublePageHeight;
    }

    /**
     * @param doublePageHeight the doublePageHeight to set
     */
    public void setDoublePageHeight(int doublePageHeight) {
        this.doublePageHeight = doublePageHeight;
    }

    /**
     * @return the biggestWidth
     */
    public int getBiggestWidth() {
        return biggestWidth;
    }

    /**
     * @param biggestWidth the biggestWidth to set
     */
    public void setBiggestWidth(int biggestWidth) {
        this.biggestWidth = biggestWidth;
    }

    /**
     * @return the biggestHeight
     */
    public int getBiggestHeight() {
        return biggestHeight;
    }

    /**
     * @param biggestHeight the biggestHeight to set
     */
    public void setBiggestHeight(int biggestHeight) {
        this.biggestHeight = biggestHeight;
    }

    /**
     * @param widestPageNR the widestPageNR to set
     */
    public void setWidestPageNR(int widestPageNR) {
        this.widestPageNR = widestPageNR;
    }

    /**
     * @param widestPageR the widestPageR to set
     */
    public void setWidestPageR(int widestPageR) {
        this.widestPageR = widestPageR;
    }

    public void calculateCombinedPageSizes(int displayView,int pageNumber,int displayRotation, MultiDisplayOptions multiDisplayOptions, PdfPageData pageData, float scaling, int insetW, int insetH ) {
        
        //height for facing pages
        int biggestFacingHeight=0;
        
        if(displayView==FACING && multiDisplayOptions.getPageW()!=null){
            
            //get 2 facing page numbers
            int p1;
            final int p2;
            if (multiDisplayOptions.isSeparateCover()) {
                p1=pageNumber;
                if((p1 & 1)==1) {
                    p1--;
                }
                p2=p1+1;
            } else {
                p1=pageNumber;
                if((p1 & 1)==0) {
                    p1--;
                }
                p2=p1+1;
            }
            
            biggestFacingHeight=multiDisplayOptions.getPageH(p1);
            if(p2<multiDisplayOptions.getPageH().length && biggestFacingHeight<multiDisplayOptions.getPageH(p2)) {
                biggestFacingHeight = multiDisplayOptions.getPageH(p2);
            }
        }

        final int gaps= this.gaps;
        final int doubleGaps= this.doubleGaps;
        
        switch(displayView){
            
            case FACING:
                
                //Get widths of pages
                final int firstW;
                final int secondW;
                if ((displayRotation + pageData.getRotation(pageNumber))%180==90) {
                    firstW = pageData.getCropBoxHeight(pageNumber);
                } else {
                    firstW = pageData.getCropBoxWidth(pageNumber);
                }
                
                final int pageCount=pageData.getPageCount();
                
                if (pageNumber+1 > pageCount || (pageNumber==1 && pageCount!=2) ) {
                    secondW = firstW;
                }else {
                    if ((displayRotation + pageData.getRotation(pageNumber+1))%180==90) {
                        secondW = pageData.getCropBoxHeight(pageNumber + 1);
                    } else {
                        secondW = pageData.getCropBoxWidth(pageNumber + 1);
                    }
                }
                
                //get total width
                final int totalW = firstW + secondW;
                
                width=(int)(totalW*scaling)+ PageOffsets.pageGap;
                height=(biggestFacingHeight);	//NOTE scaled already!
                break;
                
                
            case CONTINUOUS:
                
                if((displayRotation==90)|(displayRotation==270)){
                    width=(int)(biggestHeight *scaling);
                    height=(int)((totalSingleWidth)*scaling)+gaps+insetH;
                }else{
                    width=(int)(biggestWidth *scaling);
                    height=(int)((totalSingleHeight)*scaling)+gaps+insetH;
                }
                break;
                
                
            case CONTINUOUS_FACING:
                
                if((displayRotation==90)|(displayRotation==270)){
                    width=(int)((doublePageHeight)*scaling)+(insetW*2)+doubleGaps;
                    height=(int)((totalDoubleWidth)*scaling)+doubleGaps+insetH;
                }else{
                    width=(int)((doublePageWidth)*scaling)+(insetW*2);
                    height=(int)((totalDoubleHeight)*scaling)+doubleGaps+insetH;
                }
                break;
                
        }
    }

    public int getPageWidth() {
        return width;
    }
    
    public int getPageHeight() {
        return height;
    }
}

