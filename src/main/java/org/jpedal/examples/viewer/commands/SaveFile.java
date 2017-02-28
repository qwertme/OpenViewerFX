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
 * SaveFile.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.utils.FileFilterer;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * Saves the current document file
 */
public class SaveFile {

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
            final JFileChooser chooser = new JFileChooser(commonValues.getInputDir());
            chooser.setSelectedFile(new File(commonValues.getInputDir() + '/' + commonValues.getSelectedFile()));
            chooser.addChoosableFileFilter(new FileFilterer(new String[]{"pdf"}, "Pdf (*.pdf)"));
            chooser.addChoosableFileFilter(new FileFilterer(new String[]{"fdf"}, "fdf (*.fdf)"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            //set default name to current file name
            final int approved = chooser.showSaveDialog(null);
            if (approved == JFileChooser.APPROVE_OPTION) {

                FileInputStream fis = null;
                FileOutputStream fos = null;

                file = chooser.getSelectedFile();
                fileToSave = file.getAbsolutePath();

                if (!fileToSave.endsWith(".pdf")) {
                    fileToSave += ".pdf";
                    file = new File(fileToSave);
                }

                if (fileToSave.equals(commonValues.getSelectedFile())) {
                    return;
                }

                if (file.exists()) {
                    final int n = currentGUI.showConfirmDialog(fileToSave + '\n'
                            + Messages.getMessage("PdfViewerMessage.FileAlreadyExists") + '\n'
                            + Messages.getMessage("PdfViewerMessage.ConfirmResave"),
                            Messages.getMessage("PdfViewerMessage.Resave"), JOptionPane.YES_NO_OPTION);
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
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewerException.NotSaveInternetFile")+ ' ' +e1);
                }

                try {
                    fis.close();
                    fos.close();
                } catch (final IOException e2) {
                    //e2.printStackTrace();
                    if(LogWriter.isOutput()) { 
                         LogWriter.writeLog("Exception attempting close IOStream: " + e2); 
                     } 
                }

                finished = true;
            } else {
                return;
            }
        }
    }
}
