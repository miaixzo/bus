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
import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.image.metric.service.*;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * The {@code StgCmtSCU} class implements a Service Class User (SCU) for the Storage Commitment Push Model SOP Class. It
 * sends storage commitment requests (N-ACTION) and handles the subsequent asynchronous results (N-EVENT-REPORT).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StgCmtSCU {

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
     * A set to track outstanding transaction UIDs for which a result is expected.
     */
    private final HashSet<String> outstandingResults = new HashSet<>(2);
    /**
     * A map to group SOP instances for commitment, keyed by a split key.
     */
    private final HashMap<String, List<String>> map = new HashMap<>();
    /**
     * Additional attributes to be merged. (Not currently used in this implementation).
     */
    private Attributes attrs;
    /**
     * A suffix for generated UIDs. (Not currently used in this implementation).
     */
    private String uidSuffix;
    /**
     * The directory to store received storage commitment result files.
     */
    private File storageDir;
    /**
     * A flag to keep the association alive while waiting for results.
     */
    private boolean keepAlive;
    /**
     * A DICOM tag used to split commitment requests into multiple transactions.
     */
    private int splitTag;
    /**
     * The status code to be returned in the N-EVENT-REPORT response.
     */
    private int status;
    /**
     * The service that handles incoming N-EVENT-REPORT requests (commitment results).
     */
    private final ImageService stgcmtResultHandler = new AbstractImageService(UID.StorageCommitmentPushModel.uid) {

        @Override
        public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, Attributes data)
                throws IOException {
            if (dimse != Dimse.N_EVENT_REPORT_RQ)
                throw new ImageServiceException(Status.UnrecognizedOperation);

            int eventTypeID = cmd.getInt(Tag.EventTypeID, 0);
            if (eventTypeID != 1 && eventTypeID != 2)
                throw new ImageServiceException(Status.NoSuchEventType).setEventTypeID(eventTypeID);
            String tuid = data.getString(Tag.TransactionUID);
            try {
                Attributes rsp = Commands.mkNEventReportRSP(cmd, status);
                Attributes rspAttrs = StgCmtSCU.this.eventRecord(as, cmd, data);
                as.writeDimseRSP(pc, rsp, rspAttrs);
            } catch (InternalException e) {
                Logger.warn("{} << N-EVENT-RECORD-RSP failed: {}", as, e.getMessage());
            } finally {
                removeOutstandingResult(tuid);
            }
        }
    };
    /**
     * The active DICOM association.
     */
    private Association as;

    /**
     * Constructs a new {@code StgCmtSCU} with the given Application Entity.
     *
     * @param ae The Application Entity for this SCU.
     */
    public StgCmtSCU(ApplicationEntity ae) {
        this.remote = new Connection();
        this.ae = ae;
        ImageServiceRegistry serviceRegistry = new ImageServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(stgcmtResultHandler);
        ae.setDimseRQHandler(serviceRegistry);
    }

    /**
     * Gets the remote connection configuration.
     *
     * @return The remote connection.
     */
    public Connection getRemoteConnection() {
        return remote;
    }

    /**
     * Gets the A-ASSOCIATE-RQ message.
     *
     * @return The A-ASSOCIATE-RQ.
     */
    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    /**
     * Gets the directory where storage commitment results are stored.
     *
     * @return The storage directory.
     */
    public File getStorageDirectory() {
        return storageDir;
    }

    /**
     * Sets the directory for storing commitment result files. If the directory does not exist, it will be created.
     *
     * @param storageDir The storage directory.
     */
    public void setStorageDirectory(File storageDir) {
        if (storageDir != null)
            storageDir.mkdirs();
        this.storageDir = storageDir;
    }

    /**
     * Sets a suffix to be appended to generated UIDs.
     *
     * @param uidSuffix The UID suffix.
     */
    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    /**
     * Sets additional attributes. (Not currently used).
     *
     * @param attrs The attributes to set.
     */
    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    /**
     * Sets a DICOM tag to be used for splitting instances into separate commitment requests. All instances with the
     * same value for this tag will be included in the same request.
     *
     * @param splitTag The tag to use for splitting.
     */
    public void setSplitTag(int splitTag) {
        this.splitTag = splitTag;
    }

    /**
     * Sets whether to keep the association alive while waiting for asynchronous commitment results.
     *
     * @param keepAlive {@code true} to keep the association alive.
     */
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * Sets the status code to be sent in the N-EVENT-REPORT response.
     *
     * @param status The DICOM status code.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Configures the presentation contexts for Verification and Storage Commitment, and sets the corresponding transfer
     * capabilities on the Application Entity.
     *
     * @param tss An array of transfer syntax UIDs to propose for Storage Commitment.
     */
    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(new PresentationContext(1, UID.Verification.uid, UID.ImplicitVRLittleEndian.uid));
        rq.addPresentationContext(new PresentationContext(2, UID.StorageCommitmentPushModel.uid, tss));
        ae.addTransferCapability(
                new TransferCapability(null, UID.Verification.uid, TransferCapability.Role.SCP,
                        UID.ImplicitVRLittleEndian.uid));
        ae.addTransferCapability(
                new TransferCapability(null, UID.StorageCommitmentPushModel.uid, TransferCapability.Role.SCU, tss));
    }

    /**
     * Adds a DICOM instance to be included in a storage commitment request. Instances are grouped based on the value of
     * the configured {@code splitTag}.
     *
     * @param inst The attributes of the DICOM instance.
     * @return {@code true} if the instance was added successfully, {@code false} otherwise.
     */
    public boolean addInstance(Attributes inst) {
        String cuid = inst.getString(Tag.SOPClassUID);
        String iuid = inst.getString(Tag.SOPInstanceUID);
        String splitkey = splitTag != 0 ? inst.getString(splitTag, "") : "";
        if (cuid == null || iuid == null) {
            return false;
        }

        List<String> refSOPs = map.computeIfAbsent(splitkey, k -> new ArrayList<>());

        refSOPs.add(cuid);
        refSOPs.add(iuid);
        return true;
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
     * Performs a C-ECHO verification to check the connection.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    /**
     * Closes the DICOM association, waiting for any outstanding responses if configured to do so.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void close() throws IOException, InterruptedException {
        if (as != null) {
            if (as.isReadyForDataTransfer()) {
                as.waitForOutstandingRSP();
                if (keepAlive)
                    waitForOutstandingResults();
                as.release();
            }
            as.waitForSocketClose();
        }
        waitForOutstandingResults();
    }

    /**
     * Adds a transaction UID to the set of outstanding results.
     *
     * @param tuid The transaction UID.
     */
    private void addOutstandingResult(String tuid) {
        synchronized (outstandingResults) {
            outstandingResults.add(tuid);
        }
    }

    /**
     * Removes a transaction UID from the set of outstanding results and notifies waiting threads.
     *
     * @param tuid The transaction UID.
     */
    private void removeOutstandingResult(String tuid) {
        synchronized (outstandingResults) {
            outstandingResults.remove(tuid);
            outstandingResults.notify();
        }
    }

    /**
     * Waits until all outstanding storage commitment results have been received.
     *
     * @throws InterruptedException if the waiting thread is interrupted.
     */
    private void waitForOutstandingResults() throws InterruptedException {
        synchronized (outstandingResults) {
            while (!outstandingResults.isEmpty()) {
                outstandingResults.wait();
            }
        }
    }

    /**
     * Creates the Action Information dataset for a storage commitment request.
     *
     * @param refSOPs A list containing pairs of SOP Class UID and SOP Instance UID.
     * @return The constructed Action Information attributes.
     */
    public Attributes makeActionInfo(List<String> refSOPs) {
        Attributes actionInfo = new Attributes(2);
        actionInfo.setString(Tag.TransactionUID, VR.UI, UID.createUID());
        int n = refSOPs.size() / 2;
        Sequence refSOPSeq = actionInfo.newSequence(Tag.ReferencedSOPSequence, n);
        for (int i = 0, j = 0; j < n; j++) {
            Attributes refSOP = new Attributes(2);
            refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI, refSOPs.get(i++));
            refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI, refSOPs.get(i++));
            refSOPSeq.add(refSOP);
        }
        return actionInfo;
    }

    /**
     * Sends storage commitment requests for all grouped instances.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void sendRequests() throws IOException, InterruptedException {
        for (List<String> refSOPs : map.values())
            sendRequest(makeActionInfo(refSOPs));
    }

    /**
     * Sends a single N-ACTION storage commitment request.
     *
     * @param actionInfo The Action Information dataset for the request.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void sendRequest(Attributes actionInfo) throws IOException, InterruptedException {
        final String tuid = actionInfo.getString(Tag.TransactionUID);
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                if (cmd.getInt(Tag.Status, -1) != Status.Success)
                    removeOutstandingResult(tuid);
                super.onDimseRSP(as, cmd, data);
            }
        };

        as.naction(
                UID.StorageCommitmentPushModel.uid,
                UID.StorageCommitmentPushModelInstance.uid,
                1,
                actionInfo,
                null,
                rspHandler);
        addOutstandingResult(tuid);
    }

    /**
     * Processes a received N-EVENT-REPORT (commitment result) and stores it to a file.
     *
     * @param as        The active association.
     * @param cmd       The N-EVENT-REPORT request command.
     * @param eventInfo The dataset from the N-EVENT-REPORT request.
     * @return {@code null} as no attributes are returned in the response body.
     * @throws ImageServiceException if a storage error occurs.
     */
    private Attributes eventRecord(Association as, Attributes cmd, Attributes eventInfo) throws ImageServiceException {
        if (storageDir == null)
            return null;

        String cuid = cmd.getString(Tag.AffectedSOPClassUID);
        String iuid = cmd.getString(Tag.AffectedSOPInstanceUID);
        String tuid = eventInfo.getString(Tag.TransactionUID);
        File file = new File(storageDir, tuid);
        Logger.info("{}: M-WRITE {}", as, file);
        try (ImageOutputStream out = new ImageOutputStream(file)) {
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian.uid),
                    eventInfo);
        } catch (IOException e) {
            Logger.warn(as + ": Failed to store Storage Commitment Result:", e);
            throw new ImageServiceException(Status.ProcessingFailure, e);
        }
        return null;
    }

}
