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
 * JavaFXGeneral.java
 * ---------------
 */


package org.jpedal.objects.acroforms.javafx;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

class JavaFXGeneral {
    private Label issuedToBox;
    private Label label1;
    private Label label3;
    private Label label4;
    private Label validFromBox;
    private Label validToBox;
    
    /**
     * Creates new form General
     */
    JavaFXGeneral() {
        initComponents();
    }
    
    void setValues(final String name, final String notBefore, final String notAfter) {
        issuedToBox.setText(name);
        validFromBox.setText(notBefore);
        validToBox.setText(notAfter);
    }

    private void initComponents() {
      final HBox box = new HBox();
      
      label1.setText("Valid to:");
      label3.setText("Valid from:");
      label4.setText("Issued to:");
      
      box.getChildren().addAll(label1,label3,label4,validFromBox,issuedToBox);
    }

}
