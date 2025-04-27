/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_IMAGING_DD_001_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "PHILIPS IMAGING DD 001";

    /** (2001,xx01) VR=FL VM=1 Chemical Shift */
    public static final int ChemicalShift = 0x20010001;

    /** (2001,xx02) VR=IS VM=1 Chemical Shift Number MR */
    public static final int ChemicalShiftNumberMR = 0x20010002;

    /** (2001,xx03) VR=FL VM=1 Diffusion B-Factor */
    public static final int DiffusionBFactor = 0x20010003;

    /** (2001,xx04) VR=CS VM=1 Diffusion Direction */
    public static final int DiffusionDirection = 0x20010004;

    /** (2001,xx05) VR=SS VM=1 Graphic Annotation ParentID */
    public static final int GraphicAnnotationParentID = 0x20010005;

    /** (2001,xx06) VR=CS VM=1 Image Enhanced */
    public static final int ImageEnhanced = 0x20010006;

    /** (2001,xx07) VR=CS VM=1 Image Type End Diastole or End Systole */
    public static final int ImageTypeEDES = 0x20010007;

    /** (2001,xx08) VR=IS VM=1 Phase Number */
    public static final int PhaseNumber = 0x20010008;

    /** (2001,xx09) VR=FL VM=1 Image Prepulse Delay */
    public static final int ImagePrepulseDelay = 0x20010009;

    /** (2001,xx0A) VR=IS VM=1 Image Plane Number */
    public static final int ImagePlaneNumber = 0x2001000A;

    /** (2001,xx0B) VR=CS VM=1 Image Orientation */
    public static final int ImageOrientation = 0x2001000B;

    /** (2001,xx0C) VR=CS VM=1 Arrhythmia Rejection */
    public static final int ArrhythmiaRejection = 0x2001000C;

    /** (2001,xx0E) VR=CS VM=1 Cardiac Cycled */
    public static final int CardiacCycled = 0x2001000E;

    /** (2001,xx0F) VR=SS VM=1 Cardiac Gate Width */
    public static final int CardiacGateWidth = 0x2001000F;

    /** (2001,xx10) VR=CS VM=1 Cardiac Sync */
    public static final int CardiacSync = 0x20010010;

    /** (2001,xx11) VR=FL VM=1 Diffusion Echo Time */
    public static final int DiffusionEchoTime = 0x20010011;

    /** (2001,xx12) VR=CS VM=1 Dynamic Series */
    public static final int DynamicSeries = 0x20010012;

    /** (2001,xx13) VR=SL VM=1 EPI Factor */
    public static final int EPIFactor = 0x20010013;

    /** (2001,xx14) VR=SL VM=1 Number of Echoes */
    public static final int NumberOfEchoes = 0x20010014;

    /** (2001,xx15) VR=SS VM=1 Number of Locations */
    public static final int NumberOfLocations = 0x20010015;

    /** (2001,xx16) VR=SS VM=1 Number of PC Directions */
    public static final int NumberOfPCDirections = 0x20010016;

    /** (2001,xx17) VR=SL VM=1 Number of Phases */
    public static final int NumberOfPhases = 0x20010017;

    /** (2001,xx18) VR=SL VM=1 Number of Slices */
    public static final int NumberOfSlices = 0x20010018;

    /** (2001,xx19) VR=CS VM=1 Partial Matrix Scanned */
    public static final int PartialMatrixScanned = 0x20010019;

    /** (2001,xx1A) VR=FL VM=3 PC Velocity */
    public static final int PCVelocity = 0x2001001A;

    /** (2001,xx1B) VR=FL VM=1 Prepulse Delay */
    public static final int PrepulseDelay = 0x2001001B;

    /** (2001,xx1C) VR=CS VM=1 Prepulse Type */
    public static final int PrepulseType = 0x2001001C;

    /** (2001,xx1D) VR=IS VM=1 Reconstruction Number */
    public static final int ReconstructionNumber = 0x2001001D;

    /** (2001,xx1E) VR=CS VM=1 Reformat Accuracy */
    public static final int ReformatAccuracy = 0x2001001E;

    /** (2001,xx1F) VR=CS VM=1 Respiration Sync */
    public static final int RespirationSync = 0x2001001F;

    /** (2001,xx20) VR=LO VM=1 Scanning Technique */
    public static final int ScanningTechnique = 0x20010020;

    /** (2001,xx21) VR=CS VM=1 SPIR */
    public static final int SPIR = 0x20010021;

    /** (2001,xx22) VR=FL VM=1 Water-Fat Shift */
    public static final int WaterFatShift = 0x20010022;

    /** (2001,xx23) VR=DS VM=1 Flip Angle */
    public static final int FlipAngle = 0x20010023;

    /** (2001,xx24) VR=CS VM=1 Series is Interactive */
    public static final int SeriesIsInteractive = 0x20010024;

    /** (2001,xx25) VR=SH VM=1 Echo Time Display */
    public static final int EchoTimeDisplay = 0x20010025;

    /**
     * (2001,xx26) VR=CS VM=1 Presentation State Subtraction Active
     */
    public static final int PresentationStateSubtractionActive = 0x20010026;

    /** (2001,xx29) VR=FL VM=1 ? */
    public static final int _2001_xx29_ = 0x20010029;

    /** (2001,xx2B) VR=CS VM=1 ? */
    public static final int _2001_xx2B_ = 0x2001002B;

    /** (2001,xx2D) VR=SS VM=1 Number of Slices in Stack */
    public static final int NumberOfSlicesInStack = 0x2001002D;

    /** (2001,xx32) VR=FL VM=1 Stack Radial Angle */
    public static final int StackRadialAngle = 0x20010032;

    /** (2001,xx33) VR=CS VM=1 Stack Radial Axis */
    public static final int StackRadialAxis = 0x20010033;

    /** (2001,xx35) VR=SS VM=1 Stack Slice Number */
    public static final int StackSliceNumber = 0x20010035;

    /** (2001,xx36) VR=CS VM=1 Stack Type */
    public static final int StackType = 0x20010036;

    /** (2001,xx39) VR=FL VM=1 ? */
    public static final int _2001_xx39_ = 0x20010039;

    /** (2001,xx3D) VR=UL VM=1 Contour Fill Color */
    public static final int ContourFillColor = 0x2001003D;

    /**
     * (2001,xx3F) VR=CS VM=1 Displayed Area Zoom Interpolation Method
     */
    public static final int DisplayedAreaZoomInterpolationMeth = 0x2001003F;

    /**
     * (2001,xx43) VR=IS VM=2 Ellipse Display Shutter Major Axis First End Point
     */
    public static final int EllipsDisplShutMajorAxFrstEndPnt = 0x20010043;

    /**
     * (2001,xx44) VR=IS VM=2 Ellipse Display Shutter Major Axis Second End Point
     */
    public static final int EllipsDisplShutMajorAxScndEndPnt = 0x20010044;

    /**
     * (2001,xx45) VR=IS VM=2 Ellipse Display Shutter Other Axis First End Point
     */
    public static final int EllipsDisplShutOtherAxFrstEndPnt = 0x20010045;

    /** (2001,xx46) VR=CS VM=1 Graphic Line Style */
    public static final int GraphicLineStyle = 0x20010046;

    /** (2001,xx47) VR=FL VM=1 Graphic Line Width */
    public static final int GraphicLineWidth = 0x20010047;

    /** (2001,xx48) VR=SS VM=1 Graphic Annotation ID */
    public static final int GraphicAnnotationID = 0x20010048;

    /** (2001,xx4B) VR=CS VM=1 Interpolation Method */
    public static final int InterpolationMethod = 0x2001004B;

    /** (2001,xx4C) VR=CS VM=1 Poly Line Begin Point Style */
    public static final int PolyLineBeginPointStyle = 0x2001004C;

    /** (2001,xx4D) VR=CS VM=1 Poly Line End Point Style */
    public static final int PolyLineEndPointStyle = 0x2001004D;

    /** (2001,xx4E) VR=CS VM=1 Window Smoothing Taste */
    public static final int WindowSmoothingTaste = 0x2001004E;

    /** (2001,xx50) VR=LO VM=1 Graphic Marker Type */
    public static final int GraphicMarkerType = 0x20010050;

    /** (2001,xx51) VR=IS VM=1 Overlay Plane ID */
    public static final int OverlayPlaneID = 0x20010051;

    /** (2001,xx52) VR=UI VM=1 Image Presentation State UID */
    public static final int ImagePresentationStateUID = 0x20010052;

    /** (2001,xx53) VR=CS VM=1 Presentation GL Transform Invert */
    public static final int PresentationGLTrafoInvert = 0x20010053;

    /** (2001,xx54) VR=FL VM=1 Contour Fill Transparency */
    public static final int ContourFillTransparency = 0x20010054;

    /** (2001,xx55) VR=UL VM=1 Graphic Line Color */
    public static final int GraphicLineColor = 0x20010055;

    /** (2001,xx56) VR=CS VM=1 Graphic Type */
    public static final int GraphicType = 0x20010056;

    /** (2001,xx58) VR=UL VM=1 Contrast Transfer Taste */
    public static final int ContrastTransferTaste = 0x20010058;

    /** (2001,xx5A) VR=ST VM=1 Graphic Annotation Model */
    public static final int GraphicAnnotationModel = 0x2001005A;

    /** (2001,xx5D) VR=ST VM=1 Measurement Text Units */
    public static final int MeasurementTextUnits = 0x2001005D;

    /** (2001,xx5E) VR=ST VM=1 Measurement Text Type */
    public static final int MeasurementTextType = 0x2001005E;

    /** (2001,xx5F) VR=SQ VM=1 Stack Sequence */
    public static final int StackSequence = 0x2001005F;

    /** (2001,xx60) VR=SL VM=1 Number of Stacks */
    public static final int NumberOfStacks = 0x20010060;

    /** (2001,xx61) VR=CS VM=1 Series Transmitted */
    public static final int SeriesTransmitted = 0x20010061;

    /** (2001,xx62) VR=CS VM=1 Series Committed */
    public static final int SeriesCommitted = 0x20010062;

    /** (2001,xx63) VR=CS VM=1 Examination Source */
    public static final int ExaminationSource = 0x20010063;

    /** (2001,xx64) VR=SH VM=1 Text Type */
    public static final int TextType = 0x20010064;

    /** (2001,xx65) VR=SQ VM=1 Graphic Overlay Plane */
    public static final int GraphicOverlayPlane = 0x20010065;

    /**
     * (2001,xx67) VR=CS VM=1 Linear Presentation GL Transform Shape Sub
     */
    public static final int LinearPresentationGLTrafoShapeSub = 0x20010067;

    /** (2001,xx68) VR=SQ VM=1 Linear Modality GL Transform */
    public static final int LinearModalityGLTrafo = 0x20010068;

    /** (2001,xx69) VR=SQ VM=1 Display Shutter */
    public static final int DisplayShutter = 0x20010069;

    /** (2001,xx6A) VR=SQ VM=1 Spatial Transformation */
    public static final int SpatialTransformation = 0x2001006A;

    /** (2001,xx6B) VR=SQ VM=1 ? */
    public static final int _2001_xx6B_ = 0x2001006B;

    /** (2001,xx6D) VR=LO VM=1 Text Font */
    public static final int TextFont = 0x2001006D;

    /** (2001,xx6E) VR=SH VM=1 Series Type */
    public static final int SeriesType = 0x2001006E;

    /** (2001,xx71) VR=CS VM=1 Graphic Constraint */
    public static final int GraphicConstraint = 0x20010071;

    /**
     * (2001,xx72) VR=IS VM=1 Ellipse Display Shutter Other Axis Second End Point
     */
    public static final int EllipsDisplShutOtherAxScndEndPnt = 0x20010072;

    /** (2001,xx74) VR=DS VM=1 ? */
    public static final int _2001_xx74_ = 0x20010074;

    /** (2001,xx75) VR=DS VM=1 ? */
    public static final int _2001_xx75_ = 0x20010075;

    /** (2001,xx76) VR=UL VM=1 Number of Frames */
    public static final int NumberOfFrames = 0x20010076;

    /** (2001,xx77) VR=CS VM=1 GL Transform Type */
    public static final int GLTrafoType = 0x20010077;

    /** (2001,xx7A) VR=FL VM=1 Window Rounding Factor */
    public static final int WindowRoundingFactor = 0x2001007A;

    /** (2001,xx7B) VR=IS VM=1 Acquisition Number */
    public static final int AcquisitionNumber = 0x2001007B;

    /** (2001,xx7C) VR=US VM=1 Frame Number */
    public static final int FrameNumber = 0x2001007C;

    /** (2001,xx80) VR=LO VM=1 ? */
    public static final int _2001_xx80_ = 0x20010080;

    /** (2001,xx81) VR=IS VM=1 Number of Dynamic Scans */
    public static final int NumberOfDynamicScans = 0x20010081;

    /** (2001,xx82) VR=IS VM=1 Echo Train Length */
    public static final int EchoTrainLength = 0x20010082;

    /** (2001,xx83) VR=DS VM=1 Imaging Frequency */
    public static final int ImagingFrequency = 0x20010083;

    /** (2001,xx84) VR=DS VM=1 Inversion Time */
    public static final int InversionTime = 0x20010084;

    /** (2001,xx85) VR=DS VM=1 Magnetic Field Strength */
    public static final int MagneticFieldStrength = 0x20010085;

    /** (2001,xx86) VR=IS VM=1 Number of Phase Encoding Steps */
    public static final int NrOfPhaseEncodingSteps = 0x20010086;

    /** (2001,xx87) VR=SH VM=1 Imaged Nucleus */
    public static final int ImagedNucleus = 0x20010087;

    /** (2001,xx88) VR=DS VM=1 Number of Averages */
    public static final int NumberOfAverages = 0x20010088;

    /** (2001,xx89) VR=DS VM=1 Phase FOV Percent */
    public static final int PhaseFOVPercent = 0x20010089;

    /** (2001,xx8A) VR=DS VM=1 Sampling Percent */
    public static final int SamplingPercent = 0x2001008A;

    /** (2001,xx8B) VR=SH VM=1 Transmitting Coil */
    public static final int TransmittingCoil = 0x2001008B;

    /** (2001,xx90) VR=LO VM=1 Text Foreground Color */
    public static final int TextForegroundColor = 0x20010090;

    /** (2001,xx91) VR=LO VM=1 Text Background Color */
    public static final int TextBackgroundColor = 0x20010091;

    /** (2001,xx92) VR=LO VM=1 Text Shadow Color */
    public static final int TextShadowColor = 0x20010092;

    /** (2001,xx93) VR=LO VM=1 Text Style */
    public static final int TextStyle = 0x20010093;

    /** (2001,xx9A) VR=SQ VM=1 ? */
    public static final int _2001_xx9A_ = 0x2001009A;

    /** (2001,xx9B) VR=UL VM=1 Graphic Number */
    public static final int GraphicNumber = 0x2001009B;

    /** (2001,xx9C) VR=LO VM=1 Graphic Annotation Label */
    public static final int GraphicAnnotationLabel = 0x2001009C;

    /** (2001,xx9F) VR=US VM=2 Pixel Processing Kernel Size */
    public static final int PixelProcessingKernelSize = 0x2001009F;

    /** (2001,xxA1) VR=CS VM=1 Is Raw Image */
    public static final int IsRawImage = 0x200100A1;

    /** (2001,xxA3) VR=UL VM=1 Text Color Foreground */
    public static final int TextColorForeground = 0x200100A3;

    /** (2001,xxA4) VR=UL VM=1 Text Color Background */
    public static final int TextColorBackground = 0x200100A4;

    /** (2001,xxA5) VR=UL VM=1 Text Color Shadow */
    public static final int TextColorShadow = 0x200100A5;

    /** (2001,xxC8) VR=LO VM=1 Exam Card Name */
    public static final int ExamCardName = 0x200100C8;

    /** (2001,xxCC) VR=ST VM=1 Derivation Description */
    public static final int DerivationDescription = 0x200100CC;

    /** (2001,xxDA) VR=CS VM=1 ? */
    public static final int _2001_xxDA_ = 0x200100DA;

    /** (2001,xxF1) VR=FL VM=1-n Prospective Motion Correction */
    public static final int ProspectiveMotionCorrection = 0x200100F1;

    /** (2001,xxF2) VR=FL VM=1-n Retrospective Motion Correction */
    public static final int RetrospectiveMotionCorrection = 0x200100F2;

}
