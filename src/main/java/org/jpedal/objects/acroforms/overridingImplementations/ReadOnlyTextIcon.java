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
 * ReadOnlyTextIcon.java
 * ---------------
 */
package org.jpedal.objects.acroforms.overridingImplementations;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.*;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.acroforms.GUIData;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.XObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

/** this class is used to display the text fields in the defined font, but is used for readonly fields only. */
public class ReadOnlyTextIcon extends CustomImageIcon implements Icon, SwingConstants {
    // <start-demo><end-demo>
    
    /** used to tell the paint method that we need to scale up the image for printing */
    private boolean currentlyPrinting;
    private int printMultiplier = 1;
    
    private int alignment=-1;
    
    private static final long serialVersionUID = 8946195842453749725L;
    
    /** stores the root image for selected and unselected icons */
    private BufferedImage rootImage;
    /** stores the final image after any icon rotation */
    private BufferedImage finalImage;
    
    private PdfObject fakeObj;
    
    /** tells us if the text for this icon has chnaged and so if we need to redraw the icon*/
    private boolean textChanged;
    private String preFontStream="",betweenFontAndTextStream="",afterTextStream="",text="";
    
    private String fontName="",fontSize="12",fontCommand="";
    
    /** our full command Stream*/
    private String fullCommandString;
    
    private final PdfObjectReader currentpdffile;
    private int subtype=-1;
    private final PdfObject resources;
    
    private final PdfObject form;
    
    /** new code to store the data to create the image when needed to the size needed
     * offset = if 0 no change, 1 offset image, 2 invert image
     * <br> NOTE if decipherAppObject ios not called this will cause problems.
     */
    public ReadOnlyTextIcon(final PdfObject form, final int iconRot, final PdfObjectReader pdfObjectReader, final PdfObject res){
        super(iconRot);
        
        this.form=form;
        currentpdffile = pdfObjectReader;
        resources = res;
        
        //        if(selObj.getObjectRefAsString().equals("128 0 R") || selObj.getObjectRefAsString().equals("130 0 R"))
        //			debug = true;
    }
    
    /** returns the currently selected Image*/
    @Override
    public Image getImage(){
        final Image image;
        checkAndCreateimage();
        
        image = finalImage;
        
        return image;
    }
    
    /** draws the form to a BufferedImage the size of the Icon and returns it,
     * uses the paintIcon method for the drawing so future changes should only be in one place
     */
    public BufferedImage drawToBufferedImage(){
        final BufferedImage bufImg = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics g = bufImg.getGraphics();
        paintIcon(null, g, 0, 0);
        g.dispose();
        
        // <start-demo><end-demo>
        
        return bufImg;
    }
    
    @Override
    public synchronized void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        // <start-demo><end-demo>
        
        final BufferedImage image = (BufferedImage) getImage();
        
        if (image == null) {
            return;
        }
        
        if (c!=null && c.isEnabled()) {
            g.setColor(c.getBackground());
        } else {
            g.setColor(Color.gray);
        }
        
        // <start-demo><end-demo>
        
