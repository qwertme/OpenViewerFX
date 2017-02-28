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
 * JavaFXCommands.java
 * ---------------
 */
package org.jpedal.examples.viewer;

import java.util.Map;
import javafx.scene.Cursor;
import org.jpedal.FileAccess;
import org.jpedal.PdfDecoderFX;
import org.jpedal.PdfDecoderInt;
import static org.jpedal.examples.viewer.Commands.ABOUT;
import static org.jpedal.examples.viewer.Commands.BACKPAGE;
import static org.jpedal.examples.viewer.Commands.DOCINFO;
import static org.jpedal.examples.viewer.Commands.EXIT;
import static org.jpedal.examples.viewer.Commands.FBACKPAGE;
import static org.jpedal.examples.viewer.Commands.FFORWARDPAGE;
import static org.jpedal.examples.viewer.Commands.FIRSTPAGE;
import static org.jpedal.examples.viewer.Commands.FORWARDPAGE;
import static org.jpedal.examples.viewer.Commands.FULLSCREEN;
import static org.jpedal.examples.viewer.Commands.GOTO;
import static org.jpedal.examples.viewer.Commands.HELP;
import static org.jpedal.examples.viewer.Commands.LASTPAGE;
import static org.jpedal.examples.viewer.Commands.OPENFILE;
import static org.jpedal.examples.viewer.Commands.RSS;
import static org.jpedal.examples.viewer.Commands.TIP;
import static org.jpedal.examples.viewer.Commands.VISITWEBSITE;
import org.jpedal.examples.viewer.commands.*;
import org.jpedal.examples.viewer.commands.generic.Snapshot;

import org.jpedal.examples.viewer.commands.javafx.*;

import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.utils.PrinterInt;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.exception.PdfException;
import org.jpedal.external.JPedalActionHandler;
import org.jpedal.external.Options;
import org.jpedal.gui.GUIFactory;

/**
 *
 * @author Nathan
 */
public class JavaFXCommands extends Commands {

    public JavaFXCommands(final Values commonValues, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final GUIThumbnailPanel thumbnails, final PropertiesFile properties, final GUISearchWindow searchFrame, final PrinterInt currentPrinter) {
        super(commonValues, currentGUI, decode_pdf, thumbnails, properties, searchFrame, currentPrinter);
    }

