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
 * Messages.java
 * ---------------
 */
package org.jpedal.utils;
import java.io.BufferedReader;
import java.util.*;


/**
 * provides all internationalised messages and a routine to
 * support in Java 1.3 as well
 */
public class Messages {
	
	/**fall back for messages (ie if using 1.3) */
	private static Map<String, String> messages;

	/**log values not found so not repeateded*/
	private static Set<String> reportedValueMissing=new HashSet<String>();
	
	/**localized text bundle */
	protected static ResourceBundle bundle;
	
	private static boolean isInitialised;
	
	/**set bundle*/
	public static void setBundle(final ResourceBundle newBundle){
		bundle=newBundle;
	
		if(!isInitialised) {
                    init();
                }
	}


    /**
	 * display message from message bundle or name if problem
	 */
	public static String getMessage(final String key) {
		
		String message=null;
		
		try{
			message=messages.get(key);
			if(message==null) {
                            message=bundle.getString(key);
                        }
		}catch(final Exception e){
			//

            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: "+e.getMessage());
                        }
            //
		}
		
		//trap for 1.3 or missing
		if(message==null){
			
			try{
				
				message=messages.get(key);
				
			}catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: "+e.getMessage());
                }
                //
			}
		}
		
		//if still null use message key
		if(message==null) {
                    message=key;
                }
		
		if(message.isEmpty()) {
                    message=key+"<<";
                }
		return message;
	}
	
	/**
	 * reads message bundle manually if needed (bug in 1.3.0)
	 */
	private static void init(){
		
		isInitialised=true;

		String line;
		final BufferedReader input_stream = null;
		//ClassLoader loader = Messages.class.getClassLoader();
		/**must use windows encoding because files were edited on Windows*/
		//String enc = "Cp1252";
		//int equalsIndex;
		
		try {
			
		
			//initialise inverse lookup (add space)
			messages=new HashMap<String, String>();
			
//			String targetFile="messages.properties";
//			
//			String ID=bundle.getLocale().toString();
//			if(ID.length()==0 || ID.startsWith("en")){
//			 targetFile="messages.properties";
//			}else{
//				targetFile="messages_"+ID+".properties";
//			}
			
//			 String customBundle=System.getProperty("org.jpedal.bundleLocation");
//			 customBundle="org.jpedal.international.messages"; //test code
//			 if(customBundle!=null){
//				 String customBundleLocation=customBundle.replaceAll("\\.","/");
//				 
//				 if(ID.length()==0 || ID.startsWith("en")){
//					 targetFile=".properties";
//					}else{
//						targetFile="_"+ID+".properties";
//					}
//				 
//				 input_stream =
//					 new BufferedReader(
//							 new InputStreamReader(
//									 loader.getResourceAsStream(
//											 customBundleLocation+targetFile),
//											 enc));
//				 
//			 }else{
//
//				 input_stream =
//					 new BufferedReader(
//							 new InputStreamReader(
//									 loader.getResourceAsStream(
//											 "org/jpedal/international/"+targetFile),
//											 enc));
//				 
//			 }
			
			
			// trap problems
			if (input_stream == null) {
                            LogWriter.writeLog("Unable to open messages.properties from jar");
                        }
	
			
			final Enumeration keys = bundle.getKeys();

			while(keys.hasMoreElements()){
				final String element = (String) keys.nextElement();
				
				//read in lines and place in map for fast lookup
				//			while (true) {
				line = (String)bundle.getObject(element);
				if (line == null) {
                                    break;
                                }

				//get raw string
				final StringBuilder newMessage=new StringBuilder();

				//work through string converting #; values to unicde
				//Convert &#int to ascii
				final StringTokenizer t = new StringTokenizer(line,"\\&;",true);
				String nextValue;
				boolean isAmpersand=false;

				while (t.hasMoreTokens()) {

					if(isAmpersand){
						nextValue="&";
						isAmpersand=false;
					}else {
                                            nextValue=t.nextToken();
                                        }

					//if token is &, we have found an ascii char 
					//and need to convert
					//othwerwise just add back

					//Check for escape characters
					if(t.hasMoreTokens() && nextValue.equals("\\")){
						final String ascii=t.nextToken(); //actual value
						final char c=ascii.charAt(0);

						//Check to see if escape is a newline
						if(c=='n'){
							newMessage.append('\n');
						}else if(c==' '){
							newMessage.append(' ');
						}

						newMessage.append(ascii.substring(1));
					}else if(t.hasMoreTokens() && nextValue.equals("&")){

						String ascii=t.nextToken(); //actual value

						final String end;
						if(t.hasMoreTokens()){
							end=t.nextToken(); //read and ignore ;

							if(end.equals("&")){
								newMessage.append('&');
								newMessage.append(ascii);
								isAmpersand=true;
							}else if(end.equals(";")){

								if(ascii.startsWith("#")) {
                                                                    ascii=ascii.substring(1);
                                                                }

								//convert number to char
								final char c=(char) Integer.parseInt(ascii);

								//add back to newMessage
								newMessage.append(c);

							}else{
								{

									//get next char and check ;
									if(t.hasMoreTokens()) {
                                                                            newMessage.append('&');
                                                                        }
									newMessage.append(ascii);


								}
							}
						}else{ //and last token
							newMessage.append('&');
							newMessage.append(ascii);

						}
					}else {
                                            newMessage.append(nextValue);
                                        }
				}

				//  System.out.println("final value="+newMessage.toString()+"<<");

				//store converted message
				messages.put(element,newMessage.toString());
			}
		}	
		
		
		catch (final Exception e) {
			e.printStackTrace();
			LogWriter.writeLog("Exception "+e+" loading resource bundle.\n" +
					"Also check you have a file in org.jpedal.international.messages to support Locale="+java.util.Locale.getDefault());
		
			System.err.println("Exception loading resource bundle.\n" +
					"Also check you have a file in org.jpedal.international.messages to support Locale="+java.util.Locale.getDefault());
		
		}
		
		
		//ensure closed
		if(input_stream!=null){
			try{
				
				input_stream.close();
			}catch (final Exception e) {
				LogWriter.writeLog(
						"Exception " + e + " reading lookup table for pdf  for abobe map");
			}		
		}	
	}

	public static void dispose() {
		
		messages=null;

		reportedValueMissing=null;
		
		bundle=null;
		
		isInitialised = false;
	}
}
