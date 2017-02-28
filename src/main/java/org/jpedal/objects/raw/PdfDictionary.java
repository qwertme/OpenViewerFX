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
 * PdfDictionary.java
 * ---------------
 */
package org.jpedal.objects.raw;


import org.jpedal.color.ColorSpaces;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.io.PdfFilteredReader;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

import java.lang.reflect.Field;

/**
 * holds actual data for PDF file to process.
 *
 * We have used the naming conventions from PDF not Java
 */
public class PdfDictionary {

    public static final int Unknown=-1;

    public static final int URI=2433561;

    /**
     * all key values as hashed values
     */

    public static final int A=17;

    public static final int AA=4369;

    public static final int AC=4371;

    public static final int AcroForm=661816444;

    public static final int AcroForm_FormsJSGuide=286725562;

    public static final int ActualText=1752861363;

    public static final int Adobe_PubSec=2018874538;

    public static final int AIS=1120547;

    public static final int Alternate=2054519176;

    public static final int AlternateSpace=-1247101998;

    public static final int Annot=1044266837;

    public static final int Annots=1044338049;

    public static final int AntiAlias=2055039589;

    public static final int AP=4384;

    public static final int Array=1111634266;

    public static final int ArtBox=1142050954;

    public static final int AS=4387;

    public static final int Asset=1128478037;

    public static final int Assets=1127568774;

    public static final int Ascent=859131783;

    public static final int Author=1144541319;

    public static final int AvgWidth=1249540959;

    public static final int B=18;

    public static final int BlackPoint=1886161824;

    public static final int Background=1921025959;

    public static final int Base=305218357;

    public static final int BaseEncoding=1537782955;

    public static final int BaseFont=678461817;

    public static final int BaseState=1970567530;

    public static final int BBox=303185736;

    public static final int BC=4627;

    public static final int BDC=1184787;

    public static final int BG=4631;

    public static final int BI=4633;

    public static final int BitsPerComponent=-1344207655;

    public static final int BitsPerCoordinate=-335950113;

    public static final int BitsPerFlag=1500422077;

    public static final int BitsPerSample=-1413045608;

    public static final int Bl=4668;

    public static final int BlackIs1=1297445940;

    public static final int BleedBox=1179546749;

    public static final int Blend=1010122310;

    public static final int Bounds=1161709186;

    public static final int Border=1110722433;

    public static final int BOTTOMINSET = -2102087263;

    public static final int BM=4637;

    public static final int BPC=1187859;

    public static final int BS=4643;

    public static final int Btn=1197118;

    public static final int ByteRange=2055367785;

    public static final int C=19;

    public static final int C0=4864;

    public static final int C1=4865;

    public static final int C2=4866;

    public static final int CA=4881;

    public static final int ca=13105;

    public static final int Calculate = 1835890573;

    public static final int CapHeight=1786204300;

    public static final int Category=1248888446;

    public static final int Catalog=827289723;

    public static final int Cert=322257476;

    public static final int CF=4886;

    public static final int CFM=1250845;

    public static final int Ch=4920;

    public static final int CIDSet=337856605;

    public static final int CIDSystemInfo=1972801240;

    public static final int CharProcs=2054190454;

    public static final int CharSet=1110863221;
    
    public static final int CheckSum=1314617968;

    public static final int CIDFontType0C=-1752352082;

    public static final int CIDToGIDMap=946823533;

    public static final int ClassMap=1448698499;

    public static final int CMap=320680256;

    public static final int CMapName=827223669;

    //use ColorSpaces.DeviceCMYK for general usage
    public static final int CMYK=320678171;

    public static final int CO=4895;

    public static final int Color=1060912981;
    
    public static final int Colors=1010783618;
    
    public static final int ColorBurn=1367441811;
    
    public static final int ColorDodge=2071170184;

    public static final int ColorSpace=2087749783;

    public static final int ColorTransform=-1263544861;

    public static final int Columns=1162902911;

    public static final int Components=1920898752;

    public static final int CompressedObject=23;
    
    public static final int Compatible=1987215544;

    public static final int Configurations=-1128809475;

    public static final int Configs=910980737;

    public static final int ContactInfo=1568843969;

    public static final int Contents=1216184967;

    public static final int Coords=1061308290;

    //final public static int Count=1061502551; //matches Sound so changed
    public static final int Count=1061502502;

    public static final int CreationDate=1806481572;

    public static final int Creator=827818359;

    public static final int CropBox=1076199815;

    public static final int CS=4899;

    public static final int CVMRC=639443494;

    public static final int D=20;

    public static final int DA=5137;

    public static final int DamagedRowsBeforeError=904541242;
    
    public static final int Darken=1111181679;

    public static final int DC=5139;

    public static final int DCT=1315620;

    public static final int Decode=859785322;

    public static final int DecodeParms=1888135062;

    public static final int DescendantFonts=-1547306032;

    public static final int Descent=860451719;

    public static final int Dest=339034948;

    public static final int Dests=893600855;
    
    public static final int Difference=1802796208;

    public static final int Differences=1954328750;

    public static final int Domain=1026641277;

    public static final int DP=5152;

    public static final int DR=5154;

    public static final int DS=5155;

    public static final int DV=5158;

    public static final int DW=5159;

    public static final int E=21;

    public static final int EarlyChange=1838971823;

    public static final int EF=5398;

    public static final int EFF=1381910;
    
    public static final int EOPROPtype=1684763764;

    public static final int Encode=859785587;

    public static final int EncodedByteAlign=-823077984;

    public static final int Encoding=1232564598;

    public static final int Encrypt=1113489015;

    //final public static int Encryption=2004590012;

    public static final int EncryptMetadata=-1815804199;

    public static final int EndOfBlock=1885240971;

    public static final int EndOfLine=1517116800;
    
    public static final int Exclusion=-1955824744;

    public static final int Export=1077893004;

    public static final int Extend=1144345468;

    public static final int Extends=894663815;

    public static final int ExtGState=-1938465939;

    public static final int Event=1177894489;

    public static final int F=22;

    public static final int FDF=1446934;

    public static final int Ff=5686;

    public static final int Fields=893143676;

    public static final int FileAccess=1869245103;

    public static final int FileAttachment=-1113876231;

    public static final int Filter=1011108731;

    public static final int First=960643930;

    public static final int FirstChar=1283093660;

    public static final int FirstPage=1500740239;

    public static final int Fit=1456452;

    public static final int FitB=372851730;

    public static final int FitBH=960762414;

    public static final int FitBV=960762428;

    public static final int FitH=372851736;

    public static final int FitHeight=1920684175;

    public static final int FitR=372851746;

    public static final int FitV=372851750;

    public static final int FitWidth=1332578399;

    public static final int Flags=1009858393;

    public static final int Fo=5695;

    public static final int Font=373243460;

    public static final int FontBBox=676429196;

    public static final int FontDescriptor=-1044665361;

    public static final int FontFamily=2071816377;

    public static final int FontFile=746093177;

    public static final int FontFile2=2021292334;

