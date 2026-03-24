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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_WORKFLOW;

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

            case PrivateTag.PatientsDeathDate:
                return VR.DA;

            case PrivateTag.ScheduledTime:
                return VR.DT;

            case PrivateTag.WorkflowID:
            case PrivateTag.WorkflowDescription:
            case PrivateTag.WorkflowControlState:
            case PrivateTag.WorkitemID:
            case PrivateTag.WorkitemName:
            case PrivateTag.WorkitemType:
            case PrivateTag.WorkitemRoles:
            case PrivateTag.WorkitemDescription:
            case PrivateTag.WorkitemControlState:
            case PrivateTag.ClaimingUser:
            case PrivateTag.ClaimingHost:
            case PrivateTag.TaskflowID:
            case PrivateTag.TaskflowName:
            case PrivateTag.ClientID:
            case PrivateTag.TemplateID:
            case PrivateTag.InstitutionName:
                return VR.LO;

            case PrivateTag.PatientsDeathIndicator:
            case PrivateTag.VIPIndicator:
            case PrivateTag.InternalVisitUID:
            case PrivateTag.InternalISRUID:
            case PrivateTag.ControlState:
                return VR.SH;

            case PrivateTag.InstitutionCodeSequence:
                return VR.SQ;

            case PrivateTag.InstitutionAddress:
                return VR.ST;

            case PrivateTag.PatientsDeathTime:
                return VR.TM;

            case PrivateTag.InternalPatientUID:
            case PrivateTag.ReferencedStudies:
                return VR.UI;

            case PrivateTag.EmergencyFlag:
            case PrivateTag.LocalFlag:
            case PrivateTag.WorkflowAdHocFlag:
            case PrivateTag.HybridFlag:
            case PrivateTag.FailedFlag:
            case PrivateTag.WorkitemAdHocFlag:
            case PrivateTag.PatientUpdatePendingFlag:
            case PrivateTag.PatientMixupFlag:
                return VR.US;
        }
        return VR.UN;
    }

}
