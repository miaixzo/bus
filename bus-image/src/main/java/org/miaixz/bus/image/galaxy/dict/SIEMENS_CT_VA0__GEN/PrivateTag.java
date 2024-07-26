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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CT_VA0__GEN;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS CT VA0 GEN";

    /** (0019,xx10) VR=DS VM=1 Source Side Collimator Aperture */
    public static final int SourceSideCollimatorAperture = 0x00190010;

    /** (0019,xx11) VR=DS VM=1 Detector Side Collimator Aperture */
    public static final int DetectorSideCollimatorAperture = 0x00190011;

    /** (0019,xx20) VR=IS VM=1 Exposure Time */
    public static final int ExposureTime = 0x00190020;

    /** (0019,xx21) VR=IS VM=1 Exposure Current */
    public static final int ExposureCurrent = 0x00190021;

    /** (0019,xx25) VR=DS VM=1 KVP Generator Power Current */
    public static final int KVPGeneratorPowerCurrent = 0x00190025;

    /** (0019,xx26) VR=DS VM=1 Generator Voltage */
    public static final int GeneratorVoltage = 0x00190026;

    /** (0019,xx40) VR=UL VM=1 Master Control Mask */
    public static final int MasterControlMask = 0x00190040;

    /** (0019,xx42) VR=US VM=5 Processing Mask */
    public static final int ProcessingMask = 0x00190042;

    /** (0019,xx44) VR=UL VM=1-n ? */
    public static final int _0019_xx44_ = 0x00190044;

    /** (0019,xx45) VR=UL VM=1-n ? */
    public static final int _0019_xx45_ = 0x00190045;

    /** (0019,xx62) VR=IS VM=1 Number of Virtuell Channels */
    public static final int NumberOfVirtuellChannels = 0x00190062;

    /** (0019,xx70) VR=IS VM=1 Number of Readings */
    public static final int NumberOfReadings = 0x00190070;

    /** (0019,xx71) VR=CS VM=1 ? */
    public static final int _0019_xx71_ = 0x00190071;

    /** (0019,xx74) VR=IS VM=1 Number of Projections */
    public static final int NumberOfProjections = 0x00190074;

    /** (0019,xx75) VR=IS VM=1 Number of Bytes */
    public static final int NumberOfBytes = 0x00190075;

    /** (0019,xx80) VR=LO VM=1 Reconstruction Algorithm Set */
    public static final int ReconstructionAlgorithmSet = 0x00190080;

    /** (0019,xx81) VR=LO VM=1 Reconstruction Algorithm Index */
    public static final int ReconstructionAlgorithmIndex = 0x00190081;

    /** (0019,xx82) VR=LO VM=1 Regeneration Software Version */
    public static final int RegenerationSoftwareVersion = 0x00190082;

    /** (0019,xx88) VR=IS VM=1 ? */
    public static final int _0019_xx88_ = 0x00190088;

    /** (0021,xx10) VR=IS VM=1 Rotation Angle */
    public static final int RotationAngle = 0x00210010;

    /** (0021,xx11) VR=IS VM=1 Start Angle */
    public static final int StartAngle = 0x00210011;

    /** (0021,xx20) VR=IS VM=1-n ? */
    public static final int _0021_xx20_ = 0x00210020;

    /** (0021,xx30) VR=IS VM=1 Topogram Tube Position */
    public static final int TopogramTubePosition = 0x00210030;

    /** (0021,xx32) VR=DS VM=1 Length of Topogram */
    public static final int LengthOfTopogram = 0x00210032;

    /** (0021,xx34) VR=DS VM=1 Topogram Correction Factor */
    public static final int TopogramCorrectionFactor = 0x00210034;

    /** (0021,xx36) VR=DS VM=1 Maximum Table Position */
    public static final int MaximumTablePosition = 0x00210036;

    /** (0021,xx40) VR=IS VM=1 Table Move Direction Code */
    public static final int TableMoveDirectionCode = 0x00210040;

    /** (0021,xx45) VR=IS VM=1 VOI Start Row */
    public static final int VOIStartRow = 0x00210045;

    /** (0021,xx46) VR=IS VM=1 VOI Stop Row */
    public static final int VOIStopRow = 0x00210046;

    /** (0021,xx47) VR=IS VM=1 VOI Start Column */
    public static final int VOIStartColumn = 0x00210047;

    /** (0021,xx48) VR=IS VM=1 VOI Stop Column */
    public static final int VOIStopColumn = 0x00210048;

    /** (0021,xx49) VR=IS VM=1 VOI Start Slice */
    public static final int VOIStartSlice = 0x00210049;

    /** (0021,xx4A) VR=IS VM=1 VOI Stop Slice */
    public static final int VOIStopSlice = 0x0021004A;

    /** (0021,xx50) VR=IS VM=1 Vector Start Row */
    public static final int VectorStartRow = 0x00210050;

    /** (0021,xx51) VR=IS VM=1 Vector Row Step */
    public static final int VectorRowStep = 0x00210051;

    /** (0021,xx52) VR=IS VM=1 Vector Start Column */
    public static final int VectorStartColumn = 0x00210052;

    /** (0021,xx53) VR=IS VM=1 Vector Column Step */
    public static final int VectorColumnStep = 0x00210053;

    /** (0021,xx60) VR=IS VM=1 Range Type Code */
    public static final int RangeTypeCode = 0x00210060;

    /** (0021,xx62) VR=IS VM=1 Reference Type Code */
    public static final int ReferenceTypeCode = 0x00210062;

    /** (0021,xx70) VR=DS VM=3 Object Orientation */
    public static final int ObjectOrientation = 0x00210070;

    /** (0021,xx72) VR=DS VM=3 Light Orientation */
    public static final int LightOrientation = 0x00210072;

    /** (0021,xx75) VR=DS VM=1 Light Brightness */
    public static final int LightBrightness = 0x00210075;

    /** (0021,xx76) VR=DS VM=1 Light Contrast */
    public static final int LightContrast = 0x00210076;

    /** (0021,xx7A) VR=IS VM=2 Overlay Threshold */
    public static final int OverlayThreshold = 0x0021007A;

    /** (0021,xx7B) VR=IS VM=2 Surface Threshold */
    public static final int SurfaceThreshold = 0x0021007B;

    /** (0021,xx7C) VR=IS VM=2 Grey Scale Threshold */
    public static final int GreyScaleThreshold = 0x0021007C;

    /** (0021,xxA0) VR=IS VM=1 ? */
    public static final int _0021_xxA0_ = 0x002100A0;

    /** (0021,xxA2) VR=CS VM=1 ? */
    public static final int _0021_xxA2_ = 0x002100A2;

    /** (0021,xxA7) VR=LO VM=1 ? */
    public static final int _0021_xxA7_ = 0x002100A7;

}
