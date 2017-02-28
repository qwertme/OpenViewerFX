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
 * TextLines.java
 * ---------------
 */
package org.jpedal.text;

import org.jpedal.objects.PdfData;
import org.jpedal.utils.repositories.Vector_Rectangle;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

public class TextLines {

    /**stores area of arrays in which text should be highlighted*/
    private Map lineAreas = new HashMap();
    private Map lineWritingMode = new HashMap();

    /**Highlight Areas stored here*/
    public Map areas = new HashMap();
    
    /**Track if highlgiht areas has changed since last call to getHighlightedAreas(int)*/
    boolean hasHighlightAreasUpdated;

    /**
	 * Deprecated on 16/06/2014, please use setFoundParagraphAsArray(int x, int y, int page).
     * @deprecated
	 */
	public Rectangle setFoundParagraph(final int x, final int y, final int page){
        
        final int[][] rectParams = getLineAreasAs2DArray(page);
        
		if(rectParams!=null){
            final Rectangle[] lines = new Rectangle[rectParams.length];
            
            for(int i = 0; i < lines.length; i++){
                lines[i] = new Rectangle(rectParams[i][0],rectParams[i][1],rectParams[i][2],rectParams[i][3]);
            }
            
			final Rectangle point = new Rectangle(x,y,1,1);
			final Rectangle current = new Rectangle(0,0,0,0);
			boolean lineFound = false;
			int selectedLine = 0;

			for(int i=0; i!=lines.length; i++){
				if(lines[i].intersects(point)){
					selectedLine = i;
					lineFound = true;
					break;
				}
			}

			if(lineFound){
				double left = lines[selectedLine].x;
				double cx = lines[selectedLine].getCenterX();
				double right = lines[selectedLine].x+lines[selectedLine].width;
				double cy = lines[selectedLine].getCenterY();
				int h = lines[selectedLine].height;

				current.x=lines[selectedLine].x;
				current.y=lines[selectedLine].y;
				current.width=lines[selectedLine].width;
				current.height=lines[selectedLine].height;

				boolean foundTop = true;
				boolean foundBottom = true;
				final Vector_Rectangle selected = new Vector_Rectangle(0);
				selected.addElement(lines[selectedLine]);

				while(foundTop){
					foundTop = false;
					for(int i=0; i!=lines.length; i++){
						if(lines[i].contains(left, cy+h) || lines[i].contains(cx, cy+h) || lines[i].contains(right, cy+h)){
							selected.addElement(lines[i]);
							foundTop = true;
							cy = lines[i].getCenterY();
							h = lines[i].height;

							if(current.x>lines[i].x){
								current.width = (current.x+current.width)-lines[i].x;
								current.x = lines[i].x;
							}
							if((current.x+current.width)<(lines[i].x+lines[i].width)) {
                                current.width = (lines[i].x + lines[i].width) - current.x;
                            }
							if(current.y>lines[i].y){
								current.height = (current.y+current.height)-lines[i].y;
								current.y = lines[i].y;
							}
							if((current.y+current.height)<(lines[i].y+lines[i].height)){
								current.height = (lines[i].y+lines[i].height)-current.y;
							}

							break;
						}
					}
				}

				//Return to selected item else we have duplicate highlights
				left = lines[selectedLine].x;
				cx = lines[selectedLine].getCenterX();
				right = lines[selectedLine].x+lines[selectedLine].width;
				cy = lines[selectedLine].getCenterY();
				h = lines[selectedLine].height;

				while(foundBottom){
					foundBottom = false;
					for(int i=0; i!=lines.length; i++){
						if(lines[i].contains(left, cy-h) || lines[i].contains(cx, cy-h) || lines[i].contains(right, cy-h)){
							selected.addElement(lines[i]);
							foundBottom = true;
							cy = lines[i].getCenterY();
							h = lines[i].height;

							if(current.x>lines[i].x){
								current.width = (current.x+current.width)-lines[i].x;
								current.x = lines[i].x;
							}
							if((current.x+current.width)<(lines[i].x+lines[i].width)) {
                                current.width = (lines[i].x + lines[i].width) - current.x;
                            }
							if(current.y>lines[i].y){
								current.height = (current.y+current.height)-lines[i].y;
								current.y = lines[i].y;
							}
							if((current.y+current.height)<(lines[i].y+lines[i].height)){
								current.height = (lines[i].y+lines[i].height)-current.y;
							}

							break;
						}
					}
				}
				selected.trim();
				addHighlights(selected.get(), true, page);
				return current;
			}
			return null;
		}
		return null;
	}
    
