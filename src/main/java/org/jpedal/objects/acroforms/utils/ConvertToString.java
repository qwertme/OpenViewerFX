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
 * ConvertToString.java
 * ---------------
 */
package org.jpedal.objects.acroforms.utils;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author chris
 *
 * class to help convert data structures to a single String
 */
public class ConvertToString {

    /**
	 * @return
	 */
	public static String convertArrayToString(final float[] values) {
		if(values!=null){
			final StringBuilder ret = new StringBuilder();
			for(int i=0; i<values.length;i++){
				if(i>0) {
                    ret.append(", ");
                }
				ret.append(values[i]);
			}
			
			return ret.toString();
		}else{
			return null;
		}
	}

    /**
	 * @return
	 */
	public static String convertArrayToString(final int[] items) {
		if(items!=null){
			final StringBuilder ret = new StringBuilder();
			for(int i=0; i<items.length;i++){
				if(i>0) {
                    ret.append(", ");
                }
				ret.append(items[i]);
			}
			
			return ret.toString();
		}else{
			return null;
		}
	}

    public static void printStackTrace(final int level) {
    	printStackTrace(2,level+1,false);//start at 2 to ignore this method line
    }
    
    public static void printStackTrace(final int startLevel,int endLevel, final boolean err) {
    	
		final Throwable stackgetter = new Throwable();
		final StackTraceElement[] elems = stackgetter.getStackTrace();
		if(endLevel==-1 || endLevel>elems.length-1) {
            endLevel = elems.length - 1;
        }
		
		for(int i=startLevel;i<=endLevel;i++){
			if(err) {
                System.err.println(elems[i]);
            } else {
                System.out.println(elems[i]);
            }
		}
	}

    public static String convertDocumentToString(final Node formData){
    	return convertDocumentToString(formData,0);
    }

    private static String convertDocumentToString(final Node formData, final int level) {
    	if(formData==null) {
            return null;
        }
    	
    	final StringBuilder buf = new StringBuilder();
    	
    	buf.append(formData.getNodeName());
    	buf.append(" = ");
    	buf.append(formData.getNodeValue());
    	buf.append(" type=");
    	buf.append(formData.getNodeType());
    	buf.append(" textContent=");
    	buf.append(formData.getTextContent());
    	
    	final NamedNodeMap att = formData.getAttributes();
    	if(att!=null){
    		buf.append(" attributes=[");
    		for(int i=0;i<att.getLength();i++){
    			if(i>0) {
                    buf.append(',');
                }
    			buf.append(att.item(i));
    		}
    		buf.append(']');
    	}
    	
    	final NodeList nodes = formData.getChildNodes();
    	for(int i=0;i<nodes.getLength();i++){
    		buf.append('\n');
        	for(int d=0;d<level;d++){
        		buf.append('|');
        	}
    		buf.append(convertDocumentToString(nodes.item(i),level+1));
    	}
    	
    	return buf.toString();
    }

	/** depth starts at 0 */
//		StringBuilder buff = new StringBuilder();
//		buff.append("nodename=");
//		buff.append(formData.getNodeName());
//		
//		buff.append(" nodetype=");
//		buff.append(formData.getNodeType());
//		
//		buff.append(" nodevalue=");
//		buff.append(formData.getNodeValue());
//		
//		buff.append(" parent=");
//		buff.append(formData.getParentNode());
//		
//		buff.append(" Children - \n");
//		depth++;
//		for(int i=0;i<depth;i++){
//			buff.append(' ');
//}

}
