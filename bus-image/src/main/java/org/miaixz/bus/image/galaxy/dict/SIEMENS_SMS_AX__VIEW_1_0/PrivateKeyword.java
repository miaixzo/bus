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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__VIEW_1_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.ReviewMode:
                return "ReviewMode";

            case PrivateTag.AnatomicalBackgroundPercent:
                return "AnatomicalBackgroundPercent";

            case PrivateTag.NumberOfPhases:
                return "NumberOfPhases";

            case PrivateTag.ApplyAnatomicalBackground:
                return "ApplyAnatomicalBackground";

            case PrivateTag.PixelShiftArray:
                return "PixelShiftArray";

            case PrivateTag.Brightness:
                return "Brightness";

            case PrivateTag.Contrast:
                return "Contrast";

            case PrivateTag.EnabledShutters:
                return "EnabledShutters";

            case PrivateTag.NativeEdgeEnhancementPercentGain:
                return "NativeEdgeEnhancementPercentGain";

            case PrivateTag.NativeEdgeEnhancementLUTIndex:
                return "NativeEdgeEnhancementLUTIndex";

            case PrivateTag.NativeEdgeEnhancementKernelSize:
                return "NativeEdgeEnhancementKernelSize";

            case PrivateTag.SubtractedEdgeEnhancementPercentGain:
                return "SubtractedEdgeEnhancementPercentGain";

            case PrivateTag.SubtractedEdgeEnhancementLUTIndex:
                return "SubtractedEdgeEnhancementLUTIndex";

            case PrivateTag.SubtractedEdgeEnhancementKernelSize:
                return "SubtractedEdgeEnhancementKernelSize";

            case PrivateTag.FadePercent:
                return "FadePercent";

            case PrivateTag.FlippedBeforeLateralityApplied:
                return "FlippedBeforeLateralityApplied";

            case PrivateTag.ApplyFade:
                return "ApplyFade";

            case PrivateTag.ReferenceImagesTakenFlag:
                return "ReferenceImagesTakenFlag";

            case PrivateTag.Zoom:
                return "Zoom";

            case PrivateTag.PanX:
                return "PanX";

            case PrivateTag.PanY:
                return "PanY";

            case PrivateTag.NativeEdgeEnhancementAdvPercentGain:
                return "NativeEdgeEnhancementAdvPercentGain";

            case PrivateTag.SubtractedEdgeEnhancementAdvPercentGain:
                return "SubtractedEdgeEnhancementAdvPercentGain";

            case PrivateTag.InvertFlag:
                return "InvertFlag";

            case PrivateTag.Quant1KOverlay:
                return "Quant1KOverlay";

            case PrivateTag.OriginalResolution:
                return "OriginalResolution";

            case PrivateTag.AutoWindowCenter:
                return "AutoWindowCenter";

            case PrivateTag.AutoWindowWidth:
                return "AutoWindowWidth";

            case PrivateTag.AutoWindowCorrectValue:
                return "AutoWindowCorrectValue";

            case PrivateTag.SigmoidWindowParameter:
                return "SigmoidWindowParameter";

            case PrivateTag.DispayedAreaTopLeftHandCorner:
                return "DispayedAreaTopLeftHandCorner";

            case PrivateTag.DispayedAreaBottomRightHandCorner:
                return "DispayedAreaBottomRightHandCorner";
        }
        return "";
    }

}
