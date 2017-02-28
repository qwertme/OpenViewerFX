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
 * PdfJavaFXPopup.java
 * ---------------
 */

package org.jpedal.objects.acroforms.javafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;

/**
 *
 * @author Simon
 */
public class PdfJavaFXPopup extends Pane {
    private final FormObject form;
    private VBox content;
    private Rectangle dragBar;
    // Offset between mouse + left/top of the dragbar
    private double offX;
    private double offY;
    
    PdfJavaFXPopup(final FormObject form, final int cropBoxWidth){
        this.form = form;
        
        createContent();
        setupListeners();
        this.getChildren().add(content);
    }
    
    private void createContent(){
        content = new VBox();
        content.getStyleClass().add("popupAnnot");

//        String subj = checkString(form.getParentPdfObj().getTextStreamValue(PdfDictionary.Subj));
        final String contents;
        // Get the date String
        String m = "";
        final String t;

        if(form.getParentPdfObj() != null){ // Get information from the parent object
    //        String subj = checkString(form.getParentPdfObj().getTextStreamValue(PdfDictionary.Subj));
            contents = checkString(form.getParentPdfObj().getTextStreamValue(PdfDictionary.Contents));
            // Get the date String
            m = formatDate(form);
            t = checkString(form.getParentPdfObj().getTextStreamValue(PdfDictionary.T));
        }else{ // Get from the popup itself
            contents = checkString(form.getTextStreamValue(PdfDictionary.Contents));
            t = checkString(form.getTextStreamValue(PdfDictionary.T));
        }
        content.setPrefWidth(200);
        content.prefHeightProperty().bind(this.prefHeightProperty());
        
        dragBar = new Rectangle();
        final Label author = new Label(t);
        final Label date = new Label(m);
        final TextArea text = new TextArea(contents);
        
        dragBar.setHeight(15);
        dragBar.widthProperty().bind(content.prefWidthProperty());
        dragBar.getStyleClass().add("dragbar");
        // Let the text box take up any remaining space
        VBox.setVgrow(text, Priority.ALWAYS);
        
        author.setWrapText(true);
        date.setWrapText(true);
        text.setWrapText(true);
        
        author.setFont(Font.font(null, FontWeight.BOLD, 16));
        
        // Get the background color:
        final float[] col = form.getFloatArray(PdfDictionary.C);
        final String color;
        if(col!=null){
            //and set border to that if valid
            if(col[0]>1 || col[1]>1 || col[2]>1){
                color="rgb("+col[0]+", "+col[1]+", "+col[2]+ ')';
            }else{
                color="rgb("+col[0]*255+", "+col[1]*255+", "+col[2]*255+ ')';
            }
        }else{
            color="rgb(255,255,0)";
        }
        
        content.setStyle("-fx-border-color:"+color+ ';');
        dragBar.setStyle("-fx-fill:"+color+ ';');
        
        content.getChildren().addAll(dragBar,author, date, text);
    }
    
    private static String formatDate(final FormObject form){
        String m = form.getParentPdfObj().getTextStreamValue(PdfDictionary.M);
        
        //Format the date
        if(m != null){
            final StringBuilder mDate = new StringBuilder(m);
            mDate.delete(0, 2);//delete D:
            mDate.insert(10, ':');
            mDate.insert(13, ':');
            mDate.delete(16, mDate.length());
            mDate.insert(16, " GMT");

            final String year = mDate.substring(0, 4);
            final String day = mDate.substring(6,8);
            mDate.delete(6,8);
            mDate.delete(0, 4);
            mDate.insert(0, day);
            mDate.insert(4, year);
            mDate.insert(2, '/');
            mDate.insert(5, '/');
            mDate.insert(10, ' ');
            m = mDate.toString();
        }else{
            m = "";
        }
        
        return m;
    }
    // Helper method to format text fields
    private static String checkString(final String value){
        if(value == null) {
            return "";
        } else {
            return value;
        }
    }
    
    private void setupListeners(){
        
        dragBar.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent event) {
                if(event.getClickCount() == 2){
                    setVisible(false);
                }
            }
        });
        
        dragBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                // Get the position of the click relative to the dragbar
                offX = event.getX();
                offY = event.getY();
                toFront();
            }
        });

        dragBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                double newX = (getTranslateX() + event.getX()) - offX;
                // Flip + and - to account for PDF coords
                double newY = (getTranslateY() - event.getY()) + offY;
                
                if(newX < 0) {
                    newX = 0;
                } else if((newX-offX) - content.getWidth() > getScene().getWidth()) {
                    newX = (getScene().getWidth() + content.getWidth());
                }
                
                if(newY < 0) {
                    newY = 0;
                }
                
                setTranslateX(newX);
                setTranslateY(newY);
                
                event.consume();
            }
        });
        visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(final ObservableValue<? extends Boolean> observable, final Boolean wasVisible, final Boolean nowVisibile) {
                if(nowVisibile){
                    toFront();
                }
            }
        });
    }
}
