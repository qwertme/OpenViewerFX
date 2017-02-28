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
 * PaperSizes.java
 * ---------------
 */
package org.jpedal.examples.viewer.paper;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import java.awt.print.PageFormat;
import java.util.*;

public class PaperSizes {

    Map paperDefinitions=new HashMap();
    ArrayList paperList = new ArrayList();

    private static final double mmToSubInch = 72 / 25.4;

    final Map paperNames=new HashMap();

    /**default for paper selection*/
    private int defaultPageIndex;
    private String defaultSize;

    private PrintService printService;

    @SuppressWarnings("UnusedDeclaration")
    public PaperSizes(final PrintService printService) {
    	defaultSize = null;
    	populateNameMap();
    	addCustomPaperSizes();
    	setPrintService(printService);
    }
    
    public PaperSizes(final String defaultSize){
        this.defaultSize = defaultSize;
        populateNameMap();
        addCustomPaperSizes();
    }

    public String[] getAvailablePaperSizes(){
        final Object[] objs = paperList.toArray();
        final String[] names = new String[objs.length];
        for (int i=0; i<objs.length; i++) {
            names[i] = (String) objs[i];
        }
        return names;
    }

    /**return selected Paper*/
    public MarginPaper getSelectedPaper(final Object id) {
        return (MarginPaper) paperDefinitions.get(id);
    }

    /**
     * method to setup specific Paper sizes
     * - add your own here to extend list
     */
    private static void addCustomPaperSizes(){

    	//String printDescription;
    	//MarginPaper paper;
    	//defintion for each Paper - must match

            /**
    	//A4 (border)
		printDescription="A4";
		paper = new Paper();
		paper.setSize(595, 842);
		paper.setImageableArea(43, 43, 509, 756);
		paperDefinitions.put(printDescription,paper);
        paperList.add(printDescription);
               /**/
        /**
		//A4 (borderless)
		printDescription="A4 (borderless)";
        paper = new Paper();
		paper.setSize(595, 842);
		paper.setImageableArea(0, 0, 595, 842);
		paperDefinitions.put(printDescription,paper);
        paperList.add(printDescription);
        /**/

    /**
		//A5
		printDescription="A5";
		paper = new Paper();
		paper.setSize(420, 595);
		paper.setImageableArea(43,43,334,509);
		paperDefinitions.put(printDescription,paper);
        paperList.add(printDescription);

		//Added for Adobe
		printDescription="US Letter (8.5 x 11)";
		paper = new Paper();
		paper.setSize(612, 792);
		paper.setImageableArea(43,43,526,706);
		paperDefinitions.put(printDescription,paper);
        paperList.add(printDescription);
    /**/
        /**
		//custom
		printDescription="Custom 2.9cm x 8.9cm";
		int customW=(int) (29*2.83);
		int customH=(int) (89*2.83); //2.83 is scaling factor to convert mm to pixels
		paper = new Paper();
		paper.setSize(customW, customH);
		paper.setImageableArea(0,0,customW,customH); //MUST BE SET ALSO
		paperDefinitions.put(printDescription,paper);
        paperList.add(printDescription);
		*/

		/** kept in but commented out for general usage
		//architectural D (1728x2592)
		printDescription="Architectural D";
		paper = new Paper();
		paper.setSize(1728, 2592);
		paper.setImageableArea(25,25,1703,2567);
		paperDefinitions.put(printDescription,paper);
         paperList.add(printDescription);
		/**/

		//Add your own here

    }

