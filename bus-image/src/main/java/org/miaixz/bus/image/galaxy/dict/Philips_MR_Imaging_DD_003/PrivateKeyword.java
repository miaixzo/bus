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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_003;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.NumberOfSOPCommon:
                return "NumberOfSOPCommon";

            case PrivateTag.NoOfFilmConsumption:
                return "NoOfFilmConsumption";

            case PrivateTag.NumberOfCodes:
                return "NumberOfCodes";

            case PrivateTag.NumberOfImagePerSeriesRef:
                return "NumberOfImagePerSeriesRef";

            case PrivateTag.NoDateOfLastCalibration:
                return "NoDateOfLastCalibration";

            case PrivateTag.NoTimeOfLastCalibration:
                return "NoTimeOfLastCalibration";

            case PrivateTag.NrOfSoftwareVersion:
                return "NrOfSoftwareVersion";

            case PrivateTag.NrOfPatientOtherNames:
                return "NrOfPatientOtherNames";

            case PrivateTag.NrOfReqRecipeOfResults:
                return "NrOfReqRecipeOfResults";

            case PrivateTag.NrOfSeriesOperatorsName:
                return "NrOfSeriesOperatorsName";

            case PrivateTag.NrOfSeriesPerfPhysiName:
                return "NrOfSeriesPerfPhysiName";

            case PrivateTag.NrOfStudyAdmittingDiagnosticDescr:
                return "NrOfStudyAdmittingDiagnosticDescr";

            case PrivateTag.NrOfStudyPatientContrastAllergies:
                return "NrOfStudyPatientContrastAllergies";

            case PrivateTag.NrOfStudyPatientMedicalAlerts:
                return "NrOfStudyPatientMedicalAlerts";

            case PrivateTag.NrOfStudyPhysiciansOfRecord:
                return "NrOfStudyPhysiciansOfRecord";

            case PrivateTag.NrOfStudyPhysiReadingStudy:
                return "NrOfStudyPhysiReadingStudy";

            case PrivateTag.NrSCSoftwareVersions:
                return "NrSCSoftwareVersions";

            case PrivateTag.NrRunningAttributes:
                return "NrRunningAttributes";

            case PrivateTag.SpectrumPixelData:
                return "SpectrumPixelData";

            case PrivateTag.DefaultImageUID:
                return "DefaultImageUID";

            case PrivateTag.RunningAttributes:
                return "RunningAttributes";
        }
        return "";
    }

}
