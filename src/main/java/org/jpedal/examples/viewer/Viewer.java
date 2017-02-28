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
 * Viewer.java
 * ---------------
 */

package org.jpedal.examples.viewer;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;

import org.jpedal.*;
import org.jpedal.examples.viewer.commands.OpenFile;
import org.jpedal.examples.viewer.gui.*;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.display.GUIThumbnailPanel;
//
import org.jpedal.examples.viewer.utils.*;
import org.jpedal.examples.viewer.objects.ClientExternalHandler;
import org.jpedal.exception.PdfException;
import org.jpedal.external.Options;
import org.jpedal.fonts.FontMappings;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.actions.DefaultActionHandler;
import org.jpedal.objects.raw.OutlineObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.w3c.dom.Node;


/** <h2><b>PDF Viewer</b></h2>
 *
 * <p>If you are compiling, you will need to download all the examples source files from :
 * <a href="http://www.idrsolutions.com/how-to-view-pdf-files-in-java/">How to View PDF File in Java.</a></p>
 *
 * <p><b>Run directly from jar with java -cp jpedal.jar org/jpedal/examples/viewer/Viewer
 * or java -jar jpedal.jar</b></p>
 *
 * <p>There are plenty of tutorials on how to configure the Viewer on our website <a href="http://www.idrsolutions.com/java-pdf-library-support/">Support Section.</a></p>
 * 
 * <p><a href="http://javadoc.idrsolutions.com/org/jpedal/constants/JPedalSettings.html">See Here for Settings to Customise the PDF Viewer</a></p>
 * 
 * <p>We also have our JavaFX Viewer documented at :</p>
 * <a href="http://files.idrsolutions.com/samplecode/org/jpedal/examples/viewer/OpenViewerFX.java.html">JavaFX Viewer Documentation.</a>
 *
 * <p>If you want to implement your own there is
 * a very simple example at : 
 * <a href="http://files.idrsolutions.com/samplecode/org/jpedal/examples/jpaneldemo/JPanelDemo.java.html">JPanel Demo.</a></p>
 * <p>We recommend you look at the full viewer as it is totally configurable and does everything for you.</p>
 *
 *
 * <p>Fully featured GUI viewer and demonstration of JPedal's capabilities.</p>
 *
 * <p>This class provides the framework for the Viewer and calls other classes which provide the following
 * functions:-</p>
 *<ul>
 * <li>Values commonValues - repository for general settings.</li>
 * <li>Printer currentPrinter - All printing functions and access methods to see if printing active.</li>
 * <li>PdfDecoder decode_pdf - PDF library and panel.</li>
 * <li>ThumbnailPanel thumbnails - provides a thumbnail pane down the left side of page - thumbnails can be clicked on to goto page.</li>
 * <li>PropertiesFile properties - saved values stored between sessions.</li>
 * <li>SwingGUI currentGUI - all Swing GUI functions.</li>
 * <li>SearchWindow searchFrame (not GPL) - search Window to search pages and goto references on any page.</li>
 * <li>Commands currentCommands - parses and executes all options.</li>
 * <li>SwingMouseHandler mouseHandler - handles all mouse and related activity.</li>
 * </ul>
 * 
 */
public class Viewer implements ViewerInt{
	
    /**Location of Preferences Files*/
    public static final String PREFERENCES_DEFAULT = "jar:/org/jpedal/examples/viewer/res/preferences/Default.xml";
    public static final String PREFERENCES_NO_GUI = "jar:/org/jpedal/examples/viewer/res/preferences/NoGUI.xml";
    public static final String PREFERENCES_NO_SIDE_BAR = "jar:/org/jpedal/examples/viewer/res/preferences/NoSideTabOrTopButtons.xml";
    public static final String PREFERENCES_OPEN_AND_NAV_ONLY = "jar:/org/jpedal/examples/viewer/res/preferences/OpenAndNavOnly.xml";
    public static final String PREFERENCES_BEAN = "jar:/org/jpedal/examples/viewer/res/preferences/Bean.xml";
    
    /**repository for general settings*/
    protected Values commonValues=new Values();