        final Graphics2D g2 = (Graphics2D) g;
        if (iconWidth > 0 && iconHeight > 0) {
            
            int drawWidth = iconWidth;
            int drawHeight = iconHeight;
            if(displaySingle && (iconRotation==270 || iconRotation==90)){
                //swap width and height so that the image is drawn in the corect orientation
                //without changing the raw width and height for the icon size
                drawWidth = iconHeight;
                drawHeight = iconWidth;
            }
            
            //only work out scaling if we have a dictionary of an image, as otherwise it could be a blank image (i.e. 1 x 1).
            if(currentpdffile!=null){
                //work out w,h which we want to draw inside our icon to maintain aspect ratio.
                final float ws = (float)drawWidth / (float)image.getWidth(null);
                final float hs = (float)drawHeight / (float)image.getHeight(null);
                if(ws<hs){
                    drawWidth = (int)(ws * image.getWidth(null));
                    drawHeight = (int)(ws * image.getHeight(null));
                }else {
                    drawWidth = (int)(hs * image.getWidth(null));
                    drawHeight = (int)(hs * image.getHeight(null));
                }
            }
            
            //now work out the x,y position to keep the icon in the centre of the icon
            int posX=0,posY=0;
            if(currentpdffile!=null){
                if(displaySingle && (iconRotation==270 || iconRotation==90)){
                    posX = (iconHeight-drawWidth)/2;
                    posY = (iconWidth-drawHeight)/2;
                }else {
                    posX = (iconWidth-drawWidth)/2;
                    posY = (iconHeight-drawHeight)/2;
                }
            }
            
            if(alignment==JTextField.LEFT) {
                posX = 0;
            }
            
            final int finalRotation;
            if(displaySingle){
                finalRotation = validateRotationValue(pageRotate - iconRotation);
            }else {
                finalRotation = pageRotate;
            }
            
            /** with new decode at needed size code the resize (drawImage) may not be needed. */
            if (finalRotation ==270) {
                g2.rotate(-Math.PI / 2);
                g2.translate(-drawWidth, 0);
                g2.drawImage(image, -posX, posY, drawWidth, drawHeight, null);
            } else if (finalRotation == 90) {
                g2.rotate(Math.PI / 2);
                g2.translate(0, -drawHeight);
                g2.drawImage(image, posX, -posY, drawWidth, drawHeight, null);
            } else if (finalRotation == 180) {
                g2.rotate(Math.PI);
                g2.translate(-drawWidth, -drawHeight);
                g2.drawImage(image, -posX, -posY, drawWidth, drawHeight, null);
            }else {
                g2.drawImage(image, posX, posY, drawWidth, drawHeight, null);
            }
        } else {
            g2.drawImage(image, 0, 0, null);
        }
        
