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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CT_APPL_DATASET;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS CT APPL DATASET";

    /** (0029,xx00) VR=LT VM=1 Dual Energy Algorithm Parameters */
    public static final int DualEnergyAlgorithmParameters = 0x00290000;

    /** (0029,xx01) VR=US VM=1 Valid CT Volume MBox Tasks */
    public static final int ValidCTVolumeMBoxTasks = 0x00290001;

    /** (0029,xx02) VR=LT VM=1 Scan Options */
    public static final int ScanOptions = 0x00290002;

    /** (0029,xx03) VR=ST VM=1 Acquisition Date and Time */
    public static final int AcquisitionDateandTime = 0x00290003;

    /** (0029,xx04) VR=ST VM=1 Acquisition Number */
    public static final int AcquisitionNumber = 0x00290004;

    /** (0029,xx05) VR=ST VM=1 Dynamic Data */
    public static final int DynamicData = 0x00290005;

    /** (0029,xx06) VR=DS VM=6 Image Orientation Patient */
    public static final int ImageOrientationPatient = 0x00290006;

    /** (0029,xx07) VR=LT VM=1 Frame Of Reference Uid */
    public static final int FrameOfReferenceUid = 0x00290007;

    /** (0029,xx08) VR=LT VM=1 Patient Position */
    public static final int PatientPosition = 0x00290008;

    /** (0029,xx09) VR=LT VM=1 Convolution Kernel */
    public static final int ConvolutionKernel = 0x00290009;

    /** (0029,xx10) VR=LT VM=1 Kvp */
    public static final int Kvp = 0x00290010;

    /** (0029,xx11) VR=LT VM=1 Reconstruction Diameter */
    public static final int ReconstructionDiameter = 0x00290011;

    /** (0029,xx12) VR=LT VM=1 Rescale Intercept */
    public static final int RescaleIntercept = 0x00290012;

    /** (0029,xx13) VR=LT VM=1 Rescale Slope */
    public static final int RescaleSlope = 0x00290013;

    /** (0029,xx14) VR=LT VM=1 Slice Thickness */
    public static final int SliceThickness = 0x00290014;

    /** (0029,xx15) VR=LT VM=1 Table Height */
    public static final int TableHeight = 0x00290015;

    /** (0029,xx16) VR=LT VM=1 Gantry Detector Tilt */
    public static final int GantryDetectorTilt = 0x00290016;

    /** (0029,xx17) VR=LT VM=1 Pixel Spacing */
    public static final int PixelSpacing = 0x00290017;

    /** (0029,xx18) VR=ST VM=1 Volume PatientPosition Not Equal */
    public static final int VolumePatientPositionNotEqual = 0x00290018;

    /** (0029,xx19) VR=ST VM=1 Volume LossyImageCompression Not Equal */
    public static final int VolumeLossyImageCompressionNotEqual = 0x00290019;

    /** (0029,xx20) VR=ST VM=1 Volume ConvolutionKernel Not Equal */
    public static final int VolumeConvolutionKernelNotEqual = 0x00290020;

    /** (0029,xx21) VR=ST VM=1 Volume PixelSpacing Not Equal */
    public static final int VolumePixelSpacingNotEqual = 0x00290021;

    /** (0029,xx22) VR=ST VM=1 Volume Kvp Not Equal */
    public static final int VolumeKvpNotEqual = 0x00290022;

    /** (0029,xx23) VR=ST VM=1 Volume ReconstructionDiameter Not Equal */
    public static final int VolumeReconstructionDiameterNotEqual = 0x00290023;

    /** (0029,xx24) VR=ST VM=1 Volume TableHeight Not Equal */
    public static final int VolumeTableHeightNotEqual = 0x00290024;

    /** (0029,xx25) VR=ST VM=1 Volume Has Gaps */
    public static final int VolumeHasGaps = 0x00290025;

    /** (0029,xx26) VR=ST VM=1 Volume Number Of Missing Images */
    public static final int VolumeNumberOfMissingImages = 0x00290026;

    /** (0029,xx27) VR=ST VM=1 Volume Max Gap */
    public static final int VolumeMaxGap = 0x00290027;

    /** (0029,xx28) VR=LT VM=1 Volume Position Of Gaps */
    public static final int VolumePositionOfGaps = 0x00290028;

    /** (0029,xx29) VR=FD VM=1 Calibration Factor */
    public static final int CalibrationFactor = 0x00290029;

    /** (0029,xx2A) VR=CS VM=1 Flash Mode */
    public static final int FlashMode = 0x0029002A;

    /** (0029,xx2B) VR=LT VM=1 Warnings */
    public static final int Warnings = 0x0029002B;

    /** (0029,xx2C) VR=ST VM=1 Volume HighBit Not Equal */
    public static final int VolumeHighBitNotEqual = 0x0029002C;

    /** (0029,xx2D) VR=ST VM=1 Volume ImageType Not Equal */
    public static final int VolumeImageTypeNotEqual = 0x0029002D;

    /** (0029,xx2E) VR=ST VM=1 ImageType 0 */
    public static final int ImageType0 = 0x0029002E;

    /** (0029,xx2F) VR=ST VM=1 ImageType 1 */
    public static final int ImageType1 = 0x0029002F;

    /** (0029,xx30) VR=ST VM=1 ImageType 2 */
    public static final int ImageType2 = 0x00290030;

    /** (0029,xx31) VR=ST VM=1 ImageType 3 */
    public static final int ImageType3 = 0x00290031;

    /** (0029,xx32) VR=ST VM=1 PhotometricInterpretation not MONOCHROME2 */
    public static final int PhotometricInterpretationNotMONOCHROME2 = 0x00290032;

    /** (0029,xx33) VR=DA VM=1 First Acquisition Date */
    public static final int FirstAcquisitionDate = 0x00290033;

    /** (0029,xx34) VR=DA VM=1 Last Acquisition Date */
    public static final int LastAcquisitionDate = 0x00290034;

    /** (0029,xx35) VR=TM VM=1 First Acquisition Time */
    public static final int FirstAcquisitionTime = 0x00290035;

    /** (0029,xx36) VR=TM VM=1 Last Acquisition Time */
    public static final int LastAcquisitionTime = 0x00290036;

    /** (0029,xx37) VR=ST VM=1 Internal Data */
    public static final int InternalData = 0x00290037;

    /** (0029,xx38) VR=ST VM=1 Ranges SOM7 */
    public static final int RangesSOM7 = 0x00290038;

    /** (0029,xx39) VR=LT VM=1 Calculated Gantry Detector Tilt */
    public static final int CalculatedGantryDetectorTilt = 0x00290039;

    /** (0029,xx40) VR=ST VM=1 Volume Slice Distance */
    public static final int VolumeSliceDistance = 0x00290040;

    /** (0029,xx41) VR=DS VM=1 First Slice Z Coordinate */
    public static final int FirstSliceZCoordinate = 0x00290041;

    /** (0029,xx42) VR=DS VM=1 Last Slice Z Coordinate */
    public static final int LastSliceZCoordinate = 0x00290042;

    /** (0029,xx43) VR=DS VM=1 Content DateTime */
    public static final int ContentDateTime = 0x00290043;

    /** (0029,xx44) VR=DS VM=1 Delta Time */
    public static final int DeltaTime = 0x00290044;

    /** (0029,xx45) VR=DS VM=1 Frame Count */
    public static final int FrameCount = 0x00290045;

}
