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
 * The {@code UpsSCU} class implements a Service Class User (SCU) for the Unified Procedure Step (UPS) SOP Classes. It
 * provides functionality to find, create, update, and manage Unified Procedure Steps on a remote SCP.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpsSCU {

    /**
     * The default status to be returned in N-EVENT-REPORT responses.
     */
    private static int status;
    /**
     * A service to handle incoming N-EVENT-REPORT requests from a UPS SCP.
     */
    private static final ImageService upsscuNEventRqHandler = new AbstractImageService(
            UID.UnifiedProcedureStepPush.uid) {

        @Override
        public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, PDVInputStream data)
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
        protected void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, Attributes data) {
            throw new UnsupportedOperationException();
        }
    };
    /**
     * The Application Entity for this SCU.
     */
    private final ApplicationEntity ae;
    /**
     * The remote connection configuration.
     */
    private final Connection remote;
    /**
     * The A-ASSOCIATE-RQ message.
     */
    private final AAssociateRQ rq = new AAssociateRQ();
    /**
     * The active DICOM association.
     */
    private Association as;
    /**
     * A factory for creating DIMSE response handlers.
     */
    private final RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory() {

        @Override
        public DimseRSPHandler createDimseRSPHandlerForCFind() {
            return new DimseRSPHandler(as.nextMessageID()) {

                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    // C-FIND response handling logic goes here.
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
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }
    };
    /**
     * The path to an XML file containing UPS data.
     */
    private String xmlFile;
    /**
     * An array of query key strings.
     */
    private String[] keys;
    /**
     * An array of attribute tags to retrieve with N-GET.
     */
    private int[] tags;
    /**
     * The SOP Instance UID of the target UPS instance.
     */
    private String upsiuid;
    /**
     * The UPS operation to be performed.
     */
    private Operation operation;
    /**
     * Attributes for a "Request Cancel" N-ACTION.
     */
    private Attributes requestCancel;
    /**
     * Attributes for a "Change State" N-ACTION.
     */
    private Attributes changeState;
    /**
     * Attributes for a "Subscription" N-ACTION.
     */
    private Attributes subscriptionAction;

    /**
     * Constructs a new {@code UpsSCU} with the given Application Entity.
     *
     * @param ae The Application Entity for this SCU.
     */
    public UpsSCU(ApplicationEntity ae) {
        this.remote = new Connection();
        this.ae = ae;
    }

    /**
     * Converts an array of tag names (keywords) into an array of integer tags.
     *
     * @param tagsAsStr An array of tag keywords.
     * @return An array of integer tags.
     */
    private static int[] toTags(String[] tagsAsStr) {
        int[] tags = new int[tagsAsStr.length];
        for (int i = 0; i < tagsAsStr.length; i++)
            tags[i] = Tag.forName(tagsAsStr[i]);
        return tags;
    }

    /**
     * Creates an attributes object for changing the state of a UPS.
     *
     * @param uid  The Transaction UID.
     * @param code The new state code (e.g., "IN PROGRESS").
     * @return The attributes for the N-ACTION request.
     */
    private static Attributes state(String uid, String code) {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.TransactionUID, VR.UI, uid);
        attrs.setString(Tag.ProcedureStepState, VR.CS, code);
        return attrs;
    }

    /**
     * Adds a presentation context for the Verification SOP Class.
     */
    public void addVerificationPresentationContext() {
        rq.addPresentationContext(new PresentationContext(1, UID.Verification.uid, UID.ImplicitVRLittleEndian.uid));
    }

    /**
     * Sets the SOP Instance UID of the target UPS instance.
     *
     * @param upsiuid The UPS SOP Instance UID.
     */
    public final void setUPSIUID(String upsiuid) {
        this.upsiuid = upsiuid;
    }

    /**
     * Sets the query keys for a C-FIND operation.
     *
     * @param keys An array of key strings in "tag=value" format.
     */
    public final void setKeys(String[] keys) {
        this.keys = keys;
    }

    /**
     * Sets the type of UPS operation to perform and configures the necessary presentation context.
     *
     * @param operation The operation to perform.
     * @param tss       The transfer syntaxes to propose.
     */
    public final void setType(Operation operation, String[] tss) {
        this.operation = operation;
        rq.addPresentationContext(new PresentationContext(3, operation.negotiatingSOPClassUID, tss));
    }

    /**
     * Sets the path to an XML file containing the UPS dataset.
     *
     * @param xmlFile The path to the XML file.
     */
    public final void setXmlFile(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    /**
     * Sets the tags of attributes to be retrieved with an N-GET request.
     *
     * @param tags An array of integer tags.
     */
    public void setTags(int[] tags) {
        this.tags = tags;
    }

    /**
     * Sets the attributes for a "Change State" N-ACTION request.
     *
     * @param changeState The attributes for the action.
     */
    public void setChangeState(Attributes changeState) {
        this.changeState = changeState;
    }

    /**
     * Sets the attributes for a "Request Cancel" N-ACTION request.
     *
     * @param requestCancel The attributes for the action.
     */
    public void setRequestCancel(Attributes requestCancel) {
        this.requestCancel = requestCancel;
    }

    /**
     * Sets the attributes for a "Subscription" N-ACTION request.
     *
     * @param subscriptionAction The attributes for the action.
     */
    public void setSubscriptionAction(Attributes subscriptionAction) {
        this.subscriptionAction = subscriptionAction;
    }

    /**
     * Establishes a DICOM association with the remote AE.
     *
     * @throws IOException              if an I/O error occurs.
     * @throws InterruptedException     if the connection is interrupted.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    public void open() throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    /**
     * Closes the DICOM association.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.waitForOutstandingRSP();
            as.release();
            as.waitForSocketClose();
        }
    }

    /**
     * Performs an N-GET operation to retrieve a UPS instance.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void getUps() throws IOException, InterruptedException {
        as.nget(
                operation.getNegotiatingSOPClassUID(),
                UID.UnifiedProcedureStepPush.uid, // N-GET is on Push SOP Class
                upsiuid,
                tags,
                rspHandlerFactory.createDimseRSPHandlerForNGet());
    }

    /**
     * Ensures that the Scheduled Procedure Step Start Date Time attribute is present in a UPS dataset.
     *
     * @param ups The UPS dataset.
     * @return The supplemented dataset.
     */
    private Attributes ensureSPSStartDateTime(Attributes ups) {
        if (!ups.containsValue(Tag.ScheduledProcedureStepStartDateTime))
            ups.setString(Tag.ScheduledProcedureStepStartDateTime, VR.DT, Format.formatDT(null, new Date()));
        return ups;
    }

    /**
     * Performs an N-ACTION operation on a UPS instance.
     *
     * @param data         The action information dataset.
     * @param actionTypeId The Action Type ID.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void actionOnUps(Attributes data, int actionTypeId) throws IOException, InterruptedException {
        as.naction(
                operation.negotiatingSOPClassUID,
                UID.UnifiedProcedureStepPush.uid, // N-ACTION is on Push SOP Class
                upsiuid,
                actionTypeId,
                data,
                null,
                rspHandlerFactory.createDimseRSPHandlerForNAction());
    }

    /**
     * An enumeration of the various UPS operations supported by this SCU.
     */
    enum Operation {

        create(UID.UnifiedProcedureStepPush.uid, false), update(UID.UnifiedProcedureStepPull.uid, true),
        get(UID.UnifiedProcedureStepPush.uid, true), changeState(UID.UnifiedProcedureStepPull.uid, true),
        requestCancel(UID.UnifiedProcedureStepPush.uid, true),
        subscriptionAction(UID.UnifiedProcedureStepWatch.uid, false), receive(UID.UnifiedProcedureStepEvent.uid, false);

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

    /**
     * A factory for creating DIMSE response handlers for various DICOM operations.
     */
    public interface RSPHandlerFactory {

        DimseRSPHandler createDimseRSPHandlerForCFind();

        DimseRSPHandler createDimseRSPHandlerForNCreate();

        DimseRSPHandler createDimseRSPHandlerForNSet();

        DimseRSPHandler createDimseRSPHandlerForNGet();

        DimseRSPHandler createDimseRSPHandlerForNAction();
    }

}
