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
 * AddHeaderFooterToPDFPages.java
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

@SuppressWarnings("UnusedDeclaration")
public class AddHeaderFooterToPDFPages extends Save
{

	private static final long serialVersionUID = -8681143216306570454L;
	
	JLabel OutputLabel = new JLabel();
	final ButtonGroup buttonGroup1 = new ButtonGroup();
	ButtonGroup buttonGroup2 = new ButtonGroup();
	
	final JToggleButton jToggleButton3 = new JToggleButton();
	
	final JToggleButton jToggleButton2 = new JToggleButton();
	
	final JRadioButton printAll=new JRadioButton();
	final JRadioButton printCurrent=new JRadioButton();
	final JRadioButton printPages=new JRadioButton();
	
	final JTextField pagesBox=new JTextField();
	
	final JTextField leftHeaderBox=new JTextField();
	final JTextField centerHeaderBox=new JTextField();
	final JTextField rightHeaderBox=new JTextField();
	final JTextField leftFooterBox=new JTextField();
	final JTextField centerFooterBox=new JTextField();
	final JTextField rightFooterBox=new JTextField();
	
	final JComboBox<String> fontsList = new JComboBox<String>(new String[] { "Courier",
			"Courier-Bold", "Courier-Oblique", "Courier-BoldOblique",
			"Helvetica", "Helvetica-Bold", "Helvetica-BoldOblique",
			"Helvetica-Oblique", "Times-Roman", "Times-Bold", "Times-Italic",
			"Times-BoldItalic", "Symbol", "ZapfDingbats"
	});
	
	final JSpinner fontSize = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
	
	final JLabel colorBox = new JLabel();
	
	final JSpinner leftRightBox = new JSpinner(new SpinnerNumberModel(36.00, 1.00, 1000.00, 1));
	final JSpinner topBottomBox = new JSpinner(new SpinnerNumberModel(36.00, 1.00, 1000.00, 1));
	
	final JTextArea tagsList = new JTextArea();
	
	
	
