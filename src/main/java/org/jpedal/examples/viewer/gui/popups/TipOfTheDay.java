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
 * TipOfTheDay.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.utils.BrowserLauncher;

public class TipOfTheDay extends JDialog {
	
	private final List tipPaths = new ArrayList();
	
	private boolean tipLoadingFailed;

	private int currentTip;

	private final JEditorPane tipPane = new JEditorPane();

	private final JCheckBox showTipsOnStartup = new JCheckBox("Show Tips on Startup");
	
	public TipOfTheDay(final Container parent, final String tipsRoot, final PropertiesFile propertiesFile){
		super((JFrame)null, "Tip of the Day", true);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		try {
			populateTipsList(tipsRoot, tipPaths);
		} catch (final IOException e) {
			tipLoadingFailed = true;
			//
		}
		
		final Random r = new Random();
		currentTip = r.nextInt(tipPaths.size());
		
		setSize(550, 350);
		
		init(propertiesFile);
		
		setLocationRelativeTo(parent);
	}

	private void init(final PropertiesFile propertiesFile) {
		getContentPane().setLayout(new GridBagLayout());
		final GridBagConstraints mainPanelConstraints = new GridBagConstraints();

		mainPanelConstraints.gridx = 0;
		mainPanelConstraints.gridy = 0;
		mainPanelConstraints.fill = GridBagConstraints.HORIZONTAL; 
		mainPanelConstraints.anchor = GridBagConstraints.PAGE_START;
		mainPanelConstraints.weighty = 0;
		mainPanelConstraints.weightx = 0;
		mainPanelConstraints.insets = new Insets(10,10,0,10);
		
		/**
		 * add the top panel to the Dialog, this is the image, and the title "Did you know ... ?"
		 */
		addTopPanel(mainPanelConstraints);
		
		mainPanelConstraints.fill = GridBagConstraints.BOTH; 
		mainPanelConstraints.gridy = 1;
		mainPanelConstraints.weighty = 1;
		mainPanelConstraints.weightx = 1;
		
		/**
		 * add the main JEditorPane to the Dialog which displays the html files
		 */
		addCenterTip(mainPanelConstraints);

		mainPanelConstraints.fill = GridBagConstraints.HORIZONTAL; 
		mainPanelConstraints.gridy = 2;
		mainPanelConstraints.weighty = 0;
		mainPanelConstraints.weightx = 0;
		mainPanelConstraints.insets = new Insets(0,7,0,10);
		
		/**
		 * add the JCheckBox to the Dialog which allows the user to enable/disable displaying on
		 * startup
		 */
		addDisplayOnStartup(mainPanelConstraints,propertiesFile);
		
		mainPanelConstraints.gridy = 3;
		mainPanelConstraints.insets = new Insets(0,0,10,10);
		
		/**
		 * add the navigation buttons at the bottom of the panel which allows the user to move
		 * forwards/backwards through the tips, and also allows the Dialog to be closed.
		 */
		addBottomButtons(mainPanelConstraints);
	}

	private void addDisplayOnStartup(final GridBagConstraints mainPanelConstraints, final PropertiesFile propertiesFile) {
		final String propValue = propertiesFile.getValue("displaytipsonstartup");
		if(!propValue.isEmpty()) {
            showTipsOnStartup.setSelected(propValue.equals("true"));
        }
		showTipsOnStartup.addActionListener(new ActionListener(){
			@Override
            public void actionPerformed(final ActionEvent e) {
				propertiesFile.setValue("displaytipsonstartup", String.valueOf(showTipsOnStartup.isSelected()));
			}
		});
		getContentPane().add(showTipsOnStartup, mainPanelConstraints);
	}

	private void addBottomButtons(final GridBagConstraints mainPanelConstraints) {
		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
		bottomPanel.add(Box.createHorizontalGlue());
		
		final JButton previousTip = new JButton("Previous Tip");
		previousTip.addActionListener(new ActionListener(){
			@Override
            public void actionPerformed(final ActionEvent e) {
				changeTip(-1);
			}
		});
		bottomPanel.add(previousTip);
		
		bottomPanel.add(Box.createRigidArea(new Dimension(5,0)));
		
		final JButton nextTip = new JButton("Next Tip");
		nextTip.addActionListener(new ActionListener(){
			@Override
            public void actionPerformed(final ActionEvent e) {
				changeTip(1);
			}
		});
		nextTip.setPreferredSize(previousTip.getPreferredSize());
		bottomPanel.add(nextTip);
		
		bottomPanel.add(Box.createRigidArea(new Dimension(5,0)));
		
		final JButton close = new JButton("Close");
		close.addActionListener(new ActionListener(){
			@Override
            public void actionPerformed(final ActionEvent e) {
				dispose();
				setVisible(false);
			}
		});
		close.setPreferredSize(previousTip.getPreferredSize());
		
		setFocusTraversalPolicy(new MyFocus(getFocusTraversalPolicy(), close));
		
		close.addKeyListener(new KeyListener() {
			@Override
            public void keyTyped(final KeyEvent event) {}

			@Override
            public void keyPressed(final KeyEvent event) {
				if (event.getKeyCode() == 10) {
					dispose();
					setVisible(false);
				}
			}

			@Override
            public void keyReleased(final KeyEvent event) {}
		});
		
		bottomPanel.add(close);
		
		getContentPane().add(bottomPanel, mainPanelConstraints);
	}
	
