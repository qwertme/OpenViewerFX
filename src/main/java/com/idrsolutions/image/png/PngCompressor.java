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
 * PngCompressor.java
 * ---------------
 */
package com.idrsolutions.image.png;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import javax.imageio.ImageIO;

/**
 * This class is provided in order to offer command-line access for compressing PNG files.
 * <p>
 * It also exposes static helper methods for Java developers to use.
 * <h2>
 * Command Line Instructions:
 * </h2>
 * <pre><code>
 *     java [options] -jar jdeli.jar compresspng [pngfile [pngfile ...]]
 * </pre></code>
 * <p>
 * options:
 * <ol>
 *     <li>-Doverwrite=true       overwrite input files (default: "false")</li>
 *     <li>-Dverbose=true         print status messages (default: "false")</li>
 * </ol>
 *
 * <h3>Example 1:</h3>
 * <pre><code>
 *     java -jar jdeli.jar compresspng file.png
 * </pre></code>
 *
 * <h3>Example 2:</h3>
 * <pre><code>
 *     java -Doverwrite=true -jar jdeli.jar compresspng /directory/*.png
 * </code></pre>
 *
 * <h3>Example 3:</h3>
 * <pre><code>
 *     java -Dverbose=true -jar jdeli.jar compresspng /directory/*.png
 * </code></pre>
 * <p>
 * The output filename is the same as the input name except that _compressed
 * will be appended to the name. E.g. file.png will become file_compressed.png
 * This can be changed to overwrite the existing file by setting the overwrite
 * setting to true.
 */
public class PngCompressor {

    private static final String FILENAME = "_compressed";
    private static boolean verbose;

    private static void copyFile(final File sourceFile, final File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    private static byte[] getCompressedImageAsByteArray(final BufferedImage image, final int inputLen) throws IOException {
        final ByteArrayOutputStream tempStream = new ByteArrayOutputStream(inputLen/2);
        final PngEncoder encoder = new PngEncoder();
        encoder.setCompressed(true);
        encoder.write(image, tempStream);
        tempStream.flush();
        tempStream.close();

        return tempStream.toByteArray();
    }

    private static void writeSizeMessage(final int originalSize, final int compressedSize) {
        final String diffMessage;

        if (originalSize == compressedSize) {
            diffMessage = " (same size)";
        } else {
            float diff = 1 - ((float) compressedSize / (float) originalSize);
            diff *= 100;

            if (originalSize > compressedSize) {
                diffMessage = " (" + String.format("%.02f", diff) + "% smaller)";
            } else {
                diff = -diff;
                diffMessage = " (" + String.format("%.02f", diff) + "% larger)";
            }
        }

        System.out.println("Original size: " + originalSize);
        System.out.println("Compressed size: " + compressedSize + diffMessage);
    }

    /**
     * compress 24/32 bit images to 8 bit palette image;
     * @param input input file or directory
     * @param output output directory
     * @throws IOException
     */
    public static void compress(final File input, final File output) throws IOException {
        final BufferedImage image;
        try {
            image = ImageIO.read(input);
        } catch (IOException e) {
            System.out.println("error: Failed to read image " + input.getAbsolutePath());
            throw new IOException(e);
        }

        if (image == null) {
            throw new IOException("error: Failed to read image " + input.getAbsolutePath());
        }

        final int inputLen = (int)input.length();

        final byte[] compressedImage = getCompressedImageAsByteArray(image, inputLen);

        if (verbose) {
            System.out.println("Input: " + input.getAbsolutePath());
            System.out.println("Output: " + output.getAbsolutePath());
            writeSizeMessage(inputLen, compressedImage.length);
        }

        if (compressedImage.length < inputLen) {
            if (verbose) {
                System.out.println("Writing compressed image");
            }
            final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(output));
            outputStream.write(compressedImage);
            outputStream.flush();
            outputStream.close();
        } else {
            if (!input.equals(output)) {
                if (verbose) {
                    System.out.println("Writing original image (compressed image is not smaller)");
                }
                copyFile(input, output);
            } else {
                if (verbose) {
                    System.out.println("Compressed image is not smaller, not overwriting");
                }
            }
        }

        if (verbose) {
            System.out.println(); // Empty line
        }
    }

