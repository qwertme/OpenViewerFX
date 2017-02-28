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
 * PropertiesFile.java
 * ---------------
 */
package org.jpedal.examples.viewer.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jpedal.PdfDecoderInt;
import org.jpedal.gui.ShowGUIMessage;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.LogWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**holds values stored in XML file on disk*/
public class PropertiesFile {
    
    private final String separator=System.getProperty( "file.separator" );
    private String userDir=System.getProperty("user.dir");
    private String configFile=userDir+separator+".properties.xml";
    private InputStream configInputStream;
    
    private boolean isReadOnly;
    
    public boolean isReadOnly() {
        return isReadOnly;
    }
    
    private Document doc;
    
    private static final int noOfRecentDocs = 6;
    
    private String[] properties = {
        
        //<start-demo>
        "Flag to show popup information first time viewer is used","showfirsttimepopup", "false",
        /**
            //<end-demo>
			"Flag to show popup information first time viewer is used","showfirsttimepopup", "true", /**/
        "The amount of days left of the trial","daysLeft","",
        "Show message about rhino and it's use","showrhinomessage", "false",
        "Set how the search functionality is displayed\n0 : External Window\n1 : Side Tab Bar\n2 : Menu bar","searchWindowType", "2",
      "Set if border should be shown\n0 : Hide Border\n1 : Show Border","borderType", "1",
      "Flag to turn on hiRes printing","useHiResPrinting", "true",
      "This is set the number of pixels used to represent an inch on screen","resolution", "110",
      "Flag to allow cursor to change such as when over text","allowCursorToChange", "true",
      "Flag to allow view to scroll when dragging the mouse","autoScroll", "true",
      "No longer used, please use startView","pageMode", "1",
      "Flag to allow tips to be displayed at start up","displaytipsonstartup", "false",
      "Flag to allow the viewer to auto update","automaticupdate", "true",
      "Value shows what version of the library is being used","currentversion", PdfDecoderInt.version,
      "Show message when using tiffs","showtiffmessage", "true",
      "The maximum number of viewers when using the multi viewer example","maxmultiviewers", "20",
      "Shows the contents of the menu bar, menubar remains but empty if set to false","MenuBarMenu", "true",
      "Shows the File menu on the menu bar","FileMenu", "true",
      "Shows the Open menu on the File menu","OpenMenu", "true",
      "Shows the Open menuitem on the Open menu","Open", "true",
      "Shows the Open url menuitem on the Open menu","Openurl", "true",
      "ENDCHILDREN",
      "Shows the Save menuitem on the File menu","Save", "true",
      "Shows the Resave forms menuitem on the File menu","Resaveasforms", "false",
      "Shows the Find menuitem on the File menu","Find", "true",
      "Shows the Document Properties menuitem on the File menu","Documentproperties", "true",
      "Shows the Sign pdf menuitem on the File menu","Signpdf", "true",
      "Shows the Print menuitem on the File menu","Print", "true",
      "Shows the Recent Documents menuitem on the File menu","Recentdocuments", "true",
      "Shows the Exit menuitem on the File menu","Exit", "true",
      "ENDCHILDREN",
      "Shows the Edit menu on the menu bar","EditMenu", "true",
      "Shows the Copy menuitem on the Edit menu","Copy", "true",
      "Shows the Select all menuitem on the Edit menu","Selectall", "true",
      "Shows the Deselect all menuitem on the Edit menu","Deselectall", "true",
      "Shows the Preferences menuitem on the Edit menu","Preferences", "true",
        "ENDCHILDREN",
      "Shows the View menu on the menu bar","ViewMenu", "true",
      "Shows the Goto menu on the View menu","GotoMenu", "true",
      "Shows the First Page menuitem on the GoTo menu","Firstpage", "true",
      "Shows the Back Page menuitem on the GoTo menu","Backpage", "true",
      "Shows the Next Page menuitem on the GoTo menu","Forwardpage", "true",
      "Shows the Last Page menuitem on the GoTo menu","Lastpage", "true",
      "Shows the GoTo Page menuitem on the GoTo menu","Goto", "true",
      "Shows the Previous Document menuitem on the GoTo menu","Previousdocument", "true",
      "Shows the Next Document menuitem on the GoTo menu","Nextdocument", "true",
        "ENDCHILDREN",
      "Shows the PageLayout menu on the View menu","PagelayoutMenu", "true",
      "Shows the Single Page Mode menuitem on the PageLayout menu","Single", "true",
      "Shows the Continuous Page Mode menuitem on the PageLayout menu","Continuous", "true",
      "Shows the Facing Page Mode menuitem on the PageLayout menu","Facing", "true",
      "Shows the Continuous Facing Page Mode menuitem on the PageLayout menu","Continuousfacing", "true",
      "Shows the PageFlow Page Mode menuitem on the PageLayout menu","PageFlow", "true",
        "ENDCHILDREN",
      "Shows the Separate Cover menuitem on the View menu","separateCover", "true",
      "Shows the Text Select Mouse Mode menuitem on the View menu","textSelect", "true",
      "Shows the Pan Mouse Mode menuitem on the View menu","panMode", "true",
      "Shows the Fullscreen menuitem on the View menu","Fullscreen", "true",
        "ENDCHILDREN",
      "Shows the Window menu on the menu bar","WindowMenu", "true",
      "Shows the Cascade windows menuitem on the Window menu","Cascade", "true",
      "Shows the Tile windows menuitem on the Window menu","Tile", "true",
        "ENDCHILDREN",
      "Shows the Export menu on the menu bar","ExportMenu", "false",
      "Shows the Pdf menu on the Export menu","PdfMenu", "true",
      "Shows the One per page menuitem on the Pdf menu","Oneperpage", "true",
      "Shows the Nup menuitem on the Pdf menu","Nup", "true",
      "Shows the Handouts menuitem on the Pdf menu","Handouts", "true",
        "ENDCHILDREN",
      "Shows the Content menu on the Export menu","ContentMenu", "true",
      "Shows the Images menuitem on the Content menu","Images", "true",
      "Shows the Text menuitem on the Content menu","Text", "true",
        "ENDCHILDREN",
      "Shows the Bitmap menuitem on the Export menu","Bitmap", "true",
        "ENDCHILDREN",
      "Shows the Page Tools menu on the menu bar","PagetoolsMenu", "false",
      "Shows the Rotate Page menuitem on the Page Tools menu","Rotatepages", "true",
      "Shows the Delete Page menuitem on the Page Tools menu","Deletepages", "true",
      "Shows the Add Page menuitem on the Page Tools menu","Addpage", "true",
      "Shows the Add header and footer menuitem on the Page Tools menu","Addheaderfooter", "true",
      "Shows the stamp text menuitem on the Page Tools menu","Stamptext", "true",
      "Shows the stamp image menuitem on the Page Tools menu","Stampimage", "false",
      "Shows the crop menuitem on the Page Tools menu","Crop", "true",
        "ENDCHILDREN",
      "Shows the Help menu on the menu bar","HelpMenu", "true",
      "Shows the Visit website menuitem on the help menu","Visitwebsite", "true",
      "Shows the tip of the day menuitem on the help menu","Tipoftheday", "true",
      "Shows the check for updates menuitem on the help menu","Checkupdates", "true",
      "Shows the about menuitem on the help menu","About", "true",
        //
        "ENDCHILDREN",
        "ENDCHILDREN",
      "Show the content of the Button bar, button bar remain but empty if false","ButtonsMenu", "true",
      "Show the open file button on the button bar","Openfilebutton", "true",
      "Show the print button on the button bar","Printbutton", "true",
      "Show the search button on the button bar","Searchbutton", "true",
      "Show the document properties button on the button bar","Propertiesbutton", "false",
      "Show the about button on the button bar","Aboutbutton", "false",
      "Show the snapshot button on the button bar","Snapshotbutton", "true",
        //
      "Show the cursor button on the button bar","CursorButton", "true",
      "Show the mouse mode button on the button bar","MouseModeButton", "true",
        "ENDCHILDREN",
      "Show the contents of the display options bar, Display options bar remain empty if false","DisplayOptionsMenu", "true",
      "Show the scaling options on the display options bar","Scalingdisplay", "true",
      "Show the rotation options on the display options bar","Rotationdisplay", "true",
      "Show the image optimisation options on the display options bar","Imageopdisplay", "false",
      "Show the progress bar / display on the display options bar","Progressdisplay", "true",
      "Show the download progress display on the display options bar","Downloadprogressdisplay", "true",
        "ENDCHILDREN",
      "Show the contents of the navigation bar, navigation bar remains but empty if false","NavigationBarMenu", "true",
      "Show the memory diplay on the navigation bar","Memorybottom", "true",
      "Show the first page button on the navigation bar","Firstbottom", "true",
      "Show the back 10 pages button on the navigation bar","Back10bottom", "true",
      "Show the back 1 page button on the navigation bar","Backbottom", "true",
      "Show the goto page button on the navigation bar","Gotobottom", "true",
      "Show the forward 1 page button on the navigation bar","Forwardbottom", "true",
      "Show the forward 10 page button on the navigation bar","Forward10bottom", "true",
      "Show the last page button on the navigation bar","Lastbottom", "true",
      "Show the single page display button on the navigation bar","Singlebottom", "true",
      "Show the continuous page display button on the navigation bar","Continuousbottom", "true",
      "Show the continuous facing page display button on the navigation bar","Continuousfacingbottom", "true",
      "Show the facing page display button on the navigation bar","Facingbottom", "true",
      "Show the pageflow page display button on the navigation bar","PageFlowbottom", "true",
        "ENDCHILDREN",
      "Show the contents of the side tab bar, side tab remain but is empty if false","SideTabBarMenu", "true",
      "Show the page tab, when applicable, on the side tab bar","Pagetab", "true",
      "Show the bookmarks tab, when applicable, on the side tab bar","Bookmarkstab", "true",
      "Show the layers tab, when applicable, on the side tab bar","Layerstab", "true",
      "Show the signatures tab, when applicable, on the side tab bar","Signaturestab", "true",
        "ENDCHILDREN",
      "This removes the menu bar entirely if set to false","ShowMenubar", "true",
      "This removes the button bar entirely if set to false","ShowButtons", "true",
      "This removes the display options bar entirely if set to false","ShowDisplayoptions", "true",
      "This removes the navigation bar entirely if set to false","ShowNavigationbar", "true",
      "This removes the side tab bar entirely if set to false","ShowSidetabbar", "true",
        //			"ENDCHILDREN",
      "The integer RGB value for the highlight color","highlightBoxColor","-16777216",
      "The integer RGB value for the highlighted text color","highlightTextColor","16750900",
      "Flag to replace document text colors with user defined value","replaceDocumentTextColors","false",
      "Integer RGB value for replace text colors","vfgColor","0",
      "Integer RGB value to replace document page color","vbgColor","16777215",
      "All color values (R,G and B), must be under this value in order to change text color","TextColorThreshold","255",
      "Flag to replace the background color of the display pane","replacePdfDisplayBackground","false",
      "Color to use as display pane color if flag is set","pdfDisplayBackground","16777215",
      "Allows text color change to also change color of shapes and line art","changeTextAndLineart","false",
      "Transparency value to be used for the highlight box color","highlightComposite","0.35",
      "This overrides the highlight box color and inverts the color of anything within the highlight area","invertHighlights","false",
      "Flag to open last document upon openning the viewer","openLastDocument","false",
      "Page to open last document to","lastDocumentPage","1",
      "The inset of the page in the play area","pageInsets", "25",
      "The length of the tabbed pane when it has been collapsed","sideTabBarCollapseLength", "32",
      "The length of the tabbed pane when it has been expanded","sideTabBarExpandLength", "190",
      "Keep side tab bar consitent across multiple files, overrides startSideTabOpen on file open","consistentTabBar","false",
      "Flag to allow for the right click menu to be used in the viewer","allowRightClick","true",
      "Flag to allow the mouse scroll wheel to zoom in / out of display","allowScrollwheelZoom","true",
      "Flag to set if the properties file can be modified using the preferences window","readOnly","false",
      "Flag to use enhanced viewer mode","enhancedViewerMode","true",
      "Flag to use enhanced facing mode","enhancedFacingMode","true",
      "Text to use in the window title","windowTitle","",
      "Flag to control if we requestion confirmation to close the viewer","confirmClose","false",
      "Location where the icons to be used by the viewer are stored","iconLocation","/org/jpedal/examples/simpleviewer/res/",
      "Flag to control if we show a message when entering page flow mode","showpageflowmessage","true",
      "Specify a default printer to use","defaultPrinter","",
      "Flag to output additional printer / printing info","debugPrinter","false",
      "Default printing DPI","defaultDPI","600",
      "Default printing page size","defaultPagesize","",
      "List of printers to ignore","printerBlacklist","",
      "Flag to allow the use of hinting for true type fonts","useHinting","false",
      "Voice name to be used for text to speech functionality","voice","kevin16(general domain)",
      "Flag to turn on previews in single page mode when scrolling","previewOnSingleScroll","true",
      "Flag to show the bounding box of the mouse selection","showMouseSelectionBox","false",
      "Flag if we should spearate the cover of document when in facing mode","separateCoverOn","true",
      "Flag to set the JavaFX transition type","transitionType","None",  
        //These variables are used to track some settings between sessions
      "Flag to track the users scaling between sessions","trackScaling","false", //Viewer scaling
      "Scaling value to use on viewer start up, used/modified when tracking between sessions","startScaling","Fit Page", //Set starting scale when file opened
      "Flag to track the users display mode between sessions","trackView","false", //Viewer display mode
      "Display Mode value to use on viewer start up, used/modified when tracking between sessions","startView","1", //Set the starting display mode when file opened
      "Flag to track the if the side bar tab is open between sessions","trackSideTabOpen","false", //Should side bar be open or collapsed
      "Flag if side tab bar should be open on viewer start up and file opening, used/modified when tracking between sessions","startSideTabOpen","false", //Set if the side tab should be exapnded when new file opened
      "Flag to track the currently selected tab on side bar between sessions","trackSelectedSideTab","false", //Selected side bar index
      "Side tab bar to be selected on viewer start up / file openning, used/modified when tracking between sessions","startSelectedSideTab","", //Set tab to this one, if existing when opening new file
      "Flag to track the viewer window size between sessions","trackViewerSize","false", //Viewer size
      "Viewer width to use on viewer start up, used/modified when tracking between sessions","startViewerWidth","-1", //Set to this size, if less than 0 create default width.
      "Viewer height to use on viewer start up, used/modified when tracking between sessions","startViewerHeight","-1", //Set to this size, if less than 0 create default height.
      "Flag to track the side tab bar expanded width between sessions","trackSideTabExpandedSize","false", //Track side tab bar
      "Allow search result list to update during search (SWING VERSION ONLY)","updateResultsDuringSearch","true" //Allow search result list to update during search
    };
    