	private void changeTip(final int ammount) {
		currentTip += ammount;
		
		/** wrap the current tip if needed */
		if(currentTip == tipPaths.size()) {
            currentTip = 0;
        } else if(currentTip == -1) {
            currentTip = tipPaths.size() - 1;
        }
		
		if(!tipLoadingFailed) {
			try {
				tipPane.setPage(getClass().getResource((String) tipPaths.get(currentTip)));
			} catch (final IOException e) {
				tipLoadingFailed = true;
				//
			}
		}
		
		if (tipLoadingFailed) {
			tipPane.setText("Error displaying tips, no tip to display");
		}
	}

	private void populateTipsList(final String tipRoot, final List items) throws IOException {
		try {
			final URL url = getClass().getResource(tipRoot); //"/org/jpedal/examples/viewer/res/tips"
			
			/**
			 * allow for it in jar
			 */
			if(url.toString().startsWith("jar")){
				final JarURLConnection conn = (JarURLConnection) url.openConnection();
				final JarFile jar = conn.getJarFile();
	
				for (final Enumeration e = jar.entries(); e.hasMoreElements();) {
					final JarEntry entry = (JarEntry) e.nextElement();
					final String name=entry.getName();
					
					if ((!entry.isDirectory()) && name.contains("/res/tips/") && name.endsWith(".html")) { // this
						items.add('/' + name);
					}
				}
			}else{ //IDE
				final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				
				String inputLine;
				
				while ((inputLine = in.readLine()) != null) {
					if (inputLine.indexOf('.') == -1) { // this is a directory
						populateTipsList(tipRoot + '/' + inputLine, items);
					} else if ((inputLine.endsWith(".htm")) || inputLine.endsWith(".html")) { // this is a file
						items.add(tipRoot + '/' + inputLine);
					}
				}
			
			
				in.close();
			}
		} catch (final IOException e) {
			//
			throw e;
		}
	}
	
	private void addCenterTip(final GridBagConstraints mainPanelConstraints) {
		tipPane.setEditable(false);
		tipPane.setAutoscrolls(true);
		
		tipPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
            public void hyperlinkUpdate(final HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				
                        try {
                            BrowserLauncher.openURL(e.getURL().toExternalForm());
                        } catch (final Exception ex) {
                            Logger.getLogger(TipOfTheDay.class.getName()).log(Level.SEVERE, null, ex);
                        }
				}
			}
		});
		
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(tipPane);
		scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		getContentPane().add(scrollPane, mainPanelConstraints);
		
		changeTip(0);
	}

	private void addTopPanel(final GridBagConstraints mainPanelConstraints) {
		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

		final JLabel tipImage = new JLabel(new ImageIcon(getClass().getResource("/org/jpedal/examples/viewer/res/tip.png")));
		topPanel.add(tipImage);
		
		final JLabel label = new JLabel("Did you know ... ?");
		final Font font = label.getFont().deriveFont(16.0f);
	    label.setFont(font);
	    
	    topPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	    
		topPanel.add(label);
		getContentPane().add(topPanel, mainPanelConstraints);
	}
	
    static class MyFocus extends FocusTraversalPolicy {
        final FocusTraversalPolicy original;
        final JButton close;

        MyFocus(final FocusTraversalPolicy original, final JButton close){
            this.original = original;
            this.close = close;

        }
 
        @Override
        public Component getComponentAfter(final Container arg0, final Component arg1) {
            return original.getComponentAfter(arg0, arg1);
        }
        
        @Override
        public Component getComponentBefore(final Container arg0, final Component arg1) {
            return original.getComponentBefore(arg0, arg1);
        }
        
        @Override
        public Component getFirstComponent(final Container arg0) {
            return original.getFirstComponent(arg0);
        }
        
        @Override
        public Component getLastComponent(final Container arg0) {
            return original.getLastComponent(arg0);
        }
        
        @Override
        public Component getDefaultComponent(final Container arg0) {
            return close;
        }
    }
}