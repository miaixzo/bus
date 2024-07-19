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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Commands;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.DimseRSPHandler;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.PDVInputStream;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.image.metric.service.AbstractImageService;
import org.miaixz.bus.image.metric.service.ImageService;
import org.miaixz.bus.image.metric.service.ImageServiceException;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpsSCU {

    private static int status;
    private static final ImageService upsscuNEventRqHandler =
            new AbstractImageService(UID.UnifiedProcedureStepPush.uid) {
                @Override
                public void onDimseRQ(Association as, PresentationContext pc,
                                      Dimse dimse, Attributes cmd, PDVInputStream data)
                        throws IOException {
                    if (dimse != Dimse.N_EVENT_REPORT_RQ)
                        throw new ImageServiceException(Status.UnrecognizedOperation);

                    int eventTypeID = cmd.getInt(Tag.EventTypeID, 0);
                    if (eventTypeID == 0 || eventTypeID > 5)
                        throw new ImageServiceException(Status.NoSuchEventType).setEventTypeID(eventTypeID);

                    try {
                        as.writeDimseRSP(pc, Commands.mkNEventReportRSP(cmd, status));
                    } catch (InternalException e) {
                        Logger.warn("{} << N-EVENT-RECORD-RSP failed: {}", as, e.getMessage());
                    }
                }

                @Override
                protected void onDimseRQ(Association as, PresentationContext pc,
                                         Dimse dimse, Attributes cmd, Attributes data) {
                    throw new UnsupportedOperationException();
                }
            };
    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private Association as;
    //default response handler
    private final RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory() {
        @Override
        public DimseRSPHandler createDimseRSPHandlerForCFind() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd,
                                       Attributes data) {
                    //TODO
                }
            };
        }

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNCreate() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNSet() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNGet() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNAction() {
            return new DimseRSPHandler(as.nextMessageID()) {
                @Override
                public void onDimseRSP(Association as, Attributes cmd,
                                       Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }
    };
    private String xmlFile;
    private String[] keys;
    private int[] tags;
    private String upsiuid;
    private Operation operation;
    private Attributes requestCancel;
    private Attributes changeState;
    private Attributes subscriptionAction;

    public UpsSCU(ApplicationEntity ae) {
        this.remote = new Connection();
        this.ae = ae;
    }

    private static int[] toTags(String[] tagsAsStr) {
        int[] tags = new int[tagsAsStr.length];
        for (int i = 0; i < tagsAsStr.length; i++)
            tags[i] = Tag.forName(tagsAsStr[i]);
        return tags;
    }

    private static Attributes state(String uid, String code) {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.TransactionUID, VR.UI, uid);
        attrs.setString(Tag.ProcedureStepState, VR.CS, code);
        return attrs;
    }

    public void addVerificationPresentationContext() {
        rq.addPresentationContext(
                new PresentationContext(1, UID.Verification.uid,
                        UID.ImplicitVRLittleEndian.uid));
    }

    public final void setUPSIUID(String upsiuid) {
        this.upsiuid = upsiuid;
    }

    public final void setKeys(String[] keys) {
        this.keys = keys;
    }

    public final void setType(Operation operation, String[] tss) {
        this.operation = operation;
        rq.addPresentationContext(new PresentationContext(3, operation.negotiatingSOPClassUID, tss));
    }

    public final void setXmlFile(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public void setTags(int[] tags) {
        this.tags = tags;
    }

    public void setChangeState(Attributes changeState) {
        this.changeState = changeState;
    }

    public void setRequestCancel(Attributes requestCancel) {
        this.requestCancel = requestCancel;
    }

    public void setSubscriptionAction(Attributes subscriptionAction) {
        this.subscriptionAction = subscriptionAction;
    }

    public void open() throws IOException, InterruptedException,
            InternalException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.waitForOutstandingRSP();
            as.release();
            as.waitForSocketClose();
        }
    }

    private void getUps() throws IOException, InterruptedException {
        as.nget(operation.getNegotiatingSOPClassUID(),
                UID.UnifiedProcedureStepPush.uid,
                upsiuid,
                tags,
                rspHandlerFactory.createDimseRSPHandlerForNGet());
    }

    private Attributes ensureSPSStartDateTime(Attributes ups) {
        if (!ups.containsValue(Tag.ScheduledProcedureStepStartDateTime))
            ups.setString(Tag.ScheduledProcedureStepStartDateTime, VR.DT, Format.formatDT(null, new Date()));
        return ups;
    }

    private void actionOnUps(Attributes data, int actionTypeId) throws IOException, InterruptedException {
        as.naction(operation.negotiatingSOPClassUID,
                UID.UnifiedProcedureStepPush.uid,
                upsiuid,
                actionTypeId,
                data,
                null,
                rspHandlerFactory.createDimseRSPHandlerForNAction());
    }

    enum Operation {
        create(UID.UnifiedProcedureStepPush.uid, false),
        update(UID.UnifiedProcedureStepPull.uid, true),
        get(UID.UnifiedProcedureStepPush.uid, true),
        changeState(UID.UnifiedProcedureStepPull.uid, true),
        requestCancel(UID.UnifiedProcedureStepPush.uid, true),
        subscriptionAction(UID.UnifiedProcedureStepWatch.uid, false),
        receive(UID.UnifiedProcedureStepEvent.uid, false);

        private final boolean checkUPSIUID;
        private String negotiatingSOPClassUID;
        private int actionTypeID;

        Operation(String negotiatingSOPClassUID, boolean checkUPSIUID) {
            this.negotiatingSOPClassUID = negotiatingSOPClassUID;
            this.checkUPSIUID = checkUPSIUID;
        }

        String getNegotiatingSOPClassUID() {
            return negotiatingSOPClassUID;
        }

        Operation setNegotiatingSOPClassUID(String val) {
            this.negotiatingSOPClassUID = val;
            return this;
        }

        int getActionTypeID() {
            return actionTypeID;
        }

        Operation setActionTypeID(int val) {
            this.actionTypeID = val;
            return this;
        }
    }

    public interface RSPHandlerFactory {
        DimseRSPHandler createDimseRSPHandlerForCFind();

        DimseRSPHandler createDimseRSPHandlerForNCreate();

        DimseRSPHandler createDimseRSPHandlerForNSet();

        DimseRSPHandler createDimseRSPHandlerForNGet();

        DimseRSPHandler createDimseRSPHandlerForNAction();
    }

}
