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
package org.miaixz.bus.image.galaxy.dict.CARDIO_D_R__1_0;

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

            case PrivateTag.ImageBlankingShape:
                return VR.CS;

            case PrivateTag.EdgeEnhancementGain:
                return VR.FL;

            case PrivateTag.ImageBlankingLeftVerticalEdge:
            case PrivateTag.ImageBlankingRightVerticalEdge:
            case PrivateTag.ImageBlankingUpperHorizontalEdge:
            case PrivateTag.ImageBlankingLowerHorizontalEdge:
            case PrivateTag.CenterOfCircularImageBlanking:
            case PrivateTag.RadiusOfCircularImageBlanking:
            case PrivateTag.ImageSequenceNumber:
                return VR.IS;

            case PrivateTag.AlternateImageSequence:
            case PrivateTag.EdgeEnhancementSequence:
                return VR.SQ;

            case PrivateTag.FileLocation:
            case PrivateTag.FileSize:
            case PrivateTag.MaximumImageFrameSize:
                return VR.UL;

            case PrivateTag.ConvolutionKernelSize:
            case PrivateTag.ConvolutionKernelCoefficients:
                return VR.US;
        }
        return VR.UN;
    }

}
