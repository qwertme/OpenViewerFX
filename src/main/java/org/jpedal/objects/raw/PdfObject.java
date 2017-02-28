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
 * PdfObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.awt.Shape;
import java.util.Collections;

import org.jpedal.fonts.StandardFonts;
import org.jpedal.io.PdfFileReader;
import org.jpedal.io.ObjectStore;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;
import org.jpedal.utils.StringUtils;

/**
 * holds actual data for PDF file to process
 */
public class PdfObject implements Cloneable{
    
    protected boolean maybeIndirect;
    protected boolean isFullyResolved=true;
    protected boolean isDataExternal;
    
    private boolean streamMayBeCorrupt;
    
    Shape clip;
    
    byte[] convertedData;
    
    /**
     * states
     */
    public static final int DECODED=0;
    public static final int UNDECODED_REF=1;
    public static final int UNDECODED_DIRECT=2;
    
    private int status;
    
    byte[] unresolvedData;
    
    //hold Other dictionary values
    final Map otherValues=new HashMap();
    
    protected int pageNumber = -1;
    
    int PDFkeyInt=-1;
    
    //our type which may not be same as /Type
    int objType=PdfDictionary.Unknown;
    
    //key of object
    private int id=-1;
    
    protected int colorspace=PdfDictionary.Unknown, subtype=PdfDictionary.Unknown,type=PdfDictionary.Unknown;
    
    private int BitsPerComponent=-1, BitsPerCoordinate=-1, BitsPerFlag=-1, Count, FormType=-1, Length=-1,Length1=-1,Length2=-1,Length3=-1,Rotate=-1,verticesPerRow=-1; //-1 shows unset
    
    private float[] ArtBox, BBox, BleedBox, CropBox, Decode,Domain, Matrix, Matte, MediaBox, Range, TrimBox;
    
    protected  PdfObject ColorSpace, DecodeParms, Encoding,Function,
            Resources,Shading, SMask;
    
    private boolean ignoreRecursion, ignoreStream;
    
    //used by font code
    protected boolean isZapfDingbats, isSymbol;
    
    private boolean isCompressedStream;
    
    protected int generalType=PdfDictionary.Unknown; // some Dictionaries can be a general type (ie /ToUnicode /Identity-H)
    
    private String generalTypeAsString; //some values (ie CMAP can have unknown settings)
    
    //flag to show if we want parents (generally NO as will scan all up tree every time to root)
    protected boolean includeParent;
    
    private String Creator, Parent,Name, S, Title;
    private byte[] rawCreator,rawParent,rawName, rawS, rawTitle;
    public static boolean debug;
    
    protected String ref;
    int intRef,gen;
    
    protected boolean hasStream;
    
    public byte[] stream;
    private byte[] DecodedStream;
    
    //use for caching
    private long startStreamOnDisk=-1;
    private PdfFileReader objReader;
    private String cacheName;
    
    private byte[][] Filter, TR;
    
    private byte[][] keys;
    
    private byte[][] values;
    
    private Object[] DecodeParmsAsArray;
    
    private PdfObject[] objs;
    
    //used by /Other
    protected Object currentKey;
    
    //used to track AP
    protected int parentType=-1;
    private boolean isInCompressedStream;
    
    /** used to give the number of a new XFA reference ie. 1 0 X (XFA internal form) */
    private static int newXFAFormID = 1;
    
    /** set this PdfObject up as an internal object and define its reference */
    @SuppressWarnings("UnusedDeclaration")
    protected void setInternalReference(){
        //if this is an internal object generate the next key
        ref = (newXFAFormID++)+" 0 X";
    }
    
    protected PdfObject(){
        
    }
    
    
    public PdfObject(final int intRef, final int gen) {
        setRef(intRef,  gen);
    }
    
    public void setRef(final int intRef, final int gen){
        this.intRef=intRef;
        this.gen=gen;
        
        //force reset as may have changed
        ref=null;
        
    }
    
    /**
     * name of file with cached data on disk or null
     * @param objReader Initializes a PdfFileReader object
     * @return The name of the cache
     */
    public String getCacheName(final PdfFileReader objReader){
        
        if(isCached()){
            cacheName=null;
            this.getCachedStreamFile(objReader);
        }
        return cacheName;
    }
    
