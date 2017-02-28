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
 * PDFtoImageConvertorFX.java
 * ---------------
 */
package org.jpedal.parser.fx;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotResult;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.util.Callback;
import org.jpedal.exception.PdfException;
import org.jpedal.render.*;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.*;

public class PDFtoImageConvertorFX extends PDFtoImageConvertor{

    int pageIndex;
    
     public PDFtoImageConvertorFX(final float multiplyer, final DecoderOptions options) {
        super(multiplyer, options);
        isFX = true;
        
    }
   
    @Override
    public DynamicVectorRenderer getDisplay(final int pageIndex, final ObjectStore localStore) {
        this.pageIndex=pageIndex;
        return imageDisplay = new FXDisplayForRasterizing(pageIndex,true, 5000, localStore);
       
    }
    
    @Override
    public BufferedImage pageToImage(final boolean imageIsTransparent, final PdfStreamDecoder currentImageDecoder,
            final float scaling, final PdfObject pdfObject,final AcroRenderer formRenderer) throws PdfException {

        /**
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics graphics = image.getGraphics();

        Graphics2D g2 = (Graphics2D) graphics;

        if (!imageIsTransparent) {
            g2.setColor(Color.white);
            g2.fillRect(0, 0, w, h);
        }

        // adjustment for upside down images
        if(rotation==180){
            g2.translate(crx*2*multiplyer, -(cry*2*multiplyer));
        }

        // pass in values as needed for patterns
        imageDisplay.setScalingValues(crx*multiplyer, (crh*multiplyer) + cry, multiplyer*scaling);

        g2.setRenderingHints(ColorSpaces.hints);
        g2.transform(imageScaling);

        if (rotated){

            if(rotation==90){//90

                if(multiplyer<1){
                    cry = (int)(imageScaling.getTranslateX() + cry);
                    crx = (int)(imageScaling.getTranslateY() + crx);

                }else{
                    cry = (int)((imageScaling.getTranslateX()/multiplyer) + cry);
                    crx = (int)((imageScaling.getTranslateY()/multiplyer) + crx);
                }
                g2.translate(-crx, -cry);

            }else{ //270
                if(cry<0)
                    g2.translate(-crx, mediaH-crh+cry);
                else
                    g2.translate(-crx,mediaH-crh-cry);
            }
        }/**/
        
        final SimpleObjectProperty<BufferedImage> imageProperty = new SimpleObjectProperty();
        // Locks the Thread until the image is generated
        final CountDownLatch latch = new CountDownLatch(1);
        
        if(Platform.isFxApplicationThread()){
            snapshot(currentImageDecoder, scaling, pdfObject, null, imageProperty,formRenderer);
        }else{
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    snapshot(currentImageDecoder, scaling, pdfObject, latch, imageProperty,formRenderer);
                }
            });
        }     
        
        try {
            // This will hang if we're on the FX Thread
            if(!Platform.isFxApplicationThread()) {
                latch.await();
            }
        } catch (final InterruptedException ex) {
            ex.printStackTrace();
        }
        return imageProperty.get();
    }
    
    private void snapshot(final PdfStreamDecoder currentImageDecoder, final float scaling, final PdfObject pdfObject, 
            final CountDownLatch latch, final SimpleObjectProperty<BufferedImage> imageProperty,final AcroRenderer formRenderer){
        
        Pane g=new Pane();
        
        try {
            formRenderer.getCompData().setRootDisplayComponent(g);
            
          // currentImageDecoder.setObjectValue(ValueTypes.DirectRendering, null);//(Graphics2D) graphics);
            currentImageDecoder.decodePageContent(pdfObject);
            
            formRenderer.createDisplayComponentsForPage(pageIndex, currentImageDecoder);
            
            formRenderer.displayComponentsOnscreen(pageIndex, pageIndex);
            formRenderer.getCompData().resetScaledLocation(scaling, 0, 0);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        
        Group group;
        group=((FXDisplay)imageDisplay).getFXPane();
       
        group.getChildren().addAll(g.getChildren());
        
        // Transform from PDF coordinates and apply scaling
        group.getTransforms().add(Transform.affine(1 * scaling,0,0,-1 * scaling,crx,h+cry));
        final Scene scene=new Scene(group,w,h);

        // Fixes blending
        scene.setFill(Color.rgb(255, 255, 255, 1.0));
        
        if(latch != null){
            // Async call to snapshot
            scene.snapshot(new Callback<SnapshotResult, Void>() {
                @Override public Void call(final SnapshotResult p) {
                    imageProperty.set(SwingFXUtils.fromFXImage(p.getImage(), null));
                    latch.countDown();
                    return null;
                }
            },null);
        }else{ // If we're on the FX Thread, get the snapshot straight away.
            imageProperty.set(SwingFXUtils.fromFXImage(scene.snapshot(null),null));
        }
    }
}
