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
 * LogWriter.java
 * ---------------
 */
package org.jpedal.utils;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jpedal.*;

/**
 * <p>logs all activity. And some low level variables/methods
 * as it is visible to all classes.
 * <p>Provided for debugging and NOT officially part of the API
 */
public class LogWriter
{
    
    /**allow user to scan log output*/
    public static LogScanner logScanner;
    
    /**amount of debugging detail we put in log*/
    public static boolean debug;
    
    /**filename of logfile*/
    public static String log_name;
    
    /**flag we can set to signal code being tested*/
    public static boolean testing;
    
    /**if we echo to console. VERY USEFUL for debugging*/
    private static boolean verbose;
    
    /**Set of filters passed by -Dorg.jpedal.inclusiveLogFilters JVM argument.
     * Is null if the argument doesn't exist.
     */
    private static final Set<String> filterValues = getFilterSet();
    
    /**
     * Creates a set of filter values from JVM argument -Dorg.jpedal.inclusiveLogFilters.
     * The arguments are passed in as comma-separated values.
     *
     * @return null if the argument is not found, otherwise a HashSet of the contained values
     */
    private static Set getFilterSet(){
        final String filters = System.getProperty("org.jpedal.inclusiveLogFilters");
        Set filterSet = null;
        
        if(filters != null){
            filterSet = new HashSet<String>(Arrays.asList(filters.toLowerCase().split("[,]")));
        }
        
        return filterSet;
    }
    
    public static final boolean isOutput(){
        return verbose || logScanner!=null;
        
    }
    
    ///////////////////////////////////////////////
    public static final void writeLog( final String message )
    {
        
        /**
         * ignore any logging if we have set some inclusive values with
         *
         * -Dorg.jpedal.inclusiveLogFilters="memory,error"
         *
         * Values are case-insensitve and example above would only output messages containing 'memory' or 'error')
         */
        if(filterValues != null && message != null){
            boolean found = false;
            
            for(final String s : filterValues){
                if(message.toLowerCase().contains(s)){
                    found = true;
                    break;
                }
            }
            if(!found){
                return;
            }
        }
        
        //implement your own version of org.jpedal.utils.LogScanner
        //and set will then allow you to track any error messages
        if(logScanner!=null) {
            logScanner.message(message);
        }
        
        /**
         * write message to pane if client active
         * and put to front
         */
        if(verbose) {
            System.out.println( message );
        }
        
        if( log_name != null )
        {
            
            //write message
            final PrintWriter log_file;
            try
            {
                log_file = new PrintWriter( new FileWriter( log_name, true ) );
                
                if(!testing) {
                    log_file.println(TimeNow.getTimeNow()+ ' ' +message);		//write date to the log
                }
                log_file.println( message );
                log_file.flush();
                log_file.close();
            }catch( final Exception e ){
                System.err.println( "Exception " + e + " attempting to write to log file " + log_name );
            }
            
        }
    }
    
    //////////////////////////////////////////////
    /**
     * setup log file and check it is readable
     * also sets command line options
     */
    public static final void setupLogFile(final String command_line_values)
    {
        
        if( command_line_values != null )
        {
            
            //verbose mode echos to screen
            if( command_line_values.indexOf('v') != -1 )
            {
                verbose = true;
                writeLog( "Verbose on" );
            }else {
                verbose = false;
            }
            
        }
        
        //write out info
        if(!testing){
            //
            writeLog( "Software started - " + TimeNow.getTimeNow() );
        }
        writeLog( "=======================================================" );
    }
    
    ///////////////////////////////////////////////////////////
    /** write out logging information for forms,
     * <b>print</b> is a boolean flag, if true prints to the screen
     */
    public static void writeFormLog(final String message, final boolean print) {
        if(print) {
            System.out.println("[forms] "+message);
        }
        
        writeLog("[forms] "+message);
    }
}
