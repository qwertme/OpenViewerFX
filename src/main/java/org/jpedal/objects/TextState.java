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
 * TextState.java
 * ---------------
 */
package org.jpedal.objects;

import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

/**
 * holds the current text state
 */
public class TextState implements Cloneable
{


    float kerningAdded;

    /**orientation of text using contstants from PdfData*/
    public int writingMode;

    /**last Tm value*/
    private final float[][] TmAtStart = new float[3][3];

    /**matrix operations for calculating start of text*/
    public float[][] Tm = new float[3][3];

    private String font_ID="";

    /**leading setin text*/
    private float TL;

    /**gap between chars set by Tc command*/
    private float character_spacing;

    /**current Tfs value*/
    private float Tfs = 1;

    /** text rise set in stream*/
    private float text_rise;

    /**text height - see also Tfs*/
    private float th = 1;

    /**gap inserted with spaces - set by Tw*/
    private float word_spacing;

    private boolean hasFontChanged;

    /**
     * set Trm values
     */
    public TextState()
    {
        Tm[0][0] = 1;
        Tm[0][1] = 0;
        Tm[0][2] = 0;
        Tm[1][0] = 0;
        Tm[1][1] = 1;
        Tm[1][2] = 0;
        Tm[2][0] = 0;
        Tm[2][1] = 0;
        Tm[2][2] = 1;

        TmAtStart[0][0] = 1;
        TmAtStart[0][1] = 0;
        TmAtStart[0][2] = 0;
        TmAtStart[1][0] = 0;
        TmAtStart[1][1] = 1;
        TmAtStart[1][2] = 0;
        TmAtStart[2][0] = 0;
        TmAtStart[2][1] = 0;
        TmAtStart[2][2] = 1;

    }

    /**
     * get Tm at start of line
     */
    public float[][] getTMAtLineStart() {
        return TmAtStart;
    }

    /**
     * set Tm at start of line
     */
    public void setTMAtLineStart() {

        //keep position in case we need
        TmAtStart[0][0] = Tm[0][0];
        TmAtStart[0][1] = Tm[0][1];
        TmAtStart[0][2] = Tm[0][2];
        TmAtStart[1][0] = Tm[1][0];
        TmAtStart[1][1] = Tm[1][1];
        TmAtStart[1][2] = Tm[1][2];
        TmAtStart[2][0] = Tm[2][0];
        TmAtStart[2][1] = Tm[2][1];
        TmAtStart[2][2] = Tm[2][2];

    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * set Horizontal Scaling
     */
    public final void setHorizontalScaling( final float th )
    {
        this.th = th;
    }
    ///////////////////////////////////////////////////////////////////////
    /**
     * get font id
     */
    public final String getFontID()
    {
        return font_ID;
    }
    ///////////////////////////////////////////////////////////////////////////
    /**
     * get Text rise
     */
    public final float getTextRise()
    {
        return text_rise;
    }
    ////////////////////////////////////////////////////////////////////////
    /**
     * get character spacing
     */
    public final float getCharacterSpacing()
    {
        return character_spacing;
    }
    /////////////////////////////////////////////////////////////////////////
    /**
     * get word spacing
     */
    public final float getWordSpacing()
    {
        return word_spacing;
    }
    ///////////////////////////////////////////////////////////////////////////
    /**
     * set font tfs
     */
    public final void setLeading( final float TL )
    {
        this.TL = TL;
    }
    /////////////////////////////////////////////////////////////////////////
    /**
     * get font tfs
     */
    public final float getTfs()
    {
        return Tfs;
    }

    /////////////////////////////////////////////////////////////////////////
    /**
     * get Horizontal Scaling
     */
    public final float getHorizontalScaling()
    {
        return th;
    }

    /**
     * set Text rise
     */
    public final void setTextRise( final float text_rise )
    {
        this.text_rise = text_rise;
    }

    /**
     * get font tfs
     */
    public final float getLeading()
    {
        return TL;
    }


    /////////////////////////////////////////////////////////////////////////
    /**
     * clone object
     */
	/*final public Object clone()
	{
		Object o = null;
		try
		{
			o = super.clone();
		}
		catch( Exception e )
		{
			LogWriter.writeLog( "Unable to clone " + e );
		}
		return o;
	}*/
    /////////////////////////////////////////////////////////////////////////
    @Override
    public final Object clone(){

        try {
            super.clone();
        } catch (CloneNotSupportedException ex) {
            LogWriter.writeLog("Unable to clone "+ex);
        }
        
        final TextState ts = new TextState();

        ts.writingMode = writingMode;

        if(TmAtStart != null){
            for(int i=0;i<3;i++){
                System.arraycopy(TmAtStart[i], 0, ts.TmAtStart[i], 0, 3);
            }
        }

        if(Tm != null){
            for(int i=0;i<3;i++){
                System.arraycopy(Tm[i], 0, ts.Tm[i], 0, 3);
            }
        }

        if(font_ID!=null) {
            ts.font_ID = new String(StringUtils.toBytes(font_ID));
        }

        ts.TL = TL;

        ts.character_spacing = character_spacing;

        ts.Tfs = Tfs;

        ts.text_rise = text_rise;

        ts.th = th;

        ts.word_spacing = word_spacing;

        ts.hasFontChanged = hasFontChanged;

        return ts;
    }
    /**
     * set word spacing
     */
    public final void setWordSpacing( final float word_spacing )
    {
        this.word_spacing = word_spacing;
    }

    /**
     * set character spacing
     */
    public final void setCharacterSpacing( final float character_spacing )
    {
        this.character_spacing = character_spacing;
    }
    //////////////////////////////////////////////////////////////////////////
    /**
     * set font tfs to default
     */
    public final void resetTm()
    {
        Tm[0][0] = 1;
        Tm[0][1] = 0;
        Tm[0][2] = 0;
        Tm[1][0] = 0;
        Tm[1][1] = 1;
        Tm[1][2] = 0;
        Tm[2][0] = 0;
        Tm[2][1] = 0;
        Tm[2][2] = 1;

        //keep position in case we need
        setTMAtLineStart();

    }

    public boolean hasFontChanged() {
        return hasFontChanged;
    }

    public void setFontChanged(final boolean status) {
        hasFontChanged=status;
    }

    public void TF(final float Tfs, final String fontID) {

        //set global variables to new values
        this.Tfs = Tfs;

        font_ID = fontID;

        //flag font has changed
        hasFontChanged = true;

    }

    public void setLastKerningAdded(final float spacingAdded) {
        this.kerningAdded =spacingAdded;
    }

    public float getLastKerningAdded() {
        return kerningAdded;
    }
}