    public static final int FontFile3=2021292335;

    public static final int FontMatrix=-2105119560;

    public static final int FontName=879786873;

    public static final int FontStretch=2038281912;

    public static final int FontWeight=2004579768;

    public static final int Form=373244477;

    public static final int Format = 1111312259;

    public static final int FormType=982024818;

    public static final int FreeText=980909433;

    public static final int FS=5667;

    public static final int FT=5668;

    public static final int FullScreen=2121363126;

    public static final int Function=1518239089;

    public static final int Functions=2122150301;

    public static final int FunctionType=2127019430;

    public static final int G=23;

    public static final int Gamma=826096968;

    public static final int GoBack=305220218;

    public static final int GoTo=390014015;

    public static final int GoToR=1059340089;

    public static final int Group=1111442775;

    public static final int H=24;
    
    public static final int HardLight=1786342520;

    public static final int Height=959926393;

    public static final int Hide=406402101;

    public static final int Highlight=1919840408;
    
    public static final int Hue=1590581;

    public static final int hival=960901492;

    public static final int I=25;

    public static final int ID=6420;

    public static final int Identity=1567455623;

    public static final int Identity_H=2038913669;

    public static final int Identity_V=2038913683;

    public static final int IDTree=608325193;

    public static final int IF=6422;

    public static final int IM=6429;

    public static final int Image=1026635598;

    public static final int ImageMask=1516403337;

    public static final int Index=1043608929;

    //used to hold Indexed Colorspace read, not direct key in PDF
    public static final int Indexed=895578984;

    public static final int Info=423507519;

    public static final int Ink=1654331;

    public static final int InkList=475169151;

    public static final int Instances=2088139149;

    public static final int Intent=1144346498;

    public static final int InvisibleRect=-1716672299;

    public static final int IRT=1647140;
    
    public static final int IT=6436;

    public static final int ItalicAngle=2055844727;

    public static final int JavaScript=-2006286978;

    public static final int JS=6691;

    public static final int JT=6692;

    public static final int JBIG2Globals=1314558361;

    public static final int K=27;

    public static final int Keywords=1517780362;

    public static final int Keystroke = 2005434004;

    public static final int Kids=456733763;

    public static final int L=28;

    public static final int Lang=472989239;

    public static final int Last=472990532;

    public static final int LastChar=795440262;

    public static final int LastModified=1873390769;

    public static final int LastPage=1013086841;

    public static final int Launch=1161711465;

    public static final int Layer=826881374;

    public static final int Leading=878015336;

    public static final int LEFTINSET = 1937340825;

    public static final int Length=1043816557;

    public static final int Length1=929066303;

    public static final int Length2=929066304;

    public static final int Length3=929066305;
    
    public static final int Lighten=945843829;
    
    public static final int Limits=1027170428;

    public static final int Linearized=2004845231;

    public static final int LinearizedReader=-1276915978; //not in PDF, used by JPedal

    public static final int Link = 473513531;

    public static final int ListMode=964196217;

    public static final int Location=1618506351;

    public static final int Lock=473903931;

    public static final int Locked=859525491;

    public static final int Lookup=1060856191;
    
    public static final int Luminosity=-2139971891;

    public static final int LW=7207;

    public static final int M=29;

    //use StandardFonts.MacExpert as public value
    static final int MacExpertEncoding=-1159739105;

    //use StandardFonts.MAC as public value
    static final int MacRomanEncoding=-1511664170;

    public static final int MARGIN = 1110931055;

    public static final int MarkInfo=913275002;

    public static final int Mask=489767739;

    public static final int Matrix=1145198201;
    
    public static final int Matte=826557522;

    public static final int max=4010312;

    public static final int MaxLen=1209815663;

    public static final int MaxWidth=1449495647;

    public static final int MCID=487790868;

    public static final int MediaBox=1313305473;

    public static final int Metadata =1365674082;

    public static final int min=4012350;

    public static final int MissingWidth=-1884569950;

    public static final int MK=7451;

    public static final int ModDate=340689769;

    public static final int MouseDown = 1401195152;

    public static final int MouseEnter = -2088269930;

    public static final int MouseExit = 1418558614;

    public static final int MouseUp = 1129473157;

    public static final int Multiply=1451587725;

    public static final int N=30;

    public static final int Name=506543413;

    public static final int Named=826094930;

    public static final int Names=826094945;

    public static final int NeedAppearances=-1483477783;

    public static final int Next=506808388;

    public static final int NextPage=1046904697;

    public static final int NM=7709;

    public static final int None=507461173;

    public static final int Normal=1111314299;

    public static final int Nums=507854147;

    public static final int O=31;

    public static final int OC=7955;

    public static final int OCGs=521344835;

    public static final int OCProperties=-1567847737;

    public static final int OE=7957;

    public static final int OFF=2037270;

    public static final int Off=2045494;

    public static final int ON=7966;

    public static final int On=7998;

    public static final int OnBlur = 305947776;

    public static final int OnFocus = 1062372185;

    public static final int OP=7968;

    public static final int op=16192;

    public static final int Open=524301630;

    public static final int OpenAction=2037870513;

    public static final int OPI=2039833;

    public static final int OPM=2039837;

    public static final int Opt=2048068;

    public static final int Order=1110717793;

    public static final int Ordering=1635480172;

    public static final int Outlines=1485011327;
    
    public static final int Overlay=1113290622;

    public static final int P=32;

    public static final int PaintType=1434615449;

    public static final int Page=540096309;

    public static final int PageLabels=1768585381;

    public static final int PageMode=1030777706;

    public static final int Pages=825701731;

    public static final int Params=1110531444;

    public static final int Parent=1110793845;

    public static final int ParentTree=1719112618;

    public static final int Pattern=1146450818;

    public static final int PatternType=1755231159;

    public static final int PC=8211;

    //use StandardFonts.PDF as public value
    static final int PDFDocEncoding=1602998461;

    public static final int Perms=893533539;

    public static final int Pg=8247;

    public static final int PI=8217;

    public static final int PieceInfo=1383295380;

    public static final int PO=8223;

    public static final int Popup=1061176672;

    public static final int Predictor=1970893723;

    public static final int Prev=541209926;

    public static final int PrevPage=1081306235;

    public static final int Print=1111047780;

    public static final int PrintState=2104469658;

    public static final int Process=861242754;

    public static final int ProcSet=860059523;

    public static final int Producer=1702196342;

    public static final int Properties=-2089186617;

    public static final int PV=8230;

    public static final int Q=33;

    public static final int QFactor=862279027;

    public static final int QuadPoints=1785890247;

    public static final int R=34;

    public static final int Range=826160983;

    public static final int RBGroups=1633113989;

    public static final int RC=8723;

    public static final int Reason=826499443;

    public static final int Recipients=1752671921;

    public static final int Rect=573911876;

    public static final int Reference=1786013849;

    public static final int Registry=1702459778;

    public static final int ResetForm=1266841507;

    public static final int Resources=2004251818;

