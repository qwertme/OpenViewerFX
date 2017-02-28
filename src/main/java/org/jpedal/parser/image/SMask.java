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
 * SMask.java
 * ---------------
 */
package org.jpedal.parser.image;

import org.jpedal.io.ColorSpaceConvertor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 *
 */
public class SMask {

    static final int[] transparentPixel={0,0,0,0};
        
     
    /**
     * apply soft mask
     **/
    public static BufferedImage applySmask(BufferedImage image, BufferedImage smask, final boolean isRGB) {
        
        if(smask==null){
            return image;
        }
        
        //we assume in image pixel comparison we are using type 2
        if(smask.getType()==12) {
            smask = ColorSpaceConvertor.convertToARGB(smask);
        }
         
        final boolean debug=false;
        
        if(debug){
            System.out.println("image="+image);
            System.out.println("smask="+smask);
        }
        
        boolean maybeFourBit=true;
        
        final Raster mask=smask.getRaster();
        WritableRaster imgRas=null;
        
        boolean isConverted=false;
        
        /**
         * allow for scaled mask
         */
        int imageW=image.getWidth();
        int imageH=image.getHeight();
        int smaskW=smask.getWidth();
        int smaskH=smask.getHeight();
        final boolean isRotated=((imageW>imageH && smaskW<smaskH) || (imageW<imageH && smaskW>smaskH));
         
        if(isRotated){
            smaskH=smask.getWidth();
            smaskW=smask.getHeight();
        }
        
        float ratioW=0,ratioH=0;
        
        if(!isRotated &&(imageW!=smaskW || imageH!=smaskH)){
            ratioW=(float)imageW/(float)smaskW;
            ratioH=(float)imageH/(float)smaskH;
            
            //resize if half size to improve image quality on RGB
            if(isRGB && ratioW==0.5 && ratioH==0.5){
                
                final BufferedImage resizedImage = new BufferedImage(smaskW, smaskH, image.getType());
                final Graphics2D g = resizedImage.createGraphics();
                
                g.dispose();
                g.setComposite(AlphaComposite.Src);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
                //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                
                g.drawImage(image, 0, 0, smaskW, smaskH, null);
                
                image=resizedImage;
                
                imageW=smaskW;
                imageH=smaskH;
                ratioW=1;
                ratioH=1;
            }           
        }
        
        final int colorComponents=smask.getColorModel().getNumComponents();
        final int[] values=new int[colorComponents];
        final int[] pix=new int[4];
        
        if(debug) {
            System.out.println("colorComponents=" + colorComponents + ' ' + isRGB);
        }
        
        //apply smask
        int line;
        final int maskH=mask.getHeight();

        for(int y=0;y<imageH;y++){
            for(int x=0;x<imageW;x++){
                
                line=y;
                
                if(isRotated){
                    //get raw color data
                    if(ratioW==0) {
                        mask.getPixels(line, maskH - 1 - x, 1, 1, values);
                    } else {
                        mask.getPixels((int) (line / ratioW), (int) (x / ratioH), 1, 1, values);
                    }
                    
                }else{
                    //get raw color data
                    if(ratioW==0) {
                        mask.getPixels(x, line, 1, 1, values);
                    } else {
                        mask.getPixels((int) (x / ratioW), (int) (line / ratioH), 1, 1, values);
                    }
                }
                
                if(!isConverted){ // do it first time needed

                   if(image.getType()!=2) {
                       image = ColorSpaceConvertor.convertToARGB(image);
                   }
                   imgRas=image.getRaster();
                   isConverted=true;
               }
                 
               imgRas.getPixels(x,y,1,1,pix);
               
               //System.out.println(pix[0]+" "+pix[1]+" "+pix[2]+" "+pix[3]+" <<>> "+values[0]+" "+values.length);
              
             //  if(values[2]==0 && pix[2]==0 && values[3]==0)
              
               //if(values.length==1){
               //    if(pix[0]==0 && pix[1]==0 && pix[2]==0)
               //     imgRas.setPixels(x, y, 1, 1, new int[]{(pix[0]), (pix[1]), (pix[2]),values[0]});
               // }else
               if(values.length==3){
                   //If mask values are 0 - 1 then alter 1 to 255 (ms valid in raw PDF but not at this point)
                   //if(values[0]==1){
                   //    values[0] = 255;
                  // }
                        imgRas.setPixels(x, y, 1, 1, new int[]{(pix[0]), (pix[1]), (pix[2]), values[0]});
               }else if (colorComponents == 1) {
                   
                   //some are actually 4 bit like
                   ///PDFdata/test_data/baseline_screens/adobe/PP_download.pdf
                   //so try to catch here
                   if(maybeFourBit && values[0]<16 && pix[0]<16 && pix[1]<16 && pix[2]<16) {
                       imgRas.setPixels(x, y, 1, 1, new int[]{pix[0] * 16, pix[1] * 16, pix[2] * 16, values[0]});
                   } else{
                       imgRas.setPixels(x, y, 1, 1, new int[]{ pix[0], pix[1], pix[2], values[0]});
                       maybeFourBit=false;
                   }
               } else if (values[0] == 0 && values[1] == 0 && values[2] == 0 && values[3] == 0) {
                   if(pix[3]!=255){
                       imgRas.setPixels(x, y, 1, 1, transparentPixel);
                   }
                   
                   //if(pix[0]==255 && pix[1]==255 && pix[2]==255 && pix[3]==255){
                   //    imgRas.setPixels(x, y, 1, 1, transparentPixel);
               //    System.out.println(values[0]+" "+values[1]+" "+values[2]+" "+values[3]+" <<>> "+pix[0]+" "+pix[1]+" "+pix[2]+" "+pix[3]);
                   //}
               } else if (pix[0] == 0 && pix[1] == 0 && pix[2] == 0 && pix[3] == 0) {
                   
                   //if(pix[0]==0 && pix[1]==0 && pix[2]==0 && pix[3]==0){
                   //    imgRas.setPixels(x, y, 1, 1, transparentPixel);
                   //}else 
                   if (values[0] == 0 && values[1] == 0 && values[2] == 0 && values[3] == 0){
                       imgRas.setPixels(x, y, 1, 1, transparentPixel);
                      ////@mark 
                  }else if (values[3] == 255) {
                     // System.out.println(values[0]+" "+"<>"+values[0]+" "+values[1]+" "+values[2]+" "+values[3]);
                     imgRas.setPixels(x, y, 1, 1,transparentPixel);
                     //  imgRas.setPixels(x, y, 1, 1, transparentPixel);
                   //}else if (values[3] == 255 && values[0] != 0) {
                     //  imgRas.setPixels(x, y, 1, 1, new int[]{values[0], values[1], values[2], values[0]});
                   }
                   //}else if(values.length<4){// && values[0]==0){
                   //System.out.println(values[0]+" "+"<>"+pix[0]+" "+pix[1]+" "+pix[2]+" "+pix[3]);
                   
                  
                   //  imgRas.setPixels(x,y,1,1,new int[]{(pix[0]),(pix[1]),(pix[2]),values[0]});
               } else if (values[3] == 0 && pix[0]!=0 && pix[1]!=0 && pix[2]!=0) {
                   imgRas.setPixels(x, y, 1, 1, new int[]{(pix[0]), (pix[1]), (pix[2]), 255 - values[0]});
               } else {
                   //System.out.println(values[0]+" "+values[1]+" "+values[2]+" "+values[3]+"<>"+pix[0]+" "+pix[1]+" "+pix[2]+" "+pix[3]);
                   if (values[0] == 0 && values[1] == 0 && values[2] == 0 && values[3] == 255) {
                       imgRas.setPixels(x, y, 1, 1, transparentPixel);
                   }else if (pix[0] == 0 && pix[1] == 0 && pix[2] == 0 && pix[3]<20){ //lose very slight shade in mask
                       imgRas.setPixels(x, y, 1, 1, transparentPixel);
                       
                   } else {
                       
                       if(pix[0]==0 && pix[1]==0 && pix[2]==0 && pix[3]<64){
                           if(values[0]>pix[3]){
                               
                            imgRas.setPixels(x, y, 1, 1, new int[]{0,0,0, values[0]-pix[3]});
                            
                           }else{
                                
                               imgRas.setPixels(x,y,1,1,transparentPixel);
                           }
                       }else if(pix[0]==255 && pix[1]==255 && pix[2]==255 && pix[3]==255){
                           imgRas.setPixels(x, y, 1, 1, new int[]{(pix[0]), (pix[1]), (pix[2]), values[0]});
                            
                          // System.out.println(pix[3]);
                          // imgRas.setPixels(x,y,1,1,new int[]{0,255,0,128});
                          //  System.out.println(values[0]+" "+values[1]+" "+values[2]+" "+values[3]+"<>"+pix[0]+" "+pix[1]+" "+pix[2]+" "+pix[3]);
                     // imgRas.setPixels(x,y,1,1,transparentPixel);
                          // imgRas.setPixels(x,y,1,1,new int[]{255,0,0,128});
                       }else if(values[3]==255 && pix[0]!=0 && pix[1]!=0 && pix[2]!=0 && values[0]>pix[3]){
                      //   System.out.println(values[0]+" "+values[1]+" "+values[2]+" "+values[3]+"<>"+pix[0]+" "+pix[1]+" "+pix[2]+" "+pix[3]);  
                        // imgRas.setPixels(x, y, 1, 1, new int[]{(pix[0]), (pix[1]), (pix[2]), values[0]});
                             
                          // imgRas.setPixels(x,y,1,1,new int[]{0,255,0,128});
                       }else{
                           imgRas.setPixels(x, y, 1, 1, new int[]{(pix[0]), (pix[1]), (pix[2]), values[0]});
                             
                        //   imgRas.setPixels(x,y,1,1,new int[]{0,255,0,128});
                        //   System.out.println(values[0]+" "+values[1]+" "+values[2]+" "+values[3]+"<>"+pix[0]+" "+pix[1]+" "+pix[2]+" "+pix[3]);
                       }
                       //System.out.println(values[0]+" "+values[1]+" "+values[2]+" "+values[3]+"<>"+pix[0]+" "+pix[1]+" "+pix[2]+" "+pix[3]);
                       // imgRas.setPixels(x,y,1,1,new int[]{255,0,0,128});
                   }
               }
            }
        }
        
        return image;
    }
    
}


