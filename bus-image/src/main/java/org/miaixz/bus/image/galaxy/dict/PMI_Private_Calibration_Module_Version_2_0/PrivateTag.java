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
package org.miaixz.bus.image.galaxy.dict.PMI_Private_Calibration_Module_Version_2_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "PMI Private Calibration Module Version 2.0";

    /** (2121,xx01) VR=ST VM=1 Calibration Method */
    public static final int CalibrationMethod = 0x21210001;

    /** (2121,xx02) VR=ST VM=1 Calibration Method Info */
    public static final int CalibrationMethodInfo = 0x21210002;

    /** (2121,xx03) VR=FL VM=1 Calibration Object Size */
    public static final int CalibrationObjectSize = 0x21210003;

    /** (2121,xx04) VR=FL VM=1 Calibration Object S Dev */
    public static final int CalibrationObjectSDev = 0x21210004;

    /** (2121,xx05) VR=FL VM=1 Calibration Horizontal Pixel Spacing */
    public static final int CalibrationHorizontalPixelSpacing = 0x21210005;

    /** (2121,xx06) VR=FL VM=1 Calibration Vertical Pixel Spacing */
    public static final int CalibrationVerticalPixelSpacing = 0x21210006;

    /** (2121,xx08) VR=ST VM=1 Calibration File Name */
    public static final int CalibrationFileName = 0x21210008;

    /** (2121,xx09) VR=IS VM=1 Calibration Frame Number */
    public static final int CalibrationFrameNumber = 0x21210009;

    /** (2121,xx0A) VR=SH VM=1 Calibration Object Unit */
    public static final int CalibrationObjectUnit = 0x2121000A;

    /** (2121,xx0B) VR=SS VM=1 Averaged Calibrations Performed */
    public static final int AveragedCalibrationsPerformed = 0x2121000B;

    /** (2121,xx0C) VR=FL VM=1 Auto Magnify Factor */
    public static final int AutoMagnifyFactor = 0x2121000C;

    /** (2121,xx0D) VR=FL VM=1 Horizontal Pixel S Dev */
    public static final int HorizontalPixelSDev = 0x2121000D;

    /** (2121,xx0E) VR=FL VM=1 Vertical Pixel S Dev */
    public static final int VerticalPixelSDev = 0x2121000E;

}
