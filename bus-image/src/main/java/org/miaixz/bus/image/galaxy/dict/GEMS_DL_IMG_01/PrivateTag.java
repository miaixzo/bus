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
package org.miaixz.bus.image.galaxy.dict.GEMS_DL_IMG_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_DL_IMG_01";

    /** (0019,xx0B) VR=DS VM=1-2 FOV Dimension Double */
    public static final int FOVDimensionDouble = 0x0019000B;

    /** (0019,xx2B) VR=FL VM=1 Distance to table top */
    public static final int DistanceToTableTop = 0x0019002B;

    /** (0019,xx30) VR=LO VM=1 Image File Name */
    public static final int ImageFileName = 0x00190030;

    /** (0019,xx31) VR=IS VM=1 Default Spatial Filter Family */
    public static final int DefaultSpatialFilterFamily = 0x00190031;

    /** (0019,xx32) VR=IS VM=1 Default Spatial Filter Strength */
    public static final int DefaultSpatialFilterStrength = 0x00190032;

    /** (0019,xx33) VR=DS VM=1 Min Saturation Dose */
    public static final int MinSaturationDose = 0x00190033;

    /** (0019,xx34) VR=DS VM=1 Detector Gain */
    public static final int DetectorGain = 0x00190034;

    /** (0019,xx35) VR=DS VM=1 Patient Dose Limit */
    public static final int PatientDoseLimit = 0x00190035;

    /** (0019,xx36) VR=DS VM=1 Preproc Image Rate Max */
    public static final int PreprocImageRateMax = 0x00190036;

    /** (0019,xx37) VR=CS VM=1 Sensor Roi Shape */
    public static final int SensorRoiShape = 0x00190037;

    /** (0019,xx38) VR=DS VM=1 Sensor Roi x Position */
    public static final int SensorRoixPosition = 0x00190038;

    /** (0019,xx39) VR=DS VM=1 Sensor Roi y Position */
    public static final int SensorRoiyPosition = 0x00190039;

    /** (0019,xx3A) VR=DS VM=1 Sensor Roi x Size */
    public static final int SensorRoixSize = 0x0019003A;

    /** (0019,xx3B) VR=DS VM=1 Sensor Roi y Size */
    public static final int SensorRoiySize = 0x0019003B;

    /** (0019,xx3D) VR=DS VM=1 Noise Sensitivity */
    public static final int NoiseSensitivity = 0x0019003D;

    /** (0019,xx3E) VR=DS VM=1 Sharp Sensitivity */
    public static final int SharpSensitivity = 0x0019003E;

    /** (0019,xx3F) VR=DS VM=1 Contrast Sensitivity */
    public static final int ContrastSensitivity = 0x0019003F;

    /** (0019,xx40) VR=DS VM=1 Lag Sensitivity */
    public static final int LagSensitivity = 0x00190040;

    /** (0019,xx41) VR=CS VM=1 Tube */
    public static final int Tube = 0x00190041;

    /** (0019,xx42) VR=US VM=1 Detector Size Rows */
    public static final int DetectorSizeRows = 0x00190042;

    /** (0019,xx43) VR=US VM=1 Detector Size Columns */
    public static final int DetectorSizeColumns = 0x00190043;

    /** (0019,xx44) VR=DS VM=1 Min Object Size */
    public static final int MinObjectSize = 0x00190044;

    /** (0019,xx45) VR=DS VM=1 Max Object Size */
    public static final int MaxObjectSize = 0x00190045;

    /** (0019,xx46) VR=DS VM=1 Max Object Speed */
    public static final int MaxObjectSpeed = 0x00190046;

    /** (0019,xx47) VR=CS VM=1 Object Back Motion */
    public static final int ObjectBackMotion = 0x00190047;

    /** (0019,xx48) VR=UL VM=1 Exposure Trajectory Family */
    public static final int ExposureTrajectoryFamily = 0x00190048;

    /** (0019,xx49) VR=DS VM=1 Window Time Duration */
    public static final int WindowTimeDuration = 0x00190049;

    /** (0019,xx4A) VR=CS VM=1 Positioner Angle Display Mode */
    public static final int PositionerAngleDisplayMode = 0x0019004A;

    /** (0019,xx4B) VR=IS VM=2 Detector Origin */
    public static final int DetectorOrigin = 0x0019004B;

    /** (0019,xx4C) VR=CS VM=1 ? */
    public static final int _0019_xx4C_ = 0x0019004C;

    /** (0019,xx4E) VR=DS VM=2 Default Brightness and Contrast */
    public static final int DefaultBrightnessContrast = 0x0019004E;

    /** (0019,xx4F) VR=DS VM=2 User Brightness and Contrast */
    public static final int UserBrightnessContrast = 0x0019004F;

    /** (0019,xx50) VR=IS VM=1 Source Series Number */
    public static final int SourceSeriesNumber = 0x00190050;

    /** (0019,xx51) VR=IS VM=1 Source Image Number */
    public static final int SourceImageNumber = 0x00190051;

    /** (0019,xx52) VR=IS VM=1 Source Frame Number */
    public static final int SourceFrameNumber = 0x00190052;

    /** (0019,xx53) VR=UI VM=1 Source Series Item Id */
    public static final int SourceSeriesItemId = 0x00190053;

    /** (0019,xx54) VR=UI VM=1 Source Image Item Id */
    public static final int SourceImageItemId = 0x00190054;

    /** (0019,xx55) VR=UI VM=1 Source Frame Item Id */
    public static final int SourceFrameItemId = 0x00190055;

    /** (0019,xx60) VR=US VM=1 Number of Points Before Acquisition */
    public static final int NumberOfPointsBeforeAcquisition = 0x00190060;

    /** (0019,xx61) VR=OW VM=1 Curve Data Before Acquisition */
    public static final int CurveDataBeforeAcquisition = 0x00190061;

    /** (0019,xx62) VR=US VM=1 Number of Points Trigger */
    public static final int NumberOfPointsTrigger = 0x00190062;

    /** (0019,xx63) VR=OW VM=1 Curve Data Trigger */
    public static final int CurveDataTrigger = 0x00190063;

    /** (0019,xx64) VR=SH VM=1 ECG Synchronization */
    public static final int ECGSynchronization = 0x00190064;

    /** (0019,xx65) VR=SH VM=1 ECG Delay Mode */
    public static final int ECGDelayMode = 0x00190065;

    /** (0019,xx66) VR=IS VM=1-n ECG Delay Vector */
    public static final int ECGDelayVector = 0x00190066;

    /** (0019,xx67) VR=DS VM=1 ? */
    public static final int _0019_xx67_ = 0x00190067;

    /** (0019,xx68) VR=DS VM=1 ? */
    public static final int _0019_xx68_ = 0x00190068;

    /** (0019,xx69) VR=DS VM=1 ? */
    public static final int _0019_xx69_ = 0x00190069;

    /** (0019,xx7A) VR=DS VM=1 ? */
    public static final int _0019_xx7A_ = 0x0019007A;

    /** (0019,xx7B) VR=DS VM=1 ? */
    public static final int _0019_xx7B_ = 0x0019007B;

    /** (0019,xx7C) VR=DS VM=1 ? */
    public static final int _0019_xx7C_ = 0x0019007C;

    /** (0019,xx80) VR=DS VM=1 Image Dose */
    public static final int ImageDose = 0x00190080;

    /** (0019,xx81) VR=US VM=1 Calibration Frame */
    public static final int CalibrationFrame = 0x00190081;

    /** (0019,xx82) VR=CS VM=1 Calibration Object */
    public static final int CalibrationObject = 0x00190082;

    /** (0019,xx83) VR=DS VM=1 Calibration Object Size mm */
    public static final int CalibrationObjectSize = 0x00190083;

    /** (0019,xx84) VR=FL VM=1 Calibration Factor */
    public static final int CalibrationFactor = 0x00190084;

    /** (0019,xx85) VR=DA VM=1 Calibration Date */
    public static final int CalibrationDate = 0x00190085;

    /** (0019,xx86) VR=TM VM=1 Calibration Time */
    public static final int CalibrationTime = 0x00190086;

    /** (0019,xx87) VR=US VM=1 Calibration Accuracy */
    public static final int CalibrationAccuracy = 0x00190087;

    /** (0019,xx88) VR=CS VM=1 Calibration Extended */
    public static final int CalibrationExtended = 0x00190088;

    /** (0019,xx89) VR=US VM=1 Calibration Image Original */
    public static final int CalibrationImageOriginal = 0x00190089;

    /** (0019,xx8A) VR=US VM=1 Calibration Frame Original */
    public static final int CalibrationFrameOriginal = 0x0019008A;

    /** (0019,xx8B) VR=US VM=1 Calibration nb points uif */
    public static final int CalibrationNbPointsUif = 0x0019008B;

    /** (0019,xx8C) VR=US VM=1-n Calibration Points Row */
    public static final int CalibrationPointsRow = 0x0019008C;

    /** (0019,xx8D) VR=US VM=1-n Calibration Points Column */
    public static final int CalibrationPointsColumn = 0x0019008D;

    /** (0019,xx8E) VR=FL VM=1 Calibration Magnification Ratio */
    public static final int CalibrationMagnificationRatio = 0x0019008E;

    /** (0019,xx8F) VR=LO VM=1 Calibration Software Version */
    public static final int CalibrationSoftwareVersion = 0x0019008F;

    /** (0019,xx90) VR=LO VM=1 Extended Calibration Software Version */
    public static final int ExtendedCalibrationSoftwareVersion = 0x00190090;

    /** (0019,xx91) VR=IS VM=1 Calibration Return Code */
    public static final int CalibrationReturnCode = 0x00190091;

    /** (0019,xx92) VR=DS VM=1 Detector Rotation Angle */
    public static final int DetectorRotationAngle = 0x00190092;

    /** (0019,xx93) VR=CS VM=1 Spatial Change */
    public static final int SpatialChange = 0x00190093;

    /** (0019,xx94) VR=CS VM=1 Inconsistent Flag */
    public static final int InconsistentFlag = 0x00190094;

    /** (0019,xx95) VR=CS VM=2 Horizontal and Vertical Image Flip */
    public static final int HorizontalAndVerticalImageFlip = 0x00190095;

    /** (0019,xx96) VR=CS VM=1 Internal Label Image */
    public static final int InternalLabelImage = 0x00190096;

    /** (0019,xx97) VR=DS VM=1-n Angle 1 increment */
    public static final int Angle1Increment = 0x00190097;

    /** (0019,xx98) VR=DS VM=1-n Angle 2 increment */
    public static final int Angle2Increment = 0x00190098;

    /** (0019,xx99) VR=DS VM=1-n Angle 3 increment */
    public static final int Angle3Increment = 0x00190099;

    /** (0019,xx9A) VR=DS VM=1-n Sensor Feedback */
    public static final int SensorFeedback = 0x0019009A;

    /** (0019,xx9B) VR=CS VM=1 Grid */
    public static final int Grid = 0x0019009B;

    /** (0019,xx9C) VR=FL VM=1 Default Mask Pixel Shift */
    public static final int DefaultMaskPixelShift = 0x0019009C;

    /** (0019,xx9D) VR=CS VM=1 Applicable Review Mode */
    public static final int ApplicableReviewMode = 0x0019009D;

    /** (0019,xx9E) VR=DS VM=1-n Log LUT Control Points */
    public static final int LogLUTControlPoints = 0x0019009E;

    /** (0019,xx9F) VR=DS VM=1-n Exp LUT SUB Control Points */
    public static final int ExpLUTSUBControlPoints = 0x0019009F;

    /** (0019,xxA0) VR=DS VM=1 ABD Value */
    public static final int ABDValue = 0x001900A0;

    /** (0019,xxA1) VR=DS VM=1 Subtraction Window Center */
    public static final int SubtractionWindowCenter = 0x001900A1;

    /** (0019,xxA2) VR=DS VM=1 Subtraction Window Width */
    public static final int SubtractionWindowWidth = 0x001900A2;

    /** (0019,xxA3) VR=DS VM=1 Image Rotation */
    public static final int ImageRotation = 0x001900A3;

    /** (0019,xxA4) VR=CS VM=1 Auto Injection Enabled */
    public static final int AutoInjectionEnabled = 0x001900A4;

    /** (0019,xxA5) VR=CS VM=1 Injection Phase */
    public static final int InjectionPhase = 0x001900A5;

    /** (0019,xxA6) VR=DS VM=1 Injection Delay */
    public static final int InjectionDelay = 0x001900A6;

    /** (0019,xxA7) VR=IS VM=1 Reference Injection Frame Number */
    public static final int ReferenceInjectionFrameNumber = 0x001900A7;

    /** (0019,xxA8) VR=DS VM=1 Injection Duration */
    public static final int InjectionDuration = 0x001900A8;

    /** (0019,xxA9) VR=DS VM=1-n EPT */
    public static final int EPT = 0x001900A9;

    /** (0019,xxAA) VR=CS VM=1 Can Downscan 512 */
    public static final int CanDownscan512 = 0x001900AA;

    /** (0019,xxAB) VR=IS VM=1 Current Spatial Filter Strength */
    public static final int CurrentSpatialFilterStrength = 0x001900AB;

    /** (0019,xxAC) VR=DS VM=1 Brightness Sensitivity */
    public static final int BrightnessSensitivity = 0x001900AC;

    /** (0019,xxAD) VR=DS VM=1-n Exp LUT NOSUB Control Points */
    public static final int ExpLUTNOSUBControlPoints = 0x001900AD;

    /** (0019,xxAF) VR=DS VM=1-n ? */
    public static final int _0019_xxAF_ = 0x001900AF;

    /** (0019,xxB0) VR=DS VM=1-n ? */
    public static final int _0019_xxB0_ = 0x001900B0;

    /** (0019,xxB1) VR=LO VM=1 Acquisition Mode Description */
    public static final int AcquisitionModeDescription = 0x001900B1;

    /** (0019,xxB2) VR=LO VM=1 Acquisition Mode Description Label */
    public static final int AcquisitionModeDescriptionLabel = 0x001900B2;

    /** (0019,xxB3) VR=LO VM=1 ? */
    public static final int _0019_xxB3_ = 0x001900B3;

    /** (0019,xxB8) VR=FL VM=1 ? */
    public static final int _0019_xxB8_ = 0x001900B8;

    /** (0019,xxBA) VR=CS VM=1 Acquisition Region */
    public static final int AcquisitionRegion = 0x001900BA;

    /** (0019,xxBB) VR=CS VM=1 Acquisition SUB Mode */
    public static final int AcquisitionSUBMode = 0x001900BB;

    /** (0019,xxBC) VR=FL VM=1 Table Cradle Angle */
    public static final int TableCradleAngle = 0x001900BC;

    /** (0019,xxBD) VR=CS VM=1-n Table Rotation Status Vector */
    public static final int TableRotationStatusVector = 0x001900BD;

    /** (0019,xxBE) VR=FL VM=1-n Source to Image Distance per Frame Vector */
    public static final int SourceToImageDistancePerFrameVector = 0x001900BE;

    /** (0019,xxC2) VR=DS VM=1-n ? */
    public static final int _0019_xxC2_ = 0x001900C2;

    /** (0019,xxC3) VR=FL VM=1-n Table Rotation Angle Increment */
    public static final int TableRotationAngleIncrement = 0x001900C3;

    /** (0019,xxC4) VR=IS VM=1 ? */
    public static final int _0019_xxC4_ = 0x001900C4;

    /** (0019,xxC7) VR=CS VM=1 Patient Position per Image */
    public static final int PatientPositionPerImage = 0x001900C7;

    /** (0019,xxD7) VR=FL VM=1-n Table X Position to Iso‐center Increment */
    public static final int TableXPositionToIsocenterIncrement = 0x001900D7;

    /** (0019,xxD8) VR=FL VM=1-n Table Y Position to Iso‐center Increment */
    public static final int TableYPositionToIsocenterIncrement = 0x001900D8;

    /** (0019,xxD9) VR=FL VM=1-n Table Z Position to Iso‐center Increment */
    public static final int TableZPositionToIsocenterIncrement = 0x001900D9;

    /** (0019,xxDA) VR=FL VM=1-n Table Head Tilt Angle Increment */
    public static final int TableHeadTiltAngleIncrement = 0x001900DA;

    /** (0019,xxDC) VR=LO VM=1 ? */
    public static final int _0019_xxDC_ = 0x001900DC;

    /** (0019,xxDE) VR=CS VM=1 Acquisition Plane */
    public static final int AcquisitionPlane = 0x001900DE;

    /** (0019,xxDD) VR=DS VM=1 ? */
    public static final int _0019_xxDD_ = 0x001900DD;

    /** (0019,xxE0) VR=FL VM=1 ? */
    public static final int _0019_xxE0_ = 0x001900E0;

    /** (0019,xxE9) VR=FL VM=1-n Source to Detector Distance per Frame Vector */
    public static final int SourceToDetectorDistancePerFrameVector = 0x001900E9;

    /** (0019,xxEA) VR=FL VM=1 Table Rotation Angle */
    public static final int TableRotationAngle = 0x001900EA;

    /** (0019,xxEB) VR=FL VM=1 Table X Position to Iso‐center */
    public static final int TableXPositionToIsocenter = 0x001900EB;

    /** (0019,xxEC) VR=FL VM=1 Table Y Position to Iso‐center */
    public static final int TableYPositionToIsocenter = 0x001900EC;

    /** (0019,xxED) VR=FL VM=1 Table Z Position to Iso‐center */
    public static final int TableZPositionToIsocenter = 0x001900ED;

    /** (0019,xxEE) VR=FL VM=1 Table Head Tilt Angle */
    public static final int TableHeadTiltAngle = 0x001900EE;

    /** (0019,xxEF) VR=FL VM=1 ? */
    public static final int _0019_xxEF_ = 0x001900EF;

}
