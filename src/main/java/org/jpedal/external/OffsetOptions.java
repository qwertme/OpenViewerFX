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
 * OffsetOptions.java
 * ---------------
 */
package org.jpedal.external;

/**
 * flags to allow user to set offset in display or print
 */
public class OffsetOptions {

    public static final int DISPLAY =0 ;

    public static final int PRINTING =1 ;

    /**
     * Internal values - please do not use.
     */
    public static final int INTERNAL_DRAG_BLANK = 995;
    public static final int INTERNAL_DRAG_CURSOR_TOP_LEFT = 996;
    public static final int INTERNAL_DRAG_CURSOR_TOP_RIGHT = 997;
    public static final int INTERNAL_DRAG_CURSOR_BOTTOM_LEFT = 998;
    public static final int INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT = 999;
}