    public PropertiesFile(){
        
        try {
            final String jarLoc = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            userDir=jarLoc.substring(0, jarLoc.lastIndexOf('/'));
            
            if(!userDir.isEmpty()){ //if userDir is "" we will get /.properties.xml which is not good on Mac/Linux
                configFile=userDir+separator+".properties.xml";
            }else{
                configFile=".properties.xml";
            }
            
            if((DecoderOptions.isRunningOnWindows) &&
                (configFile.length()>1)){
                    configFile = configFile.substring(1);
                    configFile = configFile.replaceAll("\\\\", "/");
                }
            
        } catch (final Exception e) {
            userDir=System.getProperty("user.dir");
            configFile=userDir+separator+".properties.xml";
            //
        }
    }
    
     // Used in debuging. Shows the properties stored in the document file
     // @param indent : String of the indent, this should be empty string
     // @param list : List of nodes
//    static void showChildren(String indent, NodeList list){
//    	for(int i=0; i!=list.getLength(); i++){
//    		System.out.println(indent+""+list.item(i).getNodeType()+" , "+list.item(i).getNodeName()+" , "+list.item(i).getNodeValue());
//    		if(list.item(i).hasChildNodes()){
//    			showChildren(indent+"    ",list.item(i).getChildNodes());
//    		}
//    	}
//    }
    
