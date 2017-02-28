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
 * ShowGUIMessage.java
 * ---------------
 */
package org.jpedal.gui;

//import of JFC
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * provides a popup message if the library is being run in GUI mode <br>
 * <p>
 * <b>Note </b> these methods are not part of the API and is not guaranteed to
 * be in future versions of JPedal.
 * </p>
 *  
 */
@SuppressWarnings("MagicConstant")
public class ShowGUIMessage
{

	/**screen component to display*/
	private static Container contentPane;


	/**
	 * display message if in GUI mode
	 */
    public static final void showstaticGUIMessage( final StringBuffer message, final String title )
	{

		/**
		 * create a display
		 */
		final JTextArea text_pane = new JTextArea();
		text_pane.setEditable( false );
		text_pane.setWrapStyleWord( true );
		text_pane.append( "  " + message + "  " );
		final JPanel display = new JPanel();
		display.setLayout( new BorderLayout() );
		display.add( text_pane, BorderLayout.CENTER );

		//set sizes
		final int width = (int)text_pane.getSize().getWidth();
		final int height = (int)text_pane.getSize().getHeight();
		display.setSize( new Dimension( width + 10, height + 10 ) );

		/**
		 * create the dialog
		 */
		JOptionPane.showConfirmDialog( contentPane, /* parentComponent*/ display, /* message*/ title, JOptionPane.DEFAULT_OPTION, /* optionType*/ JOptionPane.PLAIN_MESSAGE ); // messageType
	}

	//////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
	/**
	 * display message if in GUI mode
	 */
    public static final void showGUIMessage( final String user_message, final BufferedImage image, final String title )
	{

	    if(image==null) {
            return;
        }
		/**
		 * create a display
		 */
		final ImagePanel display = new ImagePanel( image );
		display.setLayout( new BorderLayout() );
		//display.setBackground(Color.cyan);
		if( user_message != null ) {
            display.add(new JLabel(user_message), BorderLayout.SOUTH);
        }

		//set sizes
		final int width = image.getWidth();
		final int height = image.getHeight();
		
		display.setSize( new Dimension( width + 10, height + 10 ) );

		/**
		 * create the dialog
		 */
		JOptionPane.showConfirmDialog( contentPane, /* parentComponent*/ display, /* message*/ title, JOptionPane.DEFAULT_OPTION, /* optionType*/ JOptionPane.PLAIN_MESSAGE ); // messageType
	}
	///////////////////////////////////////////////////////////
	/**
	 * display message if in GUI mode
	 */
    public static final void showGUIMessage( final String message_string, final String title )
	{

		//check for user mode just in case
        StringBuilder output_string = new StringBuilder("<HTML><BODY><CENTER><FONT COLOR=black>");
        final StringTokenizer lines = new StringTokenizer( message_string, "\n" );
        while( lines.hasMoreTokens() ) {
            output_string.append(lines.nextToken()).append("</FONT></CENTER><CENTER><FONT COLOR=black>");
        }
        output_string.append("</FONT></CENTER></BODY></HTML>");
        final JLabel text_message = new JLabel( output_string.toString() );
        text_message.setBackground( Color.white );

        /**
         * create a display, including scroll pane
         */
        final JPanel display = new JPanel();
        display.setLayout( new BorderLayout() );
        display.add( text_message, BorderLayout.CENTER );

        /**
         * create the dialog
         */
        JOptionPane.showConfirmDialog( contentPane, /* parentComponent*/ display, /* message*/ title, JOptionPane.DEFAULT_OPTION, /* optionType*/ JOptionPane.PLAIN_MESSAGE ); // messageType
        contentPane.setVisible(true);
	}
	///////////////////////////////////////////////////////////
}
