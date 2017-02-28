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
 * JavaFXOutline.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jpedal.examples.viewer.gui.generic.GUIOutline;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Simon
 */
public class JavaFXOutline extends Tab implements GUIOutline{
	/**used by tree to convert page title into page number*/
	private Map<String, String> pageLookupTableViaTitle=new HashMap<String, String>();
	private boolean hasDuplicateTitles;
	
	private final Map<Integer, String> nodeToRef;

    private final TreeView<Label> list;
    
    public JavaFXOutline(){
        this.nodeToRef = new HashMap();
        list= new TreeView<Label>();
        
        pageLookupTableViaTitle=new HashMap<String, String>();
        hasDuplicateTitles = false;
        
        final ScrollPane content = new ScrollPane();
        setContent(content);
        content.setContent(list);
        
        list.prefWidthProperty().bind(content.widthProperty());
        list.prefHeightProperty().bind(content.heightProperty());
        list.setShowRoot(false);
        
    }
    
    @Override
    public Object getTree() {
        return list;
    }

    @Override
    public DefaultMutableTreeNode getLastSelectedPathComponent() {
        return null;
    }

    @Override
    public String getPage(final String title) {
		if(hasDuplicateTitles) {
            return null;
        }//throw new RuntimeException("Bookmark "+title+" not unique");
		else {
            return pageLookupTableViaTitle.get(title);
        }
    }

    @Override
    public void selectBookmark() {
        // Code commented out in SwingOutline
    }

    @Override
    public void reset(final Node rootNode) {

        final TreeItem<Label> root = new TreeItem<Label>();
        
        nodeToRef.clear();
        pageLookupTableViaTitle.clear();
        hasDuplicateTitles = false;
        
        readChildNodes(rootNode, root, 0);
        
        list.setRoot(root);
    }

    @Override
    public String convertNodeIDToRef(final int index) {
        return nodeToRef.get(index);
    }
    private void readChildNodes(final Node rootData, final TreeItem<Label> rootNode, int nodeIndex){
        if(rootData == null) {
            return;
        }
        
        final NodeList nl = rootData.getChildNodes();
        OutlineNode currentNode;
        
        for(int i = 0; i < nl.getLength(); i++){
            final Element currentElement = (Element)nl.item(i);
            final String title = currentElement.getAttribute("title");
			final String page = currentElement.getAttribute("page");
			final String isClosed = currentElement.getAttribute("isClosed");
            final String ref=currentElement.getAttribute("objectRef");
            
            currentNode = new OutlineNode(title,ref,isClosed);
            // Add a callback for use in JavaFXGUI.setBookmarks()
            currentNode.getValue().setUserData(currentNode);
            currentNode.setPage(page);
            
			if (pageLookupTableViaTitle.containsKey(title)) {
				hasDuplicateTitles = true;
			} else {
				pageLookupTableViaTitle.put(title, page);
			}
			nodeToRef.put(nodeIndex, ref);
            nodeIndex++;
            
            rootNode.getChildren().add(currentNode);
            
            readChildNodes(nl.item(i), currentNode, nodeIndex);
        }
    }
    
    public class OutlineNode extends TreeItem<Label>{
        private final String objectRef;
        private String page;
        
        private OutlineNode() {
            objectRef=null;
        }

        private OutlineNode(final String title, final String objectRef, final String isClosed) {
            this.objectRef = objectRef;
            final Label titleLabel = new Label(title);
            // Small adjustment to take into account vertical scrollbar
            titleLabel.prefWidthProperty().bind(list.prefWidthProperty().subtract(20));
            
            titleLabel.setTextFill(Color.BLACK);
            setValue(titleLabel);
            
            setExpanded(!isClosed.equals("true"));
            setTooltip(new Tooltip(title));
        }
        
        public String getObjectRef(){
            return objectRef;
        }

        public String getPage() {
            return page;
        }

        private void setPage(final String page) {
            this.page = page;
        }
        
    }
    
}
