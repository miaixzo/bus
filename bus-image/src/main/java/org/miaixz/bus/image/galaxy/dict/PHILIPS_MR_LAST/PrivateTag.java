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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_MR_LAST;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "PHILIPS MR/LAST";

    /** (0019,xx09) VR=DS VM=1 Main Magnetic Field */
    public static final int MainMagneticField = 0x00190009;

    /** (0019,xx0e) VR=IS VM=1 Flow Compensation */
    public static final int FlowCompensation = 0x0019000e;

    /** (0019,xxB7) VR=IS VM=1 ? */
    public static final int _0019_xxB7_ = 0x001900B7;

    /** (0019,xxE4) VR=IS VM=1 ? */
    public static final int _0019_xxE4_ = 0x001900E4;

    /** (0019,xxE5) VR=DS VM=1 ? */
    public static final int _0019_xxE5_ = 0x001900E5;

    /** (0019,xxb1) VR=IS VM=1 Minimum RR Interval */
    public static final int MinimumRRInterval = 0x001900b1;

    /** (0019,xxb2) VR=IS VM=1 Maximum RR Interval */
    public static final int MaximumRRInterval = 0x001900b2;

    /** (0019,xxb3) VR=IS VM=1 Number of Rejections */
    public static final int NumberOfRejections = 0x001900b3;

    /** (0019,xxb4) VR=IS VM=1-n Number of RR Intervals */
    public static final int NumberOfRRIntervals = 0x001900b4;

    /** (0019,xxb5) VR=IS VM=1 Arrhythmia Rejection */
    public static final int ArrhythmiaRejection = 0x001900b5;

    /** (0019,xxc0) VR=DS VM=1-n ? */
    public static final int _0019_xxc0_ = 0x001900c0;

    /** (0019,xxc6) VR=IS VM=1 Cycled Multiple Slice */
    public static final int CycledMultipleSlice = 0x001900c6;

    /** (0019,xxce) VR=IS VM=1 REST */
    public static final int REST = 0x001900ce;

    /** (0019,xxd5) VR=DS VM=1 ? */
    public static final int _0019_xxd5_ = 0x001900d5;

    /** (0019,xxd6) VR=IS VM=1 Fourier Interpolation */
    public static final int FourierInterpolation = 0x001900d6;

    /** (0019,xxd9) VR=IS VM=1-n ? */
    public static final int _0019_xxd9_ = 0x001900d9;

    /** (0019,xxe0) VR=IS VM=1 Prepulse */
    public static final int Prepulse = 0x001900e0;

    /** (0019,xxe1) VR=DS VM=1 Prepulse Delay */
    public static final int PrepulseDelay = 0x001900e1;

    /** (0019,xxe2) VR=IS VM=1 ? */
    public static final int _0019_xxe2_ = 0x001900e2;

    /** (0019,xxe3) VR=DS VM=1 ? */
    public static final int _0019_xxe3_ = 0x001900e3;

    /** (0019,xxf0) VR=LT VM=1 WS Protocol String 1 */
    public static final int WSProtocolString1 = 0x001900f0;

    /** (0019,xxf1) VR=LT VM=1 WS Protocol String 2 */
    public static final int WSProtocolString2 = 0x001900f1;

    /** (0019,xxf2) VR=LT VM=1 WS Protocol String 3 */
    public static final int WSProtocolString3 = 0x001900f2;

    /** (0019,xxf3) VR=LT VM=1 WS Protocol String 4 */
    public static final int WSProtocolString4 = 0x001900f3;

    /** (0021,xx00) VR=IS VM=1 ? */
    public static final int _0021_xx00_ = 0x00210000;

    /** (0021,xx10) VR=IS VM=1 ? */
    public static final int _0021_xx10_ = 0x00210010;

    /** (0021,xx20) VR=IS VM=1 ? */
    public static final int _0021_xx20_ = 0x00210020;

    /** (0021,xx21) VR=DS VM=1 Slice Gap */
    public static final int SliceGap = 0x00210021;

    /** (0021,xx22) VR=DS VM=1 Stack Radial Angle */
    public static final int StackRadialAngle = 0x00210022;

    /** (0027,xx00) VR=US VM=1 ? */
    public static final int _0027_xx00_ = 0x00270000;

    /** (0027,xx11) VR=US VM=1-n ? */
    public static final int _0027_xx11_ = 0x00270011;

    /** (0027,xx12) VR=DS VM=1-n ? */
    public static final int _0027_xx12_ = 0x00270012;

    /** (0027,xx13) VR=DS VM=1-n ? */
    public static final int _0027_xx13_ = 0x00270013;

    /** (0027,xx14) VR=DS VM=1-n ? */
    public static final int _0027_xx14_ = 0x00270014;

    /** (0027,xx15) VR=DS VM=1-n ? */
    public static final int _0027_xx15_ = 0x00270015;

    /** (0027,xx16) VR=LO VM=1 ? */
    public static final int _0027_xx16_ = 0x00270016;

    /** (0029,xx10) VR=DS VM=1 FP Min */
    public static final int FPMin = 0x00290010;

    /** (0029,xx20) VR=DS VM=1 FP Max */
    public static final int FPMax = 0x00290020;

    /** (0029,xx30) VR=DS VM=1 Scaled Minimum */
    public static final int ScaledMinimum = 0x00290030;

    /** (0029,xx40) VR=DS VM=1 Scaled Maximum */
    public static final int ScaledMaximum = 0x00290040;

    /** (0029,xx50) VR=DS VM=1 Window Minimum */
    public static final int WindowMinimum = 0x00290050;

    /** (0029,xx60) VR=DS VM=1 Window Maximum */
    public static final int WindowMaximum = 0x00290060;

    /** (0029,xx61) VR=IS VM=1 ? */
    public static final int _0029_xx61_ = 0x00290061;

    /** (0029,xx62) VR=IS VM=1 ? */
    public static final int _0029_xx62_ = 0x00290062;

    /** (0029,xx70) VR=DS VM=1 ? */
    public static final int _0029_xx70_ = 0x00290070;

    /** (0029,xx71) VR=DS VM=1 ? */
    public static final int _0029_xx71_ = 0x00290071;

    /** (0029,xx72) VR=IS VM=1 ? */
    public static final int _0029_xx72_ = 0x00290072;

    /** (0029,xx80) VR=IS VM=1 View Center */
    public static final int ViewCenter = 0x00290080;

    /** (0029,xx81) VR=IS VM=1 View Size */
    public static final int ViewSize = 0x00290081;

    /** (0029,xx82) VR=IS VM=1 View Zoom */
    public static final int ViewZoom = 0x00290082;

    /** (0029,xx83) VR=IS VM=1 View Transform */
    public static final int ViewTransform = 0x00290083;

    /** (0041,xx07) VR=LO VM=1 ? */
    public static final int _0041_xx07_ = 0x00410007;

    /** (0041,xx09) VR=DS VM=1 ? */
    public static final int _0041_xx09_ = 0x00410009;

    /** (6001,xx00) VR=LT VM=1 ? */
    public static final int _6001_xx00_ = 0x60010000;

}
