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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_001;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Philips MR Imaging DD 001";

    /** (2005,xx00) VR=FL VM=1 Image Angulation AP */
    public static final int ImageAngulationAP = 0x20050000;

    /** (2005,xx01) VR=FL VM=1 Image Angulation FH */
    public static final int ImageAngulationFH = 0x20050001;

    /** (2005,xx02) VR=FL VM=1 Image Angulation RL */
    public static final int ImageAngulationRL = 0x20050002;

    /** (2005,xx03) VR=IS VM=1 Image Annotation Count */
    public static final int ImageAnnotationCount = 0x20050003;

    /** (2005,xx04) VR=CS VM=1 Image Display Orientation */
    public static final int ImageDisplayOrientation = 0x20050004;

    /** (2005,xx05) VR=CS VM=1 Synergy Reconstruction Type */
    public static final int SynergyReconstructionType = 0x20050005;

    /** (2005,xx07) VR=IS VM=1 Image Line Count */
    public static final int ImageLineCount = 0x20050007;

    /** (2005,xx08) VR=FL VM=1 Image Offcenter AP */
    public static final int ImageOffcenterAP = 0x20050008;

    /** (2005,xx09) VR=FL VM=1 Image Offcenter FH */
    public static final int ImageOffcenterFH = 0x20050009;

    /** (2005,xx0A) VR=FL VM=1 Image OffCentre RL */
    public static final int ImageOffCentreRL = 0x2005000A;

    /** (2005,xx0B) VR=FL VM=1 Max FP */
    public static final int MaxFP = 0x2005000B;

    /** (2005,xx0C) VR=FL VM=1 Min FP */
    public static final int MinFP = 0x2005000C;

    /** (2005,xx0D) VR=FL VM=1 Scale Intercept */
    public static final int ScaleIntercept = 0x2005000D;

    /** (2005,xx0E) VR=FL VM=1 Scale Slope */
    public static final int ScaleSlope = 0x2005000E;

    /** (2005,xx0F) VR=DS VM=1 Window Center */
    public static final int WindowCenter = 0x2005000F;

    /** (2005,xx10) VR=DS VM=1 Window Width */
    public static final int WindowWidth = 0x20050010;

    /** (2005,xx11) VR=CS VM=1-n Image Type */
    public static final int ImageType = 0x20050011;

    /** (2005,xx12) VR=CS VM=1 Cardiac Gating */
    public static final int CardiacGating = 0x20050012;

    /** (2005,xx13) VR=CS VM=1 Development Mode */
    public static final int DevelopmentMode = 0x20050013;

    /** (2005,xx14) VR=CS VM=1 Diffusion */
    public static final int Diffusion = 0x20050014;

    /** (2005,xx15) VR=CS VM=1 Fat Saturation */
    public static final int FatSaturation = 0x20050015;

    /** (2005,xx16) VR=CS VM=1 Flow Compensation */
    public static final int FlowCompensation = 0x20050016;

    /** (2005,xx17) VR=CS VM=1 Fourier Interpolation */
    public static final int FourierInterpolation = 0x20050017;

    /** (2005,xx18) VR=LO VM=1 Hardcopy Protocol */
    public static final int HardcopyProtocol = 0x20050018;

    /** (2005,xx19) VR=CS VM=1 Inverse Reconstructed */
    public static final int InverseReconstructed = 0x20050019;

    /** (2005,xx1A) VR=SS VM=1 Label Syntax */
    public static final int LabelSyntax = 0x2005001A;

    /** (2005,xx1B) VR=CS VM=1 Magnetization Prepared */
    public static final int MagnetizationPrepared = 0x2005001B;

    /** (2005,xx1C) VR=CS VM=1 Magnetization Transfer Contrast */
    public static final int MagnetizationTransferContrast = 0x2005001C;

    /** (2005,xx1D) VR=SS VM=1 Measurement Scan Resolution */
    public static final int MeasurementScanResolution = 0x2005001D;

    /** (2005,xx1E) VR=SH VM=1 MIP Protocol */
    public static final int MIPProtocol = 0x2005001E;

    /** (2005,xx1F) VR=SH VM=1 MPR Protocol */
    public static final int MPRProtocol = 0x2005001F;

    /** (2005,xx20) VR=SL VM=1 Number of Chemical Shifts */
    public static final int NumberOfChemicalShifts = 0x20050020;

    /** (2005,xx21) VR=SS VM=1 Number of Mixes */
    public static final int NumberOfMixes = 0x20050021;

    /** (2005,xx22) VR=IS VM=1 Number of References */
    public static final int NumberOfReferences = 0x20050022;

    /** (2005,xx23) VR=SS VM=1 Number of Slabs */
    public static final int NumberOfSlabs = 0x20050023;

    /** (2005,xx25) VR=SS VM=1 Number of Volumes */
    public static final int NumberOfVolumes = 0x20050025;

    /** (2005,xx26) VR=CS VM=1 Over Sampling Phase */
    public static final int OverSamplingPhase = 0x20050026;

    /** (2005,xx27) VR=CS VM=1 Package Mode */
    public static final int PackageMode = 0x20050027;

    /** (2005,xx28) VR=CS VM=1 Partial Fourier Frequency */
    public static final int PartialFourierFrequency = 0x20050028;

    /** (2005,xx29) VR=CS VM=1 PartialFourierPhase */
    public static final int PartialFourierPhase = 0x20050029;

    /** (2005,xx2A) VR=IS VM=1 Patient Reference ID */
    public static final int PatientReferenceID = 0x2005002A;

    /** (2005,xx2B) VR=SS VM=1 Percent Scan Complete */
    public static final int PercentScanComplete = 0x2005002B;

    /** (2005,xx2C) VR=CS VM=1 Phase Encode Reordering */
    public static final int PhaseEncodeReordering = 0x2005002C;

    /** (2005,xx2D) VR=IS VM=1 PlanScan Survey Number of Images */
    public static final int PlanScanSurveyNumberOfImages = 0x2005002D;

    /** (2005,xx2E) VR=CS VM=1 PPG PPU Gating */
    public static final int PPGPPUGating = 0x2005002E;

    /** (2005,xx2F) VR=CS VM=1 Spatial Presaturation */
    public static final int SpatialPresaturation = 0x2005002F;

    /** (2005,xx30) VR=FL VM=1-n Repetition Time */
    public static final int RepetitionTime = 0x20050030;

    /** (2005,xx31) VR=CS VM=1 Respiratory Gating */
    public static final int RespiratoryGating = 0x20050031;

    /** (2005,xx32) VR=CS VM=1 Sample Representation */
    public static final int SampleRepresentation = 0x20050032;

    /** (2005,xx33) VR=FL VM=1 Acquisition Duration */
    public static final int AcquisitionDuration = 0x20050033;

    /** (2005,xx34) VR=CS VM=1 Segmented KSpace */
    public static final int SegmentedKSpace = 0x20050034;

    /** (2005,xx35) VR=CS VM=1 Data Type */
    public static final int DataType = 0x20050035;

    /** (2005,xx36) VR=CS VM=1 Is Cardiac */
    public static final int IsCardiac = 0x20050036;

    /** (2005,xx37) VR=CS VM=1 Is Spectro */
    public static final int IsSpectro = 0x20050037;

    /** (2005,xx38) VR=CS VM=1 Spoiled */
    public static final int Spoiled = 0x20050038;

    /** (2005,xx39) VR=CS VM=1 Steady State */
    public static final int SteadyState = 0x20050039;

    /** (2005,xx3A) VR=SH VM=1 Sub Anatomy */
    public static final int SubAnatomy = 0x2005003A;

    /** (2005,xx3B) VR=CS VM=1 Time Reversed Steady State */
    public static final int TimeReversedSteadyState = 0x2005003B;

    /**
     * (2005,xx3C) VR=CS VM=1 Tilt Optimized Nonsaturated Excitation
     */
    public static final int TiltOptimizedNonsaturatedExcitation = 0x2005003C;

    /** (2005,xx3D) VR=SS VM=1 Number of RR Interval Ranges */
    public static final int NumberOfRRIntervalRanges = 0x2005003D;

    /** (2005,xx3E) VR=SL VM=1-n RR Intervals Distribution */
    public static final int RRIntervalsDistribution = 0x2005003E;

    /** (2005,xx3F) VR=SL VM=1 PlanScan Acquisition Number */
    public static final int PlanScanAcquisitionNumber = 0x2005003F;

    /**
     * (2005,xx40) VR=SL VM=1-n PlanScan Survey Chemical Shift Number
     */
    public static final int PlanScanSurveyChemicalShiftNumber = 0x20050040;

    /**
     * (2005,xx41) VR=SL VM=1-n PlanScan Survey Dynamic Scan Number
     */
    public static final int PlanScanSurveyDynamicScanNumber = 0x20050041;

    /** (2005,xx42) VR=SL VM=1-n PlanScan Survey Echo Number */
    public static final int PlanScanSurveyEchoNumber = 0x20050042;

    /** (2005,xx43) VR=CS VM=1-n PlanScan Survey Image Type */
    public static final int PlanScanSurveyImageType = 0x20050043;

    /** (2005,xx44) VR=SL VM=1-n PlanScan Survey Phase Number */
    public static final int PlanScanSurveyPhaseNumber = 0x20050044;

    /**
     * (2005,xx45) VR=SL VM=1-n PlanScan Survey Reconstruction Number
     */
    public static final int PlanScanSurveyReconstructionNumber = 0x20050045;

    /** (2005,xx46) VR=CS VM=1-n PlanScan Survey Scanning Sequence */
    public static final int PlanScanSurveyScanningSequence = 0x20050046;

    /** (2005,xx47) VR=SL VM=1-n PlanScan Survey Slice Number */
    public static final int PlanScanSurveyCSliceNumber = 0x20050047;

    /** (2005,xx48) VR=IS VM=1-n Referenced Acquisition Number */
    public static final int ReferencedAcquisitionNumber = 0x20050048;

    /** (2005,xx49) VR=IS VM=1-n Referenced Chemical Shift Number */
    public static final int ReferencedChemicalShiftNumber = 0x20050049;

    /** (2005,xx4A) VR=IS VM=1-n Referenced Dynamic Scan Number */
    public static final int ReferenceDynamicScanNumber = 0x2005004A;

    /** (2005,xx4B) VR=IS VM=1-n Referenced Echo Number */
    public static final int ReferencedEchoNumber = 0x2005004B;

    /** (2005,xx4C) VR=CS VM=1-n Referenced Entity */
    public static final int ReferencedEntity = 0x2005004C;

    /** (2005,xx4D) VR=CS VM=1-n Referenced Image Type */
    public static final int ReferencedImageType = 0x2005004D;

    /** (2005,xx4E) VR=FL VM=1-n Slab FOV RL */
    public static final int SlabFOVRL = 0x2005004E;

    /** (2005,xx4F) VR=FL VM=1-n Slab Offcentre AP */
    public static final int SlabOffcentreAP = 0x2005004F;

    /** (2005,xx50) VR=FL VM=1-n Slab Offcentre FH */
    public static final int SlabOffcentreFH = 0x20050050;

    /** (2005,xx51) VR=FL VM=1-n Slab Offcentre RL */
    public static final int SlabOffcentreRL = 0x20050051;

    /** (2005,xx52) VR=CS VM=1-n Slab Type */
    public static final int SlabType = 0x20050052;

    /** (2005,xx53) VR=CS VM=1-n Slab View Axis */
    public static final int SlabViewAxis = 0x20050053;

    /** (2005,xx54) VR=FL VM=1-n Volume Angulation AP */
    public static final int VolumeAngulationAP = 0x20050054;

    /** (2005,xx55) VR=FL VM=1-n Volume Angulation FH */
    public static final int VolumeAngulationFH = 0x20050055;

    /** (2005,xx56) VR=FL VM=1-n Volume Angulation RL */
    public static final int VolumeAngulationRL = 0x20050056;

    /** (2005,xx57) VR=FL VM=1-n Volume FOV AP */
    public static final int VolumeFOVAP = 0x20050057;

    /** (2005,xx58) VR=FL VM=1-n Volume FOV FH */
    public static final int VolumeFOVFH = 0x20050058;

    /** (2005,xx59) VR=FL VM=1-n Volume FOV RL */
    public static final int VolumeFOVRL = 0x20050059;

    /** (2005,xx5A) VR=FL VM=1-n Volume Offcentre AP */
    public static final int VolumeOffcentreAP = 0x2005005A;

    /** (2005,xx5B) VR=FL VM=1-n Volume Offcentre FH */
    public static final int VolumeOffcentreFH = 0x2005005B;

    /** (2005,xx5C) VR=FL VM=1-n Volume Offcentre RL */
    public static final int VolumeOffcentreRL = 0x2005005C;

    /** (2005,xx5D) VR=CS VM=1-n Volume Type */
    public static final int VolumeType = 0x2005005D;

    /** (2005,xx5E) VR=CS VM=1-n Volume View Axis */
    public static final int VolumeViewAxis = 0x2005005E;

    /** (2005,xx5F) VR=CS VM=1 Study Origin */
    public static final int StudyOrigin = 0x2005005F;

    /** (2005,xx60) VR=IS VM=1 Study Sequence Number */
    public static final int StudySequenceNumber = 0x20050060;

    /** (2005,xx61) VR=CS VM=1 Prepulse Type */
    public static final int PrepulseType = 0x20050061;

    /** (2005,xx63) VR=SS VM=1 fMRI Status Indication */
    public static final int fMRIStatusIndication = 0x20050063;

    /** (2005,xx64) VR=IS VM=1-n Reference Phase Number */
    public static final int ReferencePhaseNumber = 0x20050064;

    /** (2005,xx65) VR=IS VM=1-n Reference Reconstruction Number */
    public static final int ReferenceReconstructionNumber = 0x20050065;

    /** (2005,xx66) VR=CS VM=1-n Reference Scanning Sequence */
    public static final int ReferenceScanningSequence = 0x20050066;

    /** (2005,xx67) VR=IS VM=1-n Reference Slice Number */
    public static final int ReferenceSliceNumber = 0x20050067;

    /** (2005,xx68) VR=CS VM=1-n Reference Type */
    public static final int ReferenceType = 0x20050068;

    /** (2005,xx69) VR=FL VM=1-n Slab Angulation AP */
    public static final int SlabAngulationAP = 0x20050069;

    /** (2005,xx6A) VR=FL VM=1-n Slab Angulation FH */
    public static final int SlabAngulationFH = 0x2005006A;

    /** (2005,xx6B) VR=FL VM=1-n Slab Angulation RL */
    public static final int SlabAngulationRL = 0x2005006B;

    /** (2005,xx6C) VR=FL VM=1-n Slab FOV AP */
    public static final int SlabFOVAP = 0x2005006C;

    /** (2005,xx6D) VR=FL VM=1-n Slab FOV FH */
    public static final int SlabFOVFH = 0x2005006D;

    /** (2005,xx6E) VR=CS VM=1-n Scanning Sequence */
    public static final int ScanningSequence = 0x2005006E;

    /** (2005,xx6F) VR=CS VM=1 Acquisition Type */
    public static final int AcquisitionType = 0x2005006F;

    /** (2005,xx70) VR=LO VM=1 Hardcopy Protocol EasyVision */
    public static final int HardcopyProtocolEV = 0x20050070;

    /** (2005,xx71) VR=FL VM=1-n Stack Angulation AP */
    public static final int StackAngulationAP = 0x20050071;

    /** (2005,xx72) VR=FL VM=1-n Stack Angulation FH */
    public static final int StackAngulationFH = 0x20050072;

    /** (2005,xx73) VR=FL VM=1-n Stack Angulation RL */
    public static final int StackAngulationRL = 0x20050073;

    /** (2005,xx74) VR=FL VM=1-n Stack FOV AP */
    public static final int StackFOVAP = 0x20050074;

    /** (2005,xx75) VR=FL VM=1-n Stack FOV FH */
    public static final int StackFOVFH = 0x20050075;

    /** (2005,xx76) VR=FL VM=1-n Stack FOV RL */
    public static final int StackFOVRL = 0x20050076;

    /** (2005,xx78) VR=FL VM=1-n Stack Offcentre AP */
    public static final int StackOffcentreAP = 0x20050078;

    /** (2005,xx79) VR=FL VM=1-n Stack Offcentre FH */
    public static final int StackOffcentreFH = 0x20050079;

    /** (2005,xx7A) VR=FL VM=1-n Stack Offcentre RL */
    public static final int StackOffcentreRL = 0x2005007A;

    /** (2005,xx7B) VR=CS VM=1-n Stack Preparation Direction */
    public static final int StackPreparationDirection = 0x2005007B;

    /** (2005,xx7E) VR=FL VM=1-n Stack Slice Distance */
    public static final int StackSliceDistance = 0x2005007E;

    /** (2005,xx80) VR=SQ VM=1 Series PlanScan */
    public static final int SeriesPlanScan = 0x20050080;

    /** (2005,xx81) VR=CS VM=1-n Stack View Axis */
    public static final int StackViewAxis = 0x20050081;

    /** (2005,xx83) VR=SQ VM=1 Series Slab */
    public static final int SeriesSlab = 0x20050083;

    /** (2005,xx84) VR=SQ VM=1 Series Reference */
    public static final int SeriesReference = 0x20050084;

    /** (2005,xx85) VR=SQ VM=1 Series Volume */
    public static final int SeriesVolume = 0x20050085;

    /** (2005,xx86) VR=SS VM=1 Number of Geometry */
    public static final int NumberOfGeometry = 0x20050086;

    /** (2005,xx87) VR=SL VM=1-n Number of Geometry Slices */
    public static final int NumberOfGeometrySlices = 0x20050087;

    /** (2005,xx88) VR=FL VM=1-n Geom Angulation AP */
    public static final int GeomAngulationAP = 0x20050088;

    /** (2005,xx89) VR=FL VM=1-n Geom Angulation FH */
    public static final int GeomAngulationFH = 0x20050089;

    /** (2005,xx8A) VR=FL VM=1-n Geom Angulation RL */
    public static final int GeomAngulationRL = 0x2005008A;

    /** (2005,xx8B) VR=FL VM=1-n Geom FOV AP */
    public static final int GeomFOVAP = 0x2005008B;

    /** (2005,xx8C) VR=FL VM=1-n Geom FOV FH */
    public static final int GeomFOVFH = 0x2005008C;

    /** (2005,xx8D) VR=FL VM=1-n Geom FOV RL */
    public static final int GeomFOVRL = 0x2005008D;

    /** (2005,xx8E) VR=FL VM=1-n Geom OffCentre AP */
    public static final int GeomOffCentreAP = 0x2005008E;

    /** (2005,xx8F) VR=FL VM=1-n Geom OffCentre FH */
    public static final int GeomOffCentreFH = 0x2005008F;

    /** (2005,xx90) VR=FL VM=1-n Geom OffCentre RL */
    public static final int GeomOffCentreRL = 0x20050090;

    /** (2005,xx91) VR=CS VM=1-n Geom Preparation Direct */
    public static final int GeomPreparationDirect = 0x20050091;

    /** (2005,xx92) VR=FL VM=1-n Geom Radial Angle */
    public static final int GeomRadialAngle = 0x20050092;

    /** (2005,xx93) VR=CS VM=1-n Geom Radial Axis */
    public static final int GeomRadialAxis = 0x20050093;

    /** (2005,xx94) VR=FL VM=1-n Geom Slice Distance */
    public static final int GeomSliceDistance = 0x20050094;

    /** (2005,xx95) VR=SL VM=1-n Geom Slice Number */
    public static final int GeomSliceNumber = 0x20050095;

    /** (2005,xx96) VR=CS VM=1-n Geom Type */
    public static final int GeomType = 0x20050096;

    /** (2005,xx97) VR=CS VM=1-n Geom ViewA xis */
    public static final int GeomViewAxis = 0x20050097;

    /** (2005,xx98) VR=CS VM=1-n Geom Colour */
    public static final int GeomColour = 0x20050098;

    /** (2005,xx99) VR=CS VM=1-n Geom Application Type */
    public static final int GeomApplicationType = 0x20050099;

    /** (2005,xx9A) VR=SL VM=1-n Geom Id */
    public static final int GeomId = 0x2005009A;

    /** (2005,xx9B) VR=SH VM=1-n Geom Application Name */
    public static final int GeomApplicationName = 0x2005009B;

    /** (2005,xx9C) VR=SH VM=1-n Geom Label Name */
    public static final int GeomLabelName = 0x2005009C;

    /** (2005,xx9D) VR=CS VM=1-n Geom Line Style */
    public static final int GeomLineStyle = 0x2005009D;

    /** (2005,xx9E) VR=SQ VM=1 Series Geometry */
    public static final int SeriesGeometry = 0x2005009E;

    /**
     * (2005,xx9F) VR=CS VM=1 SeriesSpectral Selective Excitation Pulse
     */
    public static final int SpectralSelectiveExcitationPulse = 0x2005009F;

    /** (2005,xxA0) VR=FL VM=1 Dynamic Scan Begin Time */
    public static final int DynamicScanBeginTime = 0x200500A0;

    /** (2005,xxA1) VR=CS VM=1 Syncra Scan Type */
    public static final int SyncraScanType = 0x200500A1;

    /** (2005,xxA2) VR=CS VM=1 Is COCA */
    public static final int IsCOCA = 0x200500A2;

    /** (2005,xxA3) VR=IS VM=1 Stack Coil ID */
    public static final int StackCoilID = 0x200500A3;

    /** (2005,xxA4) VR=IS VM=1 Stack CBB Coil 1 */
    public static final int StackCBBCoil1 = 0x200500A4;

    /** (2005,xxA5) VR=IS VM=1 Stack CBB Coil 2 */
    public static final int StackCBBCoil2 = 0x200500A5;

    /** (2005,xxA6) VR=IS VM=1 Stack Channel Combination Bitmask */
    public static final int StackChannelCombi = 0x200500A6;

    /** (2005,xxA7) VR=CS VM=1 Stack Coil Connection */
    public static final int StackCoilConnection = 0x200500A7;

    /** (2005,xxA8) VR=DS VM=1 Inversion Time */
    public static final int InversionTime = 0x200500A8;

    /** (2005,xxA9) VR=CS VM=1 Geometry Correction */
    public static final int GeometryCorrection = 0x200500A9;

    /** (2005,xxB0) VR=FL VM=1 Diffusion Direction RL */
    public static final int DiffusionDirectionRL = 0x200500B0;

    /** (2005,xxB1) VR=FL VM=1 Diffusion Direction AP */
    public static final int DiffusionDirectionAP = 0x200500B1;

    /** (2005,xxB2) VR=FL VM=1 Diffusion Direction FH */
    public static final int DiffusionDirectionFH = 0x200500B2;

    /** (2005,xxC0) VR=CS VM=1 Scan Sequence */
    public static final int ScanSequence = 0x200500C0;

}