    //convert to DeviceRGB
    public static final int RGB=2234130;

    public static final int RichMedia=1852273008;
    
    public static final int RichMediaContent=-1263082253;

    public static final int RIGHTINSET = 1971043222;

    public static final int RD=8724;

  	public static final int Root=574570308;

    public static final int RoleMap=893350012;

    public static final int Rotate=1144088180;

    public static final int Rows=574572355;
    
    public static final int RT=8740;

    public static final int RV=8742;

    public static final int S=35;

    public static final int SA=8977;
    
    public static final int Saturation=-2004966240;

    public static final int SaveAs=1177891956;

    public static final int Screen=1110792305;

    public static final int SetOCGState=1667731612;

    public static final int Square=1160865142;

    public static final int Shading=878474856;

    public static final int ShadingType=1487255197;

    public static final int Sig=2308407;

    public static final int SigFlags=1600810585;

    public static final int Signed=926832749;

    public static final int Size=590957109;

    public static final int SM=8989;

    public static final int SMask=489767774;
    
    public static final int SoftLight=2020441219;

    public static final int Sound=1061502534;

    public static final int Stamp=1144077667;

    public static final int Standard=1467315058;

    //use StandardFonts.STD as public value
    static final int StandardEncoding=-1595087640;

    public static final int State=1144079448;

    public static final int StemH=1144339771;

    public static final int StemV=1144339785;

    public static final int StmF=591674646;

    public static final int StrF=591675926;
    
    public static final int StrickOut=2036432546;
    
    public static final int StructElem=1468107717;

    public static final int StructParent=-1732403014;

    public static final int StructParents=-1113539877;

    public static final int StructTreeRoot=-2000237823;

    public static final int Style=1145650264;

    public static final int SubFilter=-2122953826;

    public static final int Subj=591737402;

    public static final int Subject=978876534;

    public static final int SubmitForm=1216126662;

    public static final int Subtype=1147962727;

    public static final int Supplement=2104860094;

    public static final int T=36;

    public static final int Tabs=607203907;

    public static final int TagSuspect=2002295992;

    public static final int Text=607471684;

    public static final int TI=9241;

    public static final int TilingType=1619174053;

    public static final int tintTransform=-1313946392;

    public static final int Title=960773209;

    public static final int TM=9245;

    public static final int Toggle=926376052;

    public static final int TOPINSET = -2105379491;

    public static final int ToUnicode=1919185554;

    public static final int TP=9248;

    public static final int TR=9250;

    public static final int Trapped=1080325989;

    public static final int TrimBox=1026982273;

    public static final int Tx=9288;

    public static final int TxFontSize=964209857;

    public static final int TxOutline=-2074573923;

    public static final int TU=9253;

    public static final int Type=608780341;

    public static final int U=37;

    public static final int UE=9493;

    public static final int UF=9494;

    public static final int Uncompressed=-1514034520;

    public static final int Unsigned=1551661165;

    public static final int Usage=1127298906;

    public static final int V=38;

    public static final int Validate = 1516404846;
    
    public static final int VerticesPerRow = -1180057884;

    public static final int View=641283399;
    
    public static final int ViewState=2103872382;

    public static final int VP=9760;

    public static final int W=39;

    public static final int W2=9986;

    public static final int WhitePoint=2021497500;
    
    public static final int Widget=876043389;    
    
    public static final int Win=2570558;

    //use StandardFonts.WIN as public value
    static final int WinAnsiEncoding = 1524428269;

    public static final int Width=959726687;

    public static final int Widths=876896124;

    public static final int WP=10016;

    public static final int WS=10019;

    public static final int X=40;

    public static final int XFA=2627089;
    
    public static final int XFAImages=1195921064;

    public static final int XHeight=962547833;

    public static final int XObject=979194486;

    public static final int XRefStm=910911090;

    public static final int XStep=591672680;

    public static final int XYZ=2631978;

    public static final int YStep=591672681;

    public static final int Zoom=708788029;

    public static final int ZoomTo=1060982398;

    public static final int Unchanged=2087349642;

    public static final int Underline=2053993372;

    /**
     * types of Object value found
     */

    public static final int VALUE_IS_DICTIONARY = 1;

    public static final int VALUE_IS_DICTIONARY_PAIRS = 2;

    public static final int VALUE_IS_STRING_CONSTANT = 3;

    public static final int VALUE_IS_STRING_KEY = 4;

    public static final int VALUE_IS_UNREAD_DICTIONARY = 5;

    public static final int VALUE_IS_INT = 6;

    public static final int VALUE_IS_FLOAT = 7;

    public static final int VALUE_IS_BOOLEAN = 8;

    public static final int VALUE_IS_INT_ARRAY = 9;

    public static final int VALUE_IS_FLOAT_ARRAY = 10;

    public static final int VALUE_IS_BOOLEAN_ARRAY = 12;

    public static final int VALUE_IS_KEY_ARRAY = 14;

    public static final int VALUE_IS_DOUBLE_ARRAY = 16;

    public static final int VALUE_IS_MIXED_ARRAY = 18;

    public static final int VALUE_IS_STRING_ARRAY = 20;

    public static final int VALUE_IS_OBJECT_ARRAY = 22;

    public static final int VALUE_IS_TEXTSTREAM=25;

    public static final int VALUE_IS_NAME = 30;

    public static final int VALUE_IS_NAMETREE = 35;

    public static final int VALUE_IS_VARIOUS = 40;
    
    public static final int XFA_TEMPLATE=1013350773;

    public static final int XFA_DATASET=1130793076;

    public static final int XFA_CONFIG=1043741046;

    public static final int XFA_PREAMBLE=1031041382;

    public static final int XFA_LOCALESET=1951819392;

    public static final int XFA_PDFSECURITY=1701743524;

    public static final int XFA_XMPMETA=1026916721;

    public static final int XFA_XDP=172517504;
    
    public static final int XFA_XFDF=3552310;

    public static final int XFA_POSTAMBLE=2088075366;
    public static final int STANDARD=0;
    public static final int LOWERCASE=1;
    public static final int REMOVEPOSTSCRIPTPREFIX=2;
    
    /**
     * used as type in our FormAppearanceObject to make it easy to identify
     */
    public static final int XFA_APPEARANCE=129;

    /**
     * convert stream int key for dictionary entry
     */
    public static Object getKey(final int keyStart, final int keyLength, final byte[] raw) {

        //save pair and reset
        final byte[] bytes=new byte[keyLength];

        System.arraycopy(raw,keyStart,bytes,0,keyLength);

        return new String(bytes);
    }

