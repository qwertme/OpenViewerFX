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
 * RSSyndication.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.jpedal.display.GUIDisplay;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.BrowserLauncher;

/**
 * Code to handle RSS button we display in demo versions of Viewer
 */
public class RSSyndication {

    public static void execute(final Object[] args, final GUIFactory currentGUI) {
        if (args == null) {
            getRSSBox(currentGUI);
        } else {

        }
    }
    
    
	public static void getRSSBox(final GUIFactory currentGUI) {
		final JPanel panel = new JPanel();

		final JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

		final JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JLabel label = new JLabel("Click on the link below to load a web browser and sign up to our RSS feed.");
		label.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		labelPanel.add(label);
		labelPanel.add(Box.createHorizontalGlue());

		top.add(labelPanel);

		final JPanel linkPanel = new JPanel();
		linkPanel.setLayout(new BoxLayout(linkPanel, BoxLayout.X_AXIS));
		linkPanel.add(Box.createHorizontalGlue());
		linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JLabel url=new JLabel("<html><center>"+"http://www.jpedal.org/jpedal.rss");
		url.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		url.setForeground(Color.blue);
		url.setHorizontalAlignment(JLabel.CENTER);
        
		//Create cursor control
		url.addMouseListener(new MouseListener() {
			@Override
            public void mouseEntered(final MouseEvent e) {
				if(GUIDisplay.allowChangeCursor) {
                    panel.getTopLevelAncestor().setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
				url.setText("<html><center><a>http://www.jpedal.org/jpedal.rss</a></center>");
			}

			@Override
            public void mouseExited(final MouseEvent e) {
				if(GUIDisplay.allowChangeCursor) {
                    panel.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
				url.setText("<html><center>http://www.jpedal.org/jpedal.rss");
			}

			@Override
            public void mouseClicked(final MouseEvent e) {
                try {
                    BrowserLauncher.openURL("http://www.jpedal.org/jpedal.rss");
                } catch (final Exception ex) {
                    Logger.getLogger(RSSyndication.class.getName()).log(Level.SEVERE, null, ex);
                }

			} 
           
			@Override
            public void mousePressed(final MouseEvent e) {}
			@Override
            public void mouseReleased(final MouseEvent e) {}
		});

		linkPanel.add(url);
		linkPanel.add(Box.createHorizontalGlue());
		top.add(linkPanel);

		final JLabel image = new JLabel(new ImageIcon(RSSyndication.class.getClass().getResource("/org/jpedal/examples/viewer/res/rss.png")));
		image.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		final JPanel imagePanel = new JPanel();
		imagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
		imagePanel.add(Box.createHorizontalGlue());
		imagePanel.add(image);
		imagePanel.add(Box.createHorizontalGlue());

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(top);
		panel.add(imagePanel);

		currentGUI.showMessageDialog(panel,"Subscribe to JPedal RSS Feed",JOptionPane.PLAIN_MESSAGE);
	}

}
