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
package org.miaixz.bus.image.galaxy.dict.GEMS_HELIOS_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_HELIOS_01";

    /** (0045,xx01) VR=SS VM=1 Number of Macro Rows in Detector */
    public static final int NumberOfMacroRowsInDetector = 0x00450001;

    /** (0045,xx02) VR=FL VM=1 Macro width at ISO Center */
    public static final int MacroWidthAtISOCenter = 0x00450002;

    /** (0045,xx03) VR=SS VM=1 DAS type */
    public static final int DASType = 0x00450003;

    /** (0045,xx04) VR=SS VM=1 DAS gain */
    public static final int DASGain = 0x00450004;

    /** (0045,xx05) VR=SS VM=1 DAS Temprature */
    public static final int DASTemprature = 0x00450005;

    /** (0045,xx06) VR=CS VM=1 Table Direction */
    public static final int TableDirection = 0x00450006;

    /** (0045,xx07) VR=FL VM=1 Z smoothing Factor */
    public static final int ZSmoothingFactor = 0x00450007;

    /** (0045,xx08) VR=SS VM=1 View Weighting Mode */
    public static final int ViewWeightingMode = 0x00450008;

    /** (0045,xx09) VR=SS VM=1 Sigma Row number */
    public static final int SigmaRowNumber = 0x00450009;

    /** (0045,xx0A) VR=FL VM=1 Minimum DAS value */
    public static final int MinimumDASValue = 0x0045000A;

    /** (0045,xx0B) VR=FL VM=1 Maximum Offset Value */
    public static final int MaximumOffsetValue = 0x0045000B;

    /** (0045,xx0C) VR=SS VM=1 Number of Views shifted */
    public static final int NumberOfViewsShifted = 0x0045000C;

    /** (0045,xx0D) VR=SS VM=1 Z tracking Flag */
    public static final int ZTrackingFlag = 0x0045000D;

    /** (0045,xx0E) VR=FL VM=1 Mean Z error */
    public static final int MeanZError = 0x0045000E;

    /** (0045,xx0F) VR=FL VM=1 Z tracking Error */
    public static final int ZTrackingError = 0x0045000F;

    /** (0045,xx10) VR=SS VM=1 Start View 2A */
    public static final int StartView2A = 0x00450010;

    /** (0045,xx11) VR=SS VM=1 Number of Views 2A */
    public static final int NumberOfViews2A = 0x00450011;

    /** (0045,xx12) VR=SS VM=1 Start View 1A */
    public static final int StartView1A = 0x00450012;

    /** (0045,xx13) VR=SS VM=1 Sigma Mode */
    public static final int SigmaMode = 0x00450013;

    /** (0045,xx14) VR=SS VM=1 Number of Views 1A */
    public static final int NumberOfViews1A = 0x00450014;

    /** (0045,xx15) VR=SS VM=1 Start View 2B */
    public static final int StartView2B = 0x00450015;

    /** (0045,xx16) VR=SS VM=1 Number Views 2B */
    public static final int NumberViews2B = 0x00450016;

    /** (0045,xx17) VR=SS VM=1 Start View 1B */
    public static final int StartView1B = 0x00450017;

    /** (0045,xx18) VR=SS VM=1 Number of Views 1B */
    public static final int NumberOfViews1B = 0x00450018;

    /** (0045,xx21) VR=SS VM=1 Iterbone Flag */
    public static final int IterboneFlag = 0x00450021;

    /** (0045,xx22) VR=SS VM=1 Peristaltic Flag */
    public static final int PeristalticFlag = 0x00450022;

    /** (0045,xx30) VR=CS VM=1 Cardiac Recon Algorithm */
    public static final int CardiacReconAlgorithm = 0x00450030;

    /** (0045,xx31) VR=CS VM=1 Avg Heart Rate For Image */
    public static final int AvgHeartRateForImage = 0x00450031;

    /** (0045,xx32) VR=FL VM=1 Temporal Resolution */
    public static final int TemporalResolution = 0x00450032;

    /** (0045,xx33) VR=CS VM=1 Pct Rpeak Delay */
    public static final int PctRpeakDelay = 0x00450033;

    /** (0045,xx34) VR=CS VM=1 ? */
    public static final int _0045_xx34_ = 0x00450034;

    /** (0045,xx36) VR=CS VM=1 Ekg Full Ma Start Phase */
    public static final int EkgFullMaStartPhase = 0x00450036;

    /** (0045,xx37) VR=CS VM=1 Ekg Full Ma End Phase */
    public static final int EkgFullMaEndPhase = 0x00450037;

    /** (0045,xx38) VR=CS VM=1 Ekg Modulation Max Ma */
    public static final int EkgModulationMaxMa = 0x00450038;

    /** (0045,xx39) VR=CS VM=1 Ekg Modulation Min Ma */
    public static final int EkgModulationMinMa = 0x00450039;

    /** (0045,xx3B) VR=LO VM=1 Noise Reduction Image Filter Desc */
    public static final int NoiseReductionImageFilterDesc = 0x0045003B;

    /** (0045,xx50) VR=FD VM=1 Temporal Center View Angle */
    public static final int TemporalCenterViewAngle = 0x00450050;

    /** (0045,xx51) VR=FD VM=1 Recon Center View Angle */
    public static final int ReconCenterViewAngle = 0x00450051;

    /** (0045,xx52) VR=CS VM=1 WideCone Masking */
    public static final int WideConeMasking = 0x00450052;

    /** (0045,xx53) VR=FD VM=1 WideCone Corner Blending Radius */
    public static final int WideConeCornerBlendingRadius = 0x00450053;

    /**
     * (0045,xx54) VR=FD VM=1 WideCone Corner Blending Radius Offset
     */
    public static final int WideConeCornerBlendingRadiusOffset = 0x00450054;

    /** (0045,xx55) VR=CS VM=1 Internal Recon Algorithm */
    public static final int InternalReconAlgorithm = 0x00450055;

    /** (0045,xx60) VR=FL VM=1-n Patient Centering */
    public static final int PatientCentering = 0x00450060;

    /** (0045,xx61) VR=FL VM=1-n Patient Attenuation */
    public static final int PatientAttenuation = 0x00450061;

    /** (0045,xx62) VR=FL VM=1-n Water Equivalent Diameter */
    public static final int WaterEquivalentDiameter = 0x00450062;

    /** (0045,xx63) VR=FL VM=1-n Projection Measure */
    public static final int ProjectionMeasure = 0x00450063;

    /** (0045,xx64) VR=FL VM=1-n Oval Ratio */
    public static final int OvalRatio = 0x00450064;

    /** (0045,xx65) VR=FL VM=1-n Ellipse Orientation */
    public static final int EllipseOrientation = 0x00450065;

}
