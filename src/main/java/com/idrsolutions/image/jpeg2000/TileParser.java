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
 * TileParser.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 */
public class TileParser {

    private final int offset;
    private final int dataLength;
    private final byte[] data;
    private final Tile tile;

    private int pos;
    private boolean skipNextBit;
    private int bufferSize;
    private int buffer;
    
    public TileParser(byte[] data, Tile tile) {
        this.data = data;
        this.offset = 0;
        this.dataLength = data.length;
        this.tile = tile;
    }

    private int readBits(int count) {
        while (bufferSize < count) {
            int b = data[offset + pos] & 0xff;
            pos++;
            if (skipNextBit) {
                buffer = (buffer << 7) | b;
                bufferSize += 7;
                skipNextBit = false;
            } else {
                buffer = (buffer << 8) | b;
                bufferSize += 8;
            }
            if (b == 0xFF) {
                skipNextBit = true;
            }
        }
        bufferSize -= count;
        return (buffer >>> bufferSize) & ((1 << count) - 1);

    }

    private boolean skipPacketMarker(int value) {
        if ((data[offset + pos - 1] & 0xff) == 0xff && (data[offset + pos] & 0xff) == value) {
            pos++;
            return true;
        } else if ((data[offset + pos] & 0xff) == 0xFF && (data[offset + pos + 1] & 0xff) == value) {
            pos+=2;
            return true;
        }
        return false;
    }

    private void doPadding() {
        bufferSize = 0;
        if (skipNextBit) {
            pos++;
            skipNextBit = false;
        }
    }

    private int readCodingpasses() {
        if (readBits(1) == 0) {
            return 1;
        }
        if (readBits(1) == 0) {
            return 2;
        }
        int value = readBits(2);
        if (value < 3) {
            return value + 3;
        }
        value = readBits(5);
        if (value < 31) {
            return value + 6;
        }
        value = readBits(7);
        return value + 37;
    }

    public void parseTile() {

        boolean sopMarkerUsed = tile.cod.hasSOP;
        boolean ephMarkerUsed = tile.cod.hasEPH;
        Progression packetsIterator = tile.progress;
        while (pos < dataLength) {
            doPadding();
            if (sopMarkerUsed && skipPacketMarker(0x91)) {
                pos+=4;
            }
            Packet packet = packetsIterator.getNextPacket();
            if (readBits(1) == 0) {
                continue;
            }
            int layerNumber = packet.layerNumber;
            Queue<PacketItem> queue = new LinkedList<PacketItem>();
            
            for (CodeBlock codeBlock : packet.codeBlocks) {
                
                Precinct precinct = codeBlock.precinct;
                int codeblockColumn = codeBlock.x - precinct.cbx0;
                int codeblockRow = codeBlock.y - precinct.cby0;
                boolean codeblockIncluded = false;
                boolean firstTimeInclusion = false;
                if (codeBlock.included != null) {
                    codeblockIncluded = readBits(1) != 0;
                } else {
                    precinct = codeBlock.precinct;
                    IncQuadTree incQuadTree;
                    QuadTree zeroBitTree;
                    if (precinct.incQuadTree != null) {
                        incQuadTree = precinct.incQuadTree;
                    } else {
                        int width = precinct.cbx1 - precinct.cbx0 + 1;
                        int height = precinct.cby1 - precinct.cby0 + 1;
                        incQuadTree = new IncQuadTree(width, height, layerNumber);
                        zeroBitTree = new QuadTree(width, height);
                        precinct.incQuadTree = incQuadTree;
                        precinct.zeroBitTree = zeroBitTree;
                    }
                    
                    if (incQuadTree.reset(codeblockColumn, codeblockRow, layerNumber)) {
                        while (true) {
                            if (readBits(1) != 0) {
                                if (!incQuadTree.nextNode()) {
                                    codeBlock.included = true;
                                    codeblockIncluded = true;
                                    firstTimeInclusion = true;
                                    break;
                                }
                            } else {
                                incQuadTree.incrementValue(layerNumber);
                                break;
                            }
                        }
                    }
                }
                if (!codeblockIncluded) {
                    continue;
                }
                if (firstTimeInclusion) {
                    QuadTree zeroBitTree = precinct.zeroBitTree;
                    zeroBitTree.reset(codeblockColumn, codeblockRow);
                   
                    while (true) {
                        if (readBits(1) != 0) {
                            if (!zeroBitTree.nextNode()) {
                                break;
                            }
                        } else {
                            zeroBitTree.incrementValue();
                        }
                    }
                    codeBlock.zeroBitPlanes = zeroBitTree.getValue();
                }
                int codingpasses = readCodingpasses();
                while (readBits(1) != 0) {
                    codeBlock.Lblock++;
                }

                int codingpassesLog2 = log2(codingpasses);
                int bits = ((codingpasses < (1 << codingpassesLog2)) ? codingpassesLog2 - 1 : codingpassesLog2) + codeBlock.Lblock;
                int codedDataLength = readBits(bits);

                PacketItem pi = new PacketItem();
                pi.codeBlock = codeBlock;
                pi.nCodingPass = (byte) codingpasses;
                pi.dataLength = codedDataLength;
                
                queue.add(pi);
            }

            doPadding();
            if (ephMarkerUsed) {
                skipPacketMarker(0x92);
            }
            while (!queue.isEmpty()) {
                PacketItem packetItem = queue.poll();
                CodeBlock codeBlock = packetItem.codeBlock;
                
                BlockData bd = new BlockData();
                bd.data = data;
                bd.start = offset + pos;
                bd.end = offset + pos + packetItem.dataLength;
                bd.nCodingPass = packetItem.nCodingPass;
                codeBlock.dataList.add(bd);
                pos += packetItem.dataLength;
            }
        }
    }

    private static int log2(int x) {
        int n = 1, i = 0;
        while (x > n) {
            n <<= 1;
            i++;
        }
        return i;
    }
}
