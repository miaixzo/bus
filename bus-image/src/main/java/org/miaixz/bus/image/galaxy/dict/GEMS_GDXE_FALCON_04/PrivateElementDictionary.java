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
