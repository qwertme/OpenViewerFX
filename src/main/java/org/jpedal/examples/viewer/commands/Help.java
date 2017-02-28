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
 * Help.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jpedal.display.GUIDisplay;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.LogWriter;

/**
 * Code to handle Help button we display in demo versions of Viewer
 */
public class Help {

    public static void execute(final Object[] args, final GUIFactory currentGUI) {
        if (args == null) {
            getHelpBox(currentGUI);
        }

//        if(args==null){
//            try {
//                BrowserLauncher.openURL("https://idrsolutions.fogbugz.com/default.asp?support");
//            } catch (IOException e1) {
//                currentGUI.showMessageDialog("Please visit https://idrsolutions.fogbugz.com/default.asp?support");
//                //
//            }
//        }
    }

    private static void getHelpBox(final GUIFactory currentGUI) {
        final JPanel panel = new JPanel();

        final JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        final JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JLabel label = new JLabel("<html><p>Please click on this link for lots of tutorials and documentation</p>");
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        labelPanel.add(label);
        labelPanel.add(Box.createHorizontalGlue());

        top.add(labelPanel);

        final JPanel linkPanel = new JPanel();
        linkPanel.setLayout(new BoxLayout(linkPanel, BoxLayout.X_AXIS));
        linkPanel.add(Box.createHorizontalGlue());
        linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JLabel url = new JLabel("<html><center>http://www.idrsolutions.com/java-pdf-library-support/");
        url.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        url.setForeground(Color.blue);
        url.setHorizontalAlignment(JLabel.CENTER);

        //Create cursor handling
        url.addMouseListener(new MouseListener() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                if (GUIDisplay.allowChangeCursor) {
                    panel.getTopLevelAncestor().setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                url.setText("<html><center><a>http://www.idrsolutions.com/java-pdf-library-support/</a></center>");
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                if (GUIDisplay.allowChangeCursor) {
                    panel.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                url.setText("<html><center>http://www.idrsolutions.com/java-pdf-library-support/");
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                try {
                    BrowserLauncher.openURL("http://www.idrsolutions.com/java-pdf-library-support/");
                } catch (final Exception e1) {

//                JPanel errorPanel = new JPanel();
//                errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
//
//                JLabel errorMessage = new JLabel("Your web browser could not be successfully loaded.  " +
//                "Please copy and paste the URL below, manually into your web browser.");
//                errorMessage.setAlignmentX(JLabel.LEFT_ALIGNMENT);
//                errorMessage.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//
//                JTextArea textArea = new JTextArea("http://www.idrsolutions.com/java-pdf-library-support/");
//                textArea.setEditable(false);
//                textArea.setRows(5);
//                textArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//                textArea.setAlignmentX(JTextArea.LEFT_ALIGNMENT);
//
//                errorPanel.add(errorMessage);
//                errorPanel.add(textArea);
//
//                showMessageDialog(errorPanel,"Error loading web browser",JOptionPane.PLAIN_MESSAGE);
                    //
                    
                     if(LogWriter.isOutput()) { 
                         LogWriter.writeLog("Exception attempting launch browser: " + e1); 
                     } 
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
            }
        });

        linkPanel.add(url);
        linkPanel.add(Box.createHorizontalGlue());
        top.add(linkPanel);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(top);

        currentGUI.showMessageDialog(panel, "JPedal Tutorials and documentation", JOptionPane.PLAIN_MESSAGE);
    }
}
