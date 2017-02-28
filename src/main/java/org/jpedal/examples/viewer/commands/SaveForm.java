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
 * SaveForm.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.utils.FileFilterer;
import org.jpedal.examples.viewer.utils.ItextFunctions;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.acroforms.ReturnValues;
import org.jpedal.utils.Messages;

/**
 * Saves the form and sends data to database server
 */
@SuppressWarnings({"UnusedAssignment","PMD"})
public class SaveForm {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final Values commonValues) {
        if (args == null) {
            saveChangedForm(currentGUI, decode_pdf, commonValues);
        } else {

        }
    }

    /**
     * add listeners to forms to track changes - could also do other tasks like
     * send data to database server
     */
    public static void saveChangedForm(final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final Values commonValues) {
        final org.jpedal.objects.acroforms.AcroRenderer formRenderer = decode_pdf.getFormRenderer();

        if (formRenderer == null) {
            return;
        }

        final Object[] names = formRenderer.getFormComponents(null, ReturnValues.FORM_NAMES, -1);

        if (names == null) {
            currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFields"));
        } else {
            /**
             * create the file chooser to select the file
             */
            File file;
            String fileToSave = "";
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
                    file = chooser.getSelectedFile();
                    fileToSave = file.getAbsolutePath();

                    if (!fileToSave.endsWith(".pdf")) {
                        fileToSave += ".pdf";
                        file = new File(fileToSave);
                    }

                    if (fileToSave.equals(commonValues.getSelectedFile())) {
                        currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.SaveError"));
                        continue;
                    }

                    if (file.exists()) {
                        final int n = currentGUI.showConfirmDialog(fileToSave + '\n'
                                + Messages.getMessage("PdfViewerMessage.FileAlreadyExists") + ".\n"
                                + Messages.getMessage("PdfViewerMessage.ConfirmResave"),
                                Messages.getMessage("PdfViewerMessage.Resave"), JOptionPane.YES_NO_OPTION);
                        if (n == 1) {
                            continue;
                        }
                    }
                    finished = true;
                } else {
                    return;
                }
            }

            final ItextFunctions itextFunctions = new ItextFunctions(currentGUI, commonValues.getSelectedFile(), decode_pdf);
            ItextFunctions.saveFormsData(fileToSave);

            /**
             * reset flag and graphical clue
             */
            commonValues.setFormsChanged(false);
            currentGUI.setViewerTitle(null);

        }
    }
    
    /**
     * warns user forms unsaved and offers save option
     */
    public static void handleUnsaveForms(final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf) {
        // <start-demo><end-demo>
        	//OLD FORM CHANGE CODE
            if(commonValues.isFormsChanged()){
                final int n = currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerFormsUnsavedOptions.message"),Messages.getMessage("PdfViewerFormsUnsavedWarning.message"), JOptionPane.YES_NO_OPTION);
                
                if(n==JOptionPane.YES_OPTION) {
                    SaveForm.saveChangedForm(currentGUI, decode_pdf, commonValues);
                }
            }
            // <start-demo><end-demo>
        commonValues.setFormsChanged(false);
    }
}
