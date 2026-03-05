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
package org.miaixz.bus.image.galaxy.dict.DLX_SERIE_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.AngleValueLArm:
                return "AngleValueLArm";

            case PrivateTag.AngleValuePArm:
                return "AngleValuePArm";

            case PrivateTag.AngleValueCArm:
                return "AngleValueCArm";

            case PrivateTag.AngleLabelLArm:
                return "AngleLabelLArm";

            case PrivateTag.AngleLabelPArm:
                return "AngleLabelPArm";

            case PrivateTag.AngleLabelCArm:
                return "AngleLabelCArm";

            case PrivateTag.ProcedureName:
                return "ProcedureName";

            case PrivateTag.ExamName:
                return "ExamName";

            case PrivateTag.PatientSize:
                return "PatientSize";

            case PrivateTag.RecordView:
                return "RecordView";

            case PrivateTag.InjectorDelay:
                return "InjectorDelay";

            case PrivateTag.AutoInject:
                return "AutoInject";

            case PrivateTag.AcquisitionMode:
                return "AcquisitionMode";

            case PrivateTag.CameraRotationEnabled:
                return "CameraRotationEnabled";

            case PrivateTag.ReverseSweep:
                return "ReverseSweep";

            case PrivateTag.UserSpatialFilterStrength:
                return "UserSpatialFilterStrength";

            case PrivateTag.UserZoomFactor:
                return "UserZoomFactor";

            case PrivateTag.XZoomCenter:
                return "XZoomCenter";

            case PrivateTag.YZoomCenter:
                return "YZoomCenter";

            case PrivateTag.Focus:
                return "Focus";

            case PrivateTag.Dose:
                return "Dose";

            case PrivateTag.SideMark:
                return "SideMark";

            case PrivateTag.PercentageLandscape:
                return "PercentageLandscape";

            case PrivateTag.ExposureDuration:
                return "ExposureDuration";

            case PrivateTag.IpAddress:
                return "IpAddress";

            case PrivateTag.TablePositionZ:
                return "TablePositionZ";

            case PrivateTag.TablePositionX:
                return "TablePositionX";

            case PrivateTag.TablePositionY:
                return "TablePositionY";

            case PrivateTag.Lambda:
                return "Lambda";

            case PrivateTag.RegressionSlope:
                return "RegressionSlope";

            case PrivateTag.RegressionIntercept:
                return "RegressionIntercept";

            case PrivateTag.ImageChainFWHMPsfMmMin:
                return "ImageChainFWHMPsfMmMin";

            case PrivateTag.ImageChainFWHMPsfMmMax:
                return "ImageChainFWHMPsfMmMax";
        }
        return "";
    }

}
