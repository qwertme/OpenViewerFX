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
 * MultiViewTransferHandler.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.jpedal.utils.SwingWorker;

public class MultiViewTransferHandler extends BaseTransferHandler {

	private int fileCount;
	
	public MultiViewTransferHandler(final Values commonValues, final GUIFactory currentGUI, final Commands currentCommands) {
		super(commonValues, currentGUI, currentCommands);
	}

	@Override
    public boolean importData(final JComponent src, final Transferable transferable) {
		try {
			final Object dragImport = getImport(transferable);

			if (dragImport instanceof String) {
				final String url = (String) dragImport;
				System.out.println(url);
				final String testURL = url.toLowerCase();
				if (testURL.startsWith("http:/")) {
					currentCommands.handleTransferedFile(testURL);
					return true;
				} else if (testURL.startsWith("file:/")) {
					final String[] urls = url.split("file:/");
					
					final List files = new LinkedList();
                    for (final String file : urls) {
                        if (!file.isEmpty()) {
                            final File file2 = new File(new URL("file:/" + file).getFile());
                            System.out.println(file2);
                            files.add(file2);
                        }
                    }
					
					return openFiles(files);
				}
			} else if (dragImport instanceof List) {
				final List files = (List) dragImport;
				
				return openFiles(files);
			}
		} catch (final Exception e) {
			//
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Exception attempting to import data " + e);
            }
        }
		
		return false;
	}

	private boolean openFiles(final List files) {
		fileCount = 0;
		final List flattenedFiles = getFlattenedFiles(files, new ArrayList());
		
		if (fileCount == commonValues.getMaxMiltiViewers()) {
			currentGUI.showMessageDialog("You have choosen to import more files than your current set " + 
					"maximum (" + commonValues.getMaxMiltiViewers() + ").  Only the first " + 
					commonValues.getMaxMiltiViewers() + " files will be imported.\nYou can change this value " +
							"in View | Preferences", 
					"Maximum number of files reached", JOptionPane.INFORMATION_MESSAGE);
		}
		
		final List[] filterdFiles = filterFiles(flattenedFiles);
		final List allowedFiles = filterdFiles[0];
		final List disAllowedFiles = filterdFiles[1];
		
		final int noOfDisAllowedFiles = disAllowedFiles.size();
		final int noOfAllowedFiles = allowedFiles.size();
		
		if(noOfDisAllowedFiles > 0) {
			StringBuilder unOpenableFiles = new StringBuilder();
            for (final Object disAllowedFile : disAllowedFiles) {
                final String file = (String) disAllowedFile;
                final String fileName = new File(file).getName();
                unOpenableFiles.append(fileName).append('\n');
            }
			
			final int result = currentGUI.showConfirmDialog("You have selected " + flattenedFiles.size() +
					" files to open.  The following file(s) cannot be opened\nas they are not valid PDFs " +
					"or images.\n" + unOpenableFiles + "\nWould you like to open the remaining " +
					noOfAllowedFiles + " files?", "File Import", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if (result == JOptionPane.NO_OPTION) {
				return false;
			}
		} 
		
		final SwingWorker worker = new SwingWorker() {
			@Override
            public Object construct() {
                for (final Object allowedFile : allowedFiles) {
                    final String file = (String) allowedFile;

                    try {
                        currentCommands.handleTransferedFile(file);
                    } catch (final Exception e) {
                        //

                        final int result;
                        if (allowedFiles.size() == 1) {
                            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerOpenerror"), commonValues.getSelectedFile(), JOptionPane.ERROR_MESSAGE);
                            result = JOptionPane.NO_OPTION;
                        } else {
                            result = currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerOpenerror") + ". Continue opening remaining files?", commonValues.getSelectedFile(),
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                        }

                        final JInternalFrame[] allFrames = ((JDesktopPane)currentGUI.getMultiViewerFrames()).getAllFrames();
                        for (final JInternalFrame internalFrame : allFrames) {
                            if (internalFrame.getTitle().equals(commonValues.getSelectedFile())) {
                                try {
                                    internalFrame.setClosed(true);
                                } catch (final PropertyVetoException f) {
                                    //
                                    if (LogWriter.isOutput()) {
                                        LogWriter.writeLog("Exception attempting getframes" + e);
                                    }
                                }
                                break;
                            }
                        }

                        if (result == JOptionPane.NO_OPTION) {
                            return null;
                        }
                    }
                }
				return null;
			}
		};
		worker.start();

//				
//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {
//						JInternalFrame[] allFrames = currentGUI.getMultiViewerFrames().getAllFrames();
//
//						for (int i = allFrames.length - 1; i >= 0; i--) {
//							JInternalFrame pdf = allFrames[i];
//
//							pdf.updateUI();
//							pdf.repaint();
//							try {
//								pdf.setSelected(true);
//							} catch (PropertyVetoException e) {
//								e.printStackTrace();
//							}
//						}
//						currentGUI.getMultiViewerFrames().repaint();
//					}
//				});
		
		return true;
	}

	private static List[] filterFiles(final List flattenedFiles) {
		final List allowedFiles = new LinkedList();
		final List disAllowedFiles = new LinkedList();

        for (final Object flattenedFile : flattenedFiles) {
            final String file = ((String) flattenedFile);
            final String testFile = file.toLowerCase();

            final boolean isValid = ((testFile.endsWith(".pdf")) || (testFile.endsWith(".fdf")) ||
                    (testFile.endsWith(".tif")) || (testFile.endsWith(".tiff")) ||
                    (testFile.endsWith(".png")) || (testFile.endsWith(".jpg")) ||
                    (testFile.endsWith(".jpeg")));

            if (isValid) {
                allowedFiles.add(file);
            } else {
                disAllowedFiles.add(file);
            }
        }
		
		return new List[] { allowedFiles, disAllowedFiles };
	}

	private List getFlattenedFiles(final List files, final List flattenedFiles) {
        for (final Object file1 : files) {
            if (fileCount == commonValues.getMaxMiltiViewers()) {
                return flattenedFiles;
            }

            final File file = (File) file1;
//			System.out.println(file);
            if (file.isDirectory()) {
                getFlattenedFiles(Arrays.asList(file.listFiles()), flattenedFiles);
            } else {
                flattenedFiles.add(file.getAbsolutePath());

                fileCount++;
            }
        }
		
		return flattenedFiles;
	}

//	protected void openTransferedFile(String file) {
//		String testFile = file.toLowerCase();
//		
//		boolean isValid = ((testFile.endsWith(".pdf"))
//				|| (testFile.endsWith(".fdf")) || (testFile.endsWith(".tif"))
//				|| (testFile.endsWith(".tiff")) || (testFile.endsWith(".png"))
//				|| (testFile.endsWith(".jpg")) || (testFile.endsWith(".jpeg")));
//	
//		if (isValid) {
//			currentCommands.openTransferedFile(file);
//		} else {
//			currentGUI.showMessageDialog("You may only import a valid PDF or image");
//		}
//	}	
}