    /**All printing functions and access methods to see if printing active 
     * - accessed from Commands and GUI */
    PrinterInt currentPrinter;

	/**PDF library and panel*/
	PdfDecoderInt decode_pdf;

	/**encapsulates all thumbnail functionality - just ignore if not required*/
	GUIThumbnailPanel thumbnails;

	/**values saved on file between sessions*/
	PropertiesFile properties=new PropertiesFile();
	
	/**general GUI functions*/
	protected GUIFactory currentGUI;

	/**search window and functionality*/
	GUISearchWindow searchFrame;

	/**command functions*/
	protected Commands currentCommands;

    /**warn user if viewer not setup fully*/
    private boolean isSetup;
    
    /**tell software to exit on close - default is true*/
    public static boolean exitOnClose=true;
    
    /**Throw runtime exception when user tries to open document after close() has been called*/
    public static boolean closeCalled;

    //


    /**
     *
     * @param defaultFile
     * Allow user to open PDF file to display
     */
    @Override
    public void openDefaultFile(final String defaultFile) {

        //get any user set dpi
        final String hiresFlag = System.getProperty("org.jpedal.hires");
        if(DecoderOptions.hires || hiresFlag != null) {
            commonValues.setUseHiresImage(true);
        }

        //get any user set dpi
        final String memFlag=System.getProperty("org.jpedal.memory");
        if(memFlag!=null) {
            commonValues.setUseHiresImage(false);
        }

        //reset flag
        if(thumbnails.isShownOnscreen()) {
            thumbnails.resetToDefault();
        }

        commonValues.maxViewY=0;// ensure reset for any viewport

        /**
         * open any default file and selected page
         */
        if(defaultFile!=null){

            final File testExists=new File(defaultFile);
            boolean isURL=false;
            if(defaultFile.startsWith("http:")|| defaultFile.startsWith("jar:") || defaultFile.startsWith("file:")){
                LogWriter.writeLog("Opening http connection");
                isURL=true;
            }

            if((!isURL) && (!testExists.exists())){
                currentGUI.showMessageDialog(defaultFile+ '\n' +Messages.getMessage("PdfViewerdoesNotExist.message"));
            }else if((!isURL) &&(testExists.isDirectory())){
                currentGUI.showMessageDialog(defaultFile+ '\n' +Messages.getMessage("PdfViewerFileIsDirectory.message"));
            }else{
                commonValues.setFileSize(testExists.length() >> 10);

                commonValues.setSelectedFile(defaultFile);

                currentGUI.setViewerTitle(null);

                /**see if user set Page*/
                final String page=System.getProperty("org.jpedal.page");
                final String bookmark=System.getProperty("org.jpedal.bookmark");
                if(page!=null && !isURL){

                    try{
                        int pageNum=Integer.parseInt(page);

                        if(pageNum<1){
                            pageNum=-1;
                            System.err.println(page+ " must be 1 or larger. Opening on page 1");
                            LogWriter.writeLog(page+ " must be 1 or larger. Opening on page 1");
                        }

                        if(pageNum!=-1) {
                            openFile(testExists, pageNum);
                        }


                    }catch(final Exception e){
                        System.err.println(page+ "is not a valid number for a page number. Opening on page 1 "+e);
                        LogWriter.writeLog(page+ "is not a valid number for a page number. Opening on page 1");
                    }
                }else if(bookmark!=null){
                    openFile(testExists,bookmark);
                }else{
                    currentGUI.openFile(defaultFile);
                }
            }
        }
    }

