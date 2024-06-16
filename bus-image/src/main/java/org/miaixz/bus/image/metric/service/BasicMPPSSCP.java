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
package org.miaixz.bus.image.metric.service;

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Commands;
import org.miaixz.bus.image.metric.ImageException;
import org.miaixz.bus.image.metric.internal.pdu.Presentation;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicMPPSSCP extends AbstractService {

    public BasicMPPSSCP() {
        super(UID.ModalityPerformedProcedureStepSOPClass);
    }

    public static void mayNoLongerBeUpdated() throws ImageException {
        throw new ImageException(Status.ProcessingFailure,
                "Performed Procedure Step Object may no longer be updated")
                .setErrorID(0xA710);
    }

    @Override
    public void onDimse(Association as, Presentation pc, Dimse dimse,
                        Attributes rq, Attributes rqAttrs) throws IOException {
        switch (dimse) {
            case N_CREATE_RQ:
                onNCreateRQ(as, pc, rq, rqAttrs);
                break;
            case N_SET_RQ:
                onNSetRQ(as, pc, rq, rqAttrs);
                break;
            default:
                throw new ImageException(Status.UnrecognizedOperation);
        }
    }

    protected void onNCreateRQ(Association as, Presentation pc,
                               Attributes rq, Attributes rqAttrs) throws IOException {
        Attributes rsp = Commands.mkNCreateRSP(rq, Status.Success);
        Attributes rspAttrs = create(as, rq, rqAttrs, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }

    protected Attributes create(Association as, Attributes rq,
                                Attributes rqAttrs, Attributes rsp) throws ImageException {
        return null;
    }

    protected void onNSetRQ(Association as, Presentation pc,
                            Attributes rq, Attributes rqAttrs) throws IOException {
        Attributes rsp = Commands.mkNSetRSP(rq, Status.Success);
        Attributes rspAttrs = set(as, rq, rqAttrs, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }

    protected Attributes set(Association as, Attributes rq, Attributes rqAttrs,
                             Attributes rsp) throws ImageException {
        return null;
    }

}
