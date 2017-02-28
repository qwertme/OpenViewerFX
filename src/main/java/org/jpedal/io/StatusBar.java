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
 * StatusBar.java
 * ---------------
 */
package org.jpedal.io;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jpedal.utils.LogWriter;

/**
 * encapsulates a status bar to display progess of a page decode and messages 
 * for a GUI client and methods to access it - 
 * See org.examples.jpedal.viewer.Viewer for example of usage
 *  
 */
public class StatusBar
{

	/**amount of detail to show*/
	private static final int debug_level = 0;

	/**current numeric value of Progress bar*/
	int progress_size;
	
	/**message to display*/
	String current="";

	/**numeric value Progress bar will count to (lines in data file)*/
	private static final int progress_max_size = 100;

	/**actual status bar*/
	JProgressBar status;

	/**if there is a GUI display*/
	private  boolean showMessages;
	
	/**amount done on decode*/
	public float percentageDone;
	
	/** master color for statusBar, by default is red*/
	private Color masterColor;
	
    /** boolean to show when the status has been reset*/
    private boolean reset;
	
	/**
     * Initialises statusbar using default colors.
     */
	public StatusBar()
	{
		initialiseStatus("");
	}
	
    /**
     * Initialises statusbar using default colors.
     *
     * @param newColor is of type Color
     */
	public StatusBar(final Color newColor)
	{
		masterColor=newColor;
		initialiseStatus("");
	}
	
	////////////////////////////////////////
    /**
     * Initiate status bar.
     * 
     * @param current is of type String
     */
    public final void initialiseStatus( final String current)
	{
		progress_size = 0;
		status = new JProgressBar();
		if(masterColor!=null) {
            status.setForeground(masterColor);
        }
		//show that somethings happerning but not sure how long for
		//status.setIndeterminate(true);
		status.setStringPainted( true );
		status.setMaximum( progress_max_size );
		status.setMinimum( 0 );
		updateStatus( current, 4 );
	}
	////////////////////////////////////////
    /**
     * Update status if client being used also writes to log (called internally
     * as file decoded).
     *
     * @param progress_bar is of type String
     * @param debug_level_to_use is of type int
     */
    public final void updateStatus( final String progress_bar, final int debug_level_to_use )
	{

		current=progress_bar;
		
		//update status if in client
		if(showMessages)
		{
			SwingUtilities.invokeLater(new Runnable(){
				@Override
                public void run(){
					status.setString( current );
					status.setValue( progress_size );
				}
			});
	
		}
		if( debug_level > debug_level_to_use && LogWriter.isOutput()) {
            LogWriter.writeLog(progress_bar);
        }
	}
	/////////////////////////////////////////////
    /**
     * Return handle on status bar so it can be displayed.
     * 
     * @return Component
     */
    public final Component getStatusObject()
	{
		return status;
	}

    /**
     * Set progress value (called internally as page decoded).
     * 
     * @param size is of type int
     */
    public final void setProgress( final int size )
	{
        reset=false;
	    if(status!=null){
		if(size==0) {
            progress_size = 0;
        }
		if( progress_size < size ) {
            progress_size = size;
        }
		//if( showMessages == true ){
			SwingUtilities.invokeLater(new Runnable(){
				@Override
                public void run(){
                    status.setValue( progress_size );

				}
			});
		}
	}
	////////////////////////////////////////
	/**
	 * set progress value (called internally as page decoded)
         * 
         * @param message The message displayed while the page decoding progresses
         * @param size The size of page decoding progress
	 */
    public final void setProgress( final String message, final int size )
	{
        reset=false;
	    if(status!=null){
		if(size==0) {
            progress_size = 0;
        }
		if( progress_size < size ) {
            progress_size = size;
        }
		//if( showMessages == true ){
			SwingUtilities.invokeLater(new Runnable(){
				@Override
                public void run(){
                    status.setString(message);
					status.setValue( progress_size );

				}
			});
		}
	}
	////////////////////////////////////////////////////////////
    /**
     * Reset status bar.
     *
     * @param current is of type String
     */
    public final void resetStatus( final String current )
	{
        reset = true;
		progress_size = 0;
		updateStatus( current, 4 );
	}

	//////////////////////////
	/**
	 * set client flag to display
	 */
    public final void setClientDisplay()
	{
		showMessages = true;
	}
	
	public void setVisible(final boolean visible){
		status.setVisible(visible);
	}
	
	public void setEnabled(final boolean enable){
		status.setEnabled(enable);
	}
	
	public boolean isVisible(){
		return status.isVisible();
	}
	
	public boolean isEnabled(){
		return status.isEnabled();
	}

    public boolean isDone() {
        return reset || progress_size >= progress_max_size;
    }
	
}
