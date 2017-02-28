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
 * JavaFXExtractText.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.generic.GUICopy;
import org.jpedal.examples.viewer.commands.generic.GUIExtractText;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXMessageDialog;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Messages;

/**
 * Class to Handle the popup dialogs created when user right clicks 
 * highlighted text and chooses text extraction.
 */
public class JavaFXExtractText extends GUIExtractText {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final Values commonValues) {
        if (args == null) {
            extractSelectedText(currentGUI, decode_pdf, commonValues);
        } else {

        }
    }

    /**
     * routine to link GUI into text extraction functions
     */
    private static void extractSelectedText(final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final Values commonValues) {

        if (!decode_pdf.isExtractionAllowed()) {
            currentGUI.showMessageDialog("Not allowed");
            return;
        }

        final int[][] highlights = decode_pdf.getTextLines().getHighlightedAreasAs2DArray(commonValues.getCurrentPage());
        /**
         * ensure co-ords in right order
         */
        if (highlights == null) {
            //
            return;
        }

        final BorderPane border = new BorderPane();
        final FXDialog textExtractionOptions = new FXDialog((Stage)currentGUI.getFrame(), Modality.APPLICATION_MODAL, border, 300, 300);
        textExtractionOptions.setTitle("Text Extraction Options");

        //Set up Radiobuttons
        final HBox radButtons = new HBox();
        final RadioButton rectangle = new RadioButton("Rectangle");
        rectangle.setId("rectangleExtraction");
        final RadioButton table = new RadioButton("Table");
        table.setId("tableExtraction");
        final RadioButton wordList = new RadioButton("WordList");
        wordList.setId("wordListExtraction");
        radButtons.getChildren().addAll(rectangle, table, wordList);

        table.setPadding(new Insets(0,0,0,20));
        wordList.setPadding(new Insets(0,0,0,45));
        border.setTop(radButtons);

        //Set up Selectedtext Area
        final TextArea selection = new TextArea();
        border.setCenter(selection);
        
        //Setup bottom Radio Buttons and Bottom Buttons
        final VBox allBottom = new VBox();
        final HBox bottomButtons = new HBox(15);
        final HBox bottomRadioButtons = new HBox();
        final RadioButton extractAsXML = new RadioButton("Extract as XML");
        final RadioButton extractAsText = new RadioButton("Extract as Text");
        final Button help = new Button("Help");
        final Button cancel = new Button("Cancel");
        cancel.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(final javafx.event.ActionEvent e) {
                textExtractionOptions.close();
            }
        });
        final Button extractButton = new Button("Extract");
        extractButton.setVisible(true); //currently disabled button until window is coded.
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bottomRadioButtons.getChildren().addAll(extractAsXML, spacer, extractAsText);
        bottomButtons.setAlignment(Pos.BOTTOM_RIGHT);
        bottomButtons.getChildren().addAll(help, cancel, extractButton);
        allBottom.getChildren().addAll(bottomRadioButtons, bottomButtons);
        
        extractAsXML.setPadding(new Insets(0,0,0,10));
        extractAsText.setPadding(new Insets(0,10,13,0));
        bottomButtons.setPadding(new Insets(0,5,10,0));
        /**
         * Setup button listeners.
         */
        help.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerGroupingInfo.message"));
            }
        });

        //Add format buttons to a toggle group, so only one be selected.
        final ToggleGroup formatGroup = new ToggleGroup();
        rectangle.setSelected(true);
        formatGroup.getToggles().addAll(rectangle, table, wordList);

        final ChangeListener<Toggle> updateSelectionListener = new ChangeListener<Toggle>() {
            @Override
            public void changed(final ObservableValue<? extends Toggle> ov,
                    final Toggle old_toggle, final Toggle new_toggle) {
                selection.setText(updateSelection(formatGroup.getSelectedToggle(), extractAsXML.isSelected(), decode_pdf, commonValues, currentGUI));
            }
        };

        formatGroup.selectedToggleProperty().addListener(updateSelectionListener);

        //Add extraction mode to a toggle group, so only one be selected.
        final ToggleGroup extractionGroup = new ToggleGroup();
        extractAsText.setSelected(true);
        extractionGroup.getToggles().addAll(extractAsXML, extractAsText);

        extractionGroup.selectedToggleProperty().addListener(updateSelectionListener);

        extractButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                createExtractionWindow(formatGroup.getSelectedToggle(), extractAsXML.isSelected(), decode_pdf, commonValues, currentGUI);
            }
        });

        selection.setText(updateSelection(formatGroup.getSelectedToggle(), extractAsXML.isSelected(), decode_pdf, commonValues, currentGUI));
        border.setBottom(allBottom);
       
        BorderPane.setMargin(selection, new Insets(10,10,10,10));
        BorderPane.setMargin(radButtons, new Insets(10,10,10,10));
        BorderPane.setMargin(bottomRadioButtons, new Insets(10,10,10,10));
        BorderPane.setMargin(bottomButtons, new Insets(10,10,10,10));
        
        textExtractionOptions.show();
       
        
    }

    private static String updateSelection(final Toggle selected, final boolean isXML, final PdfDecoderInt decode_pdf, final Values commonValues, final GUIFactory currentGUI) {
        StringBuilder finalValue = new StringBuilder(""); // Total data extracted so far
        try {

            final PdfPageData page_data = decode_pdf.getPdfPageData();

            final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
            final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
            final int cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
            final int cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());

            final int[][] highlights = decode_pdf.getTextLines().getHighlightedAreasAs2DArray(commonValues.getCurrentPage());
            if (highlights != null) {
                for (int t = 0; t != highlights.length; t++) {
                    String extractedText = "";
                    if (highlights[t] != null) {

                        highlights[t] = GUICopy.adjustHighlightForExtraction(highlights[t]);

                        int t_x1 = highlights[t][0];
                        int t_x2 = highlights[t][0] + highlights[t][2];
                        int t_y1 = highlights[t][1] + highlights[t][3];
                        int t_y2 = highlights[t][1];

                        if (t_y1 < t_y2) {
                            final int temp = t_y2;
                            t_y2 = t_y1;
                            t_y1 = temp;
                        }

                        if (t_x1 > t_x2) {
                            final int temp = t_x2;
                            t_x2 = t_x1;
                            t_x1 = temp;
                        }

                        if (t_x1 < cropX) {
                            t_x1 = cropX;
                        }
                        if (t_x1 > cropX + cropW) {
                            t_x1 = cropX + cropW;
                        }

                        if (t_x2 < cropX) {
                            t_x2 = cropX;
                        }
                        if (t_x2 > cropX + cropW) {
                            t_x2 = cropX + cropW;
                        }

                        if (t_y1 < cropY) {
                            t_y1 = cropY;
                        }
                        if (t_y1 > cropY + cropH) {
                            t_y1 = cropY + cropH;
                        }

                        if (t_y2 < cropY) {
                            t_y2 = cropY;
                        }
                        if (t_y2 > cropY + cropH) {
                            t_y2 = cropY + cropH;
                        }

                        if ("rectangleExtraction".equals(((RadioButton) selected).getId())) {

                            //text extraction
                            extractedText = extractTextRectangle(commonValues, decode_pdf, currentGUI, isXML, t_x1, t_x2, t_y1, t_y2) + ((char) 0x0D) + ((char) 0x0A);
                        } else if ("tableExtraction".equals(((RadioButton) selected).getId())) {
                            //text table extraction
                            extractedText = extractTextTable(commonValues, decode_pdf, isXML, t_x1, t_x2, t_y1, t_y2);
                        } else if ("wordListExtraction".equals(((RadioButton) selected).getId())) {
                            //text wordlist extraction
                            extractedText = extractTextList(decode_pdf, commonValues, currentGUI, isXML, t_x1, t_x2, t_y1, t_y2);
                        }

                        finalValue.append(extractedText);
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return finalValue.toString();
    }

    private static void createExtractionWindow(final Toggle selected, final boolean isXML, final PdfDecoderInt decode_pdf, final Values commonValues, final GUIFactory currentGUI) {

        /**
         * Setup Dialog with Content.
         */
        final Stage extractionDialog = new Stage();

        final TextArea ta = new TextArea();

        final Scene scene = new Scene(ta);
        extractionDialog.setTitle("Extracted Text");

        extractionDialog.setScene(scene);

        ta.setText(updateSelection(selected, isXML, decode_pdf, commonValues, currentGUI));

        extractionDialog.show();

    }
}
