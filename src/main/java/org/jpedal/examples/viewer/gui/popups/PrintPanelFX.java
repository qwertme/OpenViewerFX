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
 * PrintPanelFX.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;

import java.text.DecimalFormat;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.SetOfIntegerSyntax;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrinterResolution;

import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.paper.MarginPaper;
import org.jpedal.examples.viewer.paper.PaperSizes;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.objects.PrinterOptions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

public class PrintPanelFX extends Pane implements PrintPanelInt{
	
	private FXDialog printPopup;
    private TabPane tabs;
    private final BorderPane border = new BorderPane();
    private final Tab printer = new Tab("Printer");
    private final Tab range = new Tab("Range");
    private final Tab handling = new Tab("Handling");
    
    //Margin order Left, Top, Right, Bottom
    private final float[] margins = new float[4];
    
    private final PdfDecoderInt pdf;
    
    public PrintPanelFX(final String[] printersList, final String defaultPrinter, final PaperSizes paperDefinitions, final int defaultResolution, final int pageNumber, final PdfDecoderInt decoder) {

        pdf = decoder;
        pageCount = pdf.getPageCount();
        currentPageNo = pageNumber;

        this.defaultResolution = defaultResolution;

        this.paperDefinitions = paperDefinitions;

    	resetDefaults(printersList, defaultPrinter, pageCount, currentPageNo);
        
    }

    /**
     * Setting up the stage
     */
    private void setupStage() {

        printPopup = new FXDialog(null, Modality.APPLICATION_MODAL, border, 700, 300);
        printPopup.setTitle("Print");
        initPreview();
        bottomButtons();
        printPopup.show();
        printPopup.setResizeable(false);
    }
    
    Button ok;
    Button cancel;
    
