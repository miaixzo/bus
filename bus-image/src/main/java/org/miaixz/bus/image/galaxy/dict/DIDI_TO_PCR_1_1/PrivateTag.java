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
package org.miaixz.bus.image.galaxy.dict.DIDI_TO_PCR_1_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "DIDI TO PCR 1.1";

    /** (0019,xx22) VR=LO VM=1 Route AET */
    public static final int RouteAET = 0x00190022;

    /** (0019,xx23) VR=DS VM=1 PCR Print Scale */
    public static final int PCRPrintScale = 0x00190023;

    /** (0019,xx24) VR=ST VM=1 PCR Print Job End */
    public static final int PCRPrintJobEnd = 0x00190024;

    /** (0019,xx25) VR=IS VM=1 PCR No Film Copies */
    public static final int PCRNoFilmCopies = 0x00190025;

    /** (0019,xx26) VR=IS VM=1 PCR Film Layout Position */
    public static final int PCRFilmLayoutPosition = 0x00190026;

    /** (0019,xx27) VR=ST VM=1 PCR Print Report Name */
    public static final int PCRPrintReportName = 0x00190027;

    /** (0019,xx70) VR=ST VM=1 RAD Protocol Printer */
    public static final int RADProtocolPrinter = 0x00190070;

    /** (0019,xx71) VR=ST VM=1 RAD Protocol Medium */
    public static final int RADProtocolMedium = 0x00190071;

    /** (0019,xx89) VR=IS VM=1 Exposure Index */
    public static final int ExposureIndex = 0x00190089;

    /** (0019,xx8A) VR=IS VM=1 Collimator X */
    public static final int CollimatorX = 0x0019008A;

    /** (0019,xx8B) VR=IS VM=1 Collimator Y */
    public static final int CollimatorY = 0x0019008B;

    /** (0019,xx8C) VR=LO VM=1 Print Marker */
    public static final int PrintMarker = 0x0019008C;

    /** (0019,xx8D) VR=LO VM=1 RGDV Name */
    public static final int RGDVName = 0x0019008D;

    /** (0019,xx8E) VR=LO VM=1 Acqd Sensitivity */
    public static final int AcqdSensitivity = 0x0019008E;

    /** (0019,xx8F) VR=LO VM=1 Processing Category */
    public static final int ProcessingCategory = 0x0019008F;

    /** (0019,xx90) VR=LO VM=1 Unprocessed Flag */
    public static final int UnprocessedFlag = 0x00190090;

    /** (0019,xx91) VR=DS VM=1-n Key Values */
    public static final int KeyValues = 0x00190091;

    /**
     * (0019,xx92) VR=LO VM=1 Destination Postprocessing Function
     */
    public static final int DestinationPostprocessingFunction = 0x00190092;

    /** (0019,xxA0) VR=LO VM=1 Version */
    public static final int Version = 0x001900A0;

    /** (0019,xxA1) VR=LO VM=1 Ranging Mode */
    public static final int RangingMode = 0x001900A1;

    /** (0019,xxA2) VR=DS VM=1 Abdomen Brightness */
    public static final int AbdomenBrightness = 0x001900A2;

    /** (0019,xxA3) VR=DS VM=1 Fixed Brightness */
    public static final int FixedBrightness = 0x001900A3;

    /** (0019,xxA4) VR=DS VM=1 Detail Contrast */
    public static final int DetailContrast = 0x001900A4;

    /** (0019,xxA5) VR=DS VM=1 Contrast Balance */
    public static final int ContrastBalance = 0x001900A5;

    /** (0019,xxA6) VR=DS VM=1 Structure Boost */
    public static final int StructureBoost = 0x001900A6;

    /** (0019,xxA7) VR=DS VM=1 Structure Preference */
    public static final int StructurePreference = 0x001900A7;

    /** (0019,xxA8) VR=DS VM=1 Noise Robustness */
    public static final int NoiseRobustness = 0x001900A8;

    /** (0019,xxA9) VR=DS VM=1 Noise Dose Limit */
    public static final int NoiseDoseLimit = 0x001900A9;

    /** (0019,xxAA) VR=DS VM=1 Noise Dose Step */
    public static final int NoiseDoseStep = 0x001900AA;

    /** (0019,xxAB) VR=DS VM=1 Noise Frequency Limit */
    public static final int NoiseFrequencyLimit = 0x001900AB;

    /** (0019,xxAC) VR=DS VM=1 Weak Contrast Limit */
    public static final int WeakContrastLimit = 0x001900AC;

    /** (0019,xxAD) VR=DS VM=1 Strong Contrast Limit */
    public static final int StrongContrastLimit = 0x001900AD;

    /** (0019,xxAE) VR=DS VM=1 Structure Boost Offset */
    public static final int StructureBoostOffset = 0x001900AE;

    /** (0019,xxAF) VR=LO VM=1 Smooth Gain */
    public static final int SmoothGain = 0x001900AF;

    /** (0019,xxB0) VR=LO VM=1 Measure Field 1 */
    public static final int MeasureField1 = 0x001900B0;

    /** (0019,xxB1) VR=LO VM=1 Measure Field 2 */
    public static final int MeasureField2 = 0x001900B1;

    /** (0019,xxB2) VR=IS VM=1 Key Percentile 1 */
    public static final int KeyPercentile1 = 0x001900B2;

    /** (0019,xxB3) VR=IS VM=1 Key Percentile 2 */
    public static final int KeyPercentile2 = 0x001900B3;

    /** (0019,xxB4) VR=IS VM=1 Density LUT */
    public static final int DensityLUT = 0x001900B4;

    /** (0019,xxB5) VR=DS VM=1 Brightness */
    public static final int Brightness = 0x001900B5;

    /** (0019,xxB6) VR=DS VM=1 Gamma */
    public static final int Gamma = 0x001900B6;

    /** (0089,xx10) VR=SQ VM=1 Stamp Image Sequence */
    public static final int StampImageSequence = 0x00890010;

}