    public void loadProperties(){
        
        try{
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            File config = null;
            
            if(configInputStream!=null){
                try{
                    doc =  db.parse(configInputStream);
                    isReadOnly= true;
                }catch(final Exception e){
                    doc =  db.newDocument();
                    //
                }
            }else{
                config = new File(configFile);
                if(config.exists() && config.length()>0){
                    try{
                        doc =  db.parse(config);
                    }catch(final Exception e){
                        doc =  db.newDocument();
                        //
                    }
                }else{
                    doc =  db.newDocument();
                }
            }
            
//            showChildren("", doc.getChildNodes());
            
            if(configInputStream==null && (((config!=null && (config.canWrite() || (!config.exists() && !config.canWrite()))) && !getValue("readOnly").toLowerCase().equals("true"))||config.length()==0)){
                isReadOnly= false;
                checkAllElementsPresent();
                
                //If properties is an old version or we are missing elements
                //add missing / reload properties file
                /*if(refactorProperties || !hasAllElements){
                    //Reset to start of properties file
                    position = 0;
                    
                    //Delete old config file
                    config.delete();
                    //config.createNewFile();
                    
                    final Document oldDoc =  (Document)doc.cloneNode(true);
                    doc =  db.newDocument();
                    isReadOnly = !(!isReadOnly && !getValue("readOnly").toLowerCase().equals("true"));
                    checkAllElementsPresent();
                    
                    /**
                     * Move RecentFiles List Over to new properties
                   
                    //New Properties
                    final NodeList newRecentFiles =doc.getElementsByTagName("recentfiles");
                    final Element newRecentRoot=(Element) newRecentFiles.item(0);
                    
                    //Old Properties
                    final NodeList oldRecentFiles =oldDoc.getElementsByTagName("recentfiles");
                    final Element oldRecentRoot=(Element) oldRecentFiles.item(0);
                    
                    //Get children elements
                    final NodeList children = oldRecentRoot.getChildNodes();
                    for(int i=0; i!=children.getLength(); i++){
                        if(!children.item(i).getNodeName().equals("#text")){//Ignore this element
                            final Element e = doc.createElement("file");
                            e.setAttribute("name", ((Element)children.item(i)).getAttribute("name"));
                            newRecentRoot.appendChild(e);
                        }
                    }
                    
                    for(int i=0; i!=properties.length; i++){
                        if(!properties[i].equals("ENDCHILDREN")){
                        	//Ignore comments
                        	i++;
                            final NodeList nl =doc.getElementsByTagName(properties[i]);
                            final Element element=(Element) nl.item(0);
                            if(element==null){
                                ShowGUIMessage.showGUIMessage("The property "+properties[i]+" was either not found in the properties file.", "Property not found.");
                            }else{
                                final NodeList l = oldDoc.getElementsByTagName(properties[i]);
                                final Element el = (Element)l.item(0);
                                if(el!=null) {
                                    element.setAttribute("value", el.getAttribute("value"));
                                }
                            }
                            i++;
                        }
                    }
                    
                    if(!isTest) {
                        writeDoc();
                    }
                }*/
                
                //Check for invalid color options (possible mistake in properties file)
                final String v1 = getValue("vfgColor");
                final String v2 = getValue("vbgColor");
                final String v3 = getValue("sbbgColor");
                
                if(!v1.isEmpty() && !v2.isEmpty() && !v3.isEmpty()){
                    final int value = Integer.parseInt(v1) + Integer.parseInt(v2) + Integer.parseInt(v3);
                    
                    if(value == -3 ){
                        //3 null values, replace with default values
                        setValue("vfgColor", "");
                        setValue("vbgColor", "16777215");
                        setValue("sbbgColor", "16777215");
                    }
                }
                //				//only write out if needed
                //				if(!hasAllElements)
                //					writeDoc();
                
            }else{
                isReadOnly = true;
            }
        }catch(final Exception e){
            LogWriter.writeLog("Exception " + e + " generating properties file");
            //
        }
    }
    
