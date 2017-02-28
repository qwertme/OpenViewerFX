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
 * ExtractText.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.generic.GUICopy;
import org.jpedal.examples.viewer.commands.generic.GUIExtractText;
import org.jpedal.exception.PdfException;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Messages;

/**
 * Class to Handle the popup dialogs created when user right clicks 
 * highlighted text and chooses text extraction.
 */
public class ExtractText extends GUIExtractText {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final Values commonValues) {
        if (args == null) {
            extractSelectedText(currentGUI, decode_pdf, commonValues);
        } else {

        }
    }

    /**
     * routine to link GUI into text extraction functions
     */
    private static void extractSelectedText(final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final Values commonValues) {

        if (!decode_pdf.isExtractionAllowed()) {
            currentGUI.showMessageDialog("Not allowed");
            return;
        }

        final int[][] highlights = decode_pdf.getTextLines().getHighlightedAreasAs2DArray(commonValues.getCurrentPage());
        /**
         * ensure co-ords in right order
         */
        if (highlights == null) {
            //
            return;
        }

        /**
         * Window gui components
         */
        final JScrollPane examplePane = new JScrollPane();
        final JPanel display_value = new JPanel();
        final ButtonGroup group = new ButtonGroup();
        final JRadioButton text = new JRadioButton("Extract as Text");
        final JRadioButton xml = new JRadioButton("Extract  as  XML");
        final JRadioButton rectangleGrouping = new JRadioButton(Messages.getMessage("PdfViewerRect.label"));
        final JRadioButton tableGrouping = new JRadioButton(Messages.getMessage("PdfViewerTable.label"));
        final JRadioButton wordListExtraction = new JRadioButton(Messages.getMessage("PdfViewerWordList.label"));
        final SpringLayout layout = new SpringLayout();
        final JFrame extractionFrame = new JFrame(Messages.getMessage("PdfViewerCoords.message")
                + ' ' + commonValues.m_x1
                + " , " + commonValues.m_y1 + " , " + (commonValues.m_x2 - commonValues.m_x1) + " , " + (commonValues.m_y2 - commonValues.m_y1));
        //JLabel demoMessage = new JLabel(Messages.getMessage("PdfViewerDemo.message"));
        extractionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JLabel demoMessage = new JLabel("                         ");
        final ButtonGroup type = new ButtonGroup();
        final Object[] options = {Messages.getMessage("PdfViewerHelpMenu.text"),
            Messages.getMessage("PdfViewerCancel.text"),
            Messages.getMessage("PdfViewerextract.text")};
        final JButton help = new JButton((String) options[0]);
        final JButton cancel = new JButton((String) options[1]);
        final JButton extract = new JButton((String) options[2]);
        display_value.setLayout(layout);

        /**
         * Used to udpate the example scrollpane when an option is changed.
         */
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                final Enumeration en = group.getElements();
                while (en.hasMoreElements()) { //First find which button has been changed
                    final AbstractButton button = (AbstractButton) en.nextElement();
                    if (button.isSelected()) {
                        final Component[] com = display_value.getComponents();
                        for (int i = 0; i != com.length; i++) {
                            if (com[i] instanceof JScrollPane) {
                                display_value.remove(com[i]);
                            }
                        }

                        try {
                            final JScrollPane scroll = updateExtractionExample(decode_pdf, commonValues, currentGUI, button, xml.isSelected());
                            if (scroll != null) {
                                layout.putConstraint(SpringLayout.EAST, scroll, -5, SpringLayout.EAST, display_value);
                                layout.putConstraint(SpringLayout.NORTH, scroll, 5, SpringLayout.SOUTH, tableGrouping);
                                display_value.add(scroll);
                            } else {

                                final JLabel noExample = new JLabel("No Example Available");

                                Font exampleFont = noExample.getFont();
                                exampleFont = exampleFont.deriveFont(exampleFont.getStyle(), 20f);//change as ME has no deriveFont(size only)

                                noExample.setFont(exampleFont);
                                noExample.setForeground(Color.RED);

                                layout.putConstraint(SpringLayout.EAST, noExample, -75, SpringLayout.EAST, display_value);
                                layout.putConstraint(SpringLayout.NORTH, noExample, 50, SpringLayout.SOUTH, tableGrouping);
                                display_value.add(noExample);
                            }
                        } catch (final PdfException ex) {
                            Logger.getLogger(ExtractText.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        break;
                    }
                }

                //Update the display to ensure it is going to be displayed correctly
                display_value.updateUI();
            }
        };

        //Add demo message to the bottom of the display
        demoMessage.setFont(new Font("SansSerif", Font.BOLD, 10));
        demoMessage.setForeground(Color.red);
        layout.putConstraint(SpringLayout.WEST, demoMessage, 5, SpringLayout.WEST, display_value);
        layout.putConstraint(SpringLayout.SOUTH, demoMessage, -5, SpringLayout.SOUTH, display_value);
        display_value.add(demoMessage);

        /**
         * Add grouping buttons to the top of the display
         */
        //Rectangle grouping
        rectangleGrouping.setSelected(true);
        rectangleGrouping.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                xml.setText("Extract  as  XML");
                text.setText("Extract as Text");
                SwingUtilities.invokeLater(r);
            }
        });
        group.add(rectangleGrouping);
        rectangleGrouping.setToolTipText(Messages.getMessage("PdfViewerRect.message"));
        layout.putConstraint(SpringLayout.WEST, rectangleGrouping, 10, SpringLayout.WEST, display_value);
        layout.putConstraint(SpringLayout.NORTH, rectangleGrouping, 5, SpringLayout.NORTH, display_value);
        display_value.add(rectangleGrouping);
        //Table Grouping
        tableGrouping.setSelected(true);
        tableGrouping.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                xml.setText("Extract as XHTML");
                text.setText("Extract as CSV");
                SwingUtilities.invokeLater(r);
            }
        });
        group.add(tableGrouping);
        tableGrouping.setToolTipText(Messages.getMessage("PdfViewerTable.message"));
        layout.putConstraint(SpringLayout.WEST, tableGrouping, 50, SpringLayout.EAST, rectangleGrouping);
        layout.putConstraint(SpringLayout.NORTH, tableGrouping, 5, SpringLayout.NORTH, display_value);
        display_value.add(tableGrouping);
        //WordList Grouping
        wordListExtraction.setSelected(true);
        wordListExtraction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                xml.setText("Extract  as  XML");
                text.setText("Extract as Text");
                SwingUtilities.invokeLater(r);
            }
        });
        group.add(wordListExtraction);
        wordListExtraction.setToolTipText(Messages.getMessage("PdfViewerWordList.message"));
        layout.putConstraint(SpringLayout.EAST, wordListExtraction, -5, SpringLayout.EAST, display_value);
        layout.putConstraint(SpringLayout.NORTH, wordListExtraction, 5, SpringLayout.NORTH, display_value);
        display_value.add(wordListExtraction);

        //Add example pane to the window
        examplePane.setPreferredSize(new Dimension(315, 150));
        examplePane.setMinimumSize(new Dimension(315, 150));
        layout.putConstraint(SpringLayout.EAST, examplePane, -5, SpringLayout.EAST, display_value);
        layout.putConstraint(SpringLayout.NORTH, examplePane, 5, SpringLayout.SOUTH, tableGrouping);
        display_value.add(examplePane);

        //Add xml and text radio buttons
        type.add(xml);
        type.add(text);
        xml.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SwingUtilities.invokeLater(r);
            }
        });
        text.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SwingUtilities.invokeLater(r);
            }
        });
        text.setSelected(true);
        layout.putConstraint(SpringLayout.WEST, xml, 5, SpringLayout.WEST, display_value);
        layout.putConstraint(SpringLayout.SOUTH, xml, -5, SpringLayout.NORTH, extract);
        display_value.add(xml);
        layout.putConstraint(SpringLayout.EAST, text, -5, SpringLayout.EAST, display_value);
        layout.putConstraint(SpringLayout.SOUTH, text, -5, SpringLayout.NORTH, extract);
        display_value.add(text);

        //Add the bottom buttons. Extract, Help and Cancel
        layout.putConstraint(SpringLayout.SOUTH, extract, -5, SpringLayout.NORTH, demoMessage);
        layout.putConstraint(SpringLayout.EAST, extract, -5, SpringLayout.EAST, display_value);
        display_value.add(extract);
        layout.putConstraint(SpringLayout.SOUTH, cancel, -5, SpringLayout.NORTH, demoMessage);
        layout.putConstraint(SpringLayout.EAST, cancel, -5, SpringLayout.WEST, extract);
        display_value.add(cancel);
        layout.putConstraint(SpringLayout.SOUTH, help, -5, SpringLayout.NORTH, demoMessage);
        layout.putConstraint(SpringLayout.EAST, help, -5, SpringLayout.WEST, cancel);
        display_value.add(help);

        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JTextArea info = new JTextArea(Messages.getMessage("PdfViewerGroupingInfo.message"));

                currentGUI.showMessageDialog(info);
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                extractionFrame.setVisible(false);
                extractionFrame.dispose();
            }
        });
        extract.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    String finalValue = ""; // Total data extracted so far
                    final boolean isXML = true;

                    final PdfPageData page_data = decode_pdf.getPdfPageData();

                    final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
                    final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
                    final int cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
                    final int cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());

                    if (highlights != null) {
                        for (int t = 0; t != highlights.length; t++) {
                            String extractedText = "";

                            /**
                             * ensure co-ords in right order
                             */
                            highlights[t] = GUICopy.adjustHighlightForExtraction(highlights[t]);

                            int t_x1 = highlights[t][0];
                            int t_x2 = highlights[t][0] + highlights[t][2];
                            int t_y1 = highlights[t][1] + highlights[t][3];
                            int t_y2 = highlights[t][1];

                            if (t_y1 < t_y2) {
                                final int temp = t_y2;
                                t_y2 = t_y1;
                                t_y1 = temp;
                            }

                            if (t_x1 > t_x2) {
                                final int temp = t_x2;
                                t_x2 = t_x1;
                                t_x1 = temp;
                            }

                            if (t_x1 < cropX) {
                                t_x1 = cropX;
                            }
                            if (t_x1 > cropW - cropX) {
                                t_x1 = cropW - cropX;
                            }

                            if (t_x2 < cropX) {
                                t_x2 = cropX;
                            }
                            if (t_x2 > cropW - cropX) {
                                t_x2 = cropW - cropX;
                            }

                            if (t_y1 < cropY) {
                                t_y1 = cropY;
                            }
                            if (t_y1 > cropH - cropY) {
                                t_y1 = cropH - cropY;
                            }

                            if (t_y2 < cropY) {
                                t_y2 = cropY;
                            }
                            if (t_y2 > cropH - cropY) {
                                t_y2 = cropH - cropY;
                            }

                            if (rectangleGrouping.isSelected()) {
                                //text extraction
                                extractedText = extractTextRectangle(commonValues, decode_pdf, currentGUI, xml.isSelected(), t_x1, t_x2, t_y1, t_y2) + ((char) 0x0D) + ((char) 0x0A);
                            } else if (tableGrouping.isSelected()) {
                                //text table extraction
                                extractedText = extractTextTable(commonValues, decode_pdf, xml.isSelected(), t_x1, t_x2, t_y1, t_y2);
                            } else if (wordListExtraction.isSelected()) {
                                //text wordlist extraction
                                extractedText = extractTextList(decode_pdf, commonValues, currentGUI, xml.isSelected(), t_x1, t_x2, t_y1, t_y2);
                            }

                            finalValue += extractedText;
                        }
                    }

                    //Once all data is stored in finalValue, produce output window
                    if (finalValue != null) {

                        //Create scrollpane containg the final data
                        JScrollPane scroll = new JScrollPane();
                        try {
                            final JTextPane text_pane = new JTextPane();
                            scroll = createPane(text_pane, finalValue, isXML);
                        } catch (final BadLocationException e1) {
                            e1.printStackTrace();
                        }
                        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        scroll.setPreferredSize(new Dimension(400, 400));

                        /**
                         * Create a resizeable pop-up for content
                         */
                        final JDialog displayFrame = new JDialog((JFrame) null, true);
                        displayFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        if (commonValues.getModeOfOperation() != Values.RUNNING_APPLET) {
                            final Container frame = (Container)currentGUI.getFrame();
                            displayFrame.setLocation(frame.getLocationOnScreen().x + 10, frame.getLocationOnScreen().y + 10);
                        }
                        displayFrame.setSize(450, 450);
                        displayFrame.setTitle(Messages.getMessage("PdfViewerExtractedText.menu"));
                        displayFrame.getContentPane().setLayout(new BorderLayout());
                        displayFrame.getContentPane().add(scroll, BorderLayout.CENTER);

                        //Add buttons
                        final JPanel buttonBar = new JPanel();
                        buttonBar.setLayout(new BorderLayout());
                        displayFrame.getContentPane().add(buttonBar, BorderLayout.SOUTH);

                        /**
                         * yes option allows user to save content
                         */
                        final JButton yes = new JButton(Messages.getMessage("PdfViewerMenu.return"));
                        yes.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        buttonBar.add(yes, BorderLayout.WEST);
                        yes.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(final ActionEvent e) {
                                displayFrame.dispose();

                            }
                        });

                        /**
                         * no option just removes display
                         */
                        final JButton no = new JButton(Messages.getMessage("PdfViewerFileMenuExit.text"));
                        no.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        buttonBar.add(no, BorderLayout.EAST);
                        no.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(final ActionEvent e) {

                                displayFrame.dispose();
                            }
                        });

                        /**
                         * show the popup
                         */
                        displayFrame.setVisible(true);
                    }
                } catch (final PdfException e1) {
                    e1.printStackTrace();
                }
            }
        });

        //Add display panel to the extraction options window
        extractionFrame.getContentPane().add(display_value, BorderLayout.CENTER);
        extractionFrame.setSize(350, 300);

        //Initialise example
        SwingUtilities.invokeLater(r);

        //Set location over window
        extractionFrame.setLocationRelativeTo((Container)currentGUI.getFrame());
        extractionFrame.setResizable(false);

        //Display
        extractionFrame.setVisible(true);

    }

    private static JScrollPane createPane(final JTextPane text_pane, final String content, final boolean useXML) throws BadLocationException {

        text_pane.setEditable(true);
        text_pane.setFont(new Font("Lucida", Font.PLAIN, 14));

        text_pane.setToolTipText(Messages.getMessage("PdfViewerTooltip.text"));
        final Document doc = text_pane.getDocument();
        text_pane.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), Messages.getMessage("PdfViewerTitle.text")));
        text_pane.setForeground(Color.black);

        final SimpleAttributeSet token_attribute = new SimpleAttributeSet();
        final SimpleAttributeSet text_attribute = new SimpleAttributeSet();
        final SimpleAttributeSet plain_attribute = new SimpleAttributeSet();
        StyleConstants.setForeground(token_attribute, Color.blue);
        StyleConstants.setForeground(text_attribute, Color.black);
        StyleConstants.setForeground(plain_attribute, Color.black);
        int pointer = 0;

        /**
         * put content in and color XML
         */
        if ((useXML) && (content != null)) {
            //tokenise and write out data
            final StringTokenizer data_As_tokens = new StringTokenizer(content, "<>", true);

            while (data_As_tokens.hasMoreTokens()) {
                final String next_item = data_As_tokens.nextToken();

                if ((next_item.equals("<")) && ((data_As_tokens.hasMoreTokens()))) {

                    final String current_token = next_item + data_As_tokens.nextToken() + data_As_tokens.nextToken();

                    doc.insertString(pointer, current_token, token_attribute);
                    pointer += current_token.length();

                } else {
                    doc.insertString(pointer, next_item, text_attribute);
                    pointer += next_item.length();
                }
            }
        } else {
            doc.insertString(pointer, content, plain_attribute);
        }

        //wrap in scrollpane
        final JScrollPane text_scroll = new JScrollPane();
        text_scroll.getViewport().add(text_pane);
        text_scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        text_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return text_scroll;
    }

    private static JScrollPane updateExtractionExample(final PdfDecoderInt decode_pdf, final Values commonValues, final GUIFactory currentGUI, final AbstractButton button, final boolean xml) throws PdfException {

        JScrollPane scroll = new JScrollPane();
        String finalString = "";

        final PdfPageData page_data = decode_pdf.getPdfPageData();

        final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
        final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
        final int cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
        final int cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());

        final int[][] highlights = decode_pdf.getTextLines().getHighlightedAreasAs2DArray(commonValues.getCurrentPage());

        if (highlights != null) {
            for (int t = 0; t != highlights.length; t++) {
                if (highlights[t] != null) {

                    highlights[t] = GUICopy.adjustHighlightForExtraction(highlights[t]);

                    int t_x1 = highlights[t][0];
                    int t_x2 = highlights[t][0] + highlights[t][2];
                    int t_y1 = highlights[t][1] + highlights[t][3];
                    int t_y2 = highlights[t][1];

                    if (t_y1 < t_y2) {
                        final int temp = t_y2;
                        t_y2 = t_y1;
                        t_y1 = temp;
                    }

                    if (t_x1 > t_x2) {
                        final int temp = t_x2;
                        t_x2 = t_x1;
                        t_x1 = temp;
                    }

                    if (t_x1 < cropX) {
                        t_x1 = cropX;
                    }
                    if (t_x1 > cropX + cropW) {
                        t_x1 = cropX + cropW;
                    }

                    if (t_x2 < cropX) {
                        t_x2 = cropX;
                    }
                    if (t_x2 > cropX + cropW) {
                        t_x2 = cropX + cropW;
                    }

                    if (t_y1 < cropY) {
                        t_y1 = cropY;
                    }
                    if (t_y1 > cropY + cropH) {
                        t_y1 = cropY + cropH;
                    }

                    if (t_y2 < cropY) {
                        t_y2 = cropY;
                    }
                    if (t_y2 > cropY + cropH) {
                        t_y2 = cropY + cropH;
                    }

                    if (button.getText().equals("Table")) {
                        finalString += extractTextTable(commonValues, decode_pdf, xml, t_x1, t_x2, t_y1, t_y2);
                    }

                    if (button.getText().equals("Rectangle")) {
                        finalString = finalString + extractTextRectangle(commonValues, decode_pdf, currentGUI, xml, t_x1, t_x2, t_y1, t_y2) + ' ';
                    }

                    if (button.getText().equals("WordList")) {
                        finalString += extractTextList(decode_pdf, commonValues, currentGUI, xml, t_x1, t_x2, t_y1, t_y2);
                    }
                }
            }
        }

        if (!finalString.isEmpty()) {

            try {
                scroll = createPane(new JTextPane(), finalString, xml);
            } catch (final BadLocationException e) {
                e.printStackTrace();
            }

            scroll.setPreferredSize(new Dimension(315, 150));
            scroll.setMinimumSize(new Dimension(315, 150));

            final Component[] coms = scroll.getComponents();
            for (int i = 0; i != coms.length; i++) {
                if (scroll.getComponent(i) instanceof JViewport) {
                    final JViewport view = (JViewport) scroll.getComponent(i);
                    final Component[] coms1 = view.getComponents();
                    for (int j = 0; j != coms1.length; j++) {
                        if (coms1[j] instanceof JTextPane) {
                            ((JTextPane) coms1[j]).setEditable(false);
                        }
                    }
                }
            }
            return scroll;
        }
        return null;
    }

}