    /**
	 * Highlights a section of lines that form a paragraph and
	 * returns the area that encloses all highlight areas.
	 * @return int[] that contains x,y,w,h of all areas highlighted
	 */
    public int[] setFoundParagraphAsArray(final int x, final int y, final int page){

    final int[][] lines = getLineAreasAs2DArray(page);

    if(lines!=null){

        final int[] point = {x,y,1,1};
        final int[] current = {0,0,0,0};
        boolean lineFound = false;
        int selectedLine = 0;

        for(int i=0; i!=lines.length; i++){
            if(intersects(lines[i],point)){
                selectedLine = i;
                lineFound = true;
                break;
            }
        }

        if(lineFound){
            int left = lines[selectedLine][0];
            int cx = lines[selectedLine][0]+(lines[selectedLine][2]/2);
            int right = lines[selectedLine][0]+lines[selectedLine][2];
            int cy = lines[selectedLine][1]+(lines[selectedLine][3]/2);
            int h = lines[selectedLine][3];

            current[0]=lines[selectedLine][0];
            current[1]=lines[selectedLine][1];
            current[2]=lines[selectedLine][2];
            current[3]=lines[selectedLine][3];

            boolean foundTop = true;
            boolean foundBottom = true;
            final Vector_Rectangle_Int selected = new Vector_Rectangle_Int(0);
            selected.addElement(lines[selectedLine]);

            while(foundTop){
                foundTop = false;
                for(int i=0; i!=lines.length; i++){
                    if(contains(left, cy+h, lines[i]) || contains(cx, cy+h, lines[i]) || contains(right, cy+h, lines[i])){
                        selected.addElement(lines[i]);
                        foundTop = true;
                        cy = lines[i][1] + (lines[i][3]/2);
                        h = lines[i][3];

                        if(current[0]>lines[i][0]){
                            current[2] = (current[0]+current[2])-lines[i][0];
                            current[0] = lines[i][0];
                        }
                        if((current[0]+current[2])<(lines[i][0]+lines[i][2])) {
                            current[2] = (lines[i][0] + lines[i][2]) - current[0];
                        }
                        if(current[1]>lines[i][1]){
                            current[3] = (current[1]+current[3])-lines[i][1];
                            current[1] = lines[i][1];
                        }
                        if((current[1]+current[3])<(lines[i][1]+lines[i][3])){
                            current[3] = (lines[i][1]+lines[i][3])-current[1];
                        }

                        break;
                    }
                }
            }

            //Return to selected item else we have duplicate highlights
            left = lines[selectedLine][0];
            cx = lines[selectedLine][0]+(lines[selectedLine][2]/2);
            right = lines[selectedLine][0]+lines[selectedLine][2];
            cy = lines[selectedLine][1] + (lines[selectedLine][3]/2);
            h = lines[selectedLine][3];

            while(foundBottom){
                foundBottom = false;
                for(int i=0; i!=lines.length; i++){
                    if(contains(left, cy-h, lines[i]) || contains(cx,cy-h, lines[i]) || contains(right,cy-h, lines[i])){
                        selected.addElement(lines[i]);
                        foundBottom = true;
                        cy = lines[i][1] + (lines[i][3]/2);
                        h = lines[i][3];

                        if(current[0]>lines[i][0]){
                            current[2] = (current[0]+current[2])-lines[i][0];
                            current[0] = lines[i][0];
                        }
                        if((current[0]+current[2])<(lines[i][0]+lines[i][2])) {
                            current[2] = (lines[i][0] + lines[i][2]) - current[0];
                        }
                        if(current[1]>lines[i][1]){
                            current[3] = (current[1]+current[3])-lines[i][1];
                            current[1] = lines[i][1];
                        }
                        if((current[1]+current[3])<(lines[i][1]+lines[i][3])){
                            current[3] = (lines[i][1]+lines[i][3])-current[1];
                        }

                        break;
                    }
                }
            }
            selected.trim();
            addHighlights(selected.get(), true, page);
            return current;
        }
        return null;
    }
    return null;
}



    /**
     * This method is deprecated on 12/06/2014, please use 
     * addToLineAreas(int[] area, int writingMode, int page).
     * @deprecated
     * @param area
     * @param writingMode
     * @param page 
     */
    public void addToLineAreas(final Rectangle area, final int writingMode, final int page) {
        boolean addNew = true;

        if(lineAreas==null){ //If null, create array

            //Set area
            lineAreas = new HashMap();
            lineAreas.put(page, new Rectangle[]{area});

            //Set writing direction
            lineWritingMode = new HashMap();
            lineWritingMode.put(page, new int[]{writingMode});

        }else{
            final Rectangle[] lastAreas = ((Rectangle[])lineAreas.get(page));
            final int[] lastWritingMode = ((int[])lineWritingMode.get(page));

            //Check for objects close to or intersecting each other
            if(area!=null){ //Ensure actual area is selected
                if(lastAreas!=null){
                    for(int i=0; i!= lastAreas.length; i++){
                        final int lwm = lastWritingMode[i];
                        int cx = area.x;
                        int cy = area.y;
                        int cw = area.width;
                        int ch = area.height;
                        //int cm = cy+(ch/2);

                        int lx = lastAreas[i].x;
                        int ly = lastAreas[i].y;
                        int lw = lastAreas[i].width;
                        int lh = lastAreas[i].height;
                        //int lm = ly+(lh/2);

                        final int currentBaseLine;
                        final int lastBaseLine;
                        final float heightMod = 5f;
                        final float widthMod = 1.1f;

                        switch(writingMode){
                            case PdfData.HORIZONTAL_LEFT_TO_RIGHT :

                                if(lwm== writingMode && ((ly>(cy-(ch/heightMod))) && (ly<(cy+(ch/heightMod)))) && //Ensure this is actually the same line and are about the same size
                                        (((lh<ch+(ch/heightMod) && lh>ch-(ch/heightMod))) && //Check text is the same height
                                                (((lx>(cx + cw-(ch*widthMod))) && (lx<(cx + cw+(ch*widthMod)))) || //Check for object at end of this object
                                                        ((lx + lw>(cx-(ch*widthMod))) && (lx + lw<(cx+(ch*widthMod)))) ||//Check for object at start of this object
                                                        lastAreas[i].intersects(area)))//Check to see if it intersects at all
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }
                                break;
                            case PdfData.HORIZONTAL_RIGHT_TO_LEFT :

                                lx = lastAreas[i].x;
                                ly = lastAreas[i].y;
                                lw = lastAreas[i].width;
                                lh = lastAreas[i].height;
                                cx = area.x;
                                cy = area.y;
                                cw = area.width;
                                ch = area.height;

                                if(lwm== writingMode && ((ly>(cy-5)) && (ly<(cy+5)) && lh<=(ch+(ch/5)) && lh>=(ch-(ch/5))) && //Ensure this is actually the same line and are about the same size
                                        (((lx>(cx + cw-(ch*0.6))) && (lx<(cx + cw+(ch*0.6)))) || //Check for object at end of this object
                                                ((lx + lw>(cx-(ch*0.6))) && (lx + lw<(cx+(ch*0.6)))) ||//Check for object at start of this object
                                                lastAreas[i].intersects(area))//Check to see if it intersects at all
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }
                                break;
                            case PdfData.VERTICAL_TOP_TO_BOTTOM :

                                lx = lastAreas[i].y;
                                ly = lastAreas[i].x;
                                lw = lastAreas[i].height;
                                lh = lastAreas[i].width;
                                cx = area.y;
                                cy = area.x;
                                cw = area.height;
                                ch = area.width;

                                if(lwm== writingMode && ((ly>(cy-5)) && (ly<(cy+5)) && lh<=(ch+(ch/5)) && lh>=(ch-(ch/5))) && //Ensure this is actually the same line and are about the same size
                                        (((lx>(cx + cw-(ch*0.6))) && (lx<(cx + cw+(ch*0.6)))) || //Check for object at end of this object
                                                ((lx + lw>(cx-(ch*0.6))) && (lx + lw<(cx+(ch*0.6)))) ||//Check for object at start of this object
                                                lastAreas[i].intersects(area))//Check to see if it intersects at all
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }

                                break;

                            case PdfData.VERTICAL_BOTTOM_TO_TOP :

                                //Calculate the coord value at the bottom of the text
                                currentBaseLine = cx + cw;
                                lastBaseLine = lx + lw;

                                if(
                                        lwm== writingMode //Check the current writing mode
                                                && (currentBaseLine >= (lastBaseLine-(lw/3))) && (currentBaseLine <= (lastBaseLine+(lw/3))) //Check is same line
                                                && //Only check left or right if the same line is shared
                                                (
                                                        ( //Check for text on either side
                                                                ((ly+(lh+(lw*0.6))>cy) && (ly+(lh-(lw*0.6))<cy))// Check for text to left of current area
                                                                        || ((ly+(lw*0.6)>(cy+ch)) && (ly-(lw*0.6)<(cy+ch)))// Check for text to right of current area
                                                        )
                                                                || area.intersects(lastAreas[i])
                                                )
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }

                                break;

                        }

                    }
                }else{
                    addNew = true;
                }

                //If no object near enough to merge, start a new area
                if(addNew){

                    final Rectangle[] lineAreas;
                    final int[] lineWritingMode;

                    if(lastAreas!=null){
                        lineAreas = new Rectangle[lastAreas.length+1];
                        for(int i=0; i!= lastAreas.length; i++){
                            lineAreas[i] = lastAreas[i];
                        }
                        lineAreas[lineAreas.length-1] = area;

                        lineWritingMode = new int[lastWritingMode.length+1];
                        for(int i=0; i!= lastWritingMode.length; i++){
                            lineWritingMode[i] = lastWritingMode[i];
                        }
                        lineWritingMode[lineWritingMode.length-1] = writingMode;

                    }else{
                        lineAreas = new Rectangle[1];
                        lineAreas[0] = area;

                        lineWritingMode = new int[1];
                        lineWritingMode[0] = writingMode;
                    }

                    //Set area
                    this.lineAreas.put(page, lineAreas);

                    //Set writing direction
                    this.lineWritingMode.put(page, lineWritingMode);
                }

            }
        }
    }
    
