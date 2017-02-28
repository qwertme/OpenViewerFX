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
 * Values.java
 * ---------------
 */
package org.jpedal.examples.viewer;

import org.jpedal.utils.LogWriter;

import java.awt.image.BufferedImage;


/**provides access to values used by multiple classes*/
public class Values {
    
    
    public static boolean openingTransferedFile;
    
    private boolean allHighlightsShown;
    
    /**image if file tiff or png or jpg*/
    private BufferedImage img;
    
    /**Multi page tiff image loading*/
    private int tiffImageToLoad;
    
    /**Normal mode (works for webstart, application)*/
    public static final int RUNNING_NORMAL = 0;
    public static final int RUNNING_APPLET = 1;
    public static final int RUNNING_WEBSTART = 2;
    public static final int RUNNING_JSP = 3;
    public static final int RUNNING_PLUGIN = 4;
    
    /**atomic lock for open thread*/
    private boolean fileIsURL;
    
    /**flag to show if an encryption library is available*/
    private boolean isEncryptOnClasspath;
    
    /**flag to show if file opened is PDF or not*/
    private boolean isPDF=true;
    
    /**flag to show if the file opened is a Tiff with multiple pages or not*/
    private boolean isMultiTiff;
    
    /**allow common code to be aware if applet or webstart or JSP*/
    private int modeOfOperation;
    
    /**size of file for display*/
    private long size;
    
    /**directory to load files from*/
    private  String inputDir;
    
    /**current page number*/
    private int currentPage = 1;
    
    /**name of current file being decoded*/
    private String selectedFile;
    
    /**flag to show that form values have been altered by user*/
    private boolean formsChanged;

    /**uses hires images for display (uses more memory)*/
    private boolean useHiresImage=true;
    
    public int m_x1, m_y1, m_x2, m_y2;
    
    /**offsets to viewport if used*/
    public int dx,dy;
    
    /**scaling on viewport if used*/
    public double viewportScale=1;
    
    /**height of the viewport. Because everything is draw upside down we need this
     * to get true y value*/
    public int maxViewY;
    
    /**number of pages in current pdf
     * (inclusive so 2 page doc would have 2 with first page as 1)*/
    private int pageCount = 1;
    private int maxNoOfMultiViewers;
    
    /**boolean lock to stop multiple access*/
    //
     public static boolean isProcessing = false;
      /**/
    