    /**
     * Checks flags and locale and chooses a default paper size
     */
    private void setDefault() {

        if (paperList==null) {
            return;
        }

        //set default value
        defaultPageIndex = -1;

        //check JVM flag
        final String paperSizeFlag=System.getProperty("org.jpedal.printPaperSize");
    	if(paperSizeFlag!=null){
    		for(int i=0; i<paperList.size(); i++){
                if(paperList.get(i).equals(paperSizeFlag)){
    				defaultPageIndex = i;
    			}
    		}
    	}

        //Check properties file value (passed in)
        if (defaultPageIndex == -1 && defaultSize != null && !defaultSize.isEmpty()) {
            for (int i=0; i<paperList.size(); i++) {
                if (defaultSize.equals(paperList.get(i))) {
                    defaultPageIndex = i;
                }
            }
        }

        //if no default specified check location and choose
        if (defaultPageIndex == -1) {
            defaultSize = "A4";

            //Check for US countries
            final String[] letterSizeDefaults = {"US","CA","MX","CO","VE","AR","CL","PH"};
            final String country = Locale.getDefault().getCountry();
            for (final String letterSizeDefault : letterSizeDefaults) {
                if (country.equals(letterSizeDefault)) {
                    defaultSize = "North American Letter";
                }
            }

            //Get index
            for (int j=0; j<paperList.size(); j++) {
                if (defaultSize.equals(paperList.get(j))) {
                    defaultPageIndex = j;
                }
            }

            //Make sure not negative
            if (defaultPageIndex == -1) {
                defaultPageIndex = 0;
            }
        }
    }

    /**
     * Sets the print service and checks which page sizes are available
     * @param p print service
     */
    public synchronized void setPrintService(final PrintService p) {
        this.printService = p;
        paperDefinitions=new HashMap();
        paperList=new ArrayList();

        checkAndAddSize(MediaSizeName.ISO_A4);
        checkAndAddSize(MediaSizeName.NA_LETTER);
        checkAndAddSize(MediaSizeName.ISO_A0);
        checkAndAddSize(MediaSizeName.ISO_A1);
        checkAndAddSize(MediaSizeName.ISO_A2);
        checkAndAddSize(MediaSizeName.ISO_A3);
        checkAndAddSize(MediaSizeName.ISO_A5);
        checkAndAddSize(MediaSizeName.ISO_A6);
        checkAndAddSize(MediaSizeName.ISO_A7);
        checkAndAddSize(MediaSizeName.ISO_A8);
        checkAndAddSize(MediaSizeName.ISO_A9);
        checkAndAddSize(MediaSizeName.ISO_A10);
        checkAndAddSize(MediaSizeName.ISO_B0);
        checkAndAddSize(MediaSizeName.ISO_B1);
        checkAndAddSize(MediaSizeName.ISO_B2);
        checkAndAddSize(MediaSizeName.ISO_B3);
        checkAndAddSize(MediaSizeName.ISO_B4);
        checkAndAddSize(MediaSizeName.ISO_B5);
        checkAndAddSize(MediaSizeName.ISO_B6);
        checkAndAddSize(MediaSizeName.ISO_B7);
        checkAndAddSize(MediaSizeName.ISO_B8);
        checkAndAddSize(MediaSizeName.ISO_B9);
        checkAndAddSize(MediaSizeName.ISO_B10);
        checkAndAddSize(MediaSizeName.JIS_B0);
        checkAndAddSize(MediaSizeName.JIS_B1);
        checkAndAddSize(MediaSizeName.JIS_B2);
        checkAndAddSize(MediaSizeName.JIS_B3);
        checkAndAddSize(MediaSizeName.JIS_B4);
        checkAndAddSize(MediaSizeName.JIS_B5);
        checkAndAddSize(MediaSizeName.JIS_B6);
        checkAndAddSize(MediaSizeName.JIS_B7);
        checkAndAddSize(MediaSizeName.JIS_B8);
        checkAndAddSize(MediaSizeName.JIS_B9);
        checkAndAddSize(MediaSizeName.JIS_B10);
        checkAndAddSize(MediaSizeName.ISO_C0);
        checkAndAddSize(MediaSizeName.ISO_C1);
        checkAndAddSize(MediaSizeName.ISO_C2);
        checkAndAddSize(MediaSizeName.ISO_C3);
        checkAndAddSize(MediaSizeName.ISO_C4);
        checkAndAddSize(MediaSizeName.ISO_C5);
        checkAndAddSize(MediaSizeName.ISO_C6);
        checkAndAddSize(MediaSizeName.NA_LEGAL);
        checkAndAddSize(MediaSizeName.EXECUTIVE);
        checkAndAddSize(MediaSizeName.LEDGER);
        checkAndAddSize(MediaSizeName.TABLOID);
        checkAndAddSize(MediaSizeName.INVOICE);
        checkAndAddSize(MediaSizeName.FOLIO);
        checkAndAddSize(MediaSizeName.QUARTO);
        checkAndAddSize(MediaSizeName.JAPANESE_POSTCARD);
        checkAndAddSize(MediaSizeName.JAPANESE_DOUBLE_POSTCARD);
        checkAndAddSize(MediaSizeName.A);
        checkAndAddSize(MediaSizeName.B);
        checkAndAddSize(MediaSizeName.C);
        checkAndAddSize(MediaSizeName.D);
        checkAndAddSize(MediaSizeName.E);
        checkAndAddSize(MediaSizeName.ISO_DESIGNATED_LONG);
        checkAndAddSize(MediaSizeName.ITALY_ENVELOPE);
        checkAndAddSize(MediaSizeName.MONARCH_ENVELOPE);
        checkAndAddSize(MediaSizeName.PERSONAL_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_NUMBER_9_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_NUMBER_10_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_NUMBER_11_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_NUMBER_12_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_NUMBER_14_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_6X9_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_7X9_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_9X11_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_9X12_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_10X13_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_10X14_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_10X15_ENVELOPE);
        checkAndAddSize(MediaSizeName.NA_5X7);
        checkAndAddSize(MediaSizeName.NA_8X10);

        addCustomPaperSizes();

        setDefault();
    }
    
