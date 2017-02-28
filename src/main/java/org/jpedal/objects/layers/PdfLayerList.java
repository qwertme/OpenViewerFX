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
 * PdfLayerList.java
 * ---------------
 */
package org.jpedal.objects.layers;

import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.OCObject;
import org.jpedal.objects.raw.PdfKeyPairsIterator;
import org.jpedal.io.PdfObjectReader;

import java.util.*;

public class PdfLayerList {

    private static boolean debug;

    /**page we have outlines for*/
    private int OCpageNumber=-1;

    private String padding ="";

    //used in tree as unique separator
    public static final char deliminator=(char)65535;

    private final Map layerNames=new LinkedHashMap();

    private final Map streamToName=new HashMap();

    private final Map layersEnabled=new HashMap();

    private Map jsCommands;

    private final Map metaData=new HashMap();
    
    private final Map layersTested=new HashMap();

    private final Map layerLocks=new HashMap();

    private boolean changesMade;

    private Map propertyMap, refToPropertyID,refTolayerName,RBconstraints;

    private final Map minScale=new HashMap();
    private final Map maxScale=new HashMap();

    //private float scaling=1f;
    
    private int layerCount;
    private Object[] order;

    //

    private PdfObjectReader currentPdfFile;

    private Layer[] layers;


    /**
     * add layers and settings to list
     * @param OCProperties is of type PdfObject
     * @param PropertiesObj is of type PdfObject
     * @param currentPdfFile is of type PdfObjectReader
     * @param pageNumber is of type int
     */
    public void init(final PdfObject OCProperties, final PdfObject PropertiesObj, final PdfObjectReader currentPdfFile, final int pageNumber) {

        OCpageNumber=pageNumber;

        propertyMap=new HashMap();
        refToPropertyID =new HashMap();
        refTolayerName=new HashMap();
        RBconstraints=new HashMap();
        
        this.currentPdfFile=currentPdfFile;

        if(PropertiesObj!=null) {
            setupOCMaps(PropertiesObj, currentPdfFile);
        }

        final PdfObject layerDict=OCProperties.getDictionary(PdfDictionary.D);

        if(layerDict==null) {
            return;
        }

        int OCBaseState=layerDict.getNameAsConstant(PdfDictionary.BaseState);

        //if not set use default
        if(OCBaseState== PdfDictionary.Unknown) {
            OCBaseState = PdfDictionary.ON;
        }

        //read order first and may be over-written by ON/OFF
        order=layerDict.getObjectArray(PdfDictionary.Order);

        if(debug){
            System.out.println("PropertiesObj="+PropertiesObj);
            System.out.println("layerDict="+layerDict);
            System.out.println("propertyMap="+propertyMap);
            System.out.println("propertyMap="+propertyMap);
            System.out.println("refToPropertyID="+refToPropertyID);
            System.out.println("refTolayerName="+refTolayerName);

            System.out.println("OCBaseState="+OCBaseState+" (ON="+ PdfDictionary.ON+ ')');


            System.out.println("order="+Arrays.toString(order));

            showValues("ON=",PdfDictionary.ON,layerDict);

            showValues("OFF=",PdfDictionary.OFF,layerDict);

            showValues("RBGroups=",PdfDictionary.RBGroups,layerDict);

        }
        /**
         * workout list of layers (can be in several places)
         */

        addLayer(OCBaseState, order,null);

        //read the ON and OFF values
        if(OCBaseState!=PdfDictionary.ON) //redundant if basestate on
        {
            addLayer(PdfDictionary.ON, layerDict.getKeyArray(PdfDictionary.ON), null);
        }

        if(OCBaseState!=PdfDictionary.OFF) //redundant if basestate off
        {
            addLayer(PdfDictionary.OFF, layerDict.getKeyArray(PdfDictionary.OFF), null);
        }

        /**
         * handle case where layers not explicitly switched on
         */
        if(OCBaseState==PdfDictionary.ON){// && layerDict.getKeyArray(PdfDictionary.OFF)==null){
            final Iterator keys=refToPropertyID.keySet().iterator();
            Object ref,layerName;
            while(keys.hasNext()){
                ref = keys.next();
                layerName=refToPropertyID.get(ref);

                refTolayerName.put(ref,layerName);

                if(! layersTested.containsKey(layerName)){
                	layersTested.put(layerName,"x");
                	layersEnabled.put(layerName,"x");
                }
            }
        }
        
        //set any locks
        setLocks(currentPdfFile,layerDict.getKeyArray(PdfDictionary.Locked));

        //any constraints
        setConstraints(layerDict.getKeyArray(PdfDictionary.RBGroups));

        //any Additional Dictionaries
        setAS(layerDict.getKeyArray(PdfDictionary.AS), currentPdfFile);


        /**
         * read any metadata
         */
        final int[] keys={PdfDictionary.Name,PdfDictionary.Creator};
        final String[] titles={"Name","Creator"};

        final int count=keys.length;
        String val;
        for(int jj=0;jj<count;jj++){
            val= layerDict.getTextStreamValue(keys[jj]);
            if(val!=null) {
                metaData.put(titles[jj], val);
            }
        }

        //list mode if set
        val=layerDict.getName(PdfDictionary.ListMode);
        if(val!=null) {
            metaData.put("ListMode", val);
        }

    }

