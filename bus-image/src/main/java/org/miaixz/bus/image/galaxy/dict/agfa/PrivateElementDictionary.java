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
package org.miaixz.bus.image.galaxy.dict.agfa;

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

            case PrivateTag.Status:
                return VR.CS;

            case PrivateTag._0009_xx10_:
            case PrivateTag._0009_xx11_:
            case PrivateTag._0009_xx13_:
            case PrivateTag._0009_xx14_:
            case PrivateTag._0009_xx15_:
            case PrivateTag.IdentificationData:
            case PrivateTag.SensitometryName:
            case PrivateTag.DoseMonitoring:
            case PrivateTag.OtherInfo:
            case PrivateTag.ClippedExposureDeviation:
            case PrivateTag.LogarithmicPLTFullScale:
                return VR.LO;

            case PrivateTag.SessionNumber:
            case PrivateTag.IDStationName:
                return VR.SH;

            case PrivateTag.CassetteDataStream:
            case PrivateTag.ImageProcessingParameters:
            case PrivateTag.WindowLevelList:
            case PrivateTag.GeometricalTransformations:
            case PrivateTag.RoamOrigin:
                return VR.ST;

            case PrivateTag.TotalNumberSeries:
            case PrivateTag.NumberOfImagesInStudyToBeTransmitted:
            case PrivateTag.TotalNumberImages:
            case PrivateTag.ZoomFactor:
                return VR.US;
        }
        return VR.UN;
    }

}
