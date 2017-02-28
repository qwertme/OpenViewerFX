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
 * JavaFXHelper.java
 * ---------------
 */
package org.jpedal.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * home for general JavaFX code
 */
public class JavaFXHelper {
    
    //dev flag
    private static boolean javaFXAvailable = true;
    
    private static boolean javaFXTested;
    
    public static boolean isJavaFXAvailable() {
        if (!javaFXTested) {
            try {
                Class.forName("javafx.scene.image.WritableImage");
                javaFXAvailable = true;
            } catch (final Exception e) {
                if (tryToLoadFX()) {
                    javaFXAvailable = true;
                } else {
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("JavaFX Unavailable. Exception "+e);
                    }
                    
                    javaFXAvailable = false;
                }
            } catch (final Error e) {
                // extra error catch for java.lang.UnsupportedClassVersionError preventing running on 1.5
                
                if (tryToLoadFX()) {
                    javaFXAvailable = true;
                } else {
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("JavaFX Unavailable. Exception "+e);
                    }
                    
                    javaFXAvailable = false;
                }
            }
            javaFXTested = true;
        }
        return javaFXAvailable;
    }
    
    private static boolean tryToLoadFX() {
        try {
            final File jfxrt = new File(System.getProperty("java.home") + "/lib/jfxrt.jar");
            if (!jfxrt.exists()) {
                throw new Exception("jfxrt.jar not found.");
            }
            final URL url = jfxrt.toURI().toURL();
            final URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            final Class<?> sysclass = URLClassLoader.class;
            try {
                final Method method = sysclass.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(sysloader, url);
            } catch (final Throwable t) {
                //t.printStackTrace();
                throw new IOException("Error, could not add URL to system classloader "+t);
            }
            return true;
        } catch (final Exception e) {

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in handling JavaFX "+e);
            }
            return false;
        }
    }
    
    /**
     * return version of Java FX used
     */
    public static String getVersion() {
        return com.sun.javafx.runtime.VersionInfo.getRuntimeVersion();
    }
}