    public String[] getRecentDocuments(){
        final String[] recentDocuments;
        
        try{
            final NodeList nl =doc.getElementsByTagName("recentfiles");
            final List fileNames = new ArrayList();
            
            if(nl != null && nl.getLength() > 0) {
                final NodeList allRecentDocs = ((Element) nl.item(0)).getElementsByTagName("*");
                
                for(int i=0;i<allRecentDocs.getLength();i++){
                    final Node item = allRecentDocs.item(i);
                    final NamedNodeMap attrs = item.getAttributes();
                    fileNames.add(attrs.getNamedItem("name").getNodeValue());
                }
            }
            
            //prune unwanted entries
            while(fileNames.size() > noOfRecentDocs){
                fileNames.remove(0);
            }
            
            Collections.reverse(fileNames);
            
            recentDocuments = (String[]) fileNames.toArray(new String[noOfRecentDocs]);
        }catch(final Exception e){
            //
            LogWriter.writeLog("Exception " + e + " getting recent documents");
            return null;
        }
        
        return recentDocuments;
    }
    
    public void addRecentDocument(final String file){
        try{
            final Element recentElement = (Element) doc.getElementsByTagName("recentfiles").item(0);
            
            checkExists(file, recentElement);
            
            final Element elementToAdd=doc.createElement("file");
            elementToAdd.setAttribute("name",file);
            
            recentElement.appendChild(elementToAdd);
            
            removeOldFiles(recentElement);
            
            //writeDoc();
        }catch(final Exception e){
            LogWriter.writeLog("Exception " + e + " adding recent document to properties file");
            //
        }
    }
    