    public void setRef(final String ref){
        
        this.ref=ref;
        
    }
    
    public PdfObject(final String ref){
        this.ref=ref;
        
        //int ptr=ref.indexOf(" ");
        //if(ptr>0)
        //	intRef=PdfObjectReader.parseInt(0, ptr, StringUtils.toBytes(ref));
        
    }
    
    public PdfObject(final int type) {
        this.generalType=type;
    }
    
    protected static boolean[] deepCopy(final boolean[] input){
        
        if(input==null) {
            return null;
        }
        
        final int count=input.length;
        
        final boolean[] deepCopy=new boolean[count];
        System.arraycopy(input,0,deepCopy,0,count);
        
        return deepCopy;
    }
    
    public int getStatus(){
        return status;
    }
    
    public byte[] getUnresolvedData(){
        return unresolvedData;
    }
    
    public int getPDFkeyInt(){
        return PDFkeyInt;
    }
    
    public void setUnresolvedData(final byte[] unresolvedData, final int PDFkeyInt){
        this.unresolvedData=unresolvedData;
        this.PDFkeyInt=PDFkeyInt;
    }
    
    public void setStatus(final int status){
        this.status=status;
        this.unresolvedData=null;
    }
    
    protected static float[] deepCopy(final float[] input){
        
        if(input==null) {
            return null;
        }
        
        final int count=input.length;
        
        final float[] deepCopy=new float[count];
        System.arraycopy(input,0,deepCopy,0,count);
        
        return deepCopy;
    }
    
    protected static double[] deepCopy(final double[] input){
        
        if(input==null) {
            return null;
        }
        
        final int count=input.length;
        
        final double[] deepCopy=new double[count];
        System.arraycopy(input,0,deepCopy,0,count);
        
        return deepCopy;
    }
    
    protected static int[] deepCopy(final int[] input){
        
        if(input==null) {
            return null;
        }
        
        final int count=input.length;
        
        final int[] deepCopy=new int[count];
        System.arraycopy(input,0,deepCopy,0,count);
        
        return deepCopy;
    }
    
    protected static byte[][] deepCopy(final byte[][] input){
        
        if(input==null) {
            return null;
        }
        
        final int count=input.length;
        
        final byte[][] deepCopy=new byte[count][];
        System.arraycopy(input,0,deepCopy,0,count);
        
        return deepCopy;
    }
    
    public PdfObject getDictionary(final int id){
        
        switch(id){
            
            case PdfDictionary.ColorSpace:
                return ColorSpace;
                
            case PdfDictionary.DecodeParms:
                return DecodeParms;
                
            case PdfDictionary.Function:
                return Function;
                
            case PdfDictionary.Resources:
                return Resources;
                
            case PdfDictionary.Shading:
                return Shading;
                
            case PdfDictionary.SMask:
                return SMask;
                
            default:
                
                //
                
                return null;
        }
    }
    
    public int getGeneralType(final int id){
        
        //special case
        if(id==PdfDictionary.Encoding && isZapfDingbats) //note this is Enc object so local
        {
            return StandardFonts.ZAPF;
        } else if(id==PdfDictionary.Encoding && isSymbol) //note this is Enc object so local
        {
            return StandardFonts.SYMBOL;
        } else if(id==PdfDictionary.Type) {
            return objType;
        } else {
            return generalType;
        }
    }
    
    public String getGeneralStringValue(){
        return generalTypeAsString;
    }
    
    public void setGeneralStringValue(final String generalTypeAsString){
        this.generalTypeAsString=generalTypeAsString;
    }
    
    public void setIntNumber(final int id, final int value){
        
        switch(id){
            
            case PdfDictionary.BitsPerComponent:
                BitsPerComponent=value;
                break;
                
            case PdfDictionary.BitsPerCoordinate:
                BitsPerCoordinate=value;
                break;
                
            case PdfDictionary.BitsPerFlag:
                BitsPerFlag=value;
                break;
                
            case PdfDictionary.Count:
                Count=value;
                break;
                
            case PdfDictionary.FormType:
                FormType=value;
                break;
                
            case PdfDictionary.Length:
                Length=value;
                break;
                
            case PdfDictionary.Length1:
                Length1=value;
                break;
                
            case PdfDictionary.Length2:
                Length2=value;
                break;
                
            case PdfDictionary.Length3:
                Length3=value;
                break;
                
            case PdfDictionary.Rotate:
                Rotate=value;
                break;
            
            case PdfDictionary.VerticesPerRow:
                verticesPerRow = value;
                break;
                
            default:
                
                //
        }
    }
    
