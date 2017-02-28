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
 * ViewerInt.java
 * ---------------
 */

package org.jpedal.examples.viewer;


/** PDF viewer
 *
 * If you are compiling, you will need to download all the examples source files from http://www.idrsolutions.com/how-to-view-pdf-files-in-java/
 *
 * Run directly from jar with java -cp jpedal.jar org/jpedal/examples/viewer/Viewer
 * or java -jar jpedal.jar
 *
 * Lots of tutorials on how to configure on our website
 * 
 * There is also a JavaFX Viewer documented at
 * http://files.idrsolutions.com/samplecode/org/jpedal/examples/viewer/OpenViewerFX.java.html
 *
 * If you want to implement your own
 * Very simple example at http://files.idrsolutions.com/samplecode/org/jpedal/examples/jpaneldemo/JPanelDemo.java.html
 * But we would recommend you look at the full viewer as it is totally configurable and does everything for you.
 *
 * See also http://javadoc.idrsolutions.com/org/jpedal/constants/JPedalSettings.html for settings to customise
 *
 * Fully featured GUI viewer and demonstration of JPedal's capabilities
 *
 * <br>This class provides the framework for the Viewer and calls other classes which provide the following
 * functions:-
 *
 * <br>Values commonValues - repository for general settings
 * Printer currentPrinter - All printing functions and access methods to see if printing active
 * PdfDecoder decode_pdf - PDF library and panel
 * ThumbnailPanel thumbnails - provides a thumbnail pane down the left side of page - thumbnails can be clicked on to goto page
 * PropertiesFile properties - saved values stored between sessions
 * SwingGUI currentGUI - all Swing GUI functions
 * SearchWindow searchFrame (not GPL) - search Window to search pages and goto references on any page
 * Commands currentCommands - parses and executes all options
 * SwingMouseHandler mouseHandler - handles all mouse and related activity
 */
public interface ViewerInt {
	
	//


    /**
     *
     * Allow user to open PDF file to display
     * @param defaultFile The file that will be displayed 
     */
    void openDefaultFile(String defaultFile);

    /**
     *Allow user to open PDF file to display
     * @param defaultFile The file that will be displayed 
     * @param page The page number on with the displayed file will be open
     */
    void openDefaultFileAtPage(String defaultFile, int page);
    
    void setRootContainer(Object rootContainer);

    /**
     * Should be called before setupViewer
     * @param props the set properties
     */
    void loadProperties(String props);

    /**
     * initialise and run client (default as Application in own Frame)
     */
    void setupViewer();
    
    /**
     * Have the viewer handle program arguments
     * @param args :: Program arguments passed into the Viewer.
     */
    void handleArguments(String[] args);

    /**
     * Execute Jpedal functionality from outside of the library using this method.
     * EXAMPLES
     *    commandID = Commands.OPENFILE, args = {"/PDFData/Hand_Test/crbtrader.pdf}"
     *    commandID = Commands.OPENFILE, args = {byte[] = {0,1,1,0,1,1,1,0,0,1}, "/PDFData/Hand_Test/crbtrader.pdf}"
     *    commandID = Commands.ROTATION, args = {"90"}
     *    commandID = Commands.OPENURL,  args = {"http://www.cs.bham.ac.uk/~axj/pub/papers/handy1.pdf"}
     *
     * for full details see http://www.idrsolutions.com/access-pdf-viewer-features-from-your-code/
     *
     * @param commandID :: static int value from Commands to spedify which command is wanted
     * @param args :: arguements for the desired command
     * @return command
     *
     */
    @SuppressWarnings("UnusedReturnValue")
    Object executeCommand(int commandID, Object[] args);


    /**
     * Allows external helper classes to be added to JPedal to alter default functionality.
     * <br><br>If Options.FormsActionHandler is the type then the <b>newHandler</b> should be
     * of the form <b>org.jpedal.objects.acroforms.ActionHandler</b>
     * <br><br>If Options.JPedalActionHandler is the type then the <b>newHandler</b> should be
     * of the form <b>Map</b> which contains Command Integers, mapped onto their respective
     * <b>org.jpedal.examples.viewer.gui.swing.JPedalActionHandler</b> implementations.  For example,
     * to create a custom help action, you would add to your map, Integer(Commands.HELP) ->  JPedalActionHandler.
     * For a tutorial on creating custom actions in the Viewer, see
     * <b>http://www.jpedal.org/support.php</b>
     *
     * @param newHandler Implementation of interface provided by IDR solutions
     * @param type Defined value into org.jpedal.external.Options class
     */
    void addExternalHandler(Object newHandler, int type);

    /**
     * run with caution and only at end of usage if you really need
     */
    void dispose();
    
}
