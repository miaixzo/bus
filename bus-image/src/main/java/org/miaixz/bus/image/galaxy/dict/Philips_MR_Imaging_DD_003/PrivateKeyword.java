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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_003;

/**
 * @author Kimi Liu
 * @since Java 17+
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
