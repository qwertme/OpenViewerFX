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
 * ExtractSelectionAsImage.java
 * ---------------
 */

package org.jpedal.examples.viewer.commands;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.handlers.DefaultImageHelper;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.generic.GUIExtractSelectionAsImage;
import org.jpedal.examples.viewer.utils.FileFilterer;
import org.jpedal.examples.viewer.utils.IconiseImage;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * This class is a Swing specific class to hold the Swing code for
 * Extracting the drawn CursorBox as an Image.
 */
public class ExtractSelectionAsImage extends GUIExtractSelectionAsImage {
  public static void execute(final Values commonValues, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf) {
      extractSelectedScreenAsImage(commonValues,currentGUI,decode_pdf); //Calls the generic code.
        /**
         * put in panel
         */
        //if(temp!=null){
        final JPanel image_display = new JPanel();
        image_display.setLayout( new BorderLayout() );
        
        //wrap image so we can display
        if( snapShot != null ){
            final IconiseImage icon_image = new IconiseImage( snapShot );
            
            //add image if there is one
            image_display.add( new JLabel( icon_image ), BorderLayout.CENTER );
        }else{
            return;
        }
        
        final JScrollPane image_scroll = new JScrollPane();
        image_scroll.getViewport().add( image_display );
        
        //set image size
        int imgSize=snapShot.getWidth();
        if(imgSize<snapShot.getHeight()) {
            imgSize = snapShot.getHeight();
        }
        imgSize += 50;
        if(imgSize>450) {
            imgSize = 450;
        }
        
        /**resizeable pop-up for content*/
        final Container frame = (Container)currentGUI.getFrame();
        final JDialog displayFrame =  new JDialog((JFrame)null,true);
        displayFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        if(commonValues.getModeOfOperation()!=Values.RUNNING_APPLET){
            displayFrame.setLocationRelativeTo(null);
            displayFrame.setLocation(frame.getLocationOnScreen().x+10,frame.getLocationOnScreen().y+10);
        }
        
        displayFrame.setSize(imgSize,imgSize);
        displayFrame.setTitle(Messages.getMessage("PdfViewerMessage.SaveImage"));
        displayFrame.getContentPane().setLayout(new BorderLayout());
        displayFrame.getContentPane().add(image_scroll,BorderLayout.CENTER);
        
        final JPanel buttonBar=new JPanel();
        buttonBar.setLayout(new BorderLayout());
        displayFrame.getContentPane().add(buttonBar,BorderLayout.SOUTH);
        
        /**
         * yes option allows user to save content
         */
        final JButton yes=new JButton(Messages.getMessage("PdfMessage.Yes"));
        yes.setFont(new Font("SansSerif", Font.PLAIN, 12));
        buttonBar.add(yes,BorderLayout.WEST);
        yes.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                
                displayFrame.setVisible(false);
                
                File file;
                StringBuffer fileToSave;
                boolean finished=false;
                while(!finished){
                    
                    /**
                     * Sets the saveable file-types for snapshot image.
                     */
                    final JFileChooser chooser = new JFileChooser(System.getProperty( "user.dir" ) );
                    final FileFilterer defaultOpt = new FileFilterer( new String[] { "png","png" }, "PNG" );
                    chooser.setFileFilter(defaultOpt); //sets the default file-type to PNG
                    chooser.addChoosableFileFilter( new FileFilterer( new String[] { "tif", "tiff" }, "TIFF" ) ); //adds Tiff as a saveable filetype option
                    chooser.addChoosableFileFilter( new FileFilterer( new String[] { "jpg","jpeg" }, "JPEG" ) ); //adds jpeg as a saveable filetype option
                    
                    
                    final int approved = chooser.showSaveDialog(image_scroll);
                    
                    if(approved==JFileChooser.APPROVE_OPTION){
                        
                        file = chooser.getSelectedFile();
                        fileToSave= new StringBuffer (file.getAbsolutePath());
                        
                        String format=chooser.getFileFilter().getDescription();
                        
                        if(format.equals("All Files")) {
                            format = "TIFF";
                        }
                        
                        if(!fileToSave.toString().toLowerCase().endsWith(('.' +format).toLowerCase())){
                            fileToSave.append('.').append(format);
                            file=new File(fileToSave.toString());
                        }
                        
                        if(file.exists()){
                            
                            final int n=currentGUI.showConfirmDialog(fileToSave.append('\n') +
                                    Messages.getMessage("PdfViewerMessage.FileAlreadyExists")+".\n" +
                                    Messages.getMessage("PdfViewerMessage.ConfirmResave"),
                                    Messages.getMessage("PdfViewerMessage.Resave"),JOptionPane.YES_NO_OPTION);
                            if(n==1) {
                                continue;
                            }
                        }
                        
                        //Do the actual save
                        if(snapShot!=null) {
                            
                            try {
                                DefaultImageHelper.write(snapShot, format, fileToSave.toString());
                            } catch (IOException ex) {
                                if(LogWriter.isOutput()) {
                                    LogWriter.writeLog("Exception in writing image "+ex);
                                }
                            }    
                        }
                        finished=true;
                    }else{
                        return;
                    }
                }
                
                displayFrame.dispose();
                
            }
        });
        
        /**
         * no option just removes display
         */
        final JButton no=new JButton(Messages.getMessage("PdfMessage.No"));
        no.setFont(new Font("SansSerif", Font.PLAIN, 12));
        buttonBar.add(no,BorderLayout.EAST);
        no.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                displayFrame.dispose();
            }});
        
        /**show the popup*/
        displayFrame.setVisible(true);
    }
}
