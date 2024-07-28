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
package org.miaixz.bus.image.galaxy.dict.GEMS_ACQU_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_ACQU_01";

    /** (0009,xx24) VR=DS VM=1 ? */
    public static final int _0009_xx24_ = 0x00090024;

    /** (0009,xx25) VR=US VM=1 ? */
    public static final int _0009_xx25_ = 0x00090025;

    /** (0009,xx3E) VR=US VM=1 ? */
    public static final int _0009_xx3E_ = 0x0009003E;

    /** (0009,xx3F) VR=US VM=1 ? */
    public static final int _0009_xx3F_ = 0x0009003F;

    /** (0009,xx42) VR=US VM=1 ? */
    public static final int _0009_xx42_ = 0x00090042;

    /** (0009,xx43) VR=US VM=1 ? */
    public static final int _0009_xx43_ = 0x00090043;

    /** (0009,xxF8) VR=US VM=1 ? */
    public static final int _0009_xxF8_ = 0x000900F8;

    /** (0009,xxFB) VR=IS VM=1 ? */
    public static final int _0009_xxFB_ = 0x000900FB;

    /** (0019,xx01) VR=LT VM=1 ? */
    public static final int _0019_xx01_ = 0x00190001;

    /** (0019,xx02) VR=SL VM=1 Number Of Cells In Detector */
    public static final int NumberOfCellsInDetector = 0x00190002;

    /** (0019,xx03) VR=DS VM=1 Cell Number At Theta */
    public static final int CellNumberAtTheta = 0x00190003;

    /** (0019,xx04) VR=DS VM=1 Cell Spacing */
    public static final int CellSpacing = 0x00190004;

    /** (0019,xx05) VR=LT VM=1 ? */
    public static final int _0019_xx05_ = 0x00190005;

    /** (0019,xx06) VR=UN VM=1 ? */
    public static final int _0019_xx06_ = 0x00190006;

    /** (0019,xx0E) VR=US VM=1 ? */
    public static final int _0019_xx0E_ = 0x0019000E;

    /** (0019,xx0F) VR=DS VM=1 Horizontal Frame Of Reference */
    public static final int HorizontalFrameOfReference = 0x0019000F;

    /** (0019,xx11) VR=SS VM=1 Series Contrast */
    public static final int SeriesContrast = 0x00190011;

    /** (0019,xx12) VR=SS VM=1 Last Pseq */
    public static final int LastPseq = 0x00190012;

    /** (0019,xx13) VR=SS VM=1 Start Number For Baseline */
    public static final int StartNumberForBaseline = 0x00190013;

    /** (0019,xx14) VR=SS VM=1 End Number For Baseline */
    public static final int EndNumberForBaseline = 0x00190014;

    /** (0019,xx15) VR=SS VM=1 Start Number For Enhanced Scans */
    public static final int StartNumberForEnhancedScans = 0x00190015;

    /** (0019,xx16) VR=SS VM=1 End Number For Enhanced Scans */
    public static final int EndNumberForEnhancedScans = 0x00190016;

    /** (0019,xx17) VR=SS VM=1 Series Plane */
    public static final int SeriesPlane = 0x00190017;

    /** (0019,xx18) VR=LO VM=1 First Scan RAS */
    public static final int FirstScanRAS = 0x00190018;

    /** (0019,xx19) VR=DS VM=1 First Scan Location */
    public static final int FirstScanLocation = 0x00190019;

    /** (0019,xx1A) VR=LO VM=1 Last Scan RAS */
    public static final int LastScanRAS = 0x0019001A;

    /** (0019,xx1B) VR=DS VM=1 Last Scan Location */
    public static final int LastScanLocation = 0x0019001B;

    /** (0019,xx1E) VR=DS VM=1 Display Field Of View */
    public static final int DisplayFieldOfView = 0x0019001E;

    /** (0019,xx20) VR=DS VM=1 ? */
    public static final int _0019_xx20_ = 0x00190020;

    /** (0019,xx22) VR=DS VM=1 ? */
    public static final int _0019_xx22_ = 0x00190022;

    /** (0019,xx23) VR=DS VM=1 Table Speed [mm/rotation] */
    public static final int TableSpeed = 0x00190023;

    /** (0019,xx24) VR=DS VM=1 Mid Scan Time [sec] */
    public static final int MidScanTime = 0x00190024;

    /** (0019,xx25) VR=SS VM=1 Mid Scan Flag */
    public static final int MidScanFlag = 0x00190025;

    /** (0019,xx26) VR=SL VM=1 Tube Azimuth [degrees] */
    public static final int TubeAzimuth = 0x00190026;

    /** (0019,xx27) VR=DS VM=1 Rotation Speed (Gantry Period) */
    public static final int RotationSpeed = 0x00190027;

    /** (0019,xx2A) VR=DS VM=1 Xray On Position */
    public static final int XrayOnPosition = 0x0019002A;

    /** (0019,xx2B) VR=DS VM=1 Xray Off Position */
    public static final int XrayOffPosition = 0x0019002B;

    /** (0019,xx2C) VR=SL VM=1 Number of Triggers */
    public static final int NumberOfTriggers = 0x0019002C;

    /** (0019,xx2D) VR=US VM=1 ? */
    public static final int _0019_xx2D_ = 0x0019002D;

    /** (0019,xx2E) VR=DS VM=1 Angle Of First View */
    public static final int AngleOfFirstView = 0x0019002E;

    /** (0019,xx2F) VR=DS VM=1 Trigger Frequency */
    public static final int TriggerFrequency = 0x0019002F;

    /** (0019,xx39) VR=SS VM=1 Scan FOV Type */
    public static final int ScanFOVType = 0x00190039;

    /** (0019,xx3A) VR=IS VM=1 ? */
    public static final int _0019_xx3A_ = 0x0019003A;

    /** (0019,xx3B) VR=LT VM=1 ? */
    public static final int _0019_xx3B_ = 0x0019003B;

    /** (0019,xx3C) VR=UN VM=1 ? */
    public static final int _0019_xx3C_ = 0x0019003C;

    /** (0019,xx3E) VR=UN VM=1 ? */
    public static final int _0019_xx3E_ = 0x0019003E;

    /** (0019,xx3F) VR=UN VM=1 ? */
    public static final int _0019_xx3F_ = 0x0019003F;

    /** (0019,xx40) VR=SS VM=1 Stat Recon Flag */
    public static final int StatReconFlag = 0x00190040;

    /** (0019,xx41) VR=SS VM=1 Compute Type */
    public static final int ComputeType = 0x00190041;

    /** (0019,xx42) VR=SS VM=1 Segment Number */
    public static final int SegmentNumber = 0x00190042;

    /** (0019,xx43) VR=SS VM=1 Total Segments Required */
    public static final int TotalSegmentsRequired = 0x00190043;

    /** (0019,xx44) VR=DS VM=1 Interscan Delay */
    public static final int InterscanDelay = 0x00190044;

    /** (0019,xx47) VR=SS VM=1 View Compression Factor */
    public static final int ViewCompressionFactor = 0x00190047;

    /** (0019,xx48) VR=US VM=1 ? */
    public static final int _0019_xx48_ = 0x00190048;

    /** (0019,xx49) VR=US VM=1 ? */
    public static final int _0019_xx49_ = 0x00190049;

    /** (0019,xx4A) VR=SS VM=1 Total Number Of Ref Channels */
    public static final int TotalNumberOfRefChannels = 0x0019004A;

    /** (0019,xx4B) VR=SL VM=1 Data Size For Scan Data */
    public static final int DataSizeForScanData = 0x0019004B;

    /** (0019,xx52) VR=SS VM=1 Recon Post Processing Flag */
    public static final int ReconPostProcessingFlag = 0x00190052;

    /** (0019,xx54) VR=UN VM=1 ? */
    public static final int _0019_xx54_ = 0x00190054;

    /** (0019,xx57) VR=SS VM=1 CT Water Number */
    public static final int CTWaterNumber = 0x00190057;

    /** (0019,xx58) VR=SS VM=1 CT Bone Number */
    public static final int CTBoneNumber = 0x00190058;

    /** (0019,xx5A) VR=FL VM=1 Acquisition Duration */
    public static final int AcquisitionDuration = 0x0019005A;

    /** (0019,xx5D) VR=US VM=1 ? */
    public static final int _0019_xx5D_ = 0x0019005D;

    /** (0019,xx5E) VR=SL VM=1 Number Of Channels 1 To 512 */
    public static final int NumberOfChannels1To512 = 0x0019005E;

    /** (0019,xx5F) VR=SL VM=1 Increment Between Channels */
    public static final int IncrementBetweenChannels = 0x0019005F;

    /** (0019,xx60) VR=SL VM=1 Starting View */
    public static final int StartingView = 0x00190060;

    /** (0019,xx61) VR=SL VM=1 Number Of Views */
    public static final int NumberOfViews = 0x00190061;

    /** (0019,xx62) VR=SL VM=1 Increment Between Views */
    public static final int IncrementBetweenViews = 0x00190062;

    /** (0019,xx6A) VR=SS VM=1 Dependant On Number Of Views Processed */
    public static final int DependantOnNumberOfViewsProcessed = 0x0019006A;

    /** (0019,xx6B) VR=SS VM=1 Field Of View In Detector Cells */
    public static final int FieldOfViewInDetectorCells = 0x0019006B;

    /** (0019,xx70) VR=SS VM=1 Value Of Back Projection Button */
    public static final int ValueOfBackProjectionButton = 0x00190070;

    /** (0019,xx71) VR=SS VM=1 Set If Fatq Estimates Were Used */
    public static final int SetIfFatqEstimatesWereUsed = 0x00190071;

    /** (0019,xx72) VR=DS VM=1 Z Channel Avg Over Views */
    public static final int ZChannelAvgOverViews = 0x00190072;

    /** (0019,xx73) VR=DS VM=1 Avg Of Left Ref Channels Over Views */
    public static final int AvgOfLeftRefChannelsOverViews = 0x00190073;

    /** (0019,xx74) VR=DS VM=1 Max Left Channel Over Views */
    public static final int MaxLeftChannelOverViews = 0x00190074;

    /** (0019,xx75) VR=DS VM=1 Avg Of Right Ref Channels Over Views */
    public static final int AvgOfRightRefChannelsOverViews = 0x00190075;

    /** (0019,xx76) VR=DS VM=1 Max Right Channel Over Views */
    public static final int MaxRightChannelOverViews = 0x00190076;

    /** (0019,xx7D) VR=DS VM=1 Second Echo */
    public static final int SecondEcho = 0x0019007D;

    /** (0019,xx7E) VR=SS VM=1 Number Of Echos */
    public static final int NumberOfEchos = 0x0019007E;

    /** (0019,xx7F) VR=DS VM=1 Table Delta */
    public static final int TableDelta = 0x0019007F;

    /** (0019,xx81) VR=SS VM=1 Contiguous */
    public static final int Contiguous = 0x00190081;

    /** (0019,xx82) VR=US VM=1 ? */
    public static final int _0019_xx82_ = 0x00190082;

    /** (0019,xx83) VR=DS VM=1 ? */
    public static final int _0019_xx83_ = 0x00190083;

    /** (0019,xx84) VR=DS VM=1 Peak SAR */
    public static final int PeakSAR = 0x00190084;

    /** (0019,xx85) VR=SS VM=1 Monitor SAR */
    public static final int MonitorSAR = 0x00190085;

    /** (0019,xx86) VR=US VM=1 ? */
    public static final int _0019_xx86_ = 0x00190086;

    /** (0019,xx87) VR=DS VM=1 Cardiac Repetition Time */
    public static final int CardiacRepetitionTime = 0x00190087;

    /** (0019,xx88) VR=SS VM=1 Images Per Cardiac Cycle */
    public static final int ImagesPerCardiacCycle = 0x00190088;

    /** (0019,xx8A) VR=SS VM=1 Actual Receive Gain Analog */
    public static final int ActualReceiveGainAnalog = 0x0019008A;

    /** (0019,xx8B) VR=SS VM=1 Actual Receive Gain Digital */
    public static final int ActualReceiveGainDigital = 0x0019008B;

    /** (0019,xx8D) VR=DS VM=1 Delay After Trigger */
    public static final int DelayAfterTrigger = 0x0019008D;

    /** (0019,xx8F) VR=SS VM=1 Swap Phase Frequency */
    public static final int SwapPhaseFrequency = 0x0019008F;

    /** (0019,xx90) VR=SS VM=1 Pause Interval */
    public static final int PauseInterval = 0x00190090;

    /** (0019,xx91) VR=DS VM=1 Pause Time */
    public static final int PauseTime = 0x00190091;

    /** (0019,xx92) VR=SL VM=1 Slice Offset On Frequency Axis */
    public static final int SliceOffsetOnFrequencyAxis = 0x00190092;

    /** (0019,xx93) VR=DS VM=1 Auto Prescan Center Frequency */
    public static final int AutoPrescanCenterFrequency = 0x00190093;

    /** (0019,xx94) VR=SS VM=1 Auto Prescan Transmit Gain */
    public static final int AutoPrescanTransmitGain = 0x00190094;

    /** (0019,xx95) VR=SS VM=1 Auto Prescan Analog Receiver Gain */
    public static final int AutoPrescanAnalogReceiverGain = 0x00190095;

    /** (0019,xx96) VR=SS VM=1 Auto Prescan Digital Receiver Gain */
    public static final int AutoPrescanDigitalReceiverGain = 0x00190096;

    /** (0019,xx97) VR=SL VM=1 Bitmap Defining CVs */
    public static final int BitmapDefiningCVs = 0x00190097;

    /** (0019,xx98) VR=SS VM=1 Center Frequency Method */
    public static final int CenterFrequencyMethod = 0x00190098;

    /** (0019,xx99) VR=US VM=1 ? */
    public static final int _0019_xx99_ = 0x00190099;

    /** (0019,xx9B) VR=SS VM=1 Pulse Sequence Mode */
    public static final int PulseSequenceMode = 0x0019009B;

    /** (0019,xx9C) VR=LO VM=1 Pulse Sequence Name */
    public static final int PulseSequenceName = 0x0019009C;

    /** (0019,xx9D) VR=DT VM=1 Pulse Sequence Date */
    public static final int PulseSequenceDate = 0x0019009D;

    /** (0019,xx9E) VR=LO VM=1 Internal Pulse Sequence Name */
    public static final int InternalPulseSequenceName = 0x0019009E;

    /** (0019,xx9F) VR=SS VM=1 Transmitting Coil */
    public static final int TransmittingCoil = 0x0019009F;

    /** (0019,xxA0) VR=SS VM=1 Surface Coil Type */
    public static final int SurfaceCoilType = 0x001900A0;

    /** (0019,xxA1) VR=SS VM=1 Extremity Coil Flag */
    public static final int ExtremityCoilFlag = 0x001900A1;

    /** (0019,xxA2) VR=SL VM=1 Raw Data Run Number */
    public static final int RawDataRunNumber = 0x001900A2;

    /** (0019,xxA3) VR=UL VM=1 Calibrated Field Strength */
    public static final int CalibratedFieldStrength = 0x001900A3;

    /** (0019,xxA4) VR=SS VM=1 SAT Fat Water Bone */
    public static final int SATFatWaterBone = 0x001900A4;

    /** (0019,xxA5) VR=DS VM=1 Receive Bandwidth */
    public static final int ReceiveBandwidth = 0x001900A5;

    /** (0019,xxA7) VR=DS VM=1 User Data 0 */
    public static final int UserData0 = 0x001900A7;

    /** (0019,xxA8) VR=DS VM=1 User Data 1 */
    public static final int UserData1 = 0x001900A8;

    /** (0019,xxA9) VR=DS VM=1 User Data 2 */
    public static final int UserData2 = 0x001900A9;

    /** (0019,xxAA) VR=DS VM=1 User Data 3 */
    public static final int UserData3 = 0x001900AA;

    /** (0019,xxAB) VR=DS VM=1 User Data 4 */
    public static final int UserData4 = 0x001900AB;

    /** (0019,xxAC) VR=DS VM=1 User Data 5 */
    public static final int UserData5 = 0x001900AC;

    /** (0019,xxAD) VR=DS VM=1 User Data 6 */
    public static final int UserData6 = 0x001900AD;

    /** (0019,xxAE) VR=DS VM=1 User Data 7 */
    public static final int UserData7 = 0x001900AE;

    /** (0019,xxAF) VR=DS VM=1 User Data 8 */
    public static final int UserData8 = 0x001900AF;

    /** (0019,xxB0) VR=DS VM=1 User Data 9 */
    public static final int UserData9 = 0x001900B0;

    /** (0019,xxB1) VR=DS VM=1 User Data 10 */
    public static final int UserData10 = 0x001900B1;

    /** (0019,xxB2) VR=DS VM=1 User Data 11 */
    public static final int UserData11 = 0x001900B2;

    /** (0019,xxB3) VR=DS VM=1 User Data 12 */
    public static final int UserData12 = 0x001900B3;

    /** (0019,xxB4) VR=DS VM=1 User Data 13 */
    public static final int UserData13 = 0x001900B4;

    /** (0019,xxB5) VR=DS VM=1 User Data 14 */
    public static final int UserData14 = 0x001900B5;

    /** (0019,xxB6) VR=DS VM=1 User Data 15 */
    public static final int UserData15 = 0x001900B6;

    /** (0019,xxB7) VR=DS VM=1 User Data 16 */
    public static final int UserData16 = 0x001900B7;

    /** (0019,xxB8) VR=DS VM=1 User Data 17 */
    public static final int UserData17 = 0x001900B8;

    /** (0019,xxB9) VR=DS VM=1 User Data 18 */
    public static final int UserData18 = 0x001900B9;

    /** (0019,xxBA) VR=DS VM=1 User Data 19 */
    public static final int UserData19 = 0x001900BA;

    /** (0019,xxBB) VR=DS VM=1 User Data 20 */
    public static final int UserData20 = 0x001900BB;

    /** (0019,xxBC) VR=DS VM=1 User Data 21 */
    public static final int UserData21 = 0x001900BC;

    /** (0019,xxBD) VR=DS VM=1 User Data 22 */
    public static final int UserData22 = 0x001900BD;

    /** (0019,xxBE) VR=DS VM=1 Projection Angle */
    public static final int ProjectionAngle = 0x001900BE;

    /** (0019,xxC0) VR=SS VM=1 Saturation Planes */
    public static final int SaturationPlanes = 0x001900C0;

    /** (0019,xxC1) VR=SS VM=1 Surface Coil Intensity Correction Flag */
    public static final int SurfaceCoilIntensityCorrectionFlag = 0x001900C1;

    /** (0019,xxC2) VR=SS VM=1 SAT Location R */
    public static final int SATLocationR = 0x001900C2;

    /** (0019,xxC3) VR=SS VM=1 SAT Location L */
    public static final int SATLocationL = 0x001900C3;

    /** (0019,xxC4) VR=SS VM=1 SAT Location A */
    public static final int SATLocationA = 0x001900C4;

    /** (0019,xxC5) VR=SS VM=1 SAT Location P */
    public static final int SATLocationP = 0x001900C5;

    /** (0019,xxC6) VR=SS VM=1 SAT Location H */
    public static final int SATLocationH = 0x001900C6;

    /** (0019,xxC7) VR=SS VM=1 SAT Location F */
    public static final int SATLocationF = 0x001900C7;

    /** (0019,xxC8) VR=SS VM=1 SAT Thickness R L */
    public static final int SATThicknessRL = 0x001900C8;

    /** (0019,xxC9) VR=SS VM=1 SAT Thickness A P */
    public static final int SATThicknessAP = 0x001900C9;

    /** (0019,xxCA) VR=SS VM=1 SAT Thickness H F */
    public static final int SATThicknessHF = 0x001900CA;

    /** (0019,xxCB) VR=SS VM=1 PhaseContrast Flow Axis */
    public static final int PhaseContrastFlowAxis = 0x001900CB;

    /** (0019,xxCC) VR=SS VM=1 Velocity Encoding */
    public static final int VelocityEncoding = 0x001900CC;

    /** (0019,xxCD) VR=SS VM=1 Thickness Disclaimer */
    public static final int ThicknessDisclaimer = 0x001900CD;

    /** (0019,xxCE) VR=SS VM=1 Prescan Type */
    public static final int PrescanType = 0x001900CE;

    /** (0019,xxCF) VR=SS VM=1 Prescan Status */
    public static final int PrescanStatus = 0x001900CF;

    /** (0019,xxD0) VR=SH VM=1 Raw Data Type */
    public static final int RawDataType = 0x001900D0;

    /** (0019,xxD2) VR=SS VM=1 Projection Algorithm */
    public static final int ProjectionAlgorithm = 0x001900D2;

    /** (0019,xxD3) VR=SH VM=1 Projection Algorithm Name */
    public static final int ProjectionAlgorithmName = 0x001900D3;

    /** (0019,xxD4) VR=US VM=1 ? */
    public static final int _0019_xxD4_ = 0x001900D4;

    /** (0019,xxD5) VR=SS VM=1 Fractional Echo */
    public static final int FractionalEcho = 0x001900D5;

    /** (0019,xxD6) VR=SS VM=1 Prep Pulse */
    public static final int PrepPulse = 0x001900D6;

    /** (0019,xxD7) VR=SS VM=1 Cardiac Phases */
    public static final int CardiacPhases = 0x001900D7;

    /** (0019,xxD8) VR=SS VM=1 Variable Echo Flag */
    public static final int VariableEchoFlag = 0x001900D8;

    /** (0019,xxD9) VR=DS VM=1 Concatenated Sat or DTI Diffusion Direction */
    public static final int ConcatenatedSatOrDTIDiffusionDirection = 0x001900D9;

    /** (0019,xxDA) VR=SS VM=1 Reference Channel Used */
    public static final int ReferenceChannelUsed = 0x001900DA;

    /** (0019,xxDB) VR=DS VM=1 Back Projector Coefficient */
    public static final int BackProjectorCoefficient = 0x001900DB;

    /** (0019,xxDC) VR=SS VM=1 Primary Speed Correction Used */
    public static final int PrimarySpeedCorrectionUsed = 0x001900DC;

    /** (0019,xxDD) VR=SS VM=1 Overrange Correction Used */
    public static final int OverrangeCorrectionUsed = 0x001900DD;

    /** (0019,xxDE) VR=DS VM=1 Dynamic Z Alpha Value */
    public static final int DynamicZAlphaValue = 0x001900DE;

    /** (0019,xxDF) VR=DS VM=1 User Data 23 or DTI Diffusion Direction */
    public static final int UserData23 = 0x001900DF;

    /** (0019,xxE0) VR=DS VM=1 User Data 24 or DTI Diffusion Direction */
    public static final int UserData24 = 0x001900E0;

    /** (0019,xxE1) VR=DS VM=1 ? */
    public static final int _0019_xxE1_ = 0x001900E1;

    /** (0019,xxE2) VR=DS VM=1 Velocity Encode Scale */
    public static final int VelocityEncodeScale = 0x001900E2;

    /** (0019,xxE3) VR=LT VM=1 ? */
    public static final int _0019_xxE3_ = 0x001900E3;

    /** (0019,xxE4) VR=LT VM=1 ? */
    public static final int _0019_xxE4_ = 0x001900E4;

    /** (0019,xxE5) VR=IS VM=1 ? */
    public static final int _0019_xxE5_ = 0x001900E5;

    /** (0019,xxE6) VR=US VM=1 ? */
    public static final int _0019_xxE6_ = 0x001900E6;

    /** (0019,xxE8) VR=DS VM=1 ? */
    public static final int _0019_xxE8_ = 0x001900E8;

    /** (0019,xxE9) VR=DS VM=1 ? */
    public static final int _0019_xxE9_ = 0x001900E9;

    /** (0019,xxEB) VR=DS VM=1 ? */
    public static final int _0019_xxEB_ = 0x001900EB;

    /** (0019,xxEC) VR=US VM=1 ? */
    public static final int _0019_xxEC_ = 0x001900EC;

    /** (0019,xxF0) VR=UN VM=1 ? */
    public static final int _0019_xxF0_ = 0x001900F0;

    /** (0019,xxF1) VR=LT VM=1 ? */
    public static final int _0019_xxF1_ = 0x001900F1;

    /** (0019,xxF2) VR=SS VM=1 Fast Phases */
    public static final int FastPhases = 0x001900F2;

    /** (0019,xxF3) VR=LT VM=1 ? */
    public static final int _0019_xxF3_ = 0x001900F3;

    /** (0019,xxF4) VR=LT VM=1 ? */
    public static final int _0019_xxF4_ = 0x001900F4;

    /** (0019,xxF9) VR=DS VM=1 Transmit Gain */
    public static final int TransmitGain = 0x001900F9;

}
