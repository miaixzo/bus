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
package org.miaixz.bus.image.metric;

import java.io.IOException;
import java.util.Objects;

import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Implementation;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.IdentityNegotiator;
import org.miaixz.bus.image.metric.pdu.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AssociationHandler {

    private IdentityNegotiator userIdNegotiator = new IdentityNegotiator() {
    };

    public IdentityNegotiator getUserIdNegotiator() {
        return userIdNegotiator;
    }

    public void setUserIdNegotiator(IdentityNegotiator userIdNegotiator) {
        this.userIdNegotiator = Objects.requireNonNull(userIdNegotiator);
    }

    protected AAssociateAC negotiate(Association as, AAssociateRQ rq) throws IOException {
        if ((rq.getProtocolVersion() & 1) == 0)
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT, AAssociateRJ.SOURCE_SERVICE_PROVIDER_ACSE,
                    AAssociateRJ.REASON_PROTOCOL_VERSION_NOT_SUPPORTED);
        if (!rq.getApplicationContext().equals(UID.DICOMApplicationContext))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT, AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_APP_CTX_NAME_NOT_SUPPORTED);
        ApplicationEntity ae = as.getApplicationEntity();
        if (ae == null || !ae.getConnections().contains(as.getConnection()) || !ae.isInstalled()
                || !ae.isAssociationAcceptor())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT, AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLED_AET_NOT_RECOGNIZED);
        if (!ae.isAcceptedCallingAETitle(rq.getCallingAET()))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT, AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED);
        IdentityAC userIdentity = getUserIdNegotiator().negotiate(as, rq.getUserIdentityRQ());
        if (ae.getDevice().isLimitOfAssociationsExceeded(rq))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_TRANSIENT, AAssociateRJ.SOURCE_SERVICE_PROVIDER_PRES,
                    AAssociateRJ.REASON_LOCAL_LIMIT_EXCEEDED);
        return makeAAssociateAC(as, rq, userIdentity);
    }

    protected AAssociateAC makeAAssociateAC(Association as, AAssociateRQ rq, IdentityAC userIdentity) {
        AAssociateAC ac = new AAssociateAC();
        ac.setImplVersionName(Implementation.getVersionName());
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        Connection conn = as.getConnection();
        ac.setMaxPDULength(conn.getReceivePDULength());
        ac.setMaxOpsInvoked(Association.minZeroAsMax(rq.getMaxOpsInvoked(), conn.getMaxOpsPerformed()));
        ac.setMaxOpsPerformed(Association.minZeroAsMax(rq.getMaxOpsPerformed(), conn.getMaxOpsInvoked()));
        ac.setIdentityAC(userIdentity);
        ApplicationEntity ae = as.getApplicationEntity().transferCapabilitiesAE();
        for (PresentationContext rqpc : rq.getPresentationContexts())
            ac.addPresentationContext(ae != null ? ae.negotiate(rq, ac, rqpc)
                    : new PresentationContext(rqpc.getPCID(), PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED,
                            rqpc.getTransferSyntax()));
        return ac;
    }

    protected void onClose(Association as) {
        DimseRQHandler tmp = as.getApplicationEntity().getDimseRQHandler();
        if (tmp != null)
            tmp.onClose(as);
    }

}
