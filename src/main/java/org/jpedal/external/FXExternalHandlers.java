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
 * FXExternalHandlers.java
 * ---------------
 */
package org.jpedal.external;

import org.jpedal.FileAccess;
import org.jpedal.display.GUIModes;
import org.jpedal.objects.acroforms.javafx.JavaFXFormCreator;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.parser.PDFtoImageConvertor;
import org.jpedal.parser.fx.PDFtoImageConvertorFX;
import org.jpedal.render.FXDisplay;

/**
 *
 * @author markee
 */
public class FXExternalHandlers extends ExternalHandlers {

    public FXExternalHandlers(GUIModes guiModes) {
        super(guiModes);
    }
    
    @Override
    public boolean isJavaFX() {
        return true;
    }
    
    @Override
    public void setDVR(FileAccess fileAccess) {
        fileAccess.setDVR(new FXDisplay(1,fileAccess.getObjectStore(),false));
    }
    
    @Override
    public PDFtoImageConvertor getConverter(float multiplyer, DecoderOptions options) {
        return new PDFtoImageConvertorFX(multiplyer, options);
    }
    
    @Override
    public void openPdfFile(final Object userExpressionEngine) {
         
        initObjects(userExpressionEngine,new JavaFXFormCreator());
        
    }
    
}
