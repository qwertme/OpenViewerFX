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
 * ImagePanel.java
 * ---------------
 */
package org.jpedal.gui;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * JPanel used as a GUI Container for a BufferedImage.<P>
 * It provides a display for a  Bufferedimage which we used to debug software
 */
@SuppressWarnings("UnusedDeclaration")
public class ImagePanel extends JPanel
{
	
	/**image to display*/
	BufferedImage image;
	public ImagePanel( final BufferedImage image )
	{
		if(image!=null) {
            this.image = image;
        }
		this.setPreferredSize( new Dimension( image.getWidth(), image.getHeight() ) );
	}
	
	////////////////////////////////////////////////////////////////////////
	/**
	 * update screen display
	 */
    @Override
    public final void paintComponent( final Graphics g )
	{
		super.paintComponent( g );
		final Graphics2D g2 = (Graphics2D)g;
		g2.drawImage( image, 0, 0, this );
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(final BufferedImage image) {
		if(image!=null){
			this.image = image;
			this.setPreferredSize( new Dimension( image.getWidth(), image.getHeight() ) );
			this.repaint();
		}
	}
}