    /**
     * Adding the bottom buttons onto the stage
     */
    private void bottomButtons() {
        final HBox bottomButtons = new HBox(5);
        ok = new Button();
        ok.setText(Messages.getMessage("PdfMessage.Ok"));
        ok.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(final javafx.event.ActionEvent e) {
                okEvent();
            }
        });
        
        cancel = new Button();
        cancel.setText(Messages.getMessage("PdfMessage.Cancel"));
        bottomButtons.getChildren().addAll(ok, cancel);
        cancel.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(final javafx.event.ActionEvent e) {
                cancelEvent();
            }
        });
        bottomButtons.setAlignment(Pos.BOTTOM_RIGHT);
        bottomButtons.setPadding(new Insets(10, 10, 10, 0));
        border.setBottom(bottomButtons);
    }
    /**
     * initializing preview at the side.
     */
    private void initPreview() {
        final VBox previewBox = new VBox();
        final Label preview = new Label("Preview");
        preview.setPadding(new Insets(0, 0, 0, 75));
        final Slider page = new Slider();
        page.setPadding(new Insets(190, 0, 0, 0));
        previewBox.setPrefWidth(50d);
        previewBox.getChildren().addAll(preview, page);
        previewBox.setStyle("-fx-border-style: solid;"
            + "-fx-border-width: 0.5;"
            + "-fx-border-color: black");
        border.setCenter(previewBox);

    }
    /**
     * Setting up all of the tabs on the tab panel
     */
    private void setupTabs() {
        final HBox tabsList = new HBox();
        tabs = new TabPane();
        tabsList.getChildren().addAll(tabs);
        //Making the tabs not have the option to be closed
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        initprintTab();
        initRangeTab();
        initHandlingTab();
        border.setLeft(tabsList);
        //Make the tabs have no background 
        tabs.getStyleClass().add("floating");

    }
    
    ComboBox<String> printers;
    ComboBox pagesOptions;
    ComboBox resolutionOptions;

    TextField leftT;
    TextField rightT;
    TextField topT;
    TextField bottomT;
    
    /**
     * initializing the print Tab
     */
    private void initprintTab() {
        final HBox pane = new HBox();
        final GridPane grid = new GridPane();

        printers = new ComboBox<String>(FXCollections.observableArrayList(printersList));
        printers.valueProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> arg0,
					final String arg1, final String arg2) {
//                if (debugPrinterChange)
//                    System.out.println("itemStateChanged");
//                previewComponent.repaint();
                if (debugPrinterChange) {
                    System.out.println("repainted preview component");
                }
                ok.setDisable(true);
                pagesOptions.setDisable(true);
                //
                pagesOptions.setItems(FXCollections.observableArrayList("Loading..."));
                resolutionOptions.setDisable(true);
                resolutionOptions.setItems(FXCollections.observableArrayList("Loading..."));
                if (debugPrinterChange) {
                    System.out.println("GUI options disabled");
                }
                
                        if (debugPrinterChange) {
                            System.out.println("Thread invoked.");
                        }
                        final PrintService[] ps = PrintServiceLookup.lookupPrintServices(null,null);
                        if (debugPrinterChange) {
                            System.out.println("Found print services.");
                        }
                        PrintService p=null;
                        for (final PrintService p1 : ps) {
                            if (debugPrinterChange) {
                                System.out.println("checking " + p1.getName());
                            }
                            if (p1.getName().equals(printers.getSelectionModel().getSelectedItem())) {
                                p = p1;
                                if (debugPrinterChange) {
                                    System.out.println("Match!");
                                }
                            }
                        }

                        if (p!=null) {
                            if (debugPrinterChange) {
                                System.out.println("Getting available resolutions...");
                            }
                            resolutionOptions.setItems(FXCollections.observableArrayList(getAvailableResolutions(p)));
                            if (debugPrinterChange) {
                                System.out.println("Getting default resolution...");
                            }
                            final int resInd = getDefaultResolutionIndex();
                            if (resolutionOptions.getItems().size() > resInd) {
                                resolutionOptions.getSelectionModel().select(resInd);
                            }
                            resolutionOptions.setDisable(false);
                            paperDefinitions.setPrintService(p);
                            if (debugPrinterChange) {
                                System.out.println("Getting available paper sizes...");
                            }
                            pagesOptions.setItems(FXCollections.observableArrayList(getAvailablePaperSizes()));
                            if (debugPrinterChange) {
                                System.out.println("Getting default pagesize...");
                            }
                            final int pageInd = paperDefinitions.getDefaultPageIndex();
                            if (pagesOptions.getItems().size() > pageInd) {
                                pagesOptions.getSelectionModel().select(pageInd);
                            }
                            pagesOptions.setDisable(false);
                            ok.setDisable(false);
                            //
                            if (debugPrinterChange) {
                                System.out.println("Reenabled GUI");
                            }
                        }

                        if (debugPrinterChange) {
                            System.out.println("Updating margins");
                        }
                        updateMargins();

                    } 
		});
        
        pagesOptions = new ComboBox();
        resolutionOptions = new ComboBox();

        leftT = new TextField();
        leftT.textProperty().addListener(new ChangeListener<String>() {

        	@Override
        	public void changed(final ObservableValue<? extends String> observable,
        			final String oldValue, final String newValue) {
        		if(newValue.matches("[0-9]*\\.?[0-9]*")) {
                    leftT.setText(newValue);
                } else {
                    leftT.setText(oldValue);
                }
        	}
        });
        leftT.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(!arg2.booleanValue()){
					double page = 0.0;
                    
                    if(!leftT.getText().isEmpty()){
                        page = Double.parseDouble(leftT.getText());
                    }
                    
					if(page<minimumMargins[0]) {
                        page = minimumMargins[0];
                    }
					leftT.setText(df.format(page));
                    margins[0] = Float.parseFloat(leftT.getText());
				}
			}
        	
        });
        
        rightT = new TextField();
        rightT.textProperty().addListener(new ChangeListener<String>() {

        	@Override
        	public void changed(final ObservableValue<? extends String> observable,
        			final String oldValue, final String newValue) {
        		if(newValue.matches("[0-9]*\\.?[0-9]*")) {
                    rightT.setText(newValue);
                } else {
                    rightT.setText(oldValue);
                }
        	}
        });
        rightT.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(!arg2.booleanValue()){
					double page = 0.0;
                    
                    if(!rightT.getText().isEmpty()){
                        page = Double.parseDouble(rightT.getText());
                    }
                    if(page<minimumMargins[2]) {
                        page = minimumMargins[2];
                    }
					rightT.setText(df.format(page));
                    margins[2] = Float.parseFloat(rightT.getText());
				}
			}
        	
        });
        
        topT = new TextField();
        topT.textProperty().addListener(new ChangeListener<String>() {

        	@Override
        	public void changed(final ObservableValue<? extends String> observable,
        			final String oldValue, final String newValue) {
        		if(newValue.matches("[0-9]*\\.?[0-9]*")) {
                    topT.setText(newValue);
                } else {
                    topT.setText(oldValue);
                }
        	}
        });
        topT.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(!arg2.booleanValue()){
					double page = 0.0;
                    
                    if(!topT.getText().isEmpty()){
                        page = Double.parseDouble(topT.getText());
                    }
                    
					if(page<minimumMargins[1]) {
                        page = minimumMargins[1];
                    }
					topT.setText(df.format(page));
                    margins[1] = Float.parseFloat(topT.getText());
				}
			}
        	
        });
        
        bottomT = new TextField();
        bottomT.textProperty().addListener(new ChangeListener<String>() {

        	@Override
        	public void changed(final ObservableValue<? extends String> observable,
        			final String oldValue, final String newValue) {
        		if(newValue.matches("[0-9]*\\.?[0-9]*")) {
                    bottomT.setText(newValue);
                } else {
                    bottomT.setText(oldValue);
                }
        	}
        });
        bottomT.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(!arg2.booleanValue()){
					double page = 0.0;
                    
                    if(!bottomT.getText().isEmpty()){
                        page = Double.parseDouble(bottomT.getText());
                    }
                    
					if(page<minimumMargins[3]) {
                        page = minimumMargins[3];
                    }
					
					bottomT.setText(df.format(page));
                    margins[3] = Float.parseFloat(bottomT.getText());
				}
			}
        	
        });

        final Label name = new Label();
        name.setText(Messages.getMessage("PdfViewerPrint.Name"));
        final Label pageSize = new Label();
        pageSize.setText(Messages.getMessage("PdfViewerPrint.PageSize"));
        final Label margins = new Label();
        margins.setText(Messages.getMessage("PdfViewerPrintMargins.bottom"));
        final Label left = new Label();
        left.setText(Messages.getMessage("PdfViewerPrintMargins.left"));
        final Label right = new Label();
        right.setText(Messages.getMessage("PdfViewerPrintMargins.right"));
        final Label top = new Label();
        top.setText(Messages.getMessage("PdfViewerPrintMargins.top"));
        final Label bottom = new Label();
        bottom.setText(Messages.getMessage("PdfViewerPrintMargins.bottom"));
        final Label resolution = new Label();
        resolution.setText(Messages.getMessage("PdfViewerPrintResolution.text"));

        grid.setPadding(new Insets(20, 0, 0, 0));
        grid.setVgap(6d);
        //Adding all of the content to the grid on the page.
        grid.add(name, 0, 0);
        grid.add(printers, 1, 0, 4, 1);
        grid.add(pageSize, 0, 1);
        grid.add(pagesOptions, 1, 1, 4, 1);
        grid.add(margins, 0, 2);
        grid.add(left, 1, 2);
        grid.add(leftT, 2, 2);
        grid.add(right, 3, 2);
        grid.add(rightT, 4, 2);
        grid.add(top, 1, 3);
        grid.add(topT, 2, 3);
        grid.add(bottom, 3, 3);
        grid.add(bottomT, 4, 3);
        grid.add(resolution, 0, 4);
        grid.add(resolutionOptions, 1, 4, 4, 1);

        pane.getChildren().addAll(grid);
        tabs.getTabs().add(printer);
        printer.setContent(grid);
    }
    
    RadioButton all;// = new RadioButton("All");
    RadioButton currentView;// = new RadioButton("Current View");
    RadioButton currentPage;// = new RadioButton("Current Page");
    RadioButton pagesFrom;// = new RadioButton("Pages From:");
    TextField numberFrom;// = new TextField();
    TextField numberTo;// = new TextField();
    ComboBox subSet;// = new ComboBox();
    CheckBox reverse;// = new CheckBox();
        
    /**
     * Initalizing the Range Tab
     */
    private void initRangeTab() {
        final HBox rangeTab = new HBox();
        final GridPane rangeGrid = new GridPane();
        rangeGrid.setPadding(new Insets(20, 0, 0, 0));
        final ToggleGroup group = new ToggleGroup();
        
        all = new RadioButton("All");
        all.setSelected(true);
        all.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(arg2.booleanValue()){
                    allStateChanged();
                }
			}
        	
        });
        all.setToggleGroup(group);
        
        currentView = new RadioButton("Current View");
        currentView.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(arg2.booleanValue()){
                    printRangeCurrentView();
                }
			}
        	
        });
        currentView.setToggleGroup(group);
        
        currentPage = new RadioButton("Current Page");
        currentPage.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(arg2.booleanValue()){
                    currentPageStateChanged();
                }
			}
        	
        });
        currentPage.setToggleGroup(group);
        
        pagesFrom = new RadioButton("Pages From:");
        pagesFrom.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(arg2.booleanValue()){
                    pagesFromStateChanged();
                }
			}
        	
        });
        pagesFrom.setToggleGroup(group);
        
                
        numberFrom = new TextField();
        numberFrom.textProperty().addListener(new ChangeListener<String>() {

        	@Override
        	public void changed(final ObservableValue<? extends String> observable,
        			final String oldValue, final String newValue) {
        		if(newValue.matches("\\d*")) {
                    numberFrom.setText(newValue);
                } else {
                    numberFrom.setText(oldValue);
                }
        	}
        });
        numberFrom.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(!arg2.booleanValue()){
					int page = 1;
                    
                    if(!numberFrom.getText().isEmpty()){
                        page = Integer.parseInt(numberFrom.getText());
                    }
                    
					if(page>pdf.getPageCount()) {
                        page = pdf.getPageCount();
                    }
					numberFrom.setText(String.valueOf(page));
				}else{
                    pagesBoxPressed();
                }
			}
        	
        });
        
        numberTo = new TextField();
        numberTo.textProperty().addListener(new ChangeListener<String>() {

        	@Override
        	public void changed(final ObservableValue<? extends String> observable,
        			final String oldValue, final String newValue) {
        		if(newValue.matches("\\d*")) {
                    numberTo.setText(newValue);
                } else {
                    numberTo.setText(oldValue);
                }
        	}
        });
        numberTo.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(final ObservableValue<? extends Boolean> arg0,
					final Boolean arg1, final Boolean arg2) {
				if(!arg2.booleanValue()){
					int page = pdf.getPageCount();
                    
                    if(!numberTo.getText().isEmpty()){
                        page = Integer.parseInt(numberTo.getText());
                    }
                    
					if(page>pdf.getPageCount()) {
                        page = pdf.getPageCount();
                    }
					numberTo.setText(String.valueOf(page));
				}else{
                    pagesBoxPressed();
                }
			}
        	
        });
        
        subSet = new ComboBox();
        subSet.setItems(FXCollections.observableArrayList(Messages.getMessage("PdfViewerPrint.AllPagesInRange"), Messages.getMessage("PdfViewerPrint.OddPagesOnly")
                , Messages.getMessage("PdfViewerPrint.EvenPagesOnly")));
        
        reverse = new CheckBox();
        reverse.setText(Messages.getMessage("PdfViewerPrint.ReversePages"));

        final Label subset = new Label();
        subset.setText(Messages.getMessage("PdfViewerPrint.Subset"));

        rangeGrid.setVgap(6d);

        rangeGrid.add(all, 0, 0);
        rangeGrid.add(currentView, 0, 1);
        rangeGrid.add(currentPage, 0, 2);
        rangeGrid.add(pagesFrom, 0, 3);
        rangeGrid.add(numberFrom, 1, 3);
        rangeGrid.add(new Label(" to "), 2, 3);
        rangeGrid.add(numberTo, 3, 3);
        rangeGrid.add(subset, 0, 4);
        rangeGrid.add(subSet, 1, 4);
        rangeGrid.add(reverse, 0, 5);

        rangeTab.getChildren().add(rangeGrid);
        tabs.getTabs().add(range);
        range.setContent(rangeTab);
    }
    
    TextField copies;// = new TextField();
    ComboBox scaling;// = new ComboBox();
    CheckBox autoRotateCenter;// = new CheckBox();
    CheckBox paperSourceByPDF;// = new CheckBox();
    CheckBox grayscale;// = new CheckBox();
    
    /**
     * Initialsing the Handling Tab
     */
    private void initHandlingTab() {
        final HBox handlingTab = new HBox();
        final GridPane handlingGrid = new GridPane();
        handlingGrid.setPadding(new Insets(20, 0, 0, 0));
        copies = new TextField();
        copies.textProperty().addListener(new ChangeListener<String>() {

        	@Override
        	public void changed(final ObservableValue<? extends String> observable,
        			final String oldValue, final String newValue) {
        		if(newValue.matches("\\d*")) {
                    copies.setText(newValue);
                } else {
                    copies.setText(oldValue);
                }
        	}
        });
        scaling = new ComboBox();
        scaling.setItems(FXCollections.observableArrayList(PrinterOptions.PRINT_SCALING_OPTIONS));
        scaling.getSelectionModel().select(PrinterOptions.LAST_SCALING_CHOICE);
        autoRotateCenter = new CheckBox();
        paperSourceByPDF = new CheckBox();
        grayscale = new CheckBox();

        final Label copiesLabel = new Label();
        copiesLabel.setText(Messages.getMessage("PdfViewerPrint.Copies"));

        final Label pageScaling = new Label();
        pageScaling.setText(Messages.getMessage("PdfViewerPrint.PageScaling"));

        autoRotateCenter.setText((Messages.getMessage("PdfViewerPrint.AutoRotateAndCenter")));
        paperSourceByPDF.setText(Messages.getMessage("PdfViewerPrint.ChoosePaperByPdfSize"));
        grayscale.setText(Messages.getMessage("PdfViewerPrint.Grayscale"));

        handlingGrid.setVgap(6d);

        handlingGrid.add(copiesLabel, 0, 0);
        handlingGrid.add(copies, 1, 0);
        handlingGrid.add(pageScaling, 0, 1);
        handlingGrid.add(scaling, 1, 1);
        handlingGrid.add(autoRotateCenter, 0, 2);
        handlingGrid.add(paperSourceByPDF, 0, 3);
        handlingGrid.add(grayscale, 0, 4);

        handlingTab.getChildren().add(handlingGrid);
        tabs.getTabs().add(handling);
        handling.setContent(handlingTab);
    }
    private boolean debugPrinterChange;
	
    int pageCount, currentPageNo;
    private boolean okClicked;

    private String[] printersList;
        
    private final PaperSizes paperDefinitions;

    private int defaultResolution;
    
    private static final double mmPerSubInch = 25.4 / 72;
    
    //
    @Override
    public void resetDefaults(final String[] printersList, final String defaultPrinter, final int pageCount, final int currentPage) {

    	//this.defaultPrinter=defaultPrinter;
    	this.printersList = printersList;
    	this.pageCount = pageCount;
    	this.currentPageNo = currentPage;

    	initComponents();

    	/**set selected printer*/
    	final String printerFlag=System.getProperty("org.jpedal.defaultPrinter");
    	if(printerFlag!=null){
            for (final String aPrintersList : printersList) {
                if (printerFlag.equals(aPrintersList)) {
                	printers.getSelectionModel().select(aPrintersList);
                }
            }
    	}else{
    		printers.getSelectionModel().select(defaultPrinter);
    	}
    	
        if (pagesOptions.getItems().isEmpty()) {
            return;
        }

        final int defaultPagesize = paperDefinitions.getDefaultPageIndex();
        if (defaultPagesize < pagesOptions.getItems().size()) {
            pagesOptions.getSelectionModel().select(defaultPagesize);
        }
        
        numberFrom.setText("1");
        numberTo.setText(String.valueOf(pdf.getPageCount()));
        
        subSet.getSelectionModel().select(0);
        
        copies.setText("1");
        
        //
    	
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
    	setupStage();
    	setupTabs();
    }// </editor-fold>
    
    private static String[] getAvailableResolutions(final PrintService p) {
        final PrinterResolution[] resolutions = (PrinterResolution[])p.getSupportedAttributeValues(PrinterResolution.class, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);

        if (resolutions==null) {
            return new String[]{"Default"};
        }

        final String[] names = new String[resolutions.length];
        for (int i=0; i<resolutions.length; i++) {
            final PrinterResolution res = resolutions[i];
            names[i] = res.getCrossFeedResolution(PrinterResolution.DPI)+"x"+res.getFeedResolution(PrinterResolution.DPI)+" dpi";
        }

        return names;
    }

    @Override
    public PrinterResolution getResolution() {
        final PrintService[] ps = PrintServiceLookup.lookupPrintServices(null,null);
        PrintService p=null;
        for (final PrintService p1 : ps) {
            if (p1.getName().equals(printers.getSelectionModel().getSelectedItem())) {
                p = p1;
            }
        }

        final PrinterResolution[] resolutions = ((PrinterResolution[])p.getSupportedAttributeValues(PrinterResolution.class, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null));

        if (resolutions == null) {
            return null;
        }

        return resolutions[resolutionOptions.getSelectionModel().getSelectedIndex()];
    }

    private int getDefaultResolutionIndex() {
        //get print service
        final PrintService[] ps = PrintServiceLookup.lookupPrintServices(null,null);
        PrintService p=null;
        for (final PrintService p1 : ps) {
            if (p1.getName().equals(printers.getSelectionModel().getSelectedItem())) {
                p = p1;
            }
        }

        //get available resolutions
        final PrinterResolution[] resolutions = (PrinterResolution[])p.getSupportedAttributeValues(PrinterResolution.class, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);

        if (resolutions == null) {
            return 0;
        }

        if (defaultResolution == -1) {
            defaultResolution = 600;
        }

        //find nearest resolution
        int minDiff = Integer.MAX_VALUE;
        int indexToUse = 0;
        for (int i=0; i<resolutions.length; i++) {
            final PrinterResolution res = resolutions[i];
            int cfDiff = res.getCrossFeedResolution(PrinterResolution.DPI)-defaultResolution;
            if (cfDiff < 0) {
                cfDiff = -cfDiff;
            }
            int fDiff = res.getFeedResolution(PrinterResolution.DPI)-defaultResolution;
            if (fDiff < 0) {
                fDiff = -fDiff;
            }

            if (cfDiff+fDiff < minDiff) {
                minDiff = cfDiff+fDiff;
                indexToUse = i;
            }
        }

        return indexToUse;
    }
    
    //Margins organised as Left, Top, Right, Bottom
    final double[] minimumMargins = {0,0,0,0};
    final DecimalFormat df = new DecimalFormat("#.###");
    private void updateMargins() {

    	//Set here as it is first needed in the lines below
    	df.setMinimumFractionDigits(1);
    	
        final MarginPaper p = getSelectedPaper();

        if (p==null) {
            return;
        }

        //Update minimum values
//        ((CustomSpinnerModel)left.getModel()).setMinValue(p.getMinX()*mmPerSubInch);
//        ((CustomSpinnerModel)top.getModel()).setMinValue(p.getMinY()*mmPerSubInch);
//        ((CustomSpinnerModel)right.getModel()).setMinValue((p.getWidth()-p.getMaxRX())*mmPerSubInch);
//        ((CustomSpinnerModel)bottom.getModel()).setMinValue((p.getHeight()-p.getMaxBY())*mmPerSubInch);

        minimumMargins[0] = p.getMinX()*mmPerSubInch;
        minimumMargins[1] = p.getMinY()*mmPerSubInch;
        minimumMargins[2] = (p.getWidth()-p.getMaxRX())*mmPerSubInch;
        minimumMargins[3] = (p.getHeight()-p.getMaxBY())*mmPerSubInch;
        
        //Set values to min
        leftT.setText(df.format(minimumMargins[0]));
        margins[0] = Float.parseFloat(leftT.getText());
        topT.setText(df.format(minimumMargins[1]));
        margins[1] = Float.parseFloat(topT.getText());
        rightT.setText(df.format(minimumMargins[2]));
        margins[2] = Float.parseFloat(rightT.getText());
        bottomT.setText(df.format(minimumMargins[3]));
        margins[3] = Float.parseFloat(bottomT.getText());
        
    }

