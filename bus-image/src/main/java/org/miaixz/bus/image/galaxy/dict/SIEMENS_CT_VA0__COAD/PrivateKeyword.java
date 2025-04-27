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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CT_VA0__COAD;

/**
 * @author Kimi Liu
 * @since Java 17+
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
