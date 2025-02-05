/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
