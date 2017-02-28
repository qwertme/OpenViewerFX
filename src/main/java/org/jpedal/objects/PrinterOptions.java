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
 * PrinterOptions.java
 * ---------------
 */
package org.jpedal.objects;

import org.jpedal.utils.Messages;

/**Public contstants used in printing*/
public class PrinterOptions {

	/**String representation of scaling types*/
    public static final String[] PRINT_SCALING_OPTIONS={ Messages.getMessage("PdfViewerPrint.NoScaling"),
		Messages.getMessage("PdfViewerPrint.FitToPrinterMargins"),
		Messages.getMessage("PdfViewerPrint.ReduceToPrinterMargins")};
	/**type of printing*/
    public static final int PAGE_SCALING_NONE=0;
	/**type of printing*/
    public static final int PAGE_SCALING_FIT_TO_PRINTER_MARGINS=1;
	/**type of printing*/
    public static final int PAGE_SCALING_REDUCE_TO_PRINTER_MARGINS=2;
	
	/**last printer option*/
	public static int LAST_SCALING_CHOICE=2;
	
	public static final int ALL_PAGES=8;
	
	public static final int ODD_PAGES_ONLY=16;
	
	public static final int EVEN_PAGES_ONLY=32;
	
	public static final int PRINT_PAGES_REVERSED=64;

}
