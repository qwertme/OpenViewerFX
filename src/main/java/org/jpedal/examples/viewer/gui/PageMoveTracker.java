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
 * PageMoveTracker.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui;

import java.util.TimerTask;
import org.jpedal.FileAccess;
import org.jpedal.display.Display;

/**
 *
 * @author markee
 */
public class PageMoveTracker {
    
    final java.util.Timer t2 = new java.util.Timer();
    TimerTask listener;
    
    /**
     * fix submitted by Niklas Matthies
     */
    public void dispose() {
        if(t2!=null) {
            t2.cancel();
        }
    }
    
    void startTimer(final Display pages,final int  pageNumber, final FileAccess fileAccess) {
        
        //turn if off if running
        if (listener != null) {
            listener.cancel();
            t2.purge();
        }
        
        //restart - if its not stopped it will trigger page update
        listener = new PageListener(pages, pageNumber, fileAccess);
        t2.schedule(listener, 500);
    }
    
    /**
     * used to update statusBar object if exists
     */
    private class PageListener extends TimerTask {
        
        final Display pages;
        
        final FileAccess fileAccess;
        
        final int  pageNumber,pageCount;
        
        private PageListener(final Display pages, final int  pageNumber, final FileAccess fileAccess) {
            this.pages=pages;
            this.pageNumber=pageNumber;
            this.fileAccess=fileAccess;
            this.pageCount=fileAccess.getPageCount();
        }
        
        @Override
        public void run() {
            
            if (Display.debugLayout) {
                 System.out.println("PageListener action called pageNumber="+pageNumber);
            }
            
            if(pages!=null){
                
                pages.stopGeneratingPage();
                
                //Ensure page range does not drop below one
                if(pageNumber<1) {
                    fileAccess.setPageNumber(1);
                }
                
                if(pages!=null) {
                    pages.decodeOtherPages(pageNumber, pageCount);
                }
            }
            
            //Ensure we close this timer task at the end of the task
            cancel();
        }
    }
}
