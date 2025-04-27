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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__QUANT_1_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SMS-AX QUANT 1.0";

    /** (0023,xx00) VR=DS VM=2 Horizontal Calibration Pixel Size */
    public static final int HorizontalCalibrationPixelSize = 0x00230000;

    /** (0023,xx01) VR=DS VM=2 Vertical Calibration Pixel Size */
    public static final int VerticalCalibrationPixelSize = 0x00230001;

    /** (0023,xx02) VR=LO VM=1 Calibration Object */
    public static final int CalibrationObject = 0x00230002;

    /** (0023,xx03) VR=DS VM=1 Calibration Object Size */
    public static final int CalibrationObjectSize = 0x00230003;

    /** (0023,xx04) VR=LO VM=1 Calibration Method */
    public static final int CalibrationMethod = 0x00230004;

    /** (0023,xx05) VR=ST VM=1 Filename */
    public static final int Filename = 0x00230005;

    /** (0023,xx06) VR=IS VM=1 Frame Number */
    public static final int FrameNumber = 0x00230006;

    /** (0023,xx07) VR=IS VM=2 Calibration Factor Multiplicity */
    public static final int CalibrationFactorMultiplicity = 0x00230007;

    /** (0023,xx08) VR=IS VM=1 Calibration Table Object Distance */
    public static final int CalibrationTableObjectDistance = 0x00230008;

}
