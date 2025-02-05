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