//    private void printPreview(Graphics2D g2) {
//        int w = previewComponent.getWidth();
//        int h = previewComponent.getHeight();
//        int pW,pH,iW,iH,iX,iY;    //values in 72ths of an inch
//        int pageWidth, pageHeight;
//        int currentPreviewedPage = 1;
//
//        //In print current view use the visible area of the screen instead of the page heights
//        if (printRangeCurrentView.isSelected()) {
//            pageWidth = (int)(((PdfDecoder)pdf).getVisibleRect().getWidth()/(pdf.getScaling()));
//            pageHeight = (int)(((PdfDecoder)pdf).getVisibleRect().getHeight()/(pdf.getScaling()));
//        } else {
//            pageWidth = pdf.getPdfPageData().getCropBoxWidth(currentPreviewedPage);
//            pageHeight = pdf.getPdfPageData().getCropBoxHeight(currentPreviewedPage);
//        }
//
//        //set paper size and printable area
//        if (paperSourceByPDF.isSelected()) {
//            pW = pageWidth;                                                         //Paper width
//            pH = pageHeight;                                                        //Paper height
//            iW = pW;                                                                //Imageable area width
//            iH = pH;                                                                //Imageable area height
//            iX = 0;                                                                 //Imageable area start x
//            iY = 0;                                                                 //Imageable area start y
//        } else {
//            MarginPaper p = paperDefinitions.getSelectedPaper(pageSize.getSelectedItem());
//            if (p==null) {
//                g2.drawString(Messages.getMessage("PdfPrintPreview.Loading"), (w/2)-25, (h/2)-5);
//                return;
//            }
//            pW = (int)p.getWidth();
//            pH = (int)p.getHeight();
//            iX = (int)p.getImageableX();
//            iY = (int)p.getImageableY();
//            iW = (int)p.getImageableWidth();
//            iH = (int)p.getImageableHeight();
//        }
//
//        //check auto rotate
//        if (autoRotateCenter.isSelected() && ((pageWidth>pageHeight && iW<iH) || (pageWidth<pageHeight && iW>iH))) {
//            int temp;
//            temp = pW;
//            pW = pH;
//            pH = temp;
//
//            temp = iW;
//            iW = iH;
//            iH = temp;
//            temp = iX;
//            iX = iY;
//            iY = temp;
//
//            //flip
//            iY = pH - iY - iH;
//        }
//
//        //Set offsets
//        int lO = 25;
//        int tO = 30;
//        int rO = 5;
//        int bO = 25;
//
//        double paperHeightInInches = pH/72d;
//        double paperWidthInInches = pW/72d;
//
//        //Calculate scaling
//        double wScale = (double)(w-(lO+rO))/pW;
//        double hScale = (double)(h-(tO+bO))/pH;
//        double scale;
//        if (wScale < hScale)
//            scale = wScale;
//        else
//            scale = hScale;
//
//        //Scale all values
//        pW = (int)(scale*pW);
//        pH = (int)(scale*pH);
//        iX = (int)(scale*iX);
//        iY = (int)(scale*iY);
//        iW = (int)(scale*iW);
//        iH = (int)(scale*iH);
//        pageWidth = (int)(scale*pageWidth);
//        pageHeight = (int)(scale*pageHeight);
//
//        double pageScale = 1;
//        if (printHandlingScaling.getSelectedIndex()==1 ||                                                 //Fit
//                (printHandlingScaling.getSelectedIndex()==2 && (pageWidth>iW || pageHeight>iH))) {        //Reduce
//            pageScale = (double)iW / pageWidth;
//            if (((double)iH / pageHeight) < pageScale )
//                pageScale = (double)iH / pageHeight;
//
//            pageWidth = (int)(pageScale*pageWidth);
//            pageHeight = (int)(pageScale*pageHeight);
//        }
//
//        //Include any centering
//        lO+=(w-(lO+rO+pW))/2;
//        tO+=(h-(tO+bO+pH))/2;
//
//        //Fill page background
//        g2.setPaint(Color.WHITE);
//        g2.fillRect(lO,tO,pW,pH);
//
//        //Draw printable area
//        g2.setPaint(Color.RED);
//        g2.drawLine(lO+iX,tO,lO+iX,tO+pH);
//        g2.drawLine(lO,tO+iY,lO+pW,tO+iY);
//        g2.drawLine(lO+iW+iX,tO,lO+iW+iX,tO+pH);
//        g2.drawLine(lO,tO+iH+iY,lO+pW,tO+iH+iY);
//
//        //fudge printable area for image drawing
//        iX++;
//        iY++;
//        iW--;
//        iH--;
//
//        g2.setPaint(Color.BLACK);
//
//        //Draw width bar
//        NumberFormat f = NumberFormat.getNumberInstance();
//        f.setMaximumFractionDigits(1);
//        String widthString = f.format(paperWidthInInches);
//        int tLen = widthString.length()*3;
//
//        g2.drawString(widthString, lO+(pW/2)-tLen,tO-5);
//        g2.drawLine(lO,tO-15,lO,tO-5);
//        g2.drawLine(lO+pW,tO-15,lO+pW,tO-5);
//        g2.drawLine(lO,tO-10,(lO+(pW/2)-tLen)-6,tO-10);
//        g2.drawLine((lO+(pW/2)+tLen)+6,tO-10,lO+pW,tO-10);
//        g2.drawLine(lO,tO-10,lO+5,tO-15);
//        g2.drawLine(lO,tO-10,lO+5,tO-5);
//        g2.drawLine(lO+pW,tO-10,lO+pW-5,tO-15);
//        g2.drawLine(lO+pW,tO-10,lO+pW-5,tO-5);
//
//        //Draw height bar
//        String heightString = f.format(paperHeightInInches);
//        tLen = heightString.length()*3;
//        g2.drawString(heightString, (lO-12)-tLen, tO+(pH/2)+5);
//        g2.drawLine(lO-15,tO,lO-5,tO);
//        g2.drawLine(lO-15,tO+pH,lO-5,tO+pH);
//        g2.drawLine(lO-10,tO,lO-10,(tO+(pH/2))-8);
//        g2.drawLine(lO-10,((tO+(pH/2))+8),lO-10,tO+pH);
//        g2.drawLine(lO-10,tO,lO-5,tO+5);
//        g2.drawLine(lO-10,tO,lO-15,tO+5);
//        g2.drawLine(lO-10,tO+pH,lO-15,tO+pH-5);
//        g2.drawLine(lO-10,tO+pH,lO-5,tO+pH-5);
//
//        //draw page
//        try {
//            BufferedImage img;
//
//            //print current view uses current display on PdfDecoder
//            if (printRangeCurrentView.isSelected()) {
//                img = new BufferedImage((int)((PdfDecoder)pdf).getVisibleRect().getWidth(), (int)((PdfDecoder)pdf).getVisibleRect().getHeight(), BufferedImage.TYPE_INT_ARGB);
//                Graphics g = img.getGraphics();
//                g.translate((int)-((PdfDecoder)pdf).getVisibleRect().getX(), (int)-((PdfDecoder)pdf).getVisibleRect().getY());
//
//                //store border and background and restore after paint
//                Border bStore = ((PdfDecoder)pdf).getBorder();
//                Color cStore = ((PdfDecoder)pdf).getBackground();
//                ((PdfDecoder)pdf).setBorder(BorderFactory.createEmptyBorder());
//                ((PdfDecoder)pdf).setBackground(Color.WHITE);
//                ((PdfDecoder)pdf).paintComponent(g);//
//                ((PdfDecoder)pdf).setBorder(bStore);
//                ((PdfDecoder)pdf).setBackground(cStore);
//            } else {
//                img = pdf.getPageAsImage(currentPreviewedPage);
//            }
//
//            if (grayscale.isSelected())
//                img = ColorSpaceConvertor.convertColorspace(img, BufferedImage.TYPE_BYTE_GRAY);
//
//            g2.setClip(lO+iX,tO+iY,iW,iH);
//
//            int centeringX = (int)((iW-pageWidth)/2d);
//            int centeringY = (int)((iH-pageHeight)/2d);
//
//            g2.drawImage(img,lO+iX+centeringX,tO+iY+centeringY,pageWidth,pageHeight,null);
//        } catch(PdfException e) {
//            //
//        }
//
//        //Draw border
//        g2.setClip(null);
//        g2.drawRect(lO,tO,pW,pH);
//
//        f.setMaximumFractionDigits(0);
//        g2.drawString(Messages.getMessage("PdfPrintPreview.UnitScale")+f.format(pageScale*100)+ '%', 5, h-5);
//
//    }

    //

