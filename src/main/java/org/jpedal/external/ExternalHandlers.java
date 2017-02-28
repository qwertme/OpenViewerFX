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
 * ExternalHandlers.java
 * ---------------
 */
package org.jpedal.external;

import java.awt.image.BufferedImage;
import org.jpedal.objects.Javascript;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.javascript.ExpressionEngine;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.parser.ValueTypes;
import org.jpedal.render.DynamicVectorRenderer;

import java.util.Map;
import org.jpedal.FileAccess;
import org.jpedal.display.GUIModes;
import org.jpedal.fonts.glyph.JavaFXSupport;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.acroforms.creation.SwingFormCreator;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.parser.PDFtoImageConvertor;
import org.jpedal.parser.swing.PDFtoImageConvertorSwing;
import org.jpedal.render.SwingDisplay;
import org.jpedal.utils.LogWriter;

public class ExternalHandlers {

    private static final ClassLoader loader;
    
    private static JavaFXSupport javaFXSupport;
    
    private static final String fxClassName="org.jpedal.fonts.glyph.javafx.JavaFXSupportImpl";
    
    private static final boolean isXFAPresent;
    
    static {
        
        loader = ExternalHandlers.class.getClassLoader();
        /**
         * javafx
         */
        final String fxClassPath="org/jpedal/fonts/glyph/javafx/JavaFXSupportImpl.class";
        
        if(loader.getResource(fxClassPath)!=null){
            try {
                javaFXSupport=(JavaFXSupport) loader.loadClass(fxClassName).newInstance();
            } catch (Exception ex) {
                
                javaFXSupport=null;
                
                if(LogWriter.isOutput()){
                    LogWriter.writeLog("[PDF] Unable to instance FX "+ex);
                } 
            }
        }
        
        /**
         * xfa
         */
        final String xfaClassPath="org/jpedal/objects/acroforms/AcroRendererXFA.class";
        
        isXFAPresent=loader.getResource(xfaClassPath)!=null;
    }

    public static boolean isXFAPresent() {
       return isXFAPresent;
    }
    
    public static JavaFXSupport getFXHandler() {
        return javaFXSupport;
    }
    
    FormFactory userFormFactory;
    
    AdditonalHandler additionalHandler;
    
    /**default renderer for acroforms*/
    AcroRenderer formRenderer;
    
    //Option to append the error thrown due to lack of CID jar to page decode report
    public static boolean throwMissingCIDError;
    
    private DynamicVectorRenderer customDVR;
    
    ImageHandler customImageHandler;
    
//    ImageHelper images;
    
    private Object customSwingHandle;
    
    private Object customPluginHandle;
    
    private Object userExpressionEngine;
    
    boolean useXFA;
     
    /**
     * needs to be accessed in several locations so declared here
     */
    private Javascript javascript;
    
    //custom class for flagging painting
    RenderChangeListener customRenderChangeListener;
    
    GlyphTracker customGlyphTracker;
    
    ShapeTracker customShapeTracker;

    private boolean alwaysUseXFA;

    private Map jpedalActionHandlers;
    
    CustomPrintHintingHandler customPrintHintingHandler;
    
    ColorHandler customColorHandler;//new ExampleColorHandler();
    
    ErrorTracker customErrorTracker;//new ExampleColorHandler();
    
    private CustomFormPrint customFormPrint;
    
    private CustomMessageHandler customMessageHandler;
    
    //copy for callback
    Object swingGUI;
    
    private Enum modeSelected=GUIModes.SWING;

    public ExternalHandlers(){
    
        if(isXFAPresent){
            useXFA=true;
        }
    }
   
    public ExternalHandlers(final GUIModes mode) {
        this.modeSelected=mode;
        
        if(isXFAPresent){
            useXFA=true;
        }
    }
    