    public void setFloatNumber(final int id, final float value){
        
        switch(id){
            
            //	        case PdfDictionary.BitsPerComponent:
            //	    		BitsPerComponent=value;
            //	    		break;
            
            default:
                
                //
        }
    }
    
    public int getInt(final int id){
        
        switch(id){
            
            case PdfDictionary.BitsPerComponent:
                return BitsPerComponent;
                
            case PdfDictionary.BitsPerCoordinate:
                return BitsPerCoordinate;
                
            case PdfDictionary.BitsPerFlag:
                return BitsPerFlag;
                
            case PdfDictionary.Count:
                return Count;
                
            case PdfDictionary.FormType:
                return FormType;
                
            case PdfDictionary.Length:
                return Length;
                
            case PdfDictionary.Length1:
                return Length1;
                
            case PdfDictionary.Length2:
                return Length2;
                
            case PdfDictionary.Length3:
                return Length3;
                
            case PdfDictionary.Rotate:
                return Rotate;
                
            case PdfDictionary.VerticesPerRow:
                return verticesPerRow;
                
            default:
                
                //
                return PdfDictionary.Unknown;
        }
    }
    
    public float getFloatNumber(final int id){
        
        switch(id){
            
            //        	case PdfDictionary.BitsPerComponent:
            //        		return BitsPerComponent;
            
            default:
                
                //
                return PdfDictionary.Unknown;
        }
    }
    
    public boolean getBoolean(final int id){
        
        switch(id){
            
            
            default:
                
                //
        }
        
        return false;
    }
    
    public void setBoolean(final int id, final boolean value){
        
        switch(id){
            
            
            default:
                
                //
        }
    }
    
    
    
    public void setDictionary(final int id, final PdfObject value){
        
        if(value!=null){
            value.id = id;
        }
        
        switch(id){
            
            case PdfDictionary.ColorSpace:
                ColorSpace=value;
                break;
                
            case PdfDictionary.DecodeParms:
                DecodeParms=value;
                break;
                
            case PdfDictionary.Function:
                Function=value;
                break;
                
            case PdfDictionary.Resources:
                Resources=value;
                break;
                
            case PdfDictionary.Shading:
                Shading=value;
                break;
                
            case PdfDictionary.SMask:
                SMask=value;
                break;
                
            default:
                
                setOtherValues(value);
                
                //
        }
    }
    
    /**
     * some values stored in a MAP for AP or Structurede Content
     */
    protected void setOtherValues(final PdfObject value) {
        
        if(objType== PdfDictionary.Form || objType==PdfDictionary.MCID || currentKey!=null){
            
            //if(1==1)
            //throw new RuntimeException("xx="+currentKey+" id="+id);
            
            otherValues.put(currentKey,value);
            currentKey=null;
        }
    }
    
    public void setID(final int id) {
        
        this.id=id;
        
    }
    
    public int getID() {
        
        return id;
        
    }
    
    /**
     * only used internally for some forms - please do not use
     * @return
     */
    public int getParentID(){
        return parentType;
    }
    
    /**
     * flag set for embedded data
     */
    public boolean hasStream() {
        return hasStream;
    }
    
    
    //    public int setConstant(int pdfKeyType, int keyStart, int keyLength, byte[] raw) {
    //
    //        //
    //
    //        return PdfDictionary.Unknown;
    //    }
    
