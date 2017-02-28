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
 * SoundHandler.java
 * ---------------
 */

package org.jpedal.objects.acroforms.actions;

import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.utils.LogWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.*;

public class SoundHandler {
    
    private static final int EXTERNAL_BUFFER_SIZE = 128000;
    private static int frameSize;
    private static float sampleRate = 44100F;
    private static int sampleSizeInBits = 16;
    private static int channels = 2;
    private static AudioFormat.Encoding  encoding = AudioFormat.Encoding.PCM_SIGNED;
    
    public static void setAudioFormat(final int audioFormat, final int b, final float r, final int c) {
        
        sampleSizeInBits = b;
        sampleRate = r;
        channels = c;
        
        if(audioFormat!= PdfDictionary.Signed){
            if(audioFormat==PdfDictionary.Unsigned){
                encoding = AudioFormat.Encoding.PCM_UNSIGNED;
            }else{
                throw new RuntimeException("AudioFormat currently unsupported! - ");
            }
        }
    }
    
    private static AudioFormat getAudioFormat() {
        
        /** frame size = sample size in *bytes* multiplied by channels */
        frameSize = (sampleSizeInBits / 8) * channels;
        
        /**
         * not sure how to calculate frame rate but saw some other files
         * where it was just the same as sample rate, and in the cases I have
         * seen it seems to work
         */
        final int frameRate = (int) sampleRate;
        
        /**
         * according to the PDF Spec (pg 740) samples larger than 8 bits are
         * big endian
         */

        /**
         * also works, but would probably need the big constructor to handle
         * different types of encoding other than Signed and Unsigned
         */
        //AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSizeInBits,
        //	channels, true, true);
        
        return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, true);
    }
    
    public  static AudioInputStream getAudioInputStream(final byte[] data) {
        final AudioFormat audioFormat = getAudioFormat();
        
        /**
         * Taken from PlaySound
         */
        final long length = data.length / frameSize;

        return new AudioInputStream(
                new ByteArrayInputStream(data),
                audioFormat, length );
    }
    
    public static void PlaySound(final byte[] data) throws Exception{
        
        final AudioFormat audioFormat = getAudioFormat();
        
        /**
         * not massively convinced if this is the correct formula for calculating
         * the length, it works for a couple of files though, so may be right.
         */
        final long length = data.length / frameSize;
        
        final AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(data),
                audioFormat, length );
        
        
        playSoundFromStream(ais);
        
    }
    
    private static void playSoundFromStream(final AudioInputStream ais) {
        SourceDataLine line = null;
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, ais.getFormat());
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            
            line.open(ais.getFormat());
        } catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        line.start();
        
        int nBytesRead = 0;
        final byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
        while (nBytesRead != -1) {
            try {
                nBytesRead = ais.read(abData, 0, abData.length);
            } catch (final IOException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
            if (nBytesRead >= 0) {
                // do not comment out this line -> (no code == no sound)
                line.write(abData, 0, nBytesRead);
            }
        }
        line.drain();
        
        line.close();
    }  
}