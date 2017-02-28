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
 * Options.java
 * ---------------
 */
package org.jpedal.external;

/**
 * Holds set of values to indicate types of external handler
 */
public class Options {

    /**
     * allow user to process image - implements
     * {@link org.jpedal.external.ImageHandler} examples in
     * org.jpedal.examples.handlers and sample code to use commented out in
     * Viewer
     *
     */
    public static final int ImageHandler = 1;

    /**
     * not used - for future expansion
     */
    //final public static int Renderer=2;
    /**
     * allows user to over-ride form creation code with own - needs to implement
     * {@link org.jpedal.objects.acroforms.creation.FormFactory}
     *
     */
    public static final int FormFactory = 3;

    /**
     * used by Viewer - use not recommended
     */
    public static final int MultiPageUpdate = 4;
    
    /**
     * used by NetBeans plugin
     */
    public static final int PluginHandler = -4;

    /**
     * allows user to replace whole forms action Handling code - needs to
     * implement {@link org.jpedal.objects.acroforms.actions.ActionHandler} It
     * is recommended you look at Options.ExpressionEngine and
     * Options.LinkHandler for most purposes
     */
    public static final int FormsActionHandler = 5;

    /**
     * allows user to link in their own code for Javascript validation - needs
     * to implement {@link org.jpedal.objects.javascript.ExpressionEngine}
     * Default implementation at
     * {@link org.jpedal.objects.acroforms.creation.SwingFormFactory}
     */
    public static final int ExpressionEngine = 6;

    /**
     * allows user to link in their own code for Javascript validation - needs
     * to implement {@link org.jpedal.external.LinkHandler}
     */
    //final public static int LinkHandler=7; //allow user to over-ride JPedals link handling
    /**
     * used by Viewer - use not recommended
     */
    public static final int ThumbnailHandler = 8;

	public static final int JPedalActionHandler = 9;
	
//	public static final int SwingMouseHandler = 10;
    
    /** @deprecated Replaced with GUIContainer */
    @Deprecated
    public static final int SwingContainer = 11;
    /**
     * pass in GUI in Viewers
     */
    public static final int GUIContainer = 11;

    /**
     * allow user to track glyfs generated
     */
    public static final int GlyphTracker = 12;

    /**
     * allow user to track shapes
     */
    public static final int ShapeTracker = 13;

    /**
     * allow user to print own forms
     */
    public static final int CustomFormPrint = 14;

    /**
     * allow user to replace info messages with own code -also disables error
     * and other info messages
     */
    public static final int CustomMessageOutput = 15;

    /**
     * Internal use only - do not use
     */
    public static final int Display = 16;

    /**
     * Internal use only - do not use
     */
    public static final int CurrentOffset = 17;

    /**
     * Internal use only - do not use
     */
    public static final int CustomPrintHintingHandler = 18;

    /**
     * allow user to make bw
     */
    public static final int ColorHandler = 19;

    /**
     * allow user to make own output from parser
     */
    public static final int CustomOutput = 20;

    /**
     * allow user to replace info messages with own code -also disables error
     * and other info messages
     */
    public static final int RenderChangeListener = 21;
    
    /**
     * ContentHandler
     */
   // public static final int StructuredContentHandler = 22;

    //public static final int ErrorTracker = 23;

    public static final int JPedalActionHandlers = 24;

    public static final int UniqueAnnotationHandler = 25;

	public static final int CustomMouseHandler = 26;
	
//	public static final int ImageLibrary = 27;
    
    public static final int USE_XFA = 28;

    public static final int USE_XFA_IN_LEGACY_MODE=29;

    /* [AWI] Indicates that the UI is ready for Keyboard input (used for touchscreens with virtual keyboards). */
    public static final int KeyboardReadyHandler = 30;
    
    public static final int ErrorTracker = 31;
    
    public static final int SpeechEngine = 32;
    
    public static final int JavaFX_ADDITIONAL_OBJECTS = 33;
    
    public static final int FileAccess = 35;
    
    public static final int DisplayOffsets = 37;
    
    public static final int AdditionalHandler = 39;

}