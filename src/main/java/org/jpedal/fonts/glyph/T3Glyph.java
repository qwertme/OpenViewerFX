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
 * T3Glyph.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javafx.scene.shape.Path;

import org.jpedal.color.PdfPaint;
import org.jpedal.render.T3Display;
import org.jpedal.render.T3Renderer;
import org.jpedal.utils.LogWriter;


/**
 * <p>defines the current shape which is created by command stream</p> 
 * <p><b>This class is NOT part of the API</b></p>.
 * Shapes can be drawn onto pdf or used as a clip on other image/shape/text.
 * Shape is built up by storing commands and then turning these commands into a
 * shape. Has to be done this way as Winding rule is not necessarily
 * declared at start.
  */
public class T3Glyph implements PdfGlyph
{
	
	private boolean lockColours;
	
	T3Renderer glyphDisplay;
	
	/**actual offset of glyph*/
	private int maxWidth,maxHeight;

    float glyphScale=1f;

    private int glyphNumber = -1;

    /**
	 * create the glyph as a wrapper around the DynamicVectorRenderer 
	*/
	public T3Glyph(final T3Renderer glyphDisplay, final int x, final int y, final boolean lockColours){
		this.glyphDisplay=glyphDisplay;
		this.maxWidth=x;
		this.maxHeight=y;
		this.lockColours=lockColours;

    }

    //used by Type3 if need to adjust scaling due to size
    public void setScaling(final float glyphScaling){
        this.glyphScale=glyphScaling;
    }
	
	
    /**
	 * draw the t3 glyph
	 */
	@Override
    public Area getShape()
	{
		return null;
	}
	
	/**
	 * draw the t3 glyph
	 */
	@Override
    public void render(final int type, final Graphics2D g2, final float scaling, final boolean isFormGlyph){

        glyphDisplay.setScalingValues(0,0,scaling);
        
        //preseve old scaling to set back, in case others are using.
        final float OLDglyphScale = glyphScale;
        
        if(isFormGlyph){
        	//scale the glyph to include the defined scaling
        	glyphScale = scaling * glyphScale;
        }
        
        //factor in if not correct size
        AffineTransform aff=null;
        if(glyphScale!=1f){
            aff=g2.getTransform();
            g2.scale(glyphScale,glyphScale);
        }

        glyphDisplay.setG2(g2);
        glyphDisplay.paint(null,null,null);

        //undo
        if(aff!=null) {
            g2.setTransform(aff);
        }
        
        //set back the old glyphscale value, in case others are using.
        glyphScale = OLDglyphScale;
    }
	
	
	/**
	 * Returns the max width
	 */
	@Override
    public float getmaxWidth() {
        if(maxWidth==0 && glyphScale<=1f) {
            return 1f / glyphScale;
        } else {
            return maxWidth;
        }
	}


    /**
	 * set colors for display
	 */
	@Override
    public void setT3Colors(final PdfPaint strokeColor, final PdfPaint nonstrokeColor, final boolean lockColours) {

		glyphDisplay.lockColors(strokeColor,nonstrokeColor, lockColours);

    }

	/**
	 * flag if use internal colours or text colour
	 */
	@Override
    public boolean ignoreColors() {
		return lockColours;
	}

    @Override
    public int getGlyphNumber() {
        return glyphNumber;
    }

    @Override
    public void setGlyphNumber(final int no) {
        glyphNumber = no;
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
		
		//convert to bytes
		final byte[] dvr= glyphDisplay.serializeToByteArray(null);
		
		//int size=dvr.length;
		
		os.writeObject(dvr);
		os.writeInt(maxWidth);
		os.writeInt(maxHeight);
		os.writeBoolean(lockColours);
		
	}
	
	/**recreate T3 glyph from serialized data*/
	public T3Glyph(final ObjectInput os) {
		
		try {
			final byte[] dvr=(byte[]) os.readObject();
			
			glyphDisplay=new T3Display(dvr,null);
			
			maxWidth=os.readInt();
			maxHeight=os.readInt();
			lockColours=os.readBoolean();
			
		} catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
		}
	}


    @Override
    public void setWidth(final float width) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public int getFontBB(final int type) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setStrokedOnly(final boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //use by TT to handle broken TT fonts
    @Override
    public boolean containsBrokenData() {
        return false;
    }

    @Override
    public Path getPath() {
        throw new UnsupportedOperationException("getPath Not supported yet.");
    }
}
