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

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.DistanceSourceToSourceSideCollimator:
                return "DistanceSourceToSourceSideCollimator";

            case PrivateTag.DistanceSourceToDetectorSideCollimator:
                return "DistanceSourceToDetectorSideCollimator";

            case PrivateTag.NumberOfPossibleChannels:
                return "NumberOfPossibleChannels";

            case PrivateTag.MeanChannelNumber:
                return "MeanChannelNumber";

            case PrivateTag.DetectorSpacing:
                return "DetectorSpacing";

            case PrivateTag.DetectorCenter:
                return "DetectorCenter";

            case PrivateTag.ReadingIntegrationTime:
                return "ReadingIntegrationTime";

            case PrivateTag.DetectorAlignment:
                return "DetectorAlignment";

            case PrivateTag._0019_xx52_:
                return "_0019_xx52_";

            case PrivateTag._0019_xx54_:
                return "_0019_xx54_";

            case PrivateTag.FocusAlignment:
                return "FocusAlignment";

            case PrivateTag.FocalSpotDeflectionAmplitude:
                return "FocalSpotDeflectionAmplitude";

            case PrivateTag.FocalSpotDeflectionPhase:
                return "FocalSpotDeflectionPhase";

            case PrivateTag.FocalSpotDeflectionOffset:
                return "FocalSpotDeflectionOffset";

            case PrivateTag.WaterScalingFactor:
                return "WaterScalingFactor";

            case PrivateTag.InterpolationFactor:
                return "InterpolationFactor";

            case PrivateTag.PatientRegion:
                return "PatientRegion";

            case PrivateTag.PatientPhaseOfLife:
                return "PatientPhaseOfLife";

            case PrivateTag.OsteoOffset:
                return "OsteoOffset";

            case PrivateTag.OsteoRegressionLineSlope:
                return "OsteoRegressionLineSlope";

            case PrivateTag.OsteoRegressionLineIntercept:
                return "OsteoRegressionLineIntercept";

            case PrivateTag.OsteoStandardizationCode:
                return "OsteoStandardizationCode";

            case PrivateTag.OsteoPhantomNumber:
                return "OsteoPhantomNumber";

            case PrivateTag._0019_xxA0_:
                return "_0019_xxA0_";

            case PrivateTag._0019_xxA1_:
                return "_0019_xxA1_";

            case PrivateTag._0019_xxA2_:
                return "_0019_xxA2_";

            case PrivateTag._0019_xxA3_:
                return "_0019_xxA3_";

            case PrivateTag._0019_xxA4_:
                return "_0019_xxA4_";

            case PrivateTag._0019_xxA5_:
                return "_0019_xxA5_";

            case PrivateTag._0019_xxA6_:
                return "_0019_xxA6_";

            case PrivateTag._0019_xxA7_:
                return "_0019_xxA7_";

            case PrivateTag._0019_xxA8_:
                return "_0019_xxA8_";

            case PrivateTag._0019_xxA9_:
                return "_0019_xxA9_";

            case PrivateTag._0019_xxAA_:
                return "_0019_xxAA_";

            case PrivateTag._0019_xxAB_:
                return "_0019_xxAB_";

            case PrivateTag._0019_xxAC_:
                return "_0019_xxAC_";

            case PrivateTag._0019_xxAD_:
                return "_0019_xxAD_";

            case PrivateTag._0019_xxAE_:
                return "_0019_xxAE_";

            case PrivateTag._0019_xxAF_:
                return "_0019_xxAF_";

            case PrivateTag.FeedPerRotation:
                return "FeedPerRotation";

            case PrivateTag._0019_xxB1_:
                return "_0019_xxB1_";

            case PrivateTag.PulmoTriggerLevel:
                return "PulmoTriggerLevel";

            case PrivateTag.ExpiratoricReserveVolume:
                return "ExpiratoricReserveVolume";

            case PrivateTag.VitalCapacity:
                return "VitalCapacity";

            case PrivateTag.PulmoWater:
                return "PulmoWater";

            case PrivateTag.PulmoAir:
                return "PulmoAir";

            case PrivateTag.PulmoDate:
                return "PulmoDate";

            case PrivateTag.PulmoTime:
                return "PulmoTime";

            case PrivateTag._0019_xxC4_:
                return "_0019_xxC4_";

            case PrivateTag._0019_xxC5_:
                return "_0019_xxC5_";
        }
        return "";
    }

}
