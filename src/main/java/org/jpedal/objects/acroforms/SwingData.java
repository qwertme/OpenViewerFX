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
 * SwingData.java
 * ---------------
 */
package org.jpedal.objects.acroforms;


import org.jpedal.external.CustomFormPrint;
import org.jpedal.objects.acroforms.overridingImplementations.FixImageIcon;
import org.jpedal.objects.acroforms.overridingImplementations.ReadOnlyTextIcon;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.acroforms.creation.JPedalBorderFactory;

import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.display.Display;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.text.JTextComponent;

/**
 * Swing specific implementation of Widget data
 * (all non-Swing variables defined in ComponentData)
 *
 */
public class SwingData extends GUIData {
    
    //used to enable work around for bug in JDK1.6.0_10+
    //NEEDS to be public
    public static boolean JVMBugRightAlignFix;
    
    CustomFormPrint customFormPrint;
    
    JFrame dummyPanel;
    
    boolean g2SwingRenderComplete;
	
    /**
     * panel components attached to
     */
    private JPanel panel;
    
    /**scaling used for readOnly text icons drawn as images*/
    public static int readOnlyScaling=-1;
    
    public SwingData() {
    }
    
    @Override
    public void dispose(){
    	super.dispose();
    	
    	if(dummyPanel!=null){
    		if(SwingUtilities.isEventDispatchThread()){
    			dummyPanel.dispose();
    		}else{
    			try {
    				SwingUtilities.invokeAndWait(new Runnable() {
    					@Override
    					public void run() {
    						dummyPanel.dispose();
    					}
    				});
    			} catch (final InvocationTargetException e) {
    				e.printStackTrace();
    			} catch (final InterruptedException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
    /**
     * render component onto G2 for print of image creation
     * @param printcombo = tells us to print the raw combobox, and dont do aymore formatting of the combobox, should only be called from this method.
     */
    private void renderComponent(final Graphics2D g2, final FormObject formObject, final Component comp, final int rotation,boolean printcombo, final int indent, final boolean isPrinting) {
        
        if (comp != null) {
            
            boolean editable = false;
            final int page =formObject.getPageNumber();
            
            if (!printcombo && comp instanceof JComboBox) {
                
                //if we have the comobobox, adapt so we see what we want to
                //for the combobox we need to print the first item within it otherwise we doent see the contents.
                final JComboBox combo = (JComboBox) comp;
                
                if (combo.isEditable()) {
                    editable = true;
                    combo.setEditable(false);
                }
                
                if (combo.getComponentCount() > 0) {
                    final Object selected = combo.getSelectedItem();
                    if (selected != null) {
                        
                        final JTextField text = new JTextField();
                        
                        text.setText(selected.toString());
                        
                        text.setBackground(combo.getBackground());
                        text.setForeground(combo.getForeground());
                        text.setFont(combo.getFont());
                        
                        text.setBorder(combo.getBorder());
                        
                        renderComponent(g2, formObject, text, rotation, false,indent,isPrinting);
                    }
                }
                
                //set flag to say this is the combobox.
                //(we dont want to print this, as we have printed it as a textfield )
                printcombo = true;
            }
            
            if(!printcombo){
                
                final AffineTransform ax = g2.getTransform();
                
                //when true works on printing,
                //whnen false works for testrenderer, on most except eva_subjob_quer.pdf
                if(isPrinting){
                	//if we dont have the combobox print it
                    scaleComponent(formObject,1, rotation, comp, false,indent, isPrinting);
                    
                    //Rectangle rect = comp.getBounds();
                    
                    //work out new translate after rotate deduced from FixImageIcon
                    final AffineTransform at;
                    switch(360-rotation){
                        case 270:
                            at = AffineTransform.getRotateInstance(
                                    (270 * java.lang.Math.PI) / 180,0,0);
                            g2.translate(comp.getBounds().y + cropOtherY[page]-insetH,
                                    pageData.getCropBoxHeight(page)- comp.getBounds().x+insetW);
                            
                            
                            g2.transform (at);
                            g2.translate(-insetW, 0);
                            
                            break;
                        case 90:
                            at = AffineTransform.getRotateInstance(
                                    (90 * java.lang.Math.PI) / 180,0,0);
                            g2.translate(comp.getBounds().y + cropOtherY[page]-insetH,
                                    comp.getBounds().x+insetW);
                            
                            
                            g2.transform (at);
                            g2.translate(0, -insetH);
                            break;
                        case 180://not tested
                            at = AffineTransform.getRotateInstance(
                                    (180 * java.lang.Math.PI) / 180,0,0);
                            //translate to x,y of comp before applying rotate.
                            g2.translate(comp.getBounds().x - insetW, comp.getBounds().y + cropOtherY[page]);
                            
                            g2.transform (at);
                            //					g2.translate(-rect.width, -rect.height );
                            g2.translate(-insetW, -insetH );//will prob need this to work
                            
                            break;
                        default:
                            //translate to x,y of comp before applying rotate.
                            g2.translate(comp.getBounds().x - insetW, comp.getBounds().y + cropOtherY[page]);
                            break;
                    }
                }else {//used for testrenderer, images
                    
                    //if we dont have the combobox print it
                    scaleComponent(formObject,1, rotation, comp, false,indent, isPrinting);
                    
                    Rectangle rect = comp.getBounds();
                    
                    //translate to x,y of comp before applying rotate.
                    g2.translate(rect.x - insetW, rect.y + cropOtherY[page]);
                    
                    //only look at rotate on text fields as other fields should be handled.
                    if(comp instanceof JTextComponent){
                        if(pageData.getRotation(page)==90 || pageData.getRotation(page)==270){
                            comp.setBounds(rect.x, rect.y, rect.height, rect.width);
                            rect = comp.getBounds();
                        }
                        
                        //fix for file eva_subjob_quer.pdf as it has page rotations 90 0 90 0, which makes
                        //page 1 and 3 print wrong when using each pages rotation value.
                        int rotate = rotation-pageData.getRotation(0);
                        if(rotate<0) {
                            rotate = 360 + rotate;
                        }
                        
                        //work out new translate after rotate deduced from FixImageIcon
                        final AffineTransform at;
                        switch(rotate){
                            case 270:
                                at = AffineTransform.getRotateInstance(
                                        (rotate * java.lang.Math.PI) / 180,0,0);
                                g2.transform (at);
                                g2.translate(-rect.width, 0 );
                                break;
                            case 90://not tested
                                at = AffineTransform.getRotateInstance(
                                        (rotate * java.lang.Math.PI) / 180,0,0);
                                g2.transform (at);
                                g2.translate(0, -rect.height );
                                
                                break;
                            case 180://not tested
                                at = AffineTransform.getRotateInstance(
                                        (rotate * java.lang.Math.PI) / 180,0,0);
                                g2.transform (at);
                                g2.translate(-rect.width, -rect.height );
                                
                                break;
                        }
                    }
                }
                
                /**
                 *  fix for bug in Java 1.6.0_10 onwards with right aligned values
                 */
                boolean isPainted=false;
                
                //hack for a very sepcific issue so rather leave
                //Rog's code intack and take out for ME
                if (JVMBugRightAlignFix && comp instanceof JTextField) {
                    
                    final JTextField field = new JTextField();
                    final JTextField source=(JTextField)comp;
                    
                    if (source.getHorizontalAlignment() == JTextField.RIGHT) {
                        
                        field.setFont(source.getFont());
                        field.setLocation(source.getLocation());
                        field.setSize(source.getSize());
                        field.setBorder(source.getBorder());
                        field.setHorizontalAlignment(JTextField.RIGHT);
                        //field.setText(new String(createCharArray(' ', maxLengthForTextOnPage - source.getText().length())) + source.getText());
                        
                        //Rog's modified code
                        int additionalBlanks = 0;
                        int width =g2.getFontMetrics(comp.getFont()).stringWidth(new
                                String(createCharArray(' ', maxLengthForTextOnPage -
                                source.getText().length())) + source.getText());
                        final int eightPointWidth =
                                g2.getFontMetrics(comp.getFont().deriveFont(7.0F)).stringWidth(new
                                String(createCharArray(' ', maxLengthForTextOnPage -
                                source.getText().length())) + source.getText());
                        final int difference = width - eightPointWidth;
                        if (difference > 0) {
                            additionalBlanks = difference /
                                    g2.getFontMetrics(comp.getFont().deriveFont(7.0F)).stringWidth(" ");
                        }
                        final String originalTest = source.getText();
                        int bunchOfSpaces = (maxLengthForTextOnPage +
                                additionalBlanks) - source.getText().length();
                        field.setText(new String(createCharArray(' ',
                                bunchOfSpaces)) + originalTest);
                        width =
                                g2.getFontMetrics(comp.getFont()).stringWidth(field.getText());
                        
                        int insets = 0;
                        if (field.getBorder() != null) {
                            insets = (field.getBorder().getBorderInsets(field).left + field.getBorder().getBorderInsets(field).right);
                        }
                        boolean needsChange = false;
                        String newText;
                        while (bunchOfSpaces > 0 && width > field.getWidth() - insets) {
                            bunchOfSpaces = (maxLengthForTextOnPage + additionalBlanks) - source.getText().length();
                            newText = new String(createCharArray(' ', bunchOfSpaces)) + originalTest;
                            field.setText(newText);
                            additionalBlanks--;
                            width = g2.getFontMetrics(comp.getFont().deriveFont(7.0F)).stringWidth(field.getText());
                            needsChange = true;
                        }
                        
                        if (needsChange) {
                            additionalBlanks--;
                            bunchOfSpaces = (maxLengthForTextOnPage + additionalBlanks) - source.getText().length();
                            newText = new String(createCharArray(' ', bunchOfSpaces)) + originalTest;
                            field.setText(newText);
                        }
                        
                        ////
                        field.paint(g2);
                        isPainted=true;
                    }
                }
                
                if(!isPainted){
                	if(SwingUtilities.isEventDispatchThread()) {
                        comp.paint(g2);
                    } else{
                		g2SwingRenderComplete = false;
                        SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								comp.paint(g2);
								g2SwingRenderComplete = true;
							}
						});
                		
                		while(!g2SwingRenderComplete){
                			try {
								Thread.sleep(100);
							} catch (final InterruptedException e) {
								//
							}
                		}
                		
                		g2SwingRenderComplete = false;
                	}
                }
                
                //We need to set the popup back to the correct size otherwise they will be incorrectly sized
                if(isPrinting && comp instanceof JInternalFrame) {
                    scaleComponent(formObject, displayScaling, rotation, comp, false, indent, false);
                }
                
                g2.setTransform(ax);
            }
            
            if (editable) {
                ((JComboBox) comp).setEditable(true);
            }
        }
    }
    
    /**
     * render component onto G2 for print of image creation
     * @param printcombo = tells us to print the raw combobox, and dont do aymore formatting of the combobox, should only be called from this method.
     */
    private void renderComponent(final Graphics2D g2, final FormObject formObject, final boolean isPrinting) {

        final int page = formObject.getPageNumber();

        final AffineTransform ax = g2.getTransform();
        final Font backup = g2.getFont();
        final Stroke st = g2.getStroke();
        final Color old = g2.getColor();
        final Shape oldClip = g2.getClip();
        final Composite oldCom = g2.getComposite();

        //Translate to correct y coord for crop box
        g2.translate(0, pageData.getMediaBoxHeight(page) - pageData.getCropBoxHeight(page));

        if (formObject.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Text) {

            final String name = formObject.getTextStreamValue(PdfDictionary.Name);
            if (name != null && name.equals("Comment")) {
                /* Name of the icon image to use for the icon of this annotation
                 * - predefined icons are needed for names:-
                 * Comment, Key, Note, Help, NewParagraph, Paragraph, Insert
                 */
                    try {
                        final BufferedImage commentIcon = ImageIO.read(getClass().getResource("/org/jpedal/objects/acroforms/res/comment.png"));
                        g2.drawImage(commentIcon, formObject.getBoundingRectangle().x, pageData.getCropBoxHeight(page) - formObject.getBoundingRectangle().y, formObject.getBoundingRectangle().width, formObject.getBoundingRectangle().height, null);
                    } catch (final Exception e) {
                        //tell user and log
                        if (LogWriter.isOutput()) {
                            LogWriter.writeLog("Exception: " + e.getMessage());
                        }
                        //
                    }
                }
            }
        

        if (formObject.getFloatArray(PdfDictionary.C) == null) {
            formObject.setFloatArray(PdfDictionary.C, new float[]{255, 255, 0});
        }

        final float[] col = formObject.getFloatArray(PdfDictionary.C);
        Color bgColor = null;
        if (col != null) {
            if (col[0] > 1 || col[1] > 1 || col[2] > 1) {
                bgColor = new Color((int) col[0], (int) col[1], (int) col[2]);
            } else {
                bgColor = new Color(col[0], col[1], col[2]);
            }
        }
        
        if (formObject.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Popup &&
             formObject.getBoolean(PdfDictionary.Open)){
                FormRenderUtilsG2.renderPopupWindow(g2, formObject, bgColor, page, isPrinting, pageData.getCropBoxHeight(page));
            }
        
        
        //Revert back font before continuing
        g2.setFont(backup);
        
        /**
         * Type must be Border W width in points (if 0 no border, default =1) S
         * style - (default =S) S=solid, D=dashed (pattern specified by D entry
         * below), B=beveled(embossed appears to above page), I=inset(engraved
         * appeared to be below page), U=underline ( single line at bottom of
         * boundingbox) D array phase - e.g. [a b] c means:- a=on blocks,b=off
         * blocks(if not present default to a), c=start of off block preseded
         * index is on block. i.e. [4] 6 :- 4blocks on 4blocks off, block[6] if
         * off - 1=off 2=on 3=on 4=on 5=on 6=off 7=off 8=off 9=off etc...
         *
         */
        int borderWidth = FormRenderUtilsG2.renderBorder(g2, formObject, page, pageData.getCropBoxHeight(page));
        
        //Revert back stroke before continuing
        g2.setStroke(st);
        
        final String textValue = formObject.getValue();
        if (textValue != null) {
            
            FontMetrics metrics = FormRenderUtilsG2.renderFont(g2, formObject, textValue, borderWidth);
            
            //Text is drawn from the baseline so inorder to draw the highlights 
            //correctly we need to add te fonts decent
            int justification = formObject.getAlignment();
            Rectangle2D r = metrics.getStringBounds(textValue, g2);
            
            //Always center button output
            if (formObject.getFieldFlags()[FormObject.PUSHBUTTON_ID]) {
                justification = 0;
            }

            if (formObject.getObjectArray(PdfDictionary.Opt) != null && !formObject.getFieldFlags()[FormObject.COMBO_ID]) {
                FormRenderUtilsG2.renderComboForms(g2, formObject, metrics, r, page, borderWidth, justification, pageData.getCropBoxHeight(page));
            } else {
                if (!textValue.isEmpty()) {
                    g2.setClip(new Rectangle(formObject.getBoundingRectangle().x + borderWidth - 1,
                            pageData.getCropBoxHeight(page) - (formObject.getBoundingRectangle().y + formObject.getBoundingRectangle().height) + borderWidth - 1,
                            formObject.getBoundingRectangle().width - (borderWidth * 2) + 2,
                            formObject.getBoundingRectangle().height - (borderWidth * 2) + 2));

                    if (formObject.getFieldFlags()[FormObject.MULTILINE_ID]) {
                        FormRenderUtilsG2.renderMultilineTextField(g2, formObject, metrics, r, textValue, page, borderWidth, justification, pageData.getCropBoxHeight(page));
                    } else { //Single Line Field
                        FormRenderUtilsG2.renderSingleLineTextField(g2, formObject, metrics, r, textValue, page, borderWidth, justification, pageData.getCropBoxHeight(page));
                    }
                }
            }
        }

        FormRenderUtilsG2.renderQuadPoint(g2, formObject, bgColor, page, pageData.getCropBoxHeight(page));

        g2.setTransform(ax);
        g2.setFont(backup);
        g2.setStroke(st);
        g2.setColor(old);
        g2.setClip(oldClip);
        g2.setComposite(oldCom);
    }
    
    /**
     * used by fix above
     * @param c
     * @param count
     * @return
     */
    private static char[] createCharArray(final char c, final int count) {
        if(count <= 0) {
            return new char[0];
        }
        final char[] result = new char[count];
        Arrays.fill(result, 0, result.length, c);
        return result;
    }
    
    int maxLengthForTextOnPage;
  
    /**
     * draw the forms onto display for print of image. Note different routine to
     * handle forms also displayed at present
     */
    @Override
    public void renderFormsOntoG2(final Object raw, final int pageIndex, final int currentIndent,
    final int currentRotation, final Map componentsToIgnore, final FormFactory formFactory, final int pageHeight) {

        if(formsUnordered==null || rasterizeForms) {
        } else if(this.formFactory.getType()==FormFactory.HTML || this.formFactory.getType()==FormFactory.SVG) {
            renderFormsOntoG2WithHTML(pageIndex, componentsToIgnore);
        } else if(GraphicsEnvironment.isHeadless() || formFactory==null) ///mark - make 1==1 to enable
        {
            renderFormsOntoG2InHeadless(raw, pageIndex, currentRotation, componentsToIgnore, formFactory, pageHeight);
        } else {
            renderFormsOntoG2WithSwing(raw, pageIndex, currentIndent, currentRotation, componentsToIgnore, formFactory, pageHeight);
        }
        
    }
    
    /**
     * Draw forms without swing
     */
    @Override
    public void renderFormsOntoG2InHeadless(final Object raw, final int pageIndex, final int currentRotation, final Map componentsToIgnore, final FormFactory formFactory, final int pageHeight) {

        if(formsOrdered==null || formsOrdered[pageIndex]==null) {
            return;
        }
        
        this.componentsToIgnore=componentsToIgnore;
        
        FormObject formObject;
        
        //only passed in on print so also used as flag
        final boolean isPrinting = formFactory!=null;

        final Graphics2D g2 = (Graphics2D) raw;

        final AffineTransform defaultAf = g2.getTransform();
        
        // setup scaling
        final AffineTransform aff = g2.getTransform();
//        aff.scale(1, -1);
//        aff.translate(0, -pageHeight - insetH);
        aff.scale(1,1);
        g2.setTransform(aff);

        //get unsorted components and iterate over forms
        for (final Object nextVal : formsOrdered[pageIndex]) {
            
            if (nextVal !=null) {
                
                formObject=(FormObject) nextVal;

                //is this form allowed to be printed
                final boolean[] flags = formObject.getCharacteristics();
                if (((flags[1] || (isPrinting && !flags[2])))) {//1 hidden, 2 print (hense !)
                	continue;
                }

                renderComponent(g2, formObject, isPrinting);

            }
        }
        
        g2.setTransform(defaultAf);
        
    }
    
    /**
     * use Swing to draw forms
     */
    private void renderFormsOntoG2WithSwing(final Object raw, final int pageIndex, final int currentIndent, final int currentRotation, final Map componentsToIgnore, final FormFactory formFactory, final int pageHeight) {
        
        
        this.componentsToIgnore=componentsToIgnore;
        
        Component comp;
        FormObject formObject;
        
        //only passed in on print so also used as flag
        final boolean isPrinting = formFactory!=null;
        
        //fix for issue with display of items in 1.6.0_10+
        if(JVMBugRightAlignFix && isPrinting){
            
            maxLengthForTextOnPage=0;
            
            //get unsorted components and iterate over forms
            for (final Object o : formsOrdered[pageIndex]) {
                
                if (o!=null) {
                    
                    formObject = (FormObject) o;
                    
                    comp = (Component) checkGUIObjectResolved(formObject);
                    
                    if (comp instanceof JTextField) {
                        final JTextField text = (JTextField) comp;
                        final int newLength = text.getText().length();
                        
                        if (newLength > maxLengthForTextOnPage && text.getHorizontalAlignment() == JTextField.RIGHT) {
                            maxLengthForTextOnPage = newLength;
                            // System.out.println(maxLengthForTextOnPage+ " "+text.getText());
                        }
                    }
                }
            }
        }
        
        final Graphics2D g2 = (Graphics2D) raw;
        
        final AffineTransform defaultAf = g2.getTransform();
        
        // setup scaling
        final AffineTransform aff = g2.getTransform();
        aff.scale(1, -1);
        aff.translate(0, -pageHeight - insetH);
        g2.setTransform(aff);
        
        if(dummyPanel==null){
        	dummyPanel = new JFrame();
        	dummyPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	dummyPanel.pack();
        }
        //get unsorted components and iterate over forms
        for (final Object nextVal : formsOrdered[pageIndex]) {
            
            if (nextVal !=null) {
                
                formObject=(FormObject) nextVal;
                
                
                //is this form allowed to be printed
                final boolean[] flags = formObject.getCharacteristics();
                if (((flags[1] || (isPrinting && !flags[2])))) {//1 hidden, 2 print (hense !)
                    continue;
                }
                
                checkGUIObjectResolved(formObject);
                
                comp = (Component) formObject.getGUIComponent();
                
                if (comp != null && comp.isVisible()) {
                    
                    final Rectangle bounds=formObject.getBoundingRectangle();
                    final float boundHeight=bounds.height;
                    
                    final int swingHeight = comp.getPreferredSize().height+6;
                    
                    if (this.componentsToIgnore != null &&
                            (this.componentsToIgnore.containsKey(formObject.getParameterConstant(PdfDictionary.Subtype)) ||
                            this.componentsToIgnore.containsKey(formObject.getParameterConstant(PdfDictionary.Type)))) {
                    } else if (comp instanceof JList && ((JList) comp).getSelectedIndex() != -1 && boundHeight < swingHeight) {
                        
                        final JList comp2 = (JList) comp;
                        
                        dummyPanel.add(comp);
                        
                        final ListModel model = comp2.getModel();
                        final Object[] array = new Object[model.getSize()];
                        
                        final int selectedIndex = comp2.getSelectedIndex();
                        int c = 0;
                        array[c++] = model.getElementAt(selectedIndex);
                        
                        for (int i = 0; i < array.length; i++) {
                            if (i != selectedIndex) {
                                array[c++] = model.getElementAt(i);
                            }
                        }
                        
                        comp2.setListData(array);
                        comp2.setSelectedIndex(0);
                        
                        renderComponent(g2, formObject, comp2, currentRotation, false, currentIndent, isPrinting);
                        dummyPanel.remove(comp2);
                        
                    }else{
                    	//Handle popup without component
//                    	if(formObject.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Popup){
//
//                    		renderComponent(g2, formObject, currentRotation, false, isPrinting);
//                    		
//                    	}else { //if printing improve quality on AP images

                    		boolean customPrintoverRide = false;
                    		if (customFormPrint != null) {

                    			//setup scalings
                    			scaleComponent(formObject,1, rotation, comp, false, indent, isPrinting);

                    			//comp.paint(g2);
                    			customPrintoverRide = customFormPrint.print(g2, formObject,this);
                    			//g2.setTransform(ax);

                    		}

                    		if (!customPrintoverRide) {
                    			//this is where the cust1/display_error file line went, but it affects costena printing.
                    			if (comp instanceof AbstractButton) {
                    				final Object obj = ((AbstractButton) comp).getIcon();

                    				if (obj != null) {
                    					if (obj instanceof FixImageIcon) {
                    						((FixImageIcon) (obj)).setPrinting(true, 1);
                    					} else if (readOnlyScaling > 0 && obj instanceof ReadOnlyTextIcon) {
                    						((ReadOnlyTextIcon) (obj)).setPrinting(true, readOnlyScaling);
                    					}
                    				}
                    			}
                    			dummyPanel.add(comp);


                    			renderComponent(g2, formObject, comp, currentRotation, false, currentIndent, isPrinting);
                    			dummyPanel.remove(comp);

                    			if (comp instanceof AbstractButton) {
                    				final Object obj = ((AbstractButton) comp).getIcon();
                    				if (obj instanceof FixImageIcon) {
                    					((FixImageIcon) (obj)).setPrinting(false, 1);
                    				} else if (obj instanceof ReadOnlyTextIcon) {
                    					((ReadOnlyTextIcon) (obj)).setPrinting(false, 1);
                    				}
                    			}
                    		}
//                    	}
                    }
                }
            }
        }
        
        g2.setTransform(defaultAf);
        
        // put componenents back
        if (currentPage == pageIndex && panel != null) {
            // createDisplayComponentsForPage(pageIndex,this.panel,this.displayScaling,this.rotation);
            // panel.invalidate();
            // panel.repaint();
            
            //forceRedraw=true;
            resetScaledLocation(displayScaling, rotation, indent);
            
        }
        
    }
    
    
    /**
     * use Swing to draw forms
     */
    private void renderFormsOntoG2WithHTML(final int pageIndex, final Map componentsToIgnore) {
        
        this.componentsToIgnore=componentsToIgnore;
        
        FormObject formObject;
        
        //get unsorted components and iterate over forms
        for (final Object nextVal : formsOrdered[pageIndex]) {
            
            if (nextVal !=null) {
                
                formObject=(FormObject) nextVal;
                
                checkGUIObjectResolved(formObject);
                
                //comp = (Component) formObject.getGUIComponent();
                
            }
        }
    }
    
    /**
     * alter font and size to match scaling. Note we pass in compoent so we can
     * have multiple copies (needed if printing page displayed).
     */
    private void scaleComponent(final FormObject formObject, final float scale, final int rotate, final Component curComp, final boolean redraw,int indent, final boolean isPrinting) {
        
        if (curComp == null || formObject.getPageNumber()==-1) {
            return;
        }
        
        final int curPage=formObject.getPageNumber();
        /**
         * work out if visible in Layer
         */
        if (layers != null) {
            
            final String layerName = formObject.getLayerName();
            
            // do not display
            if (layerName != null && layers.isLayerName(layerName)) {
                
                final boolean isVisible = layers.isVisible(layerName);
                curComp.setVisible(isVisible);
            }
        }
        
        final int[] bounds;
        
        if(formObject.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Popup && !isPrinting) {
            bounds = cropComponent(formObject, scale, rotate, redraw, true);
        } else {
            bounds = cropComponent(formObject, scale, rotate, redraw, false);
        }
        
        /**
         * rescale the font size
         */
        final Font resetFont = curComp.getFont();
        if (resetFont != null) {
            //send in scale, rotation, and curComp as they could be from the print routines,
            //which define these parameters.
        	if(formObject.getParameterConstant(PdfDictionary.Subtype) != PdfDictionary.Popup){
    				recalcFontSize(scale, rotate, formObject, curComp);
        	}else{
        		if(isPrinting){
        	    	curComp.setFont(curComp.getFont().deriveFont(formObject.getFontSize()* (72.0f/96.0f)));
        	    	
        		}else{
        			curComp.setFont(curComp.getFont().deriveFont(formObject.getFontSize()));
        		}
        	}
        }
        
        //scale border if needed
        if((curComp instanceof JComponent && ((JComponent)curComp).getBorder()!=null) &&
            (formObject!=null) ){
                ((JComponent) curComp).setBorder((Border) generateBorderfromForm(formObject, scale));
            }
        
        // factor in offset if multiple pages displayed
        if (xReached != null) {
            bounds[0] += xReached[curPage];
            bounds[1] += yReached[curPage];
        }
        
        final int pageWidth;
        if((pageData.getRotation(curPage)+rotate)%180==90){
            pageWidth = pageData.getCropBoxHeight(curPage);
        }else {
            pageWidth = pageData.getCropBoxWidth(curPage);
        }
        
        if(displayView==Display.CONTINUOUS){
            final double newIndent;
            if(rotate==0 || rotate==180) {
                newIndent = (widestPageNR - (pageWidth)) / 2;
            } else {
                newIndent = (widestPageR - (pageWidth)) / 2;
            }
            
            indent = (int)(indent + (newIndent*scale));
        }
        
        final int totalOffsetX = userX+indent+insetW;
        final int totalOffsetY = userY+insetH;
        
        final Rectangle boundRect = new Rectangle(totalOffsetX+bounds[0],totalOffsetY+bounds[1],bounds[2],bounds[3]);
        
        curComp.setBounds(boundRect);
        
        /**
         * rescale the icons if any
         */
        if (curComp instanceof AbstractButton) {
            final AbstractButton but = ((AbstractButton) curComp);
            
            Icon curIcon = but.getIcon();
            
            boolean displaySingle = false;
            if(displayView==Display.SINGLE_PAGE || displayView==Display.NODISPLAY){
                displaySingle = true;
            }

            if (curIcon instanceof FixImageIcon) {
                ((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), rotate, displaySingle);
            } else if(curIcon instanceof ReadOnlyTextIcon) {
                ((ReadOnlyTextIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), rotate, displaySingle);
            }
            
            curIcon = but.getPressedIcon();
            if (curIcon instanceof FixImageIcon) {
                ((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), rotate, displaySingle);
            }
            
            curIcon = but.getSelectedIcon();
            if (curIcon instanceof FixImageIcon) {
                ((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), rotate, displaySingle);
            }
            
            curIcon = but.getRolloverIcon();
            if (curIcon instanceof FixImageIcon) {
                ((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), rotate, displaySingle);
            }
            
            curIcon = but.getRolloverSelectedIcon();
            if (curIcon instanceof FixImageIcon) {
                ((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), rotate, displaySingle);
            }
            
        }
    }
    
    /** we take in curComp as it could be a JTextField showing the selected value from a JComboBox
     * also the scale and rotation could be from a print routine and not the same as the global variables
     */
    private static void recalcFontSize(final float scale, final int rotate, final FormObject formObject, final Component curComp) {
        
        final int size=GUIData.getFontSize(formObject, rotate,scale);
        
        final Font resetFont = curComp.getFont();
        final Font newFont = new Font(resetFont.getFontName(),resetFont.getStyle(),size);
        
        curComp.setFont(newFont);
    }
    
    /** allows the text to be autosized at any point from anywhere by only knowing the ref. */
    @Override
    public void setAutoFontSize(final FormObject formObject){
        
        recalcFontSize(displayScaling,rotation,formObject, (Component) formObject.getGUIComponent());
        
    }
    
    /** returns Border as is swing specific class */
    public static Object generateBorderfromForm(final FormObject form, final float scaling) {
        float[] BC = form.getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BC);
        if(BC==null && form.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Screen) {
            BC = form.getFloatArray(PdfDictionary.C);
        }
        
        Border newBorder = JPedalBorderFactory.createBorderStyle(form.getDictionary(PdfDictionary.BS),
                FormObject.generateColor(BC),
                Color.white,scaling);
        
        if(form.isXFAObject()){
        	final int[] t = form.getMatteBorderDetails();
        	newBorder = BorderFactory.createMatteBorder(t[0],t[3],t[2],t[1],Color.black);
        }
        return newBorder;
    }
    
