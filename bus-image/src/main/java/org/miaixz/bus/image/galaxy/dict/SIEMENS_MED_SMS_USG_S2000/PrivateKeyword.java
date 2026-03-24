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

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.PrivateCreatorVersion:
                return "PrivateCreatorVersion";

            case PrivateTag.FrameRate:
                return "FrameRate";

            case PrivateTag.BurnedInGraphics:
                return "BurnedInGraphics";

            case PrivateTag.SieClearIndex:
                return "SieClearIndex";

            case PrivateTag._0019_xx0E_:
                return "_0019_xx0E_";

            case PrivateTag.BModeSubmode:
                return "BModeSubmode";

            case PrivateTag.BModeDynamicRange:
                return "BModeDynamicRange";

            case PrivateTag.BModeOverallGain:
                return "BModeOverallGain";

            case PrivateTag.BModeResolutionSpeedIndex:
                return "BModeResolutionSpeedIndex";

            case PrivateTag.BModeEdgeEnhanceIndex:
                return "BModeEdgeEnhanceIndex";

            case PrivateTag.BModePersistenceIndex:
                return "BModePersistenceIndex";

            case PrivateTag.BModeMapIndex:
                return "BModeMapIndex";

            case PrivateTag._0019_xx27_:
                return "_0019_xx27_";

            case PrivateTag._0019_xx28_:
                return "_0019_xx28_";

            case PrivateTag._0019_xx29_:
                return "_0019_xx29_";

            case PrivateTag.BModeTintType:
                return "BModeTintType";

            case PrivateTag.BModeTintIndex:
                return "BModeTintIndex";

            case PrivateTag.ClarifyVEIndex:
                return "ClarifyVEIndex";

            case PrivateTag._0019_xx30_:
                return "_0019_xx30_";

            case PrivateTag._0019_xx31_:
                return "_0019_xx31_";

            case PrivateTag.ImageFlag:
                return "ImageFlag";

            case PrivateTag._0019_xx3B_:
                return "_0019_xx3B_";

            case PrivateTag.ColorFlowState:
                return "ColorFlowState";

            case PrivateTag.ColorFlowWallFilterIndex:
                return "ColorFlowWallFilterIndex";

            case PrivateTag.ColorFlowSubmode:
                return "ColorFlowSubmode";

            case PrivateTag.ColorFlowOverallGain:
                return "ColorFlowOverallGain";

            case PrivateTag.ColorFlowResolutionSpeedIndex:
                return "ColorFlowResolutionSpeedIndex";

            case PrivateTag.ColorFlowSmoothIndex:
                return "ColorFlowSmoothIndex";

            case PrivateTag.ColorFlowPersistenceIndex:
                return "ColorFlowPersistenceIndex";

            case PrivateTag.ColorFlowMapIndex:
                return "ColorFlowMapIndex";

            case PrivateTag.ColorFlowPriorityIndex:
                return "ColorFlowPriorityIndex";

            case PrivateTag.ColorFlowMaximumVelocity:
                return "ColorFlowMaximumVelocity";

            case PrivateTag.DopplerDynamicRange:
                return "DopplerDynamicRange";

            case PrivateTag.DopplerOverallGain:
                return "DopplerOverallGain";

            case PrivateTag.DopplerWallFilter:
                return "DopplerWallFilter";

            case PrivateTag.DopplerGateSize:
                return "DopplerGateSize";

            case PrivateTag.DopplerMapIndex:
                return "DopplerMapIndex";

            case PrivateTag.DopplerSubmode:
                return "DopplerSubmode";

            case PrivateTag._0019_xx67_:
                return "_0019_xx67_";

            case PrivateTag.DopplerTimeFreqResIndex:
                return "DopplerTimeFreqResIndex";

            case PrivateTag.DopplerTraceInverted:
                return "DopplerTraceInverted";

            case PrivateTag.DopplerTintType:
                return "DopplerTintType";

            case PrivateTag.DopplerTintIndex:
                return "DopplerTintIndex";

            case PrivateTag.MModeDynamicRange:
                return "MModeDynamicRange";

            case PrivateTag.MModeOverallGain:
                return "MModeOverallGain";

            case PrivateTag.MModeEdgeEnhanceIndex:
                return "MModeEdgeEnhanceIndex";

            case PrivateTag.MModeMapIndex:
                return "MModeMapIndex";

            case PrivateTag.MModeTintType:
                return "MModeTintType";

            case PrivateTag.MModeSubmode:
                return "MModeSubmode";

            case PrivateTag.MModeTintIndex:
                return "MModeTintIndex";

            case PrivateTag._0019_xx95_:
                return "_0019_xx95_";
        }
        return "";
    }

}
