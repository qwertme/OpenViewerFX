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
 * EncryptPDFDocument.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import javax.swing.JTextField;
import javax.swing.JToggleButton;

@SuppressWarnings("UnusedDeclaration")
public class EncryptPDFDocument extends Save
{

    final JToggleButton jToggleButton3 = new JToggleButton();
	
	final JToggleButton jToggleButton2 = new JToggleButton();
	
	final JCheckBox userPasswordCheck=new JCheckBox("Password required to open document");
	final JCheckBox masterPasswordCheck=new JCheckBox("Password required to chnge permissions and passwords");
	
	final JCheckBox printing = new JCheckBox("No Printing");
	final JCheckBox modifyDocument = new JCheckBox("No modifying the document");
	final JCheckBox contentExtract = new JCheckBox("No content copying or extraction");
	final JCheckBox modifyAnnotations = new JCheckBox("No modifying annotations");
	final JCheckBox formFillIn = new JCheckBox("No form fields fill-in");
	
	final JTextField userPasswordBox = new JTextField();
	final JTextField masterPasswordBox = new JTextField();
	
	final String[] securityItems = {"128-bit RC4 (Acrobat 5.0 and up)",
			"40-bit RC4 (Acrobat 3.x, 4.x)"
	};
	
	final JComboBox encryptionLevel = new JComboBox(securityItems);
	
	public EncryptPDFDocument( final String root_dir, final int end_page, final int currentPage )
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
	
	private void jbInit() throws Exception
	{
		
		pageRangeLabel.setText( "Password" );
		pageRangeLabel.setBounds( new Rectangle( 13, 13, 199, 26 ) );

		userPasswordCheck.setBounds( new Rectangle( 23, 40, 300, 22 ) );
		userPasswordCheck.addMouseListener(new MouseListener(){
			@Override
            public void mouseClicked(final MouseEvent e) {
				userPasswordBox.setEditable(userPasswordCheck.isSelected());
			}

			@Override
            public void mouseEntered(final MouseEvent e) {}
			@Override
            public void mouseExited(final MouseEvent e) {}
			@Override
            public void mousePressed(final MouseEvent e) {}
			@Override
            public void mouseReleased(final MouseEvent e) {}
		});
		
		final JLabel userPasswordLabel = new JLabel("User password:");
		userPasswordLabel.setBounds( new Rectangle( 50, 70, 100, 22 ) );
		userPasswordBox.setBounds( new Rectangle( 180, 70, 150, 22 ) );
		userPasswordBox.setEditable(false);
		
		masterPasswordCheck.setBounds( new Rectangle( 23, 100, 440, 22 ) );
		masterPasswordCheck.addMouseListener(new MouseListener(){
			@Override
            public void mouseClicked(final MouseEvent e) {
				masterPasswordBox.setEditable(masterPasswordCheck.isSelected());
			}

			@Override
            public void mouseEntered(final MouseEvent e) {}
			@Override
            public void mouseExited(final MouseEvent e) {}
			@Override
            public void mousePressed(final MouseEvent e) {}
			@Override
            public void mouseReleased(final MouseEvent e) {}
		});
		
		final JLabel masterPasswordLabel = new JLabel("Master password:");
		masterPasswordLabel.setBounds( new Rectangle( 50, 130, 120, 22 ) );
		masterPasswordBox.setBounds( new Rectangle( 180, 130, 150, 22 ) );
		masterPasswordBox.setEditable(false);
		
		final JLabel permissionsLabel = new JLabel("Permissions");
		permissionsLabel.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		permissionsLabel.setDisplayedMnemonic( '0' );
		permissionsLabel.setBounds( new Rectangle( 13, 180, 199, 26 ) );
		
		final JLabel encryptionLevelLabel= new JLabel("Encryption Level:");
		encryptionLevelLabel.setBounds( new Rectangle( 23, 210, 125, 22 ) );
		
		encryptionLevel.setBounds( new Rectangle( 150, 210, 250, 22 ) );
		
		printing.setBounds( new Rectangle( 23, 250, 200, 22 ) );
		modifyDocument.setBounds( new Rectangle( 23, 280, 200, 22 ) );
		contentExtract.setBounds( new Rectangle( 23, 310, 220, 22 ) );
		modifyAnnotations.setBounds( new Rectangle( 23, 340, 200, 22 ) );
		formFillIn.setBounds( new Rectangle( 23, 370, 200, 22 ) );		
		
		this.add( changeButton, null );
		this.add( pageRangeLabel, null );
		
		this.add( userPasswordCheck, null );
		this.add( masterPasswordCheck, null );
		
		this.add(userPasswordLabel);
		this.add( userPasswordBox, null );
		
		this.add(masterPasswordLabel);
		this.add( masterPasswordBox, null );
		
		this.add(permissionsLabel, null);
		this.add(encryptionLevelLabel,null);
		this.add(encryptionLevel,null);
		
		this.add(printing);
		this.add(modifyDocument);
		this.add(contentExtract);
		this.add(modifyAnnotations);
		this.add(formFillIn);
		
		this.add( jToggleButton2, null );
		this.add( jToggleButton3, null );
		
		

		
	}
	
	@Override
    public final Dimension getPreferredSize()
	{
		return new Dimension( 420, 400 );
	}
	
	public String getPermissions(){
		StringBuilder permissions = new StringBuilder("");
		
		if(printing.isSelected()) {
            permissions.append('1');
        } else {
            permissions.append('0');
        }
		
		if(modifyDocument.isSelected()) {
            permissions.append('1');
        } else {
            permissions.append('0');
        }
		
		if(contentExtract.isSelected()) {
            permissions.append('1');
        } else {
            permissions.append('0');
        }
		
		if(modifyAnnotations.isSelected()) {
            permissions.append('1');
        } else {
            permissions.append('0');
        }
		
		if(formFillIn.isSelected()) {
            permissions.append('1');
        } else {
            permissions.append('0');
        }

		return permissions.toString();
	}
	
	public int getEncryptionLevel() {
		return encryptionLevel.getSelectedIndex();
	}
	
	public String getMasterPassword() {
		return masterPasswordBox.getText();
	}

	public String getUserPassword() {
		return userPasswordBox.getText();
	}
	
}
