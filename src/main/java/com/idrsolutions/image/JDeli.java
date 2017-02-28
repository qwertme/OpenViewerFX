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
 * JDeli.java
 * ---------------
 */
package com.idrsolutions.image;

import com.idrsolutions.image.png.PngCompressor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import javax.swing.*;

public class JDeli {

    private enum Action {
        COMPRESSPNG;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private static void doAction(final String actionAsString, final String[] args) {

        Action action;

        try {
            action = Action.valueOf(actionAsString.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            action = null;
        }

        if (action == null) {
            System.out.println("JDeli action '" + actionAsString + "' not recognised. Available actions are: " + Arrays.toString(Action.values()));
        } else {
            switch (action) {
                case COMPRESSPNG:
                    PngCompressor.main(args);
                    break;
            }
        }
    }

    /**
     * routine to return new array with first value removed
     * @param inputValues - does not check for null or single value
     * @return new array
     */
    private static String[] removeFirstValue(final String[] inputValues) {
     
        final int newArraySize = inputValues.length - 1;

        final String[] outputValues = new String[newArraySize];

        System.arraycopy(inputValues, 1, outputValues, 0, newArraySize);

        return outputValues;
    }

    private static final Color BACKGROUND_COLOR = new Color(84, 130, 31);
    private static final Color FOREGROUND_COLOR = Color.WHITE;
    private static final Font FONT = new Font("SansSerif", Font.BOLD, 13);

    private static JLabel getStyledLabel(final JLabel label) {
        label.setFont(FONT);
        label.setForeground(FOREGROUND_COLOR);
        return label;
    }

    private static void showHelpWindow() {
        final JFrame jf = new JFrame("JDeli - Java Decoding and Encoding Library for Images");
        jf.setSize(440, 400);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_COLOR);
        panel.setLayout(new GridLayout(0, 1));

        final ImageIcon logo = new ImageIcon(JDeli.class.getClass().getResource("/com/idrsolutions/image/res/logo.png"));
        final JLabel idrLogo = new JLabel(logo);
        jf.getContentPane().add(idrLogo, BorderLayout.NORTH);

        panel.add(getStyledLabel(new JLabel(" JDeli - Java Decoding and Encoding Library for Images")));
        panel.add(getStyledLabel(new JLabel(" ")));
        panel.add(getStyledLabel(new JLabel(" Features: ")));
        panel.add(getStyledLabel(new JLabel("          TiffEncoder (Single/Multi Page)")));
        panel.add(getStyledLabel(new JLabel("          TiffDecoder (Single/Multi Page)")));
        panel.add(getStyledLabel(new JLabel("          PngEncoder")));
        panel.add(getStyledLabel(new JLabel("          PngCompressor")));
        panel.add(getStyledLabel(new JLabel("          JpegEncoder")));
        panel.add(getStyledLabel(new JLabel("          JpegDecoder")));
        panel.add(getStyledLabel(new JLabel("          Jpeg2000Decoder")));
        panel.add(getStyledLabel(new JLabel(" ")));

        final JLabel homeURL = new JLabel("<html>&nbsp;Homepage: https://www.idrsolutions.com/jdeli</html>");
        homeURL.addMouseListener(new MouseListener() {

            @Override
            public void mouseEntered(final MouseEvent e) {
                panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                homeURL.setText("<html>&nbsp;Homepage: <u>https://www.idrsolutions.com/jdeli</u></html>");
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                homeURL.setText("<html>&nbsp;Homepage: https://www.idrsolutions.com/jdeli</html>");
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.idrsolutions.com/jdeli"));
                } catch (final IOException e1) {
                    writeLog("Exception: " + e1.getMessage());
                } catch (URISyntaxException ex) {
                    writeLog("Exception: " + ex.getMessage());
                }
            }

            @Override public void mousePressed(final MouseEvent e) { }
            @Override public void mouseReleased(final MouseEvent e) { }
        });
        panel.add(getStyledLabel(homeURL));

