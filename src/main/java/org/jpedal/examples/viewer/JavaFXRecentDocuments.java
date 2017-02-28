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
 * JavaFXRecentDocuments.java
 * ---------------
 */
package org.jpedal.examples.viewer;

import java.util.Stack;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.utils.*;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class Controls the Navigation of the most recent documents loaded in the
 * Viewer, it can be seen in action from the File menu.
 */
public class JavaFXRecentDocuments implements RecentDocumentsFactory {

    final int noOfRecentDocs;

    private final Stack previousFiles = new Stack();
    private final Stack nextFiles = new Stack();

    public final MenuItem[] recentDocuments;

    public JavaFXRecentDocuments(final int noOfRecentDocs) {

        this.noOfRecentDocs = noOfRecentDocs;
        recentDocuments = new MenuItem[noOfRecentDocs];

    }

    @Override
    public String getPreviousDocument() {

        String fileToOpen = null;

        if (previousFiles.size() > 1) {
            nextFiles.push(previousFiles.pop());
            fileToOpen = (String) previousFiles.pop();
        }

        return fileToOpen;
    }

    @Override
    public String getNextDocument() {

        String fileToOpen = null;

        if (!nextFiles.isEmpty()) {
            fileToOpen = (String) nextFiles.pop();
        }

        return fileToOpen;
    }

    @Override
    public void addToFileList(final String selectedFile) {
        previousFiles.push(selectedFile);

    }

    @Override
    public void enableRecentDocuments(final boolean enable) {
        if (recentDocuments == null) {
            return;
        }

        for (int i = 0; i < recentDocuments.length; i++) {
            if (recentDocuments[i] != null && !recentDocuments[i].getText().equals(i + 1 + ": ")) {
                recentDocuments[i].setVisible(enable);
                recentDocuments[i].setDisable(!enable);
            }
        }
    }

    @Override
    public void updateRecentDocuments(final String[] recentDocs) {
        if (recentDocs == null) {
            return;
        }

        for (int i = 0; i < recentDocs.length; i++) {
            if (recentDocs[i] != null) {

                final String shortenedFileName = RecentDocuments.getShortenedFileName(recentDocs[i]);

                if (recentDocuments[i] == null) {
                    recentDocuments[i] = new MenuItem();
                }

                recentDocuments[i].setText(i + 1 + ": " + shortenedFileName);
                if (recentDocuments[i].getText().equals(i + 1 + ": ")) {
                    recentDocuments[i].setVisible(false);
                } else {
                    recentDocuments[i].setVisible(true);
                }
                recentDocuments[i].setId(recentDocs[i]);
            }
        }
    }

    @Override
    public void clearRecentDocuments(final PropertiesFile properties) {
        final NodeList nl = properties.getDoc().getElementsByTagName("recentfiles");

        if (nl != null && nl.getLength() > 0) {
            final NodeList allRecentDocs = ((Element) nl.item(0)).getElementsByTagName("*");

            for (int i = 0; i < allRecentDocs.getLength(); i++) {
                final Node item = allRecentDocs.item(i);
                nl.item(0).removeChild(item);
            }
        }

        for (int i = 0; i < noOfRecentDocs; i++) {
            recentDocuments[i].setText(i + 1 + ": ");
            recentDocuments[i].setVisible(false);
        }
    }

 
    @Override
    public void createMenuItems(final String fileNameToAdd, final int position, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf, final PropertiesFile properties, final GUIThumbnailPanel thumbnails, final GUISearchWindow searchFrame) {
     
        
        final String shortenedFileName = RecentDocuments.getShortenedFileName(fileNameToAdd);
        recentDocuments[position] = new MenuItem(position + 1 + ": " + shortenedFileName);
        
        if (recentDocuments[position].getText().equals(position + 1 + ": ")) {
            recentDocuments[position].setVisible(false);
        }

        recentDocuments[position].setId(fileNameToAdd);

        recentDocuments[position].setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                //
                if (Values.isProcessing()) {
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
                } else {
                    /**
                     * warn user on forms
                     */
                    System.out.println("SaveForm in JavaFXCommands recentDocumentsOption is not yet implemented for FX");
                    //SaveForm.handleUnsaveForms(currentGUI, commonValues, decode_pdf);
                    final MenuItem item = (MenuItem) event.getSource();
                    final String fileName = item.getId();

                    if (!fileName.isEmpty()) {
                        if(!Viewer.closeCalled){
                            currentGUI.open(fileName);
                        }else {
                             throw new RuntimeException("No resource to open document, call to close() disposes viewer resources");
                        }
                        
                    }
                }
            }
        });
        
        currentGUI.getMenuItems().addToMenu(recentDocuments[position], Commands.FILEMENU);
    }
}
