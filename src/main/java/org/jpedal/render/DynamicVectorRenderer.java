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
 * DynamicVectorRenderer.java
 * ---------------
 */
package org.jpedal.render;

import org.jpedal.color.PdfPaint;
import org.jpedal.exception.PdfException;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import org.jpedal.external.FontHandler;



@SuppressWarnings("UnusedDeclaration")
public interface DynamicVectorRenderer  {

    void eliminateHiddenText(Shape currentShape, GraphicsState gs, int segmentCount, boolean ignoreScaling);

    void saveImageData(boolean b);

    boolean saveImageData();

    void setIsRenderingToImage(boolean b);

    FontHandler getFontHandler();

    
    enum Mode{PDF,XFA,SMASK}

    int TEXT=1;
	int SHAPE=2;
	int IMAGE=3;
	int TRUETYPE=4;
	int TYPE1C=5;
	int TYPE3=6;
	int CLIP=7;
	int COLOR=8;
	int AF=9;
	int TEXTCOLOR=10;
	int FILLCOLOR=11;
	int STROKECOLOR=12;
	int STROKE=14;
	int TR=15;
	int STRING=16;
	int STROKEOPACITY=17;
	int FILLOPACITY=18;

	int STROKEDSHAPE=19;
	int FILLEDSHAPE=20;

	int FONTSIZE=21;
	int LINEWIDTH=22;

	int CUSTOM=23;

	int fontBB=24;

    int DELETED_IMAGE = 27;
    int REUSED_IMAGE = 29;

    int BLENDMODE = 31;

    int SAVE_EMBEDDED_FONT = 10;
    int TEXT_STRUCTURE_OPEN = 40;
    int TEXT_STRUCTURE_CLOSE = 42;

    int IsSVGMode = 44;
    int IsTextSelectable = 45;
    int IsRealText = 46;
    int MARKER=200;

	/**flag to enable debugging of painting*/
	boolean debugPaint=false;

	/**
	 * various types of DVR which we have
	 */
	int DISPLAY_SCREEN = 1;//
	int DISPLAY_IMAGE = 2;//
	int CREATE_PATTERN =3;
	int CREATE_HTML =4;
	int CREATE_SVG =5;
	int CREATE_EPOS =7;
    int CREATE_SMASK =8;
	
	
	/**
	 * Keys for use with set value
	 */
	int ALT_BACKGROUND_COLOR=1;
	int ALT_FOREGROUND_COLOR=2;
	int FOREGROUND_INCLUDE_LINEART=3; //Alt foreground color changes lineart as well
	int COLOR_REPLACEMENT_THRESHOLD=4;
	
    /**
     * used to pass in Graphics2D for all versions
     * @param g2
     */
	void setG2(Graphics2D g2);

    /**
	 * set optimised painting as true or false and also reset if true
	 * @param optimsePainting
	 */
	void setOptimsePainting(boolean optimsePainting);

	/* remove all page objects and flush queue */
	void flush();

	/* remove all page objects and flush queue */
	void dispose();

    /**
     * only needed for screen display
     * @param x
     * @param y
     */
	void setInset(int x, int y);

	/*renders all the objects onto the g2 surface for screen display*/
	void paint(Rectangle[] highlights, AffineTransform viewScaling, Rectangle userAnnot);

	/**
	 * allow user to set component for waring message in renderer to appear -
	 * if unset no message will appear
	 * @param frame
	 */
	void setMessageFrame(Container frame);

	void paintBackground(Shape dirtyRegion);

	/* saves text object with attributes for rendering*/
	void drawText(float[][] Trm, String text, GraphicsState currentGraphicsState, float x, float y, Font javaFont);

	/**workout combined area of shapes in an area*/
	//public abstract Rectangle getCombinedAreas(Rectangle targetRectangle, boolean justText);

	/*setup renderer*/
	void init(int x, int y, Color backgroundColor);
	
	/* save image in array to draw */
	int drawImage(int pageNumber, BufferedImage image, GraphicsState currentGraphicsState, boolean alreadyCached, String name, int previousUse);

	/**
	 * return which part of page drawn onto
	 * @return
	 */
	Rectangle getOccupiedArea();

	/*save shape in array to draw cmd is Cmd.F or Cmd.S */
	void drawShape(Shape currentShape, GraphicsState currentGraphicsState, int cmd);

        /*save shape in array to draw cmd is Cmd.F or Cmd.S */
		void drawShape(Object currentShape, GraphicsState currentGraphicsState, int cmd);

	/**reset on colorspace change to ensure cached data up to data*/
	void resetOnColorspaceChange();

	/*save shape colour*/
	void drawFillColor(PdfPaint currentCol);

