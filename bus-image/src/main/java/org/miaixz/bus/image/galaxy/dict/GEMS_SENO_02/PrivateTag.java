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
package org.miaixz.bus.image.galaxy.dict.GEMS_SENO_02;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_SENO_02";

    /** (0045,xx01) VR=LO VM=1 Digital Senograph Configuration */
    public static final int DigitalSenographConfiguration = 0x00450001;

    /** (0045,xx02) VR=LT VM=1 System Series Description */
    public static final int SystemSeriesDescription = 0x00450002;

    /** (0045,xx03) VR=CS VM=1 Track */
    public static final int Track = 0x00450003;

    /** (0045,xx04) VR=CS VM=1 AES */
    public static final int AES = 0x00450004;

    /** (0045,xx06) VR=DS VM=1 Angulation */
    public static final int Angulation = 0x00450006;

    /** (0045,xx07) VR=DS VM=1 Compression Thickness */
    public static final int CompressionThickness = 0x00450007;

    /** (0045,xx08) VR=DS VM=1 Compression Force */
    public static final int CompressionForce = 0x00450008;

    /** (0045,xx09) VR=DS VM=1 Real Magnification Factor */
    public static final int RealMagnificationFactor = 0x00450009;

    /** (0045,xx0A) VR=DS VM=1 Displayed Magnification Factor */
    public static final int DisplayedMagnificationFactor = 0x0045000A;

    /** (0045,xx0B) VR=CS VM=1 Senograph Type */
    public static final int SenographType = 0x0045000B;

    /** (0045,xx0C) VR=DS VM=1 Integration Time */
    public static final int IntegrationTime = 0x0045000C;

    /** (0045,xx0D) VR=DS VM=1 ROI Origin X and Y */
    public static final int ROIOriginXY = 0x0045000D;

    /** (0045,xx0E) VR=CS VM=1 Correction Type */
    public static final int CorrectionType = 0x0045000E;

    /** (0045,xx0F) VR=CS VM=1 Acquisition Type */
    public static final int AcquisitionType = 0x0045000F;

    /** (0045,xx10) VR=DS VM=2 CCD Temperature */
    public static final int CCDTemperature = 0x00450010;

    /** (0045,xx11) VR=DS VM=2 Receptor Size cm X and Y */
    public static final int ReceptorSizeCmXY = 0x00450011;

    /** (0045,xx12) VR=IS VM=2 Receptor Size Pixels X and Y */
    public static final int ReceptorSizePixelsXY = 0x00450012;

    /** (0045,xx13) VR=ST VM=1 Screen */
    public static final int Screen = 0x00450013;

    /** (0045,xx14) VR=DS VM=1 Pixel Pitch Microns */
    public static final int PixelPitchMicrons = 0x00450014;

    /** (0045,xx15) VR=IS VM=1 Pixel Depth Bits */
    public static final int PixelDepthBits = 0x00450015;

    /** (0045,xx16) VR=IS VM=2 Binning Factor X and Y */
    public static final int BinningFactorXY = 0x00450016;

    /** (0045,xx17) VR=DS VM=1 Quantum Gain */
    public static final int QuantumGain = 0x00450017;

    /** (0045,xx18) VR=DS VM=1 Electron/EDU Ratio */
    public static final int ElectronEDURatio = 0x00450018;

    /** (0045,xx19) VR=DS VM=1 Electronic Gain */
    public static final int ElectronicGain = 0x00450019;

    /** (0045,xx1A) VR=OB VM=1 IDS Data Buffer */
    public static final int IDSDataBuffer = 0x0045001A;

    /** (0045,xx1B) VR=LO VM=1 Clinical View */
    public static final int ClinicalView = 0x0045001B;

    /** (0045,xx1C) VR=CS VM=1 Breast Laterality */
    public static final int BreastLaterality = 0x0045001C;

    /** (0045,xx1D) VR=DS VM=1 Mean Of Raw Gray Levels */
    public static final int MeanOfRawGrayLevels = 0x0045001D;

    /** (0045,xx1E) VR=DS VM=1 Mean Of Offset Gray Levels */
    public static final int MeanOfOffsetGrayLevels = 0x0045001E;

    /** (0045,xx1F) VR=DS VM=1 Mean Of Corrected Gray Levels */
    public static final int MeanOfCorrectedGrayLevels = 0x0045001F;

    /** (0045,xx20) VR=DS VM=1 Mean Of Region Gray Levels */
    public static final int MeanOfRegionGrayLevels = 0x00450020;

    /** (0045,xx21) VR=DS VM=1 Mean Of Log Region Gray Levels */
    public static final int MeanOfLogRegionGrayLevels = 0x00450021;

    /** (0045,xx22) VR=DS VM=1 Standard Deviation Of Raw Gray Levels */
    public static final int StandardDeviationOfRawGrayLevels = 0x00450022;

    /** (0045,xx23) VR=DS VM=1 Standard Deviation Of Corrected Gray Levels */
    public static final int StandardDeviationOfCorrectedGrayLevels = 0x00450023;

    /** (0045,xx24) VR=DS VM=1 Standard Deviation Of Region Gray Levels */
    public static final int StandardDeviationOfRegionGrayLevels = 0x00450024;

    /** (0045,xx25) VR=DS VM=1 Standard Deviation Of Log Region Gray Levels */
    public static final int StandardDeviationOfLogRegionGrayLevels = 0x00450025;

    /** (0045,xx26) VR=OB VM=1 MAO Buffer */
    public static final int MAOBuffer = 0x00450026;

    /** (0045,xx27) VR=IS VM=1 Set Number */
    public static final int SetNumber = 0x00450027;

    /** (0045,xx28) VR=CS VM=1 WindowingType (LINEAR or GAMMA) */
    public static final int WindowingType = 0x00450028;

    /** (0045,xx29) VR=DS VM=1-n WindowingParameters */
    public static final int WindowingParameters = 0x00450029;

    /** (0045,xx2A) VR=IS VM=1 Crosshair Cursor X Coordinates */
    public static final int CrosshairCursorXCoordinates = 0x0045002A;

    /** (0045,xx2B) VR=IS VM=1 Crosshair Cursor Y Coordinates */
    public static final int CrosshairCursorYCoordinates = 0x0045002B;

    /** (0045,xx2C) VR=DS VM=1 Reference Landmark A X 3D Coordinates */
    public static final int ReferenceLandmarkAX3DCoordinates = 0x0045002C;

    /** (0045,xx2D) VR=DS VM=1 Reference Landmark A Y 3D Coordinates */
    public static final int ReferenceLandmarkAY3DCoordinates = 0x0045002D;

    /** (0045,xx2E) VR=DS VM=1 Reference Landmark A Z 3D Coordinates */
    public static final int ReferenceLandmarkAZ3DCoordinates = 0x0045002E;

    /** (0045,xx2F) VR=IS VM=1 Reference Landmark A X Image Coordinates */
    public static final int ReferenceLandmarkAXImageCoordinates = 0x0045002F;

    /** (0045,xx30) VR=IS VM=1 Reference Landmark A Y Image Coordinates */
    public static final int ReferenceLandmarkAYImageCoordinates = 0x00450030;

    /** (0045,xx31) VR=DS VM=1 Reference Landmark B X 3D Coordinates */
    public static final int ReferenceLandmarkBX3DCoordinates = 0x00450031;

    /** (0045,xx32) VR=DS VM=1 Reference Landmark B Y 3D Coordinates */
    public static final int ReferenceLandmarkBY3DCoordinates = 0x00450032;

    /** (0045,xx33) VR=DS VM=1 Reference Landmark B Z 3D Coordinates */
    public static final int ReferenceLandmarkBZ3DCoordinates = 0x00450033;

    /** (0045,xx34) VR=IS VM=1 Reference Landmark B X Image Coordinates */
    public static final int ReferenceLandmarkBXImageCoordinates = 0x00450034;

    /** (0045,xx35) VR=IS VM=1 Reference Landmark B Y Image Coordinates */
    public static final int ReferenceLandmarkBYImageCoordinates = 0x00450035;

    /** (0045,xx36) VR=DS VM=1 X-Ray Source X Location */
    public static final int XRaySourceXLocation = 0x00450036;

    /** (0045,xx37) VR=DS VM=1 X-Ray Source Y Locatio */
    public static final int XRaySourceYLocation = 0x00450037;

    /** (0045,xx38) VR=DS VM=1 X-Ray Source Z Locatio */
    public static final int XRaySourceZLocation = 0x00450038;

    /** (0045,xx39) VR=US VM=1 Vignette Rows */
    public static final int VignetteRows = 0x00450039;

    /** (0045,xx3A) VR=US VM=1 Vignette Columns */
    public static final int VignetteColumns = 0x0045003A;

    /** (0045,xx3B) VR=US VM=1 Vignette Bits Allocated */
    public static final int VignetteBitsAllocated = 0x0045003B;

    /** (0045,xx3C) VR=US VM=1 Vignette Bits Stored */
    public static final int VignetteBitsStored = 0x0045003C;

    /** (0045,xx3D) VR=US VM=1 Vignette High Bit */
    public static final int VignetteHighBit = 0x0045003D;

    /** (0045,xx3E) VR=US VM=1 Vignette Pixel Representation */
    public static final int VignettePixelRepresentation = 0x0045003E;

    /** (0045,xx3F) VR=OB VM=1 Vignette Pixel Data */
    public static final int VignettePixelData = 0x0045003F;

    /** (0045,xx49) VR=DS VM=1 Radiological Thickness */
    public static final int RadiologicalThickness = 0x00450049;

    /** (0045,xx50) VR=UI VM=1 Fallback Instance UID (CR or SC) */
    public static final int FallbackInstanceUID = 0x00450050;

    /** (0045,xx51) VR=UI VM=1 Fallback Series UID (CR or SC) */
    public static final int FallbackSeriesUID = 0x00450051;

    /** (0045,xx52) VR=IS VM=1 Raw Diagnostic Low */
    public static final int RawDiagnosticLow = 0x00450052;

    /** (0045,xx53) VR=IS VM=1 Raw Diagnostic High */
    public static final int RawDiagnosticHigh = 0x00450053;

    /** (0045,xx54) VR=DS VM=1 Exponent */
    public static final int Exponent = 0x00450054;

    /** (0045,xx55) VR=IS VM=1 A Coefficients */
    public static final int ACoefficients = 0x00450055;

    /** (0045,xx56) VR=DS VM=1 Noise Reduction Sensitivity */
    public static final int NoiseReductionSensitivity = 0x00450056;

    /** (0045,xx57) VR=DS VM=1 Noise Reduction Threshold */
    public static final int NoiseReductionThreshold = 0x00450057;

    /** (0045,xx58) VR=DS VM=1 Mu */
    public static final int Mu = 0x00450058;

    /** (0045,xx59) VR=IS VM=1 Threshold */
    public static final int Threshold = 0x00450059;

    /** (0045,xx60) VR=IS VM=4 Breast ROI X */
    public static final int BreastROIX = 0x00450060;

    /** (0045,xx61) VR=IS VM=4 Breast ROI Y */
    public static final int BreastROIY = 0x00450061;

    /** (0045,xx62) VR=IS VM=1 User Window Center */
    public static final int UserWindowCenter = 0x00450062;

    /** (0045,xx63) VR=IS VM=1 User Window Width */
    public static final int UserWindowWidth = 0x00450063;

    /** (0045,xx64) VR=IS VM=1 Segmentation Threshold */
    public static final int SegmentationThreshold = 0x00450064;

    /** (0045,xx65) VR=IS VM=1 Detector Entrance Dose */
    public static final int DetectorEntranceDose = 0x00450065;

    /** (0045,xx66) VR=IS VM=1 Asymmetrical Collimation Information */
    public static final int AsymmetricalCollimationInformation = 0x00450066;

    /** (0045,xx71) VR=OB VM=1 STX Buffer */
    public static final int STXBuffer = 0x00450071;

    /** (0045,xx72) VR=DS VM=2 Image Crop Point */
    public static final int ImageCropPoint = 0x00450072;

    /** (0045,xx90) VR=SH VM=1 Premium View Beta */
    public static final int PremiumViewBeta = 0x00450090;

    /** (0045,xxA0) VR=DS VM=1 Signal Average Factor */
    public static final int SignalAverageFactor = 0x004500A0;

    /** (0045,xxA1) VR=DS VM=2-n Organ Dose for Source Images */
    public static final int OrganDoseForSourceImages = 0x004500A1;

    /** (0045,xxA2) VR=DS VM=2-n Entrance dose in mGy for Source Images */
    public static final int EntranceDoseInmGyForSourceImages = 0x004500A2;

    /** (0045,xxA4) VR=DS VM=1 Organ Dose in dGy for Complete DBT Sequence */
    public static final int OrganDoseIndGyForCompleteDBTSequence = 0x004500A4;

    /** (0045,xxA6) VR=UI VM=1 SOP Instance UID for Lossy Compression */
    public static final int SOPInstanceUIDForLossyCompression = 0x004500A6;

    /** (0045,xxA7) VR=LT VM=1 Reconstruction Parameters */
    public static final int ReconstructionParameters = 0x004500A7;

    /** (0045,xxA8) VR=DS VM=1 Entrance Dose in dGy for Complete DBT Sequence */
    public static final int EntranceDoseIndGyForCompleteDBTSequence = 0x004500A8;

    /** (0045,xxA9) VR=DS VM=1 Replacement Image */
    public static final int ReplacementImage = 0x004500A9;

    /** (0045,xxAA) VR=SQ VM=1 Replaced Image Sequence */
    public static final int ReplacemeImageSequence = 0x004500AA;

    /** (0045,xxAB) VR=DS VM=1 Cumulative Organ Dose in dGy */
    public static final int CumulativeOrganDoseIndGy = 0x004500AB;

    /** (0045,xxAC) VR=DS VM=1 Cumulative Entrance dose in mGy */
    public static final int CumulativeEntranceDoseInmGy = 0x004500AC;

    /** (0045,xxAD) VR=LO VM=1-n Paddle Properties */
    public static final int PaddleProperties = 0x004500AD;

}