    public void addToLineAreas(final int[] area, final int writingMode, final int page) {
        boolean addNew = true;

        if(lineAreas==null){ //If null, create array

            //Set area
            lineAreas = new HashMap();
            lineAreas.put(page, new int[][]{area});

            //Set writing direction
            lineWritingMode = new HashMap();
            lineWritingMode.put(page, new int[]{writingMode});

        }else{
            final int[][] lastAreas = ((int[][])lineAreas.get(page));
            final int[] lastWritingMode = ((int[])lineWritingMode.get(page));

            //Check for objects close to or intersecting each other
            if(area!=null){ //Ensure actual area is selected
                if(lastAreas!=null){
                    for(int i=0; i!= lastAreas.length; i++){
                        final int lwm = lastWritingMode[i];
                        int cx = area[0];
                        int cy = area[1];
                        int cw = area[2];
                        int ch = area[3];
                        //int cm = cy+(ch/2);

                        int lx = lastAreas[i][0];
                        int ly = lastAreas[i][1];
                        int lw = lastAreas[i][2];
                        int lh = lastAreas[i][3];
                        //int lm = ly+(lh/2);

                        final int currentBaseLine;
                        final int lastBaseLine;
                        final float heightMod = 5f;
                        final float widthMod = 1.1f;

                        switch(writingMode){
                            case PdfData.HORIZONTAL_LEFT_TO_RIGHT :

                                if(lwm== writingMode && ((ly>(cy-(ch/heightMod))) && (ly<(cy+(ch/heightMod)))) && //Ensure this is actually the same line and are about the same size
                                        (((lh<ch+(ch/heightMod) && lh>ch-(ch/heightMod))) && //Check text is the same height
                                                (((lx>(cx + cw-(ch*widthMod))) && (lx<(cx + cw+(ch*widthMod)))) || //Check for object at end of this object
                                                        ((lx + lw>(cx-(ch*widthMod))) && (lx + lw<(cx+(ch*widthMod)))) ||//Check for object at start of this object
                                                        intersects(lastAreas[i], area)))//Check to see if it intersects at all
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }
                                break;
                            case PdfData.HORIZONTAL_RIGHT_TO_LEFT :

                                lx = lastAreas[i][0];
                                ly = lastAreas[i][1];
                                lw = lastAreas[i][2];
                                lh = lastAreas[i][3];
                                cx = area[0];
                                cy = area[1];
                                cw = area[2];
                                ch = area[3];

                                if(lwm== writingMode && ((ly>(cy-5)) && (ly<(cy+5)) && lh<=(ch+(ch/5)) && lh>=(ch-(ch/5))) && //Ensure this is actually the same line and are about the same size
                                        (((lx>(cx + cw-(ch*0.6))) && (lx<(cx + cw+(ch*0.6)))) || //Check for object at end of this object
                                                ((lx + lw>(cx-(ch*0.6))) && (lx + lw<(cx+(ch*0.6)))) ||//Check for object at start of this object
                                                intersects(lastAreas[i], area))//Check to see if it intersects at all
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }
                                break;
                            case PdfData.VERTICAL_TOP_TO_BOTTOM :

                                lx = lastAreas[i][1];
                                ly = lastAreas[i][0];
                                lw = lastAreas[i][3];
                                lh = lastAreas[i][2];
                                cx = area[1];
                                cy = area[0];
                                cw = area[3];
                                ch = area[2];

                                if(lwm== writingMode && ((ly>(cy-5)) && (ly<(cy+5)) && lh<=(ch+(ch/5)) && lh>=(ch-(ch/5))) && //Ensure this is actually the same line and are about the same size
                                        (((lx>(cx + cw-(ch*0.6))) && (lx<(cx + cw+(ch*0.6)))) || //Check for object at end of this object
                                                ((lx + lw>(cx-(ch*0.6))) && (lx + lw<(cx+(ch*0.6)))) ||//Check for object at start of this object
                                                intersects(lastAreas[i], area))//Check to see if it intersects at all
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }

                                break;

                            case PdfData.VERTICAL_BOTTOM_TO_TOP :

                                //Calculate the coord value at the bottom of the text
                                currentBaseLine = cx + cw;
                                lastBaseLine = lx + lw;

                                if(
                                        lwm== writingMode //Check the current writing mode
                                                && (currentBaseLine >= (lastBaseLine-(lw/3))) && (currentBaseLine <= (lastBaseLine+(lw/3))) //Check is same line
                                                && //Only check left or right if the same line is shared
                                                (
                                                        ( //Check for text on either side
                                                                ((ly+(lh+(lw*0.6))>cy) && (ly+(lh-(lw*0.6))<cy))// Check for text to left of current area
                                                                        || ((ly+(lw*0.6)>(cy+ch)) && (ly-(lw*0.6)<(cy+ch)))// Check for text to right of current area
                                                        )
                                                                || intersects(area, lastAreas[i])
                                                )
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }

                                break;

                        }

                    }
                }else{
                    addNew = true;
                }

                //If no object near enough to merge, start a new area
                if(addNew){

                    final int[][] lineAreas;
                    final int[] lineWritingMode;

                    if(lastAreas!=null){
                        lineAreas = new int[lastAreas.length+1][4];
                        for(int i=0; i!= lastAreas.length; i++){
                            lineAreas[i] = lastAreas[i];
                        }
                        lineAreas[lineAreas.length-1] = area;

                        lineWritingMode = new int[lastWritingMode.length+1];
                        for(int i=0; i!= lastWritingMode.length; i++){
                            lineWritingMode[i] = lastWritingMode[i];
                        }
                        lineWritingMode[lineWritingMode.length-1] = writingMode;

                    }else{
                        lineAreas = new int[1][];
                        lineAreas[0] = area;

                        lineWritingMode = new int[1];
                        lineWritingMode[0] = writingMode;
                    }

                    //Set area
                    this.lineAreas.put(page, lineAreas);

                    //Set writing direction
                    this.lineWritingMode.put(page, lineWritingMode);
                }

            }
        }
    }

