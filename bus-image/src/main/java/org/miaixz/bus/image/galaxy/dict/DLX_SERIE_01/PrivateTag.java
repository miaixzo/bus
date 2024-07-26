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
package org.miaixz.bus.image.galaxy.dict.DLX_SERIE_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "DLX_SERIE_01";

    /** (0019,xx01) VR=DS VM=1 Angle Value L Arm */
    public static final int AngleValueLArm = 0x00190001;

    /** (0019,xx02) VR=DS VM=1 Angle Value P Arm */
    public static final int AngleValuePArm = 0x00190002;

    /** (0019,xx03) VR=DS VM=1 Angle Value C Arm */
    public static final int AngleValueCArm = 0x00190003;

    /** (0019,xx04) VR=CS VM=1 Angle Label L Arm */
    public static final int AngleLabelLArm = 0x00190004;

    /** (0019,xx05) VR=CS VM=1 Angle Label P Arm */
    public static final int AngleLabelPArm = 0x00190005;

    /** (0019,xx06) VR=CS VM=1 Angle Label C Arm */
    public static final int AngleLabelCArm = 0x00190006;

    /** (0019,xx07) VR=ST VM=1 Procedure Name */
    public static final int ProcedureName = 0x00190007;

    /** (0019,xx08) VR=ST VM=1 Exam Name */
    public static final int ExamName = 0x00190008;

    /** (0019,xx09) VR=SH VM=1 Patient Size */
    public static final int PatientSize = 0x00190009;

    /** (0019,xx0A) VR=IS VM=1 Record View */
    public static final int RecordView = 0x0019000A;

    /** (0019,xx10) VR=DS VM=1 Injector Delay */
    public static final int InjectorDelay = 0x00190010;

    /** (0019,xx11) VR=CS VM=1 Auto Inject */
    public static final int AutoInject = 0x00190011;

    /** (0019,xx14) VR=IS VM=1 Acquisition Mode */
    public static final int AcquisitionMode = 0x00190014;

    /** (0019,xx15) VR=CS VM=1 Camera Rotation Enabled */
    public static final int CameraRotationEnabled = 0x00190015;

    /** (0019,xx16) VR=CS VM=1 Reverse Sweep */
    public static final int ReverseSweep = 0x00190016;

    /** (0019,xx17) VR=IS VM=1 User Spatial Filter Strength */
    public static final int UserSpatialFilterStrength = 0x00190017;

    /** (0019,xx18) VR=IS VM=1 User Zoom Factor */
    public static final int UserZoomFactor = 0x00190018;

    /** (0019,xx19) VR=IS VM=1 X Zoom Center */
    public static final int XZoomCenter = 0x00190019;

    /** (0019,xx1A) VR=IS VM=1 Y Zoom Center */
    public static final int YZoomCenter = 0x0019001A;

    /** (0019,xx1B) VR=DS VM=1 Focus */
    public static final int Focus = 0x0019001B;

    /** (0019,xx1C) VR=CS VM=1 Dose */
    public static final int Dose = 0x0019001C;

    /** (0019,xx1D) VR=IS VM=1 Side Mark */
    public static final int SideMark = 0x0019001D;

    /** (0019,xx1E) VR=IS VM=1 Percentage Landscape */
    public static final int PercentageLandscape = 0x0019001E;

    /** (0019,xx1F) VR=DS VM=1 Exposure Duration */
    public static final int ExposureDuration = 0x0019001F;

    /** (0019,xx20) VR=LO VM=1 Ip Address */
    public static final int IpAddress = 0x00190020;

    /** (0019,xx21) VR=DS VM=1 Table position Z (vertica */
    public static final int TablePositionZ = 0x00190021;

    /** (0019,xx22) VR=DS VM=1 Table position X (longitudina */
    public static final int TablePositionX = 0x00190022;

    /** (0019,xx23) VR=DS VM=1 Table position Y (latera */
    public static final int TablePositionY = 0x00190023;

    /** (0019,xx24) VR=DS VM=1 Lambda cm Pincushion Distortion */
    public static final int Lambda = 0x00190024;

    /** (0019,xx25) VR=DS VM=1 LV Regression Slope Coefficient */
    public static final int RegressionSlope = 0x00190025;

    /** (0019,xx26) VR=DS VM=1 LV Regression Intercept Coefficient */
    public static final int RegressionIntercept = 0x00190026;

    /** (0019,xx27) VR=DS VM=1 Image chain FWHM psf mm min */
    public static final int ImageChainFWHMPsfMmMin = 0x00190027;

    /** (0019,xx28) VR=DS VM=1 Image chain FWHM psf mm max */
    public static final int ImageChainFWHMPsfMmMax = 0x00190028;

}
