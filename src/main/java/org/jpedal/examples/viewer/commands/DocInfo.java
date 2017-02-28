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
 * DocInfo.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.*;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.PdfFileInformation;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.ReturnValues;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.utils.Messages;

/**
 * Shows a Message Dialogue displaying the Documents Information
 */
public class DocInfo {

    private static final Font textFont = new Font("Serif", Font.PLAIN, 12);

    private static final Font headFont = new Font("SansSerif", Font.BOLD, 14);

    private static boolean sortFontsByDir = true;

    /**
     * user dir in which program can write
     */
    private static final String user_dir = System.getProperty("user.dir");

    public static void execute(final Object[] args, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf) {
        if (args == null) {
            if (!commonValues.isPDF()) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.ImageSearch"));
            }else if(commonValues.getSelectedFile() == null){
                    currentGUI.showMessageDialog(Messages.getMessage("PdfVieweremptyFile.message"), Messages.getMessage("PdfViewerTooltip.pageSize"), JOptionPane.PLAIN_MESSAGE);
            }else{
                getDocumentProperties(commonValues.getSelectedFile(), commonValues.getFileSize(), commonValues.getPageCount(), commonValues.getCurrentPage(), decode_pdf, currentGUI);
            }
        }
    }

    private static void getDocumentProperties(final String selectedFile, final long size, final int pageCount, final int currentPage, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI) {

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.WHITE);

        //

        int ptr = selectedFile.lastIndexOf('\\');
        if (ptr == -1) {
            ptr = selectedFile.lastIndexOf('/');
        }

        final String file = selectedFile.substring(ptr + 1, selectedFile.length());

        //

        final String path = selectedFile.substring(0, ptr + 1);

        int ii = 0;
        tabbedPane.add(getPropertiesBox(file, path, user_dir, size, pageCount, currentPage, decode_pdf));
        tabbedPane.setTitleAt(ii++, Messages.getMessage("PdfViewerTab.Properties"));

        tabbedPane.add(getFontInfoBox(decode_pdf.getInfo(PdfDictionary.Font)));
        tabbedPane.setTitleAt(ii++, Messages.getMessage("PdfViewerTab.Fonts"));

        if (org.jpedal.parser.image.ImageCommands.trackImages) {
            tabbedPane.add(getImageInfoBox(decode_pdf));
            tabbedPane.setTitleAt(ii++, Messages.getMessage("PdfViewerTab.Images"));
        }

        tabbedPane.add(getFontsFoundInfoBox());
        tabbedPane.setTitleAt(ii++, "Available");

        tabbedPane.add(getFontsAliasesInfoBox());
        tabbedPane.setTitleAt(ii++, "Aliases");

        int nextTab = ii;

        /**
         * add form details if applicable
         */
        if (getFormList(decode_pdf) != null) {
            tabbedPane.add(getFormList(decode_pdf));
            tabbedPane.setTitleAt(nextTab, "Forms");
            nextTab++;
        }

        /**
         * optional tab for new XML style info
         */
        final PdfFileInformation currentFileInformation = decode_pdf.getFileInformationData();
        final String xmlText = currentFileInformation.getFileXMLMetaData();
        if (!xmlText.isEmpty()) {
            tabbedPane.add(getXMLInfoBox(xmlText));
            tabbedPane.setTitleAt(nextTab, "XML");
        }

        currentGUI.showMessageDialog(tabbedPane, Messages.getMessage("PdfViewerTab.DocumentProperties"), JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * show document properties
     */
    private static JScrollPane getPropertiesBox(final String file, final String path, final String user_dir, final long size, final int pageCount, final int currentPage, final PdfDecoderInt decode_pdf) {

        final PdfFileInformation currentFileInformation = decode_pdf.getFileInformationData();

        /**
         * get the Pdf file information object to extract info from
         */
        if (currentFileInformation != null) {

            final JPanel details = new JPanel();
            details.setOpaque(true);
            details.setBackground(Color.white);
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

            final JScrollPane scrollPane = new JScrollPane();
            scrollPane.setPreferredSize(new Dimension(400, 300));
            scrollPane.getViewport().add(details);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            //general details
            final JLabel header1 = new JLabel(Messages.getMessage("PdfViewerGeneral"));
            header1.setFont(headFont);
            header1.setOpaque(false);
            details.add(header1);

            final JLabel g1 = new JLabel(Messages.getMessage("PdfViewerFileName") + file);
            g1.setFont(textFont);
            g1.setOpaque(false);
            details.add(g1);

            final JLabel g2 = new JLabel(Messages.getMessage("PdfViewerFilePath") + path);
            g2.setFont(textFont);
            g2.setOpaque(false);
            details.add(g2);

            final JLabel g3 = new JLabel(Messages.getMessage("PdfViewerCurrentWorkingDir") + ' ' + user_dir);
            g3.setFont(textFont);
            g3.setOpaque(false);
            details.add(g3);

            final JLabel g4 = new JLabel(Messages.getMessage("PdfViewerFileSize") + size + " K");
            g4.setFont(textFont);
            g4.setOpaque(false);
            details.add(g4);

            final JLabel g5 = new JLabel(Messages.getMessage("PdfViewerPageCount") + pageCount);
            g5.setOpaque(false);
            g5.setFont(textFont);
            details.add(g5);

            StringBuilder g6Text = new StringBuilder ("PDF " );
            g6Text.append(decode_pdf.getPDFVersion());

            //add in if Linearized
            if (decode_pdf.getJPedalObject(PdfDictionary.Linearized) != null) {
                g6Text.append(" (").append(Messages.getMessage("PdfViewerLinearized.text")).append(") ");
            }

            final JLabel g6 = new JLabel(g6Text.toString());
            g6.setOpaque(false);
            g6.setFont(textFont);
            details.add(g6);

            details.add(Box.createVerticalStrut(10));

            //general details
            final JLabel header2 = new JLabel(Messages.getMessage("PdfViewerProperties"));
            header2.setFont(headFont);
            header2.setOpaque(false);
            details.add(header2);

            //get the document properties
            final String[] values = currentFileInformation.getFieldValues();
            final String[] fields = PdfFileInformation.getFieldNames();

            //add to list and display
            final int count = fields.length;

            final JLabel[] displayValues = new JLabel[count];

            for (int i = 0; i < count; i++) {
                if (!values[i].isEmpty()) {

                    displayValues[i] = new JLabel(fields[i] + " = " + values[i]);
                    displayValues[i].setFont(textFont);
                    displayValues[i].setOpaque(false);
                    details.add(displayValues[i]);
                }
            }

            details.add(Box.createVerticalStrut(10));

            /**
             * get the Pdf file information object to extract info from
             */
            final PdfPageData currentPageSize = decode_pdf.getPdfPageData();

            if (currentPageSize != null) {

                //general details
                final JLabel header3 = new JLabel(Messages.getMessage("PdfViewerCoords.text"));
                header3.setFont(headFont);
                details.add(header3);

                final JLabel g7 = new JLabel(Messages.getMessage("PdfViewermediaBox.text") + currentPageSize.getMediaValue(currentPage));
                g7.setFont(textFont);
                details.add(g7);

                final JLabel g8 = new JLabel(Messages.getMessage("PdfViewercropBox.text") + currentPageSize.getCropValue(currentPage));
                g8.setFont(textFont);
                details.add(g8);

                final JLabel g9 = new JLabel(Messages.getMessage("PdfViewerLabel.Rotation") + currentPageSize.getRotation(currentPage));
                g9.setFont(textFont);
                details.add(g9);

            }

            details.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            return scrollPane;

        } else {
            return new JScrollPane();
        }
    }

    /**
     * show fonts displayed
     */
    private static JScrollPane getFontInfoBox(final String xmlTxt) {

        final JPanel details = new JPanel();

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.getViewport().add(details);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        details.setOpaque(true);
        details.setBackground(Color.white);
        details.setEnabled(false);
        details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));

        /**
         * list of fonts
         */
        StringBuilder xmlText = new StringBuilder("Font Substitution mode: ");

        switch (FontMappings.getFontSubstitutionMode()) {
            case (1):
                xmlText.append("using file name");
                break;
            case (2):
                xmlText.append("using PostScript name");
                break;
            case (3):
                xmlText.append("using family name");
                break;
            case (4):
                xmlText.append("using the full font name");
                break;
            default:
                xmlText.append("Unknown FontSubstitutionMode");
                break;
        }

        xmlText.append('\n');

        if (!xmlTxt.isEmpty()) {

            final JTextArea xml = new JTextArea();
            final JLabel mode = new JLabel();

            mode.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            mode.setText(xmlText.toString());
            mode.setForeground(Color.BLUE);

            xml.setLineWrap(false);
            xml.setForeground(Color.BLACK);
            xml.setText('\n' + xmlTxt);

            details.add(mode);
            details.add(xml);

            xml.setCaretPosition(0);
            xml.setOpaque(false);
        }

        return scrollPane;
    }

    /**
     * show fonts displayed
     */
    private static JScrollPane getImageInfoBox(final PdfDecoderInt decode_pdf) {

        /**
         * the generic panel details
         */
        final JPanel details = new JPanel();

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.getViewport().add(details);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        details.setOpaque(true);
        details.setBackground(Color.white);
        details.setEnabled(false);
        details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));

        /**
         * list of Images (not forms)
         */
        final String xmlTxt = decode_pdf.getInfo(PdfDictionary.Image);

        //and display in container
        if (!xmlTxt.isEmpty()) {

            final JTextArea xml = new JTextArea();

            xml.setLineWrap(false);
            xml.setForeground(Color.BLACK);
            xml.setText('\n' + xmlTxt);

            details.add(xml);

            xml.setCaretPosition(0);
            xml.setOpaque(false);

            //details.add(Box.createRigidArea(new Dimension(0,5)));
        }

        return scrollPane;
    }

    private static void displayAvailableFonts(final DefaultMutableTreeNode fontlist) {

        //Remove old font tree display panel
        fontScrollPane.getViewport().removeAll();

        //Create new font list display
        final JPanel jp = new JPanel(new BorderLayout());
        jp.setBackground(Color.WHITE);
        jp.add(new JTree(fontlist), BorderLayout.WEST);

        //Show font tree
        fontScrollPane.getViewport().add(jp);
    }

    /**
     * list of all fonts properties in sorted order
     */
    private static DefaultMutableTreeNode populateAvailableFonts(final DefaultMutableTreeNode top, final String filter) {

        //get list
        if (FontMappings.fontSubstitutionTable != null) {
            final Set fonts = FontMappings.fontSubstitutionTable.keySet();
            final Iterator fontList = FontMappings.fontSubstitutionTable.keySet().iterator();

            final int fontCount = fonts.size();
            final ArrayList fontNames = new ArrayList(fontCount);

            while (fontList.hasNext()) {
                fontNames.add(fontList.next().toString());
            }

            //sort
            Collections.sort(fontNames);

            //Sort and Display Fonts by Directory
            if (sortFontsByDir) {

                final java.util.List location = new ArrayList();
                final java.util.List locationNode = new ArrayList();

                //build display
                for (int ii = 0; ii < fontCount; ii++) {
                    final Object nextFont = fontNames.get(ii);

                    String current = ((String) FontMappings.fontSubstitutionLocation.get(nextFont));

                    int ptr = current.lastIndexOf(System.getProperty("file.separator"));
                    if (ptr == -1 && current.indexOf('/') != -1) {
                        ptr = current.lastIndexOf('/');
                    }

                    if (ptr != -1) {
                        current = current.substring(0, ptr);
                    }

                    if (filter == null || ((String) nextFont).toLowerCase().contains(filter.toLowerCase())) {
                        if (!location.contains(current)) {
                            location.add(current);
                            final DefaultMutableTreeNode loc = new DefaultMutableTreeNode(new DefaultMutableTreeNode(current));
                            top.add(loc);
                            locationNode.add(loc);
                        }

                        final DefaultMutableTreeNode FontTop = new DefaultMutableTreeNode(nextFont + " = " + FontMappings.fontSubstitutionLocation.get(nextFont));
                        final int pos = location.indexOf(current);
                        ((DefaultMutableTreeNode) locationNode.get(pos)).add(FontTop);

                        //add details
                        final String loc = (String) FontMappings.fontPropertiesTable.get(nextFont + "_path");
                        final Integer type = (Integer) FontMappings.fontPropertiesTable.get(nextFont + "_type");

                        final Map properties = StandardFonts.getFontDetails(type, loc);
                        if (properties != null) {

                            for (final Object key : properties.keySet()) {
                                final Object value = properties.get(key);

                                //JLabel fontString=new JLabel(key+" = "+value);
                                //fontString.setFont(new Font("Lucida",Font.PLAIN,10));
                                //details.add(fontString);
                                final DefaultMutableTreeNode FontDetails = new DefaultMutableTreeNode(key + " = " + value);
                                FontTop.add(FontDetails);

                            }
                        }
                    }
                }
            } else {//Show all fonts in one list

                //build display
                for (int ii = 0; ii < fontCount; ii++) {
                    final Object nextFont = fontNames.get(ii);

                    if (filter == null || ((String) nextFont).toLowerCase().contains(filter.toLowerCase())) {
                        final DefaultMutableTreeNode FontTop = new DefaultMutableTreeNode(nextFont + " = " + FontMappings.fontSubstitutionLocation.get(nextFont));
                        top.add(FontTop);

                        //add details
                        final Map properties = (Map) FontMappings.fontPropertiesTable.get(nextFont);
                        if (properties != null) {

                            for (final Object key : properties.keySet()) {
                                final Object value = properties.get(key);

                                //JLabel fontString=new JLabel(key+" = "+value);
                                //fontString.setFont(new Font("Lucida",Font.PLAIN,10));
                                //details.add(fontString);
                                final DefaultMutableTreeNode FontDetails = new DefaultMutableTreeNode(key + " = " + value);
                                FontTop.add(FontDetails);

                            }
                        }
                    }
                }
            }
        }
        return top;
    }

    /**
     * show fonts on system displayed
     */
    private static JScrollPane getFontsAliasesInfoBox() {

        final JPanel details = new JPanel();

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.getViewport().add(details);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        details.setOpaque(true);
        details.setBackground(Color.white);
        details.setEnabled(false);
        details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));

        /**
         * list of all fonts fonts
         */
        final StringBuilder fullList = new StringBuilder();

        for (final Object nextFont : FontMappings.fontSubstitutionAliasTable.keySet()) {
            fullList.append(nextFont);
            fullList.append(" ==> ");
            fullList.append(FontMappings.fontSubstitutionAliasTable.get(nextFont));
            fullList.append('\n');
        }

        final String xmlText = fullList.toString();
        if (!xmlText.isEmpty()) {

            final JTextArea xml = new JTextArea();
            xml.setLineWrap(false);
            xml.setText(xmlText);
            details.add(xml);
            xml.setCaretPosition(0);
            xml.setOpaque(false);

            details.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        return scrollPane;
    }

    //Font tree Display pane
    private static final JScrollPane fontScrollPane = new JScrollPane();

    //<link><a name="fontdetails" />
    /**
     * show fonts on system displayed
     */
    private static JPanel getFontsFoundInfoBox() {

        //Create font list display area
        final JPanel fontDetails = new JPanel(new BorderLayout());
        fontDetails.setBackground(Color.WHITE);
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        fontScrollPane.setBackground(Color.WHITE);
        fontScrollPane.getViewport().setBackground(Color.WHITE);
        fontScrollPane.setPreferredSize(new Dimension(400, 300));
        fontScrollPane.getViewport().add(fontDetails);
        fontScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        fontScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        //This allows the title to be centered above the filter box
        final JPanel filterTitlePane = new JPanel();
        filterTitlePane.setBackground(Color.WHITE);
        final JLabel filterTitle = new JLabel("Filter Font List");
        filterTitlePane.add(filterTitle);

        //Create buttons
        final ButtonGroup bg = new ButtonGroup();
        final JRadioButton folder = new JRadioButton("Sort By Folder");
        folder.setBackground(Color.WHITE);
        final JRadioButton name = new JRadioButton("Sort By Name");
        name.setBackground(Color.WHITE);
        final JTextField filter = new JTextField();

        //Ensure correct display mode selected
        if (sortFontsByDir) {
            folder.setSelected(true);
        } else {
            name.setSelected(true);
        }

        bg.add(folder);
        bg.add(name);
        final JPanel buttons = new JPanel(new BorderLayout());
        buttons.setBackground(Color.WHITE);
        buttons.add(filterTitlePane, BorderLayout.NORTH);
        buttons.add(folder, BorderLayout.WEST);
        buttons.add(filter, BorderLayout.CENTER);
        buttons.add(name, BorderLayout.EAST);

        folder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (!sortFontsByDir) {
                    DefaultMutableTreeNode fontlist = new DefaultMutableTreeNode("Fonts");
                    sortFontsByDir = true;
                    fontlist = populateAvailableFonts(fontlist, filter.getText());
                    displayAvailableFonts(fontlist);
                }
            }
        });

        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (sortFontsByDir) {
                    DefaultMutableTreeNode fontlist = new DefaultMutableTreeNode("Fonts");
                    sortFontsByDir = false;
                    fontlist = populateAvailableFonts(fontlist, filter.getText());
                    displayAvailableFonts(fontlist);
                }
            }
        });

        filter.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent e) {
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                final DefaultMutableTreeNode fontlist = new DefaultMutableTreeNode("Fonts");
                populateAvailableFonts(fontlist, ((JTextField) e.getSource()).getText());
                displayAvailableFonts(fontlist);
            }

            @Override
            public void keyTyped(final KeyEvent e) {
            }
        });

        //Start tree here
        DefaultMutableTreeNode top
                = new DefaultMutableTreeNode("Fonts");

        //Populate font list and build tree
        top = populateAvailableFonts(top, null);
        final JTree fontTree = new JTree(top);
        //Added to keep the tree left aligned when top parent is closed
        fontDetails.add(fontTree, BorderLayout.WEST);

        //Peice it all together
        panel.add(buttons, BorderLayout.NORTH);
        panel.add(fontScrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(400, 300));

        return panel;
    }

    /**
     * provide list of forms
     */
    private static JScrollPane getFormList(final PdfDecoderInt decode_pdf) {

        JScrollPane scroll = null;

        //get the form renderer
        final org.jpedal.objects.acroforms.AcroRenderer formRenderer = decode_pdf.getFormRenderer();

        if (formRenderer != null) {

            //get list of forms on page
            final Object[] formsOnPage = formRenderer.getFormComponents(null, ReturnValues.FORM_NAMES, decode_pdf.getPageNumber());

            //allow for no forms
            if (formsOnPage != null) {

                final int formCount = formsOnPage.length;

                final JPanel formPanel = new JPanel();

                scroll = new JScrollPane();
                scroll.setPreferredSize(new Dimension(400, 300));
                scroll.getViewport().add(formPanel);
                scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                /**
                 * create a JPanel to list forms and popup details
                 */
                formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
                final JLabel formHeader = new JLabel("This page contains " + formCount + " form objects");
                formHeader.setFont(headFont);
                formPanel.add(formHeader);

                formPanel.add(Box.createRigidArea(new Dimension(10, 10)));

                /**
                 * populate our list with details
                 */
                for (final Object aFormsOnPage : formsOnPage) {

                    // get name of form
                    final String formName = (String) aFormsOnPage;

                    //swing component we map data into
                    final Object[] comp = formRenderer.getFormComponents(formName, ReturnValues.GUI_FORMS_FROM_NAME, -1);

                    if (comp != null) {

                        //take value or first if array to check for types (will be same if children)
                        FormObject formObj = null;

                        //extract list of actual PDF references to display and get FormObject
                        StringBuilder PDFrefs = new StringBuilder("PDF ref=");

                        //actual data read from PDF
                        final Object[] rawFormData = formRenderer.getFormComponents(formName, ReturnValues.FORMOBJECTS_FROM_NAME, -1);
                        for (final Object aRawFormData : rawFormData) {
                            formObj = (FormObject) aRawFormData;
                            PDFrefs.append(' ').append(formObj.getObjectRefAsString());
                        }

                        final JLabel ref = new JLabel(PDFrefs.toString());

                        /**
                         * display the form component description
                         */
                        // int formComponentType = ((Integer) formData.getTypeValueByName(formName)).intValue();
                        final JLabel header = new JLabel(formName);

                        final JLabel type = new JLabel();
                        type.setText("Type="
                                + PdfDictionary.showAsConstant(formObj.getParameterConstant(PdfDictionary.Type))
                                + " Subtype=" + PdfDictionary.showAsConstant(formObj.getParameterConstant(PdfDictionary.Subtype)));

                        /**
                         * get the current Swing component type
                         */
                        final String standardDetails = "java class=" + comp[0].getClass();

                        final JLabel details = new JLabel(standardDetails);

                        header.setFont(headFont);
                        header.setForeground(Color.blue);

                        type.setFont(textFont);
                        type.setForeground(Color.blue);

                        details.setFont(textFont);
                        details.setForeground(Color.blue);

                        ref.setFont(textFont);
                        ref.setForeground(Color.blue);

                        formPanel.add(header);
                        formPanel.add(type);
                        formPanel.add(details);
                        formPanel.add(ref);

                        /**
                         * not currently used or setup JButton more = new
                         * JButton("View Form Data"); more.setFont(textFont);
                         * more.setForeground(Color.blue);
                         *
                         * more.addActionListener(new
                         * ShowFormDataListener(formName)); formPanel.add(more);
                         *
                         * formPanel.add(new JLabel(" "));
                         *
                         * /*
                         */
                    }
                }
            }
        }

        return scroll;
    }

    /**
     * page info option
     */
    private static JScrollPane getXMLInfoBox(final String xmlText) {

        final JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));

        details.setOpaque(true);
        details.setBackground(Color.white);

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.getViewport().add(details);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final JTextArea xml = new JTextArea();

        xml.setRows(5);
        xml.setColumns(15);
        xml.setLineWrap(true);
        xml.setText(xmlText);
        details.add(new JScrollPane(xml));
        xml.setCaretPosition(0);
        xml.setOpaque(true);
        xml.setBackground(Color.white);

        return scrollPane;

    }

}
