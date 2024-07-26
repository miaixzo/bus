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