        final JLabel licenceURL = new JLabel("<html>&nbsp;License: https://www.idrsolutions.com/jdeli/license</html>");
        licenceURL.addMouseListener(new MouseListener() {

            @Override
            public void mouseEntered(final MouseEvent e) {
                panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                licenceURL.setText("<html>&nbsp;License: <u>https://www.idrsolutions.com/jdeli/license</u></html>");
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                licenceURL.setText("<html>&nbsp;License: https://www.idrsolutions.com/jdeli/license</html>");
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.idrsolutions.com/jdeli/license"));
                } catch (final URISyntaxException e1) {
                    writeLog("Exception: " + e1.getMessage());
                } catch (IOException ex) {
                    writeLog("Exception :" + ex.getMessage());
                }
            }

            @Override public void mousePressed(final MouseEvent e) { }
            @Override public void mouseReleased(final MouseEvent e) { }
        });
        panel.add(getStyledLabel(licenceURL));


        final JLabel javadocURL = new JLabel("<html>&nbsp;Javadoc: https://files.idrsolutions.com/jdeli-javadoc/</html>");
        javadocURL.addMouseListener(new MouseListener() {

            @Override
            public void mouseEntered(final MouseEvent e) {
                panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                javadocURL.setText("<html>&nbsp;Javadoc: <u>https://files.idrsolutions.com/jdeli-javadoc/</u></html>");
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                javadocURL.setText("<html>&nbsp;Javadoc: https://files.idrsolutions.com/jdeli-javadoc/</html>");
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://files.idrsolutions.com/jdeli-javadoc/"));
                } catch (final IOException e1) {
                    writeLog("Exception: " + e1.getMessage());
                } catch (URISyntaxException ex) {
                    writeLog("Exception: " + ex.getMessage()); 
                }
            }

            @Override public void mousePressed(final MouseEvent e) { }
            @Override public void mouseReleased(final MouseEvent e) { }
        });
        panel.add(getStyledLabel(javadocURL));
        panel.add(getStyledLabel(new JLabel(" ")));
        panel.add(getStyledLabel(new JLabel(" Available command line arguments are: " + Arrays.toString(Action.values()))));
        panel.add(getStyledLabel(new JLabel(" ")));

        jf.getContentPane().add(panel, BorderLayout.CENTER);

        final JPanel bPanel = new JPanel();
        bPanel.setBackground(BACKGROUND_COLOR);

        final JButton button = new JButton("OK");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                jf.dispose();
            }
        });
        button.setSize(150, 75);
        button.setMaximumSize(new Dimension(150, 75));

        bPanel.add(button);
        jf.getContentPane().add(bPanel, BorderLayout.SOUTH);

        jf.setVisible(true);
        jf.setLocationRelativeTo(null);
        jf.setResizable(false);
    }

    private static void writeLog(final String msg) {
        System.out.println(msg);
    }

    public static void main(final String[] args) {
        if (args.length == 0) {
            showHelp();
        } else {
            doAction(args[0], removeFirstValue(args));
        }
    }

    private static void showHelp() {
        final String info = "JDeli - Java Decoding and Encoding Library for Images\n" +
            '\n' +
                "Features:\n" +
                "\tTiffEncoder (Single/Multi Page)\n" +
                "\tTiffDecoder (Single/Multi Page)\n" +
                "\tPngEncoder\n" +
                "\tPngCompressor\n" +
                "\tJpegEncoder\n" +
                "\tJpegDecoder\n" +
                "\tJpeg2000Decoder\n" +
            '\n' +
                "Homepage: https://www.idrsolutions.com/jdeli\n" +
                "License: https://www.idrsolutions.com/jdeli/license\n" +
                "Javadoc: https://files.idrsolutions.com/jdeli-javadoc/\n" +
            '\n' +
                "Available command line arguments are: " + Arrays.toString(Action.values());


        if (System.console() != null) {
            System.out.println(info);
        } else if (!GraphicsEnvironment.isHeadless()) {
            showHelpWindow();
        }
        // Else... User is not going to see any message. (log to file?)

    }

}
