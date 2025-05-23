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
