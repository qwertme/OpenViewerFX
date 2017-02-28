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
 * ExampleCustomMessageHandler.java
 * ---------------
 */
package org.jpedal.examples.handlers;

import org.jpedal.external.CustomMessageHandler;

public class ExampleCustomMessageHandler implements CustomMessageHandler {

    /**give verbose output so we can see what happens*/
    boolean showMessage;

    @Override
    public boolean showMessage(final Object message){

        if(showMessage){
            if(message instanceof String) {
                System.out.println("Message=" + message);
            } else{
                System.out.println("Object is a component ="+message);
            }
        }

        return false;
    }

    /**
     * input request and parameters passed in
     * @param args
     * @return null will call input - otherwise will use non-null value
     */
    @Override
    public String requestInput(final Object[] args) {

        if(showMessage){
            System.out.println("input requested - parameters passed in (String or components");
        }

        return null;
    }

    /**
     * Allow user to add own action to all dialog messages
     * and also bypass dialog messages
     * @param args
     * @return int value returnd by JOptionPane.showConfirmDialog
     */
    @Override
    public int requestConfirm(final Object[] args) {

        if(showMessage){
            System.out.println("input requested - parameters passed in (String or components");
        }

        //return -1 to pop-up message
        return 0;
    }
}