    /**
     *
     * @param defaultFile
     * Allow user to open PDF file to display
     */
    @Override
    public void openDefaultFileAtPage(final String defaultFile, final int page) {

        //get any user set dpi
        final String hiresFlag = System.getProperty("org.jpedal.hires");
        if(DecoderOptions.hires || hiresFlag != null) {
            commonValues.setUseHiresImage(true);
        }

        //get any user set dpi
        final String memFlag=System.getProperty("org.jpedal.memory");
        if(memFlag!=null) {
            commonValues.setUseHiresImage(false);
        }

        //reset flag
        if(thumbnails.isShownOnscreen()) {
            thumbnails.resetToDefault();
        }

        commonValues.maxViewY=0;// ensure reset for any viewport

        /**
         * open any default file and selected page
         */
        if(defaultFile!=null){

            final File testExists=new File(defaultFile);
            boolean isURL=false;
            if(defaultFile.startsWith("http:")|| defaultFile.startsWith("jar:")){
                LogWriter.writeLog("Opening http connection");
                isURL=true;
            }

            if((!isURL) && (!testExists.exists())){
                currentGUI.showMessageDialog(defaultFile+ '\n' +Messages.getMessage("PdfViewerdoesNotExist.message"));
            }else if((!isURL) &&(testExists.isDirectory())){
                currentGUI.showMessageDialog(defaultFile+ '\n' +Messages.getMessage("PdfViewerFileIsDirectory.message"));
           }else{

                commonValues.setSelectedFile(defaultFile);
                commonValues.setFileSize(testExists.length() >> 10);
                currentGUI.setViewerTitle(null);

                openFile(testExists,page);

            }
        }
    }
    
    public PdfDecoderInt getPdfDecoder() {
        return decode_pdf;
    }

    void init(){

        //
        
    }
    
    /**
	 * setup and run client
	 */
	public Viewer() {
        // args and stage in FX not set up yet 
        // so running init stuff causes the viewer to crash
        if(isFX){
            return;
        }
        init();
        
		//enable error messages which are OFF by default
		DecoderOptions.showErrorMessages=true;
		
		//
		
		
		final String prefFile = System.getProperty("org.jpedal.Viewer.Prefs");
		if(prefFile != null){
			properties.loadProperties(prefFile);
		}else{
			properties.loadProperties();
		}
    }

	/**
     * setup and run client passing in paramter to show if
     * running as applet, webstart or JSP (only applet has any effect
     * at present)
     * @param modeOfOperation The new modeOfOperation(RUNNING_NORMAL,RUNNING_APPLET,RUNNING_WEBSTART,RUNNING_JSP)
     */
    public Viewer(final int modeOfOperation) {

        init();
        
        //enable error messages which are OFF by default
        DecoderOptions.showErrorMessages=true;

        final String prefFile = System.getProperty("org.jpedal.Viewer.Prefs");
        if(prefFile != null){
            properties.loadProperties(prefFile);
        }else{
            properties.loadProperties();
        }

        commonValues.setModeOfOperation(modeOfOperation);

    }

    /**
     * setup and run client passing in paramter that points to the preferences file we should use.
     * @param prefs Full path to xml file containing preferences
     */
    public Viewer(final String prefs) {
        
        init();

        //enable error messages which are OFF by default
        DecoderOptions.showErrorMessages=true;

        try{
            properties.loadProperties(prefs);
        }catch(final Exception e){
            System.err.println("Specified Preferrences file not found at "+prefs+". If this file is within a jar ensure filename has jar: at the begining.\n\nLoading default properties. "+e);

            properties.loadProperties();
        }


    }

    /**
     * setup and run client passing in parameter that points to the preferences file we should use.
     * preferences file
     * @param rootContainer Is a swing component that the viewer is displayed inside
     * @param preferencesPath The path of the preferences file
     */
    public Viewer(final Object rootContainer, final String preferencesPath) {

        init();
        
        //enable error messages which are OFF by default
        DecoderOptions.showErrorMessages=true;

        if(preferencesPath!=null && !preferencesPath.isEmpty()){
            try{
                properties.loadProperties(preferencesPath);
            }catch(final Exception e){
                System.err.println("Specified Preferrences file not found at "+preferencesPath+". If this file is within a jar ensure filename has jar: at the begining.\n\nLoading default properties. "+e);

                properties.loadProperties();
            }
        }else{
            properties.loadProperties();
        }
        setRootContainer(rootContainer);

    }
    
    @Override
    public void setRootContainer(final Object rootContainer){
        currentGUI.setRootContainer(rootContainer);
    }

    /**
     * Should be called before setupViewer
     */
    @Override
    public void loadProperties(final String props){
        properties.loadProperties(props);
    }

