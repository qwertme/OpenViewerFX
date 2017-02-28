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
 * DecryptionFactory.java
 * ---------------
 */
package org.jpedal.io;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.jpedal.constants.PDFflags;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfKeyPairsIterator;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.ObjectCloneFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Provide AES/RSA decryption support
 */
public class DecryptionFactory {

    private Map cachedObjects=new HashMap();

    /**flag to show if extraction allowed*/
    private boolean extractionIsAllowed = true;

    /**flag to show provider read*/
    private boolean isInitialised;

    private boolean isMetaDataEncypted=true;

    /**flag if password supplied*/
    private boolean isPasswordSupplied;

    private boolean stringsEncoded;

    /**flag to show data encrytped*/
    private boolean isEncrypted;

    /**key used for encryption*/
    private byte[] encryptionKey;

    /** revision used for encryption*/
    private int rev;

    /**P value in encryption*/
    private int P;

    /**O value in encryption*/
    private byte[] O;

    /**U value in encryption*/
    private byte[] U;

    /**additional 5 values*/
    private byte[] OE, Perms,UE;

    //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
    /**cipher used for decryption*/
    Cipher cipher;

    //show if AES encryption
    private boolean isAES;

    private PdfObject StmFObj,StrFObj;

    @SuppressWarnings("CanBeFinal")
    private static boolean alwaysReinitCipher;

    static{
        final String flag=System.getProperty("org.jpedal.cipher.reinit");
        if(flag!=null && flag.equalsIgnoreCase("true")) {
            alwaysReinitCipher = true;
        }
    }

    /**encryption padding*/
    private final String[] pad={"28","BF","4E","5E","4E","75","8A","41","64","00","4E","56","FF","FA","01","08",
            "2E","2E","00","B6","D0","68","3E","80","2F","0C","A9","FE","64","53","69","7A"};

    private boolean isAESIdentity;

    /**length of encryption key used*/
    private int keyLength=5;

    /**flag to show if user can view file*/
    private boolean isFileViewable=true;

    //tell user status on password
    private int passwordStatus;

    /**holds file ID*/
    private final byte[] ID;


    /**encryption password*/
    private byte[] encryptionPassword;

    private Certificate certificate;

    private Key key;

    public DecryptionFactory(final byte[] ID, final byte[] encryptionPassword){
        this.ID=ID;
        this.encryptionPassword=encryptionPassword;
    }

    /**
     * version for using public certificates
     * @param id
     * @param certificate
     * @param key
     */
    public DecryptionFactory(final byte[] id, final Certificate certificate, final PrivateKey key) {
        this.ID=id;
        this.certificate=certificate;
        this.key=key;
    }

    /**see if valid for password*/
    private boolean testPassword() throws PdfSecurityException {

        int count=32;

        final byte[] rawValue=new byte[32];
        byte[] keyValue ;

        for(int i=0;i<32;i++) {
            rawValue[i] = (byte) Integer.parseInt(pad[i], 16);
        }

        byte[] encrypted= ObjectCloneFactory.cloneArray(rawValue);

        if (rev==2) {
            encryptionKey=calculateKey(O,P,ID);
            encrypted=decrypt(encrypted,"", true,null,false,false);

        } else if(rev>=3) {

            //use StmF values in preference
            int keyLength=this.keyLength;

//            if(rev==4 && StmFObj!=null){
//                final int lenKey=StmFObj.getInt(PdfDictionary.Length);
//                if(lenKey!=-1) {
//                    keyLength = lenKey;
//                    if(keyLength>32){
//                        keyLength = keyLength >>3;
//                    }
//                }
//            }

            count=16;
            encryptionKey=calculateKey(O,P,ID);
            final byte[] originalKey= ObjectCloneFactory.cloneArray(encryptionKey);

            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (final Exception e) {
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception " + e + " with digest");
                }
            }

            md.update(encrypted);

            //feed in ID
            keyValue = md.digest(ID);

            keyValue=decrypt(keyValue,"", true,null,true,false);

            final byte[] nextKey = new byte[keyLength];

            for (int i=1; i<=19; i++) {

                for (int j=0; j<keyLength; j++) {
                    nextKey[j] = (byte) (originalKey[j] ^ i);
                }

                encryptionKey=nextKey;

                keyValue=decrypt(keyValue,"", true,null,true,false);

            }

            encryptionKey=originalKey;

            encrypted = new byte[32];
            System.arraycopy(keyValue,0, encrypted,0, 16);
            System.arraycopy(rawValue,0, encrypted,16, 16);

        }

