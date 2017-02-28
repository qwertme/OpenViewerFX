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
 * Display.java
 * ---------------
 */
package org.jpedal.display;

import org.jpedal.render.DynamicVectorRenderer;

import java.awt.*;
import java.awt.geom.AffineTransform;

import javafx.scene.layout.Pane;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.text.TextLines;

public interface Display {

    @Deprecated
    /*
    * Please use PdfDecoder.setBorderPresent(boolean) instead
    * True : Show border around page.
    * Flase : Remove border around page.
    */ int BORDER_SHOW=1;

    @SuppressWarnings("UnusedDeclaration")
    @Deprecated
    /*
    * Please use PdfDecoder.setBorderPresent(boolean) instead
    * True : Show border around page.
    * Flase : Remove border around page.
    */ int BORDER_HIDE=0;

    /**when no display is set*/
    int NODISPLAY=0;
    
    /**show pages one at a time*/
    int SINGLE_PAGE=1;

    /**show all pages*/
    int CONTINUOUS=2;

    /**show all pages two at a time*/
    int CONTINUOUS_FACING=3;
    
    /**show pages two at a time*/
    int FACING=4;
    
    /**PageFlowing mode*/
    int PAGEFLOW=5;

    int DISPLAY_LEFT_ALIGNED=1;

    int DISPLAY_CENTERED=2;
    
    double getIndent();

    /**
     * Please use public int[] getCursorBoxOnScreenAsArray() instead.
     * @deprecated on 04/07/2014
     */
    Rectangle getCursorBoxOnScreen();
    
    int[] getCursorBoxOnScreenAsArray();

    void setCursorBoxOnScreen(Rectangle cursorBoxOnScreen, boolean isSamePage);

    void forceRedraw();

    void setPageRotation(int displayRotation);

    void resetViewableArea();

    void paintPage(Pane box, AcroRenderer formRenderer, TextLines textLines);
    
    void paintPage(Graphics2D g2, AcroRenderer formRenderer, TextLines textLines);
    
    void updateCursorBoxOnScreen(int[] newOutlineRectangle, int outlineColor, int pageNumber,int x_size,int y_size);

    /**
     * Deprecated on 04/07/2014, please use 
     * updateCursorBoxOnScreen(int[] newOutlineRectangle, int outlineColor, int pageNumber,int x_size,int y_size) instead.
     * @deprecated
     */
    void updateCursorBoxOnScreen(Rectangle newOutlineRectangle, Color outlineColor, int pageNumber, int x_size, int y_size);

    void drawCursor(Graphics g, float scaling);

    /**
     * Deprecated on 07/07/2014
     * Please use setViewableArea(int[] viewport) instead.
     * 
     * @deprecated
     */
    AffineTransform setViewableArea(Rectangle viewport) throws PdfException;
    
    AffineTransform setViewableArea(int[] viewport) throws PdfException;
    
    void drawFacing(Rectangle visibleRect);
    
    enum BoolValue {
        TURNOVER_ON,
        SEPARATE_COVER
    }

    /**flag used in development of layout modes*/
    boolean debugLayout=false;
    
    int[] getPageSize(int displayView);

    void decodeOtherPages(int pageNumber, int pageCount);

    void stopGeneratingPage();

    void refreshDisplay();

    Rectangle getDisplayedRectangle();
    
    void disableScreen();

    void flushPageCaches();

    void init(float scaling, int displayRotation, int pageNumber, DynamicVectorRenderer currentDisplay, boolean isInit);

    void drawBorder();
    
    void setup(boolean useAcceleration,PageOffsets currentOffset);

    int getYCordForPage(int page);

    int getYCordForPage(int page, float scaling);

    int getXCordForPage(int currentPage);

	void setThumbnailPanel(GUIThumbnailPanel thumbnails);
	
	void setScaling(float scaling);

	@SuppressWarnings("UnusedDeclaration")
    void setPageOffsets(int page);

    void dispose();

    void setAcceleration(boolean enable);
    
    void setAccelerationAlwaysRedraw(boolean enable);

    void setObjectValue(int type, Object newValue);

    int[] getHighlightedImage();

    void setHighlightedImage(int[] i);

    float getOldScaling();

    boolean getBoolean(BoolValue option);

    void setBoolean(BoolValue option, boolean value);

}
