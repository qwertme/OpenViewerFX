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
 * SwingCursor.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import static org.jpedal.examples.viewer.gui.GUI.GRABBING_CURSOR;
import static org.jpedal.examples.viewer.gui.GUI.GRAB_CURSOR;
import static org.jpedal.examples.viewer.gui.GUI.PAN_CURSOR;
import static org.jpedal.examples.viewer.gui.GUI.PAN_CURSORB;
import static org.jpedal.examples.viewer.gui.GUI.PAN_CURSORBL;
import static org.jpedal.examples.viewer.gui.GUI.PAN_CURSORBR;
import static org.jpedal.examples.viewer.gui.GUI.PAN_CURSORL;
import static org.jpedal.examples.viewer.gui.GUI.PAN_CURSORR;
import static org.jpedal.examples.viewer.gui.GUI.PAN_CURSORT;
import static org.jpedal.examples.viewer.gui.GUI.PAN_CURSORTL;
import static org.jpedal.examples.viewer.gui.GUI.PAN_CURSORTR;
import org.jpedal.utils.LogWriter;

/**
 * This class controls everything todo with Swing Cursor objects
 */
public class SwingCursor {
    
    private String iconLocation = "/org/jpedal/examples/viewer/res/";
    
    /** grabbing cursor */
    private Cursor grabCursor;
    private Cursor grabbingCursor;
    private Cursor panCursor;
    private Cursor panCursorL;
    private Cursor panCursorTL;
    private Cursor panCursorT;
    private Cursor panCursorTR;
    private Cursor panCursorR;
    private Cursor panCursorBR;
    private Cursor panCursorB;
    private Cursor panCursorBL;
    
    public Cursor getCursor(final int type) {
        switch (type) {
            case GRAB_CURSOR:
                if (grabCursor == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"grab32.png"));
                    grabCursor = kit.createCustomCursor(img, new Point(8,8),"grab");
                }
                return grabCursor;

            case GRABBING_CURSOR:
                if (grabbingCursor == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"grabbing32.png"));
                    grabbingCursor = kit.createCustomCursor(img, new Point(8,8),"grabbing");
                }
                return grabbingCursor;

            case PAN_CURSOR:
                if (panCursor == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"pan32.png"));
                    panCursor = kit.createCustomCursor(img, new Point(10,10),"pan");
                }
                return panCursor;

            case PAN_CURSORL:
                if (panCursorL == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"panl32.png"));
                    panCursorL = kit.createCustomCursor(img, new Point(11,10),"panl");
                }
                return panCursorL;

            case PAN_CURSORTL:
                if (panCursorTL == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"pantl32.png"));
                    panCursorTL = kit.createCustomCursor(img, new Point(10,10),"pantl");
                }
                return panCursorTL;

            case PAN_CURSORT:
                if (panCursorT == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"pant32.png"));
                    panCursorT = kit.createCustomCursor(img, new Point(10,11),"pant");
                }
                return panCursorT;

            case PAN_CURSORTR:
                if (panCursorTR == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"pantr32.png"));
                    panCursorTR = kit.createCustomCursor(img, new Point(10,10),"pantr");
                }
                return panCursorTR;

            case PAN_CURSORR:
                if (panCursorR == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"panr32.png"));
                    panCursorR = kit.createCustomCursor(img, new Point(10,10),"panr");
                }
                return panCursorR;

            case PAN_CURSORBR:
                if (panCursorBR == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"panbr32.png"));
                    panCursorBR = kit.createCustomCursor(img, new Point(10,10),"panbr");
                }
                return panCursorBR;

            case PAN_CURSORB:
                if (panCursorB == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"panb32.png"));
                    panCursorB = kit.createCustomCursor(img, new Point(10,10),"panb");
                }
                return panCursorB;

            case PAN_CURSORBL:
                if (panCursorBL == null) {
                    final Toolkit kit = Toolkit.getDefaultToolkit();
                    final Image img = kit.getImage(getURLForImage(iconLocation+"panbl32.png"));
                    panCursorBL = kit.createCustomCursor(img, new Point(10,10),"panbl");
                }
                return panCursorBL;

            default:
                return Cursor.getDefaultCursor();

        }
    }
    
    public void setIconLocation(final String location){
        iconLocation = location;
    }


    /**
     * Retrieve the URL of the actual image to use f
     * @param path Preferred name and location
     * @return URL of file to use
     */
    public URL getURLForImage(String path) {
        
        path = iconLocation + path;
        
        final String file = path.substring(path.lastIndexOf('/')+1);
        URL url;
        
        //Check if file location provided
        path = path.substring(0, path.indexOf('.'))+".gif";
        File p = new File(path);
        url = getClass().getResource(path);

        //It's a file location check for gif
        if(p.exists()){
        	try {
        		url = p.toURI().toURL();
        	} catch (final MalformedURLException e) {
        		//
                if (LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception attempting get path for image " + e);
                }
        	}
        }

        if(url==null){
        	 path = path.substring(0, path.indexOf('.'))+".png";
        	 p = new File(path);
             url = getClass().getResource(path);

           //It's a file location check for png
        	 if(p.exists()){
             	try {
             		url = p.toURI().toURL();
             	} catch (final MalformedURLException e) {
             		//
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception attempting get path for image " + e);
                    }
                }
            }
        }

        if(url!=null){
			return url;
		}else{ //use default graphic
            
			 path = "/org/jpedal/examples/viewer/res/" +file;
             url = getClass().getResource(path);
			 return url;
        }
    }
    
    public BufferedImage getCursorImageForFX(final int type) {
        switch (type) {
            case GRAB_CURSOR:
                try {
                    return ImageIO.read(getURLForImage("grab32.png"));
                } catch (final Exception e) {

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception in getting image "+e);
                    }

                    return null;
                }
            case GRABBING_CURSOR:
                try {
                    return ImageIO.read(getURLForImage("grabbing32.png"));
                } catch (final Exception e) {

                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception in getting image "+e);
                    }

                    return null;
                }
            default:
                return null;
        }
    }
    
}
