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
package org.miaixz.bus.image.galaxy.dict.GE_GENESIS_REV3_0;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.AxialType:
                return "AxialType";

            case PrivateTag.SwapPhaseFrequency:
                return "SwapPhaseFrequency";

            case PrivateTag.PulseSequenceName:
                return "PulseSequenceName";

            case PrivateTag.CoilType:
                return "CoilType";

            case PrivateTag.SATFatWaterBone:
                return "SATFatWaterBone";

            case PrivateTag.BitmapOfSATSelections:
                return "BitmapOfSATSelections";

            case PrivateTag.SurfaceCoilIntensityCorrectionFlag:
                return "SurfaceCoilIntensityCorrectionFlag";

            case PrivateTag.PhaseContrastFlowAxis:
                return "PhaseContrastFlowAxis";

            case PrivateTag.PhaseContrastVelocityEncoding:
                return "PhaseContrastVelocityEncoding";

            case PrivateTag.FractionalEcho:
                return "FractionalEcho";

            case PrivateTag.VariableEchoFlag:
                return "VariableEchoFlag";

            case PrivateTag.ConcatenatedSat:
                return "ConcatenatedSat";

            case PrivateTag.NumberOfPhases:
                return "NumberOfPhases";

            case PrivateTag.DeltaStartTime:
                return "DeltaStartTime";

            case PrivateTag.ScanPitchRatio:
                return "ScanPitchRatio";
        }
        return "";
    }

}