    /**
     * Should be called before setupViewer
     * @param is input of loaded properties
     */
    public void loadProperties(final InputStream is){
        properties.loadProperties(is);
    }

    /**
     * initialise and run client (default as Application in own Frame)
     */
    @Override
    public void setupViewer() {

        //also allow messages to be suppressed with JVM option
        final String flag=System.getProperty("org.jpedal.suppressViewerPopups");
        boolean suppressViewerPopups =false;

        if(flag!=null && flag.equalsIgnoreCase("true")) {
            suppressViewerPopups = true;
        }

        /**
         *  set search window position here to ensure
         *  that gui has correct value
         */
        final String searchType = properties.getValue("searchWindowType");
        if(searchType!=null && !searchType.isEmpty()){
            final int type = Integer.parseInt(searchType);
            searchFrame.setViewStyle(type);
        }else {
            searchFrame.setViewStyle(GUISearchWindow.SEARCH_MENU_BAR);
        }

        searchFrame.setUpdateListDuringSearch(properties.getValue("updateResultsDuringSearch").equals("true"));
        
        //Set search frame here
        currentGUI.setSearchFrame(searchFrame);

        /**switch on thumbnails if flag set*/
        final String setThumbnail=System.getProperty("org.jpedal.thumbnail");
        if(setThumbnail!=null){
            if(setThumbnail.equals("true")) {
                thumbnails.setThumbnailsEnabled(true);
            } else if(setThumbnail.equals("false")) {
                thumbnails.setThumbnailsEnabled(false);
            }
        }else //default
        {
            thumbnails.setThumbnailsEnabled(true);
        }

        /**
         * non-GUI initialisation
         **/

        //allow user to override messages
        //<link><a name="locale" />
        /**
         * allow user to define country and language settings
         *
         * you will need a file called messages_XX.properties in
         * org.jpedal.international.messages where XX is a valid Locale.
         *
         * You can also choose an alternative Lovation - see sample code below
         *
         *  You can manually set Java to use a Locale with this code
         *  (also useful to test)
         *
         *	Example here is Brazil (note no Locale files present for it)
         *
         * If you make and Locale files, we would be delighted to include them
         * in future versions of the software.
         *
         java.util.Locale aLocale = new java.util.Locale("br", "BR");

         java.util.Locale.setDefault(aLocale);
         */


        final String customBundle=System.getProperty("org.jpedal.bundleLocation");
        //customBundle="org.jpedal.international.messages"; //test code

        if(customBundle!=null){

            final BufferedReader input_stream;
            final ClassLoader loader = Messages.class.getClassLoader();
            final String fileName=customBundle.replaceAll("\\.","/")+ '_' +java.util.Locale.getDefault().getLanguage()+".properties";

            //also tests if locale file exists and tell user if not
            try{

                input_stream =new BufferedReader(new InputStreamReader(loader.getResourceAsStream(fileName)));
                input_stream.close();

            }catch(final IOException ee){

                //

                java.util.Locale.setDefault(new java.util.Locale("en", "EN"));
                currentGUI.showMessageDialog("No locale file "+fileName+" has been defined for this Locale - using English as Default"+
                        "\n Format is path, using '.' as break ie org.jpedal.international.messages");

            }

            init(ResourceBundle.getBundle(customBundle));

        }else {
            init(null);
        }

        /**
         * gui setup, create gui, load properties
         */
        currentGUI.init(currentCommands,currentPrinter);

        if(searchFrame.getViewStyle()==GUISearchWindow.SEARCH_TABBED_PANE) {
            currentGUI.searchInTab(searchFrame);
        }

        /**
         * setup window for warning if renderer has problem
         */
        if(!Viewer.isFX){
            decode_pdf.getDynamicRenderer().setMessageFrame((Container)currentGUI.getFrame());
        }
        String propValue = properties.getValue("showfirsttimepopup");
        final boolean showFirstTimePopup = !suppressViewerPopups && !propValue.isEmpty() && propValue.equals("true");

        if(showFirstTimePopup){
            currentGUI.showFirstTimePopup();
            properties.setValue("showfirsttimepopup","false");
        }
        
        final boolean wasUpdateAvailable = false;

//        propValue = properties.getValue("automaticupdate");
//        if (!suppressViewerPopups && !propValue.isEmpty() && propValue.equals("true")) {
//            wasUpdateAvailable = Update.checkForUpdates(false, currentGUI);
//        }

        propValue = properties.getValue("displaytipsonstartup");
        if(!suppressViewerPopups && !wasUpdateAvailable && !propValue.isEmpty() && propValue.equals("true")){
            currentCommands.executeCommand(Commands.TIP,null);
        }

        //flag so we can warn user if they call executeCommand without it setup
        isSetup=true;
    }
    /**
     * setup the viewer
     */
    protected void init(final ResourceBundle bundle) {

        /**
         * load correct set of messages
         */
        if(bundle==null){

            //load locale file
            try{
                Messages.setBundle(ResourceBundle.getBundle("org.jpedal.international.messages"));
            }catch(final Exception e){
                //
                LogWriter.writeLog("Exception "+e+" loading resource bundle.\n" +
                        "Also check you have a file in org.jpedal.international.messages to support Locale="+java.util.Locale.getDefault());
            }

        }else{
            try{
                Messages.setBundle(bundle);
            }catch(final Exception ee){
                LogWriter.writeLog("Exception with bundle "+bundle);
                ee.printStackTrace();
            }
        }
        
        //pass through GUI for use in multipages and Javascript
        decode_pdf.addExternalHandler(currentGUI, Options.MultiPageUpdate);

        //used to test ability to replace Javascript with own engine
        //org.jpedal.objects.javascript.ExpressionEngine marksTest=new TestEngine();
        //decode_pdf.addExternalHandler(marksTest, Options.ExpressionEngine);

        /**debugging code to create a log*/
        //LogWriter.setupLogFile("v");
        //LogWriter.log_name =  "/mnt/shared/log.txt";

        //make sure widths in data CRITICAL if we want to split lines correctly!!
        DecoderOptions.embedWidthData = true;

        //<link><a name="customann" />
        /**
         * ANNOTATIONS code
         *
         * replace Annotations with your own custom annotations using paint code
         *
         */
        //decode_pdf.setAnnotationsVisible(false); //disable built-in annotations and use custom versions
        //code to create a unique iconset
        //see also <link><a href="http://files.idrsolutions.com/samplecode/org/jpedal/examples/viewer/gui/GUI.java.html#handleAnnotations">org.jpedal.examples.viewer.gui.GUI.handleAnnotations()</a>

        //this allows the user to place fonts in the classpath and use these for display, as if embedded
        //decode_pdf.addSubstituteFonts("org/jpedal/res/fonts/", true);

        //set to extract all
        //COMMENT OUT THIS LINE IF USING JUST THE VIEWER
        decode_pdf.setExtractionMode(0,1); //values extraction mode,dpi of images, dpi of page as a factor of 72

        //don't extract text and images (we just want the display)

		/**/
        /**
         * FONT EXAMPLE CODE showing JPedal's functionality to set values for
         * non-embedded fonts.
         *
         * This allows sophisticated substitution of non-embedded fonts.
         *
         * Most font mapping is done as the fonts are read, so these calls must
         * be made BEFORE the openFile() call.
         */

        //<link><a name="fontmapping" />
        /**
         * FONT EXAMPLE - Replace global default for non-embedded fonts.
         *
         * You can replace Lucida as the standard font used for all non-embedded and substituted fonts
         * by using is code.
         * Java fonts are case sensitive, but JPedal resolves currentGUI.frame, so you could
         * use Webdings, webdings or webDings for Java font Webdings
         */

        /** Removed to save time on startup - uncomment if it causes problems
         try{
         //choice of example font to stand-out (useful in checking results to ensure no font missed.
         //In general use Helvetica or similar is recommended
         //			decode_pdf.setDefaultDisplayFont("SansSerif");
         }catch(PdfFontException e){ //if its not available catch error and show valid list

         System.out.println(e.getMessage());

         //get list of fonts you can use
         String[] fontList =GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
         System.out.println(Messages.getMessage("PdfViewerFontsFound.message"));
         System.out.println("=====================\n");
         int count = fontList.length;
         for (int i = 0; i < count; i++) {
         Font f=new Font(fontList[i],1,10);
         System.out.println(fontList[i]+" ("+Messages.getMessage("PdfViewerFontsPostscript.message")+ '=' +f.getPSName()+ ')');

         }
         System.exit(1);

         }/***/

        /**
         * IMPORTANT note on fonts for EXAMPLES
         *
         * USEFUL TIP : The Viewer displays a list of fonts used on the
         * current PDF page with the File > Fonts menu option.
         *
         * PDF allows the use of weights for fonts so Arial,Bold is a weight of
         * Arial. This value is not case sensitive so JPedal would regard
         * arial,bold and aRiaL,BoLd as the same.
         *
         * Java supports a set of Font families internally (which may have
         * weights), while JPedals substitution facility uses physical True Type
         * fonts so it is resolving each font weight separately. So mapping
         * works differently, depending on which is being used.
         *
         * If you are using a font, which is named as arial,bold you can use
         * either arial,bold or arial (and JPedal will then try to select the
         * bold weight if a Java font is used).
         *
         * So for a font such as Arial,Bold JPedal will test for an external
         * truetype font substitution (ie arialMT.ttf) mapped to Arial,Bold. BUT
         * if the substitute font is a Java font an additional test will be made
         * for a match against Arial if there is no match on Arial,Bold.
         *
         * If you want to map all Arial to equivalents to a Java font such as
         * Times New Roman, just map Arial to Times New Roman (only works for
         * inbuilt java fonts). Note if you map Arial,Bold to a Java font such
         * as Times New Roman, you will get Times New Roman in a bold weight, if
         * available. You cannot set a weight for the Java font.
         *
         * If you wish to substitute Arial but not Arial,Bold you should
         * explicitly map Arial,Bold to Arial,Bold as well.
         *
         * The reason for the difference is that when using Javas inbuilt fonts
         * JPedal can resolve the Font Family and will try to work out the
         * weight internally. When substituting Truetype fonts, these only
         * contain ONE weight so JPedal is resolving the Font and any weight as
         * a separate font . Different weights will require separate files.
         *
         */

        /**
         * FONT EXAMPLE - Use fonts placed in jar for substitution (1.4 and above only)
         *
         * This allows users to store fonts in the jar and use these for
         * substitution. Please see javadoc for full description of usage.
         */
        //decode_pdf.addSubstituteFonts(fontPath,enforceMapping)

        //<link><a name="substitutedfont" />
        /**
         * FONT EXAMPLE - Use fonts located on machine for substitution
         *
         * This code explains how to use JPedal to substitute fonts which are
         * not embedded using fonts held in any font directory.
         *
         * It works as follows:-
         *
         * If the -Dorg.jpedal.fontdirs="C:/win/fonts/","/mnt/X11/fonts" is set to a
         * comma-separated list of directories, any truetype fonts (with .ttf
         * file ending) will be logged and added to the substitution table. So
         * arialMT.ttf will be added as arialmt. If arialmt is used in the PDF
         * but not embedded, JPedal will use this font file to render it.
         *
         * If a command line paramter is not appropriate, the call
         * setFontDirs(String[] fontDirs) will achieve the same.
         *
         *
         * If the name is not an exact match (ie you have arialMT which you wish
         * to use to display arial, you can use the method
         * setSubstitutedFontAliases(String[] name, String[] aliases) to convert
         * it internally - see sample code at bottom of note.
         *
         * The Name is not case-sensitive.
         *
         * Spaces are important so TimesNewRoman and Times New Roman are
         * degarded as 2 fonts.
         *
         * If you have 2 copies of arialMT.ttf in the scanned directories, the
         * last one will be used.
         *
         * If the file was called arialMT,bold.ttf it is resolved as
         * ArialMT,bold only.
         *
         */

        //mappings for non-embedded fonts to use
        FontMappings.setFontReplacements();

        //decode_pdf.setFontDirs(new String[]{"C:/windows/fonts/","C:/winNT/fonts/"});
        /**
         * FONT EXAMPLE - Use Standard Java fonts for substitution
         *
         * This code tells JPedal to substitute fonts which are not embedded.
         *
         * The Name is not case-sensitive.
         *
         * Spaces are important so TimesNewRoman and Times New Roman are
         * degarded as 2 fonts.
         *
         * If you have 2 copies of arialMT.ttf in the scanned directories, the
         * last one will be used.
         *
         *
         * If you wish to use one of Javas fonts for display (for example, Times
         * New Roman is a close match for myCompanyFont in the PDF, you can the
         * code below
         *
         * String[] aliases={"Times New Roman"};//,"helvetica","arial"};
         * decode_pdf.setSubstitutedFontAliases("myCompanyFont",aliases);
         *
         * Here is is used to map Javas Times New Roman (and all weights) to
         * TimesNewRoman.
         *
         * This can also be done with the command -org.jpedal.fontmaps="TimesNewRoman=Times New Roman","font2=pdfFont1"
         */
        //String[] nameInPDF={"TimesNewRoman"};//,"helvetica","arial"};
        //decode_pdf.setSubstitutedFontAliases("Times New Roman",nameInPDF);

        //<link><a name="imageHandler" />
        /**
         * add in external handlers for code - 2 examples supplied
         *

         //org.jpedal.external.ImageHandler myExampleImageHandler=new org.jpedal.examples.handlers.ExampleImageDecodeHandler();
         org.jpedal.external.ImageHandler myExampleImageHandler=new org.jpedal.examples.handlers.ExampleImageDrawOnScreenHandler();

         decode_pdf.addExternalHandler(myExampleImageHandler, Options.ImageHandler);


         /**/
//<link><a name="customMessageOutput" />
        /**
         * divert all message to our custom code
         *

         CustomMessageHandler myExampleCustomMessageHandler =new ExampleCustomMessageHandler();

         decode_pdf.addExternalHandler(myExampleCustomMessageHandler, Options.CustomMessageOutput);

         /**/

    }
    
