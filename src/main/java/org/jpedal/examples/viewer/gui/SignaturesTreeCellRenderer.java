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
 * SignaturesTreeCellRenderer.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class SignaturesTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon icon;

    @Override
    public Icon getLeafIcon() {
        return icon;
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, Object value, final boolean isSelected,
                                                  final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {

        final DefaultMutableTreeNode node = ((DefaultMutableTreeNode) value);
		value = node.getUserObject();
		final int level = node.getLevel();
		
        final String s = value.toString();
        icon = null;
        Font treeFont = tree.getFont();

        if(level== 2){
        	final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        	final String text=parent.getUserObject().toString();
        	if(text.equals("The following signature fields are not signed")){
        		final URL resource = getClass().getResource("/org/jpedal/examples/viewer/res/unlock.png");
        		icon = new ImageIcon(resource);
        	} else {
        		final URL resource = getClass().getResource("/org/jpedal/examples/viewer/res/lock.gif");
        		icon = new ImageIcon(resource);
        		treeFont = new Font(treeFont.getFamily(), Font.BOLD, treeFont.getSize());
        	}
        }
        
        setFont(treeFont);
        setText(s);
        setIcon(icon);
        if (isSelected) {
            setBackground(new Color(236, 233, 216));
            setForeground(Color.BLACK);
        } else {
            setBackground(tree.getBackground());
            setForeground(tree.getForeground());
        }
        setEnabled(tree.isEnabled());
        
        setOpaque(true);

        return this;
    }
}