    /**
     * compress 24/32 bit image to 8 bit palette image;
     * <p>
     * This method does not close the provided OutputStream after the write
     * operation has completed; it is the responsibility of the caller to close
     * the stream,
     * </p>
     * @param inputStream PNG file as InputStream
     * @param outputStream Compressed PNG file as OutputStream
     * @throws IOException 
     * 
     */
    public static void compress(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        final byte[] originalImage = getByteArrayFromInputStream(inputStream);
        final InputStream byteArrayInputStream = new ByteArrayInputStream(originalImage);

        final BufferedImage image;
        try {
            image = ImageIO.read(byteArrayInputStream);
        } catch (IOException e) {
            System.out.println("error: Failed to read image");
            throw new IOException(e);
        }

        if (image == null) {
            throw new IOException("error: Failed to read image");
        }

        final byte[] compressedImage = getCompressedImageAsByteArray(image, originalImage.length);

        if (verbose) {
            writeSizeMessage(originalImage.length, compressedImage.length);
        }

        if (compressedImage.length < originalImage.length) {
            if (verbose) {
                System.out.println("Returning compressed image");
            }
            outputStream.write(compressedImage);
        } else {
            if (verbose) {
                System.out.println("Returning original image (compressed image is not smaller)");
            }
            outputStream.write(originalImage);
        }

        if (verbose) {
            System.out.println(); // Empty line
        }
    }

    private static byte[] getByteArrayFromInputStream(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        final byte[] data = new byte[16384]; // 16KB

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        buffer.close();

        return buffer.toByteArray();
    }

    private static boolean processFile(final File inputFile, final boolean overwrite) {

        final String fileLocation = inputFile.getAbsolutePath();
        if (inputFile.exists()) {
            if (fileLocation.toLowerCase().endsWith(".png")) {
                final File outputFile;
                if (overwrite) {
                    outputFile = inputFile;
                } else {
                    final int extIndex = fileLocation.length() - 4;// ".png".length()
                    outputFile = new File(fileLocation.substring(0, extIndex) + FILENAME + fileLocation.substring(extIndex));
                }

                try {
                    compress(inputFile, outputFile);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                System.out.println("error: " + fileLocation + " is not a png file.");
                return false;
            }
        } else {
            System.out.println("error: " + fileLocation + " does not exist.");
            return false;
        }

    }

    /**
     * For instructions, see main Javadoc for class
     * @param args For instructions, see main Javadoc for class
     * @throws IOException
     */
    public static void main(final String[] args) {

        if (args.length == 0) {
            showCommandLineHelp();
        } else {
            boolean failed = false;
            verbose = "true".equalsIgnoreCase(System.getProperty("verbose"));
            final boolean overwrite = "true".equalsIgnoreCase(System.getProperty("overwrite"));

            for (final String arg : args) {
                final boolean success = processFile(new File(arg), overwrite);
                if (!success) {
                    failed = true;
                }
            }

            if (failed) {
                System.exit(1);
            }
        }
    }

    private static void showCommandLineHelp() {

        System.out.println("How to run PngCompressor:\n" +
            '\n' +
            "    java [options] -jar jdeli.jar compresspng [pngfile [pngfile ...]]\n" +
            '\n' +
            "        options:\n" +
            "            1. -Doverwrite=true       overwrite input files (default: \"false\")\n" +
            "            2. -Dverbose=true         print status messages (default: \"false\")\n" +
            '\n' +
            "    Examples:\n" +
            "        java -jar jdeli.jar compresspng file.png\n" +
            '\n' +
            "        java -Doverwrite=true -jar jdeli.jar compresspng /directory/*.png\n" +
            '\n' +
            "        java -Dverbose=true -jar jdeli.jar compresspng /directory/*.png\n" +
            '\n' +
            "    The output filename is the same as the input name except that _compressed\n" +
            "    will be appended to the name. E.g. file.png will become file_compressed.png\n" +
            "    This can be changed to overwrite the existing file by setting the overwrite\n" +
            "    setting to true.");
    }
    
}