    /**
     * convert stream int key for dictionary entry
     */
    public static int getIntKey(final int keyStart, final int keyLength, final byte[] raw) {

        /**
        
        byte[] a=StringUtils.toBytes("IRT");

        keyStart=0;
        keyLength=a.length;
        raw=a;
        //PdfObject.debug=true;

    	byte[] bytes=new byte[keyLength];

        System.arraycopy(raw,keyStart,bytes,0,keyLength);

        System.out.println("public static final int "+new String(bytes)+"="+generateChecksum(keyStart, keyLength, raw)+";");
        if(1==1)
                 throw new RuntimeException("xx");
         /**/

        //get key
        final int id = generateChecksum(keyStart, keyLength, raw);
        int PDFkey=id;// standard setting is to use value

        /**
         * non-standard values
         */
        switch(id){

            case BPC:
                PDFkey=BitsPerComponent;
                break;

            case CMYK:
            	PDFkey=ColorSpaces.DeviceCMYK;
            	break;

            case CS:
                PDFkey=ColorSpace;
                break;

            case DCT:
            	return PdfFilteredReader.DCTDecode;

            case DP:
                PDFkey=DecodeParms;
                break;

            case PdfFilteredReader.Fl:
                PDFkey=PdfFilteredReader.FlateDecode;
                break;

            case IM:
                PDFkey=ImageMask;
                break;

            case I:
                PDFkey=Indexed;
                break;

            case MacExpertEncoding:
            	PDFkey=StandardFonts.MACEXPERT;
            	break;

            case MacRomanEncoding:
                PDFkey=StandardFonts.MAC;
                break;

            case Params:
                PDFkey=DecodeParms;
                break;

            case PDFDocEncoding:
                PDFkey=StandardFonts.PDF;
                break;

            case RGB:
            	PDFkey=ColorSpaces.DeviceRGB;
            	break;

            case StandardEncoding:
                PDFkey=StandardFonts.STD;
                break;

            case WinAnsiEncoding:
                PDFkey=StandardFonts.WIN;
                break;
        }

        return PDFkey;
    }

	public static int generateChecksum(final int keyStart, final int keyLength, final byte[] raw) {
		//convert token to unique key
        int id=0,x=0,next;

        for(int i2=keyLength-1;i2>-1;i2--){
            next=raw[keyStart+i2];

            next -= 48;

            id += ((next)<<x);

            x += 8;
        }
        
        //Check for known incorrect id values due to first character lowercase when should be uppercase
        if (id == 1145651253) {
            id = 0;
            x = 0;

            for (int i2 = keyLength - 1; i2 > -1; i2--) {
                next = raw[keyStart + i2];
                if (i2 == 0) {
                    if (96 < next && next < 123) {
                        next -= 32;
                    }
                }
                next -= 48;

                id += ((next) << x);

                x += 8;
            }
        }

        if(id==1061502551 || id==-2006286936){ //its a duplicate so different formula:-(

           // System.out.println((char)raw[keyStart]+" "+(char)raw[keyStart+keyLength-1]);
            //System.out.println((int)raw[keyStart]+" "+(int)raw[keyStart+keyLength-1]);

            id=id+raw[keyStart]-raw[keyStart+keyLength-1];
        }
        return id;
	}

    /**
     * get type of object
     */

