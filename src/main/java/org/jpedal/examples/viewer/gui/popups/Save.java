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
 * Save.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.utils.FileFilterer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**allow user to select page range and values to save*/
public class Save extends JComponent{
	
	protected final JTextField startPage = new JTextField();
	protected final JTextField endPage = new JTextField();
	protected final JTextField rootDir = new JTextField();
	
	protected final Object[] scales={"10","25","50","75","100"};
	protected final JComboBox scaling = new JComboBox(scales);
	
	protected final JLabel scalingLabel = new JLabel();
	protected final JLabel rootFilesLabel = new JLabel();
	
	protected final JButton changeButton = new JButton();
	
	protected final JLabel endLabel = new JLabel();
	protected final JLabel startLabel = new JLabel();
	protected final JLabel pageRangeLabel = new JLabel();
	
	protected final String root_dir;
	protected final int end_page;
	protected final int currentPage;
	
	protected final JLabel optionsForFilesLabel = new JLabel();
	
	public Save(final String root_dir, final int end_page, final int currentPage){
		
		this.currentPage=currentPage;
		this.root_dir = root_dir;
		this.end_page = end_page;
		
		scalingLabel.setFont( new java.awt.Font( "Dialog", Font.BOLD , 14 ) );
		scalingLabel.setText( Messages.getMessage("PdfViewerOption.Scaling")+ '\n');
		scaling.setSelectedItem("100");
		scaling.setName("exportScaling");
		
		rootFilesLabel.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		rootFilesLabel.setDisplayedMnemonic( '0' );
		rootFilesLabel.setText( Messages.getMessage("PdfViewerOption.RootDir"));
		rootDir.setText( root_dir );
		rootDir.setName("extRootDir");
		
		changeButton.setText( Messages.getMessage("PdfViewerOption.Browse"));
		changeButton.addActionListener( new ActionListener() {
			@Override
            public void actionPerformed( final ActionEvent e ){
				final JFileChooser chooser = new JFileChooser(root_dir);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				final String[] png = { "png","tif","tiff","jpg","jpeg" }; //$NON-NLS-1$
				chooser.addChoosableFileFilter(new FileFilterer(png, "Images (Tiff, Jpeg,Png)")); //$NON-NLS-1$
				chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				final int state = chooser.showOpenDialog(null);
				
				final File file = chooser.getSelectedFile();
				
				if (file != null && state == JFileChooser.APPROVE_OPTION) {
					rootDir.setText(file.getAbsolutePath());
				}
			}
		} );
		
		optionsForFilesLabel.setText( Messages.getMessage("PdfViewerOption.Output" ));
		optionsForFilesLabel.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		optionsForFilesLabel.setDisplayedMnemonic( '0' );
		
		pageRangeLabel.setText( Messages.getMessage("PdfViewerOption.PageRange" ));
		pageRangeLabel.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		pageRangeLabel.setDisplayedMnemonic( '0' );
		
		startLabel.setText( Messages.getMessage("PdfViewerOption.StartPage" ));
		endLabel.setText( Messages.getMessage("PdfViewerOption.EndPage" ) );
		
		startPage.setText( "1" );
		endPage.setText(String.valueOf(end_page));
		
	}
		
	/**
	 * get scaling value
	 */
    public final int getScaling(){
		return Integer.parseInt((String) scaling.getSelectedItem());
	}
	
	/**popup display for user to make selection*/
	public int display(final Component c, final String title) {
		
		setSize(400, 200);
		final JPanel popupPanel = new JPanel();
		popupPanel.setLayout(new BorderLayout());
		popupPanel.add(this, BorderLayout.CENTER);
		popupPanel.setSize(400, 200);
		final Object[] options = { Messages.getMessage("PdfMessage.Ok"), Messages.getMessage("PdfMessage.Cancel") };

        if(GUI.showMessages) {
            return JOptionPane.showOptionDialog(
                    c,
                    popupPanel, title,

                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);
        } else {
            return 0;
        }

	}
	
	/**
	 * get start page
	 */
    public final int getStartPage(){
		
		int page = -1;
		
		try{
			page = Integer.parseInt( startPage.getText() );
		}catch( final Exception e ){
			LogWriter.writeLog( "Exception " + e + " in exporting" );
            if(GUI.showMessages) {
                JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerError.InvalidSyntax"));
            }
		}
		
		if((page < 1) && (GUI.showMessages) ){
                JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerError.NegativePageValue"));
            }

		if(page > end_page){
            if(GUI.showMessages) {
                JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerText.Page") + ' '
                        + page + ' ' + Messages.getMessage("PdfViewerError.OutOfBounds") + ' ' +
                        Messages.getMessage("PdfViewerText.PageCount") + ' ' + end_page);
            }
			
			page = -1;
		}
		
		return page;
	}
	
	/**
	 * get root dir
	 */
    public final String getRootDir(){
		return this.rootDir.getText();
	}
	
	/**
	 * get end page
	 */
    public final int getEndPage(){
		
		int page = -1;
		try{
			page = Integer.parseInt( endPage.getText() );
		}catch( final Exception e ){
			LogWriter.writeLog( "Exception " + e + " in exporting" );

            if(GUI.showMessages) {
                JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerError.InvalidSyntax"));
            }
		}
		
		if((page < 1) && (GUI.showMessages)){
                JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerError.NegativePageValue"));
            }
        
		if(page > end_page){
            if(GUI.showMessages) {
                JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerText.Page") + ' '
                        + page + ' ' + Messages.getMessage("PdfViewerError.OutOfBounds") + ' ' +
                        Messages.getMessage("PdfViewerText.PageCount") + ' ' + end_page);
            }
			
			page = -1;
		}
		return page;
	}
	
	/**
	 * size
	 */
    @Override
    public final Dimension getSize(){
		return getPreferredSize();
	}
	
	/**
	 * size
	 */
	@Override
    public Dimension getPreferredSize(){
		return new Dimension( 400, 330 );
	}
	
	/**
	 * size
	 */
    @Override
    public final Dimension getMinimumSize(){
		return getPreferredSize();
	}
	
	/**
	 * size
	 */
    @Override
    public final Dimension getMaximumSize(){
		return getPreferredSize();
	}
	
}