//    private void updatePreview(){
//        int selection = previewSlider.getValue();
//
//        //check for illogical settings
//        SetOfIntegerSyntax set = getPrintRange();
//        int pagePrintCount=1;
//        int currentPreviewedPage=1;
//        if (set == null) {
//
//            //Illogical - preview nothing
//            currentPreviewedPage=0;
//            pagePrintCount=0;
//
//        } else {
//
//            int[][] ranges = set.getMembers();
//
//            //calculate length
//            int count=0;
//            for (int[] range1 : ranges) {
//                count += range1[1] - (range1[0] - 1);
//            }
//            int[] pagesToPrint = new int[count];
//            pagePrintCount = pagesToPrint.length;
//
//            //populate array with page numbers
//            count=0;
//            for (int[] range : ranges) {
//                int rangeLength = range[1] - (range[0] - 1);
//                for (int j = 0; j < rangeLength; j++) {
//                    pagesToPrint[count] = range[0] + j;
//                    count++;
//                }
//            }
//
//            //check selection value
//            if (selection > pagePrintCount) {
//                selection=1;
//                previewSlider.setValue(1);
//            }
//
//            //Work out which page to preview
//            if (printRangeReversePages.isSelected())
//                currentPreviewedPage = pagesToPrint[pagesToPrint.length-selection];
//            else
//                currentPreviewedPage = pagesToPrint[selection-1];
//        }
//
//        //recalculate previewSlider
//        previewSlider.setMaxValue(pagePrintCount);
//
//        //redraw
//        previewComponent.repaint();
//    }


	private void pagesBoxPressed() {
		pagesFrom.setSelected(true);
        subSet.setDisable(false);
        reverse.setDisable(false);
//        updatePreview();
    }

    private void pagesFromStateChanged() {
        if(pagesFrom.isSelected()){
        	subSet.setDisable(false);
            reverse.setDisable(false);
//            updatePreview();
        }
    }
    
    private void printRangeCurrentView() {
        if(currentView.isSelected()){
        	subSet.setDisable(true);
            reverse.setDisable(true);
//            updatePreview();
        }
    }

    private void currentPageStateChanged() {
        if(currentPage.isSelected()){
        	subSet.setDisable(true);
            reverse.setDisable(true);
//            updatePreview();
        }
    }

    private void allStateChanged() {
        if(all.isSelected()){
        	subSet.setDisable(false);
            reverse.setDisable(false);
//            updatePreview();
        }
    }

    private void cancelEvent() {
        okClicked = false;
        setVisible(false);
        printPopup.close();
    }                            

    private void okEvent() {
        okClicked = true;
        setVisible(false);
        printPopup.close();
    }                        
    
    /**
     * return range as SetOfIntegerSytax
     * - if you try to do something silly like print all
     *  even pages in rage 1-1 you will get null returned
     */
   @Override
   public SetOfIntegerSyntax getPrintRange(){
    	
       SetOfIntegerSyntax pageRange = null;
       
       if(all.isSelected()){
    	   
    	   pageRange = new PageRanges(1, pageCount);
    	   
           if(subSet.getSelectionModel().getSelectedIndex() == 0) {
               return pageRange;
           }

           if(subSet.getSelectionModel().getSelectedIndex() == 1){
        	   StringBuilder membersStr = new StringBuilder();
               int i = -1;
               while ((i = pageRange.next(i)) != -1) {
                   if(i % 2 == 1){
                       membersStr.append(i).append(',');
                   }
               }
               
               String members=membersStr.toString();
               final StringBuilder sb = new StringBuilder(members);
               sb.deleteCharAt(members.length() - 1);
               members = sb.toString();
               
               pageRange = new PageRanges(members);
           }else if(subSet.getSelectionModel().getSelectedIndex() == 2){
        	   StringBuilder membersStr = new StringBuilder();
               int i = -1;
               while ((i = pageRange.next(i)) != -1) {
                   if(i % 2 == 0){
                       membersStr.append(i).append(',');
                   }
               }
               
               String members=membersStr.toString();
               final StringBuilder sb = new StringBuilder(members);
               sb.deleteCharAt(members.length() - 1);
               members = sb.toString();
               
               pageRange = new PageRanges(members);
           }
           
       }else if(currentPage.isSelected()){
    	   
           pageRange = new PageRanges(currentPageNo);
           
       }else if(currentView.isSelected()){    
           
    	   pageRange = new PageRanges(currentPageNo);
    	   
       }else if(pagesFrom.isSelected()){

           int start, end;
           try {
               start = Integer.parseInt(numberFrom.getText());
           } catch (final NumberFormatException e) {

               if(LogWriter.isOutput()) {
                   LogWriter.writeLog("Exception in setting page range "+e);
               }

               numberFrom.setText("1");
               start = 1;
           }
           try {
               end = Integer.parseInt(numberTo.getText());
           } catch(final NumberFormatException e) {

               if(LogWriter.isOutput()) {
                   LogWriter.writeLog("Exception in setting page range "+e);
               }

               numberTo.setText(String.valueOf(pageCount));
               end = pageCount;
           }

           //Check values in range
           if (start < 0) {
               start = 1;
               pagesFrom.setText(""+1);
           } else if (start > pageCount) {
               start = pageCount;
               pagesFrom.setText(String.valueOf(pageCount));
           }
           if (end < 0) {
               end = 1;
               numberTo.setText(""+1);
           } else if (end > pageCount) {
               end = pageCount;
               numberTo.setText(String.valueOf(pageCount));
           }

           if(start>end){
        	   final int tmp=end;
        	   end=start;
        	   start=tmp;

//               if(GUI.showMessages)
//        	   JOptionPane.showMessageDialog(this,Messages.getMessage("PdfViewerPrint.SwapValues"));
           }
           pageRange = new PageRanges(start,end);
           
           if(subSet.getSelectionModel().getSelectedIndex() == 0) {
               return pageRange;
           }

           if(subSet.getSelectionModel().getSelectedIndex() == 1){
        	   StringBuilder membersStr = new StringBuilder();
               int i = -1;
               while ((i = pageRange.next(i)) != -1) {
                   if(i % 2 == 1){
                       membersStr.append(i).append(',');
                   }
               }
               
               String members = membersStr.toString();
               final StringBuilder sb = new StringBuilder(members);
               if (members.isEmpty()) {
                   return null;
               }

               sb.deleteCharAt(members.length() - 1);
               members = sb.toString();
               
               pageRange = new PageRanges(members);
           }else if(subSet.getSelectionModel().getSelectedIndex() == 2){
        	   StringBuilder membersStr = new StringBuilder();
               int i = -1;
               while ((i = pageRange.next(i)) != -1) {
                   if(i % 2 == 0){
                       membersStr.append(i).append(',');
                   }
               }
               
               String members = membersStr.toString();
               final StringBuilder sb = new StringBuilder(members);
               final int length=members.length();
               if(length>0) {
                   sb.deleteCharAt(length - 1);
               }
               members = sb.toString();
               
               if(!members.isEmpty()) {
                   pageRange = new PageRanges(members);
               } else {
                   pageRange = null;
               }
           }
       }
       
       return pageRange;
    }
    
    @Override
    public int getCopies(){
    	final String copies = this.copies.getText();
    	return Integer.parseInt(copies);
    }
    
    /** return setting for type of scaling to use 
     * PAGE_SCALING_NONE,PAGE_SCALING_FIT_TO_PRINTER_MARGINS,PAGE_SCALING_REDUCE_TO_PRINTER_MARGINS
     *see org.jpedal.objects.contstants.PrinterOptions for all values
     */
    @Override
    public int getPageScaling(){
    	
    	final int option=scaling.getSelectionModel().getSelectedIndex();
		
		int value=0;
		//make choice
		switch (option) {
		case 0: //No scaling
			value=PrinterOptions.PAGE_SCALING_NONE;
			break;
			
		case 1: //Fit to scaling
			value=PrinterOptions.PAGE_SCALING_FIT_TO_PRINTER_MARGINS;
			break;
			
		case 2: //Reduce to scaling
			value=PrinterOptions.PAGE_SCALING_REDUCE_TO_PRINTER_MARGINS;
			break;
			
		}
		
		//remember last option for next print dialog
		PrinterOptions.LAST_SCALING_CHOICE=value;
		
    	return value;
    }
    
    @Override
    public String getPrinter(){
    	
    	if(printers==null) {
            return "";
        } else{
    		
    		if(printers.getSelectionModel().getSelectedItem()==null) {
                return "";
            } else {
                return printers.getSelectionModel().getSelectedItem();
            }
    	}
    }
    
    @Override
    public boolean okClicked(){
    	return okClicked;
    }

    @Override
    public boolean isAutoRotateAndCenter(){
    	return autoRotateCenter.isSelected();
    }
    
    @Override
    public boolean isPaperSourceByPDFSize(){
    	return paperSourceByPDF.isSelected();
    }
    
    @Override
    public boolean isPrintingCurrentView(){
    	return currentView.isSelected();
    }
    
    @Override
    public String[] getAvailablePaperSizes(){
    	return paperDefinitions.getAvailablePaperSizes();
    }
    
    /**return selected Paper*/
    @Override
    public MarginPaper getSelectedPaper() {
        return paperDefinitions.getSelectedPaper(pagesOptions.getSelectionModel().getSelectedItem());
    }
    
    /**return printers default orientation*/
    @Override
    public int getSelectedPrinterOrientation() {
        return paperDefinitions.getDefaultPageOrientation();
    }
    
    @Override
    public boolean isPagesReversed(){
        return reverse.isSelected();
    }

	@Override
    public boolean isOddPagesOnly() {
		return subSet.getSelectionModel().getSelectedIndex() == 1;
	}

	@Override
    public boolean isEvenPagesOnly() {
		return subSet.getSelectionModel().getSelectedIndex() == 2;
	}

    @Override
    public boolean isMonochrome() {
        return grayscale.isSelected();
    }

    /**
     * Returns the specified margins in the order Left, Top, Right, Bottom
     * @return float[] in the order Left, Top, Right, Bottom
     */
    public float[] getMargins(){
        return margins;
    }
