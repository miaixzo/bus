/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.image.galaxy.dict.SPI_P_Release_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SPI-P Release 1";

    /** (0009,xx00) VR=LT VM=1 Data Object Recognition Code */
    public static final int DataObjectRecognitionCode = 0x00090000;

    /** (0009,xx04) VR=LO VM=1 Image Data Consistency */
    public static final int ImageDataConsistency = 0x00090004;

    /** (0009,xx08) VR=US VM=1 ? */
    public static final int _0009_xx08_ = 0x00090008;

    /** (0009,xx10) VR=LO VM=1 ? */
    public static final int _0009_xx10_ = 0x00090010;

    /** (0009,xx12) VR=LO VM=1 ? */
    public static final int _0009_xx12_ = 0x00090012;

    /** (0009,xx15) VR=LO VM=1 Unique Identifier */
    public static final int UniqueIdentifier = 0x00090015;

    /** (0009,xx16) VR=LO VM=1 ? */
    public static final int _0009_xx16_ = 0x00090016;

    /** (0009,xx18) VR=LO VM=1 ? */
    public static final int _0009_xx18_ = 0x00090018;

    /** (0009,xx21) VR=LT VM=1 ? */
    public static final int _0009_xx21_ = 0x00090021;

    /** (0009,xx31) VR=LT VM=1 PACS Unique Identifier */
    public static final int PACSUniqueIdentifier = 0x00090031;

    /** (0009,xx34) VR=LT VM=1 Cluster Unique Identifier */
    public static final int ClusterUniqueIdentifier = 0x00090034;

    /** (0009,xx38) VR=LT VM=1 System Unique Identifier */
    public static final int SystemUniqueIdentifier = 0x00090038;

    /** (0009,xx39) VR=LT VM=1 ? */
    public static final int _0009_xx39_ = 0x00090039;

    /** (0009,xx51) VR=LT VM=1 Study Unique Identifier */
    public static final int StudyUniqueIdentifier = 0x00090051;

    /** (0009,xx61) VR=LT VM=1 Series Unique Identifier */
    public static final int SeriesUniqueIdentifier = 0x00090061;

    /** (0009,xx91) VR=LT VM=1 ? */
    public static final int _0009_xx91_ = 0x00090091;

    /** (0009,xxA0) VR=UN VM=1 ? */
    public static final int _0009_xxA0_ = 0x000900A0;

    /** (0009,xxF2) VR=LT VM=1 ? */
    public static final int _0009_xxF2_ = 0x000900F2;

    /** (0009,xxF3) VR=UN VM=1 ? */
    public static final int _0009_xxF3_ = 0x000900F3;

    /** (0009,xxF4) VR=LT VM=1 ? */
    public static final int _0009_xxF4_ = 0x000900F4;

    /** (0009,xxF5) VR=UN VM=1 ? */
    public static final int _0009_xxF5_ = 0x000900F5;

    /** (0009,xxF7) VR=LT VM=1 ? */
    public static final int _0009_xxF7_ = 0x000900F7;

    /** (0011,xx10) VR=LT VM=1 Patient Entry ID */
    public static final int PatientEntryID = 0x00110010;

    /** (0011,xx20) VR=UN VM=1 ? */
    public static final int _0011_xx20_ = 0x00110020;

    /** (0011,xx21) VR=UN VM=1 ? */
    public static final int _0011_xx21_ = 0x00110021;

    /** (0011,xx22) VR=UN VM=1 ? */
    public static final int _0011_xx22_ = 0x00110022;

    /** (0011,xx30) VR=UN VM=1 ? */
    public static final int _0011_xx30_ = 0x00110030;

    /** (0011,xx31) VR=UN VM=1 ? */
    public static final int _0011_xx31_ = 0x00110031;

    /** (0011,xx32) VR=UN VM=1 ? */
    public static final int _0011_xx32_ = 0x00110032;

    /** (0019,xx00) VR=LO VM=1 ? */
    public static final int _0019_xx00_ = 0x00190000;

    /** (0019,xx01) VR=LO VM=1 ? */
    public static final int _0019_xx01_ = 0x00190001;

    /** (0019,xx02) VR=UN VM=1 ? */
    public static final int _0019_xx02_ = 0x00190002;

    /** (0019,xx10) VR=US VM=1 Mains Frequency */
    public static final int MainsFrequency = 0x00190010;

    /** (0019,xx25) VR=LO VM=1-n Original Pixel Data Quality */
    public static final int OriginalPixelDataQuality = 0x00190025;

    /** (0019,xx30) VR=US VM=1 ECG Triggering */
    public static final int ECGTriggering = 0x00190030;

    /** (0019,xx31) VR=UN VM=1 ECG 1 Offset */
    public static final int ECG1Offset = 0x00190031;

    /** (0019,xx32) VR=UN VM=1 ECG 2 Offset 1 */
    public static final int ECG2Offset1 = 0x00190032;

    /** (0019,xx33) VR=UN VM=1 ECG 2 Offset 2 */
    public static final int ECG2Offset2 = 0x00190033;

    /** (0019,xx50) VR=US VM=1 Video Scan Mode */
    public static final int VideoScanMode = 0x00190050;

    /** (0019,xx51) VR=US VM=1 Video LineRate */
    public static final int VideoLineRate = 0x00190051;

    /** (0019,xx60) VR=US VM=1 Xray Technique */
    public static final int XrayTechnique = 0x00190060;

    /** (0019,xx61) VR=DS VM=1 Image Identifier Fromat */
    public static final int ImageIdentifierFromat = 0x00190061;

    /** (0019,xx62) VR=US VM=1 Iris Diaphragm */
    public static final int IrisDiaphragm = 0x00190062;

    /** (0019,xx63) VR=CS VM=1 Filter */
    public static final int Filter = 0x00190063;

    /** (0019,xx64) VR=CS VM=1 Cine Parallel */
    public static final int CineParallel = 0x00190064;

    /** (0019,xx65) VR=CS VM=1 Cine Master */
    public static final int CineMaster = 0x00190065;

    /** (0019,xx70) VR=US VM=1 Exposure Channel */
    public static final int ExposureChannel = 0x00190070;

    /** (0019,xx71) VR=UN VM=1 Exposure Channel First Image */
    public static final int ExposureChannelFirstImage = 0x00190071;

    /** (0019,xx72) VR=US VM=1 Processing Channel */
    public static final int ProcessingChannel = 0x00190072;

    /** (0019,xx80) VR=DS VM=1 Acquisition Delay */
    public static final int AcquisitionDelay = 0x00190080;

    /** (0019,xx81) VR=UN VM=1 Relative Image Time */
    public static final int RelativeImageTime = 0x00190081;

    /** (0019,xx90) VR=CS VM=1 Video White Compression */
    public static final int VideoWhiteCompression = 0x00190090;

    /** (0019,xxA0) VR=US VM=1 Angulation */
    public static final int Angulation = 0x001900A0;

    /** (0019,xxA1) VR=US VM=1 Rotation */
    public static final int Rotation = 0x001900A1;

    /** (0021,xx14) VR=LT VM=1 ? */
    public static final int _0021_xx14_ = 0x00210014;

    /** (0029,xx00) VR=DS VM=4 ? */
    public static final int _0029_xx00_ = 0x00290000;

    /** (0029,xx20) VR=DS VM=1 Pixel Aspect Ratio */
    public static final int PixelAspectRatio = 0x00290020;

    /** (0029,xx25) VR=LO VM=1-n Processed Pixel Data Quality */
    public static final int ProcessedPixelDataQuality = 0x00290025;

    /** (0029,xx30) VR=LT VM=1 ? */
    public static final int _0029_xx30_ = 0x00290030;

    /** (0029,xx38) VR=US VM=1 ? */
    public static final int _0029_xx38_ = 0x00290038;

    /** (0029,xx60) VR=LT VM=1 ? */
    public static final int _0029_xx60_ = 0x00290060;

    /** (0029,xx61) VR=LT VM=1 ? */
    public static final int _0029_xx61_ = 0x00290061;

    /** (0029,xx67) VR=LT VM=1 ? */
    public static final int _0029_xx67_ = 0x00290067;

    /** (0029,xx70) VR=LT VM=1 Window ID */
    public static final int WindowID = 0x00290070;

    /** (0029,xx71) VR=CS VM=1 Video Invert Subtracted */
    public static final int VideoInvertSubtracted = 0x00290071;

    /** (0029,xx72) VR=CS VM=1 Video Invert Nonsubtracted */
    public static final int VideoInvertNonsubtracted = 0x00290072;

    /** (0029,xx77) VR=CS VM=1 Window Select Status */
    public static final int WindowSelectStatus = 0x00290077;

    /** (0029,xx78) VR=LT VM=1 ECG Display Printing ID */
    public static final int ECGDisplayPrintingID = 0x00290078;

    /** (0029,xx79) VR=CS VM=1 ECG Display Printing */
    public static final int ECGDisplayPrinting = 0x00290079;

    /** (0029,xx7E) VR=CS VM=1 ECG Display Printing Enable Status */
    public static final int ECGDisplayPrintingEnableStatus = 0x0029007E;

    /** (0029,xx7F) VR=CS VM=1 ECG Display Printing Select Status */
    public static final int ECGDisplayPrintingSelectStatus = 0x0029007F;

    /** (0029,xx80) VR=LT VM=1 Physiological Display ID */
    public static final int PhysiologicalDisplayID = 0x00290080;

    /** (0029,xx81) VR=US VM=1 Preferred Physiological Channel Display */
    public static final int PreferredPhysiologicalChannelDisplay = 0x00290081;

    /** (0029,xx8E) VR=CS VM=1 Physiological Display Enable Status */
    public static final int PhysiologicalDisplayEnableStatus = 0x0029008E;

    /** (0029,xx8F) VR=CS VM=1 Physiological Display Select Status */
    public static final int PhysiologicalDisplaySelectStatus = 0x0029008F;

    /** (0029,xx90) VR=DS VM=1-n ? */
    public static final int _0029_xx90_ = 0x00290090;

    /** (0029,xx91) VR=US VM=1 ? */
    public static final int _0029_xx91_ = 0x00290091;

    /** (0029,xx9F) VR=CS VM=1 ? */
    public static final int _0029_xx9F_ = 0x0029009F;

    /** (0029,xxA0) VR=DS VM=1-n ? */
    public static final int _0029_xxA0_ = 0x002900A0;

    /** (0029,xxA1) VR=US VM=1 ? */
    public static final int _0029_xxA1_ = 0x002900A1;

    /** (0029,xxAF) VR=CS VM=1 ? */
    public static final int _0029_xxAF_ = 0x002900AF;

    /** (0029,xxB0) VR=DS VM=1-n ? */
    public static final int _0029_xxB0_ = 0x002900B0;

    /** (0029,xxB1) VR=US VM=1 ? */
    public static final int _0029_xxB1_ = 0x002900B1;

    /** (0029,xxBF) VR=CS VM=1 ? */
    public static final int _0029_xxBF_ = 0x002900BF;

    /** (0029,xxC0) VR=LT VM=1 Functional Shutter ID */
    public static final int FunctionalShutterID = 0x002900C0;

    /** (0029,xxC1) VR=US VM=1 Field Of Shutter */
    public static final int FieldOfShutter = 0x002900C1;

    /** (0029,xxC5) VR=LT VM=1 Field Of Shutter Rectangle */
    public static final int FieldOfShutterRectangle = 0x002900C5;

    /** (0029,xxCE) VR=CS VM=1 Shutter Enable Status */
    public static final int ShutterEnableStatus = 0x002900CE;

    /** (0029,xxCF) VR=CS VM=1 Shutter Select Status */
    public static final int ShutterSelectStatus = 0x002900CF;

    /** (7FE1,xx10) VR=OW/OB VM=1 Pixel Data */
    public static final int PixelData = 0x7FE10010;

}
