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
package org.miaixz.bus.image.galaxy.dict.GEMS_HELIOS_01;

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

            case PrivateTag.TableDirection:
            case PrivateTag.CardiacReconAlgorithm:
            case PrivateTag.AvgHeartRateForImage:
            case PrivateTag.PctRpeakDelay:
            case PrivateTag._0045_xx34_:
            case PrivateTag.EkgFullMaStartPhase:
            case PrivateTag.EkgFullMaEndPhase:
            case PrivateTag.EkgModulationMaxMa:
            case PrivateTag.EkgModulationMinMa:
            case PrivateTag.WideConeMasking:
            case PrivateTag.InternalReconAlgorithm:
                return VR.CS;

            case PrivateTag.MacroWidthAtISOCenter:
            case PrivateTag.ZSmoothingFactor:
            case PrivateTag.MinimumDASValue:
            case PrivateTag.MaximumOffsetValue:
            case PrivateTag.MeanZError:
            case PrivateTag.ZTrackingError:
            case PrivateTag.TemporalResolution:
            case PrivateTag.PatientCentering:
            case PrivateTag.PatientAttenuation:
            case PrivateTag.WaterEquivalentDiameter:
            case PrivateTag.ProjectionMeasure:
            case PrivateTag.OvalRatio:
            case PrivateTag.EllipseOrientation:
                return VR.FL;

            case PrivateTag.TemporalCenterViewAngle:
            case PrivateTag.ReconCenterViewAngle:
            case PrivateTag.WideConeCornerBlendingRadius:
            case PrivateTag.WideConeCornerBlendingRadiusOffset:
                return VR.FD;

            case PrivateTag.NoiseReductionImageFilterDesc:
                return VR.LO;

            case PrivateTag.NumberOfMacroRowsInDetector:
            case PrivateTag.DASType:
            case PrivateTag.DASGain:
            case PrivateTag.DASTemprature:
            case PrivateTag.ViewWeightingMode:
            case PrivateTag.SigmaRowNumber:
            case PrivateTag.NumberOfViewsShifted:
            case PrivateTag.ZTrackingFlag:
            case PrivateTag.StartView2A:
            case PrivateTag.NumberOfViews2A:
            case PrivateTag.StartView1A:
            case PrivateTag.SigmaMode:
            case PrivateTag.NumberOfViews1A:
            case PrivateTag.StartView2B:
            case PrivateTag.NumberViews2B:
            case PrivateTag.StartView1B:
            case PrivateTag.NumberOfViews1B:
            case PrivateTag.IterboneFlag:
            case PrivateTag.PeristalticFlag:
                return VR.SS;
        }
        return VR.UN;
    }

}