    public void addHandlers(final PdfStreamDecoder streamDecoder) {
        
        streamDecoder.setObjectValue(ValueTypes.ImageHandler, customImageHandler);
        
        streamDecoder.setObjectValue(Options.GlyphTracker,customGlyphTracker);
        streamDecoder.setObjectValue(Options.ShapeTracker, customShapeTracker);
        
        if(customErrorTracker!=null) {
            streamDecoder.setObjectValue(Options.ErrorTracker, customErrorTracker);
        }
    }
    
    
    /**
     * allows external helper classes to be added to JPedal to alter default functionality -
     * not part of the API and should be used in conjunction with IDRsolutions only
     * <br>if Options.FormsActionHandler is the type then the <b>newHandler</b> should be
     * of the form <b>org.jpedal.objects.acroforms.ActionHandler</b>
     *
     * @param newHandler
     * @param type
     */
    public void addExternalHandler(final Object newHandler, final int type) {
        
        switch (type) {

            case Options.AdditionalHandler:
                
                additionalHandler=(AdditonalHandler) newHandler;
                
                break;
                
            case Options.USE_XFA_IN_LEGACY_MODE:
                
                alwaysUseXFA=((Boolean)newHandler).booleanValue();
                
                break;

            case Options.USE_XFA:
                
                useXFA=((Boolean)newHandler).booleanValue();
                
                break;
            
            
            case Options.PluginHandler:
                customPluginHandle = newHandler;
                break;
                        
            case Options.MultiPageUpdate:
                customSwingHandle = newHandler;
                break;
                
           case Options.ErrorTracker:
                customErrorTracker = (ErrorTracker) newHandler;
                break;
                    
            case Options.ExpressionEngine:
                userExpressionEngine = newHandler;
                break;
                
                //            case Options.LinkHandler:
                //
                //                if (formRenderer != null)
                //                    formRenderer.resetHandler(newHandler, this,Options.LinkHandler);
                //
                //                break;
                
            case Options.FormFactory:
                userFormFactory=((FormFactory) newHandler);
                break;
                
            case Options.GUIContainer:
                swingGUI = newHandler;
                break;
                
            case Options.ImageHandler:
                customImageHandler = (ImageHandler) newHandler;
                break;
                
            case Options.ColorHandler:
                customColorHandler = (ColorHandler) newHandler;
                break;
                
            case Options.GlyphTracker:
                customGlyphTracker = (GlyphTracker) newHandler;
                break;
                
            case Options.ShapeTracker:
                customShapeTracker = (ShapeTracker) newHandler;
                break;
                
                //            case Options.Renderer:
                //                //cast and assign here
                //                break;
                
                //            case Options.FormFactory:
                //                formRenderer.setFormFactory((FormFactory) newHandler);
                //                break;
                //
                //            case Options.MultiPageUpdate:
                //                customSwingHandle = newHandler;
                //                break;
                //
                
            case Options.CustomFormPrint:
                customFormPrint=(CustomFormPrint)newHandler;
                break;
                
//            case Options.ImageLibrary:
//                images= (ImageHelper) newHandler;
//                break;
                //            case Options.ExpressionEngine:
                //
                //                userExpressionEngine = newHandler;
                //
                //                if (Javascript.debugActionHandler)
                //                    System.out.println("User expression engine set to " + userExpressionEngine + ' ' + newHandler);
                //
                //                setJavascript();
                //                break;
                
                //
                //            case Options.LinkHandler:
                //
                //                if (formRenderer != null)
                //                    formRenderer.resetHandler(newHandler, this,Options.LinkHandler);
                //
                //                break;
                //
                //            case Options.FormsActionHandler:
                //
                //                if (formRenderer != null)
                //                    formRenderer.resetHandler(newHandler, this,Options.FormsActionHandler);
                //
                //                break;
                //
                //
            case Options.JPedalActionHandler:
                jpedalActionHandlers = (Map) newHandler;
                break;
                
            case Options.CustomMessageOutput:
                customMessageHandler = (CustomMessageHandler) newHandler;
                break;
                
            
            case Options.RenderChangeListener:
                customRenderChangeListener = (RenderChangeListener) newHandler;
                break;
                
            case Options.CustomPrintHintingHandler:
                customPrintHintingHandler = (CustomPrintHintingHandler) newHandler;
                break;
                
            case Options.CustomOutput:
                customDVR = (DynamicVectorRenderer) newHandler;
                break;
                
             
            default:
                if(additionalHandler!=null){
                    additionalHandler.addExternalHandler(newHandler,type);
                }else{
                    throw new IllegalArgumentException("Unknown type="+type);
                }
        }
    }
    
    /**
     * allows external helper classes to be accessed if needed - also allows user to access SwingGUI if running
     * full Viewer package - not all Options available to get - please contact IDRsolutions if you are looking to
     * use
     *
     * @param type
     */
    public Object getExternalHandler(final int type) {
        
        switch (type) {
             
            case Options.FormFactory:
                return formRenderer.getFormFactory();
            
            case Options.MultiPageUpdate:
                return customSwingHandle;
                
            case Options.PluginHandler:
                return customPluginHandle;
                
            case Options.ExpressionEngine:
                return userExpressionEngine;
                
           case Options.ErrorTracker:
                return customErrorTracker;
                
            case Options.ImageHandler:
                return customImageHandler;
                
            case Options.ColorHandler:
                return customColorHandler;
                
            case Options.GlyphTracker:
                return customGlyphTracker;
                
            case Options.CustomPrintHintingHandler:
                return customPrintHintingHandler;
            
            case Options.ShapeTracker:
                return customShapeTracker;
                
            case Options.CustomFormPrint:
                return customFormPrint;
                
//            case Options.ImageLibrary:
//                return images;
                
            case Options.GUIContainer:
                return swingGUI;
                
                //                case Options.Renderer:
                //                    return null;
                //
                //                case Options.FormFactory:
                //                    return formRenderer.getFormFactory();
                //
                //                case Options.MultiPageUpdate:
                //                    return customSwingHandle;
                //
                //                case Options.ExpressionEngine:
                //                    return userExpressionEngine;
                //
            case Options.JPedalActionHandler:
                return jpedalActionHandlers;
                
            case Options.CustomMessageOutput:
                return customMessageHandler;
                
                //                case Options.Display:
                //                    return pages;
                //
                //                case Options.CurrentOffset:
                //                    return currentOffset;
                //
            case Options.CustomOutput:
                return customDVR;
                
            case Options.RenderChangeListener:
                return customRenderChangeListener;
                
            case Options.JPedalActionHandlers:
                return jpedalActionHandlers;
            
            
            default:
                
                if(type==Options.UniqueAnnotationHandler){
                    return null;
                }else if(additionalHandler!=null){
                    return additionalHandler.getExternalHandler(type);    
                }else {
                    throw new IllegalArgumentException("Unknown type " + type);
                }
                
        }
    }
    