    private int[] cropComponent(final FormObject formObject, final float s, int r, final boolean redraw, final boolean positionOnly){
        
        final Rectangle rect = formObject.getBoundingRectangle();
        final int curPage=formObject.getPageNumber();
        
        
        final float[] box= {rect.x,rect.y, rect.width + rect.x,rect.height + rect.y};
        
        //NOTE if needs adding in ULC check SpecialOptions.SINGLE_PAGE
        if(displayView!=Display.SINGLE_PAGE && displayView!=Display.NODISPLAY) {
            r = (r + pageData.getRotation(curPage)) % 360;
        }
        
        final int cropX = pageData.getCropBoxX(curPage);
        final int cropY = pageData.getCropBoxY(curPage);
        final int cropW = pageData.getCropBoxWidth(curPage);
        
        final int mediaW = pageData.getMediaBoxWidth(curPage);
        final int mediaH = pageData.getMediaBoxHeight(curPage);
        
        final int cropOtherX = (mediaW - cropW - cropX);
        
        float x100=0,y100=0,w100=0,h100=0;
        final int x;
        final int y;
        final int w;
        final int h;

        {
            switch(r){
                case 0:
                    
                    x100 = box[0];
                    //if we are drawing on screen take off cropX if printing or extracting we dont need to do this.
                    if (redraw) {
                        x100 -= cropX;
                    }
                    
                    y100 = mediaH - box[3]-cropOtherY[curPage]+1;
                    w100 = (box[2] - box[0]);
                    h100 = (box[3] - box[1]);
                    
                    break;
                case 90:
                    
                    // new hopefully better routine
                    x100 = box[1]-cropY;
                    y100 = box[0]-cropX+1;
                    if(!positionOnly){
                    	w100 = (box[3] - box[1]);
                    	h100 = (box[2] - box[0]);
                    }else{
                    	w100 = (box[2] - box[0]);
                    	h100 = (box[3] - box[1]);
                    }
                    break;
                case 180:

                	// new hopefully better routine
                	w100 = box[2] - box[0];
                	h100 = box[3] - box[1];
                	if(!positionOnly){
                		y100 = box[1]-cropY+1;
                		x100 = mediaW-box[2]-cropOtherX;
                	}else{
                		w100 = (box[2] - box[0]);
                		h100 = (box[3] - box[1]);
                	}
                	break;
                case 270:
                    
                    // new hopefully improved routine
                	if(!positionOnly){
                		w100 = (box[3] - box[1]);
                		h100 = (box[2] - box[0]);
                	}else{
                		 w100 = (box[2] - box[0]);
                         h100 = (box[3] - box[1]);
                	}
                    x100 = mediaH -box[3]-cropOtherY[curPage];
                    y100 = mediaW-box[2]-cropOtherX+1;
                    
                    break;
            }/**/
        }
        
        x = (int) (x100*s);
        y = (int) (y100*s);
        if(!positionOnly){
            w = (int) (w100*s);
            h = (int) (h100*s);
        }else{
            //Don't forget to factor in the resolution of the display
            w = (int) (w100*dpi/72);
            h = (int) (h100*dpi/72);
        }
        return new int[]{x,y,w,h};
    }
    
