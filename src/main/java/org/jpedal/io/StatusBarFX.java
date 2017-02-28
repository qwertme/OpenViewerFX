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
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javax.swing.SwingUtilities;

import org.jpedal.utils.LogWriter;

/**
 * encapsulates a status bar to display progress of a page decode and messages 
 * for a GUI client and methods to access it - 
 * See org.jpedal.examples.viewer.ViewerFX for example of usage
 *  
 */
public class StatusBarFX
{

	/**amount of detail to show*/
	private static final int debug_level = 0;

	/**current numeric value of Progress bar*/
	double progress_size;
	
	/**message to display*/
	String current="";

	/**numeric value Progress bar will count to (lines in data file)*/
	private static final double progress_max_size = 100;

	/**actual status bar*/
    ProgressBar status;

    Text statusText;
    
    StackPane pane;
    
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
	public StatusBarFX()
	{
		initialiseStatus("");
	}
	
    /**
     * Initialises statusbar using default colors.
     *
     * @param newColor is of type Color
     */
	public StatusBarFX(final Color newColor)
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
		status = new ProgressBar();
        statusText = new Text();
        pane = new StackPane();
        
        status.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        pane.getChildren().add(status);
        pane.getChildren().add(statusText);
        
		if(masterColor!=null) {
            final int r = (int)(255*masterColor.getRed());
            final int g = (int)(255*masterColor.getGreen());
            final int b = (int)(255*masterColor.getBlue());
            status.setStyle("-fx-background:rgb(" + r + ',' + g + ',' + b + ");");
        }
		//show that somethings happerning but not sure how long for
		//status.setIndeterminate(true);
//		status.setStringPainted( true );
//		status.setMaximum( progress_max_size );
//		status.setMinimum( 0 );
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
					statusText.setText( current );
                    status.setProgress(progress_size / progress_max_size);
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
    public final Pane getStatusObject()
	{
		return pane;
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
			Platform.runLater(new Runnable() {
				@Override
                public void run(){
                    status.setProgress(progress_size / progress_max_size);

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
			Platform.runLater(new Runnable() {
				@Override
                public void run(){
                    statusText.setText(message);
                    status.setProgress(progress_size / progress_max_size);
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
        pane.setVisible(visible);
	}
	
	public void setDisable(final boolean enable){
        pane.setDisable(enable);
	}
	
	public boolean isVisible(){
		return pane.isVisible();
	}
	
	public boolean isDisable(){
		return pane.isDisabled();
	}

    public boolean isDone() {
        return reset || progress_size >= progress_max_size;
    }
	
}
