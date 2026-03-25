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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SMS_USG_S2000;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
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

            case PrivateTag._0019_xx30_:
                return VR.DS;

            case PrivateTag.FrameRate:
            case PrivateTag.BModeDynamicRange:
            case PrivateTag.BModeOverallGain:
            case PrivateTag.ColorFlowOverallGain:
            case PrivateTag.ColorFlowMaximumVelocity:
            case PrivateTag.DopplerDynamicRange:
            case PrivateTag.DopplerOverallGain:
            case PrivateTag.DopplerWallFilter:
            case PrivateTag.DopplerGateSize:
                return VR.FD;

            case PrivateTag._0019_xx3B_:
                return VR.LT;

            case PrivateTag.PrivateCreatorVersion:
            case PrivateTag.SieClearIndex:
            case PrivateTag.BModeSubmode:
            case PrivateTag.ClarifyVEIndex:
            case PrivateTag.ColorFlowState:
            case PrivateTag.ColorFlowSubmode:
            case PrivateTag.DopplerSubmode:
            case PrivateTag.MModeSubmode:
                return VR.SH;

            case PrivateTag.BurnedInGraphics:
            case PrivateTag._0019_xx0E_:
            case PrivateTag.BModeResolutionSpeedIndex:
            case PrivateTag.BModeEdgeEnhanceIndex:
            case PrivateTag.BModePersistenceIndex:
            case PrivateTag.BModeMapIndex:
            case PrivateTag._0019_xx27_:
            case PrivateTag._0019_xx28_:
            case PrivateTag._0019_xx29_:
            case PrivateTag.BModeTintType:
            case PrivateTag.BModeTintIndex:
            case PrivateTag._0019_xx31_:
            case PrivateTag.ImageFlag:
            case PrivateTag.ColorFlowWallFilterIndex:
            case PrivateTag.ColorFlowResolutionSpeedIndex:
            case PrivateTag.ColorFlowSmoothIndex:
            case PrivateTag.ColorFlowPersistenceIndex:
            case PrivateTag.ColorFlowMapIndex:
            case PrivateTag.ColorFlowPriorityIndex:
            case PrivateTag.DopplerMapIndex:
            case PrivateTag._0019_xx67_:
            case PrivateTag.DopplerTimeFreqResIndex:
            case PrivateTag.DopplerTraceInverted:
            case PrivateTag.DopplerTintType:
            case PrivateTag.DopplerTintIndex:
            case PrivateTag.MModeDynamicRange:
            case PrivateTag.MModeOverallGain:
            case PrivateTag.MModeEdgeEnhanceIndex:
            case PrivateTag.MModeMapIndex:
            case PrivateTag.MModeTintType:
            case PrivateTag.MModeTintIndex:
            case PrivateTag._0019_xx95_:
                return VR.US;
        }
        return VR.UN;
    }

}
