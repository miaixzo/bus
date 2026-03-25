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

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.RouteAET:
                return "RouteAET";

            case PrivateTag.PCRPrintScale:
                return "PCRPrintScale";

            case PrivateTag.PCRPrintJobEnd:
                return "PCRPrintJobEnd";

            case PrivateTag.PCRNoFilmCopies:
                return "PCRNoFilmCopies";

            case PrivateTag.PCRFilmLayoutPosition:
                return "PCRFilmLayoutPosition";

            case PrivateTag.PCRPrintReportName:
                return "PCRPrintReportName";

            case PrivateTag.RADProtocolPrinter:
                return "RADProtocolPrinter";

            case PrivateTag.RADProtocolMedium:
                return "RADProtocolMedium";

            case PrivateTag.ExposureIndex:
                return "ExposureIndex";

            case PrivateTag.CollimatorX:
                return "CollimatorX";

            case PrivateTag.CollimatorY:
                return "CollimatorY";

            case PrivateTag.PrintMarker:
                return "PrintMarker";

            case PrivateTag.RGDVName:
                return "RGDVName";

            case PrivateTag.AcqdSensitivity:
                return "AcqdSensitivity";

            case PrivateTag.ProcessingCategory:
                return "ProcessingCategory";

            case PrivateTag.UnprocessedFlag:
                return "UnprocessedFlag";

            case PrivateTag.KeyValues:
                return "KeyValues";

            case PrivateTag.DestinationPostprocessingFunction:
                return "DestinationPostprocessingFunction";

            case PrivateTag.Version:
                return "Version";

            case PrivateTag.RangingMode:
                return "RangingMode";

            case PrivateTag.AbdomenBrightness:
                return "AbdomenBrightness";

            case PrivateTag.FixedBrightness:
                return "FixedBrightness";

            case PrivateTag.DetailContrast:
                return "DetailContrast";

            case PrivateTag.ContrastBalance:
                return "ContrastBalance";

            case PrivateTag.StructureBoost:
                return "StructureBoost";

            case PrivateTag.StructurePreference:
                return "StructurePreference";

            case PrivateTag.NoiseRobustness:
                return "NoiseRobustness";

            case PrivateTag.NoiseDoseLimit:
                return "NoiseDoseLimit";

            case PrivateTag.NoiseDoseStep:
                return "NoiseDoseStep";

            case PrivateTag.NoiseFrequencyLimit:
                return "NoiseFrequencyLimit";

            case PrivateTag.WeakContrastLimit:
                return "WeakContrastLimit";

            case PrivateTag.StrongContrastLimit:
                return "StrongContrastLimit";

            case PrivateTag.StructureBoostOffset:
                return "StructureBoostOffset";

            case PrivateTag.SmoothGain:
                return "SmoothGain";

            case PrivateTag.MeasureField1:
                return "MeasureField1";

            case PrivateTag.MeasureField2:
                return "MeasureField2";

            case PrivateTag.KeyPercentile1:
                return "KeyPercentile1";

            case PrivateTag.KeyPercentile2:
                return "KeyPercentile2";

            case PrivateTag.DensityLUT:
                return "DensityLUT";

            case PrivateTag.Brightness:
                return "Brightness";

            case PrivateTag.Gamma:
                return "Gamma";

            case PrivateTag.StampImageSequence:
                return "StampImageSequence";
        }
        return "";
    }

}
