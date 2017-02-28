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
 * StampTextToPDFPages.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.print.attribute.standard.PageRanges;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.jpedal.examples.viewer.gui.GUI;


public class StampTextToPDFPages extends Save
{

	JLabel OutputLabel = new JLabel();
	final ButtonGroup buttonGroup1 = new ButtonGroup();
	ButtonGroup buttonGroup2 = new ButtonGroup();
	
	final JToggleButton jToggleButton3 = new JToggleButton();
	
	final JToggleButton jToggleButton2 = new JToggleButton();
	
	final JRadioButton printAll=new JRadioButton();
	final JRadioButton printCurrent=new JRadioButton();
	final JRadioButton printPages=new JRadioButton();
	
	final JTextField pagesBox=new JTextField();
	
	final JTextField textBox=new JTextField();
	
	final JSpinner rotationBox = new JSpinner(new SpinnerNumberModel(0, 0, 360, 1));
	
	final JComboBox placementBox = new JComboBox(new String[] {Messages.getMessage("PdfViewerLabel.Overlay"),
			Messages.getMessage("PdfViewerLabel.Underlay")});
	
	final JComboBox fontsList = new JComboBox(new String[] { "Courier",
			"Courier-Bold", "Courier-Oblique", "Courier-BoldOblique",
			"Helvetica", "Helvetica-Bold", "Helvetica-BoldOblique",
			"Helvetica-Oblique", "Times-Roman", "Times-Bold", "Times-Italic",
			"Times-BoldItalic", "Symbol", "ZapfDingbats"
	});
	
	final JSpinner fontSize = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
	
	final JLabel colorBox = new JLabel();
	
	final JComboBox horizontalBox = new JComboBox(new String[]{Messages.getMessage("PdfViewerLabel.FromLeft"),
			Messages.getMessage("PdfViewerLabel.Centered"),Messages.getMessage("PdfViewerLabel.FromRight")
	});
	
	final JComboBox verticalBox = new JComboBox(new String[]{Messages.getMessage("PdfViewerLabel.FromTop"),
			Messages.getMessage("PdfViewerLabel.Centered"),Messages.getMessage("PdfViewerLabel.FromBottom")
	});
	
	final JSpinner horizontalOffset = new JSpinner(new SpinnerNumberModel(0.00, -1000.00, 1000.00, 1));
	final JSpinner verticalOffset = new JSpinner(new SpinnerNumberModel(0.00, -1000.00, 1000.00, 1));
	
	public StampTextToPDFPages( final String root_dir, final int end_page, final int currentPage )
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
	/**
	 * get root dir
	 */
    public final int[] getPages()
	{
		
		int[] pagesToExport=null;
		
		if(printAll.isSelected()){
			pagesToExport=new int[end_page];
			for(int i=0;i<end_page;i++) {
                pagesToExport[i] = i + 1;
            }

		}else if( printCurrent.isSelected() ){
			pagesToExport=new int[1];
			pagesToExport[0]=currentPage;
			
		}else if( printPages.isSelected() ){
			
			try{
				final PageRanges pages=new PageRanges(pagesBox.getText());
				
				int count=0;
				int i = -1;
				while ((i = pages.next(i)) != -1) {
                    count++;
                }
				
				pagesToExport=new int[count];
				count=0;
				i = -1;
				while ((i = pages.next(i)) != -1){
					if(i > end_page){
                        if(GUI.showMessages) {
                            JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerText.Page") + ' '
                                    + pages + ' ' + Messages.getMessage("PdfViewerError.OutOfBounds") + ' ' +
                                    Messages.getMessage("PdfViewerText.PageCount") + ' ' + end_page);
                        }
						return null;
					}
					pagesToExport[count]=i;
					count++;
				}
			}catch (final IllegalArgumentException  e) {
				LogWriter.writeLog( "Exception " + e + " in exporting pdfs" );
                if(GUI.showMessages) {
                    JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerError.InvalidSyntax"));
                }
			}
		}
		