    public String[] getPaperSizes() {

        return new String[]{
        (String)paperNames.get(MediaSizeName.ISO_A4.toString()),
        (String)paperNames.get(MediaSizeName.NA_LETTER.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A0.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A1.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A2.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A3.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A5.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A6.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A7.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A8.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A9.toString()),
        (String)paperNames.get(MediaSizeName.ISO_A10.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B0.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B1.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B2.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B3.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B4.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B5.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B6.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B7.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B8.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B9.toString()),
        (String)paperNames.get(MediaSizeName.ISO_B10.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B0.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B1.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B2.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B3.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B4.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B5.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B6.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B7.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B8.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B9.toString()),
        (String)paperNames.get(MediaSizeName.JIS_B10.toString()),
        (String)paperNames.get(MediaSizeName.ISO_C0.toString()),
        (String)paperNames.get(MediaSizeName.ISO_C1.toString()),
        (String)paperNames.get(MediaSizeName.ISO_C2.toString()),
        (String)paperNames.get(MediaSizeName.ISO_C3.toString()),
        (String)paperNames.get(MediaSizeName.ISO_C4.toString()),
        (String)paperNames.get(MediaSizeName.ISO_C5.toString()),
        (String)paperNames.get(MediaSizeName.ISO_C6.toString()),
        (String)paperNames.get(MediaSizeName.NA_LEGAL.toString()),
        (String)paperNames.get(MediaSizeName.EXECUTIVE.toString()),
        (String)paperNames.get(MediaSizeName.LEDGER.toString()),
        (String)paperNames.get(MediaSizeName.TABLOID.toString()),
        (String)paperNames.get(MediaSizeName.INVOICE.toString()),
        (String)paperNames.get(MediaSizeName.FOLIO.toString()),
        (String)paperNames.get(MediaSizeName.QUARTO.toString()),
        (String)paperNames.get(MediaSizeName.JAPANESE_POSTCARD.toString()),
        (String)paperNames.get(MediaSizeName.JAPANESE_DOUBLE_POSTCARD.toString()),
        (String)paperNames.get(MediaSizeName.A.toString()),
        (String)paperNames.get(MediaSizeName.B.toString()),
        (String)paperNames.get(MediaSizeName.C.toString()),
        (String)paperNames.get(MediaSizeName.D.toString()),
        (String)paperNames.get(MediaSizeName.E.toString()),
        (String)paperNames.get(MediaSizeName.ISO_DESIGNATED_LONG.toString()),
        (String)paperNames.get(MediaSizeName.ITALY_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.MONARCH_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.PERSONAL_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_NUMBER_9_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_NUMBER_10_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_NUMBER_11_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_NUMBER_12_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_NUMBER_14_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_6X9_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_7X9_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_9X11_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_9X12_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_10X13_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_10X14_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_10X15_ENVELOPE.toString()),
        (String)paperNames.get(MediaSizeName.NA_5X7.toString()),
        (String)paperNames.get(MediaSizeName.NA_8X10.toString())};
    }

