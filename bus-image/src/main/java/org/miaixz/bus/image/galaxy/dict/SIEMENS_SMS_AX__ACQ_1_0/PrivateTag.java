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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__ACQ_1_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SMS-AX ACQ 1.0";

    /** (0021,xx00) VR=US VM=1 Acquisition Type */
    public static final int AcquisitionType = 0x00210000;

    /** (0021,xx01) VR=US VM=1 Acquisition Mode */
    public static final int AcquisitionMode = 0x00210001;

    /** (0021,xx02) VR=US VM=1 Footswitch Index */
    public static final int FootswitchIndex = 0x00210002;

    /** (0021,xx03) VR=US VM=1 Acquisition Room */
    public static final int AcquisitionRoom = 0x00210003;

    /** (0021,xx04) VR=SL VM=1 Current Time Product */
    public static final int CurrentTimeProduct = 0x00210004;

    /** (0021,xx05) VR=SL VM=1 Imager Receptor Dose */
    public static final int ImagerReceptorDose = 0x00210005;

    /** (0021,xx06) VR=SL VM=1 Skin Dose Percent */
    public static final int SkinDosePercent = 0x00210006;

    /** (0021,xx07) VR=SL VM=1 Skin Dose Accumulation */
    public static final int SkinDoseAccumulation = 0x00210007;

    /** (0021,xx08) VR=SL VM=1 Skin Dose Rate */
    public static final int SkinDoseRate = 0x00210008;

    /** (0021,xx09) VR=UL VM=1 Impac Filename */
    public static final int ImpacFilename = 0x00210009;

    /** (0021,xx0A) VR=UL VM=1 Copper Filter */
    public static final int CopperFilter = 0x0021000A;

    /** (0021,xx0B) VR=US VM=1 Measuring Field */
    public static final int MeasuringField = 0x0021000B;

    /** (0021,xx0C) VR=SS VM=3 Post Blanking Circle */
    public static final int PostBlankingCircle = 0x0021000C;

    /** (0021,xx0D) VR=SS VM=2-2n Dyna Angles */
    public static final int DynaAngles = 0x0021000D;

    /** (0021,xx0E) VR=SS VM=1 Total Steps */
    public static final int TotalSteps = 0x0021000E;

    /** (0021,xx0F) VR=SL VM=4-n Dyna X-Ray Info */
    public static final int DynaXRayInfo = 0x0021000F;

    /** (0021,xx10) VR=US VM=1 Modality LUT Input Gamma */
    public static final int ModalityLUTInputGamma = 0x00210010;

    /** (0021,xx11) VR=US VM=1 Modality LUT Output Gamma */
    public static final int ModalityLUTOutputGamma = 0x00210011;

    /** (0021,xx12) VR=OB VM=1 SH_STPAR */
    public static final int SHSTPAR = 0x00210012;

    /** (0021,xx13) VR=US VM=1 Acquisition Zoom */
    public static final int AcquisitionZoom = 0x00210013;

    /** (0021,xx14) VR=SS VM=1 Dyna Angulation Step */
    public static final int DynaAngulationStep = 0x00210014;

    /** (0021,xx15) VR=US VM=1 DDO Value */
    public static final int DDOValue = 0x00210015;

    /** (0021,xx16) VR=US VM=1 DR Single Flag */
    public static final int DRSingleFlag = 0x00210016;

    /** (0021,xx17) VR=SL VM=1 Source to Isocente */
    public static final int SourcetoIsocenter = 0x00210017;

    /** (0021,xx18) VR=US VM=1 Pressure Data */
    public static final int PressureData = 0x00210018;

    /** (0021,xx19) VR=SL VM=1 ECG Index Array */
    public static final int ECGIndexArray = 0x00210019;

    /** (0021,xx1A) VR=US VM=1 FD Flag */
    public static final int FDFlag = 0x0021001A;

    /** (0021,xx1B) VR=OB VM=1 SH_ZOOM */
    public static final int SHZOOM = 0x0021001B;

    /** (0021,xx1C) VR=OB VM=1 SH_COLPAR */
    public static final int SHCOLPAR = 0x0021001C;

    /** (0021,xx1D) VR=US VM=1 K-Factor */
    public static final int KFactor = 0x0021001D;

    /** (0021,xx1E) VR=US VM=8 EVE */
    public static final int EVE = 0x0021001E;

    /** (0021,xx1F) VR=SL VM=1 Total Scene Time */
    public static final int TotalSceneTime = 0x0021001F;

    /** (0021,xx20) VR=US VM=1 Restore Flag */
    public static final int RestoreFlag = 0x00210020;

    /** (0021,xx21) VR=US VM=1 Stand Movement Flag */
    public static final int StandMovementFlag = 0x00210021;

    /** (0021,xx22) VR=US VM=1 FD Rows */
    public static final int FDRows = 0x00210022;

    /** (0021,xx23) VR=US VM=1 FD Columns */
    public static final int FDColumns = 0x00210023;

    /** (0021,xx24) VR=US VM=1 Table Movement Flag */
    public static final int TableMovementFlag = 0x00210024;

    /** (0021,xx26) VR=DS VM=1 Crispy XPI Filter Value */
    public static final int CrispyXPIFilterValue = 0x00210026;

    /** (0021,xx27) VR=US VM=1 IC Stent Flag */
    public static final int ICStentFlag = 0x00210027;

    /** (0021,xx28) VR=SQ VM=1 Gamma LUT Sequence */
    public static final int GammaLUTSequence = 0x00210028;

    /** (0021,xx29) VR=DS VM=1 Acquisition Scene Time */
    public static final int AcquisitionSceneTime = 0x00210029;

    /** (0021,xx2A) VR=IS VM=1 3D Cardiac Phase Center */
    public static final int ThreeDCardiacPhaseCenter = 0x0021002A;

    /** (0021,xx2B) VR=IS VM=1 3D Cardiac Phase Width */
    public static final int ThreeDCardiacPhaseWidth = 0x0021002B;

    /** (0021,xx30) VR=OB VM=1 Organ Program Info */
    public static final int OrganProgramInfo = 0x00210030;

    /** (0021,xx3A) VR=IS VM=1 DDO Kernel size */
    public static final int DDOKernelsize = 0x0021003A;

    /** (0021,xx3B) VR=IS VM=1 mAs Modulation */
    public static final int mAsModulation = 0x0021003B;

    /** (0021,xx3C) VR=DT VM=1-n 3D R-Peak Reference Time */
    public static final int ThreeDRPeakReferenceTime = 0x0021003C;

    /** (0021,xx3D) VR=SL VM=1-n ECG Frame Time Vector */
    public static final int ECGFrameTimeVector = 0x0021003D;

    /** (0021,xx3E) VR=SL VM=1 ECG Start Time of Run */
    public static final int ECGStartTimeOfRun = 0x0021003E;

    /** (0021,xx40) VR=US VM=3 Gamma LUT Descriptor */
    public static final int GammaLUTDescriptor = 0x00210040;

    /** (0021,xx41) VR=LO VM=1 Gamma LUT Type */
    public static final int GammaLUTType = 0x00210041;

    /** (0021,xx42) VR=US VM=1-n Gamma LUT Data */
    public static final int GammaLUTData = 0x00210042;

    /** (0021,xx43) VR=US VM=1 Global Gain */
    public static final int GlobalGain = 0x00210043;

    /** (0021,xx44) VR=US VM=1 Global Offset */
    public static final int GlobalOffset = 0x00210044;

    /** (0021,xx45) VR=US VM=1 DIPP Mode */
    public static final int DIPPMode = 0x00210045;

    /** (0021,xx46) VR=US VM=1 Artis System Type */
    public static final int ArtisSystemType = 0x00210046;

    /** (0021,xx47) VR=US VM=1 Artis Table Type */
    public static final int ArtisTableType = 0x00210047;

    /** (0021,xx48) VR=US VM=1 Artis Table Top Type */
    public static final int ArtisTableTopType = 0x00210048;

    /** (0021,xx49) VR=US VM=1 Water Value */
    public static final int WaterValue = 0x00210049;

    /** (0021,xx51) VR=DS VM=1 3D Positioner Primary Start Angle */
    public static final int ThreeDPositionerPrimaryStartAngle = 0x00210051;

    /**
     * (0021,xx52) VR=DS VM=1 3D Positioner Secondary Start Angle
     */
    public static final int ThreeDPositionerSecondaryStartAngle = 0x00210052;

    /** (0021,xx53) VR=SS VM=3 Stand Position */
    public static final int StandPosition = 0x00210053;

    /** (0021,xx54) VR=SS VM=1 Rotation Angle */
    public static final int RotationAngle = 0x00210054;

    /** (0021,xx55) VR=US VM=1 Image Rotation */
    public static final int ImageRotation = 0x00210055;

    /** (0021,xx56) VR=SS VM=3 Table Coordinates */
    public static final int TableCoordinates = 0x00210056;

    /** (0021,xx57) VR=SS VM=3 Isocenter Table Position */
    public static final int IsocenterTablePosition = 0x00210057;

    /** (0021,xx58) VR=DS VM=1 Table Object Distance */
    public static final int TableObjectDistance = 0x00210058;

    /** (0021,xx59) VR=FL VM=12-n Carm Coordinate System */
    public static final int CarmCoordinateSystem = 0x00210059;

    /** (0021,xx5A) VR=FL VM=6-n Robot Axes */
    public static final int RobotAxes = 0x0021005A;

    /** (0021,xx5B) VR=FL VM=12 Table Coordinate System */
    public static final int TableCoordinateSystem = 0x0021005B;

    /** (0021,xx5C) VR=FL VM=12 Patient Coordinate System */
    public static final int PatientCoordinateSystem = 0x0021005C;

    /** (0021,xx5D) VR=SS VM=1-n Angulation */
    public static final int Angulation = 0x0021005D;

    /** (0021,xx5E) VR=SS VM=1-n Orbital */
    public static final int Orbital = 0x0021005E;

    /** (0021,xx61) VR=SS VM=1 Large Volume Overlap */
    public static final int LargeVolumeOverlap = 0x00210061;

    /** (0021,xx62) VR=US VM=1 Reconstruction Preset */
    public static final int ReconstructionPreset = 0x00210062;

    /** (0021,xx63) VR=SS VM=1 3D Start Angle */
    public static final int ThreeDStartAngle = 0x00210063;

    /** (0021,xx64) VR=SL VM=1 3D Planned Angle */
    public static final int ThreeDPlannedAngle = 0x00210064;

    /** (0021,xx65) VR=SL VM=1 3D Rotation Plane Alpha */
    public static final int ThreeDRotationPlaneAlpha = 0x00210065;

    /** (0021,xx66) VR=SL VM=1 3D Rotation Plane Beta */
    public static final int ThreeDRotationPlaneBeta = 0x00210066;

    /** (0021,xx67) VR=SL VM=1 3D First Image Angle */
    public static final int ThreeDFirstImageAngle = 0x00210067;

    /** (0021,xx68) VR=SS VM=1-n 3D Trigger Angle */
    public static final int ThreeDTriggerAngle = 0x00210068;

    /** (0021,xx71) VR=DS VM=1-n Detector Rotation */
    public static final int DetectorRotation = 0x00210071;

    /** (0021,xx72) VR=SS VM=1-n Physical Detector Rotation */
    public static final int PhysicalDetectorRotation = 0x00210072;

    /** (0021,xx81) VR=SS VM=1 Table Tilt */
    public static final int TableTilt = 0x00210081;

    /** (0021,xx82) VR=SS VM=1 Table Rotation */
    public static final int TableRotation = 0x00210082;

    /** (0021,xx83) VR=SS VM=1 Table Cradle Tilt */
    public static final int TableCradleTilt = 0x00210083;

    /** (0021,xxA0) VR=OB VM=1 Crispy1 Container */
    public static final int Crispy1Container = 0x002100A0;

    /** (0021,xxA3) VR=SQ VM=1 3D Cardiac Trigger Sequence */
    public static final int ThreeDCardiacTriggerSequence = 0x002100A3;

    /** (0021,xxA4) VR=DT VM=1 3D Frame Reference Date Time */
    public static final int ThreeDFrameReferenceDateTime = 0x002100A4;

    /** (0021,xxA5) VR=FD VM=1 3D Cardiac Trigger Delay Time */
    public static final int ThreeDCardiacTriggerDelayTime = 0x002100A5;

    /** (0021,xxA6) VR=FD VM=1 3D R-R Interval Time Measure */
    public static final int ThreeDRRIntervalTimeMeasured = 0x002100A6;

}