		return pagesToExport;

	}	
	
	public float getHorizontalOffset(){
		return Float.parseFloat(horizontalOffset.getValue().toString());
	}
	
	public float getVerticalOffset(){
		return Float.parseFloat(verticalOffset.getValue().toString());
	}
	
	public String getHorizontalPosition(){
		return (String) horizontalBox.getSelectedItem();
	}
	
	public String getVerticalPosition(){
		return (String) verticalBox.getSelectedItem();
	}
	
	public int getRotation(){
		return Integer.parseInt(rotationBox.getValue().toString());
	}
	
	public String getPlacement(){
		return (String) placementBox.getSelectedItem();
	}
	
	public String getFontName(){
		return (String) fontsList.getSelectedItem();
	}
	
	public int getFontSize(){
		return Integer.parseInt(fontSize.getValue().toString());
	}
	
	public Color getFontColor(){
		return colorBox.getBackground();
	}

	
	public String getText(){
		return textBox.getText();
	}
	
	
	private void jbInit() throws Exception
	{
		
		final JLabel textAndFont = new JLabel(Messages.getMessage("PdfViewerLabel.TextAndFont"));
		textAndFont.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		textAndFont.setDisplayedMnemonic( '0' );
		textAndFont.setBounds( new Rectangle( 13, 13, 220, 26 ) );
		
		final JLabel text = new JLabel(Messages.getMessage("PdfViewerLabel.Text"));
		text.setBounds( new Rectangle( 20, 40, 50, 23 ) );
		
		textBox.setBounds( new Rectangle(50, 40, 430, 23 ) );

		final JLabel rotation = new JLabel(Messages.getMessage("PdfViewerLabel.Rotation"));
		rotation.setBounds( new Rectangle( 20, 80, 80, 23 ) );
		
		rotationBox.setBounds( new Rectangle( 90, 80, 50, 23 ) );
		
		final JLabel degrees = new JLabel(Messages.getMessage("PdfViewerText.Degrees"));
		degrees.setBounds( new Rectangle( 150, 80, 50, 23 ) );
		
		final JLabel placement = new JLabel(Messages.getMessage("PdfViewerLabel.Placement"));
		placement.setBounds( new Rectangle( 210, 80, 70, 23 ) );
		
		placementBox.setBounds( new Rectangle( 270, 80,90, 23 ) );
		
		final JLabel font = new JLabel(Messages.getMessage("PdfViewerLabel.Font"));
		font.setBounds( new Rectangle( 20, 120, 90, 23 ) );
		
		fontsList.setBounds( new Rectangle( 90, 120, 150, 23 ) );
		fontsList.setSelectedItem("Helvetica");
		
		final JLabel size = new JLabel(Messages.getMessage("PdfViewerLabel.Size"));
		size.setBounds( new Rectangle( 250, 120, 50, 23 ) );
		
		fontSize.setBounds( new Rectangle( 300, 120, 50, 23 ) );
		
		final JLabel color = new JLabel(Messages.getMessage("PdfViewerLabel.Color"));
		color.setBounds( new Rectangle( 360, 120, 50, 23 ) );
		
		colorBox.setBackground(Color.black);
		colorBox.setOpaque(true);
		colorBox.setBounds( new Rectangle( 400, 120, 23, 23 ) );
		
		final JButton chooseColor = new JButton(Messages.getMessage("PdfViewerButton.ChooseColor"));
		chooseColor.setBounds( new Rectangle( 440, 120, 150, 23 ) );
		chooseColor.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				colorBox.setBackground(JColorChooser.showDialog(null, "Color",colorBox.getBackground()));
			}
		});
		
		final JLabel positionAndOffset = new JLabel(Messages.getMessage("PdfViewerLabel.PositionAndOffset"));
		positionAndOffset.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		positionAndOffset.setDisplayedMnemonic( '0' );
		positionAndOffset.setBounds( new Rectangle( 13, 150, 220, 26 ) );
		
		final JLabel horizontal = new JLabel(Messages.getMessage("PdfViewerLabel.Horizontal"));
		horizontal.setBounds( new Rectangle( 20, 185, 90, 23 ) );
		
		horizontalBox.setBounds( new Rectangle(80, 185, 120, 23 ) );
		horizontalBox.setSelectedItem(Messages.getMessage("PdfViewerLabel.Centered"));
		
		final JLabel vertical = new JLabel(Messages.getMessage("PdfViewerLabel.Vertical"));
		vertical.setBounds( new Rectangle( 20, 215, 90, 23 ) );
		
		verticalBox.setBounds( new Rectangle(80, 215, 120, 23 ) );
		verticalBox.setSelectedItem(Messages.getMessage("PdfViewerLabel.Centered"));
		
		final JLabel hOffset = new JLabel(Messages.getMessage("PdfViewerLabel.Offset"));
		hOffset.setBounds( new Rectangle( 250, 185, 90, 23 ) );
		
		horizontalOffset.setBounds( new Rectangle(315, 185, 70, 23 ) );
		
		final JLabel vOffset = new JLabel(Messages.getMessage("PdfViewerLabel.Offset"));
		vOffset.setBounds( new Rectangle( 250, 215, 90, 23 ) );
		
		verticalOffset.setBounds( new Rectangle(315, 215, 70, 23 ) );
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerPageRange.text"));
		pageRangeLabel.setBounds( new Rectangle( 13, 250, 199, 26 ) );
		
		printAll.setText(Messages.getMessage("PdfViewerRadioButton.All"));
		printAll.setBounds( new Rectangle( 23, 280, 75, 22 ) );
		
		printCurrent.setText(Messages.getMessage("PdfViewerRadioButton.CurrentPage"));
		printCurrent.setBounds( new Rectangle( 23, 300, 100, 22 ) );
		printCurrent.setSelected(true);
		
		printPages.setText(Messages.getMessage("PdfViewerRadioButton.Pages"));
		printPages.setBounds( new Rectangle( 23, 322, 70, 22 ) );
		
		pagesBox.setBounds( new Rectangle( 95, 322, 230, 22 ) );
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

		final JTextArea pagesInfo=new JTextArea(Messages.getMessage("PdfViewerMessage.PageNumberOrRangeLong"));
		pagesInfo.setBounds(new Rectangle(23,355,600,40));
		pagesInfo.setOpaque(false);
				
		
		this.add( printAll, null );
		this.add( printCurrent, null );
		
		this.add( printPages, null );
		this.add( pagesBox, null );
		this.add( pagesInfo, null );
		
		this.add( text, null );
		this.add( textBox, null );
		this.add( rotation, null );
		this.add( rotationBox, null );
		this.add( degrees, null );
		this.add( placement, null );
		this.add( placementBox, null );
		
		
		this.add( positionAndOffset, null );
		this.add( horizontal, null );
		this.add( horizontalBox, null );
		this.add( vertical, null );
		this.add( verticalBox, null );
		this.add( hOffset, null );
		this.add( horizontalOffset, null );
		this.add( vOffset, null );
		this.add( verticalOffset, null );
		
		
		
		this.add( font, null );
		this.add( fontsList, null );
		this.add( size, null );
		this.add( fontSize, null );
		this.add( color, null );
		this.add( colorBox, null );
		this.add( chooseColor, null );
		
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
		return new Dimension( 600, 400 );
	}
	
}