    public void setValue(final String elementName, final String newValue) {
        
        try {
            final NodeList nl =doc.getElementsByTagName(elementName);
            final Element element=(Element) nl.item(0);
            if(element==null || newValue==null){
                ShowGUIMessage.showGUIMessage("The property "+elementName+" was either not found in the properties file or the value "+newValue+" was not set.", "Property not found.");
            }else{
                element.setAttribute("value",newValue);
            }
            
        }catch(final Exception e){
            LogWriter.writeLog("Exception " + e + " setting value in properties file");
            //
        }
    }
    
    public NodeList getChildren(final String item){
        return doc.getElementsByTagName(item).item(0).getChildNodes();
    }
    
    public String getValue(final String elementName){
        final NamedNodeMap attrs;
        try {
            final NodeList nl =doc.getElementsByTagName(elementName);
            final Element element=(Element) nl.item(0);
            if(element==null) {
                return "";
            }
            attrs = element.getAttributes();
            
        }catch(final Exception e){
            //
            LogWriter.writeLog("Exception " + e + " generating properties file");
            return "";
        }
        
        return attrs.getNamedItem("value").getNodeValue();
    }
    
    private static void removeOldFiles(final Element recentElement) throws Exception{
        final NodeList allRecentDocs = recentElement.getElementsByTagName("*");
        
        while(allRecentDocs.getLength() > noOfRecentDocs){
            recentElement.removeChild(allRecentDocs.item(0));
        }
    }
    
