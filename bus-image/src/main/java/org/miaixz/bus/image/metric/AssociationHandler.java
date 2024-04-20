/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.metric;

import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.metric.internal.pdu.*;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AssociationHandler {

    private IdentityNegotiator userIdNegotiator;

    public IdentityNegotiator getUserIdNegotiator() {
        return userIdNegotiator;
    }

    public void setUserIdNegotiator(IdentityNegotiator userIdNegotiator) {
        this.userIdNegotiator = userIdNegotiator;
    }

    protected AAssociateAC negotiate(Association as, AAssociateRQ rq)
            throws IOException {
        if ((rq.getProtocolVersion() & 1) == 0)
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_PROVIDER_ACSE,
                    AAssociateRJ.REASON_PROTOCOL_VERSION_NOT_SUPPORTED);
        if (!rq.getApplicationContext().equals(
                UID.DICOMApplicationContextName))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_APP_CTX_NAME_NOT_SUPPORTED);
        ApplicationEntity ae = as.getApplicationEntity();
        if (null == ae || !ae.getConnections().contains(as.getConnection())
                || !ae.isInstalled() || !ae.isAssociationAcceptor())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLED_AET_NOT_RECOGNIZED);
        if (!ae.isAcceptedCallingAETitle(rq.getCallingAET()))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED);
        IdentityAC userIdentity = null != getUserIdNegotiator()
                ? getUserIdNegotiator().negotiate(as, rq.getIdentityRQ())
                : null;
        if (ae.getDevice().isLimitOfAssociationsExceeded(rq))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_TRANSIENT,
                    AAssociateRJ.SOURCE_SERVICE_PROVIDER_PRES,
                    AAssociateRJ.REASON_LOCAL_LIMIT_EXCEEDED);
        return makeAAssociateAC(as, rq, userIdentity);
    }

    protected AAssociateAC makeAAssociateAC(Association as, AAssociateRQ rq,
                                            IdentityAC userIdentity) {
        AAssociateAC ac = new AAssociateAC();
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        Connection conn = as.getConnection();
        ac.setMaxPDULength(conn.getReceivePDULength());
        ac.setMaxOpsInvoked(Association.minZeroAsMax(rq.getMaxOpsInvoked(),
                conn.getMaxOpsPerformed()));
        ac.setMaxOpsPerformed(Association.minZeroAsMax(rq.getMaxOpsPerformed(),
                conn.getMaxOpsInvoked()));
        ac.setIdentityAC(userIdentity);
        ApplicationEntity ae = as.getApplicationEntity();
        for (Presentation rqpc : rq.getPresentationContexts())
            ac.addPresentationContext(ae.negotiate(rq, ac, rqpc));
        return ac;
    }

    protected void onClose(Association as) {
        DimseRQHandler tmp = as.getApplicationEntity().getDimseRQHandler();
        if (null != tmp)
            tmp.onClose(as);
    }

}