    static boolean isFX;

    /** main method to run the software as standalone application
     * @param args Program arguments passed into the Viewer.
     */
    public static void main(final String[] args) {

        //make sure JavaFX run correctly
        if(isFX){
            System.out.println("Please run ViewerFx using java -cp jpedal.jar org.jpedal.examples.viewer.FXStartup");
            System.exit(1);
        }
        
        final Viewer current = new Viewer();
        current.setupViewer();
        current.handleArguments(args);
    }

    /**
     * Have the viewer handle program arguments
     * @param args :: Program arguments passed into the Viewer.
     */
    @Override
    public void handleArguments(final String[] args){
    	
    	//Ensure default open is on event thread, otherwise the display is updated as values are changing
        if(SwingUtilities.isEventDispatchThread()){
        	
            if (args.length > 0){
        		openDefaultFile(args[0]);
        	}else if((properties.getValue("openLastDocument").equalsIgnoreCase("true")) &&
        		(properties.getRecentDocuments()!=null
        				&& properties.getRecentDocuments().length>1)){

        			int lastPageViewed = Integer.parseInt(properties.getValue("lastDocumentPage"));

        			if(lastPageViewed<0) {
                        lastPageViewed = 1;
                    }

        			openDefaultFileAtPage(properties.getRecentDocuments()[0],lastPageViewed);
        	}
        }else{

        	final Runnable run = new Runnable() {
				
				@Override
				public void run() {
					if (args.length > 0){
						openDefaultFile(args[0]);

			        }else if(properties.getValue("openLastDocument").toLowerCase().equals("true")){
			            if(properties.getRecentDocuments()!=null
			                    && properties.getRecentDocuments().length>1){

			                int lastPageViewed = Integer.parseInt(properties.getValue("lastDocumentPage"));

			                if(lastPageViewed<0) {
                                lastPageViewed = 1;
                            }

			                openDefaultFileAtPage(properties.getRecentDocuments()[0],lastPageViewed);
			            }
			        }
				}
			};
			SwingUtilities.invokeLater(run);
        }
    }

