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
 * JavaFXDefaultActionHandler.java
 * ---------------
 */

package org.jpedal.objects.acroforms.actions;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jpedal.display.*;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.gui.JavaFxGUI;
import org.jpedal.external.Options;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.acroforms.javafx.JavaFXSummary;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author Simon
 */
public class JavaFXDefaultActionHandler extends DefaultActionHandler {

    public JavaFXDefaultActionHandler(GUIFactory currentGUI) {
        super(currentGUI);
    }
    
    /**
     * this calls the PdfDecoder to open a new page and change to the correct page and location on page,
     * is any value is null, it means leave as is.
     * @param type - the type of action
     */
    @Override
    public void changeTo(final String file, int page, final Object location, final Integer type, final boolean storeView) {

        // open file 'file'
        if (file != null) {
            try {
                
                //we are working at '2 levels'. We have the Viewer and the
                //instance of PdfDecoder. If we only open in PDFDecoder, GUI thinks it is
                //still the original file, which causes issues. So we have to change file
                //at the viewer level.
                
                //added to check the forms save flag to tell the user how to save the now changed pdf file
                
                final org.jpedal.gui.GUIFactory gui = ((org.jpedal.examples.viewer.gui.GUI) decode_pdf.getExternalHandler(Options.GUIContainer));
                if(gui!=null){
                    gui.stopThumbnails();
                    // gui.checkformSavedMessage();
                }
                
                if(file.startsWith("http://") || file.startsWith("ftp://") || file.startsWith("https:")){
                    if(gui!=null) {
                        gui.getCommand().executeCommand(Commands.OPENURL, new Object[]{file});
                    } else {
                        decode_pdf.openPdfFileFromURL(file, true);
                    }
                }else {
                    if(gui!=null) {
                        gui.getCommand().executeCommand(Commands.OPENFILE, new Object[]{file});
                    } else {
                        decode_pdf.openPdfFile(file);
                    }
                }
                
                if(page==-1) {
                    page = 1;
                }
            } catch (final Exception e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
        }
        
        // change to 'page'
        if (((page != -1) &&
            //we should use +1 as we reference pages from 1.
            (decode_pdf.getPageCount()!=1 && (decode_pdf.getDisplayView() != Display.SINGLE_PAGE || (decode_pdf.getDisplayView() == Display.SINGLE_PAGE && decode_pdf.getlastPageDecoded()!=page))))
                && (page > 0 && page < decode_pdf.getPageCount()+1)) {
                    try {
                        
                        final org.jpedal.PdfDecoderFX decode_pdf = (org.jpedal.PdfDecoderFX) this.decode_pdf;
                        
                       
                        //If we are using continuous or continuous facing we need to scroll to the correct page
                        if(decode_pdf.getDisplayView() == Display.CONTINUOUS || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){
                           // Display d = ((Display)decode_pdf.getPages());
//                            decode_pdf.scrollRectToVisible(new Rectangle(d.getXCordForPage(page), d.getYCordForPage(page), decode_pdf.getPdfPageData().getScaledCropBoxWidth(page), decode_pdf.getPdfPageData().getScaledCropBoxHeight(page)));
                        }
                        
                        this.decode_pdf.decodePage(page);
                        
                        //update page number
                        if (page != -1){
                            gui.setPage(page);
                        }
                        
                    } catch (final Exception e) {
                        //tell user and log
                        if(LogWriter.isOutput()) {
                            LogWriter.writeLog("Exception: " + e.getMessage());
                        }
                        //
                    }
                    
                    /** reset as rotation may change! */
                    decode_pdf.setPageParameters(-1, page);
                    
                }
    
        if(type!=null){
            //now available via callback
            final Object gui = this.decode_pdf.getExternalHandler(org.jpedal.external.Options.GUIContainer);
            
            /**
             * Display the page designated by page, with its contents magnified just enough to
             * fit the entire page within the window both horizontally and vertically.
             * If the required horizontal and vertical magnification factors are different,
             * use the smaller of the two, centering the page within the window in the other
             * dimension.
             */
            //set to fit - please use full paths (we do not want in imports as it will break Adobe version)
            if(gui!=null){
                if(type <0){
                    //set scaling box to 0 index, which is scale to window
                    ((org.jpedal.examples.viewer.gui.GUI)gui).setSelectedComboIndex(org.jpedal.examples.viewer.Commands.SCALING, type +3);
                }else {
                    //set scaling box to actual scaling value
                    ((org.jpedal.examples.viewer.gui.GUI)gui).setSelectedComboItem(org.jpedal.examples.viewer.Commands.SCALING,type.toString());
                }
            }
        }
        
        final org.jpedal.PdfDecoderFX decode_pdf=(org.jpedal.PdfDecoderFX)this.decode_pdf;
        
        //scroll to 'location'
        if (location != null) {
//            Display pages=(org.jpedal.display.swing.SingleDisplay) decode_pdf.getExternalHandler(org.jpedal.external.Options.Display);
//            
//            double scaling = decode_pdf.getScaling();
//            double x = ((decode_pdf.getPdfPageData().getMediaBoxWidth(page) - ((Rectangle)location).getX())*scaling) + pages.getXCordForPage(page);
//            double y = ((decode_pdf.getPdfPageData().getCropBoxHeight(page) - ((Rectangle)location).getY())*scaling) + pages.getYCordForPage(page);
//            
//            location = new Rectangle((int)x, (int)y, (int)decode_pdf.getVisibleRect().getWidth(), (int)decode_pdf.getVisibleRect().getHeight());
//            
//            decode_pdf.scrollRectToVisible((Rectangle) location);
        }
        
        final GUIFactory javaFXGUI=((JavaFxGUI)decode_pdf.getExternalHandler(Options.GUIContainer));
        if(javaFXGUI!=null){
            javaFXGUI.scaleAndRotate();
            
            if(storeView) {
                javaFXGUI.getCommand().executeCommand(Commands.ADDVIEW, new Object[]{page, location, type});
            }
        }
        
//        decode_pdf.revalidate();
//        decode_pdf.repaint();
        
    }
    
    @Override
    public Object setHoverCursor(){
        return new EventHandler<MouseEvent>(){
            @Override public void handle(final MouseEvent event) {
                if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
                    setCursor(MOUSEENTERED);
                }else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)){
                    setCursor(MOUSEEXITED);
                }
            }
        };
    }

    @Override
    protected void setCursor(final int eventType) {

        final org.jpedal.PdfDecoderFX decode_pdf = (org.jpedal.PdfDecoderFX) this.decode_pdf;

        if (decode_pdf == null) {
            //do nothing
        } else if (eventType == ActionHandler.MOUSEENTERED) {
            if (GUIDisplay.allowChangeCursor) {
                decode_pdf.setCursor(javafx.scene.Cursor.HAND);
            }
        } else if (eventType == ActionHandler.MOUSEEXITED && GUIDisplay.allowChangeCursor) {
            decode_pdf.setCursor(javafx.scene.Cursor.DEFAULT);
        }

    }
    
    @Override
    protected void popup(final Object raw, final FormObject formObj, final PdfObjectReader currentPdfFile) {
        
        if (((MouseEvent)raw).getClickCount() == 2) {
            
            //find the popup dictionary so we can get the ref (we do this to get the ref so we can lookup)
            final FormObject popupObj= (FormObject) formObj.getDictionary(PdfDictionary.Popup);
            currentPdfFile.checkResolved(popupObj);
            
            //use the ref to lookup the actual instance where the gui comp will be stored in
            final FormObject decodedObj= this.acrorend.getFormObject(popupObj.getObjectRefAsString());
            
            //if it exists toggle on/off
            final Object comp=decodedObj.getGUIComponent();
            if(comp!=null){
                
                //and we need a seperate popup for each field.
                final Node popup = (Node)comp;
                
                if (popup.isVisible()) {
                    popup.setVisible(false);
                } else {
                    popup.setVisible(true);
                }
            }
            
            //move focus so that the button does not flash
//            ((Button)((MouseEvent)raw).getSource()).setFocusable(false);
        }
    }
    
    /**
     * V action when fields value is changed [javascript], validate
     */
    @Override
    public void V(final Object ex, final FormObject formObj, final int actionID) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.V()");
        }
        
        javascript.execute(formObj, PdfDictionary.V, actionID, getKeyPressed(ex));
        
    }
    
    /**
     * when user types a keystroke
     * K action on - [javascript]
     * keystroke in textfield or combobox
     * modifys the list box selection
     * (can access the keystroke for validity and reject or modify)
     */
    @Override
    public int K(final Object ex, final FormObject formObj, final int actionID) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.K()");
        }

        return javascript.execute(formObj, PdfDictionary.K, actionID,getKeyPressed(ex));
    }
    
    
    /**
     * pick up key press or return ' '
     */
    public static char getKeyPressed(final Object raw) {
        
        try{
            
            if (raw instanceof KeyEvent){
                return ((KeyEvent) raw).getCharacter().charAt(0);
            }else{
                return ' ';
            }
            
        }catch(final Exception ee){
            System.out.println("Exception "+ee);
        }
        
        return ' ';
        
    }
    
    @Override
    protected void showSig(final PdfObject sigObject) {
        
      //org.jpedal.PdfDecoderFX decode_pdf = (org.jpedal.PdfDecoderFX) this.decode_pdf;

      final Stage frame = new Stage();
      
      final JavaFXSummary summary = new JavaFXSummary(frame,sigObject);
      
       summary.setValues(sigObject.getTextStreamValue(PdfDictionary.Name),
                sigObject.getTextStreamValue(PdfDictionary.Reason),
                sigObject.getTextStreamValue(PdfDictionary.Location));
       System.out.println("Name : " + sigObject.getTextStreamValue(PdfDictionary.Name));
       System.out.println("Reason :" + sigObject.getTextStreamValue(PdfDictionary.Reason));
       System.out.println("Location :" + sigObject.getTextStreamValue(PdfDictionary.Location));
       
       final Scene scene = new Scene(summary);
      frame.setScene(scene);
     
      frame.setScene(scene);
      frame.show();
      
      
        // Swing code
//        org.jpedal.PdfDecoder decode_pdf = (org.jpedal.PdfDecoder) this.decode_pdf;
//        
//        JDialog frame = new JDialog(getParentJFrame(decode_pdf), "Signature Properties", true);
//        
//        Summary summary = new Summary(frame, sigObject);
//        summary.setValues(sigObject.getTextStreamValue(PdfDictionary.Name),
//                sigObject.getTextStreamValue(PdfDictionary.Reason),
//                sigObject.getTextStreamValue(PdfDictionary.Location));
//        
//        frame.getContentPane().add(summary);
//        frame.setSize(550, 220);
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
        
    }
}
