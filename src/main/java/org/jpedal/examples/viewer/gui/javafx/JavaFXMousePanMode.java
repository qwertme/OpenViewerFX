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
 * JavaFXMousePanMode.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx;

import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jpedal.PdfDecoderFX;

public class JavaFXMousePanMode implements JavaFXMouseFunctionality {

	//private Point currentPoint;
    //private Rectangle currentView;
	private final PdfDecoderFX decode_pdf;
	
    
	public JavaFXMousePanMode(final PdfDecoderFX decode_pdf) {
		this.decode_pdf=decode_pdf;
	}
    
    @Override
    public void mouseClicked(final MouseEvent e) {
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        
        if(e.getButton().equals(MouseButton.PRIMARY) || e.getButton().equals(MouseButton.MIDDLE)){
            
        /**
         * We do not need this code in FX because of the way we have constructed the Scene,
         * We already have our content in a ScrollPane which automaticly scrolls when dragged.
         * Unless it becomes an issue, this code is commented out.
         
            currentPoint.setX(e.getX());
            currentPoint.setY(e.getY());
            
            currentView.setWidth(((PdfDecoderFX)decode_pdf).getBoundsInLocal().getWidth());
            currentView.setHeight(((PdfDecoderFX)decode_pdf).getBoundsInLocal().getHeight());
        */

            //set cursor
            decode_pdf.setCursor(Cursor.CLOSED_HAND);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        decode_pdf.setCursor(Cursor.OPEN_HAND);
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        
        /**
         * We do not need this method in FX because of the way we have constructed the Scene,
         * We already have our content in a ScrollPane which automaticly scrolls when dragged.
         * Unless it becomes an issue, this method is commented out.
         
        if(e.getButton().equals(MouseButton.PRIMARY) || e.getButton().equals(MouseButton.MIDDLE)){
            final Point newPoint = new Point();
            newPoint.setX(e.getX());
            newPoint.setY(e.getY());

            double diffX = currentPoint.getX()-newPoint.getX();
            double diffY = currentPoint.getY()-newPoint.getY();


            Rectangle view = currentView;

            view.setX(view.getX()+diffX);

            view.setY(view.getY()+diffY);

            if(!view.contains(((PdfDecoderFX)decode_pdf).sc.getVisibleRect()))
                ((PdfDecoder)decode_pdf).scrollRectToVisible(view);
        }
        */
        
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
    }
    
}
