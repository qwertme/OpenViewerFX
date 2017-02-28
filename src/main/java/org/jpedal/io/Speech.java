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
 * Speech.java
 * ---------------
 */

package org.jpedal.io;

public interface Speech {
    
    /**
     * Set the voice you wish to use when speaking text
     * If the voice does not exist in the available list
     * the default boice should be used.
     * @param voiceName A string representing the name of the voice
     */
    void setVoice(String voiceName);
    
    /**
     * A flag to specify if we can currently speak text.
     * This should be used to determine is all required libraries
     * and files are present.
     * @return True is text to speach will be possible.
     */
    boolean speechAvailible();
    
    /**
     * Method to get a complete list of the voices this functionality
     * can use.
     * @return String array containing the names of all voices
     */
    String[] listVoices();
    
    /**
     * Method to actually speak the text passed in.
     * This method is called internal to work with jPedals example viewer.
     * @param text String of text to be spoken.
     */
    void speakText(final String text);
}
