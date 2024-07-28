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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_NM;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MED NM";

    /** (0009,xx80) VR=ST VM=1 ? */
    public static final int _0009_xx80_ = 0x00090080;

    /** (0011,xx10) VR=ST VM=1 ? */
    public static final int _0011_xx10_ = 0x00110010;

    /** (0017,xx00) VR=ST VM=1 ? */
    public static final int _0017_xx00_ = 0x00170000;

    /** (0017,xx20) VR=ST VM=1 ? */
    public static final int _0017_xx20_ = 0x00170020;

    /** (0017,xx70) VR=ST VM=1 ? */
    public static final int _0017_xx70_ = 0x00170070;

    /** (0017,xx80) VR=ST VM=1 ? */
    public static final int _0017_xx80_ = 0x00170080;

    /** (0019,xx08) VR=SS VM=1 ? */
    public static final int _0019_xx08_ = 0x00190008;

    /** (0019,xx0F) VR=SL VM=1-n Siemens ICON Data Type */
    public static final int SiemensICONDataType = 0x0019000F;

    /** (0019,xx16) VR=SS VM=1 ? */
    public static final int _0019_xx16_ = 0x00190016;

    /** (0019,xxA5) VR=SS VM=1-n Number of Repeats per Phase */
    public static final int NumberOfRepeatsPerPhase = 0x001900A5;

    /** (0019,xxA6) VR=SS VM=1-n Cycles per Repeat */
    public static final int CyclesPerRepeat = 0x001900A6;

    /** (0019,xxA7) VR=SL VM=1-n Repeat Start Time */
    public static final int RepeatStartTime = 0x001900A7;

    /** (0019,xxA8) VR=SL VM=1-n Repeat Stop Time */
    public static final int RepeatStopTime = 0x001900A8;

    /** (0019,xxA9) VR=SL VM=1-n Effective Repeat Time */
    public static final int EffectiveRepeatTime = 0x001900A9;

    /** (0019,xxAA) VR=SS VM=1-n Acquired Cycles per Repeat */
    public static final int AcquiredCyclesPerRepeat = 0x001900AA;

    /** (0019,xx93) VR=SL VM=2 ? */
    public static final int _0019_xx93_ = 0x00190093;

    /** (0019,xxA1) VR=SS VM=1 ? */
    public static final int _0019_xxA1_ = 0x001900A1;

    /** (0019,xxA3) VR=SL VM=2 ? */
    public static final int _0019_xxA3_ = 0x001900A3;

    /** (0019,xxC3) VR=ST VM=1 ? */
    public static final int _0019_xxC3_ = 0x001900C3;

    /** (0021,xx00) VR=OB VM=1 ECAT_Main_Header */
    public static final int ECATMainHeader = 0x00210000;

    /** (0021,xx01) VR=OB VM=1 ECAT_Image_Subheader */
    public static final int ECATImageSubheader = 0x00210001;

    /** (0021,xx10) VR=ST VM=1 ? */
    public static final int _0021_xx10_ = 0x00210010;

    /** (0023,xx01) VR=US VM=1 DICOM Reader flag */
    public static final int DICOMReaderFlag = 0x00230001;

    /** (0029,xx08) VR=CS VM=1 Modality Image Header Type */
    public static final int ModalityImageHeaderType = 0x00290008;

    /** (0029,xx09) VR=LO VM=1 Modality Image Header Version */
    public static final int ModalityImageHeaderVersion = 0x00290009;

    /** (0029,xx10) VR=OB VM=1 Modality Image Header Info */
    public static final int ModalityImageHeaderInfo = 0x00290010;

    /** (0031,xx01) VR=ST VM=1 ? */
    public static final int _0031_xx01_ = 0x00310001;

    /** (0031,xx0C) VR=SS VM=1 ? */
    public static final int _0031_xx0C_ = 0x0031000C;

    /** (0031,xx0F) VR=SL VM=1 ? */
    public static final int _0031_xx0F_ = 0x0031000F;

    /** (0031,xx10) VR=SL VM=1 ? */
    public static final int _0031_xx10_ = 0x00310010;

    /** (0031,xx12) VR=SS VM=1 ? */
    public static final int _0031_xx12_ = 0x00310012;

    /** (0031,xx13) VR=ST VM=1 ? */
    public static final int _0031_xx13_ = 0x00310013;

    /** (0031,xx14) VR=ST VM=1 ? */
    public static final int _0031_xx14_ = 0x00310014;

    /** (0031,xx15) VR=SL VM=1-n ? */
    public static final int _0031_xx15_ = 0x00310015;

    /** (0031,xx16) VR=SL VM=1-n ? */
    public static final int _0031_xx16_ = 0x00310016;

    /** (0031,xx17) VR=SS VM=1-n ? */
    public static final int _0031_xx17_ = 0x00310017;

    /** (0031,xx20) VR=ST VM=1 ? */
    public static final int _0031_xx20_ = 0x00310020;

    /** (0031,xx21) VR=SS VM=1 ? */
    public static final int _0031_xx21_ = 0x00310021;

    /** (0033,xx00) VR=FL VM=1-n Flood Correction Matrix Detector 1 */
    public static final int FloodCorrectionMatrixDetector1 = 0x00330000;

    /** (0033,xx01) VR=FL VM=1-n Flood Correction Matrix Detector 2 */
    public static final int FloodCorrectionMatrixDetector2 = 0x00330001;

    /** (0033,xx10) VR=FL VM=1-n COR Data for Detector 1 */
    public static final int CORDataForDetector1 = 0x00330010;

    /** (0033,xx11) VR=FL VM=1-n COR Data for Detector 2 */
    public static final int CORDataForDetector2 = 0x00330011;

    /** (0033,xx14) VR=FL VM=1 MHR ( Y-Shif */
    public static final int MHRDataForDetector1 = 0x00330014;

    /** (0033,xx15) VR=FL VM=1 MHR ( Y-Shif */
    public static final int MHRDataForDetector2 = 0x00330015;

    /** (0033,xx18) VR=FL VM=1-n NCO Data for detector 1 */
    public static final int NCODataForDetector1 = 0x00330018;

    /** (0033,xx19) VR=FL VM=1-n NCO Data for detector 2 */
    public static final int NCODataForDetector2 = 0x00330019;

    /** (0033,xx1A) VR=FL VM=1-n ? */
    public static final int _0033_xx1A_ = 0x0033001A;

    /** (0033,xx20) VR=FL VM=1 Bed correction angle */
    public static final int BedCorrectionAngle = 0x00330020;

    /** (0033,xx21) VR=FL VM=1 Gantry correction angle */
    public static final int GantryCorrectionAngle = 0x00330021;

    /** (0033,xx22) VR=SS VM=1-n Bed U/D correction data */
    public static final int BedUDCorrectionData = 0x00330022;

    /** (0033,xx23) VR=SS VM=1-n Gantry L/R Correction Data */
    public static final int GantryLRCorrectionData = 0x00330023;

    /** (0033,xx24) VR=FL VM=1 BackProjection Correction angle head 1 */
    public static final int BackProjectionCorrectionAngleHead1 = 0x00330024;

    /** (0033,xx25) VR=FL VM=1 BackProjection Correction angle head 2 */
    public static final int BackProjectionCorrectionAngleHead2 = 0x00330025;

    /** (0033,xx28) VR=SL VM=1 MHR calibrations */
    public static final int MHRCalibrations = 0x00330028;

    /** (0033,xx29) VR=FL VM=1-n Crystal thickness */
    public static final int CrystalThickness = 0x00330029;

    /** (0033,xx30) VR=LO VM=1 Preset name used for acquisition */
    public static final int PresetNameUsedForAcquisition = 0x00330030;

    /** (0033,xx31) VR=FL VM=1 Camera Config Angle */
    public static final int CameraConfigAngle = 0x00330031;

    /** (0033,xx32) VR=LO VM=1 Crystal Type */
    public static final int CrystalType = 0x00330032;

    /** (0033,xx33) VR=SL VM=1 Coin Gantry Step */
    public static final int CoinGantryStep = 0x00330033;

    /** (0033,xx34) VR=FL VM=1 Wholebody bed step */
    public static final int WholebodyBedStep = 0x00330034;

    /** (0033,xx35) VR=FL VM=1 Weight Factor Table For Coincidence Acquisitions */
    public static final int WeightFactorTableForCoincidenceAcquisitions = 0x00330035;

    /** (0033,xx36) VR=FL VM=1 Coincidence weight factor table */
    public static final int CoincidenceWeightFactorTable = 0x00330036;

    /** (0033,xx37) VR=SL VM=1 Starburst flags at image acq time */
    public static final int StarburstFlagsAtImageAcqTime = 0x00330037;

    /** (0033,xx38) VR=FL VM=1 Pixel Scale factor */
    public static final int PixelScaleFactor = 0x00330038;

    /** (0035,xx00) VR=LO VM=1 Specialized Tomo Type */
    public static final int SpecializedTomoType = 0x00350000;

    /** (0035,xx01) VR=LO VM=1 Energy Window Type */
    public static final int EnergyWindowType = 0x00350001;

    /** (0035,xx02) VR=SS VM=1 Start and End Row Illuminated By Wind Position */
    public static final int StartandEndRowIlluminatedByWindPosition = 0x00350002;

    /** (0035,xx03) VR=LO VM=1 Blank Scan Image For Profile */
    public static final int BlankScanImageForProfile = 0x00350003;

    /** (0035,xx04) VR=SS VM=1 Repeat Number of the Original Dynamic SPECT */
    public static final int RepeatNumberOfTheOriginalDynamicSPECT = 0x00350004;

    /** (0035,xx05) VR=SS VM=1 Phase Number of the Original Dynamic SPECT */
    public static final int PhaseNumberOfTheOriginalDynamicSPECT = 0x00350005;

    /** (0035,xx06) VR=LO VM=1 Siemens Profile 2 Image Subtype */
    public static final int SiemensProfile2ImageSubtype = 0x00350006;

    /** (0039,xx00) VR=LT VM=1 Toshiba CBF Activity Results */
    public static final int ToshibaCBFActivityResults = 0x00390000;

    /** (0039,xx01) VR=LT VM=1 Related CT Series Instance UID */
    public static final int RelatedCTSeriesInstanceUID = 0x00390001;

    /** (0041,xx01) VR=SL VM=1 Whole Body Tomo Position Index */
    public static final int WholeBodyTomoPositionIndex = 0x00410001;

    /** (0041,xx02) VR=SL VM=1 Whole Body Tomo Number of Positions */
    public static final int WholeBodyTomoNumberOfPositions = 0x00410002;

    /** (0041,xx03) VR=FL VM=1 Horizontal Table Position of CT scan */
    public static final int HorizontalTablePositionOfCTScan = 0x00410003;

    /** (0041,xx04) VR=FL VM=1 Effective Energy fo CT Scan */
    public static final int EffectiveEnergyOfCTScan = 0x00410004;

    /** (0041,xx05) VR=FD VM=1-n Long Linear Drive Information for Detector 1 */
    public static final int LongLinearDriveInformationForDetector1 = 0x00410005;

    /** (0041,xx06) VR=FD VM=1-n Long Linear Drive Information for Detector 2 */
    public static final int LongLinearDriveInformationForDetector2 = 0x00410006;

    /** (0041,xx07) VR=FD VM=1-n Trunnion Information for Detector 1 */
    public static final int TrunnionInformationForDetector1 = 0x00410007;

    /** (0041,xx08) VR=FD VM=1-n Trunnion Information for Detector 2 */
    public static final int TrunnionInformationForDetector2 = 0x00410008;

    /** (0041,xx09) VR=FL VM=1 Broad Beam Factor */
    public static final int BroadBeamFactor = 0x00410009;

    /** (0041,xx0A) VR=FD VM=1 Original Wholebody Position */
    public static final int OriginalWholebodyPosition = 0x0041000A;

    /** (0041,xx0B) VR=FD VM=1 Wholebody Scan Range */
    public static final int WholebodyScanRange = 0x0041000B;

    /** (0041,xx10) VR=FL VM=1-3 Effective Emission Energy */
    public static final int EffectiveEmissionEnergy = 0x00410010;

    /** (0041,xx11) VR=FL VM=1-n Gated Frame Duration */
    public static final int GatedFrameDuration = 0x00410011;

    /** (0041,xx30) VR=ST VM=1 ? */
    public static final int _0041_xx30_ = 0x00410030;

    /** (0041,xx32) VR=ST VM=1 ? */
    public static final int _0041_xx32_ = 0x00410032;

    /** (0043,xx01) VR=FL VM=1-n Detector View Angle */
    public static final int DetectorViewAngle = 0x00430001;

    /** (0043,xx02) VR=FD VM=1-16 Transformation Matrix */
    public static final int TransformationMatrix = 0x00430002;

    /** (0043,xx03) VR=FL VM=1-n View Dependent Y Shift MHR For Detector 1 */
    public static final int ViewDependentYShiftMHRForDetector1 = 0x00430003;

    /** (0043,xx04) VR=FL VM=1-n View Dependent Y Shift MHR For Detector 2 */
    public static final int ViewDependentYShiftMHRForDetector2 = 0x00430004;

    /** (0045,xx01) VR=LO VM=1-n Planar Processing String */
    public static final int PlanarProcessingString = 0x00450001;

    /** (0055,xx04) VR=SS VM=1 Prompt Window Width */
    public static final int PromptWindowWidth = 0x00550004;

    /** (0055,xx05) VR=SS VM=1 Random Window Width */
    public static final int RandomWindowWidth = 0x00550005;

    /** (0055,xx20) VR=SS VM=1 ? */
    public static final int _0055_xx20_ = 0x00550020;

    /** (0055,xx22) VR=SS VM=1 ? */
    public static final int _0055_xx22_ = 0x00550022;

    /** (0055,xx24) VR=SS VM=1 ? */
    public static final int _0055_xx24_ = 0x00550024;

    /** (0055,xx30) VR=SS VM=1 ? */
    public static final int _0055_xx30_ = 0x00550030;

    /** (0055,xx32) VR=SS VM=1 ? */
    public static final int _0055_xx32_ = 0x00550032;

    /** (0055,xx34) VR=SS VM=1 ? */
    public static final int _0055_xx34_ = 0x00550034;

    /** (0055,xx40) VR=SS VM=1 ? */
    public static final int _0055_xx40_ = 0x00550040;

    /** (0055,xx42) VR=SS VM=1 ? */
    public static final int _0055_xx42_ = 0x00550042;

    /** (0055,xx44) VR=SS VM=1 ? */
    public static final int _0055_xx44_ = 0x00550044;

    /** (0055,xx4C) VR=SL VM=1 ? */
    public static final int _0055_xx4C_ = 0x0055004C;

    /** (0055,xx4D) VR=SL VM=1 ? */
    public static final int _0055_xx4D_ = 0x0055004D;

    /** (0055,xx51) VR=SS VM=1 ? */
    public static final int _0055_xx51_ = 0x00550051;

    /** (0055,xx52) VR=SS VM=1 ? */
    public static final int _0055_xx52_ = 0x00550052;

    /** (0055,xx53) VR=ST VM=1 ? */
    public static final int _0055_xx53_ = 0x00550053;

    /** (0055,xx55) VR=SL VM=1 ? */
    public static final int _0055_xx55_ = 0x00550055;

    /** (0055,xx5C) VR=ST VM=1 ? */
    public static final int _0055_xx5C_ = 0x0055005C;

    /** (0055,xx6D) VR=SS VM=1 ? */
    public static final int _0055_xx6D_ = 0x0055006D;

    /** (0055,xx7E) VR=FL VM=2 Collimator Thickness mm */
    public static final int CollimatorThickness = 0x0055007E;

    /** (0055,xx7F) VR=FL VM=2 Collimator Angular Resolution radians */
    public static final int CollimatorAngularResolution = 0x0055007F;

    /** (0055,xxA8) VR=SS VM=1 ? */
    public static final int _0055_xxA8_ = 0x005500A8;

    /** (0055,xxC0) VR=SS VM=1-n Useful Field of View */
    public static final int UsefulFieldOfView = 0x005500C0;

    /** (0055,xxC2) VR=SS VM=1 ? */
    public static final int _0055_xxC2_ = 0x005500C2;

    /** (0055,xxC3) VR=SS VM=1 ? */
    public static final int _0055_xxC3_ = 0x005500C3;

    /** (0055,xxC4) VR=SS VM=1 ? */
    public static final int _0055_xxC4_ = 0x005500C4;

    /** (0055,xxD0) VR=SS VM=1 ? */
    public static final int _0055_xxD0_ = 0x005500D0;

    /** (0057,xx01) VR=LO VM=1 Syngo MI DICOM Original Image Type */
    public static final int SyngoMIDICOMOriginalImageType = 0x00570001;

    /** (0057,xx02) VR=FL VM=1 Dose Calibration Factor */
    public static final int DoseCalibrationFactor = 0x00570002;

    /** (0057,xx03) VR=LO VM=1 Units */
    public static final int Units = 0x00570003;

    /** (0057,xx04) VR=LO VM=1 Decay Correction */
    public static final int DecayCorrection = 0x00570004;

    /** (0057,xx05) VR=SL VM=1-n Radionuclide Half Life */
    public static final int RadionuclideHalfLife = 0x00570005;

    /** (0057,xx06) VR=FL VM=1 Rescale Intercept */
    public static final int RescaleIntercept = 0x00570006;

    /** (0057,xx07) VR=FL VM=1 Rescale Slope */
    public static final int RescaleSlope = 0x00570007;

    /** (0057,xx08) VR=FL VM=1 Frame Reference Time */
    public static final int FrameReferenceTime = 0x00570008;

    /** (0057,xx09) VR=SL VM=1 Number of Radiopharmaceutical Information Sequence */
    public static final int NumberofRadiopharmaceuticalInformationSequence = 0x00570009;

    /** (0057,xx0A) VR=FL VM=1 Decay Factor */
    public static final int DecayFactor = 0x0057000A;

    /** (0057,xx0B) VR=LO VM=1 Counts Source */
    public static final int CountsSource = 0x0057000B;

    /** (0057,xx0C) VR=SL VM=1-n Radionuclide Positron Fraction */
    public static final int RadionuclidePositronFraction = 0x0057000C;

    /** (0057,xx0E) VR=US VM=1-n Trigger Time of CT Slice */
    public static final int TriggerTimeOfCTSlice = 0x0057000E;

    /** (0061,xx01) VR=FL VM=1-n X Principal Ray Offset */
    public static final int XPrincipalRayOffset = 0x00610001;

    /** (0061,xx05) VR=FL VM=1-n Y Principal Ray Offset */
    public static final int YPrincipalRayOffset = 0x00610005;

    /** (0061,xx09) VR=FL VM=1-n X Principal Ray Angle */
    public static final int XPrincipalRayAngle = 0x00610009;

    /** (0061,xx0A) VR=FL VM=1-n Y Principal Ray Angle */
    public static final int YPrincipalRayAngle = 0x0061000A;

    /** (0061,xx0B) VR=FL VM=1-n X Short Focal Length */
    public static final int XShortFocalLength = 0x0061000B;

    /** (0061,xx0C) VR=FL VM=1-n Y Short Focal Length */
    public static final int YShortFocalLength = 0x0061000C;

    /** (0061,xx0D) VR=FL VM=1-n X Long Focal Length */
    public static final int XLongFocalLength = 0x0061000D;

    /** (0061,xx0E) VR=FL VM=1-n Y Long Focal Length */
    public static final int YLongFocalLength = 0x0061000E;

    /** (0061,xx0F) VR=FL VM=1-n X Focal Scaling */
    public static final int XFocalScaling = 0x0061000F;

    /** (0061,xx10) VR=FL VM=1-n Y Focal Scaling */
    public static final int YFocalScaling = 0x00610010;

    /** (0061,xx11) VR=FL VM=1-n X Motion Correction Shift */
    public static final int XMotionCorrectionShift = 0x00610011;

    /** (0061,xx15) VR=FL VM=1-n Y Motion Correction Shift */
    public static final int YMotionCorrectionShift = 0x00610015;

    /** (0061,xx19) VR=FL VM=1 X Heart Center */
    public static final int XHeartCenter = 0x00610019;

    /** (0061,xx1A) VR=FL VM=1 Y Heart Center */
    public static final int YHeartCenter = 0x0061001A;

    /** (0061,xx1B) VR=FL VM=1 Z Heart Center */
    public static final int ZHeartCenter = 0x0061001B;

    /** (0061,xx1C) VR=LO VM=1 Image Pixel Content Type */
    public static final int ImagePixelContentType = 0x0061001C;

    /** (0061,xx1D) VR=SS VM=1 Auto Save Corrected Series */
    public static final int AutoSaveCorrectedSeries = 0x0061001D;

    /** (0061,xx1E) VR=LT VM=1 Distorted Series Instance UID */
    public static final int DistortedSeriesInstanceUID = 0x0061001E;

    /** (0061,xx21) VR=SS VM=1-n Recon Range */
    public static final int ReconRange = 0x00610021;

    /** (0061,xx22) VR=LO VM=1 Recon Orientation */
    public static final int ReconOrientation = 0x00610022;

    /** (0061,xx23) VR=FL VM=1-n Recon Selected Angular Range */
    public static final int ReconSelectedAngularRange = 0x00610023;

    /** (0061,xx24) VR=FL VM=1 Recon Transverse Angle */
    public static final int ReconTransverseAngle = 0x00610024;

    /** (0061,xx25) VR=FL VM=1 Recon Sagittal Angle */
    public static final int ReconSagittalAngle = 0x00610025;

    /** (0061,xx26) VR=FL VM=1 Recon X Mask Size */
    public static final int ReconXMaskSize = 0x00610026;

    /** (0061,xx27) VR=FL VM=1 Recon Y Mask Size */
    public static final int ReconYMaskSize = 0x00610027;

    /** (0061,xx28) VR=FL VM=1 Recon X Image Center */
    public static final int ReconXImageCenter = 0x00610028;

    /** (0061,xx29) VR=FL VM=1 Recon Y Image Center */
    public static final int ReconYImageCenter = 0x00610029;

    /** (0061,xx2A) VR=FL VM=1 Recon Z Image Center */
    public static final int ReconZImageCenter = 0x0061002A;

    /** (0061,xx2B) VR=FL VM=1 Recon X Zoom */
    public static final int ReconXZoom = 0x0061002B;

    /** (0061,xx2C) VR=FL VM=1 Recon Y Zoom */
    public static final int ReconYZoom = 0x0061002C;

    /** (0061,xx2D) VR=FL VM=1 Recon Threshold */
    public static final int ReconThreshold = 0x0061002D;

    /** (0061,xx2E) VR=FL VM=1 Recon Output Pixel Size */
    public static final int ReconOutputPixelSize = 0x0061002E;

    /** (0061,xx2F) VR=LO VM=1-n Scatter Estimation Method */
    public static final int ScatterEstimationMethod = 0x0061002F;

    /** (0061,xx30) VR=LO VM=1-n Scatter Estimation Method Mode */
    public static final int ScatterEstimationMethodMode = 0x00610030;

    /** (0061,xx31) VR=FL VM=1-n Scatter Estimation Lower Window Weights */
    public static final int ScatterEstimationLowerWindowWeights = 0x00610031;

    /** (0061,xx32) VR=FL VM=1-n Scatter Estimation Upper Window Weights */
    public static final int ScatterEstimationUpperWindowWeights = 0x00610032;

    /** (0061,xx33) VR=LO VM=1-n Scatter Estimation Window Mode */
    public static final int ScatterEstimationWindowMode = 0x00610033;

    /** (0061,xx34) VR=LO VM=1-n Scatter Estimation Filter */
    public static final int ScatterEstimationFilter = 0x00610034;

    /** (0061,xx35) VR=LO VM=1 Recon RawTomo Input UID */
    public static final int ReconRawTomoInputUID = 0x00610035;

    /** (0061,xx36) VR=LO VM=1 Recon CT Input UID */
    public static final int ReconCTInputUID = 0x00610036;

    /** (0061,xx37) VR=FL VM=1 Recon Z Mask Size */
    public static final int ReconZMaskSize = 0x00610037;

    /** (0061,xx38) VR=FL VM=1 Recon X Mask Center */
    public static final int ReconXMaskCenter = 0x00610038;

    /** (0061,xx39) VR=FL VM=1 Recon Y Mask Center */
    public static final int ReconYMaskCenter = 0x00610039;

    /** (0061,xx3A) VR=FL VM=1 Recon Z Mask Center */
    public static final int ReconZMaskCenter = 0x0061003A;

    /** (0061,xx51) VR=LT VM=1 Raw Tomo Series UID */
    public static final int RawTomoSeriesUID = 0x00610051;

    /** (0061,xx52) VR=LT VM=1 LowRes CT Series UID */
    public static final int LowResCTSeriesUID = 0x00610052;

    /** (0061,xx53) VR=LT VM=1 HighRes CT Series UID */
    public static final int HighResCTSeriesUID = 0x00610053;

    /** (7FE3,xx14) VR=OW VM=1 Minimum Pixel in Frame */
    public static final int MinimumPixelInFrame = 0x7FE30014;

    /** (7FE3,xx15) VR=OW VM=1 Maximum Pixel in Frame */
    public static final int MaximumPixelInFrame = 0x7FE30015;

    /** (7FE3,xx16) VR=OW VM=1 ? */
    public static final int _7FE3_xx16_ = 0x7FE30016;

    /** (7FE3,xx1B) VR=OW VM=1 ? */
    public static final int _7FE3_xx1B_ = 0x7FE3001B;

    /** (7FE3,xx1C) VR=OW VM=1 ? */
    public static final int _7FE3_xx1C_ = 0x7FE3001C;

    /** (7FE3,xx1E) VR=OW VM=1 ? */
    public static final int _7FE3_xx1E_ = 0x7FE3001E;

    /** (7FE3,xx26) VR=OW VM=1 ? */
    public static final int _7FE3_xx26_ = 0x7FE30026;

    /** (7FE3,xx27) VR=OW VM=1 ? */
    public static final int _7FE3_xx27_ = 0x7FE30027;

    /** (7FE3,xx28) VR=OW VM=1 ? */
    public static final int _7FE3_xx28_ = 0x7FE30028;

    /** (7FE3,xx29) VR=OW VM=1 Number of R-Waves in Frame */
    public static final int NumberOfRWavesInFrame = 0x7FE30029;

}