    /**
     * Allow user to access javascript object if needed
     */
    public Javascript getJavaScript() {
        return javascript;
    }
    
    public FormFactory getUserFormFactory() {
        return userFormFactory;
    }
    
    public void dispose() {
        
        if(javascript!=null) {
            javascript.dispose();
        }
        javascript=null;
        
        //dispose the javascript object before the formRenderer object as JS accesses the renderer object
        if(formRenderer!=null) {
            formRenderer.dispose();
        }
        formRenderer=null;
        
    }
    
    /**
     * Allow user to access Forms renderer object if needed
     */
    public AcroRenderer getFormRenderer() {
        return formRenderer;
    }
    
    public void setJavaScript(final Javascript javascript) {
        this.javascript=javascript;
    }
    
    /**
     * allow user to explicitly disable XFAsupport in XFA version
     * Otherwise should not be used
     * @param useXFA 
     */
    public void useXFA(final boolean useXFA) {
        this.useXFA=useXFA;
    }
    
    private static final String xfaClassName="org.jpedal.objects.acroforms.AcroRendererXFA";
    
    public void openPdfFile(final Object userExpressionEngine) {
        
        initObjects(userExpressionEngine, new SwingFormCreator());
        
    }
    
    public static BufferedImage decode(PdfObject pdfObject, PdfObjectReader currentPdfFile, PdfObject XObject, int subtype, int width, int height, int offsetImage, float pageScaling) {
        
        if(isXFAPresent){
            
            try {
                AcroRenderer formRenderer=(AcroRenderer) loader.loadClass(xfaClassName).newInstance();
                
                return formRenderer.decode(pdfObject, currentPdfFile, XObject, subtype, width, height, offsetImage, pageScaling);
            
            } catch (Exception ex) {
                ex.printStackTrace();
                if(LogWriter.isOutput()){
                    LogWriter.writeLog("[PDF] Unable to instance XFA "+ex);
                }
                
            }
            
        }
            return null;
        

    }

    void initObjects(final Object userExpressionEngine1, SwingFormCreator formCreator) {
        
        if(isXFAPresent){
            try {
                formRenderer=(AcroRenderer) loader.loadClass(xfaClassName).newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                if(LogWriter.isOutput()){
                    LogWriter.writeLog("[PDF] Unable to instance XFA "+ex);
                }
                
                formRenderer = new AcroRenderer();
            }
          
        }else{
          formRenderer = new AcroRenderer();
        }
        
        formRenderer.useXFAIfAvailable(useXFA);
        
        formRenderer.init(formCreator);
        
        formRenderer.alwaysuseXFA(alwaysUseXFA);
        final FormFactory userFormFactory= this.userFormFactory;
        if(userFormFactory!=null) {
            formRenderer.setFormFactory(userFormFactory);
        }
        /**
         * setup Javascript object and pass into objects which use it
         */
        javascript = new Javascript((ExpressionEngine) userExpressionEngine1, formRenderer, swingGUI);
    }
    
    /**
     * show if we are Swing or JavaFX (Enums in GUIModes)
     */
    public void setMode(final Enum mode) {
        modeSelected=mode;
}
    
    /**
     * show if we are Swing or JavaFX (Enum in GUIModes)
     * @return 
     */
    public Enum getMode() {    
        return modeSelected;
    }

    public boolean isJavaFX() {
        return false;
    }

    public void setDVR(FileAccess fileAccess) {
        fileAccess.setDVR(new SwingDisplay(1, fileAccess.getObjectStore(), false));
    }


    public PDFtoImageConvertor getConverter(float multiplyer, DecoderOptions options) {
        return new PDFtoImageConvertorSwing(multiplyer, options);
    }
}
