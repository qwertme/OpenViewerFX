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
 * PdfObjectCache.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.raw.*;
import org.jpedal.utils.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * caches for data
 */
public class PdfObjectCache {

    public static final int ColorspacesUsed=1;
    public static final int Colorspaces=2;
    public static final int ColorspacesObjects=3;
    public static final int GlobalShadings=4;
    public static final int LocalShadings=5;

    //init size of maps
    private static final int initSize=50;

    //int values for all colorspaces
    private final Map colorspacesUsed=new HashMap(initSize);

    private final Map colorspacesObjects=new HashMap(initSize);

    /**colors*/
    private Map colorspaces=new HashMap(initSize);

    private Map globalXObjects = new HashMap(initSize),localXObjects=new HashMap(initSize);

    public final Map XObjectColorspaces=new HashMap(initSize);

    public final Map patterns=new HashMap(initSize);
    private final Map globalShadings=new HashMap(initSize);
    private Map localShadings=new HashMap(initSize);

    final Map imposedImages = new HashMap(initSize);

    public PdfObject groupObj;

    /**fonts*/
    public Map unresolvedFonts=new HashMap(initSize);
    public Map directFonts=new HashMap(initSize);
    public Map resolvedFonts=new HashMap(initSize);

    /**GS*/
    Map GraphicsStates=new HashMap(initSize);

    public PdfObjectCache copy() {

        final PdfObjectCache copy=new PdfObjectCache();

        copy.localShadings=localShadings;
        copy.unresolvedFonts=unresolvedFonts;
        copy.GraphicsStates= GraphicsStates;
        copy.directFonts= directFonts;
        copy.resolvedFonts= resolvedFonts;
        copy.colorspaces= colorspaces;

        copy.localXObjects= localXObjects;
        copy.globalXObjects= globalXObjects;

        copy.groupObj= groupObj;


        return copy;

    }

    public PdfObjectCache() {}

    public void put(final int type, final int key, final Object value){
        switch(type){
            case ColorspacesUsed:
                colorspacesUsed.put(key,value);
                break;
            case ColorspacesObjects:
                colorspacesObjects.put(key,value);
                break;
        }
    }
    
    public void put(final int type, final String key, final Object value){
        switch(type){
            case ColorspacesUsed:
                colorspacesUsed.put(key,value);
                break;
            case ColorspacesObjects:
                colorspacesObjects.put(key,value);
                break;
        }
    }
    
    public boolean containsKey(final int key, final Object value){

        boolean returnValue=true;

        switch(key){
            case ColorspacesObjects:
                returnValue=colorspacesObjects.containsKey(key);
                break;
        }

        return returnValue;
    }

    public Iterator iterator(final int type){

        Iterator returnValue=null;

        switch(type){
            case ColorspacesUsed:
                returnValue=colorspacesUsed.keySet().iterator();
                break;
            case ColorspacesObjects:
                returnValue=colorspacesObjects.keySet().iterator();
                break;
        }

        return returnValue;
    }

    public Object get(final int key, final Object value){

        Object returnValue=null;

        switch(key){
            case ColorspacesUsed:
                returnValue=colorspacesUsed.get(value);
                break;
            case Colorspaces:
                returnValue=colorspaces.get(value);
                break;
            case ColorspacesObjects:
                returnValue=colorspacesObjects.get(value);
                break;
            case GlobalShadings:
                returnValue=globalShadings.get(value);
                break;
            case LocalShadings:
                returnValue=localShadings.get(value);
                break;

        }

        return returnValue;
    }

    public void resetFonts() {
        resolvedFonts.clear();
        unresolvedFonts.clear();
        directFonts.clear();
    }

    public PdfObject getXObjects(final String localName) {

        PdfObject XObject = (PdfObject) localXObjects.get(localName);
        if (XObject == null) {
            XObject = (PdfObject) globalXObjects.get(localName);
        }

        return XObject;
    }

    public void readResources(final PdfObject Resources, final boolean resetList)  throws PdfException{


        //decode
        final String[] names={"ColorSpace","ExtGState","Font", "Pattern","Shading","XObject"};
        final int[] keys={PdfDictionary.ColorSpace, PdfDictionary.ExtGState, PdfDictionary.Font,
                PdfDictionary.Pattern, PdfDictionary.Shading,PdfDictionary.XObject};

        for(int ii=0;ii<names.length;ii++){

            if(keys[ii]==PdfDictionary.Font || keys[ii]==PdfDictionary.XObject) {
                readArrayPairs(Resources, resetList, keys[ii]);
            } else {
                readArrayPairs(Resources, false, keys[ii]);
            }
        }
    }