	public AddHeaderFooterToPDFPages( final String root_dir, final int end_page, final int currentPage )
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
                            JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerText.Page") +
                                    ' ' + i + ' ' + Messages.getMessage("PdfViewerError.OutOfBounds") + ' ' +
                                    Messages.getMessage("PdfViewerText.PageCount") + ' ' + end_page);
                        }
						return null;
					}
					pagesToExport[count]=i;
					count++;
				}
			}catch (final IllegalArgumentException  e) {
				LogWriter.writeLog(Messages.getMessage("PdfViewerError.Exception")
						+ ' ' + e + ' ' +Messages.getMessage("PdfViewerError.ExportPdfError"));
                if(GUI.showMessages) {
                    JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerError.InvalidSyntax"));
                }
			}
		}
		
		return pagesToExport;

	}	
	
	public float getLeftRightMargin(){
		return Float.parseFloat(leftRightBox.getValue().toString());
	}
	
	public float getTopBottomMargin(){
		return Float.parseFloat(topBottomBox.getValue().toString());
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

	
	public String getLeftHeader(){
		return leftHeaderBox.getText();
	}
	
	public String getCenterHeader(){
		return centerHeaderBox.getText();
	}
	
	public String getRightHeader(){
		return rightHeaderBox.getText();
	}
	
	public String getLeftFooter(){
		return leftFooterBox.getText();
	}
	
	public String getCenterFooter(){
		return centerFooterBox.getText();
	}
	
	public String getRightFooter(){
		return rightFooterBox.getText();
	}
	
	private void jbInit() throws Exception{
		
		final JLabel textAndFont = new JLabel(Messages.getMessage("PdfViewerLabel.TextAndFont"));
		textAndFont.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		textAndFont.setDisplayedMnemonic( '0' );
		textAndFont.setBounds( new Rectangle( 13, 13, 220, 26 ) );
		
		final JLabel left = new JLabel(Messages.getMessage("PdfViewerLabel.Left"));
		left.setBounds( new Rectangle( 130, 40, 50, 23 ) );
		
		final JLabel center = new JLabel(Messages.getMessage("PdfViewerLabel.Center"));
		center.setBounds( new Rectangle( 300, 40, 50, 23 ) );
		
		final JLabel right = new JLabel(Messages.getMessage("PdfViewerLabel.Right"));
		right.setBounds( new Rectangle( 475, 40, 50, 23 ) );
		
		final JLabel header = new JLabel(Messages.getMessage("PdfViewerLabel.Header"));
		header.setBounds( new Rectangle( 20, 60, 90, 23 ) );
		
		final JLabel footer = new JLabel(Messages.getMessage("PdfViewerLabel.Footer"));
		footer.setBounds( new Rectangle( 20, 90, 50, 23 ) );
		
		leftHeaderBox.setBounds( new Rectangle(85, 60, 133, 23 ) );
		centerHeaderBox.setBounds( new Rectangle(250, 60, 133, 23 ) );
		rightHeaderBox.setBounds( new Rectangle(425, 60, 133, 23 ) );
		
		leftFooterBox.setBounds( new Rectangle(85, 90, 133, 23 ) );
		centerFooterBox.setBounds( new Rectangle(250, 90, 133, 23 ) );
		rightFooterBox.setBounds( new Rectangle(425, 90, 133, 23 ) );

		final JLabel font = new JLabel(Messages.getMessage("PdfViewerLabel.Font"));
		font.setBounds( new Rectangle( 20, 120, 75, 23 ) );
		
		fontsList.setBounds( new Rectangle( 85, 120, 150, 23 ) );
		fontsList.setSelectedItem("Helvetica");
		
		final JLabel size = new JLabel(Messages.getMessage("PdfViewerLabel.Size"));
		size.setBounds( new Rectangle( 250, 120, 50, 23 ) );
		
		fontSize.setBounds( new Rectangle( 290, 120, 50, 23 ) );
		
		final JLabel color = new JLabel(Messages.getMessage("PdfViewerLabel.Color"));
		color.setBounds( new Rectangle( 360, 120, 50, 23 ) );
		
		colorBox.setBackground(Color.black);
		colorBox.setOpaque(true);
		colorBox.setBounds( new Rectangle( 410, 120, 23, 23 ) );
		
		final JButton chooseColor = new JButton(Messages.getMessage("PdfViewerButton.ChooseColor"));
		chooseColor.setBounds( new Rectangle( 450, 120, 160, 23 ) );
		chooseColor.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				colorBox.setBackground(JColorChooser.showDialog(null, "Color",colorBox.getBackground()));
			}
		});
		
        tagsList.setText("You may use the following\n" +
        		"tags as part of the text.\n\n" +
        		"<d> - Date in short format\n" +
        		"<D> - Date in long format\n" +
        		"<t> - Time in 12-hour format\n" +
        		"<T> - Time in 24-hour format\n" +
        		"<f> - Filename\n" +
        		"<F> - Full path filename\n" +
        		"<p> - Current page number\n" +
        		"<P> - Total number of pages");
        tagsList.setOpaque(false);
        tagsList.setBounds(350, 160, 200, 210);

		final JLabel margins = new JLabel(Messages.getMessage("PdfViewerLabel.Margins"));

		margins.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		margins.setDisplayedMnemonic( '0' );
		margins.setBounds( new Rectangle( 13, 150, 220, 26 ) );
		
		final JLabel leftRight = new JLabel(Messages.getMessage("PdfViewerLabel.LeftAndRight"));
		leftRight.setBounds( new Rectangle( 20, 185, 90, 23 ) );
		
		leftRightBox.setBounds( new Rectangle(100, 185, 70, 23 ) );
		
		final JLabel topBottom = new JLabel(Messages.getMessage("PdfViewerLabel.TopAndBottom"));
		topBottom.setBounds( new Rectangle( 180, 185, 120, 23 ) );
		
		topBottomBox.setBounds( new Rectangle(300, 185, 70, 23 ) );
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerPageRange.text"));
		pageRangeLabel.setBounds( new Rectangle( 13, 220, 400, 26 ) );
		
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

		final JTextArea pagesInfo=new JTextArea(Messages.getMessage("PdfViewerMessage.PageNumberOrRangeLong"));
		pagesInfo.setBounds(new Rectangle(23,320,620,40));
		pagesInfo.setOpaque(false);
				
		
		this.add( printAll, null );
		this.add( printCurrent, null );
		
		this.add( printPages, null );
		this.add( pagesBox, null );
		this.add( pagesInfo, null );
		
		this.add( left, null );
		this.add( center, null );
		this.add( right, null );
		this.add( header, null );
		this.add( footer, null );
		this.add( leftHeaderBox, null );
		this.add( centerHeaderBox, null );
		this.add( rightHeaderBox, null );
		this.add( leftFooterBox, null );
		this.add( centerFooterBox, null );
		this.add( rightFooterBox, null );
		this.add( font, null );
		this.add( fontsList, null );
		this.add( size, null );
		this.add( fontSize, null );
		this.add( color, null );
		this.add( colorBox, null );
		this.add( chooseColor, null );
		this.add( margins, null );
		this.add( leftRight, null );
		this.add( leftRightBox, null );
		this.add( topBottom, null );
		this.add( topBottomBox, null );
		
		this.add( textAndFont, null );
		this.add( changeButton, null );
		this.add( pageRangeLabel, null );
		
		//this.add(tagsList, null);
		
		this.add( jToggleButton2, null );
		this.add( jToggleButton3, null );
		
		buttonGroup1.add( printAll );
		buttonGroup1.add( printCurrent );
		buttonGroup1.add( printPages );
	}
	
	@Override
    public final Dimension getPreferredSize()
	{
		return new Dimension( 620, 350 );
	}
	
}
