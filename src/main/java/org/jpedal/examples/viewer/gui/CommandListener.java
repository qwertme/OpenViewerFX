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
 * CommandListener.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui;

import org.jpedal.external.ExternalHandlers;
import org.jpedal.fonts.glyph.JavaFXSupport;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Viewer;
//

/**
 * This class is fxSupport wrapper for SwingCommandListener and JavaFXCommandListener.
 * single listener to execute all GUI commands and call Commands to execute
 */
public class CommandListener {
	
    CommandListenerImpl commandListener;
    
	public CommandListener(final Commands currentCommands) {
        if(Viewer.isFX()){
            JavaFXSupport fxSupport = ExternalHandlers.getFXHandler();
            if(fxSupport!=null){
                commandListener =  (CommandListenerImpl) fxSupport.getCommandHandler(currentCommands);
            }
        }
        //
	}

    public CommandListenerImpl getCommandListener(){
        return commandListener;
    }
    
}
