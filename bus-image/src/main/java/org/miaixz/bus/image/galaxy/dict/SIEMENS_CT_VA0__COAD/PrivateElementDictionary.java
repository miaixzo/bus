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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CT_VA0__COAD;

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

            case PrivateTag.PatientRegion:
            case PrivateTag.PatientPhaseOfLife:
            case PrivateTag._0019_xxA2_:
            case PrivateTag._0019_xxA3_:
            case PrivateTag._0019_xxAA_:
                return VR.CS;

            case PrivateTag.PulmoDate:
                return VR.DA;

            case PrivateTag.DetectorSpacing:
            case PrivateTag.DetectorCenter:
            case PrivateTag.ReadingIntegrationTime:
            case PrivateTag.DetectorAlignment:
            case PrivateTag._0019_xx52_:
            case PrivateTag._0019_xx54_:
            case PrivateTag.FocusAlignment:
            case PrivateTag.WaterScalingFactor:
            case PrivateTag.InterpolationFactor:
            case PrivateTag.OsteoOffset:
            case PrivateTag.OsteoRegressionLineSlope:
            case PrivateTag.OsteoRegressionLineIntercept:
            case PrivateTag._0019_xxA0_:
            case PrivateTag._0019_xxA1_:
            case PrivateTag._0019_xxA4_:
            case PrivateTag._0019_xxA5_:
            case PrivateTag._0019_xxAF_:
            case PrivateTag.FeedPerRotation:
            case PrivateTag.ExpiratoricReserveVolume:
            case PrivateTag.VitalCapacity:
            case PrivateTag.PulmoWater:
            case PrivateTag.PulmoAir:
                return VR.DS;

            case PrivateTag.DistanceSourceToSourceSideCollimator:
            case PrivateTag.DistanceSourceToDetectorSideCollimator:
            case PrivateTag.NumberOfPossibleChannels:
            case PrivateTag.MeanChannelNumber:
            case PrivateTag.OsteoStandardizationCode:
            case PrivateTag.OsteoPhantomNumber:
            case PrivateTag._0019_xxA9_:
            case PrivateTag._0019_xxAB_:
            case PrivateTag._0019_xxAC_:
            case PrivateTag._0019_xxAD_:
            case PrivateTag._0019_xxAE_:
            case PrivateTag.PulmoTriggerLevel:
            case PrivateTag._0019_xxC5_:
                return VR.IS;

            case PrivateTag._0019_xxB1_:
                return VR.LO;

            case PrivateTag.PulmoTime:
                return VR.TM;

            case PrivateTag.FocalSpotDeflectionAmplitude:
            case PrivateTag.FocalSpotDeflectionPhase:
            case PrivateTag.FocalSpotDeflectionOffset:
            case PrivateTag._0019_xxA6_:
            case PrivateTag._0019_xxA7_:
            case PrivateTag._0019_xxA8_:
            case PrivateTag._0019_xxC4_:
                return VR.UL;
        }
        return VR.UN;
    }

}
