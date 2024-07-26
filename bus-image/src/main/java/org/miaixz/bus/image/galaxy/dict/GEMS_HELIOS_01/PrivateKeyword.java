/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
