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
 * T1Glyph.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.ObjectOutput;
import org.jpedal.color.PdfTexturePaint;
import org.jpedal.io.PathSerializer;
import org.jpedal.objects.GraphicsState;
import org.jpedal.utils.repositories.Vector_Path;


/**
 * <p>defines the current shape which is created by command stream</p> 
 * <p><b>This class is NOT part of the API</b></p>.
 * Shapes can be drawn onto pdf or used as a clip on other image/shape/text.
 * Shape is built up by storing commands and then turning these commands into a
 * shape. Has to be done this way as Winding rule is not necessarily
 * declared at start.
  */
public class T1Glyph extends BaseT1Glyph
{
	/** marked as transient so it wont be serialized */
    private transient Vector_Path cached_current_path;
    
    private Paint strokePaint;


    /**
	 * store scaling factors
	*/
	public T1Glyph(final Vector_Path cached_current_path){
		this.cached_current_path=cached_current_path;
    }
	
    //////////////////////////////////////////////////////////////////////////
	/**
	 * turn shape commands into a Shape object, storing info for later. Has to
	 * be done this way because we need the winding rule to initialise the shape
	 * in Java, and it could be set awywhere in the command stream
	 */
	@Override
    public void render(final int text_fill_type, final Graphics2D g2, final float scaling, final boolean isFormGlyph)
	{
       
		if(cached_current_path!=null){

//Shape c=g2.getClip();
//			
//			g2.setClip(null);
//			
//			g2.setPaint(Color.RED);
//			g2.fillRect(0, 0, 300, 600);
//			g2.setPaint(Color.BLUE);
//			g2.fillRect(300, 0, 300, 600);
//			g2.drawLine(0,0,600,600);
//			g2.setClip(c);
//			
//			
			final GeneralPath[] paths=cached_current_path.get();
			for (final GeneralPath path : paths) {

                if (path == null) {
                    break;
                }

                if ((text_fill_type == GraphicsState.FILL)) {

                    //replicate shadow effect
                    if (isStroked) {
                        final Paint fillPaint = g2.getPaint();
                        if (!(fillPaint instanceof PdfTexturePaint) && ((Color) strokePaint).getRGB() != ((Color) fillPaint).getRGB() &&
                                strokedPositions.containsKey(String.valueOf((int) g2.getTransform().getTranslateX()) + '-' + (int) g2.getTransform().getTranslateY())) {

                            g2.setPaint(strokePaint);
                            g2.draw(path);
                            g2.setPaint(fillPaint);
                        }

                    }
                    g2.fill(path);
                }

                if (text_fill_type == GraphicsState.STROKE) {
                    
                    g2.draw(path);
                    strokePaint = g2.getPaint();
                    strokedPositions.put(String.valueOf((int) g2.getTransform().getTranslateX()) + '-' + (int) g2.getTransform().getTranslateY(), "x");

                }
            }
        }
	}

	Area glyphShape;
	
	/**return shape of glyph*/
	@Override
    public Area getShape() {
		
		if((cached_current_path!=null && glyphShape==null)){
		
			final GeneralPath[] paths=cached_current_path.get();
			final int cacheCount=paths.length;
			
			for(int i=1;i<cacheCount;i++){
				
				if(paths[i]==null) {
                    break;
                }
				
				paths[0].append(paths[i],false);
				
			}
			
			if((paths!=null)&&(paths[0]!=null)) {
                glyphShape = new Area(paths[0]);
            }
			
		}
		
		return glyphShape;
	}

    /**
	 * method to set the paths after the object has be deserialized.
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param vp - the Vector_Path to set
	 */
	public void setPaths(final Vector_Path vp){
		cached_current_path = vp;
	}

	/**
	 * method to serialize all the paths in this object.  This method is needed because
	 * GeneralPath does not implement Serializable so we need to serialize it ourself.
	 * The correct usage is to first serialize this object, cached_current_path is marked
	 * as transient so it will not be serilized, this method should then be called, so the
	 * paths are serialized directly after the main object in the same ObjectOutput.
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param os - ObjectOutput to write to
	 * @throws IOException
	 */
	public void writePathsToStream(final ObjectOutput os) throws IOException {
		if((cached_current_path!=null)){
			
			final GeneralPath[] paths=cached_current_path.get();
			
			int count=0;
			
			/** find out how many items are in the collection */
			for (int i = 0; i < paths.length; i++) {
				if(paths[i]==null){
					count = i;
					break;
				}
			}
			
			/** write out the number of items are in the collection */
			os.writeObject(count);
			
			/** iterate throught the collection, and write out each path individualy */
			for (int i = 0; i < count; i++) {
				final PathIterator pathIterator = paths[i].getPathIterator(new AffineTransform());
				PathSerializer.serializePath(os, pathIterator);
			}
			
		}
	}

	public void flushArea() {
		glyphShape=null;
	}

    int minX,minY,maxX,maxY;

    @Override
    public int getFontBB(final int type) {

        //calc if not worked out
        if((minX==0 && minY==0 && maxX==0 && maxY==0) && (cached_current_path!=null)){

                final GeneralPath[] paths=cached_current_path.get();
                final int cacheCount=paths.length;

                for(int i=0;i<cacheCount;i++){

                    if(paths[i]==null) {
                        break;
                    }

                    final Rectangle b=paths[i].getBounds();
                    if(i==0){
                        minX=b.x;
                        minY=b.y;
                        maxX=b.width;
                        maxY=b.height;

                    }else{

                        if(minX>b.x) {
                            minX = b.x;
                        }
                        if(minY>b.y) {
                            minY = b.y;
                        }

                        if(maxX<b.width) {
                            maxX = b.width;
                        }
                        if(maxY<b.height) {
                            maxY = b.height;
                        }

                    }
                }
            }

        if(type== PdfGlyph.FontBB_X) {
            return minX;
        } else if(type== PdfGlyph.FontBB_Y) {
            return minY;
        } else if(type== PdfGlyph.FontBB_WIDTH) {
            return maxX;
        } else if(type== PdfGlyph.FontBB_HEIGHT) {
            return minY;//maxY-minY;
        } else {
            return 0;
        }
    }
}
