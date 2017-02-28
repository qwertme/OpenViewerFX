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
 * FXViewerTransitions.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.javafx;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import org.jpedal.PdfDecoderFX;

public class FXViewerTransitions {

    /**
     * This enum is use to determine which type of transition should be used.
     * Not using caps here as the names are used to populate the combobox in
     * BaseOpenViewerFX Use _ to seperate words. For the same reason as above, keep
     * "None" on the end.
     */
    public enum TransitionType {

        None, Fade, Scale, Rotate, CardStack
    }

    /**
     * For directional transitions
     */
    public enum TransitionDirection {

        NONE, LEFT, RIGHT
    }

    public static Transition entryTransition(final PdfDecoderFX pdf, final TransitionType transitionType, final TransitionDirection direction) {
        Transition transition = null;
        switch (transitionType) {
            case Fade:
                transition = fadeIn(pdf);
                break;

            case Scale:
                transition = scaleIn(pdf, direction);
                break;

            case CardStack:
                transition = cardStackIn(pdf, direction);
                break;

            case Rotate:
                transition = rotateIn(pdf, direction);
                break;
        }
        return transition;

    }

    public static Transition exitTransition(final PdfDecoderFX pdf, final TransitionType transitionType, final TransitionDirection direction) {
        Transition transition = null;
        switch (transitionType) {

            case Fade:
                transition = fadeOut(pdf);
                break;

            case Scale:
                transition = scaleOut(pdf, direction);
                break;

            case CardStack:
                transition = cardStackOut(pdf, direction);
                break;

            case Rotate:
                transition = rotateOut(pdf, direction);
                break;

        }
        return transition;
    }

    private static Transition fadeIn(final PdfDecoderFX pdf) {
        final FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), pdf);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        return fadeIn;
    }

    private static Transition fadeOut(final PdfDecoderFX pdf) {
        final FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), pdf);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        return fadeOut;
    }

    private static Transition rotateIn(final PdfDecoderFX pdf, final TransitionDirection direction) {

        if (direction == TransitionDirection.RIGHT) {
            // Going forwards  

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1));
            st.setByX(1);
            st.setByY(1);

            final RotateTransition rotateIn = new RotateTransition(Duration.seconds(1), pdf);
            rotateIn.setFromAngle(0);
            rotateIn.setToAngle(360);

            return new ParallelTransition(pdf, st, rotateIn);

        } else if (direction == TransitionDirection.LEFT) {
            // Going backwards

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1));
            st.setByX(1);
            st.setByY(1);

            final RotateTransition rotateIn = new RotateTransition(Duration.seconds(1), pdf);
            rotateIn.setFromAngle(360);
            rotateIn.setToAngle(0);

            return new ParallelTransition(pdf, st, rotateIn);

        }

        return null;

    }

    private static Transition rotateOut(final PdfDecoderFX pdf, final TransitionDirection direction) {

        if (direction == TransitionDirection.RIGHT) {
            // Going backwards

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1));
            st.setToX(0);
            st.setToY(0);

            final RotateTransition rotateIn = new RotateTransition(Duration.seconds(1), pdf);
            rotateIn.setFromAngle(0);
            rotateIn.setToAngle(360);

            return new ParallelTransition(pdf, st, rotateIn);

        } else if (direction == TransitionDirection.LEFT) {
            // Going forwards

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1));
            st.setToX(0);
            st.setToY(0);

            final RotateTransition rotateIn = new RotateTransition(Duration.seconds(1), pdf);
            rotateIn.setFromAngle(360);
            rotateIn.setToAngle(0);

            return new ParallelTransition(pdf, st, rotateIn);

        }

        return null;

    }

    private static Transition scaleIn(final PdfDecoderFX pdf, final TransitionDirection direction) {

        if (direction == TransitionDirection.RIGHT) {
            // Going forwards            

            pdf.getParent().setScaleX(0);
            pdf.getParent().setScaleY(0);

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1));
            st.setByX(1);
            st.setByY(1);

            return new ParallelTransition(pdf.getParent(), st);

        } else if (direction == TransitionDirection.LEFT) {
            // Going backwards

            pdf.getParent().setScaleX(0);
            pdf.getParent().setScaleY(0);

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1));
            st.setToX(1);
            st.setToY(1);

            return new ParallelTransition(pdf.getParent(), st);

        }

        return null;
    }

    private static Transition scaleOut(final PdfDecoderFX pdf, final TransitionDirection direction) {

        if (direction == TransitionDirection.RIGHT) {
            // Going backwards

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1));
            st.setToX(0);
            st.setToY(0);

            return new ParallelTransition(pdf.getParent(), st);

        } else if (direction == TransitionDirection.LEFT) {
            // Going forwards

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1));
            st.setToX(0);
            st.setToY(0);

            return new ParallelTransition(pdf.getParent(), st);

        }

        return null;
    }

    private static Transition cardStackIn(final PdfDecoderFX pdf, final TransitionDirection direction) {

        if (direction == TransitionDirection.RIGHT) {
            // Going forwards

            pdf.getParent().setScaleX(0.0);
            pdf.getParent().setScaleY(0.0);

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1), pdf.getParent());
            st.setToX(1);
            st.setToY(1);

            final FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), pdf.getParent());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            return new ParallelTransition(pdf.getParent(), st, fadeIn);

        } else if (direction == TransitionDirection.LEFT) {
            // Going backwards
            
            // Get the current translated position of the decoder's parent
            final double xPos=pdf.getParent() != null ? pdf.getParent().getTranslateX() : 0;
            
            final ScaleTransition st = new ScaleTransition(Duration.seconds(1), pdf.getParent());
            st.setToX(1.0);
            st.setToY(1.0);

            final TranslateTransition cardStackOut = new TranslateTransition(Duration.seconds(1), pdf.getParent());
            cardStackOut.setFromX(-pdf.getWidth());
            cardStackOut.setByX(pdf.getWidth());
            cardStackOut.setToX(xPos);
            
            final FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), pdf.getParent());
            fadeOut.setFromValue(0.0);
            fadeOut.setToValue(1.0);

            return new ParallelTransition(pdf.getParent(), fadeOut, st, cardStackOut);

        }
        return null;
    }

    private static Transition cardStackOut(final PdfDecoderFX pdf, final TransitionDirection direction) {

        if (direction == TransitionDirection.RIGHT) {
            // Going backwards

            final ScaleTransition st = new ScaleTransition(Duration.seconds(1), pdf.getParent());
            st.setToX(0.0);
            st.setToY(0.0);

            final FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), pdf.getParent());
            fadeIn.setFromValue(1.0);
            fadeIn.setToValue(0.0);

            return new ParallelTransition(pdf.getParent(), st, fadeIn);

        } else if (direction == TransitionDirection.LEFT) {
            // Going forwards

            final TranslateTransition cardStackOut = new TranslateTransition(Duration.seconds(1), pdf.getParent());
            cardStackOut.setByX(-pdf.getWidth());

            final FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), pdf.getParent());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            return new ParallelTransition(pdf.getParent(), fadeOut, cardStackOut);

        }

        return null;
    }
}
