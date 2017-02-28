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
 * IndexedImage.java
 * ---------------
 */

package org.jpedal.parser.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.io.ColorSpaceConvertor;
import static org.jpedal.parser.image.ImageDecoder.allBytesZero;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
public class IndexedImage {
    
    
    public static BufferedImage make(int w, int h, final GenericColorSpace decodeColorData, byte[] index, int d, byte[] data) {
        
        
        BufferedImage image=null;
        
        if(LogWriter.isOutput()) {
                LogWriter.writeLog("Indexed " + w + ' ' + h);
            }
            
            /**convert index to rgb if CMYK or ICC*/
            if(!decodeColorData.isIndexConverted()){
                index=decodeColorData.convertIndexToRGB(index);
            }
            
            //workout size and check in range
            //int size =decodeColorData.getIndexSize()+1;
            
            //pick out daft setting of totally empty image and ignore
            if(d==8 && decodeColorData.getIndexSize()==0 && 
                    decodeColorData.getID()==ColorSpaces.DeviceRGB
                   && decodeColorData.getRawColorSpacePDFType()!=ColorSpaces.ICC){
                
                boolean hasPixels=false;
                
                final int indexCount=index.length;
                for(int ii=0;ii<indexCount;ii++){
                    if(index[ii]!=0){
                        hasPixels=true;
                        ii=indexCount;
                    }
                }
                
                if(!hasPixels){
                    final int pixelCount=data.length;
                    
                    for(int ii=0;ii<pixelCount;ii++){
                        if(data[ii]!=0){
                            hasPixels=true;
                            ii=pixelCount;
                        }
                    }
                }
                if(!hasPixels){
                    return new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
                }
            }
            
            try{
                //remove image in Itext which is white on white
                if(d==1 && index.length==6 && index[0]== index[3] && index[1]== index[4] && index[2]== index[5]){
                    image=null;
                    //optimise  silly Itext case of 1=1 white indexed image  11dec/gattest.pdf
                }else if(d==8 && w==1 && h== 1 && index[0]==-1 && index[1]==-1 && index[2]==-1 && allBytesZero(data)){
                    image = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
                    image.createGraphics().setPaint(Color.CYAN);
                    final Raster raster = ColorSpaceConvertor.createInterleavedRaster((new byte[]{(byte)255,(byte)255,(byte)255}), 1, 1);
                    image.setData(raster);
                    
                }else{
                    image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, index,false,false);
                }
            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
        return image;
    }
    
}
