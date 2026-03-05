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
package org.miaixz.bus.image.galaxy.dict.DIDI_TO_PCR_1_1;

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

            case PrivateTag.PCRPrintScale:
            case PrivateTag.KeyValues:
            case PrivateTag.AbdomenBrightness:
            case PrivateTag.FixedBrightness:
            case PrivateTag.DetailContrast:
            case PrivateTag.ContrastBalance:
            case PrivateTag.StructureBoost:
            case PrivateTag.StructurePreference:
            case PrivateTag.NoiseRobustness:
            case PrivateTag.NoiseDoseLimit:
            case PrivateTag.NoiseDoseStep:
            case PrivateTag.NoiseFrequencyLimit:
            case PrivateTag.WeakContrastLimit:
            case PrivateTag.StrongContrastLimit:
            case PrivateTag.StructureBoostOffset:
            case PrivateTag.Brightness:
            case PrivateTag.Gamma:
                return VR.DS;

            case PrivateTag.PCRNoFilmCopies:
            case PrivateTag.PCRFilmLayoutPosition:
            case PrivateTag.ExposureIndex:
            case PrivateTag.CollimatorX:
            case PrivateTag.CollimatorY:
            case PrivateTag.KeyPercentile1:
            case PrivateTag.KeyPercentile2:
            case PrivateTag.DensityLUT:
                return VR.IS;

            case PrivateTag.RouteAET:
            case PrivateTag.PrintMarker:
            case PrivateTag.RGDVName:
            case PrivateTag.AcqdSensitivity:
            case PrivateTag.ProcessingCategory:
            case PrivateTag.UnprocessedFlag:
            case PrivateTag.DestinationPostprocessingFunction:
            case PrivateTag.Version:
            case PrivateTag.RangingMode:
            case PrivateTag.SmoothGain:
            case PrivateTag.MeasureField1:
            case PrivateTag.MeasureField2:
                return VR.LO;

            case PrivateTag.StampImageSequence:
                return VR.SQ;

            case PrivateTag.PCRPrintJobEnd:
            case PrivateTag.PCRPrintReportName:
            case PrivateTag.RADProtocolPrinter:
            case PrivateTag.RADProtocolMedium:
                return VR.ST;
        }
        return VR.UN;
    }

}
