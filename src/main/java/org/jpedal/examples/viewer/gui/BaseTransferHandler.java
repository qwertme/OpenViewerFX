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
 * BaseTransferHandler.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;


import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class BaseTransferHandler extends TransferHandler {
	protected final Commands currentCommands;
	protected final GUIFactory currentGUI;
	protected final Values commonValues;

	public BaseTransferHandler(final Values commonValues, final GUIFactory currentGUI, final Commands currentCommands) {
		this.commonValues = commonValues;
		this.currentGUI = currentGUI;
		this.currentCommands = currentCommands;
	}

	@Override
    public boolean canImport(final JComponent dest, final DataFlavor[] flavors) {
		return true;
	}

	protected Object getImport(final Transferable transferable) throws Exception{
		final DataFlavor[] flavors = transferable.getTransferDataFlavors();
		DataFlavor listFlavor = null;
		final int lastFlavor = flavors.length - 1;
	
		// Check the flavors and see if we find one we like.
		// If we do, save it.
		for (int f = 0; f <= lastFlavor; f++) {
			if (flavors[f].isFlavorJavaFileListType()) {
				listFlavor = flavors[f];
			}
		}
		
		// Ok, now try to display the content of the drop.
		try {
			final DataFlavor bestTextFlavor = DataFlavor.selectBestTextFlavor(flavors);
			if (bestTextFlavor != null) { // this could be a file from a web page being dragged in
				final Reader r = bestTextFlavor.getReaderForText(transferable);
				
				/** acquire the text data from the reader. */
				String textData = readTextDate(r);
	
//              System.out.println(textData);
				
	            /** need to remove all the 0 characters that will appear in the String when importing on Linux */
	            textData = removeChar(textData, (char) 0);
	
	            if(textData.contains("ftp:/")) {
                	currentGUI.showMessageDialog("Files cannot be opened via FTP");
                	return null;
                }
	            
	            /** get the URL from the text data */
	            textData = getURL(textData);
	            
	            /** replace URL spaces */
	            textData = textData.replaceAll("%20", " ");
	            
				return textData;
	
	        } else if (listFlavor != null) { // this is most likely a file being dragged in
				return transferable.getTransferData(listFlavor);		
			}
		} catch (final Exception e) {
			//
			return null;
		}
		
		return null;
	}
	
	private static String removeChar(final String s, final char c) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != c) {
                r.append(s.charAt(i));
            }
        }
        return r.toString();
    }

    /**
	 * Returns the URL from the text data acquired from the transferable object.
	 * @param textData text data acquired from the transferable.
	 * @return the URL of the file to open
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static String getURL(String textData) throws ParserConfigurationException, SAXException, IOException {
        if (!textData.startsWith("http://") && !textData.startsWith("file://")) { // its not a url so it must be a file
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document doc = db.parse(new ByteArrayInputStream(StringUtils.toBytes(textData)));

            final Element a = (Element) doc.getElementsByTagName("a").item(0);
            textData = getHrefAttribute(a);
        }            
		
		return textData;
	}

	/**
	 * Acquire text data from a reader. <br/><br/>
	 * Firefox this will be some html containing an "a" element with the "href" attribute linking to the to the PDF. <br/><br/>
	 * IE a simple one line String containing the URL will be returned
	 * @param r the reader to read from
	 * @return the text data from the reader
	 * @throws IOException
	 */
	private static String readTextDate(final Reader r) throws IOException {
		final BufferedReader br = new BufferedReader(r);
		
		StringBuilder textData = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			textData.append(line);
			line = br.readLine();
		}
		br.close();
		
		return textData.toString();
	}

	/**
	 * Returns the URL held in the href attribute from an element
	 * @param element the element containing the href attribute
	 * @return the URL held in the href attribute
	 */
	private static String getHrefAttribute(final Element element) {
		final NamedNodeMap attrs = element.getAttributes();
	
		final Node nameNode = attrs.getNamedItem("href");
		if (nameNode != null) {
			return nameNode.getNodeValue();
		}
		
		return null;
	}
}