    @SuppressWarnings("OverlyLongMethod")
    public static int getKeyType(final int id, final int type) {

        final int PDFkey=-1;


        switch(id){

        	case A:
                if(type==PdfDictionary.Linearized) {
                    return VALUE_IS_INT;
                } else {
                    return VALUE_IS_DICTIONARY;
                }

        	case AA:
                return VALUE_IS_UNREAD_DICTIONARY;

        	case AC:
        		return VALUE_IS_TEXTSTREAM;

            case AcroForm:
                return VALUE_IS_UNREAD_DICTIONARY;

            case ActualText:
                return VALUE_IS_TEXTSTREAM;

            case Alternate:
	        	return VALUE_IS_STRING_CONSTANT;

        	case AIS:
        		return VALUE_IS_BOOLEAN;

            case Annots:
                return VALUE_IS_KEY_ARRAY;

            case AntiAlias:
                return VALUE_IS_BOOLEAN;

            case AP:
            	return VALUE_IS_DICTIONARY;

            case Array:
                return VALUE_IS_FLOAT_ARRAY;

            case ArtBox:
                return VALUE_IS_FLOAT_ARRAY;

            case AS:
                if(type==PdfDictionary.OCProperties) {
                    return VALUE_IS_KEY_ARRAY;
                } else {
                    return VALUE_IS_NAME;
                }

            case Ascent:
                return VALUE_IS_FLOAT;

            case Asset:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Assets:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Author:
                return VALUE_IS_TEXTSTREAM;

            case B:
                if(type==PdfDictionary.Sound || type==PdfDictionary.Linearized || type==PdfDictionary.MCID) {
                    return VALUE_IS_INT;
                }
                //else
                  //  return PDFkey;
                break;

            case Background:
                return VALUE_IS_FLOAT_ARRAY;

            case Base:
                return VALUE_IS_TEXTSTREAM;

            case BaseEncoding:
                return VALUE_IS_STRING_CONSTANT;

            case BaseFont:
                return VALUE_IS_NAME;

            case BaseState:
                return VALUE_IS_NAME;

            case BBox:
                return VALUE_IS_FLOAT_ARRAY;

            case BC:
            	return VALUE_IS_FLOAT_ARRAY;

            case BG:
            	if(type==PdfDictionary.MK) {
                    return VALUE_IS_FLOAT_ARRAY;
                } else {
                    return VALUE_IS_UNREAD_DICTIONARY;
                }

            case BI:
            	return PdfDictionary.VALUE_IS_DICTIONARY;

            case BitsPerComponent:
                    return VALUE_IS_INT;

            case BitsPerCoordinate:
                    return VALUE_IS_INT;

            case BitsPerFlag:
                    return VALUE_IS_INT;

            case BitsPerSample:
            	return VALUE_IS_INT;

            case Bl:
            	return VALUE_IS_DICTIONARY;

            case BlackIs1:
            	return VALUE_IS_BOOLEAN;

            case BlackPoint:
                return VALUE_IS_FLOAT_ARRAY;

            case BleedBox:
                return VALUE_IS_FLOAT_ARRAY;

            case Blend:
                return VALUE_IS_INT;

            case Border:
                return VALUE_IS_MIXED_ARRAY;

            case Bounds:
                return VALUE_IS_FLOAT_ARRAY;

            case BM:
                return VALUE_IS_MIXED_ARRAY;

            case BS:
            	return VALUE_IS_DICTIONARY;

            case ByteRange:
                return VALUE_IS_INT_ARRAY;

            case C:
            	if(type==PdfDictionary.Form || type==PdfDictionary.MCID) {
                    return PdfDictionary.VALUE_IS_VARIOUS;
                } else if(type==PdfDictionary.Sound || type==PdfDictionary.Linearized) {
                    return PdfDictionary.VALUE_IS_INT;
                } else {
                    return VALUE_IS_FLOAT_ARRAY;
                }

            case C0:
                return VALUE_IS_FLOAT_ARRAY;

            case C1:
            	//if(type==PdfDictionary.Form)
            		//return PdfDictionary.VALUE_IS_DICTIONARY;
            	//else
            		return VALUE_IS_FLOAT_ARRAY;

            //case C2:
            	//if(type==PdfDictionary.Form)
            	//	return PdfDictionary.VALUE_IS_DICTIONARY;

            case CA:
            	if(type==Form || type==PdfDictionary.MK) {
                    return VALUE_IS_VARIOUS;
                } else {
                    return VALUE_IS_FLOAT;
                }

            case ca:
                return VALUE_IS_FLOAT;

            case Category:
                if(type==PdfDictionary.OCProperties) {
                    return VALUE_IS_KEY_ARRAY;
                }
                break;

            case Cert:
                return VALUE_IS_VARIOUS;

            case CF:
            	return VALUE_IS_DICTIONARY_PAIRS;

            case CFM:
                return VALUE_IS_NAME;

            case CharProcs:
                return VALUE_IS_DICTIONARY_PAIRS;

            case CharSet:
                return VALUE_IS_TEXTSTREAM;
                
            case CheckSum:
                return VALUE_IS_TEXTSTREAM;

            case CIDSet:
                return VALUE_IS_DICTIONARY;

            case ClassMap:
                return VALUE_IS_DICTIONARY;

            case CMapName:
                return VALUE_IS_NAME;

            case CO:
                return VALUE_IS_OBJECT_ARRAY;

            case Colors:
                return VALUE_IS_INT;

            case ColorTransform:
                return VALUE_IS_INT;

            case ColorSpace:
                if(type==XObject) {
                    return VALUE_IS_UNREAD_DICTIONARY;
                } else {
                    return VALUE_IS_UNREAD_DICTIONARY;
                }

            case Columns:
                return VALUE_IS_INT;

            case Configs:
                return VALUE_IS_KEY_ARRAY;

            case Configurations:
                return VALUE_IS_UNREAD_DICTIONARY;

            case ContactInfo:
                return VALUE_IS_TEXTSTREAM;

            case Contents:
            	if(type==Form) {
                    return VALUE_IS_VARIOUS;
                } else {
                    return VALUE_IS_KEY_ARRAY;
                }

            case Coords:
                return VALUE_IS_FLOAT_ARRAY;

            case Count:
                return VALUE_IS_INT;

            case CreationDate:
            	return VALUE_IS_TEXTSTREAM;

            case Creator:
                return VALUE_IS_TEXTSTREAM;

            case CropBox:
                return VALUE_IS_FLOAT_ARRAY;

            case CIDSystemInfo:
                return VALUE_IS_DICTIONARY;

            case CIDToGIDMap:
                return VALUE_IS_DICTIONARY;

            case CVMRC:
            	return VALUE_IS_STRING_CONSTANT;

            case D:
                if(type==PdfDictionary.MCID) {
                    return VALUE_IS_INT;
                } else
            	//if(type==PdfDictionary.OCProperties || type==PdfDictionary.Form)
                {
                    return PdfDictionary.VALUE_IS_DICTIONARY;
                }

            case DA:
//                if(type==Form)
//                    return VALUE_IS_DICTIONARY;
//                else
            	return VALUE_IS_TEXTSTREAM;

            case DamagedRowsBeforeError:
                return VALUE_IS_INT;

            case DC:
            	//if(type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;

            case Decode:
                return VALUE_IS_FLOAT_ARRAY;

            case DecodeParms:
                return VALUE_IS_DICTIONARY;

            case Descent:
                return VALUE_IS_FLOAT;

            case DescendantFonts:
                return VALUE_IS_DICTIONARY;

            case Dest:
                return VALUE_IS_MIXED_ARRAY;

            case Dests:
                return VALUE_IS_DICTIONARY;

            case Differences:
                return VALUE_IS_MIXED_ARRAY;

            case Domain:
                return VALUE_IS_FLOAT_ARRAY;

            case DP:
            	//if(type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;

            case DR:
                return VALUE_IS_UNREAD_DICTIONARY;

            case DV:
            	return VALUE_IS_VARIOUS;

            case DS:
            	if(type==PdfDictionary.Form) {
                    return VALUE_IS_VARIOUS;//TEXTSTREAM;
                } else {
                    return VALUE_IS_TEXTSTREAM;
                }

            case DW:
                return VALUE_IS_INT;

            case E:

                if(type==Linearized) {
                    return VALUE_IS_INT;
                } else
            	//if(type==PdfDictionary.OCProperties || type==PdfDictionary.Form)
                {
                    return PdfDictionary.VALUE_IS_VARIOUS;
                }

            case EF:
            	return VALUE_IS_UNREAD_DICTIONARY;


            case EarlyChange:
                return VALUE_IS_INT;

            case EncodedByteAlign:
                return VALUE_IS_BOOLEAN;

            case Encode:
                return VALUE_IS_FLOAT_ARRAY;

            case Encoding:
                return VALUE_IS_DICTIONARY;

            case Encrypt:
                return VALUE_IS_UNREAD_DICTIONARY;

            case EncryptMetadata:
            	return VALUE_IS_BOOLEAN;

            case EndOfBlock:
                return VALUE_IS_BOOLEAN;

            case EndOfLine:
                return VALUE_IS_BOOLEAN;
                
            case EOPROPtype:
            	return VALUE_IS_TEXTSTREAM;

            case Event:
            	return VALUE_IS_STRING_CONSTANT;

            case Extend:
                return VALUE_IS_BOOLEAN_ARRAY;

            case Extends:
                return VALUE_IS_DICTIONARY;

            case ExtGState:
                return VALUE_IS_UNREAD_DICTIONARY;

            case F:

            	if (type==PdfDictionary.Form || type==PdfDictionary.Outlines ||type==PdfDictionary.FS || type==PdfDictionary.Names) {
                    return PdfDictionary.VALUE_IS_VARIOUS;
                } else if (type==PdfDictionary.FDF) {
                    return PdfDictionary.VALUE_IS_TEXTSTREAM;
                } else {
                    return PdfDictionary.VALUE_IS_INT;
                }

            case Ff:
            	return VALUE_IS_INT;

            case Fields:
            	if(type==PdfDictionary.FDF) {
                    return VALUE_IS_KEY_ARRAY;
                } else {
                    return VALUE_IS_MIXED_ARRAY;
                }

            case Filter:
                return VALUE_IS_MIXED_ARRAY;

            case First:
            	if (type==PdfDictionary.FS) {
                    return VALUE_IS_VARIOUS;
                } else if (type==PdfDictionary.Outlines) {
                    return VALUE_IS_UNREAD_DICTIONARY;
                } else {
                    return VALUE_IS_INT;
                }

            case FirstChar:
                return VALUE_IS_INT;

            case Flags:
                return VALUE_IS_INT;

            case Fo:
            	return PdfDictionary.VALUE_IS_DICTIONARY;

            case Font:
                return VALUE_IS_UNREAD_DICTIONARY;

            case FontBBox:
                return VALUE_IS_FLOAT_ARRAY;

            case FontDescriptor:
                return VALUE_IS_DICTIONARY;

            case FontFile:
                return VALUE_IS_DICTIONARY;

            case FontFile2:
                return VALUE_IS_DICTIONARY;

            case FontFile3:
                return VALUE_IS_DICTIONARY;

            case FontMatrix:
                return VALUE_IS_DOUBLE_ARRAY;

            case FontName:
                return VALUE_IS_NAME;

            case FontStretch:
            	return VALUE_IS_NAME;

            case FormType:
                return VALUE_IS_INT;

            case FS:
                return VALUE_IS_VARIOUS;
            	//return VALUE_IS_DICTIONARY;

            case FT:
            	return VALUE_IS_NAME;

            case Function:
                return VALUE_IS_DICTIONARY;

            case Functions:
                return VALUE_IS_KEY_ARRAY;

            case FunctionType:
                return VALUE_IS_INT;

            case G:
                if(type==XObject) {
                    return VALUE_IS_UNREAD_DICTIONARY;
                } else {
                    return PDFkey;
                }

            case Gamma:
                return VALUE_IS_FLOAT_ARRAY;

            case Group:
                return VALUE_IS_UNREAD_DICTIONARY;

            case H:

                if(type==Linearized) {
                    return VALUE_IS_INT_ARRAY;
                } else if(type==Form) {
                    return VALUE_IS_VARIOUS;
                } else if(type==Outlines) {
                    return VALUE_IS_BOOLEAN;
                } else {
                    return PDFkey;
                }

            case Height:
                return VALUE_IS_INT;

            case I:

                if(type==PdfDictionary.Form) {
                    return VALUE_IS_INT_ARRAY;
                } else if(type==PdfDictionary.MK) {
                    return VALUE_IS_UNREAD_DICTIONARY;
                } else if(type==PdfDictionary.Page || type==PdfDictionary.Group) {
                    return VALUE_IS_BOOLEAN;
                } else if(type==PdfDictionary.Linearized) {
                    return VALUE_IS_INT;
                } else if(1==2) //need to find type
                {
                    return VALUE_IS_DICTIONARY;
                } else {
                    return VALUE_IS_BOOLEAN;
                }

            case ID:
                if(type==PdfDictionary.MCID) {
                    return VALUE_IS_INT;
                } else if(type==PdfDictionary.CompressedObject) {
                    return VALUE_IS_STRING_ARRAY;
                } else {
                    return PDFkey;
                }

            case IF:
            	return VALUE_IS_UNREAD_DICTIONARY;

			case IDTree:
            	return VALUE_IS_NAMETREE;

            case Index:
            	return VALUE_IS_INT_ARRAY;

            case Info:
                //if(type==Encrypt)
                return VALUE_IS_UNREAD_DICTIONARY;

            case InkList:
                return VALUE_IS_OBJECT_ARRAY;

            case ImageMask:
            	return VALUE_IS_BOOLEAN;

            case Intent:
            	return VALUE_IS_NAME;
            
            case IRT:
                return VALUE_IS_UNREAD_DICTIONARY;

            case IT:
            	return VALUE_IS_NAME;

            case InvisibleRect:
            	return VALUE_IS_STRING_CONSTANT;

            case JavaScript:
                	return VALUE_IS_DICTIONARY;

            case JS:
                return VALUE_IS_VARIOUS;

            case K:
                if(type==PdfDictionary.Group) {
                    return VALUE_IS_BOOLEAN;
                } else if(type==XObject) {
                    return VALUE_IS_VARIOUS;
                } else if(type==Form) {
                    return VALUE_IS_VARIOUS;//KEY_ARRAY;
                } else if(type==MCID) {
                    return VALUE_IS_VARIOUS;//KEY_ARRAY;
                } else if(type==OCProperties) {
                    return VALUE_IS_VARIOUS;//KEY_ARRAY;
                } else {
                    return VALUE_IS_INT;
                }

            case Keywords:
                return VALUE_IS_TEXTSTREAM;

            case Kids:
                return VALUE_IS_KEY_ARRAY;

            case JBIG2Globals:
                return VALUE_IS_DICTIONARY;

            case JT:
                return VALUE_IS_UNREAD_DICTIONARY;

            case L:
                if(type==Linearized) {
                    return VALUE_IS_INT;
                } else {
                    return PDFkey;
                }

            case Lang:
                if(type==PdfDictionary.MCID || type==PdfDictionary.Page) {
                    return VALUE_IS_TEXTSTREAM;
                } else {
                    return PDFkey;
                }

            case Last:
                return VALUE_IS_UNREAD_DICTIONARY;

            case LastChar:
                return VALUE_IS_INT;

            case LastModified:
                return VALUE_IS_TEXTSTREAM;

            case Layer:
                if(type==Form){
                    return VALUE_IS_INT;
                }else{
                    return VALUE_IS_DICTIONARY;
                }

            case Length:
                return VALUE_IS_INT;

            case Length1:
                return VALUE_IS_INT;

            case Length2:
                return VALUE_IS_INT;

            case Length3:
                return VALUE_IS_INT;

            case Limits:
                return VALUE_IS_STRING_ARRAY;
                    
            case Linearized:
                if(type==Linearized) {
                    return VALUE_IS_FLOAT;
                } else {
                    return PDFkey;
                }

            case Location:
                return VALUE_IS_TEXTSTREAM;

            case Lock:
            	return VALUE_IS_UNREAD_DICTIONARY;

            case Locked:
            	return VALUE_IS_KEY_ARRAY;

            case LW:
                return VALUE_IS_FLOAT;

            case M:
            	if(type==Form) {
                    return VALUE_IS_VARIOUS;
                } else if(type==PdfDictionary.MCID) {
                    return VALUE_IS_INT;
                } else if(type==Sig) {
                    return VALUE_IS_TEXTSTREAM;
                } else {
                    return VALUE_IS_TEXTSTREAM;
                }

            case MarkInfo:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Mask:
                return VALUE_IS_DICTIONARY;

            case Matrix:
                return VALUE_IS_FLOAT_ARRAY;
            
            case Matte:
                return VALUE_IS_FLOAT_ARRAY;

            case max:
                return VALUE_IS_FLOAT;

            case MaxLen:
            	return VALUE_IS_INT;

			case MCID:
            	return VALUE_IS_INT;

            case MediaBox:
            	return VALUE_IS_FLOAT_ARRAY;

            case Metadata:
                return VALUE_IS_UNREAD_DICTIONARY;

            case MissingWidth:
                return VALUE_IS_INT;

            case MK:
                return VALUE_IS_DICTIONARY;

            case ModDate:
                return VALUE_IS_TEXTSTREAM;

            case N:
                if(type==Linearized) {
                    return VALUE_IS_INT;
                } else if(type==PdfDictionary.CompressedObject) {
                    return VALUE_IS_INT;
                } else if(type==PdfDictionary.Form || type==PdfDictionary.MK) {
                    return VALUE_IS_VARIOUS;
                }else if(type==Shading) {
                    return VALUE_IS_FLOAT;
                } else {
                    return VALUE_IS_NAME;
                }

            case Name:
                if(type==PdfDictionary.Form){
                    return VALUE_IS_TEXTSTREAM;
                }else{
                    return VALUE_IS_NAME;
                }
            
            case Names:
                if(type==PdfDictionary.Names || type==PdfDictionary.FS) {
                    return VALUE_IS_MIXED_ARRAY;
                } else {
                    return VALUE_IS_UNREAD_DICTIONARY;
                }

            case NeedAppearances:
            	return VALUE_IS_BOOLEAN;

            case Next:
                if(type==PdfDictionary.Form) {
                    return VALUE_IS_DICTIONARY;
                } else {
                    return VALUE_IS_UNREAD_DICTIONARY;
                }

            case NM:
            	return VALUE_IS_TEXTSTREAM;

            case Nums:
                return VALUE_IS_KEY_ARRAY;

            case min:
                return VALUE_IS_FLOAT;

            case O:
                if(type==Linearized || type==CompressedObject) {
                    return VALUE_IS_INT;
                } else if(type==Form) {
                    return VALUE_IS_DICTIONARY;
                } else {
                    return VALUE_IS_TEXTSTREAM;
                }

            case OC:
                if(type==Form) {
                    return VALUE_IS_VARIOUS;
                } else if(type==XObject) {
                    return VALUE_IS_DICTIONARY;
                } else {
                    return VALUE_IS_NAME;
                }

            case OCGs:
                return VALUE_IS_VARIOUS;
                
            case OCProperties:
                return VALUE_IS_UNREAD_DICTIONARY;

            case OE:
                return VALUE_IS_TEXTSTREAM;

            case OFF:
            	return VALUE_IS_KEY_ARRAY;

            case Off:
            	return VALUE_IS_UNREAD_DICTIONARY;

            case ON:
            	return VALUE_IS_KEY_ARRAY;

            case On:
            	return VALUE_IS_UNREAD_DICTIONARY;

            case OP:
                if(type==Form) {
                    return VALUE_IS_VARIOUS;
                } else {
                    return VALUE_IS_BOOLEAN;
                }

            case op:
            	return VALUE_IS_BOOLEAN;

            case Open:
            	return VALUE_IS_BOOLEAN;

            case OpenAction:
                return VALUE_IS_VARIOUS;

            case OPI:
            	return VALUE_IS_DICTIONARY;

            case OPM:
            	return VALUE_IS_FLOAT;

            //breaks /PDFdata/baseline_screens/shading/Lggningsanvisningar01.pdf
            //case Order:
            //	return VALUE_IS_KEY_ARRAY;

            case Opt:
            	return VALUE_IS_OBJECT_ARRAY;

            case Ordering:
                return VALUE_IS_TEXTSTREAM;

            case Outlines:
                return VALUE_IS_UNREAD_DICTIONARY;

            case P:
                if(type==Form || type==MCID || type==FS || type==MK) {
                    return VALUE_IS_UNREAD_DICTIONARY;
                } else if(type==PdfDictionary.Metadata) {
                    return VALUE_IS_DICTIONARY;
                } else {
                    return VALUE_IS_INT;
                }

            case PageLabels:
                return VALUE_IS_UNREAD_DICTIONARY;

            case PageMode:
	        	return VALUE_IS_STRING_CONSTANT;

            case Pages:
                return VALUE_IS_DICTIONARY;

            case PaintType:
                return VALUE_IS_INT;

            case ParentTree:
            	return VALUE_IS_DICTIONARY;

            case Pattern:
                return VALUE_IS_DICTIONARY;

            case PatternType:
                return VALUE_IS_INT;

            case Parent:
                return VALUE_IS_STRING_KEY;

            case PC:
            	return PdfDictionary.VALUE_IS_DICTIONARY;

            case Perms:
                if(type==Page){
                    return VALUE_IS_DICTIONARY;
                }else{
                    return VALUE_IS_VARIOUS;
                }
            
            case Prev:
                if(type==PdfDictionary.Outlines) {
                    return VALUE_IS_UNREAD_DICTIONARY;
                } else {
                    return VALUE_IS_INT;
                }

            case Pg:
                return VALUE_IS_UNREAD_DICTIONARY;

            case PI:
            	return PdfDictionary.VALUE_IS_DICTIONARY;

            case PieceInfo:
            	return PdfDictionary.VALUE_IS_UNREAD_DICTIONARY;

            case PO:
            	return PdfDictionary.VALUE_IS_DICTIONARY;

            case Process:
                return VALUE_IS_DICTIONARY;

            case Popup:
            	return VALUE_IS_UNREAD_DICTIONARY;

            case Predictor:
                return VALUE_IS_INT;

            case Print:
                return VALUE_IS_DICTIONARY;

            case PrintState:
                return VALUE_IS_STRING_CONSTANT;

            case ProcSet:
                return VALUE_IS_MIXED_ARRAY;

            case Producer:
                return VALUE_IS_TEXTSTREAM;

            case Properties:
                return VALUE_IS_DICTIONARY_PAIRS;

            case PV:
            	return PdfDictionary.VALUE_IS_DICTIONARY;

            case Q:
            	return VALUE_IS_INT;

            case QFactor:
                 return VALUE_IS_INT;

            case QuadPoints:
            	return VALUE_IS_FLOAT_ARRAY;

            case R:
            	if(type==Form || type==PdfDictionary.MK) {
                    return VALUE_IS_VARIOUS;
                } else {
                    return VALUE_IS_INT;
                }

            case Range:
                return VALUE_IS_FLOAT_ARRAY;

            case RBGroups:
            	return VALUE_IS_KEY_ARRAY;

            case RC:
            	return VALUE_IS_TEXTSTREAM;

            case RD:
            	return VALUE_IS_FLOAT_ARRAY;

            case Reason:
                return VALUE_IS_TEXTSTREAM;

            case Recipients:
                return VALUE_IS_STRING_ARRAY;

            case Reference:
                return VALUE_IS_OBJECT_ARRAY;

            case Registry:
                return VALUE_IS_TEXTSTREAM;

            case RichMediaContent:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Resources:
                return VALUE_IS_UNREAD_DICTIONARY;

            case RoleMap:
                return VALUE_IS_DICTIONARY;

            case Rotate:
                return VALUE_IS_INT;

            case Rect:
            	return  VALUE_IS_FLOAT_ARRAY;

            case Root:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Rows:
                return VALUE_IS_INT;

           case RT:
                return VALUE_IS_NAME;
                    
            case RV:
                return VALUE_IS_TEXTSTREAM;

            case Shading:
                return VALUE_IS_DICTIONARY;

            case S:
                if(type==PdfDictionary.Linearized) {
                    return VALUE_IS_INT;
                } else {
                    return VALUE_IS_NAME;
                }

            case SA:
                return VALUE_IS_BOOLEAN;

            case ShadingType:
                return VALUE_IS_INT;

            case SigFlags:
                return VALUE_IS_INT;

            case Size:
            	if(type==PdfDictionary.CompressedObject || type==PdfDictionary.Unknown) {
                    return PdfDictionary.VALUE_IS_INT;
                } else {
                    return VALUE_IS_INT_ARRAY;
                }

            case SMask:
                return VALUE_IS_DICTIONARY;

            case Sig:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Sound:
                return VALUE_IS_UNREAD_DICTIONARY;

            case State:
                return VALUE_IS_MIXED_ARRAY;

            case StemV:
                return VALUE_IS_INT;

            case PdfDictionary.StmF:
                return VALUE_IS_NAME;

            case PdfDictionary.StrF:
                return VALUE_IS_NAME;

            case StructParent:
            	return VALUE_IS_INT;

            case StructParents:
            	return VALUE_IS_INT;

            case StructTreeRoot:
            	return VALUE_IS_UNREAD_DICTIONARY;

            case Style:
            	if(type==Font) {
                    return VALUE_IS_DICTIONARY;
                } else {
                    return VALUE_IS_TEXTSTREAM;
                }

            case SubFilter:
                return VALUE_IS_NAME;

            case Subj:
            	return VALUE_IS_TEXTSTREAM;

            case Subject:
                return VALUE_IS_TEXTSTREAM;

            case Subtype:
                return VALUE_IS_STRING_CONSTANT;

            case Supplement:
                return VALUE_IS_INT;

            case T:
                if(type==Form || type==MCID || type==MK) {
                    return VALUE_IS_TEXTSTREAM;
                } else {
                    return VALUE_IS_INT;
                }

            case Tabs:
                return VALUE_IS_NAME;

            case TagSuspect:
	        	return VALUE_IS_STRING_CONSTANT;


            case TI:
        		return VALUE_IS_INT;

            case TP:
        		return VALUE_IS_INT;

            case TilingType:
            	return VALUE_IS_INT;

            case Title:
                return VALUE_IS_TEXTSTREAM;

            case TM:
                return VALUE_IS_TEXTSTREAM;

            case ToUnicode:
                return VALUE_IS_DICTIONARY;

            case TR:
                return VALUE_IS_DICTIONARY;

            case Trapped:
                return VALUE_IS_NAME;

            case TrimBox:
            	return VALUE_IS_FLOAT_ARRAY;

            case TU:
                return VALUE_IS_TEXTSTREAM;

            case TxOutline:
                return VALUE_IS_BOOLEAN;

            case TxFontSize:
                return VALUE_IS_FLOAT;

            case Type:
                return VALUE_IS_STRING_CONSTANT;

            case U:
            	if(type==PdfDictionary.Form) {
                    return PdfDictionary.VALUE_IS_DICTIONARY;
                } else {
                    return VALUE_IS_TEXTSTREAM;
                }

            case UE:
                return VALUE_IS_TEXTSTREAM;

            case UF:
            	return VALUE_IS_VARIOUS;

            case Uncompressed:
                return VALUE_IS_BOOLEAN;

            case URI:
                return VALUE_IS_TEXTSTREAM;

            case Usage:
                return VALUE_IS_DICTIONARY;

            case V:
            	if(type==PdfDictionary.Form) {
                    return VALUE_IS_VARIOUS;
                } else {
                    return VALUE_IS_INT;
                }
            
            case View:
                    return VALUE_IS_DICTIONARY;
                
            case ViewState:
                return VALUE_IS_NAME; 
                
            case VerticesPerRow:
                    return VALUE_IS_INT;

           case VP:
                return VALUE_IS_OBJECT_ARRAY;

                //hack as odd structure
            case W:
            	if(type==PdfDictionary.CompressedObject) // int not int array
                {
                    return PdfDictionary.VALUE_IS_INT_ARRAY;
                } else if(type==PdfDictionary.Form) {
                    return VALUE_IS_VARIOUS;
                } else if(type==PdfDictionary.MCID) {
                    return VALUE_IS_FLOAT;
                } else {
                    return VALUE_IS_TEXTSTREAM;
                }

            case W2:
                return VALUE_IS_TEXTSTREAM;

            case Win:
        		return VALUE_IS_DICTIONARY;

            case WhitePoint:
                return VALUE_IS_FLOAT_ARRAY;

            case Width:
                return VALUE_IS_INT;

            case Widths:
                return VALUE_IS_FLOAT_ARRAY;

            case WP:
            	//if(type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;

            case WS:
                //if(type==PdfDictionary.Form)
                return PdfDictionary.VALUE_IS_DICTIONARY;
                
            case X:
                //if(type==PdfDictionary.OCProperties || type==PdfDictionary.Form)
                return PdfDictionary.VALUE_IS_DICTIONARY;
                
            case XFA:
                return VALUE_IS_VARIOUS;
                
            case XFAImages:
                return VALUE_IS_DICTIONARY;
                
            case XObject:
                return VALUE_IS_UNREAD_DICTIONARY;
                
            case XRefStm:
                return VALUE_IS_INT;
                
            case XStep:
            	return VALUE_IS_FLOAT;

            case YStep:
            	return VALUE_IS_FLOAT;

            case Zoom:
            	return VALUE_IS_DICTIONARY;

            default:

                if(PdfObject.debug){
                    System.out.println("No type value set for "+id+" getKeyType(int id) in PdfDictionay");

                }

                break;

        }


        return PDFkey;
    }


