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
package org.miaixz.bus.image.galaxy.dict.GEMS_GDXE_FALCON_04;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_GDXE_FALCON_04";

    /** (0011,xx03) VR=UI VM=1 Processed Series UID */
    public static final int ProcessedSeriesUID = 0x00110003;

    /** (0011,xx04) VR=CS VM=1 Acquisition Type */
    public static final int AcquisitionType = 0x00110004;

    /** (0011,xx05) VR=UI VM=1 Acquisition UID */
    public static final int AcquisitionUID = 0x00110005;

    /** (0011,xx06) VR=DS VM=1 Image Dose */
    public static final int ImageDose = 0x00110006;

    /** (0011,xx07) VR=FL VM=1 Study Dose */
    public static final int StudyDose = 0x00110007;

    /** (0011,xx08) VR=FL VM=1 Study DAP */
    public static final int StudyDAP = 0x00110008;

    /** (0011,xx09) VR=SL VM=1 Non-Digital Exposures */
    public static final int NonDigitalExposures = 0x00110009;

    /** (0011,xx10) VR=SL VM=1 Total Exposures */
    public static final int TotalExposures = 0x00110010;

    /** (0011,xx11) VR=LT VM=1 ROI */
    public static final int ROI = 0x00110011;

    /** (0011,xx12) VR=LT VM=1 Patient Size String */
    public static final int PatientSizeString = 0x00110012;

    /** (0011,xx13) VR=UI VM=1 SPS UID */
    public static final int SPSUID = 0x00110013;

    /** (0011,xx14) VR=UI VM=1 ? */
    public static final int _0011_xx14_ = 0x00110014;

    /** (0011,xx15) VR=DS VM=1 Detector ARC Gain */
    public static final int DetectorARCGain = 0x00110015;

    /** (0011,xx16) VR=LT VM=1 Processing Debug Info */
    public static final int ProcessingDebugInfo = 0x00110016;

    /** (0011,xx17) VR=CS VM=1 Override Mode */
    public static final int OverrideMode = 0x00110017;

    /** (0011,xx19) VR=DS VM=1 Film Speed Selection */
    public static final int FilmSpeedSelection = 0x00110019;

    /** (0011,xx27) VR=UN VM=1 ? */
    public static final int _0011_xx27_ = 0x00110027;

    /** (0011,xx28) VR=UN VM=1 ? */
    public static final int _0011_xx28_ = 0x00110028;

    /** (0011,xx29) VR=UN VM=1 ? */
    public static final int _0011_xx29_ = 0x00110029;

    /** (0011,xx30) VR=UN VM=1 ? */
    public static final int _0011_xx30_ = 0x00110030;

    /** (0011,xx31) VR=IS VM=1-n Detected Field of View */
    public static final int DetectedFieldOfView = 0x00110031;

    /** (0011,xx32) VR=IS VM=1-n Adjusted Field of View */
    public static final int AdjustedFieldOfView = 0x00110032;

    /** (0011,xx33) VR=DS VM=1 Detector Exposure Index */
    public static final int DetectorExposureIndex = 0x00110033;

    /** (0011,xx34) VR=DS VM=1 Compensated Detector Exposure */
    public static final int CompensatedDetectorExposure = 0x00110034;

    /** (0011,xx35) VR=DS VM=1 Uncompensated Detector Exposure */
    public static final int UncompensatedDetectorExposure = 0x00110035;

    /** (0011,xx36) VR=DS VM=1 Median Anatomy Count Value */
    public static final int MedianAnatomyCountValue = 0x00110036;

    /** (0011,xx37) VR=DS VM=2 DEI Lower/Upper Limit Values */
    public static final int DEILowerAndUpperLimitValues = 0x00110037;

    /** (0011,xx38) VR=SL VM=6 Shift Vector for Pasting */
    public static final int ShiftVectorForPasting = 0x00110038;

    /** (0011,xx39) VR=CS VM=6 Image Number in Pasting */
    public static final int ImageNumberInPasting = 0x00110039;

    /** (0011,xx40) VR=SL VM=1 Pasting Overlap */
    public static final int PastingOverlap = 0x00110040;

    /** (0011,xx41) VR=IS VM=24 Sub-image Collimator Vertices */
    public static final int SubImageCollimatorVertices = 0x00110041;

    /** (0011,xx42) VR=LO VM=1 View IP */
    public static final int ViewIP = 0x00110042;

    /** (0011,xx43) VR=IS VM=24 Keystone Coordinates */
    public static final int KeystoneCoordinates = 0x00110043;

    /** (0011,xx44) VR=CS VM=1 Receptor Type */
    public static final int ReceptorType = 0x00110044;

    /** (0011,xx46) VR=LO VM=1-n ? */
    public static final int _0011_xx46_ = 0x00110046;

    /** (0011,xx47) VR=DS VM=1 ? */
    public static final int _0011_xx47_ = 0x00110047;

    /** (0011,xx59) VR=CS VM=1 ? */
    public static final int _0011_xx59_ = 0x00110059;

    /** (0011,xx60) VR=CS VM=1 ? */
    public static final int _0011_xx60_ = 0x00110060;

    /** (0011,xx6D) VR=DS VM=1 ? */
    public static final int _0011_xx6D_ = 0x0011006D;

}
