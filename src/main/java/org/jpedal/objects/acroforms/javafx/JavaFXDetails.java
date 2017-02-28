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
 * JavaFXDetails.java
 * ---------------
 */
package org.jpedal.objects.acroforms.javafx;

import java.math.BigInteger;
import javafx.beans.property.SimpleStringProperty;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;


class JavaFXDetails {

 //   private String publicKey;
    
//    private final TableView<Model> table1 = new TableView<Model>();

    public static void setValues(final int version, final String hashAlgorithm, final String subject, final String issuer, final BigInteger serialNumber, final String notBefore,
                                 final String notAfter, final String publicKeyDescription, final String publicKey, final String x509Data, final String sha1Digest, final String md5Digest) {

//        this.publicKey = publicKey;
        
//        final ObservableList<Model> data = FXCollections.observableArrayList(
//                new Model(String.valueOf(version)),
//                new Model(hashAlgorithm),
//                new Model(subject),
//                new Model(issuer),
//                new Model(Long.toHexString(serialNumber.longValue()).toUpperCase()),
//                new Model(notBefore),
//                new Model(notAfter),
//                new Model(publicKeyDescription),
//                new Model(x509Data),
//                new Model(sha1Digest),
//                new Model(md5Digest)
//        );

        final TableColumn mainCol = new TableColumn("");
        mainCol.setCellValueFactory(
                new PropertyValueFactory<Model, String>(""));

    }
    
    public static class Model {

        private final SimpleStringProperty one;

        private Model(final String firstCol) {
            this.one = new SimpleStringProperty(firstCol);
        }

        public String getFirst() {
            return one.get();
        }

        public void setFirst(final String firstCol) {
            one.set(firstCol);
        }

    }
    
 

}