    public int setConstant(final int pdfKeyType, final int keyStart, final int keyLength, final byte[] raw) {
        
        final int PDFvalue =PdfDictionary.Unknown;
        
        int id=0,x=0,next;
        
        try{
            
            //convert token to unique key which we can lookup
            
            for(int i2=keyLength-1;i2>-1;i2--){
                
                next=raw[keyStart+i2];
                
                next -= 48;
                
                id += ((next)<<x);
                
                x += 8;
            }
            
            switch(id){
                
                //            case PdfDictionary.Image:
                //                PDFvalue =PdfDictionary.Image;
                //            break;
                //
                //            case PdfDictionary.Form:
                //                PDFvalue =PdfDictionary.Form;
                //            break;
                
                
                
                default:
                    
                    //                	if(pdfKeyType==PdfDictionary.Encoding){
                    //                		PDFvalue=PdfCIDEncodings.getConstant(id);
                    //
                    //                		if(PDFvalue==PdfDictionary.Unknown){
                    //
                    //                			byte[] bytes=new byte[keyLength];
                    //
                    //                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
                    //
                    //                			unknownValue=new String(bytes);
                    //                		}
                    //
                    //                		if(debug && PDFvalue==PdfDictionary.Unknown){
                    //                			System.out.println("Value not in PdfCIDEncodings");
                    //
                    //                           	 byte[] bytes=new byte[keyLength];
                    //
                    //                               System.arraycopy(raw,keyStart,bytes,0,keyLength);
                    //                               System.out.println("Add to CIDEncodings and as String");
                    //                               System.out.println("key="+new String(bytes)+" "+id+" not implemented in setConstant in PdfFont Object");
                    //
                    //                               System.out.println("final public static int CMAP_"+new String(bytes)+"="+id+";");
                    //                		}
                    //                	}else
                    
                    if(PDFvalue==-1 && debug){
                            
                            final byte[] bytes=new byte[keyLength];
                            
                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
                            System.out.println("key="+new String(bytes)+ ' ' +id+" not implemented in setConstant in "+this);
                            
                            System.out.println("final public static int "+new String(bytes)+ '=' +id+ ';');
                            
                        }
                    
                    break;
                    
            }
            
        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        return id;
    }
    
    public int getParameterConstant(final int key) {
        final int def= PdfDictionary.Unknown;
        
        switch(key){
            
            case PdfDictionary.ColorSpace:
                return colorspace;
                
            case PdfDictionary.Subtype:
                return subtype;
                
            case PdfDictionary.Type:
                return type;
                
        }
        
        return def;
    }
    
    /**
     * common values shared between types
     * @param pdfKeyType
     * @param id The id to set
     * @return the value of PDF
     */
    public int setConstant(final int pdfKeyType, final int id) {
        int PDFvalue =id;
        
        
        /**
         * map non-standard
         */
        switch(id){
            
            case PdfDictionary.FontDescriptor:
                PDFvalue =PdfDictionary.Font;
                break;
                
        }
        
        
        switch(pdfKeyType){
            
            case PdfDictionary.ColorSpace:
                colorspace=PDFvalue;
                break;
                
            case PdfDictionary.Subtype:
                subtype=PDFvalue;
                break;
                
            case PdfDictionary.Type:
                
                //@speed if is temp hack as picks up types on some subobjects
                //if(type==PdfDictionary.Unknown)
                this.type=PDFvalue;
                
                break;
        }
        
        return PDFvalue;
    }
    
    public float[] getFloatArray(final int id) {
        
        final float[] array=null;
        switch(id){
            
            case PdfDictionary.ArtBox:
                return deepCopy(ArtBox);
                
            case PdfDictionary.BBox:
                return deepCopy(BBox);
                
            case PdfDictionary.BleedBox:
                return deepCopy(BleedBox);
                
            case PdfDictionary.CropBox:
                return deepCopy(CropBox);
                
            case PdfDictionary.Decode:
                return deepCopy(Decode);
                
            case PdfDictionary.Domain:
                return deepCopy(Domain);
                
            case PdfDictionary.Matrix:
                return deepCopy(Matrix);
                
            case PdfDictionary.Matte:
                return deepCopy(Matte);
                
            case PdfDictionary.MediaBox:
                return deepCopy(MediaBox);
                
            case PdfDictionary.Range:
                return deepCopy(Range);
                
            case PdfDictionary.TrimBox:
                return deepCopy(TrimBox);
                
            default:
                
                //
        }
        
        return deepCopy(array);
    }
    
    public byte[][] getKeyArray(final int id) {
        
        switch(id){
            
            
            default:
                
                //
        }
        
        return null;
    }
    
    public double[] getDoubleArray(final int id) {
        
        final double[] array=null;
        switch(id){
            
            default:
                
                //
        }
        
        return deepCopy(array);
    }
    
    public boolean[] getBooleanArray(final int id) {
        
        final boolean[] array=null;
        switch(id){
            
            default:
                
                //
        }
        
        return deepCopy(array);
    }
    
    public int[] getIntArray(final int id) {
        
        final int[] array=null;
        switch(id){
            
            default:
                
                //
        }
        
        return deepCopy(array);
    }
    
    public void setFloatArray(final int id, final float[] value) {
        
        switch(id){
            
            case PdfDictionary.ArtBox:
                ArtBox=value;
                break;
                
            case PdfDictionary.BBox:
                BBox=value;
                break;
                
            case PdfDictionary.BleedBox:
                BleedBox=value;
                break;
                
            case PdfDictionary.CropBox:
                CropBox=value;
                break;
                
            case PdfDictionary.Decode:
                Decode=ignoreIdentity(value);
                break;
                
            case PdfDictionary.Domain:
                Domain=value;
                break;
                
            case PdfDictionary.Matrix:
                Matrix=value;
                break;
                
            case PdfDictionary.Matte:
                Matte=value;
                break;
                
            case PdfDictionary.MediaBox:
                MediaBox=value;
                break;
                
            case PdfDictionary.Range:
                Range=value;
                break;
                
            case PdfDictionary.TrimBox:
                TrimBox=value;
                break;
                
            default:
                
                //
        }
        
    }
    
    /**ignore identity value which makes no change*/
    private static float[] ignoreIdentity(final float[] value) {
        
        boolean isIdentity =true;
        if(value!=null){
            
            final int count=value.length;
            for(int aa=0;aa<count;aa += 2){
                if(value[aa]==0f && value[aa+1]==1f){
                    //okay
                }else{
                    isIdentity =false;
                    aa=count;
                }
            }
        }
        
        if(isIdentity) {
            return null;
        } else {
            return value;
        }
    }
    
    public void setIntArray(final int id, final int[] value) {
        
        switch(id){
            
            default:
                
                //
        }
        
    }
    
    public void setBooleanArray(final int id, final boolean[] value) {
        
        switch(id){
            
            default:
                
                //
        }
        
    }
    
    public void setDoubleArray(final int id, final double[] value) {
        
        switch(id){
            
            default:
                
                //
        }
        
    }
    
    
    public void setMixedArray(final int id, final byte[][] value) {
        
        switch(id){
            
            case PdfDictionary.Filter:
                
                Filter=value;
                break;
                
                
            default:
                
                //
        }
        
    }
    
    
    public String getStringValue(final int id, final int mode) {
        
        byte[] data=null;
        
        //get data
        switch(id){
            
            case PdfDictionary.Name:
                
                data=rawName;
                break;
                
        }
        
        //
        
        //convert
        switch(mode){
            case PdfDictionary.STANDARD:
                
                //setup first time
                if(data!=null) {
                    return new String(data);
                } else {
                    return null;
                }
                
                
            case PdfDictionary.LOWERCASE:
                
                //setup first time
                if(data!=null) {
                    return new String(data);
                } else {
                    return null;
                }
                
            case PdfDictionary.REMOVEPOSTSCRIPTPREFIX:
                
                //setup first time
                if(data!=null){
                    
                    final int len=data.length;
                    
                    if(len>6 && data[6]=='+'){ //lose ABCDEF+ if present
                        final int length=len-7;
                        final byte[] newData=new byte[length];
                        System.arraycopy(data, 7, newData, 0, length);
                        return new String(newData);
                    }else {
                        return new String(data);
                    }
                }else {
                    return null;
                }
                
            default:
                throw new RuntimeException("Value not defined in getName(int,mode)");
        }
    }
    
    //return as constant we can check
    public int getNameAsConstant(final int id) {
        //return PdfDictionary.generateChecksum(0,raw.length,raw);
        return PdfDictionary.Unknown;
    }
    
    public String getName(final int id) {
        
        final String str=null;
        switch(id){
            
            case PdfDictionary.Name:
                
                //setup first time
                if(Name==null && rawName!=null) {
                    Name = new String(rawName);
                }
                
                return Name;
                
                
                
                //            case PdfDictionary.Parent:
                //
                //                //setup first time
                //                if(Filter==null && rawParent!=null)
                //                    Parent=new String(rawParent);
                //
                //                return Parent;
                
            case PdfDictionary.S:
                
                //setup first time
                if(S==null && rawS!=null) {
                    S = new String(rawS);
                }
                
                return S;
                
            default:
                
                //
        }
        
        return str;
    }
    
    public String getStringKey(final int id) {
        
        final String str=null;
        switch(id){
            
            case PdfDictionary.Parent:
                
                //setup first time
                if(Parent==null && rawParent!=null) {
                    Parent = new String(rawParent);
                }
                
                return Parent;
                
            default:
                
                //
        }
        
        return str;
    }
    
    public String getTextStreamValue(final int id) {
        
        final String str=null;
        switch(id){
            
            case PdfDictionary.Creator:
                
                //setup first time
                if(Creator==null && rawCreator!=null) {
                    Creator = StringUtils.getTextString(rawCreator, false);
                }
                
                return Creator;
                
                //can also be stream in OCProperties
            case PdfDictionary.Name:
                
                //setup first time
                if(Name==null && rawName!=null) {
                    Name = StringUtils.getTextString(rawName, false);
                }
                
                return Name;
                
            case PdfDictionary.Title:
                
                //setup first time
                if(Title==null && rawTitle!=null) {
                    Title = StringUtils.getTextString(rawTitle, false);
                }
                
                return Title;
                
                //            case PdfDictionary.Filter:
                //
                //            //setup first time
                //            if(Filter==null && rawFilter!=null)
                //            Filter=PdfObjectReader.getTextString(rawFilter);
                //
                //            return Filter;
                
            default:
                
                //
        }
        
        return str;
    }
    
    public void setName(final int id, final byte[] value) {
        
        switch(id){
            
            case PdfDictionary.Name:
                rawName=value;
                break;
                
            case PdfDictionary.S:
                rawS=value;
                break;
                
            case PdfDictionary.Parent:
                
                //gets into endless loop if any obj so use sparingly
                if(includeParent) {
                    rawParent = value;
                }
                
                break;
                
            default:
                
                if(objType==PdfDictionary.MCID){
                    
                    //if(1==1)
                    //throw new RuntimeException("xx="+currentKey+" id="+id);
                    otherValues.put(currentKey,value);
                    //System.out.println("id="+id+" "+value+" "+type+" "+objType+" "+this+" "+otherValues);
                }else{
                    
                    //
                }
        }
        
    }
    
    public void setName(final Object id, final String value) {
        
        otherValues.put(id,value);
        // System.out.println("id="+id+" "+value+" "+type+" "+objType+" "+this+" "+otherValues);
    }
    
    public void setStringKey(final int id, final byte[] value) {
        
        switch(id){
            
            case PdfDictionary.Parent:
                rawParent=value;
                break;
                
            default:
                
                //
        }
        
    }
    
    
    public void setTextStreamValue(final int id, final byte[] value) {
        
        switch(id){
            
            case PdfDictionary.Creator:
                rawCreator=value;
                break;
                
            case PdfDictionary.Name:
                rawName=value;
                break;
                
            case PdfDictionary.Title:
                rawTitle=value;
                break;
                
            default:
                
                //
        }
        
    }
    
    public byte[] getDecodedStream() {
        
        if(isCached()){
            byte[] cached=null;
            
            try{
                
                final File f=new File(getCachedStreamFile(objReader));
                final BufferedInputStream bis=new BufferedInputStream(new FileInputStream(f));
                cached=new byte[(int)f.length()];
                
                //System.out.println(cached.length+" "+DecodedStream.length);
                bis.read(cached);
                bis.close();
                
                //System.out.println(new String(cached));
            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
            
            return cached;
        }else

        {
            return DecodedStream;
        }
    }
    
    /**public byte[] getStream() {
     * 
     * if(DecodedStream==null)
     * return null;
     * 
     * //make a a DEEP copy so we cant alter
     * int len=DecodedStream.length;
     * byte[] copy=new byte[len];
     * System.arraycopy(DecodedStream, 0, copy, 0, len);
     * 
     * return copy;
     * }/**/
    
    public void setStream(final byte[] stream) {
        this.stream=stream;
        
        if(this.getObjectType()==PdfDictionary.ColorSpace) {
            hasStream = true;
        }
    }
    
    public void setDecodedStream(final byte[] stream) {
        this.DecodedStream=stream;
    }
    
    public String getObjectRefAsString() {
        
        if(ref==null) {
            ref = intRef + " " + gen + " R";
        }
        
        return this.ref;
    }
    
    public int getObjectRefID() {
        
        //initialise if not set
        if(intRef==0 && ref!=null && !ref.contains("[")){
            
            try{
                final byte[] data=ref.getBytes();
                
                int j=0,keyStart;
                
                //allow for space at start
                while (data[j] == 91 || data[j] == 32 || data[j] == 13 || data[j] == 10) {
                    j++;
                }
                
                // get object ref
                keyStart = j;
                
                //move cursor to end of reference
                while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                    j++;
                }
                
                intRef = NumberUtils.parseInt(keyStart, j, data);
                
                //move cursor to start of generation or next value
                while (data[j] == 10 || data[j] == 13 || data[j] == 32)// || data[j]==47 || data[j]==60)
                {
                    j++;
                }
                
                /**
                 * get generation number
                 */
                keyStart = j;
                //move cursor to end of reference
                while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                    j++;
                }
                
                gen = NumberUtils.parseInt(keyStart, j, data);
                
            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }   
        }
        
        return intRef;
    }
    
