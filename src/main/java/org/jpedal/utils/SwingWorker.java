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
 * SwingWorker.java
 * ---------------
 */
package org.jpedal.utils;

public abstract class SwingWorker {
    private Object value;  // see getValue(), setValue()
    //private Thread thread;
    
    /**
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;
        ThreadVar(final Thread t) { thread = t; }
        synchronized Thread get() { return thread; }
        synchronized void clear() { thread = null; }
    }
    
    final ThreadVar threadVar;
    
    /**
     * Get the value produced by the worker thread, or null if it
     * hasn't been constructed yet.
     */
    protected synchronized Object getValue() {
        return value;
    }
    
    /**
     * Set the value produced by worker thread
     */
    private synchronized void setValue(final Object x) {
        value = x;
    }
    
    /**
     * Compute the value to be returned by the get method.
     */
    public abstract Object construct();

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to stop what it's doing.
     */
    public void interrupt() {
        final Thread t = threadVar.get();
        if (t != null) {
            t.interrupt();
            
            while(t.isAlive()){
                try {
                    Thread.sleep(20);
                } catch (final InterruptedException e) {
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: "+e.getMessage());
                    }
                    //
                }
            }
        }
        threadVar.clear();
    }
    
    /**
     * Return the value created by the construct method
     * Returns null if either the constructing thread or the current
     * thread was interrupted before a value was produced
     *
     * @return the value created by the construct method
     */
    public Object get() {
        while (true) {
            final Thread t = threadVar.get();
            if (t == null) {
                return getValue();
            }
            try {
                t.join();
            }
            catch (final InterruptedException e) {

                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception in handling thread "+e);
                }
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }
    
    
    /**
     * Start a thread that will call the construct method
     * and then exit.
     */
    public SwingWorker() {

        final Runnable doConstruct = new Runnable() {
            @Override
            public void run() {
                try {
                    setValue(construct());
                    
                }catch(final Exception e){
                    //
                    if (LogWriter.isOutput()) {
                        LogWriter.writeLog("Caught a Exception " + e);
                    }
                }
                finally {
                    threadVar.clear();
                }
            }
        };
        
        final Thread t = new Thread(doConstruct);
        t.setDaemon(true);
        threadVar = new ThreadVar(t);
    }
    
    /**
     * Start the worker thread.
     */
    public void start() {
        final Thread t = threadVar.get();
        if (t != null) {
            t.start();
        }
    }
}