    @Override
    public Object executeCommand(final int ID, Object[] args) {

        //teat null and Object[]{null} as both null
        if (args != null && args.length == 1 && args[0] == null) {
            args = null;
        }

        Object status = null;

        currentGUI.setExecutingCommand(true);

        final Map jpedalActionHandlers = (Map) decode_pdf.getExternalHandler(Options.JPedalActionHandlers);

        if (jpedalActionHandlers != null) {
            final JPedalActionHandler jpedalAction = (JPedalActionHandler) jpedalActionHandlers.get(ID);
            if (jpedalAction != null) {
                jpedalAction.actionPerformed(currentGUI, this);
                return null;
            }
        }

        if (Viewer.isFX()) {

            // Temp patch to get page nav working
            if (ID >= FIRSTPAGE && ID <= GOTO) {
                commonValues.setPageCount(decode_pdf.getPageCount());
                commonValues.setCurrentPage(decode_pdf.getPageNumber());
            }

            //Execute FX Commands
            switch (ID) {
                case GETPDFNAME: //Used for JavaFX Netbeans PDF Viewer Plugin.
                    status = decode_pdf.getFileName(); //cast to string when using.
                    break;
                    
                case SINGLE:                    
                    ((FileAccess)decode_pdf.getExternalHandler(Options.FileAccess)).setLastPageDecoded(-1);
                    Single.execute(args, decode_pdf, currentGUI);
                    JavaFXTextSelect.execute(args, currentGUI, mouseMode, decode_pdf);
                    break;
                  
                    //
                    
                case SNAPSHOT:
                    extractingAsImage = Snapshot.execute(args, currentGUI, decode_pdf, extractingAsImage); // Snapshot selected area
                    if (extractingAsImage) {
                        ((PdfDecoderFX) decode_pdf).setCursor(Cursor.CROSSHAIR);
                    }
                    break;
                case EXTRACTASIMAGE:
                    JavaFXExtractSelectionAsImage.execute(commonValues, currentGUI, decode_pdf);
                    break;
                   
                case EXTRACTTEXT:
                    JavaFXExtractText.execute(args, currentGUI, decode_pdf, commonValues);
                    break;
                case DESELECTALL:
                    DeSelectAll.execute(currentGUI, decode_pdf);
                    break;
                case SELECTALL:
                    SelectAll.execute(currentGUI, decode_pdf, commonValues);
                    break;
                case COPY:
                    JavaFXCopy.execute(currentGUI, decode_pdf, commonValues);
                    break;
                case FIND:
                    Find.execute(args, commonValues, currentGUI, decode_pdf, searchFrame);
                    break;
                case PREVIOUSRESULT:
                    PreviousResult.execute(args, commonValues, currentGUI, decode_pdf, searchFrame);
                    break;
                case NEXTRESULT:
                    NextResults.execute(args, commonValues, searchFrame, currentGUI, decode_pdf);
                    break;
                case SAVE:
                    JavaFXSaveFile.execute(args, currentGUI, commonValues);
                    break;
                case PREVIOUSDOCUMENT:
                    NavigateDocuments.executePrevDoc(args, currentGUI, commonValues, searchFrame, decode_pdf, properties, thumbnails);
                    break;
                case NEXTDOCUMENT:
                    NavigateDocuments.executeNextDoc(args, currentGUI, commonValues, searchFrame, decode_pdf, properties, thumbnails);
                    break;
                case PREFERENCES:
                    JavaFXPreferences.execute(args, currentGUI);
                     break;
               // case UPDATE:
                //    Update.execute(args, currentGUI);
                case SCALING:
                    JavaFXScaling.execute(args, commonValues, decode_pdf, currentGUI);
                    break;
                case ROTATION:
                    Rotation.execute(args, currentGUI, commonValues);
                    break;
                case PANMODE:
                    JavaFXPanMode.execute(args, currentGUI, mouseMode, decode_pdf);
                    break;
                case TEXTSELECT:
                    JavaFXTextSelect.execute(args, currentGUI, mouseMode, decode_pdf);
                    break;
                case MOUSEMODE:
                    JavaFXMouseModeCommand.execute(args, currentGUI, mouseMode, decode_pdf);
                    break;
                case DOCINFO:
                    JavaFXDocInfo.execute(args, currentGUI, commonValues, decode_pdf);
                    break;
                case TIP:
                    JavaFXTipOfTheDay.execute(args, currentGUI, properties);
                    break;
                case FULLSCREEN:
                    JavaFXFullScreen.execute(args, currentGUI);
                    break;
                case ABOUT:
                    JavaFXInfo.execute(args); //Gets the info box
                    break;
                case VISITWEBSITE:
                    VisitWebsite.execute(args, currentGUI); //takes user to website
                    break;
                case HELP:
                    JavaFXHelp.execute(args); //gets the help box            
                    break;
                case FIRSTPAGE:
                    JavaFXPageNavigator.goFirstPage(args, commonValues, decode_pdf, currentGUI);
                    break;
                case FBACKPAGE:
                    JavaFXPageNavigator.goFBackPage(args, commonValues, decode_pdf, currentGUI);
                    break;
                case BACKPAGE:
                    JavaFXPageNavigator.goBackPage(args, commonValues, decode_pdf, currentGUI);
                    break;
                case FORWARDPAGE:
                    JavaFXPageNavigator.goForwardPage(args, commonValues, decode_pdf, currentGUI);
                    break;
                case FFORWARDPAGE:
                    JavaFXPageNavigator.goFForwardPage(args, commonValues, decode_pdf, currentGUI);
                    break;
                case LASTPAGE:
                    JavaFXPageNavigator.goLastPage(args, commonValues, decode_pdf, currentGUI);
                    break;
                case GOTO:
                    JavaFXPageNavigator.goPage(args, currentGUI, commonValues, decode_pdf);
                    break;
                case OPENFILE:
                    JavaFXOpenFile.executeOpenFile(args, currentGUI, searchFrame, properties, thumbnails, decode_pdf, commonValues);
                    break;
                case OPENURL:
                    JavaFXOpenFile.executeOpenURL(args, commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);
                    break;
                case RSS:
                    JavaFXRSSyndication.execute(args);
                    break;
                case EXIT:
                    JavaFXExit.execute(args, thumbnails, currentGUI, commonValues, decode_pdf, properties);
                    break;
                //
                case SETPAGECOLOR:
                    SetPageColor.execute(args, decode_pdf);
                    break;
                case SETTEXTCOLOR:
                    SetTextColor.execute(args, decode_pdf);
                    break;
                case CHANGELINEART:
                    ChangeLineArt.execute(args, decode_pdf);
                    break;
                case SETREPLACEMENTCOLORTHRESHOLD:
                    SetReplacementThreshold.execute(args, decode_pdf);
                    break;
                default:
                    if(GUI.debugFX) {
                        System.out.println("Command ID " + ID + " not Implemented Yet for JavaFX");
                    }
                    break;
            }
        } else {

            //<start-demo><end-demo>
        }

        //Mark as executed is not running in thread
        if (!currentGUI.isCommandInThread()) {
            currentGUI.setExecutingCommand(false);
        }

        return status;

    }
    
    @Override
    public void openTransferedFile() throws PdfException {
        
        if (currentGUI.isSingle()) {
            decode_pdf.flushObjectValues(true);
        } else {
            //
        }

        JavaFXOpenFile.openFile(commonValues.getSelectedFile(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);


    }
}
