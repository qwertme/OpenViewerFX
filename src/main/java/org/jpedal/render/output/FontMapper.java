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
 * FontMapper.java
 * ---------------
 */
package org.jpedal.render.output;

public interface FontMapper {

    /**
     * how to handle fonts
     */
    //public static final int IGNORE = 1;
    int FAIL_ON_UNMAPPED = 2;
    int DEFAULT_ON_UNMAPPED = 3;
    //public static final int EMBED_UNKNOWN_AS_IMAGE = 4;
    //public static final int EMBED_UNKNOWN_WITH_FONT = 5;
    int EMBED_ALL = 6;
    int EMBED_ALL_EXCEPT_BASE_FAMILIES = 7;

    String getFont();

    String getWeight();

    String getStyle();

    boolean isFontEmbedded();

    boolean equals(FontMapper fontMapper);

    boolean isFontSubstituted();

}
