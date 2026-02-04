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
package org.miaixz.bus.image.galaxy.dict.GEMS_HELIOS_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.NumberOfMacroRowsInDetector:
                return "NumberOfMacroRowsInDetector";

            case PrivateTag.MacroWidthAtISOCenter:
                return "MacroWidthAtISOCenter";

            case PrivateTag.DASType:
                return "DASType";

            case PrivateTag.DASGain:
                return "DASGain";

            case PrivateTag.DASTemprature:
                return "DASTemprature";

            case PrivateTag.TableDirection:
                return "TableDirection";

            case PrivateTag.ZSmoothingFactor:
                return "ZSmoothingFactor";

            case PrivateTag.ViewWeightingMode:
                return "ViewWeightingMode";

            case PrivateTag.SigmaRowNumber:
                return "SigmaRowNumber";

            case PrivateTag.MinimumDASValue:
                return "MinimumDASValue";

            case PrivateTag.MaximumOffsetValue:
                return "MaximumOffsetValue";

            case PrivateTag.NumberOfViewsShifted:
                return "NumberOfViewsShifted";

            case PrivateTag.ZTrackingFlag:
                return "ZTrackingFlag";

            case PrivateTag.MeanZError:
                return "MeanZError";

            case PrivateTag.ZTrackingError:
                return "ZTrackingError";

            case PrivateTag.StartView2A:
                return "StartView2A";

            case PrivateTag.NumberOfViews2A:
                return "NumberOfViews2A";

            case PrivateTag.StartView1A:
                return "StartView1A";

            case PrivateTag.SigmaMode:
                return "SigmaMode";

            case PrivateTag.NumberOfViews1A:
                return "NumberOfViews1A";

            case PrivateTag.StartView2B:
                return "StartView2B";

            case PrivateTag.NumberViews2B:
                return "NumberViews2B";

            case PrivateTag.StartView1B:
                return "StartView1B";

            case PrivateTag.NumberOfViews1B:
                return "NumberOfViews1B";

            case PrivateTag.IterboneFlag:
                return "IterboneFlag";

            case PrivateTag.PeristalticFlag:
                return "PeristalticFlag";

            case PrivateTag.CardiacReconAlgorithm:
                return "CardiacReconAlgorithm";

            case PrivateTag.AvgHeartRateForImage:
                return "AvgHeartRateForImage";

            case PrivateTag.TemporalResolution:
                return "TemporalResolution";

            case PrivateTag.PctRpeakDelay:
                return "PctRpeakDelay";

            case PrivateTag._0045_xx34_:
                return "_0045_xx34_";

            case PrivateTag.EkgFullMaStartPhase:
                return "EkgFullMaStartPhase";

            case PrivateTag.EkgFullMaEndPhase:
                return "EkgFullMaEndPhase";

            case PrivateTag.EkgModulationMaxMa:
                return "EkgModulationMaxMa";

            case PrivateTag.EkgModulationMinMa:
                return "EkgModulationMinMa";

            case PrivateTag.NoiseReductionImageFilterDesc:
                return "NoiseReductionImageFilterDesc";

            case PrivateTag.TemporalCenterViewAngle:
                return "TemporalCenterViewAngle";

            case PrivateTag.ReconCenterViewAngle:
                return "ReconCenterViewAngle";

            case PrivateTag.WideConeMasking:
                return "WideConeMasking";

            case PrivateTag.WideConeCornerBlendingRadius:
                return "WideConeCornerBlendingRadius";

            case PrivateTag.WideConeCornerBlendingRadiusOffset:
                return "WideConeCornerBlendingRadiusOffset";

            case PrivateTag.InternalReconAlgorithm:
                return "InternalReconAlgorithm";

            case PrivateTag.PatientCentering:
                return "PatientCentering";

            case PrivateTag.PatientAttenuation:
                return "PatientAttenuation";

            case PrivateTag.WaterEquivalentDiameter:
                return "WaterEquivalentDiameter";

            case PrivateTag.ProjectionMeasure:
                return "ProjectionMeasure";

            case PrivateTag.OvalRatio:
                return "OvalRatio";

            case PrivateTag.EllipseOrientation:
                return "EllipseOrientation";
        }
        return "";
    }

}
