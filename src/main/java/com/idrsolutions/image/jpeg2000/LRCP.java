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
 * LRCP.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 * Layer Resolution Component Packet Progression
 */
public class LRCP implements Progression {

    private final int layersCount;
    private final int componentsCount;
    private final int maxNL;
    private final Tile tile;
    private int comp, res, layer, prec;

    public LRCP(Info info, int tileIndex) {
        SIZ siz = info.siz;
        tile = info.tilesMap.get(tileIndex);
        layersCount = tile.cod.nLayers;
        componentsCount = siz.nComp;
        maxNL = tile.cod.nDecompLevel;
    }

    @Override
    public Packet getNextPacket() {

        // do not try to fix this for loop
        while (layer < layersCount) {
            while (res <= maxNL) {
                while (comp < componentsCount) {
                    TileComponent component = tile.components.get(comp);
                    if (res > maxNL) {
                        continue;
                    }
                    TileResolution resolution = component.resolutions.get(res);
                    int numprecincts = resolution.precinctInfo.numPrecincts;
                    if (prec < numprecincts) {
                        Packet pack = createPacket(resolution, prec, layer);
                        prec++;
                        return pack;
                    }
                    prec = 0;
                    comp++;
                }
                comp = 0;
                res++;
            }
            res = 0;
            layer++;
        }
        return null;
    }

    @Override
    public Packet createPacket(TileResolution resolution, int precintNumber, int layerNumber) {
        Packet packet = new Packet();
        packet.layerNumber = layerNumber;
        for (TileBand subband : resolution.tileBands) {
            for (CodeBlock codeBlock : subband.codeBlocks) {
                if (codeBlock.precinctNumber == precintNumber) {
                    packet.codeBlocks.add(codeBlock);
                }
            }
        }
        return packet;
    }

}