    private static void showValues(final String s, final int key, final PdfObject layerDict) {

        final byte[][] keyValues=layerDict.getKeyArray(key);
        if(keyValues!=null) {

            StringBuilder values=new StringBuilder(s);
            for (final byte[] keyValue : keyValues) {
                if (keyValue == null) {
                    values.append("null ");
                } else {
                    values.append(new String(keyValue)).append(' ');
                }
            }

            System.out.println(values);

        }
    }

    /**
     * used by Javascript to flag that state has changed
     * @param flag is of type boolean
     */
    public void setChangesMade(final boolean flag) {
        changesMade=flag;
    }

    /**
     * build a list of constraints using layer names so
     *  we can switch off if needed
     * @param layer
     */
    private void setConstraints(final byte[][] layer) {

        if(layer ==null) {
            return;
        }

        final int layerCount = layer.length;

        //turn into list of names
        final String[] layers=new String[layerCount];
        for(int ii=0;ii< layerCount;ii++){

            final String ref=new String(layer[ii]);
            layers[ii]=(String)this.refTolayerName.get(ref);
        }

        for(int ii=0;ii< layerCount;ii++){

            if(isLayerName(layers[ii])){

                StringBuilder effectedLayers=new StringBuilder();
                for(int ii2=0;ii2< layerCount;ii2++){

                    if(ii==ii2) {
                        continue;
                    }


                    effectedLayers.append(layers[ii2]).append(',');
                }

                RBconstraints.put(layers[ii],effectedLayers.toString());


            }
        }
    }

    /**
     * create list for lookup
     */
    private void setupOCMaps(final PdfObject propertiesObj, final PdfObjectReader currentPdfFile) {

        final PdfKeyPairsIterator keyPairs=propertiesObj.getKeyPairsIterator();

        String glyphKey,ref;
        PdfObject glyphObj;

        while(keyPairs.hasMorePairs()){

            glyphKey=keyPairs.getNextKeyAsString();

            glyphObj=keyPairs.getNextValueAsDictionary();
            ref=glyphObj.getObjectRefAsString();

            currentPdfFile.checkResolved(glyphObj);

            final byte[][] childPairs=glyphObj.getKeyArray(PdfDictionary.OCGs);

            if(childPairs!=null) {
                setupchildOCMaps(childPairs, glyphKey, currentPdfFile);
            } else{
                propertyMap.put(ref,glyphObj);

                final String currentNames=(String) refToPropertyID.get(ref);
                if(currentNames==null) {
                    refToPropertyID.put(ref, glyphKey);
                } else {
                    refToPropertyID.put(ref, currentNames + ',' + glyphKey);
                }
            }
            //roll on
            keyPairs.nextPair();
        }

    }