    /**
     * used to remove all components from display
     */
    @Override
    protected void removeAllComponentsFromScreen() {
        
       // System.out.println("removeAllComponentsFromScreen");
        //01032012 be care if you ever re-enable as big performace hit on Abacus code (see slow.pdf)
        
        //		Iterator formIter = rawFormData.values().iterator();
        //		while(formIter.hasNext()){
        //			FormObject formObj = (FormObject)formIter.next();
        //			pdfDecoder.getFormRenderer().getActionHandler().PI(formObj,PdfDictionary.AA);
        //			pdfDecoder.getFormRenderer().getActionHandler().PI(formObj,PdfDictionary.A);
        //			pdfDecoder.getFormRenderer().getActionHandler().PC(formObj,PdfDictionary.AA);
        //			pdfDecoder.getFormRenderer().getActionHandler().PC(formObj,PdfDictionary.A);
        //		}
        
        if (panel != null) {
            if (SwingUtilities.isEventDispatchThread()) {
                panel.removeAll();
            } else {
                final Runnable doPaintComponent = new Runnable() {
                    @Override
                    public void run() {
                        panel.removeAll();
                    }
                };
                SwingUtilities.invokeLater(doPaintComponent);
            }
        }
        
    }
    
    /**
     * pass in object components drawn onto
     * @param rootComp
     */
    @Override
    public void setRootDisplayComponent(final Object rootComp) {
        if (SwingUtilities.isEventDispatchThread()) {
            panel = (JPanel) rootComp;
        } else {
            final Runnable doPaintComponent = new Runnable() {
                @Override
                public void run() {
                    panel = (JPanel) rootComp;
                }
            };
            SwingUtilities.invokeLater(doPaintComponent);
        }
    }
    
