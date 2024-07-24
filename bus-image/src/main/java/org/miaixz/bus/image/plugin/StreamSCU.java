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
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamSCU {

    public final RelatedSOPClasses relSOPClasses = new RelatedSOPClasses();
    // Map which corresponds to the instances UIDs currently processed: the value corresponds to the
    // number of occurence
    // of this uid currently processed
    private final Map<String, Integer> instanceUidsCurrentlyProcessed = new ConcurrentHashMap<>();

    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private final Device device;
    private final Connection conn;
    private final Status state;
    private final Args options;
    private final AtomicBoolean countdown = new AtomicBoolean(false);
    private final ScheduledExecutorService closeAssociationExecutor =
            Executors.newSingleThreadScheduledExecutor();
    private Attributes attrs;
    private boolean relExtNeg;
    private Association as;
    private final TimerTask closeAssociationTask =
            new TimerTask() {
                public void run() {
                    close(false);
                }
            };
    private int lastStatusCode = Integer.MIN_VALUE;
    private int nbStatusLog = 0;
    private int numberOfSuboperations = 0;
    private final RSPHandlerFactory rspHandlerFactory =
            () ->
                    new DimseRSPHandler(as.nextMessageID()) {

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
                                        if (Logger.isDebugEnabled()) {
                                            Logger.warn(
                                                    "Received C-STORE-RSP with Status {}H{}",
                                                    Tag.shortToHexString(status),
                                                    "\r\n" + cmd);
                                        } else {
                                            Logger.warn(
                                                    "Received C-STORE-RSP with Status {}H",
                                                    Tag.shortToHexString(status));
                                        }
                                    }
                                    break;

                                default:
                                    ps = ProgressStatus.FAILED;
                                    if (lastStatusCode != status && nbStatusLog < 3) {
                                        nbStatusLog++;
                                        lastStatusCode = status;
                                        if (Logger.isDebugEnabled()) {
                                            Logger.error(
                                                    "Received C-STORE-RSP with Status {}H{}",
                                                    Tag.shortToHexString(status),
                                                    "\r\n" + cmd);
                                        } else {
                                            Logger.error(
                                                    "Received C-STORE-RSP with Status {}H",
                                                    Tag.shortToHexString(status));
                                        }
                                    }
                            }
                            Builder.notifyProgession(state.getProgress(), cmd, ps, numberOfSuboperations);
                        }
                    };
    private ScheduledFuture<?> scheduledFuture;

    public StreamSCU(Node callingNode, Node calledNode) throws IOException {
        this(null, callingNode, calledNode, null);
    }

    public StreamSCU(Args params, Node callingNode, Node calledNode)
            throws IOException {
        this(params, callingNode, calledNode, null);
    }

    public StreamSCU(
            Args params, Node callingNode, Node calledNode, ImageProgress progress)
            throws IOException {
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

        rq.addPresentationContext(
                new PresentationContext(1, UID.Verification.uid, UID.ImplicitVRLittleEndian.uid));

        options.configureConnect(rq, remote, calledNode);
        options.configureBind(ae, conn, callingNode);

        // configure
        options.configure(conn);
        options.configureTLS(conn, remote);

        setAttributes(new Attributes());
    }

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

    public void cstore(String cuid, String iuid, int priority, DataWriter dataWriter, String tsuid)
            throws IOException, InterruptedException {
        if (as == null) {
            throw new IllegalStateException("Association is null!");
        }
        as.cstore(cuid, iuid, priority, dataWriter, tsuid, rspHandlerFactory.createDimseRSPHandler());
    }

    public Node getCallingNode() {
        return new Node(ae.getAETitle(), conn.getHostname(), conn.getPort());
    }

    public Node getCalledNode() {
        return new Node(rq.getCalledAET(), remote.getHostname(), remote.getPort());
    }

    public Node getLocalDicomNode() {
        if (as == null) {
            return null;
        }
        return Node.buildLocalDicomNode(as);
    }

    public Node getRemoteDicomNode() {
        if (as == null) {
            return null;
        }
        return Node.buildRemoteDicomNode(as);
    }

    public String selectTransferSyntax(String cuid, String tsuid) {
        return selectTransferSyntax(as, cuid, tsuid);
    }

    public Device getDevice() {
        return device;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public Attributes getAttributes() {
        return attrs;
    }

    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    public final void enableSOPClassRelationshipExtNeg(boolean enable) {
        relExtNeg = enable;
    }

    public Args getOptions() {
        return options;
    }

    public boolean hasAssociation() {
        return as != null;
    }

    public boolean isReadyForDataTransfer() {
        if (as == null) {
            return false;
        }
        return as.isReadyForDataTransfer();
    }

    public Set<String> getTransferSyntaxesFor(String cuid) {
        if (as == null) {
            return Collections.emptySet();
        }
        return as.getTransferSyntaxesFor(cuid);
    }

    public int getNumberOfSuboperations() {
        return numberOfSuboperations;
    }

    public void setNumberOfSuboperations(int numberOfSuboperations) {
        this.numberOfSuboperations = numberOfSuboperations;
    }

    public Status getState() {
        return state;
    }

    public RSPHandlerFactory getRspHandlerFactory() {
        return rspHandlerFactory;
    }

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
                        new PresentationContext(
                                rq.getNumberOfPresentationContexts() * 2 + 1, cuid, UID.ExplicitVRLittleEndian.uid));
            }
            if (!tsuid.equals(UID.ImplicitVRLittleEndian.uid)) {
                rq.addPresentationContext(
                        new PresentationContext(
                                rq.getNumberOfPresentationContexts() * 2 + 1, cuid, UID.ImplicitVRLittleEndian.uid));
            }
        }
        rq.addPresentationContext(
                new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid, tsuid));
        return true;
    }

    public synchronized void triggerCloseExecutor() {
        if ((scheduledFuture == null || scheduledFuture.isDone())
                && countdown.compareAndSet(false, true)) {
            scheduledFuture =
                    closeAssociationExecutor.schedule(closeAssociationTask, 15, TimeUnit.SECONDS);
        }
    }

    public void prepareTransfer(Centre service, String iuid, String cuid, String dstTsuid)
            throws IOException {
        synchronized (this) {
            if (hasAssociation()) {
                // Handle dynamically new SOPClassUID
                checkNewSopClassUID(cuid, dstTsuid);

                // Add Presentation Context for the association
                addData(cuid, dstTsuid);
                if (ImageOutputData.isAdaptableSyntax(dstTsuid)) {
                    addData(cuid, UID.JPEGLosslessSV1.uid);
                }

                if (!isReadyForDataTransfer()) {
                    Logger.debug("prepareTransfer: as not ready for data transfer, reopen");
                    // If connection has been closed just reopen
                    open();
                }
            } else {
                service.start();
                // Add Presentation Context for the association
                addData(cuid, dstTsuid);
                if (!dstTsuid.equals(UID.ExplicitVRLittleEndian.uid)) {
                    addData(cuid, UID.ExplicitVRLittleEndian.uid);
                }
                if (ImageOutputData.isAdaptableSyntax(dstTsuid)) {
                    addData(cuid, UID.JPEGLosslessSV1.uid);
                }
                Logger.debug("prepareTransfer: connecting to the remote destination");
                open();
            }

            // Add IUID to process
            addIUIDProcessed(iuid);
        }
    }

    /**
     * Check if a new transfer syntax needs to be dynamically added to the association. If yes, wait
     * until the end of the current transfers of the streamSCU and close association to add new
     * transfer syntax.
     *
     * @param cuid     cuid
     * @param dstTsuid List of transfer syntax of the association
     */
    private void checkNewSopClassUID(String cuid, String dstTsuid) {
        Set<String> tss = getTransferSyntaxesFor(cuid);
        if (!tss.contains(dstTsuid)) {
            Logger.debug("prepareTransfer: New output transfer syntax {}: closing streamSCU", dstTsuid);
            countdown.set(false);
            int loop = 0;
            boolean runLoop = true;
            while (runLoop) {
                try {
                    if (instanceUidsCurrentlyProcessed.isEmpty()) {
                        Logger.debug("prepareTransfer: StreamSCU has no more IUID to process: stop waiting");
                        break;
                    }
                    Logger.debug("prepareTransfer: StreamSCU has some IUID to process: waiting 20 ms");
                    TimeUnit.MILLISECONDS.sleep(20);
                    loop++;
                    if (loop > 3000) { // Let 1 min max
                        Logger.warn("prepareTransfer: StreamSCU timeout reached");
                        runLoop = false;
                        instanceUidsCurrentlyProcessed.clear();
                    }
                } catch (InterruptedException e) {
                    Logger.error(String.format("prepareTransfer: InterruptedException %s", e.getMessage()));
                    runLoop = false;
                    Thread.currentThread().interrupt();
                }
            }
            Logger.info(
                    "prepareTransfer: Close association to handle dynamically new SOPClassUID: {}", cuid);
            close(true);
        }
    }

    /**
     * Manage the map corresponding to the uids currently processed: remove the uid of the map if only
     * 1 occurence, otherwise remove 1 occurence number
     *
     * @param iuid Uid to remove from the map
     */
    public void removeIUIDProcessed(String iuid) {
        if (instanceUidsCurrentlyProcessed.containsKey(iuid)
                && instanceUidsCurrentlyProcessed.get(iuid) < 2) {
            instanceUidsCurrentlyProcessed.remove(iuid);
        } else {
            instanceUidsCurrentlyProcessed.computeIfPresent(iuid, (k, v) -> v - 1);
        }
    }

    /**
     * Manage the map corresponding to the uids currently processed: add the uid to the map if uid not
     * existing add 1 occurrence, otherwise add 1 occurence number to the existing uid
     *
     * @param iuid Uid to add in the map
     */
    private void addIUIDProcessed(String iuid) {
        if (instanceUidsCurrentlyProcessed.isEmpty()
                || !instanceUidsCurrentlyProcessed.containsKey(iuid)) {
            instanceUidsCurrentlyProcessed.put(iuid, 1);
        } else {
            instanceUidsCurrentlyProcessed.computeIfPresent(iuid, (k, v) -> v + 1);
        }
    }

    @FunctionalInterface
    public interface RSPHandlerFactory {
        DimseRSPHandler createDimseRSPHandler();
    }

}
