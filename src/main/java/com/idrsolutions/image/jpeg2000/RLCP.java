/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idrsolutions.image.jpeg2000;

/**
 *
 * @author suda
 */
public class RLCP implements Progression {

    private final int layersCount;
    private final int componentsCount;
    private final int maxNL;
    private final Tile tile;
    private int res, layer, c, p;

    public RLCP(Info info, int tileIndex) {
        SIZ siz = info.siz;
        tile = info.tilesMap.get(tileIndex);
        layersCount = tile.cod.nLayers;
        componentsCount = siz.nComp;
        maxNL = tile.cod.nDecompLevel;
    }

    @Override
    public Packet getNextPacket() {

        // do not try to fix this for loop
        while( res <= maxNL) {
            while (layer < layersCount) {
                while ( c < componentsCount ) {
                    TileComponent component = tile.components.get(c);
                    if (res > tile.cod.nDecompLevel) {
                        continue;
                    }

                    TileResolution resolution = component.resolutions.get(res);
                    int numprecincts = resolution.precinctInfo.numPrecincts;
                    if (p < numprecincts) {
                        Packet packet = createPacket(resolution, p, layer);
                        p++;
                        return packet;
                    }
                    p = 0;
                    c++;
                }
                c = 0;
                layer++;
            }
            layer = 0;
            res++;
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
