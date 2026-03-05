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
            ac.addPresentationContext(
                    ae != null ? ae.negotiate(rq, ac, rqpc)
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
