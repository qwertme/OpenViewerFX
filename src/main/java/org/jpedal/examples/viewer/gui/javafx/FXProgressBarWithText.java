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
 * FXProgressBarWithText.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx;

import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Convenience class which merges a progress bar and a text label to emulate a JProgressBar
 * e.g:
 * +------------+
 * |||| 10%     |
 * +------------+
 *    ^ progress value
 * 
 * @author Simon
 */
public class FXProgressBarWithText extends StackPane{
    private final Text message;
    private final ProgressBar progress;

    public FXProgressBarWithText() {
        message = new Text();
        progress = new ProgressBar();
        
        progress.prefWidthProperty().bind(this.widthProperty());
        progress.prefHeightProperty().bind(this.heightProperty());
        
        message.setFont(Font.font(null, FontWeight.SEMI_BOLD, 14));
        
        this.getChildren().addAll(progress, message);

        StackPane.setAlignment(message, Pos.CENTER);
        StackPane.setAlignment(progress, Pos.CENTER);
        
    }
    
    /**
     * Convenience method to set the progress bar value
     * @param val new value (between 0.0 and 1.0)
     */
    public void setProgress(final double val){
        progress.setProgress(val);
    }
    
    /**
     * Convenience method to set the text
     * @param text new String
     */
    public void setText(final String text){
        message.setText(text);
    }
    
    public Text getMessage() {
        return message;
    }

    public ProgressBar getProgress() {
        return progress;
    }
}
