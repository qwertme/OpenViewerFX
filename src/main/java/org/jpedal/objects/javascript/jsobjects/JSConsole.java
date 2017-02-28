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
 * JSConsole.java
 * ---------------
 */
package org.jpedal.objects.javascript.jsobjects;

import org.jpedal.objects.javascript.JSApp;

import javax.swing.*;


public class JSConsole {

    private JDialog consoleDialog;
    private JTextArea consoleArea;

    // <end-demo>


    // This isn't part of the Adobe JS API, but is a common method in web JS
	public void log(final String s) {

        if(JSApp.showOutput){
		    System.out.println("JAVASCRIPT: " + s);
        }
        if(consoleArea != null) {
            consoleArea.append(s + '\n');
        }
	}
	public void println(final String s) {

        if(JSApp.showOutput){
		    System.out.println("JAVASCRIPT: " + s);
        }
        if(consoleArea != null) {
            consoleArea.append(s + '\n');
        }
	}

	public void show() {

        if(JSApp.showOutput){
		    System.out.println("console.show()");
        }
        if(consoleDialog != null) {
            consoleDialog.setVisible(true);
        }
	}

	public void clear() {

        if(JSApp.showOutput){
		    System.out.println("console.clear()");
        }
        if(consoleArea != null) {
            consoleArea.setText(null);
            consoleArea.getCaret().setVisible(true);
        }
	}

	public void hide() {

        if(JSApp.showOutput){
		    System.out.println("console.hide()");
        }
        if(consoleDialog != null) {
            consoleDialog.setVisible(false);
        }
	}
}