//    private class CustomSlider extends JPanel {
//        private int value=1, maxValue=100;
//        private static final int rightMargin =9;
//        private static final int leftMargin =9;
//        private boolean dragging = false;
//
//        public CustomSlider() {
//            addMouseMotionListener(new MouseMotionAdapter(){
//                @Override
//                public void mouseDragged(MouseEvent e) {
//                    if (dragging) {
//                        value = (int)((((double)(e.getX()- leftMargin)/(getWidth()-(leftMargin+rightMargin)))*(maxValue-1))+1.5);
//                        if (value > maxValue)
//                            value = maxValue;
//                        if (value < 1)
//                            value = 1;
////                        updatePreview();
//                        repaint();
//                    }
//                }
//            });
//            addMouseListener(new MouseAdapter(){
//                @Override
//                public void mouseClicked(MouseEvent e) {
//                    if (e.getY()<20) {
//                        double newValue = (((double)(e.getX()-leftMargin)/(getWidth()-(leftMargin+rightMargin)))*(maxValue-1))+1;
//                        if (newValue > value)
//                            value++;
//                        else if (newValue < value)
//                            value--;
//
//                        if (value > maxValue)
//                            value = maxValue;
//                        if (value < 1)
//                            value = 1;
//                        updatePreview();
//                        repaint();
//                    }
//                }
//
//                @Override
//                public void mousePressed(MouseEvent e) {
//                    if (e.getY()<20)
//                        dragging=true;
//                }
//
//                @Override
//                public void mouseReleased(MouseEvent e) {
//                    dragging=false;
//                }
//            });
//        }
//
//        @Override
//        public void paint(Graphics g) {
//            Graphics2D g2 = (Graphics2D)g;
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
//
//            final int w = this.getWidth();
//            final int h = this.getHeight();
//
//            //fill background
//            g2.setPaint(new Color(240,240,240));
//            g2.fillRect(0,0,w,h);
//
//            //draw text
//            g2.setPaint(Color.BLACK);
//            g2.drawString(Messages.getMessage("PdfPrintPreview.Sheet")+value+Messages.getMessage("PdfPrintPreview.SheetOf")+maxValue, 2, h-3);
//
//            //draw line
//            g2.setPaint(Color.LIGHT_GRAY);
//            g2.fillRect(leftMargin +1, 11, w-((leftMargin+rightMargin)+1), 3);
//            g2.setPaint(Color.GRAY);
//            g2.drawLine(leftMargin, 12, w- rightMargin, 12);
//
//            //draw knob
//            float pageX = leftMargin +(((float)(value-1)/(maxValue-1))*(w-(leftMargin+rightMargin)));
//            Shape number = new Ellipse2D.Float(pageX-6, 6.5f, 12, 12);
//            Shape number2 = new Ellipse2D.Float(pageX-4, 8.5f, 8, 8);
//            g2.setPaint(Color.BLACK);
//            g2.fill(number);
//            g2.setPaint(Color.WHITE);
//            g2.fill(number2);
//        }
//
//        public void setValue(int value) {
//            this.value = value;
//            repaint();
//        }
//
//        public void setMaxValue(int maxValue) {
//            if (maxValue!=this.maxValue) {
//                value = 1;
//                this.maxValue = maxValue;
//                updatePreview();
//                return;
//            }
//            this.maxValue = maxValue;
//            repaint();
//        }
//
//        public int getValue() {
//            return value;
//        }
//    }

