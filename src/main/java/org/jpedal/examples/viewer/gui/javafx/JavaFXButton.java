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
 * JavaFXButton.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.javafx;

import java.net.URL;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.jpedal.examples.viewer.gui.generic.GUIButton;

/**Swing specific implementation of GUIButton interface*/
public class JavaFXButton extends Button implements GUIButton{
	
	private int ID;

	public JavaFXButton(){
    }

    @Override
    public void init(final URL path, final int ID, final String toolTip){

        this.ID=ID;

        path.toString(); //Conver the path to string so can be used in JavaFX
        
        /**bookmarks icon*/
        this.setTooltip(new Tooltip(toolTip));
       
        if (path!=null)  {
                setIcon(path);
        }

        //Prevent the dotted focus border
        this.setFocusTraversable(false);
    }
	
	
	/**command ID of button*/
	@Override
    public int getID() {
		
		return ID;
	}
	
	@Override
    public void setName(final String s){
		super.setId(s);
	}

    @Override
    public void setEnabled(final boolean b) {
         super.setDisable(!b);
    }

    @Override
    public void setIcon(final URL url) {
        super.setGraphic(new ImageView(new Image(url.toString())));
    }
    
}