    private void setupchildOCMaps(final byte[][] keys, final String glyphKey, final PdfObjectReader currentPdfFile) {

        String ref;
        PdfObject glyphObj;

        for (final byte[] key : keys) {

            ref = new String(key);
            glyphObj = new OCObject(ref);

            currentPdfFile.readObject(glyphObj);

            currentPdfFile.checkResolved(glyphObj);

            final byte[][] childPairs = glyphObj.getKeyArray(PdfDictionary.OCGs);

//System.out.println(glyphKey+" === "+glyphObj+" childPropertiesObj="+childPairs);

            if (childPairs != null) {
                setupchildOCMaps(childPairs, glyphKey, currentPdfFile);
            } else {

                propertyMap.put(ref, glyphObj);
                final String currentNames = (String) refToPropertyID.get(ref);
                if (currentNames == null) {
                    refToPropertyID.put(ref, glyphKey);
                } else {
                    refToPropertyID.put(ref, currentNames + ',' + glyphKey);
                }
                //System.out.println("Add key "+glyphKey+" "+refToPropertyID);
            }
        }
    }

    private void addLayer(final int status, final Object[] layer, String parentName) {

        if(layer ==null) {
            return;
        }

        if(debug) {
            padding += "   ";
        }

        final int layers = layer.length;

        String ref,name,layerName=null;

        PdfObject nextObject;

        for(int ii=0;ii< layers;ii++){

            if(layer[ii] instanceof String){
                //ignore
            }else if(layer[ii] instanceof byte[]){

                final byte[] rawRef=(byte[])layer[ii];
                ref =new String(rawRef);
                name=(String) refToPropertyID.get(ref);

                nextObject=(PdfObject)propertyMap.get(ref);

                if(nextObject==null){

                    if(rawRef!=null && rawRef.length>1 && rawRef[rawRef.length-1]=='R'){
                        nextObject=new OCObject(ref);
                        currentPdfFile.readObject(nextObject);
                        name=ref;
                    }else{ //it is a name for the level so add into path of name

                        if(parentName==null) {
                            parentName = ref;
                        } else {
                            parentName = ref + deliminator + parentName;
                        }
                    }
                }

                if(nextObject!=null){

                    layerCount++;

                    layerName=nextObject.getTextStreamValue(PdfDictionary.Name);

                    if(parentName!=null) {
                        layerName = layerName + deliminator + parentName;
                    }
                    
                    if(debug) {
                        System.out.println(padding + "[layer1] add layer=" + layerName + " ref=" + ref + " parent=" + parentName + " refToLayerName=" + refTolayerName.get(ref) + " ref=" + ref);
                    }

                    refTolayerName.put(ref,layerName);

                    //and write back name value
                    layer[ii]=layerName;

                    layerNames.put(layerName, status);
                    if(name.indexOf(',')==-1){
                        final String oldValue=(String)streamToName.get(name);
                        if(oldValue==null) {
                            streamToName.put(name, layerName);
                        } else {
                            streamToName.put(name, oldValue + ',' + layerName);
                        }
                    }else{
                        final StringTokenizer names=new StringTokenizer(name,",");
                        while(names.hasMoreTokens()){
                            name=names.nextToken();
                            final String oldValue=(String)streamToName.get(name);
                            if(oldValue==null) {
                                streamToName.put(name, layerName);
                            } else {
                                streamToName.put(name, oldValue + ',' + layerName);
                            }
                        }
                    }

                    //must be done as can be defined in order with default and then ON/OFF as well
                    if(status==PdfDictionary.ON){
                        layersEnabled.put(layerName,"x");
                    }else{
                        layersEnabled.remove(layerName);
                    }
                }
            }else {
                addLayer(status, (Object[]) layer[ii], layerName);
            }
        }

        if(debug){
            final int len=padding.length();

            if(len>3) {
                padding = padding.substring(0, len - 3);
            }
        }
    }

