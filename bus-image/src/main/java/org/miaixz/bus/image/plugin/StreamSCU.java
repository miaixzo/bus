/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.ProgressStatus;
import org.miaixz.bus.image.galaxy.RelatedSOPClasses;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.DataWriter;
import org.miaixz.bus.image.metric.DimseRSPHandler;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.image.nimble.ImageOutputData;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code StreamSCU} class provides a flexible Service Class User (SCU) for DICOM C-STORE operations, designed for
 * scenarios where multiple images are streamed over a persistent association. It manages the DICOM association
 * lifecycle, including dynamic negotiation of presentation contexts and automatic closure of idle connections.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamSCU {

    /**
     * Helper for managing SOP Class Relationship extended negotiation.
     */
    public final RelatedSOPClasses relSOPClasses = new RelatedSOPClasses();
    /**
     * A map to track the SOP Instance UIDs of objects currently being processed.
     */
    private final Map<String, Integer> instanceUidsCurrentlyProcessed = new ConcurrentHashMap<>();

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
     * The DICOM device for this SCU.
     */
    private final Device device;
    /**
     * The local network connection configuration.
     */
    private final Connection conn;
    /**
     * The overall status and progress of the C-STORE operation.
     */
    private final Status state;
    /**
     * Advanced configuration options.
     */
    private final Args options;
    /**
     * An atomic flag to control the association closure countdown.
     */
    private final AtomicBoolean countdown = new AtomicBoolean(false);
    /**
     * A scheduled executor for closing idle associations.
     */
    private final ScheduledExecutorService closeAssociationExecutor = Executors.newSingleThreadScheduledExecutor();
    /**
     * Additional attributes to be merged.
     */
    private Attributes attrs;
    /**
     * A flag to enable SOP Class Relationship extended negotiation.
     */
    private boolean relExtNeg;
    /**
     * The active DICOM association.
     */
    private Association as;
    /**
     * The task to be executed for closing an idle association.
     */
    private final TimerTask closeAssociationTask = new TimerTask() {

        @Override
        public void run() {
            close(false);
        }
    };
    /**
     * The last recorded status code from a C-STORE response.
     */
    private int lastStatusCode = Integer.MIN_VALUE;
    /**
     * A counter for the number of status logs to prevent flooding.
     */
    private int nbStatusLog = 0;
    /**
     * The total number of sub-operations expected.
     */
    private int numberOfSuboperations = 0;
    /**
     * A factory for creating DIMSE response handlers.
     */
    private final RSPHandlerFactory rspHandlerFactory = () -> new DimseRSPHandler(as.nextMessageID()) {

        @Override
        public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
            super.onDimseRSP(as, cmd, data);
            onCStoreRSP(cmd);

            ImageProgress progress = state.getProgress();
            if (progress != null) {
                progress.setAttributes(cmd);
            }
        }

        private void onCStoreRSP(Attributes cmd) {
            int status = cmd.getInt(Tag.Status, -1);
            state.setStatus(status);
            ProgressStatus ps;

            switch (status) {
                case Status.Success:
                    ps = ProgressStatus.COMPLETED;
                    break;

                case Status.CoercionOfDataElements:
                case Status.ElementsDiscarded:
                case Status.DataSetDoesNotMatchSOPClassWarning:
                    ps = ProgressStatus.WARNING;
                    if (lastStatusCode != status && nbStatusLog < 3) {
                        nbStatusLog++;
                        lastStatusCode = status;
                        Logger.warn("Received C-STORE-RSP with Status {}H", Tag.shortToHexString(status));
                    }
                    break;

                default:
                    ps = ProgressStatus.FAILED;
                    if (lastStatusCode != status && nbStatusLog < 3) {
                        nbStatusLog++;
                        lastStatusCode = status;
                        Logger.error("Received C-STORE-RSP with Status {}H", Tag.shortToHexString(status));
                    }
            }
            Builder.notifyProgession(state.getProgress(), cmd, ps, numberOfSuboperations);
        }
    };
    /**
     * The future result of the scheduled close task.
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * 
     * Constructs a new {@code StreamSCU} with basic node configurations.
     *
     * @param callingNode The configuration of the calling DICOM node.
     * @param calledNode  The configuration of the called DICOM node.
     * @throws IOException if an I/O error occurs.
     */
    public StreamSCU(Node callingNode, Node calledNode) throws IOException {
        this(null, callingNode, calledNode, null);
    }

    /**
     * 
     * Constructs a new {@code StreamSCU} with advanced parameters.
     *
     * @param params      Advanced configuration parameters.
     * @param callingNode The configuration of the calling DICOM node.
     * @param calledNode  The configuration of the called DICOM node.
     * @throws IOException if an I/O error occurs.
     */
    public StreamSCU(Args params, Node callingNode, Node calledNode) throws IOException {
        this(params, callingNode, calledNode, null);
    }

    /**
     * 
     * Constructs a new {@code StreamSCU} with advanced parameters and a progress handler.
     *
     * @param params      Advanced configuration parameters.
     * @param callingNode The configuration of the calling DICOM node.
     * @param calledNode  The configuration of the called DICOM node.
     * @param progress    A progress handler to monitor the operation.
     * @throws IOException if an I/O error occurs.
     */
    public StreamSCU(Args params, Node callingNode, Node calledNode, ImageProgress progress) throws IOException {
        Objects.requireNonNull(callingNode);
        Objects.requireNonNull(calledNode);
        this.options = params == null ? new Args() : params;
        this.state = new Status(progress);
        this.device = new Device("storescu");
        this.conn = new Connection();
        device.addConnection(conn);
        this.ae = new ApplicationEntity(callingNode.getAet());
        device.addApplicationEntity(ae);
        ae.addConnection(conn);

        this.remote = new Connection();

        rq.addPresentationContext(new PresentationContext(1, UID.Verification.uid, UID.ImplicitVRLittleEndian.uid));

        options.configureConnect(rq, remote, calledNode);
        options.configureBind(ae, conn, callingNode);

        options.configure(conn);
        options.configureTLS(conn, remote);

        setAttributes(new Attributes());
    }

    /**
     * 
     * Selects the most appropriate transfer syntax from the ones offered by the SCP.
     *
     * @param as     The active association.
     * @param cuid   The SOP Class UID.
     * @param filets The preferred transfer syntax of the file.
     * @return The selected transfer syntax UID.
     */
    public static String selectTransferSyntax(Association as, String cuid, String filets) {
        Set<String> tss = as.getTransferSyntaxesFor(cuid);
        if (tss.contains(filets)) {
            return filets;
        }

        if (tss.contains(UID.ExplicitVRLittleEndian.uid)) {
            return UID.ExplicitVRLittleEndian.uid;
        }

        return UID.ImplicitVRLittleEndian.uid;
    }

    /**
     * 
     * Sends a C-STORE request over the active association.
     *
     * @param cuid       The SOP Class UID of the object.
     * @param iuid       The SOP Instance UID of the object.
     * @param priority   The priority of the request.
     * @param dataWriter The data writer for the object's dataset.
     * @param tsuid      The Transfer Syntax UID for the transfer.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void cstore(String cuid, String iuid, int priority, DataWriter dataWriter, String tsuid)
            throws IOException, InterruptedException {
        if (as == null) {
            throw new IllegalStateException("Association is null!");
        }
        as.cstore(cuid, iuid, priority, dataWriter, tsuid, rspHandlerFactory.createDimseRSPHandler());
    }

    /**
     * 
     * Gets the configuration of the calling node.
     *
     * @return The calling node.
     */
    public Node getCallingNode() {
        return new Node(ae.getAETitle(), conn.getHostname(), conn.getPort());
    }

    /**
     * 
     * Gets the configuration of the called node.
     *
     * @return The called node.
     */
    public Node getCalledNode() {
        return new Node(rq.getCalledAET(), remote.getHostname(), remote.getPort());
    }

    /**
     * 
     * Gets the local node information from the active association.
     *
     * @return The local node, or {@code null} if not associated.
     */
    public Node getLocalDicomNode() {
        return as == null ? null : Node.buildLocalDicomNode(as);
    }

    /**
     * 
     * Gets the remote node information from the active association.
     *
     * @return The remote node, or {@code null} if not associated.
     */
    public Node getRemoteDicomNode() {
        return as == null ? null : Node.buildRemoteDicomNode(as);
    }

    /**
     * 
     * Selects an appropriate transfer syntax for a given SOP Class.
     *
     * @param cuid  The SOP Class UID.
     * @param tsuid The preferred transfer syntax.
     * @return The selected transfer syntax.
     */
    public String selectTransferSyntax(String cuid, String tsuid) {
        return selectTransferSyntax(as, cuid, tsuid);
    }

    /**
     * 
     * Gets the device associated with this SCU.
     *
     * @return The device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * 
     * Gets the A-ASSOCIATE-RQ message.
     *
     * @return The A-ASSOCIATE-RQ.
     */
    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    /**
     * 
     * Gets the remote connection configuration.
     *
     * @return The remote connection.
     */
    public Connection getRemoteConnection() {
        return remote;
    }

    /**
     * 
     * Gets the attributes to be merged.
     *
     * @return The attributes.
     */
    public Attributes getAttributes() {
        return attrs;
    }

    /**
     * 
     * Sets the attributes to be merged.
     *
     * @param attrs The attributes.
     */
    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    /**
     * 
     * Enables or disables SOP Class Relationship extended negotiation.
     *
     * @param enable {@code true} to enable.
     */
    public final void enableSOPClassRelationshipExtNeg(boolean enable) {
        relExtNeg = enable;
    }

    /**
     * 
     * Gets the advanced configuration options.
     *
     * @return The arguments.
     */
    public Args getOptions() {
        return options;
    }

    /**
     * 
     * Checks if an association is currently established.
     *
     * @return {@code true} if associated.
     */
    public boolean hasAssociation() {
        return as != null;
    }

    /**
     * 
     * Checks if the current association is ready for data transfer.
     *
     * @return {@code true} if ready.
     */
    public boolean isReadyForDataTransfer() {
        return as != null && as.isReadyForDataTransfer();
    }

    /**
     * 
     * Gets the negotiated transfer syntaxes for a given SOP Class.
     *
     * @param cuid The SOP Class UID.
     * @return A set of transfer syntax UIDs.
     */
    public Set<String> getTransferSyntaxesFor(String cuid) {
        return as == null ? Collections.emptySet() : as.getTransferSyntaxesFor(cuid);
    }

    /**
     * 
     * Gets the number of sub-operations.
     *
     * @return The number of sub-operations.
     */
    public int getNumberOfSuboperations() {
        return numberOfSuboperations;
    }

    /**
     * 
     * Sets the number of sub-operations.
     *
     * @param numberOfSuboperations The number of sub-operations.
     */
    public void setNumberOfSuboperations(int numberOfSuboperations) {
        this.numberOfSuboperations = numberOfSuboperations;
    }

    /**
     * 
     * Gets the current status of the operation.
     *
     * @return The status.
     */
    public Status getState() {
        return state;
    }

    /**
     * 
     * Gets the response handler factory.
     *
     * @return The factory.
     */
    public RSPHandlerFactory getRspHandlerFactory() {
        return rspHandlerFactory;
    }

    /**
     * 
     * Establishes a DICOM association with the remote AE.
     *
     * @throws IOException if the connection fails.
     */
    public synchronized void open() throws IOException {
        countdown.set(false);
        try {
            as = ae.connect(remote, rq);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            as = null;
            Logger.trace("Connecting to remote destination", e);
        }
        if (as == null) {
            throw new IOException("Cannot connect to the remote destination");
        }
    }

    /**
     * 
     * Closes the DICOM association.
     *
     * @param force If {@code true}, closes immediately. If {@code false}, only closes if the countdown is active.
     */
    public synchronized void close(boolean force) {
        if (force || countdown.compareAndSet(true, false)) {
            if (as != null) {
                try {
                    Logger.info("Closing DICOM association");
                    if (as.isReadyForDataTransfer()) {
                        as.release();
                    }
                    as.waitForSocketClose();
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    Logger.trace("Cannot close association", e);
                }
                as = null;
            }
        }
    }

    /**
     * 
     * Adds a presentation context for a given SOP Class and Transfer Syntax to the association request.
     *
     * @param cuid  The SOP Class UID.
     * @param tsuid The Transfer Syntax UID.
     * @return {@code true} if the presentation context was added or already existed.
     */
    public boolean addData(String cuid, String tsuid) {
        countdown.set(false);
        if (cuid == null || tsuid == null) {
            return false;
        }

        if (rq.containsPresentationContextFor(cuid, tsuid)) {
            return true;
        }

        if (!rq.containsPresentationContextFor(cuid)) {
            if (relExtNeg) {
                rq.addCommonExtendedNegotiation(relSOPClasses.getCommonExtended(cuid));
            }
            if (!tsuid.equals(UID.ExplicitVRLittleEndian.uid)) {
                rq.addPresentationContext(
                        new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid,
                                UID.ExplicitVRLittleEndian.uid));
            }
            if (!tsuid.equals(UID.ImplicitVRLittleEndian.uid)) {
                rq.addPresentationContext(
                        new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid,
                                UID.ImplicitVRLittleEndian.uid));
            }
        }
        rq.addPresentationContext(new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid, tsuid));
        return true;
    }

    /**
     * 
     * Starts a countdown to close the association after a period of inactivity.
     */
    public synchronized void triggerCloseExecutor() {
        if ((scheduledFuture == null || scheduledFuture.isDone()) && countdown.compareAndSet(false, true)) {
            scheduledFuture = closeAssociationExecutor.schedule(closeAssociationTask, 15, TimeUnit.SECONDS);
        }
    }

    /**
     * 
     * Prepares for a transfer by ensuring the association is open and the necessary presentation contexts are
     * negotiated.
     *
     * @param service  The central service managing the SCU.
     * @param iuid     The SOP Instance UID of the object to be transferred.
     * @param cuid     The SOP Class UID of the object.
     * @param dstTsuid The destination transfer syntax.
     * @throws IOException if an I/O error occurs.
     */
    public void prepareTransfer(Centre service, String iuid, String cuid, String dstTsuid) throws IOException {
        synchronized (this) {
            if (hasAssociation()) {
                checkNewSopClassUID(cuid, dstTsuid);
                addData(cuid, dstTsuid);
                if (ImageOutputData.isAdaptableSyntax(dstTsuid)) {
                    addData(cuid, UID.JPEGLosslessSV1.uid);
                }

                if (!isReadyForDataTransfer()) {
                    open();
                }
            } else {
                service.start();
                addData(cuid, dstTsuid);
                if (!dstTsuid.equals(UID.ExplicitVRLittleEndian.uid)) {
                    addData(cuid, UID.ExplicitVRLittleEndian.uid);
                }
                if (ImageOutputData.isAdaptableSyntax(dstTsuid)) {
                    addData(cuid, UID.JPEGLosslessSV1.uid);
                }
                open();
            }
            addIUIDProcessed(iuid);
        }
    }

    /**
     * 
     * Handles the dynamic addition of a new transfer syntax by waiting for current transfers to complete and then
     * closing the association so it can be re-negotiated.
     *
     * @param cuid     The SOP Class UID.
     * @param dstTsuid The new destination transfer syntax.
     */
    private void checkNewSopClassUID(String cuid, String dstTsuid) {
        Set<String> tss = getTransferSyntaxesFor(cuid);
        if (!tss.contains(dstTsuid)) {
            countdown.set(false);
            int loop = 0;
            while (true) {
                try {
                    if (instanceUidsCurrentlyProcessed.isEmpty()) {
                        break;
                    }
                    TimeUnit.MILLISECONDS.sleep(20);
                    loop++;
                    if (loop > 3000) { // Let 1 min max
                        Logger.warn("prepareTransfer: StreamSCU timeout reached");
                        instanceUidsCurrentlyProcessed.clear();
                        break;
                    }
                } catch (InterruptedException e) {
                    Logger.error("prepareTransfer: InterruptedException {}", e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            close(true);
        }
    }

    /**
     * 
     * Removes a SOP Instance UID from the map of currently processed instances.
     *
     * @param iuid The UID to remove.
     */
    public void removeIUIDProcessed(String iuid) {
        instanceUidsCurrentlyProcessed.computeIfPresent(iuid, (k, v) -> v > 1 ? v - 1 : null);
    }

    /**
     * 
     * Adds a SOP Instance UID to the map of currently processed instances.
     *
     * @param iuid The UID to add.
     */
    private void addIUIDProcessed(String iuid) {
        instanceUidsCurrentlyProcessed.merge(iuid, 1, Integer::sum);
    }

    /**
     * 
     * A factory for creating DIMSE response handlers.
     */
    @FunctionalInterface
    public interface RSPHandlerFactory {

        /**
         * 
         * Creates a new DIMSE response handler.
         *
         * @return A new {@link DimseRSPHandler}.
         */
        DimseRSPHandler createDimseRSPHandler();
    }

}
