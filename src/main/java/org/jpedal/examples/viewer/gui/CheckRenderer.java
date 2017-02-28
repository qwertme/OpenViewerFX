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
 * CheckRenderer.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;

public class CheckRenderer extends JPanel implements TreeCellRenderer {
	  protected final JCheckBox check;
	  protected final TreeLabel label;

	  public CheckRenderer() {
	    setLayout(null);
	    add(check = new JCheckBox());
	    add(label = new TreeLabel());
	    check.setBackground(UIManager.getColor("Tree.textBackground"));
	  }

	  @Override
      public Component getTreeCellRendererComponent(final JTree tree, final Object value,
	               final boolean isSelected, final boolean expanded,
	               final boolean leaf, final int row, final boolean hasFocus) {

	      final String  stringValue = tree.convertValueToText(value, isSelected,
	              expanded, leaf, row, hasFocus);


	      setEnabled(tree.isEnabled());

	      if(value instanceof CheckNode){

	          check.setSelected(((CheckNode)value).isSelected());
	          setEnabled(((CheckNode)value).isEnabled());
	          check.setEnabled(((CheckNode)value).isEnabled());

	          label.setFont(tree.getFont());
	          label.setText(stringValue);
	          label.setSelected(isSelected);
	          label.setFocus(hasFocus);

	          return this;
	      }else {
              return new JLabel(stringValue);
          }

	  }

	  @Override
      public Dimension getPreferredSize() {
	    final Dimension d_check = check.getPreferredSize();
	    final Dimension d_label = label.getPreferredSize();
	    return new Dimension(d_check.width  + d_label.width,
	      (d_check.height < d_label.height ?
	       d_label.height : d_check.height));
	  }

	  @Override
      public void doLayout() {
	    final Dimension d_check = check.getPreferredSize();
	    final Dimension d_label = label.getPreferredSize();
	    int y_check = 0;
	    int y_label = 0;
	    if (d_check.height < d_label.height) {
	      y_check = (d_label.height - d_check.height)/2;
	    } else {
	      y_label = (d_check.height - d_label.height)/2;
	    }
	    check.setLocation(0,y_check);
	    check.setBounds(0,y_check,d_check.width,d_check.height);
	    label.setLocation(d_check.width,y_label);
	    label.setBounds(d_check.width,y_label,d_label.width,d_label.height);
	  }


	  @Override
      public void setBackground(Color color) {
	    if (color instanceof ColorUIResource) {
            color = null;
        }
	    super.setBackground(color);
	  }


	  public static class TreeLabel extends JLabel {
	    boolean isSelected;
	    boolean hasFocus;

	    TreeLabel() {
	    }

	    @Override
        public void setBackground(Color color) {
		if(color instanceof ColorUIResource) {
            color = null;
        }
		super.setBackground(color);
	    }

	    @Override
        public void paint(final Graphics g) {
	      final String str;
	      if ((str = getText()) != null && !str.isEmpty()){
	          if (isSelected) {
	            g.setColor(UIManager.getColor("Tree.selectionBackground"));
	          } else {
	            g.setColor(UIManager.getColor("Tree.textBackground"));
	          }
	          final Dimension d = getPreferredSize();
	          int imageOffset = 0;
	          final Icon currentI = getIcon();
	          if (currentI != null) {
	            imageOffset = currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
	          }
	          g.fillRect(imageOffset, 0, d.width -1 - imageOffset, d.height);
	          if (hasFocus) {
	            g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
	            g.drawRect(imageOffset, 0, d.width -1 - imageOffset, d.height -1);
	         }
	        }
	      super.paint(g);
	    }

	    @Override
        public Dimension getPreferredSize() {
	      Dimension retDimension = super.getPreferredSize();
	      if (retDimension != null) {
	        retDimension = new Dimension(retDimension.width + 3,
					 retDimension.height);
	      }
	      return retDimension;
	    }

	    void setSelected(final boolean isSelected) {
	    	this.isSelected = isSelected;
	    }

	    void setFocus(final boolean hasFocus) {
	      this.hasFocus = hasFocus;
	    }
	  }
	}
