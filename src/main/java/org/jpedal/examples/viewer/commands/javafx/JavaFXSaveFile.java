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
 * JavaFXSaveFile.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXOptionDialog;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * This class displays a dialog which allows the user to save their PDF file as
 * either .pdf or .fdf.
 */
public class JavaFXSaveFile {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final Values commonValues) {
        if (args == null) {
            saveFile(currentGUI, commonValues);
        } else {

        }
    }

    private static void saveFile(final GUIFactory currentGUI, final Values commonValues) {

        /**
         * create the file chooser to select the file
         */
        File file;
        String fileToSave;
        boolean finished = false;

        while (!finished) {

            /**
             * Create a FileChooser object to save file as selected filet-type.
             */
            final FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF file (*.pdf)", "*.pdf"),
                                                     new FileChooser.ExtensionFilter("FDF file (*.fdf)", "*.fdf"));
            
            final String fileName = new File(commonValues.getSelectedFile()).getName();
            
            fileChooser.setInitialFileName(fileName);

            /**
             * Begin file-save process.
             */
            file = fileChooser.showSaveDialog((Stage)currentGUI.getFrame());

            FileInputStream fis = null;
            FileOutputStream fos = null;

            if (file != null) {

                fileToSave = file.getAbsolutePath();

                if (!fileToSave.endsWith(".pdf")) {
                    fileToSave += ".pdf";
                    file = new File(fileToSave);
                }

                if (fileToSave.equals(commonValues.getSelectedFile())) {
                    return;
                }

                /**
                 * Save the File depending on users decision, yes/no.
                 */
                if (file.exists()) {
                    final int n = currentGUI.showConfirmDialog(fileToSave + '\n'
                            + Messages.getMessage("PdfViewerMessage.FileAlreadyExists") + '\n'
                            + Messages.getMessage("PdfViewerMessage.ConfirmResave"),
                            Messages.getMessage("PdfViewerMessage.Resave"), FXOptionDialog.YES_NO_OPTION);
                    if (n == 1) {
                        continue;
                    }
                }

                try {
                    fis = new FileInputStream(commonValues.getSelectedFile());
                    fos = new FileOutputStream(fileToSave);

                    final byte[] buffer = new byte[4096];
                    int bytes_read;

                    while ((bytes_read = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytes_read);
                    }
                } catch (final Exception e1) {
                    
                    //e1.printStackTrace();
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewerException.NotSaveInternetFile")+' '+e1);
                }

                try {
                    fis.close();
                    fos.close();
                } catch (final Exception e2) {
                    //e2.printStackTrace();
                     if(LogWriter.isOutput()) { 
                         LogWriter.writeLog("Exception attempting to Read File: " + e2); 
                     } 
                }
            }
            finished = true;
        }
    }
}