    /**
     * General code to open file at specified boomark - do not call directly
     *
     * @param file File the PDF to be decoded
     * @param bookmark - if not present, exception will be thrown
     */
    private void openFile(final File file, final String bookmark) {

        try{
            final boolean fileCanBeOpened=OpenFile.openUpFile(file.getCanonicalPath(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);

            Object bookmarkPage=null;

            int page=-1;

            //reads tree and populates lookup table
            if(decode_pdf.getOutlineAsXML()!=null){
                final Node rootNode= decode_pdf.getOutlineAsXML().getFirstChild();
                if(rootNode!=null) {
                    bookmarkPage = currentGUI.getBookmark(bookmark);
                }

                if(bookmarkPage!=null) {
                    page = Integer.parseInt((String) bookmarkPage);
                }
            }

            //it may be a named destination ( ie bookmark=Test1)
            if(bookmarkPage==null){
                bookmarkPage=decode_pdf.getIO().convertNameToRef(bookmark);

                if(bookmarkPage!=null){

                    //read the object
                    final PdfObject namedDest=new OutlineObject((String)bookmarkPage);
                    decode_pdf.getIO().readObject(namedDest);

                    //still needed to init viewer
                    if(fileCanBeOpened) {
                        OpenFile.processPage(commonValues, decode_pdf, currentGUI, thumbnails);
                    }

                    //and generic open Dest code
                    decode_pdf.getFormRenderer().getActionHandler().gotoDest(namedDest, ActionHandler.MOUSECLICKED, PdfDictionary.Dest );
                }
            }

            if(bookmarkPage==null) {
                throw new PdfException("Unknown bookmark " + bookmark);
            }


            if(page>-1){
                commonValues.setCurrentPage(page);
                if(fileCanBeOpened) {
                    OpenFile.processPage(commonValues, decode_pdf, currentGUI, thumbnails);
                }
            }
        }catch(final Exception e){
            System.err.println("Exception " + e + " processing file");

            //<start-demo><end-demo>

            Values.setProcessing(false);
        }
    }

    /**
     * General code to open file at specified page - do not call directly
     *
     * @param file File the PDF to be decoded
     * @param page int page number to show the user
     */
    private void openFile(final File file, final int page) {

        try{
            final boolean fileCanBeOpened=OpenFile.openUpFile(file.getCanonicalPath(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);

            commonValues.setCurrentPage(page);

            if(fileCanBeOpened) {
                OpenFile.processPage(commonValues, decode_pdf, currentGUI, thumbnails);
            }
        }catch(final Exception e){
            System.err.println("Exception " + e + " processing file");

            //<start-demo><end-demo>

            Values.setProcessing(false);
        }
    }

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
     *
     */
    @Override
    @SuppressWarnings("UnusedReturnValue")
    public Object executeCommand(final int commandID, final Object[] args){

        /**
         * far too easy to miss this step (I did!) so warn user
         */
        if(!isSetup){
            throw new RuntimeException("You must call viewer.setupViewer(); before you call any commands");
        }

        return currentCommands.executeCommand(commandID, args);

    }

    //

    public static boolean isProcessing(){
        return Values.isProcessing();
    }

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
    @Override
    public void addExternalHandler(final Object newHandler, final int type) {
        decode_pdf.addExternalHandler(newHandler, type);
    }

    /**
     * run with caution and only at end of usage if you really need
     */
    @Override
    public void dispose() {

        commonValues=null;

        currentPrinter=null;

        if(thumbnails!=null) {
            thumbnails.dispose();
        }

        thumbnails=null;

        if(properties!=null) {
            properties.dispose();
        }

        properties=null;

        if(currentGUI!=null) {
            currentGUI.dispose();
        }

        currentGUI=null;

        searchFrame=null;

        currentCommands=null;

        if(decode_pdf!=null) {
            decode_pdf.dispose();
        }

        decode_pdf =null;

        Messages.dispose();

    }
    
    public static boolean isFX(){
        return isFX;
    }
    
    
}