        return compareKeys(U, encrypted,count);

    }

    private static boolean compareKeys(final byte[] U, final byte[] encrypted, final int count) {

        boolean match=true;
        for(int i=0;i<count;i++){
            if(U[i]!=encrypted[i]){
                match =false;
                i=U.length;
            }
        }
        return match;
    }

    /**set the key value*/
    private void computeEncryptionKey() throws PdfSecurityException{

        final MessageDigest md;

        /**calculate key to use*/
        final byte[] key=getPaddedKey(encryptionPassword,encryptionPassword);

        /**feed into Md5 function*/
        try{

            // Obtain a message digest object.
            md = MessageDigest.getInstance("MD5");
            encryptionKey=md.digest(key);

            /**rev 3 extra security*/
            if(rev>=3){
                for (int ii=0; ii<50; ii++) {
                    encryptionKey = md.digest(encryptionKey);
                }
            }

        }catch(final Exception e){
            throw new PdfSecurityException("Exception "+e+" generating encryption key");
        }
    }

    /**see if valid for password*/
    private boolean testOwnerPassword() throws PdfSecurityException{

        final byte[] originalPassword=encryptionPassword;

        byte[] userPasswd=new byte[keyLength];
        final byte[] inputValue= ObjectCloneFactory.cloneArray(O);

        
        computeEncryptionKey();

        final byte[] originalKey= ObjectCloneFactory.cloneArray(encryptionKey);

        if(rev==2){
            userPasswd=decrypt(ObjectCloneFactory.cloneArray(O),"", false,null,false,false);
        }else if(rev>=3){

            //use StmF values in preference
            final int keyLength=this.keyLength;
//            if(rev==4 && StmFObj!=null){
//                final int lenKey=StmFObj.getInt(PdfDictionary.Length);
//                if(lenKey!=-1) {
//                    keyLength = lenKey;
//                    if(keyLength>32){
//                        keyLength = keyLength >>3;
//                    }
//                }
//            }

            userPasswd=inputValue;
            final byte[] nextKey = new byte[keyLength];


            for (int i=19; i>=0; i--) {

                for (int j=0; j<keyLength; j++) {
                    nextKey[j] = (byte) (originalKey[j] ^ i);
                }

                encryptionKey=nextKey;
                userPasswd=decrypt(userPasswd,"", false,null,true,false);

            }
        }

        //this value is the user password if correct
        //so test
        encryptionPassword = userPasswd;

        computeEncryptionKey();

        final boolean isMatch=testPassword();

        //put back to original if not in fact correct
        if(!isMatch){
            encryptionPassword=originalPassword;
            computeEncryptionKey();
        }

        return isMatch;
    }

    /**test password and set access settings*/
    private void verifyAccess() throws PdfSecurityException{

        /**assume false*/
        isPasswordSupplied=false;
        extractionIsAllowed=false;

        passwordStatus= PDFflags.NO_VALID_PASSWORD;

        /**workout if user or owner password valid*/
        boolean isOwnerPassword=false,isUserPassword=false;
        if(rev<5){
            isOwnerPassword =testOwnerPassword();
            isUserPassword=testPassword();
        }else{ //v5 method very different so own routines to handle
            try {

                isOwnerPassword=compareKeys(O,getV5Key(true,32),32);

                if(isOwnerPassword){
                    encryptionKey= v5Decrypt(OE, getV5Key(true, 32));
                }else{ //try user
                    isUserPassword=compareKeys(U, getV5Key(false,32),32);
                    if(isUserPassword) //if not set throws error below
                    {
                        encryptionKey = v5Decrypt(UE, getV5Key(false, 40));
                    }

                }
                
            } catch (final NoSuchAlgorithmException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
        }

        if(isOwnerPassword) {
            passwordStatus = PDFflags.VALID_OWNER_PASSWORD;
        }

        if(isUserPassword) {
            passwordStatus += PDFflags.VALID_USER_PASSWORD;
        }


        if(!isOwnerPassword){

            /**test if user first*/
            if(isUserPassword){

                //tell if not default value
                if(encryptionPassword!=null && encryptionPassword.length>0 && LogWriter.isOutput()) {
                    LogWriter.writeLog("Correct user password supplied ");
                }

                isFileViewable=true;
                isPasswordSupplied=true;

                if((P & 16)==16) {
                    extractionIsAllowed = true;
                }

            }else {
                throw new PdfSecurityException("No valid password supplied");
            }

        }else{
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Correct owner password supplied");
            }

            isFileViewable=true;
            isPasswordSupplied=true;
            extractionIsAllowed=true;
        }
    }

    /**
     * workout key from OE or UE
     */
    private static byte[] v5Decrypt(final byte[] rawValue, final byte[] key) throws PdfSecurityException {

        final int ELength= rawValue.length;
        final byte[] returnKey = new byte[ELength];
        
        try{

            //setup Cipher
            final BlockCipher cbc = new CBCBlockCipher(new AESFastEngine());
            cbc.init(false, new KeyParameter(key));

            //translate bytes
            int nextBlockSize;
            for(int i=0;i<ELength;i += nextBlockSize){
                cbc.processBlock(rawValue, i, returnKey, i);
                nextBlockSize=cbc.getBlockSize();
            }
            
        }catch(final Exception e){
            throw new PdfSecurityException("Exception "+e.getMessage()+" with v5 encoding");
        }

        return returnKey;

    }

    private byte[] getV5Key(final boolean isOwner, final int offset) throws NoSuchAlgorithmException {
        
        //set password and ensure not null
        byte[] password=this.encryptionPassword;
        if(password==null) {
            password = new byte[0];
        }

        //ignore anything over 128
        int passwordLength=password.length;
        if(passwordLength>127) {
            passwordLength = 127;
        }

        /**
         * feed in values
         */
        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password, 0, passwordLength);

        if(isOwner){
            md.update(O, offset, 8);
            md.update(U, 0, 48);
        }else {
            md.update(U, offset, 8);
        }

        return md.digest();
    }

    /**
     * routine to create a padded key
     */
    private byte[] getPaddedKey(final byte[] password, final byte[] encryptionPassword){

        /**get 32 bytes for  the key*/
        final byte[] key=new byte[32];
        int passwordLength=0;

        if(password!=null){
            passwordLength=password.length;
            if(passwordLength>32) {
                passwordLength = 32;
            }
        }

        if(encryptionPassword!=null) {
            System.arraycopy(encryptionPassword, 0, key, 0, passwordLength);
        }

        for(int ii=passwordLength;ii<32;ii++){

            key[ii]=(byte)Integer.parseInt(pad[ii-passwordLength],16);

        }


        return key;
    }
    /**
     * calculate the key
     */
    private byte[] calculateKey(final byte[] O, final int P, final byte[] ID) throws PdfSecurityException{

        /**calculate key to use*/
        final byte[] key=getPaddedKey(encryptionPassword,encryptionPassword);
        final byte[] keyValue;

        /**feed into Md5 function*/
        try{

            // Obtain a message digest object.
            final MessageDigest md = MessageDigest.getInstance("MD5");

            //add in padded key
            md.update(key);

            //write in O value
            md.update(O);

            //P value
            md.update(new byte[]{(byte)((P) & 0xff),(byte)((P>>8) & 0xff),(byte)((P>>16) & 0xff),(byte)((P>>24) & 0xff)});

            if(ID!=null) {
                md.update(ID);
            }

            if (rev==4 && !isMetaDataEncypted) {
                md.update(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});
            }

            final byte[] digest = new byte[keyLength];
            System.arraycopy(md.digest(), 0, digest, 0, keyLength);

            //for rev 3
            if(rev>=3){
                for (int i = 0; i < 50; ++i) {
                    System.arraycopy(md.digest(digest), 0, digest, 0, keyLength);
                }
            }

            keyValue=new byte[keyLength];
            System.arraycopy(digest, 0, keyValue, 0, keyLength);

        }catch(final Exception e){

            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //

            throw new PdfSecurityException("Exception "+e+" generating encryption key");
        }

        /**put significant bytes into key*/
        final byte[] returnKey = new byte[keyLength];
        System.arraycopy(keyValue,0, returnKey,0, keyLength);

        return returnKey;
    }

    /**extract  metadata for  encryption object
     */
    public void readEncryptionObject(final PdfObject encyptionObj) throws PdfSecurityException {

        //reset flags
        stringsEncoded=false;
        isMetaDataEncypted=true;
        StmFObj=null;
        StrFObj=null;
        isAES=false;

        if (!isInitialised) {
            isInitialised = true;
            SetSecurity.init();
        }

        //check type of filter and type and see if supported
        final int v = encyptionObj.getInt(PdfDictionary.V);

        //get filter value
        final PdfArrayIterator filters = encyptionObj.getMixedArray(PdfDictionary.Filter);
        int firstValue=PdfDictionary.Standard;
        if(filters!=null && filters.hasMoreTokens()) {
            firstValue = filters.getNextValueAsConstant(false);
        }

        //throw exception if we have an unsupported encryption method
        if(v==3) {
            throw new PdfSecurityException("Unsupported Custom Adobe Encryption method");
        } else if ((v > 4) && (firstValue!=PdfDictionary.Standard)) {
                throw new PdfSecurityException("Unsupported Encryption method");
            }
      
        final int newLength=encyptionObj.getInt(PdfDictionary.Length)>>3;
        if(newLength!=-1) {
            this.keyLength = newLength;
        }

        //get rest of the values (which are not optional)
        rev = encyptionObj.getInt(PdfDictionary.R);
        P = encyptionObj.getInt(PdfDictionary.P);
        O = encyptionObj.getTextStreamValueAsByte(PdfDictionary.O);
        U = encyptionObj.getTextStreamValueAsByte(PdfDictionary.U);
        
        //used for v=5
        OE = encyptionObj.getTextStreamValueAsByte(PdfDictionary.OE);
        UE = encyptionObj.getTextStreamValueAsByte(PdfDictionary.UE);
        Perms=encyptionObj.getTextStreamValueAsByte(PdfDictionary.Perms);

        //get additional AES values
        if(v>=4){

            isAES=true;

            String CFkey;

            final PdfObject CF=encyptionObj.getDictionary(PdfDictionary.CF);

            //EFF=encyptionObj.getName(PdfDictionary.EFF);
            //CFM=encyptionObj.getName(PdfDictionary.CFM);

            if(v==4){
                isMetaDataEncypted=encyptionObj.getBoolean(PdfDictionary.EncryptMetadata);
            }
            
            //now set any specific crypt values for StrF (strings) and StmF (streams)
            isAESIdentity=false;
            String key=encyptionObj.getName(PdfDictionary.StrF);

            if(key!=null){

                isAESIdentity=key.equals("Identity");

                stringsEncoded=true;

                final PdfKeyPairsIterator keyPairs=CF.getKeyPairsIterator();

                while(keyPairs.hasMorePairs()){

                    CFkey=keyPairs.getNextKeyAsString();

                    if(CFkey.equals(key)) {
                        StrFObj = keyPairs.getNextValueAsDictionary();
                    }

                    //roll on
                    keyPairs.nextPair();
                }
            }

            key=encyptionObj.getName(PdfDictionary.StmF);

            if(key!=null){

                isAESIdentity=key.equals("Identity");

                final PdfKeyPairsIterator keyPairs=CF.getKeyPairsIterator();

                while(keyPairs.hasMorePairs()){

                    CFkey=keyPairs.getNextKeyAsString();

                    if(CFkey.equals(key)) {
                        StmFObj = keyPairs.getNextValueAsDictionary();
                    }

                    //roll on
                    keyPairs.nextPair();
                }
            }
        }

        isEncrypted = true;
        isFileViewable = false;

        if(LogWriter.isOutput()) {
            LogWriter.writeLog("File has encryption settings");
        }

        //test if encrypted with password (not certificate)
        if(firstValue==PdfDictionary.Standard){
            try{
                verifyAccess();
            }catch(final PdfSecurityException e){
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("File requires password "+e);
                }
            }
        }else if(certificate!=null){

            /**
             * set flags and assume it will work correctly
             * (no validation at this point - error will be thrown in decrypt if not)
             */
            isFileViewable=true;
            isPasswordSupplied=true;
            extractionIsAllowed=true;

            passwordStatus=PDFflags.VALID_OWNER_PASSWORD;

        }

        //v5 stores this in Perms object and needs to be done after verify access
        if(rev==5){
            /*
            * now decode the permissions
            */
            Perms= v5Decrypt(Perms, encryptionKey);

            //see if metadata encrypted 
            isMetaDataEncypted = Perms[8] == 'T';

            //P set in Perms for v5
            P = (Perms[0] & 255) | ((Perms[1] & 255) << 8) | ((Perms[2] & 255) << 16) | ((Perms[2] & 255) << 24);
        }
    }

    /**
     * setup password value isung certificate passed in by User
     */
    private void setPasswordFromCertificate(final PdfObject AESObj){
        /**
         * if recipients set, use that for calculating key
         */
        final byte[][] recipients = (AESObj.getStringArray(PdfDictionary.Recipients));

        if(recipients!=null){

            final byte[] envelopedData=SetSecurity.extractCertificateData(recipients,certificate,key);

            /**
             * use match to create the key
             */
            if(envelopedData!=null){

                try {
                    final MessageDigest md = MessageDigest.getInstance("SHA-1");
                    md.update(envelopedData, 0, 20);
                    for (final byte[] recipient : recipients) {
                        md.update(recipient);
                    }

                    if (!isMetaDataEncypted) {
                        md.update(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});
                    }

                    encryptionKey = md.digest();
                }catch (final Exception e) {
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        }
    }

    /**
     * reads the line/s from file which make up an object
     * includes move
     */
    public byte[] decrypt(byte[] data, final String ref, final boolean isEncryption,
                          final String cacheName, final boolean alwaysUseRC4,
                          final boolean isString) throws PdfSecurityException{

        //boolean debug=false;//ref.equals("100 0 R");

        if(getBooleanValue(PDFflags.IS_FILE_ENCRYPTED) || isEncryption){

            BufferedOutputStream streamCache= null;
            BufferedInputStream bis = null;
            //int streamLength=0;

            boolean isAES=false;

            byte[] AESData=null;

            if(cacheName!=null){ //this version is used if we cache large object to disk
                //rename file
                try {

                    //we may need bytes for key
                    if(data==null){
                        AESData=new byte[16];
                        final FileInputStream fis=new FileInputStream(cacheName);
                        fis.read(AESData);
                        fis.close();
                    }

                    //streamLength = (int) new File(cacheName).length();

                    final File tempFile2 = File.createTempFile("jpedal",".raw",new File(ObjectStore.temp_dir));

                    cachedObjects.put(tempFile2.getAbsolutePath(),"x");
                    //System.out.println(">>>"+tempFile2.getAbsolutePath());
                    ObjectStore.copy(cacheName,tempFile2.getAbsolutePath());

                    final File rawFile=new File(cacheName);
                    rawFile.delete();

                    //decrypt
                    streamCache = new BufferedOutputStream(new FileOutputStream(cacheName));
                    bis=new BufferedInputStream(new FileInputStream(tempFile2));

                } catch (final IOException e1) {
                    //
                    
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception " + e1 + " in decrypt");
                    }
                }
            }

            //default values for rsa
            int keyLength=this.keyLength;
            String algorithm="RC4",keyType="RC4";
            //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
            IvParameterSpec ivSpec = null;

            //select for stream or string
            final PdfObject AESObj ;
            if(!isString){
                AESObj=StmFObj;
            }else{
                AESObj=StrFObj;
            }

            /**
             * reset each time as can change
             * (we can add flag later if slow)
             */
            if(certificate!=null){
                setPasswordFromCertificate(AESObj);

                //ensure value set so code below works
                AESObj.setIntNumber(PdfDictionary.Length,16);
            }

            //AES identity
            if(!alwaysUseRC4 && AESObj==null && isAESIdentity) {
                return data;
            }

            //use RC4 as default but override if needed
            if(AESObj!=null){

                //use CF values in preference

//                final int AESLength=AESObj.getInt(PdfDictionary.Length);
//                if(AESLength!=-1) {
//                    keyLength = AESLength;
//                    if(keyLength>32){
//                        keyLength = AESLength >>3;
//                    }
//                }

                final String cryptName=AESObj.getName(PdfDictionary.CFM);

                if(cryptName!=null && !alwaysUseRC4 && ((cryptName.equals("AESV2")|| (cryptName.equals("AESV3"))))){

                    cipher=null; //force reset as may be rsa

                    algorithm="AES/CBC/PKCS5Padding";
                    keyType="AES";

                    isAES=true;

                    //setup CBC
                    final byte[] iv=new byte[16];
                    if(AESData!=null) {
                        System.arraycopy(AESData, 0, iv, 0, 16);
                    } else {
                        System.arraycopy(data, 0, iv, 0, 16);
                    }

                    //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
                    ivSpec = new IvParameterSpec(iv);

                    //and knock off iv data in memory or cache
                    if(data==null){
                        try {
                            bis.skip(16);
                        } catch (final IOException e) {
                            //tell user and log
                            if(LogWriter.isOutput()) {
                                LogWriter.writeLog("Exception: " + e.getMessage());
                            }
                            //
                        }
                    }else{
                        final int origLen=data.length;
                        final int newLen=origLen-16;
                        byte[] newData=new byte[newLen];
                        System.arraycopy(data, 16, newData, 0, newLen);
                        data=newData;

                        //make sure data correct size
                        final int diff= (data.length & 15);
                        int newLength=data.length;
                        if(diff>0){
                            newLength=newLength+16-diff;

                            newData=new byte[newLength];

                            System.arraycopy(data, 0, newData, 0, data.length);
                            data=newData;
                        }
                        
                        if(rev == 5){
                            try {
                                final byte[] finalKey = new byte[32];
                                System.arraycopy(encryptionKey,0, finalKey,0, finalKey.length);
                                return decodeAES(finalKey,data,iv);
                            } catch (final Exception e) {
                                throw new PdfSecurityException("Exception "+e+" decrypting content in AES revision 5");
                            }
                        }
                    }
                }
            }

            byte[] currentKey=new byte[keyLength];

            if(!ref.isEmpty()) {
                currentKey = new byte[keyLength + 5];
            }

            System.arraycopy(encryptionKey, 0, currentKey, 0, keyLength);

            try{

                final byte[] finalKey;
                if(rev==5){
                    finalKey=new byte[32];
                    System.arraycopy(currentKey,0, finalKey,0, finalKey.length);
                   // finalKey=currentKey;
                }else{
                    //add in Object ref id if any
                    if(!ref.isEmpty()){
                        final int pointer=ref.indexOf(' ');
                        final int pointer2=ref.indexOf(' ',pointer+1);

                        final int obj=Integer.parseInt(ref.substring(0,pointer));
                        final int gen=Integer.parseInt(ref.substring(pointer+1,pointer2));

                        currentKey[keyLength]=((byte)(obj & 0xff));
                        currentKey[keyLength+1]=((byte)((obj>>8) & 0xff));
                        currentKey[keyLength+2]=((byte)((obj>>16) & 0xff));
                        currentKey[keyLength+3]=((byte)(gen & 0xff));
                        currentKey[keyLength+4]=((byte)((gen>>8) & 0xff));
                    }

                    finalKey = new byte[Math.min(currentKey.length,16)];

                    if(!ref.isEmpty()){
                        final MessageDigest currentDigest =MessageDigest.getInstance("MD5");
                        currentDigest.update(currentKey);

                        //add in salt
                        if(isAES && keyLength>=16){
                            final byte[] salt = {(byte)0x73, (byte)0x41, (byte)0x6c, (byte)0x54};

                            currentDigest.update(salt);
                        }
                        System.arraycopy(currentDigest.digest(),0, finalKey,0, finalKey.length);
                    }else{
                        System.arraycopy(currentKey,0, finalKey,0, finalKey.length);
                    }
                }

                //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
                /**only initialise once - seems to take a long time*/
                if(cipher==null) {
                    cipher = Cipher.getInstance(algorithm);
                }

                final SecretKey testKey = new SecretKeySpec(finalKey, keyType);

                if(isEncryption) {
                    cipher.init(Cipher.ENCRYPT_MODE, testKey);
                } else{
                    if(ivSpec==null) {
                        cipher.init(Cipher.DECRYPT_MODE, testKey);
                    } else //aes
                    {
                        cipher.init(Cipher.DECRYPT_MODE, testKey, ivSpec);
                    }
                }

                //if data on disk read a byte at a time and write back

                if(streamCache!=null){
                    final CipherInputStream cis=new CipherInputStream(bis,cipher);
                    int nextByte;
                    while(true){
                        nextByte=cis.read();
                        if(nextByte==-1) {
                            break;
                        }
                        streamCache.write(nextByte);
                    }
                    cis.close();
                    streamCache.close();
                    bis.close();

                }

                if(data!=null) {
                    data = cipher.doFinal(data);
                }

            }catch(final Exception e){
                throw new PdfSecurityException("Exception "+e+" decrypting content");
            }
        }

        //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
        if(alwaysReinitCipher) {
            cipher = null;
        }

        return data;
    }

    /**show if file can be displayed*/
    public boolean getBooleanValue(final int key) {

        switch(key){
            case PDFflags.IS_FILE_VIEWABLE:
                return isFileViewable;

            case PDFflags.IS_FILE_ENCRYPTED:
                return isEncrypted;

            case PDFflags.IS_METADATA_ENCRYPTED:
                return isMetaDataEncypted;

            case PDFflags.IS_EXTRACTION_ALLOWED:
                return extractionIsAllowed;

            case PDFflags.IS_PASSWORD_SUPPLIED:
                return isPasswordSupplied;
        }


        return false;
    }
    
    public boolean isAES(){
        return isAES;
    }

    public byte[] decryptString(byte[] newString, final String objectRef) throws PdfSecurityException {

        try{
            if((!isAES || stringsEncoded || isMetaDataEncypted)) {
                newString = decrypt(newString, objectRef, false, null, false, true);
            }
        }catch(final Exception e){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Unable to decrypt string in Object " + objectRef + ' ' + new String(newString)+ ' ' +e);
            }
        }

        return newString;

    }

    public int getPDFflag(final Integer flag) {

        if(flag.equals(PDFflags.USER_ACCESS_PERMISSIONS)) {
            return P;
        } else if(flag.equals(PDFflags.VALID_PASSWORD_SUPPLIED)) {
            return passwordStatus;
        } else {
            return -1;
        }
    }

    public void reset(final byte[] encryptionPassword) {

        this.encryptionPassword=encryptionPassword;

        //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
        //reset
        cipher=null;
    }

    public void flush() {

        if(cachedObjects!=null){
            for (final Object o : cachedObjects.keySet()) {
                final String fileName = (String) o;
                final File file = new File(fileName);
                //System.out.println("PdfFileReader - deleting file "+fileName);
                file.delete();
                if (LogWriter.isOutput() && file.exists()) {
                    LogWriter.writeLog("Unable to delete temp file " + fileName);
                }
            }
        }

    }

    public void dispose() {
        this.cachedObjects=null;

    }

    /**
     * show if U or O value present
     * @return
     */
    public boolean hasPassword() {
        return O!=null || U!=null;
    }
    
    /**
     * decode AES ecnoded data with IV parameters
     * @param password
     * @param encKey
     * @param encData a data gained from deducting IV bytes in beginning (encData = data - ivBytes)
     * @param ivData
     * @return
     * @throws Exception
     */
    private static byte[] decodeAES(final byte[] encKey, final byte[] encData, final byte[] ivData)
            throws Exception {
        
        final KeyParameter keyParam = new KeyParameter(encKey);
        final CipherParameters params = new ParametersWithIV(keyParam, ivData);

        // setup AES cipher in CBC mode with PKCS7 padding
        final BlockCipherPadding padding = new PKCS7Padding();
        final BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new AESEngine()), padding);
        cipher.reset();
        cipher.init(false, params);

        // create a temporary buffer to decode into (it'll include padding)
        final byte[] buf = new byte[cipher.getOutputSize(encData.length)];
        int len = cipher.processBytes(encData, 0, encData.length, buf, 0);
        len += cipher.doFinal(buf, len);

        // remove padding
        final byte[] out = new byte[len];
        System.arraycopy(buf, 0, out, 0, len);

        // return string representation of decoded bytes
        return out;
    }   
    
}
