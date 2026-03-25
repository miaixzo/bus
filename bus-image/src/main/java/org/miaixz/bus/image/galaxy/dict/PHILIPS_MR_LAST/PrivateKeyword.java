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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_MR_LAST;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.MainMagneticField:
                return "MainMagneticField";

            case PrivateTag.FlowCompensation:
                return "FlowCompensation";

            case PrivateTag._0019_xxB7_:
                return "_0019_xxB7_";

            case PrivateTag._0019_xxE4_:
                return "_0019_xxE4_";

            case PrivateTag._0019_xxE5_:
                return "_0019_xxE5_";

            case PrivateTag.MinimumRRInterval:
                return "MinimumRRInterval";

            case PrivateTag.MaximumRRInterval:
                return "MaximumRRInterval";

            case PrivateTag.NumberOfRejections:
                return "NumberOfRejections";

            case PrivateTag.NumberOfRRIntervals:
                return "NumberOfRRIntervals";

            case PrivateTag.ArrhythmiaRejection:
                return "ArrhythmiaRejection";

            case PrivateTag._0019_xxc0_:
                return "_0019_xxc0_";

            case PrivateTag.CycledMultipleSlice:
                return "CycledMultipleSlice";

            case PrivateTag.REST:
                return "REST";

            case PrivateTag._0019_xxd5_:
                return "_0019_xxd5_";

            case PrivateTag.FourierInterpolation:
                return "FourierInterpolation";

            case PrivateTag._0019_xxd9_:
                return "_0019_xxd9_";

            case PrivateTag.Prepulse:
                return "Prepulse";

            case PrivateTag.PrepulseDelay:
                return "PrepulseDelay";

            case PrivateTag._0019_xxe2_:
                return "_0019_xxe2_";

            case PrivateTag._0019_xxe3_:
                return "_0019_xxe3_";

            case PrivateTag.WSProtocolString1:
                return "WSProtocolString1";

            case PrivateTag.WSProtocolString2:
                return "WSProtocolString2";

            case PrivateTag.WSProtocolString3:
                return "WSProtocolString3";

            case PrivateTag.WSProtocolString4:
                return "WSProtocolString4";

            case PrivateTag._0021_xx00_:
                return "_0021_xx00_";

            case PrivateTag._0021_xx10_:
                return "_0021_xx10_";

            case PrivateTag._0021_xx20_:
                return "_0021_xx20_";

            case PrivateTag.SliceGap:
                return "SliceGap";

            case PrivateTag.StackRadialAngle:
                return "StackRadialAngle";

            case PrivateTag._0027_xx00_:
                return "_0027_xx00_";

            case PrivateTag._0027_xx11_:
                return "_0027_xx11_";

            case PrivateTag._0027_xx12_:
                return "_0027_xx12_";

            case PrivateTag._0027_xx13_:
                return "_0027_xx13_";

            case PrivateTag._0027_xx14_:
                return "_0027_xx14_";

            case PrivateTag._0027_xx15_:
                return "_0027_xx15_";

            case PrivateTag._0027_xx16_:
                return "_0027_xx16_";

            case PrivateTag.FPMin:
                return "FPMin";

            case PrivateTag.FPMax:
                return "FPMax";

            case PrivateTag.ScaledMinimum:
                return "ScaledMinimum";

            case PrivateTag.ScaledMaximum:
                return "ScaledMaximum";

            case PrivateTag.WindowMinimum:
                return "WindowMinimum";

            case PrivateTag.WindowMaximum:
                return "WindowMaximum";

            case PrivateTag._0029_xx61_:
                return "_0029_xx61_";

            case PrivateTag._0029_xx62_:
                return "_0029_xx62_";

            case PrivateTag._0029_xx70_:
                return "_0029_xx70_";

            case PrivateTag._0029_xx71_:
                return "_0029_xx71_";

            case PrivateTag._0029_xx72_:
                return "_0029_xx72_";

            case PrivateTag.ViewCenter:
                return "ViewCenter";

            case PrivateTag.ViewSize:
                return "ViewSize";

            case PrivateTag.ViewZoom:
                return "ViewZoom";

            case PrivateTag.ViewTransform:
                return "ViewTransform";

            case PrivateTag._0041_xx07_:
                return "_0041_xx07_";

            case PrivateTag._0041_xx09_:
                return "_0041_xx09_";

            case PrivateTag._6001_xx00_:
                return "_6001_xx00_";
        }
        return "";
    }

}
