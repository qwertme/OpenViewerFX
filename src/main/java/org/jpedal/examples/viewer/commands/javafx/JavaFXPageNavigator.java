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
 * JavaFXPageNavigator.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

// Used for page turning mode
import java.awt.Point;
import org.jpedal.*;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.*;
import org.jpedal.examples.viewer.gui.GUI.PageCounter;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXMessageDialog;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.TiffHelper;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * This class controls the different methods that allow you to navigate a
 * document by page, its critical method is navigatePages()
 */
public class JavaFXPageNavigator {

    /**
     * whether page turn is currently animating
     */
    private static boolean pageTurnAnimating;

    /**
     * flag to track if page decoded twice
     */
    private static int lastPageDecoded = -1;

    /**
     * Objects required to load Tiff 
     */
    private static TiffHelper tiffHelper;

    /**
     * Flag to prevent page changing is page changing currently taking place (prevent viewer freezing)
     */
    private static boolean pageChanging;
    
    public static void gotoPage(String page, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf) {
        int newPage;

        page = page.split("/")[0];

        //allow for bum values
        try {
            newPage = Integer.parseInt(page);

            //if loading on linearized thread, see if we can actually display
            if (!decode_pdf.isPageAvailable(newPage)) {
                currentGUI.showMessageDialog("Page " + newPage + " is not yet loaded");
                currentGUI.setPageCounterText(PageCounter.PAGECOUNTER2, String.valueOf(commonValues.getCurrentPage()));
                return;
            }

            /**
             * adjust for double jump on facing
             */
            if (decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING) {
                if ((decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER) || decode_pdf.getDisplayView() != Display.FACING) && (newPage & 1) == 1 && newPage != 1) {
                    newPage--;
                } else if (!decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER) && (newPage & 1) == 0) {
                    newPage--;
                }
            }

            //allow for invalid value
            if ((newPage > decode_pdf.getPageCount()) | (newPage < 1)) {

                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPageLabel.text") + ' '
                        + page + ' ' + Messages.getMessage("PdfViewerOutOfRange.text") + ' ' + decode_pdf.getPageCount());

                newPage = commonValues.getCurrentPage();