    /**
     * use reflection to show actual Constant for Key or return null if no value
     * @param parameterConstant
     * @return String or null
     */
    public static String showAsConstant(final int parameterConstant) {

    	final Field[] ts = PdfDictionary.class.getFields();
    	int count=ts.length;
    	String type=null;

    	for(int ii=0;ii<count;ii++){
    		try{
    			//if(ts[ii] instanceof Integer){
    				final int t=ts[ii].getInt(new PdfDictionary());

    				if(t==parameterConstant){
    					type="PdfDictionary."+ts[ii].getName();
    					count=ii;
    				}
    			//}
    		}catch(final Exception e){
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
    		}
    	}

    	return type;
    }

    /**
     * used in debugging
     * @param type
     * @return String representation of type
     */
    public static String showArrayType(final int type) {

        switch(type){
            case VALUE_IS_INT_ARRAY:
                return "VALUE_IS_INT_ARRAY";

            case VALUE_IS_BOOLEAN_ARRAY:
                return "VALUE_IS_BOOLEAN_ARRAY";

            case VALUE_IS_KEY_ARRAY:
                return "VALUE_IS_KEY_ARRAY";

            case VALUE_IS_DOUBLE_ARRAY:
                return "VALUE_IS_DOUBLE_ARRAY";

            case VALUE_IS_MIXED_ARRAY:
                return "VALUE_IS_MIXED_ARRAY";

            case VALUE_IS_STRING_ARRAY:
                return "VALUE_IS_STRING_ARRAY";

            case VALUE_IS_OBJECT_ARRAY:
                return "VALUE_IS_OBJECT_ARRAY";

            default:
                return "not set";
        }
    }

	/** converts a string into an individual checksum int*/
    public static int stringToInt(final String value){
    	final byte[] bytes = StringUtils.toBytes(value);
		return PdfDictionary.generateChecksum(0, bytes.length, bytes);
    }

}