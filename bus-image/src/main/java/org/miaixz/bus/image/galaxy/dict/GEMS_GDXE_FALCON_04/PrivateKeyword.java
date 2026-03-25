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
package org.miaixz.bus.image.galaxy.dict.GEMS_GDXE_FALCON_04;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.ProcessedSeriesUID:
                return "ProcessedSeriesUID";

            case PrivateTag.AcquisitionType:
                return "AcquisitionType";

            case PrivateTag.AcquisitionUID:
                return "AcquisitionUID";

            case PrivateTag.ImageDose:
                return "ImageDose";

            case PrivateTag.StudyDose:
                return "StudyDose";

            case PrivateTag.StudyDAP:
                return "StudyDAP";

            case PrivateTag.NonDigitalExposures:
                return "NonDigitalExposures";

            case PrivateTag.TotalExposures:
                return "TotalExposures";

            case PrivateTag.ROI:
                return "ROI";

            case PrivateTag.PatientSizeString:
                return "PatientSizeString";

            case PrivateTag.SPSUID:
                return "SPSUID";

            case PrivateTag._0011_xx14_:
                return "_0011_xx14_";

            case PrivateTag.DetectorARCGain:
                return "DetectorARCGain";

            case PrivateTag.ProcessingDebugInfo:
                return "ProcessingDebugInfo";

            case PrivateTag.OverrideMode:
                return "OverrideMode";

            case PrivateTag.FilmSpeedSelection:
                return "FilmSpeedSelection";

            case PrivateTag._0011_xx27_:
                return "_0011_xx27_";

            case PrivateTag._0011_xx28_:
                return "_0011_xx28_";

            case PrivateTag._0011_xx29_:
                return "_0011_xx29_";

            case PrivateTag._0011_xx30_:
                return "_0011_xx30_";

            case PrivateTag.DetectedFieldOfView:
                return "DetectedFieldOfView";

            case PrivateTag.AdjustedFieldOfView:
                return "AdjustedFieldOfView";

            case PrivateTag.DetectorExposureIndex:
                return "DetectorExposureIndex";

            case PrivateTag.CompensatedDetectorExposure:
                return "CompensatedDetectorExposure";

            case PrivateTag.UncompensatedDetectorExposure:
                return "UncompensatedDetectorExposure";

            case PrivateTag.MedianAnatomyCountValue:
                return "MedianAnatomyCountValue";

            case PrivateTag.DEILowerAndUpperLimitValues:
                return "DEILowerAndUpperLimitValues";

            case PrivateTag.ShiftVectorForPasting:
                return "ShiftVectorForPasting";

            case PrivateTag.ImageNumberInPasting:
                return "ImageNumberInPasting";

            case PrivateTag.PastingOverlap:
                return "PastingOverlap";

            case PrivateTag.SubImageCollimatorVertices:
                return "SubImageCollimatorVertices";

            case PrivateTag.ViewIP:
                return "ViewIP";

            case PrivateTag.KeystoneCoordinates:
                return "KeystoneCoordinates";

            case PrivateTag.ReceptorType:
                return "ReceptorType";

            case PrivateTag._0011_xx46_:
                return "_0011_xx46_";

            case PrivateTag._0011_xx47_:
                return "_0011_xx47_";

            case PrivateTag._0011_xx59_:
                return "_0011_xx59_";

            case PrivateTag._0011_xx60_:
                return "_0011_xx60_";

            case PrivateTag._0011_xx6D_:
                return "_0011_xx6D_";
        }
        return "";
    }

}
