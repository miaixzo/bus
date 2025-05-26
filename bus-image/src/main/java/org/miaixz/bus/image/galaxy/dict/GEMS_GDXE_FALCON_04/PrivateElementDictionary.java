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
package org.miaixz.bus.image.galaxy.dict.GEMS_GDXE_FALCON_04;

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

        case PrivateTag.AcquisitionType:
        case PrivateTag.OverrideMode:
        case PrivateTag.ImageNumberInPasting:
        case PrivateTag.ReceptorType:
        case PrivateTag._0011_xx59_:
        case PrivateTag._0011_xx60_:
            return VR.CS;
        case PrivateTag.ImageDose:
        case PrivateTag.DetectorARCGain:
        case PrivateTag.FilmSpeedSelection:
        case PrivateTag.DetectorExposureIndex:
        case PrivateTag.CompensatedDetectorExposure:
        case PrivateTag.UncompensatedDetectorExposure:
        case PrivateTag.MedianAnatomyCountValue:
        case PrivateTag.DEILowerAndUpperLimitValues:
        case PrivateTag._0011_xx47_:
        case PrivateTag._0011_xx6D_:
            return VR.DS;
        case PrivateTag.StudyDose:
        case PrivateTag.StudyDAP:
            return VR.FL;
        case PrivateTag.DetectedFieldOfView:
        case PrivateTag.AdjustedFieldOfView:
        case PrivateTag.SubImageCollimatorVertices:
        case PrivateTag.KeystoneCoordinates:
            return VR.IS;
        case PrivateTag.ViewIP:
        case PrivateTag._0011_xx46_:
            return VR.LO;
        case PrivateTag.ROI:
        case PrivateTag.PatientSizeString:
        case PrivateTag.ProcessingDebugInfo:
            return VR.LT;
        case PrivateTag.NonDigitalExposures:
        case PrivateTag.TotalExposures:
        case PrivateTag.ShiftVectorForPasting:
        case PrivateTag.PastingOverlap:
            return VR.SL;
        case PrivateTag.ProcessedSeriesUID:
        case PrivateTag.AcquisitionUID:
        case PrivateTag.SPSUID:
        case PrivateTag._0011_xx14_:
            return VR.UI;
        }
        return VR.UN;
    }

}