    private static void checkExists(final String file, final Element recentElement) throws Exception{
        final NodeList allRecentDocs = recentElement.getElementsByTagName("*");
        
        for(int i=0;i<allRecentDocs.getLength();i++){
            final Node item = allRecentDocs.item(i);
            final NamedNodeMap attrs = item.getAttributes();
            final String value = attrs.getNamedItem("name").getNodeValue();
            
            if(value.equals(file)) {
                recentElement.removeChild(item);
            }
        }
    }
    
    //
    public void writeDoc() throws Exception{
        
        if(!isReadOnly  && !getValue("readOnly").equalsIgnoreCase("true")){
            final InputStream stylesheet = this.getClass().getResourceAsStream("/org/jpedal/examples/viewer/res/xmlstyle.xslt");
            
            final StreamResult str=new StreamResult(configFile);
            final StreamSource ss=new StreamSource(stylesheet);
            final DOMSource dom= new DOMSource(doc);
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer(ss);
            transformer.transform(dom, str);
            
            stylesheet.close();
            if(ss!=null) {
                ss.getInputStream().close();
            }
            
            //Prevents exception when viewer is closing.
            if(str!=null && str.getOutputStream()!=null) {
                str.getOutputStream().close();
            }
            
        }
        
    }
    
    public void dispose(){
        
        
        doc=null;
        properties=null;
        this.configFile=null;
        
    }

