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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_ISI;

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

        case PrivateTag.AttachIDDate:
        case PrivateTag.MaxWaitingDate:
        case PrivateTag.PatientRegistrationDate:
        case PrivateTag.RequestedDate:
        case PrivateTag.ReportCreationDate:
            return VR.DA;
        case PrivateTag.RISKey:
            return VR.DS;
        case PrivateTag.PatientUID:
        case PrivateTag.PatientID:
        case PrivateTag.CaseID:
        case PrivateTag.RequestID:
        case PrivateTag.ExaminationUID:
        case PrivateTag.PatientLastName:
        case PrivateTag.PatientFirstName:
        case PrivateTag.PatientHospitalStatus:
        case PrivateTag.PatientInsuranceStatus:
        case PrivateTag.PatientBillingType:
        case PrivateTag.PatientBillingAddress:
        case PrivateTag.ExaminationReason:
        case PrivateTag.RequestedLocation:
        case PrivateTag.CurrentWard:
        case PrivateTag.ReportID:
        case PrivateTag.ReportStatus:
        case PrivateTag.ReportApprovingPhysician:
        case PrivateTag.ReportText:
        case PrivateTag.ReportAuthor:
        case PrivateTag.ReportingRadiologist:
            return VR.LO;
        case PrivateTag.AttachIDTime:
        case PrivateTag.MaxWaitingTime:
        case PrivateTag.PatientRegistrationTime:
        case PrivateTag.CurrentLocationTime:
        case PrivateTag.WorklistRequestStartTime:
        case PrivateTag.WorklistRequestEndTime:
        case PrivateTag.RequestedTime:
            return VR.TM;
        case PrivateTag.AttachIDMessageCount:
            return VR.UL;
        case PrivateTag.ISICommandField:
        case PrivateTag.AttachIDApplicationCode:
        case PrivateTag.MessageType:
            return VR.US;
        }
        return VR.UN;
    }
}
