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
 * AformDefaultJSscript.java
 * ---------------
 */

package org.jpedal.objects.javascript;

public class AformDefaultJSscript {

	/** return the string to add to Javascript to represent the viewer settings */
	public static String getViewerSettings() {
		return "ADBE.viewerVersion = 9.0;\n" +
			"ADBE.Reader_Need_Version = 9.0;\n" +//may be needed in future
			"ADBE.Viewer_Need_Version = 9.0;\n" +
		"xfa_installed = true;\n" +
		"xfa_version = 2.6;\n"+
                '\n';
	}
	
	/** returns the script from the adobe js file included in all adobe acrobat products 
	 * <br>
	 * methods supported:<br>
	 * AFExtractNums(string);<br>
	 * AFMakeNumber(string);<br>
	 * AFMergeChange(event);<br>
	 * 
	 */
	public static String getstaticScript() {
		return //add methods needed for AFMakeNumber
			"\nfunction AFExtractNums(string) {\n" +
		"	/* returns an array of numbers that it managed to extract from the given\n" +
		"	 * string or null on failure */\n" +
		"	var nums = new Array();\n" +
		"	if (string.charAt(0) == '.' || string.charAt(0) == ',')\n" +
		"		string = \"0\" + string;\n" +
		"	while(AFDigitsRegExp.test(string)) {\n" +
		"		nums.length++;\n" +
		"		nums[nums.length - 1] = RegExp.lastMatch;\n" +
		"		string = RegExp.rightContext;\n" +
		"	}\n" +
		"	if(nums.length >= 1) return nums;\n" +
		"	return null;\n" +
		"}\n"+
                '\n' +
		
		"function AFMakeNumber(string)\n" +
		"{	/* attempts to make a number out of a string that may not use '.' as the\n" +
		"	 * seperator; it expects that the number is fairly well-behaved other than\n" +
		"	 * possibly having a non-JavaScript friendly separator */\n" +
		"	var type = typeof string;\n" +
		"	if (type == \"number\")\n" +
		"		return string;\n" +
		"	if (type != \"string\")\n" +
		"		return null;\n" +
		"	var array = AFExtractNums(string);\n" +
		"	if(array)\n" +
		"	{\n" +
		"		var joined = array.join(\".\");\n" +
		"		if (string.indexOf(\"-.\") >= 0)\n" +
		"			joined = \"0.\" + joined;\n" +
		"		return joined * (string.indexOf(\"-\") >= 0 ? -1.0 : 1.0);\n" +
		"	}\n" +
		"	else\n" +
		"		return null;\n" +
		"}\n"+
                '\n' +
		
		//add whats needed for AFMergeChange
		"function AFMergeChange(event){	\n" +
		"/* merges the last change with the uncommitted change */\n" +
		"	var prefix, postfix;\n" +
		"	var value = event.value;\n" +
		"	if(event.willCommit) return event.value;\n" +
		"	if(event.selStart >= 0)\n" +
		"		prefix = value.substring(0, event.selStart);\n" +
		"	else prefix = \"\";\n" +
		"	if(event.selEnd >= 0 && event.selEnd <= value.length)\n" +
		"		postfix = value.substring(event.selEnd, value.length);\n" +
		"	else postfix = \"\";\n" +
		"	return prefix + event.change + postfix;\n" +
		"}\n"+

                '\n';
	}
}
