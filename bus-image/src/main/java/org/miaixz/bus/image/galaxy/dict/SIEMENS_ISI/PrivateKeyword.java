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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_ISI;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.ISICommandField:
                return "ISICommandField";

            case PrivateTag.AttachIDApplicationCode:
                return "AttachIDApplicationCode";

            case PrivateTag.AttachIDMessageCount:
                return "AttachIDMessageCount";

            case PrivateTag.AttachIDDate:
                return "AttachIDDate";

            case PrivateTag.AttachIDTime:
                return "AttachIDTime";

            case PrivateTag.MessageType:
                return "MessageType";

            case PrivateTag.MaxWaitingDate:
                return "MaxWaitingDate";

            case PrivateTag.MaxWaitingTime:
                return "MaxWaitingTime";

            case PrivateTag.RISPatientInfoIMGEF:
                return "RISPatientInfoIMGEF";

            case PrivateTag.PatientUID:
                return "PatientUID";

            case PrivateTag.PatientID:
                return "PatientID";

            case PrivateTag.CaseID:
                return "CaseID";

            case PrivateTag.RequestID:
                return "RequestID";

            case PrivateTag.ExaminationUID:
                return "ExaminationUID";

            case PrivateTag.PatientRegistrationDate:
                return "PatientRegistrationDate";

            case PrivateTag.PatientRegistrationTime:
                return "PatientRegistrationTime";

            case PrivateTag.PatientLastName:
                return "PatientLastName";

            case PrivateTag.PatientFirstName:
                return "PatientFirstName";

            case PrivateTag.PatientHospitalStatus:
                return "PatientHospitalStatus";

            case PrivateTag.CurrentLocationTime:
                return "CurrentLocationTime";

            case PrivateTag.PatientInsuranceStatus:
                return "PatientInsuranceStatus";

            case PrivateTag.PatientBillingType:
                return "PatientBillingType";

            case PrivateTag.PatientBillingAddress:
                return "PatientBillingAddress";

            case PrivateTag.ExaminationReason:
                return "ExaminationReason";

            case PrivateTag.RequestedDate:
                return "RequestedDate";

            case PrivateTag.WorklistRequestStartTime:
                return "WorklistRequestStartTime";

            case PrivateTag.WorklistRequestEndTime:
                return "WorklistRequestEndTime";

            case PrivateTag.RequestedTime:
                return "RequestedTime";

            case PrivateTag.RequestedLocation:
                return "RequestedLocation";

            case PrivateTag.CurrentWard:
                return "CurrentWard";

            case PrivateTag.RISKey:
                return "RISKey";

            case PrivateTag.RISWorklistIMGEF:
                return "RISWorklistIMGEF";

            case PrivateTag.RISReportIMGEF:
                return "RISReportIMGEF";

            case PrivateTag.ReportID:
                return "ReportID";

            case PrivateTag.ReportStatus:
                return "ReportStatus";

            case PrivateTag.ReportCreationDate:
                return "ReportCreationDate";

            case PrivateTag.ReportApprovingPhysician:
                return "ReportApprovingPhysician";

            case PrivateTag.ReportText:
                return "ReportText";

            case PrivateTag.ReportAuthor:
                return "ReportAuthor";

            case PrivateTag.ReportingRadiologist:
                return "ReportingRadiologist";
        }
        return "";
    }

}
