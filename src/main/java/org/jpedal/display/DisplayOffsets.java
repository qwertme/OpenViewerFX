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
 * DisplayOffsets.java
 * ---------------
 */
package org.jpedal.display;

import java.awt.Point;

public class DisplayOffsets {

    /** Whether the left page drag or right page drag is drawing */
    protected boolean dragLeft;
    protected boolean dragTop;

    
    /**allow user to displace display*/
    private int userOffsetX, userOffsetY,userPrintOffsetX, userPrintOffsetY;

    //store cursor position for facing drag
    private int facingCursorX=10000, facingCursorY=10000;

    public DisplayOffsets() {}

    public void setUserOffsets(final int x, final int y, final int h, final int mode) {
         switch(mode){

            case org.jpedal.external.OffsetOptions.DISPLAY:
                userOffsetX=x;
                userOffsetY=y;
                break;

            case org.jpedal.external.OffsetOptions.PRINTING:
                userPrintOffsetX=x;
                userPrintOffsetY=-y; //make it negative so both work in same direction
                break;

            case org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK:
                facingCursorX = 0;
                facingCursorY = h;
                
                setDragCorner(mode);
                
                break;

            case org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT:
                facingCursorX=x;
                facingCursorY=y;
                
                setDragCorner(mode);
                
                break;

            case org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT:
                facingCursorX=x;
                facingCursorY=y;
                
                setDragCorner(mode);
                
                break;

            case org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_LEFT:
                facingCursorX=x;
                facingCursorY=y;
                
                setDragCorner(mode);
                
                break;

            case org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_RIGHT:
                facingCursorX=x;
                facingCursorY=y;
                
                setDragCorner(mode);
                
                break;
            
            default:
                throw new RuntimeException("No such mode - look in org.jpedal.external.OffsetOptions for valid values");
        }
    }
   
    public Point getUserOffsets(final int mode) {

        switch(mode){

            case org.jpedal.external.OffsetOptions.DISPLAY:
                return new Point(userOffsetX,userOffsetY);

            case org.jpedal.external.OffsetOptions.PRINTING:
                return new Point(userPrintOffsetX,userPrintOffsetY);

            case org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT:
                return new Point(facingCursorX,facingCursorY);

            default:
                throw new RuntimeException("No such mode - look in org.jpedal.external.OffsetOptions for valid values");
        }
    }

    public int getUserPrintOffsetX() {
        return userPrintOffsetX;
    }

    public int getUserPrintOffsetY() {
        return userPrintOffsetY;
    }

    public int getUserOffsetX() {
        return userOffsetX;
    }

    public int getUserOffsetY() {
        return userOffsetY;
    }
    
    public void setDragCorner(final int a) {
        dragLeft = a == org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT ||
                a == org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_LEFT ||
                a == org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK;

        dragTop = a == org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_LEFT ||
                a == org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_RIGHT;
    }
    
    public boolean getDragLeft() {
        return dragLeft;
    }

    public boolean getDragTop() {
        return dragTop;
    }

}
