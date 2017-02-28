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
 * PrintPanelInt.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;

import javax.print.attribute.SetOfIntegerSyntax;
import javax.print.attribute.standard.PrinterResolution;

import org.jpedal.examples.viewer.paper.MarginPaper;

public interface PrintPanelInt{

    void resetDefaults(String[] printersList, String defaultPrinter, int pageCount, int currentPage);
    
    PrinterResolution getResolution();
    
    /**
     * return range as SetOfIntegerSytax
     * - if you try to do something silly like print all
     *  even pages in rage 1-1 you will get null returned
     */
    SetOfIntegerSyntax getPrintRange();
    
    int getCopies();
    
    /** return setting for type of scaling to use 
     * PAGE_SCALING_NONE,PAGE_SCALING_FIT_TO_PRINTER_MARGINS,PAGE_SCALING_REDUCE_TO_PRINTER_MARGINS
     *see org.jpedal.objects.contstants.PrinterOptions for all values
     */
    int getPageScaling();
    
    String getPrinter();
    
    boolean okClicked();

    boolean isAutoRotateAndCenter();
    
    boolean isPaperSourceByPDFSize();
    
    boolean isPrintingCurrentView();
    
    String[] getAvailablePaperSizes();
    
    /**return selected Paper*/
    MarginPaper getSelectedPaper();
    
    /**return printers default orientation*/
    int getSelectedPrinterOrientation();
    
    boolean isPagesReversed();

	boolean isOddPagesOnly();

	boolean isEvenPagesOnly();

    boolean isMonochrome();
    
    boolean isVisible();
}
