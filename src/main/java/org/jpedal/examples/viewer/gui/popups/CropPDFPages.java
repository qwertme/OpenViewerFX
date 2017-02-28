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
 * CropPDFPages.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.jpedal.utils.Messages;

public class CropPDFPages extends Save
{

    final ButtonGroup buttonGroup1 = new ButtonGroup();

    final JToggleButton jToggleButton3 = new JToggleButton();
	
	final JToggleButton jToggleButton2 = new JToggleButton();
	
	final JSpinner bottomMargin = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 1000.00, 1.00));
	final JSpinner topMargin = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 1000.00, 1.00));
	final JSpinner leftMargin = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 1000.00, 1.00));
	final JSpinner rightMargin = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 1000.00, 1.00));
	
	final JCheckBox applyToCurrent = new JCheckBox();
	
	final JRadioButton printAll=new JRadioButton();
	final JRadioButton printCurrent=new JRadioButton();
	final JRadioButton printPages=new JRadioButton();
	
	final JTextField pagesBox=new JTextField();
	
	public CropPDFPages( final String root_dir, final int end_page, final int currentPage )
	{
		super(root_dir, end_page, currentPage);
			
		try
		{
			jbInit();
		}
		catch( final Exception e )
		{
			e.printStackTrace();
		}
	}

	///////////////////////////////////////////////////////////////////////

    private void jbInit() throws Exception{
		
		final JLabel textAndFont = new JLabel(Messages.getMessage("PdfViewerLabel.CropMargins"));
		textAndFont.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		textAndFont.setDisplayedMnemonic( '0' );
		textAndFont.setBounds( new Rectangle( 13, 13, 220, 26 ) );
		
        final JLabel jLabel1 = new JLabel(Messages.getMessage("PdfViewerLabel.Top"));
        jLabel1.setBounds(140, 50, 70, 15);

        topMargin.setBounds(200, 45, 60, 23);

        final JLabel jLabel5 = new javax.swing.JLabel(Messages.getMessage("PdfViewerLabel.Left"));
        jLabel5.setBounds(25, 100, 50, 15);

        leftMargin.setBounds(70, 95, 60, 23);
        
        final JLabel jLabel6 = new javax.swing.JLabel(Messages.getMessage("PdfViewerLabel.Right"));
        jLabel6.setBounds(295, 100, 70, 15);

        rightMargin.setBounds(340, 95, 60, 23);

        final JLabel jLabel7 = new javax.swing.JLabel(Messages.getMessage("PdfViewerLabel.Bottom"));
        jLabel7.setBounds(140, 150, 110, 15);
        
        bottomMargin.setBounds(200, 145, 60, 23);

        applyToCurrent.setSelected(true);
        applyToCurrent.setText(Messages.getMessage("PdfViewerCheckBox.ApplyToPriorCroppingRectangle"));
        applyToCurrent.setBounds(5, 190, 305, 15);

        final JButton jButton1 = new javax.swing.JButton(Messages.getMessage("PdfViewerButton.Set2Zero"));
        jButton1.setBounds(310, 185, 130, 23);
		jButton1.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				leftMargin.setValue(0);
				rightMargin.setValue(0);
				topMargin.setValue(0);
				bottomMargin.setValue(0);
			}
		});
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerPageRange.text"));
		pageRangeLabel.setBounds( new Rectangle( 13, 220, 199, 26 ) );
		
		printAll.setText(Messages.getMessage("PdfViewerRadioButton.All"));
		printAll.setBounds( new Rectangle( 23, 250, 75, 22 ) );
		
		printCurrent.setText(Messages.getMessage("PdfViewerRadioButton.CurrentPage"));
		printCurrent.setBounds( new Rectangle( 23, 270, 100, 22 ) );
		printCurrent.setSelected(true);
		
		printPages.setText(Messages.getMessage("PdfViewerRadioButton.Pages"));
		printPages.setBounds( new Rectangle( 23, 292, 70, 22 ) );
		
		pagesBox.setBounds( new Rectangle( 95, 292, 230, 22 ) );
		pagesBox.addKeyListener(new KeyListener(){
			@Override
            public void keyPressed(final KeyEvent arg0) {}

			@Override
            public void keyReleased(final KeyEvent arg0) {
				if(pagesBox.getText().isEmpty()) {
                    printCurrent.setSelected(true);
                } else {
                    printPages.setSelected(true);
                }
				
			}

			@Override
            public void keyTyped(final KeyEvent arg0) {}
		});

		final JTextArea pagesInfo=new JTextArea(Messages.getMessage("PdfViewerMessage.PageNumberOrRange")+ '\n' +
				Messages.getMessage("PdfViewerMessage.PageRangeExample"));
		pagesInfo.setBounds(new Rectangle(23,325,400,40));
		pagesInfo.setOpaque(false);
				
		this.add(jLabel1);
        this.add(bottomMargin);
        this.add(jLabel5);
        this.add(topMargin);
        this.add(leftMargin);
        this.add(rightMargin);
        this.add(jLabel7);
        this.add(jLabel6);
        this.add(applyToCurrent);
        this.add(jButton1);		
		
		this.add( printAll, null );
		this.add( printCurrent, null );
		
		this.add( printPages, null );
		this.add( pagesBox, null );
		this.add( pagesInfo, null );
		
		this.add( textAndFont, null );
		this.add( changeButton, null );
		this.add( pageRangeLabel, null );
		
		this.add( jToggleButton2, null );
		this.add( jToggleButton3, null );
		
		buttonGroup1.add( printAll );
		buttonGroup1.add( printCurrent );
		buttonGroup1.add( printPages );
	}
	
	@Override
    public final Dimension getPreferredSize()
	{
		return new Dimension( 440, 400 );
	}
	
}