    private void checkAllElementsPresent() throws Exception{

    	//Reset to start of properties file
        position = 0;

        NodeList allElements = doc.getElementsByTagName("*");
        final List elementsInTree=new ArrayList(allElements.getLength());

        for(int i=0;i<allElements.getLength();i++) {
            elementsInTree.add(allElements.item(i).getNodeName());
        }

        final Element propertiesElement;

        if(elementsInTree.contains("properties")){
            propertiesElement = (Element) doc.getElementsByTagName("properties").item(0);
        }else{
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();

            doc =  db.newDocument();

            propertiesElement = doc.createElement("properties");
            doc.appendChild(propertiesElement);

//            allElements = propertiesElement.getChildNodes();//getElementsByTagName("*");
//            elementsInTree=new ArrayList(allElements.getLength());
//
//            for(int i=0;i<allElements.getLength();i++)
//                elementsInTree.add(allElements.item(i).getNodeName());
        }
        if(!elementsInTree.contains("recentfiles")){
            final Element recent = doc.createElement("recentfiles");
            propertiesElement.appendChild(recent);
        }

        allElements = propertiesElement.getChildNodes();

        addProperties(allElements, propertiesElement);

    }

    //Keep track of position in the properties array
    int position;
    
    /**
     * Checks a node list for a given value either in the tags or the comments.
     * @param tree :: The NodeList to be searched
     * @param values :: The value to check for, value must be entire node name
     * @param comments :: Only check the comments if true
     * @return
     */
    private static int checkNodelistForValue(final NodeList tree, final String value, final boolean comments){
    	
    	int nodePosition = -1;
    	for(int i=0; i<tree.getLength(); i++){
    		
    		if(comments){
    			if(tree.item(i).getNodeType()==8 && tree.item(i).getNodeValue().equals(value)){
    				nodePosition = i;
            	}
    		}else{
    			if(tree.item(i).getNodeName().equals(value)){
    				nodePosition = i;
            	}
    		}
        	
    		if(nodePosition==-1 && tree.item(i).hasChildNodes()) {
                nodePosition = checkNodelistForValue(tree.item(i).getChildNodes(), value, comments);
            }
    		
        	if(nodePosition!=-1) {
                i = tree.getLength();
            }
        	
        }
    	
    	return nodePosition;
    	
    }
    
