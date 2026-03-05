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
package org.miaixz.bus.image.metric.service;

import java.io.IOException;

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Commands;
import org.miaixz.bus.image.metric.pdu.PresentationContext;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicMPPSSCP extends AbstractImageService {

    public BasicMPPSSCP() {
        super(UID.ModalityPerformedProcedureStep.uid);
    }

    public static void mayNoLongerBeUpdated() throws ImageServiceException {
        throw new ImageServiceException(Status.ProcessingFailure,
                "Performed Procedure Step Object may no longer be updated").setErrorID(0xA710);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes rq, Attributes rqAttrs)
            throws IOException {
        switch (dimse) {
            case N_CREATE_RQ:
                onNCreateRQ(as, pc, rq, rqAttrs);
                break;

            case N_SET_RQ:
                onNSetRQ(as, pc, rq, rqAttrs);
                break;

            default:
                throw new ImageServiceException(Status.UnrecognizedOperation);
        }
    }

    protected void onNCreateRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAttrs)
            throws IOException {
        Attributes rsp = Commands.mkNCreateRSP(rq, Status.Success);
        Attributes rspAttrs = create(as, rq, rqAttrs, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }

    protected Attributes create(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp)
            throws ImageServiceException {
        return null;
    }

    protected void onNSetRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAttrs)
            throws IOException {
        Attributes rsp = Commands.mkNSetRSP(rq, Status.Success);
        Attributes rspAttrs = set(as, rq, rqAttrs, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }

    protected Attributes set(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp)
            throws ImageServiceException {
        return null;
    }

}
