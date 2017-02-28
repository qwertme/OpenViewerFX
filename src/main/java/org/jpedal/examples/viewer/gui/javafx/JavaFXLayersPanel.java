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
 * JavaFXLayersPanel.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.javafx;

import java.util.Iterator;
import java.util.Map;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.gui.generic.GUILayersPanel;
import org.jpedal.objects.layers.PdfLayerList;

/**
 *
 * @author Simon
 */
public class JavaFXLayersPanel extends Tab implements GUILayersPanel {
    private final BorderPane content;
    private final TreeItem<String> metaDataRoot;
    private final TreeView<String> layersTree;
    
    public JavaFXLayersPanel() {
        final TreeView<String> metaDataTree = new TreeView<String>();
        content = new BorderPane();
        layersTree = new TreeView<String>();
        metaDataRoot=new TreeItem<String>("Info");
        
        // setup meta data view
        metaDataTree.setRoot(metaDataRoot);
        metaDataTree.setShowRoot(true);
        metaDataTree.setTooltip(new Tooltip("Double click to see any metadata"));
        metaDataTree.setPrefHeight(60);
        
        // Use a custom callback to determine the style of the tree item
        layersTree.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override 
            public TreeCell<String> call(final TreeView<String> param) {
                return new LayersCell();
            }
        });
        
        layersTree.setShowRoot(true);

        content.setTop(metaDataTree);
        content.setCenter(layersTree);
        
        setContent(content);
    }
    
    @Override
    public void reinitialise(final PdfLayerList layersObject, final PdfDecoderInt decode_pdf, final Object scrollPane, final int currentPage) {

        metaDataRoot.getChildren().clear();
        layersTree.setRoot(null);
        
        final Map metaData = layersObject.getMetaData();
        
        final Iterator metaDataKeys=metaData.keySet().iterator();
        Object nextKey, value;
        while(metaDataKeys.hasNext()){

            nextKey=metaDataKeys.next();
            value=metaData.get(nextKey);
            metaDataRoot.getChildren().add(new TreeItem<String>(nextKey+"="+value));
        }
        
        final Object[] layerNames=layersObject.getDisplayTree();
        if(layerNames != null){
            final TreeItem<String> layersRoot = new TreeItem<String>("Layers");
            addLayersToTree(layerNames, layersRoot, true, layersObject);
            
            // Attach to the root node an event handler which handles checked events
            // from nodes lower down the tree
            layersRoot.addEventHandler(CheckBoxTreeItem.<String>checkBoxSelectionChangedEvent()
                    ,new EventHandler<CheckBoxTreeItem.TreeModificationEvent<String>>() {
                @Override
                public void handle(final CheckBoxTreeItem.TreeModificationEvent<String> t) {
                    
                    final CheckBoxTreeItem<String> node = t.getTreeItem();
                    
                    StringBuilder rawName = new StringBuilder(node.getValue());
                    
                    TreeItem parent = node.getParent();
                    
                    while(parent.getParent() != null){
                        rawName.append(PdfLayerList.deliminator).append(parent.getValue());
                        parent = parent.getParent();
                    }
                    
                    final String name = rawName.toString();
                    
                    if(layersObject.isLayerName(name) && !layersObject.isLocked(name)){
                        final Runnable updateComponent = new Runnable() {
                            @Override public void run() {
                                layersObject.setVisiblity(name, node.isSelected());
                                try{
                                    decode_pdf.decodePage(currentPage);
                                }catch(final Exception e){
                                    e.printStackTrace();
                                }
                            }
                        };
                        
                        if(Platform.isFxApplicationThread()){
                            updateComponent.run();
                        }else{
                            Platform.runLater(updateComponent);
                        }
                    }
                    
                }
            });
            // Tree listener here
            
            layersRoot.setExpanded(true);
            layersTree.setRoot(layersRoot);
        }
    }
    
    private static void addLayersToTree(final Object[] layerNames, TreeItem<String> topLayer, boolean isEnabled, final PdfLayerList layersObject) {
        String name;
        TreeItem<String> currentNode = topLayer;
        boolean parentEnabled=isEnabled, parentIsSelected=true;
        
        for(final Object layerName: layerNames){
            if(layerName instanceof Object[]){
                final TreeItem<String> oldNode = currentNode;
                addLayersToTree((Object[])layerName, currentNode, isEnabled && parentIsSelected, layersObject);
                currentNode = oldNode;
                isEnabled=parentEnabled;
            }else{
                //store possible recursive settings
                parentEnabled = isEnabled;

                if (layerName == null) {
                    continue;
                }

                if (layerName instanceof String) {
                    name = (String) layerName;
                } else //its a byte[]
                {
                    name = new String((byte[]) layerName);
                }

                /**
                 * remove full path in name
                 */
                String title = name;
                final int ptr = name.indexOf(PdfLayerList.deliminator);
                if (ptr != -1) {
                    title = title.substring(0, ptr);
                }
                
                if (name.endsWith(" R")) { //ignore
                } else if (!layersObject.isLayerName(name)) { //just text

                    currentNode = new TreeItem<String>(title);
                    topLayer.getChildren().add(currentNode);
                    topLayer = currentNode;

                    parentIsSelected = true;

                    //add a node
                } else if (topLayer != null) {

                    currentNode = new CheckBoxTreeItem<String>(title);
                    topLayer.getChildren().add(currentNode);

                    //see if showing and set box to match
                    if (layersObject.isVisible(name)) {
                        ((CheckBoxTreeItem<String>)currentNode).setSelected(true);
                        parentIsSelected = true;
                    } else{
                        ((CheckBoxTreeItem<String>)currentNode).setSelected(false);
                        parentIsSelected = false;
                    }
                    
                    //check locks and allow Parents to disable children
                    if (isEnabled) {
                        isEnabled = !layersObject.isLocked(name);
                    }
                
                    // No set enabled for CheckBoxTreeItem<String>
//                    (Node) currentNode).setEnabled(isEnabled);
                }
            }
        }
    }
    
    @Override
    public void rescanPdfLayers() {
    }

    @Override
    public void resetLayers() {
    }
    
    /**
     * The class used for the cell factory, what happens here is that we check that
     * the related item on the tree is not a CheckBoxTreeItem and if so, remove the checkbox (graphic)
     */
    private static class LayersCell extends CheckBoxTreeCell<String>{
        @Override
        public void updateItem(final String item, final boolean empty){
            super.updateItem(item, empty);
            
            if(empty){
                setGraphic(null);
                setText(null);
            }else if (!(getTreeItem() instanceof CheckBoxTreeItem)){
                setGraphic(null);
            }
        }
    }
    
}