    public int getObjectRefGeneration() {
        
        return gen;
    }
    
    public PdfArrayIterator getMixedArray(final int id) {
        
        switch(id){
            
            case PdfDictionary.Filter:
                return new PdfArrayIterator(Filter);
                
            default:
                //
                
                return null;
        }
    }
    
    public void setDictionaryPairs(final byte[][] keys, final byte[][] values, final PdfObject[] objs) {
        
        this.keys=keys;
        this.values=values;
        this.objs=objs;
        
        
    }
    
    public PdfKeyPairsIterator getKeyPairsIterator() {
        return new PdfKeyPairsIterator(keys,values,objs);
    }
    
    public void setKeyArray(final int id, final byte[][] keyValues) {
        
        switch(id){
            
            default:
                
                //
        }
    }
    
    public void setStringArray(final int id, final byte[][] keyValues) {
        
        switch(id){
            
            case PdfDictionary.TR:
                TR=keyValues;
                break;
                
            default:
                
                //
        }
    }
    
    
    public byte[][] getStringArray(final int id) {
        
        switch(id){
            
            case PdfDictionary.TR:
                return deepCopy(TR);
                
            default:
                
                //
        }
        
        return null;
    }
    
    public Object[] getObjectArray(final int id) {
        
        switch(id){
            
            case PdfDictionary.DecodeParms:
                return (DecodeParmsAsArray);
                
            default:
                
                //
        }
        
        return null;
    }
    
