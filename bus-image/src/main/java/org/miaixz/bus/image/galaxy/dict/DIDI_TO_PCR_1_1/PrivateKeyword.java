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
package org.miaixz.bus.image.galaxy.dict.DIDI_TO_PCR_1_1;

/**
 * @author Kimi Liu
 * @since Java 17+
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