    /**
     * remove zone on page for text areas if present
     */
    public void removeFoundTextArea(final int[] rectArea, final int page){

        //clearHighlights();
        if(rectArea==null|| areas==null) {
            return;
        }

        final Integer p = page;
        final int[][] areas = ((int[][])this.areas.get(p));
        if(areas!=null){
            final int size=areas.length;
            for(int i=0;i<size;i++){
                if(areas[i]!=null && (contains(rectArea[0],rectArea[1], areas[i]) || (areas[i][0] ==rectArea[0] && areas[i][1] ==rectArea[1] && areas[i][2] ==rectArea[2] &&
                        areas[i][3] ==rectArea[3]))){
                    areas[i]=null;
                    i=size;
                }
            }
            this.areas.put(p, areas);
            
            //Flag that highlights have changed
            hasHighlightAreasUpdated = true;
        }
        //currentManager.addDirtyRegion(this,0,0,x_size,y_size);
    }
    
    /**
     * Deprecated on 12/06/2014, please use removeFoundTextArea(int[] rectArea, int page).
     * @deprecated
     * @param rectArea
     * @param page 
     */
    public void removeFoundTextArea(final Rectangle rectArea, final int page){

        //clearHighlights();
        if(rectArea==null|| areas==null) {
            return;
        }

        final Integer p = page;
        final Rectangle[] areas = ((Rectangle[])this.areas.get(p));
        if(areas!=null){
            final int size=areas.length;
            for(int i=0;i<size;i++){
                if(areas[i]!=null && (areas[i].contains(rectArea) || (areas[i].x ==rectArea.x && areas[i].y ==rectArea.y && areas[i].width ==rectArea.width &&
                        areas[i].height ==rectArea.height))){
                    areas[i]=null;
                    i=size;
                }
            }
            this.areas.put(p, areas);
            
            //Flag that highlights have changed
            hasHighlightAreasUpdated = true;
        }
        //currentManager.addDirtyRegion(this,0,0,x_size,y_size);
    }

    
    /**
     * remove highlight zones on page for text areas on single pages
     * null value will totally reset
     */
    @SuppressWarnings("UnusedDeclaration")
    public void removeFoundTextAreas(final int[][] rectArea, final int page){

        if(rectArea==null){
            areas=null;
        }else{
            for (final int[] aRectArea : rectArea) {
                removeFoundTextArea(aRectArea, page);
            }
            boolean allNull = true;
            final Integer p = page;
            int[][] areas = ((int[][])this.areas.get(p));
            if(areas!=null){
                for(int ii=0;ii<areas.length;ii++){
                    if(areas[ii]!=null){
                        allNull=false;
                        ii=areas.length;
                    }
                }
                if(allNull){
                    areas = null;
                    this.areas.put(p, areas);

                    //Flag that highlights have changed
                    hasHighlightAreasUpdated = true;
                }
            }
        }
    }
    
    /**
     * Deprecated on 12/06/2014, please use removeFoundTextAreas(int[][] rectArea, int page).
     * @deprecated 
     * @param rectArea
     * @param page 
     */
    @SuppressWarnings("UnusedDeclaration")
    public void removeFoundTextAreas(final Rectangle[] rectArea, final int page){

        if(rectArea==null){
            areas=null;
        }else{
            for (final Rectangle aRectArea : rectArea) {
                removeFoundTextArea(aRectArea, page);
            }
            boolean allNull = true;
            final Integer p = page;
            Rectangle[] areas = ((Rectangle[])this.areas.get(p));
            if(areas!=null){
                for(int ii=0;ii<areas.length;ii++){
                    if(areas[ii]!=null){
                        allNull=false;
                        ii=areas.length;
                    }
                }
                if(allNull){
                    areas = null;
                    this.areas.put(p, areas);

                    //Flag that highlights have changed
                    hasHighlightAreasUpdated = true;
                }
            }
        }
    }


    /**
     * Clear all highlights that are being displayed
     */
    public void clearHighlights(){
        
        areas = null;
            
        //Flag that highlights have changed
        hasHighlightAreasUpdated = true;

//		PdfHighlights.clearAllHighlights(this);
    }

