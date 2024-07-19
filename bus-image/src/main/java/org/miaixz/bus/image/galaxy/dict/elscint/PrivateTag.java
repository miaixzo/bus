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
package org.miaixz.bus.image.galaxy.dict.elscint;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "ELSCINT1";

    /** (0003,xx01) VR=OW VM=1 Offset List Structure */
    public static final int OffsetListStructure = 0x00030001;

    /** (00E1,xx01) VR=US VM=1 Data Dictionary Version */
    public static final int DataDictionaryVersion = 0x00E10001;

    /** (00E1,xx05) VR=IS VM=1 ? */
    public static final int _00E1_xx05_ = 0x00E10005;

    /** (00E1,xx06) VR=IS VM=1 ? */
    public static final int _00E1_xx06_ = 0x00E10006;

    /** (00E1,xx07) VR=IS VM=1 ? */
    public static final int _00E1_xx07_ = 0x00E10007;

    /** (00E1,xx14) VR=CS VM=1 ? */
    public static final int _00E1_xx14_ = 0x00E10014;

    /** (00E1,xx18) VR=OB VM=1 ? */
    public static final int _00E1_xx18_ = 0x00E10018;

    /** (00E1,xx21) VR=DS VM=1 DLP Total */
    public static final int DLPTotal = 0x00E10021;

    /** (00E1,xx22) VR=DS VM=2 Presentation Relative Center */
    public static final int PresentationRelativeCenter = 0x00E10022;

    /** (00E1,xx23) VR=DS VM=2 Presentation Relative Part */
    public static final int PresentationRelativePart = 0x00E10023;

    /** (00E1,xx24) VR=CS VM=1 Presentation Horizontal Invert */
    public static final int PresentationHorizontalInvert = 0x00E10024;

    /** (00E1,xx25) VR=CS VM=1 Presentation Vertical Invert */
    public static final int PresentationVerticalInvert = 0x00E10025;

    /** (00E1,xx2A) VR=DS VM=1 ? */
    public static final int _00E1_xx2A_ = 0x00E1002A;

    /** (00E1,xx30) VR=UI VM=1 ? */
    public static final int _00E1_xx30_ = 0x00E10030;

    /** (00E1,xx31) VR=CS VM=1 ? */
    public static final int _00E1_xx31_ = 0x00E10031;

    /** (00E1,xx32) VR=US VM=2 ? */
    public static final int _00E1_xx32_ = 0x00E10032;

    /** (00E1,xx37) VR=DS VM=1 Total Dose Savings */
    public static final int TotalDoseSavings = 0x00E10037;

    /** (00E1,xx39) VR=SQ VM=1 ? */
    public static final int _00E1_xx39_ = 0x00E10039;

    /** (00E1,xx3E) VR=IS VM=1 ? */
    public static final int _00E1_xx3E_ = 0x00E1003E;

    /** (00E1,xx3F) VR=CS VM=1 ? */
    public static final int _00E1_xx3F_ = 0x00E1003F;

    /** (00E1,xx40) VR=SH VM=1 Image Label */
    public static final int ImageLabel = 0x00E10040;

    /** (00E1,xx41) VR=DS VM=1 ? */
    public static final int _00E1_xx41_ = 0x00E10041;

    /** (00E1,xx42) VR=LO VM=1 ? */
    public static final int _00E1_xx42_ = 0x00E10042;

    /** (00E1,xx43) VR=IS VM=1 ? */
    public static final int _00E1_xx43_ = 0x00E10043;

    /** (00E1,xx50) VR=DS VM=1 Acquisition Duration */
    public static final int AcquisitionDuration = 0x00E10050;

    /** (00E1,xx51) VR=SH VM=1 Series Label */
    public static final int SeriesLabel = 0x00E10051;

    /** (00E1,xx60) VR=CS VM=1 ? */
    public static final int _00E1_xx60_ = 0x00E10060;

    /** (00E1,xx61) VR=LO VM=1 Protocol File Name */
    public static final int ProtocolFileName = 0x00E10061;

    /** (00E1,xx62) VR=CS VM=1 ? */
    public static final int _00E1_xx62_ = 0x00E10062;

    /** (00E1,xx63) VR=SH VM=1 Patient Language */
    public static final int PatientLanguage = 0x00E10063;

    /** (00E1,xx65) VR=LO VM=1 Patient Data Modification Date */
    public static final int PatientDataModificationDate = 0x00E10065;

    /** (00E1,xx6A) VR=IS VM=1 ? */
    public static final int _00E1_xx6A_ = 0x00E1006A;

    /** (00E1,xx6B) VR=IS VM=1 ? */
    public static final int _00E1_xx6B_ = 0x00E1006B;

    /** (00E1,xxA0) VR=LO VM=1 ? */
    public static final int _00E1_xxA0_ = 0x00E100A0;

    /** (00E1,xxC2) VR=UI VM=1 ? */
    public static final int _00E1_xxC2_ = 0x00E100C2;

    /** (00E1,xxC4) VR=DS VM=1 ? */
    public static final int _00E1_xxC4_ = 0x00E100C4;

    /** (00E1,xxCF) VR=IS VM=1 ? */
    public static final int _00E1_xxCF_ = 0x00E100CF;

    /** (00E1,xxEB) VR=US VM=1 ? */
    public static final int _00E1_xxEB_ = 0x00E100EB;

    /** (00E1,xxEC) VR=US VM=1 ? */
    public static final int _00E1_xxEC_ = 0x00E100EC;

    /** (00E3,xx00) VR=OB VM=1 ? */
    public static final int _00E3_xx00_ = 0x00E30000;

    /** (00E3,xx18) VR=OB VM=1 ? */
    public static final int _00E3_xx18_ = 0x00E30018;

    /** (00E3,xx1F) VR=OB VM=1 ? */
    public static final int _00E3_xx1F_ = 0x00E3001F;

    /** (01E1,xx18) VR=OB VM=1 ? */
    public static final int _01E1_xx18_ = 0x01E10018;

    /** (01E1,xx21) VR=ST VM=1 ? */
    public static final int _01E1_xx21_ = 0x01E10021;

    /** (01E1,xx26) VR=CS VM=1 Phantom Type */
    public static final int PhantomType = 0x01E10026;

    /** (01E1,xx34) VR=IS VM=1 ? */
    public static final int _01E1_xx34_ = 0x01E10034;

    /** (01E1,xx40) VR=UI VM=1 ? */
    public static final int _01E1_xx40_ = 0x01E10040;

    /** (01E1,xx41) VR=OW VM=1 ? */
    public static final int _01E1_xx41_ = 0x01E10041;

    /** (01E1,xx55) VR=SQ VM=1 Reference Sequence */
    public static final int ReferenceSequence = 0x01E10055;

    /** (01E1,xx56) VR=CS VM=1 Reference Type */
    public static final int ReferenceType = 0x01E10056;

    /** (01E1,xx57) VR=CS VM=1 Reference Level */
    public static final int ReferenceLevel = 0x01E10057;

    /** (01F1,xx01) VR=CS VM=1 Acquisition Type */
    public static final int AcquisitionType = 0x01F10001;

    /** (01F1,xx02) VR=CS VM=1 Focal Spot Resolution */
    public static final int FocalSpotResolution = 0x01F10002;

    /** (01F1,xx03) VR=CS VM=1 Concurrent Slices Generation */
    public static final int ConcurrentSlicesGeneration = 0x01F10003;

    /** (01F1,xx04) VR=CS VM=1 Angular Sampling Density */
    public static final int AngularSamplingDensity = 0x01F10004;

    /** (01F1,xx05) VR=DS VM=1 Reconstruction Arc */
    public static final int ReconstructionArc = 0x01F10005;

    /** (01F1,xx06) VR=DS VM=1 ? */
    public static final int _01F1_xx06_ = 0x01F10006;

    /** (01F1,xx07) VR=DS VM=1 Table Velocity */
    public static final int TableVelocity = 0x01F10007;

    /** (01F1,xx08) VR=DS VM=1 Acquisition Length */
    public static final int AcquisitionLength = 0x01F10008;

    /** (01F1,xx0A) VR=US VM=1 Edge Enhancement Weight */
    public static final int EdgeEnhancementWeight = 0x01F1000A;

    /** (01F1,xx0B) VR=CS VM=2 ? */
    public static final int _01F1_xx0B_ = 0x01F1000B;

    /** (01F1,xx0C) VR=DS VM=1 Scanner Relative Center */
    public static final int ScannerRelativeCenter = 0x01F1000C;

    /** (01F1,xx0D) VR=DS VM=1 Rotation Angle */
    public static final int RotationAngle = 0x01F1000D;

    /** (01F1,xx0E) VR=FL VM=1 ? */
    public static final int _01F1_xx0E_ = 0x01F1000E;

    /** (01F1,xx26) VR=DS VM=1 Pitch */
    public static final int Pitch = 0x01F10026;

    /** (01F1,xx27) VR=DS VM=1 Rotation Time */
    public static final int RotationTime = 0x01F10027;

    /** (01F1,xx28) VR=DS VM=1 Table Increment */
    public static final int TableIncrement = 0x01F10028;

    /** (01F1,xx30) VR=US VM=1 ? */
    public static final int _01F1_xx30_ = 0x01F10030;

    /** (01F1,xx32) VR=CS VM=1 Image View Convention */
    public static final int ImageViewConvention = 0x01F10032;

    /** (01F1,xx33) VR=DS VM=1 Cycle Time */
    public static final int CycleTime = 0x01F10033;

    /** (01F1,xx36) VR=CS VM=1 ? */
    public static final int _01F1_xx36_ = 0x01F10036;

    /** (01F1,xx37) VR=DS VM=1 ? */
    public static final int _01F1_xx37_ = 0x01F10037;

    /** (01F1,xx38) VR=LO VM=1 ? */
    public static final int _01F1_xx38_ = 0x01F10038;

    /** (01F1,xx39) VR=LO VM=1 ? */
    public static final int _01F1_xx39_ = 0x01F10039;

    /** (01F1,xx40) VR=CS VM=1 ? */
    public static final int _01F1_xx40_ = 0x01F10040;

    /** (01F1,xx42) VR=SH VM=1 ? */
    public static final int _01F1_xx42_ = 0x01F10042;

    /** (01F1,xx43) VR=LO VM=1 ? */
    public static final int _01F1_xx43_ = 0x01F10043;

    /** (01F1,xx44) VR=OW VM=1 ? */
    public static final int _01F1_xx44_ = 0x01F10044;

    /** (01F1,xx45) VR=IS VM=1 ? */
    public static final int _01F1_xx45_ = 0x01F10045;

    /** (01F1,xx46) VR=FL VM=1 ? */
    public static final int _01F1_xx46_ = 0x01F10046;

    /** (01F1,xx47) VR=SH VM=1 ? */
    public static final int _01F1_xx47_ = 0x01F10047;

    /** (01F1,xx49) VR=DS VM=1 ? */
    public static final int _01F1_xx49_ = 0x01F10049;

    /** (01F1,xx4A) VR=SH VM=1 ? */
    public static final int _01F1_xx4A_ = 0x01F1004A;

    /** (01F1,xx4B) VR=SH VM=1 ? */
    public static final int _01F1_xx4B_ = 0x01F1004B;

    /** (01F1,xx4C) VR=SH VM=1 ? */
    public static final int _01F1_xx4C_ = 0x01F1004C;

    /** (01F1,xx4D) VR=SH VM=1 ? */
    public static final int _01F1_xx4D_ = 0x01F1004D;

    /** (01F1,xx4E) VR=LO VM=1 ? */
    public static final int _01F1_xx4E_ = 0x01F1004E;

    /** (01F1,xx4F) VR=US VM=1 Detectors Layers */
    public static final int DetectorsLayers = 0x01F1004F;

    /** (01F1,xx53) VR=SH VM=1 ? */
    public static final int _01F1_xx53_ = 0x01F10053;

    /** (01F3,xx01) VR=SQ VM=1 ? */
    public static final int _01F3_xx01_ = 0x01F30001;

    /** (01F3,xx02) VR=SS VM=1 ? */
    public static final int _01F3_xx02_ = 0x01F30002;

    /** (01F3,xx03) VR=FL VM=2 ? */
    public static final int _01F3_xx03_ = 0x01F30003;

    /** (01F3,xx04) VR=FL VM=1 ? */
    public static final int _01F3_xx04_ = 0x01F30004;

    /** (01F3,xx11) VR=SQ VM=1 PS Sequence */
    public static final int PSSequence = 0x01F30011;

    /** (01F3,xx12) VR=SS VM=1 ? */
    public static final int _01F3_xx12_ = 0x01F30012;

    /** (01F3,xx13) VR=FL VM=2 ? */
    public static final int _01F3_xx13_ = 0x01F30013;

    /** (01F3,xx14) VR=FL VM=1 ? */
    public static final int _01F3_xx14_ = 0x01F30014;

    /** (01F3,xx15) VR=US VM=1 ? */
    public static final int _01F3_xx15_ = 0x01F30015;

    /** (01F3,xx16) VR=FL VM=1 ? */
    public static final int _01F3_xx16_ = 0x01F30016;

    /** (01F3,xx17) VR=FL VM=1 ? */
    public static final int _01F3_xx17_ = 0x01F30017;

    /** (01F3,xx18) VR=SH VM=1 ? */
    public static final int _01F3_xx18_ = 0x01F30018;

    /** (01F3,xx19) VR=FL VM=1 ? */
    public static final int _01F3_xx19_ = 0x01F30019;

    /** (01F3,xx23) VR=US VM=1 ? */
    public static final int _01F3_xx23_ = 0x01F30023;

    /** (01F3,xx24) VR=IS VM=2 ? */
    public static final int _01F3_xx24_ = 0x01F30024;

    /** (01F7,xx10) VR=OB VM=1 ? */
    public static final int _01F7_xx10_ = 0x01F70010;

    /** (01F7,xx11) VR=OW VM=1 ? */
    public static final int _01F7_xx11_ = 0x01F70011;

    /** (01F7,xx13) VR=OW VM=1 ? */
    public static final int _01F7_xx13_ = 0x01F70013;

    /** (01F7,xx14) VR=OW VM=1 ? */
    public static final int _01F7_xx14_ = 0x01F70014;

    /** (01F7,xx15) VR=OW VM=1 ? */
    public static final int _01F7_xx15_ = 0x01F70015;

    /** (01F7,xx16) VR=OW VM=1 ? */
    public static final int _01F7_xx16_ = 0x01F70016;

    /** (01F7,xx17) VR=OW VM=1 ? */
    public static final int _01F7_xx17_ = 0x01F70017;

    /** (01F7,xx18) VR=OW VM=1 ? */
    public static final int _01F7_xx18_ = 0x01F70018;

    /** (01F7,xx19) VR=OW VM=1 ? */
    public static final int _01F7_xx19_ = 0x01F70019;

    /** (01F7,xx1A) VR=OW VM=1 ? */
    public static final int _01F7_xx1A_ = 0x01F7001A;

    /** (01F7,xx1B) VR=OW VM=1 ? */
    public static final int _01F7_xx1B_ = 0x01F7001B;

    /** (01F7,xx1C) VR=OW VM=1 ? */
    public static final int _01F7_xx1C_ = 0x01F7001C;

    /** (01F7,xx1E) VR=OW VM=1 ? */
    public static final int _01F7_xx1E_ = 0x01F7001E;

    /** (01F7,xx1F) VR=OW VM=1 ? */
    public static final int _01F7_xx1F_ = 0x01F7001F;

    /** (01F7,xx22) VR=UI VM=1 ? */
    public static final int _01F7_xx22_ = 0x01F70022;

    /** (01F7,xx23) VR=OW VM=1 ? */
    public static final int _01F7_xx23_ = 0x01F70023;

    /** (01F7,xx25) VR=OW VM=1 ? */
    public static final int _01F7_xx25_ = 0x01F70025;

    /** (01F7,xx26) VR=OW VM=1 ? */
    public static final int _01F7_xx26_ = 0x01F70026;

    /** (01F7,xx27) VR=OW VM=1 ? */
    public static final int _01F7_xx27_ = 0x01F70027;

    /** (01F7,xx28) VR=OW VM=1 ? */
    public static final int _01F7_xx28_ = 0x01F70028;

    /** (01F7,xx29) VR=OW VM=1 ? */
    public static final int _01F7_xx29_ = 0x01F70029;

    /** (01F7,xx2B) VR=OW VM=1 ? */
    public static final int _01F7_xx2B_ = 0x01F7002B;

    /** (01F7,xx2C) VR=OW VM=1 ? */
    public static final int _01F7_xx2C_ = 0x01F7002C;

    /** (01F7,xx2D) VR=OW VM=1 ? */
    public static final int _01F7_xx2D_ = 0x01F7002D;

    /** (01F7,xx2E) VR=OW VM=1 ? */
    public static final int _01F7_xx2E_ = 0x01F7002E;

    /** (01F7,xx30) VR=OW VM=1 ? */
    public static final int _01F7_xx30_ = 0x01F70030;

    /** (01F7,xx31) VR=OW VM=1 ? */
    public static final int _01F7_xx31_ = 0x01F70031;

    /** (01F7,xx5C) VR=OW VM=1 ? */
    public static final int _01F7_xx5C_ = 0x01F7005C;

    /** (01F7,xx70) VR=OW VM=1 ? */
    public static final int _01F7_xx70_ = 0x01F70070;

    /** (01F7,xx73) VR=OW VM=1 ? */
    public static final int _01F7_xx73_ = 0x01F70073;

    /** (01F7,xx74) VR=OW VM=1 ? */
    public static final int _01F7_xx74_ = 0x01F70074;

    /** (01F7,xx75) VR=OW VM=1 ? */
    public static final int _01F7_xx75_ = 0x01F70075;

    /** (01F7,xx7F) VR=OW VM=1 ? */
    public static final int _01F7_xx7F_ = 0x01F7007F;

    /** (01F7,xx9B) VR=IS VM=1 iDose Level */
    public static final int iDoseLevel = 0x01F7009B;

    /** (01F7,xxCB) VR=DS VM=1 keV */
    public static final int keV = 0x01F700CB;

    /** (01F7,xxCC) VR=ST VM=1 SBI Version */
    public static final int SBIVersion = 0x01F700CC;

    /** (01F7,xxCD) VR=CS VM=1 SC CT Equivalent */
    public static final int SCCTEquivalent = 0x01F700CD;

    /** (01F7,xxCE) VR=LT VM=1 Reference SBI Type */
    public static final int ReferenceSBIType = 0x01F700CE;

    /** (01F7,xxD3) VR=LT VM=1 Burned Spectral Annotations */
    public static final int BurnedSpectralAnnotations = 0x01F700D3;

    /** (01F7,xxD4) VR=SH VM=1 Head Body */
    public static final int HeadBody = 0x01F700D4;

    /** (01F7,xxD6) VR=IS VM=1 Spectral Level */
    public static final int SpectralLevel = 0x01F700D6;

    /** (01F9,xx01) VR=LO VM=1 SP Filter */
    public static final int SPFilter = 0x01F90001;

    /** (01F9,xx04) VR=IS VM=1 Adaptive Filter */
    public static final int AdaptiveFilter = 0x01F90004;

    /** (01F9,xx05) VR=IS VM=1 Recon Increment */
    public static final int ReconIncrement = 0x01F90005;

    /** (01F9,xx08) VR=DS VM=1 ? */
    public static final int _01F9_xx08_ = 0x01F90008;

    /** (01F9,xx09) VR=DS VM=1 ? */
    public static final int _01F9_xx09_ = 0x01F90009;

    /** (0601,xx00) VR=SH VM=1 Implementation Version */
    public static final int ImplementationVersion = 0x06010000;

    /** (0601,xx20) VR=DS VM=1 Relative Table Position */
    public static final int RelativeTablePosition = 0x06010020;

    /** (0601,xx21) VR=DS VM=1 Relative Table Height */
    public static final int RelativeTableHeight = 0x06010021;

    /** (0601,xx30) VR=SH VM=1 Surview Direction */
    public static final int SurviewDirection = 0x06010030;

    /** (0601,xx31) VR=DS VM=1 Surview Length */
    public static final int SurviewLength = 0x06010031;

    /** (0601,xx50) VR=SH VM=1 Image View Type */
    public static final int ImageViewType = 0x06010050;

    /** (0601,xx70) VR=DS VM=1 Batch Number */
    public static final int BatchNumber = 0x06010070;

    /** (0601,xx71) VR=DS VM=1 Batch Size */
    public static final int BatchSize = 0x06010071;

    /** (0601,xx72) VR=DS VM=1 Batch Slice Number */
    public static final int BatchSliceNumber = 0x06010072;

    /** (07A1,xx01) VR=UL VM=1 Number Of Series In Study */
    public static final int NumberOfSeriesInStudy = 0x07A10001;

    /** (07A1,xx02) VR=UL VM=1 Number Of Images In Series */
    public static final int NumberOfImagesInStudy = 0x07A10002;

    /** (07A1,xx03) VR=UL VM=1 Last Update Time */
    public static final int LastUpdateTime = 0x07A10003;

    /** (07A1,xx04) VR=UL VM=1 Last Update Date */
    public static final int LastUpdateDate = 0x07A10004;

    /** (07A1,xx07) VR=US VM=3 ? */
    public static final int _07A1_xx07_ = 0x07A10007;

    /** (07A1,xx08) VR=DS VM=1-n ? */
    public static final int _07A1_xx08_ = 0x07A10008;

    /** (07A1,xx09) VR=OW VM=1 ? */
    public static final int _07A1_xx09_ = 0x07A10009;

    /** (07A1,xx0A) VR=OB VM=1 Tamar Compressed Pixel Data */
    public static final int TamarCompressedPixelData = 0x07A1000A;

    /** (07A1,xx0C) VR=US VM=1 ? */
    public static final int _07A1_xx0C_ = 0x07A1000C;

    /** (07A1,xx10) VR=LO VM=1 Tamar Software Version */
    public static final int TamarSoftwareVersion = 0x07A10010;

    /** (07A1,xx11) VR=CS VM=1 Tamar Compression Type */
    public static final int TamarCompressionType = 0x07A10011;

    /** (07A1,xx12) VR=FL VM=1-n ? */
    public static final int _07A1_xx12_ = 0x07A10012;

    /** (07A1,xx13) VR=UL VM=1 ? */
    public static final int _07A1_xx13_ = 0x07A10013;

    /** (07A1,xx14) VR=LO VM=1 Protection Flag */
    public static final int ProtectionFlag = 0x07A10014;

    /** (07A1,xx16) VR=FL VM=1-n ? */
    public static final int _07A1_xx16_ = 0x07A10016;

    /** (07A1,xx18) VR=SQ VM=1 ? */
    public static final int _07A1_xx18_ = 0x07A10018;

    /** (07A1,xx19) VR=FL VM=1 ? */
    public static final int _07A1_xx19_ = 0x07A10019;

    /** (07A1,xx1C) VR=FL VM=1-n ? */
    public static final int _07A1_xx1C_ = 0x07A1001C;

    /** (07A1,xx2A) VR=CS VM=1 Tamar Study Status */
    public static final int TamarStudyStatus = 0x07A1002A;

    /** (07A1,xx2B) VR=CS VM=1 ? */
    public static final int _07A1_xx2B_ = 0x07A1002B;

    /** (07A1,xx36) VR=AE VM=1 Tamar Source AE */
    public static final int TamarSourceAE = 0x07A10036;

    /** (07A1,xx3D) VR=US VM=1 ? */
    public static final int _07A1_xx3D_ = 0x07A1003D;

    /** (07A1,xx40) VR=CS VM=1 Tamar Study Body Part */
    public static final int TamarStudyBodyPart = 0x07A10040;

    /** (07A1,xx42) VR=SH VM=1 ? */
    public static final int _07A1_xx42_ = 0x07A10042;

    /** (07A1,xx43) VR=IS VM=1 Tamar Study Priority */
    public static final int TamarStudyPriority = 0x07A10043;

    /** (07A1,xx45) VR=LT VM=1 ? */
    public static final int _07A1_xx45_ = 0x07A10045;

    /** (07A1,xx47) VR=CS VM=1 ? */
    public static final int _07A1_xx47_ = 0x07A10047;

    /** (07A1,xx4A) VR=SH VM=1 ? */
    public static final int _07A1_xx4A_ = 0x07A1004A;

    /** (07A1,xx4D) VR=AT VM=1 Tamar Exclude Tags List */
    public static final int TamarExcludeTagsList = 0x07A1004D;

    /** (07A1,xx50) VR=US VM=1 Tamar Site ID */
    public static final int TamarSiteID = 0x07A10050;

    /** (07A1,xx56) VR=US VM=1 ? */
    public static final int _07A1_xx56_ = 0x07A10056;

    /** (07A1,xx58) VR=CS VM=1 Tamar Study Published */
    public static final int TamarStudyPublished = 0x07A10058;

    /** (07A1,xx5D) VR=DT VM=1 Tamar Study Creation Date */
    public static final int TamarStudyCreationDate = 0x07A1005D;

    /** (07A1,xx5F) VR=CS VM=1 Tamar Study Has Bookmark */
    public static final int TamarStudyHasBookmark = 0x07A1005F;

    /** (07A1,xx70) VR=SH VM=1 ? */
    public static final int _07A1_xx70_ = 0x07A10070;

    /** (07A1,xx71) VR=SH VM=1 ? */
    public static final int _07A1_xx71_ = 0x07A10071;

    /** (07A1,xx75) VR=LO VM=2 Tamar Source Image Type */
    public static final int TamarSourceImageType = 0x07A10075;

    /** (07A1,xx76) VR=ST VM=1 ? */
    public static final int _07A1_xx76_ = 0x07A10076;

    /** (07A1,xx85) VR=UL VM=1 Tamar Translate Flags */
    public static final int TamarTranslateFlags = 0x07A10085;

    /** (07A1,xx87) VR=LT VM=1 ? */
    public static final int _07A1_xx87_ = 0x07A10087;

    /** (07A1,xx88) VR=CS VM=1 ? */
    public static final int _07A1_xx88_ = 0x07A10088;

    /** (07A1,xx8C) VR=ST VM=1 ? */
    public static final int _07A1_xx8C_ = 0x07A1008C;

    /** (07A1,xx94) VR=ST VM=1 ? */
    public static final int _07A1_xx94_ = 0x07A10094;

    /** (07A1,xx96) VR=DA VM=1 ? */
    public static final int _07A1_xx96_ = 0x07A10096;

    /** (07A1,xx97) VR=SH VM=1 ? */
    public static final int _07A1_xx97_ = 0x07A10097;

    /** (07A1,xx98) VR=CS VM=1 ? */
    public static final int _07A1_xx98_ = 0x07A10098;

    /** (07A1,xx9F) VR=CS VM=1 ? */
    public static final int _07A1_xx9F_ = 0x07A1009F;

    /** (07A1,xxC3) VR=LO VM=1 ? */
    public static final int _07A1_xxC3_ = 0x07A100C3;

    /** (07A1,xxC5) VR=IS VM=1 Tamar Exclude Tags Size */
    public static final int TamarExcludeTagsSize = 0x07A100C5;

    /** (07A1,xxC6) VR=IS VM=1 Tamar Exclude Tags Total Size */
    public static final int TamarExcludeTagsTotalSize = 0x07A100C6;

    /** (07A1,xxC7) VR=CS VM=1 Tamar Is Hcff Header */
    public static final int TamarIsHcffHeader = 0x07A100C7;

    /** (07A1,xxD0) VR=LO VM=1 ? */
    public static final int _07A1_xxD0_ = 0x07A100D0;

    /** (07A3,xx01) VR=LO VM=1 Tamar Exe Software Version */
    public static final int TamarExeSoftwareVersion = 0x07A30001;

    /** (07A3,xx03) VR=CS VM=1 Tamar Study Has Sticky Note */
    public static final int TamarStudyHasStickyNote = 0x07A30003;

    /** (07A3,xx05) VR=CS VM=1 Tamar Mp Save Pr */
    public static final int TamarMpSavePr = 0x07A30005;

    /** (07A3,xx06) VR=CS VM=1 Tamar Original Pr Modality */
    public static final int TamarOriginalPrModality = 0x07A30006;

    /** (07A3,xx09) VR=IS VM=1 ? */
    public static final int _07A3_xx09_ = 0x07A30009;

    /** (07A3,xx0F) VR=CS VM=1 Tamar Original Curve Type */
    public static final int TamarOriginalCurveType = 0x07A3000F;

    /** (07A3,xx10) VR=LO VM=1 Tamar Original Curve Desc */
    public static final int TamarOriginalCurveDesc = 0x07A30010;

    /** (07A3,xx13) VR=SH VM=1 ? */
    public static final int _07A3_xx13_ = 0x07A30013;

    /** (07A3,xx14) VR=ST VM=1 Tamar Procedure Code */
    public static final int TamarProcedureCode = 0x07A30014;

    /** (07A3,xx15) VR=ST VM=1 Tamar Patient Location */
    public static final int TamarPatientLocation = 0x07A30015;

    /** (07A3,xx17) VR=SH VM=1 Tamar Order Status */
    public static final int TamarOrderStatus = 0x07A30017;

    /** (07A3,xx19) VR=ST VM=1 Tamar Reading Physician Last Name */
    public static final int TamarReadingPhysicianLastName = 0x07A30019;

    /** (07A3,xx1A) VR=ST VM=1 Tamar Reading Physician First Name */
    public static final int TamarReadingPhysicianFirstName = 0x07A3001A;

    /** (07A3,xx1B) VR=ST VM=1 ? */
    public static final int _07A3_xx1B_ = 0x07A3001B;

    /** (07A3,xx1C) VR=ST VM=1 Tamar Signing Physician Last Name */
    public static final int TamarSigningPhysicianLastName = 0x07A3001C;

    /** (07A3,xx1D) VR=ST VM=1 Tamar Signing Physician First Name */
    public static final int TamarSigningPhysicianFirstName = 0x07A3001D;

    /** (07A3,xx1E) VR=ST VM=1 Tamar Signing Physician ID */
    public static final int TamarSigningPhysicianID = 0x07A3001E;

    /** (07A3,xx1F) VR=ST VM=1 ? */
    public static final int _07A3_xx1F_ = 0x07A3001F;

    /** (07A3,xx22) VR=ST VM=1 ? */
    public static final int _07A3_xx22_ = 0x07A30022;

    /** (07A3,xx23) VR=ST VM=1 Tamar Misc String 5 */
    public static final int TamarMiscString5 = 0x07A30023;

    /** (07A3,xx31) VR=SH VM=1 ? */
    public static final int _07A3_xx31_ = 0x07A30031;

    /** (07A3,xx34) VR=SH VM=1 Tamar Study Age */
    public static final int TamarStudyAge = 0x07A30034;

    /** (07A3,xx43) VR=DS VM=1-n Tamar Pan Side Details */
    public static final int TamarPanSideDetails = 0x07A30043;

    /** (07A3,xx53) VR=US VM=1 _007A3_xx53_ */
    public static final int _07A3_xx53_ = 0x07A30053;

    /** (07A3,xx54) VR=US VM=3 _007A3_xx54_ */
    public static final int _07A3_xx54_ = 0x07A30054;

    /** (07A3,xx55) VR=SH VM=1 Tamar Study Has Mammo Cad */
    public static final int TamarStudyHasMammoCad = 0x07A30055;

    /** (07A3,xx5C) VR=ST VM=1 _07A3xx5C_ */
    public static final int _07A3xx5C_ = 0x07A3005C;

    /** (07A3,xx61) VR=LT VM=1 Tamar Nondicom Annotations */
    public static final int TamarNondicomAnnotations = 0x07A30061;

    /** (07A3,xx62) VR=SQ VM=1 Tamar Nondicom Annotations Sequence */
    public static final int TamarNondicomAnnotationsSequence = 0x07A30062;

    /** (07A3,xx63) VR=SQ VM=1 ? */
    public static final int _07A3_xx63_ = 0x07A30063;

    /** (07A3,xx64) VR=IS VM=1-n ? */
    public static final int _07A3_xx64_ = 0x07A30064;

    /** (07A3,xx65) VR=CS VM=1-n ? */
    public static final int _07A3_xx65_ = 0x07A30065;

    /** (07A3,xx66) VR=IS VM=1 ? */
    public static final int _07A3_xx66_ = 0x07A30066;

    /** (07A3,xx80) VR=SQ VM=1 ? */
    public static final int _07A3_xx80_ = 0x07A30080;

    /** (07A3,xx8F) VR=CS VM=1 ? */
    public static final int _07A3_xx8F_ = 0x07A3008F;

    /** (07A3,xx92) VR=DS VM=1 ? */
    public static final int _07A3_xx92_ = 0x07A30092;

    /** (07A3,xx93) VR=DS VM=1 ? */
    public static final int _07A3_xx93_ = 0x07A30093;

    /** (07A3,xx99) VR=CS VM=1 Tamar Is Doc Type */
    public static final int TamarIsDocType = 0x07A30099;

    /** (07A3,xx9C) VR=CS VM=1 Tamar Study Has Key Image */
    public static final int TamarStudyHasKeyImage = 0x07A3009C;

    /** (07A3,xx9F) VR=CS VM=1 ? */
    public static final int _07A3_xx9F_ = 0x07A3009F;

    /** (07A3,xxB4) VR=US VM=1 ? */
    public static final int _07A3_xxB4_ = 0x07A300B4;

    /** (07A3,xxB9) VR=CS VM=1 Tamar Key Series Indication */
    public static final int TamarKeySeriesIndication = 0x07A300B9;

    /** (07A3,xxBB) VR=CS VM=1 Tamar Study Has Key Series */
    public static final int TamarStudyHasKeySeries = 0x07A300BB;

    /** (07A3,xxC0) VR=SQ VM=1 Tamar Grid Token Load Sequence */
    public static final int TamarGridTokenLoadSequence = 0x07A300C0;

    /** (07A3,xxC1) VR=LO VM=1 Tamar Grid Token Target Grid Name */
    public static final int TamarGridTokenTargetGridName = 0x07A300C1;

    /** (07A3,xxC2) VR=CS VM=1 Tamar Grid Token Speed */
    public static final int TamarGridTokenSpeed = 0x07A300C2;

    /** (07A3,xxC3) VR=CS VM=1 Tamar Grid Token Security */
    public static final int TamarGridTokenSecurity = 0x07A300C3;

    /** (07A3,xxC4) VR=LO VM=1 Tamar Grid Token Next Host */
    public static final int TamarGridTokenNextHost = 0x07A300C4;

    /** (07A3,xxC5) VR=LO VM=1 Tamar Grid Token Next Host Port */
    public static final int TamarGridTokenNextHostPort = 0x07A300C5;

    /** (07A3,xxC6) VR=SQ VM=1 Tamar Grid Token Query Sequence */
    public static final int TamarGridTokenQuerySequence = 0x07A300C6;

    /** (07A3,xxC8) VR=AE VM=1 Tamar Grid Token AE */
    public static final int TamarGridTokenAE = 0x07A300C8;

    /** (07A3,xxC9) VR=CS VM=1 Tamar Grid Token Tunneled */
    public static final int TamarGridTokenTunneled = 0x07A300C9;

    /** (07A3,xxCA) VR=SQ VM=1 Tamar Grid Token Archive Sequence */
    public static final int TamarGridTokenArchiveSequence = 0x07A300CA;

    /** (07A3,xxCB) VR=SQ VM=1 Tamar Grid Token Location Sequence */
    public static final int TamarGridTokenLocationSequence = 0x07A300CB;

    /** (07A3,xxCC) VR=LO VM=1 Tamar Grid Token Version */
    public static final int TamarGridTokenVersion = 0x07A300CC;

    /** (07A3,xxD0) VR=SQ VM=1 Tamar Original Nested Elements Sequence */
    public static final int TamarOriginalNestedElementsSequence = 0x07A300D0;

    /** (07A3,xxD1) VR=US VM=1 Tamar Nested Element Number */
    public static final int TamarNestedElementNumber = 0x07A300D1;

    /** (07A3,xxD2) VR=CS VM=1 Tamar Nested Text Line Visibility */
    public static final int TamarNestedTextLineVisibility = 0x07A300D2;

    /** (07A3,xxD3) VR=CS VM=1 Tamar Nested Text Default Location */
    public static final int TamarNestedTextDefaultLocation = 0x07A300D3;

    /** (07A3,xxD4) VR=SL VM=1 Tamar Nested Text Location X */
    public static final int TamarNestedTextLocationX = 0x07A300D4;

    /** (07A3,xxD5) VR=SL VM=1 Tamar Nested Text Location Y */
    public static final int TamarNestedTextLocationY = 0x07A300D5;

    /** (07A3,xxD6) VR=CS VM=1 Tamar Nested Type Of Data */
    public static final int TamarNestedTypeOfData = 0x07A300D6;

    /** (07A3,xxE3) VR=LO VM=1 ? */
    public static final int _07A3_xxE3_ = 0x07A300E3;

    /** (07A3,xxF2) VR=CS VM=1 ? */
    public static final int _07A3_xxF2_ = 0x07A300F2;

    /** (07A3,xxF5) VR=LO VM=1 ? */
    public static final int _07A3_xxF5_ = 0x07A300F5;

    /** (07A3,xxFA) VR=DT VM=1 ? */
    public static final int _07A3_xxFA_ = 0x07A300FA;

    /** (07A3,xxFB) VR=DT VM=1 ? */
    public static final int _07A3_xxFB_ = 0x07A300FB;

    /** (07A5,xx00) VR=LO VM=1 ? */
    public static final int _07A5_xx00_ = 0x07A50000;

    /** (07A5,xx54) VR=DT VM=1 Tamar Reports Update Date */
    public static final int TamarReportsUpdateDate = 0x07A50054;

    /** (07A5,xx56) VR=CS VM=1 Tamar Referring Physicians Study Read */
    public static final int TamarReferringPhysiciansStudyRead = 0x07A50056;

    /** (07A5,xx59) VR=IS VM=1 ? */
    public static final int _07A5_xx59_ = 0x07A50059;

    /** (07A5,xx62) VR=LO VM=1 Tamar Study Institution Name */
    public static final int TamarStudyInstitutionName = 0x07A50062;

    /** (07A5,xx63) VR=CS VM=1-n ? */
    public static final int _07A5_xx63_ = 0x07A50063;

    /** (07A5,xx69) VR=AE VM=1 Tamar Original Storing AE */
    public static final int TamarOriginalStoringAE = 0x07A50069;

    /** (07A5,xxAE) VR=IS VM=1 ? */
    public static final int _07A5_xxAE_ = 0x07A500AE;

    /** (07A5,xxC8) VR=CS VM=1 ? */
    public static final int _07A5_xxC8_ = 0x07A500C8;

    /** (5001,xx19) VR=FL VM=1-n ? */
    public static final int _5001_xx19_ = 0x50010019;

    /** (5001,xx70) VR=US VM=1 ? */
    public static final int _5001_xx70_ = 0x50010070;

    /** (5001,xx71) VR=US VM=3 ? */
    public static final int _5001_xx71_ = 0x50010071;

    /** (5003,xx19) VR=FL VM=1-n ? */
    public static final int _5003_xx19_ = 0x50030019;

    /** (5005,xx10) VR=ST VM=1 ? */
    public static final int _5005_xx10_ = 0x50050010;

    /** (5005,xx19) VR=FL VM=1-n ? */
    public static final int _5005_xx19_ = 0x50050019;

    /** (6001,xx10) VR=CS VM=1 Text Overlay Flag */
    public static final int TextOverlayFlag = 0x60010010;

    /** (6001,xx30) VR=FL VM=4 ? */
    public static final int _6001_xx30_ = 0x60010030;

    /** (6001,xx90) VR=LO VM=1 ? */
    public static final int _6001_xx90_ = 0x60010090;

    /** (7FDF,xxF0) VR=OB VM=1 ? */
    public static final int _7FDF_xxF0_ = 0x7FDF00F0;

    /** (7FDF,xxFF) VR=SH VM=1 ? */
    public static final int _7FDF_xxFF_ = 0x7FDF00FF;

}