    private void addLayer(final int status, final byte[][] layer, final String parentName) {

        if(layer ==null) {
            return;
        }

        String ref,name;

        PdfObject nextObject;

        for (final byte[] aLayer : layer) {

            ref = new String(aLayer);
            name = (String) refToPropertyID.get(ref);
            nextObject = (PdfObject) propertyMap.get(ref);

            if (nextObject != null) {

                layerCount++;

                String layerName = nextObject.getTextStreamValue(PdfDictionary.Name);

                if (parentName != null) {
                    layerName = layerName + deliminator + parentName;
                }

                //pick up full name set by Order
                if (status == PdfDictionary.ON || status == PdfDictionary.OFF) {
                    final String possName = (String) refTolayerName.get(ref);
                    if (possName != null) {
                        layerName = possName;
                    }
                }

                if (debug) {
                    System.out.println(padding + "[layer0] add layer=" + layerName + " ref=" + ref + " parent=" + parentName + " refToLayerName=" + refTolayerName.get(ref) + " status=" + status);
                }

                if (refTolayerName.get(ref) == null) {

                    refTolayerName.put(ref, layerName);

                    layerNames.put(layerName, status);
                }

                if (streamToName.get(name) != null) {//ignore if done
                } else if (name.indexOf(',') == -1) {
                    final String oldValue = (String) streamToName.get(name);
                    if (oldValue == null) {
                        streamToName.put(name, layerName);
                    } else {
                        streamToName.put(name, oldValue + ',' + layerName);
                    }
                } else {
                    final StringTokenizer names = new StringTokenizer(name, ",");
                    while (names.hasMoreTokens()) {
                        name = names.nextToken();
                        final String oldValue = (String) streamToName.get(name);
                        if (oldValue == null) {
                            streamToName.put(name, layerName);
                        } else {
                            streamToName.put(name, oldValue + ',' + layerName);
                        }
                    }
                }

                //must be done as can be defined in order with default and then ON/OFF as well
                if (status == PdfDictionary.ON) {
                    layersEnabled.put(layerName, "x");
                } else {
                    layersEnabled.remove(layerName);
                }

                layersTested.put(layerName, "x");
            }
        }
    }