    @Override
    public void setGUIComp(final FormObject formObject, final Object rawField) {
        
        final Component retComponent=(Component) rawField;
        
        // append state to name so we can retrieve later if needed
        String name2 = formObject.getTextStreamValue(PdfDictionary.T);
        if (name2 != null) {// we have some empty values as well as null
            final String stateToCheck = formObject.getNormalOnState();
            if (stateToCheck != null && !stateToCheck.isEmpty()) {
                name2 = name2 + "-(" + stateToCheck + ')';
            }
            
            retComponent.setName(name2);
        }
        
        // make visible
        scaleComponent(formObject,displayScaling, rotation, retComponent, true, indent, false);
        
    }
    
    /**
     * alter location and bounds so form objects show correctly scaled
     */
    @Override
    public void resetScaledLocation(final float currentScaling, final int currentRotation, final int currentIndent) {
       
        // we get a spurious call in linux resulting in an exception
        if (formsUnordered == null || panel==null || startPage==0) {
            return;
        }
        
//        if(!SwingUtilities.isEventDispatchThread()){
//            try{
//            throw new RuntimeException("xx");
//            }catch(Exception ee){
//                ee.printStackTrace();
//           
//            }
//        }
        // needed as code called recursively otherwise
        if (forceRedraw || currentScaling != lastScaling || currentRotation != oldRotation || currentIndent != oldIndent){// || SwingUtilities.isEventDispatchThread()) {
            
            oldRotation = currentRotation;
            lastScaling = currentScaling;
            oldIndent = currentIndent;
            forceRedraw=false;
            
            FormObject formObject;
            Component rawComp;
            int count;
            
            for(int currentPage=startPage;currentPage<endPage;currentPage++){
                
                if(formsOrdered[currentPage]==null) {
                    count = 0;
                } else {
                    count = formsOrdered[currentPage].size();
                }

                // reset all locations
                for (int j=0;j<count;j++) {

                    //formObject = (FormObject) formsOrdered[currentPage].get(count-1-j); //broken version
                    formObject = formsOrdered[currentPage].get(j); //example where order matters see 19071
                    
                    formObject.setCurrentScaling(currentScaling);
                    rawComp= (Component) formObject.getGUIComponent();

                    if(rawComp!=null && formObject.getBoundingRectangle().height<rawComp.getPreferredSize().height && rawComp instanceof JList){ //rawComp!=null check for xfa

                        final JList comp = (JList) rawComp;

                        rawComp=wrapComponentInScrollPane(comp);
                        formObject.setGUIComponent(comp, FormFactory.SWING);

                        // ensure visible (do it before we add)
                        final int index = comp.getSelectedIndex();
                        if (index > -1) {
                            comp.ensureIndexIsVisible(index);
                        }

                    }

                    if (SwingUtilities.isEventDispatchThread()){

                        if(rawComp!=null){
                            panel.remove(rawComp);
                            scaleComponent(formObject,currentScaling, currentRotation, rawComp, true, indent, false);
                            panel.add(rawComp);

                        }
                    }else {
                        final Component finalComp=rawComp;
                        final FormObject fo=formObject;
                        final Runnable doPaintComponent = new Runnable() {
                            @Override
                            public void run() {

                                panel.remove(finalComp);
                                scaleComponent(fo,currentScaling, currentRotation, finalComp, true, indent, false);
                                panel.add(finalComp);

                            }
                        };
                        SwingUtilities.invokeLater(doPaintComponent);
                    }
                }
            }
        }
    }
    
