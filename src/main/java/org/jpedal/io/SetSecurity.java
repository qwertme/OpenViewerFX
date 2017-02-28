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
 * SetSecurity.java
 * ---------------
 */
package org.jpedal.io;

import org.jpedal.utils.LogWriter;

import java.security.Key;
import java.security.Security;
import java.security.cert.Certificate;

public class SetSecurity {
    
    private static String altSP;
    
    public static void init(){
        
        //allow user to over-ride
        altSP=System.getProperty("org.jpedal.securityprovider");
        
        if(altSP==null) {
            altSP = "org.bouncycastle.jce.provider.BouncyCastleProvider";
        }
        
        try {
            
            final Class c = Class.forName(altSP);
            final java.security.Provider provider = (java.security.Provider) c.newInstance();
            
            Security.addProvider(provider);
            
        } catch (final Exception e) {
            
            if(LogWriter.isOutput()){
                LogWriter.writeLog("Unable to run custom security provider " + altSP+" Exception " + e);
            }
            
            throw new RuntimeException("This PDF file is encrypted and JPedal needs an additional library to \n" +
                    "decode on the classpath (we recommend bouncycastle library).\n" +
                    "There is additional explanation at http://www.idrsolutions.com/additional-jars"+ '\n');
            
            
        }
    }
    
    /**
     * cycle through all possible values to find match (only tested with Bouncy castle)
     * @param certificate
     * @param key
     */
    public static byte[] extractCertificateData(final byte[][] recipients, final Certificate certificate, final Key key) {
        
        //break if not bc
        if(altSP==null || !altSP.equals("org.bouncycastle.jce.provider.BouncyCastleProvider")) {
            throw new RuntimeException("only Bouncy castle currently supported with certificates");
        }
        
        return CertificateReader.readCertificate(recipients, certificate, key);
       
    }
}
