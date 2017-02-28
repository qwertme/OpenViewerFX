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
 * CertificateReader.java
 * ---------------
 */

package org.jpedal.io;

import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.RecipientInformation;
import org.jpedal.utils.LogWriter;

import java.security.Key;
import java.security.cert.Certificate;

public class CertificateReader {
    
    public static byte[] readCertificate(final byte[][] recipients, final Certificate certificate, final Key key) {
        
        byte[] envelopedData=null;
        
        /**
         * values for BC
         */
        final String provider="BC";
        
        /**
         * loop through all and get data if match found
         */
        for (final byte[] recipient : recipients) {
            
            try {
                final CMSEnvelopedData recipientEnvelope = new CMSEnvelopedData(recipient);
                
                final Object[] recipientList = recipientEnvelope.getRecipientInfos().getRecipients().toArray();
                final int listCount = recipientList.length;
                
                for (int ii = 0; ii < listCount; ii++) {
                    final RecipientInformation recipientInfo = (RecipientInformation) recipientList[ii];
                    
                    if (recipientInfo.getRID().match(certificate)) {
                        envelopedData = recipientInfo.getContent(key, provider);
                        ii = listCount;
                    }
                }
            } catch (final Exception e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
        }
        
        return envelopedData;
    }
}