    /**
     * Deprecated on 13/06/2014, please use addHighlights(int[][] highlights, boolean areaSelect, int page)
     *
     * @deprecated
     */
    public void addHighlights(final Rectangle[] highlights, final boolean areaSelect, final int page){

        if(highlights!=null){ //If null do nothing to clear use the clear method

        	//Flag that highlights have changed
            hasHighlightAreasUpdated = true;
            
            if(!areaSelect){
                //Ensure highlighting takes place
//				boolean nothingToHighlight = false;

                for(int j=0; j!=highlights.length; j++){
                    if(highlights[j]!=null){

                        //Ensure that the points are adjusted so that they are within line area if that is sent as rectangle
                        Point startPoint = new Point(highlights[j].x+1, highlights[j].y+1);
                        Point endPoint = new Point(highlights[j].x+highlights[j].width-1, highlights[j].y+highlights[j].height-1);
                        //both null flushes areas

                        if(areas==null){
                            //this.areas=new Rectangle[1];
                            //This is the first highlight, ensure it highlights something
                            areas = new HashMap();

                        }

                        final int[][] rectParams = getLineAreasAs2DArray(page);
                        final int[] writingMode = this.getLineWritingMode(page);

                        int start = -1;
                        int finish = -1;
                        boolean backward = false;
                        //Find the first selected line and the last selected line.
                        if(rectParams!=null){
                            final Rectangle[] lines = new Rectangle[rectParams.length];
                            for(int i = 0; i < lines.length; i++){
                                lines[i] = new Rectangle(rectParams[i][0],rectParams[i][1],rectParams[i][2],rectParams[i][3]);
                            }
                            for(int i=0; i!= lines.length; i++){
                                if(lines[i].contains(startPoint)) {
                                    start = i;
                                }

                                if(lines[i].contains(endPoint)) {
                                    finish = i;
                                }

                                if(start!=-1 && finish!=-1){
                                    break;
                                }

                            }

                            if(start>finish){
                                final int temp = start;
                                start = finish;
                                finish = temp;
                                backward = true;
                            }

                            if(start==finish){
                                if(startPoint.x>endPoint.x){
                                    final Point temp = startPoint;
                                    startPoint = endPoint;
                                    endPoint = temp;
                                }
                            }

                            if(start!=-1 && finish!=-1){
                                //Fill in all the lines between
                                final Integer p = page;
                                final Rectangle[] areas = new Rectangle[finish-start+1];

                                System.arraycopy(lines, start + 0, areas, 0, finish - start + 1);

                                if(areas.length>0){
                                    final int top = 0;
                                    final int bottom = areas.length-1;

                                    if(areas[top]!=null && areas[bottom]!=null){

                                        switch(writingMode[start]){
                                            case PdfData.HORIZONTAL_LEFT_TO_RIGHT :
                                                // if going backwards
                                                if(backward){
                                                    if((endPoint.x-15)<=areas[top].x){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top].width -= (endPoint.x-areas[top].x);
                                                        areas[top].x = endPoint.x;
                                                    }

                                                }else{
                                                    if((startPoint.x-15)<=areas[top].x){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top].width -= (startPoint.x-areas[top].x);
                                                        areas[top].x = startPoint.x;
                                                    }

                                                }
                                                break;
                                            case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                                                //
                                                break;
                                            case PdfData.VERTICAL_TOP_TO_BOTTOM:
                                                if(backward){
                                                    if((endPoint.y-15)<=areas[top].y){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top].height -= (endPoint.y-areas[top].y);
                                                        areas[top].y = endPoint.y;
                                                    }

                                                }else{
                                                    if((startPoint.y-15)<=areas[top].y){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top].height -= (startPoint.y-areas[top].y);
                                                        areas[top].y = startPoint.y;
                                                    }

                                                }
                                                break;
                                            case PdfData.VERTICAL_BOTTOM_TO_TOP :
                                                if(backward){
                                                    if((endPoint.y-15)<=areas[top].y){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top].height -= (endPoint.y-areas[top].y);
                                                        areas[top].y = endPoint.y;
                                                    }

                                                }else{
                                                    if((startPoint.y-15)<=areas[top].y){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top].height -= (startPoint.y-areas[top].y);
                                                        areas[top].y = startPoint.y;
                                                    }

                                                }
                                                break;
                                        }


                                        switch(writingMode[finish]){
                                            case PdfData.HORIZONTAL_LEFT_TO_RIGHT :
                                                // if going backwards
                                                if(backward){
                                                    if((startPoint.x+15)>=areas[bottom].x+areas[bottom].width){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else{
                                                        areas[bottom].width = startPoint.x - areas[bottom].x;
                                                    }

                                                }else{
                                                    if((endPoint.x+15)>=areas[bottom].x+areas[bottom].width){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else {
                                                        areas[bottom].width = endPoint.x - areas[bottom].x;
                                                    }
                                                }
                                                break;
                                            case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                                                //
                                                break;
                                            case PdfData.VERTICAL_TOP_TO_BOTTOM:
                                                // if going backwards
                                                if(backward){
                                                    if((startPoint.y+15)>=areas[bottom].y+areas[bottom].height){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else{
                                                        areas[bottom].height = startPoint.y - areas[bottom].y;
                                                    }

                                                }else{
                                                    if((endPoint.y+15)>=areas[bottom].y+areas[bottom].height){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else {
                                                        areas[bottom].height = endPoint.y - areas[bottom].y;
                                                    }
                                                }
                                                break;
                                            case PdfData.VERTICAL_BOTTOM_TO_TOP :
                                                // if going backwards
                                                if(backward){
                                                    if((startPoint.y+15)>=areas[bottom].y+areas[bottom].height){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else{
                                                        areas[bottom].height = startPoint.y - areas[bottom].y;
                                                    }

                                                }else{
                                                    if((endPoint.y+15)>=areas[bottom].y+areas[bottom].height){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else {
                                                        areas[bottom].height = endPoint.y - areas[bottom].y;
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                }
                                this.areas.put(p, areas);
                            }
//							else {
//								//This is the first highlight and nothing was selected
//								if(nothingToHighlight){
//									System.out.println("Area == null");
//									//Prevent text extraction on nothing
//									this.areas = null;
//								}
//							}
                        }
                    }
                }
            }else{
                //if inset add in difference transparently
                for(int v=0; v!=highlights.length; v++){
                    if(highlights[v]!=null){
                        if(highlights[v].width<0){
                            highlights[v].width = -highlights[v].width;
                            highlights[v].x -=highlights[v].width;
                        }

                        if(highlights[v].height<0){
                            highlights[v].height = -highlights[v].height;
                            highlights[v].y -=highlights[v].height;
                        }

                        if(areas!=null){
                            final Integer p = page;
                            Rectangle[] areas = ((Rectangle[])this.areas.get(p));
                            if(areas!=null){
                                boolean matchFound=false;

                                //see if already added
                                final int size=areas.length;
                                for(int i=0;i<size;i++){
                                    if(areas[i]!=null){
                                        //If area has been added before please ignore
                                        if(areas[i]!=null && (areas[i].x ==highlights[v].x && areas[i].y ==highlights[v].y && areas[i].width ==highlights[v].width &&
                                                areas[i].height ==highlights[v].height)){
                                            matchFound=true;
                                            i=size;
                                        }
                                    }
                                }

                                if(!matchFound){
                                    final int newSize=areas.length+1;
                                    final Rectangle[] newAreas=new Rectangle[newSize];
                                    for(int i=0;i<areas.length;i++){
                                        if(areas[i]!=null) {
                                            newAreas[i + 1] = new Rectangle(areas[i].x, areas[i].y, areas[i].width, areas[i].height);
                                        }
                                    }
                                    areas = newAreas;

                                    areas[0] = highlights[v];
                                }
                                this.areas.put(p, areas);
                            }else{
                                this.areas.put(p, highlights);
                            }
                        }else{
                            areas = new HashMap();
                            final Integer p = page;
                            final Rectangle[] areas = new Rectangle[1];
                            areas[0] = highlights[v];
                            this.areas.put(p, areas);
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     * Method to highlight text on page.
     *
     * If areaSelect = true then the Rectangle array will be highlgihted on screen unmodified.
     * areaSelect should be true if being when used with values returned from the search as these areas
     * are already corrected and modified for display.
     *
     * If areaSelect = false then all lines between the top left point and bottom right point
     * will be selected including two partial lines the top line starting from the top left point of the rectangle
     * and the bottom line ending at the bottom right point of the rectangle.
     *
     * @param highlights :: The 2DArray contains the raw x,y,w,h params of a set of rectangles that you wish to have highlighted
     * @param areaSelect :: The flag that will either select text as line between points if false or characters within an area if true.
     */
    public void addHighlights(final int[][] highlights, final boolean areaSelect, final int page){

        if(highlights!=null){ //If null do nothing to clear use the clear method

        	//Flag that highlights have changed
            hasHighlightAreasUpdated = true;
            
            if(!areaSelect){
                //Ensure highlighting takes place
//				boolean nothingToHighlight = false;

                for(int j=0; j!=highlights.length; j++){
                    if(highlights[j]!=null){

                        //Ensure that the points are adjusted so that they are within line area if that is sent as rectangle
                        int[] startPoint = {highlights[j][0]+1, highlights[j][1]+1};
                        int[] endPoint = {highlights[j][0]+highlights[j][2]-1, highlights[j][1]+highlights[j][3]-1};
                        //both null flushes areas

                        if(areas==null){
                            areas = new HashMap();
                        }

                        final int[][] lines = getLineAreasAs2DArray(page);
                        final int[] writingMode = this.getLineWritingMode(page);

                        int start = -1;
                        int finish = -1;
                        boolean backward = false;
                        //Find the first selected line and the last selected line.
                        if(lines!=null){
                            for(int i=0; i!= lines.length; i++){
                                if(contains(startPoint[0], startPoint[1], lines[i])) {
                                    start = i;
                                }

                                if(contains(endPoint[0], endPoint[1], lines[i])) {
                                    finish = i;
                                }

                                if(start!=-1 && finish!=-1){
                                    break;
                                }
                            }

                            if(start>finish){
                                final int temp = start;
                                start = finish;
                                finish = temp;
                                backward = true;
                            }

                            if(start==finish){
                                if(startPoint[0]>endPoint[0]){
                                    final int[] temp = startPoint;
                                    startPoint = endPoint;
                                    endPoint = temp;
                                }
                            }

                            if(start!=-1 && finish!=-1){
                                //Fill in all the lines between
                                final Integer p = page;
                                final int[][] areas = new int[finish-start+1][4];

                                System.arraycopy(lines, start + 0, areas, 0, finish - start + 1);

                                if(areas.length>0){
                                    final int top = 0;
                                    final int bottom = areas.length-1;

                                    if(areas[top]!=null && areas[bottom]!=null){

                                        switch(writingMode[start]){
                                            case PdfData.HORIZONTAL_LEFT_TO_RIGHT :
                                                // if going backwards
                                                if(backward){
                                                    if((endPoint[0]-15)<=areas[top][0]){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top][2] -= (endPoint[0]-areas[top][0]);
                                                        areas[top][0] = endPoint[0];
                                                    }

                                                }else{
                                                    if((startPoint[0]-15)<=areas[top][0]){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top][2] -= (startPoint[0]-areas[top][0]);
                                                        areas[top][0] = startPoint[0];
                                                    }

                                                }
                                                break;
                                            case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                                                //
                                                break;
                                            case PdfData.VERTICAL_TOP_TO_BOTTOM:
                                                if(backward){
                                                    if((endPoint[1]-15)<=areas[top][1]){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top][3] -= (endPoint[1]-areas[top][1]);
                                                        areas[top][1] = endPoint[1];
                                                    }

                                                }else{
                                                    if((startPoint[1]-15)<=areas[top][1]){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top][3] -= (startPoint[1]-areas[top][1]);
                                                        areas[top][1] = startPoint[1];
                                                    }

                                                }
                                                break;
                                            case PdfData.VERTICAL_BOTTOM_TO_TOP :
                                                if(backward){
                                                    if((endPoint[1]-15)<=areas[top][1]){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top][3] -= (endPoint[1]-areas[top][1]);
                                                        areas[top][1] = endPoint[1];
                                                    }

                                                }else{
                                                    if((startPoint[1]-15)<=areas[top][1]){
                                                        //Do nothing to areas as we want to pick up the start of a line
                                                    }else{
                                                        areas[top][3] -= (startPoint[1]-areas[top][1]);
                                                        areas[top][1] = startPoint[1];
                                                    }

                                                }
                                                break;
                                        }


                                        switch(writingMode[finish]){
                                            case PdfData.HORIZONTAL_LEFT_TO_RIGHT :
                                                // if going backwards
                                                if(backward){
                                                    if((startPoint[0]+15)>=areas[bottom][0]+areas[bottom][2]){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else{
                                                        areas[bottom][2] = startPoint[0] - areas[bottom][0];
                                                    }

                                                }else{
                                                    if((endPoint[0]+15)>=areas[bottom][0]+areas[bottom][2]){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else {
                                                        areas[bottom][2] = endPoint[0] - areas[bottom][0];
                                                    }
                                                }
                                                break;
                                            case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                                                //
                                                break;
                                            case PdfData.VERTICAL_TOP_TO_BOTTOM:
                                                // if going backwards
                                                if(backward){
                                                    if((startPoint[1]+15)>=areas[bottom][1]+areas[bottom][3]){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else{
                                                        areas[bottom][3] = startPoint[1] - areas[bottom][1];
                                                    }

                                                }else{
                                                    if((endPoint[1]+15)>=areas[bottom][1]+areas[bottom][3]){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else {
                                                        areas[bottom][3] = endPoint[1] - areas[bottom][1];
                                                    }
                                                }
                                                break;
                                            case PdfData.VERTICAL_BOTTOM_TO_TOP :
                                                // if going backwards
                                                if(backward){
                                                    if((startPoint[1]+15)>=areas[bottom][1]+areas[bottom][3]){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else{
                                                        areas[bottom][3] = startPoint[1] - areas[bottom][1];
                                                    }

                                                }else{
                                                    if((endPoint[1]+15)>=areas[bottom][1]+areas[bottom][3]){
                                                        //Do nothing to areas as we want to pick up the end of a line
                                                    }else {
                                                        areas[bottom][3] = endPoint[1] - areas[bottom][1];
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                }
                                this.areas.put(p, areas);
                            }
//							else {
//								//This is the first highlight and nothing was selected
//								if(nothingToHighlight){
//									System.out.println("Area == null");
//									//Prevent text extraction on nothing
//									this.areas = null;
//								}
//							}
                        }
                    }
                }
            }else{
                //if inset add in difference transparently
                for(int v=0; v!=highlights.length; v++){
                    if(highlights[v]!=null){
                        if(highlights[v][2]<0){
                            highlights[v][2] = -highlights[v][2];
                            highlights[v][0] -=highlights[v][2];
                        }

                        if(highlights[v][3]<0){
                            highlights[v][3] = -highlights[v][3];
                            highlights[v][1] -=highlights[v][3];
                        }

                        if(areas!=null){
                            final Integer p = page;
                            int[][] areas = ((int[][])this.areas.get(p));
                            if(areas!=null){
                                boolean matchFound=false;

                                //see if already added
                                final int size=areas.length;
                                for(int i=0;i<size;i++){
                                    if(areas[i]!=null){
                                        //If area has been added before please ignore
                                        if(areas[i]!=null && (areas[i][0] ==highlights[v][0] && areas[i][1] ==highlights[v][1] && areas[i][2] ==highlights[v][2] &&
                                                areas[i][3] ==highlights[v][3])){
                                            matchFound=true;
                                            i=size;
                                        }
                                    }
                                }

                                if(!matchFound){
                                    final int newSize=areas.length+1;
                                    final int[][] newAreas=new int[newSize][4];
                                    for(int i=0;i<areas.length;i++){
                                        if(areas[i]!=null) {
                                            newAreas[i] = new int[]{areas[i][0], areas[i][1], areas[i][2], areas[i][3]};
                                        }
                                    }
                                    areas = newAreas;

                                    areas[areas.length-1] = highlights[v];
                                }
                                this.areas.put(p, areas);
                            }else{
                                this.areas.put(p, highlights);
                            }
                        }else{
                            areas = new HashMap();
                            final Integer p = page;
                            final int[][] areas = new int[1][4];
                            areas[0] = highlights[v];
                            this.areas.put(p, areas);
                        }
                    }
                }
            }
        }
    }


    public boolean hasHighlightAreasUpdated() {
		return hasHighlightAreasUpdated;
	}

    /**
     * Get all the highlights currently stored. The returned Map 
     * using the page numbers as the keys for the values.
     * 
     * @return A Map containing all highlights currently stored.
     */
    public Map getAllHighlights(){
    	hasHighlightAreasUpdated = false;
        if(areas==null){
            return null;
        }else{
            return Collections.unmodifiableMap(areas);
        }
    }
    
    /**
     * This method is deprecated, please use getHighlightedAreasAs2DArray
     * and create your Swing/FX Rectangles where needed.
     * @param page :: The page to check for highlights
     * @return A Rectangle array containing any highlights on the page
     * @deprecated 
     */
	public Rectangle[] getHighlightedAreas(final int page){

        if(areas==null) {
            return null;
        } else{
            final Integer p = page;
            final Rectangle[] areas = ((Rectangle[])this.areas.get(p));
            if(areas!=null){
                final int count=areas.length;

                final Rectangle[] returnValue=new Rectangle[count];

                for(int ii=0;ii<count;ii++){
                    if(areas[ii]==null) {
                        returnValue[ii] = null;
                    } else {
                        returnValue[ii] = new Rectangle(areas[ii].x, areas[ii].y,
                                areas[ii].width, areas[ii].height);
                    }
                }
                
                //Reset flag as areas has been retrieved
                hasHighlightAreasUpdated = false;
                
                return returnValue;
            }else{
                return null;
            }
        }
    }

    /**
     * Creates a two-dimensional int array containing x,y,width and height
     * values for each rectangle that is stored in the areas map,
     * which allows us to create a swing/fx rectangle on these values.
     * @param page of type int.
     * @return an int[][] Containing x,y,w,h of Highlights on Page.
     */
    public int[][] getHighlightedAreasAs2DArray(final int page) {

        if(areas==null) {
            return null;
        } else{
            final Integer p = page;
            final int[][] areas = ((int[][])this.areas.get(p));
            if(areas!=null){
                final int count=areas.length;

                final int[][] returnValue=new int[count][4];

                for(int ii=0;ii<count;ii++){
                    if(areas[ii]==null) {
                        returnValue[ii] = null;
                    } else {
                        returnValue[ii] = new int[]{areas[ii][0], areas[ii][1],
                                areas[ii][2], areas[ii][3]};
                    }
                }
                
                //Reset flag as areas has been retrieved
                hasHighlightAreasUpdated = false;
                
                return returnValue;
            }else{
                return null;
            }
        }

    }
    

    public void setLineAreas(final Map la) {
        lineAreas = la;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setLineWritingMode(final Map lineOrientation) {
        lineWritingMode = lineOrientation;
    }


    /**
     * Creates a two-dimensional int array containing x,y,width and height
     * values for each rectangle that is stored in the lineAreas map,
     * which allows us to create a swing/fx rectangle on these values.
     * @param page of type int.
     * @return an int[][] Containing x,y,w,h of line areas on Page.
     */
    public int[][] getLineAreasAs2DArray(final int page){
        
        if(lineAreas==null || lineAreas.get(page) == null) {
            return null;
        } else{
            final int[][] lineAreas = ((int[][])this.lineAreas.get(page));

            if(lineAreas==null) {
                return null;
            }

            final int count=lineAreas.length;

            final int[][] returnValue=new int[count][4];

            for(int ii=0;ii<count;ii++){
                if(lineAreas[ii]==null) {
                    returnValue[ii] = null;
                } else {
                    returnValue[ii] = new int[]{lineAreas[ii][0], lineAreas[ii][1],
                            lineAreas[ii][2], lineAreas[ii][3]};
                }
            }
            
            return returnValue;
        }
        
    }
    
    /**
     * This method is deprecated, please use getLineAreasAs2DArray
     * and create your Swing/FX Rectangles where needed.
     * @param page :: The page to check for line areas
     * @return A Rectangle array containing any lines on the page
     * @deprecated 
     */
    public Rectangle[] getLineAreas(final int page) {

        if(lineAreas==null) {
            return null;
        } else{
            final Rectangle[] lineAreas = ((Rectangle[])this.lineAreas.get(page));

            if(lineAreas==null) {
                return null;
            }

            final int count=lineAreas.length;

            final Rectangle[] returnValue=new Rectangle[count];

            for(int ii=0;ii<count;ii++){
                if(lineAreas[ii]==null) {
                    returnValue[ii] = null;
                } else {
                    returnValue[ii] = new Rectangle(lineAreas[ii].x, lineAreas[ii].y,
                            lineAreas[ii].width, lineAreas[ii].height);
                }
            }

            return returnValue;
        }
    }


    public int[] getLineWritingMode(final int page) {

        if(lineWritingMode==null) {
            return null;
        } else{
            final int[] lineWritingMode = ((int[])this.lineWritingMode.get(page));

            if(lineWritingMode==null) {
                return null;
            }

            final int count=lineWritingMode.length;

            final int[] returnValue=new int[count];

            System.arraycopy(lineWritingMode, 0, returnValue, 0, count);

            return returnValue;
        }
    }

    private static int[] mergePartLines(final int[] lastArea, final int[] area){
        /**
         * Check coords from both areas and merge them to make
         * a single larger area containing contents of both
         */
        final int x1 =area[0];
        final int x2 =area[0] + area[2];
        final int y1 =area[1];
        final int y2 =area[1] + area[3];
        final int lx1 =lastArea[0];
        final int lx2 =lastArea[0] + lastArea[2];
        final int ly1 =lastArea[1];
        final int ly2 =lastArea[1] + lastArea[3];

        //Ensure the highest and lowest values are selected
        if(x1<lx1) {
            area[0] = x1;
        } else {
            area[0] = lx1;
        }

        if(y1<ly1) {
            area[1] = y1;
        } else {
            area[1] = ly1;
        }

        if(y2>ly2) {
            area[3] = y2 - area[1];
        } else {
            area[3] = ly2 - area[1];
        }

        if(x2>lx2) {
            area[2] = x2 - area[0];
        } else {
            area[2] = lx2 - area[0];
        }

        return area;
    }
    
    /**
     * This method is deprecated on 12/06/2014, please use
     * mergePartLines(int[] lastArea, int[] area)
     * @deprecated
     * @param lastArea
     * @param area
     * @return 
     */
    private static Rectangle mergePartLines(final Rectangle lastArea, final Rectangle area){
        /**
         * Check coords from both areas and merge them to make
         * a single larger area containing contents of both
         */
        final int x1 =area.x;
        final int x2 =area.x + area.width;
        final int y1 =area.y;
        final int y2 =area.y + area.height;
        final int lx1 =lastArea.x;
        final int lx2 =lastArea.x + lastArea.width;
        final int ly1 =lastArea.y;
        final int ly2 =lastArea.y + lastArea.height;

        //Ensure the highest and lowest values are selected
        if(x1<lx1) {
            area.x = x1;
        } else {
            area.x = lx1;
        }

        if(y1<ly1) {
            area.y = y1;
        } else {
            area.y = ly1;
        }

        if(y2>ly2) {
            area.height = y2 - area.y;
        } else {
            area.height = ly2 - area.y;
        }

        if(x2>lx2) {
            area.width = x2 - area.x;
        } else {
            area.width = lx2 - area.x;
        }

        return area;
    }
    
    /**
     * Checks whether two rectangles intersect
     * Takes the raw x,y,w,h data of the rectangles in array form.
     * @param paramsOne
     * @param paramsTwo
     * @return boolean
     */
    public static boolean intersects(final int[] paramsOne, final int[] paramsTwo){
        
        final int X1 = paramsOne[0];
        final int Y1 = paramsOne[1];
        final int W1 = paramsOne[2];
        final int H1 = paramsOne[3];
        final int X2 = paramsTwo[0];
        final int Y2 = paramsTwo[1];
        final int W2 = paramsTwo[2];
        final int H2 = paramsTwo[3];

        return !(X1 + W1 < X2 || X2 + W2 < X1 || Y1 + H1 < Y2 || Y2 + H2 < Y1);
    }

    /**
     * Checks whether a point at (x,y) lies within the
     * bounds of an unrotated rectangles raw x,y,w,h values.
     * @param Xp
     * @param Yp
     * @param rectParams[] x,y,w,h
     * @return 
     */
    private static boolean contains(final int x, final int y, final int[] rectParams){
        
        final int minX = rectParams[0]; //x
        final int minY = rectParams[1]; //y
        final int maxX = rectParams[0] + rectParams[2]; //x + width
        final int maxY = rectParams[1] + rectParams[3]; //y + height

        return (x >= minX && x <= maxX) && (y >= minY && y <= maxY);
    }

}
