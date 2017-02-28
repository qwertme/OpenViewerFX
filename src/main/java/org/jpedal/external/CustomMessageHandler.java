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
 * CustomMessageHandler.java
 * ---------------
 */
package org.jpedal.external;


/**
 * allow user to handle output messages
 */
public interface CustomMessageHandler {


    /**
     * Allow user to add own action to all dialog messages
     * and also bypass dialog messages
     * @param message
     * @return true if you still want message displayed
     */
    boolean showMessage(Object message);

    /**example below
    public boolean showMessage(Object message){

        if(message instanceof String)
            System.out.println("Message="+message);
        else{
            System.out.println("Object is a component ="+message);
        }

        //do not show JPedal message dialog
        return false;
    }
     /**/

    /**
     * Allow user to add own action to all dialog messages
     * and also bypass dialog messages
     * @param args
     * @return null if you still want input requested and used
     */
    @SuppressWarnings("UnusedParameters")
    String requestInput(Object[] args);

    /**
    public String requestInput(Object[] args) {

        System.out.println("input requested - parameters passed in (String or components");
        return null;
    }
    /**/

    /**
     * Allow user to add own action to all dialog messages
     * and also bypass dialog messages
     * @param args
     * @return int value returnd by JOptionPane.showConfirmDialog -1 to popup JPedal menu
     */
    @SuppressWarnings("UnusedParameters")
    int requestConfirm(Object[] args);

    /**
    public int requestConfirm(Object[] args) {

        System.out.println("input requested - parameters passed in (String or components");
        return -1;
    }
    /**/

}