/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__QUANT_1_0;

/**
 * @author Kimi Liu
 * @since Java 21+
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