    public void setObjectArray(final int id, final Object[] objectValues) {
        
        switch(id){
            
            case PdfDictionary.DecodeParms:
                
                DecodeParmsAsArray= objectValues;
                break;
                
            default:
                
                //
        }
    }
    
    public PdfObject duplicate() {

        return new PdfObject();
    }
    
    @Override
    public Object clone()
    {
        Object o = null;
        try
        {
            o = super.clone();
        }
        catch( final CloneNotSupportedException e ){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
        
        return o;
    }
    
    
    
    public boolean decompressStreamWhenRead() {
        return false;
    }
    
    public int getObjectType() {
        return objType;
    }
    
    public int getRawObjectType() {
        return objType;
    }
    
    public byte[] getStringValueAsByte(final int id) {
        return null;
    }
    
    public boolean isCompressedStream() {
        return isCompressedStream;
    }
    
    public void setCompressedStream(final boolean isCompressedStream) {
        this.isCompressedStream=isCompressedStream;
    }
    
    /**do not cascade down whole tree*/
    public boolean ignoreRecursion() {
        return ignoreRecursion;
    }
    
    /**do not cascade down whole tree*/
    public void ignoreRecursion(final boolean ignoreRecursion) {
        this.ignoreRecursion=ignoreRecursion;
    }
    
    public byte[] getTextStreamValueAsByte(final int id) {
        return null;
    }
    
    public byte[][] getByteArray(final int id) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void setTextStreamValue(final int id2, final String value) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * used in Forms code where keys can be page numbers
     * @return
     */
    public Map getOtherDictionaries() {
        
        return Collections.unmodifiableMap(otherValues);
    }
    
    public void setCurrentKey(final Object key) {
        currentKey=key;
    }
    
    //convenience method to return array as String
    @SuppressWarnings("UnusedDeclaration")
    public String toString(final float[] floatArray, final boolean appendPageNumber) {
        
        if(floatArray==null) {
            return null;
        }
        
        final StringBuilder value=new StringBuilder();
        
        if(appendPageNumber){
            value.append(pageNumber);
            value.append(' ');
        }
        
        for (final float aFloatArray : floatArray) {
            value.append(aFloatArray);
            value.append(' ');
        }
        
        return value.toString();
    }
    
    /**
     * @return the page this field is associated to
     */
    public int getPageNumber() {
        return pageNumber;
    }
    
    /**
     * set the page number for this form
     */
    public void setPageNumber(final int number) {
        
        pageNumber = number;
    }

    public void setCache(final long offset, final PdfFileReader objReader) {
        this.startStreamOnDisk=offset;
        this.objReader=objReader;
        
    }
    
    public boolean isCached() {
        
        return startStreamOnDisk!=-1;
    }
    
    public String getCachedStreamFile(final PdfFileReader objReader){
        
        File tmpFile=null;
        
        if(startStreamOnDisk!=-1){ //cached so we need to read it
            
            try{
                
                tmpFile=File.createTempFile("jpedal-", ".bin",new File(ObjectStore.temp_dir));
                tmpFile.deleteOnExit();
                
                /**
                 * we use Length if available
                 */
                final int length=this.getInt(PdfDictionary.Length);

                if(length==-1){
                    objReader.spoolStreamDataToDisk(tmpFile,startStreamOnDisk);
                }else{
                    objReader.spoolStreamDataToDisk(tmpFile,startStreamOnDisk,length);
                }
                
                //set name for access
                cacheName=tmpFile.getAbsolutePath();
                
                //System.out.println("cached file size="+tmpFile.length()+" "+this.getObjectRefAsString());
            }catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }finally{
                //remove at end
                if(tmpFile!=null) {
                    tmpFile.deleteOnExit();
                }
            }
        }
        
        //decrypt and decompress
        if(getObjectType()!=PdfDictionary.XObject){
            objReader.readStream(this,true,true,false, getObjectType()==PdfDictionary.Metadata, isCompressedStream, cacheName);
        }
        
        return cacheName;
    }
    
