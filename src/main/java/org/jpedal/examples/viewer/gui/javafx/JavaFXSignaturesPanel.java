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
 * JavaFXSignaturesPanel.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx;

import java.util.Iterator;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.jpedal.PdfDecoderInt;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 *
 * @author Simon
 */
public class JavaFXSignaturesPanel extends Tab {
    final TreeView<String> signatureTree;
    final Image unlock;
    final Image lock;
    static final String signedText = "The following have digitally counter-signed this document";
    static final String blankText = "The following signature fields are not signed";
    
    public JavaFXSignaturesPanel(){
        final VBox content = new VBox();
        signatureTree = new TreeView<String>();
        unlock = new Image(getClass().getResource("/org/jpedal/examples/viewer/res/unlock.png").toExternalForm());
        lock = new Image(getClass().getResource("/org/jpedal/examples/viewer/res/lock.gif").toExternalForm());
        
        signatureTree.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override 
            public TreeCell<String> call(final TreeView<String> p) {
                return new SignaturesCell();
            }
        });
        
        content.getChildren().add(signatureTree);
        
        setContent(content);
        
    }
    
    public void reinitialise(final PdfDecoderInt decode_pdf, final Iterator<FormObject> signatureObjects){
        signatureTree.setRoot(null);
        
        final TreeItem<String> root = new TreeItem<String>("Signatures");
        final TreeItem<String> signed = new TreeItem<String>(signedText);
        final TreeItem<String> blank = new TreeItem<String>(blankText);
        
        
        // using getParent() == null was causing duplicate nodes to appear, so using manual checks instead
        boolean addedSigned = false;
        boolean addedBlank = false;
        
        
        while (signatureObjects.hasNext()){
            final FormObject formObj = signatureObjects.next();
            
            final PdfObject sigObject = formObj.getDictionary(PdfDictionary.V);
            
            decode_pdf.getIO().checkResolved(sigObject);
            
            if(sigObject == null){
                if(!addedBlank){
                    addedBlank = true;
                    root.getChildren().add(blank);
                }
                final TreeItem<String> blankNode = new TreeItem<String>(formObj.getTextStreamValue(PdfDictionary.T) + " on page " + formObj.getPageNumber());
                blank.getChildren().add(blankNode);
                
            }else{
                if(!addedSigned){
                    addedSigned=true;
                    root.getChildren().add(signed);
                }
                final String name = sigObject.getTextStreamValue(PdfDictionary.Name);
                
                final TreeItem<String> owner = new TreeItem<String>("Signed by " + name);
                signed.getChildren().add(owner);
                
                final TreeItem<String> type = new TreeItem<String>("Type");
                owner.getChildren().add(type);
                
                String filter = null;
                
                final PdfArrayIterator filters = sigObject.getMixedArray(PdfDictionary.Filter);
                
                if(filters != null && filters.hasMoreTokens()) {
                    filter = filters.getNextValueAsString(true);
                }
                    
                final TreeItem<String> filterNode = new TreeItem<String>("Filter " + filter);
                type.getChildren().add(filterNode);
                
                final String subFilter = sigObject.getName(PdfDictionary.SubFilter);
                
                final TreeItem<String> subFilterNode = new TreeItem<String>("Sub Filter: " + subFilter);
                type.getChildren().add(subFilterNode);
                
                final TreeItem<String> details = new TreeItem<String>("Details");
                owner.getChildren().add(details);
                
                final String rawDate = sigObject.getTextStreamValue(PdfDictionary.M);
                
                if(rawDate != null){
                    final StringBuilder date = new StringBuilder(rawDate);

                    date.delete(0, 2);
                    date.insert(4, '/');
                    date.insert(7, '/');
                    date.insert(10, ' ');
                    date.insert(13, ':');
                    date.insert(16, ':');
                    date.insert(19, ' ');
                    
                    final TreeItem<String> time = new TreeItem<String>("Time: " + date);
                    details.getChildren().add(time);
                }else{
                    final TreeItem<String> time = new TreeItem<String>("Time: unset");
                    details.getChildren().add(time);
                }
                
                final String reason = sigObject.getTextStreamValue(PdfDictionary.Reason);
                
                final TreeItem<String> reasonNode = new TreeItem<String>("Reason: " + reason);
                details.getChildren().add(reasonNode);
                
                final String location = sigObject.getTextStreamValue(PdfDictionary.Location);

                final TreeItem<String> locationNode = new TreeItem<String>("Location: " + location);
                details.getChildren().add(locationNode);
                
                final TreeItem<String> field = new TreeItem<String>("Field: " + formObj.getTextStreamValue(PdfDictionary.T)+ " on page " + formObj.getPageNumber());
                details.getChildren().add(field);
            }
        }
        signatureTree.setRoot(root);
    }
    
    private class SignaturesCell extends TreeCell<String>{
        @Override
        public void updateItem(final String item, final boolean empty){
            super.updateItem(item, empty);
            if(empty){
                setText(null);
                setGraphic(null);
            }else{
                final TreeItem<String> parent = getTreeItem().getParent();

                final String parentText = parent != null ? parent.getValue() : "";

                if(parentText.equals(signedText)){
                    setGraphic(new ImageView(lock));
                }else if(parentText.equals(blankText)){
                    setGraphic(new ImageView(unlock));
                }
                setText(item);
            }
        }
    }
}
