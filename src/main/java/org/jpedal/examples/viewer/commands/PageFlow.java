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
 * PageFlow.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jpedal.*;
import org.jpedal.display.Display;
import org.jpedal.display.GUIDisplay;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.Viewer;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.JavaFXHelper;
import org.jpedal.utils.Messages;

/**
 *
 */
public class PageFlow {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf, final PropertiesFile properties, final GUISearchWindow searchFrame) {

        if (!decode_pdf.isOpen() || decode_pdf.getDisplayView()==Display.PAGEFLOW) {
            return;
        }

        //search does not work in coverflow so stop and close if running
        searchFrame.removeSearchWindow(false);

        if (args == null) {

            //Added condition to disable coverflow on applet.
            if (commonValues.getModeOfOperation() != Values.RUNNING_APPLET) {// && JavaFXHelper.isJavaFXAvailable()){

                //display dialog advising Java FX usage
                if (!JavaFXHelper.isJavaFXAvailable()) {
                    final String flag = System.getProperty("org.jpedal.suppressViewerPopups");
                    boolean suppressViewerPopups = false;

                    if (flag != null && flag.equalsIgnoreCase("true")) {
                        suppressViewerPopups = true;
                    }

                    final String propValue = properties.getValue("showpageflowmessage");
                    if (!suppressViewerPopups && properties != null && (!propValue.isEmpty() && propValue.equals("true"))) {
                        final JPanel a = new JPanel();
                        a.setLayout(new BoxLayout(a, BoxLayout.Y_AXIS));

                        final JLabel m1 = new JLabel(Messages.getMessage("PdfViewer.PageFlowJarsNeeded.Message"));
                        m1.setHorizontalTextPosition(JLabel.CENTER);
                        a.add(m1);

                        final MouseAdapter linkListener = new MouseAdapter() {
                            @Override
                            public void mouseEntered(final MouseEvent e) {
                                if (GUIDisplay.allowChangeCursor) {
                                    a.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                }
                            }

                            @Override
                            public void mouseExited(final MouseEvent e) {
                                if (GUIDisplay.allowChangeCursor) {
                                    a.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                }
                            }

                            @Override
                            public void mouseClicked(final MouseEvent e) {
                                try {
                                    BrowserLauncher.openURL(Messages.getMessage("PdfViewer.PageFlowJarsNeeded.Link"));
                                } catch (final Exception e1) {
                                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.ErrorWebsite"));
                                    //
                                }
                            }
                        };
                        final JLabel link = new JLabel("<html><u>" + (Messages.getMessage("PdfViewer.PageFlowJarsNeeded.Download")) + "</u></html>");
                        link.setForeground(Color.BLUE);
                        link.addMouseListener(linkListener);
                        link.setHorizontalAlignment(JLabel.CENTER);
                        a.add(link);

                        final Object[] options = {Messages.getMessage("PdfViewer.PageFlowJarsNeeded.Continue")};
                        JOptionPane.showOptionDialog(
                                (Container)currentGUI.getFrame(),
                                a,
                                Messages.getMessage("PdfViewer.PageFlowJarsNeeded.Title"),
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                options,
                                options[0]);

                        return;
                    }
                }

                currentGUI.getCombo(Commands.SCALING).setEnabled(false);
                currentGUI.getButtons().getButton(Commands.MOUSEMODE).setEnabled(false);
                currentGUI.getButtons().getButton(Commands.SNAPSHOT).setEnabled(false);
                
                currentGUI.getButtons().alignLayoutMenuOption(Display.PAGEFLOW);
                
                if (Viewer.isFX()) {
                    ModeChange.changeModeInJavaFX(Display.PAGEFLOW, decode_pdf, currentGUI, commonValues,  properties, searchFrame);
                } else {
                    ModeChange.changeModeInSwing(Display.PAGEFLOW, decode_pdf, currentGUI, commonValues,  properties, searchFrame);
                }
                
                          

            } else {
                //Case 8720: Temporarily disabled PageFlow when running on a Applet
                if (commonValues.getModeOfOperation() == Values.RUNNING_APPLET) {
                    currentGUI.showMessageDialog("PageFlow temporarily disabled for Applet");
                }

                currentGUI.getButtons().alignLayoutMenuOption(Display.SINGLE_PAGE);
                if (SwingUtilities.isEventDispatchThread()) {

                    currentGUI.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);

                    currentGUI.getButtons().hideRedundentNavButtons(currentGUI);
                    ((GUI)currentGUI).setSelectedComboIndex(Commands.ROTATION, 0);
                } else {
                    currentGUI.setCommandInThread(true);
                    final Runnable doPaintComponent = new Runnable() {

                        @Override
                        public void run() {
                            currentGUI.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);

                            currentGUI.getButtons().hideRedundentNavButtons(currentGUI);
                            ((GUI)currentGUI).setSelectedComboIndex(Commands.ROTATION, 0);

                            currentGUI.setExecutingCommand(false);
                        }
                    };
                    SwingUtilities.invokeLater(doPaintComponent);
                }

            }

            currentGUI.getCombo(Commands.ROTATION).setEnabled(false);
        } else {

        }
    }
}