    public void setInCompressedStream(final boolean isInCompressedStream) {
        
        this.isInCompressedStream=isInCompressedStream;
    }
    
    public boolean isInCompressedStream() {
        return isInCompressedStream;
    }
    
    //use in colorpsace to get ref correct
    public void maybeIndirect(final boolean b) {
        maybeIndirect=b;
    }
    
    //use in colorpsace to get ref correct
    public boolean maybeIndirect() {
        return maybeIndirect;
    }
    
    public boolean isFullyResolved() {
        return isFullyResolved;
    }
    
    public void setFullyResolved(final boolean isFullyResolved) {
        this.isFullyResolved=isFullyResolved;
    }
    
    public boolean isDataExternal() {
        return isDataExternal;
    }
    
    public void isDataExternal(final boolean isDataExternal) {
        this.isDataExternal=isDataExternal;
    }
    
    public boolean ignoreStream() {
        
        return ignoreStream;
    }
    
    public void ignoreStream(final boolean ignoreStream) {
        
        this.ignoreStream=ignoreStream;
    }
    
    public void setStreamMayBeCorrupt(final boolean streamMayBeCorrupt) {
        this.streamMayBeCorrupt=streamMayBeCorrupt;
    }
    
    public boolean streamMayBeCorrupt() {
        return streamMayBeCorrupt;
    }
    
    /**
     * move across and reset in pdfObject
     * @param pdfObject
     */
    public void moveCacheValues(final PdfObject pdfObject) {
        
        startStreamOnDisk=pdfObject.startStreamOnDisk;
        pdfObject.startStreamOnDisk=-1;
        
        this.cacheName=pdfObject.cacheName;
        pdfObject.cacheName=null;
    }

    public void setClip(final Shape clip) {
        this.clip=clip;
    }
    
    public Shape getClip() {
        return clip;
    }

    
    public byte[] getConvertedData() {
       return convertedData;
    }
    
    public void setConvertedData(byte[] convertedData) {
       this.convertedData=convertedData;
    }
}