    private void addMenuElement(final NodeList tree, final Element menu){
        
        final int containsNode = checkNodelistForValue(tree, properties[position+1], false);
        final int containsComment = checkNodelistForValue(tree, properties[position], true);
    	
        if(containsNode==-1){
        	
        	menu.appendChild(doc.createComment(properties[position]));
        	position++;
        	
            final Element property = doc.createElement(properties[position]);
            
            //Increment to property value
            position++;
            
            property.setAttribute("value",properties[position]);
            menu.appendChild(property);
            
            //update position in array
            position++;
            
            //Start on children of menu
            addProperties(tree, property);
            
        }else{
        	final Element property = (Element) doc.getElementsByTagName(properties[position+1]).item(0);
            if(containsComment==-1){
            	menu.insertBefore(doc.createComment(properties[position]), tree.item(containsNode));
            }
        	position++;
            position++;
            position++;
            addProperties(tree, property);
        }
    }
    private void addChildElements(final NodeList tree, final Element menu){
        
        if(!properties[position].equals("ENDCHILDREN")){

            final int containsNode = checkNodelistForValue(tree, properties[position+1], false);
            final int containsComment = checkNodelistForValue(tree, properties[position], true);

            if(containsNode==-1){
            	
            	menu.appendChild(doc.createComment(properties[position]));
            	position++;
            	
                final Element property = doc.createElement(properties[position]);
                
                //Increment to property value
                position++;
                
                property.setAttribute("value",properties[position]);
                menu.appendChild(property);
                
            }else{
            	if(containsComment==-1){
                	menu.insertBefore(doc.createComment(properties[position]), tree.item(containsNode));
                }
                position++;
                //Check version number for refactoring and updating
                if(properties[position].equals("currentversion")){
                    
                    //Get store value for the current version
                    final NodeList nl =doc.getElementsByTagName(properties[position]);
                    final Element element=(Element) nl.item(0);
                    
                    if(element==null){
                        //Element not found in tree, should never happen
                        ShowGUIMessage.showGUIMessage("The property "+properties[position]+" was either not found in the properties file.", "Property not found.");
                    }else{
                        
                        //Is it running in the IDE
                        if(properties[position+1].equals("6.6b14")){
                            //Do nothing as we are in the IDE
                            //Refactor for testing purposes
                            //refactorProperties  = true;
                            //							isTest=true;
                            
                        }else{//Check versions of released jar
                            
                            //Program Version
                            final float progVersion = Float.parseFloat(PdfDecoderInt.version.substring(0, 4));
                            
                            //Ensure properties is a valid value
                            String propVer = "0";
                            final String version = element.getAttribute("value");
                            if(version.length()>3) {
                                propVer = version.substring(0, 4);
                            }
                            
                            //Properties Version
                            final float propVersion = Float.parseFloat(propVer);
                            
                            //compare version, only update on newer version
                            if(progVersion>propVersion){
                                element.setAttribute("value", PdfDecoderInt.version);
                            }
                        }
                    }
                }
                
                //Increment passed value to next property
                position++;
            }
        }else{
            endMenu = true;
        }
        position++;
    }
    
    private boolean endMenu;
    
    private void addProperties(final NodeList tree, final Element menu){
        while(position<properties.length){
            //Add menu to properties
            if(properties[position+1].endsWith("Menu")){
                addMenuElement(tree, menu);
            }else{
                addChildElements(tree, menu);
                if(endMenu){
                    endMenu=false;
                    break;
                }
            }
        }
    }
    
    public static int getNoRecentDocumentsToDisplay() {
        return noOfRecentDocs;
    }
    
    public String getConfigFile() {
        return configFile;
    }
    
    public void loadProperties(final InputStream is) {
        
        configInputStream = is;
        
        loadProperties();
    }
    
    public void loadProperties(String configFile) {
        
        if(configFile.startsWith("jar:")){
            configFile = configFile.substring(4);
            
            final InputStream is = this.getClass().getResourceAsStream(configFile);
            
            if(is!=null){
                configInputStream = is;
            }else{
                throw new RuntimeException("unable to open resource stream for "+configFile);
            }
        }else{
            
            //Check if filename is actually a url
            if(configFile.startsWith("http:")){
                try {
                    
                    //Create input stream for the file
                    final URL url = new URL(configFile);
                    final URLConnection con = url.openConnection();
                    con.setDoOutput(true);
                    configInputStream = url.openStream();
                    
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            
            if(configInputStream==null){
            	//If starts with file specification, remove it as not needed or supported.
            	if(configFile.startsWith("file:/")){
            		configFile = configFile.substring(6);
            	}
                final File p = new File(configFile);
                if(p.exists() || (!p.exists() && !p.canWrite())){
                    this.configFile = configFile;
                }else{
                    throw new RuntimeException();
                }
                isReadOnly = !p.canWrite();
            }
        }
        
        loadProperties();
    }
    
    public Document getDoc(){
        return doc;
    }
}