    /**
     * Checks whether a paper size is available and adds it to the array
     * @param name The MediaSizeName to check
     */
    private void checkAndAddSize(final MediaSizeName name) {

        //Check if available on this printer
        final PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        if (!printService.isAttributeValueSupported(name, new DocFlavor.BYTE_ARRAY(DocFlavor.BYTE_ARRAY.PNG.getMimeType()), attributes)) {
            return;
        }


        //Get name and lookup in our name map
        final Object o = paperNames.get(name.toString());
        final String printDescription;
        if (o != null) {
            printDescription = o.toString();
        } else {
            printDescription = name.toString();
        }


        //Get paper size
        final MediaSize size = MediaSize.getMediaSizeForName(name);
        double pX = size.getX(MediaSize.MM);
        double pY = size.getY(MediaSize.MM);


        //Get printable area
        attributes.add(name);
        final MediaPrintableArea[] area = (MediaPrintableArea[])printService.getSupportedAttributeValues(MediaPrintableArea.class,null,attributes);

        if (area.length==0) {
            return;
        }

        int useArea = 0;
        if(area[useArea]==null) {
            for (int i = 0; i != area.length && area[useArea] == null; i++) {
                useArea = i;
            }
        }
        	
        
        final float[] values = area[useArea].getPrintableArea(MediaPrintableArea.MM);

        //Check if very near to pagesize since pagesize is stored less accurately (avoids rounding/negative issues)
        if (values[2] > pX-0.5 && values[2] < pX+0.5) {
            values[2] = (float) pX;
        }
        if (values[3] > pY-0.5 && values[3] < pY+0.5) {
            values[3] = (float) pY;
        }

        //Check if printer thinks page is other way round - flip pagesize if so
        if (values[2] > pX ^ values[3] > pY) {
            final double temp = pX;
            pX = pY;
            pY = temp;
        }


        //Create and store as Paper object
        final MarginPaper paper = new MarginPaper();
        paper.setSize(pX*mmToSubInch, pY*mmToSubInch);
        paper.setMinImageableArea(values[0]* mmToSubInch,values[1]*mmToSubInch, values[2]*mmToSubInch, values[3]*mmToSubInch);

        paperDefinitions.put(printDescription,paper);
        paperList.add(printDescription);
    }

    /**
     * Returns the index of the default paper size
     * @return
     */
    public int getDefaultPageIndex() {
    	return defaultPageIndex;
        
    }

    /**
     * Returns the default orientation requested by the printer
     * @return int flag for the orientation
     */
    public int getDefaultPageOrientation() {
    	
    	//Set the pageformat orientation based on printer preference
        final OrientationRequested or = (OrientationRequested)printService.getDefaultAttributeValue(OrientationRequested.class);
        int orientation = PageFormat.PORTRAIT;
        if(or!=null){
        	switch (or.getValue()) {
        	case 4:
        		orientation = PageFormat.LANDSCAPE;
        		break;
        	case 5:
        		orientation = PageFormat.REVERSE_LANDSCAPE;
        		break;
			}
        }
        
    	return orientation;
    }
    
