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
