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
 * QuadTree.java
 * ---------------
 */
package com.idrsolutions.image.jpeg2000;

/**
 * 
 */
public class QuadTree {

    private final TreeNode [] nodeMap;
    private int value; 
    private int currentLevel;

    public QuadTree(int width, int height) {
        int x = Math.max(width, height);
        int n = 1, tt = 0;
        while (x > n) {
            n <<= 1;
            tt++;
        }
        int levLen = tt + 1;
        
        nodeMap = new TreeNode[levLen];

        for (int i = 0; i < levLen; i++) {
            TreeNode lev = new TreeNode(width,height);
            nodeMap[i] = lev;
            width = (width + 1) >> 1;
            height = (height + 1) >> 1;
        }
    }

    public void reset(int i, int j){
        int curLev = 0;
        int val=0;
        TreeNode level;
        int len = nodeMap.length;
        while(curLev<len){
            level = nodeMap[curLev];
            int index = i+j*level.width;
            int kVal = level.items[index];
            if(kVal!=-1){
                val = kVal;
                break;
            }
            level.index = index;
            i>>=1;
            j>>=1;
            curLev++;
        }
        curLev--;
        level = nodeMap[curLev];
        level.items[level.index] =  val;
        this.currentLevel = curLev;
        this.value = 0;
    }
    
    public void incrementValue(){
        TreeNode level = nodeMap[currentLevel];
        level.items[level.index] += 1;
    }
    
    public boolean nextNode() {
        int curLev = currentLevel;
        TreeNode level = nodeMap[curLev];
        int val = level.items[level.index];
        curLev--;
        if (curLev < 0) {
            this.value = val;
            return false;
        }
        currentLevel = curLev;
        level = nodeMap[curLev];
        level.items[level.index] = val;
        return true;
    }
    
    public int getValue(){
        return value;
    }


}

    