    /**
     * Fills the name map from standardised to "pretty" names
     */
    private void populateNameMap() {
        paperNames.put("iso-a0", "A0");
        paperNames.put("iso-a1", "A1");
        paperNames.put("iso-a2", "A2");
        paperNames.put("iso-a3", "A3");
        paperNames.put("iso-a4", "A4");
        paperNames.put("iso-a5", "A5");
        paperNames.put("iso-a6", "A6");
        paperNames.put("iso-a7", "A7");
        paperNames.put("iso-a8", "A8");
        paperNames.put("iso-a9", "A9");
        paperNames.put("iso-a10", "A10");
        paperNames.put("iso-b0", "B0");
        paperNames.put("iso-b1", "B1");
        paperNames.put("iso-b2", "B2");
        paperNames.put("iso-b3", "B3");
        paperNames.put("iso-b4", "B4");
        paperNames.put("iso-b5", "B5");
        paperNames.put("iso-b6", "B6");
        paperNames.put("iso-b7", "B7");
        paperNames.put("iso-b8", "B8");
        paperNames.put("iso-b9", "B9");
        paperNames.put("iso-b10", "B10");
        paperNames.put("na-letter", "North American Letter");
        paperNames.put("na-legal", "North American Legal");
        paperNames.put("na-8x10", "North American 8x10 inch");
        paperNames.put("na-5x7", "North American 5x7 inch");
        paperNames.put("executive", "Executive");
        paperNames.put("folio", "Folio");
        paperNames.put("invoice", "Invoice");
        paperNames.put("tabloid", "Tabloid");
        paperNames.put("ledger", "Ledger");
        paperNames.put("quarto", "Quarto");
        paperNames.put("iso-c0", "C0");
        paperNames.put("iso-c1", "C1");
        paperNames.put("iso-c2", "C2");
        paperNames.put("iso-c3", "C3");
        paperNames.put("iso-c4", "C4");
        paperNames.put("iso-c5", "C5");
        paperNames.put("iso-c6", "C6");
        paperNames.put("iso-designated-long", "ISO Designated Long size");
        paperNames.put("na-10x13-envelope", "North American 10x13 inch");
        paperNames.put("na-9x12-envelope", "North American 9x12 inch");
        paperNames.put("na-number-10-envelope", "North American number 10 business envelope");
        paperNames.put("na-7x9-envelope", "North American 7x9 inch envelope");
        paperNames.put("na-9x11-envelope", "North American 9x11 inch envelope");
        paperNames.put("na-10x14-envelope", "North American 10x14 inch envelope");
        paperNames.put("na-number-9-envelope", "North American number 9 business envelope");
        paperNames.put("na-6x9-envelope", "North American 6x9 inch envelope");
        paperNames.put("na-10x15-envelope", "North American 10x15 inch envelope");
        paperNames.put("monarch-envelope", "Monarch envelope");
        paperNames.put("jis-b0", "Japanese B0");
        paperNames.put("jis-b1", "Japanese B1");
        paperNames.put("jis-b2", "Japanese B2");
        paperNames.put("jis-b3", "Japanese B3");
        paperNames.put("jis-b4", "Japanese B4");
        paperNames.put("jis-b5", "Japanese B5");
        paperNames.put("jis-b6", "Japanese B6");
        paperNames.put("jis-b7", "Japanese B7");
        paperNames.put("jis-b8", "Japanese B8");
        paperNames.put("jis-b9", "Japanese B9");
        paperNames.put("jis-b10", "Japanese B10");
        paperNames.put("a", "Engineering ANSI A");
        paperNames.put("b", "Engineering ANSI B");
        paperNames.put("c", "Engineering ANSI C");
        paperNames.put("d", "Engineering ANSI D");
        paperNames.put("e", "Engineering ANSI E");
        paperNames.put("arch-a", "Architectural A");
        paperNames.put("arch-b", "Architectural B");
        paperNames.put("arch-c", "Architectural C");
        paperNames.put("arch-d", "Architectural D");
        paperNames.put("arch-e", "Architectural E");
        paperNames.put("japanese-postcard", "Japanese Postcard");
        paperNames.put("oufuko-postcard", "Oufuko Postcard");
        paperNames.put("italian-envelope", "Italian Envelope");
        paperNames.put("personal-envelope", "Personal Envelope");
        paperNames.put("na-number-11-envelope", "North American Number 11 Envelope");
        paperNames.put("na-number-12-envelope", "North American Number 12 Envelope");
        paperNames.put("na-number-14-envelope", "North American Number 14 Envelope");
    }
}
