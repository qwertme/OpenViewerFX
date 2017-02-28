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
 * RecentDocuments.java
 * ---------------
 */
package org.jpedal.examples.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.swing.JMenuItem;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.commands.SaveForm;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.utils.*;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class RecentDocuments implements RecentDocumentsFactory {
	
	//int noOfRecentDocs;
	//PropertiesFile properties;
	
    public final int noOfRecentDocs;
    
	private final Stack previousFiles = new Stack();
	private final Stack nextFiles = new Stack();

    public final JMenuItem[] recentDocuments;
   
    
	public RecentDocuments(final int noOfRecentDocs) {
		
        this.noOfRecentDocs = noOfRecentDocs;
        recentDocuments = new JMenuItem[noOfRecentDocs];
		//this.noOfRecentDocs=noOfRecentDocs;
		//this.properties=properties;
		
	}

	static String getShortenedFileName(final String fileNameToAdd) {
		final int maxChars = 30;
		
		if (fileNameToAdd.length() <= maxChars) {
            return fileNameToAdd;
        }
		
		final StringTokenizer st = new StringTokenizer(fileNameToAdd,"\\/");

        final int noOfTokens = st.countTokens();

        //allow for /filename.pdf
        if (noOfTokens==1) {
            return fileNameToAdd.substring(0, maxChars);
        }

        final String[] arrayedFile = new String[noOfTokens];
		for (int i = 0; i < noOfTokens; i++) {
            arrayedFile[i] = st.nextToken();
        }

        final String filePathBody = fileNameToAdd.substring(arrayedFile[0].length(),
				fileNameToAdd.length() - arrayedFile[noOfTokens - 1].length());
		
		final StringBuilder sb = new StringBuilder(filePathBody);
		
		int start,end;
		for (int i = noOfTokens - 2; i > 0; i--) {
			
			start = sb.lastIndexOf(arrayedFile[i]);			
			end = start + arrayedFile[i].length();
			sb.replace(start, end, "...");

			if (sb.length() <= maxChars) {
                break;
            }
		}
		
		return arrayedFile[0] + sb + arrayedFile[noOfTokens - 1];
	}

	@Override
    public String getPreviousDocument() {
		
		String fileToOpen =null;
		
		if(previousFiles.size() > 1){
			nextFiles.push(previousFiles.pop());
			fileToOpen = (String)previousFiles.pop();	
		}
		
		return fileToOpen;
	}
	
    @Override
	public String getNextDocument() {
		
		String fileToOpen =null;
		
		if(!nextFiles.isEmpty()) {
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
      if(recentDocuments == null) {
          return;
      }

      for(int i=0; i<recentDocuments.length;i++){
          if(recentDocuments[i]!=null && !recentDocuments[i].getText().equals(i+1 + ": ")){
              recentDocuments[i].setVisible(enable);
              recentDocuments[i].setEnabled(enable);
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

                final String shortenedFileName = getShortenedFileName(recentDocs[i]);

                if (recentDocuments[i] == null) {
                    recentDocuments[i] = new JMenuItem();
                }

                recentDocuments[i].setText(i + 1 + ": " + shortenedFileName);
                if (recentDocuments[i].getText().equals(i + 1 + ": ")) {
                    recentDocuments[i].setVisible(false);
                } else {
                    recentDocuments[i].setVisible(true);
                }
                recentDocuments[i].setName(recentDocs[i]);
            }
        }
    }
    
    @Override
    public void clearRecentDocuments(final PropertiesFile properties) {
        final NodeList nl = properties.getDoc().getElementsByTagName("recentfiles");
        
        if(nl != null && nl.getLength() > 0) {
            final NodeList allRecentDocs = ((Element) nl.item(0)).getElementsByTagName("*");
            
            for(int i=0;i<allRecentDocs.getLength();i++){
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
    public void createMenuItems(final String fileNameToAdd, final int position, final GUIFactory currentGUI,
            final Values commonValues, final PdfDecoderInt decode_pdf, final PropertiesFile properties,
            final GUIThumbnailPanel thumbnails, final GUISearchWindow searchFrame) {
                
        final String shortenedFileName = RecentDocuments.getShortenedFileName(fileNameToAdd);
        recentDocuments[position] = new JMenuItem(position + 1 + ": " + shortenedFileName);

        if (recentDocuments[position].getText().equals(position + 1 + ": ")) {
            recentDocuments[position].setVisible(false);
        }

        recentDocuments[position].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {

                //
                if (Values.isProcessing()) {
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
                } else {
                    /**
                     * warn user on forms
                     */
                    SaveForm.handleUnsaveForms(currentGUI, commonValues, decode_pdf);
                    final JMenuItem item = (JMenuItem) e.getSource();
                    final String fileName = item.getName();

                    if (!fileName.isEmpty()) {
                        currentGUI.open(fileName);
                    }
                }
            }
        });

        recentDocuments[position].setName(fileNameToAdd);

        currentGUI.getMenuItems().addToMenu(recentDocuments[position], Commands.FILEMENU);

    }
    
}
