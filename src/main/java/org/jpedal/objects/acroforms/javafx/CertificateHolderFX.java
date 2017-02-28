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
 * CertificateHolderFX.java
 * ---------------
 */


package org.jpedal.objects.acroforms.javafx;

import java.math.BigInteger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CertificateHolderFX {
    private final Stage frame;
    private JavaFXGeneral generalTab;
    

     public void setValues(final String name, final int version, final String hashAlgorithm, final String subjectFields, final String issuerFields,
                          final BigInteger serialNumber, final String notBefore, final String notAfter, final String publicKeyDescription, final String publicKey,
                          final String x509Data, final String sha1Digest, final String md5Digest) {
        // TODO Auto-generated method stub
        generalTab = new JavaFXGeneral();
       
        generalTab.setValues(name, notBefore, notAfter);
        JavaFXDetails.setValues(version, hashAlgorithm, subjectFields, issuerFields,
            serialNumber, notBefore, notAfter, publicKeyDescription, publicKey, x509Data, sha1Digest, md5Digest);
        
        
     }
     
    /**
     * Creates new form CertificateHolder
     *
     * @param dialog
     */
    CertificateHolderFX(final Stage dialog) {
        initComponents();
        this.frame = dialog;
    }
    
    private void initComponents() {
        final TabPane tabbedPanel1 = new TabPane();
        final Button button1 = new Button();
        final HBox box = new HBox();
        
        
        button1.setText("OK");
        button1.setOnAction(new EventHandler<javafx.event.ActionEvent>(){
            @Override
            public void handle(final ActionEvent t) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                frame.close();
            }
            
        });
        
        box.getChildren().addAll(button1,tabbedPanel1);
              

    }

    
}
