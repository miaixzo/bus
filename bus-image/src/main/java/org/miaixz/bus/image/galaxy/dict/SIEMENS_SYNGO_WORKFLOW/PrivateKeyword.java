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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_WORKFLOW;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.InternalPatientUID:
                return "InternalPatientUID";

            case PrivateTag.PatientsDeathIndicator:
                return "PatientsDeathIndicator";

            case PrivateTag.PatientsDeathDate:
                return "PatientsDeathDate";

            case PrivateTag.PatientsDeathTime:
                return "PatientsDeathTime";

            case PrivateTag.VIPIndicator:
                return "VIPIndicator";

            case PrivateTag.EmergencyFlag:
                return "EmergencyFlag";

            case PrivateTag.InternalVisitUID:
                return "InternalVisitUID";

            case PrivateTag.InternalISRUID:
                return "InternalISRUID";

            case PrivateTag.ControlState:
                return "ControlState";

            case PrivateTag.LocalFlag:
                return "LocalFlag";

            case PrivateTag.ReferencedStudies:
                return "ReferencedStudies";

            case PrivateTag.WorkflowID:
                return "WorkflowID";

            case PrivateTag.WorkflowDescription:
                return "WorkflowDescription";

            case PrivateTag.WorkflowControlState:
                return "WorkflowControlState";

            case PrivateTag.WorkflowAdHocFlag:
                return "WorkflowAdHocFlag";

            case PrivateTag.HybridFlag:
                return "HybridFlag";

            case PrivateTag.WorkitemID:
                return "WorkitemID";

            case PrivateTag.WorkitemName:
                return "WorkitemName";

            case PrivateTag.WorkitemType:
                return "WorkitemType";

            case PrivateTag.WorkitemRoles:
                return "WorkitemRoles";

            case PrivateTag.WorkitemDescription:
                return "WorkitemDescription";

            case PrivateTag.WorkitemControlState:
                return "WorkitemControlState";

            case PrivateTag.ClaimingUser:
                return "ClaimingUser";

            case PrivateTag.ClaimingHost:
                return "ClaimingHost";

            case PrivateTag.TaskflowID:
                return "TaskflowID";

            case PrivateTag.TaskflowName:
                return "TaskflowName";

            case PrivateTag.FailedFlag:
                return "FailedFlag";

            case PrivateTag.ScheduledTime:
                return "ScheduledTime";

            case PrivateTag.WorkitemAdHocFlag:
                return "WorkitemAdHocFlag";

            case PrivateTag.PatientUpdatePendingFlag:
                return "PatientUpdatePendingFlag";

            case PrivateTag.PatientMixupFlag:
                return "PatientMixupFlag";

            case PrivateTag.ClientID:
                return "ClientID";

            case PrivateTag.TemplateID:
                return "TemplateID";

            case PrivateTag.InstitutionName:
                return "InstitutionName";

            case PrivateTag.InstitutionAddress:
                return "InstitutionAddress";

            case PrivateTag.InstitutionCodeSequence:
                return "InstitutionCodeSequence";
        }
        return "";
    }

}