    private void readArrayPairs(final PdfObject Resources, final boolean resetFontList, final int type) {

        final boolean debugPairs=false;

        if(debugPairs){
            System.out.println("-------------readArrayPairs-----------"+type);
            System.out.println("new="+Resources+ ' '+Resources.getObjectRefAsString());
        }
        String id,value;

        /**
         * new code
         */
        if(Resources!=null){

            final PdfObject resObj=Resources.getDictionary(type);

            if(debugPairs) {
                System.out.println("new res object=" + resObj);
            }

            if(resObj!=null){

                /**
                 * read all the key pairs for Glyphs
                 */
                final PdfKeyPairsIterator keyPairs=resObj.getKeyPairsIterator();

                PdfObject obj;

                if(debugPairs){
                    System.out.println("New values");
                    System.out.println("----------");
                }

                while(keyPairs.hasMorePairs()){

                    id=keyPairs.getNextKeyAsString();
                    value=keyPairs.getNextValueAsString();
                    obj=keyPairs.getNextValueAsDictionary();

                    if(debugPairs) {
                        System.out.println(id + ' ' + obj + ' ' + value + ' ' + Resources.isDataExternal());
                    }

                    if(Resources.isDataExternal()){ //check and flag if missing

                        //ObjectDecoder objectDecoder=new ObjectDecoder(currentPdfFile.getObjectReader());

                        if(obj==null && value==null){
                            Resources.setFullyResolved(false);
                            return;
                        }else if(obj==null){

                            final PdfObject childObj= ObjectFactory.createObject(type, value, type, -1);

                            childObj.setStatus(PdfObject.UNDECODED_DIRECT);
                            childObj.setUnresolvedData(StringUtils.toBytes(value), type);

//                            if(!objectDecoder.resolveFully(childObj)){
//                                Resources.setFullyResolved(false);
//                                return;
//                            }

                            //cache if setup
                            if(type==PdfDictionary.Font){
                                directFonts.put(id,childObj);
                            }
//                        }else if(!objectDecoder.resolveFully(obj)){
//                            Resources.setFullyResolved(false);
//                            return;
                        }
                    }

                    switch(type){

                        case PdfDictionary.ColorSpace:
                            colorspaces.put(id,obj);
                            break;

                        case PdfDictionary.ExtGState:
                            GraphicsStates.put(id,obj);
                            break;

                        case PdfDictionary.Font:

                            unresolvedFonts.put(id,obj);

                            break;

                        case PdfDictionary.Pattern:
                            patterns.put(id,obj);

                            break;

                        case PdfDictionary.Shading:
                            if(resetFontList) {
                                globalShadings.put(id, obj);
                            } else {
                                localShadings.put(id, obj);
                            }

                            break;

                        case PdfDictionary.XObject:
                            if(resetFontList) {
                                globalXObjects.put(id, obj);
                            } else {
                                localXObjects.put(id, obj);
                            }

                            break;

                    }

                    keyPairs.nextPair();
                }
            }
        }
    }


    public void reset(final PdfObjectCache newCache) {

        //reset copies
        localShadings=new HashMap(initSize);
        resolvedFonts=new HashMap(initSize);
        unresolvedFonts=new HashMap(initSize);
        directFonts=new HashMap(initSize);
        colorspaces=new HashMap(initSize);
        GraphicsStates=new HashMap(initSize);
        localXObjects=new HashMap(initSize);

        Iterator keys=newCache.GraphicsStates.keySet().iterator();
        while(keys.hasNext()){
            final Object key=keys.next();
            GraphicsStates.put(key,newCache.GraphicsStates.get(key));
        }

        keys=newCache.colorspaces.keySet().iterator();
        while(keys.hasNext()){
            final Object key=keys.next();
            colorspaces.put(key, newCache.colorspaces.get(key));
        }


        keys=newCache.localXObjects.keySet().iterator();
        while(keys.hasNext()){
            final Object key=keys.next();
            localXObjects.put(key, newCache.localXObjects.get(key));
        }

        keys=newCache.globalXObjects.keySet().iterator();
        while(keys.hasNext()){
            final Object key=keys.next();
            globalXObjects.put(key, newCache.globalXObjects.get(key));
        }

        //allow for no fonts in FormObject when we use any global
        if(unresolvedFonts.isEmpty()){
            //unresolvedFonts=rawFonts;
            keys=newCache.unresolvedFonts.keySet().iterator();
            while(keys.hasNext()){
                final Object key=keys.next();
                unresolvedFonts.put(key,newCache.unresolvedFonts.get(key));
            }
        }
    }

    public void restore(final PdfObjectCache mainCache) {

        directFonts= mainCache.directFonts;
        unresolvedFonts= mainCache.unresolvedFonts;
        resolvedFonts= mainCache.resolvedFonts;
        GraphicsStates= mainCache.GraphicsStates;
        colorspaces= mainCache.colorspaces;
        localShadings= mainCache.localShadings;
        localXObjects= mainCache.localXObjects;
        globalXObjects= mainCache.globalXObjects;

        groupObj= mainCache.groupObj;

    }

    public void setImposedKey(final String key, final int id) {
        if (imposedImages != null) {
            imposedImages.put(key, id);
        }
    }
}
