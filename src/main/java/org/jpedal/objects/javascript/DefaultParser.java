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
 * DefaultParser.java
 * ---------------
 */
package org.jpedal.objects.javascript;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.javascript.functions.*;

/**
 * generic version for no javascript engine which just implements AF commands in Java
 */
public class DefaultParser implements ExpressionEngine
{

    public DefaultParser() {}

    AcroRenderer acro;

    /**
     * execute javascript and reset forms values
     */
    @Override
    public int execute(final FormObject form, final int type, final Object code, final int eventType, final char keyPressed) {

        int messageCode=ActionHandler.NOMESSAGE;

        if(code instanceof String){
            final String js = (String) code;

            //convert into args array
            final String[] args=JSFunction.convertToArray(js);

            final String command=args[0];

            if(command.startsWith("AF")) {
                messageCode = handleAFCommands(form, command, js, args, eventType, keyPressed);
            }

        }

        return messageCode;
    }

    /**
     * java implementation to provide these functions
     * @param command
     * @param js
     * @param args
     * @param eventType
     * @param keyPressed
     * @return
     */
    protected int handleAFCommands(final FormObject formObject, final String command, final String js, final String[] args, final int eventType, final char keyPressed) {

        int messageCode=ActionHandler.NOMESSAGE;

        //Workout type
        int type=JSFunction.UNKNOWN;

        if(js.contains("_Keystroke")){
            type=JSFunction.KEYSTROKE;
        }else if(js.contains("_Validate")){
            type=JSFunction.VALIDATE;
        }else if(js.contains("_Format")){
            type=JSFunction.FORMAT;
        }else if(js.contains("_Calculate")){
            type=JSFunction.CALCULATE;
        }

        if(eventType!=ActionHandler.FOCUS_EVENT && (type==JSFunction.VALIDATE || type==JSFunction.FORMAT)){
            JSFunction.debug("Not called on key event "+js);
            return messageCode;
        }

        //

        if(js.startsWith("AFSpecial_")) {
            new AFSpecial(acro, formObject).execute(js, args, type, eventType, keyPressed);
        } else if(command.startsWith("AFPercent_")) {
            new AFPercent(acro, formObject).execute(js, args, type, eventType, keyPressed);
        } else if(command.startsWith("AFSimple_")) {
            new AFSimple(acro, formObject).execute(js, args, type, eventType, keyPressed);
        } else if(command.startsWith("AFDate_")) {
            new AFDate(acro, formObject).execute(js, args, type, eventType, keyPressed);
        } else if(js.startsWith("AFNumber_")) {
            messageCode = new AFNumber(acro, formObject).execute(js, args, type, eventType, keyPressed);
        } else if(js.startsWith("AFRange_")) {
            new AFRange(acro, formObject).execute(js, args, type, eventType, keyPressed);
        } else if(js.startsWith("AFTime_")) {
            new AFTime(acro, formObject).execute(js, args, type, eventType, keyPressed);
        } else {
            JSFunction.debug("Unknown command " + js);
        }

        return messageCode;
    }

    @Override
    public void closeFile() {

        flush();

    }

    @Override
    public boolean reportError(final int code, final Object[] args) {
        //does nothing in default
        return false;
    }

    @Override
    public int addCode(final String value) {
        //does nothing in default
        return 0;
    }

    @Override
    public void executeFunctions(final String jsCode, final FormObject formObject) {
        //does nothing in default
    }

    @Override
    public void dispose(){

        flush();
    }

    @Override
    public void setAcroRenderer(final AcroRenderer acro) {
        this.acro=acro;
    }

    /** make sure the contaxt has been exited */
    public void flush() {}

}
