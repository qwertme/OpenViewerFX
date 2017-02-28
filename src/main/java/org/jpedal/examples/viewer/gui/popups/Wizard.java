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
 * Wizard.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.popups;

/**
 * Creates a wizard dialog with next, back, finish and cancel buttons.
 * In order to use you must implement a WizardPanelModel which gives the 
 * Wizard the panels it must contain and controls the flow of the panels,
 * such as whether the advance button can be used and what the next panel
 * to be shown is.
 */
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/* Create a Wizard dialog box */
public class Wizard 
{ 

	private static final String BACK_TEXT = "< Back";
	private static final String NEXT_TEXT = "Next >";
	private static final String CANCEL_TEXT = "Cancel";
	private static final String FINISH_TEXT = "Finish";

	private final JDialog wizardDialog;
	private final WizardPanelModel panelManager;

	private JPanel cardPanel;
	private CardLayout cardLayout;

	private JButton backButton;
	private JButton advanceButton;
	private JButton cancelButton;

	private int returnCode = JOptionPane.CANCEL_OPTION;

	/**
	 * Create a Wizard dialog box using the panels
	 * given in the given WizardPanelModel.
	 * 
	 * @param owner Parent frame
	 * @param panelManager Implements the WizardPanelModel interface, therefore containing all the panels and logic.
	 */
	public Wizard(final Frame owner, final WizardPanelModel panelManager)
	{
		wizardDialog = new JDialog(owner);  
        this.panelManager = panelManager;
		initComponents();
	}

	private void initComponents()
	{

		final JPanel buttonPanel = new JPanel();
		final Box buttonBox = new Box(BoxLayout.X_AXIS);
		wizardDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       
        cardLayout = new CardLayout(); 
        cardPanel.setLayout(cardLayout);
        
        final Map panels = panelManager.getJPanels();
        final Set keys = panels.keySet();

        for (final Object key1 : keys) {
            final String key = (String) key1;
            cardPanel.add(key, (JPanel) panels.get(key));
        }
		
		backButton = new JButton(BACK_TEXT);
		advanceButton = new JButton(NEXT_TEXT);
		cancelButton = new JButton(CANCEL_TEXT);

		backButton.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				previousPanel();
			}
		});
		
		advanceButton.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
	            nextPanel();
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e ){
		        returnCode = JOptionPane.CANCEL_OPTION;
		        panelManager.close();
		        wizardDialog.dispose();
			}
		});

		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

		buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10))); 
		buttonBox.add(backButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(advanceButton);
		buttonBox.add(Box.createHorizontalStrut(30));
		buttonBox.add(cancelButton);
		buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
		wizardDialog.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
		wizardDialog.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);
		cardLayout.show(cardPanel, panelManager.getStartPanelID());
		setBackButtonEnabled(panelManager.hasPrevious());
		setNextButtonEnabled(panelManager.canAdvance());	
		
		panelManager.registerNextChangeListeners(new buttonNextState());
		panelManager.registerNextKeyListeners(new textboxPressState());
	}
	
	private void setBackButtonEnabled(final boolean b)
	{
	    backButton.setEnabled(b);
	}
	
	private void setNextButtonEnabled(final boolean b)
	{
	    advanceButton.setEnabled(b);
	}
	
	private void nextPanel()
	{
		if(advanceButton.getText().equals(FINISH_TEXT)) {
			panelManager.close();
		    returnCode = JOptionPane.OK_OPTION; 
		    wizardDialog.dispose();
		}
		else {
			cardLayout.show(cardPanel, panelManager.next());
			setBackButtonEnabled(panelManager.hasPrevious()); 
			setNextButtonEnabled(panelManager.canAdvance());
			if (panelManager.isFinishPanel()) {
				advanceButton.setText(FINISH_TEXT);
			}
		}
	}
	
	private void previousPanel()
	{
		if (panelManager.isFinishPanel()) {
			advanceButton.setText(NEXT_TEXT);
		}
		cardLayout.show(cardPanel, panelManager.previous());
		setBackButtonEnabled(panelManager.hasPrevious());
		setNextButtonEnabled(panelManager.canAdvance());
	}
		
	/**
	 * Display a modal wizard dialog in the middle of the screen.
	 * 
	 * @return The return code is either JOptionPane.CANCEL_OPTION or JOPtionPane.OK_OPTION
	 */
	public int showModalDialog()
	{
		wizardDialog.setModal(true);
		wizardDialog.pack();
		wizardDialog.setLocationRelativeTo(null);
		wizardDialog.setVisible(true);
		
		return returnCode;
	}	

	private class buttonNextState implements ChangeListener
	{

		@Override
        public void stateChanged(final ChangeEvent e) {
			setNextButtonEnabled(panelManager.canAdvance());
		}
	}
	
	private class textboxPressState implements KeyListener
	{	
		@Override
        public void keyReleased(final KeyEvent e) {
			
		}
		
		@Override
        public void keyPressed(final KeyEvent e) {
			
		}
		
		@Override
        public void keyTyped(final KeyEvent e) {
			setNextButtonEnabled(panelManager.canAdvance());
		}
	}
}