	/*save opacity settings*/
	void setGraphicsState(int fillType, float value, int BM);

	/*Method to add Shape, Text or image to main display on page over PDF - will be flushed on redraw*/
	void drawAdditionalObjectsOverPage(int[] type, Color[] colors, Object[] obj) throws PdfException;

	void flushAdditionalObjOnPage();

	/*save shape colour*/
	void drawStrokeColor(Paint currentCol);

	/*save custom shape*/
	void drawCustom(Object value);

	/*save shape stroke*/
	void drawTR(int value);

	/*save shape stroke*/
	void drawStroke(Stroke current);

	/*save clip in array to draw*/
	void drawClip(GraphicsState currentGraphicsState, Shape defaultClip, boolean alwaysApply);

	/**
	 * store glyph info
	 */
	void drawEmbeddedText(float[][] Trm, int fontSize,
						  PdfGlyph embeddedGlyph, Object javaGlyph, int type,
						  GraphicsState gs, double[] at, String glyf, PdfFont currentFontData, float glyfWidth);

	/**
	 * store fontBounds info
	 */
	void drawFontBounds(Rectangle newfontBB);

	/**
	 * store af info
	 */
	void drawAffine(double[] afValues);

	/**
	 * store af info
	 */
	void drawFontSize(int fontSize);

	/**
	 * store line width info
	 */
	void setLineWidth(int lineWidth);

	/**
	 * Screen drawing using hi res images and not down-sampled images but may be slower
	 * and use more memory<br> Default setting is <b>false</b> and does nothing in
	 * OS version
	 */
	void setHiResImageForDisplayMode(boolean useHiResImageForDisplay);

	void setScalingValues(double cropX, double cropH, float scaling);

	/**stop screen bein cleared on repaint - used by Canoo code
	 *
	 * NOT PART OF API and subject to change (DO NOT USE)
	 **/
	void stopClearOnNextRepaint(boolean flag);

	void setCustomImageHandler(org.jpedal.external.ImageHandler customImageHandler);

	void setCustomColorHandler(org.jpedal.external.ColorHandler colorController);

	/**
	 * operations to do once page done
	 */
	void flagDecodingFinished();

	//used internally - please do not use
	ObjectStore getObjectStore();

	void flagImageDeleted(int i);

	void setOCR(boolean isOCR);

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * turn object into byte[] so we can move across
	 * this way should be much faster than the stadard Java serialise.
	 *
	 * NOT PART OF API and subject to change (DO NOT USE)
	 *
	 * @throws java.io.IOException
	 */
	byte[] serializeToByteArray(Set fontsAlreadyOnClient) throws IOException;

	/**
	 * for font if we are generatign glyph on first render
	 */
	void checkFontSaved(Object glyph, String name, PdfFont currentFontData);

	boolean hasObjectsBehind(float[][] CTM);

	/**
     * This method is deprecated, please use getAreaAsArray and
     * create fx/swing rectangles where needed.
     * @deprecated 
     * @param i
     * @return 
     */
	Rectangle getArea(int i);
    
    int[] getAreaAsArray(int i);

	/**
	 * return number of image in display queue
	 * or -1 if none
	 * @return
	 */
	int isInsideImage(int x, int y);

	void saveImage(int id, String des, String type);

	/**
	 * return number of image in display queue
	 * or -1 if none
	 * @return
	 */
	int getObjectUnderneath(int x, int y);


    void setneedsVerticalInvert(boolean b);

    void setneedsHorizontalInvert(boolean b);

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * just for printing
     */
	void stopG2HintSetting(boolean isSet);

    void setPrintPage(int currentPrintPage);

    void writeCustom(int section, Object str);

    /**allow us to identify different types of renderer (ie HTML, Screen, Image)*/
	int getType();

	/** allow tracking of specific commands **/
	void flagCommand(int commandID, int tokenNumber);

    //generic method used by HTML to pass in values
    void setValue(int key, int i);

    //generic method used by HTML for getting values
	int getValue(int key);

    BufferedImage getSingleImagePattern();

    /**used by JavaFX and HTML5 conversion to override scaling*/
    boolean isScalingControlledByUser();

    boolean avoidDownSamplingImage();

    /**allow user to read*/
	boolean getBooleanValue(int key);

    float getScaling();

    /**
     * only used in HTML5 and SVG conversion
     * @param baseFontName
     * @param s
     * @param potentialWidth
     */
	void saveAdvanceWidth(String baseFontName, String s, int potentialWidth);
        
    void setMode(Mode pdfType);
    Mode getMode();

    Object getObjectValue(int id);

    boolean isHTMLorSVG();


}

