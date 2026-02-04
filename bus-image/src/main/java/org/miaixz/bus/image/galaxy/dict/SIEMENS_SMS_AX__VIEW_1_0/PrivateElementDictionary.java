/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__VIEW_1_0;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag.AutoWindowCenter:
            case PrivateTag.AutoWindowWidth:
            case PrivateTag.SigmoidWindowParameter:
                return VR.DS;

            case PrivateTag.AutoWindowCorrectValue:
                return VR.IS;

            case PrivateTag.Quant1KOverlay:
                return VR.OB;

            case PrivateTag.DispayedAreaTopLeftHandCorner:
            case PrivateTag.DispayedAreaBottomRightHandCorner:
                return VR.SL;

            case PrivateTag.PixelShiftArray:
            case PrivateTag.NativeEdgeEnhancementLUTIndex:
            case PrivateTag.NativeEdgeEnhancementKernelSize:
            case PrivateTag.SubtractedEdgeEnhancementLUTIndex:
            case PrivateTag.SubtractedEdgeEnhancementKernelSize:
            case PrivateTag.PanX:
            case PrivateTag.PanY:
            case PrivateTag.NativeEdgeEnhancementAdvPercentGain:
            case PrivateTag.SubtractedEdgeEnhancementAdvPercentGain:
                return VR.SS;

            case PrivateTag.ReviewMode:
            case PrivateTag.AnatomicalBackgroundPercent:
            case PrivateTag.NumberOfPhases:
            case PrivateTag.ApplyAnatomicalBackground:
            case PrivateTag.Brightness:
            case PrivateTag.Contrast:
            case PrivateTag.EnabledShutters:
            case PrivateTag.NativeEdgeEnhancementPercentGain:
            case PrivateTag.SubtractedEdgeEnhancementPercentGain:
            case PrivateTag.FadePercent:
            case PrivateTag.FlippedBeforeLateralityApplied:
            case PrivateTag.ApplyFade:
            case PrivateTag.ReferenceImagesTakenFlag:
            case PrivateTag.Zoom:
            case PrivateTag.InvertFlag:
            case PrivateTag.OriginalResolution:
                return VR.US;
        }
        return VR.UN;
    }

}