   /**
     * forms on invisible pages need removing as we do not update their position and they will appear when we scroll otherwise
     * @param startPage
     * @param endPage 
     */
    @Override
    void removeHiddenForms(final int startPage, final int endPage){
        
        FormObject formObject;
        Object comp;
        
        for (int page = startPage; page < endPage; page++) {
            
            //get unsorted components and iterate over forms
            if(formsOrdered[page]!=null){
                
                for (final Object o : formsOrdered[page]) {
                    
                    if (o != null) {
                        
                        formObject = (FormObject) o;
                        
                        comp= formObject.getGUIComponent();
                        
                        if (comp != null) {
                            
                            //((JComponent)comp).setVisible(false);
                            panel.remove((JComponent)comp);
                        }
                    }
                }
            }
        }
    }    
    
    private static Component wrapComponentInScrollPane(final JList comp) {
        
        final JScrollPane scroll= new JScrollPane(comp);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setLocation(comp.getLocation());
        scroll.setPreferredSize(comp.getPreferredSize());
        scroll.setSize(comp.getSize());
        
        return scroll;
    }
    
    @Override
    protected void displayComponent(final FormObject formObject, final Object comp) {
        
        if (SwingUtilities.isEventDispatchThread()) {
            
            scaleComponent(formObject,displayScaling, rotation, (Component)comp, true, indent, false);
            
        } else {
            
            final Runnable doPaintComponent = new Runnable() {
                @Override
                public void run() {
                    scaleComponent(formObject,displayScaling, rotation, (Component) comp, true, indent, false);
                }
            };
            SwingUtilities.invokeLater(doPaintComponent);
        }
    }
    
    @Override
    public void setCustomPrintInterface(final CustomFormPrint customFormPrint) {
        this.customFormPrint=customFormPrint;
    }

}
