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
package org.miaixz.bus.image.galaxy.dict.GEMS_PARM_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_PARM_01";

    /** (0043,xx01) VR=SS VM=1 Bitmap Of Prescan Options */
    public static final int BitmapOfPrescanOptions = 0x00430001;

    /** (0043,xx02) VR=SS VM=1 Gradient Offset In X */
    public static final int GradientOffsetInX = 0x00430002;

    /** (0043,xx03) VR=SS VM=1 Gradient Offset In Y */
    public static final int GradientOffsetInY = 0x00430003;

    /** (0043,xx04) VR=SS VM=1 Gradient Offset In Z */
    public static final int GradientOffsetInZ = 0x00430004;

    /** (0043,xx05) VR=SS VM=1 Image Is Original Or Unoriginal */
    public static final int ImageIsOriginalOrUnoriginal = 0x00430005;

    /** (0043,xx06) VR=SS VM=1 Number Of EPI Shots */
    public static final int NumberOfEPIShots = 0x00430006;

    /** (0043,xx07) VR=SS VM=1 Views Per Segment */
    public static final int ViewsPerSegment = 0x00430007;

    /** (0043,xx08) VR=SS VM=1 Respiratory Rate In BPM */
    public static final int RespiratoryRateInBPM = 0x00430008;

    /** (0043,xx09) VR=SS VM=1 Respiratory Trigger Point */
    public static final int RespiratoryTriggerPoint = 0x00430009;

    /** (0043,xx0A) VR=SS VM=1 Type Of Receiver Used */
    public static final int TypeOfReceiverUsed = 0x0043000A;

    /** (0043,xx0B) VR=DS VM=1 dB/dt Peak Rate Of Change Of Gradient Field */
    public static final int PeakRateOfChangeOfGradientField = 0x0043000B;

    /** (0043,xx0C) VR=DS VM=1 dB/dt Limits In Units Of Percent */
    public static final int LimitsInUnitsOfPercent = 0x0043000C;

    /** (0043,xx0D) VR=DS VM=1 PSD Estimated Limit */
    public static final int PSDEstimatedLimit = 0x0043000D;

    /** (0043,xx0E) VR=DS VM=1 PSD Estimated Limit In Tesla Per Second */
    public static final int PSDEstimatedLimitInTeslaPerSecond = 0x0043000E;

    /** (0043,xx0F) VR=DS VM=1 SAR Avg Head */
    public static final int SARAvgHead = 0x0043000F;

    /** (0043,xx10) VR=US VM=1 Window Value */
    public static final int WindowValue = 0x00430010;

    /** (0043,xx11) VR=US VM=1 Total Input Views */
    public static final int TotalInputViews = 0x00430011;

    /** (0043,xx12) VR=SS VM=3 Xray Chain */
    public static final int XrayChain = 0x00430012;

    /** (0043,xx13) VR=SS VM=5 Recon Kernel Parameters */
    public static final int ReconKernelParameters = 0x00430013;

    /** (0043,xx14) VR=SS VM=3 Calibration Parameters */
    public static final int CalibrationParameters = 0x00430014;

    /** (0043,xx15) VR=SS VM=3 Total Output Views */
    public static final int TotalOutputViews = 0x00430015;

    /** (0043,xx16) VR=SS VM=5 Number Of Overranges */
    public static final int NumberOfOverranges = 0x00430016;

    /** (0043,xx17) VR=DS VM=1 IBH Image Scale Factors */
    public static final int IBHImageScaleFactors = 0x00430017;

    /** (0043,xx18) VR=DS VM=3 BBH Coefficients */
    public static final int BBHCoefficients = 0x00430018;

    /** (0043,xx19) VR=SS VM=1 Number Of BBH Chains To Blend */
    public static final int NumberOfBBHChainsToBlend = 0x00430019;

    /** (0043,xx1A) VR=SL VM=1 Starting Channel Number */
    public static final int StartingChannelNumber = 0x0043001A;

    /** (0043,xx1B) VR=SS VM=1 PPScan Parameters */
    public static final int PPScanParameters = 0x0043001B;

    /** (0043,xx1C) VR=SS VM=1 GE Image Integrity */
    public static final int GEImageIntegrity = 0x0043001C;

    /** (0043,xx1D) VR=SS VM=1 Level Value */
    public static final int LevelValue = 0x0043001D;

    /** (0043,xx1E) VR=DS VM=1 Delta Start Time */
    public static final int DeltaStartTime = 0x0043001E;

    /** (0043,xx1F) VR=SL VM=1 Max Overranges In A View */
    public static final int MaxOverrangesInAView = 0x0043001F;

    /** (0043,xx20) VR=DS VM=1 Avg Overranges All Views */
    public static final int AvgOverrangesAllViews = 0x00430020;

    /** (0043,xx21) VR=SS VM=1 Corrected Afterglow Terms */
    public static final int CorrectedAfterglowTerms = 0x00430021;

    /** (0043,xx25) VR=SS VM=6 Reference Channels */
    public static final int ReferenceChannels = 0x00430025;

    /** (0043,xx26) VR=US VM=6 No Views Ref Channels Blocked */
    public static final int NoViewsRefChannelsBlocked = 0x00430026;

    /** (0043,xx27) VR=SH VM=1 Scan Pitch Ratio */
    public static final int ScanPitchRatio = 0x00430027;

    /** (0043,xx28) VR=OB VM=1 Unique Image Identifier */
    public static final int UniqueImageIdentifier = 0x00430028;

    /** (0043,xx29) VR=OB VM=1 Histogram Tables */
    public static final int HistogramTables = 0x00430029;

    /** (0043,xx2A) VR=OB VM=1 User Defined Data */
    public static final int UserDefinedData = 0x0043002A;

    /** (0043,xx2B) VR=SS VM=4 Private Scan Options */
    public static final int PrivateScanOptions = 0x0043002B;

    /** (0043,xx2C) VR=SS VM=1 Effective Echo Spacing */
    public static final int EffectiveEchoSpacing = 0x0043002C;

    /** (0043,xx2D) VR=SH VM=1 Filter Mode */
    public static final int FilterMode = 0x0043002D;

    /** (0043,xx2E) VR=SH VM=1 String Slop Field 2 */
    public static final int StringSlopField2 = 0x0043002E;

    /** (0043,xx2F) VR=SS VM=1 Image Type (Real,Imaginary,Phase,Magnitude) */
    public static final int ImageType = 0x0043002F;

    /** (0043,xx30) VR=SS VM=1 Vas Collapse Flag */
    public static final int VasCollapseFlag = 0x00430030;

    /** (0043,xx31) VR=DS VM=2 Recon Center Coordinates */
    public static final int ReconCenterCoordinates = 0x00430031;

    /** (0043,xx32) VR=SS VM=1 Vas Flags */
    public static final int VasFlags = 0x00430032;

    /** (0043,xx33) VR=FL VM=1 Neg Scan Spacing */
    public static final int NegScanSpacing = 0x00430033;

    /** (0043,xx34) VR=IS VM=1 Offset Frequency */
    public static final int OffsetFrequency = 0x00430034;

    /** (0043,xx35) VR=UL VM=1 User Usage Tag */
    public static final int UserUsageTag = 0x00430035;

    /** (0043,xx36) VR=UL VM=1 User Fill Map MSW */
    public static final int UserFillMapMSW = 0x00430036;

    /** (0043,xx37) VR=UL VM=1 User Fill Map LSW */
    public static final int UserFillMapLSW = 0x00430037;

    /** (0043,xx38) VR=FL VM=24 User 25 To User 48 */
    public static final int User25ToUser48 = 0x00430038;

    /** (0043,xx39) VR=IS VM=4 Slop Integer 6 To Slop Integer 9 */
    public static final int SlopInteger6ToSlopInteger9 = 0x00430039;

    /** (0043,xx40) VR=FL VM=4 Trigger On Position */
    public static final int TriggerOnPosition = 0x00430040;

    /** (0043,xx41) VR=FL VM=4 Degree Of Rotation */
    public static final int DegreeOfRotation = 0x00430041;

    /** (0043,xx42) VR=SL VM=4 DAS Trigger Source */
    public static final int DASTriggerSource = 0x00430042;

    /** (0043,xx43) VR=SL VM=4 DAS Fpa Gain */
    public static final int DASFpaGain = 0x00430043;

    /** (0043,xx44) VR=SL VM=4 DAS Output Source */
    public static final int DASOutputSource = 0x00430044;

    /** (0043,xx45) VR=SL VM=4 DAS Ad Input */
    public static final int DASAdInput = 0x00430045;

    /** (0043,xx46) VR=SL VM=4 DAS Cal Mode */
    public static final int DASCalMode = 0x00430046;

    /** (0043,xx47) VR=SL VM=4 DAS Cal Frequency */
    public static final int DASCalFrequency = 0x00430047;

    /** (0043,xx48) VR=SL VM=4 DAS Reg Xm */
    public static final int DASRegXm = 0x00430048;

    /** (0043,xx49) VR=SL VM=4 DAS Auto Zero */
    public static final int DASAutoZero = 0x00430049;

    /** (0043,xx4A) VR=SS VM=4 Starting Channel Of View */
    public static final int StartingChannelOfView = 0x0043004A;

    /** (0043,xx4B) VR=SL VM=4 DAS Xm Pattern */
    public static final int DASXmPattern = 0x0043004B;

    /** (0043,xx4C) VR=SS VM=4 TGGC Trigger Mode */
    public static final int TGGCTriggerMode = 0x0043004C;

    /** (0043,xx4D) VR=FL VM=4 Start Scan To Xray On Delay */
    public static final int StartScanToXrayOnDelay = 0x0043004D;

    /** (0043,xx4E) VR=FL VM=4 Duration Of Xray On */
    public static final int DurationOfXrayOn = 0x0043004E;

    /** (0043,xx60) VR=IS VM=8 Slop Integer 10 To Slop Integer 17 */
    public static final int SlopInteger10ToSlopInteger17 = 0x00430060;

    /** (0043,xx61) VR=UI VM=1 Scanner Study Entity UID */
    public static final int ScannerStudyEntityUID = 0x00430061;

    /** (0043,xx62) VR=SH VM=1 Scanner Study ID */
    public static final int ScannerStudyID = 0x00430062;

    /** (0043,xx63) VR=SH VM=1 Raw Data ID */
    public static final int RawDataID = 0x00430063;

    /** (0043,xx64) VR=CS VM=1-n Recon Filter */
    public static final int ReconFilter = 0x00430064;

    /** (0043,xx65) VR=US VM=1 Motion Correction Indicator */
    public static final int MotionCorrectionIndicator = 0x00430065;

    /** (0043,xx66) VR=US VM=1 Helical Correction Indicator */
    public static final int HelicalCorrectionIndicator = 0x00430066;

    /** (0043,xx67) VR=US VM=1 IBO Correction Indicator */
    public static final int IBOCorrectionIndicator = 0x00430067;

    /** (0043,xx68) VR=US VM=1 XT Correction Indicator */
    public static final int IXTCorrectionIndicator = 0x00430068;

    /** (0043,xx69) VR=US VM=1 Q-cal Correction Indicator */
    public static final int QcalCorrectionIndicator = 0x00430069;

    /** (0043,xx6A) VR=US VM=1 AV Correction Indicator */
    public static final int AVCorrectionIndicator = 0x0043006A;

    /** (0043,xx6B) VR=US VM=1 L-MDK Correction Indicator */
    public static final int LMDKCorrectionIndicator = 0x0043006B;

    /** (0043,xx6C) VR=IS VM=1 Detector Row */
    public static final int DetectorRow = 0x0043006C;

    /** (0043,xx6D) VR=US VM=1 Area Size */
    public static final int AreaSize = 0x0043006D;

    /** (0043,xx6E) VR=SH VM=1 Auto mA Mode */
    public static final int AutoMAMode = 0x0043006E;

    /** (0043,xx6F) VR=DS VM=3-4 Scanner Table Entry + Gradient Coil Selected */
    public static final int ScannerTableEntry = 0x0043006F;

    /** (0043,xx70) VR=LO VM=1 Paradigm Name */
    public static final int ParadigmName = 0x00430070;

    /** (0043,xx71) VR=ST VM=1 Paradigm Description */
    public static final int ParadigmDescription = 0x00430071;

    /** (0043,xx72) VR=UI VM=1 Paradigm UID */
    public static final int ParadigmUID = 0x00430072;

    /** (0043,xx73) VR=US VM=1 Experiment Type */
    public static final int ExperimentType = 0x00430073;

    /** (0043,xx74) VR=US VM=1 Number of Rest Volumes */
    public static final int NumberOfRestVolumes = 0x00430074;

    /** (0043,xx75) VR=US VM=1 Number of Active Volumes */
    public static final int NumberOfActiveVolumes = 0x00430075;

    /** (0043,xx76) VR=US VM=1 Number of Dummy Scans */
    public static final int NumberOfDummyScans = 0x00430076;

    /** (0043,xx77) VR=SH VM=1 Application Name */
    public static final int ApplicationName = 0x00430077;

    /** (0043,xx78) VR=SH VM=1 Application Version */
    public static final int ApplicationVersion = 0x00430078;

    /** (0043,xx79) VR=US VM=1 Slices Per Volume */
    public static final int SlicesPerVolume = 0x00430079;

    /** (0043,xx7A) VR=US VM=1 Expected Time Points */
    public static final int ExpectedTimePoints = 0x0043007A;

    /** (0043,xx7B) VR=FL VM=1-n Regressor Values */
    public static final int RegressorValues = 0x0043007B;

    /** (0043,xx7C) VR=FL VM=1 Delay After Slice Group */
    public static final int DelayAfterSliceGroup = 0x0043007C;

    /** (0043,xx7D) VR=US VM=1 Recon Mode Flag Word */
    public static final int ReconModeFlagWord = 0x0043007D;

    /** (0043,xx7E) VR=LO VM=1-n PACC Specific Information */
    public static final int PACCSpecificInformation = 0x0043007E;

    /** (0043,xx7F) VR=DS VM=1-n Reserved */
    public static final int Reserved = 0x0043007F;

    /** (0043,xx80) VR=LO VM=1-n Coil ID Data */
    public static final int CoilIDData = 0x00430080;

    /** (0043,xx81) VR=LO VM=1 GE Coil Name */
    public static final int GECoilName = 0x00430081;

    /** (0043,xx82) VR=LO VM=1-n System Configuration Information */
    public static final int SystemConfigurationInformation = 0x00430082;

    /** (0043,xx83) VR=DS VM=1-2 Asset R Factors */
    public static final int AssetRFactors = 0x00430083;

    /** (0043,xx84) VR=LO VM=5 Additional Asset Data */
    public static final int AdditionalAssetData = 0x00430084;

    /** (0043,xx85) VR=UT VM=1 Debug Data (Text Format) */
    public static final int DebugDataTextFormat = 0x00430085;

    /** (0043,xx86) VR=OB VM=1 Debug Data (Binary Format) */
    public static final int DebugDataBinaryFormat = 0x00430086;

    /** (0043,xx88) VR=UI VM=1 PURE Acquisition Calibration Series UID */
    public static final int PUREAcquisitionCalibrationSeriesUID = 0x00430088;

    /** (0043,xx89) VR=LO VM=3 Governing Body, dB/dt, and SAR definition */
    public static final int GoverningBodyDBdtSARDefinition = 0x00430089;

    /** (0043,xx8A) VR=CS VM=1 Private In-Plane Phase Encoding Direction */
    public static final int PrivateInPlanePhaseEncodingDirection = 0x0043008A;

    /** (0043,xx8B) VR=OB VM=1 FMRI Binary Data Block */
    public static final int FMRIBinaryDataBlock = 0x0043008B;

    /** (0043,xx8C) VR=DS VM=1 Voxel Location */
    public static final int VoxelLocation = 0x0043008C;

    /** (0043,xx8D) VR=DS VM=1-n SAT Band Locations */
    public static final int SATBandLocations = 0x0043008D;

    /** (0043,xx8E) VR=DS VM=3 Spectro Prescan Values */
    public static final int SpectroPrescanValues = 0x0043008E;

    /** (0043,xx8F) VR=DS VM=3 Spectro Parameters */
    public static final int SpectroParameters = 0x0043008F;

    /** (0043,xx90) VR=LO VM=1-n SAR Definition */
    public static final int SARDefinition = 0x00430090;

    /** (0043,xx91) VR=DS VM=1-n SAR Value */
    public static final int SARValue = 0x00430091;

    /** (0043,xx92) VR=LO VM=1 Image Error Text */
    public static final int ImageErrorText = 0x00430092;

    /** (0043,xx93) VR=DS VM=1-n Spectro Quantitation Values */
    public static final int SpectroQuantitationValues = 0x00430093;

    /** (0043,xx94) VR=DS VM=1-n Spectro Ratio Values */
    public static final int SpectroRatioValues = 0x00430094;

    /** (0043,xx95) VR=LO VM=1 Prescan Reuse String */
    public static final int PrescanReuseString = 0x00430095;

    /** (0043,xx96) VR=CS VM=1 Content Qualification */
    public static final int ContentQualification = 0x00430096;

    /** (0043,xx97) VR=LO VM=8 Image Filtering Parameters */
    public static final int ImageFilteringParameters = 0x00430097;

    /** (0043,xx98) VR=UI VM=1 ASSET Acquisition Calibration Series UID */
    public static final int ASSETAcquisitionCalibrationSeriesUID = 0x00430098;

    /** (0043,xx99) VR=LO VM=1-n Extended Options */
    public static final int ExtendedOptions = 0x00430099;

    /** (0043,xx9A) VR=IS VM=1 Rx Stack Identification */
    public static final int RxStackIdentification = 0x0043009A;

}