//    private static class CustomSpinnerModel extends SpinnerNumberModel {
//        private double value = 0;
//        private ArrayList listeners = new ArrayList();
//        private double minValue = 0;
//
//        @Override
//        public Object getPreviousValue() {
//            if (value <= minValue)
//                return null;
//            if (value-0.5<minValue)
//                return minValue;
//            return value - 0.5;
//        }
//        @Override
//        public Object getNextValue() {
//            return value + 0.5;
//        }
//        @Override
//        public Object getValue() {
//            return value;
//        }
//        @Override
//        public void addChangeListener(ChangeListener l) {
//            listeners.add(l);
//        }
//        @Override
//        public void removeChangeListener(ChangeListener l) {
//            listeners.remove(l);
//        }
//        @Override
//        public void setValue(Object value) {
//            try {
//                double newValue = (Double) value;
//                if (newValue < minValue)
//                    this.value = minValue;
//                else
//                    this.value = newValue;
//            } catch (Exception e) {
//                throw new IllegalArgumentException();
//            }
//            ChangeEvent e = new ChangeEvent(this);
//            for (Object listener : listeners) {
//                ((ChangeListener) listener).stateChanged(e);
//            }
//        }
//        public void setMinValue(double minValue) {
//            this.minValue = minValue;
//            setValue(getValue());
//        }
//    }
}
