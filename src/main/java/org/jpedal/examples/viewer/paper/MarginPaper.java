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
 * MarginPaper.java
 * ---------------
 */

package org.jpedal.examples.viewer.paper;

import java.awt.print.Paper;

/**
 * Created by IntelliJ IDEA.
 * User: Sam
 * Date: 02-Jul-2010
 * Time: 16:41:28
 * To change this template use File | Settings | File Templates.
 */
public class MarginPaper extends Paper {
    double minX, minY, maxRX, maxBY;

    public void setMinImageableArea(final double x, final double y, final double w, final double h) {
        this.minX = x;
        this.minY = y;
        this.maxRX = x+w;
        this.maxBY = y+h;
        super.setImageableArea(minX, minY, maxRX, maxBY);
    }

    @Override
    public void setImageableArea(double x, double y, double w, double h) {

        if (x < minX) {
            x = minX;
        }
        if (y < minY) {
            y = minY;
        }
        if (x+w > maxRX) {
            w = maxRX - x;
        }
        if (y+h > maxBY) {
            h = maxBY - y;
        }

        super.setImageableArea(x, y, w, h);
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxRX() {
        return maxRX;
    }

    public double getMaxBY() {
        return maxBY;
    }
}