    public Values(){
        
        String altSP=System.getProperty("org.jpedal.securityprovider");
        
        try {
            if(altSP==null) {
                altSP = "/org/bouncycastle/";
            }
            
            isEncryptOnClasspath = getClass().getResource(altSP) != null;
        }catch(final Exception e){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in encryption "+e);
            }
            isEncryptOnClasspath=false;
        }catch(final Error e){
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in encryption "+e);
            }
            isEncryptOnClasspath=false;
        }
    }
    
    /**
     *flag to show isProcessing so Viewer can lock actions while decoding page
     * @return the isProcessing flag
     */
    public static boolean isProcessing() {
        return isProcessing;
    }
    
    /**
     * set to show decoding page
     * @param isProcessing value of the decoding page
     */
    public static void setProcessing(final boolean isProcessing) {
        Values.isProcessing = isProcessing;
    }
    
    public boolean isEncrypOnClasspath() {
        return isEncryptOnClasspath;
    }
    
    /**
     * show if file is type PDF
     * @return the isPDF 
     */
    public boolean isPDF() {
        return isPDF;
    }
    
    public static void setOpeningTransferedFile(final boolean b){
        openingTransferedFile = b;
    }
    
    public static boolean getOpeningTransferedFile(){
        return openingTransferedFile;
    }
    
    /**
     * set flag to show if file is PDF or other
     * @param isPDF To find if file is PDF or other
     */
    public void setPDF(final boolean isPDF) {
        this.isPDF = isPDF;
    }
    
    /**
     *get current page number (1 - pageCount)
     * @return the currentPage
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * set current page number (1 - pageCount)
     * @param currentPage The new currentPage
     */
    public void setCurrentPage(final int currentPage) {
        this.currentPage = currentPage;
    }
    
    /**
     * get directory to use as input root
     * @return the inputDir
     */
    public String getInputDir() {
        
        if(inputDir==null) {
            inputDir = System.getProperty("user.dir");
        }
        
        return inputDir;
    }
    
    /**
     * set directory to use as input root
     * @param inputDir The new directory used as input root
     */
    public void setInputDir(final String inputDir) {
        this.inputDir = inputDir;
    }
    
    /**
     * get current filename
     * @return the selectedFile
     */
    public String getSelectedFile() {
        return selectedFile;
    }
    
    /**
     * set current filename
     * @param selectedFile The current filename
     */
    public void setSelectedFile(final String selectedFile) {
        this.selectedFile = selectedFile;
    }
    
    /**
     * 
     * @param b is a boolean and sets fileIsURL
     */
    public void setFileIsURL(final boolean b){
        fileIsURL = b;
    }
    /**
     * 
     * @return boolean fileIsURL
     */
    public boolean getFileIsURL(){
        return fileIsURL;
    }
    
    /**
     * @return if user has edited forms
     */
    public boolean isFormsChanged() {
        return formsChanged;
    }
    
    /**
     * set user has edited forms
     * @param formsChanged the value of edited forms
     */
    public void setFormsChanged(final boolean formsChanged) {
        this.formsChanged = formsChanged;
    }
    
    /**
     * get number of pages
     * @return the number of pages
     */
    public int getPageCount() {
        return pageCount;
    }
    
    /**
     * set number of pages
     * @param pageCount The new number of pages
     */
    public void setPageCount(final int pageCount) {
        this.pageCount = pageCount;
    }
    
    /**
     * get current file size in kilobytes
     * @return the size of the file in kilobytes
     */
    public long getFileSize() {
        return size;
    }
    
    /**
     * set current file size in kilobytes
     * @param size The new size of the file in kilobytes
     */
    public void setFileSize(final long size) {
        this.size = size;
    }

    
    /**
     * get modeOfOperation (RUNNING_NORMAL,RUNNING_APPLET,RUNNING_WEBSTART,RUNNING_JSP)
     * @return the modeOfOperation
     */
    public int getModeOfOperation() {
        return modeOfOperation;
    }
    
    /**
     * set modeOfOperation (RUNNING_NORMAL,RUNNING_APPLET,RUNNING_WEBSTART,RUNNING_JSP)
     * @param modeOfOperation The new modeOfOperation
     */
    public void setModeOfOperation(final int modeOfOperation) {
        this.modeOfOperation = modeOfOperation;
    }
    
    /**
     * flag to show if using images as hires
     * @return the useHiresImage flag
     */
    public boolean isUseHiresImage() {
        return useHiresImage;
    }
    
    /**
     * set to show images being used are hires and not downsampled
     * @param useHiresImage to show images being used are hires
     */
    public void setUseHiresImage(final boolean useHiresImage) {
        this.useHiresImage = useHiresImage;
    }
    
    public boolean isMultiTiff() {
        return isMultiTiff;
    }
    
    public void setMultiTiff(final boolean isMultiTiff) {
        this.isMultiTiff = isMultiTiff;
    }
    
    public int getTiffImageToLoad(){
        return tiffImageToLoad;
    }
    
    public void setTiffImageToLoad(final int x){
        tiffImageToLoad = x;
    }
    
    public void setMaxMiltiViewers(final int maxNoOfMultiViewers) {
        this.maxNoOfMultiViewers = maxNoOfMultiViewers;
    }
    
    public int getMaxMiltiViewers() {
        return maxNoOfMultiViewers;
    }
    
    public void setAllHighlightsShown(final boolean b){
        allHighlightsShown = b;
    }
    
    public boolean getAllHighlightsShown(){
        return allHighlightsShown;
    }
    
    public void setBufferedImg(final BufferedImage img){
        this.img = img;
    }
    
    public BufferedImage getBufferedImg(){
        return img;
    }
  
}