                currentGUI.setPageNumber();
            }

        } catch (final Exception e) {
            currentGUI.showMessageDialog('>' + page + "< " + Messages.getMessage("PdfViewerInvalidNumber.text")+' '+e);
            newPage = commonValues.getCurrentPage();
            currentGUI.setPageCounterText(PageCounter.PAGECOUNTER2, String.valueOf(commonValues.getCurrentPage()));
        }

        navigatePages(newPage - commonValues.getCurrentPage(), commonValues, decode_pdf, currentGUI);

        if (decode_pdf.getDisplayView() == Display.PAGEFLOW) {
            navigatePages(0, commonValues, decode_pdf, currentGUI);
        }

    }

    public static void goPage(final Object[] args, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf) {
        if (args == null) {
            final String page = currentGUI.showInputDialog(Messages.getMessage("PdfViewer.EnterPageNumber"), Messages.getMessage("PdfViewer.GotoPage"), FXMessageDialog.QUESTION_MESSAGE);
            if (page != null) {
                gotoPage(page, currentGUI, commonValues, decode_pdf);
            }
        } else {
            gotoPage((String) args[0], currentGUI, commonValues, decode_pdf);
        }
    }

    public static void goLastPage(final Object[] args, final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI) {
        if (args == null) {
            if ((commonValues.getSelectedFile() != null) && (commonValues.getPageCount() > 1) && (commonValues.getPageCount() - commonValues.getCurrentPage() > 0)) //					forward(commonValues.getPageCount() - commonValues.getCurrentPage());
            {
                navigatePages(commonValues.getPageCount() - commonValues.getCurrentPage(), commonValues, decode_pdf, currentGUI);
            }
        } else {

        }
    }

    public static void goFirstPage(final Object[] args, final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI) {
        if (args == null) {
            if ((commonValues.getSelectedFile() != null) && (commonValues.getPageCount() > 1) && (commonValues.getCurrentPage() != 1)) //					back(commonValues.getCurrentPage() - 1);
            {
                navigatePages(-(commonValues.getCurrentPage() - 1), commonValues, decode_pdf, currentGUI);
            }
        } else {

        }
    }

    public static void goFForwardPage(final Object[] args, final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI) {
        if (args == null && commonValues.getSelectedFile() != null){
                if (commonValues.getPageCount() < commonValues.getCurrentPage() + 10) //						forward(commonValues.getPageCount()-commonValues.getCurrentPage());
                {
                    navigatePages(commonValues.getPageCount() - commonValues.getCurrentPage(), commonValues, decode_pdf, currentGUI);
                } else {
                    navigatePages(10, commonValues, decode_pdf, currentGUI);
                }
            }      
    }

    public static void goForwardPage(final Object[] args, final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI) {
        if (args == null) {
            if (commonValues.getSelectedFile() != null) //					forward(1);
            {
                navigatePages(1, commonValues, decode_pdf, currentGUI);
            }
            else if(decode_pdf.getPageCount() > 0){
                if(commonValues.getPageCount() != decode_pdf.getPageCount()) {
                    commonValues.setPageCount(decode_pdf.getPageCount());
                }
                navigatePages(1, commonValues, decode_pdf, currentGUI);
            }
        } else {
            if (commonValues.getSelectedFile() != null) //					forward(Integer.parseInt((String) args[0]));
            {
                navigatePages(Integer.parseInt((String) args[0]), commonValues, decode_pdf, currentGUI);
            }
            while (Values.isProcessing()) {
                //Wait while pdf is loading
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Attempting to set propeties values " + e);
                    }
                }
            }
        }
    }

    public static void goBackPage(final Object[] args, final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI) {
        if (args == null) {
            if (commonValues.getSelectedFile() != null) //					back(1);
            {
                navigatePages(-1, commonValues, decode_pdf, currentGUI);
            }
        } else {
            if (commonValues.getSelectedFile() != null) //					back(Integer.parseInt((String) args[0]));
            {
                navigatePages(-Integer.parseInt((String) args[0]), commonValues, decode_pdf, currentGUI);
            }
            while (Values.isProcessing()) {
                //Wait while pdf is loading
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Attempting to set propeties values " + e);
                    }
                }
            }
        }
    }

    public static void goFBackPage(final Object[] args, final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI) {
        if (args == null) {
            if (commonValues.getSelectedFile() != null) {
                if (commonValues.getCurrentPage() <= 10) //						back(commonValues.getCurrentPage() - 1);
                {
                    navigatePages(-(commonValues.getCurrentPage() - 1), commonValues, decode_pdf, currentGUI);
                } else //						back(10);
                {
                    navigatePages(-10, commonValues, decode_pdf, currentGUI);
                }
            }
        } else {

        }
    }

    public static void navigatePages(int count, final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI) {
        if (count == 0) {
            return;
        }

        if(!pageChanging){
			pageChanging = true;
			//Facing modes need to move at least by 2 pages others page will not change
			if (decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING) {

				if (count == -1 && commonValues.getCurrentPage() != 2) {
					count = -2;
				}

				if (count == 1 && commonValues.getCurrentPage() != decode_pdf.getPageCount() - 1) {
					count = 2;
				}
			}

			//new page number
			int updatedTotal = commonValues.getCurrentPage() + count;
			if (count > 0) {
				/**
				 * example code to show how to check if page is now available
				 */
				//if loading on linearized thread, see if we can actually display
				if (!decode_pdf.isPageAvailable(updatedTotal)) {
					currentGUI.showMessageDialog("Page " + updatedTotal + " is not yet loaded");
					pageChanging = false;
					return;
				}

				if (!Values.isProcessing()) { //lock to stop multiple accesses

					/**
					 * if in range update count and decode next page. Decoded pages
					 * are cached so will redisplay almost instantly
					 */
					if (updatedTotal <= commonValues.getPageCount()) {

						if (commonValues.isMultiTiff()) {

							//Update page number and draw new page
							commonValues.setTiffImageToLoad((lastPageDecoded - 1) + count);
							drawMultiPageTiff(commonValues, decode_pdf);

							//Update Tiff page
							commonValues.setCurrentPage(updatedTotal);
							lastPageDecoded = commonValues.getTiffImageToLoad() + 1;
							currentGUI.setPageNumber();

							//Display new page
                            //

						} else {
							/**
							 * adjust for double jump on facing
							 */
							if (decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING) {
								if (decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER) || decode_pdf.getDisplayView() != Display.FACING) {
									//                                updatedTotal++;

									if (updatedTotal > commonValues.getPageCount()) {
										updatedTotal = commonValues.getPageCount();
									}

									if ((updatedTotal & 1) == 1 && updatedTotal != 1) {
										updatedTotal--;
									}

									if (decode_pdf.getDisplayView() == Display.FACING) {
										count = ((updatedTotal) / 2) - ((commonValues.getCurrentPage()) / 2);
									}
								} else {
									//                                updatedTotal++;

									if ((updatedTotal & 1) == 0) {
										updatedTotal--;
									}

									count = ((updatedTotal + 1) / 2) - ((commonValues.getCurrentPage() + 1) / 2);
								}
							}

                            //
                            {
								commonValues.setCurrentPage(updatedTotal);
								//							currentGUI.setPageNumber();

								if (decode_pdf.getDisplayView() == Display.CONTINUOUS
										|| decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING) {

									currentGUI.decodePage();

									//Added here else number not updated
									currentGUI.setPageNumber();

									pageChanging = false;
									return;
								}

								currentGUI.resetStatusMessage("Loading Page " + commonValues.getCurrentPage());
								/**
								 * reset as rotation may change!
								 */
								decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());

								//decode the page
								if (commonValues.isPDF()) {
									currentGUI.decodePage();
								}

								//if scaling to window reset screen to fit rotated page
								//						if(currentGUI.getSelectedComboIndex(Commands.SCALING)<3)
								//						currentGUI.zoom();
							}
						}
					}
				} else {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
				}

			} else {
				//if loading on linearized thread, see if we can actually display
				if (!decode_pdf.isPageAvailable(updatedTotal)) {
					currentGUI.showMessageDialog("Page " + updatedTotal + " is not yet loaded");
					pageChanging = false;
					return;
				}

				if (!Values.isProcessing()) { //lock to stop multiple accesses

					/**
					 * if in range update count and decode next page. Decoded pages
					 * are cached so will redisplay almost instantly
					 */
					if (updatedTotal >= 1) {

						if (commonValues.isMultiTiff()) {

							//Update page number and draw new page
							commonValues.setTiffImageToLoad((lastPageDecoded - 1) + count);
							drawMultiPageTiff(commonValues, decode_pdf);

							//Update Tiff page
							commonValues.setCurrentPage(updatedTotal);
							lastPageDecoded = commonValues.getTiffImageToLoad() + 1;
							currentGUI.setPageNumber();

							//Display new page
                            //

						}
                        else {

							/**
							 * adjust for double jump on facing
							 */
							if (decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING) {
								if (decode_pdf.getPages().getBoolean(Display.BoolValue.SEPARATE_COVER) || decode_pdf.getDisplayView() != Display.FACING) {
									if (count == -1) {
										updatedTotal--;
									}

									if (updatedTotal < 1) {
										updatedTotal = 1;
									}

									if ((updatedTotal & 1) == 1 && updatedTotal != 1) {
										updatedTotal--;
									}

									if (decode_pdf.getDisplayView() == Display.FACING) {
										count = ((updatedTotal) / 2) - ((commonValues.getCurrentPage()) / 2);
									}
								} else {
									if ((updatedTotal & 1) == 0) {
										updatedTotal--;
									}

									if (decode_pdf.getDisplayView() == Display.FACING) {
										count = ((updatedTotal + 1) / 2) - ((commonValues.getCurrentPage() + 1) / 2);
									}
								}
							}

                            //
                            {
								commonValues.setCurrentPage(updatedTotal);
								//							currentGUI.setPageNumber();

								if (decode_pdf.getDisplayView() == Display.CONTINUOUS
										|| decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING) {

									currentGUI.decodePage();

									//Added here else number not updated
									currentGUI.setPageNumber();

									pageChanging = false;
									return;
								}

								currentGUI.resetStatusMessage("loading page " + commonValues.getCurrentPage());

								/**
								 * reset as rotation may change!
								 */
								decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());

								//would reset scaling on page change to default
								//currentGUI.setScalingToDefault(); //set to 100%
								if (commonValues.isPDF()) {
									currentGUI.decodePage();
								}

								//if scaling to window reset screen to fit rotated page
								//if(currentGUI.getSelectedComboIndex(Commands.SCALING)<3)
								//	currentGUI.zoom();
							}
						}
					}
				} else {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
				}

			}

			//Ensure thumbnail scroll bar is updated when page changed
			if (currentGUI.getThumbnailScrollBar() != null) {
                currentGUI.setThumbnailScrollBarValue(commonValues.getCurrentPage() - 1);
			}

			//After changing page, ensure buttons are updated, redundent buttons are hidden
			currentGUI.getButtons().hideRedundentNavButtons(currentGUI);

			currentGUI.setPageNumber();

			pageChanging = false;
		}
    }

    public static void drawMultiPageTiff(final Values commonValues, final PdfDecoderInt decode_pdf) {

        //
    }

    public static void setPageTurnAnimating(final boolean a, final GUIFactory currentGUI) {
        pageTurnAnimating = a;

        //disable buttons during animation
        if (a) {
            currentGUI.getButtons().getButton(Commands.FORWARDPAGE).setEnabled(false);
            currentGUI.getButtons().getButton(Commands.BACKPAGE).setEnabled(false);
            currentGUI.getButtons().getButton(Commands.FFORWARDPAGE).setEnabled(false);
            currentGUI.getButtons().getButton(Commands.FBACKPAGE).setEnabled(false);
            currentGUI.getButtons().getButton(Commands.LASTPAGE).setEnabled(false);
            currentGUI.getButtons().getButton(Commands.FIRSTPAGE).setEnabled(false);
        } else {
            currentGUI.getButtons().hideRedundentNavButtons(currentGUI);
        }
    }

    public static boolean getPageTurnAnimating() {
        return pageTurnAnimating;
    }

    public static void setLastPageDecoded(final int x) {
        lastPageDecoded = x;
    }

    public static TiffHelper getTiffHelper() {
        return tiffHelper;
    }

    public static void setTiffHelper(final TiffHelper tiffHelp) {
        tiffHelper = tiffHelp;
    }
    
}
