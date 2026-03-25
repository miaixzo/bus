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
package org.miaixz.bus.image.galaxy.dict.PMI_Private_Calibration_Module_Version_2_0;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.CalibrationMethod:
                return "CalibrationMethod";

            case PrivateTag.CalibrationMethodInfo:
                return "CalibrationMethodInfo";

            case PrivateTag.CalibrationObjectSize:
                return "CalibrationObjectSize";

            case PrivateTag.CalibrationObjectSDev:
                return "CalibrationObjectSDev";

            case PrivateTag.CalibrationHorizontalPixelSpacing:
                return "CalibrationHorizontalPixelSpacing";

            case PrivateTag.CalibrationVerticalPixelSpacing:
                return "CalibrationVerticalPixelSpacing";

            case PrivateTag.CalibrationFileName:
                return "CalibrationFileName";

            case PrivateTag.CalibrationFrameNumber:
                return "CalibrationFrameNumber";

            case PrivateTag.CalibrationObjectUnit:
                return "CalibrationObjectUnit";

            case PrivateTag.AveragedCalibrationsPerformed:
                return "AveragedCalibrationsPerformed";

            case PrivateTag.AutoMagnifyFactor:
                return "AutoMagnifyFactor";

            case PrivateTag.HorizontalPixelSDev:
                return "HorizontalPixelSDev";

            case PrivateTag.VerticalPixelSDev:
                return "VerticalPixelSDev";
        }
        return "";
    }

}