    private void setAS(final byte[][] AS, final PdfObjectReader currentPdfFile) {

        if(AS ==null) {
            return;
        }

        int event;

        String ref, name,layerName;

        byte[][] OCGs;

        PdfObject nextObject;

        for (final byte[] A : AS) {

            //can also be a direct command which is not yet implemented
            if (A == null) {
                continue;
            }

            ref = new String(A);

            nextObject = new OCObject(ref);
            if (A[0] == '<') {
                nextObject.setStatus(PdfObject.UNDECODED_DIRECT);
            } else {
                nextObject.setStatus(PdfObject.UNDECODED_REF);
            }

            //must be done AFTER setStatus()
            nextObject.setUnresolvedData(A, PdfDictionary.AS);
            currentPdfFile.checkResolved(nextObject);

            event = nextObject.getParameterConstant(PdfDictionary.Event);
            if (nextObject != null) {

                if (event == PdfDictionary.View) {
                    OCGs = nextObject.getKeyArray(PdfDictionary.OCGs);

                    if (OCGs != null) {

                        for (final byte[] OCG : OCGs) {

                            ref = new String(OCG);
                            nextObject = new OCObject(ref);
                            if (OCG[0] == '<') {
                                nextObject.setStatus(PdfObject.UNDECODED_DIRECT);
                            } else {
                                nextObject.setStatus(PdfObject.UNDECODED_REF);
                            }

                            //must be done AFTER setStatus()
                            nextObject.setUnresolvedData(OCG, PdfDictionary.OCGs);
                            currentPdfFile.checkResolved(nextObject);

                            layerName = nextObject.getTextStreamValue(PdfDictionary.Name);
                            name = (String) refToPropertyID.get(ref);

                            streamToName.put(name, layerName);

                            //System.out.println((char)OCGs[jj][0]+" "+ref+" "+" "+nextObject+" "+nextObject.getTextStreamValue(PdfDictionary.Name));

                            final PdfObject usageObj = nextObject.getDictionary(PdfDictionary.Usage);

                            if (usageObj != null) {
                                final PdfObject zoomObj = usageObj.getDictionary(PdfDictionary.Zoom);

                                //set zoom values
                                if (zoomObj != null) {
                                    final float min = zoomObj.getFloatNumber(PdfDictionary.min);
                                    if (min != 0) {
                                        minScale.put(layerName, min);
                                    }
                                    final float max = zoomObj.getFloatNumber(PdfDictionary.max);

                                    if (max != 0) {
                                        maxScale.put(layerName, max);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    //
                }
                //layerCount++;

                //String layerName=nextObject.getTextStreamValue(PdfDictionary.Name);

                //if(debug)
                //System.out.println("[AS] add AS="+layerName);

                //refTolayerName.put(ref,layerName);

                //layerNames.put(layerName,new Integer(status));

//                if(layerName.indexOf(",")==-1){
//                    String oldValue=(String)streamToName.get(layerName);
//                    if(oldValue==null)
//                        streamToName.put(layerName,layerName);
//                    else
//                        streamToName.put(layerName,oldValue+","+layerName);
//                }else{
//                    StringTokenizer names=new StringTokenizer(layerName,",");
//                    while(names.hasMoreTokens()){
//                        layerName=names.nextToken();
//                        String oldValue=(String)streamToName.get(layerName);
//                        if(oldValue==null)
//                            streamToName.put(layerName,layerName);
//                        else
//                            streamToName.put(layerName,oldValue+","+layerName);
//                    }
//                }


            }
        }
    }

    private void setLocks(final PdfObjectReader currentPdfFile, final byte[][] layer) {

        if(layer ==null) {
            return;
        }

        for (final byte[] aLayer : layer) {

            final String nextValue = new String(aLayer);

            final PdfObject nextObject = new OCObject(nextValue);

            currentPdfFile.readObject(nextObject);

            final String layerName = nextObject.getTextStreamValue(PdfDictionary.Name);

            layerLocks.put(layerName, "x");

        }
    }

    public Map getMetaData() {
        return Collections.unmodifiableMap(metaData);
    }

    public Object[] getDisplayTree(){

        if(order!=null) {
            return order;
        } else {
            return getNames();
        }
    }

    /**
     * return list of layer names as String array
     */
    private String[] getNames() {

        final int count=layerNames.size();
        final String[] nameList=new String[count];

        final Iterator names=layerNames.keySet().iterator();

        int jj=0;
        while(names.hasNext()){
            nameList[jj]=names.next().toString();
            jj++;
        }


        return nameList;
    }

    /**
     * will display only these layers and hide all others and will override
     * any constraints.
     * If you pass null in, all layers will be removed
     * @param layerNames is of type String[]
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setVisibleLayers(final String[] layerNames) {

    	layersEnabled.clear();

    	if(layerNames!=null){

	    	for (final String layerName : layerNames) {
                layersEnabled.put(layerName, "x");
            }
    	}

        //flag it has been altered
        changesMade=true;
    }


    /**
     * Used internally only. takes name in Stream (ie MC7 and works out if we
     * need to decode) if isID==true.
     *
     * @param name is of type String
     * @param isID is of type boolean
     * @return type boolean
     */
    public boolean decodeLayer(final String name, final boolean isID) {

        if(layerCount==0) {
            return true;
        }

        boolean isLayerVisible =false;

        String layerName=name;

        //see if match found otherwise assume name
        if(isID){
            final String mappedName=(String)streamToName.get(name);

            if(mappedName!=null) {
                layerName = mappedName;
            }
        }

        if(layerName ==null) {
            return false;
        } else{

            //if multiple layers  them comma separated list
            if(layerName.indexOf(',')==-1){
                isLayerVisible =layersEnabled.containsKey(layerName);

                if(isLayerVisible) {
                    isLayerVisible = hiddenByParent(isLayerVisible, layerName);
                }

            }else{
                final StringTokenizer names=new StringTokenizer(layerName,",");
                while(names.hasMoreTokens()){

                    final String nextName=names.nextToken();
                    isLayerVisible =layersEnabled.containsKey(nextName);

                    if(isLayerVisible) {
                        isLayerVisible = hiddenByParent(isLayerVisible, nextName);
                    }

                    if(isLayerVisible) //exit on first match
                    {
                        break;
                    }
                }
            }

            if(debug) {
                System.out.println("[isVisible] " + name + " decode=" + isLayerVisible + " enabled=" + layersEnabled + " layerName=" + layerName + " isEnabled=" + this.layersEnabled);
            }
            //System.out.println("stream="+streamToName);

            return isLayerVisible;
        }
    }

    //check not disabled by Parent up tree        
    private boolean hiddenByParent(boolean layerVisible, String layerName) {

        int id=layerName.indexOf(deliminator);

        if(layerVisible && id!=-1){

            String parent= layerName.substring(id+1,layerName.length());

            while(parent!=null && layerVisible && isLayerName(parent)){

                layerVisible =decodeLayer(parent,false);

                layerName=parent;
                id=layerName.indexOf(deliminator);
                if(id==-1) {
                    parent = null;
                } else {
                    parent = layerName.substring(id + 1, layerName.length());
                }
            }
        }
        
        return layerVisible;
    }

    /**
     * Switch on/off layers based on Zoom.
     * @param scaling is of type float
     * @return is of type boolean
     */
    public boolean setZoom(final float scaling) {

        String layerName;
        final Iterator minZoomLayers=minScale.keySet().iterator();
        while(minZoomLayers.hasNext()){

            layerName=(String) minZoomLayers.next();
            final Float minScalingValue= (Float) minScale.get(layerName);

            //Zoom off
            if(minScalingValue!=null){

                //System.out.println(layerName+" "+scaling+" "+minScalingValue);

                if(scaling< minScalingValue){
                    layersEnabled.remove(layerName);
                    changesMade=true;
                }else if(!layersEnabled.containsKey(layerName)){
                    layersEnabled.put(layerName,"x");
                    changesMade=true;
                }
            }
        }


        final Iterator maxZoomLayers=maxScale.keySet().iterator();
        while(maxZoomLayers.hasNext()){

            layerName=(String) minZoomLayers.next();
            final Float maxScalingValue= (Float) maxScale.get(layerName);
            if(maxScalingValue!=null){
                if(scaling> maxScalingValue){
                    layersEnabled.remove(layerName);
                    changesMade=true;
                }else if(!layersEnabled.containsKey(layerName)){
                    layersEnabled.put(layerName,"x");
                    changesMade=true;
                }
            }
        }

        return changesMade;
    }

    public boolean isVisible(final String layerName) {

        return layersEnabled.containsKey(layerName);
    }

    public void setVisiblity(final String layerName, final boolean isVisible) {

        if(debug) {
            System.out.println("[layer] setVisiblity=" + layerName + " isVisible=" + isVisible);
        }

        if(isVisible){
            layersEnabled.put(layerName,"x");

            //disable any other layers
            final String layersToDisable=(String)RBconstraints.get(layerName);
            if(layersToDisable!=null){
                final StringTokenizer layers=new StringTokenizer(layersToDisable,",");
                while(layers.hasMoreTokens()) {
                    layersEnabled.remove(layers.nextToken());
                }
            }
        }else {
            layersEnabled.remove(layerName);
        }

        //flag it has been altered
        changesMade=true;
    }

    public boolean isVisible(final PdfObject XObject) {

        //see if visible
        boolean isVisible = true;

        //if layer object attached see if should be visible
        final PdfObject layerObj = XObject.getDictionary(PdfDictionary.OC);

        if (layerObj != null) {

            String layerName=null;
            
            final PdfObject OCGs_as_dictionary=layerObj.getDictionary(PdfDictionary.OCGs);
            if(OCGs_as_dictionary!=null){ //look at viewtate first
                
                /**
                 * NOTE!!!! Print and other modes not implemented yet
                 * (just added what I needed to fix 17584)
                 */
                
                //check viewmode flag
                final PdfObject usage=OCGs_as_dictionary.getDictionary(PdfDictionary.Usage);
                if(usage!=null){
                    final PdfObject viewState=usage.getDictionary(PdfDictionary.View);
                    if(viewState!=null){
                        isVisible=viewState.getNameAsConstant(PdfDictionary.ViewState)==PdfDictionary.ON;
                    }
                }
            }else{
                final byte[][] OCGS = layerObj.getKeyArray(PdfDictionary.OCGs);
                
                if(OCGS!=null){
                    for (final byte[] OCG : OCGS) {
                        final String ref = new String(OCG);
                        layerName = getNameFromRef(ref);
                    }
                }
                
                if(layerName==null) {
                    layerName = layerObj.getTextStreamValue(PdfDictionary.Name);
                }
                
                if (layerName != null  && isLayerName(layerName)) {
                    isVisible = isVisible(layerName);
                }
            }
        }

        return isVisible;
    }

    public boolean isLocked(final String layerName) {
        
        return layerLocks.containsKey(layerName);  //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * show if decoded version match visibility flags which can be altered by
     * user
     *
     * @return type boolean
     */
    public boolean getChangesMade() {
        return changesMade;
    }

    /**
     * show if is name of layer (as opposed to just label).
     *
     * @param name is of type String
     * @return is of type boolean
     */
    public boolean isLayerName(final String name) {
        return layerNames.containsKey(name);
    }

    /**
     * number of layers setup.
     *
     * @return is of type int.
     */
    public int getLayersCount() {
        return layerCount;
    }

    public String getNameFromRef(final String ref) {
        return (String) refTolayerName.get(ref);
    }

//    public void setScaling(float scaling) {
//        this.scaling=scaling;
//    }

	/**JS
	 * Gets an array of OCG objects found on a specified page.
	 *
	 * @return - An array of OCG objects or null if no OCGs are present.
	 */
	public Object[] getOCGs(){

        //return once initialised
        if(layers!=null) {
            return layers;
        }

        final int count=layerNames.size();

        //create array of values with access to this so we can reset
        final Layer[] layers=new Layer[count];

        final Iterator layersIt=layerNames.keySet().iterator();
        int ii=0;
        String name;
        while(layersIt.hasNext()){
            name=(String)layersIt.next();
            
            layers[ii]=new Layer(name,this);
            ii++;
        }
		
		return layers;
	}


    public void addJScommand(final String name, final String js) {

        if(jsCommands==null) {
            jsCommands = new HashMap();
        }
                
        //add to list to execute
        jsCommands.put(name,js);
    }

    public Iterator getJSCommands() {

        if(jsCommands!=null){
            final Iterator names= this.jsCommands.keySet().iterator();
            final Map visibleJSCommands=new HashMap();
        	
            while(names.hasNext()){
            	final String name=(String) names.next();
            	if(this.isVisible(name)){
            		visibleJSCommands.put(jsCommands.get(name), "x");
            	}
            }
            
            return visibleJSCommands.keySet().iterator();
        }else {
            return null;
        }
    }

    public int getOCpageNumber() {
        return OCpageNumber;
    }
}
