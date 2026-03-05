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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0009_xx10_:
                return "_0009_xx10_";

            case PrivateTag._0009_xx11_:
                return "_0009_xx11_";

            case PrivateTag._0009_xx13_:
                return "_0009_xx13_";

            case PrivateTag._0009_xx14_:
                return "_0009_xx14_";

            case PrivateTag._0009_xx15_:
                return "_0009_xx15_";

            case PrivateTag.CassetteDataStream:
                return "CassetteDataStream";

            case PrivateTag.ImageProcessingParameters:
                return "ImageProcessingParameters";

            case PrivateTag.IdentificationData:
                return "IdentificationData";

            case PrivateTag.SensitometryName:
                return "SensitometryName";

            case PrivateTag.WindowLevelList:
                return "WindowLevelList";

            case PrivateTag.DoseMonitoring:
                return "DoseMonitoring";

            case PrivateTag.OtherInfo:
                return "OtherInfo";

            case PrivateTag.ClippedExposureDeviation:
                return "ClippedExposureDeviation";

            case PrivateTag.LogarithmicPLTFullScale:
                return "LogarithmicPLTFullScale";

            case PrivateTag.TotalNumberSeries:
                return "TotalNumberSeries";

            case PrivateTag.SessionNumber:
                return "SessionNumber";

            case PrivateTag.IDStationName:
                return "IDStationName";

            case PrivateTag.NumberOfImagesInStudyToBeTransmitted:
                return "NumberOfImagesInStudyToBeTransmitted";

            case PrivateTag.TotalNumberImages:
                return "TotalNumberImages";

            case PrivateTag.GeometricalTransformations:
                return "GeometricalTransformations";

            case PrivateTag.RoamOrigin:
                return "RoamOrigin";

            case PrivateTag.ZoomFactor:
                return "ZoomFactor";

            case PrivateTag.Status:
                return "Status";
        }
        return "";
    }

}