        g2.translate(-x, -y);
    }
    
    private void checkAndCreateimage() {
        //check if pdf object reader is defined, as we still use opaque images which do NOT need redecoding
        if(currentpdffile==null) {
            return;
        }
        
        /** NOTE the image code may need changing so that we store up to a certain size image
         *  and not store large images, once the user has rescaled to a more normal size.
         *  we could store the root width and height for the 100% size and use 200% as the
         *  highest image size to keep.
         *
         *  if we do this the best way would be to have an object that we move the decode routine to, and
         *  then when we read the 100% values from the image object we can store them in that size.
         */
        
        //define normal sizes for normal use
        int newWidth = iconWidth,newHeight = iconHeight;
        
        //if printing define larger sizes for root image, but dont change icon height and width
        if(currentlyPrinting){
            newWidth = iconWidth * printMultiplier;
            newHeight = iconHeight * printMultiplier;
        }
        
        //decode images at needed size
        if(textChanged || rootImage==null
                || newWidth > (rootImage.getWidth(null))
                || newHeight > (rootImage.getHeight(null))
                || newWidth < (rootImage.getWidth(null)/MAXSCALEFACTOR)
                || newHeight < (rootImage.getHeight(null)/MAXSCALEFACTOR)
                // <start-demo><end-demo>
                ){
            //System.out.println(fakeObj.getObjectRefAsString()+" command="+fullCommandString);
            rootImage = FormStream.decode(form,currentpdffile, fakeObj, subtype,newWidth,newHeight,0,1);
            // <start-demo><end-demo>
            
            finalImage = FormStream.rotate(rootImage,iconRotation);
            // <start-demo><end-demo>
            
            //make text as redrawn
            textChanged = false;
            
        }//icon rotation is always defined in the constructor so we dont need to change it
    }
    public void setText(String str){
        if(str==null) {
            str = "";
        }
        
        if(str.equals(text)) {
            return;
        }
        
        textChanged = true;
        this.text = str;
        
        //check afterTextStream to try and sto duplicate TJs with same wording
        final PdfObject xobj = new PdfObject("1 10 X");
        while(true){
            xobj.setDecodedStream(StringUtils.toBytes(afterTextStream));
            final String tj = FormStream.decipherTextFromAP(currentpdffile,xobj);
            if(tj!=null && text.contains(tj)){
                final int endOfTj = afterTextStream.indexOf(" Tj", afterTextStream.indexOf(tj))+3;
                afterTextStream = afterTextStream.substring(endOfTj);
                if(afterTextStream.isEmpty()) {
                    break;
                }
            }else {
                break;
            }
        }
        
        try {
        	
        	if(text.contains("\n")){//multiline text
                final StringTokenizer lines=new StringTokenizer(text,"\n",false);
                String nextLine;
              //Add 2 to simulate form padding
                String textAlignment;
                int alignmentX = 2;
                int alignmentY = ((FormObject)form).getBoundingRectangle().height;
                
                textAlignment = " "+alignmentX+ ' ' +alignmentY+" Td ";
                
                this.fullCommandString = preFontStream+fontName+fontSize+fontCommand+
                        betweenFontAndTextStream + textAlignment;
                
//                int y = ((FormObject)form).getBoundingRectangle().height;
                int xPoint = 0;
                
                while(lines.hasMoreTokens()){
                    
                    nextLine=lines.nextToken();
                    final FontMetrics fm = new Canvas().getFontMetrics(new Font(fontName, Font.PLAIN, (int)Float.parseFloat(fontSize)));
                	final Rectangle2D r = fm.getStringBounds(nextLine, null);

                    if(((FormObject)form).getAlignment()!=-1){

                    	
                    	switch(((FormObject)form).getAlignment()){
                    	case SwingConstants.LEFT :
                    		alignmentX=0;
                    		break;
                    	case SwingConstants.CENTER : 
                    		alignmentX = ((int)(((FormObject)form).getBoundingRectangle().width - r.getWidth()))/2;
                    		alignmentX -= xPoint;
                    		break;
                    	case SwingConstants.RIGHT : 
                    		alignmentX = ((int)(((FormObject)form).getBoundingRectangle().width - r.getWidth()));
                    		alignmentX -= xPoint;
                    		break;
                    	}
                    	
                    }

                	alignmentY = (int)-(r.getHeight()+2);
                	
                    //Construction alignment string
                    textAlignment = " "+alignmentX+ ' ' +alignmentY+" Td ";
                    
                    this.fullCommandString += textAlignment+'(' +nextLine+")Tj ";
                    
                    xPoint += alignmentX;
                }
                this.fullCommandString += afterTextStream;   
                
            }else{
            	
                //Add 2 to simulate form padding
                int alignmentX = 2;
                int alignmentY = ((int) (((FormObject) form).getBoundingRectangle().height - Float.parseFloat(fontSize))) / 2;
                if(alignmentY<2){
                    alignmentY = 2;
                }
                
                if (((FormObject) form).getAlignment() != SwingConstants.LEFT) {

                    final FontMetrics fm = new Canvas().getFontMetrics(new Font(fontName, Font.PLAIN, (int) Float.parseFloat(fontSize)));
                    final Rectangle2D r = fm.getStringBounds(text, null);

                    switch (((FormObject) form).getAlignment()) {
                        case SwingConstants.CENTER:
                            alignmentX = ((int) (((FormObject) form).getBoundingRectangle().width - r.getWidth())) / 2;
                            break;
                        case SwingConstants.RIGHT:
                            alignmentX = ((int) (((FormObject) form).getBoundingRectangle().width - r.getWidth())) - 2;
                            break;
                    }
                }
                
                //Construction alignment string
                String textAlignment = alignmentX+" "+alignmentY+" Td ";
                
                this.fullCommandString = preFontStream+fontName+fontSize+fontCommand+
                        betweenFontAndTextStream+ textAlignment+'(' +text+")Tj "+afterTextStream;
                
            }
        	
        	Color BG = null;
//        	Color BC = new Color(0, 0, 0, 0);
        	if(form.getDictionary(PdfDictionary.MK)!=null){
        		final PdfObject MK = form.getDictionary(PdfDictionary.MK);
//        		float[] bc = MK.getFloatArray(PdfDictionary.BC);
        		final float[] bg = MK.getFloatArray(PdfDictionary.BG);
        		
        		
        		/*
        		 * Some files do not store colour values between 0 and 1.
        		 * Catch these and convert them
        		 */
        		//Check BG values
        		if(bg!=null){
        			boolean colorOutOfBounds = false;
        			for(int i=0; i!=bg.length; i++){
        				if(bg[i]>1.0f){
        					colorOutOfBounds = true;
        					break;
        				}
        			}
        			if(colorOutOfBounds){
        				for(int i=0; i!=bg.length; i++){
        					if(bg[i]>1.0f) {
                                bg[i] /= 255;
                            }
        				}
        			}
        		}
        		
        		//Check BC values
//        		if(bc!=null){
//        			boolean colorOutOfBounds = false;
//        			for(int i=0; i!=bc.length; i++){
//        				if(bc[i]>1.0f){
//        					colorOutOfBounds = true;
//        					break;
//        				}
//        			}
//        			if(colorOutOfBounds){
//        				for(int i=0; i!=bc.length; i++){
//        					if(bg[i]>1.0f)
//        						bc[i] = bc[i]/255;
//        				}
//        			}
//        		}
//        		
//        		
//        		if(bc != null && bc.length>0){
//        			switch(bc.length){
//        			case 1 : 
//        				BC = new Color(bc[0],bc[0],bc[0],1.0f);
//        				break;
//        			case 3 : 
//        				BC = new Color(bc[0],bc[1],bc[2],1.0f);
//        				break;
//        			case 4 : 
////        				BC = new Color(bc[0],bc[0],bc[0],1.0f);
//        				break;
//        			}
//        		}

        		if(bg != null && bg.length>0){
        			switch(bg.length){
        			case 1 : 
        				BG = new Color(bg[0],bg[0],bg[0],1.0f);
        				break;
        			case 3 : 
        				BG = new Color(bg[0],bg[1],bg[2],1.0f);
        				break;
        			case 4 : 
//        				BG = new Color(bg[0],bg[0],bg[0],1.0f);
        				break;
        			}
        		}
        	}
        	
        	//Fill Background if set
        	if(BG!=null){            	
            	this.fullCommandString = BG.getRed()+" "+BG.getGreen()+ ' ' +BG.getBlue()+" rg 0 0 "+(((FormObject)form).getBoundingRectangle().width-3)+ ' ' +(((FormObject)form).getBoundingRectangle().height-3)+" re f "+fullCommandString;
        	}
        	
            //we may need to actually check encoding on font here rather than assume win
            fakeObj.setDecodedStream(fullCommandString.getBytes("Cp1252"));
            
        } catch (final IOException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
            
        }
    }
      
    /** decodes and saves all information needed to decode the object on the fly,
     * the test and font can be altered with specific methods.
     * @return boolean true if it all worked.
     */
    public boolean decipherAppObject(final FormObject form) {
        //read the command from file if there is one
        String fontStr="";
        final PdfObject appObj = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.N);
        if(appObj!=null){
            final byte[] bytes = appObj.getDecodedStream();
            
            //
            
            if(bytes!=null){
                int startTf=-1;
                int endTf=-1;
                int startTj;
                int endTj=-1;
                final int end=bytes.length;

                //find index of Tf command
                for (int i = 0; i < end-1; i++) {
                    if((((char)bytes[i])=='T' && ((char)bytes[i+1])=='f') && 
                        (i+2>=end || bytes[i+2]==10 || bytes[i+2]==13 || bytes[i+2]==' ')){
                            endTf = i+2;
                            break;
                        }
                    }
                
                
                if(endTf==-1){
                    startTf = 0;
                    endTf = 0;
                }else {
                    //find beginning of Tf command
                    //					int strs = 0;
                    //					boolean strFound = false;
                    for (int i = endTf-3; i > startTf; i--) {
                        //this is kept until its passed tests.
                        //						if(bytes[i]==' ' || bytes[i]==10 || bytes[i]==13){
                        //							if(strFound){
                        //								strs++;
                        //								if(strs==2){
                        //									startTj = i+1;//to allow for the gap
                        //									//should give the same index as the '/'
                        //									break;
                        //								}
                        //							}
                        //							continue;
                        //						}else
                        if(bytes[i]=='/'){
                            startTf = i;
                            break;
                            //						}else {
                            //							strFound = true;
                        }
                    }
                    
                    //******startTf and endTf should both have a value, and start should be before end******
                }
                
                //find index of Tj command
                for (int i = endTf; i < end-1; i++) {
                    if((((char)bytes[i])=='T' && ((char)bytes[i+1])=='j') &&
                        (i+2>=end || bytes[i+2]==10 || bytes[i+2]==13 || bytes[i+2]==' ')){
                            endTj = i+2;
                            break;
                        }
                    }
                
                
                if(endTj==-1){
                    startTj = endTf;
                    endTj = endTf;
                }else {
                    startTj = endTf;
                    
                    //find the start of the Tj command
                    int brackets = 0;
                    boolean strFound = false;
                    for (int i = endTj-3; i > startTj; i--) {
                        if(bytes[i]==' ' || bytes[i]==10 || bytes[i]==13){
                            if(strFound && brackets==0){
                                //+1 as we dont want the gap we just found in our text string
                                startTj = i+1;
                                break;
                            }
                        }else if(bytes[i]==')'){
                            brackets++;
                        }else if(bytes[i]=='('){
                            brackets--;
                            if(brackets==0 && strFound){
                                startTj = i;
                                break;
                            }
                        }else {
                            strFound = true;
                        }
                    }
                    
                    //******* startTJ and endTj should both have a value and start should be before end ******
                }
                
                //find actual end of Tf including any rg or g command after the Tf.
                for (int i = endTf; i < startTj; i++) {
                    if(bytes[i]==' ' || bytes[i]==10 || bytes[i]==13){
                    }else if(bytes[i]>47 && bytes[i]<58){
                        //number

                    }else {
                        if(bytes[i]=='g' && i+1<startTj && (bytes[i+1]==' ' || bytes[i+1]==10 || bytes[i+1]==13)){
                            endTf = i+1;
                            break;
                        }else if(bytes[i]=='r' && i+2<startTj && bytes[i+1]=='g' && (bytes[i+2]==' '  || bytes[i+2]==10 || bytes[i+2]==13)){
                            endTf = i+2;
                            break;
                        }else {
                            //not what we want leave endTf as is.
                            break;
                        }
                    }
                }
                
                if(endTj!=endTf){
                    //there is a Tj (text)
                    if(endTf==0){
                        //we dont have a font command defined so allow for one
                        preFontStream = new String(bytes,0,startTj);
                        betweenFontAndTextStream = " ";
                    }else {
                        //we have a font command
                        preFontStream = new String(bytes,0,startTf);
                        fontStr = new String(bytes,startTf,endTf-startTf);
                        betweenFontAndTextStream = new String(bytes,endTf,startTj-endTf);
                    }
                    //-3 to ignore the Tj command letters at the end as we add that ourselves.
                    text = new String(bytes,startTj,endTj-3-startTj);
                    afterTextStream = new String(bytes,endTj,bytes.length-endTj);
                }else {
                    //theres no TJ
                    if(endTf==0){
                        //store as command1, and if not valid we deal with below with default command
                        preFontStream = new String(bytes);
                    }else {
                        //we have a font command
                        preFontStream = new String(bytes,0,startTf);
                        fontStr = new String(bytes,startTf,endTf-startTf);
                        //add rest to middleCommand so Text can be added to end
                        betweenFontAndTextStream = new String(bytes,endTf,bytes.length-endTf);
                    }
                }
            }
        }
        
        //get the forms font string
        final String DA = form.getTextStreamValue(PdfDictionary.DA);
        
        //create a fake XObject to make use of the code we already have to generate image
        fakeObj=new XObject(form.getObjectRefAsString()); //value does not matter
        
        if(DA==null || DA.isEmpty()){
            if(!fontStr.isEmpty()){
                //set font we have found
                form.setTextStreamValue(PdfDictionary.DA, StringUtils.toBytes(fontStr));
                FormStream.decodeFontCommandObj(fontStr,form);
            }
            
            //use old methods as appropriate info not present.
            return false;
            
        }else{  //updated by Mark as previous code had bug
            //we replace the TF string (ie /FO_0 8 Tf) with the DA value (ie /Helv 8 Tf) to get the font name
            //this though assumes that Tm is 1 0 0 1 (scaling is done by fotnsize not matrix)
            //on sample file Tf was 1 and Tm was 8 0 0 8 so we ended up with text 8 times too big as we changed
            // /Fo_0 1 Tf to /Helv 8 Tf while not altering Tm
            //I have fixed by keeping Tf value and using /DA font part
            
            if(fontStr.isEmpty()) //use defined DA and remove any whitespace at front (ORIGINAL version)
            {
                //first file since we wrote this code in 2004 where the DA stream has contained more than a font and needs rest of data stripped off
                int ptr=DA.indexOf('/');
                if(ptr<0){
                    ptr=0;
                }
                //end of new code
                fontStr = DA.substring(ptr).trim();
            } else{//get font name from DA but use original fontsize
                final String fontname=DA.substring(0, DA.indexOf(' '));
                final String fontsize=fontStr.substring(fontStr.indexOf(' '), fontStr.length());
                fontStr=fontname+fontsize;
                fontStr = fontStr.trim();
            }
        }
        
        //do not think we need but here for completeness
        //		XObject.setFloatArray(PdfDictionary.Matrix,new float[]{1,0,0,1,0,0});
        
        //forms can have resources (Fonts, XOBjects, etc) which are in the DR value -
        //we store this in AcroRenderer
        if(resources!=null) {
            fakeObj.setDictionary(PdfDictionary.Resources, resources);
        }

        final Rectangle BBox = form.getBoundingRectangle();
		if(BBox != null) {
			fakeObj.setFloatArray(PdfDictionary.BBox,new float[]{BBox.width,0,0,BBox.height,0,0});
		}

        subtype=-1; //could use PdfDictionary.Highlight for transparency
        
        //if no command in file.
        if(preFontStream.isEmpty() || !preFontStream.contains("BT")){
            //build a fake command stream to decode
            preFontStream = "BT 0 0 0 RG 1 TFS ";
            betweenFontAndTextStream = " 1 0 0 1 0 0 Tm ";
            afterTextStream = "";
        }
        
        //find the start and end of the size param
        final int sizeSt = fontStr.indexOf(' ');
        int sizeEn = -1;
        boolean strFound = false;
        for(int i=sizeSt; i<fontStr.length() ;i++){
            final char chr = fontStr.charAt(i);
            if(chr==' ' || chr==10 || chr==13){
                if(strFound){
                    sizeEn = i;
                    break;
                }
            }else {
                strFound = true;
            }
        }
        
        float size = 12;
        if(sizeEn!=-1){
            //store the name, and command
            fontName = fontStr.substring(0,sizeSt);
            fontCommand = fontStr.substring(sizeEn);
            size = Float.parseFloat( fontStr.substring(sizeSt,sizeEn) );
        }
        
        //store the seperate font attributes
        if(fontName.isEmpty()){
            final Font textFont = form.getTextFont();
            fontName = '/' +textFont.getFontName();
            fontCommand = "Tf ";
        }
        
        //check if font size needs autosizing
        if(size==0 || size==-1){
            //call our calculate routine to work out a good size
            size = GUIData.calculateFontSize(BBox.height, BBox.width, false, text);
        }
        fontSize = " "+size+' ';
        
        return true;
    }
    
    public void setAlignment(final int alignment) {
        this.alignment=alignment;
    }
    
    /**generates higher quality images */
    public void setPrinting(final boolean print, final int multiplier){
        
        currentlyPrinting = print;
        printMultiplier = multiplier;
        
        checkAndCreateimage();
    }

    public PdfObject getFakeObject() {
        return fakeObj;
    }
}
