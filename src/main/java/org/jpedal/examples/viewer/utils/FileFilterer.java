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
 * FileFilterer.java
 * ---------------
 */
package org.jpedal.examples.viewer.utils;
import java.io.File;

/**
 * Used in GUI example code
 * Provides filters for save dialogs
 *
 * (based on p362 in Oreilly swing)
 */
public class FileFilterer extends javax.swing.filechooser.FileFilter{
	
	final String[] extensions;
	String description;
	
	//number off possible values
    final int  items;
	
	//setup file and descriptor
	public FileFilterer( final String[] ext, final String desc ) {
		items = ext.length;
		
		//setup as lower case list
		extensions = new String[items];
		for( int i = 0;i < items;i++ )
		{
			extensions[i] = ext[i].toLowerCase();
			
			//and add a description
			description = desc;
		}
	}
	
	@Override
    public final String getDescription(){
		return description;
	}
	
	@Override
    public final boolean accept( final File f ){
		boolean accept_flag = false;
		
		//allow directories
		if( f.isDirectory() ) {
            accept_flag = true;
        } else{
			//check file against list
			final String file_name = f.getName().toLowerCase();
			for( int i = 0;i < items;i++ ){
				if( file_name.endsWith( extensions[i] ) ) {
                    accept_flag = true;
                }
			}
		}
		return accept_flag;
	}
}
